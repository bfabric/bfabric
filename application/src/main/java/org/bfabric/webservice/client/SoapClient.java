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

package org.bfabric.webservice.client;

import java.lang.reflect.Constructor;

import org.bfabric.util.UriHelper;
import org.bfabric.webservice.client.endpoint.EPAccess;
import org.bfabric.webservice.client.endpoint.EPAnnotation;
import org.bfabric.webservice.client.endpoint.EPApplication;
import org.bfabric.webservice.client.endpoint.EPApplicationCategory;
import org.bfabric.webservice.client.endpoint.EPApplicationTestLog;
import org.bfabric.webservice.client.endpoint.EPAttachment;
import org.bfabric.webservice.client.endpoint.EPBooking;
import org.bfabric.webservice.client.endpoint.EPCharge;
import org.bfabric.webservice.client.endpoint.EPComment;
import org.bfabric.webservice.client.endpoint.EPCommentTemplate;
import org.bfabric.webservice.client.endpoint.EPCompany;
import org.bfabric.webservice.client.endpoint.EPConsumable;
import org.bfabric.webservice.client.endpoint.EPContainerStatus;
import org.bfabric.webservice.client.endpoint.EPCustomContainerStatus;
import org.bfabric.webservice.client.endpoint.EPDataset;
import org.bfabric.webservice.client.endpoint.EPDatasetTemplate;
import org.bfabric.webservice.client.endpoint.EPDepartment;
import org.bfabric.webservice.client.endpoint.EPDivision;
import org.bfabric.webservice.client.endpoint.EPExecutable;
import org.bfabric.webservice.client.endpoint.EPExternalJob;
import org.bfabric.webservice.client.endpoint.EPImportResource;
import org.bfabric.webservice.client.endpoint.EPInstitute;
import org.bfabric.webservice.client.endpoint.EPInstrument;
import org.bfabric.webservice.client.endpoint.EPInstrumentEvent;
import org.bfabric.webservice.client.endpoint.EPInstrumentEventType;
import org.bfabric.webservice.client.endpoint.EPInstrumentReservation;
import org.bfabric.webservice.client.endpoint.EPJob;
import org.bfabric.webservice.client.endpoint.EPLink;
import org.bfabric.webservice.client.endpoint.EPMail;
import org.bfabric.webservice.client.endpoint.EPMultiplexId;
import org.bfabric.webservice.client.endpoint.EPMultiplexKit;
import org.bfabric.webservice.client.endpoint.EPOffer;
import org.bfabric.webservice.client.endpoint.EPOfferedCharge;
import org.bfabric.webservice.client.endpoint.EPOption;
import org.bfabric.webservice.client.endpoint.EPOptionValue;
import org.bfabric.webservice.client.endpoint.EPOrder;
import org.bfabric.webservice.client.endpoint.EPOrganization;
import org.bfabric.webservice.client.endpoint.EPOrganizationType;
import org.bfabric.webservice.client.endpoint.EPParameter;
import org.bfabric.webservice.client.endpoint.EPPlate;
import org.bfabric.webservice.client.endpoint.EPProject;
import org.bfabric.webservice.client.endpoint.EPResource;
import org.bfabric.webservice.client.endpoint.EPRole;
import org.bfabric.webservice.client.endpoint.EPRun;
import org.bfabric.webservice.client.endpoint.EPRunUnit;
import org.bfabric.webservice.client.endpoint.EPRunUnitLane;
import org.bfabric.webservice.client.endpoint.EPSample;
import org.bfabric.webservice.client.endpoint.EPSamplePreparationProtocol;
import org.bfabric.webservice.client.endpoint.EPStandardContainerStatus;
import org.bfabric.webservice.client.endpoint.EPStorage;
import org.bfabric.webservice.client.endpoint.EPUser;
import org.bfabric.webservice.client.endpoint.EPUserGroup;
import org.bfabric.webservice.client.endpoint.EPWorkflow;
import org.bfabric.webservice.client.endpoint.EPWorkflowStep;
import org.bfabric.webservice.client.endpoint.EPWorkflowTemplate;
import org.bfabric.webservice.client.endpoint.EPWorkflowTemplateStep;
import org.bfabric.webservice.client.endpoint.EPWorkunit;

