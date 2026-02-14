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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.security.auth.message.AuthException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.FileHelper;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.primefaces.model.DefaultStreamedContent;

@Entity
@XmlRootElement
@NamedQuery(name = "ImportResource.findEquivalent", query = "SELECT a FROM ImportResource a WHERE a.application = :application AND a.container = :container AND ((a.relativePath = :relativePath AND a.storage = :storage) OR a.url = :url) AND a.id <> :id")
@NamedQuery(name = "ImportResource.findTotalSizeByContainer", query = "SELECT COALESCE(SUM(a.size), 0) FROM ImportResource a WHERE a.container = :container")
@NamedQuery(name = "ImportResource.findNonExpiredByContainerAndApplication", query = "SELECT count(a) FROM ImportResource a WHERE a.application = :application and a.container = :container and a.status = org.bfabric.enums.ResourceStatusEnum.AVAILABLE and (a.expirationDate is null or a.expirationDate > :currentDate)")
public class ImportResource extends AbstractContainerResource {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationid")
    @NotNull
    private Application application;

    @NotNull
    @XmlElement
    private LocalDateTime fileDate = LocalDateTime.now();

    @Column(updatable = false, insertable = false)
    private long resourcesCount;

    @Size(max = Constants.MAX_LENGTH_URL)
    @XmlElement
    private String url;

    @ManyToMany
    @JoinTable(name = "workunitimportresource", joinColumns = @JoinColumn(name = "importresourceid"), inverseJoinColumns = @JoinColumn(name = "workunitid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> workunits = new HashSet<>();

    public ImportResource() {
        setAvailable();
    }

    public ImportResource(final long id) {
        setId(id);
        setAvailable();
    }

    public ImportResource(Workunit workunit, BfabricUploadedFile uploadedFile) {
        if (workunit != null && uploadedFile != null) {
            setApplication(workunit.getApplication());
            setContainer(workunit.getContainer());
            setUploadedFile(uploadedFile);
            setFileDate(LocalDateTime.now());
            setExpirationDateDefault();
            setRelativePath(uploadedFile.getFileName());
            setStorage(RepositoryHelper.getTemporaryStorage());
        }
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.STORAGEMANAGER;
    }

    @Override
    public DefaultStreamedContent getDefaultStreamedContent() throws IOException, AuthException {
        return super.getDefaultStreamedContent();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getApplication() != null) {
            addEntityInfoItem(summary, "application", getApplication().getName());
        }
        if (StringHelper.isNotEmpty(getRelativePath())) {
            addEntityInfoItem(summary, "relativePath", getRelativePath());
        }
        if (StringHelper.isNotEmpty(getPrintSize())) {
            addEntityInfoItem(summary, "size", getPrintSize());
        }
        if (StringHelper.isNotEmpty(getFileChecksum())) {
            addEntityInfoItem(summary, "fileChecksum", getFileChecksum());
        }
        if (getFileDate() != null) {
            addEntityInfoItem(summary, "fileDate", getFileDate());
        }
        if (getExpirationDate() != null) {
            addEntityInfoItem(summary, "expirationDate", getExpirationDate());
        }
        if (StringHelper.isNotEmpty(getReport())) {
            addEntityInfoItem(summary, "report", getReport());
        }
        return summary.toString();
    }

    public LocalDateTime getFileDate() {
        return fileDate;
    }

    @Override
    public String getFileName() {
        String fileName = null;
        try {
            if (StringHelper.isNotEmpty(getUrl())) {
                fileName = FileHelper.getFileName(getUrl());
            } else {
                fileName = FileHelper.getFileName(getRelativePath());
            }
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public long getResourcesCount() {
        return resourcesCount;
    }

    public String getUrl() {
        return url;
    }

    public Set<Workunit> getWorkunits() {
        return workunits;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean isAvailable() {
        return super.isAvailable();
    }

    @Override
    public boolean isDeletable() {
        return getId() > 0 && isUpdatable();
    }

    public boolean isImported() {
        return resourcesCount > 0;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isStreamedContent() {
        return super.isStreamedContent();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public boolean isValidUrl() {
        return UriHelper.isValidUrl(getUrl());
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setFileDate(LocalDateTime fileDate) {
        this.fileDate = fileDate;
    }

    public void setUrl(String url) {
        this.url = StringHelper.format(url);
    }

    public void setWorkunits(Set<Workunit> workunits) {
        this.workunits = workunits;
    }
}
