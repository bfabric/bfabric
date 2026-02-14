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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEnabledBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.AccessRequest;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Container;
import org.bfabric.entity.Currency;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Role;
import org.bfabric.entity.User;
import org.bfabric.entity.api.CustomAttributes;
import org.bfabric.enums.RoleEnum;
import org.bfabric.manager.ConfManager;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;

public abstract class AbstractService extends StatelessEJB {

    private static final long serialVersionUID = 1;

    private ConfManager confManager;

    private Configuration configuration;

    private String entityClassName;

    public AbstractService() {
    }

    public AbstractService(Class<?> clazz) {
        entityClassName = clazz != null ? clazz.getSimpleName() : null;
    }

    public void addFilterString(Query query, String filterString) {
        if (query != null) {
            query.setParameter("filterString", "%" + (StringHelper.isNotEmpty(filterString) ? filterString.trim().toLowerCase() : Constants.EMPTY_STRING) + "%");
        }
    }

    public boolean checkUniqueAttributeValue(Class<?> clazz, String attributeName, String attributeValue) {
        return createQuery("from " + ClassHelper.getTrimmedClassName(clazz) + " where lower(" + attributeName + ") = lower(:" + attributeName + ")")
            .setParameter(attributeName, attributeValue).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean checkUniqueAttributeValue(AbstractEntity entity, String attributeName, String attributeValue) {
        return createQuery("from " + entity.getTrimmedClassName() + " where lower(" + attributeName + ") = lower(:" + attributeName + ") and id <> :id").setParameter(attributeName, attributeValue)
            .setParameter("id", entity.getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        return checkUniqueAttributeValue(entity, "name", entity.getName());
    }

    public Map<String, Set<String>> createDisplayFacesMessagesMap(String facesMessage) {
        final Map<String, Set<String>> facesMessagesMap = createFacesMessagesMap();
        facesMessagesMap.get(Constants.DISPLAY_MESSAGES).add(facesMessage);
        return facesMessagesMap;
    }

    public EntityQuery createEntityQuery() {
        return createEntityQuery(getEntityClassName());
    }

    public EntityQuery createEntityQuery(Class<?> clazz) {
        return createEntityQuery(clazz.getSimpleName());
    }

    public EntityQuery createEntityQuery(String className) {
        return new EntityQuery(ClassHelper.getClassByName(className).getSimpleName(), getEntityManager());
    }

    public EntityQuery createEntityQueryByContainerId(long containerId, Collection<? extends Container> associatedContainers, boolean all) {
        final EntityQuery entityQuery = createEntityQuery(getEntityClassName());
        if (containerId > 0) {
            entityQuery.addWhereClause("entity.container.id = :containerId");
            entityQuery.addParameter("containerId", containerId);
        }
        if (all && associatedContainers != null && !associatedContainers.isEmpty()) {
            entityQuery.addWhereClauseDisjunctive("entity.container IN :associatedContainers");
            entityQuery.addParameter("associatedContainers", associatedContainers);
        }
        entityQuery.setOrder("entity.modified DESC");
        return entityQuery;
    }

    public EntityQuery createEntityQueryByContainerIdJoined(long containerId) {
        final EntityQuery entityQuery = createEntityQuery(getEntityClassName());
        entityQuery.addUnnestWhereClause("containers", null, containerId);
        entityQuery.setOrder("entity.id DESC");
        return entityQuery;
    }

    public EntityQuery createEntityQueryFiltered(String filterString) {
        return new EntityQuery(getEntityClassName(), filterString, getEntityManager());
    }

    public EntityQuery createEntityQueryFiltered(String filterString, String filterPathName) {
        return new EntityQuery(getEntityClassName(), filterString, filterPathName, getEntityManager());
    }

    public String createFacesMessagesForCreatedAndDeletedItems(String message, int created, int deleted) {
        StringBuilder msg = new StringBuilder();
        if (StringHelper.isNotEmpty(message)) {
            msg.append(message).append(": ");
            if (deleted > 0) {
                msg.append(deleted).append(" ").append(Messages.get("deleted").toLowerCase()).append(" ");
            }
            if (created > 0) {
                msg.append(created).append(" ").append(Messages.get("created").toLowerCase()).append(" ");
            }
        }
        return msg.toString();
    }

    public Map<String, Set<String>> createFacesMessagesMap() {
        final Map<String, Set<String>> facesMessagesMap = new HashMap<>();
        facesMessagesMap.put(Constants.ERROR_MESSAGES, new HashSet<>());
        facesMessagesMap.put(Constants.DISPLAY_MESSAGES, new HashSet<>());
        return facesMessagesMap;
    }

    public List<?> getAllEnabledInternalOrderByName() {
        return createNamedQuery(getEntityClassName() + ".findAllEnabledInternalOrderByName").getResultList();
    }

    public ConfManager getConfManager() {
        if (confManager == null) {
            confManager = CDI.current().select(ConfManager.class).get();
        }
        return confManager;
    }

    public Configuration getConfiguration() {
        if (configuration == null && getConfManager() != null) {
            configuration = getConfManager().getConfiguration();
        }
        return configuration;
    }

    public Currency getDefaultCurrency() {
        return (Currency) createNamedQuery("Currency.findByCode").setParameter("code", getConfiguration().getDefaultCurrencyCode()).setMaxResults(1).getSingleResult();
    }

    public List<?> getEnabledIncluding(Long id) {
        return createQuery("from " + getEntityClassName() + " where (id = :id or enabled = true) order by id").setParameter("id", id).getResultList();
    }

    public List<?> getEnabledIncludingOrderByPosition(Long id) {
        return createQuery("from " + getEntityClassName() + " where (id = :id or enabled = true) order by orderPosition").setParameter("id", id).getResultList();
    }

    public List<?> getEnabledOrderByPosition() {
        return createQuery("from " + getEntityClassName() + " where enabled = true order by orderPosition").getResultList();
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public List<?> getFiltered(String filterString) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.setMaxResult(100);
        return entityQuery.getResultList();
    }

    public List<?> getFilteredEnabledExcludingOrderBy(Collection<? extends AbstractEnabledBaseEntity> entities, String filterString, String orderClause) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("enabled = true");
        if (entities != null && !entities.isEmpty()) {
            entityQuery.addWhereClause("entity NOT IN (:entities)");
            entityQuery.addParameter("entities", entities);
        }
        entityQuery.setOrder(StringHelper.isNotEmpty(orderClause) ? orderClause : "id DESC");
        return entityQuery.getResultList();
    }

    public List<?> getFilteredEnabledIncludingOrderBy(AbstractEnabledBaseEntity entity, String filterString, String orderClause) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("enabled = true or entity = :entity");
        entityQuery.addParameter("entity", entity);
        entityQuery.setOrder(StringHelper.isNotEmpty(orderClause) ? orderClause : "id DESC");
        return entityQuery.getResultList();
    }

