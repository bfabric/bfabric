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

package org.bfabric.entity.api;

import javax.faces.event.ValueChangeEvent;

import org.bfabric.entity.Company;
import org.bfabric.entity.Department;
import org.bfabric.entity.Division;
import org.bfabric.entity.Institute;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;

public interface HasAffiliation {

    default void clearButtonCompany() {
        setDivision(null);
        setDivisionName(null);
    }

    default void clearButtonDepartment() {
        setInstitute(null);
    }

    default void clearButtonOrganization() {
        setDepartment(null);
        clearButtonDepartment();
    }

    default void companyChanged(ValueChangeEvent event) {
        Company newCompany = (Company) event.getNewValue();
        if (newCompany != null) {
            setCompany(newCompany);
            setCompanyName(newCompany.getName());
            setDivision(null);
            setDivisionName(null);
        }
    }

    default void departmentChanged(ValueChangeEvent event) {
        Department newDepartment = (Department) event.getNewValue();
        if (newDepartment != null) {
            setOrganization(newDepartment.getOrganization());
            setInstitute(null);
        }
    }

    Company getCompany();

    String getCompanyName();

    Department getDepartment();

    Division getDivision();

    String getDivisionName();

    Institute getInstitute();

    Organization getOrganization();

    OrganizationType getOrganizationType();

    default void instituteChanged(ValueChangeEvent event) {
        Institute newInstitute = (Institute) event.getNewValue();
        if (newInstitute != null) {
            setOrganization(newInstitute.getDepartment().getOrganization());
            setDepartment(newInstitute.getDepartment());
            setInstitute(newInstitute);
        }
    }

    default boolean isValidAffiliation() {
        return getInstitute() != null && getDivision() == null || getInstitute() == null && getDivision() != null;
    }

    default void organizationChanged(ValueChangeEvent event) {
        Organization newOrganization = (Organization) event.getNewValue();
        if (newOrganization != null) {
            setOrganization(newOrganization);
            setDepartment(null);
            setInstitute(null);
        }
    }

    default void organizationTypeChanged(ValueChangeEvent event) {
        OrganizationType newOrganizationType = (OrganizationType) event.getNewValue();
        setOrganizationType(newOrganizationType);
        setOrganization(null);
        setDepartment(null);
        setInstitute(null);
        setCompany(null);
        setCompanyName(null);
        setDivision(null);
        setDivisionName(null);
    }

    default void setAffiliationValues() {
        if (getInstitute() != null) {
            setDepartment(getInstitute().getDepartment());
            setOrganization(getDepartment().getOrganization());
            setOrganizationType(getOrganization().getOrganizationType());
        } else if (getDivision() != null) {
            setCompany(getDivision().getCompany());
            setCompanyName(getDivision().getCompanyName());
            setDivisionName(getDivision().getName());
            setOrganizationType(getDivision().getOrganizationType());
        }
    }

    void setCompany(Company company);

    void setCompanyName(String companyName);

    void setDepartment(Department department);

    void setDivision(Division division);

    void setDivisionName(String divisionName);

    void setInstitute(Institute institute);

    void setOrganization(Organization organization);

    void setOrganizationType(OrganizationType organizationType);
}