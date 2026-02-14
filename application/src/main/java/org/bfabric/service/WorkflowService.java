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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.entity.Dataset;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.Workunit;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class WorkflowService extends AbstractService {

    private static final long serialVersionUID = 1;

    public WorkflowService() {
        super(Workflow.class);
    }

    public void assignDatasets(Workflow workflow, Set<Dataset> selectedDatasets) {
        workflow.getDatasets().clear();
        workflow.getDatasets().addAll(selectedDatasets);
        save(workflow);
    }

    public void assignPlates(Workflow workflow, Set<Plate> selectedPlates) {
        workflow.getPlates().clear();
        workflow.getPlates().addAll(selectedPlates);
        save(workflow);
    }

    public void assignSamples(Workflow workflow, Set<Sample> selectedSamples) {
        workflow.getSamples().clear();
        workflow.getSamples().addAll(selectedSamples);
        save(workflow);
    }

    public void assignWorkunits(Workflow workflow, Set<Workunit> selectedWorkunits) {
        workflow.getWorkunits().clear();
        workflow.getWorkunits().addAll(selectedWorkunits);
        save(workflow);
    }

    public List<Workflow> getWorkflowsByCreatedBy(String login) {
        return createNamedQuery("Workflow.findByCreatedByOrderById").setParameter("createdBy", login).getResultList();
    }

    public List<Workflow> getWorkflowsFilteredExcluding(String filterString, Collection<Workflow> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString, "entity.workflowTemplate.name");
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<Workflow>) entityQuery.getResultList();
    }
}