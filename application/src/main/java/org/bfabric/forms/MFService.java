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

package org.bfabric.forms;

import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceType;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveService;

public class MFService extends AbstractMF {

    private final Service service;

    private final XMLRequestParameterSaveService xmlRequestSaveService;

    public MFService(Service service, XMLRequestParameterSaveService xmlServiceRequestSave) {
        this.service = service;
        this.xmlRequestSaveService = xmlServiceRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getService().setName(getName());
        getService().setServiceType(getServiceType());
        getService().setDescription(getDescription());
        getService().setEnabled(getEnabled());
    }

    public String getDescription() {
        if (getXmlRequestSaveService().getDescription() != null) {
            return getXmlRequestSaveService().getDescription();
        }
        return getService().getDescription();
    }

    public Boolean getEnabled() throws InvalidDataException {
        if (getXmlRequestSaveService().getEnabled() != null) {
            return MFHelper.booleanValueOf("enabled", getXmlRequestSaveService().getEnabled());
        }
        return getService().isEnabled();
    }

    public String getName() {
        if (getXmlRequestSaveService().getName() != null) {
            return getXmlRequestSaveService().getName();
        }
        return getService().getName();
    }

    public Service getService() {
        return service;
    }

    public ServiceType getServiceType() throws InvalidDataException {
        if (getXmlRequestSaveService().getServicetypeid() != null) {
            return (ServiceType) fetch(ServiceType.class, MFHelper.positiveLongValueOf("servicetypeid", getXmlRequestSaveService().getServicetypeid()));
        }
        return getService().getServiceType();
    }

    public XMLRequestParameterSaveService getXmlRequestSaveService() {
        return xmlRequestSaveService;
    }
}
