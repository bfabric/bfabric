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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.BookingService;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "organizationtype_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "OrganizationType.findAll", query = "SELECT a FROM OrganizationType a ORDER BY a.id")
@NamedQuery(name = "OrganizationType.findAcademic", query = "SELECT a FROM OrganizationType a WHERE a.academic = true")
@NamedQuery(name = "OrganizationType.findNonAcademic", query = "SELECT a FROM OrganizationType a WHERE a.academic = false")
public class OrganizationType extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean academic = false;

    @OneToMany(mappedBy = "billingOrganizationType")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Company> billingCompanies = new HashSet<>();

    @OneToMany(mappedBy = "billingOrganizationType")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Organization> billingOrganizations = new HashSet<>();

    @Transient
    private BigInteger bookingCount;

    @Column(length = 9)
    @Size(max = 9)
    @XmlElement
    private String color;

    @OneToMany(mappedBy = "organizationType")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Company> companies = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean domestic = false;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean extensible = true;

    @OneToMany(mappedBy = "organizationType")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<Offer> offers = new ArrayList<>();

    @OneToMany(mappedBy = "organizationType")
    @OrderBy("getOrganizationUsage(id) desc, name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Organization> organizations = new HashSet<>();

    @OneToMany(mappedBy = "organizationType", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ServiceOrganizationTypePrice> serviceOrganizationTypePrices = new HashSet<>();

    public OrganizationType() {
        super();
    }

    public Set<Company> getBillingCompanies() {
        return billingCompanies;
    }

    public Set<Organization> getBillingOrganizations() {
        return billingOrganizations;
    }

    public BigInteger getBookingCount() {
        if (bookingCount == null) {
            bookingCount = CDI.current().select(BookingService.class).get().getBookingCountByOrganizationTypeId(getId());
        }
        return bookingCount;
    }

    public String getColor() {
        return color;
    }

    public Set<Company> getCompanies() {
        return companies;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AFFILIATIONMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "academic", isAcademic());
        addEntityInfoItem(summary, "domestic", isDomestic());
        addEntityInfoItem(summary, "extensible", isExtensible());
        return summary.toString();
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public Set<Organization> getOrganizations() {
        return organizations;
    }

    public Set<ServiceOrganizationTypePrice> getServiceOrganizationTypePrices() {
        return serviceOrganizationTypePrices;
    }

    public boolean isAcademic() {
        return academic;
    }

    public boolean isCHUni() {
        return Constants.ORGANIZATIONTYPE_CH_UNI.equalsIgnoreCase(getName());
    }

    public boolean isCompany() {
        return !isAcademic();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getOrganizations().isEmpty() && getCompanies().isEmpty() && getOffers().isEmpty();
    }

    public boolean isDomestic() {
        return domestic;
    }

    @Override
    public boolean isExtensible() {
        return extensible;
    }

    public boolean isExternal() {
        return !isDomestic() || !isFinanceSourceRequired();
    }

    public boolean isFinanceSourceRequired() {
        return getName() != null && getName().startsWith("University in Zurich");
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    public boolean isUniZH() {
        return Constants.ORGANIZATIONTYPE_UNI_ZH.equalsIgnoreCase(getName());
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setAcademic(boolean academic) {
        this.academic = academic;
    }

    public void setBillingCompanies(Set<Company> billingCompanies) {
        this.billingCompanies = billingCompanies;
    }

    public void setBillingOrganizations(Set<Organization> billingOrganizations) {
        this.billingOrganizations = billingOrganizations;
    }

    public void setColor(String color) {
        this.color = StringHelper.format(color);
    }

    public void setCompanies(Set<Company> companies) {
        this.companies = companies;
    }

    public void setDomestic(boolean domestic) {
        this.domestic = domestic;
    }

    public void setExtensible(boolean extensible) {
        this.extensible = extensible;
    }

    public void setOrganizations(Set<Organization> organizations) {
        this.organizations = organizations;
    }

    public void setServiceOrganizationTypePrices(Set<ServiceOrganizationTypePrice> serviceOrganizationTypePrices) {
        this.serviceOrganizationTypePrices = serviceOrganizationTypePrices;
    }
}