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

import org.bfabric.entity.DatasetField;

@XmlAccessorType(XmlAccessType.NONE)
public class XMLDatasetField extends XMLAbstractEntity {

    @XmlElement(required = true)
    private String attributeposition;

    @XmlElement
    private String value;

    public XMLDatasetField() {
    }

    public XMLDatasetField(DatasetField entity) {
        if (entity != null) {
            if (entity.getAttribute() != null) {
                setAttributeposition(String.valueOf(entity.getAttribute().getPosition()));
            }
            if (entity.getValue() != null) {
                setValue(entity.getValue());
            }
        }
    }

    public String getAttributeposition() {
        return attributeposition;
    }

    public String getValue() {
        return value;
    }

    public void setAttributeposition(String attributeposition) {
        this.attributeposition = attributeposition;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
