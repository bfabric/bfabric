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

import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRenameDatasetTemplateAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDatasetTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetTemplateAttributePositions;
import org.bfabric.xml.entity.XMLDatasetTemplate;
import org.bfabric.xml.entity.XMLDatasetTemplateAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPDatasetTemplateIT extends AbstractIT {

    @Test
    public void attributeNameShouldBeUpdated() {
        XMLDatasetTemplate datasetTemplate = createDatasetTemplate();

        XMLRequestParameterRenameDatasetTemplateAttribute xmlRequestRenameDatasetTemplateAttribute = new XMLRequestParameterRenameDatasetTemplateAttribute();
        xmlRequestRenameDatasetTemplateAttribute.setDatasettemplateid(datasetTemplate.getId().toString());
        xmlRequestRenameDatasetTemplateAttribute.setOldname("Attribute1");
        xmlRequestRenameDatasetTemplateAttribute.setNewname("updated attribute1");

        XMLDatasetTemplate updatedDatasetTemplate = getSoapClient().getEpDatasetTemplate().getWmRenameAttribute().rename(xmlRequestRenameDatasetTemplateAttribute);

        Assertions.assertNull(updatedDatasetTemplate.getErrorreport());
        Assertions.assertEquals("updated attribute1", updatedDatasetTemplate.getDatasettemplateattributes().get(0).getName());

        deleteDatasetTemplate(datasetTemplate.getId());
    }

    @Test
    public void attributePositionsShouldBeSwitched() {
        XMLDatasetTemplate datasetTemplate = createDatasetTemplate();

        XMLRequestParameterSwitchDatasetTemplateAttributePositions xmlRequestSwitchDatasetTemplateAttributePositions = new XMLRequestParameterSwitchDatasetTemplateAttributePositions();
        xmlRequestSwitchDatasetTemplateAttributePositions.setDatasettemplateid(datasetTemplate.getId().toString());
        xmlRequestSwitchDatasetTemplateAttributePositions.setAttributename1(datasetTemplate.getDatasettemplateattributes().get(0).getName());
        xmlRequestSwitchDatasetTemplateAttributePositions.setAttributename2(datasetTemplate.getDatasettemplateattributes().get(1).getName());

        XMLDatasetTemplate updatedDatasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSwitchAttributePositions()
            .switchDatasetTemplateAttributePositions(xmlRequestSwitchDatasetTemplateAttributePositions);

        Assertions.assertNull(updatedDatasetTemplate.getErrorreport());

        // Test attributes
        Assertions.assertEquals("attribute2", updatedDatasetTemplate.getDatasettemplateattributes().get(0).getName());
        Assertions.assertEquals("1", updatedDatasetTemplate.getDatasettemplateattributes().get(0).getPosition());
        Assertions.assertEquals("String", updatedDatasetTemplate.getDatasettemplateattributes().get(0).getType());
        Assertions.assertEquals("attribute1", updatedDatasetTemplate.getDatasettemplateattributes().get(1).getName());
        Assertions.assertEquals("2", updatedDatasetTemplate.getDatasettemplateattributes().get(1).getPosition());
        Assertions.assertEquals("Integer", updatedDatasetTemplate.getDatasettemplateattributes().get(1).getType());

        deleteDatasetTemplate(datasetTemplate.getId());
    }

    public XMLDatasetTemplate createDatasetTemplate() {
        // TestDatasetTemplate with two attributes: |attribute1|attribute2| of type |1(Integer)|2(String)|
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();
        xmlRequestSaveDatasetTemplate.setName(GENERATED_NAME);
        xmlRequestSaveDatasetTemplate.setSupervisorid("703");

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute1 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute1.setName("attribute1");
        xmlDatasetTemplateAttribute1.setPosition("1");
        xmlDatasetTemplateAttribute1.setType("Integer");
        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute2 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute2.setName("attribute2");
        xmlDatasetTemplateAttribute2.setPosition("2");
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(Arrays.asList(xmlDatasetTemplateAttribute1, xmlDatasetTemplateAttribute2));

        XMLDatasetTemplate datasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);
        if (datasetTemplate.getErrorreport() != null) {
            throw new SoapClientException("Could not create dataset template: " + datasetTemplate.getErrorreport());
        }

        return datasetTemplate;
    }

    @Test
    public void datasetShouldBeCreated() {
        XMLDatasetTemplate datasetTemplate = createDatasetTemplate();

        Assertions.assertNull(datasetTemplate.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, datasetTemplate.getName());

        // Test attribute 1
        // Auto capitalization test included
        Assertions.assertEquals("attribute1", datasetTemplate.getDatasettemplateattributes().get(0).getName());
        Assertions.assertEquals("1", datasetTemplate.getDatasettemplateattributes().get(0).getPosition());
        Assertions.assertEquals("Integer", datasetTemplate.getDatasettemplateattributes().get(0).getType());
        // Test attribute 2
        // Auto capitalization test included
        Assertions.assertEquals("attribute2", datasetTemplate.getDatasettemplateattributes().get(1).getName());
        Assertions.assertEquals("2", datasetTemplate.getDatasettemplateattributes().get(1).getPosition());
        Assertions.assertEquals("String", datasetTemplate.getDatasettemplateattributes().get(1).getType());

        // Test number of attributes/items
        Assertions.assertEquals("2", datasetTemplate.getNumberofattributes());

        deleteDatasetTemplate(datasetTemplate.getId());
    }

    @Test
    public void datasetShouldBeRead() {
        XMLDatasetTemplate datasetTemplate = createDatasetTemplate();

        XMLDatasetTemplate readDatasetTemplate = getSoapClient().getEpDatasetTemplate().getWmRead().getEntity(datasetTemplate.getId());
        XMLDatasetTemplate readDatasetTemplateDuplicate = getSoapClient().getEpDatasetTemplate().getWmRead().getEntity(readDatasetTemplate.getId());

        Assertions.assertSame(readDatasetTemplate, readDatasetTemplateDuplicate);

        Assertions.assertNull(readDatasetTemplate.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, readDatasetTemplate.getName());

        // Test attribute 1
        // Auto capitalization test included
        Assertions.assertEquals("attribute1", readDatasetTemplate.getDatasettemplateattributes().get(0).getName());
        Assertions.assertEquals("1", readDatasetTemplate.getDatasettemplateattributes().get(0).getPosition());
        Assertions.assertEquals("Integer", readDatasetTemplate.getDatasettemplateattributes().get(0).getType());
        // Test attribute 2
        // Auto capitalization test included
        Assertions.assertEquals("attribute2", readDatasetTemplate.getDatasettemplateattributes().get(1).getName());
        Assertions.assertEquals("2", readDatasetTemplate.getDatasettemplateattributes().get(1).getPosition());
        Assertions.assertEquals("String", readDatasetTemplate.getDatasettemplateattributes().get(1).getType());

        deleteDatasetTemplate(datasetTemplate.getId());
    }

    @Test
    public void datasetTemplateShouldBeDeleted() {
        XMLDatasetTemplate datasetTemplate = createDatasetTemplate();

        XMLDatasetTemplate deletedDatasetTemplate = getSoapClient().getEpDatasetTemplate().getWmDelete().delete(datasetTemplate.getId());

        Assertions.assertNull(deletedDatasetTemplate.getErrorreport());
    }

    @Test
    public void datasetTemplateShouldBeUpdated() {
        XMLDatasetTemplate datasetTemplate = createDatasetTemplate();
        //
        // Test updated dataset:
        //
        // Attribute1 Attribute2
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();

        xmlRequestSaveDatasetTemplate.setId(datasetTemplate.getId());

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute1 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute1.setName("updated attribute1");
        xmlDatasetTemplateAttribute1.setPosition("1");
        xmlDatasetTemplateAttribute1.setType("String");
        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute2 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute2.setName("updated attribute2");
        xmlDatasetTemplateAttribute2.setPosition("2");
        xmlDatasetTemplateAttribute2.setType("Integer");

        xmlRequestSaveDatasetTemplate.setName(GENERATED_NAME);
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(Arrays.asList(xmlDatasetTemplateAttribute1, xmlDatasetTemplateAttribute2));

        XMLDatasetTemplate updatedDatasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);

        Assertions.assertNull(updatedDatasetTemplate.getErrorreport());

        Assertions.assertEquals(GENERATED_NAME, updatedDatasetTemplate.getName());

        // Test attribute 1
        // Auto capitalization test included
        Assertions.assertEquals("updated attribute1", updatedDatasetTemplate.getDatasettemplateattributes().get(0).getName());
        Assertions.assertEquals("1", updatedDatasetTemplate.getDatasettemplateattributes().get(0).getPosition());
        Assertions.assertEquals("String", updatedDatasetTemplate.getDatasettemplateattributes().get(0).getType());
        // Test attribute 2
        // Auto capitalization test included
        Assertions.assertEquals("updated attribute2", updatedDatasetTemplate.getDatasettemplateattributes().get(1).getName());
        Assertions.assertEquals("2", updatedDatasetTemplate.getDatasettemplateattributes().get(1).getPosition());
        Assertions.assertEquals("Integer", updatedDatasetTemplate.getDatasettemplateattributes().get(1).getType());

        deleteDatasetTemplate(datasetTemplate.getId());
    }

    @Test
    public void datasetTemplateShouldNotBeCreatedDueToDuplicatedAttributeNames() {
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();

        List<XMLDatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute1 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute1.setName("foo");
        xmlDatasetTemplateAttribute1.setPosition("1");
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute1);
        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute2 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute2.setName("foo");
        xmlDatasetTemplateAttribute2.setPosition("2");
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute2);

        xmlRequestSaveDatasetTemplate.setName(GENERATED_NAME);
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(datasetTemplateAttributes);

        XMLDatasetTemplate datasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);

        Assertions.assertNotNull(datasetTemplate.getErrorreport());
        Assertions.assertEquals("DatasetTemplateAttribute name at position 2 is not unique!", datasetTemplate.getErrorreport());
    }

    @Test
    public void datasetTemplateShouldNotBeCreatedDueToDuplicatedAttributePositions() {
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();

        List<XMLDatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute1 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute1.setName("foo");
        xmlDatasetTemplateAttribute1.setPosition("1");
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute1);
        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute2 = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute2.setName("bar");
        xmlDatasetTemplateAttribute2.setPosition("1");
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute2);

        xmlRequestSaveDatasetTemplate.setName(GENERATED_NAME);
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(datasetTemplateAttributes);

        XMLDatasetTemplate datasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);

        Assertions.assertNotNull(datasetTemplate.getErrorreport());
        Assertions.assertEquals("DatasetTemplateAttribute position 1 is not unique!", datasetTemplate.getErrorreport());
    }

    @Test
    public void datasetTemplateShouldNotBeCreatedDueToNonSpecifiedAttributeName() {
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute.setPosition("1");
        List<XMLDatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute);

        xmlRequestSaveDatasetTemplate.setName(GENERATED_NAME);
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(datasetTemplateAttributes);

        XMLDatasetTemplate datasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);

        Assertions.assertNotNull(datasetTemplate.getErrorreport());
        Assertions.assertEquals("No value specified for datasetTemplateAttribute name!", datasetTemplate.getErrorreport());
    }

    @Test
    public void datasetTemplateShouldNotBeCreatedDueToNonSpecifiedAttributePosition() {
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute.setName(GENERATED_NAME);
        List<XMLDatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute);

        xmlRequestSaveDatasetTemplate.setName(GENERATED_NAME);
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(datasetTemplateAttributes);

        XMLDatasetTemplate datasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);

        Assertions.assertNotNull(datasetTemplate.getErrorreport());
        Assertions.assertEquals("No value specified for datasetTemplateAttribute position!", datasetTemplate.getErrorreport());
    }

    @Test
    public void datasetTemplateShouldNotBeCreatedDueToTooLongName() {
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute.setName(GENERATED_NAME);
        xmlDatasetTemplateAttribute.setPosition("1");
        List<XMLDatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute);

        xmlRequestSaveDatasetTemplate.setName(StringHelper.generateString(257));
        xmlRequestSaveDatasetTemplate.setSupervisorid("703");
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(datasetTemplateAttributes);

        XMLDatasetTemplate datasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);

        Assertions.assertNotNull(datasetTemplate.getErrorreport());
        Assertions.assertEquals("name size must be between 0 and 256", datasetTemplate.getErrorreport());
    }

    @Test
    public void datasetTemplateShouldNotBeCreatedDueToWrongFormattedAttributePosition() {
        XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate = new XMLRequestParameterSaveDatasetTemplate();

        XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute = new XMLDatasetTemplateAttribute();
        xmlDatasetTemplateAttribute.setName(GENERATED_NAME);
        xmlDatasetTemplateAttribute.setPosition(GENERATED_NAME);
        List<XMLDatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();
        datasetTemplateAttributes.add(xmlDatasetTemplateAttribute);

        xmlRequestSaveDatasetTemplate.setName(GENERATED_NAME);
        xmlRequestSaveDatasetTemplate.setDatasettemplateattribute(datasetTemplateAttributes);

        XMLDatasetTemplate datasetTemplate = getSoapClient().getEpDatasetTemplate().getWmSave().save(xmlRequestSaveDatasetTemplate);

        Assertions.assertNotNull(datasetTemplate.getErrorreport());
        Assertions.assertEquals("datasetTemplateAttribute position " + GENERATED_NAME + " is not a long (numeric) value!", datasetTemplate.getErrorreport());
    }

    public void deleteDatasetTemplate(Long id) {
        getSoapClient().getEpDatasetTemplate().getWmDelete().delete(id);
    }
}
