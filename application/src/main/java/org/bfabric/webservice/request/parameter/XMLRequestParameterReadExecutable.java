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

public class XMLRequestParameterReadExecutable extends XMLRequestParameterReadSupervisorBasedEntity {

    private final List<Long> applicationid = new ArrayList<>();

    private final List<Long> applicationworkunitid = new ArrayList<>();

    private final List<String> context = new ArrayList<>();

    private final List<String> filechecksum = new ArrayList<>();

    private final List<String> program = new ArrayList<>();

    private final List<String> relativepath = new ArrayList<>();

    private final List<Long> masterexecutableid = new ArrayList<>();

    private final List<Long> predecessorid = new ArrayList<>();

    private final List<Long> size = new ArrayList<>();

    private final List<Long> slaveexecutableid = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> status = new ArrayList<>();

    private final List<Long> storageid = new ArrayList<>();

    private final List<Long> submitterid = new ArrayList<>();

    private final List<Long> submitterworkunitid = new ArrayList<>();

    private final List<Long> successorid = new ArrayList<>();

    private final List<String> valid = new ArrayList<>();

    private final List<String> version = new ArrayList<>();

    private final List<Long> workunitid = new ArrayList<>();

    private final List<Long> wrappercreatorid = new ArrayList<>();

    private final List<Long> wrappercreatorworkunitid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(masterexecutableid, "masterExecutable.id"));
        items.addAll(getWhereClauseItemsLong(predecessorid, "predecessor.id"));
        items.addAll(getWhereClauseItemsLong(size, "size"));
        items.addAll(getWhereClauseItemsLong(successorid, "successor.id"));
        items.addAll(getWhereClauseItemsLong(submitterid, "submitter.id"));
        items.addAll(getWhereClauseItemsLong(workunitid, "workunit.id"));
        items.addAll(getWhereClauseItemsLong(wrappercreatorid, "wrapperCreator.id"));
        items.addAll(getWhereClauseItemsString(context, "context"));
        items.addAll(getWhereClauseItemsString(filechecksum, "fileChecksum"));
        items.addAll(getWhereClauseItemsString(program, "program"));
        items.addAll(getWhereClauseItemsString(relativepath, "relativePath"));
        items.addAll(getWhereClauseItemsString(version, "version"));
        items.addAll(getWhereClauseItemsBoolean(valid, "valid"));
        items.addAll(getJoinWhereClauseItemsLong(applicationid, "applications application", "application.id", "applicationid"));
        items.addAll(getJoinWhereClauseItemsLong(applicationworkunitid, "applicationWorkunits applicationWorkunit", "applicationWorkunit.id", "applicationworkunitid"));
        items.addAll(getJoinWhereClauseItemsLong(slaveexecutableid, "slaveExecutables slaveExecutable", "slaveExecutable.id", "slaveexecutableid"));
        items.addAll(getJoinWhereClauseItemsLong(storageid, "storages storage", "storage.id", "storageid"));
        items.addAll(getJoinWhereClauseItemsLong(submitterid, "submitters submitter", "submitter.id", "submitterid"));
        items.addAll(getJoinWhereClauseItemsLong(submitterworkunitid, "submitterWorkunits submitterWorkunit", "submitterWorkunit.id", "submitterworkunitid"));
        items.addAll(getJoinWhereClauseItemsLong(wrappercreatorworkunitid, "wrapperCreatorWorkunits wrapperCreatorWorkunit", "wrapperCreatorWorkunit.id", "wrappercreatorworkunitid"));

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
