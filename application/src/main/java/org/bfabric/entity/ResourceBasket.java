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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class ResourceBasket extends AbstractNamedBaseEntity implements ShowScreen, NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "resourcebasketresource", joinColumns = @JoinColumn(name = "resourcebasketid"), inverseJoinColumns = @JoinColumn(name = "resourceid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "resource")
    private Set<Resource> resources = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "userresourcebasket", joinColumns = @JoinColumn(name = "resourcebasketid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "user")
    private Set<User> users = new HashSet<>();

    public void addResource(Resource resource) {
        if (resource != null) {
            getResources().add(resource);
        }
    }

    public void addResources(Collection<Resource> collection) {
        for (Resource resource : collection) {
            addResource(resource);
        }
    }

    public boolean contains(Resource resource) {
        return resource != null && getResources() != null && getResources().contains(resource);
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public String getUserNames() {
        StringBuilder names = new StringBuilder();

        int count = 0;
        for (User user : getUsers()) {
            if (count > 0) {
                names.append(", ");
            }
            names.append(user.getFirstName()).append(" ").append(user.getLastName());
            count++;
        }

        return names.toString();
    }

    public String getUserNamesTruncated(int length) {
        return StringHelper.truncate(getUserNames(), length);
    }

    public Collection<User> getUsers() {
        return users;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || getUsers().contains(getCurrentUser());
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
