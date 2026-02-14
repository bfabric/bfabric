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

import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstrumentReservation;
import org.bfabric.xml.entity.XMLInstrumentReservation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EPInstrumentReservationIT extends AbstractIT {

    private static final String INSTRUMENT_ID = "250";

    private static final String USERID = "6";

    private static final String BOOKERIDADMIN = "703";

    private static final String TYPE = "1";

    private static final String CHARGEABLE = "true";

    private static final String MAILNOTIFICATION = "true";

    private static final String STARTDATE = "2020-01-01 00:00";

    private static final String ENDDATE = "2020-01-02 00:00";

    private static final String REPEATINGFREQUENCY = "1";

    private static final String REPEATINGUNTIL = "2020-01-02";

    private static final String DESCRIPTION = "API created IR for integration test purposes";

    private static final String SERVICE_TYPE_ID = "1";

    @BeforeAll
    public static void init() {
    }

    @Test
    public void GivenStartAndEndDateInstrumentReservationShouldBeCreated() {
        XMLInstrumentReservation instrumentReservation = createInstrumentReservation();

        Assertions.assertNotNull(instrumentReservation.getId());
        Assertions.assertNull(instrumentReservation.getErrorreport());

        deleteInstrumentReservation(instrumentReservation.getId());
    }

    public XMLInstrumentReservation createInstrumentReservation() {
        XMLRequestParameterSaveInstrumentReservation xmlRequestSaveInstrumentReservation = new XMLRequestParameterSaveInstrumentReservation();
        xmlRequestSaveInstrumentReservation.setDescription(DESCRIPTION);
        xmlRequestSaveInstrumentReservation.setInstrumentid(INSTRUMENT_ID);
        xmlRequestSaveInstrumentReservation.setTypeid(TYPE);
        xmlRequestSaveInstrumentReservation.setBookerid(BOOKERIDADMIN);
        xmlRequestSaveInstrumentReservation.setServicetypeid(SERVICE_TYPE_ID);
        xmlRequestSaveInstrumentReservation.setUserid(USERID);
        xmlRequestSaveInstrumentReservation.setChargeable(CHARGEABLE);
        xmlRequestSaveInstrumentReservation.setSendmailnotification(MAILNOTIFICATION);
        xmlRequestSaveInstrumentReservation.setStartdate(STARTDATE);
        xmlRequestSaveInstrumentReservation.setEnddate(ENDDATE);
        xmlRequestSaveInstrumentReservation.setRepeatinguntil(REPEATINGUNTIL);
        xmlRequestSaveInstrumentReservation.setRepeatingfrequency(REPEATINGFREQUENCY);

        XMLInstrumentReservation instrumentReservation = getSoapClient().getEpInstrumentReservation().getWmSave().save(xmlRequestSaveInstrumentReservation);

        if (instrumentReservation.getErrorreport() != null) {
            throw new SoapClientException("Could not create instrumentReservation: " + instrumentReservation.getErrorreport());
        }

        return instrumentReservation;
    }

    public void deleteInstrumentReservation(Long id) {
        getSoapClient().getEpInstrumentReservation().getWmDelete().delete(id);
    }

    @Test
    public void instrumentReservationShouldBeRead() {
        XMLInstrumentReservation instrumentReservation = createInstrumentReservation();

        XMLInstrumentReservation readInstrumentReservation = getSoapClient().getEpInstrumentReservation().getWmRead().getEntity(instrumentReservation.getId());

        Assertions.assertNotNull(readInstrumentReservation);

        Assertions.assertSame(readInstrumentReservation, instrumentReservation);

        Assertions.assertNull(readInstrumentReservation.getErrorreport());
        Assertions.assertNotNull(readInstrumentReservation.getDescription());
        Assertions.assertNotNull(readInstrumentReservation.getBooker());
        Assertions.assertNotNull(readInstrumentReservation.getChargeable());
        Assertions.assertNotNull(readInstrumentReservation.getDuration());
        Assertions.assertNotNull(readInstrumentReservation.getEnddate());
        Assertions.assertNotNull(readInstrumentReservation.getInstrument());
        Assertions.assertNotNull(readInstrumentReservation.getStartdate());
        Assertions.assertNotNull(readInstrumentReservation.getType());
        Assertions.assertNotNull(readInstrumentReservation.getCharge());
        Assertions.assertNotNull(readInstrumentReservation.getContainer());

        Assertions.assertEquals(instrumentReservation.getDescription(), readInstrumentReservation.getDescription());
        Assertions.assertEquals(instrumentReservation.getBooker().getId(), readInstrumentReservation.getBooker().getId());
        Assertions.assertEquals(instrumentReservation.getChargeable(), readInstrumentReservation.getChargeable());
        Assertions.assertEquals(instrumentReservation.getDuration(), readInstrumentReservation.getDuration());
        Assertions.assertEquals(instrumentReservation.getEnddate(), readInstrumentReservation.getEnddate());
        Assertions.assertEquals(instrumentReservation.getRepeatinguntil(), readInstrumentReservation.getRepeatinguntil());
        Assertions.assertEquals(instrumentReservation.getStartdate(), readInstrumentReservation.getStartdate());
        Assertions.assertEquals(instrumentReservation.getType(), readInstrumentReservation.getType());

        deleteInstrumentReservation(readInstrumentReservation.getId());
    }
}
