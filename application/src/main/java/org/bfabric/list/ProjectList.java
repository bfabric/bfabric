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

package org.bfabric.list;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Project;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ProjectService;
import org.bfabric.service.util.BfabricLazyDataModel;

@MeasureCalls
@Named
@ViewScoped
public class ProjectList extends AbstractList<Project> {

    private static final long serialVersionUID = 1;

    @Inject
    private ProjectService projectService;

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getBioinformaticianProjectsLazyModelByUserId(long userId) {
        return getService().getBioinformaticianProjectsLazyModelByUserId(userId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getBioinformaticianReassignmentTasks() {
        return getService().getBioinformaticianReassignmentTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getBookingRequiredTasks() {
        return getService().getBookingRequiredTasks(identityManager.getCurrentUser(), getConfiguration().getBookingRequiredTotal());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getCoachAssignmentTasks() {
        return getService().getCoachAssignmentTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getCoachBackupReassignmentTasks() {
        return getService().getCoachBackupReassignmentTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getCoachReassignmentTasks() {
        return getService().getCoachReassignmentTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getExtensionReportAddTasks(Boolean all) {
        return getService().getExtensionReportAddTasks(all, identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getExtensionReportApproveTasks() {
        return getService().getExtensionReportApproveTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getFormerProjectsLazyModelByUserId(long userId) {
        return getService().getFormerProjectsLazyModelByUserId(userId);
    }

    @CachedMethodResult
    public List<EntityLog> getLastEntityLogs(Project project) {
        return getService().getLastEntityLogs(project, Math.max(project.getStates().size(), 5));
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getLazyModelByCompanyId(long companyId) {
        return getService().getLazyModelByCompanyId(companyId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getLazyModelByDepartmentId(long departmentId) {
        return getService().getLazyModelByDepartmentId(departmentId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getLazyModelByOrganizationId(long organizationId) {
        return getService().getLazyModelByOrganizationId(organizationId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectCoachTasks(Boolean all) {
        return getService().getProjectCoachTasks(all, identityManager.isLoggedIn(), identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectFinalDecisionTasks() {
        return getService().getProjectFinalDecisionTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectFinishAnnounceTasks() {
        return getService().getProjectFinishAnnounceTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectFinishingTasks() {
        return getService().getProjectFinishingTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectReviewTasks(Boolean all) {
        return getService().getProjectReviewTasks(all, identityManager.isLoggedIn(), identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectSetPrivateAnnounceTasks() {
        return getService().getProjectSetPrivateAnnounceTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectSetPrivatePendingReminderTasks() {
        return getService().getProjectSetPrivatePendingReminderTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectSetPrivateReminderTasks() {
        return getService().getProjectSetPrivateReminderTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Project> getProjectSetPrivateTasks() {
        return getService().getProjectSetPrivateTasks();
    }

    @CachedMethodResult
    public List<StatusEnum> getProjectStatusEnums() {
        return getService().getProjectStatusEnums();
    }

    @Override
    protected ProjectService getService() {
        return projectService;
    }
}