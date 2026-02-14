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

package org.bfabric.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Order;
import org.bfabric.entity.Run;
import org.bfabric.entity.RunSample;
import org.bfabric.entity.RunUnit;
import org.bfabric.entity.RunUnitLane;
import org.bfabric.entity.Sample;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.MultiplexIdConflictRecord;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class RunService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    @Inject
    private ContainerService containerService;

    @Inject
    private SampleService sampleService;

    public RunService() {
        super(Run.class);
    }

    public void assignDatasets(Run run, Set<Dataset> selectedDatasets) {
        if (run != null && selectedDatasets != null) {
            run.assignDatasets(selectedDatasets);
            Set<Dataset> toBeUpdatedDatasets = run.getDatasets();
            toBeUpdatedDatasets.addAll(selectedDatasets);
            save(run);
            for (Dataset dataset : toBeUpdatedDatasets) {
                save(dataset);
            }
        }
    }

    public void assignSamples(Run run, Map<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositions) {
        if (run != null) {
            Set<Long> orderIds = new HashSet<>();
            Set<Sample> samplesToDelete = new HashSet<>();
            Set<Sample> samplesToPersist = new HashSet<>();
            Set<RunSample> runSamplesToDelete = new HashSet<>();
            Set<RunSample> runSamplesToPersist = new HashSet<>();
            if (run.getRunUnit() != null) {
                run.assignSamples(samplesRunUnitLanesPositions, orderIds, samplesToDelete, samplesToPersist, runSamplesToDelete, runSamplesToPersist);
                String namePrefix = run.getSampleNamePrefix();
                Map<Long, Long> containerIdSampleNameSuffixMap = new HashMap<>();
                for (Sample sampleToCreate : samplesToPersist) {
                    sampleToCreate.assignName(namePrefix, containerIdSampleNameSuffixMap);
                    save(sampleToCreate);
                }
                flush();
            }
            save(run);
            flush();
            for (Sample sampleToDelete : samplesToDelete) {
                sampleToDelete.getParents().clear();
                sampleToDelete.getParents().clear();
                remove(sampleToDelete);
            }
            for (RunSample runSampleToPersist : runSamplesToPersist) {
                save(runSampleToPersist);
            }
            for (RunSample runSampleToDelete : runSamplesToDelete) {
                remove(runSampleToDelete);
            }
            flush();
            for (Long orderId : orderIds) {
                containerService
                    .changeCustomStatus(find(Order.class, orderId), run.isQc() ? Constants.CUSTOM_ORDER_STATE_QUEUED_FOR_SEQUENCING_QC : Constants.CUSTOM_ORDER_STATE_QUEUED_FOR_SEQUENCING);
            }
        }
    }

    public Map<String, Set<String>> changeStatus(Run run, StatusEnum statusEnum) {
        Set<Mail> mails = run.changeStatus(statusEnum);
        super.save(run);
        flush();
        triggerCustomContainerStatusChange(run, statusEnum);
        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("run") + " " + statusEnum.getLabel());
        facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
        return facesMessages;
    }

    public List<MultiplexIdConflictRecord> checkMultiplexIdUniqueness(Run run, Map<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositions, String multiplexIdCheckType) {
        // samplesRunUnitLanesPositions, e.g., {Sample 297773={1=true, 2=false, 3=false, 4=false}, Sample 297772={1=false, 2=true, 3=false, 4=false}}
        List<Integer> lanePositions = new ArrayList<>();
        for (RunUnitLane runUnitLane : run.getRunUnit().getRunUnitLanes()) {
            lanePositions.add(runUnitLane.getPosition());
        }
        Collections.sort(lanePositions);
        final List<MultiplexIdConflictRecord> multiplexIdConflictRecords = new ArrayList<>();
        if (run.isDemultiplexingRequired()) {
            if (run.isPhysicalSeparation()) {
                multiplexIdConflictRecords.addAll(sampleService.getMultiplexIdConflictRecords(createLanePositionSamplesMap(lanePositions, samplesRunUnitLanesPositions), null, multiplexIdCheckType));
            } else {
                multiplexIdConflictRecords.addAll(sampleService.getMultiplexIdConflictRecords(null, samplesRunUnitLanesPositions.keySet(), multiplexIdCheckType));
            }
        }
        return multiplexIdConflictRecords;
    }

    private Map<Integer, Set<Sample>> createLanePositionSamplesMap(List<Integer> lanePositions, Map<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositions) {
        Map<Integer, Set<Sample>> lanePositionSamplesMap = new HashMap<>();
        for (Integer lanePosition : lanePositions) {
            lanePositionSamplesMap.put(lanePosition, new HashSet<>());
        }
        for (Map.Entry<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositionsEntry : samplesRunUnitLanesPositions.entrySet()) {
            for (Integer lanePosition : lanePositions) {
                if (samplesRunUnitLanesPositionsEntry.getValue().get(lanePosition)) {
                    lanePositionSamplesMap.get(lanePosition).add(samplesRunUnitLanesPositionsEntry.getKey());
                }
            }
        }
        return lanePositionSamplesMap;
    }

    public long getContainerSpecificNextTubeIdSuffix(Map<Long, Long> containerSpecificNextTubeIdSuffixMap, long containerId) {
        Long suffix = containerSpecificNextTubeIdSuffixMap.get(containerId);
        if (suffix == null) {
            suffix = getNextTubeIdSuffix(containerId);
            containerSpecificNextTubeIdSuffixMap.put(containerId, suffix);
        } else {
            suffix = suffix + 1;
            containerSpecificNextTubeIdSuffixMap.put(containerId, suffix);
        }
        return suffix;
    }

    public BfabricLazyDataModel<Run> getLazyModelByInstrumentReadConfigurationId(long instrumentReadConfigurationId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("instrumentReadConfiguration.id = :instrumentReadConfigurationId");
        entityQuery.addParameter("instrumentReadConfigurationId", instrumentReadConfigurationId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Run> getLazyModelByInstrumentReadConfigurationIdAndRunUnitTypeId(long instrumentReadConfigurationId, long runUnitTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("instrumentReadConfiguration.id = :instrumentReadConfigurationId and runUnit.runUnitType,id = :runUnitTypeId");
        entityQuery.addParameter("instrumentReadConfigurationId", instrumentReadConfigurationId);
        entityQuery.addParameter("runUnitTypeId", runUnitTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Run> getNonFinishedRuns(Long supervisorId) {
        return createNamedQuery("Run.findNonFinishedSupervisedBy").setParameter("supervisorId", supervisorId).getResultList();
    }

    public List<StatusEnum> getRunStatusEnums() {
        return StatusEnum.getStatusEnums(Run.class);
    }

    public List<Run> getRunsByContainerId(Long containerId) {
        return createNamedQuery("Run.findByContainerId").setParameter("containerId", containerId).getResultList();
    }

    public Long getRunsByContainerIdCount(Long containerId) {
        return (Long) createNamedQuery("Run.countByContainerId").setParameter("containerId", containerId).getSingleResult();
    }

    public List<Run> getRunsBySampleId(Long sampleId) {
        return createNamedQuery("Run.findBySampleId").setParameter("sampleId", sampleId).getResultList();
    }

    public Long getRunsBySampleIdCount(Long sampleId) {
        return (Long) createNamedQuery("Run.countBySampleId").setParameter("sampleId", sampleId).getSingleResult();
    }

    public List<StatusEnum> getStatusEnums() {
        return StatusEnum.getStatusEnums(Run.class);
    }

    public boolean isMultiplexIdsCorrectnessUniquenessCheckRequired(Run run, Map<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositions) {
        // samplesRunUnitLanesPositions, e.g., {Sample 297773={1=true, 2=false, 3=false, 4=false}, Sample 297772={1=false, 2=true, 3=false, 4=false}}
        if (run.isDemultiplexingRequired()) {
            if (run.isPhysicalSeparation()) {
                List<Integer> lanePositions = new ArrayList<>();
                for (RunUnitLane runUnitLane : run.getRunUnit().getRunUnitLanes()) {
                    lanePositions.add(runUnitLane.getPosition());
                }
                Collections.sort(lanePositions);
                // Check if the possible exception in condition (1) holds
                boolean isNotMaximumSingleSamplePerLane = false;
                if (samplesRunUnitLanesPositions.keySet().size() <= lanePositions.size()) {
                    // Less than or equal amount of samples on the runs as lanes
                    Map<Integer, Integer> lanePositionSamplesCounterMap = new HashMap<>();
                    for (Integer lanePosition : lanePositions) {
                        lanePositionSamplesCounterMap.put(lanePosition, 0);
                    }
                    for (Map<Integer, Boolean> runUnitLanesPosition : samplesRunUnitLanesPositions.values()) {
                        for (Map.Entry<Integer, Boolean> runUnitLanesPositionEntry : runUnitLanesPosition.entrySet()) {
                            if (runUnitLanesPositionEntry.getValue() != null && runUnitLanesPositionEntry.getValue()) {
                                lanePositionSamplesCounterMap.put(runUnitLanesPositionEntry.getKey(), lanePositionSamplesCounterMap.get(runUnitLanesPositionEntry.getKey()) + 1);
                            }
                        }
                    }
                    for (Integer samplesCounter : lanePositionSamplesCounterMap.values()) {
                        if (samplesCounter > 1) {
                            isNotMaximumSingleSamplePerLane = true;
                            break;
                        }
                    }
                    // isNotMaximumSingleSamplePerLane = false --> at maximum one sample per lane, hence the exception in condition (1) is satisfied
                }
                return samplesRunUnitLanesPositions.keySet().size() > lanePositions.size() || isNotMaximumSingleSamplePerLane;
            }
            // If there are no samples or only one sample on the run, the exception in condition (1) is satisfied
            return samplesRunUnitLanesPositions.keySet().size() > 1;
        }
        return false;
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Run run = (Run) entity;
        LinkedHashMap<String, String> validationErrorMsg = isValidName(run);
        validationErrorMsg.putAll(isValidCustomAttributes(run));
        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> isValidSamplesAssignment(Run run, Map<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositions, Collection<Sample> selectedSamples) {
        final LinkedHashMap<String, String> validationErrorMsg = isValidName(run);
        for (Sample sample : selectedSamples) {
            if (samplesRunUnitLanesPositions.containsKey(sample) && CollectionHelper.isCollectionAllFalse(samplesRunUnitLanesPositions.get(sample).values())) {
                // A selected sample has no lane assigned to it.
                for (Integer position : samplesRunUnitLanesPositions.get(sample).keySet()) {
                    validationErrorMsg.put(StringHelper
                        .createRowMessageComponentForInput(String.valueOf(sample.getRowKeyId()), Constants.CHECK_BOX_POSITION_COLUMN + position), Constants.REQUIRED);
                }
            }
        }
        return validationErrorMsg;
    }

    public void remove(Run run) {
        if (run.getRunUnit() != null) {
            run.removeAllSamples();
            remove(run.getRunUnit());
        }
        super.remove(run);
    }

    public void rollbackStatus(Run run) {
        if (run != null) {
            run.rollbackStatus();
            save(run);
        }
    }

    public void save(Run run) {
        save(run, true);
    }

    public void save(Run run, boolean index) {
        if (run != null) {
            if (run.getStates().isEmpty()) {
                run.addState();
            }
            if (run.isManaged() && run.isStatusChanged()) {
                triggerCustomContainerStatusChange(run, run.getStatus());
            }
            RunUnit newRunUnit = run.getRunUnit();
            if (run.getOldRunUnit() != null && run.getOldRunUnit().isManaged()) {
                run.getOldRunUnit().setRun(null);
                run.setRunUnit(null);
                remove(run.getOldRunUnit());
                flush();
            }
            if (newRunUnit != null) {
                run.setRunUnit(newRunUnit);
                save(newRunUnit, index);
            }
            super.save(run, index);
            if (run.isCloned() && newRunUnit != null) {
                try {
                    for (RunSample runSample : run.getRunSamples()) {
                        save(runSample, index);
                    }
                    Map<Long, Long> containerIdSampleNameSuffixMap = new HashMap<>();
                    Map<Long, Long> containerSpecificNextTubeIdSuffixMap = new HashMap<>();
                    String namePrefix = run.getSampleNamePrefix();
                    for (Sample sample : newRunUnit.getSamples()) {
                        sample.assignName(namePrefix, containerIdSampleNameSuffixMap);
                        sample.setTubeIdBySuffix(getContainerSpecificNextTubeIdSuffix(containerSpecificNextTubeIdSuffixMap, sample.getContainer().getId()));
                        save(sample, index);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void triggerCustomContainerStatusChange(Run run, StatusEnum statusEnum) {
        if (StatusEnum.PROCESSING.equals(statusEnum) || StatusEnum.FINISHED.equals(statusEnum)) {
            Set<Long> updatedOrdersIds = new HashSet<>();
            for (Sample sample : run.getSamples()) {
                Container container = sample.getContainer();
                if (!updatedOrdersIds.contains(container.getId()) && container.isProcessing()) {
                    if (run.isQc()) {
                        if (StatusEnum.PROCESSING.equals(statusEnum) && container.isCustomStatusEmptyOrNotEqualsTo(Constants.CUSTOM_ORDER_STATE_SEQUENCING_QC)) {
                            containerService.changeCustomStatus(find(Order.class, container.getId()), Constants.CUSTOM_ORDER_STATE_SEQUENCING_QC);
                            updatedOrdersIds.add(container.getId());
                        }
                    } else {
                        if (StatusEnum.PROCESSING.equals(statusEnum)) {
                            if (container.isCustomStatusEmptyOrNotEqualsTo(Constants.CUSTOM_ORDER_STATE_SEQUENCING)) {
                                containerService.changeCustomStatus(find(Order.class, container.getId()), Constants.CUSTOM_ORDER_STATE_SEQUENCING);
                                updatedOrdersIds.add(container.getId());
                            }
                        } else {
                            // Finished
                            if (container.isCustomStatusEmptyOrNotEqualsTo(Constants.CUSTOM_ORDER_STATE_SEQUENCING_DONE)) {
                                containerService.changeCustomStatus(find(Order.class, container.getId()), Constants.CUSTOM_ORDER_STATE_SEQUENCING_DONE);
                                updatedOrdersIds.add(container.getId());
                            }
                        }
                    }
                }
            }
        }
    }
}