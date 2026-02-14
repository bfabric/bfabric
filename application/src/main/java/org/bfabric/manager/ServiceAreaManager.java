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

import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ServiceAreaService;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class ServiceAreaManager extends AbstractOrderedEnabledNamedBaseEntityManager<ServiceArea> {

    private static final long serialVersionUID = 1;

    private boolean employeeFilter = true;

    private ServiceArea mergeSelection = new ServiceArea();

    private ServiceArea merged;

    @Inject
    private ServiceAreaService serviceAreaService;

    @Inject
    private UserService userService;

    public ServiceAreaManager() {
        super(ServiceArea.class);
    }

    public String clearPrices() {
        serviceAreaService.clearPrices(getInstance());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCleared"));
        return getShowScreenRedirectURL();
    }

    public ServiceArea getMergeSelection() {
        return mergeSelection;
    }

    public ServiceArea getMerged() {
        return merged;
    }

    @Produces
    @Named("serviceArea")
    public ServiceArea getServiceArea() {
        return getInstance();
    }

    public List<User> getUsers(String filterString) {
        List<User> userOptions;
        if (isEmployeeFilter()) {
            userOptions = userService.getEmployeesFiltered(filterString, getServiceArea().getUsers());
        } else {
            userOptions = userService.getUsersFilteredExcluding(filterString, getServiceArea().getUsers());
        }

        return userOptions;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        initMerge();
    }

    public void initMerge() {
        if (getInstance() != null && mergeId != null) {
            try {
                merged = getInstance(mergeId);
                if (merged != null) {
                    mergeSelection.setName(getServiceArea().getName());
                    mergeSelection.setEnabled(getServiceArea().isEnabled());
                    mergeSelection.setEnabledForOffer(getServiceArea().isEnabledForOffer());
                    mergeSelection.setOrderPosition(getServiceArea().getOrderPosition());
                    if (StringHelper.isNotEmpty(getServiceArea().getDescription())) {
                        mergeSelection.setDescription(getServiceArea().getDescription());
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

    @Override
    public String merge() {
        try {
            serviceAreaService.merge(getServiceArea(), getMerged(), getMergeSelection());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = serviceAreaService.isValid(getServiceArea());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            serviceAreaService.save(getServiceArea());
            if (getServiceArea().isCloned()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCloned"));
            }
            return postSave(getServiceArea().isCloned(), false);
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void setEmployeeFilter(boolean employeeFilter) {
        this.employeeFilter = employeeFilter;
    }

    public void setMergeSelection(ServiceArea mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(ServiceArea serviceArea) {
        merged = serviceArea;
    }
}
