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

package org.bfabric.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetAttribute;
import org.bfabric.entity.DatasetField;
import org.bfabric.entity.DatasetItem;
import org.bfabric.entity.DatasetTemplate;
import org.bfabric.entity.Run;
import org.bfabric.entity.Workunit;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.ArrayListReader;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDataset;
import org.bfabric.xml.entity.XMLDatasetAttribute;
import org.bfabric.xml.entity.XMLDatasetField;
import org.bfabric.xml.entity.XMLDatasetItem;

public class MFDataset extends AbstractMF {

    private final Dataset dataset;

    private final XMLRequestParameterSaveDataset xmlRequestSaveDataset;

    public MFDataset(Dataset dataset, XMLRequestParameterSaveDataset xmlRequestSaveDataset) {
        this.dataset = dataset;
        this.xmlRequestSaveDataset = xmlRequestSaveDataset;
    }

    @Override
    public synchronized void apply() throws Exception {
        getDataset().setName(getName());
        getDataset().setDescription(getDescription());
        getDataset().setContainer(getContainer());
        getDataset().setWorkunit(getWorkunit());
        getDataset().setRun(getRun());
        setContent();
        getDataset().setDatasetTemplate(getDatasetTemplate());
        // Make sure the positions are in ascending order starting with 1
        getDataset().resetItemPositions();
        getDataset().resetAttributePositions();
        getDataset().setCustomAttributes(getXmlRequestSaveDataset().getCustomattribute());
    }

    public List<DatasetAttribute> getAttributes() throws InvalidDataException {
        if (getXmlRequestSaveDataset().getAttribute() != null) {
            getDataset().getItems().clear();
            getDataset().getAttributes().clear();
            List<DatasetAttribute> attributes = new ArrayList<>();
            LinkedHashSet<String> existingNames = new LinkedHashSet<>();
            LinkedHashSet<Long> existingPositions = new LinkedHashSet<>();

            for (XMLDatasetAttribute xmlDatasetAttribute : getXmlRequestSaveDataset().getAttribute()) {
                long attributePosition = MFHelper.positiveLongValueOf("attribute position", xmlDatasetAttribute.getPosition());

                if (existingPositions.contains(attributePosition)) {
                    throw new InvalidDataException("Attribute position " + attributePosition + " is not unique!");
                }

                existingPositions.add(attributePosition);

                String attributeName = xmlDatasetAttribute.getName();
                MFHelper.checkNotNull("attribute name", attributeName);
                MFHelper.checkLength("attribute name", attributeName);

                if (existingNames.contains(attributeName.toLowerCase())) {
                    throw new InvalidDataException("Attribute name at position " + attributePosition + " is not unique!");
                }

                existingNames.add(attributeName.toLowerCase());
                attributes.add(new DatasetAttribute(getDataset(), (int) attributePosition, attributeName, xmlDatasetAttribute.getType()));
            }
            return attributes;
        }
        return getDataset().getAttributes();
    }

