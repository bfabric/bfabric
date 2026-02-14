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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.Constants;
import org.bfabric.util.StringHelper;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
public class Address extends AbstractAddress {

    private static final long serialVersionUID = 1;

    @Column(updatable = false, insertable = false)
    private String address;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country")
    @NotNull
    @XmlIDREF
    private Country country;

    @Size(max = 32)
    @XmlElement
    private String room;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String street;

    @Size(max = 64)
    @XmlElement
    private String supplement;

    @NotBlank
    @Size(max = 16)
    @XmlElement
    private String zip;

    public Address() {
    }

    public Address(HomeAddress address) {
        setSupplement(address.getSupplement());
        setStreet(address.getStreet());
        setZip(address.getZip());
        setCity(address.getCity());
        setCountry(address.getCountry());
    }

    public Address(String street, String supplement, String zip, String city, Country country, String room) {
        setSupplement(supplement);
        setStreet(street);
        setZip(zip);
        setCity(city);
        setCountry(country);
        setRoom(room);
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public Country getCountry() {
        return country;
    }

    public String getFullAddressRoom() {
        return StringHelper.getFullAddress(getStreetRoom(), getZip(), getCity(), getCountry());
    }

    public String getFullAddressRoomWithLineBreaks() {
        return StringHelper.getFullAddress(getStreetRoom(), getZip(), getCity(), getCountry(), 1);
    }

    public String getRoom() {
        return room;
    }

    @Override
    public String getStreet() {
        return street;
    }

    public String getStreetRoom() {
        return getStreet() + (StringHelper.isNotEmpty(getRoom()) ? ", " + getRoom() : Constants.EMPTY_STRING);
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

    public void setRoom(String room) {
        this.room = StringHelper.format(room);
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