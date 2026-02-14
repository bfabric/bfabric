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

package org.bfabric.xml.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Executable;
import org.bfabric.entity.Parameter;

@XmlRootElement(name = "executable")
public class XMLExecutable extends XMLAbstractResource {

    @XmlElement
    private String base64;

    @XmlElement(required = true)
    private String context;

    @XmlElement
    private String enabled;

    @XmlElement
    private XMLExecutable masterexecutable;

    @XmlElement
    private List<XMLParameter> parameter = new ArrayList<>();

    @XmlElement
    private XMLExecutable predecessor;

    @XmlElement
    private String program;

    @XmlElement
    private String statusmodified;

    @XmlElement
    private XMLUser statusmodifiedby;

    @XmlElement
    private XMLUser supervisor;

    @XmlElement
    private String valid;

    @XmlElement
    private String version;

    @XmlElement
    private XMLWorkunit workunit;

    @XmlElement
    private XMLExecutable wrappercreatorexecutable;

    public XMLExecutable() {
    }

    public XMLExecutable(Executable entity) {
        this(false, entity);
    }

    public XMLExecutable(boolean expanded, Executable entity) {
        super(entity);
        if (entity != null) {
            setBase64(entity.getBase64());
            if (entity.getContext() != null) {
                setContext(entity.getContext());
            }
            setEnabled(Boolean.toString(entity.isEnabled()));
            if (entity.getMasterExecutable() != null) {
                setMasterexecutable(new XMLExecutable(entity.getMasterExecutable(), true));
            }
            if (entity.getWrapperCreatorExecutable() != null) {
                setWrappercreatorexecutable(new XMLExecutable(entity.getWrapperCreatorExecutable(), true));
            }
            if (entity.getParameters() != null) {
                for (Parameter aParameter : entity.getParameters()) {
                    if (expanded) {
                        getParameters().add(new XMLParameter(aParameter));
                    } else {
                        getParameters().add(new XMLParameter(aParameter, true));
                    }
                }
            }
            if (entity.getPredecessor() != null) {
                setPredecessor(new XMLExecutable(entity.getPredecessor(), true));
            }
            setProgram(entity.getProgram());
            if (entity.getStatusModified() != null) {
                setStatusmodified(String.valueOf(entity.getStatusModified()));
            }
            if (entity.getStatusModifiedBy() != null) {
                setStatusmodifiedby(new XMLUser(entity.getStatusModifiedBy(), true));
            }
            if (entity.getSupervisor() != null) {
                setSupervisor(new XMLUser(entity.getSupervisor(), true));
            }
            setEnabled(Boolean.toString(entity.isEnabled()));
            setValid(Boolean.toString(entity.isValid()));
            setVersion(entity.getVersion());
            if (entity.getWorkunit() != null) {
                setWorkunit(new XMLWorkunit(entity.getWorkunit(), true));
            }
        }
    }

    public XMLExecutable(Executable entity, boolean reference) {
        super(entity, reference);
    }

    public String getBase64() {
        return base64;
    }

    public String getContext() {
        return context;
    }

    public String getEnabled() {
        return enabled;
    }

    public XMLExecutable getMasterexecutable() {
        return masterexecutable;
    }

    public List<XMLParameter> getParameters() {
        return parameter;
    }

    public XMLExecutable getPredecessor() {
        return predecessor;
    }

    public String getProgram() {
        return program;
    }

    public String getStatusmodified() {
        return statusmodified;
    }

    public XMLUser getStatusmodifiedby() {
        return statusmodifiedby;
    }

    public XMLUser getSupervisor() {
        return supervisor;
    }

    public String getValid() {
        return valid;
    }

    public String getVersion() {
        return version;
    }

    public XMLWorkunit getWorkunit() {
        return workunit;
    }

    public XMLExecutable getWrappercreatorexecutable() {
        return wrappercreatorexecutable;
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

    public void setMasterexecutable(XMLExecutable masterexecutable) {
        this.masterexecutable = masterexecutable;
    }

    public void setParameters(List<XMLParameter> parameters) {
        this.parameter = parameters;
    }

    public void setPredecessor(XMLExecutable predecessor) {
        this.predecessor = predecessor;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void setStatusmodified(String statusmodified) {
        this.statusmodified = statusmodified;
    }

    public void setStatusmodifiedby(XMLUser statusmodifiedby) {
        this.statusmodifiedby = statusmodifiedby;
    }

    public void setSupervisor(XMLUser supervisor) {
        this.supervisor = supervisor;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setWorkunit(XMLWorkunit workunit) {
        this.workunit = workunit;
    }

    public void setWrappercreatorexecutable(XMLExecutable wrappercreatorexecutable) {
        this.wrappercreatorexecutable = wrappercreatorexecutable;
    }
}
