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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.ParameterTypeEnum;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "executable_key_context_unique", columnNames = { "executableid", "key", "context" }),
    @UniqueConstraint(name = "application_key_context_unique", columnNames = { "applicationid", "key", "context" }), @UniqueConstraint(name = "storage_key_context_unique", columnNames = { "storageid",
    "key", "context" }), @UniqueConstraint(name = "submitter_key_context_unique", columnNames = { "submitterid", "key", "context" }),
    @UniqueConstraint(name = "wrappercreator_key_context_unique", columnNames = { "wrappercreatorid", "key", "context" }), @UniqueConstraint(name = "workunit_key_context_unique", columnNames = {
    "workunitid", "key", "context" }) })
@XmlRootElement
@NamedQuery(name = "Parameter.checkUniqueName", query = "SELECT a.id FROM Parameter a WHERE lower(a.key) = lower(:key) and a.id <> :id and a.context = :context and (a.executable = :executable or a.application = :application or a.storage = :storage or a.submitter = :submitter or a.wrapperCreator = :wrapperCreator or a.workunit = :workunit)")
public class Parameter extends AbstractDescriptionBaseEntity {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationid")
    @XmlIDREF
    private Application application;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private ExecutableContextEnum context;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @JoinTable(name = "parameterenumeration", joinColumns = @JoinColumn(name = "parameterid"))
    @Column(name = "value", length = Constants.MAX_LENGTH_NAME)
    @XmlElement(name = "enumeration")
    private List<String> enumeration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executableid")
    @XmlIDREF
    private Executable executable;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean inUse = true;

    @Size(max = Constants.MAX_LENGTH_NAME)
    @NotNull
    @XmlElement
    private String key;

    @Size(max = Constants.MAX_LENGTH_NAME)
    @NotNull
    @XmlElement
    private String label;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean modifiable = true;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean parentAllowsModification = true;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean required = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storageid")
    @XmlIDREF
    private Storage storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitterid")
    @XmlIDREF
    private Submitter submitter;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private ParameterTypeEnum type = ParameterTypeEnum.STRING;

    @Size(max = Constants.MAX_LENGTH_NAME)
    @XmlElement
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workunitid")
    @XmlIDREF
    private Workunit workunit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrappercreatorid")
    @XmlIDREF
    private WrapperCreator wrapperCreator;

    public Parameter() {
    }

    public Parameter(ExecutableContextEnum executableContextEnum, Executable executable) throws InvalidEnumValueException {
        setContext(executableContextEnum);
        setExecutable(executable);
    }

    @Override
    public Parameter clone() throws CloneNotSupportedException {
        Parameter clone = (Parameter) super.clone();
        clone.setParentAllowsModification(true);
        return clone;
    }

