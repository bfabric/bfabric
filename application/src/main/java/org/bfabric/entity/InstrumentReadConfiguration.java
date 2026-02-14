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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "instrumentreadconfiguration_name_unique", columnNames = { "name", "instrumentid" }) })
@XmlRootElement
@NamedQuery(name = "InstrumentReadConfiguration.checkUniqueName", query = "SELECT a.id FROM InstrumentReadConfiguration a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.instrument = :instrument")
public class InstrumentReadConfiguration extends AbstractOrderedInstrumentDependentEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Size(max = 1024)
    @XmlElement
    protected String hint;

    @OneToMany(mappedBy = "instrumentReadConfiguration")
    @Where(clause = "discriminator = 'Order'")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "instrumentReadConfiguration")
    @Where(clause = "discriminator = 'Project'")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Project> projects = new HashSet<>();

    @OneToMany(mappedBy = "instrumentReadConfiguration")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Run> runs = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocolinstrumentreadconfiguration", joinColumns = @JoinColumn(name = "instrumentreadconfigurationid"), inverseJoinColumns = @JoinColumn(name = "samplepreparationprotocolid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<SamplePreparationProtocol> samplePreparationProtocols = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumentreadconfigurationservicetypedisabled", joinColumns = @JoinColumn(name = "instrumentreadconfigurationid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("id desc")
    @XmlIDREF
    @XmlElement(name = "servicetypedisabled")
    private Set<ServiceType> serviceTypesDisabled = new HashSet<>();

    public InstrumentReadConfiguration() {
        super();
    }

    @Override
    public InstrumentReadConfiguration clone() throws CloneNotSupportedException {
        InstrumentReadConfiguration clone = (InstrumentReadConfiguration) super.clone();
        clone.orders = new HashSet<>();
        clone.projects = new HashSet<>();
        clone.runs = new HashSet<>();
        return clone;
    }

    @Override
    public InstrumentReadConfiguration getClone() {
        return (InstrumentReadConfiguration) super.getClone();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getHint())) {
            addEntityInfoItem(summary, "hint", getHint());
        }
        return summary.toString();
    }

    @Override
    public String getGroupingAttributes() {
        return getInstrument().getName();
    }

    public String getHint() {
        return hint;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Set<Run> getRuns() {
        return runs;
    }

    public Set<SamplePreparationProtocol> getSamplePreparationProtocols() {
        return samplePreparationProtocols;
    }

    public List<SamplePreparationProtocol> getSamplePreparationProtocolsAsList() {
        return new java.util.ArrayList<>(samplePreparationProtocols);
    }

    public String getSamplePreparationProtocolsDisplayNames() {
        return getSamplePreparationProtocols().stream()
            .map(SamplePreparationProtocol::getDisplayName)
            .collect(Collectors.joining(", "));
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

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public void setRuns(Set<Run> runs) {
        this.runs = runs;
    }

    public void setSamplePreparationProtocols(Set<SamplePreparationProtocol> samplePreparationProtocols) {
        this.samplePreparationProtocols = samplePreparationProtocols;
    }

    public void setSamplePreparationProtocolsAsList(List<SamplePreparationProtocol> protocols) {
        this.samplePreparationProtocols = new HashSet<>(protocols);
    }

    public void setServiceTypesDisabled(Set<ServiceType> serviceTypesDisabled) {
        this.serviceTypesDisabled = serviceTypesDisabled;
    }
}