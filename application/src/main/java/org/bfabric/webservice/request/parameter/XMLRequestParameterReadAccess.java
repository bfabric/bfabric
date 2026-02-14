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

public class XMLRequestParameterReadAccess extends XMLRequestParameterReadBaseEntity {

    private final List<String> basepath = new ArrayList<>();

    private final List<String> enabled = new ArrayList<>();

    private final List<String> host = new ArrayList<>();

    private final List<String> protocol = new ArrayList<>();

    private final List<Long> storageid = new ArrayList<>();

    private final List<String> type = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsBoolean(enabled, "enabled"));
        items.addAll(getWhereClauseItemsLong(storageid, "storage.id"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(protocol, "accessProtocol.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(type, "accessType.name"));
        items.addAll(getWhereClauseItemsString(host, "host"));
        items.addAll(getWhereClauseItemsString(basepath, "basePath"));
        return items;
    }
}
