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

import org.bfabric.entity.Plate;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.client.webmethod.ClientWebMethodAddPlateSamples;
import org.bfabric.webservice.client.webmethod.ClientWebMethodRemovePlateSamples;
import org.bfabric.webservice.client.webmethod.ClientWebMethodRepositionPlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddPlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadPlate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRemovePlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRepositionPlateSamples;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSavePlate;
import org.bfabric.xml.entity.XMLPlate;

public class EPPlate extends AbstractEndPoint<XMLPlate> {

    private ClientWebMethodAddPlateSamples<XMLPlate, XMLRequestParameterAddPlateSamples> wmAddSamples;

    private AbstractClientWebMethodDelete<XMLPlate> wmDelete;

    private AbstractClientWebMethodRead<XMLPlate, XMLRequestParameterReadPlate> wmRead;

    private ClientWebMethodRemovePlateSamples<XMLPlate, XMLRequestParameterRemovePlateSamples> wmRemoveSamples;

    private ClientWebMethodRepositionPlateSamples<XMLPlate, XMLRequestParameterRepositionPlateSamples> wmRepositionSamples;

    private AbstractClientWebMethodSave<XMLPlate, XMLRequestParameterSavePlate> wmSave;

    public EPPlate(SoapClient soapClient) {
        super(soapClient);
    }

    public ClientWebMethodAddPlateSamples<XMLPlate, XMLRequestParameterAddPlateSamples> getWmAddSamples() {
        if (wmAddSamples == null) {
            wmAddSamples = new ClientWebMethodAddPlateSamples<XMLPlate, XMLRequestParameterAddPlateSamples>(this) {
            };
        }
        return wmAddSamples;
    }

    public AbstractClientWebMethodDelete<XMLPlate> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLPlate>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLPlate, XMLRequestParameterReadPlate> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLPlate, XMLRequestParameterReadPlate>(this) {
            };
        }
        return wmRead;
    }

    public ClientWebMethodRemovePlateSamples<XMLPlate, XMLRequestParameterRemovePlateSamples> getWmRemoveSamples() {
        if (wmRemoveSamples == null) {
            wmRemoveSamples = new ClientWebMethodRemovePlateSamples<XMLPlate, XMLRequestParameterRemovePlateSamples>(this) {
            };
        }
        return wmRemoveSamples;
    }

    public ClientWebMethodRepositionPlateSamples<XMLPlate, XMLRequestParameterRepositionPlateSamples> getWmRepositionSamples() {
        if (wmRepositionSamples == null) {
            wmRepositionSamples = new ClientWebMethodRepositionPlateSamples<XMLPlate, XMLRequestParameterRepositionPlateSamples>(this) {
            };
        }
        return wmRepositionSamples;
    }

    public AbstractClientWebMethodSave<XMLPlate, XMLRequestParameterSavePlate> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLPlate, XMLRequestParameterSavePlate>(this) {
            };
        }
        return wmSave;
    }

    @Override
    public String getWsdl() {
        return getWsdl(Plate.class);
    }
}
