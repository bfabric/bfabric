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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ContainerDependent;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.indexer.IndexMap;
import org.bfabric.indexer.IndexMapContent;

@MappedSuperclass
public abstract class AbstractContainerDependentEntity extends AbstractParentDependentDescriptionNamedBaseEntity implements ContainerDependent, ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "containerid")
    @XmlIDREF
    private Container container;

    @Transient
    private AbstractContainerDependentEntity nextInContainer;

    @Transient
    private long oldContainerId;

    @Transient
    private AbstractContainerDependentEntity prevInContainer;

    public AbstractContainerDependentEntity() {
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getContainer() != null) {
            if (getContainer().isContainerProject()) {
                addEntityInfoItem(summary, "project", getContainer().getId());
            } else {
                addEntityInfoItem(summary, "order", getContainer().getId());
            }
        }
        return summary.toString();
    }

    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.PROJECTID.getField());
        fields.add(IndexMapContentEnum.ORDERID.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        return fields;
    }

    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = super.getIndexMap();
        if (getContainer() != null) {
            indexMap.put(Constants.INDEXMAP_STATUS, getContainer().getStatus().getLabel());
            indexMap.put(Constants.INDEXMAP_GROUP, getContainer().getMemberRoleName());
            indexMap.put(Constants.INDEXMAP_DOI_CREATED, getContainer().getDoiCreated());
        }
        return indexMap;
    }

    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = new IndexMapContent();
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

        if (!(this instanceof Sample)) {
            content.add(IndexMapContentEnum.DESCRIPTION, getDescription());
        }
        return content;
    }

    @Override
    public String getMetadataRepositoryPath() {
        return getContainer().getMetadataRepositoryPath();
    }

    public AbstractContainerDependentEntity getNextInContainer() {
        if (nextInContainer == null) {
            nextInContainer = getEntityService().getNextInContainer(getTrimmedClassName(), getId(), getContainer().getId());
        }
        return nextInContainer;
    }

    public String getNextInContainerHint() {
        return getNextInContainer() != null ? Messages.get("nextInContainerHint").replace("{0}", getClassLabelLowerCase()).replace("{1}", getNextInContainer().getDisplayName()) : null;
    }

    public long getOldContainerId() {
        return oldContainerId;
    }

    public AbstractContainerDependentEntity getPrevInContainer() {
        if (prevInContainer == null) {
            prevInContainer = getEntityService().getPrevInContainer(getTrimmedClassName(), getId(), getContainer().getId());
        }
        return prevInContainer;
    }

    public String getPrevInContainerHint() {
        return getPrevInContainer() != null ? Messages.get("prevInContainerHint").replace("{0}", getClassLabelLowerCase()).replace("{1}", getPrevInContainer().getDisplayName()) : null;
    }

    @Override
    public String getRelativeRepositoryPath() {
        return getContainer().getRelativeRepositoryPath() + File.separator + super.getRelativeRepositoryPath();
    }

    @Override
    public String getShowScreenLink() {
        return getShowScreenLink(this);
    }

    @Override
    public boolean isCloneable() {
        return getContainer() != null && getContainer().isExtensible();
    }

    public boolean isContainerChanged() {
        return getContainer() != null && getContainer().getId() != getOldContainerId();
    }

    @Override
    public boolean isMoved() {
        return getOldContainerId() != 0 && (getContainer() == null || getOldContainerId() != getContainer().getId());
    }

    @Override
    public boolean isRenderedAddCommentButton() {
        return getContainer() != null && getContainer().isExtensible();
    }

    @Override
    public void setContainer(Container container) {
        if (getId() != 0 && getOldContainerId() == 0 && getContainer() != null) {
            setOldContainerId(getContainer().getId());
        }
        this.container = container;
        setParent(container);
    }

    public void setOldContainerId(long oldContainerId) {
        this.oldContainerId = oldContainerId;
    }
}
