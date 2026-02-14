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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class RunUnit extends AbstractDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Size(max = 64)
    @XmlElement
    private String cellId;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean physicalSeparation = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runid")
    @XmlIDREF
    private Run run;

    @NotEmpty
    @OneToMany(mappedBy = "runUnit", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE })
    @OrderBy("position")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<RunUnitLane> runUnitLanes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rununittypeid")
    @NotNull
    @XmlIDREF
    private RunUnitType runUnitType;

    @Transient
    private boolean showSamplesLaneSeparated = true;

    public RunUnit() {
        super();
    }

    public RunUnit(RunUnitType runUnitType, Run run) {
        super();
        if (runUnitType != null) {
            setRunUnitType(runUnitType);
            for (int i = 1; i <= runUnitType.getNumberOfLanes(); i++) {
                getRunUnitLanes().add(new RunUnitLane(this, i));
            }

            setName(runUnitType.getName());

            if (run != null) {
                if (StringHelper.isEmpty(getName())) {
                    setName(run.getName());
                }
                setRun(run);
                run.setRunUnit(this);
            }
        }
    }

    @Override
    public RunUnit clone() throws CloneNotSupportedException {
        RunUnit clone = (RunUnit) super.clone();
        clone.runUnitLanes = new HashSet<>();
        Map<Long, Sample> sampleCloneMap = new HashMap<>();
        for (Sample sample : getSamples()) {
            Sample sampleClone = sample.clone(true);
            sampleClone.setReadCount(null);
            sampleClone.setReadCountTotal(null);
            sampleCloneMap.put(sample.getId(), sampleClone);
        }
        for (RunUnitLane runUnitLane : getRunUnitLanes()) {
            RunUnitLane runUnitLaneClone = runUnitLane.clone();
            runUnitLaneClone.setRunUnit(clone);
            clone.getRunUnitLanes().add(runUnitLaneClone);
            for (Sample sample : runUnitLane.getSamples()) {
                runUnitLaneClone.getSamples().add(sampleCloneMap.get(sample.getId()));
            }
        }
        return clone;
    }

    public String getCellId() {
        return cellId;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getRunUnitType() != null) {
            addEntityInfoItem(summary, "runUnitType", getRunUnitType().getDisplayName());
        }
        if (getRunUnitLanes() != null) {
            addEntityInfoItem(summary, "numberOfLanes", getRunUnitLanes().size());
        }
        if (getRun() != null) {
            addEntityInfoItem(summary, "run", getRun().getDisplayName());
        }
        return summary.toString();
    }

    public Run getRun() {
        return run;
    }

    public Set<RunUnitLane> getRunUnitLanes() {
        return runUnitLanes;
    }

    public RunUnitType getRunUnitType() {
        return runUnitType;
    }

    public List<Sample> getSamples() {
        Set<Sample> samples = new HashSet<>();
        for (RunUnitLane runUnitLane : getRunUnitLanes()) {
            samples.addAll(runUnitLane.getSamples());
        }
        return CollectionHelper.sortObjects(samples);
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isCreatableWS() {
        return false;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getRun() == null;
    }

    @Override
    public boolean isEditable() {
        return super.isEditable() && hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    public boolean isLaneSeparated() {
        return getRunUnitLanes().size() > 1;
    }

    public boolean isPhysicalSeparation() {
        return physicalSeparation;
    }

    @Override
    public boolean isReadable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.RUNREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    public boolean isShowSamplesLaneSeparated() {
        return showSamplesLaneSeparated;
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable() && getRun() == null || !getRun().isFinished();
    }

    public void setCellId(String cellId) {
        this.cellId = StringHelper.format(cellId);
    }

    public void setPhysicalSeparation(boolean physicalSeparation) {
        this.physicalSeparation = physicalSeparation;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public void setRunUnitLanes(Set<RunUnitLane> runUnitLanes) {
        this.runUnitLanes = runUnitLanes;
    }

    public void setRunUnitType(RunUnitType runUnitType) {
        this.runUnitType = runUnitType;
    }

    public void setShowSamplesLaneSeparated(boolean showSamplesLaneSeparated) {
        this.showSamplesLaneSeparated = showSamplesLaneSeparated;
    }

    public void switchLaneRepresentation() {
        setShowSamplesLaneSeparated(!isShowSamplesLaneSeparated());
    }
}