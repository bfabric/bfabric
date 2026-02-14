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
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "AgendaEventType")
@XmlRootElement
@NamedQuery(name = "EventType.findAllNonPublicOrderByColor", query = "SELECT a FROM EventType a WHERE a.publicEvent = FALSE ORDER BY a.color DESC, a.name")
@NamedQuery(name = "EventType.findAllNonPublicOrderByName", query = "SELECT a FROM EventType a WHERE a.publicEvent = FALSE ORDER BY a.name")
@NamedNativeQuery(name = "EventType.findAllGroupedByColor", query = "SELECT * FROM eventtypesgroupedbycolor")
public class EventType extends AbstractDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @XmlElement
    private boolean accountable = false;

    @NotBlank
    @Column(length = 9)
    @Size(max = 9)
    @XmlElement
    private String color = "#000000";

    @OneToMany(mappedBy = "eventType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Event> events = new HashSet<>();

    @NotNull
    @XmlElement
    private boolean freeEvent = false;

    @NotNull
    @XmlElement
    private boolean publicEvent = false;

    public EventType() {
        super();
    }

    public String getColor() {
        return color;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AGENDAMANAGER;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "accountable", isAccountable());
        addEntityInfoItem(summary, "freeEvent", isFreeEvent());
        addEntityInfoItem(summary, "publicEvent", isPublicEvent());
        if (StringHelper.isNotEmpty(getColor())) {
            addEntityInfoItem(summary, "color", getColor());
        }
        return summary.toString();
    }

    public Set<Event> getEvents() {
        return events;
    }

    @Override
    @Size(max = 64)
    public String getName() {
        return super.getName();
    }

    public boolean isAccountable() {
        return accountable;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getEvents().isEmpty();
    }

    @Override
    public boolean isExtensible() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isFreeEvent() {
        return freeEvent;
    }

    public boolean isPublicEvent() {
        return publicEvent;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.AGENDAUSER);
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setAccountable(boolean accountable) {
        this.accountable = accountable;
    }

    public void setColor(String color) {
        this.color = StringHelper.format(color);
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    public void setFreeEvent(boolean freeEvent) {
        this.freeEvent = freeEvent;
    }

    public void setPublicEvent(boolean publicEvent) {
        this.publicEvent = publicEvent;
    }
}
