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

package org.bfabric.webservice.client.endpoint;

import org.bfabric.entity.Resource;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadResourceContent;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveResource;
import org.bfabric.xml.entity.XMLResource;

public class EPResource extends AbstractEndPoint<XMLResource> {

    private AbstractClientWebMethodDelete<XMLResource> wmDelete;

    private AbstractClientWebMethodRead<XMLResource, XMLRequestParameterReadResource> wmRead;

    private AbstractClientWebMethodRead<XMLResource, XMLRequestParameterReadResourceContent> wmReadContent;

    private AbstractClientWebMethodSave<XMLResource, XMLRequestParameterSaveResource> wmSave;

    public EPResource(SoapClient soapClient) {
        super(soapClient);
    }

    public AbstractClientWebMethodDelete<XMLResource> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLResource>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLResource, XMLRequestParameterReadResource> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLResource, XMLRequestParameterReadResource>(this) {
            };
        }
        return wmRead;
    }

    public AbstractClientWebMethodRead<XMLResource, XMLRequestParameterReadResourceContent> getWmReadContent() {
        if (wmReadContent == null) {
            wmReadContent = new AbstractClientWebMethodRead<XMLResource, XMLRequestParameterReadResourceContent>(this) {
            };
        }
        return wmReadContent;
    }

    public AbstractClientWebMethodSave<XMLResource, XMLRequestParameterSaveResource> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLResource, XMLRequestParameterSaveResource>(this) {
            };
        }
        return wmSave;
    }

    @Override
    public String getWsdl() {
        return getWsdl(Resource.class);
    }
}
