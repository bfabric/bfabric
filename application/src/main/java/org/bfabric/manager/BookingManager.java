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

package org.bfabric.manager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.BillingInfo;
import org.bfabric.entity.Booker;
import org.bfabric.entity.Booking;
import org.bfabric.entity.BookingType;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.CostCentre;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.BookingService;
import org.omnifaces.cdi.Param;
import org.primefaces.component.datatable.DataTable;

@MeasureCalls
@Named
@ViewScoped
public class BookingManager extends AbstractContainerDependentEntityManager<Booking> {

    private static final Logger logger = Logger.getLogger(BookingManager.class.getName());

    private static final long serialVersionUID = 1;

    // cached list of all de-selected charges
    private final List<Charge> deselectedCharges = new ArrayList<>();

    // cached list of all selected charges
    private final Set<Charge> selectedCharges = new HashSet<>();

    private final Set<CostCentre> candidateCostCentres = new HashSet<>();

    @Param
    protected Long chargeId;

    @Inject
    private BookingService bookingService;

    // cached list of all candidate charges
    private List<Charge> candidateCharges = new ArrayList<>();

    public BookingManager() {
        super(Booking.class);
    }

    public void addSelectedCharges() {
        getSelectedCharges().clear();
        for (final Charge charge : getCandidateCharges()) {
            if (charge.isChecked()) {
                getSelectedCharges().add(charge);
            }
        }

        // Set the selected charges as new charges
        getBooking().setCharges(getSelectedCharges());

        // Calculate service charges
        getBooking().calculateCharges();

        for (final Charge charge : getBooking().getCharges()) {
            if (charge.getService().getServiceCode() != null) {
                getCandidateCostCentres().addAll(charge.getService().getServiceCode().getCostCentres());
            }
        }
        Set<CostCentre> selectedCostCentres = new HashSet<>(getBooking().getBooker().getCostCentresEnabled());
        selectedCostCentres.retainAll(getCandidateCostCentres());
        if (!selectedCostCentres.isEmpty()) {
            getBooking().setCostCentre(selectedCostCentres.stream().findFirst().get());
        }
        if (!getBooking().isManaged() && getBooking().getCostCentre() != null) {
            getBooking().setBookingNr(bookingService.generateNewBookingNumber(getBooking().getBookingType(), getBooking().getCostCentre()));
        }
    }

    public void bookerChanged(ValueChangeEvent event) {
        getBooking().setBooker((Booker) event.getNewValue());
        getBooking().setBookingType(getBooking().getBooker().getBookingTypes().stream().findFirst().orElse(null));
        getBooking().setCostCentre(getBooking().getBooker().getCostCentres().stream().findFirst().orElse(null));
        setBookingNr();
    }

    public void bookingTypeChanged(ValueChangeEvent event) {
        getBooking().setBookingType((BookingType) event.getNewValue());
        setBookingNr();
    }

