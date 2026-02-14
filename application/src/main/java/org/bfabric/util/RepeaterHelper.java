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

public class RepeaterHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private LocalDate endDate = LocalDate.now();

    private boolean repeat = false;

    private int weeks = 1;

    public RepeaterHelper() {
    }

    public RepeaterHelper(boolean repeat, LocalDate endDate, int weeks) {
        this.repeat = repeat;
        this.endDate = endDate;
        this.weeks = weeks;
    }

    public LocalDateTime getEnd() {
        return getEndDate().atStartOfDay().plusDays(1);
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getWeeks() {
        return weeks;
    }

    public void init() {
        endDate = LocalDate.now();
        weeks = 1;
        repeat = false;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public boolean isValidEndDate(LocalDateTime eventEndDate) {
        return isValidEndDate(eventEndDate, getEnd());
    }

    public boolean isValidEndDate(LocalDateTime eventEndDate, LocalDateTime repeaterEnd) {
        return repeaterEnd.isAfter(eventEndDate.plusWeeks(getWeeks()));
    }

    public void setEnd(LocalDateTime end) {
        this.endDate = end.toLocalDate();
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setEndDate(LocalDateTime eventEndDate) {
        setEnd(eventEndDate.plusWeeks(getWeeks()));
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setWeeks(int weeks) {
        this.weeks = weeks;
    }
}

