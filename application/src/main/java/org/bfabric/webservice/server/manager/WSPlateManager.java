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
package org.bfabric.webservice.server.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.bfabric.entity.Plate;
import org.bfabric.entity.PlateLayout;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePlatePosition;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFHelper;
import org.bfabric.forms.MFPlate;
import org.bfabric.service.PlateService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddPlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddPlateSamplesPosition;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRemovePlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRepositionPlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRepositionPlateSamplesPosition;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSavePlate;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;
import org.bfabric.xml.entity.XMLPlate;

public class WSPlateManager extends AbstractWSEntityManager<Plate, XMLPlate> {

    @Inject
    private PlateService plateService;

    public XMLResponse addSamples(List<XMLRequestParameterAddPlateSamples> xmlRequestAddPlateSamplesList) {
        XMLResponse xmlResponse = new XMLResponse();
        // Used for caching already retrieved grid positions.
        Map<Long, Set<String>> plateLayoutGridPositions = new HashMap<>();
        for (XMLRequestParameterAddPlateSamples xmlRequestAddSample : xmlRequestAddPlateSamplesList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                // Set the plate instance.
                Plate plate = (Plate) wsService.fetchAndSetOldStateAsXml(Plate.class, MFHelper.positiveLongValueOf("plateid", xmlRequestAddSample.getPlateid()));
                if (!plate.isSampleAssignmentEditable()) {
                    throw new InvalidDataException("The samples of the plate with id " + plate.getId() + " are not editable.");
                }
                setInstance(plate);
                // Retrieve all the (occupied) grid positions and compute the ones which are free thereof.
                PlateLayout plateLayout = plate.getPlateLayout();
                Set<String> occupiedGridPositions = plate.getOccupiedGridPositions();
                if (!plateLayoutGridPositions.containsKey(plateLayout.getId())) {
                    plateLayoutGridPositions.put(plateLayout.getId(), new HashSet<>(plateLayout.getGridPositions()));
                }
                Set<String> gridPositions = new HashSet<>(plateLayoutGridPositions.get(plateLayout.getId()));
                Set<String> freeGridPositions = new HashSet<>(gridPositions);
                freeGridPositions.removeAll(occupiedGridPositions);
                List<String> freeGridPositionsSorted = CollectionHelper.sortObjects(freeGridPositions);
                Set<Long> plateSamplesIds = new HashSet<>();
                for (SamplePlatePosition samplePlatePosition : plate.getSamplePlatePositions()) {
                    plateSamplesIds.add(samplePlatePosition.getSample().getId());
                }
                // Retrieve the to be inserted samples with their respective grid position from the request.
                Map<Sample, String> selectedSamplesPositions = new HashMap<>();
                for (XMLRequestParameterAddPlateSamplesPosition xmlRequestAddPlateSamplesPosition : xmlRequestAddSample.getSampleplateposition()) {
                    String gridPosition = xmlRequestAddPlateSamplesPosition.getGridposition();
                    if (!gridPositions.contains(gridPosition)) {
                        // This means that the provided grid position does not exist on the plate.
                        throw new InvalidDataException("NON-EXISTING GRID POSITION: The plate with id " + plate
                            .getId() + " does not contain the following grid position: " + gridPosition + ". Free grid positions are: " + CollectionHelper.print(freeGridPositionsSorted));
                    }
                    if (occupiedGridPositions.contains(gridPosition)) {
                        // This means that the provided grid position is already occupied on the plate.
                        throw new InvalidDataException("GRID POSITION OCCUPIED: The plate with id " + plate
                            .getId() + " has already a sample on grid position: " + gridPosition + ". Free grid positions are: " + CollectionHelper.print(freeGridPositionsSorted));
                    }
                    Sample sample = (Sample) wsService.fetch(Sample.class, MFHelper.positiveLongValueOf("sampleid", xmlRequestAddPlateSamplesPosition.getSampleid()));
                    if (plateSamplesIds.contains(sample.getId())) {
                        // This means that the provided sample is already on the plate.
                        throw new InvalidDataException("DUPLICATE SAMPLE: The sample with id " + sample.getId() + " is already assigned to the plate with id " + plate);
                    }

                    Sample addedSample = sample;
                    SampleType plateSampleType = plate.getPlateType().getSampleType();
                    if (plateSampleType != null && !plateSampleType.equals(addedSample.getSampleType())) {
                        // Only samples of a specific type are allowed on the plate.
                        addedSample = sample.createChildSampleOnPlate(plateSampleType);
                        // Setting necessary values for processing depending on the sample type.
                        if (SampleTypeEnum.QUALITY_CONTROL.isOfType(plateSampleType.getName())) {
                            if (StringHelper.isEmpty(xmlRequestAddSample.getQualityControlType())) {
                                // This means no qc type was provided.
                                throw new InvalidDataException("NO QC TYPE PROVIDED: for assigning a non-QC sample to a QC plate.");
                            }
                            addedSample.setQualityControlType(SampleQCTypeEnum.valueByLabel(xmlRequestAddSample.getQualityControlType()));
                        }
                    }
                    selectedSamplesPositions.put(addedSample, gridPosition);
                }
                // At this point it is guaranteed that all the grid position exists on the plate and are free as well as that all samples can be assigned to the plate.
                plateService.addSamples(plate, selectedSamplesPositions);
                xmlEntity = createNewXmlEntity(getInstance());
            } catch (InvalidDataException | InvalidEnumValueException e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFPlate(getInstance(), (XMLRequestParameterSavePlate) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFPlate(getInstance(), (XMLRequestParameterSavePlate) aXmlRequestSaveEntity);
    }

    @Override
    protected <T> void isValid(T entity) throws Exception {
        super.isValid(entity);
        handleValidationErrors(plateService.isValid(getInstance()));
    }

    public XMLResponse removeSamples(List<XMLRequestParameterRemovePlateSamples> xmlRequestRemovePlateSamplesList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterRemovePlateSamples xmlRequestRemoveSample : xmlRequestRemovePlateSamplesList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                // Set the plate instance.
                Plate plate = (Plate) wsService.fetchAndSetOldStateAsXml(Plate.class, MFHelper.positiveLongValueOf("plateid", xmlRequestRemoveSample.getPlateid()));
                if (!plate.isSampleAssignmentEditable()) {
                    throw new InvalidDataException("The samples of the plate with id " + plate.getId() + " are not editable.");
                }
                setInstance(plate);
                // Retrieve the ids from the samples on the plate.
                Set<Long> plateSamplesIds = new HashSet<>();
                for (SamplePlatePosition samplePlatePosition : plate.getSamplePlatePositions()) {
                    plateSamplesIds.add(samplePlatePosition.getSample().getId());
                }
                // Retrieve the to be deleted samples from the request.
                Set<Sample> selectedSamples = new HashSet<>();
                for (String sampleId : xmlRequestRemoveSample.getSampleIdList()) {
                    Sample sample = (Sample) wsService.fetch(Sample.class, MFHelper.positiveLongValueOf("sampleid", sampleId));
                    selectedSamples.add(sample);
                }
                Set<Long> sampleIds = selectedSamples.stream().map(Sample::getId).collect(Collectors.toSet());
                sampleIds.removeAll(plateSamplesIds);
                if (!sampleIds.isEmpty()) {
                    // This means that the request involves sample ids which are not on the plate.
                    throw new InvalidDataException("The plate with id " + plate.getId() + " does not contain samples with the following id(s): " + CollectionHelper.printIds(sampleIds));
                }
                plateService.removeSamples(plate, selectedSamples);
                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    public XMLResponse repositionSamples(List<XMLRequestParameterRepositionPlateSamples> xmlRequestRepositionPlateSamplesList) {
        XMLResponse xmlResponse = new XMLResponse();
        // Used for caching already retrieved grid positions.
        Map<Long, Set<String>> plateLayoutGridPositions = new HashMap<>();
        for (XMLRequestParameterRepositionPlateSamples xmlRequestRearrangeSample : xmlRequestRepositionPlateSamplesList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                // Set the plate instance.
                Plate plate = (Plate) wsService.fetchAndSetOldStateAsXml(Plate.class, MFHelper.positiveLongValueOf("plateid", xmlRequestRearrangeSample.getPlateid()));
                if (!plate.isSampleAssignmentEditable()) {
                    throw new InvalidDataException("The samples of the plate with id " + plate.getId() + " are not editable.");
                }
                setInstance(plate);
                // Retrieve all the occupied grid positions and compute the ones which are free thereof.
                PlateLayout plateLayout = plate.getPlateLayout();
                Map<String, SamplePlatePosition> occupiedGridPositionsSamplePlatePositionMap = plate.getOccupiedGridPositionsSamplePlatePositionMap();
                if (!plateLayoutGridPositions.containsKey(plateLayout.getId())) {
                    plateLayoutGridPositions.put(plateLayout.getId(), new HashSet<>(plateLayout.getGridPositions()));
                }
                Set<String> gridPositions = new HashSet<>(plateLayoutGridPositions.get(plateLayout.getId()));
                // Perform the switch plate position operations in the given order of the request.
                Set<SamplePlatePosition> updatedSamplePlatePositions = new HashSet<>();
                for (XMLRequestParameterRepositionPlateSamplesPosition xmlRequestRepositionPlateSamplesPosition : xmlRequestRearrangeSample.getSampleplateposition()) {
                    String gridPosition1 = xmlRequestRepositionPlateSamplesPosition.getGridposition1();
                    String gridPosition2 = xmlRequestRepositionPlateSamplesPosition.getGridposition2();
                    String nonExistingGridPosition = !gridPositions.contains(gridPosition1) ? gridPosition1 : !gridPositions.contains(gridPosition2) ? gridPosition2 : null;
                    if (nonExistingGridPosition != null) {
                        // This means that the provided grid position does not exist on the plate.
                        throw new InvalidDataException("NON-EXISTING GRID POSITION: The plate with id " + plate.getId() + " does not contain the following grid position: " + nonExistingGridPosition);
                    }
                    if (gridPosition1 != null && gridPosition2 != null && !gridPosition1.equals(gridPosition2)) {
                        // Switch the position of two sample plate positions.
                        SamplePlatePosition newSamplePlatePosition2 = occupiedGridPositionsSamplePlatePositionMap.remove(gridPosition1);
                        SamplePlatePosition newSamplePlatePosition1 = occupiedGridPositionsSamplePlatePositionMap.remove(gridPosition2);
                        if (newSamplePlatePosition1 != null) {
                            occupiedGridPositionsSamplePlatePositionMap.put(gridPosition1, newSamplePlatePosition1);
                            newSamplePlatePosition1.setPosition(plateLayout.getPosition(gridPosition1));
                            updatedSamplePlatePositions.add(newSamplePlatePosition1);
                        }
                        if (newSamplePlatePosition2 != null) {
                            occupiedGridPositionsSamplePlatePositionMap.put(gridPosition2, newSamplePlatePosition2);
                            newSamplePlatePosition2.setPosition(plateLayout.getPosition(gridPosition2));
                            updatedSamplePlatePositions.add(newSamplePlatePosition2);
                        }
                    }
                }
                plateService.updateSamplePlatePositions(updatedSamplePlatePositions);
                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    @Override
    public void save() {
        plateService.save(getInstance(), false);
    }
}