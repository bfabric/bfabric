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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.entity.api.ParentDependent;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractParentDependentDescriptionNamedBaseEntity extends AbstractDescriptionNamedBaseEntity implements ParentDependent {

    private static final long serialVersionUID = 1;

    @NotNull
    @XmlElement
    protected String parentClassName;

    @NotNull
    @XmlElement
    protected Long parentId;

    public AbstractParentDependentDescriptionNamedBaseEntity() {
    }

    public String getParentClassName() {
        if (parentClassName == null) {
            setParentIdAndClassName();
        }
        return parentClassName;
    }

    public Long getParentId() {
        if (parentId == null) {
            setParentIdAndClassName();
        }
        return parentId;
    }

    @Override
    public boolean isDeletable() {
        return getParent() != null && getParent().isDeletable();
    }

    @Override
    public boolean isReadable() {
        return getParent() != null && getParent().isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return getParent() != null && getParent().isUpdatable();
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = StringHelper.format(parentClassName);
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
