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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Access;
import org.bfabric.entity.Storage;
import org.bfabric.util.StringHelper;

@XmlRootElement(name = "storage")
public class XMLStorage extends XMLAbstractSupervisorBasedEntity {

    @XmlElement
    private String basepath;

    @XmlElement
    private String containerseparated;

    @XmlElement
    private String enabled;

    @XmlElement
    private XMLExecutable executable;

    @XmlElement
    private String host;

    @XmlElement
    private String local;

    @XmlElement
    private String projectfolderprefix;

    @XmlElement
    private String protocol;

    public XMLStorage() {
    }

    public XMLStorage(Storage entity, boolean reference) {
        super(entity, reference);
    }

    public XMLStorage(Storage entity) {
        super(entity);
        if (entity != null) {
            Access access = entity.getAccessSCP();
            if (access != null) {
                setBasepath(access.getBasePath());
                setHost(access.getHost());
                setProtocol(access.getAccessProtocol().getName());
            }
            setLocal(Boolean.toString(entity.isLocal()));
            setEnabled(Boolean.toString(entity.isEnabled()));
            if (entity.getExecutable() != null) {
                setExecutable(new XMLExecutable(entity.getExecutable(), true));
            }
            if (entity.getContainerFolderPrefix() != null) {
                setProjectfolderprefix(entity.getContainerFolderPrefix());
            }
            setProjectseparated(Boolean.toString(StringHelper.isNotEmpty(getProjectfolderprefix())));
        }
    }

    public String getBasepath() {
        return basepath;
    }

    public String getContainerseparated() {
        return containerseparated;
    }

    public String getEnabled() {
        return enabled;
    }

    public XMLExecutable getExecutable() {
        return executable;
    }

    public String getHost() {
        return host;
    }

    public String getLocal() {
        return local;
    }

    public String getProjectfolderprefix() {
        return projectfolderprefix;
    }

    public String getProjectseparated() {
        return containerseparated;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setBasepath(String basepath) {
        this.basepath = basepath;
    }

    public void setContainerseparated(String containerseparated) {
        this.containerseparated = containerseparated;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public void setExecutable(XMLExecutable executable) {
        this.executable = executable;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public void setProjectfolderprefix(String projectfolderprefix) {
        this.projectfolderprefix = projectfolderprefix;
    }

    public void setProjectseparated(String containerseparated) {
        this.containerseparated = containerseparated;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}