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

package org.bfabric.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Sample;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ContainerService;
import org.bfabric.service.SampleService;
import org.bfabric.util.DataTableHelper;
import org.bfabric.util.MultiplexIdConflictRecord;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class MultiplexedSampleManager extends AbstractManager {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(MultiplexedSampleManager.class.getName());

    private final Map<Long, Long> selectedSamplesContainerIdsCounterMap = new HashMap<>();

    private final Set<Sample> selectedSamplesOld = new HashSet<>();

    @Param
    private String clone;

    private Long clonedId = null;

    @Inject
    private ContainerService containerService;

    @Inject
    private DataTableHelper dataTableHelper;

    private Sample editedSample;

    private Set<Sample> initialSamples = new HashSet<>();

    private List<MultiplexIdConflictRecord> multiplexIdConflictRecords = new ArrayList<>();

    private Map<String, Object> multiplexIdsCorrectnessErrorMap = new HashMap<>();

    @Param
    private String sampleId;

    @Inject
    private SampleService sampleService;

    private Set<Sample> samplesWithIncompleteMultiplexIds = new HashSet<>();

    private SampleTypeEnum selectedSampleType;

    private Set<Sample> selectedSamples = new HashSet<>();

    private boolean showAllColumns = false;

    private Container targetContainer;

    @Param
    private String targetContainerId;

    public void addAllEntitiesFromDataTableToSelection(Set<AbstractEntity> selection) {
        dataTableHelper.addAllEntitiesFromDataTableToSelection(selection);
        if (isCreated()) {
            for (AbstractEntity entity : selection) {
                addToSelectedSamplesContainerIdsCounterMap(((Sample) entity).getContainer().getId());
            }
        }
    }

    public void addSampleToSelection(Set<AbstractEntity> selection, Sample sample) {
        dataTableHelper.addEntityToSelection(selection, sample);
        if (isCreated()) {
            addToSelectedSamplesContainerIdsCounterMap(sample.getContainer().getId());
        }
    }

    public void addToSelectedSamplesContainerIdsCounterMap(long containerId) {
        if (getSelectedSamplesContainerIdsCounterMap().containsKey(containerId)) {
            getSelectedSamplesContainerIdsCounterMap().put(containerId, getSelectedSamplesContainerIdsCounterMap().get(containerId) + 1);
        } else {
            getSelectedSamplesContainerIdsCounterMap().put(containerId, 1L);
        }
    }

    public void assignMarkedEntitiesToSelection(String tableId, String targetTableId, Set<AbstractEntity> selection) {
        Set<AbstractEntity> assignedEntities = dataTableHelper.assignMarkedEntitiesToSelection(tableId, targetTableId, selection);
        if (isCreated() && assignedEntities != null) {
            for (AbstractEntity entity : assignedEntities) {
                addToSelectedSamplesContainerIdsCounterMap(((Sample) entity).getContainer().getId());
            }
        }
    }

    public String cancel() {
        return getEditedSample() != null ? createRedirectShowScreenURL(getEditedSample(), sidebarHelper.getTab(), null) : getUrlHomeScreen();
    }

    public String getClone() {
        return clone;
    }

    public Long getClonedId() {
        return clonedId;
    }

    public Sample getEditedSample() {
        return editedSample;
    }

    public Set<Sample> getInitialSamples() {
        return initialSamples;
    }

    public List<MultiplexIdConflictRecord> getMultiplexIdConflictRecords() {
        return multiplexIdConflictRecords;
    }

    public Map<String, Object> getMultiplexIdsCorrectnessErrorMap() {
        return multiplexIdsCorrectnessErrorMap;
    }

    public String getPageTitle() {
        return isCreated() ? Messages.get("multiplexSample") : Messages.get("editMultiplexedSample") + " " + sampleId + " (" + getEditedSample()
            .getContainer().getEntityName() + " " + getEditedSample().getContainer().getId() + ")";
    }

    public String getSampleId() {
        return sampleId;
    }

    public Set<Sample> getSamplesWithIncompleteMultiplexIds() {
        return samplesWithIncompleteMultiplexIds;
    }

    public List<Sample> getSamplesWithIncompleteMultiplexIdsAsList() {
        return samplesWithIncompleteMultiplexIds.stream().sorted(Comparator.comparingLong(Sample::getId)).collect(Collectors.toList());
    }

    public SampleTypeEnum getSelectedSampleType() {
        return selectedSampleType;
    }

    public Set<Sample> getSelectedSamples() {
        return selectedSamples;
    }

    public Map<Long, Long> getSelectedSamplesContainerIdsCounterMap() {
        return selectedSamplesContainerIdsCounterMap;
    }

    public Set<Sample> getSelectedSamplesOld() {
        return selectedSamplesOld;
    }

    public Container getTargetContainer() {
        return targetContainer;
    }

    public String getTargetContainerId() {
        return targetContainerId;
    }

    public List<Container> getTargetContainers(String filterString) {
        return containerService.getContainersByIdsFiltered(filterString, getSelectedSamplesContainerIdsCounterMap().keySet());
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getEditedSample() == null && !isCreated()) {
            try {
                long sampleIdLong = Long.parseLong(sampleId);
                if (sampleIdLong > 0) {
                    Sample aEditedSample = entityService.find(Sample.class, sampleIdLong);
                    if (aEditedSample == null) {
                        redirectToEntityNotFoundErrorPage(Messages.get("sample"), sampleId);
                    } else if (!(aEditedSample.isMultiplexedType() || aEditedSample.isMultiplexType())) {
                        getFacesMessagesManager().bufferWarning(Messages.get("sampleNotMultiplexed") + ": " + sampleId);
                        getSessionManager().redirectRelative("/error/entity-id-invalid.html");
                    }
                    setEditedSample(aEditedSample);
                    if (getClone() != null) {
                        setClonedId(getEditedSample().getId());
                        getEditedSample().setReMultiplexed(Boolean.TRUE);
                    }
                    setSelectedSampleType(getEditedSample().getMultiplexParentSampleTypeEnum());
                    for (Sample parent : getEditedSample().getParents()) {
                        getSelectedSamples().add(parent);
                        getSelectedSamplesOld().add(parent);
                        long containerId = parent.getContainer().getId();
                        if (getSelectedSamplesContainerIdsCounterMap().containsKey(containerId)) {
                            getSelectedSamplesContainerIdsCounterMap().put(containerId, getSelectedSamplesContainerIdsCounterMap().get(containerId) + 1);
                        } else {
                            getSelectedSamplesContainerIdsCounterMap().put(containerId, 1L);
                        }
                    }
                    if (!getSelectedSamples().isEmpty()) {
                        setSelectedSampleType(SampleTypeEnum.getSampleTypeEnumByLabel(getSelectedSamples().iterator().next().getType()));
                    }
                } else {
                    redirectToEntityIdInvalidErrorPage(Messages.get("sample"), sampleId);
                }
            } catch (NumberFormatException e) {
                logger.fine("Entity id " + sampleId + " is invalid!");
                redirectToEntityIdInvalidErrorPage(Messages.get("sample"), sampleId);
            }
        } else {
            setSelectedSampleType(SampleTypeEnum.ILLUMINA_LIBRARY);
        }
    }

    public boolean isCreated() {
        return getSampleId() == null;
    }

    public boolean isRenderedContainerSelection() {
        return isCreated() && !getSelectedSamples().isEmpty() && getSelectedSamplesContainerIdsCounterMap().size() > 1;
    }

    public boolean isShowAllColumns() {
        return showAllColumns;
    }

    public void removeAllEntitiesFromSelection(Set<AbstractEntity> selection) {
        if (isCreated()) {
            for (AbstractEntity entity : selection) {
                if (getSelectedSamplesContainerIdsCounterMap().isEmpty()) {
                    break;
                }
                removeFromSelectedSamplesContainerIdsCounterMap(((Sample) entity).getContainer().getId());
            }
        }
        dataTableHelper.removeAllEntitiesFromSelection(selection);
    }

    public void removeEntityFromSelection(Set<AbstractEntity> selection, Sample entity) {
        if (isCreated()) {
            removeFromSelectedSamplesContainerIdsCounterMap(entity.getContainer().getId());
        }
        dataTableHelper.removeEntityFromSelection(selection, entity);
    }

    public void removeFromSelectedSamplesContainerIdsCounterMap(long containerId) {
        if (getSelectedSamplesContainerIdsCounterMap().get(containerId) == 1) {
            getSelectedSamplesContainerIdsCounterMap().remove(containerId);
        } else {
            getSelectedSamplesContainerIdsCounterMap().put(containerId, getSelectedSamplesContainerIdsCounterMap().get(containerId) - 1);
        }
    }

    public String saveMultiplexedSample() {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (getSelectedSamples().size() > 1 && getSelectedSampleType() != null && StringHelper.isNotEmpty(getSelectedSampleType().getMultiplexIdCheckType())) {
            String multiplexIdCheckType = getSelectedSampleType().getMultiplexIdCheckType();
            setMultiplexIdsCorrectnessErrorMap(sampleService.getMultiplexIdsCorrectnessErrorMap(getSelectedSamples(), true, false, multiplexIdCheckType));
            getSamplesWithIncompleteMultiplexIds().clear();
            if (!getMultiplexIdsCorrectnessErrorMap().isEmpty()) {
                if (getMultiplexIdsCorrectnessErrorMap().containsKey(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)) {
                    getSamplesWithIncompleteMultiplexIds().addAll((Set<Sample>) getMultiplexIdsCorrectnessErrorMap().get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY));
                    validationErrorMsg.put(Constants.EDIT + ":multiplexIdsValidErrorMessage", Messages.get("incompleteMultiplexIds"));
                }
                if (Constants.MULTIPLEX_ID_CHECK_ADVANCED.equals(multiplexIdCheckType)) {
                    if (getMultiplexIdsCorrectnessErrorMap().containsKey(Constants.MULTIPLEX_ID_MISMATCH_KEY)) {
                        validationErrorMsg.put(Constants.EDIT + ":multiplexIdsValidErrorMessage", (String) getMultiplexIdsCorrectnessErrorMap().get(Constants.MULTIPLEX_ID_MISMATCH_KEY));
                    } else if (getMultiplexIdsCorrectnessErrorMap().containsKey(Constants.MULTIPLEX_ID_MIXED_KEY)) {
                        validationErrorMsg.put(Constants.EDIT + ":multiplexIdsValidErrorMessage", (String) getMultiplexIdsCorrectnessErrorMap().get(Constants.MULTIPLEX_ID_MIXED_KEY));
                    }
                }

            }
            getMultiplexIdConflictRecords().clear();
            if (validationErrorMsg.isEmpty()) {
                // At this point in time, the multiplex ids are correct.
                getMultiplexIdConflictRecords().addAll(sampleService.getMultiplexIdConflictRecords(null, getSelectedSamples(), multiplexIdCheckType));
                if (!getMultiplexIdConflictRecords().isEmpty()) {
                    validationErrorMsg.put(Constants.EDIT + ":multiplexIdsValidErrorMessage", Messages.get("nonUniqueMultiplexIds"));
                }
            }
        }

        if (validationErrorMsg.isEmpty()) {
            long containerId;
            if (isCreated() || getClone() != null) {
                containerId = getSelectedSamplesContainerIdsCounterMap().size() == 1 ? getSelectedSamplesContainerIdsCounterMap().keySet().iterator().next() : getTargetContainer().getId();
            } else {
                containerId = getEditedSample().getContainer().getId();
            }
            Sample multiplexedSample = sampleService
                .saveMultiplexedSample(getSelectedSamples(), getSelectedSampleType(), containerId, getEditedSample(), getSelectedSamplesOld(), getSelectedSamplesContainerIdsCounterMap(), getClone());
            if (multiplexedSample != null) {
                getFacesMessagesManager().clearGlobalMessages();
                facesMessageAdd(getEditedSample() == null);
                return createRedirectShowScreenURL(multiplexedSample, sidebarHelper.getTab(), null);
            }
            return getUrlHomeScreen();
        }
        getFacesMessagesManager().printValidationErrors(validationErrorMsg);
        return null;
    }

    public void selectedSampleTypeChanged() {
        dataTableHelper.clearTableIdRowsPerPageTemplate();
        dataTableHelper.clearTableIdMarkedEntitiesValues();
        dataTableHelper.clearTableIdMarkedEntitiesValuesMap();
        dataTableHelper.clearTableIdSelectedEntitiesValues();
    }

    public void setClonedId(Long clonedId) {
        this.clonedId = clonedId;
    }

    public void setEditedSample(Sample editedSample) {
        this.editedSample = editedSample;
    }

    public void setInitialSamples(Set<Sample> initialSamples) {
        this.initialSamples = initialSamples;
    }

    public void setMultiplexIdConflictRecords(List<MultiplexIdConflictRecord> multiplexIdConflictRecords) {
        this.multiplexIdConflictRecords = multiplexIdConflictRecords;
    }

    public void setMultiplexIdsCorrectnessErrorMap(Map<String, Object> multiplexIdsCorrectnessErrorMap) {
        this.multiplexIdsCorrectnessErrorMap = multiplexIdsCorrectnessErrorMap;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public void setSamplesWithIncompleteMultiplexIds(Set<Sample> samplesWithIncompleteMultiplexIds) {
        this.samplesWithIncompleteMultiplexIds = samplesWithIncompleteMultiplexIds;
    }

    public void setSelectedSampleType(SampleTypeEnum selectedSampleType) {
        this.selectedSampleType = selectedSampleType;
    }

    public void setSelectedSamples(Set<Sample> selectedSamples) {
        this.selectedSamples = selectedSamples;
    }

    public void setShowAllColumns(boolean showAllColumns) {
        this.showAllColumns = showAllColumns;
    }

    public void setTargetContainer(Container targetContainer) {
        this.targetContainer = targetContainer;
    }

    public void setTargetContainerId(String targetContainerId) {
        this.targetContainerId = targetContainerId;
    }

    public void unAssignMarkedEntitiesFromSelection(String tableId, String sourceTableId, Set<AbstractEntity> selection) {
        Set<AbstractEntity> unassignedEntities = dataTableHelper.unAssignMarkedEntitiesFromSelection(tableId, sourceTableId, selection);
        if (isCreated() && unassignedEntities != null) {
            for (AbstractEntity entity : unassignedEntities) {
                if (getSelectedSamplesContainerIdsCounterMap().isEmpty()) {
                    break;
                }
                removeFromSelectedSamplesContainerIdsCounterMap(((Sample) entity).getContainer().getId());
            }
        }
    }
}
