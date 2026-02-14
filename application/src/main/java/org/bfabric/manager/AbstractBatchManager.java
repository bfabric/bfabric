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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.transaction.SystemException;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

public abstract class AbstractBatchManager<T extends AbstractEntity> extends AbstractEntityManager<T> {

    private static final long serialVersionUID = 1;

    private static final String BATCH_TABLE_ID = "edit:batchtable";

    // LinkedHashMap<row index, LinkedHashMap<column id, display message>> used for caching all the validation error messages.
    private final LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg = new LinkedHashMap<>();

    // LinkedHashMap<column id, display message> used for caching all the validation error messages during the value change.
    private final LinkedHashMap<String, String> valueChangedValidationErrorMsg = new LinkedHashMap<>();

    @Inject
    protected ConfManager confManager;

    protected boolean cloneColumnRendered = false;

    private int cloneItemCount = 1;

    private AbstractEntity currentCloneItem;

    private Integer jumpToPage;

    private int maxNumberOfNewBatchItems;

    private int numberOfNewBatchItems = 1;

    public AbstractBatchManager() {
    }

    public AbstractBatchManager(Class<T> entityClass) {
        super(entityClass);
    }

    public void addClones() throws CloneNotSupportedException {
    }

    public void addNewBatchItems() {
    }

    public void addValidationErrorMessage(String message, int rowIndex, String componentId) {
        getFacesMessagesManager().validationError(getBatchTableId() + ":" + rowIndex + ":" + componentId, message);
    }

    public void addValidationErrorMessage(String componentId, String message, int rowIndex) {
        addValidationErrorMessage(getBatchTableId(), componentId, message, rowIndex);
    }

    public String cancelEdit() throws IllegalStateException, SecurityException, SystemException {
        getSessionManager().rollback();
        return getInstance() != null ? getShowScreenRedirectURL() : getUrlHomeScreen();
    }

    public String getBatchTableId() {
        return BATCH_TABLE_ID;
    }

    public int getCloneItemCount() {
        return cloneItemCount;
    }

    public AbstractEntity getCurrentCloneItem() {
        return currentCloneItem;
    }

    public String getEditItemMaximumNumberOfClonesHint() {
        return Messages.get("editItemMaximumNumberOfClones").replace("{0}", String.valueOf(getMaxNumberOfNewBatchItems()));
    }

    public abstract List<? extends AbstractEntity> getEditList();

    public String getItemLimitExceededHint() {
        return Messages.get("itemLimitExceeded").replace("{0}", Integer.toString(getMaxNumberOfBatchItems()));
    }

    public Integer getJumpToPage() {
        return jumpToPage;
    }

    public int getMaxNumberOfBatchItems() {
        return getConfiguration().getMaxBatchEditItems();
    }

    public int getMaxNumberOfNewBatchItems() {
        return maxNumberOfNewBatchItems;
    }

    public int getNumberOfNewBatchItems() {
        return numberOfNewBatchItems;
    }

    public String getSampleNameInputId(long index) {
        return getBatchTableId() + ":" + index + ":" + Constants.SAMPLE_NAME + Constants.INPUT;
    }