    public Parameter clonePartial(AbstractEntity parent) {
        try {
            Parameter clone = clone();
            if (getEnumeration() != null) {
                clone.setEnumeration(new ArrayList<>(getEnumeration()));
            } else {
                clone.setEnumeration(new ArrayList<>());
            }
            clone.setInUse(true);
            clone.setParentAllowsModification(isModifiable());
            clone.resetParent(parent);
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            // Important: use trimmed class name because of hibernate proxy issues.
            String objectClassName = ClassHelper.getTrimmedClassName(object.getClass().getName());
            if (objectClassName != null && objectClassName.equals(getClass().getName())) {
                // check class cast
                Parameter baseEntity = (Parameter) object;
                if (getId() > 0 && getId() == baseEntity.getId()) {
                    // check relative position within the same dataset
                    return 0;
                } else if (getContext().compareTo(baseEntity.getContext()) != 0) {
                    return getContext().compareTo(baseEntity.getContext());
                } else {
                    return getKey().compareTo(baseEntity.getKey());
                }
            }
            throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + object.getClass().getName());
        }
        throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + Constants.NULL);
    }

    public Application getApplication() {
        return application;
    }

    public ExecutableContextEnum getContext() {
        return context;
    }

    @Override
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getKey() != null) {
            addEntityInfoItem(summary, "key", String.valueOf(getKey()));
        }
        if (getLabel() != null) {
            addEntityInfoItem(summary, "label", String.valueOf(getLabel()));
        }
        if (getValue() != null) {
            addEntityInfoItem(summary, "value", String.valueOf(getValue()));
        }
        if (getType() != null) {
            addEntityInfoItem(summary, "type", String.valueOf(getType()));
        }
        if (getContext() != null) {
            addEntityInfoItem(summary, "context", String.valueOf(getContext()));
        }
        addEntityInfoItem(summary, "required", String.valueOf(isRequired()));
        addEntityInfoItem(summary, "modifiable", String.valueOf(isModifiable()));
        addEntityInfoItem(summary, "inUse", String.valueOf(isInUse()));
        if (getExecutable() != null) {
            addEntityInfoItem(summary, "executable", String.valueOf(getExecutable().getId()));
        }
        if (getWorkunit() != null) {
            addEntityInfoItem(summary, "workunit", String.valueOf(getWorkunit().getId()));
        }
        if (getApplication() != null) {
            addEntityInfoItem(summary, "application", String.valueOf(getApplication().getId()));
        }
        if (getSubmitter() != null) {
            addEntityInfoItem(summary, "submitter", String.valueOf(getSubmitter().getId()));
        }
        if (getStorage() != null) {
            addEntityInfoItem(summary, "storage", String.valueOf(getStorage().getId()));
        }
        if (getWrapperCreator() != null) {
            addEntityInfoItem(summary, "wrapperCreator", String.valueOf(getWrapperCreator().getId()));
        }
        return summary.toString();
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    public List<String> getEnumerationSorted() {
        return enumeration == null ? null : enumeration.stream().sorted().collect(Collectors.toList());
    }

    public Executable getExecutable() {
        return executable;
    }

    public String getInUseStyleClass() {
        return isInUse() ? Constants.EMPTY_STRING : "parameter-not-in-use";
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public Storage getStorage() {
        return storage;
    }

    public Submitter getSubmitter() {
        return submitter;
    }

    public ParameterTypeEnum getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Workunit getWorkunit() {
        return workunit;
    }

    public WrapperCreator getWrapperCreator() {
        return wrapperCreator;
    }

    public boolean isBoolean() {
        return ParameterTypeEnum.BOOLEAN.equals(getType());
    }

    public boolean isEquivalent(Parameter parameter) {
        return parameter != null && parameter.getKey().equals(getKey()) && parameter.getContext().equals(getContext());
    }

    public boolean isInUse() {
        return inUse;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public boolean isParentAllowsModification() {
        return parentAllowsModification;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isString() {
        return ParameterTypeEnum.STRING.equals(getType());
    }

    public boolean isValueBoolean() {
        return isBoolean() && Boolean.TRUE.toString().equals(getValue());
    }

    public void resetParent() {
        this.application = null;
        this.executable = null;
        this.storage = null;
        this.submitter = null;
        this.workunit = null;
        this.wrapperCreator = null;
    }

    public void resetParent(AbstractEntity parent) {
        resetParent();
        if (parent instanceof Application) {
            setApplication((Application) parent);
        }
        if (parent instanceof Executable) {
            setExecutable((Executable) parent);
        }
        if (parent instanceof Storage) {
            setStorage((Storage) parent);
        }
        if (parent instanceof Submitter) {
            setSubmitter((Submitter) parent);
        }
        if (parent instanceof Workunit) {
            setWorkunit((Workunit) parent);
        }
        if (parent instanceof WrapperCreator) {
            setWrapperCreator((WrapperCreator) parent);
        }
    }

    public void setApplication(Application application) {
        resetParent();
        this.application = application;
    }

    public void setContext(ExecutableContextEnum context) {
        this.context = context;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public void setExecutable(Executable executable) {
        resetParent();
        this.executable = executable;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public void setKey(String key) {
        this.key = StringHelper.format(key);
    }

    public void setLabel(String label) {
        this.label = StringHelper.format(label);
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    public void setParentAllowsModification(boolean parentAllowsModification) {
        this.parentAllowsModification = parentAllowsModification;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setStorage(Storage storage) {
        resetParent();
        this.storage = storage;
    }

    public void setSubmitter(Submitter submitter) {
        resetParent();
        this.submitter = submitter;
    }

    public void setType(ParameterTypeEnum type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = StringHelper.format(value);
    }

    public void setValueBoolean(boolean valueBoolean) {
        if (isBoolean()) {
            if (valueBoolean) {
                setValue(Boolean.TRUE.toString());
            } else {
                setValue(Boolean.FALSE.toString());
            }
        }
    }

    public void setWorkunit(Workunit workunit) {
        resetParent();
        this.workunit = workunit;
    }

    public void setWrapperCreator(WrapperCreator wrapperCreator) {
        resetParent();
        this.wrapperCreator = wrapperCreator;
    }
}
