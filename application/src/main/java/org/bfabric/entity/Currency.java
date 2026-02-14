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

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "currency_name_unique", columnNames = { "name" }),
    @UniqueConstraint(name = "currency_code_unique", columnNames = { "code" }),
    @UniqueConstraint(name = "currency_symbol_unique", columnNames = { "symbol" }) })
@XmlRootElement
@NamedQuery(name = "Currency.findByCode", query = "SELECT a FROM Currency a WHERE lower(a.code) = lower(:code)")
@NamedQuery(name = "Currency.findByName", query = "SELECT a FROM Currency a WHERE lower(a.name) = lower(:name)")
public class Currency extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "currency")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Booking> bookings = new HashSet<>();

    @NotEmpty
    @Size(max = 3)
    @XmlElement
    private String code;

    @OneToMany(mappedBy = "currency")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Consumable> consumables = new HashSet<>();

    @OneToMany(mappedBy = "currency")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Contract> contracts = new HashSet<>();

    @OneToMany(mappedBy = "currency")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> instruments = new HashSet<>();

    @OneToMany(mappedBy = "invoicedCurrency")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> invoicedPurchases = new HashSet<>();

    @OneToMany(mappedBy = "currency")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Purchase> purchases = new HashSet<>();

    @NotEmpty
    @Size(max = 4)
    @XmlElement
    private String symbol;

    public Currency() {
        super();
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public String getCode() {
        return code;
    }

    public Set<Consumable> getConsumables() {
        return consumables;
    }

    public Set<Contract> getContracts() {
        return contracts;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }

    public Set<Purchase> getInvoicedPurchases() {
        return invoicedPurchases;
    }

    public Set<Purchase> getPurchases() {
        return purchases;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isDefault() {
        return getCode().equalsIgnoreCase(getConfiguration().getDefaultCurrencyCode());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && getBookings().isEmpty() && getConsumables().isEmpty() && getContracts().isEmpty() && getInstruments().isEmpty() && getPurchases().isEmpty() && getInvoicedPurchases().isEmpty();
    }

    public void setCode(String code) {
        this.code = code != null ? code.toUpperCase() : null;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}