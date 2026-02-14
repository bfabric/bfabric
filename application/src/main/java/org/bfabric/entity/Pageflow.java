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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@XmlRootElement
public class Pageflow extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "pageflow", cascade = CascadeType.REMOVE)
    @OrderBy("id DESC")
    @XmlIDREF
    private Set<ApplicationTypePageflowPosition> applicationTypePageflowPositions = new HashSet<>();

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "pageflow")
    @OrderBy("id desc")
    private Set<Application> applications = new HashSet<>();

    @OneToMany(mappedBy = "defaultPageflow")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ApplicationType> defaultApplicationTypes = new HashSet<>();

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "pageflow", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("position")
    @XmlIDREF
    private Set<PageflowStepPosition> pageflowStepPositions = new HashSet<>();

    @OneToMany(mappedBy = "pageflow")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<PageflowStepPosition> pageflowStepPositionsReverseOrdered = new ArrayList<>();

    @Transient
    private PageflowStep selectedPageflowStep;

    public void addStep(PageflowStep pageflowStep) {
        getPageflowStepPositions().add(new PageflowStepPosition(this, pageflowStep, getNextAvailablePosition()));
    }

    public Set<ApplicationTypePageflowPosition> getApplicationTypePageflowPositions() {
        return applicationTypePageflowPositions;
    }

    public List<ApplicationType> getApplicationTypes() {
        List<ApplicationType> applicationTypes = new ArrayList<>();
        for (ApplicationTypePageflowPosition applicationTypePageflowPosition : getApplicationTypePageflowPositions()) {
            applicationTypes.add(applicationTypePageflowPosition.getApplicationType());
        }
        return applicationTypes;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public Set<ApplicationType> getDefaultApplicationTypes() {
        return defaultApplicationTypes;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getPageflowSteps() != null && !getPageflowSteps().isEmpty()) {
            addEntityInfoItem(summary, "pageflowSteps", getPageflowSteps().size());
        }
        addEntityInfoItems(summary, getCustomAttributes());
        return summary.toString();
    }

    public PageflowStep getFirstPageflowStep() {
        return getPageflowStepPositions().stream().findFirst().map(PageflowStepPosition::getPageflowStep).orElse(null);

    }

    public PageflowStepPosition getLastPageflowStepPosition() {
        return !pageflowStepPositionsReverseOrdered.isEmpty() ? pageflowStepPositionsReverseOrdered.get(0) : null;
    }

    public int getNextAvailablePosition() {
        return getLastPageflowStepPosition() != null ? getLastPageflowStepPosition().getPosition() + 1 : 1;
    }

    public PageflowStep getNextPageflowStep(PageflowStep pageflowStep, boolean next) {
        if (pageflowStep != null) {
            List<PageflowStepPosition> stepPositions = CollectionHelper.asList(getPageflowStepPositions());
            int pageflowStepIndex = 0;
            for (PageflowStepPosition pageflowStepPosition : stepPositions) {
                if (pageflowStepPosition.getPageflowStep().equals(pageflowStep)) {
                    break;
                }
                pageflowStepIndex++;
            }
            if (stepPositions.size() > 1 && pageflowStepIndex != -1) {
                if (next && pageflowStepIndex < stepPositions.size() - 1) {
                    return stepPositions.get(pageflowStepIndex + 1).getPageflowStep();
                } else if (pageflowStepIndex > 0) {
                    return stepPositions.get(pageflowStepIndex - 1).getPageflowStep();
                }
            }
        }
        return null;
    }

    public PageflowStep getNextPageflowStep(PageflowStep pageflowStep) {
        return pageflowStep != null ? getNextPageflowStep(pageflowStep, true) : getFirstPageflowStep();
    }

    public Set<PageflowStepPosition> getPageflowStepPositions() {
        return pageflowStepPositions;
    }

    public List<PageflowStep> getPageflowSteps() {
        List<PageflowStep> pageflowSteps = new ArrayList<>();
        for (PageflowStepPosition pageflowStepPosition : getPageflowStepPositions()) {
            pageflowSteps.add(pageflowStepPosition.getPageflowStep());
        }
        return pageflowSteps;
    }

    public PageflowStep getPreviousPageflowStep(PageflowStep pageflowStep) {
        return pageflowStep != null ? getNextPageflowStep(pageflowStep, false) : getFirstPageflowStep();
    }

    public PageflowStep getSelectedPageflowStep() {
        return selectedPageflowStep;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isDataset() {
        return getName().equals(Constants.DATASET);
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.APPLICATIONREADER);
    }

    public boolean isResource() {
        return getName().equals("resource");
    }

    @Override
    public boolean isUpdatable() {
        return getApplications().isEmpty();
    }

    public void removeLastStep() {
        if (!pageflowStepPositionsReverseOrdered.isEmpty()) {
            removePageflowStepPosition(pageflowStepPositionsReverseOrdered.get(0));
        }
    }

    public void removePageflowStepPosition(PageflowStepPosition pageflowStepPosition) {
        if (pageflowStepPosition != null) {
            getPageflowStepPositions().remove(pageflowStepPosition);
        }
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public void setSelectedPageflowStep(PageflowStep selectedPageflowStep) {
        this.selectedPageflowStep = selectedPageflowStep;
    }
}