public class SoapClient {

    private final String hostname;

    private final String login;

    private final String password;

    private final int port;

    private volatile EPAccess epAccess;

    private volatile EPAnnotation epAnnotation;

    private volatile EPApplication epApplication;

    private volatile EPApplicationCategory epApplicationCategory;

    private volatile EPApplicationTestLog epApplicationTestLog;

    private volatile EPAttachment epAttachment;

    private volatile EPBooking epBooking;

    private volatile EPCharge epCharge;

    private volatile EPComment epComment;

    private volatile EPCommentTemplate epCommentTemplate;

    private volatile EPCompany epCompany;

    private volatile EPConsumable epConsumable;

    private volatile EPContainerStatus epContainerStatus;

    private volatile EPCustomContainerStatus epCustomContainerStatus;

    private volatile EPDataset epDataset;

    private volatile EPDatasetTemplate epDatasetTemplate;

    private volatile EPDepartment epDepartment;

    private volatile EPDivision epDivision;

    private volatile EPExecutable epExecutable;

    private volatile EPExternalJob epExternalJob;

    private volatile EPImportResource epImportResource;

    private volatile EPInstitute epInstitute;

    private volatile EPInstrument epInstrument;

    private volatile EPInstrumentEvent epInstrumentEvent;

    private volatile EPInstrumentEventType epInstrumentEventType;

    private volatile EPInstrumentReservation epInstrumentReservation;

    private volatile EPJob epJob;

    private volatile EPLink epLink;

    private volatile EPMail epMail;

    private volatile EPMultiplexId epMultiplexId;

    private volatile EPMultiplexKit epMultiplexKit;

    private volatile EPOffer epOffer;

    private volatile EPOfferedCharge epOfferedCharge;

    private volatile EPOption epOption;

    private volatile EPOptionValue epOptionValue;

    private volatile EPOrder epOrder;

    private volatile EPOrganization epOrganization;

    private volatile EPOrganizationType epOrganizationType;

    private volatile EPParameter epParameter;

    private volatile EPPlate epPlate;

    private volatile EPProject epProject;

    private volatile EPResource epResource;

    private volatile EPRole epRole;

    private volatile EPRun epRun;

    private volatile EPRunUnit epRunUnit;

    private volatile EPRunUnitLane epRunUnitLane;

    private volatile EPSample epSample;

    private volatile EPSamplePreparationProtocol epSamplePreparationProtocol;

    private volatile EPStandardContainerStatus epStandardContainerStatus;

    private volatile EPStorage epStorage;

    private volatile EPUser epUser;

    private volatile EPUserGroup epUserGroup;

    private volatile EPWorkflow epWorkflow;

    private volatile EPWorkflowStep epWorkflowStep;

    private volatile EPWorkflowTemplate epWorkflowTemplate;

    private volatile EPWorkflowTemplateStep epWorkflowTemplateStep;

    private volatile EPWorkunit epWorkunit;

    public SoapClient(String hostname, int port, String login, String password) {
        this.hostname = UriHelper.removeProtocol(hostname);
        this.port = port;
        this.login = login;
        this.password = password;
    }

    private synchronized <EP> EP createEndPoint(Class<EP> epClass) {
        try {
            Constructor<EP> ctor = epClass.getConstructor(SoapClient.class);
            return ctor.newInstance(this);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to create endpoint object of type " + epClass.getCanonicalName() + ": " + exception.getLocalizedMessage());
        }
    }

    public EPAccess getEpAccess() {
        if (epAccess == null) {
            synchronized (SoapClient.class) {
                if (epAccess == null) {
                    epAccess = createEndPoint(EPAccess.class);
                }
            }
        }
        return epAccess;
    }

