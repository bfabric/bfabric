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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.NumberUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "taxtype_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "TaxType.findByName", query = "SELECT a FROM TaxType a WHERE lower(a.name) = lower(:name)")
public class TaxType extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "taxType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Digits(integer = 3, fraction = 2)
    @XmlElement
    private BigDecimal tax = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public TaxType() {
        super();
    }

    @Override
    public TaxType clone() throws CloneNotSupportedException {
        final TaxType clone = (TaxType) super.clone();
        clone.charges = new HashSet<>();
        return clone;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.BOOKINGMANAGER;
    }

    @Override
    public String getDisplayName() {
        return getName() + " (" + getTax() + "%)";
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getTax() != null) {
            addEntityInfoItem(summary, "tax", getTax());
        }
        return summary.toString();
    }

    public BigDecimal getTax() {
        return tax;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getCharges().isEmpty();
    }

    public void setCharges(Set<Charge> charges) {
        this.charges = charges;
    }

    public void setTax(BigDecimal tax) {
        this.tax = NumberUtils.getDecimalScale2(tax);
    }
}