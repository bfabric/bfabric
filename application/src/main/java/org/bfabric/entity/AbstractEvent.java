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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.service.UserService;
import org.bfabric.util.RepeaterHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.ScheduleDisplayMode;

@MappedSuperclass
@DynamicUpdate
public abstract class AbstractEvent extends AbstractDescriptionBaseEntity implements Indexable {

    private static final long serialVersionUID = 1;

    @Transient
    protected RepeaterHelper repeaterHelper = new RepeaterHelper();

    @NotNull
    protected LocalDateTime endDate;

    @NotNull
    protected LocalDateTime startDate;

    @Transient
    protected Duration startTime;

    @Transient
    protected Duration endTime;

    @Transient
    protected LocalDate endDateSlot;

    @Transient
    protected LocalDate startDateSlot;

    @Transient
    protected Boolean allDayEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    @XmlIDREF
    private User user;

    public AbstractEvent() {
    }

    public static DefaultScheduleEvent getDefaultBackgroundEvent(LocalDateTime startDate, LocalDateTime endDate, String title) {
        return DefaultScheduleEvent.builder()
            .title(title)
            .startDate(startDate)
            .endDate(endDate)
            .overlapAllowed(true)
            .editable(false)
            .resizable(false)
            .display(ScheduleDisplayMode.BACKGROUND)
            .backgroundColor("lightgreen")
            .build();
    }

    public void addEventToIcsCalendar(Calendar calendar) {
        VEvent vEvent = new VEvent(getStartDate(), getEndDate(), getCalendarEventInfo());
        vEvent.add(new Uid(getTrimmedClassName() + "_" + getId()));
        vEvent.add(new Categories(getEventCategory()));
        vEvent.add(new Location(getEventLocation()));
        vEvent.add(new Description(getDescription()));
        vEvent.add(getVAlarm(getTriggerTime()));
        calendar.add(vEvent);
    }

    public void allDayEventChanged() {
        setAllDayEvent(!isAllDayEvent());
        if (isAllDayEvent()) {
            setAllDayEvent();
        }
    }

    public void exportAndDownloadIcs() {
        download(getClassLabelLowerCaseId() + ".ics", getIcsExport().toString());
    }

    public String getBackgroundColor() {
        return "";
    }

    public abstract String getCalendarEventInfo();

    public String getDateIntervalAsText() {
        if (getStartDateSlot().equals(getEndDateSlot())) {
            return Constants.DATE_FORMATTER.format(getStartDateSlot()) + " " + StringHelper.getTimeFormat(getStartTime()) + "-" + StringHelper.getTimeFormat(getEndTime());
        }
        return getStartDateAsText() + " - " + getEndDateAsText();
    }

    public DefaultScheduleEvent getDefaultScheduleEvent() {
        return DefaultScheduleEvent.builder().title(getEventTitle()).startDate(getStartDate()).endDate(getEndDate()).styleClass(getStyleClass()).backgroundColor(getBackgroundColor()).data(this)
            .build();
    }

    public List<Integer> getDisabledWeekends() {
        List<Integer> invalidDays = new ArrayList<>();
        invalidDays.add(0);
        invalidDays.add(6);
        return invalidDays;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getName();
    }

    public long getDurationAsWeeks() {
        return ChronoUnit.WEEKS.between(getStartDateSlot(), getEndDateSlot());
    }

    public List<User> getEmployeesIncludingUser(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getUser());
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public LocalDateTime getEndDateAllDayTime() {
        return getEndDateSlot() != null ? getEndDateSlot().atStartOfDay().plusDays(1) : null;
    }

    @XmlElement(name = "enddate")
    public String getEndDateAsText() {
        return Constants.DATE_FORMATTER.format(getEndDateSlot()) + " " + StringHelper.getTimeFormat(getEndTime());
    }

    public LocalDate getEndDateSlot() {
        if (endDateSlot == null && getEndDate() != null) {
            endDateSlot = getEndDate().toLocalDate().minusDays(getEndDate().toLocalTime().equals(LocalTime.MIDNIGHT) ? 1 : 0);
        }
        return endDateSlot;
    }

    public LocalDateTime getEndDateTime() {
        return getEndDateSlot().atStartOfDay().plus(getEndTime());
    }

    public Duration getEndTime() {
        if (endTime == null) {
            endTime = Duration.between(getEndDateSlot().atStartOfDay(), getEndDate());
        }
        return endTime;
    }

    public abstract String getEventCategory();

    public abstract String getEventInfo(boolean full);

    public abstract String getEventLocation();

    public String getEventTitle() {
        return getEventInfo(true);
    }

