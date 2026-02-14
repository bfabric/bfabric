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

import java.time.LocalDate;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.enums.AccessRequestStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.util.DateUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@XmlRootElement
public class AccessRequest extends AbstractAccessCardProfile {

    private static final long serialVersionUID = 1;

    @XmlElement
    private LocalDate accessCardValidityEndDate;

    @XmlElement
    private LocalDate accessCardValidityStartDate;

    @Size(max = 32)
    @XmlElement
    private String accessGranted;

    @Size(max = 32)
    @XmlElement
    private String accessProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessrequestprofileid")
    @XmlIDREF
    private AccessRequestProfile accessRequestProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessRequestTypeid")
    @XmlIDREF
    private AccessRequestType accessRequestType;

    @Size(max = 32)
    @XmlElement
    private String accessRevoked;

    @NotBlank
    @Size(max = 512)
    @XmlElement
    private String affiliation;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private AccessRequestStatusEnum status = AccessRequestStatusEnum.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    @XmlIDREF
    private User user;

    public AccessRequest() {
        super();
    }

    public AccessRequest(User user) {
        setUser(user);
        setAffiliation(user.getFullAffiliation());
        setBirthDate(user.getBirthDate());
        setSalutation(user.getSalutation());
        setTitle(user.getTitle());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setEmail(user.getEmail());
        setAddress(new Address(user.getHomeAddress()));
        setPhoneNumber(new PhoneNumber(user.getHomePhoneNumber()));
        setAccessCardCode(user.getAccessCardCode());
        setAccessCardExpiryDate(user.getAccessCardExpiryDate());
        setAccessCardNumber(user.getAccessCardNumber());
        setStatus(AccessRequestStatusEnum.PENDING);
    }

    public void accessRequestProfileChanged(ValueChangeEvent event) {
        AccessRequestProfile newAccessRequestProfile = (AccessRequestProfile) event.getNewValue();
        if (newAccessRequestProfile != null) {
            setAccessProfile(newAccessRequestProfile.getName());
            setAccessGranted(newAccessRequestProfile.getAccessGranted());
        }
    }

    public void approve() {
        setStatus(AccessRequestStatusEnum.APPROVED);
    }

    public void cancel() {
        setStatus(AccessRequestStatusEnum.CANCELED);
    }

    public LocalDate getAccessCardValidityEndDate() {
        return accessCardValidityEndDate;
    }

    public LocalDate getAccessCardValidityStartDate() {
        return accessCardValidityStartDate;
    }

    public String getAccessGranted() {
        return accessGranted;
    }

    public String getAccessProfile() {
        return accessProfile;
    }

    public AccessRequestProfile getAccessRequestProfile() {
        return accessRequestProfile;
    }

    public AccessRequestType getAccessRequestType() {
        return accessRequestType;
    }

    public String getAccessRevoked() {
        return accessRevoked;
    }

    public String getAffiliation() {
        return affiliation;
    }

    @Override
    @NotNull
    @PastOrPresent
    @XmlElement
    public LocalDate getBirthDate() {
        return super.getBirthDate();
    }

    public String getComment() {
        // IMPORTANT: Do not delete this method! It is necessary by the mail template engine which cannot handle null values!
        return StringHelper.isNotEmpty(getDescription()) ? getDescription() : Constants.EMPTY_STRING;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ACCESSREQUESTMANAGER;
    }

