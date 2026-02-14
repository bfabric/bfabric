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

package org.bfabric.service.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.event.ValueChangeEvent;
import javax.validation.constraints.NotBlank;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.entity.Sample;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.MatchMode;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class BfabricLazyDataModel<T> extends LazyDataModel<T> {

    private static final long serialVersionUID = 1;

    protected final HashMap<String, Object> defaultParameters = new HashMap<>();

    protected DatabaseQuery databaseQuery;

    protected List<T> dataList;

    protected List<String> columns;

    protected String defaultOrder;

    protected String defaultWhere;

    protected int lastPageSize;

    protected int size;

    protected String parentClassNameFilter;

    protected boolean parentClassNameFilterChanged;

    public BfabricLazyDataModel() {
        super();
    }

    public BfabricLazyDataModel(DatabaseQuery databaseQuery) {
        this();
        setDatabaseQuery(databaseQuery);
        size = (int) databaseQuery.getCount();
    }

    public BfabricLazyDataModel(DatabaseQuery databaseQuery, boolean setColumns) {
        this(databaseQuery);
        if (setColumns) {
            setColumns(databaseQuery.getColumns());
        }
    }

    private String[] buildDateRange(String value) {
        if (value != null) {
            String v = value.trim();
            try {
                // datetime with seconds, space: "2023-05-10 12:34:56" -> [dt, dt + 1s)
                if (v.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dt = LocalDateTime.parse(v, f);
                    return new String[] { dt.toString(), dt.plusSeconds(1).toString() };
                }

                // datetime with minutes, space: "2023-05-10 12:34" -> [dt, dt + 1min)
                if (v.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime dt = LocalDateTime.parse(v, f);
                    return new String[] { dt.toString(), dt.plusMinutes(1).toString() };
                }

                // datetime with hours, space: "2023-05-10 12" -> [dt, dt + 1hour)
                if (v.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}")) {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                    LocalDateTime dt = LocalDateTime.parse(v, f);
                    return new String[] { dt.toString(), dt.plusHours(1).toString() };
                }

                // date: "2023-05-10" -> [2023-05-10, 2023-05-11)
                if (v.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate d = LocalDate.parse(v);
                    return new String[] { d.toString(), d.plusDays(1).toString() };
                }
                // year-month: "2023-05" -> [2023-05-01, 2023-06-01)
                if (v.matches("\\d{4}-\\d{2}")) {
                    YearMonth ym = YearMonth.parse(v);
                    LocalDate start = ym.atDay(1);
                    LocalDate end = ym.plusMonths(1).atDay(1);
                    return new String[] { start.toString(), end.toString() };
                }
                // year: "2023" -> [2023-01-01, 2024-01-01)
                if (v.matches("\\d{4}")) {
                    int y = Integer.parseInt(v);
                    return new String[] { y + "-01-01", (y + 1) + "-01-01" };
                }
                // try full timestamp (ISO) -> exact equality (no range)
                try {
                    LocalDateTime dt = LocalDateTime.parse(v);
                    return new String[] { dt.toString(), null }; // null end -> equality
                } catch (DateTimeParseException ignored) {
                }
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        if (databaseQuery != null) {
            setDatabaseQueryWhereClause(filterBy);
            setRowCount((int) databaseQuery.getCount());
        }
        return getRowCount();
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public List<T> getFilteredValue() {
        databaseQuery.setMaxResult(size);
        List<T> ret = (List<T>) databaseQuery.getResultList();
        databaseQuery.setMaxResult(lastPageSize);
        return ret;
    }

    public long getFilteredValueCount() {
        databaseQuery.setMaxResult(size);
        long count = databaseQuery.getCount();
        databaseQuery.setMaxResult(lastPageSize);
        return count;
    }

    public StringBuilder getInitialDatabaseQueryWhereClause() {
        databaseQuery.clearParameter();
        if (!defaultParameters.isEmpty()) {
            databaseQuery.addParameters(defaultParameters);
        }
        StringBuilder whereClause = new StringBuilder();
        if (StringHelper.isNotEmpty(defaultWhere)) {
            whereClause.append("(").append(defaultWhere).append(")");
        }
        return whereClause;
    }

    private Map<String, FilterMeta> getModifiedFilterBy(Map<String, FilterMeta> filterBy) {
        Map<String, FilterMeta> modifiedFilterBy = new HashMap<>();
        if (filterBy != null && !filterBy.isEmpty()) {
            for (Entry<String, FilterMeta> filterMetaEntry : filterBy.entrySet()) {
                FilterMeta filterMeta = filterMetaEntry.getValue();
                String field = filterMeta.getField();
                SampleAttributeEnum sampleAttributeEnum = SampleAttributeEnum.getAttributeByName(field);
                String filterValue;
                if (sampleAttributeEnum != null && filterMeta.getFilterValue() != null && sampleAttributeEnum.isEnumType()) {
                    filterValue = filterMeta.getFilterValue().toString().toLowerCase();
                } else {
                    filterValue = (String) filterMeta.getFilterValue();
                }
                if (filterValue != null) {
                    filterValue = filterValue.trim();
                }
                if (sampleAttributeEnum != null && (sampleAttributeEnum.isAnnotationTypeSingleValued() || sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType())) {
                    // Filter by annotation, sample preparation protocol, multiplex kit, or instrument name.
                    field = field + ".name";
                }
                modifiedFilterBy.put(field, FilterMeta.builder().field(field).filterValue(filterValue).matchMode(filterMeta.getMatchMode()).build());
            }
        }
        return modifiedFilterBy;
    }

    @SuppressWarnings("unused")
    public String getParentClassNameFilter() {
        return parentClassNameFilter;
    }

    @Override
    public T getRowData(String rowKey) {
        for (T t : dataList) {
            String id = getRowKey(t);
            if (rowKey != null && rowKey.equals(id)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public String getRowKey(T t) {
        try {
            return String.valueOf(PropertyUtils.getProperty(t, "id"));
        } catch (Exception e) {
            return null;
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        // long startQueryTime = System.currentTimeMillis();
        setDatabaseQueryWhereClause(filterBy);
        setDatabaseQuerySortBy(sortBy);
        // paginate
        lastPageSize = pageSize;
        databaseQuery.setFirstResult(first);
        databaseQuery.setMaxResult(pageSize);
        dataList = (List<T>) databaseQuery.getResultList();
        //System.out.println("Lazy Load QueryTime: " + (System.currentTimeMillis() - startQueryTime));
        return dataList;
    }

    @SuppressWarnings("unused")
    public void parentClassNameFilterChanged(ValueChangeEvent event) {
        this.parentClassNameFilterChanged = true;
    }

    public String replaceSize(@NotBlank String clause) {
        return clause.contains(".size()") ? "size(" + clause.replace(".size()", "") + ")" : clause;
    }

    public String setAlias() {
        return StringHelper.isNotEmpty(databaseQuery.getNativeQueryString()) ? "" : "entity.";
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public void setDatabaseQuery(DatabaseQuery databaseQuery) {
        this.databaseQuery = databaseQuery;
        if (databaseQuery != null) {
            defaultWhere = databaseQuery.getWhere();
            defaultOrder = databaseQuery.getOrder();
            defaultParameters.clear();
            defaultParameters.putAll(databaseQuery.getParameters());
        }
    }

    public void setDatabaseQuerySortBy(Map<String, SortMeta> sortBy) {
        String orderClause = defaultOrder;
        if (sortBy != null && !sortBy.isEmpty()) {
            StringBuilder orderClauseBuilder = new StringBuilder();
            for (Entry<String, SortMeta> sortMeta : sortBy.entrySet()) {
                if (orderClauseBuilder.length() > 0) {
                    orderClauseBuilder.append(",");
                }
                orderClauseBuilder.append(replaceSize(setAlias() + sortMeta.getValue().getField()));
                if (sortMeta.getValue().getOrder() == SortOrder.DESCENDING) {
                    orderClauseBuilder.append(" DESC");
                }
            }
            orderClause = orderClauseBuilder.toString();
        }
        databaseQuery.setOrder(orderClause);
    }

    public void setDatabaseQueryWhereClause(Map<String, FilterMeta> filterBy) {
        StringBuilder whereClause = getInitialDatabaseQueryWhereClause();
        if (filterBy != null && !filterBy.isEmpty()) {
            if (databaseQuery.getFrom().equals(Sample.class.getSimpleName())) {
                filterBy = getModifiedFilterBy(filterBy);
            }
            for (Entry<String, FilterMeta> filterMeta : filterBy.entrySet()) {
                String filterProperty = replaceSize(setAlias() + filterMeta.getValue().getField());
                String filterValue;
                try {
                    filterValue = (String) filterMeta.getValue().getFilterValue();
                } catch (Exception e) {
                    filterValue = filterMeta.getValue().getFilterValue().toString();
                }
                if (filterValue != null) {
                    filterValue = filterValue.trim();
                }
                if (StringHelper.isNotEmpty(filterValue) && !filterValue.equals("!") && !filterValue.equals("=")) {
                    if (whereClause.length() > 0) {
                        whereClause.append(" AND ");
                    }
                    /*
                          The following filterMatchMode are built-in for non-lazy tables:
                          exact : Checks if string representations of column value and filter value are same.
                          contains : Checks if column value contains the filter value.
                          startsWith : Checks if column value starts with the filter value.
                          endsWith : Checks if column value ends with the filter value.
                          equals : Checks if column value equals the filter value.
                          lt : Checks if column value is less than the filter value.
                          lte : Checks if column value is less than or equals the filter value.
                          gt : Checks if column value is greater than the filter value.
                          gte : Checks if column value is greater than or equals the filter value.
                          in : Checks if column value is in the collection of the filter value.
                          range: Checks if column value is in the filter range.
                          For lazy tables, following extensions are supported:
                          1) specific filterValue: =null, !null, !=null, =blank, !blank
                          2) negation: !filterValue
                          3) match modes: exact, equals, startsWith, endsWith, contains
                          TODO: implement the other match modes, too!
                    */
                    String fieldName = filterMeta.getValue().getField();
                    if (ClassHelper.getDateColumns().contains(fieldName)) {
                        // try to produce a range-based predicate for date fields
                        String[] range = buildDateRange(filterValue);
                        if (range != null) {
                            if (range[1] != null) { // a start/end range
                                whereClause.append("(").append(filterProperty).append(" >= '").append(range[0]).append("' AND ").append(filterProperty).append(" < '").append(range[1]).append("')");
                            } else { // exact timestamp equality
                                whereClause.append(filterProperty).append(" = '").append(range[0]).append("'");
                            }
                            // skip the default text LIKE building
                            continue;
                        }
                        // if no range could be derived, fall back to the regular behavior below
                    }
                    String filterValueLowerCase = filterValue.toLowerCase();
                    switch (filterValueLowerCase) {
                    case "=null":
                        whereClause.append(filterProperty).append(" IS NULL");
                        break;
                    case "!null":
                        whereClause.append(filterProperty).append(" IS NOT NULL");
                        break;
                    case "=blank":
                        whereClause.append(filterProperty).append(" = ''");
                        break;
                    case "!blank":
                        whereClause.append(filterProperty).append(" <> ''");
                        break;
                    default:
                        if (filterValue.startsWith("!")) {
                            whereClause.append("NOT ");
                            filterValue = filterValue.substring(1);
                            filterValueLowerCase = filterValueLowerCase.substring(1);
                        }
                        MatchMode filterMatchMode = filterMeta.getValue().getMatchMode();
                        String filterVariable = "LOWER(CAST(" + filterProperty + " AS text))";
                        String filterOperator = " LIKE ";
                        switch (filterMatchMode) {
                        case EXACT:
                            filterVariable = filterProperty;
                            filterOperator = " = ";
                            break;
                        case EQUALS:
                            filterOperator = " = ";
                            filterValue = filterValueLowerCase;
                            break;
                        case STARTS_WITH:
                            filterValue = filterValueLowerCase + "%";
                            break;
                        case ENDS_WITH:
                            filterValue = "%" + filterValueLowerCase;
                            break;
                        default: // MatchMode.CONTAINS
                            filterValue = "%" + filterValueLowerCase + "%";
                            break;
                        }
                        whereClause.append(filterVariable).append(filterOperator).append("'").append(filterValue).append("'");
                        break;
                    }
                }
            }
        }
        databaseQuery.setWhere(whereClause.toString());
    }

    @SuppressWarnings("unused")
    public void setParentClassNameFilter(String parentClassNameFilter) {
        this.parentClassNameFilter = parentClassNameFilter;
    }

    public int size() {
        return getSize();
    }
}