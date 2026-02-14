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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class Review extends AbstractComment {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean approved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectid")
    @NotNull
    @XmlIDREF
    private Project project;

    public Review() {
    }

    public String getApprovedMarker() {
        return isApproved() ? Constants.REVIEW_APPROVED : Constants.REVIEW_REJECTED;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.REVIEWER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getApprovedMarker())) {
            addEntityInfoItem(summary, "vote", getApprovedMarker());
        }
        if (StringHelper.isNotEmpty(getComment())) {
            addEntityInfoItem(summary, "comment", getComment());
        }
        return summary.toString();
    }

    public Project getProject() {
        return project;
    }

    public boolean isApproved() {
        return approved;
    }

    @Override
    public boolean isCreatable() {
        return getProject() != null && getProject().isReviewable() && hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isLastReview() {
        Review lastReview = getProject().getReviews().get(0);
        return lastReview != null && lastReview.equals(this);
    }

    public boolean isLastReviewAndCreatedByUser() {
        return isCreator() && isLastReview();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatable() {
        return getProject() != null && getProject().isReviewable() && (isLastReviewAndCreatedByUser() || hasCurrentUserRoleEnum(RoleEnum.ADMIN));
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
