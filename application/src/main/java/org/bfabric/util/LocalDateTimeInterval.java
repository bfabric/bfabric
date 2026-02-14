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
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bfabric.Constants;

public final class LocalDateTimeInterval implements Serializable {

    private static final long serialVersionUID = 1;

    private LocalDateTime end;

    private LocalDateTime start;

    public LocalDateTimeInterval(LocalDateTime start, LocalDateTime end) {
        setStart(start);
        setEnd(end);
    }

    public LocalDateTimeInterval(LocalDate start, LocalDate end) {
        if (start != null) {
            setStart(start.atStartOfDay());
        }
        if (end != null) {
            setEnd(end.atStartOfDay());
        }
    }

    public boolean contains(LocalDateTime dateTime) {
        return dateTime != null && !(getStart() != null && getStart().isAfter(dateTime) || getEnd() != null && dateTime.isAfter(getEnd()));
    }

    public boolean contains(LocalDateTimeInterval interval) {
        return interval != null && (getStart() == null || interval.getStart() != null && !getStart().isAfter(interval.getStart())) && (getEnd() == null || interval.getEnd() != null && !getEnd()
            .isBefore(interval.getEnd()));
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getEndAsString() {
        return getEnd() != null ? Constants.DATETIME_FORMATTER.format(getEnd()) : "∞+";
    }

    public String getIntervalAsString() {
        return "[" + getStartAsString() + ", " + getEndAsString() + "]";
    }

    public LocalDateTime getStart() {
        return start;
    }

    public String getStartAsString() {
        return getStart() != null ? Constants.DATETIME_FORMATTER.format(getStart()) : "-∞";
    }

    public boolean isCollapsed() {
        return getStart() != null && getEnd() != null && getStart().equals(getEnd());
    }

    public boolean overlaps(LocalDateTimeInterval interval) {
        return interval != null && !(getStart() != null && interval.getEnd() != null && getStart().isAfter(interval.getEnd()) || interval.getStart() != null && getEnd() != null && interval.getStart()
            .isAfter(getEnd()));
    }

    public boolean same(LocalDateTimeInterval interval) {
        return interval != null && (interval.getStart() == null && getStart() == null || interval.getStart() != null && interval.getStart()
            .equals(getStart())) && (interval.getEnd() == null && getEnd() == null || interval.getEnd() != null && interval.getEnd().equals(getEnd()));
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }
}