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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class InstrumentEventType extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "instrumentEventType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentEvent> instrumentEvents = new HashSet<>();

    @XmlElement
    private boolean userCreatable = false;

    @XmlElement
    private boolean userVisible = false;

    public InstrumentEventType() {
        super();
    }

    @Override
    public InstrumentEventType clone() throws CloneNotSupportedException {
        InstrumentEventType clone = (InstrumentEventType) super.clone();
        clone.instrumentEvents = new HashSet<>();
        return clone;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "userVisible", isUserVisible());
        addEntityInfoItem(summary, "userCreatable", isUserCreatable());
        return summary.toString();
    }

    public Set<InstrumentEvent> getInstrumentEvents() {
        return instrumentEvents;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getInstrumentEvents().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.INSTRUMENTREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public boolean isUserCreatable() {
        return userCreatable;
    }

    public boolean isUserVisible() {
        return userVisible;
    }

    public void setUserCreatable(boolean userCreatable) {
        this.userCreatable = userCreatable;
    }

    public void setUserVisible(boolean userVisible) {
        this.userVisible = userVisible;
    }
}