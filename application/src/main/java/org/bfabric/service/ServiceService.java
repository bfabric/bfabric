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

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceOrganizationTypePrice;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class ServiceService extends AbstractService {

    private static final long serialVersionUID = 1;

    public ServiceService() {
        super(Service.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Service service = (Service) entity;
        return createNamedQuery("Service.checkUniqueName").setParameter("name", service.getName()).setParameter("id", service.getId()).setParameter("serviceType", service.getServiceType())
            .setMaxResults(1).getResultList().isEmpty();
    }

    public List<Service> getAvailableTrackableServices(String filterString, List<Service> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        if (StringHelper.isNotEmpty(filterString)) {
            entityQuery.addWhereClauseDisjunctive("LOWER(serviceType.name) LIKE :filterString OR LOWER(serviceType.serviceArea.name) LIKE :filterString");
        }
        entityQuery.setOrder("orderPosition");
        return (List<Service>) entityQuery.getResultList();
    }

    public long getCountEnabledServicesByServiceArea(ServiceArea serviceArea) {
        return (long) createNamedQuery("Service.countEnabledByServiceArea").setParameter("serviceArea", serviceArea).getSingleResult();
    }

    public long getCountServicesByServiceArea(ServiceArea serviceArea) {
        return (long) createNamedQuery("Service.countByServiceArea").setParameter("serviceArea", serviceArea).getSingleResult();
    }

    public List<Service> getEnabledServicesByServiceArea(ServiceArea serviceArea) {
        return createNamedQuery("Service.findEnabledByServiceArea").setParameter("serviceArea", serviceArea).getResultList();
    }

    public List<Service> getEnabledServicesForOfferIncluding(long serviceId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("enabled = true and serviceType.serviceArea.enabledForOffer = true or id = :serviceId");
        entityQuery.addParameter("serviceId", serviceId);
        entityQuery.setOrder("orderPosition");
        return (List<Service>) entityQuery.getResultList();
    }

    @Override
    public BfabricLazyDataModel<Service> getLazyModel() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setOrder("serviceType.serviceArea.name, serviceType.name, name");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Service> getLazyModelByServiceAreaId(long serviceAreaId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("serviceType.serviceArea.id = :serviceAreaId");
        entityQuery.addParameter("serviceAreaId", serviceAreaId);
        entityQuery.setOrder("serviceType.name, name");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Service> getServicesByServiceArea(ServiceArea serviceArea) {
        return createNamedQuery("Service.findByServiceArea").setParameter("serviceArea", serviceArea).getResultList();
    }

    public List<Service> getServicesFiltered(String filterString, Collection<Service> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        entityQuery.setMaxResult(100);
        return (List<Service>) entityQuery.getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Service service = (Service) entity;
        return isValidName(service, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "service type")
            .replace("{1}", service.getServiceType().getName()));
    }

    public void merge(Service service, Service merged, Service mergeSelection, boolean isSwitchPrices) throws RollbackException {
        try {
            // Merge the attributes.
            service.setName(mergeSelection.getName());
            service.setEnabled(mergeSelection.isEnabled());
            service.setOrderPosition(mergeSelection.getOrderPosition());
            service.setDescription(mergeSelection.getDescription());
            service.setServiceType(mergeSelection.getServiceType());

            // Merge the one-to-many associations. IMPORTANT: Do this before merging the many-to-many associations.
            reassign(Instrument.class, merged, service, "service");
            reassign(OfferedCharge.class, merged, service, "service");
            reassign(Charge.class, merged, service, "service");
            reassign(OrderItem.class, merged, service, "service");
            reassign(Order.class, merged.getServiceType(), service.getServiceType(), "serviceType");

            // Merge the many-to-many associations.
            if (!merged.getTrackingUsers().isEmpty()) {
                service.getTrackingUsers().addAll(merged.getTrackingUsers());
                merged.getTrackingUsers().clear();
            }

            // Reassign the prices if the prices are chosen from the entity to be merged.
            if (isSwitchPrices) {
                for (final ServiceOrganizationTypePrice price : service.getServiceOrganizationTypePrices()) {
                    price.setAdditionalPrice(merged.getServiceOrganizationTypePrices(price.getOrganizationType()).getAdditionalPrice());
                    price.setBasicPrice(merged.getServiceOrganizationTypePrices(price.getOrganizationType()).getBasicPrice());
                }
            }

            saveMerge(service, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void reassign(Class<?> clazz, Object oldAttribute, Object newAttribute, String attribute) {
        if (clazz != null && oldAttribute != null && newAttribute != null && attribute != null) {
            createQuery("UPDATE " + clazz.getSimpleName() + " SET " + attribute + " = :new WHERE " + attribute + " = :old").setParameter("old", oldAttribute).setParameter("new", newAttribute)
                .executeUpdate();
        }
    }
}
