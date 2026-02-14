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

package org.bfabric.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import com.google.common.collect.Sets;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractEvent;
import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.entity.InstrumentReservationType;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class InstrumentReservationService extends AbstractEventService {

    public static final String VIEW_APPROVALPENDING = "approvalPending";

    public static final String VIEW_APPROVED = "approved";

    public static final String VIEW_NON_REJECTED = "nonRejected";

    public static final String VIEW_REJECTED = "rejected";

    private static final long serialVersionUID = 1;

    @Inject
    protected EntityService entityService;

    public InstrumentReservationService() {
        super(InstrumentReservation.class);
    }

    public boolean checkByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        return interval != null && instrument != null && !createNamedQuery("InstrumentReservation.checkByIntervalAndInstrument").setParameter("instrument", instrument)
            .setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList().isEmpty();
    }

    public boolean checkByIntervalAndInstrumentExcludingEvents(LocalDateTimeInterval interval, InstrumentReservation instrumentReservation) {
        return instrumentReservation != null && interval != null && !createNamedQuery("InstrumentReservation.checkByIntervalAndInstrumentExcluding").setParameter("instrument", instrumentReservation
            .getInstrument()).setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).setParameter("id", instrumentReservation.getId()).getResultList().isEmpty();
    }

    public boolean existsFutureCollidingEventByOperator(User user, LocalDateTimeInterval interval) {
        return !createNamedQuery("InstrumentReservation.existsFutureCollidingEventByOperator").setParameter("userId", user.getId()).setParameter("startDate", interval.getStart())
            .setParameter("endDate", interval.getEnd()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean existsFutureCollidingEventByUser(User user, LocalDateTimeInterval interval) {
        return !createNamedQuery("InstrumentReservation.existsFutureCollidingEventByUser").setParameter("userId", user.getId()).setParameter("startDate", interval.getStart())
            .setParameter("endDate", interval.getEnd()).setMaxResults(1).getResultList().isEmpty();
    }

    public BfabricLazyDataModel<InstrumentReservation> getApprovalRequiredTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.approved is null and entity.instrumentReservationSetting.approvalRequired = TRUE");
        if (!user.hasRoleImplicit(RoleEnum.ADMIN)) {
            entityQuery.setJoin("entity.containers container");
            entityQuery.setJoinTypeLeftOuter();
            entityQuery
                .addWhereClause("entity.operator.id=:userId or entity.instrumentReservationSetting.notifyInstrumentSupervisor = TRUE and entity.instrument.supervisor.id = :userId or entity.instrumentReservationSetting.notifyCoach = TRUE and (container.coach.id = :userId or container.coachBackup.id = :userId)");
            entityQuery.addParameter("userId", user.getId());
        }
        entityQuery.setOrder("entity.id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<InstrumentReservation> getAssignOperatorTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.operator is null and entity.instrumentReservationSetting.operatorRequired = TRUE");
        if (!user.hasRoleImplicit(RoleEnum.ADMIN)) {
            entityQuery.setJoin("entity.containers container");
            entityQuery.setJoinTypeLeftOuter();
            entityQuery
                .addWhereClause("entity.instrumentReservationSetting.notifyInstrumentSupervisor = TRUE and entity.instrument.supervisor.id = :userId or entity.instrumentReservationSetting.notifyCoach = TRUE and (container.coach.id = :userId or container.coachBackup.id = :userId)");
            entityQuery.addParameter("userId", user.getId());
        }
        entityQuery.setOrder("entity.id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    @Override
    public List<AbstractEvent> getCollidingEventsByEvent(AbstractEvent abstractEvent) {
        InstrumentReservation event = (InstrumentReservation) abstractEvent;
        return createNamedQuery("InstrumentReservation.findCollidingEventsByInstrumentAndEvent").setParameter("startDate", event.getStartDate()).setParameter("endDate", event.getEndDate())
            .setParameter("instrument", event.getInstrument()).setParameter("id", event.getId()).getResultList();
    }

    public String getDisabledScheduleMessage(LocalDateTime startDate, Duration bookingAheadMinDuration, Duration bookingAheadMaxDuration) {
        Duration tillEvent = Duration.between(LocalDateTime.now(), startDate);
        if (bookingAheadMaxDuration != null && bookingAheadMaxDuration.minus(tillEvent).isNegative()) {
            return Messages.get("warningScheduleDisabledForBookingAheadMaxViolation").replace("{0}", startDate.minusMinutes(bookingAheadMaxDuration.toMinutes()).toString());
        }
        if (bookingAheadMinDuration != null && !bookingAheadMinDuration.minus(tillEvent).isNegative()) {
            return Messages.get("warningScheduleDisabledForBookingAheadMinViolation").replace("{}", startDate.minusMinutes(bookingAheadMinDuration.toMinutes()).toString());
        }
        return null;
    }

    public InstrumentReservation getEarliestInstrumentReservationBySetting(InstrumentReservationSetting instrumentReservationSetting) {
        List<InstrumentReservation> instrumentReservations = createNamedQuery("InstrumentReservation.findBySettingOrderedByEndDateDesc")
            .setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId()).setMaxResults(1).getResultList();
        return instrumentReservations.isEmpty() ? null : instrumentReservations.get(0);
    }

    public List<InstrumentReservation> getEventsApprovalPendingByInterval(LocalDateTimeInterval interval) {
        return createNamedQuery("InstrumentReservation.findApprovalPendingByInterval").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsApprovalPendingByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        return createNamedQuery("InstrumentReservation.findApprovalPendingByIntervalAndInstrument").setParameter("instrument", instrument).setParameter("startDate", interval.getStart())
            .setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsApprovedByInterval(LocalDateTimeInterval interval) {
        return createNamedQuery("InstrumentReservation.findApprovedByInterval").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsApprovedByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        return createNamedQuery("InstrumentReservation.findApprovedByIntervalAndInstrument").setParameter("instrument", instrument).setParameter("startDate", interval.getStart())
            .setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsByInterval(LocalDateTimeInterval interval, String view) {
        if (VIEW_NON_REJECTED.equals(view)) {
            return getEventsNonRejectedByInterval(interval);
        } else if (VIEW_REJECTED.equals(view)) {
            return getEventsRejectedByInterval(interval);
        } else if (VIEW_APPROVED.equals(view)) {
            return getEventsApprovedByInterval(interval);
        } else if (VIEW_APPROVALPENDING.equals(view)) {
            return getEventsApprovalPendingByInterval(interval);
        }
        return getEventsByInterval(interval);
    }

    public List<InstrumentReservation> getEventsByInterval(LocalDateTimeInterval interval) {
        return createNamedQuery("InstrumentReservation.findByInterval").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument, String view) {
        if (VIEW_NON_REJECTED.equals(view)) {
            return getEventsNonRejectedByIntervalAndInstrument(interval, instrument);
        } else if (VIEW_REJECTED.equals(view)) {
            return getEventsRejectedByIntervalAndInstrument(interval, instrument);
        } else if (VIEW_APPROVED.equals(view)) {
            return getEventsApprovedByIntervalAndInstrument(interval, instrument);
        } else if (VIEW_APPROVALPENDING.equals(view)) {
            return getEventsApprovalPendingByIntervalAndInstrument(interval, instrument);
        }
        return getEventsByIntervalAndInstrument(interval, instrument);
    }

    public List<InstrumentReservation> getEventsByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        return createNamedQuery("InstrumentReservation.findByIntervalAndInstrument").setParameter("instrument", instrument).setParameter("startDate", interval.getStart()).setParameter("endDate",
            interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsByIntervalAndInstrumentExcludingEvents(LocalDateTimeInterval interval, Instrument instrument, List<InstrumentReservation> excludeEvents) {
        if (excludeEvents.isEmpty()) {
            return createNamedQuery("InstrumentReservation.findByIntervalAndInstrument").setParameter("instrument", instrument).setParameter("startDate", interval.getStart()).setParameter("endDate",
                interval.getEnd()).getResultList();
        }
        Set<Long> ids = new HashSet<>();
        for (InstrumentReservation instrumentReservation : excludeEvents) {
            ids.add(instrumentReservation.getId());
        }
        return createNamedQuery("InstrumentReservation.findByIntervalAndInstrumentExcludingIds").setParameter("instrument", instrument).setParameter("startDate", interval.getStart()).setParameter(
            "endDate", interval.getEnd()).setParameter("ids", ids).getResultList();
    }

    public List<InstrumentReservation> getEventsByIntervalAndInstrumentOrderByStartDate(LocalDateTimeInterval interval, Instrument instrument) {
        StringBuilder queryStr = new StringBuilder("SELECT a FROM InstrumentReservation a where instrument = :instrument");
        if (interval.getStart() != null)
            queryStr.append(" and endDate >= :startDate");
        if (interval.getEnd() != null) {
            queryStr.append(" and startDate <= :endDate");
        }
        queryStr.append(" ORDER BY startDate");
        Query query = createQuery(queryStr.toString());
        query.setParameter("instrument", instrument);
        if (interval.getStart() != null)
            query.setParameter("startDate", interval.getStart());
        if (interval.getEnd() != null) {
            query.setParameter("endDate", interval.getEnd());
        }
        return query.getResultList();
    }

    public List<InstrumentReservation> getEventsNonRejectedByInterval(LocalDateTimeInterval interval) {
        return createNamedQuery("InstrumentReservation.findNonRejectedByInterval").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsNonRejectedByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        return createNamedQuery("InstrumentReservation.findNonRejectedByIntervalAndInstrument").setParameter("instrument", instrument).setParameter("startDate", interval.getStart())
            .setParameter("endDate",
                interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsRejectedByInterval(LocalDateTimeInterval interval) {
        return createNamedQuery("InstrumentReservation.findRejectedByInterval").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getEventsRejectedByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        return createNamedQuery("InstrumentReservation.findRejectedByIntervalAndInstrument").setParameter("instrument", instrument).setParameter("startDate", interval.getStart())
            .setParameter("endDate",
                interval.getEnd()).getResultList();
    }

    public List<AbstractEvent> getFutureCollidingEventsByUser(User user, LocalDateTimeInterval interval) {
        return createNamedQuery("InstrumentReservation.findUpcomingUserInstrumentReservation").setParameter("user", user).setParameter("startDate", interval.getStart())
            .setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getInstrumentReservationByContainerQuery(String filterString, Collection<InstrumentReservation> excluded, Collection<InstrumentReservation> included, ChronoUnit unit) {
        EntityQuery entityQuery = createEntityQuery();
        if (StringHelper.isNotEmpty(filterString)) {
            entityQuery.addWhereClause("LOWER(entity.instrument.name) LIKE :filterString");
            entityQuery.addParameterFilterString("filterString", filterString.trim());
        }
        if (unit != null) {
            entityQuery.addWhereClause("entity.instrumentReservationSetting.chargeTimeUnit = :unit");
            entityQuery.addParameter("unit", unit);
        }
        entityQuery.addInEntitiesClause(included);
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<InstrumentReservation>) entityQuery.getResultList();
    }

    public InstrumentReservation getInstrumentReservationByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        List<InstrumentReservation> instrumentReservations = createNamedQuery("InstrumentReservation.findByIntervalAndInstrument").setParameter("instrument", instrument)
            .setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).setMaxResults(1).getResultList();
        return instrumentReservations.isEmpty() ? null : instrumentReservations.get(0);
    }

    public List<InstrumentReservation> getInstrumentReservationsToBeReminded() {
        return createNamedQuery("InstrumentReservation.findAllStartingSoon").getResultList();
    }

    public InstrumentReservation getLatestInstrumentReservationBySetting(InstrumentReservationSetting instrumentReservationSetting) {
        List<InstrumentReservation> instrumentReservations = createNamedQuery("InstrumentReservation.findBySettingOrderedByEndDateDesc")
            .setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId()).setMaxResults(1).getResultList();
        return instrumentReservations.isEmpty() ? null : instrumentReservations.get(0);
    }

    public BfabricLazyDataModel<InstrumentReservation> getLazyModelByTechnology(Technology technology) {
        return (BfabricLazyDataModel<InstrumentReservation>) getLazyModelUnnestById("instrument.technologies", technology.getId());
    }

    @Override
    public BfabricLazyDataModel<InstrumentReservation> getLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.user.id = :userId or entity.booker.id = :userId");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Long getMaxDurationBySetting(InstrumentReservationSetting instrumentReservationSetting) {
        return (Long) createNamedQuery("InstrumentReservation.findMaxDurationBySetting").setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId()).getSingleResult();
    }

    public Long getMinDurationBySetting(InstrumentReservationSetting instrumentReservationSetting) {
        return (Long) createNamedQuery("InstrumentReservation.findMinDurationBySetting").setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId()).getSingleResult();
    }

    public List<InstrumentReservation> getOnLoanEventsByIntervalAndInstrument(LocalDateTimeInterval interval, Instrument instrument) {
        return createNamedQuery("InstrumentReservation.findOnLoanByIntervalAndInstrument").setParameter("instrument", instrument).setParameter("startDate", interval.getStart())
            .setParameter("endDate", interval.getEnd()).getResultList();
    }

    public boolean isSendApprovalMail(InstrumentReservation instrumentReservation) {
        if (instrumentReservation != null &&
            instrumentReservation.getInstrumentReservationSetting() != null &&
            instrumentReservation.getInstrumentReservationSetting().isApprovalRequired() &&
            instrumentReservation.getApproved() != null) {
            InstrumentReservation oldInstrumentReservation = entityService.find(InstrumentReservation.class, instrumentReservation.getId());
            return oldInstrumentReservation != null && !oldInstrumentReservation.getApproved().equals(instrumentReservation.getApproved());
        }
        return false;
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        InstrumentReservation reservation = (InstrumentReservation) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        if (reservation != null) {
            InstrumentReservationSetting setting = reservation.getInstrumentReservationSetting();
            long durationComputed = reservation.getDurationComputed();

            if (setting != null) {
                // Check only editable container has been altered by verifying presence of canceled/published/closed container in the difference of old and new container
                if (reservation.getOldContainers() != null && reservation.getContainers() != null && Sets.difference(reservation.getOldContainers(), reservation.getContainers()).stream()
                    .anyMatch(container -> container == null || container.isPublished() || container.isCanceled() || container.isClosed())) {
                    validationErrorMsg.put(Constants.EDIT + ":containersautocomplete", Messages.get("containersHint"));
                }

                // API-Check whether the reservation start time respects the setting.
                if (!setting.getStartTimes().contains(reservation.getStartTime())) {
                    validationErrorMsg.put(Constants.EDIT + ":startTime", Messages.get("startTimeHint").replace("{0}", setting.getValidStartTimes()));
                }

                // API-Check whether the reservation end time respects the setting.
                if (!setting.getEndTimes().contains(reservation.getEndTime())) {
                    validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("endTimeHint").replace("{0}", setting.getValidEndTimes()));
                }

                // Check whether the reservation start date is within associated settings' validity datetime interval.
                if (!setting.getValidLocalDateTimeInterval().contains(reservation.getStartDate())) {
                    validationErrorMsg.put(Constants.EDIT + ":startDate", Messages.get("dateMustBeInIntervalHint").replace("{0}", setting.getValidIntervalAsString()));
                }
                // Check whether the end start date is within associated settings' validity datetime interval.
                if (!setting.getValidLocalDateTimeInterval().contains(reservation.getEndDate())) {
                    validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("dateMustBeInIntervalHint").replace("{0}", setting.getValidIntervalAsString()));
                }

                // Check whether the duration of the reservation is within min/max duration range.
                if (reservation.getInstrumentReservationType().isContainerAssociated() && setting.getMinDuration() != null && setting.getMinDuration().toMinutes() > durationComputed) {
                    validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("minDurationCheckFailedHint").replace("{0}", String.valueOf(durationComputed))
                        .replace("{1}", String.valueOf(setting.getMinDuration().toMinutes())) + " start=" + reservation.getStartDate());
                }
                if (reservation.getInstrumentReservationType().isContainerAssociated() && setting.getMaxDuration() != null && setting.getMaxDuration().toMinutes() < durationComputed) {
                    validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("maxDurationCheckFailedHint").replace("{0}", String.valueOf(durationComputed))
                        .replace("{1}", String.valueOf(setting.getMaxDuration().toMinutes())));
                }

                // Check whether the reservation start/end time within the slot min/max time interval of the corresponding setting.
                if (setting.getSlotMinTime() != null) {
                    if (reservation.getStartDate().isBefore(reservation.getStartDate().toLocalDate().atStartOfDay().plus(setting.getSlotMinTime()))) {
                        validationErrorMsg.put(Constants.EDIT + ":startDate", Messages.get("beforeTimeHint") + " " + setting.getSlotMinTimeFormat());
                    }
                    if (reservation.getEndDate().toLocalTime().isAfter(LocalTime.MIDNIGHT) && reservation.getEndDate()
                        .isBefore(reservation.getEndDate().toLocalDate().atStartOfDay().plus(setting.getSlotMinTime()))) {
                        validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("beforeTimeHint") + " " + setting.getSlotMinTimeFormat());
                    }
                }
                if (setting.getSlotMaxTime() != null) {
                    if (reservation.getStartDate().isAfter(reservation.getStartDate().toLocalDate().atStartOfDay().plus(setting.getSlotMaxTime()))) {
                        validationErrorMsg.put(Constants.EDIT + ":startDate", Messages.get("afterTimeHint") + " " + setting.getSlotMaxTimeFormat());
                    }
                    if (reservation.getEndDate().isAfter(reservation.getEndDate().toLocalDate().atStartOfDay().plus(setting.getSlotMaxTime()))) {
                        validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("afterTimeHint") + " " + setting.getSlotMaxTimeFormat());
                    }
                }

                // Check that start date and end date are not equal.
                if (reservation.getStartDate().isEqual(reservation.getEndDate())) {
                    validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("endDateEqualStartDateHint"));
                }

                // Check that end date is after start date.
                if (reservation.getStartDate().isAfter(reservation.getEndDate())) {
                    validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("endDateBeforeStartDateHint"));
                }

                // Check whether the start and end date of the reservation belong to the same year.
                if (reservation.getStartDate().getYear() != reservation.getEndDate().getYear()) {
                    validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("yearCrossingDatesErrorHint"));
                }

                // API-Check whether the reservation end time respects the setting.
                if (reservation.getRepeaterHelper().isRepeat() && !reservation.isRepeatable()) {
                    validationErrorMsg.put(Constants.EDIT + ":repeating", Messages.get("repeatingHint").replace("{0}", reservation.getInstrumentReservationSetting().getValidUntilAsString()));
                }

                // Check repeating week number is in the correct range
                if (reservation.getRepeaterHelper().isRepeat() && reservation.isRepeatable() && reservation.getRepeaterHelper().getWeeks() < reservation.getDurationAsWeeks()) {
                    validationErrorMsg.put(Constants.EDIT + ":weeks", Messages.get("repeatingWeeksHint").replace("{0}", String.valueOf(reservation.getDurationAsWeeks())));
                }

                // Check whether reservation collides.
                List<AbstractEvent> collidingEvents = getCollidingEventsByEvent(reservation);
                if (!collidingEvents.isEmpty()) {
                    final AbstractEvent collidingEvent = collidingEvents.get(0);
                    validationErrorMsg.put(Constants.EDIT + ":startDate", Messages.get("eventCollisionHint") + " " + collidingEvent.getName());
                }

                if (reservation.getRepeaterHelper().isRepeat() && reservation.isRepeatable()) {
                    for (AbstractEvent repeaterEvent : reservation.getRepeaterEvents()) {
                        collidingEvents = getCollidingEventsByEvent(repeaterEvent);
                        if (!collidingEvents.isEmpty()) {
                            final AbstractEvent collidingEvent = collidingEvents.get(0);
                            validationErrorMsg.put(Constants.EDIT + ":repeatEndDate", Messages.get("eventCollisionHint") + " " + collidingEvent.getName());
                            break;
                        }
                    }
                }
            }
        }
        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> saveAndSendMail(InstrumentReservation instrumentReservation, boolean sendMail, MailTypeEnum mailTypeEnum, User currentUser) {
        if (instrumentReservation.getInstrumentReservationSetting().getReminderDays() > 0) {
            instrumentReservation.setReminderDate(instrumentReservation.getEndDate().toLocalDate().minusDays(instrumentReservation.getInstrumentReservationSetting().getReminderDays()));
        }
        if (instrumentReservation.getInstrumentReservationType() == null) {
            instrumentReservation.setInstrumentReservationType(findByName(InstrumentReservationType.class, "Usage"));
        }
        LinkedHashMap<String, String> validationErrorMsg = save(instrumentReservation);
        if (validationErrorMsg.isEmpty() && sendMail) {
            if (mailTypeEnum.equals(MailTypeEnum.INSTRUMENT_RESERVATION)) {
                sendMail(instrumentReservation, mailTypeEnum, currentUser);
            }
            if (isSendApprovalMail(instrumentReservation)) {
                sendMail(instrumentReservation, MailTypeEnum.INSTRUMENT_RESERVATION_APPROVAL, currentUser);
            }
        }
        return validationErrorMsg;
    }

    public void sendMail(InstrumentReservation instrumentReservation, MailTypeEnum mailTypeEnum, User currentUser) {
        final Mail mail = new Mail();
        mail.setParent(instrumentReservation);
        mail.setType(mailTypeEnum, Constants.EMPTY_STRING, instrumentReservation.getEventTitle());
        if (MailTypeEnum.INSTRUMENT_RESERVATION.equals(mailTypeEnum) || MailTypeEnum.INSTRUMENT_RESERVATION_APPROVAL.equals(mailTypeEnum)) {
            if (instrumentReservation.getInstrumentReservationSetting().isNotifyCoach() && instrumentReservation.getContainers() != null && !instrumentReservation.getContainers().isEmpty()) {
                for (Container container : instrumentReservation.getContainers()) {
                    mail.addRecipient(container.getCoach());
                    mail.addRecipient(container.getCoachBackup());
                    if (container.getProject() != null) {
                        mail.addRecipient(container.getProject().getCoach());
                        mail.addRecipient(container.getProject().getCoachBackup());
                    }
                }
            }
            if (instrumentReservation.getInstrumentReservationSetting().isNotifyInstrumentSupervisor()) {
                mail.addRecipient(instrumentReservation.getInstrument().getSupervisor());
            }

            mail.addRecipient(instrumentReservation.getBooker());
            mail.addRecipient(instrumentReservation.getUser());

            if (MailTypeEnum.INSTRUMENT_RESERVATION.equals(mailTypeEnum)) {
                mail.addRecipient(currentUser);
                if (instrumentReservation.getDescription() != null && !instrumentReservation.getDescription().isEmpty()) {
                    mail.setMessage(instrumentReservation.getDescription());
                }
            }
        }
        mail.setInput("instrumentReservation", instrumentReservation);

        mailSendService.send(mail);
    }

    public void shiftInstrumentReservations(List<InstrumentReservation> instrumentReservationsToShift, long numbersOfDaysToShift) {
        if (instrumentReservationsToShift != null && !instrumentReservationsToShift.isEmpty()) {
            for (final InstrumentReservation instrumentReservationToShift : instrumentReservationsToShift) {
                instrumentReservationToShift.setStartDate(instrumentReservationToShift.getStartDate().plusDays(numbersOfDaysToShift));
                instrumentReservationToShift.setEndDate(instrumentReservationToShift.getEndDate().plusDays(numbersOfDaysToShift));
                LocalDateTimeInterval instrumentReservationSettingInterval = instrumentReservationToShift.getInstrumentReservationSetting().getValidLocalDateTimeInterval();
                if (instrumentReservationSettingInterval.contains(new LocalDateTimeInterval(instrumentReservationToShift.getStartDate(), instrumentReservationToShift.getEndDate()))) {
                    save(instrumentReservationToShift, true);
                }
            }
        }
    }

    public boolean validateShiftEventCollision(LocalDateTimeInterval interval, Instrument instrument, List<InstrumentReservation> excludeInstrumentReservations) {
        return getEventsByIntervalAndInstrumentExcludingEvents(interval, instrument, excludeInstrumentReservations).isEmpty();
    }
}
