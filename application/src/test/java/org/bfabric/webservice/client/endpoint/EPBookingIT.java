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

package org.bfabric.webservice.client.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveBooking;
import org.bfabric.xml.entity.XMLBooking;
import org.bfabric.xml.entity.XMLCharge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPBookingIT extends AbstractIT {

    protected static final String ACCOUNT_ID = "6";

    protected static final String BOOKING_NUMBER = "10000000";

    protected static final String BOOKING_DATE = "2023-05-12";

    protected static final String ONE = "1";

    protected static final String COUNTRY_ID = "MK";

    protected static final String START_DATE = "2023-05-13";

    protected static final String END_DATE = "2023-05-18";

    public static XMLBooking createBooking() {
        XMLRequestParameterSaveBooking xmlRequestSaveBooking = new XMLRequestParameterSaveBooking();

        xmlRequestSaveBooking.setName(ONE);
        xmlRequestSaveBooking.setContainerid(CONTAINER_ID);
        xmlRequestSaveBooking.setAccountid(ACCOUNT_ID);
        xmlRequestSaveBooking.setBookingnr(BOOKING_NUMBER);
        xmlRequestSaveBooking.setBookingdate(BOOKING_DATE);
        xmlRequestSaveBooking.setBookingissuerid(USER_ID);
        xmlRequestSaveBooking.setCostcentreid(ONE);
        xmlRequestSaveBooking.setCurrencyid(ONE);
        xmlRequestSaveBooking.setExecutionperiodenddate(END_DATE);
        xmlRequestSaveBooking.setExecutionperiodstartdate(START_DATE);
        xmlRequestSaveBooking.setFinancialcenterid(ONE);
        xmlRequestSaveBooking.setOrderdate(START_DATE);
        xmlRequestSaveBooking.setRoundingvalue(ONE);
        xmlRequestSaveBooking.setSubtotal(ONE);
        xmlRequestSaveBooking.setTax(ONE);
        xmlRequestSaveBooking.setTotal(ONE);
        xmlRequestSaveBooking.setTotalcharges(ONE);
        xmlRequestSaveBooking.setInstituteid(ONE);
        xmlRequestSaveBooking.setBillingaddresscountryid(COUNTRY_ID);
        xmlRequestSaveBooking.setBillingaddresscity(S5);
        xmlRequestSaveBooking.setBillingaddressstreet(S5);
        xmlRequestSaveBooking.setBillingaddresssupplement(S5);
        xmlRequestSaveBooking.setBillingaddresszip(S5);
        xmlRequestSaveBooking.setBillingcustomerfirstname(S5);
        xmlRequestSaveBooking.setBillingcustomerlastname(S5);
        xmlRequestSaveBooking.setBillingcustomertitle(S5);
        xmlRequestSaveBooking.setBillingemail(S5);
        xmlRequestSaveBooking.setReferencenumber(S3);
        xmlRequestSaveBooking.setVatnumber(S3);

        XMLCharge xmlCharge1 = EPChargeIT.createCharge();
        XMLCharge xmlCharge2 = EPChargeIT.createCharge();
        List<String> charges = new ArrayList<>();
        charges.add(xmlCharge1.getIdString());
        charges.add(xmlCharge2.getIdString());
        xmlRequestSaveBooking.setCharges(charges);

        XMLBooking booking = getSoapClient().getEpBooking().getWmSave().save(xmlRequestSaveBooking);

        if (booking.getErrorreport() != null) {
            throw new SoapClientException("Could not create booking: " + booking.getErrorreport());
        }
        return booking;
    }

    public static void deleteBooking(Long id) {
        XMLBooking deleteBooking = getSoapClient().getEpBooking().getWmDelete().delete(id);

        Assertions.assertNull(deleteBooking.getErrorreport());
        Assertions.assertNull(deleteBooking.getId());
    }

    @Test
    public void crudTest() {
        XMLBooking booking = createBooking();

        XMLRequestParameterSaveBooking xmlRequestSaveBooking = new XMLRequestParameterSaveBooking();

        xmlRequestSaveBooking.setId(booking.getId());
        xmlRequestSaveBooking.setName(ONE);
        xmlRequestSaveBooking.setContainerid(CONTAINER_ID);
        xmlRequestSaveBooking.setAccountid(ACCOUNT_ID);
        xmlRequestSaveBooking.setBookingnr(BOOKING_NUMBER);
        xmlRequestSaveBooking.setBookingdate(BOOKING_DATE);
        xmlRequestSaveBooking.setBookingissuerid(USER_ID);
        xmlRequestSaveBooking.setCostcentreid(ONE);
        xmlRequestSaveBooking.setCurrencyid(ONE);
        xmlRequestSaveBooking.setExecutionperiodenddate(END_DATE);
        xmlRequestSaveBooking.setExecutionperiodstartdate(START_DATE);
        xmlRequestSaveBooking.setFinancialcenterid(ONE);
        xmlRequestSaveBooking.setOrderdate(START_DATE);
        xmlRequestSaveBooking.setRoundingvalue(ONE);
        xmlRequestSaveBooking.setSubtotal(ONE);
        xmlRequestSaveBooking.setTax(ONE);
        xmlRequestSaveBooking.setTotal(ONE);
        xmlRequestSaveBooking.setTotalcharges(ONE);
        xmlRequestSaveBooking.setInstituteid(ONE);
        xmlRequestSaveBooking.setBillingaddresscountryid(COUNTRY_ID);
        xmlRequestSaveBooking.setBillingaddresscity(S5);
        xmlRequestSaveBooking.setBillingaddressstreet(S5);
        xmlRequestSaveBooking.setBillingaddresssupplement(S5);
        xmlRequestSaveBooking.setBillingaddresszip(S5);
        xmlRequestSaveBooking.setBillingcustomerfirstname(S5);
        xmlRequestSaveBooking.setBillingcustomerlastname(S5);
        xmlRequestSaveBooking.setBillingcustomertitle(S5);
        xmlRequestSaveBooking.setBillingemail(S5);
        xmlRequestSaveBooking.setReferencenumber(S3);
        xmlRequestSaveBooking.setVatnumber(S3);

        for (XMLCharge xmlCharge : booking.getCharge()) {
            EPChargeIT.deleteCharge(xmlCharge.getId());
        }
        XMLCharge xmlCharge1 = EPChargeIT.createCharge();
        List<String> charges = new ArrayList<>();
        charges.add(xmlCharge1.getIdString());
        xmlRequestSaveBooking.setCharges(charges);

        XMLBooking updateBooking = getSoapClient().getEpBooking().getWmSave().save(xmlRequestSaveBooking);
        updateBooking = getSoapClient().getEpBooking().getWmRead().getEntity(updateBooking.getId());

        Assertions.assertEquals(CONTAINER_ID, updateBooking.getContainer().getIdString());
        Assertions.assertEquals(BOOKING_NUMBER, updateBooking.getBookingnr().toString());
        Assertions.assertEquals(BOOKING_DATE, updateBooking.getBookingdate());
        Assertions.assertEquals(USER_ID, updateBooking.getBookingissuer().getIdString());
        Assertions.assertEquals(ONE, updateBooking.getCostcentreid());
        Assertions.assertEquals(ONE, updateBooking.getCurrencyid());
        Assertions.assertEquals(END_DATE, updateBooking.getExecutionperiodenddate());
        Assertions.assertEquals(START_DATE, updateBooking.getExecutionperiodstartdate());
        Assertions.assertEquals(ONE, updateBooking.getFinancialcenterid());
        Assertions.assertEquals(START_DATE, updateBooking.getOrderdate());
        Assertions.assertEquals(ONE, updateBooking.getRoundingvalue().toString());
        Assertions.assertEquals(ONE, updateBooking.getSubtotal().toString());
        Assertions.assertEquals(ONE, updateBooking.getTax().toString());
        Assertions.assertEquals(ONE, updateBooking.getTotal().toString());
        Assertions.assertEquals(ONE, updateBooking.getTotalcharges().toString());
        Assertions.assertEquals(ONE, updateBooking.getInstitute().getIdString());
        Assertions.assertEquals(COUNTRY_ID, updateBooking.getBillingaddresscountryid());
        Assertions.assertEquals(S5, updateBooking.getBillingaddresscity());
        Assertions.assertEquals(S5, updateBooking.getBillingaddressstreet());
        Assertions.assertEquals(S5, updateBooking.getBillingaddresssupplement());
        Assertions.assertEquals(S5, updateBooking.getBillingaddresszip());
        Assertions.assertEquals(S5, updateBooking.getBillingcustomerfirstname());
        Assertions.assertEquals(S5, updateBooking.getBillingcustomerlastname());
        Assertions.assertEquals(S5, updateBooking.getBillingcustomertitle());
        Assertions.assertEquals(S5, updateBooking.getBillingemail());
        Assertions.assertEquals(S3, updateBooking.getVatnumber());
        Assertions.assertEquals(S3, updateBooking.getReferencenumber());
        Assertions.assertEquals(charges.size(), updateBooking.getCharge().size());
        Assertions.assertEquals(charges.get(0), updateBooking.getCharge().get(0).getId().toString());

        for (XMLCharge xmlCharge : updateBooking.getCharge()) {
            EPChargeIT.deleteCharge(xmlCharge.getId());
        }
        deleteBooking(updateBooking.getId());
    }
}
