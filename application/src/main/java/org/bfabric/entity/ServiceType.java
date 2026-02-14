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
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Messages;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.list.FeedbackAnswerList;
import org.bfabric.list.FeedbackList;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "servicetype_name_unique", columnNames = { "name", "serviceareaid" }) })
@XmlRootElement
@NamedQuery(name = "ServiceType.findByName", query = "SELECT a FROM ServiceType a WHERE lower(a.name) = lower(:name)")
@NamedQuery(name = "ServiceType.findByEnabledAndEnabledForOfferServicesIncludingService", query = "SELECT DISTINCT a FROM Service s LEFT JOIN s.serviceType a WHERE a.serviceArea.enabledForOffer = true AND (s.enabled = true OR s.id = :serviceId) ORDER BY a.name")
@NamedQuery(name = "ServiceType.findByEnabledAndServiceAreaNameAndEnabledForOfferServicesIncludingService", query = "SELECT DISTINCT a FROM Service s LEFT JOIN s.serviceType a WHERE a.serviceArea.enabledForOffer = true AND (s.enabled = true OR s.id = :serviceId) AND a.serviceArea.name = :serviceAreaFilter ORDER BY a.name")
@NamedQuery(name = "ServiceType.findByEnabledServicesAndServiceAreaNameIncludingService", query = "SELECT DISTINCT a FROM Service s LEFT JOIN s.serviceType a WHERE (s.enabled = true OR s.id = :serviceId) AND a.serviceArea.name = :serviceAreaFilter ORDER BY a.name")
@NamedQuery(name = "ServiceType.findByEnabledServicesIncludingService", query = "SELECT DISTINCT a FROM Service s LEFT JOIN s.serviceType a WHERE s.enabled = true OR s.id = :serviceId ORDER BY a.name")
@NamedQuery(name = "ServiceType.findEnabledIncludingByServiceArea", query = "SELECT a FROM ServiceType a WHERE a.enabled = true and serviceArea = :serviceArea or a = :entity ORDER BY a.orderPosition")
@NamedQuery(name = "ServiceType.findEnabledIncludingMultiple", query = "SELECT a FROM ServiceType a WHERE a.enabled = true or a in (:entities) ORDER BY a.orderPosition")
@NamedQuery(name = "ServiceType.findEnabledIncludingBySampleType", query = "SELECT a FROM ServiceType a WHERE a = :entity or enabled = true and requiresProject = :requiresProject and sampleType = :sampleType and (processesPlates = true or processesPlates = :processesPlates) and serviceColumnEnabled = :serviceColumnEnabled and serviceArea.enabled = true  and (internal = false or internal = :internal) ORDER BY serviceArea.name, name")
@NamedQuery(name = "ServiceType.findEnabledIncludingAndSampleTypeAssociated", query = "SELECT a FROM ServiceType a WHERE a = :entity or enabled = true and (sampleType is not null or processesSamples = false) AND serviceArea.enabled = true and (internal = false or internal = :internal) ORDER BY serviceArea.name, name")
@NamedQuery(name = "ServiceType.checkUniqueName", query = "SELECT a.id FROM ServiceType a WHERE lower(a.name) = lower(:name) and a.id <> :id and serviceArea = :serviceArea")
public class ServiceType extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen, TechnologiesDependent, Mergeable {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    protected boolean processesSamples = false;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    protected boolean requiresProject = true;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    protected boolean requiresSameDayProcessing = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    protected boolean serviceColumnEnabled = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    protected boolean processesPlates = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachid")
    @XmlIDREF
    private User coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachbackupid")
    @XmlIDREF
    private User coachBackup;

    @Column(updatable = false, insertable = false)
    private boolean deletable;

    @Transient
    private BfabricLazyDataModel<FeedbackAnswer> feedbackAnswers;

    @Transient
    private BfabricLazyDataModel<Feedback> feedbacks;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean initialCustomStatus = false;

    @Size(max = 256)
    private String instructionLink;

