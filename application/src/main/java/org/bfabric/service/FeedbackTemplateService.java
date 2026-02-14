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

package org.bfabric.service;

import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.FeedbackQuestion;
import org.bfabric.entity.FeedbackTemplate;
import org.bfabric.entity.FeedbackTemplateQuestion;
import org.bfabric.entity.FeedbackTemplateType;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class FeedbackTemplateService extends AbstractService {

    private static final long serialVersionUID = 1;

    public FeedbackTemplateService() {
        super(FeedbackTemplate.class);
    }

    public Long getDefaultFeedbackTemplateId(String feedbackTemplateTypeName) {
        List<FeedbackTemplate> defaultFeedbackTemplates = createNamedQuery("FeedbackTemplate.defaultForType").setParameter("feedbackTemplateTypeName", feedbackTemplateTypeName).setMaxResults(1)
            .getResultList();
        if (!defaultFeedbackTemplates.isEmpty()) {
            return defaultFeedbackTemplates.get(0).getId();
        }
        return null;
    }

    public List<FeedbackTemplate> getEnabledTemplates() {
        return createNamedQuery("FeedbackTemplate.findEnabled").getResultList();
    }

    public List<FeedbackTemplate> getUpdatableFeedbackTemplates() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("not exists (select feedback from Feedback feedback where feedback.template = entity)");
        return (List<FeedbackTemplate>) entityQuery.getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final FeedbackTemplate feedbackTemplate = (FeedbackTemplate) entity;
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();

        if (feedbackTemplate.isEnabled() && feedbackTemplate.getTemplateQuestions().isEmpty()) {
            errorMsg.put(Constants.EDIT + ":enabled", Messages.get("requiresQuestionsHint"));
        }

        return errorMsg;
    }

    public void moveOrderPositionDown(FeedbackTemplate feedbackTemplate, FeedbackQuestion feedbackQuestion) {
        FeedbackTemplateQuestion templateQuestion = feedbackTemplate.getTemplateQuestion(feedbackQuestion);
        templateQuestion.moveOrderPositionDown(feedbackTemplate.getTemplateQuestions());
        merge(feedbackTemplate);
    }

    public void moveOrderPositionEnd(FeedbackTemplate feedbackTemplate, FeedbackQuestion feedbackQuestion) {
        FeedbackTemplateQuestion templateQuestion = feedbackTemplate.getTemplateQuestion(feedbackQuestion);
        templateQuestion.moveOrderPositionEnd(feedbackTemplate.getTemplateQuestions());
        merge(feedbackTemplate);
    }

    public void moveOrderPositionStart(FeedbackTemplate feedbackTemplate, FeedbackQuestion feedbackQuestion) {
        FeedbackTemplateQuestion templateQuestion = feedbackTemplate.getTemplateQuestion(feedbackQuestion);
        templateQuestion.moveOrderPositionStart(feedbackTemplate.getTemplateQuestions());
        merge(feedbackTemplate);
    }

    public void moveOrderPositionUp(FeedbackTemplate feedbackTemplate, FeedbackQuestion feedbackQuestion) {
        FeedbackTemplateQuestion templateQuestion = feedbackTemplate.getTemplateQuestion(feedbackQuestion);
        templateQuestion.moveOrderPositionUp(feedbackTemplate.getTemplateQuestions());
        merge(feedbackTemplate);
    }

    public void resetDefaultForType(long id, FeedbackTemplateType feedbackTemplateType) {
        createNamedQuery("FeedbackTemplate.resetDefaultForType").setParameter("id", id).setParameter("feedbackTemplateType", feedbackTemplateType).executeUpdate();
    }

    public void save(FeedbackTemplate feedbackTemplate) {
        save(feedbackTemplate, true);
    }

    public void save(FeedbackTemplate feedbackTemplate, boolean index) {
        if (feedbackTemplate.isDefaultForType()) {
            resetDefaultForType(feedbackTemplate.getId(), feedbackTemplate.getFeedbackTemplateType());
        }
        super.save(feedbackTemplate, index);
    }

    public void switchDefaultForType(FeedbackTemplate feedbackTemplate) {
        if (feedbackTemplate != null) {
            feedbackTemplate.switchDefaultForType();
            save(feedbackTemplate);
        }
    }

    public void switchEnabled(FeedbackTemplate feedbackTemplate) {
        if (feedbackTemplate != null) {
            feedbackTemplate.switchEnabled();
            // Create an entity log for the feedback.
            feedbackTemplate.createEntityLog(feedbackTemplate.isEnabled() ? LogActionEnum.ENABLE : LogActionEnum.DISABLE);
            save(feedbackTemplate);
        }
    }
}