    public EPAnnotation getEpAnnotation() {
        if (epAnnotation == null) {
            synchronized (SoapClient.class) {
                if (epAnnotation == null) {
                    epAnnotation = createEndPoint(EPAnnotation.class);
                }
            }
        }
        return epAnnotation;
    }

    public EPApplication getEpApplication() {
        if (epApplication == null) {
            synchronized (SoapClient.class) {
                if (epApplication == null) {
                    epApplication = createEndPoint(EPApplication.class);
                }
            }
        }
        return epApplication;
    }

    public EPApplicationCategory getEpApplicationCategory() {
        if (epApplicationCategory == null) {
            synchronized (SoapClient.class) {
                if (epApplicationCategory == null) {
                    epApplicationCategory = createEndPoint(EPApplicationCategory.class);
                }
            }
        }
        return epApplicationCategory;
    }

    public EPApplicationTestLog getEpApplicationTestLog() {
        if (epApplicationTestLog == null) {
            synchronized (SoapClient.class) {
                if (epApplicationTestLog == null) {
                    epApplicationTestLog = createEndPoint(EPApplicationTestLog.class);
                }
            }
        }
        return epApplicationTestLog;
    }

    public EPAttachment getEpAttachment() {
        if (epAttachment == null) {
            synchronized (SoapClient.class) {
                if (epAttachment == null) {
                    epAttachment = createEndPoint(EPAttachment.class);
                }
            }
        }
        return epAttachment;
    }

    public EPBooking getEpBooking() {
        if (epBooking == null) {
            synchronized (SoapClient.class) {
                if (epBooking == null) {
                    epBooking = createEndPoint(EPBooking.class);
                }
            }
        }
        return epBooking;
    }

    public EPCharge getEpCharge() {
        if (epCharge == null) {
            synchronized (SoapClient.class) {
                if (epCharge == null) {
                    epCharge = createEndPoint(EPCharge.class);
                }
            }
        }
        return epCharge;
    }

    public EPComment getEpComment() {
        if (epComment == null) {
            synchronized (SoapClient.class) {
                if (epComment == null) {
                    epComment = createEndPoint(EPComment.class);
                }
            }
        }
        return epComment;
    }

    public EPCommentTemplate getEpCommentTemplate() {
        if (epCommentTemplate == null) {
            synchronized (SoapClient.class) {
                if (epCommentTemplate == null) {
                    epCommentTemplate = createEndPoint(EPCommentTemplate.class);
                }
            }
        }
        return epCommentTemplate;
    }

    public EPCompany getEpCompany() {
        if (epCompany == null) {
            synchronized (SoapClient.class) {
                if (epCompany == null) {
                    epCompany = createEndPoint(EPCompany.class);
                }
            }
        }
        return epCompany;
    }

    public EPConsumable getEpConsumable() {
        if (epConsumable == null) {
            synchronized (SoapClient.class) {
                if (epConsumable == null) {
                    epConsumable = createEndPoint(EPConsumable.class);
                }
            }
        }
        return epConsumable;
    }

    public EPContainerStatus getEpContainerStatus() {
        if (epContainerStatus == null) {
            synchronized (SoapClient.class) {
                if (epContainerStatus == null) {
                    epContainerStatus = createEndPoint(EPContainerStatus.class);
                }
            }
        }
        return epContainerStatus;
    }

    public EPCustomContainerStatus getEpCustomContainerStatus() {
        if (epCustomContainerStatus == null) {
            synchronized (SoapClient.class) {
                if (epCustomContainerStatus == null) {
                    epCustomContainerStatus = createEndPoint(EPCustomContainerStatus.class);
                }
            }
        }
        return epCustomContainerStatus;
    }

    public EPDataset getEpDataset() {
        if (epDataset == null) {
            synchronized (SoapClient.class) {
                if (epDataset == null) {
                    epDataset = createEndPoint(EPDataset.class);
                }
            }
        }
        return epDataset;
    }

