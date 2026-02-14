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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.ApplicationType;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Container;
import org.bfabric.entity.Executable;
import org.bfabric.entity.ExternalJob;
import org.bfabric.entity.ImportResource;
import org.bfabric.entity.Job;
import org.bfabric.entity.Resource;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.BfabricUploadedFile;

@Named
@Stateless
public class WorkunitService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(WorkunitService.class.getName());

    @Inject
    private JobService jobService;

    public WorkunitService() {
        super(Workunit.class);
    }

    public void assignSamples(Workunit workunit) {
        workunit.resetStatus();
        merge(workunit);
    }

    public String deleteEmptyWorkunits() {
        List<Workunit> emptyWorkunits = createQuery("FROM Workunit a WHERE a.created < :date AND NOT EXISTS(SELECT r.id FROM Resource r WHERE r.workunit.id = a.id) AND NOT EXISTS(SELECT d.id FROM Dataset d WHERE d.workunit.id = a.id)  AND NOT EXISTS(SELECT e.id FROM Executable e WHERE e.workunit.id = a.id) AND NOT EXISTS(SELECT p.id FROM Parameter p WHERE p.workunit.id = a.id) AND NOT EXISTS(SELECT l.id FROM Link l WHERE l.parentId = a.id and l.parentClassName = 'Workunit') ORDER BY id desc").setParameter("date", LocalDateTime.now()
            .minusMonths(6)).getResultList();
        int purgedWorkunits = 0;
        for (Workunit workunit : emptyWorkunits) {
            if (workunit != null && workunit.isPurgeable()) {
                removeWithDependents(workunit);
                purgedWorkunits++;
            }
        }
        return purgedWorkunits == 0 ? null : String.valueOf(purgedWorkunits);
    }

    public ApplicationType getApplicationTypeImport() {
        return findByName(ApplicationType.class, Constants.APPLICATION_TYPE_IMPORT);
    }

    public BfabricLazyDataModel<Workunit> getAssignSampleTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        List<StatusEnum> status = new ArrayList<>();
        status.add(StatusEnum.PUBLISHED);
        status.add(StatusEnum.PRIVATE);
        entityQuery.addWhereClause("status = :workunitStatus AND createdBy = :createdBy AND container.status NOT IN (:containerStatus)");
        entityQuery.addParameter("createdBy", user.getLogin());
        entityQuery.addParameter("workunitStatus", WorkunitStatusEnum.IMPORTED);
        entityQuery.addParameter("containerStatus", status);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Workunit> getLazyModelByApplicationId(long applicationId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("application.id = :applicationId");
        entityQuery.addParameter("applicationId", applicationId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Workunit> getLazyModelByExecutable(Executable executable) {
        EntityQuery entityQuery = createEntityQuery();
        switch (executable.getContext()) {
        case "APPLICATION":
            entityQuery.addWhereClause("applicationExecutable = :executable");
            break;
        case "SUBMITTER":
            entityQuery.addWhereClause("submitterExecutable = :executable");
            break;
        case "WRAPPERCREATOR":
            entityQuery.addWhereClause("wrapperCreatorExecutable = :executable");
            break;
        default:
            entityQuery.addWhereClause(":executable <> :executable"); // empty list for non-matching case
            break;
        }
        entityQuery.addParameter("executable", executable);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Workunit> getLazyModelByInstrumentId(long instrumentId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity IN (SELECT workunit FROM Application application JOIN application.workunits workunit WHERE application.instrument.id = :instrumentId)");
        entityQuery.addParameter("instrumentId", instrumentId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Workunit> getLazyModelBySampleId(long sampleId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity IN (SELECT resource.workunit FROM Sample sample JOIN sample.resources resource WHERE sample.id = :sampleId)");
        entityQuery.addParameter("sampleId", sampleId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByUserId(long userId) {
        return getLazyModelContainerDependentByUserId(userId);
    }

    public BfabricLazyDataModel<Workunit> getLazyModelSucceedingWorkunitsByWorkunitId(long workunitId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.inputResources inputResource");
        entityQuery.addWhereClause("inputResource.workunit.id = :workunitId");
        entityQuery.addParameter("workunitId", workunitId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Workunit> getSucceedingWorkunitsByWorkunitId(long workunitId) {
        return createNamedQuery("Workunit.findSucceedingWorkunitsByWorkunitId").setParameter("workunitId", workunitId).getResultList();
    }

    public List<Workunit> getWorkunitsBySampleId(long sampleId) {
        return createNamedQuery("Workunit.findBySampleId").setParameter("sampleId", sampleId).getResultList();
    }

    public List<Workunit> getWorkunitsFilteredByContainerIdExcluding(String filterString, Long containerId, Collection<Workunit> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        if (containerId != null) {
            entityQuery.addWhereClause("container.id = :containerId");
            entityQuery.addParameter("containerId", containerId);
        }
        return (List<Workunit>) entityQuery.getResultList();
    }

    public List<Workunit> getWorkunitsFilteredByContainersExcluding(String filterString, Collection<Container> containers, Collection<Workunit> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        if (containers != null && !containers.isEmpty()) {
            entityQuery.addWhereClause("container in (:containers)");
            entityQuery.addParameter("containers", containers);
        }
        return (List<Workunit>) entityQuery.getResultList();
    }

    public List<Workunit> getWorkunitsFilteredExcluding(String filterString, Collection<Workunit> excluded) {
        return getWorkunitsFilteredByContainerIdExcluding(filterString, null, excluded);
    }

    public List<Workunit> getWorkunitsSamplesReassignable() {
        return createNamedQuery("Workunit.findSamplesReassignable").getResultList();
    }

    @Asynchronous
    public void indexWorkunit(Long workunitId) {
        try {
            // Give some time for transaction to flush changes to the database
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Workunit workunit = getEntityManager().find(Workunit.class, workunitId);
        if (workunit != null) {
            IndexHelper.indexEntity(workunit);
        }
    }

    public boolean isAnnotated(Workunit workunit) {
        if (workunit != null) {
            Long countResourcesUnassigned = (Long) createNamedQuery("Workunit.countResourcesUnassigned").setParameter("workunit", workunit).getSingleResult();
            return countResourcesUnassigned != null && countResourcesUnassigned == 0;
        }
        return false;
    }

    public LinkedHashMap<String, String> isValid(Workunit workunit, Set<BfabricUploadedFile> uploadedFiles) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        validationErrorMsg.putAll(isValidCustomAttributes(workunit));
        if (workunit.getApplication() == null) {
            validationErrorMsg.put(Constants.EDIT + ":application", Messages.get("saveFailedApplicationRequired"));
        }
        if (workunit.getContainer() == null) {
            validationErrorMsg.put(Constants.EDIT + ":containerautocomplete", Messages.get("saveFailedContainerRequired"));
        }
        if (workunit.isManaged() && workunit.getContainer() != null && !workunit.getContainer().isExtensible()) {
            validationErrorMsg.put(Constants.EDIT + ":containerautocomplete", Messages.get("saveFailedContainerNotExtensible"));
        }
        if (uploadedFiles != null && uploadedFiles.isEmpty() && !workunit.isManaged() && workunit.getApplication().getApplicationType()
            .equals(getApplicationTypeImport()) && !workunit.isLinkImport()) {
            validationErrorMsg.put(null, Messages.get("saveFailedMultipleFileUploaded"));
        }
        return validationErrorMsg;
    }

    public void markDeleted(Workunit workunit) {
        if (workunit != null) {
            if (workunit.isDeletable()) {
                workunit.removeResourcesFromStorage();
            }
            for (Resource resource : workunit.getResources()) {
                resource.setStatus(ResourceStatusEnum.DELETED);
            }
            workunit.setStatus(WorkunitStatusEnum.DELETED);
            workunit.indexDependents();
            super.save(workunit);
        }
    }

    public void remove(Workunit workunit) {
        if (workunit != null && workunit.isDeletable()) {
            removeWithDependents(workunit);
        }
    }

    public void removeWithDependents(Workunit workunit) {
        if (workunit != null) {
            for (ExternalJob externalJob : workunit.getExternalJobs()) {
                super.remove(externalJob);
            }
            for (Comment comment : workunit.getAssociatedComments()) {
                super.remove(comment);
            }
            super.remove(workunit);
        }
    }

    public void save(Workunit workunit) {
        save(workunit, true);
    }

    public void save(Workunit workunit, boolean index) {
        save(workunit, index, true);
    }

    public void save(Workunit workunit, boolean index, boolean resetStatus) {
        LinkedHashMap<String, String> validationErrorMsg = isValid(workunit, null);
        if (validationErrorMsg.isEmpty()) {
            if (resetStatus) {
                workunit.resetStatus();
            }
            workunit.setNotify();
            super.save(workunit, index);
            if (workunit.isNotifyApplicationSupervisor()) {
                mailSendService.send(workunit.createMail(MailTypeEnum.CONTAINER_WORKUNIT_STATUS));
            }
            if (workunit.isNotifyContainerMember()) {
                mailSendService.send(workunit.createMail(MailTypeEnum.CONTAINER_WORKUNIT_AVAILABLE));
            }
        } else {
            throw new RollbackException(validationErrorMsg.toString());
        }
    }

    public void save(Workunit workunit, Set<Resource> selectedResources, Set<Resource> selectedInputResources, Set<ImportResource> selectedImportResources, Set<BfabricUploadedFile> uploadedFiles, boolean index) {
        boolean created = !workunit.isManaged();
        if (created) {
            switch (workunit.getApplication().getApplicationType().getName()) {
            case Constants.APPLICATION_TYPE_ANALYSIS:
                if (selectedResources != null) {
                    workunit.getResources().addAll(selectedResources);
                }
                if (selectedInputResources != null) {
                    workunit.getInputResources().addAll(selectedInputResources);
                }
                break;
            case Constants.APPLICATION_TYPE_IMPORT:
                if (workunit.isLinkImport()) {
                    workunit.createResourcesFromImportResources(selectedImportResources);
                } else {
                    // Create the import resources from the local file uploads.
                    try {
                        for (ImportResource importResource : workunit.createImportResourcesFromUploadedFiles(uploadedFiles)) {
                            save(importResource);
                        }
                    } catch (IOException e) {
                        logger.warning(e.getMessage());
                    }
                }
                break;
            default:
                break;
            }
        }
        workunit.resetParameters();
        save(workunit, index, true);
        if (created) {
            workunit.triggerApplicationExecution();
        }
    }

    public void unarchive(Workunit workunit, User user) {
        jobService.save(new Job(LogActionEnum.UNARCHIVE, user, workunit));
    }

    public void updateStatus(long workunitId, boolean indexDependents) {
        Workunit workunit = find(Workunit.class, workunitId);
        if (workunit != null && !workunit.isProcessing()) {
            if (indexDependents) {
                workunit.indexDependents();
            }
            save(workunit, false, true);
            indexWorkunit(workunitId);
        }
    }
}
