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

import org.bfabric.Messages;
import org.bfabric.entity.Feedback;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;

@Named
@Stateless
public class FeedbackService extends AbstractService {

    private static final long serialVersionUID = 1;

    public FeedbackService() {
        super(Feedback.class);
    }

    public void acknowledgeFeedback(Feedback feedback) {
        feedback.acknowledgeFeedback();
        merge(feedback);
    }

    public LinkedHashMap<String, String> checkUserPermissions(Feedback feedback, User currentUser) {
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();
        if (!currentUser.hasRoleImplicit(RoleEnum.ADMIN) && feedback.getContainer() != null && !feedback.getContainer().isMember()) {
            errorMsg.put(null, Messages.get("validationErrors") + " : " + (feedback.getContainer()
                .isContainerProject() ? Messages.get("notMemberOfProjectHint") : Messages.get("notMemberOfOrderHint")));
        }
        return errorMsg;
    }

    public List<Feedback> getUnacknowledgedFeedbacksByTechnology(Technology technology) {
        if (technology == null) {
            return createQuery("FROM Feedback a WHERE a.acknowledgedBy is null ORDER BY a.id DESC").getResultList();
        }
        return createQuery("FROM Feedback a WHERE a.acknowledgedBy is null and (a.container is null or exists (select t from a.container c JOIN c.technologies t where t = :technology)) ORDER BY a.id DESC").setParameter("technology", technology)
            .getResultList();
    }
}