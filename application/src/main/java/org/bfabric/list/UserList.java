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
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Container;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.UserService;
import org.bfabric.service.util.BfabricLazyDataModel;

@MeasureCalls
@Named
@ViewScoped
public class UserList extends AbstractList<User> {

    private static final long serialVersionUID = 1;

    @Inject
    private UserService userService;

    @CachedMethodResult
    public List<User> getAllBudgetOfficers() {
        return getService().getAllBudgetOfficers();
    }

    @CachedMethodResult
    public List<User> getAllOrderRequesters() {
        return getService().getAllOrderRequesters();
    }

    @CachedMethodResult
    public List<User> getAllProjectLeaders() {
        return getService().getAllProjectLeaders();
    }

    @CachedMethodResult
    public List<User> getAllUsers() {
        return getService().getAllUsers();
    }

    @CachedMethodResult
    public List<User> getAllUsersByTechnology(String technologyName) {
        return getService().getAllUsersByTechnology(technologyName);
    }

    @CachedMethodResult
    public Set<User> getAlumni() {
        return getUsersByRoleEnum(RoleEnum.ALUMNI);
    }

    @CachedMethodResult
    public List<User> getDeletableUsers() {
        return getService().getDeletableUsers();
    }

    @CachedMethodResult
    public List<User> getEmployees() {
        return getService().getEmployees();
    }

    @CachedMethodResult
    public List<User> getEmployeesIncludingUser(User user) {
        return getService().getEmployeesIncludingUser(user);
    }

    @CachedMethodResult
    public List<User> getEmployeesRegular() {
        return getService().getEmployeesRegular();
    }

    @CachedMethodResult
    public List<User> getManagersByContainerId(long containerId) {
        return getService().getManagersByContainerId(containerId);
    }

    @CachedMethodResult
    public List<User> getMembersByContainerId(long containerId) {
        return getService().getMembersByContainerId(containerId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<User> getPotentialMembers(Container container) {
        return getService().getPotentialMembers(container);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<User> getReviewEmployeeStatusTasks() {
        return getService().getReviewEmployeeStatusTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<User> getRevokeRoleTasks() {
        return getService().getRevokeRoleTasks();
    }

    @Override
    protected UserService getService() {
        return userService;
    }

    @CachedMethodResult
    public List<User> getUsersByCompanyId(Long companyId) {
        return getService().getUsersByCompanyId(companyId);
    }

    @CachedMethodResult
    public List<User> getUsersByDepartmentId(Long departmentId) {
        return getService().getUsersByDepartmentId(departmentId);
    }

    @CachedMethodResult
    public List<User> getUsersByOrganizationId(Long organizationId) {
        return getService().getUsersByOrganizationId(organizationId);
    }

    @CachedMethodResult
    public Set<User> getUsersByRoleEnum(RoleEnum roleEnum) {
        return getService().getUsersByRoleEnum(roleEnum);
    }

    @CachedMethodResult
    public List<User> getUsersByRunningProjects() {
        return getService().getUsersByRunningProjects();
    }

    @CachedMethodResult
    public List<User> getUsersByServiceTypeId(long serviceTypeId) {
        return getService().getUsersByServiceTypeId(serviceTypeId);
    }

    @CachedMethodResult
    public List<Object> getUsersMerged() {
        return getService().getUsersMerged();
    }
}
