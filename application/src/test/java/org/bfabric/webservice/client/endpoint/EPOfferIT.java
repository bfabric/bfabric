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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOffer;
import org.bfabric.xml.entity.XMLOffer;
import org.bfabric.xml.entity.XMLOrganizationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPOfferIT extends AbstractIT {

    public static XMLOffer createOffer() {
        XMLRequestParameterSaveOffer xmlRequestSaveOffer = new XMLRequestParameterSaveOffer();

        xmlRequestSaveOffer.setCoachid(USER_ID);
        xmlRequestSaveOffer.setCoachbackupid(USER_ID);
        xmlRequestSaveOffer.setOrganizationtypeid(ORGANIZATION_TYPE);
        xmlRequestSaveOffer.setRequesterid(USER_ID);
        xmlRequestSaveOffer.setRequestername(S5);
        xmlRequestSaveOffer.setRequesteraddress(S5);
        xmlRequestSaveOffer.setDescription(S5);
        xmlRequestSaveOffer.setLocked(Boolean.FALSE.toString());

        XMLOffer offer = getSoapClient().getEpOffer().getWmSave().save(xmlRequestSaveOffer);

        if (offer.getErrorreport() != null) {
            throw new SoapClientException("Could not create offer: " + offer.getErrorreport());
        }
        return offer;
    }

    public static void deleteOffer(Long id) {
        getSoapClient().getEpOffer().getWmDelete().delete(id);
    }

    public static void deleteOffer(XMLOffer offer) {
        XMLOrganizationType organizationType = getSoapClient().getEpOrganizationType().getWmRead().getEntity(offer.getOrganizationtype().getId());

        deleteOffer(offer.getId());
        EPOrganizationTypeIT.deleteOrganizationType(organizationType.getId());
    }

    @Test
    public void crudTest() {
        XMLOffer offer = createOffer();
        XMLRequestParameterSaveOffer xmlRequestSaveOffer = new XMLRequestParameterSaveOffer();

        xmlRequestSaveOffer.setId(offer.getId());
        xmlRequestSaveOffer.setOrganizationtypeid(ORGANIZATION_TYPE_NEW);
        xmlRequestSaveOffer.setCoachid(USER_NEW);
        xmlRequestSaveOffer.setCoachbackupid(USER_NEW);
        xmlRequestSaveOffer.setRequesterid(USER_NEW);

        XMLOffer updateOffer = getSoapClient().getEpOffer().getWmSave().save(xmlRequestSaveOffer);
        updateOffer = getSoapClient().getEpOffer().getWmRead().getEntity(updateOffer.getId());

        Assertions.assertEquals(ORGANIZATION_TYPE_NEW, updateOffer.getOrganizationtype().getIdString());
        Assertions.assertEquals(USER_NEW, updateOffer.getCoach().getIdString());
        Assertions.assertEquals(USER_NEW, updateOffer.getCoachbackup().getIdString());
        Assertions.assertEquals(USER_NEW, updateOffer.getRequester().getIdString());

        deleteOffer(updateOffer.getId());
        deleteOffer(offer.getId());
    }
}