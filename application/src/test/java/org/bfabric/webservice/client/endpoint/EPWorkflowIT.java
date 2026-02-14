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

import java.util.Collections;
import java.util.List;

import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflow;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowStep;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplateStep;
import org.bfabric.xml.entity.XMLWorkflow;
import org.bfabric.xml.entity.XMLWorkflowStep;
import org.bfabric.xml.entity.XMLWorkflowTemplate;
import org.bfabric.xml.entity.XMLWorkflowTemplateStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPWorkflowIT extends AbstractIT {

    private final List<String> SERVICE_TYPE_ID_LIST = Collections.singletonList("1");

    private final String EXPECTED_DURATION = "1.00";

    private final String RUNS_PER_PROCESS = "1";

    private final String SAMPLES_PER_PROCESS = "1";

    private final String ENABLED = "true";

    public void checkEqual(XMLWorkflow workflow) {
        Assertions.assertEquals(CONTAINER_ID, String.valueOf(workflow.getContainer().getId()));
    }

    public void checkEqual(XMLWorkflowTemplate workflowTemplate) {
        Assertions.assertEquals(S5, workflowTemplate.getName());
        Assertions.assertEquals(SERVICE_TYPE_ID_LIST.get(0), workflowTemplate.getServicetype().get(0).getIdString());
        Assertions.assertEquals(ENABLED, workflowTemplate.getEnabled());
        Assertions.assertEquals(S5, workflowTemplate.getDescription());
    }

    public void checkEqual(XMLWorkflowTemplateStep workflowTemplateStep) {
        Assertions.assertEquals(S5, workflowTemplateStep.getName());
        Assertions.assertEquals(ENABLED, workflowTemplateStep.getEnabled());
        Assertions.assertEquals(S5, workflowTemplateStep.getDescription());
        Assertions.assertEquals(SAMPLES_PER_PROCESS, workflowTemplateStep.getSamplesperprocess());
        Assertions.assertEquals(RUNS_PER_PROCESS, workflowTemplateStep.getRunsperprocess());
        Assertions.assertEquals(EXPECTED_DURATION, workflowTemplateStep.getExpectedduration());
    }

    public void checkNotNull(XMLWorkflow workflow) {
        Assertions.assertNotNull(workflow.getId());
        Assertions.assertNotNull(workflow.getContainer());
        Assertions.assertNotNull(workflow.getWorkflowtemplate());
    }

    public void checkNotNull(XMLWorkflowStep workflowStep) {
        Assertions.assertNotNull(workflowStep.getId());
        Assertions.assertNotNull(workflowStep.getWorkflow());
        Assertions.assertNotNull(workflowStep.getWorkflowtemplatestep());
    }

    public void checkNotNull(XMLWorkflowTemplate workflowTemplate) {
        Assertions.assertNotNull(workflowTemplate.getId());
        Assertions.assertNotNull(workflowTemplate.getName());
        Assertions.assertNotNull(workflowTemplate.getEnabled());
        Assertions.assertNotNull(workflowTemplate.getDescription());
        Assertions.assertNotNull(workflowTemplate.getServicetype());
    }

    public void checkNotNull(XMLWorkflowTemplateStep workflowTemplateStep) {
        Assertions.assertNotNull(workflowTemplateStep.getId());
        Assertions.assertNotNull(workflowTemplateStep.getName());
        Assertions.assertNotNull(workflowTemplateStep.getEnabled());
        Assertions.assertNotNull(workflowTemplateStep.getDescription());
        Assertions.assertNotNull(workflowTemplateStep.getWorkflowtemplate());
        Assertions.assertNotNull(workflowTemplateStep.getSamplesperprocess());
        Assertions.assertNotNull(workflowTemplateStep.getRunsperprocess());
        Assertions.assertNotNull(workflowTemplateStep.getExpectedduration());
    }

    public XMLWorkflow createWorkflow() {
        XMLWorkflowTemplateStep workflowTemplateStep = createWorkflowTemplateStep();

        XMLRequestParameterSaveWorkflow xmlRequestSaveWorkflow = new XMLRequestParameterSaveWorkflow();

        xmlRequestSaveWorkflow.setContainerid(CONTAINER_ID);
        xmlRequestSaveWorkflow.setWorkflowtemplateid(String.valueOf(workflowTemplateStep.getWorkflowtemplate().getId()));

        XMLWorkflow workflow = getSoapClient().getEpWorkflow().getWmSave().save(xmlRequestSaveWorkflow);

        if (workflow.getErrorreport() != null) {
            throw new SoapClientException("Could not create Workflow: " + workflow.getErrorreport());
        }

        return workflow;
    }

    public XMLWorkflowStep createWorkflowStep() {
        XMLWorkflowTemplateStep workflowTemplateStep = createWorkflowTemplateStep();

        XMLRequestParameterSaveWorkflow xmlRequestSaveWorkflow = new XMLRequestParameterSaveWorkflow();

        xmlRequestSaveWorkflow.setContainerid(CONTAINER_ID);
        xmlRequestSaveWorkflow.setWorkflowtemplateid(String.valueOf(workflowTemplateStep.getWorkflowtemplate().getId()));

        XMLWorkflow workflow = getSoapClient().getEpWorkflow().getWmSave().save(xmlRequestSaveWorkflow);

        if (workflow.getErrorreport() != null) {
            throw new SoapClientException("Could not create Workflow: " + workflow.getErrorreport());
        }

        XMLRequestParameterSaveWorkflowStep xmlRequestSaveWorkflowStep = new XMLRequestParameterSaveWorkflowStep();

        xmlRequestSaveWorkflowStep.setWorkflowid(String.valueOf(workflow.getId()));
        xmlRequestSaveWorkflowStep.setSupervisorid(USER_ID);
        xmlRequestSaveWorkflowStep.setWorkflowtemplatestepid(String.valueOf(workflowTemplateStep.getId()));

        XMLWorkflowStep workflowStep = getSoapClient().getEpWorkflowStep().getWmSave().save(xmlRequestSaveWorkflowStep);

        if (workflowStep.getErrorreport() != null) {
            throw new SoapClientException("Could not create Workflowstep: " + workflowStep.getErrorreport());
        }

        return workflowStep;
    }

    public XMLWorkflowTemplate createWorkflowTemplate() {
        XMLRequestParameterSaveWorkflowTemplate xmlRequestSaveWorkflowTemplate = new XMLRequestParameterSaveWorkflowTemplate();

        xmlRequestSaveWorkflowTemplate.setName(S5);
        xmlRequestSaveWorkflowTemplate.setEnabled(ENABLED);
        xmlRequestSaveWorkflowTemplate.setDescription(S5);
        xmlRequestSaveWorkflowTemplate.setServicetypeid(SERVICE_TYPE_ID_LIST);

        XMLWorkflowTemplate workflowTemplate = getSoapClient().getEpWorkflowTemplate().getWmSave().save(xmlRequestSaveWorkflowTemplate);

        if (workflowTemplate.getErrorreport() != null) {
            throw new SoapClientException("Could not create WorkflowTemplate: " + workflowTemplate.getErrorreport());
        }

        return workflowTemplate;
    }

    public XMLWorkflowTemplateStep createWorkflowTemplateStep() {
        XMLWorkflowTemplate workflowTemplate = createWorkflowTemplate();

        XMLRequestParameterSaveWorkflowTemplateStep xmlRequestSaveWorkflowTemplateStep = new XMLRequestParameterSaveWorkflowTemplateStep();

        xmlRequestSaveWorkflowTemplateStep.setName(S5);
        xmlRequestSaveWorkflowTemplateStep.setEnabled(ENABLED);
        xmlRequestSaveWorkflowTemplateStep.setDescription(S5);
        xmlRequestSaveWorkflowTemplateStep.setWorkflowtemplateid(String.valueOf(workflowTemplate.getId()));
        xmlRequestSaveWorkflowTemplateStep.setExpectedduration(EXPECTED_DURATION);
        xmlRequestSaveWorkflowTemplateStep.setRunsperprocess(RUNS_PER_PROCESS);
        xmlRequestSaveWorkflowTemplateStep.setSamplesperprocess(SAMPLES_PER_PROCESS);

        XMLWorkflowTemplateStep workflowTemplateStep = getSoapClient().getEpWorkflowTemplateStep().getWmSave().save(xmlRequestSaveWorkflowTemplateStep);

        if (workflowTemplateStep.getErrorreport() != null) {
            throw new SoapClientException("Could not create WorkflowTemplateStep: " + workflowTemplateStep.getErrorreport());
        }

        return workflowTemplateStep;
    }

    public XMLWorkflow deleteWorkflow(Long id) {
        return getSoapClient().getEpWorkflow().getWmDelete().delete(id);
    }

    public XMLWorkflowStep deleteWorkflowStep(Long id) {
        return getSoapClient().getEpWorkflowStep().getWmDelete().delete(id);
    }

    public XMLWorkflowTemplate deleteWorkflowTemplate(Long id) {
        return getSoapClient().getEpWorkflowTemplate().getWmDelete().delete(id);
    }

    public XMLWorkflowTemplateStep deleteWorkflowTemplateStep(Long id) {
        return getSoapClient().getEpWorkflowTemplateStep().getWmDelete().delete(id);
    }

    @Test
    public void workflowShouldBeCreated() {
        XMLWorkflow workflow = createWorkflow();

        Assertions.assertNull(workflow.getErrorreport());

        checkNotNull(workflow);
        checkEqual(workflow);

        deleteWorkflow(workflow.getId());
        deleteWorkflowTemplate(workflow.getWorkflowtemplate().getId());
    }

    @Test
    public void workflowShouldBeDeleted() {
        XMLWorkflow workflow = createWorkflow();

        XMLWorkflow deletedWorkflow = deleteWorkflow(workflow.getId());

        deleteWorkflowTemplate(workflow.getWorkflowtemplate().getId());

        Assertions.assertNull(deletedWorkflow.getErrorreport());
        Assertions.assertNull(deletedWorkflow.getId());
    }

    @Test
    public void workflowShouldBeRead() {
        XMLWorkflow workflow = createWorkflow();

        XMLWorkflow readWorkflow = getSoapClient().getEpWorkflow().getWmRead().getEntity(workflow.getId());
        XMLWorkflow readWorkflowDuplicate = getSoapClient().getEpWorkflow().getWmRead().getEntity(workflow.getId());

        Assertions.assertNotNull(readWorkflow);
        Assertions.assertNotNull(readWorkflowDuplicate);

        Assertions.assertSame(readWorkflow, readWorkflowDuplicate);

        Assertions.assertNull(readWorkflow.getErrorreport());

        checkNotNull(readWorkflow);
        checkEqual(readWorkflow);

        deleteWorkflow(workflow.getId());
        deleteWorkflowTemplate(workflow.getWorkflowtemplate().getId());
    }

    @Test
    public void workflowShouldBeUpdated() {
        XMLWorkflow workflow = createWorkflow();

        XMLRequestParameterSaveWorkflow xmlRequestSaveWorkflow = new XMLRequestParameterSaveWorkflow();

        xmlRequestSaveWorkflow.setId(workflow.getId());
        xmlRequestSaveWorkflow.setWorkflowtemplateid(String.valueOf(workflow.getWorkflowtemplate().getId()));
        String UPDATECONTAINER_ID = "403";
        xmlRequestSaveWorkflow.setContainerid(UPDATECONTAINER_ID);

        XMLWorkflow updatedWorkflow = getSoapClient().getEpWorkflow().getWmSave().save(xmlRequestSaveWorkflow);

        Assertions.assertNull(updatedWorkflow.getErrorreport());

        checkNotNull(updatedWorkflow);

        Assertions.assertEquals(UPDATECONTAINER_ID, String.valueOf(updatedWorkflow.getContainer().getId()));

        deleteWorkflow(workflow.getId());
        deleteWorkflowTemplate(workflow.getWorkflowtemplate().getId());
    }

    @Test
    public void workflowStepShouldBeCreated() {
        XMLWorkflowStep workflowStep = createWorkflowStep();

        Assertions.assertNull(workflowStep.getErrorreport());

        checkNotNull(workflowStep);

        deleteWorkflow(workflowStep.getWorkflow().getId());
        XMLWorkflowTemplateStep workflowTemplateStep = getSoapClient().getEpWorkflowTemplateStep().getWmRead().getEntity(workflowStep.getWorkflowtemplatestep().getId());
        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());
    }

    @Test
    public void workflowStepShouldBeDeleted() {
        XMLWorkflowStep workflowStep = createWorkflowStep();

        XMLWorkflowStep deletedWorkflowStep = deleteWorkflowStep(workflowStep.getId());

        deleteWorkflow(workflowStep.getWorkflow().getId());
        XMLWorkflowTemplateStep workflowTemplateStep = getSoapClient().getEpWorkflowTemplateStep().getWmRead().getEntity(workflowStep.getWorkflowtemplatestep().getId());
        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());

        Assertions.assertNull(deletedWorkflowStep.getErrorreport());
        Assertions.assertNull(deletedWorkflowStep.getId());
    }

    @Test
    public void workflowStepShouldBeRead() {
        XMLWorkflowStep workflowStep = createWorkflowStep();

        XMLWorkflowStep readWorkflowStep = getSoapClient().getEpWorkflowStep().getWmRead().getEntity(workflowStep.getId());
        XMLWorkflowStep readWorkflowStepDuplicate = getSoapClient().getEpWorkflowStep().getWmRead().getEntity(workflowStep.getId());

        deleteWorkflow(workflowStep.getWorkflow().getId());
        XMLWorkflowTemplateStep workflowTemplateStep = getSoapClient().getEpWorkflowTemplateStep().getWmRead().getEntity(workflowStep.getWorkflowtemplatestep().getId());
        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());

        Assertions.assertNotNull(readWorkflowStep);
        Assertions.assertNotNull(readWorkflowStepDuplicate);

        Assertions.assertSame(readWorkflowStep, readWorkflowStepDuplicate);

        Assertions.assertNull(readWorkflowStep.getErrorreport());

        checkNotNull(readWorkflowStep);
    }

    @Test
    public void workflowStepShouldBeUpdated() {
        XMLWorkflowStep workflowStep = createWorkflowStep();

        XMLRequestParameterSaveWorkflowStep xmlRequestSaveWorkflowStep = new XMLRequestParameterSaveWorkflowStep();

        xmlRequestSaveWorkflowStep.setId(workflowStep.getId());
        xmlRequestSaveWorkflowStep.setWorkflowid(String.valueOf(workflowStep.getWorkflow().getId()));
        xmlRequestSaveWorkflowStep.setWorkflowtemplatestepid(String.valueOf(workflowStep.getWorkflowtemplatestep().getId()));

        XMLWorkflowStep updatedWorkflowStep = getSoapClient().getEpWorkflowStep().getWmSave().save(xmlRequestSaveWorkflowStep);

        deleteWorkflow(workflowStep.getWorkflow().getId());
        XMLWorkflowTemplateStep workflowTemplateStep = getSoapClient().getEpWorkflowTemplateStep().getWmRead().getEntity(workflowStep.getWorkflowtemplatestep().getId());
        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());

        Assertions.assertNull(updatedWorkflowStep.getErrorreport());

        checkNotNull(updatedWorkflowStep);
    }

    @Test
    public void workflowTemplateShouldBeCreated() {
        XMLWorkflowTemplate workflowTemplate = createWorkflowTemplate();

        Assertions.assertNull(workflowTemplate.getErrorreport());

        checkNotNull(workflowTemplate);
        checkEqual(workflowTemplate);

        deleteWorkflowTemplate(workflowTemplate.getId());
    }

    @Test
    public void workflowTemplateShouldBeDeleted() {
        XMLWorkflowTemplate workflowTemplate = createWorkflowTemplate();

        XMLWorkflowTemplate deletedWorkflowTemplate = deleteWorkflowTemplate(workflowTemplate.getId());

        Assertions.assertNull(deletedWorkflowTemplate.getErrorreport());
        Assertions.assertNull(deletedWorkflowTemplate.getId());
    }

    @Test
    public void workflowTemplateShouldBeRead() {
        XMLWorkflowTemplate workflowTemplate = createWorkflowTemplate();

        XMLWorkflowTemplate readWorkflowTemplate = getSoapClient().getEpWorkflowTemplate().getWmRead().getEntity(workflowTemplate.getId());
        XMLWorkflowTemplate readWorkflowTemplateDuplicate = getSoapClient().getEpWorkflowTemplate().getWmRead().getEntity(workflowTemplate.getId());

        Assertions.assertNotNull(readWorkflowTemplate);
        Assertions.assertNotNull(readWorkflowTemplateDuplicate);

        Assertions.assertSame(readWorkflowTemplate, readWorkflowTemplateDuplicate);

        Assertions.assertNull(readWorkflowTemplate.getErrorreport());

        checkNotNull(readWorkflowTemplate);
        checkEqual(readWorkflowTemplate);

        deleteWorkflowTemplate(readWorkflowTemplate.getId());
    }

    @Test
    public void workflowTemplateShouldBeUpdated() {
        XMLWorkflowTemplate workflowTemplate = createWorkflowTemplate();

        XMLRequestParameterSaveWorkflowTemplate xmlRequestSaveWorkflowTemplate = new XMLRequestParameterSaveWorkflowTemplate();

        xmlRequestSaveWorkflowTemplate.setId(workflowTemplate.getId());

        xmlRequestSaveWorkflowTemplate.setName(StringHelper.generateString(6));
        xmlRequestSaveWorkflowTemplate.setEnabled("false");
        xmlRequestSaveWorkflowTemplate.setDescription(StringHelper.generateString(6));

        XMLWorkflowTemplate updatedWorkflowTemplate = getSoapClient().getEpWorkflowTemplate().getWmSave().save(xmlRequestSaveWorkflowTemplate);

        Assertions.assertNull(updatedWorkflowTemplate.getErrorreport());

        checkNotNull(updatedWorkflowTemplate);

        Assertions.assertEquals(StringHelper.generateString(6), updatedWorkflowTemplate.getName());
        Assertions.assertEquals("false", updatedWorkflowTemplate.getEnabled());
        Assertions.assertEquals(StringHelper.generateString(6), updatedWorkflowTemplate.getDescription());

        deleteWorkflowTemplate(workflowTemplate.getId());
    }

    @Test
    public void workflowTemplateShouldNotBeCreatedDueToNonExistingServiceType() {
        XMLRequestParameterSaveWorkflowTemplate xmlRequestSaveWorkflowTemplate = new XMLRequestParameterSaveWorkflowTemplate();

        xmlRequestSaveWorkflowTemplate.setName(S5);
        xmlRequestSaveWorkflowTemplate.setEnabled(ENABLED);
        xmlRequestSaveWorkflowTemplate.setDescription(StringHelper.generateString(6));
        xmlRequestSaveWorkflowTemplate.setServicetypeid(getListWithNonExistingEntityId());

        XMLWorkflowTemplate workflowTemplate = getSoapClient().getEpWorkflowTemplate().getWmSave().save(xmlRequestSaveWorkflowTemplate);

        Assertions.assertNotNull(workflowTemplate.getErrorreport());
        Assertions.assertEquals("There is no servicetype with id " + getEntityIdNonExistingAsString() + "!", workflowTemplate.getErrorreport());
    }

    @Test
    public void workflowTemplateShouldNotBeCreatedDueToTooLongName() {
        XMLRequestParameterSaveWorkflowTemplate xmlRequestSaveWorkflowTemplate = new XMLRequestParameterSaveWorkflowTemplate();

        xmlRequestSaveWorkflowTemplate.setName(StringHelper.generateString(257));
        xmlRequestSaveWorkflowTemplate.setEnabled(ENABLED);
        xmlRequestSaveWorkflowTemplate.setDescription(StringHelper.generateString(6));
        xmlRequestSaveWorkflowTemplate.setServicetypeid(SERVICE_TYPE_ID_LIST);

        XMLWorkflowTemplate workflowTemplate = getSoapClient().getEpWorkflowTemplate().getWmSave().save(xmlRequestSaveWorkflowTemplate);

        Assertions.assertNotNull(workflowTemplate.getErrorreport());
        Assertions.assertEquals("name size must be between 0 and 256", workflowTemplate.getErrorreport());
    }

    @Test
    public void workflowTemplateStepShouldBeCreated() {
        XMLWorkflowTemplateStep workflowTemplateStep = createWorkflowTemplateStep();

        Assertions.assertNull(workflowTemplateStep.getErrorreport());

        checkNotNull(workflowTemplateStep);
        checkEqual(workflowTemplateStep);

        deleteWorkflowTemplateStep(workflowTemplateStep.getId());
        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());
    }

    @Test
    public void workflowTemplateStepShouldBeDeleted() {
        XMLWorkflowTemplateStep workflowTemplateStep = createWorkflowTemplateStep();

        XMLWorkflowTemplateStep deletedWorkflowTemplateStep = deleteWorkflowTemplateStep(workflowTemplateStep.getId());

        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());

        Assertions.assertNull(deletedWorkflowTemplateStep.getErrorreport());
        Assertions.assertNull(deletedWorkflowTemplateStep.getId());
    }

    @Test
    public void workflowTemplateStepShouldBeRead() {
        XMLWorkflowTemplateStep workflowTemplateStep = createWorkflowTemplateStep();

        XMLWorkflowTemplateStep readWorkflowTemplateStep = getSoapClient().getEpWorkflowTemplateStep().getWmRead().getEntity(workflowTemplateStep.getId());
        XMLWorkflowTemplateStep readWorkflowTemplateStepDuplicate = getSoapClient().getEpWorkflowTemplateStep().getWmRead().getEntity(workflowTemplateStep.getId());

        deleteWorkflowTemplateStep(readWorkflowTemplateStep.getId());
        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());

        Assertions.assertNotNull(readWorkflowTemplateStep);
        Assertions.assertNotNull(readWorkflowTemplateStepDuplicate);

        Assertions.assertSame(readWorkflowTemplateStep, readWorkflowTemplateStepDuplicate);

        Assertions.assertNull(readWorkflowTemplateStep.getErrorreport());

        checkNotNull(readWorkflowTemplateStep);
        checkEqual(readWorkflowTemplateStep);
    }

    @Test
    public void workflowTemplateStepShouldBeUpdated() {
        XMLWorkflowTemplateStep workflowTemplateStep = createWorkflowTemplateStep();

        XMLRequestParameterSaveWorkflowTemplateStep xmlRequestSaveWorkflowTemplateStep = new XMLRequestParameterSaveWorkflowTemplateStep();

        xmlRequestSaveWorkflowTemplateStep.setId(workflowTemplateStep.getId());

        xmlRequestSaveWorkflowTemplateStep.setName(StringHelper.generateString(6));
        xmlRequestSaveWorkflowTemplateStep.setEnabled("false");
        xmlRequestSaveWorkflowTemplateStep.setDescription(StringHelper.generateString(6));
        xmlRequestSaveWorkflowTemplateStep.setWorkflowtemplateid(String.valueOf(workflowTemplateStep.getWorkflowtemplate().getId()));
        xmlRequestSaveWorkflowTemplateStep.setExpectedduration(EXPECTED_DURATION);
        xmlRequestSaveWorkflowTemplateStep.setRunsperprocess(RUNS_PER_PROCESS);
        xmlRequestSaveWorkflowTemplateStep.setSamplesperprocess(SAMPLES_PER_PROCESS);

        XMLWorkflowTemplateStep updatedWorkflowTemplateStep = getSoapClient().getEpWorkflowTemplateStep().getWmSave().save(xmlRequestSaveWorkflowTemplateStep);

        deleteWorkflowTemplateStep(workflowTemplateStep.getId());
        deleteWorkflowTemplate(workflowTemplateStep.getWorkflowtemplate().getId());

        Assertions.assertNull(updatedWorkflowTemplateStep.getErrorreport());

        checkNotNull(updatedWorkflowTemplateStep);

        Assertions.assertEquals(StringHelper.generateString(6), updatedWorkflowTemplateStep.getName());
        Assertions.assertEquals("false", updatedWorkflowTemplateStep.getEnabled());
        Assertions.assertEquals(StringHelper.generateString(6), updatedWorkflowTemplateStep.getDescription());
    }
}
