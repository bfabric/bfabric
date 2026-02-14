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

package org.bfabric.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.jws.WebService;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Country;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.Link;
import org.bfabric.entity.Order;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Project;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Run;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Workunit;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.webservice.server.endpoint.AbstractAPIWebService;
import org.hibernate.Hibernate;
import static org.reflections.ReflectionUtils.getAllFields;
import org.reflections.Reflections;

@Named
@ApplicationScoped
public class ClassHelper {

    // Class name postfix for detecting Hibernate proxies.
    private static final String HIBERNATE_PROXY = "$HibernateProxy$";

    private static final Set<Class<?>> baseEntityClasses = new HashSet<>();

    private static final List<String> baseEntityClassNames = new ArrayList<>();

    private static final List<String> attributeTypeNames = new ArrayList<>();

    private static final List<String> dateColumns = new ArrayList<>();

    private static final HashMap<String, Class<?>> classMap = new HashMap<>();

    private static final List<String> entityClassNames = new ArrayList<>();

    private static final List<String> entityClassNamesLowerCase = new ArrayList<>();

    private static final Set<Class<?>> entityClasses = new HashSet<>();

    private static final List<String> indexableClasses = new ArrayList<>();

    private static final Set<Class<Mergeable>> mergeableEntityClasses = new HashSet<>();

    private static final List<String> mergeableEntityClassNames = new ArrayList<>();

    private static final HashMap<String, Class<?>> showScreenClassMap = new HashMap<>();

    private static final List<String> wsEndPoints = new ArrayList<>();

    private static final Set<Class<? extends AbstractAPIWebService>> wsEndPointClasses = new HashSet<>();

    static {
        // Set entityClasses. Note that the class Country is added individually since it does not extend the superclass AbstractEntity.
        entityClasses.addAll(new Reflections("org.bfabric.entity").getSubTypesOf(AbstractEntity.class));
        entityClasses.add(Country.class);

        for (Class<?> clazz : entityClasses) {
            entityClassNames.add(clazz.getSimpleName());
            CollectionHelper.sortObjects(entityClassNames);
            entityClassNamesLowerCase.add(clazz.getSimpleName().toLowerCase());
            CollectionHelper.sortObjects(entityClassNamesLowerCase);

            classMap.put(clazz.getSimpleName().toLowerCase(), clazz);

            if (ShowScreen.class.isAssignableFrom(clazz)) {
                showScreenClassMap.put(clazz.getSimpleName().toLowerCase(), clazz);
            }

            for (Class<?> interfaceClazz : clazz.getInterfaces()) {
                if (interfaceClazz.equals(Mergeable.class)) {
                    mergeableEntityClasses.add((Class<Mergeable>) clazz);
                    mergeableEntityClassNames.add(clazz.getSimpleName());
                }
            }
            CollectionHelper.sortObjects(mergeableEntityClassNames);

        }

        for (Class<?> clazz : new Reflections("org.bfabric.entity").getSubTypesOf(AbstractBaseEntity.class)) {
            if (clazz.getAnnotation(Entity.class) != null && clazz.getAnnotation(Inheritance.class) == null) {
                baseEntityClasses.add(clazz);
            }
        }

        for (Class<?> clazz : baseEntityClasses) {
            if (clazz.getAnnotation(DiscriminatorValue.class) == null) {
                baseEntityClassNames.add(clazz.getSimpleName());
            }
        }
        baseEntityClassNames.add(Comment.class.getSimpleName());
        baseEntityClassNames.add(Link.class.getSimpleName());
        baseEntityClassNames.add(Order.class.getSimpleName());
        baseEntityClassNames.add(Project.class.getSimpleName());
        CollectionHelper.sortObjects(baseEntityClassNames);

        attributeTypeNames.add("String");
        attributeTypeNames.add("Integer");
        attributeTypeNames.add("Numeric");
        attributeTypeNames.add("Boolean");
        attributeTypeNames.add("Date");
        attributeTypeNames.add("DateTime");
        attributeTypeNames.add("Time");
        attributeTypeNames.addAll(baseEntityClassNames);

        dateColumns.addAll(computeDateColumns());

        List<String> indexableClassNames = new ArrayList<>();
        for (IndexMapEnum indexMapEnum : IndexMapEnum.values()) {
            indexableClassNames.add(getTrimmedClassName(indexMapEnum.getEntityClass()));
        }
        indexableClasses.addAll(indexableClassNames);
        CollectionHelper.sortObjects(indexableClasses);

        wsEndPointClasses.addAll(new Reflections("org.bfabric.webservice.server.endpoint").getSubTypesOf(AbstractAPIWebService.class));
        for (Class<? extends AbstractAPIWebService> wsClass : wsEndPointClasses) {
            WebService annotation = wsClass.getAnnotation(WebService.class);
            if (annotation != null) {
                wsEndPoints.add("/" + annotation.serviceName());
            }
            CollectionHelper.sortObjects(wsEndPoints);
        }
    }

    public static Set<String> computeDateColumns() {
        Set<String> dateCols = new HashSet<>();
        for (Class<?> clazz : entityClasses) {
            for (Field f : getAllFields(clazz)) {
                Class<?> t = f.getType();
                if (t == java.time.LocalDate.class || t == java.time.LocalDateTime.class) {
                    dateCols.add(f.getName());
                }
            }
        }
        return dateCols;
    }

    public static String getAttributeName(Class<?> clazz) {
        return clazz != null ? StringHelper.firstLower(clazz.getSimpleName()) : null;
    }

