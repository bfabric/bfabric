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

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class ContactInfo extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Embedded
    @XmlElement
    private Address address;

    @Size(max = 64)
    @Email
    @XmlElement
    private String email;

    @Size(max = 1024)
    @XmlElement
    private String mapUrl;

    @Size(max = 64)
    @XmlElement
    private String officeHours;

    @Embedded
    @XmlElement
    private PhoneNumber phoneNumber;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "contactInfo")
    @OrderBy("name")
    private Set<ServiceArea> serviceAreas = new HashSet<>();

    public ContactInfo() {
        super();
        setAddress(new Address());
        setPhoneNumber(new PhoneNumber());
    }

    public Address getAddress() {
        return address;
    }

    public String getContactInfo() {
        return getFullAddress();
    }

    public String getContactInfoEmail() {
        return getEmail() != null ? getEmail() : getConfiguration().getContactInfoEmail();
    }

    public String getContactInfoMapUrl() {
        return getMapUrl() != null ? getMapUrl() : getConfiguration().getContactInfoMapUrl();
    }

    public String getContactInfoOfficeHours() {
        return getOfficeHours() != null ? getOfficeHours() : getConfiguration().getContactInfoOfficeHours();
    }

    public String getContactInfoPhone() {
        return getPhone() != null ? getPhone() : getConfiguration().getContactInfoPhone();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getAddress() != null) {
            addEntityInfoItem(summary, "address", getAddress().getFullAddress());
        }
        if (getPhoneNumber() != null) {
            addEntityInfoItem(summary, "phone", getPhoneNumber().getFullNumber());
        }
        addEntityInfoItem(summary, "email", getEmail());
        addEntityInfoItem(summary, "mapUrl", getMapUrl());
        addEntityInfoItem(summary, "officeHours", getOfficeHours());
        return summary.toString();
    }

    public String getFullAddress() {
        return getAddress() != null ? StringHelper.getFullAddress(getAddress().getSupplement(), getAddress().getRoom(), getAddress().getStreet(), getAddress().getZip(), getAddress().getCity(), getAddress().getCountry(), 1) : getConfiguration().getFullContactInfo();
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public String getOfficeHours() {
        return officeHours;
    }

    public String getPhone() {
        return getPhoneNumber() != null ? getPhoneNumber().getFullNumber() : null;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public Set<ServiceArea> getServiceAreas() {
        return serviceAreas;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getServiceAreas().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = StringHelper.format(email);
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = StringHelper.format(mapUrl);
    }

    public void setOfficeHours(String officeHours) {
        this.officeHours = StringHelper.format(officeHours);
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}