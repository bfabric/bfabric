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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.Constants;
import org.bfabric.entity.api.ResourceDependent;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@MappedSuperclass
public abstract class AbstractContainerResource extends AbstractContainerDependentEntity implements ResourceDependent {

    private static final long serialVersionUID = 1;

    @Transient
    protected ResourceStatusEnum oldStatus;

    @Transient
    protected long oldSize;

    @XmlElement
    private LocalDateTime archiveExpirationDate;

    @XmlElement
    private LocalDateTime expirationDate;

    @Size(max = Constants.MAX_LENGTH_FILECHECKSUM)
    @XmlElement
    private String fileChecksum = Constants.EMPTY_STRING;

    @Size(max = Constants.MAX_LENGTH_RELATIVE_PATH)
    @XmlElement
    private String relativePath;

    @Size(max = 32)
    @XmlElement
    private String report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampleid")
    @XmlIDREF
    private Sample sample;

    @Column(columnDefinition = "bigint DEFAULT 0")
    @NotNull
    @XmlElement
    private long size = 0;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private ResourceStatusEnum status = ResourceStatusEnum.PENDING;

    @ManyToOne
    @JoinColumn(name = "storageid")
    @XmlIDREF
    private Storage storage;

    @Transient
    private BfabricUploadedFile uploadedFile;

    public AbstractContainerResource() {
    }

    public EntityLog createDownloadFolderEntityLog() {
        return createEntityLog(LogActionEnum.DOWNLOAD_FOLDER, LogStatusEnum.INVOKED);
    }

    public EntityLog createDownloadHttpEntityLog() {
        return createEntityLog(LogActionEnum.DOWNLOAD_HTTP, LogStatusEnum.INVOKED);
    }

    public EntityLog createWebLinkEntityLog() {
        return createEntityLog(LogActionEnum.ACCESS, LogStatusEnum.INVOKED);
    }

    public abstract Application getApplication();

    public LocalDateTime getArchiveExpirationDate() {
        return archiveExpirationDate;
    }

