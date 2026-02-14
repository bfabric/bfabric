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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Plate;
import org.bfabric.entity.PlateLayout;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePlatePosition;
import org.bfabric.entity.SampleType;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OrderItemService;
import org.bfabric.service.PlateTypeService;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.SampleAttributeHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Ajax;

@MeasureCalls
@Named
@ViewScoped
public class OrderItemPlateBatchManager extends AbstractBatchManager<Order> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(OrderItemPlateBatchManager.class.getName());

    private static final int sampleLimitValidationErrorMsgKey = -2;

    private final List<OrderItem> createdList = new ArrayList<>();

    private final List<OrderItem> deleteList = new ArrayList<>();

    private List<OrderItem> editList = new ArrayList<>();

    private Plate editedPlate;

    @Param
    private Boolean initializeItems;

    @Inject
    private OrderItemService orderItemService;

    @Inject
    private PlateTypeService plateTypeService;

    @Inject
    private SampleAttributeHelper sampleAttributeHelper;

    private SampleType sampleType;

    private ServiceArea serviceArea;

    private ServiceType serviceType;

    public OrderItemPlateBatchManager() {
        super(Order.class);
    }

    @Override
    public void addNewBatchItems() {
        if (getSampleType() != null) {
            // The current maximum item number value for the tube id.
            final int maxItemNumber = getMaxItemNumber();

            if (getNumberOfNewBatchItems() > getMaxNumberOfNewBatchItems()) {
                setNumberOfNewBatchItems(getMaxNumberOfNewBatchItems());
            }

            for (int i = 0; i < getNumberOfNewBatchItems(); i++) {
                final OrderItem orderItem = createNewOrderItem();

                // Generate tube id.
                orderItem.generateTubeId(maxItemNumber + i + 1, false);
                if (StringHelper.isNotEmpty(orderItem.getTubeId())) {
                    orderItem.getPlate().setName(orderItem.getTubeId());
                }

                getEditList().add(orderItem);
                getCreatedList().add(orderItem);
            }

            setNumberOfNewBatchItems(1);
            updateMaxNumberOfNewLines();
        }
    }

    public void assignSamplesToPlate() {
        int plateId = getEditedPlate().hashCode();
        getEditedPlate().setChanged(true);
        Map<Integer, Integer> plateIdsNonEmptySampleNamesMap = getEditedPlate().getPlateIdsNonEmptySampleNamesMap();
        plateIdsNonEmptySampleNamesMap.clear();
        for (SamplePlatePosition samplePlatePosition : getEditedPlate().getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder()) {
            Sample sample = samplePlatePosition.getSample();
            sample.setNameFromUserSampleName(true);
            if (StringHelper.isNotEmpty(sample.getName())) {
                if (!plateIdsNonEmptySampleNamesMap.containsKey(plateId)) {
                    plateIdsNonEmptySampleNamesMap.put(plateId, 1);
                } else {
                    plateIdsNonEmptySampleNamesMap.put(plateId, plateIdsNonEmptySampleNamesMap.get(plateId) + 1);
                }
            }
        }
        // setEditedPlate(null);
    }

    public void cancelAssignSamplesToPlate() {
        for (SamplePlatePosition samplePlatePosition : getEditedPlate().getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder()) {
            Sample sample = samplePlatePosition.getSample();
            sample.setName(sample.getOldUserSampleName());
            sample.setOldUserSampleName(null);
            sample.setUserSampleName(null);
        }
        setEditedPlate(null);
    }

    public boolean correctPlateNameFormat(String plateName) {
        return plateName != null && plateName.matches(Constants.SAMPLE_NAME_CHARACTERS_REGEXP);
    }

    private OrderItem createNewOrderItem() {
        OrderItem orderItem;
        orderItem = new OrderItem();
        orderItem.setOrder(getOrder());

        orderItem.setPlate(new Plate());
        orderItem.getPlate().setContainer(getOrder());
        orderItem.getPlate().setPlateType(plateTypeService.getPlateTypeByName(Constants.PLATE_TYPE_USER_SUBMITTED_NAME));
        orderItem.getPlate().setSupervisor(getOrder().getCoach());
        return orderItem;
    }

    public void deleteChanged(OrderItem orderItem) {
        getEditList().remove(orderItem);
        getCreatedList().remove(orderItem);
        if (orderItem.getId() > 0) {
            orderItem.setDeleted(true);
            deleteList.add(orderItem);
        }

        updateMaxNumberOfNewLines();
    }

    public List<OrderItem> getCreatedList() {
        return createdList;
    }

    @Override
    public OrderItem getCurrentCloneItem() {
        return (OrderItem) super.getCurrentCloneItem();
    }

    public List<OrderItem> getDeleteList() {
        return deleteList;
    }

    @Override
    public List<OrderItem> getEditList() {
        return editList;
    }

    public Plate getEditedPlate() {
        return editedPlate;
    }

    private int getMaxItemNumber() {
        int maxItemNumber = 0;
        if (getOrder() != null) {
            final List<OrderItem> orderItemsList = new ArrayList<>();
            orderItemsList.addAll(getOrder().getOrderItems());
            orderItemsList.addAll(getEditList());

            for (final OrderItem item : orderItemsList) {
                final String tubeId = item.getTubeId();
                if (tubeId != null && StringHelper.correctTubeIdFormat(tubeId)) {
                    final int itemNumber = Integer.parseInt(tubeId.substring(tubeId.indexOf("/") + 1));
                    if (itemNumber > maxItemNumber) {
                        maxItemNumber = itemNumber;
                    }
                }
            }
        }
        return maxItemNumber;
    }

    @Override
    public int getMaxNumberOfBatchItems() {
        return getConfiguration().getMaxBatchEditItemsPlates();
    }

    public Order getOrder() {
        return getInstance();
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public ServiceArea getServiceArea() {
        return serviceArea;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void givenNameChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        getEditList().get(rowIndex).getPlate().setChanged(true);
    }

    @Override
    @PostConstruct
    public void init() {
        if (id != null) {
            setInstance(loadInstance());
            if (getOrder() != null) {
                serviceType = getOrder().getServiceType();
                if (serviceType != null) {
                    serviceArea = serviceType.getServiceArea();
                    setSampleType(serviceType.getSampleType());
                }
            }
            // Initialize the editList.
            getEditList().clear();
            initializeEditList();
            updateMaxNumberOfNewLines();
            getCustomListingRows();
            if (getOrder() != null && getOrder().getOrderItems().isEmpty()) {
                if (initializeItems != null && initializeItems && getOrder().getNumberOfPlates() != null) {
                    setNumberOfNewBatchItems(getOrder().getNumberOfPlates());
                    addNewBatchItems();
                } else if (getOrder().isUpdatable()) {
                    // Show per default an empty order item iff the order processes plates, has no order items, and is updatable.
                    setNumberOfNewBatchItems(1);
                    addNewBatchItems();
                }
            }
        }
    }

    @Override
    public void initializeEditList() {
        if (getOrder() != null && !getOrder().getOrderItems().isEmpty() && getOrder().isUpdatable()) {
            for (OrderItem orderItem : orderItemService.getEditableOrderItemsByOrder(getOrder())) {
                if (orderItem.getPlate() == null || orderItem.getPlate().isUpdatableOrUserUpdatable()) {
                    getEditList().add(orderItem);
                    Plate plate = orderItem.getPlate();
                    if (plate != null && plate.isManaged()) {
                        plate.initPlateIdsNonEmptySampleNamesMap();
                    }
                }
            }
        }
    }

    private boolean isOrderItemSpecificAttribute(String columnId) {
        return Constants.SERVICE.equals(columnId) || Constants.ORDER_ITEM_LIBRARY_TYPE.equals(columnId)
            || Constants.ORDER_ITEM_REGION.equals(columnId) || Constants.ORDER_ITEM_READ_TYPE.equals(columnId)
            || Constants.ORDER_ITEM_INSERT_SIZE.equals(columnId) || Constants.ORDER_ITEM_MULTIPLEXING.equals(columnId);
    }

    public boolean isSamplePlateAssignmentOrderWarningRendered() {
        for (OrderItem orderItem : getEditList()) {
            Plate plate = orderItem.getPlate();
            if (plate != null) {
                PlateLayout plateLayout = plate.getPlateLayout();
                if (plate.isSampleAssignmentPerRow() && (plateLayout == null || !plateLayout.hasOneRow())) {
                    return true;
                }
            }
        }
        return !getCurrentUser().hasRoleImplicit(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isSamplePlateHasEmptyPositionsInBetweenWarningRendered() {
        if (isSamplePlateAssignmentOrderWarningRendered()) {
            if (getOrder() != null) {
                for (Plate plate : getOrder().getPlates()) {
                    if (plate.equals(getEditedPlate())) {
                        if (getEditedPlate().hasEmptyPositionsInBetween()) {
                            return true;
                        }
                    } else if (plate.hasEmptyPositionsInBetween()) {
                        return true;
                    }
                }
            }
            return getEditedPlate() != null && getEditedPlate().hasEmptyPositionsInBetween();
        }
        return false;
    }

    @Override
    public LinkedHashMap<Integer, LinkedHashMap<String, String>> isValid(int lo, int hi) {
        LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg = new LinkedHashMap<>();

        // Caching the rendered columns.
        final Map<String, Boolean> renderedColumnsMap = new HashMap<>();
        renderedColumnsMap.put(Constants.SERVICE, sampleAttributeHelper.isRenderedColumn(getOrder(), Constants.SERVICE));

        // Caching the required columns.
        final Map<String, Boolean> requiredColumnsMap = orderItemService.createRequiredColumnsMap(getOrder());

        OrderItem currentItem;
        int plateCounter = 0;
        int sampleCounter = 0;
        Map<String, Integer> plateNamesCounter = new HashMap<>();
        for (int i = lo; i < hi; i++) {
            currentItem = getEditList().get(i);

            // Skip the validation if the item is marked for deletion.
            if (!currentItem.isDeleted()) {
                plateCounter++;
                String plateName = currentItem.getPlate().getName();
                if (StringHelper.isNotEmpty(plateName)) {
                    if (plateNamesCounter.containsKey(plateName)) {
                        plateNamesCounter.put(plateName, plateNamesCounter.get(plateName) + 1);
                    } else {
                        plateNamesCounter.put(plateName, 1);
                    }
                }
            }
        }

        Map<String, Set<Integer>> notUniqueSampleNameWithinPlateIndices = new HashMap<>();
        Set<Plate> plates = new HashSet<>();
        Map<Integer, Map<String, Integer>> plateIndexSampleNamesCounter = new HashMap<>();
        Map<String, Set<Integer>> sampleNamesPlatesIndices = new HashMap<>();
        for (int i = lo; i < hi; i++) {
            validationErrorMsg.put(i, new LinkedHashMap<>());
            currentItem = getEditList().get(i);

            // Skip the validation if the item is marked for deletion.
            if (!currentItem.isDeleted()) {
                // Order item specific validation.
                if (renderedColumnsMap.get(Constants.SERVICE) && currentItem.getService() == null) {
                    validationErrorMsg.get(i).put(Constants.SERVICE, Constants.REQUIRED);
                }
                orderItemService.isValidOrderItem(currentItem, getOrder(), requiredColumnsMap, validationErrorMsg, i);

                // Plate specific validation
                Plate plate = currentItem.getPlate();
                if (plate != null && !plates.contains(plate)) {
                    plates.add(plate);
                    if (StringHelper.isNotEmpty(plate.getNameGiven()) && !correctPlateNameFormat(plate.getNameGiven())) {
                        validationErrorMsg.get(i).put(Constants.PLATE_NAME_GIVEN, Messages.get("invalidCharacter"));
                    }

                    if (plate.getPlateLayout() == null) {
                        validationErrorMsg.get(i).put(Constants.PLATE_LAYOUT, Constants.REQUIRED);
                    }

                    if (plate.getPlateIdsNonEmptySampleNamesMap().isEmpty()) {
                        validationErrorMsg.get(i).put(Constants.SAMPLE_PLATE_ASSIGNMENT, Constants.REQUIRED);
                    } else if (plate.getPlateIdsNonEmptySampleNamesMap().get(plate.hashCode()) != null) {
                        sampleCounter = sampleCounter + plate.getPlateIdsNonEmptySampleNamesMap().get(plate.hashCode());
                    }

                    if (plate.getSamplePlatePositionsPlateSubmission().isEmpty() && plate.getSamplePlatePositionsPlateSubmissionOrderedByAssignmentOrder().isEmpty()) {
                        plate.initSamplePlatePositionsPlateSubmissionCollections(getSampleType(), currentItem.getOrder());
                    }
                    for (SamplePlatePosition samplePlatePosition : plate.getCurrentSamplePlatePositionsWithNonEmptySampleNames()) {
                        if (!plateIndexSampleNamesCounter.containsKey(i)) {
                            plateIndexSampleNamesCounter.put(i, new HashMap<>());
                        }

                        String sampleNameLowerCase = samplePlatePosition.getSample().getName().toLowerCase();
                        if (!sampleNamesPlatesIndices.containsKey(sampleNameLowerCase)) {
                            sampleNamesPlatesIndices.put(sampleNameLowerCase, new HashSet<>());
                        }
                        sampleNamesPlatesIndices.get(sampleNameLowerCase).add(i);

                        if (!plateIndexSampleNamesCounter.get(i).containsKey(sampleNameLowerCase)) {
                            plateIndexSampleNamesCounter.get(i).put(sampleNameLowerCase, 1);
                        } else {
                            // At this point, the sample name 'sampleNameLowerCase' is not unique within the plate.
                            plateIndexSampleNamesCounter.get(i).put(sampleNameLowerCase, plateIndexSampleNamesCounter.get(i).get(sampleNameLowerCase) + 1);
                            if (!notUniqueSampleNameWithinPlateIndices.containsKey(sampleNameLowerCase)) {
                                notUniqueSampleNameWithinPlateIndices.put(sampleNameLowerCase, new HashSet<>());
                            }
                            notUniqueSampleNameWithinPlateIndices.get(sampleNameLowerCase).add(i);
                        }
                    }
                }
                if (validationErrorMsg.get(i).isEmpty()) {
                    // The row contains no errors, so the entry can be removed entirely.
                    validationErrorMsg.remove(i);
                }
            }
        }

        // Check name uniqueness within a plate.
        for (Map.Entry<String, Set<Integer>> entry : notUniqueSampleNameWithinPlateIndices.entrySet()) {
            isValidSampleNamesWithinPlateOrAcrossPlates(entry.getKey(), entry.getValue(), false, validationErrorMsg);
        }

        // Check name uniqueness across a plate.
        for (Map.Entry<String, Set<Integer>> entry : sampleNamesPlatesIndices.entrySet()) {
            isValidSampleNamesWithinPlateOrAcrossPlates(entry.getKey(), entry.getValue(), true, validationErrorMsg);
        }

        if (sampleCounter > super.getMaxNumberOfBatchItems()) {
            validationErrorMsg.put(sampleLimitValidationErrorMsgKey, new LinkedHashMap<>());
            validationErrorMsg.get(sampleLimitValidationErrorMsgKey)
                .put(Constants.EDIT + ":sampleLimit", Messages.get("sampleLimitExceeded").replace("{0}", Integer.toString(super.getMaxNumberOfBatchItems()))
                    .replace("{1}", Integer.toString(sampleCounter)).replace("{2}", Integer.toString(plateCounter)));
        }
        return validationErrorMsg;
    }

    private void isValidSampleNamesWithinPlateOrAcrossPlates(String sampleName, Set<Integer> plateIndices, boolean acrossPlates, LinkedHashMap<
        Integer, LinkedHashMap<String, String>> validationErrorMsg) {
        if (StringHelper.isNotEmpty(sampleName) && plateIndices != null && validationErrorMsg != null && (!acrossPlates || plateIndices.size() > 1)) {
            for (int plateIndex : plateIndices) {
                if (!validationErrorMsg.containsKey(plateIndex)) {
                    validationErrorMsg.put(plateIndex, new LinkedHashMap<>());
                }

                if (!validationErrorMsg.get(plateIndex).containsKey(Constants.SAMPLE_PLATE_ASSIGNMENT)) {
                    validationErrorMsg.get(plateIndex).put(Constants.SAMPLE_PLATE_ASSIGNMENT, Messages.get("namesNotUniqueException") + ": " + sampleName);
                } else if (!validationErrorMsg.get(plateIndex).get(Constants.SAMPLE_PLATE_ASSIGNMENT).equals(Constants.REQUIRED)) {
                    validationErrorMsg.get(plateIndex).put(Constants.SAMPLE_PLATE_ASSIGNMENT, validationErrorMsg.get(plateIndex).get(Constants.SAMPLE_PLATE_ASSIGNMENT) + ", " + sampleName);
                }

                if (validationErrorMsg.get(plateIndex).isEmpty()) {
                    // The row contains no errors, so the entry can be removed entirely.
                    validationErrorMsg.remove(plateIndex);
                }
            }
        }
    }

    public void prepareSampleNamesModalPanel(OrderItem orderItem) {
        Plate plate = orderItem.getPlate();
        if (plate != null) {
            setEditedPlate(plate);
            getEditedPlate().initSamplePlatePositionsPlateSubmissionCollections(getSampleType(), orderItem.getOrder());
        }
    }

    @Override
    public void removeEmptyLines() {
        final ListIterator<OrderItem> listIterator = getEditList().listIterator();
        while (listIterator.hasNext()) {
            final OrderItem currentItem = listIterator.next();
            if (currentItem.isEmpty()) {
                getCreatedList().remove(currentItem);
                listIterator.remove();
            }
        }

        updateMaxNumberOfNewLines();
    }

    @Override
    public String save() {
        removeEmptyLines();

        String redirectURL = super.save();
        if (redirectURL != null) {
            String message = orderItemService.saveOrderItems(getEditList(), getDeleteList(), getOrder());
            getFacesMessagesManager().bufferWarningClear(message);
            return redirectURL;
        }
        handleValidationErrorsForBatch(getValidationErrorMsg());
        return null;
    }

    public void setEditList(List<OrderItem> editList) {
        this.editList = editList;
    }

    public void setEditedPlate(Plate editedPlate) {
        this.editedPlate = editedPlate;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    private void updateSampleAssignmentPerRow(Plate changedPlate, Object value, int rowIndex) {
        if (changedPlate != null && value != null) {
            if (!getCurrentUser().hasRoleImplicit(RoleEnum.CONTAINERMANAGER)) {
                changedPlate.setSampleAssignmentPerRow(false);
            }
            dataTableHelper.updateCell(getBatchTableId(), Constants.SAMPLE_PLATE_ASSIGNMENT_ORDER + Constants.GROUP, rowIndex, false);
        }
    }

    @Override
    public void valueChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final Object value = event.getNewValue();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        final String columnId = clientId.split(":")[3].replaceAll(Constants.INPUT, Constants.EMPTY_STRING);
        final OrderItem changedOrderItem = getEditList().get(rowIndex);
        final Plate changedPlate = changedOrderItem.getPlate();

        if (!getValidationErrorMsg().containsKey(rowIndex)) {
            getValidationErrorMsg().put(rowIndex, new LinkedHashMap<>());
        }
        getValueChangedValidationErrorMsg().clear();

        boolean isOrderItemSpecificAttribute = isOrderItemSpecificAttribute(columnId);
        if (isOrderItemSpecificAttribute) {
            // Order item specific attributes.
            if (value != null) {
                if (Constants.ORDER_ITEM_INSERT_SIZE.equals(columnId)) {
                    String errorMessage = NumberUtils.isNumericGreaterZero(value);
                    if (errorMessage != null) {
                        getValueChangedValidationErrorMsg().put(columnId, errorMessage);
                    }
                }
            } else if (sampleAttributeHelper.isRenderedColumn(getOrder(), columnId)) {
                getValueChangedValidationErrorMsg().put(columnId, Constants.REQUIRED);
            }
        }

        if (getValueChangedValidationErrorMsg().isEmpty()) {
            source.setValue(value);
            if (!isOrderItemSpecificAttribute) {
                changedPlate.setChanged(true);
                if (Constants.PLATE_LAYOUT.equals(columnId)) {
                    changedPlate.resetSamplePlatePositionsPlateSubmissionCollections();
                    dataTableHelper.updateCell(getBatchTableId(), Constants.SAMPLE_PLATE_ASSIGNMENT + Constants.GROUP, rowIndex, false);
                    updateSampleAssignmentPerRow(changedPlate, value, rowIndex);
                    Ajax.update(Constants.EDIT + ":" + Constants.SAMPLE_PLATE_ASSIGNMENT_ORDER + Constants.WARNING);
                } else if (Constants.SAMPLE_PLATE_ASSIGNMENT_ORDER.equals(columnId)) {
                    changedPlate.resetSamplePlatePositionsPlateSubmissionCollections();
                    Ajax.update(Constants.EDIT + ":" + Constants.SAMPLE_PLATE_ASSIGNMENT_ORDER + Constants.WARNING);
                }
            } else {
                changedOrderItem.setChanged(true);
            }
            if (getValidationErrorMsg().containsKey(rowIndex)) {
                getValidationErrorMsg().get(rowIndex).remove(columnId);
            }
        } else {
            source.setValue(event.getOldValue());
            getValidationErrorMsg().get(rowIndex).putAll(getValueChangedValidationErrorMsg());
            handleValidationErrorsForRow(getValueChangedValidationErrorMsg(), rowIndex);
        }

        getValueChangedValidationErrorMsg().clear();
        updateBatchTable(columnId, rowIndex);
    }

    @Override
    public void valueChangedAll(ValueChangeEvent event) {
        final String columnId = ((UIComponent) event.getSource()).getId().replaceAll(Constants.HEADER_INPUT, Constants.EMPTY_STRING);
        final Object value = event.getNewValue();
        for (int i = 0; i < getEditList().size(); i++) {
            final OrderItem changedOrderItem = getEditList().get(i);
            final Plate changedPlate = changedOrderItem.getPlate();
            if (changedPlate.isEmptyUserSubmitted()) {
                if (Constants.PLATE_LAYOUT.equals(columnId)) {
                    if (changedPlate.getPlateLayout() == null || !changedPlate.getPlateLayout().equals(value)) {
                        changedPlate.setPlateLayout((PlateLayout) value);
                        changedPlate.setChanged(true);
                        changedPlate.resetSamplePlatePositionsPlateSubmissionCollections();
                        dataTableHelper.updateCell(getBatchTableId(), Constants.SAMPLE_PLATE_ASSIGNMENT + Constants.GROUP, i, false);
                        updateSampleAssignmentPerRow(changedPlate, value, i);
                        updateBatchTable(columnId, i);
                    }
                } else if (Constants.SAMPLE_PLATE_ASSIGNMENT_ORDER.equals(columnId) && !((Object) changedPlate.isSampleAssignmentPerRow()).equals(value) && (changedPlate
                    .getPlateLayout() == null || !changedPlate.hasFixedSamplePlateAssignmentOrder())) {
                    changedPlate.setSampleAssignmentPerRow((Boolean) value);
                    changedPlate.setChanged(true);
                    changedPlate.resetSamplePlatePositionsPlateSubmissionCollections();
                    updateBatchTable(columnId, i);
                }
            }
        }
        Ajax.update(Constants.EDIT + ":" + Constants.SAMPLE_PLATE_ASSIGNMENT_ORDER + Constants.WARNING);
    }
}
