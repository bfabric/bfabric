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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Container;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.User;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.CommentList;
import org.bfabric.service.CommentService;
import org.bfabric.service.MailSendService;
import org.bfabric.util.AJAX;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datascroller.DataScroller;

@MeasureCalls
@Named
@ViewScoped
public class CommentHelper extends AbstractManager {

    private static final long serialVersionUID = 1;

    private final Map<String, List<Comment>> dataScrollerCommentsMap = new HashMap<>();

    private final Map<String, List<Comment>> dataScrollerCommentsPinnedMap = new HashMap<>();

    @Inject
    protected MailSendService mailSendService;

    @Inject
    private CommentList commentList;

    @Inject
    private CommentService commentService;

    private boolean orderCommentsShown;

    private boolean pinnedCommentsShown;

    public List<Comment> getComments(AbstractBaseEntity parent) {
        if (parent == null) {
            return null;
        }
        parent.resetCommentCaches();
        return parent instanceof Project && orderCommentsShown ? ((Project) parent).getProjectAndOrderComments() : parent.getCommentsCurrentUser();
    }

    private List<Comment> getCommentsFromDataScroller(DataScroller dataScroller, boolean isDataScrollerCommentPinned) {
        if (dataScroller != null && dataScroller.getValue() != null) {
            if (isDataScrollerCommentPinned) {
                if (!isOrderCommentsShown()) {
                    if (!getDataScrollerCommentsPinnedMap().containsKey(Constants.PROJECT)) {
                        getDataScrollerCommentsPinnedMap().put(Constants.PROJECT, (List<Comment>) dataScroller.getValue());
                    }
                    return getDataScrollerCommentsPinnedMap().get(Constants.PROJECT);
                }
                if (!getDataScrollerCommentsPinnedMap().containsKey(Constants.ORDER)) {
                    getDataScrollerCommentsPinnedMap().put(Constants.ORDER, (List<Comment>) dataScroller.getValue());
                }
                return getDataScrollerCommentsPinnedMap().get(Constants.ORDER);
            }
            if (!isOrderCommentsShown()) {
                if (!getDataScrollerCommentsMap().containsKey(Constants.PROJECT)) {
                    getDataScrollerCommentsMap().put(Constants.PROJECT, (List<Comment>) dataScroller.getValue());
                }
                return getDataScrollerCommentsMap().get(Constants.PROJECT);
            }
            if (!getDataScrollerCommentsMap().containsKey(Constants.ORDER)) {
                getDataScrollerCommentsMap().put(Constants.ORDER, (List<Comment>) dataScroller.getValue());
            }
            return getDataScrollerCommentsMap().get(Constants.ORDER);
        }
        return null;
    }

