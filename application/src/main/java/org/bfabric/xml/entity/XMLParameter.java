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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Parameter;

@XmlRootElement(name = "parameter")
public class XMLParameter extends XMLAbstractDescriptionBaseEntity {

    @XmlElement
    private XMLApplication application;

    @XmlElement
    private String context;

    @XmlElement
    private List<String> enumeration = new ArrayList<>();

    @XmlElement
    private XMLExecutable executable;

    @XmlElement
    private String inUse;

    @XmlElement
    private String key;

    @XmlElement
    private String label;

    @XmlElement
    private String modifiable;

    @XmlElement
    private String parentAllowsModification;

    @XmlElement
    private String required;

    @XmlElement
    private XMLStorage storage;

    @XmlElement
    private XMLSubmitter submitter;

    @XmlElement
    private String type;

    @XmlElement
    private String value;

    @XmlElement
    private XMLWorkunit workunit;

    @XmlElement
    private XMLWrapperCreator wrapperCreator;

    public XMLParameter() {
    }

    public XMLParameter(Parameter entity, boolean reference) {
        super(entity, reference);
    }

    public XMLParameter(Parameter parameter) {
        super(parameter);
        if (parameter != null) {
            if (parameter.getApplication() != null) {
                setApplication(new XMLApplication(parameter.getApplication(), true));
            }
            if (parameter.getContext() != null) {
                setContext(parameter.getContext().toString());
            }
            if (parameter.getEnumeration() != null) {
                setEnumeration(parameter.getEnumeration());
            }
            if (parameter.getExecutable() != null) {
                setExecutable(new XMLExecutable(parameter.getExecutable(), true));
            }
            setInUse(Boolean.toString(parameter.isInUse()));
            if (parameter.getKey() != null) {
                setKey(parameter.getKey());
            }
            if (parameter.getLabel() != null) {
                setLabel(parameter.getLabel());
            }
            setModifiable(Boolean.toString(parameter.isModifiable()));
            setParentAllowsModification(Boolean.toString(parameter.isParentAllowsModification()));
            setRequired(Boolean.toString(parameter.isRequired()));
            if (parameter.getSubmitter() != null) {
                setSubmitter(new XMLSubmitter(parameter.getSubmitter(), true));
            }
            if (parameter.getStorage() != null) {
                setStorage(new XMLStorage(parameter.getStorage(), true));
            }
            if (parameter.getType() != null) {
                setType(parameter.getType().toString());
            }
            if (parameter.getValue() != null) {
                setValue(parameter.getValue());
            }
            if (parameter.getWorkunit() != null) {
                setWorkunit(new XMLWorkunit(parameter.getWorkunit(), true));
            }
            if (parameter.getWrapperCreator() != null) {
                setWrapperCreator(new XMLWrapperCreator(parameter.getWrapperCreator(), true));
            }
        }
    }

    public XMLApplication getApplication() {
        return application;
    }

    public String getContext() {
        return context;
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    public XMLExecutable getExecutable() {
        return executable;
    }

    public String getInUse() {
        return inUse;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getModifiable() {
        return modifiable;
    }

    public String getParentAllowsModification() {
        return parentAllowsModification;
    }

    public String getRequired() {
        return required;
    }

    public XMLStorage getStorage() {
        return storage;
    }

    public XMLSubmitter getSubmitter() {
        return submitter;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public XMLWorkunit getWorkunit() {
        return workunit;
    }

    public XMLWrapperCreator getWrapperCreator() {
        return wrapperCreator;
    }

    public void setApplication(XMLApplication application) {
        this.application = application;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public void setExecutable(XMLExecutable executable) {
        this.executable = executable;
    }

    public void setInUse(String inUse) {
        this.inUse = inUse;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setModifiable(String modifiable) {
        this.modifiable = modifiable;
    }

    public void setParentAllowsModification(String parentAllowsModification) {
        this.parentAllowsModification = parentAllowsModification;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public void setStorage(XMLStorage storage) {
        this.storage = storage;
    }

    public void setSubmitter(XMLSubmitter submitter) {
        this.submitter = submitter;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setWorkunit(XMLWorkunit workunit) {
        this.workunit = workunit;
    }

    public void setWrapperCreator(XMLWrapperCreator wrapperCreator) {
        this.wrapperCreator = wrapperCreator;
    }
}
