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

package org.bfabric.manager;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.FeedbackQuestion;
import org.bfabric.entity.FeedbackQuestionType;
import org.bfabric.entity.FeedbackTemplate;
import org.bfabric.service.FeedbackQuestionService;
import org.omnifaces.cdi.Param;

@Named
@ViewScoped
public class FeedbackQuestionManager extends AbstractEntityManager<FeedbackQuestion> {

    private static final long serialVersionUID = 1;

    @Inject
    private FeedbackQuestionService feedbackQuestionService;

    @Param
    private Long feedbackTemplateId;

    public FeedbackQuestionManager() {
        super(FeedbackQuestion.class);
    }

    public void addCustomOption(FeedbackQuestionType feedbackQuestionType) {
        if (feedbackQuestionType != null && feedbackQuestionType.isRequiresCustomOptions() && getFeedbackQuestion().getCustomOptions().isEmpty()) {
            getFeedbackQuestion().addCustomOption();
        }
    }

    @Override
    public String createRedirectURL(String screen, Long redirectId, String tab, Map<String, String> fParams) {
        return getFeedbackTemplateId() != null ? super.createRedirectShowScreenURL(FeedbackTemplate.class.getSimpleName(), getFeedbackTemplateId(), "questions", null) : super.createRedirectURL(screen, redirectId, tab, fParams);
    }

    @Produces
    @Named("feedbackQuestion")
    public FeedbackQuestion getFeedbackQuestion() {
        return getInstance();
    }

    public Long getFeedbackTemplateId() {
        return feedbackTemplateId;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getFeedbackQuestion() != null && feedbackTemplateId != null) {
            final FeedbackTemplate feedbackTemplate = entityService.find(FeedbackTemplate.class, feedbackTemplateId);
            if (feedbackTemplate != null) {
                getFeedbackQuestion().addTemplate(feedbackTemplate);
            }
        }
    }

    @Override
    public String save() {
        setCreated(!isManaged());

        LinkedHashMap<String, String> errorMsg = feedbackQuestionService.isValid(getFeedbackQuestion());
        if (errorMsg.isEmpty()) {
            feedbackQuestionService.save(getFeedbackQuestion());
            return postSave(true, false);
        }

        getFacesMessagesManager().printValidationErrors(errorMsg);
        return null;
    }
}