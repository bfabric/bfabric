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

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveResource extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    protected String expirationdate;

    @XmlElement
    protected String archiveexpirationdate;

    @XmlElement
    private String base64;

    @XmlElement
    private String filechecksum;

    @XmlElement
    private String inputresourceid;

    @XmlElement
    private String relativepath;

    @XmlElement
    private String report;

    @XmlElement
    private String sampleid;

    @XmlElement
    private String size;

    @XmlElement
    private String status;

    @XmlElement
    private String storageid;

    @XmlElement(required = true)
    private String workunitid;

    public String getArchiveexpirationdate() {
        return archiveexpirationdate;
    }

    public String getBase64() {
        return base64;
    }

    public String getExpirationdate() {
        return expirationdate;
    }

    public String getFilechecksum() {
        return filechecksum;
    }

    public String getInputresourceid() {
        return inputresourceid;
    }

    public String getRelativepath() {
        return relativepath;
    }

    public String getReport() {
        return report;
    }

    public String getSampleid() {
        return sampleid;
    }

    public String getSize() {
        return size;
    }

    public String getStatus() {
        return status;
    }

    public String getStorageid() {
        return storageid;
    }

    public String getWorkunitid() {
        return workunitid;
    }

    public void setArchiveexpirationdate(String archiveexpirationdate) {
        this.archiveexpirationdate = archiveexpirationdate;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public void setExpirationdate(String expirationdate) {
        this.expirationdate = expirationdate;
    }

    public void setFilechecksum(String filechecksum) {
        this.filechecksum = filechecksum;
    }

    public void setInputresourceid(String inputresourceid) {
        this.inputresourceid = inputresourceid;
    }

    public void setRelativepath(String relativepath) {
        this.relativepath = relativepath;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public void setSampleid(String sampleid) {
        this.sampleid = sampleid;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStorageid(String storageid) {
        this.storageid = storageid;
    }

    public void setWorkunitid(String workunitid) {
        this.workunitid = workunitid;
    }
}
