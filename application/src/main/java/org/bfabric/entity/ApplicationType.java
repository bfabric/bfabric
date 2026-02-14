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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@XmlRootElement
public class ApplicationType extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "applicationType", cascade = CascadeType.REMOVE)
    @OrderBy("id DESC")
    @XmlIDREF
    private Set<ApplicationTypePageflowPosition> applicationTypePageflowPositions = new HashSet<>();

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "applicationType")
    @OrderBy("id desc")
    private Set<Application> applications = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defaultPageflowId")
    @XmlIDREF
    private Pageflow defaultPageflow;

    public Set<ApplicationTypePageflowPosition> getApplicationTypePageflowPositions() {
        return applicationTypePageflowPositions;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public Pageflow getDefaultPageflow() {
        return defaultPageflow;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItems(summary, getCustomAttributes());
        return summary.toString();
    }

    public List<Pageflow> getPageflows() {
        List<Pageflow> pageflows = new ArrayList<>();
        for (ApplicationTypePageflowPosition applicationTypePageflowPosition : getApplicationTypePageflowPositions()) {
            pageflows.add(applicationTypePageflowPosition.getPageflow());
        }
        return pageflows;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.APPLICATIONREADER);
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && getApplications().isEmpty();
    }

    public void setApplicationTypePageflowPositions(Set<ApplicationTypePageflowPosition> applicationTypePageflowPositions) {
        this.applicationTypePageflowPositions = applicationTypePageflowPositions;
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public void setDefaultPageflow(Pageflow pageflow) {
        this.defaultPageflow = pageflow;
    }
}