    public List<Comment> getCommentsPinned(AbstractBaseEntity parent) {
        return pinnedCommentsShown && parent != null ? parent.getCommentsPinned(hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER), orderCommentsShown) : null;
    }

    public long getCountPinnedByParentAndType(AbstractEntity parent, CommentDiscriminator commentType) {
        return commentService.getCountPinnedByParentAndType(parent, commentType, isOrderCommentsShown(), hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER));
    }

    public Map<String, List<Comment>> getDataScrollerCommentsMap() {
        return dataScrollerCommentsMap;
    }

    public Map<String, List<Comment>> getDataScrollerCommentsPinnedMap() {
        return dataScrollerCommentsPinnedMap;
    }

    public long getOrderCommentCountByProject(Project project) {
        return getOrderCommentCountByProject(project, false);
    }

    private long getOrderCommentCountByProject(Project project, boolean pinnedOnly) {
        List<Long> orderIds = new ArrayList<>();
        for (final Order order : project.getOrders()) {
            orderIds.add(order.getId());
        }
        return commentList.getCountByParentIdsAndTypeAndPinned(orderIds, CommentDiscriminator.ORDER_COMMENT, pinnedOnly);
    }

    public String getRedirectURL(Comment comment) {
        return comment != null && comment.getDiscriminator() != null && comment.getParent() != null ? createRedirectURL(comment.getParent().getShowScreenPathPrefix(), comment.getParent()
            .getId(), comment.getCategory().toLowerCase() + Constants.PLURAL_S, null) : null;
    }

    @CachedMethodResult
    public boolean isOrderCommentsShowButtonRendered(AbstractEntity parent, CommentDiscriminator commentType) {
        return isParentProjectAndOrderEnabled(parent, commentType) && getOrderCommentCountByProject((Project) parent) > 0;
    }

    public boolean isOrderCommentsShown() {
        return orderCommentsShown;
    }

    @CachedMethodResult
    public boolean isParentContainer(AbstractEntity parent) {
        return parent instanceof Container;
    }

    @CachedMethodResult
    public boolean isParentContainerTrackable(AbstractEntity parent) {
        return isParentContainer(parent) && hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER);
    }

    @CachedMethodResult
    public boolean isParentProjectAndOrderEnabled(AbstractEntity parent, CommentDiscriminator commentType) {
        return CommentDiscriminator.PROJECT_COMMENT.equals(commentType) && getConfiguration().isOrderEnabled() && parent instanceof Project && parent.getId() > 0;
    }

    @CachedMethodResult
    public boolean isPinnedCommentsShowButtonRendered(AbstractEntity parent, CommentDiscriminator commentType, boolean isOrderCommentsShown) {
        if (isParentProjectAndOrderEnabled(parent, commentType)) {
            Project project = (Project) parent;
            if (isOrderCommentsShown) {
                if (isPinnedCommentsShowButtonRendered(project, commentType)) {
                    return true;
                }
                return project.getOrders().stream().anyMatch(order -> isPinnedCommentsShowButtonRendered(order, CommentDiscriminator.ORDER_COMMENT));
            } else {
                return isPinnedCommentsShowButtonRendered(project, commentType, hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER));
            }
        }
        return false;
    }

    public boolean isPinnedCommentsShowButtonRendered(AbstractEntity parent, CommentDiscriminator commentType) {
        return getCountPinnedByParentAndType(parent, commentType) > 0;
    }

    public boolean isPinnedCommentsShown() {
        return pinnedCommentsShown;
    }

    @CachedMethodResult
    public boolean isPlateOrRunComment(CommentDiscriminator type) {
        return CommentDiscriminator.PLATE_COMMENT.equals(type) || CommentDiscriminator.RUN_COMMENT.equals(type);
    }

    public boolean isSwitchPinnedCommentsButtonRendered(AbstractEntity parent, CommentDiscriminator commentType) {
        return hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE) && isPinnedCommentsShowButtonRendered(parent, commentType);
    }

    public void loadListener() {
        Map<String, String> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Set<Integer> indices = new HashSet<>();
        Set<Integer> indicesPinned = new HashSet<>();
        if (requestMap.containsKey("viewedCommentIndices") && !requestMap.get("viewedCommentIndices").isEmpty()) {
            try {
                indices = new HashSet<>(Arrays.asList(requestMap.get("viewedCommentIndices").split(","))).stream().map(Integer::parseInt).collect(Collectors.toSet());
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        if (requestMap.containsKey("viewedCommentIndicesPinned") && !requestMap.get("viewedCommentIndicesPinned").isEmpty()) {
            try {
                indicesPinned = new HashSet<>(Arrays.asList(requestMap.get("viewedCommentIndicesPinned").split(","))).stream().map(Integer::parseInt).collect(Collectors.toSet());
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }

        if (!indices.isEmpty()) {
            String dataScrollerId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("dataScrollerId"));
            if (dataScrollerId != null) {
                DataScroller dataScroller = (DataScroller) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataScrollerId);
                if (dataScroller != null) {
                    List<Comment> comments = getCommentsFromDataScroller(dataScroller, false);
                    if (comments != null) {
                        updateCommentsViewedBy(indices, comments, dataScrollerId);
                    }
                }
            }
        }

        if (!indicesPinned.isEmpty()) {
            String dataScrollerPinnedId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("dataScrollerPinnedId"));
            if (dataScrollerPinnedId != null) {
                DataScroller dataScroller = (DataScroller) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataScrollerPinnedId);
                if (dataScroller != null) {
                    List<Comment> comments = getCommentsFromDataScroller(dataScroller, true);
                    if (comments != null) {
                        updateCommentsViewedBy(indicesPinned, comments, dataScrollerPinnedId);
                    }
                }
            }
        }
    }

    public String remove(Comment comment) {
        final String entityName = comment.toString();
        final String ret = getRedirectURL(comment);
        Comment fetchedComment = entityService.find(Comment.class, comment.getId());
        entityService.remove(fetchedComment);
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted") + " " + entityName);
        return ret;
    }

    public void setOrderCommentsShown(boolean orderCommentsShown) {
        this.orderCommentsShown = orderCommentsShown;
    }

    public void setPinnedCommentsShown(boolean pinnedCommentsShown) {
        this.pinnedCommentsShown = pinnedCommentsShown;
    }

    public void switchAcknowledgedBy(Comment currentComment, User user) {
        switchFlag(currentComment, user, Constants.ACKNOWLEDGEDBY);
    }

    public void switchFlag(Comment currentComment, User user, String flag) {
        if (currentComment != null && (flag.equals(Constants.VIEWEDBY) || flag.equals(Constants.STARREDBY) || flag.equals(Constants.ACKNOWLEDGEDBY))) {
            Comment currentCommentFetched = entityService.find(Comment.class, currentComment.getId());
            Mail mail = null;
            if (currentCommentFetched != null) {
                if (flag.equals(Constants.VIEWEDBY)) {
                    currentComment.switchViewedBy(user);
                    currentCommentFetched.switchViewedBy(user);
                }
                if (flag.equals(Constants.STARREDBY)) {
                    currentComment.switchStarredBy(user);
                    currentCommentFetched.switchStarredBy(user);
                }
                if (flag.equals(Constants.ACKNOWLEDGEDBY)) {
                    User commentCreator = currentComment.getCreatedByUser();
                    if (currentComment.getAcknowledgedBy().isEmpty() && !commentCreator.hasRoleImplicit(RoleEnum.COMMENTREADER)) {
                        mail = new Mail();
                        mail.addRecipient(commentCreator);
                        mail.setParent(currentComment.getParent());
                        mail.setType(MailTypeEnum.COMMENT_ACKNOWLEDGED);
                        mail.setInput("configuration", getConfiguration());
                        mail.setInput("comment", currentComment);
                        mail.setInput("parent", currentComment.getParent());
                    }
                    currentComment.switchAcknowledgedBy(user);
                    currentCommentFetched.switchAcknowledgedBy(user);
                }
                currentCommentFetched.setSetModifiedEnabled(false);
                entityService.save(currentCommentFetched);
                if (mail != null) {
                    mailSendService.send(mail);
                }
            }
        }
    }

    public void switchOrderCommentsShown() {
        setOrderCommentsShown(!isOrderCommentsShown());
        PrimeFaces.current().executeScript("detectViewPort();");
    }

    public void switchPinned(Comment currentComment) {
        if (currentComment != null) {
            Comment currentCommentFetched = entityService.find(Comment.class, currentComment.getId());
            boolean isPinnedNew = !currentComment.isPinned();
            currentComment.setPinned(isPinnedNew);
            currentCommentFetched.setPinned(isPinnedNew);
            entityService.save(currentCommentFetched);

            String dataScrollerPinnedId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("dataScrollerPinnedId"));
            if (dataScrollerPinnedId != null) {
                DataScroller dataScrollerPinned = (DataScroller) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataScrollerPinnedId);
                if (dataScrollerPinned != null && dataScrollerPinned.getValue() != null) {
                    List<Comment> commentsPinned = getCommentsFromDataScroller(dataScrollerPinned, true);

                    if (isPinnedNew) {
                        // The commentsPinned in the dataScrollerPinned are ordered by created descending ("ORDER BY entity.created DESC")
                        int i = 0;
                        while (i < commentsPinned.size()) {
                            if (currentCommentFetched.getCreated().isAfter(commentsPinned.get(i).getCreated())) {
                                break;
                            }
                            i++;
                        }
                        commentsPinned.add(i, currentCommentFetched);
                    } else {
                        commentsPinned.remove(currentCommentFetched);
                    }

                    String dataScrollerId = String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("dataScrollerId"));
                    if (dataScrollerId != null) {
                        DataScroller dataScroller = (DataScroller) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataScrollerPinnedId);
                        List<Comment> comments = getCommentsFromDataScroller(dataScroller, false);
                        if (comments != null) {
                            int i = 0;
                            while (i < comments.size()) {
                                if (currentCommentFetched.getId() == comments.get(i).getId()) {
                                    comments.get(i).setPinned(isPinnedNew);
                                    PrimeFaces.current().ajax().update(dataScrollerId + ":" + i + ":pinned");
                                    break;
                                }
                                i++;
                            }
                        }
                    }
                    AJAX.update("commentPinnedGroup");
                }
            }
        }
    }

    public void switchPinnedCommentsShown() {
        setPinnedCommentsShown(!isPinnedCommentsShown());
        PrimeFaces.current().executeScript("detectViewPort();");
    }

    public void switchStarredBy(Comment currentComment, User user) {
        switchFlag(currentComment, user, Constants.STARREDBY);
    }

    public void switchViewedBy(Comment currentComment, User user) {
        switchFlag(currentComment, user, Constants.VIEWEDBY);
    }

    private void updateCommentsViewedBy(Set<Integer> indices, List<Comment> comments, String dataScrollerId) {
        Set<Comment> commentsViewedBy = new HashSet<>();
        for (int i : indices) {
            if (i >= 0 && i <= comments.size() - 1) {
                Comment commentViewedBy = comments.get(i);
                if (!commentViewedBy.getViewedBy().contains(getCurrentUser())) {
                    commentsViewedBy.add(commentViewedBy);
                }
            }
        }
        if (!commentsViewedBy.isEmpty()) {
            commentService.setViewedBy(commentsViewedBy, getCurrentUser());
            for (int i : indices) {
                if (i >= 0 && i <= comments.size() - 1) {
                    PrimeFaces.current().ajax().update(dataScrollerId + ":" + i + ":viewed");
                }
            }
        }
    }
}