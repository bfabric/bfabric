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

import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.Service;
import org.bfabric.entity.TaxType;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class ChargeService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private TaxTypeService taxTypeService;

    public ChargeService() {
        super(Charge.class);
    }

    public List<Charge> getChargesByContainerAndSelectedChargesFrom(String filterString, Container container, List<Charge> selectedChargesFrom) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString, "servicename");
        entityQuery.addEntityWhereClause(container, "container");
        entityQuery.addNotInEntitiesClause(selectedChargesFrom);
        entityQuery.setOrder("id DESC, servicename");
        return (List<Charge>) entityQuery.getResultList();
    }

    public List<Charge> getChargesByCreatedByOrderByCreated(String login) {
        return createNamedQuery("Charge.findByCreatedByOrderById").setParameter("createdBy", login).getResultList();
    }

    public BfabricLazyDataModel<Charge> getLazyModelByServiceAreaId(long serviceAreaId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("service.serviceType.serviceArea.id = :serviceAreaId");
        entityQuery.addParameter("serviceAreaId", serviceAreaId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Charge> getLazyModelByServiceTypeId(long serviceTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("service.serviceType.id = :serviceTypeId");
        entityQuery.addParameter("serviceTypeId", serviceTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public LinkedHashMap<String, String> isValid(Charge charge, int selectedServicesSize) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (charge.getId() == 0 && selectedServicesSize == 0 || charge.getId() > 0 && charge.getService() == null) {
            validationErrorMsg.put(Constants.EDIT + ":" + Constants.SERVICE_SELECTION + "table", Constants.REQUIRED);
        }
        if (charge.getOrganizationType() == null) {
            validationErrorMsg.put(Constants.EDIT + ":organizationType", Constants.REQUIRED);
        }
        if (charge.getAccountedComputed().doubleValue() < 0) {
            validationErrorMsg.put(Constants.EDIT + ":total", Messages.get("totalLessThanNotAccounted"));
        }
        return validationErrorMsg;
    }

    public void save(Charge charge, List<Service> selectedServices) {
        if (charge != null) {
            try {
                if (selectedServices != null && charge.getId() == 0) {
                    // Create a charge object for every service selected
                    for (final Service service : selectedServices) {
                        final Charge chargeClone = charge.clone();
                        chargeClone.setService(service);
                        chargeClone.setPrice();
                        if (StringHelper.isEmpty(chargeClone.getDescription())) {
                            chargeClone.setDescription(service.getDescription());
                        }
                        save(chargeClone);
                    }
                } else {
                    save(charge);
                }
                charge.indexDependents();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RollbackException();
            }
        }
    }

    public void save(Charge charge) {
        if (charge != null) {
            if (charge.getTaxType() == null) {
                charge.setTaxType(taxTypeService.getDefaultTaxType());
                charge.setPrice();
            }
            super.save(charge);
        }
    }

    public int saveCharges(List<Charge> charges) {
        int counter = 0;
        try {
            TaxType taxType = taxTypeService.getDefaultTaxType();
            for (Charge currentCharge : charges) {
                currentCharge.setTaxType(taxType);
                currentCharge.setTaxRate(currentCharge.getTaxType().getTax());
                currentCharge.setOrganizationType(currentCharge.getContainer().getBillingOrganizationType());
                currentCharge.setPrice();
                save(currentCharge, null);
                counter++;
            }
            return counter;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }
}