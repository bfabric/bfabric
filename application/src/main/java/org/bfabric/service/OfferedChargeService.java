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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.entity.Service;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class OfferedChargeService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private ChargeService chargeService;

    @Inject
    private OfferService offerService;

    public OfferedChargeService() {
        super(OfferedCharge.class);
    }

    public void copyFromCharges(Offer offer, List<Charge> selectedChargesFrom) {
        if (!selectedChargesFrom.isEmpty()) {
            for (Charge charge : selectedChargesFrom) {
                save(new OfferedCharge(offer, charge), null);
            }
            offerService.updateModified(offer);
        }
    }

    public void copyToCharges(Container container, List<OfferedCharge> selectedChargesTo, boolean isUseCurrentServicePrices) {
        for (OfferedCharge offeredCharge : selectedChargesTo) {
            chargeService.save(new Charge(offeredCharge, container, isUseCurrentServicePrices), null);
        }
    }

    public List<OfferedCharge> getFilteredOfferedChargesByOfferIdAndSelectedChargesTo(String filterString, long offerId, List<OfferedCharge> selectedChargesTo) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("offerid = :offerid");
        // Get all offered charges whose id or service name matches the given filterString
        if (StringHelper.isNotEmpty(filterString)) {
            entityQuery.addWhereClause("(cast(id as text) like :filterString OR lower(servicename) like :filterString)");
            entityQuery.addParameterFilterString("filterString", filterString);
        }
        entityQuery.addParameter("offerid", offerId);
        entityQuery.setOrder("id, servicename");
        entityQuery.setMaxResult(100);

        List<OfferedCharge> offeredChargesOptions = (List<OfferedCharge>) entityQuery.getResultList();
        if (!selectedChargesTo.isEmpty()) {
            offeredChargesOptions.removeAll(selectedChargesTo);
        }
        return offeredChargesOptions;
    }

    public LinkedHashMap<String, String> isValid(OfferedCharge offeredCharge, int selectedServicesSize) {
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();

        if (offeredCharge.getId() == 0 && selectedServicesSize == 0 || offeredCharge.getId() > 0 && offeredCharge.getService() == null) {
            errorMsg.put(Constants.EDIT + ":" + Constants.SERVICE_SELECTION + "table", Constants.REQUIRED);
        }

        return errorMsg;
    }

    public void save(OfferedCharge offeredCharge, List<Service> selectedServices) {
        try {
            if (selectedServices != null && !selectedServices.isEmpty()) {
                // Create a charge object for every service selected.
                for (final Service service : selectedServices) {
                    final OfferedCharge offeredChargeClone = offeredCharge.clone();
                    offeredChargeClone.setService(service);
                    offeredChargeClone.setPrice();
                    if (StringHelper.isEmpty(offeredChargeClone.getDescription())) {
                        offeredChargeClone.setDescription(service.getDescription());
                    }
                    super.save(offeredChargeClone);
                }
            } else {
                super.save(offeredCharge);
            }
            offerService.updateModified(offeredCharge.getOffer());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }

    public String saveOfferedCharges(List<OfferedCharge> editList, List<OfferedCharge> deleteList) {
        int deleted = 0;
        int created = 0;

        Set<Offer> offers = new HashSet<>();

        for (OfferedCharge offeredCharge : editList) {
            if (offeredCharge.getId() == 0) {
                offers.add(offeredCharge.getOffer());
                save(offeredCharge);
                created++;
            }
            if (offeredCharge.isChanged()) {
                offers.add(offeredCharge.getOffer());
                save(offeredCharge);
            }
        }

        for (OfferedCharge offeredCharge : deleteList) {
            if (offeredCharge.getId() > 0) {
                offers.add(offeredCharge.getOffer());
                remove(offeredCharge);
                deleted++;
            }
        }

        // Update modified of all associated offers.
        for (Offer offer : offers) {
            offerService.updateModified(offer);
        }

        // Create and print the faces messages.
        if (created > 0 || deleted > 0) {
            return createFacesMessagesForCreatedAndDeletedItems(Messages.get("successfullyEditedCharges"), created, deleted);
        }

        return Messages.get("successfullyUpdated");
    }
}