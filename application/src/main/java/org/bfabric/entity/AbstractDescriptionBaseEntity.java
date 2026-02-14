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
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractDescriptionBaseEntity extends AbstractBaseEntity {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "TEXT")
    @XmlElement
    private String description;

    public AbstractDescriptionBaseEntity() {
    }

    public String getDescription() {
        return description;
    }

    public String getDescriptionTrunc(int maxLength) {
        return StringHelper.truncate(getDescription(), maxLength);
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "description", getDescription());
        return summary.toString();
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());
        return content;
    }

    public void setDescription(String description) {
        this.description = StringHelper.formatText(description);
    }
}
