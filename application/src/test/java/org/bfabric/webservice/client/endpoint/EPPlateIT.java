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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddPlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddPlateSamplesPosition;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRemovePlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRepositionPlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRepositionPlateSamplesPosition;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSavePlate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveSample;
import org.bfabric.xml.entity.XMLCustomAttribute;
import org.bfabric.xml.entity.XMLPlate;
import org.bfabric.xml.entity.XMLSample;
import org.bfabric.xml.entity.XMLSamplePlatePosition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EPPlateIT extends AbstractIT {

    private static final String A1 = "A1";

    private static final String B1 = "B1";

    private static final String C1 = "C1";

    private static final String CONTAINER_ID = "403";

    // 24 * 16 <-> 10 columns, 5 rows <-> position 0 to 384 <-> grid position A1 to P24
    private static final String PLATE_LAYOUT_ID_24_16 = "2";

    private static final String PLATE_LAYOUT_NAME_24_16 = "384-well";

    // 12 * 8 <-> 12 columns, 8 rows <-> position 0 to 96 <-> grid position A1 to H12
    private static final String PLATE_LAYOUT_ID_12_8 = "1";

    private static final String PLATE_LAYOUT_NAME_12_8 = "96-well";

    // 'Quality Control' plate
    private static final String PLATE_TYPE_ID_QUALITY_CONTROL = "2";

    private static final String QC_TYPE = "qPCR";

    private static final String QC_TYPE_INVALID = "qPCR_invalid";

    // 'Storage' plate
    private static final String PLATE_TYPE_ID_STORAGE = "1";

    private static final String PLATE_TYPE_NAME_QUALITY_CONTROL = "Quality Control";

    private static final String PLATE_TYPE_NAME_STORAGE = "Storage";

    // 'admin'
    private static final String SUPERVISOR_ID_ADMIN = "703";

    // 'bemployee'
    private static final String SUPERVISOR_ID_BEMPLOYEE = "595";

    /**
     * Method used to teardown test harness used for all test in this test suite.
     * - Teardown all the resources allocated in the beforeAll method.
     * - Check if all entities created during the tests are deleted.
     */
    @AfterAll
    public static void afterAll() {
    }

    /**
     * Method used to set up the test harness used for all test in this test suite.
     * - Check if all the initial values are valid, e.g., constant 'CONTAINER_ID_EXTENSIBLE' is the id of a container which is actually extensible.
     * - Setup resources which are used by multiple tests.
     */
    @BeforeAll
    public static void beforeAll() {
    }

    /**
     * Test the given plate with respect to the createPlate(null) method.
     *
     * @param plate the plate
     */
    public static void testPlate(XMLPlate plate) {
        testPlate(plate, null);
    }

    /**
     * Test the given plate with respect to the createPlate(plateTypeId) method.
     *
     * @param plate the plate
     * @param plateTypeName the plateTypeName
     */
    public static void testPlate(XMLPlate plate, String plateTypeName) {
        Assertions.assertNull(plate.getErrorreport());
        Assertions.assertEquals(S5, plate.getName());
        Assertions.assertEquals(Long.valueOf(SUPERVISOR_ID_ADMIN), plate.getSupervisor().getId());
        Assertions.assertEquals(PLATE_LAYOUT_NAME_12_8, plate.getLayout());
        Assertions.assertEquals(plateTypeName != null ? plateTypeName : PLATE_TYPE_NAME_STORAGE, plate.getType());
    }

    /**
     * Create a plate with the given plate type. If plate type is null, the plate type defaults to 'PLATE_TYPE_ID_STORAGE'.
     *
     * @param plateTypeId the plateTypeId
     * @return the plate
     */
    public XMLPlate createPlate(String plateTypeId) {
        XMLRequestParameterSavePlate xmlRequestSavePlate = new XMLRequestParameterSavePlate();
        xmlRequestSavePlate.setName(S5);
        xmlRequestSavePlate.setSupervisorid(SUPERVISOR_ID_ADMIN);
        xmlRequestSavePlate.setPlatelayoutid(PLATE_LAYOUT_ID_12_8);
        xmlRequestSavePlate.setPlatetypeid(plateTypeId != null ? plateTypeId : PLATE_TYPE_ID_STORAGE);
        xmlRequestSavePlate.setCustomattribute(createCustomAttributes(2));
        return getSoapClient().getEpPlate().getWmSave().save(xmlRequestSavePlate);
    }

    /**
     * Create given number of samples.
     *
     * @param number The number
     * @return list of XMLSample
     */
    public List<XMLSample> createSamples(int number) {
        List<XMLSample> createdSamples = new ArrayList<>();
        int counter = 0;

        for (int i = 0; i < number; i++) {
            XMLRequestParameterSaveSample xmlRequestSaveSample = new XMLRequestParameterSaveSample();
            xmlRequestSaveSample.setName(StringHelper.generateString(5 + counter));
            xmlRequestSaveSample.setContainerid(CONTAINER_ID);
            xmlRequestSaveSample.setType(SampleTypeEnum.GENERIC.getLabel());
            createdSamples.add(getSoapClient().getEpSample().getWmSave().save(xmlRequestSaveSample));
            counter++;
        }

        return createdSamples;
    }

    /**
     * Create an add samples request for the given 3 samples to the given plate on positions A1, B1, C1.
     *
     * @param plate the plate
     * @return the xmlRequestAddPlateSamples
     */
    private XMLRequestParameterAddPlateSamples createXmlRequestAddPlateSamples(XMLPlate plate, List<XMLSample> samples) {
        XMLRequestParameterAddPlateSamples xmlRequestAddPlateSamples = new XMLRequestParameterAddPlateSamples();
        xmlRequestAddPlateSamples.setPlateid(plate.getId().toString());
        xmlRequestAddPlateSamples.getSampleplateposition().add(new XMLRequestParameterAddPlateSamplesPosition(A1, samples.get(0).getId().toString()));
        xmlRequestAddPlateSamples.getSampleplateposition().add(new XMLRequestParameterAddPlateSamplesPosition(B1, samples.get(1).getId().toString()));
        xmlRequestAddPlateSamples.getSampleplateposition().add(new XMLRequestParameterAddPlateSamplesPosition(C1, samples.get(2).getId().toString()));
        return xmlRequestAddPlateSamples;
    }

    public void deletePlate(Long id) {
        getSoapClient().getEpPlate().getWmDelete().delete(id);
    }

    public void deleteSample(Long id) {
        getSoapClient().getEpSample().getWmDelete().delete(id);
    }

    @Test
    public void plateLayoutAndTypeShouldNotBeModifiedDueToNonEmptyPlate() {
        XMLPlate plate = createPlate(null);
        List<XMLSample> createdSamples = createSamples(3);
        XMLPlate nonEmptyPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(createXmlRequestAddPlateSamples(plate, createdSamples));

        testPlate(nonEmptyPlate);
        testCustomAttributes(nonEmptyPlate.getCustomattribute());

        // Layout change
        XMLRequestParameterSavePlate xmlRequestSavePlateLayoutChange = new XMLRequestParameterSavePlate();
        xmlRequestSavePlateLayoutChange.setId(nonEmptyPlate.getId());
        xmlRequestSavePlateLayoutChange.setPlatelayoutid(PLATE_LAYOUT_ID_24_16);
        XMLPlate updatedPlateLayoutChange = getSoapClient().getEpPlate().getWmSave().save(xmlRequestSavePlateLayoutChange);
        Assertions.assertNotNull(updatedPlateLayoutChange.getErrorreport());
        Assertions.assertEquals(updatedPlateLayoutChange.getErrorreport(), "NON-EMPTY PLATE: Cannot change the layout of the plate with id " + nonEmptyPlate
            .getId() + " as there are already samples assigned to it");

        // Type change
        XMLRequestParameterSavePlate xmlRequestSavePlateTypeChange = new XMLRequestParameterSavePlate();
        xmlRequestSavePlateTypeChange.setId(nonEmptyPlate.getId());
        xmlRequestSavePlateTypeChange.setPlatetypeid(PLATE_TYPE_ID_QUALITY_CONTROL);
        XMLPlate updatedPlateTypeChange = getSoapClient().getEpPlate().getWmSave().save(xmlRequestSavePlateTypeChange);
        Assertions.assertNotNull(updatedPlateTypeChange.getErrorreport());
        Assertions.assertEquals(updatedPlateTypeChange.getErrorreport(), "NON-EMPTY PLATE: Cannot change the type of the plate with id " + nonEmptyPlate
            .getId() + " as there are already samples assigned to it");

        // A plate is only deletable if it is empty, hence the added samples need to be removed first.
        XMLRequestParameterRemovePlateSamples xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(plate.getId().toString());
        xmlRequestRemovePlateSamples.setSampleIdList(Arrays.asList(createdSamples.get(0).getId().toString(), createdSamples.get(1).getId().toString(), createdSamples.get(2).getId().toString()));
        getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);
        deletePlate(plate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void plateShouldBeCreated() {
        XMLPlate plate = createPlate(null);

        testPlate(plate);
        testCustomAttributes(plate.getCustomattribute());

        deletePlate(plate.getId());
    }

    @Test
    public void plateShouldBeDeleted() {
        XMLPlate plate = createPlate(null);
        XMLPlate deletedPLate = getSoapClient().getEpPlate().getWmDelete().delete(plate.getId());
        Assertions.assertNull(deletedPLate.getErrorreport());
        Assertions.assertNull(deletedPLate.getId());
    }

    @Test
    public void plateShouldBeRead() {
        XMLPlate plate = createPlate(null);

        XMLPlate readPlate = getSoapClient().getEpPlate().getWmRead().getEntity(plate.getId());
        XMLPlate readPlateDuplicate = getSoapClient().getEpPlate().getWmRead().getEntity(plate.getId());

        Assertions.assertNotNull(readPlate);
        Assertions.assertNotNull(readPlateDuplicate);
        Assertions.assertSame(readPlateDuplicate, readPlate);

        testPlate(readPlate);
        testCustomAttributes(readPlate.getCustomattribute());

        testPlate(readPlateDuplicate);
        testCustomAttributes(readPlateDuplicate.getCustomattribute());

        deletePlate(plate.getId());
    }

    @Test
    public void plateShouldBeUpdated() {
        XMLPlate plate = createPlate(null);

        XMLRequestParameterSavePlate xmlRequestSavePlate = new XMLRequestParameterSavePlate();

        XMLCustomAttribute customAttribute1 = new XMLCustomAttribute();
        customAttribute1.setName("custom attribute updated1");
        customAttribute1.setValue("value updated1");
        XMLCustomAttribute customAttribute2 = new XMLCustomAttribute();
        customAttribute2.setName("custom attribute updated2");
        customAttribute2.setValue("value updated2");
        xmlRequestSavePlate.setId(plate.getId());
        xmlRequestSavePlate.setName(StringHelper.generateString(6));
        xmlRequestSavePlate.setSupervisorid(SUPERVISOR_ID_BEMPLOYEE);
        xmlRequestSavePlate.setPlatelayoutid(PLATE_LAYOUT_ID_24_16);
        xmlRequestSavePlate.setPlatetypeid(PLATE_TYPE_ID_QUALITY_CONTROL);
        xmlRequestSavePlate.setCustomattribute(Arrays.asList(customAttribute1, customAttribute2));

        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmSave().save(xmlRequestSavePlate);

        Assertions.assertNull(updatedPlate.getErrorreport());
        Assertions.assertEquals(StringHelper.generateString(6), updatedPlate.getName());
        Assertions.assertEquals(Long.valueOf(SUPERVISOR_ID_BEMPLOYEE), updatedPlate.getSupervisor().getId());
        Assertions.assertEquals(PLATE_LAYOUT_NAME_24_16, updatedPlate.getLayout());
        Assertions.assertEquals(PLATE_TYPE_NAME_QUALITY_CONTROL, updatedPlate.getType());

        // Test customattributes
        Assertions.assertEquals("custom attribute updated1", updatedPlate.getCustomattribute().get(0).getName());
        Assertions.assertEquals("value updated1", updatedPlate.getCustomattribute().get(0).getValue());
        Assertions.assertEquals("String", updatedPlate.getCustomattribute().get(0).getType());
        Assertions.assertEquals("custom attribute updated2", updatedPlate.getCustomattribute().get(1).getName());
        Assertions.assertEquals("value updated2", updatedPlate.getCustomattribute().get(1).getValue());
        Assertions.assertEquals("String", updatedPlate.getCustomattribute().get(1).getType());

        deletePlate(updatedPlate.getId());
    }

    @Test
    public void samplesShouldBeAdded() {
        XMLPlate plate = createPlate(null);
        List<XMLSample> createdSamples = createSamples(3);
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(createXmlRequestAddPlateSamples(plate, createdSamples));

        testPlate(updatedPlate);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertFalse(updatedPlate.getSample().isEmpty());
        Assertions.assertEquals(3, updatedPlate.getSample().size());

        List<XMLSamplePlatePosition> updatedPlateSamplePlatePositions = new ArrayList<>(updatedPlate.getSample());
        Map<String, Long> gridPositionSampleIdMap = new HashMap<>();
        for (XMLSamplePlatePosition samplePlatePosition : updatedPlateSamplePlatePositions) {
            gridPositionSampleIdMap.put(samplePlatePosition.getGridposition(), samplePlatePosition.getId());
        }
        Assertions.assertEquals(Stream.of(A1, B1, C1).collect(Collectors.toSet()), gridPositionSampleIdMap.keySet());

        List<String> gridPositionsSorted = new ArrayList<>(gridPositionSampleIdMap.keySet());
        gridPositionsSorted.sort(String::compareTo);
        List<Long> sampleIdsSorted = new ArrayList<>(gridPositionSampleIdMap.values());
        sampleIdsSorted.sort(Long::compareTo);
        Assertions.assertEquals(A1, gridPositionsSorted.get(0));
        Assertions.assertEquals(B1, gridPositionsSorted.get(1));
        Assertions.assertEquals(C1, gridPositionsSorted.get(2));
        Assertions.assertEquals(sampleIdsSorted.get(0), createdSamples.get(0).getId());
        Assertions.assertEquals(sampleIdsSorted.get(2), createdSamples.get(2).getId());
        Assertions.assertEquals(sampleIdsSorted.get(1), createdSamples.get(1).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(A1), createdSamples.get(0).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(B1), createdSamples.get(1).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(C1), createdSamples.get(2).getId());

        // A plate is only deletable if it is empty, hence the added samples need to be removed first.
        XMLRequestParameterRemovePlateSamples xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(updatedPlate.getId().toString());
        xmlRequestRemovePlateSamples.setSampleIdList(Arrays.asList(createdSamples.get(0).getId().toString(), createdSamples.get(1).getId().toString(), createdSamples.get(2).getId().toString()));
        getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);
        deletePlate(plate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void samplesShouldBeAddedWithTheAutomaticCreationOfQcChildSamples() {
        XMLPlate plate = createPlate(PLATE_TYPE_ID_QUALITY_CONTROL);
        List<XMLSample> createdSamples = createSamples(3);
        XMLRequestParameterAddPlateSamples xmlRequestAddPlateSamples = createXmlRequestAddPlateSamples(plate, createdSamples);
        xmlRequestAddPlateSamples.setQualityControlType(QC_TYPE);

        // Original sample plate positions map, e.g., k=A1, v=1 (sample on position A1 with id 1 and name 'ccccc').
        Map<String, Long> originalSamplePlatePositionsMap = new HashMap<>();
        for (XMLRequestParameterAddPlateSamplesPosition xmlRequestAddPlateSamplesPosition : xmlRequestAddPlateSamples.getSampleplateposition()) {
            originalSamplePlatePositionsMap.put(xmlRequestAddPlateSamplesPosition.getGridposition(), Long.valueOf(xmlRequestAddPlateSamplesPosition.getSampleid()));
        }
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(xmlRequestAddPlateSamples);

        testPlate(updatedPlate, PLATE_TYPE_NAME_QUALITY_CONTROL);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertFalse(updatedPlate.getSample().isEmpty());
        Assertions.assertEquals(3, updatedPlate.getSample().size());

        // Created sample plate positions map, e.g., k=A1, v=4 (created sample on position A1 with id 4 and name 'ccccc_Quality_Control_Sample_1' having the sample with id 1 as its parent).
        Map<String, Long> createdSamplePlatePositionsMap = new HashMap<>();
        List<XMLSamplePlatePosition> createdQcChildSamples = updatedPlate.getSample();
        createdQcChildSamples.sort(Comparator.comparing(XMLSamplePlatePosition::getGridposition));
        for (XMLSamplePlatePosition xmlSamplePlatePosition : createdQcChildSamples) {
            createdSamplePlatePositionsMap.put(xmlSamplePlatePosition.getGridposition(), xmlSamplePlatePosition.getId());
        }

        getSoapClient().getEpSample().clearCache();
        for (Map.Entry<String, Long> entry : createdSamplePlatePositionsMap.entrySet()) {
            XMLSample originalSample = getSoapClient().getEpSample().getWmRead().getEntity(originalSamplePlatePositionsMap.get(entry.getKey()));
            XMLSample createdSample = getSoapClient().getEpSample().getWmRead().getEntity(entry.getValue());
            createdSamples.add(createdSample);
            Assertions.assertEquals(SampleTypeEnum.GENERIC.getLabel(), originalSample.getType());
            Assertions.assertEquals(SampleTypeEnum.QUALITY_CONTROL.getLabel(), createdSample.getType());
            Assertions.assertNotNull(createdSample.getQualitycontroltype());
            Assertions.assertEquals(QC_TYPE, createdSample.getQualitycontroltype());
        }

        // A plate is only deletable if it is empty, hence the added samples need to be removed first.
        XMLRequestParameterRemovePlateSamples xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(updatedPlate.getId().toString());
        xmlRequestRemovePlateSamples
            .setSampleIdList(Arrays.asList(createdQcChildSamples.get(0).getId().toString(), createdQcChildSamples.get(1).getId().toString(), createdQcChildSamples.get(2).getId().toString()));
        getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);
        deletePlate(updatedPlate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void samplesShouldBeRemoved() {
        XMLPlate plate = createPlate(null);
        List<XMLSample> createdSamples = createSamples(3);
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(createXmlRequestAddPlateSamples(plate, createdSamples));

        testPlate(updatedPlate);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertFalse(updatedPlate.getSample().isEmpty());
        Assertions.assertEquals(3, updatedPlate.getSample().size());

        XMLRequestParameterRemovePlateSamples xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(updatedPlate.getId().toString());
        xmlRequestRemovePlateSamples.setSampleIdList(Arrays.asList(createdSamples.get(0).getId().toString(), createdSamples.get(1).getId().toString(), createdSamples.get(2).getId().toString()));
        updatedPlate = getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);

        testPlate(updatedPlate);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertTrue(updatedPlate.getSample().isEmpty());

        deletePlate(plate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void samplesShouldBeRepositioned() {
        XMLPlate plate = createPlate(null);
        List<XMLSample> createdSamples = createSamples(3);
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(createXmlRequestAddPlateSamples(plate, createdSamples));

        testPlate(updatedPlate);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertFalse(updatedPlate.getSample().isEmpty());
        Assertions.assertEquals(3, updatedPlate.getSample().size());

        List<XMLSamplePlatePosition> updatedPlateSamplePlatePositions = new ArrayList<>(updatedPlate.getSample());
        Map<String, Long> gridPositionSampleIdMap = new HashMap<>();
        for (XMLSamplePlatePosition samplePlatePosition : updatedPlateSamplePlatePositions) {
            gridPositionSampleIdMap.put(samplePlatePosition.getGridposition(), samplePlatePosition.getId());
        }
        Assertions.assertEquals(Stream.of(A1, B1, C1).collect(Collectors.toSet()), gridPositionSampleIdMap.keySet());

        List<String> gridPositionsSorted = new ArrayList<>(gridPositionSampleIdMap.keySet());
        gridPositionsSorted.sort(String::compareTo);
        List<Long> sampleIdsSorted = new ArrayList<>(gridPositionSampleIdMap.values());
        sampleIdsSorted.sort(Long::compareTo);
        Assertions.assertEquals(A1, gridPositionsSorted.get(0));
        Assertions.assertEquals(B1, gridPositionsSorted.get(1));
        Assertions.assertEquals(C1, gridPositionsSorted.get(2));
        Assertions.assertEquals(sampleIdsSorted.get(0), createdSamples.get(0).getId());
        Assertions.assertEquals(sampleIdsSorted.get(2), createdSamples.get(2).getId());
        Assertions.assertEquals(sampleIdsSorted.get(1), createdSamples.get(1).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(A1), createdSamples.get(0).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(B1), createdSamples.get(1).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(C1), createdSamples.get(2).getId());

        XMLRequestParameterRepositionPlateSamples xmlRequestRepositionPlateSamples = new XMLRequestParameterRepositionPlateSamples();
        xmlRequestRepositionPlateSamples.setPlateid(updatedPlate.getId().toString());
        xmlRequestRepositionPlateSamples.getSampleplateposition().add(new XMLRequestParameterRepositionPlateSamplesPosition(A1, B1));
        xmlRequestRepositionPlateSamples.getSampleplateposition().add(new XMLRequestParameterRepositionPlateSamplesPosition(B1, C1));
        xmlRequestRepositionPlateSamples.getSampleplateposition().add(new XMLRequestParameterRepositionPlateSamplesPosition(C1, A1));
        updatedPlate = getSoapClient().getEpPlate().getWmRepositionSamples().repositionSamples(xmlRequestRepositionPlateSamples);

        testPlate(updatedPlate);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertFalse(updatedPlate.getSample().isEmpty());
        Assertions.assertEquals(3, updatedPlate.getSample().size());

        updatedPlateSamplePlatePositions = new ArrayList<>(updatedPlate.getSample());
        gridPositionSampleIdMap = new HashMap<>();
        for (XMLSamplePlatePosition samplePlatePosition : updatedPlateSamplePlatePositions) {
            gridPositionSampleIdMap.put(samplePlatePosition.getGridposition(), samplePlatePosition.getId());
        }
        Assertions.assertEquals(Stream.of(A1, B1, C1).collect(Collectors.toSet()), gridPositionSampleIdMap.keySet());

        gridPositionsSorted = new ArrayList<>(gridPositionSampleIdMap.keySet());
        gridPositionsSorted.sort(String::compareTo);
        sampleIdsSorted = new ArrayList<>(gridPositionSampleIdMap.values());
        sampleIdsSorted.sort(Long::compareTo);
        Assertions.assertEquals(A1, gridPositionsSorted.get(0));
        Assertions.assertEquals(B1, gridPositionsSorted.get(1));
        Assertions.assertEquals(C1, gridPositionsSorted.get(2));
        Assertions.assertEquals(sampleIdsSorted.get(0), createdSamples.get(0).getId());
        Assertions.assertEquals(sampleIdsSorted.get(2), createdSamples.get(2).getId());
        Assertions.assertEquals(sampleIdsSorted.get(1), createdSamples.get(1).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(A1), createdSamples.get(0).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(C1), createdSamples.get(1).getId());
        Assertions.assertEquals(gridPositionSampleIdMap.get(B1), createdSamples.get(2).getId());

        // A plate is only deletable if it is empty, hence the added samples need to be removed first.
        XMLRequestParameterRemovePlateSamples xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(updatedPlate.getId().toString());
        xmlRequestRemovePlateSamples.setSampleIdList(Arrays.asList(createdSamples.get(0).getId().toString(), createdSamples.get(1).getId().toString(), createdSamples.get(2).getId().toString()));
        getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);
        deletePlate(plate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void samplesShouldNotAddedToQcPlateDueToEmptyQcType() {
        XMLPlate plate = createPlate(PLATE_TYPE_ID_QUALITY_CONTROL);
        List<XMLSample> createdSamples = createSamples(3);
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(createXmlRequestAddPlateSamples(plate, createdSamples));

        Assertions.assertNotNull(updatedPlate.getErrorreport());
        Assertions.assertEquals("NO QC TYPE PROVIDED: for assigning a non-QC sample to a QC plate.", updatedPlate.getErrorreport());

        // A plate is only deletable if it is empty, hence the added samples need to be removed first.
        XMLRequestParameterRemovePlateSamples xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(plate.getId().toString());
        xmlRequestRemovePlateSamples.setSampleIdList(Arrays.asList(createdSamples.get(0).getId().toString(), createdSamples.get(1).getId().toString(), createdSamples.get(2).getId().toString()));
        getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);
        deletePlate(plate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void samplesShouldNotAddedToQcPlateDueToInvalidQcType() {
        XMLPlate plate = createPlate(PLATE_TYPE_ID_QUALITY_CONTROL);
        List<XMLSample> createdSamples = createSamples(3);
        XMLRequestParameterAddPlateSamples xmlRequestAddPlateSamples = createXmlRequestAddPlateSamples(plate, createdSamples);
        xmlRequestAddPlateSamples.setQualityControlType(QC_TYPE_INVALID);
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(xmlRequestAddPlateSamples);

        Assertions.assertNotNull(updatedPlate.getErrorreport());
        Assertions.assertEquals("Invalid type: qPCR_invalid. Valid values: " + CollectionHelper
            .print(Arrays.asList(Arrays.stream(SampleQCTypeEnum.values()).map(SampleQCTypeEnum::getLabel).toArray(String[]::new))) + "!", updatedPlate.getErrorreport());

        deletePlate(plate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void samplesShouldNotBeRemovedDueToNotContainedSamples() {
        XMLPlate plate = createPlate(null);
        List<XMLSample> createdSamples = createSamples(4);
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmAddSamples().addSamples(createXmlRequestAddPlateSamples(plate, createdSamples.subList(0, 3)));

        testPlate(updatedPlate);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertFalse(updatedPlate.getSample().isEmpty());
        Assertions.assertEquals(3, updatedPlate.getSample().size());

        XMLRequestParameterRemovePlateSamples xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(updatedPlate.getId().toString());
        xmlRequestRemovePlateSamples.setSampleIdList(Arrays.asList(createdSamples.get(0).getId().toString(), createdSamples.get(3).getId().toString()));
        updatedPlate = getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);

        Assertions.assertNotNull(updatedPlate.getErrorreport());
        Assertions.assertEquals(updatedPlate.getErrorreport(), "The plate with id " + plate.getId() + " does not contain samples with the following id(s): " + createdSamples.get(3).getId());

        // A plate is only deletable if it is empty, hence the added samples need to be removed first.
        xmlRequestRemovePlateSamples = new XMLRequestParameterRemovePlateSamples();
        xmlRequestRemovePlateSamples.setPlateid(plate.getId().toString());
        xmlRequestRemovePlateSamples.setSampleIdList(Arrays.asList(createdSamples.get(0).getId().toString(), createdSamples.get(1).getId().toString(), createdSamples.get(2).getId().toString()));
        getSoapClient().getEpPlate().getWmRemoveSamples().removeSamples(xmlRequestRemovePlateSamples);
        deletePlate(plate.getId());
        for (XMLSample sample : createdSamples) {
            deleteSample(sample.getId());
        }
    }

    @Test
    public void samplesShouldNotBeRepositionedDueToNonExistingGridPosition() {
        XMLPlate plate = createPlate(null);

        testPlate(plate);
        testCustomAttributes(plate.getCustomattribute());

        String nonExistingGridPosition = "A13";
        XMLRequestParameterRepositionPlateSamples xmlRequestRepositionPlateSamples = new XMLRequestParameterRepositionPlateSamples();
        xmlRequestRepositionPlateSamples.setPlateid(plate.getId().toString());
        xmlRequestRepositionPlateSamples.getSampleplateposition().add(new XMLRequestParameterRepositionPlateSamplesPosition(A1, nonExistingGridPosition));
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmRepositionSamples().repositionSamples(xmlRequestRepositionPlateSamples);

        Assertions.assertNotNull(updatedPlate.getErrorreport());
        Assertions.assertEquals(updatedPlate.getErrorreport(), "NON-EXISTING GRID POSITION: The plate with id " + plate
            .getId() + " does not contain the following grid position: " + nonExistingGridPosition);

        deletePlate(plate.getId());
    }

    @Test
    public void samplesShouldNotBeRepositionedDueToRepositioningOnlyEmptyPositions() {
        XMLPlate plate = createPlate(null);

        testPlate(plate);
        testCustomAttributes(plate.getCustomattribute());
        Assertions.assertTrue(plate.getSample().isEmpty());

        XMLRequestParameterRepositionPlateSamples xmlRequestRepositionPlateSamples = new XMLRequestParameterRepositionPlateSamples();
        xmlRequestRepositionPlateSamples.setPlateid(plate.getId().toString());
        xmlRequestRepositionPlateSamples.getSampleplateposition().add(new XMLRequestParameterRepositionPlateSamplesPosition("A2", "B2"));
        xmlRequestRepositionPlateSamples.getSampleplateposition().add(new XMLRequestParameterRepositionPlateSamplesPosition("B2", "C2"));
        xmlRequestRepositionPlateSamples.getSampleplateposition().add(new XMLRequestParameterRepositionPlateSamplesPosition("C2", "A2"));
        XMLPlate updatedPlate = getSoapClient().getEpPlate().getWmRepositionSamples().repositionSamples(xmlRequestRepositionPlateSamples);

        testPlate(updatedPlate);
        testCustomAttributes(updatedPlate.getCustomattribute());
        Assertions.assertTrue(updatedPlate.getSample().isEmpty());

        deletePlate(plate.getId());
    }
}
