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

import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadResource extends XMLRequestParameterReadContainerReferencingEntity {

    private final List<Long> applicationid = new ArrayList<>();

    private final List<String> archiveexpirationdate = new ArrayList<>();

    private final List<String> archiveexpirationdateafter = new ArrayList<>();

    private final List<String> archiveexpirationdatebefore = new ArrayList<>();

    private final List<String> description = new ArrayList<>();

    private final List<String> expirationdate = new ArrayList<>();

    private final List<String> expirationdateafter = new ArrayList<>();

    private final List<String> expirationdatebefore = new ArrayList<>();

    private final List<String> filechecksum = new ArrayList<>();

    private final List<Long> inputresourceid = new ArrayList<>();

    private final List<String> relativepath = new ArrayList<>();

    private final List<String> report = new ArrayList<>();

    private final List<Long> sampleid = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> status = new ArrayList<>();

    private final List<Long> storageid = new ArrayList<>();

    private final List<Long> workunitid = new ArrayList<>();

    private final List<Long> size = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();

        items.addAll(getWhereClauseItemsLong(size, "size"));
        items.addAll(getWhereClauseItemsLong(applicationid, "workunit.application.id"));
        items.addAll(getWhereClauseItemsLong(inputresourceid, "inputResource.id"));
        items.addAll(getWhereClauseItemsLong(sampleid, "sample.id"));
        items.addAll(getWhereClauseItemsLong(storageid, "storage.id"));
        items.addAll(getWhereClauseItemsLong(workunitid, "workunit.id"));
        items.addAll(getWhereClauseItemsDateTime(expirationdate, "expirationDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(expirationdateafter, "expirationDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(expirationdatebefore, "expirationDate"));
        items.addAll(getWhereClauseItemsDateTime(archiveexpirationdate, "archiveExpirationDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(archiveexpirationdateafter, "archiveExpirationDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(archiveexpirationdatebefore, "archiveExpirationDate"));
        items.addAll(getWhereClauseItemsString(filechecksum, "fileChecksum"));
        items.addAll(getWhereClauseItemsString(relativepath, "relativePath"));
        items.addAll(getWhereClauseItemsString(report, "report"));
        items.addAll(getWhereClauseItemsString(description, "description"));

        for (int index = 0; index < status.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("status");
            whereClauseItem.setParameterName("status" + index);
            whereClauseItem.setParameterValueEnum(ResourceStatusEnum.value(status.get(index)));
            items.add(whereClauseItem);
        }

        return items;
    }
}