    public EPDatasetTemplate getEpDatasetTemplate() {
        if (epDatasetTemplate == null) {
            synchronized (SoapClient.class) {
                if (epDatasetTemplate == null) {
                    epDatasetTemplate = createEndPoint(EPDatasetTemplate.class);
                }
            }
        }
        return epDatasetTemplate;
    }

    public EPDepartment getEpDepartment() {
        if (epDepartment == null) {
            synchronized (SoapClient.class) {
                if (epDepartment == null) {
                    epDepartment = createEndPoint(EPDepartment.class);
                }
            }
        }
        return epDepartment;
    }

    public EPDivision getEpDivision() {
        if (epDivision == null) {
            synchronized (SoapClient.class) {
                if (epDivision == null) {
                    epDivision = createEndPoint(EPDivision.class);
                }
            }
        }
        return epDivision;
    }

    public EPExecutable getEpExecutable() {
        if (epExecutable == null) {
            synchronized (SoapClient.class) {
                if (epExecutable == null) {
                    epExecutable = createEndPoint(EPExecutable.class);
                }
            }
        }
        return epExecutable;
    }

    public EPExternalJob getEpExternalJob() {
        if (epExternalJob == null) {
            synchronized (SoapClient.class) {
                if (epExternalJob == null) {
                    epExternalJob = createEndPoint(EPExternalJob.class);
                }
            }
        }
        return epExternalJob;
    }

    public EPImportResource getEpImportResource() {
        if (epImportResource == null) {
            synchronized (SoapClient.class) {
                if (epImportResource == null) {
                    epImportResource = createEndPoint(EPImportResource.class);
                }
            }
        }
        return epImportResource;
    }

    public EPInstitute getEpInstitute() {
        if (epInstitute == null) {
            synchronized (SoapClient.class) {
                if (epInstitute == null) {
                    epInstitute = createEndPoint(EPInstitute.class);
                }
            }
        }
        return epInstitute;
    }

    public EPInstrument getEpInstrument() {
        if (epInstrument == null) {
            synchronized (SoapClient.class) {
                if (epInstrument == null) {
                    epInstrument = createEndPoint(EPInstrument.class);
                }
            }
        }
        return epInstrument;
    }

    public EPInstrumentEvent getEpInstrumentEvent() {
        if (epInstrumentEvent == null) {
            synchronized (SoapClient.class) {
                if (epInstrumentEvent == null) {
                    epInstrumentEvent = createEndPoint(EPInstrumentEvent.class);
                }
            }
        }
        return epInstrumentEvent;
    }

    public EPInstrumentEventType getEpInstrumentEventType() {
        if (epInstrumentEventType == null) {
            synchronized (SoapClient.class) {
                if (epInstrumentEventType == null) {
                    epInstrumentEventType = createEndPoint(EPInstrumentEventType.class);
                }
            }
        }
        return epInstrumentEventType;
    }

    public EPInstrumentReservation getEpInstrumentReservation() {
        if (epInstrumentReservation == null) {
            synchronized (SoapClient.class) {
                if (epInstrumentReservation == null) {
                    epInstrumentReservation = createEndPoint(EPInstrumentReservation.class);
                }
            }
        }
        return epInstrumentReservation;
    }

    public EPJob getEpJob() {
        if (epJob == null) {
            synchronized (SoapClient.class) {
                if (epJob == null) {
                    epJob = createEndPoint(EPJob.class);
                }
            }
        }
        return epJob;
    }

    public EPLink getEpLink() {
        if (epLink == null) {
            synchronized (SoapClient.class) {
                if (epLink == null) {
                    epLink = createEndPoint(EPLink.class);
                }
            }
        }
        return epLink;
    }

    public EPMail getEpMail() {
        if (epMail == null) {
            synchronized (SoapClient.class) {
                if (epMail == null) {
                    epMail = createEndPoint(EPMail.class);
                }
            }
        }
        return epMail;
    }

