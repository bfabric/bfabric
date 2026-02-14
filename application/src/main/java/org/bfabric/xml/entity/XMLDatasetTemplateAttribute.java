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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.entity.DatasetTemplateAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class XMLDatasetTemplateAttribute {

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private String position;

    @XmlElement
    private String type;

    public XMLDatasetTemplateAttribute() {
    }

    public XMLDatasetTemplateAttribute(DatasetTemplateAttribute datasetTemplateAttribute) {
        if (datasetTemplateAttribute != null) {
            if (datasetTemplateAttribute.getName() != null) {
                setName(datasetTemplateAttribute.getName());
            }
            setPosition(String.valueOf(datasetTemplateAttribute.getPosition()));
            if (datasetTemplateAttribute.getType() != null) {
                setType(datasetTemplateAttribute.getType());
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setType(String type) {
        this.type = type;
    }
}
