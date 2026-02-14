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

package org.bfabric.webservice.server.manager;

import java.util.List;

import javax.inject.Inject;

import org.bfabric.entity.DatasetTemplate;
import org.bfabric.entity.DatasetTemplateAttribute;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFDatasetTemplate;
import org.bfabric.forms.MFHelper;
import org.bfabric.service.DatasetTemplateService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRenameDatasetTemplateAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDatasetTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetTemplateAttributePositions;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;
import org.bfabric.xml.entity.XMLDatasetTemplate;

public class WSDatasetTemplateManager extends AbstractWSEntityManager<DatasetTemplate, XMLDatasetTemplate> {

    @Inject
    private DatasetTemplateService datasetTemplateService;

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFDatasetTemplate(getInstance(), (XMLRequestParameterSaveDatasetTemplate) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFDatasetTemplate(getInstance(), (XMLRequestParameterSaveDatasetTemplate) aXmlRequestSaveEntity);
    }

    @Override
    protected <T> void isValid(T entity) throws Exception {
        super.isValid(entity);
        handleValidationErrors(datasetTemplateService.isValid(getInstance()));
    }

    public XMLResponse renameDatasetTemplateAttribute(List<XMLRequestParameterRenameDatasetTemplateAttribute> xmlRequestParameterRenameDatasetTemplateAttributeList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterRenameDatasetTemplateAttribute xmlRequestParameterRenameDatasetAttributeDataset : xmlRequestParameterRenameDatasetTemplateAttributeList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                DatasetTemplate datasetTemplate = (DatasetTemplate) wsService.fetchAndSetOldStateAsXml(DatasetTemplate.class, MFHelper.positiveLongValueOf("datasettemplateid", xmlRequestParameterRenameDatasetAttributeDataset.getDatasettemplateid()));
                setInstance(datasetTemplate);

                DatasetTemplateAttribute templateAttribute = datasetTemplate.getDatasetTemplateAttributeChecked(xmlRequestParameterRenameDatasetAttributeDataset.getOldname());

                templateAttribute.setName(xmlRequestParameterRenameDatasetAttributeDataset.getNewname());

                isValid(getInstance());

                for (DatasetTemplateAttribute datasetTemplateAttribute : getInstance().getDatasetTemplateAttributes()) {
                    isValid(datasetTemplateAttribute);
                }

                datasetTemplateService.save(getInstance(), false);

                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    public XMLResponse switchDatasetTemplateAttributePositions(List<XMLRequestParameterSwitchDatasetTemplateAttributePositions> xmlRequestParameterRenameDatasetTemplateAttributeList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterSwitchDatasetTemplateAttributePositions xmlRequestParameterSwitchDatasetTemplateAttributePositionDataset : xmlRequestParameterRenameDatasetTemplateAttributeList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                DatasetTemplate datasetTemplate = (DatasetTemplate) wsService.fetchAndSetOldStateAsXml(DatasetTemplate.class, MFHelper.positiveLongValueOf("datasettemplateid", xmlRequestParameterSwitchDatasetTemplateAttributePositionDataset.getDatasettemplateid()));
                setInstance(datasetTemplate);

                DatasetTemplateAttribute datasetTemplateAttribute1 = datasetTemplate.getDatasetTemplateAttributeChecked(xmlRequestParameterSwitchDatasetTemplateAttributePositionDataset.getAttributename1());
                DatasetTemplateAttribute datasetTemplateAttribute2 = datasetTemplate.getDatasetTemplateAttributeChecked(xmlRequestParameterSwitchDatasetTemplateAttributePositionDataset.getAttributename2());

                datasetTemplateAttribute1.switchPositions(datasetTemplateAttribute2);

                isValid(getInstance());

                for (DatasetTemplateAttribute datasetTemplateAttribute : getInstance().getDatasetTemplateAttributes()) {
                    isValid(datasetTemplateAttribute);
                }

                datasetTemplateService.save(getInstance(), false);

                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }
}