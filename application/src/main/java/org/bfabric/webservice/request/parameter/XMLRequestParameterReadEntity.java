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

import javax.enterprise.inject.spi.CDI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.service.EntityService;

@XmlAccessorType(XmlAccessType.FIELD)
public class XMLRequestParameterReadEntity {

    private final List<Long> id = new ArrayList<>();

    public Boolean includeassociations = false;

    public Boolean includedeletableupdateable = false;

    public Boolean fulldetails = false;

    public void addIdList(List<Long> idList) {
        if (idList != null) {
            id.addAll(idList);
        }
    }

    public AbstractEntity fetch(Class<? extends AbstractEntity> entityClass, Long entityId) throws InvalidDataException {
        return CDI.current().select(EntityService.class).get().fetch(entityClass, entityId);
    }

    public List<WhereClauseItem> getJoinWhereClauseItemsInteger(List<Integer> items, String joinClause, String attributeName, String parameterName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setJoinClause(joinClause);
            whereClauseItem.setParameterName(parameterName + index);
            whereClauseItem.setParameterValueInteger(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getJoinWhereClauseItemsLong(List<Long> items, String joinClause, String attributeName, String parameterName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setJoinClause(joinClause);
            whereClauseItem.setParameterName(parameterName + index);
            whereClauseItem.setParameterValueLong(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getJoinWhereClauseItemsString(List<String> items, String joinClause, String attributeName, String parameterName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setJoinClause(joinClause);
            whereClauseItem.setParameterName(parameterName + index);
            whereClauseItem.setParameterValueString(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        return new ArrayList<>(getWhereClauseItemsLong(id, "id"));
    }

    public List<WhereClauseItem> getWhereClauseItemsBoolean(List<String> items, String attributeName) throws InvalidDataException {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueBoolean(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsBooleanList(List<Boolean> items, String attributeName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueBoolean(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsDate(List<String> items, String attributeName) throws InvalidDataException {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueDate(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsDateAfter(List<String> items, String attributeName) throws InvalidDataException {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, "after" + index);
            whereClauseItem.setParameterValueDateAfter(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsDateBefore(List<String> items, String attributeName) throws InvalidDataException {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, "before" + index);
            whereClauseItem.setParameterValueDateBefore(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsDateTime(List<String> items, String attributeName) throws InvalidDataException {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueDateTime(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsDateTimeAfter(List<String> items, String attributeName) throws InvalidDataException {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, "after" + index);
            whereClauseItem.setParameterValueDateTimeAfter(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsDateTimeBefore(List<String> items, String attributeName) throws InvalidDataException {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, "before" + index);
            whereClauseItem.setParameterValueDateTimeBefore(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsEnum(List<Enum<?>> items, String attributeName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueEnum(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsInteger(List<Integer> items, String attributeName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueInteger(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsLong(List<Long> items, String attributeName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueLong(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsNumber(List<Number> items, String attributeName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueNumber(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsString(List<String> items, String attributeName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setParameterValueString(items.get(index));
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }

    public List<WhereClauseItem> getWhereClauseItemsStringCaseInsensitive(List<String> items, String attributeName) {
        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem(attributeName, index);
            whereClauseItem.setCaseInsensitive(true);
            String parameterValue = items.get(index);
            if (parameterValue != null) {
                parameterValue = parameterValue.toLowerCase();
            }
            whereClauseItem.setParameterValueString(parameterValue);
            whereClauseItems.add(whereClauseItem);
        }
        return whereClauseItems;
    }
}
