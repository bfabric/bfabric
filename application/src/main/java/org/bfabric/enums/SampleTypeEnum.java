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
import java.util.stream.Stream;

import org.bfabric.Constants;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

public enum SampleTypeEnum {

    GENERIC("Biological Sample - Generic"),
    GENOMICS("Biological Sample - Genomics"),
    METABOLOMICS("Biological Sample - Metabolomics"),
    PROTEOMICS_INTERACTION("Biological Sample - Proteomics Interaction"),
    PROTEOMICS_EU("Biological Sample - Proteomics EU"),
    PROTEOMICS_SERVICES("Biological Sample - Proteomics Services"),
    SPATIAL_MS("Biological Sample - Spatial MS"),
    SPATIAL_MS_MALDI("Spatial MS - MALDI"),
    PROTEOMICS_USER_LAB("Biological Sample - Proteomics User Lab"),
    BIOMOLECULES_CHARACTERIZATION("Biological Sample - Biomolecules Characterization"),
    AMINO_ACID_ANALYSIS("Biological Sample - Amino Acid Analysis"),
    GLYCOPROTEIN_ANALYSIS("Biological Sample - Glycoprotein Analysis"),
    SEQUENCING("Biological Sample - Sequencing"),
    SINGLE_CELL_SEQUENCING("Biological Sample - Single Cell Sequencing"),
    TRANSCRIPTOMICS("Biological Sample - Transcriptomics"),
    RNA_DNA("RNA/DNA Sample"),
    SINGLE_CELLS("Single Cells Sample"),
    CHEMICAL("Chemical Sample"),
    CLINICAL("Clinical Sample"),
    ENVIRONMENTAL("Environmental Sample"),
    SCINET_HUMAN_PATIENT("SCI-NET Human Patient"),
    SCINET_RODENT_MODEL("SCI-NET Rodent Model"),
    SURFACE("Surface"),
    SYNTHETICBIO("Synthetic Biomolecule"),
    SYNTHETICSMALL("Synthetic Small Molecule"),
    QUALITY_CONTROL("Quality Control Sample"),
    CDNA("cDNA"),
    DIGESTED_SAMPLE("Digested Sample"),
    MS_SAMPLE("MS Sample"),
    USER_LIBRARY_IN_POOL("User Library in Pool"),
    CONTROL_SAMPLE("Control Sample"),
    OFF_TARGET_IDENTIFICATION("Off Target Identification"),
    CELL_ENGINEERING("Cell Engineering"),
    CRISPR_SCREEN("CRISPR Screen"),
    SPATIAL_VIS("Biological Sample - Spatial VIS"),
    ILLUMINA_LIBRARY("Library - Illumina"),
    ILLUMINA_LIBRARY_ON_RUN("Library on Run - Illumina"),
    ILLUMINA_MULTIPLEXED("Library Pooled - Illumina"),
    NANOPORE_LIBRARY_ON_RUN("Library on Run - Nanopore"),
    NANOPORE_LIBRARY("Library - Nanopore"),
    NANOPORE_MULTIPLEXED("Library Pooled - Nanopore"),
    ONT_READY_MADE_LIBRARY_ON_RUN("Library on Run - ONT Ready-Made"),
    ONT_READY_MADE_LIBRARY("Library - ONT Ready-Made"),
    ONT_READY_MADE_MULTIPLEXED("Library Pooled - ONT Ready-Made"),
    PACBIO_LIBRARY("Library - PacBio"),
    PACBIO_LIBRARY_ON_RUN("Library on Run - PacBio"),
    PACBIO_MULTIPLEXED("Library Pooled - PacBio"),
    MS_SAMPLE_LABELED("MS Sample Labeled - Proteomics"),
    MS_SAMPLE_ON_RUN("MS Sample on Run - Proteomics"),
    MS_SAMPLE_MULTIPLEXED("MS Sample Multiplexed - Proteomics");

    private final String label;

    SampleTypeEnum(String label) {
        this.label = label;
    }