    @SuppressWarnings("unused")
    public StreamedContent getDownloadShellFile() {
        try {
            String command = getWgetDownloadCommand() + "\n read -r -n1";
            InputStream inputStream = new ByteArrayInputStream(command.getBytes(StandardCharsets.UTF_8));
            String fileName = "download_" + getClassLabelLowerCaseId() + ".sh";
            return DefaultStreamedContent.builder().name(fileName).contentType("application/x-sh").stream(() -> inputStream).build();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Access> getEnabledAccesses() {
        List<Access> enabledAccesses = new ArrayList<>();
        if (getStorage() != null && !getStorage().getAccesses(true).isEmpty()) {
            enabledAccesses.addAll(getStorage().getAccesses(true));
        }
        return enabledAccesses;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus());
        }
        if (getPrintSize() != null) {
            addEntityInfoItem(summary, "size", getPrintSize());
        }
        if (getFileChecksum() != null) {
            addEntityInfoItem(summary, "checksum", getFileChecksum());
        }
        addEntityInfoItem(summary, "relativePath", getRelativePath());
        if (getExpirationDate() != null) {
            addEntityInfoItem(summary, "expirationDate", getExpirationDate());
        }
        if (getArchiveExpirationDate() != null) {
            addEntityInfoItem(summary, "archiveExpirationDate", getArchiveExpirationDate());
        }
        if (getStorage() != null) {
            addEntityInfoItem(summary, "storage", getStorage().getDisplayName());
        }
        if (getSample() != null) {
            addEntityInfoItem(summary, "sample", getSample().getDisplayName());
        }
        return summary.toString();
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public LocalDateTime getExpirationDateDefault() {
        return LocalDateTime.now().plusMonths(1);
    }

    @Override
    public String getFileChecksum() {
        return fileChecksum;
    }

    public long getOldSize() {
        return oldSize;
    }

    public ResourceStatusEnum getOldStatus() {
        return oldStatus;
    }

    public String getPrintSize() {
        return NumberUtils.getPrintSize(getSize());
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    public String getReport() {
        return report;
    }

    public Sample getSample() {
        return sample;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public ResourceStatusEnum getStatus() {
        return status;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public BfabricUploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public String getUri() {
        return getUriByAccessType("SCP");
    }

    public String getUriByAccessType(String accessType) {
        return getUrisByAccessType(accessType).isEmpty() ? null : getUrisByAccessType(accessType).get(0);
    }

    public String getUriDownloadHttp() {
        return getUriByAccessType("HTTP");
    }

    public String getUriDownloadManager() {
        return getUriByAccessType("DM");
    }

    public String getUriWebLink() {
        return getUriByAccessType("LINK");
    }

    public List<String> getUrisByAccessType() {
        return getUrisByAccessType(null);
    }

    public List<String> getUrisByAccessType(String accessType) {
        return getUrisByAccessType(accessType, Boolean.TRUE);
    }

    public List<String> getUrisByAccessType(String accessType, Boolean enabled) {
        List<String> uris = new ArrayList<>();
        if (getRelativePath() != null && getStorage() != null) {
            for (Access access : getStorage().getAccesses()) {
                if ((accessType == null || access.getAccessType().getName().equals(accessType)) && (enabled == null || access.isEnabled() == enabled)) {
                    uris.add(access.getFullPathPrefix() + getRelativePath());
                }
            }
        }
        return uris;
    }

    public String getWgetDownloadCommand() {
        return "wget --user " + getCurrentUsername() + " -e robots=off --ask-password -r --no-parent -nH --cut-dirs=2 --reject='index.html*' " + getUriDownloadHttp();
    }

    @Override
    public boolean isAvailable() {
        return ResourceStatusEnum.AVAILABLE.equals(getStatus());
    }

    public boolean isDownloadButtonRendered() {
        return !isFolder() && isDownloadable() && getUriDownloadManager() != null && getConfiguration().isBrowserDownloadEnabled();
    }

    public boolean isDownloadFolderButtonRendered() {
        return isDownloadable() && getWgetDownloadCommand() != null && getUriDownloadHttp() != null;
    }

    public boolean isDownloadHttpButtonRendered() {
        return isDownloadable() && getUriDownloadHttp() != null;
    }

    public boolean isDownloadManagerDownloadButtonRendered() {
        return !isFolder() && isDownloadable() && getUriDownloadManager() != null && getCurrentUser().isDownloadManagerEnabled();
    }

    public boolean isDownloadable() {
        return (isAvailable() || isArchiving()) && !getUrisByAccessType(null, Boolean.TRUE).isEmpty() && getConfiguration().isDownloadEnabled();
    }

    @Override
    public boolean isFolder() {
        return StringHelper.isNotEmpty(getFileChecksum()) && getFileChecksum().equalsIgnoreCase(Constants.FOLDER);
    }

    public boolean isSizeChanged() {
        return getSize() != getOldSize();
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    public boolean isStreamedContentDownloadButtonRendered() {
        try {
            return RepositoryHelper.getLocalStorage(false).equals(getStorage()) && getDefaultStreamedContent().getStream().get().available() > 0;
        } catch (Exception e) {
            logger.info(e.toString());
        }
        return false;
    }

    public boolean isWebLinkButtonRendered() {
        return isDownloadable() && getUriWebLink() != null;
    }

    public void setArchiveExpirationDate(LocalDateTime archiveExpirationDate) {
        this.archiveExpirationDate = archiveExpirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setExpirationDateDefault() {
        setExpirationDate(getExpirationDateDefault());
    }

    @Override
    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = StringHelper.formatNonNull(fileChecksum);
    }

    public void setOldSize(long oldSize) {
        this.oldSize = oldSize;
    }

    public void setOldStatus(ResourceStatusEnum oldStatus) {
        this.oldStatus = oldStatus;
    }

    @Override
    public void setRelativePath(String relativePath) {
        this.relativePath = StringHelper.trimEmptyLinesAndControlCharacters(relativePath);
    }

    public void setReport(String report) {
        this.report = StringHelper.format(report);
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    @Override
    public void setSize(long size) {
        if (oldSize == 0) {
            setOldSize(getSize());
        }
        this.size = size;
    }

    @Override
    public void setStatus(ResourceStatusEnum status) {
        if (oldStatus == null) {
            setOldStatus(getStatus());
        }
        this.status = status;
    }

    @Override
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setUploadedFile(BfabricUploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
        setUploadFileHelper();
    }
}