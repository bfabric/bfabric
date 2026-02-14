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

package org.bfabric.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Asynchronous;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.AbstractResource;
import org.bfabric.entity.Attachment;
import org.bfabric.manager.FacesMessagesManager;
import org.primefaces.event.FileUploadEvent;

@Named
@ViewScoped
public class FileUploadHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private final List<String> uploadErrors = new ArrayList<>();

    @Inject
    private FacesMessagesManager facesMessagesManager;

    private Set<Attachment> initialAttachments = new HashSet<>();

    private Set<BfabricUploadedFile> uploadedFiles = new HashSet<>();

    public FileUploadHelper() {
    }

    public void addClientError() {
        String error = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("error");
        String sanitizedError = StringHelper.format(error);
        if (sanitizedError != null && !uploadErrors.contains(sanitizedError)) {
            uploadErrors.add(sanitizedError);
        }
        uploadErrors.sort(String::compareTo);
        facesMessagesManager.printErrors(uploadErrors);
    }

    @Asynchronous
    public void clearAllUploadData() {
        getUploadedFiles().clear();
        uploadErrors.clear();
    }

    public void clearUploadErrors() {
        uploadErrors.clear();
    }

    public byte[] getImageUpload(FileUploadEvent event) {
        try {
            InputStream file = event.getFile().getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int read;
            while ((read = file.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            buffer.flush();
            BufferedImage bufferedImage = ImageHelper.resizeToFit(ImageIO.read(new ByteArrayInputStream(buffer.toByteArray())), 300, 300);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<Attachment> getInitialAttachments() {
        return initialAttachments;
    }

    public long getMaxAttachmentSize() {
        return ConfigurationHelper.getConfiguration().getMaxAttachmentSize();
    }

    public long getMaxAttachmentSizeInMb() {
        return getMaxAttachmentSize() / 1000000;
    }

    public BfabricUploadedFile getSingleUploadedFile() {
        return !getUploadedFiles().isEmpty() ? getUploadedFiles().stream().findFirst().orElse(null) : null;
    }

    public Set<BfabricUploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    private boolean isValid(BfabricUploadedFile uploadedFile) {
        Set<AbstractResource> uncheckedAttachments = getInitialAttachments().stream().filter(attachment -> !attachment.isChecked()).collect(Collectors.toSet());
        String errorMsg;
        try {
            errorMsg = FileHelper.isValid(uploadedFile, getUploadedFiles(), uncheckedAttachments);
        } catch (IOException e) {
            errorMsg = e.getMessage();
        }
        if (errorMsg != null) {
            uploadErrors.add(errorMsg);
        }
        return errorMsg == null;
    }

    public void listener(FileUploadEvent event) {
        BfabricUploadedFile uploadedFile = new BfabricUploadedFile(event.getFile());
        if (isValid(uploadedFile)) {
            getUploadedFiles().add(uploadedFile);
        }
        uploadErrors.sort(String::compareTo);
        facesMessagesManager.printErrors(uploadErrors);
    }

    public void listenerSingleUpload(FileUploadEvent event) {
        clearAllUploadData();
        listener(event);
    }

    public void setInitialAttachments(Set<Attachment> initialAttachments) {
        this.initialAttachments = initialAttachments;
    }

    public void setUploadedFiles(Set<BfabricUploadedFile> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }
}
