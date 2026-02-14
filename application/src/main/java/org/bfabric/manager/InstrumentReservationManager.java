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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEvent;
import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.entity.InstrumentReservationType;
import org.bfabric.entity.Run;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ContainerService;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.InstrumentReservationSettingService;
import org.bfabric.service.InstrumentService;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

@MeasureCalls
@Named
@ViewScoped
public class InstrumentReservationManager extends AbstractEventManager<InstrumentReservation> {

    private static final long serialVersionUID = 1;

    @Inject
    InstrumentReservationSettingService instrumentReservationSettingService;

    LocalDate initialDate;

    @Param
    private Long containerId;

    @Inject
    private ContainerService containerService;

    private Instrument instrument;

    @Param
    private Long instrumentId;

    @Inject
    private InstrumentReservationService instrumentReservationService;

    private InstrumentReservationSetting instrumentReservationSetting;

    private boolean instrumentReservationSettingChanged;

    @Param
    private Long instrumentReservationSettingId;

    @Inject
    private InstrumentService instrumentService;

    private ScheduleModel lazyEventModel;

    @Param
    private Long runId;

    @Param
    private String slot;

    private Technology technology;

    private String warningDisabledScheduleMessage;

    public InstrumentReservationManager() {
        super(InstrumentReservation.class);
    }

    public String approve() {
        getInstrumentReservation().approve();
        return saveApproval();
    }

    public String approveOrRejectInstrumentReservations(boolean approval, Set<InstrumentReservation> selectedInstrumentReservations) {
        if (!selectedInstrumentReservations.isEmpty()) {
            selectedInstrumentReservations.forEach(instrumentReservation -> {
                instrumentReservation.setApproved(approval);
                instrumentReservationService.saveAndSendMail(instrumentReservation, true, MailTypeEnum.INSTRUMENT_RESERVATION_APPROVAL, getCurrentUser());
            });
            getFacesMessagesManager().bufferWarningClear((approval ? Messages.get("approved") : Messages.get("rejected")) + " " + Messages.get("instrumentReservations"));
            return createRedirectURL("task/" + Constants.LIST, null, instrumentReservationService.getApprovalRequiredTasks(getCurrentUser())
                .getSize() > 0 ? "approvalrequiredinstrumentreservation" : null, null);
        }
        return null;
    }

    public void containersUnselect(UnselectEvent<Container> event) {
        Container container = event.getObject();
        if (!container.isInFinalState()) {
            getInstrumentReservation().getContainers().remove(container);
        } else {
            getInstrumentReservation().getContainers().add(container);
            getFacesMessagesManager().validationError("edit:containersautocomplete", Messages.get("containerNotRemovableHint"));
            getFacesMessagesManager().printWarn(Messages.get("containerNotRemovableHint"));
        }
    }

    @Override
    protected InstrumentReservation createInstance() {
        final InstrumentReservation instrumentReservation = super.createInstance();

        // set current as default user and booker for the instrumentReservation.
        User user = entityService.find(User.class, getCurrentUser().getId());
        instrumentReservation.setBooker(user);
        instrumentReservation.setUser(user);

        // Add container if the given container is not in a final state.
        Long containerId = getContainerId();
        if (containerId == null && getContextContainer() != null && getContextContainer().isInstrumentReservationEditable()) {
            containerId = getContextContainer().getId();
        }
        if (containerId != null) {
            Container container = entityService.find(Container.class, containerId);
            if (!container.isInFinalState()) {
                instrumentReservation.getContainers().add(container);
            }
        }

        if (getInstrumentId() != null) {
            instrumentReservation.setInstrument(entityService.find(Instrument.class, getInstrumentId()));
        }
        if (getRunId() != null) {
            Run run = entityService.find(Run.class, getRunId());
            if (run != null) {
                instrumentReservation.setRun(run);
                instrumentReservation.setContainers(new HashSet<>(run.getContainers()));
            }
        }
        if (instrumentReservation.getInstrumentReservationType() == null) {
            instrumentReservation.setInstrumentReservationType(entityService.findByName(InstrumentReservationType.class, "Usage"));
        }

        return instrumentReservation;
    }

