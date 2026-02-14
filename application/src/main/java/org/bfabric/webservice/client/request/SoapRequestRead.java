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

package org.bfabric.webservice.client.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.webservice.request.parameter.XMLRequestParameterReadAccess;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadAnnotation;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadApplication;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadAttachment;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadBooking;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadCharge;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadComment;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadCommentTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadCompany;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadConsumable;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadContainerStatus;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadDataset;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadDepartment;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadDivision;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadExecutable;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadExternalJob;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadImportResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadInstitute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadInstrument;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadInstrumentReservation;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadLink;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadMail;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadMultiplexId;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadMultiplexKit;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadOffer;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadOfferedCharge;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadOption;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadOptionValue;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadOrganization;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadOrganizationType;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadParameter;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadProject;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadRole;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadSample;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadSamplePreparationProtocol;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadServiceArea;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadServiceType;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadStorage;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadUser;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadWorkflow;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadWorkflowStep;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadWorkflowTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadWorkflowTemplateStep;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadWorkunit;

@XmlRootElement(name = "soapenv:Envelope")
public class SoapRequestRead extends AbstractSoapRequest {

    @XmlElement(name = "soapenv:Body")
    private SoapenvBody soapenvBody;

    public static SoapRequestRead instance(String login, String password, int page) {
        SoapRequestRead ret = new SoapRequestRead();
        ret.soapenvBody = new SoapenvBody();
        ret.soapenvBody.end = new End();
        ret.soapenvBody.end.parameters = new Parameters();
        ret.soapenvBody.end.parameters.login = login;
        ret.soapenvBody.end.parameters.password = password;
        ret.soapenvBody.end.parameters.page = page;
        return ret;
    }

    public SoapenvBody getSoapenvBody() {
        return soapenvBody;
    }

    public static class End {

        @XmlElement
        private Parameters parameters;

        public Parameters getParameters() {
            return parameters;
        }
    }

    public static class Parameters {

        @XmlElement(name = "query")
        private XMLRequestParameterReadAccess accessQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadAnnotation annotationQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadApplication applicationQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadAttachment attachmentQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadBooking bookingQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadCharge chargeQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadComment commentQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadCommentTemplate commentTemplateQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadCompany companyQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadConsumable consumableQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadContainerStatus customContainerStatusQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadDataset datasetQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadDepartment departmentQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadDivision divisionQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadExecutable executableQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadExternalJob externalJobQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadImportResource importResourceQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadInstitute instituteQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadInstrument instrumentQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadInstrumentReservation instrumentReservationQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadLink linkQuery;

        @XmlElement
        private String login;

        @XmlElement(name = "query")
        private XMLRequestParameterReadMail mailQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadMultiplexId multiplexIdQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadMultiplexKit multiplexKitQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadOffer offerQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadOfferedCharge offeredChargeQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadOption optionQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadOptionValue optionValueQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadOrganization organizationQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadOrganizationType organizationTypeQuery;

        @XmlElement
        private Integer page;

        @XmlElement(name = "query")
        private XMLRequestParameterReadParameter parameterQuery;

        @XmlElement
        private String password;

        @XmlElement(name = "query")
        private XMLRequestParameterReadProject projectQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadResource resourceQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadRole roleQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadSamplePreparationProtocol samplePreparationProtocolQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadSample sampleQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadServiceArea serviceAreaQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadService serviceQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadServiceType serviceTypeQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadStorage storageQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadUser userQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadWorkflow workflowQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadWorkflowStep workflowStepQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadWorkflowTemplate workflowTemplateQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadWorkflowTemplateStep workflowTemplateStepQuery;

        @XmlElement(name = "query")
        private XMLRequestParameterReadWorkunit workunitQuery;

        public XMLRequestParameterReadAccess getAccessQuery() {
            return accessQuery;
        }

        public XMLRequestParameterReadAnnotation getAnnotationQuery() {
            return annotationQuery;
        }

        public XMLRequestParameterReadApplication getApplicationQuery() {
            return applicationQuery;
        }

        public XMLRequestParameterReadAttachment getAttachmentQuery() {
            return attachmentQuery;
        }

        public XMLRequestParameterReadBooking getBookingQuery() {
            return bookingQuery;
        }

        public XMLRequestParameterReadCharge getChargeQuery() {
            return chargeQuery;
        }

        public XMLRequestParameterReadComment getCommentQuery() {
            return commentQuery;
        }

