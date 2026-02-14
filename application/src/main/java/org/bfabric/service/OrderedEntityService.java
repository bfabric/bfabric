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
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.entity.AbstractOrderedEnabledNamedBaseEntity;
import org.bfabric.util.ClassHelper;

@Named
@Stateless
public class OrderedEntityService extends AbstractService {

    private static final long serialVersionUID = 1;

    public int getNextOrderPositionByClass(Class<?> clazz) {
        return ((BigInteger) createNativeQuery("select coalesce(max(orderposition), 0) from " + ClassHelper.getTrimmedClassName(clazz)).getSingleResult()).intValue() + 1;
    }

    public List<AbstractOrderedEnabledNamedBaseEntity> getOrderedEntitiesByClassNameOrderByPosition(String entityClassName) {
        return createQuery("from " + entityClassName + " entity order by orderPosition").getResultList();
    }

    public void moveOrderPositionDown(AbstractOrderedEnabledNamedBaseEntity entity, List<? extends AbstractOrderedEnabledNamedBaseEntity> entities) {
        AbstractOrderedEnabledNamedBaseEntity positionSwitchWith = entity.moveOrderPositionDown(entities);
        merge(entity);
        merge(positionSwitchWith);
    }

    public void moveOrderPositionEnd(AbstractOrderedEnabledNamedBaseEntity entity, List<? extends AbstractOrderedEnabledNamedBaseEntity> entities) {
        entity.moveOrderPositionEnd(entities);
        merge(entity);
    }

    public void moveOrderPositionStart(AbstractOrderedEnabledNamedBaseEntity entity, List<? extends AbstractOrderedEnabledNamedBaseEntity> entities) {
        entity.moveOrderPositionStart(entities);
        merge(entity);
    }

    public void moveOrderPositionUp(AbstractOrderedEnabledNamedBaseEntity entity, List<? extends AbstractOrderedEnabledNamedBaseEntity> entities) {
        AbstractOrderedEnabledNamedBaseEntity positionSwitchWith = entity.moveOrderPositionUp(entities);
        merge(entity);
        merge(positionSwitchWith);
    }

}
