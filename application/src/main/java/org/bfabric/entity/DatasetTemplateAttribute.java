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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.NotEntityLoggable;

@Entity
@XmlRootElement
public class DatasetTemplateAttribute extends AbstractNamedBaseEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @ManyToOne
    @JoinColumn(name = "datasetTemplateid")
    @NotNull
    @XmlIDREF
    private DatasetTemplate datasetTemplate;

    @NotNull
    @XmlElement
    private int position = 0;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean required = false;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String type = Constants.STRING;

    public DatasetTemplateAttribute() {
    }

    public DatasetTemplateAttribute(DatasetAttribute datasetAttribute, DatasetTemplate datasetTemplate) {
        this.datasetTemplate = datasetTemplate;
        if (datasetAttribute != null) {
            this.position = datasetAttribute.getPosition();
            this.name = datasetAttribute.getName();
            this.type = datasetAttribute.getType();
            this.required = datasetAttribute.isRequired();
        }
    }

    public DatasetTemplateAttribute(DatasetTemplate datasetTemplate, Integer position, String name, String type) {
        this(datasetTemplate, position, name, type, false);
    }

    public DatasetTemplateAttribute(DatasetTemplate datasetTemplate, Integer position, String name, String type, boolean required) {
        this.datasetTemplate = datasetTemplate;
        this.position = position != null ? position : datasetTemplate.getNextAttributePosition();
        this.name = name != null ? name.trim() : generateName();
        if (type != null) {
            this.type = type.trim();
        }
        this.required = required;
    }

    private String generateName() {
        String newName = "COLUMN";
        boolean nameUnique = false;
        int i = 1;
        while (!nameUnique) {
            if (getDatasetTemplate().isAttributeNameUnique(newName + "_" + i)) {
                nameUnique = true;
            } else {
                i++;
            }
        }
        return newName + "_" + i;
    }

    public DatasetTemplate getDatasetTemplate() {
        return datasetTemplate;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "position", getPosition());
        addEntityInfoItem(summary, "type", getType());
        addEntityInfoItem(summary, "required", isRequired());
        if (getDatasetTemplate() != null) {
            addEntityInfoItem(summary, "datasetTemplate", getDatasetTemplate().getId());
        }
        return summary.toString();
    }

    private DatasetTemplateAttribute getNextAttribute() {
        DatasetTemplateAttribute nextAttribute = null;
        int listPosition = getDatasetTemplate().getDatasetTemplateAttributes().indexOf(this) + 1;
        if (listPosition < getDatasetTemplate().getDatasetTemplateAttributes().size()) {
            nextAttribute = getDatasetTemplate().getDatasetTemplateAttributes().get(listPosition);
        }
        return nextAttribute;
    }

    public int getPosition() {
        return position;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean isDeletable() {
        return getDatasetTemplate() != null && getDatasetTemplate().isUpdatable();
    }

    public boolean isMovable() {
        return getPosition() != getDatasetTemplate().getLastAttributePosition();
    }

    @Override
    public boolean isReadable() {
        return getDatasetTemplate() != null && getDatasetTemplate().isReadable();
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isUpdatable() {
        return getDatasetTemplate() != null && getDatasetTemplate().isUpdatable();
    }

    public void move() {
        switchPositions(getNextAttribute());
    }

    public void setDatasetTemplate(DatasetTemplate datasetTemplate) {
        this.datasetTemplate = datasetTemplate;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void switchPositions(DatasetTemplateAttribute switchAttribute) {
        if (switchAttribute != null) {
            int temp = getPosition();
            setPosition(switchAttribute.getPosition());
            switchAttribute.setPosition(temp);

            int listPosition = getDatasetTemplate().getDatasetTemplateAttributes().indexOf(this);
            int listPositionSwitch = getDatasetTemplate().getDatasetTemplateAttributes().indexOf(switchAttribute);
            getDatasetTemplate().getDatasetTemplateAttributes().set(listPosition, switchAttribute);
            getDatasetTemplate().getDatasetTemplateAttributes().set(listPositionSwitch, this);
        }
    }
}