/*
 *
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


package org.bfabric.manager;

import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.TechnologyList;
import org.bfabric.service.ServiceTypeService;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Ajax;

@MeasureCalls
@Named
@ViewScoped
public class ServiceTypeManager extends AbstractOrderedEnabledNamedBaseEntityManager<ServiceType> {

    private static final long serialVersionUID = 1;

    @Param
    protected Long serviceAreaId;

    @Inject
    TechnologyList technologyList;

    private boolean employeeFilter = true;

    private ServiceType mergeSelection = new ServiceType();

    private ServiceType merged;

    private User oldCoach;

    private User oldCoachBackup;

    @Inject
    private ServiceTypeService serviceTypeService;

    @Inject
    private UserService userService;

    public ServiceTypeManager() {
        super(ServiceType.class);
    }

    public String clearPrices() {
        serviceTypeService.clearPrices(getInstance());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCleared"));
        return getShowScreenRedirectURL();
    }

    @Override
    protected ServiceType createInstance() {
        final ServiceType serviceType = super.createInstance();
        if (serviceType != null) {
            List<Technology> technologies = technologyList.getTechnologiesEnabledIncludingTechnologies(serviceType.getTechnologies());
            if (technologies != null && technologies.size() == 1) {
                serviceType.addTechnology(technologies.get(0));
            }
            if (serviceAreaId != null) {
                final ServiceArea serviceArea = entityService.find(ServiceArea.class, serviceAreaId);
                if (serviceArea != null) {
                    serviceType.setServiceArea(serviceArea);
                }
            }
            serviceType.setCoach(getCurrentUser());
            setOldCoach(getCurrentUser());
        }
        return serviceType;
    }

    public void enabledChangedListener(ValueChangeEvent event) {
        if ((Boolean) event.getNewValue()) {
            final User currentCoach = getServiceType().getCoach();
            if (currentCoach != null && !currentCoach.hasRoleImplicit(getServiceType().getDefaultRequiredRole())) {
                getServiceType().setCoach(null);
                setOldCoach(null);
                Ajax.update(Constants.EDIT + ":coach");
            } else if (currentCoach == null) {
                setOldCoach(null);
                Ajax.update(Constants.EDIT + ":coach");
            }

            final User currentCoachBackup = getServiceType().getCoachBackup();
            if (currentCoachBackup != null && !currentCoachBackup.hasRoleImplicit(getServiceType().getDefaultRequiredRole())) {
                getServiceType().setCoachBackup(null);
                setOldCoachBackup(null);
                Ajax.update(Constants.EDIT + ":coachBackup");
            } else if (currentCoachBackup == null) {
                setOldCoachBackup(null);
                Ajax.update(Constants.EDIT + ":coachBackup");
            }
        }
    }

    public List<User> getFilteredUsersByDefaultRequiredRoleIncludingOldCoach(String filterString) {
        return userService.getUsersFilteredByRoleEnumIncludingUser(filterString, getServiceType().getDefaultRequiredRole(), getOldCoach());
    }

    public List<User> getFilteredUsersByDefaultRequiredRoleIncludingOldCoachBackup(String filterString) {
        return userService.getUsersFilteredByRoleEnumIncludingUser(filterString, getServiceType().getDefaultRequiredRole(), getOldCoachBackup());
    }

    public ServiceType getMergeSelection() {
        return mergeSelection;
    }

    public ServiceType getMerged() {
        return merged;
    }

    public User getOldCoach() {
        return oldCoach;
    }

    public User getOldCoachBackup() {
        return oldCoachBackup;
    }

    @Produces
    @Named("serviceType")
    public ServiceType getServiceType() {
        return getInstance();
    }

    public List<User> getUsers(String filterString) {
        List<User> userOptions;
        if (isEmployeeFilter()) {
            userOptions = userService.getEmployeesFiltered(filterString, getServiceType().getUsers());
        } else {
            userOptions = userService.getUsersFilteredExcluding(filterString, getServiceType().getUsers());
        }
        return userOptions;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getInstance() != null) {
            setOldCoach(getServiceType().getCoach());
            setOldCoachBackup(getServiceType().getCoachBackup());
        }
        initMerge();
    }

    public void initMerge() {
        if (getInstance() != null && mergeId != null) {
            try {
                merged = getInstance(mergeId);
                if (merged != null) {
                    mergeSelection.setName(getServiceType().getName());
                    mergeSelection.setServiceArea(getServiceType().getServiceArea());
                    mergeSelection.setEnabled(getServiceType().isEnabled());
                    mergeSelection.setRequiresProject(getServiceType().isRequiresProject());
                    mergeSelection.setProcessesSamples(getServiceType().isProcessesSamples());
                    mergeSelection.setServiceColumnEnabled(getServiceType().isServiceColumnEnabled());
                    if (StringHelper.isNotEmpty(getServiceType().getDescription())) {
                        mergeSelection.setDescription(getServiceType().getDescription());
                    } else {
                        mergeSelection.setDescription(getMerged().getDescription());
                    }
                    if (getServiceType().getSampleType() != null) {
                        mergeSelection.setSampleType(getServiceType().getSampleType());
                    } else {
                        mergeSelection.setSampleType(getMerged().getSampleType());
                    }
                    if (getServiceType().getCoach() != null) {
                        mergeSelection.setCoach(getServiceType().getCoach());
                    } else {
                        mergeSelection.setCoach(getMerged().getCoach());
                    }
                    if (getServiceType().getCoachBackup() != null) {
                        mergeSelection.setCoachBackup(getServiceType().getCoachBackup());
                    } else {
                        mergeSelection.setCoachBackup(getMerged().getCoachBackup());
                    }
                    if (StringHelper.isNotEmpty(getServiceType().getInstructionLink())) {
                        mergeSelection.setInstructionLink(getServiceType().getInstructionLink());
                    } else {
                        mergeSelection.setInstructionLink(getMerged().getInstructionLink());
                    }
                    mergeSelection.setOrderPosition(getServiceType().getOrderPosition());
                } else {
                    redirectToEntityNotFoundErrorPage(getEntityClass().getSimpleName(), String.valueOf(mergeId));
                }
            } catch (NumberFormatException e) {
                redirectToEntityIdInvalidErrorPage(getEntityClass().getSimpleName(), mergeId);
            }
        }
    }

    public boolean isCoachBackupChanged() {
        final User currentCoachBackup = getServiceType().getCoachBackup();
        return !(currentCoachBackup == null && getOldCoachBackup() == null || currentCoachBackup != null && currentCoachBackup.equals(getOldCoachBackup()));
    }

    public boolean isCoachChanged() {
        final User currentCoach = getServiceType().getCoach();
        return !(currentCoach == null && getOldCoach() == null || currentCoach != null && currentCoach.equals(getOldCoach()));
    }

    public boolean isEmployeeFilter() {
        return employeeFilter;
    }

    @Override
    public String merge() {
        try {
            serviceTypeService.merge(getServiceType(), getMerged(), getMergeSelection());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    public void reassign(Class<?> clazz, String attribute) {
        serviceTypeService.reassign(clazz, getMerged(), getServiceType(), attribute);
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = serviceTypeService.isValid(getServiceType());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            serviceTypeService.save(getServiceType(), isCoachChanged(), isCoachBackupChanged());
            if (getServiceType().isCloned()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCloned"));
            }
            return postSave(getServiceType().isCloned(), false);
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void setEmployeeFilter(boolean employeeFilter) {
        this.employeeFilter = employeeFilter;
    }

    public void setMergeSelection(ServiceType mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(ServiceType serviceType) {
        merged = serviceType;
    }

    public void setOldCoach(User oldCoach) {
        this.oldCoach = oldCoach;
    }

    public void setOldCoachBackup(User oldCoachBackup) {
        this.oldCoachBackup = oldCoachBackup;
    }
}
