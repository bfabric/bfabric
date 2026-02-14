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

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "UserGroup.findAllEnabledInternalOrderByName", query = "SELECT a FROM UserGroup a WHERE a.enabled = true and a.internal = true ORDER BY a.name")
public class UserGroup extends AbstractSupervisorNamedBaseEntity implements ShowScreen, HasAffiliation {

    private static final long serialVersionUID = 1;

    @Transient
    private Company company;

    @Transient
    private String companyName;

    @Transient
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisionid")
    @XmlIDREF
    private Division division;

    @Transient
    private String divisionName;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean forEmployeesOnly = true;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean hidden = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituteid")
    @XmlIDREF
    private Institute institute;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean internal = false;

    @Transient
    private Organization organization;

    @Transient
    private OrganizationType organizationType;

    @OneToMany(mappedBy = "defaultUserGroup")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> trackingUsers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "usergroupuser", joinColumns = @JoinColumn(name = "usergroupid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("lastName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "user")
    private Set<User> users = new HashSet<>();

    public UserGroup() {
        super();
    }

    public void addSupervisorAsMember() {
        getUsers().add(getSupervisor());
    }

    @Override
    public UserGroup clone() throws CloneNotSupportedException {
        UserGroup clone = (UserGroup) super.clone();
        clone.trackingUsers = new HashSet<>();
        clone.users = new HashSet<>();
        if (!getUsers().isEmpty()) {
            clone.users.addAll(getUsers());
        }
        return clone;
    }

    public void forEmployeesOnlyChanged(ValueChangeEvent event) {
        setForEmployeesOnly((Boolean) event.getNewValue());
        if (isForEmployeesOnly()) {
            Set<User> usersToRemove = new HashSet<>();
            for (User user : getUsers()) {
                if (!user.isEmployee()) {
                    usersToRemove.add(user);
                }
            }
            getUsers().removeAll(usersToRemove);
        }
    }

    @Override
    public Company getCompany() {
        return company;
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.USERGROUPMANAGER;
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public Division getDivision() {
        return division;
    }

    @Override
    public String getDivisionName() {
        return divisionName;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getUsers() != null && !getUsers().isEmpty()) {
            addEntityInfoItem(summary, "users", getUsers().size());
        }
        addEntityInfoItem(summary, "internal", isInternal());
        addEntityInfoItem(summary, "hidden", isHidden());
        if (getInstitute() != null) {
            addEntityInfoItem(summary, "organization", getInstitute().getOrganizationName());
            addEntityInfoItem(summary, "department", getInstitute().getDepartmentName());
            addEntityInfoItem(summary, "institute", getInstitute().getName());
        }
        if (getDivision() != null) {
            addEntityInfoItem(summary, "company", getDivision().getCompanyName());
            if (getDivision().isSet()) {
                addEntityInfoItem(summary, "division", getDivision().getName());
            }
        }
        return summary.toString();
    }

    public Institute getInstitute() {
        return institute;
    }

    @Override
    @Size(max = 64)
    public String getName() {
        return super.getName();
    }

    public String getNameWithUserNames() {
        StringBuilder nameWithUserNames = new StringBuilder(getName());

        if (!getUsers().isEmpty()) {
            boolean first = true;
            nameWithUserNames.append(" (");

            for (User user : getUsers()) {
                if (!first) {
                    nameWithUserNames.append(", ");
                } else {
                    first = false;
                }
                nameWithUserNames.append(user.getLastName());
            }

            nameWithUserNames.append(")");
        }

        return nameWithUserNames.toString();
    }

    public String getNameWithUserNamesTrunc(int maxLength) {
        return StringHelper.truncate(getNameWithUserNames(), maxLength);
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public Set<User> getTrackingUsers() {
        return trackingUsers;
    }

    public Set<User> getUsers() {
        return users;
    }

    public List<User> getUsersAsList() {
        return CollectionHelper.asList(users);
    }

    public boolean isDeletable() {
        return isAdminOrSupervisor();
    }

    public boolean isForEmployeesOnly() {
        return forEmployeesOnly;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isInternal() {
        return internal;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.USERGROUPREADER) || hasCurrentUserRoleEnum(RoleEnum.USER);
    }

    public boolean isSupervisorMember() {
        return getUsers().contains(getSupervisor());
    }

    public boolean isUpdatable() {
        return isInternal() && hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isAdminOrSupervisor();
    }

    @Override
    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public void setCompanyName(String companyName) {
        this.companyName = StringHelper.format(companyName);
    }

    @Override
    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public void setDivisionHierarchy(Division division) {
        setDivision(division);
        setDivisionName(getDivision().getName());
        setCompanyName(getDivision().getCompanyName());
        setOrganizationType(getDivision().getOrganizationType());
    }

    @Override
    public void setDivisionName(String divisionName) {
        this.divisionName = StringHelper.format(divisionName);
    }

    public void setForEmployeesOnly(boolean forEmployeesOnly) {
        this.forEmployeesOnly = forEmployeesOnly;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public void setInstitute(Institute institute) {
        this.institute = institute;
    }

    public void setInstituteHierarchy(Institute institute) {
        setInstitute(institute);
        setDepartment(getInstitute().getDepartment());
        setOrganization(getDepartment().getOrganization());
        setOrganizationType(getOrganization().getOrganizationType());
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void setUsersAsList(List<User> users) {
        this.users = (Set<User>) CollectionHelper.asSet(users);
    }
}
