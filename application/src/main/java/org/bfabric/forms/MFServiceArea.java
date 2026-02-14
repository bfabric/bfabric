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

import org.bfabric.entity.ServiceArea;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveServiceArea;

public class MFServiceArea extends AbstractMF {

    private final ServiceArea serviceArea;

    private final XMLRequestParameterSaveServiceArea xmlRequestSaveServiceArea;

    public MFServiceArea(ServiceArea serviceArea, XMLRequestParameterSaveServiceArea xmlServiceAreaRequestSave) {
        this.serviceArea = serviceArea;
        this.xmlRequestSaveServiceArea = xmlServiceAreaRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getServiceArea().setName(getName());
        getServiceArea().setDescription(getDescription());
        getServiceArea().setEnabled(getEnabled());
    }

    public String getDescription() {
        if (getXmlRequestSaveServiceArea().getDescription() != null) {
            return getXmlRequestSaveServiceArea().getDescription();
        }
        return getServiceArea().getDescription();
    }

    public Boolean getEnabled() throws InvalidDataException {
        if (getXmlRequestSaveServiceArea().getEnabled() != null) {
            return MFHelper.booleanValueOf("enabled", getXmlRequestSaveServiceArea().getEnabled());
        }
        return getServiceArea().isEnabled();
    }

    public String getName() {
        if (getXmlRequestSaveServiceArea().getName() != null) {
            return getXmlRequestSaveServiceArea().getName();
        }
        return getServiceArea().getName();
    }

    public ServiceArea getServiceArea() {
        return serviceArea;
    }

    public XMLRequestParameterSaveServiceArea getXmlRequestSaveServiceArea() {
        return xmlRequestSaveServiceArea;
    }
}
