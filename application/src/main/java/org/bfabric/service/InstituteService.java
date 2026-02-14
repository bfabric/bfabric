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
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Department;
import org.bfabric.entity.Institute;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class InstituteService extends AbstractService {

    private static final long serialVersionUID = 1;

    public InstituteService() {
        super(Institute.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Institute institute = (Institute) entity;
        return createNamedQuery("Institute.checkUniqueName").setParameter("name", institute.getName()).setParameter("id", institute.getId())
            .setParameter("department", institute.getDepartment()).setMaxResults(1).getResultList().isEmpty();
    }

    public List<Institute> getInstitutes(String filterString, OrganizationType organizationType, Organization organization, Department department) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addEntityWhereClause(department, null);
        entityQuery.addEntityWhereClause(organization, "department.organization");
        entityQuery.addEntityWhereClause(organizationType, "department.organization.organizationType");
        entityQuery.setOrder("name");
        entityQuery.setMaxResult(100);
        return (List<Institute>) entityQuery.getResultList();
    }

    public List<Institute> getInstitutesByNameAndDepartment(String name, Department department) {
        return createNamedQuery("Institute.findByNameAndDepartment").setParameter("name", name).setParameter("department", department).setMaxResults(1).getResultList();
    }

    public List<Institute> getInstitutesByOrganizationId(Long organizationId) {
        return createNamedQuery("Institute.findAllByOrganizationId").setParameter("organizationId", organizationId).getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Institute institute = (Institute) entity;
        return isValidName(institute, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "department")
            .replace("{1}", institute.getDepartment().getName()));
    }

    public void merge(Long id1, Long id2) {
        try {
            Institute d1 = find(Institute.class, id1);
            Institute d2 = find(Institute.class, id2);
            if (d1 == null || d2 == null) {
                String errorMsg = "Institute with id = " + d1 + " or " + d2 + " not found";
                throw new EntityNotFoundException(errorMsg);
            }
            merge(d1, d2, d1);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void merge(Institute institute, Institute merged, Institute mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            institute.setName(mergeSelection.getName());
            institute.setDepartment(mergeSelection.getDepartment());
            institute.setJointDepartmentAsList(mergeSelection.getJointDepartmentAsList());

            // Update the references.
            updateProjectOnMerge(institute, merged);
            updateUserOnMerge(institute, merged);
            updateUserGroupOnMerge(institute, merged);
            updateOrderOnMerge(institute, merged);
            updateBookingOnMerge(institute, merged);
            updateBillingInfoOnMerge(institute, merged);

            saveMerge(institute, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void updateBillingInfoOnMerge(Institute institute, Institute merged) {
        createQuery("update UserBillingInfo set institute = :new where institute = :old").setParameter("old", merged).setParameter("new", institute).executeUpdate();
    }

    public void updateBookingOnMerge(Institute institute, Institute merged) {
        createQuery("update Booking set institute = :new where institute = :old").setParameter("old", merged).setParameter("new", institute).executeUpdate();
    }

    public void updateOrderOnMerge(Institute institute, Institute merged) {
        createQuery("update Order set institute = :new where institute = :old").setParameter("old", merged).setParameter("new", institute).executeUpdate();
    }

    public void updateProjectOnMerge(Institute institute, Institute merged) {
        createQuery("update Project set institute = :new where institute = :old").setParameter("old", merged).setParameter("new", institute).executeUpdate();
    }

    public void updateUserGroupOnMerge(Institute institute, Institute merged) {
        createQuery("update UserGroup set institute = :new where institute = :old").setParameter("old", merged).setParameter("new", institute).executeUpdate();
    }

    public void updateUserOnMerge(Institute institute, Institute merged) {
        createQuery("update User set institute = :new where institute = :old").setParameter("old", merged).setParameter("new", institute).executeUpdate();
    }
}