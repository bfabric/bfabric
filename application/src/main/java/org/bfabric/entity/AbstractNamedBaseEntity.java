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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.Constants;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractNamedBaseEntity extends AbstractBaseEntity {

    private static final long serialVersionUID = 1;

    @NotBlank
    @Size(max = Constants.MAX_LENGTH_NAME)
    @XmlElement
    protected String name;

    @Column(updatable = false, insertable = false)
    private String displayName;

    @Transient
    private String oldName;

    public AbstractNamedBaseEntity() {
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getId() + " - " + getName();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getName() != null) {
            addEntityInfoItem(summary, "name", getName());
        }
        return summary.toString();
    }

    public String getName() {
        return name != null ? name : Constants.EMPTY_STRING;
    }

    public String getName(String delimiter) {
        return delimiter != null && delimiter.equals(",") ? "\"" + name + "\"" : name;
    }

    public String getNameBreakable() {
        return StringHelper.convertToBreakable(getName());
    }

    public String getOldName() {
        return oldName;
    }

    public String getTruncatedDisplayName(int prefixLength) {
        return StringHelper.truncate(getDisplayName(), prefixLength);
    }

    public String getTruncatedName(int prefixLength) {
        return StringHelper.truncate(getName(), prefixLength);
    }

    @Override
    public void index() {
        // Important: The following setting must be done before super.index() invocation!
        if (isNameChanged()) {
            setIndexDependents(true);
        }
        super.index();
    }

    @Override
    public void initClone() throws CloneNotSupportedException {
        super.initClone();
        ((AbstractNamedBaseEntity) getClone()).setName(getName());
    }

    public boolean isNameChanged() {
        return oldName != null && !oldName.equals(name);
    }

    public void setName(final String name) {
        if (getOldName() == null) {
            setOldName(this.name);
        }
        this.name = StringHelper.format(name);
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }
}
