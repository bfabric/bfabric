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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.InstrumentReservationSettingService;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.DateUtils;
import org.bfabric.util.DurationHelper;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.RepeaterHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "InstrumentReservation.findByUser", query = "SELECT a FROM InstrumentReservation a WHERE a.user = :user")
@NamedQuery(name = "InstrumentReservation.findByInterval", query = "SELECT a FROM InstrumentReservation a WHERE a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findNonRejectedByInterval", query = "SELECT a FROM InstrumentReservation a WHERE (a.approved is null or a.approved = true) and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findRejectedByInterval", query = "SELECT a FROM InstrumentReservation a WHERE a.approved = false and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findApprovedByInterval", query = "SELECT a FROM InstrumentReservation a WHERE (a.instrumentReservationSetting.approvalRequired = false or a.approved = true) and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findApprovalPendingByInterval", query = "SELECT a FROM InstrumentReservation a WHERE a.instrumentReservationSetting.approvalRequired = true and a.approved is null and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findByIntervalAndUser", query = "SELECT a FROM InstrumentReservation a WHERE a.startDate <= :endDate and a.endDate >= :startDate and (a.booker.id = :userId or a.user.id = :userId)")
@NamedQuery(name = "InstrumentReservation.findByIntervalAndInstrument", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findNonRejectedByIntervalAndInstrument", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and (a.approved is null or a.approved = true) and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findRejectedByIntervalAndInstrument", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and a.approved = false and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findApprovedByIntervalAndInstrument", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and (a.instrumentReservationSetting.approvalRequired = false or a.approved = true)  and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findOnLoanByIntervalAndInstrument", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and lower(a.instrumentReservationType.name) = 'loan' and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findApprovalPendingByIntervalAndInstrument", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and a.instrumentReservationSetting.approvalRequired = true and a.approved is null and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findByIntervalAndInstrumentOrderByStartDate", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and a.startDate <= :endDate and a.endDate >= :startDate ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findByIntervalAndInstrumentExcludingIds", query = "SELECT a FROM InstrumentReservation a WHERE a.instrument = :instrument and a.startDate <= :endDate and a.endDate >= :startDate and a.id not in (:ids) ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.findCollidingEventsByInstrumentAndEvent", query = "SELECT a FROM InstrumentReservation a WHERE (a.approved is null or a.approved = true) and a.instrument = :instrument and a.startDate < :endDate and a.endDate > :startDate and id <> :id ORDER BY a.startDate")
@NamedQuery(name = "InstrumentReservation.checkByIntervalAndInstrumentExcluding", query = "SELECT a.id FROM InstrumentReservation a WHERE a.instrument = :instrument and a.startDate <= :endDate and a.endDate >= :startDate and a.id <> :id")
@NamedQuery(name = "InstrumentReservation.findUpcomingUserInstrumentReservation", query = "SELECT a FROM InstrumentReservation a WHERE a.user = :user and a.startDate > current_date and a.startDate < :endDate and a.endDate > :startDate")
@NamedQuery(name = "InstrumentReservation.existsFutureCollidingEventByOperator", query = "SELECT a.id FROM InstrumentReservation a WHERE a.operator.id = :userId and a.startDate > current_date and a.startDate < :endDate and a.endDate > :startDate")
@NamedQuery(name = "InstrumentReservation.existsFutureCollidingEventByUser", query = "SELECT a.id FROM Event a WHERE a.user.id = :userId and a.startDate > current_date and a.startDate < :endDate and a.endDate > :startDate")
@NamedQuery(name = "InstrumentReservation.checkByIntervalAndInstrument", query = "SELECT a.id FROM InstrumentReservation a WHERE a.instrument = :instrument and a.startDate <= :endDate and a.endDate >= :startDate")
@NamedQuery(name = "InstrumentReservation.findAllStartingSoon", query = "SELECT a FROM InstrumentReservation a WHERE a.reminderDate = current_date and a.instrument.instrumentStatusInfo.userBookable = true")
@NamedQuery(name = "InstrumentReservation.findMinDurationBySetting", query = "SELECT COALESCE(MIN(a.duration), 0) FROM InstrumentReservation a WHERE a.instrumentReservationSetting.id = :instrumentReservationSettingId ")
@NamedQuery(name = "InstrumentReservation.findMaxDurationBySetting", query = "SELECT COALESCE(MAX(a.duration), 0) FROM InstrumentReservation  a WHERE a.instrumentReservationSetting.id = :instrumentReservationSettingId")
@NamedQuery(name = "InstrumentReservation.findAnyChargeableBySetting", query = "SELECT a.id FROM InstrumentReservation a where a.instrumentReservationSetting.id = :instrumentReservationSettingId and a.chargeable = true")
@NamedQuery(name = "InstrumentReservation.findBySettingOrderedByEndDateDesc", query = "SELECT a FROM InstrumentReservation a where a.instrumentReservationSetting.id = :instrumentReservationSettingId ORDER BY a.endDate DESC")
@NamedQuery(name = "InstrumentReservation.findBySettingOrderedByStartDate", query = "SELECT a FROM InstrumentReservation a where a.instrumentReservationSetting.id = :instrumentReservationSettingId ORDER BY a.startDate")
public class InstrumentReservation extends AbstractEvent implements ShowScreen, Indexable {

    private static final long serialVersionUID = 1;

    @Transient
    Container container;

    @PastOrPresent
    @XmlElement
    private LocalDate approvalDate;

    @Size(max = 512)
    @XmlElement
    private String approvalNote;

    @XmlElement
    private Boolean approved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedbyid")
    @XmlIDREF
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookerid")
    @NotNull
    @XmlIDREF
    private User booker;

    @XmlElement
    private boolean chargeable;

    @ManyToMany
    @JoinTable(name = "chargeinstrumentreservation", joinColumns = @JoinColumn(name = "instrumentreservationid"), inverseJoinColumns = @JoinColumn(name = "chargeid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumentreservationcontainer", joinColumns = @JoinColumn(name = "instrumentreservationid"), inverseJoinColumns = @JoinColumn(name = "containerid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> containers = new HashSet<>();

    @Column(updatable = false, insertable = false)
    @XmlElement
    private Long duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentid")
    @NotNull
    @XmlIDREF
    private Instrument instrument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentreservationsettingid")
    @NotNull
    @XmlIDREF
    private InstrumentReservationSetting instrumentReservationSetting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentreservationtypeid")
    @XmlIDREF
    private InstrumentReservationType instrumentReservationType;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<InstrumentReservationNote> notes = new HashSet<>();

    @Transient
    private Set<Container> oldContainers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operatorid")
    @XmlIDREF
    private User operator;

    private LocalDate reminderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repeaterid")
    @XmlIDREF
    private InstrumentReservation repeater;

    @OneToMany(mappedBy = "repeater", cascade = { CascadeType.PERSIST })
    @OrderBy("startDate")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<InstrumentReservation> repeaterEvents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runid")
    @XmlIDREF
    private Run run;

    @Transient
    private boolean sendMailNotification = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicetypeid")
    @XmlIDREF
    private ServiceType serviceType;

    @Transient
    private LocalDateTime shiftedEndDate;

    @Transient
    private LocalDateTime shiftedStartDate;

    public InstrumentReservation() {
        setStartDate(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
        setEndDate(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1));
    }

    public void approve() {
        setApproved(true);
    }

    public void approvedChanged() {
        setApprovalDate(LocalDate.now());
        setApprovedBy(getCurrentUser());
    }

    @Override
    public InstrumentReservation clone() throws CloneNotSupportedException {
        InstrumentReservation clone = (InstrumentReservation) super.clone();
        clone.repeater = null;
        clone.repeaterEvents = new ArrayList<>();
        clone.charges = new HashSet<>();
        clone.notes = new HashSet<>();
        clone.approved = null;
        clone.approvedBy = null;
        clone.approvalDate = null;
        clone.reminderDate = null;
        if (!hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)) {
            clone.serviceType = null;
            clone.run = null;
            clone.booker = getCurrentUser();
        }
        return clone;
    }

    public InstrumentReservation cloneRepeating() throws CloneNotSupportedException {
        InstrumentReservation clone = clone();
        clone.setRepeater(this);
        clone.approved = approved;
        clone.approvedBy = approvedBy;
        clone.approvalDate = approvalDate;
        clone.reminderDate = reminderDate;
        return clone;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public String getApprovalLabel() {
        if (approved) {
            return Messages.get("approved").toLowerCase();
        } else {
            return Messages.get("rejected").toLowerCase();
        }
    }

    public String getApprovalNote() {
        return approvalNote;
    }

    public Boolean getApproved() {
        return approved;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public String getBackgroundColor() {
        return getInstrumentReservationType() != null && getInstrumentReservationType().getColor() != null ? getInstrumentReservationType().getColor() : "";
    }

    public User getBooker() {
        return booker;
    }

    public String getCalendarEventInfo() {
        StringBuilder info = new StringBuilder();
        info.append(getInstrument().getLabel()).append(" ").append(getDurationAccountedWithChargeTimeUnit());
        if (getInstrumentReservationType() != null) {
            info.append(" ").append(getInstrumentReservationType().getName());
        }
        if (getBooker() != null) {
            info.append(" ").append(Messages.get("booker")).append(": ").append(getBooker().getFullName());
        }
        if (getUser() != null) {
            info.append(" ").append(Messages.get("user")).append(": ").append(getUser().getFullName());
        }
        if (getOperator() != null) {
            info.append(" ").append(Messages.get("operator")).append(": ").append(getOperator().getFullName());
        }
        return info.toString();
    }

    public BigDecimal getChargeTime() {
        return DurationHelper.convertMinutesToChronoUnit(this);
    }

    public ChronoUnit getChargeTimeUnit() {
        return getInstrumentReservationSetting() != null ? getInstrumentReservationSetting().getChargeTimeUnit() : null;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public Container getContainer() {
        if (container == null) {
            container = getContainers() != null && getContainers().size() == 1 ? getContainersAsList().get(0) : null;
        }
        return container;
    }

    public Set<Container> getContainers() {
        return containers;
    }

    public List<Container> getContainersAsList() {
        return CollectionHelper.asList(containers);
    }

    public Set<User> getContainersCoaches() {
        Set<User> coaches = getContainers().stream().map(Container::getCoach).collect(Collectors.toSet());
        coaches.addAll(getContainers().stream().map(Container::getCoachBackup).collect(Collectors.toSet()));
        return coaches;
    }

    public Set<User> getContainersMembers() {
        return getContainers().stream().map(Container::getMembersTransitive).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public BigDecimal getCosts() {
        return getUnitPrice() != null ? getUnitPrice().multiply(getChargeTime()).setScale(2, RoundingMode.HALF_UP) : null;
    }

    public long getDayDuration() {
        long dayDuration = getDayMinTime().until(getDayMaxTime(), ChronoUnit.MINUTES);
        return dayDuration > 0 ? dayDuration : Duration.ofDays(1).toMinutes() + dayDuration;
    }

    public LocalTime getDayMaxTime() {
        return getInstrumentReservationSetting() != null ? LocalTime.MIDNIGHT.plus(getInstrumentReservationSetting().getMaxTime()) : null;
    }

    public LocalTime getDayMinTime() {
        return getInstrumentReservationSetting() != null ? LocalTime.MIDNIGHT.plus(getInstrumentReservationSetting().getMinTime()) : null;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    public Long getDuration() {
        return duration;
    }

    public String getDurationAccountedWithChargeTimeUnit() {
        return DurationHelper.convertMinutesToChronoUnit(Optional.ofNullable(getDuration())
            .orElse(getDurationComputed()), getChargeTimeUnit(), getDayDuration(), getInstrumentReservationSetting().isWeekends()).doubleValue() + " " + getChargeTimeUnit();
    }

    public long getDurationComputed() {
        long durationComputed = 0;
        InstrumentReservationSetting setting = getInstrumentReservationSetting();
        LocalDateTime maxStartDateTime = getStartDate().with(getDayMaxTime()).plusDays(getDayMaxTime().equals(LocalTime.MIDNIGHT) ? 1 : 0);
        LocalDateTime minEndDateTime = getEndDate().with(getDayMinTime());
        if (getStartDate().isBefore(maxStartDateTime) && (setting.isWeekends() || !DateUtils.isWeekend(getStartDate()))) {
            durationComputed += getStartDate().until(maxStartDateTime, ChronoUnit.MINUTES);
        }
        if (getEndDate().isAfter(minEndDateTime) && (setting.isWeekends() || !DateUtils.isWeekend(getEndDate()))) {
            durationComputed += minEndDateTime.until(getEndDate(), ChronoUnit.MINUTES);
        }
        long fullDays;
        if (getStartDate().toLocalDate().isEqual(getEndDate().toLocalDate())) {
            fullDays = -1;
        } else {
            fullDays = setting.isWeekends() ? Duration.between(getStartDate().toLocalDate().plusDays(1).atStartOfDay(), getEndDate().toLocalDate().atStartOfDay())
                .toDays() : DateUtils.getWorkingDaysCount(getStartDate().toLocalDate().plusDays(1), getEndDate().toLocalDate());
        }
        durationComputed += fullDays * getDayDuration();
        return durationComputed;
    }

    public List<User> getEmployeesIncludingApprovedBy(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getApprovedBy());
    }

    public List<User> getEmployeesIncludingBooker(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getBooker());
    }

    @Override
    public LocalDateTime getEndDateAllDayTime() {
        return getEndDateSlot() != null && getInstrumentReservationSetting() != null ? getEndDateSlot().atStartOfDay().plus(getInstrumentReservationSetting().getMaxTime()) : null;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getInstrument() != null) {
            addEntityInfoItem(summary, "instrument", getInstrument().getName());
        }
        if (StringHelper.isNotEmpty(getStartDateAsText())) {
            addEntityInfoItem(summary, "startDate", getStartDateAsText());
        }
        if (StringHelper.isNotEmpty(getEndDateAsText())) {
            addEntityInfoItem(summary, "endDate", getEndDateAsText());
        }
        if (StringHelper.isNotEmpty(getDurationAccountedWithChargeTimeUnit())) {
            addEntityInfoItem(summary, "duration", getDurationAccountedWithChargeTimeUnit());
        }
        if (getInstrumentReservationType() != null) {
            addEntityInfoItem(summary, "type", getInstrumentReservationType().getName());
        }
        if (getBooker() != null) {
            addEntityInfoItem(summary, "booker", getBooker().getFullName());
        }
        if (getUser() != null) {
            addEntityInfoItem(summary, "user", getUser().getLastName());
        }
        if (getServiceType() != null) {
            addEntityInfoItem(summary, "serviceType", getServiceType().getName());
        }
        if (getOperator() != null) {
            addEntityInfoItem(summary, "operator", getOperator().getFullName());
        }
        addEntityInfoItem(summary, "chargeable", isChargeable());
        if (getApproved() != null) {
            addEntityInfoItem(summary, "approved", getApproved());
        }
        if (getApprovalDate() != null) {
            addEntityInfoItem(summary, "approvalDate", getApprovalDate());
        }
        if (getApprovedBy() != null) {
            addEntityInfoItem(summary, "approvedBy", getApprovedBy().getFullName());
        }
        if (StringHelper.isNotEmpty(getApprovalNote())) {
            addEntityInfoItem(summary, "approvalNote", getApprovalNote());
        }
        return summary.toString();
    }

    @Override
    public String getEventCategory() {
        return getInstrumentReservationType() != null ? getInstrumentReservationType().getName() : null;
    }

    @Override
    public String getEventInfo(boolean full) {
        return getEventInfo(full, true);
    }

    public String getEventInfo(boolean full, boolean includeInstrument) {
        StringBuilder info = new StringBuilder();
        info.append(getDateIntervalAsText()).append(", ").append(getDurationAccountedWithChargeTimeUnit());
        if (isApprovalPending()) {
            info.append(", ").append(Messages.get("approvalPending"));
        }
        if (includeInstrument) {
            info.append(", ").append(getInstrument().getLabel());
        }
        if (getInstrumentReservationType() != null) {
            info.append(", ").append(getInstrumentReservationType().getName());
        }
        if (getBooker() != null) {
            info.append(", ").append(Messages.get("booker")).append(": ").append(getBooker().getFullName());
        }
        if (getUser() != null) {
            info.append(", ").append(Messages.get("user")).append(": ").append(getUser().getFullName());
        }
        if (getOperator() != null) {
            info.append(", ").append(Messages.get("operator")).append(": ").append(getOperator().getFullName());
        }
        if (!full && getContainers() != null && getContainers().stream().findFirst().isPresent()) {
            Container container = getContainers().stream().findFirst().get();
            info.append(", ").append(container.getEntityName()).append(": ").append(container.getId());
            if (getContainers().size() > 1) {
                info.append(" +");
            }
        }
        if (full) {
            if (getContainers() != null) {
                getContainers().forEach(container -> info.append(", ").append(container.getEntityName()).append(": ").append(container.getId()));
            }
            if (getRepeater() != null) {
                info.append(", ").append(Messages.get("repeatingEvent"));
            }
            if (StringHelper.isNotEmpty(getDescription())) {
                info.append(": ").append(getDescription());
            }
        }
        if (getRun() != null) {
            info.append(", ").append(Messages.get("run")).append(": ").append(getRun().getIdString());
        }
        return info.toString();
    }

    @Override
    public String getEventLocation() {
        return getInstrument().getLocation();
    }

    @Override
    public String getEventTitle() {
        return getEventInfo(false, false);
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.INSTRUMENT.getField());
        fields.add(IndexMapContentEnum.BOOKER.getField());
        fields.add(IndexMapContentEnum.PROJECTID.getField());
        fields.add(IndexMapContentEnum.ORDERID.getField());
        fields.add(IndexMapContentEnum.USER.getField());
        fields.add(IndexMapContentEnum.STARTDATE.getField());
        fields.add(IndexMapContentEnum.ENDDATE.getField());
        fields.add(IndexMapContentEnum.DURATION.getField());
        fields.add(IndexMapContentEnum.REPEATERID.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        fields.add(IndexMapContentEnum.CHARGEABLE.getField());
        fields.add(IndexMapContentEnum.CHARGED.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getName());
        if (getInstrument() != null) {
            content.add(IndexMapContentEnum.INSTRUMENT, getInstrument().getName());
        }
        if (getBooker() != null) {
            content.add(IndexMapContentEnum.BOOKER, getBooker().getFullName());
            content.add(IndexMapContentEnum.BOOKER, getBooker().getLogin());
        }
        if (getOperator() != null) {
            content.add(IndexMapContentEnum.OPERATOR, getOperator().getFullName());
            content.add(IndexMapContentEnum.OPERATOR, getOperator().getLogin());
        }
        if (getContainers() != null && !getContainers().isEmpty()) {
            getContainers().forEach(c -> {
                if (c.isContainerProject()) {
                    content.add(IndexMapContentEnum.PROJECTID, c.getId());
                } else {
                    content.add(IndexMapContentEnum.ORDERID, c.getId());
                    if (c.getProject() != null) {
                        content.add(IndexMapContentEnum.PROJECTID, c.getProject().getId());
                    }
                }
            });
        }
        content.add(IndexMapContentEnum.STARTDATE, getStartDate());
        content.add(IndexMapContentEnum.ENDDATE, getEndDate());
        content.add(IndexMapContentEnum.DURATION, getDurationAccountedWithChargeTimeUnit());
        if (getRepeater() != null) {
            content.add(IndexMapContentEnum.REPEATERID, getRepeaterId().toString());
        }
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());
        content.add(IndexMapContentEnum.CHARGEABLE, String.valueOf(isChargeable()));
        content.add(IndexMapContentEnum.CHARGED, String.valueOf(isCharged()));

        addUserToIndexMapContent(getUser(), content);
        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.INSTRUMENTRESERVATION;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public InstrumentReservationSetting getInstrumentReservationSetting() {
        if (instrumentReservationSetting == null && getInstrument() != null && getStartDate() != null) {
            instrumentReservationSetting = CDI.current().select(InstrumentReservationSettingService.class).get().getSettingByInstrumentAndDate(getInstrument(), getStartDate().toLocalDate());
            if (instrumentReservationSetting != null) {
                setChargeable(instrumentReservationSetting.isChargeable());
                if (instrumentReservationSetting.isApprovalSkipInternal() && getCurrentUser().isEmployee()) {
                    approve();
                }
            }
        }
        return instrumentReservationSetting;
    }

    public InstrumentReservationType getInstrumentReservationType() {
        return instrumentReservationType;
    }

    public String getMetadataExport(String dateDownloadString) {
        return dateDownloadString;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.INSTRUMENT_RESERVATION_NOTE;
    }

    public Set<InstrumentReservationNote> getNotes() {
        return notes;
    }

    public Set<Container> getOldContainers() {
        return oldContainers;
    }

    public User getOperator() {
        return operator;
    }

    public List<User> getOperatorsIncludingUser(User user) {
        if (getInstrumentReservationSetting().isOperatorRequired() && !getInstrument().getOperators().isEmpty()) {
            Set<User> operators = new HashSet<>(getInstrument().getOperators());
            if (user != null) {
                operators.add(user);
            }
            return CollectionHelper.asList(operators);
        }
        return CDI.current().select(UserService.class).get().getEmployeesIncludingUser(user);
    }

    public User getRemindableUser() {
        return getInstrument().isUserBookable() && getInstrument().getBookers().contains(getBooker()) ? getBooker() : null;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    @Override
    public InstrumentReservation getRepeater() {
        return repeater;
    }

    @Override
    public List<InstrumentReservation> getRepeaterEvents() {
        return repeaterEvents;
    }

    public LocalDate getRepeaterHelperValidUntilMaxDate() {
        return getInstrumentReservationSetting().getValidUntil();
    }

    public int getRepeaterHelperWeeksMax() {
        if (getInstrumentReservationSetting().getValidUntil() != null) {
            return (int) ChronoUnit.WEEKS.between(getEndDateSlot(), getInstrumentReservationSetting().getValidUntil());
        }
        return 52;
    }

    public int getRepeaterHelperWeeksMin() {
        return Long.valueOf(getDurationAsWeeks()).intValue() + 1;
    }

    public Run getRun() {
        return run;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public LocalDateTime getShiftedEndDate() {
        return shiftedEndDate;
    }

    public String getShiftedEndDateAsText() {
        if (getShiftedEndDate() != null) {
            LocalDate shiftedEndDateSlot = getShiftedEndDate().toLocalDate().minusDays(getShiftedEndDate().toLocalTime().equals(LocalTime.MIDNIGHT) ? 1 : 0);
            return Constants.DATE_FORMATTER.format(shiftedEndDateSlot) + " " + StringHelper.getTimeFormat(Duration.between(shiftedEndDateSlot.atStartOfDay(), getShiftedEndDate()));
        }
        return Constants.EMPTY_STRING;
    }

    public LocalDateTime getShiftedStartDate() {
        return shiftedStartDate;
    }

    public String getShiftedStartDateAsText() {
        return getShiftedStartDate() != null ? Constants.DATETIME_FORMATTER_MM.format(getShiftedStartDate()) : Constants.EMPTY_STRING;
    }

    @Override
    public LocalDateTime getStartDateAllDayTime() {
        return getStartDateSlot() != null && getInstrumentReservationSetting() != null ? getStartDateSlot().atStartOfDay().plus(getInstrumentReservationSetting().getMinTime()) : null;
    }

    @Override
    public String getStyleClass() {
        if (getInstrumentReservationType() != null && getInstrumentReservationType().getColor() == null) {
            if (isApprovalPending()) {
                return "approvalPending";
            } else if (getApproved() != null && !getApproved()) {
                return "nonApproved";
            } else if (isCharged()) {
                return "charged";
            } else if (!isChargeable()) {
                return "nonChargeable";
            }
            return "notCharged";
        }
        return "";
    }

    @Override
    public Instant getTriggerTime() {
        return getStartDate().minusDays(getInstrumentReservationSetting().getReminderDays()).atZone(ZoneId.of("Europe/Paris")).toInstant();
    }

    public BigDecimal getUnitPrice() {
        if (getInstrument() != null && getInstrument().getService() != null) {
            OrganizationType organizationTypeForBilling = getUser() != null ? getUser().getOrganizationTypeForBilling() : getBooker().getOrganizationTypeForBilling();
            ServiceOrganizationTypePrice serviceOrganizationTypePrice = getInstrument().getService().getServiceOrganizationTypePrices(organizationTypeForBilling);
            BigDecimal unitPrice = serviceOrganizationTypePrice.getBasicPrice();
            for (Service childService : getInstrument().getService().getChildren()) {
                ServiceOrganizationTypePrice childServiceOrganizationTypePrice = childService.getServiceOrganizationTypePrices(organizationTypeForBilling);
                unitPrice = unitPrice.add(childServiceOrganizationTypePrice.getBasicPrice());
            }
            return unitPrice;
        }
        return null;
    }

    public boolean isAllDayEventCheckboxDisabled() {
        return getStartDateSlot().equals(getEndDateSlot()) && CDI.current().select(InstrumentReservationService.class).get()
            .checkByIntervalAndInstrumentExcludingEvents(new LocalDateTimeInterval(getStartDateSlot().atStartOfDay(), getStartDateSlot().atStartOfDay().plusDays(1)), this);
    }

    public boolean isAllDayRendered() {
        return getInstrumentReservationSetting().getSlotShiftTime().toMinutes() == 0;
    }

    public boolean isApprovableByCurrentUser() {
        return getOperator() != null && getOperator().isIdentityUser() || getInstrument().isAdminOrSupervisor() || isIdentityContainerCoach();
    }

    public boolean isApprovalPending() {
        return getApproved() == null && isApprovalRequired();
    }

    public boolean isApprovalRequired() {
        return getInstrumentReservationSetting() != null && getInstrumentReservationSetting().isApprovalRequired();
    }

    public boolean isApproveButtonRendered() {
        return (getApproved() == null || !getApproved()) && isApprovableByCurrentUser() && isApprovalRequired();
    }

    public boolean isChargeable() {
        return chargeable;
    }

    public boolean isChargeableByCurrentUser() {
        return isChargeable() && hasCurrentUserRoleEnum(RoleEnum.INSTRUMENTMANAGER);
    }

    public boolean isCharged() {
        return !getCharges().isEmpty();
    }

    public boolean isComputedDurationCorrect() {
        return Long.valueOf(getDurationComputed()).equals(getDuration());
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || getInstrument() == null || getInstrument().isUserBookable() || getInstrument().isCurrentUserBooker();
    }

    public boolean isCreatable(LocalDateTime localDateTime) {
        return isCreatable() && getInstrumentReservationSetting() != null && getInstrumentReservationSetting().isValidBookingAhead(localDateTime);
    }

    public boolean isCurrentUserBooker() {
        return getBooker() != null && getBooker().isIdentityUser();
    }

    public boolean isCurrentUserOperator() {
        return getOperator() != null && getOperator().isIdentityUser();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isDeletableAll() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isDeletableAll(getRepeater().getRepeaterEvents());
    }

    public boolean isDeletableAll(Collection<InstrumentReservation> instrumentReservations) {
        boolean deletable = true;
        for (InstrumentReservation instrumentReservation : instrumentReservations) {
            if (instrumentReservation.isCharged()) {
                deletable = false;
                break;
            }
        }
        return deletable;
    }

    public boolean isDeletableAllFollowing() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isDeletableAll((List<InstrumentReservation>) getRepeaterEventsFollowing());
    }

    public boolean isIdentityContainerCoach() {
        return getContainersCoaches() != null && getContainersCoaches().contains(getCurrentUser());
    }

    @Override
    public boolean isReadable() {
        return isCreatable() || getUser() != null && getUser().isIdentityUser();
    }

    public boolean isRejectButtonRendered() {
        return (getApproved() == null || getApproved()) && isApprovableByCurrentUser() && isApprovalRequired();
    }

    public boolean isRepeatable() {
        return getInstrumentReservationSetting().getValidUntil() == null || getEndDateSlot() != null && !getEndDateSlot().plusWeeks(Math.max(getRepeaterHelper().getWeeks(), getDurationAsWeeks()))
            .isAfter(getInstrumentReservationSetting().getValidUntil());
    }

    public boolean isSendMailNotification() {
        return sendMailNotification;
    }

    @Override
    public boolean isUpdatable() {
        return !isCharged() && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isCurrentUserBooker() || isCurrentUserOperator()) && (getApproved() == null || getCurrentUser().isEmployee());
    }

    public void reject() {
        setApproved(false);
    }

    public void resetContainer() {
        setContainer(null);
        setUser(null);
        setChargeable(false);
    }

    @Override
    public void resetEndDate() {
        Optional<InstrumentReservationSetting> setting = getInstrument().getReservationSetting(getStartDate().toLocalDate());
        if (getStartDate() != null && getEndDate() != null && setting.isPresent()) {
            setEndDate(getStartDate().plusMinutes(setting.get().getMinDuration().toMinutes()));
        }
    }

    @Override
    public void resetFields() {
        if (!getInstrumentReservationType().isContainerAssociated()) {
            resetContainer();
        }
        if (!isApprovalRequired()) {
            setApproved(null);
            setApprovalDate(null);
            setApprovedBy(null);
            setApprovalNote(null);
        } else if (getApproved() != null && (getApprovedBy() == null || getApprovalDate() == null)) {
            approvedChanged();
        }
    }

    @Override
    public void resetStartDate() {
        Optional<InstrumentReservationSetting> setting = getInstrument().getReservationSetting(getStartDate().toLocalDate());
        if (getStartDate() != null && getEndDate() != null && setting.isPresent()) {
            setStartDate(getEndDate().minusMinutes(setting.get().getMinDuration().toMinutes()));
        }
    }

    public void sendMailNotificationChanged() {
        setSendMailNotification(!isSendMailNotification());
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public void setApprovalNote(String approvalNote) {
        this.approvalNote = StringHelper.format(approvalNote);
    }

    public void setApproved(Boolean approved) {
        if (approved != null && this.approved == null || approved == null && this.approved != null || approved != null && !approved.equals(this.approved)) {
            setApprovalDate(LocalDate.now());
            setApprovedBy(getCurrentUser());
        }
        this.approved = approved;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setBooker(User booker) {
        this.booker = booker;
    }

    public void setChargeable(boolean chargeable) {
        this.chargeable = chargeable;
    }

    public void setCharges(Set<Charge> charges) {
        this.charges = charges;
    }

    public void setContainer(Set<Container> containers) {
        this.containers = containers;
    }

    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

    public void setContainersAsList(List<Container> containers) {
        this.containers = (Set<Container>) CollectionHelper.asSet(containers);
    }

    public void setEndDateDefault() {
        if (getInstrumentReservationSetting() != null) {
            setEndDate(getStartDate().plus(getInstrumentReservationSetting().getSlotLabelInterval()));
        }
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentReservationSetting(InstrumentReservationSetting instrumentReservationSetting) {
        this.instrumentReservationSetting = instrumentReservationSetting;
    }

    public void setInstrumentReservationType(InstrumentReservationType instrumentReservationType) {
        this.instrumentReservationType = instrumentReservationType;
    }

    public void setNotes(Set<InstrumentReservationNote> notes) {
        this.notes = notes;
    }

    public void setOldContainers(Set<Container> oldContainers) {
        this.oldContainers = oldContainers;
    }

    public void setOldValues() {
        oldContainers = new HashSet<>(containers);
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    @Override
    public void setRepeater(AbstractEvent repeater) {
        this.repeater = (InstrumentReservation) repeater;
    }

    public void setRepeaterEvents(RepeaterHelper repeater) throws CloneNotSupportedException {
        // Important note: Do not remove the following lines since they are needed in case during interaction the repeater is switch off after a validation error.
        if (!getRepeaterEvents().isEmpty()) {
            setRepeater(null);
            getRepeaterEvents().clear();
        }

        if (repeater != null && repeater.isRepeat()) {
            setRepeater(this);
            getRepeaterEvents().add(this);

            LocalDateTime tmpStart = getStartDate().plusWeeks(repeater.getWeeks());
            LocalDateTime tmpEnd = getEndDate().plusWeeks(repeater.getWeeks());

            while (tmpEnd.isBefore(repeater.getEnd().plusDays(1))) {
                // Create and store new event
                final InstrumentReservation newEvent = cloneRepeating();
                newEvent.setStartDate(tmpStart);
                newEvent.setEndDate(tmpEnd);
                // Split event if it overlaps two or more years
                if (newEvent.isYearCrossing()) {
                    getRepeaterEvents().addAll(newEvent.splitEvent());
                } else {
                    getRepeaterEvents().add(newEvent);
                }

                // Increase start and end time
                tmpStart = tmpStart.plusWeeks(repeater.getWeeks());
                tmpEnd = tmpEnd.plusWeeks(repeater.getWeeks());
            }
        }
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public void setSendMailNotification(boolean sendMailNotification) {
        this.sendMailNotification = sendMailNotification;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void setShiftedEndDate(LocalDateTime shiftedEndDate) {
        this.shiftedEndDate = shiftedEndDate;
    }

    public void setShiftedStartDate(LocalDateTime shiftedStartDate) {
        this.shiftedStartDate = shiftedStartDate;
    }

    public List<InstrumentReservation> splitEvent() {
        List<InstrumentReservation> events = new ArrayList<>();
        if (isYearCrossing()) {
            InstrumentReservation newEvent = new InstrumentReservation();
            newEvent.setBooker(getBooker());
            newEvent.setOperator(getOperator());
            newEvent.setInstrument(getInstrument());
            newEvent.setContainers(containers);
            newEvent.setStartDate(getStartDate().plusYears(1).withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            newEvent.setEndDate(getEndDate());
            newEvent.setChargeable(isChargeable());
            newEvent.setUser(getUser());
            newEvent.setDescription(getDescription());
            newEvent.setRepeater(getRepeater());
            newEvent.setChargeable(isChargeable());

            setEndDate(getStartDate().withMonth(12).withDayOfMonth(31).withHour(0).withMinute(0).withSecond(0).withNano(0));

            events.add(this);

            events.addAll(newEvent.splitEvent());
        } else {
            events.add(this);
        }

        return events;
    }
}
