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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
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
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.Dashboard;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.list.InstrumentEventList;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.UserService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.AJAX;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.bfabric.util.TokenUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.primefaces.event.FileUploadEvent;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "instrument_label_unique", columnNames = { "label" }) })
@XmlRootElement
@NamedQuery(name = "Instrument.findAllOrderByLabel", query = "SELECT a FROM Instrument a ORDER BY a.label")
@NamedQuery(name = "Instrument.findBookable", query = "SELECT a FROM Instrument a WHERE a.instrumentStatusInfo.bookable = true ORDER BY a.name")
@NamedQuery(name = "Instrument.findBookableIncluding", query = "SELECT a FROM Instrument a WHERE a.instrumentStatusInfo.bookable = true or a = :entity ORDER BY a.label")
@NamedQuery(name = "Instrument.findRunEnabled", query = "SELECT a FROM Instrument a WHERE a.instrumentStatusInfo.runEnabled = true ORDER BY a.name")
@NamedQuery(name = "Instrument.findRunEnabledIncluding", query = "SELECT a FROM Instrument a WHERE a.instrumentStatusInfo.runEnabled = true or a = :entity ORDER BY a.label")
public class Instrument extends AbstractSupervisorNamedBaseEntity implements ShowScreen, TechnologiesDependent, Indexable, Dashboard {

    private static final long serialVersionUID = 1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminid")
    @XmlIDREF
    private User admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotationid")
    @XmlIDREF
    private Annotation annotation;

