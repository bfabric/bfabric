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
import javax.persistence.EntityNotFoundException;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Company;
import org.bfabric.entity.Division;
import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.RollbackException;

@Named
@Stateless
public class DivisionService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private AffiliationHelperService affiliationHelperService;

    public DivisionService() {
        super(Division.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Division division = (Division) entity;
        return createNamedQuery("Division.checkUniqueName").setParameter("name", division.getName()).setParameter("id", division.getId())
            .setParameter("company", division.getCompany()).setMaxResults(1).getResultList().isEmpty();
    }

    public List<Division> getDivisionByNameAndCompany(String name, Company company) {
        return createNamedQuery("Division.findByNameAndCompany").setParameter("name", name).setParameter("company", company).getResultList();
    }

    public Division getDivisionByNameAndCompanyName(String name, String companyName) {
        List<Division> divisions = createNamedQuery("Division.findByNameAndCompanyName").setParameter("name", name).setParameter("companyName", companyName).setMaxResults(1).getResultList();
        return divisions.isEmpty() ? null : divisions.get(0);
    }

    public Division getDivisionByNameAndCompanyNameAndCreateIfNotExists(String name, String companyName) {
        Division division = getDivisionByNameAndCompanyName(name, companyName);
        if (division == null) {
            division = affiliationHelperService.saveDivisionIfNotExists(find(OrganizationType.class, 4), getConfiguration().getDefaultDivision(), getConfiguration()
                .getDefaultCompanyName());
        }
        return division;
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Division division = (Division) entity;
        return isValidName(division, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "company")
            .replace("{1}", division.getCompany().getName()));
    }

    public void merge(Long id1, Long id2) {
        try {
            Division d1 = find(Division.class, id1);
            Division d2 = find(Division.class, id2);
            if (d1 == null || d2 == null) {
                String errorMsg = "Division with id = " + d1 + " or " + d2 + " not found";
                throw new EntityNotFoundException(errorMsg);
            }
            merge(d1, d2, d1);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void merge(Division division, Division merged, Division mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            division.setName(mergeSelection.getName());
            division.setCompany(mergeSelection.getCompany());

            // Update the references.
            updateUserOnMerge(division, merged);
            updateUserGroupOnMerge(division, merged);
            updateProjectOnMerge(division, merged);
            updateOrderOnMerge(division, merged);
            updateBookingOnMerge(division, merged);
            updateBillingInfoOnMerge(division, merged);

            saveMerge(division, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void updateBillingInfoOnMerge(Division division, Division merged) {
        createQuery("update UserBillingInfo set division = :new where division = :old").setParameter("old", merged).setParameter("new", division).executeUpdate();
    }

    public void updateBookingOnMerge(Division division, Division merged) {
        createQuery("update Booking set division = :new where division = :old").setParameter("old", merged).setParameter("new", division).executeUpdate();
    }

    public void updateOrderOnMerge(Division division, Division merged) {
        createQuery("update Order set division = :new where division = :old").setParameter("old", merged).setParameter("new", division).executeUpdate();
    }

    public void updateProjectOnMerge(Division division, Division merged) {
        createQuery("update Project set division = :new where division = :old").setParameter("old", merged).setParameter("new", division).executeUpdate();
    }

    public void updateUserGroupOnMerge(Division division, Division merged) {
        createQuery("update UserGroup set division = :new where division = :old").setParameter("old", merged).setParameter("new", division).executeUpdate();
    }

    public void updateUserOnMerge(Division division, Division merged) {
        createQuery("update User set division = :new where division = :old").setParameter("old", merged).setParameter("new", division).executeUpdate();
    }
}