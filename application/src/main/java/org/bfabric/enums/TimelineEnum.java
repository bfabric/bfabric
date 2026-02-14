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

package org.bfabric.enums;

import org.bfabric.Constants;
import org.bfabric.util.StringHelper;

public enum TimelineEnum {

    STATES,
    BOOKINGS,
    CHARGES,
    COMMENTS,
    DATASETS,
    FEEDBACKS,
    INSTRUMENT_RESERVATIONS,
    MEMBERS,
    NOTES,
    OFFERS,
    ORDERS,
    RESOURCES,
    RESULTS,
    SAMPLES,
    WORKFLOWS,
    WORKUNITS;

    TimelineEnum() {
    }

    public String getLabel() {
        return StringHelper.capitalize(name().replaceAll("_", " "));
    }

    public String getShortCut() {
        return equals(CHARGES) || equals(OFFERS) || equals(RESULTS) ? getLabel().substring(0, 2).toUpperCase() : getLabel().substring(0, 1);
    }

    public String getTab() {
        return name().toLowerCase().replaceAll("_", Constants.EMPTY_STRING);
    }

    public String getTableName() {
        return getLabel().substring(0, getLabel().length() - 1).replaceAll(" ", Constants.EMPTY_STRING);
    }

}
