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

public class XMLRequestParameterReadCharge extends XMLRequestParameterReadBaseEntity {

    private final List<Long> chargerid = new ArrayList<>();

    private final List<Long> containerid = new ArrayList<>();

    private final List<String> serviceareaname = new ArrayList<>();

    private final List<Long> servicecodeid = new ArrayList<>();

    private final List<String> servicecodename = new ArrayList<>();

    private final List<Long> serviceid = new ArrayList<>();

    private final List<Long> orderitemid = new ArrayList<>();

    private final List<Long> sampleid = new ArrayList<>();

    private final List<String> servicename = new ArrayList<>();

    private final List<String> servicetypename = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(containerid, "container.id"));
        items.addAll(getWhereClauseItemsLong(chargerid, "charger.id"));
        items.addAll(getWhereClauseItemsLong(serviceid, "service.id"));
        items.addAll(getWhereClauseItemsLong(servicecodeid, "serviceCode.id"));
        items.addAll(getWhereClauseItemsString(servicecodename, "serviceCodeName"));
        items.addAll(getWhereClauseItemsString(servicename, "serviceName"));
        items.addAll(getWhereClauseItemsString(servicetypename, "serviceTypeName"));
        items.addAll(getWhereClauseItemsString(serviceareaname, "serviceAreaName"));
        items.addAll(getJoinWhereClauseItemsLong(sampleid, "samples sample", "sample.id", "sampleid"));
        items.addAll(getJoinWhereClauseItemsLong(orderitemid, "orderItems orderItem", "orderItem.id", "orderitemid"));
        return items;
    }
}
