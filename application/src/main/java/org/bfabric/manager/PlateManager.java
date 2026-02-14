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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import net.sf.ehcache.util.FindBugsSuppressWarnings;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.MultiplexId;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.Plate;
import org.bfabric.entity.PlateLayout;
import org.bfabric.entity.PlateType;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePlatePosition;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.MultiplexIdService;
import org.bfabric.service.PlateService;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.DragDropEvent;

@MeasureCalls
@Named
@ViewScoped
public class PlateManager extends AbstractEntityManager<Plate> {

    private static final long serialVersionUID = 1;

    private final Map<Integer, Long> createdSampleIdentifiersParentSamplesMap = new HashMap<>();

    private final Set<Integer> emptyPositionIndices = new HashSet<>();

    private final Map<Long, SamplePlatePosition> initialSampleIdSamplePlatePositionMap = new HashMap<>();

    private final Map<Long, Sample> markedSamples = new HashMap<>();

    private final List<Sample> selectedSamples = new ArrayList<>();

    private final Set<Long> selectedSamplesIds = new HashSet<>();

    private final Set<String> types = new HashSet<>();

    private final Set<String> typesNonCached = new HashSet<>();

    private final Set<SampleQCTypeEnum> qcTypes = new HashSet<>();

    private final Set<SampleFormEnum> sampleForms = new HashSet<>();

    private final Map<Sample, Set<Sample>> controlSampleChildrenCreatedMap = new HashMap<>();

    @Param
    private String clonedPlateName;

    @Param
    private String clonedPlateTypeId;

    @Param
    private String clonedQualityControlTypeName;

    private List<String> combinedMultiplexIdNames;

    private Map<String, String> combinedMultiplexIdNamesPrettyPrintMap;

    private Map<String, Map<String, String>> combinedMultiplexIdsMap;

    private Boolean filterQcPassed;

    private Boolean filterUserDecision = Boolean.TRUE;

    private boolean initialMultiplexIdWrapAround = true;

    private boolean isGridView = true;

    private boolean isSampleAssignmentPerRow;

    private boolean linkedMultiplexId = true;

    private boolean multiplexIdAssignmentWrapAround = false;

    @Inject
    private MultiplexIdService multiplexIdService;

    private boolean[] multiplexIdsAssignability;

    private String multiplexIdsAssignmentAttribute;

    @Inject
    private PlateService plateService;

    private SampleQCTypeEnum qualityControlType = SampleQCTypeEnum.AGILENT_BIOANALYZER;

    private boolean savePlateHintRendered = false;

    private String selectedInitialCombinedMultiplexIdForMultiplexIdAssignment;

    private MultiplexId selectedInitialMultiplexIdForMultiplexIdAssignment;

    private MultiplexKit selectedMultiplexKit;

    private LinkedHashMap<Integer, Sample> selectedSamplesForMultiplexIdAssignment;

    private boolean showAllColumns = false;

    private boolean showControlOnly = false;

    private boolean showOrphansOnly = true;

    private Container singlePlateContainer;

    private Integer startPositionForMultiplexIdAssignment;

    private List<Integer> startPositionsForMultiplexIdAssignment;

    private boolean startPositionsForMultiplexIdAssignmentWrapAround = true;

    private SampleAttributeEnum tileAttribute;

    public PlateManager() {
        super(Plate.class);
    }

