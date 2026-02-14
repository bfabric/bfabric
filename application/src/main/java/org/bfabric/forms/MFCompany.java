package org.bfabric.forms;

import org.bfabric.entity.BookingType;
import org.bfabric.entity.Company;
import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCompany;

public class MFCompany extends AbstractMF {

    private final Company company;

    private final XMLRequestParameterSaveCompany xmlRequestSaveCompany;

    public MFCompany(Company company, XMLRequestParameterSaveCompany xmlRequestSaveCompany) {
        this.company = company;
        this.xmlRequestSaveCompany = xmlRequestSaveCompany;
    }

    @Override
    public void apply() throws Exception {
        getCompany().setName(getName());
        getCompany().setVatNumber(getVatNumber());
        getCompany().setDebitorNumber(getDebitorNumber());
        getCompany().setOrganizationType(getOrganizationType());
        getCompany().setBillingOrganizationType(getBillingOrganizationType());
        getCompany().setDefaultBookingType(getDefaultBookingType());
    }

    private OrganizationType getBillingOrganizationType() throws InvalidDataException {
        if (getXmlRequestSaveCompany().getBillingorganizationtypeid() != null) {
            return (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("billingorganizationtypeid", getXmlRequestSaveCompany().getBillingorganizationtypeid()));
        }
        return getCompany().getBillingOrganizationType();
    }

    public Company getCompany() {
        return company;
    }

    public Long getDebitorNumber() {
        if (getXmlRequestSaveCompany().getDebitornumber() != null) {
            return getXmlRequestSaveCompany().getDebitornumber();
        }
        return getCompany().getDebitorNumber();
    }

    private BookingType getDefaultBookingType() throws InvalidDataException {
        if (getXmlRequestSaveCompany().getDefaultbookingtypeid() != null) {
            MFHelper.checkNotNull("defaultbookingtypeid", getXmlRequestSaveCompany().getDefaultbookingtypeid());
            return (BookingType) fetch(BookingType.class, MFHelper.positiveLongValueOf("billingbookingtypeid", getXmlRequestSaveCompany().getDefaultbookingtypeid()));
        }
        return getCompany().getDefaultBookingType();
    }

    public String getName() {
        if (getXmlRequestSaveCompany().getName() != null) {
            return getXmlRequestSaveCompany().getName();
        }
        return getCompany().getName();
    }

    private OrganizationType getOrganizationType() throws InvalidDataException {
        if (getXmlRequestSaveCompany().getOrganizationtypeid() != null) {
            MFHelper.checkNotNull("organizationtypeid", getXmlRequestSaveCompany().getOrganizationtypeid());
            return (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("organizationtypeid", getXmlRequestSaveCompany().getOrganizationtypeid()));
        }
        return getCompany().getOrganizationType();
    }

    public String getVatNumber() {
        if (getXmlRequestSaveCompany().getVatnumber() != null) {
            return getXmlRequestSaveCompany().getVatnumber();
        }
        return getCompany().getVatNumber();
    }

    public XMLRequestParameterSaveCompany getXmlRequestSaveCompany() {
        return xmlRequestSaveCompany;
    }
}
