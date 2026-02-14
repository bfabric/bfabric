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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Storage;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class StorageService extends AbstractService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(StorageService.class.getName());

    public StorageService() {
        super(Storage.class);
    }

    public List<Storage> getEnabledStorages() {
        return createNamedQuery("Storage.findEnabled").getResultList();
    }

    public BfabricLazyDataModel<Storage> getReassignStorageSupervisorTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("enabled = TRUE and supervisor.empDegree IS NULL and supervisor.login <> 'admin'");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Storage getStorageByName(String name) {
        try {
            Storage storage = (Storage) createNamedQuery("Storage.findByName").setParameter("name", name).setMaxResults(1).getSingleResult();
            if (storage != null) {
                String localStorageAccessCheckResult = storage.getLocalStorageAccessCheckResult();
                if (StringHelper.isNotEmpty(localStorageAccessCheckResult)) {
                    logger.fine(localStorageAccessCheckResult);
                }
                return storage;
            }
        } catch (NoResultException nre) {
            logger.severe("Storage with name " + name + " does not exist");
        }
        return null;
    }

    public List<Object> getStorageInfoByContainerId(Long containerId) {
        return createNativeQuery("SELECT containerid, storageid, status, COUNT(*) AS resources, SUM(size) AS totalsize FROM resource WHERE containerId = :containerId GROUP BY containerid, storageid, status").setParameter("containerId", containerId)
            .getResultList();
    }

    public List<Storage> getStoragesByContainerId(Long containerId) {
        return createQuery("select distinct r.storage from Resource r WHERE r.container.id = :containerId").setParameter("containerId", containerId).getResultList();
    }

    public List<Storage> getSupervisedStorages(Long supervisorId, boolean hasRoleAdmin) {
        EntityQuery entityQuery = createEntityQuery();
        if (!hasRoleAdmin) {
            entityQuery.addWhereClause("supervisor.id = :supervisorId");
            entityQuery.addParameter("supervisorId", supervisorId);
        }
        entityQuery.setOrder("id");
        return (List<Storage>) entityQuery.getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((Storage) entity);
    }
}