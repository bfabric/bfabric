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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.list.FeedbackAnswerList;
import org.bfabric.list.FeedbackList;
import org.bfabric.service.ServiceService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "servicearea_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "ServiceArea.findByName", query = "SELECT a FROM ServiceArea a WHERE lower(a.name) = lower(:name)")
@NamedQuery(name = "ServiceArea.findByEnabledServicesIncludingService", query = "SELECT DISTINCT sa FROM Service s LEFT JOIN s.serviceType.serviceArea sa WHERE s.enabled = true OR s = :service ORDER BY sa.name")
@NamedQuery(name = "ServiceArea.findByEnabledForOfferServicesIncludingService", query = "SELECT DISTINCT sa FROM Service s LEFT JOIN s.serviceType.serviceArea sa WHERE sa.enabledForOffer = true AND (s.enabled = true OR s = :service) ORDER BY sa.name")
@NamedQuery(name = "ServiceArea.findByServiceTypes", query = "SELECT DISTINCT sa FROM ServiceType st JOIN st.serviceArea sa WHERE st IN (:serviceTypes) ORDER BY sa.name")
@NamedQuery(name = "ServiceArea.findEnabled", query = "SELECT a FROM ServiceArea a WHERE a.enabled = true ORDER BY a.orderPosition")
@NamedQuery(name = "ServiceArea.findEnabledIncluding", query = "SELECT a FROM ServiceArea a WHERE a.enabled = true or a = :entity ORDER BY a.orderPosition")
@NamedQuery(name = "ServiceArea.checkByName", query = "SELECT a FROM ServiceArea a WHERE lower(a.name) = lower(:name)")
public class ServiceArea extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen, Mergeable {

    private static final long serialVersionUID = 1;

    @Column(name = "enabledForOffer", columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    protected boolean enabledForOffer = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contactinfoid")
    @XmlIDREF
    private ContactInfo contactInfo;

    @Column(updatable = false, insertable = false)
    private boolean deletable;

    @Transient
    private BfabricLazyDataModel<FeedbackAnswer> feedbackAnswers;

    @Transient
    private BfabricLazyDataModel<Feedback> feedbacks;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<ServiceAreaNote> notes = new HashSet<>();

    @OneToMany(mappedBy = "serviceArea")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypes = new HashSet<>();

    @OneToMany(mappedBy = "serviceArea")
    @Where(clause = "enabled = false")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypesDisabled = new HashSet<>();

    @OneToMany(mappedBy = "serviceArea")
    @Where(clause = "enabled = true")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypesEnabled = new HashSet<>();

    @Transient
    private List<Service> services;

    @Transient
    private Long servicesCount;

    @Transient
    private List<Service> servicesEnabled;

    @Transient
    private Long servicesEnabledCount;

    @ManyToMany
    @JoinTable(name = "serviceareauser", joinColumns = @JoinColumn(name = "serviceareaid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "user")
    private Set<User> users = new HashSet<>();

    public ServiceArea() {
        super();
    }

    @Override
    public ServiceArea clone() throws CloneNotSupportedException {
        final ServiceArea clone = (ServiceArea) super.clone();
        clone.notes = new HashSet<>();
        clone.serviceTypes = new HashSet<>();
        clone.users = new HashSet<>();
        for (ServiceType serviceType : getServiceTypes()) {
            ServiceType typeClone = serviceType.clone();
            typeClone.setServiceArea(clone);
            clone.getServiceTypes().add(typeClone);
        }
        if (getUsers() != null && !getUsers().isEmpty()) {
            clone.users.addAll(getUsers());
        }
        return clone;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.SERVICEMANAGER;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "enabledForOffer", isEnabledForOffer());
        return summary.toString();
    }

    public BfabricLazyDataModel<FeedbackAnswer> getFeedbackAnswers() {
        if (feedbackAnswers == null) {
            feedbackAnswers = CDI.current().select(FeedbackAnswerList.class).get().getLazyModelByFeedbackContainerServiceAreaId(getId());
        }
        return feedbackAnswers;
    }

    public BfabricLazyDataModel<Feedback> getFeedbacks() {
        if (feedbacks == null) {
            feedbacks = CDI.current().select(FeedbackList.class).get().getLazyModelByContainerServiceAreaId(getId());
        }
        return feedbacks;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.SERVICEAREA_NOTE;
    }

    public Set<ServiceAreaNote> getNotes() {
        return notes;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public Set<ServiceType> getServiceTypesDisabled() {
        return serviceTypesDisabled;
    }

    public Set<ServiceType> getServiceTypesEnabled() {
        return serviceTypesEnabled;
    }

    public List<Service> getServices() {
        if (services == null) {
            services = CDI.current().select(ServiceService.class).get().getServicesByServiceArea(this);
        }
        return services;
    }

    public Long getServicesCount() {
        if (servicesCount == null) {
            servicesCount = CDI.current().select(ServiceService.class).get().getCountServicesByServiceArea(this);
        }
        return servicesCount;
    }

    public List<Service> getServicesEnabled() {
        if (servicesEnabled == null) {
            servicesEnabled = CDI.current().select(ServiceService.class).get().getEnabledServicesByServiceArea(this);
        }
        return servicesEnabled;
    }

    public Long getServicesEnabledCount() {
        if (servicesEnabledCount == null) {
            servicesEnabledCount = CDI.current().select(ServiceService.class).get().getCountEnabledServicesByServiceArea(this);
        }
        return servicesEnabledCount;
    }

    public Set<User> getUsers() {
        return users;
    }

    public List<User> getUsersAsList() {
        return CollectionHelper.asList(users);
    }

    public boolean isClearPricesRendered() {
        if (hasCurrentUserRoleEnum(RoleEnum.SERVICEMANAGER)) {
            for (Service service : getServices()) {
                if (service.isClearPricesRendered()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && isDeletableWithoutUpdatableCheck();
    }

    public boolean isDeletableWithoutUpdatableCheck() {
        return deletable;
    }

    public boolean isEnabledForOffer() {
        return enabledForOffer;
    }

    public boolean isFeedbacksRendered() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER) && !getFeedbacks().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER);
    }

    public boolean isServiceTypesDeletable() {
        for (ServiceType serviceType : getServiceTypes()) {
            if (!serviceType.isDeletableWithoutUpdatableCheck()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    @Override
    public void propagateEnabled() {
        for (ServiceType serviceType : getServiceTypes()) {
            serviceType.setAndPropagateEnabled(isEnabled());
        }
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public void setEnabledForOffer(boolean enabledForOffer) {
        this.enabledForOffer = enabledForOffer;
    }

    public void setServiceTypes(Set<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void setUsersAsList(List<User> users) {
        this.users = (Set<User>) CollectionHelper.asSet(users);
    }
}