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

import org.bfabric.entity.DatasetTemplate;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.client.webmethod.ClientWebMethodRenameDatasetTemplateAttribute;
import org.bfabric.webservice.client.webmethod.ClientWebMethodSwitchDatasetTemplateAttributePositions;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadDatasetTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRenameDatasetTemplateAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDatasetTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetTemplateAttributePositions;
import org.bfabric.xml.entity.XMLDatasetTemplate;

public class EPDatasetTemplate extends AbstractEndPoint<XMLDatasetTemplate> {

    private AbstractClientWebMethodDelete<XMLDatasetTemplate> wmDelete;

    private AbstractClientWebMethodRead<XMLDatasetTemplate, XMLRequestParameterReadDatasetTemplate> wmRead;

    private ClientWebMethodRenameDatasetTemplateAttribute<XMLDatasetTemplate, XMLRequestParameterRenameDatasetTemplateAttribute> wmRenameAttribute;

    private AbstractClientWebMethodSave<XMLDatasetTemplate, XMLRequestParameterSaveDatasetTemplate> wmSave;

    private ClientWebMethodSwitchDatasetTemplateAttributePositions<XMLDatasetTemplate, XMLRequestParameterSwitchDatasetTemplateAttributePositions> wmSwitchAttributePositions;

    public EPDatasetTemplate(SoapClient soapClient) {
        super(soapClient);
    }

    public AbstractClientWebMethodDelete<XMLDatasetTemplate> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLDatasetTemplate>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLDatasetTemplate, XMLRequestParameterReadDatasetTemplate> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLDatasetTemplate, XMLRequestParameterReadDatasetTemplate>(this) {
            };
        }
        return wmRead;
    }

    public ClientWebMethodRenameDatasetTemplateAttribute<XMLDatasetTemplate, XMLRequestParameterRenameDatasetTemplateAttribute> getWmRenameAttribute() {
        if (wmRenameAttribute == null) {
            wmRenameAttribute = new ClientWebMethodRenameDatasetTemplateAttribute<XMLDatasetTemplate, XMLRequestParameterRenameDatasetTemplateAttribute>(this) {
            };
        }
        return wmRenameAttribute;
    }

    public AbstractClientWebMethodSave<XMLDatasetTemplate, XMLRequestParameterSaveDatasetTemplate> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLDatasetTemplate, XMLRequestParameterSaveDatasetTemplate>(this) {
            };
        }
        return wmSave;
    }

    public ClientWebMethodSwitchDatasetTemplateAttributePositions<XMLDatasetTemplate, XMLRequestParameterSwitchDatasetTemplateAttributePositions> getWmSwitchAttributePositions() {
        if (wmSwitchAttributePositions == null) {
            wmSwitchAttributePositions = new ClientWebMethodSwitchDatasetTemplateAttributePositions<XMLDatasetTemplate, XMLRequestParameterSwitchDatasetTemplateAttributePositions>(this) {
            };
        }
        return wmSwitchAttributePositions;
    }

    @Override
    public String getWsdl() {
        return getWsdl(DatasetTemplate.class);
    }
}
