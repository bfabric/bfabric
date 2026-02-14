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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDepartment;
import org.bfabric.xml.entity.XMLDepartment;
import org.bfabric.xml.entity.XMLOrganization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPDepartmentIT extends AbstractIT {

    public static XMLDepartment createDepartment() {
        XMLRequestParameterSaveDepartment xmlRequestSaveDepartment = new XMLRequestParameterSaveDepartment();

        XMLOrganization xmlRequestSaveOrganization = EPOrganizationIT.createOrganization();
        xmlRequestSaveDepartment.setName(GENERATED_NAME);
        xmlRequestSaveDepartment.setOrganizationid(xmlRequestSaveOrganization.getId().toString());

        XMLDepartment department = getSoapClient().getEpDepartment().getWmSave().save(xmlRequestSaveDepartment);

        if (department.getErrorreport() != null) {
            throw new SoapClientException("Could not create department: " + department.getErrorreport());
        }
        return department;
    }

    public static void deleteDepartment(Long id) {
        getSoapClient().getEpDepartment().getWmDelete().delete(id);
    }

    public static void deleteDepartment(XMLDepartment department) {
        XMLOrganization organization = getSoapClient().getEpOrganization().getWmRead().getEntity(department.getOrganization().getId());
        deleteDepartment(department.getId());
        EPOrganizationIT.deleteOrganization(organization);
    }

    @Test
    public void departmentShouldBeCreated() {
        XMLDepartment department = createDepartment();

        Assertions.assertNull(department.getErrorreport());
        Assertions.assertNotNull(department.getId());
        Assertions.assertNotNull(department.getName());
        Assertions.assertNotNull(department.getOrganization());

        Assertions.assertEquals(GENERATED_NAME, department.getName());
        deleteDepartment(department);
    }

    @Test
    public void departmentShouldBeRead() {
        XMLDepartment department = createDepartment();

        XMLDepartment readDepartment = getSoapClient().getEpDepartment().getWmRead().getEntity(department.getId());
        XMLDepartment readDepartmentDuplicate = getSoapClient().getEpDepartment().getWmRead().getEntity(department.getId());

        Assertions.assertNotNull(readDepartment);
        Assertions.assertNotNull(readDepartmentDuplicate);

        Assertions.assertNull(readDepartment.getErrorreport());

        Assertions.assertEquals(readDepartmentDuplicate.getName(), readDepartment.getName());
        Assertions.assertEquals(readDepartmentDuplicate.getOrganization(), readDepartment.getOrganization());

        deleteDepartment(department);
    }

    @Test
    public void departmentShouldBeUpdated() {
        XMLDepartment department = createDepartment();
        XMLRequestParameterSaveDepartment xmlRequestSaveDepartment = new XMLRequestParameterSaveDepartment();

        xmlRequestSaveDepartment.setId(department.getId());
        xmlRequestSaveDepartment.setName(GENERATED_NAME_NEW);

        XMLDepartment updatedDepartment = getSoapClient().getEpDepartment().getWmSave().save(xmlRequestSaveDepartment);

        Assertions.assertNull(updatedDepartment.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME_NEW, updatedDepartment.getName());

        deleteDepartment(department);
    }

    @Test
    public void departmentShouldDeleted() {
        XMLDepartment department = createDepartment();

        XMLDepartment deleteDepartment = getSoapClient().getEpDepartment().getWmDelete().delete(department.getId());

        Assertions.assertNull(deleteDepartment.getErrorreport());
        Assertions.assertNull(deleteDepartment.getId());

        deleteDepartment(department);
    }
}
