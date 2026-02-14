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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.FlushModeType;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.SampleUserDecisionEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.RollbackException;
import org.bfabric.forms.MFHelper;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.AJAX;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.MultiplexIdConflictRecord;
import org.bfabric.util.StringHelper;
import org.hibernate.Hibernate;

@Named
@Stateless
public class SampleService extends AbstractService {

    private static final long serialVersionUID = 1;

    @Inject
    private SampleTypeService sampleTypeService;

    public SampleService() {
        super(Sample.class);
    }

    private boolean checkMultiplexIdsConflict(Map.Entry<Sample, String> firstSampleMultiplexIdsConcatenationEntry, Map.Entry<Sample, String> secondSampleMultiplexIdsConcatenationEntry, Pattern pattern, String multiplexIdCheckType) {
        if (Constants.MULTIPLEX_ID_CHECK_ADVANCED.equals(multiplexIdCheckType)) {
            return checkMultiplexIdsConflictAdvanced(firstSampleMultiplexIdsConcatenationEntry, secondSampleMultiplexIdsConcatenationEntry, pattern);
        }
        if (Constants.MULTIPLEX_ID_CHECK_BASIC.equals(multiplexIdCheckType)) {
            return checkMultiplexIdsConflictBasic(firstSampleMultiplexIdsConcatenationEntry, secondSampleMultiplexIdsConcatenationEntry);
        }
        return false;
    }

    private boolean checkMultiplexIdsConflictAdvanced(Map.Entry<Sample, String> firstSampleMultiplexIdsConcatenationEntry, Map.Entry<Sample, String> secondSampleMultiplexIdsConcatenationEntry, Pattern pattern) {
        String multiplexIdFirst = firstSampleMultiplexIdsConcatenationEntry.getKey().getMultiplexId();
        if (multiplexIdFirst != null) {
            multiplexIdFirst = multiplexIdFirst.toLowerCase();
        }
        String multiplexId2First = firstSampleMultiplexIdsConcatenationEntry.getKey().getMultiplexId2();
        if (multiplexId2First != null) {
            multiplexId2First = multiplexId2First.toLowerCase();
        }
        String multiplexIdSecond = secondSampleMultiplexIdsConcatenationEntry.getKey().getMultiplexId();
        if (multiplexIdSecond != null) {
            multiplexIdSecond = multiplexIdSecond.toLowerCase();
        }
        String multiplexId2Second = secondSampleMultiplexIdsConcatenationEntry.getKey().getMultiplexId2();
        if (multiplexId2Second != null) {
            multiplexId2Second = multiplexId2Second.toLowerCase();
        }
        boolean multiplexIdSetFirst = StringHelper.isNotEmpty(multiplexIdFirst);
        boolean multiplexIdSetSecond = StringHelper.isNotEmpty(multiplexIdSecond);
        boolean multiplexId2SetFirst = StringHelper.isNotEmpty(multiplexId2First);
        boolean multiplexId2SetSecond = StringHelper.isNotEmpty(multiplexId2Second);
        boolean multiplexIdSetBoth = multiplexIdSetFirst && multiplexIdSetSecond;
        boolean multiplexId2SetBoth = multiplexId2SetFirst && multiplexId2SetSecond;
        boolean multiplexIdAndMultiplexId2SetBoth = multiplexIdSetBoth && multiplexId2SetBoth;
        String multiplexIdsConcatenationFirst = firstSampleMultiplexIdsConcatenationEntry.getValue();
        String multiplexIdsConcatenationSecond = secondSampleMultiplexIdsConcatenationEntry.getValue();
        // One sample has only multiplexId set while the other has only multiplexId2 set and vice versa.
        if (firstSampleMultiplexIdsConcatenationEntry.getKey().checkMultiplexIdsOnlyOfPattern(pattern) && secondSampleMultiplexIdsConcatenationEntry.getKey().checkMultiplexIdsOnlyOfPattern(pattern)) {
            // The multiplex ids are only of the given pattern.
            if (multiplexIdAndMultiplexId2SetBoth) {
                // Both samples have multiplexId and multiplexId2 set.
                if (multiplexIdFirst.length() == multiplexIdSecond.length() && multiplexId2First.length() == multiplexId2Second.length()) {
                    // multiplexId and multiplexId2 are not empty and have the same length, i.e., no substring check is needed.
                    return multiplexIdsConcatenationFirst.equalsIgnoreCase(multiplexIdsConcatenationSecond);
                }
                // multiplexId and multiplexId2 are not empty and of different length, hence an additional substring (prefix) check is needed.
                boolean multiplexIdStartsWith = multiplexIdFirst.startsWith(multiplexIdSecond) || multiplexIdSecond.startsWith(multiplexIdFirst);
                boolean multiplexId2StartsWith = multiplexId2First.startsWith(multiplexId2Second) || multiplexId2Second.startsWith(multiplexId2First);
                if (!multiplexIdStartsWith && !multiplexId2StartsWith) {
                    return false;
                }
                return !(!multiplexIdStartsWith || !multiplexId2StartsWith);
            }
            if (multiplexIdSetBoth) {
                // Both samples have multiplexId set but one or both samples have multiplexId2 empty.
                return !(!multiplexIdFirst.startsWith(multiplexIdSecond) && !multiplexIdSecond.startsWith(multiplexIdFirst));
            }
            if (multiplexId2SetBoth) {
                // Both samples have multiplexId2 set but one or both samples have multiplexId empty.
                return !(!multiplexId2First.startsWith(multiplexId2Second) && !multiplexId2Second.startsWith(multiplexId2First));
            }
        } else {
            if (multiplexIdAndMultiplexId2SetBoth) {
                // Both samples have multiplexId and multiplexId2 set.
                return multiplexIdsConcatenationFirst.equalsIgnoreCase(multiplexIdsConcatenationSecond);
            }
            if (multiplexIdSetBoth) {
                // Both samples have multiplexId set but one or both samples have multiplexId2 empty.
                return multiplexIdFirst.equalsIgnoreCase(multiplexIdSecond);
            }
            if (multiplexId2SetBoth) {
                // Both samples have multiplexId2 set but one or both samples have multiplexId empty.
                return multiplexId2First.equalsIgnoreCase(multiplexId2Second);
            }
        }
        // The uniqueness check is skipped or the multiplex ids are not correct.
        return false;
    }

    private boolean checkMultiplexIdsConflictBasic(Map.Entry<Sample, String> firstSampleMultiplexIdsConcatenationEntry, Map.Entry<Sample, String> secondSampleMultiplexIdsConcatenationEntry) {
        String multiplexIdFirst = firstSampleMultiplexIdsConcatenationEntry.getKey().getMultiplexId();
        if (multiplexIdFirst != null) {
            multiplexIdFirst = multiplexIdFirst.toLowerCase();
        }
        String multiplexIdSecond = secondSampleMultiplexIdsConcatenationEntry.getKey().getMultiplexId();
        if (multiplexIdSecond != null) {
            multiplexIdSecond = multiplexIdSecond.toLowerCase();
        }
        if (StringHelper.isNotEmpty(multiplexIdFirst) && StringHelper.isNotEmpty(multiplexIdSecond)) {
            if (multiplexIdFirst.length() == multiplexIdSecond.length()) {
                // Labels are not empty and have the same length, i.e., no substring check is needed.
                return multiplexIdFirst.equalsIgnoreCase(multiplexIdSecond);
            }
            // Labels are not empty and of different length, hence an additional substring (prefix) check is needed.
            return multiplexIdFirst.startsWith(multiplexIdSecond) || multiplexIdSecond.startsWith(multiplexIdFirst);
        }
        // The uniqueness check is skipped or the labels are not correct.
        return false;
    }

    public boolean checkUniqueTubeId(Sample entity) {
        return checkUniqueAttributeValue(entity, "tubeId", entity.getTubeId());
    }

