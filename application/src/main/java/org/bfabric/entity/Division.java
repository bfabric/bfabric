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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@XmlRootElement
@NamedQuery(name = "Division.findByNameAndCompany", query = "SELECT a FROM Division a WHERE lower(a.name) = lower(:name) and a.company = :company")
@NamedQuery(name = "Division.findByNameAndCompanyName", query = "SELECT a FROM Division a WHERE lower(a.name) = lower(:name) and  lower(a.company.name) =  lower(:companyName)")
@NamedQuery(name = "Division.findUnassigned", query = "SELECT a FROM Division a WHERE NOT EXISTS (SELECT c.id FROM Container c WHERE c.division = a) AND NOT EXISTS (SELECT u.id FROM User u WHERE u.division = a) AND NOT EXISTS (SELECT b.id FROM Booking b WHERE b.division = a) AND NOT EXISTS (SELECT ubi.id FROM UserBillingInfo ubi WHERE ubi.division = a)")
@NamedQuery(name = "Division.checkUniqueName", query = "SELECT a.id FROM Division a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.company = :company")
public class Division extends AbstractNamedBaseEntity implements ShowScreen, Mergeable {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "division")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Booking> bookings = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyid")
    @NotNull
    @XmlIDREF
    private Company company;

    @OneToMany(mappedBy = "division")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "division")
    @Where(clause = "discriminator = 'Order'")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "division")
    @Where(clause = "discriminator = 'Project'")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Project> projects = new HashSet<>();

    @OneToMany(mappedBy = "division")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<UserBillingInfo> userBillingInfos = new HashSet<>();

    @OneToMany(mappedBy = "division")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<UserGroup> userGroups = new HashSet<>();

    public Division() {
        super();
    }

    public String getAffiliation() {
        String affiliation = getCompany().getName();
        if (StringHelper.isNotEmpty(getName()) && !getName().equals(getConfiguration().getDefaultDivision())) {
            affiliation += ", " + getName();
        }
        return affiliation;
    }

    public List<String> getAffiliationAsList() {
        List<String> strList = new ArrayList<>();

        strList.add(getCompany().getName());
        if (StringHelper.isNotEmpty(getName()) && !getName().equals(getConfiguration().getDefaultDivision())) {
            strList.add(getName());
        }

        return strList;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public Company getCompany() {
        return company;
    }

    public String getCompanyName() {
        return getCompany() != null ? getCompany().getName() : null;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AFFILIATIONMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getCompany() != null) {
            addEntityInfoItem(summary, "company", getCompany().getName());
        }
        return summary.toString();
    }

    public String getFullName() {
        return getName() + " - " + getCompany().getFullName();
    }

    public Set<User> getMembers() {
        return members;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public OrganizationType getOrganizationType() {
        return getCompany() != null ? getCompany().getOrganizationType() : null;
    }

    public OrganizationType getOrganizationTypeForBilling() {
        return getCompany() != null ? getCompany().getOrganizationTypeForBilling() : null;
    }

    public String getOrganizationTypeName() {
        return getOrganizationType() != null ? getOrganizationType().getName() : null;
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
        return getUserBillingInfos().isEmpty() && getMembers().isEmpty() && getProjects().isEmpty() && getOrders().isEmpty() && getBookings().isEmpty() && isUpdatable();
    }

    public boolean isMember(User user) {
        return user != null && this.equals(user.getDivision());
    }

    public boolean isNotMatchingEmail(String email) {
        return getCompany() != null && getCompany().isNotMatchingEmail(email);
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.AFFILIATIONREADER) || isMember(getCurrentUser());
    }

    public boolean isSet() {
        return getName() != null && !getName().equals(getConfiguration().getDefaultDivision());
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
}
