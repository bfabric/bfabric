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

package org.bfabric.forms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.entity.Account;
import org.bfabric.entity.BillingInfo;
import org.bfabric.entity.Booking;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.CostCentre;
import org.bfabric.entity.Country;
import org.bfabric.entity.Currency;
import org.bfabric.entity.Division;
import org.bfabric.entity.FinancialCenter;
import org.bfabric.entity.Institute;
import org.bfabric.entity.User;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.CountryService;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveBooking;

public class MFBooking extends AbstractMF {

    private final Booking booking;

    private final XMLRequestParameterSaveBooking xmlRequestSaveBooking;

    public MFBooking(Booking booking, XMLRequestParameterSaveBooking xmlRequestSaveBooking) {
        this.booking = booking;
        this.xmlRequestSaveBooking = xmlRequestSaveBooking;
    }

    @Override
    public void apply() throws Exception {
        getBooking().setAccount(getAccount());
        getBooking().setBookingDate(getBookingDate());
        getBooking().setBookingIssuer(getBookingIssuer());
        getBooking().setBookingNr(getBookingNr());
        getBooking().setContainer(getContainer());
        getBooking().setCostCentre(getCostCentre());
        getBooking().setCurrency(getCurrency());
        if (getXmlRequestSaveBooking().getDivisionid() != null) {
            getBooking().setDivisionHierarchy(getDivision());
        }
        if (getXmlRequestSaveBooking().getInstituteid() != null) {
            getBooking().setInstituteHierarchy(getInstitute());
        }
        getBooking().setExecutionPeriodEndDate(getExecutionPeriodEndDate());
        getBooking().setExecutionPeriodStartDate(getExecutionPeriodStartDate());
        getBooking().setFinancialCenter(getFinancialCenter());
        getBooking().setOldServiceOrderBookingId(getOldServiceOrderBookingId());
        getBooking().setOrderDate(getOrderDate());
        getBooking().setPaid(getPaid());
        getBooking().setRoundingValue(getRoundingValue());
        getBooking().setSapNumber(getSapNumber());
        getBooking().setSapNumberNext(getSapNumberNext());
        getBooking().setSubTotal(getSubTotal());
        getBooking().setTax(getTax());
        getBooking().setTotal(getTotal());
        getBooking().setTotalCharges(getTotalCharges());
        getBooking().setDescription(getDescription());
        getBooking().setName(getName());
        getBooking().setBillingInfo(getBillingInfo());
        if (getBooking().getBillingInfo() == null) {
            throw new InvalidDataException("Billing info is null!");
        }
        getBooking().getBillingInfo().checkBillingInfo();
        if (!getBooking().getCharges().isEmpty()) {
            getBooking().getInitialCharges().addAll(getBooking().getCharges());
        }
        getBooking().setCharges(getCharges());
        getBooking().setName();
    }

