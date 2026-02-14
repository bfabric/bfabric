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

import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadRun extends XMLRequestParameterReadSupervisorBasedEntity {

    private final List<Long> containerid = new ArrayList<>();

    private final List<String> datafolder = new ArrayList<>();

    private final List<Long> instrumentid = new ArrayList<>();

    private final List<Long> instrumentreadconfigurationid = new ArrayList<>();

    private final List<Long> rununitid = new ArrayList<>();

    private final List<Long> sampleid = new ArrayList<>();

    private final List<String> serverlocation = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> status = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(instrumentid, "instrument.id"));
        items.addAll(getWhereClauseItemsLong(instrumentreadconfigurationid, "instrumentReadConfiguration.id"));
        items.addAll(getWhereClauseItemsLong(rununitid, "runUnit.id"));
        items.addAll(getWhereClauseItemsString(datafolder, "dataFolder"));
        items.addAll(getWhereClauseItemsString(serverlocation, "serverLocation"));
        items.addAll(getJoinWhereClauseItemsLong(sampleid, "runUnit.runUnitLanes runUnitLane join runUnitLane.samples sample", "sample.id", "sampleid"));
        items.addAll(getJoinWhereClauseItemsLong(containerid, "runUnit.runUnitLanes runUnitLane join runUnitLane.samples sample", "sample.container.id", "containerid"));

        for (int index = 0; index < status.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("status");
            whereClauseItem.setParameterName("status" + index);
            whereClauseItem.setParameterValueEnum(StatusEnum.value(status.get(index)));
            items.add(whereClauseItem);
        }

        return items;
    }
}
