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

package org.bfabric.util;

import java.io.Serializable;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.Company;
import org.bfabric.entity.Department;
import org.bfabric.entity.Division;
import org.bfabric.entity.Institute;
import org.bfabric.entity.Organization;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.api.HasAffiliation;
import org.bfabric.service.AffiliationHelperService;
import org.bfabric.service.CompanyService;
import org.bfabric.service.DepartmentService;
import org.bfabric.service.EntityService;
import org.bfabric.service.InstituteService;
import org.bfabric.service.OrganizationService;

@Named
@ViewScoped
public class AffiliationHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private static final String COMPANY = "company";

    private static final String DEPARTMENT = "department";

    private static final String INSTITUTE = "institute";

    private static final String MEMBER = "member";

    private static final String ORDER = "order";

    private static final String ORGANIZATION = "organization";

    private static final String ORGANIZATION_TYPE = "organizationType";

    private static final String PROJECT = "project";

    private static final String USER = "user";

    private static final String USER_BILLING_INFO = "userBillingInfo";

    @Inject
    private AffiliationHelperService affiliationHelperService;

    @Inject
    private CompanyService companyService;

    private Department department;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private EntityService entityService;

    private Institute institute;

    @Inject
    private InstituteService instituteService;

    private String modalPanelCaller;

    private AbstractBaseEntity modalPanelCallerEntity;

    private String modalPanelType;

    private Organization organization;

    @Inject
    private OrganizationService organizationService;

    private OrganizationType organizationType;

    public void createNewCompany(AbstractBaseEntity callerEntity, String caller, OrganizationType orgType) {
        setModalPanelType(COMPANY);
        setModalPanelCallerEntity(callerEntity);
        setModalPanelCaller(caller);
        // Organization type has to be given.
        setOrganizationType(orgType);
    }

    public void createNewInstitute(AbstractBaseEntity callerEntity, String caller, OrganizationType orgType, Organization org, Department dep) {
        if (!caller.equalsIgnoreCase(INSTITUTE)) {
            setInstitute(new Institute());
        }

        setModalPanelType(INSTITUTE);
        setModalPanelCallerEntity(callerEntity);
        setModalPanelCaller(caller);

        if (dep == null) {
            setDepartment(new Department());
            setModalPanelType(DEPARTMENT);
        } else {
            setDepartment(dep);
        }

        if (org == null) {
            setOrganization(new Organization());
            setModalPanelType(ORGANIZATION);
        } else {
            setOrganization(org);
        }

        // Set the organization type if it is given.
        if (orgType != null) {
            setOrganizationType(orgType);
            getOrganization().setOrganizationType(getOrganizationType());
        }
    }

    public List<Company> getCompanies(String filterString) {
        FacesContext context = FacesContext.getCurrentInstance();
        String organizationTypeId = (String) UIComponent.getCurrentComponent(context).getAttributes().get(ORGANIZATION_TYPE);
        OrganizationType organizationTypeFilter = null;
        if (organizationTypeId != null) {
            organizationTypeFilter = entityService.find(OrganizationType.class, Long.valueOf(organizationTypeId));
        }
        return companyService.getCompaniesByOrganizationTypeFiltered(filterString, organizationTypeFilter);
    }

    public Department getDepartment() {
        return department;
    }

    public List<Department> getDepartments(String filterString) {
        FacesContext context = FacesContext.getCurrentInstance();
        String organizationTypeId = (String) UIComponent.getCurrentComponent(context).getAttributes().get(ORGANIZATION_TYPE);
        String organizationId = (String) UIComponent.getCurrentComponent(context).getAttributes().get(ORGANIZATION);
        OrganizationType organizationTypeFilter = null;
        Organization organizationFilter = null;
        if (organizationTypeId != null) {
            organizationTypeFilter = entityService.find(OrganizationType.class, Long.valueOf(organizationTypeId));
        }
        if (organizationId != null) {
            organizationFilter = entityService.find(Organization.class, Long.valueOf(organizationId));
        }
        return departmentService.getDepartments(filterString, organizationTypeFilter, organizationFilter);
    }

    public Institute getInstitute() {
        return institute;
    }

    public List<Institute> getInstitutes(String filterString) {
        FacesContext context = FacesContext.getCurrentInstance();
        String organizationTypeId = (String) UIComponent.getCurrentComponent(context).getAttributes().get(ORGANIZATION_TYPE);
        String organizationId = (String) UIComponent.getCurrentComponent(context).getAttributes().get(ORGANIZATION);
        String departmentId = (String) UIComponent.getCurrentComponent(context).getAttributes().get(DEPARTMENT);
        OrganizationType organizationTypeFilter = null;
        Organization organizationFilter = null;
        Department departmentFilter = null;
        if (organizationTypeId != null) {
            organizationTypeFilter = entityService.find(OrganizationType.class, Long.valueOf(organizationTypeId));
        }
        if (organizationId != null) {
            organizationFilter = entityService.find(Organization.class, Long.valueOf(organizationId));
        }
        if (departmentId != null) {
            departmentFilter = entityService.find(Department.class, Long.valueOf(departmentId));
        }
        return instituteService.getInstitutes(filterString, organizationTypeFilter, organizationFilter, departmentFilter);
    }

    public String getModalPanelCaller() {
        return modalPanelCaller;
    }

    public AbstractBaseEntity getModalPanelCallerEntity() {
        return modalPanelCallerEntity;
    }

    public String getModalPanelType() {
        return modalPanelType;
    }

    public Organization getOrganization() {
        return organization;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public List<Organization> getOrganizations(String filterString) {
        FacesContext context = FacesContext.getCurrentInstance();
        String organizationTypeId = (String) UIComponent.getCurrentComponent(context).getAttributes().get(ORGANIZATION_TYPE);
        OrganizationType organizationTypeFilter = null;
        if (organizationTypeId != null) {
            organizationTypeFilter = entityService.find(OrganizationType.class, Long.valueOf(organizationTypeId));
        }
        return organizationService.getOrganizationsByOrganizationTypeFiltered(filterString, organizationTypeFilter);
    }

    public String getResetInput() {
        switch (getModalPanelCaller()) {
        case USER:
        case USER_BILLING_INFO:
        case ORDER:
        case PROJECT:
            return Constants.EDIT + ":institute, " + Constants.EDIT + ":department, " + Constants.EDIT + ":organization";
        case MEMBER:
            return "addUser:institute, addUser:department, addUser:organization";
        case INSTITUTE:
            return Constants.EDIT + ":department, " + Constants.EDIT + ":organization";
        default:
            return Constants.EMPTY_STRING;
        }
    }

    public boolean isShowDepartment() {
        return DEPARTMENT.equalsIgnoreCase(getModalPanelType()) || ORGANIZATION.equalsIgnoreCase(getModalPanelType());
    }

    public boolean isShowInstitute() {
        return !INSTITUTE.equalsIgnoreCase(getModalPanelCaller());
    }

    public boolean isShowOrganization() {
        return ORGANIZATION.equalsIgnoreCase(getModalPanelType());
    }

    public void save() {
        if (ORGANIZATION.equalsIgnoreCase(getModalPanelType())) {
            setOrganization(saveOrganizationIfNotExists(getOrganization(), getOrganizationType()));

            switch (getModalPanelCaller()) {
            case USER:
            case USER_BILLING_INFO:
            case MEMBER:
            case PROJECT:
            case ORDER:
                HasAffiliation entity = (HasAffiliation) getModalPanelCallerEntity();
                entity.setOrganization(getOrganization());
                entity.setOrganizationType(getOrganization().getOrganizationType());
                break;
            case INSTITUTE:
                ((Institute) getModalPanelCallerEntity()).setOrganization(getOrganization());
                break;
            default:
                break;
            }
        }

        if (ORGANIZATION.equalsIgnoreCase(getModalPanelType()) || DEPARTMENT.equalsIgnoreCase(getModalPanelType())) {
            setDepartment(saveDepartmentIfNotExists(getDepartment(), getOrganization()));

            switch (getModalPanelCaller()) {
            case USER:
            case USER_BILLING_INFO:
            case MEMBER:
            case PROJECT:
            case ORDER:
                HasAffiliation entity = (HasAffiliation) getModalPanelCallerEntity();
                entity.setDepartment(getDepartment());
                break;
            case INSTITUTE:
                ((Institute) getModalPanelCallerEntity()).setDepartment(getDepartment());
                break;
            default:
                break;
            }
        }

        setInstitute(saveInstituteIfNotExists(getInstitute(), getDepartment()));

        switch (getModalPanelCaller()) {
        case USER:
        case USER_BILLING_INFO:
        case MEMBER:
        case PROJECT:
        case ORDER:
            HasAffiliation entity = (HasAffiliation) getModalPanelCallerEntity();
            entity.setInstitute(getInstitute());
            break;
        default:
            break;
        }
    }

    public void saveCompany(HasAffiliation entity) {
        Division division = affiliationHelperService.saveDivisionIfNotExists(entity.getOrganizationType(), entity.getCompanyName(), entity.getDivisionName());
        entity.setCompany(division.getCompany());
        entity.setCompanyName(division.getCompanyName());
        entity.setDivision(division);
        entity.setDivisionName(division.getName());
    }

    @SuppressWarnings("hiding")
    private Department saveDepartmentIfNotExists(Department department, Organization organization) {
        List<Department> departments = departmentService.getDepartmentsByNameAndOrganization(department.getName(), organization);
        if (!departments.isEmpty()) {
            return departments.get(0);
        }
        department.setOrganization(organization);
        entityService.persist(department);
        return department;
    }

    @SuppressWarnings("hiding")
    private Institute saveInstituteIfNotExists(Institute institute, Department department) {
        if (institute != null) {
            List<Institute> institutes = instituteService.getInstitutesByNameAndDepartment(institute.getName(), department);
            if (!institutes.isEmpty()) {
                return institutes.get(0);
            }
            institute.setDepartment(department);
            entityService.persist(institute);
            return institute;
        }
        return null;
    }

    @SuppressWarnings("hiding")
    public Organization saveOrganizationIfNotExists(Organization organization, OrganizationType organizationType) {
        List<Organization> organizations = organizationService.getOrganizationsByNameAndOrganizationType(organization.getName(), organizationType);
        if (!organizations.isEmpty()) {
            return organizations.get(0);
        }
        organizationService.save(organization);
        return organization;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setInstitute(Institute institute) {
        this.institute = institute;
    }

    public void setModalPanelCaller(String modalPanelCaller) {
        this.modalPanelCaller = modalPanelCaller;
    }

    public void setModalPanelCallerEntity(AbstractBaseEntity modalPanelCallerEntity) {
        this.modalPanelCallerEntity = modalPanelCallerEntity;
    }

    public void setModalPanelType(String modalPanelType) {
        this.modalPanelType = modalPanelType;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }
}