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

package org.bfabric.xml.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Company;

@XmlRootElement(name = "company")
public class XMLCompany extends XMLAbstractNamedBaseEntity {

    @XmlElement
    private XMLOrganizationType billingorganizationtype;

    @XmlElement
    private Long debitornumber;

    @XmlElement
    private String defaultbookingtype;

    @XmlElement
    private XMLOrganizationType organizationtype;

    @XmlElement
    private String vatnumber;

    public XMLCompany() {
    }

    public XMLCompany(Company entity, boolean reference) {
        super(entity, reference);
    }

    public XMLCompany(Company entity) {
        super(entity);
        if (entity != null) {
            if (entity.getVatNumber() != null) {
                setVatnumber(entity.getVatNumber());
            }
            if (entity.getBillingOrganizationType() != null) {
                setBillingorganizationtype(new XMLOrganizationType(entity.getBillingOrganizationType(), true));
            }
            if (entity.getDefaultBookingType() != null) {
                setDefaultbookingtype(entity.getDefaultBookingType().getIdString());
            }
            if (entity.getOrganizationType() != null) {
                setOrganizationtype(new XMLOrganizationType(entity.getOrganizationType(), true));
            }
            if (entity.getDebitorNumber() != null) {
                setDebitornumber(entity.getDebitorNumber());
            }
        }
    }

    public XMLOrganizationType getBillingorganizationtype() {
        return billingorganizationtype;
    }

    public Long getDebitornumber() {
        return debitornumber;
    }

    public String getDefaultbookingtype() {
        return defaultbookingtype;
    }

    public XMLOrganizationType getOrganizationtype() {
        return organizationtype;
    }

    public String getVatnumber() {
        return vatnumber;
    }

    public void setBillingorganizationtype(XMLOrganizationType billingorganizationtype) {
        this.billingorganizationtype = billingorganizationtype;
    }

    public void setDebitornumber(Long debitornumber) {
        this.debitornumber = debitornumber;
    }

    public void setDefaultbookingtype(String defaultbookingtype) {
        this.defaultbookingtype = defaultbookingtype;
    }

    public void setOrganizationtype(XMLOrganizationType organizationtype) {
        this.organizationtype = organizationtype;
    }

    public void setVatnumber(String vatnumber) {
        this.vatnumber = vatnumber;
    }
}
