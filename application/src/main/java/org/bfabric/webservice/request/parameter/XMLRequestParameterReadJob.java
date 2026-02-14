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

import org.bfabric.entity.Job;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadJob extends XMLRequestParameterReadNamedBaseEntity {

    private final List<String> parentclassname = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> action = new ArrayList<>();

    private final List<String> log = new ArrayList<>();

    private final List<String> requester = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> status = new ArrayList<>();

    private final List<Long> parentid = new ArrayList<>();

    private final List<Long> workunitid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(parentid, "parentId"));
        items.addAll(getWhereClauseItemsString(parentclassname, "parentClassName"));
        items.addAll(getWhereClauseItemsString(requester, "requester"));
        items.addAll(getJoinWhereClauseItemsLong(workunitid, "workunits workunit", "workunit.id", "workunitid"));
        items.addAll(getJoinWhereClauseItemsString(log, "jobLogs jobLog", "jobLog.log", "log"));

        for (int index = 0; index < action.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("action");
            whereClauseItem.setParameterName("action" + index);
            whereClauseItem.setParameterValueEnum(LogActionEnum.value(action.get(index)));
            items.add(whereClauseItem);
        }

        for (int index = 0; index < status.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("status");
            whereClauseItem.setParameterName("status" + index);
            whereClauseItem.setParameterValueEnum(StatusEnum.value(status.get(index), Job.class));
            items.add(whereClauseItem);
        }

        return items;
    }
}
