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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexMap;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.AgendaYearClosedService;
import org.bfabric.service.EventService;
import org.bfabric.util.AgendaEventHelper;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.RepeaterHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "AgendaEvent")
@XmlRootElement
@NamedQuery(name = "Event.findByInterval", query = "SELECT a FROM Event a WHERE a.startDate <= :endDate and a.endDate >= :startDate")
@NamedQuery(name = "Event.findByIntervalAndUser", query = "SELECT a FROM Event a WHERE a.startDate <= :endDate and a.endDate >= :startDate and (a.user is null or a.user = :user)")
@NamedQuery(name = "Event.findByIntervalAndUsers", query = "SELECT a FROM Event a WHERE a.startDate <= :endDate and a.endDate >= :startDate and (a.user is null or a.user in (:users))")
@NamedQuery(name = "Event.findByIntervalAndEvent", query = "SELECT a FROM Event a where a.startDate <= :endDate and a.endDate >= :startDate and a.id <> :id")
@NamedQuery(name = "Event.findPublicEvents", query = "SELECT a FROM Event a where a.user is null order by a.startDate")
@NamedQuery(name = "Event.findPublicEventsByInterval", query = "SELECT a FROM Event a where a.user is null and a.startDate <= :endDate and a.endDate >= :startDate")
@NamedQuery(name = "Event.findCollidingPublicEvents", query = "SELECT a FROM Event a where a.user is null and a.startDate < :endDate and a.endDate > :startDate and a.id <> :id")
@NamedQuery(name = "Event.findCollidingPrivateEvents", query = "SELECT a FROM Event a where a.user = :user and a.startDate < :endDate and a.endDate > :startDate and a.id <> :id")
@NamedQuery(name = "Event.findUpcomingUserEvent", query = "SELECT a FROM Event a WHERE a.user = :user and a.startDate > current_date and a.startDate < :endDate and a.endDate > :startDate")
@NamedQuery(name = "Event.findAccountableEventsByInterval", query = "SELECT a FROM Event a join fetch a.eventType eventType where eventType.accountable = true and a.startDate <= :endDate and a.endDate >= :startDate")
public class Event extends AbstractEvent implements ShowScreen, Indexable {

    public static final String AM = "AM";

    public static final int MIN_SLOT_DURATION = 5;

    public static final String PM = "PM";

    private static final long serialVersionUID = 1;

    @NotNull
    @Digits(integer = 3, fraction = 1)
    @XmlElement
    private BigDecimal accountedDays = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

    @Column(updatable = false, insertable = false)
    @XmlElement
    private BigDecimal duration;

    @Transient
    private String endSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventtypeid")
    @NotNull
    @XmlIDREF
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repeaterid")
    @XmlIDREF
    private Event repeater;

    @OneToMany(mappedBy = "repeater", cascade = { CascadeType.PERSIST })
    @OrderBy("startDate")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<Event> repeaterEvents = new ArrayList<>();

    @Transient
    private String startSlot;

    public Event() {
        // Set default to current date AM for the start and PM for the end.
        setStart(LocalDateTime.now(), AM);
        setEnd(LocalDateTime.now(), PM);
    }

    @Override
    public Event clone() throws CloneNotSupportedException {
        Event event = (Event) super.clone();
        event.repeater = null;
        event.repeaterEvents = new ArrayList<>();
        return event;
    }

    public BigDecimal getAccountedDays() {
        return accountedDays;
    }

    public String getCalendarEventInfo() {
        StringBuilder info = new StringBuilder();

        if (getUser() != null) {
            info.append(getUser().getFullName()).append(": ");
        }
        info.append(getEventType().getName());
        info.append(" (").append(getDurationWithTimeUnit()).append(")");

        return info.toString();
    }

