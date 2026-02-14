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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetAttribute;
import org.bfabric.entity.DatasetField;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.entity.Workunit;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;
import org.bfabric.xml.XmlHelper;

@Named
@Stateless
public class DatasetService extends AbstractService {

    private static final long serialVersionUID = 1;

    public DatasetService() {
        super(Dataset.class);
    }

    public BfabricLazyDataModel<Dataset> getAttributeCompatibleDatasetsByDatasetTemplateId(Long datasetTemplateId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("(entity.datasetTemplate is null or entity.datasetTemplate.id <> :datasetTemplateId) and (SELECT COUNT(*) FROM DatasetAttribute WHERE dataset.id = entity.id) >= (SELECT COUNT(*) FROM DatasetTemplateAttribute WHERE datasetTemplate.id = :datasetTemplateId) AND NOT EXISTS(SELECT id FROM DatasetAttribute da1 WHERE dataset.id = entity.id AND da1.position <= (SELECT COUNT(*) FROM DatasetTemplateAttribute WHERE datasetTemplate.id = :datasetTemplateId)  AND NOT EXISTS (SELECT id FROM DatasetTemplateAttribute da2 WHERE datasetTemplate.id = :datasetTemplateId AND (da1.position, da1.name, da1.type, da1.required) = (da2.position, da2.name, da2.type, da2.required)))");
        entityQuery.addParameter("datasetTemplateId", datasetTemplateId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Dataset> getAttributeEquivalentDatasetsByDatasetId(Long datasetId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("entity.id <> :datasetId and (SELECT COUNT(*) FROM DatasetAttribute WHERE dataset.id = entity.id) = (SELECT COUNT(*) FROM DatasetAttribute WHERE dataset.id = :datasetId) AND NOT EXISTS(SELECT id FROM DatasetAttribute da1 WHERE dataset.id = entity.id AND NOT EXISTS (SELECT id FROM DatasetAttribute da2 WHERE da2.dataset.id = :datasetId AND (da1.position, da1.name, da1.type, da1.required) = (da2.position, da2.name, da2.type, da2.required)))");
        entityQuery.addParameter("datasetId", datasetId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Dataset> getAttributeEquivalentDatasetsByDatasetTemplateId(Long datasetTemplateId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("(entity.datasetTemplate is null or entity.datasetTemplate.id <> :datasetTemplateId) and (SELECT COUNT(*) FROM DatasetAttribute WHERE dataset.id = entity.id) = (SELECT COUNT(*) FROM DatasetTemplateAttribute WHERE datasetTemplate.id = :datasetTemplateId) AND NOT EXISTS(SELECT id FROM DatasetAttribute da1 WHERE dataset.id = entity.id AND NOT EXISTS (SELECT id FROM DatasetTemplateAttribute da2 WHERE datasetTemplate.id = :datasetTemplateId AND (da1.position, da1.name, da1.type, da1.required) = (da2.position, da2.name, da2.type, da2.required)))");
        entityQuery.addParameter("datasetTemplateId", datasetTemplateId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Dataset> getDatasetsByAttributeTypeAndFieldValue(String attributeType, String fieldValue) {
        return createNamedQuery("Dataset.findByAttributeTypeAndFieldValue").setParameter("attributeType", attributeType).setParameter("fieldValue", fieldValue).getResultList();
    }

    public List<Dataset> getDatasetsByEntity(@NotNull AbstractEntity entity) {
        return createNamedQuery("Dataset.findByEntityReference").setParameter("attributeType", entity.getTrimmedClassName()).setParameter("fieldValue", entity.getIdString()).getResultList();
    }

    public List<Dataset> getDatasetsFilteredByContainersExcluding(String filterString, Collection<Container> containers, Collection<Dataset> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        if (containers != null && !containers.isEmpty()) {
            entityQuery.addWhereClause("container in (:containers)");
            entityQuery.addParameter("containers", containers);
        }
        return (List<Dataset>) entityQuery.getResultList();
    }

    public List<DatasetField> getFieldsByItemIdOrderByPosition(long datasetItemId) {
        return createQuery("Select f from DatasetField f join DatasetAttribute a on (f.attribute.id = a.id) where f.item.id = :itemId order by a.position").setParameter("itemId", datasetItemId)
            .getResultList();
    }

    public Map<Long, List<DatasetField>> getFieldsByItemIdsOrderByPosition(List<Long> datasetItemIds) {
        if (datasetItemIds == null || datasetItemIds.isEmpty()) {
            return new HashMap<>();
        }
        List<DatasetField> allFields = createQuery("select f from DatasetField f join DatasetAttribute a on (f.attribute.id = a.id) where f.item.id in :datasetItemIds order by f.item.id, a.position").setParameter("datasetItemIds", datasetItemIds)
            .getResultList();
        Map<Long, List<DatasetField>> fieldsByItemId = new HashMap<>();
        for (DatasetField field : allFields) {
            Long itemId = field.getItem().getId();
            fieldsByItemId.computeIfAbsent(itemId, k -> new ArrayList<>()).add(field);
        }
        return fieldsByItemId;
    }

    public BfabricLazyDataModel<Dataset> getLazyModelByUserId(long userId) {
        return (BfabricLazyDataModel<Dataset>) getLazyModelContainerDependentByUserId(userId);
    }

    public BfabricLazyDataModel<Dataset> getLazyModelSucceedingDatasetsByWorkunitId(long workunitId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.attributes attribute join attribute.fields field");
        entityQuery.addWhereClause("attribute.type = :workunitType and field.value = :workunitIdString or attribute.type = :resourceType and field.value IN (SELECT CAST(resource.id AS string) FROM Resource resource WHERE resource.workunit.id = :workunitId)");
        entityQuery.addParameter("resourceType", Resource.class.getSimpleName());
        entityQuery.addParameter("workunitType", Workunit.class.getSimpleName());
        entityQuery.addParameter("workunitId", workunitId);
        entityQuery.addParameter("workunitIdString", String.valueOf(workunitId));
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Dataset> getSucceedingDatasetsByWorkunitId(long workunitId) {
        List<String> resourceIds = createQuery("SELECT cast(id as string) from Resource where workunit.id = :workunitId").setParameter("workunitId", workunitId).getResultList();
        return createQuery("SELECT DISTINCT a.dataset FROM DatasetAttribute a JOIN a.fields field WHERE a.type = :workunitType and field.value = :workunitIdString or a.type = :resourceType and field.value in (:resourceIds)").setParameter("resourceType", Resource.class.getSimpleName())
            .setParameter("workunitType", Workunit.class.getSimpleName()).setParameter("workunitIdString", String.valueOf(workunitId)).setParameter("resourceIds", resourceIds).getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Dataset dataset = (Dataset) entity;
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();

        errorMsg.putAll(isValidCustomAttributes(dataset));

        if (dataset.getAttributes().isEmpty()) {
            errorMsg.put(null, Messages.get("validationErrorDatasetNoAttributes"));
            return errorMsg;
        }

        if (dataset.getItems().isEmpty()) {
            errorMsg.put(null, Messages.get("validationErrorDatasetNoItems"));
            return errorMsg;
        }

        // Check the attributes for unique names.
        List<DatasetAttribute> attributesToCheck = new ArrayList<>(dataset.getAttributes());
        for (DatasetAttribute attribute1 : dataset.getAttributes()) {
            for (DatasetAttribute attribute2 : attributesToCheck) {
                if (!attribute1.equals(attribute2) && attribute1.getName().equalsIgnoreCase(attribute2.getName())) {
                    errorMsg.put(null, Messages.get("validationErrorColumnsSameName") + ": " + attribute2.getName());
                    return errorMsg;
                }
            }
        }

        if (dataset.isDatasetTypeCheckEnabled()) {
            dataset.typeCheck();
            if (!dataset.getTypeInvalidFields().isEmpty()) {
                for (DatasetField field : dataset.getTypeInvalidFields()) {
                    errorMsg.put("edit:datasetItems:" + (field.getItem().getPosition() - 1) + ":field_" + (field.getAttribute().getPosition() - 1), Messages.get("invalid"));
                }
                return errorMsg;
            }
        }

        if (dataset.hasEmptyItems()) {
            errorMsg.put(null, Messages.get("validationErrorDatasetContentEmpty"));
            return errorMsg;
        }
        boolean valid = true;
        StringBuilder message = new StringBuilder();
        if (dataset.getItems().isEmpty()) {
            valid = false;
            message.append(Messages.get("item").toLowerCase());
        }
        if (dataset.getAttributes().isEmpty()) {
            valid = false;
            if (message.length() > 0) {
                message.append(" ").append(Messages.get("and").toLowerCase()).append(" ");
            }
            message.append(Messages.get("attribute").toLowerCase());
        }
        if (!valid) {
            message.append(" ").append(Messages.get("javax.faces.component.UIInput.REQUIRED"));
            errorMsg.put(null, Messages.get("validationErrorDatasetInvalidContent") + ": " + message);
        }
        if (dataset.getDatasetTemplate() != null && !dataset.isCompatibleWith(dataset.getDatasetTemplate())) {
            errorMsg.put(null, Messages.get("validationErrorDatasetIncompatibleContent") + " " + dataset.getDatasetTemplate().getId());
            return errorMsg;
        }
        return errorMsg;
    }

    public void save(Dataset dataset, ResourceBasket resourceBasket) {
        save(dataset);
        // Cleanup: Delete the temporary resource basket.
        if (resourceBasket != null) {
            remove(resourceBasket);
        }
    }

    public void save(Dataset dataset) {
        save(dataset, true);
    }

    public void save(Dataset dataset, boolean index) {
        if (dataset != null) {
            dataset.resetAttributePositions();
            dataset.resetItemPositions();
            dataset.removeDanglingFields();
            if (dataset.isManaged() && StringHelper.isNotEmpty(XmlHelper.getXmlLogDiff(dataset.getOldStateAsXml(), dataset.getXml()))) {
                dataset.createEntityLogUpdate();
            }
            super.save(dataset, index);
        }
    }
}