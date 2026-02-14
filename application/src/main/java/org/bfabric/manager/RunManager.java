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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Run;
import org.bfabric.entity.RunUnit;
import org.bfabric.entity.RunUnitType;
import org.bfabric.entity.Sample;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.RunService;
import org.bfabric.service.SampleService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.MultiplexIdConflictRecord;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;

@MeasureCalls
@Named
@ViewScoped
public class RunManager extends AbstractEntityManager<Run> {

    private static final long serialVersionUID = 1;

    private final Map<Long, Sample> selectedSamplesMap = new HashMap<>();

    @Inject
    protected ConfManager confManager;

    @Inject
    InstrumentReservationService instrumentReservationService;

    @Param
    private Long containerId;

    private boolean excludeOrderItemSamples = true;

    @Inject
    private InstrumentService instrumentService;

    private Set<Sample> markedSourceSamples = new HashSet<>();

    private Map<Sample, Boolean> markedSourceSamplesMap = new HashMap<>();

    private Set<Sample> markedTargetSamples = new HashSet<>();

    private Map<Sample, Boolean> markedTargetSamplesMap = new HashMap<>();

    private Map<Integer, Map<Sample, Boolean>> markedTargetSamplesPerLaneMap = new HashMap<>();

    private List<MultiplexIdConflictRecord> multiplexIdConflictRecords = new ArrayList<>();

    private Map<String, Object> multiplexIdsCorrectnessErrorMap = new HashMap<>();

    @Inject
    private RunService runService;

    @Inject
    private SampleService sampleService;

    private Set<Sample> samplesWithIncompleteMultiplexIds = new HashSet<>();

    private Set<Dataset> selectedDatasets = new HashSet<>();

    private Set<Integer> selectedLanePositions = new HashSet<>();

    private Run selectedRunForSampleAssignment;

    private String selectedSampleType;

    private Set<Sample> selectedSamples = new HashSet<>();

    private boolean selectionModePageOnly = true;

    private boolean showAllColumns = false;

    private boolean showAllSamples = true;

    private boolean showSamplesLaneSeparated = false;

    private LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

    public RunManager() {
        super(Run.class);
    }

    public void addDatasetToSelection(Dataset dataset) {
        getSelectedDatasets().add(dataset);
    }

