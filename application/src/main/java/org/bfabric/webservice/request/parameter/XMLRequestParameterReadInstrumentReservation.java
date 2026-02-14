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

package org.bfabric.webservice.request.parameter;

import java.util.ArrayList;
import java.util.List;

import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadInstrumentReservation extends XMLRequestParameterReadBaseEntity {

    private final List<Long> bookerid = new ArrayList<>();

    private final List<String> chargeable = new ArrayList<>();

    private final List<Long> chargeid = new ArrayList<>();

    private final List<Long> containerid = new ArrayList<>();

    private final List<String> enddate = new ArrayList<>();

    private final List<String> enddateafter = new ArrayList<>();

    private final List<String> enddatebefore = new ArrayList<>();

    private final List<Long> instrumentid = new ArrayList<>();

    private final List<String> startdate = new ArrayList<>();

    private final List<String> startdateafter = new ArrayList<>();

    private final List<String> startdatebefore = new ArrayList<>();

    private final List<Long> userid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(instrumentid, "instrument.id"));
        items.addAll(getWhereClauseItemsLong(containerid, "container.id"));
        items.addAll(getWhereClauseItemsLong(chargeid, "charge.id"));
        items.addAll(getWhereClauseItemsLong(userid, "user.id"));
        items.addAll(getWhereClauseItemsLong(bookerid, "booker.id"));
        items.addAll(getWhereClauseItemsBoolean(chargeable, "chargeable"));
        items.addAll(getWhereClauseItemsDateTime(startdate, "startDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(startdateafter, "startDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(startdatebefore, "startDate"));
        items.addAll(getWhereClauseItemsDateTime(enddate, "endDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(enddateafter, "endDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(enddatebefore, "endDate"));
        return items;
    }
}
