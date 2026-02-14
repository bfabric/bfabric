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

package org.bfabric.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.manager.IdentityManager;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

@MeasureCalls
@Named
@ViewScoped
public class DataTableHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private final Map<String, Set<AbstractEntity>> tableIdMarkedEntities = new HashMap<>();

    private final Map<String, Map<AbstractEntity, Boolean>> tableIdMarkedEntitiesMap = new HashMap<>();

    private final Map<String, Set<AbstractEntity>> tableIdSelectedEntities = new HashMap<>();

    private final Set<String> initializedTabledIds = new HashSet<>();

    private final Set<AbstractEntity> selectedEntities = new HashSet<>();

    private final Map<String, String> tableIdRowsPerPageTemplate = new HashMap<>();

    @Param
    protected String filterValueCreated;

    @Param
    protected String filterValueModified;

    Integer dataTableExportLimit;

    @Inject
    private IdentityManager identityManager;

    private Map<AbstractEntity, Boolean> selectedEntitiesMap = new HashMap<>();

    public DataTableHelper() {
    }

    public void addAllEntitiesFromDataTableToSelection(Set<AbstractEntity> selection) {
        String dataTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID);
        if (StringHelper.isNotEmpty(dataTableClientId)) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataTableClientId);
            if (dataTable != null) {
                if (selection.isEmpty()) {
                    PrimeFaces.current().ajax().addCallbackParam("triggerUpdateTable", Boolean.TRUE);
                }
                List<AbstractEntity> entities = getDataTableValues(dataTable);
                selection.addAll(entities);
                updateAddButtonColumn(dataTableClientId);
                if (getInitializedTabledIds().contains(dataTableClientId) && getTableIdSelectedEntities().containsKey(dataTableClientId)) {
                    getTableIdSelectedEntities().get(dataTableClientId).addAll(entities);
                    clearTableIdMarkedEntitiesValues();
                    clearTableIdMarkedEntitiesValuesMap();
                    updateCheckboxColumn(dataTableClientId);
                }
            }
        }
    }

    public void addEntityToSelection(Set<AbstractEntity> selection, AbstractEntity entity) {
        if (selection != null && entity != null) {
            selection.add(entity);
            String dataTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID);
            if (StringHelper.isNotEmpty(dataTableClientId)) {
                updateAddButtonColumn(dataTableClientId);
                if (getInitializedTabledIds().contains(dataTableClientId) && getTableIdSelectedEntities().containsKey(dataTableClientId)) {
                    getTableIdSelectedEntities().get(dataTableClientId).add(entity);
                    clearTableIdMarkedEntitiesValues();
                    clearTableIdMarkedEntitiesValuesMap();
                    updateCheckboxColumn(dataTableClientId);
                }
            }
        }
    }

    public Set<AbstractEntity> assignMarkedEntitiesToSelection(String tableId, String targetTableId, Set<AbstractEntity> selection) {
        if (StringHelper.isNotEmpty(tableId) && getTableIdMarkedEntities().containsKey(tableId) && !getTableIdMarkedEntities().get(tableId).isEmpty() && selection != null) {
            Set<AbstractEntity> assignedEntities = new HashSet<>();
            if (!getTableIdSelectedEntities().containsKey(tableId)) {
                getTableIdSelectedEntities().put(tableId, new HashSet<>());
            }
            for (AbstractEntity entity : getTableIdMarkedEntities().get(tableId)) {
                getTableIdSelectedEntities().get(tableId).add(entity);
                assignedEntities.add(entity);
            }
            clearTableIdMarkedEntitiesValues();
            clearTableIdMarkedEntitiesValuesMap();
            updateAddButtonColumn(tableId);
            updateCheckboxColumn(tableId);

            if (StringHelper.isNotEmpty(targetTableId)) {
                DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(targetTableId);
                if (dataTable != null && selection.isEmpty()) {
                    Integer defaultListingRows = identityManager.getCurrentUser().getDefaultListingRows();
                    List<Integer> rowsPerPageTemplateList = Stream.of(dataTable.getRowsPerPageTemplate().split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
                    dataTable
                        .setRows(defaultListingRows != null && !rowsPerPageTemplateList.isEmpty() && rowsPerPageTemplateList.contains(defaultListingRows) ? defaultListingRows : rowsPerPageTemplateList
                            .get(rowsPerPageTemplateList.size() - 1));
                }
            }

            selection.addAll(assignedEntities);
            return assignedEntities;
        }
        return null;
    }

    public void checkAll(boolean check) {
        List<AbstractEntity> entities = getDataTableValues();
        for (AbstractEntity entity : entities) {
            entity.setChecked(check);
        }
    }

    public void clear(String tableId) {
        clearTableIdRowsPerPageTemplate();
        clearTableIdMarkedEntitiesValuesMap();
        clearTableIdSelectedEntitiesValues();
        clearTableIdMarkedEntitiesValuesMap();
        if (tableId != null) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableId);
            if (dataTable != null) {
                dataTable.resetRows();
            }
        }
    }

    public void clearTableIdMarkedEntitiesValues() {
        for (Set<AbstractEntity> entities : getTableIdMarkedEntities().values()) {
            entities.clear();
        }
    }

    public void clearTableIdMarkedEntitiesValuesMap() {
        for (Map<AbstractEntity, Boolean> entities : getTableIdMarkedEntitiesMap().values()) {
            entities.clear();
        }
    }

    public void clearTableIdRowsPerPageTemplate() {
        tableIdRowsPerPageTemplate.clear();
    }

    public void clearTableIdSelectedEntitiesValues() {
        for (Set<AbstractEntity> entities : getTableIdSelectedEntities().values()) {
            entities.clear();
        }
    }

    public DataTable getDataTableByTableClientId() {
        String dataTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID);
        if (StringHelper.isNotEmpty(dataTableClientId)) {
            return (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataTableClientId);
        }
        return null;
    }

    public int getDataTableExportLimit() {
        if (dataTableExportLimit == null) {
            dataTableExportLimit = ConfigurationHelper.getConfiguration().getDataTableExportLimit();
        }
        return dataTableExportLimit;
    }

    public <T extends AbstractEntity> List<T> getDataTableValues(DataTable dataTable) {
        return getDataTableValues(dataTable, false);
    }

    public <T> List<T> getDataTableValues(DataTable dataTable, boolean ignoreFilter) {
        if (dataTable.isLazy()) {
            return ((BfabricLazyDataModel<T>) dataTable.getValue()).getDataList();
        }
        if (!ignoreFilter && dataTable.getFilteredValue() != null) {
            return (List<T>) dataTable.getFilteredValue();
        }
        return (List<T>) dataTable.getValue();
    }

    public <T extends AbstractEntity> List<T> getDataTableValues() {
        DataTable dataTable = getDataTableByTableClientId();
        return dataTable != null ? getDataTableValues(dataTable) : new ArrayList<>();
    }

    public <T extends AbstractEntity> List<T> getDataTableValuesPageOnly(DataTable dataTable) {
        List<T> dataList = getDataTableValues(dataTable);
        if (dataTable.isLazy()) {
            return dataList;
        }
        List<T> dataTableValuesPageOnly = new ArrayList<>();
        int lo = dataTable.getPage() * dataTable.getRowsToRender();
        int hi = Math.min(lo + dataTable.getRowsToRender(), dataList.size());
        for (int i = lo; i < hi; i++) {
            dataTableValuesPageOnly.add(dataList.get(i));
        }
        return dataTableValuesPageOnly;
    }

    @Produces
    @Named("filterValueCreated")
    public String getFilterValueCreated() {
        return filterValueCreated;
    }

    @Produces
    @Named("filterValueModified")
    public String getFilterValueModified() {
        return filterValueModified;
    }

    public int getFilteredDataTableSize(DataTable dataTable, Iterable<?> tableContent) {
        int dataTableSize = 0;
        if (dataTable != null) {
            if (dataTable.isLazy()) {
                if (tableContent != null) {
                    dataTableSize = ((BfabricLazyDataModel<?>) tableContent).getRowCount();
                }
            } else {
                if (dataTable.getFilteredValue() != null) {
                    dataTableSize = dataTable.getFilteredValue().size();
                } else {
                    dataTableSize = ((Collection<?>) dataTable.getValue()).size();
                }
            }
        } else if (tableContent != null) {
            if (tableContent instanceof BfabricLazyDataModel<?>) {
                dataTableSize = ((BfabricLazyDataModel<?>) tableContent).getRowCount();
            } else {
                dataTableSize = ((Collection<?>) tableContent).size();
            }
        }
        return dataTableSize;
    }

    public Set<String> getInitializedTabledIds() {
        return initializedTabledIds;
    }

    @CachedMethodResult
    public int getListingRowsCached() {
        return identityManager.getSessionManager().getListingRows();
    }

    public int getRows(Boolean paginator, Integer customListingRows, Integer maximumRowsPerPage, int tableContentSize) {
        int rows = getRowsComputed(paginator, customListingRows, maximumRowsPerPage, tableContentSize);
        return rows != 0 ? rows : identityManager.getCurrentUser().getListingRows();
    }

    public int getRowsComputed(Boolean paginator, Integer customListingRows, Integer maximumRowsPerPage, int tableContentSize) {
        if (paginator == null || paginator) {
            if (customListingRows != null) {
                return maximumRowsPerPage != null ? Math.min(maximumRowsPerPage, customListingRows) : customListingRows;
            }
            return maximumRowsPerPage != null ? Math.min(maximumRowsPerPage, getListingRowsCached()) : Math.min(tableContentSize, getListingRowsCached());
        }
        return tableContentSize;
    }

    public String getRowsPerPageTemplate(String tableId, Integer size, Boolean all) {
        if (!tableIdRowsPerPageTemplate.containsKey(tableId)) {
            List<Integer> defaultRowsPerPageTemplateList = ConfigurationHelper.getConfiguration().getDefaultRowsPerPageTemplateList();
            int tableContentSize = size != null ? size : defaultRowsPerPageTemplateList.get(0);
            List<Integer> rowsPerPageTemplateList = new ArrayList<>();
            for (Integer rows : defaultRowsPerPageTemplateList) {
                if (rows < tableContentSize) {
                    rowsPerPageTemplateList.add(rows);
                } else {
                    break;
                }
            }
            if (all != null && all || tableContentSize < defaultRowsPerPageTemplateList.get(defaultRowsPerPageTemplateList.size() - 1)) {
                rowsPerPageTemplateList.add(tableContentSize);
            }
            tableIdRowsPerPageTemplate.put(tableId, rowsPerPageTemplateList.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        return tableIdRowsPerPageTemplate.get(tableId);
    }

    public Set<AbstractEntity> getSelectedEntities() {
        return selectedEntities;
    }

    public Map<AbstractEntity, Boolean> getSelectedEntitiesMap() {
        return selectedEntitiesMap;
    }

    public Map<String, Set<AbstractEntity>> getTableIdMarkedEntities() {
        return tableIdMarkedEntities;
    }

    public Map<String, Map<AbstractEntity, Boolean>> getTableIdMarkedEntitiesMap() {
        return tableIdMarkedEntitiesMap;
    }

    public Map<String, Set<AbstractEntity>> getTableIdSelectedEntities() {
        return tableIdSelectedEntities;
    }

    @CachedMethodResult
    public boolean hasColumnId(Object entity) {
        return entity instanceof AbstractEntity;
    }

    public void initializeSelectCheckboxColumn(Collection<String> tableIds) {
        if (tableIds != null) {
            for (String tableId : tableIds) {
                if (StringHelper.isNotEmpty(tableId)) {
                    getInitializedTabledIds().add(tableId);
                    if (!getTableIdMarkedEntities().containsKey(tableId)) {
                        getTableIdMarkedEntities().put(tableId, new HashSet<>());
                    }
                    if (!getTableIdMarkedEntitiesMap().containsKey(tableId)) {
                        getTableIdMarkedEntitiesMap().put(tableId, new HashMap<>());
                    }
                    if (!getTableIdSelectedEntities().containsKey(tableId)) {
                        getTableIdSelectedEntities().put(tableId, new HashSet<>());
                    }
                }
            }
        }
    }

    @CachedMethodResult
    public boolean isExportable(DataTable dataTable, Iterable<?> tableContent) {
        return getFilteredDataTableSize(dataTable, tableContent) <= getDataTableExportLimit();
    }

    public boolean isRowsPerPageTemplateShown(Boolean paginator, Integer customListingRows, Integer maximumRowsPerPage, int tableContentSize) {
        return getRowsComputed(paginator, customListingRows, maximumRowsPerPage, tableContentSize) != 0;
    }

    public void markEntities() {
        String dataTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID);
        String tableIsSelect = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_IS_SELECT);
        if (StringHelper.isNotEmpty(dataTableClientId)) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataTableClientId);
            if (dataTable != null && (Constants.TRUE.equalsIgnoreCase(tableIsSelect) || Constants.FALSE.equalsIgnoreCase(tableIsSelect))) {
                for (AbstractEntity entity : getDataTableValues(dataTable)) {
                    markEntity(entity, Boolean.parseBoolean(tableIsSelect), dataTableClientId);
                }
                updateCheckboxColumn(dataTableClientId);
            }
        }
    }

    public void markEntity(AbstractEntity entity, boolean isSelect, String tableId) {
        if (entity != null && tableId != null) {
            if (isSelect) {
                if (!getTableIdMarkedEntities().containsKey(tableId)) {
                    getTableIdMarkedEntities().put(tableId, new HashSet<>());
                }
                getTableIdMarkedEntities().get(tableId).add(entity);
                if (!getTableIdMarkedEntitiesMap().containsKey(tableId)) {
                    getTableIdMarkedEntitiesMap().put(tableId, new HashMap<>());
                }
                getTableIdMarkedEntitiesMap().get(tableId).put(entity, Boolean.TRUE);
            } else {
                if (getTableIdMarkedEntities().containsKey(tableId)) {
                    getTableIdMarkedEntities().get(tableId).remove(entity);
                }
                if (getTableIdMarkedEntitiesMap().containsKey(tableId)) {
                    getTableIdMarkedEntitiesMap().get(tableId).remove(entity);
                }
            }
        }
    }

    public void removeAllEntitiesFromSelection(Set<AbstractEntity> selection) {
        String dataTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID);
        if (StringHelper.isNotEmpty(dataTableClientId)) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataTableClientId);
            if (dataTable != null) {
                List<AbstractEntity> entities = getDataTableValues(dataTable);
                selection.removeAll(entities);
                dataTable.reset();
                String sourceTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.SOURCE_TABLE_CLIENT_ID);
                if (StringHelper.isNotEmpty(sourceTableClientId)) {
                    updateAddButtonColumn(sourceTableClientId);
                    if (getInitializedTabledIds().contains(sourceTableClientId) && getTableIdSelectedEntities().containsKey(sourceTableClientId)) {
                        getTableIdSelectedEntities().get(sourceTableClientId).removeAll(entities);
                        clearTableIdMarkedEntitiesValues();
                        clearTableIdMarkedEntitiesValuesMap();
                        updateCheckboxColumn(sourceTableClientId);
                    }
                }
            }
        }
    }

    public void removeEntityFromSelection(Set<AbstractEntity> selection, AbstractEntity entity) {
        String dataTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID);
        if (StringHelper.isNotEmpty(dataTableClientId)) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataTableClientId);
            if (dataTable != null) {
                selection.remove(entity);
                if (getDataTableValues(dataTable).size() == 1) {
                    dataTable.reset();
                }
                updateAddButtonColumn(dataTableClientId);
                String sourceTableClientId = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.SOURCE_TABLE_CLIENT_ID);
                if (StringHelper.isNotEmpty(sourceTableClientId)) {
                    updateAddButtonColumn(sourceTableClientId);
                    if (getInitializedTabledIds().contains(sourceTableClientId) && getTableIdSelectedEntities().containsKey(sourceTableClientId)) {
                        getTableIdSelectedEntities().get(sourceTableClientId).remove(entity);
                        clearTableIdMarkedEntitiesValues();
                        clearTableIdMarkedEntitiesValuesMap();
                        updateCheckboxColumn(sourceTableClientId);
                    }
                }
            }
        }
    }

    public void selectAll(boolean selected) {
        List<AbstractEntity> entities = getDataTableValues();
        for (AbstractEntity entity : entities) {
            selectEntity(entity, selected);
        }
    }

    public void selectEntity(AbstractEntity entity, boolean selected) {
        if (selected) {
            getSelectedEntities().add(entity);
            getSelectedEntitiesMap().put(entity, true);
        } else {
            getSelectedEntities().remove(entity);
            getSelectedEntitiesMap().remove(entity);
        }
    }

    public void selectEntity(AbstractEntity entity) {
        if (getSelectedEntities().contains(entity)) {
            getSelectedEntities().remove(entity);
        } else {
            getSelectedEntities().add(entity);
        }
    }

    public void setFilterValueCreated(String filterValueCreated) {
        this.filterValueCreated = filterValueCreated;
    }

    public void setFilterValueModified(String filterValueModified) {
        this.filterValueModified = filterValueModified;
    }

    public void setSelectedEntitiesMap(Map<AbstractEntity, Boolean> selectedEntitiesMap) {
        this.selectedEntitiesMap = selectedEntitiesMap;
    }

    public Set<AbstractEntity> unAssignMarkedEntitiesFromSelection(String tableId, String sourceTableId, Set<AbstractEntity> selection) {
        if (StringHelper.isNotEmpty(tableId) && getTableIdMarkedEntities().containsKey(tableId) && !getTableIdMarkedEntities().get(tableId).isEmpty() && getTableIdSelectedEntities()
            .containsKey(sourceTableId) && selection != null) {
            Set<AbstractEntity> unassignedEntities = new HashSet<>(getTableIdMarkedEntities().get(tableId));
            getTableIdSelectedEntities().get(sourceTableId).removeAll(unassignedEntities);
            clearTableIdMarkedEntitiesValues();
            clearTableIdMarkedEntitiesValuesMap();
            if (StringHelper.isNotEmpty(sourceTableId)) {
                updateAddButtonColumn(sourceTableId);
                updateCheckboxColumn(sourceTableId);
            }
            selection.removeAll(unassignedEntities);
            return unassignedEntities;
        }
        return null;
    }

    public void updateAddButtonColumn(String tableId) {
        updateColumn(tableId, Constants.ADD + Constants.BUTTON, false);
    }

    public void updateCell(String tableId, String columnId, int rowIndex) {
        updateCell(tableId, columnId, rowIndex, true);
    }

    public void updateCell(String tableId, String columnId, int rowIndex, boolean isInputCell) {
        Ajax.update(tableId + ":" + rowIndex + ":" + columnId + (isInputCell ? Constants.INPUT : Constants.EMPTY_STRING));
    }

    public void updateCellAndMessage(String tableId, String columnId, int rowIndex) {
        updateCell(tableId, columnId, rowIndex);
        Ajax.update(tableId + ":" + rowIndex + ":" + columnId + Constants.MESSAGE);
    }

    public void updateCheckboxColumn(String tableId) {
        updateColumn(tableId, Constants.SELECT_CHECK_BOX, true);
    }

    public void updateColumn(String tableId, String columnId) {
        updateColumn(tableId, columnId, true);
    }

    public void updateColumn(String tableId, String columnId, boolean isInputCell) {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableId);
        if (dataTable != null) {
            int lo = dataTable.getPage() * dataTable.getRowsToRender();
            int hi = lo + dataTable.getRowsToRender();
            for (int i = lo; i < hi; i++) {
                updateCell(tableId, columnId, i, isInputCell);
            }
        }
    }
}