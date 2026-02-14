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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class OrderAttribute extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotBlank
    @Size(max = 1024)
    @XmlElement
    protected String hint;

    @NotBlank
    @Size(max = Constants.MAX_LENGTH_NAME)
    @XmlElement
    protected String label;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optionalCounterpartid")
    private OrderAttribute optionalCounterpart;

    @ManyToMany
    @JoinTable(name = "orderattributeorder", joinColumns = @JoinColumn(name = "orderattributeid"), inverseJoinColumns = @JoinColumn(name = "orderid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "order")
    private Set<Order> orders = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "orderattributeservicetype", joinColumns = @JoinColumn(name = "orderattributeid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "servicetype")
    private Set<ServiceType> serviceTypes = new HashSet<>();

    public OrderAttribute() {
        super();
    }

    public String getAttributeName() {
        return getName().replace("Optional", Constants.EMPTY_STRING);
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getLabel())) {
            addEntityInfoItem(summary, "label", getLabel());
        }
        if (StringHelper.isNotEmpty(getHint())) {
            addEntityInfoItem(summary, "hint", getHint());
        }
        return summary.toString();
    }

    public String getHint() {
        return hint;
    }

    public String getLabel() {
        return label;
    }

    public OrderAttribute getOptionalCounterpart() {
        return optionalCounterpart;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public List<ServiceType> getServiceTypesAsList() {
        return CollectionHelper.asList(serviceTypes);
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    @Override
    public boolean isDeletable() {
        return isCreatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER);
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    protected void preRemove() {
        super.preRemove();
        if (getOptionalCounterpart() != null) {
            getOptionalCounterpart().setOptionalCounterpart(null);
        }
    }

    public void setHint(String hint) {
        this.hint = StringHelper.format(hint);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setOptionalCounterpart(OrderAttribute optionalCounterpart) {
        this.optionalCounterpart = optionalCounterpart;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setServiceTypes(Set<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setServiceTypesAsList(final List<ServiceType> serviceTypes) {
        setServiceTypes((Set<ServiceType>) CollectionHelper.asSet(serviceTypes));
    }
}