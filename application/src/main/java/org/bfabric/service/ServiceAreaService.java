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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceCode;
import org.bfabric.entity.ServiceType;
import org.bfabric.exception.RollbackException;

@Named
@Stateless
public class ServiceAreaService extends AbstractService {

    private static final long serialVersionUID = 1;

    public ServiceAreaService() {
        super(ServiceArea.class);
    }

    public void clearPrices(ServiceArea serviceArea) {
        if (serviceArea != null) {
            for (ServiceType serviceType : serviceArea.getServiceTypes()) {
                for (Service service : serviceType.getServices()) {
                    if (service.isClearPricesRendered() || service.getFullCost() != null) {
                        service.clearPrices();
                        super.save(service);
                    }
                }
            }
        }
    }

    public List<ServiceArea> getEnabledServiceAreas() {
        return createNamedQuery("ServiceArea.findEnabled").getResultList();
    }

    public List<ServiceArea> getEnabledServiceAreasIncluding(ServiceArea serviceArea) {
        return createNamedQuery("ServiceArea.findEnabledIncluding").setParameter("entity", serviceArea).getResultList();
    }

    public List<ServiceArea> getServiceAreasByName(String name) {
        return createNamedQuery("ServiceArea.findByName").setParameter("name", name).getResultList();
    }

    public List<ServiceArea> getServiceAreasByServiceTypes(List<ServiceType> serviceTypes) {
        return createNamedQuery("ServiceArea.findByServiceTypes").setParameter("serviceTypes", serviceTypes).getResultList();
    }

    public List<ServiceArea> getServiceAreasOfEnabledForOfferServicesIncluding(Service service) {
        return createNamedQuery("ServiceArea.findByEnabledForOfferServicesIncludingService").setParameter("service", service).getResultList();
    }

    public List<ServiceArea> getServiceAreasOfEnabledServicesIncluding(Service service) {
        return createNamedQuery("ServiceArea.findByEnabledServicesIncludingService").setParameter("service", service).getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((ServiceArea) entity);
    }

    public void merge(ServiceArea serviceArea, ServiceArea merged, ServiceArea mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            serviceArea.setName(mergeSelection.getName());
            serviceArea.setEnabled(mergeSelection.isEnabled());
            serviceArea.setOrderPosition(mergeSelection.getOrderPosition());

            // Merge the one-to-many associations. IMPORTANT: Do this before merging the many-to-many associations.
            reassign(ServiceType.class, merged, serviceArea, "serviceArea");

            // Merge the many-to-many associations.
            if (!merged.getUsers().isEmpty()) {
                serviceArea.getUsers().addAll(merged.getUsers());
                merged.getUsers().clear();
            }

            saveMerge(serviceArea, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void reassign(Class<?> clazz, ServiceArea oldServiceArea, ServiceArea newServiceArea, String attribute) {
        if (clazz != null && oldServiceArea != null && newServiceArea != null && attribute != null) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE ").append(clazz.getSimpleName()).append(" SET ").append(attribute).append(" = :new WHERE ").append(attribute).append(" = :old");

            if (clazz.equals(ServiceArea.class)) {
                queryBuilder.append(" AND id <> :id");
            }

            Query query = createQuery(queryBuilder.toString()).setParameter("old", oldServiceArea).setParameter("new", newServiceArea);
            if (clazz.equals(ServiceArea.class)) {
                query.setParameter("id", oldServiceArea.getId());
            }

            query.executeUpdate();
        }
    }

    public void save(ServiceArea serviceArea) {
        save(serviceArea, true);
    }

    public void save(ServiceArea serviceArea, boolean index) {
        super.save(serviceArea, index);

        // Save service hierarchy in case of cloning or when propagation is required.
        if (serviceArea.isCloned()) {
            for (ServiceType serviceType : serviceArea.getServiceTypes()) {
                super.save(serviceType, index);
                for (Service service : serviceType.getServices()) {
                    int year = LocalDate.now().getYear();
                    if (service.getServiceCode() != null && service.getServiceCode().getName().endsWith("_" + (year - 1))) {
                        String newCode = service.getServiceCode().getName().replace("_" + (year - 1), "_" + year);
                        ServiceCode serviceCode = CDI.current().select(ServiceCodeService.class).get().findByName(ServiceCode.class, newCode);
                        if (serviceCode == null) {
                            try {
                                serviceCode = service.getServiceCode().clone();
                                serviceCode.setName(newCode);
                                super.save(serviceCode);
                            } catch (Exception ignored) {
                            }
                        }
                        service.setServiceCode(serviceCode);
                    }
                    super.save(service, index);
                }
            }
        } else if (serviceArea.isPropagateEnabled()) {
            serviceArea.propagateEnabled();
            for (ServiceType serviceType : serviceArea.getServiceTypes()) {
                if (serviceType.isEnabledChanged()) {
                    super.save(serviceType, index);
                }
                for (Service service : serviceType.getServices()) {
                    if (service.isEnabledChanged()) {
                        super.save(service, index);
                    }
                }
            }
        }
    }
}