    public EPMultiplexId getEpMultiplexId() {
        if (epMultiplexId == null) {
            synchronized (SoapClient.class) {
                if (epMultiplexId == null) {
                    epMultiplexId = createEndPoint(EPMultiplexId.class);
                }
            }
        }
        return epMultiplexId;
    }

    public EPMultiplexKit getEpMultiplexKit() {
        if (epMultiplexKit == null) {
            synchronized (SoapClient.class) {
                if (epMultiplexKit == null) {
                    epMultiplexKit = createEndPoint(EPMultiplexKit.class);
                }
            }
        }
        return epMultiplexKit;
    }

    public EPOffer getEpOffer() {
        if (epOffer == null) {
            synchronized (SoapClient.class) {
                if (epOffer == null) {
                    epOffer = createEndPoint(EPOffer.class);
                }
            }
        }
        return epOffer;
    }

    public EPOfferedCharge getEpOfferedCharge() {
        if (epOfferedCharge == null) {
            synchronized (SoapClient.class) {
                if (epOfferedCharge == null) {
                    epOfferedCharge = createEndPoint(EPOfferedCharge.class);
                }
            }
        }
        return epOfferedCharge;
    }

    public EPOption getEpOption() {
        if (epOption == null) {
            synchronized (SoapClient.class) {
                if (epOption == null) {
                    epOption = createEndPoint(EPOption.class);
                }
            }
        }
        return epOption;
    }

    public EPOptionValue getEpOptionValue() {
        if (epOptionValue == null) {
            synchronized (SoapClient.class) {
                if (epOptionValue == null) {
                    epOptionValue = createEndPoint(EPOptionValue.class);
                }
            }
        }
        return epOptionValue;
    }

    public EPOrder getEpOrder() {
        if (epOrder == null) {
            synchronized (SoapClient.class) {
                if (epOrder == null) {
                    epOrder = createEndPoint(EPOrder.class);
                }
            }
        }
        return epOrder;
    }

    public EPOrganization getEpOrganization() {
        if (epOrganization == null) {
            synchronized (SoapClient.class) {
                if (epOrganization == null) {
                    epOrganization = createEndPoint(EPOrganization.class);
                }
            }
        }
        return epOrganization;
    }

    public EPOrganizationType getEpOrganizationType() {
        if (epOrganizationType == null) {
            synchronized (SoapClient.class) {
                if (epOrganizationType == null) {
                    epOrganizationType = createEndPoint(EPOrganizationType.class);
                }
            }
        }
        return epOrganizationType;
    }

    public EPParameter getEpParameter() {
        if (epParameter == null) {
            synchronized (SoapClient.class) {
                if (epParameter == null) {
                    epParameter = createEndPoint(EPParameter.class);
                }
            }
        }
        return epParameter;
    }

    public EPPlate getEpPlate() {
        if (epPlate == null) {
            synchronized (SoapClient.class) {
                if (epPlate == null) {
                    epPlate = createEndPoint(EPPlate.class);
                }
            }
        }
        return epPlate;
    }

    public EPProject getEpProject() {
        if (epProject == null) {
            synchronized (SoapClient.class) {
                if (epProject == null) {
                    epProject = createEndPoint(EPProject.class);
                }
            }
        }
        return epProject;
    }

    public EPResource getEpResource() {
        if (epResource == null) {
            synchronized (SoapClient.class) {
                if (epResource == null) {
                    epResource = createEndPoint(EPResource.class);
                }
            }
        }
        return epResource;
    }

    public EPRole getEpRole() {
        if (epRole == null) {
            synchronized (SoapClient.class) {
                if (epRole == null) {
                    epRole = createEndPoint(EPRole.class);
                }
            }
        }
        return epRole;
    }

    public EPRun getEpRun() {
        if (epRun == null) {
            synchronized (SoapClient.class) {
                if (epRun == null) {
                    epRun = createEndPoint(EPRun.class);
                }
            }
        }
        return epRun;
    }

