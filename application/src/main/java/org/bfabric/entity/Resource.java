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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.security.auth.message.AuthException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexMap;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.primefaces.model.DefaultStreamedContent;

@Entity
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "Resource.findByContainerAndRelativePathAndStorage", query = "SELECT a FROM Resource a WHERE a.container = :container AND a.relativePath = :relativePath AND a.storage = :storage")
@NamedQuery(name = "Resource.findByWorkunitId", query = "SELECT a FROM Resource a WHERE a.workunit.id = :workunitId")
@NamedQuery(name = "Resource.findTotalSizeByContainer", query = "SELECT COALESCE(SUM(a.size), 0) FROM Resource a WHERE a.container = :container AND a.status <> :status")
@NamedQuery(name = "Resource.checkUnique", query = "SELECT a FROM Resource a WHERE a.storage.id = :storageId AND a.relativePath = :relativePath AND a.workunit.id = :workunitId AND a.id <> :id")
@NamedQuery(name = "Resource.checkUniqueImport", query = "SELECT a FROM Resource a WHERE a.container.id = :containerId AND a.storage.id = :storageId AND a.relativePath = :relativePath AND a.workunit.application.id = :applicationId")
@NamedQuery(name = "Resource.findByArchiveExpirationDatePassed", query = "SELECT a FROM Resource a WHERE a.status IN (org.bfabric.enums.ResourceStatusEnum.ARCHIVED,  org.bfabric.enums.ResourceStatusEnum.ARCHIVING) and a.archiveExpirationDate is not null and a.archiveExpirationDate <= current_date")
public class Resource extends AbstractContainerResource implements Indexable {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "workunitinput", joinColumns = @JoinColumn(name = "resourceid"), inverseJoinColumns = @JoinColumn(name = "workunitid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<Workunit> succeedingWorkunits = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inputresourceid")
    @XmlIDREF
    private Resource inputResource;

    @Transient
    private String inputResourcesName;

    @Transient
    private Boolean isRenderedTree;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean junk = false;

    @Size(max = 1024)
    @XmlElement
    private String junkComment;

