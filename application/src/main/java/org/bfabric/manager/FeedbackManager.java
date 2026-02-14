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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.Feedback;
import org.bfabric.entity.FeedbackAnswer;
import org.bfabric.entity.FeedbackTemplate;
import org.bfabric.entity.FeedbackTemplateQuestion;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.FeedbackService;
import org.omnifaces.cdi.Param;

@Named
@ViewScoped
public class FeedbackManager extends AbstractEntityManager<Feedback> {

    private static final long serialVersionUID = 1;

    @Param
    private Long containerId;

    @Param
    private Long defaultFeedbackTemplateId;

    @Inject
    private FeedbackService feedbackService;

    public FeedbackManager() {
        super(Feedback.class);
    }

    public void acknowledgeFeedback() {
        feedbackService.acknowledgeFeedback(getFeedback());
        getFacesMessagesManager().bufferWarningClear(Messages.get("acknowledged"));
    }

    public String cancelSubmitFeedback() {
        return getFeedback().getContainer() != null ? createRedirectShowScreenURL(getFeedback().getContainer()) : getUrlHomeScreen();
    }

    public void feedbackTemplateChange() {
        initAnswers();
        getFeedback().setContainer(null);
    }

    public Long getContainerId() {
        return containerId;
    }

    public Long getDefaultFeedbackTemplateId() {
        return defaultFeedbackTemplateId;
    }

    @Produces
    @Named("feedback")
    public Feedback getFeedback() {
        return getInstance();
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getFeedback() != null && getDefaultFeedbackTemplateId() != null) {
            getFeedback().setTemplate(entityService.find(FeedbackTemplate.class, getDefaultFeedbackTemplateId()));
            initAnswers();
            if (getContainerId() != null) {
                getFeedback().setContainer(entityService.find(Container.class, getContainerId()));
            }
        }
    }

    private void initAnswers() {
        if (getFeedback().getTemplate() != null) {
            // To prevent the creation of new answers in case of a template change for a feedback.
            final Set<FeedbackAnswer> answers = new HashSet<>();
            for (final FeedbackTemplateQuestion feedbackTemplateQuestion : getFeedback().getTemplate().getTemplateQuestions()) {
                final FeedbackAnswer feedbackAnswer = new FeedbackAnswer();
                feedbackAnswer.setFeedback(getFeedback());
                feedbackAnswer.setQuestion(feedbackTemplateQuestion.getFeedbackQuestion());
                feedbackAnswer.setOrderPosition(feedbackTemplateQuestion.getOrderPosition());
                answers.add(feedbackAnswer);
            }
            getFeedback().setAnswers(answers);
        }
    }

    public boolean isTemplateRendered() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER);
    }

    @Override
    public String save() {
        setCreated(!isManaged());

        LinkedHashMap<String, String> errorMsg = feedbackService.checkUserPermissions(getFeedback(), getCurrentUser());
        if (errorMsg.isEmpty()) {
            feedbackService.save(getFeedback());
            return postSave(true, false);
        }

        getFacesMessagesManager().printValidationErrors(errorMsg);
        return null;
    }
}