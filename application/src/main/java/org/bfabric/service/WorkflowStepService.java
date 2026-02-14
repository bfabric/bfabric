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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.User;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.BfabricUploadedFile;

@Named
@Stateless
public class WorkflowStepService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private CommentService commentService;

    public WorkflowStepService() {
        super(WorkflowStep.class);
    }

    public void assignDatasets(WorkflowStep workflowStep, Set<Dataset> selectedDatasets) {
        workflowStep.getDatasets().clear();
        workflowStep.getDatasets().addAll(selectedDatasets);
        save(workflowStep);
    }

    public void assignPlates(WorkflowStep workflowStep, Set<Plate> selectedPlates) {
        workflowStep.getPlates().clear();
        workflowStep.getPlates().addAll(selectedPlates);
        save(workflowStep);
    }

    public void assignSamples(WorkflowStep workflowStep, Set<Sample> selectedSamples) {
        workflowStep.getSamples().clear();
        workflowStep.getSamples().addAll(selectedSamples);
        save(workflowStep);
    }

    public void assignWorkunits(WorkflowStep workflowStep, Set<Workunit> selectedWorkunits) {
        workflowStep.getWorkunits().clear();
        workflowStep.getWorkunits().addAll(selectedWorkunits);
        save(workflowStep);
    }

    public BfabricLazyDataModel<WorkflowStep> getSupervisedWorkflowSteps(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("supervisor.id = :userId");
        entityQuery.addParameter("userId", user.getId());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<WorkflowStep> getWorkflowStepTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("supervisor.id = :userId and status = :status and workflow.container.status not in :finalStates");
        entityQuery.addParameter("userId", user.getId());
        entityQuery.addParameter("status", StatusEnum.RUNNING);
        entityQuery.addParameter("finalStates", Arrays.asList(StatusEnum.CLOSED, StatusEnum.CANCELED));
        entityQuery.setOrder("id DESC");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<WorkflowStep> getWorkflowStepsFilteredExcluding(String filterString, Collection<WorkflowStep> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString, "entity.workflowTemplateStep.name");
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<WorkflowStep>) entityQuery.getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidCustomAttributes(entity);
    }

    public Set<String> save(WorkflowStep workflowStep, Set<BfabricUploadedFile> bfabricUploadedFiles) {
        save(workflowStep);
        // Important: Do NOT replace !workflowStep.getComments().isEmpty() by workflowStep.getComment() != null since getComment() will automatically create an empty comment!
        return !workflowStep.getComments().isEmpty() ? commentService.save(workflowStep.getComment(), bfabricUploadedFiles, true) : new HashSet<>();
    }
}