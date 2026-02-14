package org.bfabric.manager;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Department;
import org.bfabric.entity.Institute;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.DepartmentService;
import org.bfabric.service.InstituteService;

@MeasureCalls
@Named
@ViewScoped
public class InstituteManager extends AbstractEntityManager<Institute> {

    private static final long serialVersionUID = 1;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private InstituteService instituteService;

    private Institute mergeSelection = new Institute();

    private Institute merged;

    public InstituteManager() {
        super(Institute.class);
    }

    @Produces
    @Named("institute")
    public Institute getInstitute() {
        return getInstance();
    }

    public Institute getMergeSelection() {
        return mergeSelection;
    }

    public Institute getMerged() {
        return merged;
    }

    public List<Department> getPossibleJointDepartments(String filterString) {
        List<Department> jointDepartment = departmentService.getDepartmentsIncludeOrganizationsInSearch(filterString);
        jointDepartment.removeAll(getInstitute().getJointDepartment());
        jointDepartment.remove(getInstitute().getDepartment());
        return jointDepartment;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (isManaged() && getInstitute().getDepartment() != null) {
            // Initialize affiliations
            getInstitute().setOrganization(getInstitute().getDepartment().getOrganization());
        }
        initMerge();
    }

    public void initMerge() {
        if (getInstance() != null && mergeId != null) {
            try {
                merged = getInstance(mergeId);
                if (merged != null) {
                    mergeSelection.setName(getInstitute().getName());
                    mergeSelection.setDepartment(getInstitute().getDepartment());
                    mergeSelection.setJointDepartmentAsList(getInstitute().getJointDepartmentAsList());
                } else {
                    redirectToEntityNotFoundErrorPage(getEntityClass().getSimpleName(), String.valueOf(mergeId));
                }
            } catch (NumberFormatException e) {
                redirectToEntityIdInvalidErrorPage(getEntityClass().getSimpleName(), mergeId);
            }
        }
    }

    @Override
    public String merge() {
        try {
            instituteService.merge(getInstitute(), getMerged(), getMergeSelection());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (final Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        return validateAndSave(instituteService);
    }

    public void setMergeSelection(Institute mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Institute institute) {
        merged = institute;
    }
}