    public Account getAccount() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getAccountid() != null) {
            return (Account) fetch(Account.class, MFHelper.positiveLongValueOf("accountid", getXmlRequestSaveBooking().getAccountid()));
        }
        return getBooking().getAccount();
    }

    public String getBillingAddressCity() {
        if (getXmlRequestSaveBooking().getBillingaddresscity() != null) {
            return getXmlRequestSaveBooking().getBillingaddresscity();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingAddressCity();
    }

    public Country getBillingAddressCountry() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getBillingaddresscountryid() != null) {
            Country country = CDI.current().select(CountryService.class).get()
                .find(Country.class, getXmlRequestSaveBooking().getBillingaddresscountryid());
            if (country == null) {
                throw new InvalidDataException("There is no country with id " + getXmlRequestSaveBooking().getBillingaddresscountryid() + "!");
            }
            return country;
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingAddressCountry();
    }

    public String getBillingAddressStreet() {
        if (getXmlRequestSaveBooking().getBillingaddressstreet() != null) {
            return getXmlRequestSaveBooking().getBillingaddressstreet();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingAddressStreet();
    }

    public String getBillingAddressSupplement() {
        if (getXmlRequestSaveBooking().getBillingaddresssupplement() != null) {
            return getXmlRequestSaveBooking().getBillingaddresssupplement();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingAddressSupplement();
    }

    public String getBillingAddressZip() {
        if (getXmlRequestSaveBooking().getBillingaddresszip() != null) {
            return getXmlRequestSaveBooking().getBillingaddresszip();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingAddressZip();
    }

    public String getBillingCustomerFirstName() {
        if (getXmlRequestSaveBooking().getBillingcustomerfirstname() != null) {
            return getXmlRequestSaveBooking().getBillingcustomerfirstname();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingCustomerFirstName();
    }

    public String getBillingCustomerLastName() {
        if (getXmlRequestSaveBooking().getBillingcustomerlastname() != null) {
            return getXmlRequestSaveBooking().getBillingcustomerlastname();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingCustomerLastName();
    }

    public String getBillingCustomerTitle() {
        if (getXmlRequestSaveBooking().getBillingcustomertitle() != null) {
            return getXmlRequestSaveBooking().getBillingcustomertitle();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingCustomerTitle();
    }

    public String getBillingEmail() {
        if (getXmlRequestSaveBooking().getBillingemail() != null) {
            return getXmlRequestSaveBooking().getBillingemail();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getBillingEmail();
    }

    public BillingInfo getBillingInfo() throws InvalidDataException {
        if (getBillingAddressCountry() != null || getBillingAddressCity() != null || (getBillingAddressSupplement() != null) || getBillingAddressStreet() != null || getBillingAddressZip() != null || getBillingCustomerFirstName() != null || getBillingCustomerLastName() != null || getBillingCustomerTitle() != null || getBillingEmail() != null || getReferenceNumber() != null || getVatNumber() != null) {
            BillingInfo billingInfo = new BillingInfo();
            billingInfo.setBillingAddressCity(getBillingAddressCity());
            billingInfo.setBillingAddressStreet(getBillingAddressStreet());
            billingInfo.setBillingAddressSupplement(getBillingAddressSupplement());
            billingInfo.setBillingAddressZip(getBillingAddressZip());
            billingInfo.setBillingCustomerFirstName(getBillingCustomerFirstName());
            billingInfo.setBillingCustomerLastName(getBillingCustomerLastName());
            billingInfo.setBillingCustomerTitle(getBillingCustomerTitle());
            billingInfo.setBillingEmail(getBillingEmail());
            billingInfo.setReferenceNumber(getReferenceNumber());
            billingInfo.setVatNumber(getVatNumber());
            billingInfo.setBillingAddressCountry(getBillingAddressCountry());
            return billingInfo;
        }
        return getBooking().getBillingInfo();
    }

    public Booking getBooking() {
        return booking;
    }

    public LocalDate getBookingDate() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getBookingdate() != null) {
            return MFHelper.dateValueOf("bookingdate", getXmlRequestSaveBooking().getBookingdate());
        }
        return getBooking().getBookingDate();
    }

    private User getBookingIssuer() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getBookingissuerid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("bookingissuerid", getXmlRequestSaveBooking().getBookingissuerid()));
        }
        return getBooking().getBookingIssuer();
    }

    private Long getBookingNr() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getBookingnr() != null) {
            return MFHelper.longValueOf("bookingnr", getXmlRequestSaveBooking().getBookingnr());
        }
        return getBooking().getBookingNr();
    }

    public Set<Charge> getCharges() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getCharges() != null) {
            if (getXmlRequestSaveBooking().getCharges().isEmpty()) {
                throw new InvalidDataException("Charges must be non empty!");
            }
            Set<Charge> charges = new HashSet<>();
            for (String charge : getXmlRequestSaveBooking().getCharges()) {
                if (StringHelper.isNotEmpty(charge)) {
                    Charge chargeEntity = (Charge) fetch(Charge.class, MFHelper.positiveLongValueOf("charges", charge));
                    if (chargeEntity.isBooked()) {
                        throw new InvalidDataException("The following charge is already booked: " + charge);
                    }
                    charges.add((Charge) fetch(Charge.class, MFHelper.positiveLongValueOf("charges", charge)));
                }
            }
            return charges;
        }
        return getBooking().getCharges();
    }

    public Container getContainer() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getContainerid() != null) {
            Container container = (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveBooking().getContainerid()));
            if (!container.isExtensible()) {
                throw new InvalidDataException("Container " + getXmlRequestSaveBooking().getContainerid() + " is not extensible!");
            }
            return container;
        }
        return getBooking().getContainer();
    }

    public CostCentre getCostCentre() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getCostcentreid() != null) {
            return (CostCentre) fetch(CostCentre.class, MFHelper.positiveLongValueOf("costcentreid", getXmlRequestSaveBooking().getCostcentreid()));
        }
        return getBooking().getCostCentre();
    }

    public Currency getCurrency() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getCurrencyid() != null) {
            return (Currency) fetch(Currency.class, MFHelper.positiveLongValueOf("currencyid", getXmlRequestSaveBooking().getCurrencyid()));
        }
        return getBooking().getCurrency();
    }

    public String getDescription() {
        if (getXmlRequestSaveBooking().getDescription() != null) {
            return getXmlRequestSaveBooking().getDescription();
        }
        return getBooking().getDescription();
    }

    private Division getDivision() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getDivisionid() != null) {
            return (Division) fetch(Division.class, MFHelper.positiveLongValueOf("divisionid", getXmlRequestSaveBooking().getDivisionid()));
        }
        return getBooking().getDivision();
    }

    public LocalDate getExecutionPeriodEndDate() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getExecutionperiodenddate() != null) {
            return MFHelper.dateValueOf("executionperiodenddate", getXmlRequestSaveBooking().getExecutionperiodenddate());
        }
        return getBooking().getExecutionPeriodEndDate();
    }

    public LocalDate getExecutionPeriodStartDate() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getExecutionperiodstartdate() != null) {
            return MFHelper.dateValueOf("executionperiodstartdate", getXmlRequestSaveBooking().getExecutionperiodstartdate());
        }
        return getBooking().getExecutionPeriodStartDate();
    }

    public FinancialCenter getFinancialCenter() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getFinancialcenterid() != null) {
            return (FinancialCenter) fetch(FinancialCenter.class, MFHelper.positiveLongValueOf("financialcenterid", getXmlRequestSaveBooking().getFinancialcenterid()));
        }
        return getBooking().getFinancialCenter();
    }

    private Institute getInstitute() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getInstituteid() != null) {
            return (Institute) fetch(Institute.class, MFHelper.positiveLongValueOf("instituteid", getXmlRequestSaveBooking().getInstituteid()));
        }
        return getBooking().getInstitute();
    }

    public String getName() {
        if (getXmlRequestSaveBooking().getName() != null) {
            return getXmlRequestSaveBooking().getName();
        }
        return getBooking().getName();
    }

    private Long getOldServiceOrderBookingId() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getOldserviceorderbookingid() != null) {
            return MFHelper.longValueOf("oldserviceorderbookingid", getXmlRequestSaveBooking().getOldserviceorderbookingid());
        }
        return getBooking().getOldServiceOrderBookingId();
    }

    public LocalDate getOrderDate() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getOrderdate() != null) {
            return MFHelper.dateValueOf("orderdate", getXmlRequestSaveBooking().getOrderdate());
        }
        return getBooking().getOrderDate();
    }

    public Boolean getPaid() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getPaid() != null) {
            return MFHelper.booleanValueOf("paid", getXmlRequestSaveBooking().getPaid());
        }
        return getBooking().getPaid();
    }

    public String getReferenceNumber() {
        if (getXmlRequestSaveBooking().getReferencenumber() != null) {
            return getXmlRequestSaveBooking().getReferencenumber();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getReferenceNumber();
    }

    private BigDecimal getRoundingValue() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getRoundingvalue() != null) {
            return MFHelper.bigDecimalValueOf("roundingvalue", getXmlRequestSaveBooking().getRoundingvalue());
        }
        return getBooking().getRoundingValue();
    }

    private Long getSapNumber() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getSapnumber() != null) {
            return MFHelper.longValueOf("sapnumber", getXmlRequestSaveBooking().getSapnumber());
        }
        return getBooking().getSapNumber();
    }

    private Long getSapNumberNext() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getSapnumbernext() != null) {
            return MFHelper.longValueOf("sapnumbernext", getXmlRequestSaveBooking().getSapnumbernext());
        }
        return getBooking().getSapNumberNext();
    }

    private BigDecimal getSubTotal() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getSubtotal() != null) {
            return MFHelper.bigDecimalValueOf("subtotal", getXmlRequestSaveBooking().getSubtotal());
        }
        return getBooking().getSubTotal();
    }

    private BigDecimal getTax() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getTax() != null) {
            return MFHelper.bigDecimalValueOf("tax", getXmlRequestSaveBooking().getTax());
        }
        return getBooking().getTax();
    }

    private BigDecimal getTotal() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getTotal() != null) {
            return MFHelper.bigDecimalValueOf("total", getXmlRequestSaveBooking().getTotal());
        }
        return getBooking().getTotal();
    }

    private BigDecimal getTotalCharges() throws InvalidDataException {
        if (getXmlRequestSaveBooking().getTotalcharges() != null) {
            return MFHelper.bigDecimalValueOf("totalcharges", getXmlRequestSaveBooking().getTotalcharges());
        }
        return getBooking().getTotalCharges();
    }

    public String getVatNumber() {
        if (getXmlRequestSaveBooking().getVatnumber() != null) {
            return getXmlRequestSaveBooking().getVatnumber();
        }
        return getBooking().getBillingInfo() == null ? null : getBooking().getBillingInfo().getVatNumber();
    }

    public XMLRequestParameterSaveBooking getXmlRequestSaveBooking() {
        return xmlRequestSaveBooking;
    }
}
