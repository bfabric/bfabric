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

import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Review;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.StatusEnum;

@Named
@Stateless
public class ReviewService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    @Inject
    private UserService userService;

    public ReviewService() {
        super(Review.class);
    }

    public Map<String, Set<String>> approve(Review review) {
        review.setApproved(true);
        review.getProject().changeStatus(StatusEnum.REVIEW);
        save(review);

        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("reviewApproveAdded"));
        return facesMessages;
    }

    public Map<String, Set<String>> finalApprove(Review review) {
        review.setApproved(true);
        review.getProject().changeStatus(StatusEnum.RUNNING);
        save(review);

        // Synchronize with the AD.
        userService.addRoleUserAndSynchronizeWithAD(review.getProject());

        Map<String, Set<String>> facesMessages = createFacesMessagesMap();

        // Send mails.
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(review, MailTypeEnum.CONTAINER_APPROVE));
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(review, MailTypeEnum.CONTAINER_APPROVE_COACH));

        facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("finallyApproved"));
        return facesMessages;
    }

    public Map<String, Set<String>> finalReject(Review review) {
        review.setApproved(false);
        review.getProject().changeStatus(StatusEnum.REJECTED);
        save(review);

        Map<String, Set<String>> facesMessages = createFacesMessagesMap();

        // Send emails.
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(review, MailTypeEnum.CONTAINER_REJECT));
        if (review.getProject().getCoach() != null) {
            facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(review, MailTypeEnum.CONTAINER_REJECT_COACH));
        }

        facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("finallyRejected"));
        return facesMessages;
    }

    public Map<String, Set<String>> reject(Review review) {
        review.setApproved(false);
        review.getProject().changeStatus(StatusEnum.REVIEW);
        save(review);

        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("reviewRejectAdded"));
        return facesMessages;
    }

    public void save(Review review) {
        save(review, true);
    }

    public void save(Review review, boolean index) {
        super.save(review, index);
        merge(review.getProject());
        if (index) {
            // Index the project.
            review.getProject().index();
        }
    }

    private String sendMail(Review review, MailTypeEnum mailTypeEnum) {
        final Mail mail = review.getProject().createMail(mailTypeEnum);
        if (!mail.getRecipients().isEmpty()) {
            mail.setInput("review", review);
            return mailSendService.send(mail);
        }
        return null;
    }
}