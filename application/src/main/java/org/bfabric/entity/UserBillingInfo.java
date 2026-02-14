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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@XmlRootElement
public class UserBillingInfo extends AbstractBaseEntity implements HasAffiliation {

    private static final long serialVersionUID = 1;

    @Embedded
    @XmlElement
    private Address address;

    @Transient
    private Company company;

    @Transient
    private String companyName;

    @Size(max = 8)
    @NotEmpty
    @XmlElement
    private String costCentre;

    @Transient
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisionid")
    @XmlIDREF
    private Division division;

    @Transient
    private String divisionName;

    @NotEmpty
    @Size(max = 64)
    @Email
    @Pattern(regexp = StringHelper.validEmailRegex)
    @XmlElement
    private String email;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private Boolean euGrant;

    @XmlElement
    @NotNull
    private Boolean financeSourceEth;

    @NotEmpty
    @Size(max = 64)
    @XmlElement
    private String firstName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituteid")
    @XmlIDREF
    private Institute institute;

    @NotEmpty
    @Size(max = 64)
    @XmlElement
    private String lastName;

    @Column(updatable = false, insertable = false)
    private String name;

    @Transient
    private String oldReferenceNumber;

    @Transient
    private String oldVatNumber;

    @Transient
    private Organization organization;

    @Transient
    private OrganizationType organizationType;

    @Size(max = 16)
    @NotEmpty
    @XmlElement
    private String pspElement;

    @Size(max = 64)
    @XmlElement
    private String referenceNumber;

    @Transient
    private Boolean renderedCostCentre;

    @NotBlank
    @Size(max = 2)
    @XmlElement
    private String salutation;

    @Size(max = 64)
    @XmlElement
    private String title;

    @OneToOne(mappedBy = "userBillingInfo")
    @XmlIDREF
    private User user;

    @Column(length = 32)
    @Size(max = 32)
    @XmlElement
    private String vatNumber;

