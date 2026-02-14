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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.entity.Company;
import org.bfabric.entity.Department;
import org.bfabric.entity.Division;
import org.bfabric.entity.Institute;
import org.bfabric.entity.Organization;

@Named
@Stateless
public class UnassignedObjectsService extends AbstractService {

    private static final long serialVersionUID = 1;

    private static final String UNASSIGNED_LIST_REDIRECTION_URL = "/unassigned/list.html?faces-redirect=true";

    public List<Company> deleteUnassignedCompanies() {
        List<Company> companies = getUnassignedCompanies();
        for (Company company : companies) {
            remove(company);
        }
        return companies;
    }

    public String deleteUnassignedCompaniesAndRedirect() {
        deleteUnassignedCompanies();
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public void deleteUnassignedCompany(long id) {
        remove(Company.class, id);
    }

    public String deleteUnassignedCompanyAndRedirect(long id) {
        deleteUnassignedCompany(id);
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public void deleteUnassignedDepartment(long id) {
        remove(Department.class, id);
    }

    public String deleteUnassignedDepartmentAndRedirect(long id) {
        deleteUnassignedDepartment(id);
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public List<Department> deleteUnassignedDepartments() {
        List<Department> departments = getUnassignedDepartments();
        for (Department department : departments) {
            remove(department);
        }
        return departments;
    }

    public String deleteUnassignedDepartmentsAndRedirect() {
        deleteUnassignedDepartments();
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public void deleteUnassignedDivision(long id) {
        remove(Division.class, id);
    }

    public String deleteUnassignedDivisionAndRedirect(long id) {
        deleteUnassignedDivision(id);
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public List<Division> deleteUnassignedDivisions() {
        List<Division> divisions = getUnassignedDivisions();
        for (Division division : divisions) {
            remove(division);
        }
        return divisions;
    }

    public String deleteUnassignedDivisionsAndRedirect() {
        deleteUnassignedDivisions();
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public void deleteUnassignedInstitute(long id) {
        remove(Institute.class, id);
    }

    public String deleteUnassignedInstituteAndRedirect(long id) {
        deleteUnassignedInstitute(id);
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public List<Institute> deleteUnassignedInstitutes() {
        List<Institute> institutes = getUnassignedInstitutes();
        for (Institute institute : institutes) {
            remove(institute);
        }
        return institutes;
    }

    public String deleteUnassignedInstitutesAndRedirect() {
        deleteUnassignedInstitutes();
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public Map<String, List<?>> deleteUnassignedObjects() {
        Map<String, List<?>> deletedObjects = new HashMap<>();
        deletedObjects.put("division", deleteUnassignedDivisions());
        deletedObjects.put("company", deleteUnassignedCompanies());
        deletedObjects.put("institute", deleteUnassignedInstitutes());
        deletedObjects.put("department", deleteUnassignedDepartments());
        deletedObjects.put("organization", deleteUnassignedOrganizations());
        return deletedObjects;
    }

    public String deleteUnassignedObjectsAndRedirect() {
        deleteUnassignedObjects();
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public void deleteUnassignedOrganization(long id) {
        remove(Organization.class, id);
    }

    public String deleteUnassignedOrganizationAndRedirect(long id) {
        deleteUnassignedOrganization(id);
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public List<Organization> deleteUnassignedOrganizations() {
        List<Organization> organizations = getUnassignedOrganizations();
        for (Organization organization : organizations) {
            remove(organization);
        }
        return organizations;
    }

    public String deleteUnassignedOrganizationsAndRedirect() {
        deleteUnassignedOrganizations();
        return UNASSIGNED_LIST_REDIRECTION_URL;
    }

    public List<Company> getUnassignedCompanies() {
        return createNamedQuery("Company.findUnassigned").getResultList();
    }

    public List<Department> getUnassignedDepartments() {
        return createNamedQuery("Department.findUnassigned").getResultList();
    }

    public List<Division> getUnassignedDivisions() {
        return createNamedQuery("Division.findUnassigned").getResultList();
    }

    public List<Institute> getUnassignedInstitutes() {
        return createNamedQuery("Institute.findUnassigned").getResultList();
    }

    public int getUnassignedObjectsSize() {
        int unassignedObjectsSize = 0;

        List<Institute> institutes = getUnassignedInstitutes();
        if (institutes != null && !institutes.isEmpty()) {
            unassignedObjectsSize += institutes.size();
        }

        List<Department> departments = getUnassignedDepartments();
        if (departments != null && !departments.isEmpty()) {
            unassignedObjectsSize += departments.size();
        }

        List<Organization> organizations = getUnassignedOrganizations();
        if (organizations != null && !organizations.isEmpty()) {
            unassignedObjectsSize += organizations.size();
        }

        List<Division> divisions = getUnassignedDivisions();
        if (divisions != null && !divisions.isEmpty()) {
            unassignedObjectsSize += divisions.size();
        }

        List<Company> companies = getUnassignedCompanies();
        if (companies != null && !companies.isEmpty()) {
            unassignedObjectsSize += companies.size();
        }

        return unassignedObjectsSize;
    }

    public List<Organization> getUnassignedOrganizations() {
        return createNamedQuery("Organization.findUnassigned").getResultList();
    }
}