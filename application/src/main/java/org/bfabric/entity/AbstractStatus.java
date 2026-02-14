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

import javax.persistence.MappedSuperclass;

import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidEnumValueException;

@MappedSuperclass
public abstract class AbstractStatus extends AbstractNamedBaseEntity {

    private static final long serialVersionUID = 1;

    public AbstractStatus() {
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            AbstractStatus entity = (AbstractStatus) object;
            return Long.compare(getId(), entity.getId());
        }
        throw new ClassCastException("Cannot compare this " + getClass().getName() + " with null");
    }

    public String getLabel() {
        return getName().toLowerCase();
    }

    public StatusEnum getStatusEnum() {
        try {
            return StatusEnum.value(getName());
        } catch (InvalidEnumValueException e) {
            return StatusEnum.INVALID;
        }
    }

    public boolean isCustomContainerStatus() {
        return this instanceof CustomContainerStatus;
    }

    @Override
    public boolean isReadable() {
        return super.isReadable() || isCreator();
    }

    public void setStatusEnum(StatusEnum statusEnum) {
        setName(statusEnum.getLabel());
    }
}
