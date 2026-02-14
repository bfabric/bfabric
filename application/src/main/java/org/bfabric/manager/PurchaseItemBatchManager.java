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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Constants;
import org.bfabric.entity.Consumable;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.PurchaseItem;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ConsumableService;
import org.bfabric.service.PurchaseItemService;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.component.autocomplete.AutoComplete;

@MeasureCalls
@Named
@ViewScoped
public class PurchaseItemBatchManager extends AbstractBatchManager<Purchase> {

    private static final long serialVersionUID = 1;

    private final List<PurchaseItem> createdList = new ArrayList<>();

    private final List<PurchaseItem> deleteList = new ArrayList<>();

    @Inject
    private ConsumableService consumableService;

    private List<PurchaseItem> editList = new ArrayList<>();

    @Inject
    private PurchaseItemService purchaseItemService;

    public PurchaseItemBatchManager() {
        super(Purchase.class);
    }

    @Override
    public void addNewBatchItems() {
        if (getNumberOfNewBatchItems() > getMaxNumberOfNewBatchItems()) {
            setNumberOfNewBatchItems(getMaxNumberOfNewBatchItems());
        }

        for (int i = 0; i < getNumberOfNewBatchItems(); i++) {
            final PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setPurchase(getPurchase());
            getEditList().add(purchaseItem);
            getCreatedList().add(purchaseItem);
        }

        setNumberOfNewBatchItems(1);
        updateMaxNumberOfNewLines();
    }

    public void deleteChanged(PurchaseItem purchaseItem) {
        getEditList().remove(purchaseItem);
        getCreatedList().remove(purchaseItem);
        if (purchaseItem.getId() > 0) {
            deleteList.add(purchaseItem);
        }

        updateMaxNumberOfNewLines();
    }

    public List<Consumable> getConsumablesFiltered(String filterString) {
        return consumableService.getConsumableFiltered(filterString, new HashSet<>());
    }

    private List<PurchaseItem> getCreatedList() {
        return createdList;
    }

    public List<PurchaseItem> getDeleteList() {
        return deleteList;
    }

    @Override
    public List<PurchaseItem> getEditList() {
        return editList;
    }

    public Purchase getPurchase() {
        return getInstance();
    }

    @Override
    @PostConstruct
    public void init() {
        if (id != null) {
            setInstance(loadInstance());

            // Initialize the editList.
            getEditList().clear();
            initializeEditList();
            updateMaxNumberOfNewLines();
        }
    }

    @Override
    public void initializeEditList() {
        if (getPurchase() != null && !getPurchase().getItems().isEmpty() && getPurchase().isUpdatable()) {
            setEditList(new ArrayList<>(getPurchase().getItems()));
        }
    }

    @CachedMethodResult
    public boolean isAttributeRequired(String columnName) {
        switch (columnName) {
        case Constants.CONSUMABLE:
            return true;
        case Constants.BASIC_PRICE:
        case Constants.QUANTITY:
        case Constants.TOTAL_PRICE:
        default:
            return false;
        }
    }

    @CachedMethodResult
    public boolean isRenderedColumn(String columnName) {
        switch (columnName) {
        case Constants.BASIC_PRICE:
        case Constants.CONSUMABLE:
        case Constants.QUANTITY:
        case Constants.TOTAL_PRICE:
            return true;
        case Constants.TOTAL_PRICE_DEFAULT_CURRENCY:
            return getPurchase() != null && !getPurchase().isInvoicedDefaultCurrency();
        default:
            return false;
        }
    }

    @Override
    public String save() {
        String redirectURL = super.save();
        if (redirectURL != null) {
            String message = purchaseItemService.savePurchaseItems(getPurchase(), getEditList(), getDeleteList());
            getFacesMessagesManager().bufferWarningClear(message);
            return redirectURL;
        }
        handleValidationErrorsForBatch(getValidationErrorMsg());
        return null;
    }

    public void setEditList(List<PurchaseItem> editList) {
        this.editList = editList;
    }

