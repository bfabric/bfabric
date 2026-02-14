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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Offer.findDeletable", query = "SELECT a FROM Offer a WHERE a.created < :creationDate and a.charges is empty and a.referencingOrders is empty")
@NamedQuery(name = "Offer.findExpired", query = "SELECT a FROM Offer a WHERE a.status = org.bfabric.enums.StatusEnum.PENDING and a.created < :creationDate order by id")
public class Offer extends AbstractDescriptionBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "offer", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "charge")
    private List<OfferedCharge> charges = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachid")
    @XmlIDREF
    private User coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachbackupid")
    @XmlIDREF
    private User coachBackup;

    @Transient
    private boolean coachBackupChanged;

    @Transient
    private boolean coachChanged;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<OfferComment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "offercontainer", joinColumns = @JoinColumn(name = "offerid"), inverseJoinColumns = @JoinColumn(name = "containerid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "container")
    private Set<Container> containers = new HashSet<>();

    @DecimalMin("0")
    @DecimalMax("100")
    @Digits(integer = 3, fraction = 2)
    @XmlElement
    private BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @XmlElement
    private Boolean euGrant;

    @Transient
    private StatusEnum oldStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationtypeid")
    @NotNull
    @XmlIDREF
    private OrganizationType organizationType;

    @OneToMany(mappedBy = "offer")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<Order> referencingOrders = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesterid")
    @XmlIDREF
    private User requester;

    @Size(max = 256)
    @XmlElement
    private String requesterAddress;

    @Column(length = 64)
    @Size(max = 64)
    @Email
    @XmlElement
    private String requesterEmail;

    @Size(max = 128)
    @XmlElement
    private String requesterName;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<OfferStatus> states = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private StatusEnum status;

    public Offer() {
        setStatus(StatusEnum.PENDING);
    }

    public void addState() {
        if (getStates().isEmpty() || isStatusChanged()) {
            getStates().add(new OfferStatus(this, getStatus()));
        }
    }

    public void changeStatus(StatusEnum statusEnum) {
        setStatusAndAddState(statusEnum);
    }

    @Override
    public Offer clone() throws CloneNotSupportedException {
        final Offer clone = (Offer) super.clone();
        clone.setContainers(new HashSet<>());
        clone.setCharges(new ArrayList<>());
        for (OfferedCharge offeredCharge : getCharges()) {
            OfferedCharge offeredChargeClone = offeredCharge.clone();
            offeredChargeClone.setCharges(null);
            offeredChargeClone.setOffer(clone);
            clone.getCharges().add(offeredChargeClone);
        }
        clone.states = new ArrayList<>();
        clone.status = StatusEnum.PENDING;
        return clone;
    }

    public void coachBackupChangedListener(ValueChangeEvent event) {
        setCoachBackupChanged(!(getCoachBackup() == null && event.getNewValue() == null || getCoachBackup() != null && getCoachBackup().equals(event.getNewValue())));
    }

    public void coachChangedListener(ValueChangeEvent event) {
        setCoachChanged(!(getCoach() == null && event.getNewValue() == null || getCoach() != null && getCoach().equals(event.getNewValue())));
    }

    public void computeChargesPrice() {
        for (OfferedCharge charge : getCharges()) {
            charge.resetOffer(this);
        }
    }

    public Mail createMail() {
        Mail mail = new Mail();
        mail.setParent(this);
        mail.setType(MailTypeEnum.OFFER);
        mail.setSubject(mail.getSubject() + " " + getId());
        try {
            String exportUrl = getReportPDFUrl("offer-fop");
            if (exportUrl != null) {
                File pdf = exportUrlToPDF(exportUrl, getClassLabelLowerCase() + "_" + getId());
                if (pdf != null) {
                    mail.getAttachments().add(pdf);
                }
            }
            if (getRequesterEmailDisplay() != null) {
                mail.getMailHelper().setTo(new ArrayList<>(Collections.singletonList(new InternetAddress(getRequesterEmailDisplay()))));
                mail.getMailHelper().setBcc(new ArrayList<>(Collections.singletonList(new InternetAddress(getCurrentUser().getEmail()))));
                mail.setReplyToAddress(getCurrentUser().getEmail());
            }
        } catch (AddressException | IOException e) {
            logger.warning("Failed to create mail: " + e.getMessage());
        }
        return mail;
    }

    private File exportUrlToPDF(String urlStr, String prefix) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setInstanceFollowRedirects(false); // handle redirects so we can forward cookies
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(10_000);
        urlConnection.setReadTimeout(30_000);
        urlConnection.setRequestProperty("Accept", "application/pdf");
        // forward current request cookies (JSESSIONID etc.) when available to avoid getting a login HTML page
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                Object req = facesContext.getExternalContext().getRequest();
                if (req instanceof javax.servlet.http.HttpServletRequest) {
                    String cookie = ((javax.servlet.http.HttpServletRequest) req).getHeader("Cookie");
                    if (cookie != null) {
                        urlConnection.setRequestProperty("Cookie", cookie);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        // follow a single redirect and carry Set-Cookie if present
        int code = urlConnection.getResponseCode();
        if (code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_SEE_OTHER) {
            String location = urlConnection.getHeaderField("Location");
            String setCookie = urlConnection.getHeaderField("Set-Cookie");
            urlConnection.disconnect();
            if (location == null) {
                return null;
            }
            url = new URL(location);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10_000);
            urlConnection.setReadTimeout(30_000);
            urlConnection.setRequestProperty("Accept", "application/pdf");
            if (setCookie != null) {
                urlConnection.setRequestProperty("Cookie", setCookie);
            }
            code = urlConnection.getResponseCode();
        }
        String contentType = urlConnection.getContentType();
        if (code != HttpURLConnection.HTTP_OK || contentType == null || !contentType.toLowerCase().contains("pdf")) {
            // for debugging: optionally save returned HTML to a temp file (helps inspect login page)
            try (InputStream in = urlConnection.getInputStream()) {
                File debug = File.createTempFile(prefix + "_resp", ".html");
                try (FileOutputStream out = new FileOutputStream(debug)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                    }
                }
                logger.warning("Export did not return PDF (contentType=" + contentType + ", code=" + code + "). Debug saved to " + debug.getAbsolutePath());
            } catch (IOException e) {
                logger.warning("Export did not return PDF and could not save debug response: " + e.getMessage());
            } finally {
                urlConnection.disconnect();
            }
            return null;
        }
        File temp = File.createTempFile(prefix, ".pdf");
        try (InputStream in = urlConnection.getInputStream(); FileOutputStream out = new FileOutputStream(temp)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        } finally {
            urlConnection.disconnect();
        }
        return temp;
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        if (getOrganizationType() != null && !getOrganizationType().isFinanceSourceRequired()) {
            setEuGrant(null);
        }
    }

    public List<OfferedCharge> getCharges() {
        return charges;
    }

    @XmlElement(name = "charges")
    public int getChargesCount() {
        return getCharges() != null ? getCharges().size() : 0;
    }

    public User getCoach() {
        if (coach == null) {
            coach = getUserByLogin(getCreatedBy());
        }
        return coach;
    }

    public User getCoachBackup() {
        return coachBackup;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.OFFER_COMMENT;
    }

    public Set<OfferComment> getComments() {
        return comments;
    }

    public Set<Container> getContainers() {
        return containers;
    }

    public List<Container> getContainersAsList() {
        return CollectionHelper.asList(getContainers());
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getRequesterName();
    }

    public List<User> getEmployeesIncludingCoach(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getCoach());
    }

    public List<User> getEmployeesIncludingCoachBackup(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getCoachBackup());
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus().getLabel());
        }
        if (StringHelper.isNotEmpty(getRequesterName())) {
            addEntityInfoItem(summary, "requester", getRequesterName());
        }
        if (StringHelper.isNotEmpty(getRequesterAddress())) {
            addEntityInfoItem(summary, "address", getRequesterAddress());
        }
        if (getDiscount() != null) {
            addEntityInfoItem(summary, "discount", getDiscount());
        }
        if (getTotalDiscountedPrice() != null) {
            addEntityInfoItem(summary, "offerDiscount", getTotalDiscountedPrice());
        }
        if (getTotalBookedPrice() != null) {
            addEntityInfoItem(summary, "bookedTotal", getTotalBookedPrice());
        }
        if (getEuGrant() != null) {
            addEntityInfoItem(summary, "euGrant", getEuGrant());
        }
        if (getCoach() != null) {
            addEntityInfoItem(summary, "coach", getCoach().getName());
        }
        if (getCoachBackup() != null) {
            addEntityInfoItem(summary, "coachBackup", getCoachBackup().getName());
        }
        return summary.toString();
    }

    public Boolean getEuGrant() {
        return euGrant;
    }

    public List<StatusEnum> getNextStates() {
        List<StatusEnum> nextStates = new ArrayList<>();
        StatusEnum statusEnum = getStatus();
        switch (statusEnum) {
        case PENDING:
            nextStates.add(StatusEnum.LOCKED);
            nextStates.add(StatusEnum.CANCELED);
            break;
        case LOCKED:
            nextStates.add(StatusEnum.PENDING);
            break;
        case CANCELED:
        case EXPIRED:
        default:
            break;
        }
        return nextStates;
    }

    public StatusEnum getOldStatus() {
        return oldStatus;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public List<Order> getReferencingOrders() {
        return referencingOrders;
    }

    public User getRequester() {
        return requester;
    }

    public String getRequesterAddress() {
        return requesterAddress;
    }

    public String getRequesterAddressComputed() {
        return getRequester() != null ? getRequester().getFullAddressWithLineBreaks() : getRequesterAddress();
    }

    public String getRequesterAddressDisplay() {
        if (getRequesterAddress() != null) {
            return getRequesterAddress();
        }
        return getRequester() != null ? getRequester().getFullAddress() : null;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public String getRequesterEmailComputed() {
        return getRequester() != null ? getRequester().getEmail() : getRequesterEmail();
    }

    public String getRequesterEmailDisplay() {
        if (getRequesterEmail() != null) {
            return getRequesterEmail();
        }
        return getRequester() != null ? getRequester().getEmail() : null;
    }

    public List<String> getRequesterFullDisplayAddressAsList() {
        List<String> strList = new ArrayList<>();
        String str = getRequester() == null ? requesterAddress : getRequester().getFullAddressWithLineBreaks();
        StringTokenizer tokens = new StringTokenizer(str, "\n");
        while (tokens.hasMoreElements()) {
            strList.add((String) tokens.nextElement());
        }
        return strList;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getRequesterNameComputed() {
        return getRequester() != null ? getRequester().getName() : getRequesterName();
    }

    public List<OfferStatus> getStates() {
        return states;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public BigDecimal getTotalBookedPrice() {
        BigDecimal totalBookedPrice = BigDecimal.ZERO;
        for (Container container : getContainers()) {
            for (Booking booking : container.getBookings()) {
                totalBookedPrice = totalBookedPrice.add(booking.getTotalDiscountedPrice());
            }
        }
        return NumberUtils.getDecimalScale2(totalBookedPrice);
    }

    public BigDecimal getTotalDiscount() {
        return getTotalPrice().subtract(getTotalDiscountedPrice());
    }

    public BigDecimal getTotalDiscountedPrice() {
        BigDecimal totalDiscountedPrice = BigDecimal.ZERO;
        for (OfferedCharge charge : getCharges()) {
            totalDiscountedPrice = totalDiscountedPrice.add(charge.getDiscountedPrice());
        }
        return NumberUtils.getDecimalScale2(totalDiscountedPrice);
    }

    public BigDecimal getTotalOfferDiscount() {
        BigDecimal totalOfferDiscount = BigDecimal.ZERO;
        for (OfferedCharge charge : getCharges()) {
            BigDecimal discountedCharge = BigDecimal.valueOf(charge.getPrice().doubleValue() - (charge.getPrice().doubleValue() * charge.getDiscount().doubleValue() / 100.0));
            BigDecimal offerDiscount = BigDecimal.valueOf(discountedCharge.doubleValue() * charge.getOffer().getDiscount().doubleValue() / 100.0);
            totalOfferDiscount = totalOfferDiscount.add(offerDiscount);
        }
        return NumberUtils.getDecimalScale2(totalOfferDiscount);
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OfferedCharge charge : getCharges()) {
            totalPrice = totalPrice.add(charge.getPrice());
        }
        return NumberUtils.getDecimalScale2(totalPrice);
    }

    public BigDecimal getTotalPriceWithoutOfferDiscount() {
        BigDecimal totalPriceWithoutOfferDiscount = BigDecimal.ZERO;
        for (OfferedCharge charge : getCharges()) {
            totalPriceWithoutOfferDiscount = totalPriceWithoutOfferDiscount.add(charge.getPriceWithoutOfferDiscount());
        }
        return NumberUtils.getDecimalScale2(totalPriceWithoutOfferDiscount);
    }

    public BigDecimal getTotalTax() {
        double totalTax = 0;
        for (OfferedCharge charge : getCharges()) {
            totalTax += charge.getTaxType().getTax().doubleValue() / 100.0 * charge.getDiscountedPrice().doubleValue();
        }
        return NumberUtils.getDecimalScale2(BigDecimal.valueOf(totalTax));
    }

    public boolean hasBasicPricedServicesOnly() {
        for (OfferedCharge charge : getCharges()) {
            if (charge.getAdditionalPrice().doubleValue() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean hasChargeWithServiceCodeOnly() {
        for (OfferedCharge charge : getCharges()) {
            if (StringHelper.isEmpty(charge.getServiceCodeName())) {
                return false;
            }
        }
        return true;
    }

    public boolean hasCharges() {
        return getCharges() != null && !getCharges().isEmpty();
    }

    public boolean hasDiscountedCharge() {
        for (OfferedCharge charge : getCharges()) {
            if (charge.isDiscounted()) {
                return true;
            }
        }
        return false;
    }

    public boolean isChargesCopyable() {
        return isCreatable() && hasCharges();
    }

    public boolean isCoach(User user) {
        return getCoach() != null && getCoach().equals(user);
    }

    public boolean isCoachBackup(User user) {
        return getCoachBackup() != null && getCoachBackup().equals(user);
    }

    public boolean isCoachBackupChanged() {
        return coachBackupChanged;
    }

    public boolean isCoachBackupValid() {
        return isInFinalState() || getCoachBackup() != null && getCoachBackup().hasRoleImplicit(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isCoachChanged() {
        return coachChanged;
    }

    public boolean isCoachValid() {
        return isInFinalState() || getCoach() != null && getCoach().hasRoleImplicit(RoleEnum.CONTAINERMANAGER);
    }

    @Override
    public boolean isDeletable() {
        if (hasCurrentUserRoleEnum(getDefaultRequiredRole()) && getReferencingOrders().isEmpty()) {
            if (hasCharges()) {
                for (OfferedCharge charge : getCharges()) {
                    if (!charge.isDeletable()) {
                        return false;
                    }
                }
                return isUpdatable();
            }
            return true;
        }
        return false;
    }

    public boolean isDeletableAndExpired() {
        return isDeletable() && isExpired();
    }

    public boolean isDiscounted() {
        return getDiscount().doubleValue() > 0;
    }

    public boolean isExpired() {
        return StatusEnum.EXPIRED.equals(getStatus());
    }

    public boolean isExportable() {
        return hasCharges();
    }

    public boolean isIdentityCoach() {
        return isCoach(getCurrentUser());
    }

    public boolean isIdentityCoachBackup() {
        return isCoachBackup(getCurrentUser());
    }

    public boolean isIdentityCoachOrCoachBackup() {
        return isIdentityCoach() || isIdentityCoachBackup();
    }

    public boolean isInFinalState() {
        return isLocked() || isExpired();
    }

    public boolean isLocked() {
        return StatusEnum.LOCKED.equals(getStatus());
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || !CollectionUtils.intersection(getCurrentUser().getContainersTransitive(), getContainers()).isEmpty() || isRequesterIdentity();
    }

    public boolean isRegisteredRequester() {
        return getRequester() != null;
    }

    @Override
    public boolean isRenderedAddCommentButton() {
        return isReadable() && !isExpired();
    }

    public boolean isRenderedFinancedByEuGrant() {
        return isManaged() && getEuGrant() != null && getEuGrant() && getOrganizationType() != null && getOrganizationType().isFinanceSourceRequired();
    }

    public boolean isRenderedOfferCoach() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || getCurrentUser().equals(getCoach());
    }

    public boolean isRenderedOfferCoachBackup() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || getCurrentUser().equals(getCoach());
    }

    public boolean isRequesterIdentity() {
        return getRequester() != null && getCurrentUsername() != null && getCurrentUsername().equals(getRequester().getLogin());
    }

    public boolean isRollbackable() {
        return getStates().size() > 1;
    }

    public boolean isSendButtonRendered() {
        return !getCharges().isEmpty() && !isExpired();
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable() && !(isLocked() || isExpired());
    }

    public void organizationTypeChanged(ValueChangeEvent event) {
        setOrganizationType((OrganizationType) event.getNewValue());
        computeChargesPrice();
    }

    public void requesterChanged(ValueChangeEvent event) {
        setRequester((User) event.getNewValue());
        if (getRequester() != null) {
            setOrganizationType(getRequester().getOrganizationTypeForBilling());
        }
    }

    public void rollbackStatus() {
        if (isRollbackable()) {
            getStates().remove(getStates().size() - 1);
            setStatus(getStates().get(getStates().size() - 1).getStatusEnum());
        }
    }

    public void setCharges(List<OfferedCharge> charges) {
        this.charges = charges;
    }

    public void setCoach(User coach) {
        this.coach = coach;
    }

    public void setCoachBackup(User coachBackup) {
        this.coachBackup = coachBackup;
    }

    public void setCoachBackupChanged(boolean coachBackupChanged) {
        this.coachBackupChanged = coachBackupChanged;
    }

    public void setCoachChanged(boolean coachChanged) {
        this.coachChanged = coachChanged;
    }

    public void setContainers(Collection<Container> containers) {
        this.containers = (Set<Container>) CollectionHelper.asSet(containers);
    }

    public void setContainersAsList(List<Container> containers) {
        this.containers = (Set<Container>) CollectionHelper.asSet(containers);
    }

    public void setDiscount(BigDecimal discount) {
        if (this.discount == null || !this.discount.equals(discount)) {
            this.discount = discount;
            computeChargesPrice();
        }
    }

    public void setEuGrant(Boolean euGrant) {
        this.euGrant = euGrant;
    }

    public void setOldStatus(StatusEnum oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setOrganizationType(OrganizationType organizationType) {
        if (this.organizationType != organizationType) {
            this.organizationType = organizationType;
            if (organizationType != null) {
                computeChargesPrice();
            }
        }
    }

    public void setRequester(User requester) {
        this.requester = requester;
        setRequesterName();
        setRequesterAddress();
        setRequesterEmail();
    }

    public void setRequesterAddress(String requesterAddress) {
        this.requesterAddress = StringHelper.formatText(requesterAddress);
    }

    public void setRequesterAddress() {
        setRequesterAddress(getRequesterAddressComputed());
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = StringHelper.format(requesterEmail);
    }

    public void setRequesterEmail() {
        setRequesterEmail(getRequesterEmailComputed());
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = StringHelper.format(requesterName);
    }

    public void setRequesterName() {
        setRequesterName(getRequesterNameComputed());
    }

    public void setStates(List<OfferStatus> states) {
        this.states = states;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setStatusAndAddState(StatusEnum status) {
        setOldStatus(getStatus());
        setStatus(status);
        addState();
    }
}