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

import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Order;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OrderService;
import org.bfabric.service.util.BfabricLazyDataModel;

@MeasureCalls
@Named
@ViewScoped
public class OrderList extends AbstractList<Order> {

    private static final long serialVersionUID = 1;

    @Inject
    private OrderService orderService;

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getAcceptOrderRevisionTasks() {
        return getService().getAcceptOrderRevisionTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getAcceptOrderTasks() {
        return getService().getAcceptOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getBioinformaticianOrdersLazyModelByUserId(long userId) {
        return getService().getBioinformaticianOrdersLazyModelByUserId(userId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getCheckOrderStatusTasks() {
        return getService().getCheckOrderStatusTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getCloseOrderTasks() {
        return getService().getCloseOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getFinishAnalyzingOrderTasks() {
        return getService().getFinishAnalyzingOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getFinishOrderTasks() {
        return getService().getFinishOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getFinishProcessingOrderTasks() {
        return getService().getFinishProcessingOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public List<EntityLog> getLastEntityLogs(Order order) {
        return getService().getLastEntityLogs(order, Math.max(order.getStates().size(), 5));
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getLazyModelByCompanyId(long companyId) {
        return getService().getLazyModelByCompanyId(companyId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getLazyModelByDepartmentId(long departmentId) {
        return getService().getLazyModelByDepartmentId(departmentId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getLazyModelByOrganizationId(long organizationId) {
        return getService().getLazyModelByOrganizationId(organizationId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getLazyModelBySampleTypeId(long sampleTypeId) {
        return getService().getLazyModelBySampleTypeId(sampleTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getOngoingOrderTasks() {
        return getService().getOngoingOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public List<StatusEnum> getOrderStatusEnums() {
        return getService().getOrderStatusEnums();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getOrdersLazyModelBySamplePreparationProtocolId(long samplePreparationProtocolId) {
        return getService().getOrdersLazyModelBySamplePreparationProtocolId(samplePreparationProtocolId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getReassignOrderBioinformaticianTasks() {
        return getService().getReassignOrderBioinformaticianTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getReassignOrderCoachBackupTasks() {
        return getService().getReassignOrderCoachBackupTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getReassignOrderCoachTasks() {
        return getService().getReassignOrderCoachTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getSendOrderSampleTasks() {
        return getService().getSendOrderSampleTasks(identityManager.getCurrentUser());
    }

    @Override
    protected OrderService getService() {
        return orderService;
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getStartAnalyzingOrderTasks() {
        return getService().getStartAnalyzingOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getStartProcessingOrderTasks() {
        return getService().getStartProcessingOrderTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Order> getSubmitOrderTasks() {
        return getService().getSubmitOrderTasks(identityManager.getCurrentUser());
    }
}