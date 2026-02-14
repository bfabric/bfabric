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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Technology;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class InstrumentService extends AbstractService {

    private static final long serialVersionUID = 1;

    public InstrumentService() {
        super(Instrument.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Instrument instrument = (Instrument) entity;
        return checkUniqueAttributeValue(instrument, "label", instrument.getLabel());
    }

    private EntityQuery createInstrumentsEntityQuery(boolean all, Technology technology, boolean userVisibleRestricted) {
        EntityQuery entityQuery = createEntityQuery();
        if (!all) {
            entityQuery.addWhereClause("entity.instrumentStatusInfo.bookable = true");
        }
        if (userVisibleRestricted) {
            entityQuery.addWhereClause("entity.instrumentStatusInfo.userVisible = true");
        }
        if (technology != null) {
            entityQuery.addUnnestWhereClause("technologies", null, technology.getId());
        }
        entityQuery.setOrder("entity.label");
        return entityQuery;
    }

    public List<Instrument> getBookableInstruments() {
        return createNamedQuery("Instrument.findBookable").getResultList();
    }

    public List<Instrument> getBookableInstrumentsByTechnology(Technology technology, boolean userVisibleRestricted) {
        return getInstruments(false, technology, userVisibleRestricted);
    }

    public List<Instrument> getInstruments(boolean all, Technology technology, boolean userVisibleRestricted) {
        return (List<Instrument>) createInstrumentsEntityQuery(all, technology, userVisibleRestricted).getResultList();
    }

    public List<Instrument> getInstruments(String filterString) {
        return getInstruments(filterString, null, null);
    }

    public List<Instrument> getInstruments(String filterString, Collection<Instrument> included, Collection<Instrument> excluded) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addInEntitiesClause(included);
        entityQuery.addNotInEntitiesClause(excluded, "OR");
        entityQuery.addIdOrNameWhereClause(filterString);
        return (List<Instrument>) entityQuery.getResultList();
    }

    public List<Instrument> getInstruments(String filterString, boolean all, Technology technology, boolean userVisibleRestricted) {
        EntityQuery entityQuery = createInstrumentsEntityQuery(all, technology, userVisibleRestricted);
        entityQuery.addIdOrNameWhereClause(filterString);
        return (List<Instrument>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<Instrument> getLazyModelByAnnotation(Annotation annotation) {
        if (annotation != null) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("annotation.id = :id");
            entityQuery.addParameter("id", annotation.getId());
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return null;
    }

    public BfabricLazyDataModel<Instrument> getReassignInstrumentAdminTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("admin.empDegree IS NULL AND instrumentStatusInfo.available = TRUE");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Instrument> getReassignInstrumentSupervisorTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("supervisor.empDegree IS NULL and enabled = true AND instrumentStatusInfo.available = TRUE");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Instrument> getReassignableAdminInstrumentsByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("admin.id = :userId and enabled = true and instrumentStatusInfo.available = true");
        entityQuery.addParameter("userId", userId);
        return (List<Instrument>) entityQuery.getResultList();
    }

    @Override
    public List<Instrument> getResultList() {
        return createNamedQuery("Instrument.findAllOrderByLabel").getResultList();
    }

    public List<Instrument> getResultListBookableIncluding(Instrument instrument) {
        return createNamedQuery("Instrument.findBookableIncluding").setParameter("entity", instrument).getResultList();
    }

    public List<Instrument> getResultListBookableIncludingFiltered(String filterString, Instrument instrument) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("instrumentStatusInfo.bookable = true or entity = :entity");
        entityQuery.addParameter("entity", instrument);
        entityQuery.addIdOrNameWhereClause(filterString);
        entityQuery.setOrder("label");
        return (List<Instrument>) entityQuery.getResultList();
    }

    public List<Instrument> getResultListEnabledByName(String name) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("enabled = true and lower(name) = lower(:entityName)");
        entityQuery.addParameter("entityName", name);
        return (List<Instrument>) entityQuery.getResultList();
    }

    public List<Instrument> getResultListEnabledIncludingByServiceType(Instrument instrument, ServiceType serviceType) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.serviceTypes serviceType");
        entityQuery.setWhere("(entity.enabled = true or entity = :entity) and serviceType = :serviceType");
        entityQuery.addParameter("entity", instrument);
        entityQuery.addParameter("serviceType", serviceType);
        entityQuery.setOrder("entity.label ASC");
        return (List<Instrument>) entityQuery.getResultList();
    }

    public List<Instrument> getResultListRunEnabledIncluding(Instrument instrument) {
        return createNamedQuery("Instrument.findRunEnabledIncluding").setParameter("entity", instrument).getResultList();
    }

    public List<Instrument> getResultListRunEnabledIncludingFiltered(String filterString, Instrument instrument) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("instrumentStatusInfo.runEnabled = true or entity = :entity");
        entityQuery.addParameter("entity", instrument);
        entityQuery.addIdOrNameWhereClause(filterString);
        entityQuery.setOrder("label");
        return (List<Instrument>) entityQuery.getResultList();
    }

    public List<Instrument> getRunEnabledInstruments() {
        return createNamedQuery("Instrument.findRunEnabled").getResultList();
    }

    public List<Technology> getTechnologiesByBookableInstruments() {
        List<Instrument> bookableInstruments = getBookableInstruments();
        Set<Technology> ret = new HashSet<>();
        for (Instrument instrument : bookableInstruments) {
            ret.addAll(instrument.getTechnologies());
        }
        return new ArrayList<>(ret);
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((Instrument) entity);
    }

    public Map<String, List<Instrument>> remove(Instrument instrument) {
        find(Instrument.class, instrument.getId());
        // Save the changes made to the hierarchy.
        Map<String, List<Instrument>> updatedInstruments = updateHierarchy(true, instrument);
        super.remove(instrument);
        return updatedInstruments;
    }

    @Transactional
    public Map<String, List<Instrument>> save(Instrument instrument) {
        // Create the default reservation settings if the instrument has no setting.
        boolean isCreateDefaultReservationSettings = instrument.getReservationSettings().isEmpty();

        find(Instrument.class, instrument.getId());
        // Save the changes made to the hierarchy.
        Map<String, List<Instrument>> updatedInstruments = updateHierarchy(false, instrument);

        // Save the current instrument.
        instrument.updateState(instrument.getInstrumentStatusInfo().getStatusComment());
        super.save(instrument);

        if (isCreateDefaultReservationSettings) {
            save(new InstrumentReservationSetting(instrument));
        }

        return updatedInstruments;
    }

    public Map<String, List<Instrument>> updateHierarchy(boolean isDeleted, Instrument instrument) {
        List<Instrument> updateDescendants = new ArrayList<>();
        List<Instrument> updateAncestors = new ArrayList<>();
        instrument.propagateChangesToHierarchy(isDeleted, updateDescendants, updateAncestors);

        // Save the changes made to the descendants.
        if (!updateDescendants.isEmpty()) {
            Collections.reverse(updateDescendants);
            for (Instrument child : updateDescendants) {
                child.updateState(instrument.getInstrumentStatusInfo().getStatusComment());
                merge(child);
            }
        }

        // Save the changes made to the ancestors.
        if (!updateAncestors.isEmpty()) {
            Collections.reverse(updateAncestors);
            for (Instrument parent : updateAncestors) {
                parent.updateState(instrument.getInstrumentStatusInfo().getStatusComment());
                merge(parent);
            }
        }

        Map<String, List<Instrument>> updatedInstruments = new HashMap<>();
        updatedInstruments.put(Constants.DESCENDANT, updateDescendants);
        updatedInstruments.put(Constants.ANCESTOR, updateAncestors);
        return updatedInstruments;
    }
}