    public String getFullEventInfo() {
        return getEventInfo(true);
    }

    public Calendar getIcsExport(Collection<? extends AbstractEvent> events) {
        Calendar calendar = getConfiguration().getIcsCalendar();
        if (events != null && !events.isEmpty()) {
            for (AbstractEvent event : events) {
                event.addEventToIcsCalendar(calendar);
            }
            calendar.validate();
        }
        return calendar;
    }

    public Calendar getIcsExport() {
        Calendar calendar = getConfiguration().getIcsCalendar();
        addEventToIcsCalendar(calendar);
        calendar.validate();
        return calendar;
    }

    public String getName() {
        return getEventInfo(false);
    }

    public abstract AbstractEvent getRepeater();

    public AbstractEvent getRepeaterEventNext() {
        AbstractEvent repeaterEventNext = null;
        if (isRepeaterEvent()) {
            for (AbstractEvent event : getRepeater().getRepeaterEvents()) {
                if (event.getStartDate().isAfter(getStartDate()) && (repeaterEventNext == null || event.getStartDate().isBefore(repeaterEventNext.getStartDate()))) {
                    repeaterEventNext = event;
                }
            }
        }
        return repeaterEventNext;
    }

    public abstract List<? extends AbstractEvent> getRepeaterEvents();

    public List<? extends AbstractEvent> getRepeaterEventsFollowing() {
        List<AbstractEvent> repeaterEventsFollowing = new ArrayList<>();
        repeaterEventsFollowing.add(this);
        if (isRepeaterEvent()) {
            for (AbstractEvent repeaterEvent : getRepeater().getRepeaterEvents()) {
                if (repeaterEvent.getStartDate().isAfter(getStartDate())) {
                    repeaterEventsFollowing.add(repeaterEvent);
                }
            }
        }
        return repeaterEventsFollowing;
    }

    public RepeaterHelper getRepeaterHelper() {
        if (repeaterHelper == null) {
            repeaterHelper = new RepeaterHelper();
        }
        return repeaterHelper;
    }

    public LocalDate getRepeaterHelperValidUntilMaxDate() {
        return LocalDate.now();
    }

    public LocalDate getRepeaterHelperValidUntilMinDate() {
        return getEndDateSlot().plusWeeks(Math.max(getDurationAsWeeks(), getRepeaterHelper().getWeeks()));
    }

    public Long getRepeaterId() {
        return getRepeater() != null ? getRepeater().getId() : null;
    }

