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

import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetAttribute;
import org.bfabric.entity.DatasetItem;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFDataset;
import org.bfabric.forms.MFHelper;
import org.bfabric.service.DatasetService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRemoveDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRenameDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDataset;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetAttributePositions;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetItemPositions;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;
import org.bfabric.xml.entity.XMLDataset;

public class WSDatasetManager extends AbstractWSEntityManager<Dataset, XMLDataset> {

    @Inject
    private DatasetService datasetService;

    public XMLResponse addAttribute(List<XMLRequestParameterAddDatasetAttribute> xmlRequestAddDatasetAttributeList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterAddDatasetAttribute xmlRequestAddDatasetAttributeDataset : xmlRequestAddDatasetAttributeList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                Dataset dataset = (Dataset) wsService.fetchAndSetOldStateAsXml(Dataset.class, MFHelper.positiveLongValueOf("datasetid", xmlRequestAddDatasetAttributeDataset.getDatasetid()));
                setInstance(dataset);

                dataset.checkAttribute(xmlRequestAddDatasetAttributeDataset.getNewattribute());

                dataset.addAttribute(null, xmlRequestAddDatasetAttributeDataset.getNewattribute(), xmlRequestAddDatasetAttributeDataset.getNewattributevalue(), xmlRequestAddDatasetAttributeDataset.getNewattributetype());

                isValid(getInstance());

                for (DatasetAttribute datasetAttribute : getInstance().getAttributes()) {
                    isValid(datasetAttribute);
                }

                datasetService.save(getInstance(), null);

                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFDataset(getInstance(), (XMLRequestParameterSaveDataset) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFDataset(getInstance(), (XMLRequestParameterSaveDataset) aXmlRequestSaveEntity);
    }

    @Override
    protected <T> void isValid(T entity) throws Exception {
        super.isValid(entity);
        handleValidationErrors(datasetService.isValid(getInstance()));
    }

    public XMLResponse removeAttribute(List<XMLRequestParameterRemoveDatasetAttribute> xmlRequestRemoveDatasetAttributeList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterRemoveDatasetAttribute xmlRequestRemoveDatasetAttributeDataset : xmlRequestRemoveDatasetAttributeList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                Dataset dataset = (Dataset) wsService.fetchAndSetOldStateAsXml(Dataset.class, MFHelper.positiveLongValueOf("datasetid", xmlRequestRemoveDatasetAttributeDataset.getDatasetid()));
                setInstance(dataset);

                DatasetAttribute datasetAttribute = dataset.getAttributeChecked(xmlRequestRemoveDatasetAttributeDataset.getAttribute());

                dataset.removeAttribute(datasetAttribute);

                isValid(getInstance());

                datasetService.save(getInstance(), null);

                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    public XMLResponse renameAttribute(List<XMLRequestParameterRenameDatasetAttribute> xmlRequestRenameDatasetAttributeList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterRenameDatasetAttribute xmlRequestRenameDatasetAttributeDataset : xmlRequestRenameDatasetAttributeList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                Dataset dataset = (Dataset) wsService.fetchAndSetOldStateAsXml(Dataset.class, MFHelper.positiveLongValueOf("datasetid", xmlRequestRenameDatasetAttributeDataset.getDatasetid()));
                setInstance(dataset);

                DatasetAttribute attribute = dataset.getAttributeChecked(xmlRequestRenameDatasetAttributeDataset.getOldname());

                attribute.setName(xmlRequestRenameDatasetAttributeDataset.getNewname());

                isValid(getInstance());

                for (DatasetAttribute datasetAttribute : getInstance().getAttributes()) {
                    isValid(datasetAttribute);
                }

                datasetService.save(getInstance(), null);

                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    public XMLResponse switchAttributePositions(List<XMLRequestParameterSwitchDatasetAttributePositions> xmlRequestSwitchAttributePositionsList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterSwitchDatasetAttributePositions xmlRequestSwitchAttributePositionsDataset : xmlRequestSwitchAttributePositionsList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                Dataset dataset = (Dataset) wsService.fetchAndSetOldStateAsXml(Dataset.class, MFHelper.positiveLongValueOf("datasetid", xmlRequestSwitchAttributePositionsDataset.getDatasetid()));
                setInstance(dataset);

                DatasetAttribute attribute1 = dataset.getAttributeChecked(xmlRequestSwitchAttributePositionsDataset.getAttributename1());
                DatasetAttribute attribute2 = dataset.getAttributeChecked(xmlRequestSwitchAttributePositionsDataset.getAttributename2());

                dataset.switchAttributePositions(attribute1, attribute2);

                isValid(getInstance());

                for (DatasetAttribute datasetAttribute : getInstance().getAttributes()) {
                    isValid(datasetAttribute);
                }

                datasetService.save(getInstance(), null);

                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(e.getMessage());
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }

    public XMLResponse switchItemPositions(List<XMLRequestParameterSwitchDatasetItemPositions> xmlRequestSwitchItemPositionsList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterSwitchDatasetItemPositions xmlRequestSwitchItemPositionsDataset : xmlRequestSwitchItemPositionsList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                Dataset dataset = (Dataset) wsService.fetchAndSetOldStateAsXml(Dataset.class, MFHelper.positiveLongValueOf("datasetid", xmlRequestSwitchItemPositionsDataset.getDatasetid()));
                setInstance(dataset);

                DatasetItem item1 = dataset.getItemChecked(MFHelper.integerValueOf("itemposition1", xmlRequestSwitchItemPositionsDataset.getItemposition1()));
                DatasetItem item2 = dataset.getItemChecked(MFHelper.integerValueOf("itemposition2", xmlRequestSwitchItemPositionsDataset.getItemposition2()));

                // Switch Item positions.
                item1.switchPositions(item2);

                isValid(getInstance());

                for (DatasetItem datasetItem : getInstance().getItems()) {
                    isValid(datasetItem);
                }

                datasetService.save(getInstance(), null);

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
