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

package org.bfabric.webservice.response;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.util.ClassHelper;
import org.bfabric.xml.entity.XMLAccess;
import org.bfabric.xml.entity.XMLAnnotation;
import org.bfabric.xml.entity.XMLApplication;
import org.bfabric.xml.entity.XMLApplicationCategory;
import org.bfabric.xml.entity.XMLApplicationTestLog;
import org.bfabric.xml.entity.XMLAttachment;
import org.bfabric.xml.entity.XMLBooking;
import org.bfabric.xml.entity.XMLCharge;
import org.bfabric.xml.entity.XMLComment;
import org.bfabric.xml.entity.XMLCommentTemplate;
import org.bfabric.xml.entity.XMLCompany;
import org.bfabric.xml.entity.XMLConsumable;
import org.bfabric.xml.entity.XMLContainer;
import org.bfabric.xml.entity.XMLContainerStatus;
import org.bfabric.xml.entity.XMLCustomContainerStatus;
import org.bfabric.xml.entity.XMLDataset;
import org.bfabric.xml.entity.XMLDatasetTemplate;
import org.bfabric.xml.entity.XMLDepartment;
import org.bfabric.xml.entity.XMLDivision;
import org.bfabric.xml.entity.XMLExecutable;
import org.bfabric.xml.entity.XMLExternalJob;
import org.bfabric.xml.entity.XMLImportResource;
import org.bfabric.xml.entity.XMLInstitute;
import org.bfabric.xml.entity.XMLInstrument;
import org.bfabric.xml.entity.XMLInstrumentEvent;
import org.bfabric.xml.entity.XMLInstrumentEventType;
import org.bfabric.xml.entity.XMLInstrumentReservation;
import org.bfabric.xml.entity.XMLJob;
import org.bfabric.xml.entity.XMLLink;
import org.bfabric.xml.entity.XMLMail;
import org.bfabric.xml.entity.XMLMultiplexId;
import org.bfabric.xml.entity.XMLMultiplexKit;
import org.bfabric.xml.entity.XMLOffer;
import org.bfabric.xml.entity.XMLOfferedCharge;
import org.bfabric.xml.entity.XMLOption;
import org.bfabric.xml.entity.XMLOptionValue;
import org.bfabric.xml.entity.XMLOrder;
import org.bfabric.xml.entity.XMLOrderItem;
import org.bfabric.xml.entity.XMLOrganization;
import org.bfabric.xml.entity.XMLOrganizationType;
import org.bfabric.xml.entity.XMLParameter;
import org.bfabric.xml.entity.XMLPlate;
import org.bfabric.xml.entity.XMLProject;
import org.bfabric.xml.entity.XMLResource;
import org.bfabric.xml.entity.XMLResourceContent;
import org.bfabric.xml.entity.XMLRole;
import org.bfabric.xml.entity.XMLRun;
import org.bfabric.xml.entity.XMLRunSample;
import org.bfabric.xml.entity.XMLRunUnit;
import org.bfabric.xml.entity.XMLRunUnitLane;
import org.bfabric.xml.entity.XMLSample;
import org.bfabric.xml.entity.XMLSamplePreparationProtocol;
import org.bfabric.xml.entity.XMLService;
import org.bfabric.xml.entity.XMLServiceArea;
import org.bfabric.xml.entity.XMLServiceType;
import org.bfabric.xml.entity.XMLStandardContainerStatus;
import org.bfabric.xml.entity.XMLStorage;
import org.bfabric.xml.entity.XMLUser;
import org.bfabric.xml.entity.XMLUserGroup;
import org.bfabric.xml.entity.XMLWorkflow;
import org.bfabric.xml.entity.XMLWorkflowStep;
import org.bfabric.xml.entity.XMLWorkflowTemplate;
import org.bfabric.xml.entity.XMLWorkflowTemplateStep;
import org.bfabric.xml.entity.XMLWorkunit;

@XmlRootElement
public class XMLResponse extends AbstractXMLResponse {

    private static final Logger logger = Logger.getLogger(XMLResponse.class.getName());

    private static final Map<Type, Field> classMethodMap = new HashMap<>();

    static {
        for (Field field : XMLResponse.class.getDeclaredFields()) {
            if (field.getType().equals(List.class)) {
                Type genericType = ClassHelper.getRuntimeClass(field.getGenericType(), 0);
                classMethodMap.put(genericType, field);
            }
        }
    }

    @XmlElement
    private List<XMLAccess> access = new ArrayList<>();

    @XmlElement
    private List<XMLAnnotation> annotation = new ArrayList<>();

