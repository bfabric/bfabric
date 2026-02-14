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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@XmlRootElement
public class AccessRequestType extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "accessRequestType", cascade = CascadeType.REMOVE)
    @OrderBy("id DESC")
    @XmlIDREF
    private Set<AccessRequest> accessRequests = new HashSet<>();

    public static String getAccessRequestTypeName(String accessCardCode) {
        if (accessCardCode != null) {
            if (isValidPersonalAccessCardCode(accessCardCode)) {
                return "Personal Card Access";
            }
            if (isValidGuestAccessCardCode(accessCardCode)) {
                return "Guest Card Extension";
            }
        }
        return "Guest Card Application";
    }

    public static boolean isValidAccessCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches(ConfigurationHelper.getConfiguration().getAccessCardNumberPattern());
    }

    public static boolean isValidGuestAccessCardCode(String cardCode) {
        return StringHelper.isNotEmpty(cardCode) && StringHelper.firstUpper(cardCode).matches(ConfigurationHelper.getConfiguration().getAccessCardCodeGuestPattern());
    }

    public static boolean isValidPersonalAccessCardCode(String cardCode) {
        return StringHelper.isNotEmpty(cardCode) && cardCode.matches(ConfigurationHelper.getConfiguration().getAccessCardCodePattern());
    }

    public Set<AccessRequest> getAccessRequests() {
        return accessRequests;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ACCESSREQUESTMANAGER;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.ACCESSREQUESTREADER);
    }

    @Override
    public boolean isUpdatable() {
        return getAccessRequests().isEmpty();
    }

    public void setAccessRequests(Set<AccessRequest> accessRequests) {
        this.accessRequests = accessRequests;
    }
}
