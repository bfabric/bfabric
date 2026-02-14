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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AccessRequest;
import org.bfabric.entity.AccessRequestType;
import org.bfabric.entity.Application;
import org.bfabric.entity.Booking;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.Contract;
import org.bfabric.entity.Credit;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Event;
import org.bfabric.entity.Executable;
import org.bfabric.entity.ExternalJob;
import org.bfabric.entity.Feedback;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Membership;
import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.Role;
import org.bfabric.entity.Run;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.ShibbolethLog;
import org.bfabric.entity.Storage;
import org.bfabric.entity.Submitter;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.entity.WrapperCreator;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.BfabricPasswordHash;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class UserService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    @Inject
    private AffiliationHelperService affiliationHelperService;

    @Inject
    private ContainerService containerService;

    @Inject
    private ExternalJobService externalJobService;

    @Inject
    private RoleService roleService;

    public UserService() {
        super(User.class);
    }

    private static Set<Class<?>> getIndexableClasses() {
        Set<Class<?>> indexableClasses = new HashSet<>();
        for (IndexMapEnum indexMapEnum : IndexMapEnum.values()) {
            indexableClasses.add(indexMapEnum.getEntityClass());
        }
        return indexableClasses;
    }

    public void addRoleUserAndSynchronizeWithAD(Container container, Set<User> users) {
        if (container != null && container.isStatusSyncable()) {
            if (users != null && !users.isEmpty()) {
                for (final User user : users) {
                    user.addRoleUser();
                    synchronizeWithAD(user);
                }
            }
            synchronizeWithADEntireContainer(container);
        }
    }

    public void addRoleUserAndSynchronizeWithAD(Container container) {
        if (container != null) {
            addRoleUserAndSynchronizeWithAD(container, container.getMembersTransitive());
        }
    }

    public void addRoleUserAndSynchronizeWithAD(User user, Container container) {
        if (user != null && container != null) {
            Set<User> users = new HashSet<>();
            users.add(user);
            addRoleUserAndSynchronizeWithAD(container, users);
        }
    }

    public void anonymize(User user) {
        if (user != null && user.isAnonymizeRendered()) {
            user.anonymize();
            synchronizeWithAD(user);
        }
    }

    public int assignInstruments(Set<Instrument> instruments, Set<User> users) {
        int assignments = 0;
        if (!instruments.isEmpty() && !users.isEmpty()) {
            for (User user : users) {
                // Compute the new instruments for the given user.
                Set<Instrument> newInstruments = new HashSet<>(instruments);
                newInstruments.removeAll(user.getTrainedInstruments());
                if (!newInstruments.isEmpty()) {
                    // Assign instruments to the user and create an entity log entry.
                    user.getTrainedInstruments().addAll(newInstruments);
                    merge(user);
                    assignments += newInstruments.size();
                }
            }
        }
        return assignments;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void checkComputerLoginValidity(Long id) {
        User user = find(User.class, id);
        user.setLogEntity(false);
        user.setComputerLoginEnabled(false);
        user.checkComputerLoginValidity();
        merge(user);
    }

    public boolean checkUniqueAccessCardCode(User user, String cardCode) {
        return createNamedQuery("User.checkUniqueAccessCardCode").setParameter("accessCardCode", cardCode).setParameter("id", user.getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean checkUniqueAccessCardNumber(User user, String cardNumber) {
        return createNamedQuery("User.checkUniqueAccessCardNumber").setParameter("accessCardNumber", cardNumber).setParameter("id", user.getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean checkUniqueLogin(User user, String login) {
        return createNamedQuery("User.checkUniqueLogin").setParameter("login", login.trim()).setParameter("id", user.getId()).setMaxResults(1).getResultList().isEmpty();
    }

    private void computeIndexModifiedEntities(Set<Indexable> indexModifiedEntities, User merged) {
        for (Class<?> indexableEntityClass : getIndexableClasses()) {
            indexModifiedEntities.addAll(getEntities(indexableEntityClass, merged));
        }
    }

    private void createLogEntry(User userLeft, User merged, User currentUser) {
        // Insert a log entry into the user merge table and take over the merge request info, if any.
        long id1 = userLeft.getId();
        long id2 = merged.getId();
        if (id1 > id2) {
            long temp = id1;
            id1 = id2;
            id2 = temp;
        }
        createUserMerge(userLeft.getLogin(), merged.getLogin(), currentUser.getLogin(), id1, id2);
    }

    public void createUserMerge(String login, String loginMerged, String mergedBy, long id1, long id2) {
        createNativeQuery("INSERT INTO usermerge (login, loginmerged, mergedby, mergerequested, mergerequestedby) VALUES (:login, :loginMerged, :mergedBy, " + "(SELECT created FROM usermergerequest WHERE id1 = :id1 and id2 = :id2), " + "(SELECT createdby FROM usermergerequest WHERE id1 = :id1 and id2 = :id2)) ").setParameter("login", login)
            .setParameter("loginMerged", loginMerged).setParameter("mergedBy", mergedBy).setParameter("id1", id1).setParameter("id2", id2).executeUpdate();
    }

    public String deleteDeletableUsers() {
        List<User> users = getDeletableUsers();
        for (User user : users) {
            remove(user);
        }
        return users.isEmpty() ? null : String.valueOf(users.size());
    }

    public void disableMassMailForUser(long id) {
        createQuery("update User set massMailEnabled = false where id = :id").setParameter("id", id).executeUpdate();
    }

    public void employeeEntry(User user) {
        if (user != null && user.getEmpDegree() != null) {
            user.employeeEntry();
            user.createEntityLog(LogActionEnum.EMPLOYEE_ENTRY);
            synchronizeWithAD(user);
        }
    }

    public void employeeLeave(User user) {
        if (user != null) {
            // Note: Order matters! Do not change the order of the following statements.
            for (Container container : user.getContainers()) {
                if (!container.hasSpecificFunction(user, false) && !container.isInFinalState()) {
                    containerService.removeMembership(user.getMembership(container), false, new HashSet<>(), false);
                }
            }
            flush();
            user.employeeLeave();
            user.createEntityLog(LogActionEnum.EMPLOYEE_LEAVE);
            synchronizeWithAD(user);
            for (User backupOf : user.getBackupOf()) {
                backupOf.setBackup(null);
                merge(backupOf);
            }
            for (UserGroup userGroup : user.getUserGroups()) {
                if (userGroup.getUsers().size() == 1 && userGroup.getSupervisor().equals(user)) {
                    remove(userGroup);
                } else if (userGroup.isForEmployeesOnly()) {
                    userGroup.getUsers().remove(user);
                    merge(userGroup);
                }
            }
            for (Event event : user.getFutureEvents()) {
                remove(event);
            }
        }
    }

    public List<User> getAllBudgetOfficers() {
        return createNamedQuery("User.findContainerBudgetOfficers").getResultList();
    }

    public List<User> getAllOrderBudgetOfficers() {
        return createNamedQuery("User.findOrderBudgetOfficers").getResultList();
    }

    public List<User> getAllOrderRequesters() {
        return createNamedQuery("User.findOrderRequesters").getResultList();
    }

    public List<User> getAllPotentiallyDeletableUsers() {
        return createNamedQuery("User.findByPotentiallyDeletable").getResultList();
    }

    public List<User> getAllProjectLeaders() {
        return createNamedQuery("User.findProjectLeaders").getResultList();
    }

    public List<User> getAllUsers() {
        return (List<User>) getResultList();
    }

    public List<User> getAllUsersByTechnology(String technologyName) {
        return createNamedQuery("User.findByProjectTechnologies").setParameter("name", technologyName).getResultList();
    }

    public Set<User> getArchiveManager() {
        return getUsersByRoleEnum(RoleEnum.ARCHIVEMANAGER);
    }

    public List<User> getDeletableUsers() {
        List<User> users = new ArrayList<>();
        List<User> potentiallyDeletableUsers = getAllPotentiallyDeletableUsers();
        for (User user : potentiallyDeletableUsers) {
            if (user.isDeletableCondition()) {
                users.add(user);
            }
        }
        return users;
    }

    public List<User> getEmployees() {
        return createNamedQuery("User.findEmployees").getResultList();
    }

    public List<User> getEmployeesExcludingSupportAndMembers(String filterString, Container container) {
        HashSet<User> excluded = new HashSet<>();
        container.setSupporters(null);
        if (container.getSupporters() != null && !container.getSupporters().isEmpty()) {
            excluded.addAll(container.getSupporters());
        }
        if (container.getMembers() != null && !container.getMembers().isEmpty()) {
            excluded.addAll(container.getMembers());
        }
        return getEmployeesFilteredExcludingUsers(filterString, excluded);
    }

    public List<User> getEmployeesFiltered(String filterString, Collection<User> excluded) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("empDegree > 0");
        entityQuery.addNotInEntitiesClause(excluded);
        entityQuery.addUserNameWhereClause(filterString);
        return (List<User>) entityQuery.getResultList();
    }

    public List<User> getEmployeesFilteredExcludingUsers(String filterString, Collection<User> excluded) {
        EntityQuery entityQuery = createEntityQuery();
        if (excluded != null && !excluded.isEmpty()) {
            entityQuery.addWhereClause("empDegree > 0 AND entity NOT IN :entities");
            entityQuery.addParameter("entities", excluded);
        } else {
            entityQuery.addWhereClause("empDegree > 0");
        }
        entityQuery.addUserNameWhereClause(filterString);
        return (List<User>) entityQuery.getResultList();
    }

    public List<User> getEmployeesFilteredIncludingUser(String filterString, User included) {
        return getEmployeesFilteredIncludingUsers(filterString, Collections.singleton(included));
    }

    public List<User> getEmployeesFilteredIncludingUsers(String filterString, Collection<User> included) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("empDegree > 0 OR entity IN :entities");
        entityQuery.addParameter("entities", included);
        entityQuery.addUserNameWhereClause(filterString);
        return (List<User>) entityQuery.getResultList();
    }

    public List<User> getEmployeesIncludingUser(User user) {
        return createNamedQuery("User.findEmployeesIncluding").setParameter("user", user).getResultList();
    }

    public List<User> getEmployeesOrUsersFiltered(String filterString, Collection<User> excluded, Set<User> users) {
        EntityQuery entityQuery = createEntityQuery();
        StringBuilder where = new StringBuilder();
        if (users != null && !users.isEmpty()) {
            where.append("(empDegree > 0 OR entity IN :users)");
            entityQuery.addParameter("users", users);
        } else {
            where.append("empDegree > 0");
        }
        entityQuery.addWhereClause(where.toString());
        entityQuery.addNotInEntitiesClause(excluded);
        entityQuery.addUserNameWhereClause(filterString);
        entityQuery.setOrder("lastName ASC, firstName ASC");
        return (List<User>) entityQuery.getResultList();
    }

    public List<User> getEmployeesRegular() {
        return createNamedQuery("User.findEmployeesRegular").getResultList();
    }

    public List<? extends Indexable> getEntities(Class<?> clazz, User user) {
        if (clazz != null && user != null) {
            final StringBuilder queryBuilder = new StringBuilder("select entity from ");
            queryBuilder.append(clazz.getSimpleName()).append(" entity where createdBy = :login or modifiedBy = :login");
            if (clazz.equals(User.class)) {
                queryBuilder.append(" AND id <> :id");
            }
            final Query query = createQuery(queryBuilder.toString()).setParameter("login", user.getLogin());
            if (clazz.equals(User.class)) {
                query.setParameter("id", user.getId());
            }
            return query.getResultList();
        }

        return new ArrayList<>();
    }

    public List<User> getManagersByContainerId(long containerId) {
        return createNamedQuery("User.findCurrentManagersByContainerId").setParameter("containerId", containerId).getResultList();
    }

    public List<User> getMembersByContainerId(long containerId) {
        return createNamedQuery("User.findCurrentMembersByContainerId").setParameter("containerId", containerId).getResultList();
    }

    public BfabricLazyDataModel<User> getPotentialMembers(Container container) {
        EntityQuery entityQuery = createEntityQuery();
        if (!container.getMembers().isEmpty()) {
            entityQuery.addWhereClause("entity not in (:members)");
            entityQuery.addParameter("members", container.getMembers());
        }
        entityQuery.setOrder("entity.lastName");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<User> getReviewEmployeeStatusTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("(empDegree IS NULL AND EXISTS (SELECT user.id FROM User user JOIN user.roles role WHERE user = entity AND role.name = 'employee')) OR (empDegree > 0 AND NOT EXISTS (SELECT user.id FROM User user JOIN user.roles role WHERE user = entity AND role.name = 'employee'))");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Set<User> getReviewManagers() {
        return getUsersByRoleEnum(RoleEnum.REVIEWMANAGER);
    }

    public BfabricLazyDataModel<User> getRevokeRoleTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.roles role");
        List<String> roles = new ArrayList<>();
        roles.add(RoleEnum.USER.getName());
        roles.add(RoleEnum.ALUMNI.getName());
        roles.add(RoleEnum.ADMIN.getName());
        roles.add(RoleEnum.INTERNAL.getName());
        roles.add(RoleEnum.STEERINGCOMMITTEE.getName());
        roles.add(RoleEnum.FEEDER.getName());
        roles.add(RoleEnum.REVIEWER.getName());
        roles.add(RoleEnum.EXECUTABLEMANAGER.getName());
        entityQuery.addWhereClause("entity.empDegree is null AND role.name NOT IN (" + CollectionHelper.print(roles, true) + ") AND NOT EXISTS (SELECT user.id FROM User user JOIN user.roles role where user = entity and role.name in ('" + RoleEnum.EMPLOYEE.getName() + "', '" + RoleEnum.INTERNAL.getName() + "'))");
        entityQuery.setOrder("entity.id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Role getRoleAlumni() {
        return roleService.getRoleByRoleEnum(RoleEnum.ALUMNI);
    }

    public Role getRoleEmployee() {
        return roleService.getRoleByRoleEnum(RoleEnum.EMPLOYEE);
    }

    public Role getRoleUser() {
        return roleService.getRoleByRoleEnum(RoleEnum.USER);
    }

    public User getUserByEmail(String email) {
        if (StringHelper.isNotEmpty(email)) {
            final List<User> users = createNamedQuery("User.findByEmail").setParameter("email", email).setMaxResults(1).getResultList();
            if (!users.isEmpty()) {
                return users.get(0);
            }
        }
        return null;
    }

    public List<User> getUserByFirstNameAndLastName(String firstName, String lastName) {
        return createNamedQuery("User.findByFirstNameAndLastName").setParameter("firstName", firstName).setParameter("lastName", lastName).getResultList();
    }

    public User getUserByLogin(String login) {
        if (StringHelper.isNotEmpty(login)) {
            final List<User> users = createNamedQuery("User.findByLogin").setParameter("login", login).setMaxResults(1).getResultList();
            if (!users.isEmpty()) {
                return users.get(0);
            }
        }
        return null;
    }

    public User getUserByLoginOrEmail(String loginOrEmail) {
        if (StringHelper.isNotEmpty(loginOrEmail)) {
            return loginOrEmail.contains("@") ? getUserByEmail(loginOrEmail) : getUserByLogin(loginOrEmail);
        }
        return null;
    }

    public User getUserByShibbolethEmail(String userShibbolethEmail) {
        User user;
        try {
            user = (User) createNamedQuery("User.findByEmail").setParameter("shibbolethEmail", userShibbolethEmail).getSingleResult();
        } catch (final NoResultException nre) {
            return null;
        }
        return user;
    }

    public User getUserByShibbolethId(String userShibbolethId) {
        User user;
        try {
            user = (User) createNamedQuery("User.findByShibbolethId").setParameter("shibbolethId", userShibbolethId).getSingleResult();
        } catch (final NoResultException nre) {
            return null;
        }
        return user;
    }

    public List<User> getUserForComputerLoginValidityCheck() {
        return createNamedQuery("User.findForComputerLoginValidityCheck").setParameter("validityChecked", LocalDateTime.now().minusDays(getConfiguration().getCheckComputerLoginValidity()))
            .getResultList();
    }

    public List<User> getUsersByCompanyId(Long companyId) {
        return createNamedQuery("User.findByCompanyId").setParameter("companyId", companyId).getResultList();
    }

    public List<User> getUsersByDepartmentId(Long departmentId) {
        return createNamedQuery("User.findByDepartmentId").setParameter("departmentId", departmentId).getResultList();
    }

    public List<User> getUsersByOrganizationId(Long organizationId) {
        return createNamedQuery("User.findByOrganizationId").setParameter("organizationId", organizationId).getResultList();
    }

    public Set<User> getUsersByRoleEnum(RoleEnum roleEnum) {
        Role role = getRoleByRoleEnum(roleEnum);
        return role != null ? role.getUsers() : new HashSet<>();
    }

    public List<User> getUsersByRunningProjects() {
        return createNamedQuery("User.findCurrentUsersByRunningProjects").getResultList();
    }

    public List<User> getUsersByServiceTypeId(long serviceTypeId) {
        return createNamedQuery("User.findByServiceTypeId").setParameter("serviceTypeId", serviceTypeId).getResultList();
    }

    public List<User> getUsersFiltered(String filterString) {
        return getUsersFilteredExcluding(filterString, null);
    }

    public List<User> getUsersFilteredByRoleEnumIncludingUser(String filterString, RoleEnum roleEnum, User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addUserNameWhereClause(filterString);
        Set<User> users = new HashSet<>();
        if (roleEnum != null) {
            Role role = getRoleByRoleEnum(roleEnum);
            if (role != null) {
                users = role.getUsers();
            }
            if (user != null) {
                users.add(user);
            }
        }
        entityQuery.addInEntitiesClause(users);
        return (List<User>) entityQuery.getResultList();
    }

    public List<User> getUsersFilteredExcluding(String filterString, Collection<User> excluded) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addUserNameWhereClause(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        return (List<User>) entityQuery.getResultList();
    }

    public List<Object> getUsersMerged() {
        return getResult("select login, loginmerged, cast(merged as date), mergedby, cast(mergerequested as date), mergerequestedby from usermerge order by merged desc");
    }

    public void grantRoleEmployee(User user) {
        if (user != null) {
            user.addRole(getRoleEmployee());
            synchronizeWithAD(user);
        }
    }

    public void increaseInvalidLoginAttempts(User user) {
        user.setInvalidLoginAttempts(user.getInvalidLoginAttempts() + 1);
        update(user);
    }

    public boolean isEmailNotUnique(User user, String email) {
        return !createNamedQuery("User.checkUniqueEmail").setParameter("email", email.trim()).setParameter("id", user.getId()).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean isLoginBlacklisted(String name) {
        return !createNamedQuery("Blacklist.checkExists").setParameter("name", name).setParameter("context", "login").setMaxResults(1).getResultList().isEmpty();
    }

    public String isRequestAccessAllowed(User user) {
        if (!user.isAccessRequestable()) {
            logger.severe("Permission to request access for User " + user.getId() + " denied.");
            return Messages.get("permissionDenied");
        }
        if (!user.isComplete()) {
            logger.severe("Request for access was denied to User " + user.getId() + " because of an incomplete profile.");
            return Messages.get("profileIncomplete");
        }

        return null;
    }

    public boolean isTokenUsed(String token) {
        return !createNativeQuery("select token from UserToken where token = :token").setParameter("token", token).getResultList().isEmpty();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (entity instanceof User) {
            User user = (User) entity;
            validationErrorMsg = user.checkDownloadDirectoryPathValidity();
            if (user.getUserBillingInfo() != null && user.getUserBillingInfo().getOrganizationType() != null && user.getUserBillingInfo().getOrganizationType().isExternal()) {
                if ((!user.isManaged() || user.getUserBillingInfo().getOldVatNumber() != null) && user.getUserBillingInfo().getVatNumber() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":vatNumber", Constants.REQUIRED);
                }

                if ((!user.isManaged() || user.getUserBillingInfo().getOldReferenceNumber() != null) && user.getUserBillingInfo().getReferenceNumber() == null) {
                    validationErrorMsg.put(Constants.EDIT + ":referenceNumber", Constants.REQUIRED);
                }
            }
        }
        return validationErrorMsg;
    }

    public void logLogin(User user, char[] password, boolean passwordEncrypted) {
        user.setSetModifiedEnabled(false);
        user.setInvalidLoginAttempts(0);
        user.setLastLoginDate(LocalDateTime.now());
        // Store encoded password if the encoded length has changed: This is needed to smoothly do a transition to a new encoding scheme!
        if (!passwordEncrypted && user.getPassword().length() != BfabricPasswordHash.encode(password).length()) {
            user.setPassword(password);
            if (user.isSynchronizationWithADRequired()) {
                // Set password again to consistently set passwordAD!
                user.setPasswordAD(password);
            }
        }
        merge(user);
        // Log login!
        persist(new EntityLog(user, LogActionEnum.LOGIN, LogStatusEnum.DONE, user.getLogin()));
        // Synchronize with the AD if required.
        if (user.isSynchronizationWithADRequired() || user.isRecomputeComputerLoginAndDataAccessEnabledRequired()) {
            user = find(User.class, user.getId());
            synchronizeWithAD(user);
        }
        // Clear sensitive data.
        StringHelper.clearCharArray(password);
    }

    public void logShibbolethActivity(String type, User user) {
        ShibbolethLog shibbolethLog = new ShibbolethLog();
        shibbolethLog.setCreatedBy(user.getLogin());
        shibbolethLog.setType(type);
        shibbolethLog.setCreated(LocalDateTime.now());
        shibbolethLog.setShibbolethId(user.getShibbolethId());
        persist(shibbolethLog);
    }

    public String merge(User userLeft, User mergeSelection, User merged, User currentUser) throws RollbackException {
        try {
            TreeMap<String, Long> times = new TreeMap<>();
            long relativeTime = System.currentTimeMillis();

            // Merge the users.
            Map<String, Set<Indexable>> indexMap = mergeUsers(userLeft, mergeSelection, merged, currentUser);
            times.put("1 user merge", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Remove the deleted entity from the index.
            IndexHelper.removeEntities(indexMap.get(Constants.REMOVED));
            times.put("2 remove from index", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Reindex the merged user including its associated entities.
            userLeft.setIndexDependents(true);
            IndexHelper.indexEntity(userLeft);
            times.put("3 indexing userleft", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Reindex all dependent entities, including the entities whose createdBy / modifiedBy attributes has been reassigned.
            IndexHelper.indexEntities(indexMap.get(Constants.UPDATED));
            times.put("4 indexing rest", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Synchronize with the AD.
            synchronizeWithADJobExecute(userLeft);
            times.put("5 ad sync", System.currentTimeMillis() - relativeTime);

            logger.info(times.toString());

            // Send user merged mail.
            return sendMail(userLeft, merged);
        } catch (Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    private Map<String, Set<Indexable>> mergeUsers(User userLeft, User mergeSelection, User merged, User currentUser) {
        Map<String, Set<Indexable>> indexMap = new HashMap<>();
        indexMap.put(Constants.UPDATED, new HashSet<>());
        try {
            TreeMap<String, Long> times = new TreeMap<>();
            long relativeTime = System.currentTimeMillis();

            // Merge the selected attributes.
            userLeft.mergeAttributes(mergeSelection, merged);
            times.put("1 attribute merge", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Reassign the one-to-many associations. Important: Do this before merging the many-to-many associations!
            reassignOneToManyAssociations(merged, userLeft);
            times.put("2 reassign onetomany", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Reassign the implicit references to users.
            reassignGenericEntities(merged, userLeft);
            times.put("3 reassign generic entities", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Reassign the many-to-many associations.
            reassignManyToManyAssociations(merged, userLeft);
            times.put("4 reassign manytomany", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Important: Flush required to enforce Hibernate to adhere to the desired execution order (else Hibernate determines the execution order on its own).
            flush();
            times.put("5 flush", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Reassign the createdBy / modifiedBy attribute of the merged user.
            computeIndexModifiedEntities(indexMap.get(Constants.UPDATED), merged);
            reassignCreatedByModifiedBy(merged, userLeft);
            times.put("6 reassign createdby/modifiedby", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Insert a log entry into the user merge table and take over the merge request info, if any.
            createLogEntry(userLeft, merged, currentUser);
            times.put("7 creating entitylog", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Recompute computerLoginAndDataAccessEnabled.
            boolean syncRequired = userLeft.recomputeComputerLoginAndDataAccessEnabled();

            // Store the user left.
            merge(userLeft);

            // Important: Flush required to enforce Hibernate to adhere to the desired execution order (else Hibernate determines the execution order on its own).
            flush();
            times.put("8 flush userleft", System.currentTimeMillis() - relativeTime);
            relativeTime = System.currentTimeMillis();

            // Cache all entries to be removed from the index after the removal of the userMerge entity.
            indexMap.put(Constants.REMOVED, merged.getEntriesToBeRemovedFromIndex());

            // Remove the user.
            remove(merged);

            // Important: Flush required to enforce Hibernate to adhere to the desired execution order (else Hibernate determines the execution order on its own).
            flush();
            times.put("9 remove merged", System.currentTimeMillis() - relativeTime);

            if (syncRequired) {
                synchronizeWithADJobExecute(userLeft);
            }

            logger.info(times.toString());
        } catch (Exception e) {
            throw new RollbackException(e.getMessage());
        }
        return indexMap;
    }

    public void reassign(Class<?> clazz, User oldUser, User newUser, String attribute) {
        if (clazz != null && oldUser != null && newUser != null && attribute != null) {
            final StringBuilder queryBuilder = new StringBuilder("UPDATE " + clazz.getSimpleName() + " SET " + attribute + " = :newUser WHERE " + attribute + " = :oldUser");
            if (clazz.equals(User.class)) {
                queryBuilder.append(" AND id <> :id");
            }

            final Query query = createQuery(queryBuilder.toString());
            if (attribute.equalsIgnoreCase(Constants.CREATEDBY) || attribute.equalsIgnoreCase(Constants.MODIFIEDBY)) {
                query.setParameter("oldUser", oldUser.getLogin()).setParameter("newUser", newUser.getLogin());
            } else {
                query.setParameter("oldUser", oldUser).setParameter("newUser", newUser);
            }
            if (clazz.equals(User.class)) {
                query.setParameter("id", oldUser.getId());
            }
            query.executeUpdate();
        }
    }

    private void reassignCreatedBy(Class<?> clazz, User merged, User userLeft) {
        reassign(clazz, merged, userLeft, "createdBy");
    }

    private void reassignCreatedByModifiedBy(User merged, User userLeft) {
        TreeMap<Long, String> times = new TreeMap<>();

        long relativeTime = System.currentTimeMillis();
        int i = 1;
        for (Class<?> clazz : ClassHelper.getBaseEntityClasses()) {
            reassignCreatedModifiedBy(clazz, merged, userLeft);
            times.put(System.currentTimeMillis() - relativeTime, String.format("%03d", i++) + " " + clazz.getSimpleName());
            relativeTime = System.currentTimeMillis();
        }

        logger.info(times.toString());
    }

    private void reassignCreatedModifiedBy(Class<?> clazz, User merged, User userLeft) {
        reassignCreatedBy(clazz, merged, userLeft);
        reassignModifiedBy(clazz, merged, userLeft);
    }

    public void reassignExternalJobs(User oldUser, User newUser) {
        if (oldUser != null && newUser != null) {
            createQuery("UPDATE ExternalJob SET clientEntityId = :new WHERE clientEntityId = :old and clientEntityClassName in (:types)").setParameter("old", oldUser.getId())
                .setParameter("new", newUser.getId()).setParameter("types", User.class.getSimpleName()).executeUpdate();
        }
    }

    private void reassignGenericEntities(User merged, User userLeft) {
        reassignMails(merged, userLeft);
        reassignExternalJobs(merged, userLeft);
    }

    public void reassignMails(User oldUser, User newUser) {
        if (oldUser != null && newUser != null) {
            createQuery("UPDATE Mail SET parentId = :new WHERE parentId = :old and type in (:types)").setParameter("old", oldUser.getId()).setParameter("new", newUser.getId())
                .setParameter("types", MailTypeEnum.getUserMailTypes()).executeUpdate();
        }
    }

    private void reassignManyToManyAssociations(User merged, User userLeft) {
        if (!merged.getRoles().isEmpty()) {
            userLeft.getRoles().addAll(merged.getRoles());
            merged.getRoles().clear();
        }

        if (!merged.getTrackedServices().isEmpty()) {
            userLeft.getTrackedServices().addAll(merged.getTrackedServices());
            merged.getTrackedServices().clear();
        }

        if (!merged.getTrackedContainers().isEmpty()) {
            userLeft.getTrackedContainers().addAll(merged.getTrackedContainers());
            merged.getTrackedContainers().clear();
        }

        if (!merged.getMails().isEmpty()) {
            userLeft.getMails().addAll(merged.getMails());
            merged.getMails().clear();
        }

        if (!merged.getUserGroups().isEmpty()) {
            userLeft.getUserGroups().addAll(merged.getUserGroups());
            merged.getUserGroups().clear();
        }

        if (!merged.getServiceAreas().isEmpty()) {
            userLeft.getServiceAreas().addAll(merged.getServiceAreas());
            merged.getServiceAreas().clear();
        }

        if (!merged.getServiceTypes().isEmpty()) {
            userLeft.getServiceTypes().addAll(merged.getServiceTypes());
            merged.getServiceTypes().clear();
        }

        if (!merged.getServices().isEmpty()) {
            userLeft.getServices().addAll(merged.getServices());
            merged.getServices().clear();
        }

        if (!merged.getDiscussedContainers().isEmpty()) {
            userLeft.getDiscussedContainers().addAll(merged.getDiscussedContainers());
            merged.getDiscussedContainers().clear();
        }

        if (!merged.getBookableInstruments().isEmpty()) {
            userLeft.getBookableInstruments().addAll(merged.getBookableInstruments());
            merged.getBookableInstruments().clear();
        }

        if (!merged.getOperatorInstruments().isEmpty()) {
            userLeft.getOperatorInstruments().addAll(merged.getOperatorInstruments());
            merged.getOperatorInstruments().clear();
        }

        if (!merged.getTrainedInstruments().isEmpty()) {
            userLeft.getTrainedInstruments().addAll(merged.getTrainedInstruments());
            merged.getTrainedInstruments().clear();
        }

        if (!merged.getResourceBaskets().isEmpty()) {
            userLeft.getResourceBaskets().addAll(merged.getResourceBaskets());
            merged.getResourceBaskets().clear();
        }

        if (!merged.getUserPurchases().isEmpty()) {
            userLeft.getUserPurchases().addAll(merged.getUserPurchases());
            merged.getUserPurchases().clear();
        }
    }

    private void reassignModifiedBy(Class<?> clazz, User merged, User userLeft) {
        reassign(clazz, merged, userLeft, "modifiedBy");
    }

    private void reassignOneToManyAssociations(User merged, User userLeft) {
        reassign(AccessRequest.class, merged, userLeft, "user");
        reassign(Application.class, merged, userLeft, "supervisor");
        reassign(User.class, merged, userLeft, "backup");
        reassign(Booking.class, merged, userLeft, "bookingIssuer");
        reassign(Charge.class, merged, userLeft, "charger");
        reassign(Container.class, merged, userLeft, "bioinformatician");
        reassign(Container.class, merged, userLeft, "budgetOfficer");
        reassign(Container.class, merged, userLeft, "coach");
        reassign(Container.class, merged, userLeft, "coachBackup");
        reassign(Container.class, merged, userLeft, "contact");
        reassign(Container.class, merged, userLeft, "leader");
        reassign(Container.class, merged, userLeft, "requester");
        reassign(Container.class, merged, userLeft, "statusModifiedBy");
        reassign(Contract.class, merged, userLeft, "approvedBy");
        reassign(Contract.class, merged, userLeft, "supervisor");
        reassign(Credit.class, merged, userLeft, "user");
        reassign(Event.class, merged, userLeft, "user");
        reassign(Executable.class, merged, userLeft, "supervisor");
        reassign(Executable.class, merged, userLeft, "statusModifiedBy");
        reassign(ExternalJob.class, merged, userLeft, "statusModifiedBy");
        reassign(Feedback.class, merged, userLeft, "acknowledgedBy");
        reassign(Feedback.class, merged, userLeft, "user");
        reassign(Instrument.class, merged, userLeft, "supervisor");
        reassign(Instrument.class, merged, userLeft, "admin");
        reassign(InstrumentReservation.class, merged, userLeft, "booker");
        reassign(InstrumentReservation.class, merged, userLeft, "operator");
        reassign(InstrumentReservation.class, merged, userLeft, "user");
        reassign(Offer.class, merged, userLeft, "requester");
        reassign(OfferedCharge.class, merged, userLeft, "charger");
        reassign(Purchase.class, merged, userLeft, "orderedBy");
        reassign(Purchase.class, merged, userLeft, "orderItemReceivedBy");
        reassign(Plate.class, merged, userLeft, "statusModifiedBy");
        reassign(Plate.class, merged, userLeft, "supervisor");
        reassign(Run.class, merged, userLeft, "statusModifiedBy");
        reassign(Run.class, merged, userLeft, "supervisor");
        reassign(ServiceType.class, merged, userLeft, "coach");
        reassign(ServiceType.class, merged, userLeft, "coachBackup");
        reassign(Storage.class, merged, userLeft, "supervisor");
        reassign(Submitter.class, merged, userLeft, "supervisor");
        reassign(UserGroup.class, merged, userLeft, "supervisor");
        reassign(WrapperCreator.class, merged, userLeft, "supervisor");

        // Reassign all memberships.
        Set<Membership> mergedMemberships = new HashSet<>(merged.getMemberships());
        for (Membership mergedMembership : mergedMemberships) {
            Membership membershipLeft = userLeft.getMembership(mergedMembership.getContainer());
            if (membershipLeft != null) {
                // Take over the membership only if the membership is former or its role is current member while the role of the user merge membership is current manager.
                if (!(membershipLeft.isCurrent() && membershipLeft.isRoleManager()) && mergedMembership.isCurrent() && (!membershipLeft.isCurrent() || membershipLeft.isCurrent() && mergedMembership.isRoleManager())) {
                    membershipLeft.setDiscriminator(mergedMembership.getDiscriminator());
                    membershipLeft.setRole(mergedMembership.getRole());
                    membershipLeft.setCreated(mergedMembership.getCreated());
                    membershipLeft.setCreatedBy(mergedMembership.getCreatedBy());
                }
            } else {
                userLeft.getMemberships().add(mergedMembership.copy(userLeft));
            }
            merged.getMemberships().remove(mergedMembership);
        }
    }

    public Map<String, Set<String>> requestAccess(User user) {
        Map<String, Set<String>> map = new HashMap<>();
        map.put(Constants.ERROR_MESSAGES, new HashSet<>());
        map.put("accessRequestId", new HashSet<>());

        // Create an access request.
        final AccessRequest accessRequest = new AccessRequest(user);
        AccessRequestType accessRequestType = findByName(AccessRequestType.class, "Personal Card Access");
        if (user.getAffiliatedWithUZH()) {
            if (AccessRequestType.isValidPersonalAccessCardCode(user.getAccessCardCode())) {
                accessRequest.setAccessCardExpiryDate(null);
            } else if (AccessRequestType.isValidGuestAccessCardCode(user.getAccessCardCode())) {
                accessRequestType = findByName(AccessRequestType.class, "Guest Card Extension");
                accessRequest.setAccessCardExpiryDate(null);
            }
        } else {
            accessRequestType = findByName(AccessRequestType.class, "Guest Card Application");
            accessRequest.setAccessCardNumber(null);
            accessRequest.setAccessCardCode(null);
            accessRequest.setAccessCardExpiryDate(null);

            // Reset the access card data in case it was set before.
            user.resetAccessCard();
        }

        accessRequest.setAccessRequestType(accessRequestType);
        // Persist the access request and save the user.
        persist(accessRequest);
        super.save(user);

        switch (accessRequestType.getName()) {
        case "Personal Card Access":
        case "Guest Card Application":
            // Send mail to the user for whom the access was requested.
            map.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, accessRequest, MailTypeEnum.USER_REQUEST_ACCESS));
            // Send mail to the access manager such that the request can be reviewed.
            map.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, accessRequest, MailTypeEnum.USER_REQUEST_ACCESS_MANAGER));
            break;
        case "Guest Card Extension":
            // Send mail to the user for whom the extension was requested.
            map.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, accessRequest, MailTypeEnum.USER_REQUEST_EXTENSION));
            // Send mail to the extension manager such that the request can be reviewed.
            map.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, accessRequest, MailTypeEnum.USER_REQUEST_EXTENSION_MANAGER));
            break;
        default:
            break;
        }

        map.get("accessRequestId").add(accessRequest.getIdString());
        return map;
    }

    public Map<String, Set<String>> requestPasswordReset(String loginOrEmail) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        User user = getUserByLoginOrEmail(loginOrEmail);
        if (user != null) {
            // Reset the invalid login counter.
            user.setInvalidLoginAttempts(0);
            merge(user);

            // Send mail.
            facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, null, MailTypeEnum.USER_PASSWORD_LOST));

            String displayMessage = Messages.get("lostPasswordResent");
            if (!loginOrEmail.contains("@")) {
                displayMessage += " " + Messages.get("emailAddressOfUser");
            }
            displayMessage += " " + loginOrEmail;
            facesMessages.get(Constants.DISPLAY_MESSAGES).add(displayMessage);
        } else {
            // Do nothing but log for security audits (brute force attack).
            logger.warning("Invalid user " + loginOrEmail + " tried to obtain a new password.");
            facesMessages.get(Constants.ERROR_MESSAGES).add(Messages.get("invalidUser") + ": " + loginOrEmail);
        }
        return facesMessages;
    }

    public Map<String, Set<String>> resendVerificationMail(User user) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        user.setEmailActive(true);
        save(user);
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, null, MailTypeEnum.USER_CONFIRMATION_REQUIRED));
        facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("emailConfirmationMailSent"));
        return facesMessages;
    }

    public void resetComputerLoginActivated() {
        createQuery("update User set computerLoginActivated = false").executeUpdate();
    }

    public void resetShibbolethId(User user) {
        user.setShibbolethId(null);
        merge(user);
    }

    @Transactional
    public void revokeRoleEmployee(User user) {
        if (user != null) {
            user.revokeEmployeeRights();
            synchronizeWithAD(user);
        }
    }

    public Map<String, Set<String>> save(User user, User currentUser, char[] password, boolean isCreated, boolean isCompleteRequired) {
        Map<String, Set<String>> map = createFacesMessagesMap();
        boolean isEmailChanged = user.isEmailChanged();

        // Save and check affiliation (not only for the user but also for the billing info, if any).
        affiliationHelperService.saveAndCheckAffiliation(user);
        affiliationHelperService.saveAndCheckAffiliation(user.getUserBillingInfo());

        if (isCompleteRequired && !user.isComplete()) {
            throw new RollbackException(Messages.get("accessRequestRequiredAllField"));
        }

        if (currentUser == null) {
            // Just to be sure that the setPassword method is set.
            user.setPassword(password);
        }

        // Update the user account in the authentication database.
        synchronizeWithAD(user);

        // Check whether the user's computer login is now activated.
        if (!user.isComputerLoginActivated()) {
            setComputerLoginActivated(user);
        }

        if (isCreated) {
            map.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, null, MailTypeEnum.USER_CONFIRMATION));
            map.get(Constants.DISPLAY_MESSAGES).add(Messages.get("accountConfirmationMail"));
        } else if (isEmailChanged) {
            map.get(Constants.ERROR_MESSAGES).add(sendMail(user, null, null, MailTypeEnum.USER_CONFIRMATION_REQUIRED));
            map.get(Constants.DISPLAY_MESSAGES).add(Messages.get("emailConfirmationMailSent"));
        }

        map.put("savedUserId", new HashSet<>(Collections.singleton(user.getIdString())));
        return map;
    }

    public void saveAfterShibbolethLogin(User user, String uniqueID, boolean mapped) {
        user.setSetModifiedEnabled(false);
        user.setLastLoginDate(LocalDateTime.now());
        user.setShibbolethLastLoginDate(LocalDate.now());
        save(user);
        if (mapped) {
            user.setShibbolethId(uniqueID);
            logShibbolethActivity(Constants.SHIBBOLETH_ACCOUNT_AUTO_MAPPED, user);
        }
        logShibbolethActivity(Constants.SHIBBOLETH_LOGGED_IN, user);
    }

    public String saveRoles(User user, List<Role> selectedRoles) {
        final List<Role> rolesSpecific = roleService.getRolesSpecific();
        final Set<Role> newRoles = new HashSet<>();

        if (selectedRoles != null) {
            newRoles.addAll(selectedRoles);
        }

        // Compute all added roles.
        final Set<Role> addedRoles = new HashSet<>(newRoles);
        addedRoles.removeAll(user.getRoles());

        // Compute all removed roles. Important: Do not count the implicit roles which are removed from the target list before selection.
        final Set<Role> removedRoles = new HashSet<>(user.getRoles());
        removedRoles.removeAll(rolesSpecific);
        removedRoles.removeAll(newRoles);

        // Assign the roles to the user and create an entity log entry.
        final Set<Role> resetRoles = new HashSet<>(user.getRoles());
        // Important: Retain all specific roles since they are not in the new roles set.
        resetRoles.retainAll(rolesSpecific);
        resetRoles.addAll(newRoles);
        user.setSetModifiedEnabled(false);
        user.setRoles(resetRoles);
        save(user, false);

        // Synchronize with the AD.
        synchronizeWithAD(user);

        // Create a message.
        if (!addedRoles.isEmpty() || !removedRoles.isEmpty()) {
            final StringBuilder message = new StringBuilder(Messages.get("successfully"));
            if (!addedRoles.isEmpty()) {
                message.append(" ").append(Messages.get("addedRoles")).append(": ").append(CollectionHelper.printNames(addedRoles));
            }
            if (!removedRoles.isEmpty()) {
                if (!addedRoles.isEmpty()) {
                    message.append(" ").append(Messages.get("and").toLowerCase());
                }
                message.append(" ").append(Messages.get("removedRoles")).append(": ").append(CollectionHelper.printNames(removedRoles));
            }
            return message.toString();
        }

        return null;
    }

    public String sendMail(User user, String password, AccessRequest accessRequest, MailTypeEnum mailTypeEnum) {
        final Mail mail = new Mail();
        mail.setParent(user);
        mail.setType(mailTypeEnum);

        if (MailTypeEnum.USER_REQUEST_ACCESS_MANAGER.equals(mailTypeEnum) || MailTypeEnum.USER_REQUEST_EXTENSION_MANAGER.equals(mailTypeEnum)) {
            mail.addRecipients(getUsersByRoleEnum(RoleEnum.ACCESSREQUESTMANAGER));
        } else {
            // MailTypeEnum.USER_REQUEST_ACCESS
            // MailTypeEnum.USER_CREATE_ONBEHALF
            // MailTypeEnum.PASSWORD_LOST
            // MailTypeEnum.USER_CONFIRMATION
            // MailTypeEnum.USER_CONFIRMATION_REQUIRED
            // MailTypeEnum.USER_REQUEST_EXTENSION
            mail.setRecipient(user);

            switch (mailTypeEnum) {
            case USER_CREATE_ONBEHALF:
                mail.setInput("password", password);
                break;
            case USER_REQUEST_ACCESS:
                mail.setInput("accessRequestVisitWithAccessCard", Messages.get("accessRequestVisitWithAccessCard"));
                mail.setInput("accessRequestPickUpAccessCard", Messages.get("accessRequestPickUpAccessCard"));
                mail.setInput("accessRequestManagerOfficeTimes", Messages.get("accessRequestManagerOfficeTimes") + ": " + getConfiguration().getAccessRequestManagerOfficeTimes());
                break;
            default:
                break;
            }
        }

        mail.setInput("user", user);
        mail.setInput("accessRequest", accessRequest);
        return mailSendService.send(mail);
    }

    private String sendMail(User userLeft, User merged) {
        Mail mail = new Mail();
        mail.setParent(userLeft);
        mail.setType(MailTypeEnum.USER_MERGE);
        mail.setRecipient(userLeft);
        mail.setInput("merged", merged);
        mail.setInput("userLeft", userLeft);
        return mailSendService.send(mail);
    }

    public void setComputerLoginActivated(List<User> users) {
        for (final User user : users) {
            setComputerLoginActivated(user);
        }
    }

    public void setComputerLoginActivated(User user) {
        if (!user.isComputerLoginActivated() && user.hasRoleUserImplicit()) {
            user.setComputerLoginActivated(true);
            merge(user);
        }
    }

    public void setSystemUser(User user) {
        user.setMassMailEnabled(false);
        user.setEmailVerified(true);
        user.setComputerLoginActivated(true);
        synchronizeWithAD(user);
    }

    public void setUserAvailable() {
        createNamedQuery("User.resetAvailable").executeUpdate();
        createNamedQuery("User.setNotAvailable").executeUpdate();
    }

    public void setUserAvailableAM() {
        createNamedQuery("User.resetAvailable").executeUpdate();
        createNamedQuery("User.setNotAvailable").setParameter("time", LocalDateTime.now().plusHours(8)).executeUpdate();
    }

    public void setUserAvailablePM() {
        createNamedQuery("User.resetAvailable").executeUpdate();
        createNamedQuery("User.setNotAvailable").setParameter("time", LocalDateTime.now().plusHours(1)).executeUpdate();
    }

    public void switchAccountEnabled(User user) {
        user.switchAccountEnabled();
        super.save(user);
    }

    public boolean synchronizeWithAD(User user) {
        if (user != null) {
            user.setSetModifiedEnabled(false);
            user.recomputeComputerLoginAndDataAccessEnabled();
            save(user);
            synchronizeWithADJobExecute(user);
            return true;
        }
        return false;
    }

    public boolean synchronizeWithADEntireContainer(Container container) {
        if (container != null && container.isSyncable()) {
            final Executable executableContainerSync = find(Executable.class, getConfiguration().getMasterExecutableIdContainerSync());
            if (executableContainerSync != null) {
                externalJobService.persistAndExecute(new ExternalJob(container, executableContainerSync));
            }
            return true;
        }
        return false;
    }

    public void synchronizeWithADJobExecute(User user) {
        if (user != null && user.isSyncable()) {
            final Executable executableUserSync = find(Executable.class, getConfiguration().getMasterExecutableIdUserSync());
            if (executableUserSync != null) {
                externalJobService.persistAndExecute(new ExternalJob(user, executableUserSync));
            }
        }
    }

    public void updateLastContainer(User user, Container container) {
        if (user != null && container != null) {
            createQuery("update User set lastContainer.id = :containerId where id = :userId").setParameter("containerId", container.getId()).setParameter("userId", user.getId()).executeUpdate();
        }
    }

    public boolean useToken(String token) {
        if (getConfiguration().isOneTimeTokenEnabled() && isTokenUsed(token)) {
            return false;
        }
        createNativeQuery("insert into UserToken (token) values (:token)").setParameter("token", token).executeUpdate();
        return true;
    }
}
