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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
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
@XmlRootElement
public class SequencingApplicationIndexLength extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Size(max = 1024)
    @XmlElement
    protected String hint;

    @OneToMany(mappedBy = "sequencingApplicationIndexLength")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequencingApplicationid")
    @NotNull
    @XmlIDREF
    private SequencingApplication sequencingApplication;

    public SequencingApplicationIndexLength() {
        super();
    }

    @Override
    public SequencingApplicationIndexLength clone() throws CloneNotSupportedException {
        SequencingApplicationIndexLength clone = (SequencingApplicationIndexLength) super.clone();
        clone.orders = new HashSet<>();
        return clone;
    }

    @Override
    public SequencingApplicationIndexLength getClone() {
        return (SequencingApplicationIndexLength) super.getClone();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getSequencingApplication() != null) {
            addEntityInfoItem(summary, "sequencingApplication", getSequencingApplication().getDisplayName());
        }
        return summary.toString();
    }

    @Override
    public String getGroupingAttributes() {
        return getSequencingApplication().getName();
    }

    public String getHint() {
        return hint;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public SequencingApplication getSequencingApplication() {
        return sequencingApplication;
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

    public void setSequencingApplication(SequencingApplication sequencingApplication) {
        this.sequencingApplication = sequencingApplication;
    }
}
