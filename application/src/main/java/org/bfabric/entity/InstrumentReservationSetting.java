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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.ehcache.util.FindBugsSuppressWarnings;
import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.InstrumentReservationSettingService;
import org.bfabric.util.DateUtils;
import org.bfabric.util.DurationHelper;
import org.bfabric.util.LocalDateInterval;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "InstrumentReservationSetting.findByInstrument", query = "SELECT a FROM InstrumentReservationSetting a WHERE a.instrument = :instrument")
@NamedQuery(name = "InstrumentReservationSetting.findByInstrumentAndDate", query = "select a from InstrumentReservationSetting a where a.instrument = :instrument and (a.validFrom IS NULL OR a.validFrom <= :date) and (a.validUntil IS NULL OR a.validUntil > :date)")
@NamedQuery(name = "InstrumentReservationSetting.updateActiveSettingValidUntil", query = "update InstrumentReservationSetting a set a.validUntil = :validUntil where a.instrument = :instrument and a.validUntil IS NULL")
@NamedQuery(name = "InstrumentReservationSetting.updateSettingValidFrom", query = "update InstrumentReservationSetting a set a.validFrom = :newDate where a.id = :instrumentReservationSettingId")
@NamedQuery(name = "InstrumentReservationSetting.updateSettingValidUntil", query = "update InstrumentReservationSetting a set a.validUntil = :newDate where a.id = :instrumentReservationSettingId")
public class InstrumentReservationSetting extends AbstractInstrumentDependentEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "instrumentReservationSetting")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OrderBy("endDate desc")
    private final Set<InstrumentReservation> reservationsOrderedByNewest = new HashSet<>();

    @OneToMany(mappedBy = "instrumentReservationSetting")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OrderBy("startDate")
    private final Set<InstrumentReservation> reservationsOrderedByOldest = new HashSet<>();

    @Transient
    InstrumentReservation latestInstrumentReservation;

    @Transient
    InstrumentReservation earliestInstrumentReservation;

    @Transient
    InstrumentReservationSetting nextSetting;

    @Transient
    InstrumentReservationSetting previousSetting;

    @Transient
    Long maxInstrumentReservationDuration;

    @Transient
    Long minInstrumentReservationDuration;

    @Transient
    Boolean chargeTimeUnitEditDisabled;

    @Transient
    LocalDateInterval updatableValidUntilInterval = null;

    @Transient
    LocalDateInterval updatableValidFromInterval = null;

    @NotNull
    @XmlElement
    private boolean approvalRequired = false;

    @NotNull
    @XmlElement
    private boolean approvalSkipInternal = false;

    @XmlElement
    private Duration bookingAheadMaxDuration;

    @Transient
    private DurationHelper bookingAheadMaxDurationHelper;

    @XmlElement
    private Duration bookingAheadMinDuration;

    @Transient
    private DurationHelper bookingAheadMinDurationHelper;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private ChronoUnit chargeTimeUnit = ChronoUnit.DAYS;

    @XmlElement
    private boolean chargeable;

    @Column(updatable = false, insertable = false)
    private Duration dayDuration;

    @Transient
    private LinkedList<Duration> endTimes;

    @OneToMany(mappedBy = "instrumentReservationSetting")
    @OrderBy("id DESC")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<InstrumentReservation> instrumentReservations = new HashSet<>();

    @XmlElement
    private Duration maxDuration;

    @Transient
    private DurationHelper maxDurationHelper;

    @NotNull
    @XmlElement
    private Duration minDuration;

    @Transient
    private DurationHelper minDurationHelper;

    @XmlElement
    private boolean notifyCoach;

    @XmlElement
    private boolean notifyInstrumentSupervisor;

    @NotNull
    @XmlElement
    private boolean operatorRequired = false;

    @NotNull
    @XmlElement
    private int reminderDays = 1;

    @XmlElement
    private Duration slotDuration;

    @NotNull
    @XmlElement
    private Duration slotLabelInterval;

    @Transient
    private DurationHelper slotLabelIntervalHelper;

    @XmlElement
    private Duration slotMaxTime;

    @Transient
    private DurationHelper slotMaxTimeHelper;

    @XmlElement
    private Duration slotMinTime;

    @Transient
    private DurationHelper slotMinTimeHelper;

    @NotNull
    @XmlElement
    private Duration slotShiftTime = Duration.ZERO;

    @Transient
    private DurationHelper slotShiftTimeHelper;

    @Transient
    private LinkedList<Duration> startTimes;

    @XmlElement
    private LocalDate validFrom;

    @XmlElement
    private LocalDate validUntil;

    @NotNull
    @XmlElement
    private boolean weekends = true;

    public InstrumentReservationSetting() {
    }

    public InstrumentReservationSetting(Instrument instrument) {
        if (instrument != null) {
            setInstrument(instrument);
            setDefaults();
            instrument.getReservationSettings().add(this);
        }
    }

    @Override
    public InstrumentReservationSetting clone() throws CloneNotSupportedException {
        InstrumentReservationSetting clone = (InstrumentReservationSetting) super.clone();
        clone.startTimes = null;
        clone.endTimes = null;
        clone.instrumentReservations = new HashSet<>();
        InstrumentReservationSetting lastReservationSetting = getInstrument().getLastReservationSetting();
        InstrumentReservation latestInstrumentReservation = lastReservationSetting.getLatestInstrumentReservation();
        if (latestInstrumentReservation != null) {
            // Note: Minus one day if endDateTime=24:00 since internally it is represented as 00:00 of the next day!
            clone.setValidFrom(latestInstrumentReservation.getEndDate().toLocalDate()
                .minusDays(latestInstrumentReservation.getEndDate().equals(latestInstrumentReservation.getEndDate().toLocalDate().atStartOfDay()) ? 1 : 0).plusWeeks(1).with(DayOfWeek.MONDAY));
        } else if (lastReservationSetting.getValidFrom() != null && !lastReservationSetting.getValidFrom().isBefore(LocalDate.now().with(DayOfWeek.MONDAY))) {
            clone.setValidFrom(lastReservationSetting.getValidFrom().plusWeeks(1));
        } else {
            clone.setValidFrom(LocalDate.now().with(DayOfWeek.MONDAY));
        }
        return clone;
    }

    public void computeStartEndTimes() {
        startTimes = new LinkedList<>();
        endTimes = new LinkedList<>();
        LocalDateTime fromDate = Optional.ofNullable(getValidFrom()).map(LocalDate::atStartOfDay).orElse(LocalDate.now().atStartOfDay());
        LocalDateTimeInterval minMaxInterval = new LocalDateTimeInterval(fromDate.plus(getMinTime()), fromDate.plus(getMaxTime()));
        LocalDateTime timeIterator = minMaxInterval.getStart();
        Duration duration = Optional.ofNullable(getSlotDuration()).orElse(Duration.ofMinutes(1));
        while (minMaxInterval.contains(timeIterator)) {
            Duration increment = Duration.between(fromDate, timeIterator).plus(getSlotShiftTime());
            startTimes.add(increment);
            endTimes.add(increment);
            timeIterator = timeIterator.plus(duration);
        }
        if (startTimes.size() > 1) {
            startTimes.removeLast();
            if (getSlotShiftTime().toMinutes() == 0) {
                endTimes.removeFirst();
            } else {
                endTimes.removeLast();
            }
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

    public Duration getBookingAheadMaxDuration() {
        return bookingAheadMaxDuration;
    }

    public String getBookingAheadMaxDurationFormat() {
        return StringHelper.getFormattedDurationTrim(getBookingAheadMaxDuration());
    }

    public DurationHelper getBookingAheadMaxDurationHelper() {
        if (bookingAheadMaxDurationHelper == null) {
            bookingAheadMaxDurationHelper = new DurationHelper(bookingAheadMaxDuration, false);
        }
        return bookingAheadMaxDurationHelper;
    }

    public Duration getBookingAheadMinDuration() {
        return bookingAheadMinDuration;
    }

    public String getBookingAheadMinDurationFormat() {
        return StringHelper.getFormattedDurationTrim(getBookingAheadMinDuration());
    }

    public DurationHelper getBookingAheadMinDurationHelper() {
        if (bookingAheadMinDurationHelper == null) {
            bookingAheadMinDurationHelper = new DurationHelper(bookingAheadMinDuration, false);
        }
        return bookingAheadMinDurationHelper;
    }

    public ChronoUnit getChargeTimeUnit() {
        return chargeTimeUnit;
    }

    public LocalTime getClosestPastSlot(LocalTime from) {
        LocalTime slotLabelIntervalAsTime = LocalTime.parse(StringHelper.getTimeFormat(getSlotLabelInterval()));
        int slotLabelIntervalAsTimeInMinutes = 60 * slotLabelIntervalAsTime.getHour() + slotLabelIntervalAsTime.getMinute();
        int pastSlot = IntStream.range(slotLabelIntervalAsTimeInMinutes, 1)
            .filter(nbr -> nbr % slotLabelIntervalAsTimeInMinutes == 0)
            .findFirst()
            .orElse(0);
        return toLocalTime(pastSlot);
    }

    public LocalTime getClosestUpcomingSlot(LocalTime from) {
        LocalTime slotLabelIntervalAsTime = LocalTime.parse(StringHelper.getTimeFormat(getSlotLabelInterval()));
        int slotLabelIntervalAsTimeInMinutes = 60 * slotLabelIntervalAsTime.getHour() + slotLabelIntervalAsTime.getMinute();
        OptionalInt nextSlotOptional = IntStream.range(slotLabelIntervalAsTimeInMinutes, 1440)
            .filter(nbr -> nbr % slotLabelIntervalAsTimeInMinutes == 0)
            .findFirst();
        int nextSlot = nextSlotOptional.isPresent() ? nextSlotOptional.getAsInt() : 0;
        return toLocalTime(nextSlot);
    }

    public Duration getDayDuration() {
        if (dayDuration == null) {
            dayDuration = getMaxTime().minus(getMinTime());
        }
        return dayDuration;
    }

    public String getDayDurationFormat() {
        return StringHelper.getTimeFormat(getDayDuration());
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    public List<Integer> getDisabledDays() {
        List<Integer> invalidDays = new ArrayList<>();
        invalidDays.add(0);
        invalidDays.add(2);
        invalidDays.add(3);
        invalidDays.add(4);
        invalidDays.add(5);
        invalidDays.add(6);
        return invalidDays;
    }

    public List<Integer> getDisabledWeekends() {
        List<Integer> invalidDays = new ArrayList<>();
        if (!weekends) {
            invalidDays.add(0);
            invalidDays.add(6);
        }
        return invalidDays;
    }

    public InstrumentReservation getEarliestInstrumentReservation() {
        if (earliestInstrumentReservation == null) {
            earliestInstrumentReservation = CDI.current().select(InstrumentReservationService.class).get().getEarliestInstrumentReservationBySetting(this);
        }
        return earliestInstrumentReservation;
    }

    public LinkedList<Duration> getEndTimes() {
        return endTimes;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getValidFrom() != null) {
            addEntityInfoItem(summary, "validFrom", getValidFrom());
        }
        if (getValidUntil() != null) {
            addEntityInfoItem(summary, "validUntil", getValidUntil());
        }
        addEntityInfoItem(summary, "weekends", isWeekends());
        addEntityInfoItem(summary, "notifyCoach", isNotifyCoach());
        addEntityInfoItem(summary, "notifyInstrumentSupervisor", isNotifyInstrumentSupervisor());
        if (getSlotDuration() != null) {
            addEntityInfoItem(summary, "slotDuration", getSlotDurationTimeFormat());
        }
        if (StringHelper.isNotEmpty(getSlotShiftTimeFormat())) {
            addEntityInfoItem(summary, "slotShiftTime", getSlotShiftTimeFormat());
        }
        if (StringHelper.isNotEmpty(getSlotLabelIntervalTimeFormat())) {
            addEntityInfoItem(summary, "slotLabelInterval", getSlotLabelIntervalTimeFormat());
        }
        if (StringHelper.isNotEmpty(getMinDurationTimeFormat())) {
            addEntityInfoItem(summary, "minDuration", getMinDurationTimeFormat());
        }
        if (getMaxDuration() != null) {
            addEntityInfoItem(summary, "maxDuration", getMaxDurationTimeFormat());
        }
        if (getSlotMinTime() != null) {
            addEntityInfoItem(summary, "slotMinTime", getSlotMinTimeFormat());
        }
        if (getSlotMaxTime() != null) {
            addEntityInfoItem(summary, "slotMaxTime", getSlotMaxTimeFormat());
        }
        if (getBookingAheadMinDuration() != null) {
            addEntityInfoItem(summary, "bookingAheadMinDuration", getBookingAheadMinDuration());
        }
        if (getBookingAheadMaxDuration() != null) {
            addEntityInfoItem(summary, "bookingAheadMaxDuration", getBookingAheadMaxDuration());
        }
        addEntityInfoItem(summary, "operatorRequired", isOperatorRequired());
        addEntityInfoItem(summary, "approvalRequired", isApprovalRequired());
        if (getChargeTimeUnit() != null) {
            addEntityInfoItem(summary, "chargeTimeUnit", getChargeTimeUnit());
        }
        addEntityInfoItem(summary, "reminderDays", getReminderDays());
        return summary.toString();
    }

    public boolean getHasModifiableValidFrom() {
        return getValidFrom() != null && !getUpdatableValidFromInterval().isCollapsed();
    }

    public boolean getHasModifiableValidUntil() {
        return validUntil != null && !getUpdatableValidFromInterval().isCollapsed();
    }

    public Set<InstrumentReservation> getInstrumentReservations() {
        return instrumentReservations;
    }

    public InstrumentReservation getLatestInstrumentReservation() {
        if (latestInstrumentReservation == null) {
            latestInstrumentReservation = CDI.current().select(InstrumentReservationService.class).get().getLatestInstrumentReservationBySetting(this);
        }
        return latestInstrumentReservation;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public DurationHelper getMaxDurationHelper() {
        if (maxDurationHelper == null) {
            maxDurationHelper = new DurationHelper(maxDuration, true);
        }
        return maxDurationHelper;
    }

    public int getMaxDurationMinHours() {
        return getMaxInstrumentReservationDuration() != null ? DurationHelper.convertMinutesToChronoUnit(getMaxInstrumentReservationDuration(), ChronoUnit.HOURS, 1440L, isWeekends()).intValue() : 0;
    }

    public String getMaxDurationTimeFormat() {
        return StringHelper.getTimeFormatHM(getMaxDuration());
    }

    public Long getMaxInstrumentReservationDuration() {
        if (maxInstrumentReservationDuration == null) {
            maxInstrumentReservationDuration = CDI.current().select(InstrumentReservationService.class).get().getMaxDurationBySetting(this);
        }
        return maxInstrumentReservationDuration;
    }

    public Duration getMaxTime() {
        return Optional.ofNullable(getSlotMaxTime()).orElse(Duration.ofHours(24));
    }

    public String getMaxTimeFormat() {
        return StringHelper.getTimeFormat(getMaxTime().plus(getSlotShiftTime()));
    }

    public Duration getMinDuration() {
        return minDuration;
    }

    public DurationHelper getMinDurationHelper() {
        if (minDurationHelper == null) {
            minDurationHelper = new DurationHelper(minDuration, true);
        }
        return minDurationHelper;
    }

    public int getMinDurationMaxHours() {
        return getMinInstrumentReservationDuration() != null && getMinInstrumentReservationDuration() != 0L ? DurationHelper
            .convertMinutesToChronoUnit(getMinInstrumentReservationDuration(), ChronoUnit.HOURS, 1440L, isWeekends()).intValue() : getConfiguration().getInstrumentReservationSettingMaxHours();
    }

    public String getMinDurationTimeFormat() {
        return StringHelper.getTimeFormatHM(getMinDuration());
    }

    public Long getMinInstrumentReservationDuration() {
        if (minInstrumentReservationDuration == null) {
            minInstrumentReservationDuration = CDI.current().select(InstrumentReservationService.class).get().getMinDurationBySetting(this);
        }
        return minInstrumentReservationDuration;
    }

    public Duration getMinTime() {
        return Optional.ofNullable(getSlotMinTime()).orElse(Duration.ofHours(0));
    }

    public String getMinTimeFormat() {
        return StringHelper.getTimeFormat(getMinTime().plus(getSlotShiftTime()));
    }

    public String getName() {
        return getValidFromAsText() + " - " + getValidUntilAsString();
    }

    public InstrumentReservationSetting getNextSetting() {
        return nextSetting;
    }

    public InstrumentReservationSetting getPreviousSetting() {
        return previousSetting;
    }

    public int getReminderDays() {
        return reminderDays;
    }

    public Set<InstrumentReservation> getReservationsOrderedByNewest() {
        return reservationsOrderedByNewest;
    }

    public Set<InstrumentReservation> getReservationsOrderedByOldest() {
        return reservationsOrderedByOldest;
    }

    public String getScheduleSlotDuration() {
        return getSlotDuration() != null ? StringHelper.getTimeFormat(getSlotDuration()) : StringHelper.getTimeFormat(getSlotLabelInterval());
    }

    public String getScheduleSlotLabelInterval() {
        return StringHelper.getTimeFormat(getSlotLabelInterval());
    }

    public Duration getSlotDuration() {
        return slotDuration;
    }

    public String getSlotDurationTimeFormat() {
        return StringHelper.getTimeFormatHM(getSlotDuration());
    }

    public List<Duration> getSlotDurations() {
        return DateUtils.getSlotDurations(getSlotMinTime(), getSlotMaxTime());
    }

    public Duration getSlotLabelInterval() {
        return slotLabelInterval;
    }

    public DurationHelper getSlotLabelIntervalHelper() {
        if (slotLabelIntervalHelper == null) {
            slotLabelIntervalHelper = new DurationHelper(slotLabelInterval, true);
        }
        return slotLabelIntervalHelper;
    }

    public String getSlotLabelIntervalTimeFormat() {
        return StringHelper.getTimeFormatHM(getSlotLabelInterval());
    }

    public Duration getSlotMaxTime() {
        return slotMaxTime;
    }

    public String getSlotMaxTimeFormat() {
        return StringHelper.getTimeFormat(getSlotMaxTime());
    }

    public DurationHelper getSlotMaxTimeHelper() {
        if (slotMaxTimeHelper == null) {
            slotMaxTimeHelper = new DurationHelper(slotMaxTime, true);
        }
        return slotMaxTimeHelper;
    }

    public Duration getSlotMinTime() {
        return slotMinTime;
    }

    public String getSlotMinTimeFormat() {
        return StringHelper.getTimeFormat(getSlotMinTime());
    }

    public DurationHelper getSlotMinTimeHelper() {
        if (slotMinTimeHelper == null) {
            slotMinTimeHelper = new DurationHelper(slotMinTime, true);
        }
        return slotMinTimeHelper;
    }

    public Duration getSlotShiftTime() {
        return slotShiftTime;
    }

    public String getSlotShiftTimeFormat() {
        return StringHelper.getTimeFormatHM(getSlotShiftTime());
    }

    public DurationHelper getSlotShiftTimeHelper() {
        if (slotShiftTimeHelper == null) {
            slotShiftTimeHelper = new DurationHelper(getSlotShiftTime(), true);
        }
        return slotShiftTimeHelper;
    }

    public LinkedList<Duration> getStartTimes() {
        if (startTimes == null) {
            computeStartEndTimes();
        }
        return startTimes;
    }

    @FindBugsSuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public LocalDateInterval getUpdatableValidFromInterval() {
        if (updatableValidFromInterval == null) {
            updatableValidFromInterval = CDI.current().select(InstrumentReservationSettingService.class).get().getUpdatableValidFromInterval(getId());
        }
        return updatableValidFromInterval;
    }

    @FindBugsSuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public LocalDate getUpdatableValidFromMaxDateSlot() {
        return getUpdatableValidFromInterval() != null ? getUpdatableValidFromInterval().getEnd() : null;
    }

    @FindBugsSuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public LocalDate getUpdatableValidFromMinDateSlot() {
        return getUpdatableValidFromInterval() != null ? getUpdatableValidFromInterval().getStart() : null;
    }

    @FindBugsSuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public LocalDateInterval getUpdatableValidUntilInterval() {
        if (updatableValidUntilInterval == null) {
            updatableValidUntilInterval = CDI.current().select(InstrumentReservationSettingService.class).get().getUpdatableValidUntilInterval(this.getId());
        }
        return updatableValidUntilInterval;
    }

    @FindBugsSuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public LocalDate getUpdatableValidUntilMaxDateSlot() {
        return getUpdatableValidUntilInterval() != null ? getUpdatableValidUntilInterval().getEnd() : null;
    }

    @FindBugsSuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public LocalDate getUpdatableValidUntilMinDateSlot() {
        return getUpdatableValidUntilInterval() != null ? getUpdatableValidUntilInterval().getStart() : null;
    }

    public String getValidEndTimes() {
        return getEndTimes().stream().map(StringHelper::getTimeFormat).collect(Collectors.joining(","));
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public String getValidFromAsText() {
        return getValidFrom() != null ? Constants.DATE_FORMATTER.format(getValidFrom().atStartOfDay()) : "-∞";
    }

    public String getValidFromDateSlot() {
        return getValidFrom() != null ? Constants.DATE_FORMATTER.format(getValidFrom()) : Constants.DATE_FORMATTER.format(LocalDate.now());
    }

    public String getValidIntervalAsString() {
        return getValidLocalDateInterval().getIntervalAsString();
    }

    public LocalDateInterval getValidLocalDateInterval() {
        return new LocalDateInterval(getValidFrom(), getValidUntil());
    }

    public LocalDateTimeInterval getValidLocalDateTimeInterval() {
        return new LocalDateTimeInterval(getValidFrom(), getValidUntil());
    }

    public String getValidStartTimes() {
        return getStartTimes().stream().map(StringHelper::getTimeFormat).collect(Collectors.joining(","));
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public String getValidUntilAsString() {
        return getValidUntil() != null ? Constants.DATE_FORMATTER.format(getValidUntil().atStartOfDay()) : "∞+";
    }

    public Boolean hasBookingAheadDuration() {
        return getBookingAheadMaxDuration() != null || getBookingAheadMinDuration() != null;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public boolean isApprovalSkipInternal() {
        return approvalSkipInternal;
    }

    public boolean isAttributesUpdatable() {
        return isUpdatable() && getInstrumentReservations().isEmpty();
    }

    public boolean isChargeTimeUnitEditDisabled() {
        if (chargeTimeUnitEditDisabled == null) {
            chargeTimeUnitEditDisabled = !getInstrumentReservations().isEmpty() || CDI.current().select(InstrumentReservationSettingService.class).get().anyChargeableReservation(this);
        }
        return chargeTimeUnitEditDisabled;
    }

    public boolean isChargeable() {
        return chargeable;
    }

    public boolean isContained(LocalDate date) {
        return getValidLocalDateInterval().contains(date) && (getValidUntil() == null || !getValidUntil().equals(date));
    }

    @Override
    public boolean isDeletable() {
        return isReadable() && getInstrument().getReservationSettings().size() > 1 && isLastReservationSetting() && getInstrumentReservations().isEmpty();
    }

    public boolean isLastReservationSetting() {
        return getValidUntil() == null;
    }

    public boolean isNotifyCoach() {
        return notifyCoach;
    }

    public boolean isNotifyInstrumentSupervisor() {
        return notifyInstrumentSupervisor;
    }

    public boolean isOperatorRequired() {
        return operatorRequired;
    }

    public boolean isReadable() {
        return isCreatable();
    }

    public boolean isSlotLabelIntervalDisabled() {
        return getSlotDuration() != null || getMinDuration() != null && getMinDuration().toMinutes() > getMaxTime().toMinutes() - getMinTime().toMinutes() && getInstrumentReservations().isEmpty();
    }

    public boolean isSlotMaxTimeHelperDisabled() {
        return !getInstrumentReservations().isEmpty();
    }

    public boolean isSlotMinTimeHelperDisabled() {
        return !getInstrumentReservations().isEmpty();
    }

    public boolean isSlotShiftTimeEditDisabled() {
        return getSlotDuration() == null || !getInstrumentReservations().isEmpty();
    }

    @Override
    public boolean isUpdatable() {
        return isReadable();
    }

    public boolean isValidBookingAhead(LocalDateTime eventTime) {
        if (eventTime != null && hasBookingAheadDuration()) {
            Duration tillEvent = Duration.between(LocalDateTime.now(), eventTime);
            if (getBookingAheadMinDuration() != null && !getBookingAheadMinDuration().minus(tillEvent).isNegative()) {
                return false;
            }
            return getBookingAheadMaxDuration() == null || !getBookingAheadMaxDuration().minus(tillEvent).isNegative();
        }
        return true;
    }

    public boolean isWeekendEditAllowed() {
        return getInstrumentReservations().isEmpty() || CDI.current().select(InstrumentReservationSettingService.class).get().canWeekendReservationBeAltered(getId());
    }

    public boolean isWeekends() {
        return weekends;
    }

    public void maxDurationHelperChanged() {
        setMaxDuration(getMaxDurationHelper().getDuration());
    }

    public void minDurationHelperChanged() {
        setMinDuration(getMinDurationHelper().getDuration());
        resetSlotLabelIntervalHelper();
    }

    public void resetBookingAheadMaxDurationHelper() {
        setBookingAheadMaxDurationHelper(null);
        setBookingAheadMaxDuration(null);
    }

    public void resetBookingAheadMinDurationHelper() {
        setBookingAheadMinDurationHelper(null);
        setBookingAheadMinDuration(null);
    }

    public void resetMaxDurationHelper() {
        setMaxDurationHelper(null);
        setMaxDuration(null);
    }

    public void resetMinDurationHelper() {
        setMinDurationHelper(null);
        setMinDuration(getSlotDuration() != null ? getSlotDuration() : getConfiguration().getInstrumentReservationSettingSlotDurationDefaultAsDuration());
        resetSlotLabelIntervalHelper();
    }

    public void resetSlotDuration() {
        setSlotDuration(null);
        resetMinDurationHelper();
        computeStartEndTimes();
    }

    public void resetSlotLabelIntervalHelper() {
        setSlotLabelIntervalHelper(null);
        setSlotLabelInterval(getMaxTime().minus(getMinTime()).minus(getMinDuration()).toMinutes() > 0 ? getMinDuration() : getMaxTime().minus(getMinTime()));
    }

    public void resetSlotMaxTimeHelper() {
        setSlotMaxTimeHelper(null);
        setSlotMaxTime(null);
        resetSlotDuration();
    }

    public void resetSlotMinTimeHelper() {
        setSlotMinTimeHelper(null);
        setSlotMinTime(null);
        resetSlotDuration();
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public void setApprovalSkipInternal(boolean approvalSkipInternal) {
        this.approvalSkipInternal = approvalSkipInternal;
    }

    public void setBookingAheadMaxDuration(Duration bookingAheadMaxDuration) {
        this.bookingAheadMaxDuration = bookingAheadMaxDuration;
    }

    public void setBookingAheadMaxDurationHelper(DurationHelper bookingAheadMaxDurationHelper) {
        this.bookingAheadMaxDurationHelper = bookingAheadMaxDurationHelper;
    }

    public void setBookingAheadMinDuration(Duration bookingAheadMinDuration) {
        this.bookingAheadMinDuration = bookingAheadMinDuration;
    }

    public void setBookingAheadMinDurationHelper(DurationHelper bookingAheadMinDurationHelper) {
        this.bookingAheadMinDurationHelper = bookingAheadMinDurationHelper;
    }

    public void setChargeTimeUnit(ChronoUnit chargeTimeUnit) {
        this.chargeTimeUnit = chargeTimeUnit;
    }

    public void setChargeable(boolean chargeable) {
        this.chargeable = chargeable;
    }

    public void setDefaults() {
        Duration slotDuration = getConfiguration().getInstrumentReservationSettingSlotDurationDefaultAsDuration();
        setSlotDuration(slotDuration);
        setMinDuration(slotDuration);
        setSlotLabelInterval(slotDuration);
        setWeekends(getConfiguration().isInstrumentReservationWeekendsEnabled());
    }

    public void setMaxDuration(Duration maxDuration) {
        this.maxDuration = maxDuration;
    }

    public void setMaxDurationHelper(DurationHelper maxDurationHelper) {
        this.maxDurationHelper = maxDurationHelper;
    }

    public void setMinDuration(Duration minDuration) {
        this.minDuration = minDuration;
    }

    public void setMinDurationHelper(DurationHelper minDurationHelper) {
        this.minDurationHelper = minDurationHelper;
    }

    public void setNextSetting(InstrumentReservationSetting nextSetting) {
        this.nextSetting = nextSetting;
    }

    public void setNotifyCoach(boolean notifyCoach) {
        this.notifyCoach = notifyCoach;
    }

    public void setNotifyInstrumentSupervisor(boolean notifyInstrumentSupervisor) {
        this.notifyInstrumentSupervisor = notifyInstrumentSupervisor;
    }

    public void setOperatorRequired(boolean operatorRequired) {
        this.operatorRequired = operatorRequired;
    }

    public void setPreviousSetting(InstrumentReservationSetting previousSetting) {
        this.previousSetting = previousSetting;
    }

    public void setReminderDays(int reminderDays) {
        this.reminderDays = reminderDays;
    }

    public void setSlotDuration(Duration slotDuration) {
        this.slotDuration = slotDuration;
    }

    public void setSlotLabelInterval(Duration slotLabelInterval) {
        this.slotLabelInterval = slotLabelInterval;
    }

    public void setSlotLabelIntervalHelper(DurationHelper slotLabelIntervalHelper) {
        this.slotLabelIntervalHelper = slotLabelIntervalHelper;
    }

    public void setSlotMaxTime(Duration slotMaxTime) {
        this.slotMaxTime = slotMaxTime;
    }

    public void setSlotMaxTimeHelper(DurationHelper slotMaxTimeHelper) {
        this.slotMaxTimeHelper = slotMaxTimeHelper;
    }

    public void setSlotMinTime(Duration slotMinTime) {
        this.slotMinTime = slotMinTime;
    }

    public void setSlotMinTimeHelper(DurationHelper slotMinTimeHelper) {
        this.slotMinTimeHelper = slotMinTimeHelper;
    }

    public void setSlotShiftTime(Duration slotShiftTime) {
        this.slotShiftTime = slotShiftTime;
    }

    public void setSlotShiftTimeHelper(DurationHelper slotShiftTimeHelper) {
        this.slotShiftTimeHelper = slotShiftTimeHelper;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public void setWeekends(boolean weekends) {
        this.weekends = weekends;
    }

    public void slotDurationChanged() {
        resetMinDurationHelper();
        if (getMaxDuration() != null && getSlotDuration() != null && getMaxDuration().toMinutes() < getSlotDuration().toMinutes()) {
            resetMaxDurationHelper();
        }
    }

    public void slotLabelIntervalHelperChanged() {
        if (getSlotLabelIntervalHelper() != null) {
            if (getSlotLabelIntervalHelper().getHours() != null && getSlotLabelIntervalHelper().getHours() >= 24) {
                getSlotLabelIntervalHelper().setHours(24L);
                getSlotLabelIntervalHelper().setMinutes(0L);
            }
            setSlotLabelInterval(getSlotLabelIntervalHelper().getDuration());
        }
    }

    public void slotMaxTimeChanged() {
        setSlotMaxTime(getSlotMaxTimeHelper().getDuration());
        if (slotDuration != null) {
            resetSlotDuration();
        }
    }

    public void slotMinTimeChanged() {
        setSlotMinTime(getSlotMinTimeHelper().getDuration());
        if (getSlotDuration() != null) {
            resetSlotDuration();
        }
        resetSlotLabelIntervalHelper();
    }

    public void slotShiftTimeHelperChanged() {
        if (getSlotShiftTimeHelper() != null) {
            if (getSlotShiftTimeHelper().getHours() != null && getSlotShiftTimeHelper().getDuration().toMinutes() >= (getSlotDuration() != null ? getSlotDuration().toMinutes() : 0)) {
                getSlotShiftTimeHelper().setHours(0L);
                getSlotShiftTimeHelper().setMinutes(0L);
            }
            setSlotShiftTime(getSlotShiftTimeHelper().getDuration());
        }
    }

    private LocalTime toLocalTime(int minutes) {
        int extractedHours = minutes / 60;
        int extractedMinutes = minutes % 60;
        return LocalTime.of(extractedHours, extractedMinutes);
    }

    public void validateValidFrom(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        LocalDate newValidFrom = (LocalDate) value;
        if (newValidFrom != null && getValidUntil() != null && !getValidUntil().isAfter(newValidFrom)) {
            throw new BfabricValidatorException("validFromException");
        }
    }
}
