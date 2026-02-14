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

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveJob extends XMLRequestParameterSaveAbstractEntity {

    @XmlElement(required = true)
    private String action;

    @XmlElement
    private String log;

    @XmlElement
    private String logthis;

    @XmlElement(required = true)
    private String parentclassname;

    @XmlElement(required = true)
    private String parentid;

    @XmlElement
    private String requesterid;

    @XmlElement(required = true)
    private String status;

    @XmlElement
    private String url;

    @XmlElement
    private Set<String> workunitid;

    public String getAction() {
        return action;
    }

    public String getLog() {
        return log;
    }

    public String getLogthis() {
        return logthis;
    }

    public String getParentclassname() {
        return parentclassname;
    }

    public String getParentid() {
        return parentid;
    }

    public String getRequesterid() {
        return requesterid;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public Set<String> getWorkunitid() {
        return workunitid;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void setLogthis(String logthis) {
        this.logthis = logthis;
    }

    public void setParentclassname(String parentclassname) {
        this.parentclassname = parentclassname;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public void setRequesterid(String requesterid) {
        this.requesterid = requesterid;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setWorkunitid(Set<String> workunitid) {
        this.workunitid = workunitid;
    }
}
