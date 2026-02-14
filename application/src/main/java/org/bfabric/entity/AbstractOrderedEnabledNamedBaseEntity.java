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

package org.bfabric.entity;

import java.util.Comparator;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.Orderable;

@MappedSuperclass
public abstract class AbstractOrderedEnabledNamedBaseEntity extends AbstractEnabledBaseEntity implements Orderable<AbstractOrderedEnabledNamedBaseEntity> {

    private static final long serialVersionUID = 1;

    @NotNull
    @XmlElement
    protected long orderPosition;

    @Override
    public AbstractOrderedEnabledNamedBaseEntity getFirstItem(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList) {
        return orderedList.get(0);
    }

    public String getGroupingAttributes() {
        return Constants.EMPTY_STRING;
    }

    @Override
    public AbstractOrderedEnabledNamedBaseEntity getLastItem(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList) {
        return orderedList.get(orderedList.size() - 1);
    }

    @Override
    public AbstractOrderedEnabledNamedBaseEntity getNextItem(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList, boolean up) {
        AbstractOrderedEnabledNamedBaseEntity nextItem = null;

        int listPosition;
        if (up) {
            listPosition = orderedList.indexOf(this) - 1;
        } else {
            listPosition = orderedList.indexOf(this) + 1;
        }
        if (listPosition < orderedList.size() && listPosition >= 0) {
            nextItem = orderedList.get(listPosition);
        }

        return nextItem;
    }

    @Override
    public long getNextOrderPosition(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList) {
        AbstractOrderedEnabledNamedBaseEntity nextItem = getNextItem(orderedList, true);
        return nextItem == null ? 1 : nextItem.getOrderPosition() + 1;
    }

    @Override
    @Transient
    public Comparator<AbstractOrderedEnabledNamedBaseEntity> getOrderComparator() {
        return Comparator.comparingLong(AbstractOrderedEnabledNamedBaseEntity::getOrderPosition);
    }

    @Override
    public long getOrderPosition() {
        return orderPosition;
    }

    @Override
    public AbstractOrderedEnabledNamedBaseEntity moveOrderPositionDown(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList) {
        AbstractOrderedEnabledNamedBaseEntity positionSwitchWith = getNextItem(orderedList, false);
        switchOrderPositions(positionSwitchWith);
        orderedList.sort(getOrderComparator());
        return positionSwitchWith;
    }

    @Override
    public void moveOrderPositionEnd(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList) {
        setOrderPosition(getLastItem(orderedList).getOrderPosition() + 1);
        orderedList.sort(getOrderComparator());
    }

    @Override
    public void moveOrderPositionStart(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList) {
        setOrderPosition(getFirstItem(orderedList).getOrderPosition() - 1);
        orderedList.sort(getOrderComparator());
    }

    @Override
    public AbstractOrderedEnabledNamedBaseEntity moveOrderPositionUp(List<? extends AbstractOrderedEnabledNamedBaseEntity> orderedList) {
        AbstractOrderedEnabledNamedBaseEntity positionSwitchWith = getNextItem(orderedList, true);
        switchOrderPositions(positionSwitchWith);
        orderedList.sort(getOrderComparator());
        return positionSwitchWith;
    }

    @Override
    public void setOrderPosition(long orderPosition) {
        this.orderPosition = orderPosition;
    }

    @Override
    public void switchOrderPositions(AbstractOrderedEnabledNamedBaseEntity entity) {
        long temp = getOrderPosition();
        this.setOrderPosition(entity.getOrderPosition());
        entity.setOrderPosition(temp);
    }
}
