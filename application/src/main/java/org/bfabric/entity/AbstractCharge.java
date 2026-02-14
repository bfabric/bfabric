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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.service.UserService;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractCharge extends AbstractDescriptionBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @DecimalMin("0")
    @XmlElement
    protected BigDecimal notAccounted = BigDecimal.ZERO;

    @Column(updatable = false, insertable = false)
    private BigDecimal accounted;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal additionalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal basicPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean billable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chargerid")
    @NotNull
    @XmlIDREF
    private User charger;

    @XmlElement
    private LocalDate date;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Digits(integer = 3, fraction = 2)
    @XmlElement
    private BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal discountedPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Size(max = 512)
    @XmlElement
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationtypeid")
    @XmlIDREF
    private OrganizationType organizationType;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean prePayment = false;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceid")
    @NotNull
    private Service service;

    @Size(max = 256)
    @XmlElement
    private String serviceAreaName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceCodeId")
    private ServiceCode serviceCode;

    @Size(max = 16)
    @XmlElement
    private String serviceCodeName;

    @Size(max = 256)
    @XmlElement
    private String serviceName;

    @Size(max = 256)
    @XmlElement
    private String serviceTypeName;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 3, fraction = 2)
    @XmlElement
    private BigDecimal taxRate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxtypeid")
    @NotNull
    @XmlIDREF
    private TaxType taxType;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public AbstractCharge() {
        super();
    }

    public BigDecimal getAccounted() {
        return accounted;
    }

    public BigDecimal getAccountedComputed() {
        return getTotal() != null && getNotAccounted() != null ? getTotal().subtract(getNotAccounted()) : getTotal();
    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public BigDecimal getBasicPrice() {
        return basicPrice;
    }

    public User getCharger() {
        return charger;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    @Size(max = 512)
    public String getDescription() {
        return super.getDescription();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getDiscountedPrice() {
        return discountedPrice;
    }

    public List<User> getEmployeesIncludingCharger(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getCharger());
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "total", getTotal());
        addEntityInfoItem(summary, "notAccounted", getNotAccounted());
        addEntityInfoItem(summary, "price", getPrice());
        addEntityInfoItem(summary, "discountedPrice", getDiscountedPrice());
        addEntityInfoItem(summary, "tax", getTax());
        addEntityInfoItem(summary, "taxRate", getTaxRate());
        if (StringHelper.isNotEmpty(getServiceName())) {
            addEntityInfoItem(summary, "service", getServiceName());
        }
        if (StringHelper.isNotEmpty(getServiceCodeName())) {
            addEntityInfoItem(summary, "code", getServiceCodeName());
        }
        return summary.toString();
    }

    public String getName() {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(getId()).append(" - ").append(getServiceName());
        if (StringHelper.isNotEmpty(getServiceCodeName())) {
            nameBuilder.append(", ").append(getServiceCodeName());
        }
        nameBuilder.append(", Accounted ").append(getAccounted()).append(", Price ").append(getDiscountedPrice()).append(", ").append(getTaxType().getName());
        return nameBuilder.toString();
    }

    public BigDecimal getNotAccounted() {
        return notAccounted;
    }

    public String getNotes() {
        return notes;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Service getService() {
        return service;
    }

    public BigDecimal getServiceAdditionalPrice() {
        return NumberUtils.getDecimalScale2(getService() != null && getOrganizationType() != null ? getService().getServiceOrganizationTypePrices(getOrganizationType())
            .getAdditionalPrice() : BigDecimal.ZERO);
    }

    public String getServiceAreaName() {
        return serviceAreaName;
    }

    public BigDecimal getServiceBasicPrice() {
        return NumberUtils.getDecimalScale2(getService() != null && getOrganizationType() != null ? getService().getServiceOrganizationTypePrices(getOrganizationType())
            .getBasicPrice() : BigDecimal.ZERO);
    }

    public ServiceCode getServiceCode() {
        return serviceCode;
    }

    public String getServiceCodeName() {
        return serviceCodeName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceTypeName() {
        return serviceTypeName;
    }

    public BigDecimal getTax() {
        return BigDecimal.valueOf(getDiscountedPrice().doubleValue() * getTaxRate().doubleValue() / 100);
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public BigDecimal getTaxedPrice() {
        return getDiscountedPrice().add(getTax());
    }

    public BigDecimal getTotal() {
        return total;
    }

    public boolean isBillable() {
        return billable;
    }

    public boolean isDiscounted() {
        return getDiscount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isOrganizationType(OrganizationType type) {
        return type != null && getOrganizationType() != null && type.getId() == getOrganizationType().getId();
    }

    public boolean isPrePayment() {
        return prePayment;
    }

    public abstract boolean isServiceCodeChanged();

    public abstract boolean isServiceNameChanged();

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = NumberUtils.getDecimalScale2(additionalPrice);
    }

    public void setBasicPrice(BigDecimal basicPrice) {
        this.basicPrice = NumberUtils.getDecimalScale2(basicPrice);
    }

    public void setBillable(boolean billable) {
        this.billable = billable;
    }

    public void setCharger(User charger) {
        this.charger = charger;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = NumberUtils.getDecimalScale2(discount);
    }

    public void setDiscountedPrice(BigDecimal discountedPrice) {
        this.discountedPrice = NumberUtils.getDecimalScale2(discountedPrice);
    }

    public void setNotAccounted(BigDecimal notAccounted) {
        this.notAccounted = NumberUtils.getDecimalScale2(notAccounted);
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public void setPrePayment(boolean prePayment) {
        this.prePayment = prePayment;
    }

    public abstract void setPrice();

    public void setPrice(BigDecimal price) {
        this.price = NumberUtils.getDecimalScale2(price);
    }

    public void setService(Service service) {
        this.service = service;
        // Store the service information as literal strings.
        setServiceSnapshot();
    }

    private void setServiceAreaName(String serviceAreaName) {
        this.serviceAreaName = StringHelper.format(serviceAreaName);
    }

    public void setServiceCode(ServiceCode serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setServiceCodeName(String serviceCodeName) {
        this.serviceCodeName = StringHelper.format(serviceCodeName);
    }

    public void setServiceName(String serviceName) {
        this.serviceName = StringHelper.format(serviceName);
    }

    private void setServiceSnapshot() {
        if (getService() != null) {
            setServiceAreaName(getService().getServiceType().getServiceArea().getName());
            setServiceTypeName(getService().getServiceType().getName());
            setServiceName(getService().getName());
            setServiceCode(getService().getServiceCode());
            setServiceCodeName(getService().getCode());
        } else {
            setServiceAreaName(null);
            setServiceTypeName(null);
            setServiceName(null);
            setServiceCode(null);
            setServiceCodeName(null);
        }
    }

    private void setServiceTypeName(String serviceTypeName) {
        this.serviceTypeName = StringHelper.format(serviceTypeName);
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = NumberUtils.getDecimalScale2(taxRate);
    }

    public void setTaxType(final TaxType taxType) {
        this.taxType = taxType;
    }

    public void setTotal(BigDecimal total) {
        this.total = NumberUtils.getDecimalScale2(total);
    }

    public void setTotalAndComputePrice(BigDecimal total) {
        setTotal(total);
        // Recompute the price.
        setPrice();
    }
}