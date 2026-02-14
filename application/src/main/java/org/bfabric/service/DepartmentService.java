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
import org.bfabric.entity.Department;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class DepartmentService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private InstituteService instituteService;

    public DepartmentService() {
        super(Department.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Department department = (Department) entity;
        return createNamedQuery("Department.checkUniqueName").setParameter("name", department.getName()).setParameter("id", department.getId())
            .setParameter("organization", department.getOrganization()).setMaxResults(1).getResultList().isEmpty();
    }

    public void deleteJointInstitutesOnMerge(Department department) {
        createNativeQuery("delete from institutedepartment where departmentid = :new and exists(select id from institute where id = instituteid and departmentid = :new)").setParameter("new",
            department.getId()).executeUpdate();
    }

    public List<Department> getDepartments(String filterString, OrganizationType organizationType, Organization organization) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addEntityWhereClause(organization, null);
        entityQuery.addEntityWhereClause(organizationType, "organization.organizationType");
        entityQuery.setOrder("name");
        entityQuery.setMaxResult(100);
        return (List<Department>) entityQuery.getResultList();
    }

    public List<Department> getDepartmentsByNameAndOrganization(String name, Organization organization) {
        return createNamedQuery("Department.findByNameAndOrganization").setParameter("name", name).setParameter("organization", organization).setMaxResults(1).getResultList();
    }

    public List<Department> getDepartmentsIncludeOrganizationsInSearch(String filterString) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        if (StringHelper.isNotEmpty(filterString)) {
            entityQuery.addWhereClauseDisjunctive("LOWER(organization.name) LIKE :filterString");
            entityQuery.addParameterFilterString("filterString", filterString);
        }
        entityQuery.setOrder("name");
        return (List<Department>) entityQuery.getResultList();
    }

    public List<Object[]> getInstituteIdsByDepartmentIdsAndSameName(long departmentId1, long departmentId2) {
        return createNativeQuery("select i1.id as id1, i2.id as id2 from Institute i1 join Institute i2 on (i1.departmentid = :new and i2.departmentid = :old and i1.name = i2.name)").setParameter(
            "old", departmentId1).setParameter("new", departmentId2).getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Department department = (Department) entity;
        return isValidName(department, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "organization")
            .replace("{1}", department.getOrganization().getName()));
    }

    public void merge(Long id1, Long id2) {
        try {
            Department d1 = find(Department.class, id1);
            Department d2 = find(Department.class, id2);
            if (d1 == null || d2 == null) {
                String errorMsg = "Department with id = " + d1 + " or " + d2 + " not found";
                throw new EntityNotFoundException(errorMsg);
            }
            merge(d1, d2, d1);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void merge(Department department, Department merged, Department mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            department.setName(mergeSelection.getName());
            department.setOrganization(mergeSelection.getOrganization());

            // Merge same named institutes of the two departments.
            List<Object[]> result = getInstituteIdsByDepartmentIdsAndSameName(merged.getId(), department.getId());

            for (Object[] r : result) {
                // Since the native query yield BigInteger values, we cast via String.
                instituteService.merge(Long.valueOf(r[0].toString()), Long.valueOf(r[1].toString()));
            }

            // Redirect the remaining institutes of the department to be merged.
            updateInstituteOnMerge(department, merged);

            // Redirect the joint institutes of the department to be merged.
            updateJointInstituteOnMerge(department, merged);

            // Delete joint institutes that are directly associated after the merge.
            deleteJointInstitutesOnMerge(department);

            saveMerge(department, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void updateInstituteOnMerge(Department department, Department merged) {
        createQuery("update Institute set department = :new where department = :old").setParameter("old", merged).setParameter("new", department).executeUpdate();
    }

    public void updateJointInstituteOnMerge(Department department, Department merged) {
        createNativeQuery("update institutedepartment set departmentid = :new where departmentid = :old").setParameter("old", merged.getId()).setParameter("new", department.getId()).executeUpdate();
    }
}