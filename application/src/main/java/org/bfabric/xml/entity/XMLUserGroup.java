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

package org.bfabric.xml.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;

@XmlRootElement(name = "usergroup")
public class XMLUserGroup extends XMLAbstractSupervisorBasedEntity {

    @XmlElement
    protected Boolean internal;

    @XmlElement
    protected Boolean hidden;

    @XmlElement
    protected Boolean foremployeesonly;

    @XmlElement
    private String division;

    @XmlElement
    private String institute;

    @XmlElement
    private List<XMLUser> trackingusers = new ArrayList<>();

    @XmlElement
    private List<XMLUser> users = new ArrayList<>();

    public XMLUserGroup() {
    }

    public XMLUserGroup(UserGroup entity, boolean reference) {
        super(entity, reference);
    }

    public XMLUserGroup(UserGroup entity) {
        super(entity);
        if (entity != null) {
            setHidden(entity.isHidden());
            setForemployeesonly(entity.isForEmployeesOnly());
            setInternal(entity.isInternal());
            if (entity.getInstitute() != null) {
                setInstitute(entity.getInstitute().getIdString());
            }
            if (entity.getDivision() != null) {
                setDivision(entity.getDivision().getIdString());
            }
            if (entity.getUsers() != null) {
                for (User user : entity.getUsers()) {
                    getUsers().add(new XMLUser(user, true));
                }
            }
            if (entity.getTrackingUsers() != null) {
                for (User user : entity.getTrackingUsers()) {
                    getTrackingusers().add(new XMLUser(user, true));
                }
            }
        }
    }

    public String getDivision() {
        return division;
    }

    public Boolean getForemployeesonly() {
        return foremployeesonly;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public String getInstitute() {
        return institute;
    }

    public Boolean getInternal() {
        return internal;
    }

    public List<XMLUser> getTrackingusers() {
        return trackingusers;
    }

    public List<XMLUser> getUsers() {
        return users;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setForemployeesonly(Boolean foremployeesonly) {
        this.foremployeesonly = foremployeesonly;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public void setTrackingusers(List<XMLUser> trackingusers) {
        this.trackingusers = trackingusers;
    }

    public void setUsers(List<XMLUser> users) {
        this.users = users;
    }
}