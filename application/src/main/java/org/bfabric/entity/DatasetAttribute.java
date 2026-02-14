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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.text.WordUtils;
import org.bfabric.Constants;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;

@Entity
@XmlRootElement
public class DatasetAttribute extends AbstractNamedBaseEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @ManyToOne
    @JoinColumn(name = "datasetid")
    @NotNull
    @XmlIDREF
    private Dataset dataset;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("id")
    @XmlIDREF
    @XmlElement(name = "field")
    private List<DatasetField> fields = new ArrayList<>();

    @NotNull
    @XmlElement
    private int position = 0;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean required = false;

    @Transient
    private Boolean showScreenAvailable;

    @Transient
    private String showScreenPath;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String type = Constants.STRING;

    public DatasetAttribute() {
    }

    public DatasetAttribute(DatasetTemplateAttribute datasetTemplateAttribute, Dataset dataset) {
        this.name = datasetTemplateAttribute.getName();
        this.position = datasetTemplateAttribute.getPosition();
        this.type = datasetTemplateAttribute.getType();
        this.required = datasetTemplateAttribute.isRequired();
        this.dataset = dataset;
    }

    public DatasetAttribute(Dataset dataset, Integer position, String name, String type) {
        this(dataset, position, name, type, false);
    }

    public DatasetAttribute(Dataset dataset, Integer position, String name, String type, boolean required) {
        this.dataset = dataset;
        this.position = position != null ? position : dataset.getNextAttributePosition();
        this.name = name != null ? name.trim() : generateName();
        if (type != null) {
            this.type = type;
        }
        this.required = required;
    }

    public DatasetAttribute clone(final Dataset cloneDataset) {
        DatasetAttribute clone = new DatasetAttribute();
        clone.setPosition(getPosition());
        clone.setName(getName());
        clone.setType(getType());
        clone.setRequired(isRequired());

        // link attribute with the dataset
        clone.setDataset(cloneDataset);
        cloneDataset.getAttributes().add(clone);
        return clone;
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            // Important: use trimmed class name because of hibernate proxy issues.
            String objectClassName = ClassHelper.getTrimmedClassName(object.getClass().getName());
            if (objectClassName != null && objectClassName.equals(getClass().getName())) {
                // check class cast
                DatasetAttribute baseEntity = (DatasetAttribute) object;
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

    private String generateName() {
        String newName = "COLUMN";
        boolean nameUnique = false;
        int i = 1;
        while (!nameUnique) {
            if (getDataset().isAttributeNameUnique(newName + "_" + i)) {
                nameUnique = true;
            } else {
                i++;
            }
        }
        return newName + "_" + i;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public AbstractEntity getEntity(String entityId) {
        if (isEntityReference()) {
            try {
                AbstractEntity entity = getEntityService().getEntityByClassNameAndId(getType(), Long.valueOf(entityId));
                return entity != null && entity.isReadable() ? entity : null;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "position", getPosition());
        addEntityInfoItem(summary, "type", getType());
        addEntityInfoItem(summary, "required", isRequired());
        if (getDataset() != null) {
            addEntityInfoItem(summary, "dataset", getDataset().getId());
        }
        return summary.toString();
    }

    public List<DatasetField> getFields() {
        return fields;
    }

    public String getFullShowScreenPath(String entityId) {
        return getShowScreenPath() != null && NumberUtils.isNumericGreaterZero(entityId) == null ? getShowScreenPath() + "?id=" + entityId : null;
    }

    public int getPosition() {
        return position;
    }

    public String getShowScreenPath() {
        if (showScreenPath == null) {
            setShowScreen();
        }
        return showScreenPath;
    }

    public String getType() {
        return type;
    }

    public boolean hasNonEmptyField() {
        for (final DatasetItem item : getDataset().getItems()) {
            DatasetField field = item.getField(this);
            if (field != null && field.getValue() != null && !field.getValue().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEntityReference() {
        return ClassHelper.getEntityClassNames().contains(getType());
    }

    public boolean isMovable() {
        return !getDataset().isDatasetTemplateFixed() && getPosition() != getDataset().getLastAttributePosition();
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isShowScreenAvailable() {
        if (showScreenAvailable == null) {
            setShowScreen();
        }
        return showScreenAvailable;
    }

    public void prettyPrint() {
        if (getName() != null) {
            setName(getName());
            setType(getType());
        }
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setFields(List<DatasetField> fields) {
        this.fields = fields;
    }

    @Override
    public void setName(String name) {
        this.name = WordUtils.capitalize(StringHelper.format(name));
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setShowScreen() {
        if (getType() != null) {
            setShowScreenAvailable(ClassHelper.isShowScreenAvailable(getType()));
            if (isShowScreenAvailable()) {
                setShowScreenPath(UriHelper.getUrlShowScreen(getType()));
            }
        }
    }

    public void setShowScreenAvailable(Boolean showScreenAvailable) {
        this.showScreenAvailable = showScreenAvailable;
    }

    public void setShowScreenPath(String showScreenPath) {
        this.showScreenPath = StringHelper.format(showScreenPath);
    }

    public void setType(String type) {
        this.type = ClassHelper.getClassName(type);
        setShowScreen();
    }
}