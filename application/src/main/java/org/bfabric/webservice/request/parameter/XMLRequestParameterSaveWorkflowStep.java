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

public class XMLRequestParameterSaveWorkflowStep extends XMLRequestParameterSaveAbstractEntity {

    @XmlElement(required = true)
    private List<String> datasetid;

    @XmlElement
    private String enddatetime;

    @XmlElement
    private String expectedduration;

    @XmlElement(required = true)
    private List<String> sampleid;

    @XmlElement
    private String startdatetime;

    @XmlElement(required = true)
    private String supervisorid;

    @XmlElement(required = true)
    private String workflowid;

    @XmlElement(required = true)
    private String workflowtemplatestepid;

    @XmlElement(required = true)
    private List<String> workunitid;

    public List<String> getDatasetid() {
        return datasetid;
    }

    public String getEnddatetime() {
        return enddatetime;
    }

    public String getExpectedduration() {
        return expectedduration;
    }

    public List<String> getSampleid() {
        return sampleid;
    }

    public String getStartdatetime() {
        return startdatetime;
    }

    public String getSupervisorid() {
        return supervisorid;
    }

    public String getWorkflowid() {
        return workflowid;
    }

    public String getWorkflowtemplatestepid() {
        return workflowtemplatestepid;
    }

    public List<String> getWorkunitid() {
        return workunitid;
    }

    public void setDatasetid(List<String> datasetid) {
        this.datasetid = datasetid;
    }

    public void setEnddatetime(String enddatetime) {
        this.enddatetime = enddatetime;
    }

    public void setExpectedduration(String expectedduration) {
        this.expectedduration = expectedduration;
    }

    public void setSampleid(List<String> sampleid) {
        this.sampleid = sampleid;
    }

    public void setStartdatetime(String startdatetime) {
        this.startdatetime = startdatetime;
    }

    public void setSupervisorid(String supervisorid) {
        this.supervisorid = supervisorid;
    }

    public void setWorkflowid(String workflowid) {
        this.workflowid = workflowid;
    }

    public void setWorkflowtemplatestepid(String workflowtemplatestepid) {
        this.workflowtemplatestepid = workflowtemplatestepid;
    }

    public void setWorkunitid(List<String> workunitid) {
        this.workunitid = workunitid;
    }
}
