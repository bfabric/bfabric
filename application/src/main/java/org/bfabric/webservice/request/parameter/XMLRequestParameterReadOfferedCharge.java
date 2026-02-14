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

public class XMLRequestParameterReadOfferedCharge extends XMLRequestParameterReadBaseEntity {

    private final List<String> additionalprice = new ArrayList<>();

    private final List<String> basicprice = new ArrayList<>();

    private final List<String> billable = new ArrayList<>();

    private final List<String> date = new ArrayList<>();

    private final List<String> discount = new ArrayList<>();

    private final List<String> discountedprice = new ArrayList<>();

    private final List<String> notacccounted = new ArrayList<>();

    private final List<String> notes = new ArrayList<>();

    private final List<Long> organizationtypeid = new ArrayList<>();

    private final List<String> price = new ArrayList<>();

    private final List<String> taxrate = new ArrayList<>();

    private final List<String> taxtype = new ArrayList<>();

    private final List<String> total = new ArrayList<>();

    private final List<Long> chargerid = new ArrayList<>();

    private final List<Long> offerid = new ArrayList<>();

    private final List<Long> serviceid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsString(additionalprice, "additionalPrice"));
        items.addAll(getWhereClauseItemsString(basicprice, "basicPrice"));
        items.addAll(getWhereClauseItemsString(billable, "billable"));
        items.addAll(getWhereClauseItemsString(date, "date"));
        items.addAll(getWhereClauseItemsString(discount, "discount"));
        items.addAll(getWhereClauseItemsString(discountedprice, "discountedPrice"));
        items.addAll(getWhereClauseItemsString(notacccounted, "notAccounted"));
        items.addAll(getWhereClauseItemsString(notes, "notes"));
        items.addAll(getWhereClauseItemsString(price, "price"));
        items.addAll(getWhereClauseItemsString(taxrate, "taxRate"));
        items.addAll(getWhereClauseItemsString(taxtype, "taxType"));
        items.addAll(getWhereClauseItemsString(total, "total"));
        items.addAll(getWhereClauseItemsLong(organizationtypeid, "organizationType.id"));
        items.addAll(getWhereClauseItemsLong(chargerid, "charger.id"));
        items.addAll(getWhereClauseItemsLong(serviceid, "service.id"));
        items.addAll(getWhereClauseItemsLong(offerid, "offer.id"));
        return items;
    }
}
