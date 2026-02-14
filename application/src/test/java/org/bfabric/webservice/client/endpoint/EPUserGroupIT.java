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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUserGroup;
import org.bfabric.xml.entity.XMLUserGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPUserGroupIT extends AbstractIT {

    private static XMLRequestParameterSaveUserGroup getXmlRequestParameterSaveUserGroup(XMLUserGroup userGroup) {
        XMLRequestParameterSaveUserGroup xmlRequestSaveUserGroup = new XMLRequestParameterSaveUserGroup();
        xmlRequestSaveUserGroup.setId(userGroup.getId());
        xmlRequestSaveUserGroup.setName(GENERATED_NAME);
        xmlRequestSaveUserGroup.setDivisionid(DIVISION_ID);
        xmlRequestSaveUserGroup.setHidden(Boolean.TRUE.toString());
        xmlRequestSaveUserGroup.setInternal(Boolean.TRUE.toString());
        xmlRequestSaveUserGroup.setForEmployeesOnly(Boolean.TRUE.toString());
        xmlRequestSaveUserGroup.setInstituteid(INSTITUTE_ID);
        xmlRequestSaveUserGroup.setSupervisorid(USER_ID);
        xmlRequestSaveUserGroup.setUsers(USERS_NEW);
        return xmlRequestSaveUserGroup;
    }

    public XMLUserGroup createUserGroup() {
        XMLRequestParameterSaveUserGroup xmlRequestSaveUserGroup = new XMLRequestParameterSaveUserGroup();

        xmlRequestSaveUserGroup.setName(GENERATED_NAME);
        xmlRequestSaveUserGroup.setDivisionid(DIVISION_ID);
        xmlRequestSaveUserGroup.setHidden(Boolean.FALSE.toString());
        xmlRequestSaveUserGroup.setSupervisorid(USER_ID);

        xmlRequestSaveUserGroup.setInternal(Boolean.FALSE.toString());
        xmlRequestSaveUserGroup.setForEmployeesOnly(Boolean.FALSE.toString());
        xmlRequestSaveUserGroup.setInstituteid(INSTITUTE_ID);
        xmlRequestSaveUserGroup.setUsers(USERS);

        XMLUserGroup userGroup = getSoapClient().getEpUserGroup().getWmSave().save(xmlRequestSaveUserGroup);

        if (userGroup.getErrorreport() != null) {
            throw new SoapClientException("Could not create user group: " + userGroup.getErrorreport());
        }
        return userGroup;
    }

    public void deleteUserGroup(Long id) {
        getSoapClient().getEpUserGroup().getWmDelete().delete(id);
    }

    @Test
    public void userGroupShouldBeCreated() {
        XMLUserGroup userGroup = createUserGroup();

        Assertions.assertNull(userGroup.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, userGroup.getName());
        Assertions.assertEquals(DIVISION_ID, userGroup.getDivision());
        Assertions.assertEquals(Long.valueOf(USER_ID), userGroup.getSupervisor().getId());

        Assertions.assertEquals(Boolean.FALSE.toString(), userGroup.getHidden().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), userGroup.getInternal().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), userGroup.getForemployeesonly().toString());
        Assertions.assertEquals(INSTITUTE_ID, userGroup.getInstitute());
        Assertions.assertEquals(USERS_NEW.get(0), userGroup.getUsers().get(1).getIdString());
        Assertions.assertEquals(USERS.size(), userGroup.getUsers().size());

        deleteUserGroup(userGroup.getId());
    }

    @Test
    public void userGroupShouldBeDeleted() {
        XMLUserGroup userGroup = createUserGroup();

        XMLUserGroup deleteUserGroup = getSoapClient().getEpUserGroup().getWmDelete().delete(userGroup.getId());

        Assertions.assertNull(deleteUserGroup.getErrorreport());
        Assertions.assertNull(deleteUserGroup.getId());
    }

    @Test
    public void userGroupShouldBeRead() {
        XMLUserGroup userGroup = createUserGroup();

        XMLUserGroup readUserGroup = getSoapClient().getEpUserGroup().getWmRead().getEntity(userGroup.getId());
        XMLUserGroup readUserGroupDuplicate = getSoapClient().getEpUserGroup().getWmRead().getEntity(userGroup.getId());

        Assertions.assertNotNull(readUserGroup);
        Assertions.assertNotNull(readUserGroupDuplicate);

        Assertions.assertSame(readUserGroup, readUserGroupDuplicate);

        Assertions.assertNull(readUserGroup.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, readUserGroup.getName());
        Assertions.assertEquals(DIVISION_ID, readUserGroup.getDivision());
        Assertions.assertEquals(Long.valueOf(USER_ID), readUserGroup.getSupervisor().getId());

        Assertions.assertEquals(Boolean.FALSE.toString(), readUserGroup.getHidden().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), readUserGroup.getInternal().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), readUserGroup.getForemployeesonly().toString());
        Assertions.assertEquals(INSTITUTE_ID, readUserGroup.getInstitute());
        Assertions.assertEquals(USERS_NEW.get(0), readUserGroup.getUsers().get(1).getIdString());
        Assertions.assertEquals(USERS.size(), readUserGroup.getUsers().size());

        deleteUserGroup(readUserGroup.getId());
    }

    @Test
    public void userGroupShouldBeUpdated() {
        XMLUserGroup userGroup = createUserGroup();

        XMLRequestParameterSaveUserGroup xmlRequestSaveUserGroup = getXmlRequestParameterSaveUserGroup(userGroup);

        XMLUserGroup updatedUserGroup = getSoapClient().getEpUserGroup().getWmSave().save(xmlRequestSaveUserGroup);
        Assertions.assertNull(updatedUserGroup.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, updatedUserGroup.getName());
        Assertions.assertEquals(DIVISION_ID, updatedUserGroup.getDivision());
        Assertions.assertEquals(Long.valueOf(USER_ID), updatedUserGroup.getSupervisor().getId());

        Assertions.assertEquals(Boolean.TRUE.toString(), updatedUserGroup.getHidden().toString());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedUserGroup.getInternal().toString());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedUserGroup.getForemployeesonly().toString());
        Assertions.assertEquals(INSTITUTE_ID, updatedUserGroup.getInstitute());
        Assertions.assertEquals(USERS_NEW.get(0), updatedUserGroup.getUsers().get(0).getIdString());
        Assertions.assertEquals(USERS_NEW.size(), updatedUserGroup.getUsers().size());

        deleteUserGroup(updatedUserGroup.getId());
    }
}
