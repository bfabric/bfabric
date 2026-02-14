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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.service.OrganizationEmailSuffixService;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Company.findByNameAndOrganizationType", query = "SELECT a FROM Company a WHERE lower(a.name) = lower(:name) and a.organizationType = :organizationType")
@NamedQuery(name = "Company.findUnassigned", query = "SELECT a FROM Company a WHERE NOT EXISTS (SELECT d.id FROM Division d WHERE d.company = a)")
@NamedQuery(name = "Company.checkUniqueName", query = "SELECT a.id FROM Company a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.organizationType = :organizationType")
public class Company extends AbstractOrganizationTypeDependentEntity implements ShowScreen, Mergeable {

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

    @OneToMany(mappedBy = "company")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Division> divisions = new HashSet<>();

    @Column(length = 32)
    @Size(max = 32)
    @XmlElement
    private String vatNumber;

    public Company() {
        super();
    }

    public Long getDebitorNumber() {
        return debitorNumber;
    }

    public BookingType getDefaultBookingType() {
        return defaultBookingType;
    }

    public Set<Division> getDivisions() {
        return divisions;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getVatNumber())) {
            addEntityInfoItem(summary, "vatNumber", getVatNumber());
        }
        if (getDebitorNumber() != null) {
            addEntityInfoItem(summary, "debitorNumber", getDebitorNumber());
        }
        return summary.toString();
    }

    @Override
    public List<User> getMembers() {
        if (members == null) {
            members = CDI.current().select(UserService.class).get().getUsersByCompanyId(getId());
        }
        return members;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getDivisions().isEmpty();
    }

    @Override
    public boolean isMember(User user) {
        return user != null && user.getDivision() != null && this.equals(user.getDivision().getCompany());
    }

    public boolean isNotMatchingEmail(String email) {
        return StringHelper.isNotEmpty(email) && email.indexOf('@') > 1 && isNotMatchingEmailSuffix(email.substring(email.indexOf('@') + 1));
    }

    public boolean isNotMatchingEmailSuffix(String emailSuffix) {
        return CDI.current().select(OrganizationEmailSuffixService.class).get().isNotMatching(emailSuffix, 0L);
    }

    public void setDebitorNumber(Long debitorNumber) {
        this.debitorNumber = debitorNumber;
    }

    public void setDefaultBookingType(BookingType defaultBookingType) {
        this.defaultBookingType = defaultBookingType;
    }

    public void setDivisions(Set<Division> divisions) {
        this.divisions = divisions;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = StringHelper.format(vatNumber);
    }
}
