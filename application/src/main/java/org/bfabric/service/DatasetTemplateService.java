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

package org.bfabric.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.DatasetTemplate;
import org.bfabric.entity.DatasetTemplateAttribute;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;
import org.bfabric.xml.XmlHelper;

@Named
@Stateless
public class DatasetTemplateService extends AbstractService {

    private static final long serialVersionUID = 1;

    public DatasetTemplateService() {
        super(DatasetTemplate.class);
    }

    public BfabricLazyDataModel<DatasetTemplate> getAttributeEquivalentDatasetTemplatesByDatasetId(Long datasetId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("(SELECT COUNT(*) FROM DatasetTemplateAttribute WHERE datasetTemplate.id = entity.id) = (SELECT COUNT(*) FROM DatasetAttribute WHERE dataset.id = :datasetId) AND NOT EXISTS(SELECT id FROM DatasetTemplateAttribute da1 WHERE datasetTemplate.id = entity.id AND NOT EXISTS (SELECT id FROM DatasetAttribute da2 WHERE dataset.id = :datasetId AND (da1.position, da1.name, da1.type, da1.required) = (da2.position, da2.name, da2.type, da2.required)))");
        entityQuery.addParameter("datasetId", datasetId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<DatasetTemplate> getAttributeEquivalentDatasetTemplatesByDatasetTemplateId(Long datasetTemplateId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("entity.id <> :datasetTemplateId and (SELECT COUNT(*) FROM DatasetTemplateAttribute WHERE datasetTemplate.id = entity.id) = (SELECT COUNT(*) FROM DatasetTemplateAttribute WHERE datasetTemplate.id = :datasetTemplateId) AND NOT EXISTS(SELECT id FROM DatasetTemplateAttribute da1 WHERE datasetTemplate.id = entity.id AND NOT EXISTS (SELECT id FROM DatasetTemplateAttribute da2 WHERE datasetTemplate.id = :datasetTemplateId AND (da1.position, da1.name, da1.type, da1.required) = (da2.position, da2.name, da2.type, da2.required)))");
        entityQuery.addParameter("datasetTemplateId", datasetTemplateId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<DatasetTemplate> getDatasetTemplates(String filterString) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addIdOrNameWhereClause(filterString);
        entityQuery.setMaxResult(100);
        return (List<DatasetTemplate>) entityQuery.getResultList();
    }

    public List<DatasetTemplate> getFilteredEnabledIncludingOrderBy(DatasetTemplate entity, String filterString) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("enabled = true or entity = :entity");
        entityQuery.addParameter("entity", entity);
        entityQuery.setOrder("name");
        return (List<DatasetTemplate>) entityQuery.getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final DatasetTemplate datasetTemplate = (DatasetTemplate) entity;
        LinkedHashMap<String, String> errorMsg = isValidName((DatasetTemplate) entity);
        if (!errorMsg.isEmpty()) {
            return errorMsg;
        }
        if (datasetTemplate.getDatasetTemplateAttributes().isEmpty()) {
            errorMsg.put(null, Messages.get("validationErrorEmptyAttributes"));
            errorMsg.put("edit:datasetTemplateAttributes", Messages.get("javax.faces.component.UIInput.REQUIRED"));
            return errorMsg;
        }
        List<DatasetTemplateAttribute> attributesToCheck = new ArrayList<>(datasetTemplate.getDatasetTemplateAttributes());
        for (DatasetTemplateAttribute attribute1 : datasetTemplate.getDatasetTemplateAttributes()) {
            for (DatasetTemplateAttribute attribute2 : attributesToCheck) {
                if (!attribute1.equals(attribute2) && attribute1.getName().equalsIgnoreCase(attribute2.getName())) {
                    errorMsg.put(null, Messages.get("validationErrorColumnsSameName"));
                    errorMsg.put("edit:datasetTemplateAttributes", attribute1.getName() + " " + Messages.get("notUniqueException"));
                    return errorMsg;
                }
            }
        }
        return errorMsg;
    }

    public void save(DatasetTemplate datasetTemplate) {
        save(datasetTemplate, true);
    }

    public void save(DatasetTemplate datasetTemplate, boolean index) {
        if (datasetTemplate != null) {
            datasetTemplate.resetAttributePositions();
            if (datasetTemplate.isManaged() && StringHelper.isNotEmpty(XmlHelper.getXmlLogDiff(datasetTemplate.getOldStateAsXml(), datasetTemplate.getXml()))) {
                datasetTemplate.createEntityLogUpdate();
            }
            super.save(datasetTemplate, index);
        }
    }
}
