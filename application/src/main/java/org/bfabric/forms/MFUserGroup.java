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

package org.bfabric.forms;

import java.util.HashSet;
import java.util.Set;

import org.bfabric.entity.Division;
import org.bfabric.entity.Institute;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUserGroup;

public class MFUserGroup extends AbstractMF {

    private final UserGroup userGroup;

    private final XMLRequestParameterSaveUserGroup xmlRequestSaveUserGroup;

    public MFUserGroup(UserGroup userGroup, XMLRequestParameterSaveUserGroup xmlRequestSaveUserGroup) {
        this.userGroup = userGroup;
        this.xmlRequestSaveUserGroup = xmlRequestSaveUserGroup;
    }

    @Override
    public void apply() throws Exception {
        if (getXmlRequestSaveUserGroup().getDivisionid() != null) {
            getUserGroup().setDivisionHierarchy(getDivision());
        }
        if (getXmlRequestSaveUserGroup().getInstituteid() != null) {
            getUserGroup().setInstituteHierarchy(getInstitute());
        }
        getUserGroup().setHidden(getHidden());
        getUserGroup().setForEmployeesOnly(getForEmployeesOnly());
        getUserGroup().setInternal(getInternal());
        getUserGroup().setUsers(getUsers());
        getUserGroup().setName(getName());
        getUserGroup().setSupervisor(getSupervisor());
    }

    public Division getDivision() throws InvalidDataException {
        if (getXmlRequestSaveUserGroup().getDivisionid() != null) {
            return (Division) fetch(Division.class, MFHelper.positiveLongValueOf("divisionid", getXmlRequestSaveUserGroup().getDivisionid()));
        }
        return getUserGroup().getDivision();
    }

    public Boolean getForEmployeesOnly() throws InvalidDataException {
        if (getXmlRequestSaveUserGroup().getForEmployeesOnly() != null) {
            return MFHelper.booleanValueOf("foremployeesonly", getXmlRequestSaveUserGroup().getForEmployeesOnly());
        }
        return getUserGroup().isForEmployeesOnly();
    }

    public Boolean getHidden() throws InvalidDataException {
        if (getXmlRequestSaveUserGroup().getHidden() != null) {
            return MFHelper.booleanValueOf("hidden", getXmlRequestSaveUserGroup().getHidden());
        }
        return getUserGroup().isHidden();
    }

    public Institute getInstitute() throws InvalidDataException {
        if (getXmlRequestSaveUserGroup().getInstituteid() != null) {
            return (Institute) fetch(Institute.class, MFHelper.positiveLongValueOf("instituteid", getXmlRequestSaveUserGroup().getInstituteid()));
        }
        return getUserGroup().getInstitute();
    }

    public Boolean getInternal() throws InvalidDataException {
        if (getXmlRequestSaveUserGroup().getInternal() != null) {
            return MFHelper.booleanValueOf("internal", getXmlRequestSaveUserGroup().getInternal());
        }
        return getUserGroup().isInternal();
    }

    public String getName() {
        if (getXmlRequestSaveUserGroup().getName() != null) {
            return getXmlRequestSaveUserGroup().getName();
        }
        return getUserGroup().getName();
    }

    private User getSupervisor() throws InvalidDataException {
        if (getXmlRequestSaveUserGroup().getSupervisorid() != null) {
            MFHelper.checkNotNull("supervisorid", getXmlRequestSaveUserGroup().getSupervisorid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSaveUserGroup().getSupervisorid()));
        }
        return getUserGroup().getSupervisor();
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public Set<User> getUsers() throws InvalidDataException {
        if (getXmlRequestSaveUserGroup().getUsers() != null) {
            Set<User> users = new HashSet<>();
            for (String user : getXmlRequestSaveUserGroup().getUsers()) {
                if (StringHelper.isNotEmpty(user)) {
                    users.add((User) fetch(User.class, MFHelper.positiveLongValueOf("users", user)));
                }
            }
            return users;
        }
        return getUserGroup().getUsers();
    }

    public XMLRequestParameterSaveUserGroup getXmlRequestSaveUserGroup() {
        return xmlRequestSaveUserGroup;
    }
}
