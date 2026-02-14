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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class Feedback extends AbstractDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledgedby")
    @XmlIDREF
    private User acknowledgedBy;

    @Column(name = "acknowledgeddate")
    private LocalDate acknowledgedDate;

    @OneToMany(mappedBy = "feedback", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<FeedbackAnswer> answers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "containerid")
    @XmlIDREF
    private Container container;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "templateid")
    @NotNull
    @XmlIDREF
    private FeedbackTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    @NotNull
    @XmlIDREF
    private User user;

    public Feedback() {
        super();
    }

    public void acknowledgeFeedback() {
        if (getAcknowledgedBy() == null) {
            setAcknowledgedBy(getCurrentUser());
            setAcknowledgedDate(LocalDate.now());
        }
    }

    @Override
    public Feedback clone() throws CloneNotSupportedException {
        return (Feedback) super.clone();
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        if (getUser() == null) {
            setUser(getCurrentUser());
        }
    }

    public User getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public LocalDate getAcknowledgedDate() {
        return acknowledgedDate;
    }

    public Set<FeedbackAnswer> getAnswers() {
        return answers;
    }

    public List<FeedbackAnswer> getAnswersOrderedList() {
        List<FeedbackAnswer> answersSortedList = new ArrayList<>(getAnswers());
        answersSortedList.sort(getOrderComparator());
        return answersSortedList;
    }

    @Override
    public Feedback getClone() {
        return (Feedback) super.getClone();
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.FEEDBACKMANAGER;
    }

    @Override
    public String getDisplayName() {
        if (getContainer() != null) {
            return getId() + " - " + getContainer().getEntityName() + " " + getContainer().getId();
        }
        return Long.toString(getId());
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getTemplate() != null) {
            addEntityInfoItem(summary, "template", getTemplate().getId());
        }
        if (getContainer() != null) {
            addEntityInfoItem(summary, "container", getContainer().getId());
        }
        if (getUser() != null) {
            addEntityInfoItem(summary, "user", getUser().getName());
        }
        return summary.toString();
    }

    public Comparator<FeedbackAnswer> getOrderComparator() {
        return Comparator.comparingLong(AbstractOrderedEntity::getOrderPosition);
    }

    public FeedbackTemplate getTemplate() {
        return template;
    }

    public User getUser() {
        return user;
    }

    public boolean isAcknowledgable() {
        return acknowledgedDate == null && hasCurrentUserRoleEnum(RoleEnum.FEEDBACKMANAGER);
    }

    public boolean isAcknowledged() {
        return acknowledgedDate != null;
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER) || getUser() != null && getUser().isIdentityUser();
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) && !isAcknowledged();
    }

    public void setAcknowledgedBy(User acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public void setAcknowledgedDate(LocalDate acknowledgedDate) {
        this.acknowledgedDate = acknowledgedDate;
    }

    public void setAnswers(Set<FeedbackAnswer> answers) {
        this.answers = answers;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setTemplate(FeedbackTemplate template) {
        this.template = template;
    }

    public void setUser(User user) {
        this.user = user;
    }
}