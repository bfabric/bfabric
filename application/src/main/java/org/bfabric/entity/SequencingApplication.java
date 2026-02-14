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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "sequencingapplication_name_unique", columnNames = { "name", "servicetypeid" }) })
@XmlRootElement
@NamedQuery(name = "SequencingApplication.findEnabledIncludingByServiceType", query = "SELECT a FROM SequencingApplication a WHERE a.enabled = true and a.serviceType = :serviceType or a = :entity ORDER BY a.orderPosition")
@NamedQuery(name = "SequencingApplication.findEnabledIncludingExcludingByServiceType", query = "SELECT a FROM SequencingApplication a WHERE a.enabled = true and a.name <> :serviceTypeNameExcluding and a.serviceType = :serviceType or a = :entity and a.name <> :serviceTypeNameExcluding ORDER BY a.orderPosition")
@NamedQuery(name = "SequencingApplication.checkUniqueName", query = "SELECT a.id FROM SequencingApplication a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.serviceType = :serviceType")
public class SequencingApplication extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "sequencingApplication")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "sequencingApplication")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<SequencingApplicationIndexLength> sequencingApplicationIndexLengths = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocolsequencingapplication", joinColumns = @JoinColumn(name = "sequencingapplicationid"), inverseJoinColumns = @JoinColumn(name = "samplepreparationprotocolid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<SamplePreparationProtocol> samplePreparationProtocols = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicetypeid")
    @NotNull
    @XmlIDREF
    private ServiceType serviceType;

    public SequencingApplication() {
        super();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getServiceType() != null) {
            addEntityInfoItem(summary, "serviceType", getServiceType().getDisplayName());
        }
        return summary.toString();
    }

    @Override
    public String getGroupingAttributes() {
        return getServiceType().getName();
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Set<SamplePreparationProtocol> getSamplePreparationProtocols() {
        return samplePreparationProtocols;
    }

    public Set<SequencingApplicationIndexLength> getSequencingApplicationIndexLengths() {
        return sequencingApplicationIndexLengths;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getSamplePreparationProtocols().isEmpty() && getSequencingApplicationIndexLengths().isEmpty() && getOrders().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || hasCurrentUserRoleEnum(RoleEnum.USER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
}