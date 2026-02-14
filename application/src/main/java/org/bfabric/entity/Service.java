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
import java.util.ArrayList;
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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.ServiceCodeService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "service_name_unique", columnNames = { "name", "servicetypeid" }) })
@XmlRootElement
@NamedQuery(name = "Service.findByServiceArea", query = "SELECT a FROM Service a WHERE a.serviceType.serviceArea = :serviceArea ORDER BY a.serviceType.name, a.name")
@NamedQuery(name = "Service.countByServiceArea", query = "SELECT COUNT(a) FROM Service a WHERE a.serviceType.serviceArea = :serviceArea")
@NamedQuery(name = "Service.findEnabledByServiceArea", query = "SELECT a FROM Service a WHERE a.enabled = true AND a.serviceType.serviceArea = :serviceArea ORDER BY a.serviceType.name, a.name")
@NamedQuery(name = "Service.countEnabledByServiceArea", query = "SELECT COUNT(a) FROM Service a WHERE a.enabled = true AND a.serviceType.serviceArea = :serviceArea")
@NamedQuery(name = "Service.checkUniqueName", query = "SELECT a.id FROM Service a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.serviceType = :serviceType")
public class Service extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen, Mergeable {

    private static final long serialVersionUID = 1;

    @Transient
    List<ServiceOrganizationTypePrice> serviceOrganizationTypePricesAsList;

    @OneToMany(mappedBy = "service")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<AnalysisReason> analysisReasons = new HashSet<>();

