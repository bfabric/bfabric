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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timer;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import javax.transaction.Transactional;

import org.apache.lucene.index.IndexWriter;
import org.bfabric.Messages;
import org.bfabric.entity.Application;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Container;
import org.bfabric.entity.Contract;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Link;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.ScheduledJobLog;
import org.bfabric.entity.User;
import org.bfabric.enums.JobEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.indexer.enums.IndexerEnum;
import org.bfabric.service.AgendaYearClosedService;
import org.bfabric.service.ApplicationService;
import org.bfabric.service.EntityService;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.LinkService;
import org.bfabric.service.MailSendService;
import org.bfabric.service.OfferService;
import org.bfabric.service.OrderService;
import org.bfabric.service.ResourceService;
import org.bfabric.service.StatisticsService;
import org.bfabric.service.UnassignedObjectsService;
import org.bfabric.service.UserService;
import org.bfabric.service.WorkunitService;
import org.bfabric.util.BfabricPasswordHash;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.RepositoryHelper;

@CustomFormAuthenticationMechanismDefinition(loginToContinue = @LoginToContinue(loginPage = "/home.html", errorPage = ""))
@DatabaseIdentityStoreDefinition(dataSourceLookup = "${'jdbc/bfabric_datasource'}", callerQuery = "#{'SELECT password FROM user_ WHERE login = lower(?)'}", groupsQuery = "SELECT name FROM impliedrolenames(lower(?))", hashAlgorithm = BfabricPasswordHash.class)
@Startup
@Singleton
@Named
public class ScheduledTaskManager implements Serializable {

    public static final String DISABLED = "Disabled ";

