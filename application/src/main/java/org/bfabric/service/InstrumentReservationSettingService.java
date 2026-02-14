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
    SOFTWARE. */

package org.bfabric.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.exception.RollbackException;
import org.bfabric.util.LocalDateInterval;

@Named
@Stateless
public class InstrumentReservationSettingService extends AbstractService {

    private static final long serialVersionUID = 1;

    public InstrumentReservationSettingService() {
        super(InstrumentReservationSetting.class);
    }

    public boolean anyChargeableReservation(InstrumentReservationSetting instrumentReservationSetting) {
        return instrumentReservationSetting != null && !createNamedQuery("InstrumentReservation.findAnyChargeableBySetting")
            .setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean canMaxDurationReservationBeAltered(InstrumentReservationSetting instrumentReservationSetting) {
        return instrumentReservationSetting != null && (instrumentReservationSetting
            .getMaxDuration() == null || createQuery("SELECT ir FROM InstrumentReservation ir where instrumentReservationSetting.id = :instrumentReservationSettingId and duration > :duration")
            .setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId())
            .setParameter("duration", instrumentReservationSetting.getMaxDuration().toMinutes()).setMaxResults(1).getResultList().isEmpty());
    }

    public boolean canMinDurationReservationBeAltered(InstrumentReservationSetting instrumentReservationSetting) {
        return instrumentReservationSetting != null && createQuery("SELECT ir FROM InstrumentReservation ir where instrumentReservationSetting.id = :instrumentReservationSettingId and duration < :duration")
            .setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId())
            .setParameter("duration", instrumentReservationSetting.getMinDuration().toMinutes()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean canSlotDurationReservationBeAltered(InstrumentReservationSetting instrumentReservationSetting) {
        return instrumentReservationSetting != null && createQuery("SELECT ir FROM InstrumentReservation ir where instrumentReservationSetting.id = :instrumentReservationSettingId and mod(duration, :duration) != 0")
            .setParameter("instrumentReservationSettingId", instrumentReservationSetting.getId())
            .setParameter("duration", instrumentReservationSetting.getSlotDuration().toMinutes()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean canWeekendReservationBeAltered(long instrumentReservationSettingId) {
        return createNativeQuery("SELECT id FROM InstrumentReservation where instrumentReservationSettingId = :instrumentReservationSettingId and contains_weekend(startdate, enddate) = TRUE")
            .setParameter("instrumentReservationSettingId", instrumentReservationSettingId).setMaxResults(1).getResultList().isEmpty();
    }

    public InstrumentReservationSetting getSettingByInstrumentAndDate(Instrument instrument, LocalDate localDate) {
        return (InstrumentReservationSetting) createNamedQuery("InstrumentReservationSetting.findByInstrumentAndDate").setParameter("instrument", instrument).setParameter("date", localDate)
            .setMaxResults(1).getSingleResult();
    }

    public LocalDateInterval getUpdatableValidFromInterval(long instrumentReservationSettingId) {
        Object[] result = (Object[]) createNativeQuery("SELECT min, max FROM UpdatableInstrumentReservationSettingInterval WHERE instrumentReservationSettingId2 = " + instrumentReservationSettingId)
            .getSingleResult();
        return new LocalDateInterval(result[0] != null ? ((java.sql.Timestamp) result[0]).toLocalDateTime().toLocalDate() : null, result[1] != null ? ((java.sql.Timestamp) result[1]).toLocalDateTime()
            .toLocalDate() : null);
    }

    public LocalDateInterval getUpdatableValidUntilInterval(long instrumentReservationSettingId) {
        Object[] result = (Object[]) createNativeQuery("SELECT min, max FROM UpdatableInstrumentReservationSettingInterval WHERE instrumentReservationSettingId1 = " + instrumentReservationSettingId)
            .getSingleResult();
        return new LocalDateInterval(result[0] != null ? ((java.sql.Timestamp) result[0]).toLocalDateTime().toLocalDate() : null, result[1] != null ? ((java.sql.Timestamp) result[1]).toLocalDateTime()
            .toLocalDate() : null);
    }

    private boolean isBookingAheadMinTimeLongerThanBookingAheadMaxTime(InstrumentReservationSetting instrumentReservationSetting) {
        return !instrumentReservationSetting.getBookingAheadMaxDurationHelper().isEmpty(false) &&
            !instrumentReservationSetting.getBookingAheadMinDurationHelper().isEmpty(false) &&
            instrumentReservationSetting.getBookingAheadMaxDurationHelper().getBeyondTimeDuration()
                .compareTo(instrumentReservationSetting.getBookingAheadMinDurationHelper().getBeyondTimeDuration()) < 0;
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final InstrumentReservationSetting instrumentReservationSetting = (InstrumentReservationSetting) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        // Verify validFrom.
        if (instrumentReservationSetting.getPreviousSetting() != null && instrumentReservationSetting.getValidFrom() == null) {
            validationErrorMsg.put(Constants.EDIT + ":validFrom", Messages.get("required"));
        }

        // Verify validUntil.
        if (instrumentReservationSetting.getValidFrom() != null && instrumentReservationSetting.getValidUntil() != null && instrumentReservationSetting.getValidFrom().plusWeeks(1)
            .isAfter(instrumentReservationSetting.getValidUntil())) {
            validationErrorMsg.put(Constants.EDIT + ":validUntil", Messages.get("mustBeAtLeastOneWeekHint"));
        }

        // Verify slotMinTime.
        if (instrumentReservationSetting.getSlotMinTime() != null && instrumentReservationSetting.getSlotMaxTime() != null && instrumentReservationSetting.getSlotMinTime()
            .toMinutes() >= instrumentReservationSetting.getSlotMaxTime().toMinutes()) {
            validationErrorMsg.put(Constants.EDIT + ":slotMaxTime", Messages.get("mustAfterSlotMinTimeHint"));
        }

        // Verify slotMaxTime.
        if (instrumentReservationSetting.getMinDuration() != null && instrumentReservationSetting.getMaxDuration() != null && instrumentReservationSetting.getMinDuration()
            .toMinutes() > instrumentReservationSetting.getMaxDuration().toMinutes()) {
            validationErrorMsg.put(Constants.EDIT + ":minDuration", Messages.get("mustNotBeLongerThanMaxDurationHint"));
        }

        // Verify slotLabelInterval.
        if (instrumentReservationSetting.getMinDuration() != null && instrumentReservationSetting.getSlotLabelInterval() != null && instrumentReservationSetting.getMinDuration()
            .toMinutes() < instrumentReservationSetting.getSlotLabelInterval().toMinutes()) {
            validationErrorMsg.put(Constants.EDIT + ":slotLabelInterval", Messages.get("durationMustNotBeLongerThanMinDurationHint"));
        }

        // Verify minDuration is not shorter than slotDuration.
        if (instrumentReservationSetting.getMinDuration() != null && instrumentReservationSetting.getSlotDuration() != null && instrumentReservationSetting.getMinDuration()
            .toMinutes() < instrumentReservationSetting.getSlotDuration().toMinutes()) {
            validationErrorMsg.put(Constants.EDIT + ":minDuration", Messages.get("minDurationMustNotBeShorterThanSlotDurationHint"));
        }

        // Verify BookingAheadMinDuration is not zero or less.
        if (instrumentReservationSetting.getBookingAheadMinDuration() != null && instrumentReservationSetting.getBookingAheadMinDuration().getSeconds() <= 0) {
            instrumentReservationSetting.setBookingAheadMinDuration(null);
        }

        // Verify BookingAheadMaxDuration is not zero or less.
        if (instrumentReservationSetting.getBookingAheadMaxDuration() != null && instrumentReservationSetting.getBookingAheadMaxDuration().getSeconds() <= 0) {
            instrumentReservationSetting.setBookingAheadMaxDuration(null);
        }

        // Verify BookingAheadMinTime is not longer than BookingAheadMaxTime.
        if (isBookingAheadMinTimeLongerThanBookingAheadMaxTime(instrumentReservationSetting)) {
            validationErrorMsg.put(Constants.EDIT + ":bookingAheadMinDurationGrid", Messages.get("bookingAheadMinDurationMustNotBeLongerThanBookingAheadMaxDurationGridHint"));
        }

        if (!instrumentReservationSetting.getInstrumentReservations().isEmpty()) {
            if (instrumentReservationSetting.getSlotDuration() == null) {
                if (!canMinDurationReservationBeAltered(instrumentReservationSetting)) {
                    validationErrorMsg.put(Constants.EDIT + ":minDuration", Messages.get("minDurationChangeValidationHint"));
                }

                if (!canMaxDurationReservationBeAltered(instrumentReservationSetting)) {
                    validationErrorMsg.put(Constants.EDIT + ":maxDuration", Messages.get("maxDurationChangeValidationHint"));
                }
            } else {
                if (!canSlotDurationReservationBeAltered(instrumentReservationSetting)) {
                    validationErrorMsg.put(Constants.EDIT + ":slotDuration", Messages.get("slotDurationChangeValidationHint"));
                }
            }
        }

        return validationErrorMsg;
    }

    public void remove(InstrumentReservationSetting instrumentReservationSetting) {
        if (instrumentReservationSetting != null && instrumentReservationSetting.getInstrument().getReservationSettings().size() > 1) {
            InstrumentReservationSetting newLastInstrumentReservationSetting = instrumentReservationSetting.getInstrument().getReservationSettings().get(1);
            super.remove(instrumentReservationSetting);
            newLastInstrumentReservationSetting.setValidUntil(null);
            merge(newLastInstrumentReservationSetting);
        }
    }

    public LinkedHashMap<String, String> save(InstrumentReservationSetting instrumentReservationSetting) {
        try {
            LinkedHashMap<String, String> validationErrorMsg = isValid(instrumentReservationSetting);
            if (validationErrorMsg.isEmpty()) {
                // Reset the adjacent borders of the previous and next settings.
                InstrumentReservationSetting previousSetting = instrumentReservationSetting.getPreviousSetting();
                if (previousSetting != null && instrumentReservationSetting.getValidFrom() != null && !instrumentReservationSetting.getValidFrom().equals(previousSetting.getValidUntil())) {
                    previousSetting.setValidUntil(instrumentReservationSetting.getValidFrom());
                    super.save(previousSetting);
                }
                InstrumentReservationSetting nextSetting = instrumentReservationSetting.getNextSetting();
                if (nextSetting != null && instrumentReservationSetting.getValidUntil() != null && !instrumentReservationSetting.getValidUntil().equals(nextSetting.getValidFrom())) {
                    nextSetting.setValidFrom(instrumentReservationSetting.getValidUntil());
                    super.save(nextSetting);
                }
                super.save(instrumentReservationSetting);
            }
            return validationErrorMsg;
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }
}