    public static List<SampleTypeEnum> getControl() {
        return Stream.of(CONTROL_SAMPLE).collect(Collectors.toList());
    }

    public static List<SampleTypeEnum> getLabelable() {
        return Stream.of(PROTEOMICS_SERVICES).collect(Collectors.toList());
    }

    public static List<SampleTypeEnum> getLabeled() {
        return Stream.of(ILLUMINA_LIBRARY, NANOPORE_LIBRARY, ONT_READY_MADE_LIBRARY, PACBIO_LIBRARY, MS_SAMPLE_LABELED).collect(Collectors.toList());
    }

    public static List<String> getLabeledLabels() {
        return getLabeled().stream().map(SampleTypeEnum::getLabel).collect(Collectors.toList());
    }

    public static SampleTypeEnum getMultiplexParentSampleTypeEnumByLabel(String label) {
        return getMultiplexParentType(getSampleTypeEnumByLabel(label));
    }

    public static SampleTypeEnum getMultiplexParentType(SampleTypeEnum sampleTypeEnum) {
        if (ILLUMINA_MULTIPLEXED.equals(sampleTypeEnum)) {
            return ILLUMINA_LIBRARY;
        }
        if (NANOPORE_MULTIPLEXED.equals(sampleTypeEnum)) {
            return NANOPORE_LIBRARY;
        }
        if (ONT_READY_MADE_MULTIPLEXED.equals(sampleTypeEnum)) {
            return ONT_READY_MADE_LIBRARY;
        }
        if (PACBIO_MULTIPLEXED.equals(sampleTypeEnum)) {
            return PACBIO_LIBRARY;
        }
        if (MS_SAMPLE_MULTIPLEXED.equals(sampleTypeEnum)) {
            return MS_SAMPLE_LABELED;
        }
        return null;
    }

    public static List<SampleTypeEnum> getMultiplexed() {
        return Stream.of(ILLUMINA_MULTIPLEXED, NANOPORE_MULTIPLEXED, ONT_READY_MADE_MULTIPLEXED, PACBIO_MULTIPLEXED, MS_SAMPLE_MULTIPLEXED).collect(Collectors.toList());
    }

    public static List<String> getMultiplexedLabels() {
        return getMultiplexed().stream().map(SampleTypeEnum::getLabel).collect(Collectors.toList());
    }

    public static List<String> getOnRunAndMultiplexedTypeLabels() {
        return Stream.of(ILLUMINA_LIBRARY_ON_RUN, NANOPORE_LIBRARY_ON_RUN, ONT_READY_MADE_LIBRARY_ON_RUN, PACBIO_LIBRARY_ON_RUN, MS_SAMPLE_ON_RUN, ILLUMINA_MULTIPLEXED, NANOPORE_MULTIPLEXED, ONT_READY_MADE_MULTIPLEXED, PACBIO_MULTIPLEXED, MS_SAMPLE_MULTIPLEXED, USER_LIBRARY_IN_POOL)
            .map(SampleTypeEnum::getLabel).collect(Collectors.toList());
    }

    public static List<SampleTypeEnum> getOnRunTypes() {
        return Stream.of(ILLUMINA_LIBRARY_ON_RUN, NANOPORE_LIBRARY_ON_RUN, ONT_READY_MADE_LIBRARY_ON_RUN, PACBIO_LIBRARY_ON_RUN, MS_SAMPLE_ON_RUN).collect(Collectors.toList());
    }

    public static SampleTypeEnum getSampleTypeEnumByLabel(String label) {
        SampleTypeEnum ret = null;
        if (StringHelper.isNotEmpty(label)) {
            for (SampleTypeEnum typeEnum : values()) {
                if (typeEnum.getLabel().equalsIgnoreCase(label)) {
                    ret = typeEnum;
                    break;
                }
            }
        }
        return ret;
    }

