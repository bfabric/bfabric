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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bfabric.Constants;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveApplication;
import org.bfabric.xml.entity.XMLApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPApplicationIT extends AbstractIT {

    private final String EMPLOYEE_ID = "595";

    private final String PREDECESSOR_ID = "80";

    @Test
    public void applicationShouldBeAbleToHandleMultipleTechnologies() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS);
        List<String> technologies = new ArrayList<>();
        technologies.add("6");
        technologies.add("1");
        xmlRequestSaveApplication.setTechnologies(technologies);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNull(application.getErrorreport());
        Assertions.assertNotNull(application.getId());
        Assertions.assertEquals(S5, application.getName());
        Assertions.assertEquals(Arrays.asList("Administration", "Genomics"), application.getTechnology());

        deleteApplication(application.getId());
    }

    @Test
    public void applicationShouldBeCreatedWithDefaultSupervisor() {
        XMLApplication application = createApplication(null);

        Assertions.assertNull(application.getErrorreport());

        Assertions.assertEquals(S5, application.getName());
        Assertions.assertEquals(Long.valueOf(EMPLOYEE_ID), application.getSupervisor().getId());
        Assertions.assertEquals(Constants.APPLICATION_TYPE_ANALYSIS, application.getType());
        Assertions.assertEquals(Constants.DATASET, application.getPageflow());
        Assertions.assertEquals(S5, application.getHelp());
        Assertions.assertEquals(S5, application.getDescription());
        Assertions.assertEquals(Long.valueOf(PREDECESSOR_ID), application.getPredecessor().getId());
        Assertions.assertEquals(Boolean.TRUE.toString(), application.getHidden());
        Assertions.assertEquals(Boolean.TRUE.toString(), application.getForemployeesonly());
        Assertions.assertEquals(Boolean.TRUE.toString(), application.getNotifyapplicationsupervisor());
        Assertions.assertEquals(Boolean.TRUE.toString(), application.getNotifycontainermember());
        Assertions.assertEquals(Long.valueOf(STORAGE_ID), application.getStorage().getId());

        deleteApplication(application.getId());
    }

    @Test
    public void applicationShouldBeCreatedWithSpecifiedSupervisor() {
        XMLApplication application = createApplication(USER_ID);

        Assertions.assertNull(application.getErrorreport());
        Assertions.assertNotNull(application.getId());
        Assertions.assertNotNull(application.getName());
        Assertions.assertNotNull(application.getSupervisor());
        Assertions.assertNotNull(application.getType());
        Assertions.assertNotNull(application.getPageflow());
        Assertions.assertNotNull(application.getHelp());
        Assertions.assertNotNull(application.getDescription());
        Assertions.assertNotNull(application.getPredecessor());
        Assertions.assertNotNull(application.getHidden());
        Assertions.assertNotNull(application.getForemployeesonly());
        Assertions.assertNotNull(application.getNotifyapplicationsupervisor());
        Assertions.assertNotNull(application.getNotifycontainermember());
        Assertions.assertNotNull(application.getStorage());

        Assertions.assertEquals(Long.valueOf(USER_ID), application.getSupervisor().getId());

        deleteApplication(application.getId());
    }

    @Test
    public void applicationShouldBeDeleted() {
        XMLApplication application = createApplication(null);

        XMLApplication deletedApplication = getSoapClient().getEpApplication().getWmDelete().delete(application.getId());

        Assertions.assertNull(deletedApplication.getErrorreport());
        Assertions.assertNull(deletedApplication.getId());
    }

    @Test
    public void applicationShouldBeRead() {
        XMLApplication application = createApplication(null);

        XMLApplication readApplication = getSoapClient().getEpApplication().getWmRead().getEntity(application.getId());
        XMLApplication readApplicationDuplicate = getSoapClient().getEpApplication().getWmRead().getEntity(application.getId());

        Assertions.assertNotNull(readApplication);
        Assertions.assertNotNull(readApplicationDuplicate);

        Assertions.assertSame(readApplication, readApplicationDuplicate);

        Assertions.assertNull(readApplication.getErrorreport());

        Assertions.assertEquals(S5, readApplication.getName());
        Assertions.assertEquals(Long.valueOf(EMPLOYEE_ID), readApplication.getSupervisor().getId());
        Assertions.assertEquals(Constants.APPLICATION_TYPE_ANALYSIS, readApplication.getType());
        Assertions.assertEquals(Constants.DATASET, readApplication.getPageflow());
        Assertions.assertEquals(S5, readApplication.getHelp());
        Assertions.assertEquals(S5, readApplication.getDescription());
        Assertions.assertEquals(Long.valueOf(PREDECESSOR_ID), readApplication.getPredecessor().getId());
        Assertions.assertEquals(Boolean.TRUE.toString(), readApplication.getHidden());
        Assertions.assertEquals(Boolean.TRUE.toString(), readApplication.getForemployeesonly());
        Assertions.assertEquals(Boolean.TRUE.toString(), readApplication.getNotifyapplicationsupervisor());
        Assertions.assertEquals(Boolean.TRUE.toString(), readApplication.getNotifycontainermember());
        Assertions.assertEquals(Long.valueOf(STORAGE_ID), readApplication.getStorage().getId());

        deleteApplication(readApplication.getId());
    }

    @Test
    public void applicationShouldBeUpdated() {
        XMLApplication application = createApplication(null);

        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setId(application.getId());

        xmlRequestSaveApplication.setName(StringHelper.generateString(6));
        xmlRequestSaveApplication.setSupervisorid(USER_ID);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_IMPORT);
        xmlRequestSaveApplication.setHelp(StringHelper.generateString(6));
        xmlRequestSaveApplication.setDescription(StringHelper.generateString(6));
        xmlRequestSaveApplication.setHidden(Boolean.FALSE.toString());
        String UPDATED_STORAGE_ID = "2";
        xmlRequestSaveApplication.setStorageid(UPDATED_STORAGE_ID);

        XMLApplication updatedApplication = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNull(updatedApplication.getErrorreport());

        Assertions.assertEquals(StringHelper.generateString(6), updatedApplication.getName());
        Assertions.assertEquals(Long.valueOf(USER_ID), updatedApplication.getSupervisor().getId());
        Assertions.assertEquals(Constants.APPLICATION_TYPE_ANALYSIS, updatedApplication.getType());
        Assertions.assertEquals(Constants.DATASET, updatedApplication.getPageflow());
        Assertions.assertEquals(StringHelper.generateString(6), updatedApplication.getHelp());
        Assertions.assertEquals(StringHelper.generateString(6), updatedApplication.getDescription());
        Assertions.assertEquals(Long.valueOf(PREDECESSOR_ID), updatedApplication.getPredecessor().getId());
        Assertions.assertEquals(Boolean.FALSE.toString(), updatedApplication.getHidden());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedApplication.getForemployeesonly());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedApplication.getNotifyapplicationsupervisor());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedApplication.getNotifycontainermember());
        Assertions.assertEquals(Long.valueOf(UPDATED_STORAGE_ID), updatedApplication.getStorage().getId());

        deleteApplication(application.getId());
    }

    @Test
    public void applicationShouldNotBeCreatedDueToInvalidPageflowName() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS.toLowerCase());
        xmlRequestSaveApplication.setPageflowname(S5);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        String errorPrefix = "Invalid pageflow: " + S5;
        Assertions.assertEquals(errorPrefix, application.getErrorreport().subSequence(0, 23));
    }

    @Test
    public void applicationShouldNotBeCreatedDueToInvalidTechnology() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS.toLowerCase());
        xmlRequestSaveApplication.setTechnologies(Collections.singletonList(getEntityIdNonExistingAsString()));

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("There is no technology with id " + getEntityIdNonExistingAsString() + "!", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueToInvalidType() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(S5);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        String errorPrefix = "Invalid application type: " + S5;
        Assertions.assertEquals(errorPrefix, application.getErrorreport().subSequence(0, errorPrefix.length()));
    }

    @Test
    public void applicationShouldNotBeCreatedDueToNonExistingPredecessor() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS.toLowerCase());
        xmlRequestSaveApplication.setPredecessorid(getEntityIdNonExistingAsString());

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("There is no application with id " + getEntityIdNonExistingAsString() + "!", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueToNonExistingStorage() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS.toLowerCase());
        xmlRequestSaveApplication.setStorageid(getEntityIdNonExistingAsString());

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("There is no storage with id " + getEntityIdNonExistingAsString() + "!", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueToNonExistingSupervisor() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS.toLowerCase());
        xmlRequestSaveApplication.setSupervisorid(getEntityIdNonExistingAsString());

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("There is no user with id " + getEntityIdNonExistingAsString() + "!", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueToNonSpecifiedName() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_WEBAPP);
        xmlRequestSaveApplication.setWeburl(S5);
        xmlRequestSaveApplication.setDescription(S5);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("name must not be blank", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueToNonSpecifiedType() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("No value specified for type!", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueToNonSpecifiedWebUrl() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_WEBAPP);
        xmlRequestSaveApplication.setDescription(S5);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("webUrl must not be blank", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueTooLongHelp() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS.toLowerCase());
        xmlRequestSaveApplication.setHelp(StringHelper.generateString(257));
        xmlRequestSaveApplication.setDescription(S5);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("help size must be between 0 and 256", application.getErrorreport());
    }

    @Test
    public void applicationShouldNotBeCreatedDueTooLongName() {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(StringHelper.generateString(257));
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS);
        xmlRequestSaveApplication.setDescription(S5);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        Assertions.assertNotNull(application.getErrorreport());
        Assertions.assertEquals("name size must be between 0 and 256", application.getErrorreport());
    }

    public XMLApplication createApplication(String supervisorId) {
        XMLRequestParameterSaveApplication xmlRequestSaveApplication = new XMLRequestParameterSaveApplication();

        xmlRequestSaveApplication.setName(S5);
        if (supervisorId != null) {
            xmlRequestSaveApplication.setSupervisorid(supervisorId);
        }
        xmlRequestSaveApplication.setType(Constants.APPLICATION_TYPE_ANALYSIS.toLowerCase());
        xmlRequestSaveApplication.setPageflowname(Constants.DATASET);

        xmlRequestSaveApplication.setHelp(S5);
        xmlRequestSaveApplication.setDescription(S5);
        xmlRequestSaveApplication.setPredecessorid(PREDECESSOR_ID);
        xmlRequestSaveApplication.setHidden(Boolean.TRUE.toString());
        xmlRequestSaveApplication.setForemployeesonly(Boolean.TRUE.toString());
        xmlRequestSaveApplication.setNotifyapplicationsupervisor(Boolean.TRUE.toString());
        xmlRequestSaveApplication.setNotifycontainermember(Boolean.TRUE.toString());
        xmlRequestSaveApplication.setStorageid(STORAGE_ID);

        XMLApplication application = getSoapClient().getEpApplication().getWmSave().save(xmlRequestSaveApplication);

        if (application.getErrorreport() != null) {
            throw new SoapClientException("Could not create application: " + application.getErrorreport());
        }

        return application;
    }

    public void deleteApplication(Long id) {
        getSoapClient().getEpApplication().getWmDelete().delete(id);
    }
}
