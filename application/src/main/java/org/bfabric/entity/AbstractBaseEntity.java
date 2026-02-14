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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.indexer.IndexMap;
import org.bfabric.util.DateUtils;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractBaseEntity extends AbstractEntity {

    protected static final Logger logger = Logger.getLogger(AbstractBaseEntity.class.getName());

    private static final long serialVersionUID = 1;

    @Version
    @NotNull
    protected int opLockVersion;

    @Transient
    protected String entityCreationInfo;

    @Transient
    User createdByUser;

    @Transient
    User modifiedByUser;

    @NotNull
    private LocalDateTime created;

    @NotBlank
    @Size(max = 32)
    private String createdBy;

    @NotNull
    private LocalDateTime modified;

    @NotBlank
    @Size(max = 32)
    private String modifiedBy;

    public AbstractBaseEntity() {
    }

    public AbstractBaseEntity(long id) {
        setId(id);
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByFull() {
        return getUserDate(getCreatedByUser(), getCreatedBy(), getCreated(), true);
    }

    public String getCreatedByFullDateFirst() {
        return getUserDate(getCreatedByUser(), getCreatedBy(), getCreated(), false);
    }

    public User getCreatedByUser() {
        if (createdByUser == null) {
            createdByUser = getUserByLogin(getCreatedBy());
        }
        return createdByUser;
    }

    public LocalDate getCreatedDate() {
        return created != null ? created.toLocalDate() : null;
    }

    public String getCreatedFormattedAsDateString() {
        return DateUtils.getDateAsFormattedStringWithoutTime(getCreated());
    }

    public String getEntityCreationInfo() {
        if (entityCreationInfo == null) {
            entityCreationInfo = "\n" + Messages.get("createdBy") + " = " + getUserDate(null, getCreatedBy(), getCreated(), true) + "\n" + Messages.get("modifiedBy") + " = " + getUserDate(null, getModifiedBy(), getModified(), true);
        }
        return entityCreationInfo;
    }

    @Override
    public String getEntityInfo() {
        if (entityInfo == null) {
            entityInfo = getEntityInfo(true);
        }
        return entityInfo;
    }

    public String getEntityInfo(boolean includeCreationInfo) {
        StringBuilder summary = new StringBuilder();
        summary.append(getEntitySpecifics());
        if (includeCreationInfo) {
            summary.append(getEntityCreationInfo());
        }
        return summary.toString();
    }

    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = super.getIndexMap();
        indexMap.put(Constants.INDEXMAP_CREATED, getCreated());
        indexMap.put(Constants.INDEXMAP_CREATEDBY, getCreatedBy());
        indexMap.put(Constants.INDEXMAP_MODIFIED, getModified());
        indexMap.put(Constants.INDEXMAP_MODIFIEDBY, getModifiedBy());
        return indexMap;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public String getModifiedBy() {
        String ret = modifiedBy;
        if (ret != null && ret.equalsIgnoreCase(Constants.SYSTEM)) {
            ret = getCreatedBy();
        }
        return ret;
    }

    public String getModifiedByFull() {
        return getUserDate(getModifiedByUser(), getModifiedBy(), getModified(), true);
    }

    public String getModifiedByFullDateFirst() {
        return getUserDate(getModifiedByUser(), getModifiedBy(), getModified(), false);
    }

    public User getModifiedByUser() {
        if (modifiedByUser == null) {
            modifiedByUser = getUserByLogin(getModifiedBy());
        }
        return modifiedByUser;
    }

    public int getOpLockVersion() {
        return opLockVersion;
    }

    public boolean isCreatedBeforeBfabric10ReleaseDate() {
        return getCreated() != null && !LocalDateTime.of(2020, 9, 20, 10, 0).isBefore(getCreated());
    }

    public boolean isCreator() {
        return getCreatedBy() != null && getCreatedBy().equals(getCurrentUsername());
    }

    public boolean isCreatorOrUser(User user) {
        return isCreator() || user != null && user.isIdentityUser();
    }

    @Override
    protected void prePersist() {
        // logger.info(this + "---AbstractBaseEntity.prePersist-----------------logEntity=" + isLogEntity());
        super.prePersist();
        setOpLockVersion(1);
        setCreatedToCurrentDateAndUser();
        setModifiedToCurrentDateAndUser();
    }

    @Override
    protected void preUpdate() {
        // logger.info(this + "---AbstractBaseEntity.preUpdate-----------------logEntity=" + isLogEntity());
        super.preUpdate();
        // Important: setModifiedToCurrentDateAndUser must be executed after the EntityLog is created via the preUpdate method of the super class!
        setModifiedToCurrentDateAndUser();
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = StringHelper.isNotEmpty(createdBy) ? createdBy : Constants.SYSTEM;
    }

    public void setCreatedToCurrentDateAndUser() {
        setCreated(LocalDateTime.now());
        setCreatedBy(getCurrentUsername());
    }

    public void setModified(final LocalDateTime modified) {
        this.modified = modified;
    }

    public void setModifiedBy(final String modifiedBy) {
        this.modifiedBy = StringHelper.isNotEmpty(modifiedBy) ? modifiedBy : Constants.SYSTEM;
    }

    public void setModifiedToCurrentDateAndUser() {
        if (isSetModifiedEnabled()) {
            setModified(LocalDateTime.now());
            setModifiedBy(getCurrentUsername());
        }
    }

    public void setOpLockVersion(int opLockVersion) {
        this.opLockVersion = opLockVersion;
    }
}
