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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.exception.BfabricValidatorException;

@Named
@ApplicationScoped
public class DateUtils {

    public static String getDateAsFormattedString(LocalDate date) {
        return date != null ? Constants.DATE_FORMATTER.format(date) : null;
    }

    public static String getDateAsFormattedString(LocalDateTime date) {
        return getDateAsFormattedString(date, false);
    }

    public static String getDateAsFormattedString(LocalDateTime date, boolean showMilliSeconds) {
        return date != null ? showMilliSeconds ? Constants.DATETIME_FORMATTER_N.format(date) : Constants.DATETIME_FORMATTER.format(date) : null;
    }

    public static String getDateAsFormattedStringWithoutTime(LocalDateTime date) {
        return date != null ? Constants.DATE_FORMATTER.format(date) : null;
    }

    public static String getDateDownloadString() {
        return getDateDownloadString(LocalDateTime.now());
    }

    public static String getDateDownloadString(LocalDateTime dateTime) {
        return dateTime != null ? DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(dateTime) : Constants.EMPTY_STRING;
    }

    public static LocalDate getDateFrom(String dateString) {
        if (StringHelper.isNotEmpty(dateString)) {
            try {
                return LocalDate.parse(dateString, Constants.DATE_FORMATTER);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static String getDateIndexString(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) : null;
    }

    public static String getDateIndexString(LocalDateTime date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) : null;
    }

    public static LocalDateTime getDateTimeFrom(String dateString) {
        if (StringHelper.isNotEmpty(dateString)) {
            try {
                return LocalDateTime.parse(dateString, Constants.DATETIME_FORMATTER);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static int getDaySlotPercentage(LocalDateTime dateTime, LocalDateTime dayDateTime) {
        return Double.valueOf(dayDateTime.toLocalDate().atStartOfDay().until(dateTime, ChronoUnit.MINUTES) / 14.4).intValue();
    }

    public static int getDaySlotPercentage(LocalDateTime dateTime) {
        return Double.valueOf(dateTime.toLocalDate().atStartOfDay().until(dateTime, ChronoUnit.MINUTES) / 14.4).intValue();
    }

    public static List<Long> getDivisors(long n) {
        List<Long> divisors = new ArrayList<>();
        // Note that this loop runs till square root.
        for (long i = 1; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                if (n / i == i) {
                    // If divisors are equal, add only one.
                    divisors.add(i);
                } else {
                    // Otherwise, add both.
                    divisors.add(i);
                    divisors.add(n / i);
                }
            }
        }
        Collections.sort(divisors);
        return divisors;
    }

    public static LocalTime getLocalTime(Duration duration) {
        return duration != null ? LocalTime.MIN.plusMinutes(duration.toMinutes()) : null;
    }

    public static List<Duration> getSlotDurations(Duration minTime, Duration maxTime) {
        Long minTimeMinutes = minTime != null ? minTime.toMinutes() : 0L;
        Long maxTimeMinutes = maxTime != null ? maxTime.toMinutes() : 1440L;
        List<Duration> durations = new ArrayList<>();
        for (Long minutes : getDivisors(maxTimeMinutes - minTimeMinutes)) {
            durations.add(Duration.of(minutes, ChronoUnit.MINUTES));
        }
        return durations;
    }

    public static long getWorkingDaysCount(final LocalDate start, final LocalDate end) {
        final DayOfWeek startDayOfWeek = start.getDayOfWeek();
        final DayOfWeek endDayOfWeek = end.getDayOfWeek();

        final long days = Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays();
        final long daysWithoutWeekends = days - 2 * ((days + startDayOfWeek.getValue()) / 7);

        //adjust for starting and ending on a Sunday:
        return daysWithoutWeekends + (startDayOfWeek == DayOfWeek.SUNDAY ? 1 : 0) + (endDayOfWeek == DayOfWeek.SUNDAY ? 1 : 0);
    }

    public static boolean isEndDateWeekend(LocalDateTime localDateTime) {
        return localDateTime != null && (localDateTime.toLocalDate().getDayOfWeek().equals(DayOfWeek.SATURDAY) && !localDateTime.toLocalTime().equals(LocalTime.MIN) || localDateTime.toLocalDate()
            .getDayOfWeek().equals(DayOfWeek.SUNDAY) || localDateTime.toLocalDate().getDayOfWeek().equals(DayOfWeek.MONDAY) && localDateTime.toLocalTime().equals(LocalTime.MIN));
    }

    public static boolean isWeekend(LocalDate localDate) {
        return localDate != null && (localDate.getDayOfWeek().equals(DayOfWeek.SATURDAY) || localDate.getDayOfWeek().equals(DayOfWeek.SUNDAY));
    }

    public static boolean isWeekend(LocalDateTime localDateTime) {
        return localDateTime != null && isWeekend(localDateTime.toLocalDate());
    }

    public static LocalTime minus(LocalTime time, LocalTime duration) {
        return time != null ? duration != null ? time.minusHours(duration.getHour()).minusMinutes(duration.getMinute()) : time : null;
    }

    public static LocalDateTime minus(LocalDateTime time, LocalTime duration) {
        return time != null ? duration != null ? time.minusHours(duration.getHour()).minusMinutes(duration.getMinute()) : time : null;
    }

    public static boolean overlapsWithWeekend(LocalDateInterval interval) {
        long numOfDaysBetween = ChronoUnit.DAYS.between(interval.getStart(), interval.getEnd());
        return IntStream.iterate(0, i -> i + 1)
            .limit(numOfDaysBetween)
            .mapToObj(i -> interval.getStart().plusDays(i))
            .anyMatch(s -> s.getDayOfWeek() == DayOfWeek.SUNDAY || s.getDayOfWeek() == DayOfWeek.SATURDAY);
    }

    public static LocalTime plus(LocalTime time, LocalTime duration) {
        return time != null ? duration != null ? time.plusHours(duration.getHour()).plusMinutes(duration.getMinute()) : time : null;
    }

    public static LocalDateTime plus(LocalDateTime time, LocalTime duration) {
        return time != null ? duration != null ? time.plusHours(duration.getHour()).plusMinutes(duration.getMinute()) : time : null;
    }

    @SuppressWarnings("SameReturnValue")
    public static boolean validateDateRange(LocalDate startDate, LocalDate endDate) throws BfabricValidatorException {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BfabricValidatorException("invalidDateIntervalException");
        }
        return true;
    }
}
