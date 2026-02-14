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

public class XMLRequestParameterReadBooking extends XMLRequestParameterReadContainerReferencingEntity {

    private final List<Long> accountid = new ArrayList<>();

    private final List<Long> containerid = new ArrayList<>();

    private final List<String> bookingdate = new ArrayList<>();

    private final List<Long> bookingnr = new ArrayList<>();

    private final List<Long> bookingissuerid = new ArrayList<>();

    private final List<Long> costcentreid = new ArrayList<>();

    private final List<Long> currencyid = new ArrayList<>();

    private final List<Long> divisionid = new ArrayList<>();

    private final List<String> executionperiodenddate = new ArrayList<>();

    private final List<String> executionperiodstartdate = new ArrayList<>();

    private final List<Long> financialcenterid = new ArrayList<>();

    private final List<Long> instituteid = new ArrayList<>();

    private final List<Long> oldserviceorderbookingid = new ArrayList<>();

    private final List<String> orderdate = new ArrayList<>();

    private final List<String> paid = new ArrayList<>();

    private final List<String> roundingvalue = new ArrayList<>();

    private final List<String> sapnumber = new ArrayList<>();

    private final List<String> sapnumbernext = new ArrayList<>();

    private final List<String> subtotal = new ArrayList<>();

    private final List<String> tax = new ArrayList<>();

    private final List<String> total = new ArrayList<>();

    private final List<String> totalcharges = new ArrayList<>();

    private final List<String> billingaddress = new ArrayList<>();

    private final List<String> billingaddresscity = new ArrayList<>();

    private final List<Long> billingaddresscountryid = new ArrayList<>();

    private final List<String> billingaddressstreet = new ArrayList<>();

    private final List<String> billingaddresssupplement = new ArrayList<>();

    private final List<String> billingaddresszip = new ArrayList<>();

    private final List<String> billingcustomerfirstname = new ArrayList<>();

    private final List<String> billingcustomerlastname = new ArrayList<>();

    private final List<String> billingcustomername = new ArrayList<>();

    private final List<String> billingcustomertitle = new ArrayList<>();

    private final List<String> billingemail = new ArrayList<>();

    private final List<String> referencenumber = new ArrayList<>();

    private final List<String> vatnumber = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(accountid, "account.id"));
        items.addAll(getWhereClauseItemsString(bookingdate, "bookingDate"));
        items.addAll(getWhereClauseItemsLong(bookingnr, "bookingNr"));
        items.addAll(getWhereClauseItemsLong(bookingissuerid, "bookingIssuer.id"));
        items.addAll(getWhereClauseItemsLong(containerid, "container.id"));
        items.addAll(getWhereClauseItemsLong(costcentreid, "costCentre.id"));
        items.addAll(getWhereClauseItemsLong(currencyid, "currency.id"));
        items.addAll(getWhereClauseItemsLong(divisionid, "division.id"));
        items.addAll(getWhereClauseItemsLong(instituteid, "institute.id"));
        items.addAll(getWhereClauseItemsLong(financialcenterid, "financialCenter.id"));
        items.addAll(getWhereClauseItemsString(executionperiodenddate, "executionPeriodEndDate"));
        items.addAll(getWhereClauseItemsString(executionperiodstartdate, "executionPeriodStartDate"));
        items.addAll(getWhereClauseItemsLong(oldserviceorderbookingid, "oldServiceOrderBooking.id"));
        items.addAll(getWhereClauseItemsString(orderdate, "orderDate"));
        items.addAll(getWhereClauseItemsBoolean(paid, "paid"));
        items.addAll(getWhereClauseItemsString(roundingvalue, "roundingValue"));
        items.addAll(getWhereClauseItemsString(sapnumber, "sapNumber"));
        items.addAll(getWhereClauseItemsString(sapnumbernext, "sapNumberNext"));
        items.addAll(getWhereClauseItemsString(subtotal, "subTotal"));
        items.addAll(getWhereClauseItemsString(tax, "tax"));
        items.addAll(getWhereClauseItemsString(total, "total"));
        items.addAll(getWhereClauseItemsString(totalcharges, "totalCharges"));
        items.addAll(getWhereClauseItemsString(billingaddress, "billingAddress"));
        items.addAll(getWhereClauseItemsString(billingaddresscity, "billingAddressCity"));
        items.addAll(getWhereClauseItemsLong(billingaddresscountryid, "billingAddressCountry.id"));
        items.addAll(getWhereClauseItemsString(billingaddressstreet, "billingAddressStreet"));
        items.addAll(getWhereClauseItemsString(billingaddresssupplement, "billingAddressSupplement"));
        items.addAll(getWhereClauseItemsString(billingaddresszip, "billingAddressZip"));
        items.addAll(getWhereClauseItemsString(billingcustomerfirstname, "billingCustomerFirstName"));
        items.addAll(getWhereClauseItemsString(billingcustomerlastname, "billingCustomerLastName"));
        items.addAll(getWhereClauseItemsString(billingcustomername, "billingCustomerName"));
        items.addAll(getWhereClauseItemsString(billingcustomertitle, "billingCustomerTitle"));
        items.addAll(getWhereClauseItemsString(billingemail, "billingEmail"));
        items.addAll(getWhereClauseItemsString(referencenumber, "referenceNumber"));
        items.addAll(getWhereClauseItemsString(vatnumber, "vatNumber"));
        return items;
    }
}
