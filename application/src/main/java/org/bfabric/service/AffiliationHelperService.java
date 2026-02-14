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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.BookingType;
import org.bfabric.entity.Company;
import org.bfabric.entity.Department;
import org.bfabric.entity.Division;
import org.bfabric.entity.Institute;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.exception.RollbackException;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class AffiliationHelperService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private CompanyService companyService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private DivisionService divisionService;

    @Inject
    private InstituteService instituteService;

    @Inject
    private OrganizationService organizationService;

    public void saveAndCheckAffiliation(HasAffiliation entity) throws RollbackException {
        if (entity != null) {
            try {
                if (entity.getOrganizationType() != null && entity.getOrganizationType().isCompany()) {
                    entity.setDivision(saveDivisionIfNotExists(entity.getOrganizationType(), entity.getCompanyName(), entity.getDivisionName()));
                }
            } catch (Exception e) {
                throw new RollbackException(Messages.get("companySaveFailedHint"));
            }
            if (!entity.isValidAffiliation()) {
                throw new RollbackException(Messages.get("instituteOrCompanyMustSetHint"));
            }
        }
    }

    public Company saveCompanyIfNotExists(OrganizationType companyOrganizationType, String companyName) {
        // Trim company name.
        String name = StringHelper.trim(companyName);

        Company company = new Company();
        // Check whether a company by this name already exists
        List<Company> companies = companyService.getCompaniesByNameAndOrganizationType(name, companyOrganizationType);
        if (companies != null && !companies.isEmpty()) {
            company = companies.get(0);
        } else {
            company.setName(name);
            company.setOrganizationType(companyOrganizationType);
            companyService.save(company);
        }

        return company;
    }

    public Department saveDepartmentIfNotExists(OrganizationType organizationType, String organizationName, String departmentName) {
        Organization organization = saveOrganizationIfNotExists(organizationType, organizationName);
        String name = StringHelper.trim(departmentName);
        Department department = new Department();
        List<Department> departments = departmentService.getDepartmentsByNameAndOrganization(name, organization);
        if (departments != null && !departments.isEmpty()) {
            department = departments.get(0);
        } else {
            department.setName(name);
            department.setOrganization(organization);
            persist(department);
        }
        return department;
    }

    public Division saveDivisionIfNotExists(OrganizationType divisionOrganizationType, String companyName, String divisionName) {
        Company company = saveCompanyIfNotExists(divisionOrganizationType, companyName);

        String name = StringHelper.trim(divisionName);
        if (StringHelper.isEmpty(name)) {
            name = getConfiguration().getDefaultDivision();
        }

        Division division = new Division();
        // Check whether a division by this name already exists
        List<Division> divisions = divisionService.getDivisionByNameAndCompany(name, company);
        if (divisions != null && !divisions.isEmpty()) {
            division = divisions.get(0);
        } else {
            division.setName(name);
            division.setCompany(company);
            persist(division);
        }

        return division;
    }

    public Institute saveInstituteIfNotExists(OrganizationType organizationType, String organizationName, String departmentName, String instituteName) {
        Department department = saveDepartmentIfNotExists(organizationType, organizationName, departmentName);
        String name = StringHelper.trim(instituteName);
        Institute institute = new Institute();
        List<Institute> institutes = instituteService.getInstitutesByNameAndDepartment(name, department);
        if (institutes != null && !institutes.isEmpty()) {
            institute = institutes.get(0);
        } else {
            institute.setName(name);
            institute.setDepartment(department);
            persist(institute);
        }
        return institute;
    }

    public Organization saveOrganizationIfNotExists(OrganizationType organizationType, String organizationName) {
        String name = StringHelper.trim(organizationName);
        Organization organization = new Organization();
        List<Organization> organizations = organizationService.getOrganizationsByNameAndOrganizationType(name, organizationType);
        if (organizations != null && !organizations.isEmpty()) {
            organization = organizations.get(0);
        } else {
            if (organization.getDefaultBookingType() == null) {
                organization.setDefaultBookingType(find(BookingType.class, 2L));
            }
            organization.setName(name);
            organization.setOrganizationType(organizationType);
            persist(organization);
        }
        return organization;
    }
}