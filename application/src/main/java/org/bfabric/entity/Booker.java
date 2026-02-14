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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "booker_name_unique", columnNames = { "name" }), @UniqueConstraint(name = "booker_vatnumber_unique", columnNames = { "vatnumber" }) })
@XmlRootElement
@NamedQuery(name = "Booker.findByName", query = "SELECT a FROM Booker a WHERE lower(a.name) = lower(:name)")
@NamedQuery(name = "Booker.checkUniqueVatNumber", query = "SELECT a.id FROM Booker a WHERE lower(a.vatNumber) = lower(:vatNumber) and a.id <> :id")
public class Booker extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "booker")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Account> accounts = new HashSet<>();

    @OneToMany(mappedBy = "booker")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<BookingType> bookingTypes = new HashSet<>();

    @OneToMany(mappedBy = "booker")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "booker")
    @OrderBy("orderPosition")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<CostCentre> costCentres = new HashSet<>();

    @OneToMany(mappedBy = "booker")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<FinancialCenter> financialCenters = new HashSet<>();

    @NotBlank
    @Size(max = 32)
    @Column(unique = true)
    @XmlElement
    private String vatNumber;

    public Booker() {
        super();
    }

    @Override
    public Booker clone() throws CloneNotSupportedException {
        Booker clone = (Booker) super.clone();
        clone.accounts = new HashSet<>();
        clone.bookings = new HashSet<>();
        clone.bookingTypes = new HashSet<>();
        clone.costCentres = new HashSet<>();
        clone.financialCenters = new HashSet<>();
        return clone;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public Set<BookingType> getBookingTypes() {
        return bookingTypes;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public Set<CostCentre> getCostCentres() {
        return costCentres;
    }

    public Set<CostCentre> getCostCentresEnabled() {
        Set<CostCentre> costCentresEnabled = new HashSet<>();
        for (CostCentre costCentre : getCostCentres()) {
            if (costCentre.isEnabled()) {
                costCentresEnabled.add(costCentre);
            }
        }
        return costCentresEnabled;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.BOOKINGMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getVatNumber())) {
            addEntityInfoItem(summary, "vatNumber", getVatNumber());
        }
        return summary.toString();
    }

    public Set<FinancialCenter> getFinancialCenters() {
        return financialCenters;
    }

    public String getVatNumber() {
        return this.vatNumber;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getAccounts().isEmpty() && getBookingTypes().isEmpty() && getCostCentres().isEmpty() && getFinancialCenters().isEmpty() && getBookings().isEmpty();
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    public void setBookingTypes(Set<BookingType> bookingTypes) {
        this.bookingTypes = bookingTypes;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    public void setCostCentres(Set<CostCentre> costCentres) {
        this.costCentres = costCentres;
    }

    public void setFinancialCenters(Set<FinancialCenter> financialCenters) {
        this.financialCenters = financialCenters;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = StringHelper.format(vatNumber);
    }
}