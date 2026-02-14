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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractEvent;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.InstrumentService;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class InstrumentReservationShiftManager extends InstrumentReservationManager {

    private static final long serialVersionUID = 1;

    @Inject
    private InstrumentReservationService instrumentReservationService;

    private List<InstrumentReservation> instrumentReservationsToShift;

    @Inject
    private InstrumentService instrumentService;

    private List<InstrumentReservation> potentialInstrumentReservationsToShift;

    private LocalDate shiftEndDate;

    @Param
    private String shiftInit;

    private LocalDate shiftStartDate;

    private LocalDate shiftToDate;

    private Instrument shiftedInstrument;

    public String cancelShiftInstrumentReservations() {
        return getRedirectURLFromRefererUrl(false);
    }

    public Optional<InstrumentReservation> getEarliestPotentialInstrumentReservationToShift() {
        return getPotentialInstrumentReservationsToShift().stream().findFirst();
    }

    public List<InstrumentReservation> getInstrumentReservationsToShift() {
        return instrumentReservationsToShift;
    }

    public List<Instrument> getInstruments(String filterString) {
        return instrumentService.getInstruments(filterString, false, getTechnology(), false);
    }

    public LocalDate getMaxShiftToDate() {
        Optional<InstrumentReservation> earliestInstrumentReservation = getEarliestPotentialInstrumentReservationToShift();
        Optional<Duration> maxShiftableDuration = getMaxShiftableDuration();
        if (earliestInstrumentReservation.isPresent() && maxShiftableDuration.isPresent()) {
            return earliestInstrumentReservation.get().getStartDate().toLocalDate().plusDays(maxShiftableDuration.get().toDays());
        }
        return getInstrumentReservationSetting() != null ? getInstrumentReservationSetting().getValidUntil() : null;
    }

    public Optional<Duration> getMaxShiftableDuration() {
        Optional<LocalDateTime> latestEndDate = Optional.ofNullable(getPotentialInstrumentReservationsToShift())
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(AbstractEvent::getEndDate)
            .max(LocalDateTime::compareTo);
        if (getInstrumentReservationSetting() != null && getInstrumentReservationSetting().getValidUntil() != null) {
            return latestEndDate.map(t -> Duration.between(t, getInstrumentReservationSetting().getValidUntil().atStartOfDay()));
        }
        return Optional.empty();
    }

    public List<InstrumentReservation> getPotentialInstrumentReservationsToShift() {
        return potentialInstrumentReservationsToShift;
    }

    public LocalDate getShiftEndDate() {
        return shiftEndDate;
    }

    public LocalDate getShiftStartDate() {
        return shiftStartDate;
    }

    public LocalDate getShiftToDate() {
        return shiftToDate;
    }

    public Instrument getShiftedInstrument() {
        return shiftedInstrument;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if ("true".equals(shiftInit)) {
            initShiftInstrumentReservations();
        }
    }

    public void initShiftInstrumentReservations() {
        if (getInstrumentReservationSettingId() != null) {
            InstrumentReservationSetting setting = entityService.find(InstrumentReservationSetting.class, getInstrumentReservationSettingId());
            if (setting != null) {
                setShiftedInstrument(setting.getInstrument());
                setInstrumentReservationSetting(setting);
            }
        } else if (getInstrumentId() != null) {
            setShiftedInstrument(entityService.find(Instrument.class, getInstrumentId()));
            setInstrumentReservationSetting(getShiftedInstrument().getLastReservationSetting());
        } else {
            setTechnology(getCurrentUser().getDefaultTechnology());
        }
        if (getInstrumentReservationSetting() != null) {
            setShiftStartDate(getInstrumentReservationSetting().getValidFrom());
            setShiftEndDate(getInstrumentReservationSetting().getValidUntil());
        }

        // Important: Load potential instrument reservations to shift BEFORE resetting shiftToDate!
        loadPotentialInstrumentReservationsToShift();
        resetShiftToDate();
    }

    private void loadPotentialInstrumentReservationsToShift() {
        setPotentialInstrumentReservationsToShift(instrumentReservationService
            .getEventsByIntervalAndInstrumentOrderByStartDate(new LocalDateTimeInterval(getShiftStartDate(), getShiftEndDate()), getShiftedInstrument()));
    }

    public void resetShiftToDate() {
        // If reservation's start date to be used as shiftToDate is available
        setShiftToDate(getEarliestPotentialInstrumentReservationToShift().map(reservation -> reservation.getStartDate().toLocalDate()).orElseGet(() -> {
                // compute shiftToDate
                if (getShiftStartDate() != null) {
                    return getShiftStartDate();
                }
                if (getInstrumentReservation().getInstrumentReservationSetting() != null && getInstrumentReservationSetting().getValidLocalDateInterval().contains(LocalDate.now())) {
                    return LocalDate.now();
                }
                return null;
            })
        );
    }

    private void resetShiftedDates() {
        if (getInstrumentReservationsToShift() != null) {
            for (final InstrumentReservation instrumentReservationToShift : getInstrumentReservationsToShift()) {
                instrumentReservationToShift.setShiftedStartDate(null);
                instrumentReservationToShift.setShiftedEndDate(null);
            }
        }

        if (getPotentialInstrumentReservationsToShift() != null) {
            for (final InstrumentReservation potentialInstrumentReservationToShift : getPotentialInstrumentReservationsToShift()) {
                potentialInstrumentReservationToShift.setShiftedStartDate(null);
                potentialInstrumentReservationToShift.setShiftedEndDate(null);
            }
        }
    }

    public void setInstrumentReservationsToShift(List<InstrumentReservation> instrumentReservationsToShift) {
        this.instrumentReservationsToShift = instrumentReservationsToShift;
    }

    public void setPotentialInstrumentReservationsToShift(List<InstrumentReservation> potentialInstrumentReservationsToShift) {
        this.potentialInstrumentReservationsToShift = potentialInstrumentReservationsToShift;
    }

    public void setShiftEndDate(LocalDate shiftEndDate) {
        this.shiftEndDate = shiftEndDate;
    }

    public void setShiftStartDate(LocalDate shiftStartDate) {
        this.shiftStartDate = shiftStartDate;
    }

    public void setShiftToDate(LocalDate shiftToDate) {
        this.shiftToDate = shiftToDate;
    }

    public void setShiftedInstrument(Instrument shiftedInstrument) {
        this.shiftedInstrument = shiftedInstrument;
    }

    public void shiftEndDateChanged() {
        resetShiftToDate();
        resetShiftedDates();

        if (getShiftStartDate() != null && getShiftEndDate() != null && getShiftEndDate().isBefore(getShiftStartDate())) {
            setShiftStartDate(getShiftEndDate());
            getFacesMessagesManager().validationError("shiftInstrumentReservationsForm:shiftStartDate", Messages.get("adapted"));
            getFacesMessagesManager().printWarn(Messages.get("noteAdaptedEndDate"));
        }

        loadPotentialInstrumentReservationsToShift();
    }

    public void shiftInstrumentReservationSettingChanged() {
        InstrumentReservationSetting selectedSetting = getInstrumentReservationSetting();
        setShiftStartDate(selectedSetting.getValidFrom());
        setShiftEndDate(selectedSetting.getValidUntil());
        resetShiftToDate();
        resetShiftedDates();
        loadPotentialInstrumentReservationsToShift();
    }

    public String shiftInstrumentReservations() {
        if (getInstrumentReservationsToShift().stream().findFirst().isPresent()) {
            getPotentialInstrumentReservationsToShift().removeAll(getInstrumentReservationsToShift());
            String message = Messages.get("successfullyShiftedInstrumentReservation").replace("{0}", Integer.toString(getInstrumentReservationsToShift().size()));
            final int conflicts = getPotentialInstrumentReservationsToShift().size();
            if (conflicts > 0) {
                message += "; " + Messages.get("conflictingInstrumentReservationCouldNotShifted").replace("{0}", Integer.toString(conflicts)).replace("{1}", StringHelper.getEnding(conflicts));
            }

            InstrumentReservation instrumentReservationFirst = getInstrumentReservationsToShift().stream().findFirst().get();
            instrumentReservationService.shiftInstrumentReservations(getInstrumentReservationsToShift(), DAYS.between(instrumentReservationFirst.getStartDate().toLocalDate(), getShiftToDate()));

            getFacesMessagesManager().bufferWarningClear(message);
            return getViewScreenURL(instrumentReservationFirst);
        }
        return null;
    }

    public void shiftStartDateChanged() {
        resetShiftToDate();
        resetShiftedDates();

        if (getShiftStartDate() != null && getShiftEndDate() != null && getShiftEndDate().isBefore(getShiftStartDate())) {
            setShiftEndDate(getShiftStartDate());
            getFacesMessagesManager().validationError("shiftInstrumentReservationsForm:shiftEndDate", Messages.get("adapted"));
            getFacesMessagesManager().printWarn(Messages.get("noteAdaptedEndDate"));
        }

        loadPotentialInstrumentReservationsToShift();
    }

    public void shiftToDateChanged() {
        instrumentReservationsToShift = new ArrayList<>();
        getEarliestPotentialInstrumentReservationToShift().ifPresent(res -> {
            long numbersOfDaysToShift = DAYS.between(res.getStartDate().toLocalDate(), getShiftToDate());
            int counter = 0;
            // Calculate the start and end the Instrument Reservation is supposed to be shifted to.
            for (final InstrumentReservation potentialInstrumentReservationToShift : getPotentialInstrumentReservationsToShift()) {
                LocalDateTime newStartDateTime = potentialInstrumentReservationToShift.getStartDate().plusDays(numbersOfDaysToShift);
                LocalDateTime newEndDateTime = potentialInstrumentReservationToShift.getEndDate().plusDays(numbersOfDaysToShift);

                // If there is a collision with another Instrument Reservation, display a message. Instrument Reservations which are shifted at the same time are not checked since they cannot collide.
                if (!instrumentReservationService.validateShiftEventCollision(new LocalDateTimeInterval(newStartDateTime, newEndDateTime), potentialInstrumentReservationToShift
                    .getInstrument(), getPotentialInstrumentReservationsToShift())) {
                    getFacesMessagesManager().validationError("shiftInstrumentReservationsForm:shiftinstrumentreservationtable:" + counter + ":id", Messages.get("conflict"));
                } else {
                    potentialInstrumentReservationToShift.setShiftedStartDate(newStartDateTime);
                    potentialInstrumentReservationToShift.setShiftedEndDate(newEndDateTime);
                    getInstrumentReservationsToShift().add(potentialInstrumentReservationToShift);
                }
                counter++;
            }
        });
    }

    public void shiftedInstrumentChanged() {
        setShiftToDate(null);
        resetShiftedDates();
        loadPotentialInstrumentReservationsToShift();
    }

    public void technologyChanged() {
        setShiftedInstrument(null);
        setShiftToDate(null);
        resetShiftedDates();
        loadPotentialInstrumentReservationsToShift();
    }
}