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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractEvent;
import org.bfabric.entity.Event;

public abstract class AbstractEventService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    public AbstractEventService() {
    }

    public AbstractEventService(Class<?> clazz) {
        super(clazz);
    }

    public abstract List<AbstractEvent> getCollidingEventsByEvent(AbstractEvent event);

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final AbstractEvent event = (AbstractEvent) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        // check that end date is after start date
        if (event.getStartDate().isAfter(event.getEndDate())) {
            validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("endDateBeforeStartDateHint"));
        }

        // check whether the start and end date of the event belong to the same year
        if (event.getStartDate().getYear() != event.getEndDate().getYear()) {
            validationErrorMsg.put(Constants.EDIT + ":endDate", Messages.get("yearCrossingDatesErrorHint"));
        }

        // check whether event collides
        List<AbstractEvent> collidingEvents = getCollidingEventsByEvent(event);
        if (!collidingEvents.isEmpty()) {
            final AbstractEvent collidingEvent = collidingEvents.get(0);
            validationErrorMsg.put(Constants.EDIT + ":startDate", Messages.get("eventCollisionHint") + " " + collidingEvent.getName());
        }

        for (AbstractEvent repeaterEvent : event.getRepeaterEvents()) {
            collidingEvents = getCollidingEventsByEvent(repeaterEvent);
            if (!collidingEvents.isEmpty()) {
                final AbstractEvent collidingEvent = collidingEvents.get(0);
                validationErrorMsg.put(Constants.EDIT + ":repeatEndDate", Messages.get("eventCollisionHint") + " " + collidingEvent.getName());
                break;
            }
        }

        return validationErrorMsg;
    }

    public int removeRepeating(AbstractEvent event, String series) {
        int ret = 1;
        if (event.isRepeaterEvent()) {
            TreeSet<AbstractEvent> removeEvents = new TreeSet<>();
            if (Constants.REMOVE_FOLLOWING.equals(series)) {
                removeEvents = new TreeSet<>(event.getRepeaterEventsFollowing());
            } else if (Constants.REMOVE_ALL.equals(series)) {
                removeEvents = new TreeSet<>(event.getRepeater().getRepeaterEvents());
            } else {
                removeEvents.add(event);
            }

            ret = removeEvents.size();

            final Iterator<AbstractEvent> eventIter = removeEvents.descendingIterator();
            while (eventIter.hasNext()) {
                final AbstractEvent removeEvent = eventIter.next();
                removeEvent.getRepeater().getRepeaterEvents().remove(removeEvent);
                remove(removeEvent);
                if (removeEvent instanceof Event) {
                    Event agendaEvent = (Event) removeEvent;
                    if (agendaEvent.getUser() != null && agendaEvent.isUserAvailableAffected()) {
                        resetUserAvailableByUserId(removeEvent.getUser().getId(), true);
                    }
                }
            }
        }
        return ret;
    }

    public void resetUserAvailableByUserId(Long userId, boolean available) {
        createNamedQuery("User.setAvailableByUserId").setParameter("userId", userId).setParameter("available", available).executeUpdate();
    }

    public LinkedHashMap<String, String> save(AbstractEvent event) {
        LinkedHashMap<String, String> validationErrorMsg = isValid(event);
        if (validationErrorMsg.isEmpty()) {
            if (event.getRepeaterEvents().isEmpty()) {
                event.resetFields();
            } else {
                for (AbstractEvent repeaterEvent : event.getRepeaterEvents()) {
                    repeaterEvent.resetFields();
                }
            }
            super.save(event);
        }
        return validationErrorMsg;
    }
}
