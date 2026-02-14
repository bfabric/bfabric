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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Container;
import org.bfabric.entity.Project;
import org.bfabric.entity.StandardContainerStatus;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.TimelineEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.ContainerService;
import org.bfabric.util.DateUtils;
import org.primefaces.model.timeline.TimelineEvent;
import org.primefaces.model.timeline.TimelineModel;

@Named
@ViewScoped
public class ContainerTimelineManager extends AbstractEntityManager<Container> {

    private static final long serialVersionUID = 1;

    protected final HashMap<String, LocalDateTime[]> minMaxDate = new HashMap<>();

    protected final HashMap<TimelineEnum, List<TimelineEvent<Object>>> allTimelineEvents = new HashMap<>();

    // Timeline events which are displayed by default, i.e., they cannot be de-/selected by the user
    protected List<TimelineEnum> defaultTimelineEvents = null;

    protected List<TimelineEnum> selectableTimelineEvents = null;

    protected List<TimelineEnum> selectedTimelines = new ArrayList<>();

    @Inject
    private ContainerService containerService;

    public ContainerTimelineManager() {
        super(Container.class);
    }

    public Container getContainer() {
        return getInstance();
    }

    public String getDateAsFormattedString(LocalDateTime date) {
        return DateUtils.getDateAsFormattedStringWithoutTime(date);
    }

    @CachedMethodResult
    public List<TimelineEnum> getDefaultTimelineEvents() {
        if (defaultTimelineEvents == null) {
            defaultTimelineEvents = new ArrayList<>();
            defaultTimelineEvents.add(TimelineEnum.STATES);
        }
        return defaultTimelineEvents;
    }

    public LocalDateTime getMaxDate() {
        return getMinMaxDate().get(Constants.TIMELINE_INTERVAL)[1];
    }

    public LocalDateTime getMinDate() {
        return getMinMaxDate().get(Constants.TIMELINE_INTERVAL)[0];
    }

    @CachedMethodResult
    public HashMap<String, LocalDateTime[]> getMinMaxDate() {
        return minMaxDate;
    }

    @CachedMethodResult
    public List<TimelineEnum> getSelectableTimelineEvents() {
        if (selectableTimelineEvents == null) {
            selectableTimelineEvents = new ArrayList<>();
            Configuration configuration = getContainer().getConfiguration();
            selectableTimelineEvents.add(TimelineEnum.STATES);
            if (!getContainer().getMembers().isEmpty()) {
                selectableTimelineEvents.add(TimelineEnum.MEMBERS);
            }
            if (!getContainer().getCommentsCurrentUser().isEmpty()) {
                selectableTimelineEvents.add(TimelineEnum.COMMENTS);
            }
            if (!getContainer().getResultsCurrentUser().isEmpty()) {
                selectableTimelineEvents.add(TimelineEnum.RESULTS);
            }
            if (!getContainer().getNotesCurrentUser().isEmpty()) {
                selectableTimelineEvents.add(TimelineEnum.NOTES);
            }
            if (configuration.isDataManagementEnabled()) {
                if (!getContainer().getSamples().isEmpty()) {
                    selectableTimelineEvents.add(TimelineEnum.SAMPLES);
                }
                if (!getContainer().getWorkunits().isEmpty()) {
                    selectableTimelineEvents.add(TimelineEnum.WORKUNITS);
                }
                if (!getContainer().getResources().isEmpty()) {
                    selectableTimelineEvents.add(TimelineEnum.RESOURCES);
                }
                if (!getContainer().getDatasets().isEmpty()) {
                    selectableTimelineEvents.add(TimelineEnum.DATASETS);
                }
            }
            if (!getContainer().isRenderedCharges()) {
                selectableTimelineEvents.add(TimelineEnum.CHARGES);
            }
            if (!getContainer().isRenderedBookings()) {
                selectableTimelineEvents.add(TimelineEnum.BOOKINGS);
            }
            if (configuration.isFeedbackEnabled() && getContainer().isRenderedFeedbacks()) {
                selectableTimelineEvents.add(TimelineEnum.FEEDBACKS);
            }
            if (configuration.isWorkflowEnabled() && !getContainer().getWorkflows().isEmpty()) {
                selectableTimelineEvents.add(TimelineEnum.WORKFLOWS);
            }
            if (configuration.isInstrumentReservationEnabled() && !getContainer().getInstrumentReservations().isEmpty()) {
                selectableTimelineEvents.add(TimelineEnum.INSTRUMENT_RESERVATIONS);
            }

            if (getContainer() instanceof Project) {
                Project project = (Project) getContainer();
                if (project.isAcceptedButNotPublished()) {
                    if (!project.getOrderOffersIncluded().isEmpty()) {
                        selectableTimelineEvents.add(TimelineEnum.OFFERS);
                    }
                    if (project.isRenderedOrders()) {
                        selectableTimelineEvents.add(TimelineEnum.ORDERS);
                    }
                }
            }

            selectableTimelineEvents.removeAll(getDefaultTimelineEvents());
        }
        return selectableTimelineEvents;
    }

