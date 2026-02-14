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

package org.bfabric.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.FileHelper;
import org.bfabric.util.StringHelper;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "access_storageid_type_unique", columnNames = { "storageid", "accessTypeId" }) })
@XmlRootElement
@NamedQuery(name = "Access.checkUnique", query = "SELECT a.id FROM Access a WHERE a.storage = :storage and a.accessType = :accessType and a.id <> :id")
public class Access extends AbstractBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessProtocolId")
    @XmlIDREF
    private AccessProtocol accessProtocol;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessTypeId")
    @XmlIDREF
    private AccessType accessType;

    @Column(columnDefinition = "TEXT")
    @NotBlank
    @XmlElement
    private String basePath;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean enabled = true;

    @NotBlank
    @Size(max = 256)
    @XmlElement
    private String host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storageid")
    @XmlIDREF
    @NotNull
    private Storage storage;

    @Size(max = 64)
    @XmlElement
    private String typeSpecificPrefix;

    public Access() {
    }

    public AccessProtocol getAccessProtocol() {
        return accessProtocol;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public String getBasePath() {
        return basePath;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.STORAGEMANAGER;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getFullPathPrefix();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getAccessType() != null) {
            addEntityInfoItem(summary, "accessType", getAccessType().getName());
        }
        if (getAccessProtocol() != null) {
            addEntityInfoItem(summary, "accessProtocol", getAccessProtocol().getName());
        }
        if (StringHelper.isNotEmpty(getHost())) {
            addEntityInfoItem(summary, "host", getHost());
        }
        if (StringHelper.isNotEmpty(getBasePath())) {
            addEntityInfoItem(summary, "basePath", getBasePath());
        }
        if (getStorage() != null) {
            addEntityInfoItem(summary, "storage", getStorage().getDisplayName());
        }
        if (StringHelper.isNotEmpty(getTypeSpecificPrefix())) {
            addEntityInfoItem(summary, "typeSpecificPrefix", getTypeSpecificPrefix());
        }
        return summary.toString();
    }

    public String getFullPathPrefix() {
        return getAccessProtocol() != null ? getFullPrefix(getAccessProtocol().getName() + Constants.PROTOCOL_SEPARATOR + getHost() + getBasePath()) : null;
    }

    public String getFullPrefix(String prefix) {
        String ret = prefix;
        if (ret != null) {
            ret = ret.trim();
            if (!ret.endsWith("/")) {
                ret = ret.concat("/");
            }
        }
        return ret;
    }

    public String getHost() {
        return host;
    }

    public Storage getStorage() {
        return storage;
    }

    public String getTypeSpecificPrefix() {
        return typeSpecificPrefix;
    }

    public boolean isDM() {
        return getAccessType() != null && getAccessType().getName().equals("DM");
    }

    @Override
    public boolean isDeletable() {
        return getId() > 0 && isUpdatable() && (!getStorage().isLocal() || !isFM());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFM() {
        return getAccessType() != null && getAccessType().getName().equals("FM");
    }

    public boolean isHTTP() {
        return getAccessType() != null && getAccessType().getName().equals("HTTP");
    }

    @Override
    public boolean isUpdatable() {
        return getStorage() != null && getStorage().isUpdatable();
    }

    public void setAccessProtocol(AccessProtocol accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public void setBasePath(String basePath) {
        this.basePath = FileHelper.formatBasePath(basePath);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setHost(String host) {
        this.host = StringHelper.format(host);
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setTypeSpecificPrefix(String typeSpecificPrefix) {
        this.typeSpecificPrefix = StringHelper.formatFolderName(typeSpecificPrefix);
    }

    public void switchEnabled() {
        setEnabled(!isEnabled());
    }
}