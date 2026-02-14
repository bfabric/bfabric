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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "instrumentdatapackage_name_unique", columnNames = { "name", "instrumentid" }) })
@XmlRootElement
@NamedQuery(name = "InstrumentDataPackage.checkUniqueName", query = "SELECT a.id FROM InstrumentDataPackage a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.instrument = :instrument")
public class InstrumentDataPackage extends AbstractOrderedInstrumentDependentEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Size(max = 1024)
    @XmlElement
    protected String hint;

    @XmlElement
    private Integer numberOfLanes;

    @Size(max = 32)
    private String numberOfReads;

    @OneToMany(mappedBy = "instrumentDataPackage")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rununittypeid")
    @XmlIDREF
    private RunUnitType runUnitType;

    @ManyToMany
    @JoinTable(name = "instrumentdatapackageservicetypedisabled", joinColumns = @JoinColumn(name = "instrumentdatapackageid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("id desc")
    @XmlIDREF
    @XmlElement(name = "servicetypedisabled")
    private Set<ServiceType> serviceTypesDisabled = new HashSet<>();

    public InstrumentDataPackage() {
        super();
    }

    @Override
    public InstrumentDataPackage clone() throws CloneNotSupportedException {
        InstrumentDataPackage clone = (InstrumentDataPackage) super.clone();
        clone.orders = new HashSet<>();
        return clone;
    }

    @Override
    public InstrumentDataPackage getClone() {
        return (InstrumentDataPackage) super.getClone();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    @Override
    public String getGroupingAttributes() {
        return getInstrument().getName();
    }

    public String getHint() {
        return hint;
    }

    public Integer getNumberOfLanes() {
        return numberOfLanes;
    }

    public String getNumberOfReads() {
        return numberOfReads;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public RunUnitType getRunUnitType() {
        return runUnitType;
    }

    public Set<ServiceType> getServiceTypes() {
        Set<ServiceType> serviceTypes = new HashSet<>();
        if (getInstrument() != null) {
            serviceTypes.addAll(getInstrument().getServiceTypes());
        }
        if (getServiceTypesDisabled() != null && !getServiceTypesDisabled().isEmpty()) {
            serviceTypes.removeAll(getServiceTypesDisabled());
        }
        return serviceTypes;
    }

    public Set<ServiceType> getServiceTypesDisabled() {
        return serviceTypesDisabled;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getOrders().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || hasCurrentUserRoleEnum(RoleEnum.USER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setHint(String hint) {
        this.hint = StringHelper.format(hint);
    }

    public void setNumberOfLanes(Integer numberOfLanes) {
        this.numberOfLanes = numberOfLanes;
    }

    public void setNumberOfReads(String numberOfReads) {
        this.numberOfReads = StringHelper.format(numberOfReads);
    }

    public void setRunUnitType(RunUnitType runUnitType) {
        this.runUnitType = runUnitType;
    }

    public void setServiceTypesDisabled(Set<ServiceType> serviceTypesDisabled) {
        this.serviceTypesDisabled = serviceTypesDisabled;
    }
}
