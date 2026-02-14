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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOrganization;
import org.bfabric.xml.entity.XMLOrganization;
import org.bfabric.xml.entity.XMLOrganizationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPOrganizationIT extends AbstractIT {

    public static XMLOrganization createOrganization() {
        XMLRequestParameterSaveOrganization xmlRequestSaveOrganization = new XMLRequestParameterSaveOrganization();

        XMLOrganizationType xmlRequestSaveOrganizationType = EPOrganizationTypeIT.createOrganizationType();
        xmlRequestSaveOrganization.setName(GENERATED_NAME);
        xmlRequestSaveOrganization.setVatnumber(S3);
        xmlRequestSaveOrganization.setDebitornumber(L1);
        xmlRequestSaveOrganization.setDefaultbookingtypeid("1");
        xmlRequestSaveOrganization.setOrganizationtypeid(xmlRequestSaveOrganizationType.getId().toString());

        XMLOrganization organization = getSoapClient().getEpOrganization().getWmSave().save(xmlRequestSaveOrganization);

        if (organization.getErrorreport() != null) {
            throw new SoapClientException("Could not create organization: " + organization.getErrorreport());
        }
        return organization;
    }

    public static void deleteOrganization(Long id) {
        getSoapClient().getEpOrganization().getWmDelete().delete(id);
    }

    public static void deleteOrganization(XMLOrganization organization) {
        deleteOrganization(organization.getId());
        EPOrganizationTypeIT.deleteOrganizationType(organization.getOrganizationtype().getId());
    }

    @Test
    public void organizationShouldBeCreated() {
        XMLOrganization organization = createOrganization();

        Assertions.assertNull(organization.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, organization.getName());
        Assertions.assertEquals(S3, organization.getVatnumber());
        Assertions.assertEquals(L1, organization.getDebitornumber());
        Assertions.assertEquals("1", organization.getDefaultbookingtype());
        Assertions.assertEquals(Boolean.TRUE.toString(), organization.getExtensible().toString());

        deleteOrganization(organization);
    }

    @Test
    public void organizationShouldBeRead() {
        XMLOrganization organization = createOrganization();

        XMLOrganization readOrganization = getSoapClient().getEpOrganization().getWmRead().getEntity(organization.getId());
        XMLOrganization readOrganizationDuplicate = getSoapClient().getEpOrganization().getWmRead().getEntity(organization.getId());

        Assertions.assertNull(readOrganization.getErrorreport());

        Assertions.assertEquals(readOrganizationDuplicate.getName(), readOrganization.getName());
        Assertions.assertEquals(readOrganizationDuplicate.getVatnumber(), readOrganization.getVatnumber());
        Assertions.assertEquals(readOrganizationDuplicate.getDebitornumber(), readOrganization.getDebitornumber());
        Assertions.assertEquals(readOrganizationDuplicate.getOrganizationtype(), readOrganization.getOrganizationtype());
        Assertions.assertEquals(readOrganizationDuplicate.getDefaultbookingtype(), readOrganization.getDefaultbookingtype());
        Assertions.assertEquals(readOrganizationDuplicate.getExtensible().toString(), organization.getExtensible().toString());

        deleteOrganization(organization);
    }

    @Test
    public void organizationShouldBeUpdated() {
        XMLOrganization organization = createOrganization();
        XMLRequestParameterSaveOrganization xmlRequestSaveOrganization = new XMLRequestParameterSaveOrganization();

        xmlRequestSaveOrganization.setId(organization.getId());
        xmlRequestSaveOrganization.setName(GENERATED_NAME_NEW);
        xmlRequestSaveOrganization.setDefaultbookingtypeid("2");

        XMLOrganization updatedOrganization = getSoapClient().getEpOrganization().getWmSave().save(xmlRequestSaveOrganization);

        Assertions.assertNull(updatedOrganization.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME_NEW, updatedOrganization.getName());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedOrganization.getExtensible().toString());
        Assertions.assertEquals("2", updatedOrganization.getDefaultbookingtype());

        deleteOrganization(organization);
    }

    @Test
    public void organizationShouldDeleted() {
        XMLOrganization organization = createOrganization();

        XMLOrganization deleteOrganization = getSoapClient().getEpOrganization().getWmDelete().delete(organization.getId());

        Assertions.assertNull(deleteOrganization.getErrorreport());
        Assertions.assertNull(deleteOrganization.getId());

        deleteOrganization(organization);
    }
}
