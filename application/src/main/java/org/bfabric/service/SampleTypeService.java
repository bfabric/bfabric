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
import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.SampleType;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class SampleTypeService extends AbstractService {

    private static final long serialVersionUID = 1;

    public SampleTypeService() {
        super(SampleType.class);
    }

    @Override
    public List<SampleType> getResultList() {
        return (List<SampleType>) getResultListOrderByName();
    }

    public List<SampleType> getResultListEnabledIncludingExcludingOrderBy(SampleType entity, Collection<String> excludedNames, String orderClause) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity = :entity OR enabled = true" + (excludedNames == null || excludedNames.isEmpty() ? "" : " AND name NOT IN (:excludedNames)"));
        entityQuery.addParameter("entity", entity);
        if (excludedNames != null && !excludedNames.isEmpty()) {
            entityQuery.addParameter("excludedNames", excludedNames);
        }
        entityQuery.setOrder(StringHelper.isNotEmpty(orderClause) ? orderClause : "name");
        return (List<SampleType>) entityQuery.getResultList();
    }

    public SampleType getSampleTypeByName(String name) {
        return (SampleType) createNamedQuery("SampleType.findByName").setParameter("name", name).getSingleResult();
    }

    public List<SampleType> getSampleTypesByRunId(Long runId) {
        return createNamedQuery("SampleType.findByRunId").setParameter("runId", runId).getResultList();
    }

    public List<SampleType> getSampleTypesFiltered(String filterString, Collection<SampleType> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        entityQuery.setMaxResult(100);
        return (List<SampleType>) entityQuery.getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((SampleType) entity);
    }
}
