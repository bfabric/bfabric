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

import org.bfabric.entity.DatasetTemplate;
import org.bfabric.entity.DatasetTemplateAttribute;
import org.bfabric.entity.User;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDatasetTemplate;
import org.bfabric.xml.entity.XMLDatasetTemplateAttribute;

public class MFDatasetTemplate extends AbstractMF {

    private final DatasetTemplate datasetTemplate;

    private final XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate;

    public MFDatasetTemplate(DatasetTemplate datasetTemplate, XMLRequestParameterSaveDatasetTemplate xmlRequestSaveDatasetTemplate) {
        this.datasetTemplate = datasetTemplate;
        this.xmlRequestSaveDatasetTemplate = xmlRequestSaveDatasetTemplate;
    }

    @Override
    public synchronized void apply() throws Exception {
        getDatasetTemplate().setName(getName());
        getDatasetTemplate().setSupervisor(getSupervisor());
        getDatasetTemplate().setDatasetTemplateAttributes(getDatasetTemplateAttributes());
        getDatasetTemplate().resetAttributePositions();
    }

    public DatasetTemplate getDatasetTemplate() {
        return datasetTemplate;
    }

    public List<DatasetTemplateAttribute> getDatasetTemplateAttributes() throws InvalidDataException {
        if (getXmlRequestSaveDatasetTemplate().getDatasettemplateattribute() != null) {
            getDatasetTemplate().getDatasetTemplateAttributes().clear();
            List<DatasetTemplateAttribute> datasetTemplateAttributes = new ArrayList<>();
            LinkedHashSet<String> existingNames = new LinkedHashSet<>();
            LinkedHashSet<Long> existingPositions = new LinkedHashSet<>();
            for (XMLDatasetTemplateAttribute xmlDatasetTemplateAttribute : getXmlRequestSaveDatasetTemplate().getDatasettemplateattribute()) {
                long datasetTemplateAttributePosition = MFHelper.positiveLongValueOf("datasetTemplateAttribute position", xmlDatasetTemplateAttribute.getPosition());
                if (existingPositions.contains(datasetTemplateAttributePosition)) {
                    throw new InvalidDataException("DatasetTemplateAttribute position " + datasetTemplateAttributePosition + " is not unique!");
                }
                existingPositions.add(datasetTemplateAttributePosition);
                String datasetTemplateAttributeName = xmlDatasetTemplateAttribute.getName();
                MFHelper.checkNotNull("datasetTemplateAttribute name", datasetTemplateAttributeName);
                MFHelper.checkLength("datasetTemplateAttribute name", datasetTemplateAttributeName);
                if (existingNames.contains(datasetTemplateAttributeName.toLowerCase())) {
                    throw new InvalidDataException("DatasetTemplateAttribute name at position " + datasetTemplateAttributePosition + " is not unique!");
                }
                existingNames.add(datasetTemplateAttributeName.toLowerCase());
                datasetTemplateAttributes.add(new DatasetTemplateAttribute(getDatasetTemplate(), (int) datasetTemplateAttributePosition, datasetTemplateAttributeName, xmlDatasetTemplateAttribute.getType()));
            }
            return datasetTemplateAttributes;
        }
        return getDatasetTemplate().getDatasetTemplateAttributes();
    }

    public String getName() {
        if (getXmlRequestSaveDatasetTemplate().getName() != null) {
            return getXmlRequestSaveDatasetTemplate().getName();
        }
        return getDatasetTemplate().getName();
    }

    private User getSupervisor() throws InvalidDataException {
        if (getXmlRequestSaveDatasetTemplate().getSupervisorid() != null) {
            MFHelper.checkNotNull("supervisorid", getXmlRequestSaveDatasetTemplate().getSupervisorid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSaveDatasetTemplate().getSupervisorid()));
        }
        return getDatasetTemplate().getSupervisor();
    }

    public XMLRequestParameterSaveDatasetTemplate getXmlRequestSaveDatasetTemplate() {
        return xmlRequestSaveDatasetTemplate;
    }
}
