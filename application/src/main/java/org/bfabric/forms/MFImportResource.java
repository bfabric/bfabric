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

package org.bfabric.forms;

import java.time.LocalDateTime;

import org.bfabric.entity.Application;
import org.bfabric.entity.Container;
import org.bfabric.entity.ImportResource;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Storage;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.FileHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveImportResource;

public class MFImportResource extends AbstractMF {

    private final ImportResource importResource;

    private final XMLRequestParameterSaveImportResource xmlRequestSaveImportResource;

    public MFImportResource(ImportResource importResource, XMLRequestParameterSaveImportResource xmlImportResourceRequestSave) {
        this.importResource = importResource;
        xmlRequestSaveImportResource = xmlImportResourceRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getImportResource().setContainer(getContainer());
        getImportResource().setApplication(getApplication());
        getImportResource().setUrl(getUrl());
        getImportResource().setRelativePath(getRelativePath());
        getImportResource().setSample(getSample());
        getImportResource().setStorage(getStorage());
        getImportResource().setReport(getReport());
        getImportResource().setDescription(getDescription());
        getImportResource().setExpirationDate(getExpirationDate());
        getImportResource().setFileDate(getFileDate());
        getImportResource().setFileChecksum(getFileChecksum());
        getImportResource().setSize(getSize());
        getImportResource().setName(getName());
    }

    public Application getApplication() throws InvalidDataException {
        if (getXmlRequestSaveImportResource().getApplicationid() != null) {
            Application ret = (Application) fetch(Application.class, MFHelper.positiveLongValueOf("applicationid", getXmlRequestSaveImportResource().getApplicationid()));
            if (!ret.isImport()) {
                throw new InvalidDataException("Application " + ret.getId() + " is not an 'Import' application!");
            }
            return ret;
        }
        return getImportResource().getApplication();
    }

    public Container getContainer() throws InvalidDataException {
        if (getXmlRequestSaveImportResource().getContainerid() != null) {
            Container ret = (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveImportResource().getContainerid()));
            if (!ret.isExtensible()) {
                throw new InvalidDataException("Container " + getXmlRequestSaveImportResource().getContainerid() + " is not extensible!");
            }
            return ret;
        }
        return getImportResource().getContainer();
    }

    public String getDescription() {
        if (getXmlRequestSaveImportResource().getDescription() != null) {
            return getXmlRequestSaveImportResource().getDescription();
        }
        return getImportResource().getDescription();
    }

    public LocalDateTime getExpirationDate() throws InvalidDataException {
        if (getXmlRequestSaveImportResource().getExpirationdate() != null) {
            return MFHelper.dateTimeValueOf("expirationdate", getXmlRequestSaveImportResource().getExpirationdate());
        }
        return getImportResource().getExpirationDate();
    }

    public String getFileChecksum() {
        if (getXmlRequestSaveImportResource().getFilechecksum() != null) {
            return getXmlRequestSaveImportResource().getFilechecksum();
        }
        return getImportResource().getFileChecksum();
    }

    public LocalDateTime getFileDate() throws InvalidDataException {
        if (getXmlRequestSaveImportResource().getFiledate() != null) {
            return MFHelper.dateTimeValueOf("filedate", getXmlRequestSaveImportResource().getFiledate());
        }
        return getImportResource().getFileDate();
    }

    public ImportResource getImportResource() {
        return importResource;
    }

    public String getName() throws InvalidDataException {
        if (StringHelper.isNotEmpty(getXmlRequestSaveImportResource().getName())) {
            return getXmlRequestSaveImportResource().getName();
        } else if (getImportResource().getId() == 0 && getImportResource().getUrl() != null) {
            return FileHelper.getFileName(getImportResource().getUrl());
        } else if (getImportResource().getId() == 0 && getImportResource().getRelativePath() != null) {
            return FileHelper.getFileName(getImportResource().getRelativePath());
        }
        return getImportResource().getName();
    }

    public String getRelativePath() {
        if (getXmlRequestSaveImportResource().getRelativepath() != null) {
            return getXmlRequestSaveImportResource().getRelativepath();
        }
        return getImportResource().getRelativePath();
    }

    public String getReport() {
        if (getXmlRequestSaveImportResource().getReport() != null) {
            return getXmlRequestSaveImportResource().getReport();
        }
        return getImportResource().getReport();
    }

    public Sample getSample() throws InvalidDataException {
        if (StringHelper.isNotEmpty(getXmlRequestSaveImportResource().getSampleid())) {
            Sample ret = (Sample) fetch(Sample.class, MFHelper.positiveLongValueOf("sampleid", getXmlRequestSaveImportResource().getSampleid()));
            Container container = getContainer();
            if (!container.equals(ret.getContainer())) {
                throw new InvalidDataException("Sample " + ret.getId() + " belongs to container " + ret.getContainer().getId() + ", but the importresource belongs to container " + container.getId()
                    + ".");
            }
            return ret;
        }
        return getImportResource().getSample();
    }

    public long getSize() throws InvalidDataException {
        if (StringHelper.isNotEmpty(getXmlRequestSaveImportResource().getSize())) {
            return MFHelper.nonNegativeLongValueOf("size", getXmlRequestSaveImportResource().getSize());
        }
        return getImportResource().getSize();
    }

    public Storage getStorage() throws InvalidDataException {
        if (StringHelper.isNotEmpty(getXmlRequestSaveImportResource().getStorageid())) {
            return (Storage) fetch(Storage.class, MFHelper.positiveLongValueOf("storageid", getXmlRequestSaveImportResource().getStorageid()));
        }
        return getImportResource().getStorage();
    }

    public String getUrl() throws InvalidDataException {
        if (getXmlRequestSaveImportResource().getUrl() != null) {
            MFHelper.checkUri(getXmlRequestSaveImportResource().getUrl());
            return getXmlRequestSaveImportResource().getUrl();
        }
        return getImportResource().getUrl();
    }

    public XMLRequestParameterSaveImportResource getXmlRequestSaveImportResource() {
        return xmlRequestSaveImportResource;
    }
}