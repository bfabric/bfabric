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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.UserGroupService;
import org.bfabric.service.UserService;

@MeasureCalls
@Named
@ViewScoped
public class UserGroupManager extends AbstractEntityManager<UserGroup> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(UserGroupManager.class.getName());

    @Inject
    private UserGroupService userGroupService;

    @Inject
    private UserService userService;

    public UserGroupManager() {
        super(UserGroup.class);
    }

    public String addSupervisorAsMember() {
        if (!getUserGroup().isSupervisorMember()) {
            getUserGroup().addSupervisorAsMember();
            return save();
        }
        return null;
    }

    @Override
    protected UserGroup createInstance() {
        final UserGroup userGroup = super.createInstance();
        if (userGroup != null) {
            userGroup.setSupervisor(getCurrentUser());
        }
        return userGroup;
    }

    @Produces
    @Named("userGroup")
    public UserGroup getUserGroup() {
        return getInstance();
    }

    public List<User> getUsers(String filterString) {
        List<User> userGroupUsers;
        if (getUserGroup().isForEmployeesOnly()) {
            userGroupUsers = userService.getEmployeesFiltered(filterString, getUserGroup().getUsers());
        } else {
            userGroupUsers = userService.getUsersFilteredExcluding(filterString, getUserGroup().getUsers());
        }
        return userGroupUsers;
    }

    @Override
    public UserGroup loadInstance() {
        UserGroup userGroup = super.loadInstance();
        if (userGroup != null) {
            userGroup.setAffiliationValues();
        }
        return userGroup;
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = userGroupService.isValid(getUserGroup());
        if (validationErrorMsg.isEmpty()) {
            try {
                // Cache whether the entity is created or not.
                setCreated(!isManaged());
                userGroupService.save(getUserGroup());
                return postSave(true, isCreated());
            } catch (final Exception e) {
                getFacesMessagesManager().printError(e.getLocalizedMessage());
                logger.severe("Save UserGroup throws " + e);
            }
        } else {
            handleValidationErrors(validationErrorMsg);
        }
        return null;
    }
}
