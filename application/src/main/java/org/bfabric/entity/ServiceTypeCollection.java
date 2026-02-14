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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "servicetypecollection_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "ServiceTypeCollection.findByName", query = "SELECT a FROM ServiceTypeCollection a WHERE lower(a.name) = lower(:name)")
@NamedQuery(name = "ServiceTypeCollection.findByServiceTypes", query = "SELECT DISTINCT stl FROM ServiceTypeCollection stl JOIN stl.serviceTypes st WHERE st IN (:serviceTypes) ORDER BY stl.name")
@NamedQuery(name = "ServiceTypeCollection.findEnabled", query = "SELECT a FROM ServiceTypeCollection a WHERE a.enabled = true ORDER BY a.name")
@NamedQuery(name = "ServiceTypeCollection.checkUniqueName", query = "SELECT a.id FROM ServiceTypeCollection a WHERE lower(a.name) = lower(:name) and a.id <> :id")
public class ServiceTypeCollection extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "ServiceTypeCollectionServiceType", joinColumns = @JoinColumn(name = "ServiceTypeCollectionId"), inverseJoinColumns = @JoinColumn(name = "ServiceTypeId"))
    @OrderBy("name asc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceType> serviceTypes = new HashSet<>();

    public ServiceTypeCollection() {
        super();
    }

    @Override
    public ServiceTypeCollection clone() throws CloneNotSupportedException {
        final ServiceTypeCollection clone = (ServiceTypeCollection) super.clone();
        clone.serviceTypes = new HashSet<>();
        clone.serviceTypes.addAll(getServiceTypes());
        return clone;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.SERVICEMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getServiceTypes() != null && !getServiceTypes().isEmpty()) {
            addEntityInfoItem(summary, "serviceTypes", getServiceTypes().size());
        }
        return summary.toString();
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public List<ServiceType> getServiceTypesAsList() {
        return new ArrayList<>(getServiceTypes());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER);
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setServiceTypes(Set<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setServiceTypesAsList(List<ServiceType> serviceTypes) {
        this.serviceTypes = new HashSet<>(serviceTypes);
    }
}