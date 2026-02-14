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

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.BookingType;
import org.bfabric.entity.Company;
import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class CompanyService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private DivisionService divisionService;

    public CompanyService() {
        super(Company.class);
    }

    public List<Company> getCompaniesByNameAndOrganizationType(String name, OrganizationType organizationType) {
        return createNamedQuery("Company.findByNameAndOrganizationType").setParameter("name", name).setParameter("organizationType", organizationType).getResultList();
    }

    public List<Company> getCompaniesByOrganizationTypeFiltered(String filterString, OrganizationType organizationType) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addEntityWhereClause(organizationType, null);
        entityQuery.setOrder("name ASC");
        return (List<Company>) entityQuery.getResultList();
    }

    public List<Object[]> getDivisionIdsByCompanyIdsAndSameName(long companyId1, long companyId2) {
        return createNativeQuery("select d1.id as id1, d2.id as id2 from Division d1 join Division d2 on (d1.companyid = :new and d2.companyid = :old and d1.name = d2.name)").setParameter("old",
            companyId1).setParameter("new", companyId2).getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((AbstractNamedBaseEntity) entity);
    }

    public void merge(Company company, Company merged, Company mergeSelection, long mergeBillingOrganizationTypeCompanyId) throws RollbackException {
        try {
            // Merge the attributes.
            company.setName(mergeSelection.getName());
            company.setOrganizationType(mergeSelection.getOrganizationType());
            company.setBillingOrganizationType(mergeSelection.getBillingOrganizationType());
            company.setDefaultBookingType(mergeSelection.getDefaultBookingType());
            company.setDebitorNumber(mergeSelection.getDebitorNumber());
            company.setVatNumber(mergeSelection.getVatNumber());

            if (company.getId() == mergeBillingOrganizationTypeCompanyId) {
                mergeSelection.setBillingOrganizationType(company.getBillingOrganizationType());
            } else {
                mergeSelection.setBillingOrganizationType(merged.getBillingOrganizationType());
            }

            // Merge same named divisions of the two companies.
            List<Object[]> result = getDivisionIdsByCompanyIdsAndSameName(merged.getId(), company.getId());

            for (Object[] r : result) {
                // Since the native query yield BigInteger values, we cast via String.
                divisionService.merge(Long.valueOf(r[0].toString()), Long.valueOf(r[1].toString()));
            }

            // Redirect the remaining divisions of the company to be merged.
            updateDivisionOnMerge(company, merged);

            saveMerge(company, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void save(Company company) {
        save(company, true);
    }

    public void save(Company company, boolean index) {
        if (company != null) {
            if (company.getDefaultBookingType() == null) {
                company.setDefaultBookingType(find(BookingType.class, 1L));
            }
            super.save(company, index);
        }
    }

    public void updateDivisionOnMerge(Company company, Company merged) {
        createQuery("update Division set company = :new where company = :old").setParameter("old", merged).setParameter("new", company).executeUpdate();
    }
}