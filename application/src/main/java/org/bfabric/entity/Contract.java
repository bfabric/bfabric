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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.ContractStatusEnum;
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
@Table(uniqueConstraints = { @UniqueConstraint(name = "contract_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "Contract.findExpired", query = "SELECT a FROM Contract a WHERE a.status = org.bfabric.enums.ContractStatusEnum.ENABLED and a.expiryDate < current_date")
@NamedQuery(name = "Contract.findExpiring", query = "SELECT a FROM Contract a WHERE a.status = org.bfabric.enums.ContractStatusEnum.ENABLED and (a.expiryDate = current_date + 90 or a.expiryDate = current_date + 60 or a.expiryDate = current_date + 30 or a.expiryDate < current_date)")
public class Contract extends AbstractSupervisorDescriptionNamedBaseEntity implements ShowScreen, Indexable, TechnologiesDependent {

    private static final long serialVersionUID = 1;

    @PastOrPresent
    @XmlElement
    private LocalDate approvalDate;

    @Size(max = 512)
    @XmlElement
    private String approvalNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedbyid")
    @XmlIDREF
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typeid")
    @NotNull
    @XmlIDREF
    private ContractType contractType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyid")
    @XmlIDREF
    private Currency currency;

    @XmlElement
    private LocalDate expiryDate;

    @Column(updatable = false, insertable = false)
    private String instrumentNamesAsString;

    @ManyToMany
    @JoinTable(name = "contractinstrument", joinColumns = @JoinColumn(name = "contractid"), inverseJoinColumns = @JoinColumn(name = "instrumentid"))
    @OrderBy("label")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "instrument")
    private Set<Instrument> instruments = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<ContractNote> notes = new HashSet<>();

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @ManyToMany
    @JoinTable(name = "contractpurchase", joinColumns = @JoinColumn(name = "contractid"), inverseJoinColumns = @JoinColumn(name = "purchaseid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<Purchase> purchases = new HashSet<>();

    @Size(max = 256)
    @XmlElement
    private String referenceNumber;

    @XmlElement
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private ContractStatusEnum status = ContractStatusEnum.ENABLED;

    @ManyToMany
    @JoinTable(name = "contracttechnology", joinColumns = @JoinColumn(name = "contractid"), inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @XmlIDREF
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technologies")
    private String technologiesAsString;

    public Contract() {
        super();
    }

    @Override
    public Contract clone() throws CloneNotSupportedException {
        Contract clone = (Contract) super.clone();
        clone.instruments = new HashSet<>();
        clone.notes = new HashSet<>();
        return clone;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public String getApprovalNote() {
        return approvalNote;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    @Override
    public Contract getClone() {
        return (Contract) super.getClone();
    }

    public ContractType getContractType() {
        return contractType;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTRACTMANAGER;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    public List<User> getEmployeesIncludingApprovedBy(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getApprovedBy());
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (!getTechnologies().isEmpty()) {
            for (Technology technology : getTechnologies()) {
                addEntityInfoItem(summary, "technology", technology.getName());
            }
        }
        if (getContractType() != null) {
            addEntityInfoItem(summary, "type", getContractType().getName());
        }
        if (StringHelper.isNotEmpty(getReferenceNumber())) {
            addEntityInfoItem(summary, "referenceNumber", getReferenceNumber());
        }
        if (getStartDate() != null) {
            addEntityInfoItem(summary, "startDate", getStartDate());
        }
        if (getExpiryDate() != null) {
            addEntityInfoItem(summary, "expiryDate", getExpiryDate());
        }
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus());
        }
        if (getPrice() != null) {
            addEntityInfoItem(summary, "price", getPrice());
        }
        if (getCurrency() != null) {
            addEntityInfoItem(summary, "currency", getCurrency().getCode());
        }
        if (getApprovalDate() != null) {
            addEntityInfoItem(summary, "approvalDate", getApprovalDate());
        }
        if (getApprovedBy() != null) {
            addEntityInfoItem(summary, "approvedBy", getApprovedBy().getName());
        }
        if (StringHelper.isNotEmpty(getApprovalNote())) {
            addEntityInfoItem(summary, "approvalNote", getApprovalNote());
        }
        if (!getPurchases().isEmpty()) {
            for (Purchase purchase : getPurchases()) {
                addEntityInfoItem(summary, "purchase", purchase.getId());
            }
        }
        return summary.toString();
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public ContractStatusEnum getExpiryDateStatus() {
        ContractStatusEnum expiryDateStatus;
        if (getExpiryDate() == null) {
            expiryDateStatus = ContractStatusEnum.NONEXPIRING;
        } else {
            LocalDate today = LocalDate.now();
            if (today.isAfter(getExpiryDate())) {
                expiryDateStatus = ContractStatusEnum.EXPIRED;
            } else {
                expiryDateStatus = ContractStatusEnum.ENABLED;
            }
        }
        return expiryDateStatus;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.SUPERVISOR.getField());
        fields.add(IndexMapContentEnum.TECHNOLOGY.getField());
        fields.add(IndexMapContentEnum.STATUS.getField());
        fields.add(IndexMapContentEnum.TYPE.getField());
        fields.add(IndexMapContentEnum.PRICE.getField());
        fields.add(IndexMapContentEnum.CURRENCY.getField());
        fields.add(IndexMapContentEnum.APPROVALDATE.getField());
        fields.add(IndexMapContentEnum.APPROVALNOTE.getField());
        fields.add(IndexMapContentEnum.APPROVEDBY.getField());
        fields.add(IndexMapContentEnum.EXPIRYDATE.getField());
        fields.add(IndexMapContentEnum.INSTRUMENTS.getField());
        fields.add(IndexMapContentEnum.REFERENCENUMBER.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getName());
        if (getSupervisor() != null) {
            content.add(IndexMapContentEnum.SUPERVISOR, getSupervisor().getFullName());
            content.add(IndexMapContentEnum.SUPERVISOR, getSupervisor().getLogin());
        }
        if (!getTechnologies().isEmpty()) {
            for (Technology technology : getTechnologies()) {
                content.add(IndexMapContentEnum.TECHNOLOGY, technology.getName());
            }
        }
        content.add(IndexMapContentEnum.STATUS, getStatus());
        if (getContractType() != null) {
            content.add(IndexMapContentEnum.TYPE, getContractType().getName());
        }
        if (getPrice() != null) {
            content.add(IndexMapContentEnum.PRICE, getPrice());
            if (getCurrency() != null) {
                content.add(IndexMapContentEnum.CURRENCY, getCurrency().getCode());
            }
        }
        content.add(IndexMapContentEnum.APPROVALDATE, getApprovalDate());
        content.add(IndexMapContentEnum.APPROVALNOTE, getApprovalNote());
        content.add(IndexMapContentEnum.APPROVEDBY, getApprovedBy());
        content.add(IndexMapContentEnum.STARTDATE, getStartDate());
        content.add(IndexMapContentEnum.EXPIRYDATE, getExpiryDate());
        content.add(IndexMapContentEnum.INSTRUMENTS, getInstrumentNamesAsString());
        content.add(IndexMapContentEnum.REFERENCENUMBER, getReferenceNumber());
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.CONTRACT;
    }

    public String getInstrumentNamesAsString() {
        return instrumentNamesAsString;
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }

    public List<Instrument> getInstrumentsAsList() {
        return CollectionHelper.asList(getInstruments());
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.CONTRACT_NOTE;
    }

    public Set<ContractNote> getNotes() {
        return notes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Set<Purchase> getPurchases() {
        return purchases;
    }

    public List<Purchase> getPurchasesAsList() {
        return CollectionHelper.asList(purchases);
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getRowStyleClass() {
        if (isExpired()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isObsolete()) {
            return Constants.BACKGROUND_COLOR_BROWN;
        }
        return Constants.BACKGROUND_COLOR_GREEN;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public ContractStatusEnum getStatus() {
        return status;
    }

    @Override
    @NotNull
    public User getSupervisor() {
        return super.getSupervisor();
    }

    public Set<Technology> getTechnologies() {
        return technologies;
    }

    @Override
    public String getTechnologiesAsString() {
        return technologiesAsString;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && !getPurchases().isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return getStatus().isEnabled();
    }

    public boolean isExpired() {
        return ContractStatusEnum.EXPIRED.equals(getStatus());
    }

    @Override
    public boolean isExtensible() {
        return isUpdatable();
    }

    public boolean isObsolete() {
        return ContractStatusEnum.OBSOLETE.equals(getStatus());
    }

    @Override
    public boolean isReadable() {
        return isCreatable() || hasCurrentUserRoleEnum(RoleEnum.CONTRACTREADER) || getSupervisor() != null && getSupervisor().isIdentityUser();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void removeInstrument(Instrument instrument) {
        if (getInstruments() != null && instrument != null) {
            getInstruments().remove(instrument);
        }
    }

    public void resetStatus() {
        setStatus(getExpiryDateStatus());
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public void setApprovalNote(String approvalNote) {
        this.approvalNote = StringHelper.format(approvalNote);
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        if (expiryDate != null && !expiryDate.equals(getExpiryDate())) {
            this.expiryDate = expiryDate;
        } else if (expiryDate == null) {
            this.expiryDate = null;
        }

        setStatus(getExpiryDateStatus());
    }

    public void setInstruments(Set<Instrument> instruments) {
        this.instruments = instruments;
    }

    public void setInstrumentsAsList(List<Instrument> instruments) {
        this.instruments = (Set<Instrument>) CollectionHelper.asSet(instruments);
    }

    public void setNotes(Set<ContractNote> notes) {
        this.notes = notes;
    }

    public void setObsolete() {
        setStatus(ContractStatusEnum.OBSOLETE);
    }

    public void setPrice(BigDecimal price) {
        this.price = NumberUtils.getDecimalScale2(price);
    }

    public void setPurchases(Set<Purchase> purchases) {
        this.purchases = purchases;
    }

    public void setPurchasesAsList(List<Purchase> purchasesList) {
        this.purchases = (Set<Purchase>) CollectionHelper.asSet(purchasesList);
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = StringHelper.format(referenceNumber);
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setStatus(ContractStatusEnum status) {
        this.status = status;
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    @Override
    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }
}