    public static SampleTypeEnum getSampleTypeEnumOnRun(String sampleTypeName) {
        SampleTypeEnum sampleTypeEnum = getSampleTypeEnumByLabel(sampleTypeName);
        return sampleTypeEnum != null ? sampleTypeEnum.getSampleTypeEnumOnRun() : null;
    }

    public static boolean isControl(String sampleTypeName) {
        return getControl().contains(getSampleTypeEnumByLabel(sampleTypeName));
    }

    public static boolean isLabelable(SampleTypeEnum sampleTypeEnum) {
        return sampleTypeEnum != null && getLabelable().contains(sampleTypeEnum);
    }

    public static boolean isLabelableBySampleTypeName(String sampleTypeName) {
        return isLabelable(getSampleTypeEnumByLabel(sampleTypeName));
    }

    public static boolean isLabeled(String sampleTypeName) {
        return getLabeled().contains(getSampleTypeEnumByLabel(sampleTypeName));
    }

    public static boolean isMultiplexed(String sampleTypeName) {
        return getMultiplexed().contains(getSampleTypeEnumByLabel(sampleTypeName));
    }

    public static boolean isOnRunType(String sampleTypeName) {
        return getOnRunTypes().contains(getSampleTypeEnumByLabel(sampleTypeName));
    }

    public static boolean requiresMultiplexIdCheck(String label) {
        SampleTypeEnum sampleTypeEnum = getSampleTypeEnumByLabel(label);
        return sampleTypeEnum != null && sampleTypeEnum.getMultiplexIdCheckType() != null;
    }

    public static SampleTypeEnum value(String name) throws InvalidEnumValueException {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException iae) {
            throw new InvalidEnumValueException("type", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public static SampleTypeEnum valueByLabel(String label) throws InvalidEnumValueException {
        SampleTypeEnum sampleTypeEnum = getSampleTypeEnumByLabel(label);
        if (sampleTypeEnum == null) {
            throw new InvalidEnumValueException("type", label, CollectionHelper.print(Arrays.asList(Arrays.stream(values()).map(SampleTypeEnum::getLabel).toArray(String[]::new))));
        }
        return value(sampleTypeEnum.name());
    }

    public String getLabel() {
        return label;
    }

    public String getMultiplexIdCheckType() {
        if (equals(ILLUMINA_LIBRARY) || equals(PACBIO_LIBRARY) || equals(NANOPORE_LIBRARY) || equals(ILLUMINA_MULTIPLEXED) || equals(PACBIO_MULTIPLEXED) || equals(NANOPORE_MULTIPLEXED)) {
            return Constants.MULTIPLEX_ID_CHECK_ADVANCED;
        }
        if (equals(MS_SAMPLE_LABELED) || equals(MS_SAMPLE_MULTIPLEXED)) {
            return Constants.MULTIPLEX_ID_CHECK_BASIC;
        }
        return null;
    }

    public SampleTypeEnum getSampleTypeEnumOnRun() {
        if (this.equals(ILLUMINA_LIBRARY) || this.equals(ILLUMINA_MULTIPLEXED)) {
            return ILLUMINA_LIBRARY_ON_RUN;
        }
        if (this.equals(NANOPORE_LIBRARY) || this.equals(NANOPORE_MULTIPLEXED)) {
            return NANOPORE_LIBRARY_ON_RUN;
        }
        if (this.equals(ONT_READY_MADE_LIBRARY) || this.equals(ONT_READY_MADE_MULTIPLEXED)) {
            return ONT_READY_MADE_LIBRARY_ON_RUN;
        }
        if (this.equals(PACBIO_LIBRARY) || this.equals(PACBIO_MULTIPLEXED)) {
            return PACBIO_LIBRARY_ON_RUN;
        }
        if (this.equals(MS_SAMPLE_LABELED) || this.equals(MS_SAMPLE_MULTIPLEXED)) {
            return MS_SAMPLE_ON_RUN;
        }
        return null;
    }

    public boolean isOfType(String sampleTypeName) {
        return getLabel().equalsIgnoreCase(sampleTypeName);
    }
}