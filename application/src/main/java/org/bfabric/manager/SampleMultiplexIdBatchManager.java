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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.MultiplexId;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.MultiplexIdService;
import org.omnifaces.cdi.Param;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;

@MeasureCalls
@Named
@ViewScoped
public class SampleMultiplexIdBatchManager extends AbstractSampleBatchManager<Sample> {

    private static final long serialVersionUID = 1;

    private final List<Sample> samplesForMultiplexIdAssignment = new ArrayList<>();

    private int blockCounter = 1;

    private String blockCounterPrefix;

    private List<Sample> editList = new ArrayList<>();

    private int initialMultiplexIdIndex = -1;

    private Set<Sample> initialSamples = new HashSet<>();

    private int multiplexIdAssignmentLimit = -1;

    private boolean multiplexIdAssignmentRepeatUntilEnd = false;

    @Inject
    private MultiplexIdService multiplexIdService;

    private MultiplexId selectedInitialMultiplexIdForMultiplexIdAssignment;

    private MultiplexKit selectedMultiplexKit;

    private SampleTypeEnum selectedSampleType;

    private Set<Sample> selectedSamples = new HashSet<>();

    private boolean selectionMode = true;

    private int startPositionIndex = -1;

    private Sample startSampleForMultiplexIdAssignment;

    @Param
    private String targetContainerId;

    public SampleMultiplexIdBatchManager() {
        super(Sample.class);
    }

    public void assignMultiplexIdsToSelectedSamples() {
        if (getInitialMultiplexIdIndex() > -1 && getStartPositionIndex() > -1 && getMultiplexIdAssignmentLimit() > 0) {
            List<MultiplexId> multiplexIds = getMultiplexIdsEnabledByMultiplexIdFiltered(null);
            if (multiplexIds != null) {
                int currentBlockCounter = getBlockCounter();
                if (!isMultiplexIdAssignmentRepeatUntilEnd() || getMultiplexIdAssignmentLimit() <= multiplexIds.size() - getInitialMultiplexIdIndex()) {
                    for (int i = 0; i < getMultiplexIdAssignmentLimit(); i++) {
                        getSamplesForMultiplexIdAssignment().get(getStartPositionIndex() + i).setMultiplexKit(getSelectedMultiplexKit());
                        getSamplesForMultiplexIdAssignment().get(getStartPositionIndex() + i).setMultiplexId(multiplexIds.get(getInitialMultiplexIdIndex() + i).getSequence());
                        getSamplesForMultiplexIdAssignment().get(getStartPositionIndex() + i).setBlock(getBlockCounterPrefix(), currentBlockCounter);
                    }
                    currentBlockCounter++;
                } else {
                    int initialMultiplexIdIndexCurrent = getInitialMultiplexIdIndex();
                    for (int i = 0; i < getMultiplexIdAssignmentLimit(); i++) {
                        getSamplesForMultiplexIdAssignment().get(getStartPositionIndex() + i).setMultiplexKit(getSelectedMultiplexKit());
                        getSamplesForMultiplexIdAssignment().get(getStartPositionIndex() + i).setMultiplexId(multiplexIds.get(initialMultiplexIdIndexCurrent).getSequence());
                        getSamplesForMultiplexIdAssignment().get(getStartPositionIndex() + i).setBlock(getBlockCounterPrefix(), currentBlockCounter);
                        if (initialMultiplexIdIndexCurrent == multiplexIds.size() - 1) {
                            initialMultiplexIdIndexCurrent = 0;
                            currentBlockCounter++;
                        } else {
                            initialMultiplexIdIndexCurrent++;
                        }
                    }
                    if (currentBlockCounter == getBlockCounter()) {
                        currentBlockCounter++;
                    }
                }
                setBlockCounter(currentBlockCounter);
            }
        }
        resetAssignMultiplexIdsModalPanel();
    }

    @Override
    public String cancel() {
        return getRedirectURLFromRefererUrl();
    }

    @Override
    public void cancelAssignMultiplexIds() {
        resetAssignMultiplexIdsModalPanel();
    }

    public void cancelLabelingSamples() {
        getEditList().clear();
        getSelectedSamples().clear();
        setBlockCounter(1);
        setBlockCounterPrefix(Constants.EMPTY_STRING);
        setSelectionMode(true);
    }

