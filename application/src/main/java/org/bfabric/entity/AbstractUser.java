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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.Constants;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractUser extends AbstractDescriptionBaseEntity {

    private static final long serialVersionUID = 1;

    @Embedded
    @XmlElement
    private Address address;

    @NotBlank
    @Size(max = 64)
    @Email
    @Pattern(regexp = StringHelper.validEmailRegex)
    @XmlElement
    private String email;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String firstName;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String lastName;

    @Column(updatable = false, insertable = false)
    private String name;

    @Embedded
    @XmlElement
    private PhoneNumber phoneNumber;

    @NotBlank
    @Size(max = 2)
    @XmlElement
    private String salutation;

    @Size(max = 16)
    @XmlElement
    private String title;

    public AbstractUser() {
    }

    public Address getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstLastName() {
        return getFirstName() + " " + getLastName();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullAddress() {
        return getAddress() != null ? getAddress().getFullAddress() : null;
    }

    public String getFullAddressWithLineBreaks() {
        return getAddress() != null ? getAddress().getFullAddressWithLineBreaks() : null;
    }

    public String getFullLastFirstName() {
        return getTitle() != null ? getLastName() + ", " + getFirstName() + " " + getTitle() : getLastName() + ", " + getFirstName();
    }

    public String getFullName() {
        return (getTitle() != null ? getTitle() + " " : Constants.EMPTY_STRING) + getFirstLastName();
    }

    public String getFullNameFormat(int format) {
        if (format == 0) {
            return getFullLastFirstName();
        }
        if (format == 1) {
            return getTitle() != null ? getFirstName() + " " + getLastName() + ", " + getTitle() : getFirstName() + " " + getLastName();
        }
        return getFullName();
    }

    public String getLastName() {
        return lastName;
    }

    public String getLastNameFirstName() {
        return getLastName() + " " + getFirstName();
    }

    public String getName() {
        return name != null ? name : getFullName();
    }

    public String getPhone() {
        return getPhoneNumber() != null ? getPhoneNumber().getFullNumber() : null;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhoneNumberForContactList() {
        String phonePrefix = getConfiguration().getDeployerPhonePrefix();
        return getPhone() != null && phonePrefix != null && getPhone().startsWith(phonePrefix) ? getPhone().substring(phonePrefix.length()) : getPhone();
    }

    public String getSalutation() {
        return salutation;
    }

    public String getTitle() {
        return title;
    }

    public boolean isSalutationFemale() {
        return Constants.MS.equalsIgnoreCase(getSalutation());
    }

    public boolean isSalutationMale() {
        return Constants.MR.equalsIgnoreCase(getSalutation());
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = StringHelper.format(email);
    }

    public void setFirstName(String firstName) {
        this.firstName = StringHelper.format(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = StringHelper.format(lastName);
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSalutation(String salutation) {
        this.salutation = StringHelper.format(salutation);
    }

    public void setTitle(String title) {
        this.title = StringHelper.format(title);
    }
}