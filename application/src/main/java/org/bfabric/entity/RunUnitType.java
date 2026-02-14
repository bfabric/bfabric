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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "rununittype_name_unique", columnNames = { "name", "instrumentid" }) })
@XmlRootElement
@NamedQuery(name = "RunUnitType.checkUniqueName", query = "SELECT a.id FROM RunUnitType a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.instrument = :instrument")
public class RunUnitType extends AbstractOrderedInstrumentDependentEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "runUnitType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<RunUnit> runUnits = new HashSet<>();

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal capacity;

    @OneToMany(mappedBy = "runUnitType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentDataPackage> instrumentDataPackages = new HashSet<>();

    @NotNull
    @Min(1)
    @XmlElement
    private Integer numberOfLanes;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean physicalSeparation = false;

    public RunUnitType() {
        super();
    }

    @Override
    public RunUnitType clone() throws CloneNotSupportedException {
        RunUnitType clone = (RunUnitType) super.clone();
        instrumentDataPackages = new HashSet<>();
        return clone;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "capacity", getCapacity());
        addEntityInfoItem(summary, "numberOfLanes", getNumberOfLanes());
        return summary.toString();
    }

    @Override
    public String getGroupingAttributes() {
        return getInstrument().getName();
    }

    public Set<InstrumentDataPackage> getInstrumentDataPackages() {
        return instrumentDataPackages;
    }

    public Integer getNumberOfLanes() {
        return numberOfLanes;
    }

    public Set<RunUnit> getRunUnits() {
        return runUnits;
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getRunUnits().isEmpty() && getInstrumentDataPackages().isEmpty();
    }

    public boolean isPhysicalSeparation() {
        return physicalSeparation;
    }

    @Override
    public boolean isReadable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.RUNREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setCapacity(BigDecimal capacity) {
        this.capacity = capacity;
    }

    public void setInstrumentDataPackages(Set<InstrumentDataPackage> instrumentDataPackages) {
        this.instrumentDataPackages = instrumentDataPackages;
    }

    public void setNumberOfLanes(Integer numberOfLanes) {
        this.numberOfLanes = numberOfLanes;
    }

    public void setPhysicalSeparation(boolean physicalSeparation) {
        this.physicalSeparation = physicalSeparation;
    }
}