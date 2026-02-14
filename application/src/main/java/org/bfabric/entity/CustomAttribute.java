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

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CustomAttributeColumn;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class CustomAttribute extends AbstractParentDependentEntity {

    private static final long serialVersionUID = 1;

    @Transient
    private CustomAttributeColumn column;

    @Transient
    private boolean hasChanged;

    @NotBlank
    @Size(min = 1, max = 32)
    @XmlElement
    private String name;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String type = Constants.STRING;

    @Size(min = 1)
    @XmlElement
    private String value;

    public CustomAttribute() {
    }

    public CustomAttribute(AbstractEntity parent) {
        if (parent != null) {
            setParent(parent);
        }
    }

    public CustomAttribute(AbstractEntity parent, String name, String type, String value) {
        this(parent);
        setName(name);
        setValue(value);
        if (StringHelper.isNotEmpty(type)) {
            setType(type);
        }
    }

    public CustomAttribute(AbstractEntity parent, CustomAttributeColumn column, String name, String type, String value, boolean changed) {
        this(parent, name, type, value);
        setColumn(column);
        setChanged(changed);
    }

    @Override
    public CustomAttribute clone() throws CloneNotSupportedException {
        CustomAttribute clone = (CustomAttribute) super.clone();
        ClassHelper.initializeFullObject(clone);
        return clone;
    }

    public CustomAttribute clone(AbstractEntity parent) throws CloneNotSupportedException {
        CustomAttribute clone = clone();
        clone.setParent(parent);
        return clone;
    }

    public void customAttributeNameChanged(ValueChangeEvent event) {
        setName((String) event.getNewValue());
    }

    public void customAttributeValueChanged(ValueChangeEvent event) {
        setValue((String) event.getNewValue());
        setChanged(true);
    }

    public CustomAttributeColumn getColumn() {
        return column;
    }

    public String getName() {
        return name;
    }

    public String getNameValue() {
        return getName() + ": " + (StringHelper.isEmpty(getValue()) ? "" : getValue());
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public void setColumn(CustomAttributeColumn column) {
        this.column = column;
    }

    public void setName(String name) {
        this.name = name != null ? name.replaceAll("[^A-Za-z0-9_\\-\\s\\[\\]()]", "").replaceAll("\\s+", " ") : null;
    }

    @Override
    public void setParent(AbstractEntity parent) {
        super.setParent(parent);
        if (getParent() != null && getParent().getCustomAttributes() != null && !getParent().getCustomAttributes().contains(this)) {
            getParent().getCustomAttributes().add(this);
        }
    }

    public void setType(String type) {
        this.type = ClassHelper.getClassName(type);
    }

    public void setValue(String value) {
        this.value = StringHelper.format(value);
    }
}