        public XMLRequestParameterReadCommentTemplate getCommentTemplateQuery() {
            return commentTemplateQuery;
        }

        public XMLRequestParameterReadCompany getCompanyQuery() {
            return companyQuery;
        }

        public XMLRequestParameterReadConsumable getConsumableQuery() {
            return consumableQuery;
        }

        public XMLRequestParameterReadContainerStatus getCustomContainerStatusQuery() {
            return customContainerStatusQuery;
        }

        public XMLRequestParameterReadDataset getDatasetQuery() {
            return datasetQuery;
        }

        public XMLRequestParameterReadDepartment getDepartmentQuery() {
            return departmentQuery;
        }

        public XMLRequestParameterReadDivision getDivisionQuery() {
            return divisionQuery;
        }

        public XMLRequestParameterReadExecutable getExecutableQuery() {
            return executableQuery;
        }

        public XMLRequestParameterReadExternalJob getExternalJobQuery() {
            return externalJobQuery;
        }

        public XMLRequestParameterReadImportResource getImportResourceQuery() {
            return importResourceQuery;
        }

        public XMLRequestParameterReadInstitute getInstituteQuery() {
            return instituteQuery;
        }

        public XMLRequestParameterReadInstrument getInstrumentQuery() {
            return instrumentQuery;
        }

        public XMLRequestParameterReadInstrumentReservation getInstrumentReservationQuery() {
            return instrumentReservationQuery;
        }

        public XMLRequestParameterReadLink getLinkQuery() {
            return linkQuery;
        }

        public String getLogin() {
            return login;
        }

        public XMLRequestParameterReadMail getMailQuery() {
            return mailQuery;
        }

        public XMLRequestParameterReadMultiplexId getMultiplexIdQuery() {
            return multiplexIdQuery;
        }

        public XMLRequestParameterReadMultiplexKit getMultiplexKitQuery() {
            return multiplexKitQuery;
        }

        public XMLRequestParameterReadOffer getOffer() {
            return offerQuery;
        }

        public XMLRequestParameterReadOfferedCharge getOfferedChargeQuery() {
            return offeredChargeQuery;
        }

        public XMLRequestParameterReadOption getOptionQuery() {
            return optionQuery;
        }

        public XMLRequestParameterReadOptionValue getOptionValueQuery() {
            return optionValueQuery;
        }

        public XMLRequestParameterReadOrganization getOrganizationQuery() {
            return organizationQuery;
        }

        public XMLRequestParameterReadOrganizationType getOrganizationTypeQuery() {
            return organizationTypeQuery;
        }

        public Integer getPage() {
            return page;
        }

        public XMLRequestParameterReadParameter getParameterQuery() {
            return parameterQuery;
        }

        public String getPassword() {
            return password;
        }

        public XMLRequestParameterReadProject getProjectQuery() {
            return projectQuery;
        }

        public XMLRequestParameterReadResource getResourceQuery() {
            return resourceQuery;
        }

        public XMLRequestParameterReadRole getRoleQuery() {
            return roleQuery;
        }

        public XMLRequestParameterReadSamplePreparationProtocol getSamplePreparationProtocolQuery() {
            return samplePreparationProtocolQuery;
        }

        public XMLRequestParameterReadSample getSampleQuery() {
            return sampleQuery;
        }

        public XMLRequestParameterReadServiceArea getServiceAreaQuery() {
            return serviceAreaQuery;
        }

        public XMLRequestParameterReadService getServiceQuery() {
            return serviceQuery;
        }

        public XMLRequestParameterReadServiceType getServiceTypeQuery() {
            return serviceTypeQuery;
        }

        public XMLRequestParameterReadStorage getStorageQuery() {
            return storageQuery;
        }

        public XMLRequestParameterReadUser getUserQuery() {
            return userQuery;
        }

        public XMLRequestParameterReadWorkflow getWorkflowQuery() {
            return workflowQuery;
        }

        public XMLRequestParameterReadWorkflowStep getWorkflowStepQuery() {
            return workflowStepQuery;
        }

        public XMLRequestParameterReadWorkflowTemplate getWorkflowTemplateQuery() {
            return workflowTemplateQuery;
        }

        public XMLRequestParameterReadWorkflowTemplateStep getWorkflowTemplateStepQuery() {
            return workflowTemplateStepQuery;
        }

        public XMLRequestParameterReadWorkunit getWorkunitQuery() {
            return workunitQuery;
        }
    }

    public static class SoapenvBody {

        @XmlElement(name = "end:read")
        private End end;

        public End getEnd() {
            return end;
        }
    }
}
