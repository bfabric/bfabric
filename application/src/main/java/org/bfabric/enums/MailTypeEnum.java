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

package org.bfabric.enums;

import java.util.ArrayList;
import java.util.List;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Application;
import org.bfabric.entity.Consumable;
import org.bfabric.entity.Container;
import org.bfabric.entity.Contract;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Event;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentEvent;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Job;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Project;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.Run;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.User;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.Workunit;
import org.bfabric.util.CollectionHelper;

public enum MailTypeEnum {
    ACCESSCARD_EXPIRY_REMINDER(
        "UZH Card Expires",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    AGENDA_ABSENCE(
        "Absence",
        Event.class,
        MailSenderEnum.PERSONAL,
        MailRecipientTypeEnum.CC,
        true),
    AGENDA_CLOSE_YEAR(
        "Close Agenda Year",
        User.class,
        MailSenderEnum.COORDINATOR,
        null,
        true),
    ANNOTATION_INTERNAL_COMMENT(
        "Internal Comment",
        Annotation.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    ANNOTATION_COMMENT(
        "Comment",
        Annotation.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    COMMENT_ACKNOWLEDGED(
        "Comment Acknowledged",
        null,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONSUMABLE_NOTE(
        "Note",
        Consumable.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_ACCEPTED(
        "Accepted",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_APPROVE(
        "Approve",
        Container.class,
        MailSenderEnum.COORDINATOR,
        null,
        false),
    CONTAINER_APPROVE_COACH(
        "Approve",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_ARRIVED(
        "Arrived",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_BOOKING_REQUEST(
        "Book Charges",
        Container.class,
        MailSenderEnum.PERSONAL,
        null,
        true),
    CONTAINER_BUDGETOFFICER_ALTER(
        "Budget Officer Changed",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_CANCELED(
        "Canceled",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_CLOSE(
        "Close",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_CLOSE_COACH(
        "Close",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_COACH(
        "Coaching",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_COACH_ALTER(
        "Coach Changed",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_COACH_CHANGED(
        "Coaching Changed",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_COMMENT(
        "Comment",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_CONTACT_ALTER(
        "Contact Changed",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_CREATED(
        "Created",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_CREATED_COACH(
        "Created",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_CUSTOM_STATE(
        "Reached New State",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_EMPLOYEE_COMMENT(
        "Comment",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_EMPLOYEE_INTERNAL_COMMENT(
        "Internal Comment",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_FINISH(
        "Finish",
        Container.class,
        MailSenderEnum.COORDINATOR,
        null,
        false),
    CONTAINER_FINISHED(
        "Finished",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_NOTE(
        "Note",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_INTERNAL_COMMENT(
        "Internal Comment",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_LEADER_ALTER(
        "Leader Changed",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_PILOT(
        "Pilot",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_PILOT_APPROVE(
        "Pilot Approve",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_PILOT_APPROVE_COACH(
        "Pilot Approve",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_PILOT_COACH(
        "Pilot",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_PILOT_COORDINATOR(
        "Pilot",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_PRIVATE(
        "Private",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_PUBLISH(
        "Publish",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_REJECT(
        "Reject",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_REJECT_COACH(
        "Reject",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_REPORT_APPROVE(
        "Report Approve",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_REPORT_REMINDER(
        "Report Request",
        Container.class,
        MailSenderEnum.COORDINATOR,
        null,
        false),
    CONTAINER_REPORT_UPLOAD(
        "Report Upload",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_REQUEST(
        "Request Confirmation",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_REQUEST_COORDINATOR(
        "Requested",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_REQUESTER_ALTER(
        "Requester Changed",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_RESULT(
        "Result",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_REVIEW(
        "Review",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_SUBMITTED(
        "Submitted",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTAINER_USER_COMMENT(
        "Comment",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_USER_DECISION_SUBMITTED(
        "User Decision Submitted",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_WORKUNIT_AVAILABLE(
        "Workunit Available",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    CONTAINER_WORKUNIT_STATUS(
        "Workunit",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTRACT_EXPIRY_REMINDER(
        "Contract Expiry Reminder",
        Contract.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CONTRACT_NOTE(
        "Note",
        Contract.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    CUSTOM(
        "Will be replaced by the custom subject!",
        User.class,
        MailSenderEnum.PERSONAL,
        null,
        false),
    DATASET_INTERNAL_COMMENT(
        "Internal Comment",
        Dataset.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    DATASET_COMMENT(
        "Comment",
        Dataset.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    DOI_REQUESTED(
        "Digital Object Identifier",
        Project.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    FLATRATEDORDER_AGREEMENT(
        "Flat-Rated Order Agreement",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    UNARCHIVE(
        "Unarchive",
        Job.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    UNARCHIVED(
        "Unarchived",
        Job.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    UNARCHIVE_CANCELED(
        "Unarchive Canceled",
        Job.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    UNARCHIVE_FAILED(
        "Unarchive Failed",
        Job.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    INSTRUMENT_EVENT(
        "Instrument Event",
        InstrumentEvent.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    INSTRUMENT_EVENT_NOTE(
        "Note",
        InstrumentEvent.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    INSTRUMENT_NOTE(
        "Note",
        Instrument.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    INSTRUMENT_RESERVATION(
        "Instrument Reservation",
        InstrumentReservation.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    INSTRUMENT_RESERVATION_APPROVAL(
        "Approval",
        InstrumentReservation.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    INSTRUMENT_RESERVATION_NOTE(
        "Note",
        InstrumentReservation.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    INSTRUMENT_RESERVATION_REMINDER(
        "Upcoming Instrument Reservation",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_ADD(
        "Member Add",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_ADD_CONTACT(
        "Member Add",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_ALTER(
        "Member Role Changed",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_REMOVE(
        "Member Remove",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_REMOVE_CONTACT(
        "Member Remove",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_ROLE_DOWNGRADE(
        "Member Role Downgrade",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_ROLE_DOWNGRADE_CONTACT(
        "Member Role Downgrade",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_ROLE_UPGRADE(
        "Member Role Upgrade",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    MEMBER_ROLE_UPGRADE_CONTACT(
        "Member Role Upgrade",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    OFFER(
        "Offer",
        Offer.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    OFFER_COMMENT(
        "Comment",
        Offer.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    OFFER_INTERNAL_COMMENT(
        "Internal Comment",
        Offer.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    OFFER_NOTE(
        "Note",
        Offer.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    ORDER_CANCELED(
        "Order Canceled",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    ORDER_PENDING_REMINDER(
        "Pending Order Reminder: Action Needed!",
        Container.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    PLATE_COMMENT(
        "Comment",
        Plate.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    PLATE_INTERNAL_COMMENT(
        "Internal Comment",
        Plate.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    PURCHASE_CHANGE(
        "Change",
        Purchase.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    PURCHASE_NOTE(
        "Note",
        Purchase.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    RUN_INTERNAL_COMMENT(
        "Internal Comment",
        Run.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    RUN_COMMENT(
        "Comment",
        Run.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    SAMPLE_PREPARATION_PROTOCOL_NOTE(
        "Note",
        SamplePreparationProtocol.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    SAMPLE_INTERNAL_COMMENT(
        "Internal Comment",
        Sample.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    SAMPLE_COMMENT(
        "Comment",
        Sample.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    SERVICE_NOTE(
        "Note",
        Service.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    SERVICEAREA_NOTE(
        "Note",
        ServiceArea.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    SERVICETYPE_NOTE(
        "Note",
        ServiceType.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    SERVICETYPE_COACH_CHANGED(
        "Coaching Changed",
        ServiceType.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    USER_CONFIRMATION(
        "Please confirm your account",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_CONFIRMATION_REQUIRED(
        "Please confirm your email address",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_CREATE(
        "User Registration Confirmation",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_CREATE_ONBEHALF(
        "User Registration Confirmation",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_EXPIRE(
        "Account Expiration",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_KEYREQUEST(
        "Key Request Confirmation",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_KEYREQUEST_MANAGER(
        "Key Request",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_MERGE(
        "User Account Merge Notification",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_MERGE_REQUEST(
        "User Account Merge Request",
        User.class,
        MailSenderEnum.SUPPORT,
        null,
        false),
    USER_PASSWORD_LOST(
        "Password Reset",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_ACCESS(
        "Request Access Confirmation",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_ACCESS_APPROVED(
        "Request Access Approved",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_ACCESS_MANAGER(
        "Request Access",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_ACCESS_REJECTED(
        "Request Access Rejected",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_ACCESS_SENT(
        "Mutation (Webformular)",
        User.class,
        MailSenderEnum.COORDINATOR,
        null,
        false),
    USER_REQUEST_EXTENSION(
        "Request Extension Confirmation",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_EXTENSION_APPROVED(
        "Request Extension Approved",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_EXTENSION_MANAGER(
        "Request Extension",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    USER_REQUEST_EXTENSION_REJECTED(
        "Request Extension Rejected",
        User.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    WEB_URL_NOT_FOUND(
        "Web URL NOT_FOUND",
        Application.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    WORKFLOW_STEP_INTERNAL_COMMENT(
        "Internal Comment",
        WorkflowStep.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    WORKFLOW_STEP_COMMENT(
        "Comment",
        WorkflowStep.class,
        MailSenderEnum.SYSTEM,
        null,
        false),
    WORKUNIT_INTERNAL_COMMENT(
        "Internal Comment",
        Workunit.class,
        MailSenderEnum.SYSTEM,
        null,
        true),
    WORKUNIT_COMMENT(
        "Comment",
        Workunit.class,
        MailSenderEnum.SYSTEM,
        null,
        false);

    /**
     * Attributes. Note that type prerequisites an email template named #{type}-email.ftl.
     */
    private final MailSenderEnum mailSenderEnum;

    private final Class<?> parentClass;

    private final MailRecipientTypeEnum mailRecipientTypeEnum;

    private final String subjectBody;

    private final boolean internal;

    MailTypeEnum(String subjectBody, Class<?> parentClass, MailSenderEnum mailSenderEnum, MailRecipientTypeEnum mailRecipientTypeEnum, boolean internal) {
        this.subjectBody = subjectBody;
        this.parentClass = parentClass;
        this.mailSenderEnum = mailSenderEnum;
        this.mailRecipientTypeEnum = mailRecipientTypeEnum;
        this.internal = internal;
    }

    public static List<MailTypeEnum> getFilterableMailTypesByParentEntity(AbstractBaseEntity entity, boolean internal) {
        Class<?> parentClass = entity != null ? entity.getClass() : null;
        List<MailTypeEnum> mailTypeEnums = new ArrayList<>();
        for (MailTypeEnum mailTypeEnum : values()) {
            if ((parentClass == null || mailTypeEnum.getParentClass() != null && mailTypeEnum.getParentClass().isAssignableFrom(parentClass)) && (internal || !mailTypeEnum.internal)) {
                mailTypeEnums.add(mailTypeEnum);
            }
        }
        return mailTypeEnums;
    }

    public static String getMailTypes(Class<?> parentClass, boolean internal) {
        List<String> mailTypes = new ArrayList<>();
        if (parentClass != null) {
            for (MailTypeEnum mailTypeEnum : values()) {
                if ((mailTypeEnum.getParentClass() == null || mailTypeEnum.getParentClass().isAssignableFrom(parentClass)) && (internal || !mailTypeEnum.internal)) {
                    mailTypes.add(mailTypeEnum.name());
                }
            }
        }
        return CollectionHelper.print(mailTypes, true);
    }

    public static List<MailTypeEnum> getUserMailTypes() {
        List<MailTypeEnum> mailTypeEnums = new ArrayList<>();
        for (MailTypeEnum mailTypeEnum : values()) {
            if (mailTypeEnum.getParentClass() != null && mailTypeEnum.getParentClass().equals(User.class)) {
                mailTypeEnums.add(mailTypeEnum);
            }
        }
        return mailTypeEnums;
    }

    public static MailTypeEnum value(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public MailRecipientTypeEnum getMailRecipientTypeEnum() {
        return mailRecipientTypeEnum;
    }

    public MailSenderEnum getMailSenderEnum() {
        return mailSenderEnum;
    }

    public String getMailTemplateFileName() {
        StringBuilder mailTemplateFileName = new StringBuilder();
        if (name().endsWith("_INTERNAL_COMMENT")) {
            mailTemplateFileName.append("internal-comment");
        } else if (name().endsWith("_COMMENT")) {
            mailTemplateFileName.append("comment");
        } else if (name().endsWith("_NOTE")) {
            mailTemplateFileName.append("note");
        } else if (name().endsWith("_RESULT")) {
            mailTemplateFileName.append("result");
        } else {
            mailTemplateFileName.append(name().toLowerCase().replaceAll("_", "-"));
        }
        return mailTemplateFileName + "-email.ftl";
    }

    public Class<?> getParentClass() {
        return parentClass;
    }

    public String getSubject(String prefix) {
        return getSubject(prefix, Constants.EMPTY_STRING);
    }

    public String getSubject(String prefix, String postfix) {
        StringBuilder subject = new StringBuilder(getSubjectBody());
        if (prefix != null && !prefix.isEmpty()) {
            subject.insert(0, " ");
            subject.insert(0, prefix);
        }
        if (postfix != null && !postfix.isEmpty()) {
            subject.append(" ");
            subject.append(postfix);
        }
        return subject.toString();
    }

    public String getSubjectBody() {
        return subjectBody;
    }
}