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

import java.util.Collections;
import java.util.List;

import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadResourceContent extends XMLRequestParameterReadEntity {

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();

        // Only process the first id in the id list and ignore the remaining ones.
        if (items.size() > 1) {
            items.retainAll(Collections.singletonList(items.get(0)));
        }

        // File contents of resources on the bfabric internal local storage.
        WhereClauseItem whereClauseItemInternalLocalStorage = new WhereClauseItem();
        whereClauseItemInternalLocalStorage.setAttributeName("storage.id");
        whereClauseItemInternalLocalStorage.setParameterName("storageid0");
        whereClauseItemInternalLocalStorage.setParameterValueLong(9L);
        items.add(whereClauseItemInternalLocalStorage);

        // File contents of resources on the bfabric external local storage.
        WhereClauseItem whereClauseItemExternalLocalStorage = new WhereClauseItem();
        whereClauseItemExternalLocalStorage.setAttributeName("storage.id");
        whereClauseItemExternalLocalStorage.setParameterName("storageid1");
        whereClauseItemExternalLocalStorage.setParameterValueLong(10L);
        items.add(whereClauseItemExternalLocalStorage);

        return items;
    }
}
