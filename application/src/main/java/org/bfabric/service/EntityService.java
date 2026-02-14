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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.entity.AbstractContainerDependentEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.api.HasSupervisor;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class EntityService extends AbstractService {

    private static final long serialVersionUID = 1;

    public boolean checkEntityExistence(String entityClassName, long id) {
        return !createQuery("select id from " + entityClassName + " where id = " + id).getResultList().isEmpty();
    }

    public boolean checkUniqueName(AbstractEntity entity, String name) {
        return checkUniqueAttributeValue(entity, "name", name);
    }

    public AbstractEntity getEntityByClassNameAndId(String className, Long id) {
        final EntityQuery entityQuery = createEntityQuery(className);
        entityQuery.addWhereClause("id = :id");
        entityQuery.addParameter("id", id);
        entityQuery.setMaxResult(1);
        return (AbstractEntity) entityQuery.getResultList().stream().findFirst().orElse(null);
    }

    public <T extends AbstractContainerDependentEntity> T getNextInContainer(String className, Long id, Long containerId) {
        return (T) createQuery("SELECT e FROM " + className + " e WHERE e.container.id = :containerId AND e.id > :currentId ORDER BY e.id ASC").setParameter("containerId", containerId)
            .setParameter("currentId", id).setMaxResults(1).getResultStream().findFirst().orElse(null);
    }

    public <T extends AbstractContainerDependentEntity> T getPrevInContainer(String className, Long id, Long containerId) {
        return (T) createQuery("SELECT e FROM " + className + " e WHERE e.container.id = :containerId AND e.id < :currentId ORDER BY e.id DESC").setParameter("containerId", containerId)
            .setParameter("currentId", id).setMaxResults(1).getResultStream().findFirst().orElse(null);
    }

    public List<?> getQueryResultList(String query, int firstResult, int maxResults) {
        return createQuery(query).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public Long getQuerySingleResult(String query) {
        return (Long) createQuery(query).getSingleResult();
    }

    public List<HasSupervisor> getSupervisorDutiesLazyModelByUserId(long userId) {
        List<HasSupervisor> supervisorDuties = createQuery("FROM org.bfabric.entity.api.HasSupervisor entity WHERE entity.supervisor.id = :userId").setParameter("userId", userId).getResultList();
        supervisorDuties.removeIf(duty -> !duty.isEnabled());
        supervisorDuties.removeIf(duty -> duty instanceof WorkflowStep && !((WorkflowStep) duty).getStatus().equals(StatusEnum.RUNNING));
        return supervisorDuties;
    }
}