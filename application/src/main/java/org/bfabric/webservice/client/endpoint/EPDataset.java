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

import org.bfabric.entity.Dataset;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.client.webmethod.ClientWebMethodAddDatasetAttribute;
import org.bfabric.webservice.client.webmethod.ClientWebMethodRemoveDatasetAttribute;
import org.bfabric.webservice.client.webmethod.ClientWebMethodRenameDatasetAttribute;
import org.bfabric.webservice.client.webmethod.ClientWebMethodSwitchDatasetAttributePositions;
import org.bfabric.webservice.client.webmethod.ClientWebMethodSwitchDatasetItemPositions;
import org.bfabric.webservice.request.parameter.XMLRequestParameterAddDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadDataset;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRemoveDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterRenameDatasetAttribute;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDataset;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetAttributePositions;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetItemPositions;
import org.bfabric.xml.entity.XMLDataset;

public class EPDataset extends AbstractEndPoint<XMLDataset> {

    private ClientWebMethodAddDatasetAttribute<XMLDataset, XMLRequestParameterAddDatasetAttribute> wmAddAttribute;

    private AbstractClientWebMethodDelete<XMLDataset> wmDelete;

    private AbstractClientWebMethodRead<XMLDataset, XMLRequestParameterReadDataset> wmRead;

    private ClientWebMethodRemoveDatasetAttribute<XMLDataset, XMLRequestParameterRemoveDatasetAttribute> wmRemoveAttribute;

    private ClientWebMethodRenameDatasetAttribute<XMLDataset, XMLRequestParameterRenameDatasetAttribute> wmRenameAttribute;

    private AbstractClientWebMethodSave<XMLDataset, XMLRequestParameterSaveDataset> wmSave;

    private ClientWebMethodSwitchDatasetAttributePositions<XMLDataset, XMLRequestParameterSwitchDatasetAttributePositions> wmSwitchAttributePositions;

    private ClientWebMethodSwitchDatasetItemPositions<XMLDataset, XMLRequestParameterSwitchDatasetItemPositions> wmSwitchItemPositions;

    public EPDataset(SoapClient soapClient) {
        super(soapClient);
    }

    public ClientWebMethodAddDatasetAttribute<XMLDataset, XMLRequestParameterAddDatasetAttribute> getWmAddAttribute() {
        if (wmAddAttribute == null) {
            wmAddAttribute = new ClientWebMethodAddDatasetAttribute<XMLDataset, XMLRequestParameterAddDatasetAttribute>(this) {
            };
        }
        return wmAddAttribute;
    }

    public AbstractClientWebMethodDelete<XMLDataset> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLDataset>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLDataset, XMLRequestParameterReadDataset> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLDataset, XMLRequestParameterReadDataset>(this) {
            };
        }
        return wmRead;
    }

    public ClientWebMethodRemoveDatasetAttribute<XMLDataset, XMLRequestParameterRemoveDatasetAttribute> getWmRemoveAttribute() {
        if (wmRemoveAttribute == null) {
            wmRemoveAttribute = new ClientWebMethodRemoveDatasetAttribute<XMLDataset, XMLRequestParameterRemoveDatasetAttribute>(this) {
            };
        }
        return wmRemoveAttribute;
    }

    public ClientWebMethodRenameDatasetAttribute<XMLDataset, XMLRequestParameterRenameDatasetAttribute> getWmRenameAttribute() {
        if (wmRenameAttribute == null) {
            wmRenameAttribute = new ClientWebMethodRenameDatasetAttribute<XMLDataset, XMLRequestParameterRenameDatasetAttribute>(this) {
            };
        }
        return wmRenameAttribute;
    }

    public AbstractClientWebMethodSave<XMLDataset, XMLRequestParameterSaveDataset> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLDataset, XMLRequestParameterSaveDataset>(this) {
            };
        }
        return wmSave;
    }

    public ClientWebMethodSwitchDatasetAttributePositions<XMLDataset, XMLRequestParameterSwitchDatasetAttributePositions> getWmSwitchAttributePositions() {
        if (wmSwitchAttributePositions == null) {
            wmSwitchAttributePositions = new ClientWebMethodSwitchDatasetAttributePositions<XMLDataset, XMLRequestParameterSwitchDatasetAttributePositions>(this) {
            };
        }
        return wmSwitchAttributePositions;
    }

    public ClientWebMethodSwitchDatasetItemPositions<XMLDataset, XMLRequestParameterSwitchDatasetItemPositions> getWmSwitchItemPositions() {
        if (wmSwitchItemPositions == null) {
            wmSwitchItemPositions = new ClientWebMethodSwitchDatasetItemPositions<XMLDataset, XMLRequestParameterSwitchDatasetItemPositions>(this) {
            };
        }
        return wmSwitchItemPositions;
    }

    @Override
    public String getWsdl() {
        return getWsdl(Dataset.class);
    }
}
