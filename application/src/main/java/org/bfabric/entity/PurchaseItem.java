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

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ParentDependent;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.util.NumberUtils;

@Entity
@XmlRootElement
public class PurchaseItem extends AbstractEntity implements ShowScreen, ParentDependent {

    private static final long serialVersionUID = 1;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal basicPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumableid")
    @NotNull
    @XmlElement
    private Consumable consumable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseid")
    @NotNull
    @XmlElement
    private Purchase purchase;

    @NotNull
    @DecimalMin("1")
    @DecimalMax("9999")
    @Digits(integer = 4, fraction = 0)
    @XmlElement
    private BigDecimal quantity = BigDecimal.ONE;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal totalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal totalPriceDefaultCurrency;

    public PurchaseItem() {
    }

    public PurchaseItem(Purchase purchase, Consumable consumable, BigDecimal quantity, BigDecimal totalPrice) {
        setPurchase(purchase);
        setConsumable(consumable);
        setQuantity(quantity);
        setTotalPrice(totalPrice);
    }

    public void basicPriceChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null && getQuantity() != null) {
            setTotalPrice(((BigDecimal) event.getNewValue()).multiply(getQuantity()));
        }
    }

    @Override
    public PurchaseItem clone() throws CloneNotSupportedException {
        return (PurchaseItem) super.clone();
    }

    public void consumableChanged(ValueChangeEvent event) {
        Consumable newConsumable = (Consumable) event.getNewValue();
        if (newConsumable.getPrice() != null) {
            setBasicPrice(newConsumable.getPrice());
        }

        if (getBasicPrice() != null && getQuantity() != null) {
            setTotalPrice(getBasicPrice().multiply(getQuantity()));
        }
    }

    public BigDecimal getBasicPrice() {
        return basicPrice;
    }

    public Consumable getConsumable() {
        return consumable;
    }

    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder();
        summary.append(getConsumable().getName());
        summary.append(", Quantity: ");
        summary.append(getQuantity());
        summary.append(", Price: ");
        summary.append(getBasicPrice());
        summary.append(", Total: ");
        summary.append(getTotalPrice());
        summary.append(" ");
        if (getPurchase().getCurrency() != null) {
            summary.append(getPurchase().getCurrency().getCode());
        }
        return summary.toString();
    }

    @Override
    public AbstractBaseEntity getParent() {
        return getPurchase();
    }

    @Override
    public String getParentClassName() {
        return getParent() != null ? getParent().getTrimmedClassName() : null;
    }

    @Override
    public Long getParentId() {
        return getParent().getId();
    }

    @Override
    public String getParentUrlShowScreen() {
        return getParent() != null ? getParent().getUrlShowScreen() : null;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getTotalPriceDefaultCurrency() {
        return totalPriceDefaultCurrency;
    }

    @Override
    public boolean isDeletable() {
        return getPurchase().isDeletable();
    }

    @Override
    public boolean isEditable() {
        return getPurchase() != null && getPurchase().isEditable();
    }

    @Override
    public boolean isReadable() {
        return getPurchase().isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return getPurchase().isUpdatable();
    }

    public void quantityChanged(ValueChangeEvent event) {
        if (getBasicPrice() != null && event.getNewValue() != null) {
            setTotalPrice(getBasicPrice().multiply((BigDecimal) event.getNewValue()));
        }
    }

    public void setBasicPrice(BigDecimal basicPrice) {
        this.basicPrice = NumberUtils.getDecimalScale2(basicPrice);
    }

    public void setConsumable(Consumable consumable) {
        this.consumable = consumable;
    }

    @Override
    public void setParent(AbstractEntity parent) {
        setPurchase((Purchase) parent);
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = NumberUtils.getDecimalScale2(totalPrice);
    }

    public void setTotalPriceDefaultCurrency(BigDecimal totalPriceDefaultCurrency) {
        this.totalPriceDefaultCurrency = NumberUtils.getDecimalScale2(totalPriceDefaultCurrency);
    }
}
