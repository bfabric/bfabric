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

package org.bfabric.forms;

import org.bfabric.entity.BookingType;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOrganization;

public class MFOrganization extends AbstractMF {

    private final Organization organization;

    private final XMLRequestParameterSaveOrganization xmlRequestSaveOrganization;

    public MFOrganization(Organization organization, XMLRequestParameterSaveOrganization xmlRequestSaveOrganization) {
        this.organization = organization;
        this.xmlRequestSaveOrganization = xmlRequestSaveOrganization;
    }

    @Override
    public void apply() throws Exception {
        getOrganization().setName(getName());
        getOrganization().setVatNumber(getVatNumber());
        getOrganization().setDebitorNumber(getDebitorNumber());
        getOrganization().setOrganizationType(getOrganizationType());
        getOrganization().setBillingOrganizationType(getBillingOrganizationType());
        getOrganization().setDefaultBookingType(getDefaultBookingType());
    }

    private OrganizationType getBillingOrganizationType() throws InvalidDataException {
        if (getXmlRequestSaveOrganization().getBillingorganizationtypeid() != null) {
            return (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("billingorganizationtypeid", getXmlRequestSaveOrganization().getBillingorganizationtypeid()));
        }
        return getOrganization().getBillingOrganizationType();
    }

    public Long getDebitorNumber() {
        if (getXmlRequestSaveOrganization().getDebitornumber() != null) {
            return getXmlRequestSaveOrganization().getDebitornumber();
        }
        return getOrganization().getDebitorNumber();
    }

    private BookingType getDefaultBookingType() throws InvalidDataException {
        if (getXmlRequestSaveOrganization().getDefaultbookingtypeid() != null) {
            MFHelper.checkNotNull("defaultbookingtypeid", getXmlRequestSaveOrganization().getDefaultbookingtypeid());
            return (BookingType) fetch(BookingType.class, MFHelper.positiveLongValueOf("billingbookingtypeid", getXmlRequestSaveOrganization().getDefaultbookingtypeid()));
        }
        return getOrganization().getDefaultBookingType();
    }

    public String getName() {
        if (getXmlRequestSaveOrganization().getName() != null) {
            return getXmlRequestSaveOrganization().getName();
        }
        return getOrganization().getName();
    }

    public Organization getOrganization() {
        return organization;
    }

    private OrganizationType getOrganizationType() throws InvalidDataException {
        if (getXmlRequestSaveOrganization().getOrganizationtypeid() != null) {
            MFHelper.checkNotNull("organizationtypeid", getXmlRequestSaveOrganization().getOrganizationtypeid());
            return (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("organizationtypeid", getXmlRequestSaveOrganization().getOrganizationtypeid()));
        }
        return getOrganization().getOrganizationType();
    }

    public String getVatNumber() {
        if (getXmlRequestSaveOrganization().getVatnumber() != null) {
            return getXmlRequestSaveOrganization().getVatnumber();
        }
        return getOrganization().getVatNumber();
    }

    public XMLRequestParameterSaveOrganization getXmlRequestSaveOrganization() {
        return xmlRequestSaveOrganization;
    }
}