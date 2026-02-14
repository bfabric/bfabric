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

import java.util.List;
import java.util.Optional;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.enums.RoleEnum;

@MappedSuperclass
public abstract class AbstractOrganizationTypeDependentEntity extends AbstractNamedBaseEntity {

    private static final long serialVersionUID = 1;

    @Transient
    protected List<Institute> institutes;

    @Transient
    protected List<User> members;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingorganizationtypeid")
    @XmlIDREF
    private OrganizationType billingOrganizationType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationtypeid")
    @XmlIDREF
    private OrganizationType organizationType;

    public AbstractOrganizationTypeDependentEntity() {
        super();
    }

    public OrganizationType getBillingOrganizationType() {
        return this.billingOrganizationType;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AFFILIATIONMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getOrganizationType() != null) {
            addEntityInfoItem(summary, "organizationType", getOrganizationType().getName());
        }
        if (getBillingOrganizationType() != null) {
            addEntityInfoItem(summary, "billingOrganizationType", getBillingOrganizationType().getName());
        }
        return summary.toString();
    }

    public String getFullName() {
        return getName() + " - " + getOrganizationType().getName();
    }

    public List<Institute> getInstitutes() {
        return institutes;
    }

    public List<User> getMembers() {
        return members;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public OrganizationType getOrganizationTypeForBilling() {
        return Optional.ofNullable(getBillingOrganizationType()).orElse(getOrganizationType());
    }

    public String getOrganizationTypeName() {
        return getOrganizationType() != null ? getOrganizationType().getName() : null;
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isMember(User user) {
        return user != null && getMembers().contains(user);
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.AFFILIATIONREADER) || isMember(getCurrentUser());
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setBillingOrganizationType(OrganizationType billingOrganizationType) {
        this.billingOrganizationType = billingOrganizationType;
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }
}