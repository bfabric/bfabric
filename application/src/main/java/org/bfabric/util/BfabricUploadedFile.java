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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.bfabric.Constants;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;

public class BfabricUploadedFile implements UploadedFile, Serializable {

    private static final long serialVersionUID = 1;

    private final String contentType;

    private final byte[] data;

    private final String fileName;

    public BfabricUploadedFile(byte[] data, String fileName, String contentType) {
        this.data = data != null ? data.clone() : null;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public BfabricUploadedFile(UploadedFile uploadedFile) {
        this.data = uploadedFile.getContent();
        this.fileName = uploadedFile.getFileName();
        this.contentType = uploadedFile.getContentType();
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void delete() throws IOException {
    }

    @Override
    public byte[] getContent() {
        return data != null ? data.clone() : null;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public DefaultStreamedContent getDefaultStreamedContent() {
        return FileHelper.getDefaultStreamedContent(getInputStream(), getContentType(), getFileName());
    }

    public String getFileChecksum() {
        return FileHelper.calculateHash(getInputStream());
    }

    public String getFileContent() {
        StringBuilder fileContentBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getInputStream(), ConfigurationHelper.getConfiguration().getDefaultCharset()))) {
            String line = Constants.EMPTY_STRING;
            do {
                fileContentBuilder.append(line);
                line = bufferedReader.readLine();
            } while (line != null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return fileContentBuilder.toString();
    }

    @Override
    public String getFileName() {
        return StringHelper.formatFileName(fileName);
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public long getSize() {
        return data.length;
    }

    @Override
    public void write(String filePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        }
    }
}
