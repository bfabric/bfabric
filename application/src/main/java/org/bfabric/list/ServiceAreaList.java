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

package org.bfabric.list;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.ServiceAreaService;

@Named
@ViewScoped
public class ServiceAreaList extends AbstractList<ServiceArea> {

    private static final long serialVersionUID = 1;

    @Inject
    private ServiceAreaService serviceAreaService;

    // Do not cache!
    public List<ServiceArea> getEnabledServiceAreas() {
        return getService().getEnabledServiceAreas();
    }

    @CachedMethodResult
    public List<ServiceArea> getEnabledServiceAreasIncluding(ServiceArea serviceArea) {
        return getService().getEnabledServiceAreasIncluding(serviceArea);
    }

    @Override
    @CachedMethodResult
    public List<ServiceArea> getResultListEnabled() {
        return getResultListEnabledIncludingOrderByEntityId(0, "orderPosition");
    }

    @Override
    @CachedMethodResult
    public List<ServiceArea> getResultListEnabledIncluding(long entityId) {
        return getResultListEnabledIncludingOrderByEntityId(entityId, "orderPosition");
    }

    @Override
    protected ServiceAreaService getService() {
        return serviceAreaService;
    }

    @CachedMethodResult
    public List<ServiceArea> getServiceAreasByServiceTypes(List<ServiceType> serviceTypes) {
        return getService().getServiceAreasByServiceTypes(serviceTypes);
    }

    @CachedMethodResult
    public List<ServiceArea> getServiceAreasOfEnabledForOfferServicesIncluding(Service service) {
        return getService().getServiceAreasOfEnabledForOfferServicesIncluding(service);
    }

    @CachedMethodResult
    public List<ServiceArea> getServiceAreasOfEnabledServicesIncluding(Service service) {
        return getService().getServiceAreasOfEnabledServicesIncluding(service);
    }
}