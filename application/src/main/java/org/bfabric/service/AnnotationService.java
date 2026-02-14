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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Sample;
import org.bfabric.entity.User;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.AnnotationTypeRecord;

@Named
@Stateless
public class AnnotationService extends AbstractService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(AnnotationService.class.getName());

    public AnnotationService() {
        super(Annotation.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final Annotation annotation = (Annotation) entity;
        return createNamedQuery("Annotation.checkUniqueName").setParameter("name", annotation.getName()).setParameter("type", annotation.getType()).setParameter("id", annotation.getId())
            .setMaxResults(1).getResultList().isEmpty();
    }

    public List<AnnotationTypeRecord> getAllAnnotations() {
        final List<AnnotationTypeRecord> allAnnotations = new ArrayList<>();
        final List<Object[]> annotationTypeCounts = getAnnotationTypeCounts();
        for (final Object[] annotationTypeCount : annotationTypeCounts) {
            final AnnotationTypeRecord annotationTypeRecord = new AnnotationTypeRecord();
            annotationTypeRecord.setType((String) annotationTypeCount[0]);
            annotationTypeRecord.setCount((BigInteger) annotationTypeCount[1]);
            annotationTypeRecord.setUnreleasedAnnotations(getUnreleasedAnnotationsByType(annotationTypeRecord.getType()));
            allAnnotations.add(annotationTypeRecord);
        }

        return allAnnotations;
    }

    public List<String> getAnnotationNamesByType(String type) {
        return createNamedQuery("Annotation.findNamesByType").setParameter("type", type).getResultList();
    }

    public List<Annotation> getAnnotationReleaseTasks(User coach) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("released <> TRUE");
        StringBuilder where = new StringBuilder();
        Iterator<SampleAttributeEnum> it = SampleAttributeEnum.getAnnotationTypes().iterator();
        while (it.hasNext()) {
            SampleAttributeEnum sampleAttributeEnum = it.next();
            where.append("entity IN ");
            if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                where.append("(SELECT s.").append(sampleAttributeEnum.getName());
            }
            if (sampleAttributeEnum.isAnnotationTypeMultiValued()) {
                where.append("(SELECT ELEMENTS(s.").append(sampleAttributeEnum.getName()).append(")");
            }
            where.append(" FROM Sample s WHERE s.container.coach = :coach AND s.container.status = :status)");
            if (it.hasNext()) {
                where.append(" OR ");
            }
        }
        entityQuery.addWhereClause(where.toString());
        entityQuery.addParameter("coach", coach);
        entityQuery.addParameter("status", StatusEnum.RUNNING);
        entityQuery.setOrder("id DESC");
        return (List<Annotation>) entityQuery.getResultList();
    }

    public List<Object[]> getAnnotationTypeCounts() {
        return createNativeQuery("select type, count(*) from Annotation group by type order by type").getResultList();
    }

    public List<Annotation> getAnnotationsByAttributeType(String filterString, Sample sample, SampleAttributeEnum sampleAttributeEnum) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("LOWER(type) = LOWER(:type)");
        entityQuery.addParameter("type", sampleAttributeEnum.getLabel());

        try {
            if (sampleAttributeEnum.getMultiValued()) {
                entityQuery.addNotInEntitiesClause((List<Annotation>) PropertyUtils.getProperty(sample, sampleAttributeEnum.getName()));
            }
        } catch (Exception e) {
            logger.fine(sample + " does not have the property" + sampleAttributeEnum.getName());
        }

        entityQuery.setOrder("na DESC, top DESC, LOWER(name)");
        return (List<Annotation>) entityQuery.getResultList();
    }

    public List<Annotation> getAnnotationsByFilterAndType(String filterString, String type) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("LOWER(type) = LOWER(:type)");
        entityQuery.addParameter("type", type);
        entityQuery.setOrder("na DESC, top DESC, LOWER(name)");
        return (List<Annotation>) entityQuery.getResultList();
    }

    public List<Annotation> getAnnotationsByNameAndType(String name, String type) {
        return createNamedQuery("Annotation.findByNameAndType").setParameter("name", name).setParameter("type", type).setMaxResults(1).getResultList();
    }

    public List<Annotation> getAnnotationsByType(String type) {
        return createNamedQuery("Annotation.findByType").setParameter("type", type).getResultList();
    }

    public List<Annotation> getSimilarAnnotationsById(Long id) {
        return createNamedQuery("Annotation.findSimilarAnnotations").setParameter("id", id).setMaxResults(50).getResultList();
    }

    public List<Annotation> getUnreleasedAnnotationsByType(String type) {
        return createNamedQuery("Annotation.findByTypeAndReleasedNot").setParameter("type", type).getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final Annotation annotation = (Annotation) entity;
        return isValidName(annotation, Constants.EDIT + ":" + Constants.NAME, Messages.get("nameNotUniqueForTypeException").replace("{0}", "type").replace("{1}", annotation.getType()));
    }

    public void merge(Annotation annotation, Annotation merged, Annotation mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            annotation.setName(mergeSelection.getName());
            annotation.setCommon(mergeSelection.isCommon());
            annotation.setNa(mergeSelection.isNa());
            annotation.setTop(mergeSelection.isTop());
            annotation.setReleased(mergeSelection.isReleased());
            annotation.setDescription(mergeSelection.getDescription());

            final String mergeAnnotationType = annotation.getType();
            final String annotationColumn = Annotation.toColumnName(mergeAnnotationType);
            final Long mergeAnnotationId = annotation.getId();

            // Merge the comments.
            updateCommentOnMerge(merged, mergeAnnotationId);

            // Merge the samples.
            if (SampleAttributeEnum.isAnnotationTypeSingleValued(annotation.getType())) {
                updateSampleSingleValued(merged, annotationColumn, mergeAnnotationId);
            } else if (SampleAttributeEnum.isAnnotationTypeMultiValued(annotation.getType())) {
                updateSampleMultiValued(merged, annotationColumn, mergeAnnotationId);
            }

            saveMerge(annotation, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void updateCommentOnMerge(Annotation merged, Long mergeAnnotationId) {
        createQuery("update Comment set parentId = :newParentId where discriminator = :discriminator AND parentId = :parentId").setParameter("discriminator", CommentDiscriminator.ANNOTATION_COMMENT)
            .setParameter("newParentId", mergeAnnotationId).setParameter("parentId", merged.getId()).executeUpdate();
    }

    public void updateSampleMultiValued(Annotation merged, String annotationColumn, Long mergeAnnotationId) {
        createNativeQuery("update Sample" + annotationColumn + " set " + annotationColumn + "id = :newId where " + annotationColumn + "id = :oldId").setParameter("newId", mergeAnnotationId)
            .setParameter("oldId", merged.getId()).executeUpdate();
    }

    public void updateSampleSingleValued(Annotation merged, String annotationColumn, Long mergeAnnotationId) {
        createNativeQuery("update Sample set " + annotationColumn + "id = :newId where " + annotationColumn + "id = :oldId").setParameter("newId", mergeAnnotationId).setParameter("oldId", merged
            .getId()).executeUpdate();
    }
}
