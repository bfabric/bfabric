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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.ClassHelper;

@Named
@Stateless
public class DuplicateService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    public boolean checkIgnoredDuplicateExistenceByTableNamePrefixAndIds(String tableNamePrefix, long id1, long id2) {
        return createNativeQuery("select * FROM " + tableNamePrefix + "duplicateignore WHERE id1 = :id1 and id2 = :id2").setParameter("id1", id1).setParameter("id2", id2).setMaxResults(1)
            .getResultList().isEmpty();
    }

    public boolean checkMergeRequestExistenceByTableNamePrefixAndIds(String tableNamePrefix, long id1, long id2) {
        return createNativeQuery("SELECT * FROM " + tableNamePrefix + "mergerequest WHERE id1 = :id1 and id2 = :id2").setParameter("id1", id1).setParameter("id2", id2).setMaxResults(1).getResultList()
            .isEmpty();
    }

    public void deleteAllDuplicatesByTableName(String tableNamePrefix) {
        createNativeQuery("DELETE FROM " + tableNamePrefix + "duplicate").executeUpdate();
    }

    public void deleteIgnoredDuplicatesByTableNamePrefixAndIds(String tableNamePrefix, long id1, long id2) {
        createNativeQuery("DELETE FROM " + tableNamePrefix + "duplicateignore WHERE id1 = :id1 and id2 = :id2").setParameter("id1", id1).setParameter("id2", id2).executeUpdate();
    }

    public void duplicateReset(boolean all, String mergeableClass, User user) {
        for (final Class<Mergeable> clazz : ClassHelper.getMergeableEntityClasses()) {
            final String className = clazz.getSimpleName();
            if (all || className.equals(mergeableClass)) {
                setLastSyncedAndLastSyncedByOfDuplicateTableByClassName(null, user.getLogin(), className);
                deleteAllDuplicatesByTableName(ClassHelper.getTrimmedClassName(clazz));
            }
        }
    }

    public String duplicateSync(boolean all, String mergeableClass, User user) {
        final StringBuilder msg = new StringBuilder();
        for (final Class<Mergeable> clazz : ClassHelper.getMergeableEntityClasses()) {
            final String className = clazz.getSimpleName();
            if (all || className.equals(mergeableClass)) {
                final long startTime = System.currentTimeMillis();
                final String tableName = ClassHelper.getTrimmedClassName(clazz);
                setDuplicateByTableName(tableName);
                setLastSyncedAndLastSyncedByOfDuplicateTableByClassName(LocalDate.now(), user.getLogin(), className);
                msg.append(className).append("[").append(System.currentTimeMillis() - startTime).append("ms] ");
            }
        }

        return Messages.get("duplicateSyncCompleteHint") + ". " + msg;
    }

    public List<Object[]> getDuplicatesIgnoredByMergeableClass(String mergeableClass) {
        return createNativeQuery("SELECT id1,id2,name1,name2 FROM " + mergeableClass.toLowerCase() + "duplicateignore di ORDER BY name1, name2").getResultList();
    }

    public BfabricLazyDataModel<AbstractBaseEntity> getEntitiesByClazz(Class<?> clazz) {
        return new BfabricLazyDataModel<>(createEntityQuery(clazz));
    }

    public String getLastSyncedByClassName(String className) {
        return (String) createNativeQuery("SELECT to_char(lastSynced, 'YYYY-MM-DD HH24:MI:SS') || ' by ' || lastSyncedBy FROM duplicatetable WHERE classname = :classname").setParameter("classname",
            className).getSingleResult();
    }

    public String getMinLastSynced() {
        return (String) createNativeQuery("SELECT DISTINCT to_char(lastSynced, 'YYYY-MM-DD HH24:MI:SS') FROM duplicatetable WHERE lastsynced <= (SELECT MIN(lastsynced) FROM duplicatetable) ")
            .getSingleResult();
    }

    public List<Object[]> getPotentialDuplicatesByMergeableClass(String mergeableClass) {
        return createNativeQuery("SELECT id1,id2,name1,name2,score FROM " + mergeableClass.toLowerCase() + "duplicate d " + "WHERE NOT EXISTS(SELECT * FROM " + mergeableClass.toLowerCase()
            + "duplicateignore WHERE id1=d.id1 AND id2=d.id2) ORDER BY name1, name2").getResultList();
    }

    public List<Object[]> getPotentialUserDuplicates() {
        return createNativeQuery(
            "SELECT id1,id2,name1,name2,score, (SELECT created FROM usermergerequest WHERE id1=sd.id1 and id2=sd.id2) as mergerequest, COALESCE((SELECT false FROM usermergerequest WHERE id1=sd.id1 and id2=sd.id2), true) as mergerequestable "
                + " FROM userduplicate sd WHERE NOT EXISTS(SELECT * FROM userduplicateignore WHERE id1=sd.id1 AND id2=sd.id2) ORDER BY name1, name2").getResultList();
    }

    public List<Object[]> getUserDuplicatesIgnored() {
        return createNativeQuery("SELECT id1,id2,name1,name2, 0, " + "(SELECT created FROM usermergerequest WHERE id1=sd.id1 and id2=sd.id2) as mergerequest, "
            + "COALESCE((SELECT false FROM usermergerequest WHERE id1=sd.id1 and id2=sd.id2), true) as mergerequestable " + "FROM userduplicateignore sd ORDER BY name1, name2").getResultList();
    }

    public String ignore(String type, long dupId1, long dupId2, String dupName1, String dupName2, User user) {
        try {
            long id1;
            long id2;
            String name1;
            String name2;

            // To increase performance, all id-pairs are stored such that id1 < id2.
            if (dupId1 > dupId2) {
                id1 = dupId2;
                id2 = dupId1;
                name1 = dupName2;
                name2 = dupName1;
            } else {
                id1 = dupId1;
                id2 = dupId2;
                name1 = dupName1;
                name2 = dupName2;
            }

            // Insert the pair to be ignored if it is not already ignored.
            if (checkIgnoredDuplicateExistenceByTableNamePrefixAndIds(type, id1, id2)) {
                insertDuplicateIgnoreByTableNamePrefix(type, id1, id2, name1, name2, user);
                return Messages.get("duplicateIgnoredHint");
            }

            return Messages.get("duplicateAlreadyIgnoredHint");
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void insertDuplicateIgnoreByTableNamePrefix(String tableNamePrefix, long id1, long id2, String name1, String name2, User user) {
        createNativeQuery("insert into " + tableNamePrefix + "duplicateignore (id1, id2, name1, name2, created, createdby) " + "values (:id1, :id2, :name1, :name2, CURRENT_DATE, :createdby)")
            .setParameter("id1", id1).setParameter("id2", id2).setParameter("name1", name1).setParameter("name2", name2).setParameter("createdby", user.getLogin())
            .executeUpdate();
    }

    public void insertMergeRequestByTableNamePrefix(String tableNamePrefix, long id1, long id2, User user) {
        createNativeQuery("INSERT INTO " + tableNamePrefix + "mergerequest VALUES (:id1, :id2, CURRENT_TIMESTAMP, :createdby)").setParameter("id1", id1).setParameter("id2", id2).setParameter(
            "createdby", user.getLogin()).executeUpdate();
    }

    public Map<String, Set<String>> mergeRequest(String type, Long mid1, Long mid2, User user) {
        try {
            Map<String, Set<String>> facesMessages = createFacesMessagesMap();
            String tableNamePrefix;
            long id1;
            long id2;
            if (type.equalsIgnoreCase(User.class.getSimpleName())) {
                tableNamePrefix = User.class.getSimpleName();

                // To increase performance, all id-pairs are stored such that id1 < id2.
                if (mid1 > mid2) {
                    id1 = mid2;
                    id2 = mid1;
                } else {
                    id1 = mid1;
                    id2 = mid2;
                }

                // Insert the merge request pair if it is not already merge requested.
                if (checkMergeRequestExistenceByTableNamePrefixAndIds(tableNamePrefix, id1, id2)) {
                    insertMergeRequestByTableNamePrefix(tableNamePrefix, id1, id2, user);

                    // If this pair was ignored, then remove this entry from ignore table.
                    deleteIgnoredDuplicatesByTableNamePrefixAndIds(tableNamePrefix, id1, id2);
                }

                // Send mail.
                facesMessages.get(Constants.ERROR_MESSAGES).addAll(sendMergeRequestMail((User) fetch(User.class, id1), (User) fetch(User.class, id2)));
                facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("userMergeRequested"));
            } else {
                facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("mergeRequestInvalid"));
            }
            return facesMessages;
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    private Set<String> sendMergeRequestMail(User user1, User user2) {
        Set<String> errorMsg = new HashSet<>();

        Mail mail = new Mail();
        mail.setType(MailTypeEnum.USER_MERGE_REQUEST);
        mail.setParent(user1);
        mail.setRecipient(user1);
        mail.setInput("user1", user1);
        mail.setInput("user2", user2);
        mailSendService.send(mail);

        mail = new Mail();
        mail.setType(MailTypeEnum.USER_MERGE_REQUEST);
        mail.setParent(user2);
        mail.setRecipient(user2);
        mail.setInput("user1", user1);
        mail.setInput("user2", user2);
        mailSendService.send(mail);

        return errorMsg;
    }

    public void setDuplicateByTableName(String tableName) {
        createNativeQuery("select set" + tableName + "duplicate()").getSingleResult();
    }

    public void setLastSyncedAndLastSyncedByOfDuplicateTableByClassName(LocalDate lastSynced, String lastSyncedBy, String className) {
        if (lastSynced != null) {
            createNativeQuery("UPDATE duplicatetable SET lastSynced = :date, lastSyncedBy = :username WHERE className = :className").setParameter("className", className).setParameter("date",
                lastSynced).setParameter("username", lastSyncedBy).executeUpdate();
        } else {
            createNativeQuery("UPDATE duplicatetable SET lastsynced = to_timestamp('1970'), lastsyncedby = :username WHERE className = :className").setParameter("className", className).setParameter(
                "username", lastSyncedBy).executeUpdate();
        }
    }
}