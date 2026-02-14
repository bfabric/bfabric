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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Sample;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.AnnotationService;
import org.bfabric.service.OrderItemService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.SampleAttributeHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DualListModel;

@MeasureCalls
@Named
@ViewScoped
public class OrderItemBatchManager extends AbstractSampleBatchManager<Order> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(OrderItemBatchManager.class.getName());

    private final List<OrderItem> createdList = new ArrayList<>();

    private final List<OrderItem> deleteList = new ArrayList<>();

    private final Set<OrderItem> sampleNameEditedList = new HashSet<>();

    @Inject
    private AnnotationService annotationService;

    // Required for caching for the usage inside the bean
    private BfabricLazyDataModel<Sample> availableSamples = null;

    private List<OrderItem> editList = new ArrayList<>();

    private OrderItem editedOrderItem;

    private List<Long> editedSampleIdList = new ArrayList<>();

    @Param
    private Boolean initializeItems;

    private Set<Sample> markedSamples = new HashSet<>();

    private Map<Sample, Boolean> markedSamplesMap = new HashMap<>();

    private String oldSampleName;

    @Inject
    private OrderItemService orderItemService;

    @Inject
    private SampleAttributeHelper sampleAttributeHelper;

    private ServiceArea serviceArea;

    private ServiceType serviceType;

    public OrderItemBatchManager() {
        super(Order.class);
    }

    @Override
    public void addClones() throws CloneNotSupportedException {
        // The current maximum item number value for the tube id.
        final int maxItemNumber = getMaxItemNumber();

        if (getNumberOfNewBatchItems() > getMaxNumberOfNewBatchItems()) {
            setNumberOfNewBatchItems(getMaxNumberOfNewBatchItems());
        }

        initializeJumpToPageAndAddItToCallbackParam();

        final Sample currentCloneItemSample = getCurrentCloneItem().getSample();
        for (int i = 0; i < getCloneItemCount(); i++) {
            final OrderItem orderItem = getCurrentCloneItem().cloneWithClonedSample();
            orderItem.getSample().setName(currentCloneItemSample.getName() + "_Clone_" + (i + 1));

            // Generate tube id.
            orderItem.generateTubeId(maxItemNumber + i + 1, true);

            getEditList().add(orderItem);
            getCreatedList().add(orderItem);
        }

        setNumberOfNewBatchItems(1);
        updateMaxNumberOfNewLines();

        // Reset the clone count.
        setCloneItemCount(1);
    }

    @Override
    public void addNewBatchItems() {
        if (getSampleType() != null) {
            // The current maximum item number value for the tube id.
            final int maxItemNumber = getMaxItemNumber();

            if (getNumberOfNewBatchItems() > getMaxNumberOfNewBatchItems()) {
                setNumberOfNewBatchItems(getMaxNumberOfNewBatchItems());
            }

            initializeJumpToPageAndAddItToCallbackParam();

            for (int i = 0; i < getNumberOfNewBatchItems(); i++) {
                final OrderItem orderItem = createNewOrderItem();

                // Generate tube id.
                orderItem.generateTubeId(maxItemNumber + i + 1, false);
                if (orderItem.getOrder().isOrderItemTubeIdRendered()) {
                    orderItem.setTubeIdOld(orderItem.getTubeId());
                } else {
                    orderItem.setTubeIdOld(orderItem.getSample().getTubeId());
                }

                getEditList().add(orderItem);
                getCreatedList().add(orderItem);

                if (getOrder().hasSequencingApplicationReadyMadeLibraries() || getOrder().hasServiceTypeReadyMadeLibrariesSequencing()) {
                    // Set the attribute 'multiplexed' and 'multiplexedByUser' of the created sample.
                    orderItem.getSample().setMultiplexed(Boolean.TRUE);
                    orderItem.getSample().setMultiplexedByUser(Boolean.TRUE);
                    // Set the attribute 'Sample Preparation Protocol' to the library protocol from the order if possible iff it is not set.
                    if (getOrder().hasServiceTypeReadyMadeLibrariesSequencing() && orderItem.getSample().getSamplePreparationProtocol() == null) {
                        orderItem.getSample().setSamplePreparationProtocolFromOrder();
                    }
                }
            }
            setNumberOfNewBatchItems(1);
            updateMaxNumberOfNewLines();
        }
    }

    private void addNewItems(Set<Sample> samples) {
        if (getSampleType() != null) {
            // The current maximum item number value for the tube id.
            final int maxItemNumber = getMaxItemNumber();
            initializeJumpToPageAndAddItToCallbackParam();

            final HashMap<Long, Sample> idSampleMap = new HashMap<>();
            for (final OrderItem orderItem : getEditList()) {
                idSampleMap.put(orderItem.getSample().getId(), orderItem.getSample());
            }

            List<Sample> orderedSampleList = CollectionHelper.sortObjects(samples);
            int i = 0;
            for (final Sample sample : orderedSampleList) {
                final OrderItem orderItem = createNewOrderItem();
                if (idSampleMap.containsKey(sample.getId())) {
                    final Sample addSample = idSampleMap.get(sample.getId());
                    orderItem.setSample(addSample);
                } else {
                    orderItem.setSample(sample);
                }

                // Generate tube id.
                orderItem.generateTubeId(maxItemNumber + i + 1, false);

                getEditList().add(orderItem);
                getCreatedList().add(orderItem);
                getEditedSampleIdList().add(sample.getId());
                i++;
            }

            updateMaxNumberOfNewLines();
            updateRowStyleClassAndRowTitleCoupled();
        } else {
            logger.warning("getSampleType() returned null. This should not happen.");
        }
    }

    @Override
    public void applyChangesToMultiValueField() {
        super.applyChangesToMultiValueField();
        updateCoupling(getEditedOrderItem(), getSampleAttributeEnum().getName());
        setEditedOrderItem(null);
    }

    public void applyChangesToMultiValueFields() {
        for (final OrderItem currentItem : getEditList()) {
            try {
                PropertyUtils.setProperty(currentItem.getSample(), getSampleAttributeEnum().getName(), ((DualListModel<Annotation>) PropertyUtils.getProperty(this, "allRowsModal")).getTarget());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
            currentItem.getSample().setChanged(true);
        }
        setAllRowsModal(null);
        updateCouplingForAllRowsChange(getSampleAttributeEnum().getName());
    }

    @Override
    public void clearGeneratedSampleNames() {
        clearGeneratedSampleNames(getEditList().stream().map(OrderItem::getSample).collect(Collectors.toList()));
    }

    private OrderItem coupleAndGetBaseItem(int indexComparedOrderItem, int indexChangedOrderItem, OrderItem comparedItem, OrderItem changedOrderItem) {
        OrderItem baseItem;
        if (indexComparedOrderItem < indexChangedOrderItem) {
            // The compared order item is higher up in the createdList than the changed order item.
            updateTubeId(comparedItem, changedOrderItem);
            changedOrderItem.setSample(comparedItem.getSample());
            changedOrderItem.setChanged(true);
            baseItem = comparedItem;
        } else {
            // The compared order item is further down in the createdList than the changed order item.
            if (comparedItem.getCoupledNotManagedHashCodesOrderItemsMap().isEmpty()) {
                updateTubeId(changedOrderItem, comparedItem);
                comparedItem.setSample(changedOrderItem.getSample());
                comparedItem.setChanged(true);
                baseItem = changedOrderItem;
            } else {
                updateTubeId(comparedItem, changedOrderItem);
                changedOrderItem.setSample(comparedItem.getSample());
                if (changedOrderItem.getOrder().isOrderItemTubeIdRendered()) {
                    changedOrderItem.setTubeId(changedOrderItem.getTubeIdOld());
                } else {
                    changedOrderItem.getSample().setTubeId(changedOrderItem.getTubeIdOld());
                }
                changedOrderItem.setChanged(true);
                baseItem = comparedItem;
            }
        }
        return baseItem;
    }

    private OrderItem createNewOrderItem() {
        OrderItem orderItem;
        orderItem = new OrderItem();
        orderItem.setOrder(getOrder());

        orderItem.setSample(new Sample());
        orderItem.getSample().setContainer(orderItem.getOrder());
        orderItem.getSample().setType(getSampleType().getName());
        orderItem.getSample().setSampleType(getSampleType());

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
        updateRowStyleClassAndRowTitleCoupled();
    }

    @Override
    public void generateSampleNames() {
        generateSampleNames(getOrder(), getEditList().stream().map(OrderItem::getSample).collect(Collectors.toList()));
    }

    public BfabricLazyDataModel<Sample> getAvailableSamples() {
        if (availableSamples == null && getOrder() != null) {
            Set<Container> containers = new HashSet<>();
            containers.add(getOrder());
            containers.add(getOrder().getContainer());
            availableSamples = sampleService.getAvailableSamplesByContainersAndSampleType(containers, getSampleType());
        }
        return availableSamples;
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

    private OrderItem getEditedOrderItem() {
        return editedOrderItem;
    }

    public List<Long> getEditedSampleIdList() {
        return editedSampleIdList;
    }

    public Set<Sample> getMarkedSamples() {
        return markedSamples;
    }

    public Map<Sample, Boolean> getMarkedSamplesMap() {
        return markedSamplesMap;
    }

    private int getMaxItemNumber() {
        int maxItemNumber = 0;
        if (getOrder() != null) {
            final List<OrderItem> orderItemsList = new ArrayList<>();
            orderItemsList.addAll(getOrder().getOrderItems());
            orderItemsList.addAll(getEditList());

            for (final OrderItem item : orderItemsList) {
                final String tubeId = item.getTubeId() != null ? item.getTubeId() : item.getSample().getTubeId();
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

    public int getNumberOfNonEditableItems() {
        return getOrder() != null && getOrder().getOrderItems() != null && getEditList() != null ? getOrder().getOrderItems().size() - getEditList().size() : 0;
    }

    public Order getOrder() {
        return getInstance();
    }

    public ServiceArea getServiceArea() {
        return serviceArea;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    private List<OrderItem> getUpdatedList() {
        final List<OrderItem> ret = new ArrayList<>(getEditList());
        ret.removeAll(getCreatedList());
        return ret;
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
            setAnnotationTypeAnnotationNamesAnnotationMap(null);
            initializeEditList();
            initializeInitialParentSamplesOfUserMultiplexForAllPooledLibraries(getEditList().stream().map(OrderItem::getSample).collect(Collectors.toList()));
            initializeSelectAllValuesForIlluminaLibraryCalculation(getEditList().stream().map(OrderItem::getSample).collect(Collectors.toList()));
            updateMaxNumberOfNewLines();
            getCustomListingRows();
            if (initializeItems != null && initializeItems && getOrder() != null && getOrder().getOrderItems().isEmpty() && getOrder().getNumberOfSamples() != null) {
                setNumberOfNewBatchItems(getOrder().getNumberOfSamples());
                addNewBatchItems();
            } else if (getOrder() != null && getOrder().isProcessesSamples() && getOrder().getOrderItems().isEmpty() && getOrder().isUpdatable()) {
                // Show per default an empty order item iff the order processes samples, has no order items, and is updatable.
                setNumberOfNewBatchItems(1);
                addNewBatchItems();
            }
        }
    }

    @Override
    public void initializeEditList() {
        if (getOrder() != null && !getOrder().getOrderItems().isEmpty() && getOrder().isUpdatable()) {
            for (OrderItem orderItem : orderItemService.getEditableOrderItemsByOrder(getOrder())) {
                if (orderItem.getSample() != null && orderItem.getSample().isUpdatable()) {
                    getEditList().add(orderItem);
                    getEditedSampleIdList().add(orderItem.getSample().getId());
                }
            }
            getOrder().initializeRowStyleClassAndRowTitleCoupled(getEditList(), false);
        }
    }

    @Override
    public void inputQcSampleChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        if (event.getNewValue() != null) {
            inputQcSampleChanged((Sample) event.getNewValue(), getEditList().get(rowIndex).getSample());
            updateCoupling(getEditList().get(rowIndex), SampleAttributeEnum.CONCENTRATION_INPUT_QC.getName());
        } else {
            getEditList().get(rowIndex).getSample().setInputQcSample(null);
        }
        updateBatchTable(SampleAttributeEnum.CONCENTRATION_INPUT_QC.getName(), rowIndex);
    }

    public boolean isAddNewItemsSampleDisabled() {
        return getMarkedSamples().size() > getMaxNumberOfNewBatchItems();
    }

    private boolean isOrderItemSpecificAttribute(String columnId) {
        return Constants.SERVICE.equals(columnId) || Constants.ORDER_ITEM_LIBRARY_TYPE.equals(columnId) || Constants.ORDER_ITEM_REGION.equals(columnId) || Constants.ORDER_ITEM_READ_TYPE
            .equals(columnId) || Constants.ORDER_ITEM_INSERT_SIZE.equals(columnId) || Constants.ORDER_ITEM_MULTIPLEXING.equals(columnId);
    }

    public boolean isSampleNamesNotEditable() {
        return super.isSampleNamesNotEditable() || !getOrder().getOrderItems().isEmpty();
    }

    @Override
    public LinkedHashMap<Integer, LinkedHashMap<String, String>> isValid(int lo, int hi) {
        LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg = new LinkedHashMap<>();

        OrderItem currentItem;
        int lo_adapted = lo;
        int hi_adapted = Math.min(hi, getEditList().size());

        // Caching the rendered columns.
        final Map<String, Boolean> renderedColumnsMap = new HashMap<>();
        renderedColumnsMap.put(Constants.SERVICE, sampleAttributeHelper.isRenderedColumn(getOrder(), Constants.SERVICE));

        // Caching the required columns.
        final Map<String, Boolean> requiredColumnsMap = orderItemService.createRequiredColumnsMap(getOrder());

        Set<SampleAttributeEnum> sampleFormDependentAttributesRequired = new HashSet<>(SampleAttributeEnum.getSampleFormDependentRequiredAttributes());
        Set<SampleAttributeEnum> qcTypeDependentAttributesRequired = new HashSet<>(SampleAttributeEnum.getQcTypeDependentRequiredAttributes());
        Set<SampleAttributeEnum> booleanAttributesRequired = new HashSet<>(SampleAttributeEnum.getBooleanRequiredAttributes());
        for (int i = lo_adapted; i < hi_adapted; i++) {
            validationErrorMsg.put(i, new LinkedHashMap<>());
            currentItem = getEditList().get(i);
            // Skip the validation if the item is marked for deletion.
            if (!currentItem.isDeleted()) {
                // Check multiplex id if the sample is multiplexed and user submitted.
                if (SampleAttributeEnum.MULTIPLEXED.isAttribute(currentItem.getSample().getType())) {
                    String errorMessage = currentItem.getSample().isValidMultiplexedByUser();
                    if (errorMessage != null) {
                        validationErrorMsg.get(i).put(SampleAttributeEnum.MULTIPLEXED.getName(), errorMessage);
                    }
                }
                // Check sample form dependent attributes, i.e., for all sample types which have the attribute SAMPLE_FORM.
                if (SampleAttributeEnum.hasSampleTypeSampleForm(getSampleType().getName()) && currentItem.getSample().getSampleForm() != null) {
                    for (SampleAttributeEnum aSampleAttributeEnum : sampleFormDependentAttributesRequired) {
                        try {
                            validationErrorMsg.get(i)
                                .putAll(isValidSampleFormDependentAttribute(PropertyUtils.getProperty(currentItem.getSample(), aSampleAttributeEnum.getName()), currentItem.getSample()
                                    .getType(), aSampleAttributeEnum, currentItem.getSample().getSampleForm()));
                        } catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException ignored) {
                        }
                    }
                }
                // Check qc type dependent attributes.
                if (getSampleType().getName().equals(SampleTypeEnum.QUALITY_CONTROL.getLabel()) && currentItem.getSample().getQualityControlType() != null) {
                    for (SampleAttributeEnum aSampleAttributeEnum : qcTypeDependentAttributesRequired) {
                        try {
                            validationErrorMsg.get(i).putAll(isValidQcTypeDependentAttribute(PropertyUtils.getProperty(currentItem.getSample(), aSampleAttributeEnum.getName()), currentItem.getSample()
                                .getType(), aSampleAttributeEnum, currentItem.getSample().getQualityControlType()));
                        } catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException ignored) {
                        }
                    }
                }
                // Check non-primitive boolean values (NULL, TRUE, FALSE).
                for (SampleAttributeEnum aSampleAttributeEnum : booleanAttributesRequired) {
                    try {
                        if (aSampleAttributeEnum.isAttribute(currentItem.getSample().getType()) && aSampleAttributeEnum
                            .isEmptySampleAttribute(PropertyUtils.getProperty(currentItem.getSample(), aSampleAttributeEnum.getName()))) {
                            validationErrorMsg.get(i).put(aSampleAttributeEnum.getName(), Constants.REQUIRED);
                        }
                    } catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException ignored) {
                    }
                }
                // Order item specific validation.
                if (renderedColumnsMap.get(Constants.SERVICE) && currentItem.getService() == null) {
                    validationErrorMsg.get(i).put(Constants.SERVICE, Constants.REQUIRED);
                }
                orderItemService.isValidOrderItem(currentItem, getOrder(), requiredColumnsMap, validationErrorMsg, i);
            }

            if (validationErrorMsg.get(i).isEmpty()) {
                // The row contains no errors, so the entry can be removed entirely.
                validationErrorMsg.remove(i);
            }
        }

        if (lo_adapted < hi_adapted) {
            // Checking for existing names.
            final List<String> existingNames = orderItemService.getExistingNamesByContainerAndEditListAndEditedSampleIdList(getOrder(), getEditList(), editedSampleIdList, lo_adapted, hi_adapted);
            for (int i = lo_adapted; i < hi_adapted; i++) {
                if (!validationErrorMsg.containsKey(i)) {
                    validationErrorMsg.put(i, new LinkedHashMap<>());
                }

                currentItem = getEditList().get(i);
                // Skip the validation for deleted items.
                if ((sampleNameEditedList.contains(currentItem) || getCreatedList().contains(currentItem)) && !currentItem.isDeleted()) {
                    if (StringHelper.isEmpty(currentItem.getSample().getName())) {
                        validationErrorMsg.get(i).put(Constants.SAMPLE_NAME, Constants.REQUIRED);
                    } else if (currentItem.getSample().getId() <= 0 && !correctSampleNameFormat(currentItem.getSample().getName())) {
                        validationErrorMsg.get(i).put(Constants.SAMPLE_NAME, Messages.get("invalidCharacter"));
                    } else {
                        if (existingNames.contains(currentItem.getSample().getName())) {
                            validationErrorMsg.get(i).put(Constants.SAMPLE_NAME, Messages.get("nameNotUniqueWithinContainerException"));
                        } else {
                            // Check the sample name against samples in the editList.
                            for (int j = 0; j < getEditList().size(); j++) {
                                if (j != i) {
                                    final OrderItem comparedItem = getEditList().get(j);
                                    if (!comparedItem.isDeleted() && !comparedItem.getSample().equals(currentItem.getSample()) && comparedItem.getSample().getName().equals(currentItem.getSample()
                                        .getName())) {
                                        if (!validationErrorMsg.containsKey(j)) {
                                            validationErrorMsg.put(j, new LinkedHashMap<>());
                                        }

                                        // This means something is wrong in the database since sample names must be unique within containers.
                                        validationErrorMsg.get(i).put(Constants.SAMPLE_NAME, Messages.get("uncoupleSampleWithSameNameExists"));
                                        validationErrorMsg.get(j).put(Constants.SAMPLE_NAME, Messages.get("uncoupleSampleWithSameNameExists"));

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (validationErrorMsg.get(i).isEmpty()) {
                    // The row contains no errors, so the entry can be removed entirely.
                    validationErrorMsg.remove(i);
                }
            }

            final int managed = getEditList().size() - getCreatedList().size();
            lo_adapted = Math.max(lo, managed) - managed;
            hi_adapted = hi_adapted - managed;
            for (int i = lo_adapted; i < hi_adapted; i++) {
                if (!validationErrorMsg.containsKey(i + managed)) {
                    validationErrorMsg.put(i + managed, new LinkedHashMap<>());
                }
                currentItem = getCreatedList().get(i);
                // Skip the validation if the item is marked for deletion.
                if (!currentItem.isDeleted()) {
                    // Sample attribute specific validation.
                    validationErrorMsg.get(i + managed).putAll(isValidSampleAttributes(currentItem.getSample()));
                }
                if (validationErrorMsg.get(i + managed).isEmpty()) {
                    // The row contains no errors, so the entry can be removed entirely.
                    validationErrorMsg.remove(i + managed);
                }
            }
        }
        return validationErrorMsg;
    }

    public void markSample(Sample sample, boolean isSelect) {
        if (isSelect) {
            getMarkedSamples().add(sample);
            getMarkedSamplesMap().put(sample, Boolean.TRUE);
        } else {
            getMarkedSamples().remove(sample);
            getMarkedSamplesMap().remove(sample);
        }
    }

    public void markSamples(boolean isSelect) {
        if (UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID) != null) {
            String tableClientId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID));
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableClientId);
            List<Sample> dataList = dataTableHelper.getDataTableValues(dataTable);
            for (Sample sample : dataList) {
                markSample(sample, isSelect);
            }
            dataTableHelper.updateColumn(tableClientId, Constants.SELECT + Constants.COLUMN);
        }
    }

    @Override
    public void molaritySampleChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        if (event.getNewValue() != null) {
            molaritySampleChanged((Sample) event.getNewValue(), getEditList().get(rowIndex).getSample());
            updateCoupling(getEditList().get(rowIndex), SampleAttributeEnum.MOLARITY.getName());
        } else {
            getEditList().get(rowIndex).getSample().setMolaritySample(null);
        }
        updateBatchTable(SampleAttributeEnum.MOLARITY.getName(), rowIndex);
    }

    @Override
    public void prepareMultiValueModalPanel(OrderItem orderItem, SampleAttributeEnum attributeEnum) {
        setEditedOrderItem(orderItem);
        super.prepareMultiValueModalPanel(orderItem, attributeEnum);
    }

    public void prepareMultiplexIdsModalPanel(OrderItem orderItem) {
        Sample sample = orderItem.getSample();
        super.prepareMultiplexIdsModalPanel(sample);
    }

    private void propagateCouplingStateAmongNotManagedOrderItems(OrderItem changedOrderItem, OrderItem comparedItem, boolean isAdd) {
        if (isAdd) {
            changedOrderItem.getCoupledNotManagedHashCodesOrderItemsMap().put(comparedItem.hashCode(), comparedItem);
            comparedItem.getCoupledNotManagedHashCodesOrderItemsMap().put(changedOrderItem.hashCode(), changedOrderItem);
            // Propagate the state changed to the already coupled not managed order items.
            for (OrderItem otherOrderItem : comparedItem.getCoupledNotManagedHashCodesOrderItemsMap().values()) {
                if (changedOrderItem.hashCode() != otherOrderItem.hashCode()) {
                    changedOrderItem.getCoupledNotManagedHashCodesOrderItemsMap().put(otherOrderItem.hashCode(), otherOrderItem);
                    otherOrderItem.getCoupledNotManagedHashCodesOrderItemsMap().put(changedOrderItem.hashCode(), changedOrderItem);
                }
            }
        } else {
            changedOrderItem.getCoupledNotManagedHashCodesOrderItemsMap().clear();
            comparedItem.getCoupledNotManagedHashCodesOrderItemsMap().remove(changedOrderItem.hashCode());
            // Propagate the state changed to the already coupled not managed order items.
            for (OrderItem otherOrderItem : comparedItem.getCoupledNotManagedHashCodesOrderItemsMap().values()) {
                otherOrderItem.getCoupledNotManagedHashCodesOrderItemsMap().remove(changedOrderItem.hashCode());
            }
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

    public void sampleNameChanged(ValueChangeEvent event) {
        oldSampleName = (String) event.getOldValue();

        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        getEditList().get(rowIndex).getSample().setChanged(true);
        sampleNameEditedList.add(getEditList().get(rowIndex));
    }

    @Override
    public String save() {
        for (OrderItem orderItem : getEditList()) {
            resetSampleFields(orderItem.getSample());
        }
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

    @Override
    public void saveAnnotation() {
        if (!annotationService.checkUniqueName(getAnnotation())) {
            getFacesMessagesManager().validationError("annotationName", Messages.get("nameNotUniqueForTypeException").replace("{0}", getAnnotation().getType()));
            FacesContext.getCurrentInstance().validationFailed();
        } else {
            entityService.persist(getAnnotation());
            if (cachedSelectionValuesListsHashMap.containsKey(getAnnotation().getType())) {
                cachedSelectionValuesListsHashMap.remove(getAnnotation().getType());
            } else if (getOrder().getServiceType() != null && getOrder().getServiceType().getSampleType() != null) {
                cachedSelectionValuesListsHashMap.remove(getAnnotation().getType() + getOrder().getServiceType().getSampleType().getName());
            }
            setAnnotation(null);
        }
    }

    @Override
    public void selectedMultiplexIdChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        final String columnId = clientId.split(":")[3].replaceAll(Constants.INPUT, Constants.EMPTY_STRING).replaceAll(Constants.SELECTED, Constants.EMPTY_STRING);
        if (event.getNewValue() != null) {
            getEditList().get(rowIndex).getSample().selectedMultiplexIdChangedHelper(event, columnId);
            updateCoupling(getEditList().get(rowIndex), columnId);
        }
        updateBatchTable(columnId, rowIndex);
    }

    @Override
    public void setCurrentCloneItem(AbstractEntity currentCloneItem) {
        // Make sure, it is not detached (lazy loading...)
        if (currentCloneItem.getId() != 0) {
            super.setCurrentCloneItem(entityService.find(OrderItem.class, currentCloneItem.getId()));
        } else {
            super.setCurrentCloneItem(currentCloneItem);
        }
    }

    public void setEditList(List<OrderItem> editList) {
        this.editList = editList;
    }

    private void setEditedOrderItem(OrderItem editedOrderItem) {
        this.editedOrderItem = editedOrderItem;
    }

    public void setEditedSampleIdList(List<Long> editedSampleIdList) {
        this.editedSampleIdList = editedSampleIdList;
    }

    public void setMarkedSamples(Set<Sample> markedSamples) {
        this.markedSamples = markedSamples;
    }

    public void setMarkedSamplesMap(Map<Sample, Boolean> markedSamplesMap) {
        this.markedSamplesMap = markedSamplesMap;
    }

    private void updateCoupledCells(OrderItem changedOrderItem, String columnName, int index, boolean updateName) {
        int start;
        int end;
        if (index == -1) {
            // Update all items that are coupleable with the changed item.
            start = 0;
            end = getEditList().size();
        } else {
            // Update only the changed item.
            start = index;
            end = start + 1;
        }

        for (int i = start; i < end; ++i) {
            final OrderItem comparedItem = getEditList().get(i);
            if (comparedItem.isCoupleable(changedOrderItem)) {
                if (!columnName.equals(Constants.SAMPLE_NAME)) {
                    // In case a coupleable attribute was changed except for the sample name.
                    if (updateName) {
                        updateCell(Constants.SAMPLE_NAME, i);
                    }
                    updateCell(columnName, i);
                    if (comparedItem.getOrder().isOrderItemTubeIdRendered()) {
                        updateCell(Constants.ORDER_ITEM_TUBE_ID + Constants.COLUMN, i);
                    } else {
                        updateCell(Constants.SAMPLE_TUBE_ID + Constants.COLUMN, i);
                    }
                    updateSubTypeDependentBatchTable(columnName, i);
                    updateCalculationDependentBatchTable(columnName, i);
                } else {
                    // In case the sample name was changed and the two samples were coupled together.
                    final DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(getBatchTableId());
                    Ajax.updateRow(dataTable, i);
                }
            }
        }
    }

    private void updateCoupledOrderItemsTubeIds(OrderItem baseItem) {
        if (!baseItem.getCoupledNotManagedHashCodesOrderItemsMap().isEmpty()) {
            final List<OrderItem> orderedByTubeIdOldPadded = new ArrayList<>();
            orderedByTubeIdOldPadded.add(baseItem);
            orderedByTubeIdOldPadded.addAll(baseItem.getCoupledNotManagedHashCodesOrderItemsMap().values());
            orderedByTubeIdOldPadded.sort(Comparator.comparing(OrderItem::getTubeIdOldPadded));

            final OrderItem firstOrderItem = orderedByTubeIdOldPadded.remove(0);
            for (OrderItem toUpdate : orderedByTubeIdOldPadded) {
                if (toUpdate.getOrder().isOrderItemTubeIdRendered()) {
                    toUpdate.setTubeId(firstOrderItem.getTubeId());
                } else {
                    toUpdate.getSample().setTubeId(firstOrderItem.getSample().getTubeId());
                }
            }
        }
    }

    public void updateCoupling(OrderItem changedOrderItem, String columnName) {
        if (changedOrderItem.getId() == 0) {
            // The changed item is not managed.
            final Set<Sample> updatedSamples = getUpdatedList().stream().map(OrderItem::getSample).collect(Collectors.toSet());
            final Set<Integer> coupledMessageIndices = new HashSet<>();
            if (changedOrderItem.getCoupledNotManagedHashCodesOrderItemsMap().isEmpty()) {
                // The not managed order item is not coupled with any not managed order item.
                for (final OrderItem comparedItem : getCreatedList()) {
                    if (!comparedItem.equals(changedOrderItem) && !updatedSamples.contains(comparedItem.getSample()) && comparedItem.isCoupleable(changedOrderItem)) {
                        // Do not compare the same order item with itself and all the samples are not coupled with any managed order item.
                        final int indexChangedOrderItem = getEditList().indexOf(changedOrderItem);
                        final int indexComparedOrderItem = getEditList().indexOf(comparedItem);

                        if (indexComparedOrderItem != -1 && indexChangedOrderItem != -1) {
                            final OrderItem baseItem = coupleAndGetBaseItem(indexComparedOrderItem, indexChangedOrderItem, comparedItem, changedOrderItem);
                            propagateCouplingStateAmongNotManagedOrderItems(changedOrderItem, comparedItem, true);
                            coupledMessageIndices.add(indexChangedOrderItem);
                            coupledMessageIndices.add(indexComparedOrderItem);
                            getFacesMessagesManager().validationError(getSampleNameInputId(indexChangedOrderItem), Messages.get("coupled"));
                            updateCoupledCells(changedOrderItem, columnName, indexChangedOrderItem, true);
                            updateCoupledCells(changedOrderItem, columnName, indexComparedOrderItem, true);

                            boolean updateName = false;
                            // Iterate through every not managed item and check if it is coupleable after.
                            for (int i = 0; i < getEditList().size(); ++i) {
                                final OrderItem otherOrderItem = getEditList().get(i);
                                if (otherOrderItem != null && otherOrderItem.equals(baseItem) && otherOrderItem.getSample() != null && !otherOrderItem.getSample()
                                    .equals(baseItem.getSample()) && !coupledMessageIndices.contains(i) && baseItem.isCoupleable(otherOrderItem)) {
                                    updateTubeId(baseItem, otherOrderItem);
                                    otherOrderItem.setSample(baseItem.getSample());
                                    otherOrderItem.setChanged(true);
                                    propagateCouplingStateAmongNotManagedOrderItems(changedOrderItem, otherOrderItem, true);
                                    coupledMessageIndices.add(i);
                                    getFacesMessagesManager().validationError(getSampleNameInputId(i), Messages.get("coupled"));
                                    updateName = true;
                                }
                            }
                            updateCoupledCells(changedOrderItem, columnName, -1, updateName);
                            break;
                        }
                    }
                }
            } else {
                // The not managed order item is already coupled with at least one not managed order item.
                for (final OrderItem comparedItem : getCreatedList()) {
                    if (!comparedItem.equals(changedOrderItem) && !updatedSamples.contains(comparedItem.getSample())) {
                        // Do not compare the same order item with itself and all the samples are not coupled with any managed order item.
                        if (comparedItem.getSample().equals(changedOrderItem.getSample()) && columnName.equals(Constants.SAMPLE_NAME)) {
                            // At this point, we have two ALREADY coupled items where the sample name was changed.
                            changedOrderItem.decouple(comparedItem, oldSampleName);
                            final int indexChangedOrderItem = getEditList().indexOf(changedOrderItem);
                            final int indexComparedOrderItem = getEditList().indexOf(comparedItem);
                            if (indexComparedOrderItem != -1 && indexChangedOrderItem != -1) {
                                propagateCouplingStateAmongNotManagedOrderItems(changedOrderItem, comparedItem, false);
                                getFacesMessagesManager().validationError(getSampleNameInputId(indexChangedOrderItem), Messages.get("decoupledFrom") + " " + (comparedItem.getOrder()
                                    .isOrderItemTubeIdRendered() ? comparedItem.getTubeId() : comparedItem.getSample().getName()));
                                updateCoupledCells(changedOrderItem, columnName, indexChangedOrderItem, false);
                                updateCoupledCells(comparedItem, columnName, indexComparedOrderItem, false);
                                changedOrderItem.setChanged(true);
                                changedOrderItem.getSample().setChanged(true);
                                comparedItem.setChanged(true);
                                comparedItem.getSample().setChanged(true);

                                // At this point, we have at least one not managed order item, which is still coupled with the compared item, that might need to have its tube id adapted.
                                updateCoupledOrderItemsTubeIds(comparedItem);

                                // At this point, we have the not managed order item, which was decoupled, that might be coupeable after.
                                for (final OrderItem otherComparedItem : getCreatedList()) {
                                    if (!otherComparedItem.equals(changedOrderItem) && !updatedSamples.contains(otherComparedItem.getSample()) && (otherComparedItem
                                        .isCoupleable(changedOrderItem) || changedOrderItem.isCoupleable(otherComparedItem))) {
                                        // Do not compare the same order item with itself and all the samples are not coupled with any managed order item.
                                        final int indexOtherComparedOrderItem = getEditList().indexOf(otherComparedItem);
                                        if (indexOtherComparedOrderItem != -1) {
                                            final OrderItem baseItem = coupleAndGetBaseItem(indexOtherComparedOrderItem, indexChangedOrderItem, otherComparedItem, changedOrderItem);
                                            propagateCouplingStateAmongNotManagedOrderItems(changedOrderItem, otherComparedItem, true);
                                            getFacesMessagesManager().validationError(getSampleNameInputId(indexChangedOrderItem), Messages.get("coupled"));
                                            updateCoupledCells(changedOrderItem, columnName, indexChangedOrderItem, true);
                                            updateCoupledCells(changedOrderItem, columnName, indexOtherComparedOrderItem, true);
                                            updateCoupledOrderItemsTubeIds(baseItem);
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        updateCoupledCells(changedOrderItem, columnName, -1, false);
                    }
                }
            }

            for (final OrderItem comparedItem : getUpdatedList()) {
                // Compare the changed item with the other managed items in the editList.
                if (comparedItem.getSample() != null && changedOrderItem.getSample() != null) {
                    if (comparedItem.getSample().equals(changedOrderItem.getSample()) && columnName.equals(Constants.SAMPLE_NAME)) {
                        // At this point, we have two ALREADY coupled items where the sample name was changed.
                        changedOrderItem.decouple(comparedItem, oldSampleName);
                        final int index = getEditList().indexOf(changedOrderItem);
                        if (index != -1) {
                            getFacesMessagesManager()
                                .validationError(getSampleNameInputId(index), Messages.get("decoupledFrom") + " " + (comparedItem.getOrder()
                                    .isOrderItemTubeIdRendered() ? comparedItem.getTubeId() : comparedItem.getSample().getName()));
                            updateCoupledCells(changedOrderItem, columnName, index, false);
                        }

                        changedOrderItem.setChanged(true);
                        changedOrderItem.getSample().setChanged(true);
                        comparedItem.setChanged(true);
                        comparedItem.getSample().setChanged(true);
                        break;
                    }

                    if (changedOrderItem.getSample().equals(comparedItem.getSample())) {
                        // At this point, we have two ALREADY coupled items where an attribute was changed.
                        boolean updateName = false;
                        // Iterate through every not managed item and check if it is coupleable after.
                        for (int i = 0; i < getEditList().size(); ++i) {
                            final OrderItem orderItem = getEditList().get(i);
                            if (orderItem != null && orderItem.getId() == 0 && orderItem.getSample() != null && !changedOrderItem.getSample().equals(orderItem.getSample()) && changedOrderItem
                                .isCoupleable(orderItem)) {
                                updateTubeId(comparedItem, changedOrderItem);
                                orderItem.setSample(changedOrderItem.getSample());
                                orderItem.setChanged(true);
                                updateName = true;
                                getFacesMessagesManager().validationError(getSampleNameInputId(i), Messages.get("coupled"));
                            }
                        }
                        updateCoupledCells(changedOrderItem, columnName, -1, updateName);
                        break;
                    }
                    if (comparedItem.isCoupleable(changedOrderItem)) {
                        // At this point, we have two NOT ALREADY coupled items where an attribute was changed.
                        updateTubeId(comparedItem, changedOrderItem);
                        changedOrderItem.setSample(comparedItem.getSample());
                        changedOrderItem.setChanged(true);
                        final int index = getEditList().indexOf(changedOrderItem);
                        if (index != -1) {
                            getFacesMessagesManager().validationError(getSampleNameInputId(index), Messages.get("coupled"));
                            updateCoupledCells(changedOrderItem, columnName, index, true);
                        }
                        break;
                    }
                }
            }
        } else {
            // The changed item is managed.
            for (int i = 0; i < getEditList().size(); ++i) {
                final OrderItem comparedItem = getEditList().get(i);
                if (changedOrderItem.getSample() != null && comparedItem.getSample() != null && changedOrderItem.isCoupleable(comparedItem)) {
                    // At this point, we have two items that are coupleable.
                    boolean updateName = false;
                    if (comparedItem.getId() == 0 && !comparedItem.getSample().equals(changedOrderItem.getSample())) {
                        // At this point, we have two NOT ALREADY coupled items where an attribute was changed.
                        updateTubeId(changedOrderItem, comparedItem);
                        comparedItem.setSample(changedOrderItem.getSample());
                        comparedItem.setChanged(true);
                        updateName = true;
                        getFacesMessagesManager().validationError(getSampleNameInputId(i), Messages.get("coupled"));
                    }
                    // Else --> the items are already coupled.
                    updateCoupledCells(changedOrderItem, columnName, i, updateName);
                }
            }
        }
        updateRowStyleClassAndRowTitleCoupled();
    }

    private void updateCouplingForAllRowsChange(String columnName) {
        final Map<String, OrderItem> managedOrderItemsMap = new HashMap<>();
        final List<OrderItem> notManageOrderItemsList = new ArrayList<>();
        for (final OrderItem item : getEditList()) {
            if (item.getId() != 0 && item.getSample() != null) {
                if (!managedOrderItemsMap.containsKey(item.getSample().getName())) {
                    managedOrderItemsMap.put(item.getSample().getName(), item);
                }
            } else {
                notManageOrderItemsList.add(item);
            }
        }

        final Set<Integer> coupledMessageIndices = new HashSet<>();
        for (final OrderItem comparedItem : notManageOrderItemsList) {
            if (comparedItem.getSample() != null && managedOrderItemsMap.containsKey(comparedItem.getSample().getName()) && !comparedItem.equals(managedOrderItemsMap.get(comparedItem.getSample()
                .getName())) && managedOrderItemsMap.get(comparedItem.getSample().getName()).isCoupleable(comparedItem)) {
                updateTubeId(managedOrderItemsMap.get(comparedItem.getSample().getName()), comparedItem);
                comparedItem.setSample(managedOrderItemsMap.get(comparedItem.getSample().getName()).getSample());

                final int index = getEditList().indexOf(comparedItem);
                if (index != -1) {
                    coupledMessageIndices.add(index);
                    getFacesMessagesManager().validationError(getSampleNameInputId(index), Messages.get("coupled"));
                    Ajax.update(getBatchTableId() + ":" + index + ":" + (comparedItem.getOrder()
                        .isOrderItemTubeIdRendered() ? Constants.ORDER_ITEM_TUBE_ID + Constants.COLUMN : Constants.SAMPLE_TUBE_ID + Constants.COLUMN) + Constants.INPUT);
                    Ajax.update(getBatchTableId() + ":" + index + ":" + Constants.SAMPLE_NAME + Constants.MESSAGE);
                }
            }
        }

        final Set<Sample> updatedSamples = getUpdatedList().stream().map(OrderItem::getSample).collect(Collectors.toSet());
        for (final OrderItem outerItem : notManageOrderItemsList) {
            if (!updatedSamples.contains(outerItem.getSample())) {
                for (final OrderItem innerItem : notManageOrderItemsList) {
                    if (!outerItem.equals(innerItem) && !outerItem.getSample().equals(innerItem.getSample()) && !outerItem.getCoupledNotManagedHashCodesOrderItemsMap()
                        .containsValue(innerItem) && outerItem.isCoupleable(innerItem)) {
                        final int indexOuterItem = getEditList().indexOf(outerItem);
                        final int indexInnerItem = getEditList().indexOf(innerItem);

                        if (indexInnerItem != -1 && indexOuterItem != -1 && !coupledMessageIndices.contains(indexInnerItem) && !coupledMessageIndices.contains(indexOuterItem)) {
                            final OrderItem baseItem = coupleAndGetBaseItem(indexInnerItem, indexOuterItem, innerItem, outerItem);
                            propagateCouplingStateAmongNotManagedOrderItems(outerItem, innerItem, true);
                            coupledMessageIndices.add(indexOuterItem);
                            coupledMessageIndices.add(indexInnerItem);
                            getFacesMessagesManager().validationError(getSampleNameInputId(indexOuterItem), Messages.get("coupled"));
                            getFacesMessagesManager().validationError(getSampleNameInputId(indexInnerItem), Messages.get("coupled"));
                            updateCoupledOrderItemsTubeIds(baseItem);
                            Ajax.update(getBatchTableId() + ":" + indexOuterItem + ":" + Constants.SAMPLE_NAME + Constants.MESSAGE);
                            Ajax.update(getBatchTableId() + ":" + indexInnerItem + ":" + Constants.SAMPLE_NAME + Constants.MESSAGE);
                        }
                    }
                }
            }
        }

        updateColumn((getOrder().isOrderItemTubeIdRendered() ? Constants.ORDER_ITEM_TUBE_ID : Constants.SAMPLE_TUBE_ID) + Constants.COLUMN);
        updateColumn(Constants.SAMPLE_NAME);
        updateColumn(columnName);
        updateRowStyleClassAndRowTitleCoupled();
    }

    private void updateRowStyleClassAndRowTitleCoupled() {
        for (OrderItem orderItem : getEditList()) {
            orderItem.setRowStyleClassCoupled(null);
            orderItem.setRowTitleCoupled(null);
        }
        getOrder().initializeRowStyleClassAndRowTitleCoupled(getEditList(), true);
        updateColumn((getOrder().isOrderItemTubeIdRendered() ? Constants.ORDER_ITEM_TUBE_ID : Constants.SAMPLE_TUBE_ID) + Constants.GROUP + Constants.COLUMN);
        Ajax.update(Constants.EDIT + ":" + Constants.BUTTONS);
    }

    private void updateTubeId(OrderItem comparedItem, OrderItem changedOrderItem) {
        if (comparedItem.getOrder().isOrderItemTubeIdRendered()) {
            if (changedOrderItem.getTubeIdOld() == null) {
                changedOrderItem.setTubeIdOld(changedOrderItem.getTubeId());
            }
            changedOrderItem.setTubeId(comparedItem.getTubeId());
        } else {
            if (changedOrderItem.getTubeIdOld() == null) {
                changedOrderItem.setTubeIdOld(changedOrderItem.getSample().getTubeId());
                if (!comparedItem.isManaged()) {
                    comparedItem.setTubeIdOld(comparedItem.getSample().getTubeId());
                }
            }
        }
    }

    public void useSelectedSamples() {
        addNewItems(getMarkedSamples());
        initializeInitialParentSamplesOfUserMultiplexForAllPooledLibraries(new ArrayList<>(getMarkedSamples()));
        initializeSelectAllValuesForIlluminaLibraryCalculation(new ArrayList<>(getMarkedSamples()));
        getMarkedSamples().clear();
        getMarkedSamplesMap().clear();
    }

    @Override
    public void valueChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final Object value = event.getNewValue();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        final String columnId = clientId.split(":")[3].replaceAll(Constants.INPUT, Constants.EMPTY_STRING);
        final OrderItem changedOrderItem = getEditList().get(rowIndex);
        final Sample changedSample = changedOrderItem.getSample();

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
        } else {
            // Sample specific attributes.
            SampleAttributeEnum sampleAttributeEnum = SampleAttributeEnum.getAttributeByName(columnId);
            if (sampleAttributeEnum != null && !sampleAttributeEnum.isBooleanType()) {
                getValueChangedValidationErrorMsg().putAll(isValidSampleAttribute(value, changedSample.getType(), changedSample.getSampleForm(), changedSample.getQualityControlType(), columnId));
            }
        }

        if (getValueChangedValidationErrorMsg().isEmpty()) {
            source.setValue(value);
            performMultiplexSpecificLogic(columnId, value, SampleAttributeEnum.getAttributeByName(columnId), changedSample);
            if (!isOrderItemSpecificAttribute) {
                changedSample.setChanged(true);
                // Only changes to sample specific attributes require to update the coupling.
                updateCoupling(changedOrderItem, columnId);
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
        super.valueChangedAll(event);
        final UIComponent source = (UIComponent) event.getSource();
        final String columnId = source.getId().replaceAll(Constants.HEADER_INPUT, Constants.EMPTY_STRING);
        updateCouplingForAllRowsChange(columnId);
    }
}