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

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.forms.MFHelper;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class DatasetField extends AbstractBaseEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @ManyToOne
    @JoinColumn(name = "attributeid")
    @NotNull
    @XmlIDREF
    private DatasetAttribute attribute;

    @ManyToOne
    @JoinColumn(name = "itemid")
    @NotNull
    @XmlIDREF
    private DatasetItem item;

    @Size(max = 4096)
    @XmlElement
    private String value;

    public DatasetField() {
    }

    public DatasetField(DatasetItem item, DatasetAttribute attribute) {
        this.item = item;
        this.attribute = attribute;
    }

    public DatasetField(DatasetItem item, DatasetAttribute attribute, String value) {
        this.item = item;
        this.attribute = attribute;
        if (value != null) {
            this.value = value.trim();
        }
        if (attribute != null) {
            attribute.getFields().add(this);
        }
    }

    public DatasetField clone(DatasetItem datasetItem) {
        return new DatasetField(datasetItem, getAttribute(), Constants.EMPTY_STRING);
    }

    public DatasetField clone(DatasetItem datasetItem, Map<DatasetAttribute, DatasetAttribute> attributeMap) {
        return new DatasetField(datasetItem, attributeMap.get(getAttribute()), getValue());
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            // Important: use trimmed class name because of hibernate proxy issues.
            String objectClassName = ClassHelper.getTrimmedClassName(object.getClass().getName());
            if (objectClassName != null && objectClassName.equals(getClass().getName())) {
                // check class cast
                DatasetField baseEntity = (DatasetField) object;
                if (getItem() != null && baseEntity.getItem() != null && getAttribute() != null && baseEntity.getAttribute() != null) {
                    if (getItem().getId() == baseEntity.getItem().getId()) {
                        // check relative position of the corresponding attributes within the same dataset
                        // item
                        return Integer.compare(getAttribute().getPosition(), baseEntity.getAttribute().getPosition());
                    } else if (getItem().getId() < baseEntity.getItem().getId()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                // default in case at least one dataset item/attribute is null
                return 1;
            }
            throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + object.getClass().getName());
        }
        throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + Constants.NULL);
    }

    public DatasetAttribute getAttribute() {
        return attribute;
    }

    public AbstractEntity getEntity() {
        return getAttribute().getEntity(getValue());
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "value", getValue());
        return summary.toString();
    }

    public DatasetItem getItem() {
        return item;
    }

    public String getTitle() {
        return getValue() != null && getValue().length() > 20 ? getValue() : null;
    }

    public String getValue() {
        return value;
    }

    public String getValue(String delimiter) {
        return delimiter != null && delimiter.equals(",") ? "\"" + value + "\"" : value;
    }

    public boolean isEntityNotFound() {
        return getEntity() == null;
    }

    public boolean isEntityReference() {
        return getAttribute().isEntityReference();
    }

    public boolean isTypeInvalid() {
        if (getAttribute() != null) {
            if (StringHelper.isNotEmpty(getValue())) {
                String type = getAttribute().getType();
                if (type != null) {
                    return type.equals("Boolean") && !("true".equalsIgnoreCase(getValue()) || "false".equalsIgnoreCase(getValue()))
                        || type.equals("Integer") && !NumberUtils.isLong(getValue())
                        || type.equals("Numeric") && !NumberUtils.isNumeric(getValue())
                        || type.equals("Date") && !MFHelper.isDate(getValue())
                        || type.equals("DateTime") && !MFHelper.isDateTime(getValue())
                        || type.equals("Time") && !MFHelper.isTime(getValue())
                        || !type.equals("String") && !type.equals("Boolean") && !type.equals("Date") && !type.equals("DateTime") && !type.equals("Time") && !NumberUtils.isLong(getValue());
                }
            } else {
                return getAttribute().isRequired();
            }
        }
        return false;
    }

    public void setAttribute(DatasetAttribute attribute) {
        this.attribute = attribute;
    }

    public void setItem(DatasetItem item) {
        this.item = item;
    }

    public void setValue(String value) {
        this.value = StringHelper.format(value);
    }
}