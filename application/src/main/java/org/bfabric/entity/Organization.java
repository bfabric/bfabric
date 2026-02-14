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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.service.OrganizationEmailSuffixService;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "organization_name_unique", columnNames = { "name", "organizationtypeid" }) })
@XmlRootElement
@NamedQuery(name = "Organization.findByNameAndOrganizationType", query = "SELECT a FROM Organization a WHERE lower(a.name) = lower(:name) AND a.organizationType = :organizationType")
@NamedQuery(name = "Organization.findUnassigned", query = "SELECT a FROM Organization a WHERE NOT EXISTS (SELECT d.id FROM Department d WHERE d.organization = a)")
@NamedQuery(name = "Organization.checkUniqueName", query = "SELECT a.id FROM Organization a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.organizationType = :organizationType")
public class Organization extends AbstractOrganizationTypeDependentEntity implements ShowScreen, Mergeable {

    private static final long serialVersionUID = 1;

    @Min(0)
    @Max(999999)
    @XmlElement
    private Long debitorNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defaultBookingTypeId")
    @NotNull
    @XmlIDREF
    private BookingType defaultBookingType;

    @OneToMany(mappedBy = "organization")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Department> departments = new HashSet<>();

    @OneToMany(mappedBy = "organization")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrganizationEmailSuffix> organizationEmailSuffixes = new HashSet<>();

    @Column(length = 32)
    @Size(max = 32)
    @XmlElement
    private String vatNumber;

    public Organization() {
        super();
    }

    public Long getDebitorNumber() {
        return debitorNumber;
    }

    public BookingType getDefaultBookingType() {
        return defaultBookingType;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "vatNumber", getVatNumber());
        addEntityInfoItem(summary, "debitorNumber", getDebitorNumber());
        return summary.toString();
    }

    @Override
    public List<User> getMembers() {
        if (members == null) {
            members = CDI.current().select(UserService.class).get().getUsersByOrganizationId(getId());
        }
        return members;
    }

    public Set<OrganizationEmailSuffix> getOrganizationEmailSuffixes() {
        return organizationEmailSuffixes;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getDepartments().isEmpty();
    }

    public boolean isEth() {
        return getName() != null && getName().startsWith(Constants.ETH_ZURICH);
    }

    public boolean isNotMatchingEmail(String email) {
        return StringHelper.isNotEmpty(email) && email.indexOf('@') > 1 && isNotMatchingEmailSuffix(email.substring(email.indexOf('@') + 1));
    }

    public boolean isNotMatchingEmailSuffix(String emailSuffix) {
        return CDI.current().select(OrganizationEmailSuffixService.class).get().isNotMatching(emailSuffix, getId());
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    public boolean isUzh() {
        return getName() != null && getName().startsWith(Constants.UZH);
    }

    public void setDebitorNumber(Long debitorNumber) {
        this.debitorNumber = debitorNumber;
    }

    public void setDefaultBookingType(BookingType defaultBookingType) {
        this.defaultBookingType = defaultBookingType;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = StringHelper.format(vatNumber);
    }
}