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

import org.bfabric.entity.Booking;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceCode;
import org.bfabric.entity.TaxType;
import org.bfabric.entity.User;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.TaxTypeService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCharge;

public class MFCharge extends AbstractMF {

    private final Charge charge;

    private final XMLRequestParameterSaveCharge xmlRequestSaveCharge;

    public MFCharge(Charge charge, XMLRequestParameterSaveCharge xmlChargeRequestSave) {
        this.charge = charge;
        this.xmlRequestSaveCharge = xmlChargeRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getCharge().setUseCurrentServicePricesForOfferedCharge(getUseCurrentServicePricesForOfferedCharge());
        getCharge().setTotal(getTotal());
        getCharge().setNotAccounted(getNotAccounted());
        getCharge().setAdditionalPrice(getAdditionalPrice());
        getCharge().setBasicPrice(getBasicPrice());
        getCharge().setDiscount(getDiscount());
        getCharge().setDiscountedPrice(getDiscountedPrice());
        getCharge().setDate(getDate());
        getCharge().setService(getService());
        getCharge().setServiceCode(getServiceCode());
        getCharge().setBooking(getBooking());
        getCharge().setCharger(getCharger());
        getCharge().setContainer(getContainer());
        getCharge().setOrganizationType(getOrganizationType());
        getCharge().setTaxType(getTaxType());
        getCharge().setTaxRate(getTaxRate());
        getCharge().setDescription(getDescription());
        getCharge().setNotes(getNotes());
        getCharge().setInstrumentReservations(getInstrumentReservations());
        getCharge().setOrderItems(getOrderItems());
        getCharge().setSamples(getSamples());
        getCharge().setBillable(getBillable());
        getCharge().setPrice();
        getCharge().setPrice(getPrice());
    }

