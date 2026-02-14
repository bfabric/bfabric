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

import org.bfabric.entity.Executable;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.WorkunitService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class WorkunitList extends AbstractList<Workunit> {

    private static final long serialVersionUID = 1;

    @Inject
    private WorkunitService workunitService;

    @CachedMethodResult
    public BfabricLazyDataModel<Workunit> getAssignSampleTasks() {
        return getService().getAssignSampleTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Workunit> getAvailableWorkunitsLazyModelByUser(User user) {
        if (user != null) {
            return user.hasRoleImplicit(RoleEnum.EMPLOYEE) ? getLazyModel() : getLazyModelByUserId(user.getId());
        }
        return null;
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Workunit> getLazyModelByApplicationId(long applicationId) {
        return getService().getLazyModelByApplicationId(applicationId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Workunit> getLazyModelByExecutable(Executable executable) {
        return getService().getLazyModelByExecutable(executable);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Workunit> getLazyModelByInstrumentId(long instrumentId) {
        return getService().getLazyModelByInstrumentId(instrumentId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Workunit> getLazyModelBySampleId(long sampleId) {
        return getService().getLazyModelBySampleId(sampleId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Workunit> getLazyModelSucceedingWorkunitsByWorkunitId(long workunitId) {
        return getService().getLazyModelSucceedingWorkunitsByWorkunitId(workunitId);
    }

    @Override
    protected WorkunitService getService() {
        return workunitService;
    }

    @CachedMethodResult
    public List<Workunit> getWorkunitsSamplesReassignable() {
        return getService().getWorkunitsSamplesReassignable();
    }
}