    @XmlElement
    private List<XMLApplication> application = new ArrayList<>();

    @XmlElement
    private List<XMLApplicationCategory> applicationcategory = new ArrayList<>();

    @XmlElement
    private List<XMLApplicationTestLog> applicationtestlog = new ArrayList<>();

    @XmlElement
    private List<XMLAttachment> attachment = new ArrayList<>();

    @XmlElement
    private List<XMLBooking> booking = new ArrayList<>();

    @XmlElement
    private List<XMLCharge> charge = new ArrayList<>();

    @XmlElement
    private List<XMLComment> comment = new ArrayList<>();

    @XmlElement
    private List<XMLCommentTemplate> commenttemplate = new ArrayList<>();

    @XmlElement
    private List<XMLCompany> company = new ArrayList<>();

    @XmlElement
    private List<XMLConsumable> consumable = new ArrayList<>();

    @XmlElement
    private List<XMLContainer> container = new ArrayList<>();

    @XmlElement
    private List<XMLContainerStatus> containerstatus = new ArrayList<>();

    @XmlElement
    private List<XMLCustomContainerStatus> customcontainerstatus = new ArrayList<>();

    @XmlElement
    private List<XMLDataset> dataset = new ArrayList<>();

    @XmlElement
    private List<XMLDatasetTemplate> datasettemplate = new ArrayList<>();

    @XmlElement
    private List<XMLDepartment> department = new ArrayList<>();

    @XmlElement
    private List<XMLDivision> division = new ArrayList<>();

    @XmlElement
    private List<XMLExecutable> executable = new ArrayList<>();

    @XmlElement
    private List<XMLExternalJob> externaljob = new ArrayList<>();

    @XmlElement
    private List<XMLImportResource> importresource = new ArrayList<>();

    @XmlElement
    private List<XMLInstitute> institute = new ArrayList<>();

    @XmlElement
    private List<XMLInstrument> instrument = new ArrayList<>();

    @XmlElement
    private List<XMLInstrumentEvent> instrumentevent = new ArrayList<>();

    @XmlElement
    private List<XMLInstrumentEventType> instrumenteventtype = new ArrayList<>();

    @XmlElement
    private List<XMLInstrumentReservation> instrumentreservation = new ArrayList<>();

    @XmlElement
    private List<XMLJob> job = new ArrayList<>();

    @XmlElement
    private List<XMLLink> link = new ArrayList<>();

    @XmlElement
    private List<XMLMail> mail = new ArrayList<>();

    @XmlElement
    private List<XMLMultiplexId> multiplexid = new ArrayList<>();

    @XmlElement
    private List<XMLMultiplexKit> multiplexkit = new ArrayList<>();

    @XmlElement
    private List<XMLOffer> offer = new ArrayList<>();

    @XmlElement
    private List<XMLOfferedCharge> offeredcharge = new ArrayList<>();

    @XmlElement
    private List<XMLOption> option = new ArrayList<>();

    @XmlElement
    private List<XMLOptionValue> optionvalue = new ArrayList<>();

    @XmlElement
    private List<XMLOrder> order = new ArrayList<>();

    @XmlElement
    private List<XMLOrderItem> orderitem = new ArrayList<>();

    @XmlElement
    private List<XMLOrganization> organization = new ArrayList<>();

    @XmlElement
    private List<XMLOrganizationType> organizationtype = new ArrayList<>();

    @XmlElement
    private List<XMLParameter> parameter = new ArrayList<>();

    @XmlElement
    private List<XMLPlate> plate = new ArrayList<>();

    @XmlElement
    private List<XMLProject> project = new ArrayList<>();

    @XmlElement
    private List<XMLResource> resource = new ArrayList<>();

    @XmlElement
    private List<XMLResourceContent> resourcecontent = new ArrayList<>();

    @XmlElement
    private List<XMLRole> role = new ArrayList<>();

    @XmlElement
    private List<XMLRun> run = new ArrayList<>();

    @XmlElement
    private List<XMLRunSample> runsample = new ArrayList<>();

    @XmlElement
    private List<XMLRunUnit> rununit = new ArrayList<>();

    @XmlElement
    private List<XMLRunUnitLane> rununitlane = new ArrayList<>();

    @XmlElement
    private List<XMLSample> sample = new ArrayList<>();

    @XmlElement
    private List<XMLSamplePreparationProtocol> samplepreparationprotocol = new ArrayList<>();

    @XmlElement
    private List<XMLService> service = new ArrayList<>();

    @XmlElement
    private List<XMLServiceArea> servicearea = new ArrayList<>();

    @XmlElement
    private List<XMLServiceType> servicetype = new ArrayList<>();

