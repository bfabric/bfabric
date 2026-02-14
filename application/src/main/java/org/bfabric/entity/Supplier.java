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

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.service.SupplierService;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

@Entity
@XmlRootElement
public class Supplier extends AbstractDescriptionNamedBaseEntity implements ShowScreen, Mergeable {

    private static final long serialVersionUID = 1;

    @Embedded
    @XmlElement
    private AddressOptional address;

    @Size(max = 32)
    @XmlElement
    private String companyId;

    @OneToMany(mappedBy = "supplier")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Consumable> consumables = new HashSet<>();

    @Size(max = 64)
    @Email
    @XmlElement
    private String contactEmail;

    @Size(max = 64)
    @XmlElement
    private String contactFirstName;

    @Size(max = 64)
    @XmlElement
    private String contactLastName;

    @Column(updatable = false, insertable = false)
    private String contactName;

    @Embedded
    @XmlElement
    private ContactPhoneNumber contactPhoneNumber;

    @Size(max = 2)
    @XmlElement
    private String contactSalutation;

    @Size(max = 16)
    @XmlElement
    private String contactTitle;

    @Size(max = 64)
    @Email
    @Pattern(regexp = StringHelper.validEmailRegex)
    @XmlElement
    private String email;

    @Embedded
    @XmlElement
    private PhoneNumberOptional phoneNumber;

    @OneToMany(mappedBy = "supplier")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> purchases = new HashSet<>();

    @OneToMany(mappedBy = "supplier")
    @Where(clause = "internal = false")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> purchasesNonInternal = new HashSet<>();

    @Size(max = 512)
    @XmlElement
    private String url;

    public Supplier() {
        setAddress(new AddressOptional());
        setPhoneNumber(new PhoneNumberOptional());
        setContactPhoneNumber(new ContactPhoneNumber());
    }

    @Override
    public Supplier clone() throws CloneNotSupportedException {
        Supplier clone = (Supplier) super.clone();
        clone.consumables = new HashSet<>();
        clone.purchases = new HashSet<>();
        clone.purchasesNonInternal = new HashSet<>();
        return clone;
    }

    public AddressOptional getAddress() {
        return address;
    }

    public String getCompanyId() {
        return companyId;
    }

    public Set<Consumable> getConsumables() {
        return consumables;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactPhone() {
        return getContactPhoneNumber() != null ? getContactPhoneNumber().getFullNumber() : null;
    }

    public ContactPhoneNumber getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public String getContactSalutation() {
        return contactSalutation;
    }

    public String getContactTitle() {
        return contactTitle;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getCompanyId())) {
            addEntityInfoItem(summary, "companyId", getCompanyId());
        }
        if (getPhoneNumber() != null && !getPhoneNumber().isEmpty()) {
            addEntityInfoItem(summary, "phone", getPhoneNumber().getFullNumber());
        }
        if (StringHelper.isNotEmpty(getEmail())) {
            addEntityInfoItem(summary, "email", getEmail());
        }
        if (StringHelper.isNotEmpty(getFullAddress())) {
            addEntityInfoItem(summary, "address", getFullAddress());
        }
        if (getAddress() != null && !getAddress().isEmpty()) {
            addEntityInfoItem(summary, "room", getAddress().getRoom());
        }
        if (StringHelper.isNotEmpty(getUrl())) {
            addEntityInfoItem(summary, "url", getUrl());
        }
        if (StringHelper.isNotEmpty(getContactName())) {
            addEntityInfoItem(summary, "contactName", getContactName());
        }
        if (StringHelper.isNotEmpty(getContactPhone())) {
            addEntityInfoItem(summary, "contactPhone", getContactPhone());
        }
        if (StringHelper.isNotEmpty(getContactEmail())) {
            addEntityInfoItem(summary, "contactEmail", getContactEmail());
        }
        return summary.toString();
    }

    public String getFullAddress() {
        return getAddress() != null ? getAddress().getFullAddress() : null;
    }

    public String getPhone() {
        return getPhoneNumber() != null ? getPhoneNumber().getFullNumber() : null;
    }

    public PhoneNumberOptional getPhoneNumber() {
        return phoneNumber;
    }

    public Set<Purchase> getPurchases() {
        return purchases;
    }

    public Set<Purchase> getPurchasesByInternal() {
        return hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER) ? getPurchases() : getPurchasesNonInternal();
    }

    public Set<Purchase> getPurchasesNonInternal() {
        return purchasesNonInternal;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER);
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getPurchases().isEmpty() && getConsumables().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public boolean isValidUrl() {
        return UriHelper.isValidUrl(getUrl());
    }

    public void setAddress(AddressOptional address) {
        this.address = address;
    }

    public void setCompanyId(String companyId) {
        this.companyId = StringHelper.format(companyId);
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = StringHelper.format(contactEmail);
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = StringHelper.format(contactFirstName);
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = StringHelper.format(contactLastName);
    }

    public void setContactPhoneNumber(ContactPhoneNumber contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public void setContactSalutation(String contactSalutation) {
        this.contactSalutation = StringHelper.format(contactSalutation);
    }

    public void setContactTitle(String contactTitle) {
        this.contactTitle = StringHelper.format(contactTitle);
    }

    public void setEmail(String email) {
        this.email = StringHelper.format(email);
    }

    public void setPhoneNumber(PhoneNumberOptional phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPurchases(Set<Purchase> purchases) {
        this.purchases = purchases;
    }

    public void setUrl(String url) {
        this.url = StringHelper.format(url);
    }

    public void validateEmail(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        final String email = StringHelper.format((String) value);
        String supplierEmail = (String) uiComponent.getAttributes().get("supplierEmail");
        String contactEmail = (String) uiComponent.getAttributes().get("contactEmail");

        if (email != null && supplierEmail != null && contactEmail == null) {
            if (StringHelper.isInvalidEmailAddress(email)) {
                throw new BfabricValidatorException("emailNotValidException");
            }
            setEmail(email);
        }
        if (email != null && supplierEmail == null && contactEmail != null) {
            if (StringHelper.isInvalidEmailAddress(email)) {
                throw new BfabricValidatorException("emailNotValidException");
            }
            setContactEmail(email);
        }
    }
}
