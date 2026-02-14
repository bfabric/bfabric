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
import java.util.HashSet;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.enums.RoleEnum;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class OfferedCharge extends AbstractCharge {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "offeredCharge")
    @OrderBy("id desc")
    private Set<Charge> charges = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerid")
    @XmlIDREF
    private Offer offer;

    public OfferedCharge() {
        super();
    }

    public OfferedCharge(Offer offer, Charge charge) {
        super();
        setOffer(offer);
        setOrganizationType(offer.getOrganizationType());
        setService(charge.getService());
        setCharger(charge.getCharger());
        setTotal(charge.getTotal());
        setNotAccounted(charge.getNotAccounted());
        setTaxType(charge.getTaxType());
        setTaxRate(charge.getTaxType().getTax());
        setDiscount(charge.getDiscount());
        setPrice();
    }

    @Override
    public OfferedCharge clone() throws CloneNotSupportedException {
        OfferedCharge clone = (OfferedCharge) super.clone();
        clone.setCharges(new HashSet<>());
        return clone;
    }

    public void discountChanged(ValueChangeEvent event) {
        setDiscount((BigDecimal) event.getNewValue());
        if (isManaged()) {
            setPrice();
        }
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getOffer() != null) {
            addEntityInfoItem(summary, "offer", getOffer().getId());
        }
        if (getAccounted() != null) {
            addEntityInfoItem(summary, "accounted", getAccounted());
        }
        if (getDiscount() != null) {
            addEntityInfoItem(summary, "discount", getDiscount());
        }
        if (getOffer().getDiscount() != null) {
            addEntityInfoItem(summary, "offerDiscount", getOffer().getDiscount());
        }
        if (getBasicPrice() != null) {
            addEntityInfoItem(summary, "basicPrice", getBasicPrice());
        }
        if (getAdditionalPrice() != null) {
            addEntityInfoItem(summary, "additionalPrice", getAdditionalPrice());
        }
        if (getTaxType() != null) {
            addEntityInfoItem(summary, "taxType", getTaxType().getName());
        }
        if (getCharger() != null) {
            addEntityInfoItem(summary, "charger", getCharger().getName());
        }
        if (StringHelper.isNotEmpty(getNotes())) {
            addEntityInfoItem(summary, "notes", getNotes());
        }
        return summary.toString();
    }

    public Offer getOffer() {
        return offer;
    }

    public BigDecimal getPriceWithoutOfferDiscount() {
        // Compute the discount for this charge
        if (isDiscounted()) {
            return BigDecimal.valueOf(NumberUtils.roundToDecimals(getPrice().doubleValue() - getPrice().doubleValue() * getDiscount().doubleValue() / 100.0));
        }
        return getPrice();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isItemDiscounted() {
        return isDiscounted() || getOffer().isDiscounted();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER);
    }

    public boolean isServiceCodeChanged() {
        return isUpdatable() && getService() != null && StringHelper.isNotEmpty(getService().getCode()) && (StringHelper.isEmpty(getServiceCodeName()) || !getService().getCode().equalsIgnoreCase(
            getServiceCodeName()));
    }

    public boolean isServiceNameChanged() {
        return isUpdatable() && getService() != null && StringHelper.isNotEmpty(getService().getName()) && (StringHelper.isEmpty(getServiceName()) || !getService().getName().equalsIgnoreCase(
            getServiceName()));
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable() && !getOffer().isLocked() && getCharges().isEmpty();
    }

    public void resetOffer(Offer offer) {
        setOffer(offer);
        setOrganizationType(offer.getOrganizationType());
        setPrice();
    }

    public void setCharges(Set<Charge> charges) {
        this.charges = charges;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    @Override
    public void setPrice() {
        if (getService() != null && getOffer() != null) {
            // Save basic price information for this offered charge
            ServiceOrganizationTypePrice serviceOrganizationTypePrice = getService().getServiceOrganizationTypePrices(getOffer().getOrganizationType());
            if (getOffer().getEuGrant() != null && getOffer().getEuGrant()) {
                setBasicPrice(serviceOrganizationTypePrice.getEuGrantPrice());
            } else {
                setBasicPrice(serviceOrganizationTypePrice.getBasicPrice());
            }
            if (getAccountedComputed().doubleValue() > 0) {
                setPrice(getAccountedComputed().multiply(getBasicPrice()));

                // Compute the discount for this charge.
                if (isDiscounted()) {
                    setDiscountedPrice(BigDecimal.valueOf(getPrice().doubleValue() - (getPrice().doubleValue() * getDiscount().doubleValue() / 100.0)));
                } else {
                    setDiscountedPrice(getPrice());
                }

                // Apply discount for the offer discounted prices.
                if (getOffer().isDiscounted()) {
                    setDiscountedPrice(BigDecimal.valueOf(getDiscountedPrice().doubleValue() - (getDiscountedPrice().doubleValue() * getOffer().getDiscount().doubleValue() / 100.0)));
                }
            } else {
                setPrice(BigDecimal.ZERO);
                setDiscountedPrice(BigDecimal.ZERO);
            }
        }
    }

    public void taxTypeChanged(ValueChangeEvent event) {
        setTaxType((TaxType) event.getNewValue());
        setTaxRate(getTaxType().getTax());
        if (isManaged()) {
            setPrice();
        }
    }

    public void totalChanged(ValueChangeEvent event) {
        setTotal((BigDecimal) event.getNewValue());
        if (isManaged()) {
            setPrice();
        }
    }
}