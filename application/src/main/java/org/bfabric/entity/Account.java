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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "account_name_unique", columnNames = { "name", "bookerid" }) })
@XmlRootElement
@NamedQuery(name = "Account.checkUniqueName", query = "SELECT a.id FROM Account a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.booker = :booker")
public class Account extends AbstractBookerDependentEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotBlank
    @Size(max = 16)
    @XmlElement
    private String accountNr;

    @OneToMany(mappedBy = "account")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Booking> bookings = new HashSet<>();

    public Account() {
        super();
    }

    @Override
    public Account clone() throws CloneNotSupportedException {
        Account clone = (Account) super.clone();
        clone.bookings = new HashSet<>();
        return clone;
    }

    public String getAccountNr() {
        return accountNr;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getAccountNr())) {
            addEntityInfoItem(summary, "accountNr", getAccountNr());
        }
        return summary.toString();
    }

    public void setAccountNr(String accountNr) {
        this.accountNr = StringHelper.format(accountNr);
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }
}