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

package org.bfabric.webservice.request.parameter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveParameter extends XMLRequestParameterSaveAbstractDescriptionBaseEntity {

    @XmlElement
    private String applicationid;

    @XmlElement
    private String context;

    @XmlElement
    private List<String> enumeration;

    @XmlElement
    private String executableid;

    @XmlElement
    private String key;

    @XmlElement
    private String label;

    @XmlElement
    private String modifiable;

    @XmlElement
    private String required;

    @XmlElement
    private String storageid;

    @XmlElement
    private String submitterid;

    @XmlElement
    private String type;

    @XmlElement
    private String value;

    @XmlElement
    private String workunitid;

    @XmlElement
    private String wrappercreatorid;

    public String getApplicationid() {
        return applicationid;
    }

    public String getContext() {
        return context;
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    public String getExecutableid() {
        return executableid;
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

    public String getRequired() {
        return required;
    }

    public String getStorageid() {
        return storageid;
    }

    public String getSubmitterid() {
        return submitterid;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getWorkunitid() {
        return workunitid;
    }

    public String getWrappercreatorid() {
        return wrappercreatorid;
    }

    public boolean isEmpty() {
        return getId() == null && getModifiable() == null && getDescription() == null && getKey() == null && getLabel() == null && getRequired() == null && getType() == null && getValue() == null
            && (getEnumeration() == null || getEnumeration().isEmpty());
    }

    public void setApplicationid(String applicationid) {
        this.applicationid = applicationid;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public void setExecutableid(String executableid) {
        this.executableid = executableid;
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

    public void setRequired(String required) {
        this.required = required;
    }

    public void setStorageid(String storageid) {
        this.storageid = storageid;
    }

    public void setSubmitterid(String submitterid) {
        this.submitterid = submitterid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setWorkunitid(String workunitid) {
        this.workunitid = workunitid;
    }

    public void setWrappercreatorid(String wrappercreatorid) {
        this.wrappercreatorid = wrappercreatorid;
    }
}
