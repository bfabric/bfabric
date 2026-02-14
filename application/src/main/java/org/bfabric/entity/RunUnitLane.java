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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class RunUnitLane extends AbstractBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @Min(1)
    @XmlElement
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rununitid")
    @NotNull
    @XmlIDREF
    private RunUnit runUnit;

    @ManyToMany
    @JoinTable(name = "rununitlanesample", joinColumns = @JoinColumn(name = "rununitlaneid"), inverseJoinColumns = @JoinColumn(name = "sampleid"))
    @OrderBy(value = "id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "sample")
    private Set<Sample> samples = new HashSet<>();

    @Column(columnDefinition = "integer DEFAULT 0")
    @NotNull
    @XmlElement
    private int unassignedReads;

    public RunUnitLane() {
        super();
    }

    public RunUnitLane(RunUnit runUnit, Integer position) {
        super();
        if (runUnit != null) {
            setRunUnit(runUnit);
            setPosition(position);
        }
    }

    public RunUnitLane clone() throws CloneNotSupportedException {
        RunUnitLane clone = (RunUnitLane) super.clone();
        clone.samples = new HashSet<>();
        return clone;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    public Integer getPosition() {
        return position;
    }

    public RunUnit getRunUnit() {
        return runUnit;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public int getUnassignedReads() {
        return unassignedReads;
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
    public boolean isReadable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.RUNREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return super.isUpdatable() && getRunUnit().getRun() != null && !getRunUnit().getRun().isFinished();
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setRunUnit(RunUnit runUnit) {
        this.runUnit = runUnit;
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void setUnassignedReads(int unassignedReads) {
        this.unassignedReads = unassignedReads;
    }

}