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

import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.UserGroupService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class UserGroupList extends AbstractList<UserGroup> {

    private static final long serialVersionUID = 1;

    @Inject
    private UserGroupService userGroupService;

    @CachedMethodResult
    public List<UserGroup> getAllEnabledInternalOrderByName() {
        return (List<UserGroup>) getService().getAllEnabledInternalOrderByName();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<UserGroup> getMember(User user) {
        return getService().getMembersUserGroups(user);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<UserGroup> getReassignUserGroupSupervisorTasks() {
        return getService().getReassignUserGroupSupervisorTasks();
    }

    @CachedMethodResult
    public List<UserGroup> getResultListOrderByName() {
        return (List<UserGroup>) getService().getResultListOrderByName();
    }

    @Override
    protected UserGroupService getService() {
        return userGroupService;
    }

    @CachedMethodResult
    public BfabricLazyDataModel<UserGroup> getSupervisedUserGroups(User user) {
        return getService().getSupervisedUserGroups(user);
    }
}