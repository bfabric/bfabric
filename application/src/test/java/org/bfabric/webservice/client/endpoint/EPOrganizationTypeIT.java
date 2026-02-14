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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOrganizationType;
import org.bfabric.xml.entity.XMLOrganizationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPOrganizationTypeIT extends AbstractIT {

    public static XMLOrganizationType createOrganizationType() {
        XMLRequestParameterSaveOrganizationType xmlRequestSaveOrganizationType = new XMLRequestParameterSaveOrganizationType();

        xmlRequestSaveOrganizationType.setName(GENERATED_NAME);
        xmlRequestSaveOrganizationType.setAcademic(Boolean.FALSE.toString());
        xmlRequestSaveOrganizationType.setDomestic(Boolean.FALSE.toString());
        xmlRequestSaveOrganizationType.setExtensible(Boolean.FALSE.toString());
        xmlRequestSaveOrganizationType.setColor(COLOR);

        XMLOrganizationType organizationType = getSoapClient().getEpOrganizationType().getWmSave().save(xmlRequestSaveOrganizationType);

        if (organizationType.getErrorreport() != null) {
            throw new SoapClientException("Could not create organization type: " + organizationType.getErrorreport());
        }
        return organizationType;
    }

    public static void deleteOrganizationType(Long id) {
        getSoapClient().getEpOrganizationType().getWmDelete().delete(id);
    }

    @Test
    public void organizationTypeShouldBeCreated() {
        XMLOrganizationType organizationType = createOrganizationType();

        Assertions.assertNull(organizationType.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, organizationType.getName());
        Assertions.assertEquals(Boolean.FALSE.toString(), organizationType.getAcademic().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), organizationType.getDomestic().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), organizationType.getExtensible().toString());
        Assertions.assertEquals(COLOR, organizationType.getColor());

        deleteOrganizationType(organizationType.getId());
    }

    @Test
    public void organizationTypeShouldBeRead() {
        XMLOrganizationType organizationType = createOrganizationType();

        XMLOrganizationType readOrganizationType = getSoapClient().getEpOrganizationType().getWmRead().getEntity(organizationType.getId());
        XMLOrganizationType readOrganizationTypeDuplicate = getSoapClient().getEpOrganizationType().getWmRead().getEntity(organizationType.getId());

        Assertions.assertNotNull(readOrganizationType);
        Assertions.assertNotNull(readOrganizationTypeDuplicate);

        Assertions.assertNull(readOrganizationType.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, readOrganizationType.getName());
        Assertions.assertEquals(Boolean.FALSE.toString(), readOrganizationType.getAcademic().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), readOrganizationType.getDomestic().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), readOrganizationType.getExtensible().toString());
        Assertions.assertEquals(COLOR, readOrganizationType.getColor());

        deleteOrganizationType(readOrganizationType.getId());
    }

    @Test
    public void organizationTypeShouldBeUpdated() {
        XMLOrganizationType organizationType = createOrganizationType();
        XMLRequestParameterSaveOrganizationType xmlRequestSaveOrganizationType = new XMLRequestParameterSaveOrganizationType();

        xmlRequestSaveOrganizationType.setId(organizationType.getId());

        xmlRequestSaveOrganizationType.setName(GENERATED_NAME_NEW);
        xmlRequestSaveOrganizationType.setAcademic(Boolean.TRUE.toString());
        xmlRequestSaveOrganizationType.setDomestic(Boolean.TRUE.toString());
        xmlRequestSaveOrganizationType.setExtensible(Boolean.TRUE.toString());
        xmlRequestSaveOrganizationType.setColor(COLOR_NEW);

        XMLOrganizationType updatedOrganizationType = getSoapClient().getEpOrganizationType().getWmSave().save(xmlRequestSaveOrganizationType);

        Assertions.assertNull(updatedOrganizationType.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME_NEW, updatedOrganizationType.getName());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedOrganizationType.getAcademic().toString());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedOrganizationType.getExtensible().toString());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedOrganizationType.getDomestic().toString());
        Assertions.assertEquals(COLOR_NEW, updatedOrganizationType.getColor());

        deleteOrganizationType(updatedOrganizationType.getId());
    }

    @Test
    public void organizationTypeShouldDeleted() {
        XMLOrganizationType organizationType = createOrganizationType();

        XMLOrganizationType deleteOrganizationType = getSoapClient().getEpOrganizationType().getWmDelete().delete(organizationType.getId());

        Assertions.assertNull(deleteOrganizationType.getErrorreport());
        Assertions.assertNull(deleteOrganizationType.getId());
    }
}