    public String callSoapETH(String soapString) {
        String endPoint = "https://sap-wdw.ethz.ch:12443/XISOAPAdapter/MessageServlet?senderParty=&senderService=PO&receiverParty=&receiverService=&interface=BillingDocOutb&interfaceNamespace=http://ethz.ch/po/fi/billing/v2";
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            System.out.println("Created SOAP Connection");
            InputStream is = new ByteArrayInputStream(soapString.getBytes(StandardCharsets.UTF_8));
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
            String soapETHAuthorization = "...";
            soapMessage.getMimeHeaders().addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(soapETHAuthorization.getBytes(StandardCharsets.UTF_8)));
            System.out.println("Calling end " + endPoint + " with " + soapMessage.getSOAPBody().getTextContent());
            SOAPMessage response = soapConnection.call(soapMessage, endPoint);
            System.out.println("SOAP Connection SUCCESSFUL " + response.getSOAPBody().getTextContent());
            getFacesMessagesManager().bufferWarningClear(Messages.get("transferETHSuccessful"));
        } catch (Exception e) {
            System.out.println("SOAP Connection FAILED: ");
            e.printStackTrace();
            getFacesMessagesManager().bufferWarningClear(Messages.get("transferETHFailed"));
        }
        return getShowScreenRedirectURL();
    }

    public void checkAllCandidateCharges(boolean check) {
        List<Charge> charges = getCandidateCharges();
        final DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("select-chargestableform:select-chargestable");
        if (dataTable != null && dataTable.getFilteredValue() != null && !dataTable.getFilteredValue().isEmpty()) {
            charges = (List<Charge>) dataTable.getFilteredValue();
        }
        for (final Charge charge : charges) {
            charge.setChecked(check);
        }
    }

    public void costCentreChanged(ValueChangeEvent event) {
        getBooking().setCostCentre((CostCentre) event.getNewValue());
        setBookingNr();
    }

    @Override
    protected Booking createInstance() {
        Booking booking = super.createInstance();

        if (getContainerId() != null) {
            booking.setContainer(entityService.find(Container.class, getContainerId()));
        } else if (getContextContainer() != null && getContextContainer().isExtensible()) {
            booking.setContainer(entityService.find(Container.class, getContextContainer().getId()));
        }

        if (booking.getContainer() != null) {
            try {
                booking.setBillingInfo(booking.getContainer().getBillingInfo().clone());
                if (booking.getContainer().getInstitute() != null) {
                    booking.setInstituteHierarchy(booking.getContainer().getInstitute());
                } else {
                    booking.setDivisionHierarchy(booking.getContainer().getDivision());
                }
            } catch (Exception e) {
                booking.setBillingInfo(new BillingInfo());
            }
        }

        if (chargeId != null) {
            Charge charge = entityService.find(Charge.class, chargeId);
            if (charge != null) {
                booking.getCharges().add(charge);
                charge.setChecked(true);
                getCandidateCharges().add(charge);
                addSelectedCharges();
            }
        }

        if (booking.getBooker() != null) {
            booking.setAccount(new ArrayList<>(booking.getBooker().getAccounts()).get(0));
            booking.setCostCentre(new ArrayList<>(booking.getBooker().getCostCentresEnabled()).get(0));
            booking.setFinancialCenter(new ArrayList<>(booking.getBooker().getFinancialCenters()).get(0));
        }
        booking.setBookingIssuer(bookingService.getDefaultBookingIssuer());
        booking.setCurrency(bookingService.getDefaultCurrency());
        booking.setBookingNr(bookingService.generateNewBookingNumber(booking.getBookingType(), booking.getCostCentre()));
        booking.setBookingDate(LocalDate.now());
        booking.calculateCharges();

        return booking;
    }

    public void executionPeriodEndDateChanged() {
        final LocalDate startDate = getBooking().getExecutionPeriodStartDate();
        final LocalDate endDate = getBooking().getExecutionPeriodEndDate();
        if (startDate.isAfter(endDate)) {
            getBooking().setExecutionPeriodStartDate(endDate);
            getFacesMessagesManager().validationError("edit:executionPeriodStartDate", Messages.get("adapted"));
            getFacesMessagesManager().printWarn(Messages.get("noteAdaptedStartDate"));
        }
    }

    public void executionPeriodStartDateChanged() {
        final LocalDate startDate = getBooking().getExecutionPeriodStartDate();
        final LocalDate endDate = getBooking().getExecutionPeriodEndDate();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            getBooking().setExecutionPeriodEndDate(startDate);
            getFacesMessagesManager().validationError("edit:executionPeriodEndDate", Messages.get("adapted"));
            getFacesMessagesManager().printWarn(Messages.get("noteAdaptedEndDate"));
        }
    }

    @Produces
    @Named("booking")
    public Booking getBooking() {
        return getInstance();
    }

    @CachedMethodResult
    public List<Charge> getCandidateCharges() {
        return candidateCharges;
    }

    public Set<CostCentre> getCandidateCostCentres() {
        return candidateCostCentres;
    }

    public List<Charge> getDeselectedCharges() {
        return deselectedCharges;
    }

    public Set<Charge> getSelectedCharges() {
        return selectedCharges;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getBooking() != null && getBooking().isManaged() && getBooking().getBillingInfo() != null) {
            getBooking().getBillingInfo().setOldDebitorNumber(getBooking().getBillingInfo().getDebitorNumber());
        }
    }

    public void initCandidateCharges() {
        // Non-booked charges and initial charges (in case of editing of booking) are candidates for the new booking.
        getCandidateCharges().clear();
        getCandidateCharges().addAll(getBooking().getInitialCharges());
        getCandidateCharges().addAll(getBooking().getContainer().getNonBookedCharges());
        // In case of editing, candidates must include the current charges of the booking.
        getCandidateCharges().removeAll(getBooking().getCharges());
        getCandidateCostCentres().clear();
        for (final Charge charge : getBooking().getCharges()) {
            charge.setChecked(true);
            getCandidateCharges().add(charge);
        }
        getCandidateCharges().sort(Collections.reverseOrder());
    }

    @Override
    public Booking loadInstance() {
        Booking booking = super.loadInstance();
        if (booking != null) {
            booking.setAffiliationValues();
            // Initialize the initial charges list.
            booking.getInitialCharges().addAll(booking.getCharges());
        }
        return booking;
    }

    public String removeCascade() {
        bookingService.removeCascade(getBooking());
        return getRedirectURLAfterRemove();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = bookingService.isValid(getBooking());
        if (validationErrorMsg.isEmpty()) {
            try {
                // Cache whether the entity is created or not.
                setCreated(!isManaged());
                bookingService.saveBooking(getBooking());
                return postSave(true, isCreated());
            } catch (final Exception e) {
                getFacesMessagesManager().printError(e.getLocalizedMessage());
                logger.severe("Save Booking throws " + e);
            }
        } else {
            handleValidationErrors(validationErrorMsg);
        }
        return null;
    }

    public void setBookingNr() {
        getBooking().setBookingNr(bookingService.generateNewBookingNumber(getBooking().getBookingType(), getBooking().getCostCentre()));
        setBookingNrInputField();
    }

    public void setBookingNrInputField() {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final UIInput input = (UIInput) facesContext.getViewRoot().findComponent(Constants.EDIT + ":bookingNr");
        input.setSubmittedValue(null);
        input.setValue(getBooking().getBookingNr());
        input.setValid(true);
    }

    public void setCandidateCharges(List<Charge> candidateCharges) {
        this.candidateCharges = candidateCharges;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    public void taxTypeChanged(Charge charge) {
        charge.setTaxRate(charge.getTaxType().getTax());
        charge.setPrice();
        getBooking().calculateCharges();
    }

    public String testReadETH() {
        String soapString = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bil=\"http://ethz.ch/po/fi/billing/v2\">" +
            "   <soapenv:Header/>" +
            "   <soapenv:Body>" +
            "      <bil:BillingDocReadReq>" +
            "         <dataset>" +
            "            <belnr>1500006096</belnr>" +
            "            <gjahr>2023</gjahr>" +
            "         </dataset>" +
            "      </bil:BillingDocReadReq>" +
            "   </soapenv:Body>" +
            "</soapenv:Envelope>";
        return callSoapETH(soapString);
    }

    public String testWriteETH() {
        String soapString = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bil=\"http://ethz.ch/po/fi/billing/v2\">" +
            "   <soapenv:Header/><soapenv:Body><bil:BillingDocCreateReq><dataset><budat>20230901</budat><bldat>20230901</bldat><material>FG2300-GHT</material><quantitiy>1.000</quantitiy><preis>1.07</preis><betrag>1.07</betrag><pernr/><objnr>26120</objnr><objekt/><hkont>42070001</hkont><dmbtr>1.07</dmbtr><waers>CHF</waers><creator>NOTTERR</creator><email>notterr@ethz.ch</email></dataset><dataset><budat>20230901</budat><bldat>20230901</bldat><material>FG2600-GHT</material><quantitiy>2.000</quantitiy><preis>1.01</preis><betrag>1.01</betrag><pernr/><objnr>1-001898-000</objnr><objekt>1-001898-000</objekt><hkont>42070001</hkont><dmbtr>1.07</dmbtr><waers>CHF</waers><creator>NOTTERR</creator><email>notterr@ethz.ch</email></dataset></bil:BillingDocCreateReq>" +
            "   </soapenv:Body></soapenv:Envelope>";
        return callSoapETH(soapString);
    }
}