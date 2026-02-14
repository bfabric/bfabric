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

package org.bfabric.util;

import java.io.Serializable;

import org.bfabric.entity.AbstractContainerDependentEntity;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Container;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Service;

public class MailRecipientHelper extends UserFunctionHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private final Comment comment;

    private Container container;

    private Offer offer;

    public MailRecipientHelper(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

    public Container getContainer() {
        if (container == null && getComment() != null && getComment().getParent() != null) {
            if (getComment().isContainerComment()) {
                container = (Container) getComment().getParent();
            } else if (getComment().getParent() instanceof AbstractContainerDependentEntity) {
                container = ((AbstractContainerDependentEntity) getComment().getParent()).getContainer();
            }
        }
        return container;
    }

    public Offer getOffer() {
        if (offer == null && getComment() != null && getComment().getParent() instanceof Offer) {
            offer = (Offer) getComment().getParent();
        }
        return offer;
    }

    public void init() {
        getFunctions().clear();
        getFunctionsInitList().clear();
        getUserFunctions().clear();
        getUsersCustom().clear();
        setUserGroup(null);
        if (getComment() != null) {
            if (getContainer() != null) {
                if (getComment().isInternal()) {
                    addUserFunctions(getContainer().getInternalMembersTransitiveSorted(), INTERNAL_MEMBER, true);
                } else {
                    addUserFunction(getContainer().getRequester(), REQUESTER, true);
                    addUserFunction(getContainer().getContact(), CONTACT, true);
                    addUserFunction(getContainer().getBudgetOfficer(), BUDGET_OFFICER, true);
                    addUserFunction(getContainer().getLeader(), LEADER, true);
                    addUserFunctions(getContainer().getMembersTransitiveSorted(), MEMBER, true);
                }
                addUserFunction(getContainer().getCoach(), COACH, true);
                addUserFunction(getContainer().getCoachBackup(), COACH_BACKUP, true);
                addUserFunction(getContainer().getBioinformatician(), BIOINFORMATICIAN, true);
                addUserFunctions(getContainer().getTrackingUsers(), TRACKER, true);
                if (getContainer().getServiceType() != null) {
                    if (getContainer().getCoach() == null && getContainer().getServiceType().getCoach() != null) {
                        addUserFunction(getContainer().getServiceType().getCoach(), SERVICE_TYPE_COACH, true);
                    }
                    addUserFunctions(getContainer().getServiceType().getUsers(), SERVICE_TYPE_TRACKER, true);
                    for (Service service : getContainer().getServices()) {
                        addUserFunctions(service.getUsers(), SERVICE_TRACKER, true);
                    }
                }
                if (getContainer().getProject() != null) {
                    if (!getComment().isInternal()) {
                        addUserFunction(getContainer().getProject().getRequester(), PROJECT_REQUESTER, true);
                        addUserFunction(getContainer().getProject().getContact(), PROJECT_CONTACT, true);
                        addUserFunction(getContainer().getProject().getBudgetOfficer(), PROJECT_BUDGET_OFFICER, true);
                        addUserFunction(getContainer().getProject().getLeader(), PROJECT_LEADER, true);
                    }
                    addUserFunction(getContainer().getProject().getCoach(), PROJECT_COACH, true);
                    addUserFunction(getContainer().getProject().getCoachBackup(), PROJECT_COACH_BACKUP, true);
                    addUserFunction(getContainer().getProject().getBioinformatician(), PROJECT_BIOINFORMATICIAN, true);
                    addUserFunctions(getContainer().getProject().getTrackingUsers(), TRACKER, true);
                }
            }
            if (getOffer() != null) {
                if (!getComment().isInternal()) {
                    addUserFunction(getOffer().getRequester(), REQUESTER, true);
                }
                addUserFunction(getOffer().getCoach(), COACH, true);
                addUserFunction(getOffer().getCoachBackup(), COACH_BACKUP, true);
            }
            addUserFunctions(getComment().getReplyToUsers(), REPLY_TO_USER, true);
        }
        resetFunctions();
        resetFunctionsInitList();
        if (getComment() != null && (getComment().isCategoryNote() || getComment().isCategoryResult())) {
            uncheckAll();
        }
        setFunctionsChecked();
    }

    public boolean isRenderedFunctionsSelect() {
        return !getFunctionsInitList().isEmpty() && (getComment().isCategoryNote() || getComment().isCategoryResult() || StringHelper.isNotEmpty(getComment().getDiscriminator()
            .getMailTargetExternal()) || StringHelper.isNotEmpty(getComment().getDiscriminator().getMailTargetInternal()));
    }

    public boolean isUncheckEnabled(UserFunction userFunction) {
        return getComment().isMailRecipientsEditingEnabled() || userFunction != null && !userFunction.getUser().isDisableNotificationExclusionForParent(getComment().getParent());
    }
}