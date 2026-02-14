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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "organizationemailsuffix_name_unique", columnNames = { "name", "organizationid" }) })
@XmlRootElement
@NamedQuery(name = "OrganizationEmailSuffix.findAll", query = "SELECT a FROM OrganizationEmailSuffix a ORDER BY a.id")
@NamedQuery(name = "OrganizationEmailSuffix.checkUniqueName", query = "SELECT a.id FROM OrganizationEmailSuffix a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.organization = :organization")
@NamedQuery(name = "OrganizationEmailSuffix.checkName", query = "SELECT a.id FROM OrganizationEmailSuffix a WHERE (lower(:name) LIKE '%' || lower(a.name) or a.organization.id = :organizationId) and not exists(SELECT b.id FROM OrganizationEmailSuffix b where lower(:name) LIKE '%' || lower(b.name) and b.organization.id = :organizationId)")
public class OrganizationEmailSuffix extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationid")
    @NotNull
    @XmlIDREF
    private Organization organization;

    public OrganizationEmailSuffix() {
        super();
    }

    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}