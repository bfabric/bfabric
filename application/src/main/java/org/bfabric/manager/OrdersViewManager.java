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

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.bfabric.entity.Order;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.ServiceTypeCollection;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.list.StatisticsList;
import org.bfabric.service.OrderService;
import org.bfabric.service.ServiceTypeCollectionService;
import org.bfabric.service.util.BfabricLazyDataModel;

public class OrdersViewManager extends AbstractManager {

    private static final long serialVersionUID = 1;

    @Inject
    protected ServiceTypeCollectionService serviceTypeCollectionService;

    @Inject
    private OrderService orderService;

    private ServiceType selectedServiceType;

    private ServiceTypeCollection serviceTypeCollection;

    @Inject
    private StatisticsList statisticsList;

    public List<String> getCustomOrderStatesFiltered(String filterString) {
        return orderService.getCurrentCustomContainerStatesFiltered(filterString);
    }

    public List<Object> getOrderCountPerCustomStatus() {
        return statisticsList.getOrderCountPerCustomStatusAndServiceType(getSelectedServiceType(), getServiceTypeCollection());
    }

    public List<Object> getOrderCountPerStatus() {
        return statisticsList.getOrderCountPerStatusAndServiceType(getSelectedServiceType(), getServiceTypeCollection());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getOrdersLazyModelByServiceType(ServiceType serviceType) {
        return orderService.getOrdersViewLazyModel(serviceType != null ? serviceType.getName() : null, getServiceTypeCollection());
    }

    public ServiceType getSelectedServiceType() {
        return selectedServiceType;
    }

    public ServiceTypeCollection getServiceTypeCollection() {
        return serviceTypeCollection;
    }

    public Set<ServiceType> getServiceTypes() {
        return getServiceTypeCollection() != null ? getServiceTypeCollection().getServiceTypes() : null;
    }

    public void setSelectedServiceType(ServiceType selectedServiceType) {
        this.selectedServiceType = selectedServiceType;
    }

    public void setServiceTypeCollection(ServiceTypeCollection serviceTypeCollection) {
        this.serviceTypeCollection = serviceTypeCollection;
    }
}