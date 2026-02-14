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

import org.bfabric.webservice.request.XMLRequestDelete;
import org.bfabric.webservice.request.XMLRequestReadCustomContainerStatus;
import org.bfabric.webservice.request.XMLRequestSaveCustomContainerStatus;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.webservice.server.manager.WSCustomContainerStatusManager;

@WebService(serviceName = "customcontainerstatus")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class APICustomContainerStatus extends AbstractAPIWebService {

    @Resource
    private WebServiceContext wsContext;

    @Inject
    private WSCustomContainerStatusManager wsManager;

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
    public XMLResponse read(final @WebParam(name = "parameters") XMLRequestReadCustomContainerStatus parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.read(parameters.getQuery(), parameters);
            }
        }.execute();
    }

    @WebMethod
    public XMLResponse save(final @WebParam(name = "parameters") XMLRequestSaveCustomContainerStatus parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            public XMLResponse performOperation() {
                return wsManager.save(parameters.getCustomcontainerstatus(), parameters.getIdonly());
            }
        }.execute();
    }
}
