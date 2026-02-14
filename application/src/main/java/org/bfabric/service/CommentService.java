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

package org.bfabric.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Attachment;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Container;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.User;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.FileHelper;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class CommentService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    @Inject
    private AttachmentService attachmentService;

    public CommentService() {
        super(Comment.class);
    }

    private StringBuilder createCommentsQueryString(boolean countQuery, boolean all, boolean pinnedOnly, boolean isCommentManager) {
        StringBuilder queryString = new StringBuilder();
        if (countQuery) {
            queryString.append("SELECT count(*) ");
        } else {
            queryString.append("SELECT entity ");
        }
        queryString.append("FROM Comment entity WHERE entity.parentId = :parentId AND entity.discriminator IN (:discriminators) ");
        if (!isCommentManager && !all) {
            queryString.append("AND entity.internal = false ");
        }
        if (pinnedOnly) {
            queryString.append("AND entity.pinned = true ");
        }
        if (!countQuery) {
            queryString.append("ORDER BY entity.created DESC");
        }
        return queryString;
    }

    public List<Comment> getAllOrderCommentsAndNotesAndResultsByOrder(Order order, boolean isCommentManager) {
        return getCommentsQuery(order, Arrays.asList(CommentDiscriminator.ORDER_COMMENT, CommentDiscriminator.ORDER_NOTE, CommentDiscriminator.ORDER_RESULT), false, true, isCommentManager).getResultList();
    }

    public List<Comment> getAllProjectAndOrderCommentsAndNotesAndResultsByProject(Project project, boolean isCommentManager) {
        List<Comment> projectAndOrderCommentsAndNotesAndResults = new ArrayList<Comment>(getCommentsQuery(project, Collections.singletonList(CommentDiscriminator.PROJECT_COMMENT), false, true, isCommentManager)
            .getResultList());
        for (Order order : project.getOrders()) {
            projectAndOrderCommentsAndNotesAndResults.addAll(getAllOrderCommentsAndNotesAndResultsByOrder(order, isCommentManager));
        }
        return projectAndOrderCommentsAndNotesAndResults;
    }

    public List<Comment> getCommentsByParentAndType(AbstractEntity parent, CommentDiscriminator commentType, boolean isCommentManager) {
        return getCommentsQuery(parent, Collections.singletonList(commentType), false, false, isCommentManager).getResultList();
    }

    public List<Comment> getCommentsByParentIds(Collection<Long> parentIds) {
        if (parentIds != null && !parentIds.isEmpty()) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("entity.parentId IN (:parentIds)");
            entityQuery.addParameter("parentIds", parentIds);
            return (List<Comment>) entityQuery.getResultList();
        }
        return new ArrayList<>();
    }

    public List<Comment> getCommentsPinnedByParentAndType(AbstractEntity parent, CommentDiscriminator commentType, boolean isCommentManager) {
        return getCommentsPinnedQuery(parent, Collections.singletonList(commentType), false, false, isCommentManager).getResultList();
    }

    public Query getCommentsPinnedQuery(AbstractEntity parent, List<CommentDiscriminator> discriminators, boolean countQuery, boolean all, boolean isCommentManager) {
        Query query = createQuery(createCommentsQueryString(countQuery, all, true, isCommentManager).toString());
        query.setParameter("parentId", parent.getId());
        query.setParameter("discriminators", discriminators);
        return query;
    }

    public Query getCommentsQuery(AbstractEntity parent, List<CommentDiscriminator> discriminators, boolean countQuery, boolean all, boolean isCommentManager) {
        Query query = createQuery(createCommentsQueryString(countQuery, all, false, isCommentManager).toString());
        query.setParameter("parentId", parent.getId());
        query.setParameter("discriminators", discriminators);
        return query;
    }

    public List<Comment> getContainerCommentsReadableByUserAfterRevokingEmployeeRights(User user) {
        if (user != null) {
            // Get the containers where the user is currently a member of.
            Set<Container> containers = user.getContainers();
            if (!containers.isEmpty()) {
                Set<Long> containerIds = containers.stream().map(Container::getId).collect(Collectors.toSet());
                return (List<Comment>) createNamedQuery("Comment.findContainerCommentsReadableByUserAfterRevokingEmployeeRights").setParameter("containerIds", containerIds)
                    .setParameter("discriminators", Arrays.asList(CommentDiscriminator.PROJECT_COMMENT, CommentDiscriminator.ORDER_COMMENT, CommentDiscriminator.ORDER_NOTE, CommentDiscriminator.ORDER_RESULT))
                    .getResultList();
            }
        }
        return new ArrayList<>();
    }

    public long getCountByParentAndType(AbstractBaseEntity parent, CommentDiscriminator commentType, boolean isCommentManager) {
        return (long) getCommentsQuery(parent, Collections.singletonList(commentType), true, false, isCommentManager).getSingleResult();
    }

    public long getCountByParentIdsAndType(List<Long> parentIds, CommentDiscriminator commentType) {
        return getCountByParentIdsAndType(parentIds, commentType, false);
    }

    private long getCountByParentIdsAndType(List<Long> parentIds, CommentDiscriminator commentType, boolean pinnedOnly) {
        if (pinnedOnly) {
            return (long) createNamedQuery("Comment.countPinnedByParentIdsAndType").setParameter("parentIds", parentIds).setParameter("discriminator", commentType).getSingleResult();
        }
        return (long) createNamedQuery("Comment.countByParentIdsAndType").setParameter("parentIds", parentIds).setParameter("discriminator", commentType).getSingleResult();
    }

    public long getCountPinnedByParentAndType(AbstractEntity parent, CommentDiscriminator commentType, boolean isOrderCommentsShown, boolean isCommentManager) {
        long countPinned = (long) getCommentsPinnedQuery(parent, Collections.singletonList(commentType), true, false, isCommentManager).getSingleResult();
        if (CommentDiscriminator.PROJECT_COMMENT.equals(commentType) && parent instanceof Project && !((Project) parent).getOrders().isEmpty() && isOrderCommentsShown) {
            countPinned += getCountPinnedByParentIdsAndType(((Project) parent).getOrders().stream().map(Order::getId).collect(Collectors.toList()), CommentDiscriminator.ORDER_COMMENT);
        }
        return countPinned;
    }

    public long getCountPinnedByParentIdsAndType(List<Long> parentIds, CommentDiscriminator commentType) {
        return getCountByParentIdsAndType(parentIds, commentType, true);
    }

    public List<Comment> getLastCommentsByParentAndType(AbstractEntity parent, CommentDiscriminator commentType, Integer maxResult, boolean isCommentManager) {
        return getCommentsQuery(parent, Collections.singletonList(commentType), false, false, isCommentManager).setMaxResults(maxResult != null ? maxResult : 10).getResultList();
    }

    public List<Comment> getLastCommentsByUser(User user, Integer maxResult) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.createdBy = :createdBy");
        entityQuery.addParameter("createdBy", user.getLogin());
        entityQuery.setOrder("id desc");
        entityQuery.setMaxResult(maxResult != null ? maxResult : 10);
        return (List<Comment>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<Comment> getLazyModelAssociatedCommentByUser(User user) {
        EntityQuery entityQuery = createEntityQuery();
        String query = "createdBy = :createdBy";

        if (user.hasRoleImplicit(RoleEnum.PURCHASEMANAGER)) {
            query += " or discriminator = 'PURCHASE_NOTE'";
        }
        if (user.hasRoleImplicit(RoleEnum.CONTRACTMANAGER)) {
            query += " or discriminator = 'CONTRACT_NOTE'";
        }
        if (user.hasRoleImplicit(RoleEnum.CONTAINERMANAGER)) {
            query += " or discriminator = 'CONSUMABLE_NOTE'";
        }
        if (user.hasRoleImplicit(RoleEnum.SERVICEMANAGER)) {
            query += " or discriminator = 'SERVICE_NOTE'";
        }
        if (user.hasRoleImplicit(RoleEnum.SERVICEMANAGER)) {
            query += " or discriminator = 'SERVICEAREA_NOTE'";
        }
        if (user.hasRoleImplicit(RoleEnum.SERVICEMANAGER)) {
            query += " or discriminator = 'SERVICETYPE_NOTE'";
        }
        if (!user.getRuns().isEmpty()) {
            query += " or discriminator = 'RUN_COMMENT' and parentid in ( " + CollectionHelper.printIds(user.getRuns()) + " )";
        }
        if (!user.getPlates().isEmpty()) {
            query += " or (discriminator = 'PLATE_COMMENT' and parentid in ( " + CollectionHelper.printIds(user.getPlates()) + " ))";
        }
        if (!user.getSupervisedInstruments().isEmpty()) {
            query += " or discriminator in ('INSTRUMENT_NOTE', 'INSTRUMENT_EVENT_NOTE') and parentid in ( " + CollectionHelper.printIds(user.getSupervisedInstruments()) + " )";
        }
        if (!user.getBookedInstrumentReservations().isEmpty()) {
            query += " or discriminator = 'INSTRUMENT_RESERVATION_NOTE' and parentid in ( " + CollectionHelper.printIds(user.getBookedInstrumentReservations()) + " )";
        }
        if (!user.getWorkflowSteps().isEmpty()) {
            query += " or discriminator = 'WORKFLOW_STEP_COMMENT' and parentid in ( " + CollectionHelper.printIds(user.getWorkflowSteps()) + " )";
        }
        if (user.isEmployee()) {
            query += " or discriminator IN ('ORDER_COMMENT', 'ORDER_RESULT', 'ORDER_NOTE') and parentid in ( " + CollectionHelper.printIds(user.getAssociatedOrders()) + " )";
            query += " or discriminator IN ('PROJECT_COMMENT') and parentid in ( " + CollectionHelper.printIds(user.getAssociatedProjects()) + " )";
        } else {
            query += " or internal != true";
            query += " and discriminator IN ('ORDER_COMMENT', 'ORDER_RESULT', 'ORDER_NOTE') and parentid in ( " + CollectionHelper.printIds(user.getOrdersTransitive()) + " )";
            query += " and discriminator IN ('PROJECT_COMMENT') and parentid in ( " + CollectionHelper.printIds(user.getProjects()) + " )";
        }

        // Note: Not sure anymore whether we should include the sample and workunit comments since this put heavy computation into the game! In this case, the methods getSamples() and getWorkunits() have to be implemented first!
        // query += " or discriminator IN ('SAMPLE_COMMENT') and parentid in ( " + CollectionHelper.printIds(user.getSamples()) + " )";
        // query += " or discriminator IN ('WORKUNIT_COMMENT') and parentid in ( " + CollectionHelper.printIds(user.getWorkunits()) + " )";

        entityQuery.addWhereClause(query);
        entityQuery.addParameter("createdBy", user.getLogin());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Comment> getLazyModelByCreatedByUser(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("createdBy = :createdBy");
        entityQuery.addParameter("createdBy", user.getLogin());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Comment> getLazyModelByReplyToId(long replyToId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("replyTo.id = :replyToId");
        entityQuery.addParameter("replyToId", replyToId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Comment> getLazyModelByUserIdStarred(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.starredBy starredByUser");
        entityQuery.addWhereClause("starredByUser.id = :userId");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Comment> getLazyModelByUserIdViewed(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.viewedBy viewedByUser");
        entityQuery.addWhereClause("viewedByUser.id = :userId");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Comment> getProjectAndOrderCommentsByProject(Project project, boolean isCommentManager) {
        List<Comment> projectAndOrderComments = new ArrayList<>(getCommentsByParentAndType(project, CommentDiscriminator.PROJECT_COMMENT, isCommentManager));
        for (Order order : project.getOrders()) {
            projectAndOrderComments.addAll(getCommentsByParentAndType(order, CommentDiscriminator.ORDER_COMMENT, isCommentManager));
        }
        projectAndOrderComments.sort(Comparator.comparing(AbstractBaseEntity::getCreated).reversed());
        return projectAndOrderComments;
    }

    public List<Comment> getProjectAndOrderCommentsPinnedByProject(Project project, boolean isCommentManager) {
        List<Comment> projectAndOrderCommentsPinned = new ArrayList<>(getCommentsPinnedByParentAndType(project, CommentDiscriminator.PROJECT_COMMENT, isCommentManager));
        for (Order order : project.getOrders()) {
            projectAndOrderCommentsPinned.addAll(getCommentsPinnedByParentAndType(order, CommentDiscriminator.ORDER_COMMENT, isCommentManager));
        }
        projectAndOrderCommentsPinned.sort(Comparator.comparing(AbstractBaseEntity::getCreated).reversed());
        return projectAndOrderCommentsPinned;
    }

    public List<String> getSubjectsFiltered(String filterString, Long parentId) {
        Query query = createQuery("SELECT DISTINCT(subject) FROM Comment entity WHERE parentId = :parentId AND LOWER(subject) LIKE :filterString ORDER BY subject ASC");
        query.setParameter("parentId", parentId);
        addFilterString(query, filterString);
        query.setMaxResults(25);
        return (List<String>) query.getResultList();
    }

    public boolean isLastComment(Comment comment) {
        if (comment != null) {
            Long lastId = (Long) createNamedQuery("Comment.findLastCommentIdByParent").setParameter("parentId", comment.getParentId()).setParameter("discriminator", comment.getDiscriminator())
                .getSingleResult();
            return lastId != null && lastId == comment.getId();
        }
        return false;
    }

    public LinkedHashMap<String, String> isValid(Comment comment, Set<BfabricUploadedFile> uploadedFiles) throws IOException {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        if (StringHelper.isEmpty(comment.getComment()) && uploadedFiles.isEmpty() && comment.getUncheckedAttachments().isEmpty()) {
            validationErrorMsg.put(null, Messages.get("commentContainError"));
        }

        for (BfabricUploadedFile uploadedFile : uploadedFiles) {
            String errorMsg = FileHelper.isValid(uploadedFile, null, new HashSet<>(comment.getUncheckedAttachments()));
            if (errorMsg != null) {
                validationErrorMsg.put(null, errorMsg);
            }
        }

        return validationErrorMsg;
    }

    public Set<String> save(Comment comment, Set<BfabricUploadedFile> bfabricUploadedFiles, boolean index) throws RollbackException {
        try {
            if (comment.isCategoryResult() && comment.isInternal()) {
                comment.setInternal(false);
            } else if (comment.isCategoryNote() && !comment.isInternal()) {
                comment.setInternal(true);
            }
            super.save(comment, index);

            if (comment.isInternalChanged()) {
                comment.moveAttachments();
                for (Attachment attachment : comment.getAttachments()) {
                    merge(attachment);
                }
            }

            for (Attachment attachment : comment.getCheckedAttachments()) {
                comment.removeAttachment(attachment);
                remove(attachment);
            }

            // Important: Flush required to enforce Hibernate to adhere to the desired execution order (else Hibernate determines the execution order on its own).
            flush();

            if (bfabricUploadedFiles != null) {
                for (BfabricUploadedFile uploadedFile : bfabricUploadedFiles) {
                    comment.addAttachment(uploadedFile);
                }
            }

            for (Attachment attachment : comment.getNewAttachments()) {
                attachmentService.save(attachment);
            }

            Mail mail = comment.createMail();
            if (mail != null && !mail.getRecipients().isEmpty()) {
                mailSendService.send(mail);
            }
            return new HashSet<>();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RollbackException(e.getMessage());
        }
    }

    public void setViewedBy(Collection<Comment> comments, User user) {
        if (comments != null && user != null) {
            for (Comment comment : comments) {
                comment.getViewedBy().add(user);
                Comment fetchedComment = find(Comment.class, comment.getId());
                fetchedComment.getViewedBy().add(user);
                fetchedComment.setSetModifiedEnabled(false);
                save(fetchedComment);
            }
        }
    }
}