    public List<?> getFilteredEnabledOrderedByName(String filterString) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("enabled = true");
        entityQuery.setMaxResult(100);
        entityQuery.setOrder("name");
        return entityQuery.getResultList();
    }

    public List<?> getFilteredExcluded(String filterString, Set<? extends AbstractNamedBaseEntity> exclude) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(exclude);
        entityQuery.setOrder("name");
        return entityQuery.getResultList();
    }

    public List<?> getFilteredOrderedByName(String filterString) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.setMaxResult(100);
        entityQuery.setOrder("name");
        return entityQuery.getResultList();
    }

    public List<EntityLog> getLastEntityLogs(AbstractBaseEntity entity, Integer maxResult) {
        EntityQuery entityQuery = createEntityQuery(EntityLog.class);
        entityQuery.addWhereClause("(entityId = :entityId AND entityClassName = :entityClassName) OR (parentEntityId = :entityId AND parentEntityClassName = :entityClassName)");
        entityQuery.addParameter("entityId", entity.getId());
        entityQuery.addParameter("entityClassName", entity.getTrimmedClassName());
        entityQuery.setMaxResult(maxResult != null ? maxResult : 10);
        entityQuery.setOrder("created desc");
        return (List<EntityLog>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<?> getLazyModel() {
        return getLazyModelByClassName(getEntityClassName());
    }

    public BfabricLazyDataModel<?> getLazyModelByAccessProtocolId(long accessProtocolId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("accessProtocol.id = :accessProtocolId");
        entityQuery.addParameter("accessProtocolId", accessProtocolId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<AccessRequest> getLazyModelByAccessRequestTypeId(long accessRequestTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("accessRequestType.id = :accessRequestTypeId");
        entityQuery.addParameter("accessRequestTypeId", accessRequestTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByAccessTypeId(long accessTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("accessType.id = :accessTypeId");
        entityQuery.addParameter("accessTypeId", accessTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByApplicationCategoryId(long applicationCategoryId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("applicationCategory.id = :applicationCategoryId");
        entityQuery.addParameter("applicationCategoryId", applicationCategoryId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByApplicationTypeId(long applicationTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("applicationType.id = :applicationTypeId");
        entityQuery.addParameter("applicationTypeId", applicationTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByChargerId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("charger.id = :userId");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByClassName(String className) {
        return new BfabricLazyDataModel<>(new EntityQuery(className, getEntityManager()));
    }

    public BfabricLazyDataModel<?> getLazyModelByContainerBudgetOfficerId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("container.budgetOfficer.id = :userId");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByContainerId(long containerId, Collection<? extends Container> associatedContainers, boolean all) {
        return new BfabricLazyDataModel<>(createEntityQueryByContainerId(containerId, associatedContainers, all));
    }

    public BfabricLazyDataModel<?> getLazyModelByContainerId(long containerId) {
        return getLazyModelByContainerId(containerId, null, false);
    }

    public BfabricLazyDataModel<?> getLazyModelByContainerIdJoined(long containerId) {
        return new BfabricLazyDataModel<>(createEntityQueryByContainerIdJoined(containerId));
    }

    public BfabricLazyDataModel<?> getLazyModelByContainerServiceAreaId(long serviceAreaId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("container.serviceType.serviceArea.id = :serviceAreaId");
        entityQuery.addParameter("serviceAreaId", serviceAreaId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByContainerServiceTypeId(long serviceTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("container.serviceType.id = :serviceTypeId");
        entityQuery.addParameter("serviceTypeId", serviceTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByContainers(Collection<? extends Container> containers) {
        return getLazyModelByContainerId(0, containers, true);
    }

    public BfabricLazyDataModel<?> getLazyModelByFeedbackContainerServiceAreaId(long serviceAreaId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("feedback.container.serviceType.serviceArea.id = :serviceAreaId");
        entityQuery.addParameter("serviceAreaId", serviceAreaId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByFeedbackContainerServiceTypeId(long serviceTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("feedback.container.serviceType.id = :serviceTypeId");
        entityQuery.addParameter("serviceTypeId", serviceTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByFeedbackQuestionTypeId(long feedbackQuestionTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("feedbackQuestionType.id = :feedbackQuestionTypeId");
        entityQuery.addParameter("feedbackQuestionTypeId", feedbackQuestionTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByFeedbackTemplateTypeId(long feedbackTemplateTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("feedbackTemplateType.id = :feedbackTemplateTypeId");
        entityQuery.addParameter("feedbackTemplateTypeId", feedbackTemplateTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByInstrumentId(long instrumentId) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("instrument.id = :instrumentId");
        entityQuery.addParameter("instrumentId", instrumentId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<InstrumentReservation> getLazyModelByInstrumentReservationTypeId(long instrumentReservationTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("instrumentReservationType.id = :instrumentReservationTypeId");
        entityQuery.addParameter("instrumentReservationTypeId", instrumentReservationTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByPageflowId(long pageflowId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("pageflow.id = :pageflowId");
        entityQuery.addParameter("pageflowId", pageflowId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByServiceAreaId(long serviceAreaId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("serviceArea.id = :serviceAreaId");
        entityQuery.addParameter("serviceAreaId", serviceAreaId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByServiceCodeId(long serviceCodeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("serviceCode.id = :serviceCodeId");
        entityQuery.addParameter("serviceCodeId", serviceCodeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByServiceId(long serviceId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("service.id = :serviceId");
        entityQuery.addParameter("serviceId", serviceId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByServiceIdParents(long serviceId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addUnnestWhereClause("children", null, serviceId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByServiceTypeId(long serviceTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("serviceType.id = :serviceTypeId");
        entityQuery.addParameter("serviceTypeId", serviceTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Container> getLazyModelByStorageModelId(long storageModelId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("storageModel.id = :storageModelId");
        entityQuery.addParameter("storageModelId", storageModelId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Charge> getLazyModelByTaxTypeId(long taxTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("taxType.id = :taxTypeId");
        entityQuery.addParameter("taxTypeId", taxTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByUserId(long userId) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("user.id = :userId");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<? extends AbstractEntity> getLazyModelByWidgetTypeId(long widgetTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("widgetType.id = :widgetTypeId");
        entityQuery.addParameter("widgetTypeId", widgetTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelContainerDependentByContainerIds(Set<Long> containerIds) {
        if (containerIds != null && !containerIds.isEmpty()) {
            final EntityQuery entityQuery = createEntityQuery(getEntityClassName());
            entityQuery.addWhereClause("container.id in (:containerIds)");
            entityQuery.addParameter("containerIds", containerIds);
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return new BfabricLazyDataModel<>();
    }

    public BfabricLazyDataModel<?> getLazyModelContainerDependentByUserId(long userId) {
        final EntityQuery entityQuery = createEntityQuery(getEntityClassName());
        entityQuery.addWhereClause("EXISTS(SELECT membership.id FROM Membership membership WHERE membership.user.id = :userId AND (membership.container = entity.container or membership.container = entity.container.project) and discriminator = org.bfabric.entity.Membership.DISCRIMINATOR_CURRENT)");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelReferencesEntity(AbstractEntity entity) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause(entity.getClassNameFirstLowerCase() + ".id = :id");
        entityQuery.addParameter("id", entity.getId());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelUnnestById(String nestedColumn, String idPath, long id, String orderBy) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addUnnestWhereClause(nestedColumn, idPath, id);
        if (StringHelper.isNotEmpty(orderBy)) {
            entityQuery.setOrder(orderBy);
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelUnnestById(String nestedColumn, String idPath, long id) {
        return getLazyModelUnnestById(nestedColumn, idPath, id, null);
    }

    public BfabricLazyDataModel<?> getLazyModelUnnestById(String nestedColumn, long id) {
        return getLazyModelUnnestById(nestedColumn, null, id, null);
    }

    public Long getNextSampleNameSuffix(Long containerId, String prefix) {
        return containerId != null && prefix != null ? Long.valueOf(String.valueOf(createNativeQuery("SELECT nextsamplenamesuffix('" + containerId + "', '" + prefix + "')").getSingleResult())) : null;
    }

    public Long getNextTubeIdSuffix(Long containerId) {
        return containerId != null ? Long.valueOf(String.valueOf(createNativeQuery("SELECT nexttubeidsuffix('" + containerId + "')").getSingleResult())) : null;
    }

    public List<Object> getResult(String query) {
        return createNativeQuery(query).getResultList();
    }

    public List<?> getResultList() {
        return getResultListByClassName(getEntityClassName());
    }

    public List<?> getResultListByClassName(String className) {
        return createEntityQuery(className).getResultList();
    }

    public List<?> getResultListByContainerId(long containerId, Collection<? extends Container> associatedContainers, boolean all) {
        return createEntityQueryByContainerId(containerId, associatedContainers, all).getResultList();
    }

    public List<?> getResultListEnabled() {
        return getResultListEnabledIncludingOrderByEntityId(0, null);
    }

    public List<?> getResultListEnabledIncluding(long entityId) {
        return getResultListEnabledIncludingOrderByEntityId(entityId, null);
    }

    public List<?> getResultListEnabledIncludingByInstrumentId(long instrumentId, long entityId) {
        return getResultListEnabledIncludingByInstrumentIdOrderBy(instrumentId, entityId, null);
    }

    public List<?> getResultListEnabledIncludingByInstrumentIdAndServiceTypeDisabled(long instrumentId, long entityId, long serviceTypeDisabledId) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("id = :entityId or enabled = true and instrument.id = :instrumentId and NOT EXISTS(SELECT std FROM entity.serviceTypesDisabled std WHERE std.id = :serviceTypeDisabledId)");
        entityQuery.addParameter("instrumentId", instrumentId);
        entityQuery.addParameter("entityId", entityId);
        entityQuery.addParameter("serviceTypeDisabledId", serviceTypeDisabledId);
        entityQuery.setOrder("orderPosition");
        return entityQuery.getResultList();
    }

    public List<?> getResultListEnabledIncludingByInstrumentIdOrderBy(long instrumentId, long entityId, String orderClause) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("enabled = true and (instrument.id = :instrumentId or instrument is null and :instrumentId = 0) or id = :entityId");
        entityQuery.addParameter("instrumentId", instrumentId);
        entityQuery.addParameter("entityId", entityId);
        entityQuery.setOrder(StringHelper.isNotEmpty(orderClause) ? orderClause : "id DESC");
        return entityQuery.getResultList();
    }

    public List<?> getResultListEnabledIncludingMultiple(Collection<? extends AbstractEnabledBaseEntity> entities) {
        return createNamedQuery(getEntityClassName() + ".findEnabledIncludingMultiple").setParameter("entities", entities).getResultList();
    }

    public List<?> getResultListEnabledIncludingOrderByEntityId(long id, String orderClause) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("enabled = true or id = :id");
        entityQuery.addParameter("id", id);
        entityQuery.setOrder(StringHelper.isNotEmpty(orderClause) ? orderClause : "id DESC");
        return entityQuery.getResultList();
    }

    public List<?> getResultListEnabledOrderedByName() {
        return getResultListEnabledIncludingOrderByEntityId(0, "name");
    }

    public List<?> getResultListOrderByCode() {
        return createQuery("from " + getEntityClassName() + " order by code").getResultList();
    }

    public List<?> getResultListOrderByName() {
        return createQuery("from " + getEntityClassName() + " order by name").getResultList();
    }

    public List<?> getResultListOrderByPosition() {
        return createQuery("from " + getEntityClassName() + " order by orderPosition").getResultList();
    }

    public Role getRoleByRoleEnum(@NotNull RoleEnum roleEnum) {
        return (Role) createNamedQuery("Role.findByName").setParameter("name", roleEnum.getName()).setMaxResults(1).getSingleResult();
    }

    public String getTubeIdPadded(String tubeId) {
        return StringHelper.isNotEmpty(tubeId) ? (String) createNativeQuery("SELECT CAST(padtubeid('" + tubeId + "') AS TEXT)").getSingleResult() : null;
    }

    public User getUserByEmail(String email) {
        if (StringHelper.isNotEmpty(email)) {
            final List<User> users = createNamedQuery("User.findByEmail").setParameter("email", email).setMaxResults(1).getResultList();
            if (!users.isEmpty()) {
                return users.get(0);
            }
        }
        return null;
    }

    @SuppressWarnings("SameReturnValue")
    public String hello() {
        return "hello";
    }

    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return new LinkedHashMap<>();
    }

    public LinkedHashMap<String, String> isValidCustomAttributes(CustomAttributes entity) {
        final LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();

        if (entity != null && entity.getCustomAttributes() != null && !entity.getCustomAttributes().isEmpty()) {
            final CustomAttribute[] customAttributeList = new CustomAttribute[entity.getCustomAttributes().size()];
            entity.getCustomAttributes().toArray(customAttributeList);
            for (int i = 0; i < customAttributeList.length; i++) {
                if (StringHelper.isEmpty(customAttributeList[i].getValue())) {
                    errorMsg.put(Constants.EDIT + ":customAttribute:" + i + ":attributeValue", Messages.get("valueMustBeSet"));
                }

                final String attributeName = customAttributeList[i].getName();
                if (StringHelper.isEmpty(attributeName)) {
                    errorMsg.put(Constants.EDIT + ":customAttribute:" + i + ":attributeName", Messages.get("nameMustBeSet"));
                } else {
                    for (final Field field : FieldUtils.getAllFields(entity.getClass())) {
                        if (attributeName.replaceAll("\\s", Constants.EMPTY_STRING).equalsIgnoreCase(field.getName().replaceAll("\\s", Constants.EMPTY_STRING))) {
                            errorMsg.put(Constants.EDIT + ":customAttribute:" + i + ":attributeName", Messages.get("nameReservedKeyword").replace("{0}", field.getName()));
                            break;
                        }
                    }
                    for (int j = i + 1; j < customAttributeList.length; j++) {
                        if (StringHelper.isNotEmpty(customAttributeList[j].getName()) && attributeName.equals(customAttributeList[j].getName())) {
                            errorMsg.put(Constants.EDIT + ":customAttribute:" + j + ":attributeName", Messages.get("nameNotUniqueWithinEntityException"));
                            break;
                        }
                    }
                }
            }
        }

        return errorMsg;
    }

    public LinkedHashMap<String, String> isValidName(AbstractNamedBaseEntity entity) {
        return isValidName(entity, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueException"));
    }

    public LinkedHashMap<String, String> isValidName(AbstractNamedBaseEntity entity, String componentId, String errorMessage) {
        final LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();
        if (!checkUniqueName(entity)) {
            errorMsg.put(componentId, errorMessage);
        }
        return errorMsg;
    }

    public void saveMerge(AbstractEntity entity, AbstractEntity merged) {
        // Important: Re-read the entity to avoid that some already deleted associations are re-merged.
        remove(find(merged.getClass(), merged.getId()));
        // Important: Flush required to enforce Hibernate to adhere to the desired execution order (else Hibernate determines the execution order on its own).
        flush();
        merge(entity);
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }
}