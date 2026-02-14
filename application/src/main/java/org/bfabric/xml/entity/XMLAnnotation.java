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
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Annotation;

@XmlRootElement(name = "annotation")
public class XMLAnnotation extends XMLAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String common;

    @XmlElement
    private String na;

    @XmlElement
    private String released;

    @XmlElement
    private String top;

    @XmlElement
    private String type;

    public XMLAnnotation() {
    }

    public XMLAnnotation(Annotation entity, boolean reference) {
        super(entity, reference);
    }

    public XMLAnnotation(String name, long id) {
        setId(id);
        setClassname(Annotation.class.getSimpleName());
        if (name != null) {
            setName(name);
        }
    }

    public XMLAnnotation(Annotation annotation) {
        super(annotation);
        if (annotation != null) {
            setCommon(Boolean.toString(annotation.isCommon()));
            setReleased(Boolean.toString(annotation.isReleased()));
            setTop(Boolean.toString(annotation.isTop()));
            if (annotation.getType() != null) {
                setType(annotation.getType());
            }
        }
    }

    public String getCommon() {
        return common;
    }

    public String getNa() {
        return na;
    }

    public String getReleased() {
        return released;
    }

    public String getTop() {
        return top;
    }

    public String getType() {
        return type;
    }

    public void setCommon(String common) {
        this.common = common;
    }

    public void setNa(String na) {
        this.na = na;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public void setType(String type) {
        this.type = type;
    }
}
