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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Sample;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OrderItemService;
import org.bfabric.service.SampleService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class OrderItemManager extends AbstractEntityManager<OrderItem> {

    private static final long serialVersionUID = 1;

    private List<OrderItem> newOrderItems = new ArrayList<>();

    @Inject
    private OrderItemService orderItemService;

    @Inject
    private SampleService sampleService;

    public OrderItemManager() {
        super(OrderItem.class);
    }

    @Override
    public String cancel() {
        return getOrderItem().getOrder() != null ? createRedirectShowScreenURL(getOrderItem().getOrder()) : super.getRedirectURLAfterCancel();
    }

    public List<OrderItem> getNewOrderItems() {
        return newOrderItems;
    }

    @Produces
    @Named("orderItem")
    public OrderItem getOrderItem() {
        return getInstance();
    }

    @Override
    public String getRedirectURLAfterRemove() {
        return createRedirectShowScreenURL(getOrderItem().getOrder());
    }

    public Collection<Sample> getSamples() {
        Set<Container> containers = new HashSet<>();
        containers.add(getOrderItem().getOrder());
        containers.add(getOrderItem().getOrder().getContainer());
        return sampleService.getAvailableSamplesByContainersAndSampleTypeNonLazy(containers, getOrderItem().getOrder().getSampleType());
    }

    @Override
    public String getShowScreenRedirectURL() {
        return createRedirectShowScreenURL(getOrderItem().getOrder());
    }

    @Override
    public void initClone() throws CloneNotSupportedException {
        super.initClone();
        Order order = getOrderItem().getOrder();
        if (order != null) {
            int maxItemNumber = 0;
            for (final OrderItem item : order.getOrderItems()) {
                final String tubeId = item.getTubeId() != null ? item.getTubeId() : item.getSample() != null ? item.getSample().getTubeId() : null;
                if (tubeId != null && StringHelper.correctTubeIdFormat(tubeId)) {
                    final int itemNumber = Integer.parseInt(tubeId.substring(tubeId.indexOf("/") + 1));
                    if (itemNumber > maxItemNumber) {
                        maxItemNumber = itemNumber;
                    }
                }
            }
            // Generate tube id.
            if (order.isOrderItemTubeIdRendered() || order.isSampleTubeIdRendered()) {
                getOrderItem().generateTubeId(maxItemNumber + 1, true);
            }
        }
    }

    public boolean isChargeableRendered() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && getOrderItem().getCharges().isEmpty();
    }

    @Override
    public String remove() {
        final String entityName = getOrderItem().toString();
        orderItemService.remove(getOrderItem());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted") + " " + entityName);
        return getRedirectURLAfterRemove();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = orderItemService.isValid(getOrderItem());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            orderItemService.save(getOrderItem());
            return postSave(true, false);
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void setNewOrderItems(List<OrderItem> newOrderItems) {
        this.newOrderItems = newOrderItems;
    }
}
