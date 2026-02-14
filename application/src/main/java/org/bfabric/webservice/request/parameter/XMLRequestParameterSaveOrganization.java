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

package org.bfabric.webservice.request.parameter;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveOrganization extends XMLRequestParameterSaveAbstractNamedBaseEntity {

    @XmlElement
    private String billingorganizationtypeid;

    @XmlElement
    private Long debitornumber;

    @XmlElement
    private String defaultbookingtypeid;

    @XmlElement
    private String organizationtypeid;

    @XmlElement
    private String vatnumber;

    public String getBillingorganizationtypeid() {
        return billingorganizationtypeid;
    }

    public Long getDebitornumber() {
        return debitornumber;
    }

    public String getDefaultbookingtypeid() {
        return defaultbookingtypeid;
    }

    public String getOrganizationtypeid() {
        return organizationtypeid;
    }

    public String getVatnumber() {
        return vatnumber;
    }

    public void setBillingorganizationtypeid(String billingorganizationtypeid) {
        this.billingorganizationtypeid = billingorganizationtypeid;
    }

    public void setDebitornumber(Long debitornumber) {
        this.debitornumber = debitornumber;
    }

    public void setDefaultbookingtypeid(String defaultbookingtypeid) {
        this.defaultbookingtypeid = defaultbookingtypeid;
    }

    public void setOrganizationtypeid(String organizationtypeid) {
        this.organizationtypeid = organizationtypeid;
    }

    public void setVatnumber(String vatnumber) {
        this.vatnumber = vatnumber;
    }
}