    private BigDecimal getAdditionalPrice() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getAdditionalprice() != null) {
            return MFHelper.bigDecimalValueOf("additionalprice", getXmlRequestSaveCharge().getAdditionalprice());
        }
        return charge.getAdditionalPrice();
    }

    private BigDecimal getBasicPrice() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getBasicprice() != null) {
            return MFHelper.bigDecimalValueOf("basicprice", getXmlRequestSaveCharge().getBasicprice());
        }
        return charge.getBasicPrice();
    }

    private Boolean getBillable() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getBillable() != null) {
            return MFHelper.booleanValueOf("billable", getXmlRequestSaveCharge().getBillable());
        }
        return charge.isBillable();
    }

    public Booking getBooking() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getBookingid() != null) {
            return (Booking) fetch(Booking.class, MFHelper.positiveLongValueOf("bookingid", getXmlRequestSaveCharge().getBookingid()));
        }
        return getCharge().getBooking();
    }

    public Charge getCharge() {
        return charge;
    }

    public User getCharger() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getChargerid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("chargerid", getXmlRequestSaveCharge().getChargerid()));
        }
        return getCharge().getCharger();
    }

    public Container getContainer() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getContainerid() != null) {
            return (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveCharge().getContainerid()));
        }
        return getCharge().getContainer();
    }

    public LocalDate getDate() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getDate() != null) {
            return MFHelper.dateValueOf("date", getXmlRequestSaveCharge().getDate());
        }
        return getCharge().getDate();
    }

    public String getDescription() {
        if (getXmlRequestSaveCharge().getDescription() != null) {
            return getXmlRequestSaveCharge().getDescription();
        }
        return getCharge().getDescription();
    }

    private BigDecimal getDiscount() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getDiscount() != null) {
            return MFHelper.bigDecimalValueOf("discount", getXmlRequestSaveCharge().getDiscount());
        }
        return charge.getDiscount();
    }

    private BigDecimal getDiscountedPrice() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getDiscountedprice() != null) {
            return MFHelper.bigDecimalValueOf("discountedprice", getXmlRequestSaveCharge().getDiscountedprice());
        }
        return charge.getDiscountedPrice();
    }

    public Set<InstrumentReservation> getInstrumentReservations() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getInstrumentreservationid() != null) {
            Set<InstrumentReservation> instrumentReservations = new HashSet<>();
            for (String instrumentReservationId : getXmlRequestSaveCharge().getInstrumentreservationid()) {
                if (!instrumentReservationId.isEmpty()) {
                    instrumentReservations.add((InstrumentReservation) fetch(InstrumentReservation.class, MFHelper.positiveLongValueOf("instrumentreservationid", instrumentReservationId)));
                }
            }
            return instrumentReservations;
        }
        return charge.getInstrumentReservations();
    }

    public BigDecimal getNotAccounted() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getNotaccounted() != null) {
            return MFHelper.bigDecimalValueOf("notaccounted", getXmlRequestSaveCharge().getNotaccounted());
        }
        return charge.getNotAccounted();
    }

    public String getNotes() {
        if (getXmlRequestSaveCharge().getNotes() != null) {
            return getXmlRequestSaveCharge().getNotes();
        }
        return getCharge().getNotes();
    }

    public OfferedCharge getOfferedCharge() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getOfferedchargeid() != null) {
            return (OfferedCharge) fetch(OfferedCharge.class, MFHelper.positiveLongValueOf("offeredchargeid", getXmlRequestSaveCharge().getOfferedchargeid()));
        }
        return getCharge().getOfferedCharge();
    }

    public Set<OrderItem> getOrderItems() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getOrderitemid() != null) {
            Set<OrderItem> orderItems = new HashSet<>();
            for (String orderItemId : getXmlRequestSaveCharge().getOrderitemid()) {
                if (!orderItemId.isEmpty()) {
                    orderItems.add((OrderItem) fetch(OrderItem.class, MFHelper.positiveLongValueOf("orderitemid", orderItemId)));
                }
            }
            return orderItems;
        }
        return charge.getOrderItems();
    }

    public OrganizationType getOrganizationType() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getOrganizationtypeid() != null) {
            return (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("organizationtypeid", getXmlRequestSaveCharge().getOrganizationtypeid()));
        }
        return getCharge().getOrganizationType();
    }

    private BigDecimal getPrice() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getPrice() != null) {
            return MFHelper.bigDecimalValueOf("price", getXmlRequestSaveCharge().getPrice());
        }
        return charge.getPrice();
    }

    public Set<Sample> getSamples() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getSampleid() != null) {
            Set<Sample> samples = new HashSet<>();
            for (String sampleId : getXmlRequestSaveCharge().getSampleid()) {
                if (!sampleId.isEmpty()) {
                    samples.add((Sample) fetch(Sample.class, MFHelper.positiveLongValueOf("sampleid", sampleId)));
                }
            }
            return samples;
        }
        return charge.getSamples();
    }

    public Service getService() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getServiceid() != null) {
            return (Service) fetch(Service.class, MFHelper.positiveLongValueOf("serviceid", getXmlRequestSaveCharge().getServiceid()));
        }
        return getCharge().getService();
    }

    public ServiceCode getServiceCode() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getServicecodeid() != null) {
            return (ServiceCode) fetch(ServiceCode.class, MFHelper.positiveLongValueOf("servicecodeid", getXmlRequestSaveCharge().getServicecodeid()));
        }
        return getCharge().getServiceCode();
    }

    private BigDecimal getTaxRate() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getTaxrate() != null) {
            return MFHelper.bigDecimalValueOf("taxrate", getXmlRequestSaveCharge().getTaxrate());
        }
        if (getCharge().getTaxRate() == null && getCharge().getTaxType() != null) {
            return getCharge().getTaxType().getTax();
        }
        return charge.getTaxRate();
    }

    public TaxType getTaxType() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getTaxtypeid() != null) {
            return (TaxType) fetch(TaxType.class, MFHelper.positiveLongValueOf("taxtypeid", getXmlRequestSaveCharge().getTaxtypeid()));
        }
        if (getCharge().getTaxType() == null) {
            return CDI.current().select(TaxTypeService.class).get().getDefaultTaxType();
        }
        return getCharge().getTaxType();
    }

    private BigDecimal getTotal() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getTotal() != null) {
            return MFHelper.bigDecimalValueOf("total", getXmlRequestSaveCharge().getTotal());
        }
        return charge.getTotal();
    }

    private Boolean getUseCurrentServicePricesForOfferedCharge() throws InvalidDataException {
        if (getXmlRequestSaveCharge().getUsecurrentservicepricesforofferedcharge() != null) {
            return MFHelper.booleanValueOf("usecurrentservicepricesforofferedcharge", getXmlRequestSaveCharge().getUsecurrentservicepricesforofferedcharge());
        }
        return charge.isUseCurrentServicePricesForOfferedCharge();
    }

    public XMLRequestParameterSaveCharge getXmlRequestSaveCharge() {
        return xmlRequestSaveCharge;
    }
}