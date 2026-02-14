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
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.service.FeedbackTemplateQuestionService;

@Entity
@XmlRootElement
@NamedQuery(name = "FeedbackTemplateQuestion.findMaxOrderPosition", query = "SELECT COALESCE(max(a.orderPosition), 0) FROM FeedbackTemplateQuestion a WHERE a.feedbackTemplate = :feedbackTemplate")
public class FeedbackTemplateQuestion extends AbstractOrderedEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionid")
    private FeedbackQuestion feedbackQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "templateid")
    private FeedbackTemplate feedbackTemplate;

    public FeedbackTemplateQuestion() {
    }

    public FeedbackTemplateQuestion(FeedbackQuestion question, FeedbackTemplate template) {
        if (question != null && template != null) {
            setFeedbackQuestion(question);
            setFeedbackTemplate(template);
            // Keep order (add first to collection) to calculate the order position!
            question.getTemplateQuestions().add(this);
            template.getTemplateQuestions().add(this);

            // The templateQuestions must be fetched from template (not from question)!
            List<FeedbackTemplateQuestion> templateQuestions = new ArrayList<>(template.getTemplateQuestions());
            setOrderPosition(getNextOrderPosition(templateQuestions));
        }
    }

    @Override
    public FeedbackTemplateQuestion clone() throws CloneNotSupportedException {
        FeedbackTemplateQuestion clone = (FeedbackTemplateQuestion) super.clone();
        clone.setOrderPosition(getNextOrderPosition());
        return clone;
    }

    public FeedbackTemplateQuestion cloneWithCurrentPosition() throws CloneNotSupportedException {
        return (FeedbackTemplateQuestion) super.clone();
    }

    public FeedbackQuestion getFeedbackQuestion() {
        return feedbackQuestion;
    }

    @XmlElement(name = "question")
    public String getFeedbackQuestionValue() {
        return getFeedbackQuestion() != null && getFeedbackQuestion().getFeedbackQuestionType() != null ? " " + getFeedbackQuestion().getValue() + " " + getFeedbackQuestion().getFeedbackQuestionType()
            .getName() : null;
    }

    public FeedbackTemplate getFeedbackTemplate() {
        return feedbackTemplate;
    }

    public long getNextOrderPosition() {
        return CDI.current().select(FeedbackTemplateQuestionService.class).get().getMaxOrderPosition(getFeedbackTemplate()) + 1;
    }

    public void setFeedbackQuestion(FeedbackQuestion feedbackQuestion) {
        this.feedbackQuestion = feedbackQuestion;
    }

    public void setFeedbackTemplate(FeedbackTemplate feedbackTemplate) {
        this.feedbackTemplate = feedbackTemplate;
    }
}
