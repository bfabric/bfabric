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
import java.nio.CharBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.jimfs.Jimfs;
import org.apache.commons.codec.digest.DigestUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.Dashboard;
import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.DownloadDirectoryStructureEnum;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMap;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.list.ChargeList;
import org.bfabric.list.DatasetList;
import org.bfabric.list.ResourceList;
import org.bfabric.list.SampleList;
import org.bfabric.list.WorkunitList;
import org.bfabric.service.CommentService;
import org.bfabric.service.ContainerService;
import org.bfabric.service.CountryService;
import org.bfabric.service.DivisionService;
import org.bfabric.service.EventService;
import org.bfabric.service.MembershipService;
import org.bfabric.service.OrderService;
import org.bfabric.service.RoleService;
import org.bfabric.service.UserService;
import org.bfabric.service.WorkflowService;
import org.bfabric.util.BfabricPasswordEncryptor;
import org.bfabric.util.BfabricPasswordGenerator;
import org.bfabric.util.BfabricPasswordHash;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.DateUtils;
import org.bfabric.util.StringHelper;
import org.bfabric.util.TokenUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Where;
import org.primefaces.event.FileUploadEvent;

@Entity
@DynamicUpdate
@Table(name = "User_", uniqueConstraints = { @UniqueConstraint(name = "user_login_unique", columnNames = { "login" }), @UniqueConstraint(name = "user_email_unique", columnNames = { "email" }) })
@XmlRootElement
@NamedQuery(name = "User.findById", query = "SELECT a FROM User a WHERE a.id = :id")
@NamedQuery(name = "User.findForComputerLoginValidityCheck", query = "SELECT a FROM User a WHERE a.empDegree IS NULL and (a.computerLoginValidityChecked is null or a.computerLoginValidityChecked  < :validityChecked) and (a.lastLoginDate is null or a.lastLoginDate < :validityChecked) and a.computerLoginEnabled = true")
@NamedQuery(name = "User.findByLogin", query = "SELECT a FROM User a WHERE lower(a.login) = lower(:login)")
@NamedQuery(name = "User.findByEmail", query = "SELECT a FROM User a WHERE lower(a.email) = lower(:email)")
@NamedQuery(name = "User.findByFirstNameAndLastName", query = "SELECT a FROM User a WHERE lower(a.firstName) = lower(:firstName) and lower(a.lastName) = lower(:lastName)")
@NamedQuery(name = "User.findByShibbolethId", query = "SELECT a FROM User a WHERE a.shibbolethId = :shibbolethId")
@NamedQuery(name = "User.findByDepartmentId", query = "SELECT a FROM User a where a.institute.department.id = :departmentId")
@NamedQuery(name = "User.findByOrganizationId", query = "SELECT a FROM User a where a.institute.department.organization.id = :organizationId")
@NamedQuery(name = "User.findByCompanyId", query = "SELECT a FROM User a where a.division.company.id = :companyId")
@NamedQuery(name = "User.findByExpiredShibbolethMappings", query = "SELECT a FROM User a WHERE a.shibbolethId is not null and shibbolethLastLoginDate < current_date - 180")
@NamedQuery(name = "User.findByAccessCardExpiring", query = "SELECT a FROM User a WHERE a.accessCardExpiryDate is not null and a.accessCardExpiryDate = current_date + 30")
@NamedQuery(name = "User.findEmployees", query = "SELECT a FROM User a where a.empDegree > 0 ORDER BY a.lastName")
@NamedQuery(name = "User.findEmployeesIncluding", query = "SELECT a FROM User a where a.empDegree > 0 or a = :user ORDER BY a.lastName")
@NamedQuery(name = "User.findEmployeesRegular", query = "SELECT a FROM User a where a.empDegree > 20 ORDER BY a.lastName")
@NamedQuery(name = "User.findProjectLeaders", query = "SELECT DISTINCT a.leader FROM Project a")
@NamedQuery(name = "User.findCurrentUsersByRunningProjects", query = "SELECT DISTINCT m.user FROM Membership m WHERE m.discriminator = org.bfabric.entity.Membership.DISCRIMINATOR_CURRENT and m.container IN (SELECT p FROM Project p WHERE p.status IN (org.bfabric.enums.StatusEnum.RUNNING, org.bfabric.enums.StatusEnum.FINISHED))")
@NamedQuery(name = "User.findCurrentMembers", query = "SELECT DISTINCT m.user FROM Membership m WHERE m.discriminator = org.bfabric.entity.Membership.DISCRIMINATOR_CURRENT and m.role = org.bfabric.entity.Membership.ROLE_MEMBER")
@NamedQuery(name = "User.findCurrentManagers", query = "SELECT DISTINCT m.user FROM Membership m WHERE m.discriminator = org.bfabric.entity.Membership.DISCRIMINATOR_CURRENT and m.role = org.bfabric.entity.Membership.ROLE_MANAGER")
@NamedQuery(name = "User.findByServiceTypeId", query = "SELECT DISTINCT m.requester FROM Container m WHERE m.serviceType.id = :serviceTypeId")
@NamedQuery(name = "User.findCurrentMembersByContainerId", query = "SELECT DISTINCT m.user FROM Membership m WHERE m.container.id = :containerId and m.discriminator = org.bfabric.entity.Membership.DISCRIMINATOR_CURRENT and m.role = org.bfabric.entity.Membership.ROLE_MEMBER")
@NamedQuery(name = "User.findCurrentManagersByContainerId", query = "SELECT DISTINCT m.user FROM Membership m WHERE m.container.id = :containerId and m.discriminator = org.bfabric.entity.Membership.DISCRIMINATOR_CURRENT and m.role = org.bfabric.entity.Membership.ROLE_MANAGER")
@NamedQuery(name = "User.findByProjectTechnologies", query = "SELECT DISTINCT m.user FROM Membership m WHERE EXISTS(select p.id FROM Project p JOIN p.technologies t WHERE p = m.container AND t.name = :name)")
@NamedQuery(name = "User.findOrderBudgetOfficers", query = "SELECT DISTINCT a.budgetOfficer FROM Order a")
@NamedQuery(name = "User.findOrderRequesters", query = "SELECT DISTINCT a.requester FROM Order a")
@NamedQuery(name = "User.findContainerBudgetOfficers", query = "SELECT DISTINCT a.budgetOfficer FROM Container a")
@NamedQuery(name = "User.findByPotentiallyDeletable", query = "SELECT a FROM User a WHERE a.empDegree is null AND created < current_date - 1092 AND NOT EXISTS (SELECT m.id FROM Membership m WHERE m.user.id = a.id) AND NOT EXISTS (SELECT role.id FROM a.roles role where role.name in ('user', 'employee', 'alumni', 'internal', 'feeder', 'admin')) ORDER BY a.id")
@NamedQuery(name = "User.checkUniqueLogin", query = "SELECT a.id FROM User a WHERE lower(a.login) = lower(:login) and a.id <> :id")
@NamedQuery(name = "User.checkUniqueEmail", query = "SELECT a.id FROM User a WHERE lower(a.email) = lower(:email) and a.id <> :id")
@NamedQuery(name = "User.checkUniqueAccessCardNumber", query = "SELECT a.id FROM User a WHERE a.accessCardNumber = :accessCardNumber and a.id <> :id")
@NamedQuery(name = "User.checkUniqueAccessCardCode", query = "SELECT a.id FROM User a WHERE a.accessCardCode = :accessCardCode and a.id <> :id")
@NamedQuery(name = "User.resetAvailable", query = "update User a set a.available = true where a.available = false")
@NamedQuery(name = "User.setNotAvailable", query = "update User a set a.available = false where a.empDegree > 0 and exists(SELECT e.id FROM Event e WHERE e.eventType.name in ('Business Trip', 'Childbirth', 'Maternity Leave', 'Childcare', 'Compensation', 'Funeral', 'Home Schooling', 'Long Term Sickness', 'Military Service', 'Move', 'Nursing', 'Parttime Absence', 'Official Appointment', 'Sickness', 'Unpaid Vacation', 'Vacation', 'Wedding') and e.startDate <= :time and e.endDate >= :time and e.user.id=a.id)")
@NamedQuery(name = "User.setAvailableByUserId", query = "update User a set a.available = :available where a.id = :userId and a.empDegree > 0")
public class User extends AbstractAccessCardProfile implements ShowScreen, Indexable, Mergeable, HasAffiliation, Dashboard {

    private static final long serialVersionUID = 1;

    protected LocalDateTime computerLoginValidityChecked;

    @OneToMany(mappedBy = "user")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<AccessRequest> accessRequests = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean accountEnabled = true;

    @Transient
    private BigDecimal accountedDays;

    @OneToMany(mappedBy = "user")
    @Where(clause = "accountedDays > 0")
    @OrderBy("startDate")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<Event> accountedEvents = new ArrayList<>();

