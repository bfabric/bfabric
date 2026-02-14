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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

public enum SampleFormEnum {

    BEADS_REQUIRED(
        "Beads (please specify)",
        true,
        Arrays.asList(SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS)),
    BEADS_OPTIONAL(
        "Beads",
        false,
        Arrays.asList(SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS)),
    DRY(
        "Dry",
        true,
        Arrays.asList(SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION)),
    GEL_BAND_REQUIRED(
        "Gel Band (please specify MW)",
        true,
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_SERVICES)),
    GEL_BAND(
        "Gel Band",
        true,
        Collections.singletonList(SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION)),
    GEL_LANE(
        "Gel Lane (6 - 9 bands)",
        false,
        Collections.emptyList()),
    SOLUTION(
        "Solution",
        true,
        Arrays.asList(SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION)),
    PVDF(
        "PVDF",
        false,
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_SERVICES)),
    CELL_PELLET(
        "Cell Pellet",
        true,
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_SERVICES)),
    TISSUE(
        "Tissue",
        true,
        Arrays.asList(SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.SPATIAL_MS)),
    ORGANOID(
        "Organoid",
        true,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS)),
    BIOFILM(
        "Biofilm",
        true,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS)),
    CELLS(
        "Cells",
        true,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS)),
    TMA(
        "TMA",
        true,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS));

    private final String label;

    private final boolean enabled;

    private final List<SampleTypeEnum> types;

    SampleFormEnum(String label, boolean enabled, List<SampleTypeEnum> types) {
        this.label = label;
        this.enabled = enabled;
        this.types = types;
    }

    public static List<SampleFormEnum> getEnabledValues() {
        return Arrays.stream(values()).filter(SampleFormEnum::isEnabled).collect(Collectors.toList());
    }

    public static List<SampleFormEnum> getEnabledValuesByType(String type) {
        List<SampleFormEnum> enabledValuesByType = Arrays.stream(values()).filter(SampleFormEnum::isEnabled).collect(Collectors.toList());
        enabledValuesByType.removeIf(v -> !v.getTypeNames().contains(type));
        return enabledValuesByType;
    }

    public static SampleFormEnum getSampleFormEnumByLabel(String label) {
        return getSampleFormEnumByLabel(label, true);
    }

    public static SampleFormEnum getSampleFormEnumByLabel(String label, boolean caseSensitive) {
        SampleFormEnum ret = null;
        if (StringHelper.isNotEmpty(label)) {
            for (SampleFormEnum attributeEnum : values()) {
                if ((caseSensitive ? attributeEnum.getLabel() : attributeEnum.getLabel().toLowerCase()).equals(caseSensitive ? label : label.toLowerCase())) {
                    ret = attributeEnum;
                    break;
                }
            }
        }
        return ret;
    }

    public static SampleFormEnum value(String name) throws InvalidEnumValueException {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException iae) {
            throw new InvalidEnumValueException("type", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public static SampleFormEnum valueByLabel(String label, boolean caseSensitive) throws InvalidEnumValueException {
        SampleFormEnum sampleFormEnum = getSampleFormEnumByLabel(label, caseSensitive);
        if (sampleFormEnum == null) {
            throw new InvalidEnumValueException("type", label, CollectionHelper.print(Arrays.asList(Arrays.stream(values()).map(SampleFormEnum::getLabel).toArray(String[]::new))));
        }
        return value(sampleFormEnum.name());
    }

    public static SampleFormEnum valueByLabel(String label) throws InvalidEnumValueException {
        SampleFormEnum sampleFormEnum = getSampleFormEnumByLabel(label);
        if (sampleFormEnum == null) {
            throw new InvalidEnumValueException("type", label, CollectionHelper.print(Arrays.asList(Arrays.stream(values()).map(SampleFormEnum::getLabel).toArray(String[]::new))));
        }
        return value(sampleFormEnum.name());
    }

    public String getLabel() {
        return label;
    }

    public List<String> getTypeNames() {
        return types.stream().map(SampleTypeEnum::getLabel).collect(Collectors.toList());
    }

    public List<SampleTypeEnum> getTypes() {
        return types;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return StringHelper.firstUpper(super.toString().toLowerCase());
    }
}