    @OneToMany(mappedBy = "instrument")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> applications = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumentbooker", joinColumns = @JoinColumn(name = "instrumentid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<User> bookers = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = { CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> childInstruments = new HashSet<>();

    @Size(max = 512)
    @XmlElement
    private String computer;

    @ManyToMany
    @JoinTable(name = "contractinstrument", joinColumns = @JoinColumn(name = "instrumentid"), inverseJoinColumns = @JoinColumn(name = "contractid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Contract> contracts = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyid")
    @XmlIDREF
    private Currency currency;

    @Transient
    private Boolean currentUserBooker;

    @Transient
    private Boolean currentUserTrained;

    @OneToMany(mappedBy = "instrument")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Demultiplexing> demultiplexing = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "imageid")
    @XmlIDREF
    private Image image;

    @XmlElement
    private LocalDate installationDate;

    @OneToMany(mappedBy = "instrument")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentDataDelivery> instrumentDataDeliveries = new HashSet<>();

    @OneToMany(mappedBy = "instrument")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentDataPackage> instrumentDataPackages = new HashSet<>();

    @OneToMany(mappedBy = "instrument", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentEvent> instrumentEvent = new HashSet<>();

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "instrument")
    @OrderBy("id desc")
    private Set<InstrumentEvent> instrumentEvents = new HashSet<>();

    @OneToMany(mappedBy = "instrument")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReadConfiguration> instrumentReadConfigurations = new HashSet<>();

    @Embedded
    private InstrumentStatusInfo instrumentStatusInfo = new InstrumentStatusInfo();

    @Size(max = 32)
    @XmlElement
    private String inventoryNumber;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String label;

    @Transient
    private InstrumentReservationSetting lastReservationSetting;

    @Transient
    private InstrumentStatus lastStatus;

    @Size(max = 64)
    @XmlElement
    private String location;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<InstrumentNote> notes = new HashSet<>();

    @Transient
    private User oldAdmin;

    @Transient
    private boolean oldAvailable;

    @Transient
    private boolean oldBookable;

    @Transient
    private Instrument oldParent;

    @Transient
    private boolean oldRunEnabled;

    @Transient
    private User oldSupervisor;

    @Transient
    private boolean oldUp;

    @Transient
    private boolean oldUserBookable;

    @Transient
    private boolean oldUserVisible;

    @ManyToMany
    @JoinTable(name = "instrumentoperator", joinColumns = @JoinColumn(name = "instrumentid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<User> operators = new HashSet<>();

    @OneToMany(mappedBy = "instrument")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentid")
    @XmlIDREF
    private Instrument parent;

    @XmlElement
    private LocalDate purchasedDate;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal purchasedPrice;

    @OneToMany(mappedBy = "instrument")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReadConfiguration> readConfigurations = new HashSet<>();

    @OneToMany(mappedBy = "instrument", cascade = { CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlElement(name = "setting")
    private List<InstrumentReservationSetting> reservationSettings = new ArrayList<>();

    @OneToMany(mappedBy = "instrument", cascade = { CascadeType.REMOVE })
    @OrderBy("startDate desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReservation> reservations = new HashSet<>();

    @OneToMany(mappedBy = "instrument")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<RunUnitType> runUnitTypes = new HashSet<>();

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "instrument")
    @OrderBy("id desc")
    private Set<Run> runs = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocolinstrument", joinColumns = @JoinColumn(name = "instrumentid"), inverseJoinColumns = @JoinColumn(name = "samplepreparationprotocolid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<SamplePreparationProtocol> samplePreparationProtocols = new HashSet<>();

    @Size(max = 512)
    @XmlElement
    private String seller;

    @Size(max = 64)
    @XmlElement
    private String sellerContact;

    @Size(max = 128)
    @XmlElement
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceid")
    @XmlIDREF
    private Service service;

    @ManyToMany
    @JoinTable(name = "instrumentservicetype", joinColumns = @JoinColumn(name = "instrumentid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("id desc")
    @XmlIDREF
    @XmlElement(name = "servicetype")
    private Set<ServiceType> serviceTypes = new HashSet<>();

    @Transient
    private boolean showChildInstrumentEvents;

    @OneToMany(mappedBy = "instrument", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<InstrumentStatus> states = new ArrayList<>();

    @OneToMany(mappedBy = "instrument")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<InstrumentStatus> statesReverseOrdered = new ArrayList<>();

    @NotNull
    private Instant statusModified;

    @NotEmpty
    @ManyToMany
    @JoinTable(name = "instrumenttechnology", joinColumns = @JoinColumn(name = "instrumentid"), inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technology")
    private String technologiesAsString;

    @ManyToMany
    @JoinTable(name = "instrumenttraineduser", joinColumns = @JoinColumn(name = "instrumentid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<User> trainedUsers = new HashSet<>();

    public Instrument() {
        setEnabled(false);
    }

    public void addImage(byte[] image) {
        if (getImage() == null) {
            setImage(new Image());
        }
        getImage().setContent(image);
    }

    @Override
    public Instrument clone() throws CloneNotSupportedException {
        Instrument clone = (Instrument) super.clone();
        clone.states = new ArrayList<>();
        clone.applications = new HashSet<>();
        clone.bookers = new HashSet<>();
        if (!getBookers().isEmpty()) {
            clone.bookers.addAll(getBookers());
        }
        clone.demultiplexing = new HashSet<>();
        clone.notes = new HashSet<>();
        clone.operators = new HashSet<>();
        if (!getOperators().isEmpty()) {
            clone.operators.addAll(getOperators());
        }
        clone.contracts = new HashSet<>();
        clone.childInstruments = new HashSet<>();
        clone.instrumentDataDeliveries = new HashSet<>();
        clone.instrumentDataPackages = new HashSet<>();
        clone.instrumentReadConfigurations = new HashSet<>();
        clone.orders = new HashSet<>();
        clone.serviceTypes = new HashSet<>();
        clone.samplePreparationProtocols = new HashSet<>();
        clone.reservationSettings = new ArrayList<>();
        clone.runUnitTypes = new HashSet<>();
        clone.statesReverseOrdered = new ArrayList<>();
        clone.trainedUsers = new HashSet<>();
        if (!getTrainedUsers().isEmpty()) {
            clone.trainedUsers.addAll(getTrainedUsers());
        }
        return clone;
    }

    public void exportAndDownloadIcs(Set<InstrumentReservation> instrumentReservations) {
        Event event = new Event();
        Set<InstrumentReservation> exportInstrumentReservations = new HashSet<>();
        if (instrumentReservations != null && !instrumentReservations.isEmpty()) {
            exportInstrumentReservations.addAll(instrumentReservations);
        } else {
            exportInstrumentReservations.addAll(getReservations());
        }
        event.download(getClassLabelLowerCaseId() + "_instrumentreservations.ics", event.getIcsExport(exportInstrumentReservations).toString());
    }

    public User getAdmin() {
        return admin;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public Set<User> getBookers() {
        return bookers;
    }

    public List<User> getBookersAsList() {
        return CollectionHelper.asList(bookers);
    }

    public String getBookersAsString() {
        return CollectionHelper.printNames(getBookers());
    }

    public Set<Charge> getCharges() {
        Set<Charge> charges = new HashSet<>();
        for (InstrumentReservation reservation : getReservations()) {
            charges.addAll(reservation.getCharges());
        }
        return charges;
    }

    public Set<Contract> getChildContracts() {
        Set<Contract> childContracts = new TreeSet<>(Collections.reverseOrder());
        childContracts.addAll(getContracts());
        for (Instrument childInstrument : getChildInstruments()) {
            childContracts.addAll(childInstrument.getChildContracts());
        }
        return childContracts;
    }

    public int getChildInstrumentEventsCount() {
        return CDI.current().select(InstrumentEventList.class).get().getLazyModelTransitiveByInstrumentIdAndUser(getId(), getCurrentUser()).size() - CDI.current()
            .select(InstrumentEventList.class).get().getLazyModelByInstrumentIdAndUser(getId(), getCurrentUser()).size();
    }

    public Set<Instrument> getChildInstruments() {
        return childInstruments;
    }

    public String getComputer() {
        return computer;
    }

    public Set<Contract> getContracts() {
        return contracts;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    public Set<Demultiplexing> getDemultiplexing() {
        return demultiplexing;
    }

    public Set<Instrument> getDescendants() {
        Set<Instrument> descendants = new HashSet<>();
        return getDescendants(descendants);
    }

    public Set<Instrument> getDescendants(Set<Instrument> descendants) {
        for (Instrument instrument : getChildInstruments()) {
            if (!descendants.contains(instrument)) {
                descendants.add(instrument);
                instrument.getDescendants(descendants);
            }
        }
        return descendants;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    public Duration getDurationAvailableTimeNow() {
        return getDurationNow(getStartDateStatusAvailable());
    }

    public String getDurationAvailableTimeNowAsText() {
        return StringHelper.getFormattedDuration(getDurationAvailableTimeNow());
    }

    public Duration getDurationAvailableTimeTotalNow() {
        return isAvailable() ? getDurationSum(getInstrumentStatusInfo().getAvailableTimeTotal(), getDurationLastStatusNow()) : getInstrumentStatusInfo().getAvailableTimeTotal();
    }

    public String getDurationAvailableTimeTotalNowAsText() {
        return StringHelper.getFormattedDuration(getDurationAvailableTimeTotalNow());
    }

    public Duration getDurationBookableTimeNow() {
        return getDurationNow(getStartDateStatusBookable());
    }

    public String getDurationBookableTimeNowAsText() {
        return StringHelper.getFormattedDuration(getDurationBookableTimeNow());
    }

    public Duration getDurationBookableTimeTotalNow() {
        return isBookable() ? getDurationSum(getInstrumentStatusInfo().getBookableTimeTotal(), getDurationLastStatusNow()) : getInstrumentStatusInfo().getBookableTimeTotal();
    }

    public String getDurationBookableTimeTotalNowAsText() {
        return StringHelper.getFormattedDuration(getDurationBookableTimeTotalNow());
    }

    public Duration getDurationLastStatusNow() {
        return getLastStatus() != null ? getDurationNow(getLastStatus().getCreated()) : null;
    }

    public Duration getDurationNow(LocalDateTime localDateTime) {
        return localDateTime != null ? Duration.between(localDateTime, LocalDateTime.now()) : null;
    }

    public Duration getDurationRunEnabledTimeNow() {
        return getDurationNow(getStartDateStatusRunEnabled());
    }

    public String getDurationRunEnabledTimeNowAsText() {
        return StringHelper.getFormattedDuration(getDurationRunEnabledTimeNow());
    }

    public Duration getDurationRunEnabledTimeTotalNow() {
        return isRunEnabled() ? getDurationSum(getInstrumentStatusInfo().getRunEnabledTimeTotal(), getDurationLastStatusNow()) : getInstrumentStatusInfo().getRunEnabledTimeTotal();
    }

    public String getDurationRunEnabledTimeTotalNowAsText() {
        return StringHelper.getFormattedDuration(getDurationRunEnabledTimeTotalNow());
    }

    public Duration getDurationSum(Duration duration1, Duration duration2) {
        if (duration1 != null) {
            return duration2 != null ? duration1.plus(duration2) : duration1;
        }
        return duration2;
    }

    public Duration getDurationUpTimeNow() {
        return getDurationNow(getStartDateStatusUp());
    }

    public String getDurationUpTimeNowAsText() {
        return StringHelper.getFormattedDuration(getDurationUpTimeNow());
    }

    public Duration getDurationUpTimeTotalNow() {
        return isUp() ? getDurationSum(getInstrumentStatusInfo().getUpTimeTotal(), getDurationLastStatusNow()) : getInstrumentStatusInfo().getUpTimeTotal();
    }

    public String getDurationUpTimeTotalNowAsText() {
        return StringHelper.getFormattedDuration(getDurationUpTimeTotalNow());
    }

    public Duration getDurationUserBookableTimeNow() {
        return getDurationNow(getStartDateStatusUserBookable());
    }

    public String getDurationUserBookableTimeNowAsText() {
        return StringHelper.getFormattedDuration(getDurationUserBookableTimeNow());
    }

    public Duration getDurationUserBookableTimeTotalNow() {
        return isUserBookable() ? getDurationSum(getInstrumentStatusInfo().getUserBookableTimeTotal(), getDurationLastStatusNow()) : getInstrumentStatusInfo().getUserBookableTimeTotal();
    }

    public String getDurationUserBookableTimeTotalNowAsText() {
        return StringHelper.getFormattedDuration(getDurationUserBookableTimeTotalNow());
    }

    public Duration getDurationUserVisibleTimeNow() {
        return getDurationNow(getStartDateStatusUserVisible());
    }

    public String getDurationUserVisibleTimeNowAsText() {
        return StringHelper.getFormattedDuration(getDurationUserVisibleTimeNow());
    }

    public Duration getDurationUserVisibleTimeTotalNow() {
        return isUserVisible() ? getDurationSum(getInstrumentStatusInfo().getUserVisibleTimeTotal(), getDurationLastStatusNow()) : getInstrumentStatusInfo().getUserVisibleTimeTotal();
    }

    public String getDurationUserVisibleTimeTotalNowAsText() {
        return StringHelper.getFormattedDuration(getDurationUserVisibleTimeTotalNow());
    }

    public List<User> getEmployeesIncludingAdmin(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getAdmin());
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getLabel())) {
            addEntityInfoItem(summary, "label", getLabel());
        }
        if (getAdmin() != null) {
            addEntityInfoItem(summary, "admin", getAdmin().getName());
        }
        if (getParent() != null) {
            addEntityInfoItem(summary, "parent", getParent().getName());
        }
        if (hasCurrentUserRoleEnum(RoleEnum.INSTRUMENTREADER)) {
            if (getStates() != null && !getStates().isEmpty()) {
                addEntityInfoItem(summary, "states", getStates().size());
            }
            addEntityInfoItem(summary, "up", isUp());
            if (StringHelper.isNotEmpty(getDurationUpTimeNowAsText())) {
                addEntityInfoItem(summary, "upTime", getDurationUpTimeNowAsText());
            }
            if (StringHelper.isNotEmpty(getDurationUpTimeTotalNowAsText())) {
                addEntityInfoItem(summary, "upTotal", getDurationUpTimeTotalNowAsText());
            }
            addEntityInfoItem(summary, "bookable", isBookable());
            if (StringHelper.isNotEmpty(getDurationBookableTimeNowAsText())) {
                addEntityInfoItem(summary, "bookableTime", getDurationBookableTimeNowAsText());
            }
            if (StringHelper.isNotEmpty(getDurationBookableTimeTotalNowAsText())) {
                addEntityInfoItem(summary, "bookableTimeTotal", getDurationBookableTimeTotalNowAsText());
            }
            addEntityInfoItem(summary, "userBookable", isUserBookable());
            if (StringHelper.isNotEmpty(getDurationUserBookableTimeNowAsText())) {
                addEntityInfoItem(summary, "userBookableTime", getDurationBookableTimeNowAsText());
            }
            if (StringHelper.isNotEmpty(getDurationUserBookableTimeTotalNowAsText())) {
                addEntityInfoItem(summary, "userBookableTimeTotal", getDurationBookableTimeTotalNowAsText());
            }
            addEntityInfoItem(summary, "available", isAvailable());
            if (StringHelper.isNotEmpty(getDurationAvailableTimeTotalNowAsText())) {
                addEntityInfoItem(summary, "availableTime", getDurationAvailableTimeNowAsText());
            }
            if (StringHelper.isNotEmpty(getDurationAvailableTimeTotalNowAsText())) {
                addEntityInfoItem(summary, "availableTimeTotal", getDurationAvailableTimeTotalNowAsText());
            }
            addEntityInfoItem(summary, "userVisible", isUserVisible());
            if (StringHelper.isNotEmpty(getDurationUserVisibleTimeNowAsText())) {
                addEntityInfoItem(summary, "userVisibleTime", getDurationUserVisibleTimeNowAsText());
            }
            if (StringHelper.isNotEmpty(getDurationUserVisibleTimeTotalNowAsText())) {
                addEntityInfoItem(summary, "userVisibleTimeTotal", getDurationUserVisibleTimeTotalNowAsText());
            }
            addEntityInfoItem(summary, "runEnabled", isRunEnabled());
            if (StringHelper.isNotEmpty(getDurationRunEnabledTimeNowAsText())) {
                addEntityInfoItem(summary, "runEnabledTime", getDurationRunEnabledTimeNowAsText());
            }
            if (StringHelper.isNotEmpty(getDurationRunEnabledTimeTotalNowAsText())) {
                addEntityInfoItem(summary, "runEnabledTimeTotal", getDurationRunEnabledTimeTotalNowAsText());
            }
            if (getService() != null) {
                addEntityInfoItem(summary, "defaultService", getService().getName());
            }
            if (StringHelper.isNotEmpty(getSerialNumber())) {
                addEntityInfoItem(summary, "serialNumber", getSerialNumber());
            }
            if (StringHelper.isNotEmpty(getInventoryNumber())) {
                addEntityInfoItem(summary, "inventoryNumber", getInventoryNumber());
            }
            if (StringHelper.isNotEmpty(getLocation())) {
                addEntityInfoItem(summary, "location", getLocation());
            }
            if (StringHelper.isNotEmpty(getComputer())) {
                addEntityInfoItem(summary, "computer", getComputer());
            }
            if (getInstallationDate() != null) {
                addEntityInfoItem(summary, "installationDate", getInstallationDate());
            }
            if (getPurchasedDate() != null) {
                addEntityInfoItem(summary, "purchasedDate", getPurchasedDate());
            }
            if (getPurchasedPrice() != null) {
                addEntityInfoItem(summary, "purchasedPrice", getPurchasedPrice());
            }
            if (getCurrency() != null) {
                addEntityInfoItem(summary, "currency", getCurrency().getCode());
            }
            if (StringHelper.isNotEmpty(getSeller())) {
                addEntityInfoItem(summary, "seller", getSeller());
            }
            if (StringHelper.isNotEmpty(getSellerContact())) {
                addEntityInfoItem(summary, "sellerContact", getSellerContact());
            }
        }
        return summary.toString();
    }

    public Image getImage() {
        return image;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.PARENT.getField());
        fields.add(IndexMapContentEnum.SUPERVISOR.getField());
        fields.add(IndexMapContentEnum.ADMIN.getField());
        fields.add(IndexMapContentEnum.TECHNOLOGY.getField());
        fields.add(IndexMapContentEnum.ENABLED.getField());
        fields.add(IndexMapContentEnum.UP.getField());
        fields.add(IndexMapContentEnum.BOOKABLE.getField());
        fields.add(IndexMapContentEnum.USERVISIBLE.getField());
        fields.add(IndexMapContentEnum.USERBOOKABLE.getField());
        fields.add(IndexMapContentEnum.RUNENABLED.getField());
        fields.add(IndexMapContentEnum.AVAILABLE.getField());
        fields.add(IndexMapContentEnum.BOOKER.getField());
        fields.add(IndexMapContentEnum.TRAINEDUSER.getField());
        fields.add(IndexMapContentEnum.OPERATOR.getField());
        fields.add(IndexMapContentEnum.SERIALNUMBER.getField());
        fields.add(IndexMapContentEnum.INVENTORYNUMBER.getField());
        fields.add(IndexMapContentEnum.COMPUTER.getField());
        fields.add(IndexMapContentEnum.INSTALLATIONDATE.getField());
        fields.add(IndexMapContentEnum.PURCHASEDDATE.getField());
        fields.add(IndexMapContentEnum.PURCHASEDPRICE.getField());
        fields.add(IndexMapContentEnum.CURRENCY.getField());
        fields.add(IndexMapContentEnum.SELLER.getField());
        fields.add(IndexMapContentEnum.SELLERCONTACT.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());

        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getName());
        if (getParent() != null) {
            content.add(IndexMapContentEnum.PARENT, getParent().getLabel());
        }
        if (getSupervisor() != null) {
            content.add(IndexMapContentEnum.SUPERVISOR, getSupervisor().getFullName());
            content.add(IndexMapContentEnum.SUPERVISOR, getSupervisor().getLogin());
        }
        if (getAdmin() != null) {
            content.add(IndexMapContentEnum.ADMIN, getAdmin().getFullName());
            content.add(IndexMapContentEnum.ADMIN, getAdmin().getLogin());
        }
        for (User booker : getBookers()) {
            content.add(IndexMapContentEnum.BOOKER, booker.getLastNameFirstName(), CollectionHelper.print(getBookers()));
        }
        for (User trainedUser : getTrainedUsers()) {
            content.add(IndexMapContentEnum.TRAINEDUSER, trainedUser.getLastNameFirstName(), CollectionHelper.print(getTrainedUsers()));
        }
        for (User operator : getOperators()) {
            content.add(IndexMapContentEnum.OPERATOR, operator.getLastNameFirstName(), CollectionHelper.print(getOperators()));
        }
        if (StringHelper.isNotEmpty(getTechnologiesAsString())) {
            content.add(IndexMapContentEnum.TECHNOLOGY, getTechnologiesAsString());
        }
        content.add(IndexMapContentEnum.ENABLED, isEnabled());
        if (getInstrumentStatusInfo() != null) {
            content.add(IndexMapContentEnum.UP, getInstrumentStatusInfo().isUp());
            content.add(IndexMapContentEnum.BOOKABLE, getInstrumentStatusInfo().isBookable());
            content.add(IndexMapContentEnum.USERVISIBLE, getInstrumentStatusInfo().isUserVisible());
            content.add(IndexMapContentEnum.USERBOOKABLE, getInstrumentStatusInfo().isUserBookable());
            content.add(IndexMapContentEnum.RUNENABLED, getInstrumentStatusInfo().isRunEnabled());
            content.add(IndexMapContentEnum.AVAILABLE, getInstrumentStatusInfo().isAvailable());
        }

        content.add(IndexMapContentEnum.SERIALNUMBER, getSerialNumber());
        content.add(IndexMapContentEnum.INVENTORYNUMBER, getInventoryNumber());
        content.add(IndexMapContentEnum.COMPUTER, getComputer());
        content.add(IndexMapContentEnum.INSTALLATIONDATE, getInstallationDate());
        content.add(IndexMapContentEnum.PURCHASEDDATE, getPurchasedDate());
        content.add(IndexMapContentEnum.PURCHASEDPRICE, getPurchasedPrice());
        if (getCurrency() != null) {
            content.add(IndexMapContentEnum.CURRENCY, getCurrency().getCode());
        }
        content.add(IndexMapContentEnum.SELLER, getSeller());
        content.add(IndexMapContentEnum.SELLERCONTACT, getSellerContact());
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.INSTRUMENT;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public Set<InstrumentDataDelivery> getInstrumentDataDeliveries() {
        return instrumentDataDeliveries;
    }

    public Set<InstrumentDataPackage> getInstrumentDataPackages() {
        return instrumentDataPackages;
    }

    public Set<InstrumentEvent> getInstrumentEvents() {
        return instrumentEvents;
    }

    public BfabricLazyDataModel<InstrumentEvent> getInstrumentEventsLazyModel() {
        return isShowChildInstrumentEvents() ? CDI.current().select(InstrumentEventList.class).get().getLazyModelTransitiveByInstrumentIdAndUser(getId(), getCurrentUser()) : CDI.current()
            .select(InstrumentEventList.class).get().getLazyModelByInstrumentIdAndUser(getId(), getCurrentUser());
    }

    public Set<InstrumentReadConfiguration> getInstrumentReadConfigurations() {
        return instrumentReadConfigurations;
    }

    public InstrumentStatusInfo getInstrumentStatusInfo() {
        return instrumentStatusInfo;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelSpecifics() {
        final StringBuilder summary = new StringBuilder();
        if (StringHelper.isNotEmpty(getLabel())) {
            addEntityInfoItem(summary, "label", getLabel());
        }
        if (StringHelper.isNotEmpty(getLabel())) {
            addEntityInfoItem(summary, "name", getName());
        }
        if (getSupervisor() != null) {
            addEntityInfoItem(summary, "admin", getSupervisor().getName());
        }
        if (getAdmin() != null) {
            addEntityInfoItem(summary, "admin", getAdmin().getName());
        }
        if (getParent() != null) {
            addEntityInfoItem(summary, "parent", getParent().getName());
        }
        if (StringHelper.isNotEmpty(getLocation())) {
            addEntityInfoItem(summary, "location", getLocation());
        }
        return summary.toString();
    }

    public InstrumentReservationSetting getLastReservationSetting() {
        if (lastReservationSetting == null && !getReservationSettings().isEmpty()) {
            lastReservationSetting = getReservationSettings().get(0);
        }
        return lastReservationSetting;
    }

    public InstrumentStatus getLastStatus() {
        if (lastStatus == null && !getStates().isEmpty()) {
            lastStatus = getStates().get(getStates().size() - 1);
        }
        return lastStatus;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.INSTRUMENT_NOTE;
    }

    public Set<InstrumentNote> getNotes() {
        return notes;
    }

    public User getOldAdmin() {
        return oldAdmin;
    }

    public Instrument getOldParent() {
        return oldParent;
    }

    public User getOldSupervisor() {
        return oldSupervisor;
    }

    public Set<User> getOperators() {
        return operators;
    }

    public List<User> getOperatorsAsList() {
        return CollectionHelper.asList(operators);
    }

    public String getOperatorsAsString() {
        return CollectionHelper.printNames(getOperators());
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Instrument getParent() {
        return parent;
    }

    public LocalDate getPurchasedDate() {
        return purchasedDate;
    }

    public BigDecimal getPurchasedPrice() {
        return purchasedPrice;
    }

    public Set<InstrumentReadConfiguration> getReadConfigurations() {
        return readConfigurations;
    }

    public Optional<InstrumentReservationSetting> getReservationSetting(LocalDate localDate) {
        return getReservationSettings().stream().filter(setting -> setting.getValidLocalDateInterval().contains(localDate)).findFirst();
    }

    public List<InstrumentReservationSetting> getReservationSettings() {
        return reservationSettings;
    }

    public Set<InstrumentReservation> getReservations() {
        return reservations;
    }

    public Set<RunUnitType> getRunUnitTypes() {
        return runUnitTypes;
    }

    @Override
    public List<Application> getRunnableApplications() {
        if (runnableApplications == null) {
            runnableApplications = getApplications().stream().filter(Application::isRunnable).collect(Collectors.toList());
        }
        return runnableApplications;
    }

    public Set<Run> getRuns() {
        return runs;
    }

    public Set<SamplePreparationProtocol> getSamplePreparationProtocols() {
        return samplePreparationProtocols;
    }

    public List<SamplePreparationProtocol> getSamplePreparationProtocolsAsList() {
        return CollectionHelper.asList(getSamplePreparationProtocols());
    }

    public String getSeller() {
        return seller;
    }

    public String getSellerContact() {
        return sellerContact;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Service getService() {
        return service;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public LocalDateTime getStartDateStatusAvailable() {
        LocalDateTime ret = null;
        if (isAvailable()) {
            for (InstrumentStatus instrumentStatus : getStatesReverseOrdered()) {
                if (!instrumentStatus.getInstrumentStatusInfo().isAvailable()) {
                    break;
                }
                ret = instrumentStatus.getCreated();
            }
        }
        return ret;
    }

    public LocalDateTime getStartDateStatusBookable() {
        LocalDateTime ret = null;
        if (isBookable()) {
            for (InstrumentStatus instrumentStatus : getStatesReverseOrdered()) {
                if (!instrumentStatus.getInstrumentStatusInfo().isBookable()) {
                    break;
                }
                ret = instrumentStatus.getCreated();
            }
        }
        return ret;
    }

    public LocalDateTime getStartDateStatusRunEnabled() {
        LocalDateTime ret = null;
        if (isRunEnabled()) {
            for (InstrumentStatus instrumentStatus : getStatesReverseOrdered()) {
                if (!instrumentStatus.getInstrumentStatusInfo().isRunEnabled()) {
                    break;
                }
                ret = instrumentStatus.getCreated();
            }
        }
        return ret;
    }

    public LocalDateTime getStartDateStatusUp() {
        LocalDateTime ret = null;
        if (isUp()) {
            for (InstrumentStatus instrumentStatus : getStatesReverseOrdered()) {
                if (!instrumentStatus.getInstrumentStatusInfo().isUp()) {
                    break;
                }
                ret = instrumentStatus.getCreated();
            }
        }
        return ret;
    }

    public LocalDateTime getStartDateStatusUserBookable() {
        LocalDateTime ret = null;
        if (isUserBookable()) {
            for (InstrumentStatus instrumentStatus : getStatesReverseOrdered()) {
                if (!instrumentStatus.getInstrumentStatusInfo().isUserBookable()) {
                    break;
                }
                ret = instrumentStatus.getCreated();
            }
        }
        return ret;
    }

    public LocalDateTime getStartDateStatusUserVisible() {
        LocalDateTime ret = null;
        if (isUserVisible()) {
            for (InstrumentStatus instrumentStatus : getStatesReverseOrdered()) {
                if (!instrumentStatus.getInstrumentStatusInfo().isUserVisible()) {
                    break;
                }
                ret = instrumentStatus.getCreated();
            }
        }
        return ret;
    }

    public List<InstrumentStatus> getStates() {
        return states;
    }

    public List<InstrumentStatus> getStatesReverseOrdered() {
        return statesReverseOrdered;
    }

    public Instant getStatusModified() {
        return statusModified;
    }

    public Set<Technology> getTechnologies() {
        return technologies;
    }

    @Override
    public String getTechnologiesAsString() {
        return technologiesAsString;
    }

    @Override
    public String getTechnologiesAsStringComputed() {
        return isChild() ? getParent().getTechnologiesAsString() : CollectionHelper.printNames(getTechnologies());
    }

    public Set<User> getTrainedUsers() {
        return trainedUsers;
    }

    public List<User> getTrainedUsersAsList() {
        return CollectionHelper.asList(trainedUsers);
    }

    public String getTrainedUsersAsString() {
        return CollectionHelper.printNames(getTrainedUsers());
    }

    public String getUrlCalendarForUserInstrumentReservations() {
        return new TokenUtils().getCalendarSyncUrl("instrument=" + getId());
    }

    public boolean hasAdminChanged() {
        return getOldAdmin() == null ? getAdmin() != null : !getOldAdmin().equals(getAdmin());
    }

    public boolean hasAvailableChanged() {
        return isOldAvailable() != isAvailable();
    }

    public boolean hasBookableChanged() {
        return isOldBookable() != isBookable();
    }

    public boolean hasInstrumentStatusInfoChanged() {
        return hasAvailableChanged() || hasBookableChanged() || hasUpChanged() || hasUserVisibleChanged() || hasUserBookableChanged() || hasRunEnabledChanged();
    }

    public boolean hasNewParent() {
        return getOldParent() == null ? getParent() != null : !getOldParent().equals(getParent());
    }

    public boolean hasRunEnabledChanged() {
        return isOldRunEnabled() != isRunEnabled();
    }

    public boolean hasSameAvailableAndUpStates(Instrument instrument) {
        return isAvailable() == instrument.isAvailable() && isUp() == instrument.isUp();
    }

    public boolean hasStateChanged() {
        return getId() == 0 || hasNewParent() || getInstrumentStatusInfo() != null && hasInstrumentStatusInfoChanged();
    }

    public boolean hasSupervisorChanged() {
        return getOldSupervisor() == null ? getSupervisor() != null : !getOldSupervisor().equals(getSupervisor());
    }

    public boolean hasUpChanged() {
        return isOldUp() != isUp();
    }

    public boolean hasUserBookableChanged() {
        return isOldUserBookable() != isUserBookable();
    }

    public boolean hasUserVisibleChanged() {
        return isOldUserVisible() != isUserVisible();
    }

    public void imageUploadListener(FileUploadEvent event) {
        if (getImage() == null) {
            setImage(new Image());
        }
        getImage().setContent(getFileUploadHelper().getImageUpload(event));
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntities(getContracts());
        IndexHelper.indexEntities(getReservations());
    }

    public boolean isAdminValid() {
        return !isEnabled() || getAdmin() != null && getAdmin().hasRoleImplicit(getDefaultRequiredRole());
    }

    public boolean isAvailable() {
        return getInstrumentStatusInfo() != null && getInstrumentStatusInfo().isAvailable();
    }

    public boolean isBookable() {
        return getInstrumentStatusInfo() != null && getInstrumentStatusInfo().isBookable();
    }

    public boolean isBookableByCurrentUser() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isBookable() || isCurrentUserBooker() || isUserBookable() && getBookers().isEmpty();
    }

    public boolean isChild() {
        return parent != null;
    }

    public boolean isCurrentUserBooker() {
        if (currentUserBooker == null) {
            currentUserBooker = getBookers().contains(getCurrentUser());
        }
        return currentUserBooker;
    }

    public boolean isCurrentUserTrained() {
        if (currentUserTrained == null) {
            currentUserTrained = getTrainedUsers().contains(getCurrentUser());
        }
        return currentUserTrained;
    }

    @Override
    public boolean isDeletable() {
        return getContracts().isEmpty() && getReservations().isEmpty() && getOrders().isEmpty() && getApplications().isEmpty() && getTrainedUsers().isEmpty() && isUpdatable();
    }

    public boolean isEventCreatable() {
        return isCreatable() || isUserBookable();
    }

    @Override
    public boolean isExtensible() {
        return isUpdatable();
    }

    public boolean isLoan() {
        return !CDI.current().select(InstrumentReservationService.class).get().getOnLoanEventsByIntervalAndInstrument(new LocalDateTimeInterval(LocalDateTime.now(), LocalDateTime.now()), this)
            .isEmpty();
    }

    public boolean isNotNullPurchasedPrice() {
        return purchasedPrice != null;
    }

    public boolean isOldAvailable() {
        return oldAvailable;
    }

    public boolean isOldBookable() {
        return oldBookable;
    }

    public boolean isOldRunEnabled() {
        return oldRunEnabled;
    }

    public boolean isOldUp() {
        return oldUp;
    }

    public boolean isOldUserBookable() {
        return oldUserBookable;
    }

    public boolean isOldUserVisible() {
        return oldUserVisible;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.USER) || hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isRunEnabled() {
        return getInstrumentStatusInfo() != null && getInstrumentStatusInfo().isRunEnabled();
    }

    public boolean isShowChildInstrumentEvents() {
        return showChildInstrumentEvents;
    }

    public boolean isShowChildInstrumentEventsRendered() {
        return getChildInstrumentEventsCount() > 0;
    }

    public boolean isUp() {
        return getInstrumentStatusInfo() != null && getInstrumentStatusInfo().isUp();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public boolean isUserBookable() {
        return getInstrumentStatusInfo() != null && getInstrumentStatusInfo().isUserBookable();
    }

    public boolean isUserVisible() {
        return getInstrumentStatusInfo() != null && getInstrumentStatusInfo().isUserVisible();
    }

    public boolean isVisibleByCurrentUser() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isUserVisible() || isCurrentUserBooker() || isUserBookable() && getBookers().isEmpty();
    }

    public void parentChanged(ValueChangeEvent event) {
        setParent((Instrument) event.getNewValue());
    }

    public void propagateChangesToAncestors(Instrument child, List<Instrument> ancestors, boolean isDecoupled) {
        if (child != null && ancestors != null) {
            boolean availableChild = child.isAvailable();
            boolean upChild = child.isUp();
            setOldAvailable(getInstrumentStatusInfo().isAvailable());
            setOldUp(getInstrumentStatusInfo().isUp());
            if (isDecoupled) {
                // Propagate to old ancestors.
                propagateChangesToAncestorsHelper(child, ancestors);
            } else {
                if (!availableChild) {
                    getInstrumentStatusInfo().setAvailable(false);
                    ancestors.add(this);
                } else {
                    if (!upChild) {
                        getInstrumentStatusInfo().setUp(false);
                        ancestors.add(this);
                    } else {
                        // Both availableChild and upChild must be true at this point as it is not possible to have an instrument with up = true and available = false.
                        propagateChangesToAncestorsHelper(child, ancestors);
                    }
                }
            }

            if (getParent() != null && !hasSameAvailableAndUpStates(getParent())) {
                getParent().propagateChangesToAncestors(this, ancestors, isDecoupled);
            }
        }
    }

    private void propagateChangesToAncestorsHelper(Instrument child, List<Instrument> ancestors) {
        if (child != null && ancestors != null && !ancestors.isEmpty()) {
            boolean newUp = true;
            if (!isUp()) {
                for (Instrument childInstrument : getChildInstruments()) {
                    if (!childInstrument.equals(child) && !childInstrument.isUp()) {
                        newUp = false;
                        break;
                    }
                }
                if (newUp) {
                    getInstrumentStatusInfo().setUp(true);
                    ancestors.add(this);
                }
            }

            // If up is true, the available state must be true too.
            if (!newUp && !isAvailable()) {
                boolean newAvailable = true;
                for (Instrument childInstrument : getChildInstruments()) {
                    if (!childInstrument.equals(child) && !childInstrument.isAvailable()) {
                        newAvailable = false;
                        break;
                    }
                }
                if (newAvailable) {
                    getInstrumentStatusInfo().setAvailable(true);
                    ancestors.add(this);
                }
            }
        }
    }

    public void propagateChangesToDescendants(List<Instrument> descendants) {
        if (descendants != null) {
            for (Instrument child : getChildInstruments()) {
                // Temporary keep the old values as setParent creates a new status info and sets the admin as well as supervisor to the one of the parent.
                boolean temporaryOldAvailable = child.getInstrumentStatusInfo().isAvailable();
                boolean temporaryOldUp = child.getInstrumentStatusInfo().isUp();
                User temporaryOldSupervisor = child.getSupervisor();
                User temporaryOldAdmin = child.getAdmin();
                child.setParent(this);
                child.setOldAvailable(temporaryOldAvailable);
                child.setOldUp(temporaryOldUp);
                child.setOldAdmin(temporaryOldAdmin);
                child.setOldSupervisor(temporaryOldSupervisor);
                child.propagateChangesToDescendants(descendants);
                if (child.hasAvailableChanged() || child.hasUpChanged() || child.hasAdminChanged() || child.hasSupervisorChanged()) {
                    descendants.add(child);
                }
            }
        }
    }

    public void propagateChangesToHierarchy(boolean isDeleted, List<Instrument> updateDescendants, List<Instrument> updateAncestors) {
        if (!isDeleted) {
            // Propagating the changes made to the instrument to its descendants.
            propagateChangesToDescendants(updateDescendants);
        }

        // Propagating the changes made to the instrument to its old and new ancestors.
        if (isDeleted || hasNewParent() || hasAvailableChanged() || hasUpChanged()) {
            // No need to check for bookable as child instruments have their bookable value always set to false.
            if ((isDeleted || hasNewParent()) && getOldParent() != null) {
                // Propagate to old ancestors.
                getOldParent().propagateChangesToAncestors(this, updateAncestors, true);
            }
            if (!isDeleted && getParent() != null && !hasSameAvailableAndUpStates(getParent())) {
                // Propagate to new or same ancestors.
                getParent().propagateChangesToAncestors(this, updateAncestors, false);
            }
            setEnabled(isAvailable());
        }
    }

    public void purchasedPriceChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setPurchasedPrice(new BigDecimal(event.getNewValue().toString()));
        } else {
            setPurchasedPrice(null);
        }
    }

    public void selectionValueChanged(ValueChangeEvent event) {
        setAnnotation((Annotation) event.getNewValue());
        AJAX.update(Constants.EDIT + ":annotation");
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public void setBookers(Set<User> bookers) {
        this.bookers = bookers;
    }

    public void setBookersAsList(List<User> bookers) {
        this.bookers = (Set<User>) CollectionHelper.asSet(bookers);
    }

    public void setComputer(String computer) {
        this.computer = StringHelper.format(computer);
        if (this.computer != null && this.computer.isEmpty()) {
            this.computer = null;
        }
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setDemultiplexing(Set<Demultiplexing> demultiplexing) {
        this.demultiplexing = demultiplexing;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public void setInstrumentStatusInfo(InstrumentStatusInfo instrumentStatusInfo) {
        this.instrumentStatusInfo = instrumentStatusInfo;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = StringHelper.format(inventoryNumber);
    }

    public void setLabel(String label) {
        this.label = StringHelper.format(label);
    }

    public void setLocation(String location) {
        this.location = StringHelper.format(location);
    }

    public void setNotes(Set<InstrumentNote> notes) {
        this.notes = notes;
    }

    public void setOldAdmin(User oldAdmin) {
        this.oldAdmin = oldAdmin;
    }

    public void setOldAvailable(boolean oldAvailable) {
        this.oldAvailable = oldAvailable;
    }

    public void setOldBookable(boolean oldBookable) {
        this.oldBookable = oldBookable;
    }

    public void setOldParent(Instrument oldParent) {
        this.oldParent = oldParent;
    }

    public void setOldRunEnabled(boolean oldRunEnabled) {
        this.oldRunEnabled = oldRunEnabled;
    }

    public void setOldSupervisor(User oldSupervisor) {
        this.oldSupervisor = oldSupervisor;
    }

    public void setOldUp(boolean oldUp) {
        this.oldUp = oldUp;
    }

    public void setOldUserBookable(boolean oldUserBookable) {
        this.oldUserBookable = oldUserBookable;
    }

    public void setOldUserVisible(boolean oldUserVisible) {
        this.oldUserVisible = oldUserVisible;
    }

    public void setOldValues() {
        setOldParent(getParent());
        setOldSupervisor(getSupervisor());
        setOldAdmin(getAdmin());
        setOldAvailable(isAvailable());
        setOldBookable(isBookable());
        setOldUp(isUp());
        setOldUserBookable(isUserBookable());
        setOldRunEnabled(isRunEnabled());
        setOldUserVisible(isUserVisible());
        setTechnologiesAsString();
    }

    public void setOperators(Set<User> operators) {
        this.operators = operators;
    }

    public void setOperatorsAsList(List<User> operators) {
        this.operators = (Set<User>) CollectionHelper.asSet(operators);
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setParent(Instrument parent) {
        this.parent = parent;
        if (parent != null) {
            setSupervisor(parent.getSupervisor());
            setAdmin(parent.getAdmin());
            setTechnologies(new HashSet<>(parent.getTechnologies()));
            setService(parent.getService());
            setInstrumentStatusInfo(new InstrumentStatusInfo(parent));
            // Important: Child instruments are not bookable!
            getInstrumentStatusInfo().setBookable(false);
            setLocation(parent.getLocation());
            setEnabled(parent.isEnabled());
        }
    }

    public void setPurchasedDate(LocalDate purchasedDate) {
        this.purchasedDate = purchasedDate;
    }

    public void setPurchasedPrice(BigDecimal purchasedPrice) {
        this.purchasedPrice = NumberUtils.getDecimalScale2(purchasedPrice);
    }

    public void setReadConfigurations(Set<InstrumentReadConfiguration> readConfigurations) {
        this.readConfigurations = readConfigurations;
    }

    public void setReservationSettings(List<InstrumentReservationSetting> settings) {
        this.reservationSettings = settings;
    }

    public void setReservations(Set<InstrumentReservation> reservations) {
        this.reservations = reservations;
    }

    public void setSamplePreparationProtocols(Set<SamplePreparationProtocol> samplePreparationProtocols) {
        this.samplePreparationProtocols = samplePreparationProtocols;
    }

    public void setSamplePreparationProtocolsAsList(List<SamplePreparationProtocol> samplePreparationProtocols) {
        this.samplePreparationProtocols = (Set<SamplePreparationProtocol>) CollectionHelper.asSet(samplePreparationProtocols);
    }

    public void setSeller(String seller) {
        this.seller = StringHelper.format(seller);
    }

    public void setSellerContact(String sellerContact) {
        this.sellerContact = StringHelper.format(sellerContact);
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = StringHelper.format(serialNumber);
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setServiceTypes(Set<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setShowChildInstrumentEvents(boolean showChildInstrumentEvents) {
        this.showChildInstrumentEvents = showChildInstrumentEvents;
    }

    public void setStates(List<InstrumentStatus> states) {
        this.states = states;
    }

    public void setStatusModified(Instant statusModified) {
        this.statusModified = statusModified;
    }

    public void setStatusModified() {
        setStatusModified(Instant.now());
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }

    public void setTrainedUsers(Set<User> trainedUsers) {
        this.trainedUsers = trainedUsers;
    }

    public void setTrainedUsersAsList(List<User> trainedUsers) {
        this.trainedUsers = (Set<User>) CollectionHelper.asSet(trainedUsers);
    }

    public void switchAvailable(String statusComment) {
        getInstrumentStatusInfo().setAvailable(!isAvailable());
        updateState(statusComment);
        setEnabled(isAvailable());
    }

    public void switchBookable(String statusComment) {
        getInstrumentStatusInfo().setBookable(!isBookable());
        updateState(statusComment);
    }

    public void switchRunEnabled(String statusComment) {
        getInstrumentStatusInfo().setRunEnabled(!isRunEnabled());
        updateState(statusComment);
    }

    public void switchShowChildInstrumentEvents() {
        setShowChildInstrumentEvents(!isShowChildInstrumentEvents());
    }

    public void switchUp(String statusComment) {
        getInstrumentStatusInfo().setUp(!isUp());
        updateState(statusComment);
    }

    public void switchUserBookable(String statusComment) {
        getInstrumentStatusInfo().setUserBookable(!isUserBookable());
        updateState(statusComment);
    }

    public void switchUserVisible(String statusComment) {
        getInstrumentStatusInfo().setUserVisible(!isUserVisible());
        updateState(statusComment);
    }

    public void updateState(String statusComment) {
        if (hasStateChanged()) {
            InstrumentStatus instrumentStatus = new InstrumentStatus(this);
            instrumentStatus.getInstrumentStatusInfo().setStatusComment(statusComment);
            getStates().add(instrumentStatus);
            setStatusModified();
            getInstrumentStatusInfo().setUpTime(instrumentStatus.getInstrumentStatusInfo().getUpTime());
            getInstrumentStatusInfo().setUpTimeTotal(instrumentStatus.getInstrumentStatusInfo().getUpTimeTotal());
            getInstrumentStatusInfo().setAvailableTime(instrumentStatus.getInstrumentStatusInfo().getAvailableTime());
            getInstrumentStatusInfo().setAvailableTimeTotal(instrumentStatus.getInstrumentStatusInfo().getAvailableTimeTotal());
            getInstrumentStatusInfo().setBookableTime(instrumentStatus.getInstrumentStatusInfo().getBookableTime());
            getInstrumentStatusInfo().setBookableTimeTotal(instrumentStatus.getInstrumentStatusInfo().getBookableTimeTotal());
            getInstrumentStatusInfo().setUserBookableTime(instrumentStatus.getInstrumentStatusInfo().getUserBookableTime());
            getInstrumentStatusInfo().setUserBookableTimeTotal(instrumentStatus.getInstrumentStatusInfo().getUserBookableTimeTotal());
            getInstrumentStatusInfo().setRunEnabledTime(instrumentStatus.getInstrumentStatusInfo().getRunEnabledTime());
            getInstrumentStatusInfo().setRunEnabledTimeTotal(instrumentStatus.getInstrumentStatusInfo().getRunEnabledTimeTotal());
            getInstrumentStatusInfo().setUserVisibleTime(instrumentStatus.getInstrumentStatusInfo().getUserVisibleTime());
            getInstrumentStatusInfo().setUserVisibleTimeTotal(instrumentStatus.getInstrumentStatusInfo().getUserVisibleTimeTotal());
            getInstrumentStatusInfo().setStatusComment(instrumentStatus.getInstrumentStatusInfo().getStatusComment());
        }
    }
}