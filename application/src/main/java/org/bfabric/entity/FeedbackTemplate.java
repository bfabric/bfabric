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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "FeedbackTemplate.findEnabled", query = "SELECT a FROM FeedbackTemplate a WHERE a.enabled = true ORDER BY a.name")
@NamedQuery(name = "FeedbackTemplate.defaultForType", query = "SELECT a FROM FeedbackTemplate a WHERE lower(a.feedbackTemplateType.name) = lower(:feedbackTemplateTypeName) and a.defaultForType = true and a.enabled = true")
@NamedQuery(name = "FeedbackTemplate.resetDefaultForType", query = "UPDATE FeedbackTemplate a SET a.defaultForType = false WHERE a.id <> :id AND a.feedbackTemplateType = :feedbackTemplateType")
public class FeedbackTemplate extends AbstractDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @XmlElement
    private boolean defaultForType = false;

    @NotNull
    @XmlElement
    private boolean enabled = false;

    @Transient
    private Set<FeedbackAnswer> feedbackAnswers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedbackTemplateTypeId")
    @XmlIDREF
    private FeedbackTemplateType feedbackTemplateType;

    @OneToMany(mappedBy = "template")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Feedback> feedbacks = new HashSet<>();

    @OneToMany(mappedBy = "feedbackTemplate", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("orderPosition")
    @XmlElement(name = "question")
    private List<FeedbackTemplateQuestion> templateQuestions = new ArrayList<>();

    public FeedbackTemplate() {
        super();
    }

    @SuppressWarnings("unused")
    public void addQuestion(FeedbackQuestion question) {
        new FeedbackTemplateQuestion(question, this);
    }

    @Override
    public FeedbackTemplate clone() throws CloneNotSupportedException {
        FeedbackTemplate clone = (FeedbackTemplate) super.clone();
        clone.setFeedbacks(new HashSet<>());
        List<FeedbackTemplateQuestion> clonedTemplateQuestions = new ArrayList<>();
        for (FeedbackTemplateQuestion feedbackTemplateQuestion : clone.getTemplateQuestions()) {
            FeedbackTemplateQuestion feedbackTemplateQuestionClone = feedbackTemplateQuestion.cloneWithCurrentPosition();
            feedbackTemplateQuestionClone.setFeedbackTemplate(clone);
            clonedTemplateQuestions.add(feedbackTemplateQuestionClone);
        }
        clone.setTemplateQuestions(clonedTemplateQuestions);
        return clone;
    }

    @Override
    public FeedbackTemplate getClone() {
        return (FeedbackTemplate) super.getClone();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.FEEDBACKMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getFeedbackTemplateType() != null) {
            addEntityInfoItem(summary, "feedbackTemplateType", getFeedbackTemplateType().getName());
        }
        addEntityInfoItem(summary, "enabled", isEnabled());
        addEntityInfoItem(summary, "default", isDefaultForType());
        return summary.toString();
    }

    public Set<FeedbackAnswer> getFeedbackAnswers() {
        if (feedbackAnswers == null) {
            feedbackAnswers = new HashSet<>();
            for (Feedback feedback : getFeedbacks()) {
                feedbackAnswers.addAll(feedback.getAnswers());
            }
        }
        return feedbackAnswers;
    }

    public FeedbackTemplateType getFeedbackTemplateType() {
        return feedbackTemplateType;
    }

    public Set<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public List<FeedbackQuestion> getQuestions() {
        List<FeedbackQuestion> feedbackQuestions = new ArrayList<>();
        for (FeedbackTemplateQuestion feedbackTemplateQuestion : getTemplateQuestions()) {
            feedbackQuestions.add(feedbackTemplateQuestion.getFeedbackQuestion());
        }
        return feedbackQuestions;
    }

    public FeedbackTemplateQuestion getTemplateQuestion(FeedbackQuestion question) {
        for (FeedbackTemplateQuestion feedbackTemplateQuestion : getTemplateQuestions()) {
            if (feedbackTemplateQuestion.getFeedbackQuestion().equals(question)) {
                return feedbackTemplateQuestion;
            }
        }
        return null;
    }

    public List<FeedbackTemplateQuestion> getTemplateQuestions() {
        return templateQuestions;
    }

    public boolean isDefaultForType() {
        return defaultForType;
    }

    @Override
    public boolean isDeletable() {
        return isCreatable() && getFeedbacks().isEmpty();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHintRendered() {
        return isOrdersRendered() || isProjectsRendered();
    }

    public boolean isOrdersRendered() {
        return getFeedbackTemplateType() != null && getFeedbackTemplateType().isOrder();
    }

    public boolean isProjectsRendered() {
        return getFeedbackTemplateType() != null && getFeedbackTemplateType().isProject();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isDeletable();
    }

    public void removeQuestion(FeedbackQuestion question) {
        FeedbackTemplateQuestion templateQuestion = getTemplateQuestion(question);
        if (question != null && templateQuestion != null) {
            getTemplateQuestions().remove(templateQuestion);
            question.getTemplateQuestions().remove(templateQuestion);
        }
    }

    public void setDefaultForType(boolean defaultForType) {
        this.defaultForType = defaultForType;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFeedbackTemplateType(FeedbackTemplateType feedbackTemplateType) {
        this.feedbackTemplateType = feedbackTemplateType;
    }

    public void setFeedbacks(Set<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public void setTemplateQuestions(List<FeedbackTemplateQuestion> questions) {
        templateQuestions = questions;
    }

    public void switchDefaultForType() {
        setDefaultForType(!isDefaultForType());
    }

    public void switchEnabled() {
        setEnabled(!isEnabled());
    }
}