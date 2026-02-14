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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Application;
import org.bfabric.entity.Container;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Storage;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.DatabaseQuery;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class ResourceService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    @Inject
    WorkunitService workunitService;

    public ResourceService() {
        super(Resource.class);
    }

    public boolean checkUnique(Resource resource) {
        return createNamedQuery("Resource.checkUnique").setParameter("id", resource.getId()).setParameter("storageId", resource.getStorage().getId())
            .setParameter("relativePath", resource.getRelativePath()).setParameter("workunitId", resource.getWorkunit().getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean checkUniqueImport(String relativePath, long storageId, long containerId, long applicationId) {
        return createNamedQuery("Resource.checkUniqueImport").setParameter("relativePath", relativePath).setParameter("storageId", storageId).setParameter("containerId", containerId)
            .setParameter("applicationId", applicationId).setMaxResults(1).getResultList().isEmpty();
    }

    public void deleteLocalImportResources() {
        createNativeQuery("DELETE FROM importresource WHERE storageid = (SELECT id FROM storage WHERE name = 'Local Temporary Storage') AND created < CURRENT_TIMESTAMP - INTERVAL '1 week'").executeUpdate();
        createNativeQuery("DELETE FROM workunitinput WHERE resourceid IN (SELECT id FROM resource WHERE storageid = (select id from storage where name = 'SlurmLog')) AND workunitid IN (SELECT workunitid FROM workunitinput WHERE resourceid NOT IN (SELECT id FROM resource WHERE storageid = (select id from storage where name = 'SlurmLog')))").executeUpdate();
        createNativeQuery("DELETE FROM resource WHERE storageid = (select id from storage where name = 'SlurmLog')").executeUpdate();
    }

    public BfabricLazyDataModel<Resource> getAvailableResourcesByApplicationAndUser(Application application, User user, boolean insideBasket, boolean hasContainerReaderRole) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("workunit.status = :available");
        entityQuery.addParameter("available", WorkunitStatusEnum.AVAILABLE);
        if (application != null && StringHelper.isNotEmpty(application.getResourceRelativePathFilter())) {
            entityQuery.addWhereClause("lower(relativePath) LIKE :resourceRelativePathFilter");
            entityQuery.addParameter("resourceRelativePathFilter", application.getResourceRelativePathFilter().toLowerCase());
        }
        if (application != null && !application.getPrecedingApplications().isEmpty()) {
            entityQuery.addWhereClause("workunit.application IN (:applications)");
            entityQuery.addParameter("applications", application.getPrecedingApplications());
        } else {
            return new BfabricLazyDataModel<>();
        }
        if (!hasContainerReaderRole) {
            if (!user.getContainersTransitive().isEmpty()) {
                entityQuery.addWhereClause("container IN (:containers)");
                entityQuery.addParameter("containers", user.getContainersTransitive());
            } else {
                return new BfabricLazyDataModel<>();
            }
        }
        if (insideBasket) {
            if (!user.getSelectedResourceBasket().getResources().isEmpty()) {
                entityQuery.addWhereClause("entity IN (:basketResources)");
                entityQuery.addParameter("basketResources", user.getSelectedResourceBasket().getResources());
            } else {
                return new BfabricLazyDataModel<>();
            }
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Resource> getLazyModelByApplicationId(long applicationId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("workunit.application.id = :applicationId");
        entityQuery.addParameter("applicationId", applicationId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Resource> getLazyModelByStorageId(long storageId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("storage.id = :storageId");
        entityQuery.addParameter("storageId", storageId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByUserId(long userId) {
        return getLazyModelContainerDependentByUserId(userId);
    }

    public BfabricLazyDataModel<Resource> getLazyModelByWorkunitId(long workunitId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("workunit.id = :workunitId");
        entityQuery.addParameter("workunitId", workunitId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Object> getLazyModelResourceSampleViewByContainerId(Long containerId) {
        if (containerId != null) {
            DatabaseQuery resourceSampleViewQuery = new DatabaseQuery(getEntityManager());
            resourceSampleViewQuery.setNativeQueryString("select * from resourcesample WHERE container_id = " + containerId);
            return new BfabricLazyDataModel<>(resourceSampleViewQuery, true);
        }
        return new BfabricLazyDataModel<>();
    }

    public List<Resource> getResourcesByContainerAndRelativePathAndStorage(Container container, String relativePath, Storage storage) {
        return createNamedQuery("Resource.findByContainerAndRelativePathAndStorage").setParameter("container", container).setParameter("relativePath", relativePath).setParameter("storage", storage)
            .getResultList();
    }

    public BfabricLazyDataModel<Resource> getResourcesByIds(Set<Long> ids) {
        if (!ids.isEmpty()) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("id IN (:ids)");
            entityQuery.addParameter("ids", ids);
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return new BfabricLazyDataModel<>();
    }

    public List<Resource> getResourcesByWorkunitId(long workunitId) {
        return createNamedQuery("Resource.findByWorkunitId").setParameter("workunitId", workunitId).getResultList();
    }

    public BfabricLazyDataModel<Resource> getResourcesByWorkunitIds(Set<Long> workunitIds) {
        if (!workunitIds.isEmpty()) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("workunit.id IN (:ids)");
            entityQuery.addParameter("ids", workunitIds);
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return new BfabricLazyDataModel<>();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Resource resource = (Resource) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (StringHelper.isEmpty(resource.getRelativePath()) || resource.getStorage() == null) {
            validationErrorMsg.put(null, Messages.get("pathOrBase64MustBeSpecified"));
        } else if (resource.getWorkunit().getId() > 0 && !checkUnique(resource)) {
            validationErrorMsg.put(null, Messages.get("resourceAlreadyExists").replace("{0}", resource.getStorage().getIdString()).replace("{1}", resource
                .getRelativePath()).replace("{2}", resource.getWorkunit().getIdString()));
        }
        return validationErrorMsg;
    }

    public void junk(Resource resource) {
        if (resource != null && resource.getId() > 0) {
            if (resource.isJunk()) {
                resource.setJunk(false);
                resource.setJunkComment("");
            } else {
                resource.setJunk(true);
            }
            super.save(resource);
        }
    }

    public String resetArchiveExpirationDatePassed() {
        int countExpired = 0;
        int countAvailable = 0;
        Map<Workunit, Set<Resource>> workunitSetMap = new HashMap<>();
        List<Resource> resources = createNamedQuery("Resource.findByArchiveExpirationDatePassed").getResultList();
        if (!resources.isEmpty()) {
            for (Resource resource : resources) {
                if (resource.isArchiving()) {
                    resource.setStatus(ResourceStatusEnum.AVAILABLE);
                    countAvailable++;
                } else {
                    resource.setStatus(ResourceStatusEnum.EXPIRED);
                    countExpired++;
                }
                Set<Resource> resourceSet;
                if (workunitSetMap.containsKey(resource.getWorkunit())) {
                    resourceSet = workunitSetMap.get(resource.getWorkunit());
                } else {
                    resourceSet = new HashSet<>();
                }
                resourceSet.add(resource);
                workunitSetMap.put(resource.getWorkunit(), resourceSet);
            }
        }
        for (Map.Entry<Workunit, Set<Resource>> workunitSetEntry : workunitSetMap.entrySet()) {
            try {
                Workunit workunit = workunitSetEntry.getKey();
                for (Resource resource : workunitSetEntry.getValue()) {
                    super.update(resource);
                }
                if (workunit != null) {
                    workunit.indexDependents();
                    workunit.resetStatus();
                    super.save(workunit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (countExpired > 0 || countAvailable > 0) ? countExpired + " " + countAvailable : null;
    }

    public void save(Resource resource) {
        save(resource, true);
    }

    public void save(Resource resource, boolean index) {
        try {
            // Note: Do indexing only once when the status of the parent workunit is updated and indexed together with all its resources!
            boolean uploadedFile = resource.getUploadedFile() != null;
            super.save(resource, !uploadedFile && index);
            if (uploadedFile) {
                RepositoryHelper.createImport(resource);
                resource.setAvailable();
                super.save(resource, index);
            }
            if (resource.isStatusChanged()) {
                workunitService.updateStatus(resource.getWorkunit().getId(), false);
            } else if (resource.isSizeChanged()) {
                workunitService.indexWorkunit(resource.getWorkunit().getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }

    public void update(Resource resource) {
        super.save(resource);
    }
}