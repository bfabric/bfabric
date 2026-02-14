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

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.FeedbackQuestion;
import org.bfabric.entity.FeedbackTemplate;
import org.bfabric.entity.FeedbackTemplateQuestion;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.FeedbackTemplateService;

@MeasureCalls
@Named
@ViewScoped
public class FeedbackTemplateManager extends AbstractEntityManager<FeedbackTemplate> {

    private static final long serialVersionUID = 1;

    private Container currentContainer;

    @Inject
    private FeedbackTemplateService feedbackTemplateService;

    public FeedbackTemplateManager() {
        super(FeedbackTemplate.class);
    }

    @Override
    public String cancel() {
        return isCloned() ? createRedirectShowScreenURL(FeedbackTemplate.class.getSimpleName(), getClonedId(), null, null) : super.cancel();
    }

    public Container getCurrentContainer() {
        return currentContainer;
    }

    @Produces
    @Named("feedbackTemplate")
    public FeedbackTemplate getFeedbackTemplate() {
        return getInstance();
    }

    public boolean isDisableRendered() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.FEEDBACKMANAGER) && getFeedbackTemplate().isEnabled();
    }

    public boolean isEnableRendered() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.FEEDBACKMANAGER) && !getFeedbackTemplate().getQuestions().isEmpty() && !getFeedbackTemplate().isEnabled();
    }

    public boolean isSetDefaultForTypeRendered() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.FEEDBACKMANAGER) && !getFeedbackTemplate().isDefaultForType();
    }

    public boolean isUnsetDefaultForTypeRendered() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.FEEDBACKMANAGER) && getFeedbackTemplate().isDefaultForType();
    }

    public void moveOrderPositionDown(FeedbackQuestion feedbackQuestion) {
        feedbackTemplateService.moveOrderPositionDown(getFeedbackTemplate(), feedbackQuestion);
        FeedbackTemplateQuestion templateQuestion = getFeedbackTemplate().getTemplateQuestion(feedbackQuestion);
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionDownHint").replace("{0}", templateQuestion.getFeedbackQuestion().toString()));
    }

    public void moveOrderPositionEnd(FeedbackQuestion feedbackQuestion) {
        feedbackTemplateService.moveOrderPositionEnd(getFeedbackTemplate(), feedbackQuestion);
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionEndHint").replace("{0}", feedbackQuestion.toString()));
    }

    public void moveOrderPositionStart(FeedbackQuestion feedbackQuestion) {
        feedbackTemplateService.moveOrderPositionStart(getFeedbackTemplate(), feedbackQuestion);
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionStartHint").replace("{0}", feedbackQuestion.toString()));
    }

    public void moveOrderPositionUp(FeedbackQuestion feedbackQuestion) {
        feedbackTemplateService.moveOrderPositionUp(getFeedbackTemplate(), feedbackQuestion);
        FeedbackTemplateQuestion templateQuestion = getFeedbackTemplate().getTemplateQuestion(feedbackQuestion);
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionUpHint").replace("{0}", templateQuestion.getFeedbackQuestion().toString()));
    }

    public String request() {
        HashMap<String, String> params = new HashMap<>();
        params.put("feedbackTemplateId", getFeedbackTemplate().getIdString());
        params.put("feedbackContainerId", getCurrentContainer().getIdString());
        return createRedirectURL("mail/send", null, null, params);
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = feedbackTemplateService.isValid(getFeedbackTemplate());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            feedbackTemplateService.save(getFeedbackTemplate());
            return postSave(true, false);
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void setCurrentContainer(Container currentContainer) {
        this.currentContainer = currentContainer;
    }

    public String switchDefaultForType() {
        feedbackTemplateService.switchDefaultForType(getFeedbackTemplate());
        getFacesMessagesManager().bufferWarningClear(getFeedbackTemplate().isDefaultForType() ? Messages.get("setDefaultForType") : Messages.get("unsetDefaultForType"));
        return getShowScreenRedirectURL();
    }

    public String switchEnabled() {
        feedbackTemplateService.switchEnabled(getFeedbackTemplate());
        getFacesMessagesManager().bufferWarningClear(getFeedbackTemplate().isEnabled() ? Messages.get("enabled") : Messages.get("disabled"));
        return getShowScreenRedirectURL();
    }
}