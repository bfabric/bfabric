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

import org.bfabric.entity.WorkflowTemplate;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadWorkflowTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplate;
import org.bfabric.xml.entity.XMLWorkflowTemplate;

public class EPWorkflowTemplate extends AbstractEndPoint<XMLWorkflowTemplate> {

    private AbstractClientWebMethodDelete<XMLWorkflowTemplate> wmDelete;

    private AbstractClientWebMethodRead<XMLWorkflowTemplate, XMLRequestParameterReadWorkflowTemplate> wmRead;

    private AbstractClientWebMethodSave<XMLWorkflowTemplate, XMLRequestParameterSaveWorkflowTemplate> wmSave;

    public EPWorkflowTemplate(SoapClient soapClient) {
        super(soapClient);
    }

    public AbstractClientWebMethodDelete<XMLWorkflowTemplate> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLWorkflowTemplate>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLWorkflowTemplate, XMLRequestParameterReadWorkflowTemplate> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLWorkflowTemplate, XMLRequestParameterReadWorkflowTemplate>(this) {
            };
        }
        return wmRead;
    }

    public AbstractClientWebMethodSave<XMLWorkflowTemplate, XMLRequestParameterSaveWorkflowTemplate> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLWorkflowTemplate, XMLRequestParameterSaveWorkflowTemplate>(this) {
            };
        }
        return wmSave;
    }

    @Override
    public String getWsdl() {
        return getWsdl(WorkflowTemplate.class);
    }
}
