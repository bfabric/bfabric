/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.BookingService;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.LocalDateInterval;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class Booking extends AbstractContainerDependentEntity implements Indexable, HasAffiliation {

    private static final long serialVersionUID = 1;

    @Transient
    private final List<Charge> initialCharges = new ArrayList<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountid")
    @XmlIDREF
    private Account account;

    @Embedded
    @XmlElement
    private BillingInfo billingInfo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookerid")
    @XmlIDREF
    private Booker booker;

    @NotNull
    @XmlElement
    private LocalDate bookingDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingissuer")
    @XmlIDREF
    private User bookingIssuer;

    @NotNull
    @XmlElement
    private long bookingNr;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingtypeid")
    @XmlIDREF
    private BookingType bookingType;

    @XmlElement
    private LocalDate cancellationDate;

    @OneToMany(mappedBy = "booking")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @Transient
    private Company company;

    @Transient
    private String companyName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costcentreid")
    @XmlIDREF
    private CostCentre costCentre;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyid")
    @XmlIDREF
    private Currency currency;

    @NotNull
    @XmlElement
    private BigDecimal currencyRate = BigDecimal.ONE;

    @Transient
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisionid")
    @XmlIDREF
    private Division division;

    @Transient
    private String divisionName;

    @NotNull
    @XmlElement
    private LocalDate executionPeriodEndDate;

    @NotNull
    @XmlElement
    private LocalDate executionPeriodStartDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financialcenterid")
    @XmlIDREF
    private FinancialCenter financialCenter;

    @Transient
    private List<Charge> groupedChargesByServiceAndDiscount;

    @Transient
    private List<Charge> groupedChargesByServiceAndTax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituteid")
    @XmlIDREF
    private Institute institute;

    private Long oldServiceOrderBookingId;

    @NotNull
    @XmlElement
    private LocalDate orderDate;

    @Transient
    private Organization organization;

    @Transient
    private OrganizationType organizationType;

    @XmlElement
    private Boolean paid;

    @XmlElement
    private LocalDate paymentDate;

    @NotNull
    @XmlElement
    private BigDecimal roundingValue;

    @XmlElement
    private Long sapNumber;

    @XmlElement
    private Long sapNumberNext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sapRecordedById")
    @XmlIDREF
    private User sapRecordedBy;

    @NotNull
    @XmlElement
    private BigDecimal subTotal;

    @NotNull
    @XmlElement
    private BigDecimal tax;

    @NotNull
    @XmlElement
    private BigDecimal total;

    @NotNull
    @XmlElement
    private BigDecimal totalCharges;

    public Booking() {
        super();
    }

    public void calculateCharges() {
        final BigDecimal tax = getTotalTax();
        final BigDecimal totalChargesWithoutTax = getTotalDiscountedPrice().multiply(getCurrencyRate());
        setTotalCharges(totalChargesWithoutTax);
        setTax(tax);
        setSubTotal(totalChargesWithoutTax.add(tax));
        BigDecimal roundedSubTotal = NumberUtils.getRoundedPrice(getSubTotal(), getConfiguration().getDefaultCurrencyCode());
        setRoundingValue(roundedSubTotal.subtract(getSubTotal()));
        setTotal(roundedSubTotal);
    }

    @Override
    public void clearButtonOrganization() {
        HasAffiliation.super.clearButtonOrganization();
        getBillingInfo().setDebitorNumber(null);
        getBillingInfo().setVatNumber(null);
    }

    @Override
    public void companyChanged(ValueChangeEvent event) {
        HasAffiliation.super.companyChanged(event);
        resetVatAndDebitorNumber();
        setVatAndDebitorNumberOfCompany((Company) event.getNewValue());
    }

    @Override
    public void departmentChanged(ValueChangeEvent event) {
        Organization oldOrganization = getOrganization();
        HasAffiliation.super.departmentChanged(event);
        if (oldOrganization == null || !oldOrganization.equals(getOrganization())) {
            resetVatAndDebitorNumber();
            setVatAndDebitorNumberOfOrganization(getOrganization());
        }
    }

    public void executionPeriodEndDateChanged(ValueChangeEvent event) {
        setExecutionPeriodEndDate((LocalDate) event.getNewValue());
    }

    public void executionPeriodStartDateChanged(ValueChangeEvent event) {
        setExecutionPeriodStartDate((LocalDate) event.getNewValue());
    }

    public void exportForETHIS() {
        download(generateExportedBookingFileName(".txt"), generateCSVForETHIS(generateExportedBookingFileName(".pdf")));
    }

    /**
     * Generate the csv used for ETHIS.
     * The comma separated values for every booked item from groupedChargesByServiceAndDiscount are (in this exact order):
     * - Date: Date when the csv string is generated in yyyy-MM-dd format.
     * - From: Start of the execution period in yyyy-MM-dd format.
     * - To: End of the execution period in yyyy-MM-dd format. This can be empty for the EPIC interface.
     * - Cost centre / customer number: Cost centre for internal accounting and debitor number for external accounting prefixed with 'ext'.
     * - Project number: Container id.
     * - Material number of the SAP: Service code of the charged item.
     * - Quantity: The quantity.
     * - Price: The unit price.
     * - Path to the PDF: This is the path on the Computer where the downloaded booking file should be placed by the user.
     * Requirements:
     * - The cost centre / customer number is limited to a maximum of 10 characters.
     * - The project number is limited to a maximum of 25 characters.
     * - An umlaut needs to be replaced by ae, oe, and ue respectively.
     * - All accents need to be removed, e.g., é, è, and ê become e.
     *
     * @param bookingFileName the bookingFileName
     * @return the csv
     */
    public String generateCSVForETHIS(String bookingFileName) {
        StringBuilder csv = new StringBuilder();
        if (StringHelper.isNotEmpty(bookingFileName)) {
            String exportedFilePath = getCurrentUser().getExportBookingPath();
            // Note: For the import to work, the path has to be set in the user settings and the given path needs to be accessible by the import tool, e.g., '\\Client\C$\Users\<yourUser>\Desktop\Bookings\'.
            if (StringHelper.isEmpty(exportedFilePath)) {
                exportedFilePath = Messages.get("configureDownloadDirectoryPath") + "\\";
            } else if (!(exportedFilePath.endsWith("/") || exportedFilePath.endsWith("\\"))) {
                if (exportedFilePath.contains("\\")) {
                    exportedFilePath += "\\";
                } else {
                    exportedFilePath += "/";
                }
            }
            exportedFilePath = "\"" + exportedFilePath + bookingFileName + "\"\n";

            String date = Constants.DATE_FORMATTER.format(getContainer().getCreated());
            String executionPeriodStartDate = Constants.DATE_FORMATTER.format(getExecutionPeriodStartDate());
            String executionPeriodEndDate = Constants.DATE_FORMATTER.format(getExecutionPeriodEndDate());

            for (Charge charge : getGroupedChargesByServiceAndDiscount()) {
                List<String> stringList = new ArrayList<>();
                stringList.add(date);
                stringList.add(executionPeriodStartDate);
                stringList.add(executionPeriodEndDate);
                stringList.add((!isUmbuchung() ? "ext" : Constants.EMPTY_STRING) + (getCostCentreOrPspElement() != null ? getCostCentreOrPspElement() : getDebitorNumberAsString()));
                stringList.add(getContainer().getTrimmedClassName() + " " + getContainer().getId());
                stringList.add(charge.getServiceCodeName());
                stringList.add(String.valueOf(charge.getAccountedComputed()));
                stringList.add(String.valueOf(charge.getBasicPrice()));
                stringList.add(exportedFilePath);
                csv.append(StringHelper.replaceAccent(StringHelper.replaceUmlaut(CollectionHelper.print(stringList, null, Constants.CSV_SEPARATOR, false))));
            }
        }
        return csv.toString();
    }

    public String generateExportedBookingFileName(String suffix) {
        return getTrimmedClassName() + "_" + getId() + suffix;
    }

    public Account getAccount() {
        return account;
    }

    public BillingInfo getBillingInfo() {
        return billingInfo;
    }

    public Booker getBooker() {
        return booker;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public User getBookingIssuer() {
        return bookingIssuer;
    }

    public long getBookingNr() {
        return bookingNr;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public LocalDate getCancellationDate() {
        return cancellationDate;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public List<Charge> getChargesOrderByServiceAndDiscount() {
        return CDI.current().select(BookingService.class).get().getChargesByBookingIdOrderByServiceAndDiscount(getId());
    }

    public Company getCompany() {
        return company;
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    public LocalDateInterval getComputedExecutionPeriod() {
        LocalDate startDate = null;
        LocalDate endDate = null;
        for (Charge charge : getCharges()) {
            if (startDate == null || startDate.isAfter(charge.getCreatedDate())) {
                startDate = charge.getCreatedDate();
            }
            LocalDateTime startDateInstrumentReservation = charge.getInstrumentReservations().stream().min(Comparator.comparing(InstrumentReservation::getStartDate))
                .map(InstrumentReservation::getStartDate).orElse(null);
            if (startDateInstrumentReservation != null && startDateInstrumentReservation.toLocalDate().isBefore(startDate)) {
                startDate = startDateInstrumentReservation.toLocalDate();
            }
            if (endDate == null || endDate.isBefore(charge.getCreatedDate())) {
                endDate = charge.getCreatedDate();
            }
            LocalDateTime endDateInstrumentReservation = charge.getInstrumentReservations().stream().max(Comparator.comparing(InstrumentReservation::getEndDate)).map(InstrumentReservation::getEndDate)
                .orElse(null);
            if (endDateInstrumentReservation != null && endDateInstrumentReservation.toLocalDate().isAfter(endDate) && endDateInstrumentReservation.toLocalDate().isBefore(LocalDate.now())) {
                endDate = endDateInstrumentReservation.toLocalDate();
            }
        }
        return new LocalDateInterval(startDate, endDate);
    }

    public LocalDateInterval getContainerExecutionPeriod() {
        if (getContainer().getExecutionPeriod() != null) {
            if (getContainer().getExecutionPeriod().getEnd() != null) {
                return getContainer().getExecutionPeriod();
            }
            return new LocalDateInterval(getContainer().getExecutionPeriod().getStart(), getMaxChargeDate());
        }
        return null;
    }

    public CostCentre getCostCentre() {
        return costCentre;
    }

    public String getCostCentreOrPspElement() {
        return getContainer().getCostCentre() != null ? getContainer().getCostCentre() : getContainer().getPspElement();
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    public Long getDebitorNumber() {
        if (getBillingInfo() != null && getBillingInfo().getDebitorNumber() != null) {
            return getBillingInfo().getDebitorNumber();
        } else if (getContainer().getInstitute() != null && getContainer().getInstitute().getOrganization() != null) {
            return getContainer().getInstitute().getOrganization().getDebitorNumber();
        } else if (getContainer().getDivision() != null && getContainer().getDivision().getCompany() != null) {
            return getContainer().getDivision().getCompany().getDebitorNumber();
        }
        return null;
    }

    private String getDebitorNumberAsString() {
        return getDebitorNumber() != null ? String.valueOf(getDebitorNumber()) : Constants.EMPTY_STRING;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.BOOKINGMANAGER;
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    @Override
    public Division getDivision() {
        return division;
    }

    @Override
    public String getDivisionName() {
        return divisionName;
    }

    public List<User> getEmployeesIncludingSapRecordedBy(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getSapRecordedBy());
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getOldServiceOrderBookingId() != null) {
            addEntityInfoItem(summary, "oldServiceOrderBookingId", getOldServiceOrderBookingId());
        }
        if (getCostCentre() != null) {
            addEntityInfoItem(summary, "costCentre", getCostCentre().getCode());
        }
        if (getAccount() != null) {
            addEntityInfoItem(summary, "account", getAccount().getAccountNr());
        }
        if (getBillingInfo() != null) {
            if (StringHelper.isNotEmpty(getBillingInfo().getBillingCustomerName())) {
                addEntityInfoItem(summary, "billingCustomer", getBillingInfo().getBillingCustomerFullName());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getBillingAddressFull())) {
                addEntityInfoItem(summary, "billingAddress", getBillingInfo().getBillingAddressFull());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getBillingEmail())) {
                addEntityInfoItem(summary, "billingEmail", getBillingInfo().getBillingEmail());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getVatNumber())) {
                addEntityInfoItem(summary, "vatNumber", getBillingInfo().getVatNumber());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getReferenceNumber())) {
                addEntityInfoItem(summary, "referenceNumber", getBillingInfo().getReferenceNumber());
            }
        }
        if (getContainer() != null) {
            addEntityInfoItem(summary, "container", getContainer().getId());
        }
        addEntityInfoItem(summary, "total", getTotal());
        if (getDebitorNumber() != null) {
            addEntityInfoItem(summary, "debitorNumber", getDebitorNumber());
        }
        if (getSapNumber() != null) {
            addEntityInfoItem(summary, "sapNumber", getSapNumber());
        }
        if (getSapNumberNext() != null) {
            addEntityInfoItem(summary, "sapNumberNext", getSapNumberNext());
        }
        if (getSapRecordedBy() != null) {
            addEntityInfoItem(summary, "sapRecordedBy", getSapRecordedBy().getName());
        }
        if (getCharges() != null) {
            addEntityInfoItem(summary, "charges", getCharges().size());
        }
        if (getPaid() != null) {
            addEntityInfoItem(summary, "paid", getPaid());
        }
        return summary.toString();
    }

    public LocalDateInterval getExecutionPeriod() {
        return new LocalDateInterval(getExecutionPeriodStartDate(), getExecutionPeriodEndDate());
    }

    public String getExecutionPeriodAsEUFormattedDateString() {
        return Constants.DATE_FORMATTER_EU.format(getExecutionPeriodStartDate()) + " - " + Constants.DATE_FORMATTER_EU.format(getExecutionPeriodEndDate());
    }

    public LocalDate getExecutionPeriodEndDate() {
        return executionPeriodEndDate;
    }

    public LocalDate getExecutionPeriodStartDate() {
        return executionPeriodStartDate;
    }

    public String getExecutionPeriodString() {
        return new LocalDateInterval(getExecutionPeriodStartDate(), getExecutionPeriodEndDate()).getIntervalAsString();
    }

    public String getExportPDFLink() {
        return getReportPDFLink("booking-fop");
    }

    public FinancialCenter getFinancialCenter() {
        return financialCenter;
    }

    public List<Charge> getGroupedChargesByServiceAndDiscount() {
        if (groupedChargesByServiceAndDiscount == null) {
            groupedChargesByServiceAndDiscount = new ArrayList<>();
            for (Charge charge : CDI.current().select(BookingService.class).get().getChargesByBookingIdOrderByServiceAndDiscount(getId())) {
                boolean found = false;
                for (Charge groupedCharge : groupedChargesByServiceAndDiscount) {
                    if (groupedCharge.getService().getName().equalsIgnoreCase(charge.getService().getName())
                        && (groupedCharge.getService().getCode() == null && charge.getService().getCode() == null || groupedCharge.getService().getCode() != null && groupedCharge.getService()
                        .getCode().equalsIgnoreCase(charge.getService().getCode()))
                        && groupedCharge.getDiscount().equals(charge.getDiscount())
                        && groupedCharge.getOrderDiscount().equals(charge.getOrderDiscount())) {
                        groupedCharge.addChargeToGroupedCharge(charge);
                        if (StringHelper.isNotEmpty(charge.getDescription()) && StringHelper.isNotEmpty(groupedCharge.getDescription()) && !charge.getDescription()
                            .contains(groupedCharge.getDescription())) {
                            groupedCharge.setDescription(groupedCharge.getDescription() + ", " + charge.getDescription());
                        }
                        groupedCharge.setGrouped(true);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    try {
                        groupedChargesByServiceAndDiscount.add(charge.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return groupedChargesByServiceAndDiscount;
    }

    public List<Charge> getGroupedChargesByServiceAndTax() {
        if (groupedChargesByServiceAndTax == null) {
            groupedChargesByServiceAndTax = new ArrayList<>();
            for (Charge charge : getChargesOrderByServiceAndDiscount()) {
                boolean found = false;
                for (Charge groupedCharge : groupedChargesByServiceAndTax) {
                    if (groupedCharge.getService().getName().equalsIgnoreCase(charge.getService().getName()) && groupedCharge.getTaxRate().equals(charge.getTaxRate())) {
                        groupedCharge.addChargeToGroupedCharge(charge);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    try {
                        groupedChargesByServiceAndTax.add(charge.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return groupedChargesByServiceAndTax;
    }

    public Map<Technology, List<Charge>> getGroupedChargesByTechnology() {
        Map<Technology, List<Charge>> chargesGroupedByTechnology = new HashMap<>();
        for (Charge charge : getCharges()) {
            Technology technology = charge.getService().getServiceType().getTechnologies().stream().findFirst().get();
            if (chargesGroupedByTechnology.containsKey(technology)) {
                chargesGroupedByTechnology.get(technology).add(charge);
            } else {
                List<Charge> charges = new ArrayList<>();
                charges.add(charge);
                chargesGroupedByTechnology.put(technology, charges);
            }
        }
        return chargesGroupedByTechnology;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = super.getIndexListingFields();
        fields.add(IndexMapContentEnum.BILLINGCUSTOMER.getField());
        fields.add(IndexMapContentEnum.BOOKINGNUMBER.getField());
        fields.add(IndexMapContentEnum.BOOKINGTOTAL.getField());
        fields.add(IndexMapContentEnum.ADDRESS.getField());
        fields.add(IndexMapContentEnum.EMAIL.getField());
        fields.add(IndexMapContentEnum.VATNUMBER.getField());
        fields.add(IndexMapContentEnum.REFERENCENUMBER.getField());
        fields.add(IndexMapContentEnum.INSTITUTE.getField());
        fields.add(IndexMapContentEnum.DEPARTMENT.getField());
        fields.add(IndexMapContentEnum.ORGANIZATION.getField());
        fields.add(IndexMapContentEnum.COMPANY.getField());
        fields.add(IndexMapContentEnum.DIVISION.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        if (getBillingInfo() != null) {
            if (StringHelper.isNotEmpty(getBillingInfo().getBillingCustomerName())) {
                content.add(IndexMapContentEnum.BILLINGCUSTOMER, getBillingInfo().getBillingCustomerFullName());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getBillingAddressFull())) {
                content.add(IndexMapContentEnum.ADDRESS, getBillingInfo().getBillingAddressFull());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getBillingEmail())) {
                content.add(IndexMapContentEnum.EMAIL, getBillingInfo().getBillingEmail());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getReferenceNumber())) {
                content.add(IndexMapContentEnum.VATNUMBER, getBillingInfo().getVatNumber());
            }
            if (StringHelper.isNotEmpty(getBillingInfo().getReferenceNumber())) {
                content.add(IndexMapContentEnum.REFERENCENUMBER, getBillingInfo().getReferenceNumber());
            }
        }

        content.add(IndexMapContentEnum.BOOKINGNUMBER, getBookingNr());
        content.add(IndexMapContentEnum.BOOKINGTOTAL, getTotal());
        if (getInstitute() != null) {
            content.add(IndexMapContentEnum.INSTITUTE, getInstitute().getName());
            content.add(IndexMapContentEnum.DEPARTMENT, getInstitute().getDepartmentName());
            content.add(IndexMapContentEnum.ORGANIZATION, getInstitute().getOrganizationName());
        }
        if (getDivision() != null) {
            content.add(IndexMapContentEnum.COMPANY, getDivision().getCompanyName());
            if (getDivision().isSet()) {
                content.add(IndexMapContentEnum.DIVISION, getDivision().getName());
            }
        }

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.BOOKING;
    }

    public List<Charge> getInitialCharges() {
        return initialCharges;
    }

    @Override
    public Institute getInstitute() {
        return institute;
    }

    public LocalDate getMaxChargeDate() {
        return getCharges().stream().max(Comparator.comparing(Charge::getCreated)).map(Charge::getCreatedDate).orElse(null);
    }

    public LocalDate getMinChargeDate() {
        return getCharges().stream().min(Comparator.comparing(Charge::getCreated)).map(Charge::getCreatedDate).orElse(null);
    }

    public String getOldId() {
        return getOldServiceOrderBookingId() != null ? StringHelper.embraceParentheses(Messages.get("oldServiceOrderBookingId") + " " + getOldServiceOrderBookingId()) : null;
    }

    public Long getOldServiceOrderBookingId() {
        return oldServiceOrderBookingId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public Boolean getPaid() {
        return paid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getPostalAddress() {
        return StringHelper.getPostalAddress(getBillingInfo(), getInstitute(), getDivision());
    }

    public BigDecimal getRoundingValue() {
        return roundingValue;
    }

    public String getRowStyleClass() {
        if (getPaid() != null && getPaid()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (getSapNumberNext() != null && getSapNumberNext() > 0) {
            return Constants.BACKGROUND_COLOR_BLUE_LIGHT;
        }
        if (getSapNumber() != null && getSapNumber() > 0) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        return Constants.BACKGROUND_COLOR_RED;
    }

    public String getSOAPTransferContent() {
        StringBuilder stringBuilder = new StringBuilder();
        // String test = "<budat>20230901</budat><bldat>20230901</bldat><material>FG2600-GHT</material><quantitiy>2.000</quantitiy><preis>1.01</preis><betrag>1.01</betrag><pernr/><objnr>1-001898-000</objnr><objekt>1-001898-000</objekt><hkont>42070001</hkont><dmbtr>1.07</dmbtr><waers>CHF</waers><creator>NOTTERR</creator><email>notterr@ethz.ch</email>";
        for (Charge charge : getGroupedChargesByServiceAndDiscount()) {
            stringBuilder.append("<dataset>");
            stringBuilder.append("<budat>" + Constants.DATE_FORMATTER_ETH.format(getExecutionPeriodEndDate()) + "</budat>");
            stringBuilder.append("<bldat>" + Constants.DATE_FORMATTER_ETH.format(getExecutionPeriodEndDate()) + "</bldat>");
            stringBuilder.append("<material>" + charge.getServiceCodeName() + "</material>");
            stringBuilder.append("<quantity>" + charge.getAccountedComputed() + "</quantity>");
            stringBuilder.append("<preis>" + charge.getBasicPrice() + "</preis>");
            stringBuilder.append("<betrag>" + charge.getPrice() + "</betrag>");
            stringBuilder.append("<pernr>" + "</pernr>");
            stringBuilder.append("<objnr>" + getContainer().getTrimmedClassName() + " " + getContainer().getId() + "</objnr>");
            stringBuilder.append("<objekt>" + getContainer().getTrimmedClassName() + " " + getContainer().getId() + "</objekt>");
            stringBuilder.append("<hkont>" + (!isUmbuchung() ? "ext" : Constants.EMPTY_STRING) + (getCostCentreOrPspElement() != null ? getCostCentreOrPspElement() : getDebitorNumberAsString()) + "</hkont>");
            stringBuilder.append("<dmbtr>" + charge.getPrice() + "</dmbtr>");
            stringBuilder.append("<waers>" + "CHF" + "</waers>");
            stringBuilder.append("<creator>" + "</creator>");
            stringBuilder.append("<email>" + getCurrentUser().getEmail() + "</email>");
            stringBuilder.append("</dataset>");
        }
        return stringBuilder.toString();
    }

    public Long getSapNumber() {
        return sapNumber;
    }

    public Long getSapNumberNext() {
        return sapNumberNext;
    }

    public User getSapRecordedBy() {
        return sapRecordedBy;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public boolean getTaxExemptedForCHUniversity() {
        return getTaxType() != null && getConfiguration().getDefaultTaxTypeName()
            .equalsIgnoreCase(getTaxType().getName()) && getInstitute() != null && (getInstitute().isOrganizationTypeCHUni() || getInstitute()
            .isOrganizationTypeUniZH());
    }

    public TaxType getTaxType() {
        TaxType taxType = null;
        for (Charge charge : getCharges()) {
            taxType = charge.getTaxType();
        }
        return taxType;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getTotalCharges() {
        return totalCharges;
    }

    public BigDecimal getTotalDiscount() {
        return getTotalPrice().subtract(getTotalDiscountedPrice());
    }

    public BigDecimal getTotalDiscountedPrice() {
        BigDecimal totalDiscountedPrice = BigDecimal.ZERO;
        for (Charge charge : getCharges()) {
            totalDiscountedPrice = totalDiscountedPrice.add(charge.getDiscountedPrice());
        }
        return NumberUtils.getDecimalScale2(totalDiscountedPrice);
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Charge charge : getCharges()) {
            totalPrice = totalPrice.add(charge.getPrice());
        }
        return NumberUtils.getDecimalScale2(totalPrice);
    }

    public BigDecimal getTotalPriceWithoutOrderDiscount() {
        BigDecimal totalPriceWithoutOrderDiscount = BigDecimal.ZERO;
        for (Charge charge : getCharges()) {
            totalPriceWithoutOrderDiscount = totalPriceWithoutOrderDiscount.add(charge.getPriceWithItemDiscount());
        }
        return NumberUtils.getDecimalScale2(totalPriceWithoutOrderDiscount);
    }

    public BigDecimal getTotalTax() {
        BigDecimal totalTax = BigDecimal.ZERO;
        for (Charge charge : getCharges()) {
            totalTax = totalTax.add(charge.getTax());
        }
        return NumberUtils.getDecimalScale2(totalTax);
    }

    public String getTransferLink() {
        return getTransferLink("booking-fop");
    }

    public boolean hasBasicPricedServicesOnly() {
        for (Charge charge : getCharges()) {
            if (charge.getAdditionalPrice().doubleValue() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean hasChargeWithItemDiscount() {
        for (Charge charge : getCharges()) {
            if (charge.isDiscounted()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasChargeWithOrderDiscount() {
        if (getContainer() != null) {
            return getContainer().isDiscounted();
        }
        // Check if there is any order charge included in the booking.
        // This case normally should not occur.
        for (Charge charge : getCharges()) {
            if (charge.isOrderDiscounted()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasChargeWithServiceCodeOnly() {
        for (Charge charge : getCharges()) {
            if (StringHelper.isEmpty(charge.getServiceCodeName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void instituteChanged(ValueChangeEvent event) {
        Organization oldOrganization = getOrganization();
        HasAffiliation.super.instituteChanged(event);
        if (oldOrganization == null || !oldOrganization.equals(getOrganization())) {
            resetVatAndDebitorNumber();
            setVatAndDebitorNumberOfOrganization(getOrganization());
        }
    }

    public boolean isBookingType(String bookingTypeName) {
        return getBookingType() != null && getBookingType().getName() != null && getBookingType().getName().equalsIgnoreCase(bookingTypeName);
    }

    public boolean isCSVExportableForETHIS() {
        return getConfiguration().isBookerETHEnabled() && Constants.BOOKER_ETH.equals(getBooker().getName()) && (getCostCentreOrPspElement() != null || getDebitorNumber() != null) &&
            hasChargeWithServiceCodeOnly() && hasBasicPricedServicesOnly() && !hasChargeWithItemDiscount() && !hasChargeWithOrderDiscount();
    }

    public boolean isDebitorNumberRequired() {
        return (!isManaged() || getBillingInfo() != null && getBillingInfo().getOldDebitorNumber() != null) && getOrganizationType() != null && getOrganizationType().isExternal();
    }

    @Override
    public boolean isDeletable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && getContainer().isInBookableState();
    }

    public boolean isDiscounted() {
        return hasChargeWithItemDiscount() || hasChargeWithOrderDiscount();
    }

    public boolean isExportETHISRendered() {
        return hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER) && isCSVExportableForETHIS();
    }

    public boolean isInvoice() {
        return isBookingType(Constants.BOOKING_TYPE_INVOICE);
    }

    public boolean isNotAccounted() {
        boolean notAccounted = false;
        for (Charge sc : getCharges()) {
            if (sc.getNotAccounted().doubleValue() > 0) {
                notAccounted = true;
                break;
            }
        }
        return notAccounted;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || getContainer().isMember();
    }

    public boolean isRenderedDetailedPrintOut() {
        return getGroupedChargesByServiceAndDiscount().size() != getCharges().size();
    }

    public boolean isResetExecutionPeriodRendered() {
        return getExecutionPeriod() != null && !getExecutionPeriod().same(getComputedExecutionPeriod());
    }

    public boolean isTransferEnabled() {
        return getConfiguration().isBookingTransferEnabled() && isExportETHISRendered() && hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    public boolean isUmbuchung() {
        return isBookingType(Constants.BOOKING_TYPE_UMBUCHUNG);
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public void organizationChanged(ValueChangeEvent event) {
        HasAffiliation.super.organizationChanged(event);
        resetVatAndDebitorNumber();
        setVatAndDebitorNumberOfOrganization((Organization) event.getNewValue());
    }

    @Override
    public void organizationTypeChanged(ValueChangeEvent event) {
        HasAffiliation.super.organizationTypeChanged(event);
        resetVatAndDebitorNumber();
    }

    @Override
    protected void prePersist() {
        setName();
        super.prePersist();
    }

    @Override
    protected void preRemove() {
        super.preRemove();
        // Mark the associated charges as not booked anymore.
        for (Charge charge : getCharges()) {
            charge.setBooking(null);
        }
    }

    @Override
    protected void preUpdate() {
        setName();
        super.preUpdate();
    }

    public String printBookingNr() {
        StringBuilder bookingNrText = new StringBuilder();
        if (getCostCentre() != null && StringHelper.isNotEmpty(getCostCentre().getCode())) {
            bookingNrText.append(getCostCentre().getCode());
            bookingNrText.append("/");
        }
        bookingNrText.append(getBookingNr());
        return bookingNrText.toString();
    }

    public void resetExecutionPeriod() {
        LocalDateInterval computedExecutionPeriod = getComputedExecutionPeriod();
        if (computedExecutionPeriod != null) {
            setExecutionPeriodStartDate(computedExecutionPeriod.getStart());
            setExecutionPeriodEndDate(computedExecutionPeriod.getEnd());
        }
    }

    private void resetVatAndDebitorNumber() {
        getBillingInfo().setVatNumber(null);
        getBillingInfo().setDebitorNumber(null);
    }

    public void setAccount(final Account account) {
        this.account = account;
    }

    public void setBillingInfo(BillingInfo billingInfo) {
        this.billingInfo = billingInfo;
    }

    public void setBooker(final Booker booker) {
        this.booker = booker;
    }

    public void setBookingDate(final LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setBookingIssuer(final User bookingIssuer) {
        this.bookingIssuer = bookingIssuer;
    }

    public void setBookingNr(final long bookingNr) {
        this.bookingNr = bookingNr;
    }

    public void setBookingType(final BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public void setCancellationDate(LocalDate cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public void setCharges(final Set<Charge> charges) {
        this.charges = charges;
        if (getExecutionPeriodStartDate() == null || getExecutionPeriodEndDate() == null) {
            resetExecutionPeriod();
        }
    }

    @Override
    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public void setCompanyName(String companyName) {
        this.companyName = StringHelper.format(companyName);
    }

    @Override
    public void setContainer(Container container) {
        super.setContainer(container);
        if (container != null && isContainerChanged()) {
            if (container.getCreated() != null) {
                setOrderDate(container.getCreated().toLocalDate());
            }
            LocalDateInterval containerExecutionPeriod = getContainerExecutionPeriod();
            if (containerExecutionPeriod != null) {
                setExecutionPeriodStartDate(containerExecutionPeriod.getStart());
                setExecutionPeriodEndDate(containerExecutionPeriod.getEnd());
            }
        }
    }

    public void setCostCentre(final CostCentre costCentre) {
        this.costCentre = costCentre;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    public void setCurrencyRate(final BigDecimal currencyRate) {
        this.currencyRate = currencyRate;
    }

    @Override
    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public void setDivision(Division division) {
        this.division = division;
    }

    public void setDivisionHierarchy(Division division) {
        setDivision(division);
        setDivisionName(getDivision().getName());
        setCompany(division.getCompany());
        setCompanyName(getCompany().getName());
        setOrganizationType(getDivision().getOrganizationType());
        if (getBillingInfo() != null) {
            getBillingInfo().setDebitorNumber(getCompany().getDebitorNumber());
        }
        if (getCompany() != null) {
            setBookingType(getCompany().getDefaultBookingType());
            setBooker(getCompany().getDefaultBookingType().getBooker());
        }
    }

    @Override
    public void setDivisionName(String divisionName) {
        this.divisionName = StringHelper.format(divisionName);
    }

    public void setExecutionPeriodEndDate(LocalDate executionPeriodEndDate) {
        this.executionPeriodEndDate = executionPeriodEndDate;
    }

    public void setExecutionPeriodStartDate(LocalDate executionPeriodStartDate) {
        this.executionPeriodStartDate = executionPeriodStartDate;
    }

    public void setFinancialCenter(final FinancialCenter financialCenter) {
        this.financialCenter = financialCenter;
    }

    @Override
    public void setInstitute(Institute institute) {
        this.institute = institute;
    }

    public void setInstituteHierarchy(Institute institute) {
        setInstitute(institute);
        setDepartment(getInstitute().getDepartment());
        setOrganization(getDepartment().getOrganization());
        setOrganizationType(getOrganization().getOrganizationType());
        if (getBillingInfo() != null) {
            getBillingInfo().setDebitorNumber(getOrganization().getDebitorNumber());
        }
        if (getOrganization() != null) {
            setBookingType(getOrganization().getDefaultBookingType());
            setBooker(getOrganization().getDefaultBookingType().getBooker());
        }
    }

    public void setName() {
        StringBuilder nameBuilder = new StringBuilder();
        if (getBooker() != null) {
            nameBuilder.append(getBooker().getName()).append(" ");
        }
        if (getCostCentre() != null) {
            nameBuilder.append(getCostCentre().getCode()).append("/");
        }
        nameBuilder.append(getBookingNr());
        setName(nameBuilder.toString());
    }

    public void setOldServiceOrderBookingId(Long oldServiceOrderBookingId) {
        this.oldServiceOrderBookingId = oldServiceOrderBookingId;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setRoundingValue(final BigDecimal roundingValue) {
        this.roundingValue = roundingValue;
    }

    public void setSapNumber(Long sapNumber) {
        if (this.sapNumber == null && sapNumber != null) {
            setSapRecordedBy(getCurrentUser());
        }
        this.sapNumber = sapNumber;
    }

    public void setSapNumberNext(Long sapNumberNext) {
        if (this.sapNumberNext == null && sapNumberNext != null) {
            setSapRecordedBy(getCurrentUser());
        }
        this.sapNumberNext = sapNumberNext;
    }

    public void setSapRecordedBy(User sapRecordedBy) {
        this.sapRecordedBy = sapRecordedBy;
    }

    public void setSubTotal(final BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public void setTotal(final BigDecimal total) {
        this.total = total;
    }

    public void setTotalCharges(final BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }

    private void setVatAndDebitorNumberOfCompany(Company company) {
        if (company != null) {
            getBillingInfo().setVatAndDebitorNumber(company.getDebitorNumber(), company.getVatNumber());
        }
    }

    private void setVatAndDebitorNumberOfOrganization(Organization organization) {
        if (organization != null) {
            getBillingInfo().setVatAndDebitorNumber(organization.getDebitorNumber(), organization.getVatNumber());
        }
    }
}