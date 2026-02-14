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

package org.bfabric.indexer.enums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.entity.Annotation;
import org.bfabric.entity.Booking;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Contract;
import org.bfabric.entity.Credit;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Event;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.service.IdentityService;
import org.bfabric.util.ClassHelper;

public enum IndexMapEnum {
    ANNOTATION(
        Annotation.class,
        IndexMapTypeEnum.annotation,
        RoleEnum.ANNOTATIONREADER,
        false),
    BOOKING(
        Booking.class,
        IndexMapTypeEnum.booking,
        RoleEnum.BOOKINGREADER,
        false),
    COMMENT(
        Comment.class,
        IndexMapTypeEnum.comment,
        RoleEnum.COMMENTREADER,
        false),
    CONTRACT(
        Contract.class,
        IndexMapTypeEnum.contract,
        RoleEnum.CONTRACTREADER,
        false),
    CREDIT(
        Credit.class,
        IndexMapTypeEnum.credit,
        RoleEnum.AGENDAUSER,
        false),
    DATASET(
        Dataset.class,
        IndexMapTypeEnum.dataset,
        null,
        true),
    EVENT(
        Event.class,
        IndexMapTypeEnum.event,
        RoleEnum.AGENDAUSER,
        false),
    INSTRUMENT(
        Instrument.class,
        IndexMapTypeEnum.instrument,
        RoleEnum.INSTRUMENTREADER,
        false),
    INSTRUMENTRESERVATION(
        InstrumentReservation.class,
        IndexMapTypeEnum.instrumentReservation,
        RoleEnum.INSTRUMENTREADER,
        false),
    ORDER(
        Order.class,
        IndexMapTypeEnum.order,
        null,
        true),
    PROJECT(
        Project.class,
        IndexMapTypeEnum.project,
        null,
        true),
    PURCHASE(
        Purchase.class,
        IndexMapTypeEnum.purchase,
        RoleEnum.PURCHASEREADER,
        false),
    RESOURCE(
        Resource.class,
        IndexMapTypeEnum.resource,
        null,
        true),
    SAMPLE(
        Sample.class,
        IndexMapTypeEnum.sample,
        null,
        true),
    SAMPLEPREPARATIONPROTOCOL(
        SamplePreparationProtocol.class,
        IndexMapTypeEnum.samplePreparationProtocol,
        null,
        false),
    USER(
        User.class,
        IndexMapTypeEnum.user,
        RoleEnum.USERREADER,
        false),
    WORKUNIT(
        Workunit.class,
        IndexMapTypeEnum.workunit,
        null,
        true);

    private final boolean containerDependent;

    private final Class<? extends Indexable> entityClass;

    private final RoleEnum role;

    private final IndexMapTypeEnum type;

    IndexMapEnum(Class<? extends Indexable> clazz, IndexMapTypeEnum type, RoleEnum role, boolean containerDependent) {
        entityClass = clazz;
        this.type = type;
        this.role = role;
        this.containerDependent = containerDependent;
    }

    public static Collection<String> getContainerDependentIndexMapTypes() {
        Set<String> indexMapTypes = new TreeSet<>();
        indexMapTypes.add(IndexMapTypeEnum.all.name());
        for (IndexMapEnum indexMapEnum : values()) {
            if (indexMapEnum.isContainerDependent()) {
                indexMapTypes.add(indexMapEnum.getType().name());
            }
        }
        return indexMapTypes;
    }

    public static IndexMapEnum getEnum(Class<? extends Indexable> clazz) {
        IndexMapEnum ret = null;
        for (IndexMapEnum indexMapEnum : values()) {
            if (indexMapEnum.entityClass.equals(clazz)) {
                ret = indexMapEnum;
                break;
            }
        }
        return ret;
    }

    public static IndexMapEnum getEnum(String indexMapEnumName) {
        IndexMapEnum ret = null;
        for (IndexMapEnum indexMapEnum : values()) {
            if (indexMapEnum.name().equals(indexMapEnumName)) {
                ret = indexMapEnum;
                break;
            }
        }
        return ret;
    }

    public static IndexMapEnum getEnumByCanonicalClassName(String canonicalClassName) {
        IndexMapEnum ret = null;
        for (IndexMapEnum indexMapEnum : values()) {
            if (indexMapEnum.getEntityClass().getCanonicalName().equals(canonicalClassName)) {
                ret = indexMapEnum;
                break;
            }
        }
        return ret;
    }

    public static IndexMapEnum getEnumBySimpleClassName(String simpleClassName) {
        IndexMapEnum ret = null;
        for (IndexMapEnum indexMapEnum : values()) {
            String currentSimpleClassName = ClassHelper.getTrimmedClassName(indexMapEnum.getEntityClass());
            if (currentSimpleClassName.equals(simpleClassName)) {
                ret = indexMapEnum;
                break;
            }
        }
        return ret;
    }

    public static IndexMapEnum getEnumByType(String indexMapType) {
        IndexMapEnum ret = null;
        IndexMapTypeEnum indexMapTypeEnum = IndexMapTypeEnum.value(indexMapType);
        if (indexMapTypeEnum != null) {
            for (IndexMapEnum indexMapEnum : values()) {
                if (indexMapEnum.getType().equals(indexMapTypeEnum)) {
                    ret = indexMapEnum;
                    break;
                }
            }
        }
        return ret;
    }

