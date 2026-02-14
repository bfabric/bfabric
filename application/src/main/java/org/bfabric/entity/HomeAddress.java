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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.util.StringHelper;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
public class HomeAddress extends AbstractAddress {

    private static final long serialVersionUID = 1;

    @Column(name = "homecity")
    @Size(max = 64)
    @XmlElement(name = "homecity")
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homecountry")
    @XmlElement(name = "homecountry")
    @XmlIDREF
    private Country country;

    @Column(updatable = false, insertable = false)
    private String homeAddress;

    @Column(name = "homestreet")
    @Size(max = 64)
    @XmlElement(name = "homestreet")
    private String street;

    @Column(name = "homesupplement")
    @Size(max = 64)
    @XmlElement(name = "homesupplement")
    private String supplement;

    @Column(name = "homezip")
    @Size(max = 16)
    @XmlElement(name = "homezip")
    private String zip;

    public HomeAddress() {
    }

    public HomeAddress(Address address) {
        setSupplement(address.getSupplement());
        setStreet(address.getStreet());
        setZip(address.getZip());
        setCity(address.getCity());
        setCountry(address.getCountry());
    }

    public HomeAddress(String street, String zip, String city, Country country) {
        setStreet(street);
        setZip(zip);
        setCity(city);
        setCountry(country);
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public Country getCountry() {
        return country;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    @Override
    public String getStreet() {
        return street;
    }

    @Override
    public String getSupplement() {
        return supplement;
    }

    @Override
    public String getZip() {
        return zip;
    }

    public void setCity(String city) {
        this.city = StringHelper.format(city);
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public void setStreet(String street) {
        this.street = StringHelper.format(street);
    }

    public void setSupplement(String supplement) {
        this.supplement = StringHelper.format(supplement);
    }

    public void setZip(String zip) {
        this.zip = StringHelper.format(zip);
    }
}
