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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePlatePosition;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.omnifaces.cdi.Param;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;

@MeasureCalls
@Named
@ViewScoped
public class PlateSampleBatchManager extends AbstractSampleBatchManager<Plate> {

    private static final long serialVersionUID = 1;

    private Set<Container> containers = new HashSet<>();

    private List<Sample> editList = new ArrayList<>();

    private List<Sample> initialEditList = new ArrayList<>();

    @Param
    private Boolean plateSubmission;

    private Map<Sample, String> samplePlatePositions = new HashMap<>();

    public PlateSampleBatchManager() {
        super(Plate.class);
    }

    public void applyChangesToMultiValueFields() {
        for (final Sample sample : getEditList()) {
            try {
                PropertyUtils.setProperty(sample, getSampleAttributeEnum().getName(), ((DualListModel<Annotation>) PropertyUtils.getProperty(this, "allRowsModal")).getTarget());
                sample.setChanged(true);
            } catch (final Exception ignored) {
            }
        }
        setAllRowsModal(null);
        updateBatchTable(getSampleAttributeEnum().getName(), -1);
    }

    public void changeEditList() {
        if (!getSelectedContainers().isEmpty()) {
            List<Sample> filteredEditList = new ArrayList<>();
            for (Sample editedSample : getInitialEditList()) {
                if (getSelectedContainers().contains(editedSample.getContainer())) {
                    filteredEditList.add(editedSample);
                }
            }
            getEditList().clear();
            getEditList().addAll(filteredEditList);
        } else {
            getEditList().clear();
            getEditList().addAll(getInitialEditList());
        }
        PrimeFaces.current().executeScript("fixOversize();");
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnums() {
        return SampleAttributeEnum.getAttributeEnumsOrderedForPlate(SampleAttributeEnum.getAttributeEnums(getType(), getTypes()), getType(), getTypes(), getPlate());
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnumsWithoutTubeId() {
        List<SampleAttributeEnum> attributeEnums = new ArrayList<>(getAttributeEnums());
        attributeEnums.removeIf(sampleAttributeEnum -> sampleAttributeEnum.equals(SampleAttributeEnum.TUBE_ID));
        attributeEnums.removeIf(sampleAttributeEnum -> sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEXED));
        return attributeEnums;
    }

    public Set<Container> getContainers() {
        return containers;
    }

    public List<Container> getContainersFiltered(String filterString) {
        return getContainers().stream().filter(container -> !getSelectedContainers().contains(container) && container.getDisplayName().toLowerCase().contains(filterString))
            .collect(Collectors.toList());
    }

    @Override
    public List<Sample> getEditList() {
        return editList;
    }

    public List<Sample> getInitialEditList() {
        return initialEditList;
    }

    public Plate getPlate() {
        return getInstance();
    }

    public Boolean getPlateSubmission() {
        return plateSubmission;
    }

    public Map<Sample, String> getSamplePlatePositions() {
        return samplePlatePositions;
    }

    @Override
    public String getShowScreenRedirectURL() {
        if (getPlateSubmission() != null && getPlateSubmission()) {
            return getPlate().getContainer() != null && !getPlate().getContainer().isContainerProject() ? createRedirectShowScreenURL(getPlate().getContainer(), null, null) : getUrlHomeScreen();
        }
        return createRedirectShowScreenURL(getPlate(), "samples", null);
    }

    @Override
    @PostConstruct
    public void init() {
        if (getId() != null) {
            String requestUri = getRequestURI();
            // Set the plate.
            setInstance(getPlateSubmission() != null && getPlateSubmission() && requestUri != null && requestUri.contains("/plate/edit-batch-samples.html") ? loadInstanceForBatchEditing() : loadInstance());
            // Initialize the editList.
            getEditList().clear();
            initializeEditList();
            initializeInitialParentSamplesOfUserMultiplexForAllPooledLibraries(getEditList());
            initializeSelectAllValuesForIlluminaLibraryCalculation(getEditList());
            getCustomListingRows();
        }
    }

    @Override
    public void initializeEditList() {
        if (getPlate() != null && !getPlate().getSamplePlatePositions().isEmpty() && getPlate().isUpdatableOrUserUpdatable()) {
            List<SamplePlatePosition> orderedSamplePlatePositions = new ArrayList<>();
            Map<SamplePlatePosition, Sample> samplePlatePositionSampleMap = new HashMap<>();
            getInitialEditList().clear();
            getContainers().clear();
            getSelectedContainers().clear();
            for (SamplePlatePosition aSamplePlatePosition : getPlate().getSamplePlatePositions()) {
                Sample sample = aSamplePlatePosition.getSample();
                if (sample.isUpdatable()) {
                    orderedSamplePlatePositions.add(aSamplePlatePosition);
                    samplePlatePositionSampleMap.put(aSamplePlatePosition, sample);
                    getCustomAttributes().addAll(sample.getCustomAttributes());
                    getSamplePlatePositions().put(sample, getPlate().getPlateLayout().getGridPosition(aSamplePlatePosition.getPosition()));
                    getSampleTypes().add(sample.getSampleType());
                    getTypes().add(sample.getSampleType().getName());
                    getContainers().add(sample.getContainer());
                }
            }

            // Order the samples in the editList by their sample plate positions according to their assignment order on the plate.
            orderedSamplePlatePositions = getPlate().getPlateLayout().getSamplePlatePositionsOrderedByAssignmentOrder(orderedSamplePlatePositions);
            for (SamplePlatePosition aSamplePlatePosition : orderedSamplePlatePositions) {
                getEditList().add(samplePlatePositionSampleMap.get(aSamplePlatePosition));
                getInitialEditList().add(samplePlatePositionSampleMap.get(aSamplePlatePosition));
            }

            // Evaluate the custom attribute columns.
            evaluateCustomAttributeColumns();
        }

        if (getTypes().size() == 1) {
            // All samples in the editList are of the same type.
            setSampleType(getSampleTypes().iterator().next());
            setSampleTypes(null);
            setType(getTypes().iterator().next());
            setTypes(null);
        } else if (getTypes().size() > 1) {
            // The samples in the editList are of different types.
            setSampleType(null);
            setType(null);
        }
    }

    public boolean isSampleNamesNotEditable() {
        return true;
    }

    @Override
    public LinkedHashMap<Integer, LinkedHashMap<String, String>> isValid(int lo, int hi) {
        LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg = new LinkedHashMap<>();

        int hi_adapted = Math.min(hi, getEditList().size());
        for (int i = lo; i < hi_adapted; i++) {
            validationErrorMsg.put(i, new LinkedHashMap<>());
            checkSampleAttributesValidity(getEditList().get(i), i, validationErrorMsg);
        }

        return validationErrorMsg;
    }

    public Plate loadInstanceForBatchEditing() {
        // Important: Do not delete this method as the access control via entity.isReadable() in the loadInstance method is too restrictive and not correct for the intended use case.
        if (getIdLong() != null) {
            Plate plate = getInstance(getIdLong());
            if (plate != null) {
                if (plate.isUpdatableOrUserUpdatable()) {
                    return plate;
                } else {
                    getSessionManager().redirectRelative("/error/permission-denied.html");
                }
            }
        }
        return null;
    }

    @Override
    public String save() {
        for (Sample sample : getEditList()) {
            resetSampleFields(sample);
        }

        String redirectURL = super.save();
        if (redirectURL != null) {
            String message = sampleService.saveSamples(getEditList(), null);
            getFacesMessagesManager().bufferWarningClear(message);
            return redirectURL;
        }
        handleValidationErrorsForBatch(getValidationErrorMsg());
        return null;
    }

    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

    public void setEditList(List<Sample> editList) {
        this.editList = editList;
    }

    public void setInitialEditList(List<Sample> initialEditList) {
        this.initialEditList = initialEditList;
    }

    public void setPlateSubmission(Boolean plateSubmission) {
        this.plateSubmission = plateSubmission;
    }

    public void setSamplePlatePositions(Map<Sample, String> samplePlatePositions) {
        this.samplePlatePositions = samplePlatePositions;
    }

}
