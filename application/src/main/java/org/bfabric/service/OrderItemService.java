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

package org.bfabric.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.util.SampleAttributeHelper;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class OrderItemService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private PlateService plateService;

    @Inject
    private SampleAttributeHelper sampleAttributeHelper;

    @Inject
    private SampleService sampleService;

    public OrderItemService() {
        super(OrderItem.class);
    }

    public boolean checkUniqueTubeId(long orderItemId, String tubeId) {
        return createNamedQuery("OrderItem.checkUniqueTubeId").setParameter("tubeId", tubeId).setParameter("id", orderItemId).setMaxResults(1).getResultList().isEmpty();
    }

    public Map<String, Boolean> createRequiredColumnsMap(Order order) {
        final Map<String, Boolean> requiredColumnsMap = new HashMap<>();
        if (order != null) {
            requiredColumnsMap.put(Constants.ORDER_ITEM_LIBRARY_TYPE, sampleAttributeHelper.isRenderedColumn(order, Constants.ORDER_ITEM_LIBRARY_TYPE));
            requiredColumnsMap.put(Constants.ORDER_ITEM_REGION, sampleAttributeHelper.isRenderedColumn(order, Constants.ORDER_ITEM_REGION));
            requiredColumnsMap.put(Constants.ORDER_ITEM_READ_TYPE, sampleAttributeHelper.isRenderedColumn(order, Constants.ORDER_ITEM_READ_TYPE));
            requiredColumnsMap.put(Constants.ORDER_ITEM_INSERT_SIZE, sampleAttributeHelper.isRenderedColumn(order, Constants.ORDER_ITEM_INSERT_SIZE));
            requiredColumnsMap.put(Constants.ORDER_ITEM_MULTIPLEXING, sampleAttributeHelper.isRenderedColumn(order, Constants.ORDER_ITEM_MULTIPLEXING));
        }
        return requiredColumnsMap;
    }

    public List<OrderItem> getEditableOrderItemsByOrder(Order order) {
        return createNamedQuery("OrderItem.findEditableByOrder").setParameter("order", order).getResultList();
    }

    public List<String> getExistingNamesByContainerAndEditListAndEditedSampleIdList(Container container, List<OrderItem> editList, List<Long> editedSampleIdList, int lo_adapted, int hi_adapted) {
        // checking existing names
        StringBuilder uniqueCheckQueryString = new StringBuilder("select name from Sample where (container = :container or container = :parent) and lower(name) in :names");
        if (!editedSampleIdList.isEmpty()) {
            uniqueCheckQueryString.append(" and id not in (:editedSampleIdList)");
        }
        Query uniqueCheckQuery = createQuery(uniqueCheckQueryString.toString()).setParameter("container", container).setParameter("parent", container.getProject());
        if (!editedSampleIdList.isEmpty()) {
            uniqueCheckQuery.setParameter("editedSampleIdList", editedSampleIdList);
        }
        List<String> names = new ArrayList<>();
        for (int i = lo_adapted; i < hi_adapted; i++) {
            names.add(editList.get(i).getSample().getName().toLowerCase());
        }
        uniqueCheckQuery.setParameter("names", names);
        return uniqueCheckQuery.getResultList();
    }

    public List<Order> getOrdersBySampleId(Long sampleId) {
        return createNamedQuery("OrderItem.findOrdersBySampleId").setParameter("sampleId", sampleId).getResultList();
    }

    public boolean hasCorrectTubeIdFormat(String checkTubeId) {
        return checkTubeId != null && checkTubeId.matches(Constants.ORDER_ITEM_TUBEID_REGEXP);
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final OrderItem orderItem = (OrderItem) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        if (orderItem.getTubeId() == null && orderItem.getOrder().isOrderItemTubeIdRendered()) {
            validationErrorMsg.put(Constants.EDIT + ":tubeId", Constants.REQUIRED);
        }

        if (orderItem.getTubeId() != null) {
            if (!hasCorrectTubeIdFormat(orderItem.getTubeId())) {
                validationErrorMsg.put(Constants.EDIT + ":tubeId", Messages.get("tubeIdFormatNotCorrectException"));
            }

            if (!checkUniqueTubeId(orderItem.getId(), orderItem.getTubeId())) {
                validationErrorMsg.put(Constants.EDIT + ":tubeId", Messages.get("notUniqueException"));
            }
        }

        if (!orderItem.getOrder().isProcessesPlates() && orderItem.getSample() == null) {
            validationErrorMsg.put(Constants.EDIT + ":sample", Constants.REQUIRED);
        }
        if (orderItem.getOrder().getServiceType() != null && orderItem.getOrder().getServiceType().getSampleType() != null && orderItem.getOrder().getServiceType()
            .isServiceColumnEnabled() && orderItem.getService() == null) {
            validationErrorMsg.put(Constants.EDIT + ":service", Constants.REQUIRED);
        }
        if (orderItem.isServiceArea(Constants.SERVICE_AREA_NEXTGENSEQUENCING) && orderItem.getRegion() == null) {
            validationErrorMsg.put(Constants.EDIT + ":region", Constants.REQUIRED);
        }
        return validationErrorMsg;
    }

    public void isValidOrderItem(OrderItem orderItem, Order order, Map<String, Boolean> requiredColumnsMap, LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg, int index) {
        if (orderItem != null && order != null && validationErrorMsg != null && sampleAttributeHelper.isAttributeCheckRequired(order)) {
            if (requiredColumnsMap.get(Constants.ORDER_ITEM_LIBRARY_TYPE) && orderItem.getLibraryType() == null) {
                validationErrorMsg.get(index).put(Constants.ORDER_ITEM_LIBRARY_TYPE, Constants.REQUIRED);
            }

            if (requiredColumnsMap.get(Constants.ORDER_ITEM_REGION) && orderItem.getRegion() == null) {
                validationErrorMsg.get(index).put(Constants.ORDER_ITEM_REGION, Constants.REQUIRED);
            }

            if (requiredColumnsMap.get(Constants.ORDER_ITEM_READ_TYPE) && orderItem.getReadType() == null) {
                validationErrorMsg.get(index).put(Constants.ORDER_ITEM_READ_TYPE, Constants.REQUIRED);
            }

            if (requiredColumnsMap.get(Constants.ORDER_ITEM_INSERT_SIZE) && orderItem.getInsertSize() == null) {
                validationErrorMsg.get(index).put(Constants.ORDER_ITEM_INSERT_SIZE, Constants.REQUIRED);
            }

            if (orderItem.getInsertSize() != null && orderItem.getInsertSize() <= 0) {
                validationErrorMsg.get(index).put(Constants.ORDER_ITEM_INSERT_SIZE, Messages.get("notPositive"));
            }

            if (requiredColumnsMap.get(Constants.ORDER_ITEM_MULTIPLEXING) && StringHelper.isEmpty(orderItem.getMultiplexing())) {
                validationErrorMsg.get(index).put(Constants.ORDER_ITEM_MULTIPLEXING, Constants.REQUIRED);
            }
        }
    }

    public void remove(OrderItem orderItem) {
        if (orderItem != null) {
            Order order = orderItem.getOrder();
            order.getOrderItems().remove(orderItem);
            if (!order.isProcessesPlates()) {
                Sample sample = orderItem.getSample();
                orderItem.setSample(null);
                sample.getOrderItems().remove(orderItem);
                // Note: The container of a sample being a project means that the sample was reused, i.e., added as an item using existing samples, and therefore should not be removed.
                if (sample.isDeletable() && !sample.getContainer().isContainerProject()) {
                    sampleService.remove(sample);
                }
                super.remove(orderItem);
            } else {
                plateService.removeOrderItemAndUserSubmittedPlate(orderItem);
            }
            resetNumberOfSamplesPlatesAndSaveOrder(order);
        }
    }

    private void resetNumberOfSamplesPlatesAndSaveOrder(Order order) {
        if (order != null && order.resetNumberOfSamplesPlates(true)) {
            flush();
            Order orderToUpdate = find(Order.class, order.getId());
            // Important: DO NOT REMOVE this call since the order is fetched from the database and needs to reset the number of samples/plates.
            orderToUpdate.resetNumberOfSamplesPlates(true);
            save(orderToUpdate);
        }
    }

    public void save(OrderItem orderItem) {
        super.save(orderItem);
        orderItem.getOrder().index(false);
    }

    public String saveOrderItems(List<OrderItem> editList, List<OrderItem> deleteList, Order order) {
        int deleted = 0;
        int created = 0;
        if (!order.isProcessesPlates()) {
            Set<Long> savedSampleIds = new HashSet<>();
            Set<Long> managedSampleIds = new HashSet<>();
            for (final OrderItem currentItem : editList) {
                boolean isSampleChanged = currentItem.getSample().isChanged();
                if (currentItem.getId() == 0) {
                    // Handle the newly created order items.
                    currentItem.setOrder(order);
                    order.getOrderItems().add(currentItem);
                    // Important: Persist needed here since the log entity otherwise has the id zero.
                    if (currentItem.getSample().getId() > 0) {
                        managedSampleIds.add(currentItem.getSample().getId());
                    }
                    if (currentItem.getSample().getId() == 0 || isSampleChanged && !savedSampleIds.contains(currentItem.getSample().getId())) {
                        savedSampleIds.add(currentItem.getSample().getId());
                        sampleService.save(currentItem.getSample());
                    }
                    persist(currentItem);
                    created++;
                } else {
                    if (currentItem.isChanged()) {
                        merge(currentItem);
                    }
                    if (isSampleChanged && !savedSampleIds.contains(currentItem.getSample().getId())) {
                        managedSampleIds.add(currentItem.getSample().getId());
                        savedSampleIds.add(currentItem.getSample().getId());
                        sampleService.save(currentItem.getSample());
                    }
                }
            }
            for (final OrderItem currentItem : deleteList) {
                if (currentItem.getId() > 0) {
                    // Handle the deletion of existing order items.
                    order.getOrderItems().remove(currentItem);
                    final Sample sample = currentItem.getSample();
                    currentItem.setSample(null);
                    sample.getOrderItems().remove(currentItem);
                    // Note: The container of a sample being a project means that the sample was reused, i.e., added as an item using existing samples, and therefore should not be removed.
                    if (!managedSampleIds.contains(sample.getId()) && sample.isDeletable() && !sample.getContainer().isContainerProject()) {
                        sampleService.remove(sample);
                    }
                    super.remove(currentItem);
                    deleted++;
                }
            }
        } else {
            Set<Plate> plates = new HashSet<>();
            boolean updateUserSubmittedPlate = true;
            for (final OrderItem currentItem : editList) {
                final Plate plate = currentItem.getPlate();
                if (plate != null) {
                    if (plates.contains(plate)) {
                        updateUserSubmittedPlate = false;
                    } else {
                        plates.add(plate);
                    }
                }
                if (!currentItem.isManaged()) {
                    // Handle the newly created order items.
                    currentItem.setOrder(order);
                    if (plate != null && updateUserSubmittedPlate) {
                        plateService.updateUserSubmittedPlate(plate, currentItem);
                    }
                    order.getOrderItems().add(currentItem);
                    persist(currentItem);
                    created++;
                } else {
                    if (currentItem.isChanged()) {
                        merge(currentItem);
                    }
                    if (plate != null && plate.isChanged() && updateUserSubmittedPlate) {
                        plateService.updateUserSubmittedPlate(plate, currentItem);
                    }
                }
            }
            for (final OrderItem currentItem : deleteList) {
                order.getOrderItems().remove(currentItem);
                plateService.removeOrderItemAndUserSubmittedPlate(currentItem);
                deleted++;
            }
        }
        resetNumberOfSamplesPlatesAndSaveOrder(order);
        // Create and print facesMessage.
        if (created > 0 || deleted > 0) {
            // Index the created and updated samples.
            order.indexSamples();
            return createFacesMessagesForCreatedAndDeletedItems(Messages.get("successfullyEditedOrderItems"), created, deleted);
        }

        return Messages.get("successfullyUpdated");
    }
}