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

package org.bfabric.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.comparator.UserNameCaseInsensitiveComparator;
import org.bfabric.entity.Event;
import org.bfabric.entity.User;
import org.bfabric.service.IdentityService;

public class AgendaEventHelper extends AbstractAgendaHelper {

    private static final Logger logger = Logger.getLogger(AgendaEventHelper.class.getName());

    protected List<Event> events;

    public AgendaEventHelper() {
    }

    public BigDecimal getAccountableDays(Event event) {
        BigDecimal numberOfDays = BigDecimal.ZERO;

        if (event != null && event.getEventType().isAccountable()) {
            List<AgendaEventHelperSlot> userSlots = new ArrayList<>();
            LocalDateTimeInterval tmpDate = new LocalDateTimeInterval(interval.getStart(), interval.getStart().plusHours(5));

            try {
                // get public slots
                Iterator<AgendaEventHelperSlot> publicIter = getPublicSlots().iterator();

                // create slots in interval
                while (interval.contains(tmpDate)) {
                    try {
                        // create slot
                        AgendaEventHelperSlot agendaHelperSlot = new AgendaEventHelperSlot();
                        agendaHelperSlot.setDate(tmpDate.getStart());
                        agendaHelperSlot.setUser(event.getUser());
                        agendaHelperSlot.setEvent(event);

                        // Fetch the next public slot in the list and add as common event if necessary
                        AgendaEventHelperSlot tempSlot = publicIter.next();
                        if (tempSlot.getEvent() != null) {
                            agendaHelperSlot.setCommonEvent(tempSlot.getEvent());
                        }
                        userSlots.add(agendaHelperSlot);

                        // proceed to next slot
                        tmpDate = getNextSlot(tmpDate);
                    } catch (Exception e) {
                        logger.severe(e.toString());
                        break;
                    }
                }
            } catch (Exception e) {
                logger.severe(e.toString());
            }

            for (AgendaEventHelperSlot userSlot : userSlots) {
                if (userSlot.getEvent() != null && userSlot.getEvent().equals(event) && userSlot.isAccountable()) {
                    numberOfDays = numberOfDays.add(new BigDecimal("0.5"));
                }
            }
        }

        return numberOfDays;
    }

    @Override
    public List<Event> getEvents() {
        if (events == null) {
            events = new ArrayList<>();
        }
        return events;
    }

    public LocalDateTimeInterval getNextSlot(LocalDateTimeInterval currentSlot) {
        if (currentSlot.getStart().getHour() < 13) {
            return new LocalDateTimeInterval(currentSlot.getStart().plusHours(5), currentSlot.getEnd().plusHours(5));
        }
        return new LocalDateTimeInterval(currentSlot.getStart().plusDays(1).minusHours(5), currentSlot.getEnd().plusDays(1).minusHours(5));
    }

    public List<AgendaEventHelperSlot> getPublicSlots() {
        List<AgendaEventHelperSlot> publicSlots = new ArrayList<>();

        LocalDateTimeInterval tmpDate = new LocalDateTimeInterval(interval.getStart(), interval.getStart().plusHours(5));

        // create slots in interval
        while (interval.contains(tmpDate)) {
            try {
                // create slot
                AgendaEventHelperSlot agendaHelperSlot = new AgendaEventHelperSlot();
                agendaHelperSlot.setDate(tmpDate.getStart());
                for (Event event : getEvents()) {
                    if (event.getEventType().isPublicEvent()) {
                        LocalDateTimeInterval eventInterval = new LocalDateTimeInterval(event.getStartDate(), event.getEndDate());
                        if (eventInterval.contains(tmpDate)) {
                            agendaHelperSlot.setEvent(event);
                        }
                    }
                }
                publicSlots.add(agendaHelperSlot);

                // proceed to next slot
                tmpDate = getNextSlot(tmpDate);
            } catch (Exception e) {
                logger.severe(e.toString());
                break;
            }
        }

        return publicSlots;
    }

    public List<AgendaEventHelperSlot> getSlotsForUser(User user) {
        // Get Events for User
        List<Event> userEvents = new ArrayList<>();
        HashMap<User, List<Event>> map = getUserEventsMap();
        if (map.containsKey(user)) {
            userEvents = map.get(user);
        }

        List<AgendaEventHelperSlot> userSlots = new ArrayList<>();
        LocalDateTimeInterval tmpDate = new LocalDateTimeInterval(interval.getStart(), interval.getStart().plusHours(5));

        try {
            // get public slots
            Iterator<AgendaEventHelperSlot> publicIter = getPublicSlots().iterator();

            // create slots in interval
            while (interval.contains(tmpDate)) {
                try {
                    // create slot
                    AgendaEventHelperSlot agendaHelperSlot = new AgendaEventHelperSlot();
                    agendaHelperSlot.setDate(tmpDate.getStart());
                    agendaHelperSlot.setUser(user);
                    if (userEvents != null) {
                        for (Event event : userEvents) {
                            LocalDateTimeInterval eventInterval = new LocalDateTimeInterval(event.getStartDate(), event.getEndDate());
                            if (eventInterval.contains(tmpDate)) {
                                agendaHelperSlot.setEvent(event);
                            }
                        }
                    }
                    // Fetch the next public slot in the list and add as common event if necessary
                    AgendaEventHelperSlot tempSlot = publicIter.next();
                    if (tempSlot.getEvent() != null) {
                        agendaHelperSlot.setCommonEvent(tempSlot.getEvent());
                    }
                    userSlots.add(agendaHelperSlot);

                    // proceed to next slot
                    tmpDate = getNextSlot(tmpDate);
                } catch (Exception e) {
                    logger.severe(e.toString());
                    break;
                }
            }
        } catch (Exception e) {
            logger.severe(e.toString());
        }
        return userSlots;
    }

    private HashMap<User, List<Event>> getUserEventsMap() {
        HashMap<User, List<Event>> orderedEvents = new HashMap<>();
        for (Event event : getEvents()) {
            if (orderedEvents.containsKey(event.getUser())) {
                orderedEvents.get(event.getUser()).add(event);
            } else {
                List<Event> col = new ArrayList<>();
                col.add(event);
                orderedEvents.put(event.getUser(), col);
            }
        }
        return orderedEvents;
    }

    public List<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();

        // Force user to be included in the list if the user is an employee.
        User currentUser = CDI.current().select(IdentityService.class).get().getCurrentUser();
        if (currentUser != null && currentUser.hasRoleEmployee() && currentUser.isEmployee()) {
            users.add(currentUser);
        }

        // Get users having an event in the given date interval.
        if (hasEvents()) {
            for (Event event : getEvents()) {
                if (event.getUser() != null && !users.contains(event.getUser())) {
                    users.add(event.getUser());
                }
            }
            users.sort(new UserNameCaseInsensitiveComparator<>());
        }

        return users;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}