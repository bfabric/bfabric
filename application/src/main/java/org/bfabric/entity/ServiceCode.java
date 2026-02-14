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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "servicecode_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "ServiceCode.findByName", query = "SELECT a FROM ServiceCode a WHERE lower(a.name) = lower(:name)")
@NamedQuery(name = "ServiceCode.findEnabled", query = "SELECT a FROM ServiceCode a WHERE a.enabled = true ORDER BY a.name")
@NamedQuery(name = "ServiceCode.findEnabledIncluding", query = "SELECT a FROM ServiceCode a WHERE a.enabled = true or a = :entity ORDER BY a.name")
@NamedQuery(name = "ServiceCode.checkByName", query = "SELECT a FROM ServiceCode a WHERE lower(a.name) = lower(:name)")
public class ServiceCode extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "serviceCode")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "serviceCodeCostCentre", joinColumns = @JoinColumn(name = "servicecodeid"), inverseJoinColumns = @JoinColumn(name = "costcentreid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "costcentre")
    private Set<CostCentre> costCentres = new HashSet<>();

    @OneToMany(mappedBy = "serviceCode")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OfferedCharge> offeredCharges = new HashSet<>();

    @OneToMany(mappedBy = "serviceCode")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Service> services = new HashSet<>();

    public ServiceCode() {
        super();
    }

    @Override
    public ServiceCode clone() throws CloneNotSupportedException {
        ServiceCode clone = (ServiceCode) super.clone();
        clone.services = new HashSet<>();
        clone.charges = new HashSet<>();
        clone.offeredCharges = new HashSet<>();
        clone.links = new ArrayList<>();
        clone.costCentres = new HashSet<>(getCostCentres());
        return clone;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public Set<CostCentre> getCostCentres() {
        return costCentres;
    }

    public List<CostCentre> getCostCentresAsList() {
        return CollectionHelper.asList(costCentres);
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.SERVICEMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getCostCentres() != null && !getCostCentres().isEmpty()) {
            addEntityInfoItem(summary, "costCentres", CollectionHelper.print(getCostCentres(), "getDisplayName"));
        }
        return summary.toString();
    }

    @Override
    @Size(max = 16)
    public String getName() {
        return super.getName();
    }

    public Set<OfferedCharge> getOfferedCharges() {
        return offeredCharges;
    }

    public Set<Service> getServices() {
        return services;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getServices().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER);
    }

    public boolean isUpdatable() {
        return isReadable() && hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setCostCentres(Set<CostCentre> costCentres) {
        this.costCentres = costCentres;
    }

    public void setCostCentresAsList(List<CostCentre> costCentres) {
        this.costCentres = (Set<CostCentre>) CollectionHelper.asSet(costCentres);
    }
}