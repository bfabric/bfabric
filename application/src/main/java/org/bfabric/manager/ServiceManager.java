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

package org.bfabric.manager;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceCode;
import org.bfabric.entity.ServiceOrganizationTypePrice;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.OrganizationTypeList;
import org.bfabric.service.ServiceService;
import org.bfabric.service.ServiceTypeService;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class ServiceManager extends AbstractOrderedEnabledNamedBaseEntityManager<Service> {

    private static final long serialVersionUID = 1;

    @Inject
    protected UserService userService;

    private boolean employeeFilter = true;

    private Service mergeSelection = new Service();

    private Service merged;

    @Inject
    private OrganizationTypeList organizationTypeList;

    @Param
    private Long serviceAreaId;

    @Param
    private Long serviceCodeId;

    @Inject
    private ServiceService serviceService;

    @Param
    private Long serviceTypeId;

    @Inject
    private ServiceTypeService serviceTypeService;

    private boolean switchPrices = false;

    public ServiceManager() {
        super(Service.class);
    }

    public String clearPrices() {
        getInstance().clearPrices();
        return save();
    }

    @Override
    protected Service createInstance() {
        final Service service = super.createInstance();
        service.setServiceOrganizationTypePrices(new HashSet<>());

        for (final OrganizationType organizationType : organizationTypeList.getResultList()) {
            final ServiceOrganizationTypePrice serviceOrganizationTypePrice = new ServiceOrganizationTypePrice(service, organizationType);
            service.getServiceOrganizationTypePrices().add(serviceOrganizationTypePrice);
        }

        if (getServiceTypeId() != null) {
            final ServiceType serviceType = entityService.find(ServiceType.class, getServiceTypeId());
            service.setServiceType(serviceType);
            service.setServiceArea(serviceType.getServiceArea());
        } else if (getServiceAreaId() != null) {
            service.setServiceArea(entityService.find(ServiceArea.class, getServiceAreaId()));
        }

        if (getServiceCodeId() != null) {
            final ServiceCode serviceCode = entityService.find(ServiceCode.class, getServiceCodeId());
            service.setServiceCode(serviceCode);
        }
        return service;
    }

    public Service getMergeSelection() {
        return mergeSelection;
    }

    public Service getMerged() {
        return merged;
    }

    public List<Service> getPossibleChildren(String filterString) {
        final Set<Service> exclude = new HashSet<>();
        if (getService().isManaged()) {
            exclude.add(getService());
        }
        exclude.addAll(getService().getAncestors());
        exclude.addAll(getService().getDescendants());
        return serviceService.getServicesFiltered(filterString, exclude);
    }

    @Produces
    @Named("service")
    public Service getService() {
        return getInstance();
    }

    public Long getServiceAreaId() {
        return serviceAreaId;
    }

    public Long getServiceCodeId() {
        return serviceCodeId;
    }

    public Long getServiceTypeId() {
        return serviceTypeId;
    }

    public List<ServiceType> getServiceTypes() {
        return serviceTypeService.getEnabledServiceTypesIncludingServiceTypeByServiceArea(getService().getServiceArea(), getService().getServiceType());
    }

    public List<User> getUsers(String filterString) {
        List<User> userOptions;
        if (isEmployeeFilter()) {
            userOptions = userService.getEmployeesFiltered(filterString, getService().getUsers());
        } else {
            userOptions = userService.getUsersFilteredExcluding(filterString, getService().getUsers());
        }

        return userOptions;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getService() != null && isManaged() && getService().getServiceType() != null) {
            // Initialize the service area and types.
            getService().setServiceArea(getService().getServiceType().getServiceArea());
        }
        initMerge();
    }

    public void initMerge() {
        if (getInstance() != null && mergeId != null) {
            try {
                merged = getInstance(mergeId);
                if (merged != null) {
                    mergeSelection.setName(getService().getName());
                    mergeSelection.setServiceType(getService().getServiceType());
                    mergeSelection.setEnabled(getService().isEnabled());
                    mergeSelection.setOrderPosition(getService().getOrderPosition());
                    if (!StringHelper.isEmpty(getService().getDescription())) {
                        mergeSelection.setDescription(getService().getDescription());
                    } else {
                        mergeSelection.setDescription(getMerged().getDescription());
                    }
                } else {
                    redirectToEntityNotFoundErrorPage(getEntityClass().getSimpleName(), String.valueOf(mergeId));
                }
            } catch (NumberFormatException e) {
                redirectToEntityIdInvalidErrorPage(getEntityClass().getSimpleName(), mergeId);
            }
        }
    }

    public boolean isEmployeeFilter() {
        return employeeFilter;
    }

    public boolean isSwitchPrices() {
        return switchPrices;
    }

    @Override
    public String merge() {
        try {
            serviceService.merge(getService(), getMerged(), getMergeSelection(), isSwitchPrices());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = serviceService.isValid(getService());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            serviceService.save(getService());
            if (getService().isCloned()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCloned"));
            }
            return postSave(getService().isCloned(), false);
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void setEmployeeFilter(boolean employeeFilter) {
        this.employeeFilter = employeeFilter;
    }

    public void setMergeSelection(Service mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Service service) {
        merged = service;
    }

    public void setSwitchPrices(boolean switchPrices) {
        this.switchPrices = switchPrices;
    }

    public String switchTracked() {
        getInstance().switchTracked();
        if (getInstance().getTracked()) {
            getCurrentUser().getTrackedServices().add(getInstance());
            getFacesMessagesManager().bufferWarningClear(Messages.get("trackedServiceHint"));

        } else {
            getCurrentUser().getTrackedServices().remove(getInstance());
            getFacesMessagesManager().bufferWarningClear(Messages.get("untrackedServiceHint"));
        }
        userService.save(getCurrentUser());
        return getShowScreenRedirectURL(sidebarHelper.getTab());
    }
}