    public void assignMultiplexIds() {
        if (getSelectedInitialMultiplexIdForMultiplexIdAssignment() != null) {
            List<MultiplexId> multiplexIds = getSelectedMultiplexKit()
                .getOrderedEnabledMultiplexIdsByPlateLayoutAndType(getPlate().getPlateLayout(), isSampleAssignmentPerRow(), getMultiplexIdsAssignmentAttribute());
            List<MultiplexId> linkedMultiplexIds = null;
            if (isLinkedMultiplexId() && Messages.get("multiplexIdAndMultiplexId2").equals(getMultiplexIdsAssignmentAttribute())) {
                // At this point, it is certain that multiplex ids corresponds to multiplexId and hence linkedMultiplexIds have to refer to multiplexId2.
                linkedMultiplexIds = CDI.current().select(MultiplexIdService.class).get()
                    .getOrderedEnabledMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(getSelectedMultiplexKit().getId(), getPlate()
                        .getPlateLayout(), isSampleAssignmentPerRow(), Messages
                        .get("multiplexId2"));
            }
            int initialMultiplexIdIndex = multiplexIds.indexOf(getSelectedInitialMultiplexIdForMultiplexIdAssignment());
            if (initialMultiplexIdIndex > -1) {
                int multiplexIdIndex = initialMultiplexIdIndex;
                int assignedMultiplexIdsCounter = 0;
                int maximumAssignableMultiplexIds = getStartPositionsForMultiplexIdAssignment().size() - getStartPositionsForMultiplexIdAssignment()
                    .indexOf(getStartPositionForMultiplexIdAssignment());
                List<Integer> positionsOrderedFromStartPosition = new ArrayList<>(getStartPositionsForMultiplexIdAssignment()
                    .subList(getStartPositionsForMultiplexIdAssignment().indexOf(getStartPositionForMultiplexIdAssignment()), getStartPositionsForMultiplexIdAssignment().size()));
                positionsOrderedFromStartPosition
                    .addAll(getStartPositionsForMultiplexIdAssignment().subList(0, getStartPositionsForMultiplexIdAssignment().indexOf(getStartPositionForMultiplexIdAssignment())));
                int positionsOrderedFromStartPositionIndex = 0;
                for (int i = Math.min(multiplexIds.size(), getSelectedSamplesForMultiplexIdAssignment().size()); i > 0 && positionsOrderedFromStartPositionIndex < positionsOrderedFromStartPosition
                    .size(); i--) {
                    if (!isStartPositionsForMultiplexIdAssignmentWrapAround() && assignedMultiplexIdsCounter == maximumAssignableMultiplexIds) {
                        // No wrap-around at the end of the plate.
                        break;
                    }
                    if (multiplexIdIndex == multiplexIds.size()) {
                        if (isInitialMultiplexIdWrapAround()) {
                            multiplexIdIndex = 0;
                        } else {
                            // No wrap-around at the end of the multiplex id list.
                            break;
                        }
                    }
                    if (getSelectedSamplesForMultiplexIdAssignment().containsKey(positionsOrderedFromStartPosition.get(positionsOrderedFromStartPositionIndex))) {
                        Sample sample = getSelectedSamplesForMultiplexIdAssignment().get(positionsOrderedFromStartPosition.get(positionsOrderedFromStartPositionIndex));
                        if (linkedMultiplexIds != null && isLinkedMultiplexId() && Messages.get("multiplexIdAndMultiplexId2").equals(getMultiplexIdsAssignmentAttribute())) {
                            sample.setMultiplexKit(getSelectedMultiplexKit());
                            sample.setMultiplexId(multiplexIds.get(multiplexIdIndex).getSequence());
                            sample.setMultiplexKit2(getSelectedMultiplexKit());
                            sample.setMultiplexId2(linkedMultiplexIds.get(multiplexIdIndex).getSequence());
                        } else if (Messages.get("multiplexId").equals(getMultiplexIdsAssignmentAttribute())) {
                            sample.setMultiplexKit(getSelectedMultiplexKit());
                            sample.setMultiplexId(multiplexIds.get(multiplexIdIndex).getSequence());
                        } else if (Messages.get("multiplexId2").equals(getMultiplexIdsAssignmentAttribute())) {
                            sample.setMultiplexKit2(getSelectedMultiplexKit());
                            sample.setMultiplexId2(multiplexIds.get(multiplexIdIndex).getSequence());
                        }
                    } else {
                        break;
                    }
                    multiplexIdIndex++;
                    assignedMultiplexIdsCounter++;
                    positionsOrderedFromStartPositionIndex++;
                }
            }
        } else if (getSelectedInitialCombinedMultiplexIdForMultiplexIdAssignment() != null) {
            // Combined multiplex ids.
            List<String> orderedCombinedMultiplexIdNames = getOrderedEnabledCombinedMultiplexIdsByPlateLayoutAndTypeFiltered(null);
            int initialMultiplexIdIndex = orderedCombinedMultiplexIdNames.indexOf(getSelectedInitialCombinedMultiplexIdForMultiplexIdAssignment());
            if (initialMultiplexIdIndex > -1) {
                int multiplexIdIndex = initialMultiplexIdIndex;
                int assignedMultiplexIdsCounter = 0;
                int maximumAssignableMultiplexIds = getStartPositionsForMultiplexIdAssignment().size() - getStartPositionsForMultiplexIdAssignment()
                    .indexOf(getStartPositionForMultiplexIdAssignment());
                List<Integer> positionsOrderedFromStartPosition = new ArrayList<>(getStartPositionsForMultiplexIdAssignment()
                    .subList(getStartPositionsForMultiplexIdAssignment().indexOf(getStartPositionForMultiplexIdAssignment()), getStartPositionsForMultiplexIdAssignment().size()));
                positionsOrderedFromStartPosition
                    .addAll(getStartPositionsForMultiplexIdAssignment().subList(0, getStartPositionsForMultiplexIdAssignment().indexOf(getStartPositionForMultiplexIdAssignment())));
                int positionsOrderedFromStartPositionIndex = 0;
                for (int i = Math
                    .min(orderedCombinedMultiplexIdNames.size(), getSelectedSamplesForMultiplexIdAssignment()
                        .size()); i > 0 && positionsOrderedFromStartPositionIndex < positionsOrderedFromStartPosition
                    .size(); i--) {
                    if (!isStartPositionsForMultiplexIdAssignmentWrapAround() && assignedMultiplexIdsCounter == maximumAssignableMultiplexIds) {
                        // No wrap-around at the end of the plate.
                        break;
                    }
                    if (multiplexIdIndex == orderedCombinedMultiplexIdNames.size()) {
                        if (isInitialMultiplexIdWrapAround()) {
                            multiplexIdIndex = 0;
                        } else {
                            // No wrap-around at the end of the multiplex id names list.
                            break;
                        }
                    }
                    if (getSelectedSamplesForMultiplexIdAssignment().containsKey(positionsOrderedFromStartPosition.get(positionsOrderedFromStartPositionIndex))) {
                        Sample sample = getSelectedSamplesForMultiplexIdAssignment().get(positionsOrderedFromStartPosition.get(positionsOrderedFromStartPositionIndex));
                        if (getSelectedMultiplexKit().isCombinedMultiplexId() && isLinkedMultiplexId() && Messages.get("multiplexIdAndMultiplexId2")
                            .equals(getMultiplexIdsAssignmentAttribute())) {
                            sample.setMultiplexKit(getSelectedMultiplexKit());
                            sample.setMultiplexId(getCombinedMultiplexIdsMap().get(orderedCombinedMultiplexIdNames.get(multiplexIdIndex)).get(Messages.get("multiplexId")));
                            sample.setMultiplexKit2(getSelectedMultiplexKit());
                            sample.setMultiplexId2(getCombinedMultiplexIdsMap().get(orderedCombinedMultiplexIdNames.get(multiplexIdIndex)).get(Messages.get("multiplexId2")));
                        }
                    } else {
                        break;
                    }
                    multiplexIdIndex++;
                    assignedMultiplexIdsCounter++;
                    positionsOrderedFromStartPositionIndex++;
                }
            }
        }
        resetAssignMultiplexIdsModalPanel();
        /*
        if (!getPlate().isSampleAssignmentEditable()) {
            save();
        }
        */
    }