    public UserBillingInfo() {
        setAddress(new Address());
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public UserBillingInfo(UserBillingInfo userBillingInfo) {
        copy(userBillingInfo);
    }

    @Override
    public void clearButtonOrganization() {
        HasAffiliation.super.clearButtonOrganization();
        setVatNumber(null);
        setReferenceNumber(null);
    }

    public void copy(UserBillingInfo userBillingInfo) {
        if (userBillingInfo != null) {
            setAddress(userBillingInfo.getAddress());
            setSalutation(userBillingInfo.getSalutation());
            setTitle(userBillingInfo.getTitle());
            setFirstName(userBillingInfo.getFirstName());
            setLastName(userBillingInfo.getLastName());
            setEmail(userBillingInfo.getEmail());
            setCostCentre(userBillingInfo.getCostCentre());
            setDivision(userBillingInfo.getDivision());
            setInstitute(userBillingInfo.getInstitute());
            setPspElement(userBillingInfo.getPspElement());
            setFinanceSourceEth(userBillingInfo.getFinanceSourceEth());
            setEuGrant(userBillingInfo.getEuGrant());
            setReferenceNumber(userBillingInfo.getReferenceNumber());
            setVatNumber(userBillingInfo.getVatNumber());
        }
    }

    public void copy(User user) {
        if (user != null) {
            setAddress(user.getAddress());
            setSalutation(user.getSalutation());
            setTitle(user.getTitle());
            setFirstName(user.getFirstName());
            setLastName(user.getLastName());
            setDivision(user.getDivision());
            setInstitute(user.getInstitute());
            setOrganizationType(user.getOrganizationType());
            setOrganization(user.getOrganization());
            setCompanyName(user.getCompanyName());
            setDepartment(user.getDepartment());
        }
    }

    @Override
    public void departmentChanged(ValueChangeEvent event) {
        Organization oldOrganization = getOrganization();
        HasAffiliation.super.departmentChanged(event);
        if (oldOrganization == null || !oldOrganization.equals(getOrganization())) {
            resetVatAndReferenceNumber();
            setVatNumberOfOrganization(getOrganization());
        }
    }

    public String getAccountDisplay() {
        if (isRenderedFinanceSource()) {
            if (getCostCentre() != null) {
                return getCostCentreDisplay();
            } else if (getPspElement() != null) {
                return getPspElementDisplay();
            }
        }
        return null;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public Company getCompany() {
        return company;
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    public String getCostCentre() {
        return costCentre;
    }

    public String getCostCentreDisplay() {
        return getFinanceSourceEthDisplay() + " " + Messages.get("costCentre") + " " + getCostCentre();
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    @Override
    public Division getDivision() {
        return division;
    }

    @Override
    public String getDivisionName() {
        return divisionName;
    }

    public String getEmail() {
        return email;
    }

    public String getEntityAsText() {
        StringBuilder summary = new StringBuilder();
        summary.append(this).append(": ");
        summary.append(getFullName()).append(", ").append(getAddress().getFullAddress()).append(", ").append(getEmail());
        if (getInstitute() != null) {
            summary.append(", ").append(getInstitute().getAffiliation());
        }
        if (getDivision() != null) {
            summary.append(", ").append(getDivision().getAffiliation());
        }
        if (getAccountDisplay() != null) {
            summary.append(", ").append(getAccountDisplay());
        }
        if (getReferenceNumber() != null) {
            summary.append(", ").append(getReferenceNumber());
        }
        if (getVatNumber() != null) {
            summary.append(", ").append(getVatNumber());
        }
        return summary.toString();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder();
        addEntityInfoItem(summary, "name", getFullName());
        addEntityInfoItem(summary, "address", getAddress().getFullAddress());
        addEntityInfoItem(summary, "email", getEmail());
        if (getInstitute() != null) {
            addEntityInfoItem(summary, "affiliation", getInstitute().getAffiliation());
        }
        if (getDivision() != null) {
            addEntityInfoItem(summary, "affiliation", getDivision().getAffiliation());
        }
        if (getAccountDisplay() != null) {
            addEntityInfoItem(summary, "account", getAccountDisplay());
        }
        if (getReferenceNumber() != null) {
            addEntityInfoItem(summary, "referenceNumber", getReferenceNumber());
        }
        if (getVatNumber() != null) {
            addEntityInfoItem(summary, "vatNumber", getVatNumber());
        }
        if (getEuGrant() != null) {
            addEntityInfoItem(summary, "getEuGrant", getEuGrant());
        }
        return summary.toString();
    }

    public Boolean getEuGrant() {
        return euGrant;
    }

    public Boolean getFinanceSourceEth() {
        return financeSourceEth;
    }

    public String getFinanceSourceEthDisplay() {
        StringBuilder ret = new StringBuilder();
        if (getFinanceSourceEth() != null) {
            if (getFinanceSourceEth()) {
                ret.append("ETHZ ");
            } else {
                ret.append("UZH ");
            }
        }
        return ret.toString();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullName() {
        return (getSalutation() != null ? getSalutation() + " " : Constants.EMPTY_STRING) + (getTitle() != null ? getTitle() + " " : Constants.EMPTY_STRING) + getFirstName() + " " + getLastName();
    }

    @Override
    public Institute getInstitute() {
        return institute;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return name;
    }

    public String getOldReferenceNumber() {
        return oldReferenceNumber;
    }

    public String getOldVatNumber() {
        return oldVatNumber;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public OrganizationType getOrganizationTypeForBilling() {
        if (getInstitute() != null && getInstitute().getOrganizationTypeForBilling() != null) {
            return getInstitute().getOrganizationTypeForBilling();
        }
        return getDivision() != null ? getDivision().getOrganizationTypeForBilling() : null;
    }

    public String getPspElement() {
        return pspElement;
    }

    public String getPspElementDisplay() {
        return getFinanceSourceEthDisplay() + " " + Messages.get("pspElement") + " " + getPspElement();
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public Boolean getRenderedCostCentre() {
        Boolean ret = renderedCostCentre;
        if (StringHelper.isNotEmpty(getCostCentre())) {
            ret = Boolean.TRUE;
        } else if (StringHelper.isNotEmpty(getPspElement())) {
            ret = Boolean.FALSE;
        }
        return ret;
    }

    public String getSalutation() {
        return salutation;
    }

    public String getTitle() {
        return title;
    }

    public User getUser() {
        return user;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    @Override
    public void instituteChanged(ValueChangeEvent event) {
        Organization oldOrganization = getOrganization();
        HasAffiliation.super.instituteChanged(event);
        if (oldOrganization == null || !oldOrganization.equals(getOrganization())) {
            resetVatAndReferenceNumber();
            setVatNumberOfOrganization(getOrganization());
        }
    }

    public boolean isEmailOrganizationNotMatching() {
        return getInstitute() != null ? getInstitute().isEmailOrganizationNotMatching(getEmail()) : new Company().isNotMatchingEmail(getEmail());
    }

    public boolean isEmpty() {
        return StringHelper.isEmpty(getFirstName()) && StringHelper.isEmpty(getLastName()) && StringHelper.isEmpty(getSalutation()) && StringHelper.isEmpty(getTitle()) && StringHelper
            .isEmpty(getCompanyName()) && StringHelper.isEmpty(getCostCentre()) && StringHelper.isEmpty(getDivisionName()) && StringHelper.isEmpty(getEmail()) && StringHelper
            .isEmpty(getPspElement()) && StringHelper.isEmpty(getReferenceNumber()) && getDivision() == null && getInstitute() == null && (getAddress() == null || getAddress().isEmpty());
    }

    public boolean isRenderedFinanceSource() {
        return getOrganizationType() != null && getOrganizationType().isFinanceSourceRequired();
    }

    public boolean isRenderedFinanceSourceEth() {
        return getOrganization() != null && !(getOrganization().isEth() || getOrganization().isUzh());
    }

    public boolean isRenderedFinancedByEuGrant() {
        return false;
    }

    @Override
    public void organizationChanged(ValueChangeEvent event) {
        HasAffiliation.super.organizationChanged(event);
        resetVatAndReferenceNumber();
        setVatNumberOfOrganization((Organization) event.getNewValue());
    }

    @Override
    public void organizationTypeChanged(ValueChangeEvent event) {
        HasAffiliation.super.organizationTypeChanged(event);
        resetVatAndReferenceNumber();
    }

    public void resetFinanceSource() {
        setCostCentre(null);
        setPspElement(null);
    }

    public void resetFinanceSourceEth() {
        if (getInstitute() != null) {
            resetFinanceSource();
            if (getInstitute().getDepartment().getOrganization().isEth()) {
                setFinanceSourceEth(Boolean.TRUE);
            } else if (getInstitute().getDepartment().getOrganization().isUzh()) {
                setFinanceSourceEth(Boolean.FALSE);
            } else {
                setFinanceSourceEth(null);
            }
        }
    }

    private void resetVatAndReferenceNumber() {
        setVatNumber(null);
        setReferenceNumber(null);
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public void setCompanyName(String companyName) {
        this.companyName = StringHelper.format(companyName);
    }

    public void setCostCentre(String costCentre) {
        this.costCentre = StringHelper.format(costCentre);
    }

    @Override
    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public void setDivision(Division division) {
        this.division = division;
    }

    @Override
    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public void setEmail(String email) {
        this.email = StringHelper.format(email);
    }

    public void setEuGrant(Boolean euGrant) {
        this.euGrant = euGrant;
    }

    public void setFinanceSourceEth(Boolean financeSourceEth) {
        this.financeSourceEth = financeSourceEth;
    }

    public void setFirstName(String firstName) {
        this.firstName = StringHelper.format(firstName);
    }

    @Override
    public void setInstitute(Institute institute) {
        this.institute = institute;
        resetFinanceSourceEth();
    }

    public void setLastName(String lastName) {
        this.lastName = StringHelper.format(lastName);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOldReferenceNumber(String oldReferenceNumber) {
        this.oldReferenceNumber = oldReferenceNumber;
    }

    public void setOldVatNumber(String oldVatNumber) {
        this.oldVatNumber = oldVatNumber;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public void setPspElement(String pspElement) {
        this.pspElement = StringHelper.format(pspElement);
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public void setRenderedCostCentre(Boolean renderedCostCentre) {
        this.renderedCostCentre = renderedCostCentre;
    }

    public void setSalutation(String salutation) {
        this.salutation = StringHelper.format(salutation);
    }

    public void setTitle(String title) {
        this.title = StringHelper.format(title);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    private void setVatNumberOfOrganization(Organization organization) {
        if (organization != null && organization.getVatNumber() != null) {
            setVatNumber(organization.getVatNumber());
        }
    }

    @Override
    public void validateEmail(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        final String email = StringHelper.format((String) value);
        if (StringHelper.isInvalidEmailAddress(email)) {
            throw new BfabricValidatorException("emailNotValidException");
        }
        setEmail(email);
        if (isEmailOrganizationNotMatching()) {
            throw new BfabricValidatorException("emailNotMatchOrganizationException");
        }
    }
}
