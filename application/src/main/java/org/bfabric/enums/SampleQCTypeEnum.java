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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bfabric.Constants;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

public enum SampleQCTypeEnum {

    AGILENT_BIOANALYZER(
        "Agilent Bioanalyzer", true),
    AGILENT_FEMTO_PULSE(
        "Agilent Femto Pulse", true),
    AGILENT_FRAGMENT_ANALYZER(
        "Agilent Fragment Analyzer", true),
    AGILENT_TAPESTATION(
        "Agilent TapeStation", true),
    GLOMAX_QUANTIFLUOR(
        "GloMax QuantiFluor", true),
    LCMS_CONTROL(
        "LCMS Control", true),
    NANODROP(
        "Nanodrop", true),
    QPCR(
        "qPCR", true),
    QUBIT_FLUOROMETRIC_QUANTIFICATION(
        "Qubit Fluorometric Quantification", true),
    SEQUENCING_CONTROL(
        "Sequencing Control", true),
    SINGLE_CELL_VISUAL_ASSESSMENT_AND_COUNTING(
        "Single Cell Visual Assessment and Counting", true
    );

    private final boolean enabled;

    private final String label;

    @SuppressWarnings("SameParameterValue")
    SampleQCTypeEnum(String label, boolean enabled) {
        this.label = label;
        this.enabled = enabled;
    }

    public static List<SampleQCTypeEnum> getEnabledValues() {
        return Arrays.stream(values()).filter(SampleQCTypeEnum::isEnabled).collect(Collectors.toList());
    }

    public static SampleQCTypeEnum getSampleQCTypeEnumByLabel(String label) {
        return getSampleQCTypeEnumByLabel(label, true);
    }

    public static SampleQCTypeEnum getSampleQCTypeEnumByLabel(String label, boolean caseSensitive) {
        SampleQCTypeEnum ret = null;
        if (StringHelper.isNotEmpty(label)) {
            for (SampleQCTypeEnum attributeEnum : values()) {
                if ((caseSensitive ? attributeEnum.getLabel() : attributeEnum.getLabel().toLowerCase()).equals(caseSensitive ? label : label.toLowerCase())) {
                    ret = attributeEnum;
                    break;
                }
            }
        }
        return ret;
    }

    public static List<Set<SampleQCTypeEnum>> getSampleQCTypeEnumGroupsOrderedForView(String view) {
        List<Set<SampleQCTypeEnum>> grouped = new ArrayList<>();
        if (Constants.QC_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            // Groups ordered by importance: Qubit, GloMax, Nanodrop, Agilent TapeStation / Fragment Analyzer / Bioanalyzer / Femto Pulse, qPCR, Sequencing Control, Single Cell
            grouped.add(Stream.of(QUBIT_FLUOROMETRIC_QUANTIFICATION).collect(Collectors.toSet()));
            grouped.add(Stream.of(NANODROP).collect(Collectors.toSet()));
            grouped.add(Stream.of(GLOMAX_QUANTIFLUOR).collect(Collectors.toSet()));
            grouped.add(Stream.of(AGILENT_BIOANALYZER, AGILENT_FEMTO_PULSE, AGILENT_FRAGMENT_ANALYZER, AGILENT_TAPESTATION).collect(Collectors.toSet()));
            grouped.add(Stream.of(QPCR).collect(Collectors.toSet()));
            grouped.add(Stream.of(SEQUENCING_CONTROL).collect(Collectors.toSet()));
            grouped.add(Stream.of(SINGLE_CELL_VISUAL_ASSESSMENT_AND_COUNTING).collect(Collectors.toSet()));
        } else {
            grouped.add(new HashSet<>(Arrays.asList(values())));
        }

        return grouped;
    }

    public static SampleQCTypeEnum value(String name) throws InvalidEnumValueException {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException iae) {
            throw new InvalidEnumValueException("type", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public static SampleQCTypeEnum valueByLabel(String label) throws InvalidEnumValueException {
        SampleQCTypeEnum sampleQCTypeEnum = getSampleQCTypeEnumByLabel(label);
        if (sampleQCTypeEnum == null) {
            throw new InvalidEnumValueException("type", label, CollectionHelper.print(Arrays.asList(Arrays.stream(values()).map(SampleQCTypeEnum::getLabel).toArray(String[]::new))));
        }
        return value(sampleQCTypeEnum.name());
    }

    public static SampleQCTypeEnum valueByLabel(String label, boolean caseSensitive) throws InvalidEnumValueException {
        SampleQCTypeEnum sampleQCTypeEnum = getSampleQCTypeEnumByLabel(label, caseSensitive);
        if (sampleQCTypeEnum == null) {
            throw new InvalidEnumValueException("type", label, CollectionHelper.print(Arrays.asList(Arrays.stream(values()).map(SampleQCTypeEnum::getLabel).toArray(String[]::new))));
        }
        return value(sampleQCTypeEnum.name());
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