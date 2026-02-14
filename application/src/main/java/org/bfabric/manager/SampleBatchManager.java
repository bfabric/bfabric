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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.validation.constraints.Min;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DualListModel;

@MeasureCalls
@Named
@ViewScoped
public class SampleBatchManager extends AbstractSampleBatchManager<Container> {

    private static final long serialVersionUID = 1;

    private final List<Sample> createdList = new ArrayList<>();

    private Map<Sample, Boolean> checkedEditListItems = new HashMap<>();

    private int cloneCount = 1;

    private Sample currentSample;

    private List<Sample> editList = new ArrayList<>();

    private boolean editView = false;

    @Param
    @Min(2)
    private Integer fractions;

    private Sample fractionsParent;

    @Param
    private String fractionsParentId;

    @Param
    private String fractionsSampleTypeId;

    private List<Sample> selectionList = new ArrayList<>();

    @Param
    private Boolean skipSelect;

    public SampleBatchManager() {
        super(Container.class);
    }

    @Override
    public void addClones() throws CloneNotSupportedException {
        for (int i = 0; i < cloneCount; i++) {
            Sample cloneSample = currentSample.clone();
            cloneSample.setName(currentSample.getName() + "_" + Messages.get("sampleNamePostfixClone") + "_" + (i + 1));
            // Add the clone to the current clone list.
            getEditList().add(cloneSample);
            getCustomAttributes().addAll(cloneSample.getCustomAttributes());
            evaluateCustomAttributeColumns();
        }
        // Reset the clone count.
        setCloneCount(1);
    }

    @Override
    public void addNewBatchItems() {
        if (getNumberOfNewBatchItems() > getMaxNumberOfNewBatchItems()) {
            setNumberOfNewBatchItems(getMaxNumberOfNewBatchItems());
        }
        for (int count = 0; count < getNumberOfNewBatchItems(); count++) {
            Sample sample = new Sample();
            sample.setSampleType(getSampleType());
            sample.setContainer(getContainer());
            getEditList().add(sample);
            getCreatedList().add(sample);
        }
        updateMaxNumberOfNewLines();
    }

