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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCompany;
import org.bfabric.xml.entity.XMLCompany;
import org.bfabric.xml.entity.XMLOrganizationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPCompanyIT extends AbstractIT {

    public static XMLCompany createCompany() {
        XMLRequestParameterSaveCompany xmlRequestSaveCompany = new XMLRequestParameterSaveCompany();

        XMLOrganizationType xmlRequestSaveOrganizationType = EPOrganizationTypeIT.createOrganizationType();

        xmlRequestSaveCompany.setName(GENERATED_NAME);
        xmlRequestSaveCompany.setVatnumber(S3);
        xmlRequestSaveCompany.setDebitornumber(L1);
        xmlRequestSaveCompany.setDefaultbookingtypeid("1");
        xmlRequestSaveCompany.setOrganizationtypeid(xmlRequestSaveOrganizationType.getId().toString());

        XMLCompany company = getSoapClient().getEpCompany().getWmSave().save(xmlRequestSaveCompany);

        if (company.getErrorreport() != null) {
            throw new SoapClientException("Could not create company: " + company.getErrorreport());
        }
        return company;
    }

    public static void deleteCompany(Long id) {
        getSoapClient().getEpCompany().getWmDelete().delete(id);
    }

    @Test
    public void companyShouldBeCreated() {
        XMLCompany company = createCompany();

        Assertions.assertNull(company.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, company.getName());
        Assertions.assertEquals(S3, company.getVatnumber());
        Assertions.assertEquals(L1, company.getDebitornumber());
        Assertions.assertEquals("1", company.getDefaultbookingtype());

        deleteCompany(company.getId());
        EPOrganizationTypeIT.deleteOrganizationType(company.getOrganizationtype().getId());
    }

    @Test
    public void companyShouldBeRead() {
        XMLCompany company = createCompany();

        XMLCompany readCompany = getSoapClient().getEpCompany().getWmRead().getEntity(company.getId());
        XMLCompany readCompanyDuplicate = getSoapClient().getEpCompany().getWmRead().getEntity(company.getId());

        Assertions.assertNull(readCompany.getErrorreport());

        Assertions.assertEquals(readCompanyDuplicate.getName(), readCompany.getName());
        Assertions.assertEquals(readCompanyDuplicate.getVatnumber(), readCompany.getVatnumber());
        Assertions.assertEquals(readCompanyDuplicate.getDebitornumber(), readCompany.getDebitornumber());
        Assertions.assertEquals(readCompanyDuplicate.getOrganizationtype(), readCompany.getOrganizationtype());
        Assertions.assertEquals(readCompanyDuplicate.getDefaultbookingtype(), readCompany.getDefaultbookingtype());

        deleteCompany(readCompany.getId());
        EPOrganizationTypeIT.deleteOrganizationType(company.getOrganizationtype().getId());
    }

    @Test
    public void companyShouldBeUpdated() {
        XMLCompany company = createCompany();
        XMLRequestParameterSaveCompany xmlRequestSaveCompany = new XMLRequestParameterSaveCompany();

        xmlRequestSaveCompany.setId(company.getId());
        xmlRequestSaveCompany.setName(GENERATED_NAME_NEW);
        xmlRequestSaveCompany.setDefaultbookingtypeid("2");

        XMLCompany updateCompany = getSoapClient().getEpCompany().getWmSave().save(xmlRequestSaveCompany);

        Assertions.assertNull(updateCompany.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME_NEW, updateCompany.getName());
        Assertions.assertEquals("2", updateCompany.getDefaultbookingtype());

        deleteCompany(updateCompany.getId());
        EPOrganizationTypeIT.deleteOrganizationType(company.getOrganizationtype().getId());
    }

    @Test
    public void companyShouldDeleted() {
        XMLCompany company = createCompany();

        XMLCompany deleteCompany = getSoapClient().getEpCompany().getWmDelete().delete(company.getId());

        Assertions.assertNull(deleteCompany.getErrorreport());
        Assertions.assertNull(deleteCompany.getId());

        EPOrganizationTypeIT.deleteOrganizationType(company.getOrganizationtype().getId());
    }
}
