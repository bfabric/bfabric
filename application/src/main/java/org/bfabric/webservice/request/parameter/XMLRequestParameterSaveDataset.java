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

package org.bfabric.webservice.request.parameter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.bfabric.xml.entity.XMLDatasetAttribute;
import org.bfabric.xml.entity.XMLDatasetItem;

public class XMLRequestParameterSaveDataset extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private List<XMLDatasetAttribute> attribute;

    @XmlElement(required = true)
    private String containerid;

    @XmlElement
    private String contenttsv;

    @XmlElement
    private String datasettemplateid;

    @XmlElement
    private List<XMLDatasetItem> item;

    @XmlElement
    private String runid;

    @XmlElement
    private String workunitid;

    public List<XMLDatasetAttribute> getAttribute() {
        return attribute;
    }

    public String getContainerid() {
        return containerid;
    }

    public String getContenttsv() {
        return contenttsv;
    }

    public String getDatasettemplateid() {
        return datasettemplateid;
    }

    public List<XMLDatasetItem> getItem() {
        return item;
    }

    public String getRunid() {
        return runid;
    }

    public String getWorkunitid() {
        return workunitid;
    }

    public void setAttribute(List<XMLDatasetAttribute> attribute) {
        this.attribute = attribute;
    }

    public void setContainerid(String containerid) {
        this.containerid = containerid;
    }

    public void setContenttsv(String contenttsv) {
        this.contenttsv = contenttsv;
    }

    public void setDatasettemplateid(String datasettemplateid) {
        this.datasettemplateid = datasettemplateid;
    }

    public void setItem(List<XMLDatasetItem> item) {
        this.item = item;
    }

    public void setRunid(String runid) {
        this.runid = runid;
    }

    public void setWorkunitid(String workunitid) {
        this.workunitid = workunitid;
    }
}
