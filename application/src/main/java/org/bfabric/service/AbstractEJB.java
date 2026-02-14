/*
 *
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

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.Link;
import org.bfabric.entity.Option;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.ClassHelper;

public abstract class AbstractEJB implements Serializable {

    private static final long serialVersionUID = 1;

    public AbstractEJB() {
    }

    public void clear() {
        getEntityManager().clear();
    }

    public boolean contains(Object entity) {
        return getEntityManager().contains(entity);
    }

    public int count(Class<?> clazz) {
        return ((Long) createQuery("select count(1) from " + clazz.getSimpleName()).getSingleResult()).intValue();
    }

    public Query createNamedQuery(String queryString) {
        return getEntityManager().createNamedQuery(queryString).setFlushMode(FlushModeType.COMMIT);
    }

    public Query createNamedQuery(String queryString, EntityManager entityManager) {
        return entityManager.createNamedQuery(queryString).setFlushMode(FlushModeType.COMMIT);
    }

    public Query createNativeQuery(String queryString) {
        return getEntityManager().createNativeQuery(queryString).setFlushMode(FlushModeType.COMMIT);
    }

    public Query createNativeQuery(String queryString, Class<?> clazz) {
        return getEntityManager().createNativeQuery(queryString, clazz).setFlushMode(FlushModeType.COMMIT);
    }

    public Query createQuery(String queryString) {
        return getEntityManager().createQuery(queryString).setFlushMode(FlushModeType.COMMIT);
    }

    public AbstractEntity fetch(Class<? extends AbstractEntity> entityClass, Long entityId) throws InvalidDataException {
        if (entityClass == null) {
            throw new InvalidDataException("No entity class specified!");
        }
        if (entityId == null) {
            throw new InvalidDataException("No id specified!");
        }
        AbstractEntity entity = find(entityClass, entityId);
        if (entity == null) {
            throw new InvalidDataException("There is no " + entityClass.getSimpleName().toLowerCase() + " with id " + entityId + "!");
        }
        return entity;
    }

    public AbstractEntity fetch(String entityClassName, Long entityId) throws InvalidDataException {
        return fetch((Class<AbstractEntity>) ClassHelper.getClassByName(entityClassName), entityId);
    }

    public AbstractEntity fetchAndSetOldStateAsXml(Class<? extends AbstractEntity> entityClass, Long entityId) throws InvalidDataException {
        AbstractEntity entity = fetch(entityClass, entityId);
        if (entity != null) {
            entity.setOldStateAsXml();
        }
        return entity;
    }

    public <T> T find(Class<T> clazz, Object id) {
        return getEntityManager().find(clazz, id);
    }

    public <T> T findByName(Class<T> clazz, String name) {
        return findByName(clazz.getSimpleName(), name);
    }

    public <T> T findByName(String entityClassName, String name) {
        List<T> ret = createQuery("from " + entityClassName + " WHERE lower(name) = lower(:name)").setParameter("name", name).setMaxResults(1).getResultList();
        return ret.isEmpty() ? null : ret.get(0);
    }

    public void flush() {
        getEntityManager().flush();
    }

    protected abstract EntityManager getEntityManager();

    public Object merge(Object entity) {
        if (entity instanceof AbstractEntity) {
            // Important: The value of transient variables such as 'logEntity' or 'setModifiedEnabled' are reset to its default value after a merge!
            AbstractEntity abstractEntity = (AbstractEntity) entity;
            boolean isLogEntity = abstractEntity.isLogEntity();
            boolean setModifiedEnabled = abstractEntity.isSetModifiedEnabled();
            AbstractEntity merged = getEntityManager().merge(abstractEntity);
            merged.setLogEntity(isLogEntity);
            merged.setSetModifiedEnabled(setModifiedEnabled);
            return merged;
        }
        getEntityManager().merge(entity);
        return entity;
    }

    public void persist(Object entity) {
        getEntityManager().persist(entity);
    }

    public void refresh(Object entity) {
        getEntityManager().refresh(entity);
    }

    public void remove(Class<?> clazz, Long id) {
        getEntityManager().remove(find(clazz, id));
    }

    public void remove(Object entity) {
        if (entity instanceof AbstractEntity) {
            AbstractEntity abstractEntity = (AbstractEntity) entity;
            for (Link link : abstractEntity.getLinks()) {
                remove(link);
            }
            for (CustomAttribute customAttribute : abstractEntity.getCustomAttributes()) {
                remove(customAttribute);
            }
        }
        getEntityManager().remove(merge(entity));
    }

    public void save(AbstractEntity entity) {
        save(entity, true);
    }

    public void save(AbstractEntity entity, boolean index) {
        if (entity != null) {
            if (entity.getId() > 0) {
                if (entity instanceof AbstractBaseEntity) {
                    ((AbstractBaseEntity) entity).setModifiedToCurrentDateAndUser();
                }
                merge(entity);
            } else {
                persist(entity);
            }

            saveAssociations(entity);

            if (index) {
                entity.index();
            }
        }
    }

    public void saveAssociations(AbstractEntity entity) {
        if (entity != null) {
            for (Link link : entity.getLinks()) {
                if (link.getParentId() == 0) {
                    link.setParent(link.getParent());
                }
                save(link, false);
            }
            for (Link link : entity.getLinksToBeRemoved()) {
                remove(link);
            }
            for (Option option : entity.getOptions()) {
                if (option.getParentId() == 0) {
                    option.setParent(option.getParent());
                }
                save(option);
            }
            for (Option option : entity.getOptionsToBeRemoved()) {
                remove(option);
            }
            for (CustomAttribute customAttribute : entity.getCustomAttributes()) {
                if (customAttribute.getParentId() == 0) {
                    customAttribute.setParent(customAttribute.getParent());
                }
                save(customAttribute, false);
            }
            for (CustomAttribute customAttribute : entity.getCustomAttributesToBeRemoved()) {
                remove(customAttribute);
            }
        }
    }

    public void update(AbstractEntity entity) {
        find(entity.getClass(), entity.getId());
        merge(entity);
    }
}
