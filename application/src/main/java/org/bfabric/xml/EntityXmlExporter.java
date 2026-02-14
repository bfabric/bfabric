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

package org.bfabric.xml;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.bfabric.Constants;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Application;
import org.bfabric.entity.Attachment;
import org.bfabric.entity.BillingInfo;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Company;
import org.bfabric.entity.Container;
import org.bfabric.entity.ContainerStatus;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetField;
import org.bfabric.entity.DatasetItem;
import org.bfabric.entity.Department;
import org.bfabric.entity.Executable;
import org.bfabric.entity.ExternalJob;
import org.bfabric.entity.Institute;
import org.bfabric.entity.Membership;
import org.bfabric.entity.OptionValue;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderAttribute;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.Parameter;
import org.bfabric.entity.Plate;
import org.bfabric.entity.PlateStatus;
import org.bfabric.entity.Project;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Run;
import org.bfabric.entity.RunStatus;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceOrganizationTypePrice;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Storage;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.list.ChargeList;
import org.bfabric.list.CommentList;
import org.bfabric.list.EntityLogList;
import org.bfabric.list.InstrumentReservationList;
import org.bfabric.list.OrderList;
import org.bfabric.list.ProjectList;
import org.bfabric.list.ResourceList;
import org.bfabric.list.StatisticsList;
import org.bfabric.list.UserGroupList;
import org.bfabric.service.DatasetService;
import org.bfabric.service.MailService;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EntityXmlExporter {

    private final AbstractEntity entity;

    private Document document;

    private Element documentElement;

    public EntityXmlExporter(AbstractEntity entity, String exported) {
        this.entity = entity;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            documentElement = document.createElement(entity.getClassNameLowerCase());
            documentElement.setAttribute("exported", exported);
            document.appendChild(documentElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element addCollectionElement(Collection<?> collection, String label) {
        Element collectionElement = document.createElement(label);
        collectionElement.setAttribute("count", String.valueOf(collection.size()));
        return collectionElement;
    }

    private Element addComments(List<Comment> comments, String type) {
        String elementName = type != null ? type : "comment";
        Element commentList = addCollectionElement(comments, elementName + Constants.PLURAL_S);
        for (Comment comment : comments) {
            Element commentElement = document.createElement(elementName);
            appendChild(commentElement, "id", comment.getId());
            appendChildIfNonEmpty(commentElement, "subject", comment.getSubject());
            appendChildIfNonEmpty(commentElement, "text", comment.getComment());
            appendChild(commentElement, "type", comment.getDiscriminator());
            appendChildIfNonEmpty(commentElement, "replyTo", comment.getReplyTo());
            appendChildIfNonEmpty(commentElement, "parent", comment.getParent());
            appendChild(commentElement, "created", comment.getCreated());
            appendChild(commentElement, "createdBy", comment.getCreatedBy());
            if (comment.getAttachments() != null && !comment.getAttachments().isEmpty()) {
                appendChild(commentElement, "attachments", comment.getAttachments().size());
                for (Attachment attachment : comment.getAttachments()) {
                    Element attachmentElement = document.createElement("attachment");
                    appendChildCommonAttributes(attachmentElement, attachment);
                    appendChild(attachmentElement, "size", attachment.getSize());
                    commentElement.appendChild(attachmentElement);
                }
            }
            commentList.appendChild(commentElement);
        }
        return commentList;
    }

    private Element addContainerStates(List<ContainerStatus> states) {
        Element stateList = addCollectionElement(states, "states");
        for (ContainerStatus state : states) {
            Element stateElement = document.createElement("state");
            appendChildCommonAttributes(stateElement, state);
            appendChildCommonAttributesEnd(stateElement, state);
            stateList.appendChild(stateElement);
        }
        return stateList;
    }

    private Element addCustomAttributes(List<CustomAttribute> attributes) {
        Element attributeList = addCollectionElement(attributes, "customAttributes");
        for (CustomAttribute customAttribute : attributes) {
            Element customAttributeElement = document.createElement("customAttribute");
            appendChild(customAttributeElement, "name", customAttribute.getName());
            appendChild(customAttributeElement, "value", customAttribute.getValue());
            attributeList.appendChild(customAttributeElement);
        }
        return attributeList;
    }

    private void addDatasetContent(Dataset dataset) {
        appendChild(documentElement, "attributes", dataset.getAttributesForXmlExport());
        appendChild(documentElement, "types", dataset.getAttributeTypesForXmlExport());
        Element datasetItemsElement = document.createElement("items");
        preloadDatasetFields(Collections.singleton(dataset));
        for (DatasetItem item : dataset.getItems()) {
            appendChild(datasetItemsElement, "item", item.getFieldsForXmlExport());
        }
        documentElement.appendChild(datasetItemsElement);
    }

    private Element addDatasets(Set<Dataset> datasets, String label) {
        Element datasetList = addCollectionElement(datasets, StringHelper.isNotEmpty(label) ? label + "s" : "datasets");
        preloadDatasetFields(datasets);
        for (Dataset dataset : datasets) {
            Element datasetElement = document.createElement(StringHelper.isNotEmpty(label) ? label : "dataset");
            appendChildCommonAttributes(datasetElement, dataset);
            appendChild(datasetElement, "attributes", dataset.getAttributesForXmlExport());
            appendChild(datasetElement, "types", dataset.getAttributeTypesForXmlExport());
            Element datasetItemsElement = document.createElement("items");
            for (DatasetItem item : dataset.getItems()) {
                appendChild(datasetItemsElement, "item", item.getFieldsForXmlExport());
            }
            datasetElement.appendChild(datasetItemsElement);
            appendChildCommonAttributesEnd(datasetElement, dataset);
            datasetList.appendChild(datasetElement);
        }
        return datasetList;
    }

    private Element addDatasets(Set<Dataset> datasets) {
        return addDatasets(datasets, null);
    }

    private Element addDetails(Container container) {
        Element details = document.createElement("details");
        appendChildCommonAttributes(details, container);
        appendChild(details, "internal", container.isInternal());
        appendChild(details, "status", container.getStatus().getLabel());
        appendChildUserNameIfNonEmpty(details, "coach", container.getCoach());
        appendChildUserNameIfNonEmpty(details, "coachbackup", container.getCoachBackup());
        appendChildUserNameIfNonEmpty(details, "bioinformatician", container.getBioinformatician());
        if (container.getInstitute() != null) {
            appendChildEntityNameIfNonEmpty(details, "organization", container.getInstitute().getDepartment().getOrganization());
            appendChildEntityNameIfNonEmpty(details, "department", container.getInstitute().getDepartment());
            appendChildEntityNameIfNonEmpty(details, "institute", container.getInstitute());
        }
        if (container.getDivision() != null) {
            appendChildEntityNameIfNonEmpty(details, "company", container.getDivision().getCompany());
            if (container.getDivision().isSet()) {
                appendChildEntityNameIfNonEmpty(details, "division", container.getDivision());
            }
        }
        appendChildUserNameIfNonEmpty(details, "budgetofficer", container.getBudgetOfficer());
        appendChildUserNameIfNonEmpty(details, "requester", container.getRequester());
        appendChildUserNameIfNonEmpty(details, "leader", container.getLeader());
        appendChildUserNameIfNonEmpty(details, "contact", container.getContact());
        if (container.isContainerProject()) {
            appendChildProjectDetails(details, (Project) container);
        } else {
            appendChildOrderDetails(details, (Order) container);
        }
        appendChildIfNonEmpty(details, "budgetlimit", container.getBudgetLimit());
        appendChildIfNonEmpty(details, "budgetremaining", container.getBudgetRemaining());
        appendChildCommonAttributesEnd(details, container);
        if (container.getBillingInfo() != null) {
            BillingInfo billingInfo = container.getBillingInfo();
            Element billingInfoElement = document.createElement("billinginfo");
            appendChildIfNonEmpty(billingInfoElement, "billingaddressstreet", billingInfo.getBillingAddressStreet());
            appendChildIfNonEmpty(billingInfoElement, "billingaddresssupplement", billingInfo.getBillingAddressSupplement());
            appendChildIfNonEmpty(billingInfoElement, "billingaddresszip", billingInfo.getBillingAddressZip());
            appendChildIfNonEmpty(billingInfoElement, "billingaddresscity", billingInfo.getBillingAddressCity());
            if (billingInfo.getBillingAddressCountry() != null) {
                appendChild(billingInfoElement, "billingaddresscountry", billingInfo.getBillingAddressCountry().getName());
            }
            appendChildIfNonEmpty(billingInfoElement, "billingcustomertitle", billingInfo.getBillingCustomerTitle());
            appendChildIfNonEmpty(billingInfoElement, "billingcustomerfirstname", billingInfo.getBillingCustomerFirstName());
            appendChildIfNonEmpty(billingInfoElement, "billingcustomerlastname", billingInfo.getBillingCustomerLastName());
            appendChildIfNonEmpty(billingInfoElement, "billingemail", billingInfo.getBillingEmail());
            details.appendChild(billingInfoElement);
        }
        addSummaryContainer(details, container);
        return details;
    }

    private Element addExecutables(Set<Executable> executables) {
        Element executableList = addCollectionElement(executables, "executables");
        for (Executable executable : executables) {
            Element executableElement = document.createElement("executable");
            appendChildCommonAttributes(executableElement, executable);
            appendChild(executableElement, "context", executable.getContext());
            appendChild(executableElement, "supervisor", executable.getSupervisor());
            appendChild(executableElement, "status", executable.getStatus());
            appendChild(executableElement, "enabled", executable.isEnabled());
            appendChild(executableElement, "valid", executable.isValid());
            appendChild(executableElement, "path", executable.getPath());
            appendChild(executableElement, "size", executable.getPrintSize());
            appendChild(executableElement, "description", executable.getDescription());
            if (!executable.getParameters().isEmpty()) {
                executableElement.appendChild(addParameters(executable.getParameters()));
            }
            appendChildCommonAttributesEnd(executableElement, executable);
            executableList.appendChild(executableElement);
        }
        return executableList;
    }

    private Element addExternalJobs(Collection<ExternalJob> externalJobs) {
        Element externalJobList = addCollectionElement(externalJobs, "externalJobs");
        for (ExternalJob externalJob : externalJobs) {
            Element externalJobElement = document.createElement("externalJob");
            appendChild(externalJobElement, "id", externalJob.getId());
            appendChildEntityIfNonEmpty(externalJobElement, "executable", externalJob.getExecutable());
            appendChild(externalJobElement, "context", externalJob.getExecutable().getContext());
            appendChild(externalJobElement, "entityId", externalJob.getClientEntityId());
            appendChild(externalJobElement, "entityClassName", externalJob.getClientEntityClassName());
            appendChild(externalJobElement, "action", externalJob.getAction());
            appendChild(externalJobElement, "status", externalJob.getStatus());
            appendChild(externalJobElement, "log", externalJob.getLog());
            appendChildCommonAttributesEnd(externalJobElement, externalJob);
            externalJobList.appendChild(externalJobElement);
        }
        return externalJobList;
    }

    private Element addMembers(Container container) {
        Element members = document.createElement("members");
        for (Membership membershipsCurrent : container.getMembershipsCurrent()) {
            Element member = document.createElement("member");
            appendChildCommonMemberAttributes(member, membershipsCurrent);
            members.appendChild(member);
        }
        return members;
    }

    private Element addMembersFormer(Container container) {
        Element members = document.createElement("formermembers");
        for (Membership membershipsFormer : container.getMembershipsFormer()) {
            Element member = document.createElement("formermember");
            appendChildCommonMemberAttributes(member, membershipsFormer);
            members.appendChild(member);
        }
        return members;
    }

    private Element addOrderItems(Set<OrderItem> orderItems) {
        Element orderItemList = addCollectionElement(orderItems, "orderitems");
        for (OrderItem orderItem : orderItems) {
            Element orderItemElement = document.createElement("orderitem");
            appendChild(orderItemElement, "id", orderItem.getIdString());
            appendChild(orderItemElement, "chargeable", orderItem.isChargeable());
            appendChild(orderItemElement, "tubeid", orderItem.getTubeId());
            if (orderItem.getSample() != null) {
                appendChild(orderItemElement, "sample", orderItem.getSample().getDisplayName());
            }
            if (orderItem.getPlate() != null) {
                appendChild(orderItemElement, "plate", orderItem.getPlate().getDisplayName());
            }
            if (orderItem.getService() != null) {
                appendChild(orderItemElement, "service", orderItem.getService().getDisplayName());
            }
            appendChild(orderItemElement, "insertsize", orderItem.getInsertSize());
            appendChild(orderItemElement, "librarytype", orderItem.getLibraryType());
            appendChild(orderItemElement, "multiplexing", orderItem.getMultiplexing());
            appendChild(orderItemElement, "readtype", orderItem.getReadType());
            appendChild(orderItemElement, "region", orderItem.getRegion());
            appendChildCommonAttributesEnd(orderItemElement, orderItem);
            orderItemList.appendChild(orderItemElement);
        }
        return orderItemList;
    }

    private Element addOrders(Set<Order> orders) {
        Element orderList = addCollectionElement(orders, "orders");
        for (Order order : orders) {
            Element orderElement = document.createElement("order");
            appendChildContainer(orderElement, order, false);
            orderList.appendChild(orderElement);
        }
        return orderList;
    }

    private Element addParameters(Set<Parameter> parameters) {
        Element parameterList = addCollectionElement(parameters, "parameters");
        for (Parameter parameter : parameters) {
            Element parameterElement = document.createElement("parameter");
            appendChild(parameterElement, "id", parameter.getId());
            appendChild(parameterElement, "context", parameter.getContext());
            appendChild(parameterElement, "key", parameter.getKey());
            appendChild(parameterElement, "label", parameter.getLabel());
            appendChild(parameterElement, "type", parameter.getType());
            appendChild(parameterElement, "value", parameter.getValue());
            appendChild(parameterElement, "description", parameter.getDescription());
            appendChild(parameterElement, "required", parameter.isRequired());
            appendChild(parameterElement, "modifiable", parameter.isModifiable());
            appendChild(parameterElement, "inUse", parameter.isInUse());
            appendChildCommonAttributesEnd(parameterElement, parameter);
            parameterList.appendChild(parameterElement);
        }
        return parameterList;
    }

    private Element addPlateStates(List<PlateStatus> states) {
        Element stateList = addCollectionElement(states, "states");
        for (PlateStatus state : states) {
            Element stateElement = document.createElement("state");
            appendChildCommonAttributes(stateElement, state);
            appendChildCommonAttributesEnd(stateElement, state);
            stateList.appendChild(stateElement);
        }
        return stateList;
    }

    private Element addResources(Set<Resource> resources) {
        return addResources(resources, null);
    }

    private Element addResources(Set<Resource> resources, String label) {
        String printLabel = label != null ? label : "resource";
        Element resourceList = addCollectionElement(resources, printLabel + "s");
        for (Resource resource : resources) {
            Element resourceElement = document.createElement(printLabel);
            appendChildCommonAttributes(resourceElement, resource);
            appendChild(resourceElement, "status", resource.getStatus());
            appendChild(resourceElement, "size", resource.getSize());
            appendChild(resourceElement, "relativePath", resource.getRelativePath());
            appendChild(resourceElement, "workunit", resource.getWorkunit().getId());
            appendChildEntityIfNonEmpty(resourceElement, "sample", resource.getSample());
            appendChildEntityIfNonEmpty(resourceElement, "inputResource", resource.getInputResource());
            appendChildIfNonEmpty(resourceElement, "report", resource.getReport());
            appendChildIfNonEmpty(resourceElement, "checksum", resource.getFileChecksum());
            appendChildCommonAttributesEnd(resourceElement, resource);
            resourceList.appendChild(resourceElement);
        }
        return resourceList;
    }

    private Element addRunStates(List<RunStatus> states) {
        Element stateList = addCollectionElement(states, "states");
        for (RunStatus state : states) {
            Element stateElement = document.createElement("state");
            appendChildCommonAttributes(stateElement, state);
            appendChildCommonAttributesEnd(stateElement, state);
            stateList.appendChild(stateElement);
        }
        return stateList;
    }

    private Element addSamples(Set<Sample> samples) {
        Element samplesElement = document.createElement("samples");
        for (Sample sample : samples) {
            Element sampleElement = document.createElement("sample");
            documentBuilderSampleElement(sample, sampleElement);
            appendChildCommonAttributesEnd(sampleElement, sample);
            samplesElement.appendChild(sampleElement);
        }
        return samplesElement;
    }

    private Element addServiceOrganizationPrices(Set<ServiceOrganizationTypePrice> serviceOrganizationTypePrices) {
        Element stateList = addCollectionElement(serviceOrganizationTypePrices, "serviceorganizationprices");
        for (ServiceOrganizationTypePrice serviceOrganizationTypePrice : serviceOrganizationTypePrices) {
            Element element = document.createElement("serviceorganizationprice");
            appendChildEntityNameIfNonEmpty(element, "name", serviceOrganizationTypePrice.getOrganizationType());
            appendChildIfNonEmpty(element, "basicprice", serviceOrganizationTypePrice.getBasicPrice());
            appendChildIfNonEmpty(element, "eugrantprice", serviceOrganizationTypePrice.getEuGrantPrice());
            appendChildIfNonEmpty(element, "additionalprice", serviceOrganizationTypePrice.getAdditionalPrice());
            stateList.appendChild(element);
        }
        return stateList;
    }

    private Element addServiceUser(Set<User> users) {
        Element stateList = addCollectionElement(users, "users");
        for (User user : users) {
            Element element = document.createElement("user");
            appendChild(element, "lastname", user.getLastName());
            appendChild(element, "firstname", user.getFirstName());
            appendChild(element, "email", user.getEmail());
            appendChildEntityNameIfNonEmpty(element, "institute", user.getInstitute());
            if (user.getDivision() != null) {
                appendChildEntityNameIfNonEmpty(element, "company", user.getDivision().getCompany());
            }
            appendChildIfNonEmpty(element, "lastlogin", user.getLastLoginDate());
            stateList.appendChild(element);
        }
        return stateList;
    }

    private void addSummaryApplication(Element element) {
        Application application = (Application) entity;
        appendChildCollectionSizeIfNonEmpty(element, "parameters", application.getParameters());
        appendChildCollectionSizeIfNonEmpty(element, "precedingapplications", application.getPrecedingApplications());
        appendChildCollectionSizeIfNonEmpty(element, "succeedingapplications", application.getSucceedingApplications());
        appendChildCollectionSizeIfNonEmpty(element, "workunits", application.getWorkunits());
        int resourceCount = CDI.current().select(ResourceList.class).get().getLazyModelByApplicationId(application.getId()).getSize();
        appendChildCount(element, "resources", resourceCount);
        appendChildCollectionSizeIfNonEmpty(element, "importresources", application.getImportResources());
        appendChildCollectionSizeIfNonEmpty(element, "successors", application.getSuccessors());
    }

    private void addSummaryCompany(Element element) {
        Company company = (Company) entity;
        appendChildCollectionSizeIfNonEmpty(element, "divisions", company.getDivisions());
        appendChildCollectionSizeIfNonEmpty(element, "members", company.getMembers());
        int projectCount = CDI.current().select(ProjectList.class).get().getLazyModelByCompanyId(company.getId()).getSize();
        appendChildCount(element, "projects", projectCount);
        int orderCount = CDI.current().select(OrderList.class).get().getLazyModelByCompanyId(company.getId()).getSize();
        appendChildCount(element, "orders", orderCount);
        int bookedChargeCount = CDI.current().select(StatisticsList.class).get().getBookedChargeCompany(company.getId()).size();
        appendChildCount(element, "bookedcharges", bookedChargeCount);
    }

    private void addSummaryContainer(Element element, Container container) {
        appendChildCollectionSizeIfNonEmpty(element, "members", container.getMembers());
        appendChildCollectionSizeIfNonEmpty(element, "formermembers", container.getMembersFormerSorted());
        appendChildCollectionSizeIfNonEmpty(element, "trackers", container.getTrackingUsers());
        appendChildCollectionSizeIfNonEmpty(element, "comments", container.getComments());
        appendChildCollectionSizeIfNonEmpty(element, "samples", container.getSamples());
        appendChildCollectionSizeIfNonEmpty(element, "workunits", container.getWorkunits());
        appendChildCollectionSizeIfNonEmpty(element, "datasets", container.getDatasets());
        appendChildCollectionSizeIfNonEmpty(element, "orders", container.getOrders());
        appendChildCollectionSizeIfNonEmpty(element, "offers", container.getOffers());
        appendChildCollectionSizeIfNonEmpty(element, "charges", container.getCharges());
        appendChildCollectionSizeIfNonEmpty(element, "bookings", container.getBookings());
        appendChildCollectionSizeIfNonEmpty(element, "instrumentreservations", container.getInstrumentReservations());
        appendChildMailCount(element, container);
        if (container.getResourcesTotalSize() != null) {
            appendChild(element, "resourcesTotalSize", NumberUtils.getPrintSize(container.getResourcesTotalSize()));
        }
        appendChild(element, "billableCosts", container.getBillableChargeCount());
    }

    private void addSummaryDataset(Element element) {
        Dataset dataset = (Dataset) entity;
        appendChildCollectionSizeIfNonEmpty(element, "comments", dataset.getComments());
        appendChildCollectionSizeIfNonEmpty(element, "workflows", dataset.getWorkflows());
        appendChildCollectionSizeIfNonEmpty(element, "workflowsteps", dataset.getWorkflowSteps());
        appendChildCollectionSizeIfNonEmpty(element, "inputresources", dataset.getInputResources());
        appendChildCollectionSizeIfNonEmpty(element, "succeedingworkunits", dataset.getSucceedingWorkunits());
    }

    private void addSummaryDepartment(Element element) {
        Department department = (Department) entity;
        appendChildCollectionSizeIfNonEmpty(element, "institutes", department.getInstitutes());
        appendChildCollectionSizeIfNonEmpty(element, "members", department.getMembers());
        int projectCount = CDI.current().select(ProjectList.class).get().getLazyModelByDepartmentId(department.getId()).getSize();
        appendChildCount(element, "projects", projectCount);

        int orderCount = CDI.current().select(OrderList.class).get().getLazyModelByDepartmentId(department.getId()).getSize();
        appendChildCount(element, "orders", orderCount);

        int bookedChargeCount = CDI.current().select(StatisticsList.class).get().getBookedChargeDepartment(department.getId()).size();
        appendChildCount(element, "bookedcharges", bookedChargeCount);

    }

    private void addSummaryInstitute(Element element) {
        Institute institute = (Institute) entity;
        appendChildCollectionSizeIfNonEmpty(element, "members", institute.getMembers());
        appendChildCollectionSizeIfNonEmpty(element, "projects", institute.getProjects());
        appendChildCollectionSizeIfNonEmpty(element, "bookings", institute.getBookings());
        appendChildCollectionSizeIfNonEmpty(element, "orders", institute.getOrders());
        int bookedChargeCount = CDI.current().select(StatisticsList.class).get().getBookedChargeInstitute(institute.getId()).size();
        appendChildCount(element, "bookedcharges", bookedChargeCount);
    }

    private void addSummaryOrganization(Element element) {
        Organization organization = (Organization) entity;
        appendChildCollectionSizeIfNonEmpty(element, "departments", organization.getDepartments());
        appendChildCollectionSizeIfNonEmpty(element, "institutes", organization.getInstitutes());
        appendChildCollectionSizeIfNonEmpty(element, "members", organization.getMembers());
        int projectCount = CDI.current().select(ProjectList.class).get().getLazyModelByOrganizationId(organization.getId()).getSize();
        appendChildCount(element, "projects", projectCount);
        int orderCount = CDI.current().select(OrderList.class).get().getLazyModelByOrganizationId(organization.getId()).getSize();
        appendChildCount(element, "orders", orderCount);
        int bookedChargeCount = CDI.current().select(StatisticsList.class).get().getBookedChargeOrganization(organization.getId()).size();
        appendChildCount(element, "bookedcharges", bookedChargeCount);
    }

    private void addSummaryOrganizationType(Element element) {
        OrganizationType organizationType = (OrganizationType) entity;
        appendChildCollectionSizeIfNonEmpty(element, "organizations", organizationType.getOrganizations());
        appendChildCollectionSizeIfNonEmpty(element, "billingorganizations", organizationType.getBillingOrganizations());
        appendChildCollectionSizeIfNonEmpty(element, "companies", organizationType.getCompanies());
        appendChildCollectionSizeIfNonEmpty(element, "billingcompanies", organizationType.getBillingCompanies());
        int bookedChargeCount = CDI.current().select(StatisticsList.class).get().getBookedChargeOrganizationType(organizationType.getId()).size();
        appendChildCount(element, "bookedcharges", bookedChargeCount);
    }

    private void addSummaryPlate(Element element) {
        Plate plate = (Plate) entity;
        appendChildCollectionSizeIfNonEmpty(element, "comments", plate.getComments());
        appendChildCollectionSizeIfNonEmpty(element, "samples", plate.getSamplePlatePositions());
        appendChildCollectionSizeIfNonEmpty(element, "containers", plate.getContainers());
        appendChildMailCount(element, plate);
    }

    private void addSummaryResource(Element element) {
        Resource resource = (Resource) entity;
        appendChildCollectionSizeIfNonEmpty(element, "succeedingworkunits", resource.getSucceedingWorkunits());
        appendChildCollectionSizeIfNonEmpty(element, "succeedingdatasets", resource.getSucceedingDatasets());
        appendChildCollectionSizeIfNonEmpty(element, "accesses", resource.getEnabledAccesses());
    }

    private void addSummaryRun(Element element) {
        Run run = (Run) entity;
        appendChildCollectionSizeIfNonEmpty(element, "comments", run.getComments());
        appendChildCollectionSizeIfNonEmpty(element, "datasets", run.getDatasets());
        appendChildCollectionSizeIfNonEmpty(element, "samples", run.getSamples());
        appendChildCollectionSizeIfNonEmpty(element, "container", run.getContainers());
        appendChildMailCount(element, run);
    }

    private void addSummaryService(Element element) {
        Service service = (Service) entity;
        appendChildCollectionSizeIfNonEmpty(element, "notes", service.getNotes());
        appendChildCollectionSizeIfNonEmpty(element, "parents", service.getParents());
        appendChildCollectionSizeIfNonEmpty(element, "children", service.getChildren());
        appendChildCollectionSizeIfNonEmpty(element, "offeredcharges", service.getOfferedCharges());
        appendChildCollectionSizeIfNonEmpty(element, "charges", service.getCharges());
        appendChildCollectionSizeIfNonEmpty(element, "trackers", service.getTrackingUsers());
        appendChildCollectionSizeIfNonEmpty(element, "instruments", service.getInstruments());
        int orderCount = CDI.current().select(OrderList.class).get().getLazyModelByServiceId(service.getId()).getSize();
        appendChildCount(element, "orders", orderCount);
        appendChildMailCount(element, service);
    }

    private void addSummaryServiceArea(Element element) {
        ServiceArea serviceArea = (ServiceArea) entity;
        appendChildCollectionSizeIfNonEmpty(element, "notes", serviceArea.getNotes());
        appendChildCollectionSizeIfNonEmpty(element, "servicetype", serviceArea.getServiceTypes());
        appendChildCollectionSizeIfNonEmpty(element, "services", serviceArea.getServices());
        int orderCount = CDI.current().select(OrderList.class).get().getLazyModelByServiceAreaId(serviceArea.getId()).getSize();
        appendChildCount(element, "orders", orderCount);
        int chargeCount = CDI.current().select(ChargeList.class).get().getLazyModelByServiceAreaId(serviceArea.getId()).getSize();
        appendChildCount(element, "charges", chargeCount);
        appendChildMailCount(element, serviceArea);
    }

    private void addSummaryServiceType(Element element) {
        ServiceType serviceType = (ServiceType) entity;
        appendChildCollectionSizeIfNonEmpty(element, "notes", serviceType.getNotes());
        appendChildCollectionSizeIfNonEmpty(element, "services", serviceType.getServices());
        appendChildCollectionSizeIfNonEmpty(element, "orders", serviceType.getOrders());
        int chargeCount = CDI.current().select(ChargeList.class).get().getLazyModelByServiceTypeId(serviceType.getId()).getSize();
        appendChildCount(element, "charges", chargeCount);
        appendChildCollectionSizeIfNonEmpty(element, "instruments", serviceType.getInstruments());
        appendChildCollectionSizeIfNonEmpty(element, "workflowtemplates", serviceType.getWorkflowTemplates());
        appendChildCollectionSizeIfNonEmpty(element, "orderattributes", serviceType.getOrderAttributes());
        appendChildCollectionSizeIfNonEmpty(element, "sequencingapplications", serviceType.getSequencingApplications());
        appendChildMailCount(element, serviceType);
    }

    private void addSummaryStorage(Element element) {
        Storage storage = (Storage) entity;
        appendChildCollectionSizeIfNonEmpty(element, "accesses", storage.getAccesses());
        appendChildCollectionSizeIfNonEmpty(element, "parameters", storage.getParameters());
        appendChildCollectionSizeIfNonEmpty(element, "applications", storage.getApplications());
        appendChildCollectionSizeIfNonEmpty(element, "resources", storage.getResources());
        appendChildCollectionSizeIfNonEmpty(element, "importresources", storage.getImportResources());
    }

    private void addSummaryUser(Element element) {
        User user = (User) entity;
        appendChildCollectionSizeIfNonEmpty(element, "orders", user.getOrders());
        appendChildCollectionSizeIfNonEmpty(element, "projects", user.getProjects());
        appendChildCollectionSizeIfNonEmpty(element, "formerprojects", user.getProjectsFormer());
        appendChildCount(element, "budgetofficercharges", user.getBudgetOfficerChargeCount());
        appendChildCount(element, "samples", user.getSampleCount());
        appendChildCount(element, "workunits", user.getWorkunitCount());
        appendChildCount(element, "resources", user.getResourceCount());
        appendChildCount(element, "datasets", user.getDatasetCount());
        appendChildCollectionSizeIfNonEmpty(element, "coachedcontainers", user.getCoachedContainers());
        appendChildCollectionSizeIfNonEmpty(element, "discussedcontainers", user.getDiscussedContainers());
        appendChildCollectionSizeIfNonEmpty(element, "plates", user.getPlates());
        appendChildCollectionSizeIfNonEmpty(element, "runs", user.getRuns());
        appendChildCollectionSizeIfNonEmpty(element, "offers", user.getOffers());
        appendChildCollectionSizeIfNonEmpty(element, "chargercharges", user.getChargerCharges());
        int instrumentReservationCount = CDI.current().select(InstrumentReservationList.class).get().getLazyModelByUserId(user.getId()).getSize();
        appendChildCount(element, "instrumentreservation", instrumentReservationCount);
        appendChildCollectionSizeIfNonEmpty(element, "instruments", user.getInstruments());
        int commentCount = CDI.current().select(CommentList.class).get().getLazyModelByCreatedByUser(user).getSize();
        appendChildCount(element, "comments", commentCount);
        int commentAssociatedCount = CDI.current().select(CommentList.class).get().getLazyModelAssociatedCommentByUser(user).getSize();
        appendChildCount(element, "commentassociated", commentAssociatedCount);
        int commentStarredCount = CDI.current().select(CommentList.class).get().getLazyModelByUserIdStarred(user.getId()).getSize();
        appendChildCount(element, "commentstarred", commentStarredCount);
        int commentViewedCount = CDI.current().select(CommentList.class).get().getLazyModelByUserIdViewed(user.getId()).getSize();
        appendChildCount(element, "commentviewed", commentViewedCount);
        appendChildCollectionSizeIfNonEmpty(element, "contracts", user.getContracts());
        appendChildCollectionSizeIfNonEmpty(element, "events", user.getEvents());
        appendChildCollectionSizeIfNonEmpty(element, "credits", user.getCredits());
        appendChildMailCount(element, user);
        appendChildCollectionSizeIfNonEmpty(element, "serviceareas", user.getServiceAreas());
        appendChildCollectionSizeIfNonEmpty(element, "servicetypes", user.getServiceTypes());
        appendChildCollectionSizeIfNonEmpty(element, "trackedcontainers", user.getTrackedContainers());
        appendChildCollectionSizeIfNonEmpty(element, "trackedservices", user.getTrackedServices());
        appendChildCollectionSizeIfNonEmpty(element, "accessrequests", user.getAccessRequests());
        int userGroupCount = CDI.current().select(UserGroupList.class).get().getMember(user).getSize();
        appendChildCount(element, "usergroups", userGroupCount);
        int supervisedUserGroupCount = CDI.current().select(UserGroupList.class).get().getSupervisedUserGroups(user).getSize();
        appendChildCount(element, "supervisedusergroups", supervisedUserGroupCount);
        int actionCount = CDI.current().select(EntityLogList.class).get().getLazyModelByCreatedBy(user.getLogin()).getSize();
        appendChildCount(element, "actions", actionCount);
    }

    private void addSummaryWorkunit(Element element) {
        Workunit workunit = (Workunit) entity;
        appendChildCollectionSizeIfNonEmpty(element, "comments", workunit.getComments());
        appendChildCollectionSizeIfNonEmpty(element, "associatedcomments", workunit.getAssociatedComments());
        appendChildCollectionSizeIfNonEmpty(element, "workflowsteps", workunit.getWorkflowSteps());
        appendChildCollectionSizeIfNonEmpty(element, "succeedingworkunits", workunit.getSucceedingWorkunits());
        appendChildCollectionSizeIfNonEmpty(element, "succeedingdatasets", workunit.getSucceedingDatasets());
        appendChildCollectionSizeIfNonEmpty(element, "importresources", workunit.getImportResources());
    }

    private Element addTechnologies(Set<Technology> technologies) {
        Element technologyList = addCollectionElement(technologies, "technologies");
        for (Technology technology : technologies) {
            Element technologyElement = document.createElement("technology");
            technologyElement.setTextContent(technology.getName());
            technologyList.appendChild(technologyElement);

        }
        return technologyList;
    }

    private Element addWorkflows(Set<Workflow> workflows) {
        Element workflowList = addCollectionElement(workflows, "workflows");
        for (Workflow workflow : workflows) {
            Element workflowElement = document.createElement("workflow");
            appendChild(workflowElement, "id", workflow.getIdString());
            appendChildEntityNameIfNonEmpty(workflowElement, "workflowtemplate", workflow.getWorkflowTemplate());
            if (!workflow.getOrderItems().isEmpty()) {
                Element orderItemList = addCollectionElement(workflow.getOrderItems(), "orderitems");
                for (OrderItem orderItem : workflow.getOrderItems()) {
                    appendChild(orderItemList, "orderitemid", orderItem.getIdString());
                }
                workflowElement.appendChild(orderItemList);
            }
            if (!workflow.getWorkflowSteps().isEmpty()) {
                Element workflowStepList = addCollectionElement(workflow.getWorkflowSteps(), "workflowsteps");
                for (WorkflowStep workflowStep : workflow.getWorkflowSteps()) {
                    Element workflowStepElement = document.createElement("workflowstep");
                    appendChild(workflowStepElement, "id", workflowStep.getIdString());
                    appendChildEntityNameIfNonEmpty(workflowStepElement, "workflowtemplatestep", workflowStep.getWorkflowTemplateStep());
                    if (workflowStep.getStatus() != null && StringHelper.isNotEmpty(workflowStep.getStatus().toString())) {
                        appendChild(workflowStepElement, "status", workflowStep.getStatus().toString());
                    }
                    if (workflowStep.getStartDateTime() != null) {
                        appendChild(workflowStepElement, "starttime", workflowStep.getStartDateTime());
                    }
                    if (workflowStep.getEndDateTime() != null) {
                        appendChild(workflowStepElement, "endtime", workflowStep.getEndDateTime());
                    }
                    if (workflowStep.getExpectedDuration() != null) {
                        appendChild(workflowStepElement, "expectedduration", workflowStep.getExpectedDuration());
                    }
                    appendChildCommonAttributesEnd(workflowStepElement, workflowStep);
                    workflowStepElement.appendChild(addComments(workflowStep.getCommentsCurrentUser(), null));
                    workflowStepList.appendChild(workflowStepElement);
                }
                workflowElement.appendChild(workflowStepList);
            }
            appendChildCommonAttributesEnd(workflowElement, workflow);
            workflowList.appendChild(workflowElement);
        }
        return workflowList;
    }

    private Element addWorkunits(Set<Workunit> workunits) {
        Element workunitList = addCollectionElement(workunits, "workunits");
        for (Workunit workunit : workunits) {
            Element workunitElement = document.createElement("workunit");
            appendChildCommonAttributes(workunitElement, workunit);
            appendChild(workunitElement, "status", workunit.getStatus());
            appendChildEntityIfNonEmpty(workunitElement, "container", workunit.getContainer());
            appendChildEntityIfNonEmpty(workunitElement, "application", workunit.getApplication());
            appendChildEntityIfNonEmpty(workunitElement, "dataset", workunit.getDataset());
            appendChildEntityIfNonEmpty(workunitElement, "inputDataset", workunit.getInputDataset());
            appendChildCommonAttributesEnd(workunitElement, workunit);
            workunitList.appendChild(workunitElement);
        }
        return workunitList;
    }

    private void appendChild(Element parent, String elementName, Object elementContent) {
        if (document != null && parent != null && StringHelper.isNotEmpty(elementName)) {
            Element element = document.createElement(elementName);
            element.appendChild(document.createTextNode(String.valueOf(elementContent)));
            parent.appendChild(element);
        }
    }

    private void appendChildCollectionSizeIfNonEmpty(Element parent, String elementName, Collection<?> collection) {
        if (collection != null && !collection.isEmpty()) {
            appendChild(parent, elementName, collection.size());
        }
    }

    private <T extends AbstractNamedBaseEntity> void appendChildCommonAttributes(Element element, T entity) {
        appendChild(element, "id", entity.getId());
        appendChild(element, "name", entity.getName());
    }

    private <T extends AbstractBaseEntity> void appendChildCommonAttributesEnd(Element element, T entity) {
        appendChild(element, "created", entity.getCreated());
        appendChild(element, "createdby", entity.getCreatedBy());
        appendChild(element, "modified", entity.getModified());
        appendChild(element, "modifiedby", entity.getModifiedBy());
    }

    private void appendChildCommonMemberAttributes(Element element, Membership membership) {
        User user = membership.getUser();
        appendChild(element, "id", user.getIdString());
        appendChild(element, "name", user.getName());
        appendChild(element, "login", user.getLogin());
        appendChild(element, "email", user.getEmail());
        appendChild(element, "role", membership.getRole());
        appendChildCommonAttributesEnd(element, user);
    }

    private void appendChildContainer(Element element, Container container, boolean isAppendMembers) {
        if (element != null && container != null) {
            element.appendChild(addDetails(container));
            if (isAppendMembers) {
                Container parentContainer = container.getContainer();
                if (parentContainer != null) {
                    if (!parentContainer.getMembers().isEmpty()) {
                        element.appendChild(addMembers(parentContainer));
                    }
                    if (!parentContainer.getMembershipsFormer().isEmpty()) {
                        element.appendChild(addMembersFormer(parentContainer));
                    }
                }
            }
            if (!container.getCommentsCurrentUser().isEmpty()) {
                element.appendChild(addComments(container.getCommentsCurrentUser(), null));
            }
            if (!container.getResultsCurrentUser().isEmpty()) {
                element.appendChild(addComments(container.getResultsCurrentUser(), "result"));
            }
            if (!container.getNotesCurrentUser().isEmpty()) {
                element.appendChild(addComments(container.getNotesCurrentUser(), "note"));
            }
            if (!container.getSamples().isEmpty()) {
                element.appendChild(addSamples(container.getSamples()));
            }
            if (!container.getWorkunits().isEmpty()) {
                element.appendChild(addWorkunits(container.getWorkunits()));
            }
            if (!container.getResources().isEmpty()) {
                element.appendChild(addResources(container.getResources()));
            }
            if (!container.getDatasets().isEmpty()) {
                element.appendChild(addDatasets(container.getDatasets()));
            }
            if (!container.getCustomAttributes().isEmpty()) {
                element.appendChild(addCustomAttributes(container.getCustomAttributes()));
            }
            if (!container.getStates().isEmpty()) {
                element.appendChild(addContainerStates(container.getAllStates()));
            }
        }
    }

    private void appendChildCount(Element element, String elementName, int count) {
        if (count > 0) {
            appendChild(element, elementName, count);
        }
    }

    private <T extends AbstractNamedBaseEntity> void appendChildEntityIfNonEmpty(Element parent, String elementName, T entity) {
        if (entity != null) {
            appendChild(parent, elementName, entity.getId());
        }
    }

    private <T extends AbstractNamedBaseEntity> void appendChildEntityNameIfNonEmpty(Element parent, String elementName, T entity) {
        if (entity != null) {
            appendChild(parent, elementName, entity.getName());
        }
    }

    private void appendChildIfNonEmpty(Element parent, String elementName, Object elementContent) {
        if (elementContent != null) {
            appendChild(parent, elementName, elementContent);
        }
    }

    private void appendChildIfTrue(Element parent, String elementName, Boolean elementContent) {
        if (elementContent) {
            appendChild(parent, elementName, true);
        }
    }

    private <T extends AbstractBaseEntity> void appendChildMailCount(Element element, T entity) {
        MailService mailService = CDI.current().select(MailService.class).get();
        int mailCount = mailService.getMailsLazyModelByParent(entity, entity.getCurrentUser()).getSize();
        appendChildCount(element, "mails", mailCount);
    }

    private void appendChildOrderDetails(Element element, Order order) {
        if (order.getServiceType() != null) {
            if (StringHelper.isNotEmpty(order.getServiceType().getDisplayName())) {
                appendChild(element, "servicetype", order.getServiceType().getDisplayName());
            }
            for (OrderAttribute orderAttribute : order.getServiceType().getOrderAttributes()) {
                String elementContent = null;
                String orderAttributeName = orderAttribute.getAttributeName();
                if (orderAttributeName != null) {
                    switch (orderAttributeName) {
                    case Constants.CONSUMABLE:
                        if (order.getConsumable() != null) {
                            elementContent = order.getConsumable().getDisplayName();
                        }
                        break;
                    case Constants.DATA_PRODUCED:
                        if (order.getDataProduced() != null) {
                            elementContent = order.getDataProduced().toString();
                        }
                        break;
                    case Constants.DEMULTIPLEXING:
                        if (order.getDemultiplexing() != null) {
                            elementContent = order.getDemultiplexing().getDisplayName();
                        }
                        break;
                    case Constants.HOURS_REQUESTED:
                        if (order.getHoursRequested() != null) {
                            elementContent = order.getHoursRequested().toString();
                        }
                        break;
                    case Constants.ORDER_ITEM_INSERT_SIZE:
                        if (order.getInsertSize() != null) {
                            elementContent = order.getInsertSize().toString();
                        }
                        break;
                    case Constants.INSTRUMENT:
                        if (order.getInstrument() != null) {
                            elementContent = order.getInstrument().getDisplayName();
                        }
                        break;
                    case Constants.KITS_USED:
                        elementContent = order.getKitsUsed();
                        break;
                    case Constants.LIBRARY_PROTOCOL:
                        if (order.getLibraryProtocol() != null) {
                            elementContent = order.getLibraryProtocol().getDisplayName();
                        }
                        break;
                    case Constants.NUMBER_OF_CELLS_NUCLEI:
                        if (order.getNumberOfCellsNuclei() != null) {
                            elementContent = order.getNumberOfCellsNuclei().toString();
                        }
                        break;
                    case Constants.NUMBER_OF_CHIPS:
                        if (order.getNumberOfChips() != null) {
                            elementContent = order.getNumberOfChips().toString();
                        }
                        break;
                    case Constants.NUMBER_OF_RUNS_SEQUENCING:
                        if (order.getNumberOfRunsSequencing() != null) {
                            elementContent = order.getNumberOfRunsSequencing().toString();
                        }
                        break;
                    case Constants.NUMBER_OF_RUNS_TAPE_STATION:
                        if (order.getNumberOfRunsTapeStation() != null) {
                            elementContent = order.getNumberOfRunsTapeStation().toString();
                        }
                        break;
                    case Constants.NUMBER_OF_SAMPLES:
                        if (order.getNumberOfSamples() != null) {
                            elementContent = order.getNumberOfSamples().toString();
                        }
                        break;
                    case Constants.REMARKS:
                        elementContent = order.getRemarks();
                        break;
                    case Constants.SEQUENCING_APPLICATION:
                        if (order.getSequencingApplication() != null) {
                            elementContent = order.getSequencingApplication().getDisplayName();
                        }
                        break;
                    case Constants.STORAGE_MODEL:
                        if (order.getStorageModel() != null) {
                            elementContent = order.getStorageModel().getName() + " (" + order.getStorageModel().getDescription() + ")";
                        }
                        break;
                    case Constants.TOTAL_NUMBER_OF_INSTRUMENT_DATA_PACKAGES:
                        if (order.getTotalNumberOfInstrumentDataPackages() != null) {
                            elementContent = order.getTotalNumberOfInstrumentDataPackages().toString();
                        }
                        break;
                    case Constants.INSTRUMENT_DATA_DELIVERY:
                        if (order.getInstrumentDataDelivery() != null) {
                            elementContent = order.getInstrumentDataDelivery().getDisplayName();
                        }
                        break;
                    case Constants.INSTRUMENT_DATA_PACKAGES:
                        if (order.getInstrumentDataPackage() != null) {
                            elementContent = order.getInstrumentDataPackage().getDisplayName();
                        }
                        break;
                    case Constants.USER_BENCH_USAGE:
                        if (order.getUserBenchUsage() != null) {
                            elementContent = order.getUserBenchUsage().toString();
                        }
                        break;
                    default:
                        break;
                    }

                    if (StringHelper.isNotEmpty(elementContent)) {
                        appendChild(element, orderAttributeName.toLowerCase(), elementContent);
                    }

                    if (Constants.LIBRARY_PROTOCOL.equals(orderAttributeName) && !order.getLibraryProtocolOptionValues().isEmpty()) {
                        Element optionsElement = document.createElement("libraryprotocoloptions");
                        optionsElement.setAttribute("count", String.valueOf(order.getLibraryProtocolOptionValues().size()));
                        element.appendChild(optionsElement);
                        for (OptionValue optionValue : order.getLibraryProtocolOptionValues()) {
                            Element optionElement = document.createElement("libraryprotocoloption");
                            appendChild(optionElement, "name", optionValue.getOption().getName());
                            appendChild(optionElement, "value", optionValue.getName());
                            optionsElement.appendChild(optionElement);
                        }
                    }
                }
            }
        }
        if (order.getProject() != null && StringHelper.isNotEmpty(order.getProject().getDisplayName())) {
            appendChild(element, "project", order.getProject().getDisplayName());
        }
        if (order.getOffer() != null && StringHelper.isNotEmpty(order.getOffer().getDisplayName())) {
            appendChild(element, "offer", order.getOffer().getDisplayName());
        }
    }

    private void appendChildProjectDetails(Element element, Project project) {

        if (!project.getTechnologies().isEmpty()) {
            element.appendChild(addTechnologies(project.getTechnologies()));
        }
        appendChild(element, "summary", project.getSummary());
    }

    private void appendChildUserNameIfNonEmpty(Element parent, String elementName, User user) {
        if (user != null) {
            appendChild(parent, elementName, user.getName());
        }
    }

    public String createXml() {
        String ret = Constants.EMPTY_STRING;
        if (entity != null) {
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                StringWriter writer = new StringWriter();
                if (entity instanceof Workunit) {
                    documentBuilderWorkunit();
                }
                if (entity instanceof Resource) {
                    documentBuilderResource();
                }
                if (entity instanceof Plate) {
                    documentBuilderPlate();
                }
                if (entity instanceof Run) {
                    documentBuilderRun();
                }
                if (entity instanceof Application) {
                    documentBuilderApplication();
                }
                if (entity instanceof Dataset) {
                    documentBuilderDataset();
                }
                if (entity instanceof Storage) {
                    documentBuilderStorage();
                }
                if (entity instanceof Sample) {
                    documentBuilderSample();
                }
                if (entity instanceof Service) {
                    documentBuilderService();
                }
                if (entity instanceof ServiceArea) {
                    documentBuilderServiceArea();
                }
                if (entity instanceof ServiceType) {
                    documentBuilderServiceType();
                }
                if (entity instanceof Institute) {
                    documentBuilderInstitute();
                }
                if (entity instanceof Department) {
                    documentBuilderDepartment();
                }
                if (entity instanceof Organization) {
                    documentBuilderOrganization();
                }
                if (entity instanceof OrganizationType) {
                    documentBuilderOrganizationType();
                }
                if (entity instanceof Company) {
                    documentBuilderCompany();
                }
                if (entity instanceof User) {
                    documentBuilderUser();
                }
                if (entity instanceof Container) {
                    documentBuilderContainer();
                }
                if (document != null) {
                    transformer.transform(new DOMSource(document), new StreamResult(writer));
                }
                ret = writer.getBuffer().toString();
            } catch (TransformerFactoryConfigurationError | TransformerException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void documentBuilderApplication() {
        Application application = (Application) entity;
        appendChildCommonAttributes(documentElement, application);
        if (application.getSupervisor() != null) {
            appendChild(documentElement, "supervisor", application.getSupervisor().getFullName());
        }
        if (application.getApplicationType() != null) {
            appendChild(documentElement, "type", application.getApplicationType().getName());
        }
        if (application.getPageflow() != null) {
            appendChild(documentElement, "pageflow", application.getPageflow().getName());
        }

        if (!application.getTechnologies().isEmpty()) {
            documentElement.appendChild(addTechnologies(application.getTechnologies()));
        }

        addSummaryApplication(documentElement);
        appendChildCommonAttributesEnd(documentElement, application);
    }

    public void documentBuilderCompany() {
        Company company = (Company) entity;
        appendChildCommonAttributes(documentElement, company);
        appendChildEntityNameIfNonEmpty(documentElement, "organizationtype", company.getOrganizationType());
        appendChildEntityNameIfNonEmpty(documentElement, "billingorganizationtype", company.getBillingOrganizationType());
        addSummaryCompany(documentElement);
        appendChildCommonAttributesEnd(documentElement, company);
    }

    public void documentBuilderContainer() {
        Container container = (Container) entity;
        appendChildContainer(documentElement, container, true);
        if (!container.getOrders().isEmpty()) {
            documentElement.appendChild(addOrders(container.getOrders()));
        }
        if (!container.getOrderItems().isEmpty()) {
            documentElement.appendChild(addOrderItems(container.getOrderItems()));
        }
        if (!container.getWorkflows().isEmpty()) {
            documentElement.appendChild(addWorkflows(container.getWorkflows()));
        }
    }

    public void documentBuilderDataset() {
        Dataset dataset = (Dataset) entity;
        appendChildCommonAttributes(documentElement, dataset);
        appendChildEntityIfNonEmpty(documentElement, "datasetTemplate", dataset.getDatasetTemplate());
        appendChildEntityIfNonEmpty(documentElement, "run", dataset.getRun());
        appendChild(documentElement, "workunit", dataset.getWorkunit());
        addDatasetContent(dataset);
        addSummaryDataset(documentElement);
        appendChildCommonAttributesEnd(documentElement, dataset);
    }

    public void documentBuilderDepartment() {
        Department department = (Department) entity;
        appendChildCommonAttributes(documentElement, department);
        appendChildEntityNameIfNonEmpty(documentElement, "organization", department.getOrganization());
        addSummaryDepartment(documentElement);
        appendChildCommonAttributesEnd(documentElement, department);
    }

    public void documentBuilderInstitute() {
        Institute institute = (Institute) entity;
        appendChildCommonAttributes(documentElement, institute);
        addSummaryInstitute(documentElement);
        appendChildCommonAttributesEnd(documentElement, institute);
    }

    public void documentBuilderOrganization() {
        Organization organization = (Organization) entity;
        appendChildCommonAttributes(documentElement, organization);
        appendChildEntityNameIfNonEmpty(documentElement, "organizationtype", organization.getOrganizationType());
        appendChildEntityNameIfNonEmpty(documentElement, "billingorganizationtype", organization.getBillingOrganizationType());
        addSummaryOrganization(documentElement);
        appendChildCommonAttributesEnd(documentElement, organization);
    }

    public void documentBuilderOrganizationType() {
        OrganizationType organizationType = (OrganizationType) entity;
        appendChildCommonAttributes(documentElement, organizationType);

        appendChildIfTrue(documentElement, "academic", organizationType.isAcademic());
        appendChildIfTrue(documentElement, "domestic", organizationType.isDomestic());
        appendChildIfTrue(documentElement, "extensible", organizationType.isExtensible());

        addSummaryOrganizationType(documentElement);
        appendChildCommonAttributesEnd(documentElement, organizationType);
    }

    public void documentBuilderPlate() {
        Plate plate = (Plate) entity;
        appendChildCommonAttributes(documentElement, plate);
        appendChild(documentElement, "status", plate.getStatus());

        if (plate.getSupervisor() != null) {
            appendChild(documentElement, "supervisor", plate.getSupervisor().getFullName());
        }
        appendChildEntityNameIfNonEmpty(documentElement, "layout", plate.getPlateLayout());
        appendChildEntityNameIfNonEmpty(documentElement, "type", plate.getPlateType());
        addSummaryPlate(documentElement);
        documentElement.appendChild(addPlateStates(plate.getStates()));
        appendChildCommonAttributesEnd(documentElement, plate);
    }

    public void documentBuilderResource() {
        Resource resource = (Resource) entity;
        appendChildCommonAttributes(documentElement, resource);
        appendChild(documentElement, "status", resource.getStatus());
        appendChild(documentElement, "size", NumberUtils.getPrintSize(resource.getSize()));
        appendChildEntityIfNonEmpty(documentElement, "sample", resource.getSample());
        appendChildEntityIfNonEmpty(documentElement, "workunit", resource.getWorkunit());
        appendChildEntityIfNonEmpty(documentElement, "storage", resource.getStorage());
        addSummaryResource(documentElement);
        appendChildCommonAttributesEnd(documentElement, resource);
    }

    public void documentBuilderRun() {
        Run run = (Run) entity;
        appendChildCommonAttributes(documentElement, run);
        appendChild(documentElement, "status", run.getStatus());
        if (run.getSupervisor() != null) {
            appendChild(documentElement, "supervisor", run.getSupervisor().getFullName());
        }
        appendChildIfTrue(documentElement, "demultiplexing", run.isDemultiplexingRequired());
        appendChild(documentElement, "qc", run.isQc());
        appendChild(documentElement, "instrument", run.getInstrumentReadConfiguration());
        addSummaryRun(documentElement);
        documentElement.appendChild(addRunStates(run.getStates()));
        appendChildCommonAttributesEnd(documentElement, run);
    }

    public void documentBuilderSample() {
        documentBuilderSampleElement((Sample) entity, documentElement);
    }

    public void documentBuilderSampleElement(Sample sample, Element parentElement) {
        appendChildCommonAttributes(parentElement, sample);
        appendChild(parentElement, "type", sample.getType());
        if (sample.getParents() != null && !sample.getParents().isEmpty()) {
            for (Sample parent : sample.getParents()) {
                appendChild(parentElement, "parent", parent.getId());
            }
        }
        if (sample.getChildren() != null && !sample.getChildren().isEmpty()) {
            for (Sample child : sample.getChildren()) {
                appendChild(parentElement, "child", child.getId());
            }
        }
        for (SampleAttributeEnum attributeEnum : SampleAttributeEnum.values()) {
            try {
                Object value = PropertyUtils.getProperty(sample, attributeEnum.getName());
                if (value != null) {
                    if (attributeEnum.isStringType()) {
                        if (StringHelper.isNotEmpty((String) value)) {
                            appendChild(parentElement, attributeEnum.getName(), value);
                        }
                    } else if (attributeEnum.isAnnotationTypeSingleValued()) {
                        appendChild(parentElement, attributeEnum.getName(), ((Annotation) value).getName());
                    } else if (attributeEnum.isAnnotationTypeMultiValued()) {
                        if (!((List<Annotation>) value).isEmpty()) {
                            for (Annotation annotation : (List<Annotation>) value) {
                                appendChild(parentElement, StringUtils.chop(attributeEnum.getName()), annotation.getName());
                            }
                        }
                    } else {
                        appendChild(parentElement, attributeEnum.getName(), value);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        }
        if (!sample.getCustomAttributes().isEmpty()) {
            parentElement.appendChild(addCustomAttributes(sample.getCustomAttributes()));
        }
        appendChildCommonAttributesEnd(parentElement, sample);
    }

    public void documentBuilderService() {
        Service service = (Service) entity;
        appendChildCommonAttributes(documentElement, service);
        appendChild(documentElement, "servicearea", service.getServiceArea().getDisplayName());
        appendChild(documentElement, "servicetype", service.getServiceType().getDisplayName());
        appendChild(documentElement, "code", service.getCode());
        appendChild(documentElement, "description", service.getDescription());
        appendChildIfTrue(documentElement, "enable", service.isEnabled());
        if (service.getFullCost() != null) {
            appendChild(documentElement, "cost", service.getFullCost());
        }

        addSummaryService(documentElement);
        if (service.getUsers() != null) {
            documentElement.appendChild(addServiceUser(service.getUsers()));
        }
        if (service.getServiceOrganizationTypePrices() != null) {
            documentElement.appendChild(addServiceOrganizationPrices(service.getServiceOrganizationTypePrices()));
        }
        appendChildCommonAttributesEnd(documentElement, service);
    }

    public void documentBuilderServiceArea() {
        ServiceArea serviceArea = (ServiceArea) entity;
        appendChildCommonAttributes(documentElement, serviceArea);
        appendChild(documentElement, "description", serviceArea.getDescription());
        appendChildIfTrue(documentElement, "enable", serviceArea.isEnabled());
        appendChild(documentElement, "serviceenabledforoffer", serviceArea.isEnabledForOffer());
        appendChild(documentElement, "servicetypeenabled", serviceArea.getServiceTypes().size());
        appendChild(documentElement, "serviceenabledcount", serviceArea.getServicesEnabledCount());
        addSummaryServiceArea(documentElement);
        if (serviceArea.getUsers() != null) {
            documentElement.appendChild(addServiceUser(serviceArea.getUsers()));
        }
        appendChildCommonAttributesEnd(documentElement, serviceArea);
    }

    public void documentBuilderServiceType() {
        ServiceType serviceType = (ServiceType) entity;
        appendChildCommonAttributes(documentElement, serviceType);
        appendChild(documentElement, "servicearea", serviceType.getServiceArea());
        if (!serviceType.getTechnologies().isEmpty()) {
            documentElement.appendChild(addTechnologies(serviceType.getTechnologies()));
        }
        appendChildIfTrue(documentElement, "enable", serviceType.isEnabled());

        appendChild(documentElement, "servicesenabled", serviceType.getServicesEnabled().size());
        appendChild(documentElement, "coach", serviceType.getCoach().getFullName());

        if (serviceType.getCoachBackup() != null) {
            appendChild(documentElement, "coachbackup", serviceType.getCoachBackup().getFullName());
        }
        appendChildIfNonEmpty(documentElement, "instructionlink", serviceType.getInstructionLink());
        appendChildIfNonEmpty(documentElement, "description", serviceType.getDescription());
        appendChildIfNonEmpty(documentElement, "internal", serviceType.isInternal());
        appendChildIfNonEmpty(documentElement, "requiresproject", serviceType.isRequiresProject());
        appendChildIfNonEmpty(documentElement, "processessamples", serviceType.isProcessesSamples());
        appendChildIfNonEmpty(documentElement, "sampletype", serviceType.getSampleType());
        appendChildIfNonEmpty(documentElement, "servicecolumnenabled", serviceType.isServiceColumnEnabled());
        appendChildIfNonEmpty(documentElement, "processesplates", serviceType.isProcessesPlates());
        appendChildIfNonEmpty(documentElement, "platesubmissionproposallimit", serviceType.getPlateSubmissionProposalLimit());
        appendChildIfNonEmpty(documentElement, "initialcustomstatus", serviceType.isInitialCustomStatus());
        addSummaryServiceType(documentElement);
        if (serviceType.getUsers() != null) {
            documentElement.appendChild(addServiceUser(serviceType.getUsers()));
        }
        appendChildCommonAttributesEnd(documentElement, serviceType);
    }

    public void documentBuilderStorage() {
        Storage storage = (Storage) entity;
        appendChildCommonAttributes(documentElement, storage);
        if (storage.getSupervisor() != null) {
            appendChild(documentElement, "supervisor", storage.getSupervisor().getFullName());
        }
        addSummaryStorage(documentElement);
        appendChildCommonAttributesEnd(documentElement, storage);
    }

    public void documentBuilderUser() {
        User user = (User) entity;
        appendChild(documentElement, "id", user.getId());
        appendChild(documentElement, "login", user.getLogin());
        appendChild(documentElement, "fullname", user.getFullName());
        if (user.getInstitute() != null) {
            appendChildEntityNameIfNonEmpty(documentElement, "organization", user.getInstitute().getDepartment().getOrganization());
            appendChildEntityNameIfNonEmpty(documentElement, "department", user.getInstitute().getDepartment());
            appendChildEntityNameIfNonEmpty(documentElement, "institute", user.getInstitute());
        }
        if (user.getDivision() != null) {
            appendChildEntityNameIfNonEmpty(documentElement, "company", user.getDivision().getCompany());
            appendChildEntityNameIfNonEmpty(documentElement, "division", user.getDivision());
        }
        appendChild(documentElement, "email", user.getEmail());
        if (user.getPhoneNumber() != null) {
            appendChild(documentElement, "phone", user.getPhoneNumber().getFullNumber());
        }
        if (user.getAddress() != null) {
            appendChild(documentElement, "address", user.getFullAddress());
            appendChild(documentElement, "room", user.getAddress().getRoom());
        }
        if (user.getHomePhone() != null) {
            appendChild(documentElement, "homephone", user.getHomePhone());
        }
        if (user.getHomeAddress() != null) {
            appendChild(documentElement, "homeaddress", user.getHomeAddress().getFullAddress());
        }
        if (user.getBirthDate() != null) {
            appendChild(documentElement, "birthdate", user.getBirthDate());
        }
        if (user.getEmpDegree() != null) {
            appendChild(documentElement, "employmentdegree", user.getEmpDegree());
            appendChild(documentElement, "accesscardnumber", user.getAccessCardNumber());
            appendChild(documentElement, "accesscardcodenumber", user.getAccessCardCode());
            appendChild(documentElement, "accesscardexpirydate", user.getAccessCardExpiryDate());
        }
        if (user.isUpdatable()) {
            appendChild(documentElement, "massemailenable", user.getMassMailEnabled());
            appendChild(documentElement, "computerloginactivated", user.isComputerLoginActivated());
            appendChild(documentElement, "computerloginenabled", user.isComputerLoginEnabled());
            appendChild(documentElement, "dataaccessenabled", user.isDataAccessEnabled());
        }
        appendChildIfTrue(documentElement, "emailactive", user.isEmailActive());
        if (user.isEmailVerifiedRendered()) {
            appendChild(documentElement, "emailverified", user.isEmailVerified());
            appendChild(documentElement, "accountenabled", user.isAccountEnabled());
        }
        addSummaryUser(documentElement);
        appendChildCommonAttributesEnd(documentElement, user);
    }

    public void documentBuilderWorkunit() {
        Workunit workunit = (Workunit) entity;
        appendChildCommonAttributes(documentElement, workunit);
        appendChild(documentElement, "status", workunit.getStatus());
        appendChild(documentElement, "size", workunit.getSize());
        appendChildEntityIfNonEmpty(documentElement, "container", workunit.getContainer());
        appendChildEntityIfNonEmpty(documentElement, "application", workunit.getApplication());
        addSummaryWorkunit(documentElement);
        if (workunit.getInputDataset() != null) {
            documentElement.appendChild(addDatasets(Collections.singleton(workunit.getInputDataset()), "inputdataset"));
        }
        if (workunit.getDataset() != null) {
            documentElement.appendChild(addDatasets(Collections.singleton(workunit.getDataset())));
        }
        if (!workunit.getInputResources().isEmpty()) {
            documentElement.appendChild(addResources(workunit.getInputResources(), "inputresource"));
        }
        if (!workunit.getResources().isEmpty()) {
            documentElement.appendChild(addResources(workunit.getResources()));
        }
        if (!workunit.getExecutables().isEmpty()) {
            documentElement.appendChild(addExecutables(workunit.getExecutables()));
        }
        if (!workunit.getExternalJobs().isEmpty()) {
            documentElement.appendChild(addExternalJobs(workunit.getExternalJobs()));
        }
        if (!workunit.getParameters().isEmpty()) {
            documentElement.appendChild(addParameters(workunit.getParameters()));
        }
        if (!workunit.getWorkflows().isEmpty()) {
            documentElement.appendChild(addWorkflows(workunit.getWorkflows()));
        }
        if (!workunit.getCommentsCurrentUser().isEmpty()) {
            documentElement.appendChild(addComments(workunit.getCommentsCurrentUser(), null));
        }
        appendChildCommonAttributesEnd(documentElement, workunit);
    }

    public Document getDocument() {
        return document;
    }

    private void preloadDatasetFields(Collection<Dataset> datasets) {
        List<Long> allItemIds = datasets.stream()
            .flatMap(dataset -> dataset.getItems().stream())
            .map(DatasetItem::getId)
            .collect(Collectors.toList());
        if (allItemIds.isEmpty()) {
            return;
        }
        DatasetService datasetService = CDI.current().select(DatasetService.class).get();
        Map<Long, List<DatasetField>> fieldsByItemId = datasetService.getFieldsByItemIdsOrderByPosition(allItemIds);
        datasets.forEach(dataset ->
            dataset.getItems().forEach(item -> {
                List<DatasetField> fields = fieldsByItemId.get(item.getId());
                if (fields != null) {
                    item.setFieldsOrderByPosition(fields);
                }
            })
        );
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
