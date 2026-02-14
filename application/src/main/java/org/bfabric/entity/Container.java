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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.Dashboard;
import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMap;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.manager.ContextManager;
import org.bfabric.service.ContainerService;
import org.bfabric.service.FeedbackTemplateService;
import org.bfabric.service.MembershipService;
import org.bfabric.service.PlateService;
import org.bfabric.service.RunService;
import org.bfabric.service.SampleService;
import org.bfabric.service.StorageService;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.DashboardHelper;
import org.bfabric.util.LocalDateInterval;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force = true)
@NamedQuery(name = "Container.findAll", query = "SELECT a FROM Container a ORDER BY a.id DESC")
@NamedQuery(name = "Container.findOrderIdsByContainerIds", query = "SELECT a.id FROM Container a WHERE a.project.id in (:containerIds)")
@NamedQuery(name = "Container.findIdByOldServiceOrderId", query = "SELECT a.id FROM Container a where a.oldServiceOrderId = :oldServiceOrderId")
@NamedQuery(name = "Container.findIdByOldProjectOrderId", query = "SELECT a.id FROM Container a where a.oldProjectOrderId = :oldProjectOrderId")
@NamedQuery(name = "Container.findByPlateId", query = "SELECT DISTINCT a.sample.container FROM SamplePlatePosition a WHERE a.plate.id = :plateId")
@NamedQuery(name = "Container.countByPlateId", query = "SELECT COUNT(DISTINCT a.sample.container) FROM SamplePlatePosition a WHERE a.plate.id = :plateId")
@NamedQuery(name = "Container.findByRunId", query = "SELECT DISTINCT sample.container FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE a.run.id = :runId")
@NamedQuery(name = "Container.countByRunId", query = "SELECT COUNT(DISTINCT sample.container) FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE a.run.id = :runId")
public abstract class Container extends AbstractNamedInstrumentReferencingEntity implements ShowScreen, Indexable, HasAffiliation, TechnologiesDependent, Dashboard {

    public static final String USER_DECISION_SUBMITTED = "User Decision Submitted";

    public static final String USER_DECISION_REQUIRED = "User Decision Required";

    public static final String WAITING_FOR_REPLACEMENT_SAMPLES = "Waiting for Replacement Samples";

    private static final long serialVersionUID = 1;

    @Transient
    protected Long commentsTotalSize;

    @Transient
    protected Long fullSize;

    @Transient
    protected Long resourcesTotalSize;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    protected boolean serviceColumnEnabled = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    protected boolean processesSamples = false;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    protected boolean requiresProject = true;

    @Transient
    protected Set<OrderAttribute> orderAttributesMapped;

    @Transient
    protected Boolean hasBeenAccepted;

    @OneToMany(mappedBy = "container")
    @OrderBy("created")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<ContainerStatus> allStates = new ArrayList<>();

    @Transient
    private Set<? extends Container> associatedContainers;

    @Transient
    private List<Dataset> associatedDatasets;

    @Transient
    private List<Plate> associatedPlates;

    @Column(updatable = false, insertable = false)
    private long billableChargeCount;

    @Embedded
    @XmlElement
    private BillingInfo billingInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bioinformaticianid")
    @XmlIDREF
    private User bioinformatician;

    @Transient
    private boolean bioinformaticianChanged;

    @Column(updatable = false, insertable = false)
    private long bookedChargeCount;

    @Transient
    private Set<User> bookingManagers;

    @OneToMany(mappedBy = "container")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Booking> bookings = new HashSet<>();

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal budgetLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budgetofficerid")
    @NotNull
    @XmlIDREF
    private User budgetOfficer;

    @OneToMany(mappedBy = "container")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @Transient
    private BigDecimal chargesSum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachid")
    @XmlIDREF
    private User coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachbackupid")
    @XmlIDREF
    private User coachBackup;

    @Transient
    private boolean coachBackupChanged;

    @Transient
    private boolean coachChanged;

    @Transient
    private Company company;

    @Transient
    private String companyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contactid")
    @NotNull
    @XmlIDREF
    private User contact;

    @NotEmpty
    @Size(max = 8)
    @XmlElement
    private String costCentre;

