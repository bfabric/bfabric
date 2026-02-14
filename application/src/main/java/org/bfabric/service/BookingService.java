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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Booking;
import org.bfabric.entity.BookingType;
import org.bfabric.entity.Charge;
import org.bfabric.entity.CostCentre;
import org.bfabric.entity.Division;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class BookingService extends AbstractService {

    private static final long serialVersionUID = 1;

    // private static final Logger logger = Logger.getLogger(BookingService.class.getName());

    @Inject
    private AffiliationHelperService affiliationHelperService;

    public BookingService() {
        super(Booking.class);
    }

    public Booking findBySapNumber(Long sapNumber, String bookerName) {
        return (Booking) createQuery("FROM Booking WHERE sapNumber = :sapNumber and (booker is null or :bookerName is null or booker.name = :bookerName) order by id desc").setParameter("sapNumber", sapNumber)
            .setParameter("bookerName", bookerName).getResultStream().findFirst().orElse(null);
    }

    public Booking findBySapNumberNext(Long sapNumberNext, String bookerName) {
        return (Booking) createQuery("FROM Booking WHERE sapNumberNext = :sapNumberNext and (booker is null or :bookerName is null or booker.name = :bookerName) order by id desc").setParameter("sapNumberNext", sapNumberNext)
            .setParameter("bookerName", bookerName).getResultStream().findFirst().orElse(null);
    }

    public long generateNewBookingNumber(BookingType bookingType, CostCentre costCentre) {
        return ((BigInteger) createNativeQuery("select case when max(maxbookingnr) is null then 1 else max(maxbookingnr) + 1 end from (select max(bookingnr) as maxbookingnr from booking where bookingtypeid = :bookingTypeId AND costcentreid = :costCentreId) t")
            .setParameter("bookingTypeId", bookingType.getId())
            .setParameter("costCentreId", costCentre.getId()).getSingleResult()).longValue();
    }

    public BigInteger getBookingCountByOrganizationTypeId(Long organizationTypeId) {
        return (BigInteger) createNativeQuery("SELECT COUNT(*) FROM booking b WHERE exists (select * from institutehierarchy i where organizationtypeid = :organizationTypeId and i.instituteid = b.instituteid) or exists (select * from divisionhierarchy d where organizationtypeid = :organizationTypeId and d.divisionid = b.divisionid)").setParameter("organizationTypeId", organizationTypeId)
            .getResultList().get(0);
    }

    public List<Charge> getChargesByBookingIdOrderByServiceAndDiscount(Long bookingId) {
        return createNamedQuery("Charge.findByBookingIdOrderByServiceAndDiscount").setParameter("bookingId", bookingId).getResultList();
    }

    public List<Charge> getChargesByBookingIdOrderByServiceAndTaxRate(Long bookingId) {
        return createNamedQuery("Charge.findByBookingIdOrderByServiceAndTaxRate").setParameter("bookingId", bookingId).getResultList();
    }

    public User getDefaultBookingIssuer() {
        return (User) createNamedQuery("User.findById").setParameter("id", getConfiguration().getDefaultBookingIssuerId()).getSingleResult();
    }

    public BfabricLazyDataModel<Booking> getNonPaidBookingTasks(User user) {
        if (user.hasRoleImplicit(RoleEnum.BOOKINGMANAGER)) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("(paid is null or paid = false) and created > current_date - 730");
            entityQuery.setOrder("id");
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return null;
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();
        final Booking booking = (Booking) entity;
        if (booking.getCharges().isEmpty()) {
            errorMsg.put(Constants.EDIT + ":charges", Constants.REQUIRED);
        }
        if (!isValidBookingNr(booking)) {
            errorMsg.put(Constants.EDIT + ":bookingNr", Messages.get("bookingNrIsNotUnique"));
        }
        if (booking.getOrganizationType() != null && booking.getOrganizationType().isExternal() && booking.getBillingInfo() != null && (!booking.isManaged() || booking.getBillingInfo()
            .getOldDebitorNumber() != null) && booking
            .getBillingInfo().getDebitorNumber() == null) {
            errorMsg.put(Constants.EDIT + ":debitorNumber", Constants.REQUIRED);
        }
        return errorMsg;
    }

    public boolean isValidBookingNr(Booking booking) {
        return createNativeQuery("select bookingnr from booking where bookingnr = :bookingNr AND bookingtypeid = :bookingType AND costcentreid = :costCentreId" + (booking
            .getId() > 0 ? " AND id <> " + booking.getId() : " ")).setParameter("bookingNr", booking.getBookingNr()).setParameter("bookingType", booking.getBookingType().getId())
            .setParameter("costCentreId", booking.getCostCentre().getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public void removeCascade(Booking booking) {
        if (booking.isDeletable()) {
            super.remove(booking);
            for (Charge charge : booking.getCharges()) {
                super.remove(charge);
            }
        }
    }

    public void saveBooking(Booking booking) {
        try {
            if (booking.getOrganizationType() != null && booking.getOrganizationType().isCompany()) {
                final Division division = affiliationHelperService.saveDivisionIfNotExists(booking.getOrganizationType(), booking.getCompanyName(), booking.getDivisionName());
                booking.setDivision(division);
            }

            List<Charge> initialCharges = new ArrayList<>();
            if (!booking.getInitialCharges().isEmpty()) {
                initialCharges.addAll(booking.getInitialCharges());
            }
            save(booking, false);

            // Associate the booked charges.
            for (final Charge charge : booking.getCharges()) {
                charge.setBooking(booking);
                // Do not log this update since it is not necessary!
                charge.setLogEntity(false);
                merge(charge);
            }

            // Cleanup de-selected charges.
            if (!initialCharges.isEmpty()) {
                List<Charge> deselectedCharges = new ArrayList<>(initialCharges);
                deselectedCharges.removeAll(booking.getCharges());
                for (final Charge charge : deselectedCharges) {
                    charge.setBooking(null);
                    // Do not log this update since it is not necessary!
                    charge.setLogEntity(false);
                    merge(charge);
                }
            }

            // Do the indexing.
            booking.index();
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
        // logger.fine("---3 end save " + booking);
    }
}