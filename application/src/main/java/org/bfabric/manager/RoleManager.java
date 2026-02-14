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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Role;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.RoleService;

@MeasureCalls
@Named
@ViewScoped
public class RoleManager extends AbstractEntityManager<Role> {

    private static final long serialVersionUID = 1;

    @Inject
    private RoleService roleService;

    private List<Role> selectedRoleGroups = null;

    private List<Role> selectedRoles;

    private List<User> selectedUsers;

    public RoleManager() {
        super(Role.class);
    }

    public String assignRoles() {
        if (!getSelectedRoles().isEmpty() && !getSelectedUsers().isEmpty()) {
            int assignments = roleService.assignRoles(getSelectedRoles(), getSelectedUsers());
            if (assignments > 0) {
                getFacesMessagesManager().bufferWarning(Messages.get("successfullyAssignedRoles").replace("{0}", Integer.toString(assignments)));
            } else {
                getFacesMessagesManager().bufferWarning(Messages.get("noRoleAssignmentSelected"));
            }
            return createRedirectURL("role/assign-roles");
        }
        return null;
    }

    public String cancelAssignRoles() {
        return getUrlHomeScreen();
    }

    public void clearForm() {
        getSelectedRoles().clear();
        getSelectedUsers().clear();
        getFacesMessagesManager().bufferWarningClear(Messages.get("formCleared"));
    }

    public void deselectRole(Role role) {
        if (!getSelectedRoles().isEmpty()) {
            getSelectedRoles().remove(role);
        }
    }

    public void deselectUser(User user) {
        if (!getSelectedUsers().isEmpty()) {
            getSelectedUsers().remove(user);
        }
    }

    @Produces
    @Named("role")
    public Role getRole() {
        return getInstance();
    }

    public List<Role> getRoleGroups(String filterString) {
        Set<Role> exclude = new HashSet<>();
        if (isManaged()) {
            exclude.add(getRole());
            exclude.addAll(getRole().getParents());
        }
        if (getSelectedRoleGroups() != null && !getSelectedRoleGroups().isEmpty()) {
            exclude.addAll(getSelectedRoleGroups());
        }
        return roleService.getRolesFiltered(filterString, exclude);
    }

    public List<Role> getSelectedRoleGroups() {
        if (selectedRoleGroups == null) {
            selectedRoleGroups = new ArrayList<>();
            selectedRoleGroups.addAll(getRole().getGroups());
        }
        return selectedRoleGroups;
    }

    public List<Role> getSelectedRoles() {
        if (selectedRoles == null) {
            selectedRoles = new ArrayList<>();
        }
        return selectedRoles;
    }

    public List<User> getSelectedUsers() {
        if (selectedUsers == null) {
            selectedUsers = new ArrayList<>();
        }
        return selectedUsers;
    }

    public boolean isRenderClearAssignRoles() {
        return !getSelectedUsers().isEmpty() || !getSelectedRoles().isEmpty();
    }

    public boolean isRenderSaveAssignRoles() {
        return !getSelectedUsers().isEmpty() && !getSelectedRoles().isEmpty();
    }

    @SuppressWarnings("SameReturnValue")
    public boolean isRoleSelected() {
        // Note: Needed because JSF requires a properly writable property, i.e., roleManager.selectedRoles.contains(entity) is not applicable.
        return true;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean isUserSelected() {
        // Note: Needed because JSF requires a properly writable property, i.e., roleManager.selectedUsers.contains(entity) is not applicable.
        return true;
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = roleService.isValid(getRole());

        if (validationErrorMsg.isEmpty()) {
            if (selectedRoleGroups == null) {
                getRole().setGroups(new HashSet<>());
            } else {
                getRole().setGroups(new HashSet<>(getSelectedRoleGroups()));
            }
            return super.save();
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void selectRole(Role role) {
        if (!getSelectedRoles().contains(role)) {
            getSelectedRoles().add(role);
        }
    }

    public void selectUser(User user) {
        if (!getSelectedUsers().contains(user)) {
            getSelectedUsers().add(user);
        }
    }

    @SuppressWarnings("EmptyMethod")
    public void setRoleSelected(boolean isRoleSelected) {
        // Note: Needed because JSF requires a properly writable property, i.e., roleManager.selectedRoles.contains(entity) is not applicable.
    }

    public void setSelectedRoleGroups(List<Role> selectedRoleGroups) {
        this.selectedRoleGroups = selectedRoleGroups;
    }

    public void setSelectedRoles(List<Role> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public void setSelectedUsers(List<User> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    @SuppressWarnings("EmptyMethod")
    public void setUserSelected(boolean isUserSelected) {
        // Needed because JSF requires a properly writable property, i.e., roleManager.selectedUsers.contains(entity) is not applicable.
    }
}
