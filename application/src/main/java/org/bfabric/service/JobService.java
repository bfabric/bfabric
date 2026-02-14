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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Application;
import org.bfabric.entity.Job;
import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.enums.WorkunitStatusEnum;

@Named
@Stateless
public class JobService extends AbstractParentDependentEntityService {

    private static final long serialVersionUID = 1;

    @Inject
    protected MailSendService mailSendService;

    public JobService() {
        super(Job.class);
    }

    public Map<String, Set<String>> changeStatus(Job job, StatusEnum statusEnum) {
        Set<Mail> mails = job.changeStatus(statusEnum);
        super.save(job);
        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("job") + " " + statusEnum.getLabel());
        facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
        return facesMessages;
    }

    public Job createWebAppJob(Application application, AbstractEntity clientEntity, User user) {
        Job job = new Job(LogActionEnum.RUNNING, user, application);
        job.addState();
        persist(job);
        if (clientEntity == null) {
            clientEntity = application;
        }
        job.setUrl(application.getWebUrlWithToken(clientEntity, job));
        job.setLogEntity(false);
        save(job);
        return job;
    }

    public Job getLastUnarchiveJob(AbstractEntity parent) {
        return (Job) createNamedQuery("Job.findUnarchiveByParent").setParameter("parentClassName", parent.getTrimmedClassName()).setParameter("parentId", parent.getId()).setMaxResults(1)
            .getResultStream().findFirst().orElse(null);
    }

    public List<Job> getUnarchiveByParentAndStatusNew() {
        return createNamedQuery("Job.findUnarchiveByParentAndStatusNew").getResultList();
    }

    public void rollbackStatus(Job job) {
        if (job != null) {
            job.rollbackStatus();
            save(job);
        }
    }

    public void save(Job job) {
        save(job, true);
    }

    public void save(Job job, boolean index) {
        if (job != null) {
            if (!job.isManaged()) {
                job.addState();
            }
            boolean sendMailUnarchive = false;
            boolean sendMailUnarchived = false;
            boolean sendMailUnarchiveFailed = false;
            boolean sendMailUnarchiveCanceled = false;
            if (job.getParent() instanceof Workunit) {
                Workunit workunit = (Workunit) job.getParent();
                if (StatusEnum.NEW.equals(job.getStatus()) && (!job.isManaged() || !job.getStatus().equals(job.getOldStatus()))) {
                    sendMailUnarchive = true;
                } else if (StatusEnum.DONE.equals(job.getStatus()) && !job.getStatus().equals(job.getOldStatus())) {
                    if (WorkunitStatusEnum.AVAILABLE.equals(workunit.getStatus()) || WorkunitStatusEnum.IMPORTED.equals(workunit.getStatus())) {
                        sendMailUnarchived = true;
                    } else {
                        sendMailUnarchiveFailed = true;
                    }
                } else if (StatusEnum.CANCELED.equals(job.getStatus()) && !job.getStatus().equals(job.getOldStatus())) {
                    sendMailUnarchiveCanceled = true;
                }
            }
            super.save(job, index);
            if (sendMailUnarchive) {
                mailSendService.send(job.createMailUnarchive());
            }
            if (sendMailUnarchived) {
                mailSendService.send(job.createMailUnarchived());
            }
            if (sendMailUnarchiveFailed) {
                mailSendService.send(job.createMailUnarchiveFailed());
            }
            if (sendMailUnarchiveCanceled) {
                mailSendService.send(job.createMailUnarchiveCanceled());
            }
        }
    }
}