    @ManyToMany
    @JoinTable(name = "resourcebasketresource", joinColumns = @JoinColumn(name = "resourceid"), inverseJoinColumns = @JoinColumn(name = "resourcebasketid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ResourceBasket> resourceBaskets = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workunitid")
    @NotNull
    @XmlIDREF
    private Workunit workunit;

    public Resource() {
    }

    public Resource(Workunit workunit, ImportResource importResource) {
        if (workunit != null && importResource != null) {
            setName(importResource.getName());
            setDescription(importResource.getDescription());
            setSample(importResource.getSample());
            setWorkunit(workunit);
            setContainer(workunit.getContainer());
            setStorage(workunit.getApplication().getStorage());
            setRelativePath(importResource.getRelativePath());
            setSize(importResource.getSize());
            setFileChecksum(importResource.getFileChecksum());
            setAvailable();
        }
    }

    public static boolean isDownloadButtonRendered(Set<Resource> selectedResources) {
        for (Resource resource : selectedResources) {
            if (resource.isDownloadButtonRendered()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDownloadManagerDownloadButtonRendered(Set<Resource> selectedResources) {
        for (Resource resource : selectedResources) {
            if (resource.isDownloadManagerDownloadButtonRendered()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Application getApplication() {
        return workunit == null ? null : workunit.getApplication();
    }

    @Override
    public DefaultStreamedContent getDefaultStreamedContent() throws IOException, AuthException {
        return super.getDefaultStreamedContent();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getWorkunit() != null) {
            addEntityInfoItem(summary, "workunit", getWorkunit().getDisplayName());
        }
        if (getInputResource() != null) {
            addEntityInfoItem(summary, "inputResource", getInputResource().getDisplayName());
        }
        if (isJunk()) {
            if (getJunkComment() != null) {
                addEntityInfoItem(summary, "junk", getJunkComment());
            } else {
                addEntityInfoItem(summary, null, Messages.get("junk"));
            }
        }
        return summary.toString();
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
        fields.add(IndexMapContentEnum.STORAGEID.getField());
    }

    @Override
    public List<String> getIndexListingFields() {
        final List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.PROJECTID.getField());
        fields.add(IndexMapContentEnum.ORDERID.getField());
        fields.add(IndexMapContentEnum.WORKUNITID.getField());
        fields.add(IndexMapContentEnum.STATUS.getField());
        fields.add(IndexMapContentEnum.SIZE.getField());
        fields.add(IndexMapContentEnum.STORAGE.getField());
        fields.add(IndexMapContentEnum.RELATIVEPATH.getField());
        fields.add(IndexMapContentEnum.FILENAME.getField());
        fields.add(IndexMapContentEnum.FILECHECKSUM.getField());
        fields.add(IndexMapContentEnum.SAMPLEID.getField());
        return fields;
    }

    @Override
    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = super.getIndexMap();
        if (getContainer() != null) {
            indexMap.put(Constants.INDEXMAP_GROUP, getContainer().getMemberRoleName());
            indexMap.put(Constants.INDEXMAP_STATUS, getContainer().getStatus().getLabel());
            indexMap.put(Constants.INDEXMAP_DOI_CREATED, getContainer().getDoiCreated());
        }
        return indexMap;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();
        if (getContainer() != null) {
            if (getContainer().isContainerProject()) {
                content.add(IndexMapContentEnum.PROJECTID, getContainer().getId());
            } else {
                content.add(IndexMapContentEnum.ORDERID, getContainer().getId());
                if (getContainer().getProject() != null) {
                    content.add(IndexMapContentEnum.PROJECTID, getContainer().getProject().getId());
                }
            }
        }
        content.add(IndexMapContentEnum.NAME, getName());
        if (getWorkunit() != null) {
            content.add(IndexMapContentEnum.WORKUNITID, getWorkunit().getId());
        }
        content.add(IndexMapContentEnum.STATUS, getStatus());
        content.add(IndexMapContentEnum.SIZE, getSize(), getPrintSize());
        if (getStorage() != null) {
            content.add(IndexMapContentEnum.STORAGE, getStorage().getDisplayName());
            content.add(IndexMapContentEnum.STORAGEID, getStorage().getId());
        }
        content.add(IndexMapContentEnum.RELATIVEPATH, getRelativePath());
        content.add(IndexMapContentEnum.FILENAME, getFileName());
        content.add(IndexMapContentEnum.FILECHECKSUM, getFileChecksum());
        if (getSample() != null) {
            content.add(IndexMapContentEnum.SAMPLEID, getSample().getId());
        }
        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.RESOURCE;
    }

    public Resource getInputResource() {
        return inputResource;
    }

    public String getInputResourcesName() {
        if (inputResourcesName == null) {
            if (getInputResource() != null) {
                return getInputResource().getName();
            } else if (!getWorkunit().getInputResources().isEmpty()) {
                inputResourcesName = CollectionHelper.print(getWorkunit().getInputResources(), "getName", " ", false);
            }
        }
        return inputResourcesName;
    }

    public String getJunkComment() {
        return junkComment;
    }

    public String getJunkFullComment() {
        return isJunk() ? "Marked as junk! " : Constants.EMPTY_STRING + (getJunkComment() != null ? getJunkComment() : Constants.EMPTY_STRING);
    }

    @Override
    public String getRelativeRepositoryPath() {
        return getWorkunit() != null ? getWorkunit().getRelativeRepositoryPath() : super.getRelativeRepositoryPath();
    }

    public Collection<ResourceBasket> getResourceBaskets() {
        return resourceBaskets;
    }

    public String getRowStyleClass() {
        if (isAvailable()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isFailed()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isDeleted()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isExpired()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isPending()) {
            return Constants.BACKGROUND_COLOR_ORANGE;
        }
        if (isArchived()) {
            return Constants.BACKGROUND_COLOR_BROWN;
        }
        if (isArchiving()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        return Constants.EMPTY_STRING;
    }

    public Sample getSampleTransitive() {
        Sample sample = null;
        if (getSamples().size() == 1) {
            sample = getSamples().get(0);
        }
        return sample;
    }

    public List<Sample> getSamples() {
        final List<Sample> samples = new ArrayList<>();
        if (getSample() != null) {
            samples.add(getSample());
        } else if (getInputResource() != null) {
            samples.addAll(getInputResource().getSamples());
        } else if (!getWorkunit().getInputResources().isEmpty()) {
            for (final Resource resource : getWorkunit().getInputResources()) {
                if (!resource.getSamples().isEmpty()) {
                    samples.addAll(resource.getSamples());
                }
            }
        }
        return CollectionHelper.sortObjects(samples);
    }

    public Resource getSingleInputResource() {
        if (getInputResource() != null) {
            return getInputResource();
        }
        return getWorkunit().getInputResource();
    }

    public Set<Application> getSucceedingApplications() {
        return getWorkunit().getApplication().getSucceedingApplications();
    }

    public String getSucceedingApplicationsNames() {
        return CollectionHelper.print(getSucceedingApplications());
    }

    public List<Dataset> getSucceedingDatasets() {
        return getAssociatedDatasets();
    }

    public Set<Workunit> getSucceedingWorkunits() {
        return succeedingWorkunits;
    }

    public Sample getTransitiveSample() {
        if (getSample() != null) {
            return getSample();
        } else if (getSingleInputResource() != null) {
            return getSingleInputResource().getTransitiveSample();
        }
        return null;
    }

    public Workunit getWorkunit() {
        return workunit;
    }

    public boolean isAnnotated() {
        return !isAnnotationRequired() || getSample() != null;
    }

    public boolean isAnnotationRequired() {
        return getWorkunit() != null && getWorkunit().isAnnotationRequired();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean isAvailable() {
        return super.isAvailable();
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return getId() > 0 && isUpdatable() && getWorkunit() != null && !(getWorkunit().isArchived() || getWorkunit().isDeleted()) && !getWorkunit().hasSucceedingWorkunits();
    }

    @Override
    public boolean isDownloadable() {
        return super.isDownloadable() && isAnnotated();
    }

    public boolean isInputResourcesBased() {
        return getInputResource() != null || getWorkunit() != null && !getWorkunit().getInputResources().isEmpty();
    }

    public boolean isJunk() {
        return junk;
    }

    public boolean isJunkButtonRendered() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || getContainer().isMember();
    }

    public boolean isMultipleInputResourcesBased() {
        return getInputResource() == null && getWorkunit() != null && getWorkunit().getInputResources().size() > 1;
    }

    @Override
    public boolean isReadable() {
        return getContainer() != null && getContainer().isReadable();
    }

    public boolean isRenderedTree() {
        if (isRenderedTree == null) {
            isRenderedTree = !getAssociatedDatasets().isEmpty() || !getSucceedingWorkunits().isEmpty();
        }
        return isRenderedTree;
    }

    public boolean isSingleInputResourceBased() {
        return getInputResource() != null || getWorkunit() != null && getWorkunit().getInputResources().size() == 1;
    }

    @Override
    public boolean isStreamedContent() {
        return super.isStreamedContent();
    }

    @Override
    public boolean isUpdatable() {
        return getContainer() != null && getContainer().isExtensible();
    }

    public void setInputResource(Resource inputResource) {
        this.inputResource = inputResource;

        if (inputResource != null) {
            setSample(inputResource.getTransitiveSample());
        }
    }

    public void setJunk(boolean junk) {
        this.junk = junk;
    }

    public void setJunkComment(String junkComment) {
        this.junkComment = StringHelper.formatText(junkComment);
    }

    public void setResourceBaskets(Set<ResourceBasket> resourceBaskets) {
        this.resourceBaskets = resourceBaskets;
    }

    @Override
    public void setUploadedFile(BfabricUploadedFile uploadedFile) {
        super.setUploadedFile(uploadedFile);

        if (uploadedFile != null) {
            setRelativePath(getRelativeRepositoryPath() + File.separator + uploadedFile.getFileName());
            setStorage(RepositoryHelper.getLocalStorage(false));
        }
    }

    public void setWorkunit(Workunit workunit) {
        this.workunit = workunit;
    }
}