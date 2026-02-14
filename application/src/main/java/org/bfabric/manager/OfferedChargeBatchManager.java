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
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OfferedChargeService;
import org.bfabric.service.TaxTypeService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class OfferedChargeBatchManager extends AbstractBatchManager<Offer> {

    private static final long serialVersionUID = 1;

    private final List<OfferedCharge> createdList = new ArrayList<>();

    private final List<OfferedCharge> deleteList = new ArrayList<>();

    private List<OfferedCharge> editList = new ArrayList<>();

    @Param
    private Long id;

    @Inject
    private OfferedChargeService offeredChargeService;

    @Inject
    private TaxTypeService taxTypeService;

    public OfferedChargeBatchManager() {
        super(Offer.class);
    }

    @Override
    public void addNewBatchItems() {
        if (getNumberOfNewBatchItems() > getMaxNumberOfNewBatchItems()) {
            setNumberOfNewBatchItems(getMaxNumberOfNewBatchItems());
        }

        for (int i = 0; i < getNumberOfNewBatchItems(); i++) {
            final OfferedCharge offeredCharge = createNewOfferedCharge();
            getEditList().add(offeredCharge);
            getCreatedList().add(offeredCharge);
        }

        setNumberOfNewBatchItems(1);
        updateMaxNumberOfNewLines();
    }

    private OfferedCharge createNewOfferedCharge() {
        OfferedCharge offeredCharge = new OfferedCharge();
        offeredCharge.setOffer(getOffer());
        offeredCharge.setOrganizationType(offeredCharge.getOffer().getOrganizationType());
        offeredCharge.setTaxType(taxTypeService.getDefaultTaxType());
        offeredCharge.setTaxRate(taxTypeService.getDefaultTaxType().getTax());
        offeredCharge.setCharger(getCurrentUser());
        return offeredCharge;
    }

    public void deleteChanged(OfferedCharge offeredCharge) {
        getEditList().remove(offeredCharge);
        getCreatedList().remove(offeredCharge);
        if (offeredCharge.getId() > 0) {
            deleteList.add(offeredCharge);
        }

        updateMaxNumberOfNewLines();
    }

    private List<OfferedCharge> getCreatedList() {
        return createdList;
    }

    public List<OfferedCharge> getDeleteList() {
        return deleteList;
    }

    @Override
    public List<OfferedCharge> getEditList() {
        return editList;
    }

    public Offer getOffer() {
        return getInstance();
    }

    public double getTotalPrice(OfferedCharge offeredCharge) {
        double price = 0;

        OfferedCharge charge;
        try {
            charge = offeredCharge.clone();
            charge.setService(offeredCharge.getService());
            charge.setPrice();
            price = charge.getPrice().doubleValue();
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return price;
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
        if (getOffer() != null && !getOffer().getCharges().isEmpty() && getOffer().isUpdatable()) {
            setEditList(getOffer().getCharges());
        }
    }

    public boolean isAttributeRequired(String columnName) {
        switch (columnName) {
        case Constants.CHARGER:
        case Constants.SERVICE:
        case Constants.TAX_TYPE:
        case Constants.TOTAL:
            return true;
        default:
            return false;
        }
    }

    @CachedMethodResult
    public boolean isRenderedColumn(String columnName) {
        switch (columnName) {
        case Constants.BASIC_PRICE:
        case Constants.CHARGER:
        case Constants.DESCRIPTION:
        case Constants.NOTES:
        case Constants.SERVICE:
        case Constants.SERVICE_CODE:
        case Constants.TAX_TYPE:
        case Constants.TOTAL:
        case Constants.TOTAL_PRICE:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String save() {
        String redirectURL = super.save();
        if (redirectURL != null) {
            String message = offeredChargeService.saveOfferedCharges(getEditList(), getDeleteList());
            getFacesMessagesManager().bufferWarningClear(message);
            return redirectURL;
        }
        handleValidationErrorsForBatch(getValidationErrorMsg());
        return null;
    }

    public void setEditList(List<OfferedCharge> editList) {
        this.editList = editList;
    }

    private void setPropertyValue(OfferedCharge offeredCharge, String columnId, Object value) {
        switch (columnId) {
        case Constants.CHARGER:
        case Constants.DESCRIPTION:
        case Constants.NOTES:
        case Constants.SERVICE:
        case Constants.TAX_TYPE:
        case Constants.TOTAL:
            try {
                Object oldValue = PropertyUtils.getProperty(offeredCharge, columnId);
                PropertyUtils.setProperty(offeredCharge, columnId, value);
                if (oldValue == null || !oldValue.equals(value)) {
                    offeredCharge.setChanged(true);
                }

                if (columnId.equals(Constants.TAX_TYPE)) {
                    offeredCharge.setTaxRate(offeredCharge.getTaxType().getTax());
                }

                switch (columnId) {
                case Constants.TAX_TYPE:
                case Constants.SERVICE:
                case Constants.TOTAL:
                    offeredCharge.setPrice();
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

            if (columnId.equals(Constants.SERVICE) || columnId.equals(Constants.TAX_TYPE) || columnId.equals(Constants.TOTAL)) {
                if (rowIndex >= 0) {
                    updateCell(Constants.TOTAL_PRICE, rowIndex);
                } else {
                    updateColumn(Constants.TOTAL_PRICE);
                }

                if (columnId.equals(Constants.SERVICE)) {
                    if (rowIndex >= 0) {
                        updateCell(Constants.BASIC_PRICE, rowIndex);
                        updateCell(Constants.SERVICE_CODE, rowIndex);
                    } else {
                        updateColumn(Constants.BASIC_PRICE);
                        updateColumn(Constants.SERVICE_CODE);
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
        final String columnId = clientId.split(":")[3].replaceAll(Constants.INPUT, Constants.EMPTY_STRING);
        final OfferedCharge changedOfferedCharge = getEditList().get(rowIndex);

        if (!getValidationErrorMsg().containsKey(rowIndex)) {
            getValidationErrorMsg().put(rowIndex, new LinkedHashMap<>());
        }
        getValueChangedValidationErrorMsg().clear();

        if (columnId.equals(Constants.TOTAL)) {
            try {
                if (new BigDecimal(value.toString()).doubleValue() < 0) {
                    getValueChangedValidationErrorMsg().put(columnId, Messages.get("notPositive"));
                }
            } catch (NumberFormatException e) {
                getValueChangedValidationErrorMsg().put(columnId, Messages.get("notNumeric"));
            }
        }

        if (getValueChangedValidationErrorMsg().isEmpty()) {
            source.setValue(value);
            setPropertyValue(changedOfferedCharge, columnId, value);
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
        final UIInput source = (UIInput) event.getSource();
        Object value = event.getNewValue();
        final String clientId = source.getClientId();
        final String columnId = clientId.split(":")[2].replaceAll(Constants.HEADER_INPUT, Constants.EMPTY_STRING);
        boolean setProperty = true;

        switch (columnId) {
        case Constants.CHARGER:
        case Constants.DESCRIPTION:
        case Constants.NOTES:
        case Constants.SERVICE:
        case Constants.TAX_TYPE:
            if (value == null) {
                setProperty = false;
            }
            break;
        case Constants.TOTAL:
            try {
                BigDecimal total = new BigDecimal(value.toString());
                if (total.doubleValue() < 0) {
                    setProperty = false;
                } else {
                    value = total;
                }
            } catch (NumberFormatException e) {
                setProperty = false;
            }
            break;
        default:
            break;
        }

        if (setProperty) {
            for (OfferedCharge offeredCharge : getEditList()) {
                setPropertyValue(offeredCharge, columnId, value);
            }
            updateBatchTable(columnId, -1);
        }
    }
}
