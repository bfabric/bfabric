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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class Purchase extends AbstractDescriptionBaseEntity implements ShowScreen, Indexable, TechnologiesDependent {

    private static final long serialVersionUID = 1;

    @Size(max = 512)
    @XmlElement
    private String comment;

    @ManyToMany
    @JoinTable(name = "contractpurchase", joinColumns = @JoinColumn(name = "purchaseid"), inverseJoinColumns = @JoinColumn(name = "contractid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Contract> contracts = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyid")
    @XmlIDREF
    private Currency currency;

    @Transient
    private boolean currencyChanged = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean internal = false;

    @Size(max = 256)
    @XmlElement
    private String invoiceNumber;

    @PastOrPresent
    @XmlElement
    private LocalDate invoiceReceivedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoicedcurrencyid")
    @XmlIDREF
    private Currency invoicedCurrency;

    @Transient
    private Boolean invoicedDefaultCurrency;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal invoicedPrice;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal invoicedPriceDefaultCurrency;

    @OneToMany(mappedBy = "purchase", cascade = { CascadeType.REMOVE })
    @OrderBy("id asc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<PurchaseItem> items = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<PurchaseNote> notes = new HashSet<>();

    @PastOrPresent
    @NotNull
    @XmlElement
    private LocalDate orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderitemreceivedbyuserid")
    @XmlIDREF
    private User orderItemReceivedBy;

    @PastOrPresent
    @XmlElement
    private LocalDate orderItemReceivedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderedbyuserid")
    @NotNull
    @XmlIDREF
    private User orderedBy;

    @Size(max = 256)
    @XmlElement
    private String p4uNumber;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean paid = false;

    @Size(max = 4)
    @XmlElement
    private String payer;

    @Size(max = 256)
    @XmlElement
    private String payersReferenceNumber;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal price;

    @Transient
    private boolean recalculatePriceChecked = false;

    @XmlElement
    private Long sapNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplierid")
    @XmlIDREF
    private Supplier supplier;

    @ManyToMany
    @JoinTable(name = "purchasetechnology",
        joinColumns = @JoinColumn(name = "purchaseid"),
        inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @XmlIDREF
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technologies")
    private String technologiesAsString;

    @ManyToMany
    @JoinTable(name = "purchaseuser", joinColumns = @JoinColumn(name = "purchaseid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "user")
    private Set<User> users = new HashSet<>();

    public Purchase() {
        super();
    }

    @Override
    public Purchase clone() throws CloneNotSupportedException {
        final Purchase clone = (Purchase) super.clone();
        clone.contracts = new HashSet<>();
        clone.notes = new HashSet<>();
        clone.users = new HashSet<>();
        clone.items = new HashSet<>();
        clone.setInvoiceNumber(null);
        clone.setInvoiceReceivedDate(null);
        clone.setInvoicedCurrency(null);
        clone.setInvoicedPrice(null);
        clone.setOrderItemReceivedDate(null);
        clone.setComment(null);
        clone.setInternal(false);
        clone.setP4uNumber(null);
        clone.setSapNumber(null);
        clone.setPaid(false);
        clone.setPayersReferenceNumber(null);
        clone.setPayer(null);
        for (PurchaseItem purchaseItem : getItems()) {
            PurchaseItem itemClone = purchaseItem.clone();
            itemClone.setPurchase(clone);
            clone.getItems().add(itemClone);
        }
        return clone;
    }

    public String getComment() {
        return comment;
    }

    public String getCommentTrimmed() {
        return StringHelper.removeDoubleEmptyLines(getComment());
    }

    public Set<Contract> getContracts() {
        return contracts;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.PURCHASEMANAGER;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    public String getDisplayName() {
        return getId() + " - " + getName();
    }

    public List<User> getEmployeesExcludingUsers(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFiltered(filterString, getUsers());
    }

    public List<User> getEmployeesIncludingOrderItemReceivedBy(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getOrderItemReceivedBy());
    }

    public List<User> getEmployeesIncludingOrderedBy(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getOrderedBy());
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getSupplier() != null) {
            addEntityInfoItem(summary, "supplier", getSupplier().getName());
        }
        if (getOrderDate() != null) {
            addEntityInfoItem(summary, "orderDate", getOrderDate());
        }
        if (getOrderedBy() != null) {
            addEntityInfoItem(summary, "orderedBy", getOrderedBy().getLastNameFirstName());
        }
        if (getPrice() != null) {
            addEntityInfoItem(summary, "price", getPrice());
        }
        if (getCurrency() != null) {
            addEntityInfoItem(summary, "currency", getCurrency().getCode());
        }
        if (!getTechnologies().isEmpty()) {
            for (Technology technology : getTechnologies()) {
                addEntityInfoItem(summary, "technology", technology.getName());
            }
        }
        if (StringHelper.isNotEmpty(getP4uNumber())) {
            addEntityInfoItem(summary, "p4uNumber", getP4uNumber());
        }
        if (getOrderItemReceivedDate() != null) {
            addEntityInfoItem(summary, "orderItemReceivedDate", getOrderItemReceivedDate());
        }
        if (getOrderItemReceivedBy() != null) {
            addEntityInfoItem(summary, "orderItemReceivedBy", getOrderItemReceivedBy().getLastNameFirstName());
        }
        if (getInvoiceReceivedDate() != null) {
            addEntityInfoItem(summary, "invoiceReceivedDate", getInvoiceReceivedDate());
        }
        if (StringHelper.isNotEmpty(getInvoiceNumber())) {
            addEntityInfoItem(summary, "invoiceNumber", getInvoiceNumber());
        }
        if (getInvoicedPrice() != null) {
            addEntityInfoItem(summary, "invoicedPrice", getInvoicedPrice());
        }
        if (getInvoicedPriceDefaultCurrency() != null) {
            addEntityInfoItem(summary, "invoicedPriceDefaultCurrency", getInvoicedPriceDefaultCurrency());
        }
        if (StringHelper.isNotEmpty(getPayer())) {
            addEntityInfoItem(summary, "payer", getPayer());
        }
        if (StringHelper.isNotEmpty(getPayersReferenceNumber())) {
            addEntityInfoItem(summary, "payersReferenceNumber", getPayersReferenceNumber());
        }
        addEntityInfoItem(summary, "paid", isPaid());
        if (StringHelper.isNotEmpty(getComment())) {
            addEntityInfoItem(summary, "comment", getComment());
        }
        return summary.toString();
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.SUPPLIER.getField());
        fields.add(IndexMapContentEnum.ORDERDATE.getField());
        fields.add(IndexMapContentEnum.ORDEREDBY.getField());
        fields.add(IndexMapContentEnum.PRICE.getField());
        fields.add(IndexMapContentEnum.CURRENCY.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        fields.add(IndexMapContentEnum.TECHNOLOGY.getField());
        fields.add(IndexMapContentEnum.ORDERITEMRECEIVEDDATE.getField());
        fields.add(IndexMapContentEnum.ORDERITEMRECEIVEDBY.getField());
        fields.add(IndexMapContentEnum.INVOICERECEIVEDDATE.getField());
        fields.add(IndexMapContentEnum.INVOICENUMBER.getField());
        fields.add(IndexMapContentEnum.INVOICEDPRICE.getField());
        fields.add(IndexMapContentEnum.INVOICEDCURRENCY.getField());
        fields.add(IndexMapContentEnum.PAYER.getField());
        fields.add(IndexMapContentEnum.PAYERSREFERENCENUMBER.getField());
        fields.add(IndexMapContentEnum.PAID.getField());
        fields.add(IndexMapContentEnum.P4UNUMBER.getField());
        fields.add(IndexMapContentEnum.COMMENT.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getName());

        content.add(IndexMapContentEnum.SUPPLIER, getSupplierName());
        content.add(IndexMapContentEnum.ORDERDATE, getOrderDate());
        if (getOrderedBy() != null) {
            content.add(IndexMapContentEnum.ORDEREDBY, getOrderedBy().getFullName());
            content.add(IndexMapContentEnum.ORDEREDBY, getOrderedBy().getLogin());
        }
        content.add(IndexMapContentEnum.PRICE, getPrice());
        if (getCurrency() != null) {
            content.add(IndexMapContentEnum.CURRENCY, getCurrency().getCode());
        }
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());
        if (!getTechnologies().isEmpty()) {
            for (Technology technology : getTechnologies()) {
                content.add(IndexMapContentEnum.TECHNOLOGY, technology.getName());
            }
        }
        content.add(IndexMapContentEnum.ORDERITEMRECEIVEDDATE, getOrderItemReceivedDate());

        if (getOrderItemReceivedBy() != null) {
            content.add(IndexMapContentEnum.ORDERITEMRECEIVEDBY, getOrderItemReceivedBy().getFullName());
            content.add(IndexMapContentEnum.ORDERITEMRECEIVEDBY, getOrderItemReceivedBy().getLogin());
        }
        content.add(IndexMapContentEnum.INVOICERECEIVEDDATE, getInvoiceReceivedDate());
        content.add(IndexMapContentEnum.INVOICENUMBER, getInvoiceNumber());
        content.add(IndexMapContentEnum.INVOICEDPRICE, getInvoicedPrice());
        if (getInvoicedCurrency() != null) {
            content.add(IndexMapContentEnum.INVOICEDCURRENCY, getInvoicedCurrency().getCode());
        }
        if (getPayer() != null) {
            content.add(IndexMapContentEnum.PAYER, getPayer());
        }
        if (getPayersReferenceNumber() != null) {
            content.add(IndexMapContentEnum.PAYERSREFERENCENUMBER, getPayersReferenceNumber());
        }
        content.add(IndexMapContentEnum.PAID, isPaid());
        content.add(IndexMapContentEnum.P4UNUMBER, getP4uNumber());
        content.add(IndexMapContentEnum.COMMENT, getComment());
        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.PURCHASE;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDate getInvoiceReceivedDate() {
        return invoiceReceivedDate;
    }

    public Currency getInvoicedCurrency() {
        return invoicedCurrency;
    }

    public BigDecimal getInvoicedPrice() {
        return invoicedPrice;
    }

    public BigDecimal getInvoicedPriceDefaultCurrency() {
        return invoicedPriceDefaultCurrency;
    }

    public Set<PurchaseItem> getItems() {
        return items;
    }

    public String getItemsSpecifics() {
        return CollectionHelper.print(getItems(), "getEntitySpecifics", "\n", false);
    }

    public String getName() {
        String name = Constants.EMPTY_STRING;
        if (getSupplierName() != null) {
            name += getSupplierName() + ": ";
        }
        if (getOrderDate() != null) {
            name += getOrderDate() + ": ";
        }
        if (getOrderedBy() != null) {
            name += getOrderedBy().getName();
        }
        return name;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.PURCHASE_NOTE;
    }

    public Set<PurchaseNote> getNotes() {
        return notes;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public User getOrderItemReceivedBy() {
        return orderItemReceivedBy;
    }

    public LocalDate getOrderItemReceivedDate() {
        return orderItemReceivedDate;
    }

    public User getOrderedBy() {
        return orderedBy;
    }

    public String getP4uNumber() {
        return p4uNumber;
    }

    public String getPayer() {
        return payer;
    }

    public String getPayersReferenceNumber() {
        return payersReferenceNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getPurchaseItemsPriceSum(Collection<PurchaseItem> purchaseItems) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PurchaseItem purchaseItem : purchaseItems) {
            sum = sum.add(purchaseItem.getTotalPrice());
        }
        return NumberUtils.getDecimalScale2(sum);
    }

    public String getPurchasePriceNotEqualHint(Collection<PurchaseItem> purchaseItems) {
        return isRecalculatePriceNotEqualHintRendered(purchaseItems) ? Messages.get("purchasePriceNotEqualHint").replace("{0}", String.valueOf(getPrice()))
            .replace("{1}", String.valueOf(getPurchaseItemsPriceSum(purchaseItems))) : null;
    }

    public Long getSapNumber() {
        return sapNumber;
    }

    public BigDecimal getSumOfPurchaseItemPrices() {
        return getPurchaseItemsPriceSum(getItems());
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public String getSupplierName() {
        return getSupplier() != null ? getSupplier().getName() : Constants.EMPTY_STRING;
    }

    public Set<Technology> getTechnologies() {
        return technologies;
    }

    @Override
    public String getTechnologiesAsString() {
        return technologiesAsString;
    }

    public Set<User> getUsers() {
        return users;
    }

    public List<User> getUsersAsList() {
        return CollectionHelper.asList(users);
    }

    public void invoicedPriceChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setInvoicedPrice(new BigDecimal(event.getNewValue().toString()));
        } else {
            setInvoicedPrice(null);
            setInvoicedCurrency(null);
        }
    }

    public boolean isCurrencyChanged() {
        return currencyChanged;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getOrderItemReceivedDate() == null;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isInvoicedDefaultCurrency() {
        if (invoicedDefaultCurrency == null) {
            invoicedDefaultCurrency = invoicedCurrency != null && getConfiguration().getDefaultCurrencyCode().equalsIgnoreCase(invoicedCurrency.getCode());
        }
        return invoicedDefaultCurrency;
    }

    public boolean isPaid() {
        return paid;
    }

    @Override
    public boolean isReadable() {
        return (isCreatable() || hasCurrentUserRoleEnum(RoleEnum.PURCHASEREADER)) && (!isInternal() || hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER));
    }

    public boolean isRecalculatePriceChecked() {
        return recalculatePriceChecked;
    }

    public boolean isRecalculatePriceNotEqualHintRendered(Collection<PurchaseItem> purchaseItems) {
        BigDecimal priceSum = getPurchaseItemsPriceSum(purchaseItems);
        return getPrice() != null ? getPrice().compareTo(priceSum) != 0 : !priceSum.equals(BigDecimal.ZERO);
    }

    public boolean isRecalculatePriceNotEqualHintRenderedForPurchase(Collection<PurchaseItem> purchaseItems) {
        BigDecimal priceSum = getPurchaseItemsPriceSum(purchaseItems);
        return getPrice() != null ? !purchaseItems.isEmpty() && getPrice().compareTo(priceSum) != 0 : !priceSum.equals(BigDecimal.ZERO);
    }

    @Override
    public boolean isUpdatable() {
        return isReadable() && hasCurrentUserRoleEnum(RoleEnum.PURCHASEMANAGER) && (!isInternal() || hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER));
    }

    public void orderItemReceivedDateChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            LocalDate newDate = (LocalDate) event.getNewValue();
            if (!newDate.equals(getOrderItemReceivedDate())) {
                setSendMail(true);
            }
            if (getOrderItemReceivedBy() == null) {
                setOrderItemReceivedBy(getCurrentUser());
            }
        } else {
            setOrderItemReceivedBy(null);
        }
    }

    public void priceChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setPrice(new BigDecimal(event.getNewValue().toString()));
        } else {
            setPrice(null);
            setCurrency(null);
        }
    }

    public void recalculatePriceCheckedChanged() {
        setRecalculatePriceChecked(!isRecalculatePriceChecked());
    }

    public void setComment(String comment) {
        this.comment = StringHelper.formatText(comment);
    }

    public void setContracts(Set<Contract> contracts) {
        this.contracts = contracts;
    }

    public void setCurrency(Currency currency) {
        if (!(this.currency == null && currency == null || currency != null && currency.equals(this.currency))) {
            setCurrencyChanged(true);
        }
        this.currency = currency;
    }

    public void setCurrencyChanged(boolean currencyChanged) {
        this.currencyChanged = currencyChanged;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = StringHelper.format(invoiceNumber);
    }

    public void setInvoiceReceivedDate(LocalDate invoiceReceivedDate) {
        this.invoiceReceivedDate = invoiceReceivedDate;
    }

    public void setInvoicedCurrency(Currency invoicedCurrency) {
        this.invoicedCurrency = invoicedCurrency;
    }

    public void setInvoicedPrice(BigDecimal invoicedPrice) {
        this.invoicedPrice = invoicedPrice;
    }

    public void setInvoicedPriceDefaultCurrency(BigDecimal invoicedPriceDefaultCurrency) {
        this.invoicedPriceDefaultCurrency = invoicedPriceDefaultCurrency;
    }

    public void setItems(Set<PurchaseItem> items) {
        this.items = items;
    }

    public void setNotes(Set<PurchaseNote> notes) {
        this.notes = notes;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setOrderItemReceivedBy(User orderItemReceivedBy) {
        this.orderItemReceivedBy = orderItemReceivedBy;
    }

    public void setOrderItemReceivedDate(LocalDate orderItemReceivedDate) {
        this.orderItemReceivedDate = orderItemReceivedDate;
    }

    public void setOrderedBy(User orderedBy) {
        this.orderedBy = orderedBy;
    }

    public void setP4uNumber(String p4uNumber) {
        this.p4uNumber = StringHelper.format(p4uNumber);
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public void setPayer(String payer) {
        this.payer = StringHelper.format(payer);
    }

    public void setPayersReferenceNumber(String payersReferenceNumber) {
        this.payersReferenceNumber = StringHelper.format(payersReferenceNumber);
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        if (price == null) {
            setCurrency(null);
        }
    }

    public void setPriceToSumOfPurchaseItemPrices() {
        setPrice(getSumOfPurchaseItemPrices());
    }

    public void setRecalculatePriceChecked(boolean recalculatePriceChecked) {
        this.recalculatePriceChecked = recalculatePriceChecked;
    }

    public void setSapNumber(Long sapNumber) {
        this.sapNumber = sapNumber;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    @Override
    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void setUsersAsList(List<User> users) {
        this.users = (Set<User>) CollectionHelper.asSet(users);
    }
}
