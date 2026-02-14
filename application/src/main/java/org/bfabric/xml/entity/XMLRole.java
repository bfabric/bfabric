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

import org.bfabric.entity.Role;
import org.bfabric.entity.User;

@XmlRootElement(name = "role")
public class XMLRole extends XMLAbstractNamedBaseEntity {

    @XmlElement
    private List<XMLRole> group = new ArrayList<>();

    @XmlElement
    private List<XMLRole> parentrole = new ArrayList<>();

    @XmlElement
    private List<XMLUser> user = new ArrayList<>();

    public XMLRole() {
    }

    public XMLRole(Role entity, boolean reference) {
        super(entity, reference);
    }

    public XMLRole(Role role) {
        super(role);
        if (role != null) {
            if (role.getGroups() != null) {
                for (Role aGroup : role.getGroups()) {
                    getGroup().add(new XMLRole(aGroup, true));
                }
            }
            if (role.getParents() != null) {
                for (Role parentRole : role.getParents()) {
                    getParentrole().add(new XMLRole(parentRole, true));
                }
            }
            if (role.getUsers() != null) {
                for (User aUser : role.getUsers()) {
                    getUser().add(new XMLUser(aUser, true));
                }
            }
        }
    }

    public List<XMLRole> getGroup() {
        return group;
    }

    public List<XMLRole> getParentrole() {
        return parentrole;
    }

    public List<XMLUser> getUser() {
        return user;
    }

    public void setGroup(List<XMLRole> group) {
        this.group = group;
    }

    public void setParentrole(List<XMLRole> parentrole) {
        this.parentrole = parentrole;
    }

    public void setUser(List<XMLUser> user) {
        this.user = user;
    }
}
