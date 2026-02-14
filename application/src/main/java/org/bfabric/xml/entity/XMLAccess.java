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

@XmlRootElement(name = "access")
public class XMLAccess extends XMLAbstractBaseEntity {

    @XmlElement
    private String basepath;

    @XmlElement
    private String enabled;

    @XmlElement
    private String host;

    @XmlElement
    private String protocol;

    @XmlElement
    private XMLStorage storage;

    @XmlElement
    private String type;

    @XmlElement
    private String typespecificprefix;

    public XMLAccess() {
    }

    public XMLAccess(Access entity, boolean reference) {
        super(entity, reference);
    }

    public XMLAccess(Access access) {
        super(access);
        if (access != null) {
            if (access.getBasePath() != null) {
                setBasepath(access.getBasePath());
            }
            setEnabled(Boolean.toString(access.isEnabled()));
            if (access.getHost() != null) {
                setHost(access.getHost());
            }
            if (access.getAccessProtocol() != null) {
                setProtocol(access.getAccessProtocol().getName());
            }
            if (access.getStorage() != null) {
                setStorage(new XMLStorage(access.getStorage(), true));
            }
            if (access.getAccessType() != null) {
                setType(access.getAccessType().getName());
            }
            if (access.getTypeSpecificPrefix() != null) {
                setTypespecificprefix(access.getTypeSpecificPrefix());
            }
        }
    }

    public String getBasepath() {
        return basepath;
    }

    public String getEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public String getProtocol() {
        return protocol;
    }

    public XMLStorage getStorage() {
        return storage;
    }

    public String getType() {
        return type;
    }

    public String getTypespecificprefix() {
        return typespecificprefix;
    }

    public void setBasepath(String basepath) {
        this.basepath = basepath;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setStorage(XMLStorage storage) {
        this.storage = storage;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypespecificprefix(String typespecificprefix) {
        this.typespecificprefix = typespecificprefix;
    }
}