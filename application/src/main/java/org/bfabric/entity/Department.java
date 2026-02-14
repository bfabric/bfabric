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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "department_name_unique", columnNames = { "name", "organizationid" }) })
@XmlRootElement
@NamedQuery(name = "Department.findByNameAndOrganization", query = "SELECT a FROM Department a WHERE lower(a.name) = lower(:name) and a.organization = :organization")
@NamedQuery(name = "Department.findUnassigned", query = "SELECT a FROM Department a WHERE NOT EXISTS (SELECT i.id FROM Institute i WHERE i.department = a)")
@NamedQuery(name = "Department.checkUniqueName", query = "SELECT a.id FROM Department a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.organization = :organization")
public class Department extends AbstractNamedBaseEntity implements ShowScreen, Mergeable {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "department")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Institute> institutes = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "institutedepartment", joinColumns = @JoinColumn(name = "departmentid"), inverseJoinColumns = @JoinColumn(name = "instituteid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Institute> jointInstitutes = new HashSet<>();

    @Transient
    private List<User> members;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationid")
    @NotNull
    @XmlIDREF
    private Organization organization;

    public Department() {
        super();
    }

    public List<Institute> getAllInstitutes() {
        Set<Institute> allInstitutes = new HashSet<>();
        allInstitutes.addAll(getInstitutes());
        allInstitutes.addAll(getJointInstitutes());
        return CollectionHelper.sortObjects(allInstitutes);
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AFFILIATIONMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getOrganization() != null) {
            addEntityInfoItem(summary, "organization", getOrganization().getName());
        }
        return summary.toString();
    }

    public String getFullName() {
        return name + " - " + getOrganization().getFullName();
    }

    public Set<Institute> getInstitutes() {
        return institutes;
    }

    public Set<Institute> getJointInstitutes() {
        return jointInstitutes;
    }

    public List<User> getMembers() {
        if (members == null) {
            members = CDI.current().select(UserService.class).get().getUsersByDepartmentId(getId());
        }
        return members;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getOrganizationName() {
        return getOrganization() != null ? getOrganization().getName() : null;
    }

    public String getOrganizationTypeName() {
        return getOrganization() != null ? getOrganization().getOrganizationTypeName() : null;
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getInstitutes().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setInstitutes(Set<Institute> institutes) {
        this.institutes = institutes;
    }

    public void setJointInstitutes(Set<Institute> jointInstitutes) {
        this.jointInstitutes = jointInstitutes;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}