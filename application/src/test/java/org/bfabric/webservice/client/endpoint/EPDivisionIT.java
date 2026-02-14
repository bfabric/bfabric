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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDivision;
import org.bfabric.xml.entity.XMLCompany;
import org.bfabric.xml.entity.XMLDivision;
import org.bfabric.xml.entity.XMLOrganizationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPDivisionIT extends AbstractIT {

    public static XMLDivision createDivision() {
        XMLRequestParameterSaveDivision xmlRequestSaveDivision = new XMLRequestParameterSaveDivision();

        XMLCompany xmlRequestSaveCompany = EPCompanyIT.createCompany();

        xmlRequestSaveDivision.setName(GENERATED_NAME);
        xmlRequestSaveDivision.setCompanyid(xmlRequestSaveCompany.getId().toString());

        XMLDivision division = getSoapClient().getEpDivision().getWmSave().save(xmlRequestSaveDivision);

        if (division.getErrorreport() != null) {
            throw new SoapClientException("Could not create division: " + division.getErrorreport());
        }
        return division;
    }

    @Test
    public void crudTest() {
        XMLDivision division = createDivision();
        XMLRequestParameterSaveDivision xmlRequestSaveDivision = new XMLRequestParameterSaveDivision();

        xmlRequestSaveDivision.setId(division.getId());
        xmlRequestSaveDivision.setName(GENERATED_NAME_NEW);

        XMLDivision updateDivision = getSoapClient().getEpDivision().getWmSave().save(xmlRequestSaveDivision);
        updateDivision = getSoapClient().getEpDivision().getWmRead().getEntity(updateDivision.getId());

        Assertions.assertEquals(GENERATED_NAME_NEW, updateDivision.getName());

        deleteDivision(updateDivision.getId());
        deleteDivision(division);
    }

    public void deleteDivision(XMLDivision division) {
        XMLCompany company = getSoapClient().getEpCompany().getWmRead().getEntity(division.getCompany().getId());
        deleteDivision(division.getId());
        XMLOrganizationType organizationType = getSoapClient().getEpOrganizationType().getWmRead().getEntity(company.getOrganizationtype().getId());

        EPCompanyIT.deleteCompany(company.getId());
        EPOrganizationTypeIT.deleteOrganizationType(organizationType.getId());
    }

    public void deleteDivision(Long id) {
        getSoapClient().getEpDivision().getWmDelete().delete(id);
    }
}
