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

import org.bfabric.enums.RoleEnum;
import org.bfabric.webservice.request.XMLRequestReadContainer;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.webservice.server.manager.WSContainerManager;

@WebService(serviceName = "container")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class APIContainer extends AbstractAPIWebService {

    @Resource
    private WebServiceContext wsContext;

    @Inject
    private WSContainerManager wsManager;

    @WebMethod
    public XMLResponse read(final @WebParam(name = "parameters") XMLRequestReadContainer parameters) {
        return new AbstractWebMethod(wsContext, parameters) {

            @Override
            protected boolean hasPermission() {
                return hasCurrentUserRoleEnum(RoleEnum.USER) || hasCurrentUserRoleEnum(RoleEnum.FEEDER);
            }

            @Override
            public XMLResponse performOperation() {
                return wsManager.read(parameters.getQuery(), parameters);
            }
        }.execute();
    }
}
