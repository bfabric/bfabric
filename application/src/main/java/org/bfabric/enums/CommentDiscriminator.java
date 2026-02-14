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
import java.util.Arrays;
import java.util.List;

import org.bfabric.Constants;
import org.bfabric.entity.AnnotationComment;
import org.bfabric.entity.Comment;
import org.bfabric.entity.ConsumableNote;
import org.bfabric.entity.ContractNote;
import org.bfabric.entity.DatasetComment;
import org.bfabric.entity.InstrumentEventNote;
import org.bfabric.entity.InstrumentNote;
import org.bfabric.entity.InstrumentReservationNote;
import org.bfabric.entity.OfferComment;
import org.bfabric.entity.OrderComment;
import org.bfabric.entity.OrderNote;
import org.bfabric.entity.OrderResult;
import org.bfabric.entity.PlateComment;
import org.bfabric.entity.ProjectComment;
import org.bfabric.entity.PurchaseNote;
import org.bfabric.entity.RunComment;
import org.bfabric.entity.SampleComment;
import org.bfabric.entity.SamplePreparationProtocolNote;
import org.bfabric.entity.ServiceAreaNote;
import org.bfabric.entity.ServiceNote;
import org.bfabric.entity.ServiceTypeNote;
import org.bfabric.entity.WorkflowStepComment;
import org.bfabric.entity.WorkunitComment;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

public enum CommentDiscriminator {

    ANNOTATION_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        AnnotationComment.class,
        null,
        false,
        null,
        false),
    CONSUMABLE_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        ConsumableNote.class,
        null,
        false,
        null,
        false),
    CONTRACT_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        ContractNote.class,
        null,
        false,
        null,
        false),
    DATASET_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        DatasetComment.class,
        Constants.MAIL_TARGET_PROJECT_MEMBERS,
        true,
        Constants.MAIL_TARGET_PROJECT_INTERNALS,
        true),
    INSTRUMENT_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        InstrumentNote.class,
        null,
        false,
        null,
        false),
    INSTRUMENT_EVENT_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        InstrumentEventNote.class,
        null,
        false,
        null,
        false),
    INSTRUMENT_RESERVATION_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        InstrumentReservationNote.class,
        null,
        false,
        null,
        false),
    OFFER_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        OfferComment.class,
        Constants.MAIL_TARGET_REQUESTER,
        true,
        Constants.MAIL_TARGET_COACHES,
        true),
    ORDER_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        OrderComment.class,
        Constants.MAIL_TARGET_ORDER_MEMBERS,
        true,
        Constants.MAIL_TARGET_ORDER_INTERNALS,
        true),
    ORDER_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        OrderNote.class,
        null,
        true,
        null,
        true),
    ORDER_RESULT(
        Constants.COMMENT_CATEGORY_RESULT,
        OrderResult.class,
        Constants.MAIL_TARGET_ORDER_MEMBERS,
        true,
        Constants.MAIL_TARGET_ORDER_INTERNALS,
        true),
    PLATE_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        PlateComment.class,
        null,
        false,
        null,
        false),
    PROJECT_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        ProjectComment.class,
        Constants.MAIL_TARGET_PROJECT_MEMBERS,
        true,
        Constants.MAIL_TARGET_PROJECT_INTERNALS,
        true),
    PURCHASE_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        PurchaseNote.class,
        null,
        false,
        null,
        false),
    RUN_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        RunComment.class,
        null,
        false,
        null,
        false),
    SAMPLE_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        SampleComment.class,
        Constants.MAIL_TARGET_PROJECT_MEMBERS,
        true,
        Constants.MAIL_TARGET_PROJECT_INTERNALS,
        true),
    SAMPLE_PREPARATION_PROTOCOL_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        SamplePreparationProtocolNote.class,
        null,
        false,
        null,
        false),
    SERVICE_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        ServiceNote.class,
        null,
        false,
        null,
        false),
    SERVICEAREA_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        ServiceAreaNote.class,
        null,
        false,
        null,
        false),
    SERVICETYPE_NOTE(
        Constants.COMMENT_CATEGORY_NOTE,
        ServiceTypeNote.class,
        null,
        false,
        null,
        false),
    WORKFLOW_STEP_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        WorkflowStepComment.class,
        null,
        false,
        null,
        false),
    WORKUNIT_COMMENT(
        Constants.COMMENT_CATEGORY_COMMENT,
        WorkunitComment.class,
        Constants.MAIL_TARGET_ORDER_MEMBERS,
        true,
        Constants.MAIL_TARGET_ORDER_INTERNALS,
        true);

    private final String category;

    private final Class<? extends Comment> commentClass;

    private final boolean mailTargetExternalSelect;

    private final boolean mailTargetInternalSelect;

    private final String mailTargetExternal;

    private final String mailTargetInternal;

    CommentDiscriminator(String category, Class<? extends Comment> commentClass, String mailTargetExternal, boolean mailTargetExternalSelect, String mailTargetInternal, boolean mailTargetInternalSelect) {
        this.category = category;
        this.commentClass = commentClass;
        this.mailTargetExternal = mailTargetExternal;
        this.mailTargetExternalSelect = mailTargetExternalSelect;
        this.mailTargetInternal = mailTargetInternal;
        this.mailTargetInternalSelect = mailTargetInternalSelect;
    }

    public static CommentDiscriminator getCommentType(String commentClass) {
        for (CommentDiscriminator CommentDiscriminator : values()) {
            if (StringHelper.isNotEmpty(commentClass) && CommentDiscriminator.getCommentClass().getSimpleName().equals(commentClass)) {
                return CommentDiscriminator;
            }
        }
        return null;
    }

    private static String getLegacyType(CommentDiscriminator CommentDiscriminator) {
        return CommentDiscriminator.toString();
    }

    public static CommentDiscriminator value(String name) throws InvalidEnumValueException {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException iae) {
            for (CommentDiscriminator CommentDiscriminator : values()) {
                if (getLegacyType(CommentDiscriminator).equals(name)) {
                    return CommentDiscriminator;
                }
            }
            throw new InvalidEnumValueException("type", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public String getCategory() {
        return category;
    }

    public Class<? extends Comment> getCommentClass() {
        return commentClass;
    }

    public String getDirectoryPrefix() {
        return category.toLowerCase();
    }

    public String getLabel() {
        return super.toString().toLowerCase().replaceAll("_", "-");
    }

    public String getMailTargetExternal() {
        return mailTargetExternal;
    }

    public List<String> getMailTargetExternalList() {
        return getMailTargetExternal() != null ? new ArrayList<>(Arrays.asList(getMailTargetExternal().trim().split(","))) : null;
    }

    public String getMailTargetInternal() {
        return mailTargetInternal;
    }

    public List<String> getMailTargetInternalList() {
        return getMailTargetInternal() != null ? new ArrayList<>(Arrays.asList(getMailTargetInternal().trim().split(","))) : null;
    }

    public List<String> getMailTargetList(boolean internal) {
        return CollectionHelper.sortObjects(internal ? getMailTargetInternalList() : getMailTargetExternalList());
    }

    public boolean isMailTargetExternalSelect() {
        return mailTargetExternalSelect;
    }

    public boolean isMailTargetInternalSelect() {
        return mailTargetInternalSelect;
    }
}
