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

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.service.EntityService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@XmlRootElement
public class EntityLog extends AbstractLog {

    private static final long serialVersionUID = 1;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private LogActionEnum action;

    @Size(max = 64)
    @NotNull
    @XmlElement
    private String entityClassName;

    @NotNull
    @XmlElement
    private Long entityId;

    @Size(max = 64)
    @XmlElement
    private String parentEntityClassName;

    @XmlElement
    private Long parentEntityId;

    @Transient
    private Boolean renderedEntityShowLink;

    @Transient
    private Boolean renderedParentEntityShowLink;

    public EntityLog() {
    }

    public EntityLog(AbstractEntity entity, LogActionEnum action) {
        this(entity, action, null);
    }

    public EntityLog(AbstractEntity entity, LogActionEnum action, String log) {
        this(entity, action, LogStatusEnum.DONE, Constants.SYSTEM, log, null);
    }

    public EntityLog(AbstractEntity entity, LogActionEnum action, LogStatusEnum status, String createdBy) {
        this(entity, action, status, createdBy, null, null);
    }

    public EntityLog(AbstractEntity entity, LogActionEnum action, LogStatusEnum status, String createdBy, String log) {
        this(entity, action, status, createdBy, log, null);
    }

    public EntityLog(AbstractEntity entity, LogActionEnum action, LogStatusEnum status, String createdBy, String log, AbstractEntity parentEntity) {
        setEntity(entity);
        setLog(log);
        setAction(action);
        setStatus(status);
        setCreatedBy(createdBy);
        setParentEntity(parentEntity);
    }

    @Override
    public EntityLog clone() throws CloneNotSupportedException {
        return (EntityLog) super.clone();
    }

    public LogActionEnum getAction() {
        return action;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public long getEntityId() {
        return entityId;
    }

    @Transient
    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "entityClassName", getEntityClassName());
        addEntityInfoItem(summary, "entityId", getEntityId());
        addEntityInfoItem(summary, "action", getAction());
        return summary.toString();
    }

    public String getParentEntityClassName() {
        return parentEntityClassName;
    }

    public Long getParentEntityId() {
        return parentEntityId;
    }

    public String getUrlEntityShowScreen() {
        if (getEntityClassName() != null) {
            String className = getEntityClassName();
            if (CommentDiscriminator.getCommentType(className) != null) {
                className = "comment";
            } else {
                className = getEntityClassName().toLowerCase();
            }
            return UriHelper.getUrlShowScreen(className);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public String getUrlParentEntityShowScreen() {
        return UriHelper.getUrlShowScreen(StringHelper.isNotEmpty(getParentEntityClassName()) && getParentEntityClassName().endsWith("Comment") ? "Comment" : getParentEntityClassName());
    }

    @SuppressWarnings("unused")
    public boolean isRenderedEntityShowLink() {
        if (renderedEntityShowLink == null) {
            renderedEntityShowLink = !getAction().equals(LogActionEnum.DELETE) && StringHelper.isNotEmpty(getEntityClassName()) && StringHelper.isNotEmpty(String.valueOf(getEntityId())) && ClassHelper
                .isShowScreenAvailable(getEntityClassName()) && CDI.current().select(EntityService.class).get().checkEntityExistence(getEntityClassName(), getEntityId());
        }
        return renderedEntityShowLink;
    }

    @SuppressWarnings("unused")
    public boolean isRenderedParentEntityShowLink() {
        if (renderedParentEntityShowLink == null) {
            renderedParentEntityShowLink = StringHelper.isNotEmpty(getParentEntityClassName()) && StringHelper.isNotEmpty(String.valueOf(getParentEntityId())) && ClassHelper
                .isShowScreenAvailable(getParentEntityClassName()) && CDI.current().select(EntityService.class).get().checkEntityExistence(getParentEntityClassName(), getParentEntityId());
        }
        return renderedParentEntityShowLink;
    }

    public void setAction(LogActionEnum action) {
        this.action = action;
    }

    public void setEntity(AbstractEntity entity) {
        if (entity != null) {
            setEntityClassName(entity.getTrimmedClassName());
            setEntityId(entity.getId());
        } else {
            setEntityClassName(null);
            setEntityId(null);
        }
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public void setParentEntity(AbstractEntity parentEntity) {
        if (parentEntity != null) {
            setParentEntityClassName(parentEntity.getTrimmedClassName());
            setParentEntityId(parentEntity.getId());
        } else {
            setParentEntityClassName(null);
            setParentEntityId(null);
        }
    }

    public void setParentEntityClassName(String parentEntityClassName) {
        this.parentEntityClassName = parentEntityClassName;
    }

    public void setParentEntityId(Long parentEntityId) {
        this.parentEntityId = parentEntityId;
    }
}