    public void endDateChanged() {
        getInstrumentReservation().setStartDate();
        getInstrumentReservation().setEndDate();

        final LocalDateTime startDateTime = getInstrumentReservation().getStartDate();
        final LocalDateTime endDateTime = getInstrumentReservation().getEndDate();

        if (startDateTime != null && endDateTime != null) {
            if (!startDateTime.isBefore(endDateTime)) {
                getInstrumentReservation().resetStartDate();
                getFacesMessagesManager().validationError("edit:startDate", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedStartDate"));
            } else {
                if (endDateTime.getYear() != startDateTime.getYear()) {
                    getInstrumentReservation().resetStartDate();
                    getFacesMessagesManager().validationError("edit:startDate", Messages.get("adapted"));
                    getFacesMessagesManager().printWarn(Messages.get("noteAdaptedStartDateOverlappingHint"));
                }
            }
        }

        validateRepeaterHelper();
    }

    public boolean existsFutureCollidingEventByUser(User user) {
        return instrumentReservationService.existsFutureCollidingEventByUser(user, new LocalDateTimeInterval(getInstrumentReservation().getStartDateTime(), getInstrumentReservation()
            .getEndDateTime())) || instrumentReservationService
            .existsFutureCollidingEventByOperator(user, new LocalDateTimeInterval(getInstrumentReservation().getStartDateTime(), getInstrumentReservation().getEndDateTime()));
    }

    public List<Container> getBookableContainers(String filterString) {
        return containerService.getBookableContainers(filterString, getInstrumentReservation().getContainers(), identityManager.getCurrentUser());
    }

    public Long getContainerId() {
        return containerId;
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    @Produces
    @Named("instrumentReservation")
    public InstrumentReservation getInstrumentReservation() {
        return getInstance();
    }

    public InstrumentReservationSetting getInstrumentReservationSetting() {
        return instrumentReservationSetting;
    }

    public Long getInstrumentReservationSettingId() {
        return instrumentReservationSettingId;
    }

    public ScheduleModel getLazyEventModel() {
        if (lazyEventModel == null) {
            lazyEventModel = new LazyScheduleModel() {
                private static final long serialVersionUID = 1;

                @Override
                public void loadEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {
                    if (getInstrument() != null) {
                        instrumentReservationService.getEventsByIntervalAndInstrument(new LocalDateTimeInterval(startDateTime, endDateTime), getInstrument(), view)
                            .forEach(reservation -> addEvent(reservation.getDefaultScheduleEvent()));
                        updateInstrumentReservationSetting(startDateTime.toLocalDate());
                        LocalDateTime now = LocalDateTime.now();
                        if (getInstrumentReservationSetting().getBookingAheadMaxDuration() != null) {
                            LocalDateTime shiftedStartDate = now.plusMinutes(getInstrumentReservationSetting().getBookingAheadMaxDuration().toMinutes());
                            LocalTime startTime = getInstrumentReservationSetting().getClosestUpcomingSlot(shiftedStartDate.toLocalTime());
                            LocalDateTime bgStartDate = LocalDateTime.of(shiftedStartDate.toLocalDate(), startTime);
                            LocalDateTime bgEndDate = Optional.ofNullable(getInstrumentReservationSetting().getValidUntil()).map(LocalDate::atStartOfDay).orElse(LocalDateTime.of(2100, 1, 1, 0, 0));
                            addEvent(AbstractEvent.getDefaultBackgroundEvent(bgStartDate, bgEndDate, Messages.get("warningNotBookableSlot").replace("{0}", Messages.get("warningTooEarly"))));
                        }
                        if (getInstrumentReservationSetting().getBookingAheadMinDuration() != null) {
                            LocalDateTime bgStartDate = Optional.ofNullable(getInstrumentReservationSetting().getValidFrom()).map(LocalDate::atStartOfDay).orElse(LocalDateTime.of(1900, 1, 1, 0, 0));
                            LocalDateTime shiftedEndDate = now.minusMinutes(getInstrumentReservationSetting().getBookingAheadMinDuration().toMinutes());
                            LocalTime endTime = getInstrumentReservationSetting().getClosestPastSlot(shiftedEndDate.toLocalTime());
                            LocalDateTime bgEndDate = LocalDateTime.of(shiftedEndDate.toLocalDate(), endTime);
                            addEvent(AbstractEvent.getDefaultBackgroundEvent(bgStartDate, bgEndDate, Messages.get("warningNotBookableSlot").replace("{0}", Messages.get("warningTooLate"))));
                        }
                    }
                }
            };
        }
        return lazyEventModel;
    }

    @Override
    public String getListScreenRedirectURL() {
        return getViewScreenURL();
    }

    @Override
    public String getRedirectURLAfterRemove() {
        return getViewScreenURL(getInstrumentReservation());
    }

    public List<Instrument> getResultListBookableIncludingFiltered(String filterString) {
        return instrumentService.getResultListBookableIncludingFiltered(filterString, getInstrumentReservation().getInstrument());
    }

    public Run getRun() {
        return getRunId() != null ? entityService.find(Run.class, getRunId()) : null;
    }

    public Long getRunId() {
        return runId;
    }

    public String getScheduleURL() {
        return "/instrumentschedule/show.xhtml?instrumentId=" + getInstrumentReservation().getInstrument()
            .getId() + "&slot=" + Constants.DATE_FORMATTER.format(getInstrumentReservation().getStartDate());
    }

    @Override
    public String getSlot() {
        return slot;
    }

    public Technology getTechnology() {
        return technology;
    }

    public String getView() {
        return view;
    }

    public String getViewScreenURL(InstrumentReservation instrumentReservation, String... slots) {
        final HashMap<String, String> fParams = new HashMap<>();
        if (instrumentReservation != null) {
            if (instrumentReservation.getInstrument() != null) {
                fParams.put("instrumentId", instrumentReservation.getInstrument().getIdString());
            }
            fParams.put("slot", slots.length == 0 ? instrumentReservation.getSlot() : slots[0]);
        }
        return createRedirectShowScreenURL("instrumentschedule", null, null, fParams);
    }

    public String getViewScreenURL(String... slots) {
        return getViewScreenURL(getInstrumentReservation(), slots);
    }

    public String getWarningDisabledScheduleMessage() {
        return warningDisabledScheduleMessage;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getInstrumentReservation() != null) {
            setInstrumentReservationSetting(getInstrumentReservation().getInstrumentReservationSetting());
            getInstrumentReservation().setOldValues();
        }

        if (StringHelper.isNotEmpty(slot)) {
            try {
                setInitialDate(LocalDate.parse(slot, Constants.DATE_FORMATTER));
            } catch (Exception e) {
                setInitialDate(LocalDate.now());
            }
        }

        if (getInstrumentId() != null) {
            setInstrument(entityService.find(Instrument.class, instrumentId));
        }
    }

    public boolean isInstrumentReservationSettingChanged() {
        return instrumentReservationSettingChanged;
    }

    public boolean isInstrumentSelectionDisabled() {
        return getInstrumentId() != null;
    }

    public boolean isRenderedSendEmailCheckbox() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.INSTRUMENTMANAGER);
    }

    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        LocalDateTime selectedStartDate = selectEvent.getObject();
        setInstance(createInstance());
        getInstrumentReservation().setStartDate(selectedStartDate);
        getInstrumentReservation().setEndDateDefault();
        if (getInstrumentReservation().isCreatable(selectedStartDate)) {
            PrimeFaces.current().executeScript("PF('eventDialog').show();");
        } else {
            setWarningDisabledScheduleMessage(instrumentReservationService
                .getDisabledScheduleMessage(getInstrumentReservation().getStartDate(), getInstrumentReservationSetting().getBookingAheadMinDuration(), getInstrumentReservationSetting()
                    .getBookingAheadMaxDuration()));
            Ajax.update(Constants.EDIT + ":notBookableWarning");
            PrimeFaces.current().executeScript("PF('eventNotBookableDialog').show();");
        }
    }

    public void onEventSelect(SelectEvent<ScheduleEvent<InstrumentReservation>> event) {
        InstrumentReservation selectedInstrumentReservation = event.getObject().getData();
        if (selectedInstrumentReservation != null && selectedInstrumentReservation.isManaged()) {
            redirectRelative("/" + selectedInstrumentReservation.getShowScreenLink());
        }
    }

    public void refreshScheduleListener() {
        if (isInstrumentReservationSettingChanged()) {
            Ajax.update(Constants.EDIT + ":schedule");
        }
    }

    public String reject() {
        getInstrumentReservation().reject();
        return saveApproval();
    }

    @Override
    public String remove() {
        try {
            // Cache instrumentId for redirection.
            instrumentId = getInstrumentReservation().getInstrument().getId();
            instrumentReservationService.remove(getInstrumentReservation());
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted"));
            return getRedirectURLAfterRemove();
        } catch (final Exception e) {
            e.printStackTrace();
            getFacesMessagesManager().printError(Messages.get("eventDeletionFailed"));
        }
        return null;
    }

    @Override
    public String removeRepeating(String series) {
        try {
            // Cache instrumentId for redirection.
            instrumentId = getInstrumentReservation().getInstrument().getId();
            int result = instrumentReservationService.removeRepeating(getInstrumentReservation(), series);
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeletedRepeatingEvents").replace("{0}", Integer.toString(result)));
            return getViewScreenURL();
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
            if (getInstrumentReservation().getInstrumentReservationType() != null && !getInstrumentReservation().getInstrumentReservationType().isContainerAssociated()) {
                getInstrumentReservation().resetContainer();
            }
            if (created) {
                getInstrumentReservation().setRepeaterEvents(getInstrumentReservation().getRepeaterHelper());
            }
            LinkedHashMap<String, String> validationErrorMsg = instrumentReservationService
                .saveAndSendMail(getInstrumentReservation(), getInstrumentReservation().isSendMailNotification(), MailTypeEnum.INSTRUMENT_RESERVATION, getCurrentUser());
            if (validationErrorMsg.isEmpty()) {
                if (created && !getInstrumentReservation().getRepeaterEvents().isEmpty()) {
                    getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCreated") + " " + getInstrumentReservation().getRepeaterEvents().size() + " " + Messages.get(
                        "repeatingEvents"));
                } else {
                    facesMessageAdd(created);
                }
                return getShowScreenRedirectURL();
            }
            handleValidationErrors(validationErrorMsg);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
        return null;
    }

    public String saveApproval() {
        try {
            LinkedHashMap<String, String> validationErrorMsg = instrumentReservationService.saveAndSendMail(
                getInstrumentReservation(),
                true,
                MailTypeEnum.INSTRUMENT_RESERVATION_APPROVAL,
                getCurrentUser()
            );
            if (validationErrorMsg.isEmpty()) {
                return getShowScreenRedirectURL();
            }
            handleValidationErrors(validationErrorMsg);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
        return null;
    }

    public void setInitialDate(LocalDate initialDate) {
        this.initialDate = initialDate;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public void setInstrumentReservationSetting(InstrumentReservationSetting instrumentReservationSetting) {
        this.instrumentReservationSetting = instrumentReservationSetting;
    }

    public void setInstrumentReservationSettingChanged(boolean changed) {
        this.instrumentReservationSettingChanged = changed;
    }

    public void setInstrumentReservationSettingId(Long instrumentReservationSettingId) {
        this.instrumentReservationSettingId = instrumentReservationSettingId;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setWarningDisabledScheduleMessage(String warningDisabledScheduleMessage) {
        this.warningDisabledScheduleMessage = warningDisabledScheduleMessage;
    }

    public void startDateChanged() {
        getInstrumentReservation().setStartDate();
        getInstrumentReservation().setEndDate();
        final LocalDateTime startDateTime = getInstrumentReservation().getStartDate();
        final LocalDateTime endDateTime = getInstrumentReservation().getEndDate();

        if (startDateTime != null && endDateTime != null) {
            if (!startDateTime.isBefore(endDateTime)) {
                getInstrumentReservation().resetEndDate();
                getFacesMessagesManager().validationError("edit:endDate", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedEndDate"));
            } else {
                if (endDateTime.getYear() != startDateTime.getYear()) {
                    getInstrumentReservation().resetEndDate();
                    getFacesMessagesManager().validationError("edit:endDate", Messages.get("adapted"));
                    getFacesMessagesManager().printWarn(Messages.get("noteAdaptedEndDateYearOverlapping"));
                }
            }
        }

        validateRepeaterHelper();
    }

    public void updateInstrumentReservationSetting(LocalDate startDate) {
        if (startDate != null) {
            if (!startDate.equals(getInitialDate())) {
                setInitialDate(startDate);
            }
            if (getInstrumentReservationSetting() == null || !getInstrumentReservationSetting().isContained(startDate)) {
                setInstrumentReservationSetting(instrumentReservationSettingService.getSettingByInstrumentAndDate(getInstrument(), startDate));
                setInstrumentReservationSettingChanged(true);
            } else {
                setInstrumentReservationSettingChanged(false);
            }
        }
    }

    public void validateRepeaterHelper() {
        long durationInWeeks = getInstrumentReservation().getDurationAsWeeks() + 1;
        if (durationInWeeks != getInstrumentReservation().getRepeaterHelper().getWeeks()) {
            getInstrumentReservation().getRepeaterHelper().setWeeks((int) durationInWeeks);
            if (getInstrumentReservation().getRepeaterHelper().isRepeat()) {
                getFacesMessagesManager().validationError("edit:weeks", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedRepeatWeeksHint"));
            }
        }

        if (!getInstrumentReservation().getRepeaterHelper().isValidEndDate(getInstrumentReservation().getEndDate())) {
            getInstrumentReservation().getRepeaterHelper().setEndDate(getInstrumentReservation().getRepeaterHelperValidUntilMinDate());
            if (getInstrumentReservation().getRepeaterHelper().isRepeat()) {
                getFacesMessagesManager().validationError("edit:repeatEndDate", Messages.get("adapted"));
                getFacesMessagesManager().printWarn(Messages.get("noteAdaptedRepeatDateHint"));
            }
        }
    }
}
