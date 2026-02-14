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
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.service.EntityService;
import org.bfabric.service.ServiceAreaService;
import org.bfabric.service.ServiceTypeService;
import org.omnifaces.util.Ajax;

@Named
@ViewScoped
public class ServiceHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private String caller;

    private AbstractBaseEntity callerEntity;

    @Inject
    private EntityService entityService;

    private ServiceArea serviceArea;

    private String serviceAreaFilter;

    private String serviceAreaName;

    @Inject
    private ServiceAreaService serviceAreaService;

    private ServiceType serviceType;

    private String serviceTypeName;

    @Inject
    private ServiceTypeService serviceTypeService;

    @SuppressWarnings("hiding")
    public void createNew(AbstractBaseEntity callerEntity, String caller, ServiceArea serviceArea) {
        setCallerEntity(callerEntity);
        setCaller(caller);

        switch (getCaller()) {
        case "serviceType":
            setServiceArea(new ServiceArea());
            break;
        case "service":
        case "sequencingApplication":
            if (serviceArea == null) {
                setServiceArea(new ServiceArea());
            } else {
                setServiceArea(serviceArea);
            }
            setServiceType(new ServiceType());
            getServiceType().setServiceArea(getServiceArea());
            break;
        default:
            break;
        }
    }

    public String getCaller() {
        return caller;
    }

    public AbstractBaseEntity getCallerEntity() {
        return callerEntity;
    }

    public ServiceArea getServiceArea() {
        return serviceArea;
    }

    public String getServiceAreaFilter() {
        return serviceAreaFilter;
    }

    public String getServiceAreaName() {
        return serviceAreaName;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getServiceTypeName() {
        return serviceTypeName;
    }

    public List<ServiceType> getServiceTypesOfEnabledForOfferServicesIncluding(Service service) {
        if (StringHelper.isEmpty(getServiceAreaFilter())) {
            return serviceTypeService.getServiceTypesEnabledAndEnabledForOfferServicesIncludingService(service);
        }
        return serviceTypeService.getServiceTypesEnabledAndByServiceAreaNameAndEnabledForOfferServicesIncludingService(service, getServiceAreaFilter());
    }

    public List<ServiceType> getServiceTypesOfEnabledServicesIncluding(Service service) {
        if (StringHelper.isEmpty(getServiceAreaFilter())) {
            return serviceTypeService.getServiceTypesOfEnabledServicesIncludingService(service);
        }
        return serviceTypeService.getServiceTypesByServiceAreaNameOfEnabledServicesIncludingService(service, getServiceAreaFilter());
    }

    public void save() {
        switch (getCaller()) {
        case "serviceType":
            if (getServiceAreaName() != null) {
                getServiceArea().setName(getServiceAreaName());
                saveServiceAreaIfNotExists();
                ((ServiceType) getCallerEntity()).setServiceArea(getServiceArea());
            }
            break;
        case "service":
            if (getServiceAreaName() != null) {
                getServiceArea().setName(getServiceAreaName());
                saveServiceAreaIfNotExists();
                Service service = (Service) getCallerEntity();
                service.setServiceArea(getServiceArea());
            }
            if (getServiceTypeName() != null) {
                getServiceType().setName(getServiceTypeName());
                saveServiceTypeIfNotExists();
                Service service = (Service) getCallerEntity();
                service.setServiceArea(getServiceArea());
                service.setServiceType(getServiceType());
            }
            break;
        case "sequencingApplication":
            if (getServiceAreaName() != null) {
                getServiceArea().setName(getServiceAreaName());
                saveServiceAreaIfNotExists();
            }
            if (getServiceTypeName() != null) {
                getServiceType().setName(getServiceTypeName());
                saveServiceTypeIfNotExists();
                SequencingApplication sequencingApplication = (SequencingApplication) getCallerEntity();
                sequencingApplication.setServiceType(getServiceType());
            }
            break;
        default:
            break;
        }

        setServiceTypeName(null);
        setServiceAreaName(null);
    }

    private void saveServiceAreaIfNotExists() {
        List<ServiceArea> serviceAreas = serviceAreaService.getServiceAreasByName(getServiceArea().getName());
        if (!serviceAreas.isEmpty()) {
            setServiceArea(serviceAreas.get(0));
        } else {
            entityService.persist(getServiceArea());
        }
    }

    private void saveServiceTypeIfNotExists() {
        List<ServiceType> serviceTypes = serviceTypeService.getServiceTypesByName(getServiceType().getName());
        if (!serviceTypes.isEmpty()) {
            setServiceType(serviceTypes.get(0));
        } else {
            entityService.persist(getServiceType());
        }
    }

    public void serviceAreaChanged(ValueChangeEvent event) {
        setServiceArea((ServiceArea) event.getNewValue());
        getServiceType().setServiceArea(getServiceArea());
    }

    public void serviceAreaFilterChanged() {
        String dataTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID);
        if (dataTableClientId != null) {
            Ajax.update(dataTableClientId + ":type");
        }
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public void setCallerEntity(AbstractBaseEntity callerEntity) {
        this.callerEntity = callerEntity;
    }

    public void setServiceArea(ServiceArea serviceArea) {
        this.serviceArea = serviceArea;
    }

    public void setServiceAreaFilter(String serviceAreaFilter) {
        this.serviceAreaFilter = serviceAreaFilter;
    }

    public void setServiceAreaName(String serviceAreaName) {
        this.serviceAreaName = serviceAreaName;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void setServiceTypeName(String serviceTypeName) {
        this.serviceTypeName = serviceTypeName;
    }
}