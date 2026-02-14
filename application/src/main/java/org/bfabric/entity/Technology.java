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
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "technology_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "Technology.findByEnabled", query = "SELECT a FROM Technology a WHERE a.enabled = true ORDER BY a.name")
@NamedQuery(name = "Technology.findByEnabledIncludingTechnologies", query = "SELECT a FROM Technology a WHERE a.enabled = true or a in :technologies ORDER BY a.name")
@NamedQuery(name = "Technology.findAllOrderByPosition", query = "SELECT a FROM Technology a ORDER BY a.orderPosition")
@NamedQuery(name = "Technology.havingOrders", query = "SELECT DISTINCT technology FROM Order o JOIN o.technologies technology ORDER BY technology.name")
@NamedQuery(name = "Technology.havingProjects", query = "SELECT DISTINCT technology FROM Project p JOIN p.technologies technology ORDER BY technology.name")
public class Technology extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen, Mergeable {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "applicationtechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "applicationid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> applications = new HashSet<>();

    @Column(length = 9)
    @Size(max = 9)
    @XmlElement
    private String color;

    @ManyToMany
    @JoinTable(name = "containertechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "containerid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> containers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "contracttechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "contractid"))
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Contract> contracts = new HashSet<>();

    @OneToMany(mappedBy = "technology")
    @OrderBy("lastName, firstName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> employees = new HashSet<>();

    @OneToMany(mappedBy = "technologyHead")
    @OrderBy("lastName, firstName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> heads = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "instrumenttechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "instrumentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> instruments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "purchasetechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "purchaseid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> purchases = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocoltechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "samplepreparationprotocolid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<SamplePreparationProtocol> samplePreparationProtocols = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "servicetypetechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypes = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "storagetechnology", joinColumns = @JoinColumn(name = "technologyid"), inverseJoinColumns = @JoinColumn(name = "storageid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Storage> storages = new HashSet<>();

    @OneToMany(mappedBy = "defaultTechnology")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> users = new HashSet<>();

    public Technology() {
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public String getColor() {
        return color;
    }

    public Set<Container> getContainers() {
        return containers;
    }

    public Set<Contract> getContracts() {
        return contracts;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.TECHNOLOGYMANAGER;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    public Set<User> getEmployees() {
        return employees;
    }

    public Set<User> getHeads() {
        return heads;
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }

    public Set<Purchase> getPurchases() {
        return purchases;
    }

    public Set<SamplePreparationProtocol> getSamplePreparationProtocols() {
        return samplePreparationProtocols;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public Set<Storage> getStorages() {
        return storages;
    }

    public Set<User> getUsers() {
        return users;
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntities(getContainers());
        IndexHelper.indexEntities(getSamplePreparationProtocols());
        IndexHelper.indexEntities(getInstruments());
        if (!getApplications().isEmpty()) {
            Set<Workunit> workunits = new HashSet<>();
            for (Application application : getApplications()) {
                if (!application.getWorkunits().isEmpty()) {
                    workunits.addAll(application.getWorkunits());
                }
            }
            IndexHelper.indexEntities(workunits);
        }
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getStorages().isEmpty() && getHeads().isEmpty() && getEmployees().isEmpty() && getApplications().isEmpty() && getInstruments().isEmpty() && getContainers().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.TECHNOLOGYREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setColor(String color) {
        this.color = StringHelper.format(color);
    }

    public void setContracts(Set<Contract> contracts) {
        this.contracts = contracts;
    }

    public void setPurchases(Set<Purchase> purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return name;
    }
}
