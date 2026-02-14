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
import java.util.List;

import org.bfabric.Constants;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRemoveDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRenameDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDataset;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetAttributePositions;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetItemPositions;
import org.bfabric.xml.entity.XMLDataset;
import org.bfabric.xml.entity.XMLDatasetAttribute;
import org.bfabric.xml.entity.XMLDatasetField;
import org.bfabric.xml.entity.XMLDatasetItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPDatasetIT extends AbstractIT {

    public final String CONTAINERID = "403";

    public final String A1 = "A1";

    public final String A2 = "A2";

    public final String A1_NEW = "A1New";

    public final String A2_NEW = "A2New";

    public final String V1 = "1";

    public final String V2 = "2";

    public final String V3 = "3";

    public final String V4 = "4";

    public final String P1 = "1";

    public final String P2 = "2";

    public final String T1 = "String";

    public final String T2 = "Integer";

    @Test
    public void attributeNameShouldBeUpdated() {
        XMLDataset dataset = createDataset();

        XMLRequestParameterRenameDatasetAttribute xmlRequestRenameDatasetAttribute = new XMLRequestParameterRenameDatasetAttribute();
        xmlRequestRenameDatasetAttribute.setDatasetid(dataset.getId().toString());
        xmlRequestRenameDatasetAttribute.setOldname(A1);
        xmlRequestRenameDatasetAttribute.setNewname(A1_NEW);

        XMLDataset updatedDataset = getSoapClient().getEpDataset().getWmRenameAttribute().rename(xmlRequestRenameDatasetAttribute);

        Assertions.assertNull(updatedDataset.getErrorreport());
        Assertions.assertNotNull(updatedDataset.getId());
        Assertions.assertEquals(A1_NEW, updatedDataset.getAttribute().get(0).getName());

        deleteDataset(dataset.getId());
    }

    @Test
    public void attributePositionsShouldBeSwitched() {
        XMLDataset dataset = createDataset();

        XMLRequestParameterSwitchDatasetAttributePositions xmlRequestSwitchDatasetAttributePositions = new XMLRequestParameterSwitchDatasetAttributePositions();
        xmlRequestSwitchDatasetAttributePositions.setDatasetid(dataset.getId().toString());
        xmlRequestSwitchDatasetAttributePositions.setAttributename1(dataset.getAttribute().get(0).getName());
        xmlRequestSwitchDatasetAttributePositions.setAttributename2(dataset.getAttribute().get(1).getName());

        XMLDataset updatedDataset = getSoapClient().getEpDataset().getWmSwitchAttributePositions().switchAttributePositions(xmlRequestSwitchDatasetAttributePositions);

        Assertions.assertNull(updatedDataset.getErrorreport());
        Assertions.assertNotNull(updatedDataset.getId());

        Assertions.assertEquals(A2, updatedDataset.getAttribute().get(0).getName());
        Assertions.assertEquals(P1, updatedDataset.getAttribute().get(0).getPosition());
        Assertions.assertEquals(T1, updatedDataset.getAttribute().get(0).getType());
        Assertions.assertEquals(A1, updatedDataset.getAttribute().get(1).getName());
        Assertions.assertEquals(P2, updatedDataset.getAttribute().get(1).getPosition());
        Assertions.assertEquals(T2, updatedDataset.getAttribute().get(1).getType());

        Assertions.assertEquals(V2, updatedDataset.getItem().get(0).getField().get(0).getValue());
        Assertions.assertEquals(V1, updatedDataset.getItem().get(0).getField().get(1).getValue());
        Assertions.assertEquals(V4, updatedDataset.getItem().get(1).getField().get(0).getValue());
        Assertions.assertEquals(V3, updatedDataset.getItem().get(1).getField().get(1).getValue());

        deleteDataset(dataset.getId());
    }

    @Test
    public void attributeShouldBeAdded() {
        XMLDataset dataset = createDataset();

        XMLRequestParameterAddDatasetAttribute xmlRequestAddDatasetAttribute = new XMLRequestParameterAddDatasetAttribute();
        xmlRequestAddDatasetAttribute.setDatasetid(dataset.getId().toString());
        xmlRequestAddDatasetAttribute.setNewattribute(A1_NEW);
        xmlRequestAddDatasetAttribute.setNewattributetype(T1);
        xmlRequestAddDatasetAttribute.setNewattributevalue("123");

        XMLDataset updatedDataset = getSoapClient().getEpDataset().getWmAddAttribute().add(xmlRequestAddDatasetAttribute);

        Assertions.assertNull(updatedDataset.getErrorreport());
        Assertions.assertEquals(A1_NEW, updatedDataset.getAttribute().get(updatedDataset.getAttribute().size() - 1).getName());
        Assertions.assertEquals(T1, updatedDataset.getAttribute().get(updatedDataset.getAttribute().size() - 1).getType());

        deleteDataset(dataset.getId());
    }

    @Test
    public void attributeShouldBeRemoved() {
        XMLDataset dataset = createDataset();

        XMLRequestParameterRemoveDatasetAttribute xmlRequestRemoveDatasetAttribute = new XMLRequestParameterRemoveDatasetAttribute();
        xmlRequestRemoveDatasetAttribute.setDatasetid(dataset.getId().toString());
        xmlRequestRemoveDatasetAttribute.setAttribute(A1);

        XMLDataset updatedDataset = getSoapClient().getEpDataset().getWmRemoveAttribute().remove(xmlRequestRemoveDatasetAttribute);

        Assertions.assertNull(updatedDataset.getErrorreport());
        Assertions.assertNotNull(updatedDataset.getId());
        Assertions.assertEquals(A2, updatedDataset.getAttribute().get(0).getName());

        deleteDataset(dataset.getId());
    }

    public XMLDataset createDataset() {

        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute1 = new XMLDatasetAttribute();
        xmlDatasetAttribute1.setName(A1);
        xmlDatasetAttribute1.setPosition(P1);
        xmlDatasetAttribute1.setType(T2);
        XMLDatasetAttribute xmlDatasetAttribute2 = new XMLDatasetAttribute();
        xmlDatasetAttribute2.setName(A2);
        xmlDatasetAttribute2.setPosition(P2);

        XMLDatasetField xmlDatasetField1 = new XMLDatasetField();
        xmlDatasetField1.setAttributeposition(P1);
        xmlDatasetField1.setValue(V1);
        XMLDatasetField xmlDatasetField2 = new XMLDatasetField();
        xmlDatasetField2.setAttributeposition(P2);
        xmlDatasetField2.setValue(V2);
        XMLDatasetField xmlDatasetField3 = new XMLDatasetField();
        xmlDatasetField3.setAttributeposition(P1);
        xmlDatasetField3.setValue(V3);
        XMLDatasetField xmlDatasetField4 = new XMLDatasetField();
        xmlDatasetField4.setAttributeposition(P2);
        xmlDatasetField4.setValue(V4);

        XMLDatasetItem xmlDatasetItem1 = new XMLDatasetItem();
        xmlDatasetItem1.setPosition(P1);
        xmlDatasetItem1.setField(Arrays.asList(xmlDatasetField1, xmlDatasetField2));
        XMLDatasetItem xmlDatasetItem2 = new XMLDatasetItem();
        xmlDatasetItem2.setPosition(P2);
        xmlDatasetItem2.setField(Arrays.asList(xmlDatasetField3, xmlDatasetField4));

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(Arrays.asList(xmlDatasetAttribute1, xmlDatasetAttribute2));
        xmlRequestSaveDataset.setItem(Arrays.asList(xmlDatasetItem1, xmlDatasetItem2));

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        if (dataset.getErrorreport() != null) {
            throw new SoapClientException("Could not create dataset: " + dataset.getErrorreport());
        }

        return dataset;
    }

    public XMLDataset createDatasetViaTSV() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();
        xmlRequestSaveDataset.setContenttsv(A1 + "\t" + A2 + "\n" + V1 + "\t" + V2 + "\n" + V3 + "\t" + V4 + "\n");
        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        if (dataset.getErrorreport() != null) {
            throw new SoapClientException("Could not create dataset: " + dataset.getErrorreport());
        }

        return dataset;
    }

    @Test
    public void datasetShouldBeCreated() {
        XMLDataset dataset = createDataset();

        Assertions.assertNull(dataset.getErrorreport());
        Assertions.assertNotNull(dataset.getId());
        Assertions.assertNotNull(dataset.getName());
        Assertions.assertNotNull(dataset.getContainer().getId());
        Assertions.assertNotNull(dataset.getAttribute());
        Assertions.assertNotNull(dataset.getAttribute().get(0));
        Assertions.assertNotNull(dataset.getAttribute().get(0).getName());
        Assertions.assertNotNull(dataset.getAttribute().get(0).getPosition());
        Assertions.assertNotNull(dataset.getAttribute().get(0).getType());
        Assertions.assertNotNull(dataset.getAttribute().get(1).getName());
        Assertions.assertNotNull(dataset.getAttribute().get(1).getPosition());
        Assertions.assertNotNull(dataset.getAttribute().get(1).getType());
        Assertions.assertNotNull(dataset.getItem());
        Assertions.assertNotNull(dataset.getItem().get(0));
        Assertions.assertNotNull(dataset.getItem().get(0).getPosition());
        Assertions.assertNotNull(dataset.getItem().get(0).getField());
        Assertions.assertNotNull(dataset.getItem().get(0).getField().get(0));
        Assertions.assertNotNull(dataset.getItem().get(0).getField().get(1));
        Assertions.assertNotNull(dataset.getItem().get(1));
        Assertions.assertNotNull(dataset.getItem().get(1).getPosition());
        Assertions.assertNotNull(dataset.getItem().get(1).getField());
        Assertions.assertNotNull(dataset.getItem().get(1).getField().get(0));
        Assertions.assertNotNull(dataset.getItem().get(1).getField().get(1));

        Assertions.assertEquals(GENERATED_NAME, dataset.getName());
        Assertions.assertEquals(5, dataset.getName().length());
        Assertions.assertEquals(Long.valueOf(CONTAINERID), dataset.getContainer().getId());

        Assertions.assertEquals(A1, dataset.getAttribute().get(0).getName());
        Assertions.assertEquals(P1, dataset.getAttribute().get(0).getPosition());
        Assertions.assertEquals(T2, dataset.getAttribute().get(0).getType());
        Assertions.assertEquals(A2, dataset.getAttribute().get(1).getName());
        Assertions.assertEquals(P2, dataset.getAttribute().get(1).getPosition());
        Assertions.assertEquals(T1, dataset.getAttribute().get(1).getType());

        Assertions.assertEquals(P1, dataset.getItem().get(0).getPosition());
        Assertions.assertEquals(V1, dataset.getItem().get(0).getField().get(0).getValue());
        Assertions.assertEquals(P1, dataset.getItem().get(0).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V2, dataset.getItem().get(0).getField().get(1).getValue());
        Assertions.assertEquals(P2, dataset.getItem().get(0).getField().get(1).getAttributeposition());

        Assertions.assertEquals(P2, dataset.getItem().get(1).getPosition());
        Assertions.assertEquals(V3, dataset.getItem().get(1).getField().get(0).getValue());
        Assertions.assertEquals(P1, dataset.getItem().get(1).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V4, dataset.getItem().get(1).getField().get(1).getValue());
        Assertions.assertEquals(P2, dataset.getItem().get(1).getField().get(1).getAttributeposition());

        Assertions.assertEquals("2", dataset.getNumberofattributes());
        Assertions.assertEquals("2", dataset.getNumberofitems());

        deleteDataset(dataset.getId());
    }

    @Test
    public void datasetShouldBeDeleted() {
        XMLDataset dataset = createDataset();

        XMLDataset deletedDataset = getSoapClient().getEpDataset().getWmDelete().delete(dataset.getId());

        Assertions.assertNull(deletedDataset.getErrorreport());
        Assertions.assertNull(deletedDataset.getId());
    }

    @Test
    public void datasetShouldBeRead() {
        XMLDataset dataset = createDataset();

        XMLDataset readDataset = getSoapClient().getEpDataset().getWmRead().getEntity(dataset.getId());
        XMLDataset readDatasetDuplicate = getSoapClient().getEpDataset().getWmRead().getEntity(readDataset.getId());

        Assertions.assertNotNull(readDataset);
        Assertions.assertNotNull(readDatasetDuplicate);

        Assertions.assertSame(readDataset, readDatasetDuplicate);

        Assertions.assertNull(readDataset.getErrorreport());
        Assertions.assertNotNull(readDataset.getId());
        Assertions.assertNotNull(readDataset.getName());
        Assertions.assertNotNull(readDataset.getContainer());
        Assertions.assertNotNull(readDataset.getAttribute());
        Assertions.assertNotNull(readDataset.getAttribute().get(0));
        Assertions.assertNotNull(readDataset.getAttribute().get(0).getName());
        Assertions.assertNotNull(readDataset.getAttribute().get(0).getPosition());
        Assertions.assertNotNull(readDataset.getAttribute().get(0).getType());
        Assertions.assertNotNull(readDataset.getAttribute().get(1).getName());
        Assertions.assertNotNull(readDataset.getAttribute().get(1).getPosition());
        Assertions.assertNotNull(readDataset.getAttribute().get(1).getType());
        Assertions.assertNotNull(readDataset.getItem());
        Assertions.assertNotNull(readDataset.getItem().get(0));
        Assertions.assertNotNull(readDataset.getItem().get(0).getPosition());
        Assertions.assertNotNull(readDataset.getItem().get(0).getField());
        Assertions.assertNotNull(readDataset.getItem().get(0).getField().get(0));
        Assertions.assertNotNull(readDataset.getItem().get(0).getField().get(1));
        Assertions.assertNotNull(readDataset.getItem().get(1));
        Assertions.assertNotNull(readDataset.getItem().get(1).getPosition());
        Assertions.assertNotNull(readDataset.getItem().get(1).getField());
        Assertions.assertNotNull(readDataset.getItem().get(1).getField().get(0));
        Assertions.assertNotNull(readDataset.getItem().get(1).getField().get(1));

        Assertions.assertEquals(GENERATED_NAME, readDataset.getName());
        Assertions.assertEquals(5, readDataset.getName().length());
        Assertions.assertEquals(Long.valueOf(CONTAINERID), readDataset.getContainer().getId());

        Assertions.assertEquals(A1, readDataset.getAttribute().get(0).getName());
        Assertions.assertEquals(P1, readDataset.getAttribute().get(0).getPosition());
        Assertions.assertEquals(T2, readDataset.getAttribute().get(0).getType());
        Assertions.assertEquals(A2, readDataset.getAttribute().get(1).getName());
        Assertions.assertEquals(P2, readDataset.getAttribute().get(1).getPosition());
        Assertions.assertEquals(T1, readDataset.getAttribute().get(1).getType());

        Assertions.assertEquals(P1, readDataset.getItem().get(0).getPosition());
        Assertions.assertEquals(V1, readDataset.getItem().get(0).getField().get(0).getValue());
        Assertions.assertEquals(P1, readDataset.getItem().get(0).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V2, readDataset.getItem().get(0).getField().get(1).getValue());
        Assertions.assertEquals(P2, readDataset.getItem().get(0).getField().get(1).getAttributeposition());

        Assertions.assertEquals(P2, readDataset.getItem().get(1).getPosition());
        Assertions.assertEquals(V3, readDataset.getItem().get(1).getField().get(0).getValue());
        Assertions.assertEquals(P1, readDataset.getItem().get(1).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V4, readDataset.getItem().get(1).getField().get(1).getValue());
        Assertions.assertEquals(P2, readDataset.getItem().get(1).getField().get(1).getAttributeposition());

        deleteDataset(dataset.getId());
    }

    @Test
    public void datasetShouldBeUpdated() {
        XMLDataset dataset = createDataset();

        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        xmlRequestSaveDataset.setId(dataset.getId());

        XMLDatasetAttribute xmlDatasetAttribute1 = new XMLDatasetAttribute();
        xmlDatasetAttribute1.setName(A1_NEW);
        xmlDatasetAttribute1.setPosition(P1);
        xmlDatasetAttribute1.setType(T1);
        XMLDatasetAttribute xmlDatasetAttribute2 = new XMLDatasetAttribute();
        xmlDatasetAttribute2.setName(A2_NEW);
        xmlDatasetAttribute2.setPosition(P2);
        xmlDatasetAttribute2.setType(T2);

        XMLDatasetField xmlDatasetField1 = new XMLDatasetField();
        xmlDatasetField1.setAttributeposition(P1);
        xmlDatasetField1.setValue(V1);
        XMLDatasetField xmlDatasetField2 = new XMLDatasetField();
        xmlDatasetField2.setAttributeposition(P2);
        xmlDatasetField2.setValue(V2);
        XMLDatasetField xmlDatasetField3 = new XMLDatasetField();
        xmlDatasetField3.setAttributeposition(P1);
        xmlDatasetField3.setValue(V3);
        XMLDatasetField xmlDatasetField4 = new XMLDatasetField();
        xmlDatasetField4.setAttributeposition(P2);
        xmlDatasetField4.setValue(V4);

        XMLDatasetItem xmlDatasetItem1 = new XMLDatasetItem();
        xmlDatasetItem1.setPosition(P1);
        xmlDatasetItem1.setField(Arrays.asList(xmlDatasetField1, xmlDatasetField2));
        XMLDatasetItem xmlDatasetItem2 = new XMLDatasetItem();
        xmlDatasetItem2.setPosition(P2);
        xmlDatasetItem2.setField(Arrays.asList(xmlDatasetField3, xmlDatasetField4));

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        String EXTENSIBLECONTAINERID = "403";
        xmlRequestSaveDataset.setContainerid(EXTENSIBLECONTAINERID);
        xmlRequestSaveDataset.setAttribute(Arrays.asList(xmlDatasetAttribute1, xmlDatasetAttribute2));
        xmlRequestSaveDataset.setItem(Arrays.asList(xmlDatasetItem1, xmlDatasetItem2));

        XMLDataset updatedDataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNull(updatedDataset.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, updatedDataset.getName());
        Assertions.assertEquals(5, updatedDataset.getName().length());
        Assertions.assertEquals(Long.valueOf(EXTENSIBLECONTAINERID), updatedDataset.getContainer().getId());

        Assertions.assertEquals(A1_NEW, updatedDataset.getAttribute().get(0).getName());
        Assertions.assertEquals(P1, updatedDataset.getAttribute().get(0).getPosition());
        Assertions.assertEquals(T1, updatedDataset.getAttribute().get(0).getType());
        Assertions.assertEquals(A2_NEW, updatedDataset.getAttribute().get(1).getName());
        Assertions.assertEquals(P2, updatedDataset.getAttribute().get(1).getPosition());
        Assertions.assertEquals(T2, updatedDataset.getAttribute().get(1).getType());

        Assertions.assertEquals(P1, updatedDataset.getItem().get(0).getPosition());
        Assertions.assertEquals(V1, updatedDataset.getItem().get(0).getField().get(0).getValue());
        Assertions.assertEquals(P1, updatedDataset.getItem().get(0).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V2, updatedDataset.getItem().get(0).getField().get(1).getValue());
        Assertions.assertEquals(P2, updatedDataset.getItem().get(0).getField().get(1).getAttributeposition());

        Assertions.assertEquals(P2, updatedDataset.getItem().get(1).getPosition());
        Assertions.assertEquals(V3, updatedDataset.getItem().get(1).getField().get(0).getValue());
        Assertions.assertEquals(P1, updatedDataset.getItem().get(1).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V4, updatedDataset.getItem().get(1).getField().get(1).getValue());
        Assertions.assertEquals(P2, updatedDataset.getItem().get(1).getField().get(1).getAttributeposition());

        deleteDataset(dataset.getId());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToAlreadyUsedWorkunit() {
        XMLDataset dataset = createDataset();

        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();
        xmlRequestSaveDataset.setName(Constants.DATASET);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        String WORKUNITIDWITHDATASET = "131742";
        xmlRequestSaveDataset.setWorkunitid(WORKUNITIDWITHDATASET);

        XMLDataset datasetWithSameWorkunitId = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(datasetWithSameWorkunitId.getErrorreport());
        Assertions.assertEquals("Workunit " + WORKUNITIDWITHDATASET + " is assigned to another dataset!", datasetWithSameWorkunitId.getErrorreport());

        deleteDataset(dataset.getId());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToDuplicatedAttributeNames() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();

        XMLDatasetAttribute xmlDatasetAttribute1 = new XMLDatasetAttribute();
        xmlDatasetAttribute1.setName("foo");
        xmlDatasetAttribute1.setPosition(P1);
        datasetAttributes.add(xmlDatasetAttribute1);
        XMLDatasetAttribute xmlDatasetAttribute2 = new XMLDatasetAttribute();
        xmlDatasetAttribute2.setName("foo");
        xmlDatasetAttribute2.setPosition(P2);
        datasetAttributes.add(xmlDatasetAttribute2);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("Attribute name at position 2 is not unique!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToDuplicatedAttributePositions() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();

        XMLDatasetAttribute xmlDatasetAttribute1 = new XMLDatasetAttribute();
        xmlDatasetAttribute1.setName("foo");
        xmlDatasetAttribute1.setPosition(P1);
        datasetAttributes.add(xmlDatasetAttribute1);
        XMLDatasetAttribute xmlDatasetAttribute2 = new XMLDatasetAttribute();
        xmlDatasetAttribute2.setName("bar");
        xmlDatasetAttribute2.setPosition(P1);
        datasetAttributes.add(xmlDatasetAttribute2);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertEquals("Attribute position 1 is not unique!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonExistingContainer() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(getEntityIdNonExistingAsString());

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertEquals("There is no container with id " + getEntityIdNonExistingAsString() + "!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonExistingWorkunit() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setWorkunitid(getEntityIdNonExistingAsString());

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertNull(dataset.getWorkunit());
        Assertions.assertEquals("There is no workunit with id " + getEntityIdNonExistingAsString() + "!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonExtensibleContainer() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        String NONEXTENSIBLECONTAINERID = "129";
        xmlRequestSaveDataset.setContainerid(NONEXTENSIBLECONTAINERID);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("Container " + NONEXTENSIBLECONTAINERID + " is not extensible!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedAttributeName() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("No value specified for attribute name!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedAttributePosition() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("No value specified for attribute position!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedAttributes() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("No value specified for attributes!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedContainer() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute1 = new XMLDatasetAttribute();
        xmlDatasetAttribute1.setName(A1);
        xmlDatasetAttribute1.setPosition(P1);
        xmlDatasetAttribute1.setType(T2);
        XMLDatasetAttribute xmlDatasetAttribute2 = new XMLDatasetAttribute();
        xmlDatasetAttribute2.setName(A2);
        xmlDatasetAttribute2.setPosition(P2);

        XMLDatasetField xmlDatasetField1 = new XMLDatasetField();
        xmlDatasetField1.setAttributeposition(P1);
        xmlDatasetField1.setValue(V1);
        XMLDatasetField xmlDatasetField2 = new XMLDatasetField();
        xmlDatasetField2.setAttributeposition(P2);
        xmlDatasetField2.setValue(V2);
        XMLDatasetField xmlDatasetField3 = new XMLDatasetField();
        xmlDatasetField3.setAttributeposition(P1);
        xmlDatasetField3.setValue(V3);
        XMLDatasetField xmlDatasetField4 = new XMLDatasetField();
        xmlDatasetField4.setAttributeposition(P2);
        xmlDatasetField4.setValue(V4);

        XMLDatasetItem xmlDatasetItem1 = new XMLDatasetItem();
        xmlDatasetItem1.setPosition(P1);
        xmlDatasetItem1.setField(Arrays.asList(xmlDatasetField1, xmlDatasetField2));
        XMLDatasetItem xmlDatasetItem2 = new XMLDatasetItem();
        xmlDatasetItem2.setPosition(P2);
        xmlDatasetItem2.setField(Arrays.asList(xmlDatasetField3, xmlDatasetField4));

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setAttribute(Arrays.asList(xmlDatasetAttribute1, xmlDatasetAttribute2));
        xmlRequestSaveDataset.setItem(Arrays.asList(xmlDatasetItem1, xmlDatasetItem2));

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertNull(dataset.getContainer());
        Assertions.assertTrue(dataset.getErrorreport().equals("parentId must not be null") || dataset.getErrorreport().equals("parentClassName must not be null") || dataset.getErrorreport()
            .equals("container must not be null"));
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedItemField() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("No. of item fields incorrect. There should be 1!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedItemFieldPosition() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        XMLDatasetField xmlDatasetField = new XMLDatasetField();
        xmlDatasetField.setValue(GENERATED_NAME);
        List<XMLDatasetField> datasetFields = new ArrayList<>();
        datasetFields.add(xmlDatasetField);

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        xmlDatasetItem.setPosition(P1);
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("No. of item fields incorrect. There should be 1!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedItemFieldValue() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        XMLDatasetField xmlDatasetField = new XMLDatasetField();
        xmlDatasetField.setAttributeposition(P1);
        List<XMLDatasetField> datasetFields = new ArrayList<>();
        datasetFields.add(xmlDatasetField);

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        xmlDatasetItem.setPosition(P1);
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("No. of item fields incorrect. There should be 1!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedItemPosition() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        XMLDatasetField xmlDatasetField = new XMLDatasetField();
        List<XMLDatasetField> datasetFields = new ArrayList<>();
        xmlDatasetField.setAttributeposition(P1);
        datasetFields.add(xmlDatasetField);

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        xmlDatasetItem.setField(datasetFields);
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertEquals("No value specified for item position!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToNonSpecifiedName() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        XMLDatasetField xmlDatasetField = new XMLDatasetField();
        xmlDatasetField.setAttributeposition(P1);
        xmlDatasetField.setValue("foo");

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        xmlDatasetItem.setPosition(P1);
        xmlDatasetItem.getField().add(xmlDatasetField);
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertNull(dataset.getId());
        Assertions.assertNull(dataset.getName());
        Assertions.assertEquals("name must not be blank", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToTooLongAttributeName() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(StringHelper.generateString(257));
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        XMLDatasetField xmlDatasetField = new XMLDatasetField();
        xmlDatasetField.setAttributeposition(P1);
        xmlDatasetField.setValue("foo");

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        xmlDatasetItem.setPosition(P1);
        xmlDatasetItem.getField().add(xmlDatasetField);
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertEquals("attribute name must not be longer than " + Constants.MAX_LENGTH_NAME + " characters!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToTooLongName() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        xmlDatasetAttribute.setPosition(P1);
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        XMLDatasetField xmlDatasetField = new XMLDatasetField();
        xmlDatasetField.setAttributeposition(P1);
        xmlDatasetField.setValue("foo");

        XMLDatasetItem xmlDatasetItem = new XMLDatasetItem();
        xmlDatasetItem.setPosition(P1);
        xmlDatasetItem.getField().add(xmlDatasetField);
        List<XMLDatasetItem> datasetItems = new ArrayList<>();
        datasetItems.add(xmlDatasetItem);

        xmlRequestSaveDataset.setName(StringHelper.generateString(257));
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);
        xmlRequestSaveDataset.setItem(datasetItems);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertEquals("name size must be between 0 and 256", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToWrongFormattedAttributePosition() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        XMLDatasetAttribute xmlDatasetAttribute = new XMLDatasetAttribute();
        xmlDatasetAttribute.setName(GENERATED_NAME);
        xmlDatasetAttribute.setPosition(StringHelper.generateString(1));
        List<XMLDatasetAttribute> datasetAttributes = new ArrayList<>();
        datasetAttributes.add(xmlDatasetAttribute);

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setAttribute(datasetAttributes);

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertEquals("attribute position " + StringHelper.generateString(1) + " is not a long (numeric) value!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToWrongFormattedContainerId() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(StringHelper.generateString(1));

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertEquals("containerid " + StringHelper.generateString(1) + " is not a long (numeric) value!", dataset.getErrorreport());
    }

    @Test
    public void datasetShouldNotBeCreatedDueToWrongFormattedWorkunitId() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();

        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);
        xmlRequestSaveDataset.setWorkunitid(StringHelper.generateString(1));

        XMLDataset dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);

        Assertions.assertNotNull(dataset.getErrorreport());
        Assertions.assertEquals("workunitid " + StringHelper.generateString(1) + " is not a long (numeric) value!", dataset.getErrorreport());
    }

    @Test
    public void datasetViaTSVShouldBeCreated() {
        XMLDataset dataset = createDatasetViaTSV();

        Assertions.assertNull(dataset.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, dataset.getName());
        Assertions.assertEquals(5, dataset.getName().length());
        Assertions.assertEquals(Long.valueOf(CONTAINERID), dataset.getContainer().getId());

        Assertions.assertEquals(A1, dataset.getAttribute().get(0).getName());
        Assertions.assertEquals(P1, dataset.getAttribute().get(0).getPosition());
        Assertions.assertEquals(T1, dataset.getAttribute().get(0).getType());
        Assertions.assertEquals(A2, dataset.getAttribute().get(1).getName());
        Assertions.assertEquals(P2, dataset.getAttribute().get(1).getPosition());
        Assertions.assertEquals(T1, dataset.getAttribute().get(1).getType());

        Assertions.assertEquals(P1, dataset.getItem().get(0).getPosition());
        Assertions.assertEquals(V1, dataset.getItem().get(0).getField().get(0).getValue());
        Assertions.assertEquals(P1, dataset.getItem().get(0).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V2, dataset.getItem().get(0).getField().get(1).getValue());
        Assertions.assertEquals(P2, dataset.getItem().get(0).getField().get(1).getAttributeposition());

        Assertions.assertEquals(P2, dataset.getItem().get(1).getPosition());
        Assertions.assertEquals(V3, dataset.getItem().get(1).getField().get(0).getValue());
        Assertions.assertEquals(P1, dataset.getItem().get(1).getField().get(0).getAttributeposition());
        Assertions.assertEquals(V4, dataset.getItem().get(1).getField().get(1).getValue());
        Assertions.assertEquals(P2, dataset.getItem().get(1).getField().get(1).getAttributeposition());

        Assertions.assertEquals("2", dataset.getNumberofattributes());
        Assertions.assertEquals("2", dataset.getNumberofitems());

        deleteDataset(dataset.getId());
    }

    @Test
    public void datasetViaTSVShouldBeNotBeCreated() {
        XMLRequestParameterSaveDataset xmlRequestSaveDataset = new XMLRequestParameterSaveDataset();
        xmlRequestSaveDataset.setContenttsv(A1 + "\t" + A2 + "\n" + V1 + "\t" + V2 + "\n" + V3 + "\t" + V4 + "\tERROR\n");
        xmlRequestSaveDataset.setName(GENERATED_NAME);
        xmlRequestSaveDataset.setContainerid(CONTAINERID);

        XMLDataset dataset = null;
        try {
            dataset = getSoapClient().getEpDataset().getWmSave().save(xmlRequestSaveDataset);
        } catch (Exception e) {
            Assertions.assertNull(dataset);
        }
    }

    public void deleteDataset(Long id) {
        getSoapClient().getEpDataset().getWmDelete().delete(id);
    }

    @Test
    public void itemPositionsShouldBeSwitched() {
        XMLDataset dataset = createDataset();

        XMLRequestParameterSwitchDatasetItemPositions xmlRequestSwitchDatasetItemPositions = new XMLRequestParameterSwitchDatasetItemPositions();
        xmlRequestSwitchDatasetItemPositions.setDatasetid(dataset.getId().toString());
        xmlRequestSwitchDatasetItemPositions.setItemposition1(dataset.getItem().get(0).getPosition());
        xmlRequestSwitchDatasetItemPositions.setItemposition2(dataset.getItem().get(1).getPosition());

        XMLDataset updatedDataset = getSoapClient().getEpDataset().getWmSwitchItemPositions().switchItemPositions(xmlRequestSwitchDatasetItemPositions);

        Assertions.assertNull(updatedDataset.getErrorreport());
        Assertions.assertNotNull(updatedDataset.getId());

        Assertions.assertEquals(A1, updatedDataset.getAttribute().get(0).getName());
        Assertions.assertEquals(P1, updatedDataset.getAttribute().get(0).getPosition());
        Assertions.assertEquals(T2, updatedDataset.getAttribute().get(0).getType());
        Assertions.assertEquals(A2, updatedDataset.getAttribute().get(1).getName());
        Assertions.assertEquals(P2, updatedDataset.getAttribute().get(1).getPosition());
        Assertions.assertEquals(T1, updatedDataset.getAttribute().get(1).getType());

        Assertions.assertEquals(V3, updatedDataset.getItem().get(0).getField().get(0).getValue());
        Assertions.assertEquals(V4, updatedDataset.getItem().get(0).getField().get(1).getValue());
        Assertions.assertEquals(V1, updatedDataset.getItem().get(1).getField().get(0).getValue());
        Assertions.assertEquals(V2, updatedDataset.getItem().get(1).getField().get(1).getValue());

        deleteDataset(dataset.getId());
    }
}
