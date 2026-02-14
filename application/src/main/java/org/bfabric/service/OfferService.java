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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class OfferService extends AbstractService {

    private static final long serialVersionUID = 1;

    public OfferService() {
        super(Offer.class);
    }

    public Map<String, Set<String>> changeStatus(Offer offer, StatusEnum statusEnum) {
        offer.changeStatus(statusEnum);
        super.save(offer);
        return createDisplayFacesMessagesMap(Messages.get("offer") + " " + statusEnum.getLabel());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void checkOfferValidityDuration(Long id) {
        Offer offer = find(Offer.class, id);
        offer.setStatus(StatusEnum.EXPIRED);
        merge(offer);
    }

    public String deleteDeletableOffers() {
        List<Offer> offers = getDeletableOffers();
        for (Offer offer : offers) {
            remove(offer);
        }
        return offers.isEmpty() ? null : String.valueOf(offers.size());
    }

    public List<Offer> getAssignableOffers(User user) {
        if (user != null) {
            if (user.hasRoleImplicit(RoleEnum.CONTAINERMANAGER)) {
                return (List<Offer>) getResultList();
            }
            return new ArrayList<>(user.getOffers());
        }
        return null;
    }

    public List<Offer> getAssignableOffersFiltered(User user, String filterString, Collection<Offer> excluded) {
        if (user != null) {
            List<Offer> assignableOffersFiltered = new ArrayList<>();
            List<Offer> assignableOffers = user.hasRoleImplicit(RoleEnum.CONTAINERMANAGER) ? (List<Offer>) getResultList() : (List<Offer>) user.getOffers();
            if (excluded != null) {
                assignableOffers.removeAll(excluded);
            }

            String filterStringTrimmed = filterString.trim().toLowerCase();

            if (StringHelper.isNotEmpty(filterStringTrimmed)) {
                EntityQuery entityQuery = createEntityQuery();
                if (Pattern.compile("\\d+").matcher(filterStringTrimmed).matches()) {
                    entityQuery.addIdOrNameWhereClause(filterStringTrimmed);
                } else {
                    entityQuery.addIdOrNameWhereClause(filterStringTrimmed, "entity.requesterName");
                    entityQuery.setJoinTypeLeftOuter();
                    entityQuery.setJoin("entity.requester");
                    entityQuery.addIdOrNameWhereClause(filterStringTrimmed, "entity.requester.name", "OR");
                }

                entityQuery.setWhere("(" + entityQuery.getWhere() + ")");
                entityQuery.addInEntitiesClause(assignableOffers);
                entityQuery.setMaxResult(100);
                assignableOffersFiltered.addAll((List<Offer>) entityQuery.getResultList());
            } else {
                assignableOffersFiltered.addAll(assignableOffers.subList(0, Math.min(assignableOffers.size(), 100)));
            }

            return assignableOffersFiltered;
        }
        return null;
    }

    public List<Offer> getDeletableOffers() {
        final Configuration configuration = getConfiguration();
        return createNamedQuery("Offer.findDeletable").setParameter("creationDate", LocalDate.now().atStartOfDay().minusDays(configuration.getOfferValidityDuration()))
            .getResultList();
    }

    public List<Offer> getExpiredOffers() {
        final Configuration configuration = getConfiguration();
        return createNamedQuery("Offer.findExpired").setParameter("creationDate", LocalDate.now().atStartOfDay().minusDays(configuration.getOfferValidityDuration()))
            .getResultList();
    }

    public List<Offer> getReassignableOffersByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.coach.id = :userId and entity.lock <> true and entity.created > :expiryDate");
        entityQuery.addParameter("userId", userId);
        entityQuery.addParameter("expiryDate", LocalDate.now().minusDays(getConfiguration().getOfferValidityDuration()).atStartOfDay());
        return (List<Offer>) entityQuery.getResultList();
    }

    public LinkedHashMap<String, String> isValid(Offer offer, boolean isRegisteredUser) {
        // CONSTRAINT customer_required CHECK (requesterid IS NOT NULL OR requestername IS NOT NULL AND requesteraddress IS NOT NULL AND organizationtypeid IS NOT NULL)
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        if (isRegisteredUser) {
            if (offer.getRequester() == null) {
                validationErrorMsg.put(Constants.EDIT + ":requesterautocomplete", Constants.REQUIRED);
            }
        } else {
            if (StringHelper.isEmpty(offer.getRequesterName())) {
                validationErrorMsg.put(Constants.EDIT + ":requesterName", Constants.REQUIRED);
            }
            if (StringHelper.isEmpty(offer.getRequesterAddress())) {
                validationErrorMsg.put(Constants.EDIT + ":requesterAddress", Constants.REQUIRED);
            }
            if (offer.getOrganizationType() == null) {
                validationErrorMsg.put(Constants.EDIT + ":organizationType", Constants.REQUIRED);
            }
        }

        return validationErrorMsg;
    }

    public void lock(Offer offer) {
        offer.setStatus(StatusEnum.LOCKED);
        save(offer);
    }

    public void rollbackStatus(Offer offer) {
        if (offer != null) {
            offer.rollbackStatus();
            save(offer);
        }
    }

    public void save(Offer offer) {
        save(offer, true);
    }

    public void save(Offer offer, boolean index) {
        try {
            if (offer.getStates().isEmpty()) {
                offer.addState();
            }
            super.save(offer, index);
            if (offer.isCloned()) {
                for (OfferedCharge offeredCharge : offer.getCharges()) {
                    super.save(offeredCharge, index);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }

    public void updateModified(Offer offer) {
        // Important: Refresh offer since otherwise this method overwrites offered charges updates.
        offer = find(Offer.class, offer.getId());
        offer.setModifiedToCurrentDateAndUser();
        merge(offer);
    }
}