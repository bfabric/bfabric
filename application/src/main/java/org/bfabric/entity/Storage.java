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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Messages;
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@Table(uniqueConstraints = { @UniqueConstraint(name = "storage_name_unique", columnNames = { "name" }) })
@NamedQuery(name = "Storage.findByName", query = "SELECT a FROM Storage a WHERE lower(a.name) = lower(:name)")
@NamedQuery(name = "Storage.findEnabled", query = "SELECT a FROM Storage a WHERE a.enabled = true ORDER BY a.name")
public class Storage extends AbstractAssociatedToExecutableEntity implements TechnologiesDependent {

    private static final long serialVersionUID = 1;

    @Transient
    String localStorageAccessCheckResult;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "storage", cascade = { CascadeType.REMOVE })
    @OrderBy("id desc")
    private Set<Access> accesses = new HashSet<>();

    @OneToMany(mappedBy = "storage")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> applications = new HashSet<>();

    @Size(max = 64)
    @XmlElement
    private String containerFolderPrefix;

    @Transient
    private String containerFolderUrl;

    @OneToMany(mappedBy = "storage", cascade = { CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ImportResource> importResources = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @XmlElement
    private boolean local;

    @OneToMany(mappedBy = "storage", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Parameter> parameters = new HashSet<>();

    @OneToMany(mappedBy = "storage")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Resource> resources = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "storagetechnology", joinColumns = @JoinColumn(name = "storageid"), inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technologies")
    private String technologiesAsString;

    public String createAndPrintMessage(String label, String replace) {
        String msg = Messages.get(label).replace("{0}", replace);
        logger.severe(msg);
        return msg;
    }

    public Access getAccessByType(String accessType) {
        if (accessType != null) {
            for (Access access : getAccesses()) {
                if (accessType.equals(access.getAccessType().getName())) {
                    return access;
                }
            }
        }
        return null;
    }

    public Access getAccessDM() {
        return getAccessByType("DM");
    }

    public Access getAccessFM() {
        return getAccessByType("FM");
    }

    public Access getAccessSCP() {
        return getAccessByType("SCP");
    }

    public Set<Access> getAccesses() {
        return accesses;
    }

    public Set<Access> getAccesses(boolean enabledAccess) {
        Set<Access> enabledAccesses = new HashSet<>();
        for (Access access : getAccesses()) {
            if (access.isEnabled() == enabledAccess) {
                enabledAccesses.add(access);
            }
        }
        return enabledAccesses;
    }

    public List<Access> getAccessesAsList() {
        return CollectionHelper.asList(getAccesses());
    }

    public String getAccessesAsString() {
        StringBuilder accessesStringBuilder = new StringBuilder();
        for (Access access : getAccesses()) {
            accessesStringBuilder.append(access.getFullPathPrefix()).append(" ");
        }
        return accessesStringBuilder.toString();
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public String getBasePath() {
        Access access = getAccessFM();
        return access != null ? access.getBasePath() : null;
    }

    public String getContainerFolderPrefix() {
        return containerFolderPrefix;
    }

    public String getContainerFolderUrl() {
        if (containerFolderUrl == null) {
            Access access = getAccessByType("HTTP");
            if (access != null && access.getFullPathPrefix() != null && getContainerFolderPrefix() != null) {
                containerFolderUrl = access.getFullPathPrefix() + getContainerFolderPrefix();
            }
        }
        return containerFolderUrl;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.STORAGEMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "containerFolderPrefix", getContainerFolderPrefix());
        addEntityInfoItem(summary, "local", isLocal());
        if (getTechnologiesAsString() != null) {
            addEntityInfoItem(summary, "technologies", getTechnologiesAsString());
        }
        addEntityInfoItem(summary, "enabled", isEnabled());
        return summary.toString();
    }

    public Set<ImportResource> getImportResources() {
        return importResources;
    }

    public String getLocalStorageAccessCheckResult() {
        if (localStorageAccessCheckResult == null && isLocal()) {
            Access access = getAccessFM();
            if (access != null) {
                File directory = new File(access.getBasePath());
                if (!directory.exists()) {
                    localStorageAccessCheckResult = createAndPrintMessage("storageLocationDoesNotExist", directory.getAbsolutePath());
                } else if (!directory.isDirectory()) {
                    localStorageAccessCheckResult = createAndPrintMessage("storageLocationNotDirectory", directory.getAbsolutePath());
                } else if (!directory.canWrite()) {
                    localStorageAccessCheckResult = createAndPrintMessage("storageLocationNotWritable", directory.getAbsolutePath());
                }
            } else {
                localStorageAccessCheckResult = createAndPrintMessage("storageLocationNotConfiguredFileManagerAccess", getName());
            }
        }
        return localStorageAccessCheckResult;
    }

    @Override
    public Set<Parameter> getParameters() {
        return parameters;
    }

    public Map<String, String> getParametersKeyValueMap() {
        Map<String, String> parametersKeyValueMap = new HashMap<>();
        for (Parameter parameter : getParameters()) {
            parametersKeyValueMap.put(parameter.getKey(), parameter.getValue());
        }
        return parametersKeyValueMap;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    @Override
    public Set<Technology> getTechnologies() {
        return technologies;
    }

    @Override
    public String getTechnologiesAsString() {
        return technologiesAsString;
    }

    @Override
    public boolean hasNoDependents() {
        return getApplications().isEmpty() && getImportResources().isEmpty() && getResources().isEmpty();
    }

    public boolean isAccessDMEnabled() {
        return getAccessDM() != null && getAccessDM().isEnabled();
    }

    @Override
    public boolean isDeletable() {
        return super.isDeletable() && !isLocal();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLocal() {
        return local;
    }

    @Override
    public boolean isReadable() {
        return isCreatable() || hasCurrentUserRoleEnum(RoleEnum.STORAGEREADER);
    }

    @Override
    public boolean isUpdatable() {
        return super.isUpdatable() && isAdminOrSupervisor();
    }

    public void setAccesses(Set<Access> accesses) {
        this.accesses = accesses;
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public void setContainerFolderPrefix(String containerFolderPrefix) {
        this.containerFolderPrefix = StringHelper.formatFolderName(containerFolderPrefix);
    }

    public void setImportResources(Set<ImportResource> importResources) {
        this.importResources = importResources;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    @Override
    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    @Override
    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }
}
