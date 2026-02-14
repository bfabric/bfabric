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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

public enum SampleStatusEnum {

    CANCELLED_REQUESTED_BY_USER(
        "Cancelled, requested by user", true),
    PROCEED_AGREED_BY_USER(
        "Proceed, agreed by user", true),
    ON_HOLD_REQUESTED_BY_USER(
        "On hold, requested by user", true),
    ON_HOLD_WAITING_FOR_MORE_MATERIAL(
        "On hold, waiting for more material", true),
    ON_HOLD_WAITING_FOR_REPLACEMENT_SAMPLES(
        "On hold, waiting for replacement samples", true),
    ON_HOLD_WAITING_FOR_USER_FEEDBACK(
        "On hold, waiting for user feedback", true),
    WILL_BE_REPEATED(
        "Will be repeated", true),
    REPLACEMENT_PROVIDED(
        "Replacement provided", true);

    private final String label;

    private final boolean enabled;

    SampleStatusEnum(String label, boolean enabled) {
        this.label = label;
        this.enabled = enabled;
    }

    public static List<SampleStatusEnum> getEnabledValues() {
        return Arrays.stream(values()).filter(SampleStatusEnum::isEnabled).collect(Collectors.toList());
    }

    public static SampleStatusEnum getSampleStatusEnumByLabel(String label) {
        return getSampleStatusEnumByLabel(label, true);
    }

    public static SampleStatusEnum getSampleStatusEnumByLabel(String label, boolean caseSensitive) {
        SampleStatusEnum ret = null;
        if (StringHelper.isNotEmpty(label)) {
            for (SampleStatusEnum attributeEnum : values()) {
                if ((caseSensitive ? attributeEnum.getLabel() : attributeEnum.getLabel().toLowerCase()).equals(caseSensitive ? label : label.toLowerCase())) {
                    ret = attributeEnum;
                    break;
                }
            }
        }
        return ret;
    }

    public static SampleStatusEnum value(String name) throws InvalidEnumValueException {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException iae) {
            throw new InvalidEnumValueException("type", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public static SampleStatusEnum valueByLabel(String label) throws InvalidEnumValueException {
        SampleStatusEnum sampleStatusEnum = getSampleStatusEnumByLabel(label);
        if (sampleStatusEnum == null) {
            throw new InvalidEnumValueException("type", label, CollectionHelper
                .print(Arrays.asList(Arrays.stream(values()).map(SampleStatusEnum::getLabel).toArray(String[]::new))));
        }
        return value(sampleStatusEnum.name());
    }

    public static SampleStatusEnum valueByLabel(String label, boolean caseSensitive) throws InvalidEnumValueException {
        SampleStatusEnum sampleStatusEnum = getSampleStatusEnumByLabel(label, caseSensitive);
        if (sampleStatusEnum == null) {
            throw new InvalidEnumValueException("type", label, CollectionHelper
                .print(Arrays.asList(Arrays.stream(values()).map(SampleStatusEnum::getLabel).toArray(String[]::new))));
        }
        return value(sampleStatusEnum.name());
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return StringHelper.firstUpper(super.toString().toLowerCase());
    }
}