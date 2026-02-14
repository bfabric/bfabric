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

package org.bfabric.xml.entity;

import javax.xml.bind.annotation.XmlElement;

import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.util.DateUtils;

public class XMLAbstractBaseEntity extends XMLAbstractEntity {

    @XmlElement
    private String created;

    @XmlElement
    private String createdby;

    @XmlElement
    private String modified;

    @XmlElement
    private String modifiedby;

    public XMLAbstractBaseEntity() {
    }

    public XMLAbstractBaseEntity(AbstractBaseEntity entity) {
        super(entity);
        if (entity != null) {
            if (entity.getCreated() != null) {
                setCreated(DateUtils.getDateAsFormattedString(entity.getCreated()));
            }
            if (entity.getCreatedBy() != null) {
                setCreatedby(entity.getCreatedBy());
            }
            if (entity.getModified() != null) {
                setModified(DateUtils.getDateAsFormattedString(entity.getModified()));
            }
            if (entity.getModifiedBy() != null) {
                setModifiedby(entity.getModifiedBy());
            }
        }
    }

    public XMLAbstractBaseEntity(AbstractBaseEntity entity, boolean reference) {
        super(entity, reference);
    }

    public String getCreated() {
        return created;
    }

    public String getCreatedby() {
        return createdby;
    }

    public Long getId() {
        return id;
    }

    public String getModified() {
        return modified;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }
}
