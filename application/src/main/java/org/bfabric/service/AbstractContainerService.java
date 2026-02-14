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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.Query;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Container;
import org.bfabric.entity.CustomContainerStatus;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Membership;
import org.bfabric.entity.Order;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.enums.TimelineEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

public class AbstractContainerService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(AbstractContainerService.class.getName());

    @Inject
    protected AffiliationHelperService affiliationHelperService;

    @Inject
    protected MembershipService membershipService;

    @Inject
    protected UserService userService;

    @Inject
    private CommentService commentService;

    public AbstractContainerService() {
        super(Container.class);
    }

    public AbstractContainerService(Class<? extends Container> entityClass) {
        super(entityClass);
    }

    public Map<String, Set<String>> addManager(Container container, User user, User currentUser, Set<Mail> mails) {
        return addMember(user, true, container, currentUser, false, mails);
    }

    public Map<String, Set<String>> addMember(User user, boolean asManager, Container container, User currentUser, boolean logAndSendMail, Set<Mail> mails) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        if (user != null) {
            Membership membership = membershipService.getMembership(container, user);
            if (membership != null && membership.isCurrent()) {
                // User is already member of the container.
                if (asManager && membership.isRoleMember()) {
                    // Upgrade to manager if the user is not already a manager of the container.
                    switchRole(membership, logAndSendMail, mails);
                }
            } else {
                if (membership != null && membership.isFormer()) {
                    // Update membership from former to current.
                    membership.reactivate(asManager, currentUser.getLastNameFirstName());
                    merge(membership);
                } else {
                    // Add a new membership.
                    membership = new Membership(container, user, asManager, currentUser.getLogin());
                    persist(membership);
                }
                if (!container.isPendingOrReview() && !container.isSubmitted()) {
                    // Create notification mails.
                    mails.add(container.createMail(MailTypeEnum.MEMBER_ADD, user));
                    mails.add(container.createMail(MailTypeEnum.MEMBER_ADD_CONTACT, user));
                }
                if (logAndSendMail) {
                    // Create entity log.
                    persist(container.createEntityUpdateLog(asManager ? "manager" : "member", null, user.getIdString()));
                    if (!container.isPendingOrReview() && !container.isSubmitted()) {
                        // Send mails.
                        facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
                    }
                }

                facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("addedUserToContainer").replace("{0}", user.getLogin()).replace("{1}", container.toString()));

                // Note: Do not remove! flush() needed here to enforce order of DB commands!
                flush();

                // Add the user role if the container is accepted and synchronize with the AD.
                userService.addRoleUserAndSynchronizeWithAD(user, container);
            }
        }
        return facesMessages;
    }

    public EntityQuery createEntityQueryExtensibleReadableContainersExcluding(String filterString, Container excluded, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addContainerReadableClause(user);
        if (excluded != null) {
            entityQuery.addNotInEntitiesClause(Collections.singletonList(excluded));
        }
        entityQuery.addWhereClause("status not in :nonExtensibleStatusList");
        entityQuery.addParameter("nonExtensibleStatusList", StatusEnum.NON_EXTENSIBLE_CONTAINER_STATUS_LIST);
        entityQuery.setMaxResult(100);
        return entityQuery;
    }

    public List<Container> getAcceptedReadableContainers(String filterString, Collection<Container> excluded, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addContainerReadableClause(user);
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<Container>) entityQuery.getResultList();
    }

    public Long getAttachmentAndResourcesSizeByContainer(Container container) {
        String query
            = "SELECT COALESCE(SUM(size),0) FROM (SELECT SUM(a.size) as size FROM Attachment a WHERE a.commentid IN (SELECT id FROM Comment c WHERE c.parentId=:containerId AND c.discriminator='"
            + container.getClassLabelUpperCase() + "_COMMENT' AND c.internal=false)"
            + " UNION SELECT SUM(a.size) FROM Attachment a WHERE a.commentid IN (SELECT id FROM Comment c WHERE c.parentId IN (SELECT s.id FROM Sample s WHERE s.containerid=:containerId) AND c.discriminator='SAMPLE_COMMENT' AND c.internal=false)"
            + " UNION SELECT SUM(a.size) FROM Attachment a WHERE a.commentid IN (SELECT id FROM Comment c WHERE c.parentId IN (SELECT w.id FROM Workunit w WHERE w.containerid=:containerId) AND c.discriminator='WORKUNIT_COMMENT' AND c.internal=false)) q";
        return ((BigDecimal) createNativeQuery(query).setParameter("containerId", container.getId()).getSingleResult()).longValue() + getResourcesTotalSizeByContainer(container);
    }

    public List<Container> getBookableContainers(String filterString, Collection<Container> excluded, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addContainerBookableClause(user);
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<Container>) entityQuery.getResultList();
    }

    public List<Container> getChargeAssignableContainers(String filterString, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("status not in :nonBookableStatusList");
        entityQuery.addParameter("nonBookableStatusList", StatusEnum.NON_BOOKABLE_CONTAINER_STATUS_LIST);
        entityQuery.setMaxResult(100);
        return (List<Container>) entityQuery.getResultList();
    }

    public List<Container> getChargeableReadableContainers(String filterString, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addContainerReadableClause(user);
        entityQuery.addWhereClause("status not in :finalSuccessStatusList");
        entityQuery.addParameter("finalSuccessStatusList", StatusEnum.FINAL_SUCCESS_CONTAINER_STATUS_LIST);
        return (List<Container>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<Container> getCoachedContainersLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addCoachClause(userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Long getContainerIdByOldProjectOrderId(long oldProjectOrderId) {
        List<Long> ret = createNamedQuery("Container.findIdByOldProjectOrderId").setParameter("oldProjectOrderId", oldProjectOrderId).setMaxResults(1).getResultList();
        return ret.isEmpty() ? null : ret.get(0);
    }

    public Long getContainerIdByOldServiceOrderId(long oldServiceOrderId) {
        List<Long> ret = createNamedQuery("Container.findIdByOldServiceOrderId").setParameter("oldServiceOrderId", oldServiceOrderId).setMaxResults(1).getResultList();
        return ret.isEmpty() ? null : ret.get(0);
    }

    public List<Container> getContainersByIdsFiltered(String filterString, Collection<Long> ids) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        if (ids != null) {
            entityQuery.addWhereClause("id IN (:ids)");
            entityQuery.addParameter("ids", ids);
        }
        return (List<Container>) entityQuery.getResultList();
    }

    public List<Container> getContainersFiltered(String filterString) {
        return (List<Container>) createEntityQueryFiltered(filterString).getResultList();
    }

    public List<Container> getContainersFilteredExcluding(String filterString, Collection<Container> excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        if (excluded != null) {
            entityQuery.addNotInEntitiesClause(excluded);
        }
        return (List<Container>) entityQuery.getResultList();
    }

    public List<Container> getContainersWithNonEmptyChargesFiltered(String filterString) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("entity IN (SELECT DISTINCT charge.container FROM Charge charge)");
        return (List<Container>) entityQuery.getResultList();
    }

    public List<String> getCurrentCustomContainerStatesFiltered(String filterString) {
        return getCurrentCustomContainerStatesFiltered(filterString, null);
    }

    public List<String> getCurrentCustomContainerStatesFiltered(String filterString, String discriminator) {
        Query query = createQuery("SELECT DISTINCT(entity.customStatus) FROM Container entity WHERE entity.customStatus IS NOT NULL AND LOWER(entity.customStatus) LIKE :filterString" + (discriminator != null ? " AND LOWER(entity.discriminator) = '" + discriminator + "'" : Constants.EMPTY_STRING) + " ORDER BY entity.customStatus ASC");
        addFilterString(query, filterString);
        query.setMaxResults(25);
        return (List<String>) query.getResultList();
    }

    public List<Container> getExtensibleReadableContainers(String filterString, User user) {
        return getExtensibleReadableContainersExcluding(filterString, null, user);
    }

    public List<Container> getExtensibleReadableContainersExcluding(String filterString, Container excluded, User user) {
        return (List<Container>) createEntityQueryExtensibleReadableContainersExcluding(filterString, excluded, user).getResultList();
    }

    public long getImportResourcesTotalSizeByContainer(Container container) {
        return (long) createNamedQuery("ImportResource.findTotalSizeByContainer").setParameter("container", container).setMaxResults(1).getSingleResult();
    }

    public BfabricLazyDataModel<Container> getLazyDataModelByDiscussedWith(Long userId) {
        return (BfabricLazyDataModel<Container>) getLazyModelUnnestById("discussedWith", userId);
    }

    public List<Container> getOfferAssignableContainersIncludingAndExcluding(String filterString, Collection<Container> included, Collection<Container> excluded) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addInEntitiesClause(included);
        entityQuery.addNotInEntitiesClause(excluded, "OR");
        entityQuery
            .addWhereClause("(entity.discriminator = 'Project' AND entity.status in ('RUNNING', 'FINISHED')) OR (entity.discriminator = 'Order' AND entity.status not in ('PENDING', 'CANCELED', 'FINISHED', 'CLOSED', 'INVALID'))", excluded == null || excluded
                .isEmpty() ? "OR" : "AND");
        entityQuery.setParenthesisAroundWhere();
        entityQuery.addIdOrNameWhereClause(filterString);
        return (List<Container>) entityQuery.getResultList();
    }

    public List<Container> getReadableContainers(String filterString, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addContainerReadableClause(user);
        return (List<Container>) entityQuery.getResultList();
    }

    public List<Container> getReassignableContainersByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.status not in (org.bfabric.enums.StatusEnum.CANCELED, org.bfabric.enums.StatusEnum.REJECTED, org.bfabric.enums.StatusEnum.PUBLISHED, org.bfabric.enums.StatusEnum.PRIVATE, org.bfabric.enums.StatusEnum.CLOSED) and (entity.coach.id = :userId or entity.coachBackup.id = :userId or entity.bioinformatician.id = :userId)");
        entityQuery.addParameter("userId", userId);
        return (List<Container>) entityQuery.getResultList();
    }

    public long getResourcesTotalSizeByContainer(Container container) {
        return (long) createNamedQuery("Resource.findTotalSizeByContainer").setParameter("container", container).setParameter("status", ResourceStatusEnum.DELETED).setMaxResults(1).getSingleResult();
    }

    public List<BigInteger> getSampleSuffixBySampleNamePrefixAndContainer(String sampleNamePrefix, Container container) {
        String containerIds = container.getIdString();
        if (container.getProject() != null) {
            containerIds += ", " + container.getProject().getIdString();
        }
        return createNativeQuery("select cast(suffix as bigint) from sample, substring(name, '^" + sampleNamePrefix + "([0-9]*)$') as suffix where containerid in (" + containerIds + ") and suffix <> '' and length(suffix) < 19 order by cast(suffix as bigint) desc")
            .getResultList();
    }

    public List<Object[]> getTimelineEventsByContainerId(long containerId, TimelineEnum timelineEnum) {
        return createQuery("select created, count(*) from " + timelineEnum.getTableName() + " where containerId = :containerId group by created order by created")
            .setParameter("containerId", containerId).getResultList();
    }

    public Long getTimelineEventsCountByContainerId(long containerId, TimelineEnum timelineEnum) {
        return (Long) createQuery("select count(*) from " + timelineEnum.getTableName() + " where containerId = :containerId").setParameter("containerId", containerId).setMaxResults(1)
            .getSingleResult();
    }

    public List<Object[]> getTimelineMemberEventsByContainerId(long containerId) {
        return createQuery(
            "select created, count(*), type from Mail where parentId = :parentId and type in ('MEMBER_ADD','MEMBER_REMOVE','MEMBER_ADD','MEMBER_REMOVE') group by type, created order by created, type")
            .setParameter("parentId", containerId).getResultList();
    }

    public List<Object[]> getTimelineOrderEventsByProjectId(long projectId) {
        return createQuery("select created, count(*) from Order where projectId = :projectId group by created order by created")
            .setParameter("projectId", projectId).getResultList();
    }

    public List<Container> getTrackableContainers(String filterString, Collection<Container> excluded, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addContainerTrackableClause(user);
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<Container>) entityQuery.getResultList();
    }

    public LinkedHashMap<String, String> isValidCoaching(Container container) {
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();
        if (container.getCoachBackup() != null) {
            if (container.getCoach() == null) {
                errorMsg.put(Constants.EDIT + ":coachautocomplete", Messages.get("coachRequiredException"));
            } else if (container.getCoachBackup().equals(container.getCoach())) {
                errorMsg.put(Constants.EDIT + ":coachbackupautocomplete", Messages.get("coachSameException"));
            }
        }
        return errorMsg;
    }

    public LinkedHashMap<String, String> isValidVatAndReferenceNumber(Container container) {
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();
        if (container.getOrganizationType().isExternal() && container.getBillingInfo() != null) {
            if ((!container.isManaged() || container.getBillingInfo().getOldVatNumber() != null) && container.getBillingInfo().getVatNumber() == null) {
                errorMsg.put(Constants.EDIT + ":vatNumber", Constants.REQUIRED);
            }

            if ((!container.isManaged() || container.getBillingInfo().getOldReferenceNumber() != null) && container.getBillingInfo().getReferenceNumber() == null) {
                errorMsg.put(Constants.EDIT + ":referenceNumber", Constants.REQUIRED);
            }
        }
        return errorMsg;
    }

    protected Set<String> postUpdate(Container container, User currentUser, Set<Mail> mails) {
        Set<String> errorMsg = new HashSet<>();
        Set<User> membersToBeRemoved = new HashSet<>();
        boolean includeRequester = container.getRequester() != null;
        // Grant the projectManager role to the requester, budgetOfficer, leader, contact in case they were changed.
        if (container.getRequester() != null && container.isRequesterChanged()) {
            errorMsg.addAll(addManager(container, find(User.class, container.getRequester().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
            if (!container.hasSpecificFunction(container.getOldRequester(), false)) {
                membersToBeRemoved.add(container.getOldRequester());
                includeRequester = false;
            }
        }
        if (container.getBudgetOfficer() != null && container.isBudgetOfficerChanged()) {
            errorMsg.addAll(addManager(container, find(User.class, container.getBudgetOfficer().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
            if (!container.hasSpecificFunction(container.getOldBudgetOfficer(), includeRequester)) {
                membersToBeRemoved.add(container.getOldBudgetOfficer());
            }
        }
        if (container.getContact() != null && container.isContactChanged()) {
            errorMsg.addAll(addManager(container, find(User.class, container.getContact().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
            if (!container.hasSpecificFunction(container.getOldContact(), includeRequester)) {
                membersToBeRemoved.add(container.getOldContact());
            }
        }
        if (container.getLeader() != null && container.isLeaderChanged()) {
            errorMsg.addAll(addManager(container, find(User.class, container.getLeader().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
            if (!container.hasSpecificFunction(container.getOldLeader(), includeRequester)) {
                membersToBeRemoved.add(container.getOldLeader());
            }
        }
        for (User memberToBeRemoved : membersToBeRemoved) {
            errorMsg.addAll(removeManager(find(User.class, memberToBeRemoved.getId()), container, mails).get(Constants.ERROR_MESSAGES));
        }
        return errorMsg;
    }

    public void removeFailedWorkunits(Container container) {
        try {
            if (container != null) {
                Iterator<Workunit> iterator = container.getWorkunits().iterator();
                while (iterator.hasNext()) {
                    final Workunit workunit = iterator.next();
                    if (workunit.isFailed()) {
                        logger.info("Removing failed workunit " + workunit.getId());
                        iterator.remove();
                        remove(workunit);
                    }
                }
            }
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    private Map<String, Set<String>> removeManager(User user, Container container, Set<Mail> mails) {
        return removeMembership(membershipService.getMembership(container, user), false, mails, true);
    }

    public Map<String, Set<String>> removeMembership(Membership membership, boolean logAndSendMail, Set<Mail> mails, boolean synchronizeWithAD) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        if (membership != null) {
            // Cache the user.
            final User user = membership.getUser();
            final Container container = membership.getContainer();
            if (container.isPendingOrReview()) {
                // Remove membership entry in case of non-accepted containers.
                remove(membership);
            } else {
                // Set the membership to former.
                membership.setDiscriminator(Membership.DISCRIMINATOR_FORMER);
                merge(membership);
            }
            if (!user.hasRoleImplicit(RoleEnum.CONTAINERREADER)) {
                // Remove the container from the user's trackedContainers.
                user.getTrackedContainers().remove(container);
                if (container.isContainerProject() && !container.getOrders().isEmpty()) {
                    user.getTrackedContainers().removeAll(container.getOrders());
                }
                // Remove the starred/viewed comments from the user iff the container will not be readable anymore after the membership removal.
                if (!(container.isContainerProject() && container.isPublished())) {
                    final Set<Long> containerIds = new HashSet<>();
                    containerIds.add(container.getId());
                    if (container.isContainerProject()) {
                        containerIds.addAll(container.getOrders().stream().map(Order::getId).collect(Collectors.toSet()));
                    }
                    for (Comment comment : commentService.getCommentsByParentIds(containerIds)) {
                        comment.getStarredBy().remove(user);
                        comment.getViewedBy().remove(user);
                        comment.setSetModifiedEnabled(false);
                        save(comment);
                    }
                }
            }
            // Create notification mails.
            mails.add(container.createMail(MailTypeEnum.MEMBER_REMOVE, user));
            mails.add(container.createMail(MailTypeEnum.MEMBER_REMOVE_CONTACT, user));
            if (logAndSendMail) {
                // Create entity log.
                persist(container.createEntityUpdateLog("member", user.getIdString(), null));
                // Send mails.
                facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
            }

            facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("removedUserFromContainer").replace("{0}", user.getLogin()).replace("{1}", container.toString()));

            // Note: Do not remove! flush() needed here to enforce order of DB commands!
            flush();

            if (synchronizeWithAD) {
                // Synchronize these changes with the authentication database.
                userService.synchronizeWithAD(user);
                // Note: Do not remove! flush() needed here to enforce order of DB commands!
                flush();
            }
        }
        return facesMessages;
    }

    public Map<String, Set<String>> removeMembershipAndTrackContainer(Container container, User user) {
        if (container != null && user != null && user.hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) && container.isMemberRemovable(user)) {
            Set<Mail> mails = new HashSet<>();
            Map<String, Set<String>> facesMessages = removeMembership(user.getMembership(container), false, mails, true);
            if (!container.isTrackedByUser(user)) {
                user.setSetModifiedEnabled(false);
                user.getTrackedContainers().add(container);
                userService.save(user);
            }
            facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("trackedContainerHint").replace("{0}", container.toString()));
            return facesMessages;
        }
        return null;
    }

    public Map<String, Set<String>> saveUserOnBehalf(User user) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        if (user != null) {
            // Save and check affiliation.
            affiliationHelperService.saveAndCheckAffiliation(user);
            // Generate login and password.
            char[] generatedPassword = user.createLoginAndPassword();
            persist(user);
            // Clear sensitive data.
            StringHelper.clearCharArray(generatedPassword);

            // Send mail.
            facesMessages.get(Constants.ERROR_MESSAGES).add(userService.sendMail(user, user.getPassword(), null, MailTypeEnum.USER_CREATE_ONBEHALF));
            facesMessages.get(Constants.ERROR_MESSAGES).add(userService.sendMail(user, null, null, MailTypeEnum.USER_CONFIRMATION));
        }
        return facesMessages;
    }

    public String sendMail(Container container, MailTypeEnum mailTypeEnum) {
        return sendMail(container, mailTypeEnum, null);
    }

    public String sendMail(Container container, MailTypeEnum mailTypeEnum, User member) {
        Mail mail = container.createMail(mailTypeEnum, member);
        return !mail.getRecipients().isEmpty() ? mailSendService.send(mail) : null;
    }

    public Map<String, Set<String>> sendMailCustomStateTransition(Container container) {
        if (container != null) {
            CustomContainerStatus customContainerStatus = container.getLastCustomState();
            if (customContainerStatus != null) {
                Mail mail = container.createMailCustomStateTransition(customContainerStatus.getName());
                if (mail != null) {
                    Set<Mail> mails = new HashSet<>();
                    mails.add(mail);
                    Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("sentMailCustomStateTransition"));
                    facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
                    if (!mails.isEmpty()) {
                        setCustomContainerStatusSentMail(container);
                        return facesMessages;
                    }
                }
            }
        }
        return null;
    }

    public void setCustomContainerStatusSentMail(Container container) {
        // Important: Re-read container to make sure that the last custom state is read from the database and not still having the id=0!
        container = find(container.getClass(), container.getId());
        CustomContainerStatus customContainerStatus = container.getLastCustomState();
        if (customContainerStatus != null) {
            customContainerStatus.setSentMail(true);
            update(customContainerStatus);
        }
    }

    public Map<String, Set<String>> setStatusAndSave(Container container, StatusEnum statusEnum) {
        container.changeStatus(statusEnum);
        container.setIndexDependents(true);
        super.save(container);
        flush();
        for (User user : container.getMembers()) {
            boolean syncRequired = user.recomputeComputerLoginAndDataAccessEnabled();
            super.save(user);
            if (syncRequired) {
                userService.synchronizeWithADJobExecute(user);
            }
        }
        return createDisplayFacesMessagesMap(Messages.get(container.getClassLabelLowerCase()) + " " + statusEnum.getLabel());
    }

    public Map<String, Set<String>> switchRole(Membership membership, boolean logAndSendMail, Set<Mail> mails) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        if (membership != null) {
            final User user = membership.getUser();
            final Container container = membership.getContainer();
            if (membership.isRoleMember()) {
                membership.switchRole();
                merge(membership);
                // Create notification mails.
                mails.add(container.createMail(MailTypeEnum.MEMBER_ROLE_UPGRADE, user));
                mails.add(container.createMail(MailTypeEnum.MEMBER_ROLE_UPGRADE_CONTACT, user));
                if (logAndSendMail) {
                    // Create entity log.
                    persist(container.createEntityUpdateLog("managerupgrade", null, user.getIdString()));
                    // Send mails.
                    facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
                }

                facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("upgradeUserToManager").replace("{0}", user.getLogin()));
            } else {
                membership.switchRole();
                merge(membership);
                // Create notification mails.
                mails.add(container.createMail(MailTypeEnum.MEMBER_ROLE_DOWNGRADE, user));
                mails.add(container.createMail(MailTypeEnum.MEMBER_ROLE_DOWNGRADE_CONTACT, user));
                if (logAndSendMail) {
                    // Create entity log.
                    persist(container.createEntityUpdateLog("memberdowngrade", null, user.getIdString()));
                    // Send mails.
                    facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
                }

                facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("downgradeUserToMember").replace("{0}", user.getLogin()));
            }
        }
        return facesMessages;
    }

    public void userDecisionSubmitted(Container container) {
        if (container.userDecisionSubmitted()) {
            save(container);
            sendMail(container, MailTypeEnum.CONTAINER_USER_DECISION_SUBMITTED);
            setCustomContainerStatusSentMail(container);
        }
    }
}