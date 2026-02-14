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
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "accessrequestprofile_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "AccessRequestProfile.findEnabled", query = "SELECT a FROM AccessRequestProfile a WHERE a.enabled = true ORDER BY a.orderPosition")
public class AccessRequestProfile extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotBlank
    @Size(max = 256)
    @XmlElement
    private String accessGranted;

    @OneToMany(mappedBy = "accessRequestProfile")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<AccessRequest> accessRequests = new HashSet<>();

    public AccessRequestProfile() {
        super();
    }

    public String getAccessGranted() {
        return accessGranted;
    }

    public Set<AccessRequest> getAccessRequests() {
        return accessRequests;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ACCESSREQUESTMANAGER;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getAccessGranted())) {
            addEntityInfoItem(summary, "accessGranted", getAccessGranted());
        }
        return summary.toString();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getAccessRequests().isEmpty();
    }

    public void setAccessGranted(String accessGranted) {
        this.accessGranted = StringHelper.format(accessGranted);
    }

    public void setAccessRequests(Set<AccessRequest> accessRequests) {
        this.accessRequests = accessRequests;
    }
}