    @OneToMany(mappedBy = "service")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "serviceparent", joinColumns = { @JoinColumn(name = "parentid") }, inverseJoinColumns = { @JoinColumn(name = "serviceid") })
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Service> children = new HashSet<>();

    @Column(updatable = false, insertable = false)
    private boolean deletable;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal fullCost;

    @OneToMany(mappedBy = "service")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> instruments = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<ServiceNote> notes = new HashSet<>();

    @OneToMany(mappedBy = "service")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OfferedCharge> offeredCharges = new HashSet<>();

    @OneToMany(mappedBy = "service")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "serviceparent", joinColumns = @JoinColumn(name = "serviceid"), inverseJoinColumns = @JoinColumn(name = "parentid"))
    @XmlIDREF
    @XmlElement(name = "parent")
    private Set<Service> parents = new HashSet<>();

    @Transient
    private ServiceArea serviceArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceCodeId")
    @XmlIDREF
    private ServiceCode serviceCode;

    @OneToMany(mappedBy = "service", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("organizationType.id")
    private Set<ServiceOrganizationTypePrice> serviceOrganizationTypePrices = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicetypeid")
    @NotNull
    @XmlIDREF
    private ServiceType serviceType;

    @Transient
    private Boolean tracked;

    @ManyToMany
    @JoinTable(name = "usertrackedservice", joinColumns = @JoinColumn(name = "serviceid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("lastName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> trackingUsers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "serviceuser", joinColumns = @JoinColumn(name = "serviceid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "user")
    private Set<User> users = new HashSet<>();

    public Service() {
        super();
    }

    public void clearPrices() {
        for (ServiceOrganizationTypePrice price : getServiceOrganizationTypePrices()) {
            price.setAdditionalPrice(BigDecimal.ZERO);
            price.setBasicPrice(BigDecimal.ZERO);
            price.setEuGrantPrice(BigDecimal.ZERO);
        }
        setFullCost(null);
    }

    @Override
    public Service clone() throws CloneNotSupportedException {
        final Service clone = (Service) super.clone();
        clone.serviceArea = getServiceType().getServiceArea();
        clone.charges = new HashSet<>();
        clone.offeredCharges = new HashSet<>();
        clone.children = new HashSet<>();
        clone.instruments = new HashSet<>();
        clone.notes = new HashSet<>();
        clone.orderItems = new HashSet<>();
        clone.parents = new HashSet<>();
        clone.serviceOrganizationTypePrices = new HashSet<>();
        clone.trackingUsers = new HashSet<>();
        clone.users = new HashSet<>();
        for (Service child : getChildren()) {
            Service childClone = child.clone();
            childClone.getParents().add(clone);
            clone.getChildren().add(childClone);
        }
        for (ServiceOrganizationTypePrice serviceOrganizationTypePrice : getServiceOrganizationTypePrices()) {
            ServiceOrganizationTypePrice serviceOrganizationTypePriceClone = serviceOrganizationTypePrice.clone();
            serviceOrganizationTypePriceClone.setService(clone);
            clone.getServiceOrganizationTypePrices().add(serviceOrganizationTypePriceClone);
        }
        if (getUsers() != null && !getUsers().isEmpty()) {
            clone.users.addAll(getUsers());
        }
        if (getTrackingUsers() != null && !getTrackingUsers().isEmpty()) {
            clone.trackingUsers.addAll(getTrackingUsers());
        }
        return clone;
    }

    public Set<AnalysisReason> getAnalysisReasons() {
        return analysisReasons;
    }

    public Set<Service> getAncestors() {
        Set<Service> ancestors = new HashSet<>();
        return getAncestors(ancestors);
    }

    public Set<Service> getAncestors(Set<Service> ancestors) {
        for (Service service : getParents()) {
            if (!ancestors.contains(service)) {
                ancestors.add(service);
                service.getAncestors(ancestors);
            }
        }
        return ancestors;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public Set<Service> getChildren() {
        return children;
    }

    public List<Service> getChildrenAsList() {
        return CollectionHelper.asList(children);
    }

    public List<Service> getChildrenFiltered(String filterString) {
        List<Service> childrenFiltered = new ArrayList<>();
        if (StringHelper.isEmpty(filterString)) {
            childrenFiltered.addAll(getChildren());
        } else {
            for (Service parent : getChildren()) {
                if (parent.getName().contains(filterString) || parent.getIdString().contains(filterString)) {
                    childrenFiltered.add(parent);
                }
            }
        }
        return childrenFiltered;
    }

    public String getCode() {
        return getServiceCode() != null ? getServiceCode().getName() : null;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.SERVICEMANAGER;
    }

    public Set<Service> getDescendants() {
        Set<Service> descendants = new HashSet<>();
        return getDescendants(descendants);
    }

    public Set<Service> getDescendants(Set<Service> descendants) {
        for (Service service : getChildren()) {
            if (!descendants.contains(service)) {
                descendants.add(service);
                service.getDescendants(descendants);
            }
        }
        return descendants;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    public List<ServiceCode> getEnabledServiceCodesIncluding(String filterString) {
        return (List<ServiceCode>) CDI.current().select(ServiceCodeService.class).get().getFilteredEnabledIncludingOrderBy(serviceCode, filterString, null);
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getServiceType() != null) {
            addEntityInfoItem(summary, "serviceType", getServiceType().getName());
            addEntityInfoItem(summary, "serviceArea", getServiceType().getServiceArea().getName());
        }
        if (hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER)) {
            if (StringHelper.isNotEmpty(getCode())) {
                addEntityInfoItem(summary, "code", getCode());
            }
            if (getFullCost() != null) {
                addEntityInfoItem(summary, "fullCost", getFullCost());
            }
            for (ServiceOrganizationTypePrice serviceOrganizationTypePrice : getServiceOrganizationTypePrices()) {
                addEntityInfoItem(summary, Constants.EMPTY_STRING, StringHelper.format(serviceOrganizationTypePrice.getEntitySpecifics()));
            }
        }
        return summary.toString();
    }

    public BigDecimal getFullCost() {
        return fullCost;
    }

    public String getFullName() {
        String fullName;
        if (getServiceType() != null && getServiceType().getServiceArea() != null) {
            fullName = getName() + " - " + getServiceType().getName() + " - " + getServiceType().getServiceArea().getName();
        } else {
            fullName = getName();
        }
        return fullName;
    }

    @Override
    public String getGroupingAttributes() {
        return getServiceType().getName() + ", " + getServiceType().getServiceArea().getName();
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.SERVICE_NOTE;
    }

    public Set<ServiceNote> getNotes() {
        return notes;
    }

    public Set<OfferedCharge> getOfferedCharges() {
        return offeredCharges;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Set<Service> getParents() {
        return parents;
    }

    public List<Service> getParentsAsList() {
        return CollectionHelper.asList(parents);
    }

    public List<Service> getParentsFiltered(String filterString) {
        List<Service> parentsFiltered = new ArrayList<>();
        if (StringHelper.isEmpty(filterString)) {
            parentsFiltered.addAll(getParents());
        } else {
            for (Service parent : getParents()) {
                if (parent.getName().contains(filterString) || parent.getIdString().contains(filterString)) {
                    parentsFiltered.add(parent);
                }
            }
        }
        return parentsFiltered;
    }

    @XmlElement(name = "price")
    public List<String> getPrices() {
        List<String> prices = new ArrayList<>();
        for (ServiceOrganizationTypePrice price : getServiceOrganizationTypePrices()) {
            prices.add(price.getOrganizationType().getId() + " b " + price.getBasicPrice());
            prices.add(price.getOrganizationType().getId() + " a " + price.getAdditionalPrice());
        }
        return prices;
    }

    public ServiceArea getServiceArea() {
        return serviceArea;
    }

    public ServiceCode getServiceCode() {
        return serviceCode;
    }

    public BigDecimal getServiceOrganizationTypePrice(OrganizationType organizationType) {
        ServiceOrganizationTypePrice serviceOrganizationTypePrice = getServiceOrganizationTypePrices(organizationType);
        return serviceOrganizationTypePrice != null ? serviceOrganizationTypePrice.getBasicPrice() : null;
    }

    public Set<ServiceOrganizationTypePrice> getServiceOrganizationTypePrices() {
        return this.serviceOrganizationTypePrices;
    }

    public ServiceOrganizationTypePrice getServiceOrganizationTypePrices(OrganizationType organizationType) {
        ServiceOrganizationTypePrice serviceOrganizationTypePrice = null;
        for (ServiceOrganizationTypePrice price : serviceOrganizationTypePrices) {
            if (price.getOrganizationType().equals(organizationType)) {
                serviceOrganizationTypePrice = price;
                break;
            }
        }
        return serviceOrganizationTypePrice;
    }

    public List<ServiceOrganizationTypePrice> getServiceOrganizationTypePricesAsList() {
        if (serviceOrganizationTypePricesAsList == null) {
            serviceOrganizationTypePricesAsList = CollectionHelper.sortObjects(getServiceOrganizationTypePrices());
        }
        return serviceOrganizationTypePricesAsList;
    }

    public ServiceOrganizationTypePrice getServiceOrganizationTypePricesByOrganizationType(OrganizationType organizationType) {
        ServiceOrganizationTypePrice serviceOrganizationTypePrice = null;
        for (ServiceOrganizationTypePrice price : serviceOrganizationTypePrices) {
            if (price.getOrganizationType().equals(organizationType)) {
                serviceOrganizationTypePrice = price;
                break;
            }
        }
        return serviceOrganizationTypePrice;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public ServiceOrganizationTypePrice getServiceUniversityZurichPrice() {
        ServiceOrganizationTypePrice serviceOrganizationTypePrice = null;
        for (ServiceOrganizationTypePrice price : getServiceOrganizationTypePrices()) {
            if (price.getOrganizationType().getId() == 1) {
                serviceOrganizationTypePrice = price;
                break;
            }
        }
        return serviceOrganizationTypePrice;
    }

    public Boolean getTracked() {
        if (tracked == null) {
            tracked = getCurrentUser().getTrackedServices().contains(this);
        }
        return tracked;
    }

    public Set<User> getTrackingUsers() {
        return trackingUsers;
    }

    public Set<User> getUsers() {
        return users;
    }

    public List<User> getUsersAsList() {
        return CollectionHelper.asList(users);
    }

    public boolean isAdditionalPriceColumnRendered() {
        for (ServiceOrganizationTypePrice price : getServiceOrganizationTypePrices()) {
            if (price.getAdditionalPrice() != null && price.getAdditionalPrice().doubleValue() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isClearPricesRendered() {
        if (hasCurrentUserRoleEnum(RoleEnum.SERVICEMANAGER)) {
            for (ServiceOrganizationTypePrice price : getServiceOrganizationTypePrices()) {
                if (!BigDecimal.ZERO.equals(price.getBasicPrice()) || !BigDecimal.ZERO.equals(price.getEuGrantPrice()) || !BigDecimal.ZERO.equals(price.getAdditionalPrice())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isConsumedService() {
        return !getInstruments().isEmpty() || !getCharges().isEmpty() || !getOfferedCharges().isEmpty();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && isDeletableWithoutUpdatableCheck();
    }

    public boolean isDeletableWithoutUpdatableCheck() {
        return deletable;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void serviceAreaChanged(ValueChangeEvent event) {
        setServiceArea((ServiceArea) event.getNewValue());
        setServiceType(null);
    }

    public void setCharges(Set<Charge> charges) {
        this.charges = charges;
    }

    public void setChildrenAsList(List<Service> children) {
        this.children = (Set<Service>) CollectionHelper.asSet(children);
    }

    public void setFullCost(BigDecimal fullCost) {
        this.fullCost = fullCost;
    }

    public void setInstruments(Set<Instrument> instruments) {
        this.instruments = instruments;
    }

    public void setOfferedCharges(Set<OfferedCharge> offeredCharges) {
        this.offeredCharges = offeredCharges;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setParentsAsList(List<Service> parents) {
        this.parents = (Set<Service>) CollectionHelper.asSet(parents);
    }

    public void setServiceArea(ServiceArea serviceArea) {
        this.serviceArea = serviceArea;
    }

    public void setServiceCode(ServiceCode serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setServiceOrganizationTypePrices(Set<ServiceOrganizationTypePrice> serviceOrganizationTypePrices) {
        this.serviceOrganizationTypePrices = serviceOrganizationTypePrices;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void setTracked(Boolean tracked) {
        this.tracked = tracked;
    }

    public void setTrackingUsers(Set<User> trackingUsers) {
        this.trackingUsers = trackingUsers;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void setUsersAsList(List<User> users) {
        this.users = (Set<User>) CollectionHelper.asSet(users);
    }

    public void switchTracked() {
        setTracked(!getTracked());
    }
}