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

import org.bfabric.entity.WorkflowTemplateStep;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadWorkflowTemplateStep;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplateStep;
import org.bfabric.xml.entity.XMLWorkflowTemplateStep;

public class EPWorkflowTemplateStep extends AbstractEndPoint<XMLWorkflowTemplateStep> {

    private AbstractClientWebMethodDelete<XMLWorkflowTemplateStep> wmDelete;

    private AbstractClientWebMethodRead<XMLWorkflowTemplateStep, XMLRequestParameterReadWorkflowTemplateStep> wmRead;

    private AbstractClientWebMethodSave<XMLWorkflowTemplateStep, XMLRequestParameterSaveWorkflowTemplateStep> wmSave;

    public EPWorkflowTemplateStep(SoapClient soapClient) {
        super(soapClient);
    }

    public AbstractClientWebMethodDelete<XMLWorkflowTemplateStep> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLWorkflowTemplateStep>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLWorkflowTemplateStep, XMLRequestParameterReadWorkflowTemplateStep> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLWorkflowTemplateStep, XMLRequestParameterReadWorkflowTemplateStep>(this) {
            };
        }
        return wmRead;
    }

    public AbstractClientWebMethodSave<XMLWorkflowTemplateStep, XMLRequestParameterSaveWorkflowTemplateStep> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLWorkflowTemplateStep, XMLRequestParameterSaveWorkflowTemplateStep>(this) {
            };
        }
        return wmSave;
    }

    @Override
    public String getWsdl() {
        return getWsdl(WorkflowTemplateStep.class);
    }
}