    private static final Logger logger = Logger.getLogger(ScheduledTaskManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    private AgendaYearClosedService agendaYearClosedService;

    @Inject
    private ApplicationService applicationService;

    @Inject
    private ConfManager confManager;

    @Inject
    private EntityService entityService;

    @Inject
    private InstrumentReservationService instrumentReservationService;

    @Inject
    private LinkService linkService;

    @Inject
    private MailSendService mailSendService;

    @Inject
    private OfferService offerService;

    @Inject
    private OrderService orderService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private StatisticsService statisticsService;

    @Inject
    private UnassignedObjectsService unassignedObjectsService;

    @Inject
    private UserService userService;

    @Inject
    private WorkunitService workunitService;

    @Schedule(hour = "2", persistent = false)
    @Asynchronous
    public void cancelPendingOrders() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.CancelPendingOrders);
        List<Order> pendingOrdersToBeCanceled = orderService.getPendingOrdersToBeCanceled();
        if (!pendingOrdersToBeCanceled.isEmpty()) {
            scheduledJobLog.appendLog(CollectionHelper.printIds(pendingOrdersToBeCanceled));
            for (Order order : pendingOrdersToBeCanceled) {
                orderService.cancel(order);
                sendMailOrderCanceled(order);
            }
        }
        updateJobLog(scheduledJobLog);
    }

    @Schedule(hour = "1", minute = "50", persistent = false)
    @Asynchronous
    public void checkComputerLoginValidity() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.CheckComputerLoginValidity);
        List<User> usersLastLogin = userService.getUserForComputerLoginValidityCheck();
        for (User user : usersLastLogin) {
            userService.checkComputerLoginValidity(user.getId());
        }
        scheduledJobLog.appendLog(String.valueOf(usersLastLogin.size()));
        updateJobLog(scheduledJobLog);
    }

    @Schedule(second = "*/30", minute = "*", hour = "*", persistent = false)
    @Asynchronous
    protected void checkIndexDirectory(final Timer timer) {
        if (getConfiguration() != null && getConfiguration().isReindexJobEnabled() && !getConfiguration().isIndexDirectoryExisting()) {
            reindex();
        }
        // Important: Cancel timer such that this method is only executed once!
        timer.cancel();
    }

    @Schedule(hour = "5", persistent = false)
    @Asynchronous
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void checkLinkValidity() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.CheckLinkValidity);
        List<Link> linksToBeChecked = linkService.getLinksForValidityCheck();
        int invalidCount = 0;
        for (Link link : linksToBeChecked) {
            Boolean valid = linkService.checkLinkValidity(link.getId());
            if (valid == null || !valid) {
                invalidCount++;
            }
        }
        scheduledJobLog.appendLog(linksToBeChecked.size() + " " + invalidCount);
        updateJobLog(scheduledJobLog);
    }

    @Schedule(hour = "5", minute = "20", persistent = false)
    @Asynchronous
    public void checkOfferValidityDuration() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.CheckOfferValidityDuration);
        List<Offer> offers = offerService.getExpiredOffers();
        for (Offer offer : offers) {
            offerService.checkOfferValidityDuration(offer.getId());
        }
        scheduledJobLog.appendLog(String.valueOf(offers.size()));
        updateJobLog(scheduledJobLog);
    }

    @Schedule(hour = "5", minute = "10", persistent = false)
    @Asynchronous
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void checkWebUrlValidity() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.CheckWebUrlValidity);
        List<Application> applicationsToBeChecked = applicationService.getApplicationsForWebUrlValidityCheck();
        int invalidCount = 0;
        for (Application application : applicationsToBeChecked) {
            if (!applicationService.checkWebUrlValidity(application.getId())) {
                invalidCount++;
                sendMailWebUrlNotFound(application);
            }
        }
        scheduledJobLog.appendLog(applicationsToBeChecked.size() + " " + invalidCount);
        updateJobLog(scheduledJobLog);
    }

    public void closeAgendaYear() {
        if (getConfiguration().isAgendaEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.CloseAgendaYear);
            agendaYearClosedService.closeAgendaForPreviousYear();
            updateJobLog(scheduledJobLog);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ScheduledJobLog createScheduledJobLog(JobEnum jobEnum) {
        ScheduledJobLog scheduledJobLog = new ScheduledJobLog(jobEnum, LogStatusEnum.RUNNING);
        entityService.persist(scheduledJobLog);
        scheduledJobLog.setStart();
        logInfo("Running " + jobEnum);
        return scheduledJobLog;
    }

    @Schedule(minute = "10", persistent = false)
    @Asynchronous
    public void deleteDeletableOffers() {
        if (getConfiguration().isDeleteDeletableOffersJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.DeleteDeletableOffers);
            scheduledJobLog.appendLog(offerService.deleteDeletableOffers());
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.DeleteDeletableOffers);
        }
    }

    @Schedule(hour = "2", minute = "10", persistent = false)
    @Asynchronous
    public void deleteDeletableUsers() {
        if (getConfiguration().isDeleteDeletableUsersJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.DeleteDeletableUsers);
            scheduledJobLog.appendLog(userService.deleteDeletableUsers());
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.DeleteDeletableUsers);
        }
    }

    @Schedule(hour = "2", minute = "25", persistent = false)
    @Asynchronous
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteEmptyWorkunits() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.DeleteEmptyWorkunits);
        scheduledJobLog.appendLog(workunitService.deleteEmptyWorkunits());
        updateJobLog(scheduledJobLog);
    }

    @Schedule(hour = "2", minute = "20", persistent = false)
    @Asynchronous
    public void deleteExpiredMetadataFiles() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.DeleteExpiredMetadataFiles);
        List<Container> containerList = entityService.createNamedQuery("Container.findAll").getResultList();
        if (!containerList.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            Date date = calendar.getTime();
            for (Container container : containerList) {
                File downloadFolder = new File(RepositoryHelper.getLocalStorage(false).getBasePath() + container.getRelativeRepositoryPath() + File.separator
                    + Messages.get("configureDownloadMetadataDirectory"));
                if (downloadFolder.exists() && downloadFolder.isDirectory()) {
                    File[] files = downloadFolder.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.lastModified() < date.getTime()) {
                                boolean ret = file.delete();
                                if (!ret) {
                                    logger.warning("Could not remove the file " + file.getAbsolutePath() + " from the " + Messages.get("configureDownloadMetadataDirectory") + " folder.");
                                }
                            }
                        }
                    }
                }
            }
        }
        updateJobLog(scheduledJobLog);
    }

    // Note: Currently there no need to run the deleteExpiredShibbolethMappings job! IMPORTANT: DO NOT REMOVE this method since it will be necessary again when Shibboleth access is reactivated.
    // @Schedule(hour = "2", minute = "30", persistent = false)
    @Asynchronous
    public void deleteExpiredShibbolethMappings() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.DeleteExpiredShibbolethMappings);
        // Get the list of users with expired Shibboleth mappings.
        List<User> users = entityService.createNamedQuery("User.findByExpiredShibbolethMappings").getResultList();
        if (!users.isEmpty()) {
            scheduledJobLog.appendLog(CollectionHelper.printIds(users));
        }
        // Remove expired Shibboleth mappings.
        entityService.createNativeQuery("update user_ set shibbolethId = null where shibbolethLastLoginDate < current_date - 180").executeUpdate();
        updateJobLog(scheduledJobLog);
    }

    @Schedule(hour = "2", minute = "15", persistent = false)
    @Asynchronous
    public void deleteLocalImportResources() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.DeleteLocalImportResources);
        resourceService.deleteLocalImportResources();
        updateJobLog(scheduledJobLog);
    }

    @Schedule(hour = "2", minute = "40", persistent = false)
    @Asynchronous
    public void deleteUnassignedObjects() {
        if (getConfiguration().isDeleteUnassignedObjectsJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.DeleteUnassignedObjects);
            scheduledJobLog.appendLog(scheduledJobLog.logDeletedObjects(unassignedObjectsService.deleteUnassignedObjects()));
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.DeleteUnassignedObjects);
        }
    }

    @Schedule(hour = "*/4", minute = "45", persistent = false)
    @Asynchronous
    public void deleteUnusedIndexFiles() throws IOException {
        if (getConfiguration().isReindexJobEnabled()) {
            final IndexWriter indexWriter = IndexerEnum.ALL.getIndexer().getIndexWriter();
            indexWriter.deleteUnusedFiles();
            indexWriter.close();
        } else {
            logInfo("Disabled deleteUnusedIndexFiles");
        }
    }

    public Configuration getConfiguration() {
        return confManager.getConfiguration();
    }

    public boolean isMailEnabled(Configuration configuration) {
        if (configuration != null && configuration.isMailEnabled()) {
            return true;
        }
        logInfo("Mail disabled: cannot execute timer bean reminder jobs!");
        return false;
    }

    public void logInfo(String info) {
        logger.info(info + " " + LocalDateTime.now().withNano(0));
    }

    @Schedule(hour = "3", persistent = false)
    @Asynchronous
    public void refreshMaterializedViews() {
        if (getConfiguration().isRefreshMaterializedViewsJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.RefreshMaterializedViews);
            statisticsService.refreshMaterializedViews();
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.RefreshMaterializedViews);
        }
    }

    @Schedule(hour = "4", persistent = false)
    @Asynchronous
    public void reindex() {
        if (getConfiguration().isReindexJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.Reindex);
            IndexerEnum.ALL.getIndexer().indexClasses();
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.Reindex);
        }
    }

    @Schedule(hour = "1", minute = "40", persistent = false)
    @Asynchronous
    public void remindAccessCardExpiry() {
        if (isMailEnabled(getConfiguration())) {
            if (getConfiguration().isAccessCardExpiryReminderJobEnabled()) {
                ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.RemindAccessCardExpiry);
                List<User> users = entityService.createNamedQuery("User.findByAccessCardExpiring").getResultList();
                if (!users.isEmpty()) {
                    scheduledJobLog.appendLog(CollectionHelper.printIds(users));
                    for (User user : users) {
                        sendMailAccessCardExpiryReminder(user);
                    }
                }
                updateJobLog(scheduledJobLog);
            } else {
                logInfo(DISABLED + JobEnum.RemindAccessCardExpiry);
            }
        }
    }

    @Schedule(hour = "1", persistent = false)
    @Asynchronous
    public void remindContractExpiry() {
        if (isMailEnabled(getConfiguration())) {
            if (getConfiguration().isContractExpiryReminderJobEnabled()) {
                ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.RemindContractExpiry);
                // Query to get all non-obsolete contracts that are expired or expiring in exactly 3, 2, and 1 month(s).
                List<Contract> contracts = entityService.createNamedQuery("Contract.findExpiring").getResultList();
                if (!contracts.isEmpty()) {
                    scheduledJobLog.appendLog("1: " + CollectionHelper.printIds(contracts));
                    for (Contract contract : contracts) {
                        sendMailContractExpiryReminder(contract);
                    }
                }
                // Set status to 'expired' if the expiryDate has passed.
                contracts = entityService.createNamedQuery("Contract.findExpired").getResultList();
                if (!contracts.isEmpty()) {
                    scheduledJobLog.appendLog("2: " + CollectionHelper.printIds(contracts));
                    for (Contract contract : contracts) {
                        contract.resetStatus();
                        entityService.update(contract);
                    }
                }
                updateJobLog(scheduledJobLog);
            } else {
                logInfo(DISABLED + JobEnum.RemindContractExpiry);
            }
        }
    }

    @Schedule(hour = "1", minute = "10", persistent = false)
    @Asynchronous
    public void remindInstrumentReservation() {
        if (isMailEnabled(getConfiguration()) && getConfiguration().isInstrumentReservationEnabled()) {
            if (getConfiguration().isInstrumentReservationReminderJobEnabled()) {
                ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.RemindInstrumentReservation);
                List<InstrumentReservation> reservationsToBeReminded = instrumentReservationService.getInstrumentReservationsToBeReminded();
                if (!reservationsToBeReminded.isEmpty()) {
                    scheduledJobLog.appendLog(CollectionHelper.printIds(reservationsToBeReminded));
                    sendMailInstrumentReservationReminder(reservationsToBeReminded);
                }
                updateJobLog(scheduledJobLog);
            } else {
                logInfo(DISABLED + JobEnum.RemindInstrumentReservation);
            }
        }
    }

    @Schedule(hour = "1", minute = "20", persistent = false)
    @Asynchronous
    public void remindPendingExtensionReport() {
        if (isMailEnabled(getConfiguration())) {
            if (getConfiguration().isExtensionReportReminderJobEnabled()) {
                ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.RemindExtensionReport);
                // Remind that the first extension report is pending.
                List<Project> projects = entityService.createNamedQuery("Project.findByFirstExtensionReportPending").setParameter("startDateWeek", LocalDate.now().minusYears(1).minusDays(7))
                    .setParameter("startDate", LocalDate.now().minusYears(1)).getResultList();
                if (!projects.isEmpty()) {
                    scheduledJobLog.appendLog("1: " + CollectionHelper.printIds(projects));
                    for (Project project : projects) {
                        sendMailProjectReportReminder(project, "First");
                        project.setExtensionReport1ReminderSent(true);
                        entityService.update(project);
                    }
                }
                // Remind that the second extension report is pending.
                projects = entityService.createNamedQuery("Project.findBySecondExtensionReportPending").setParameter("startDateWeek", LocalDate.now().minusYears(2).minusDays(7))
                    .setParameter("startDate", LocalDate.now().minusYears(2)).getResultList();
                if (!projects.isEmpty()) {
                    scheduledJobLog.appendLog("2: " + CollectionHelper.printIds(projects));
                    for (Project project : projects) {
                        sendMailProjectReportReminder(project, "Second");
                        project.setExtensionReport2ReminderSent(true);
                        entityService.update(project);
                    }

                }
                // Remind that the third extension report is pending.
                projects = entityService.createNamedQuery("Project.findByFinalExtensionReportPending").setParameter("startDateWeek", LocalDate.now().minusYears(3).minusDays(7))
                    .setParameter("startDate", LocalDate.now().minusYears(3)).getResultList();
                if (!projects.isEmpty()) {
                    scheduledJobLog.appendLog("3: " + CollectionHelper.printIds(projects));
                    for (Project project : projects) {
                        sendMailProjectReportReminder(project, "Final");
                        project.setExtensionReport3ReminderSent(true);
                        entityService.update(project);
                    }
                }
                updateJobLog(scheduledJobLog);
            } else {
                logInfo(DISABLED + JobEnum.RemindExtensionReport);
            }
        }
    }

    @Schedule(hour = "1", minute = "30", persistent = false)
    @Asynchronous
    public void remindPendingOrders() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.RemindPendingOrders);
        List<Order> pendingOrdersToBeNotified = orderService.getPendingOrdersToBeReminded();
        if (!pendingOrdersToBeNotified.isEmpty()) {
            scheduledJobLog.appendLog(CollectionHelper.printIds(pendingOrdersToBeNotified));
            for (Order order : pendingOrdersToBeNotified) {
                sendMailOrderPendingWarning(order);
            }
        }
        updateJobLog(scheduledJobLog);
    }

    @Schedule(hour = "3", minute = "20", persistent = false)
    @Asynchronous
    public void resetArchiveExpirationDatePassed() {
        ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.ResetArchiveExpirationDatePassed);
        scheduledJobLog.appendLog(resourceService.resetArchiveExpirationDatePassed());
        updateJobLog(scheduledJobLog);
    }

    // @Schedule(hour = "*/12", persistent = false)
    // @Asynchronous
    // @Transactional
    public void resetUserAvailable() {
        if (getConfiguration().isResetUserAvailableJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.ResetUserAvailable);
            userService.setUserAvailable();
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.ResetUserAvailable);
        }
    }

    @Schedule(persistent = false)
    @Asynchronous
    public void resetUserAvailableAM() {
        if (getConfiguration().isResetUserAvailableJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.ResetUserAvailable);
            userService.setUserAvailableAM();
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.ResetUserAvailable);
        }
    }

    @Schedule(hour = "12", persistent = false)
    @Asynchronous
    public void resetUserAvailablePM() {
        if (getConfiguration().isResetUserAvailableJobEnabled()) {
            ScheduledJobLog scheduledJobLog = createScheduledJobLog(JobEnum.ResetUserAvailable);
            userService.setUserAvailablePM();
            updateJobLog(scheduledJobLog);
        } else {
            logInfo(DISABLED + JobEnum.ResetUserAvailable);
        }
    }

    @Schedule(month = "1", dayOfMonth = "1", hour = "1", persistent = false)
    @Asynchronous
    public void scheduledJobsYearly() {
        closeAgendaYear();
    }

    public void sendMailAccessCardExpiryReminder(User user) {
        Mail mail = new Mail();
        mail.setParent(user);
        mail.setType(MailTypeEnum.ACCESSCARD_EXPIRY_REMINDER);
        mail.setRecipient(user);
        mailSendService.send(mail);
    }

    public void sendMailContractExpiryReminder(Contract contract) {
        Mail mail = new Mail();
        mail.setParent(contract);
        mail.setRecipient(contract.getSupervisor());
        mail.setType(MailTypeEnum.CONTRACT_EXPIRY_REMINDER);
        mail.setInput("contract", contract);
        mailSendService.send(mail);
    }

    public void sendMailInstrumentReservationReminder(List<InstrumentReservation> instrumentReservations) {
        for (InstrumentReservation instrumentReservation : instrumentReservations) {
            Mail mail = new Mail();
            mail.setParent(instrumentReservation.getRemindableUser());
            mail.setRecipient(instrumentReservation.getRemindableUser());
            mail.setType(MailTypeEnum.INSTRUMENT_RESERVATION_REMINDER);
            mail.setInput("instrumentReservation", instrumentReservation);
            mailSendService.send(mail);
        }
    }

    public void sendMailOrderCanceled(Order order) {
        if (order != null) {
            Mail mail = new Mail();
            mail.setParent(order);
            mail.setType(MailTypeEnum.ORDER_CANCELED);
            mail.setRecipient(order.getRequester());
            mail.addRecipient(order.getContact());
            mail.setInput("parent", order);
            mailSendService.send(mail);
        }
    }

    public void sendMailOrderPendingWarning(Order order) {
        if (order != null) {
            Mail mail = new Mail();
            mail.setParent(order);
            mail.setType(MailTypeEnum.ORDER_PENDING_REMINDER);
            mail.setRecipient(order.getRequester());
            mail.addRecipient(order.getContact());
            mail.setInput("parent", order);
            mailSendService.send(mail);
        }
    }

    public void sendMailProjectReportReminder(Project project, String reportYear) {
        Mail mail = new Mail();
        mail.setParent(project);
        mail.setRecipient(project.getContact());
        mail.setType(MailTypeEnum.CONTAINER_REPORT_REMINDER, "Project " + project.getId());
        mail.setInput("project", project);
        mail.getMailHelper().setReportYear(reportYear);
        mailSendService.send(mail);
    }

    public void sendMailWebUrlNotFound(Application application) {
        Mail mail = new Mail();
        mail.setParent(application);
        mail.setType(MailTypeEnum.WEB_URL_NOT_FOUND);
        mail.setRecipient(application.getSupervisor());
        mailSendService.send(mail);
    }

    public void triggerJob(JobEnum jobEnum) {
        if (jobEnum != null) {
            if (jobEnum.equals(JobEnum.CancelPendingOrders)) {
                cancelPendingOrders();
            } else if (jobEnum.equals(JobEnum.CheckComputerLoginValidity)) {
                checkComputerLoginValidity();
            } else if (jobEnum.equals(JobEnum.CheckLinkValidity)) {
                checkLinkValidity();
            } else if (jobEnum.equals(JobEnum.CheckOfferValidityDuration)) {
                checkOfferValidityDuration();
            } else if (jobEnum.equals(JobEnum.CheckWebUrlValidity)) {
                checkWebUrlValidity();
            } else if (jobEnum.equals(JobEnum.CloseAgendaYear)) {
                closeAgendaYear();
            } else if (jobEnum.equals(JobEnum.DeleteDeletableOffers)) {
                deleteDeletableOffers();
            } else if (jobEnum.equals(JobEnum.DeleteExpiredMetadataFiles)) {
                deleteExpiredMetadataFiles();
            } else if (jobEnum.equals(JobEnum.DeleteExpiredShibbolethMappings)) {
                deleteExpiredShibbolethMappings();
            } else if (jobEnum.equals(JobEnum.DeleteUnassignedObjects)) {
                deleteUnassignedObjects();
            } else if (jobEnum.equals(JobEnum.DeleteDeletableUsers)) {
                deleteDeletableUsers();
            } else if (jobEnum.equals(JobEnum.DeleteLocalImportResources)) {
                deleteLocalImportResources();
            } else if (jobEnum.equals(JobEnum.RefreshMaterializedViews)) {
                refreshMaterializedViews();
            } else if (jobEnum.equals(JobEnum.RemindContractExpiry)) {
                remindContractExpiry();
            } else if (jobEnum.equals(JobEnum.RemindExtensionReport)) {
                remindPendingExtensionReport();
            } else if (jobEnum.equals(JobEnum.RemindPendingOrders)) {
                remindPendingOrders();
            } else if (jobEnum.equals(JobEnum.RemindInstrumentReservation)) {
                remindInstrumentReservation();
            } else if (jobEnum.equals(JobEnum.RemindAccessCardExpiry)) {
                remindAccessCardExpiry();
            } else if (jobEnum.equals(JobEnum.ResetArchiveExpirationDatePassed)) {
                resetArchiveExpirationDatePassed();
            } else if (jobEnum.equals(JobEnum.ResetUserAvailable)) {
                resetUserAvailable();
            } else if (jobEnum.equals(JobEnum.Reindex)) {
                reindex();
            }
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateJobLog(ScheduledJobLog scheduledJobLog) {
        scheduledJobLog.setDone();
        entityService.update(scheduledJobLog);
        logInfo("Finished " + scheduledJobLog.getCreatedBy());
    }
}