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

public class XMLRequestParameterReadImportResource extends XMLRequestParameterReadNamedBaseEntity {

    private final List<Long> applicationid = new ArrayList<>();

    private final List<Long> containerid = new ArrayList<>();

    private final List<String> expirationdate = new ArrayList<>();

    private final List<String> expirationdateafter = new ArrayList<>();

    private final List<String> expirationdatebefore = new ArrayList<>();

    private final List<String> filechecksum = new ArrayList<>();

    private final List<String> filedate = new ArrayList<>();

    private final List<String> filedateafter = new ArrayList<>();

    private final List<String> filedatebefore = new ArrayList<>();

    private final List<String> relativepath = new ArrayList<>();

    private final List<String> report = new ArrayList<>();

    private final List<Long> sampleid = new ArrayList<>();

    private final List<Long> storageid = new ArrayList<>();

    private final List<String> url = new ArrayList<>();

    private final List<Long> workunitid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(applicationid, "application.id"));
        items.addAll(getWhereClauseItemsLong(containerid, "container.id"));
        items.addAll(getWhereClauseItemsLong(sampleid, "sample.id"));
        items.addAll(getWhereClauseItemsLong(storageid, "storage.id"));
        items.addAll(getWhereClauseItemsDateTime(expirationdate, "expirationDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(expirationdateafter, "expirationDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(expirationdatebefore, "expirationDate"));
        items.addAll(getWhereClauseItemsDateTime(filedate, "fileDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(filedateafter, "fileDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(filedatebefore, "fileDate"));
        items.addAll(getWhereClauseItemsString(filechecksum, "fileChecksum"));
        items.addAll(getWhereClauseItemsString(relativepath, "relativePath"));
        items.addAll(getWhereClauseItemsString(report, "report"));
        items.addAll(getWhereClauseItemsString(url, "url"));
        items.addAll(getJoinWhereClauseItemsLong(workunitid, "workunits workunit", "workunit.id", "workunitid"));

        return items;
    }
}
