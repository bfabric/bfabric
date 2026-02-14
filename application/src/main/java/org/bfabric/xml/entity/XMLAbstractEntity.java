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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.Link;

@XmlAccessorType(XmlAccessType.NONE)
public class XMLAbstractEntity extends XMLAbstractIdentifiableEntity {

    @XmlAttribute
    private String classname;

    @XmlElement
    private List<XMLCustomAttribute> customattribute = new ArrayList<>();

    @XmlElement
    private String deletable;

    @XmlElement
    private String deletionreport;

    @XmlElement
    private String errorreport;

    @XmlElement
    private List<XMLLink> link = new ArrayList<>();

    @XmlElement
    private String updatable;

    public XMLAbstractEntity() {
    }

    public XMLAbstractEntity(AbstractEntity entity) {
        super(entity);
        if (entity != null) {
            setClassname(entity.getClassName());
            if (entity.getReadRequestParameter() != null && entity.getReadRequestParameter().includedeletableupdateable) {
                setDeletable(String.valueOf(entity.isDeletable()));
                setUpdatable(String.valueOf(entity.isUpdatable()));
            }
            if (entity.getCustomAttributes() != null) {
                for (CustomAttribute customAttribute : entity.getCustomAttributes()) {
                    getCustomattribute().add(new XMLCustomAttribute(customAttribute));
                }
            }
            if (entity.getLinks() != null) {
                for (Link link : entity.getLinks()) {
                    getLink().add(new XMLLink(link, true));
                }
            }
        }
    }

    public XMLAbstractEntity(AbstractEntity entity, boolean reference) {
        if (entity != null && reference) {
            setId(entity.getId());
            setClassname(entity.getClassName());
        }
    }

    public String getClassname() {
        return classname;
    }

    public List<XMLCustomAttribute> getCustomattribute() {
        return customattribute;
    }

    public String getDeletable() {
        return deletable;
    }

    public String getDeletionreport() {
        return deletionreport;
    }

    public String getErrorreport() {
        return errorreport;
    }

    public List<XMLLink> getLink() {
        return link;
    }

    public String getUpdatable() {
        return updatable;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setCustomattribute(List<XMLCustomAttribute> customattribute) {
        this.customattribute = customattribute;
    }

    public void setDeletable(String deletable) {
        this.deletable = deletable;
    }

    public void setDeletionreport(String deletionreport) {
        this.deletionreport = deletionreport;
    }

    public void setErrorreport(String errorreport) {
        this.errorreport = errorreport;
    }

    public void setLink(List<XMLLink> link) {
        this.link = link;
    }

    public void setUpdatable(String updatable) {
        this.updatable = updatable;
    }
}