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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Messages;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.NumberUtils;

@Entity
@XmlRootElement
public class ServiceOrganizationTypePrice extends AbstractBaseEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal additionalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal basicPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal euGrantPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationtypeid")
    @XmlIDREF
    private OrganizationType organizationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceid")
    @XmlIDREF
    private Service service;

    public ServiceOrganizationTypePrice() {
        super();
    }

    public ServiceOrganizationTypePrice(Service service, OrganizationType organizationType) {
        super();
        setService(service);
        setOrganizationType(organizationType);
    }

    @Override
    public ServiceOrganizationTypePrice clone() throws CloneNotSupportedException {
        return (ServiceOrganizationTypePrice) super.clone();
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            // Important: use trimmed class name because of hibernate proxy issues.
            String objectClassName = ClassHelper.getTrimmedClassName(object.getClass().getName());
            if (objectClassName != null && objectClassName.equals(getClass().getName())) {
                ServiceOrganizationTypePrice baseEntity = (ServiceOrganizationTypePrice) object;
                if (getService() != null && baseEntity.getService() != null && getService().equals(baseEntity.getService()) && getOrganizationType() != null && baseEntity.getOrganizationType() != null
                    || getService() == null && baseEntity.getService() == null) {
                    if (getOrganizationType().getId() < baseEntity.getOrganizationType().getId()) {
                        return -1;
                    } else if (getOrganizationType().getId() > baseEntity.getOrganizationType().getId()) {
                        return 1;
                    }
                    return 0;
                }
                return super.compareTo(object);
            }
            throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + object.getClass().getName());
        }
        throw new ClassCastException("Cannot compare this " + getClass().getName() + " with NULL");
    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public BigDecimal getBasicPrice() {
        return basicPrice;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder();
        if (getOrganizationType() != null) {
            addEntityInfoItem(summary, "organizationType", getOrganizationType().getId());
        }
        if (getBasicPrice() != null) {
            summary.append(" ").append(Messages.get("basicPrice")).append(" ").append(getBasicPrice());
        }
        if (getEuGrantPrice() != null) {
            summary.append(" ").append(Messages.get("euGrantPrice")).append(" ").append(getEuGrantPrice());
        }
        if (getAdditionalPrice() != null) {
            summary.append(" ").append(Messages.get("additionalPrice")).append(" ").append(getAdditionalPrice());
        }
        return summary.toString();
    }

    public BigDecimal getEuGrantPrice() {
        return euGrantPrice;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public Service getService() {
        return service;
    }

    public void setAdditionalPrice(BigDecimal price) {
        this.additionalPrice = NumberUtils.getDecimalScale2(price);
    }

    public void setBasicPrice(BigDecimal price) {
        this.basicPrice = NumberUtils.getDecimalScale2(price);
    }

    public void setEuGrantPrice(BigDecimal euGrantPrice) {
        this.euGrantPrice = NumberUtils.getDecimalScale2(euGrantPrice);
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public void setService(Service service) {
        this.service = service;
    }
}