    private void setPropertyValue(PurchaseItem purchaseItem, String columnId, Object value) {
        switch (columnId) {
        case Constants.BASIC_PRICE:
        case Constants.CONSUMABLE:
        case Constants.QUANTITY:
        case Constants.TOTAL_PRICE:
        case Constants.TOTAL_PRICE_DEFAULT_CURRENCY:
            try {
                Object oldValue = PropertyUtils.getProperty(purchaseItem, columnId);
                PropertyUtils.setProperty(purchaseItem, columnId, value);
                if (oldValue == null || !oldValue.equals(value)) {
                    purchaseItem.setChanged(true);
                }

                if (columnId.equals(Constants.CONSUMABLE) && purchaseItem.getConsumable() != null && purchaseItem.getConsumable().getPrice() != null) {
                    purchaseItem.setBasicPrice(purchaseItem.getConsumable().getPrice());
                }

                switch (columnId) {
                case Constants.BASIC_PRICE:
                case Constants.CONSUMABLE:
                case Constants.QUANTITY:
                    if (purchaseItem.getBasicPrice() != null && purchaseItem.getQuantity() != null) {
                        purchaseItem.setTotalPrice(purchaseItem.getBasicPrice().multiply(purchaseItem.getQuantity()));
                    }
                    break;
                default:
                    break;
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void updateBatchTable(String columnId, int rowIndex) {
        if (columnId != null) {
            super.updateBatchTable(columnId, rowIndex);

            if (columnId.equals(Constants.BASIC_PRICE) || columnId.equals(Constants.CONSUMABLE) || columnId.equals(Constants.QUANTITY)) {
                if (rowIndex >= 0) {
                    updateCell(Constants.TOTAL_PRICE, rowIndex);
                } else {
                    updateColumn(Constants.TOTAL_PRICE);
                }

                if (columnId.equals(Constants.CONSUMABLE)) {
                    if (rowIndex > -1) {
                        updateCell(Constants.BASIC_PRICE, rowIndex);
                    } else {
                        updateColumn(Constants.BASIC_PRICE);
                    }
                }
            }
        }
    }

    @Override
    public void valueChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final Object value = event.getNewValue();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        final String columnId = clientId.contains("autocomplete") && source instanceof AutoComplete ? clientId.split(":")[3].replaceAll("autocomplete", Constants.EMPTY_STRING) : clientId.split(":")[3]
            .replaceAll(Constants.INPUT, Constants.EMPTY_STRING);
        final PurchaseItem changedPurchaseItem = getEditList().get(rowIndex);

        source.setValue(value);
        setPropertyValue(changedPurchaseItem, columnId, value);

        updateBatchTable(columnId, rowIndex);
        Ajax.update(Constants.EDIT + ":buttons");
    }

    @Override
    public void valueChangedAll(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        Object value = event.getNewValue();
        final String clientId = source.getClientId();
        boolean isAutoComplete = clientId.contains("autocomplete") && source instanceof AutoComplete;
        final String columnId = isAutoComplete ? clientId.split(":")[2].replaceAll("autocomplete", Constants.EMPTY_STRING).replaceAll(Constants.HEADER, Constants.EMPTY_STRING) : clientId.split(":")[2]
            .replaceAll(Constants.HEADER_INPUT, Constants.EMPTY_STRING);
        boolean setProperty = true;

        switch (columnId) {
        case Constants.CONSUMABLE:
            if (value == null) {
                setProperty = false;
            }
            break;
        case Constants.BASIC_PRICE:
        case Constants.QUANTITY:
        case Constants.TOTAL_PRICE:
        case Constants.TOTAL_PRICE_DEFAULT_CURRENCY:
            if (value == null) {
                setProperty = false;
            } else {
                value = new BigDecimal(value.toString());
            }
            break;
        default:
            break;
        }

        if (setProperty) {
            for (PurchaseItem purchaseItem : getEditList()) {
                setPropertyValue(purchaseItem, columnId, value);
            }
            updateBatchTable(columnId, -1);
            Ajax.update(Constants.EDIT + ":buttons");
            if (isAutoComplete) {
                String widgetVar = columnId + Constants.HEADER;
                PrimeFaces.current().executeScript("PF('" + widgetVar + "').clear();");
            }
        }
    }
}
