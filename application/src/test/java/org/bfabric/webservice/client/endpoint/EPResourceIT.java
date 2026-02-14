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

import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkunit;
import org.bfabric.xml.entity.XMLResource;
import org.bfabric.xml.entity.XMLWorkunit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EPResourceIT extends AbstractIT {

    private static String WORKUNIT_ID;

    @AfterAll
    public static void after() {
        getSoapClient().getEpWorkunit().getWmDelete().delete(Long.valueOf(WORKUNIT_ID));
    }

    public static XMLWorkunit createWorkunit() {
        XMLRequestParameterSaveWorkunit xmlRequestSaveWorkunit = new XMLRequestParameterSaveWorkunit();

        xmlRequestSaveWorkunit.setName(S5);
        xmlRequestSaveWorkunit.setContainerid(CONTAINER_ID);
        xmlRequestSaveWorkunit.setApplicationid(APPLICATION_ID);
        xmlRequestSaveWorkunit.setStatus(WorkunitStatusEnum.PENDING.toString());
        xmlRequestSaveWorkunit.setDescription(S5);

        XMLWorkunit workunit = getSoapClient().getEpWorkunit().getWmSave().save(xmlRequestSaveWorkunit);

        if (workunit.getErrorreport() != null) {
            throw new SoapClientException("Could not create workunit: " + workunit.getErrorreport());
        }

        return workunit;
    }

    @BeforeAll
    public static void init() {
        XMLWorkunit xmlWorkunit = createWorkunit();
        WORKUNIT_ID = xmlWorkunit.getId().toString();
    }

    public XMLResource createResource() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setName(S5);
        xmlRequestSaveResource.setDescription(S5);
        xmlRequestSaveResource.setExpirationdate("2018-11-08");
        xmlRequestSaveResource.setFilechecksum(S5);
        xmlRequestSaveResource.setRelativepath(S5);
        xmlRequestSaveResource.setReport(S5);
        xmlRequestSaveResource.setSize("1");
        xmlRequestSaveResource.setStatus(ResourceStatusEnum.PENDING.toString());
        xmlRequestSaveResource.setStorageid(STORAGE_ID);
        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        String SAMPLE_ID = "172868";
        xmlRequestSaveResource.setSampleid(SAMPLE_ID);

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        if (resource.getErrorreport() != null) {
            throw new SoapClientException("Could not create resource: " + resource.getErrorreport());
        }

        return resource;
    }

    public void deleteResource(Long id) {
        getSoapClient().getEpResource().getWmDelete().delete(id);
    }

    @Test
    public void resourceShouldBeCreated() {
        XMLResource resource = createResource();

        Assertions.assertNull(resource.getErrorreport());

        Assertions.assertEquals(S5, resource.getName());
        Assertions.assertEquals(S5, resource.getDescription());
        Assertions.assertEquals("2018-11-08 00:00:00", resource.getExpirationdate());
        Assertions.assertEquals(S5, resource.getFilechecksum());
        Assertions.assertEquals(S5, resource.getRelativepath());
        Assertions.assertEquals(S5, resource.getReport());
        Assertions.assertEquals("1", resource.getSize());
        Assertions.assertEquals(ResourceStatusEnum.PENDING.getLabel(), resource.getStatus());
        Assertions.assertEquals(Long.valueOf(STORAGE_ID), resource.getStorage().getId());
        Assertions.assertEquals(Long.valueOf(WORKUNIT_ID), resource.getWorkunit().getId());

        deleteResource(resource.getId());
    }

    @Test
    public void resourceShouldBeDeleted() {
        XMLResource resource = createResource();

        XMLResource deletedResource = getSoapClient().getEpResource().getWmDelete().delete(resource.getId());

        Assertions.assertNull(deletedResource.getErrorreport());
        Assertions.assertNull(deletedResource.getId());
    }

    @Test
    public void resourceShouldBeRead() {
        XMLResource resource = createResource();

        XMLResource readResource = getSoapClient().getEpResource().getWmRead().getEntity(resource.getId());
        XMLResource readResourceDuplicate = getSoapClient().getEpResource().getWmRead().getEntity(resource.getId());

        Assertions.assertNotNull(readResource);
        Assertions.assertNotNull(readResourceDuplicate);

        Assertions.assertSame(readResource, readResourceDuplicate);

        Assertions.assertNull(readResource.getErrorreport());

        Assertions.assertEquals(S5, readResource.getName());
        Assertions.assertEquals(S5, readResource.getDescription());
        Assertions.assertEquals("2018-11-08 00:00:00", readResource.getExpirationdate());
        Assertions.assertEquals(S5, readResource.getFilechecksum());
        Assertions.assertEquals(S5, readResource.getRelativepath());
        Assertions.assertEquals(S5, readResource.getReport());
        Assertions.assertEquals("1", readResource.getSize());
        Assertions.assertEquals(ResourceStatusEnum.PENDING.getLabel(), readResource.getStatus());
        Assertions.assertEquals(Long.valueOf(STORAGE_ID), readResource.getStorage().getId());
        Assertions.assertEquals(Long.valueOf(WORKUNIT_ID), readResource.getWorkunit().getId());

        deleteResource(resource.getId());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToInvalidStatus() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(S5);
        xmlRequestSaveResource.setStorageid(STORAGE_ID);
        xmlRequestSaveResource.setStatus(S5);

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertNull(resource.getId());
        Assertions.assertNull(resource.getStatus());
        Assertions
            .assertEquals("Invalid status: " + xmlRequestSaveResource.getStatus() + ". Valid values: " + CollectionHelper.print(Arrays.asList(ResourceStatusEnum.values())) + "!", resource
                .getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToNegativeSize() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(S5);
        xmlRequestSaveResource.setStorageid(STORAGE_ID);
        xmlRequestSaveResource.setSize("-1");

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertNull(resource.getId());
        Assertions.assertNull(resource.getSize());
        Assertions.assertEquals("size -1 is negative!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToNonExistingInputResource() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(S5);
        xmlRequestSaveResource.setStorageid(STORAGE_ID);
        xmlRequestSaveResource.setInputresourceid(getEntityIdNonExistingAsString());

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertNull(resource.getId());
        Assertions.assertNull(resource.getInputresource());
        Assertions.assertEquals("There is no resource with id " + getEntityIdNonExistingAsString() + "!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToNonExistingStorage() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(S5);
        xmlRequestSaveResource.setStorageid(getEntityIdNonExistingAsString());

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("There is no storage with id " + getEntityIdNonExistingAsString() + "!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToNonExistingWorkunit() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(getEntityIdNonExistingAsString());

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("There is no workunit with id " + getEntityIdNonExistingAsString() + "!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToNonSpecifiedRelativePath() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setName(S5);
        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("Either storage and relative path or base64 must be specified!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToNonSpecifiedStorage() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setName(S5);
        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(S5);

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("Either storage and relative path or base64 must be specified!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToNonSpecifiedWorkunit() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setName(S5);
        xmlRequestSaveResource.setRelativepath(S5);

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("No value specified for workunitid!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToTooLongName() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(S5);
        xmlRequestSaveResource.setStorageid(STORAGE_ID);
        xmlRequestSaveResource.setName(StringHelper.generateString(257));

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("name size must be between 0 and 256", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToTooLongRelativePath() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(StringHelper.generateString(1025));

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToTooLongReport() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setWorkunitid(WORKUNIT_ID);
        xmlRequestSaveResource.setRelativepath(S5);
        xmlRequestSaveResource.setStorageid(STORAGE_ID);
        xmlRequestSaveResource.setReport(StringHelper.generateString(33));

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("report size must be between 0 and 32", resource.getErrorreport());
    }

    @Test
    public void resourceShouldNotBeCreatedDueToWorkunitWithNonExtensibleContainer() {
        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        String WORKUNITIDWITHNONEXTENSIBLECONTAINER = "135694";
        xmlRequestSaveResource.setWorkunitid(WORKUNITIDWITHNONEXTENSIBLECONTAINER);

        XMLResource resource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNotNull(resource.getErrorreport());
        Assertions.assertEquals("Workunit's container is not extensible!", resource.getErrorreport());
    }

    @Test
    public void resourceShouldUpdate() {
        XMLResource resource = createResource();

        XMLRequestParameterSaveResource xmlRequestSaveResource = new XMLRequestParameterSaveResource();

        xmlRequestSaveResource.setId(resource.getId());

        xmlRequestSaveResource.setName(StringHelper.generateString(6));
        xmlRequestSaveResource.setDescription(StringHelper.generateString(6));
        xmlRequestSaveResource.setExpirationdate("2018-11-09");
        xmlRequestSaveResource.setFilechecksum(StringHelper.generateString(6));
        xmlRequestSaveResource.setRelativepath(StringHelper.generateString(6));
        xmlRequestSaveResource.setReport(StringHelper.generateString(6));
        xmlRequestSaveResource.setSize("2");
        xmlRequestSaveResource.setStatus(ResourceStatusEnum.ARCHIVED.toString());

        XMLResource updatedResource = getSoapClient().getEpResource().getWmSave().save(xmlRequestSaveResource);

        Assertions.assertNull(updatedResource.getErrorreport());

        Assertions.assertEquals(StringHelper.generateString(6), updatedResource.getName());
        Assertions.assertEquals(StringHelper.generateString(6), updatedResource.getDescription());
        Assertions.assertEquals("2018-11-09 00:00:00", updatedResource.getExpirationdate());
        Assertions.assertEquals(StringHelper.generateString(6), updatedResource.getFilechecksum());
        Assertions.assertEquals(StringHelper.generateString(6), updatedResource.getRelativepath());
        Assertions.assertEquals(StringHelper.generateString(6), updatedResource.getReport());
        Assertions.assertEquals("2", updatedResource.getSize());
        Assertions.assertEquals(ResourceStatusEnum.ARCHIVED.getLabel(), updatedResource.getStatus());
        Assertions.assertEquals(Long.valueOf(STORAGE_ID), updatedResource.getStorage().getId());
        Assertions.assertEquals(Long.valueOf(WORKUNIT_ID), updatedResource.getWorkunit().getId());

        deleteResource(resource.getId());
    }
}
