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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Messages;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.DatasetService;
import org.bfabric.service.DatasetTemplateService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class DatasetTemplate extends AbstractSupervisorDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "datasetTemplate")
    @OrderBy("id desc")
    private Set<Application> applications = new HashSet<>();

    @Transient
    private BfabricLazyDataModel<Dataset> attributeCompatibleDatasets;

    @Transient
    private BfabricLazyDataModel<DatasetTemplate> attributeEquivalentDatasetTemplates;

    @Transient
    private BfabricLazyDataModel<Dataset> attributeEquivalentDatasets;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "datasetTemplate", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("position")
    private List<DatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "datasetTemplate")
    @OrderBy("id desc")
    private Set<Dataset> datasets = new HashSet<>();

    @XmlElement
    private boolean enabled = true;

    public void addAttribute() {
        addAttribute(null, null, false);
    }

    public DatasetTemplateAttribute addAttribute(String attributeName, String attributeType, boolean required) {
        DatasetTemplateAttribute datasetAttribute = null;
        if (attributeName != null) {
            // If name is given, get attribute if exists
            datasetAttribute = getAttribute(attributeName);
        }
        if (datasetAttribute == null) {
            // Create new attribute (no name given or no existing found)
            datasetAttribute = new DatasetTemplateAttribute(this, null, attributeName, attributeType, required);
            getDatasetTemplateAttributes().add(datasetAttribute);
        }
        return datasetAttribute;
    }

    @Override
    public DatasetTemplate clone() throws CloneNotSupportedException {
        DatasetTemplate clone = (DatasetTemplate) super.clone();
        clone.setDatasetTemplateAttributes(new ArrayList<>());
        clone.setName("Clone " + getName());
        for (DatasetTemplateAttribute attribute : getDatasetTemplateAttributes()) {
            DatasetTemplateAttribute attributeClone = (DatasetTemplateAttribute) attribute.clone();
            clone.getDatasetTemplateAttributes().add(attributeClone);
            attributeClone.setDatasetTemplate(clone);
        }
        clone.setSupervisor(getCurrentUser());
        return clone;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public DatasetTemplateAttribute getAttribute(String attributeName) {
        for (DatasetTemplateAttribute attribute : getDatasetTemplateAttributes()) {
            if (attributeName.equalsIgnoreCase(attribute.getName())) {
                return attribute;
            }
        }
        return null;
    }

    public DatasetTemplateAttribute getAttributeByPosition(int position) {
        return position > 0 && position <= getDatasetTemplateAttributes().size() ? getDatasetTemplateAttributes().get(position - 1) : null;
    }

    public BfabricLazyDataModel<Dataset> getAttributeCompatibleDatasets() {
        if (attributeCompatibleDatasets == null) {
            attributeCompatibleDatasets = CDI.current().select(DatasetService.class).get().getAttributeCompatibleDatasetsByDatasetTemplateId(getId());
        }
        return attributeCompatibleDatasets;
    }

    public BfabricLazyDataModel<DatasetTemplate> getAttributeEquivalentDatasetTemplates() {
        if (attributeEquivalentDatasetTemplates == null) {
            attributeEquivalentDatasetTemplates = CDI.current().select(DatasetTemplateService.class).get().getAttributeEquivalentDatasetTemplatesByDatasetTemplateId(getId());
        }
        return attributeEquivalentDatasetTemplates;
    }

    public BfabricLazyDataModel<Dataset> getAttributeEquivalentDatasets() {
        if (attributeEquivalentDatasets == null) {
            attributeEquivalentDatasets = CDI.current().select(DatasetService.class).get().getAttributeEquivalentDatasetsByDatasetTemplateId(getId());
        }
        return attributeEquivalentDatasets;
    }

    @XmlElement(name = "attribute")
    public List<String> getAttributesAsXml() {
        List<String> ret = new ArrayList<>();
        for (DatasetTemplateAttribute attribute : getDatasetTemplateAttributes()) {
            ret.add(attribute.getPosition() + " " + attribute.getName() + " (" + attribute.getType() + ")");
        }
        return ret;
    }

    public DatasetTemplateAttribute getDatasetTemplateAttributeChecked(String attributeName) throws InvalidDataException {
        DatasetTemplateAttribute attribute = getAttribute(attributeName);
        if (attribute == null) {
            List<String> availableNames = new ArrayList<>();
            for (DatasetTemplateAttribute existingAttribute : getDatasetTemplateAttributes()) {
                availableNames.add(existingAttribute.getName());
            }
            throw new InvalidDataException("There is no dataset template attribute " + attributeName + "! Available attributes: " + CollectionHelper.print(availableNames) + ".");
        }
        return attribute;
    }

    public String getDatasetTemplateAttributeTypesAsString() {
        return CollectionHelper.printTypes(datasetTemplateAttributes);
    }

    public List<DatasetTemplateAttribute> getDatasetTemplateAttributes() {
        return datasetTemplateAttributes;
    }

    public String getDatasetTemplateAttributesAsString() {
        return CollectionHelper.printNames(datasetTemplateAttributes);
    }

    public Set<Dataset> getDatasets() {
        return datasets;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getDatasetTemplateAttributes() != null) {
            addEntityInfoItem(summary, "attributes", getDatasetTemplateAttributes().size());
            addEntityInfoItem(summary, "attributeNames", getDatasetTemplateAttributesAsString());
            addEntityInfoItem(summary, "attributeTypes", getDatasetTemplateAttributeTypesAsString());
        }
        return summary.toString();
    }

    public String getIncompatibleHint(Dataset dataset) {
        return dataset != null && !dataset.isAttributeEquivalentWith(this) ? Messages.get("datasetTemplateIncompatibleHint") : "";
    }

    public String getLabel() {
        return getName() + " [" + getDatasetTemplateAttributesAsString() + "] ";
    }

    public String getLabel(Dataset dataset) {
        return getLabel() + getIncompatibleHint(dataset);
    }

    public int getLastAttributePosition() {
        return !getDatasetTemplateAttributes().isEmpty() ? getDatasetTemplateAttributes().get(getDatasetTemplateAttributes().size() - 1).getPosition() : 0;
    }

    public int getNextAttributePosition() {
        return getLastAttributePosition() + 1;
    }

    @Override
    @NotNull
    public User getSupervisor() {
        return super.getSupervisor();
    }

    public boolean isAttributeEquivalentWith(DatasetTemplate datasetTemplate) {
        if (datasetTemplate != null && datasetTemplate.getDatasetTemplateAttributes().size() == getDatasetTemplateAttributes().size()) {
            for (DatasetTemplateAttribute attribute : getDatasetTemplateAttributes()) {
                DatasetTemplateAttribute attributeAtSamePosition = getAttributeByPosition(attribute.getPosition());
                if (!attribute.getType().equalsIgnoreCase(attributeAtSamePosition.getType())
                    || !attribute.getName().equalsIgnoreCase(attributeAtSamePosition.getName())
                    || attribute.isRequired() != attributeAtSamePosition.isRequired()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isAttributeNameUnique(String attributeName) {
        return getAttribute(attributeName) == null;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isReadable() {
        return isAdminOrSupervisor() || hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isRenderedRemoveAttribute(DatasetTemplateAttribute attribute) {
        return isUpdatable() && getDatasetTemplateAttributes().size() > 1;
    }

    @Override
    public boolean isUpdatable() {
        return getApplications().isEmpty() && getDatasets().isEmpty();
    }

    @Override
    protected void preUpdate() {
        // Important: Do not log update using the preUpdate. This is done via the save method of the DatasetTemplateService!
        setLogEntity(false);
        super.preUpdate();
    }

    public void removeAttribute(DatasetTemplateAttribute attribute) {
        getDatasetTemplateAttributes().remove(attribute);
        resetAttributePositions();
    }

    public void resetAttributePositions() {
        int position = 1;
        for (DatasetTemplateAttribute attribute : getDatasetTemplateAttributes()) {
            attribute.setPosition(position);
            position++;
        }
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public void setDatasetTemplateAttributes(List<DatasetTemplateAttribute> datasetTemplateAttributes) {
        this.datasetTemplateAttributes = datasetTemplateAttributes;
    }

    public void setDatasets(Set<Dataset> datasets) {
        this.datasets = datasets;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void switchEnabled() {
        setEnabled(!isEnabled());
    }
}
