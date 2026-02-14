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
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ContainerDependent;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.DurationHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.primefaces.event.UnselectEvent;

@Entity
@XmlRootElement
@NamedQuery(name = "Charge.findByCreatedByOrderById", query = "SELECT a FROM Charge a WHERE a.createdBy = :createdBy ORDER BY a.created DESC, a.id DESC")
@NamedQuery(name = "Charge.findByBookingIdOrderByServiceAndDiscount", query = "SELECT a FROM Charge a LEFT JOIN ServiceCode sc ON (a.serviceCode = sc) WHERE a.booking.id = :bookingId ORDER BY sc.name, a.service.name, a.discount")
@NamedQuery(name = "Charge.findByBookingIdOrderByServiceAndTaxRate", query = "SELECT a FROM Charge a WHERE a.booking.id = :bookingId ORDER BY a.service.name, a.taxRate")
public class Charge extends AbstractCharge implements ContainerDependent {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingid")
    @XmlIDREF
    private Booking booking;

    @Transient
    private ChronoUnit chargeTimeUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "containerid")
    @NotNull
    @XmlIDREF
    private Container container;

    @Transient
    private boolean grouped;

    @ManyToMany
    @JoinTable(name = "chargeinstrumentreservation", joinColumns = @JoinColumn(name = "chargeid"), inverseJoinColumns = @JoinColumn(name = "instrumentreservationid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReservation> instrumentReservations = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offeredchargeid")
    @XmlIDREF
    private OfferedCharge offeredCharge;

    private Long oldServiceChargeId;

    @ManyToMany
    @JoinTable(name = "chargeorderitem", joinColumns = @JoinColumn(name = "chargeid"), inverseJoinColumns = @JoinColumn(name = "orderitemid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlElement(name = "orderitem")
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "chargesample", joinColumns = @JoinColumn(name = "chargeid"), inverseJoinColumns = @JoinColumn(name = "sampleid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "sample")
    private Set<Sample> samples = new HashSet<>();

    // True, if the current service prices will be used for offered charge.
    @XmlElement
    private boolean useCurrentServicePricesForOfferedCharge = false;

    public Charge() {
        super();
    }

    public Charge(OfferedCharge offeredCharge, Container container, boolean isUseCurrentServicePrices) {
        super();
        setContainer(container);
        // Associate the charge with all order items of the order.
        if (container != null && !container.isContainerProject() && !container.getOrderItems().isEmpty()) {
            setOrderItems(new HashSet<>(container.getOrderItems()));
        }
        setOfferedCharge(offeredCharge);
        setOrganizationType(offeredCharge.getOrganizationType());
        setUseCurrentServicePricesForOfferedCharge(isUseCurrentServicePrices);
        setService(offeredCharge.getService());
        setCharger(offeredCharge.getCharger());
        setTotal(offeredCharge.getTotal());
        setNotAccounted(offeredCharge.getNotAccounted());
        setTaxType(offeredCharge.getTaxType());
        setTaxRate(offeredCharge.getTaxType().getTax());
        setDiscount(offeredCharge.getDiscount());
        setPrice();
        setBillable(true);
    }

    public void addChargeToGroupedCharge(Charge charge) {
        if (charge != null) {
            setNotAccounted(getNotAccounted().add(charge.getNotAccounted()));
            setTotal(getTotal().add(charge.getTotal()));
            setPrice(getPrice().add(charge.getPrice()));
            setDiscountedPrice(getDiscountedPrice().add(charge.getDiscountedPrice()));
        }
    }

    @Override
    public Charge clone() throws CloneNotSupportedException {
        Charge clone = (Charge) super.clone();
        clone.booking = null;
        return clone;
    }

    public BigDecimal computeTotal(Collection<InstrumentReservation> instrumentReservations) {
        double totalDuration = 0.0;
        for (InstrumentReservation reservation : instrumentReservations) {
            totalDuration += DurationHelper.convertMinutesToChronoUnit(reservation).doubleValue();
        }
        return BigDecimal.valueOf(totalDuration).setScale(2, RoundingMode.HALF_EVEN);
    }

    public void discountChanged(ValueChangeEvent event) {
        setDiscount((BigDecimal) event.getNewValue());
        if (isManaged()) {
            setPrice();
        }
    }

    public String getBillableAsText() {
        return isBillable() ? "yes" : "no";
    }

    public Booking getBooking() {
        return booking;
    }

    public ChronoUnit getChargeTimeUnit() {
        return chargeTimeUnit;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CHARGEMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getContainer() != null) {
            addEntityInfoItem(summary, "container", getContainer().getId());
        }
        if (getAccounted() != null) {
            addEntityInfoItem(summary, "accounted", getAccounted());
        }
        if (getBasicPrice() != null) {
            addEntityInfoItem(summary, "basicPrice", getBasicPrice());
        }
        if (getAdditionalPrice() != null) {
            addEntityInfoItem(summary, "additionalPrice", getAdditionalPrice());
        }
        if (getDiscount() != null) {
            addEntityInfoItem(summary, "discount", getDiscount());
        }
        if (getOrderDiscount() != null) {
            addEntityInfoItem(summary, "orderDiscount", getOrderDiscount());
        }
        if (getDate() != null) {
            addEntityInfoItem(summary, "date", getDate());
        }
        if (StringHelper.isNotEmpty(getNotes())) {
            addEntityInfoItem(summary, "notes", getNotes());
        }
        if (getInstrumentReservations() != null) {
            addEntityInfoItem(summary, "instrumentReservations", CollectionHelper.print(getInstrumentReservations(), "getDisplayName"));
        }
        if (StringHelper.isNotEmpty(getBillableAsText())) {
            addEntityInfoItem(summary, "billable", getBillableAsText());
        }
        addEntityInfoItem(summary, "prePayment", isPrePayment());
        if (getTaxType() != null) {
            addEntityInfoItem(summary, "taxType", getTaxType().getName());
        }
        if (getBooking() != null) {
            addEntityInfoItem(summary, "booking", getBooking());
        }
        if (getContainer().getInstitute() != null) {
            addEntityInfoItem(summary, "institute", getContainer().getInstitute().getName());
            addEntityInfoItem(summary, "department", getContainer().getInstitute().getDepartmentName());
            addEntityInfoItem(summary, "organization", getContainer().getInstitute().getOrganizationName());
        }
        if (getCharger() != null) {
            addEntityInfoItem(summary, "charger", getCharger().getFullName());
        }
        return summary.toString();
    }

    public Set<InstrumentReservation> getInstrumentReservations() {
        return instrumentReservations;
    }

    public List<InstrumentReservation> getInstrumentReservationsAsList() {
        return CollectionHelper.asList(getInstrumentReservations());
    }

    public BigDecimal getItemDiscount() {
        // Compute the item discount for this charge
        if (isDiscounted()) {
            return BigDecimal.valueOf(NumberUtils.roundToDecimals(getPrice().doubleValue() * getDiscount().doubleValue() / 100.0));
        }
        return BigDecimal.ZERO;
    }

    public String getName() {
        StringBuilder nameBuilder = new StringBuilder(super.getName());
        if (getContainer() != null) {
            nameBuilder.append(", ").append(Messages.get(getContainer().getClassLabelLowerCase())).append(" ").append(getContainer().getId());
        }
        return nameBuilder.toString();
    }

    public OfferedCharge getOfferedCharge() {
        return offeredCharge;
    }

    public String getOldId() {
        return getOldServiceChargeId() != null ? StringHelper.embraceParentheses(Messages.get("oldServiceChargeId") + " " + getOldServiceChargeId()) : null;
    }

    public Long getOldServiceChargeId() {
        return oldServiceChargeId;
    }

    public BigDecimal getOrderDiscount() {
        // Compute the order discount for this charge
        if (getContainer() != null && getContainer().isDiscounted()) {
            return BigDecimal.valueOf(NumberUtils.roundToDecimals(getPriceWithItemDiscount().doubleValue() * getContainer().getDiscount().doubleValue() / 100.0));
        }
        return BigDecimal.ZERO;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public BigDecimal getPriceWithItemDiscount() {
        // Compute the item discount for this charge
        if (isDiscounted()) {
            return BigDecimal.valueOf(NumberUtils.roundToDecimals(getPrice().doubleValue() - NumberUtils.roundToDecimals(getPrice().doubleValue() * getDiscount().doubleValue() / 100.0)));
        }
        return getPrice();
    }

    public BigDecimal getPriceWithOrderDiscount() {
        BigDecimal priceWithItemDiscount = getPriceWithItemDiscount();
        // Compute the order discount for this charge
        if (getContainer() != null && getContainer().isDiscounted()) {
            return BigDecimal.valueOf(NumberUtils
                .roundToDecimals(priceWithItemDiscount.doubleValue() - NumberUtils.roundToDecimals(priceWithItemDiscount.doubleValue() * getContainer().getDiscount().doubleValue() / 100.0)));
        }
        return priceWithItemDiscount;
    }

    public String getRowStyleClass() {
        if (isBooked()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (!isBillable()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        return Constants.BACKGROUND_COLOR_RED;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntity(getContainer());
        IndexHelper.indexEntities(getInstrumentReservations());
        IndexHelper.indexEntity(getBooking());
        IndexHelper.indexEntities(getSamples());
    }

    public void instrumentReservationChanged(ValueChangeEvent event) {
        List<InstrumentReservation> selectedInstrumentReservations = (List<InstrumentReservation>) event.getNewValue();
        InstrumentReservation last = null;
        if (!selectedInstrumentReservations.isEmpty()) {
            for (InstrumentReservation instrumentReservation : selectedInstrumentReservations) {
                last = instrumentReservation;
            }
            setChargeTimeUnit(last.getInstrumentReservationSetting().getChargeTimeUnit());
        }
        setTotalAndComputePrice(computeTotal(selectedInstrumentReservations));
    }

    public void instrumentReservationUnselected(UnselectEvent<InstrumentReservation> event) {
        if (getInstrumentReservations().isEmpty()) {
            setChargeTimeUnit(null);
        }
    }

    public boolean isBookable() {
        return isBillable() && !isBooked();
    }

    public boolean isBooked() {
        return getBooking() != null;
    }

    public boolean isChargedServiceEditable() {
        return !(getId() > 0 && getOfferedCharge() != null && !getOfferedCharge().getOffer().isExpired());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isEmpty() {
        return getService() == null && getContainer() == null && getAccountedComputed().equals(BigDecimal.ZERO) && getNotAccounted().equals(BigDecimal.ZERO) && StringHelper.isEmpty(getDescription());
    }

    public boolean isGrouped() {
        return grouped;
    }

    public boolean isItemDiscounted() {
        return isDiscounted() || isOrderDiscounted();
    }

    public boolean isOrderDiscounted() {
        return getContainer() != null && getContainer().isDiscounted();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.CHARGEREADER) || getContainer().isMember();
    }

    public boolean isServiceCodeChanged() {
        return !isBooked() && getService() != null && StringHelper.isNotEmpty(getService().getCode()) && (StringHelper.isEmpty(getServiceCodeName()) || !getService().getCode().equalsIgnoreCase(
            getServiceCodeName()));
    }

    public boolean isServiceNameChanged() {
        return !isBooked() && getService() != null && StringHelper.isNotEmpty(getService().getName()) && (StringHelper.isEmpty(getServiceName()) || !getService().getName().equalsIgnoreCase(
            getServiceName()));
    }

    @Override
    public boolean isUpdatable() {
        // Can be updated if no booking attached to it and the user has the default required role.
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && !isBooked();
    }

    public boolean isUseCurrentServicePricesForOfferedCharge() {
        return useCurrentServicePricesForOfferedCharge;
    }

    public void notAccountedChanged(ValueChangeEvent event) {
        setNotAccountedAndComputePrice((BigDecimal) event.getNewValue());
    }

    public void organizationTypeChanged(ValueChangeEvent event) {
        setOrganizationType((OrganizationType) event.getNewValue());
        setPrice();
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setChargeTimeUnit(ChronoUnit chargeTimeUnit) {
        this.chargeTimeUnit = chargeTimeUnit;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
        if (container != null) {
            setOrganizationType(container.getBillingOrganizationType());
        }
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public void setInstrumentReservations(Set<InstrumentReservation> instrumentReservations) {
        this.instrumentReservations = instrumentReservations;
    }

    public void setInstrumentReservationsAsList(List<InstrumentReservation> instrumentReservations) {
        this.instrumentReservations = (Set<InstrumentReservation>) CollectionHelper.asSet(instrumentReservations);
    }

    @Override
    public void setNotAccounted(BigDecimal notAccounted) {
        this.notAccounted = notAccounted;
    }

    public void setNotAccountedAndComputePrice(BigDecimal notAccounted) {
        this.notAccounted = notAccounted;

        // Recompute the price.
        this.setPrice();
    }

    public void setOfferedCharge(OfferedCharge offeredCharge) {
        this.offeredCharge = offeredCharge;
    }

    public void setOldServiceChargeId(Long oldServiceChargeId) {
        this.oldServiceChargeId = oldServiceChargeId;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public void setPrice() {
        if (getService() != null && getContainer() != null) {
            // Check if the charge is copied from an offer and if the offered prices are to be used.
            if (getOfferedCharge() != null && !isUseCurrentServicePricesForOfferedCharge()) {
                // Use the prices of the offer.
                setBasicPrice(getOfferedCharge().getBasicPrice());
                setAdditionalPrice(getOfferedCharge().getAdditionalPrice());
            } else {
                // Use the current prices.
                ServiceOrganizationTypePrice serviceOrganizationTypePrice;
                if (getOrganizationType() != null) {
                    serviceOrganizationTypePrice = getService().getServiceOrganizationTypePrices(getOrganizationType());
                } else {
                    serviceOrganizationTypePrice = getService().getServiceOrganizationTypePrices(getContainer().getBillingOrganizationType());
                }
                if (serviceOrganizationTypePrice != null) {
                    if (getContainer() != null && getContainer().getEuGrant() != null && getContainer().getEuGrant() || getContainer() == null && getContainer() != null && getContainer()
                        .getEuGrant() != null && getContainer().getEuGrant()) {
                        setBasicPrice(serviceOrganizationTypePrice.getEuGrantPrice());
                    } else {
                        setBasicPrice(serviceOrganizationTypePrice.getBasicPrice());
                    }
                } else {
                    logger.severe("ServiceOrganizationTypePrice is null for " + getService() + " and " + getOrganizationType());
                }
            }

            if (getAccountedComputed().doubleValue() > 0) {
                if (getAdditionalPrice().doubleValue() == 0) {
                    setPrice(getAccountedComputed().multiply(getBasicPrice()));
                } else {
                    setPrice(getBasicPrice().add(getAdditionalPrice().multiply(getAccountedComputed().subtract(BigDecimal.ONE))));
                }

                // Initialize the discounted price. Assume no discount.
                setDiscountedPrice(getPrice());

                // Compute the discount for this charge.
                if (isDiscounted()) {
                    setDiscountedPrice(BigDecimal
                        .valueOf(NumberUtils.roundToDecimals(getPrice().doubleValue() - NumberUtils.roundToDecimals(getPrice().doubleValue() * getDiscount().doubleValue() / 100.0))));
                }

                // If this charge belong to an order, apply discount for the order.
                if (getContainer() != null && getContainer().isDiscounted()) {
                    setDiscountedPrice(BigDecimal.valueOf(NumberUtils
                        .roundToDecimals(getDiscountedPrice().doubleValue() - NumberUtils.roundToDecimals(getDiscountedPrice().doubleValue() * getContainer().getDiscount().doubleValue() / 100.0))));
                }
            } else {
                setPrice(BigDecimal.ZERO);
                setDiscountedPrice(BigDecimal.ZERO);
            }
        }
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void setUseCurrentServicePricesForOfferedCharge(boolean useCurrentServicePricesForOfferedCharge) {
        this.useCurrentServicePricesForOfferedCharge = useCurrentServicePricesForOfferedCharge;
    }

    public void taxTypeChanged(ValueChangeEvent event) {
        setTaxType((TaxType) event.getNewValue());
        setTaxRate(getTaxType().getTax());
        setPrice();
    }

    public void totalChanged(ValueChangeEvent event) {
        setTotalAndComputePrice((BigDecimal) event.getNewValue());
    }
}