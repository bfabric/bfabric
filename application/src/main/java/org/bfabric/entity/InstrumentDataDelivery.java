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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "instrumentdatadelivery_name_unique", columnNames = { "name", "instrumentid" }) })
@XmlRootElement
@NamedQuery(name = "InstrumentDataDelivery.checkUniqueName", query = "SELECT a.id FROM InstrumentDataDelivery a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.instrument = :instrument")
public class InstrumentDataDelivery extends AbstractOrderedInstrumentDependentEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Size(max = 1024)
    @XmlElement
    protected String hint;

    @OneToMany(mappedBy = "instrumentDataDelivery")
    @Where(clause = "discriminator = 'Order'")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    public InstrumentDataDelivery() {
        super();
    }

    @Override
    public InstrumentDataDelivery clone() throws CloneNotSupportedException {
        InstrumentDataDelivery clone = (InstrumentDataDelivery) super.clone();
        clone.orders = new HashSet<>();
        return clone;
    }

    @Override
    public InstrumentDataDelivery getClone() {
        return (InstrumentDataDelivery) super.getClone();
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

    public Set<Order> getOrders() {
        return orders;
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
}