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

package org.bfabric.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Department;
import org.bfabric.entity.Division;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Membership;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderAttribute;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Organization;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceTypeCollection;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class OrderService extends AbstractContainerService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    @Inject
    private ContainerService containerService;

    @Inject
    private InstrumentDataDeliveryService instrumentDataDeliveryService;

    @Inject
    private PlateService plateService;

    @Inject
    private SampleService sampleService;

    @Inject
    private ServiceTypeCollectionService serviceTypeCollectionService;

    public OrderService() {
        super(Order.class);
    }

    private String addBioinformaticianCondition(String filter) {
        if (filter != null) {
            if (!filter.isEmpty()) {
                filter += " OR ";
            }
            filter += "entity.bioinformatician = :user";
        }
        return filter;
    }

    private String addCoachCondition(String filter) {
        if (filter != null) {
            if (!filter.isEmpty()) {
                filter += " OR ";
            }
            filter += "entity.coach = :user";
        }
        return filter;
    }

    private String addProjectCoachCondition(String filter, User user) {
        if (filter != null && !user.getCoachedProjects().isEmpty()) {
            if (!filter.isEmpty()) {
                filter += " OR ";
            }
            filter += "entity.project IN (:coachedProjects)";
        }
        return filter;
    }

    private String addRequesterCondition(String filter) {
        if (filter != null) {
            if (!filter.isEmpty()) {
                filter += " OR ";
            }
            filter += "entity.requester = :user";
        }
        return filter;
    }

    private String addServiceAreaCondition(String filter, User user) {
        if (filter != null && !user.getServiceAreas().isEmpty()) {
            if (!filter.isEmpty()) {
                filter += " OR ";
            }
            filter += "entity.serviceType.serviceArea IN (:serviceAreas)";
        }
        return filter;
    }

    private String addServiceCondition(String filter, User user) {
        if (filter != null && !user.getServices().isEmpty()) {
            if (!filter.isEmpty()) {
                filter += " OR ";
            }
            filter += "exists (select orderItem.id from entity.orderItems orderItem where orderItem.service IN (:services))";
        }
        return filter;
    }

    private String addServiceTypeCondition(String filter, User user) {
        if (filter != null && !user.getServiceTypesForTasks().isEmpty()) {
            if (!filter.isEmpty()) {
                filter += " OR ";
            }
            filter += "entity.serviceType IN (:serviceTypes)";
        }
        return filter;
    }

    public void cancel(Order order) {
        if (order != null) {
            order.changeStatus(StatusEnum.CANCELED);
            save(order);
        }
    }

    public Map<String, Set<String>> changeStatus(Order order, StatusEnum statusEnum) {
        if (order.isManaged()) {
            Set<Mail> mails = order.changeStatus(statusEnum);
            save(order);
            flush();
            // Set the custom status to 'Waiting For Sample QC' if the processing has started and the initialCustomStatus of the service type = TRUE.
            if (StatusEnum.PROCESSING.equals(statusEnum) && order.isInitialCustomStatus()) {
                containerService.changeCustomStatus(find(Order.class, order.getId()), Constants.CUSTOM_ORDER_STATE_WAITING_FOR_SAMPLE_QC);
            }
            if (StatusEnum.ACCEPTED.equals(statusEnum)) {
                userService.addRoleUserAndSynchronizeWithAD(order);
            }
            // Set the status of the plates to 'Finished' if the user submits the order which processes plates.
            if (StatusEnum.SUBMITTED.equals(statusEnum) && order.isProcessesPlates()) {
                for (OrderItem orderItem : order.getOrderItems()) {
                    if (orderItem.getPlate().isPlateTypeUserSubmitted()) {
                        plateService.changeStatus(orderItem.getPlate(), StatusEnum.READY, false);
                    }
                }
            }
            Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("order") + " " + statusEnum.getLabel());
            facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
            if (!mails.isEmpty()) {
                setCustomContainerStatusSentMail(order);
            }
            return facesMessages;
        }
        String msg = Messages.get("changeOrderStatusException").replace("{0}", statusEnum.getLabel());
        logger.info(msg);
        return createDisplayFacesMessagesMap(msg);
    }

    public BfabricLazyDataModel<Order> getAcceptOrderRevisionTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.REVISED, user);
    }

    public BfabricLazyDataModel<Order> getAcceptOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.SUBMITTED, user);
    }

    public BfabricLazyDataModel<Order> getBioinformaticianOrdersLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.bioinformatician.id = :bioinformaticianId");
        entityQuery.addParameter("bioinformaticianId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getCheckOrderStatusTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        if (!user.hasRoleImplicit(RoleEnum.ADMIN)) {
            entityQuery.addWhereClause("entity.coach.id = :coachId");
            entityQuery.addParameter("coachId", user.getId());
        }
        entityQuery.addWhereClause("entity.status not in :finalStates and not exists(select cs from ContainerStatus cs where cs.container.id = entity.id and cs.created > :dateLimit)");
        entityQuery.addParameter("finalStates", Arrays.asList(StatusEnum.CLOSED, StatusEnum.CANCELED, StatusEnum.FINISHED));
        entityQuery.addParameter("dateLimit", LocalDateTime.now().minusMonths(3));
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getCloseOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.FINISHED, user);
    }

    public List<String> getCurrentCustomOrderStatesFiltered(String filterString) {
        return super.getCurrentCustomContainerStatesFiltered(filterString, Constants.ORDER);
    }

    private String getExtendedFilterCondition(User user, boolean includeRequester, boolean includeBioinformatician) {
        String extendedFilterCondition = Constants.EMPTY_STRING;
        if (!user.hasRoleImplicit(RoleEnum.ADMIN)) {
            if (includeRequester) {
                extendedFilterCondition = addRequesterCondition(extendedFilterCondition);
            }
            if (includeBioinformatician) {
                extendedFilterCondition = addBioinformaticianCondition(extendedFilterCondition);
            }
            extendedFilterCondition = addCoachCondition(extendedFilterCondition);
            extendedFilterCondition = addProjectCoachCondition(extendedFilterCondition, user);
            extendedFilterCondition = addServiceAreaCondition(extendedFilterCondition, user);
            extendedFilterCondition = addServiceTypeCondition(extendedFilterCondition, user);
            extendedFilterCondition = addServiceCondition(extendedFilterCondition, user);
            if (!extendedFilterCondition.isEmpty()) {
                extendedFilterCondition = " AND (" + extendedFilterCondition + ")";
            }
        }
        return extendedFilterCondition;
    }

    public BfabricLazyDataModel<Order> getFinishAnalyzingOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.ANALYZING, user);
    }

    public BfabricLazyDataModel<Order> getFinishOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.ANALYZED, user);
    }

    public BfabricLazyDataModel<Order> getFinishProcessingOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.PROCESSING, user);
    }

    public BfabricLazyDataModel<Order> getLazyModelByCompanyId(long companyId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.division.company.id = :companyId");
        entityQuery.addParameter("companyId", companyId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getLazyModelByDepartmentId(long departmentId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.institute.department.id = :departmentId");
        entityQuery.addParameter("departmentId", departmentId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getLazyModelByOrganizationId(long organizationId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.institute.department.organization.id = :organizationId");
        entityQuery.addParameter("organizationId", organizationId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getLazyModelBySampleTypeId(long sampleTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.sampleType.id = :sampleTypeId");
        entityQuery.addParameter("sampleTypeId", sampleTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getLazyModelByServiceAreaId(long serviceAreaId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.serviceType.serviceArea.id = :serviceAreaId");
        entityQuery.addParameter("serviceAreaId", serviceAreaId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getLazyModelByServiceId(long serviceId) {
        return (BfabricLazyDataModel<Order>) getLazyModelUnnestById("orderItems", "service", serviceId);
    }

    @Override
    public BfabricLazyDataModel<Order> getLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        if (userId > 0) {
            entityQuery
                .addWhereClause("EXISTS(SELECT membership.id FROM Membership membership WHERE membership.user.id = :userId AND (membership.container = entity or membership.container = entity.project) and membership.discriminator = :discriminator)");
            entityQuery.addParameter("userId", userId);
            entityQuery.addParameter("discriminator", Membership.DISCRIMINATOR_CURRENT);
        }

        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getOngoingOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(null, user);
    }

    public List<StatusEnum> getOrderStatusEnums() {
        return StatusEnum.getStatusEnums(Order.class);
    }

    public List<Order> getOrdersByDepartment(Department department) {
        return createNamedQuery("Order.findByDepartment").setParameter("department", department).getResultList();
    }

    public List<Order> getOrdersByOrganization(Organization organization) {
        return createNamedQuery("Order.findByOrganization").setParameter("organization", organization).getResultList();
    }

    public List<Order> getOrdersByService(Service service) {
        return createNamedQuery("Order.findByService").setParameter("service", service).getResultList();
    }

    public List<Order> getOrdersByServiceArea(ServiceArea serviceArea) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.serviceType.serviceArea.id = :serviceAreaId");
        entityQuery.addParameter("serviceAreaId", serviceArea.getId());
        return (List<Order>) entityQuery.getResultList();
    }

    public List<Order> getOrdersFiltered(String filterString) {
        return (List<Order>) createEntityQueryFiltered(filterString).getResultList();
    }

    public BfabricLazyDataModel<Order> getOrdersLazyModelBySamplePreparationProtocolId(long samplePreparationProtocolId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.libraryProtocol.id = :samplePreparationProtocolId");
        entityQuery.addParameter("samplePreparationProtocolId", samplePreparationProtocolId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Order> getOrdersTransitiveByUser(User user) {
        return createNamedQuery("Order.findAllTransitiveByUser").setParameter("userId", user.getId()).setParameter("discriminator", Membership.DISCRIMINATOR_CURRENT).getResultList();
    }

    public BfabricLazyDataModel<Order> getOrdersViewLazyModel(String serviceTypeName, ServiceTypeCollection serviceTypeCollection) {
        EntityQuery entityQuery = createEntityQuery();
        if (StringHelper.isNotEmpty(serviceTypeName)) {
            entityQuery.addWhereClause("entity.serviceType.name = :serviceTypeName");
            entityQuery.addParameter("serviceTypeName", serviceTypeName);
        } else {
            if (serviceTypeCollection != null) {
                entityQuery.addWhereClause("entity.serviceType IN (:serviceTypes)");
                entityQuery.addParameter("serviceTypes", serviceTypeCollection.getServiceTypes());
            }
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Order> getPendingOrdersToBeCanceled() {
        return createNamedQuery("Order.findPendingOrdersToBeCanceled").setParameter("creationDate", LocalDateTime.now().minusWeeks(8)).getResultList();
    }

    public List<Order> getPendingOrdersToBeReminded() {
        return createNamedQuery("Order.findPendingOrdersByCreated").setParameter("creationDate", LocalDateTime.now().minusWeeks(6)).getResultList();
    }

    public BfabricLazyDataModel<Order> getReassignOrderBioinformaticianTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("bioinformatician.empDegree IS NULL AND status NOT IN ('CANCELED', 'FINISHED', 'CLOSED')");
        if (user != null && !user.hasRoleImplicit(RoleEnum.REVIEWMANAGER)) {
            entityQuery.addWhereClause("serviceType.coach.id = " + user.getIdString());
        }
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getReassignOrderCoachBackupTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("coachBackup.empDegree IS NULL AND status NOT IN ('CANCELED', 'FINISHED', 'CLOSED')");
        if (user != null && !user.hasRoleImplicit(RoleEnum.REVIEWMANAGER)) {
            entityQuery.addWhereClause("serviceType.coach.id = " + user.getIdString());
        }
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getReassignOrderCoachTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("coach.empDegree IS NULL AND status NOT IN ('CANCELED', 'FINISHED', 'CLOSED')");
        if (user != null && !user.hasRoleImplicit(RoleEnum.REVIEWMANAGER)) {
            entityQuery.addWhereClause("serviceType.coach.id = " + user.getIdString());
        }
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Order> getSendOrderSampleTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.ACCEPTED, user);
    }

    public BfabricLazyDataModel<Order> getStartAnalyzingOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.PROCESSED, user);
    }

    public BfabricLazyDataModel<Order> getStartProcessingOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.ARRIVED, user);
    }

    public BfabricLazyDataModel<Order> getSubmitOrderTasks(User user) {
        return getTasksOfCurrentUserByStatus(StatusEnum.PENDING, user);
    }

    private BfabricLazyDataModel<Order> getTasksOfCurrentUserByStatus(StatusEnum statusEnum, User user) {
        String filterCoachesOnly = getExtendedFilterCondition(user, false, false);
        String filterRequesterIncluded = getExtendedFilterCondition(user, true, false);
        String filterBioinformaticianIncluded = getExtendedFilterCondition(user, false, true);

        String where = null;
        if (StatusEnum.PENDING.equals(statusEnum)) {
            // Submit order.
            where = "status = '" + statusEnum + "'" + filterRequesterIncluded;
        } else if (StatusEnum.SUBMITTED.equals(statusEnum)) {
            // Accept order.
            where = "(status = '" + statusEnum + "' OR status = '" + StatusEnum.ARRIVED + "' AND NOT EXISTS (select id from ContainerStatus cs where cs.container.id = entity.id AND UPPER(cs.name) = '" + StatusEnum.ACCEPTED + "'))" + filterCoachesOnly;
        } else if (StatusEnum.REVISED.equals(statusEnum)) {
            // Accept revision.
            where = "status = '" + statusEnum + "'" + filterRequesterIncluded;
        } else if (StatusEnum.REVISIONACCEPTED.equals(statusEnum)) {
            // Accept order.
            where = "status = '" + statusEnum + "'" + filterCoachesOnly;
        } else if (StatusEnum.ACCEPTED.equals(statusEnum)) {
            // Send samples.
            where = "(status = '" + StatusEnum.ACCEPTED + "' AND NOT EXISTS (select id from ContainerStatus cs where cs.container.id = entity.id AND UPPER(cs.name) = '" + StatusEnum.ARRIVED + "')";
            where += filterRequesterIncluded + ")";
        } else if (StatusEnum.ARRIVED.equals(statusEnum)) {
            // Start Processing.
            where = "(status = '" + StatusEnum.ARRIVED + "' AND EXISTS (select id from ContainerStatus cs where cs.container.id = entity.id AND UPPER(cs.name) = '" + StatusEnum.ACCEPTED + "')";
            where += " OR ";
            where += "status = '" + StatusEnum.ACCEPTED + "' AND EXISTS (select id from ContainerStatus cs where cs.container.id = entity.id AND UPPER(cs.name) = '" + StatusEnum.ARRIVED + "'))";
            where += filterCoachesOnly;
        } else if (StatusEnum.PROCESSING.equals(statusEnum)) {
            // Finish processing.
            where = "status = '" + statusEnum + "'" + filterCoachesOnly;
        } else if (StatusEnum.PROCESSED.equals(statusEnum)) {
            // Start analyzing.
            where = "status = '" + statusEnum + "'" + filterBioinformaticianIncluded;
        } else if (StatusEnum.ANALYZING.equals(statusEnum)) {
            // Finish analyzing.
            where = "status = '" + statusEnum + "'" + filterBioinformaticianIncluded;
        } else if (StatusEnum.ANALYZED.equals(statusEnum)) {
            // Finish order.
            where = "status = '" + statusEnum + "'" + filterBioinformaticianIncluded;
        } else if (StatusEnum.FINISHED.equals(statusEnum) && (user.hasRoleImplicit(RoleEnum.BOOKINGMANAGER) || user.hasRoleImplicit(RoleEnum.SERVICEMANAGER))) {
            // Close order.
            where = "status = '" + StatusEnum.REOPENED + "' AND statusModifiedBy = :user";
            if (user.hasRoleImplicit(RoleEnum.BOOKINGMANAGER)) {
                where += " or status = '" + statusEnum + "'";
            }
        } else if (statusEnum == null) {
            // Ongoing order.
            where = "status in ('" + StatusEnum.PENDING + "', '" + StatusEnum.SUBMITTED + "', '" + StatusEnum.REVISED + "', '" + StatusEnum.REVISIONACCEPTED + "', '" + StatusEnum.ACCEPTED + "', '" + StatusEnum.ARRIVED + "', '" + StatusEnum.PROCESSING + "', '" + StatusEnum.PROCESSED + "', '" + StatusEnum.ANALYZING + "', '" + StatusEnum.ANALYZED + "')" + filterBioinformaticianIncluded;
        }

        BfabricLazyDataModel<Order> lazyDataModel = null;
        if (StringHelper.isNotEmpty(where)) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause(where);
            if (where.contains(":user")) {
                entityQuery.addParameter("user", user);
            }
            if (where.contains(":coachedProjects")) {
                entityQuery.addParameter("coachedProjects", user.getCoachedProjects());
            }
            if (where.contains(":serviceTypes")) {
                entityQuery.addParameter("serviceTypes", user.getServiceTypesForTasks());
            }
            if (where.contains(":serviceAreas")) {
                entityQuery.addParameter("serviceAreas", user.getServiceAreas());
            }
            if (where.contains(":services")) {
                entityQuery.addParameter("services", user.getServices());
            }
            lazyDataModel = new BfabricLazyDataModel<>(entityQuery);
        }
        return lazyDataModel;
    }

    public BigInteger getUserSamplesCount(Long orderId) {
        List<BigInteger> ret = createNativeQuery("SELECT userSamples FROM orderUserSamplesCount WHERE orderId = :orderId").setParameter("orderId", orderId).setMaxResults(1).getResultList();
        return ret.isEmpty() ? BigInteger.ZERO : ret.get(0);
    }

    public boolean hasSameServiceSampleCombination(Long orderId) {
        return !createNamedQuery("Order.hasSameServiceSampleCombination").setParameter("orderId", orderId).setMaxResults(1).getResultList().isEmpty();
    }

    public LinkedHashMap<String, String> isValid(Order order) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>(isValidCoaching(order));

        if (order.getTechnologies().isEmpty() && order.getServiceType() != null) {
            order.addTechnologies(order.getServiceType().getTechnologies());
        }

        if (order.getServiceType() == null) {
            validationErrorMsg.put(Constants.EDIT + ":" + Constants.SERVICE_SELECTION + "table", Constants.REQUIRED);
        } else if (order.getServiceType().isRequiresProject() && order.getProject() == null) {
            validationErrorMsg.put(Constants.EDIT + ":projectrequiredautocomplete", Constants.REQUIRED);
        } else {
            if (!order.getServiceType().getOrderAttributes().isEmpty()) {
                if (order.getServiceType().isOrderAttribute("sequencingApplication") && order.getSequencingApplication() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":sequencingApplication", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("instrument") && order.getInstrument() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":instrument", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("libraryProtocol") && (order.getInstrument() != null || order.getSequencingApplication() != null) && !order
                    .hasServiceTypeReadyMadeLibrariesSequencingAndSequencingApplicationCustomOther() && order.getLibraryProtocol() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":libraryProtocol", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("numberOfRunsSequencing") && order.getNumberOfRunsSequencing() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":numberOfRunsSequencing", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("numberOfSamples") && !order.isProcessesPlates()) {
                    if (order.getNumberOfSamples() == null) {
                        validationErrorMsg.put(Constants.EDIT + ":numberOfSamples", Constants.REQUIRED);
                    } else {
                        int maxBatchEditItems = ConfigurationHelper.getConfiguration().getMaxBatchEditItems();
                        if (order.getNumberOfSamples() > maxBatchEditItems) {
                            validationErrorMsg.put(Constants.EDIT + ":numberOfSamples", Messages.get("numberOfSamplesLimitExceeded").replace("{0}", String.valueOf(maxBatchEditItems)));
                        }
                    }
                }

                if (order.getServiceType().isOrderAttribute("numberOfPlates") && order.isProcessesPlates()) {
                    if (order.getNumberOfPlates() == null) {
                        validationErrorMsg.put(Constants.EDIT + ":numberOfPlates", Constants.REQUIRED);
                    } else {
                        if (order.getNumberOfPlates() > getConfiguration().getMaxBatchEditItemsPlates()) {
                            validationErrorMsg
                                .put(Constants.EDIT + ":numberOfPlates", Messages.get("numberOfPlatesLimitExceeded")
                                    .replace("{0}", String.valueOf(getConfiguration().getMaxBatchEditItemsPlates())));
                        }
                    }
                }

                OrderAttribute nuclei = order.getServiceType().getOrderAttribute("nuclei");
                OrderAttribute numberOfCellsNuclei = order.getServiceType().getOrderAttribute("numberOfCellsNuclei");
                if (nuclei != null && (numberOfCellsNuclei == null || !numberOfCellsNuclei.isEnabled() && order.getNumberOfCellsNucleiOld() == null) && order.getNuclei() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":nuclei", Constants.REQUIRED);
                }

                if (numberOfCellsNuclei != null && (nuclei == null || order.getNuclei() == null)) {
                    if (order.getNumberOfCellsNuclei() == null) {
                        validationErrorMsg.put(Constants.EDIT + ":numberOfCellsNuclei", Constants.REQUIRED);
                    } else {
                        // numeric(12,2)
                        String numberOfCellsNucleiValue = String.valueOf(order.getNumberOfCellsNuclei());

                        if (numberOfCellsNucleiValue.length() > 13) {
                            validationErrorMsg.put(Constants.EDIT + ":numberOfCellsNuclei", Messages.get("maxLength12"));
                        } else if (numberOfCellsNucleiValue.contains(".") && !(numberOfCellsNucleiValue.indexOf(".") == numberOfCellsNucleiValue.length() - 2 || numberOfCellsNucleiValue
                            .indexOf(".") == numberOfCellsNucleiValue.length() - 3)) {
                            validationErrorMsg.put(Constants.EDIT + ":numberOfCellsNuclei", Messages.get("max2FractionDigits"));
                        }
                    }
                }

                if (order.getServiceType().isOrderAttribute("instrumentDataDelivery") && order.getInstrument() != null && order.getInstrumentDataDelivery() == null && !instrumentDataDeliveryService
                    .getResultListEnabledIncludingByInstrumentId(order.getInstrument().getId(), 0).isEmpty()) {
                    validationErrorMsg.put(Constants.EDIT + ":instrumentDataDelivery", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("totalNumberOfInstrumentDataPackages") && order.isInstrumentDataPackageRequired()) {
                    if (order.getTotalNumberOfInstrumentDataPackages() == null) {
                        validationErrorMsg.put(Constants.EDIT + ":totalNumberOfInstrumentDataPackages", Constants.REQUIRED);
                    } else {
                        // numeric(12,2)
                        String totalNumberOfInstrumentDataPackages = String.valueOf(order.getTotalNumberOfInstrumentDataPackages());

                        if (totalNumberOfInstrumentDataPackages.length() > 13) {
                            validationErrorMsg.put(Constants.EDIT + ":totalNumberOfInstrumentDataPackages", Messages.get("maxLength12"));
                        } else if (totalNumberOfInstrumentDataPackages.contains(".") && !(totalNumberOfInstrumentDataPackages.indexOf(".") == totalNumberOfInstrumentDataPackages
                            .length() - 2 || totalNumberOfInstrumentDataPackages
                            .indexOf(".") == totalNumberOfInstrumentDataPackages.length() - 3)) {
                            validationErrorMsg.put(Constants.EDIT + ":totalNumberOfInstrumentDataPackages", Messages.get("max2FractionDigits"));
                        }
                    }
                }

                if (order.getServiceType().isOrderAttribute("instrumentDataPackage") && order.isInstrumentDataPackageRequired() && order.getInstrumentDataPackage() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":instrumentDataPackage", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("instrumentReadConfiguration") && order.getInstrument() != null && !order.getInstrument().getReadConfigurations().isEmpty() && order
                    .getInstrumentReadConfiguration() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":instrumentReadConfiguration", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("numberOfChips") && order.getNumberOfChips() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":numberOfChips", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("dataProduced") && order.getDataProduced() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":dataProduced", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("hoursRequested") && order.getHoursRequested() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":hoursRequested", Constants.REQUIRED);
                }

                if (order.getServiceType().isOrderAttribute("storageModel") && order.getStorageModel() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":storageModel", Constants.REQUIRED);
                }
            }

            validationErrorMsg.putAll(isValidVatAndReferenceNumber(order));

            if (order.isRenderedFinanceSource() && order.getCostCentre() == null && order.getPspElement() == null) {
                validationErrorMsg.put(Constants.EDIT + ":financeSourceGrid", Constants.REQUIRED);
            }
        }

        validationErrorMsg.putAll(isValidCustomAttributes(order));
        return validationErrorMsg;
    }

    private Set<String> postPersist(Order order, User currentUser, Set<Mail> mails) {
        Set<String> errorMsg = new HashSet<>();
        if (order.getProject() == null) {
            // Grant the manager role to the budget officer and requester.
            errorMsg.addAll(addManager(order, find(User.class, order.getBudgetOfficer().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
            errorMsg.addAll(addManager(order, find(User.class, order.getRequester().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
            errorMsg.addAll(addManager(order, find(User.class, order.getContact().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
        }
        return errorMsg;
    }

    private Set<String> postUpdate(Order order, User currentUser, Set<Mail> mails) {
        Set<String> errorMsg = super.postUpdate(order, currentUser, mails);

        if (order.isCoachChanged() || order.isCoachBackupChanged() || order.isBioinformaticianChanged()) {
            // Send mail to inform that coaching has changed.
            mails.add(order.createMail(MailTypeEnum.CONTAINER_COACH_CHANGED));
        }

        return errorMsg;
    }

    public void remove(Order order) {
        Set<Sample> samplesToDelete = new HashSet<>();
        Set<Sample> samplesOnPlateToDelete = new HashSet<>();
        Set<Plate> platesToDelete = new HashSet<>();
        Set<OrderItem> orderItemsToDelete = order.getOrderItems();

        for (OrderItem orderItem : orderItemsToDelete) {
            if (!order.isProcessesPlates()) {
                if (!orderItem.getSample().getContainer().isContainerProject() && orderItem.getSample().isDeletableUponContainerDeletion(order)) {
                    samplesToDelete.add(orderItem.getSample());
                }
            } else {
                if (orderItem.getPlate().getSamplePlatePositions().isEmpty()) {
                    platesToDelete.add(orderItem.getPlate());
                    samplesOnPlateToDelete.addAll(orderItem.getPlate().getSamples());
                }
            }
        }

        for (OrderItem orderItem : orderItemsToDelete) {
            orderItem.setSample(null);
            orderItem.setPlate(null);
            remove(orderItem);
        }

        for (Plate plateToDelete : platesToDelete) {
            plateService.remove(plateToDelete);
        }

        for (Sample sample : samplesToDelete) {
            sample.getOrderItems().clear();
            sampleService.remove(sample);
        }

        for (Sample sample : order.getSamples()) {
            if (!samplesToDelete.contains(sample) && !samplesOnPlateToDelete.contains(sample)) {
                sampleService.remove(sample);
            }
        }

        order.getOrderItems().clear();
        order.getSamples().clear();
        super.remove(order);
    }

    public void save(Order order) {
        if (order != null) {
            order.resetLibraryProtocolOptions();
            super.save(order);
        }
    }

    public Set<String> save(Order order, User currentUser, Set<Offer> updatedOffers) {
        boolean isManaged = order.isManaged();

        if (isManaged) {
            order.resetNumberOfSamplesPlates(false);
        }
        if (order.getId() == 0) {
            order.setCreateAndAddStatus(order.getStatus());
        }
        // Set the billing division.
        if (order.getOrganizationType() != null && order.getOrganizationType().isCompany()) {
            final Division division = affiliationHelperService.saveDivisionIfNotExists(order.getOrganizationType(), order.getCompanyName(), order.getDivisionName());
            order.setDivision(division);
        }

        if (updatedOffers != null) {
            for (Offer offer : updatedOffers) {
                super.save(offer);
            }
        }

        save(order);

        Set<Mail> mails = new HashSet<>();
        Set<String> errorMsg;
        if (isManaged) {
            errorMsg = postUpdate(order, currentUser, mails);
        } else {
            errorMsg = postPersist(order, currentUser, mails);
        }

        errorMsg.addAll(mailSendService.sendMails(mails));
        if (!isManaged) {
            setCustomContainerStatusSentMail(order);
        }
        return errorMsg;
    }
}