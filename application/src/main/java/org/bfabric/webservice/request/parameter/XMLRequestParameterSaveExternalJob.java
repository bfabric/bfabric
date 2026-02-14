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

public class XMLRequestParameterSaveExternalJob extends XMLRequestParameterSaveAbstractEntity {

    @XmlElement(required = true)
    private String action;

    @XmlElement
    private String containerid;

    @XmlElement
    private String executableid;

    @XmlElement
    private String logthis;

    @XmlElement
    private String status;

    @XmlElement
    private String userid;

    @XmlElement
    private String workunitid;

    public String getAction() {
        return action;
    }

    public String getContainerid() {
        return containerid;
    }

    public String getExecutableid() {
        return executableid;
    }

    public String getLogthis() {
        return logthis;
    }

    public String getStatus() {
        return status;
    }

    public String getUserid() {
        return userid;
    }

    public String getWorkunitid() {
        return workunitid;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContainerid(String containerid) {
        this.containerid = containerid;
    }

    public void setExecutableid(String executableid) {
        this.executableid = executableid;
    }

    public void setLogthis(String logthis) {
        this.logthis = logthis;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setWorkunitid(String workunitid) {
        this.workunitid = workunitid;
    }
}
