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

import org.bfabric.entity.OfferedCharge;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadOfferedCharge;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOfferedCharge;
import org.bfabric.xml.entity.XMLOfferedCharge;

public class EPOfferedCharge extends AbstractEndPoint<XMLOfferedCharge> {

    private AbstractClientWebMethodDelete<XMLOfferedCharge> wmDelete;

    private AbstractClientWebMethodRead<XMLOfferedCharge, XMLRequestParameterReadOfferedCharge> wmRead;

    private AbstractClientWebMethodSave<XMLOfferedCharge, XMLRequestParameterSaveOfferedCharge> wmSave;

    public EPOfferedCharge(SoapClient soapClient) {
        super(soapClient);
    }

    public AbstractClientWebMethodDelete<XMLOfferedCharge> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLOfferedCharge>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLOfferedCharge, XMLRequestParameterReadOfferedCharge> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLOfferedCharge, XMLRequestParameterReadOfferedCharge>(this) {
            };
        }
        return wmRead;
    }

    public AbstractClientWebMethodSave<XMLOfferedCharge, XMLRequestParameterSaveOfferedCharge> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLOfferedCharge, XMLRequestParameterSaveOfferedCharge>(this) {
            };
        }
        return wmSave;
    }

    @Override
    public String getWsdl() {
        return getWsdl(OfferedCharge.class);
    }
}
