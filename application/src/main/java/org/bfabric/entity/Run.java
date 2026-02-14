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

package org.bfabric.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.ContainerService;
import org.bfabric.service.RunSampleService;
import org.bfabric.service.SampleService;
import org.bfabric.service.SampleTypeService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Run.findNonFinished", query = "SELECT a FROM Run a WHERE a.status <> org.bfabric.enums.StatusEnum.FINISHED order by a.id")
@NamedQuery(name = "Run.findNonFinishedSupervisedBy", query = "SELECT a FROM Run a WHERE a.status <> org.bfabric.enums.StatusEnum.FINISHED and a.supervisor.id = :supervisorId ORDER BY a.id")
@NamedQuery(name = "Run.findBySampleId", query = "SELECT DISTINCT a.run FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE sample.id = :sampleId")
@NamedQuery(name = "Run.countBySampleId", query = "SELECT COUNT(DISTINCT a.run) FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE sample.id = :sampleId")
@NamedQuery(name = "Run.findByContainerId", query = "SELECT DISTINCT a.run FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE sample.container.id = :containerId")
@NamedQuery(name = "Run.countByContainerId", query = "SELECT COUNT(DISTINCT a.run) FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE sample.container.id = :containerId")
public class Run extends AbstractSupervisorDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Transient
    protected StatusEnum oldStatus;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<RunComment> comments = new HashSet<>();

    @Transient
    private Container container;

    @Transient
    private List<Container> containers;

    @Size(max = 256)
    @XmlElement
    private String dataFolder;

    @OneToMany(mappedBy = "run")
    @OrderBy("name")
    @XmlIDREF
    private Set<Dataset> datasets = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean demultiplexingRequired = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentId")
    @NotNull
    @XmlIDREF
    private Instrument instrument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentReadConfigurationId")
    @XmlIDREF
    private InstrumentReadConfiguration instrumentReadConfiguration;

    @OneToMany(mappedBy = "run")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReservation> instrumentReservations = new HashSet<>();

    @Transient
    private List<Integer> lanePositions = new ArrayList<>();

    @Transient
    private RunUnit oldRunUnit = null;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean qc = false;

    @OneToMany(mappedBy = "run", cascade = { CascadeType.REMOVE })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<RunSample> runSamples = new HashSet<>();

    @OneToOne(mappedBy = "run", cascade = { CascadeType.PERSIST })
    @XmlIDREF
    private RunUnit runUnit;

    @Transient
    private Map<Integer, Set<Sample>> runUnitLanesPositionsSamples = new HashMap<>();

    @Transient
    private List<Sample> samples;

    @Transient
    private Map<Sample, Boolean> samplesRunUnitLanesAll = new HashMap<>();

    @Transient
    private Map<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositions = new HashMap<>();

    @Transient
    private Map<Sample, Set<Integer>> samplesRunUnitLanesPositionsMap = new HashMap<>();

    @Size(max = 256)
    @XmlElement
    private String serverLocation;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<RunStatus> states = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private StatusEnum status;

    @NotNull
    private LocalDateTime statusModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusModifiedById")
    @NotNull
    private User statusModifiedBy;

    public Run() {
        setStatus(StatusEnum.PENDING);
    }

    public void addState() {
        if (getStates().isEmpty() || isStatusChanged()) {
            getStates().add(new RunStatus(this, getStatus()));
            setStatusModified();
        }
    }

    public void assignDatasets(Set<Dataset> aDatasets) {
        if (aDatasets != null) {
            // Remove the associations of the datasets which are not anymore to be associated with the run.
            getDatasets().removeAll(aDatasets);
            for (Dataset dataset : getDatasets()) {
                dataset.setRun(null);
            }
            for (Dataset dataset : aDatasets) {
                dataset.setRun(this);
            }
        }
    }

    public void assignSamples(Map<Sample, Map<Integer, Boolean>> aSamplesRunUnitLanesPositions, Set<Long> orderIds, Set<Sample> samplesToDelete, Set<Sample> samplesToPersist, Set<RunSample> runSamplesToDelete, Set<RunSample> runSamplesToPersist) {
        if (getRunUnit() != null && orderIds != null && samplesToDelete != null && samplesToPersist != null) {
            RunUnit aRunUnit = getRunUnit();
            List<RunUnitLane> aRunUnitLanes = new ArrayList<>(aRunUnit.getRunUnitLanes());
            // The current samples on the run unit lane(s).
            Set<Sample> currentSamplesOnRunUnitLane = new HashSet<>();
            // The mapped samples visible to the user which are assigned to the run.
            Set<Sample> mappedSamplesOnRunOld = new HashSet<>();
            for (RunUnitLane runUnitLane : aRunUnitLanes) {
                currentSamplesOnRunUnitLane.addAll(runUnitLane.getSamples());
                for (Sample sample : runUnitLane.getSamples()) {
                    if (sample.isOnRunType()) {
                        // The sample is of on run type.
                        if (sample.getMultiplexedParentForSampleOnRunType() != null) {
                            // The sample on the run type is in a multiplex.
                            mappedSamplesOnRunOld.add(sample.getMultiplexedParentForSampleOnRunType());
                        } else if (sample.getLabeledParentForSampleOnRunType() != null) {
                            // The sample on the run type is not in a multiplex.
                            mappedSamplesOnRunOld.add(sample.getLabeledParentForSampleOnRunType());
                        }
                    } else {
                        // The sample is not of on run type.
                        mappedSamplesOnRunOld.add(sample);
                    }
                }
                runUnitLane.getSamples().clear();
            }
            Set<Sample> removedSamples = new HashSet<>(mappedSamplesOnRunOld);
            removedSamples.removeAll(aSamplesRunUnitLanesPositions.keySet());
            for (Sample removedSample : removedSamples) {
                if (removedSample.isLabeledType()) {
                    if (removedSample.getChildren().size() == 1 && removedSample.getChildren().iterator().next().isOnRunType()) {
                        // The sample is only assigned to a single run, simply remove the sole child.
                        samplesToDelete.add(removedSample.getChildren().iterator().next());
                    } else if (removedSample.getChildren().size() > 1) {
                        // The sample might be assigned to multiple runs, remove only the child which is actually on the run.
                        for (Sample childToDelete : removedSample.getChildren()) {
                            if (childToDelete.isOnRunType() && currentSamplesOnRunUnitLane.contains(childToDelete)) {
                                samplesToDelete.add(childToDelete);
                                break;
                            }
                        }
                    }
                } else if (removedSample.isMultiplexedType()) {
                    // The multiplexed sample might be assigned to multiple runs, remove only the children which are actually on the run.
                    for (Sample childToDelete : removedSample.getChildren()) {
                        if (childToDelete.isOnRunType() && currentSamplesOnRunUnitLane.contains(childToDelete)) {
                            samplesToDelete.add(childToDelete);
                        }
                    }
                }
            }
            if (!removedSamples.isEmpty()) {
                runSamplesToDelete.addAll(CDI.current().select(RunSampleService.class).get()
                    .getRunSamplesByRunAndSamples(getId(), removedSamples.stream().map(Sample::getId).collect(Collectors.toSet())));
            }
            for (Map.Entry<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPosition : aSamplesRunUnitLanesPositions.entrySet()) {
                // The samples to create per each labeled/multiplexed sample.
                Set<Sample> samplesToCreatePerSample = new HashSet<>();
                Sample sample = samplesRunUnitLanesPosition.getKey();
                SampleTypeEnum sampleTypeEnumOnRun = sample.getSampleTypeEnumOnRun();
                if (!mappedSamplesOnRunOld.contains(sample) && (sample.isLabeledType() || sample.isMultiplexedType()) && sampleTypeEnumOnRun != null) {
                    SampleType onRunSampleType = CDI.current().select(SampleTypeService.class).get().getSampleTypeByName(sampleTypeEnumOnRun.getLabel());
                    if (sample.isLabeledType()) {
                        Sample sampleOnRun = sample.createChildSampleOnRun(onRunSampleType, null, null);
                        sampleOnRun.setTubeId(sample.getTubeId());
                        samplesToCreatePerSample.add(sampleOnRun);
                        samplesToPersist.add(sampleOnRun);
                    } else if (sample.isMultiplexedType()) {
                        for (Sample sampleInMultiplex : sample.getParents()) {
                            Sample sampleOnRun = sample.createChildSampleOnRun(onRunSampleType, sampleInMultiplex.getMultiplexId(), sampleInMultiplex.getMultiplexId2());
                            sampleOnRun.setContainer(sampleInMultiplex.getContainer());
                            sampleOnRun.setTubeId(sampleInMultiplex.getTubeId());
                            sampleOnRun.getParents().add(sampleInMultiplex);
                            samplesToCreatePerSample.add(sampleOnRun);
                            samplesToPersist.add(sampleOnRun);
                        }
                        runSamplesToPersist.add(new RunSample(this, sample));
                    }
                }
                for (Map.Entry<Integer, Boolean> sampleOnLane : samplesRunUnitLanesPosition.getValue().entrySet()) {
                    if (sampleOnLane.getKey() != null && sampleOnLane.getValue() != null && sampleOnLane.getValue()) {
                        if (!sample.isLabeledType() && !sample.isMultiplexedType()) {
                            // The sample is neither labeled nor multiplexed, i.e., simply put the sample on the run unit lane.
                            aRunUnitLanes.get(sampleOnLane.getKey() - 1).getSamples().add(sample);
                        } else {
                            if (!mappedSamplesOnRunOld.contains(sample)) {
                                if (sample.getSampleTypeEnumOnRun() != null) {
                                    // Sample is either labeled or multiplexed and not yet on the run, i.e., put the samplesToCreatePerSample on the run unit lane.
                                    aRunUnitLanes.get(sampleOnLane.getKey() - 1).getSamples().addAll(samplesToCreatePerSample);
                                }
                            } else {
                                for (Sample sampleOnRunChild : sample.getChildren()) {
                                    if (sampleOnRunChild.isOnRunType() && sampleOnRunChild.isOnlyOnRun(this)) {
                                        aRunUnitLanes.get(sampleOnLane.getKey() - 1).getSamples().add(sampleOnRunChild);
                                    }
                                }
                            }
                        }
                        if (!mappedSamplesOnRunOld.contains(sample) && sample.getContainer().isProcessing()) {
                            if (aRunUnit.getRun().isQc()) {
                                if (sample.getContainer().isCustomStatusEmptyOrNotEqualsTo(Constants.CUSTOM_ORDER_STATE_QUEUED_FOR_SEQUENCING_QC)) {
                                    orderIds.add(sample.getContainer().getId());
                                }
                            } else {
                                if (sample.getContainer().isCustomStatusEmptyOrNotEqualsTo(Constants.CUSTOM_ORDER_STATE_QUEUED_FOR_SEQUENCING)) {
                                    orderIds.add(sample.getContainer().getId());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Set<Mail> changeStatus(StatusEnum statusEnum) {
        Set<Mail> mails = new HashSet<>();
        setStatusAndAddState(statusEnum);
        return mails;
    }

    @Override
    public Run clone() throws CloneNotSupportedException {
        Run clone = (Run) super.clone();
        clone.runUnitLanesPositionsSamples = new HashMap<>();
        clone.samplesRunUnitLanesAll = new HashMap<>();
        clone.samplesRunUnitLanesPositions = new HashMap<>();
        clone.samplesRunUnitLanesPositionsMap = new HashMap<>();
        clone.comments = new HashSet<>();
        clone.datasets = new HashSet<>();
        clone.lanePositions = new ArrayList<>();
        clone.states = new ArrayList<>();
        clone.status = StatusEnum.PENDING;
        clone.dataFolder = null;
        clone.statusModified = null;
        clone.statusModifiedBy = null;
        clone.oldRunUnit = null;
        clone.oldStatus = null;
        clone.modifiedByUser = null;
        clone.samples = null;
        clone.container = null;
        clone.containers = null;
        clone.runUnit = getRunUnit().clone();
        clone.runUnit.setRun(clone);
        clone.runSamples = new HashSet<>();
        for (RunSample runSample : getRunSamples()) {
            RunSample newRunSample = runSample.clone();
            newRunSample.setRun(clone);
            clone.runSamples.add(newRunSample);
        }
        return clone;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.RUN_COMMENT;
    }

    public Set<RunComment> getComments() {
        return comments;
    }

    public Container getContainer() {
        return container;
    }

    public List<Container> getContainers() {
        if (containers == null) {
            containers = CDI.current().select(ContainerService.class).get().getContainersByRunId(getId());
        }
        return containers;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public Set<Dataset> getDatasets() {
        return datasets;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus());
        }
        addEntityInfoItem(summary, "demultiplexingRequired", isDemultiplexingRequired());
        addEntityInfoItem(summary, "qc", isQc());
        if (getInstrument() != null) {
            addEntityInfoItem(summary, "instrument", getInstrument().getName());
        }
        if (getInstrumentReadConfiguration() != null) {
            addEntityInfoItem(summary, "instrumentReadConfiguration", getInstrumentReadConfiguration().getName());
        }
        if (getRunUnit() != null) {
            addEntityInfoItem(summary, "runUnit", getRunUnit().getName());
        }
        if (StringHelper.isNotEmpty(getDataFolder())) {
            addEntityInfoItem(summary, "dataFolder", getDataFolder());
        }
        if (StringHelper.isNotEmpty(getServerLocation())) {
            addEntityInfoItem(summary, "serverLocation", getServerLocation());
        }
        addEntityInfoItems(summary, getCustomAttributes());
        return summary.toString();
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public InstrumentReadConfiguration getInstrumentReadConfiguration() {
        return instrumentReadConfiguration;
    }

    public Set<InstrumentReservation> getInstrumentReservations() {
        return instrumentReservations;
    }

    public List<Integer> getLanePositions() {
        return lanePositions;
    }

    public List<StatusEnum> getNextStates() {
        List<StatusEnum> nextStates = new ArrayList<>();
        StatusEnum statusEnum = getStatus();
        switch (statusEnum) {
        case PENDING:
            nextStates.add(StatusEnum.READY);
            break;
        case READY:
        case PROCESSINGFAILED:
            nextStates.add(StatusEnum.PROCESSING);
            break;
        case PROCESSING:
            if (isDemultiplexingRequired()) {
                nextStates.add(StatusEnum.PROCESSED);
                nextStates.add(StatusEnum.PROCESSINGFAILED);
            } else {
                nextStates.add(StatusEnum.FINISHED);
            }
            break;
        case PROCESSED:
        case DEMULTIPLEXINGFAILED:
            nextStates.add(StatusEnum.DEMULTIPLEXING);
            break;
        case DEMULTIPLEXING:
            nextStates.add(StatusEnum.DEMULTIPLEXED);
            nextStates.add(StatusEnum.DEMULTIPLEXINGFAILED);
            break;
        case DEMULTIPLEXED:
            nextStates.add(StatusEnum.FINISHED);
            break;
        case FINISHED:
        default:
            break;
        }
        return nextStates;
    }

    public RunUnit getOldRunUnit() {
        return oldRunUnit;
    }

    public StatusEnum getOldStatus() {
        return oldStatus;
    }

    public String getRowStyleClass() {
        if (isReady()) {
            return Constants.BACKGROUND_COLOR_BROWN;
        }
        if (isFinished()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isProcessing()) {
            return Constants.BACKGROUND_COLOR_BLUE_LIGHT;
        }
        if (isProcessed()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (isProcessingFailed()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isDemultiplexing()) {
            return Constants.BACKGROUND_COLOR_BLUE_LIGHT;
        }
        if (isDemultiplexingFailed()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isDemultiplexed()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (isPending()) {
            return Constants.BACKGROUND_COLOR_ORANGE;
        }
        if (isCanceled()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        return Constants.EMPTY_STRING;
    }

    public Set<RunSample> getRunSamples() {
        return runSamples;
    }

    public RunUnit getRunUnit() {
        return runUnit;
    }

    public Map<Integer, Set<Sample>> getRunUnitLanesPositionsSamples() {
        return runUnitLanesPositionsSamples;
    }

    public String getSampleNamePrefix() {
        return Messages.get("run") + "_" + getId() + "_";
    }

    public List<Sample> getSamples() {
        if (samples == null) {
            samples = CDI.current().select(SampleService.class).get().getSamplesByRunId(getId());
        }
        return samples;
    }

    public Map<Sample, Boolean> getSamplesRunUnitLanesAll() {
        return samplesRunUnitLanesAll;
    }

    public Map<Sample, Map<Integer, Boolean>> getSamplesRunUnitLanesPositions() {
        return samplesRunUnitLanesPositions;
    }

    public Map<Sample, Set<Integer>> getSamplesRunUnitLanesPositionsMap() {
        return samplesRunUnitLanesPositionsMap;
    }

    public String getServerLocation() {
        return serverLocation;
    }

    public AbstractStatus getState() {
        return getStates().isEmpty() ? null : getStates().get(getStates().size() - 1);
    }

    public List<RunStatus> getStates() {
        return states;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public LocalDateTime getStatusModified() {
        return statusModified;
    }

    public User getStatusModifiedBy() {
        return statusModifiedBy;
    }

    @Override
    @NotNull
    public User getSupervisor() {
        return super.getSupervisor();
    }

    public void initializeRunUnitLanes(boolean doMap) {
        getSamplesRunUnitLanesPositions().clear();
        for (RunUnitLane runUnitLane : getRunUnit().getRunUnitLanes()) {
            getLanePositions().add(runUnitLane.getPosition());
        }
        Collections.sort(getLanePositions());

        // Map samples for each run unit lane depending on doMap.
        Map<RunUnitLane, Set<Sample>> runUnitLaneSamplesMap = new HashMap<>();
        for (RunUnitLane runUnitLane : getRunUnit().getRunUnitLanes()) {
            runUnitLaneSamplesMap.put(runUnitLane, new HashSet<>(doMap ? mapSamplesOfOnRunType(runUnitLane.getSamples()) : runUnitLane.getSamples()));
        }

        for (RunUnitLane runUnitLane : getRunUnit().getRunUnitLanes()) {
            for (Sample sample : runUnitLaneSamplesMap.get(runUnitLane)) {
                if (!getSamplesRunUnitLanesPositions().containsKey(sample)) {
                    Map<Integer, Boolean> runUnitLanesPositions = new HashMap<>();
                    for (Integer position : getLanePositions()) {
                        runUnitLanesPositions.put(position, Boolean.FALSE);
                    }
                    getSamplesRunUnitLanesPositions().put(sample, runUnitLanesPositions);
                }
                getSamplesRunUnitLanesAll().put(sample, Boolean.FALSE);
                getSamplesRunUnitLanesPositions().get(sample).put(runUnitLane.getPosition(), Boolean.TRUE);
            }
        }
        for (Map.Entry<Sample, Map<Integer, Boolean>> sampleRunUnitLanesPosition : getSamplesRunUnitLanesPositions().entrySet()) {
            if (CollectionHelper.isCollectionAllTrue(sampleRunUnitLanesPosition.getValue().values())) {
                getSamplesRunUnitLanesAll().put(sampleRunUnitLanesPosition.getKey(), Boolean.TRUE);
            }
        }

        getRunUnitLanesPositionsSamples().clear();
        for (Integer position : getLanePositions()) {
            getRunUnitLanesPositionsSamples().put(position, new HashSet<>());
        }

        getSamplesRunUnitLanesPositionsMap().clear();
        for (Map.Entry<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositionsEntry : getSamplesRunUnitLanesPositions().entrySet()) {
            Sample sample = samplesRunUnitLanesPositionsEntry.getKey();
            getSamplesRunUnitLanesPositionsMap().put(sample, new HashSet<>());
            for (Map.Entry<Integer, Boolean> runUnitLanesPositionsEntry : samplesRunUnitLanesPositionsEntry.getValue().entrySet()) {
                if (runUnitLanesPositionsEntry.getValue() != null && runUnitLanesPositionsEntry.getValue()) {
                    getRunUnitLanesPositionsSamples().get(runUnitLanesPositionsEntry.getKey()).add(sample);
                    getSamplesRunUnitLanesPositionsMap().get(sample).add(runUnitLanesPositionsEntry.getKey());
                }
            }
        }
    }

    public boolean isCancelable() {
        return isPending() || isReady() || isProcessed() || isProcessingFailed() || isDemultiplexed() || isDemultiplexingFailed();
    }

    public boolean isCanceled() {
        return StatusEnum.CANCELED.equals(getStatus());
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration() != null && getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getInstrumentReservations().isEmpty() && (getSamples().isEmpty() && getDatasets().isEmpty() || isPending());
    }

    public boolean isDemultiplexed() {
        return StatusEnum.DEMULTIPLEXED.equals(getStatus());
    }

    public boolean isDemultiplexedOrFinished() {
        return isDemultiplexed() || isFinished();
    }

    public boolean isDemultiplexing() {
        return StatusEnum.DEMULTIPLEXING.equals(getStatus());
    }

    public boolean isDemultiplexingFailed() {
        return StatusEnum.DEMULTIPLEXINGFAILED.equals(getStatus());
    }

    public boolean isDemultiplexingRequired() {
        return demultiplexingRequired;
    }

    @Override
    public boolean isEnabled() {
        return !(getStatus().equals(StatusEnum.FINISHED) || getStatus().equals(StatusEnum.CANCELED));
    }

    public boolean isFinished() {
        return StatusEnum.FINISHED.equals(getStatus());
    }

    public boolean isPending() {
        return StatusEnum.PENDING.equals(getStatus());
    }

    public boolean isPendingOrReady() {
        return StatusEnum.PENDING.equals(getStatus()) || StatusEnum.READY.equals(getStatus());
    }

    public boolean isPhysicalSeparation() {
        return getRunUnit() != null && getRunUnit().isPhysicalSeparation();
    }

    public boolean isProcessed() {
        return StatusEnum.PROCESSED.equals(getStatus());
    }

    public boolean isProcessing() {
        return StatusEnum.PROCESSING.equals(getStatus());
    }

    public boolean isProcessingFailed() {
        return StatusEnum.PROCESSINGFAILED.equals(getStatus());
    }

    public boolean isQc() {
        return qc;
    }

    @Override
    public boolean isReadable() {
        return getConfiguration() != null && getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.RUNREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    public boolean isReady() {
        return StatusEnum.READY.equals(getStatus());
    }

    public boolean isRenderedReadConfigurations() {
        return getInstrument() != null && getInstrument().getReadConfigurations() != null && !getInstrument().getReadConfigurations().isEmpty();
    }

    public boolean isRenderedRunUnitType() {
        return getInstrument() != null && getInstrument().getRunUnitTypes() != null && !getInstrument().getRunUnitTypes().isEmpty();
    }

    public boolean isRenderedSendEmailCheckbox() {
        // return getNextStates() != null && (getNextStates().contains(StatusEnum.READY) || getNextStates().contains(StatusEnum.RUNNING) || getNextStates().contains(StatusEnum.FINISHED) || getNextStates().contains(StatusEnum.DEMULTIPLEXED));
        return false;
    }

    public boolean isRollbackable() {
        return getStates().size() > 1;
    }

    public boolean isRunning() {
        return StatusEnum.RUNNING.equals(getStatus());
    }

    public boolean isSamplesAssignable() {
        return (StatusEnum.PENDING.equals(getStatus()) || StatusEnum.READY.equals(getStatus())) && getInstrument() != null && getRunUnit() != null && getRunUnit().getRunUnitType() != null;
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    public boolean isSupervisorValid() {
        return getSupervisor() != null && getSupervisor().hasRoleImplicit(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public Set<Sample> mapSamplesOfOnRunType(Collection<Sample> aSamples) {
        Set<Sample> mappedSamples = new HashSet<>();
        if (aSamples != null) {
            for (Sample sample : aSamples) {
                if (!sample.isOnRunType()) {
                    // The sample is not of on run type, i.e., simply take the sample on the run.
                    mappedSamples.add(sample);
                } else {
                    // The sample is of on run type, i.e., map it to the respective labeled/multiplexed sample.
                    if (sample.getMultiplexedParentForSampleOnRunType() != null) {
                        // The on run type is in a multiplex.
                        mappedSamples.add(sample.getMultiplexedParentForSampleOnRunType());
                    } else if (sample.getLabeledParentForSampleOnRunType() != null) {
                        // The on run type is not in a multiplex.
                        mappedSamples.add(sample.getLabeledParentForSampleOnRunType());
                    }
                }
            }
        }
        return mappedSamples;
    }

    public void removeAllSamples() {
        if (getRunUnit() != null) {
            for (RunUnitLane runUnitLane : getRunUnit().getRunUnitLanes()) {
                runUnitLane.getSamples().clear();
            }
        }
    }

    public void rollbackStatus() {
        if (isRollbackable()) {
            getStates().remove(getStates().size() - 1);
            setStatus(getStates().get(getStates().size() - 1).getStatusEnum());
        }
    }

    public void setComments(Set<RunComment> comments) {
        this.comments = comments;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = StringHelper.format(dataFolder);
    }

    public void setDatasets(Set<Dataset> datasets) {
        this.datasets = datasets;
    }

    public void setDemultiplexingRequired(boolean demultiplexingRequired) {
        this.demultiplexingRequired = demultiplexingRequired;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentReadConfiguration(InstrumentReadConfiguration instrumentReadConfiguration) {
        this.instrumentReadConfiguration = instrumentReadConfiguration;
    }

    public void setLanePositions(List<Integer> lanePositions) {
        this.lanePositions = lanePositions;
    }

    public void setOldRunUnit(RunUnit oldRunUnit) {
        this.oldRunUnit = oldRunUnit;
    }

    public void setOldStatus(StatusEnum oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setQc(boolean qc) {
        this.qc = qc;
    }

    public void setRunSamples(Set<RunSample> runSamples) {
        this.runSamples = runSamples;
    }

    public void setRunUnit(RunUnit runUnit) {
        this.runUnit = runUnit;
    }

    public void setRunUnitLanesPositionsSamples(Map<Integer, Set<Sample>> runUnitLanesPositionsSamples) {
        this.runUnitLanesPositionsSamples = runUnitLanesPositionsSamples;
    }

    public void setSamplesRunUnitLanesAll(Map<Sample, Boolean> samplesRunUnitLanesAll) {
        this.samplesRunUnitLanesAll = samplesRunUnitLanesAll;
    }

    public void setSamplesRunUnitLanesPositions(Map<Sample, Map<Integer, Boolean>> samplesRunUnitLanesPositions) {
        this.samplesRunUnitLanesPositions = samplesRunUnitLanesPositions;
    }

    public void setServerLocation(String serverLocation) {
        this.serverLocation = StringHelper.format(serverLocation);
    }

    public void setStates(List<RunStatus> states) {
        this.states = states;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setStatusAndAddState(StatusEnum status) {
        setOldStatus(getStatus());
        setStatus(status);
        addState();
    }

    public void setStatusModified(LocalDateTime statusModified) {
        this.statusModified = statusModified;
    }

    public void setStatusModified() {
        statusModified = LocalDateTime.now();
        statusModifiedBy = getCurrentUser();
    }

    public void setStatusModifiedBy(User statusModifiedBy) {
        this.statusModifiedBy = statusModifiedBy;
    }
}