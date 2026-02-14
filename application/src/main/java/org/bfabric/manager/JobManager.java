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

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Job;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.JobService;
import org.bfabric.service.UserService;
import org.bfabric.service.WorkunitService;

@MeasureCalls
@Named
@ViewScoped
public class JobManager extends AbstractEntityManager<Job> {

    private static final long serialVersionUID = 1;

    @Inject
    private JobService jobService;

    @Inject
    private WorkunitService workunitService;

    public JobManager() {
        super(Job.class);
    }

    public String changeStatus(StatusEnum statusEnum) {
        printFacesMessagesClear(jobService.changeStatus(getJob(), statusEnum));
        return getShowScreenRedirectURL();
    }

    public List<User> getEmployeesIncludingRequester(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getJob().getRequester());
    }

    @Produces
    @Named("job")
    public Job getJob() {
        return getInstance();
    }

    public List<Workunit> getWorkunits(String filterString) {
        return workunitService.getWorkunitsFilteredByContainerIdExcluding(filterString, null, getJob().getWorkunits());
    }

    public String rollbackStatus() {
        jobService.rollbackStatus(getJob());
        getFacesMessagesManager().bufferWarningClear(Messages.get("statusRolledBack"));
        return getShowScreenRedirectURL();
    }

    @Override
    public String save() {
        jobService.save(getJob());
        return postSave(true, false);
    }
}