    @OneToMany(mappedBy = "container", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @Where(clause = "discriminator = 'C'")
    @OrderBy("created")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<CustomContainerStatus> customStates = new ArrayList<>();

    @NotEmpty
    @Size(max = 256)
    @XmlElement
    private String customStatus;

    @Transient
    private DashboardHelper dashboardHelper;

    @OneToMany(mappedBy = "container")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Dataset> datasets = new HashSet<>();

    @Transient
    private Long defaultFeedbackTemplateId;

    @Transient
    private Department department;

    @Column(insertable = false, updatable = false)
    private String discriminator;

    @ManyToMany
    @JoinTable(name = "containerdiscussedwith", joinColumns = @JoinColumn(name = "containerid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> discussedWith = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisionid")
    @XmlIDREF
    private Division division;

    @Transient
    private String divisionName;

    @XmlElement
    private LocalDate doiCreated;

    @Size(max = 32)
    @XmlElement
    private String doiCreatedBy;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private Boolean euGrant;

    @Column(columnDefinition = "boolean DEFAULT false")
    @XmlElement
    private boolean express;

    @Column(updatable = false, insertable = false)
    private int failedWorkunitsCount;

    @OneToMany(mappedBy = "container")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Feedback> feedbacks = new HashSet<>();

    @Column(updatable = false, insertable = false)
    private String filterValue;

    @XmlElement
    @NotNull
    private Boolean financeSourceEth;

    @OneToMany(mappedBy = "container")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ImportResource> importResources = new HashSet<>();

    @Transient
    private Long importResourcesTotalSize;

    @Transient
    private boolean includeOrderData = true;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean initialCustomStatus = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituteid")
    @XmlIDREF
    private Institute institute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentreadconfigurationid")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private InstrumentReadConfiguration instrumentReadConfiguration;

    @ManyToMany
    @JoinTable(name = "instrumentreservationcontainer", joinColumns = @JoinColumn(name = "containerid"), inverseJoinColumns = @JoinColumn(name = "instrumentreservationid"))
    @OrderBy("startDate desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReservation> instrumentReservations = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @XmlElement
    private boolean internal;

    @Transient
    private Boolean isRenderedBookings;

    @Transient
    private Boolean isRenderedCharges;

    @Transient
    private Boolean isRenderedFeedbacks;

    @Transient
    private Boolean isRenderedOrders;

    @Transient
    private Boolean isRenderedReviews;

    @Transient
    private Boolean isRenderedTree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leaderid")
    @XmlIDREF
    private User leader;

    @OneToMany(mappedBy = "container", cascade = { CascadeType.REMOVE })
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Membership> memberships = new HashSet<>();

    @OneToMany(mappedBy = "container")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "discriminator = '" + Membership.DISCRIMINATOR_CURRENT + "'")
    private Set<Membership> membershipsCurrent = new HashSet<>();

    @OneToMany(mappedBy = "container")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "discriminator = '" + Membership.DISCRIMINATOR_FORMER + "'")
    private Set<Membership> membershipsFormer = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "offercontainer", joinColumns = @JoinColumn(name = "containerid"), inverseJoinColumns = @JoinColumn(name = "offerid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Offer> offers = new HashSet<>();

    @Transient
    private User oldBudgetOfficer;

    @Transient
    private User oldContact;

    private Long oldFlatRatedOrderId;

    @Transient
    private User oldLeader;

    private Long oldProjectOrderId;

    @Transient
    private User oldRequester;

    private Long oldServiceOrderId;

    @ManyToMany
    @JoinTable(name = "orderattributeorder", joinColumns = @JoinColumn(name = "orderid"), inverseJoinColumns = @JoinColumn(name = "orderattributeid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrderAttribute> orderAttributes = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @XmlElement
    private boolean orderDataOnly;

    @OneToMany(mappedBy = "project")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @Transient
    private Organization organization;

    @Transient
    private OrganizationType organizationType;

    @Min(1)
    @XmlElement
    private Integer plateSubmissionProposalLimit;

    @OneToMany(mappedBy = "container")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Plate> plates = new HashSet<>();

    @Transient
    private Long platesCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectid")
    @XmlIDREF
    private Project project;

    @Size(max = 16)
    @NotEmpty
    @XmlElement
    private String pspElement;

    @Column(columnDefinition = "TEXT")
    @XmlElement
    private String remarks;

    @Transient
    @NotNull
    private Boolean renderedCostCentre;

    @Transient
    private boolean replaceBioinformatician = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesterid")
    @NotNull
    @XmlIDREF
    private User requester;

    @OneToMany(mappedBy = "container")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Resource> resources = new HashSet<>();

    @Transient
    private boolean reviewRequired;

    @Transient
    private List<Run> runs;

    @Transient
    private Long runsCount;

    @Transient
    private List<Sample> sampleReplacements;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampletypeid")
    @XmlIDREF
    private SampleType sampleType;

    @OneToMany(mappedBy = "container")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Sample> samples = new HashSet<>();

    @Transient
    private List<Sample> samplesUserDecisionRequired;

    @Transient
    private String selectedCustomStatus;

    @Transient
    private SampleTypeEnum selectedSampleType;

    @Transient
    private StatusEnum selectedStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicetypeid")
    @XmlIDREF
    private ServiceType serviceType;

    @Transient
    private Set<Service> services;

    @Transient
    private boolean showEula = false;

    @Transient
    private boolean showFormerMembers;

    @OneToMany(mappedBy = "container", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @Where(clause = "discriminator = 'S'")
    @OrderBy("created")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<StandardContainerStatus> states = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private StatusEnum status = StatusEnum.PENDING;

    @NotNull
    private LocalDateTime statusModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusmodifiedbyid")
    @NotNull
    private User statusModifiedBy;

    @Transient
    private List<Object> storageInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storageModelId")
    @XmlIDREF
    private StorageModel storageModel;

    @Column(columnDefinition = "TEXT")
    @XmlElement
    private String summary;

    @Transient
    private Set<User> supporters;

    @NotEmpty
    @ManyToMany
    @JoinTable(name = "containertechnology", joinColumns = @JoinColumn(name = "containerid"), inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technology")
    private String technologiesAsString;

    @Column(updatable = false, insertable = false)
    @XmlElement
    private BigDecimal totalPriceBillableCharges;

    @Column(updatable = false, insertable = false)
    @XmlElement
    private BigDecimal totalPriceBookedBillableCharges;

    @Column(updatable = false, insertable = false)
    @XmlElement
    private BigDecimal totalPriceNonBookedBillableCharges;

    @Transient
    private Boolean tracked;

    @ManyToMany
    @JoinTable(name = "usertrackedcontainer", joinColumns = @JoinColumn(name = "containerid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("lastName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "trackingUser")
    private Set<User> trackingUsers = new HashSet<>();

    @OneToMany(mappedBy = "container")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workflow> workflows = new HashSet<>();

    @OneToMany(mappedBy = "container", cascade = { CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> workunits = new HashSet<>();

    public Container() {
    }

    public void acceptEula() {
        setShowEula(false);
    }

    public void bioinformaticianChangedListener(ValueChangeEvent event) {
        setBioinformaticianChanged(!(getBioinformatician() == null && event.getNewValue() == null || getBioinformatician() != null && getBioinformatician()
            .equals(event.getNewValue())));
    }

    public void budgetOfficerChangedListener(ValueChangeEvent event) {
        final User newBudgetOfficer = (User) event.getNewValue();
        final User oldSelectedBudgetOfficer = (User) event.getOldValue();
        if (getOldBudgetOfficer() == null) {
            setOldBudgetOfficer(oldSelectedBudgetOfficer);
        }
        if (newBudgetOfficer != null && (oldSelectedBudgetOfficer == null || !oldSelectedBudgetOfficer.equals(newBudgetOfficer))) {
            setBudgetOfficerAndBillingData(newBudgetOfficer);
        }
    }

    public Set<Mail> changeCustomStatus(String aCustomStatus) {
        Set<Mail> mails = new HashSet<>();
        setCreateAndAddCustomStatus(aCustomStatus);
        if (isSendMail()) {
            Mail mail = createMailCustomStateTransition(aCustomStatus);
            if (mail != null) {
                mails.add(mail);
            }
        }
        return mails;
    }

    public Set<Mail> changeStatus(StatusEnum statusEnum) {
        return null;
    }

    @Override
    public void clearButtonOrganization() {
        HasAffiliation.super.clearButtonOrganization();
        getBillingInfo().setVatNumber(null);
        getBillingInfo().setReferenceNumber(null);
    }

    public void coachBackupChangedListener(ValueChangeEvent event) {
        setCoachBackupChanged(!(getCoachBackup() == null && event.getNewValue() == null || getCoachBackup() != null && getCoachBackup().equals(event
            .getNewValue())));
    }

    public void coachChangedListener(ValueChangeEvent event) {
        setCoachChanged(!(getCoach() == null && event.getNewValue() == null || getCoach() != null && getCoach().equals(event.getNewValue())));
    }

    @Override
    public void companyChanged(ValueChangeEvent event) {
        HasAffiliation.super.companyChanged(event);
        resetVatAndReferenceNumber();
        setVatNumberOfCompany((Company) event.getNewValue());
    }

    public void contactChangedListener(ValueChangeEvent event) {
        if (getOldContact() == null) {
            setOldContact((User) event.getOldValue());
        }
    }

    public List<String> createFacesMessageForChangedManager() {
        List<String> facesMessages = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        Set<User> oldManagers = new HashSet<>();
        if (isContactChanged()) {
            msg.append(Messages.get("contact")).append(" (").append(getOldContact().getName()).append(") ");
            oldManagers.add(getOldContact());
        }
        if (isRequesterChanged()) {
            msg.append(Messages.get("requester")).append(" (").append(getOldRequester().getName()).append(") ");
            oldManagers.add(getOldRequester());
        }
        if (isBudgetOfficerChanged()) {
            msg.append(Messages.get("budgetOfficer")).append(" (").append(getOldBudgetOfficer().getName()).append(") ");
            oldManagers.add(getOldBudgetOfficer());
        }
        if (isLeaderChanged()) {
            msg.append(Messages.get("leader")).append(" (").append(getOldLeader().getName()).append(") ");
            oldManagers.add(getOldLeader());
        }
        if (!msg.toString().isEmpty()) {
            msg.insert(0, Messages.get("replaced") + ": ");
            facesMessages.add(msg.toString());
        }
        if (!oldManagers.isEmpty()) {
            msg.setLength(0);
            for (final User user : oldManagers) {
                if (!getContainer().hasSpecificFunction(user, true)) {
                    msg.append(" ").append(user.getName());
                }
            }
            if (!msg.toString().isEmpty()) {
                msg.insert(0, Messages.get("removedMembers") + ": ");
                facesMessages.add(msg.toString());
            }
        }
        return facesMessages;
    }

    public Mail createMail(MailTypeEnum mailTypeEnum) {
        return createMail(mailTypeEnum, null);
    }

    public Mail createMail(MailTypeEnum mailTypeEnum, User member) {
        Mail mail = new Mail();
        mail.setParent(this);
        mail.setType(mailTypeEnum, getClassLabelId());
        switch (mail.getType()) {
        case CONTAINER_REQUEST:
            mail.addRecipient(getRequester());
            mail.addRecipient(getBudgetOfficer());
            mail.addRecipient(getContact());
            mail.addRecipient(getLeader());
            break;
        case CONTAINER_REJECT:
        case CONTAINER_REQUEST_COORDINATOR:
            mail.addRecipients(CDI.current().select(UserService.class).get().getReviewManagers());
            break;
        case CONTAINER_APPROVE:
        case CONTAINER_FINISH:
        case CONTAINER_PRIVATE:
        case DOI_REQUESTED:
            mail.addRecipients(getMembers());
            break;
        case CONTAINER_APPROVE_COACH:
        case CONTAINER_REJECT_COACH:
            mail.addRecipient(getCoach());
            mail.addRecipient(getCoachBackup());
            break;
        case CONTAINER_COACH_CHANGED:
            mail.setInput("coachChanged", isCoachBackupChanged());
            mail.setInput("coachBackupChanged", isCoachBackupChanged());
            mail.setInput("bioinformaticianChanged", isBioinformaticianChanged());
            if (getCoach() != null && !getCoach().equals(getCurrentUser())) {
                mail.addRecipientIfEmployee(getCoach());
            }
            if (getCoachBackup() != null && !getCoachBackup().equals(getCurrentUser())) {
                mail.addRecipientIfEmployee(getCoachBackup());
            }
            if (getBioinformatician() != null && !getBioinformatician().equals(getCurrentUser())) {
                mail.addRecipientIfEmployee(getBioinformatician());
            }
            mail.setInput("entity", this);
            mail.setInput("entityLabel", getClassLabelLowerCase());
            mail.setInput("entityDisplayLabel", getClassLabel());
            break;
        case MEMBER_ADD:
        case MEMBER_REMOVE:
        case MEMBER_ROLE_DOWNGRADE:
        case MEMBER_ROLE_UPGRADE:
            if (isManaged()) {
                mail.addRecipient(member);
                mail.setCachedUser(member);
            }
            break;
        case CONTAINER_ARRIVED:
        case CONTAINER_SUBMITTED:
        case CONTAINER_USER_DECISION_SUBMITTED:
            if (mail.getType().equals(MailTypeEnum.CONTAINER_ARRIVED)) {
                mail.addRecipient(getRequester());
            }
            mail.addRecipientIfEmployee(getCoach());
            mail.addRecipientIfEmployee(getCoachBackup());
            if (getProject() != null) {
                mail.addRecipientIfEmployee(getProject().getCoach());
                mail.addRecipientIfEmployee(getProject().getCoachBackup());
            }
            if (getServiceType() != null) {
                mail.addRecipientIfEmployee(getServiceType().getCoach());
                mail.addRecipientIfEmployee(getServiceType().getCoachBackup());
                mail.addRecipientsIfEmployee(getServiceType().getUsers());
                mail.addRecipientsIfEmployee(getServiceType().getServiceArea().getUsers());
            }
            mail.addRecipientsIfEmployee(getServicesUsers());
            break;
        case CONTAINER_ACCEPTED:
        case CONTAINER_FINISHED:
        case CONTAINER_CANCELED:
            mail.addRecipient(getRequester());
            break;
        case CONTAINER_BOOKING_REQUEST:
            mail.addRecipientsIfEmployee(getBookingManagers());
            break;
        case CONTAINER_COACH_ALTER:
            mail.addRecipient(getContact());
            mail.setCachedUser(getCoach());
            break;
        case CONTAINER_REQUESTER_ALTER:
            mail.addRecipient(getContact());
            mail.setCachedUser(getRequester());
            break;
        case CONTAINER_BUDGETOFFICER_ALTER:
            mail.addRecipient(getContact());
            mail.setCachedUser(getBudgetOfficer());
            break;
        case CONTAINER_COACH:
            mail.addRecipientIfEmployee(getCoach());
            mail.addRecipientIfEmployee(getCoachBackup());
            break;
        case CONTAINER_LEADER_ALTER:
            mail.addRecipient(getContact());
            mail.setCachedUser(getLeader());
            break;
        case CONTAINER_CONTACT_ALTER:
            mail.addRecipient(getContact());
            mail.setCachedUser(getContact());
            break;
        case CONTAINER_REPORT_APPROVE:
            mail.addRecipient(getContact());
            break;
        case CONTAINER_REPORT_UPLOAD:
            mail.addRecipientIfEmployee(getCoach());
            break;
        case MEMBER_ADD_CONTACT:
        case MEMBER_REMOVE_CONTACT:
        case MEMBER_ROLE_DOWNGRADE_CONTACT:
        case MEMBER_ROLE_UPGRADE_CONTACT:
            if (isManaged()) {
                mail.addRecipient(getContact());
                mail.setCachedUser(member);
            }
            break;
        default:
            break;
        }
        if (!mail.getRecipients().isEmpty()) {
            mail.setInput("container", this);
            mail.setInput("containerLabel", getClassLabelLowerCase());
            mail.setInput("containerLink", getClassLabelLowerCase());
            mail.setInput(getClassLabelLowerCase(), this);
            if (getProject() != null) {
                mail.setInput("project", getProject());
            }
        }
        return mail;
    }

    public Mail createMailCustomStateTransition(String aCustomStatus) {
        if (aCustomStatus != null) {
            Mail mail = new Mail();
            mail.setParent(this);
            mail.setType(MailTypeEnum.CONTAINER_CUSTOM_STATE, getClassLabel() + " " + getId());
            mail.addRecipient(getRequester());
            if (!isContainerProject() && aCustomStatus.equals(Constants.CUSTOM_ORDER_STATE_SEQUENCING_DONE)) {
                mail.addRecipient(getCoach());
                mail.addRecipient(getCoachBackup());
            }
            mail.setInput("state", aCustomStatus);
            mail.setInput("container", this);
            mail.setInput("containerLabel", getClassLabelLowerCase());
            return mail;
        }
        return null;
    }

    @Override
    public void departmentChanged(ValueChangeEvent event) {
        Organization oldOrganization = getOrganization();
        HasAffiliation.super.departmentChanged(event);
        if (oldOrganization == null || !oldOrganization.equals(getOrganization())) {
            resetVatAndReferenceNumber();
            setVatNumberOfOrganization(getOrganization());
        }
    }

    public void exportAndDownloadIcs(Set<InstrumentReservation> instrumentReservations) {
        Event event = new Event();
        Set<InstrumentReservation> exportInstrumentReservations = new HashSet<>();
        if (instrumentReservations != null && !instrumentReservations.isEmpty()) {
            exportInstrumentReservations.addAll(instrumentReservations);
        } else {
            exportInstrumentReservations.addAll(getInstrumentReservations());
        }
        event.download(getClassLabelLowerCaseId() + "_instrumentreservations.ics", event.getIcsExport(exportInstrumentReservations).toString());
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        if (getBillingOrganizationType() != null && !getBillingOrganizationType().isFinanceSourceRequired()) {
            setEuGrant(null);
            setFinanceSourceEth(null);
            setCostCentre(null);
            setPspElement(null);
        }
        if (getCoach() != null && getCoachBackup() == null && getCoach().isBackupValid()) {
            setCoachBackup(getCoach().getBackup());
        }
    }

    public List<ContainerStatus> getAllStates() {
        return allStates;
    }

    public Set<? extends Container> getAssociatedContainers() {
        return associatedContainers;
    }

    public List<Dataset> getAssociatedDatasets() {
        if (associatedDatasets == null) {
            associatedDatasets = getDatasetsTransitive();
        }
        return associatedDatasets;
    }

    public List<Plate> getAssociatedPlates() {
        if (associatedPlates == null) {
            associatedPlates = CDI.current().select(PlateService.class).get().getPlatesByContainerId(getId());
        }
        return associatedPlates;
    }

    public long getBillableChargeCount() {
        return billableChargeCount;
    }

    public String getBillingAccountDisplay() {
        if (isRenderedFinanceSource()) {
            if (getCostCentre() != null) {
                return getCostCentreDisplay();
            } else if (getPspElement() != null) {
                return getPspElementDisplay();
            }
        }
        return null;
    }

    public BillingInfo getBillingInfo() {
        return billingInfo;
    }

    public OrganizationType getBillingOrganizationType() {
        if (getInstitute() != null && getInstitute().getDepartment() != null && getInstitute().getDepartment().getOrganization() != null) {
            return getInstitute().getDepartment().getOrganization().getOrganizationTypeForBilling();
        } else if (getDivision() != null && getDivision().getCompany() != null) {
            return getDivision().getCompany().getOrganizationType();
        }
        return null;
    }

    public User getBioinformatician() {
        return bioinformatician;
    }

    public long getBookedChargeCount() {
        return bookedChargeCount;
    }

    public Set<User> getBookingManagers() {
        if (bookingManagers == null) {
            return bookingManagers = CDI.current().select(UserService.class).get().getUsersByRoleEnum(RoleEnum.BOOKINGMANAGER);
        }
        return bookingManagers;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public User getBudgetOfficer() {
        return budgetOfficer;
    }

    public BigDecimal getBudgetRemaining() {
        return getBudgetLimit() != null && getChargesSum() != null ? getBudgetLimit().subtract(getChargesSum()) : getBudgetLimit();
    }

    public String getBudgetWarning() {
        if (budgetLimit == null) {
            return "";
        }
        BigDecimal remaining = getBudgetRemaining();
        if (remaining.compareTo(budgetLimit.multiply(new BigDecimal("0.05"))) < 0) {
            return "warning";
        }
        if (remaining.compareTo(budgetLimit.multiply(new BigDecimal("0.2"))) < 0) {
            return "warn";
        }
        return "available";
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public BigDecimal getChargesSum() {
        if (chargesSum == null && !getCharges().isEmpty()) {
            double sum = 0.0;
            for (Charge charge : getCharges()) {
                sum += charge.getDiscountedPrice().doubleValue();
            }
            chargesSum = BigDecimal.valueOf(sum);
        }
        return chargesSum;
    }

    public User getCoach() {
        return coach;
    }

    public User getCoachBackup() {
        return coachBackup;
    }

    public abstract Set<? extends Comment> getComments();

    public String getCommentsPrintSize() {
        return NumberUtils.getPrintSize(getCommentsTotalSize());
    }

    public abstract Long getCommentsTotalSize();

    public Long getCommentsTotalSize(List<Comment> comments) {
        if (commentsTotalSize == null) {
            long size = 0;
            for (Comment comment : comments) {
                size += comment.getSize();
            }
            commentsTotalSize = size;
        }
        return commentsTotalSize;
    }

    @Override
    public Company getCompany() {
        return company;
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    public User getContact() {
        return contact;
    }

    public Container getContainer() {
        return getProject() != null ? getProject() : this;
    }

    public String getCostCentre() {
        return costCentre;
    }

    public String getCostCentreDisplay() {
        return getFinanceSourceEthDisplay() + " " + Messages.get("costCentre") + " " + getCostCentre();
    }

    public List<CustomContainerStatus> getCustomStates() {
        return customStates;
    }

    public String getCustomStatus() {
        return customStatus;
    }

    public DashboardHelper getDashboardHelper() {
        if (dashboardHelper == null) {
            dashboardHelper = new DashboardHelper(this);
        }
        return dashboardHelper;
    }

    public Set<Dataset> getDatasets() {
        return datasets;
    }

    public List<Dataset> getDatasetsTransitive() {
        Set<Dataset> datasetsTransitive = new HashSet<>();
        if (!getDatasets().isEmpty()) {
            datasetsTransitive.addAll(getDatasets());
        }
        if (!getOrders().isEmpty()) {
            for (Order order : getOrders()) {
                if (!order.getDatasets().isEmpty()) {
                    datasetsTransitive.addAll(order.getDatasets());
                }
            }
        }
        return CollectionHelper.sortObjects(datasetsTransitive);
    }

    public Long getDefaultFeedbackTemplateId() {
        if (defaultFeedbackTemplateId == null) {
            defaultFeedbackTemplateId = CDI.current().select(FeedbackTemplateService.class).get().getDefaultFeedbackTemplateId(getClassName());
        }
        return defaultFeedbackTemplateId;
    }

    public Set<Sample> getDeletableSamples() {
        Set<Sample> deletableSamples = new HashSet<>();
        for (Sample sample : getSamples()) {
            if (sample.isDeletable()) {
                deletableSamples.add(sample);
            }
        }
        return deletableSamples;
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    public abstract BigDecimal getDiscount();

    public String getDiscriminator() {
        return discriminator;
    }

    public Set<User> getDiscussedWith() {
        return discussedWith;
    }

    @XmlElement(name = "discussedwith")
    public String getDiscussedWithAsString() {
        List<String> discussedWithAsString = new ArrayList<>();
        for (User user : getDiscussedWith()) {
            discussedWithAsString.add(user.getName());
        }
        return CollectionHelper.print(CollectionHelper.sortObjects(discussedWithAsString));
    }

    public String getDisplayEntityNameCoach() {
        return getEntityName() + " " + getDisplayName() + (getCoach() != null ? " (" + Messages.get("coach") + ": " + getCoach().getName() + ")" : Constants.EMPTY_STRING);
    }

    @Override
    public Division getDivision() {
        return division;
    }

    @Override
    public String getDivisionName() {
        return divisionName;
    }

    public String getDoi() {
        return getConfiguration().getDoiPrefix() + getId();
    }

    public LocalDate getDoiCreated() {
        return doiCreated;
    }

    public String getDoiCreatedBy() {
        return doiCreatedBy;
    }

    public List<User> getEmployeesExcludingSupportAndMembers(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesExcludingSupportAndMembers(filterString, this);
    }

    public List<User> getEmployeesIncludingBioinformatician(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getBioinformatician());
    }

    public List<User> getEmployeesIncludingCoach(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getCoach());
    }

    public List<User> getEmployeesIncludingCoachBackup(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getCoachBackup());
    }

    public String getEntityName() {
        return getClassLabel();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (isInternal()) {
            addEntityInfoItem(summary, "internal", isInternal());
        }
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus().getLabel());
        }
        if (getServiceType() != null) {
            addEntityInfoItem(summary, "serviceType", getServiceType().getName());
        }
        if (getSampleType() != null) {
            addEntityInfoItem(summary, "sampleType", getSampleType().getName());
        }
        if (getTechnologiesAsString() != null) {
            addEntityInfoItem(summary, "technologies", getTechnologiesAsString());
        }
        if (getProject() != null) {
            addEntityInfoItem(summary, "project", getProject().getId());
        }
        if (getRequester() != null) {
            addEntityInfoItem(summary, "requester", getRequester().getName());
        }
        if (getContact() != null) {
            addEntityInfoItem(summary, "contact", getContact().getName());
        }
        if (getBudgetOfficer() != null) {
            addEntityInfoItem(summary, "budgetOfficer", getBudgetOfficer().getName());
        }
        if (getLeader() != null) {
            addEntityInfoItem(summary, "leader", getLeader().getName());
        }
        if (getCoach() != null) {
            addEntityInfoItem(summary, "coach", getCoach().getName());
        }
        if (getCoachBackup() != null) {
            addEntityInfoItem(summary, "coachBackup", getCoachBackup().getName());
        }
        if (getBioinformatician() != null) {
            addEntityInfoItem(summary, "bioinformatician", getBioinformatician().getName());
        }
        if (getInstitute() != null) {
            addEntityInfoItem(summary, "institute", getInstitute().getFullName());
        }
        if (getDivision() != null && getDivision().getCompany() != null) {
            addEntityInfoItem(summary, "company", getDivision().getCompanyName());
        }
        if (getRemarks() != null) {
            addEntityInfoItem(summary, "remarks", getRemarks());
        }
        addEntityInfoItems(summary, getCustomAttributes());
        if (getOldProjectOrderId() != null) {
            addEntityInfoItem(summary, "oldProjectOrderId", getOldProjectOrderId());
        }
        if (getOldServiceOrderId() != null) {
            addEntityInfoItem(summary, "oldServiceOrderId", getOldServiceOrderId());
        }
        return summary.toString();
    }

    public Boolean getEuGrant() {
        return euGrant;
    }

    public LocalDateInterval getExecutionPeriod() {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (!isContainerProject()) {
            boolean arrived = false;
            for (StandardContainerStatus state : getStates()) {
                if (start == null && (StatusEnum.RUNNING.getLabel().equals(state.getName()) || StatusEnum.ACCEPTED.getLabel().equals(state.getName()))) {
                    start = state.getCreated();
                }
                if (!arrived && StatusEnum.ARRIVED.getLabel().equals(state.getName())) {
                    start = state.getCreated();
                    arrived = true;
                }
                if (end == null && (isContainerProject() ? StatusEnum.PRIVATE.getLabel().equals(state.getName()) || StatusEnum.PUBLISHED.getLabel()
                    .equals(state.getName()) : StatusEnum.FINISHED.getLabel().equals(state.getName()))) {
                    end = state.getCreated();
                }
            }
        }
        return start != null ? new LocalDateInterval(start.toLocalDate(), end != null ? end.toLocalDate() : null) : null;
    }

    public String getExecutionPeriodString() {
        return new LocalDateInterval(getExecutionPeriod().getStart(), getExecutionPeriod().getEnd()).getIntervalAsString();
    }

    public int getFailedWorkunitsCount() {
        return failedWorkunitsCount;
    }

    public Set<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public Boolean getFinanceSourceEth() {
        return financeSourceEth;
    }

    public String getFinanceSourceEthDisplay() {
        StringBuilder ret = new StringBuilder();
        if (getFinanceSourceEth() != null) {
            if (getFinanceSourceEth()) {
                ret.append("ETHZ ");
            } else {
                ret.append("UZH ");
            }
        }
        return ret.toString();
    }

    public Long getFullSize(boolean includeOrderData) {
        if (fullSize == null) {
            fullSize = 0L;
            Long containerAttachmentAndResourcesSize = CDI.current().select(ContainerService.class).get().getAttachmentAndResourcesSizeByContainer(this);
            if (containerAttachmentAndResourcesSize != null) {
                fullSize += containerAttachmentAndResourcesSize;
            }

            for (Workflow workflow : getWorkflows()) {
                for (WorkflowStep workflowStep : workflow.getWorkflowSteps()) {
                    for (WorkflowStepComment workflowStepComment : workflowStep.getComments()) {
                        fullSize += workflowStepComment.getSize();
                    }
                }
            }
        }
        return fullSize;
    }

    public Set<ImportResource> getImportResources() {
        return importResources;
    }

    public Long getImportResourcesTotalSize() {
        if (importResourcesTotalSize == null) {
            importResourcesTotalSize = CDI.current().select(ContainerService.class).get().getImportResourcesTotalSizeByContainer(this);
        }
        return importResourcesTotalSize;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.STATUS.getField());
        fields.add(IndexMapContentEnum.TECHNOLOGY.getField());
        if (isContainerProject()) {
            fields.add(IndexMapContentEnum.STARTDATE.getField());
            fields.add(IndexMapContentEnum.ENDDATE.getField());
        } else {
            fields.add(IndexMapContentEnum.SAMPLETYPE.getField());
            fields.add(IndexMapContentEnum.SERVICETYPE.getField());
            fields.add(IndexMapContentEnum.SERVICEAREA.getField());
            fields.add(IndexMapContentEnum.PROJECTID.getField());
            fields.add(IndexMapContentEnum.STORAGEMODEL.getField());
            fields.add(IndexMapContentEnum.OFFERID.getField());
            fields.add(IndexMapContentEnum.ITEMS.getField());
            fields.add(IndexMapContentEnum.CHARGEABLEITEMS.getField());
            fields.add(IndexMapContentEnum.CHARGEDITEMS.getField());
        }

        fields.add(IndexMapContentEnum.BOOKINGS.getField());
        fields.add(IndexMapContentEnum.CHARGES.getField());
        fields.add(IndexMapContentEnum.CHARGESSUM.getField());
        fields.add(IndexMapContentEnum.CHARGESBILLABLE.getField());
        fields.add(IndexMapContentEnum.CHARGESBOOKED.getField());
        fields.add(IndexMapContentEnum.SIZE.getField());
        fields.add(IndexMapContentEnum.IMPORTRESOUCESSIZE.getField());

        fields.add(IndexMapContentEnum.COACH.getField());
        fields.add(IndexMapContentEnum.COACHBACKUP.getField());
        fields.add(IndexMapContentEnum.BIOINFORMATICIAN.getField());

        fields.add(IndexMapContentEnum.CONTACT.getField());
        fields.add(IndexMapContentEnum.BUDGETOFFICER.getField());
        fields.add(IndexMapContentEnum.REQUESTER.getField());
        if (isContainerProject()) {
            fields.add(IndexMapContentEnum.LEADER.getField());
        }

        fields.add(IndexMapContentEnum.ORGANIZATION.getField());
        fields.add(IndexMapContentEnum.DEPARTMENT.getField());
        fields.add(IndexMapContentEnum.INSTITUTE.getField());
        fields.add(IndexMapContentEnum.COMPANY.getField());

        fields.add(IndexMapContentEnum.BILLINGCUSTOMER.getField());
        fields.add(IndexMapContentEnum.BILLINGADDRESS.getField());
        fields.add(IndexMapContentEnum.BILLINGEMAIL.getField());
        fields.add(IndexMapContentEnum.VATNUMBER.getField());
        fields.add(IndexMapContentEnum.REFERENCENUMBER.getField());

        fields.add(IndexMapContentEnum.SUMMARY.getField());

        for (CustomAttribute customAttribute : getCustomAttributes()) {
            fields.add(customAttribute.getName());
        }

        return fields;
    }

    @Override
    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = super.getIndexMap();
        indexMap.put(Constants.INDEXMAP_STATUS, getStatus().getLabel());
        indexMap.put(Constants.INDEXMAP_GROUP, getMemberRoleName());
        indexMap.put(Constants.INDEXMAP_DOI_CREATED, getDoiCreated());
        return indexMap;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getName());
        content.add(IndexMapContentEnum.NAME, getId());

        content.add(IndexMapContentEnum.STATUS, getStatus().getLabel());

        content.add(IndexMapContentEnum.SIZE, getResourcesTotalSize(), NumberUtils.getPrintSize(getResourcesTotalSize(), Constants.SIZE_GB));
        content.add(IndexMapContentEnum.IMPORTRESOUCESSIZE, getImportResourcesTotalSize(), NumberUtils.getPrintSize(getImportResourcesTotalSize(), Constants.SIZE_GB));

        content.add(IndexMapContentEnum.BOOKINGS, getBookings().size());
        content.add(IndexMapContentEnum.CHARGES, getCharges().size());
        content.add(IndexMapContentEnum.CHARGESSUM, getChargesSum());
        content.add(IndexMapContentEnum.CHARGESBILLABLE, getBillableChargeCount());
        content.add(IndexMapContentEnum.CHARGESBOOKED, getBookedChargeCount());

        if (getContact() != null) {
            content.add(IndexMapContentEnum.CONTACT, getContact().getFullName());
            content.add(IndexMapContentEnum.CONTACT, getContact().getLogin());
        }
        if (getRequester() != null) {
            content.add(IndexMapContentEnum.REQUESTER, getRequester().getFullName());
            content.add(IndexMapContentEnum.REQUESTER, getRequester().getLogin());
        }
        if (getBudgetOfficer() != null) {
            content.add(IndexMapContentEnum.BUDGETOFFICER, getBudgetOfficer().getFullName());
            content.add(IndexMapContentEnum.BUDGETOFFICER, getBudgetOfficer().getLogin());
        }
        if (getLeader() != null) {
            content.add(IndexMapContentEnum.LEADER, getLeader().getFullName());
            content.add(IndexMapContentEnum.LEADER, getLeader().getLogin());
        }

        if (getCoach() != null) {
            content.add(IndexMapContentEnum.COACH, getCoach().getLastNameFirstName());
            content.add(IndexMapContentEnum.COACH, getCoach().getLogin());
        }
        if (getCoachBackup() != null) {
            content.add(IndexMapContentEnum.COACHBACKUP, getCoachBackup().getLastNameFirstName());
            content.add(IndexMapContentEnum.COACHBACKUP, getCoachBackup().getLogin());
        }
        if (getBioinformatician() != null) {
            content.add(IndexMapContentEnum.BIOINFORMATICIAN, getBioinformatician().getLastNameFirstName());
            content.add(IndexMapContentEnum.BIOINFORMATICIAN, getBioinformatician().getLogin());
        }

        if (getInstitute() != null) {
            content.add(IndexMapContentEnum.INSTITUTE, getInstitute().getName());
            content.add(IndexMapContentEnum.DEPARTMENT, getInstitute().getDepartmentName());
            content.add(IndexMapContentEnum.ORGANIZATION, getInstitute().getOrganizationName());
        }
        if (getDivision() != null) {
            content.add(IndexMapContentEnum.COMPANY, getDivision().getCompanyName());
            if (getDivision().isSet()) {
                content.add(IndexMapContentEnum.DIVISION, getDivision().getName());
            }
        }

        if (getBillingInfo() != null) {
            content.add(IndexMapContentEnum.BILLINGCUSTOMER, getBillingInfo().getBillingCustomerName());
            content.add(IndexMapContentEnum.BILLINGADDRESS, getBillingInfo().getBillingAddressFull());
            content.add(IndexMapContentEnum.BILLINGEMAIL, getBillingInfo().getBillingEmail());
            content.add(IndexMapContentEnum.VATNUMBER, getBillingInfo().getVatNumber());
            content.add(IndexMapContentEnum.REFERENCENUMBER, getBillingInfo().getReferenceNumber());
        }

        if (StringHelper.isNotEmpty(getTechnologiesAsString())) {
            content.add(IndexMapContentEnum.TECHNOLOGY, getTechnologiesAsString());
        }

        if (getStorageModel() != null) {
            content.add(IndexMapContentEnum.STORAGEMODEL, getStorageModel().getName());
        }

        for (CustomAttribute customAttribute : getCustomAttributes()) {
            content.put(customAttribute.getName(), customAttribute.getValue());
        }

        return content;
    }

    @Override
    public Institute getInstitute() {
        return institute;
    }

    public InstrumentReadConfiguration getInstrumentReadConfiguration() {
        return instrumentReadConfiguration;
    }

    public Set<InstrumentReservation> getInstrumentReservations() {
        return instrumentReservations;
    }

    public String getInternalMemberNames() {
        return CollectionHelper.print(getInternalMembers(), "getFullName");
    }

    public Set<User> getInternalMembers() {
        Set<User> internalMembers = new HashSet<>();
        for (User user : getMembers()) {
            if (user.hasRoleEmployee()) {
                internalMembers.add(user);
            }
        }
        return internalMembers;
    }

    public Set<User> getInternalMembersSorted() {
        Set<User> internalMembers = new HashSet<>();
        for (User user : getMembersSorted()) {
            if (user.hasRoleEmployee()) {
                internalMembers.add(user);
            }
        }
        return internalMembers;
    }

    public Set<User> getInternalMembersTransitive() {
        Set<User> internalMembers = new HashSet<>();
        for (User user : getMembersTransitive()) {
            if (user.hasRoleEmployee()) {
                internalMembers.add(user);
            }
        }
        return internalMembers;
    }

    public Set<User> getInternalMembersTransitiveSorted() {
        Set<User> internalMembers = new HashSet<>();
        for (User user : getMembersTransitiveSorted()) {
            if (user.hasRoleEmployee()) {
                internalMembers.add(user);
            }
        }
        return internalMembers;
    }

    public CustomContainerStatus getLastCustomState() {
        return !getCustomStates().isEmpty() ? getCustomStates().get(getCustomStates().size() - 1) : null;
    }

    public ContainerStatus getLastState() {
        return getAllStates().isEmpty() ? null : getAllStates().get(getAllStates().size() - 1);
    }

    public User getLeader() {
        return leader;
    }

    public String getManagerRoleName() {
        return getManagerRoleName(getContainer().getId());
    }

    public String getManagerRoleName(@NotNull Long containerId) {
        return RoleEnum.CONTAINERMANAGER.getName() + containerId;
    }

    public String getMemberNames() {
        return CollectionHelper.print(getMembers(), "getFullName");
    }

    public String getMemberRoleName(@NotNull Long containerId) {
        return RoleEnum.CONTAINERMEMBER.getName() + containerId;
    }

    public String getMemberRoleName() {
        return getMemberRoleName(getContainer().getId());
    }

    public String getMemberSalutation() {
        return "Member of " + getClassLabel() + " " + getId();
    }

    public Set<User> getMembers() {
        return getMembers(getMembershipsCurrent());
    }

    public Set<User> getMembers(Set<Membership> membershipSet) {
        Set<User> members = new HashSet<>();
        if (membershipSet != null) {
            for (Membership membership : membershipSet) {
                members.add(membership.getUser());
            }
        }
        return members;
    }

    public List<User> getMembersFormerSorted() {
        return getMembersSorted(Membership.DISCRIMINATOR_FORMER);
    }

    public List<User> getMembersFormerTransitiveSorted() {
        if (getProject() != null) {
            return getProject().getMembersFormerSorted();
        }
        return getMembersFormerSorted();
    }

    public List<User> getMembersSorted() {
        return getMembersSorted(Membership.DISCRIMINATOR_CURRENT);
    }

    public List<User> getMembersSorted(String discriminator) {
        List<User> members = new ArrayList<>();
        for (Membership membership : getMembershipsSorted(discriminator)) {
            members.add(membership.user);
        }
        return members;
    }

    public Set<User> getMembersTransitive() {
        if (getProject() != null) {
            return getProject().getMembers();
        }
        return getMembers();
    }

    public List<User> getMembersTransitiveSorted() {
        if (getProject() != null) {
            return getProject().getMembersSorted();
        }
        return getMembersSorted();
    }

    public Set<Membership> getMemberships() {
        return memberships;
    }

    public Set<Membership> getMembershipsCurrent() {
        return membershipsCurrent;
    }

    public Set<Membership> getMembershipsFormer() {
        return membershipsFormer;
    }

    public List<Membership> getMembershipsSorted(String discriminator) {
        return CDI.current().select(MembershipService.class).get().getMembershipsByContainerAndDiscriminatorOrderByUser(this, discriminator);
    }

    public Set<Charge> getNonBookedCharges() {
        Set<Charge> nonBookedCharges = new HashSet<>();
        for (Charge charge : getCharges()) {
            if (!charge.isBooked() && charge.isBillable()) {
                nonBookedCharges.add(charge);
            }
        }
        return nonBookedCharges;
    }

    public Set<Offer> getOffers() {
        return offers;
    }

    public List<Offer> getOffersAsList() {
        return CollectionHelper.asList(getOffers());
    }

    public User getOldBudgetOfficer() {
        return oldBudgetOfficer;
    }

    public User getOldContact() {
        return oldContact;
    }

    public Long getOldFlatRatedOrderId() {
        return oldFlatRatedOrderId;
    }

    public String getOldId() {
        if (getOldProjectOrderId() != null) {
            return StringHelper.embraceParentheses(Messages.get("oldProjectOrderId") + " " + getOldProjectOrderId());
        } else if (getOldServiceOrderId() != null) {
            return StringHelper.embraceParentheses(Messages.get("oldServiceOrderId") + " " + getOldServiceOrderId());
        } else if (getOldFlatRatedOrderId() != null) {
            return StringHelper.embraceParentheses(Messages.get("oldFlatRatedOrderId") + " " + getOldFlatRatedOrderId());
        }
        return null;
    }

    public User getOldLeader() {
        return oldLeader;
    }

    public Long getOldProjectOrderId() {
        return oldProjectOrderId;
    }

    public User getOldRequester() {
        return oldRequester;
    }

    public Long getOldServiceOrderId() {
        return oldServiceOrderId;
    }

    public Set<OrderAttribute> getOrderAttributes() {
        return orderAttributes;
    }

    public Set<OrderAttribute> getOrderAttributesMapped() {
        if (orderAttributesMapped == null) {
            orderAttributesMapped = new HashSet<>();
            for (OrderAttribute orderAttribute : getOrderAttributes()) {
                if (orderAttribute.getOptionalCounterpart() != null) {
                    orderAttributesMapped.add(orderAttribute.getOptionalCounterpart());
                } else {
                    orderAttributesMapped.add(orderAttribute);
                }
            }
        }
        return orderAttributesMapped;
    }

    public List<Sample> getOrderItemSamples() {
        Set<Sample> orderItemSamples = new HashSet<>();
        for (OrderItem orderItem : getOrderItems()) {
            if (orderItem.getSample() != null) {
                orderItemSamples.add(orderItem.getSample());
            }
        }
        return CollectionHelper.asList(orderItemSamples);
    }

    public Set<OrderItem> getOrderItems() {
        return new HashSet<>();
    }

    public Set<Order> getOrders() {
        return orders;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    public AbstractNamedBaseEntity getOrganizationCompany() {
        if (getInstitute() != null) {
            return getInstitute().getOrganization();
        }
        return getDivision() != null ? getDivision().getCompany() : null;
    }

    public String getOrganizationCompanyName() {
        AbstractNamedBaseEntity organizationCompany = getOrganizationCompany();
        return organizationCompany != null ? organizationCompany.getName() : null;
    }

    @Override
    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public Integer getPlateSubmissionProposalLimit() {
        return plateSubmissionProposalLimit;
    }

    public Set<Plate> getPlates() {
        return plates;
    }

    public Long getPlatesCount() {
        if (platesCount == null) {
            platesCount = CDI.current().select(PlateService.class).get().getPlatesByContainerIdCount(getId());
        }
        return platesCount;
    }

    public String getPostalAddress() {
        return StringHelper.getPostalAddress(getBillingInfo(), getInstitute(), getDivision());
    }

    public String getPrintImportResourcesTotalSize() {
        return NumberUtils.getPrintSize(getImportResourcesTotalSize());
    }

    public String getPrintResourcesTotalSize() {
        return NumberUtils.getPrintSize(getResourcesTotalSize());
    }

    public Project getProject() {
        return project;
    }

    public String getPspElement() {
        return pspElement;
    }

    public String getPspElementDisplay() {
        return getFinanceSourceEthDisplay() + " " + Messages.get("pspElement") + " " + getPspElement();
    }

    @Override
    public String getRelativeRepositoryPath() {
        return "container_" + getId();
    }

    public String getRemarks() {
        return remarks;
    }

    public Boolean getRenderedCostCentre() {
        Boolean ret = renderedCostCentre;
        if (StringHelper.isNotEmpty(costCentre)) {
            ret = Boolean.TRUE;
        } else if (StringHelper.isNotEmpty(pspElement)) {
            ret = Boolean.FALSE;
        }
        return ret;
    }

    public User getRequester() {
        return requester;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public Long getResourcesTotalSize() {
        if (resourcesTotalSize == null) {
            resourcesTotalSize = CDI.current().select(ContainerService.class).get().getResourcesTotalSizeByContainer(this);
        }
        return resourcesTotalSize;
    }

    public String getRowStyleClass() {
        if (isPending()) {
            return Constants.BACKGROUND_COLOR_ORANGE;
        }
        if (isSubmitted()) {
            return Constants.BACKGROUND_COLOR_BROWN;
        }
        if (isReview()) {
            return Constants.BACKGROUND_COLOR_BROWN;
        }
        if (isArrived()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isRevised()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isRevisionAccepted()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isRunning()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isProcessing()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isProcessed()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isAnalyzing()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isAnalyzed()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isFinished()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (isReopened()) {
            return Constants.BACKGROUND_COLOR_BROWN;
        }
        if (isRejected()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isCanceled()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isPrivate()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (isPublished()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (isClosed()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (isAccepted()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        return Constants.EMPTY_STRING;
    }

    public List<Run> getRuns() {
        if (runs == null) {
            runs = CDI.current().select(RunService.class).get().getRunsByContainerId(getId());
        }
        return runs;
    }

    public Long getRunsCount() {
        if (runsCount == null) {
            runsCount = CDI.current().select(RunService.class).get().getRunsByContainerIdCount(getId());
        }
        return runsCount;
    }

    public List<Sample> getSampleReplacements() {
        if (sampleReplacements == null) {
            sampleReplacements = CDI.current().select(SampleService.class).get().getSampleReplacementsByContainerId(getId());
        }
        return sampleReplacements;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public List<Sample> getSamplesUserDecisionRequired() {
        if (samplesUserDecisionRequired == null) {
            samplesUserDecisionRequired = CDI.current().select(SampleService.class).get().getSamplesUserDecisionRequired(getId());
        }
        return samplesUserDecisionRequired;
    }

    public String getSelectedCustomStatus() {
        return selectedCustomStatus;
    }

    public SampleTypeEnum getSelectedSampleType() {
        return selectedSampleType;
    }

    public StatusEnum getSelectedStatus() {
        return selectedStatus;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public Set<Service> getServices() {
        if (services == null) {
            services = new HashSet<>();
            for (OrderItem orderItem : getOrderItems()) {
                if (orderItem.getService() != null) {
                    services.add(orderItem.getService());
                }
            }
        }
        return services;
    }

    public Set<User> getServicesUsers() {
        Set<User> users = new HashSet<>();
        for (Service service : getServices()) {
            if (!service.getUsers().isEmpty()) {
                users.addAll(service.getUsers());
            }
        }
        return users;
    }

    @Override
    public String getShowScreenLink() {
        return getShowScreenLink(this);
    }

    public List<String> getSpecificFunctions(User user) {
        List<String> specificFunctions = new ArrayList<>();
        if (user.equals(getContact())) {
            specificFunctions.add(Messages.get("contact"));
        }
        if (user.equals(getRequester())) {
            specificFunctions.add(Messages.get("requester"));
        }
        if (user.equals(getBudgetOfficer())) {
            specificFunctions.add(Messages.get("budgetOfficer"));
        }
        if (user.equals(getLeader())) {
            specificFunctions.add(Messages.get("leader"));
        }
        return specificFunctions;
    }

    public String getSpecificFunctionsFirst(User user) {
        return getSpecificFunctions(user).stream().findFirst().orElse(null);
    }

    public AbstractStatus getState() {
        return getStates().isEmpty() ? null : getStates().get(getStates().size() - 1);
    }

    public List<StandardContainerStatus> getStates() {
        return states;
    }

    public List<StandardContainerStatus> getStatesIncludingAnnouncements() {
        List<StandardContainerStatus> statesIncludingAnnouncements = new ArrayList<>();
        statesIncludingAnnouncements.addAll(getStates());
        if (this instanceof Project) {
            Project asProject = (Project) this;
            asProject.isReadableProjectInternals();
            if (asProject.getFinishAnnouncedDate() != null) {
                StandardContainerStatus newStatus = new StandardContainerStatus();
                newStatus.setName("finishAnnounced");
                newStatus.setCreated(asProject.getFinishAnnouncedDate());
                newStatus.setModified(asProject.getFinishAnnouncedDate());
                statesIncludingAnnouncements.add(newStatus);
            }
            if (asProject.getPrivateAnnouncedDate() != null) {
                StandardContainerStatus newStatus = new StandardContainerStatus();
                newStatus.setName("privateAnnounced");
                newStatus.setCreated(asProject.getPrivateAnnouncedDate());
                newStatus.setModified(asProject.getPrivateAnnouncedDate());
                statesIncludingAnnouncements.add(newStatus);
            }
            if (asProject.getPublishGrantedDate() != null) {
                StandardContainerStatus newStatus = new StandardContainerStatus();
                newStatus.setName("publishGranted");
                newStatus.setCreated(asProject.getPublishGrantedDate());
                newStatus.setModified(asProject.getPublishGrantedDate());
                statesIncludingAnnouncements.add(newStatus);
            }
        }
        return statesIncludingAnnouncements;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public LocalDateTime getStatusModified() {
        return statusModified;
    }

    public User getStatusModifiedBy() {
        return statusModifiedBy;
    }

    public List<Object> getStorageInfo() {
        if (storageInfo == null) {
            storageInfo = CDI.current().select(StorageService.class).get().getStorageInfoByContainerId(getId());
        }
        return storageInfo;
    }

    public StorageModel getStorageModel() {
        return storageModel;
    }

    public String getStorageUrl(Storage storage) {
        String storageFolderUrl = storage != null ? storage.getContainerFolderUrl() : null;
        return storageFolderUrl != null ? storageFolderUrl + getId() : null;
    }

    public String getStorageUrlByStorageId(String storageId) {
        return getStorageUrl(getEntityService().find(Storage.class, Long.valueOf(storageId)));
    }

    public List<Storage> getStorages() {
        return CDI.current().select(StorageService.class).get().getStoragesByContainerId(getId());
    }

    public String getSummary() {
        return summary;
    }

    public String getSummarySafeHtml() {
        return StringHelper.getSafeHtml(getSummary());
    }

    public String getSummaryTrimmed() {
        return StringHelper.removeEmptyLines(getSummary());
    }

    public List<String> getSupportFunctions(User user) {
        List<String> supportFunctions = new ArrayList<>();
        if (user.equals(getCoach())) {
            supportFunctions.add(Messages.get("coach"));
        }
        if (user.equals(getCoachBackup())) {
            supportFunctions.add(Messages.get("coachBackup"));
        }
        if (user.equals(getBioinformatician())) {
            supportFunctions.add(Messages.get("bioinformatician"));
        }
        if (getTrackingUsers().contains(user)) {
            supportFunctions.add(Messages.get("tracker"));
        }
        if (getServicesUsers().contains(user)) {
            supportFunctions.add(Messages.get("serviceTracker"));
        }
        if (getServiceType().getUsers().contains(user)) {
            supportFunctions.add(Messages.get("serviceTypeTracker"));
        }
        if (getProject() != null) {
            if (user.equals(getProject().getCoach())) {
                supportFunctions.add(Messages.get("pCoach"));
            }
            if (user.equals(getProject().getCoachBackup())) {
                supportFunctions.add(Messages.get("pCoachBackup"));
            }
            if (user.equals(getProject().getBioinformatician())) {
                supportFunctions.add(Messages.get("pBioinformatician"));
            }
            if (getProject().getTrackingUsers().contains(user)) {
                supportFunctions.add(Messages.get("tracker"));
            }
        }
        return supportFunctions;
    }

    public Set<User> getSupporters() {
        if (supporters == null) {
            setSupporters();
        }
        return supporters;
    }

    public Set<Technology> getTechnologies() {
        return technologies;
    }

    public String getTechnologiesAsString() {
        return technologiesAsString;
    }

    public BigDecimal getTotalPriceBillableCharges() {
        return totalPriceBillableCharges;
    }

    public BigDecimal getTotalPriceBookedBillableCharges() {
        return totalPriceBookedBillableCharges;
    }

    public BigDecimal getTotalPriceNonBookedBillableCharges() {
        return totalPriceNonBookedBillableCharges;
    }

    public Boolean getTracked() {
        if (tracked == null) {
            tracked = getCurrentUser().getTrackedContainers().contains(this);
        }
        return tracked;
    }

    public Set<User> getTrackingUsers() {
        return trackingUsers;
    }

    public List<User> getTrackingUsersAsList() {
        return CollectionHelper.asList(getTrackingUsers());
    }

    public boolean getUserDecisionRequired() {
        return !getSamplesUserDecisionRequired().isEmpty();
    }

    public Set<WorkflowStep> getWorkflowSteps() {
        Set<WorkflowStep> workflowSteps = new HashSet<>();
        for (Workflow workflow : getWorkflows()) {
            workflowSteps.addAll(workflow.getWorkflowSteps());
        }
        return workflowSteps;
    }

    public Set<Workflow> getWorkflows() {
        return workflows;
    }

    public Set<Workunit> getWorkunits() {
        return workunits;
    }

    public boolean hasBeenAccepted() {
        if (hasBeenAccepted == null) {
            hasBeenAccepted = false;
            for (StandardContainerStatus state : getStates()) {
                if (StatusEnum.ACCEPTED.getLabel().equals(state.name)) {
                    hasBeenAccepted = true;
                    break;
                }
            }
        }
        return hasBeenAccepted;
    }

    public boolean hasBeenAcceptedButNotFinished() {
        return hasBeenAccepted() && !hasBeenFinished();
    }

    public boolean hasBeenArrived() {
        for (StandardContainerStatus state : getStates()) {
            if (StatusEnum.ARRIVED.getLabel().equals(state.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBeenFinished() {
        for (StandardContainerStatus state : getStates()) {
            if (StatusEnum.FINISHED.getLabel().equals(state.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBeenRevised() {
        for (StandardContainerStatus state : getStates()) {
            if (StatusEnum.REVISED.getLabel().equals(state.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBookingsWithoutSAPNumber() {
        for (Booking booking : getBookings()) {
            if (booking.getSapNumber() == null || booking.getSapNumber() == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDeletableFailedWorkunits() {
        return isExtensible() && hasFailedWorkunits();
    }

    public boolean hasFailedWorkunits() {
        return getFailedWorkunitsCount() > 0;
    }

    public boolean hasLabeledSamples() {
        return CDI.current().select(SampleService.class).get().hasLabeledSamplesByContainer(this);
    }

    public boolean hasSampleReplacements() {
        return false;
    }

    public boolean hasSampleReplacementsToBeSent() {
        return false;
    }

    public boolean hasSpecificFunction(User user, boolean includeRequester) {
        return user != null && user.getLogin() != null && (user.getLogin().equals(getBudgetOfficer().getLogin()) || user.getLogin().equals(getContact().getLogin()) || getLeader() != null && user
            .getLogin().equals(getLeader().getLogin()) || includeRequester && user.getLogin().equals(getRequester().getLogin()));
    }

    public void indexDependents() {
        IndexHelper.indexEntities(getSamples());
        IndexHelper.indexEntities(getDatasets());
        IndexHelper.indexEntities(getWorkunits());
        IndexHelper.indexEntities(getResources());
    }

    public void init(User user) {
        if (user != null) {
            setContact(user);
            setRequester(user);
            // setBudgetOfficer(user);
            if (isContainerProject()) {
                setLeader(user);
                ((Project) this).setStartDate(LocalDate.now());
            }
            initBillingData(user);
        }
        setOrderDataOnly(!isContainerProject());
    }

    public void initBillingData(User user) {
        if (user != null) {
            if (user.getUserBillingInfo() != null) {
                setBillingInfo(new BillingInfo(user.getUserBillingInfo()));
                if (user.getUserBillingInfo().getInstitute() != null) {
                    setInstituteHierarchy(user.getUserBillingInfo().getInstitute());
                } else if (user.getUserBillingInfo().getDivision() != null) {
                    setDivisionHierarchy(user.getUserBillingInfo().getDivision());
                }
                setFinanceSourceEth(user.getUserBillingInfo().getFinanceSourceEth());
                setRenderedCostCentre(user.getUserBillingInfo().getCostCentre() != null);
                if (getRenderedCostCentre()) {
                    setCostCentre(user.getUserBillingInfo().getCostCentre());
                } else {
                    setPspElement(user.getUserBillingInfo().getPspElement());
                }
                setEuGrant(user.getUserBillingInfo().getEuGrant());
            } else {
                setBillingInfo(new BillingInfo(user));
                if (user.getInstitute() != null) {
                    setInstituteHierarchy(user.getInstitute());
                } else if (user.getDivision() != null) {
                    setDivisionHierarchy(user.getDivision());
                }
                resetFinanceSourceEth();
            }
        }
    }

    @Override
    public void instituteChanged(ValueChangeEvent event) {
        Organization oldOrganization = getOrganization();
        HasAffiliation.super.instituteChanged(event);
        if (oldOrganization == null || !oldOrganization.equals(getOrganization())) {
            resetVatAndReferenceNumber();
            setVatNumberOfOrganization(getOrganization());
        }
    }

    public boolean isAcceptRevisionPending() {
        boolean revised = false;
        boolean revisionAccepted = false;
        for (StandardContainerStatus state : getStates()) {
            if (StatusEnum.REVISED.getLabel().equals(state.getName())) {
                revised = true;
            }
            if (StatusEnum.REVISIONACCEPTED.getLabel().equals(state.getName())) {
                revisionAccepted = true;
            }
        }
        return revised && !revisionAccepted && !(isCanceled() || isClosed() || isFinished());
    }

    public boolean isAccepted() {
        return StatusEnum.ACCEPTED.equals(getStatus());
    }

    public boolean isAddressConfirmedRendered() {
        return getId() == 0;
    }

    public boolean isAllBillableChargesBooked() {
        for (Charge charge : getCharges()) {
            if (charge.isBillable() && !charge.isBooked()) {
                return false;
            }
        }
        return true;
    }

    public boolean isAnalyzed() {
        return StatusEnum.ANALYZED.equals(getStatus());
    }

    public boolean isAnalyzing() {
        return StatusEnum.ANALYZING.equals(getStatus());
    }

    public boolean isArrived() {
        return StatusEnum.ARRIVED.equals(getStatus());
    }

    public boolean isBillingAddressUpdatable() {
        return true;
    }

    public boolean isBioinformatician(User user) {
        return getBioinformatician() != null && getBioinformatician().equals(user);
    }

    public boolean isBioinformaticianChanged() {
        return bioinformaticianChanged;
    }

    public boolean isBioinformaticianValid() {
        return isInFinalState() || getBioinformatician() != null && getBioinformatician().hasRoleImplicit(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isBookingAddable() {
        return (hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(new Booking().getDefaultRequiredRole()) && !getCharges().isEmpty() && !isClosed()) && !getNonBookedCharges()
            .isEmpty();
    }

    public boolean isBookingRequired() {
        boolean required = false;
        if (LocalDateTime.now().isAfter(LocalDateTime.now().withDayOfMonth(1).withMonth(11))) {
            // Important: After November 1, all non-booked charges have to booked.
            required = !getNonBookedCharges().isEmpty();
        } else {
            BigDecimal sum = BigDecimal.ZERO;
            for (Charge charge : getNonBookedCharges()) {
                sum = sum.add(charge.getDiscountedPrice());
                if (LocalDateTime.now().withDayOfYear(1).isAfter(charge.getCreated()) || sum.doubleValue() > getConfiguration().getBookingRequiredTotal()) {
                    required = true;
                    break;
                }
            }
        }
        return required;
    }

    public boolean isBudgetLimitEditable() {
        return getConfiguration().isBudgetLimitEnabled() && (!isManaged()
            || hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)
            || isManager() && (isPending() || isReview()));
    }

    public boolean isBudgetLimitExceeded() {
        return getBudgetLimit() != null && getChargesSum() != null && getBudgetLimit().compareTo(getChargesSum()) < 0;
    }

    public boolean isBudgetOfficer(User user) {
        return getBudgetOfficer().equals(user);
    }

    public boolean isBudgetOfficerChanged() {
        return isManaged() && getOldBudgetOfficer() != null && !(getBudgetOfficer() != null && getBudgetOfficer().equals(getOldBudgetOfficer()));
    }

    public boolean isCanceled() {
        return StatusEnum.CANCELED.equals(getStatus());
    }

    public boolean isChargeCreatable() {
        return !isPendingOrReview() && !isSubmitted() && !isCanceled();
    }

    public boolean isChargeable() {
        return isReadable() && !isClosed();
    }

    public boolean isClosable() {
        return isFinished() && !hasBookingsWithoutSAPNumber();
    }

    public boolean isClosed() {
        return StatusEnum.CLOSED.equals(getStatus());
    }

    public boolean isCoach(User user) {
        return getCoach() != null && getCoach().equals(user);
    }

    public boolean isCoachBackup(User user) {
        return getCoachBackup() != null && getCoachBackup().equals(user);
    }

    public boolean isCoachBackupChanged() {
        return coachBackupChanged;
    }

    public boolean isCoachBackupValid() {
        return isInFinalState() || getCoachBackup() != null && getCoachBackup().hasRoleImplicit(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isCoachChanged() {
        return coachChanged;
    }

    public boolean isCoachValid() {
        return isInFinalState() || getCoach() != null && getCoach().hasRoleImplicit(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isComputerLoginEnabled() {
        return false;
    }

    public boolean isContact(User user) {
        return getBudgetOfficer().equals(user);
    }

    public boolean isContactChanged() {
        return isManaged() && getOldContact() != null && !(getContact() != null && getContact().equals(getOldContact()));
    }

    public boolean isContainerProject() {
        return getClass().equals(Project.class);
    }

    public boolean isCustomStatusCreatable() {
        // A custom container status is creatable for the container iff one is editable for the container.
        return isCustomStatusEditable();
    }

    public boolean isCustomStatusDeletable() {
        // A custom container status is deletable for the container iff the last state is a custom container state, one is creatable for the container, and the container is rollbackable.
        if (getLastState() != null && getLastState().isCustomContainerStatus()) {
            // At this point in time, it is certain that the last state is a custom container status.
            return isCustomStatusEditable() && isRollbackable();
        }
        return false;
    }

    private boolean isCustomStatusEditable() {
        return (hasCurrentUserRoleEnum(RoleEnum.FEEDER) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)) && !isContainerProject() && !(isPrivate() || isPublished() || isRejected() || isPending() || isSubmitted() || isCanceled() || isFinished() || isClosed() || isReopened());
    }

    public boolean isCustomStatusEmptyOrNotEqualsTo(String aCustomStatus) {
        return StringHelper.isEmpty(getCustomStatus()) || !getCustomStatus().equals(aCustomStatus);
    }

    public boolean isDataAccessEnabled() {
        return false;
    }

    public boolean isDataDeliveryOnly() {
        return getStorageModel() != null && getStorageModel().isDataDeliveryOnly();
    }

    @Override
    public boolean isDeletable() {
        return getDatasets().isEmpty() && getResources().isEmpty() && getWorkunits().isEmpty() && getOffers().isEmpty() && getCharges().isEmpty() && getBookings().isEmpty() && getWorkflows()
            .isEmpty() && getFeedbacks().isEmpty() && getInstrumentReservations().isEmpty() && isSamplesDeletable();
    }

    public boolean isDiscounted() {
        return getDiscount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDownloadable() {
        return getConfiguration().isDownloadManagerEnabled() && getCurrentUser().isDownloadManagerEnabled() && RepositoryHelper.getLocalStorage(false).isAccessDMEnabled();
    }

    public boolean isExpress() {
        return express;
    }

    public boolean isExtensionReportPending() {
        return false;
    }

    public boolean isExtensionReportPendingHintRendered() {
        return isContainerProject() && isReadableExtensionReports() && isExtensionReportPending();
    }

    public boolean isExtensionReportReview() {
        return false;
    }

    public boolean isExtensionReportReviewHintRendered() {
        return isContainerProject() && isReadableExtensionReports() && isExtensionReportReview();
    }

    public boolean isFeedbacksRendered() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER) && !getFeedbacks().isEmpty();
    }

    public boolean isFinished() {
        return StatusEnum.FINISHED.equals(getStatus());
    }

    public boolean isFormerMember(User user) {
        return isMember(user, Membership.DISCRIMINATOR_FORMER, null);
    }

    public boolean isIdentityBioinformatician() {
        return isBioinformatician(getCurrentUser());
    }

    public boolean isIdentityCoach() {
        return isCoach(getCurrentUser());
    }

    public boolean isIdentityCoachBackup() {
        return isCoachBackup(getCurrentUser());
    }

    public boolean isIdentityCoachOrCoachBackup() {
        return isIdentityCoach() || isIdentityCoachBackup();
    }

    public boolean isIdentityCoachOrCoachBackupOrBioinformatician() {
        return isIdentityCoachOrCoachBackup() || isIdentityBioinformatician();
    }

    public boolean isInBookableState() {
        return !(isClosed() || isPublished() || isCanceled() || isRejected());
    }

    public boolean isInFinalState() {
        return isClosed() || isPublished() || isPrivate() || isCanceled() || isRejected();
    }

    public boolean isIncludeOrderData() {
        return includeOrderData;
    }

    public boolean isInitialCustomStatus() {
        return initialCustomStatus;
    }

    public boolean isInstrumentDataPackageRequired() {
        return getInstrument() != null && !getInstrument().getInstrumentDataPackages().isEmpty();
    }

    public boolean isInstrumentReservationEditable() {
        return isExtensible() && (hasBeenAccepted() || getProject() != null && getProject().isInstrumentReservationEditable()) && !isInFinalState();
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isLeader(User user) {
        return getBudgetOfficer().equals(user);
    }

    public boolean isLeaderChanged() {
        return isManaged() && getOldLeader() != null && !(getLeader() != null && getLeader().equals(getOldLeader()));
    }

    public boolean isMailsUserReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || isMemberTransitive();
    }

    public boolean isManager() {
        return isManager(getCurrentUser());
    }

    public boolean isManager(User user) {
        return isMember(user, Membership.DISCRIMINATOR_CURRENT, Membership.ROLE_MANAGER);
    }

    public boolean isMember() {
        return isMember(getCurrentUser());
    }

    public boolean isMember(User user) {
        return getProject() != null ? getProject().isMember(user, Membership.DISCRIMINATOR_CURRENT, null) : isMember(user, Membership.DISCRIMINATOR_CURRENT, null);
    }

    public boolean isMember(User user, String discriminator, String role) {
        return getId() > 0 && CDI.current().select(MembershipService.class).get().checkMemberByContainerAndUserAndDiscriminatorAndRole(this, user, discriminator, role);
    }

    public boolean isMemberAddable() {
        return (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isManager(getCurrentUser())) && isMemberEditableState();
    }

    public boolean isMemberEditableState() {
        return !isPendingOrReview() && !isSubmitted() && !isPublished() && !isRejected();
    }

    public boolean isMemberRemovable(User user) {
        return user != null && isMembershipRemovable(user.getMembership(this));
    }

    public boolean isMemberShowState() {
        return !isPendingOrReview() && !isSubmitted() && !isRejected();
    }

    public boolean isMemberTransitive(User user) {
        return isMember(user) || getProject() != null && getProject().isMember(user);
    }

    public boolean isMemberTransitive() {
        return isMember() || getProject() != null && getProject().isMember();
    }

    public boolean isMembershipRemovable(Membership membership) {
        return membership != null && !hasSpecificFunction(membership.getUser(), false);
    }

    public boolean isMembershipRoleDowngradable(Membership membership) {
        return membership != null && membership.isRoleManager() && isMembershipRemovable(membership);
    }

    public boolean isMembershipRoleUpgradable(Membership membership) {
        return membership != null && membership.isRoleMember();
    }

    public boolean isNonBookedCharges() {
        return getTotalPriceNonBookedBillableCharges() != null && getTotalPriceNonBookedBillableCharges().doubleValue() > 0;
    }

    public boolean isNotPendingOrReviewOrRejected() {
        return !(isPending() || isReview() || isRejected());
    }

    public boolean isNotPendingOrSubmitted() {
        return !(isPending() || isSubmitted());
    }

    public boolean isNotPendingOrSubmittedOrCanceled() {
        return !(isPending() || isSubmitted() || isCanceled());
    }

    public boolean isOrderDataOnly() {
        return orderDataOnly;
    }

    public boolean isPending() {
        return StatusEnum.PENDING.equals(getStatus());
    }

    public boolean isPendingOrReview() {
        return isPending() || isReview();
    }

    public boolean isPrivate() {
        return StatusEnum.PRIVATE.equals(getStatus());
    }

    public boolean isProcessed() {
        return StatusEnum.PROCESSED.equals(getStatus());
    }

    public boolean isProcessesSamples() {
        return processesSamples;
    }

    public boolean isProcessing() {
        return StatusEnum.PROCESSING.equals(getStatus());
    }

    public boolean isPublished() {
        return StatusEnum.PUBLISHED.equals(getStatus());
    }

    public boolean isReadableByUser(User user) {
        return isContainerProject() && isPublished() || isMemberTransitive(user);
    }

    public boolean isReadableExtensionReports() {
        return false;
    }

    public boolean isReadyForProcessing() {
        boolean accepted = false;
        boolean arrived = false;
        for (StandardContainerStatus state : getStates()) {
            if (StatusEnum.ACCEPTED.getLabel().equals(state.getName())) {
                accepted = true;
            } else if (StatusEnum.ARRIVED.getLabel().equals(state.getName())) {
                arrived = true;
            }
        }
        return accepted && arrived && (isAccepted() || isArrived());
    }

    public boolean isRejected() {
        return StatusEnum.REJECTED.equals(getStatus());
    }

    public boolean isRemovableFromOffer() {
        return !isInFinalState();
    }

    public boolean isRenderHasBookingsWithoutSAPNumberHint() {
        return isFinished() && hasBookingsWithoutSAPNumber();
    }

    public boolean isRenderedAddWorkflowButton() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isRenderedBookings() {
        if (isRenderedBookings == null) {
            isRenderedBookings = isChargeCreatable() && (hasCurrentUserRoleEnum(RoleEnum.BOOKINGREADER) || isMember());
        }
        return isRenderedBookings;
    }

    public boolean isRenderedChargeButton() {
        return isChargeable() && hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isRenderedCharges() {
        if (isRenderedCharges == null) {
            isRenderedCharges = isChargeCreatable() && (hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || isMember());
        }
        return isRenderedCharges;
    }

    public boolean isRenderedCosts() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || isMember();
    }

    public boolean isRenderedCustomStateSendMail() {
        CustomContainerStatus lastCustomState = getLastCustomState();
        return lastCustomState != null && lastCustomState.equals(getLastState()) && !lastCustomState.isSentMail();
    }

    public boolean isRenderedFeedbackButton() {
        return getConfiguration().isFeedbackEnabled() && (hasCurrentUserRoleEnum(RoleEnum.FEEDBACKMANAGER) || isMember());
    }

    public boolean isRenderedFeedbacks() {
        if (isRenderedFeedbacks == null) {
            isRenderedFeedbacks = !getFeedbacks().isEmpty() && hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER);
        }
        return isRenderedFeedbacks;
    }

    public boolean isRenderedFinanceSource() {
        return getOrganizationType() != null && getOrganizationType().isFinanceSourceRequired();
    }

    public boolean isRenderedFinanceSourceEth() {
        return getOrganization() != null && !(getOrganization().isEth() || getOrganization().isUzh());
    }

    public boolean isRenderedFinancedByEuGrant() {
        return isManaged() && getEuGrant() != null && getEuGrant();
    }

    public boolean isRenderedOffers() {
        return !getOffers().isEmpty();
    }

    public boolean isRenderedOrders() {
        if (isRenderedOrders == null) {
            if (getConfiguration().isOrderEnabled() && isContainerProject()) {
                Project projectContainer = (Project) this;
                isRenderedOrders = projectContainer.isAcceptedButNotPublished() || !projectContainer.getOrders().isEmpty() && (hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || projectContainer
                    .isMember());
            } else {
                isRenderedOrders = false;
            }
        }
        return isRenderedOrders;
    }

    public boolean isRenderedReviews() {
        if (isRenderedReviews == null) {
            isRenderedReviews = getConfiguration().isReviewRequired() && (hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || hasCurrentUserRoleEnum(
                RoleEnum.REVIEWER));
        }
        return isRenderedReviews;
    }

    public boolean isRenderedSendEmailCheckboxForCustomStatusChange() {
        return isProcessing();
    }

    public boolean isRenderedTree() {
        if (isRenderedTree == null) {
            isRenderedTree = !getSamples().isEmpty() || !getWorkunits().isEmpty() || !getDatasets().isEmpty();
        }
        return isRenderedTree;
    }

    public boolean isReopened() {
        return StatusEnum.REOPENED.equals(getStatus());
    }

    public boolean isReplaceBioinformatician() {
        return replaceBioinformatician;
    }

    public boolean isRequestBookingButtonRendered() {
        return (isIdentityCoachOrCoachBackup() || hasCurrentUserRoleEnum(RoleEnum.ADMIN)) && !getNonBookedCharges().isEmpty();
    }

    public boolean isRequester(User user) {
        return getBudgetOfficer().equals(user);
    }

    public boolean isRequesterAccessWarning() {
        return getRequester() != null && !getRequester().equals(getContact()) && !getRequester().equals(getBudgetOfficer()) && (getLeader() == null || !getRequester().equals(getLeader()));
    }

    public boolean isRequesterChanged() {
        return isManaged() && getOldRequester() != null && !(getRequester() != null && getRequester().equals(getOldRequester()));
    }

    public boolean isRequiresProject() {
        return requiresProject;
    }

    public boolean isReview() {
        return StatusEnum.REVIEW.equals(getStatus());
    }

    public boolean isReviewRequired() {
        return reviewRequired;
    }

    public boolean isReviewed() {
        return isAccepted() || isRejected();
    }

    public boolean isRevised() {
        return StatusEnum.REVISED.equals(getStatus());
    }

    public boolean isRevisionAccepted() {
        return StatusEnum.REVISIONACCEPTED.equals(getStatus());
    }

    public boolean isRollbackable() {
        return false;
    }

    public boolean isRunning() {
        return StatusEnum.RUNNING.equals(getStatus());
    }

    public boolean isSampleCreatable() {
        return isExtensible() && (hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || !isOrderDataOnly());
    }

    public boolean isSamplesDeletable() {
        for (Sample sample : getSamples()) {
            if (!sample.isDeletableUponContainerDeletion(this)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    public boolean isSamplesUserDecisionRequiredNotEmpty() {
        return !getSamplesUserDecisionRequired().isEmpty();
    }

    @SuppressWarnings("unused")
    public boolean isSequencingDone() {
        return getCustomStatus() != null && getCustomStatus().equals(Constants.CUSTOM_ORDER_STATE_SEQUENCING_DONE);
    }

    public boolean isServiceColumnEnabled() {
        return serviceColumnEnabled;
    }

    public boolean isSetMailTrackingNumberRendered() {
        return getServiceType().isOrderAttribute("mailTrackingNumber") && isExtensible();
    }

    public boolean isShowEula() {
        return showEula;
    }

    public boolean isShowFormerMembers() {
        return showFormerMembers;
    }

    public boolean isStatusSyncable() {
        // Note: Orders associated with a project have no own member management. Hence, they are not synchronized!
        return isNotPendingOrReviewOrRejected() && isNotPendingOrSubmittedOrCanceled();
    }

    public boolean isSubmitReplacementsButtonRendered() {
        return isProcessing() && USER_DECISION_REQUIRED.equals(getCustomStatus()) && hasSampleReplacementsToBeSent();
    }

    public boolean isSubmitted() {
        return StatusEnum.SUBMITTED.equals(getStatus());
    }

    public boolean isSyncable() {
        return isStatusSyncable() && getConfiguration().isSynchronizeWithADEnabled();
    }

    public boolean isSyncableByCurrentUser() {
        return isSyncable() && hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isTrackedByUser(User user) {
        return user != null && user.getTrackedContainers().contains(this);
    }

    public boolean isUserDecisionRendered() {
        for (Sample sample : getSamples()) {
            if (sample.getUserDecision() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isWorkunitCreatable() {
        return isExtensible() || getProject() != null && getProject().isExtensible();
    }

    public void leaderChangedListener(ValueChangeEvent event) {
        if (getOldLeader() == null) {
            setOldLeader((User) event.getOldValue());
        }
    }

    @Override
    public void organizationChanged(ValueChangeEvent event) {
        HasAffiliation.super.organizationChanged(event);
        resetVatAndReferenceNumber();
        setVatNumberOfOrganization((Organization) event.getNewValue());
    }

    @Override
    public void organizationTypeChanged(ValueChangeEvent event) {
        HasAffiliation.super.organizationTypeChanged(event);
        resetVatAndReferenceNumber();
    }

    @Override
    protected void preRemove() {
        super.preRemove();
        if (getCurrentUsername() != null) {
            CDI.current().select(ContextManager.class).get().setContextContainer(null);
        }
    }

    public void requesterChangedListener(ValueChangeEvent event) {
        if (getOldRequester() == null) {
            setOldRequester((User) event.getOldValue());
        }
    }

    public void resetFinanceSource() {
        setCostCentre(null);
        setPspElement(null);
    }

    public void resetFinanceSourceEth() {
        resetFinanceSource();
        setFinanceSourceEth(null);
        if (getInstitute() != null) {
            if (getInstitute().getDepartment().getOrganization().isEth()) {
                setFinanceSourceEth(Boolean.TRUE);
            } else if (getInstitute().getDepartment().getOrganization().isUzh()) {
                setFinanceSourceEth(Boolean.FALSE);
            }
        }
    }

    private void resetVatAndReferenceNumber() {
        getBillingInfo().setVatNumber(null);
        getBillingInfo().setReferenceNumber(null);
    }

    public void rollbackStatus() {
        if (isRollbackable()) {
            if (getLastState() != null && getLastState().isCustomContainerStatus()) {
                if (getCustomStates().size() > 1) {
                    getCustomStates().remove(getCustomStates().size() - 1);
                    setCustomStatus(getCustomStates().get(getCustomStates().size() - 1).getName());
                } else if (getCustomStates().size() == 1) {
                    getCustomStates().remove(getCustomStates().size() - 1);
                    setCustomStatus(null);
                }
            } else {
                if (getStates().size() > 1) {
                    getStates().remove(getStates().size() - 1);
                    setStatus(getStates().get(getStates().size() - 1).getStatusEnum());
                }
            }

            // Set all states to null, so they are recalculated.
            setAllStates(null);
        }
    }

    public void selectedStatusChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setSelectedCustomStatus(null);
        }
    }

    public void setAllStates(List<ContainerStatus> allStates) {
        this.allStates = allStates;
    }

    public void setAssociatedContainers(Set<? extends Container> associatedContainers) {
        this.associatedContainers = associatedContainers;
    }

    public void setBillingInfo(BillingInfo billingInfo) {
        this.billingInfo = billingInfo;
    }

    public void setBioinformatician(User bioinformatician) {
        this.bioinformatician = bioinformatician;
    }

    public void setBioinformaticianChanged(boolean bioinformaticianChanged) {
        this.bioinformaticianChanged = bioinformaticianChanged;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public void setBudgetOfficer(User budgetOfficer) {
        this.budgetOfficer = budgetOfficer;
    }

    public void setBudgetOfficerAndBillingData(User newBudgetOfficer) {
        setBudgetOfficer(newBudgetOfficer);
        if (hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || !hasBeenAccepted()) {
            initBillingData(newBudgetOfficer);
        }
    }

    public void setCharges(Set<Charge> charges) {
        this.charges = charges;
    }

    public void setCoach(User coach) {
        this.coach = coach;
    }

    public void setCoachBackup(User coachBackup) {
        this.coachBackup = coachBackup;
    }

    public void setCoachBackupChanged(boolean coachBackupChanged) {
        this.coachBackupChanged = coachBackupChanged;
    }

    public void setCoachChanged(boolean coachChanged) {
        this.coachChanged = coachChanged;
    }

    @Override
    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public void setCompanyName(String companyName) {
        this.companyName = StringHelper.format(companyName);
    }

    public void setContact(User contact) {
        this.contact = contact;
    }

    public void setCostCentre(String costCentre) {
        this.costCentre = costCentre;
    }

    public void setCreateAndAddCustomStatus(String aCustomStatus) {
        if (StringHelper.isNotEmpty(aCustomStatus) && (getCustomStatus() == null || !getCustomStatus().equals(aCustomStatus))) {
            CustomContainerStatus customContainerStatus = new CustomContainerStatus(this, aCustomStatus);
            getCustomStates().add(customContainerStatus);
            this.customStatus = aCustomStatus;
            setStatusModified();
        }
    }

    public void setCreateAndAddStatus(StatusEnum status) {
        if (getStates().isEmpty() || status == null || getStatus() != null && !getStatus().equals(status)) {
            StandardContainerStatus containerStatus = new StandardContainerStatus(this, status);
            getStates().add(containerStatus);
            this.status = status;
            setStatusModified();
        }
    }

    public void setCustomStates(List<CustomContainerStatus> customStates) {
        this.customStates = customStates;
    }

    public void setCustomStatus(String customStatus) {
        this.customStatus = customStatus;
    }

    public void setDatasets(Set<Dataset> datasets) {
        this.datasets = datasets;
    }

    @Override
    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public void setDiscussedWith(Set<User> discussedWith) {
        this.discussedWith = discussedWith;
    }

    @Override
    public void setDivision(Division division) {
        this.division = division;
        if (division != null) {
            setInstitute(null);
        }
    }

    public void setDivisionHierarchy(Division division) {
        setDivision(division);
        setDivisionName(getDivision().getName());
        setCompanyName(getDivision().getCompanyName());
        setOrganizationType(getDivision().getOrganizationType());
    }

    @Override
    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public void setDoiCreated(LocalDate doiCreated) {
        this.doiCreated = doiCreated;
    }

    public void setDoiCreatedBy(String doiCreatedBy) {
        this.doiCreatedBy = StringHelper.format(doiCreatedBy);
    }

    public void setEuGrant(Boolean euGrant) {
        this.euGrant = euGrant;
    }

    public void setExpress(boolean express) {
        this.express = express;
    }

    public void setFeedbacks(Set<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public void setFinanceSourceEth(Boolean financeSourceEth) {
        this.financeSourceEth = financeSourceEth;
    }

    public void setIncludeOrderData(boolean includeOrderData) {
        this.includeOrderData = includeOrderData;
    }

    public void setInitialCustomStatus(boolean initialCustomStatus) {
        this.initialCustomStatus = initialCustomStatus;
    }

    @Override
    public void setInstitute(Institute institute) {
        this.institute = institute;
        resetFinanceSourceEth();
        if (institute != null) {
            setDivision(null);
        }
    }

    public void setInstituteHierarchy(Institute institute) {
        setInstitute(institute);
        setDepartment(getInstitute().getDepartment());
        setOrganization(getDepartment().getOrganization());
        setOrganizationType(getOrganization().getOrganizationType());
    }

    public void setInstrumentReadConfiguration(InstrumentReadConfiguration instrumentReadConfiguration) {
        this.instrumentReadConfiguration = instrumentReadConfiguration;
    }

    public void setInstrumentReservations(Set<InstrumentReservation> instrumentReservations) {
        this.instrumentReservations = instrumentReservations;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public void setMemberships(Set<Membership> memberships) {
        this.memberships = memberships;
    }

    public void setOffers(Collection<Offer> offers) {
        this.offers = CollectionHelper.asSet(offers);
    }

    public void setOffersAsList(List<Offer> offers) {
        this.offers = CollectionHelper.asSet(offers);
    }

    public void setOldBudgetOfficer(User oldBudgetOfficer) {
        this.oldBudgetOfficer = oldBudgetOfficer;
    }

    public void setOldContact(User oldContact) {
        this.oldContact = oldContact;
    }

    public void setOldFlatRatedOrderId(Long oldFlatRatedOrderId) {
        this.oldFlatRatedOrderId = oldFlatRatedOrderId;
    }

    public void setOldLeader(User oldLeader) {
        this.oldLeader = oldLeader;
    }

    public void setOldProjectOrderId(Long oldProjectOrderId) {
        this.oldProjectOrderId = oldProjectOrderId;
    }

    public void setOldRequester(User oldRequester) {
        this.oldRequester = oldRequester;
    }

    public void setOldServiceOrderId(Long oldServiceOrderId) {
        this.oldServiceOrderId = oldServiceOrderId;
    }

    public void setOrderAttributes(Set<OrderAttribute> orderAttributes) {
        this.orderAttributes = orderAttributes;
    }

    public void setOrderDataOnly(boolean orderDataOnly) {
        this.orderDataOnly = orderDataOnly;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public void setPlateSubmissionProposalLimit(Integer plateSubmissionProposalLimit) {
        this.plateSubmissionProposalLimit = plateSubmissionProposalLimit;
    }

    public void setPlates(Set<Plate> plates) {
        this.plates = plates;
    }

    public void setProcessesSamples(boolean processesSamples) {
        this.processesSamples = processesSamples;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setPspElement(String pspElement) {
        this.pspElement = pspElement;
    }

    public void setRemarks(String remarks) {
        this.remarks = StringHelper.formatText(remarks);
    }

    public void setRenderedCostCentre(Boolean renderedCostCentre) {
        this.renderedCostCentre = renderedCostCentre;
    }

    public void setReplaceBioinformatician(boolean replaceBioinformatician) {
        this.replaceBioinformatician = replaceBioinformatician;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public void setRequiresProject(boolean requiresProject) {
        this.requiresProject = requiresProject;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public void setReviewRequired(boolean reviewRequired) {
        this.reviewRequired = reviewRequired;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void setSamplesUserDecisionRequired(List<Sample> samplesUserDecisionRequired) {
        this.samplesUserDecisionRequired = samplesUserDecisionRequired;
    }

    public void setSelectedCustomStatus(String selectedCustomStatus) {
        this.selectedCustomStatus = StringHelper.trimBoth(selectedCustomStatus);
    }

    public void setSelectedSampleType(SampleTypeEnum selectedSampleType) {
        this.selectedSampleType = selectedSampleType;
    }

    public void setSelectedStatus(StatusEnum selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public void setServiceColumnEnabled(boolean serviceColumnEnabled) {
        this.serviceColumnEnabled = serviceColumnEnabled;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        if (serviceType != null) {
            setRequiresProject(serviceType.isRequiresProject());
            setProcessesSamples(serviceType.isProcessesSamples());
            setSampleType(serviceType.getSampleType());
            setPlateSubmissionProposalLimit(serviceType.getPlateSubmissionProposalLimit());
            setServiceColumnEnabled(serviceType.isServiceColumnEnabled());
            setInitialCustomStatus(serviceType.isInitialCustomStatus());
            getOrderAttributes().clear();
            getOrderAttributes().addAll(serviceType.getOrderAttributes());
        }
    }

    public void setShowEula(boolean showEula) {
        this.showEula = showEula;
    }

    public void setShowFormerMembers(boolean showFormerMembers) {
        this.showFormerMembers = showFormerMembers;
    }

    public void setStates(List<StandardContainerStatus> states) {
        this.states = states;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setStatusModified() {
        statusModified = LocalDateTime.now();
        statusModifiedBy = getCurrentUser() != null ? getCurrentUser() : getUserByLogin("admin");
    }

    public void setStorageModel(StorageModel storageModel) {
        this.storageModel = storageModel;
    }

    public void setSummary(String summary) {
        this.summary = StringHelper.formatText(summary);
    }

    public void setSupporter(User user) {
        if (user != null && supporters != null) {
            supporters.add(user);
        }
    }

    public void setSupporters(Set<User> supporters) {
        this.supporters = supporters;
    }

    public void setSupporters() {
        supporters = new HashSet<>();
        setSupporter(getCoach());
        setSupporter(getCoachBackup());
        setSupporter(getBioinformatician());
        if (getTrackingUsers() != null && !getTrackingUsers().isEmpty()) {
            supporters.addAll(getTrackingUsers());
        }
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }

    public void setTracked(Boolean tracked) {
        this.tracked = tracked;
    }

    public void setTrackingUsers(Set<User> trackingUsers) {
        this.trackingUsers = trackingUsers;
    }

    public void setTrackingUsersAsList(List<User> users) {
        this.trackingUsers = CollectionHelper.asSet(users);
    }

    private void setVatNumberOfCompany(Company company) {
        if (company != null && company.getVatNumber() != null) {
            getBillingInfo().setVatNumber(company.getVatNumber());
        }
    }

    private void setVatNumberOfOrganization(Organization organization) {
        if (organization != null && organization.getVatNumber() != null) {
            getBillingInfo().setVatNumber(organization.getVatNumber());
        }
    }

    public void setWorkflows(Set<Workflow> workflows) {
        this.workflows = workflows;
    }

    public void setWorkunits(Set<Workunit> workunits) {
        this.workunits = workunits;
    }

    public String switchShowFormerMembers() {
        showFormerMembers = !showFormerMembers;
        return getUrlShowScreenLink() + "?id=" + getId() + "&tab=members&formerMembers=" + showFormerMembers + "&faces-redirect=true";
    }

    public void switchTracked() {
        setTracked(!getTracked());
    }

    public boolean userDecisionSubmitted() {
        setSamplesUserDecisionRequired(null);
        if (!getUserDecisionRequired() && !isSubmitReplacementsButtonRendered()) {
            setCreateAndAddCustomStatus(USER_DECISION_SUBMITTED);
            return true;
        }
        return false;
    }

    public boolean validateAddressConfirmed(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value != null) {
            final Boolean addressConfirmed = (Boolean) value;
            if (!addressConfirmed) {
                throw new BfabricValidatorException("addressNotConfirmedException");
            }
            return true;
        }
        return false;
    }
}