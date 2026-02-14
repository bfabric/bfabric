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

import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUser;
import org.bfabric.xml.entity.XMLUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPUserIT extends AbstractIT {

    public XMLUser createUser(int option) {
        XMLRequestParameterSaveUser xmlRequestSaveUser = new XMLRequestParameterSaveUser();
        xmlRequestSaveUser.setLogin("bfabricwstest");
        xmlRequestSaveUser.setPassword("1234567890YXyx");
        xmlRequestSaveUser.setEmail("bfabric@xx.xx");
        xmlRequestSaveUser.setPrivateemail("bfabric@xx.xx");
        xmlRequestSaveUser.setTitle("Dr.");
        xmlRequestSaveUser.setSalutation("MR");
        xmlRequestSaveUser.setFirstname("Bfabric");
        xmlRequestSaveUser.setLastname("Test");
        if (option == 1) {
            xmlRequestSaveUser.setOrganizationtypeid("4");
            xmlRequestSaveUser.setCompany("1");
        } else if (option == 2) {
            xmlRequestSaveUser.setOrganizationtypeid("3");
            xmlRequestSaveUser.setOrganization("New");
            xmlRequestSaveUser.setDepartment("New");
            xmlRequestSaveUser.setInstitute("New");
        } else {
            xmlRequestSaveUser.setInstituteid("1");
        }
        xmlRequestSaveUser.setDescription(StringHelper.generateString(5));
        xmlRequestSaveUser.setAddressstreet("Bahnhofstrasse 1");
        xmlRequestSaveUser.setAddresszip("8001");
        xmlRequestSaveUser.setAddresscity("Zürich");
        xmlRequestSaveUser.setAddresscountrycode("CH");
        xmlRequestSaveUser.setPhonecountrycode("41");
        xmlRequestSaveUser.setPhoneareacode("44");
        xmlRequestSaveUser.setPhonelocalnumber("1234567");
        xmlRequestSaveUser.setHomeaddressstreet("Bahnhofstrasse 1");
        xmlRequestSaveUser.setHomeaddresszip("8001");
        xmlRequestSaveUser.setHomeaddresscountrycode("CH");
        xmlRequestSaveUser.setHomeaddresscity("Zurich");
        xmlRequestSaveUser.setHomephonecountrycode("41");
        xmlRequestSaveUser.setHomephoneareacode("44");
        xmlRequestSaveUser.setHomephonelocalnumber("1234567");

        XMLUser user = getSoapClient().getEpUser().getWmSave().save(xmlRequestSaveUser);
        if (user != null && user.getErrorreport() != null) {
            throw new SoapClientException("Could not create User: " + user.getErrorreport());
        }
        return user;
    }

    public void deleteUser(Long id) {
        getSoapClient().getEpUser().getWmDelete().delete(id);
    }

    @Test
    public void sampleShouldBeRead() {
        Long USERID = (long) 21;
        XMLUser user = getSoapClient().getEpUser().getWmRead().getEntity(USERID);

        Assertions.assertNull(user.getErrorreport());
        Assertions.assertNotNull(user.getId());
        Assertions.assertNotNull(user.getLastname());
        Assertions.assertNotNull(user.getFirstname());
        Assertions.assertNotNull(user.getEmail());
        Assertions.assertNotNull(user.getLogin());

        user = getSoapClient().getEpUser().getWmRead().getEntity(getEntityIdNonExisting());

        Assertions.assertNull(user);
    }

    @Test
    public void userShouldBeCreated() {
        XMLUser user = createUser(0);
        Assertions.assertNull(user.getErrorreport());
        Assertions.assertNotNull(user.getId());
        Assertions.assertNotNull(user.getDescription());
        Assertions.assertEquals("Dr.", user.getTitle());
        Assertions.assertEquals("Mr", user.getSalutation());
        Assertions.assertEquals("Bfabric", user.getFirstname());
        Assertions.assertEquals("Test", user.getLastname());
        Assertions.assertEquals(StringHelper.generateString(5), user.getDescription());
        Assertions.assertEquals("bfabric@xx.xx", user.getEmail());
        Assertions.assertEquals("bfabric@xx.xx", user.getPrivateemail());
        Assertions.assertEquals("bfabricwstest", user.getLogin());
        Assertions.assertEquals("1", user.getInstituteid());
        Assertions.assertEquals("Bahnhofstrasse 1", user.getAddressstreet());
        Assertions.assertEquals("8001", user.getAddresszip());
        Assertions.assertEquals("Zürich", user.getAddresscity());
        Assertions.assertEquals("CH", user.getAddresscountrycode());
        Assertions.assertEquals("41", user.getPhonecountrycode());
        Assertions.assertEquals("44", user.getPhoneareacode());
        Assertions.assertEquals("1234567", user.getPhonelocalnumber());
        Assertions.assertEquals("Bahnhofstrasse 1", user.getHomeaddressstreet());
        Assertions.assertEquals("8001", user.getHomeaddresszip());
        Assertions.assertEquals("Zürich", user.getHomeaddresscity());
        Assertions.assertEquals("CH", user.getHomeaddresscountrycode());
        Assertions.assertEquals("41", user.getHomephonecountrycode());
        Assertions.assertEquals("44", user.getHomephoneareacode());
        Assertions.assertEquals("1234567", user.getHomephonelocalnumber());

        deleteUser(user.getId());
    }

    @Test
    public void userShouldBeCreated2() {
        XMLUser user = createUser(1);
        Assertions.assertNull(user.getErrorreport());
        Assertions.assertEquals("1", user.getCompany());
        deleteUser(user.getId());
    }

    @Test
    public void userShouldBeCreated3() {
        XMLUser user = createUser(2);
        Assertions.assertNull(user.getErrorreport());
        Assertions.assertEquals("New", user.getOrganzation());
        deleteUser(user.getId());
    }
}
