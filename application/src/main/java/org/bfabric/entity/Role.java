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

package org.bfabric.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.comparator.NameComparator;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Role.findByName", query = "SELECT a FROM Role a WHERE a.name = :name")
@NamedQuery(name = "Role.findAllSpecific", query = "SELECT a FROM Role a WHERE a.name = 'user' ORDER BY a.name")
public class Role extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "rolegroup", joinColumns = @JoinColumn(name = "roleid"), inverseJoinColumns = @JoinColumn(name = "groupid"))
    @OrderBy(value = "name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "group")
    private Set<Role> groups = new HashSet<>();

    @Transient
    private Set<Role> impliedRoles = null;

    @ManyToMany
    @JoinTable(name = "rolegroup", joinColumns = @JoinColumn(name = "groupid"), inverseJoinColumns = @JoinColumn(name = "roleid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Role> parents = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "userrole", joinColumns = @JoinColumn(name = "roleid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> users = new HashSet<>();

    public boolean addGroup(final Role role) {
        if (role == null) {
            throw new NullPointerException("Adding a null role is not allowed");
        }
        return getGroups().add(role);
    }

    public void addUser(User user) {
        if (user != null) {
            getUsers().add(user);
            user.addRole(this);
        }
    }

    public void addUsers(Collection<User> newUsers) {
        for (User newUser : newUsers) {
            addUser(newUser);
        }
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ROLEMANAGER;
    }

    public List<Role> getDescendants() {
        List<Role> descendantRoles = new ArrayList<>();
        for (Role role : getImpliedRoles()) {
            if (!getGroups().contains(role) && role != this) {
                descendantRoles.add(role);
            }
        }
        descendantRoles.sort(new NameComparator<>());
        return descendantRoles;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getGroups() != null && !getGroups().isEmpty()) {
            addEntityInfoItem(summary, "groups", getGroups().size());
        }
        if (getParents() != null && !getParents().isEmpty()) {
            addEntityInfoItem(summary, "parents", getParents().size());
        }
        return summary.toString();
    }

    public Set<Role> getGroups() {
        return groups;
    }

    public Set<Role> getImpliedRoles() {
        if (impliedRoles == null) {
            impliedRoles = getImpliedRoles(this);
        }
        return impliedRoles;
    }

    public Set<Role> getImpliedRoles(Role parentRole) {
        Set<Role> transitiveRoles = new HashSet<>();
        transitiveRoles.add(parentRole);
        for (Role role : parentRole.getGroups()) {
            transitiveRoles.addAll(getImpliedRoles(role));
        }
        return transitiveRoles;
    }

    public Set<Role> getParents() {
        return parents;
    }

    public Set<User> getUsers() {
        return users;
    }

    @Override
    public boolean isDeletable() {
        // Can be deleted if there are no dependencies ({@link User}s, {@link Role}s)
        return isUpdatable() && getUsers().isEmpty() && getParents().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.ROLEREADER);
    }

    public boolean removeGroup(final Role role) {
        return getGroups().remove(role);
    }

    public void setGroups(Set<Role> groups) {
        this.groups = groups;
    }

    public void setImpliedRoles(Set<Role> impliedRoles) {
        this.impliedRoles = impliedRoles;
    }

    public void setParents(Set<Role> parents) {
        this.parents = parents;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
