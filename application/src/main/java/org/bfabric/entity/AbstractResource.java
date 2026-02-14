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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractResource extends AbstractDescriptionNamedBaseEntity implements ResourceDependent {

    private static final long serialVersionUID = 1;

    @Transient
    protected ResourceStatusEnum oldStatus;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    protected ResourceStatusEnum status;

    @NotNull
    @Size(max = Constants.MAX_LENGTH_FILECHECKSUM)
    @XmlElement
    private String fileChecksum = Constants.EMPTY_STRING;

    @Size(max = Constants.MAX_LENGTH_RELATIVE_PATH)
    @XmlElement
    private String relativePath;

    @Column(columnDefinition = "bigint DEFAULT 0")
    @NotNull
    @XmlElement
    private long size = 0;

    @ManyToOne
    @JoinColumn(name = "storageid")
    @XmlIDREF
    private Storage storage;

    @Transient
    private BfabricUploadedFile uploadedFile;

    public AbstractResource() {
        setStatus(ResourceStatusEnum.PENDING);
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus().getLabel());
        }
        addEntityInfoItem(summary, "size", getPrintSize());
        if (getRelativePath() != null) {
            addEntityInfoItem(summary, "relativePath", getRelativePath());
        }
        return summary.toString();
    }

    @Override
    public String getFileChecksum() {
        return fileChecksum;
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
        return getRelativePath();
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    @Override
    protected void postRemove() {
        super.postRemove();
        RepositoryHelper.removeImport(this);
    }

    @Override
    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = StringHelper.format(fileChecksum);
    }

    public void setOldStatus(ResourceStatusEnum oldStatus) {
        this.oldStatus = oldStatus;
    }

    @Override
    public void setRelativePath(String relativePath) {
        this.relativePath = StringHelper.trimEmptyLinesAndControlCharacters(relativePath);
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public void setStatus(ResourceStatusEnum status) {
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