    @OneToMany(mappedBy = "acknowledgedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Feedback> acknowledgedFeedbacks = new HashSet<>();

    @OneToMany(mappedBy = "admin")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> adminInstruments = new HashSet<>();

    @Transient
    private Boolean affiliatedWithUZH = Boolean.TRUE;

    @Transient
    private boolean affiliationAcknowledged = false;

    @OneToMany(mappedBy = "approvedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Contract> approvedContracts = new HashSet<>();

    @Transient
    private Set<Order> associatedOrders;

    @Transient
    private Set<Project> associatedProjects;

    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backupid")
    @XmlIDREF
    private User backup;

    @OneToMany(mappedBy = "backup")
    @OrderBy("lastName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> backupOf = new HashSet<>();

    @OneToMany(mappedBy = "bioinformatician")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> bioinformaticianContainers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumentbooker", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "instrumentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> bookableInstruments = new HashSet<>();

    @OneToMany(mappedBy = "booker")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReservation> bookedInstrumentReservations = new HashSet<>();

    @OneToMany(mappedBy = "bookingIssuer")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Booking> bookings = new HashSet<>();

    @Transient
    private Integer budgetOfficerChargeCount;

    @OneToMany(mappedBy = "budgetOfficer")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> budgetOfficerContainers = new HashSet<>();

    @OneToMany(mappedBy = "charger")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> chargerCharges = new HashSet<>();

    @OneToMany(mappedBy = "coachBackup")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> coachBackupContainers = new HashSet<>();

    @OneToMany(mappedBy = "coach")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> coachContainers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "commentacknowledgedbyuser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "commentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Comment> commentsAcknowledgedBy = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "commentstarredbyuser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "commentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Comment> commentsStarredBy = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "commentviewedbyuser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "commentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Comment> commentsViewedBy = new HashSet<>();

    @Transient
    private Company company;

    @Transient
    private String companyName;

    /**
     * The computerLoginActivated flag. Set to false when a new user entity is created. When there is a reason to also create an account on Active Directory for this user (for example, when the user is
     * added to a project), a new ExternalJob is created and the master_user executable is triggered. This executable assures that the AD account is created and updates the mentioned ExternalJob
     * entity by setting the status to "done" if the creation was successful or something else in case the creation failed. If the status of the ExternalJob is set to "done", then the system will
     * assume that the account creation was successful and the "computerLoginActivated" attribute of the User entity will be set to true.
     */
    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean computerLoginActivated = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean computerLoginEnabled = false;

    @OneToMany(mappedBy = "contact")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> contactContainers = new HashSet<>();

    @Transient
    private Set<Long> containerIds;

    @OneToMany(mappedBy = "requester")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> containersRequested = new HashSet<>();

    @Transient
    private Set<Container> containersTransitive;

    @OneToMany(mappedBy = "user")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Credit> credits = new HashSet<>();

    @Transient
    private BigDecimal creditsRemaining;

    @Transient
    private BigDecimal creditsTotal;

    @Transient
    private BigDecimal creditsYearly;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean dataAccessEnabled = false;

    @Transient
    private Integer datasetCount;

    @Column(columnDefinition = "smallint")
    @Min(0)
    @Max(20)
    @XmlElement
    private Integer defaultDataScrollerChunkSize;

    @Column(columnDefinition = "smallint")
    @Min(0)
    @Max(200)
    @XmlElement
    private Integer defaultListingRows;

    @Column(columnDefinition = "smallint")
    @Min(0)
    @Max(200)
    @XmlElement
    private Integer defaultParentSamplesMaximumDisplayAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defaulttechnologyid")
    @XmlIDREF
    private Technology defaultTechnology;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defaultusergroupid")
    @XmlIDREF
    private UserGroup defaultUserGroup;

    @Transient
    private Department department;

    @Transient
    private String departmentName;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean disableNotificationExclusion;

    @ManyToMany
    @JoinTable(name = "containerdiscussedwith", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "containerid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> discussedContainers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisionid")
    @XmlIDREF
    private Division division;

    @Transient
    private String divisionName;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean downloadDirectoryEditable;

    @Column(columnDefinition = "character varying(128) DEFAULT 'Downloads'")
    @NotBlank
    @Size(max = 128)
    @XmlElement
    private String downloadDirectoryPath = Messages.get("configureDownloadDirectoryPath");

    @Column(columnDefinition = "character varying(1) DEFAULT 'b'")
    @NotBlank
    @Size(max = 1)
    @XmlElement
    private String downloadDirectoryStructure = DownloadDirectoryStructureEnum.BFABRIC.getCode();

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean downloadManagerEnabled;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean emailActive = true;

    @Transient
    private boolean emailChanged = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean emailVerified = false;

    @Min(0)
    @Max(100)
    @XmlElement
    private Integer empDegree;

    @Transient
    private Boolean empDegreeConsistent;

    @Transient
    private Boolean employee;

    @OneToMany(mappedBy = "user")
    @OrderBy("startDate desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Event> events = new HashSet<>();

    @Size(max = 256)
    @XmlElement
    private String exportBookingPath;

    @OneToMany(mappedBy = "user")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Feedback> feedbacks = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @OrderBy("startDate desc")
    @Where(clause = "startDate >= current_date")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Event> futureEvents = new HashSet<>();

    @Transient
    private Boolean hasExtensibleContainer;

    @Transient
    private Boolean hasExtensibleContainerForSampleCreation;

    @Transient
    private Boolean hasNoOrderAssignableProject;

    @Embedded
    @XmlElement
    private HomeAddress homeAddress;

    @Embedded
    @XmlElement
    private HomePhoneNumber homePhoneNumber = null;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "imageid")
    @XmlIDREF
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituteid")
    @XmlIDREF
    private Institute institute;

    @Transient
    private String instituteName;

    @OneToMany(mappedBy = "user")
    @OrderBy("startDate desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReservation> instrumentReservations = new HashSet<>();

    @Column(columnDefinition = "integer DEFAULT 0")
    @NotNull
    private int invalidLoginAttempts;

    private LocalDateTime lastActionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lastcontainerid")
    private Container lastContainer;

    private LocalDateTime lastLoginDate;

    @OneToMany(mappedBy = "leader")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> leaderContainers = new HashSet<>();

    @Transient
    private Boolean loggedIn;

    @NaturalId(mutable = true)
    @NotBlank
    @Size(max = 32)
    @XmlElement
    private String login;

    @ManyToMany
    @JoinTable(name = "mailrecipient", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "mailid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Mail> mails = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean massMailEnabled = true;

    @Transient
    private List<User> matchingUsers;

    @OneToMany(mappedBy = "user", cascade = { CascadeType.MERGE }, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Membership> memberships = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "discriminator = '" + Membership.DISCRIMINATOR_CURRENT + "'")
    private Set<Membership> membershipsCurrent;

    @OneToMany(mappedBy = "user")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "discriminator = '" + Membership.DISCRIMINATOR_FORMER + "'")
    private Set<Membership> membershipsFormer;

    @Transient
    private long mergeAffiliationUserId;

    @OneToMany(mappedBy = "charger")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OfferedCharge> offeredCharges = new HashSet<>();

    @OneToMany(mappedBy = "requester")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Offer> offers = new HashSet<>();

    @OneToMany(mappedBy = "operator")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReservation> operatedInstrumentReservations = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumentoperator", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "instrumentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> operatorInstruments = new HashSet<>();

    @OneToMany(mappedBy = "orderItemReceivedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> orderItemReceivedByPurchases = new HashSet<>();

    @Transient
    private Set<Order> orders;

    @Transient
    private List<Order> ordersTransitive;

    @Transient
    private Organization organization;

    @Transient
    private String organizationName;

    @Transient
    private OrganizationType organizationType;

    @Transient
    private String organizationTypeName;

    @NotBlank
    @Size(min = 32, max = 96)
    private String password;

    @Size(max = 1024)
    private String passwordAD;

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Plate> plates = new HashSet<>();

    @Column(length = 64)
    @Size(max = 64)
    @Email
    @XmlElement
    private String privateEmail;

    @Transient
    private Set<Project> projects;

    @Transient
    private Set<Project> projectsFormer;

    @Transient
    private List<Event> publicEvents;

    @OneToMany(mappedBy = "orderedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> purchases = new HashSet<>();

    @Transient
    private BigDecimal remainingDays;

    @OneToMany(mappedBy = "requester")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> requesterContainers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "userresourcebasket", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "resourcebasketid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ResourceBasket> resourceBaskets = new HashSet<>();

    @Transient
    private Integer resourceCount;

    @Transient
    private List<String> roleNamesImplicit;

    @ManyToMany
    @JoinTable(name = "userrole", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "roleid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<Role> roles = new HashSet<>();

    @Transient
    private List<Role> rolesImplicit;

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Run> runs = new HashSet<>();

    @Transient
    private Integer sampleCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selectedresourcebasketid")
    @XmlIDREF
    private ResourceBasket selectedResourceBasket;

    @ManyToMany
    @JoinTable(name = "serviceareauser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "serviceareaid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceArea> serviceAreas = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "servicetypeuser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypes = new HashSet<>();

    @OneToMany(mappedBy = "coach")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypesCoach = new HashSet<>();

    @OneToMany(mappedBy = "coachBackup")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypesCoachBackup = new HashSet<>();

    @Transient
    private Set<ServiceType> serviceTypesForTasks;

    @ManyToMany
    @JoinTable(name = "serviceuser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "serviceid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Service> services = new HashSet<>();

    @Size(min = 8, max = 32)
    @XmlElement
    private String shibbolethId;

    @XmlElement
    private LocalDate shibbolethLastLoginDate;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean showSamplesLaneButton;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean showSamplesLaneButtonAndCheckbox;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean showSamplesLaneSeparated;

    @Size(max = 4096)
    private String sshPublicKey;

    @OneToMany(mappedBy = "statusModifiedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> statusModifiedContainers = new HashSet<>();

    @OneToMany(mappedBy = "statusModifiedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Executable> statusModifiedExecutables = new HashSet<>();

    @OneToMany(mappedBy = "statusModifiedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ExternalJob> statusModifiedExternalJobs = new HashSet<>();

    @OneToMany(mappedBy = "statusModifiedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Plate> statusModifiedPlates = new HashSet<>();

    @OneToMany(mappedBy = "statusModifiedBy")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Run> statusModifiedRuns = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> supervisedApplications = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Contract> supervisedContracts = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Executable> supervisedExecutables = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> supervisedInstruments = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Storage> supervisedStorages = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Submitter> supervisedSubmitters = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<UserGroup> supervisedUserGroups = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowStep> supervisedWorkflowSteps = new HashSet<>();

    @OneToMany(mappedBy = "supervisor")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WrapperCreator> supervisedWrapperCreators = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technologyid")
    @XmlIDREF
    private Technology technology;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technologyHeadId")
    @XmlIDREF
    private Technology technologyHead;

    @ManyToMany
    @JoinTable(name = "usertrackedcontainer", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "containerid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<Container> trackedContainers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "usertrackedservice", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "serviceid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<Service> trackedServices = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumenttraineduser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "instrumentid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> trainedInstruments = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "userbillinginfoid")
    @XmlIDREF
    private UserBillingInfo userBillingInfo;

    @ManyToMany
    @JoinTable(name = "usergroupuser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "usergroupid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<UserGroup> userGroups = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "purchaseuser", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "purchaseid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> userPurchases = new HashSet<>();

    @Transient
    private List<WorkflowStep> workflowSteps;

    @Transient
    private List<Workflow> workflows;

    @Transient
    private Integer workunitCount;

    public User() {
        setAddress(new Address());
        setHomeAddress(new HomeAddress());
        setPhoneNumber(new PhoneNumber());
        setHomePhoneNumber(new HomePhoneNumber());
    }

    public void accessCardCodeChanged(ValueChangeEvent event) {
        setAccessCardCode((String) event.getNewValue());
    }

    public void accessCardNumberChanged(ValueChangeEvent event) {
        setAccessCardNumber((String) event.getNewValue());
    }

    public void addRole(Role newRole) {
        if (newRole != null) {
            getRoles().add(newRole);
            checkAndSetMassMailEnabled();
        }
    }

    public void addRoleUser() {
        addRole(getRoleUser());
    }

    public void affiliatedWithUZHChanged(ValueChangeEvent event) {
        setAffiliatedWithUZH((Boolean) event.getNewValue());
    }

    public void anonymize() {
        if (isAnonymizeRendered()) {
            setTitle(null);
            setFirstName(Constants.X);
            setLastName(Constants.X);
            setEmail(getId() + StringHelper.EMAIL_XX);
            if (getPhoneNumber() != null) {
                getPhoneNumber().setCountryCode(41);
                getPhoneNumber().setAreaCode(1);
                getPhoneNumber().setLocalNumber(1);
            }
            if (getAddress() != null) {
                getAddress().setStreet(Constants.X);
                getAddress().setZip("1");
                getAddress().setCity(Constants.X);
                getAddress().setCountry(CDI.current().select(CountryService.class).get().getCountryDefault());
                getAddress().setRoom(null);
            }
            setDivision(CDI.current().select(DivisionService.class).get()
                .getDivisionByNameAndCompanyNameAndCreateIfNotExists(getConfiguration().getDefaultDivision(), getConfiguration().getDefaultCompanyName()));
            setOrganizationType(getDivision().getCompany().getOrganizationType());
            setCompanyName(getDivision().getCompany().getName());
            setOrganization(null);
            setDepartment(null);
            setInstitute(null);
            clearPrivateInfo();
            resetAccessCard();
            setUserBillingInfo(null);
            setImage(null);
            setDescription(null);
        }
    }

    public void checkAndSetMassMailEnabled() {
        if (!isMassMailEnabled() && hasRoleEmployee()) {
            setMassMailEnabled(true);
        }
    }

    public void checkComputerLoginValidity() {
        setComputerLoginValidityChecked(LocalDateTime.now());
    }

    public LinkedHashMap<String, String> checkDownloadDirectoryPathValidity() {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        try {
            // Check whether the path is valid for unix, windows, and osX.
            Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix()).getPath(getDownloadDirectoryPath());
            Jimfs.newFileSystem(com.google.common.jimfs.Configuration.windows()).getPath(getDownloadDirectoryPath());
            Jimfs.newFileSystem(com.google.common.jimfs.Configuration.osX()).getPath(getDownloadDirectoryPath());
        } catch (Exception e) {
            validationErrorMsg.put("settings:downloadDirectoryPath", Messages.get("pathValidationException"));
        }
        return validationErrorMsg;
    }

    public void clearBillingAccount() {
        getUserBillingInfo().setAddress(new Address());
        getUserBillingInfo().setSalutation(null);
        getUserBillingInfo().setTitle(null);
        getUserBillingInfo().setFirstName(null);
        getUserBillingInfo().setLastName(null);
        getUserBillingInfo().setEmail(null);
        getUserBillingInfo().setCostCentre(null);
        getUserBillingInfo().setDivision(null);
        getUserBillingInfo().setInstitute(null);
        getUserBillingInfo().setPspElement(null);
        getUserBillingInfo().setFinanceSourceEth(null);
        getUserBillingInfo().setEuGrant(null);
        getUserBillingInfo().setReferenceNumber(null);
        getUserBillingInfo().setOrganizationType(null);
        getUserBillingInfo().setOrganization(null);
        getUserBillingInfo().setDepartment(null);
    }

    public void clearPrivateInfo() {
        if (isClearPrivateInfoRendered()) {
            setHomeAddress(new HomeAddress());
            setHomePhoneNumber(new HomePhoneNumber());
            setBirthDate(null);
        }
    }

    public void clearPrivateInfoAddress() {
        getHomeAddress().setStreet(null);
        getHomeAddress().setZip(null);
        getHomeAddress().setCity(null);
        getHomeAddress().setCountry(null);
    }

    @Override
    public User clone() throws CloneNotSupportedException {
        User clone = (User) super.clone();
        clone.userBillingInfo = null;
        clone.image = null;

        // Reset associations
        clone.accessRequests = new HashSet<>();
        clone.acknowledgedFeedbacks = new HashSet<>();
        clone.adminInstruments = new HashSet<>();
        clone.approvedContracts = new HashSet<>();
        clone.bioinformaticianContainers = new HashSet<>();
        clone.bookableInstruments = new HashSet<>();
        clone.bookedInstrumentReservations = new HashSet<>();
        clone.bookings = new HashSet<>();
        clone.budgetOfficerContainers = new HashSet<>();
        clone.chargerCharges = new HashSet<>();
        clone.coachBackupContainers = new HashSet<>();
        clone.coachContainers = new HashSet<>();
        clone.contactContainers = new HashSet<>();
        clone.containersRequested = new HashSet<>();
        clone.credits = new HashSet<>();
        clone.discussedContainers = new HashSet<>();
        clone.events = new HashSet<>();
        clone.feedbacks = new HashSet<>();
        clone.instrumentReservations = new HashSet<>();
        clone.leaderContainers = new HashSet<>();
        clone.mails = new HashSet<>();
        clone.memberships = new HashSet<>();
        clone.membershipsCurrent = new HashSet<>();
        clone.membershipsFormer = new HashSet<>();
        clone.operatorInstruments = new HashSet<>();
        clone.offeredCharges = new HashSet<>();
        clone.offers = new HashSet<>();
        clone.plates = new HashSet<>();
        clone.purchases = new HashSet<>();
        clone.orderItemReceivedByPurchases = new HashSet<>();
        clone.resourceBaskets = new HashSet<>();
        clone.roles = new HashSet<>();
        clone.runs = new HashSet<>();
        clone.serviceAreas = new HashSet<>();
        clone.services = new HashSet<>();
        clone.serviceTypes = new HashSet<>();
        clone.serviceTypesCoach = new HashSet<>();
        clone.serviceTypesCoachBackup = new HashSet<>();
        clone.serviceTypesForTasks = new HashSet<>();
        clone.statusModifiedContainers = new HashSet<>();
        clone.statusModifiedExecutables = new HashSet<>();
        clone.statusModifiedPlates = new HashSet<>();
        clone.statusModifiedRuns = new HashSet<>();
        clone.statusModifiedExternalJobs = new HashSet<>();
        clone.supervisedApplications = new HashSet<>();
        clone.supervisedContracts = new HashSet<>();
        clone.supervisedExecutables = new HashSet<>();
        clone.supervisedInstruments = new HashSet<>();
        clone.supervisedStorages = new HashSet<>();
        clone.supervisedSubmitters = new HashSet<>();
        clone.supervisedUserGroups = new HashSet<>();
        clone.supervisedWrapperCreators = new HashSet<>();
        clone.trackedServices = new HashSet<>();
        clone.trackedContainers = new HashSet<>();
        clone.trainedInstruments = new HashSet<>();
        clone.userGroups = new HashSet<>();

        return clone;
    }

    public char[] createLoginAndPassword() {
        setLogin(generateLoginName());
        char[] generatedPassword = BfabricPasswordGenerator.generatePassword(64);
        setPassword(generatedPassword);
        return generatedPassword;
    }

    public User createMergeSelection(User merged) throws CloneNotSupportedException {
        User mergeSelection = clone();
        mergeSelection.setMergeAffiliationUserId(getId());

        // In the case of optional attributes, pre-select the non-empty ones.
        if (merged != null) {
            if (StringHelper.isEmpty(mergeSelection.getDescription())) {
                mergeSelection.setDescription(merged.getDescription());
            }
            if (StringHelper.isEmpty(mergeSelection.getTitle())) {
                mergeSelection.setTitle(merged.getTitle());
            }
            if (mergeSelection.getAddress() == null || mergeSelection.getAddress().isEmpty()) {
                mergeSelection.setAddress(merged.getAddress());
            }
            if (mergeSelection.getHomeAddress() == null || mergeSelection.getHomeAddress().isEmpty()) {
                mergeSelection.setHomeAddress(merged.getHomeAddress());
            }
            if (mergeSelection.getPhoneNumber() == null || mergeSelection.getPhoneNumber().isEmpty()) {
                mergeSelection.setPhoneNumber(merged.getPhoneNumber());
            }
            if (mergeSelection.getHomePhoneNumber() == null || mergeSelection.getHomePhoneNumber().isEmpty()) {
                mergeSelection.setHomePhoneNumber(merged.getHomePhoneNumber());
            }
            if (mergeSelection.getBirthDate() == null) {
                mergeSelection.setBirthDate(merged.getBirthDate());
            }
            if (mergeSelection.getEmpDegree() == null) {
                mergeSelection.setEmpDegree(merged.getEmpDegree());
            }
            if (mergeSelection.getAccessCardNumber() == null) {
                mergeSelection.setAccessCardNumber(merged.getAccessCardNumber());
            }
            if (mergeSelection.getLastLoginDate() == null || merged.getLastLoginDate() != null && mergeSelection.getLastLoginDate().isBefore(merged.getLastLoginDate())) {
                mergeSelection.setLastLoginDate(merged.getLastLoginDate());
            }
            mergeSelection.setMassMailEnabled(merged.getMassMailEnabled());
            if (mergeSelection.getDefaultTechnology() == null) {
                mergeSelection.setDefaultTechnology(merged.getDefaultTechnology());
            }
            if (mergeSelection.getDefaultUserGroup() == null) {
                mergeSelection.setDefaultUserGroup(merged.getDefaultUserGroup());
            }
            if (mergeSelection.getDownloadDirectoryPath() == null) {
                mergeSelection.setDownloadDirectoryPath(merged.getDownloadDirectoryPath());
            }
            if (getUserBillingInfo() == null) {
                mergeSelection.setUserBillingInfo(merged.getUserBillingInfo());
            } else {
                mergeSelection.setUserBillingInfo(getUserBillingInfo());
            }
            if (getImage() == null) {
                mergeSelection.setImage(merged.getImage());
            } else {
                mergeSelection.setImage(getImage());
            }
        }
        return mergeSelection;
    }

    public void downloadManagerEnabledChanged(ValueChangeEvent event) {
        setDownloadManagerEnabled((Boolean) event.getNewValue());
    }

    public void emailChangeListener(ValueChangeEvent event) {
        if (!((String) event.getNewValue()).trim().equals(getEmail())) {
            setEmailChanged(true);
        }
    }

    public void employeeEntry() {
        getRoles().add(getRoleEmployee());
        getRoles().add(getRoleAgendaUser());
        getRoles().remove(getRoleAlumni());
    }

    public void employeeLeave() {
        setEmpDegree(null);
        setBackup(null);
        setTechnology(null);
        setTechnologyHead(null);
        revokeEmployeeRights();
        getRoles().add(getRoleAlumni());
    }

    public void exportAndDownloadIcs(Set<AbstractEvent> abstractEvents) {
        Set<AbstractEvent> exportEvents = new HashSet<>();
        if (abstractEvents != null && !abstractEvents.isEmpty()) {
            exportEvents.addAll(abstractEvents);
        } else {
            exportEvents.addAll(getInstrumentReservations());
        }
        Event event = new Event();
        event.download(getClassLabelLowerCaseId() + "_events.ics", event.getIcsExport(exportEvents).toString());
    }

    public String generateCode(String uniquePrefix) {
        StringBuilder code = new StringBuilder(uniquePrefix);
        if (StringHelper.isNotEmpty(getLogin())) {
            code.append(getLogin());
        }
        if (StringHelper.isNotEmpty(getEmail())) {
            code.append(getEmail());
        }
        return DigestUtils.md5Hex(code.toString());
    }

    public String generateLoginName() {
        String loginChars = "abcdefghijklmonpqrstuvwxyz";

        String transformedLastName = transformNameToLogin(getLastName());

        // 1) The login name is the last name
        String loginName = getAvailableLogin(transformedLastName);
        if (loginName != null) {
            return loginName;
        }

        String transformedFirstName = transformNameToLogin(getFirstName());

        // 2) If not possible, then the login name is the combination of last and first name
        loginName = getAvailableLogin(transformedLastName + transformedFirstName);
        if (loginName != null) {
            return loginName;
        }

        // 3) If not possible, then the login name is the combination of first and last name
        loginName = getAvailableLogin(transformedFirstName + transformedLastName);
        if (loginName != null) {
            return loginName;
        }

        // 4) Fallback: the login name is the combination of the last name (restricted to 28 characters) and a generated a random string
        String prefix = transformedLastName.length() > 28 ? transformedLastName.substring(0, 27) : transformedLastName;
        outerLoop:
        for (int i = 0; i < loginChars.length(); i++) {
            for (int j = i; j < i + 32 - prefix.length(); j++) {
                loginName = getAvailableLogin(prefix + loginChars.substring(i, j));
                if (loginName != null) {
                    break outerLoop;
                }
            }
        }

        return loginName;
    }

    public Set<AccessRequest> getAccessRequests() {
        return accessRequests;
    }

    public BigDecimal getAccountedDays() {
        if (accountedDays == null) {
            accountedDays = getAccountedDaysByYear(LocalDate.now().getYear());
        }
        return accountedDays;
    }

    public BigDecimal getAccountedDaysByYear(int year) {
        BigDecimal accountedDays = BigDecimal.ZERO;
        for (Event event : getAccountedEventsByYear(year)) {
            accountedDays = accountedDays.add(event.getAccountedDays());
        }
        return accountedDays;
    }

    public List<Event> getAccountedEvents() {
        return accountedEvents;
    }

    public List<Event> getAccountedEventsByYear(int year) {
        List<Event> accountedEventsForYear = new ArrayList<>();
        for (Event event : getAccountedEvents()) {
            if (event.isInYear(year)) {
                accountedEventsForYear.add(event);
            }
        }
        return accountedEventsForYear;
    }

    public Set<Feedback> getAcknowledgedFeedbacks() {
        return acknowledgedFeedbacks;
    }

    @SuppressWarnings("unused")
    public String getActivateUrl() {
        return getUrlWithCodeParameter("activate", getActivationCode());
    }

    public String getActivationCode() {
        return getGeneratedCode();
    }

    public Set<Instrument> getAdminInstruments() {
        return adminInstruments;
    }

    public Boolean getAffiliatedWithUZH() {
        return affiliatedWithUZH;
    }

    public String getAffiliation() {
        String affiliation = Constants.EMPTY_STRING;
        if (getInstitute() != null) {
            affiliation = getInstitute().getAffiliation();
        } else if (getDivision() != null) {
            affiliation = getDivision().getAffiliation();
        }
        return affiliation;
    }

    public List<String> getAffiliationAsList() {
        List<String> strList = new ArrayList<>();
        if (getInstitute() != null) {
            strList = getInstitute().getAffiliationAsList();
        } else if (getDivision() != null) {
            strList = getDivision().getAffiliationAsList();
        }
        return strList;
    }

    public Set<Container> getAllTrackedContainers() {
        Set<Container> allTrackedContainers = new HashSet<>();
        allTrackedContainers.addAll(getTrackedContainers());
        allTrackedContainers.addAll(getCoachedContainers());
        allTrackedContainers.addAll(getBioinformaticianContainers());
        return allTrackedContainers;
    }

    public Set<Contract> getApprovedContracts() {
        return approvedContracts;
    }

    public Set<Order> getAssociatedOrders() {
        if (associatedOrders == null) {
            associatedOrders = new HashSet<>();
            associatedOrders.addAll(getOrdersTransitive());
            associatedOrders.addAll(getCoachedOrders());
            associatedOrders.addAll(getBioinformaticianOrders());
        }
        return associatedOrders;
    }

    public Set<Project> getAssociatedProjects() {
        if (associatedProjects == null) {
            associatedProjects = new HashSet<>();
            associatedProjects.addAll(getProjects());
            associatedProjects.addAll(getCoachedProjects());
            associatedProjects.addAll(getBioinformaticianProjects());
        }
        return associatedProjects;
    }

    public Boolean getAvailable() {
        return available;
    }

    public String getAvailableLogin(String username) {
        if (StringHelper.isNotEmpty(username)) {
            String loginName = username.length() > 32 ? username.substring(0, 31) : username;
            if (getUserService().checkUniqueLogin(this, loginName) && !getUserService().isLoginBlacklisted(loginName)) {
                return loginName;
            }
        }
        return null;
    }

    public User getBackup() {
        return backup;
    }

    public Set<User> getBackupOf() {
        return backupOf;
    }

    public Set<Container> getBioinformaticianContainers() {
        return bioinformaticianContainers;
    }

    public Set<Order> getBioinformaticianOrders() {
        Set<Order> allBioinformaticianOrders = new HashSet<>();
        for (Container container : getBioinformaticianContainers()) {
            if (!container.isContainerProject()) {
                allBioinformaticianOrders.add((Order) container);
            }
        }
        return allBioinformaticianOrders;
    }

    public Set<Project> getBioinformaticianProjects() {
        Set<Project> allBioinformaticianProjects = new HashSet<>();
        for (Container container : getBioinformaticianContainers()) {
            if (container.isContainerProject()) {
                allBioinformaticianProjects.add((Project) container);
            }
        }
        return allBioinformaticianProjects;
    }

    public Set<Instrument> getBookableInstruments() {
        return bookableInstruments;
    }

    public Set<InstrumentReservation> getBookedInstrumentReservations() {
        return bookedInstrumentReservations;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public Integer getBudgetOfficerChargeCount() {
        if (budgetOfficerChargeCount == null) {
            budgetOfficerChargeCount = CDI.current().select(ChargeList.class).get().getLazyModelByContainerBudgetOfficerId(getId()).getSize();
        }
        return budgetOfficerChargeCount;
    }

    public Set<Container> getBudgetOfficerContainers() {
        return budgetOfficerContainers;
    }

    public List<Container> getChargebleTrackedContainers() {
        List<Container> trackableChargableContainers = new ArrayList<>();
        for (Container container : getCurrentUser().getTrackedContainers()) {
            if (container.isChargeable()) {
                trackableChargableContainers.add(container);
            }
        }
        return trackableChargableContainers;
    }

    public Set<Charge> getChargerCharges() {
        return chargerCharges;
    }

    public Set<Container> getCoachBackupContainers() {
        return coachBackupContainers;
    }

    public Set<Container> getCoachContainers() {
        return coachContainers;
    }

    public Set<Container> getCoachedContainers() {
        Set<Container> allCoachedContainers = new HashSet<>();
        allCoachedContainers.addAll(coachContainers);
        allCoachedContainers.addAll(coachBackupContainers);
        return allCoachedContainers;
    }

    public Set<Order> getCoachedOrders() {
        Set<Order> allCoachedOrders = new HashSet<>();
        for (Container container : getCoachedContainers()) {
            if (!container.isContainerProject()) {
                allCoachedOrders.add((Order) container);
            }
        }
        return allCoachedOrders;
    }

    public Set<Project> getCoachedProjects() {
        Set<Project> allCoachedProjects = new HashSet<>();
        for (Container container : getCoachedContainers()) {
            if (container.isContainerProject()) {
                allCoachedProjects.add((Project) container);
            }
        }
        return allCoachedProjects;
    }

    public Set<Comment> getCommentsAcknowledgedBy() {
        return commentsAcknowledgedBy;
    }

    public Set<Comment> getCommentsStarredBy() {
        return commentsStarredBy;
    }

    public Set<Comment> getCommentsViewedBy() {
        return commentsViewedBy;
    }

    @Override
    public Company getCompany() {
        return company;
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    public LocalDateTime getComputerLoginValidityChecked() {
        return computerLoginValidityChecked;
    }

    public Set<Container> getContactContainers() {
        return contactContainers;
    }

    public Set<Long> getContainerIds() {
        if (containerIds == null) {
            containerIds = CDI.current().select(MembershipService.class).get().getCurrentContainerIdsByUserId(this.getId());
            if (!containerIds.isEmpty()) {
                Set<Long> orderIds = CDI.current().select(ContainerService.class).get().getOrderIdsByContainerIds(containerIds);
                if (!orderIds.isEmpty()) {
                    containerIds.addAll(orderIds);
                }
            }
        }
        return containerIds;
    }

    public Set<Container> getContainers() {
        return getContainers(getMembershipsCurrent());
    }

    public Set<Container> getContainers(Set<Membership> membershipSet) {
        Set<Container> containers = new HashSet<>();
        if (membershipSet != null) {
            for (Membership membership : membershipSet) {
                containers.add(membership.getContainer());
            }
        }
        return containers;
    }

    public Set<Container> getContainersAll() {
        return getContainers(getMemberships());
    }

    public Set<Container> getContainersFormer() {
        return getContainers(getMembershipsFormer());
    }

    public Set<Container> getContainersRequested() {
        return containersRequested;
    }

    public Set<Container> getContainersTransitive() {
        if (containersTransitive == null) {
            containersTransitive = new HashSet<>();
            if (!getContainers().isEmpty()) {
                containersTransitive.addAll(getContainers());
            }
            if (!getOrdersTransitive().isEmpty()) {
                containersTransitive.addAll(getOrdersTransitive());
            }
        }
        return containersTransitive;
    }

    public Set<Contract> getContracts() {
        return getSupervisedContracts();
    }

    public Set<Credit> getCredits() {
        return credits;
    }

    public Set<Credit> getCreditsByYear(int year) {
        Set<Credit> yearCredits = new HashSet<>();
        for (Credit credit : getCredits()) {
            if (credit.isInYear(year)) {
                yearCredits.add(credit);
            }
        }
        return yearCredits;
    }

    public BigDecimal getCreditsRemaining() {
        if (creditsRemaining == null) {
            creditsRemaining = getCreditsRemainingByYear(LocalDate.now().getYear());
        }
        return creditsRemaining;
    }

    public BigDecimal getCreditsRemainingByYear(int year) {
        return getCreditsTotalByYear(year).subtract(getAccountedDaysByYear(year));
    }

    public BigDecimal getCreditsTotal() {
        if (creditsTotal == null) {
            creditsTotal = getCreditsTotalByYear(LocalDate.now().getYear());
        }
        return creditsTotal;
    }

    public BigDecimal getCreditsTotalByYear(int year) {
        BigDecimal creditsTotal = BigDecimal.ZERO;
        for (Credit credit : getCredits()) {
            if (credit.isInYear(year)) {
                creditsTotal = creditsTotal.add(credit.getDays());
            }
        }
        return creditsTotal;
    }

    public BigDecimal getCreditsYearly() {
        if (creditsYearly == null) {
            creditsYearly = getCreditsYearly(LocalDate.now().getYear());
        }
        return creditsYearly;
    }

    public BigDecimal getCreditsYearly(int year) {
        double creditsYearly = 0;
        if (isEmployee()) {
            creditsYearly = (isAboveAgeLimit(year) ? getConfiguration().getAnnualVacationCreditAboveAgeLimit() : getConfiguration().getAnnualVacationCreditBelowAgeLimit()) * getEmpDegree().doubleValue() / 100;
        }
        return BigDecimal.valueOf(creditsYearly).setScale(1, RoundingMode.HALF_EVEN);
    }

    public Set<Long> getCurrentAndRunningProjectIds() {
        return CDI.current().select(MembershipService.class).get().getCurrentAndRunningProjectIdsByUser(this);
    }

    public int getDataScrollerChunkSize() {
        return getDefaultDataScrollerChunkSize() != null ? getDefaultDataScrollerChunkSize() : getConfiguration().getDataScrollerChunkSize();
    }

    public Integer getDatasetCount() {
        if (datasetCount == null) {
            datasetCount = CDI.current().select(DatasetList.class).get().getLazyModelContainerDependentByContainerIds(getContainerIds()).getSize();
        }
        return datasetCount;
    }

    public Integer getDefaultDataScrollerChunkSize() {
        return defaultDataScrollerChunkSize;
    }

    public Integer getDefaultListingRows() {
        return defaultListingRows;
    }

    public Integer getDefaultParentSamplesMaximumDisplayAmount() {
        return defaultParentSamplesMaximumDisplayAmount;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.USERMANAGER;
    }

    public Technology getDefaultTechnology() {
        return defaultTechnology;
    }

    public UserGroup getDefaultUserGroup() {
        return defaultUserGroup;
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Set<Container> getDiscussedContainers() {
        return discussedContainers;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getFirstName() + " " + getLastName();
    }

    @Override
    public Division getDivision() {
        return division;
    }

    @Override
    public String getDivisionName() {
        return divisionName;
    }

    public String getDownloadDirectoryPath() {
        return downloadDirectoryPath;
    }

    public String getDownloadDirectoryStructure() {
        return downloadDirectoryStructure;
    }

    @Override
    @NaturalId(mutable = true)
    public String getEmail() {
        return super.getEmail();
    }

    public Integer getEmpDegree() {
        return empDegree;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "name", getFullName());
        if (StringHelper.isNotEmpty(getLogin())) {
            addEntityInfoItem(summary, "login", getLogin());
        }
        addEntityInfoItem(summary, "email", getEmail());
        if (getInstitute() != null) {
            addEntityInfoItem(summary, "organizationType", getInstitute().getDepartment().getOrganization().getOrganizationType().getName());
            addEntityInfoItem(summary, "organization", getInstitute().getDepartment().getOrganization().getName());
            addEntityInfoItem(summary, "department", getInstitute().getDepartment().getName());
            addEntityInfoItem(summary, "institute", getInstitute().getName());
        }

        if (hasCurrentUserRoleEnum(RoleEnum.USERREADER)) {
            if (getDivision() != null) {
                addEntityInfoItem(summary, "organizationType", getDivision().getCompany().getOrganizationType().getName());
                addEntityInfoItem(summary, "company", getDivision().getCompany().getName());
                if (getDivision().isSet()) {
                    addEntityInfoItem(summary, "division", getDivision().getName());
                }
            }
            if (getAddress() != null && !getAddress().isEmpty()) {
                addEntityInfoItem(summary, "address", getAddress().getFullAddress());
            }
            addEntityInfoItem(summary, "phone", getPhone());
        }
        if (hasCurrentUserRoleEnum(RoleEnum.EMPLOYEEMANAGER)) {

            if (getHomeAddress() != null && !getHomeAddress().isEmpty()) {
                addEntityInfoItem(summary, "homeAddress", getHomeAddress().getFullAddress());
            }
            addEntityInfoItem(summary, "homePhone", getHomePhone());
            addEntityInfoItem(summary, "emailActive", isEmailActive());
            addEntityInfoItem(summary, "emailVerified", isEmailVerified());
            addEntityInfoItem(summary, "massMail", isMassMailEnabled());
            addEntityInfoItem(summary, "computerLoginActivated", isComputerLoginActivated());
            addEntityInfoItem(summary, "computerLoginEnabled", isComputerLoginEnabled());
            addEntityInfoItem(summary, "dataAccessEnabled", isDataAccessEnabled());
            addEntityInfoItem(summary, "invalidLoginAttempts", getInvalidLoginAttempts());
        }
        if (hasCurrentUserRoleEnum(RoleEnum.USERREADER)) {
            if (getLastLoginDate() != null) {
                addEntityInfoItem(summary, "lastLoginDate", getLastLoginDate());
            }
            if (getLastActionTime() != null) {
                addEntityInfoItem(summary, "lastActionTime", getLastActionTime());
            }
        }
        return summary.toString();
    }

    public Set<Event> getEvents() {
        return events;
    }

    public String getExportBookingPath() {
        return exportBookingPath;
    }

    public Set<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public String getFullAffiliation() {
        StringBuilder fullAffiliation = new StringBuilder();

        if (getFullHomeAddress() != null && !getFullHomeAddress().isEmpty()) {
            fullAffiliation.append(getFullHomeAddress()).append(", ");
        }
        if (getHomePhone() != null && !getHomePhone().isEmpty()) {
            fullAffiliation.append(getHomePhone()).append(", ");
        }

        fullAffiliation.append(getFullAddress()).append(", ");
        fullAffiliation.append(getPhone()).append(", ");
        fullAffiliation.append(getAffiliation());

        return fullAffiliation.toString();
    }

    public String getFullContactDetails() {
        StringBuilder fullContactDetails = new StringBuilder();

        fullContactDetails.append(getFullName()).append(", ");
        if (getBirthDate() != null) {
            fullContactDetails.append(DateUtils.getDateAsFormattedString(getBirthDate())).append(", ");
        }
        if (getAccessCardNumber() != null) {
            fullContactDetails.append(getAccessCardNumber()).append(", ");
        }
        if (getAccessCardCode() != null && !getAccessCardCode().trim().isEmpty()) {
            fullContactDetails.append(getAccessCardCode()).append(", ");
        }
        if (getAccessCardExpiryDate() != null) {
            fullContactDetails.append(DateUtils.getDateAsFormattedString(getAccessCardExpiryDate())).append(", ");
        }
        if (getFullHomeAddress() != null && !getFullHomeAddress().isEmpty()) {
            fullContactDetails.append(getFullHomeAddress()).append(", ");
        }
        if (getHomePhone() != null && !getHomePhone().isEmpty()) {
            fullContactDetails.append(getHomePhone()).append(", ");
        }

        fullContactDetails.append(getFullAddress()).append(", ");
        fullContactDetails.append(getPhone()).append(", ");
        fullContactDetails.append(getAffiliation());

        return fullContactDetails.toString();
    }

    public String getFullHomeAddress() {
        return getHomeAddress() != null ? getHomeAddress().getFullAddress() : null;
    }

    public String getFullNameLogin() {
        return getFullName() + " (" + getLogin() + ")";
    }

    public Set<Event> getFutureEvents() {
        return futureEvents;
    }

    public String getGeneratedCode() {
        return generateCode(getIdString() + " " + getCreated());
    }

    public HomeAddress getHomeAddress() {
        return homeAddress;
    }

    public String getHomePhone() {
        return getHomePhoneNumber() != null ? getHomePhoneNumber().getFullNumber() : null;
    }

    public HomePhoneNumber getHomePhoneNumber() {
        return homePhoneNumber;
    }

    public Image getImage() {
        return image;
    }

    public Set<Role> getImpliedRoles() {
        Set<Role> impliedRoles = new HashSet<>();
        for (Role role : getRoles()) {
            impliedRoles.addAll(role.getImpliedRoles());
        }
        return impliedRoles;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
        fields.add(IndexMapContentEnum.FIRSTNAME.getField());
        fields.add(IndexMapContentEnum.LASTNAME.getField());
        fields.add(IndexMapContentEnum.COMPANY.getField());
        fields.add(IndexMapContentEnum.DIVISION.getField());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.LOGIN.getField());
        fields.add(IndexMapContentEnum.ORGANIZATION.getField());
        fields.add(IndexMapContentEnum.DEPARTMENT.getField());
        fields.add(IndexMapContentEnum.INSTITUTE.getField());
        fields.add(IndexMapContentEnum.EMAIL.getField());
        fields.add(IndexMapContentEnum.ADDRESS.getField());
        fields.add(IndexMapContentEnum.EMAILACTIVE.getField());
        fields.add(IndexMapContentEnum.EMAILVERIFIED.getField());
        fields.add(IndexMapContentEnum.LASTLOGINDATE.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        return fields;
    }

    @Override
    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = super.getIndexMap();
        indexMap.put(Constants.INDEXMAP_GROUP, RoleEnum.USERREADER);
        return indexMap;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getLastNameFirstName());
        content.add(IndexMapContentEnum.NAME, getLogin());
        content.add(IndexMapContentEnum.FIRSTNAME, getFirstName());
        content.add(IndexMapContentEnum.LASTNAME, getLastName());
        content.add(IndexMapContentEnum.LOGIN, getLogin());
        if (getInstitute() != null) {
            content.add(IndexMapContentEnum.INSTITUTE, getInstitute().getName());
            content.add(IndexMapContentEnum.DEPARTMENT, getInstitute().getDepartmentName());
            content.add(IndexMapContentEnum.ORGANIZATION, getInstitute().getOrganizationName());
        }
        content.add(IndexMapContentEnum.EMAIL, getEmail());
        content.add(IndexMapContentEnum.ADDRESS, getFullAddress());
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());
        content.add(IndexMapContentEnum.EMAILACTIVE, isEmailActive());
        content.add(IndexMapContentEnum.EMAILVERIFIED, isEmailVerified());
        content.add(IndexMapContentEnum.LASTLOGINDATE, getLastLoginDate());

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.USER;
    }

    @Override
    public Institute getInstitute() {
        return institute;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public Set<InstrumentReservation> getInstrumentReservations() {
        return instrumentReservations;
    }

    public List<Instrument> getInstruments() {
        Set<Instrument> instruments = new HashSet<>();
        instruments.addAll(getSupervisedInstruments());
        instruments.addAll(getAdminInstruments());
        instruments.addAll(getOperatorInstruments());
        instruments.addAll(getBookableInstruments());
        instruments.addAll(getTrainedInstruments());
        return CollectionHelper.sortObjects(instruments);
    }

    public int getInvalidLoginAttempts() {
        return invalidLoginAttempts;
    }

    public LocalDateTime getLastActionTime() {
        return lastActionTime;
    }

    public List<Comment> getLastCommentsCurrentUser(Integer maxResult) {
        if (lastCommentsCurrentUser == null) {
            return lastCommentsCurrentUser = CDI.current().select(CommentService.class).get().getLastCommentsByUser(this, maxResult);
        }
        return lastCommentsCurrentUser;
    }

    public Container getLastContainer() {
        return lastContainer;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public Set<Container> getLeaderContainers() {
        return leaderContainers;
    }

    public int getListingRows() {
        return getDefaultListingRows() != null ? getDefaultListingRows() : getConfiguration().getListingRows();
    }

    public String getLogin() {
        return login;
    }

    public int getLoginAttemptsLeft() {
        return Math.max(getConfiguration().getMaxLoginAttempts() - getInvalidLoginAttempts(), 0);
    }

    public Set<Mail> getMails() {
        return mails;
    }

    public boolean getMassMailEnabled() {
        return massMailEnabled;
    }

    public List<User> getMatchingUsers() {
        return matchingUsers;
    }

    public Membership getMembership(Container container) {
        for (Membership membership : getMemberships()) {
            if (membership.getContainer().equals(container)) {
                return membership;
            }
        }
        return null;
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

    public long getMergeAffiliationUserId() {
        return mergeAffiliationUserId;
    }

    public Set<OfferedCharge> getOfferedCharges() {
        return offeredCharges;
    }

    public Set<Offer> getOffers() {
        return offers;
    }

    public Set<InstrumentReservation> getOperatedInstrumentReservations() {
        return operatedInstrumentReservations;
    }

    public Set<Instrument> getOperatorInstruments() {
        return operatorInstruments;
    }

    public Set<Purchase> getOrderItemReceivedByPurchases() {
        return orderItemReceivedByPurchases;
    }

    public Set<Order> getOrders() {
        if (orders == null) {
            orders = getOrders(getMembershipsCurrent());
        }
        return orders;
    }

    public Set<Order> getOrders(Set<Membership> membershipSet) {
        Set<Order> aOrders = new HashSet<>();
        if (membershipSet != null) {
            for (Membership membership : membershipSet) {
                Container container = (Container) Hibernate.unproxy(membership.getContainer());
                if (!container.isContainerProject()) {
                    aOrders.add((Order) container);
                }
            }
        }
        return aOrders;
    }

    public List<Order> getOrdersTransitive() {
        if (ordersTransitive == null) {
            ordersTransitive = CDI.current().select(OrderService.class).get().getOrdersTransitiveByUser(this);
        }
        return ordersTransitive;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    public String getOrganizationName() {
        if (organizationName == null && getOrganization() != null) {
            organizationName = getOrganization().getName();
        }
        return organizationName;
    }

    @Override
    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public OrganizationType getOrganizationTypeForBilling() {
        if (getUserBillingInfo() != null && getUserBillingInfo().getOrganizationTypeForBilling() != null) {
            return getUserBillingInfo().getOrganizationTypeForBilling();
        }
        if (getInstitute() != null && getInstitute().getOrganizationTypeForBilling() != null) {
            return getInstitute().getOrganizationTypeForBilling();
        }
        return getDivision() != null ? getDivision().getOrganizationTypeForBilling() : null;
    }

    public String getOrganizationTypeName() {
        return organizationTypeName;
    }

    public int getParentSamplesMaximumDisplayAmount() {
        return getDefaultParentSamplesMaximumDisplayAmount() != null ? getDefaultParentSamplesMaximumDisplayAmount() : getConfiguration().getParentSamplesMaximumDisplayAmount();
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordAD() {
        return passwordAD;
    }

    public String getPasswordADSecret() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDER) || hasCurrentUserRoleEnum(RoleEnum.ADMIN) ? getPasswordAD() : null;
    }

    public String getPasswordResetUrl() {
        return getUrlWithCodeParameter("reset-password", getResetPasswordCode());
    }

    public String getPasswordWS() {
        return DigestUtils.md5Hex(password + "Ju5t4sTuP1DS4lt");
    }

    public Set<Plate> getPlates() {
        return plates;
    }

    public List<User> getPotentialBackups(String filterString) {
        if (hasRoleEmployeeImplicit()) {
            List<User> potentialBackups = CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getBackup());
            potentialBackups.remove(this);
            return potentialBackups;
        }
        return new ArrayList<>();
    }

    public String getPrivateEmail() {
        return privateEmail;
    }

    public Set<Project> getProjects() {
        if (projects == null) {
            projects = getProjects(getMembershipsCurrent());
        }
        return projects;
    }

    public Set<Project> getProjects(Set<Membership> membershipSet) {
        Set<Project> aProjects = new HashSet<>();
        if (membershipSet != null) {
            for (Membership membership : membershipSet) {
                Container container = (Container) Hibernate.unproxy(membership.getContainer());
                if (container.isContainerProject()) {
                    aProjects.add((Project) container);
                }
            }
        }
        return aProjects;
    }

    public Set<Project> getProjectsFormer() {
        if (projectsFormer == null) {
            projectsFormer = getProjects(getMembershipsFormer());
        }
        return projectsFormer;
    }

    public List<Event> getPublicEvents() {
        if (publicEvents == null && isEmployee()) {
            publicEvents = CDI.current().select(EventService.class).get().getPublicEvents();
        }
        return publicEvents;
    }

    public Set<Purchase> getPurchases() {
        return purchases;
    }

    public BigDecimal getRemainingDays() {
        if (remainingDays == null) {
            remainingDays = getCreditsTotal().subtract(getAccountedDays());
        }
        return remainingDays;
    }

    public BigDecimal getRemainingDays(int agendaYear) {
        return getCreditsTotalByYear(agendaYear).subtract(getAccountedDaysByYear(agendaYear));
    }

    public Set<Container> getRequesterContainers() {
        return requesterContainers;
    }

    public String getResetPasswordCode() {
        return getResetPasswordCodeFunctional() + StringHelper.encodeBase64Date();
    }

    public String getResetPasswordCodeFunctional() {
        return getGeneratedCode();
    }

    public Set<ResourceBasket> getResourceBaskets() {
        return resourceBaskets;
    }

    public Integer getResourceCount() {
        if (resourceCount == null) {
            resourceCount = CDI.current().select(ResourceList.class).get().getLazyModelContainerDependentByContainerIds(getContainerIds()).getSize();
        }
        return resourceCount;
    }

    public Role getRoleAgendaUser() {
        return getRoleService().getRoleByRoleEnum(RoleEnum.AGENDAUSER);
    }

    public Role getRoleAlumni() {
        return getRoleService().getRoleByRoleEnum(RoleEnum.ALUMNI);
    }

    public Role getRoleEmployee() {
        return getRoleService().getRoleByRoleEnum(RoleEnum.EMPLOYEE);
    }

    public List<String> getRoleNamesImplicit() {
        if (roleNamesImplicit == null && StringHelper.isNotEmpty(getLogin())) {
            roleNamesImplicit = CDI.current().select(RoleService.class).get().getRoleNamesImplicitByUserLogin(getLogin());
        }
        return roleNamesImplicit;
    }

    public Role getRoleUser() {
        return getRoleService().getRoleByRoleEnum(RoleEnum.USER);
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public List<Role> getRolesImplicit() {
        if (rolesImplicit == null && StringHelper.isNotEmpty(getLogin())) {
            rolesImplicit = CDI.current().select(RoleService.class).get().getRolesImplicitByUserLogin(getLogin());
        }
        return rolesImplicit;
    }

    public String getRowStyleClass() {
        if (!isEmailVerified()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isEmailOrganizationNotMatching()) {
            return Constants.BACKGROUND_COLOR_ORANGE;
        }
        if (!isEmailActive()) {
            return Constants.BACKGROUND_COLOR_YELLOW;
        }
        if (isLoggedIn()) {
            return isActivelyUsing() ? Constants.BACKGROUND_COLOR_GREEN : Constants.BACKGROUND_COLOR_BLUE;
        }
        return Constants.EMPTY_STRING;
    }

    public Set<Run> getRuns() {
        return runs;
    }

    public Integer getSampleCount() {
        if (sampleCount == null) {
            sampleCount = CDI.current().select(SampleList.class).get().getLazyModelContainerDependentByContainerIds(getContainerIds()).getSize();
        }
        return sampleCount;
    }

    public ResourceBasket getSelectedResourceBasket() {
        return selectedResourceBasket;
    }

    public Set<ServiceArea> getServiceAreas() {
        return serviceAreas;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public Set<ServiceType> getServiceTypesCoach() {
        return serviceTypesCoach;
    }

    public Set<ServiceType> getServiceTypesCoachBackup() {
        return serviceTypesCoachBackup;
    }

    public Set<ServiceType> getServiceTypesForTasks() {
        if (serviceTypesForTasks == null) {
            serviceTypesForTasks = new HashSet<>();
            serviceTypesForTasks.addAll(getServiceTypes());
            serviceTypesForTasks.addAll(getServiceTypesCoach());
            serviceTypesForTasks.addAll(getServiceTypesCoachBackup());
        }
        return serviceTypesForTasks;
    }

    public Set<Service> getServices() {
        return services;
    }

    public String getShibbolethId() {
        return shibbolethId;
    }

    public LocalDate getShibbolethLastLoginDate() {
        return shibbolethLastLoginDate;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public String getSshPublicKeySecret() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDER) || hasCurrentUserRoleEnum(RoleEnum.ADMIN) ? getSshPublicKey() : null;
    }

    public Set<Container> getStatusModifiedContainers() {
        return statusModifiedContainers;
    }

    public Set<Executable> getStatusModifiedExecutables() {
        return statusModifiedExecutables;
    }

    public Set<ExternalJob> getStatusModifiedExternalJobs() {
        return statusModifiedExternalJobs;
    }

    public Set<Plate> getStatusModifiedPlates() {
        return statusModifiedPlates;
    }

    public Set<Run> getStatusModifiedRuns() {
        return statusModifiedRuns;
    }

    public Set<Application> getSupervisedApplications() {
        return supervisedApplications;
    }

    public Set<Contract> getSupervisedContracts() {
        return supervisedContracts;
    }

    public Set<Executable> getSupervisedExecutables() {
        return supervisedExecutables;
    }

    public Set<Instrument> getSupervisedInstruments() {
        return supervisedInstruments;
    }

    public Set<Storage> getSupervisedStorages() {
        return supervisedStorages;
    }

    public Set<Submitter> getSupervisedSubmitters() {
        return supervisedSubmitters;
    }

    public Set<UserGroup> getSupervisedUserGroups() {
        return supervisedUserGroups;
    }

    public Set<WorkflowStep> getSupervisedWorkflowSteps() {
        return supervisedWorkflowSteps;
    }

    public Set<WrapperCreator> getSupervisedWrapperCreators() {
        return supervisedWrapperCreators;
    }

    public Technology getTechnology() {
        return technology;
    }

    public Technology getTechnologyHead() {
        return technologyHead;
    }

    public Set<Container> getTrackedContainers() {
        return trackedContainers;
    }

    public List<Container> getTrackedContainersAsList() {
        return CollectionHelper.asList(trackedContainers);
    }

    public Set<Service> getTrackedServices() {
        return trackedServices;
    }

    public List<Service> getTrackedServicesAsList() {
        return CollectionHelper.asList(trackedServices);
    }

    public Set<Instrument> getTrainedInstruments() {
        return trainedInstruments;
    }

    @SuppressWarnings("unused")
    public String getUnsubscribeUrl() {
        return getUrlWithCodeParameter("unsubscribe", getUnsubscriptionCode());
    }

    public String getUnsubscriptionCode() {
        return getGeneratedCode();
    }

    public String getUrlCalendarForPublicEvents() {
        return new TokenUtils().getCalendarSyncUrl("user=" + getLogin() + ",scope=publicevent");
    }

    public String getUrlCalendarForUser() {
        return new TokenUtils().getCalendarSyncUrl("user=" + getLogin());
    }

    public String getUrlCalendarForUserEvents() {
        return new TokenUtils().getCalendarSyncUrl("user=" + getLogin() + ",scope=agendaevent");
    }

    public String getUrlCalendarForUserInstrumentReservations() {
        return new TokenUtils().getCalendarSyncUrl("user=" + getLogin() + ",scope=instrumentreservation");
    }

    public String getUrlWithCodeParameter(String page, String code) {
        return getClassUrlPrefix() + page + ".html?id=" + getId() + "&amp;code=" + code;
    }

    public UserBillingInfo getUserBillingInfo() {
        return userBillingInfo;
    }

    public Set<UserGroup> getUserGroups() {
        return userGroups;
    }

    public Set<Purchase> getUserPurchases() {
        return userPurchases;
    }

    public List<WorkflowStep> getWorkflowSteps() {
        if (workflowSteps == null) {
            workflowSteps = new ArrayList<>();
            for (Workflow workflow : getWorkflows()) {
                workflowSteps.addAll(workflow.getWorkflowSteps());
            }
        }
        return workflowSteps;
    }

    public List<Workflow> getWorkflows() {
        if (workflows == null) {
            workflows = CDI.current().select(WorkflowService.class).get().getWorkflowsByCreatedBy(getLogin());
        }
        return workflows;
    }

    public Integer getWorkunitCount() {
        if (workunitCount == null) {
            workunitCount = CDI.current().select(WorkunitList.class).get().getLazyModelContainerDependentByContainerIds(getContainerIds()).getSize();
        }
        return workunitCount;
    }

    public boolean hasAcceptedContainer() {
        boolean acceptedContainer = false;
        for (Container container : getContainersAll()) {
            if (container.hasBeenAccepted()) {
                acceptedContainer = true;
                break;
            }
        }
        return acceptedContainer;
    }

    public boolean hasAcceptedContainerAndEmail() {
        return hasAcceptedContainer() && isEmailActive() && isEmailVerified();
    }

    public boolean hasActiveProject() {
        boolean activeProject = false;
        for (Project project : getProjects()) {
            if (project.getStatus().equals(StatusEnum.RUNNING) || project.getStatus().equals(StatusEnum.FINISHED)) {
                activeProject = true;
                break;
            }
        }
        return activeProject;
    }

    public boolean hasActiveProjectAndEmail() {
        return hasActiveProject() && isEmailActive() && isEmailVerified();
    }

    public Boolean hasExtensibleContainer() {
        if (hasExtensibleContainer == null) {
            hasExtensibleContainer = false;
            for (Container container : getContainersAll()) {
                if (container.isExtensible()) {
                    hasExtensibleContainer = true;
                    break;
                }
            }
        }
        return hasExtensibleContainer;
    }

    public Boolean hasExtensibleContainerForSampleCreation() {
        if (hasExtensibleContainerForSampleCreation == null) {
            hasExtensibleContainerForSampleCreation = false;
            for (Container container : getContainersAll()) {
                if (container.isExtensible() && container.isSampleCreatable()) {
                    hasExtensibleContainerForSampleCreation = true;
                    break;
                }
            }
        }
        return hasExtensibleContainerForSampleCreation;
    }

    public boolean hasGuestAccessCard() {
        return getAccessCardNumber() != null && AccessRequestType.isValidGuestAccessCardCode(getAccessCardCode());
    }

    public boolean hasNoLoginAttemptsLeft() {
        return getConfiguration().getMaxLoginAttempts() > 0 && getLoginAttemptsLeft() <= 0;
    }

    public Boolean hasNoOrderAssignableProject() {
        if (hasNoOrderAssignableProject == null) {
            if (hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)) {
                hasNoOrderAssignableProject = false;
            } else {
                hasNoOrderAssignableProject = true;
                for (Project project : getProjects()) {
                    if (project.getStatus().equals(StatusEnum.RUNNING)) {
                        hasNoOrderAssignableProject = false;
                        break;
                    }
                }
            }
        }
        return hasNoOrderAssignableProject;
    }

    public boolean hasPendingAccessRequest() {
        if (!getAccessRequests().isEmpty()) {
            AccessRequest accessRequest = getAccessRequests().iterator().next();
            // Log records are kept in descending order by created. Thus, the first element in the iterator is the last log record.
            // A decision has not been made yet.
            return accessRequest.isPending();
        }
        return false;
    }

    public boolean hasPersonalAccessCard() {
        return getAccessCardNumber() != null && AccessRequestType.isValidPersonalAccessCardCode(getAccessCardCode());
    }

    public boolean hasRole(RoleEnum roleEnum) {
        return CDI.current().select(RoleService.class).get().hasRole(getId(), roleEnum);
    }

    public boolean hasRoleAlumni() {
        return hasRoleImplicit(RoleEnum.ALUMNI);
    }

    public boolean hasRoleEmployee() {
        return hasRoleImplicit(RoleEnum.EMPLOYEE) || getRoles().contains(getRoleEmployee());
    }

    public boolean hasRoleEmployeeImplicit() {
        return hasRoleEmployee() || hasRoleImplicit(RoleEnum.EMPLOYEE);
    }

    public boolean hasRoleImplicit(Role role) {
        return hasRoleImplicit(role.getName());
    }

    public boolean hasRoleImplicit(RoleEnum roleEnum) {
        return hasRoleImplicit(roleEnum.getName());
    }

    public boolean hasRoleImplicit(String roleName) {
        return getRoleNamesImplicit() != null && getRoleNamesImplicit().contains(roleName);
    }

    public boolean hasRoleUser() {
        return hasRole(RoleEnum.USER) || getRoles().contains(getRoleUser());
    }

    public boolean hasRoleUserImplicit() {
        return hasRoleUser() || hasRoleImplicit(RoleEnum.USER);
    }

    public boolean hasRoleUserManager() {
        return hasRoleImplicit(RoleEnum.USERMANAGER);
    }

    public void icsEventExportAndDownload(Set<Event> aEvents) {
        Event event = new Event();
        Set<Event> exportEvents = new HashSet<>();
        if (aEvents != null && !aEvents.isEmpty()) {
            exportEvents.addAll(aEvents);
        } else {
            exportEvents.addAll(getEvents());
        }
        event.download(getClassLabelLowerCaseId() + "_events.ics", event.getIcsExport(exportEvents).toString());
    }

    public void imageUploadListener(FileUploadEvent event) {
        if (getImage() == null) {
            setImage(new Image());
        }
        getImage().setContent(getFileUploadHelper().getImageUpload(event));
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntities(getCredits());
        IndexHelper.indexEntities(getEvents());
        Set<Container> indexContainers = new HashSet<>();
        indexContainers.addAll(getContainers());
        indexContainers.addAll(getCoachContainers());
        indexContainers.addAll(getCoachBackupContainers());
        indexContainers.addAll(getBioinformaticianContainers());
        indexContainers.addAll(getTrackedContainers());
        IndexHelper.indexEntities(indexContainers);
        Set<Instrument> indexInstruments = new HashSet<>();
        indexInstruments.addAll(getSupervisedInstruments());
        indexInstruments.addAll(getAdminInstruments());
        indexInstruments.addAll(getBookableInstruments());
        indexInstruments.addAll(getOperatorInstruments());
        IndexHelper.indexEntities(indexInstruments);
        Set<InstrumentReservation> indexInstrumentReservations = new HashSet<>();
        indexInstrumentReservations.addAll(getInstrumentReservations());
        indexInstrumentReservations.addAll(getBookedInstrumentReservations());
        IndexHelper.indexEntities(indexInstrumentReservations);
    }

    public boolean isAboveAgeLimit(int year) {
        return getBirthDate() != null && year - getBirthDate().getYear() >= getConfiguration().getAnnualVacationCreditAgeLimit();
    }

    public boolean isAccessCardCodeUnique(String cardCode) {
        return getUserService().checkUniqueAccessCardCode(this, cardCode);
    }

    public boolean isAccessCardCodeValid(String cardCode) {
        return AccessRequestType.isValidPersonalAccessCardCode(cardCode) || AccessRequestType.isValidGuestAccessCardCode(cardCode);
    }

    public boolean isAccessCardNotEmpty() {
        return getAccessCardNumber() != null || StringHelper.isNotEmpty(getAccessCardCode()) || getAccessCardExpiryDate() != null;
    }

    public boolean isAccessCardNumberUnique(String cardNumber) {
        return getUserService().checkUniqueAccessCardNumber(this, cardNumber);
    }

    public boolean isAccessRequestable() {
        return getConfiguration().isAccessRequestEnabled() && !hasPendingAccessRequest() && isEnabled() && (isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.ACCESSREQUESTMANAGER)) &&
            (isEmployee() || !getMemberships().isEmpty());
    }

    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    public boolean isActionsRendered() {
        return isManaged() && (isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.ADMIN));
    }

    public boolean isActivelyUsing() {
        return getLastActionTime() != null && Duration.between(getLastActionTime(), LocalDateTime.now()).getSeconds() < getConfiguration().getSessionTimeoutWarningTime();
    }

    public boolean isAffiliationAcknowledged() {
        return affiliationAcknowledged;
    }

    public boolean isAlumni() {
        return hasRoleAlumni();
    }

    public boolean isAnonymizeRendered() {
        return isManaged() && isNotAnonymized() && (isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.ADMIN));
    }

    public boolean isAvailableRendered() {
        return isManaged() && (isIdentityUser() && hasRoleImplicit(RoleEnum.AGENDAUSER) || hasCurrentUserRoleEnum(RoleEnum.ADMIN));
    }

    public boolean isBackupValid() {
        return getBackup() != null && getBackup().hasRoleImplicit(RoleEnum.EMPLOYEE);
    }

    public boolean isBillingAddressUpdatable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || isIdentityUser();
    }

    public boolean isChangePasswordAllowed() {
        return isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    public boolean isClearPrivateInfoAddressRendered() {
        return StringHelper.isNotEmpty(getHomeAddress().getStreet()) || StringHelper.isNotEmpty(getHomeAddress().getZip()) || StringHelper.isNotEmpty(getHomeAddress().getCity()) || getHomeAddress()
            .getCountry() != null;
    }

    public boolean isClearPrivateInfoRendered() {
        return isManaged() && isPrivateInfoNotEmpty() && !isPrivateInfoRequired() && (isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.USERMANAGER));
    }

    public boolean isComplete() {
        return getHomeAddress() != null && getHomeAddress().isComplete() && getHomePhoneNumber() != null && getHomePhoneNumber().isComplete() && getBirthDate() != null;
    }

    public boolean isComputerLoginActivated() {
        return computerLoginActivated;
    }

    public boolean isComputerLoginEnabled() {
        return computerLoginEnabled;
    }

    public boolean isComputerLoginToBeEnabled() {
        return hasRoleEmployeeImplicit() || hasActiveProjectAndEmail();
    }

    public boolean isContainerDependentEntityCreatable() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || hasExtensibleContainer();
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isCreatableWS() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDER) || hasCurrentUserRoleEnum(RoleEnum.ADMIN) || isCreatable();
    }

    public boolean isCreditsOverdrawn() {
        return getAccountedDays().doubleValue() > getCreditsTotal().doubleValue();
    }

    public boolean isDataAccessEnabled() {
        return dataAccessEnabled;
    }

    public boolean isDataAccessToBeEnabled() {
        return hasRoleEmployeeImplicit() || hasAcceptedContainerAndEmail();
    }

    @Override
    public boolean isDeletable() {
        return (hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(getDefaultRequiredRole())) && !isEmployee() && !hasRoleUser() && !isAlumni() && !isInternal() && isDeletableCondition();
    }

    public boolean isDeletableCondition() {
        return getMemberships().isEmpty() &&
            getRequesterContainers().isEmpty() &&
            getContactContainers().isEmpty() &&
            getBudgetOfficerContainers().isEmpty() &&
            getLeaderContainers().isEmpty() &&
            getAccessRequests().isEmpty() &&
            getOffers().isEmpty() &&
            getFeedbacks().isEmpty() &&
            getInstrumentReservations().isEmpty() &&
            getAdminInstruments().isEmpty() &&
            getAcknowledgedFeedbacks().isEmpty() &&
            getApprovedContracts().isEmpty() &&
            getBioinformaticianContainers().isEmpty() &&
            getBookableInstruments().isEmpty() &&
            getBookings().isEmpty() &&
            getCoachBackupContainers().isEmpty() &&
            getCoachContainers().isEmpty() &&
            getContracts().isEmpty() &&
            getCredits().isEmpty() &&
            getDiscussedContainers().isEmpty() &&
            getEvents().isEmpty() &&
            getChargerCharges().isEmpty() &&
            getOperatedInstrumentReservations().isEmpty() &&
            getOperatorInstruments().isEmpty() &&
            getOfferedCharges().isEmpty() &&
            getPlates().isEmpty() &&
            getPurchases().isEmpty() &&
            getOrderItemReceivedByPurchases().isEmpty() &&
            getRuns().isEmpty() &&
            getServiceTypesCoach().isEmpty() &&
            getServiceTypesCoachBackup().isEmpty() &&
            getStatusModifiedContainers().isEmpty() &&
            getStatusModifiedExecutables().isEmpty() &&
            getStatusModifiedPlates().isEmpty() &&
            getStatusModifiedRuns().isEmpty() &&
            getStatusModifiedExternalJobs().isEmpty() &&
            getSupervisedContracts().isEmpty() &&
            getSupervisedInstruments().isEmpty() &&
            getSupervisedApplications().isEmpty() &&
            getSupervisedExecutables().isEmpty() &&
            getSupervisedWrapperCreators().isEmpty() &&
            getSupervisedWorkflowSteps().isEmpty() &&
            getSupervisedSubmitters().isEmpty() &&
            getSupervisedStorages().isEmpty() &&
            getSupervisedUserGroups().isEmpty() &&
            getUserGroups().isEmpty() &&
            getCommentsCurrentUser().isEmpty();
    }

    public boolean isDisableNotificationExclusion() {
        return disableNotificationExclusion;
    }

    public boolean isDisableNotificationExclusionForParent(AbstractEntity parent) {
        return disableNotificationExclusion || parent instanceof Container && getTrackedContainers().contains((Container) parent);
    }

    public boolean isDownloadDirectoryEditable() {
        return downloadDirectoryEditable;
    }

    public boolean isDownloadDirectoryPathInvalid() {
        return !checkDownloadDirectoryPathValidity().isEmpty();
    }

    public boolean isDownloadManagerDownloadButtonRendered() {
        return isDownloadManagerEnabled() && getConfiguration().isDownloadManagerEnabled();
    }

    public boolean isDownloadManagerEnabled() {
        return downloadManagerEnabled;
    }

    public boolean isEmailActive() {
        return emailActive;
    }

    public boolean isEmailActiveRendered() {
        return isEmailNoticeRendered() || hasCurrentUserRoleEnum(RoleEnum.USERMANAGER);
    }

    public boolean isEmailChanged() {
        return emailChanged;
    }

    public boolean isEmailNoticeRendered() {
        return !isManaged() || this.equals(getCurrentUser());
    }

    public boolean isEmailOrganizationNotMatching() {
        return getInstitute() != null ? getInstitute().isEmailOrganizationNotMatching(getEmail()) : new Company().isNotMatchingEmail(getEmail());
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isEmailVerifiedRendered() {
        return isIdentityUser() && hasRoleImplicit(RoleEnum.USERREADER);
    }

    public boolean isEmpDegreeConsistent() {
        if (empDegreeConsistent == null) {
            empDegreeConsistent = isEmployee() && hasRoleEmployee() || !isEmployee() && !hasRoleEmployee();
        }
        return empDegreeConsistent;
    }

    public boolean isEmployee() {
        if (employee == null) {
            employee = getEmpDegree() != null;
        }
        return employee;
    }

    public boolean isEmployeeEntryButtonDisabled() {
        return getEmpDegree() == null;
    }

    public boolean isEnabled() {
        return isEmailActive() && isEmailVerified();
    }

    public boolean isEventScheduleRendered() {
        return isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.EMPLOYEEMANAGER);
    }

    public boolean isExtensibleByCurrentUser() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isIdentityUser() {
        return getLogin() != null && getLogin().equals(getCurrentUsername());
    }

    public boolean isIdentityUserOrUserManager() {
        return isIdentityUser() || hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isInternal() {
        return hasRole(RoleEnum.INTERNAL);
    }

    public boolean isLoggedIn() {
        if (loggedIn == null) {
            loggedIn = ConfigurationHelper.getConfManager().getLoggedInUsers().contains(this);
        }
        return loggedIn;
    }

    public boolean isMailsRendered() {
        return !getMails().isEmpty() && isUpdatable();
    }

    public boolean isMassMailEnabled() {
        return massMailEnabled;
    }

    public boolean isMassMailEnabledRendered() {
        return !isMassMailEnabled() || !hasRoleImplicit(RoleEnum.EMPLOYEE);
    }

    public boolean isMatchingUserRendered() {
        if (StringHelper.isNotEmpty(getFirstName()) && StringHelper.isNotEmpty(getLastName())) {
            setMatchingUsers(getUserService().getUserByFirstNameAndLastName(getFirstName(), getLastName()));
            return !getMatchingUsers().isEmpty();
        }
        return false;
    }

    public boolean isMaxInvalidLoginAttemptsPassed() {
        final int maxInvalidLoginAttempts = getConfiguration().getMaxLoginAttempts();
        return maxInvalidLoginAttempts > 0 && getInvalidLoginAttempts() > maxInvalidLoginAttempts;
    }

    public boolean isNotAnonymized() {
        return !isManaged() ||
            isPrivateInfoNotEmpty() ||
            isAccessCardNotEmpty() ||
            getUserBillingInfo() != null ||
            getImage() != null ||
            StringHelper.isNotEmpty(getDescription()) ||
            StringHelper.isNotEmpty(getTitle()) ||
            !getFirstName().equals(Constants.X) ||
            !getLastName().equals(Constants.X) ||
            !getEmail().equals(getId() + StringHelper.EMAIL_XX) ||
            !getPhoneNumber().getCountryCode().equals(41) ||
            !getPhoneNumber().getAreaCode().equals(1) ||
            !getPhoneNumber().getLocalNumber().equals(1) ||
            !getAddress().getStreet().equals(Constants.X) ||
            !getAddress().getZip().equals("1") ||
            !getAddress().getCity().equals(Constants.X) ||
            !getAddress().getCountry().equals(CDI.current().select(CountryService.class).get().getCountryDefault()) ||
            StringHelper.isNotEmpty(getAddress().getRoom()) || getDivision() == null || !getDivision().equals(CDI.current().select(DivisionService.class).get()
            .getDivisionByNameAndCompanyNameAndCreateIfNotExists(getConfiguration().getDefaultDivision(), getConfiguration().getDefaultCompanyName()));
    }

    public boolean isPasswordResetNeeded() {
        return getConfiguration().isSynchronizeWithADEnabled() && hasRoleUser() && !isComputerLoginActivated();
    }

    private boolean isPrivateInfoAddressComplete() {
        return getHomeAddress() != null && getHomeAddress().isComplete();
    }

    private boolean isPrivateInfoAddressEmpty() {
        return getHomeAddress() == null || getHomeAddress().isEmpty();
    }

    public boolean isPrivateInfoAddressWarningRendered() {
        return !isPrivateInfoAddressEmpty() && !isPrivateInfoAddressComplete();
    }

    public boolean isPrivateInfoNotEmpty() {
        return getBirthDate() != null || StringHelper.isNotEmpty(getHomePhone()) || getHomeAddress() != null && !getHomeAddress().isEmpty();
    }

    public boolean isPrivateInfoRequired() {
        return getConfiguration().isEmployeePrivateInfoRequired() && getEmpDegree() != null || hasPendingAccessRequest();
    }

    @Override
    public boolean isReadable() {
        return isIdentityUser() || hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.USERREADER) || hasCurrentUserRoleEnum(RoleEnum.USER);
    }

    public boolean isRecomputeComputerLoginAndDataAccessEnabledRequired() {
        if (isComputerLoginToBeEnabled()) {
            return !isComputerLoginEnabled() || !isDataAccessEnabled() || !isComputerLoginActivated() || !hasRoleUser();
        }
        if (isDataAccessToBeEnabled()) {
            return isComputerLoginEnabled() || !isDataAccessEnabled() || !isComputerLoginActivated();
        }
        return isComputerLoginEnabled() || isDataAccessEnabled();
    }

    public boolean isRenderedDuties() {
        return hasRoleEmployee() && isIdentityUser() || (hasRoleEmployee() || hasRoleAlumni()) && (hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(RoleEnum.UNITHEAD));
    }

    public boolean isResetBillingInfoRendered() {
        return !(getInstitute() != null && getUserBillingInfo() != null && getInstitute()
            .equals(getUserBillingInfo().getInstitute()) || getDivision() != null && getUserBillingInfo() != null && getDivision().equals(getUserBillingInfo().getDivision()));
    }

    public boolean isRolesImplicitRendered() {
        List<Role> checkRoles = new ArrayList<>(getRolesImplicit());
        checkRoles.removeAll(getRoles());
        return !checkRoles.isEmpty() && getCurrentUser().hasRoleImplicit(RoleEnum.USERREADER);
    }

    public boolean isSaveButtonRendered() {
        return isEmailActiveRendered() && (hasCurrentUserRoleEnum(RoleEnum.USERMANAGER) || !isManaged() || isEmailActive());
    }

    public boolean isShibbolethIdRendered() {
        return StringHelper.isNotEmpty(shibbolethId) && (isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.ADMIN));
    }

    public boolean isShowSamplesLaneButton() {
        return showSamplesLaneButton;
    }

    public boolean isShowSamplesLaneButtonAndCheckbox() {
        return showSamplesLaneButtonAndCheckbox;
    }

    public boolean isShowSamplesLaneSeparated() {
        return showSamplesLaneSeparated;
    }

    public boolean isSyncable() {
        // Note: extended check condition to capture the case that the newly assigned role User is only available on the entity itself but not flushed to the DB yet!
        return getConfiguration().isSynchronizeWithADEnabled() && (isRecomputeComputerLoginAndDataAccessEnabledRequired() || getRoles().contains(getRoleUser()) || getRoles()
            .contains(getRoleEmployee()) || hasRoleUserImplicit());
    }

    public boolean isSyncableByCurrentUser() {
        return isSyncable() && hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isSynchronizationWithADRequired() {
        return getConfiguration().isSynchronizeWithADEnabled() && !"public".equals(getLogin()) && (!isComputerLoginActivated() || getPasswordAD() == null);
    }

    public boolean isUnitHead() {
        return technologyHead != null;
    }

    @Override
    public boolean isUpdatable() {
        return isIdentityUser() || hasCurrentUserRoleEnum(RoleEnum.EMPLOYEEMANAGER) || hasCurrentUserRoleEnum(getDefaultRequiredRole()) && !isEmployee() && !isInternal();
    }

    public boolean isUserBillingInfoInstituteDifferent() {
        return getUserBillingInfo() != null && getUserBillingInfo().getInstitute() != null && !getUserBillingInfo().getInstitute().equals(getInstitute());
    }

    public boolean isUserBillingInfoZurichUniversityDifferent() {
        return getUserBillingInfo() != null && getUserBillingInfo().getInstitute() != null && getUserBillingInfo().getInstitute()
            .isOrganizationTypeUniZH() && getInstitute() != null && !getInstitute().isOrganizationTypeUniZH();
    }

    public boolean isWebServicePasswordRendered() {
        return isIdentityUser() && (hasRoleImplicit(RoleEnum.USER) || hasRoleImplicit(RoleEnum.FEEDER));
    }

    public void mergeAttributes(User mergeSelection, User merged) {
        if (getId() != mergeSelection.getMergeAffiliationUserId()) {
            // The affiliation of the merge user is selected.
            setInstitute(merged.getInstitute());
            setDivision(merged.getDivision());
        }

        if (getSelectedResourceBasket() == null && merged.getSelectedResourceBasket() != null) {
            setSelectedResourceBasket(merged.getSelectedResourceBasket());
            getSelectedResourceBasket().getUsers().remove(merged);
        }

        // Important: check whether the userLeft's UserBillingInfo object exists or not.
        if (getUserBillingInfo() == null && mergeSelection.getUserBillingInfo() != null) {
            // Create a new object with the userBillingInfo data!
            setUserBillingInfo(new UserBillingInfo(mergeSelection.getUserBillingInfo()));
        } else if (getUserBillingInfo() != null && !getUserBillingInfo().equals(mergeSelection.getUserBillingInfo())) {
            // Just copy the userBillingInfo data!
            getUserBillingInfo().copy(mergeSelection.getUserBillingInfo());
        }

        // Important: check whether the userLeft's Image object exists or not.
        if (getImage() == null && mergeSelection.getImage() != null) {
            // Create a new object with the image data!
            setImage(new Image(mergeSelection.getImage().getContent()));
        } else if (getImage() != null && !getImage().equals(mergeSelection.getImage())) {
            // Just copy the other image!
            getImage().setContent(mergeSelection.getImage().getContent());
        }

        // It is active by default, but it is necessary to send the email anyway.
        setEmailActive(true);

        // Reset the invalid login attempts.
        setInvalidLoginAttempts(0);

        // Set the attributes depending on the selection.
        setAccountEnabled(mergeSelection.isAccountEnabled());
        setLastLoginDate(mergeSelection.getLastLoginDate());
        setTitle(mergeSelection.getTitle());
        setLastName(mergeSelection.getLastName());
        setFirstName(mergeSelection.getFirstName());
        setSalutation(mergeSelection.getSalutation());
        setBirthDate(mergeSelection.getBirthDate());
        setEmpDegree(mergeSelection.getEmpDegree());
        setEmail(mergeSelection.getEmail());
        setLogin(mergeSelection.getLogin());
        setPhoneNumber(mergeSelection.getPhoneNumber());
        setAddress(mergeSelection.getAddress());
        setInstitute(mergeSelection.getInstitute());
        setDivision(mergeSelection.getDivision());
        setPrivateEmail(mergeSelection.getPrivateEmail());
        setHomePhoneNumber(mergeSelection.getHomePhoneNumber());
        setHomeAddress(mergeSelection.getHomeAddress());
        // Important: Do this check before you set the selected access card number!
        if (mergeSelection.getAccessCardNumber() != null && !mergeSelection.getAccessCardNumber().equals(getAccessCardNumber())) {
            // Take the dependent attributes of the access card.
            setAccessCardCode(merged.getAccessCardCode());
            setAccessCardExpiryDate(merged.getAccessCardExpiryDate());
        }
        setAccessCardNumber(mergeSelection.getAccessCardNumber());
        setMassMailEnabled(mergeSelection.getMassMailEnabled());
        setDefaultTechnology(mergeSelection.getDefaultTechnology());
        setDefaultUserGroup(mergeSelection.getDefaultUserGroup());
        setDownloadDirectoryEditable(mergeSelection.isDownloadDirectoryEditable());
        setDownloadDirectoryStructure(mergeSelection.getDownloadDirectoryStructure());
        setDownloadDirectoryPath(mergeSelection.getDownloadDirectoryPath());
        setDescription(mergeSelection.getDescription());
    }

    @Override
    protected void prePersist() {
        opLockVersion = 1;
        setCreated(LocalDateTime.now());
        setModified(LocalDateTime.now());
        String username = getCurrentUsername();
        if (username != null) {
            setCreatedBy(username);
            setModifiedBy(username);
        } else {
            setCreatedBy(getLogin());
            setModifiedBy(getLogin());
        }
    }

    public boolean recomputeComputerLoginAndDataAccessEnabled() {
        boolean syncRequired = false;
        if (isComputerLoginToBeEnabled()) {
            if (!isComputerLoginEnabled()) {
                setComputerLoginEnabled(true);
                syncRequired = true;
            }
            if (!isDataAccessEnabled()) {
                setDataAccessEnabled(true);
                syncRequired = true;
            }
            if (!isComputerLoginActivated()) {
                syncRequired = true;
            }
            if (!hasRoleUser()) {
                final Role userRole = getRoleUser();
                addRole(userRole);
                syncRequired = true;
            }
        } else if (isDataAccessToBeEnabled()) {
            if (isComputerLoginEnabled()) {
                setComputerLoginEnabled(false);
                syncRequired = true;
            }
            if (!isDataAccessEnabled()) {
                setDataAccessEnabled(true);
                syncRequired = true;
            }
        } else if (isComputerLoginEnabled() || isDataAccessEnabled()) {
            setComputerLoginEnabled(false);
            setDataAccessEnabled(false);
            syncRequired = true;
        }
        return syncRequired;
    }

    public void removeRole(Role role) {
        if (role != null) {
            getRoles().remove(role);
            role.getUsers().remove(this);
        }
    }

    public void resetAccessCard() {
        setAccessCardNumber(null);
        setAccessCardCode(null);
        setAccessCardExpiryDate(null);
    }

    public void resetBillingInfo() {
        getUserBillingInfo().copy(this);
    }

    public void revokeEmployeeRights() {
        /*
         * Remove all starred/viewed comments from the user if the comment:
         * - is internal
         * - or belongs to a container where the user is not a member of
         *
         * Exception for a comment:
         * - which is not internal
         * - and does belong to a container where the user is not a member of
         * - and the container or its project is published
         */
        Set<Comment> containerCommentsReadableByUserAfterRevokingEmployeeRights = new HashSet<>(CDI.current().select(CommentService.class).get()
            .getContainerCommentsReadableByUserAfterRevokingEmployeeRights(this));
        getCommentsStarredBy().retainAll(containerCommentsReadableByUserAfterRevokingEmployeeRights);
        getCommentsViewedBy().retainAll(containerCommentsReadableByUserAfterRevokingEmployeeRights);

        // Revoke all roles except the user and alumni roles.
        boolean hasRoleUser = hasRoleUser();
        boolean hasRoleAlumni = hasRoleAlumni();
        getRoles().clear();
        if (hasRoleUser) {
            getRoles().add(getRoleUser());
        }
        if (hasRoleAlumni) {
            getRoles().add(getRoleAlumni());
        }
        for (Container container : getTrackedContainers()) {
            container.getTrackingUsers().remove(this);
        }
        getTrackedContainers().clear();
        for (Service service : getTrackedServices()) {
            service.getTrackingUsers().remove(this);
        }
        getTrackedServices().clear();
        for (ServiceArea serviceArea : getServiceAreas()) {
            serviceArea.getUsers().remove(this);
        }
        getServiceAreas().clear();
        for (ServiceType serviceType : getServiceTypes()) {
            serviceType.getUsers().remove(this);
        }
        getServiceTypes().clear();
        for (Service service : getServices()) {
            service.getUsers().remove(this);
        }
        getServices().clear();
        getBookableInstruments().clear();

        setComputerLoginEnabled(hasActiveProjectAndEmail());
    }

    public void setAccessRequests(Set<AccessRequest> accessRequests) {
        this.accessRequests = accessRequests;
    }

    public void setAccountEnabled(boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public void setAffiliatedWithUZH(Boolean affiliatedWithUZH) {
        this.affiliatedWithUZH = affiliatedWithUZH;
    }

    public void setAffiliationAcknowledged(boolean affiliationAcknowledged) {
        this.affiliationAcknowledged = affiliationAcknowledged;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public void setBackup(User backup) {
        this.backup = backup;
    }

    public void setBackupOf(Set<User> backupOf) {
        this.backupOf = backupOf;
    }

    public void setBookableInstruments(Set<Instrument> bookableInstruments) {
        this.bookableInstruments = bookableInstruments;
    }

    public void setBudgetOfficerContainers(Set<Container> budgetOfficerContainers) {
        this.budgetOfficerContainers = budgetOfficerContainers;
    }

    public void setChargerCharges(Set<Charge> charges) {
        this.chargerCharges = charges;
    }

    public void setCommentsAcknowledgedBy(Set<Comment> commentsAcknowledgedBy) {
        this.commentsAcknowledgedBy = commentsAcknowledgedBy;
    }

    public void setCommentsStarredBy(Set<Comment> commentsStarredBy) {
        this.commentsStarredBy = commentsStarredBy;
    }

    public void setCommentsViewedBy(Set<Comment> commentsViewedBy) {
        this.commentsViewedBy = commentsViewedBy;
    }

    @Override
    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public void setCompanyName(String companyName) {
        this.companyName = StringHelper.format(companyName);
    }

    public void setComputerLoginActivated(boolean computerLoginActivated) {
        this.computerLoginActivated = computerLoginActivated;
    }

    public void setComputerLoginEnabled(boolean computerLoginEnabled) {
        this.computerLoginEnabled = computerLoginEnabled;
    }

    public void setComputerLoginValidityChecked(LocalDateTime computerLoginValidityChecked) {
        this.computerLoginValidityChecked = computerLoginValidityChecked;
    }

    public void setContainersRequested(Set<Container> containersRequested) {
        this.containersRequested = containersRequested;
    }

    public void setCredits(Set<Credit> credits) {
        this.credits = credits;
    }

    public void setDataAccessEnabled(boolean dataAccessEnabled) {
        this.dataAccessEnabled = dataAccessEnabled;
    }

    public void setDefaultDataScrollerChunkSize(Integer defaultDataScrollerChunkSize) {
        this.defaultDataScrollerChunkSize = defaultDataScrollerChunkSize;
    }

    public void setDefaultListingRows(Integer defaultListingRows) {
        this.defaultListingRows = defaultListingRows;
    }

    public void setDefaultParentSamplesMaximumDisplayAmount(Integer defaultParentSamplesMaximumDisplayAmount) {
        this.defaultParentSamplesMaximumDisplayAmount = defaultParentSamplesMaximumDisplayAmount;
    }

    public void setDefaultTechnology(Technology defaultTechnology) {
        this.defaultTechnology = defaultTechnology;
    }

    public void setDefaultUserGroup(UserGroup defaultUserGroup) {
        this.defaultUserGroup = defaultUserGroup;
    }

    @Override
    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setDisableNotificationExclusion(boolean disableNotificationExclusion) {
        this.disableNotificationExclusion = disableNotificationExclusion;
    }

    @Override
    public void setDivision(Division division) {
        this.division = division;
    }

    @Override
    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public void setDownloadDirectoryEditable(boolean downloadDirectoryEditable) {
        this.downloadDirectoryEditable = downloadDirectoryEditable;
    }

    public void setDownloadDirectoryPath(String downloadDirectoryPath) {
        this.downloadDirectoryPath = StringHelper.format(downloadDirectoryPath);
    }

    public void setDownloadDirectoryStructure(String downloadDirectoryStructure) {
        this.downloadDirectoryStructure = StringHelper.format(downloadDirectoryStructure);
    }

    public void setDownloadManagerEnabled(boolean downloadManagerEnabled) {
        this.downloadManagerEnabled = downloadManagerEnabled;
    }

    @Override
    public void setEmail(String email) {
        // Set active to true and email verified to false when the email is changed.
        if (StringHelper.isNotEmpty(email) && !email.trim().equals(getEmail())) {
            super.setEmail(email);
            setEmailChanged(true);
            setEmailActive(true);
            setEmailVerified(false);
        }
    }

    public void setEmailActive(boolean emailActive) {
        this.emailActive = emailActive;

        // Set emailVerified to false when the user email is not active anymore.
        if (!emailActive) {
            setEmailVerified(false);
        }
    }

    public void setEmailChanged(boolean emailChanged) {
        this.emailChanged = emailChanged;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        // Set active to true when the email is verified.
        if (emailVerified) {
            setEmailActive(true);
        }
    }

    public void setEmpDegree(Integer empDegree) {
        this.empDegree = empDegree;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    public void setExportBookingPath(String exportBookingPath) {
        this.exportBookingPath = exportBookingPath;
    }

    @Override
    public void setFirstName(String firstName) {
        String currentFirstName = getFirstName();
        super.setFirstName(firstName);
        // Set index dependents to true when this attribute is changed.
        if (getFirstName() != null && !getFirstName().equals(currentFirstName)) {
            setIndexDependents(true);
        }
    }

    public void setGeneratedLoginName(ValueChangeEvent event) {
        if (!isManaged() && StringHelper.isNotEmpty(event.getNewValue().toString())) {
            String componentId = event.getComponent().getId();
            if (componentId.equals("firstName")) {
                setFirstName(event.getNewValue().toString());
            } else if (componentId.equals("lastName")) {
                setLastName(event.getNewValue().toString());
            }

            if (StringHelper.isNotEmpty(getFirstName()) && StringHelper.isNotEmpty(getLastName())) {
                setLogin(generateLoginName());
            } else {
                setLogin("");
            }
        } else if (!isManaged() && StringHelper.isEmpty(event.getNewValue().toString())) {
            setLogin("");
        }
    }

    public void setHomeAddress(HomeAddress homeAddress) {
        this.homeAddress = homeAddress;
    }

    public void setHomePhoneNumber(HomePhoneNumber homePhoneNumber) {
        this.homePhoneNumber = homePhoneNumber;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public void setInstitute(Institute institute) {
        this.institute = institute;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public void setInvalidLoginAttempts(int invalidLoginAttempts) {
        this.invalidLoginAttempts = invalidLoginAttempts;
    }

    public void setLastActionTime(LocalDateTime lastActionTime) {
        this.lastActionTime = lastActionTime;
    }

    public void setLastContainer(Container lastContainer) {
        this.lastContainer = lastContainer;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @Override
    public void setLastName(String lastName) {
        String currentLastName = getLastName();
        super.setLastName(lastName);
        // Set index dependents to true when this attribute is changed.
        if (getLastName() != null && !getLastName().equals(currentLastName)) {
            setIndexDependents(true);
        }
    }

    public void setLeaderContainers(Set<Container> leaderContainers) {
        this.leaderContainers = leaderContainers;
    }

    public void setLogin(String login) {
        String currentLogin = getLogin();
        this.login = StringHelper.format(login);
        if (StringHelper.isNotEmpty(getLogin())) {
            this.login = getLogin().toLowerCase();
        }
        // Set index dependents to true when this attribute is changed.
        if (getLogin() != null && !getLogin().equalsIgnoreCase(currentLogin)) {
            setIndexDependents(true);
        }
    }

    public void setMails(Set<Mail> mails) {
        this.mails = mails;
    }

    public void setMassMailEnabled(boolean massMailEnabled) {
        this.massMailEnabled = massMailEnabled;
    }

    public void setMatchingUsers(List<User> matchingUsers) {
        this.matchingUsers = matchingUsers;
    }

    public void setMemberships(Set<Membership> memberships) {
        this.memberships = memberships;
    }

    public void setMergeAffiliationUserId(long mergeAffiliationUserId) {
        this.mergeAffiliationUserId = mergeAffiliationUserId;
    }

    public void setOffers(Set<Offer> offers) {
        this.offers = offers;
    }

    public void setOperatorInstruments(Set<Instrument> operatorInstruments) {
        this.operatorInstruments = operatorInstruments;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    @Override
    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public void setOrganizationTypeName(String organizationTypeName) {
        this.organizationTypeName = organizationTypeName;
    }

    public void setPassword(char[] password) {
        if (password != null) {
            setPasswordAD(password);
            this.password = BfabricPasswordHash.encode(password);
        }
    }

    /**
     * Set the password for AD. IMPORTANT: Ensure that after this call the input parameter passwordAD is cleared, i.e., filled with zeros, if not used anymore!
     */
    public void setPasswordAD(char[] passwordAD) {
        BfabricPasswordEncryptor encryptor = new BfabricPasswordEncryptor();
        encryptor = encryptor.getInstance();
        if (encryptor != null) {
            this.passwordAD = encryptor.encrypt(passwordAD);
        }
    }

    public void setPrivateEmail(String privateEmail) {
        this.privateEmail = privateEmail;
    }

    public void setResourceBaskets(Set<ResourceBasket> resourceBaskets) {
        this.resourceBaskets = resourceBaskets;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
        checkAndSetMassMailEnabled();
    }

    public void setSelectedResourceBasket(ResourceBasket selectedResourceBasket) {
        this.selectedResourceBasket = selectedResourceBasket;
    }

    public void setShibbolethId(String shibbolethId) {
        this.shibbolethId = StringHelper.format(shibbolethId);
    }

    public void setShibbolethLastLoginDate(LocalDate shibbolethLastLoginDate) {
        this.shibbolethLastLoginDate = shibbolethLastLoginDate;
    }

    public void setShowSamplesLaneButton(boolean showSamplesLaneButton) {
        this.showSamplesLaneButton = showSamplesLaneButton;
    }

    public void setShowSamplesLaneButtonAndCheckbox(boolean showSamplesLaneButtonAndCheckbox) {
        this.showSamplesLaneButtonAndCheckbox = showSamplesLaneButtonAndCheckbox;
    }

    public void setShowSamplesLaneSeparated(boolean showSamplesLaneSeparated) {
        this.showSamplesLaneSeparated = showSamplesLaneSeparated;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = StringHelper.format(sshPublicKey);
    }

    public void setSupervisedInstruments(Set<Instrument> supervisedInstruments) {
        this.supervisedInstruments = supervisedInstruments;
    }

    public void setSupervisedUserGroups(Set<UserGroup> supervisedUserGroups) {
        this.supervisedUserGroups = supervisedUserGroups;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public void setTechnologyHead(Technology technologyHead) {
        this.technologyHead = technologyHead;
    }

    public void setTrackedContainers(Set<Container> trackedContainers) {
        this.trackedContainers = trackedContainers;
    }

    public void setTrackedContainersAsList(List<Container> trackedContainers) {
        this.trackedContainers = CollectionHelper.asSet(trackedContainers);
    }

    public void setTrackedServices(Set<Service> trackedServices) {
        this.trackedServices = trackedServices;
    }

    public void setTrackedServicesAsList(List<Service> trackedServices) {
        this.trackedServices = CollectionHelper.asSet(trackedServices);
    }

    public void setUserBillingInfo(UserBillingInfo userBillingInfo) {
        this.userBillingInfo = userBillingInfo;
    }

    public void setUserBillingInfo() {
        setUserBillingInfo(new UserBillingInfo());
    }

    public void setUserGroups(Set<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public void switchAccountEnabled() {
        setAccountEnabled(!isAccountEnabled());
    }

    public String transformNameToLogin(String name) {
        String ret = name;
        if (ret != null) {
            ret = ret.toLowerCase();
            ret = ret.replaceAll("ä", "ae");
            ret = ret.replaceAll("ö", "oe");
            ret = ret.replaceAll("ü", "ue");
            ret = ret.replaceAll("ß", "ss");
            ret = ret.replaceAll("à", "a");
            ret = ret.replaceAll("â", "a");
            ret = ret.replaceAll("à", "a");
            ret = ret.replaceAll("å", "a");
            ret = ret.replaceAll("ã", "a");
            ret = ret.replaceAll("ā", "a");
            ret = ret.replaceAll("ă", "a");
            ret = ret.replaceAll("ą", "a");
            ret = ret.replaceAll("æ", "ae");
            ret = ret.replaceAll("ç", "c");
            ret = ret.replaceAll("ć", "c");
            ret = ret.replaceAll("ĉ", "c");
            ret = ret.replaceAll("ċ", "c");
            ret = ret.replaceAll("č", "c");
            ret = ret.replaceAll("ď", "d");
            ret = ret.replaceAll("đ", "d");
            ret = ret.replaceAll("é", "e");
            ret = ret.replaceAll("è", "e");
            ret = ret.replaceAll("ë", "e");
            ret = ret.replaceAll("ê", "e");
            ret = ret.replaceAll("ē", "e");
            ret = ret.replaceAll("ĕ", "e");
            ret = ret.replaceAll("ė", "e");
            ret = ret.replaceAll("ę", "e");
            ret = ret.replaceAll("ě", "e");
            ret = ret.replaceAll("ƒ", "f");
            ret = ret.replaceAll("ĝ", "g");
            ret = ret.replaceAll("ğ", "g");
            ret = ret.replaceAll("ġ", "g");
            ret = ret.replaceAll("ģ", "g");
            ret = ret.replaceAll("ĥ", "h");
            ret = ret.replaceAll("ħ", "h");
            ret = ret.replaceAll("ï", "i");
            ret = ret.replaceAll("î", "i");
            ret = ret.replaceAll("ì", "i");
            ret = ret.replaceAll("í", "i");
            ret = ret.replaceAll("ĩ", "i");
            ret = ret.replaceAll("ī", "i");
            ret = ret.replaceAll("ĭ", "i");
            ret = ret.replaceAll("į", "i");
            ret = ret.replaceAll("ı", "i");
            ret = ret.replaceAll("ĵ", "j");
            ret = ret.replaceAll("ķ", "k");
            ret = ret.replaceAll("ĺ", "l");
            ret = ret.replaceAll("ļ", "l");
            ret = ret.replaceAll("ľ", "l");
            ret = ret.replaceAll("ŀ", "l");
            ret = ret.replaceAll("ł", "l");
            ret = ret.replaceAll("ñ", "n");
            ret = ret.replaceAll("ń", "n");
            ret = ret.replaceAll("ņ", "n");
            ret = ret.replaceAll("ň", "n");
            ret = ret.replaceAll("õ", "o");
            ret = ret.replaceAll("ô", "o");
            ret = ret.replaceAll("ò", "o");
            ret = ret.replaceAll("ó", "o");
            ret = ret.replaceAll("ø", "o");
            ret = ret.replaceAll("ō", "o");
            ret = ret.replaceAll("ŏ", "o");
            ret = ret.replaceAll("ő", "o");
            ret = ret.replaceAll("œ", "oe");
            ret = ret.replaceAll("ŕ", "r");
            ret = ret.replaceAll("ŗ", "r");
            ret = ret.replaceAll("ř", "r");
            ret = ret.replaceAll("š", "s");
            ret = ret.replaceAll("ś", "s");
            ret = ret.replaceAll("ŝ", "s");
            ret = ret.replaceAll("ş", "s");
            ret = ret.replaceAll("š", "s");
            ret = ret.replaceAll("ţ", "t");
            ret = ret.replaceAll("ť", "t");
            ret = ret.replaceAll("ŧ", "t");
            ret = ret.replaceAll("û", "u");
            ret = ret.replaceAll("ù", "u");
            ret = ret.replaceAll("ú", "u");
            ret = ret.replaceAll("ũ", "u");
            ret = ret.replaceAll("ū", "u");
            ret = ret.replaceAll("ŭ", "u");
            ret = ret.replaceAll("ů", "u");
            ret = ret.replaceAll("ű", "u");
            ret = ret.replaceAll("ų", "u");
            ret = ret.replaceAll("ŵ", "w");
            ret = ret.replaceAll("ÿ", "y");
            ret = ret.replaceAll("ý", "y");
            ret = ret.replaceAll("ŷ", "y");
            ret = ret.replaceAll("ź", "z");
            ret = ret.replaceAll("ż", "z");
            ret = ret.replaceAll("ž", "z");
            ret = ret.replaceAll("[^a-z]", Constants.EMPTY_STRING);
        }
        return ret;
    }

    public boolean validateAccessCardCode(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (StringHelper.isNotEmpty((String) value)) {
            String cardCode = (String) value;
            if (!isAccessCardCodeValid(cardCode)) {
                throw new BfabricValidatorException("accessCardCodeValidException");
            }
            if (!isAccessCardCodeUnique(cardCode)) {
                throw new BfabricValidatorException("accessCardCodeNotUniqueException");
            }
        }
        return true;
    }

    public boolean validateAccessCardNumber(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value != null) {
            if (!AccessRequestType.isValidAccessCardNumber(String.valueOf(value))) {
                throw new BfabricValidatorException("accessCardNumberValidException");
            }
            if (!isAccessCardNumberUnique(String.valueOf(value))) {
                throw new BfabricValidatorException("accessCardNumberNotUniqueException");
            }
        }
        return true;
    }

    public boolean validateConfirmPassword(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        return validatePassword(facesContext, uiComponent, value);
    }

    public void validateEmail(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        final String email = StringHelper.format((String) value);
        if (email == null || StringHelper.isInvalidEmailAddress(email)) {
            throw new BfabricValidatorException("emailNotValidException");
        }
        if (getUserService().isEmailNotUnique(this, email)) {
            throw new BfabricValidatorException("emailNotUniqueException");
        }
        setEmail(email);
        if (isEmailOrganizationNotMatching()) {
            throw new BfabricValidatorException("emailNotMatchOrganizationException");
        }
    }

    public boolean validateLogin(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        final String login = (String) value;

        if (!StringHelper.isNotEmpty(login)) {
            throw new BfabricValidatorException("loginNotLetterException");
        }

        if (!Pattern.compile("^[a-z]+$").matcher(login).find()) {
            throw new BfabricValidatorException("loginNotLetterException");
        }

        if (getUserService().isLoginBlacklisted(login)) {
            throw new BfabricValidatorException("loginInvalidException");
        }

        if (ClassHelper.getEntityClassNamesLowerCase().contains(login)) {
            throw new BfabricValidatorException("loginInvalidException");
        }

        if (!getUserService().checkUniqueLogin(this, login)) {
            throw new BfabricValidatorException("loginNotUniqueException");
        }

        return true;
    }

    public boolean validateOldPassword(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value == null) {
            throw new BfabricValidatorException("required");
        }
        if (!new BfabricPasswordHash().verify((char[]) value, getPassword())) {
            throw new BfabricValidatorException("oldPasswordException");
        }
        return true;
    }

    /**
     * Password must meet the requirements of the authentication database:
     * <ul>
     * <li>is at least 8 characters long.</li>
     * <li>must contain upper case letters [A-Z]</li>
     * <li>must contain lower case letters [a-z]</li>
     * <li>must contain numbers [0-9]</li>
     * <li>must contain special characters</li>
     * <li>white space characters are forbidden</li>
     * <li>shall not contain the user's login, first or last name</li>
     * <ul>
     * see <a href="http://technet.microsoft.com/en-us/library/cc757692(WS.10).aspx#w2k3tr_sepol_accou_set_kuwh">...</a>
     */
    public boolean validatePassword(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value == null) {
            throw new BfabricValidatorException("required");
        }

        final char[] newPassword = (char[]) value;

        if (newPassword.length < 8) {
            throw new BfabricValidatorException("passwordMinLengthException");
        }
        if (newPassword.length > 32) {
            throw new BfabricValidatorException("passwordMaxLengthException");
        }

        Pattern pattern = Pattern.compile("[A-Z]+");
        Matcher matcher = pattern.matcher(CharBuffer.wrap(newPassword));
        if (!matcher.find()) {
            throw new BfabricValidatorException("passwordNoUpperCaseCharException");
        }
        pattern = Pattern.compile("[a-z]+");
        matcher = pattern.matcher(CharBuffer.wrap(newPassword));
        if (!matcher.find()) {
            throw new BfabricValidatorException("passwordNoLowerCaseCharException");
        }
        pattern = Pattern.compile("[0-9]+");
        matcher = pattern.matcher(CharBuffer.wrap(newPassword));
        if (!matcher.find()) {
            throw new BfabricValidatorException("passwordNoNumbersException");
        }
        pattern = Pattern.compile("\\s+");
        matcher = pattern.matcher(CharBuffer.wrap(newPassword));
        if (matcher.find()) {
            throw new BfabricValidatorException("passwordNoSpacesException");
        }

        // Check whether the lastname, firstname, or login is contained in the password
        final String[] nameStrings = new String[3];
        if (((String) uiComponent.getAttributes().get("firstNameId")).isEmpty()) {
            nameStrings[0] = getFirstName();
            nameStrings[1] = getLastName();
            nameStrings[2] = getLogin();
        } else {
            final Map<String, Object> map = uiComponent.getAttributes();
            String name = (String) map.get("firstNameId");
            UIInput input = (UIInput) facesContext.getViewRoot().findComponent(name);
            nameStrings[0] = (String) input.getSubmittedValue();

            name = (String) map.get("lastNameId");
            input = (UIInput) facesContext.getViewRoot().findComponent(name);
            nameStrings[1] = (String) input.getSubmittedValue();

            name = (String) map.get("loginId");
            input = (UIInput) facesContext.getViewRoot().findComponent(name);
            nameStrings[2] = (String) input.getLocalValue();
        }

        for (final String nameString : nameStrings) {
            logger.fine("nameStrings: " + nameString);
            if (nameString != null && nameString.length() > 2 && StringHelper.containsSubstring(newPassword, nameString)) {
                throw new BfabricValidatorException("passwordNameException");
            }
        }

        return true;
    }
}