    public String getSlot() {
        return Constants.DATE_FORMATTER.format(getStartDate());
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getStartDateAllDayTime() {
        return getStartDateSlot() != null ? getStartDateSlot().atStartOfDay() : null;
    }

    @XmlElement(name = "startdate")
    public String getStartDateAsText() {
        return Constants.DATE_FORMATTER.format(getStartDateSlot()) + " " + StringHelper.getTimeFormat(getStartTime());
    }

    public LocalDate getStartDateSlot() {
        if (startDateSlot == null && getStartDate() != null) {
            startDateSlot = getStartDate().toLocalDate();
        }
        return startDateSlot;
    }

    public LocalDateTime getStartDateTime() {
        return getStartDateSlot().atStartOfDay().plus(getStartTime());
    }

    public Duration getStartTime() {
        if (startTime == null) {
            startTime = Duration.between(getStartDate().toLocalDate().atStartOfDay(), getStartDate());
        }
        return startTime;
    }

    public String getStyleClass() {
        return "nonChargeable";
    }

    public Instant getTriggerTime() {
        return getStartDate().minusDays(1).atZone(ZoneId.of("Europe/Paris")).toInstant();
    }

    public User getUser() {
        return user;
    }

    private VAlarm getVAlarm(Instant instant) {
        VAlarm vAlarm = new VAlarm();
        vAlarm.add(Action.DISPLAY);
        vAlarm.add(new Trigger(instant));
        vAlarm.add(new Description(getDescription()));
        return vAlarm;
    }

    public int getYear() {
        return getStartDate().getYear();
    }

    @Override
    public void index() {
        if (getRepeater() != null && getRepeaterEvents().size() > 1) {
            IndexHelper.indexEntities(getRepeaterEvents());
        } else {
            IndexHelper.indexEntity(this);
        }
    }

    public boolean isAllDayEvent() {
        if (allDayEvent == null) {
            allDayEvent = isAllDayRendered() && getStartDate().equals(getStartDateAllDayTime()) && getEndDate().equals(getEndDateAllDayTime());
        }
        return allDayEvent;
    }

    public boolean isAllDayEventCheckboxDisabled() {
        return false;
    }

    public boolean isAllDayRendered() {
        return true;
    }

    public boolean isInYear(int year) {
        return getEndDate().getYear() == year || getStartDate().getYear() == year;
    }

    public boolean isRepeaterEvent() {
        return getRepeater() != null;
    }

    public boolean isRepeaterEventFirst() {
        return isRepeaterEvent() && equals(getRepeater());
    }

    public boolean isYearCrossing() {
        return getEndDate().getYear() != getStartDate().getYear();
    }

    @Override
    protected void preRemove() {
        super.preRemove();
        // If the event is the start of a repeater, then reset the start to the next event in the repeater.
        if (isRepeaterEventFirst()) {
            setRepeaterToNext();
        }
    }

    public void repeatChanged(ValueChangeEvent event) {
        if (event.getNewValue() == null) {
            logger.info("This should not happen: event.getNewValue() == null! " + getEntityInfo());
        }
        if (event.getNewValue() != null) {
            getRepeaterHelper().setRepeat((Boolean) event.getNewValue());
            getRepeaterHelper().setEndDate(getEndDate());
        }
    }

    public String repeaterEndChanged() {
        // reset repeater end date if it is not valid
        if (!getRepeaterHelper().isValidEndDate(getEndDate())) {
            getRepeaterHelper().setEndDate(getEndDate());
            return Messages.get("resetRepeaterDateNotValidHint");
        }
        return null;
    }

    public String repeaterWeeksChanged(ValueChangeEvent event) {
        getRepeaterHelper().setWeeks((Integer) event.getNewValue());
        if (!getRepeaterHelper().isValidEndDate(getEndDate())) {
            getRepeaterHelper().setEndDate(getEndDate());
            return Messages.get("noteAdaptedRepeatDateHint");
        }
        return null;
    }

    public void resetEndDate() {
        if (getStartDate() != null && getEndDate() != null) {
            setEndDate(getStartDate());
        }
    }

    public void resetEndDateTime() {
        setEndDateSlot(null);
        setEndTime(null);
    }

    public void resetFields() {
    }

    public void resetRepeater() {
        getRepeaterHelper().init();
    }

    public void resetStartDate() {
        if (getStartDate() != null && getEndDate() != null) {
            setStartDate(getEndDate());
        }
    }

    public void resetStartDateTime() {
        setStartDateSlot(null);
        setStartTime(null);
    }

    public void setAllDayEvent() {
        setStartDate(getStartDateAllDayTime());
        setEndDate(getEndDateAllDayTime());
    }

    public void setAllDayEvent(boolean allDayEvent) {
        this.allDayEvent = allDayEvent;
    }

    public void setDateTimeSlot(String dateTimeSlot) {
        if (StringHelper.isNotEmpty(dateTimeSlot)) {
            final LocalDateTime dateTime = LocalDate.parse(dateTimeSlot.substring(0, 10), Constants.DATE_FORMATTER).atStartOfDay();
            setStartDate(dateTime);
            setEndDate(dateTime);
        }
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        resetEndDateTime();
    }

    public void setEndDate() {
        setEndDate(getEndDateSlot().atStartOfDay().plus(getEndTime()));
    }

    public void setEndDateSlot(LocalDate endDateSlot) {
        this.endDateSlot = endDateSlot;
        if (endDateSlot != null) {
            setEndDate();
        }
    }

    public void setEndTime(Duration endTime) {
        this.endTime = endTime;
        if (endTime != null) {
            setEndDate();
        }
    }

    public abstract void setRepeater(AbstractEvent repeater);

    public void setRepeaterHelper(RepeaterHelper repeaterHelper) {
        this.repeaterHelper = repeaterHelper;
    }

    public void setRepeaterToNext() {
        if (isRepeaterEventFirst()) {
            AbstractEvent newRepeater = getRepeaterEventNext();
            for (AbstractEvent event : getRepeaterEvents()) {
                if (!event.equals(this)) {
                    event.setRepeater(newRepeater);
                }
            }
        }
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        resetStartDateTime();
    }

    public void setStartDate() {
        setStartDate(getStartDateSlot().atStartOfDay().plus(getStartTime()));
    }

    public void setStartDateSlot(LocalDate startDateSlot) {
        this.startDateSlot = startDateSlot;
        if (startDateSlot != null) {
            setStartDate();
        }
    }

    public void setStartTime(Duration startTime) {
        this.startTime = startTime;
        if (startTime != null) {
            setStartDate();
        }
    }

    public void setUser(User user) {
        this.user = user;
    }
}