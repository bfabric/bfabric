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
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.entity.DatasetField;
import org.bfabric.entity.DatasetItem;

@XmlAccessorType(XmlAccessType.NONE)
public class XMLDatasetItem {

    @XmlElement
    private List<XMLDatasetField> field = new ArrayList<>();

    @XmlElement(required = true)
    private String position;

    public XMLDatasetItem() {
    }

    public XMLDatasetItem(DatasetItem item) {
        if (item != null) {
            if (item.getFields() != null) {
                for (DatasetField aField : item.getFields()) {
                    getField().add(new XMLDatasetField(aField));
                }
            }
            setPosition(String.valueOf(item.getPosition()));
        }
    }

    public List<XMLDatasetField> getField() {
        return field;
    }

    public String getPosition() {
        return position;
    }

    public void setField(List<XMLDatasetField> field) {
        this.field = field;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
