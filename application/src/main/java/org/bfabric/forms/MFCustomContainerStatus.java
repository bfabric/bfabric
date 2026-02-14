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

import org.bfabric.entity.Container;
import org.bfabric.entity.CustomContainerStatus;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCustomContainerStatus;

public class MFCustomContainerStatus extends AbstractMF {

    private final CustomContainerStatus customContainerStatus;

    private final XMLRequestParameterSaveCustomContainerStatus xmlRequestSaveCustomContainerStatus;

    public MFCustomContainerStatus(CustomContainerStatus customContainerStatus, XMLRequestParameterSaveCustomContainerStatus xmlRequestSaveCustomContainerStatus) {
        this.customContainerStatus = customContainerStatus;
        this.xmlRequestSaveCustomContainerStatus = xmlRequestSaveCustomContainerStatus;
    }

    @Override
    public synchronized void apply() throws Exception {
        getCustomContainerStatus().setName(getName());
        getCustomContainerStatus().setContainer(getContainer());
    }

    public Container getContainer() throws InvalidDataException {
        if (getXmlRequestSaveCustomContainerStatus().getContainerid() != null) {
            return (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveCustomContainerStatus().getContainerid()));
        }
        return getCustomContainerStatus().getContainer();
    }

    public CustomContainerStatus getCustomContainerStatus() {
        return customContainerStatus;
    }

    public String getName() {
        if (getXmlRequestSaveCustomContainerStatus().getName() != null) {
            return getXmlRequestSaveCustomContainerStatus().getName();
        }
        return getCustomContainerStatus().getName();
    }

    public XMLRequestParameterSaveCustomContainerStatus getXmlRequestSaveCustomContainerStatus() {
        return xmlRequestSaveCustomContainerStatus;
    }
}
