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
import java.util.Collections;
import java.util.List;

import org.bfabric.Messages;
import org.bfabric.entity.Executable;
import org.bfabric.entity.ExternalJob;
import org.bfabric.entity.Job;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Order;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Project;
import org.bfabric.entity.Run;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;

public enum StatusEnum {

    ACCEPTED(
        "accept",
        Order.class),
    ANALYZED(
        "finishAnalyzing",
        Order.class),
    ANALYZING(
        "startAnalyzing",
        Order.class),
    ARRIVED(
        "arrived",
        Order.class),
    AVAILABLE(
        "available",
        Executable.class),
    BUGGY(
        "buggy",
        Executable.class),
    CANCELED(
        "cancel",
        Job.class,
        Run.class,
        Project.class,
        Offer.class,
        Order.class),
    CLOSED(
        "close",
        Order.class),
    DEMULTIPLEXED(
        "finishDemultiplexing",
        Run.class),
    DEMULTIPLEXING(
        "startDemultiplexing",
        Run.class),
    DEMULTIPLEXINGFAILED(
        "demultiplexingFailed",
        Run.class),
    DISABLED(
        "disabled",
        Executable.class),
    DONE(
        "done",
        Job.class,
        ExternalJob.class,
        WorkflowStep.class),
    ENABLED(
        "enabled",
        Executable.class),
    EXPIRED(
        "expired",
        Offer.class),
    FAILED(
        "failed",
        Job.class,
        ExternalJob.class,
        WorkflowStep.class),
    FINISHED(
        "finish",
        Plate.class,
        Run.class,
        Project.class,
        Order.class),
    INVALID(
        null),
    LOCKED(
        "lock",
        Offer.class),
    NEW(
        "new",
        Job.class,
        ExternalJob.class),
    PENDING(
        "pending",
        Executable.class,
        Plate.class,
        Run.class,
        Project.class,
        Offer.class,
        Order.class),
    PRIVATE(
        "privatize",
        Project.class),
    PROCESSED(
        "finishProcessing",
        Order.class,
        Run.class),
    PROCESSING(
        "startProcessing",
        Order.class,
        Plate.class,
        Run.class),
    PROCESSINGFAILED(
        "processingFailed",
        Order.class,
        Plate.class,
        Run.class),
    PUBLISHED(
        "publish",
        Project.class),
    REJECTED(
        "reject",
        Project.class),
    REOPENED(
        "reopen",
        Order.class),
    REVIEW(
        "review",
        Project.class),
    REVISED(
        "revise",
        Order.class),
    REVISIONACCEPTED(
        "acceptRevision",
        Order.class),
    ROLLBACK(
        "rollback"),
    READY(
        "ready",
        Plate.class,
        Run.class),
    RUNNING(
        "running",
        Job.class,
        ExternalJob.class,
        Project.class,
        WorkflowStep.class),
    RESUBMITTED(
        "submit",
        Job.class,
        ExternalJob.class),
    SUBMITTED(
        "submit",
        Job.class,
        ExternalJob.class,
        Order.class);

    public static final List<StatusEnum> NON_BOOKABLE_CONTAINER_STATUS_LIST = Collections.unmodifiableList(Arrays.asList(PENDING, SUBMITTED, PRIVATE, PUBLISHED, CANCELED, REVIEW, REJECTED, CLOSED, INVALID));

    public static final List<StatusEnum> NON_EXTENSIBLE_CONTAINER_STATUS_LIST = Collections.unmodifiableList(Arrays.asList(PENDING, SUBMITTED, PRIVATE, PUBLISHED, CANCELED, REVIEW, REJECTED, CLOSED, INVALID, FINISHED));

    public static final List<StatusEnum> FINAL_SUCCESS_CONTAINER_STATUS_LIST = Collections.unmodifiableList(Arrays.asList(PUBLISHED, CLOSED));

    public static final List<StatusEnum> FINAL_CONTAINER_STATUS_LIST = Collections.unmodifiableList(Arrays.asList(PUBLISHED, CANCELED, REJECTED, CLOSED));

    private final String action;

    private final List<Class<?>> classList;

    StatusEnum(String action, Class<?>... classList) {
        this.action = action;
        this.classList = Arrays.asList(classList);
    }

    public static StatusEnum get(ResourceStatusEnum statusEnum) {
        try {
            return valueOf(statusEnum.getLabel().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static List<StatusEnum> getStatusEnums(Class<?> clazz) {
        List<StatusEnum> ret = new ArrayList<>();
        for (StatusEnum statusEnum : values()) {
            if (statusEnum.getClassList().contains(clazz)) {
                ret.add(statusEnum);
            }
        }
        return ret;
    }

    public static boolean isFinished(StatusEnum status) {
        return DONE.equals(status) || FAILED.equals(status);
    }

    public static StatusEnum value(String name) throws InvalidEnumValueException {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException iae) {
            throw new InvalidEnumValueException("status", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public static StatusEnum value(String name, Class<?> clazz) throws InvalidEnumValueException {
        List<StatusEnum> statusEnums = getStatusEnums(clazz);
        if (!statusEnums.isEmpty()) {
            try {
                StatusEnum statusEnum = name != null ? valueOf(name.toUpperCase()) : null;
                return statusEnum != null && statusEnums.contains(statusEnum) ? statusEnum : null;
            } catch (Exception e) {
                throw new InvalidEnumValueException("status", name, CollectionHelper.print(Collections.singletonList(statusEnums)));
            }
        }
        return null;
    }

    public String getAction() {
        return action;
    }

    public List<Class<?>> getClassList() {
        return classList;
    }

    public String getHint() {
        try {
            return Messages.get(getLabel() + "StatusEnumHint");
        } catch (Exception e) {
            return getLabel();
        }
    }

    public String getLabel() {
        return name().toLowerCase();
    }
}