    private static IdentityService getIdentityService() {
        return CDI.current().select(IdentityService.class).get();
    }

    public static TreeSet<String> getIndexFields() {
        TreeSet<String> fields = new TreeSet<>();
        List<IndexMapTypeEnum> processedIndexMapTypeEnums = new ArrayList<>();

        // Cache currentUserRoleNames to speedup!
        List<String> currentUserRoleNames = getIdentityService().getCurrentUserRoleNames();

        for (IndexMapEnum indexMapEnum : values()) {
            // Fields should be added only once for every type (like booking types).
            if (!processedIndexMapTypeEnums.contains(indexMapEnum.getType())) {
                Indexable indexable;
                try {
                    if (indexMapEnum.getRole() == null || currentUserRoleNames.contains(indexMapEnum.getRole().getName())) {
                        indexable = indexMapEnum.getEntityClass().getDeclaredConstructor().newInstance();
                        indexable.getIndexFields(fields);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                processedIndexMapTypeEnums.add(indexMapEnum.getType());
            }
        }

        return fields;
    }

    public static Collection<String> getIndexMapTypeFields(String indexMapType) {
        TreeSet<String> indexMapTypeFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        IndexMapTypeEnum indexMapTypeEnum = IndexMapTypeEnum.value(indexMapType);
        if (indexMapTypeEnum != null) {
            // Add general fields
            indexMapTypeFields.add(IndexMapContentEnum.ANY.getField());
            indexMapTypeFields.add(IndexMapContentEnum.ID.getField());
            indexMapTypeFields.add(IndexMapContentEnum.NAME.getField());
            indexMapTypeFields.add(IndexMapContentEnum.CREATED.getField());
            indexMapTypeFields.add(IndexMapContentEnum.CREATEDBY.getField());
            indexMapTypeFields.add(IndexMapContentEnum.MODIFIED.getField());
            indexMapTypeFields.add(IndexMapContentEnum.MODIFIEDBY.getField());

            List<IndexMapEnum> restrictedEnumList = getRestrictedEnumList();
            if (indexMapTypeEnum.equals(IndexMapTypeEnum.all)) {
                for (IndexMapEnum indexMapEnum : restrictedEnumList) {
                    try {
                        indexMapEnum.getIndexFields(indexMapTypeFields);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (IndexMapEnum indexMapEnum : restrictedEnumList) {
                    if (indexMapEnum.getType().equals(indexMapTypeEnum)) {
                        try {
                            indexMapEnum.getIndexFields(indexMapTypeFields);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        return indexMapTypeFields;
    }

    public static Collection<String> getIndexMapTypes(boolean containerDependent) {
        Set<String> indexMapTypes = new TreeSet<>();
        indexMapTypes.add(IndexMapTypeEnum.all.name());

        // Cache currentUserRoleNames to speedup!
        List<String> currentUserRoleNames = getIdentityService().getCurrentUserRoleNames();

        for (IndexMapEnum indexMapEnum : values()) {
            // If no role is set or the identity has the role, then add the corresponding type dependent on whether containerDependent indexMapTypes should be included or not.
            if ((indexMapEnum.getRole() == null || currentUserRoleNames.contains(indexMapEnum.getRole().getName())) && (!containerDependent || indexMapEnum.isContainerDependent())) {
                indexMapTypes.add(indexMapEnum.getType().name());
            }
        }

        return indexMapTypes;
    }

    private static List<IndexMapEnum> getRestrictedEnumList() {
        List<IndexMapEnum> ret = new ArrayList<>();
        // Cache currentUserRoleNames to speedup!
        List<String> currentUserRoleNames = getIdentityService().getCurrentUserRoleNames();
        for (IndexMapEnum indexMapEnum : values()) {
            // If no role is set, then there are no restrictions.
            if (indexMapEnum.getRole() == null || currentUserRoleNames.contains(indexMapEnum.getRole().getName())) {
                ret.add(indexMapEnum);
            }
        }
        return ret;
    }

    public static boolean isContainerDependent(String indexMap) {
        IndexMapEnum indexMapEnum = getEnumByType(indexMap);
        return indexMapEnum != null && indexMapEnum.isContainerDependent();
    }

    public Class<? extends Indexable> getEntityClass() {
        return entityClass;
    }

    public void getIndexFields(TreeSet<String> fields) throws Exception {
        Indexable indexable;
        try {
            indexable = entityClass.getDeclaredConstructor().newInstance();
            indexable.getIndexFields(fields);
        } catch (InstantiationException e) {
            throw new Exception("Unable to add index fields for indexMapType " + this + "(InstantiationException)!");
        } catch (IllegalAccessException e) {
            throw new Exception("Unable to add index fields for indexMapType " + this + "(IllegalAccessException)!");
        }
    }

    public RoleEnum getRole() {
        return role;
    }

    public IndexMapTypeEnum getType() {
        return type;
    }

    public boolean isContainerDependent() {
        return containerDependent;
    }
}
