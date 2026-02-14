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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Attachment;
import org.bfabric.entity.Department;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Membership;
import org.bfabric.entity.Organization;
import org.bfabric.entity.Project;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.RepositoryHelper;

@Named
@Stateless
public class ProjectService extends AbstractContainerService {

    private static final long serialVersionUID = 1;

    public ProjectService() {
        super(Project.class);
    }

    public Map<String, Set<String>> announceFinish(Project project) {
        project.setFinishAnnouncedDate(LocalDateTime.now());
        project.setIndexDependents(false);
        save(project);

        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("finishAnnouncedDate"));
        // Send mail.
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(project, MailTypeEnum.CONTAINER_FINISH));
        return facesMessages;
    }

    public Map<String, Set<String>> announcePrivate(Project project) {
        project.setPrivateAnnouncedDate(LocalDateTime.now());
        project.setIndexDependents(false);
        save(project);

        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("privateAnnouncedDate"));
        // Send mail.
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(project, MailTypeEnum.CONTAINER_PRIVATE));
        return facesMessages;
    }

    public Map<String, Set<String>> approveExtensionReport(Project project, int extensionReportYear) {
        project.setExtensionReportApproved(extensionReportYear, true);
        save(project);

        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("reportApproved"));
        // Send mail.
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(project, MailTypeEnum.CONTAINER_REPORT_APPROVE));
        return facesMessages;
    }

    public Map<String, Set<String>> finish(Project project) {
        project.changeStatus(StatusEnum.FINISHED);
        project.setIndexDependents(false);
        save(project);
        return createDisplayFacesMessagesMap(Messages.get("project") + " " + StatusEnum.FINISHED.getLabel());
    }

    public List<Project> getAcceptedReadableProjectsFiltered(String filterString) {
        List<Project> projects = new ArrayList<>();
        for (Project project : getProjects(filterString)) {
            if (project.isReadable() && project.isAccepted()) {
                projects.add(project);
            }
        }
        return projects;
    }

    public BfabricLazyDataModel<Project> getBioinformaticianProjectsLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        if (userId > 0) {
            entityQuery.addWhereClause("entity.bioinformatician = :bioinformaticianId");
            entityQuery.addParameter("bioinformaticianId", userId);
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getBioinformaticianReassignmentTasks() {
        EntityQuery entityQuery = createEntityQuery();
        List<StatusEnum> status = new ArrayList<>();
        status.add(StatusEnum.PRIVATE);
        status.add(StatusEnum.PUBLISHED);
        status.add(StatusEnum.REJECTED);
        entityQuery.addWhereClause(
            "bioinformatician IS NOT NULL AND status NOT IN (:status) AND NOT EXISTS (SELECT user FROM User user JOIN user.roles role WHERE user = entity.bioinformatician AND role.name = 'employee')");
        entityQuery.addParameter("status", status);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getBookingRequiredTasks(User user, long bookingRequiredTotal) {
        EntityQuery entityQuery = createEntityQuery();

        if (LocalDate.now().withMonth(11).withDayOfMonth(1).isBefore(LocalDate.now())) {
            // After November 1, book all non-booked charges.
            entityQuery.addWhereClause("EXISTS(SELECT id FROM Charge pc WHERE pc.container = entity AND pc.billable = TRUE AND pc.booking is EMPTY)");
        } else {
            // Otherwise, do it quarterly or when bookingRequiredTotal is passed.
            LocalDateTime date = LocalDateTime.now().withMonth(1).withDayOfMonth(1);
            if (LocalDateTime.now().withMonth(4).withDayOfMonth(1).isBefore(LocalDateTime.now())) {
                date = LocalDateTime.now().withMonth(4).withDayOfMonth(1);
            }
            if (LocalDateTime.now().withMonth(7).withDayOfMonth(1).isBefore(LocalDateTime.now())) {
                date = LocalDateTime.now().withMonth(7).withDayOfMonth(1);
            }
            if (LocalDateTime.now().withMonth(10).withDayOfMonth(1).isBefore(LocalDateTime.now())) {
                date = LocalDateTime.now().withMonth(10).withDayOfMonth(1);
            }
            entityQuery.addWhereClause(
                "(EXISTS(SELECT id FROM Charge pc WHERE pc.container = entity AND pc.billable = TRUE AND pc.booking is EMPTY and pc.created < :date) OR (SELECT sum(discountedPrice) FROM Charge pc WHERE pc.container = entity AND pc.billable = TRUE AND pc.booking is EMPTY) > :bookingRequiredTotal)");
            entityQuery.addParameter("bookingRequiredTotal", BigDecimal.valueOf(bookingRequiredTotal));
            entityQuery.addParameter("date", date);
        }

        if (!user.hasRoleImplicit(RoleEnum.BOOKINGMANAGER)) {
            entityQuery.addWhereClause("coach = :coach and " + entityQuery.getWhere());
            entityQuery.addParameter("coach", user);
        }

        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getCoachAssignmentTasks() {
        EntityQuery entityQuery = createEntityQuery();
        List<StatusEnum> status = new ArrayList<>();
        status.add(StatusEnum.PRIVATE);
        status.add(StatusEnum.PUBLISHED);
        status.add(StatusEnum.REJECTED);
        entityQuery.addWhereClause("coach IS NULL AND status NOT IN (:status)");
        entityQuery.addParameter("status", status);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getCoachBackupReassignmentTasks() {
        EntityQuery entityQuery = createEntityQuery();
        List<StatusEnum> status = new ArrayList<>();
        status.add(StatusEnum.PRIVATE);
        status.add(StatusEnum.PUBLISHED);
        status.add(StatusEnum.REJECTED);
        entityQuery.addWhereClause(
            "coachBackup IS NOT NULL AND status NOT IN (:status) AND NOT EXISTS (SELECT user FROM User user JOIN user.roles role WHERE user = entity.coachBackup AND role.name = 'employee')");
        entityQuery.addParameter("status", status);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getCoachReassignmentTasks() {
        EntityQuery entityQuery = createEntityQuery();
        List<StatusEnum> status = new ArrayList<>();
        status.add(StatusEnum.PRIVATE);
        status.add(StatusEnum.PUBLISHED);
        status.add(StatusEnum.REJECTED);
        entityQuery.addWhereClause("coach IS NOT NULL AND status NOT IN (:status) AND NOT EXISTS (SELECT user FROM User user JOIN user.roles role WHERE user = entity.coach AND role.name = 'employee')");
        entityQuery.addParameter("status", status);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<String> getCurrentCustomProjectStatesFiltered(String filterString) {
        return super.getCurrentCustomContainerStatesFiltered(filterString, Constants.PROJECT);
    }

    public BfabricLazyDataModel<Project> getExtensionReportAddTasks(Boolean all, User user) {
        EntityQuery entityQuery = createEntityQuery();
        if (!all) {
            entityQuery.addWhereClause("contact = :contact");
            entityQuery.addParameter("contact", user);
        }
        entityQuery.addWhereClause(
            "status = :status AND ((startDate <= ( CURRENT_DATE - 365 ) AND startDate > ( CURRENT_DATE - 730 ) AND extensionReport1 IS NULL) OR (startDate <= ( CURRENT_DATE - 730 ) AND startDate > ( CURRENT_DATE - 1095 ) AND extensionReport2 IS NULL) OR (startDate <= ( CURRENT_DATE - 1095 ) AND extensionReport3 IS NULL))");
        entityQuery.addParameter("status", StatusEnum.RUNNING);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getExtensionReportApproveTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause(
            "(extensionReport1 IS NOT NULL AND extensionReport1Approved = FALSE) OR (extensionReport2 IS NOT NULL AND extensionReport2Approved = FALSE) OR (extensionReport3 IS NOT NULL AND extensionReport3Approved = FALSE)");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getFormerProjectsLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        if (userId > 0) {
            entityQuery.addWhereClause(
                "EXISTS(SELECT membership.id FROM Membership membership WHERE membership.user.id = :userId AND membership.container = entity and membership.discriminator = :discriminator)");
            entityQuery.addParameter("userId", userId);
            entityQuery.addParameter("discriminator", Membership.DISCRIMINATOR_FORMER);
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getLazyModelByCompanyId(long companyId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.division.company.id = :companyId");
        entityQuery.addParameter("companyId", companyId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getLazyModelByDepartmentId(long departmentId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.institute.department.id = :departmentId");
        entityQuery.addParameter("departmentId", departmentId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getLazyModelByOrganizationId(long organizationId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("entity.institute.department.organization.id = :organizationId");
        entityQuery.addParameter("organizationId", organizationId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    @Override
    public BfabricLazyDataModel<Project> getLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        if (userId > 0) {
            entityQuery.addWhereClause(
                "EXISTS(SELECT membership.id FROM Membership membership WHERE membership.user.id = :userId AND membership.container = entity and membership.discriminator = :discriminator)");
            entityQuery.addParameter("userId", userId);
            entityQuery.addParameter("discriminator", Membership.DISCRIMINATOR_CURRENT);
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Project> getOrderAssignableProjects(String filterString, User user, Set<Project> included) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("status = :running");
        entityQuery.addParameter("running", StatusEnum.RUNNING);
        if (user != null) {
            entityQuery
                .addWhereClause("EXISTS(SELECT membership.id FROM Membership membership WHERE membership.user = :user AND membership.container = entity and membership.discriminator = :discriminator)");
            entityQuery.addParameter("user", user);
            entityQuery.addParameter("discriminator", Membership.DISCRIMINATOR_CURRENT);
        }
        if (included != null && !included.isEmpty()) {
            entityQuery.addWhereClauseDisjunctive("entity in :included");
            entityQuery.addParameter("included", included);
        }
        return (List<Project>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<Project> getProjectCoachTasks(Boolean all, boolean isLoggedIn, User user) {
        BfabricLazyDataModel<Project> lazyDataModel = null;
        if (isLoggedIn) {
            EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("status = :status");
            if (!all) {
                entityQuery.addWhereClause("coach = :coach");
                entityQuery.addParameter("coach", user);
            }
            entityQuery.addParameter("status", StatusEnum.PENDING);
            entityQuery.setOrder("id");
            lazyDataModel = new BfabricLazyDataModel<>(entityQuery);
        }
        return lazyDataModel;
    }

    public BfabricLazyDataModel<Project> getProjectFinalDecisionTasks(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status = :status AND EXISTS (SELECT review FROM Review review WHERE review.project = entity AND review.createdBy = :createdBy)");
        entityQuery.addParameter("createdBy", user.getLogin());
        entityQuery.addParameter("status", StatusEnum.REVIEW);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getProjectFinishAnnounceTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status = :status AND endDate <= CURRENT_DATE AND finishAnnouncedDate IS NULL");
        entityQuery.addParameter("status", StatusEnum.RUNNING);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getProjectFinishingTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status = :status AND endDate <= CURRENT_DATE AND finishAnnouncedDate <= (CURRENT_DATE - 30)");
        entityQuery.addParameter("status", StatusEnum.RUNNING);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getProjectReviewTasks(Boolean all, boolean isLoggedIn, User user) {
        BfabricLazyDataModel<Project> lazyDataModel = null;
        if (isLoggedIn) {
            EntityQuery entityQuery = createEntityQuery();
            String whereClause = "status = :status AND NOT EXISTS (SELECT review FROM Review review WHERE review.project = entity";
            if (!all) {
                entityQuery.addWhereClause(whereClause + " AND review.createdBy = :createdBy)");
                entityQuery.addParameter("createdBy", user.getCreatedBy());
            } else {
                entityQuery.addWhereClause(whereClause + ")");
            }
            entityQuery.addParameter("status", StatusEnum.REVIEW);
            entityQuery.setOrder("id");
            lazyDataModel = new BfabricLazyDataModel<>(entityQuery);
        }
        return lazyDataModel;
    }

    public BfabricLazyDataModel<Project> getProjectSetPrivateAnnounceTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status = :status AND privateAnnouncedDate IS NULL AND finishDate <= (CURRENT_DATE - 180)");
        entityQuery.addParameter("status", StatusEnum.FINISHED);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getProjectSetPrivatePendingReminderTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status = :status AND privateAnnouncedDate <= (CURRENT_DATE - 30) AND reminderDate > (CURRENT_DATE - 180)");
        entityQuery.addParameter("status", StatusEnum.FINISHED);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getProjectSetPrivateReminderTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status = :status AND privateAnnouncedDate <= (CURRENT_DATE - 30) AND reminderDate <= (CURRENT_DATE - 180)");
        entityQuery.addParameter("status", StatusEnum.FINISHED);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Project> getProjectSetPrivateTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status = :status AND privateAnnouncedDate <= (CURRENT_DATE - 30) AND reminderDate IS NULL");
        entityQuery.addParameter("status", StatusEnum.FINISHED);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<StatusEnum> getProjectStatusEnums() {
        return StatusEnum.getStatusEnums(Project.class);
    }

    public List<Project> getProjects(String filterString) {
        return (List<Project>) createEntityQueryFiltered(filterString).getResultList();
    }

    public List<Project> getProjectsByDepartment(Department department) {
        return createNamedQuery("Project.findByDepartment").setParameter("department", department).getResultList();
    }

    public List<Project> getProjectsByOrganization(Organization organization) {
        return createNamedQuery("Project.findByOrganization").setParameter("organization", organization).getResultList();
    }

    public List<Project> getProjectsByUser(String filterString, User user) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        if (user != null) {
            entityQuery.addWhereClause(
                "EXISTS(SELECT membership.id FROM Membership membership WHERE membership.user = :user AND membership.container = entity and membership.discriminator = :discriminator)");
            entityQuery.addParameter("user", user);
            entityQuery.addParameter("discriminator", Membership.DISCRIMINATOR_CURRENT);
        }
        return (List<Project>) entityQuery.getResultList();
    }

    public List<Project> getProjectsDoiCreated() {
        return createNamedQuery("Project.doiCreated").getResultList();
    }

    public List<Project> getProjectsDoiCreatedAfterTimestamp(String timestamp) {
        return createNamedQuery("Project.doiCreatedAfterTimestamp").setParameter("timestamp", timestamp).getResultList();
    }

    public LinkedHashMap<String, String> isDescriptionValid(Project project, BfabricUploadedFile bfabricUploadedFile) {
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();
        if (project.getDescription() == null && bfabricUploadedFile == null) {
            errorMsg.put("edit:description", Messages.get("javax.faces.component.UIInput.REQUIRED"));
        }
        return errorMsg;
    }

    public LinkedHashMap<String, String> isValid(Project project, BfabricUploadedFile bfabricUploadedFile) {
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>(isValidCoaching(project));
        errorMsg.putAll(isValidVatAndReferenceNumber(project));
        errorMsg.putAll(isDescriptionValid(project, bfabricUploadedFile));
        errorMsg.putAll(isValidCustomAttributes(project));
        return errorMsg;
    }

    private Set<String> postPersist(Project project, User currentUser, Set<Mail> mails) {
        Set<String> errorMsg = new HashSet<>();

        // Grant the manager role to the requester, budgetOfficer, leader, contact.
        errorMsg.addAll(addManager(project, find(User.class, project.getRequester().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
        errorMsg.addAll(addManager(project, find(User.class, project.getBudgetOfficer().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
        errorMsg.addAll(addManager(project, find(User.class, project.getLeader().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));
        errorMsg.addAll(addManager(project, find(User.class, project.getContact().getId()), currentUser, mails).get(Constants.ERROR_MESSAGES));

        if (project.isReviewRequired()) {
            mails.add(project.createMail(MailTypeEnum.CONTAINER_REQUEST));
            mails.add(project.createMail(MailTypeEnum.CONTAINER_REQUEST_COORDINATOR));
        } else {
            mails.add(project.createMail(MailTypeEnum.CONTAINER_CREATED));
            if (project.getCoach() != null) {
                mails.add(project.createMail(MailTypeEnum.CONTAINER_CREATED_COACH));
            }
        }

        return errorMsg;
    }

    private Set<String> postUpdate(Project project, User currentUser, Set<Mail> mails) {
        Set<String> errorMsg = super.postUpdate(project, currentUser, mails);

        // If the end date of a project was changed, adapt the status of its members in the authentication database.
        if (project.isEndDateChanged()) {
            userService.addRoleUserAndSynchronizeWithAD(project);
        }

        // Send mails.
        if (project.isCoachChanged() && project.getCoach() != null) {
            if (!project.isAccepted()) {
                // Send mail to review the project.
                mails.add(project.createMail(MailTypeEnum.CONTAINER_COACH));
            } else if (project.isAcceptedButNotPrivateOrPublished()) {
                // Send mail to contact that the coach changed.
                mails.add(project.createMail(MailTypeEnum.CONTAINER_COACH_ALTER));
            }
        }

        if ((project.isCoachChanged() || project.isCoachBackupChanged() || project.isBioinformaticianChanged()) && project.isAcceptedButNotPrivateOrPublished()) {
            // Send mail to inform that coaching has changed.
            mails.add(project.createMail(MailTypeEnum.CONTAINER_COACH_CHANGED));
        }

        if (project.isAcceptedButNotPublished()) {
            if (project.isRequesterChanged()) {
                // Send mail to the contact that the requester changed.
                mails.add(project.createMail(MailTypeEnum.CONTAINER_REQUESTER_ALTER));
            }
            if (project.isContactChanged()) {
                // Send mail to the contact that the contact changed.
                mails.add(project.createMail(MailTypeEnum.CONTAINER_CONTACT_ALTER));
            }
            if (project.isBudgetOfficerChanged()) {
                // Send mail to the contact that the budgetOfficer changed.
                mails.add(project.createMail(MailTypeEnum.CONTAINER_BUDGETOFFICER_ALTER));
            }
            if (project.isLeaderChanged()) {
                // Send mail to the contact that the leader changed.
                mails.add(project.createMail(MailTypeEnum.CONTAINER_LEADER_ALTER));
            }
        }

        return errorMsg;
    }

    public Map<String, Set<String>> privatize(Project project) {
        return setStatusAndSave(project, StatusEnum.PRIVATE);
    }

    public Map<String, Set<String>> publish(Project project) {
        return setStatusAndSave(project, StatusEnum.PUBLISHED);
    }

    public Map<String, Set<String>> publishGrantedDate(Project project) {
        project.setPublishGrantedDate(LocalDateTime.now());
        project.setIndexDependents(true);
        save(project);
        return createDisplayFacesMessagesMap(Messages.get("grantedPublishOption"));
    }

    public Map<String, Set<String>> resetReminder(Project project) {
        project.setReminderDate(LocalDate.now());
        project.setIndexDependents(false);
        save(project);
        return createDisplayFacesMessagesMap(Messages.get("reminderReset"));
    }

    public Set<String> save(Project project, BfabricUploadedFile bfabricUploadedFile, User currentUser) throws IOException {
        boolean isManaged = project.isManaged();

        if (!isManaged) {
            project.setCreateAndAddStatus(project.getStatus());

            if (project.getEndDate() == null) {
                project.setEndDate(project.getStartDate().plusYears(3));
            }

            if (project.isReviewRequired()) {
                project.setCreateAndAddStatus(StatusEnum.PENDING);
            } else {
                project.setCreateAndAddStatus(StatusEnum.RUNNING);
            }
        }
        if (project.getOrganizationType() != null && project.getOrganizationType().isCompany()) {
            project.setDivision(affiliationHelperService.saveDivisionIfNotExists(project.getOrganizationType(), project.getCompanyName(), project.getDivisionName()));
        }

        save(project);

        Set<Mail> mails = new HashSet<>();
        Set<String> errorMsg;
        if (isManaged) {
            errorMsg = postUpdate(project, currentUser, mails);
        } else {
            errorMsg = postPersist(project, currentUser, mails);
        }

        if (bfabricUploadedFile != null) {
            if (!isManaged || !project.getDescription().getFileName().equals(bfabricUploadedFile.getFileName())) {
                if (isManaged) {
                    remove(project.getDescription());
                    project.setDescription(null);
                }
                project.setDescription(new Attachment(project, bfabricUploadedFile, "description"));
                RepositoryHelper.createImport(project.getDescription());
            } else {
                // No need to create a new attachment as the attachment has the same name regardless of its content.
                Files.copy(bfabricUploadedFile.getInputStream(), new File(project.getDescription().getAbsolutePathFM()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            project.getDescription().setAvailable();
            merge(project);
        }

        errorMsg.addAll(mailSendService.sendMails(mails));
        if (!isManaged) {
            setCustomContainerStatusSentMail(project);
        }
        return errorMsg;
    }

    public Map<String, Set<String>> saveReport(Project project, BfabricUploadedFile bfabricUploadedFile, int reportYear) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        try {
            if (bfabricUploadedFile != null) {
                final Attachment oldReport = project.getExtensionReport(reportYear);
                if (oldReport != null) {
                    remove(oldReport);
                }

                project.setExtensionReport(reportYear, new Attachment(project, bfabricUploadedFile, "report" + reportYear));
                final Attachment newReport = project.getExtensionReport(reportYear);
                RepositoryHelper.createImport(newReport);
                newReport.setAvailable();

                save(project);

                facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("reportSaved"));

                // Send mail.
                facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(project, MailTypeEnum.CONTAINER_REPORT_UPLOAD));
            }
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
        return facesMessages;
    }

    public Map<String, Set<String>> setDoi(Project project, User user) {
        project.setDoiCreated(LocalDate.now());
        project.setDoiCreatedBy(user.getLastNameFirstName());
        save(project);

        Map<String, Set<String>> facesMessages = createFacesMessagesMap();
        facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("successfullyCreatedDOI"));

        // Send mail.
        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(project, MailTypeEnum.DOI_REQUESTED));
        return facesMessages;
    }

    public Map<String, Set<String>> setRunning(Project project) {
        return setStatusAndSave(project, StatusEnum.RUNNING);
    }
}