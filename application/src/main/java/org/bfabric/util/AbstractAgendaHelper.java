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

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEvent;

public abstract class AbstractAgendaHelper {

    protected LocalDateTimeInterval interval;

    public abstract List<? extends AbstractEvent> getEvents();

    public List<AgendaHelperSlot> getHeadSlots() {
        List<AgendaHelperSlot> headSlots = new ArrayList<>();
        LocalDateTime tmpDateTime = interval.getStart();

        // create slots in interval
        while (interval.contains(tmpDateTime)) {
            // create slot
            AgendaHelperSlot agendaHelperSlot = new AgendaHelperSlot();

            // create slot
            agendaHelperSlot.setDate(tmpDateTime);

            // set month span: number of slots until the end of the month (just for morning slots)
            if ((tmpDateTime.getDayOfMonth() == 1 || tmpDateTime.equals(interval.getStart())) && agendaHelperSlot.isMorningSlot()) {
                LocalDateTime endOfMonthDate = tmpDateTime.plusMonths(1).withDayOfMonth(1).withHour(12);
                endOfMonthDate = endOfMonthDate.minusDays(1);
                if (interval.contains(endOfMonthDate)) {
                    agendaHelperSlot.setMonthSpan((Period.between(tmpDateTime.toLocalDate(), endOfMonthDate.toLocalDate()).getDays() + 1) * 2);
                } else {
                    agendaHelperSlot.setMonthSpan((Period.between(tmpDateTime.toLocalDate(), interval.getEnd().toLocalDate()).getDays() + 1) * 2);
                }
            }

            headSlots.add(agendaHelperSlot);

            // proceed to next slot
            if (tmpDateTime.getHour() < 12) {
                // go from hour 8 to 13 to switch to the next afternoon slot
                tmpDateTime = tmpDateTime.plusHours(5);
            } else {
                // go from hour 13 to 8 plus one to switch to the next morning slot
                tmpDateTime = tmpDateTime.plusDays(1).minusHours(5);
            }
        }
        return headSlots;
    }

    public LocalDateTimeInterval getInterval() {
        return interval;
    }

    public String getMonthYearAsText() {
        return getHeadSlots().isEmpty() ? Constants.EMPTY_STRING : getHeadSlots().get(0).getMonthYearAsText();
    }

    public boolean hasEvents() {
        return getEvents() != null && !getEvents().isEmpty();
    }

    public void setInterval(LocalDateTime intervalStart, LocalDateTime intervalEnd) {
        if (intervalStart.isBefore(intervalEnd)) {
            setInterval(new LocalDateTimeInterval(intervalStart, intervalEnd));
        }
    }

    public void setInterval(LocalDateTimeInterval interval) {
        if (interval != null) {
            this.interval = interval;
        }
    }
}