    public LinkedHashMap<Integer, LinkedHashMap<String, String>> getValidationErrorMsg() {
        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> getValueChangedValidationErrorMsg() {
        return valueChangedValidationErrorMsg;
    }

    public void handleValidationErrorsForBatch(LinkedHashMap<Integer, LinkedHashMap<String, String>> aValidationErrorMsg) {
        for (Map.Entry<Integer, LinkedHashMap<String, String>> entry : aValidationErrorMsg.entrySet()) {
            if (entry.getKey() >= 0) {
                handleValidationErrorsForRow(entry.getValue(), entry.getKey());
            } else {
                // Handle general validation errors not for the rows, e.g., custom attribute validation errors.
                for (Map.Entry<String, String> aEntry : getValidationErrorMsg().get(entry.getKey()).entrySet()) {
                    getFacesMessagesManager().validationError(aEntry.getKey(), aEntry.getValue());
                }
            }
        }
        FacesContext.getCurrentInstance().validationFailed();
    }

    public void handleValidationErrorsForRow(LinkedHashMap<String, String> aValidationErrorMsg, int rowIndex) {
        for (Map.Entry<String, String> entry : aValidationErrorMsg.entrySet()) {
            addValidationErrorMessage(entry.getKey() + Constants.INPUT, entry.getValue(), rowIndex);
        }
    }

    public void initCloneItemPanel() {
        cloneItemCount = 1;
        updateMaxNumberOfNewLines();
    }

    public abstract void initializeEditList();

    protected void initializeJumpToPage() {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(getBatchTableId());
        if (dataTable != null) {
            setJumpToPage(null);
            int hi = dataTable.getPage() * dataTable.getRowsToRender() + dataTable.getRowsToRender();
            if (getEditList().size() == dataTable.getPageCount() * dataTable.getRowsToRender()) {
                setJumpToPage(dataTable.getPageCount());
            } else if (getEditList().size() > hi) {
                setJumpToPage(dataTable.getPageCount() - 1);
            }
        }
    }

    protected void initializeJumpToPageAndAddItToCallbackParam() {
        initializeJumpToPage();
        PrimeFaces.current().ajax().addCallbackParam("jumpToPage", getJumpToPage());
        setJumpToPage(null);
    }

    public boolean isAddNewBatchItemsDisabled() {
        return getEditList() == null || getEditList().size() >= getMaxNumberOfBatchItems();
    }

    public boolean isCloneColumnRendered() {
        return cloneColumnRendered;
    }

    public LinkedHashMap<Integer, LinkedHashMap<String, String>> isValid() {
        return isValid(0, getEditList().size());
    }

    public LinkedHashMap<Integer, LinkedHashMap<String, String>> isValid(int lo, int hi) {
        return new LinkedHashMap<>();
    }

    public void pageListener() {
        if (!getValidationErrorMsg().isEmpty() && UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID) != null) {
            final DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
                .findComponent(String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID)));
            final int lo = dataTable.getPage() * dataTable.getRowsToRender();
            final int hi = Math.min(lo + dataTable.getRowsToRender(), getEditList().size());

            for (int i = lo; i < hi; i++) {
                if (getValidationErrorMsg().containsKey(i)) {
                    handleValidationErrorsForRow(getValidationErrorMsg().get(i), i);
                    for (String columnId : getValidationErrorMsg().get(i).keySet()) {
                        updateCell(columnId, i);
                    }
                }
            }
        }
        Ajax.update(getBatchTableId());
    }

    public void removeEmptyLines() {
    }

    public String save() {
        getValidationErrorMsg().clear();
        getValidationErrorMsg().putAll(isValid());
        return getValidationErrorMsg().isEmpty() ? getRedirectURLAfterSave() : null;
    }

    public void setCloneItemCount(int cloneItemCount) {
        this.cloneItemCount = cloneItemCount;
    }

    public void setCurrentCloneItem(AbstractEntity currentCloneItem) {
        this.currentCloneItem = currentCloneItem;
    }

    public void setJumpToPage(Integer jumpToPage) {
        this.jumpToPage = jumpToPage;
    }

    public void setMaxNumberOfNewBatchItems(int maxNumberOfNewBatchItems) {
        this.maxNumberOfNewBatchItems = maxNumberOfNewBatchItems;
    }

    public void setNumberOfNewBatchItems(int numberOfNewBatchItems) {
        this.numberOfNewBatchItems = numberOfNewBatchItems;
    }

    public void updateBatchTable(String columnId, int rowIndex) {
        if (rowIndex > -1) {
            updateCell(columnId, rowIndex);
        } else {
            updateColumn(columnId);
        }
    }

    public void updateCell(String tableId, String columnId, int rowIndex) {
        dataTableHelper.updateCell(tableId, columnId, rowIndex);
        Ajax.update(tableId + ":" + rowIndex + ":" + columnId + Constants.MESSAGE);
        Ajax.update(tableId + ":" + rowIndex + ":" + columnId + Constants.REQUIRED_FIELD);
    }

    public void updateCell(String columnId, int rowIndex) {
        updateCell(getBatchTableId(), columnId, rowIndex);
    }

    public void updateColumn(String columnId) {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(getBatchTableId());

        int lo = dataTable.getPage() * dataTable.getRowsToRender();
        int hi = lo + dataTable.getRowsToRender();

        for (int i = lo; i < hi; i++) {
            updateCell(columnId, i);
        }
    }

    public void updateMaxNumberOfNewLines() {
        if (getEditList() != null) {
            setMaxNumberOfNewBatchItems(getMaxNumberOfBatchItems() - getEditList().size());
        }
    }

    public abstract void valueChanged(ValueChangeEvent event);

    public abstract void valueChangedAll(ValueChangeEvent event);
}