    public void createLabeledSamples() {
        getEditList().clear();
        // Note: Currently only samples of types 'Labeled MS Sample - Proteomics' are covered.
        SampleTypeEnum sampleTypeEnum = SampleTypeEnum.PROTEOMICS_SERVICES.equals(getSelectedSampleType()) ? SampleTypeEnum.MS_SAMPLE_LABELED : null;
        if (sampleTypeEnum != null) {
            SampleType sampleType = sampleTypeService.getSampleTypeByName(sampleTypeEnum.getLabel());
            String namePrefix = Messages.get("sampleNamePostfixLabeled") + "_";
            Map<Long, Long> containerIdSampleNameSuffixMap = new HashMap<>();
            for (Sample sample : getSelectedSamples()) {
                Sample childSample = sample.createChildSample(sampleType, null, null, false);
                childSample.setTubeIdBySuffix(getContainerSpecificNextTubeIdSuffix(childSample.getContainer().getId()));
                incrementContainerSpecificNextTubeIdSuffix(childSample.getContainer().getId());
                childSample.setTubeIdPadded(sampleService.getTubeIdPadded(childSample.getTubeId()));
                childSample.setMultiplexKit(getSelectedMultiplexKit());
                childSample.assignName(namePrefix, containerIdSampleNameSuffixMap);
                getEditList().add(childSample);
            }
        }
        boolean sortableByTubeIdPadded = true;
        for (Sample sample : getEditList()) {
            if (sample.getTubeIdPadded() == null) {
                sortableByTubeIdPadded = false;
                break;
            }
        }
        if (sortableByTubeIdPadded) {
            getEditList().sort(Comparator.comparing(Sample::getTubeIdPadded));
        }
        getSelectedSamples().clear();
        dataTableHelper.clearTableIdMarkedEntitiesValues();
        dataTableHelper.clearTableIdMarkedEntitiesValuesMap();
        dataTableHelper.clearTableIdSelectedEntitiesValues();
        setSelectionMode(false);
    }

    public int getBlockCounter() {
        return blockCounter;
    }

    public String getBlockCounterPrefix() {
        return blockCounterPrefix;
    }

    @Override
    public List<Sample> getEditList() {
        return editList;
    }

    public int getInitialMultiplexIdIndex() {
        return initialMultiplexIdIndex;
    }

    public Set<Sample> getInitialSamples() {
        return initialSamples;
    }

    public int getMultiplexIdAssignmentLimit() {
        return multiplexIdAssignmentLimit;
    }

    public List<MultiplexId> getMultiplexIdsEnabledByMultiplexIdFiltered(String filterString) {
        return getSelectedMultiplexKit() != null ? multiplexIdService.getMultiplexIdsEnabledByMultiplexKitIdFiltered(filterString, getSelectedMultiplexKit().getId()) : null;
    }

    @Override
    public String getRedirectURLFromRefererUrl() {
        final HttpServletRequest httpServletRequest = getHttpServletRequest();
        // If the referer URL is null, the same as the requested URL (without parameters) or if the referer URL is an edit screen, redirect to the 'container' or 'home'.
        if (getRefererURL() == null || getRefererURL().contains(httpServletRequest.getRequestURL().toString()) || getRefererURL().contains("/edit")) {
            return getDefaultRedirectURL();
        }
        return createRedirectURLFromRefererURL();
    }

    public String getRemarkForMultiplexIdAssignment(Sample newStartSampleForMultiplexIdAssignment) {
        if (newStartSampleForMultiplexIdAssignment != null) {
            setStartSampleForMultiplexIdAssignment(newStartSampleForMultiplexIdAssignment);
        }
        updateAssignmentLimit();
        return getMultiplexIdAssignmentLimit() > -1 ? getMultiplexIdAssignmentLimit() + " multiplexId(s) will be assigned to " + getMultiplexIdAssignmentLimit() + " samples" : null;
    }

    public String getSampleTypeNameOfSamplesInEditList() {
        return !getEditList().isEmpty() ? getEditList().iterator().next().getSampleType().getName() : null;
    }

    public List<Sample> getSamplesForMultiplexIdAssignment() {
        return samplesForMultiplexIdAssignment;
    }

    public MultiplexId getSelectedInitialMultiplexIdForMultiplexIdAssignment() {
        return selectedInitialMultiplexIdForMultiplexIdAssignment;
    }

    public MultiplexKit getSelectedMultiplexKit() {
        return selectedMultiplexKit;
    }