    @Override
    @Size(max = 40)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getAccessRequestType() != null) {
            addEntityInfoItem(summary, "accessRequestType", getAccessRequestType().getName());
        }
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus());
        }
        if (getUser() != null) {
            addEntityInfoItem(summary, "user", getUser().getFullName());
        }
        if (StringHelper.isNotEmpty(getFullAddress())) {
            addEntityInfoItem(summary, "address", getFullAddress());
        }
        if (getAccessCardExpiryDate() != null) {
            addEntityInfoItem(summary, "accessCardExpiryDate", getAccessCardExpiryDate());
        }
        if (StringHelper.isNotEmpty(getAccessCardCode())) {
            addEntityInfoItem(summary, "accessCardCode", getAccessCardCode());
        }
        if (getAccessCardNumber() != null) {
            addEntityInfoItem(summary, "accessCardNumber", getAccessCardNumber());
        }
        if (StringHelper.isNotEmpty(getAccessProfile())) {
            addEntityInfoItem(summary, "accessProfile", getAccessProfile());
        }
        if (StringHelper.isNotEmpty(getAccessGranted())) {
            addEntityInfoItem(summary, "accessGranted", getAccessGranted());
        }
        if (StringHelper.isNotEmpty(getAccessRevoked())) {
            addEntityInfoItem(summary, "accessRevoked", getAccessRevoked());
        }
        return summary.toString();
    }

    public AccessRequestStatusEnum getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public String getUserFullContactDetails() {
        StringBuilder fullContactDetails = new StringBuilder();

        fullContactDetails.append(getFullNameFormat(1)).append(", ");
        if (getBirthDate() != null) {
            fullContactDetails.append(DateUtils.getDateAsFormattedString(getBirthDate())).append(", ");
        }
        if (getAccessCardNumber() != null) {
            fullContactDetails.append(getAccessCardNumber()).append(", ");
        }
        if (getAccessCardCode() != null && !getAccessCardCode().trim().isEmpty()) {
            fullContactDetails.append(getAccessCardCode()).append(", ");
        }
        if (getAccessCardExpiryDate() != null) {
            fullContactDetails.append(DateUtils.getDateAsFormattedString(getAccessCardExpiryDate())).append(", ");
        }
        fullContactDetails.append(getAffiliation());

        return fullContactDetails.toString();
    }

    public boolean isApproved() {
        return AccessRequestStatusEnum.APPROVED.equals(getStatus());
    }

    public boolean isComplete() {
        switch (getAccessRequestType().getName()) {
        case "Guest Card Application":
            return getAccessCardValidityStartDate() != null && getAccessCardValidityEndDate() != null;
        case "Personal Card Access":
            return getAccessProfile() != null;
        case "Guest Card Extension":
            return getAccessCardExpiryDate() != null;
        default:
            return false;
        }
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isGuestAccessCardApplication() {
        return getAccessRequestType() != null && getAccessRequestType().getName().equals("Guest Card Application");
    }

    public boolean isGuestAccessCardExtension() {
        return getAccessRequestType() != null && getAccessRequestType().getName().equals("Guest Card Extension");
    }

    public boolean isGuestCardApplicationPdfRendered() {
        return hasCurrentUserRoleEnum(RoleEnum.ACCESSREQUESTMANAGER) && isComplete() && (isPending() || isApproved()) && isGuestAccessCardApplication();
    }

    public boolean isGuestCardExtensionPdfRendered() {
        return hasCurrentUserRoleEnum(RoleEnum.ACCESSREQUESTMANAGER) && isComplete() && isApproved() && isGuestAccessCardExtension();
    }

    public boolean isPending() {
        return AccessRequestStatusEnum.PENDING.equals(getStatus());
    }

    public boolean isPersonalAccessCardAccess() {
        return getAccessRequestType() != null && getAccessRequestType().getName().equals("Personal Card Access");
    }

    public boolean isProcessCardFieldsRendered() {
        return isPersonalAccessCardAccess() || isGuestAccessCardApplication() && isComplete();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || getUser().isIdentityUser();
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isPending();
    }

    public void reject() {
        setStatus(AccessRequestStatusEnum.REJECTED);
    }

    @Override
    public void setAccessCardCode(String accessCardCode) {
        super.setAccessCardCode(accessCardCode);
        setTypeByAccessCardCode(accessCardCode);
    }

    public void setAccessCardValidityEndDate(LocalDate accessCardValidityEndDate) {
        this.accessCardValidityEndDate = accessCardValidityEndDate;
    }

    public void setAccessCardValidityStartDate(LocalDate accessCardValidityStartDate) {
        this.accessCardValidityStartDate = accessCardValidityStartDate;
    }

    public void setAccessGranted(String accessGranted) {
        this.accessGranted = StringHelper.format(accessGranted);
    }

    public void setAccessProfile(String accessProfile) {
        this.accessProfile = StringHelper.format(accessProfile);
    }

    public void setAccessRequestProfile(AccessRequestProfile accessRequestProfile) {
        this.accessRequestProfile = accessRequestProfile;
    }

    public void setAccessRequestType(AccessRequestType accessRequestType) {
        this.accessRequestType = accessRequestType;
    }

    public void setAccessRevoked(String accessRevoked) {
        this.accessRevoked = StringHelper.format(accessRevoked);
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = StringHelper.format(affiliation);
    }

    public void setDecision(boolean approved) {
        if (approved) {
            approve();
        } else {
            if (getUser().equals(getCurrentUser())) {
                cancel();
            } else {
                reject();
            }
        }
    }

    public void setStatus(AccessRequestStatusEnum status) {
        this.status = status;
    }

    public void setTypeByAccessCardCode(String accessCardCode) {
        setAccessRequestType(getIdentityService().findByName(AccessRequestType.class, AccessRequestType.getAccessRequestTypeName(accessCardCode)));
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void validateAccessCardExpiryDate(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value != null) {
            LocalDate accessCardExpiryDate = (LocalDate) value;
            LocalDate maxExpiryDate = LocalDate.now();
            if (getUser().getAccessCardExpiryDate() != null && getUser().getAccessCardExpiryDate().isAfter(LocalDate.now())) {
                maxExpiryDate = getUser().getAccessCardExpiryDate();
            }
            maxExpiryDate = maxExpiryDate.plusYears(1).plusDays(1);

            if (!(accessCardExpiryDate.isAfter(LocalDate.now()) && accessCardExpiryDate.isBefore(maxExpiryDate))) {
                throw new BfabricValidatorException("accessCardExpiryDateValidException");
            }
        }
    }

    public boolean validateAccessCardValidityEndDate(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        return DateUtils.validateDateRange(getAccessCardValidityStartDate(), (LocalDate) value);
    }
}