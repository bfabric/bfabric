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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEvent;
import org.bfabric.entity.Event;

public class AgendaHelperSlot {

    protected AbstractEvent event;

    private LocalDateTime date;

    private int monthSpan;

    public AgendaHelperSlot() {
    }

    public String getColor() {
        return "#FFFFFF";
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateAsText() {
        return getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-a"));
    }

    public String getDayColor() {
        return "color:" + (isToday() ? "#FF0000" : "#545E69");
    }

    public String getDayHeaderStyleClass() {
        return isWeekend() ? " slotHeaderWeekend" : Constants.EMPTY_STRING;
    }

    public String getDayOfMonth() {
        return isMorningSlot() ? String.valueOf(getDate().getDayOfMonth()) : Constants.EMPTY_STRING;
    }

    public String getDescription() {
        String description = Constants.EMPTY_STRING;
        if (getEvent() != null) {
            description += getEvent().getName();
            if (getEvent().getDescription() != null) {
                description += ": " + getEvent().getDescription();
            }
        } else {
            description += getDateAsText();
        }
        return description;
    }

    public AbstractEvent getEvent() {
        return event;
    }

    public int getMonthSpan() {
        return monthSpan;
    }

    public String getMonthYearAsText() {
        return isMorningSlot() && getMonthSpan() > 0 ? getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + getDate().getYear() : Constants.EMPTY_STRING;
    }

    public String getName() {
        return getEvent() != null ? getEvent().getName() : null;
    }

    public String getSlot() {
        return DateTimeFormatter.ofPattern("a").format(getDate());
    }

    public String getSlotStyleClass() {
        return isWeekend() ? " slotWeekend" : Constants.EMPTY_STRING;
    }

    public String getWeekday() {
        return isMorningSlot() ? getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 3) : Constants.EMPTY_STRING;
    }

    public boolean hasEvent() {
        return event != null;
    }

    public boolean isMonthSpanGreaterThan(int span) {
        return getMonthSpan() > span;
    }

    public boolean isMorningSlot() {
        return getSlot().equals(Event.AM);
    }

    public boolean isToday() {
        return getDate().toLocalDate().equals(LocalDate.now());
    }

    public boolean isWeekend() {
        return DateUtils.isWeekend(getDate());
    }

    public void setDate(LocalDateTime date) throws NullPointerException {
        if (date == null) {
            throw new NullPointerException("Date cannot be null");
        }
        this.date = date;
    }

    public void setEvent(AbstractEvent event) {
        this.event = event;
    }

    public void setMonthSpan(int monthSpan) {
        this.monthSpan = monthSpan;
    }
}
