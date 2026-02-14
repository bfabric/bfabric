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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEvent;
import org.bfabric.entity.Event;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.manager.ConfManager;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class EventService extends AbstractEventService {

    public static final String VIEW_EVENTS = "events";

    public static final String VIEW_INSTRUMENTRESERVATIONS = "instrumentreservations";

    private static final long serialVersionUID = 1;

    @Inject
    protected ConfManager confManager;

    @Inject
    private IdentityService identityService;

    public EventService() {
        super(Event.class);
    }

    @Override
    public List<AbstractEvent> getCollidingEventsByEvent(AbstractEvent abstractEvent) {
        Event event = (Event) abstractEvent;
        boolean publicEvent = event.getEventType() != null && event.getEventType().isPublicEvent();
        if (publicEvent) {
            return createNamedQuery("Event.findCollidingPublicEvents").setParameter("startDate", event.getStartDate()).setParameter("endDate", event.getEndDate()).setParameter("id", event.getId())
                .getResultList();
        }

        User user = event.getUser();
        if (user == null) {
            user = identityService.getCurrentUser();
        }
        return createNamedQuery("Event.findCollidingPrivateEvents").setParameter("startDate", event.getStartDate()).setParameter("endDate", event.getEndDate()).setParameter("id", event.getId())
            .setParameter("user", user)
            .getResultList();
    }

    public List<Event> getEventsByIntervalAndEvent(LocalDateTimeInterval interval, Event event) {
        return createNamedQuery("Event.findByIntervalAndEvent").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd())
            .setParameter("id", event != null ? event.getId() : 0L).getResultList();
    }

    public List<Event> getEventsByIntervalAndUser(LocalDateTimeInterval interval, User user) {
        return createNamedQuery("Event.findByIntervalAndUser").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).setParameter("user", user).getResultList();
    }

    public List<AbstractEvent> getEventsByIntervalAndUser(LocalDateTimeInterval interval, User user, String view) {
        List<AbstractEvent> abstractEvents = new ArrayList<>();
        if (view == null || VIEW_EVENTS.equals(view)) {
            List<Event> events = getEventsByIntervalAndUser(interval, user);
            if (!events.isEmpty()) {
                abstractEvents.addAll(events);
            }
        }
        if (view == null || VIEW_INSTRUMENTRESERVATIONS.equals(view)) {
            List<InstrumentReservation> instrumentReservations = getInstrumentReservationsByIntervalAndUser(interval, user);
            if (!instrumentReservations.isEmpty()) {
                abstractEvents.addAll(instrumentReservations);
            }
        }
        return abstractEvents;
    }

    public List<Event> getEventsByIntervalAndUsers(LocalDateTimeInterval interval, Collection<User> users) {
        if (users != null && !users.isEmpty()) {
            return createNamedQuery("Event.findByIntervalAndUsers").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).setParameter("users", users)
                .getResultList();
        }
        return createNamedQuery("Event.findByInterval").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<AbstractEvent> getFutureCollidingEventsByUser(User user, LocalDateTimeInterval interval) {
        return createNamedQuery("Event.findUpcomingUserEvent").setParameter("user", user).setParameter("startDate", interval.getStart())
            .setParameter("endDate", interval.getEnd()).getResultList();
    }

    public List<InstrumentReservation> getInstrumentReservationsByIntervalAndUser(LocalDateTimeInterval interval, User user) {
        return createNamedQuery("InstrumentReservation.findByIntervalAndUser").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd())
            .setParameter("userId", user.getId()).getResultList();
    }

    public List<Event> getOverlappingAccountedEventsByEvent(Event event) {
        return createNamedQuery("Event.findAccountableEventsByInterval").setParameter("startDate", event.getStartDate()).setParameter("endDate", event.getEndDate()).getResultList();
    }

    public List<Event> getPublicEvents() {
        return createNamedQuery("Event.findPublicEvents").getResultList();
    }

    public List<Event> getPublicEventsByInterval(LocalDateTimeInterval interval) {
        return createNamedQuery("Event.findPublicEventsByInterval").setParameter("startDate", interval.getStart()).setParameter("endDate", interval.getEnd()).getResultList();
    }

    public void remove(Event event) {
        final Set<Event> overlappingAccountedEvents = new HashSet<>();
        if (event.getEventType().isPublicEvent() && event.getEventType().isFreeEvent()) {
            // Collect all overlapping accounted events to recompute their accounted days
            overlappingAccountedEvents.addAll(getOverlappingAccountedEventsByEvent(event));
        }

        super.remove(event);

        for (final Event overlappingAccountedEvent : overlappingAccountedEvents) {
            overlappingAccountedEvent.setAccountedDays();
            update(overlappingAccountedEvent);
        }

        if (event.getUser() != null && event.isUserAvailableAffected()) {
            resetUserAvailableByUserId(event.getUser().getId(), true);
        }
    }

    public LinkedHashMap<String, String> save(Event event) {
        event.setAccountedDays();
        LinkedHashMap<String, String> validationErrorMsg = super.save(event);
        if (validationErrorMsg.isEmpty()) {
            // Recompute all accounted days of overlapping events if the managed event is free and public.
            Set<Event> events = new HashSet<>();
            if (event.getEventType().isPublicEvent() && event.getEventType().isFreeEvent()) {
                if (event.isRepeaterEventFirst()) {
                    events.addAll(event.getRepeaterEvents());
                } else {
                    events.add(event);
                }

                // Collect all overlapping accounted events to recompute their accounted days.
                final Set<Event> overlappingAccountedEvents = new HashSet<>();
                for (final Event overlappingAccountedEvent : events) {
                    overlappingAccountedEvents.addAll(getOverlappingAccountedEventsByEvent(overlappingAccountedEvent));
                }

                for (final Event overlappingAccountedEvent : overlappingAccountedEvents) {
                    overlappingAccountedEvent.setAccountedDays();
                    update(overlappingAccountedEvent);
                }
            } else if (event.getUser() != null) {
                if (event.getRepeaterEvents().isEmpty()) {
                    if (event.isUserAvailableAffected()) {
                        resetUserAvailableByUserId(event.getUser().getId(), !event.isUserNotAvailable());
                    }
                } else {
                    for (final Event repeaterEvent : event.getRepeaterEvents()) {
                        if (repeaterEvent.isUserAvailableAffected()) {
                            resetUserAvailableByUserId(repeaterEvent.getUser().getId(), false);
                            break;
                        }
                    }
                }
            }
        }
        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> saveAndSendMail(Event event, boolean sendMail, MailTypeEnum mailTypeEnum, User currentUser, UserGroup userGroup, List<User> recipientsList) throws Exception {
        LinkedHashMap<String, String> validationErrorMsg = save(event);
        if (validationErrorMsg.isEmpty() && sendMail) {
            sendMail(event, mailTypeEnum, currentUser, userGroup, recipientsList);
        }
        return validationErrorMsg;
    }

    public void sendMail(Event event, MailTypeEnum mailTypeEnum, User currentUser, UserGroup userGroup, List<User> recipientsList) throws AddressException {
        final Mail mail = new Mail();
        mail.setParent(event);
        mail.setType(mailTypeEnum, Constants.EMPTY_STRING, event.getFullEventInfo());

        if (MailTypeEnum.AGENDA_ABSENCE.equals(mailTypeEnum)) {
            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                mail.setMessage(event.getDescription());
            }

            if (StringHelper.isNotEmpty(getConfiguration().getAbsencesMailAddress())) {
                mail.getMailHelper().setTo(Collections.singletonList(new InternetAddress(getConfiguration().getAbsencesMailAddress())));
            }

            mail.setRecipient(currentUser);
            // Add selected recipients
            mail.addRecipients(recipientsList);

            // Add users from the mail group (duplicates are handled in the addRecipients method)
            if (userGroup != null) {
                mail.addRecipients(userGroup.getUsers());
            }
        }
        mail.setInput("event", event);

        mailSendService.send(mail);
    }
}