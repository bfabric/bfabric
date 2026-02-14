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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePlatePosition;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;
import org.hibernate.Hibernate;

@Named
@Stateless
public class PlateService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    @Inject
    private ContainerService containerService;

    @Inject
    private SampleService sampleService;

    public PlateService() {
        super(Plate.class);
    }

    private void addOrderForTransitionToLibraryPrep(Plate plate, Sample sample, Set<Long> orderIds) {
        if (plate != null && sample != null && orderIds != null && !sample.isManaged() && SampleTypeEnum.isLabeled(sample.getType())) {
            Container container = sample.getContainer();
            if (plate.getPlateType().isLibraryPlateType() && container.isProcessing() && !Constants.CUSTOM_ORDER_STATE_LIBRARY_PREP.equals(container.getCustomStatus())) {
                orderIds.add(container.getId());
            }
        }
    }

    public void addSamples(Plate plate, Map<Sample, String> selectedSamplesPositions) {
        if (plate != null && selectedSamplesPositions != null) {
            String namePrefix = Messages.get("plate") + "_" + plate.getId() + "_";
            Map<Long, Long> containerIdSampleNameSuffixMap = new HashMap<>();
            for (Map.Entry<Sample, String> selectedSamplesPosition : selectedSamplesPositions.entrySet()) {
                Sample sample = selectedSamplesPosition.getKey();
                if (!sample.isManaged()) {
                    sample.assignName(namePrefix, containerIdSampleNameSuffixMap);
                    save(sample);
                }
                SamplePlatePosition samplePlatePosition = new SamplePlatePosition(sample, plate, plate.getPlateLayout().getPosition(selectedSamplesPosition.getValue()));
                plate.getSamplePlatePositions().add(samplePlatePosition);
                save(samplePlatePosition);
            }
        }
    }

    public void assignSamples(Plate plate, List<Sample> selectedSamples, Map<Long, SamplePlatePosition> initialSampleIdSamplePlatePositionMap) {
        long counter = 0;
        save(plate);
        Set<Long> orderIds = new HashSet<>();
        String namePrefix = Messages.get("plate") + "_" + plate.getId() + "_";
        Map<Long, Long> containerIdSampleNameSuffixMap = new HashMap<>();
        if (plate.getSamplePlatePositions().isEmpty()) {
            for (Sample sample : selectedSamples) {
                ++counter;
                if (sample != null) {
                    addOrderForTransitionToLibraryPrep(plate, sample, orderIds);
                    sample.presetSamplePreparationProtocolFromParentIfEligible();
                    sample.assignName(namePrefix, containerIdSampleNameSuffixMap);
                    save(sample, false);
                    SamplePlatePosition samplePlatePosition = new SamplePlatePosition(sample, plate, counter);
                    save(samplePlatePosition, false);
                }
            }
        } else {
            // Updated sample plate positions.
            Set<SamplePlatePosition> updatedSamplePlatePositions = new HashSet<>();
            // Samples have been reassigned.
            Map<Long, SamplePlatePosition> updatedSampleIdSamplePlatePositionMap = new HashMap<>();
            // Check which samples are still on the plate or new based on the initial sample plate positions.
            for (Sample sample : selectedSamples) {
                SamplePlatePosition samplePlatePosition;
                ++counter;
                if (sample != null) {
                    addOrderForTransitionToLibraryPrep(plate, sample, orderIds);
                    if (initialSampleIdSamplePlatePositionMap.containsKey(sample.getId())) {
                        // The sample already is assigned to the plate.
                        samplePlatePosition = initialSampleIdSamplePlatePositionMap.get(sample.getId());
                        // Only update the samples whose position changed.
                        if (!samplePlatePosition.getPosition().equals(counter)) {
                            samplePlatePosition.setPosition(counter);
                            updatedSamplePlatePositions.add(samplePlatePosition);
                        }
                        updatedSampleIdSamplePlatePositionMap.put(sample.getId(), samplePlatePosition);
                    } else {
                        // A new sample was assigned to the plate.
                        sample.presetSamplePreparationProtocolFromParentIfEligible();
                        sample.assignName(namePrefix, containerIdSampleNameSuffixMap);
                        save(sample, false);
                        SamplePlatePosition newSamplePlatePosition = new SamplePlatePosition(sample, plate, counter);
                        updatedSamplePlatePositions.add(newSamplePlatePosition);
                    }
                }
            }
            Iterator<SamplePlatePosition> iterator = plate.getSamplePlatePositions().iterator();
            while (iterator.hasNext()) {
                SamplePlatePosition samplePlatePosition = iterator.next();
                if (!updatedSampleIdSamplePlatePositionMap.containsKey(samplePlatePosition.getSample().getId())) {
                    iterator.remove();
                    remove(samplePlatePosition);
                }
            }
            updateSamplePlatePositions(updatedSamplePlatePositions);
        }
        for (SamplePlatePosition samplePlatePosition : plate.getSamplePlatePositions()) {
            save(samplePlatePosition.getSample(), false);
        }
        flush();
        for (Long orderId : orderIds) {
            // When there are orders where a custom status is needed to indicate that the sample preparation is done.
            containerService.changeCustomStatus(find(Order.class, orderId), Constants.CUSTOM_ORDER_STATE_LIBRARY_PREP);
        }
        // Index the samples.
        for (Sample sample : selectedSamples) {
            if (sample != null) {
                sample.index();
            }
        }
    }

    public Map<String, Set<String>> changeStatus(Plate plate, StatusEnum statusEnum, boolean isSendMail) {
        Set<Mail> mails = plate.changeStatus(statusEnum);
        super.save(plate);
        flush();
        triggerCustomContainerStatusChange(plate, statusEnum);
        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("plate") + " " + statusEnum.getLabel());
        if (isSendMail) {
            facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
        }
        return facesMessages;
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Plate plate = (Plate) entity;
        return plate.getContainer() == null ? super.checkUniqueName(plate) : createNamedQuery("Plate.checkUniqueName").setParameter("name", plate.getName())
            .setParameter("containerId", plate.getContainer().getId()).setParameter("id", plate.getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public List<Plate> getNonFinishedPlates(Long supervisorId) {
        return createNamedQuery("Plate.findNonFinishedSupervisedBy").setParameter("supervisorId", supervisorId).getResultList();
    }

    public List<StatusEnum> getPlateStatusEnums() {
        return StatusEnum.getStatusEnums(Plate.class);
    }

    public List<Plate> getPlatesByContainerId(Long containerId) {
        return createNamedQuery("Plate.findByContainerId").setParameter("containerId", containerId).getResultList();
    }

    public Long getPlatesByContainerIdCount(Long containerId) {
        return (Long) createNamedQuery("Plate.countByContainerId").setParameter("containerId", containerId).getSingleResult();
    }

    public List<Plate> getPlatesBySampleId(Long sampleId) {
        return createNamedQuery("Plate.findBySampleId").setParameter("sampleId", sampleId).getResultList();
    }

    public Long getPlatesBySampleIdCount(Long sampleId) {
        return (Long) createNamedQuery("Plate.countBySampleId").setParameter("sampleId", sampleId).getSingleResult();
    }

    public BfabricLazyDataModel<Plate> getPlatesBySamplePreparationProtocolId(Long samplePreparationProtocolId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.samplePlatePositions samplePlatePosition");
        entityQuery.addWhereClause("samplePlatePosition.sample.samplePreparationProtocol.id = :samplePreparationProtocolId");
        entityQuery.addParameter("samplePreparationProtocolId", samplePreparationProtocolId);
        entityQuery.setOrder("entity.id desc");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Plate> getPlatesFilteredByContainersExcluding(String filterString, Collection<Plate> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<Plate>) entityQuery.getResultList();
    }

    public List<StatusEnum> getStatusEnums() {
        return StatusEnum.getStatusEnums(Plate.class);
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Plate plate = (Plate) entity;
        LinkedHashMap<String, String> validationErrorMsg = isValidName(plate);
        if (!plate.isPlateTypeUserSubmitted() && plate.getSupervisor() == null) {
            validationErrorMsg.put(Constants.EDIT + ":supervisorautocomplete", Constants.REQUIRED);
        }
        validationErrorMsg.putAll(isValidCustomAttributes(plate));
        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> isValidPlateTypeUserSubmitted(Plate plate, List<Sample> selectedSamples, Container singlePlateContainer) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (plate != null && plate.isPlateTypeUserSubmitted() && selectedSamples != null) {
            Set<Container> containers = new HashSet<>();
            for (Sample sample : selectedSamples) {
                if (sample != null) {
                    containers.add(sample.getContainer());
                    if (containers.size() > 1) {
                        // A user submitted plate has only samples of a single order.
                        break;
                    }
                }
            }
            if (containers.isEmpty()) {
                validationErrorMsg.put("assignSamples:validationErrorMsg", Messages.get("userSubmittedPlateEmptyError"));
            } else if (containers.size() == 1) {
                if (singlePlateContainer != null) {
                    Container container = containers.iterator().next();
                    if (container.isContainerProject()) {
                        validationErrorMsg.put("assignSamples:validationErrorMsg", Messages.get("userSubmittedPlateAssociateProjectError"));
                    } else if (!((Order) Hibernate.unproxy(container)).isProcessesPlates()) {
                        validationErrorMsg.put("assignSamples:validationErrorMsg", Messages.get("userSubmittedPlateAssociateOrderNotProcessingPlatesError"));
                    } else if (!singlePlateContainer.equals(container)) {
                        validationErrorMsg.put("assignSamples:validationErrorMsg", Messages.get("userSubmittedPlateAssociateOrderReAssociatedError"));
                    }
                }
            } else {
                validationErrorMsg.put("assignSamples:validationErrorMsg", Messages.get("userSubmittedPlateAssociateOrderMultipleError"));
            }
        }
        return validationErrorMsg;
    }

    public void remove(Plate plate, OrderItem orderItem) {
        if (plate != null && plate.isDeletable(orderItem)) {
            // Delete the plate with its sample plate positions as well as its associated sample if deletable.
            Set<Sample> samplesToDelete = new HashSet<>();
            for (SamplePlatePosition samplePlatePosition : plate.getSamplePlatePositions()) {
                Sample sample = samplePlatePosition.getSample();
                sample.getSamplePlatePositions().remove(samplePlatePosition);
                samplesToDelete.add(sample);
                samplePlatePosition.setPlate(null);
                remove(samplePlatePosition);
            }
            plate.getSamplePlatePositions().clear();
            for (Sample sample : samplesToDelete) {
                if (sample.isDeletable()) {
                    sampleService.remove(sample);
                }
            }
            if (orderItem != null) {
                plate.getOrderItems().remove(orderItem);
            }
            super.remove(plate);
        }
    }

    public void remove(Plate plate) {
        remove(plate, null);
    }

    public void removeOrderItemAndUserSubmittedPlate(OrderItem orderItem) {
        if (orderItem != null) {
            remove(orderItem.getPlate(), orderItem);
            remove(orderItem);
        }
    }

    public void removeSamples(Plate plate, Set<Sample> selectedSamples) {
        Iterator<SamplePlatePosition> iterator = plate.getSamplePlatePositions().iterator();
        while (iterator.hasNext()) {
            SamplePlatePosition samplePlatePosition = iterator.next();
            if (selectedSamples.contains(samplePlatePosition.getSample())) {
                iterator.remove();
                remove(samplePlatePosition);
            }
        }
    }

    public void rollbackStatus(Plate plate) {
        if (plate != null) {
            plate.rollbackStatus();
            save(plate);
        }
    }

    public void save(Plate plate) {
        save(plate, true);
    }

    public void save(Plate plate, boolean index) {
        if (plate != null) {
            if (plate.getStates().isEmpty()) {
                plate.addState();
            }
            if (plate.isManaged() && plate.isStatusChanged()) {
                triggerCustomContainerStatusChange(plate, plate.getStatus());
            }
            super.save(plate, index);
        }
    }

    public void triggerCustomContainerStatusChange(Plate plate, StatusEnum statusEnum) {
        Set<Long> orderIds = new HashSet<>();
        Set<Long> orderIdsExcludedFromTransitionToLibraryPrepDone = new HashSet<>();
        if (StatusEnum.FINISHED.equals(statusEnum) && plate.getPlateType().isLibraryPlateType()) {
            for (Sample sample : plate.getSamples()) {
                Container container = sample.getContainer();
                if (!Constants.CUSTOM_ORDER_STATE_LIBRARY_PREP_DONE.equals(container.getCustomStatus()) && SampleTypeEnum.isLabeled(sample.getType()) && container.isProcessing()) {
                    orderIds.add(container.getId());
                    if (sample.getQcPassed() == null || !sample.getQcPassed()) {
                        orderIdsExcludedFromTransitionToLibraryPrepDone.add(container.getId());
                    }
                }
            }
        }
        orderIds.removeAll(orderIdsExcludedFromTransitionToLibraryPrepDone);
        for (Long orderId : orderIds) {
            containerService.changeCustomStatus(find(Order.class, orderId), Constants.CUSTOM_ORDER_STATE_LIBRARY_PREP_DONE);
        }
    }

    public void updateSamplePlatePositions(Set<SamplePlatePosition> updatedSamplePlatePositions) {
        for (SamplePlatePosition samplePlatePosition : updatedSamplePlatePositions) {
            save(samplePlatePosition);
        }
    }

    public void updateUserSubmittedPlate(Plate plate, OrderItem orderItem) {
        if (plate != null) {
            Set<SamplePlatePosition> currentSamplePlatePositionsWithNonEmptySampleNames = plate.getCurrentSamplePlatePositionsWithNonEmptySampleNames();
            Map<Long, SamplePlatePosition> initialSampleIdSamplePlatePositionMap = plate.getInitialSampleIdSamplePlatePositionMap();
            if (plate.isManaged()) {
                if (plate.isChanged()) {
                    save(plate);
                    Set<SamplePlatePosition> currentManagedSamplePlatePositionsOrWithNonEmptySampleNames = new HashSet<>(currentSamplePlatePositionsWithNonEmptySampleNames);
                    currentManagedSamplePlatePositionsOrWithNonEmptySampleNames.addAll(initialSampleIdSamplePlatePositionMap.values());
                    int maxItemNumber = plate.getMaxItemNumber(initialSampleIdSamplePlatePositionMap.values());
                    int samplesWithNonEmptySampleNamesCounter = 1;
                    for (SamplePlatePosition samplePlatePosition : plate.getPlateLayout()
                        .getSamplePlatePositionsOrderedByAssignmentOrder(currentManagedSamplePlatePositionsOrWithNonEmptySampleNames)) {
                        // Note: Ordering according to the assignment order is necessary so the "first" sample on B1 gets a lower tube id than one on C4.
                        if (!samplePlatePosition.isManaged()) {
                            // A new sample is assigned to the plate.
                            if (!samplePlatePosition.getSample().isManaged()) {
                                if (orderItem != null && StringHelper.isNotEmpty(orderItem.getTubeId())) {
                                    samplePlatePosition.getSample().setTubeId(orderItem.getTubeId() + "#" + (maxItemNumber + samplesWithNonEmptySampleNamesCounter));
                                    samplesWithNonEmptySampleNamesCounter++;
                                }
                                sampleService.save(samplePlatePosition.getSample());
                            }
                            save(samplePlatePosition);
                        } else {
                            Sample sample = samplePlatePosition.getSample();
                            if (initialSampleIdSamplePlatePositionMap.containsValue(samplePlatePosition)) {
                                if (StringHelper.isEmpty(sample.getName())) {
                                    // Delete an existing sample on the plate if its name is set to null or the empty string.
                                    remove(samplePlatePosition);
                                    flush();
                                    if (sample.isDeletable()) {
                                        sampleService.remove(sample);
                                    }
                                } else if (sample.isChanged()) {
                                    sampleService.save(sample);
                                }
                            } else {
                                remove(samplePlatePosition);
                                flush();
                                if (sample.isDeletable()) {
                                    sampleService.remove(sample);
                                }
                            }
                        }
                    }
                }
            } else {
                // Save the plate, the samples, and the sample plate positions.
                save(plate);
                int samplesWithNonEmptySampleNamesCounter = 1;
                for (SamplePlatePosition samplePlatePosition : plate.getPlateLayout().getSamplePlatePositionsOrderedByAssignmentOrder(currentSamplePlatePositionsWithNonEmptySampleNames)) {
                    if (orderItem != null && StringHelper.isNotEmpty(orderItem.getTubeId())) {
                        samplePlatePosition.getSample().setTubeId(orderItem.getTubeId() + "#" + samplesWithNonEmptySampleNamesCounter);
                    }
                    sampleService.save(samplePlatePosition.getSample());
                    samplesWithNonEmptySampleNamesCounter++;
                }
                flush();
                updateSamplePlatePositions(currentSamplePlatePositionsWithNonEmptySampleNames);
            }
        }
    }
}