    public static List<String> getAttributeTypeNames() {
        return attributeTypeNames;
    }

    public static List<String> getBaseEntityClassNames() {
        return baseEntityClassNames;
    }

    public static Set<Class<?>> getBaseEntityClasses() {
        return baseEntityClasses;
    }

    public static Class<?> getClassByName(String className) {
        return className != null ? classMap.get(className.toLowerCase()) : null;
    }

    public static HashMap<String, Class<?>> getClassMap() {
        return classMap;
    }

    public static String getClassName(String className) {
        Class<?> clazz = getClassByName(className);
        return clazz != null ? clazz.getSimpleName() : StringUtils.deleteWhitespace(WordUtils.capitalize(className));
    }

    public static List<String> getDateColumns() {
        return dateColumns;
    }

    public static Class<?> getEntityClass(String className) {
        return className != null ? classMap.get(className.toLowerCase()) : null;
    }

    public static List<String> getEntityClassNames() {
        return entityClassNames;
    }

    public static List<String> getEntityClassNamesLowerCase() {
        return entityClassNamesLowerCase;
    }

    public static Set<Class<?>> getEntityClasses() {
        return entityClasses;
    }

    public static List<String> getIndexableClasses() {
        return indexableClasses;
    }

    public static List<String> getMergeableEntityClassNames() {
        return mergeableEntityClassNames;
    }

    public static Set<Class<Mergeable>> getMergeableEntityClasses() {
        return mergeableEntityClasses;
    }

    public static <T> T getNewObject(final String simpleClassName) throws Exception {
        if (simpleClassName != null && getClassMap().containsKey(simpleClassName.toLowerCase())) {
            return (T) getClassMap().get(simpleClassName.toLowerCase()).newInstance();
        }
        throw new NullPointerException("Key for " + simpleClassName + " not found");
    }

    public static String getRequestParameterId(Class<?> clazz) {
        return clazz != null ? StringHelper.firstLower(clazz.getSimpleName() + "Id") : null;
    }

    public static Class<?> getRuntimeClass(Class<?> clazz, int pos) {
        return clazz != null ? getRuntimeClass(clazz.getGenericSuperclass(), pos) : null;
    }

    public static Class<?> getRuntimeClass(Type type, int pos) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        return (Class<?>) parameterizedType.getActualTypeArguments()[pos];
    }

    public static HashMap<String, Class<?>> getShowScreenClassMap() {
        return showScreenClassMap;
    }

    public static String getSimpleClassName(String canonicalName) {
        String simpleClassName = canonicalName;
        if (simpleClassName != null) {
            int pos = simpleClassName.lastIndexOf(".");
            if (pos >= 0) {
                simpleClassName = simpleClassName.substring(pos + 1);
            }
        }
        return simpleClassName;
    }

    public static String getTableName(Class<?> clazz) {
        if (clazz != null) {
            if (Order.class.equals(clazz)) {
                return Order.class.getSimpleName() + "_";
            }
            Table table = clazz.getAnnotation(Table.class);
            if (table != null && StringHelper.isNotEmpty(table.name())) {
                return table.name();
            }
            return clazz.getSimpleName();
        }
        return null;
    }

    public static String getTableName(String className) {
        return className != null ? getTableName(getClassMap().get(className.toLowerCase())) : null;
    }

    public static String getTrimmedClassName(Class<?> clazz) {
        return getTrimmedClassName(clazz.getSimpleName());
    }

    public static String getTrimmedClassName(String className) {
        String trimmedClassName = className;
        if (trimmedClassName != null && !trimmedClassName.isEmpty()) {
            int pos = trimmedClassName.lastIndexOf(HIBERNATE_PROXY);
            if (pos > 0) {
                trimmedClassName = trimmedClassName.substring(0, pos);
            }
        }
        return trimmedClassName;
    }

    public static Set<Class<? extends AbstractAPIWebService>> getWsEndPointClasses() {
        return wsEndPointClasses;
    }

    public static List<String> getWsEndPoints() {
        return wsEndPoints;
    }

    public static void initializeFullObject(AbstractEntity abstractEntity) {
        for (Field field : abstractEntity.getClass().getDeclaredFields()) {
            try {
                if (!field.getType().isPrimitive()) {
                    field.setAccessible(true);
                    Object object = field.get(abstractEntity);
                    if (object != null) {
                        Object castedObject = object.getClass().cast(object);
                        Hibernate.initialize(castedObject);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isProxied(String className) {
        return StringHelper.isNotEmpty(className) && className.lastIndexOf(HIBERNATE_PROXY) > 0;
    }

    public static boolean isShowScreenAvailable(String className) {
        return className != null && getShowScreenClassMap().containsKey(className.toLowerCase());
    }

    public List<String> getBaseEntityClassNamesList() {
        return getBaseEntityClassNames();
    }

    public List<String> getEntityClassNamesList() {
        return getEntityClassNames();
    }

    public List<String> getMergeableEntityClassNamesList() {
        return getMergeableEntityClassNames();
    }

    public List<String> getWebAppEntityClassNamesList() {
        return Arrays.asList(Dataset.class.getSimpleName(), Instrument.class.getSimpleName(), Order.class.getSimpleName(),
            Plate.class.getSimpleName(), Project.class.getSimpleName(), Resource.class.getSimpleName(), Run.class.getSimpleName(),
            Sample.class.getSimpleName(), Workunit.class.getSimpleName());
    }
}