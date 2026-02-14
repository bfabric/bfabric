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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "institute_name_unique", columnNames = { "name", "departmentid" }) })
@XmlRootElement
@NamedQuery(name = "Institute.findByNameAndDepartment", query = "SELECT a FROM Institute a WHERE lower(a.name) = lower(:name) and a.department = :department")
@NamedQuery(name = "Institute.findAllByOrganizationId", query = "SELECT a FROM Institute a WHERE a.department.organization.id = :organizationId or EXISTS(SELECT j.id FROM a.jointDepartment j WHERE j.id IN (SELECT d.id FROM Department d WHERE d.organization.id = :organizationId)) ORDER BY a.name")
@NamedQuery(name = "Institute.findUnassigned", query = "SELECT a FROM Institute a WHERE NOT EXISTS (SELECT c.id FROM Container c WHERE c.institute = a) AND NOT EXISTS (SELECT u.id FROM User u WHERE u.institute = a) AND NOT EXISTS (SELECT b.id FROM Booking b WHERE b.institute = a) AND NOT EXISTS (SELECT ubi.id FROM UserBillingInfo ubi WHERE ubi.institute = a)")
@NamedQuery(name = "Institute.checkUniqueName", query = "SELECT a.id FROM Institute a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.department = :department")
public class Institute extends AbstractNamedBaseEntity implements ShowScreen, Mergeable, HasAffiliation {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "institute")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "institute")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Container> containers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentid")
    @NotNull
    @XmlIDREF
    private Department department;

    @ManyToMany
    @JoinTable(name = "institutedepartment", joinColumns = @JoinColumn(name = "instituteid"), inverseJoinColumns = @JoinColumn(name = "departmentid"))
    @OrderBy("id desc")
    @XmlIDREF
    @XmlElement(name = "department")
    private Set<Department> jointDepartment = new HashSet<>();

    @OneToMany(mappedBy = "institute")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "institute")
    @Where(clause = "discriminator = 'Order'")
    @OrderBy("id desc")
    private Set<Order> orders = new HashSet<>();

    @Transient
    private Organization organization;

    @OneToMany(mappedBy = "institute")
    @Where(clause = "discriminator = 'Project'")
    @OrderBy("id desc")
    private Set<Project> projects = new HashSet<>();

    @OneToMany(mappedBy = "institute")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<UserBillingInfo> userBillingInfos = new HashSet<>();

    @OneToMany(mappedBy = "institute")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<UserGroup> userGroups = new HashSet<>();

    public Institute() {
        super();
    }

    public String getAffiliation() {
        return getDepartment().getOrganization().getName() + ", " + getDepartment().getName() + ", " + getName();
    }

    public List<String> getAffiliationAsList() {
        List<String> strList = new ArrayList<>();

        strList.add(getDepartment().getOrganization().getName());
        strList.add(getDepartment().getName());
        strList.add(getName());

        return strList;
    }

    public List<Department> getAllDepartments() {
        List<Department> allDepartments = new ArrayList<>();
        allDepartments.add(getDepartment());
        allDepartments.addAll(getJointDepartment());
        return allDepartments;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    @Override
    public Company getCompany() {
        return null;
    }

    @Override
    public String getCompanyName() {
        return null;
    }

    public Set<Container> getContainers() {
        return containers;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AFFILIATIONMANAGER;
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    public String getDepartmentName() {
        return getDepartment() != null ? getDepartment().getName() : null;
    }

    @Override
    public Division getDivision() {
        return null;
    }

    @Override
    public String getDivisionName() {
        return null;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getDepartment() != null) {
            addEntityInfoItem(summary, "department", getDepartment().getName());
            addEntityInfoItem(summary, "organization", getDepartment().getOrganization().getName());
        }
        if (getJointDepartment() != null && !getJointDepartment().isEmpty()) {
            addEntityInfoItem(summary, "jointDepartment", getJointDepartment().size());
        }
        return summary.toString();
    }

    public String getFullName() {
        return name + " - " + getDepartment().getFullName();
    }

    @Override
    public Institute getInstitute() {
        return this;
    }

    public Set<Department> getJointDepartment() {
        return jointDepartment;
    }

    public List<Department> getJointDepartmentAsList() {
        return CollectionHelper.asList(jointDepartment);
    }

    public Set<User> getMembers() {
        return members;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    public String getOrganizationName() {
        return getDepartment() != null ? getDepartment().getOrganizationName() : null;
    }

    @Override
    public OrganizationType getOrganizationType() {
        return null;
    }

    public OrganizationType getOrganizationTypeForBilling() {
        return getDepartment() != null && getDepartment().getOrganization() != null ? getDepartment().getOrganization().getOrganizationTypeForBilling() : null;
    }

    public String getOrganizationTypeName() {
        return getDepartment() != null ? getDepartment().getOrganizationTypeName() : null;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Set<UserBillingInfo> getUserBillingInfos() {
        return userBillingInfos;
    }

    public Set<UserGroup> getUserGroups() {
        return userGroups;
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return getUserBillingInfos().isEmpty() && getMembers().isEmpty() && getContainers().isEmpty() && getBookings().isEmpty() && isUpdatable();
    }

    public boolean isEmailOrganizationNotMatching(String email) {
        return getDepartment() != null && getDepartment().getOrganization() != null && getDepartment().getOrganization().isNotMatchingEmail(email);
    }

    public boolean isOrganizationTypeCHUni() {
        return getDepartment().getOrganization().getOrganizationType().isCHUni();
    }

    public boolean isOrganizationTypeUniZH() {
        return getDepartment().getOrganization().getOrganizationType().isUniZH();
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    @Override
    public void setCompany(Company company) {
    }

    @Override
    public void setCompanyName(String companyName) {
    }

    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

    @Override
    public void setDepartment(Department department) {
        this.department = department;
        if (department != null) {
            getJointDepartment().remove(department);
        }
    }

    @Override
    public void setDivision(Division division) {
    }

    @Override
    public void setDivisionName(String divisionName) {
    }

    @Override
    public void setInstitute(Institute institute) {
    }

    public void setJointDepartment(Set<Department> jointDepartment) {
        this.jointDepartment = jointDepartment;
    }

    public void setJointDepartmentAsList(List<Department> jointDepartment) {
        if (jointDepartment != null) {
            jointDepartment.remove(getDepartment());
        }
        this.jointDepartment = (Set<Department>) CollectionHelper.asSet(jointDepartment);
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public void setOrganizationType(OrganizationType organizationType) {
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
}