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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.ContainerService;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "plate_container_name_unique", columnNames = { "name", "containerid" }) })
@XmlRootElement
@NamedQuery(name = "Plate.findNonFinished", query = "SELECT a FROM Plate a WHERE a.status <> org.bfabric.enums.StatusEnum.FINISHED ORDER BY a.id")
@NamedQuery(name = "Plate.findNonFinishedSupervisedBy", query = "SELECT a FROM Plate a WHERE a.status <> org.bfabric.enums.StatusEnum.FINISHED and a.supervisor.id = :supervisorId ORDER BY a.id")
@NamedQuery(name = "Plate.findByContainerId", query = "SELECT DISTINCT a.plate FROM SamplePlatePosition a WHERE a.sample.container.id = :containerId")
@NamedQuery(name = "Plate.countByContainerId", query = "SELECT COUNT(DISTINCT a.plate) FROM SamplePlatePosition a WHERE a.sample.container.id = :containerId")
@NamedQuery(name = "Plate.findBySampleId", query = "SELECT DISTINCT a.plate FROM SamplePlatePosition a WHERE a.sample.id = :sampleId")
@NamedQuery(name = "Plate.countBySampleId", query = "SELECT COUNT(DISTINCT a.plate) FROM SamplePlatePosition a WHERE a.sample.id = :sampleId")
@NamedQuery(name = "Plate.checkUniqueName", query = "SELECT a.id FROM Plate a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.container.id = :containerId")
public class Plate extends AbstractSupervisorDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Transient
    private final Map<Long, SamplePlatePosition> initialSampleIdSamplePlatePositionMap = new HashMap<>();

    @Transient
    protected StatusEnum oldStatus;

    @Transient
    private String cloneMode = Constants.CLONE_MODE_SAMPLES_NONE;

    @Transient
    private String clonedPlateName;

    @Transient
    private PlateType clonedPlateType;

    @Transient
    private SampleQCTypeEnum clonedQualityControlType;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<PlateComment> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "containerid")
    @XmlIDREF
    private Container container;

    @Transient
    private List<Container> containers;

    @Size(max = 64)
    @XmlElement
    private String location;

    @Size(max = 256)
    @XmlElement
    private String nameGiven;

    @OneToMany(mappedBy = "plate")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrderItem> orderItems = new HashSet<>();

    @Transient
    private Map<Integer, Integer> plateIdsNonEmptySampleNamesMap = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platelayoutid")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @NotNull
    @XmlIDREF
    @XmlElement(name = "platelayout")
    private PlateLayout plateLayout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platetypeid")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @NotNull
    @XmlIDREF
    @XmlElement(name = "platetype")
    private PlateType plateType;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement(name = "sampleassignmentperrow")
    private boolean sampleAssignmentPerRow = false;

    @OneToMany(mappedBy = "plate", cascade = { CascadeType.REMOVE })
    @OrderBy("position")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlElement(name = "sampleplateposition")
    private Set<SamplePlatePosition> samplePlatePositions = new HashSet<>();

    @Transient
    private Set<SamplePlatePosition> samplePlatePositionsPlateSubmission = new HashSet<>();

    @Transient
    private List<SamplePlatePosition> samplePlatePositionsPlateSubmissionOrderedByAssignmentOrder = new ArrayList<>();

    @Transient
    private Set<Sample> samples;

    @OneToMany(mappedBy = "plate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<PlateStatus> states = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private StatusEnum status;

    @NotNull
    private LocalDateTime statusModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusmodifiedbyid")
    @NotNull
    private User statusModifiedBy;

    @ManyToMany
    @JoinTable(name = "workflowstepplate", joinColumns = @JoinColumn(name = "plateid"), inverseJoinColumns = @JoinColumn(name = "workflowstepid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowStep> workflowSteps = new HashSet<>();

    public Plate() {
        setStatus(StatusEnum.PENDING);
    }

    public void addState() {
        if (getStates().isEmpty() || isStatusChanged()) {
            getStates().add(new PlateStatus(this, getStatus()));
            setStatusModified();
        }
    }

    public Set<Mail> changeStatus(StatusEnum statusEnum) {
        Set<Mail> mails = new HashSet<>();
        setStatusAndAddState(statusEnum);
        return mails;
    }

    @Override
    public Plate clone() throws CloneNotSupportedException {
        Plate clone = (Plate) super.clone();
        clone.samplePlatePositions = new HashSet<>();
        clone.orderItems = new HashSet<>();
        clone.workflowSteps = new HashSet<>();
        clone.states = new ArrayList<>();
        clone.status = StatusEnum.PENDING;
        return clone;
    }

    public void cloneModeChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null && event.getNewValue().equals(Constants.CLONE_MODE_SAMPLES_NONE)) {
            setClonedPlateType(null);
            setClonedQualityControlType(null);
        }
    }

    private Sample createNewSampleForPlateSubmission(SampleType sampleType, Order order) {
        Sample newSample = new Sample();
        newSample.setSampleType(sampleType);
        newSample.setContainer(order);
        return newSample;
    }

    public String getCloneMode() {
        return cloneMode;
    }

    public String getClonedPlateName() {
        return clonedPlateName;
    }

    public PlateType getClonedPlateType() {
        return clonedPlateType;
    }

    public SampleQCTypeEnum getClonedQualityControlType() {
        return clonedQualityControlType;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.PLATE_COMMENT;
    }

    public Set<PlateComment> getComments() {
        return comments;
    }

    public Container getContainer() {
        return container;
    }

    public List<Container> getContainers() {
        if (containers == null) {
            containers = CDI.current().select(ContainerService.class).get().getContainersByPlateId(getId());
        }
        return containers;
    }

    public List<SamplePlatePosition> getCurrentSamplePlatePositionsOrderedByAssignmentOrder() {
        return getPlateLayout().getSamplePlatePositionsOrderedByAssignmentOrder(getSamplePlatePositions());
    }

    public Set<SamplePlatePosition> getCurrentSamplePlatePositionsWithNonEmptySampleNames() {
        Set<SamplePlatePosition> currentSamplePlatePositionsWithNonEmptySampleNames = new HashSet<>();
        int numberOfSamplesWithNonEmptyNamesLeftCounter = getPlateIdsNonEmptySampleNamesMap().get(hashCode()) != null ? getPlateIdsNonEmptySampleNamesMap().get(hashCode()) : -1;
        for (int i = 0; i < getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder().size(); i++) {
            final SamplePlatePosition samplePlatePosition = getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder().get(i);
            if (StringHelper.isNotEmpty(samplePlatePosition.getSample().getName())) {
                currentSamplePlatePositionsWithNonEmptySampleNames.add(samplePlatePosition);
                numberOfSamplesWithNonEmptyNamesLeftCounter--;
                if (numberOfSamplesWithNonEmptyNamesLeftCounter == 0) {
                    break;
                }
            }
        }
        return currentSamplePlatePositionsWithNonEmptySampleNames;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.PLATEMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "status", getStatus());
        if (getContainer() != null) {
            addEntityInfoItem(summary, "container", getContainer().getDisplayName());
        }
        if (getPlateLayout() != null) {
            addEntityInfoItem(summary, "plateLayout", getPlateLayout().getDisplayName());
            addEntityInfoItem(summary, "capacity", getPlateLayout().getCapacity());
            addEntityInfoItem(summary, "columns", getPlateLayout().getColumns());
            addEntityInfoItem(summary, "rows", getPlateLayout().getRows());
        }
        if (getPlateType() != null) {
            addEntityInfoItem(summary, "plateType", getPlateType().getDisplayName());
        }
        addEntityInfoItems(summary, getCustomAttributes());
        return summary.toString();
    }

    public Map<Long, SamplePlatePosition> getInitialSampleIdSamplePlatePositionMap() {
        return initialSampleIdSamplePlatePositionMap;
    }

    public String getLocation() {
        return location;
    }

    public int getMaxItemNumber(Collection<SamplePlatePosition> aSamplePlatePositions) {
        int maxItemNumber = 0;
        if (aSamplePlatePositions != null) {
            for (SamplePlatePosition samplePlatePosition : aSamplePlatePositions) {
                Sample sample = samplePlatePosition.getSample();
                if (samplePlatePosition.isManaged() && sample != null && sample.isManaged() && StringHelper.isNotEmpty(sample.getTubeId()) && StringHelper.isNotEmpty(sample.getName())) {
                    String tubeId = sample.getTubeId();
                    int itemNumber = Integer.parseInt(tubeId.substring(tubeId.indexOf("#") + 1));
                    if (itemNumber > maxItemNumber) {
                        maxItemNumber = itemNumber;
                    }
                }
            }
        }
        return maxItemNumber;
    }

    public String getNameGiven() {
        return nameGiven;
    }

    public List<StatusEnum> getNextStates() {
        List<StatusEnum> nextStates = new ArrayList<>();
        StatusEnum statusEnum = getStatus();
        switch (statusEnum) {
        case PENDING:
            //            if (!getSamples().isEmpty()) {
            nextStates.add(StatusEnum.READY);
            //            }
            break;
        case READY:
            nextStates.add(StatusEnum.PROCESSING);
            break;
        case PROCESSING:
            nextStates.add(StatusEnum.FINISHED);
            break;
        case FINISHED:
        default:
            break;
        }
        return nextStates;
    }

    @SuppressWarnings("unused")
    public int getNonEmptySampleNamesCount() {
        return getPlateIdsNonEmptySampleNamesMap().get(hashCode()) != null ? getPlateIdsNonEmptySampleNamesMap().get(hashCode()) : 0;
    }

    public Set<String> getOccupiedGridPositions() {
        return getOccupiedGridPositionsSamplePlatePositionMap().keySet();
    }

    public Map<String, SamplePlatePosition> getOccupiedGridPositionsSamplePlatePositionMap() {
        Map<String, SamplePlatePosition> occupiedGridPositionsSamplePlatePositionMap = new HashMap<>();
        for (SamplePlatePosition samplePlatePosition : getSamplePlatePositions()) {
            occupiedGridPositionsSamplePlatePositionMap.put(getPlateLayout().getGridPosition(samplePlatePosition.getPosition()), samplePlatePosition);
        }
        return occupiedGridPositionsSamplePlatePositionMap;
    }

    public StatusEnum getOldStatus() {
        return oldStatus;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Map<Integer, Integer> getPlateIdsNonEmptySampleNamesMap() {
        return plateIdsNonEmptySampleNamesMap;
    }

    public PlateLayout getPlateLayout() {
        return plateLayout;
    }

    public PlateType getPlateType() {
        return plateType;
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
        if (isPending()) {
            return Constants.BACKGROUND_COLOR_ORANGE;
        }
        return Constants.EMPTY_STRING;
    }

    public String getSampleAssignmentDirection() {
        return sampleAssignmentPerRow ? Constants.ROW : Constants.COLUMN;
    }

    public Set<SamplePlatePosition> getSamplePlatePositions() {
        return samplePlatePositions;
    }

    public Set<SamplePlatePosition> getSamplePlatePositionsPlateSubmission() {
        return samplePlatePositionsPlateSubmission;
    }

    public List<SamplePlatePosition> getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder() {
        return samplePlatePositionsPlateSubmissionOrderedByAssignmentOrder;
    }

    public Set<Sample> getSamples() {
        if (samples == null) {
            samples = new HashSet<>();
            for (SamplePlatePosition samplePlatePosition : getSamplePlatePositions()) {
                samples.add(samplePlatePosition.getSample());
            }
        }
        return samples;
    }

    public AbstractStatus getState() {
        return getStates().isEmpty() ? null : getStates().get(getStates().size() - 1);
    }

    public List<PlateStatus> getStates() {
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

    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public boolean hasEmptyPositionsInBetween() {
        List<SamplePlatePosition> positions = new ArrayList<>(getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder());
        if (positions.isEmpty()) {
            Map<Integer, SamplePlatePosition> positionSamplePlatePositionMap = new HashMap<>();
            for (SamplePlatePosition samplePlatePosition : getSamplePlatePositions()) {
                if (samplePlatePosition.getSample() != null && StringHelper.isNotEmpty(samplePlatePosition.getSample().getName())) {
                    positionSamplePlatePositionMap.put(samplePlatePosition.getPosition().intValue() - 1, samplePlatePosition);
                }
            }
            for (int i = 0; i < getPlateLayout().getCapacity(); i++) {
                if (positionSamplePlatePositionMap.containsKey(i)) {
                    positions.add(positionSamplePlatePositionMap.get(i));
                } else {
                    positions.add(new SamplePlatePosition(new Sample(), this, i + 1L));
                }
            }
        }
        positions.sort(SamplePlatePosition.getAssignmentPositionComparator(getPlateLayout().getColumns(), isSampleAssignmentPerRow()));
        boolean foundEmpty = false;
        for (SamplePlatePosition position : positions) {
            Sample sample = position.getSample();
            if (sample != null && StringHelper.isNotEmpty(sample.getName())) {
                if (foundEmpty) {
                    return true;
                }
            } else {
                foundEmpty = true;
            }
        }
        return false;
    }

    public boolean hasFixedSamplePlateAssignmentOrder() {
        return getPlateLayout() != null && (getPlateLayout().hasOneColumn() || getPlateLayout().hasOneRow());
    }

    public void initPlateIdsNonEmptySampleNamesMap() {
        getPlateIdsNonEmptySampleNamesMap().clear();
        for (SamplePlatePosition samplePlatePosition : getSamplePlatePositions()) {
            getInitialSampleIdSamplePlatePositionMap().put(samplePlatePosition.getSample().getId(), samplePlatePosition);
            getPlateIdsNonEmptySampleNamesMap().merge(hashCode(), 1, Integer::sum);
        }
    }

    public void initSamplePlatePositionsPlateSubmissionCollections(SampleType sampleType, Order order) {
        if (sampleType != null && order != null && getPlateLayout() != null) {
            if (getSamplePlatePositionsPlateSubmission().isEmpty() && getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder().isEmpty()) {
                if (getSamples().isEmpty()) {
                    // The plate has no samples and the sample plate positions collections used for the plate submission are not initialized yet.
                    for (int i = 0; i < getPlateLayout().getCapacity(); i++) {
                        getSamplePlatePositionsPlateSubmission().add(new SamplePlatePosition(createNewSampleForPlateSubmission(sampleType, order), this, i + 1L));
                    }
                } else {
                    // The plate has samples and the sample plate positions collections used for the plate submission are not initialized yet.
                    Map<Integer, SamplePlatePosition> positionSamplePlatePositionMap = new HashMap<>();
                    for (SamplePlatePosition samplePlatePosition : getSamplePlatePositions()) {
                        positionSamplePlatePositionMap.put(samplePlatePosition.getPosition().intValue() - 1, samplePlatePosition);
                    }
                    for (int i = 0; i < getPlateLayout().getCapacity(); i++) {
                        if (positionSamplePlatePositionMap.containsKey(i)) {
                            Sample sample = positionSamplePlatePositionMap.get(i).getSample();
                            sample.setOldUserSampleName(sample.getName());
                            sample.setUserSampleName(sample.getName());
                            getSamplePlatePositionsPlateSubmission().add(positionSamplePlatePositionMap.get(i));
                        } else {
                            getSamplePlatePositionsPlateSubmission().add(new SamplePlatePosition(createNewSampleForPlateSubmission(sampleType, order), this, i + 1L));
                        }
                    }
                }
                getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder().addAll(getPlateLayout().getSamplePlatePositionsOrderedByAssignmentOrder(getSamplePlatePositionsPlateSubmission()));
            } else {
                for (SamplePlatePosition samplePlatePosition : getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder()) {
                    Sample sample = samplePlatePosition.getSample();
                    sample.setOldUserSampleName(sample.getName());
                    sample.setUserSampleName(sample.getName());
                }
            }
        }
    }

    public boolean isCancelable() {
        return isPending();
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getOrderItems().isEmpty();
    }

    public boolean isDeletable(OrderItem orderItem) {
        if (orderItem == null) {
            return isDeletable();
        }
        if (!orderItem.isDeletable()) {
            return false;
        }
        Set<OrderItem> orderItemSet = new HashSet<>(getOrderItems());
        orderItemSet.remove(orderItem);
        Set<SamplePlatePosition> samplePlatePositionSet = new HashSet<>(getSamplePlatePositions());
        samplePlatePositionSet.removeAll(getInitialSampleIdSamplePlatePositionMap().values());
        return isUpdatable() && orderItemSet.isEmpty() && samplePlatePositionSet.isEmpty();
    }

    public boolean isEmptyUserSubmitted() {
        if (isNotEmpty()) {
            return false;
        }
        for (SamplePlatePosition samplePlatePosition : getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder()) {
            if (StringHelper.isNotEmpty(samplePlatePosition.getSample().getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !getStatus().equals(StatusEnum.FINISHED);
    }

    public boolean isFinished() {
        return StatusEnum.FINISHED.equals(getStatus());
    }

    public boolean isLabManagerOrAdmin() {
        return hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    public boolean isNotEmpty() {
        return !getSamplePlatePositions().isEmpty();
    }

    public boolean isPending() {
        return StatusEnum.PENDING.equals(getStatus());
    }

    public boolean isPlateTypeUserSubmitted() {
        return getPlateType() != null && getPlateType().getName().equals(Constants.PLATE_TYPE_USER_SUBMITTED_NAME);
    }

    public boolean isProcessing() {
        return StatusEnum.PROCESSING.equals(getStatus());
    }

    @Override
    public boolean isReadable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.PLATEREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    public boolean isReady() {
        return StatusEnum.READY.equals(getStatus());
    }

    public boolean isRenderedSendEmailCheckbox() {
        // return getNextStates() != null && getNextStates().contains(StatusEnum.READY);
        return false;
    }

    public boolean isRollbackable() {
        return getStates().size() > 1;
    }

    public boolean isSampleAssignmentEditable() {
        return isLabManagerOrAdmin() && !StatusEnum.PROCESSING.equals(getStatus()) && !StatusEnum.FINISHED.equals(getStatus());
    }

    public boolean isSampleAssignmentPerRow() {
        return sampleAssignmentPerRow;
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    public boolean isSupervisorValid() {
        return getSupervisor() != null && getSupervisor().hasRoleImplicit(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatable() {
        return !StatusEnum.FINISHED.equals(getStatus()) && isCreatable();
    }

    public boolean isUpdatableOrUserUpdatable() {
        return isUpdatable() || isUserUpdatable();
    }

    @Override
    public boolean isUpdatableWS() {
        return isCreatable();
    }

    private boolean isUserUpdatable() {
        return StatusEnum.PENDING.equals(getStatus()) && isPlateTypeUserSubmitted() && super.isCreator();
    }

    public void resetSamplePlatePositionsPlateSubmissionCollections() {
        getSamplePlatePositionsPlateSubmission().clear();
        getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder().clear();
    }

    public void rollbackStatus() {
        if (isRollbackable()) {
            getStates().remove(getStates().size() - 1);
            setStatus(getStates().get(getStates().size() - 1).getStatusEnum());
        }
    }

    public void setCloneMode(String cloneMode) {
        this.cloneMode = StringHelper.format(cloneMode);
    }

    public void setClonedPlateName(String clonedPlateName) {
        this.clonedPlateName = StringHelper.format(clonedPlateName);
    }

    public void setClonedPlateType(PlateType clonedPlateType) {
        this.clonedPlateType = clonedPlateType;
    }

    public void setClonedQualityControlType(SampleQCTypeEnum clonedQualityControlType) {
        this.clonedQualityControlType = clonedQualityControlType;
    }

    public void setComments(Set<PlateComment> comments) {
        this.comments = comments;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setLocation(String location) {
        this.location = StringHelper.format(location);
    }

    public void setNameGiven(String nameGiven) {
        this.nameGiven = StringHelper.format(nameGiven);
    }

    public void setOldStatus(StatusEnum oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setPlateIdsNonEmptySampleNamesMap(Map<Integer, Integer> plateIdsNonEmptySampleNamesMap) {
        this.plateIdsNonEmptySampleNamesMap = plateIdsNonEmptySampleNamesMap;
    }

    public void setPlateLayout(PlateLayout plateLayout) {
        this.plateLayout = plateLayout;
    }

    public void setPlateType(PlateType plateType) {
        this.plateType = plateType;
    }

    public void setSampleAssignmentPerRow(boolean sampleAssignmentPerRow) {
        this.sampleAssignmentPerRow = sampleAssignmentPerRow;
    }

    public void setSamplePlatePositions(Set<SamplePlatePosition> samplePlatePositions) {
        this.samplePlatePositions = samplePlatePositions;
    }

    public void setSamplePlatePositionsPlateSubmission(Set<SamplePlatePosition> samplePlatePositionsPlateSubmission) {
        this.samplePlatePositionsPlateSubmission = samplePlatePositionsPlateSubmission;
    }

    public void setSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder(List<SamplePlatePosition> samplePlatePositionsPlateSubmissionOrderedByAssignmentOrder) {
        this.samplePlatePositionsPlateSubmissionOrderedByAssignmentOrder = samplePlatePositionsPlateSubmissionOrderedByAssignmentOrder;
    }

    public void setStates(List<PlateStatus> states) {
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

    public void setWorkflowSteps(Set<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public void shiftPositions(List<SamplePlatePosition> samplePlatePositions) {
        if (samplePlatePositions != null) {
            for (int i = 0; i < samplePlatePositions.size(); i++) {
                SamplePlatePosition p1 = samplePlatePositions.get(i);
                if (p1.getPosition() == null) {
                    for (int j = i + 1; j < samplePlatePositions.size(); j++) {
                        SamplePlatePosition p2 = samplePlatePositions.get(j);
                        if (p2.getPosition() != null) {
                            switchPositions(samplePlatePositions, i, j);
                        }
                    }
                }
            }
        }
    }

    public void shiftPositions() {
        shiftPositions(getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder());
    }

    public void switchPositions(List<SamplePlatePosition> samplePlatePositions, int i, int j) {
        if (samplePlatePositions != null && i >= 0 && i < j && samplePlatePositions.size() > j) {
            SamplePlatePosition temp = samplePlatePositions.get(i);
            temp.setPosition((long) (j + 1));
            SamplePlatePosition temp2 = samplePlatePositions.get(j);
            temp2.setPosition((long) (i + 1));
            samplePlatePositions.set(i, temp2);
            samplePlatePositions.set(j, temp);
        }
    }
}
