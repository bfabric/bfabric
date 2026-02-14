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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOfferedCharge;
import org.bfabric.xml.entity.XMLOffer;
import org.bfabric.xml.entity.XMLOfferedCharge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPOfferedChargeIT extends AbstractIT {

    public static XMLOfferedCharge createOfferedCharge() {
        XMLRequestParameterSaveOfferedCharge xmlRequestSaveOfferedCharge = new XMLRequestParameterSaveOfferedCharge();

        XMLOffer xmlOffer = EPOfferIT.createOffer();

        xmlRequestSaveOfferedCharge.setOrganizationtypeid(xmlOffer.getOrganizationtype().getId().toString());
        xmlRequestSaveOfferedCharge.setServiceid(SERVICE_ID);
        xmlRequestSaveOfferedCharge.setOfferid(xmlOffer.getId().toString());
        xmlRequestSaveOfferedCharge.setChargerid(CHARGER_ID);
        xmlRequestSaveOfferedCharge.setTaxtype(TAX_TYPE_ID);

        XMLOfferedCharge offeredCharge = getSoapClient().getEpOfferedCharge().getWmSave().save(xmlRequestSaveOfferedCharge);

        if (offeredCharge.getErrorreport() != null) {
            throw new SoapClientException("Could not create offeredcharge: " + offeredCharge.getErrorreport());
        }
        return offeredCharge;
    }

    public static void deleteOfferedCharge(Long id) {
        getSoapClient().getEpOfferedCharge().getWmDelete().delete(id);
    }

    public static void deleteOfferedCharge(XMLOfferedCharge offeredCharge) {
        XMLOffer offer = getSoapClient().getEpOffer().getWmRead().getEntity(Long.valueOf(offeredCharge.getOfferid()));
        deleteOfferedCharge(offeredCharge.getId());
        EPOfferIT.deleteOffer(offer);
    }

    @Test
    public void crudTest() {
        XMLOfferedCharge offeredCharge = createOfferedCharge();
        XMLRequestParameterSaveOfferedCharge xmlRequestSaveOfferedCharge = new XMLRequestParameterSaveOfferedCharge();

        xmlRequestSaveOfferedCharge.setId(offeredCharge.getId());
        xmlRequestSaveOfferedCharge.setServiceid(SERVICE_ID_NEW);
        xmlRequestSaveOfferedCharge.setChargerid(USER_ID);

        XMLOfferedCharge updateOfferedCharge = getSoapClient().getEpOfferedCharge().getWmSave().save(xmlRequestSaveOfferedCharge);
        updateOfferedCharge = getSoapClient().getEpOfferedCharge().getWmRead().getEntity(updateOfferedCharge.getId());

        Assertions.assertEquals(SERVICE_ID_NEW, updateOfferedCharge.getServiceid());
        Assertions.assertEquals(USER_ID, updateOfferedCharge.getCharger().getIdString());

        deleteOfferedCharge(updateOfferedCharge);
    }
}