    private EntityQuery createAvailableSamplesByContainersAndSampleTypeQuery(Collection<Container> containers, SampleType sampleType) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("container IN :containers");
        entityQuery.addParameter("containers", containers);
        if (sampleType != null) {
            entityQuery.addWhereClause("type = :type ");
            entityQuery.addParameter("type", sampleType.getName());
        }
        entityQuery.setOrder("id");
        return entityQuery;
    }

    public Sample createReplacement(Sample sample) {
        if (sample != null) {
            try {
                Sample sampleReplacement = sample.clone();
                sampleReplacement.setReplaces(sample);
                long i = 1;
                boolean nonUnique = true;
                while (nonUnique) {
                    sampleReplacement.setName(sample.getName() + "_" + Messages.get("replacement") + "_" + i);
                    if (checkUniqueName(sampleReplacement)) {
                        nonUnique = false;
                    } else {
                        i++;
                    }
                }
                i = getNextTubeIdSuffix(sampleReplacement.getContainer().getId());
                nonUnique = true;
                while (nonUnique) {
                    sampleReplacement.setTubeIdBySuffix(i);
                    if (checkUniqueTubeId(sampleReplacement)) {
                        nonUnique = false;
                    } else {
                        i++;
                    }
                }
                save(sampleReplacement);
                save(sample);
                AJAX.update("@this", "processesSamples", "orderItemsGroup");
                return sampleReplacement;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public void exclude(Sample sample) {
        if (sample != null) {
            sample.setUserDecision(SampleUserDecisionEnum.EXCLUDED);
            save(sample);
            AJAX.update("@this", "processesSamples", "orderItemsGroup");
        }
    }

    public void excludeAll(Collection<Sample> samples) {
        if (samples != null) {
            for (Sample sample : samples) {
                sample.setUserDecision(SampleUserDecisionEnum.EXCLUDED);
                save(sample);
            }
            AJAX.update("@this", "processesSamples", "orderItemsGroup");
        }
    }

    public BfabricLazyDataModel<Sample> getAvailableSamplesByContainersAndSampleType(Collection<Container> containers, SampleType sampleType) {
        return new BfabricLazyDataModel<>(createAvailableSamplesByContainersAndSampleTypeQuery(containers, sampleType));
    }

    public List<Sample> getAvailableSamplesByContainersAndSampleTypeNonLazy(Collection<Container> containers, SampleType sampleType) {
        return (List<Sample>) createAvailableSamplesByContainersAndSampleTypeQuery(containers, sampleType).getResultList();
    }

    /**
     * Get the "common" tubeId, i.e., 'pXZY_ABC/1' or 'oXZY/1' results in 'pXZY_ABC' respectively 'oXZY' of the given samples
     * Important: Iff all samples have a not empty tubeId as well as the same "common" tubeId, said "common" tubeId is returned, else null.
     */
    private String getCommonTubeIdFromSamples(Set<Sample> samples) {
        Set<String> commonTubeIds = new HashSet<>();
        for (Sample sampleInMultiplex : samples) {
            if (commonTubeIds.size() > 1 || StringHelper.isEmpty(sampleInMultiplex.getTubeId())) {
                return null;
            }
            int lastIndex = sampleInMultiplex.getTubeId().lastIndexOf("/");
            if (lastIndex > -1) {
                commonTubeIds.add(sampleInMultiplex.getTubeId().substring(0, lastIndex));
            }
        }
        if (commonTubeIds.size() == 1 && StringHelper.isNotEmpty(commonTubeIds.iterator().next())) {
            return commonTubeIds.iterator().next();
        }
        return null;
    }

    public BfabricLazyDataModel<Sample> getLabeledAndMultiplexedByTypeLazyModel(String libraryType, boolean excludeOrderItemSamples) {
        if (Constants.ILLUMINA.equals(libraryType)) {
            return getLazyModelByTypes(Arrays.asList(SampleTypeEnum.ILLUMINA_LIBRARY.getLabel(), SampleTypeEnum.ILLUMINA_MULTIPLEXED.getLabel()), excludeOrderItemSamples);
        }
        if (Constants.NANOPORE.equals(libraryType)) {
            return getLazyModelByTypes(Arrays.asList(SampleTypeEnum.NANOPORE_LIBRARY.getLabel(), SampleTypeEnum.NANOPORE_MULTIPLEXED.getLabel(), SampleTypeEnum.ONT_READY_MADE_LIBRARY.getLabel(), SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED.getLabel()), excludeOrderItemSamples);
        }
        if (Constants.PACBIO.equals(libraryType)) {
            return getLazyModelByTypes(Arrays.asList(SampleTypeEnum.PACBIO_LIBRARY.getLabel(), SampleTypeEnum.PACBIO_MULTIPLEXED.getLabel()), excludeOrderItemSamples);
        }
        if (Constants.MS_SAMPLE.equals(libraryType)) {
            return getLazyModelByTypes(Arrays.asList(SampleTypeEnum.MS_SAMPLE_LABELED.getLabel(), SampleTypeEnum.MS_SAMPLE_MULTIPLEXED.getLabel()), excludeOrderItemSamples);
        }
        return getLazyModelByTypes(null, excludeOrderItemSamples);
    }

    public BfabricLazyDataModel<Sample> getLabeledSamplesBySampleTypeEnum(SampleTypeEnum sampleTypeEnum) {
        return getLabeledSamplesByType(sampleTypeEnum != null ? sampleTypeEnum.getLabel() : null);
    }

    public BfabricLazyDataModel<Sample> getLabeledSamplesByType(String type) {
        EntityQuery entityQuery = createEntityQuery();
        if (StringHelper.isNotEmpty(type)) {
            entityQuery.addWhereClause("multiplexId <> null and type = :type");
            entityQuery.addParameter("type", type);
        } else {
            entityQuery.addWhereClause("multiplexId <> null");
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    @Override
    public BfabricLazyDataModel<Sample> getLazyModel() {
        return new BfabricLazyDataModel<>(createEntityQuery());
    }

    public BfabricLazyDataModel<Sample> getLazyModelByAnnotation(Annotation annotation) {
        SampleAttributeEnum sampleAttributeEnum = SampleAttributeEnum.getAttributeByLabel(annotation.getType(), Annotation.class);
        if (sampleAttributeEnum != null) {
            EntityQuery entityQuery = createEntityQuery();
            if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                entityQuery.addWhereClause(sampleAttributeEnum.getName() + " = :annotation");
                entityQuery.addParameter("annotation", annotation);
            } else if (sampleAttributeEnum.isAnnotationTypeMultiValued()) {
                entityQuery.setJoin("entity." + sampleAttributeEnum.getName() + " annotation");
                entityQuery.addWhereClause("annotation = :annotation");
                entityQuery.addParameter("annotation", annotation);
            } else {
                // No result.
                entityQuery = null;
            }
            return entityQuery != null ? new BfabricLazyDataModel<>(entityQuery) : null;
        }
        return null;
    }

    @Override
    public BfabricLazyDataModel<Sample> getLazyModelByContainerId(long containerId, Collection<? extends Container> associatedContainers, boolean all) {
        return new BfabricLazyDataModel<>(createEntityQueryByContainerId(containerId, associatedContainers, all));
    }

    @Override
    public BfabricLazyDataModel<Sample> getLazyModelByContainerId(long containerId) {
        return new BfabricLazyDataModel<>(createEntityQueryByContainerId(containerId, null, false));
    }

    public BfabricLazyDataModel<Sample> getLazyModelByContainerIdAndTypes(long containerId, Collection<String> types) {
        EntityQuery entityQuery = createEntityQueryByContainerId(containerId, null, false);
        if (types != null) {
            entityQuery.setParenthesisAroundWhere();
            entityQuery.addWhereClause("entity.type IN (:types)");
            entityQuery.addParameter("types", types);
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByControlSampleId(long controlSampleId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("controlSample.id = :controlSampleId");
        entityQuery.addParameter("controlSampleId", controlSampleId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByInstrumentReadConfigurationId(long instrumentReadConfigurationId) {
        return (BfabricLazyDataModel<Sample>) getLazyModelUnnestById("orderItems", "order.instrumentReadConfiguration", instrumentReadConfigurationId);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByInstrumentReadConfigurationIdAndTypes(long instrumentReadConfigurationId, Collection<String> types, boolean excludeOrderItemSamples) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("EXISTS(SELECT o.id FROM Order o WHERE o.id = entity.container.id AND o.instrumentReadConfiguration.id = :instrumentReadConfigurationId)");
        entityQuery.addParameter("instrumentReadConfigurationId", instrumentReadConfigurationId);
        entityQuery.addWhereClause("entity.type IN (:types)");
        entityQuery.addParameter("types", types != null ? types : new ArrayList<>());
        if (excludeOrderItemSamples) {
            entityQuery.addWhereClause("NOT EXISTS(SELECT oi.id FROM entity.orderItems oi WHERE oi.sample.id = entity.id)");
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByOrphansOnlyAndControlOnly(Boolean showOrphansOnly, Boolean showControlOnly, Boolean filterQcPassed, Boolean filterUserDecision) {
        EntityQuery entityQuery = createEntityQuery();
        if (showOrphansOnly != null && showOrphansOnly) {
            entityQuery.addWhereClause("entity.parents is empty");
        }
        if (showControlOnly != null && showControlOnly) {
            entityQuery.addWhereClause("entity.type = '" + SampleTypeEnum.CONTROL_SAMPLE.getLabel() + "'");
        }
        if (filterQcPassed != null) {
            entityQuery.addWhereClause("entity.qcPassed IS " + filterQcPassed);
        }
        if (filterUserDecision != null) {
            if (filterUserDecision) {
                entityQuery.addWhereClause("entity.userDecision IS NULL OR entity.userDecision = '" + SampleUserDecisionEnum.PROCEED + "'");
            } else {
                entityQuery.addWhereClause("entity.userDecision IS NOT NULL AND entity.userDecision <> '" + SampleUserDecisionEnum.PROCEED + "'");
            }
        }
        entityQuery.setOrder("entity.tubeId");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByRunId(long runId) {
        return (BfabricLazyDataModel<Sample>) getLazyModelUnnestById("runs", runId);
    }

    public BfabricLazyDataModel<Sample> getLazyModelBySamplePreparationProtocolId(long samplePreparationProtocolId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("samplePreparationProtocol.id = :samplePreparationProtocolId");
        entityQuery.addParameter("samplePreparationProtocolId", samplePreparationProtocolId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByServiceTypeId(long serviceTypeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.container.serviceType.id = :serviceTypeId");
        entityQuery.addParameter("serviceTypeId", serviceTypeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByType(String type) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("type = :type");
        entityQuery.addParameter("type", type);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByTypeId(long typeId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("sampleType.id = :typeId");
        entityQuery.addParameter("typeId", typeId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByTypes(Collection<String> types, boolean excludeOrderItemSamples) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.type IN (:types)");
        entityQuery.addParameter("types", types != null ? types : new ArrayList<>());
        entityQuery.addWhereClause((excludeOrderItemSamples ? "NOT " : Constants.EMPTY_STRING) + "EXISTS(SELECT oi.id FROM entity.orderItems oi WHERE oi.sample.id = entity.id)");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLazyModelByTypes(Collection<String> types) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("type IN (:types)");
        entityQuery.addParameter("types", types != null ? types : new ArrayList<>());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<?> getLazyModelByUserId(long userId) {
        return getLazyModelContainerDependentByUserId(userId);
    }

    public BfabricLazyDataModel<Sample> getLazyModelExcludingTypes(Collection<String> exclude) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("type NOT IN (:exclude)");
        entityQuery.addParameter("exclude", exclude != null ? exclude : new ArrayList<>());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Sample> getLibrariesLazyModel() {
        return getLazyModelByTypes(SampleTypeEnum.getLabeledLabels());
    }

    /**
     * Get the multiplex id conflict records for the given lanePosition and samples.
     * Important: The samples need to have passed the multiplex ids correctness check first (depending on the multiplexIdCheckType, getMultiplexIdsCorrectnessErrorMapGenomics or getMultiplexIdsCorrectnessErrorMapProteomics must be empty), before checking for uniqueness.
     * <p>
     * Multiplex IDs combinations:
     * - multiplexId and multiplexId2 are not empty and of the same length, e.g. s1=(b1: AA, b2: CC) and s2=(b1: TT, b2: GG):
     * -- Concatenation of multiplexId and multiplexId2 needs to be unique:
     * ---- s1=(b1: AA, b2: CC) --> AACC
     * ---- s2=(b1: TT, b2: GG) --> TTGG --> UNIQUE
     * ---- Remark: No substring test needed because AA,CC and AA is not possible as it would be a multiplex id mismatch
     * - multiplexId xor multiplexId2 is defined:
     * -- multiplexId or multiplexId2 cannot be equals and are not allowed to be a substring:
     * ---- s1=(b1: AA, b2: empty) --> AA
     * ---- s1=(b1: AACC, b2: empty) --> AACC --> NOT UNIQUE
     * ---- Remark: AA is not equals AACC, but AA is a substring of AACC, hence it is not allowed
     * - Index name case:
     * ---- s1=(b1: SI-GA-A1, b2: empty) --> SI-GA-A1
     * ---- s1=(b1: SI-GA-A10, b2: empty) --> SI-GA-A10 --> UNIQUE
     * ---- Remark: Fall back option (worst case): If there are other characters than ACTG, then skip the uniqueness check for the moment
     * <p>
     * Rule of thumb:
     * - If the multiplex ids have the same combination and same length --> concatenation is enough (in all other cases: substring)
     * - If the sample is a multiplex --> treat them as single samples (compare them all)
     */
    private Set<MultiplexIdConflictRecord> getMultiplexIdConflictRecords(int lanePosition, Set<Sample> samples, String multiplexIdCheckType) {
        Set<MultiplexIdConflictRecord> multiplexIdConflictRecords = new HashSet<>();
        if (samples != null && !samples.isEmpty()) {
            Map<Sample, String> samplesMultiplexIdsConcatenationMap = new HashMap<>();
            for (Sample sample : samples) {
                if (sample.isMultiplexedType() || sample.isMultiplexType()) {
                    for (Sample parentSample : sample.getParents()) {
                        parentSample.setMultiplexIdConflictMultiplexedSample(sample);
                        if (Constants.MULTIPLEX_ID_CHECK_ADVANCED.equals(multiplexIdCheckType)) {
                            samplesMultiplexIdsConcatenationMap.put(parentSample, parentSample.getMultiplexIdAndMultiplexId2Concatenation(Constants.MULTIPLEX_ID_UNIQUENESS_CHECK_DELIMITER));
                        } else if (Constants.MULTIPLEX_ID_CHECK_BASIC.equals(multiplexIdCheckType)) {
                            samplesMultiplexIdsConcatenationMap.put(parentSample, StringHelper.isNotEmpty(parentSample.getMultiplexId()) ? parentSample.getMultiplexId() : Constants.EMPTY_STRING);
                        }
                    }
                } else {
                    if (Constants.MULTIPLEX_ID_CHECK_ADVANCED.equals(multiplexIdCheckType)) {
                        samplesMultiplexIdsConcatenationMap.put(sample, sample.getMultiplexIdAndMultiplexId2Concatenation(Constants.MULTIPLEX_ID_UNIQUENESS_CHECK_DELIMITER));
                    } else if (Constants.MULTIPLEX_ID_CHECK_BASIC.equals(multiplexIdCheckType)) {
                        samplesMultiplexIdsConcatenationMap.put(sample, StringHelper.isNotEmpty(sample.getMultiplexId()) ? sample.getMultiplexId() : Constants.EMPTY_STRING);
                    }
                }
            }
            Set<Sample> notUniqueOuter = new HashSet<>();
            Set<Sample> notUniqueInner = new HashSet<>();
            Map<Sample, Set<Sample>> notUniqueGrouped = new HashMap<>();
            Pattern pattern = Pattern.compile(Constants.MULTIPLEX_ID_ACTG_REGEXP);
            for (Map.Entry<Sample, String> outerLoopSample : samplesMultiplexIdsConcatenationMap.entrySet()) {
                for (Map.Entry<Sample, String> innerLoopSample : samplesMultiplexIdsConcatenationMap.entrySet()) {
                    // Do not compare the same sample with itself
                    if (!outerLoopSample.getKey().equals(innerLoopSample.getKey()) && checkMultiplexIdsConflict(outerLoopSample, innerLoopSample, pattern, multiplexIdCheckType)) {
                        // Conflicting multiplex ids.
                        if (notUniqueOuter.contains(outerLoopSample.getKey())) {
                            // outerLoopSample.getKey() is already a key in notUniqueOuter
                            if (!notUniqueOuter.contains(innerLoopSample.getKey())) {
                                notUniqueGrouped.get(outerLoopSample.getKey()).add(innerLoopSample.getKey());
                                notUniqueOuter.add(outerLoopSample.getKey());
                                notUniqueInner.add(innerLoopSample.getKey());
                            }
                        } else {
                            // outerLoopSample.getKey() is not yet a key in notUniqueInner
                            if (!notUniqueInner.contains(outerLoopSample.getKey()) && !notUniqueOuter.contains(innerLoopSample.getKey())) {
                                notUniqueGrouped.put(outerLoopSample.getKey(), new HashSet<>());
                                notUniqueGrouped.get(outerLoopSample.getKey()).add(innerLoopSample.getKey());
                                notUniqueOuter.add(outerLoopSample.getKey());
                                notUniqueInner.add(innerLoopSample.getKey());
                            }
                        }
                    }
                }
            }
            for (Map.Entry<Sample, Set<Sample>> entry : notUniqueGrouped.entrySet()) {
                Set<Sample> notUniqueGroup = new HashSet<>();
                notUniqueGroup.add(entry.getKey());
                notUniqueGroup.addAll(entry.getValue());
                multiplexIdConflictRecords.add(new MultiplexIdConflictRecord(lanePosition, notUniqueGroup));
            }
        }
        return multiplexIdConflictRecords;
    }

    public Set<MultiplexIdConflictRecord> getMultiplexIdConflictRecords(Map<Integer, Set<Sample>> lanePositionSamplesMap, Set<Sample> samples, String multiplexIdCheckType) {
        Set<MultiplexIdConflictRecord> multiplexIdConflictRecords = new HashSet<>();
        if (lanePositionSamplesMap != null) {
            for (Map.Entry<Integer, Set<Sample>> lanePositionSamplesMapEntry : lanePositionSamplesMap.entrySet()) {
                multiplexIdConflictRecords.addAll(getMultiplexIdConflictRecords(lanePositionSamplesMapEntry.getKey(), lanePositionSamplesMapEntry.getValue(), multiplexIdCheckType));
            }
        } else if (samples != null) {
            multiplexIdConflictRecords.addAll(getMultiplexIdConflictRecords(-1, samples, multiplexIdCheckType));
        }
        return multiplexIdConflictRecords;
    }

    public Map<String, Object> getMultiplexIdsCorrectnessErrorMap(Set<Sample> samples, boolean skipMultiplex, boolean sampleRunAssignment, String multiplexIdCheckType) {
        if (Constants.MULTIPLEX_ID_CHECK_ADVANCED.equals(multiplexIdCheckType)) {
            return getMultiplexIdsCorrectnessErrorMapAdvanced(samples, skipMultiplex, sampleRunAssignment);
        }
        if (Constants.MULTIPLEX_ID_CHECK_BASIC.equals(multiplexIdCheckType)) {
            return getMultiplexIdsCorrectnessErrorMapBasic(samples, skipMultiplex);
        }
        return new HashMap<>();
    }

    /**
     * Check the multiplex ids correctness for the given samples and return the correctness error map containing various information.
     * - Potential values of multiplexId and multiplexId2:
     * -- multiplexId and multiplexId2 are empty --> NOT ALLOWED
     * -- multiplexId is not empty and multiplexId2 is empty:
     * --- Standalone: s1=(b1: TT, b2: empty) --> ALLOWED
     * --- Mixed:
     * ----- s1=(b1: TT, b2: empty)
     * ----- s2=(b1: CC, b2: empty) ALLOWED (same pattern for all samples)
     * ----- s1=(b1: TT, b2: empty)
     * ----- s2=(b1: CC, b2: GG) --> NOT ALLOWED (mixed pattern) (sampleRunAssignment=false) / ALLOWED (sampleRunAssignment=true)
     * --- multiplexId is empty and multiplexId2 is not empty --> SAME AS ABOVE
     * - Multiplex ID length:
     * -- s1=(b1: TT, b2: C) --> ALLOWED
     * -- s1=(b1: TT, b2: G)
     * -- s2=(b1: TT, b2: AA) --> ALLOWED as index names can be of different length, e.g., SI-GA-A1 & SI-GA-A10
     * -- Mixed multiplex ids, i.e., some samples have 'ACTG' multiplex ids only while others do not --> NOT ALLOWED (sampleRunAssignment=false) / ALLOWED (sampleRunAssignment=true)
     */
    private Map<String, Object> getMultiplexIdsCorrectnessErrorMapAdvanced(Set<Sample> samples, boolean skipMultiplex, boolean sampleRunAssignment) {
        Map<String, Object> multiplexIdsCorrectnessErrorMap = new HashMap<>();
        multiplexIdsCorrectnessErrorMap.put(Constants.MULTIPLEX_ID_INCOMPLETE_KEY, new HashSet<Sample>());
        multiplexIdsCorrectnessErrorMap.put(Constants.MULTIPLEX_ID_MISMATCH_KEY, null);
        multiplexIdsCorrectnessErrorMap.put(Constants.MULTIPLEX_ID_MIXED_KEY, null);
        int[] sampleMultiplexIdsCounters = { 0, 0 };
        if (samples != null && !samples.isEmpty()) {
            boolean isMultiplexIdSet = false;
            boolean isMultiplexId2Set = false;
            boolean isMultiplexIdAndMultiplexId2NotSet = false;
            int multiplexCounter = 0;
            for (Sample sample : samples) {
                if (SampleTypeEnum.requiresMultiplexIdCheck(sample.getType())) {
                    if (sample.isMultiplexedType()) {
                        if (!skipMultiplex) {
                            Sample parentSample = sample.getParents().iterator().next();
                            isMultiplexIdSet = StringHelper.isNotEmpty(parentSample.getMultiplexId());
                            isMultiplexId2Set = StringHelper.isNotEmpty(parentSample.getMultiplexId2());
                            break;
                        }
                        multiplexCounter++;
                    } else {
                        isMultiplexIdSet = StringHelper.isNotEmpty(sample.getMultiplexId());
                        isMultiplexId2Set = StringHelper.isNotEmpty(sample.getMultiplexId2());
                        break;
                    }
                }
            }
            if (skipMultiplex && samples.size() == multiplexCounter) {
                // Only multiplexes which are skipped, i.e., everything is correct.
                multiplexIdsCorrectnessErrorMap.clear();
                return multiplexIdsCorrectnessErrorMap;
            }
            if (!isMultiplexIdSet && !isMultiplexId2Set) {
                isMultiplexIdAndMultiplexId2NotSet = true;
            }
            Pattern pattern = Pattern.compile(Constants.MULTIPLEX_ID_ACTG_REGEXP);
            for (Sample sample : samples) {
                if (SampleTypeEnum.requiresMultiplexIdCheck(sample.getType())) {
                    if (sample.isMultiplexedType()) {
                        if (!skipMultiplex) {
                            for (Sample parentSample : sample.getParents()) {
                                if (parentSample.isMultiplexIdAndMultiplexId2Empty()) {
                                    parentSample.setMultiplexIdConflictMultiplexedSample(sample);
                                    ((Set<Sample>) multiplexIdsCorrectnessErrorMap.get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)).add(parentSample);
                                } else if (!sampleRunAssignment && !isMultiplexIdAndMultiplexId2NotSet && multiplexIdsCorrectnessErrorMap
                                    .get(Constants.MULTIPLEX_ID_MISMATCH_KEY) == null && (isMultiplexIdSet != StringHelper
                                    .isNotEmpty(parentSample.getMultiplexId()) || isMultiplexId2Set != StringHelper.isNotEmpty(parentSample.getMultiplexId2()))) {
                                    multiplexIdsCorrectnessErrorMap
                                        .put(Constants.MULTIPLEX_ID_MISMATCH_KEY, Messages.get("multiplexIdMismatchErrorMessage") + " (" + Messages.get("multiplexIdMismatchErrorMessageHint") + ")");
                                }
                                if (!sampleRunAssignment) {
                                    updateMultiplexIdsCounters(parentSample, pattern, sampleMultiplexIdsCounters);
                                }
                            }
                        }
                    } else {
                        if (sample.isMultiplexIdAndMultiplexId2Empty()) {
                            ((Set<Sample>) multiplexIdsCorrectnessErrorMap.get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)).add(sample);
                        } else if (!sampleRunAssignment && !isMultiplexIdAndMultiplexId2NotSet && multiplexIdsCorrectnessErrorMap
                            .get(Constants.MULTIPLEX_ID_MISMATCH_KEY) == null && (isMultiplexIdSet != StringHelper
                            .isNotEmpty(sample.getMultiplexId()) || isMultiplexId2Set != StringHelper.isNotEmpty(sample.getMultiplexId2()))) {
                            multiplexIdsCorrectnessErrorMap
                                .put(Constants.MULTIPLEX_ID_MISMATCH_KEY, Messages.get("multiplexIdMismatchErrorMessage") + " (" + Messages.get("multiplexIdMismatchErrorMessageHint") + ")");
                        }
                        if (!sampleRunAssignment) {
                            updateMultiplexIdsCounters(sample, pattern, sampleMultiplexIdsCounters);
                        }
                    }
                }
            }
        }
        if (((Set<Sample>) multiplexIdsCorrectnessErrorMap.get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)).isEmpty()) {
            multiplexIdsCorrectnessErrorMap.remove(Constants.MULTIPLEX_ID_INCOMPLETE_KEY);
        }
        if (multiplexIdsCorrectnessErrorMap.get(Constants.MULTIPLEX_ID_MISMATCH_KEY) == null) {
            multiplexIdsCorrectnessErrorMap.remove(Constants.MULTIPLEX_ID_MISMATCH_KEY);
        }
        if (!sampleRunAssignment && sampleMultiplexIdsCounters[0] != 0 && sampleMultiplexIdsCounters[1] != 0) {
            multiplexIdsCorrectnessErrorMap.put(Constants.MULTIPLEX_ID_MIXED_KEY, Messages.get("multiplexIdMixedErrorMessage") + " (" + Messages.get("multiplexIdMixedErrorMessageHint") + ")");
        } else {
            multiplexIdsCorrectnessErrorMap.remove(Constants.MULTIPLEX_ID_MIXED_KEY);
        }
        return multiplexIdsCorrectnessErrorMap;
    }

    /**
     * Check the multiplex ids correctness for the given samples and return the correctness error map containing various information.
     * - Potential values of multiplexId and multiplexId2:
     * -- multiplexId is empty --> NOT ALLOWED
     */
    private Map<String, Object> getMultiplexIdsCorrectnessErrorMapBasic(Set<Sample> samples, boolean skipMultiplex) {
        Map<String, Object> labelCorrectnessErrorMap = new HashMap<>();
        labelCorrectnessErrorMap.put(Constants.MULTIPLEX_ID_INCOMPLETE_KEY, new HashSet<Sample>());
        if (samples != null && !samples.isEmpty()) {
            int poolCounter = 0;
            for (Sample sample : samples) {
                if (sample.isMultiplexType()) {
                    if (!skipMultiplex) {
                        for (Sample parentSample : sample.getParents()) {
                            if (StringHelper.isEmpty(parentSample.getMultiplexId())) {
                                parentSample.setMultiplexIdConflictMultiplexedSample(sample);
                                ((Set<Sample>) labelCorrectnessErrorMap.get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)).add(parentSample);
                            }
                        }
                    }
                    poolCounter++;
                } else {
                    if (StringHelper.isEmpty(sample.getMultiplexId())) {
                        ((Set<Sample>) labelCorrectnessErrorMap.get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)).add(sample);
                    }
                }
            }
            if (skipMultiplex && samples.size() == poolCounter) {
                // Only multiplexes which are skipped, i.e., everything is correct.
                labelCorrectnessErrorMap.clear();
                return labelCorrectnessErrorMap;
            }
        }
        if (((Set<Sample>) labelCorrectnessErrorMap.get(Constants.MULTIPLEX_ID_INCOMPLETE_KEY)).isEmpty()) {
            labelCorrectnessErrorMap.remove(Constants.MULTIPLEX_ID_INCOMPLETE_KEY);
        }
        return labelCorrectnessErrorMap;
    }

    public List<Sample> getPotentialReplacesFiltered(String filterString, Sample exclude) {
        if (exclude != null) {
            EntityQuery entityQuery = createEntityQueryFiltered(filterString);
            entityQuery.addWhereClause("entity.container.id = :containerId");
            entityQuery.addParameter("containerId", exclude.getContainer().getId());
            if (exclude.getId() > 0) {
                entityQuery.addWhereClause("entity.id <> :exclude");
                entityQuery.addParameter("exclude", exclude.getId());
            }
            entityQuery.addIdOrNameWhereClause(filterString);
            entityQuery.setOrder("id DESC");
            return (List<Sample>) entityQuery.getResultList();
        }
        return new ArrayList<>();
    }

    public List<Sample> getSampleReplacementsByContainerId(Long containerId) {
        return createNamedQuery("Sample.findReplacementsByContainerId").setParameter("containerId", containerId).getResultList();
    }

    public BfabricLazyDataModel<Sample> getSamplesByAnnotation(Annotation annotation) {
        if (annotation != null) {
            EntityQuery entityQuery = createEntityQuery();
            SampleAttributeEnum sampleAttributeEnum = SampleAttributeEnum.getAttributeByLabel(annotation.getType(), Annotation.class);
            List<String> whereClauses = new ArrayList<>();
            List<String> joinClauses = new ArrayList<>();
            if (sampleAttributeEnum != null) {
                if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                    whereClauses.add(sampleAttributeEnum.getName() + ".id = :annotationId");
                } else if (sampleAttributeEnum.isAnnotationTypeMultiValued()) {
                    joinClauses.add(" entity." + sampleAttributeEnum.getName() + " " + sampleAttributeEnum.getName() + "_");
                    whereClauses.add(sampleAttributeEnum.getName() + "_.id = :annotationId");
                }
            }
            if (!whereClauses.isEmpty()) {
                entityQuery.setJoin(CollectionHelper.print(joinClauses, " "));
                entityQuery.addWhereClause(CollectionHelper.print(whereClauses, " and "));
                entityQuery.addParameter("annotationId", annotation.getId());
                return new BfabricLazyDataModel<>(entityQuery);
            }
            return new BfabricLazyDataModel<>();
        }
        return null;
    }

    public List<Sample> getSamplesByContainer(String filterString, Container container, Collection<Sample> excluded) {
        getEntityManager().setFlushMode(FlushModeType.COMMIT);
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("container = :container");
        entityQuery.addParameter("container", container);
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<Sample>) entityQuery.getResultList();
    }

    public List<Sample> getSamplesByContainerAndSampleType(Container container, SampleType sampleType) {
        return createNamedQuery("Sample.findByContainerAndType").setParameter("type", sampleType.getName()).setParameter("container", container).getResultList();
    }

    public Long getSamplesByContainerIdCount(Long containerId) {
        return (Long) createNamedQuery("Sample.countByContainerId").setParameter("containerId", containerId).getSingleResult();
    }

    public List<Sample> getSamplesByRunId(Long runId) {
        return createNamedQuery("Sample.findByRunId").setParameter("runId", runId).getResultList();
    }

    public List<Sample> getSamplesFilteredByContainersExcluding(String filterString, Collection<Container> containers, Collection<Sample> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        if (containers != null && !containers.isEmpty()) {
            entityQuery.addWhereClause("container in (:containers)");
            entityQuery.addParameter("containers", containers);
        }
        return (List<Sample>) entityQuery.getResultList();
    }

    public List<Sample> getSamplesUserDecisionRequired(Long containerId) {
        return createNamedQuery("Sample.findByContainerIdAndUserDecisionRequired").setParameter("containerId", containerId).getResultList();
    }

    public void handleParentSamplesOfUserMultiplex(Sample sample) {
        /*
         * IMPORTANT: For this method to work properly, the initialParentSamplesOfUserMultiplex has to be initialized.
         * Do not remove this hint for potential future bug fixing. Cascade operations:
         * - children: cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE } --> methods causing cascade: merge, persist, remove, and find as merge is called in the find method
         * - parents: cascade = { CascadeType.MERGE }                                           --> methods causing cascade: merge and remove as merge is called in the remove method
         */
        if (!sample.isManaged()) {
            // initialParentSamplesOfUserMultiplex is empty as the sample is not managed.
            if (sample.isNotMultiplexed()) {
                // Simply remove the parents of type 'User Library in Pool' to avoid cascade operations.
                sample.removeParentSamplesOfUserMultiplex();
            } else {
                // Set and/or update all the necessary values for persisting the created sample(s).
                int parentSamplesOfUserMultiplexSize = sample.getParentSamplesOfUserMultiplex().size();
                for (int i = 0; i < parentSamplesOfUserMultiplexSize; i++) {
                    sample.getParentSamplesOfUserMultiplex().get(i).setNamePrefix(sample.createNamePrefixForParentOfUserSampleInMultiplex(i + 1, parentSamplesOfUserMultiplexSize));
                    sample.getParentSamplesOfUserMultiplex().get(i).setFinalNameForSampleOfTypeUserSampleInMultiplex();
                    super.save(sample.getParentSamplesOfUserMultiplex().get(i));
                }
            }
        } else {
            Set<Sample> initialParentSamplesOfUserMultiplex = sample.getInitialParentSamplesOfUserMultiplex();
            if (sample.isNotMultiplexed()) {
                // Simply remove the parents of type 'User Library in Pool' to avoid cascade operations.
                sample.removeParentSamplesOfUserMultiplex();
                // Delete the initialParentSamplesOfUserMultiplex as the sample is not a multiplexed sample (anymore).
                for (Sample parentSample : initialParentSamplesOfUserMultiplex) {
                    parentSample.getChildren().clear();
                    parentSample.getParents().clear();
                    super.remove(parentSample);
                }
            } else {
                // Delete the initial parents which have been removed.
                for (Sample initialParentOfMultiplex : initialParentSamplesOfUserMultiplex) {
                    if (!sample.getParents().contains(initialParentOfMultiplex)) {
                        // Delete the initialParentOfMultiplex as it is not anymore in the parents.
                        initialParentOfMultiplex.getChildren().clear();
                        initialParentOfMultiplex.getParents().clear();
                        super.remove(initialParentOfMultiplex);
                    }
                }
                // Persist the new parent samples and update the already existing ones.
                Set<Sample> samplesToPersist = new HashSet<>();
                Set<Sample> samplesToUpdate = new HashSet<>();
                List<Sample> allSystemGeneratedParentSamplesOfUserMultiplex = new ArrayList<>();
                for (Sample parentSample : sample.getParents()) {
                    if (!parentSample.isManaged()) {
                        samplesToPersist.add(parentSample);
                    } else {
                        samplesToUpdate.add(parentSample);
                    }
                    if (parentSample.isUserSampleInMultiplexType()) {
                        allSystemGeneratedParentSamplesOfUserMultiplex.add(parentSample);
                    }
                }
                //allSystemGeneratedParentSamplesOfUserMultiplex.sort(Comparator.comparing(Sample::getName));
                allSystemGeneratedParentSamplesOfUserMultiplex.sort(Comparator.comparing(Sample::getNamePrefix));
                int allSystemGeneratedParentSamplesOfUserMultiplexSize = allSystemGeneratedParentSamplesOfUserMultiplex.size();
                for (int i = 0; i < allSystemGeneratedParentSamplesOfUserMultiplex.size(); i++) {
                    Sample systemGeneratedParentOfMultiplex = allSystemGeneratedParentSamplesOfUserMultiplex.get(i);
                    //systemGeneratedParentOfMultiplex.setName(sample.createNameForParentSampleOfTypeUserSampleInMultiplex(i + 1, allSystemGeneratedParentSamplesOfUserMultiplexSize));
                    systemGeneratedParentOfMultiplex
                        .setNamePrefix(sample.createNamePrefixForParentOfUserSampleInMultiplex(i + 1, allSystemGeneratedParentSamplesOfUserMultiplexSize));
                    systemGeneratedParentOfMultiplex.setFinalNameForSampleOfTypeUserSampleInMultiplex();
                }
                for (Sample sampleToPersist : samplesToPersist) {
                    super.persist(sampleToPersist);
                }
                for (Sample sampleToUpdate : samplesToUpdate) {
                    super.save(sampleToUpdate);
                }
            }
        }
    }

    public boolean hasLabeledSamplesByContainer(Container container) {
        return !createNamedQuery("Sample.findByContainerAndTypes").setParameter("types", SampleTypeEnum.getLabeledLabels()).setParameter("container", container).setMaxResults(1).getResultList()
            .isEmpty();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Sample sample = (Sample) entity;
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (!isValidName(sample.getName(), Collections.singleton(sample.getId()), sample.getContainer().getId())) {
            validationErrorMsg.put(Constants.EDIT + ":name", Messages.get("nameNotUniqueWithinContainerException"));
        }
        if (!sample.isManaged() && !sample.getSampleType().isEnabled()) {
            validationErrorMsg.put(null, Messages.get("sampleTypeIsNotEnabled").replace("{0}", sample.getSampleType().getName()));
        }
        if (!sample.getContainer().isExtensible()) {
            validationErrorMsg.put(null, Messages.get("containerIsNotExtensible").replace("{0}", sample.getContainer().getIdString()));
        }
        if (!sample.isMoved()) {
            for (Sample parent : sample.getParents()) {
                if (!sample.getContainer().equals(parent.getContainer()) && sample.isNotMultiplexed() && parent.isNotMultiplexed()) {
                    validationErrorMsg.put(null, Messages.get("parentSampleDoesNotBelongToContainer").replace("{0}", parent.getIdString()).replace("{1}", sample.getContainer().getIdString()));
                }
            }
            if (sample.getSampleType().getName().equals(SampleTypeEnum.CONTROL_SAMPLE.getLabel()) && sample.getControlSample() == null) {
                validationErrorMsg.put(Constants.EDIT + ":controlSample", Constants.REQUIRED);
            }
        }
        if (sample.getSampleType() != null) {
            for (final SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum
                .getAttributeEnums(true, sample, sample.getSampleType().getName(), true, sample.getSampleForm(), true, sample.getQualityControlType(), false, false)) {
                try {
                    final Object value = PropertyUtils.getProperty(sample, sampleAttributeEnum.getName());
                    if (sampleAttributeEnum.isAttributeRequired(sample.getType(), sample.getSampleForm(), sample.getQualityControlType()) && sampleAttributeEnum.isEmptySampleAttribute(value)) {
                        validationErrorMsg.put(Constants.EDIT + ":" + sampleAttributeEnum.getName(), Constants.REQUIRED);
                    }
                    // Every number needs to be >= 0. If this changes, this check needs to be extended.
                    if (value != null && sampleAttributeEnum.isNumericType() && Double.parseDouble(String.valueOf(value)) < 0) {
                        validationErrorMsg.put(Constants.EDIT + ":" + sampleAttributeEnum.getName(), Messages.get("notPositiveOrZero"));
                    }
                    // Check the multiplex ids if the sample is multiplexed and user submitted.
                    if (value != null && sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEXED)) {
                        String errorMessage = sample.isValidMultiplexedByUser();
                        if (errorMessage != null) {
                            validationErrorMsg.put(Constants.EDIT + ":" + sampleAttributeEnum.getName(), errorMessage);
                        }
                    }
                    if (value != null && !sampleAttributeEnum.isAnnotationType()) {
                        if (sampleAttributeEnum.isStringType() && !sampleAttributeEnum.equals(SampleAttributeEnum.DESCRIPTION)) {
                            MFHelper.checkLength(sampleAttributeEnum.getName().toLowerCase(), value.toString(), sampleAttributeEnum.getMaxLength());
                        } else if (sampleAttributeEnum.isBooleanType()) {
                            MFHelper.booleanValueOf(sampleAttributeEnum.getName().toLowerCase(), value.toString());
                        } else if (sampleAttributeEnum.isLocalDateType()) {
                            MFHelper.dateValueOf(sampleAttributeEnum.getName().toLowerCase(), value.toString());
                        } else if (sampleAttributeEnum.isDoubleType()) {
                            MFHelper.doubleValueOf(sampleAttributeEnum.getName().toLowerCase(), value.toString());
                        } else if (sampleAttributeEnum.isFloatType()) {
                            MFHelper.floatValueOf(sampleAttributeEnum.getName().toLowerCase(), value.toString());
                        } else if (sampleAttributeEnum.isIntegerType()) {
                            MFHelper.integerValueOf(sampleAttributeEnum.getName().toLowerCase(), value.toString());
                        } else if (sampleAttributeEnum.isLongType()) {
                            MFHelper.longValueOf(sampleAttributeEnum.getName().toLowerCase(), value.toString());
                        } else if (sampleAttributeEnum.isBigDecimalType()) {
                            MFHelper.bigDecimalValueOf(sampleAttributeEnum.getName().toLowerCase(), value.toString());
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException | SecurityException |
                         InvocationTargetException | NoSuchMethodException ignored) {
                } catch (InvalidDataException e) {
                    validationErrorMsg.put(Constants.EDIT + ":" + sampleAttributeEnum.getName(), e.getMessage());
                }
            }
        }
        validationErrorMsg.putAll(isValidCustomAttributes(sample));
        // Move specific validation.
        if (sample.isMoved()) {
            if (!sample.isMovable()) {
                validationErrorMsg.put(null, Messages.get("sampleIsNotMovable").replace("{0}", sample.getIdString()));
            }
            if (sample.getOldContainerId() == sample.getContainer().getId()) {
                validationErrorMsg.put(Constants.EDIT + ":container", Messages.get("targetContainerMustDiffer"));
            }
        }
        return validationErrorMsg;
    }

    public boolean isValidName(String sampleName, Collection<Long> excludedIds, Long containerId) {
        if (StringHelper.isNotEmpty(sampleName)) {
            StringBuilder query = new StringBuilder("select id from sample where containerId = :containerId and lower(name) = lower(:name) ");
            if (excludedIds != null && !excludedIds.isEmpty()) {
                query.append(" and id not in (").append(CollectionHelper.printBasic(excludedIds)).append(") ");
            }
            return createNativeQuery(query.toString()).setParameter("name", sampleName.trim()).setParameter("containerId", containerId).setMaxResults(1).getResultList().isEmpty();
        }
        return false;
    }

    public void proceed(Sample sample) {
        if (sample != null) {
            sample.setUserDecision(SampleUserDecisionEnum.PROCEED);
            save(sample);
            AJAX.update("@this", "processesSamples", "orderItemsGroup");
        }
    }

    public void proceedAll(Collection<Sample> samples) {
        if (samples != null) {
            for (Sample sample : samples) {
                sample.setUserDecision(SampleUserDecisionEnum.PROCEED);
                save(sample);
            }
            AJAX.update("@this", "processesSamples", "orderItemsGroup");
        }
    }

    /**
     * Remove the sample. IMPORTANT: This function has to be called wherever a sample is deletable/deleted.
     */
    public void remove(Sample sample, boolean cascadeToDescendants) {
        Set<Sample> parentsToDelete = new HashSet<>(sample.getParents());
        Set<Sample> descendants = new HashSet<>(sample.getDescendants());
        // Important: Do not remove the clearing of the parents as changes are propagated on merge which is called in the remove method.
        sample.getParents().clear();
        super.remove(sample);
        for (Sample parentSample : parentsToDelete) {
            if (parentSample.isUserSampleInMultiplexType()) {
                // Clear the children first to avoid cascade operations to the sample given as the parameter 'sample'.
                parentSample.getChildren().clear();
                super.remove(parentSample);
            }
        }
        if (cascadeToDescendants) {
            // Cascade the delete operation along its descendants.
            for (Sample descendant : descendants) {
                descendant.getParents().clear();
                descendant.getChildren().clear();
                super.remove(descendant);
            }
        }
    }

    /**
     * Remove the sample. IMPORTANT: This function has to be called wherever a sample is deletable/deleted.
     */
    public void remove(Sample sample) {
        remove(sample, true);
    }

    public Integer removeAllDeletableSamples(Project project) {
        if (project != null) {
            final Set<Sample> deletableSamples = project.getDeletableSamples();
            for (final Sample sample : deletableSamples) {
                super.remove(sample);
            }
            return deletableSamples.size();
        }
        return null;
    }

    public void replaceAll(Collection<Sample> samples) {
        if (samples != null) {
            for (Sample sample : samples) {
                createReplacement(sample);
            }
        }
    }

    public void save(Sample sample) {
        save(sample, true);
    }

    /**
     * Save the sample. IMPORTANT: This function has to be called wherever a sample is editable/edited.
     */
    public void save(Sample sample, boolean index) {
        sample.setCalculatedAttributes();
        try {
            if (sample.isCloned()) {
                sample.cloneChildren(sample.getClone());
            } else if (sample.isMoved()) {
                sample.move();
            }
            if (sample.isInitialParentSamplesOfUserMultiplexInitialized() || sample.getId() == 0 && sample.getMultiplexedByUser() != null && sample.getMultiplexedByUser()) {
                handleParentSamplesOfUserMultiplex(sample);
            }
            super.save(sample, index);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }

    public Sample saveMultiplexedSample(Set<Sample> selectedSamples, SampleTypeEnum selectedSampleType, Long containerId, Sample editedSample, Set<Sample> oldSelectedSamples, Map<Long, Long> selectedSamplesContainerIdsCounterMap, String clone) {
        if (selectedSamples != null && !selectedSamples.isEmpty() && selectedSampleType != null && containerId != null) {
            Sample multiplexedSample;
            // Note: containerId is only used if not all samples are from the same container and therefore the multiplex along with the libraryInPool samples need to be assigned to a dedicated container.
            Container container = find(Container.class, containerId);
            if (clone == null && editedSample != null && oldSelectedSamples != null && !oldSelectedSamples.isEmpty()) {
                // An already existing multiplexed sample is modified.
                multiplexedSample = editedSample;
                Set<Sample> samplesToRemove = new HashSet<>(oldSelectedSamples);
                samplesToRemove.removeAll(selectedSamples);
                multiplexedSample.getParents().removeAll(samplesToRemove);
                multiplexedSample.getParents().addAll(selectedSamples);
            } else {
                // A new multiplexed sample is created or an already existing one is cloned.
                boolean isClone = clone != null && editedSample != null;
                try {
                    multiplexedSample = isClone ? editedSample.clone() : new Sample();
                } catch (final CloneNotSupportedException e) {
                    throw new RollbackException(e.getMessage());
                }
                multiplexedSample.setContainer(container);
                multiplexedSample.setMultiplexed(Boolean.TRUE);
                String commonTubeId = getCommonTubeIdFromSamples(selectedSamples);
                if (commonTubeId == null) {
                    commonTubeId = container.getId() + "/" + (getSamplesByContainerIdCount(container.getId()) + 1);
                }
                // At this point the commonTubeId is never null or empty.
                multiplexedSample.setName(commonTubeId + "_" + (isClone ? Messages.get("sampleNamePostfixReMultiplexed") : Messages.get("sampleNamePostfixMultiplexed")));
                multiplexedSample.setTubeIdToNext();
                multiplexedSample.assignValidName();
                multiplexedSample.getParents().addAll(selectedSamples);
                if (selectedSampleType.equals(SampleTypeEnum.ILLUMINA_LIBRARY)) {
                    multiplexedSample.setSampleType(sampleTypeService.getSampleTypeByName(SampleTypeEnum.ILLUMINA_MULTIPLEXED.getLabel()));
                } else if (selectedSampleType.equals(SampleTypeEnum.NANOPORE_LIBRARY)) {
                    multiplexedSample.setSampleType(sampleTypeService.getSampleTypeByName(SampleTypeEnum.NANOPORE_MULTIPLEXED.getLabel()));
                } else if (selectedSampleType.equals(SampleTypeEnum.PACBIO_LIBRARY)) {
                    multiplexedSample.setSampleType(sampleTypeService.getSampleTypeByName(SampleTypeEnum.PACBIO_MULTIPLEXED.getLabel()));
                } else if (selectedSampleType.equals(SampleTypeEnum.ONT_READY_MADE_LIBRARY)) {
                    multiplexedSample.setSampleType(sampleTypeService.getSampleTypeByName(SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED.getLabel()));
                } else if (selectedSampleType.equals(SampleTypeEnum.MS_SAMPLE_LABELED)) {
                    multiplexedSample.setSampleType(sampleTypeService.getSampleTypeByName(SampleTypeEnum.MS_SAMPLE_MULTIPLEXED.getLabel()));
                }
            }
            if (selectedSamplesContainerIdsCounterMap != null) {
                if (selectedSamplesContainerIdsCounterMap.size() != 1) {
                    // Set the sample preparation protocol to null as not all samples belong to the same container.
                    multiplexedSample.setSamplePreparationProtocol(null);
                } else {
                    // Set the sample preparation protocol from the order if possible since all samples belong to the same container.
                    Container order = find(Container.class, selectedSamplesContainerIdsCounterMap.keySet().iterator().next());
                    if (order != null && !order.isContainerProject() && ((Order) Hibernate.unproxy(order)).getLibraryProtocol() != null) {
                        multiplexedSample.setSamplePreparationProtocol((SamplePreparationProtocol) Hibernate.unproxy(((Order) Hibernate.unproxy(order)).getLibraryProtocol()));
                    } else {
                        multiplexedSample.setSamplePreparationProtocol(null);
                    }
                }
            }
            save(multiplexedSample);
            return multiplexedSample;
        }
        return null;
    }

    public void saveSamples(Collection<Sample> samples) {
        for (Sample sample : samples) {
            save(sample);
        }
    }

    public String saveSamples(List<Sample> editList, Map<Sample, Boolean> editListItemsMapForDeletion) {
        int created = 0;
        int deleted = 0;
        int updated = 0;
        // Important: Changes to the custom attributes of the sample are cascaded in the case of merge, persist, and remove.
        for (Sample sample : editList) {
            if (editListItemsMapForDeletion == null || !(sample.getId() == 0 && editListItemsMapForDeletion.containsKey(sample) && editListItemsMapForDeletion.get(sample))) {
                if (editListItemsMapForDeletion == null || editListItemsMapForDeletion.isEmpty() || !editListItemsMapForDeletion.containsKey(sample) || !editListItemsMapForDeletion
                    .get(sample) || sample.getId() == 0) {
                    Iterator<CustomAttribute> iterator = sample.getCustomAttributes().iterator();
                    CustomAttribute customSampleAttribute;
                    while (iterator.hasNext()) {
                        customSampleAttribute = iterator.next();
                        if (StringHelper.isEmpty(customSampleAttribute.getName()) || StringHelper.isEmpty(customSampleAttribute.getValue())) {
                            iterator.remove();
                            sample.removeCustomAttribute(customSampleAttribute);
                            if (customSampleAttribute.isManaged()) {
                                // Important: Removing an empty not managed custom sample attribute does not constitute a change in the respective sample.
                                sample.setChanged(true);
                            }
                        } else if (customSampleAttribute.hasChanged()) {
                            sample.setChanged(true);
                        }
                    }
                    if (sample.getId() == 0) {
                        // Persist the new sample.
                        save(sample);
                        ++created;
                    } else {
                        if (sample.isChanged()) {
                            // Update the already existing sample.
                            save(sample);
                            ++updated;
                        }
                    }
                } else {
                    // Remove the checked sample.
                    sample.getCustomAttributes().clear();
                    remove(sample);
                    ++deleted;
                }
            }
        }
        if (editListItemsMapForDeletion != null) {
            editListItemsMapForDeletion.clear();
        }
        IndexHelper.indexEntities(editList);
        if (created > 0 || deleted > 0 || updated > 0) {
            final StringBuilder msg = new StringBuilder(Messages.get("successfullyEditedSamples"));
            msg.append(": ");
            if (created > 0) {
                msg.append(created).append(" ").append(Messages.get("created").toLowerCase()).append(" ");
            }
            if (deleted > 0) {
                msg.append(deleted).append(" ").append(Messages.get("deleted").toLowerCase()).append(" ");
            }
            if (updated > 0) {
                msg.append(updated).append(" ").append(Messages.get("updated").toLowerCase()).append(" ");
            }
            return msg.toString();
        }
        return Messages.get("successfullyUpdated");
    }

    private void updateMultiplexIdsCounters(Sample sample, Pattern pattern, int[] sampleMultiplexIdsCounters) {
        if (StringHelper.isNotEmpty(sample.getMultiplexId())) {
            if (!pattern.matcher(sample.getMultiplexId()).matches()) {
                ++sampleMultiplexIdsCounters[1];
            } else {
                ++sampleMultiplexIdsCounters[0];
            }
        }
        if (StringHelper.isNotEmpty(sample.getMultiplexId2())) {
            if (!pattern.matcher(sample.getMultiplexId2()).matches()) {
                ++sampleMultiplexIdsCounters[1];
            } else {
                ++sampleMultiplexIdsCounters[0];
            }
        }
    }

    public void userDecisionRequired(Sample sample, AbstractEntity containerToRendered) {
        if (sample != null) {
            if (containerToRendered instanceof Container) {
                ((Container) containerToRendered).setSamplesUserDecisionRequired(null);
            }
            sample.getContainer().setSamplesUserDecisionRequired(null);
            if (sample.isUserDecisionOnParent() && !sample.getParents().isEmpty()) {
                for (Sample parent : sample.getParents()) {
                    userDecisionRequired(parent, containerToRendered);
                }
            } else {
                if (!SampleUserDecisionEnum.REQUIRED.equals(sample.getUserDecision())) {
                    sample.setUserDecision(SampleUserDecisionEnum.REQUIRED);
                    save(sample);
                    // Important: Re-read container to avoid concurrency issues.
                    Container container = find(Container.class, sample.getContainer().getId());
                    if (!Order.USER_DECISION_REQUIRED.equals(container.getCustomStatus())) {
                        container.setCreateAndAddCustomStatus(Order.USER_DECISION_REQUIRED);
                        save(container);
                    }
                }
            }
            AJAX.update("@this", "processesSamples");
        }
    }
}
