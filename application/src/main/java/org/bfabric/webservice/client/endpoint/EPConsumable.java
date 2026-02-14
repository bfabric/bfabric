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

import org.bfabric.entity.Consumable;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadConsumable;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveConsumable;
import org.bfabric.xml.entity.XMLConsumable;

public class EPConsumable extends AbstractEndPoint<XMLConsumable> {

    private AbstractClientWebMethodDelete<XMLConsumable> wmDelete;

    private AbstractClientWebMethodRead<XMLConsumable, XMLRequestParameterReadConsumable> wmRead;

    private AbstractClientWebMethodSave<XMLConsumable, XMLRequestParameterSaveConsumable> wmSave;

    public EPConsumable(SoapClient soapClient) {
        super(soapClient);
    }

    public AbstractClientWebMethodDelete<XMLConsumable> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLConsumable>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLConsumable, XMLRequestParameterReadConsumable> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLConsumable, XMLRequestParameterReadConsumable>(this) {
            };
        }
        return wmRead;
    }

    public AbstractClientWebMethodSave<XMLConsumable, XMLRequestParameterSaveConsumable> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLConsumable, XMLRequestParameterSaveConsumable>(this) {
            };
        }
        return wmSave;
    }

    @Override
    public String getWsdl() {
        return getWsdl(Consumable.class);
    }
}


