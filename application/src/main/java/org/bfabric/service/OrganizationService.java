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
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.BookingType;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class OrganizationService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private DepartmentService departmentService;

    public OrganizationService() {
        super(Organization.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Organization organization = (Organization) entity;
        return createNamedQuery("Organization.checkUniqueName").setParameter("name", organization.getName()).setParameter("id", organization.getId())
            .setParameter("organizationType", organization.getOrganizationType()).setMaxResults(1).getResultList().isEmpty();
    }

    public List<Object[]> getDepartmentIdsByOrganizationIdsAndSameName(long organizationId1, long organizationId2) {
        return createNativeQuery("select d1.id as id1, d2.id as id2 from Department d1 join Department d2 on (d1.organizationid = :new and d2.organizationid = :old and d1.name = d2.name)")
            .setParameter("old", organizationId1).setParameter("new", organizationId2).getResultList();
    }

    public List<Organization> getOrganizationsByNameAndOrganizationType(String name, OrganizationType organizationType) {
        return createNamedQuery("Organization.findByNameAndOrganizationType").setParameter("name", name).setParameter("organizationType", organizationType).setMaxResults(1).getResultList();
    }

    public List<Organization> getOrganizationsByOrganizationTypeFiltered(String filterString, OrganizationType organizationType) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addEntityWhereClause(organizationType, null);
        entityQuery.setOrder("getOrganizationUsage(id) DESC, name");
        return (List<Organization>) entityQuery.getResultList();
    }

    public List<Organization> getOrganizationsFiltered(String filterString) {
        EntityQuery entityQuery = createEntityQuery();
        if (StringHelper.isNotEmpty(filterString)) {
            entityQuery.addWhereClause("(lower(name) LIKE :filterString)");
            entityQuery.addParameterFilterString("filterString", filterString);
        }
        entityQuery.setOrder("name");
        entityQuery.setMaxResult(100);
        return (List<Organization>) entityQuery.getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Organization organization = (Organization) entity;
        return isValidName(organization, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "organization type")
            .replace("{1}", organization.getOrganizationType().getName()));
    }

    public void merge(Organization organization, Organization merged, Organization mergeSelection, long mergeBillingOrganizationTypeOrganizationId) throws RollbackException {
        try {
            // Merge the attributes.
            organization.setName(mergeSelection.getName());
            organization.setOrganizationType(mergeSelection.getOrganizationType());
            organization.setBillingOrganizationType(mergeSelection.getBillingOrganizationType());
            organization.setDefaultBookingType(mergeSelection.getDefaultBookingType());
            organization.setDebitorNumber(mergeSelection.getDebitorNumber());
            organization.setVatNumber(mergeSelection.getVatNumber());

            if (organization.getId() == mergeBillingOrganizationTypeOrganizationId) {
                mergeSelection.setBillingOrganizationType(organization.getBillingOrganizationType());
            } else {
                mergeSelection.setBillingOrganizationType(merged.getBillingOrganizationType());
            }

            // Merge same named departments of the two organizations.
            List<Object[]> result = getDepartmentIdsByOrganizationIdsAndSameName(merged.getId(), organization.getId());

            for (Object[] r : result) {
                // Since the native query yield BigInteger values, we cast via String.
                departmentService.merge(Long.valueOf(r[0].toString()), Long.valueOf(r[1].toString()));
            }

            // Redirect the remaining departments of the organization to be merged.
            updateDepartmentOnMerge(organization, merged);

            saveMerge(organization, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void save(Organization organization) {
        save(organization, true);
    }

    public void save(Organization organization, boolean index) {
        if (organization != null) {
            if (organization.getDefaultBookingType() == null) {
                organization.setDefaultBookingType(find(BookingType.class, 2L));
            }
            super.save(organization, index);
        }
    }

    public void updateDepartmentOnMerge(Organization organization, Organization merged) {
        createQuery("update Department set organization = :new where organization = :old").setParameter("old", merged).setParameter("new", organization).executeUpdate();
    }
}