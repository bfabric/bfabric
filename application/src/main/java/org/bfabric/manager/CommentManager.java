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

package org.bfabric.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Comment;
import org.bfabric.entity.CommentTemplate;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.CommentService;
import org.bfabric.service.CommentTemplateService;
import org.bfabric.service.UserService;
import org.bfabric.service.WorkunitService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.FileUploadHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class CommentManager extends AbstractEntityManager<Comment> {

    private static final long serialVersionUID = 1;

    @Inject
    private CommentHelper commentHelper;

    @Inject
    private CommentService commentService;

    @Inject
    private CommentTemplateService commentTemplateService;

    @Inject
    private FileUploadHelper fileUploadHelper;

    @Param
    private Boolean internal;

    @Param
    private Long parentId;

    @Param
    private Long replyToId;

    @Param
    private String type;

    @Inject
    private UserService userService;

    @Inject
    private WorkunitService workunitService;

    public CommentManager() {
        super(Comment.class);
    }

    @Override
    protected Comment createInstance() {
        final Comment comment = super.createInstance();
        if (comment != null && getParentId() != null) {
            comment.setParent(entityService.find((Class<? extends AbstractEntity>) ClassHelper.getClassByName(getType().substring(0, getType().lastIndexOf("_"))
                .replace("_", Constants.EMPTY_STRING)), getParentId()));
            if (getInternal() != null && getInternal() && comment.hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER)) {
                comment.setInternal(getInternal());
            }
        }
        return comment;
    }

    @Produces
    @Named("comment")
    public Comment getComment() {
        return getInstance();
    }

    public List<User> getEmployeesOrUsersFiltered(String filterString) {
        return userService.getEmployeesOrUsersFiltered(filterString, getComment().getMailRecipientHelper().getUsers(), null);
    }

    public List<CommentTemplate> getEnabledCommentTemplates(String filterString) {
        return commentTemplateService.getFilteredEnabledIncludingOrderBy(filterString, getComment().getCommentTemplate());
    }

    @Override
    public Class<Comment> getEntityClass() {
        if (getType() != null && (getIdLong() == null || getIdLong() <= 0)) {
            try {
                return (Class<Comment>) CommentDiscriminator.value(getType()).getCommentClass();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.getEntityClass();
    }

    public Boolean getInternal() {
        return internal;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public String getRedirectURLAfterCancelCreated() {
        return commentHelper.getRedirectURL(getComment());
    }

    @Override
    public String getRedirectURLAfterCancelManaged() {
        return commentHelper.getRedirectURL(getComment());
    }

    @Override
    public String getRedirectURLAfterSave() {
        return commentHelper.getRedirectURL(getComment());
    }

    public List<String> getSubjectFiltered(String filterString) {
        if (getComment().getParent() != null) {
            return commentService.getSubjectsFiltered(filterString, getComment().getParent().getId());
        }
        return new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public List<Workunit> getWorkunits(String filterString) {
        return getComment().isContainerComment() ? workunitService.getWorkunitsFilteredByContainerIdExcluding(filterString, getComment().getParent().getId(), getComment().getWorkunits()) : null;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getComment() != null && isManaged()) {
            fileUploadHelper.setInitialAttachments(new HashSet<>(getComment().getAttachments()));
            viewedByUpdate(getComment());
        }
        if (replyToId != null) {
            Comment replyTo = entityService.find(Comment.class, replyToId);
            if (replyTo != null) {
                getComment().setInternal(replyTo.isInternal());
                getComment().setSubject(replyTo.getSubject());
                getComment().setReplyTo(replyTo);
                viewedByUpdate(replyTo);
            }
        }
        if (getComment() != null) {
            getComment().getMailRecipientHelper().init();
        }
    }

    @Override
    public String save() {
        try {
            LinkedHashMap<String, String> validationErrorMsg = commentService.isValid(getComment(), fileUploadHelper.getUploadedFiles());
            if (validationErrorMsg.isEmpty()) {
                setCreated(!isManaged());
                getFacesMessagesManager().bufferErrors(commentService.save(getComment(), fileUploadHelper.getUploadedFiles(), true));
                if (isCreated()) {
                    viewedByUpdate(getComment());
                }
                return postSave(true, false);
            }
            handleValidationErrors(validationErrorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            getFacesMessagesManager().printError(e.getMessage());
        }
        return null;
    }

    public void viewedByUpdate(Comment comment) {
        if (!comment.getViewedBy().contains(getCurrentUser())) {
            comment.getViewedBy().add(getCurrentUser());
            comment.setSetModifiedEnabled(false);
            entityService.save(comment);
        }
        getCurrentUser().getCommentsViewedBy().add(comment);
    }
}