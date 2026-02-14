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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
public class HomePhoneNumber extends AbstractPhoneNumber {

    private static final long serialVersionUID = 1;

    @Column(name = "homephoneareacode")
    @Min(0)
    @Max(9999)
    @XmlElement(name = "homephoneareacode")
    private Integer areaCode;

    @Column(name = "homephonecountrycode")
    @Min(0)
    @Max(999)
    @XmlElement(name = "homephonecountrycode")
    private Integer countryCode;

    @Column(updatable = false, insertable = false)
    private String homePhone;

    @Column(name = "homephonelocalnumber")
    @Min(0)
    @Max(99999999)
    @XmlElement(name = "homephonelocalnumber")
    private Integer localNumber;

    public HomePhoneNumber() {
    }

    public HomePhoneNumber(Integer countryCode, Integer areaCode, Integer localNumber) {
        this.countryCode = countryCode;
        this.areaCode = areaCode;
        this.localNumber = localNumber;
    }

    public HomePhoneNumber(PhoneNumber phoneNumber) {
        this.countryCode = phoneNumber.getCountryCode();
        this.areaCode = phoneNumber.getAreaCode();
        this.localNumber = phoneNumber.getLocalNumber();
    }

    @Override
    public Integer getAreaCode() {
        return areaCode;
    }

    @Override
    public Integer getCountryCode() {
        return countryCode;
    }

    public String getHomePhone() {
        return homePhone;
    }

    @Override
    public Integer getLocalNumber() {
        return localNumber;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public void setCountryCode(Integer countryCode) {
        this.countryCode = countryCode;
    }

    public void setLocalNumber(Integer localNumber) {
        this.localNumber = localNumber;
    }
}