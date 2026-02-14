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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "StorageModel.checkUniqueName", query = "SELECT a.id FROM StorageModel a WHERE lower(a.name) = lower(:name) and a.id <> :id")
public class StorageModel extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "storageModel")
    @OrderBy("id desc")
    private Set<Container> containers = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean dataDeliveryOnly;

    public StorageModel() {
        super();
    }

    public Set<Container> getContainers() {
        return containers;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.SERVICEMANAGER;
    }

    public String getFullName() {
        return getName() + (StringHelper.isNotEmpty(getDescription()) ? " (" + getDescription() + ")" : "");
    }

    public boolean isDataDeliveryOnly() {
        return dataDeliveryOnly;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getContainers().isEmpty();
    }

    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

    public void setDataDeliveryOnly(boolean dataDeliverOnly) {
        this.dataDeliveryOnly = dataDeliverOnly;
    }
}