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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Application;
import org.bfabric.entity.ApplicationType;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetTemplate;
import org.bfabric.entity.Pageflow;
import org.bfabric.entity.Parameter;
import org.bfabric.entity.Technology;
import org.bfabric.entity.Workunit;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class ApplicationService extends AbstractService {

    private static final long serialVersionUID = 1;

    public ApplicationService() {
        super(Application.class);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean checkWebUrlValidity(Long id) {
        Application application = find(Application.class, id);
        application.setLogEntity(false);
        application.checkValidity();
        merge(application);
        return application.isValid();
    }

    public ApplicationType getApplicationTypeAnalysis() {
        return findByName(ApplicationType.class, Constants.APPLICATION_TYPE_ANALYSIS);
    }

    public ApplicationType getApplicationTypeImport() {
        return findByName(ApplicationType.class, Constants.APPLICATION_TYPE_IMPORT);
    }

    public ApplicationType getApplicationTypeWebApp() {
        return findByName(ApplicationType.class, Constants.APPLICATION_TYPE_WEBAPP);
    }

    public List<Application> getApplications(ApplicationType applicationType, List<Pageflow> pageflows, Boolean forEmployeesOnly, Boolean hidden, Technology technology, DatasetTemplate datasetTemplate) {
        EntityQuery entityQuery = createEntityQuery();

        if (datasetTemplate != null) {
            entityQuery.addWhereClause("datasetTemplate is null or datasetTemplate = :datasetTemplate");
            entityQuery.addParameter("datasetTemplate", datasetTemplate);
        }

        if (applicationType != null) {
            entityQuery.addWhereClause("applicationType = :applicationType");
            entityQuery.addParameter("applicationType", applicationType);
        }

        if (pageflows != null && !pageflows.isEmpty()) {
            entityQuery.addWhereClause("pageflow IN :pageflows");
            entityQuery.addParameter("pageflows", pageflows);
        }

        if (forEmployeesOnly != null) {
            entityQuery.addWhereClause("forEmployeesOnly = :forEmployeesOnly");
            entityQuery.addParameter("forEmployeesOnly", forEmployeesOnly);
        }

        if (hidden != null) {
            entityQuery.addWhereClause("hidden = :hidden");
            entityQuery.addParameter("hidden", hidden);
        }

        if (technology != null) {
            entityQuery.addWhereClause(":technology MEMBER OF technologies");
            entityQuery.addParameter("technology", technology);
        }

        entityQuery.addWhereClause("NOT(applicationType = :applicationTypeImport AND pageflow = null) AND NOT( applicationType = :applicationTypeAnalysis AND executable = null) AND ( executable = null OR ( NOT pageflow = null AND NOT storage = null AND NOT executable = null AND NOT wrapperCreator = null AND NOT submitter = null))");
        entityQuery.addParameter("applicationTypeImport", getApplicationTypeImport());
        entityQuery.addParameter("applicationTypeAnalysis", getApplicationTypeAnalysis());
        entityQuery.setOrder("id");
        return (List<Application>) entityQuery.getResultList();
    }

    public List<Application> getApplicationsForWebUrlValidityCheck() {
        return createNamedQuery("Application.findByValidityCheckRequired").setParameter("validityChecked", LocalDateTime.now().minusDays(getConfiguration().getCheckLinkValidityInterval()))
            .getResultList();
    }

    public List<Application> getAvailableSucceedingWebApps(String filterString, Application included) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("enabled = true and hidden = false and applicationType.name = :applicationTypeName or entity = :included");
        entityQuery.addParameter("applicationTypeName", Constants.APPLICATION_TYPE_WEBAPP);
        entityQuery.addParameter("included", included);
        entityQuery.setOrder("name");
        return (List<Application>) entityQuery.getResultList();
    }

    public List<Application> getFilteredImportResourceApplicationsExcluding(String filterString, Collection<Application> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        entityQuery.setMaxResult(100);
        return (List<Application>) entityQuery.getResultList();
    }

    public Pageflow getPageflowDataset() {
        return findByName(Pageflow.class, Constants.DATASET);
    }

    public List<Application> getPotentialPrecedingApplications() {
        return createNamedQuery("Application.findAll").getResultList();
    }

    public List<Application> getPotentialPredecessorApplicationsFiltered(String filterString, Application application) {
        if (application != null) {
            EntityQuery entityQuery = createEntityQueryFiltered(filterString);
            entityQuery.addWhereClause("applicationType = :applicationType");
            entityQuery.addParameter("applicationType", application.getApplicationType());
            entityQuery.setOrder("id DESC");
            if (application.getId() > 0) {
                List<Application> descendants = new ArrayList<>();
                descendants.add(application);
                descendants.addAll(application.getDescendants());
                entityQuery.addNotInEntitiesClause(descendants);
            }
            return (List<Application>) entityQuery.getResultList();
        }
        return new ArrayList<>();
    }

    public List<Application> getPotentialSucceedingApplications() {
        return createNamedQuery("Application.findAllPotentialSucceeding").getResultList();
    }

    public List<Application> getReassignApplicationSupervisorTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("enabled = TRUE and supervisor.empDegree IS NULL and supervisor.login <> 'admin'");
        entityQuery.setOrder("id");
        return (List<Application>) entityQuery.getResultList();
    }

    public List<Application> getRunnableApplications(boolean hasApplicationManagerRole, DatasetTemplate datasetTemplate) {
        List<Pageflow> pageflows = new ArrayList<>();
        pageflows.add(getPageflowDataset());
        Boolean forEmployeesOnly = null;
        if (!hasApplicationManagerRole) {
            forEmployeesOnly = Boolean.FALSE;
        }
        return getApplications(getApplicationTypeAnalysis(), pageflows, forEmployeesOnly, Boolean.FALSE, null, datasetTemplate);
    }

    public BfabricLazyDataModel<Application> getRunnableApplicationsLazyModel(Container container, boolean hasEmployeeRole) {
        if (container != null && container.isExtensible()) {
            EntityQuery entityQuery = createEntityQuery();
            if (!hasEmployeeRole) {
                entityQuery.addWhereClause("forEmployeesOnly != TRUE");
            }
            entityQuery.addWhereClause(
                "hidden != TRUE AND (applicationType != :applicationType OR (importResourcesRequired != TRUE OR EXISTS (SELECT ir from ImportResource ir WHERE ir.application = entity AND ir.container = :container AND ir.status = org.bfabric.enums.ResourceStatusEnum.AVAILABLE AND (ir.expirationDate IS NULL OR ir.expirationDate > :currentDate))))");
            entityQuery.addParameter("applicationType", getApplicationTypeImport());
            entityQuery.addParameter("container", container);
            entityQuery.addParameter("currentDate", LocalDateTime.now());
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return null;
    }

    public List<Application> getRunnableWebAppsByEntity(AbstractEntity entity, boolean hasEmployeeRole) {
        EntityQuery entityQuery = createEntityQuery();
        if (!hasEmployeeRole) {
            entityQuery.addWhereClause("forEmployeesOnly != TRUE");
        }
        entityQuery.addWhereClause("enabled = true and hidden != TRUE AND applicationType = :applicationType");
        entityQuery.addParameter("applicationType", getApplicationTypeWebApp());
        if (entity != null) {
            if (entity instanceof Container) {
                Container container = (Container) entity;
                if (!container.hasBeenAccepted() || !container.isExtensible()) {
                    return new ArrayList<>();
                }
            }
            entityQuery.addWhereClause("entityClassName = :entityClassName");
            entityQuery.addParameter("entityClassName", entity.getClass().getSimpleName());
            if (entity instanceof Workunit) {
                Workunit workunit = (Workunit) entity;
                entityQuery.addWhereClause("(entity.precedingApplications is empty or exists(select pa from entity.precedingApplications pa where pa = :application))");
                entityQuery.addParameter("application", workunit.getApplication());
            } else if (entity instanceof Dataset) {
                Dataset dataset = (Dataset) entity;
                entityQuery.addWhereClause("datasetTemplate is null or datasetTemplate = :datasetTemplate");
                entityQuery.addParameter("datasetTemplate", dataset.getDatasetTemplate());
            }
        }
        return (List<Application>) entityQuery.getResultList();
    }

    public String isValid(Application application) {
        List<Application> potentialSucceedingApplications = getPotentialSucceedingApplications();
        for (Application succeedingApplication : application.getSucceedingApplications()) {
            if (!potentialSucceedingApplications.contains(succeedingApplication)) {
                return "Invalid succeeding application " + succeedingApplication.getId() + " of type " + succeedingApplication.getApplicationType().getName();
            }
        }
        return null;
    }

    public void removeParameter(Application application, Parameter parameter) {
        application.getParameters().remove(parameter);
        save(application, false);
    }

    public void removeParameters(Application application) {
        application.getParameters().clear();
        save(application, false);
    }
}