    public EPRunUnit getEpRunUnit() {
        if (epRunUnit == null) {
            synchronized (SoapClient.class) {
                if (epRunUnit == null) {
                    epRunUnit = createEndPoint(EPRunUnit.class);
                }
            }
        }
        return epRunUnit;
    }

    public EPRunUnitLane getEpRunUnitLane() {
        if (epRunUnitLane == null) {
            synchronized (SoapClient.class) {
                if (epRunUnitLane == null) {
                    epRunUnitLane = createEndPoint(EPRunUnitLane.class);
                }
            }
        }
        return epRunUnitLane;
    }

    public EPSample getEpSample() {
        if (epSample == null) {
            synchronized (SoapClient.class) {
                if (epSample == null) {
                    epSample = createEndPoint(EPSample.class);
                }
            }
        }
        return epSample;
    }

    public EPSamplePreparationProtocol getEpSamplePreparationProtocol() {
        if (epSamplePreparationProtocol == null) {
            synchronized (SoapClient.class) {
                if (epSamplePreparationProtocol == null) {
                    epSamplePreparationProtocol = createEndPoint(EPSamplePreparationProtocol.class);
                }
            }
        }
        return epSamplePreparationProtocol;
    }

    public EPStandardContainerStatus getEpStandardContainerStatus() {
        if (epStandardContainerStatus == null) {
            synchronized (SoapClient.class) {
                if (epStandardContainerStatus == null) {
                    epStandardContainerStatus = createEndPoint(EPStandardContainerStatus.class);
                }
            }
        }
        return epStandardContainerStatus;
    }

    public EPStorage getEpStorage() {
        if (epStorage == null) {
            synchronized (SoapClient.class) {
                if (epStorage == null) {
                    epStorage = createEndPoint(EPStorage.class);
                }
            }
        }
        return epStorage;
    }

    public EPUser getEpUser() {
        if (epUser == null) {
            synchronized (SoapClient.class) {
                if (epUser == null) {
                    epUser = createEndPoint(EPUser.class);
                }
            }
        }
        return epUser;
    }

    public EPUserGroup getEpUserGroup() {
        if (epUserGroup == null) {
            synchronized (SoapClient.class) {
                if (epUserGroup == null) {
                    epUserGroup = createEndPoint(EPUserGroup.class);
                }
            }
        }
        return epUserGroup;
    }

    public EPWorkflow getEpWorkflow() {
        if (epWorkflow == null) {
            synchronized (SoapClient.class) {
                if (epWorkflow == null) {
                    epWorkflow = createEndPoint(EPWorkflow.class);
                }
            }
        }
        return epWorkflow;
    }

    public EPWorkflowStep getEpWorkflowStep() {
        if (epWorkflowStep == null) {
            synchronized (SoapClient.class) {
                if (epWorkflowStep == null) {
                    epWorkflowStep = createEndPoint(EPWorkflowStep.class);
                }
            }
        }
        return epWorkflowStep;
    }

    public EPWorkflowTemplate getEpWorkflowTemplate() {
        if (epWorkflowTemplate == null) {
            synchronized (SoapClient.class) {
                if (epWorkflowTemplate == null) {
                    epWorkflowTemplate = createEndPoint(EPWorkflowTemplate.class);
                }
            }
        }
        return epWorkflowTemplate;
    }

    public EPWorkflowTemplateStep getEpWorkflowTemplateStep() {
        if (epWorkflowTemplateStep == null) {
            synchronized (SoapClient.class) {
                if (epWorkflowTemplateStep == null) {
                    epWorkflowTemplateStep = createEndPoint(EPWorkflowTemplateStep.class);
                }
            }
        }
        return epWorkflowTemplateStep;
    }

    public EPWorkunit getEpWorkunit() {
        if (epWorkunit == null) {
            synchronized (SoapClient.class) {
                if (epWorkunit == null) {
                    epWorkunit = createEndPoint(EPWorkunit.class);
                }
            }
        }
        return epWorkunit;
    }

    public String getHostname() {
        return hostname;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }
}
