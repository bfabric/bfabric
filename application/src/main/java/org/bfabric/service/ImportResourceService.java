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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Application;
import org.bfabric.entity.Container;
import org.bfabric.entity.ImportResource;
import org.bfabric.entity.User;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class ImportResourceService extends AbstractService {

    private static final long serialVersionUID = 1;

    public ImportResourceService() {
        super(ImportResource.class);
    }

    public BfabricLazyDataModel<ImportResource> getAvailableLinkImportResourcesByApplicationAndUser(Application application, User user, boolean linkImport, boolean hasContainerReaderRole) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("application = :application");
        entityQuery.addParameter("application", application);
        if (StringHelper.isNotEmpty(application.getResourceRelativePathFilter())) {
            entityQuery.addWhereClause("relativePath LIKE :resourceRelativePathFilter");
            entityQuery.addParameter("resourceRelativePathFilter", application.getResourceRelativePathFilter());
        }
        if (linkImport) {
            entityQuery.addWhereClause("storage IS NOT NULL");
        }
        if (!hasContainerReaderRole) {
            if (!user.getContainersTransitive().isEmpty()) {
                entityQuery.addWhereClause("container IN :containers");
                entityQuery.addParameter("containers", user.getContainersTransitive());
            } else {
                return new BfabricLazyDataModel<>();
            }
        }
        entityQuery.addWhereClause(" expirationDate IS NULL OR expirationDate > :currentDate ");
        entityQuery.addParameter("currentDate", LocalDateTime.now());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public ImportResource getEquivalentImportResource(ImportResource importResource) {
        List<ImportResource> importResources = getEquivalentImportResources(importResource);
        return !importResources.isEmpty() ? importResources.get(0) : null;
    }

    private List<ImportResource> getEquivalentImportResources(ImportResource importResource) {
        return createNamedQuery("ImportResource.findEquivalent").setParameter("id", importResource.getId()).setParameter("application", importResource.getApplication()).setParameter("container",
            importResource.getContainer()).setParameter("relativePath", importResource.getRelativePath()).setParameter("storage", importResource.getStorage()).setParameter("url", importResource
            .getUrl()).getResultList();
    }

    public BfabricLazyDataModel<ImportResource> getImportResourcesLazyModelByApplication(Application application) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("application = :application");
        entityQuery.addParameter("application", application);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<ImportResource> getLazyModelByStorageId(long storageId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("storage.id = :storageId");
        entityQuery.addParameter("storageId", storageId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<ImportResource> getLazyModelByWorkunitId(long workunitId) {
        return (BfabricLazyDataModel<ImportResource>) getLazyModelUnnestById("workunits", workunitId);
    }

    public ImportResource getOverwrittenEquivalentImportResourceIfExists(ImportResource importResource) {
        ImportResource equivalentImportResource = getEquivalentImportResource(importResource);
        if (equivalentImportResource != null) {
            equivalentImportResource.setName(importResource.getName());
            equivalentImportResource.setDescription(importResource.getDescription());
            equivalentImportResource.setExpirationDate(importResource.getExpirationDate());
            equivalentImportResource.setFileChecksum(importResource.getFileChecksum());
            equivalentImportResource.setFileDate(importResource.getFileDate());
            equivalentImportResource.setSize(importResource.getSize());
            equivalentImportResource.setReport(importResource.getReport());
            return equivalentImportResource;
        }
        return importResource;
    }

    public boolean hasNonExpiredImportResourcesByContainerAndApplication(Container container, Application application) {
        return ((Long) createNamedQuery("ImportResource.findNonExpiredByContainerAndApplication").setParameter("application", application).setParameter("container", container).setParameter(
            "currentDate", LocalDateTime.now()).getSingleResult()).intValue() > 0;
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final ImportResource importResource = (ImportResource) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (!(StringHelper.isNotEmpty(importResource.getUrl()) && StringHelper.isEmpty(importResource.getRelativePath()) && importResource.getStorage() == null || StringHelper
            .isEmpty(importResource.getUrl()) && StringHelper.isNotEmpty(importResource.getRelativePath()) && importResource.getStorage() != null)) {
            validationErrorMsg.put(null, Messages.get("pathOrURLMustBeSpecified"));
        }
        if (importResource.getId() != 0) {
            ImportResource equivalentImportResource = getEquivalentImportResource(importResource);
            if (equivalentImportResource != null) {
                validationErrorMsg.put(null, Messages.get("importResourceAlreadyExists").replace("{0}", equivalentImportResource.getApplication().getIdString())
                    .replace("{1}", equivalentImportResource.getContainer().getIdString()));
            }
        }
        return validationErrorMsg;
    }
}