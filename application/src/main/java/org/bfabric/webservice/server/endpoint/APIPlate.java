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

package org.bfabric.webservice.server.endpoint;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import org.bfabric.webservice.request.XMLRequestAddPlateSamples;
import org.bfabric.webservice.request.XMLRequestDelete;
import org.bfabric.webservice.request.XMLRequestReadPlate;
import org.bfabric.webservice.request.XMLRequestRemovePlateSamples;
import org.bfabric.webservice.request.XMLRequestRepositionPlateSamples;
import org.bfabric.webservice.request.XMLRequestSavePlate;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.webservice.server.manager.WSPlateManager;

@WebService(serviceName = "plate")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class APIPlate extends AbstractAPIWebService {

    @Resource
    private WebServiceContext wsContext;

    @Inject
    private WSPlateManager wsManager;

    @WebMethod
    public XMLResponse addSamples(final @WebParam(name = "parameters") XMLRequestAddPlateSamples parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.addSamples(parameters.getPlate());
            }
        }.execute();
    }

    @WebMethod
    public XMLResponse delete(final @WebParam(name = "parameters") XMLRequestDelete parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.delete(parameters.getIdList());
            }
        }.execute();
    }

    @WebMethod
    public XMLResponse read(final @WebParam(name = "parameters") XMLRequestReadPlate parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.read(parameters.getQuery(), parameters);
            }
        }.execute();
    }

    @WebMethod
    public XMLResponse removeSamples(final @WebParam(name = "parameters") XMLRequestRemovePlateSamples parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.removeSamples(parameters.getPlate());
            }
        }.execute();
    }

    @WebMethod
    public XMLResponse repositionSamples(final @WebParam(name = "parameters") XMLRequestRepositionPlateSamples parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.repositionSamples(parameters.getPlate());
            }
        }.execute();
    }

    @WebMethod
    public XMLResponse save(final @WebParam(name = "parameters") XMLRequestSavePlate parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.save(parameters.getPlate(), parameters.getIdonly());
            }
        }.execute();
    }
}