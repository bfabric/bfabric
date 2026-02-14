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

import org.bfabric.entity.AbstractContainerDependentEntity;

public class XMLContainerReferencingEntity extends XMLAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private XMLContainer container;

    @XmlElement
    private String containerid;

    public XMLContainerReferencingEntity() {
    }

    public XMLContainerReferencingEntity(AbstractContainerDependentEntity entity, boolean reference) {
        super(entity, reference);
    }

    public XMLContainerReferencingEntity(AbstractContainerDependentEntity entity) {
        super(entity);
        if (entity != null && entity.getContainer() != null) {
            setContainer(new XMLContainer(entity.getContainer(), true));
            // The next line is due to legacy nd actually not needed.
            setContainerid(entity.getContainer().getIdString());
        }
    }

    public XMLContainer getContainer() {
        return container;
    }

    public String getContainerid() {
        return containerid;
    }

    public void setContainer(XMLContainer container) {
        this.container = container;
    }

    public void setContainerid(String containerid) {
        this.containerid = containerid;
    }
}