    public void addMarkedSamples() {
        for (Sample sample : getMarkedSourceSamples()) {
            getSelectedSamples().add(sample);
            getSelectedSamplesMap().put(sample.getId(), sample);
            if (!getRun().getSamplesRunUnitLanesPositions().containsKey(sample)) {
                Map<Integer, Boolean> runUnitLanesPositions = new HashMap<>();
                for (Integer position : getRun().getLanePositions()) {
                    runUnitLanesPositions.put(position, Boolean.FALSE);
                }
                getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.FALSE);
                getRun().getSamplesRunUnitLanesPositions().put(sample, runUnitLanesPositions);
                if (!getSelectedLanePositions().isEmpty()) {
                    for (Integer lanePosition : getSelectedLanePositions()) {
                        getRun().getSamplesRunUnitLanesPositions().get(sample).put(lanePosition, Boolean.TRUE);
                    }
                    if (getRun().getLanePositions().size() == getSelectedLanePositions().size()) {
                        getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.TRUE);
                    }
                } else if (isShowSamplesLaneSeparated() || getRun().getLanePositions().size() == 1) {
                    // If no lane is selected, add the sample(s) to the first lane if there is only one lane or the lanes are shown separately.
                    getRun().getSamplesRunUnitLanesPositions().get(sample).put(getRun().getLanePositions().iterator().next(), Boolean.TRUE);
                    if (getRun().getLanePositions().size() == 1) {
                        getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.TRUE);
                    }
                }
                if (!getRun().getSamplesRunUnitLanesPositionsMap().containsKey(sample)) {
                    getRun().getSamplesRunUnitLanesPositionsMap().put(sample, new HashSet<>());
                }
                for (Map.Entry<Integer, Boolean> runUnitLanesPositionsEntry : getRun().getSamplesRunUnitLanesPositions().get(sample).entrySet()) {
                    if (runUnitLanesPositionsEntry.getValue() != null && runUnitLanesPositionsEntry.getValue()) {
                        getRun().getRunUnitLanesPositionsSamples().get(runUnitLanesPositionsEntry.getKey()).add(sample);
                        getRun().getSamplesRunUnitLanesPositionsMap().get(sample).add(runUnitLanesPositionsEntry.getKey());
                    }
                }
            }
        }
        emptyMarkedSamplesCollections();
        getSelectedLanePositions().clear();
    }

    public void addMarkedSamplesToLane(Integer lanePosition) {
        if (lanePosition != null) {
            getSelectedLanePositions().clear();
            getSelectedLanePositions().add(lanePosition);
        }
        addMarkedSamples();
    }

    public String assignDatasets() {
        runService.assignDatasets(getRun(), getSelectedDatasets());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("datasets");
    }

    public String assignSamples() {
        getValidationErrorMsg().clear();
        getValidationErrorMsg().putAll(runService.isValidSamplesAssignment(getRun(), getRun().getSamplesRunUnitLanesPositions(), getSelectedSamples()));
        // Multiplex IDs correctness/uniqueness check is not required iff demultiplexingRequired=false or there is only one sample per run (physicalSeparation=false) or per lane (physicalSeparation=true)
        if (runService.isMultiplexIdsCorrectnessUniquenessCheckRequired(getRun(), getRun().getSamplesRunUnitLanesPositions()) && getValidationErrorMsg().isEmpty()) {
            setMultiplexIdsCorrectnessErrorMap(sampleService.getMultiplexIdsCorrectnessErrorMap(getSelectedSamples(), false, true, Constants.MULTIPLEX_ID_CHECK_ADVANCED));
            getSamplesWithIncompleteMultiplexIds().clear();
            if (!getMultiplexIdsCorrectnessErrorMap().isEmpty()) {
                if (getMultiplexIdsCorrectnessErrorMap().containsKey(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)) {
                    getSamplesWithIncompleteMultiplexIds().addAll((Set<Sample>) getMultiplexIdsCorrectnessErrorMap().get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY));
                    getValidationErrorMsg().put(Constants.EDIT + ":multiplexIdsValidErrorMessage", "Incomplete multiplexIds for samples:");
                } else if (getMultiplexIdsCorrectnessErrorMap().containsKey(Constants.MULTIPLEX_ID_MISMATCH_KEY)) {
                    getValidationErrorMsg().put(Constants.EDIT + ":multiplexIdsValidErrorMessage", (String) getMultiplexIdsCorrectnessErrorMap().get(Constants.MULTIPLEX_ID_MISMATCH_KEY));
                } else if (getMultiplexIdsCorrectnessErrorMap().containsKey(Constants.MULTIPLEX_ID_MIXED_KEY)) {
                    getValidationErrorMsg().put(Constants.EDIT + ":multiplexIdsValidErrorMessage", (String) getMultiplexIdsCorrectnessErrorMap().get(Constants.MULTIPLEX_ID_MIXED_KEY));
                }
            }
            getMultiplexIdConflictRecords().clear();
            if (getValidationErrorMsg().isEmpty()) {
                // At this point in time, the multiplex ids are correct
                getMultiplexIdConflictRecords().addAll(runService.checkMultiplexIdUniqueness(getRun(), getRun().getSamplesRunUnitLanesPositions(), Constants.MULTIPLEX_ID_CHECK_ADVANCED));
                if (!getMultiplexIdConflictRecords().isEmpty()) {
                    getValidationErrorMsg().put(Constants.EDIT + ":multiplexIdsValidErrorMessage", "Not unique multiplexIds for samples:");
                }
            }
        }
        if (getValidationErrorMsg().isEmpty()) {
            getRun().getSamplesRunUnitLanesPositions().keySet().removeIf(sample -> !getSelectedSamples().contains(sample));
            setCreated(!isManaged());
            runService.assignSamples(getRun(), getRun().getSamplesRunUnitLanesPositions());
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyAssigned") + " " + Messages.get("samples"));
            return getShowScreenRedirectURL("samples");
        }
        handleValidationErrors(getValidationErrorMsg(), getDataTablesForValidation());
        return null;
    }

    public String changeStatus(StatusEnum statusEnum) {
        printFacesMessagesClear(runService.changeStatus(getRun(), statusEnum));
        if (getRun().getStatus().equals(StatusEnum.PROCESSING)) {
            InstrumentReservation instrumentReservation = instrumentReservationService.getInstrumentReservationByIntervalAndInstrument(new LocalDateTimeInterval(LocalDateTime.now(), LocalDateTime.now()), getRun().getInstrument());
            if (instrumentReservation == null || !getRun().equals(instrumentReservation.getRun())) {
                getFacesMessagesManager().bufferWarning(Messages.get("instrumentNotBookedForRunHint") + " " + getRun().getId());
                return "/instrumentschedule/show.xhtml?instrumentId=" + getRun().getInstrument().getId() + "&runId=" + getRun().getId() + "&faces-redirect=true";
            }
        }
        return getShowScreenRedirectURL();
    }

    private void emptyAllSamplesCollections() {
        getRun().getSamplesRunUnitLanesAll().clear();
        getRun().getSamplesRunUnitLanesPositions().clear();
        getSelectedSamples().clear();
        getSelectedSamplesMap().clear();
        emptyMarkedSamplesCollections();
    }

    private void emptyMarkedSamplesCollections() {
        getMarkedSourceSamples().clear();
        getMarkedSourceSamplesMap().clear();
        getMarkedTargetSamples().clear();
        getMarkedTargetSamplesMap().clear();
    }

    public String getAssignSamplesTargetColumnsProcessAndUpdate(Integer lanePosition) {
        StringBuilder components = new StringBuilder("@this");
        components.append(", ").append(Constants.EDIT).append(":").append(Constants.TARGET).append(Constants.SAMPLE).append(Constants.BUTTONS);
        if (lanePosition != null && isShowSamplesLaneSeparated()) {
            components.append(", ").append(Constants.EDIT).append(":").append(Constants.TARGET).append(Constants.SAMPLE).append(Constants.LANE).append(lanePosition).append(Constants.BUTTONS);
        }
        return components.toString();
    }

    public Long getContainerId() {
        return containerId;
    }

    public Set<DataTable> getDataTablesForValidation() {
        Set<DataTable> tables = new HashSet<>();
        DataTable targetSampleTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(Constants.EDIT_TARGET_SAMPLE_TABLE);
        if (targetSampleTable != null) {
            tables.add(targetSampleTable);
        }
        return tables;
    }

    public Set<Sample> getMarkedSourceSamples() {
        return markedSourceSamples;
    }

    public Map<Sample, Boolean> getMarkedSourceSamplesMap() {
        return markedSourceSamplesMap;
    }

    public Set<Sample> getMarkedTargetSamples() {
        return markedTargetSamples;
    }

    public Map<Sample, Boolean> getMarkedTargetSamplesMap() {
        return markedTargetSamplesMap;
    }

    public Map<Integer, Map<Sample, Boolean>> getMarkedTargetSamplesPerLaneMap() {
        return markedTargetSamplesPerLaneMap;
    }

    public List<MultiplexIdConflictRecord> getMultiplexIdConflictRecords() {
        return multiplexIdConflictRecords;
    }

    public Map<String, Object> getMultiplexIdsCorrectnessErrorMap() {
        return multiplexIdsCorrectnessErrorMap;
    }

    public Collection<String> getPotentialSelectedSampleTypes() {
        if (Constants.ILLUMINA.equals(getSelectedSampleType())) {
            return Arrays.asList(SampleTypeEnum.ILLUMINA_LIBRARY.getLabel(), SampleTypeEnum.ILLUMINA_MULTIPLEXED.getLabel());
        }
        if (Constants.NANOPORE.equals(getSelectedSampleType())) {
            return Arrays.asList(SampleTypeEnum.NANOPORE_LIBRARY.getLabel(), SampleTypeEnum.NANOPORE_MULTIPLEXED.getLabel(),
                SampleTypeEnum.ONT_READY_MADE_LIBRARY.getLabel(), SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED.getLabel());
        }
        if (Constants.PACBIO.equals(getSelectedSampleType())) {
            return Arrays.asList(SampleTypeEnum.PACBIO_LIBRARY.getLabel(), SampleTypeEnum.PACBIO_MULTIPLEXED.getLabel());
        }
        if (Constants.MS_SAMPLE.equals(getSelectedSampleType())) {
            return Arrays.asList(SampleTypeEnum.MS_SAMPLE_LABELED.getLabel(), SampleTypeEnum.MS_SAMPLE_MULTIPLEXED.getLabel());
        }
        return null;
    }

    @Override
    public String getRedirectURLAfterSave() {
        return getRefererURL() != null && getRefererURL().contains("run/show") && getRefererURL().contains("tab=samples") ? createRedirectShowScreenURL(getRun(), "samples", null) : super
            .getRedirectURLAfterSave();
    }

    public List<Instrument> getResultListRunEnabledIncludingFiltered(String filterString) {
        return instrumentService.getResultListRunEnabledIncludingFiltered(filterString, getRun().getInstrument());
    }

    @Produces
    @Named("run")
    public Run getRun() {
        return getInstance();
    }

    public Set<Sample> getSamplesWithIncompleteMultiplexIds() {
        return samplesWithIncompleteMultiplexIds;
    }

    public List<Sample> getSamplesWithIncompleteMultiplexIdsAsList() {
        return samplesWithIncompleteMultiplexIds.stream().sorted(Comparator.comparingLong(Sample::getId)).collect(Collectors.toList());
    }

    public Set<Dataset> getSelectedDatasets() {
        return selectedDatasets;
    }

    public Set<Integer> getSelectedLanePositions() {
        return selectedLanePositions;
    }

    public Run getSelectedRunForSampleAssignment() {
        return selectedRunForSampleAssignment;
    }

    public String getSelectedSampleType() {
        return selectedSampleType;
    }

    public Set<Sample> getSelectedSamples() {
        return selectedSamples;
    }

    public List<Sample> getSelectedSamplesAsList() {
        return CollectionHelper.asList(getSelectedSamples());
    }

    public Map<Long, Sample> getSelectedSamplesMap() {
        return selectedSamplesMap;
    }

    public LinkedHashMap<String, String> getValidationErrorMsg() {
        return validationErrorMsg;
    }

    public void handleValidationErrors(Object... arguments) {
        getFacesMessagesManager().printValidationErrors(getValidationErrorMsg(), arguments);
        if (getOldRefererURL() != null) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("oldRefererURL", getOldRefererURL());
        }
    }

    public void handleValidationErrors(LinkedHashMap<String, String> aValidationErrorMsg, Object... arguments) {
        getFacesMessagesManager().printValidationErrors(aValidationErrorMsg, arguments);
        if (getOldRefererURL() != null) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("oldRefererURL", getOldRefererURL());
        }
    }

    public boolean hasLaneMarkedSample(Integer lanePosition) {
        if (!isShowSamplesLaneSeparated() || lanePosition == null) {
            return false;
        }

        if (getMarkedTargetSamplesPerLaneMap().get(lanePosition) != null) {
            for (Boolean entry : getMarkedTargetSamplesPerLaneMap().get(lanePosition).values()) {
                if (entry != null && entry) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getRun() != null) {
            if (!isManaged()) {
                if (!isCloned()) {
                    getRun().setSupervisor(getCurrentUser());
                }
            } else {
                // Map samples of on run type to their respective labeled / multiplexed samples.
                for (Sample sample : getRun().mapSamplesOfOnRunType(getRun().getSamples())) {
                    getSelectedSamples().add(sample);
                    getSelectedSamplesMap().put(sample.getId(), sample);
                }
                getSelectedDatasets().addAll(getRun().getDatasets());
                setSelectedSampleType(Constants.ILLUMINA);
                if (!getSelectedSamples().isEmpty()) {
                    Sample sample = getSelectedSamples().iterator().next();
                    if (SampleTypeEnum.NANOPORE_LIBRARY.getLabel().equals(sample.getType()) || SampleTypeEnum.NANOPORE_MULTIPLEXED.getLabel()
                        .equals(sample.getType()) || SampleTypeEnum.ONT_READY_MADE_LIBRARY.getLabel().equals(sample.getType()) || SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED.getLabel()
                        .equals(sample.getType())) {
                        setSelectedSampleType(Constants.NANOPORE);
                    } else if (SampleTypeEnum.PACBIO_LIBRARY.getLabel().equals(sample.getType()) || SampleTypeEnum.PACBIO_MULTIPLEXED.getLabel().equals(sample.getType())) {
                        setSelectedSampleType(Constants.PACBIO);
                    } else if (SampleTypeEnum.MS_SAMPLE_LABELED.getLabel().equals(sample.getType()) || SampleTypeEnum.MS_SAMPLE_MULTIPLEXED.getLabel().equals(sample.getType())) {
                        setSelectedSampleType(Constants.MS_SAMPLE);
                    }
                }
            }

            if (getRun().getRunUnit() != null) {
                // If the run unit and its respective lanes are already set.
                if (getRun().getRunUnit().getRunUnitLanes() != null && !isCloned()) {
                    getRun().initializeRunUnitLanes(true);
                    for (Integer lanePosition : getRun().getLanePositions()) {
                        getMarkedTargetSamplesPerLaneMap().put(lanePosition, new HashMap<>());
                    }
                }
            } else {
                if (getRun().getInstrument() == null || !getRun().getInstrument().getRunUnitTypes().isEmpty()) {
                    // If the run has no run unit set yet, e.g., when assigning samples from an empty run.
                    getRun().setRunUnit(new RunUnit(null, getRun()));
                }
            }
            setShowSamplesLaneSeparated(getCurrentUser().isShowSamplesLaneSeparated());
            if (getRun().getRunUnit() != null) {
                getRun().getRunUnit().setShowSamplesLaneSeparated(getCurrentUser().isShowSamplesLaneSeparated());
            }
        }
    }

    public void instrumentReadConfigurationChanged(ValueChangeEvent event) {
        emptyAllSamplesCollections();
    }

    public void instrumentSelectListener(SelectEvent<Instrument> event) {
        emptyAllSamplesCollections();
        getRun().setOldRunUnit(getRun().getRunUnit());
        Instrument instrument = event.getObject();
        getRun().setInstrumentReadConfiguration(null);
        if (instrument == null) {
            getRun().setRunUnit(null);
        } else {
            getRun().setRunUnit(new RunUnit());
        }
    }

    public boolean isExcludeOrderItemSamples() {
        return excludeOrderItemSamples;
    }

    public boolean isSelectionModePageOnly() {
        return selectionModePageOnly;
    }

    public boolean isShowAllColumns() {
        return showAllColumns;
    }

    public boolean isShowAllSamples() {
        return showAllSamples;
    }

    public boolean isShowSamplesLaneSeparated() {
        return showSamplesLaneSeparated;
    }

    public void lanePositionAllChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            final DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
            final String rowKeyId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("rowKeyId"));
            if (dataTable != null && rowKeyId != null && getSelectedSamplesMap().containsKey(Long.parseLong(rowKeyId))) {
                final int rowIndex = Integer.parseInt(((UIInput) event.getSource()).getClientId().split(":")[2]);
                final Sample sample = getSelectedSamplesMap().get(Long.parseLong(rowKeyId));
                for (Integer lanePosition : getRun().getLanePositions()) {
                    getRun().getSamplesRunUnitLanesPositions().get(sample).put(lanePosition, (Boolean) event.getNewValue());
                    dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + lanePosition);
                    getValidationErrorMsg().remove(StringHelper.createRowMessageComponentForInput(sample.getRowKeyIdAsString(), Constants.CHECK_BOX_POSITION_COLUMN + lanePosition));
                    dataTableHelper.updateCellAndMessage(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + lanePosition, rowIndex);
                }
                if (!getValidationErrorMsg().isEmpty()) {
                    handleValidationErrors(getValidationErrorMsg(), getDataTablesForValidation());
                }
                getRun().getSamplesRunUnitLanesAll().put(sample, (Boolean) event.getNewValue());
                dataTableHelper.updateCell(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL, rowIndex);
                dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL);
            }
        }
    }

    public void lanePositionAllChangedAll(boolean isSelect) {
        DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
        if (dataTable != null) {
            List<Sample> dataList = isSelectionModePageOnly() ? dataTableHelper.getDataTableValuesPageOnly(dataTable) : dataTableHelper.getDataTableValues(dataTable);
            for (Sample sample : dataList) {
                if (getRun().getSamplesRunUnitLanesPositions().containsKey(sample)) {
                    for (Integer lanePosition : getRun().getLanePositions()) {
                        getRun().getSamplesRunUnitLanesPositions().get(sample).put(lanePosition, isSelect);
                        dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + lanePosition);
                        dataTableHelper.updateColumn(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + lanePosition);
                    }
                    getValidationErrorMsg().clear();
                    getRun().getSamplesRunUnitLanesAll().put(sample, isSelect);
                    dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL);
                    dataTableHelper.updateColumn(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL);
                }
            }
        }
    }

    public void lanePositionChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            final DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
            final String lanePosition = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("lanePosition"));
            final String rowKeyId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("rowKeyId"));
            if (lanePosition != null && dataTable != null && rowKeyId != null && getSelectedSamplesMap().containsKey(Long.parseLong(rowKeyId))) {
                final int rowIndex = Integer.parseInt(((UIInput) event.getSource()).getClientId().split(":")[2]);
                final Sample sample = getSelectedSamplesMap().get(Long.parseLong(rowKeyId));
                if (!(Boolean) event.getNewValue()) {
                    getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.FALSE);
                } else {
                    getRun().getSamplesRunUnitLanesPositions().get(sample).put(Integer.parseInt(lanePosition), Boolean.TRUE);
                    if (CollectionHelper.isCollectionAllTrue(getRun().getSamplesRunUnitLanesPositions().get(sample).values())) {
                        getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.TRUE);
                    }
                    for (Integer position : getRun().getSamplesRunUnitLanesPositions().get(sample).keySet()) {
                        if (getValidationErrorMsg().remove(StringHelper.createRowMessageComponentForInput(sample.getRowKeyIdAsString(), Constants.CHECK_BOX_POSITION_COLUMN + position)) != null) {
                            dataTableHelper.updateCellAndMessage(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + position, rowIndex);
                        }
                    }
                }
                if (!getValidationErrorMsg().isEmpty()) {
                    handleValidationErrors(getValidationErrorMsg(), getDataTablesForValidation());
                }
                dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL);
                dataTableHelper.updateCell(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL, rowIndex);
            }
            dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + lanePosition);
        }
    }

    public void lanePositionChangedAll(boolean isSelect, Integer lanePosition) {
        DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
        if (dataTable != null && lanePosition != null) {
            List<Sample> dataList = isSelectionModePageOnly() ? dataTableHelper.getDataTableValuesPageOnly(dataTable) : dataTableHelper.getDataTableValues(dataTable);
            for (Sample sample : dataList) {
                if (getRun().getSamplesRunUnitLanesPositions().containsKey(sample)) {
                    getRun().getSamplesRunUnitLanesPositions().get(sample).put(lanePosition, isSelect);
                    if (isSelect) {
                        if (CollectionHelper.isCollectionAllTrue(getRun().getSamplesRunUnitLanesPositions().get(sample).values())) {
                            getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.TRUE);
                        }
                        for (Integer position : getRun().getSamplesRunUnitLanesPositions().get(sample).keySet()) {
                            getValidationErrorMsg().remove(StringHelper.createRowMessageComponentForInput(sample.getRowKeyIdAsString(), Constants.CHECK_BOX_POSITION_COLUMN + position));
                        }
                    } else {
                        getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.FALSE);
                    }
                }
            }
            if (!getValidationErrorMsg().isEmpty()) {
                handleValidationErrors(getValidationErrorMsg(), getDataTablesForValidation());
            }
            dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + lanePosition);
            dataTableHelper.updateColumn(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN + lanePosition);
            dataTableHelper.updateColumn(Constants.EDIT_SOURCE_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL);
            dataTableHelper.updateColumn(Constants.EDIT_TARGET_SAMPLE_TABLE, Constants.CHECK_BOX_POSITION_COLUMN_ALL);
        }
    }

    public void markSample(Sample sample, boolean isSelect, boolean isSelectionTable, Integer lanePosition) {
        if (isSelect) {
            if (isSelectionTable) {
                getMarkedSourceSamples().add(sample);
                getMarkedSourceSamplesMap().put(sample, Boolean.TRUE);
            } else {
                if (lanePosition == null) {
                    getMarkedTargetSamples().add(sample);
                    getMarkedTargetSamplesMap().put(sample, Boolean.TRUE);
                } else {
                    if (!getMarkedTargetSamplesPerLaneMap().containsKey(lanePosition)) {
                        getMarkedTargetSamplesPerLaneMap().put(lanePosition, new HashMap<>());
                    }
                    getMarkedTargetSamplesPerLaneMap().get(lanePosition).put(sample, Boolean.TRUE);
                }
            }
        } else {
            if (isSelectionTable) {
                getMarkedSourceSamples().remove(sample);
                getMarkedSourceSamplesMap().remove(sample);
            } else {
                if (lanePosition == null) {
                    getMarkedTargetSamples().remove(sample);
                    getMarkedTargetSamplesMap().remove(sample);
                } else {
                    getMarkedTargetSamplesPerLaneMap().get(lanePosition).remove(sample);
                }
            }
        }
        if (getMarkedSourceSamples().isEmpty()) {
            getSelectedLanePositions().clear();
        }
    }

    public void markSamplesForSelection(boolean isSelect, boolean isSelectionTable) {
        String tableId = Constants.EDIT + ":" + (isSelectionTable ? Constants.SOURCE : Constants.TARGET) + Constants.SAMPLE + Constants.TABLE;
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableId);
        if (dataTable != null) {
            List<Sample> dataList = isSelectionModePageOnly() ? dataTableHelper.getDataTableValuesPageOnly(dataTable) : dataTableHelper.getDataTableValues(dataTable);
            for (Sample sample : dataList) {
                if (!getSelectedSamples().contains(sample) || !isSelectionTable) {
                    // Only select/deselect samples which are not already assigned to the run.
                    markSample(sample, isSelect, isSelectionTable, null);
                }
            }
            dataTableHelper.updateColumn(tableId, Constants.CHECK_BOX + (isSelectionTable ? Constants.SOURCE : Constants.TARGET) + Constants.SAMPLE);
        }
    }

    public void pageListener() {
        if (!getValidationErrorMsg().isEmpty() && UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID) != null) {
            String tableClientId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID));
            final DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableClientId);
            boolean lazy = dataTable.isLazy();
            final List<Sample> dataList = dataTableHelper.getDataTableValues(dataTable);
            final int lo = lazy ? 0 : isSelectionModePageOnly() ? dataTable.getPage() * dataTable.getRowsToRender() : 0;
            final int hi = lazy ? dataList.size() : Math.min(lo + dataTable.getRowsToRender(), dataList.size());
            for (int i = lo; i < hi; i++) {
                String rowKeyId = dataList.get(i).getRowKeyIdAsString();
                for (Integer position : getRun().getSamplesRunUnitLanesPositions().get(dataList.get(i)).keySet()) {
                    String rowMessageComponentForInput = StringHelper.createRowMessageComponentForInput(rowKeyId, Constants.CHECK_BOX_POSITION_COLUMN + position);
                    String rowMessageComponentIdForInput = StringHelper.getComponentIdFromRowMessageComponent(rowMessageComponentForInput);
                    if (getValidationErrorMsg().containsKey(rowMessageComponentForInput)) {
                        getFacesMessagesManager().validationError(tableClientId + ":" + i + ":" + rowMessageComponentIdForInput, getValidationErrorMsg().get(rowMessageComponentForInput));
                        dataTableHelper.updateCellAndMessage(tableClientId, rowMessageComponentIdForInput.replace(Constants.INPUT, Constants.EMPTY_STRING), i);
                    }
                }
            }
        }
    }

    @Override
    public String remove() {
        final String entityName = getRun().toString();
        runService.remove(getRun());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted") + " " + entityName);
        return getRedirectURLAfterRemove();
    }

    public void removeAllSamples() {
        emptyAllSamplesCollections();
        getSamplesWithIncompleteMultiplexIds().clear();
        getMultiplexIdConflictRecords().clear();
    }

    public void removeAllSamplesFromLane(Integer lanePosition) {
        if (lanePosition != null) {
            for (Map.Entry<Sample, Set<Integer>> entry : getRun().getSamplesRunUnitLanesPositionsMap().entrySet()) {
                if (entry.getValue() != null && entry.getValue().contains(lanePosition)) {
                    removeSampleFromLane(entry.getKey(), lanePosition);
                }
            }
            getMarkedTargetSamplesPerLaneMap().get(lanePosition).clear();
            getSelectedLanePositions().clear();
            getSamplesWithIncompleteMultiplexIds().clear();
        }
    }

    public void removeMarkedSamples() {
        for (Sample sample : getMarkedTargetSamples()) {
            getRun().getSamplesRunUnitLanesAll().remove(sample);
            getRun().getSamplesRunUnitLanesPositions().remove(sample);
            getSelectedSamples().remove(sample);
            getSelectedSamplesMap().remove(sample.getId());
            getSamplesWithIncompleteMultiplexIds().remove(sample);
        }
        emptyMarkedSamplesCollections();
        getMultiplexIdConflictRecords().clear();
        getSelectedLanePositions().clear();
    }

    public void removeMarkedSamplesFromLane(Integer lanePosition) {
        if (lanePosition != null) {
            for (Map.Entry<Sample, Boolean> entry : getMarkedTargetSamplesPerLaneMap().get(lanePosition).entrySet()) {
                if (entry.getValue() != null && entry.getValue()) {
                    removeSampleFromLane(entry.getKey(), lanePosition);
                }
            }
            getMarkedTargetSamplesPerLaneMap().get(lanePosition).clear();
            getSelectedLanePositions().clear();
        }
    }

    private void removeSampleFromLane(Sample sample, Integer lanePosition) {
        if (sample != null && lanePosition != null && getRun().getSamplesRunUnitLanesPositionsMap().get(sample) != null) {
            if (getRun().getSamplesRunUnitLanesPositionsMap().get(sample).size() == 1 && getRun().getSamplesRunUnitLanesPositionsMap().get(sample).contains(lanePosition)) {
                getRun().getSamplesRunUnitLanesAll().remove(sample);
                getRun().getSamplesRunUnitLanesPositions().remove(sample);
                getSelectedSamples().remove(sample);
                getSelectedSamplesMap().remove(sample.getId());
                getSamplesWithIncompleteMultiplexIds().remove(sample);
            } else {
                if (getRun().getSamplesRunUnitLanesPositionsMap().get(sample).contains(lanePosition)) {
                    if (getRun().getSamplesRunUnitLanesAll().get(sample)) {
                        getRun().getSamplesRunUnitLanesAll().put(sample, Boolean.FALSE);
                    }
                    getRun().getSamplesRunUnitLanesPositions().get(sample).put(lanePosition, Boolean.FALSE);
                }
            }
            getRun().getRunUnitLanesPositionsSamples().get(lanePosition).remove(sample);
            getRun().getSamplesRunUnitLanesPositionsMap().get(sample).remove(lanePosition);
        }
    }

    public String rollbackStatus() {
        runService.rollbackStatus(getRun());
        getFacesMessagesManager().bufferWarningClear(Messages.get("statusRolledBack"));
        return getShowScreenRedirectURL();
    }

    public void runUnitTypeChanged(ValueChangeEvent event) {
        RunUnitType runUnitType = (RunUnitType) event.getNewValue();
        if (runUnitType != null) {
            emptyAllSamplesCollections();
            if (getRun().getOldRunUnit() == null) {
                getRun().setOldRunUnit(getRun().getRunUnit());
            }
            getRun().setRunUnit(new RunUnit(runUnitType, getRun()));
            getRun().getRunUnit().setPhysicalSeparation(runUnitType.isPhysicalSeparation());

            if (getRun().getRunUnit().getRunUnitLanes() != null) {
                getRun().initializeRunUnitLanes(true);
            }
        }
        getRun().setInstrumentReadConfiguration(null);
    }

    @Override
    public String save() {
        getValidationErrorMsg().clear();
        getValidationErrorMsg().putAll(runService.isValid(getRun()));
        if (getValidationErrorMsg().isEmpty()) {
            runService.save(getRun());
            return postSave(true, false);
        }
        handleValidationErrors(getValidationErrorMsg(), getDataTablesForValidation());
        return null;
    }

    public void setExcludeOrderItemSamples(boolean excludeOrderItemSamples) {
        this.excludeOrderItemSamples = excludeOrderItemSamples;
    }

    public void setMarkedSourceSamples(Set<Sample> markedSourceSamples) {
        this.markedSourceSamples = markedSourceSamples;
    }

    public void setMarkedSourceSamplesMap(Map<Sample, Boolean> markedSourceSamplesMap) {
        this.markedSourceSamplesMap = markedSourceSamplesMap;
    }

    public void setMarkedTargetSamples(Set<Sample> markedTargetSamples) {
        this.markedTargetSamples = markedTargetSamples;
    }

    public void setMarkedTargetSamplesMap(Map<Sample, Boolean> markedTargetSamplesMap) {
        this.markedTargetSamplesMap = markedTargetSamplesMap;
    }

    public void setMarkedTargetSamplesPerLaneMap(Map<Integer, Map<Sample, Boolean>> markedTargetSamplesPerLaneMap) {
        this.markedTargetSamplesPerLaneMap = markedTargetSamplesPerLaneMap;
    }

    public void setMultiplexIdConflictRecords(List<MultiplexIdConflictRecord> multiplexIdConflictRecords) {
        this.multiplexIdConflictRecords = multiplexIdConflictRecords;
    }

    public void setMultiplexIdsCorrectnessErrorMap(Map<String, Object> multiplexIdsCorrectnessErrorMap) {
        this.multiplexIdsCorrectnessErrorMap = multiplexIdsCorrectnessErrorMap;
    }

    public void setSamplesWithIncompleteMultiplexIds(Set<Sample> samplesWithIncompleteMultiplexIds) {
        this.samplesWithIncompleteMultiplexIds = samplesWithIncompleteMultiplexIds;
    }

    public void setSelectedDatasets(Set<Dataset> selectedDatasets) {
        this.selectedDatasets = selectedDatasets;
    }

    public void setSelectedLanePositions(Set<Integer> selectedLanePositions) {
        this.selectedLanePositions = selectedLanePositions;
    }

    public void setSelectedRunForSampleAssignment(Run selectedRunForSampleAssignment) {
        this.selectedRunForSampleAssignment = selectedRunForSampleAssignment;
    }

    public void setSelectedSampleType(String selectedSampleType) {
        if (selectedSampleType != null && !selectedSampleType.equals(this.selectedSampleType)) {
            emptyMarkedSamplesCollections();
        }
        this.selectedSampleType = selectedSampleType;
        if (Constants.ONT_READY_MADE.equals(getSelectedSampleType())) {
            setExcludeOrderItemSamples(false);
        }
    }

    public void setSelectedSamples(Set<Sample> selectedSamples) {
        this.selectedSamples = selectedSamples;
    }

    public void setSelectedSamplesAsList(List<Sample> samples) {
        this.selectedSamples = (Set<Sample>) CollectionHelper.asSet(samples);
    }

    public void setSelectionModePageOnly(boolean selectionModePageOnly) {
        this.selectionModePageOnly = selectionModePageOnly;
    }

    public void setShowAllColumns(boolean showAllColumns) {
        this.showAllColumns = showAllColumns;
    }

    public void setShowAllSamples(boolean showAllSamples) {
        this.showAllSamples = showAllSamples;
    }

    public void setShowSamplesLaneSeparated(boolean showSamplesLaneSeparated) {
        this.showSamplesLaneSeparated = showSamplesLaneSeparated;
    }

    public void setValidationErrorMsg(LinkedHashMap<String, String> validationErrorMsg) {
        this.validationErrorMsg = validationErrorMsg;
    }

    public void switchLaneRepresentation() {
        setShowSamplesLaneSeparated(!isShowSamplesLaneSeparated());
    }
}