    public SampleTypeEnum getSelectedSampleType() {
        return selectedSampleType;
    }

    public Set<Sample> getSelectedSamples() {
        return selectedSamples;
    }

    public int getStartPositionIndex() {
        return startPositionIndex;
    }

    public Sample getStartSampleForMultiplexIdAssignment() {
        return startSampleForMultiplexIdAssignment;
    }

    public String getTargetContainerId() {
        return targetContainerId;
    }

    @Override
    @PostConstruct
    public void init() {
        // Note: Labeling currently restricted to following sample type.
        setSelectedSampleType(SampleTypeEnum.PROTEOMICS_SERVICES);
        if (getTargetContainerId() != null) {
            Container container = entityService.find(Container.class, Long.valueOf(getTargetContainerId()));
            for (OrderItem orderItem : container.getOrderItems()) {
                if (orderItem.getSample() != null) {
                    getSelectedSamples().add(orderItem.getSample());
                }
            }
        }
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void initializeEditList() {
    }

    public boolean isMultiplexIdAssignmentRepeatUntilEnd() {
        return multiplexIdAssignmentRepeatUntilEnd;
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    @Override
    public LinkedHashMap<Integer, LinkedHashMap<String, String>> isValid(int lo, int hi) {
        LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg = new LinkedHashMap<>();

        for (int i = 0; i < getEditList().size(); i++) {
            if (!validationErrorMsg.containsKey(i)) {
                validationErrorMsg.put(i, new LinkedHashMap<>());
            }

            validationErrorMsg.get(i).putAll(isValidSampleAttributes(getEditList().get(i)));

            if (validationErrorMsg.get(i).isEmpty()) {
                // The row contains no errors, so the entry can be removed entirely.
                validationErrorMsg.remove(i);
            }
        }

        return validationErrorMsg;
    }

    public void prepareMultiplexIdsModalPanel() {
        getSamplesForMultiplexIdAssignment().clear();
        DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
        if (dataTable != null) {
            getSamplesForMultiplexIdAssignment().addAll(dataTableHelper.getDataTableValues(dataTable));
            if (!getSamplesForMultiplexIdAssignment().isEmpty()) {
                getSamplesForMultiplexIdAssignment().sort(Comparator.comparing(Sample::getSingleParentId));
                setStartSampleForMultiplexIdAssignment(getSamplesForMultiplexIdAssignment().iterator().next());
            }
        }
    }

    public void resetAssignMultiplexIdsModalPanel() {
        // setSelectedMultiplexKit(null);
        setStartSampleForMultiplexIdAssignment(null);
        resetMultiplexIdAssignmentLimit();
    }

    public void resetInitialMultiplexIdForMultiplexIdAssignmentAndMultiplexIdAssignmentLimit() {
        setSelectedInitialMultiplexIdForMultiplexIdAssignment(null);
        resetMultiplexIdAssignmentLimit();
    }

    public void resetMultiplexIdAssignmentLimit() {
        setInitialMultiplexIdIndex(-1);
        setStartPositionIndex(-1);
        setMultiplexIdAssignmentLimit(-1);
    }

    @Override
    public String save() {
        for (Sample sample : getEditList()) {
            resetSampleFields(sample);
        }

        getValidationErrorMsg().clear();
        getValidationErrorMsg().putAll(isValid());
        if (getValidationErrorMsg().isEmpty()) {
            sampleService.saveSamples(getEditList());
            getFacesMessagesManager().clearGlobalMessages();
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCreatedLabeledSamples"));
            Set<Long> containerIds = new HashSet<>();
            for (Sample sample : getEditList()) {
                containerIds.add(sample.getContainer().getId());
                if (containerIds.size() > 1) {
                    break;
                }
            }
            if (containerIds.size() == 1 && containerIds.iterator().next() != null) {
                return createRedirectURL("container/" + Constants.SHOW, containerIds.iterator().next(), "samples", null);
            }
            return getRedirectURLFromRefererUrl();
        }
        handleValidationErrorsForBatch(getValidationErrorMsg());
        return null;
    }

    public void selectedMultiplexIdKitForMultiplexIdAssignmentSelect(SelectEvent<MultiplexKit> event) {
        resetInitialMultiplexIdForMultiplexIdAssignmentAndMultiplexIdAssignmentLimit();
        setSelectedInitialMultiplexIdForMultiplexIdAssignmentFromMultiplexKit(event.getObject());
    }

    public void selectedMultiplexKitChanged(ValueChangeEvent event) {
        resetInitialMultiplexIdForMultiplexIdAssignmentAndMultiplexIdAssignmentLimit();
        if (event.getNewValue() != null) {
            setSelectedMultiplexKit((MultiplexKit) event.getNewValue());
            setSelectedInitialMultiplexIdForMultiplexIdAssignmentFromMultiplexKit(getSelectedMultiplexKit());
        }
    }

    public void setBlockCounter(int blockCounter) {
        this.blockCounter = blockCounter;
    }

    public void setBlockCounterPrefix(String blockCounterPrefix) {
        this.blockCounterPrefix = blockCounterPrefix;
    }

    public void setEditList(List<Sample> editList) {
        this.editList = editList;
    }

    public void setInitialMultiplexIdIndex(int initialMultiplexIdIndex) {
        this.initialMultiplexIdIndex = initialMultiplexIdIndex;
    }

    public void setInitialSamples(Set<Sample> initialSamples) {
        this.initialSamples = initialSamples;
    }

    public void setMultiplexIdAssignmentLimit(int multiplexIdAssignmentLimit) {
        this.multiplexIdAssignmentLimit = multiplexIdAssignmentLimit;
    }

    public void setMultiplexIdAssignmentRepeatUntilEnd(boolean multiplexIdAssignmentRepeatUntilEnd) {
        this.multiplexIdAssignmentRepeatUntilEnd = multiplexIdAssignmentRepeatUntilEnd;
    }

    public void setSelectedInitialMultiplexIdForMultiplexIdAssignment(MultiplexId selectedInitialMultiplexIdForMultiplexIdAssignment) {
        this.selectedInitialMultiplexIdForMultiplexIdAssignment = selectedInitialMultiplexIdForMultiplexIdAssignment;
    }

    public void setSelectedInitialMultiplexIdForMultiplexIdAssignmentFromMultiplexKit(MultiplexKit multiplexKit) {
        if (multiplexKit != null && !multiplexKit.getMultiplexIds().isEmpty()) {
            setSelectedMultiplexKit(multiplexKit);
            setSelectedInitialMultiplexIdForMultiplexIdAssignment(multiplexKit.getMultiplexIds().stream().sorted(Comparator.comparingLong(MultiplexId::getOrderPosition)).collect(Collectors.toList())
                .iterator().next());
        }
    }

    public void setSelectedMultiplexKit(MultiplexKit selectedMultiplexKit) {
        this.selectedMultiplexKit = selectedMultiplexKit;
    }

    public void setSelectedSampleType(SampleTypeEnum selectedSampleType) {
        this.selectedSampleType = selectedSampleType;
    }

    public void setSelectedSamples(Set<Sample> selectedSamples) {
        this.selectedSamples = selectedSamples;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
    }

    public void setStartPositionIndex(int startPositionIndex) {
        this.startPositionIndex = startPositionIndex;
    }

    public void setStartSampleForMultiplexIdAssignment(Sample startSampleForMultiplexIdAssignment) {
        this.startSampleForMultiplexIdAssignment = startSampleForMultiplexIdAssignment;
    }

    public void setTargetContainerId(String targetContainerId) {
        this.targetContainerId = targetContainerId;
    }

    private void updateAssignmentLimit() {
        resetMultiplexIdAssignmentLimit();
        List<MultiplexId> multiplexIds = getMultiplexIdsEnabledByMultiplexIdFiltered(null);
        if (multiplexIds != null) {
            setInitialMultiplexIdIndex(multiplexIds.indexOf(getSelectedInitialMultiplexIdForMultiplexIdAssignment()));
            setStartPositionIndex(getSamplesForMultiplexIdAssignment().indexOf(getStartSampleForMultiplexIdAssignment()));
            if (getInitialMultiplexIdIndex() > -1 && getStartPositionIndex() > -1) {
                if (!isMultiplexIdAssignmentRepeatUntilEnd()) {
                    setMultiplexIdAssignmentLimit(Math.min(multiplexIds.size() - getInitialMultiplexIdIndex(), getSamplesForMultiplexIdAssignment().size() - getStartPositionIndex()));
                } else {
                    setMultiplexIdAssignmentLimit(getSamplesForMultiplexIdAssignment().size() - getStartPositionIndex());
                }
            }
        }
    }
}
