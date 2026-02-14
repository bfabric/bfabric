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

import org.bfabric.enums.LogStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadInstrumentEvent extends XMLRequestParameterReadBaseEntity {

    private final List<Long> instrumentid = new ArrayList<>();

    private final List<Long> instrumenteventtypeid = new ArrayList<>();

    private final List<Long> userid = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> status = new ArrayList<>();

    private final List<String> description = new ArrayList<>();

    private final List<String> datetime = new ArrayList<>();

    private final List<String> datetimeafter = new ArrayList<>();

    private final List<String> datetimebefore = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(instrumentid, "instrument.id"));
        items.addAll(getWhereClauseItemsLong(instrumenteventtypeid, "instrumentEventType.id"));
        items.addAll(getWhereClauseItemsLong(userid, "user.id"));
        items.addAll(getWhereClauseItemsString(description, "description"));
        items.addAll(getWhereClauseItemsDateTime(datetime, "dateTime"));
        items.addAll(getWhereClauseItemsDateTimeAfter(datetimeafter, "dateTime"));
        items.addAll(getWhereClauseItemsDateTimeBefore(datetimebefore, "dateTime"));
        for (int index = 0; index < status.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("status");
            whereClauseItem.setParameterName("status" + index);
            whereClauseItem.setParameterValueEnum(LogStatusEnum.value(status.get(index)));
            items.add(whereClauseItem);
        }
        return items;
    }
}
