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
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SampleType;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.enums.SamplePreparationProtocolDiscriminator;
import org.bfabric.enums.SamplePreparationProtocolType;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.CollectionHelper;

@Named
@Stateless
public class SamplePreparationProtocolService extends AbstractService {

    private static final long serialVersionUID = 1;

    public SamplePreparationProtocolService() {
        super(SamplePreparationProtocol.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final SamplePreparationProtocol samplePreparationProtocol = (SamplePreparationProtocol) entity;
        final EntityQuery entityQuery = createEntityQuery();
        StringBuilder whereClause = new StringBuilder("entity.id <> :entityId AND LOWER(entity.name) = LOWER(:name) AND entity.discriminator = :discriminator");
        if (samplePreparationProtocol.getDiscriminator() != null) {
            if (SamplePreparationProtocolDiscriminator.Instrument.equals(samplePreparationProtocol.getDiscriminator()) && !samplePreparationProtocol.getInstruments().isEmpty()) {
                whereClause.append(" AND EXISTS(SELECT i FROM SamplePreparationProtocol s join s.instruments i WHERE" + " s.id <> :entityId AND LOWER(s.name) = LOWER(:name) AND s.discriminator = :discriminator AND" + " i.id IN (:instrumentIds))");
                entityQuery.addParameter("instrumentIds", samplePreparationProtocol.getInstruments().stream().map(Instrument::getId).collect(Collectors.toList()));
            } else if (SamplePreparationProtocolDiscriminator.SequencingApplication.equals(samplePreparationProtocol.getDiscriminator()) && !samplePreparationProtocol.getSequencingApplications()
                .isEmpty()) {
                whereClause.append(" AND EXISTS(SELECT sa FROM SamplePreparationProtocol s join s.sequencingApplications sa WHERE" + " s.id <> :entityId AND LOWER(s.name) = LOWER(:name) AND s.discriminator = :discriminator AND" + " sa.id IN (:sequencingApplicationIds))");
                entityQuery.addParameter("sequencingApplicationIds", samplePreparationProtocol.getSequencingApplications().stream().map(SequencingApplication::getId).collect(Collectors.toList()));
            }
        }
        entityQuery.setWhere(whereClause.toString());
        entityQuery.addParameter("entityId", samplePreparationProtocol.getId());
        entityQuery.addParameter("name", samplePreparationProtocol.getName());
        entityQuery.addParameter("discriminator", samplePreparationProtocol.getDiscriminator());
        entityQuery.setMaxResult(1);
        return entityQuery.getResultList().isEmpty();
    }

    private EntityQuery createFilteredEnabledSamplePreparationProtocolsIncludingQuery(String filterString, Sample sample) {
        final EntityQuery entityQuery = createEntityQuery();
        StringBuilder whereClause = new StringBuilder("entity.enabled = TRUE");
        if (sample != null) {
            if (sample.getSampleType() != null) {
                entityQuery.setJoin("entity.sampleTypes sampleType");
                whereClause.append(" and sampleType = :sampleType");
                entityQuery.addParameter("sampleType", sample.getSampleType());
            }
            if (sample.getSamplePreparationProtocol() != null) {
                whereClause.append(" or entity = :entity");
                entityQuery.addParameter("entity", sample.getSamplePreparationProtocol());
            }
        }
        entityQuery.setWhere("(" + whereClause + ")");
        if (filterString != null) {
            entityQuery.setMaxResult(100);
            entityQuery.addIdOrNameWhereClause(filterString, "entity.name");
        }
        entityQuery.setOrder("entity.name");
        return entityQuery;
    }

    public List<SamplePreparationProtocol> getEnabledLibraryProtocolsIncludingByInstrumentOrSequencingApplication(Instrument instrument, SequencingApplication sequencingApplication, SamplePreparationProtocol samplePreparationProtocol) {
        EntityQuery entityQuery = createEntityQuery();
        StringBuilder whereClause = new StringBuilder("entity.type = :type and (entity = :samplePreparationProtocol or entity.enabled = TRUE)");
        if (instrument != null) {
            entityQuery.setJoinTypeLeftOuter();
            entityQuery.setJoin("entity.instruments instrument");
            if (sequencingApplication != null) {
                entityQuery.setJoin("entity.instruments instrument left join entity.sequencingApplications sequencingApplication");
                whereClause.append(" and (instrument = :instrument or sequencingApplication = :sequencingApplication)");
            } else {
                whereClause.append(" and instrument = :instrument");
            }
        } else if (sequencingApplication != null) {
            entityQuery.setJoinTypeLeftOuter();
            entityQuery.setJoin("entity.sequencingApplications sequencingApplication");
            whereClause.append(" and sequencingApplication = :sequencingApplication");
        }
        entityQuery.setWhere(whereClause.toString());
        entityQuery.addParameter("type", SamplePreparationProtocolType.Library);
        entityQuery.addParameter("samplePreparationProtocol", samplePreparationProtocol);
        if (instrument != null) {
            entityQuery.addParameter("instrument", instrument);
        }
        if (sequencingApplication != null) {
            entityQuery.addParameter("sequencingApplication", sequencingApplication);
        }
        entityQuery.setOrder("entity.name ASC");
        return (List<SamplePreparationProtocol>) entityQuery.getResultList();
    }

    public List<SamplePreparationProtocol> getEnabledSamplePreparationProtocolsIncluding(Sample sample) {
        return getFilteredEnabledSamplePreparationProtocolsIncluding(null, sample);
    }

    public List<SamplePreparationProtocol> getEnabledSamplePreparationProtocolsIncludingByName(String name, Sample sample) {
        final EntityQuery entityQuery = createFilteredEnabledSamplePreparationProtocolsIncludingQuery(null, sample);
        entityQuery.addWhereClause("lower(entity.name) = lower(:entityName)");
        entityQuery.addParameter("entityName", name);
        return (List<SamplePreparationProtocol>) entityQuery.getResultList();
    }

    public List<SamplePreparationProtocol> getEnabledSampleTypeSpecificProtocols(SampleType sampleType) {
        EntityQuery entityQuery = createEntityQuery();
        StringBuilder whereClause = new StringBuilder("entity.enabled = TRUE");
        if (sampleType != null) {
            entityQuery.setJoin("entity.sampleTypes sampleType");
            whereClause.append(" and sampleType = :sampleType");
            entityQuery.addParameter("sampleType", sampleType);
        }
        entityQuery.setWhere(whereClause.toString());
        entityQuery.setOrder("entity.name");
        return (List<SamplePreparationProtocol>) entityQuery.getResultList();
    }

    public List<SamplePreparationProtocol> getFilteredEnabledSamplePreparationProtocolsIncluding(String filterString, Sample sample) {
        return (List<SamplePreparationProtocol>) createFilteredEnabledSamplePreparationProtocolsIncludingQuery(filterString, sample).getResultList();
    }

    public List<SamplePreparationProtocol> getFilteredEnabledSamplePreparationProtocolsIncluding(String filterString, SamplePreparationProtocol samplePreparationProtocol) {
        return createNamedQuery("SamplePreparationProtocol.filterEnabledIncludingByIdOrName").setParameter("entityId", samplePreparationProtocol != null ? samplePreparationProtocol.getId() : null)
            .setParameter("filterString", "%" + filterString + "%").getResultList();
    }

    public List<SamplePreparationProtocol> getFilteredInstrumentExcluding(String filterString, Collection<SamplePreparationProtocol> samplePreparationProtocols) {
        return samplePreparationProtocols != null && !samplePreparationProtocols.isEmpty() ? createNamedQuery("SamplePreparationProtocol.filterInstrumentExcluding").setParameter("excluded", samplePreparationProtocols)
            .setParameter("filterString", "%" + filterString + "%")
            .getResultList() : createNamedQuery("SamplePreparationProtocol.filterInstrument").setParameter("filterString", "%" + filterString + "%").getResultList();
    }

    public BfabricLazyDataModel<SamplePreparationProtocol> getLazyModelByAnnotation(Annotation annotation) {
        if (annotation != null) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("strandedness.id = :annotationId");
            entityQuery.addParameter("annotationId", annotation.getId());
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return null;
    }

    public BfabricLazyDataModel<?> getLazyModelByInstrumentId(long instrumentId) {
        return getLazyModelUnnestById("instruments", instrumentId);
    }

    public List<SamplePreparationProtocol> getSamplePreparationProtocols(String filterString, Set<SamplePreparationProtocol> exclude) {
        return (List<SamplePreparationProtocol>) getFilteredExcluded(filterString, exclude);
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final SamplePreparationProtocol samplePreparationProtocol = (SamplePreparationProtocol) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        if (samplePreparationProtocol.getDiscriminator() != null) {
            if (SamplePreparationProtocolDiscriminator.Instrument.equals(samplePreparationProtocol.getDiscriminator())) {
                if (samplePreparationProtocol.getInstruments().isEmpty()) {
                    validationErrorMsg.put(Constants.EDIT + ":selectedinstrumentsautocomplete", Constants.REQUIRED);
                } else {
                    validationErrorMsg.putAll(isValidName(samplePreparationProtocol, Constants.EDIT + ":" + Constants.NAME, "not unique for discriminator '" + samplePreparationProtocol.getDiscriminator() + "' and instrument(s) '" + CollectionHelper.printDisplayNames(samplePreparationProtocol.getInstruments()) + "'"));
                }
            } else if (SamplePreparationProtocolDiscriminator.SequencingApplication.equals(samplePreparationProtocol.getDiscriminator())) {
                if (samplePreparationProtocol.getSequencingApplications().isEmpty()) {
                    validationErrorMsg.put(Constants.EDIT + ":sequencingapplicationsautocomplete", Constants.REQUIRED);
                } else {
                    validationErrorMsg.putAll(isValidName(samplePreparationProtocol, Constants.EDIT + ":" + Constants.NAME, "not unique for discriminator '" + samplePreparationProtocol.getDiscriminator() + "' and sequencing application(s) '" + CollectionHelper.printDisplayNames(samplePreparationProtocol.getSequencingApplications()) + "'"));
                }
            }
        }
        return validationErrorMsg;
    }
}