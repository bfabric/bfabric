package org.bfabric.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bfabric.Messages;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;

public enum ResourceStatusEnum {

    ARCHIVED,
    ARCHIVING,
    AVAILABLE,
    DELETED,
    EXPIRED,
    FAILED,
    INVALID,
    PENDING;

    public static ResourceStatusEnum get(StatusEnum statusEnum) {
        try {
            return valueOf(statusEnum.getLabel().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> getValidEnumValues() {
        List<String> validEnumValues = new ArrayList<>();
        for (WorkunitStatusEnum workunitStatusEnum : WorkunitStatusEnum.values()) {
            if (!workunitStatusEnum.equals(WorkunitStatusEnum.INVALID)) {
                validEnumValues.add(workunitStatusEnum.toString());
            }
        }
        return validEnumValues;
    }

    public static ResourceStatusEnum value(String name) throws InvalidEnumValueException {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new InvalidEnumValueException("status", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public String getHint() {
        try {
            return Messages.get(getLabel() + "ResourceStatusEnumHint");
        } catch (Exception e) {
            return getLabel();
        }
    }

    public String getLabel() {
        return name().toLowerCase();
    }
}