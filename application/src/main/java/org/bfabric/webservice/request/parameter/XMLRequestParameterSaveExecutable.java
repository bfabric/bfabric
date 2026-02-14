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
import javax.xml.bind.annotation.XmlRootElement;

// IMPORTANT: Do not remove @XMLRootElement!
@XmlRootElement(name = "executable")
public class XMLRequestParameterSaveExecutable extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String base64;

    @XmlElement
    private String context;

    @XmlElement
    private String enabled;

    @XmlElement
    private String masterexecutableid;

    @XmlElement
    private List<XMLRequestParameterSaveParameter> parameter;

    @XmlElement
    private String predecessorid;

    @XmlElement
    private String program;

    @XmlElement
    private String status;

    @XmlElement
    private String supervisorid;

    @XmlElement
    private String valid;

    @XmlElement
    private String version;

    @XmlElement
    private String workunitid;

    @XmlElement
    private String wrappercreatorexecutableid;

    public String getBase64() {
        return base64;
    }

    public String getContext() {
        return context;
    }

    public String getEnabled() {
        return enabled;
    }

    public String getMasterexecutableid() {
        return masterexecutableid;
    }

    public List<XMLRequestParameterSaveParameter> getParameter() {
        return parameter;
    }

    public String getPredecessorid() {
        return predecessorid;
    }

    public String getProgram() {
        return program;
    }

    public String getStatus() {
        return status;
    }

    public String getSupervisorid() {
        return supervisorid;
    }

    public String getValid() {
        return valid;
    }

    public String getVersion() {
        return version;
    }

    public String getWorkunitid() {
        return workunitid;
    }

    public String getWrappercreatorexecutableid() {
        return wrappercreatorexecutableid;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public void setMasterexecutableid(String masterexecutableid) {
        this.masterexecutableid = masterexecutableid;
    }

    public void setParameter(List<XMLRequestParameterSaveParameter> parameter) {
        this.parameter = parameter;
    }

    public void setPredecessorid(String predecessorid) {
        this.predecessorid = predecessorid;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSupervisorid(String supervisorid) {
        this.supervisorid = supervisorid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setWorkunitid(String workunitid) {
        this.workunitid = workunitid;
    }

    public void setWrappercreatorexecutableid(String wrappercreatorexecutableid) {
        this.wrappercreatorexecutableid = wrappercreatorexecutableid;
    }
}