    @ManyToMany
    @JoinTable(name = "instrumentdatapackageservicetypedisabled", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "instrumentdatapackageid"))
    @OrderBy("id desc")
    @XmlIDREF
    @XmlElement(name = "instrumentdatapackagedisabled")
    private Set<InstrumentDataPackage> instrumentDataPackagesDisabled = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumentreadconfigurationservicetypedisabled", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "instrumentreadconfigurationid"))
    @OrderBy("id desc")
    @XmlIDREF
    @XmlElement(name = "instrumentreadconfigurationdisabled")
    private Set<InstrumentReadConfiguration> instrumentReadConfigurationsDisabled = new HashSet<>();

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "serviceType")
    @OrderBy("id desc")
    @XmlIDREF
    private Set<InstrumentReservation> instrumentReservations = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumentservicetype", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "instrumentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> instruments = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean internal = false;

    @ManyToMany
    @JoinTable(name = "multiplexkitservicetype", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "multiplexkitid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "multiplexKit")
    private Set<MultiplexKit> multiplexKits = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<ServiceTypeNote> notes = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "orderattributeservicetype", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "orderattributeid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrderAttribute> orderAttributes = new HashSet<>();

    @OneToMany(mappedBy = "serviceType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @Min(1)
    @XmlElement
    private Integer plateSubmissionProposalLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampletypeid")
    @XmlIDREF
    private SampleType sampleType;

    @OneToMany(mappedBy = "serviceType")
    @OrderBy("name asc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<SequencingApplication> sequencingApplications = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceareaid")
    @NotNull
    @XmlIDREF
    private ServiceArea serviceArea;

    @ManyToMany
    @JoinTable(name = "ServiceTypeCollectionServiceType", joinColumns = @JoinColumn(name = "ServiceTypeId"), inverseJoinColumns = @JoinColumn(name = "ServiceTypeCollectionId"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceTypeCollection> serviceTypeCollections = new HashSet<>();

    @OneToMany(mappedBy = "serviceType")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Service> services = new HashSet<>();

    @OneToMany(mappedBy = "serviceType")
    @Where(clause = "enabled = false")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Service> servicesDisabled = new HashSet<>();

    @OneToMany(mappedBy = "serviceType")
    @Where(clause = "enabled = true")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Service> servicesEnabled = new HashSet<>();

    @NotEmpty
    @ManyToMany
    @JoinTable(name = "servicetypetechnology", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technology")
    private String technologiesAsString;

    @ManyToMany
    @JoinTable(name = "servicetypeuser", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "user")
    private Set<User> users = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workflowtemplateservicetype", joinColumns = @JoinColumn(name = "servicetypeid"), inverseJoinColumns = @JoinColumn(name = "workflowtemplateid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowTemplate> workflowTemplates = new HashSet<>();

    public ServiceType() {
        super();
    }

    @Override
    public ServiceType clone() throws CloneNotSupportedException {
        final ServiceType clone = (ServiceType) super.clone();
        clone.instrumentReservations = new HashSet<>();
        clone.instruments = new HashSet<>();
        clone.notes = new HashSet<>();
        clone.orderAttributes = new HashSet<>();
        clone.orders = new HashSet<>();
        clone.sequencingApplications = new HashSet<>();
        clone.services = new HashSet<>();
        clone.technologies = new HashSet<>();
        clone.users = new HashSet<>();
        clone.workflowTemplates = new HashSet<>();
        if (getInstruments() != null && !getInstruments().isEmpty()) {
            clone.instruments.addAll(getInstruments());
        }
        if (getOrderAttributes() != null && !getOrderAttributes().isEmpty()) {
            clone.orderAttributes.addAll(getOrderAttributes());
        }
        for (Service service : getServices()) {
            Service serviceClone = service.clone();
            serviceClone.setServiceType(clone);
            clone.getServices().add(serviceClone);
        }
        if (getTechnologies() != null && !getTechnologies().isEmpty()) {
            clone.addTechnologies(getTechnologies());
        }
        if (getUsers() != null && !getUsers().isEmpty()) {
            clone.users.addAll(getUsers());
        }
        if (getWorkflowTemplates() != null && !getWorkflowTemplates().isEmpty()) {
            clone.workflowTemplates.addAll(getWorkflowTemplates());
        }
        return clone;
    }

    public User getCoach() {
        return coach;
    }

    public User getCoachBackup() {
        return coachBackup;
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
        addEntityInfoItem(summary, "internal", isInternal());
        if (getServiceArea() != null) {
            addEntityInfoItem(summary, "serviceArea", getServiceArea().getName());
        }
        if (getServiceTypeCollections() != null && !getServiceTypeCollections().isEmpty()) {
            addEntityInfoItem(summary, "serviceTypeCollections", CollectionHelper.print(getServiceTypeCollections(), "getName"));
        }
        if (getCoach() != null) {
            addEntityInfoItem(summary, "coach", getCoach().getName());
        }
        if (getCoachBackup() != null) {
            addEntityInfoItem(summary, "coachBackup", getCoachBackup().getName());
        }
        if (getTechnologiesAsString() != null) {
            addEntityInfoItem(summary, "technologies", getTechnologiesAsString());
        }
        addEntityInfoItem(summary, "requiresProject", isRequiresProject());
        addEntityInfoItem(summary, "processesPlates", isProcessesPlates());
        addEntityInfoItem(summary, "processesSamples", isProcessesSamples());
        if (getSampleType() != null) {
            addEntityInfoItem(summary, "sampleType", getSampleType().getName());
        }
        addEntityInfoItem(summary, "serviceColumnEnabled", isServiceColumnEnabled());
        if (StringHelper.isNotEmpty(getInstructionLink())) {
            addEntityInfoItem(summary, "instructionLink", getInstructionLink());
        }
        return summary.toString();
    }

    public BfabricLazyDataModel<FeedbackAnswer> getFeedbackAnswers() {
        if (feedbackAnswers == null) {
            feedbackAnswers = CDI.current().select(FeedbackAnswerList.class).get().getLazyModelByFeedbackContainerServiceTypeId(getId());
        }
        return feedbackAnswers;
    }

    public BfabricLazyDataModel<Feedback> getFeedbacks() {
        if (feedbacks == null) {
            feedbacks = CDI.current().select(FeedbackList.class).get().getLazyModelByContainerServiceTypeId(getId());
        }
        return feedbacks;
    }

    public String getFullName() {
        return getName() + " - " + getServiceArea().getName();
    }

    @Override
    public String getGroupingAttributes() {
        return getServiceArea().getName();
    }

    public String getInstructionLink() {
        return instructionLink;
    }

    public Set<InstrumentDataPackage> getInstrumentDataPackagesDisabled() {
        return instrumentDataPackagesDisabled;
    }

    public Set<InstrumentReadConfiguration> getInstrumentReadConfigurationsDisabled() {
        return instrumentReadConfigurationsDisabled;
    }

    public Set<InstrumentReservation> getInstrumentReservations() {
        return instrumentReservations;
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }

    public Set<MultiplexKit> getMultiplexKits() {
        return multiplexKits;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.SERVICETYPE_NOTE;
    }

    public Set<ServiceTypeNote> getNotes() {
        return notes;
    }

    public OrderAttribute getOrderAttribute(String attributeName) {
        if (StringHelper.isNotEmpty(attributeName)) {
            for (OrderAttribute orderAttribute : getOrderAttributes()) {
                if (attributeName.equals(orderAttribute.getName())) {
                    return orderAttribute;
                }
            }
        }
        return null;
    }

    public Set<OrderAttribute> getOrderAttributes() {
        return orderAttributes;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Integer getPlateSubmissionProposalLimit() {
        return plateSubmissionProposalLimit;
    }

    public String getPlateSubmissionProposalLimitHint() {
        return plateSubmissionProposalLimit != null ? Messages.get("plateSubmissionProposalLimitWarning").replace("{0}", plateSubmissionProposalLimit.toString()) : null;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public Set<SequencingApplication> getSequencingApplications() {
        return sequencingApplications;
    }

    public ServiceArea getServiceArea() {
        return serviceArea;
    }

    public Set<ServiceTypeCollection> getServiceTypeCollections() {
        return serviceTypeCollections;
    }

    public Set<Service> getServices() {
        return services;
    }

    public Set<Service> getServicesDisabled() {
        return servicesDisabled;
    }

    public Set<Service> getServicesEnabled() {
        return servicesEnabled;
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

    public Set<WorkflowTemplate> getWorkflowTemplates() {
        return workflowTemplates;
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

    public boolean isCoachBackupValid() {
        return !isEnabled() || getCoachBackup() != null && getCoachBackup().hasRoleImplicit(getDefaultRequiredRole());
    }

    public boolean isCoachValid() {
        return !isEnabled() || getCoach() != null && getCoach().hasRoleImplicit(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && isDeletableWithoutUpdatableCheck();
    }

    public boolean isDeletableWithoutUpdatableCheck() {
        return deletable;
    }

    public boolean isFeedbacksRendered() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER) && !getFeedbacks().isEmpty();
    }

    public boolean isInitialCustomStatus() {
        return initialCustomStatus;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isOrderAttribute(String attributeName) {
        return getOrderAttribute(attributeName) != null;
    }

    public boolean isOrderAttributeAndEnabled(String attributeName) {
        OrderAttribute orderAttribute = getOrderAttribute(attributeName);
        return orderAttribute != null && orderAttribute.isEnabled();
    }

    public boolean isProcessesPlates() {
        return processesPlates;
    }

    public boolean isProcessesSamples() {
        return processesSamples;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER);
    }

    public boolean isRequiresProject() {
        return requiresProject;
    }

    public boolean isRequiresSameDayProcessing() {
        return requiresSameDayProcessing;
    }

    public boolean isServiceColumnEnabled() {
        return serviceColumnEnabled;
    }

    public boolean isServicesDeletable() {
        for (Service service : getServices()) {
            if (!service.isDeletableWithoutUpdatableCheck()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void processesPlatesChanged(ValueChangeEvent event) {
        if ((Boolean) event.getNewValue()) {
            setProcessesSamples(true);
        } else {
            setPlateSubmissionProposalLimit(null);
        }
    }

    public void processesSamplesChanged(ValueChangeEvent event) {
        if (!(Boolean) event.getNewValue()) {
            setProcessesPlates(false);
            setPlateSubmissionProposalLimit(null);
        }
    }

    @Override
    public void propagateEnabled() {
        for (Service service : getServices()) {
            service.setEnabled(isEnabled());
        }
    }

    public void setCoach(User coach) {
        this.coach = coach;
    }

    public void setCoachBackup(User coachBackup) {
        this.coachBackup = coachBackup;
    }

    public void setInitialCustomStatus(boolean initialCustomStatus) {
        this.initialCustomStatus = initialCustomStatus;
    }

    public void setInstructionLink(String instructionLink) {
        this.instructionLink = StringHelper.format(instructionLink);
    }

    public void setInstruments(Set<Instrument> instruments) {
        this.instruments = instruments;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public void setMultiplexKits(Set<MultiplexKit> multiplexKits) {
        this.multiplexKits = multiplexKits;
    }

    public void setOrderAttributes(Set<OrderAttribute> orderAttributes) {
        this.orderAttributes = orderAttributes;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setPlateSubmissionProposalLimit(Integer plateSubmissionProposalLimit) {
        this.plateSubmissionProposalLimit = plateSubmissionProposalLimit;
    }

    public void setProcessesPlates(boolean processesPlates) {
        this.processesPlates = processesPlates;
    }

    public void setProcessesSamples(boolean processesSamples) {
        this.processesSamples = processesSamples;
        if (!processesSamples) {
            setServiceColumnEnabled(false);
            setRequiresSameDayProcessing(false);
        }
    }

    public void setRequiresProject(boolean requiresProject) {
        this.requiresProject = requiresProject;
    }

    public void setRequiresSameDayProcessing(boolean requiresSameDayProcessing) {
        this.requiresSameDayProcessing = requiresSameDayProcessing;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public void setServiceArea(final ServiceArea serviceArea) {
        this.serviceArea = serviceArea;
    }

    public void setServiceColumnEnabled(boolean serviceColumnEnabled) {
        this.serviceColumnEnabled = serviceColumnEnabled;
    }

    public void setServiceTypeCollections(Set<ServiceTypeCollection> serviceTypeCollections) {
        this.serviceTypeCollections = serviceTypeCollections;
    }

    public void setServices(Set<Service> services) {
        this.services = services;
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void setUsersAsList(List<User> users) {
        this.users = (Set<User>) CollectionHelper.asSet(users);
    }

    public void setWorkflowTemplates(Set<WorkflowTemplate> workflowTemplates) {
        this.workflowTemplates = workflowTemplates;
    }
}