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

import java.time.LocalDate;
import java.time.Period;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractAccessCardProfile extends AbstractUser {

    private static final long serialVersionUID = 1;

    @Size(max = 64)
    @XmlElement
    private String accessCardCode;

    @XmlElement
    private LocalDate accessCardExpiryDate;

    @Size(max = 16)
    @XmlElement
    private String accessCardNumber;

    @PastOrPresent
    @XmlElement
    private LocalDate birthDate;

    public AbstractAccessCardProfile() {
    }

    public String getAccessCardCode() {
        return accessCardCode;
    }

    public LocalDate getAccessCardExpiryDate() {
        return accessCardExpiryDate;
    }

    public String getAccessCardNumber() {
        return accessCardNumber;
    }

    public Integer getAge() {
        return birthDate != null ? Period.between(birthDate, LocalDate.now()).getYears() : null;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    @SuppressWarnings("unused")
    public String getBirthDay() {
        return birthDate != null ? birthDate.toString() : null;
    }

    public void setAccessCardCode(String accessCardCode) {
        this.accessCardCode = StringHelper.firstUpper(StringHelper.format(accessCardCode));
    }

    public void setAccessCardExpiryDate(LocalDate accessCardExpiryDate) {
        this.accessCardExpiryDate = accessCardExpiryDate;
    }

    public void setAccessCardNumber(String accessCardNumber) {
        this.accessCardNumber = StringHelper.format(accessCardNumber);
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}