    @XmlElement
    private List<XMLStandardContainerStatus> standarcontainerstatus = new ArrayList<>();

    @XmlElement
    private List<XMLStorage> storage = new ArrayList<>();

    @XmlElement
    private List<XMLUser> user = new ArrayList<>();

    @XmlElement
    private List<XMLUserGroup> usergroup = new ArrayList<>();

    @XmlElement
    private List<XMLWorkflow> workflow = new ArrayList<>();

    @XmlElement
    private List<XMLWorkflowStep> workflowstep = new ArrayList<>();

    @XmlElement
    private List<XMLWorkflowTemplate> workflowtemplate = new ArrayList<>();

    @XmlElement
    private List<XMLWorkflowTemplateStep> workflowtemplatestep = new ArrayList<>();

    @XmlElement
    private List<XMLWorkunit> workunit = new ArrayList<>();

    public void add(Object object) {
        Class<?> clazz = object.getClass();
        try {
            List<Object> list = null;
            if (classMethodMap.get(clazz) != null) {
                list = (List<Object>) classMethodMap.get(clazz).get(this);
            } else {
                logger.fine("No list for objects of the following type was declared: " + clazz.getCanonicalName());
            }
            if (list != null) {
                list.add(object);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public List<XMLAccess> getAccess() {
        return access;
    }

    public List<XMLAnnotation> getAnnotation() {
        return annotation;
    }

    public List<XMLApplication> getApplication() {
        return application;
    }

    public List<XMLApplicationCategory> getApplicationcategory() {
        return applicationcategory;
    }

    public List<XMLApplicationTestLog> getApplicationtestlog() {
        return applicationtestlog;
    }

    public List<XMLAttachment> getAttachment() {
        return attachment;
    }

    public List<XMLBooking> getBooking() {
        return booking;
    }

    public List<XMLCharge> getCharge() {
        return charge;
    }

    public List<XMLComment> getComment() {
        return comment;
    }

    public List<XMLCommentTemplate> getCommenttemplate() {
        return commenttemplate;
    }

    public List<XMLCompany> getCompany() {
        return company;
    }

    public List<XMLConsumable> getConsumable() {
        return consumable;
    }

    public List<XMLContainer> getContainer() {
        return container;
    }

    public List<XMLContainerStatus> getContainerstatus() {
        return containerstatus;
    }

    public List<XMLCustomContainerStatus> getCustomcontainerstatus() {
        return customcontainerstatus;
    }

    public List<XMLDataset> getDataset() {
        return dataset;
    }

    public List<XMLDatasetTemplate> getDatasettemplate() {
        return datasettemplate;
    }

    public List<XMLDepartment> getDepartment() {
        return department;
    }

    public List<XMLDivision> getDivision() {
        return division;
    }

    public List<XMLExecutable> getExecutable() {
        return executable;
    }

    public List<XMLExternalJob> getExternaljob() {
        return externaljob;
    }

    public List<XMLImportResource> getImportresource() {
        return importresource;
    }

    public List<XMLInstitute> getInstitute() {
        return institute;
    }

    public List<XMLInstrument> getInstrument() {
        return instrument;
    }

    public List<XMLInstrumentEvent> getInstrumentEvent() {
        return instrumentevent;
    }

    public List<XMLInstrumentEventType> getInstrumentEventType() {
        return instrumenteventtype;
    }

    public List<XMLInstrumentEvent> getInstrumentevent() {
        return instrumentevent;
    }

    public List<XMLInstrumentEventType> getInstrumenteventtype() {
        return instrumenteventtype;
    }

    public List<XMLInstrumentReservation> getInstrumentreservation() {
        return instrumentreservation;
    }

    public List<XMLJob> getJob() {
        return job;
    }

    public List<XMLLink> getLink() {
        return link;
    }

    public List<XMLMail> getMail() {
        return mail;
    }

    public List<XMLMultiplexId> getMultiplexid() {
        return multiplexid;
    }

    public List<XMLMultiplexKit> getMultiplexkit() {
        return multiplexkit;
    }

    public List<XMLOffer> getOffer() {
        return offer;
    }

    public List<XMLOfferedCharge> getOfferedcharge() {
        return offeredcharge;
    }

    public List<XMLOption> getOption() {
        return option;
    }

    public List<XMLOptionValue> getOptionValue() {
        return optionvalue;
    }

    public List<XMLOrder> getOrder() {
        return order;
    }

    public List<XMLOrderItem> getOrderitem() {
        return orderitem;
    }

    public List<XMLOrganization> getOrganization() {
        return organization;
    }

    public List<XMLOrganizationType> getOrganizationtype() {
        return organizationtype;
    }

    public List<XMLParameter> getParameter() {
        return parameter;
    }

    public List<XMLPlate> getPlate() {
        return plate;
    }

    public List<XMLProject> getProject() {
        return project;
    }

    public List<XMLResource> getResource() {
        return resource;
    }

    public List<XMLResourceContent> getResourcecontent() {
        return resourcecontent;
    }

    public List<XMLRole> getRole() {
        return role;
    }

    public List<XMLRun> getRun() {
        return run;
    }

    public List<XMLRunSample> getRunsample() {
        return runsample;
    }

    public List<XMLRunUnit> getRununit() {
        return rununit;
    }

    public List<XMLRunUnitLane> getRununitlane() {
        return rununitlane;
    }

    public List<XMLSample> getSample() {
        return sample;
    }

    public List<XMLSamplePreparationProtocol> getSamplepreparationprotocol() {
        return samplepreparationprotocol;
    }

    public List<XMLService> getService() {
        return service;
    }

    public List<XMLServiceArea> getServicearea() {
        return servicearea;
    }

    public List<XMLServiceType> getServicetype() {
        return servicetype;
    }

    public List<XMLStandardContainerStatus> getStandarcontainerstatus() {
        return standarcontainerstatus;
    }

    public List<XMLStorage> getStorage() {
        return storage;
    }

    public List<XMLUser> getUser() {
        return user;
    }

    public List<XMLUserGroup> getUsergroup() {
        return usergroup;
    }

    public List<XMLWorkflow> getWorkflow() {
        return workflow;
    }

    public List<XMLWorkflowStep> getWorkflowstep() {
        return workflowstep;
    }

    public List<XMLWorkflowTemplate> getWorkflowtemplate() {
        return workflowtemplate;
    }

    public List<XMLWorkflowTemplateStep> getWorkflowtemplatestep() {
        return workflowtemplatestep;
    }

    public List<XMLWorkunit> getWorkunit() {
        return workunit;
    }

    public void setAccess(List<XMLAccess> access) {
        this.access = access;
    }

    public void setAnnotation(List<XMLAnnotation> annotation) {
        this.annotation = annotation;
    }

    public void setApplication(List<XMLApplication> application) {
        this.application = application;
    }

    public void setApplicationcategory(List<XMLApplicationCategory> applicationcategory) {
        this.applicationcategory = applicationcategory;
    }

    public void setApplicationtestlog(List<XMLApplicationTestLog> applicationtestlog) {
        this.applicationtestlog = applicationtestlog;
    }

    public void setAttachment(List<XMLAttachment> attachment) {
        this.attachment = attachment;
    }

    public void setBooking(List<XMLBooking> booking) {
        this.booking = booking;
    }

    public void setCharge(List<XMLCharge> charge) {
        this.charge = charge;
    }

    public void setComment(List<XMLComment> comment) {
        this.comment = comment;
    }

    public void setCommenttemplate(List<XMLCommentTemplate> commenttemplate) {
        this.commenttemplate = commenttemplate;
    }

    public void setCompany(List<XMLCompany> company) {
        this.company = company;
    }

    public void setConsumable(List<XMLConsumable> consumable) {
        this.consumable = consumable;
    }

    public void setContainer(List<XMLContainer> container) {
        this.container = container;
    }

    public void setContainerstatus(List<XMLContainerStatus> containerstatus) {
        this.containerstatus = containerstatus;
    }

    public void setCustomcontainerstatus(List<XMLCustomContainerStatus> customcontainerstatus) {
        this.customcontainerstatus = customcontainerstatus;
    }

    public void setDataset(List<XMLDataset> dataset) {
        this.dataset = dataset;
    }

    public void setDatasettemplate(List<XMLDatasetTemplate> datasettemplate) {
        this.datasettemplate = datasettemplate;
    }

    public void setDepartment(List<XMLDepartment> department) {
        this.department = department;
    }

    public void setDivision(List<XMLDivision> division) {
        this.division = division;
    }

    public void setExecutable(List<XMLExecutable> executable) {
        this.executable = executable;
    }

    public void setExternaljob(List<XMLExternalJob> externaljob) {
        this.externaljob = externaljob;
    }

    public void setImportresource(List<XMLImportResource> importresource) {
        this.importresource = importresource;
    }

    public void setInstitute(List<XMLInstitute> institute) {
        this.institute = institute;
    }

    public void setInstrument(List<XMLInstrument> instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentEvent(List<XMLInstrumentEvent> instrumentevent) {
        this.instrumentevent = instrumentevent;
    }

    public void setInstrumentEventType(List<XMLInstrumentEventType> instrumenteventtype) {
        this.instrumenteventtype = instrumenteventtype;
    }

    public void setInstrumentevent(List<XMLInstrumentEvent> instrumentevent) {
        this.instrumentevent = instrumentevent;
    }

    public void setInstrumenteventtype(List<XMLInstrumentEventType> instrumenteventtype) {
        this.instrumenteventtype = instrumenteventtype;
    }

    public void setInstrumentreservation(List<XMLInstrumentReservation> instrumentreservation) {
        this.instrumentreservation = instrumentreservation;
    }

    public void setJob(List<XMLJob> job) {
        this.job = job;
    }

    public void setLink(List<XMLLink> link) {
        this.link = link;
    }

    public void setMail(List<XMLMail> mail) {
        this.mail = mail;
    }

    public void setMultiplexid(List<XMLMultiplexId> multiplexid) {
        this.multiplexid = multiplexid;
    }

    public void setMultiplexkit(List<XMLMultiplexKit> multiplexkit) {
        this.multiplexkit = multiplexkit;
    }

    public void setOffer(List<XMLOffer> offer) {
        this.offer = offer;
    }

    public void setOfferedcharge(List<XMLOfferedCharge> offeredcharge) {
        this.offeredcharge = offeredcharge;
    }

    public void setOption(List<XMLOption> option) {
        this.option = option;
    }

    public void setOptionvalue(List<XMLOptionValue> optionvalue) {
        this.optionvalue = optionvalue;
    }

    public void setOrder(List<XMLOrder> order) {
        this.order = order;
    }

    public void setOrderitem(List<XMLOrderItem> orderitem) {
        this.orderitem = orderitem;
    }

    public void setOrganization(List<XMLOrganization> organization) {
        this.organization = organization;
    }

    public void setOrganizationtype(List<XMLOrganizationType> organizationtype) {
        this.organizationtype = organizationtype;
    }

    public void setParameter(List<XMLParameter> parameter) {
        this.parameter = parameter;
    }

    public void setPlate(List<XMLPlate> plate) {
        this.plate = plate;
    }

    public void setProject(List<XMLProject> project) {
        this.project = project;
    }

    public void setResource(List<XMLResource> resource) {
        this.resource = resource;
    }

    public void setResourcecontent(List<XMLResourceContent> resourcecontent) {
        this.resourcecontent = resourcecontent;
    }

    public void setRole(List<XMLRole> role) {
        this.role = role;
    }

    public void setRun(List<XMLRun> run) {
        this.run = run;
    }

    public void setRunsample(List<XMLRunSample> runsample) {
        this.runsample = runsample;
    }

    public void setRununit(List<XMLRunUnit> rununit) {
        this.rununit = rununit;
    }

    public void setRununitlane(List<XMLRunUnitLane> rununitlane) {
        this.rununitlane = rununitlane;
    }

    public void setSample(List<XMLSample> sample) {
        this.sample = sample;
    }

    public void setSamplepreparationprotocol(List<XMLSamplePreparationProtocol> samplepreparationprotocol) {
        this.samplepreparationprotocol = samplepreparationprotocol;
    }

    public void setService(List<XMLService> service) {
        this.service = service;
    }

    public void setServicearea(List<XMLServiceArea> servicearea) {
        this.servicearea = servicearea;
    }

    public void setServicetype(List<XMLServiceType> servicetype) {
        this.servicetype = servicetype;
    }

    public void setStandarcontainerstatus(List<XMLStandardContainerStatus> standarcontainerstatus) {
        this.standarcontainerstatus = standarcontainerstatus;
    }

    public void setStorage(List<XMLStorage> storage) {
        this.storage = storage;
    }

    public void setUser(List<XMLUser> user) {
        this.user = user;
    }

    public void setUsergroup(List<XMLUserGroup> usergroup) {
        this.usergroup = usergroup;
    }

    public void setWorkflow(List<XMLWorkflow> workflow) {
        this.workflow = workflow;
    }

    public void setWorkflowstep(List<XMLWorkflowStep> workflowstep) {
        this.workflowstep = workflowstep;
    }

    public void setWorkflowtemplate(List<XMLWorkflowTemplate> workflowtemplate) {
        this.workflowtemplate = workflowtemplate;
    }

    public void setWorkflowtemplatestep(List<XMLWorkflowTemplateStep> workflowtemplatestep) {
        this.workflowtemplatestep = workflowtemplatestep;
    }

    public void setWorkunit(List<XMLWorkunit> workunit) {
        this.workunit = workunit;
    }
}