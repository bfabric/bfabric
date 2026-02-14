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

public class XMLRequestParameterReadOffer extends XMLRequestParameterReadDescriptionNamedBaseEntity {

    private final List<Long> containerid = new ArrayList<>();

    private final List<String> eugrant = new ArrayList<>();

    private final List<String> locked = new ArrayList<>();

    private final List<String> requesteraddress = new ArrayList<>();

    private final List<String> requestername = new ArrayList<>();

    private final List<Long> coachbackupid = new ArrayList<>();

    private final List<Long> requesterid = new ArrayList<>();

    private final List<Long> coachid = new ArrayList<>();

    private final List<Long> organizationtypeid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(containerid, "container.id"));
        items.addAll(getWhereClauseItemsBoolean(eugrant, "euGrant"));
        items.addAll(getWhereClauseItemsBoolean(locked, "lock"));
        items.addAll(getWhereClauseItemsString(requesteraddress, "requesterAddress"));
        items.addAll(getWhereClauseItemsString(requestername, "requesterName"));
        items.addAll(getWhereClauseItemsLong(coachbackupid, "coachBackup.id"));
        items.addAll(getWhereClauseItemsLong(coachid, "coach.id"));
        items.addAll(getWhereClauseItemsLong(requesterid, "requester.id"));
        items.addAll(getWhereClauseItemsLong(organizationtypeid, "organizationType.id"));
        return items;
    }
}