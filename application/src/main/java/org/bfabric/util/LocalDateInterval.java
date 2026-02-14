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

import org.bfabric.Constants;

public final class LocalDateInterval implements Serializable {

    private static final long serialVersionUID = 1;

    private LocalDate end;

    private LocalDate start;

    public LocalDateInterval(LocalDate start, LocalDate end) {
        setStart(start);
        setEnd(end);
    }

    public boolean contains(LocalDate date) {
        return date != null && !(getStart() != null && getStart().isAfter(date) || getEnd() != null && date.isAfter(getEnd()));
    }

    public boolean contains(LocalDateInterval interval) {
        return interval != null && (getStart() == null || interval.getStart() != null && !getStart().isAfter(interval.getStart())) && (getEnd() == null || interval.getEnd() != null && !getEnd()
            .isBefore(interval.getEnd()));
    }

    public String getAsString() {
        if (getStart() != null && getEnd() != null) {
            return "[" + getStartAsString() + ", " + getEndAsString() + "]";
        } else if (getStart() != null) {
            return getStartAsString();
        } else if (getEnd() != null) {
            return getEndAsString();
        }
        return null;
    }

    public LocalDate getEnd() {
        return end;
    }

    public String getEndAsString() {
        if (getEnd() != null) {
            return Constants.DATE_FORMATTER.format(getEnd());
        }
        return "∞+";
    }

    public String getIntervalAsEUFormattedDateString() {
        return Constants.DATE_FORMATTER_EU.format(getStart()) + " - " + Constants.DATE_FORMATTER_EU.format(getEnd());
    }

    public String getIntervalAsString() {
        return "[" + getStartAsString() + ", " + getEndAsString() + "]";
    }

    public LocalDate getStart() {
        return start;
    }

    public String getStartAsString() {
        if (getStart() != null) {
            return Constants.DATE_FORMATTER.format(getStart());
        }
        return "-∞";
    }

    public boolean isCollapsed() {
        return getStart() != null && getEnd() != null && getStart().equals(getEnd());
    }

    public boolean overlaps(LocalDateInterval interval) {
        return interval != null && !(getStart() != null && interval.getEnd() != null && getStart().isAfter(interval.getEnd()) || interval.getStart() != null && getEnd() != null && interval.getStart()
            .isAfter(getEnd()));
    }

    public boolean same(LocalDateInterval interval) {
        return interval != null && (interval.getStart() == null && getStart() == null || interval.getStart() != null && interval.getStart()
            .equals(getStart())) && (interval.getEnd() == null && getEnd() == null || interval.getEnd() != null && interval.getEnd().equals(getEnd()));
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }
}