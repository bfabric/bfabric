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

import java.util.Arrays;

import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveExecutable;
import org.bfabric.xml.entity.XMLExecutable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPExecutableIT extends AbstractIT {

    private final String MASTEREXECUTABLE_ID = "69";

    private final String PREDECESSOR_ID = "2";

    private final String USER_ID = "703";

    public XMLExecutable createExecutable() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.WRAPPERCREATOR.toString());
        xmlRequestSaveExecutable.setDescription(S5);
        xmlRequestSaveExecutable.setMasterexecutableid(MASTEREXECUTABLE_ID);
        xmlRequestSaveExecutable.setPredecessorid(PREDECESSOR_ID);
        xmlRequestSaveExecutable.setProgram(S5);
        xmlRequestSaveExecutable.setSupervisorid(USER_ID);
        xmlRequestSaveExecutable.setValid("false");
        xmlRequestSaveExecutable.setVersion(S5);

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        if (executable.getErrorreport() != null) {
            throw new SoapClientException("Could not create executable: " + executable.getErrorreport());
        }

        return executable;
    }

    public void deleteExecutable(Long id) {
        getSoapClient().getEpExecutable().getWmDelete().delete(id);
    }

    @Test
    public void executableShouldBeCreated() {
        XMLExecutable executable = createExecutable();

        Assertions.assertNull(executable.getErrorreport());

        Assertions.assertEquals(S5, executable.getName());
        Assertions.assertEquals(ExecutableContextEnum.WRAPPERCREATOR.toString(), executable.getContext());
        Assertions.assertEquals(S5, executable.getDescription());
        Assertions.assertEquals(Long.valueOf(MASTEREXECUTABLE_ID), executable.getMasterexecutable().getId());
        Assertions.assertEquals(Long.valueOf(PREDECESSOR_ID), executable.getPredecessor().getId());
        Assertions.assertEquals(S5, executable.getProgram());
        Assertions.assertEquals("0", executable.getSize());
        Assertions.assertEquals(Long.valueOf(USER_ID), executable.getSupervisor().getId());
        Assertions.assertEquals(Boolean.FALSE.toString(), executable.getValid());
        Assertions.assertEquals(S5, executable.getVersion());

        deleteExecutable(executable.getId());
    }

    @Test
    public void executableShouldBeDeleted() {
        XMLExecutable executable = createExecutable();

        XMLExecutable deletedExecutable = getSoapClient().getEpExecutable().getWmDelete().delete(executable.getId());

        Assertions.assertNull(deletedExecutable.getErrorreport());
        Assertions.assertNull(deletedExecutable.getId());
    }

    @Test
    public void executableShouldBeRead() {
        XMLExecutable executable = createExecutable();

        XMLExecutable readExecutable = getSoapClient().getEpExecutable().getWmRead().getEntity(executable.getId());
        XMLExecutable readExecutableDuplicate = getSoapClient().getEpExecutable().getWmRead().getEntity(executable.getId());

        Assertions.assertNotNull(readExecutable);
        Assertions.assertNotNull(readExecutableDuplicate);

        Assertions.assertSame(readExecutable, readExecutableDuplicate);

        Assertions.assertNull(readExecutable.getErrorreport());

        Assertions.assertEquals(S5, readExecutable.getName());
        Assertions.assertEquals(ExecutableContextEnum.WRAPPERCREATOR.toString(), readExecutable.getContext());
        Assertions.assertEquals(S5, readExecutable.getDescription());
        Assertions.assertEquals(Long.valueOf(MASTEREXECUTABLE_ID), readExecutable.getMasterexecutable().getId());
        Assertions.assertEquals(Long.valueOf(PREDECESSOR_ID), readExecutable.getPredecessor().getId());
        Assertions.assertEquals(S5, readExecutable.getProgram());
        Assertions.assertEquals("0", readExecutable.getSize());
        Assertions.assertEquals(Long.valueOf(USER_ID), readExecutable.getSupervisor().getId());
        Assertions.assertEquals(Boolean.FALSE.toString(), readExecutable.getValid());
        Assertions.assertEquals(S5, readExecutable.getVersion());

        deleteExecutable(readExecutable.getId());
    }

    @Test
    public void executableShouldBeUpdated() {
        XMLExecutable executable = createExecutable();

        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setId(executable.getId());

        xmlRequestSaveExecutable.setName(StringHelper.generateString(6));
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.APPLICATION.toString());
        xmlRequestSaveExecutable.setDescription(StringHelper.generateString(6));
        String UPDATEDMASTEREXECUTABLEID = "70";
        xmlRequestSaveExecutable.setMasterexecutableid(UPDATEDMASTEREXECUTABLEID);
        String UPDATEDPREDECESSORID = "2";
        xmlRequestSaveExecutable.setPredecessorid(UPDATEDPREDECESSORID);
        xmlRequestSaveExecutable.setProgram(StringHelper.generateString(6));
        String UPDATEDUSER_ID = "4668";
        xmlRequestSaveExecutable.setSupervisorid(UPDATEDUSER_ID);
        xmlRequestSaveExecutable.setValid("true");
        xmlRequestSaveExecutable.setVersion(StringHelper.generateString(6));

        XMLExecutable updatedExecutable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNull(updatedExecutable.getErrorreport());

        Assertions.assertEquals(StringHelper.generateString(6), updatedExecutable.getName());
        Assertions.assertEquals(ExecutableContextEnum.WRAPPERCREATOR.toString(), updatedExecutable.getContext());
        Assertions.assertEquals(StringHelper.generateString(6), updatedExecutable.getDescription());
        Assertions.assertEquals(Long.valueOf(UPDATEDMASTEREXECUTABLEID), updatedExecutable.getMasterexecutable().getId());
        Assertions.assertEquals(Long.valueOf(UPDATEDPREDECESSORID), updatedExecutable.getPredecessor().getId());
        Assertions.assertEquals(StringHelper.generateString(6), updatedExecutable.getProgram());
        Assertions.assertEquals("0", updatedExecutable.getSize());
        Assertions.assertEquals(Long.valueOf(UPDATEDUSER_ID), updatedExecutable.getSupervisor().getId());
        Assertions.assertEquals(Boolean.TRUE.toString(), updatedExecutable.getValid());
        Assertions.assertEquals(StringHelper.generateString(6), updatedExecutable.getVersion());

        deleteExecutable(updatedExecutable.getId());
    }

    @Test
    public void executableShouldNotBeCreatedDueToAvailableWorkunitStatus() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.WORKUNIT.toString());
        String WORKUNITIDWITHSTATUSAVAILABLE = "98480";
        xmlRequestSaveExecutable.setWorkunitid(WORKUNITIDWITHSTATUSAVAILABLE);

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getWorkunit());
        Assertions.assertEquals("The executable cannot be attached to the workunit with id " + WORKUNITIDWITHSTATUSAVAILABLE + " since its status is AVAILABLE.", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToInvalidContext() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(S5);

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getContext());
        Assertions
            .assertEquals("Invalid context: " + xmlRequestSaveExecutable.getContext() + ". Valid values: " + CollectionHelper.print(Arrays.asList(ExecutableContextEnum.values())) + "!", executable
                .getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToMasterExecutableHasNonMasterStatus() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.APPLICATION.toString());
        String NONMASTEREXECUTABLEID = "1";
        xmlRequestSaveExecutable.setMasterexecutableid(NONMASTEREXECUTABLEID);

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getMasterexecutable());
        Assertions.assertEquals("Executable " + NONMASTEREXECUTABLEID + " has not the context MASTER", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToNonExistingPredecessor() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.SUBMITTER.toString());
        xmlRequestSaveExecutable.setPredecessorid(getEntityIdNonExistingAsString());

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getPredecessor());
        Assertions.assertEquals("There is no executable with id " + getEntityIdNonExistingAsString() + "!", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToNonExistingUser() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.SUBMITTER.toString());
        xmlRequestSaveExecutable.setSupervisorid(getEntityIdNonExistingAsString());

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getSupervisor());
        Assertions.assertEquals("There is no user with id " + getEntityIdNonExistingAsString() + "!", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToNonExistingWorkunit() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.WORKUNIT.toString());
        xmlRequestSaveExecutable.setWorkunitid(getEntityIdNonExistingAsString());

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getWorkunit());
        Assertions.assertEquals("There is no workunit with id " + getEntityIdNonExistingAsString() + "!", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToNonSpecifiedContext() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getContext());
        Assertions.assertEquals("No value specified for context!", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToNonSpecifiedName() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setContext("application");

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getName());
        Assertions.assertEquals("name must not be blank", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToNonSpecifiedWorkunitForWorkunitContext() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.WORKUNIT.toString());

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getWorkunit());
        Assertions.assertEquals("Context WORKUNIT requires specification of workunitid.", executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToNonWorkunitContextLinkedWithWorkunit() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);

        for (ExecutableContextEnum context : ExecutableContextEnum.values()) {
            if (context != ExecutableContextEnum.WORKUNIT) {
                xmlRequestSaveExecutable.setContext(context.toString());
                String WORKUNITID = "120204";
                xmlRequestSaveExecutable.setWorkunitid(WORKUNITID);

                XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

                Assertions.assertNotNull(executable.getErrorreport());
                Assertions.assertNull(executable.getId());
                Assertions.assertNull(executable.getWorkunit());
                Assertions.assertEquals(context + " executable cannot be linked with a workunit!", executable.getErrorreport());
            }
        }
    }

    @Test
    public void executableShouldNotBeCreatedDueToPredecessorWithDifferentContext() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.SUBMITTER.toString());
        xmlRequestSaveExecutable.setPredecessorid(PREDECESSOR_ID);

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getPredecessor());
        Assertions.assertEquals("The context of predecessor " + PREDECESSOR_ID + " is not equal to the context of the executable " + S5, executable.getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToWorkunitBelongingToNonExtensibleContainer() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.WORKUNIT.toString());
        String WORKUNITIDFROMNONEXTENSIBLECONTAINER = "30783";
        xmlRequestSaveExecutable.setWorkunitid(WORKUNITIDFROMNONEXTENSIBLECONTAINER);

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getWorkunit());
        Assertions.assertEquals("The executable cannot be attached to the workunit with id " + WORKUNITIDFROMNONEXTENSIBLECONTAINER + " since it belongs to a non-extensible container", executable
            .getErrorreport());
    }

    @Test
    public void executableShouldNotBeCreatedDueToWrongFormattedProgram() {
        XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = new XMLRequestParameterSaveExecutable();

        xmlRequestSaveExecutable.setName(S5);
        xmlRequestSaveExecutable.setContext(ExecutableContextEnum.SUBMITTER.toString());
        xmlRequestSaveExecutable.setProgram(StringHelper.generateString(1025));

        XMLExecutable executable = getSoapClient().getEpExecutable().getWmSave().save(xmlRequestSaveExecutable);

        Assertions.assertNotNull(executable.getErrorreport());
        Assertions.assertNull(executable.getId());
        Assertions.assertNull(executable.getWorkunit());
        Assertions.assertEquals("program size must be between 0 and 1024", executable.getErrorreport());
    }
}
