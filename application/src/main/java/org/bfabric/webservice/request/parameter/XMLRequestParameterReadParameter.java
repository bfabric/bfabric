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

import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadParameter extends XMLRequestParameterReadBaseEntity {

    private final List<Long> applicationid = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> context = new ArrayList<>();

    private final List<Long> executableid = new ArrayList<>();

    private final List<String> key = new ArrayList<>();

    private final List<String> label = new ArrayList<>();

    private final List<String> modifiable = new ArrayList<>();

    private final List<String> required = new ArrayList<>();

    private final List<Long> storageid = new ArrayList<>();

    private final List<String> type = new ArrayList<>();

    private final List<String> value = new ArrayList<>();

    private final List<Long> workunitid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(applicationid, "application.id"));
        items.addAll(getWhereClauseItemsLong(executableid, "executable.id"));
        items.addAll(getWhereClauseItemsLong(storageid, "storage.id"));
        items.addAll(getWhereClauseItemsLong(workunitid, "workunit.id"));
        items.addAll(getWhereClauseItemsString(key, "key"));
        items.addAll(getWhereClauseItemsString(label, "label"));
        items.addAll(getWhereClauseItemsString(modifiable, "modifiable"));
        items.addAll(getWhereClauseItemsString(required, "required"));
        items.addAll(getWhereClauseItemsString(type, "type"));
        items.addAll(getWhereClauseItemsString(value, "value"));

        for (int index = 0; index < context.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("context");
            whereClauseItem.setParameterName("context" + index);
            whereClauseItem.setParameterValueEnum(ExecutableContextEnum.value(context.get(index)));
            items.add(whereClauseItem);
        }

        return items;
    }
}