    public void applyChangesToMultiValueFields() {
        for (Sample sample : getEditList()) {
            try {
                PropertyUtils.setProperty(sample, getSampleAttributeEnum().getName(), ((DualListModel<Annotation>) PropertyUtils.getProperty(this, "allRowsModal")).getTarget());
                sample.setChanged(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setAllRowsModal(null);
        updateBatchTable(getSampleAttributeEnum().getName(), -1);
    }

    public String cancel() {
        if (isEditView()) {
            if (getFractionsParent() != null) {
                return createRedirectShowScreenURL(getFractionsParent());
            }
            setSelectMode();
            return null;
        }
        return getShowScreenRedirectURL();
    }

    @Override
    public void clearGeneratedSampleNames() {
        clearGeneratedSampleNames(getCreatedList());
    }

    public void editSelectedItems() {
        getEditList().clear();
        getCreatedList().clear();
        getCheckedEditListItems().clear();
        getCustomAttributes().clear();
        setCustomListingRows(0);

        for (Sample sample : getSelectionList()) {
            if (sample.isChecked()) {
                getEditList().add(sample);
                if (!cloneColumnRendered && sample.isManaged()) {
                    cloneColumnRendered = true;
                }
            }
        }
        initializeInitialParentSamplesOfUserMultiplexForAllPooledLibraries(getEditList());
        initializeSelectAllValuesForIlluminaLibraryCalculation(getEditList());

        for (Sample sample : getEditList()) {
            getCustomAttributes().addAll(sample.getCustomAttributes());
        }

        evaluateCustomAttributeColumns();
        updateMaxNumberOfNewLines();
        setNumberOfNewBatchItems(1);

        setEditView(true);
    }

    @Override
    public void generateSampleNames() {
        generateSampleNames(getContainer(), getCreatedList());
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnums(String sampleTypeName) {
        return SampleAttributeEnum.getAttributeEnums(false, null, sampleTypeName, false, null, false, null, false, false);
    }

    public Map<Sample, Boolean> getCheckedEditListItems() {
        return checkedEditListItems;
    }

    public int getCloneCount() {
        return cloneCount;
    }

    public Container getContainer() {
        return getInstance();
    }

    public List<Sample> getCreatedList() {
        return createdList;
    }

    public Sample getCurrentSample() {
        return currentSample;
    }

    @Override
    public List<Sample> getEditList() {
        return editList;
    }

    public Integer getFractions() {
        return fractions;
    }

    public Sample getFractionsParent() {
        return fractionsParent;
    }

    @Override
    public String getRedirectURLAfterSave() {
        return getFractionsParent() != null ? super.getRedirectURLAfterSave() : Constants.EMPTY_STRING;
    }

    private Set<Long> getSampleIds() {
        Set<Long> sampleIds = new HashSet<>();
        for (Sample sample : getEditList()) {
            if (sample.getId() != 0) {
                sampleIds.add(sample.getId());
            }
        }
        return sampleIds;
    }

    public List<Sample> getSelectionList() {
        return selectionList;
    }

    @Override
    public String getShowScreenRedirectURL() {
        return createRedirectShowScreenURL(getContainer(), "samples", null);
    }

    public boolean getSkipSelect() {
        return skipSelect != null && skipSelect;
    }

    private List<Sample> getUpdatableSamplesByContainerAndSampleType(Container container, SampleType sampleType) {
        List<Sample> updatableSamples = new ArrayList<>();
        for (Sample sample : sampleService.getSamplesByContainerAndSampleType(container, sampleType)) {
            if (sample.isUpdatable()) {
                updatableSamples.add(sample);
            }
        }
        return updatableSamples;
    }

    @Override
    @PostConstruct
    public void init() {
        if (StringHelper.isNotEmpty(fractionsParentId) && StringHelper.isNotEmpty(fractionsSampleTypeId)) {
            setFractionsParent(entityService.find(Sample.class, Long.valueOf(fractionsParentId)));
            setSampleType(entityService.find(SampleType.class, Long.valueOf(fractionsSampleTypeId)));
            if (StringHelper.isEmpty(getId()) && getContextContainer() == null && getFractionsParent() != null) {
                setId(getFractionsParent().getContainer().getIdString());
            }
        }
        if (id == null && getContextContainer() != null && getContextContainer().isExtensible()) {
            setId(getContextContainer().getIdString());
        }
        setInstance(loadInstance());

        // Fractionate.
        if (getFractionsParent() != null && getSampleType() != null) {
            // Create child samples (fractions).
            Set<String> sampleNamesCreated = new HashSet<>();
            for (int i = 1; i <= getFractions(); i++) {
                Sample childSample = getFractionsParent().createChildSample(getSampleType(), null, null, false);
                childSample.setContainer(getContainer());
                childSample.setName(getFractionsParent().getName() + "_" + Messages.get("sampleNamePostfixFraction") + "_" + i);
                childSample.setTubeIdBySuffix(getContainerSpecificNextTubeIdSuffix(childSample.getContainer().getId()));
                incrementContainerSpecificNextTubeIdSuffix(childSample.getContainer().getId());
                childSample.setTubeIdPadded(sampleService.getTubeIdPadded(childSample.getTubeId()));
                childSample.assignValidName(sampleNamesCreated);
                sampleNamesCreated.add(childSample.getName());
                childSample.check();
                childSample.setFraction(true);
                getSelectionList().add(childSample);
            }
            setMaxNumberOfNewBatchItems(getMaxNumberOfBatchItems() - getFractions());
            editSelectedItems();
            skipSelect = true;
        }
    }

    public void initCloneItemPanel() {
        cloneCount = 1;
        updateMaxNumberOfNewLines();
    }

    @Override
    public void initializeEditList() {
        // Leave empty until sample type is selected. Then, the edit list will be populated accordingly.
    }

    public boolean isEditView() {
        return editView;
    }

    @Override
    public LinkedHashMap<Integer, LinkedHashMap<String, String>> isValid(int lo, int hi) {
        LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg = new LinkedHashMap<>();

        int hi_adapted = Math.min(hi, getEditList().size());
        Set<String> sampleNameSet = new HashSet<>();
        for (int i = lo; i < hi_adapted; i++) {
            Sample currentCheckSample = getEditList().get(i);

            if (!(getCheckedEditListItems().containsKey(currentCheckSample) && getCheckedEditListItems().get(currentCheckSample))) {
                validationErrorMsg.put(i, new LinkedHashMap<>());

                // The sample is not marked for deletion.
                String sampleName = currentCheckSample.getName().toLowerCase();
                if (StringHelper.isNotEmpty(sampleName)) {
                    if (sampleNameSet.contains(sampleName) || !sampleService.isValidName(sampleName, getSampleIds(), getContainer().getId())) {
                        // The sample name is invalid.
                        validationErrorMsg.get(i).put(Constants.SAMPLE_NAME, Messages.get("nameNotUniqueException"));
                    }
                    sampleNameSet.add(sampleName);
                } else {
                    validationErrorMsg.get(i).put(Constants.SAMPLE_NAME, Constants.REQUIRED);
                }

                // Check the multiplex id if the sample is multiplexed and user submitted.
                if (SampleAttributeEnum.MULTIPLEXED.isAttribute(currentCheckSample.getType())) {
                    String errorMessage = currentCheckSample.isValidMultiplexedByUser();
                    if (errorMessage != null) {
                        validationErrorMsg.get(i).put(SampleAttributeEnum.MULTIPLEXED.getName(), errorMessage);
                    }
                }

                checkSampleAttributesValidity(currentCheckSample, i, validationErrorMsg);
            }
        }

        return validationErrorMsg;
    }

    @Override
    public void removeEmptyLines() {
        getEditList().removeIf(Sample::isEmpty);
        updateMaxNumberOfNewLines();
    }

    public void sampleTypeChangedListener(ValueChangeEvent event) {
        setSampleType((SampleType) event.getNewValue());
        // Important: Due to a caching issue, reuse the existing list rather than assigning a new one.
        selectionList.clear();
        selectionList.addAll(getUpdatableSamplesByContainerAndSampleType(getContainer(), getSampleType()));

        getEditList().clear();
        getCheckedEditListItems().clear();
        getCustomAttributes().clear();
        getCustomAttributeColumns().clear();

        if (skipSelect != null && skipSelect) {
            editSelectedItems();
        }
    }

    public String saveSamples() {
        for (Sample sample : getEditList()) {
            resetSampleFields(sample);
        }
        removeEmptyLines();
        String redirectURL = super.save();
        if (redirectURL != null) {
            String message = sampleService.saveSamples(getEditList(), getCheckedEditListItems());
            getFacesMessagesManager().printWarn(message);
            if (getFractionsParent() != null) {
                return redirectURL;
            }
            // Render later the select view.
            setSelectMode();
        } else {
            handleValidationErrorsForBatch(getValidationErrorMsg());
        }
        return null;
    }

    public void setCheckedEditListItems(Map<Sample, Boolean> checkedEditListItems) {
        this.checkedEditListItems = checkedEditListItems;
    }

    public void setCloneCount(int cloneCount) {
        this.cloneCount = cloneCount;
    }

    public void setCurrentSample(Sample currentSample) {
        // Make sure, it is not detached (lazy loading...)
        if (currentSample.getId() != 0) {
            this.currentSample = entityService.find(Sample.class, currentSample.getId());
        } else {
            this.currentSample = currentSample;
        }
    }

    public void setEditList(List<Sample> editList) {
        this.editList = editList;
    }

    public void setEditView(boolean editView) {
        this.editView = editView;
    }

    public void setFractions(Integer fractions) {
        this.fractions = fractions;
    }

    public void setFractionsParent(Sample fractionsParent) {
        this.fractionsParent = fractionsParent;
    }

    private void setSelectMode() {
        Set<Long> checkIds = new HashSet<>();
        for (Sample sample : getEditList()) {
            if (sample.getId() != 0) {
                checkIds.add(sample.getId());
            }
        }
        getSampleIdentifiersNumberMap().clear();
        getInputQcSamplePlatesSampleMap().clear();
        getMolaritySamplePlatesSampleMap().clear();
        getInputQcSamplePlates().clear();
        getMolaritySamplePlates().clear();

        selectionList.clear();
        selectionList.addAll(getUpdatableSamplesByContainerAndSampleType(getContainer(), getSampleType()));
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("select-sample:selectiontable");
        if (dataTable != null) {
            dataTable.setValue(getSelectionList());
        }
        for (Sample sample : getSelectionList()) {
            if (checkIds.contains(sample.getId())) {
                sample.setChecked(true);
                checkIds.remove(sample.getId());
            }
            if (checkIds.isEmpty()) {
                break;
            }
        }

        setEditView(false);
    }

    public void setSelectionList(List<Sample> selectionList) {
        this.selectionList = selectionList;
    }
}