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
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Application;
import org.bfabric.entity.Contract;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.exception.RollbackException;

@Named
@Stateless
public class TechnologyService extends AbstractService {

    private static final long serialVersionUID = 1;

    public TechnologyService() {
        super(Technology.class);
    }

    @Override
    public List<Technology> getResultList() {
        return createNamedQuery("Technology.findAllOrderByPosition").getResultList();
    }

    public List<Technology> getTechnologiesEnabledIncludingTechnologies(Set<Technology> technologies) {
        return technologies != null && !technologies.isEmpty() ? createNamedQuery("Technology.findByEnabledIncludingTechnologies").setParameter("technologies", technologies).getResultList()
            : createNamedQuery("Technology.findByEnabled").getResultList();
    }

    public List<Technology> getTechnologiesHavingOrders() {
        return createNamedQuery("Technology.havingOrders").getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((Technology) entity);
    }

    public void merge(Technology technology, Technology merged, Technology mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            technology.setName(mergeSelection.getName());
            technology.setEnabled(mergeSelection.isEnabled());
            technology.setOrderPosition(mergeSelection.getOrderPosition());
            technology.setDescription(mergeSelection.getDescription());
            // Merge the one-to-many associations. IMPORTANT: Do this before merging the many-to-many associations.
            reassign(Application.class, merged, technology, "technology");
            reassign(User.class, merged, technology, "defaultTechnology");
            reassign(Contract.class, merged, technology, "technology");
            reassign(Purchase.class, merged, technology, "technology");
            // Merge the many-to-many associations.
            if (!merged.getContainers().isEmpty()) {
                technology.getContainers().addAll(merged.getContainers());
                merged.getContainers().clear();
            }
            if (!merged.getInstruments().isEmpty()) {
                technology.getInstruments().addAll(merged.getInstruments());
                merged.getInstruments().clear();
            }
            if (!merged.getSamplePreparationProtocols().isEmpty()) {
                technology.getSamplePreparationProtocols().addAll(merged.getSamplePreparationProtocols());
                merged.getSamplePreparationProtocols().clear();
            }
            if (!merged.getServiceTypes().isEmpty()) {
                technology.getServiceTypes().addAll(merged.getServiceTypes());
                merged.getServiceTypes().clear();
            }
            saveMerge(technology, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void reassign(Class<?> clazz, Technology oldTechnology, Technology newTechnology, String attribute) {
        if (clazz != null && oldTechnology != null && newTechnology != null && attribute != null) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE ").append(clazz.getSimpleName()).append(" SET ").append(attribute).append(" = :new WHERE ").append(attribute).append(" = :old");
            if (clazz.equals(Technology.class)) {
                queryBuilder.append(" AND id <> :id");
            }
            Query query = createQuery(queryBuilder.toString()).setParameter("old", oldTechnology).setParameter("new", newTechnology);
            if (clazz.equals(Technology.class)) {
                query.setParameter("id", oldTechnology.getId());
            }
            query.executeUpdate();
        }
    }
}
