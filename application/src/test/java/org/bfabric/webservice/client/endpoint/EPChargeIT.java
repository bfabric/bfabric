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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCharge;
import org.bfabric.xml.entity.XMLCharge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPChargeIT extends AbstractIT {

    public static XMLCharge createCharge() {
        XMLRequestParameterSaveCharge xmlRequestSaveCharge = new XMLRequestParameterSaveCharge();

        xmlRequestSaveCharge.setServiceid(SERVICE_ID);
        xmlRequestSaveCharge.setChargerid(USER_ID);
        xmlRequestSaveCharge.setTaxtypeid(TAX_TYPE_ID);
        xmlRequestSaveCharge.setContainerid(CONTAINER_ID);

        XMLCharge charge = getSoapClient().getEpCharge().getWmSave().save(xmlRequestSaveCharge);

        if (charge.getErrorreport() != null) {
            throw new SoapClientException("Could not create charge: " + charge.getErrorreport());
        }
        return charge;
    }

    public static void deleteCharge(Long id) {
        getSoapClient().getEpCharge().getWmDelete().delete(id);
    }

    @Test
    public void crudTest() {
        XMLCharge charge = createCharge();
        XMLRequestParameterSaveCharge xmlRequestSaveCharge = new XMLRequestParameterSaveCharge();

        xmlRequestSaveCharge.setId(charge.getId());
        xmlRequestSaveCharge.setServiceid(SERVICE_ID_NEW);
        xmlRequestSaveCharge.setContainerid(CONTAINER_ID_NEW);

        XMLCharge updateCharge = getSoapClient().getEpCharge().getWmSave().save(xmlRequestSaveCharge);
        updateCharge = getSoapClient().getEpCharge().getWmRead().getEntity(updateCharge.getId());

        Assertions.assertEquals(SERVICE_ID_NEW, updateCharge.getServiceid());
        Assertions.assertEquals(CONTAINER_ID_NEW, updateCharge.getContainer().getIdString());

        deleteCharge(updateCharge.getId());
    }
}
