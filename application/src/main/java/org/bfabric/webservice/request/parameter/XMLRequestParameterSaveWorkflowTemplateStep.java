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

public class XMLRequestParameterSaveWorkflowTemplateStep extends XMLRequestParameterSaveAbstractEnabledBaseEntity {

    @XmlElement
    private String expectedduration;

    @XmlElement
    private String runsperprocess;

    @XmlElement
    private String samplesperprocess;

    @XmlElement(required = true)
    private String workflowtemplateid;

    public String getExpectedduration() {
        return expectedduration;
    }

    public String getRunsperprocess() {
        return runsperprocess;
    }

    public String getSamplesperprocess() {
        return samplesperprocess;
    }

    public String getWorkflowtemplateid() {
        return workflowtemplateid;
    }

    public void setExpectedduration(String expectedduration) {
        this.expectedduration = expectedduration;
    }

    public void setRunsperprocess(String runsperprocess) {
        this.runsperprocess = runsperprocess;
    }

    public void setSamplesperprocess(String samplesperprocess) {
        this.samplesperprocess = samplesperprocess;
    }

    public void setWorkflowtemplateid(String workflowtemplateid) {
        this.workflowtemplateid = workflowtemplateid;
    }
}
