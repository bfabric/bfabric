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

import org.bfabric.entity.Application;
import org.bfabric.entity.Container;
import org.bfabric.entity.DatasetTemplate;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.ApplicationService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class ApplicationList extends AbstractList<Application> {

    private static final long serialVersionUID = 1;

    @Inject
    private ApplicationService applicationService;

    @CachedMethodResult
    public BfabricLazyDataModel<?> getLazyModelByApplicationCategoryId(long applicationCategoryId) {
        return getService().getLazyModelByApplicationCategoryId(applicationCategoryId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<?> getLazyModelByPageflowId(long pageflowId) {
        return getService().getLazyModelByPageflowId(pageflowId);
    }

    @CachedMethodResult
    public List<Application> getReassignApplicationSupervisorTasks() {
        return getService().getReassignApplicationSupervisorTasks();
    }

    @CachedMethodResult
    public List<Application> getRunnableApplicationsByDatasetTemplate(DatasetTemplate datasetTemplate) {
        return getService().getRunnableApplications(identityManager.hasCurrentUserRoleEnum(RoleEnum.APPLICATIONMANAGER), datasetTemplate);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Application> getRunnableApplicationsLazyModel(Container container) {
        return getService().getRunnableApplicationsLazyModel(container, identityManager.hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE));
    }

    @Override
    protected ApplicationService getService() {
        return applicationService;
    }
}