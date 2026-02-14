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
import javax.xml.bind.annotation.XmlAttribute;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.xml.JAXBMarshaller;

@XmlAccessorType(XmlAccessType.NONE)
public class XMLAbstractIdentifiableEntity {

    @XmlAttribute
    protected Long id;

    private SoapClient soapClient;

    public XMLAbstractIdentifiableEntity() {
    }

    public XMLAbstractIdentifiableEntity(AbstractEntity entity) {
        if (entity != null) {
            setId(entity.getId());
        }
    }

    public Long getId() {
        return id;
    }

    public String getIdString() {
        return String.valueOf(id);
    }

    public SoapClient getSoapClient() {
        return soapClient;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSoapClient(SoapClient soapClient) {
        this.soapClient = soapClient;
    }

    @Override
    public String toString() {
        return JAXBMarshaller.getXmlAsText(this);
    }
}
