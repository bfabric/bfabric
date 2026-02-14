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
public class PageflowStep extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "pageflowStep", cascade = CascadeType.REMOVE)
    @OrderBy("id DESC")
    @XmlIDREF
    private Set<PageflowStepPosition> pageflowStepPositions = new HashSet<>();

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public Set<PageflowStepPosition> getPageflowStepPositions() {
        return pageflowStepPositions;
    }

    public List<Pageflow> getPageflows() {
        List<Pageflow> pageflows = new ArrayList<>();
        for (PageflowStepPosition pageflowStepPosition : getPageflowStepPositions()) {
            pageflows.add(pageflowStepPosition.getPageflow());
        }
        return pageflows;
    }

    public boolean isAssignSamples() {
        return getName().equals("assignSamples");
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isEditWorkunit() {
        return getName().equals("editWorkunit");
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.APPLICATIONREADER);
    }

    public boolean isSelectDataset() {
        return getName().equals("selectDataset");
    }

    public boolean isSelectImportResources() {
        return getName().equals("selectImportResources");
    }

    public boolean isSelectResources() {
        return getName().equals("selectResources");
    }

    @Override
    public boolean isUpdatable() {
        return getPageflowStepPositions().isEmpty();
    }

    public boolean isUploadWorkunit() {
        return getName().equals("uploadWorkunit");
    }

    public void setPageflowStepPositions(Set<PageflowStepPosition> pageflowStepPositions) {
        this.pageflowStepPositions = pageflowStepPositions;
    }
}