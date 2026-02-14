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

import java.io.Serializable;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.Constants;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.CountryService;
import org.bfabric.util.StringHelper;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
public class BillingInfo implements Serializable, Cloneable, NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @Column(updatable = false, insertable = false)
    private String billingAddress;

    @Size(max = 64)
    @NotEmpty
    @XmlElement
    private String billingAddressCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingaddresscountryid")
    @NotNull
    @XmlIDREF
    private Country billingAddressCountry;

    @NotEmpty
    @Size(max = 64)
    @XmlElement
    private String billingAddressStreet;

    @Size(max = 64)
    @XmlElement
    private String billingAddressSupplement;

    @NotEmpty
    @Size(max = 16)
    @XmlElement
    private String billingAddressZip;

    @NotEmpty
    @Size(max = 64)
    @XmlElement
    private String billingCustomerFirstName;

    @NotEmpty
    @Size(max = 64)
    @XmlElement
    private String billingCustomerLastName;

    @Column(updatable = false, insertable = false)
    private String billingCustomerName;

    @Size(max = 64)
    @XmlElement
    private String billingCustomerTitle;

    @NotEmpty
    @Size(max = 64)
    @Email
    @XmlElement
    private String billingEmail;

    @Min(0)
    @Max(999999)
    @XmlElement
    private Long debitorNumber;

    @Transient
    private Long oldDebitorNumber;

    @Transient
    private String oldReferenceNumber;

    @Transient
    private String oldVatNumber;

    @Size(max = 64)
    @XmlElement
    private String referenceNumber;

    @Column(length = 32)
    @Size(max = 32)
    @XmlElement
    private String vatNumber;

    public BillingInfo() {
    }

    public BillingInfo(User user) {
        if (user != null) {
            setBillingAddressStreet(user.getAddress().getStreet());
            setBillingAddressSupplement(user.getAddress().getSupplement());
            setBillingAddressZip(user.getAddress().getZip());
            setBillingAddressCity(user.getAddress().getCity());
            setBillingAddressCountry(user.getAddress().getCountry());
            setBillingCustomerTitle(user.getTitle());
            setBillingCustomerFirstName(user.getFirstName());
            setBillingCustomerLastName(user.getLastName());
        }
    }

    public BillingInfo(UserBillingInfo userBillingInfo) {
        if (userBillingInfo != null) {
            setBillingAddressStreet(userBillingInfo.getAddress().getStreet());
            setBillingAddressSupplement(userBillingInfo.getAddress().getSupplement());
            setBillingAddressZip(userBillingInfo.getAddress().getZip());
            setBillingAddressCity(userBillingInfo.getAddress().getCity());
            setBillingAddressCountry(userBillingInfo.getAddress().getCountry());
            setBillingCustomerTitle(userBillingInfo.getTitle());
            setBillingCustomerFirstName(userBillingInfo.getFirstName());
            setBillingCustomerLastName(userBillingInfo.getLastName());
            setBillingEmail(userBillingInfo.getEmail());
            setVatNumber(userBillingInfo.getVatNumber());
            setReferenceNumber(userBillingInfo.getReferenceNumber());
        }
    }

    public void checkBillingInfo() throws InvalidDataException {
        StringBuilder errorMessage = new StringBuilder();
        if (StringHelper.isEmpty(getBillingAddressCity())) {
            errorMessage.append(" billingaddresscity");
        }
        if (StringHelper.isEmpty(getBillingAddressStreet())) {
            errorMessage.append(" billingaddressstreet");
        }
        if (StringHelper.isEmpty(getBillingAddressZip())) {
            errorMessage.append(" billingaddresszip");
        }
        if (getBillingAddressCountry() == null) {
            errorMessage.append(" billingaddresscountry");
        }
        if (StringHelper.isEmpty(getBillingCustomerFirstName())) {
            errorMessage.append(" billingcustomerfirstname");
        }
        if (StringHelper.isEmpty(getBillingCustomerLastName())) {
            errorMessage.append(" billingcustomerlastname");
        }
        if (StringHelper.isEmpty(getBillingEmail())) {
            errorMessage.append(" billingemail");
        }
        if (!errorMessage.toString().isEmpty()) {
            throw new InvalidDataException("Must not be empty" + errorMessage + "!");
        }
    }

    @Override
    public BillingInfo clone() throws CloneNotSupportedException {
        return (BillingInfo) super.clone();
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public String getBillingAddressCity() {
        return billingAddressCity;
    }

    public Country getBillingAddressCountry() {
        return billingAddressCountry;
    }

    public String getBillingAddressFull() {
        return StringHelper.getFullAddress(getBillingAddressSupplement(), getBillingAddressStreet(), getBillingAddressZip(), getBillingAddressCity(), getBillingAddressCountry());
    }

    public String getBillingAddressStreet() {
        return billingAddressStreet;
    }

    public String getBillingAddressSupplement() {
        return billingAddressSupplement;
    }

    public String getBillingAddressZip() {
        return billingAddressZip;
    }

    public String getBillingAddressZipCity() {
        return StringHelper.getFullAddress(null, getBillingAddressZip(), getBillingAddressCity(), null);
    }

    public String getBillingCustomerFirstName() {
        return billingCustomerFirstName;
    }

    public String getBillingCustomerFullName() {
        return (getBillingCustomerTitle() != null ? getBillingCustomerTitle() + " " : Constants.EMPTY_STRING) + getBillingCustomerFirstName() + " " + getBillingCustomerLastName();
    }

    public String getBillingCustomerLastName() {
        return billingCustomerLastName;
    }

    public String getBillingCustomerName() {
        return StringHelper.isNotEmpty(billingCustomerName) ? billingCustomerName : getBillingCustomerFullName();
    }

    public String getBillingCustomerTitle() {
        return billingCustomerTitle;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public Long getDebitorNumber() {
        return debitorNumber;
    }

    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder();
        StringHelper.addEntityInfoItemIfNotEmpty(summary, "billingCustomer", getBillingCustomerFullName());
        StringHelper.addEntityInfoItemIfNotEmpty(summary, "billingAddress", getBillingAddressFull());
        StringHelper.addEntityInfoItemIfNotEmpty(summary, "billingEmail", getBillingEmail());
        StringHelper.addEntityInfoItemIfNotEmpty(summary, "vatNumber", getVatNumber());
        StringHelper.addEntityInfoItemIfNotEmpty(summary, "referenceNumber", getReferenceNumber());
        StringHelper.addEntityInfoItemIfNotEmpty(summary, "debitorNumber", getDebitorNumber());
        return summary.toString();
    }

    public Long getOldDebitorNumber() {
        return oldDebitorNumber;
    }

    public String getOldReferenceNumber() {
        return oldReferenceNumber;
    }

    public String getOldVatNumber() {
        return oldVatNumber;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public boolean isForeignBillingAddress() {
        return getBillingAddressCountry() != null && !getBillingAddressCountry().getId().isEmpty() && !getBillingAddressCountry().getId().equals("CH");
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = StringHelper.format(billingAddress);
    }

    public void setBillingAddressCity(String billingAddressCity) {
        this.billingAddressCity = StringHelper.format(billingAddressCity);
    }

    public void setBillingAddressCountry(Country billingAddressCountry) {
        this.billingAddressCountry = billingAddressCountry;
    }

    public void setBillingAddressStreet(String billingAddressStreet) {
        this.billingAddressStreet = StringHelper.format(billingAddressStreet);
    }

    public void setBillingAddressSupplement(String billingAddressSupplement) {
        this.billingAddressSupplement = StringHelper.format(billingAddressSupplement);
    }

    public void setBillingAddressZip(String billingAddressZip) {
        this.billingAddressZip = StringHelper.format(billingAddressZip);
    }

    public void setBillingCustomerFirstName(String billingCustomerFirstName) {
        this.billingCustomerFirstName = StringHelper.format(billingCustomerFirstName);
    }

    public void setBillingCustomerLastName(String billingCustomerLastName) {
        this.billingCustomerLastName = StringHelper.format(billingCustomerLastName);
    }

    public void setBillingCustomerName(String billingCustomerName) {
        this.billingCustomerName = StringHelper.format(billingCustomerName);
    }

    public void setBillingCustomerTitle(String billingCustomerTitle) {
        this.billingCustomerTitle = StringHelper.format(billingCustomerTitle);
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = StringHelper.format(billingEmail);
    }

    public void setDebitorNumber(Long debitorNumber) {
        this.debitorNumber = debitorNumber;
    }

    public void setOldDebitorNumber(Long oldDebitorNumber) {
        this.oldDebitorNumber = oldDebitorNumber;
    }

    public void setOldReferenceNumber(String oldReferenceNumber) {
        this.oldReferenceNumber = oldReferenceNumber;
    }

    public void setOldVatNumber(String oldVatNumber) {
        this.oldVatNumber = oldVatNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = StringHelper.format(referenceNumber);
    }

    public void setUzh() {
        setBillingAddressStreet("Hirschengraben 60");
        setBillingAddressSupplement("Zentraler Rechnungseingang");
        setBillingAddressZip("8001");
        setBillingAddressCity("Zürich");
        setBillingAddressCountry(CDI.current().select(CountryService.class).get().findByName(Country.class, "Switzerland"));
    }

    public void setVatAndDebitorNumber(Long debitorNumber, String vatNumber) {
        if (debitorNumber != null) {
            setDebitorNumber(debitorNumber);
        }
        if (vatNumber != null) {
            setVatNumber(vatNumber);
        }
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = StringHelper.format(vatNumber);
    }
}
