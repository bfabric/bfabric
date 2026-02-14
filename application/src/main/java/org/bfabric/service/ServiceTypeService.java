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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Order;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class ServiceTypeService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    public ServiceTypeService() {
        super(ServiceType.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final ServiceType serviceType = (ServiceType) entity;
        return createNamedQuery("ServiceType.checkUniqueName").setParameter("name", serviceType.getName()).setParameter("id", serviceType.getId())
            .setParameter("serviceArea", serviceType.getServiceArea()).setMaxResults(1).getResultList().isEmpty();
    }

    public void clearPrices(ServiceType serviceType) {
        if (serviceType != null) {
            for (Service service : serviceType.getServices()) {
                if (service.isClearPricesRendered() || service.getFullCost() != null) {
                    service.clearPrices();
                    super.save(service);
                }
            }
        }
    }

    public List<ServiceType> getEnabledServiceTypesByOrder(Order order) {
        if (order != null) {
            if (!order.getOrderItems().isEmpty() && order.getSampleType() != null) {
                return createNamedQuery("ServiceType.findEnabledIncludingBySampleType").setParameter("entity", order.getServiceType()).setParameter("requiresProject", order.isRequiresProject())
                    .setParameter("sampleType", order.getSampleType()).setParameter("processesPlates", order.isProcessesPlates()).setParameter("serviceColumnEnabled", order.isServiceColumnEnabled())
                    .setParameter("internal", order.isInternal()).getResultList();
            }
            return createNamedQuery("ServiceType.findEnabledIncludingAndSampleTypeAssociated").setParameter("entity", order.getServiceType()).setParameter("internal", order.isInternal())
                .getResultList();
        }
        return null;
    }

    public List<ServiceType> getEnabledServiceTypesIncludingServiceTypeByServiceArea(ServiceArea serviceArea, ServiceType serviceType) {
        return createNamedQuery("ServiceType.findEnabledIncludingByServiceArea").setParameter("serviceArea", serviceArea).setParameter("entity", serviceType).getResultList();
    }

    public List<ServiceType> getPossibleServiceTypes(String filterString, Collection<ServiceType> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        entityQuery.setMaxResult(100);
        return (List<ServiceType>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<ServiceType> getReassignServiceTypeCoachBackupTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery
            .addWhereClause("enabled = true AND coachBackup IS NOT NULL AND EXISTS (select id from ServiceArea where id = serviceArea.id AND enabled = true) AND NOT EXISTS (SELECT user FROM User user JOIN user.roles role WHERE user = coachBackup AND role.name = 'employee')");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<ServiceType> getReassignServiceTypeCoachTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery
            .addWhereClause("enabled = true AND coach IS NOT NULL AND EXISTS (select id from ServiceArea where id = serviceArea.id AND enabled = true) AND NOT EXISTS (SELECT user FROM User user JOIN user.roles role WHERE user = coach AND role.name = 'employee')");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    @Override
    public List<ServiceType> getResultList() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setOrder("name");
        return (List<ServiceType>) entityQuery.getResultList();
    }

    public List<ServiceType> getServiceTypes(String filterString, Collection<ServiceType> included, Collection<ServiceType> excluded) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addInEntitiesClause(included);
        entityQuery.addNotInEntitiesClause(excluded, "OR");
        entityQuery.addIdOrNameWhereClause(filterString);
        return (List<ServiceType>) entityQuery.getResultList();
    }

    public List<ServiceType> getServiceTypesByName(String name) {
        return createNamedQuery("ServiceType.findByName").setParameter("name", name).getResultList();
    }

    public List<ServiceType> getServiceTypesByServiceAreaNameOfEnabledServicesIncludingService(Service service, String serviceAreaFilter) {
        return createNamedQuery("ServiceType.findByEnabledServicesAndServiceAreaNameIncludingService").setParameter("serviceId", service != null ? service.getId() : null)
            .setParameter("serviceAreaFilter", serviceAreaFilter).getResultList();
    }

    public List<ServiceType> getServiceTypesEnabledAndByServiceAreaNameAndEnabledForOfferServicesIncludingService(Service service, String serviceAreaFilter) {
        return createNamedQuery("ServiceType.findByEnabledAndServiceAreaNameAndEnabledForOfferServicesIncludingService").setParameter("serviceId", service != null ? service.getId() : null)
            .setParameter("serviceAreaFilter", serviceAreaFilter).getResultList();
    }

    public List<ServiceType> getServiceTypesEnabledAndEnabledForOfferServicesIncludingService(Service service) {
        return createNamedQuery("ServiceType.findByEnabledAndEnabledForOfferServicesIncludingService").setParameter("serviceId", service != null ? service.getId() : null).getResultList();
    }

    public List<ServiceType> getServiceTypesOfEnabledServicesIncludingService(Service service) {
        return createNamedQuery("ServiceType.findByEnabledServicesIncludingService").setParameter("serviceId", service != null ? service.getId() : null).getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final ServiceType serviceType = (ServiceType) entity;
        LinkedHashMap<String, String> validationErrorMsg = isValidName(serviceType, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "service area")
            .replace("{1}", serviceType.getServiceArea().getName()));

        if (serviceType.getCoach() == null) {
            validationErrorMsg.put(Constants.EDIT + ":coachautocomplete", Constants.REQUIRED);
        } else if (serviceType.getCoachBackup() != null && serviceType.getCoachBackup().equals(serviceType.getCoach())) {
            validationErrorMsg.put(Constants.EDIT + ":coachBackupautocomplete", Messages.get("coachSameException"));
        }

        if (serviceType.getOrders().isEmpty() && serviceType.isProcessesSamples() && serviceType.getSampleType() == null) {
            validationErrorMsg.put(Constants.EDIT + ":sampleType", Constants.REQUIRED);
        }

        return validationErrorMsg;
    }

    public void merge(ServiceType serviceType, ServiceType merged, ServiceType mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            serviceType.setName(mergeSelection.getName());
            serviceType.setServiceArea(mergeSelection.getServiceArea());
            serviceType.setSampleType(mergeSelection.getSampleType());
            serviceType.setCoach(mergeSelection.getCoach());
            serviceType.setCoachBackup(mergeSelection.getCoachBackup());
            serviceType.setRequiresProject(mergeSelection.isRequiresProject());
            serviceType.setProcessesSamples(mergeSelection.isProcessesSamples());
            serviceType.setServiceColumnEnabled(mergeSelection.isServiceColumnEnabled());
            serviceType.setInstructionLink(mergeSelection.getInstructionLink());
            serviceType.setEnabled(mergeSelection.isEnabled());
            serviceType.setOrderPosition(mergeSelection.getOrderPosition());

            // Merge the one-to-many associations. IMPORTANT: Do this before merging the many-to-many associations.
            reassign(Order.class, merged, serviceType, "serviceType");
            reassign(SequencingApplication.class, merged, serviceType, "serviceType");
            reassign(Service.class, merged, serviceType, "serviceType");

            // Merge the many-to-many associations.
            if (!merged.getInstruments().isEmpty()) {
                serviceType.getInstruments().addAll(merged.getInstruments());
                merged.getInstruments().clear();
            }

            if (!merged.getOrderAttributes().isEmpty()) {
                serviceType.getOrderAttributes().addAll(merged.getOrderAttributes());
                merged.getOrderAttributes().clear();
            }

            if (!merged.getUsers().isEmpty()) {
                serviceType.getUsers().addAll(merged.getUsers());
                merged.getUsers().clear();
            }

            if (!merged.getWorkflowTemplates().isEmpty()) {
                serviceType.getWorkflowTemplates().addAll(merged.getWorkflowTemplates());
                merged.getWorkflowTemplates().clear();
            }

            saveMerge(serviceType, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void reassign(Class<?> clazz, ServiceType oldServiceType, ServiceType newServiceType, String attribute) {
        if (clazz != null && oldServiceType != null && newServiceType != null && attribute != null) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE ").append(clazz.getSimpleName()).append(" SET ").append(attribute).append(" = :new WHERE ").append(attribute).append(" = :old");
            if (clazz.equals(ServiceType.class)) {
                queryBuilder.append(" AND id <> :id");
            }

            Query query = createQuery(queryBuilder.toString()).setParameter("old", oldServiceType).setParameter("new", newServiceType);
            if (clazz.equals(ServiceType.class)) {
                query.setParameter("id", oldServiceType.getId());
            }

            query.executeUpdate();
        }
    }

    public void save(ServiceType serviceType, boolean isCoachChanged, boolean isCoachBackupChanged) {
        if (!serviceType.isProcessesSamples()) {
            serviceType.setSampleType(null);
        }

        super.save(serviceType);

        // Save services in case of cloning or when propagation is required.
        if (serviceType.isCloned()) {
            for (Service service : serviceType.getServices()) {
                super.save(service);
            }
        } else if (serviceType.isPropagateEnabled()) {
            serviceType.propagateEnabled();
            for (Service service : serviceType.getServices()) {
                if (service.isEnabledChanged()) {
                    super.save(service);
                }
            }
        }

        // Send mail.
        if (isCoachChanged || isCoachBackupChanged) {
            final Mail mail = new Mail();
            mail.setParent(serviceType);
            mail.setType(MailTypeEnum.SERVICETYPE_COACH_CHANGED, Messages.get("serviceType") + " " + serviceType.getId());
            mail.setInput("coachChanged", isCoachChanged);
            mail.setInput("coachBackupChanged", isCoachBackupChanged);
            mail.setInput("bioinformaticianChanged", false);
            mail.setRecipient(serviceType.getCoach());
            mail.addRecipient(serviceType.getCoachBackup());
            if (!mail.getRecipients().isEmpty()) {
                mail.setInput("entity", serviceType);
                mail.setInput("entityLabel", Messages.get("serviceType").replaceAll(" ", Constants.EMPTY_STRING).toLowerCase());
                mail.setInput("entityDisplayLabel", Messages.get("serviceType"));
                mailSendService.send(mail);
            }
        }
    }
}
