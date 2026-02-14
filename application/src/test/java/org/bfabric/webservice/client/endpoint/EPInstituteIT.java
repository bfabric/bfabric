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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstitute;
import org.bfabric.xml.entity.XMLDepartment;
import org.bfabric.xml.entity.XMLInstitute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPInstituteIT extends AbstractIT {

    public static XMLInstitute createInstitute() {
        XMLRequestParameterSaveInstitute xmlRequestSaveInstitute = new XMLRequestParameterSaveInstitute();

        XMLDepartment xmlRequestSaveDepartment = EPDepartmentIT.createDepartment();

        xmlRequestSaveInstitute.setName(GENERATED_NAME);
        xmlRequestSaveInstitute.setDepartmentid(xmlRequestSaveDepartment.getId().toString());

        XMLInstitute institute = getSoapClient().getEpInstitute().getWmSave().save(xmlRequestSaveInstitute);

        if (institute.getErrorreport() != null) {
            throw new SoapClientException("Could not create institute: " + institute.getErrorreport());
        }
        return institute;
    }

    public void deleteInstitute(Long id) {
        getSoapClient().getEpInstitute().getWmDelete().delete(id);
    }

    public void deleteInstitute(XMLInstitute institute) {
        XMLDepartment department = getSoapClient().getEpDepartment().getWmRead().getEntity(institute.getDepartment().getId());
        deleteInstitute(institute.getId());
        EPDepartmentIT.deleteDepartment(department);
    }

    @Test
    public void instituteShouldBeCreated() {
        XMLInstitute institute = createInstitute();

        Assertions.assertNull(institute.getErrorreport());
        Assertions.assertNotNull(institute.getId());
        Assertions.assertNotNull(institute.getName());
        Assertions.assertNotNull(institute.getDepartment());

        Assertions.assertEquals(GENERATED_NAME, institute.getName());

        deleteInstitute(institute);
    }

    @Test
    public void instituteShouldBeRead() {
        XMLInstitute institute = createInstitute();

        XMLInstitute readInstitute = getSoapClient().getEpInstitute().getWmRead().getEntity(institute.getId());
        XMLInstitute readInstituteDuplicate = getSoapClient().getEpInstitute().getWmRead().getEntity(institute.getId());

        Assertions.assertNotNull(readInstitute);
        Assertions.assertNotNull(readInstituteDuplicate);

        Assertions.assertNull(readInstitute.getErrorreport());

        Assertions.assertEquals(readInstituteDuplicate.getName(), readInstitute.getName());
        Assertions.assertEquals(readInstituteDuplicate.getDepartment(), readInstitute.getDepartment());

        deleteInstitute(institute);
    }

    @Test
    public void instituteShouldBeUpdated() {
        XMLInstitute institute = createInstitute();
        XMLRequestParameterSaveInstitute xmlRequestSaveInstitute = new XMLRequestParameterSaveInstitute();

        xmlRequestSaveInstitute.setId(institute.getId());
        xmlRequestSaveInstitute.setName(GENERATED_NAME_NEW);

        XMLInstitute updateInstitute = getSoapClient().getEpInstitute().getWmSave().save(xmlRequestSaveInstitute);

        Assertions.assertNull(updateInstitute.getErrorreport());
        Assertions.assertNotNull(updateInstitute.getName());
        Assertions.assertNotNull(updateInstitute.getId());

        Assertions.assertEquals(GENERATED_NAME_NEW, updateInstitute.getName());

        deleteInstitute(institute);
    }

    @Test
    public void instituteShouldDeleted() {
        XMLInstitute institute = createInstitute();

        XMLInstitute deleteInstitute = getSoapClient().getEpInstitute().getWmDelete().delete(institute.getId());

        Assertions.assertNull(deleteInstitute.getErrorreport());
        Assertions.assertNull(deleteInstitute.getId());

        deleteInstitute(institute);
    }
}