    public String assignSamples() {
        LinkedHashMap<String, String> validationErrorMsg = plateService.isValidPlateTypeUserSubmitted(getPlate(), getSelectedSamples(), getSinglePlateContainer());
        if (validationErrorMsg.isEmpty()) {
            for (Map.Entry<Sample, Set<Sample>> entry : getControlSampleChildrenCreatedMap().entrySet()) {
                // Created child samples for samples of type 'Control Sample'.
                Set<String> sampleNamesCreated = new HashSet<>();
                sampleNamesCreated.add(entry.getKey().getName());
                for (Sample sample : entry.getValue()) {
                    sample.assignValidName(sampleNamesCreated);
                    sampleNamesCreated.add(sample.getName());
                }
            }
            plateService.assignSamples(getPlate(), getSelectedSamples(), getInitialSampleIdSamplePlatePositionMap());
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
            return getShowScreenRedirectURL("samples");
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void cancelAssignMultiplexIds() {
        setSavePlateHintRendered(false);
    }

    public String changeStatus(StatusEnum statusEnum) {
        printFacesMessagesClear(plateService.changeStatus(getPlate(), statusEnum, true));
        return getShowScreenRedirectURL();
    }

    public String clonePlate() {
        if (getPlate().getCloneMode() != null) {
            if (isCloneModeSamplesNone()) {
                // Cloning without the samples.
                return createRedirectURL("plate/edit", getPlate().getId(), null, Collections.singletonMap("clone", "true"));
            }

            LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
            if (getPlate().getClonedPlateName() == null) {
                validationErrorMsg.put("clonePlate:name", Messages.get("required"));
            } else if (!plateService.checkUniqueAttributeValue(Plate.class, Constants.NAME, getPlate().getClonedPlateName())) {
                validationErrorMsg.put("clonePlate:name", Messages.get("notUniqueException"));
            }
            if (getPlate().getClonedPlateType() == null) {
                validationErrorMsg.put("clonePlate:plateTypeClone", Messages.get("required"));
            }

            if (validationErrorMsg.isEmpty()) {
                // Cloning with the samples.
                final Map<String, String> fParams = new HashMap<>(Collections.singletonMap("clone", "true"));
                fParams.put("clonedPlateName", getPlate().getClonedPlateName());
                fParams.put("clonedPlateTypeId", getPlate().getClonedPlateType().getIdString());
                if (getPlate().getClonedPlateType().isQualityControlPlateType()) {
                    if (getPlate().getClonedQualityControlType() != null) {
                        fParams.put("clonedQualityControlTypeName", getPlate().getClonedQualityControlType().name());
                        return createRedirectURL("plate/assignSamples", getPlate().getId(), null, fParams);
                    }
                    getFacesMessagesManager().validationError("clonePlate:qualityControlTypeClone", Messages.get("required"));
                } else {
                    return createRedirectURL("plate/assignSamples", getPlate().getId(), null, fParams);
                }
            } else {
                getFacesMessagesManager().printValidationErrors(validationErrorMsg);
            }
        } else {
            getFacesMessagesManager().validationError("clonePlate:cloneMode", Messages.get("required"));
        }
        if (FacesContext.getCurrentInstance().isValidationFailed()) {
            FacesContext.getCurrentInstance().validationFailed();
        }
        return null;
    }

    public Sample createChildChildSampleIfNecessary(Sample sample) {
        Sample selectedSample = sample;
        if (sample != null) {
            SampleType targetSampleType = getPlate().getPlateType().getSampleType();
            if (targetSampleType == null) {
                targetSampleType = sample.getSampleType();
            }
            if (!targetSampleType.equals(sample.getSampleType()) || sample.getControlSample() != null) {
                // Only samples of a specific type are allowed on the plate.
                selectedSample = sample.createChildSampleOnPlate(targetSampleType);
                // Setting necessary values for processing depending on the sample type.
                if (targetSampleType.getName().equals(SampleTypeEnum.QUALITY_CONTROL.getLabel())) {
                    // Quality Control Sample
                    selectedSample.setQualityControlType(getQualityControlType());
                }
                getCreatedSampleIdentifiersParentSamplesMap().put(selectedSample.hashCode(), sample.getId());
            }
        }
        return selectedSample;
    }

    public void deselectAllSamples() {
        for (int i = 0; i < getSelectedSamples().size(); i++) {
            if (getSelectedSamples().get(i) != null) {
                getSelectedSamples().set(i, null);
                getEmptyPositionIndices().add(i);
            }
        }
        getControlSampleChildrenCreatedMap().clear();
        getSelectedSamplesIds().clear();
        getTypesNonCached().clear();
        getMarkedSamples().clear();
        updateAddButtonColumnOfSampleSelectionTable();
    }

    public void deselectMarkedSamples() {
        Set<String> remainingTypes = new HashSet<>();
        for (int i = 0; i < getSelectedSamples().size(); i++) {
            if (getSelectedSamples().get(i) != null && getMarkedSamples()
                .containsKey(getSelectedSamples().get(i).isManaged() ? getSelectedSamples().get(i).getId() : getCreatedSampleIdentifiersParentSamplesMap()
                    .get(getSelectedSamples().get(i).hashCode()))) {
                getSelectedSamplesIds()
                    .remove(getSelectedSamples().get(i).isManaged() ? getSelectedSamples().get(i).getId() : getCreatedSampleIdentifiersParentSamplesMap().get(getSelectedSamples().get(i).hashCode()));
                getSelectedSamples().set(i, null);
                getEmptyPositionIndices().add(i);
            } else {
                if (getSelectedSamples().get(i) != null) {
                    remainingTypes.add(getSelectedSamples().get(i).getType());
                }
            }
        }
        for (Sample sample : getMarkedSamples().values()) {
            if (sample != null) {
                sample.setChecked(false);
                if (sample.getControlSampleParent() != null && !sample.isManaged() && getControlSampleChildrenCreatedMap().containsKey(sample.getControlSampleParent())) {
                    getControlSampleChildrenCreatedMap().get(sample.getControlSampleParent()).remove(sample);
                }
                if (!remainingTypes.contains(sample.getSampleType().getName())) {
                    getTypesNonCached().remove(sample.getSampleType().getName());
                }
            }
        }
        getMarkedSamples().clear();
        updateAddButtonColumnOfSampleSelectionTable();
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnums() {
        return SampleAttributeEnum.getAttributeEnumsOrderedForPlate(SampleAttributeEnum.getAttributeEnums(null, getTypes(), getQcTypes(), getSampleForms()), null, getTypes(), getPlate());
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnumsAssignSamplesSelectColumns() {
        List<SampleAttributeEnum> sampleAttributeEnums = SampleAttributeEnum.getAttributeEnums(true, SampleTypeEnum.SEQUENCING.getLabel());
        SampleAttributeEnum.orderSampleAttributes(sampleAttributeEnums);
        return sampleAttributeEnums;
    }

    public String getClonedPlateName() {
        return clonedPlateName;
    }

    public String getClonedPlateTypeId() {
        return clonedPlateTypeId;
    }

    public String getClonedQualityControlTypeName() {
        return clonedQualityControlTypeName;
    }

    public List<String> getCombinedMultiplexIdNames() {
        return combinedMultiplexIdNames;
    }

    public Map<String, String> getCombinedMultiplexIdNamesPrettyPrintMap() {
        return combinedMultiplexIdNamesPrettyPrintMap;
    }

    public Map<String, Map<String, String>> getCombinedMultiplexIdsMap() {
        return combinedMultiplexIdsMap;
    }

    public Map<Sample, Set<Sample>> getControlSampleChildrenCreatedMap() {
        return controlSampleChildrenCreatedMap;
    }

    public Map<Integer, Long> getCreatedSampleIdentifiersParentSamplesMap() {
        return createdSampleIdentifiersParentSamplesMap;
    }

    public Set<Integer> getEmptyPositionIndices() {
        return emptyPositionIndices;
    }

    public Boolean getFilterQcPassed() {
        return filterQcPassed;
    }

    public Boolean getFilterUserDecision() {
        return filterUserDecision;
    }

    public Map<Long, SamplePlatePosition> getInitialSampleIdSamplePlatePositionMap() {
        return initialSampleIdSamplePlatePositionMap;
    }

    public Map<Long, Sample> getMarkedSamples() {
        return markedSamples;
    }

    public boolean[] getMultiplexIdsAssignability() {
        return multiplexIdsAssignability != null ? multiplexIdsAssignability.clone() : null;
    }

    public String getMultiplexIdsAssignmentAttribute() {
        return multiplexIdsAssignmentAttribute;
    }

    public List<String> getOrderedEnabledCombinedMultiplexIdsByPlateLayoutAndTypeFiltered(String filterString) {
        if (getCombinedMultiplexIdNames() != null) {
            int numberOfColumns = getPlate().getPlateLayout().getColumns();
            int capacity = getPlate().getPlateLayout().getCapacity();
            if (StringHelper.isEmpty(filterString)) {
                return multiplexIdService.orderCombinedMultiplexIdNamesByPlateLayout(getCombinedMultiplexIdNames(), numberOfColumns, isSampleAssignmentPerRow(), capacity);
            }
            return multiplexIdService
                .orderCombinedMultiplexIdNamesByPlateLayout(getCombinedMultiplexIdNames().stream().filter(combinedMultiplexIdName -> combinedMultiplexIdName.toLowerCase().contains(filterString))
                    .collect(Collectors.toList()), numberOfColumns, isSampleAssignmentPerRow(), capacity);
        }
        return null;
    }

    public List<MultiplexId> getOrderedEnabledMultiplexIdsByPlateLayoutAndTypeFiltered(String filterString) {
        return multiplexIdService.getOrderedEnabledMultiplexIdsByMultiplexKitIdAndPlateLayoutAndTypeFiltered(filterString, getSelectedMultiplexKit().getId(), getPlate()
            .getPlateLayout(), isSampleAssignmentPerRow(), getMultiplexIdsAssignmentAttribute());
    }

    @Produces
    @Named("plate")
    public Plate getPlate() {
        return getInstance();
    }

    @CachedMethodResult
    public Set<SampleQCTypeEnum> getQcTypes() {
        return qcTypes;
    }

    public SampleQCTypeEnum getQualityControlType() {
        return qualityControlType;
    }

    public long getRemainingCapacity() {
        return getEmptyPositionIndices().size();
    }

    public String getRemarkForMultiplexIdAssignment() {
        // upperLimitInitial: 1. Case: The number of multiplex ids in the multiplex kit is greater than samples on the plate, i.e., the initial upper limit is represented by the number of samples on the plate.
        // 2. Case: The number of multiplex ids in the multiplex kit is less than samples on the plate, i.e., the initial upper limit is represented by the number of multiplex ids in the multiplex kit.
        List<?> multiplexIds = null;
        int initialMultiplexIdIndex = -1;
        if (getSelectedInitialMultiplexIdForMultiplexIdAssignment() != null) {
            multiplexIds = getSelectedMultiplexKit()
                .getOrderedEnabledMultiplexIdsByPlateLayoutAndType(getPlate().getPlateLayout(), isSampleAssignmentPerRow(), getMultiplexIdsAssignmentAttribute());
            if (multiplexIds != null) {
                initialMultiplexIdIndex = multiplexIds.indexOf(getSelectedInitialMultiplexIdForMultiplexIdAssignment());
            }
        } else if (getSelectedInitialCombinedMultiplexIdForMultiplexIdAssignment() != null) {
            // Combined multiplex ids.
            multiplexIds = getOrderedEnabledCombinedMultiplexIdsByPlateLayoutAndTypeFiltered(null);
            if (multiplexIds != null) {
                initialMultiplexIdIndex = multiplexIds.indexOf(getSelectedInitialCombinedMultiplexIdForMultiplexIdAssignment());
            }
        }
        if (multiplexIds != null && initialMultiplexIdIndex > -1) {
            int upperLimitInitial = Math.min(multiplexIds.size(), getSelectedSamplesForMultiplexIdAssignment().size());
            int upperLimitWithoutMultiplexIdWrapAround = multiplexIds.size() - initialMultiplexIdIndex;
            int upperLimitWithoutPositionWrapAround = getStartPositionsForMultiplexIdAssignment().size() - getStartPositionsForMultiplexIdAssignment()
                .indexOf(getStartPositionForMultiplexIdAssignment());
            if (isInitialMultiplexIdWrapAround() && isStartPositionsForMultiplexIdAssignmentWrapAround() || upperLimitInitial < upperLimitWithoutMultiplexIdWrapAround && upperLimitInitial < upperLimitWithoutPositionWrapAround) {
                // The upper limit is represented by the initial upper limit.
                return upperLimitInitial + " multiplex id(s) will be assigned to " + getSelectedSamplesForMultiplexIdAssignment().size() + " samples";
            }
            int upperLimit;
            if (isInitialMultiplexIdWrapAround() && !isStartPositionsForMultiplexIdAssignmentWrapAround()) {
                // Wrap-around at the end of the multiplex id list and no wrap-around at the end of the plate.
                upperLimit = Math.min(upperLimitInitial, upperLimitWithoutPositionWrapAround);
            } else if (!isInitialMultiplexIdWrapAround() && isStartPositionsForMultiplexIdAssignmentWrapAround()) {
                // No wrap-around at the end of the multiplex id list and wrap-around at the end of the plate.
                upperLimit = Math.min(upperLimitInitial, upperLimitWithoutMultiplexIdWrapAround);
            } else {
                // No wrap-around at the end of the multiplex id list and no wrap-around at the end of the plate.
                upperLimit = Math.min(upperLimitInitial, Math.min(upperLimitWithoutPositionWrapAround, upperLimitWithoutMultiplexIdWrapAround));
            }
            return upperLimit + " multiplex id(s) will be assigned to " + upperLimit + " samples";
        }
        return null;
    }

    public List<SampleAttributeEnum> getSampleAttributeEnumsForTile(String filterString) {
        List<SampleAttributeEnum> sampleAttributeEnums = SampleAttributeEnum.getAttributeEnums(null, getTypesNonCached(), null, null);
        // Note: The removed attribute(s) are the non-selectable, i.e., attributes which are either always shown or never.
        sampleAttributeEnums.remove(SampleAttributeEnum.TUBE_ID);
        sampleAttributeEnums.removeIf(SampleAttributeEnum::isAnnotationTypeMultiValued);
        sampleAttributeEnums = StringHelper.isNotEmpty(filterString) ? sampleAttributeEnums.stream()
            .filter(sampleAttributeEnum -> sampleAttributeEnum.getLabel().toLowerCase().contains(filterString.toLowerCase())).collect(Collectors.toList()) : sampleAttributeEnums;
        sampleAttributeEnums.sort(Comparator.comparing(SampleAttributeEnum::getLabel));
        return sampleAttributeEnums;
    }

    @CachedMethodResult
    public Set<SampleFormEnum> getSampleForms() {
        return sampleForms;
    }

    public LinkedHashMap<Integer, Sample> getSamplesForMultiplexIdAssignment() {
        LinkedHashMap<Integer, Sample> samplesForMultiplexIdAssignment = new LinkedHashMap<>();
        if (!getSelectedSamples().isEmpty()) {
            for (int nextIndex : sortIndices(Arrays.stream(IntStream.rangeClosed(0, getSelectedSamples().size() - 1).toArray()).boxed().collect(Collectors.toSet()))) {
                Sample sample = getSelectedSamples().get(nextIndex);
                if (sample != null) {
                    samplesForMultiplexIdAssignment.put(nextIndex, sample);
                }
            }
        }
        return samplesForMultiplexIdAssignment;
    }

    public String getSelectedInitialCombinedMultiplexIdForMultiplexIdAssignment() {
        return selectedInitialCombinedMultiplexIdForMultiplexIdAssignment;
    }

    public MultiplexId getSelectedInitialMultiplexIdForMultiplexIdAssignment() {
        return selectedInitialMultiplexIdForMultiplexIdAssignment;
    }

    public MultiplexKit getSelectedMultiplexKit() {
        return selectedMultiplexKit;
    }

    public List<Sample> getSelectedSamples() {
        return selectedSamples;
    }

    public LinkedHashMap<Integer, Sample> getSelectedSamplesForMultiplexIdAssignment() {
        return selectedSamplesForMultiplexIdAssignment;
    }

    public Set<Long> getSelectedSamplesIds() {
        return selectedSamplesIds;
    }

    public Container getSinglePlateContainer() {
        return singlePlateContainer;
    }

    public Integer getStartPositionForMultiplexIdAssignment() {
        return startPositionForMultiplexIdAssignment;
    }

    public List<Integer> getStartPositionsForMultiplexIdAssignment() {
        return startPositionsForMultiplexIdAssignment;
    }

    public SampleAttributeEnum getTileAttribute() {
        return tileAttribute;
    }

    @CachedMethodResult
    public Set<String> getTypes() {
        return types;
    }

    public Set<String> getTypesNonCached() {
        return typesNonCached;
    }

    private void handleSelectedControlSample(Sample sample, Sample selectedSample) {
        if (sample != null && selectedSample != null && !selectedSample.isManaged()) {
            if (!getControlSampleChildrenCreatedMap().containsKey(sample)) {
                getControlSampleChildrenCreatedMap().put(sample, new HashSet<>());
            }
            getControlSampleChildrenCreatedMap().get(sample).add(selectedSample);
            selectedSample.setControlSampleParent(sample);
        }
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getPlate() != null) {
            getControlSampleChildrenCreatedMap().clear();
            // Populate the plate.
            if (isManaged()) {
                // Fill up all positions with empty (null) values.
                for (int i = 0; i < getPlate().getPlateLayout().getCapacity(); i++) {
                    getSelectedSamples().add(null);
                    getEmptyPositionIndices().add(i);
                }

                if (!getPlate().getSamplePlatePositions().isEmpty()) {
                    if (getPlate().isPlateTypeUserSubmitted()) {
                        // A user submitted plate has only samples of a single order.
                        setSinglePlateContainer(getPlate().getSamplePlatePositions().iterator().next().getSample().getContainer());
                    }
                    for (SamplePlatePosition samplePlatePosition : getPlate().getSamplePlatePositions()) {
                        initPlateHelper(samplePlatePosition);
                        if (samplePlatePosition != null) {
                            final Sample sample = samplePlatePosition.getSample();
                            // Add the parent sample to the selectedSamplesIds except if the parent is a control sample as they can be on the same plate multiple times.
                            if (sample.getParents().size() == 1 && !sample.getParents().iterator().next().getType().equals(SampleTypeEnum.CONTROL_SAMPLE.getLabel()) && (getPlate().getPlateType()
                                .isQualityControlPlateType() || getPlate().getPlateType().isIlluminaLibraryPlateType() || getPlate().getPlateType().isPacBioLibraryPlateType() || getPlate()
                                .getPlateType().isNanoporeLibraryPlateType())) {
                                getSelectedSamplesIds().add(sample.getParents().iterator().next().getId());
                            }
                        }
                    }
                    qcTypes.remove(null);
                    sampleForms.remove(null);
                }
            } else {
                getPlate().setSupervisor(getCurrentUser());
                if (isCloned()) {
                    getPlate().setContainer(null);
                    // At this point in time, an empty plate is available. This represents the clone mode "Without Samples".
                    if (getClonedPlateName() != null && getClonedId() != null && getClonedPlateTypeId() != null) {
                        final Plate clonedPlate = entityService.find(Plate.class, getClonedId());
                        final PlateType clonedPlateType = entityService.find(PlateType.class, Long.parseLong(getClonedPlateTypeId()));
                        if (clonedPlate != null && clonedPlateType != null) {
                            // At this point in time, an empty plate is available to be populated. This represents the clone mode "Reference Samples".
                            getPlate().setName(getClonedPlateName());
                            getPlate().setPlateType(clonedPlateType);
                            if (getClonedQualityControlTypeName() != null) {
                                try {
                                    SampleQCTypeEnum aClonedQualityControlType = SampleQCTypeEnum.value(getClonedQualityControlTypeName());
                                    if (aClonedQualityControlType != null) {
                                        setQualityControlType(aClonedQualityControlType);
                                    }
                                } catch (InvalidEnumValueException invalidEnumValueException) {
                                    throw new RuntimeException(invalidEnumValueException);
                                }
                            }

                            // Fill up all positions with empty (null) values.
                            for (int i = 0; i < getPlate().getPlateLayout().getCapacity(); i++) {
                                getSelectedSamples().add(null);
                                getEmptyPositionIndices().add(i);
                            }
                            if (!clonedPlate.getSamplePlatePositions().isEmpty()) {
                                for (SamplePlatePosition samplePlatePosition : clonedPlate.getSamplePlatePositions()) {
                                    final SamplePlatePosition newSamplePlatePosition = new SamplePlatePosition(createChildChildSampleIfNecessary(samplePlatePosition.getSample()), getPlate(), samplePlatePosition.getPosition());
                                    initPlateHelper(newSamplePlatePosition);
                                }
                                getSelectedSamplesIds().remove(0L);
                                getSelectedSamplesIds().addAll(getCreatedSampleIdentifiersParentSamplesMap().values());
                                qcTypes.remove(null);
                                sampleForms.remove(null);
                            }
                        }
                    }
                }
            }
        }
    }

    private void initPlateHelper(SamplePlatePosition samplePlatePosition) {
        if (samplePlatePosition != null) {
            final Sample sample = samplePlatePosition.getSample();
            // -1 because position starts at 1 instead of 0.
            getSelectedSamples().set(samplePlatePosition.getPosition().intValue() - 1, sample);
            if (!sample.getType().equals(SampleTypeEnum.CONTROL_SAMPLE.getLabel())) {
                getSelectedSamplesIds().add(sample.getId());
            }
            getEmptyPositionIndices().remove(samplePlatePosition.getPosition().intValue() - 1);
            getInitialSampleIdSamplePlatePositionMap().put(sample.getId(), samplePlatePosition);
            types.add(sample.getSampleType().getName());
            getTypesNonCached().add(sample.getSampleType().getName());
            qcTypes.add(sample.getQualityControlType());
            sampleForms.add(sample.getSampleForm());
        }
    }

    public boolean isCloneModeSamplesNone() {
        return Constants.CLONE_MODE_SAMPLES_NONE.equals(getPlate().getCloneMode());
    }

    public boolean isGridView() {
        return isGridView;
    }

    public boolean isInitialMultiplexIdWrapAround() {
        return initialMultiplexIdWrapAround;
    }

    public boolean isLinkedMultiplexId() {
        return linkedMultiplexId;
    }

    public boolean isMultiplexIdAssignmentWrapAround() {
        return multiplexIdAssignmentWrapAround;
    }

    public boolean isSampleAssignmentPerRow() {
        return isSampleAssignmentPerRow;
    }

    public boolean isSavePlateHintRendered() {
        return savePlateHintRendered;
    }

    public boolean isSelectedMultiplexKitCombined() {
        return getSelectedMultiplexKit() != null && getSelectedMultiplexKit().isCombinedMultiplexId();
    }

    public boolean isShowAllColumns() {
        return showAllColumns;
    }

    public boolean isShowControlOnly() {
        return showControlOnly;
    }

    public boolean isShowOrphansOnly() {
        return showOrphansOnly;
    }

    public boolean isStartPositionsForMultiplexIdAssignmentWrapAround() {
        return startPositionsForMultiplexIdAssignmentWrapAround;
    }

    public void linkedMultiplexIdsChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setMultiplexIdsAssignmentAttribute((Boolean) event.getNewValue() ? Messages.get("multiplexIdAndMultiplexId2") : null);
        }
        multiplexIdsAssignmentAttributeChanged();
    }

    public void markSample(Sample sample) {
        long key = sample.isManaged() ? sample.getId() : getCreatedSampleIdentifiersParentSamplesMap().get(sample.hashCode());
        if (sample.isChecked()) {
            getMarkedSamples().put(key, sample);
        } else {
            getMarkedSamples().remove(key);
        }
    }

    public void multiplexIdsAssignmentAttributeChanged() {
        if (getSelectedMultiplexKit() != null) {
            getSelectedMultiplexKit().setOrderedEnabledMultiplexIds(null);
        }
        setSelectedInitialMultiplexIdForMultiplexIdAssignment(null);
    }

    public void onSampleDrop(DragDropEvent<Sample> event) {
        String dragId = event.getDragId();
        String dropId = event.getDropId();
        int dragIndex = Integer.parseInt(dragId.substring(dragId.indexOf("availableSamples:") + "availableSamples:".length(), dragId.indexOf(":samplePanel")));
        int dropIndex = Integer.parseInt(dropId.substring(dropId.indexOf("availableSamples:") + "availableSamples:".length(), dropId.indexOf(":samplePanel")));
        Collections.swap(getSelectedSamples(), dragIndex, dropIndex);
        if (!(getEmptyPositionIndices().contains(dropIndex) && getEmptyPositionIndices().contains(dragIndex))) {
            if (getEmptyPositionIndices().contains(dragIndex)) {
                getEmptyPositionIndices().remove(dragIndex);
                getEmptyPositionIndices().add(dropIndex);
            } else if (getEmptyPositionIndices().contains(dropIndex)) {
                getEmptyPositionIndices().remove(dropIndex);
                getEmptyPositionIndices().add(dragIndex);
            }
        }
    }

    public void prepareClonePlateModalDialog() {
        getPlate().setClonedPlateName(getPlate().getName());
        if (getPlate().getPlateType().getName().equals(Constants.PLATE_TYPE_USER_SUBMITTED_NAME)) {
            getPlate().setCloneMode(Constants.CLONE_MODE_SAMPLES_REFERENCE);
        }
    }

    public void prepareMultiplexIdsModalPanel(LinkedHashMap<Integer, Sample> aSelectedSamplesForMultiplexIdAssignment, boolean[] aMultiplexIdsAssignability, boolean aIsSampleAssignmentPerRow) {
        String requestUri = getRequestURI();
        if (requestUri != null && requestUri.contains("/plate/show.html")) {
            setSavePlateHintRendered(true);
        }

        // Note: aMultiplexIdsAssignability and aIsSampleAssignmentPerRow are parameters in case the modal panel should be used elsewhere.
        resetAssignMultiplexIdsModalPanel();
        setSelectedSamplesForMultiplexIdAssignment(aSelectedSamplesForMultiplexIdAssignment);

        // As the LinkedHashMap preserves the insertion order, the first entry represents the "first" sample position on the plate.
        setStartPositionForMultiplexIdAssignment(getSelectedSamplesForMultiplexIdAssignment().entrySet().iterator().next().getKey());
        setStartPositionsForMultiplexIdAssignment(new ArrayList<>());
        for (Map.Entry<Integer, Sample> selectedSamplesForMultiplexIdAssignmentEntry : getSelectedSamplesForMultiplexIdAssignment().entrySet()) {
            getStartPositionsForMultiplexIdAssignment().add(selectedSamplesForMultiplexIdAssignmentEntry.getKey());
        }

        for (Sample sample : getSelectedSamplesForMultiplexIdAssignment().values()) {
            // Reset the cached values for multiplexIdNameWithSequence and multiplexId2NameWithSequence.
            sample.setMultiplexIdNameWithSequence(null);
            sample.setMultiplexId2NameWithSequence(null);
        }
        setMultiplexIdsAssignability(aMultiplexIdsAssignability);
        setSampleAssignmentPerRow(aIsSampleAssignmentPerRow);
        if (getMultiplexIdsAssignability()[0] && getMultiplexIdsAssignability()[1] && isLinkedMultiplexId()) {
            setMultiplexIdsAssignmentAttribute(Messages.get("multiplexIdAndMultiplexId2"));
        } else if (!(getMultiplexIdsAssignability()[0] && getMultiplexIdsAssignability()[1])) {
            if (getMultiplexIdsAssignability()[0] && !getMultiplexIdsAssignability()[1]) {
                setMultiplexIdsAssignmentAttribute(Messages.get("multiplexId"));
                setLinkedMultiplexId(false);
            } else if (!getMultiplexIdsAssignability()[0] && getMultiplexIdsAssignability()[1]) {
                setMultiplexIdsAssignmentAttribute(Messages.get("multiplexId2"));
                setLinkedMultiplexId(false);
            }
        }
    }

    @Override
    public String remove() {
        plateService.remove(getPlate());
        facesMessageAdd(false);
        return getRedirectURLAfterRemove();
    }

    public void resetAssignMultiplexIdsModalPanel() {
        setMultiplexIdAssignmentWrapAround(false);
        setLinkedMultiplexId(true);
        setStartPositionForMultiplexIdAssignment(null);
        setStartPositionsForMultiplexIdAssignment(null);
        if (getSelectedSamplesForMultiplexIdAssignment() != null) {
            for (Sample sample : getSelectedSamplesForMultiplexIdAssignment().values()) {
                // Reset the cached values for multiplexIdNameWithSequence and multiplexId2NameWithSequence.
                sample.setMultiplexIdNameWithSequence(null);
                sample.setMultiplexId2NameWithSequence(null);
            }
        }
        setSelectedSamplesForMultiplexIdAssignment(null);
        if (getSelectedMultiplexKit() != null) {
            getSelectedMultiplexKit().setOrderedEnabledMultiplexIds(null);
        }
        setSelectedMultiplexKit(null);
        setSelectedInitialMultiplexIdForMultiplexIdAssignment(null);
        setMultiplexIdsAssignability(null);
        setMultiplexIdsAssignmentAttribute(null);
    }

    public void resetClonePlateModalDialog() {
        getPlate().setCloneMode(Constants.CLONE_MODE_SAMPLES_NONE);
        getPlate().setClonedPlateName(null);
        getPlate().setClonedPlateType(null);
        getPlate().setClonedQualityControlType(null);
    }

    public String rollbackStatus() {
        plateService.rollbackStatus(getPlate());
        getFacesMessagesManager().bufferWarningClear(Messages.get("statusRolledBack"));
        return getShowScreenRedirectURL();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = plateService.isValid(getPlate());
        if (validationErrorMsg.isEmpty()) {
            if (getPlate().getPlateLayout().hasOneRow()) {
                getPlate().setSampleAssignmentPerRow(true);
            }
            plateService.save(getPlate());
            return postSave(true, false);
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    @FindBugsSuppressWarnings("BX_UNBOXING_IMMEDIATELY_REBOXED")
    public void selectAllSamples() {
        if (UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID) != null) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
                .findComponent(String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID)));
            List<Sample> samples = dataTableHelper.getDataTableValues(dataTable);
            LinkedList<Integer> sortedIndices = sortIndices(getEmptyPositionIndices());
            Iterator<Integer> iterator = sortedIndices.iterator();
            for (Sample sample : samples) {
                if (!getSelectedSamplesIds().contains(sample.isManaged() ? sample.getId() : getCreatedSampleIdentifiersParentSamplesMap().get(sample.hashCode()))) {
                    if (getRemainingCapacity() > 0 && iterator.hasNext()) {
                        int position = iterator.next();

                        Sample selectedSample = createChildChildSampleIfNecessary(sample);
                        getSelectedSamples().set(position, selectedSample);
                        if (!sample.getType().equals(SampleTypeEnum.CONTROL_SAMPLE.getLabel())) {
                            getSelectedSamplesIds().add(sample.getId());
                        } else {
                            handleSelectedControlSample(sample, selectedSample);
                        }
                        iterator.remove();
                        sample.setChecked(false);
                        getTypesNonCached().add(sample.getSampleType().getName());
                    } else {
                        PrimeFaces.current().executeScript("PF('assignSamplesModalDialog').show();");
                        break;
                    }
                }
            }

            getEmptyPositionIndices().clear();
            getEmptyPositionIndices().addAll(sortedIndices);
            updateAddButtonColumnOfSampleSelectionTable();
        }
    }

    public void selectSample(Sample sample) {
        if (!getEmptyPositionIndices().isEmpty()) {
            sample.setChecked(false);
            LinkedList<Integer> sortedIndices = sortIndices(getEmptyPositionIndices());
            Sample selectedSample = createChildChildSampleIfNecessary(sample);
            getTypesNonCached().add(selectedSample.getSampleType().getName());
            getSelectedSamples().set(sortedIndices.getFirst(), selectedSample);
            if (!sample.getType().equals(SampleTypeEnum.CONTROL_SAMPLE.getLabel())) {
                getSelectedSamplesIds().add(sample.getId());
            } else {
                handleSelectedControlSample(sample, selectedSample);
            }
            sortedIndices.removeFirst();
            getEmptyPositionIndices().clear();
            getEmptyPositionIndices().addAll(sortedIndices);
        } else {
            PrimeFaces.current().executeScript("PF('assignSamplesModalDialog').show();");
        }
    }

    public void selectedMultiplexKitChanged() {
        setSelectedInitialMultiplexIdForMultiplexIdAssignment(null);
        setSelectedInitialCombinedMultiplexIdForMultiplexIdAssignment(null);
    }

    public void selectedMultiplexKitChangedListener(ValueChangeEvent event) {
        if (event.getNewValue() != null && ((MultiplexKit) event.getNewValue()).isCombinedMultiplexId()) {
            MultiplexKit newMultiplexKit = (MultiplexKit) event.getNewValue();
            List<MultiplexId> multiplexIds = multiplexIdService.getMultiplexIdsByMultiplexKitId(newMultiplexKit.getId());
            multiplexIds.sort(Comparator.comparing(MultiplexId::getId));
            List<MultiplexId> xAxis = new ArrayList<>();
            List<MultiplexId> yAxis = new ArrayList<>();
            for (MultiplexId multiplexId : multiplexIds) {
                if (multiplexId.getMultiplexKit().isCombinedMultiplexId()) {
                    (multiplexId.getCombinedXAxis() ? xAxis : yAxis).add(multiplexId);
                }
            }

            if (xAxis.size() == getPlate().getPlateLayout().getColumns()) {
                setLinkedMultiplexId(true);
                setMultiplexIdsAssignmentAttribute(Messages.get("multiplexIdAndMultiplexId2"));
                if (getCombinedMultiplexIdNames() != null) {
                    getCombinedMultiplexIdNames().clear();
                } else {
                    setCombinedMultiplexIdNames(new ArrayList<>());
                }
                if (getCombinedMultiplexIdNamesPrettyPrintMap() != null) {
                    getCombinedMultiplexIdNamesPrettyPrintMap().clear();
                } else {
                    setCombinedMultiplexIdNamesPrettyPrintMap(new HashMap<>());
                }
                if (getCombinedMultiplexIdsMap() != null) {
                    getCombinedMultiplexIdsMap().clear();
                } else {
                    setCombinedMultiplexIdsMap(new HashMap<>());
                }
                for (MultiplexId xMultiplexId : xAxis) {
                    for (MultiplexId yMultiplexId : yAxis) {
                        String combinedMultiplexIdName = xMultiplexId.getName() + yMultiplexId.getName();
                        getCombinedMultiplexIdNames().add(combinedMultiplexIdName);
                        getCombinedMultiplexIdsMap().put(combinedMultiplexIdName, new HashMap<>());
                        if (xMultiplexId.isMultiplexIdAssignableOnly() && yMultiplexId.isMultiplexId2AssignableOnly()) {
                            getCombinedMultiplexIdsMap().get(combinedMultiplexIdName).put(Messages.get("multiplexId"), xMultiplexId.getSequence());
                            getCombinedMultiplexIdsMap().get(combinedMultiplexIdName).put(Messages.get("multiplexId2"), yMultiplexId.getSequence());
                            getCombinedMultiplexIdNamesPrettyPrintMap().put(combinedMultiplexIdName, xMultiplexId.getName() + "-" + yMultiplexId.getName());
                        } else if (xMultiplexId.isMultiplexId2AssignableOnly() && yMultiplexId.isMultiplexIdAssignableOnly()) {
                            getCombinedMultiplexIdsMap().get(combinedMultiplexIdName).put(Messages.get("multiplexId2"), xMultiplexId.getSequence());
                            getCombinedMultiplexIdsMap().get(combinedMultiplexIdName).put(Messages.get("multiplexId"), yMultiplexId.getSequence());
                            getCombinedMultiplexIdNamesPrettyPrintMap().put(combinedMultiplexIdName, yMultiplexId.getName() + "-" + xMultiplexId.getName());
                        }
                    }
                }
            }
        }
    }

    public void setCombinedMultiplexIdNames(List<String> combinedMultiplexIdNames) {
        this.combinedMultiplexIdNames = combinedMultiplexIdNames;
    }

    public void setCombinedMultiplexIdNamesPrettyPrintMap(Map<String, String> combinedMultiplexIdNamesPrettyPrintMap) {
        this.combinedMultiplexIdNamesPrettyPrintMap = combinedMultiplexIdNamesPrettyPrintMap;
    }

    public void setCombinedMultiplexIdsMap(Map<String, Map<String, String>> combinedMultiplexIdsMap) {
        this.combinedMultiplexIdsMap = combinedMultiplexIdsMap;
    }

    public void setFilterQcPassed(Boolean filterQcPassed) {
        this.filterQcPassed = filterQcPassed;
    }

    public void setFilterUserDecision(Boolean filterUserDecision) {
        this.filterUserDecision = filterUserDecision;
    }

    public void setGridView(boolean gridView) {
        if (gridView) {
            getMarkedSamples().clear();
            for (Sample sample : getSelectedSamples()) {
                if (sample != null) {
                    sample.setChecked(false);
                }
            }
            getSelectedSamples().clear();
            getSelectedSamplesIds().clear();
            getEmptyPositionIndices().clear();
            getInitialSampleIdSamplePlatePositionMap().clear();
            types.clear();
            getTypesNonCached().clear();
            qcTypes.clear();
            sampleForms.clear();
            setShowAllColumns(false);
            setShowControlOnly(false);
            setShowOrphansOnly(false);
            setFilterQcPassed(null);
            setFilterUserDecision(Boolean.TRUE);
            init();
        }
        isGridView = gridView;
    }

    public void setInitialMultiplexIdWrapAround(boolean initialMultiplexIdWrapAround) {
        this.initialMultiplexIdWrapAround = initialMultiplexIdWrapAround;
    }

    public void setLinkedMultiplexId(boolean linkedMultiplexId) {
        this.linkedMultiplexId = linkedMultiplexId;
    }

    public void setMultiplexIdAssignmentWrapAround(boolean multiplexIdAssignmentWrapAround) {
        this.multiplexIdAssignmentWrapAround = multiplexIdAssignmentWrapAround;
        setInitialMultiplexIdWrapAround(multiplexIdAssignmentWrapAround);
        setStartPositionsForMultiplexIdAssignmentWrapAround(multiplexIdAssignmentWrapAround);
    }

    public void setMultiplexIdsAssignability(boolean[] multiplexIdsAssignability) {
        this.multiplexIdsAssignability = multiplexIdsAssignability != null ? multiplexIdsAssignability.clone() : null;
    }

    public void setMultiplexIdsAssignmentAttribute(String multiplexIdsAssignmentAttribute) {
        this.multiplexIdsAssignmentAttribute = multiplexIdsAssignmentAttribute;
    }

    public void setQualityControlType(SampleQCTypeEnum qualityControlType) {
        this.qualityControlType = qualityControlType;
    }

    public void setSampleAssignmentPerRow(boolean sampleAssignmentPerRow) {
        isSampleAssignmentPerRow = sampleAssignmentPerRow;
    }

    public void setSavePlateHintRendered(boolean savePlateHintRendered) {
        this.savePlateHintRendered = savePlateHintRendered;
    }

    public void setSelectedInitialCombinedMultiplexIdForMultiplexIdAssignment(String selectedInitialCombinedMultiplexIdForMultiplexIdAssignment) {
        this.selectedInitialCombinedMultiplexIdForMultiplexIdAssignment = selectedInitialCombinedMultiplexIdForMultiplexIdAssignment;
    }

    public void setSelectedInitialMultiplexIdForMultiplexIdAssignment(MultiplexId selectedInitialMultiplexIdForMultiplexIdAssignment) {
        this.selectedInitialMultiplexIdForMultiplexIdAssignment = selectedInitialMultiplexIdForMultiplexIdAssignment;
    }

    public void setSelectedMultiplexKit(MultiplexKit selectedMultiplexKit) {
        this.selectedMultiplexKit = selectedMultiplexKit;
    }

    public void setSelectedSamplesForMultiplexIdAssignment(LinkedHashMap<Integer, Sample> selectedSamplesForMultiplexIdAssignment) {
        this.selectedSamplesForMultiplexIdAssignment = selectedSamplesForMultiplexIdAssignment;
    }

    public void setShowAllColumns(boolean showAllColumns) {
        this.showAllColumns = showAllColumns;
    }

    public void setShowControlOnly(boolean showControlOnly) {
        this.showControlOnly = showControlOnly;
    }

    public void setShowOrphansOnly(boolean showOrphansOnly) {
        this.showOrphansOnly = showOrphansOnly;
    }

    public void setSinglePlateContainer(Container singlePlateContainer) {
        this.singlePlateContainer = singlePlateContainer;
    }

    public void setStartPositionForMultiplexIdAssignment(Integer startPositionForMultiplexIdAssignment) {
        this.startPositionForMultiplexIdAssignment = startPositionForMultiplexIdAssignment;
    }

    public void setStartPositionsForMultiplexIdAssignment(List<Integer> startPositionsForMultiplexIdAssignment) {
        this.startPositionsForMultiplexIdAssignment = startPositionsForMultiplexIdAssignment;
    }

    public void setStartPositionsForMultiplexIdAssignmentWrapAround(boolean startPositionsForMultiplexIdAssignmentWrapAround) {
        this.startPositionsForMultiplexIdAssignmentWrapAround = startPositionsForMultiplexIdAssignmentWrapAround;
    }

    public void setTileAttribute(SampleAttributeEnum tileAttribute) {
        this.tileAttribute = tileAttribute;
    }

    public void shiftEmptyPositions() {
        Sample[] shifted = new Sample[getSelectedSamples().size()];

        int allCounter = 0;
        int notNullCounter = 0;
        for (int nextIndex : sortIndices(Arrays.stream(IntStream.rangeClosed(0, getSelectedSamples().size() - 1).toArray()).boxed().collect(Collectors.toSet()))) {
            if (getSelectedSamples().get(nextIndex) != null) {
                shifted[notNullCounter] = getSelectedSamples().get(nextIndex);
                notNullCounter++;
            }
            allCounter++;
            if (!(allCounter < shifted.length && notNullCounter < shifted.length)) {
                break;
            }
        }

        getEmptyPositionIndices().clear();
        for (int i = 0; i < getPlate().getPlateLayout().getCapacity(); i++) {
            getEmptyPositionIndices().add(i);
        }

        Iterator<Integer> sortedIndicesIterator = sortIndices(getEmptyPositionIndices()).iterator();
        for (Sample sample : shifted) {
            if (sortedIndicesIterator.hasNext()) {
                int nextIndex = sortedIndicesIterator.next();
                getSelectedSamples().set(nextIndex, sample);
                if (sample != null) {
                    getEmptyPositionIndices().remove(nextIndex);
                }
                sortedIndicesIterator.remove();
            }
        }
    }

    private LinkedList<Integer> sortIndices(Set<Integer> indices) {
        LinkedList<Integer> sortedIndices = new LinkedList<>(indices);
        if (getPlate() != null && getPlate().getPlateLayout() != null) {
            sortedIndices.sort(PlateLayout.getSamplePlatePositionAssignmentComparator(getPlate().getPlateLayout().getColumns(), getPlate().isSampleAssignmentPerRow()));
        }
        return sortedIndices;
    }

    private void updateAddButtonColumnOfSampleSelectionTable() {
        /*
         * Workaround for updating the 'Add' button(s) in the datatable after the removal of a sample from the plate.
         * Updating the whole table after the removal of a sample from the plate, iff any drag/drop operation was made prior, leads to an JavaScript error in PF9.
         * Additionally, only updating the column instead of the whole table is more performant.
         */
        if (UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID) != null) {
            dataTableHelper
                .updateColumn((String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID), Constants.ADD + Constants.BUTTON, false);
        }
    }
}