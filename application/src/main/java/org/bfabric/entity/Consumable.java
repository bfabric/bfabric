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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class Consumable extends AbstractEnabledInstrumentReferencingEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Size(max = 256)
    @XmlElement
    private String articleNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyid")
    @XmlIDREF
    private Currency currency;

    @DecimalMin("0")
    @XmlElement
    private Integer inventoryCount;

    @XmlElement
    private boolean inventoryEnabled;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<ConsumableNote> notes = new HashSet<>();

    @OneToMany(mappedBy = "consumable")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal price;

    @OneToMany(mappedBy = "consumable")
    @OrderBy("id asc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<PurchaseItem> purchaseItems = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplierid")
    @XmlIDREF
    private Supplier supplier;

    @Size(max = 256)
    @XmlElement
    private String unit;

    public Consumable() {
        super();
    }

    public void checkIn(int count) {
        if (isInventoryEnabled()) {
            if (getInventoryCount() == null) {
                setInventoryCount(0);
            }
            setInventoryCount(getInventoryCount() + count);
        }
    }

    public boolean checkOut(int count) {
        if (isInventoryEnabled()) {
            if (getInventoryCount() == null) {
                setInventoryCount(0);
            }
            if (getInventoryCount() >= count) {
                setInventoryCount(getInventoryCount() - count);
                return true;
            }
        }
        return false;
    }

    @Override
    public Consumable clone() throws CloneNotSupportedException {
        final Consumable clone = (Consumable) super.clone();
        clone.notes = new HashSet<>();
        clone.orders = new HashSet<>();
        clone.purchaseItems = new HashSet<>();
        return clone;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getSupplierName())) {
            addEntityInfoItem(summary, "supplier", getSupplierName());
        }
        if (StringHelper.isNotEmpty(getArticleNumber())) {
            addEntityInfoItem(summary, "articleNumber", getArticleNumber());
        }
        if (getPrice() != null) {
            addEntityInfoItem(summary, "price", getPrice());
        }
        if (getCurrency() != null) {
            addEntityInfoItem(summary, "currency", getCurrency().getCode());
        }
        if (StringHelper.isNotEmpty(getUnit())) {
            addEntityInfoItem(summary, "unit", getUnit());
        }
        return summary.toString();
    }

    public @DecimalMin("0") Integer getInventoryCount() {
        return inventoryCount;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.CONSUMABLE_NOTE;
    }

    public Set<ConsumableNote> getNotes() {
        return notes;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Set<PurchaseItem> getPurchaseItems() {
        return purchaseItems;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public String getSupplierName() {
        return getSupplier() != null ? getSupplier().getName() : Constants.EMPTY_STRING;
    }

    public String getUnit() {
        return unit;
    }

    public void inventoryEnabledChanged(ValueChangeEvent event) {
        setInventoryEnabled((Boolean) event.getNewValue());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getOrders().isEmpty() && getPurchaseItems().isEmpty();
    }

    public boolean isInventoryEnabled() {
        return inventoryEnabled;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || hasCurrentUserRoleEnum(RoleEnum.USER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void priceChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setPrice(new BigDecimal(event.getNewValue().toString()));
        } else {
            setPrice(null);
            setCurrency(null);
        }
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = StringHelper.format(articleNumber);
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setInventoryCount(@DecimalMin("0") Integer inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public void setInventoryEnabled(boolean inventoryEnabled) {
        this.inventoryEnabled = inventoryEnabled;
        if (!inventoryEnabled) {
            setInventoryCount(0);
        }
    }

    public void setNotes(Set<ConsumableNote> notes) {
        this.notes = notes;
    }

    public void setPrice(BigDecimal price) {
        this.price = NumberUtils.getDecimalScale2(price);
    }

    public void setPurchaseItems(Set<PurchaseItem> purchaseItems) {
        this.purchaseItems = purchaseItems;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public void setSupplierName(String supplier) {
        this.supplier.setName(supplier);
    }

    public void setUnit(String unit) {
        this.unit = StringHelper.format(unit);
    }
}
