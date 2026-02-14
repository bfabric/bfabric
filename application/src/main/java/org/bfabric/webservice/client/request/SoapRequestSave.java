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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveApplication;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveBooking;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCharge;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveComment;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCommentTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCompany;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveConsumable;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCustomContainerStatus;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDataset;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDatasetTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDepartment;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDivision;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveExecutable;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveExternalJob;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveImportResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstitute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstrumentReservation;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveJob;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveLink;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveMail;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOffer;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOfferedCharge;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOption;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOptionValue;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOrganization;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOrganizationType;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveParameter;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSavePlate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveSample;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveSamplePreparationProtocol;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveServiceArea;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveServiceType;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUser;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUserGroup;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflow;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowStep;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplateStep;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkunit;

@XmlRootElement(name = "soapenv:Envelope")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoapRequestSave extends AbstractSoapRequest {

    @XmlElement(name = "soapenv:Body")
    private SoapenvBody soapenvBody;

    public static SoapRequestSave instance(String login, String password) {
        SoapRequestSave soapRequestSave = new SoapRequestSave();
        soapRequestSave.soapenvBody = new SoapenvBody();
        soapRequestSave.soapenvBody.end = new End();
        soapRequestSave.soapenvBody.end.parameters = new Parameters();
        soapRequestSave.soapenvBody.end.parameters.login = login;
        soapRequestSave.soapenvBody.end.parameters.password = password;
        return soapRequestSave;
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

        @XmlElement(name = "application")
        private final List<XMLRequestParameterSaveApplication> xmlRequestSaveApplicationList = new ArrayList<>();

        @XmlElement(name = "booking")
        private final List<XMLRequestParameterSaveBooking> xmlRequestSaveBookingList = new ArrayList<>();

        @XmlElement(name = "charge")
        private final List<XMLRequestParameterSaveCharge> xmlRequestSaveChargeList = new ArrayList<>();

        @XmlElement(name = "comment")
        private final List<XMLRequestParameterSaveComment> xmlRequestSaveCommentList = new ArrayList<>();

        @XmlElement(name = "commenttemplate")
        private final List<XMLRequestParameterSaveCommentTemplate> xmlRequestSaveCommentTemplateList = new ArrayList<>();

        @XmlElement(name = "company")
        private final List<XMLRequestParameterSaveCompany> xmlRequestSaveCompanyList = new ArrayList<>();

        @XmlElement(name = "consumable")
        private final List<XMLRequestParameterSaveConsumable> xmlRequestSaveConsumableList = new ArrayList<>();

        @XmlElement(name = "customcontainerstatus")
        private final List<XMLRequestParameterSaveCustomContainerStatus> xmlRequestSaveCustomContainerStatusList = new ArrayList<>();

        @XmlElement(name = "dataset")
        private final List<XMLRequestParameterSaveDataset> xmlRequestSaveDatasetList = new ArrayList<>();

        @XmlElement(name = "datasettemplate")
        private final List<XMLRequestParameterSaveDatasetTemplate> xmlRequestSaveDatasetTemplateList = new ArrayList<>();

        @XmlElement(name = "executable")
        private final List<XMLRequestParameterSaveExecutable> xmlRequestSaveExecutableList = new ArrayList<>();

        @XmlElement(name = "externaljob")
        private final List<XMLRequestParameterSaveExternalJob> xmlRequestSaveExternalJobList = new ArrayList<>();

        @XmlElement(name = "job")
        private final List<XMLRequestParameterSaveJob> xmlRequestSaveJobList = new ArrayList<>();

        @XmlElement(name = "department")
        private final List<XMLRequestParameterSaveDepartment> xmlRequestSaveDepartmentList = new ArrayList<>();

        @XmlElement(name = "importresource")
        private final List<XMLRequestParameterSaveImportResource> xmlRequestSaveImportResourceList = new ArrayList<>();

        @XmlElement(name = "institute")
        private final List<XMLRequestParameterSaveInstitute> xmlRequestSaveInstituteList = new ArrayList<>();

        @XmlElement(name = "instrumentreservation")
        private final List<XMLRequestParameterSaveInstrumentReservation> xmlRequestSaveInstrumentReservationList = new ArrayList<>();

        @XmlElement(name = "link")
        private final List<XMLRequestParameterSaveLink> xmlRequestSaveLinkList = new ArrayList<>();

        @XmlElement(name = "mail")
        private final List<XMLRequestParameterSaveMail> xmlRequestSaveMailList = new ArrayList<>();

        @XmlElement(name = "offeredcharge")
        private final List<XMLRequestParameterSaveOfferedCharge> xmlRequestSaveOfferedChargeList = new ArrayList<>();

        @XmlElement(name = "offer")
        private final List<XMLRequestParameterSaveOffer> xmlRequestSaveOfferList = new ArrayList<>();

        @XmlElement(name = "option")
        private final List<XMLRequestParameterSaveOption> xmlRequestSaveOptionList = new ArrayList<>();

        @XmlElement(name = "optionvalue")
        private final List<XMLRequestParameterSaveOptionValue> xmlRequestSaveOptionValueList = new ArrayList<>();

        @XmlElement(name = "organization")
        private final List<XMLRequestParameterSaveOrganization> xmlRequestSaveOrganizationList = new ArrayList<>();

        @XmlElement(name = "organizationtype")
        private final List<XMLRequestParameterSaveOrganizationType> xmlRequestSaveOrganizationTypeList = new ArrayList<>();

        @XmlElement(name = "parameter")
        private final List<XMLRequestParameterSaveParameter> xmlRequestSaveParameterList = new ArrayList<>();

        @XmlElement(name = "plate")
        private final List<XMLRequestParameterSavePlate> xmlRequestSavePlateList = new ArrayList<>();

        @XmlElement(name = "division")
        private final List<XMLRequestParameterSaveDivision> xmlRequestSaveDivisionList = new ArrayList<>();

        @XmlElement(name = "resource")
        private final List<XMLRequestParameterSaveResource> xmlRequestSaveResourceList = new ArrayList<>();

        @XmlElement(name = "sample")
        private final List<XMLRequestParameterSaveSample> xmlRequestSaveSampleList = new ArrayList<>();

        @XmlElement(name = "samplePreparationProtocol")
        private final List<XMLRequestParameterSaveSamplePreparationProtocol> xmlRequestSaveSamplePreparationProtocolList = new ArrayList<>();

        @XmlElement(name = "servicearea")
        private final List<XMLRequestParameterSaveServiceArea> xmlRequestSaveServiceAreaList = new ArrayList<>();

        @XmlElement(name = "service")
        private final List<XMLRequestParameterSaveService> xmlRequestSaveServiceList = new ArrayList<>();

        @XmlElement(name = "servicetype")
        private final List<XMLRequestParameterSaveServiceType> xmlRequestSaveServiceTypeList = new ArrayList<>();

        @XmlElement(name = "user")
        private final List<XMLRequestParameterSaveUser> xmlRequestSaveUserList = new ArrayList<>();

        @XmlElement(name = "workflow")
        private final List<XMLRequestParameterSaveWorkflow> xmlRequestSaveWorkflowList = new ArrayList<>();

        @XmlElement(name = "workflowstep")
        private final List<XMLRequestParameterSaveWorkflowStep> xmlRequestSaveWorkflowStepList = new ArrayList<>();

        @XmlElement(name = "workflowtemplate")
        private final List<XMLRequestParameterSaveWorkflowTemplate> xmlRequestSaveWorkflowTemplateList = new ArrayList<>();

        @XmlElement(name = "workflowtemplatestep")
        private final List<XMLRequestParameterSaveWorkflowTemplateStep> xmlRequestSaveWorkflowTemplateStepList = new ArrayList<>();

        @XmlElement(name = "workunit")
        private final List<XMLRequestParameterSaveWorkunit> xmlRequestSaveWorkunitList = new ArrayList<>();

        @XmlElement(name = "usergroup")
        private final List<XMLRequestParameterSaveUserGroup> xmlRequestSaveUserGroupList = new ArrayList<>();

        @XmlElement
        private String login;

        @XmlElement
        private String password;

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public List<XMLRequestParameterSaveApplication> getXmlRequestSaveApplicationList() {
            return xmlRequestSaveApplicationList;
        }

        public List<XMLRequestParameterSaveBooking> getXmlRequestSaveBookingList() {
            return xmlRequestSaveBookingList;
        }

        public List<XMLRequestParameterSaveCharge> getXmlRequestSaveChargeList() {
            return xmlRequestSaveChargeList;
        }

        public List<XMLRequestParameterSaveComment> getXmlRequestSaveCommentList() {
            return xmlRequestSaveCommentList;
        }

        public List<XMLRequestParameterSaveCommentTemplate> getXmlRequestSaveCommentTemplateList() {
            return xmlRequestSaveCommentTemplateList;
        }

        public List<XMLRequestParameterSaveCompany> getXmlRequestSaveCompanyList() {
            return xmlRequestSaveCompanyList;
        }

        public List<XMLRequestParameterSaveConsumable> getXmlRequestSaveConsumableList() {
            return xmlRequestSaveConsumableList;
        }

        public List<XMLRequestParameterSaveCustomContainerStatus> getXmlRequestSaveCustomContainerStatusList() {
            return xmlRequestSaveCustomContainerStatusList;
        }

        public List<XMLRequestParameterSaveDataset> getXmlRequestSaveDatasetList() {
            return xmlRequestSaveDatasetList;
        }

        public List<XMLRequestParameterSaveDatasetTemplate> getXmlRequestSaveDatasetTemplateList() {
            return xmlRequestSaveDatasetTemplateList;
        }

        public List<XMLRequestParameterSaveDepartment> getXmlRequestSaveDepartmentList() {
            return xmlRequestSaveDepartmentList;
        }

        public List<XMLRequestParameterSaveDivision> getXmlRequestSaveDivisionList() {
            return xmlRequestSaveDivisionList;
        }

        public List<XMLRequestParameterSaveExecutable> getXmlRequestSaveExecutableList() {
            return xmlRequestSaveExecutableList;
        }

        public List<XMLRequestParameterSaveExternalJob> getXmlRequestSaveExternalJobList() {
            return xmlRequestSaveExternalJobList;
        }

        public List<XMLRequestParameterSaveImportResource> getXmlRequestSaveImportResourceList() {
            return xmlRequestSaveImportResourceList;
        }

        public List<XMLRequestParameterSaveInstitute> getXmlRequestSaveInstituteList() {
            return xmlRequestSaveInstituteList;
        }

        public List<XMLRequestParameterSaveInstrumentReservation> getXmlRequestSaveInstrumentReservationList() {
            return xmlRequestSaveInstrumentReservationList;
        }

        public List<XMLRequestParameterSaveJob> getXmlRequestSaveJobList() {
            return xmlRequestSaveJobList;
        }

        public List<XMLRequestParameterSaveLink> getXmlRequestSaveLinkList() {
            return xmlRequestSaveLinkList;
        }

        public List<XMLRequestParameterSaveMail> getXmlRequestSaveMailList() {
            return xmlRequestSaveMailList;
        }

        public List<XMLRequestParameterSaveOffer> getXmlRequestSaveOfferList() {
            return xmlRequestSaveOfferList;
        }

        public List<XMLRequestParameterSaveOfferedCharge> getXmlRequestSaveOfferedChargeList() {
            return xmlRequestSaveOfferedChargeList;
        }

        public List<XMLRequestParameterSaveOption> getXmlRequestSaveOptionList() {
            return xmlRequestSaveOptionList;
        }

        public List<XMLRequestParameterSaveOptionValue> getXmlRequestSaveOptionValueList() {
            return xmlRequestSaveOptionValueList;
        }

        public List<XMLRequestParameterSaveOrganization> getXmlRequestSaveOrganizationList() {
            return xmlRequestSaveOrganizationList;
        }

        public List<XMLRequestParameterSaveOrganizationType> getXmlRequestSaveOrganizationTypeList() {
            return xmlRequestSaveOrganizationTypeList;
        }

        public List<XMLRequestParameterSaveParameter> getXmlRequestSaveParameterList() {
            return xmlRequestSaveParameterList;
        }

        public List<XMLRequestParameterSavePlate> getXmlRequestSavePlateList() {
            return xmlRequestSavePlateList;
        }

        public List<XMLRequestParameterSaveResource> getXmlRequestSaveResourceList() {
            return xmlRequestSaveResourceList;
        }

        public List<XMLRequestParameterSaveSample> getXmlRequestSaveSampleList() {
            return xmlRequestSaveSampleList;
        }

        public List<XMLRequestParameterSaveSamplePreparationProtocol> getXmlRequestSaveSamplePreparationProtocolList() {
            return xmlRequestSaveSamplePreparationProtocolList;
        }

        public List<XMLRequestParameterSaveServiceArea> getXmlRequestSaveServiceAreaList() {
            return xmlRequestSaveServiceAreaList;
        }

        public List<XMLRequestParameterSaveService> getXmlRequestSaveServiceList() {
            return xmlRequestSaveServiceList;
        }

        public List<XMLRequestParameterSaveServiceType> getXmlRequestSaveServiceTypeList() {
            return xmlRequestSaveServiceTypeList;
        }

        public List<XMLRequestParameterSaveUserGroup> getXmlRequestSaveUserGroupList() {
            return xmlRequestSaveUserGroupList;
        }

        public List<XMLRequestParameterSaveUser> getXmlRequestSaveUserList() {
            return xmlRequestSaveUserList;
        }

        public List<XMLRequestParameterSaveWorkflow> getXmlRequestSaveWorkflowList() {
            return xmlRequestSaveWorkflowList;
        }

        public List<XMLRequestParameterSaveWorkflowStep> getXmlRequestSaveWorkflowStepList() {
            return xmlRequestSaveWorkflowStepList;
        }

        public List<XMLRequestParameterSaveWorkflowTemplate> getXmlRequestSaveWorkflowTemplateList() {
            return xmlRequestSaveWorkflowTemplateList;
        }

        public List<XMLRequestParameterSaveWorkflowTemplateStep> getXmlRequestSaveWorkflowTemplateStepList() {
            return xmlRequestSaveWorkflowTemplateStepList;
        }

        public List<XMLRequestParameterSaveWorkunit> getXmlRequestSaveWorkunitList() {
            return xmlRequestSaveWorkunitList;
        }

        public List<XMLRequestParameterSaveWorkunit> getXmlRequestSaveWorkunitTemplateList() {
            return xmlRequestSaveWorkunitList;
        }
    }

    public static class SoapenvBody {

        @XmlElement(name = "end:save")
        private End end;

        public End getEnd() {
            return end;
        }
    }
}
