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

import org.bfabric.entity.Container;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Storage;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.FileHelper;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveResource;

public class MFResource extends AbstractMF {

    private final Resource resource;

    private final XMLRequestParameterSaveResource xmlRequestSaveResource;

    public MFResource(Resource resource, XMLRequestParameterSaveResource xmlResourceRequestSave) {
        this.resource = resource;
        this.xmlRequestSaveResource = xmlResourceRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getResource().setWorkunit(getWorkunit());
        getResource().setContainer(getContainer());
        getResource().setArchiveExpirationDate(getArchiveExpirationDate());
        getResource().setExpirationDate(getExpirationDate());
        getResource().setSample(getSample());
        getResource().setDescription(getDescription());
        getResource().setReport(getReport());
        getResource().setInputResource(getInputResource());
        getResource().setUploadedFile(getUploadedFile());
        if (getResource().getUploadedFile() == null) {
            // If base64 is set, the attributes below will be set automatically and corresponding WS input will not be considered.
            getResource().setRelativePath(getRelativePath());
            getResource().setStorage(getStorage());
            getResource().setFileChecksum(getFileChecksum());
            getResource().setSize(getSize());
            getResource().setStatus(getStatus());
        }
        getResource().setName(getName());
        getResource().setCustomAttributes(getXmlRequestSaveResource().getCustomattribute());
    }

    public LocalDateTime getArchiveExpirationDate() throws InvalidDataException {
        if (getXmlRequestSaveResource().getArchiveexpirationdate() != null) {
            return MFHelper.dateTimeValueOf("archiveexpirationdate", getXmlRequestSaveResource().getArchiveexpirationdate());
        }
        return getResource().getArchiveExpirationDate();
    }

    public String getBase64() {
        return getXmlRequestSaveResource().getBase64();
    }

    public Container getContainer() {
        if (getResource().getWorkunit() != null) {
            return getResource().getWorkunit().getContainer();
        }
        return null;
    }

    public String getDescription() {
        if (getXmlRequestSaveResource().getDescription() != null) {
            return getXmlRequestSaveResource().getDescription();
        }
        return getResource().getDescription();
    }

    public LocalDateTime getExpirationDate() throws InvalidDataException {
        if (getXmlRequestSaveResource().getExpirationdate() != null) {
            return MFHelper.dateTimeValueOf("expirationdate", getXmlRequestSaveResource().getExpirationdate());
        }
        return getResource().getExpirationDate();
    }

    public String getFileChecksum() {
        if (getXmlRequestSaveResource().getFilechecksum() != null) {
            return getXmlRequestSaveResource().getFilechecksum();
        }
        return getResource().getFileChecksum();
    }

    public Resource getInputResource() throws InvalidDataException {
        if (getXmlRequestSaveResource().getInputresourceid() != null) {
            Resource inputResource = (Resource) fetch(Resource.class, MFHelper.positiveLongValueOf("inputresourceid", getXmlRequestSaveResource().getInputresourceid()));
            if (!inputResource.getWorkunit().getApplication().isPreceding(getResource().getApplication())) {
                throw new InvalidDataException("Input Resource " + inputResource.getId() + " was created with application " + inputResource.getWorkunit().getApplication().getName()
                    + ". This is not an input/preceding application of " + getWorkunit().getApplication().getName() + "!");
            }
            return inputResource;
        }
        return getResource().getInputResource();
    }

    public String getName() throws Exception {
        if (StringHelper.isNotEmpty(getXmlRequestSaveResource().getName())) {
            MFHelper.checkNotNull("name", getXmlRequestSaveResource().getName());
            return getXmlRequestSaveResource().getName();
        } else if (StringHelper.isEmpty(getResource().getName())) {
            MFHelper.checkNotNull("name", getResource().getRelativePath());
            return FileHelper.getFileName(getResource().getRelativePath());
        }
        return getResource().getName();
    }

    public String getRelativePath() throws Exception {
        if (getUploadedFile() != null || getXmlRequestSaveResource().getRelativepath() != null) {
            return getXmlRequestSaveResource().getRelativepath();
        }
        return getResource().getRelativePath();
    }

    public String getReport() {
        if (getXmlRequestSaveResource().getReport() != null) {
            return getXmlRequestSaveResource().getReport();
        }
        return getResource().getReport();
    }

    public Resource getResource() {
        return resource;
    }

    public Sample getSample() throws InvalidDataException {
        if (getXmlRequestSaveResource().getSampleid() != null) {
            Sample sample = (Sample) fetch(Sample.class, MFHelper.positiveLongValueOf("sampleid", getXmlRequestSaveResource().getSampleid()));
            if (!sample.getContainer().equals(getResource().getContainer())) {
                throw new InvalidDataException("Sample " + sample.getId() + " does not belong to container" + sample.getContainer().getId() + "!");
            }
            return sample;
        }
        return getResource().getSample();
    }

    public long getSize() throws Exception {
        if (StringHelper.isNotEmpty(getXmlRequestSaveResource().getSize())) {
            return MFHelper.nonNegativeLongValueOf("size", getXmlRequestSaveResource().getSize());
        }
        return getResource().getSize();
    }

    public ResourceStatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSaveResource().getStatus() != null) {
            return ResourceStatusEnum.value(getXmlRequestSaveResource().getStatus());
        }
        return getResource().getStatus() != null ? getResource().getStatus() : ResourceStatusEnum.PENDING;
    }

    public Storage getStorage() throws Exception {
        if (getUploadedFile() != null || getXmlRequestSaveResource().getStorageid() != null) {
            return (Storage) fetch(Storage.class, MFHelper.positiveLongValueOf("storageid", getXmlRequestSaveResource().getStorageid()));
        }
        return getResource().getStorage();
    }

    public BfabricUploadedFile getUploadedFile() throws Exception {
        if (getXmlRequestSaveResource().getBase64() != null) {
            BfabricUploadedFile bfabricUploadedFile = decodeAndCreateFile(getXmlRequestSaveResource().getBase64(), getName());
            if (bfabricUploadedFile == null && getResource().isManaged()) {
                // Remove old import file (if exists).
                RepositoryHelper.removeImport(getResource());
            }
            return bfabricUploadedFile;
        }
        return null;
    }

    public Workunit getWorkunit() throws InvalidDataException {
        Workunit ret = getResource().getWorkunit();
        if (getResource().getId() == 0 && ret == null || getXmlRequestSaveResource().getWorkunitid() != null) {
            MFHelper.checkNotNull("workunitid", getXmlRequestSaveResource().getWorkunitid());
            ret = (Workunit) fetch(Workunit.class, MFHelper.positiveLongValueOf("workunitid", getXmlRequestSaveResource().getWorkunitid()));
            if (!ret.getContainer().isExtensible()) {
                throw new InvalidDataException("Workunit's container is not extensible!");
            }
            if (ret.hasSucceedingWorkunits()) {
                throw new InvalidDataException("Workunit is not extensible since it has succeeding workunits!");
            }
        }
        return ret;
    }

    public XMLRequestParameterSaveResource getXmlRequestSaveResource() {
        return xmlRequestSaveResource;
    }
}