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

package org.bfabric.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.DatasetService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Entity
@XmlRootElement
public class DatasetItem extends AbstractBaseEntity implements NotEntityLoggable {

    public static final String DELIMITER = " | ";

    private static final long serialVersionUID = 1;

    @Transient
    private Map<DatasetAttribute, DatasetField> attributeFieldMap;

    @ManyToOne
    @JoinColumn(name = "datasetid")
    @NotNull
    @XmlIDREF
    private Dataset dataset;

    @OneToMany(mappedBy = "item", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("id")
    @XmlIDREF
    @XmlElement(name = "field")
    private List<DatasetField> fields = new ArrayList<>();

    @Transient
    private List<DatasetField> fieldsOrderByPosition;

    @NotNull
    @XmlElement
    private int position = 0;

    @Transient
    private List<Resource> resources = new ArrayList<>();

    public DatasetItem() {
    }

    public DatasetItem(Dataset dataset) {
        this.dataset = dataset;
        if (dataset != null) {
            this.position = dataset.getNextItemPosition();
        } else {
            this.position = 0;
        }
    }

    public DatasetItem(Dataset dataset, int position) {
        this.dataset = dataset;
        this.position = position;
    }

    public DatasetItem(List<DatasetField> fieldsList) {
        super();
        setFields(fieldsList);
    }

    public DatasetItem clone(Dataset cloneDataset, Map<DatasetAttribute, DatasetAttribute> attributeMap) {
        List<DatasetField> fieldsList = new ArrayList<>();
        DatasetItem clone = new DatasetItem(fieldsList);

        // Clone position.
        clone.setPosition(getPosition());

        // Link item with the dataset.
        clone.setDataset(cloneDataset);

        // Clone fields.
        for (DatasetField field : getFieldsOrderByPosition()) {
            clone.getFields().add(field.clone(clone, attributeMap));
        }

        return clone;
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            // Important: use trimmed class name because of hibernate proxy issues.
            String objectClassName = ClassHelper.getTrimmedClassName(object.getClass().getName());
            if (objectClassName != null && objectClassName.equals(getClass().getName())) {
                // check class cast
                DatasetItem baseEntity = (DatasetItem) object;
                if (getDataset() != null && baseEntity.getDataset() != null) {
                    if (getDataset().getId() == baseEntity.getDataset().getId()) {
                        // check relative position within the same dataset
                        return Integer.compare(getPosition(), baseEntity.getPosition());
                    } else if (getDataset().getId() < baseEntity.getDataset().getId()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                // default in case at least one dataset is null
                return 1;
            }
            throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + object.getClass().getName());
        }
        throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + Constants.NULL);
    }

    public Map<DatasetAttribute, DatasetField> getAttributeFieldMap() {
        if (attributeFieldMap == null) {
            attributeFieldMap = new HashMap<>();
            for (DatasetField field : getFields()) {
                attributeFieldMap.put(field.getAttribute(), field);
            }
        }
        return attributeFieldMap;
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "position", getPosition());
        return summary.toString();
    }

    public DatasetField getField(DatasetAttribute attribute) {
        for (DatasetField field : getFields()) {
            if (field.getAttribute().equals(attribute)) {
                return field;
            }
        }
        return null;
    }

    public List<DatasetField> getFields() {
        return fields;
    }

    public String getFieldsForXmlExport() {
        return getFieldsOrderByPosition().stream().map(DatasetField::getValue).collect(Collectors.joining(DELIMITER));
    }

    public List<DatasetField> getFieldsOrderByPosition() {
        if (fieldsOrderByPosition == null) {
            fieldsOrderByPosition = CDI.current().select(DatasetService.class).get().getFieldsByItemIdOrderByPosition(getId());
        }
        return fieldsOrderByPosition;
    }

    private DatasetItem getNextItem() {
        DatasetItem nextItem = null;
        int listPosition = getDataset().getItems().indexOf(this) + 1;
        if (listPosition < getDataset().getItems().size()) {
            nextItem = getDataset().getItems().get(listPosition);
        }
        return nextItem;
    }

    public int getPosition() {
        return position;
    }

    public List<Resource> getResources() {
        return resources;
    }

    @SuppressWarnings("unused")
    public Object getSortValueByAttribute(DatasetAttribute attribute) {
        DatasetField datasetField = getAttributeFieldMap().get(attribute);
        if (datasetField == null) {
            return null;
        }
        try {
            if (attribute.getType() != null && attribute.getType().equalsIgnoreCase("Numeric")) {
                return Double.valueOf(datasetField.getValue());
            } else if (attribute.getType() != null && (attribute.getType().equalsIgnoreCase("Integer") || ClassHelper.getEntityClassNamesLowerCase().contains(attribute.getType().toLowerCase()))) {
                return Long.valueOf(datasetField.getValue());
            }
        } catch (Exception ignored) {
        }
        return datasetField.getValue();
    }

    public String getValue(DatasetAttribute attribute) {
        DatasetField field = getField(attribute);
        if (field != null) {
            return field.getValue();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public String getValueByAttribute(DatasetAttribute attribute) {
        DatasetField datasetField = getAttributeFieldMap().get(attribute);
        return datasetField != null ? datasetField.getValue() : null;
    }

    public boolean isEmpty() {
        return !getFields().stream().anyMatch(field -> StringHelper.isNotEmpty(field.getValue()));
    }

    public boolean isMovable() {
        return getPosition() != getDataset().getLastItemPosition();
    }

    public void move() {
        switchPositions(getNextItem());
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setFields(List<DatasetField> fields) {
        this.fields = fields;
    }

    public void setFieldsOrderByPosition(List<DatasetField> fieldsOrderByPosition) {
        this.fieldsOrderByPosition = fieldsOrderByPosition;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public void switchPositions(DatasetItem switchItem) {
        if (switchItem != null) {
            int temp = getPosition();
            setPosition(switchItem.getPosition());
            switchItem.setPosition(temp);

            int listPosition = getDataset().getItems().indexOf(this);
            int listPositionSwitch = getDataset().getItems().indexOf(switchItem);
            getDataset().getItems().set(listPosition, switchItem);
            getDataset().getItems().set(listPositionSwitch, this);
        }
    }
}
