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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveSample;
import org.bfabric.xml.entity.XMLCustomAttribute;
import org.bfabric.xml.entity.XMLSample;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EPSampleIT extends AbstractIT {

    private static final String CONTAINER_ID_EXTENSIBLE = "403";

    private static final String CONTAINER_ID_NON_EXTENSIBLE = "129";

    @AfterAll
    public static void afterAll() {
    }

    @BeforeAll
    public static void beforeAll() {
    }

    public XMLSample createSample() {
        XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();

        XMLCustomAttribute customAttribute1 = new XMLCustomAttribute();
        customAttribute1.setName("custom attribute1");
        customAttribute1.setValue("value1");
        XMLCustomAttribute customAttribute2 = new XMLCustomAttribute();
        customAttribute2.setName("custom attribute2");
        customAttribute2.setValue("value2");

        xmlRequestSaveSample.setName(S5);
        xmlRequestSaveSample.setContainerid(CONTAINER_ID);
        xmlRequestSaveSample.setType(SampleTypeEnum.GENERIC.getLabel());
        xmlRequestSaveSample.setCustomattribute(Arrays.asList(customAttribute1, customAttribute2));

        return getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);
    }

    public void deleteSample(Long id) {
        getSoapClient().getEpSample().getWmDelete().delete(id);
    }

    @Test
    public void sampleShouldBeCreated() {
        if (getBeforeAllFailedErrorMessage() != null) {
            Assertions.fail(getBeforeAllFailedErrorMessage());
        }

        XMLSample sample = createSample();

        Assertions.assertNull(sample.getErrorreport());

        Assertions.assertEquals(S5, sample.getName());
        Assertions.assertEquals(Long.valueOf(CONTAINER_ID), sample.getContainer().getId());
        Assertions.assertEquals(SampleTypeEnum.GENERIC.getLabel(), sample.getType());

        Assertions.assertEquals("custom attribute1", sample.getCustomattribute().get(0).getName());
        Assertions.assertEquals("value1", sample.getCustomattribute().get(0).getValue());
        Assertions.assertEquals("String", sample.getCustomattribute().get(0).getType());
        Assertions.assertEquals("custom attribute2", sample.getCustomattribute().get(1).getName());
        Assertions.assertEquals("value2", sample.getCustomattribute().get(1).getValue());
        Assertions.assertEquals("String", sample.getCustomattribute().get(1).getType());

        deleteSample(sample.getId());
    }

    @Test
    public void sampleShouldBeDeleted() {
        XMLSample sample = createSample();

        XMLSample deletedSample = getSoapClient().getEpSample().getWmDelete().delete(sample.getId());

        Assertions.assertNull(deletedSample.getErrorreport());
        Assertions.assertNull(deletedSample.getId());
    }

    @Test
    public void sampleShouldBeRead() {
        XMLSample sample = createSample();

        XMLSample readSample = getSoapClient().getEpSample().getWmRead().getEntity(sample.getId());
        XMLSample readSampleDuplicate = getSoapClient().getEpSample().getWmRead().getEntity(sample.getId());

        Assertions.assertNotNull(readSample);
        Assertions.assertNotNull(readSampleDuplicate);

        Assertions.assertSame(readSampleDuplicate, readSample);

        Assertions.assertNull(readSample.getErrorreport());

        Assertions.assertEquals(S5, readSample.getName());
        Assertions.assertEquals(Long.valueOf(CONTAINER_ID), readSample.getContainer().getId());
        Assertions.assertEquals(SampleTypeEnum.GENERIC.getLabel(), readSample.getType());

        Assertions.assertEquals("custom attribute1", readSample.getCustomattribute().get(0).getName());
        Assertions.assertEquals("value1", readSample.getCustomattribute().get(0).getValue());
        Assertions.assertEquals("String", readSample.getCustomattribute().get(0).getType());
        Assertions.assertEquals("custom attribute2", readSample.getCustomattribute().get(1).getName());
        Assertions.assertEquals("value2", readSample.getCustomattribute().get(1).getValue());
        Assertions.assertEquals("String", readSample.getCustomattribute().get(1).getType());

        deleteSample(sample.getId());
    }

    @Test
    public void sampleShouldBeUpdated() {
        XMLSample sample = createSample();

        XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();

        XMLCustomAttribute customAttribute1 = new XMLCustomAttribute();
        customAttribute1.setName("custom attribute updated1");
        customAttribute1.setValue("value updated1");
        XMLCustomAttribute customAttribute2 = new XMLCustomAttribute();
        customAttribute2.setName("custom attribute updated2");
        customAttribute2.setValue("value updated2");

        xmlRequestSaveSample.setId(sample.getId());
        xmlRequestSaveSample.setName(StringHelper.generateString(6));
        xmlRequestSaveSample.setContainerid(CONTAINER_ID_EXTENSIBLE);
        xmlRequestSaveSample.setType(SampleTypeEnum.CHEMICAL.getLabel());
        xmlRequestSaveSample.setCustomattribute(Arrays.asList(customAttribute1, customAttribute2));

        XMLSample updatedSample = getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);

        Assertions.assertNull(updatedSample.getErrorreport());

        Assertions.assertEquals(StringHelper.generateString(6), updatedSample.getName());
        Assertions.assertEquals(Long.valueOf(CONTAINER_ID_EXTENSIBLE), updatedSample.getContainer().getId());
        Assertions.assertEquals(SampleTypeEnum.CHEMICAL.getLabel(), updatedSample.getType());

        Assertions.assertEquals("custom attribute updated1", updatedSample.getCustomattribute().get(0).getName());
        Assertions.assertEquals("value updated1", updatedSample.getCustomattribute().get(0).getValue());
        Assertions.assertEquals("String", updatedSample.getCustomattribute().get(0).getType());
        Assertions.assertEquals("custom attribute updated2", updatedSample.getCustomattribute().get(1).getName());
        Assertions.assertEquals("value updated2", updatedSample.getCustomattribute().get(1).getValue());
        Assertions.assertEquals("String", updatedSample.getCustomattribute().get(1).getType());

        deleteSample(sample.getId());
    }

    @Test
    public void sampleShouldNotBeCreatedDueToNonExtensibleContainer() {
        XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();

        xmlRequestSaveSample.setName(S5);
        xmlRequestSaveSample.setContainerid(CONTAINER_ID_NON_EXTENSIBLE);
        xmlRequestSaveSample.setType(SampleTypeEnum.GENERIC.getLabel());

        XMLSample sample = getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);

        Assertions.assertNotNull(sample.getErrorreport());
        Assertions.assertNull(sample.getId());
        Assertions.assertNull(sample.getContainer());
        Assertions.assertEquals("Container " + CONTAINER_ID_NON_EXTENSIBLE + " is not extensible!", sample.getErrorreport());
    }

    @Test
    public void sampleShouldNotBeCreatedDueToNonSpecifiedContainer() {
        XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();

        xmlRequestSaveSample.setName(S5);
        xmlRequestSaveSample.setType(SampleTypeEnum.GENERIC.getLabel());

        XMLSample sample = getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);

        Assertions.assertNotNull(sample.getErrorreport());
        Assertions.assertNull(sample.getId());
        Assertions.assertNull(sample.getContainer());
        Assertions.assertTrue(sample.getErrorreport().equals("parentId must not be null") || sample.getErrorreport().equals("parentClassName must not be null") || sample.getErrorreport()
            .equals("container must not be null"));
    }

    @Test
    public void sampleShouldNotBeCreatedDueToNonSpecifiedName() {
        XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();

        xmlRequestSaveSample.setContainerid(CONTAINER_ID);
        xmlRequestSaveSample.setType(SampleTypeEnum.GENERIC.getLabel());

        XMLSample sample = getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);

        Assertions.assertNotNull(sample.getErrorreport());
        Assertions.assertNull(sample.getId());
        Assertions.assertNull(sample.getName());
        Assertions.assertEquals("name must not be blank", sample.getErrorreport());
    }

    @Test
    public void sampleShouldNotBeCreatedDueToNonSpecifiedType() {
        XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();

        xmlRequestSaveSample.setName(S5);
        xmlRequestSaveSample.setContainerid(CONTAINER_ID);

        XMLSample sample = getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);

        Assertions.assertNotNull(sample.getErrorreport());
        Assertions.assertNull(sample.getId());
        Assertions.assertNull(sample.getType());
        Assertions.assertEquals("No value specified for type!", sample.getErrorreport());
    }

    @Test
    public void sampleShouldNotBeCreatedDueToNonUniqueName() {
        XMLSample sample = createSample();

        XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();

        xmlRequestSaveSample.setName(S5);
        xmlRequestSaveSample.setContainerid(CONTAINER_ID);
        xmlRequestSaveSample.setType(SampleTypeEnum.GENERIC.getLabel());

        XMLSample sampleDuplicate = getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);

        Assertions.assertNotNull(sampleDuplicate.getErrorreport());
        Assertions.assertNull(sampleDuplicate.getId());
        Assertions.assertNull(sampleDuplicate.getName());

        deleteSample(sample.getId());
    }

    @Test
    public void testSampleHierarchies() {
        int children = 3;
        XMLSample parentSample = createSample();
        for (int i = 1; i <= children; i++) {
            XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();
            xmlRequestSaveSample.setName(S5 + LocalDateTime.now());
            xmlRequestSaveSample.setContainerid(CONTAINER_ID);
            xmlRequestSaveSample.setType(SampleTypeEnum.GENERIC.getLabel());
            xmlRequestSaveSample.setParentid(Collections.singletonList(parentSample.getIdString()));
            getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample);
        }
        for (XMLSample xmlSample : parentSample.getChild()) {
            getSoapClient().getEpSample().getWmDelete().delete(xmlSample.getId());
        }
        getSoapClient().getEpSample().getWmDelete().delete(parentSample.getId());
    }
}
