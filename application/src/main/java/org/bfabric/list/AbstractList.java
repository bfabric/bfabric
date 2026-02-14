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

package org.bfabric.list;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEnabledBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Container;
import org.bfabric.entity.EntityLog;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.manager.ConfManager;
import org.bfabric.manager.IdentityManager;
import org.bfabric.service.AbstractService;
import org.bfabric.service.util.BfabricLazyDataModel;

public abstract class AbstractList<T extends AbstractEntity> implements Serializable {

    private static final long serialVersionUID = 1;

    @Inject
    protected IdentityManager identityManager;

    private ConfManager confManager;

    private Configuration configuration;

    public AbstractList() {
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

    @CachedMethodResult
    public List<?> getEnabledIncluding(Long id) {
        return getService().getEnabledIncluding(id);
    }

    @CachedMethodResult
    public List<?> getEnabledIncludingOrderByPosition(Long id) {
        return getService().getEnabledIncludingOrderByPosition(id);
    }

    @CachedMethodResult
    public List<?> getEnabledOrderByPosition() {
        return getService().getEnabledOrderByPosition();
    }

    @CachedMethodResult
    public List<EntityLog> getLastEntityLogs(AbstractBaseEntity entity, Integer maxResult) {
        return getService().getLastEntityLogs(entity, maxResult);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModel() {
        return (BfabricLazyDataModel<T>) getService().getLazyModel();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByChargerId(long userId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByChargerId(userId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByContainerBudgetOfficerId(long userId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByContainerBudgetOfficerId(userId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByContainerId(long containerId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByContainerId(containerId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByContainerId(long containerId, Collection<? extends Container> associatedContainers, boolean all) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByContainerId(containerId, associatedContainers, all);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<?> getLazyModelByContainerIdJoined(long containerId) {
        return getService().getLazyModelByContainerIdJoined(containerId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByContainerServiceAreaId(long serviceTypeId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByContainerServiceAreaId(serviceTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByContainerServiceTypeId(long serviceTypeId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByContainerServiceTypeId(serviceTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByContainers(Collection<? extends Container> containers) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByContainers(containers);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByFeedbackContainerServiceAreaId(long serviceTypeId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByFeedbackContainerServiceAreaId(serviceTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByFeedbackContainerServiceTypeId(long serviceTypeId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByFeedbackContainerServiceTypeId(serviceTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByInstrumentId(long instrumentId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByInstrumentId(instrumentId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByServiceAreaId(long serviceAreaId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByServiceAreaId(serviceAreaId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByServiceCodeId(long serviceCodeId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByServiceCodeId(serviceCodeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByServiceId(long serviceId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByServiceId(serviceId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByServiceIdParents(long serviceId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByServiceIdParents(serviceId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByServiceTypeId(long serviceTypeId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByServiceTypeId(serviceTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByTaxTypeId(long taxTypeId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByTaxTypeId(taxTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelByUserId(long userId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelByUserId(userId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelContainerDependentByContainerIds(Set<Long> containerIds) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelContainerDependentByContainerIds(containerIds);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<T> getLazyModelContainerDependentByUserId(long userId) {
        return (BfabricLazyDataModel<T>) getService().getLazyModelContainerDependentByUserId(userId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<?> getLazyModelReferencesEntity(AbstractEntity entity) {
        return getService().getLazyModelReferencesEntity(entity);
    }

    @CachedMethodResult
    public List<T> getResultList() {
        return (List<T>) getService().getResultList();
    }

    @CachedMethodResult
    public List<T> getResultListByContainerId(long containerId, Collection<? extends Container> associatedContainers, boolean all) {
        return (List<T>) getService().getResultListByContainerId(containerId, associatedContainers, all);
    }

    @CachedMethodResult
    public List<T> getResultListEnabled() {
        return (List<T>) getService().getResultListEnabled();
    }

    @CachedMethodResult
    public List<T> getResultListEnabledIncluding(long entityId) {
        return (List<T>) getService().getResultListEnabledIncluding(entityId);
    }

    @CachedMethodResult
    public List<T> getResultListEnabledIncludingByInstrumentId(long instrumentId, long entityId) {
        return (List<T>) getService().getResultListEnabledIncludingByInstrumentIdOrderBy(instrumentId, entityId, null);
    }

    @CachedMethodResult
    public List<T> getResultListEnabledIncludingByInstrumentIdOrderBy(long instrumentId, long entityId, String orderClause) {
        return (List<T>) getService().getResultListEnabledIncludingByInstrumentIdOrderBy(instrumentId, entityId, orderClause);
    }

    @CachedMethodResult
    public List<T> getResultListEnabledIncludingMultiple(Collection<? extends AbstractEnabledBaseEntity> entities) {
        if (!entities.isEmpty()) {
            return (List<T>) getService().getResultListEnabledIncludingMultiple(entities);
        }
        return (List<T>) getService().getResultListEnabled();
    }

    @CachedMethodResult
    public List<T> getResultListEnabledIncludingOrderByEntityId(long entityId, String orderClause) {
        return (List<T>) getService().getResultListEnabledIncludingOrderByEntityId(entityId, orderClause);
    }

    @CachedMethodResult
    public List<T> getResultListEnabledOrderedByName() {
        return getResultListEnabledIncludingOrderByEntityId(0, "name");
    }

    @CachedMethodResult
    public List<T> getResultListOrderByCode() {
        return (List<T>) getService().getResultListOrderByCode();
    }

    @CachedMethodResult
    public List<T> getResultListOrderByName() {
        return (List<T>) getService().getResultListOrderByName();
    }

    @CachedMethodResult
    public List<?> getResultListOrderByPosition() {
        return getService().getResultListOrderByPosition();
    }

    protected abstract AbstractService getService();

    @CachedMethodResult
    public boolean hasCurrentUserRoleEnum(RoleEnum roleEnum) {
        return getConfiguration().hasCurrentUserRoleEnum(roleEnum);
    }
}
