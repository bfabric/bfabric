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

package org.bfabric.entity.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.security.auth.message.AuthException;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Access;
import org.bfabric.entity.Storage;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.FileHelper;
import org.bfabric.util.StringHelper;
import org.primefaces.model.DefaultStreamedContent;

public interface ResourceDependent {

    default String getAbsolutePathByAccessType(String accessType) {
        if (accessType != null && getStorage() != null && getRelativePath() != null) {
            Access accessByType = getStorage().getAccessByType(accessType);
            if (accessByType != null && accessByType.getBasePath() != null && getRelativePath() != null) {
                return accessByType.getBasePath() + getRelativePath();
            }
        }
        return null;
    }

    default String getAbsolutePathFM() {
        return getAbsolutePathByAccessType("FM");
    }

    default DefaultStreamedContent getDefaultStreamedContent() throws IOException, AuthException {
        return getDefaultStreamedContent(getAbsolutePathFM());
    }

    default DefaultStreamedContent getDefaultStreamedContent(String filePath) throws IOException, AuthException {
        if (StringHelper.isEmpty(filePath)) {
            throw new AuthException("No file manager access to resource for " + this);
        }
        File file = new File(filePath);
        FileHelper.checkFileAccessibility(file);
        InputStream stream = Files.newInputStream(file.toPath());
        String contentType = Files.probeContentType(new File(filePath).toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return FileHelper.getDefaultStreamedContent(stream, contentType, ((AbstractNamedBaseEntity) this).getName());
    }

    String getFileChecksum();

    default String getFileName() {
        return getFileName(getRelativePath());
    }

    default String getFileName(String uri) {
        String fileName = null;
        try {
            fileName = FileHelper.getFileName(uri);
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    String getRelativePath();

    long getSize();

    ResourceStatusEnum getStatus();

    Storage getStorage();

    BfabricUploadedFile getUploadedFile();

    default boolean isArchived() {
        return ResourceStatusEnum.ARCHIVED.equals(getStatus());
    }

    default boolean isArchiving() {
        return ResourceStatusEnum.ARCHIVING.equals(getStatus());
    }

    default boolean isAvailable() {
        return ResourceStatusEnum.AVAILABLE.equals(getStatus());
    }

    default boolean isDeleted() {
        return ResourceStatusEnum.DELETED.equals(getStatus());
    }

    default boolean isExpired() {
        return ResourceStatusEnum.EXPIRED.equals(getStatus());
    }

    default boolean isFailed() {
        return ResourceStatusEnum.FAILED.equals(getStatus());
    }

    default boolean isFolder() {
        return StringHelper.isNotEmpty(getFileChecksum()) && getFileChecksum().equalsIgnoreCase(Constants.FOLDER);
    }

    default boolean isPending() {
        return ResourceStatusEnum.PENDING.equals(getStatus());
    }

    default boolean isStreamedContent() {
        try {
            return getDefaultStreamedContent() != null;
        } catch (Exception e) {
            return false;
        }
    }

    default void setArchived() {
        setStatus(ResourceStatusEnum.ARCHIVED);
    }

    default void setArchiving() {
        setStatus(ResourceStatusEnum.ARCHIVING);
    }

    default void setAvailable() {
        setStatus(ResourceStatusEnum.AVAILABLE);
    }

    default void setDeleted() {
        setStatus(ResourceStatusEnum.DELETED);
    }

    default void setFailed() {
        setStatus(ResourceStatusEnum.FAILED);
    }

    void setFileChecksum(String fileChecksum);

    default void setPending() {
        setStatus(ResourceStatusEnum.PENDING);
    }

    void setRelativePath(String relativePath);

    void setSize(long size);

    void setStatus(ResourceStatusEnum resourceStatusEnum);

    void setStorage(Storage storage);

    default void setUploadFileHelper() {
        if (getUploadedFile() != null) {
            ((AbstractNamedBaseEntity) this).setName(getUploadedFile().getFileName());
            setSize(getUploadedFile().getSize());
            setFileChecksum(getUploadedFile().getFileChecksum());
        }
    }
}