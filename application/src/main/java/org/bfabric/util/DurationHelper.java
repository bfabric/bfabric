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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.bfabric.entity.InstrumentReservation;

public class DurationHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private static final Integer weeksMax = 51;

    private static final Integer daysMax = 6;

    private static final Integer hoursMax = 23;

    private static final Integer minutesMax = 55;

    private Long days;

    private Long hours;

    private Long minutes;

    private Long weeks;

    public DurationHelper(Duration duration, boolean timeUnitOnly) {
        setDuration(duration, timeUnitOnly);
    }

    public DurationHelper() {
    }

    public static BigDecimal convertMinutesToChronoUnit(InstrumentReservation instrumentReservation) {
        return instrumentReservation != null ? convertMinutesToChronoUnit(instrumentReservation.getDuration(),
            instrumentReservation.getInstrumentReservationSetting().getChargeTimeUnit(), instrumentReservation.getDayDuration(), instrumentReservation.getInstrumentReservationSetting()
                .isWeekends()) : null;
    }

    public static BigDecimal convertMinutesToChronoUnit(Long minutes, ChronoUnit chronoUnit, Long dayDurationMinutes, boolean weekends) {
        if (minutes != null && chronoUnit != null && dayDurationMinutes != null) {
            Double duration = Double.valueOf(minutes);
            Double dayDuration = Double.valueOf(dayDurationMinutes);
            Double weekDuration = Double.valueOf(dayDurationMinutes) * (weekends ? 7 : 5);
            switch (chronoUnit) {
            case WEEKS:
                return BigDecimal.valueOf(duration / weekDuration).setScale(2, RoundingMode.HALF_EVEN);
            case DAYS:
                return BigDecimal.valueOf(duration / dayDuration).setScale(2, RoundingMode.HALF_EVEN);
            case HALF_DAYS:
                return BigDecimal.valueOf(2 * duration / dayDuration).setScale(2, RoundingMode.HALF_EVEN);
            case HOURS:
                return BigDecimal.valueOf(duration / 60).setScale(2, RoundingMode.HALF_EVEN);
            default:
                return BigDecimal.valueOf(duration).setScale(0, RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }

    public static Duration getDurationFromChronoUnits(Long weeks, Long days, Long hours, Long minutes) {
        Duration ret = null;
        if (weeks != null) {
            ret = Duration.ofMinutes(ChronoUnit.WEEKS.getDuration().toMinutes() * weeks);
        }
        if (days != null) {
            ret = recomputeDuration(ChronoUnit.DAYS, ret, days);
        }
        if (hours != null) {
            ret = recomputeDuration(ChronoUnit.HOURS, ret, hours);
        }
        if (minutes != null) {
            ret = recomputeDuration(ChronoUnit.MINUTES, ret, minutes);
        }
        return ret;
    }

    public static Duration getDurationFromHoursAndMinutes(Long hours, Long minutes) {
        Duration ret = null;
        if (hours != null) {
            ret = Duration.ofHours(hours);
        }
        if (minutes != null) {
            if (ret != null) {
                ret = ret.plus(Duration.ofMinutes(minutes));
            } else {
                ret = Duration.ofMinutes(minutes);
            }
        }
        return ret;
    }

    private static Duration recomputeDuration(TemporalUnit temporalUnit, Duration duration, long amount) {
        if (duration != null) {
            return duration.plus(Duration.of(amount, temporalUnit));
        } else {
            return Duration.of(amount, temporalUnit);
        }
    }

    public void clear() {
        setDuration(null, true);
    }

    public Duration getBeyondTimeDuration() {
        return getDurationFromChronoUnits(getWeeks(), getDays(), getHours(), getMinutes());
    }

    public Long getDays() {
        return days;
    }

    public Integer getDaysMax() {
        return daysMax;
    }

    public Duration getDuration() {
        return getDurationFromHoursAndMinutes(getHours(), getMinutes());
    }

    public Long getHours() {
        return hours;
    }

    public Integer getHoursMax() {
        return hoursMax;
    }

    public Long getMinutes() {
        return minutes;
    }

    public Integer getMinutesMax() {
        return minutesMax;
    }

    public Long getWeeks() {
        return weeks;
    }

    public Integer getWeeksMax() {
        return weeksMax;
    }

    public boolean isEmpty(boolean timeUnitOnly) {
        if (timeUnitOnly) {
            return getMinutes() == null && getHours() == null;
        } else {
            return getWeeks() == null && getDays() == null && getMinutes() == null && getHours() == null;
        }
    }

    public boolean isFullDay() {
        return getHours() != null && getHours() == 24 && (getMinutes() == null || getMinutes() == 0);
    }

    public void setDays(Long days) {
        this.days = days;
    }

    public void setDuration(Duration duration, boolean timeUnitOnly) {
        if (duration != null) {
            if (timeUnitOnly) {
                setHours(duration.toHours());
                setMinutes(duration.toMinutes() % 60);
            } else {
                setWeeks(duration.toMinutes() / ChronoUnit.WEEKS.getDuration().toMinutes());
                setDays((duration.toMinutes() - ChronoUnit.WEEKS.getDuration().toMinutes() * getWeeks()) / Duration.ofDays(1).toMinutes());
                setHours((duration.toMinutes() - ChronoUnit.WEEKS.getDuration().toMinutes() * getWeeks() - Duration.ofDays(1)
                    .toMinutes() * getDays()) / Duration.ofHours(1).toMinutes());
                setMinutes(duration.toMinutes() - ChronoUnit.WEEKS.getDuration().toMinutes() * getWeeks() - Duration.ofDays(getDays())
                    .toMinutes() - Duration.ofHours(getHours()).toMinutes());
            }
        } else {
            setWeeks(null);
            setDays(null);
            setHours(null);
            setMinutes(null);
        }
    }

    public void setHours(Long hours) {
        this.hours = hours;
    }

    public void setMinutes(Long minutes) {
        this.minutes = minutes;
    }

    public void setWeeks(Long weeks) {
        this.weeks = weeks;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (getHours() != null && getHours() > 0) {
            ret.append(getHours()).append("h ");
        }
        if (getMinutes() != null && getMinutes() > 0) {
            ret.append(getMinutes()).append("m ");
        }
        return ret.toString();
    }
}