    public long getSelectedTimelineEventsCounter() {
        long selectedTimelineEventsCounter = 0;

        for (TimelineEnum timelineEnum : getSelectedTimelines()) {
            Long result = getTimelineEventsCount(timelineEnum);
            selectedTimelineEventsCounter += result != null ? result : 0;
        }

        return selectedTimelineEventsCounter;
    }

    public List<TimelineEnum> getSelectedTimelines() {
        return selectedTimelines;
    }

    @CachedMethodResult
    public Long getTimelineEventsCount(TimelineEnum timelineEnum) {
        Long result = null;
        switch (timelineEnum) {
        case STATES:
            result = (long) getContainer().getStates().size();
            break;
        case MEMBERS:
            result = (long) getContainer().getMembers().size();
            break;
        case SAMPLES:
        case WORKUNITS:
        case RESOURCES:
        case DATASETS:
        case CHARGES:
        case BOOKINGS:
        case FEEDBACKS:
        case WORKFLOWS:
            result = containerService.getTimelineEventsCountByContainerId(getIdLong(), timelineEnum);
            break;
        case ORDERS:
            if (getContainer() instanceof Project) {
                result = (long) getContainer().getOrders().size();
            }
            break;
        case INSTRUMENT_RESERVATIONS:
            result = (long) getContainer().getInstrumentReservations().size();
            break;
        case OFFERS:
        case COMMENTS:
        case NOTES:
        case RESULTS:
            switch (timelineEnum) {
            case OFFERS:
                if (getContainer() instanceof Project) {
                    result = (long) ((Project) getContainer()).getOrderOffersIncluded().size();
                }
                break;
            case COMMENTS:
                result = (long) getContainer().getCommentsCurrentUser().size();
                break;
            case NOTES:
                result = (long) getContainer().getNotesCurrentUser().size();
                break;
            case RESULTS:
                result = (long) getContainer().getResultsCurrentUser().size();
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        return result;
    }

    @Produces
    @Named("timelineModel")
    public TimelineModel<Object, TimelineEnum> getTimelineModel() {
        final TimelineModel<Object, TimelineEnum> timelineModel = new TimelineModel<>();

        if (selectedTimelines.isEmpty()) {
            selectedTimelines.addAll(getDefaultTimelineEvents());
        } else {
            for (final TimelineEnum t : getDefaultTimelineEvents()) {
                if (!selectedTimelines.contains(t)) {
                    selectedTimelines.add(t);
                }
            }
        }

        if (!selectedTimelines.isEmpty()) {
            LocalDateTime finalMinDate = null;
            LocalDateTime finalMaxDate = null;

            for (final TimelineEnum selected : selectedTimelines) {
                final String label = selected.getLabel();
                if (!allTimelineEvents.containsKey(selected) && !minMaxDate.containsKey(label)) {
                    // Timeline events for this selection have not yet been created
                    List<Object[]> result;
                    LocalDateTime minDate = null;
                    LocalDateTime maxDate = null;

                    switch (selected) {
                    case STATES:
                        final List<StandardContainerStatus> states = getContainer().getStates();
                        if (!states.isEmpty()) {
                            final List<TimelineEvent<Object>> newTimelineEvents = new ArrayList<>();

                            for (final StandardContainerStatus status : states) {
                                final LocalDateTime created = status.getCreated();
                                if (created != null) {
                                    if (minDate == null || created.isBefore(minDate)) {
                                        minDate = created;
                                    }
                                    if (maxDate == null || created.isAfter(maxDate)) {
                                        maxDate = created;
                                    }
                                    newTimelineEvents.add(TimelineEvent.builder().data(status).startDate(created).build());
                                }
                            }

                            allTimelineEvents.put(selected, newTimelineEvents);
                        }
                        break;
                    case MEMBERS:
                        result = containerService.getTimelineMemberEventsByContainerId(getIdLong());
                        if (result != null && !result.isEmpty()) {
                            final List<TimelineEvent<Object>> newTimelineEvents = new ArrayList<>();

                            for (final Object[] o : result) {
                                if (o.length == 3 && o[0] != null) {
                                    if (MailTypeEnum.MEMBER_ADD.equals(o[2])) {
                                        o[2] = selected.getShortCut() + "+";
                                    } else if (MailTypeEnum.MEMBER_REMOVE.equals(o[2])) {
                                        o[2] = selected.getShortCut() + "-";
                                    }
                                    Object[] entry = { o[0], o[1], o[2], selected.getTab() };
                                    newTimelineEvents.add(TimelineEvent.builder().data(entry).startDate((LocalDateTime) o[0]).build());
                                }
                            }

                            allTimelineEvents.put(selected, newTimelineEvents);
                            minDate = (LocalDateTime) result.get(0)[0];
                            maxDate = (LocalDateTime) result.get(result.size() - 1)[0];
                        }
                        break;
                    case SAMPLES:
                    case WORKUNITS:
                    case RESOURCES:
                    case DATASETS:
                    case CHARGES:
                    case BOOKINGS:
                    case FEEDBACKS:
                    case ORDERS:
                    case WORKFLOWS:
                        if (TimelineEnum.ORDERS.equals(selected)) {
                            result = containerService.getTimelineOrderEventsByProjectId(getIdLong());
                        } else {
                            result = containerService.getTimelineEventsByContainerId(getIdLong(), selected);
                        }

                        if (result != null && !result.isEmpty()) {
                            final List<TimelineEvent<Object>> newTimelineEvents = new ArrayList<>();

                            for (final Object[] o : result) {
                                if (o.length == 2 && o[0] != null) {
                                    Object[] entry = { o[0], o[1], selected.getShortCut(), selected.getTab() };
                                    newTimelineEvents.add(TimelineEvent.builder().data(entry).startDate((LocalDateTime) o[0]).build());
                                }
                            }

                            allTimelineEvents.put(selected, newTimelineEvents);
                            minDate = (LocalDateTime) result.get(0)[0];
                            maxDate = (LocalDateTime) result.get(result.size() - 1)[0];
                        }
                        break;
                    case OFFERS:
                    case COMMENTS:
                    case INSTRUMENT_RESERVATIONS:
                    case NOTES:
                    case RESULTS:
                        List<? extends AbstractBaseEntity> results = null;
                        switch (selected) {
                        case OFFERS:
                            if (getContainer() instanceof Project) {
                                Project project = (Project) getContainer();
                                results = project.getOrderOffersIncluded();
                            }
                            break;
                        case COMMENTS:
                            results = getContainer().getCommentsCurrentUser();
                            break;
                        case INSTRUMENT_RESERVATIONS:
                            results = new ArrayList<>(getContainer().getInstrumentReservations());
                            break;
                        case NOTES:
                            results = getContainer().getNotesCurrentUser();
                            break;
                        case RESULTS:
                            results = getContainer().getResultsCurrentUser();
                            break;
                        default:
                            break;
                        }

                        if (results != null && !results.isEmpty()) {
                            // Entities of the selected type exist
                            final List<TimelineEvent<Object>> newTimelineEvents = new ArrayList<>();
                            final HashMap<LocalDateTime, Integer> dateCount = new HashMap<>();

                            for (final AbstractBaseEntity entity : results) {
                                if (entity.getCreated() != null) {
                                    final LocalDateTime commentDate = entity.getCreated().toLocalDate().atStartOfDay();
                                    if (dateCount.containsKey(commentDate)) {
                                        dateCount.put(commentDate, dateCount.get(commentDate) + 1);
                                    } else {
                                        dateCount.put(commentDate, 1);
                                    }
                                    if (minDate == null || commentDate.isBefore(minDate)) {
                                        minDate = commentDate;
                                    }
                                    if (maxDate == null || commentDate.isAfter(maxDate)) {
                                        maxDate = commentDate;
                                    }
                                }
                            }

                            for (final Entry<LocalDateTime, Integer> entry : dateCount.entrySet()) {
                                final Object[] o = { entry.getKey(), entry.getValue(), selected.getShortCut(), selected.getTab() };
                                newTimelineEvents.add(TimelineEvent.builder().data(o).startDate(entry.getKey()).build());
                            }

                            allTimelineEvents.put(selected, newTimelineEvents);
                        }
                        break;
                    default:
                        break;
                    }

                    if (minDate == null || maxDate == null) {
                        if (minDate == null) {
                            minDate = getContainer().getCreated();
                        }
                        if (maxDate == null) {
                            maxDate = LocalDateTime.now();
                        }
                    } else {
                        // Expand the min and max values for visibility reasons
                        minDate = minDate.minusYears(1);
                        maxDate = maxDate.plusYears(1);
                    }

                    minMaxDate.put(label, new LocalDateTime[] { minDate, maxDate });
                }

                // Timeline events for this selection have already been created
                timelineModel.addAll(allTimelineEvents.get(selected));
                if (finalMinDate == null || finalMaxDate == null) {
                    // No min or max date set yet
                    finalMinDate = minMaxDate.get(label)[0];
                    finalMaxDate = minMaxDate.get(label)[1];
                } else {
                    // Min or max date already set
                    LocalDateTime tempMinDate = minMaxDate.get(label)[0];
                    LocalDateTime tempMaxDate = minMaxDate.get(label)[1];
                    if (tempMinDate.isBefore(finalMinDate)) {
                        finalMinDate = tempMinDate;
                    }
                    if (tempMaxDate.isAfter(finalMaxDate)) {
                        finalMaxDate = tempMaxDate;
                    }
                }

                minMaxDate.put(Constants.TIMELINE_INTERVAL, new LocalDateTime[] { finalMinDate, finalMaxDate });
            }
        }

        return timelineModel;
    }

    public boolean isRenderedDeSelectAll() {
        return getSelectedTimelines().size() == getDefaultTimelineEvents().size();
    }

    public boolean isRenderedSelectAll() {
        return !(getSelectedTimelines().size() - getDefaultTimelineEvents().size() < getSelectableTimelineEvents().size());
    }

    public boolean isTimeLineEventDisabled(TimelineEnum timelineEnum) {
        if (getSelectableTimelineEvents().contains(timelineEnum)) {
            Long result = getTimelineEventsCount(timelineEnum);
            int maxSelectedTimelineEventsCounter = 5000;
            return result == null || result > maxSelectedTimelineEventsCounter || getSelectedTimelineEventsCounter() + result > maxSelectedTimelineEventsCounter;
        }
        return true;
    }

    public void setSelectedTimelines(List<TimelineEnum> selectedTimelines) {
        this.selectedTimelines = selectedTimelines;
    }

    public void toggleAllTimelines(boolean selectAll) {
        if (selectAll) {
            selectedTimelines.clear();
            selectedTimelines.addAll(getSelectableTimelineEvents());
        } else {
            selectedTimelines.clear();
        }
    }
}