    public Container getContainer() throws InvalidDataException {
        if (getXmlRequestSaveDataset().getContainerid() != null) {
            Container container = (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveDataset().getContainerid()));
            if (!container.isExtensible()) {
                throw new InvalidDataException("Container " + getXmlRequestSaveDataset().getContainerid() + " is not extensible!");
            }
            return container;
        }
        return getDataset().getContainer();
    }

    public Dataset getDataset() {
        return dataset;
    }

    public DatasetTemplate getDatasetTemplate() throws InvalidDataException {
        if (getXmlRequestSaveDataset().getDatasettemplateid() != null) {
            return (DatasetTemplate) fetch(DatasetTemplate.class, MFHelper.positiveLongValueOf("DatasetTemplateid", getXmlRequestSaveDataset().getDatasettemplateid()));
        }
        return getDataset().getDatasetTemplate();
    }

    public String getDescription() {
        if (getXmlRequestSaveDataset().getDescription() != null) {
            return getXmlRequestSaveDataset().getDescription();
        }
        return getDataset().getDescription();
    }

    private List<DatasetField> getFields(XMLDatasetItem xmlDatasetItem, DatasetItem item) throws InvalidDataException {
        List<DatasetField> fields = new ArrayList<>();
        LinkedHashSet<Long> existingFieldAttributePositions = new LinkedHashSet<>();

        for (XMLDatasetField xmlDatasetField : xmlDatasetItem.getField()) {
            boolean correspondingAttributeFound = false;
            long fieldAttributePosition = MFHelper.positiveLongValueOf("Field attributeposition", xmlDatasetField.getAttributeposition());

            if (existingFieldAttributePositions.contains(fieldAttributePosition)) {
                throw new InvalidDataException("Field attributeposition " + fieldAttributePosition + " is not unique!");
            }
            MFHelper.checkLength("Field value", xmlDatasetField.getValue(), 4096);

            existingFieldAttributePositions.add(fieldAttributePosition);

            for (DatasetAttribute attribute : getDataset().getAttributes()) {
                DatasetField field = null;
                if (Long.valueOf(attribute.getPosition()).equals(fieldAttributePosition)) {
                    field = new DatasetField(item, attribute, xmlDatasetField.getValue());
                    correspondingAttributeFound = true;
                }

                if (field != null) {
                    switch (attribute.getType()) {
                    case "Boolean":
                        MFHelper.booleanValueOf("Value of field at attributeposition " + fieldAttributePosition, field.getValue());
                        break;
                    case "Integer":
                        MFHelper.integerValueOf("Value of field at attributeposition " + fieldAttributePosition, field.getValue());
                        break;
                    case "Long":
                        MFHelper.longValueOf("Value of field at attributeposition " + fieldAttributePosition, field.getValue());
                        break;
                    default:
                        break;
                    }

                    fields.add(field);
                }
            }

            if (!correspondingAttributeFound) {
                throw new InvalidDataException("Field attributeposition " + fieldAttributePosition + " does not correspond to an existing attribute position!");
            }
        }

        return fields;
    }

    public List<DatasetItem> getItems() throws InvalidDataException {
        if (getXmlRequestSaveDataset().getItem() != null) {
            List<DatasetItem> items = new ArrayList<>();
            LinkedHashSet<Long> existingItemPositions = new LinkedHashSet<>();

            if (getDataset().getAttributes().isEmpty()) {
                MFHelper.throwNoValueSpecifiedError("attributes");
            }

            for (XMLDatasetItem xmlDatasetItem : getXmlRequestSaveDataset().getItem()) {
                if (xmlDatasetItem.getField().size() != getDataset().getAttributes().size()) {
                    throw new InvalidDataException("No. of item fields incorrect. There should be " + getDataset().getAttributes().size() + "!");
                }

                long itemPosition = MFHelper.positiveLongValueOf("item position", xmlDatasetItem.getPosition());
                if (existingItemPositions.contains(itemPosition)) {
                    throw new InvalidDataException("Item position " + itemPosition + " is not unique!");
                }

                existingItemPositions.add(itemPosition);

                DatasetItem item = new DatasetItem(getDataset(), (int) itemPosition);
                item.getFields().addAll(getFields(xmlDatasetItem, item));
                items.add(item);
            }
            return items;
        }
        return getDataset().getItems();
    }

    public String getName() {
        if (getXmlRequestSaveDataset().getName() != null) {
            return getXmlRequestSaveDataset().getName();
        }
        return getDataset().getName();
    }

    public Run getRun() throws InvalidDataException {
        if (getXmlRequestSaveDataset().getRunid() != null) {
            if (StringHelper.isEmpty(getXmlRequestSaveDataset().getRunid())) {
                return null;
            }
            return (Run) fetch(Run.class, MFHelper.positiveLongValueOf("runid", getXmlRequestSaveDataset().getRunid()));
        }
        return getDataset().getRun();
    }

    public Workunit getWorkunit() throws InvalidDataException {
        if (getXmlRequestSaveDataset().getWorkunitid() != null) {
            Workunit workunit = (Workunit) fetch(Workunit.class, MFHelper.positiveLongValueOf("workunitid", getXmlRequestSaveDataset().getWorkunitid()));
            if (workunit.getDataset() != null && workunit.getDataset().getId() != getDataset().getId()) {
                throw new InvalidDataException("Workunit " + workunit.getId() + " is assigned to another dataset!");
            }
            return workunit;
        }

        return getDataset().getWorkunit();
    }

    public XMLRequestParameterSaveDataset getXmlRequestSaveDataset() {
        return xmlRequestSaveDataset;
    }

    public void setContent() throws InvalidDataException {
        if (StringHelper.isNotEmpty(getXmlRequestSaveDataset().getContenttsv())) {
            if (getXmlRequestSaveDataset().getAttribute() != null && !getXmlRequestSaveDataset().getAttribute().isEmpty()) {
                throw new InvalidDataException("Attribute and contenttsv cannot be used together!");
            }
            if (getXmlRequestSaveDataset().getItem() != null && !getXmlRequestSaveDataset().getItem().isEmpty()) {
                throw new InvalidDataException("Item and contenttsv cannot be used together!");
            }
            getDataset().setContent(ArrayListReader.createArrayListFromTSV(getXmlRequestSaveDataset().getContenttsv()));
        } else {
            getDataset().setAttributes(getAttributes());
            getDataset().setItems(getItems());
        }
    }
}
