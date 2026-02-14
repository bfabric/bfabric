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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleUserDecisionEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "OrderItem.findOrdersBySampleId", query = "SELECT DISTINCT a.order FROM OrderItem a WHERE a.sample.id = :sampleId")
@NamedQuery(name = "OrderItem.findBySample", query = "SELECT a FROM OrderItem a WHERE a.sample = :sample")
@NamedQuery(name = "OrderItem.findEditableByOrder", query = "SELECT a FROM OrderItem a WHERE a.order = :order and a.charges is empty and a.workflows is empty ORDER BY a.id")
@NamedQuery(name = "OrderItem.checkUniqueTubeId", query = "SELECT a.id FROM OrderItem a WHERE lower(tubeId) = lower(:tubeId) and a.id <> :id")
public class OrderItem extends AbstractDescriptionBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "orderitemanalysisreason", joinColumns = @JoinColumn(name = "orderitemid"), inverseJoinColumns = @JoinColumn(name = "analysisreasonid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "reason")
    private Set<AnalysisReason> analysisReasons = new HashSet<>();

    @XmlElement
    private boolean chargeable = true;

    @ManyToMany
    @JoinTable(name = "chargeorderitem", joinColumns = @JoinColumn(name = "orderitemid"), inverseJoinColumns = @JoinColumn(name = "chargeid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @Transient
    private Map<Integer, OrderItem> coupledNotManagedHashCodesOrderItemsMap = new HashMap<>();

    @Transient
    private boolean deleted = false;

    @XmlElement
    private Integer insertSize;

    @Size(max = 64)
    @XmlElement
    private String libraryType;

    @Size(max = 32)
    @XmlElement
    private String multiplexing;

    private Long oldServiceOrderItemId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderid")
    @NotNull
    @XmlIDREF
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plateid")
    @XmlIDREF
    private Plate plate;

    @Size(max = 64)
    @XmlElement
    private String readType;

    @Size(max = 64)
    @XmlElement
    private String region;

    @Transient
    private String rowStyleClassCoupled = Constants.EMPTY_STRING;

    @Transient
    private String rowTitleCoupled = Constants.EMPTY_STRING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampleid")
    @XmlIDREF
    private Sample sample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceid")
    @XmlIDREF
    private Service service;

    @Size(max = 32)
    @XmlElement
    private String tubeId;

    @Transient
    private String tubeIdOld;

    @Column(updatable = false, insertable = false)
    private String tubeIdPadded;

    @ManyToMany
    @JoinTable(name = "workfloworderitem", joinColumns = @JoinColumn(name = "orderitemid"), inverseJoinColumns = @JoinColumn(name = "workflowid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workflow> workflows = new HashSet<>();

    public OrderItem() {
        super();
    }

    @Override
    public OrderItem clone() throws CloneNotSupportedException {
        OrderItem clone = (OrderItem) super.clone();
        clone.analysisReasons = new HashSet<>();
        clone.charges = new HashSet<>();
        clone.workflows = new HashSet<>();
        return clone;
    }

    public OrderItem cloneWithClonedSample() throws CloneNotSupportedException {
        OrderItem clone = clone();
        if (getSample() != null) {
            clone.setSample(getSample().clone());
        }
        return clone;
    }

    public void decouple(OrderItem comparedItem, String oldSampleName) {
        if (getSample() != null) {
            Sample aSample;
            try {
                aSample = getSample().clone();
            } catch (final CloneNotSupportedException e) {
                aSample = new Sample();
            }

            setSample(aSample);
            if (comparedItem.getOrder().isOrderItemTubeIdRendered()) {
                setTubeId(getTubeIdOld());
            } else {
                getSample().setTubeId(getTubeIdOld());
                if (!comparedItem.isManaged()) {
                    comparedItem.getSample().setTubeId(comparedItem.getTubeIdOld());
                }
            }
            comparedItem.getSample().setName(oldSampleName);
        }
    }

    public void generateTubeId(int index, boolean isClone) {
        if (getOrder().isSampleTubeIdRendered() && getSample() != null) {
            if (getSample().getTubeId() == null || isClone) {
                // Do not override the tube id when reusing a sample.
                getSample().setTubeId(generateTubeId(index));
                getSample().setChanged(true);
            }
        } else {
            setTubeId(generateTubeId(index));
        }
    }

    public String generateTubeId(int itemId) {
        return getOrder().getId() + "/" + itemId;
    }

    public Set<AnalysisReason> getAnalysisReasons() {
        return analysisReasons;
    }

    public String getAnalysisReasonsAsString() {
        return CollectionHelper.print(getAnalysisReasons(), "name", "/n", false);
    }

    public List<Charge> getBookedCharges() {
        ArrayList<Charge> bookedCharges = new ArrayList<>();
        for (Charge charge : getCharges()) {
            if (charge.isBooked()) {
                bookedCharges.add(charge);
            }
        }
        return bookedCharges;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public Map<Integer, OrderItem> getCoupledNotManagedHashCodesOrderItemsMap() {
        return coupledNotManagedHashCodesOrderItemsMap;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getName();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getOrder() != null) {
            addEntityInfoItem(summary, null, getOrder().getEntitySpecifics());
        }
        if (getService() != null) {
            addEntityInfoItem(summary, "service", getService().getName());
        }
        addEntityInfoItem(summary, "tubeId", getTubeId());
        if (getSample() != null) {
            addEntityInfoItem(summary, null, getSample().getEntitySpecifics());
        }
        if (getPlate() != null) {
            addEntityInfoItem(summary, null, getPlate().getEntitySpecifics());
        }
        if (StringHelper.isNotEmpty(getLibraryType())) {
            addEntityInfoItem(summary, "libraryType", getLibraryType());
        }
        if (StringHelper.isNotEmpty(getReadType())) {
            addEntityInfoItem(summary, "readType", getReadType());
        }
        if (StringHelper.isNotEmpty(getRegion())) {
            addEntityInfoItem(summary, "region", getRegion());
        }
        if (StringHelper.isNotEmpty(getMultiplexing())) {
            addEntityInfoItem(summary, "multiplexing", getMultiplexing());
        }
        addEntityInfoItem(summary, "charged", isCharged());
        if (!isCharged()) {
            addEntityInfoItem(summary, "chargeable", isChargeable());
        }
        if (!getAnalysisReasons().isEmpty()) {
            addEntityInfoItem(summary, "analysisReasons", CollectionHelper.print(getAnalysisReasons()));
        }
        return summary.toString();
    }

    public Integer getInsertSize() {
        return insertSize;
    }

    public String getLibraryType() {
        return libraryType;
    }

    public String getMultiplexing() {
        return multiplexing;
    }

    public String getName() {
        return getSample() != null ? getOrder().isProcessesPlates() && getPlate() != null ? getPlate().getName() : getSample().getName() : Constants.EMPTY_STRING;
    }

    public String getOldId() {
        return getOldServiceOrderItemId() != null ? StringHelper.embraceParentheses(Messages.get("oldServiceOrderItemId") + " " + getOldServiceOrderItemId()) : null;
    }

    public Long getOldServiceOrderItemId() {
        return oldServiceOrderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public Plate getPlate() {
        return plate;
    }

    public String getReadType() {
        return readType;
    }

    public String getRegion() {
        return region;
    }

    public String getRowStyleClass() {
        return getSample() != null && getSample().getUserDecision() != null && !getSample().getUserDecision()
            .equals(SampleUserDecisionEnum.PROCEED) ? Constants.BACKGROUND_COLOR_RED : Constants.EMPTY_STRING;
    }

    public String getRowStyleClassCoupled() {
        return rowStyleClassCoupled;
    }

    public String getRowTitleCoupled() {
        return rowTitleCoupled;
    }

    public Sample getSample() {
        return sample;
    }

    public Service getService() {
        return service;
    }

    public String getTubeId() {
        return tubeId;
    }

    public String getTubeIdDisplay() {
        return getOrder().isOrderItemTubeIdRendered() ? getTubeId() : getSample().getTubeId();
    }

    public String getTubeIdOld() {
        return tubeIdOld;
    }

    public String getTubeIdOldPadded() {
        if (getTubeIdOld() != null && getTubeIdOld().contains("/")) {
            String tubeIdOldPadded;
            String[] splitOne = getTubeIdOld().split("/");
            if (splitOne[1].contains("#")) {
                String[] splitTwo = splitOne[1].split("#");
                tubeIdOldPadded = splitOne[0] + "/" + StringUtils.leftPad(splitTwo[0], 5, "0") + "#" + StringUtils.leftPad(splitTwo[1], 5, "0");
            } else {
                tubeIdOldPadded = splitOne[0] + "/" + StringUtils.leftPad(splitOne[1], 5, "0");
            }
            return tubeIdOldPadded;
        }
        return null;
    }

    public String getTubeIdPadded() {
        return tubeIdPadded;
    }

    public Set<Workflow> getWorkflows() {
        return workflows;
    }

    public boolean hasNoDependents() {
        return getCharges().isEmpty() && getWorkflows().isEmpty();
    }

    public boolean hasSampleReplacements() {
        return getSample() != null && getSample().hasReplacements() || getPlate() != null && getPlate().getSamples().stream().anyMatch(Sample::hasReplacements);
    }

    public boolean isChargeable() {
        return chargeable;
    }

    public boolean isCharged() {
        return !getCharges().isEmpty();
    }

    @Override
    public boolean isCloneable() {
        return getOrder().isUpdatable() && (!getOrder().isProcessesPlates() || getOrder().getServiceType().isServiceColumnEnabled());
    }

    public boolean isCoupleable(OrderItem orderItem) {
        return orderItem != null && orderItem.getSample() != null && getSample() != null && getSample().isCoupleable(orderItem.getSample());
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && (getPlate() == null || !getOrder().isProcessesPlates() || getPlate().getSamples().isEmpty() || getPlate().isUpdatableOrUserUpdatable());
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isEmpty() {
        Sample checkSample = getSample();
        if (checkSample != null) {
            if (StringHelper.isNotEmpty(checkSample.getName())) {
                return false;
            }

            // Order item attributes
            boolean ret = isEmptyAttributes();
            if (ret) {
                // Sample attributes
                ret = checkSample.isEmpty();
            }
            return ret;
        }
        Plate checkPlate = getPlate();
        if (checkPlate != null) {
            if (StringHelper.isNotEmpty(checkPlate.getName())) {
                return false;
            }

            // Order item attributes
            boolean ret = isEmptyAttributes();
            if (ret) {
                // Plate attributes
                ret = checkPlate.getPlateLayout() == null && checkPlate.getPlateIdsNonEmptySampleNamesMap().isEmpty() && checkPlate.getInitialSampleIdSamplePlatePositionMap()
                    .isEmpty();
            }
            return ret;
        }
        return false;
    }

    private boolean isEmptyAttributes() {
        return getLibraryType() == null && getReadType() == null && getInsertSize() == null && StringHelper.isEmpty(getMultiplexing()) && getRegion() == null && getService() == null;
    }

    @Override
    public boolean isReadable() {
        return getOrder() != null && getOrder().isReadable();
    }

    public boolean isServiceArea(String serviceArea) {
        return getOrder().getServiceType().getServiceArea().getName().equalsIgnoreCase(serviceArea);
    }

    @Override
    public boolean isUpdatable() {
        return getOrder() != null && getOrder().isUpdatable() && hasNoDependents();
    }

    public void setAnalysisReasons(final Set<AnalysisReason> analysisReasons) {
        this.analysisReasons = analysisReasons;
    }

    public void setChargeable(boolean chargeable) {
        this.chargeable = chargeable;
    }

    public void setCharges(final Set<Charge> charges) {
        this.charges = charges;
    }

    public void setCoupledNotManagedHashCodesOrderItemsMap(Map<Integer, OrderItem> coupledNotManagedHashCodesOrderItemsMap) {
        this.coupledNotManagedHashCodesOrderItemsMap = coupledNotManagedHashCodesOrderItemsMap;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setInsertSize(Integer insertSize) {
        this.insertSize = insertSize;
    }

    public void setLibraryType(String libraryType) {
        this.libraryType = libraryType;
    }

    public void setMultiplexing(String multiplexing) {
        this.multiplexing = StringHelper.format(multiplexing);
    }

    public void setOldServiceOrderItemId(Long oldServiceOrderItemId) {
        this.oldServiceOrderItemId = oldServiceOrderItemId;
    }

    public void setOrder(final Order order) {
        this.order = order;
    }

    public void setPlate(Plate plate) {
        this.plate = plate;
    }

    public void setReadType(String readType) {
        this.readType = readType;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setRowStyleClassCoupled(String rowStyleClassCoupled) {
        this.rowStyleClassCoupled = rowStyleClassCoupled;
    }

    public void setRowTitleCoupled(String rowTitleCoupled) {
        this.rowTitleCoupled = rowTitleCoupled;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setTubeId(String tubeId) {
        this.tubeId = StringHelper.format(tubeId);
    }

    public void setTubeIdOld(String tubeIdOld) {
        this.tubeIdOld = tubeIdOld;
    }

    public void setWorkflows(Set<Workflow> workflows) {
        this.workflows = workflows;
    }
}