    @Override
    public String getDateIntervalAsText() {
        if (getStartDateSlot().equals(getEndDateSlot())) {
            return Constants.DATE_FORMATTER.format(getStartDateSlot()) + (getStartSlot().equals(getEndSlot()) ? " " + getStartSlot() : Constants.EMPTY_STRING);
        }
        return getStartDateAsText() + " - " + getEndDateAsText();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AGENDAUSER;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public BigDecimal getDurationComputed() {
        double numberOfDays = 0;
        LocalDateTimeInterval interval = new LocalDateTimeInterval(getStartDate(), getEndDate());
        LocalDateTimeInterval tmpDate = new LocalDateTimeInterval(interval.getStart(), interval.getStart().plusHours(MIN_SLOT_DURATION));
        while (interval.contains(tmpDate)) {
            if (!(tmpDate.getStart().getDayOfWeek().equals(DayOfWeek.SATURDAY) || tmpDate.getStart().getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                numberOfDays += 0.5;
            }
            // proceed to next slot
            if (tmpDate.getStart().getHour() < 12) {
                tmpDate = new LocalDateTimeInterval(tmpDate.getStart().plusHours(MIN_SLOT_DURATION), tmpDate.getEnd().plusHours(MIN_SLOT_DURATION));
            } else {
                tmpDate = new LocalDateTimeInterval(tmpDate.getStart().plusDays(1).minusHours(MIN_SLOT_DURATION), tmpDate.getEnd().plusDays(1).minusHours(MIN_SLOT_DURATION));
            }
        }
        return BigDecimal.valueOf(numberOfDays);
    }

    public String getDurationWithTimeUnit() {
        BigDecimal durationComputed = getDurationComputed();
        return durationComputed + " " + Messages.get(durationComputed.doubleValue() == 1 ? "day" : "days").toLowerCase();
    }

    @Override
    public String getEndDateAsText() {
        return Constants.DATE_FORMATTER.format(getEndDate()) + " " + getEndSlot();
    }

    public String getEndSlot() {
        if (getEndDate() != null) {
            return getEndDate().toLocalTime().getHour() > 13 ? PM : AM;
        }
        return Optional.ofNullable(endSlot).orElse(PM);
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getUser() != null) {
            addEntityInfoItem(summary, "user", getUser().getFullName());
        }
        if (getEventType() != null) {
            addEntityInfoItem(summary, "type", getEventType().getName());
        }
        if (StringHelper.isNotEmpty(getStartDateAsText())) {
            addEntityInfoItem(summary, "startDate", getStartDateAsText());
        }
        if (StringHelper.isNotEmpty(getEndDateAsText())) {
            addEntityInfoItem(summary, "endDate", getEndDateAsText());
        }
        addEntityInfoItem(summary, "days", getDuration());
        if (getAccountedDays() != null) {
            addEntityInfoItem(summary, "accountedDays", getAccountedDays());
        }
        if (getRepeaterId() != null) {
            addEntityInfoItem(summary, "repeaterId", getRepeaterId());
        }
        return summary.toString();
    }

    @Override
    public String getEventCategory() {
        return getEventType().getName();
    }

    @Override
    public String getEventInfo(boolean full) {
        StringBuilder info = new StringBuilder();

        if (getUser() != null) {
            info.append(getUser().getFullName()).append(": ");
        }

        info.append(getDateIntervalAsText());
        info.append(" ").append(getEventType().getName());

        info.append(" (").append(getDurationWithTimeUnit());
        if (full) {
            if (getAccountedDays().doubleValue() > 0) {
                info.append(", ").append(Messages.get("accounted").toLowerCase()).append(" ").append(getAccountedDays());
            }

            if (getRepeater() != null) {
                info.append(", ").append(Messages.get("repeatingEvent"));
            }
        }
        info.append(")");

        return info.toString();
    }

    @Override
    public String getEventLocation() {
        return Constants.EMPTY_STRING;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
        fields.add(IndexMapContentEnum.USERID.getField());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.USER.getField());
        fields.add(IndexMapContentEnum.EVENTTYPE.getField());
        fields.add(IndexMapContentEnum.YEAR.getField());
        fields.add(IndexMapContentEnum.STARTDATE.getField());
        fields.add(IndexMapContentEnum.ENDDATE.getField());
        fields.add(IndexMapContentEnum.DAYS.getField());
        fields.add(IndexMapContentEnum.ACCOUNTEDDAYS.getField());
        fields.add(IndexMapContentEnum.REPEATERID.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        return fields;
    }

    @Override
    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = super.getIndexMap();
        indexMap.put(Constants.INDEXMAP_GROUP, RoleEnum.AGENDAUSER);
        return indexMap;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getName());
        if (getEventType() != null) {
            content.add(IndexMapContentEnum.EVENTTYPE, getEventType().getName());
        }
        content.add(IndexMapContentEnum.YEAR, getYear());
        content.add(IndexMapContentEnum.STARTDATE, getStartDate());
        content.add(IndexMapContentEnum.ENDDATE, getEndDate());
        content.add(IndexMapContentEnum.DAYS, getDuration());
        content.add(IndexMapContentEnum.ACCOUNTEDDAYS, getAccountedDays());
        if (getRepeater() != null) {
            content.add(IndexMapContentEnum.REPEATERID, getRepeaterId().toString());
        }
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());

        addUserToIndexMapContent(getUser(), content);

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.EVENT;
    }

    @Override
    public Event getRepeater() {
        return repeater;
    }

    @Override
    public List<Event> getRepeaterEvents() {
        return repeaterEvents;
    }

    @Override
    public String getStartDateAsText() {
        return Constants.DATE_FORMATTER.format(getStartDate()) + " " + getStartSlot();
    }

    public String getStartSlot() {
        if (getStartDate() != null) {
            return getStartDate().toLocalTime().getHour() < 13 ? AM : PM;
        }
        return Optional.ofNullable(startSlot).orElse(AM);
    }

    public boolean isCurrent() {
        return LocalDateTime.now().isAfter(getStartDate()) && LocalDateTime.now().isBefore(getEndDate());
    }

    public boolean isDeletableAll() {
        return CDI.current().select(AgendaYearClosedService.class).get().isOpen(getYear()) && (hasCurrentUserRoleEnum(RoleEnum.AGENDAMANAGER) || hasCurrentUserRoleEnum(getDefaultRequiredRole())
            && isCreatorOrUser(getUser()) && isInEditableInterval(getRepeater()));
    }

    public boolean isInEditableInterval() {
        return isInEditableInterval(this);
    }

    public boolean isInEditableInterval(Event event) {
        return event != null && (event.getEndDate().plusDays(7).isAfter(LocalDateTime.now()) || event.getCreated().plusDays(7).isAfter(LocalDateTime.now()));
    }

    @Override
    public boolean isUpdatable() {
        return CDI.current().select(AgendaYearClosedService.class).get().isOpen(getYear()) && (hasCurrentUserRoleEnum(RoleEnum.AGENDAMANAGER) || hasCurrentUserRoleEnum(getDefaultRequiredRole())
            && isCreatorOrUser(getUser()) && isInEditableInterval());
    }

    public boolean isUserAvailableAffected() {
        // TODO: adapt in the meeting branch!
        // return LocalDateTime.now().isAfter(getStartDate()) && LocalDateTime.now().isBefore(getEndDate());
        return LocalDateTime.now().isAfter(getStartDate().minusHours(1)) && LocalDateTime.now().isBefore(getEndDate().minusHours(1));
    }

    public boolean isUserEditable() {
        return hasCurrentUserRoleEnum(RoleEnum.AGENDAMANAGER) && getEventType() != null && !getEventType().isPublicEvent();
    }

    public boolean isUserNotAvailable() {
        return getEventType() != null && !getEventType().isPublicEvent() && !getEventType().getName().equalsIgnoreCase("homework");
    }

    @Override
    public void resetEndDate() {
        if (getStartDate() != null) {
            this.endDate = getStartDate().plusHours(MIN_SLOT_DURATION);
            this.endDateSlot = endDate.toLocalDate();
            this.endSlot = endDate.toLocalTime().getHour() == 13 ? AM : PM;
        }
    }

    @Override
    public void resetFields() {
        // public events do not have a user
        if (getEventType().isPublicEvent()) {
            setUser(null);
        }
        setAccountedDays();
    }

    @Override
    public void resetStartDate() {
        if (getEndDate() != null) {
            this.startDate = getEndDate().minusHours(MIN_SLOT_DURATION);
            this.startDateSlot = startDate.toLocalDate();
            this.startSlot = startDate.toLocalTime().getHour() == 13 ? PM : AM;
        }
    }

    public void setAccountedDays() {
        if (getEventType().isAccountable()) {
            final AgendaEventHelper eventAgendaHelper = new AgendaEventHelper();
            try {
                eventAgendaHelper.setInterval(getStartDate(), getEndDate());
                eventAgendaHelper.setEvents(CDI.current().select(EventService.class).get().getEventsByIntervalAndEvent(eventAgendaHelper.getInterval(), this));
                setAccountedDays(eventAgendaHelper.getAccountableDays(this));
            } catch (Exception e) {
                setAccountedDays(BigDecimal.ZERO);
            }
        } else {
            setAccountedDays(BigDecimal.ZERO);
        }
    }

    public void setAccountedDays(BigDecimal accountedDays) {
        this.accountedDays = NumberUtils.getDecimalScale(accountedDays, 1);
    }

    @Override
    public void setDateTimeSlot(String dateTimeSlot) {
        if (StringHelper.isNotEmpty(dateTimeSlot)) {
            final LocalDateTime dateTime = LocalDate.parse(dateTimeSlot.substring(0, 10), Constants.DATE_FORMATTER).atStartOfDay();
            final String slot = dateTimeSlot.substring(11, 13);
            setStart(dateTime, slot);
            setEnd(dateTime, slot);
        }
    }

    public void setEnd(LocalDateTime endDateTime, String endSlot) {
        if (endSlot.equals(AM)) {
            this.endSlot = endSlot;
            this.endDate = endDateTime.withHour(13);
        } else {
            this.endSlot = PM;
            this.endDate = endDateTime.withHour(18);
        }
    }

    @Override
    public void setEndDate(LocalDateTime endDate) {
        setEnd(endDate, getEndSlot());
    }

    @Override
    public void setEndDateSlot(LocalDate endDateSlot) {
        this.endDateSlot = endDateSlot;
        if (endDateSlot != null) {
            setEnd(endDateSlot.atStartOfDay(), getEndSlot());
        } else {
            setEnd(LocalDate.now().atStartOfDay(), getEndSlot());
        }
    }

    public void setEndSlot(String endSlot) {
        setEnd(getEndDate(), endSlot);
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public void setRepeater(AbstractEvent repeater) {
        this.repeater = (Event) repeater;
    }

    public void setRepeaterEvents(RepeaterHelper repeaterHelper) throws CloneNotSupportedException {
        // Important note: Do not remove the following lines since they are needed in case during interaction the repeater is switch off after a validation error.
        if (!getRepeaterEvents().isEmpty()) {
            setRepeater(null);
            getRepeaterEvents().clear();
        }

        if (repeaterHelper != null && repeaterHelper.isRepeat()) {
            setRepeater(this);
            getRepeaterEvents().add(this);

            LocalDateTime tmpStart = getStartDate().plusWeeks(repeaterHelper.getWeeks());
            LocalDateTime tmpEnd = getEndDate().plusWeeks(repeaterHelper.getWeeks());

            while (tmpEnd.isBefore(repeaterHelper.getEnd().plusDays(1))) {
                // Create and store new event
                final Event newEvent = clone();
                newEvent.setStartDate(tmpStart);
                newEvent.setEndDate(tmpEnd);
                newEvent.setRepeater(this);
                // Split event if it overlaps two or more years
                if (newEvent.isYearCrossing()) {
                    getRepeaterEvents().addAll(newEvent.splitEvent());
                } else {
                    getRepeaterEvents().add(newEvent);
                }

                // Increase start and end time
                tmpStart = tmpStart.plusWeeks(repeaterHelper.getWeeks());
                tmpEnd = tmpEnd.plusWeeks(repeaterHelper.getWeeks());
            }
        }
    }

    public void setStart(LocalDateTime startDateTime, String startSlot) {
        if (startSlot.equals(PM)) {
            this.startSlot = startSlot;
            this.startDate = startDateTime.withHour(13);
        } else {
            this.startSlot = AM;
            this.startDate = startDateTime.withHour(8);
        }
    }

    @Override
    public void setStartDate(LocalDateTime startDate) {
        setStart(startDate, getStartSlot());
    }

    public void setStartDateSlot(LocalDate startDateSlot) {
        this.startDateSlot = startDateSlot;
        if (startDateSlot != null) {
            setStart(startDateSlot.atStartOfDay(), getStartSlot());
        } else {
            setStart(LocalDate.now().atStartOfDay(), getStartSlot());
        }
    }

    public void setStartSlot(String startSlot) {
        setStart(getStartDate(), startSlot);
    }

    public Set<Event> splitEvent() {
        Set<Event> events = new HashSet<>();
        if (isYearCrossing()) {
            Event newEvent = new Event();
            newEvent.setEventType(getEventType());
            newEvent.setUser(getUser());
            newEvent.setStart(getStartDate().plusYears(1).withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0), AM);
            newEvent.setEnd(getEndDate(), getEndSlot());
            newEvent.setDescription(getDescription());
            newEvent.setRepeater(getRepeater());

            setEnd(getStartDate().withMonth(12).withDayOfMonth(31).withHour(0).withMinute(0).withSecond(0).withNano(0), PM);

            events.add(this);
            events.addAll(newEvent.splitEvent());
        } else {
            events.add(this);
        }
        return events;
    }

    public void typeChanged(ValueChangeEvent event) {
        setEventType((EventType) event.getNewValue());
    }
}
