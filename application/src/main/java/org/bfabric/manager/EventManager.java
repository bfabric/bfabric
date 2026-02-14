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

package org.bfabric.manager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Event;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.EventService;
import org.bfabric.util.DateUtils;
import org.bfabric.util.LocalDateTimeInterval;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleModel;

@MeasureCalls
@Named
@ViewScoped
public class EventManager extends AbstractEventManager<Event> {

    private static final long serialVersionUID = 1;

    @Inject
    private EventService eventService;

    private ScheduleModel lazyEventModel;

    public EventManager() {
        super(Event.class);
    }

    public void endDateChanged() {
        final LocalDateTime startDate = getEvent().getStartDate();
        final LocalDateTime endDate = getEvent().getEndDate();

        if (startDate != null && endDate != null) {
            if (!startDate.isBefore(endDate)) {
                getEvent().resetStartDate();
                getFacesMessagesManager().validationError("edit:startDate", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedStartDate"));
            } else {
                if (endDate.getYear() != startDate.getYear()) {
                    getEvent().resetStartDate();
                    getFacesMessagesManager().validationError("edit:startDate", Messages.get("adapted"));
                    getFacesMessagesManager().printWarn(Messages.get("noteAdaptedStartDateOverlappingHint"));
                }
            }
        }

        if (!getEvent().getRepeaterHelper().isValidEndDate(getEvent().getEndDate())) {
            long durationInWeeks = ChronoUnit.WEEKS.between(getEvent().getStartDateSlot(), getEvent().getEndDateSlot()) + 1;
            if (durationInWeeks > getEvent().getRepeaterHelper().getWeeks()) {
                getEvent().getRepeaterHelper().setWeeks((int) durationInWeeks);
                getFacesMessagesManager().validationError("edit:weeks", Messages.get("adapted"));
            }
            getEvent().getRepeaterHelper().setEndDate(getEvent().getRepeaterHelperValidUntilMinDate());
            if (getEvent().getRepeaterHelper().isRepeat()) {
                getFacesMessagesManager().validationError("edit:repeatEndDate", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedRepeatDateHint"));
            }
        }
    }

    @Produces
    @Named("event")
    public Event getEvent() {
        return getInstance();
    }

    public ScheduleModel getLazyEventModel() {
        if (lazyEventModel == null) {
            lazyEventModel = new LazyScheduleModel() {
                private static final long serialVersionUID = 1;

                @Override
                public void loadEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {
                    if (getInstance().getUser() != null) {
                        eventService.getEventsByIntervalAndUser(new LocalDateTimeInterval(startDateTime, endDateTime), getInstance().getUser(), view)
                            .forEach(event -> addEvent(event.getDefaultScheduleEvent()));
                    }
                }
            };
        }
        return lazyEventModel;
    }

    @Override
    public String getRedirectURLAfterRemove() {
        final HashMap<String, String> fParams = new HashMap<>();
        fParams.put("interval", DateUtils.getDateAsFormattedStringWithoutTime(getEvent().getStartDate()));
        return createRedirectURL("eventschedule/list", null, null, fParams);
    }

    @Override
    public String remove() {
        eventService.remove(getEvent());
        return getRedirectURLAfterRemove();
    }

    @Override
    public String removeRepeating(String series) {
        try {
            int result = eventService.removeRepeating(getEvent(), series);
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeletedRepeatingEvents").replace("{0}", Integer.toString(result)));
            return getRedirectURLAfterRemove();
        } catch (final Exception e) {
            e.printStackTrace();
            getFacesMessagesManager().printError(Messages.get("eventDeletionFailed"));
        }
        return null;
    }

    @Override
    public String save() {
        try {
            boolean created = !isManaged();
            if (created) {
                getEvent().setRepeaterEvents(getEvent().getRepeaterHelper());
            }

            LinkedHashMap<String, String> validationErrorMsg = eventService.saveAndSendMail(getEvent(), sendMail, MailTypeEnum.AGENDA_ABSENCE, getCurrentUser(), getUserGroup(), getRecipientsList());
            if (validationErrorMsg.isEmpty()) {
                if (created && !getEvent().getRepeaterEvents().isEmpty()) {
                    getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCreated") + " " + getEvent().getRepeaterEvents().size() + " " + Messages.get("repeatingEvents"));
                } else {
                    facesMessageAdd(created);
                }
                return getRedirectURLAfterSave();
            }
            handleValidationErrors(validationErrorMsg);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
        return null;
    }

    public void startDateChanged() {
        final LocalDateTime startDate = getEvent().getStartDate();
        final LocalDateTime endDate = getEvent().getEndDate();

        if (startDate != null && endDate != null) {
            if (!startDate.isBefore(endDate)) {
                getEvent().resetEndDate();
                getFacesMessagesManager().validationError("edit:endDate", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedEndDate"));
            } else {
                if (endDate.getYear() != startDate.getYear()) {
                    getEvent().resetEndDate();
                    getFacesMessagesManager().validationError("edit:endDate", Messages.get("adapted"));
                    getFacesMessagesManager().printWarn(Messages.get("noteAdaptedEndDateYearOverlapping"));
                }
            }
        }

        if (!getEvent().getRepeaterHelper().isValidEndDate(getEvent().getEndDate())) {
            getEvent().getRepeaterHelper().setEndDate(getEvent().getEndDate());
            if (getEvent().getRepeaterHelper().isRepeat()) {
                getFacesMessagesManager().validationError("edit:repeatEndDate", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedRepeatDateHint"));
            }
        }
    }
}