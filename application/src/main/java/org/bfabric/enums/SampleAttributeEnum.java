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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;

import org.apache.commons.text.WordUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.Order;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.service.IdentityService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

public enum SampleAttributeEnum {
    // Important: Please don't change the order of the attributes since the order is relevant! TUBE_ID attribute for all types!
    TUBE_ID(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.values()),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MULTIPLEXED(
        Boolean.FALSE,
        Boolean.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.SEQUENCING, SampleTypeEnum.ILLUMINA_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.MS_SAMPLE_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    FRACTION(
        Boolean.FALSE,
        Boolean.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    QC_PASSED(
        Boolean.FALSE,
        Boolean.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.QUALITY_CONTROL, SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.ILLUMINA_MULTIPLEXED, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.MS_SAMPLE_LABELED, SampleTypeEnum.MS_SAMPLE_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    STATUS(
        Boolean.FALSE,
        SampleStatusEnum.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.QUALITY_CONTROL, SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.MS_SAMPLE_LABELED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    QUALITY_CONTROL_TYPE(
        Boolean.FALSE,
        SampleQCTypeEnum.class,
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MULTIPLEX_KIT(
        Boolean.FALSE,
        MultiplexKit.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.MS_SAMPLE_LABELED, SampleTypeEnum.SPATIAL_MS_MALDI),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MULTIPLEX_ID(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE_LABELED),
        Arrays.asList(SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.SEQUENCING, SampleTypeEnum.SINGLE_CELL_SEQUENCING, SampleTypeEnum.USER_LIBRARY_IN_POOL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MULTIPLEX_ID_DMX(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MULTIPLEX_KIT_2(
        Boolean.FALSE,
        MultiplexKit.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MULTIPLEX_ID_2(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.USER_LIBRARY_IN_POOL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MULTIPLEX_ID_2_DMX(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    BLOCK(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE_LABELED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    EXPRESSION_SYSTEM(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SPECIES(
        Boolean.FALSE,
        Annotation.class,
        Arrays.asList(SampleTypeEnum.METABOLOMICS, SampleTypeEnum.PROTEOMICS_USER_LAB, SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.PROTEOMICS_EU, SampleTypeEnum.SEQUENCING, SampleTypeEnum.SINGLE_CELL_SEQUENCING, SampleTypeEnum.TRANSCRIPTOMICS, SampleTypeEnum.CLINICAL, SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL, SampleTypeEnum.RNA_DNA, SampleTypeEnum.SINGLE_CELLS, SampleTypeEnum.OFF_TARGET_IDENTIFICATION, SampleTypeEnum.CELL_ENGINEERING, SampleTypeEnum.SPATIAL_VIS, SampleTypeEnum.SPATIAL_MS),
        Arrays.asList(SampleTypeEnum.CRISPR_SCREEN, SampleTypeEnum.USER_LIBRARY_IN_POOL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SAMPLE_FORM(
        Boolean.FALSE,
        SampleFormEnum.class,
        Arrays.asList(SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS, SampleTypeEnum.SPATIAL_MS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    AGE(
        Boolean.FALSE,
        BigDecimal.class,
        Arrays.asList(SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL),
        Collections.singletonList(SampleTypeEnum.CLINICAL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    AGE_UNIT(
        Boolean.FALSE,
        String.class,
        Arrays.asList(SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL),
        Collections.singletonList(SampleTypeEnum.CLINICAL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    AMOUNT_ELUTED(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.RNA_DNA, SampleTypeEnum.CDNA),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "ng",
        Boolean.FALSE),
    AMOUNT_INPUT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.DIGESTED_SAMPLE, SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY_ON_RUN, SampleTypeEnum.ONT_READY_MADE_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "ng",
        Boolean.FALSE),
    AMPLICON_SEQUENCE(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    ARRAY_DESIGN_NAME(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.TRANSCRIPTOMICS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    ASIA_SCALE(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.SCINET_HUMAN_PATIENT),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    AVERAGE_SIZE_IN_RANGE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Arrays.asList(SampleQCTypeEnum.AGILENT_BIOANALYZER, SampleQCTypeEnum.AGILENT_TAPESTATION, SampleQCTypeEnum.AGILENT_FEMTO_PULSE, SampleQCTypeEnum.AGILENT_FRAGMENT_ANALYZER),
        "bp",
        Boolean.FALSE),
    BEADS_TYPE(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_INTERACTION),
        Collections.singletonList(SampleFormEnum.BEADS_REQUIRED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    BIAS(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SEQUENCING_CONTROL),
        "%",
        Boolean.FALSE),
    BUFFER(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.SCINET_RODENT_MODEL),
        Arrays.asList(SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS),
        Arrays.asList(SampleFormEnum.SOLUTION, SampleFormEnum.BEADS_REQUIRED, SampleFormEnum.BEADS_OPTIONAL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CELL_COMPARTMENT(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CELL_CONCENTRATION(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.QUALITY_CONTROL, SampleTypeEnum.SINGLE_CELLS, SampleTypeEnum.SINGLE_CELL_SEQUENCING),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SINGLE_CELL_VISUAL_ASSESSMENT_AND_COUNTING),
        "cells/µl",
        Boolean.FALSE),
    CELL_LINE(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CELL_NUMBERS(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "per ml",
        Boolean.FALSE),
    CELL_SIZE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.SINGLE_CELL_SEQUENCING),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CELL_TYPE(
        Boolean.FALSE,
        String.class,
        Arrays.asList(SampleTypeEnum.SINGLE_CELL_SEQUENCING, SampleTypeEnum.SINGLE_CELLS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CELL_VIABILITY(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SINGLE_CELL_VISUAL_ASSESSMENT_AND_COUNTING),
        "%",
        Boolean.FALSE),
    NUMBER_OF_CELLS_LOADED(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SINGLE_CELL_VISUAL_ASSESSMENT_AND_COUNTING),
        null,
        Boolean.FALSE),
    CHEMICAL_MODIFICATIONS(
        Boolean.TRUE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_EU),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    COMPOUND_CLASS(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.METABOLOMICS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CONCENTRATION(
        Boolean.FALSE,
        BigDecimal.class,
        Arrays.asList(SampleTypeEnum.SEQUENCING, SampleTypeEnum.SCINET_RODENT_MODEL, SampleTypeEnum.RNA_DNA, SampleTypeEnum.ONT_READY_MADE_LIBRARY),
        Arrays.asList(SampleTypeEnum.QUALITY_CONTROL, SampleTypeEnum.CDNA, SampleTypeEnum.DIGESTED_SAMPLE, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.OFF_TARGET_IDENTIFICATION, SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Arrays.asList(SampleQCTypeEnum.QUBIT_FLUOROMETRIC_QUANTIFICATION, SampleQCTypeEnum.GLOMAX_QUANTIFLUOR, SampleQCTypeEnum.NANODROP, SampleQCTypeEnum.AGILENT_BIOANALYZER, SampleQCTypeEnum.AGILENT_TAPESTATION, SampleQCTypeEnum.AGILENT_FEMTO_PULSE, SampleQCTypeEnum.AGILENT_FRAGMENT_ANALYZER),
        "ng/µl",
        Boolean.FALSE),
    CONCENTRATION_INPUT_QC(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "ng/µl",
        Boolean.FALSE),
    CONCENTRATION_IN_RANGE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Arrays.asList(SampleQCTypeEnum.AGILENT_BIOANALYZER, SampleQCTypeEnum.AGILENT_TAPESTATION, SampleQCTypeEnum.AGILENT_FEMTO_PULSE, SampleQCTypeEnum.AGILENT_FRAGMENT_ANALYZER),
        "ng/µl",
        Boolean.FALSE),
    CONCENTRATION_LOADING(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.PACBIO_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "pM",
        Boolean.FALSE),
    CONCENTRATION_MOLAR(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.QPCR),
        "nM",
        Boolean.FALSE),
    CONCENTRATION_MOLAR_IN_RANGE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Arrays.asList(SampleQCTypeEnum.AGILENT_BIOANALYZER, SampleQCTypeEnum.AGILENT_TAPESTATION, SampleQCTypeEnum.AGILENT_FEMTO_PULSE, SampleQCTypeEnum.AGILENT_FRAGMENT_ANALYZER),
        "nM",
        Boolean.FALSE),
    CONCENTRATION_PROTEIN(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS),
        Collections.singletonList(SampleFormEnum.SOLUTION),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µg/µl",
        Boolean.FALSE),
    CONDITION(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.PROTEOMICS_EU, SampleTypeEnum.SINGLE_CELLS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CORRECTION_RATE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.QUALITY_CONTROL, SampleTypeEnum.ILLUMINA_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SEQUENCING_CONTROL)),
    COVERAGE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    CQ(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.QPCR)),
    CRISPR_LIBRARY(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.CELL_ENGINEERING, SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DEVELOPMENT_STAGE(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DIGESTION_PROTOCOL(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.DIGESTED_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DILUTION(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DISEASE_STATE(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DMX_FLAG(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DS_ODN(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.OFF_TARGET_IDENTIFICATION),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DV_200(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.SPATIAL_VIS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "%",
        Boolean.FALSE),
    EFFECTOR_TYPE(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.CELL_ENGINEERING),
        Arrays.asList(SampleTypeEnum.CRISPR_SCREEN, SampleTypeEnum.OFF_TARGET_IDENTIFICATION),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    ENZYMES(
        Boolean.TRUE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_EU),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    EXTRACTION_PROTOCOL_STRING(
        Boolean.FALSE,
        String.class,
        Arrays.asList(SampleTypeEnum.SEQUENCING, SampleTypeEnum.RNA_DNA),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    EXTRACTION_PROTOCOL(
        Boolean.FALSE,
        Annotation.class,
        Arrays.asList(SampleTypeEnum.METABOLOMICS, SampleTypeEnum.SCINET_RODENT_MODEL),
        Arrays.asList(SampleTypeEnum.TRANSCRIPTOMICS, SampleTypeEnum.OFF_TARGET_IDENTIFICATION),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    FASTQ_SCREEN(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SEQUENCING_CONTROL)),
    GENETIC_MODIFICATION(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    GENOMIC_COORDINATES(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.OFF_TARGET_IDENTIFICATION),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    GENOTYPE(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.SCINET_RODENT_MODEL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    FIXATION(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    EMBEDDING_MEDIUM(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SLIDE_TYPE(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MATRIX(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS_MALDI),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    ON_SLIDE_MODIFICATION(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.SPATIAL_MS_MALDI),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    GROUPING_VAR(
        Boolean.FALSE,
        Annotation.class,
        Arrays.asList(SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_USER_LAB, SampleTypeEnum.PROTEOMICS_SERVICES),
        Arrays.asList(SampleTypeEnum.SPATIAL_MS, SampleTypeEnum.METABOLOMICS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    BAIT_ID(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_INTERACTION),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    GROWTH_CONDITIONS(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    GUIDE_NAME(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.OFF_TARGET_IDENTIFICATION, SampleTypeEnum.CELL_ENGINEERING),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    GUIDE_SEQUENCE(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.OFF_TARGET_IDENTIFICATION),
        Collections.singletonList(SampleTypeEnum.CELL_ENGINEERING),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    HYBRIDIZATION_PROTOCOL(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.TRANSCRIPTOMICS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    IMMUNO_PRECIPITATION_TARGET(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    INDIVIDUAL_ID(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    INITIAL_TIME_POINT(
        Boolean.FALSE,
        Annotation.class,
        Arrays.asList(SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL),
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_EU),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    INSTRUMENT(
        Boolean.FALSE,
        Instrument.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    INSTRUMENT_METHOD(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    INTEGRITY_NUMBER(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.RNA_DNA, SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Arrays.asList(SampleQCTypeEnum.AGILENT_BIOANALYZER, SampleQCTypeEnum.AGILENT_TAPESTATION, SampleQCTypeEnum.AGILENT_FEMTO_PULSE, SampleQCTypeEnum.AGILENT_FRAGMENT_ANALYZER)),
    INTERNAL_STANDARDS(
        Boolean.TRUE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_EU),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    IRTS(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    LABEL_AMOUNT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE_LABELED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µg",
        Boolean.FALSE),
    LABELING_METHOD(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE_LABELED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    LABELING_PROTOCOL(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.TRANSCRIPTOMICS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    LIBRARY_PROTOCOL(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    LIBRARY_SELECTION(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    LIBRARY_STRATEGY(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    LOT_INFORMATION(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE_LABELED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    LYSIS_BUFFER(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.DIGESTED_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MEDIA(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.SINGLE_CELL_SEQUENCING),
        Collections.singletonList(SampleTypeEnum.SINGLE_CELLS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    MOLARITY(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.ILLUMINA_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "nM",
        Boolean.FALSE),
    MOLARITY_FMOL(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.ONT_READY_MADE_LIBRARY, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.NANOPORE_LIBRARY_ON_RUN, SampleTypeEnum.ONT_READY_MADE_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "fmol",
        Boolean.FALSE),
    MOLARITY_TARGET(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "nM",
        Boolean.FALSE),
    MOLECULAR_WEIGHT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.QUALITY_CONTROL, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION),
        Arrays.asList(SampleFormEnum.DRY, SampleFormEnum.GEL_BAND, SampleFormEnum.GEL_LANE, SampleFormEnum.SOLUTION, SampleFormEnum.PVDF),
        new ArrayList<>(),
        new ArrayList<>(),
        Arrays.asList(SampleQCTypeEnum.AGILENT_BIOANALYZER, SampleQCTypeEnum.AGILENT_TAPESTATION, SampleQCTypeEnum.AGILENT_FEMTO_PULSE, SampleQCTypeEnum.AGILENT_FRAGMENT_ANALYZER),
        "Da",
        Boolean.FALSE),
    NUMBER_OF_CYCLES(
        Boolean.FALSE,
        Integer.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    ORGANISM_PART(
        Boolean.FALSE,
        Annotation.class,
        Arrays.asList(SampleTypeEnum.METABOLOMICS, SampleTypeEnum.CLINICAL, SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL),
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_EU),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    PRE_TREATMENT(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.SINGLE_CELLS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    PROTEIN_AMOUNT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE_LABELED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µg",
        Boolean.FALSE),
    PURITY_A_260_230(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.NANODROP)),
    PURITY_A_260_280(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.NANODROP)),
    QPCR(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "nM",
        Boolean.FALSE),
    QUBIT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "nM",
        Boolean.FALSE),
    READ_COUNT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.QUALITY_CONTROL, SampleTypeEnum.ILLUMINA_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SEQUENCING_CONTROL)),
    READ_COUNT_TOTAL(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        Collections.singletonList(SampleQCTypeEnum.SEQUENCING_CONTROL),
        new ArrayList<>()),
    RE_MULTIPLEXED(
        Boolean.FALSE,
        Boolean.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.ILLUMINA_MULTIPLEXED, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.MS_SAMPLE_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SAMPLING_DATE(
        Boolean.FALSE,
        LocalDateTime.class,
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_USER_LAB),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SCANNING_PROTOCOL(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.TRANSCRIPTOMICS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SEPARATION_TECHNIQUES(
        Boolean.TRUE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_EU),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SEQUENCING_METHOD(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SEQUENCING_MODE(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SEQUENCING_PLATFORM(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SEQUENCING_PRIMER(
        Boolean.FALSE,
        Annotation.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SEX(
        Boolean.FALSE,
        Annotation.class,
        Arrays.asList(SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL),
        Collections.singletonList(SampleTypeEnum.CLINICAL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SIZE(
        Boolean.FALSE,
        Integer.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µg",
        Boolean.FALSE),
    SIZE_AVERAGE(
        Boolean.FALSE,
        BigDecimal.class,
        Collections.singletonList(SampleTypeEnum.ONT_READY_MADE_LIBRARY),
        Arrays.asList(SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.ILLUMINA_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "bp",
        Boolean.FALSE),
    SIZE_GENOME_ESTIMATED(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.SEQUENCING),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "Mb",
        Boolean.FALSE),
    SIZE_RANGE(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.QUALITY_CONTROL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        Arrays.asList(SampleQCTypeEnum.AGILENT_BIOANALYZER, SampleQCTypeEnum.AGILENT_TAPESTATION, SampleQCTypeEnum.AGILENT_FEMTO_PULSE, SampleQCTypeEnum.AGILENT_FRAGMENT_ANALYZER)),
    SOURCE_TYPE(
        Boolean.FALSE,
        Annotation.class,
        Arrays.asList(SampleTypeEnum.SEQUENCING, SampleTypeEnum.RNA_DNA),
        Arrays.asList(SampleTypeEnum.OFF_TARGET_IDENTIFICATION, SampleTypeEnum.CELL_ENGINEERING),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    STRAIN(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SUBJECT_ID(
        Boolean.FALSE,
        String.class,
        Arrays.asList(SampleTypeEnum.CLINICAL, SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SURFACE(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.SURFACE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    TISSUE(
        Boolean.FALSE,
        String.class,
        Collections.singletonList(SampleTypeEnum.SPATIAL_VIS),
        Collections.singletonList(SampleTypeEnum.SINGLE_CELLS),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    TREATMENT(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.SCINET_RODENT_MODEL),
        Collections.singletonList(SampleTypeEnum.PROTEOMICS_EU),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    TS(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_MULTIPLEXED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "nM",
        Boolean.FALSE),
    VECTOR(
        Boolean.FALSE,
        Annotation.class,
        Collections.singletonList(SampleTypeEnum.CRISPR_SCREEN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    VOLUME(
        Boolean.FALSE,
        BigDecimal.class,
        Collections.singletonList(SampleTypeEnum.ONT_READY_MADE_LIBRARY),
        Arrays.asList(SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.METABOLOMICS, SampleTypeEnum.SEQUENCING, SampleTypeEnum.RNA_DNA, SampleTypeEnum.CDNA, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.ILLUMINA_MULTIPLEXED, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS, SampleTypeEnum.OFF_TARGET_IDENTIFICATION, SampleTypeEnum.SINGLE_CELL_SEQUENCING),
        Arrays.asList(SampleFormEnum.SOLUTION, SampleFormEnum.BEADS_REQUIRED, SampleFormEnum.BEADS_OPTIONAL),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_ELUTED(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.RNA_DNA, SampleTypeEnum.CDNA),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_INPUT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.DIGESTED_SAMPLE, SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.MS_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_LYSIS_BUFFER(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.DIGESTED_SAMPLE),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_MEASURED(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.RNA_DNA),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_REACTION(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.MS_SAMPLE_LABELED),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_TARGET(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    AMOUNT_TOTAL(
        Boolean.FALSE,
        BigDecimal.class,
        Collections.singletonList(SampleTypeEnum.ONT_READY_MADE_LIBRARY),
        Arrays.asList(SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.METABOLOMICS, SampleTypeEnum.SEQUENCING, SampleTypeEnum.RNA_DNA, SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS, SampleTypeEnum.CRISPR_SCREEN, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY),
        Arrays.asList(SampleFormEnum.DRY, SampleFormEnum.GEL_BAND),
        Collections.singletonList(SampleFormEnum.PVDF),
        new ArrayList<>(),
        new ArrayList<>(),
        "µg",
        Boolean.FALSE),
    VOLUME_DILUTION_SAMPLE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_DILUTION_WATER(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_TO_ADD_SAMPLE(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    VOLUME_TO_ADD_EBT(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Collections.singletonList(SampleTypeEnum.ILLUMINA_LIBRARY),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "µl",
        Boolean.FALSE),
    YIELD(
        Boolean.FALSE,
        BigDecimal.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.RNA_DNA, SampleTypeEnum.CDNA),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        "%",
        Boolean.FALSE),
    ORGANISM(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    SAMPLE_PREPARATION_PROTOCOL(
        Boolean.FALSE,
        SamplePreparationProtocol.class,
        Collections.singletonList(SampleTypeEnum.METABOLOMICS),
        Arrays.asList(SampleTypeEnum.PROTEOMICS_USER_LAB, SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.CDNA, SampleTypeEnum.ILLUMINA_MULTIPLEXED, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED, SampleTypeEnum.ONT_READY_MADE_MULTIPLEXED, SampleTypeEnum.ILLUMINA_LIBRARY_ON_RUN, SampleTypeEnum.PACBIO_LIBRARY_ON_RUN, SampleTypeEnum.NANOPORE_LIBRARY_ON_RUN, SampleTypeEnum.ONT_READY_MADE_LIBRARY_ON_RUN),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()),
    DESCRIPTION(
        Boolean.FALSE,
        String.class,
        new ArrayList<>(),
        Arrays.asList(SampleTypeEnum.GENERIC, SampleTypeEnum.METABOLOMICS, SampleTypeEnum.PROTEOMICS_INTERACTION, SampleTypeEnum.PROTEOMICS_SERVICES, SampleTypeEnum.PROTEOMICS_USER_LAB, SampleTypeEnum.PROTEOMICS_EU, SampleTypeEnum.SEQUENCING,
            SampleTypeEnum.SINGLE_CELL_SEQUENCING, SampleTypeEnum.TRANSCRIPTOMICS, SampleTypeEnum.CHEMICAL, SampleTypeEnum.ENVIRONMENTAL, SampleTypeEnum.SYNTHETICBIO, SampleTypeEnum.SYNTHETICSMALL,
            SampleTypeEnum.RNA_DNA, SampleTypeEnum.SINGLE_CELLS, SampleTypeEnum.ILLUMINA_LIBRARY_ON_RUN, SampleTypeEnum.PACBIO_LIBRARY_ON_RUN,
            SampleTypeEnum.CDNA, SampleTypeEnum.ILLUMINA_MULTIPLEXED, SampleTypeEnum.ILLUMINA_LIBRARY, SampleTypeEnum.PACBIO_LIBRARY, SampleTypeEnum.QUALITY_CONTROL,
            SampleTypeEnum.NANOPORE_LIBRARY_ON_RUN, SampleTypeEnum.NANOPORE_LIBRARY, SampleTypeEnum.ONT_READY_MADE_LIBRARY_ON_RUN,
            SampleTypeEnum.DIGESTED_SAMPLE, SampleTypeEnum.MS_SAMPLE, SampleTypeEnum.CLINICAL, SampleTypeEnum.SCINET_HUMAN_PATIENT, SampleTypeEnum.SCINET_RODENT_MODEL, SampleTypeEnum.SURFACE,
            SampleTypeEnum.AMINO_ACID_ANALYSIS, SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION, SampleTypeEnum.GLYCOPROTEIN_ANALYSIS, SampleTypeEnum.USER_LIBRARY_IN_POOL, SampleTypeEnum.CONTROL_SAMPLE,
            SampleTypeEnum.OFF_TARGET_IDENTIFICATION, SampleTypeEnum.CELL_ENGINEERING, SampleTypeEnum.CRISPR_SCREEN, SampleTypeEnum.PACBIO_MULTIPLEXED, SampleTypeEnum.NANOPORE_MULTIPLEXED,
            SampleTypeEnum.MS_SAMPLE_LABELED, SampleTypeEnum.MS_SAMPLE_MULTIPLEXED, SampleTypeEnum.SPATIAL_VIS, SampleTypeEnum.SPATIAL_MS, SampleTypeEnum.SPATIAL_MS_MALDI),
        Arrays.asList(SampleFormEnum.BEADS_REQUIRED, SampleFormEnum.GEL_BAND_REQUIRED, SampleFormEnum.CELL_PELLET, SampleFormEnum.TISSUE),
        Arrays.asList(SampleFormEnum.BEADS_OPTIONAL, SampleFormEnum.GEL_BAND, SampleFormEnum.DRY, SampleFormEnum.GEL_LANE, SampleFormEnum.SOLUTION, SampleFormEnum.PVDF, SampleFormEnum.BIOFILM, SampleFormEnum.CELLS, SampleFormEnum.ORGANOID, SampleFormEnum.TMA),
        new ArrayList<>(),
        new ArrayList<>());

    private final String name;

    private final Class<?> clazz;

    private final List<SampleFormEnum> sampleFormsOptional;

    private final List<SampleFormEnum> sampleFormsRequired;

    private final Boolean multiValued;

    private final Boolean forEmployeesOnly;

    private final List<SampleQCTypeEnum> qcTypesOptional;

    private final List<SampleQCTypeEnum> qcTypesRequired;

    private final List<SampleTypeEnum> typesOptional;

    private final List<SampleTypeEnum> typesRequired;

    private final String unit;

    SampleAttributeEnum(Boolean multiValued, Class<?> clazz, List<SampleTypeEnum> typesRequired, List<SampleTypeEnum> typesOptional, List<SampleFormEnum> sampleFormsRequired, List<SampleFormEnum> sampleFormsOptional, List<SampleQCTypeEnum> qcTypesRequired, List<SampleQCTypeEnum> qcTypesOptional) {
        this(multiValued, clazz, typesRequired, typesOptional, sampleFormsRequired, sampleFormsOptional, qcTypesRequired, qcTypesOptional, null, Boolean.FALSE);
    }

    SampleAttributeEnum(Boolean multiValued, Class<?> clazz, List<SampleTypeEnum> typesRequired, List<SampleTypeEnum> typesOptional, List<SampleFormEnum> sampleFormsRequired, List<SampleFormEnum> sampleFormsOptional, List<SampleQCTypeEnum> qcTypesRequired, List<SampleQCTypeEnum> qcTypesOptional, String unit, Boolean forEmployeesOnly) {
        this.name = StringHelper.firstLower(WordUtils.capitalizeFully(name(), '_').replaceAll("_", ""));
        this.multiValued = multiValued;
        this.clazz = clazz;
        this.typesRequired = typesRequired;
        this.typesOptional = typesOptional;
        this.sampleFormsRequired = sampleFormsRequired;
        this.sampleFormsOptional = sampleFormsOptional;
        this.qcTypesRequired = qcTypesRequired;
        this.qcTypesOptional = qcTypesOptional;
        this.unit = unit;
        this.forEmployeesOnly = forEmployeesOnly;
    }

    public static List<SampleAttributeEnum> getAnnotationTypes() {
        Set<SampleAttributeEnum> annotationTypes = new HashSet<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.isAnnotationType()) {
                annotationTypes.add(attributeEnum);
            }
        }
        return CollectionHelper.sortObjects(annotationTypes);
    }

    public static SampleAttributeEnum getAttributeByLabel(String label, Class<?> clazz) {
        SampleAttributeEnum ret = null;
        if (StringHelper.isNotEmpty(label) && clazz != null) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getLabel().equals(label) && attributeEnum.getClazz().equals(clazz)) {
                    ret = attributeEnum;
                    break;
                }
            }
        }
        return ret;
    }

    public static SampleAttributeEnum getAttributeByName(String name) {
        SampleAttributeEnum ret = null;
        if (StringHelper.isNotEmpty(name)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getName().equals(name)) {
                    ret = attributeEnum;
                    break;
                }
            }
        }
        return ret;
    }

    public static List<SampleAttributeEnum> getAttributeEnums(boolean isForEmployeesOnlyFlagActive, String type) {
        if (StringHelper.isEmpty(type)) {
            return Arrays.asList(values());
        }
        List<SampleAttributeEnum> attributeEnums = new ArrayList<>();
        attributeEnums.addAll(getAttributeEnumsRequired(type));
        attributeEnums.addAll(getAttributeEnumsOptional(type));
        if (isForEmployeesOnlyFlagActive) {
            removeForContainerManagerOnlyAttributes(attributeEnums, getIdentityService().hasRoleEnum(RoleEnum.CONTAINERMANAGER));
        }
        return attributeEnums;
    }

    public static List<SampleAttributeEnum> getAttributeEnums(String type, Set<String> types) {
        final List<SampleAttributeEnum> attributeEnums = new ArrayList<>();

        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.isAttribute(type, types)) {
                attributeEnums.add(attributeEnum);
            }
        }

        removeForContainerManagerOnlyAttributes(attributeEnums, getIdentityService().hasRoleEnum(RoleEnum.CONTAINERMANAGER));
        orderSampleAttributes(attributeEnums);
        return attributeEnums;
    }

    public static List<SampleAttributeEnum> getAttributeEnums(Set<String> types) {
        final List<SampleAttributeEnum> attributeEnums = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.isAttribute(null, types)) {
                attributeEnums.add(attributeEnum);
            }
        }
        removeForContainerManagerOnlyAttributes(attributeEnums, getIdentityService().hasRoleEnum(RoleEnum.CONTAINERMANAGER));
        return attributeEnums;
    }

    public static List<SampleAttributeEnum> getAttributeEnums(String type, Set<String> types, Set<SampleQCTypeEnum> qcTypes, Set<SampleFormEnum> sampleForms) {
        final List<SampleAttributeEnum> attributeEnums = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.isAttribute(type, types, qcTypes, sampleForms)) {
                attributeEnums.add(attributeEnum);
            }
        }
        removeForContainerManagerOnlyAttributes(attributeEnums, getIdentityService().hasRoleEnum(RoleEnum.CONTAINERMANAGER));
        orderSampleAttributes(attributeEnums);
        return attributeEnums;
    }

    public static List<SampleAttributeEnum> getAttributeEnums(String type, boolean orderForm, boolean orderConfirmationForm) {
        if (StringHelper.isEmpty(type)) {
            return Arrays.asList(values());
        }
        List<SampleAttributeEnum> attributeEnums = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.getTypesRequired().contains(type) && attributeEnum.isAttribute(type, orderForm, orderConfirmationForm)) {
                attributeEnums.add(attributeEnum);
            }
        }
        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.getTypesOptional().contains(type) && attributeEnum.isAttribute(type, orderForm, orderConfirmationForm)) {
                attributeEnums.add(attributeEnum);
            }
        }

        removeForContainerManagerOnlyAttributes(attributeEnums, getIdentityService().hasRoleEnum(RoleEnum.CONTAINERMANAGER));
        return attributeEnums;
    }

    public static List<SampleAttributeEnum> getAttributeEnums(boolean includeOldSampleAttributes, Sample sample, String type, boolean includeSampleFormDependency, SampleFormEnum sampleFormEnum, boolean includeSampleQCTypeDependency, SampleQCTypeEnum sampleQCTypeEnum, boolean orderForm, boolean orderConfirmationForm) {
        final List<SampleAttributeEnum> attributeEnums = new ArrayList<>();
        if (StringHelper.isNotEmpty(type)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.isAttribute(type, orderForm, orderConfirmationForm)) {
                    if (hasSampleTypeSampleForm(type) && includeSampleFormDependency) {
                        if (attributeEnum.isSampleFormDependentAttributeAndRequired(type, sampleFormEnum)) {
                            attributeEnums.add(attributeEnum);
                        }
                    } else if (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) && includeSampleQCTypeDependency) {
                        if (!attributeEnum.isQCTypeDependentAttribute() && attributeEnum.getTypesRequired()
                            .contains(SampleTypeEnum.QUALITY_CONTROL.getLabel()) || sampleQCTypeEnum != null && attributeEnum.getQcTypesRequired().contains(sampleQCTypeEnum.getLabel())) {
                            attributeEnums.add(attributeEnum);
                        }
                    } else {
                        if (attributeEnum.getTypesRequired().contains(type)) {
                            attributeEnums.add(attributeEnum);
                        }
                    }
                }
            }
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.isAttribute(type, orderForm, orderConfirmationForm)) {
                    if (hasSampleTypeSampleForm(type) && includeSampleFormDependency) {
                        if (attributeEnum.isSampleFormDependentAttributeAndOptional(type, sampleFormEnum)) {
                            attributeEnums.add(attributeEnum);
                        }
                    } else if (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) && includeSampleQCTypeDependency) {
                        if (!attributeEnum.isQCTypeDependentAttribute() && attributeEnum.getTypesOptional()
                            .contains(SampleTypeEnum.QUALITY_CONTROL.getLabel()) || sampleQCTypeEnum != null && attributeEnum.getQcTypesOptional().contains(sampleQCTypeEnum.getLabel())) {
                            attributeEnums.add(attributeEnum);
                        }
                    } else {
                        if (attributeEnum.getTypesOptional().contains(type)) {
                            attributeEnums.add(attributeEnum);
                        }
                    }
                }
            }

            // Used solely in single sample editing.
            if (includeOldSampleAttributes && sample != null) {
                // Make sure the oldSampleAttributeEnums are initialized.
                sample.setOldSampleAttributeEnums();
                for (final SampleAttributeEnum oldSampleAttributeEnum : sample.getOldSampleAttributeEnums()) {
                    if (!attributeEnums.contains(oldSampleAttributeEnum)) {
                        // Allow editing of legacy attributes.
                        attributeEnums.add(oldSampleAttributeEnum);
                    }
                }
            }
        }

        removeForContainerManagerOnlyAttributes(attributeEnums, getIdentityService().hasRoleEnum(RoleEnum.CONTAINERMANAGER));
        orderSampleAttributes(attributeEnums);
        return attributeEnums;
    }

    public static List<SampleAttributeEnum> getAttributeEnumsOptional(String type) {
        Set<SampleAttributeEnum> attributeEnums = new HashSet<>();
        if (StringHelper.isNotEmpty(type)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getTypesOptional().contains(type)) {
                    attributeEnums.add(attributeEnum);
                }
            }
        }
        return CollectionHelper.sortObjects(attributeEnums);
    }

    public static List<SampleAttributeEnum> getAttributeEnumsOrderedForPlate(List<SampleAttributeEnum> toOrder, String type, Set<String> aTypes, Plate plate) {
        if (plate != null) {
            if (plate.getPlateType().isQualityControlPlateType() && (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) || aTypes != null && aTypes.size() == 1 && SampleTypeEnum.QUALITY_CONTROL
                .getLabel().equals(aTypes.iterator().next()))) {
                return getAttributeEnumsOrderedForView(toOrder, Constants.QC_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
            }
            if (plate.getPlateType().isIlluminaLibraryPlateType() && (SampleTypeEnum.ILLUMINA_LIBRARY.getLabel().equals(type) || aTypes != null && aTypes
                .size() == 1 && SampleTypeEnum.ILLUMINA_LIBRARY.getLabel().equals(aTypes.iterator().next()))) {
                return getAttributeEnumsOrderedForView(toOrder, Constants.ILLUMINA_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
            }
            if (plate.getPlateType().isNanoporeLibraryPlateType() && (SampleTypeEnum.NANOPORE_LIBRARY.getLabel().equals(type) || aTypes != null && aTypes
                .size() == 1 && SampleTypeEnum.NANOPORE_LIBRARY.getLabel().equals(aTypes.iterator().next()))) {
                return getAttributeEnumsOrderedForView(toOrder, Constants.NANOPORE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
            }
            if (plate.getPlateType().isONTReadyMadeLibraryPlateType() && (SampleTypeEnum.ONT_READY_MADE_LIBRARY.getLabel().equals(type) || aTypes != null && aTypes
                .size() == 1 && SampleTypeEnum.ONT_READY_MADE_LIBRARY.getLabel().equals(aTypes.iterator().next()))) {
                return getAttributeEnumsOrderedForView(toOrder, Constants.ONT_READY_MADE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
            }
            if (plate.getPlateType().isPacBioLibraryPlateType() && (SampleTypeEnum.PACBIO_LIBRARY.getLabel().equals(type) || aTypes != null && aTypes
                .size() == 1 && SampleTypeEnum.PACBIO_LIBRARY.getLabel().equals(aTypes.iterator().next()))) {
                return getAttributeEnumsOrderedForView(toOrder, Constants.PACBIO_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
            }
        }
        return toOrder;
    }

    public static List<SampleAttributeEnum> getAttributeEnumsOrderedForView(List<SampleAttributeEnum> toOrder, String view) {
        if (Constants.QC_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            // Grouping/ordering of the attributes regarding the QC type from left to right. Default order up to column 'QC Type', then Column concentration and columns belonging to Qubit, GloMax, Nanodrop, Agilent TapeStation / Fragment Analyzer / Bioanalyzer / Femto Pulse, qPCR, Sequencing Control, Single Cell
            List<SampleAttributeEnum> orderedSampleAttributeEnums = new ArrayList<>();
            int sliceIndexStart = 0;
            while (sliceIndexStart < toOrder.size()) {
                sliceIndexStart++;
                orderedSampleAttributeEnums.add(toOrder.get(sliceIndexStart - 1));
                if (toOrder.get(sliceIndexStart - 1).equals(QUALITY_CONTROL_TYPE)) {
                    break;
                }
            }

            if (sliceIndexStart < toOrder.size()) {
                orderedSampleAttributeEnums
                    .addAll(getAttributeEnumsOrderedForViewHelper(toOrder.subList(sliceIndexStart, toOrder.size()), Constants.QC_PLATE_SAMPLE_TABLE_COLUMN_ORDER));
            }

            return orderedSampleAttributeEnums;
        }
        if (Constants.ILLUMINA_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            return getAttributeEnumsOrderedForViewHelper(toOrder, Constants.ILLUMINA_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
        }
        if (Constants.NANOPORE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            return getAttributeEnumsOrderedForViewHelper(toOrder, Constants.NANOPORE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
        }
        if (Constants.ONT_READY_MADE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            return getAttributeEnumsOrderedForViewHelper(toOrder, Constants.ONT_READY_MADE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
        }
        if (Constants.PACBIO_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            return getAttributeEnumsOrderedForViewHelper(toOrder, Constants.PACBIO_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER);
        }
        return toOrder;
    }

    public static List<SampleAttributeEnum> getAttributeEnumsOrderedForViewHelper(List<SampleAttributeEnum> toOrder, String view) {
        List<SampleAttributeEnum> ordered = new ArrayList<>();
        if (Constants.QC_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            if (toOrder.remove(CONCENTRATION)) {
                // Concentration is the most important attribute.
                ordered.add(CONCENTRATION);
            }

            // Groups ordered by importance: Qubit, GloMax, Nanodrop, Agilent TapeStation / Fragment Analyzer / Bioanalyzer / Femto Pulse, qPCR, Sequencing Control, Single Cell
            List<Set<SampleQCTypeEnum>> groups = SampleQCTypeEnum.getSampleQCTypeEnumGroupsOrderedForView(view);
            Map<String, List<SampleAttributeEnum>> grouped = new HashMap<>();
            List<SampleAttributeEnum> defaultOrder = new ArrayList<>();

            for (SampleAttributeEnum sampleAttributeEnum : toOrder) {
                if (sampleAttributeEnum.isQCTypeDependentAttribute()) {
                    groupLoop:
                    for (Set<SampleQCTypeEnum> group : groups) {
                        for (SampleQCTypeEnum sampleQCTypeEnum : group) {
                            if (sampleAttributeEnum.getQcTypesRequired().contains(sampleQCTypeEnum.getLabel()) || sampleAttributeEnum.getQcTypesOptional().contains(sampleQCTypeEnum.getLabel())) {
                                String key = CollectionHelper.print(group);
                                if (grouped.containsKey(key)) {
                                    grouped.get(key).add(sampleAttributeEnum);
                                } else {
                                    grouped.put(key, new ArrayList<>(Collections.singletonList(sampleAttributeEnum)));
                                }
                                break groupLoop;
                            }
                        }
                    }
                } else {
                    defaultOrder.add(sampleAttributeEnum);
                }
            }

            for (Set<SampleQCTypeEnum> group : groups) {
                String key = CollectionHelper.print(group);
                if (grouped.containsKey(key)) {
                    ordered.addAll(grouped.get(key));
                }
            }
            ordered.addAll(defaultOrder);
        } else if (Constants.ILLUMINA_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            Set<SampleAttributeEnum> toOrderSet = new HashSet<>(toOrder);
            List<SampleAttributeEnum> allOrdered = Arrays.asList(TUBE_ID, MULTIPLEX_ID, MULTIPLEX_ID_2, MULTIPLEX_KIT,
                MULTIPLEX_KIT_2, QC_PASSED, STATUS, SAMPLE_PREPARATION_PROTOCOL,
                CONCENTRATION_INPUT_QC, AMOUNT_INPUT, VOLUME_INPUT, VOLUME_DILUTION_SAMPLE, VOLUME_DILUTION_WATER,
                MOLARITY, MOLARITY_TARGET, VOLUME_TARGET, VOLUME_TO_ADD_SAMPLE, VOLUME_TO_ADD_EBT,
                DESCRIPTION);

            // Check if all attributes in allOrdered are actually in the toOrder, i.e., allOrdered needs to be a subset of toOrder.
            if (toOrderSet.containsAll(new HashSet<>(allOrdered))) {
                ordered.addAll(allOrdered);
            } else {
                ordered.addAll(toOrder);
            }
        } else if (Constants.NANOPORE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view) || Constants.ONT_READY_MADE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view) || Constants.PACBIO_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
            Set<SampleAttributeEnum> toOrderSet = new HashSet<>(toOrder);
            List<SampleAttributeEnum> allOrdered = new ArrayList<>(Arrays
                .asList(TUBE_ID, MULTIPLEX_ID, MULTIPLEX_ID_2, MULTIPLEX_KIT, MULTIPLEX_KIT_2,
                    QC_PASSED, STATUS, SAMPLE_PREPARATION_PROTOCOL));
            if (Constants.NANOPORE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view) || Constants.ONT_READY_MADE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER.equals(view)) {
                allOrdered.addAll(Arrays.asList(CONCENTRATION, VOLUME, AMOUNT_TOTAL, SIZE_AVERAGE, MOLARITY_FMOL));
            } else {
                allOrdered.addAll(Arrays.asList(CONCENTRATION, VOLUME, AMOUNT_TOTAL, SIZE_AVERAGE));
            }
            allOrdered.add(DESCRIPTION);

            // Check if all attributes in allOrdered are actually in the toOrder, i.e., allOrdered needs to be a subset of toOrder.
            if (toOrderSet.containsAll(new HashSet<>(allOrdered))) {
                ordered.addAll(allOrdered);
            } else {
                ordered.addAll(toOrder);
            }
        } else {
            ordered.addAll(toOrder);
        }

        orderSampleAttributes(ordered);
        return ordered;
    }

    public static List<SampleAttributeEnum> getAttributeEnumsRequired(String type) {
        Set<SampleAttributeEnum> attributeEnums = new HashSet<>();
        if (StringHelper.isNotEmpty(type)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getTypesRequired().contains(type)) {
                    attributeEnums.add(attributeEnum);
                }
            }
        }
        return CollectionHelper.sortObjects(attributeEnums);
    }

    public static String getAttributeLabelByName(String name) {
        String ret = null;
        if (StringHelper.isNotEmpty(name)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getName().equals(name)) {
                    ret = attributeEnum.getLabel();
                    break;
                }
            }
        }
        return ret;
    }

    public static List<String> getAttributeLabelsLowerCase() {
        List<String> ret = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            ret.add(attributeEnum.getLabel().toLowerCase());
        }
        return ret;
    }

    public static List<String> getAttributeTypes() {
        Set<String> attributeTypes = new HashSet<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            attributeTypes.addAll(attributeEnum.getTypesOptional());
            attributeTypes.addAll(attributeEnum.getTypesRequired());
        }
        return CollectionHelper.sortObjects(attributeTypes);
    }

    public static List<String> getAttributesOptional() {
        Set<String> attributes = new HashSet<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (!attributeEnum.getTypesOptional().isEmpty()) {
                attributes.add(attributeEnum.getLabel());
            }
        }
        return CollectionHelper.sortObjects(attributes);
    }

    public static List<String> getAttributesOptional(String type) {
        Set<String> attributes = new HashSet<>();
        if (StringHelper.isNotEmpty(type)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getTypesOptional().contains(type)) {
                    attributes.add(attributeEnum.getLabel());
                }
            }
        }
        return CollectionHelper.sortObjects(attributes);
    }

    public static List<String> getAttributesRequired() {
        Set<String> attributes = new HashSet<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (!attributeEnum.getTypesRequired().isEmpty()) {
                attributes.add(attributeEnum.getLabel());
            }
        }
        return CollectionHelper.sortObjects(attributes);
    }

    public static List<String> getAttributesRequired(String type) {
        Set<String> attributes = new HashSet<>();
        if (StringHelper.isNotEmpty(type)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getTypesRequired().contains(type)) {
                    attributes.add(attributeEnum.getName());
                }
            }
        }
        return CollectionHelper.sortObjects(attributes);
    }

    public static List<SampleAttributeEnum> getBooleanRequiredAttributes() {
        List<SampleAttributeEnum> booleanRequiredAttributes = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.isBooleanType() && !attributeEnum.getTypesRequired().isEmpty()) {
                booleanRequiredAttributes.add(attributeEnum);
            }
        }
        return booleanRequiredAttributes;
    }

    public static List<SampleAttributeEnum> getEnabledSampleAttributeEnum() {
        List<SampleAttributeEnum> enabled = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (!attributeEnum.getTypesRequired().isEmpty() || !attributeEnum.getTypesOptional().isEmpty()) {
                enabled.add(attributeEnum);
            }
        }

        orderSampleAttributes(enabled);
        return CollectionHelper.sortObjects(enabled);
    }

    public static List<SampleAttributeEnum> getExtensibleAnnotationTypes(String sampleTypeName) {
        return getExtensibleAnnotationTypes(SampleTypeEnum.getSampleTypeEnumByLabel(sampleTypeName));
    }

    public static List<SampleAttributeEnum> getExtensibleAnnotationTypes(SampleTypeEnum sampleTypeEnum) {
        List<SampleAttributeEnum> annotationTypes = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (attributeEnum.isAnnotationExtensible() && attributeEnum.isAttribute(sampleTypeEnum.getLabel())) {
                annotationTypes.add(attributeEnum);
            }
        }
        return CollectionHelper.sortObjects(annotationTypes);
    }

    public static List<String> getFormLabels(List<SampleFormEnum> sampleFormEnums) {
        List<String> formLabels = new ArrayList<>();
        for (SampleFormEnum sampleFormEnum : sampleFormEnums) {
            formLabels.add(sampleFormEnum.getLabel());
        }
        return formLabels;
    }

    public static IdentityService getIdentityService() {
        return CDI.current().select(IdentityService.class).get();
    }

    public static boolean[] getMultiplexIdsAssignability(String type, Set<String> types) {
        boolean[] multiplexIdAssignability = new boolean[] { false, false };
        if (!StringHelper.isEmpty(type)) {
            return SampleTypeEnum.USER_LIBRARY_IN_POOL.getLabel().equals(type) ? multiplexIdAssignability : new boolean[] { hasMultiplexIdAttribute(type), hasMultiplexId2Attribute(type) };
        }

        if (types != null && !types.isEmpty() && !types.contains(SampleTypeEnum.USER_LIBRARY_IN_POOL.getLabel())) {
            multiplexIdAssignability[0] = true;
            multiplexIdAssignability[1] = true;
            for (String aType : types) {
                multiplexIdAssignability[0] &= hasMultiplexIdAttribute(aType);
                multiplexIdAssignability[1] &= hasMultiplexId2Attribute(aType);
                if (!multiplexIdAssignability[0] && !multiplexIdAssignability[1]) {
                    // Case 2 is not satisfied.
                    break;
                }
            }
        }
        return multiplexIdAssignability;
    }

    public static List<SampleAttributeEnum> getQCTypeDependentAttributes() {
        return getQcTypeDependentAttributes(false, false);
    }

    public static List<SampleAttributeEnum> getQcTypeDependentAttributes(boolean onlyRequired, boolean onlyOptional) {
        List<SampleAttributeEnum> qcTypeDependentAttributes = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (!onlyOptional && !attributeEnum.getQcTypesRequired().isEmpty() || !onlyRequired && !attributeEnum.getQcTypesOptional().isEmpty()) {
                qcTypeDependentAttributes.add(attributeEnum);
            }
        }

        return CollectionHelper.sortObjects(qcTypeDependentAttributes);
    }

    public static List<SampleAttributeEnum> getQcTypeDependentRequiredAttributes() {
        return getQcTypeDependentAttributes(true, false);
    }

    public static List<String> getQcTypeLabels(List<SampleQCTypeEnum> sampleQCTypeEnums) {
        List<String> qcTypeLabels = new ArrayList<>();
        for (SampleQCTypeEnum sampleQCTypeEnum : sampleQCTypeEnums) {
            qcTypeLabels.add(sampleQCTypeEnum.getLabel());
        }
        return qcTypeLabels;
    }

    public static List<SampleAttributeEnum> getSampleFormDependentAttributes() {
        return getSampleFormDependentAttributes(false, false);
    }

    public static List<SampleAttributeEnum> getSampleFormDependentAttributes(boolean onlyRequired, boolean onlyOptional) {
        List<SampleAttributeEnum> sampleFormDependentAttributes = new ArrayList<>();
        for (SampleAttributeEnum attributeEnum : values()) {
            if (!onlyOptional && !attributeEnum.getSampleFormsRequired().isEmpty() || !onlyRequired && !attributeEnum.getSampleFormsOptional().isEmpty()) {
                sampleFormDependentAttributes.add(attributeEnum);
            }
        }

        return CollectionHelper.sortObjects(sampleFormDependentAttributes);
    }

    public static List<SampleAttributeEnum> getSampleFormDependentOptionalAttributes() {
        return getSampleFormDependentAttributes(false, true);
    }

    public static List<SampleAttributeEnum> getSampleFormDependentRequiredAttributes() {
        return getSampleFormDependentAttributes(true, false);
    }

    public static List<String> getTypeLabels(List<SampleTypeEnum> sampleTypeEnums) {
        List<String> typeLabels = new ArrayList<>();
        for (SampleTypeEnum sampleTypeEnum : sampleTypeEnums) {
            typeLabels.add(sampleTypeEnum.getLabel());
        }
        return typeLabels;
    }

    public static boolean hasMultiplexId2Attribute(String type) {
        return type != null && (MULTIPLEX_ID_2.getTypesRequired().contains(type) || MULTIPLEX_ID_2.getTypesOptional().contains(type));
    }

    public static boolean hasMultiplexIdAttribute(String type) {
        return type != null && (MULTIPLEX_ID.getTypesRequired().contains(type) || MULTIPLEX_ID.getTypesOptional().contains(type));
    }

    public static boolean hasSampleTypeSampleForm(String type) {
        return SAMPLE_FORM.getTypesRequired().contains(type) || SAMPLE_FORM.getTypesOptional().contains(type);
    }

    public static boolean isAnnotationType(String label) {
        return isAnnotationTypeSingleValued(label) || isAnnotationTypeMultiValued(label);
    }

    public static boolean isAnnotationTypeMultiValued(String label) {
        boolean ret = false;
        if (StringHelper.isNotEmpty(label)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getLabel().equals(label) && attributeEnum.isAnnotationType() && attributeEnum.getMultiValued()) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public static boolean isAnnotationTypeSingleValued(String label) {
        boolean ret = false;
        if (StringHelper.isNotEmpty(label)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getLabel().equals(label) && attributeEnum.isAnnotationType() && !attributeEnum.getMultiValued()) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public static boolean isAttributeOptional(String name, String type) {
        boolean ret = false;
        if (StringHelper.isNotEmpty(name) && StringHelper.isNotEmpty(type)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getName().equals(name) && attributeEnum.getTypesOptional().contains(type)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public static boolean isAttributeRequired(String name, String type) {
        boolean ret = false;
        if (StringHelper.isNotEmpty(name) && StringHelper.isNotEmpty(type)) {
            for (SampleAttributeEnum attributeEnum : values()) {
                if (attributeEnum.getName().equals(name) && attributeEnum.getTypesRequired().contains(type)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public static void orderSampleAttributes(List<SampleAttributeEnum> sampleAttributes) {
        // Re-order attributes if the default order does not fit.
        if (sampleAttributes != null && sampleAttributes.contains(TUBE_ID) && !sampleAttributes.get(0).equals(TUBE_ID)) {
            SampleAttributeEnum tubeId = sampleAttributes.get(sampleAttributes.indexOf(TUBE_ID));
            sampleAttributes.remove(tubeId);
            sampleAttributes.add(0, tubeId);
            if (sampleAttributes.contains(MULTIPLEXED) && !sampleAttributes.get(1).equals(MULTIPLEXED)) {
                SampleAttributeEnum multiplexed = sampleAttributes.get(sampleAttributes.indexOf(MULTIPLEXED));
                sampleAttributes.remove(multiplexed);
                sampleAttributes.add(1, multiplexed);
            }
        }
    }

    public static void removeForContainerManagerOnlyAttributes(Collection<SampleAttributeEnum> sampleAttributeEnums, Boolean isContainerManager) {
        if (isContainerManager != null && !isContainerManager) {
            sampleAttributeEnums.removeIf(SampleAttributeEnum::isForContainerManagerOnlyType);
        }
    }

    public String getClassName() {
        return getClazz().getSimpleName();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<?> getEnumSelectionValues(String type) {
        if (isEnumType()) {
            if (getClazz().equals(SampleFormEnum.class) && type != null) {
                return SampleFormEnum.getEnabledValuesByType(type);
            }
            return getEnumSelectionValues();
        }

        return new ArrayList<>();
    }

    public List<?> getEnumSelectionValues() {
        return getEnumValues(true);
    }

    public List<?> getEnumSelectionValuesIncluding(Sample sample) {
        if (sample != null && isEnumType()) {
            if (getClazz().equals(SampleFormEnum.class)) {
                List<SampleFormEnum> enabledValues = new ArrayList<>(SampleFormEnum.getEnabledValuesByType(sample.getType()));
                if (sample.getSampleForm() != null && !enabledValues.contains(sample.getSampleForm())) {
                    enabledValues.add(sample.getSampleForm());
                }
                return enabledValues;
            }
            if (getClazz().equals(SampleQCTypeEnum.class)) {
                List<SampleQCTypeEnum> enabledValues = new ArrayList<>(SampleQCTypeEnum.getEnabledValues());
                if (sample.getQualityControlType() != null && !enabledValues.contains(sample.getQualityControlType())) {
                    enabledValues.add(sample.getQualityControlType());
                }
                return enabledValues;
            }
            if (getClazz().equals(SampleStatusEnum.class)) {
                List<SampleStatusEnum> enabledValues = new ArrayList<>(SampleStatusEnum.getEnabledValues());
                if (sample.getStatus() != null && !enabledValues.contains(sample.getStatus())) {
                    enabledValues.add(sample.getStatus());
                }
                return enabledValues;
            }
        }

        return new ArrayList<>();
    }

    public List<?> getEnumValues(boolean onlyEnabled) {
        if (isEnumType()) {
            if (getClazz().equals(SampleFormEnum.class)) {
                return onlyEnabled ? SampleFormEnum.getEnabledValues() : Arrays.stream(SampleFormEnum.values()).collect(Collectors.toList());
            }
            if (getClazz().equals(SampleQCTypeEnum.class)) {
                return onlyEnabled ? SampleQCTypeEnum.getEnabledValues() : Arrays.stream(SampleQCTypeEnum.values()).collect(Collectors.toList());
            }
            if (getClazz().equals(SampleStatusEnum.class)) {
                return onlyEnabled ? SampleStatusEnum.getEnabledValues() : Arrays.stream(SampleStatusEnum.values()).collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    public Boolean getForEmployeesOnly() {
        return forEmployeesOnly;
    }

    public String getLabel() {
        return Messages.get(StringHelper.firstLower(WordUtils.capitalizeFully(name(), '_').replaceAll("_", "")));
    }

    public String getLabelWithUnit() {
        StringBuilder ret = new StringBuilder();
        ret.append(getLabel());
        if (StringHelper.isNotEmpty(getUnit())) {
            ret.append(" (");
            ret.append(getUnit());
            ret.append(")");
        }
        return ret.toString();
    }

    public int getMaxLength() {
        if (isStringType()) {
            switch (this) {
            case DESCRIPTION:
                return -1;
            case AGE_UNIT:
                return 9;
            case BLOCK:
            case TUBE_ID:
                return 32;
            case ARRAY_DESIGN_NAME:
            case LABELING_PROTOCOL:
            case BEADS_TYPE:
                return 128;
            case CONDITION:
            case CRISPR_LIBRARY:
            case DILUTION:
            case FASTQ_SCREEN:
            case GUIDE_NAME:
            case INSTRUMENT_METHOD:
            case IRTS:
            case LOT_INFORMATION:
            case LYSIS_BUFFER:
            case ORGANISM:
            case PRE_TREATMENT:
            case SIZE_RANGE:
            case TISSUE:
                return 256;
            case AMPLICON_SEQUENCE:
            case DMX_FLAG:
            case GUIDE_SEQUENCE:
                return 1024;
            case BUFFER:
                return 2048;
            default:
                return 64;
            }
        }
        return -1;
    }

    public Boolean getMultiValued() {
        return multiValued;
    }

    public String getName() {
        return name;
    }

    public List<String> getQcTypesOptional() {
        return getQcTypeLabels(qcTypesOptional);
    }

    public List<String> getQcTypesRequired() {
        return getQcTypeLabels(qcTypesRequired);
    }

    public List<String> getSampleFormsOptional() {
        return getFormLabels(sampleFormsOptional);
    }

    public List<String> getSampleFormsRequired() {
        return getFormLabels(sampleFormsRequired);
    }

    public List<String> getTypesOptional() {
        return getTypeLabels(typesOptional);
    }

    public String getTypesOptionalAsText() {
        return CollectionHelper.print(getTypeLabels(typesOptional), " ");
    }

    public String getTypesOptionalAsText2() {
        return CollectionHelper.print(getTypeLabels(typesOptional), " ", ", ", true);
    }

    public List<String> getTypesRequired() {
        return getTypeLabels(typesRequired);
    }

    public String getTypesRequiredAsText() {
        return CollectionHelper.print(getTypeLabels(typesRequired), " ");
    }

    public String getTypesRequiredAsText2() {
        return CollectionHelper.print(getTypeLabels(typesRequired), " ", ", ", true);
    }

    public String getUiIncludeColumnPath() {
        if (isStringType()) {
            return Constants.UI_INCLUDE_COLUMN_PATH_STRING_TYPE;
        }
        if (isBigDecimalType() || isFloatType() || isIntegerType()) {
            return Constants.UI_INCLUDE_COLUMN_PATH_NUMBER_TYPE;
        }
        if (isSelectionType() && !isAnnotationTypeMultiValued()) {
            return Constants.UI_INCLUDE_COLUMN_PATH_SINGLE_SELECTION_TYPE;
        }
        if (isAnnotationTypeMultiValued()) {
            return Constants.UI_INCLUDE_COLUMN_PATH_MULTI_VALUED_TYPE;
        }
        if (isBooleanType()) {
            return Constants.UI_INCLUDE_COLUMN_PATH_BOOLEAN_TYPE;
        }
        if (isLocalDateType() || isLocalDateTimeType()) {
            return Constants.UI_INCLUDE_COLUMN_PATH_DATE_TYPE;
        }
        return null;
    }

    public String getUnit() {
        return unit;
    }

    public String getWatermarkValueForSampleSubTypeDependentAttribute(String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        if (hasSampleTypeSampleForm(type) && isSampleFormDependentAttribute()) {
            if (sampleFormEnum == null) {
                return Messages.get("watermarkSampleFormSelectHint");
            } else if (!isSampleFormDependentAttributeEnabled(type, sampleFormEnum)) {
                return Messages.get("disabled");
            }
        } else if (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) && isQCTypeDependentAttribute()) {
            if (sampleQCTypeEnum == null) {
                return Messages.get("watermarkQCTypeSelectHint");
            } else if (!isQCTypeDependentAttributeEnabled(type, sampleQCTypeEnum)) {
                return Messages.get("disabled");
            }
        }
        return null;
    }

    public String getWatermarkValueHintForSampleSubTypeDependentAttribute(String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        if (hasSampleTypeSampleForm(type) && isSampleFormDependentAttribute() && sampleFormEnum != null && !isSampleFormDependentAttributeEnabled(type, sampleFormEnum)) {
            return Messages.get("watermarkDisabledHint").replace("{0}", getLabel()).replace("{1}", Messages.get("sampleForm")).replace("{2}", sampleFormEnum.getLabel());
        } else if (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) && isQCTypeDependentAttribute() && sampleQCTypeEnum != null && !isQCTypeDependentAttributeEnabled(type, sampleQCTypeEnum)) {
            return Messages.get("watermarkDisabledHint").replace("{0}", getLabel()).replace("{1}", Messages.get("qualityControlType"))
                .replace("{2}", sampleQCTypeEnum.getLabel());
        }
        return null;
    }

    public boolean hasMultiplexIdOrMultiplexId2() {
        return equals(MULTIPLEX_ID) || equals(MULTIPLEX_ID_2);
    }

    public boolean hasSampleAttributeValueChangedListener() {
        return equals(CONCENTRATION_INPUT_QC) || equals(AMOUNT_INPUT) || equals(VOLUME_INPUT) || equals(VOLUME_DILUTION_SAMPLE) || equals(MOLARITY_TARGET) || equals(VOLUME_TARGET) || equals(MOLARITY) || equals(VOLUME_TO_ADD_SAMPLE) || equals(CONCENTRATION) || equals(VOLUME);
    }

    public boolean isAnnotationExtensible() {
        if (isAnnotationType()) {
            switch (this) {
            case SPECIES:
            case COMPOUND_CLASS:
            case LIBRARY_STRATEGY:
            case DIGESTION_PROTOCOL:
            case LABELING_METHOD:
            case SOURCE_TYPE:
                return getIdentityService().hasRoleEnum(RoleEnum.ANNOTATIONMANAGER);
            default:
                return getIdentityService().hasRoleEnum(RoleEnum.USER);
            }
        }
        return false;
    }

    public boolean isAnnotationType() {
        return getClazz().equals(Annotation.class);
    }

    public boolean isAnnotationTypeMultiValued() {
        return isAnnotationType() && getMultiValued();
    }

    public boolean isAnnotationTypeSingleValued() {
        return isAnnotationType() && !getMultiValued();
    }

    public boolean isAttribute(String type, Set<String> types, Set<SampleQCTypeEnum> qcTypes, Set<SampleFormEnum> sampleForms) {
        if (!StringHelper.isEmpty(type)) {
            return isAttribute(type);
        }
        if (types != null) {
            for (String aType : types) {
                if (isAttribute(aType)) {
                    if (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(aType) && qcTypes != null) {
                        for (SampleQCTypeEnum sampleQCTypeEnum : qcTypes) {
                            if (!isQCTypeDependentAttribute() || isQCTypeDependentAttribute() && isQCTypeDependentAttributeEnabled(aType, sampleQCTypeEnum)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    if ((SampleTypeEnum.PROTEOMICS_SERVICES.getLabel().equals(aType) || SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel().equals(aType) || SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION
                        .getLabel().equals(aType) || SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel()
                        .equals(aType)) && sampleForms != null) {
                        for (SampleFormEnum sampleFormEnum : sampleForms) {
                            if (!isSampleFormDependentAttribute() || isSampleFormDependentAttribute() && isSampleFormDependentAttributeEnabled(aType, sampleFormEnum)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAttribute(String type, Set<String> types) {
        if (!StringHelper.isEmpty(type)) {
            return isAttribute(type);
        }
        if (types != null) {
            for (String aType : types) {
                if (isAttribute(aType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAttribute(String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        return (SampleTypeEnum.PROTEOMICS_SERVICES.getLabel().equals(type) || SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel().equals(type) || SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION.getLabel()
            .equals(type) || SampleTypeEnum.GLYCOPROTEIN_ANALYSIS.getLabel().equals(type) || SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel()
            .equals(type)) && isSampleFormDependentAttribute() && isSampleFormDependentAttributeEnabled(type, sampleFormEnum) || SampleTypeEnum.QUALITY_CONTROL
            .getLabel().equals(type) && isQCTypeDependentAttribute() && isQCTypeDependentAttributeEnabled(type, sampleQCTypeEnum) || isAttribute(type);
    }

    public boolean isAttribute(String type) {
        return getTypesRequired().contains(type) || getTypesOptional().contains(type);
    }

    public boolean isAttribute(String type, boolean orderForm, boolean orderConfirmationForm) {
        if (isAttribute(type)) {
            if (orderForm) {
                switch (this) {
                case AGE:
                case AGE_UNIT:
                case INITIAL_TIME_POINT:
                case ORGANISM_PART:
                case SEX:
                    return !(orderConfirmationForm && (SampleTypeEnum.SCINET_HUMAN_PATIENT.getLabel().equals(type) || SampleTypeEnum.SCINET_RODENT_MODEL.getLabel().equals(type)));
                case COMPOUND_CLASS:
                    return !(orderConfirmationForm && SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel().equals(type));
                case CELL_COMPARTMENT:
                    return !(orderConfirmationForm && SampleTypeEnum.METABOLOMICS.getLabel().equals(type));
                case LIBRARY_SELECTION:
                case SEQUENCING_PLATFORM:
                case SIZE:
                    return false;
                case DESCRIPTION:
                    return SampleTypeEnum.METABOLOMICS.getLabel().equals(type) || SampleTypeEnum.CELL_ENGINEERING.getLabel().equals(type) || SampleTypeEnum.CRISPR_SCREEN.getLabel()
                        .equals(type) || SampleTypeEnum.OFF_TARGET_IDENTIFICATION.getLabel().equals(type) || hasSampleTypeSampleForm(type);
                default:
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isAttributeOptional(String type) {
        return getTypesOptional().contains(type);
    }

    public boolean isAttributeRequired(String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        return StringHelper.isNotEmpty(type) && (isSampleFormDependentAttribute() && isSampleFormDependentAttributeRequired(type, sampleFormEnum) ||
            isQCTypeDependentAttribute() && isQCTypeDependentAttributeRequired(type, sampleQCTypeEnum) || getTypesRequired().contains(type));
    }

    public boolean isAttributeRequired(String type) {
        return getTypesRequired().contains(type);
    }

    public boolean isBigDecimalType() {
        return getClazz().equals(BigDecimal.class);
    }

    public boolean isBooleanType() {
        return getClazz().equals(Boolean.class);
    }

    public boolean isCalculatedAttribute(String sampleTypeEnum) {
        return equals(VOLUME_DILUTION_SAMPLE) || equals(VOLUME_DILUTION_WATER) || equals(VOLUME_TO_ADD_EBT) || equals(VOLUME_TO_ADD_SAMPLE) || equals(AMOUNT_TOTAL) && (SampleTypeEnum.NANOPORE_LIBRARY.isOfType(sampleTypeEnum) || SampleTypeEnum.ONT_READY_MADE_LIBRARY.isOfType(sampleTypeEnum));
    }

    public boolean isDoubleType() {
        return getClazz().equals(Double.class);
    }

    public boolean isEmptySampleAttribute(Object value) {
        return value == null || isStringType() && StringHelper.isEmpty((String) value) || getMultiValued() && value instanceof Collection && ((Collection<?>) value).isEmpty();
    }

    public boolean isEnumType() {
        return getClazz().equals(SampleFormEnum.class) || getClazz().equals(SampleQCTypeEnum.class) || getClazz().equals(SampleStatusEnum.class);
    }

    public boolean isFloatType() {
        return getClazz().equals(Float.class);
    }

    public boolean isForContainerManagerOnlyType() {
        return getForEmployeesOnly() != null && getForEmployeesOnly();
    }

    public boolean isIntegerType() {
        return getClazz().equals(Integer.class);
    }

    public boolean isLabeledOrMultiplexed(Boolean multiplexed) {
        return multiplexed != null && multiplexed && hasMultiplexIdOrMultiplexId2();
    }

    public boolean isLocalDateTimeType() {
        return getClazz().equals(LocalDateTime.class);
    }

    public boolean isLocalDateType() {
        return getClazz().equals(LocalDate.class);
    }

    public boolean isLongType() {
        return getClazz().equals(Long.class);
    }

    public boolean isMultiplexed() {
        return equals(MULTIPLEXED) || hasMultiplexIdOrMultiplexId2();
    }

    public boolean isMultiplexedAndRendered(Order order) {
        return isMultiplexed() && (order == null || order.hasSequencingApplicationReadyMadeLibraries() || order.hasServiceTypeReadyMadeLibrariesSequencing() || hasMultiplexIdOrMultiplexId2() && order
            .isCreatedBeforeBfabric10ReleaseDate());
    }

    public boolean isNotMultiplexed(Sample sample) {
        return !hasMultiplexIdOrMultiplexId2() || sample.isNotMultiplexed();
    }

    public boolean isNotMultiplexedButKitSet(Sample sample) {
        return (equals(MULTIPLEX_ID) && sample.getMultiplexKit() != null || equals(MULTIPLEX_ID_2) && sample.getMultiplexKit2() != null) && sample.isNotMultiplexed();
    }

    public boolean isNumericType() {
        return Number.class.isAssignableFrom(getClazz());
    }

    public boolean isQCTypeDependentAttribute() {
        return !getQcTypesRequired().isEmpty() || !getQcTypesOptional().isEmpty();
    }

    public boolean isQCTypeDependentAttributeEnabled(SampleQCTypeEnum sampleQCTypeEnum) {
        return sampleQCTypeEnum != null && (getQcTypesRequired().contains(sampleQCTypeEnum.getLabel()) || getQcTypesOptional().contains(sampleQCTypeEnum.getLabel()));
    }

    public boolean isQCTypeDependentAttributeEnabled(String type, SampleQCTypeEnum sampleQCTypeEnum) {
        return !SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) || isQCTypeDependentAttributeEnabled(sampleQCTypeEnum);
    }

    public boolean isQCTypeDependentAttributeRequired(String type, SampleQCTypeEnum sampleQCTypeEnum) {
        return SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) && sampleQCTypeEnum != null && getQcTypesRequired().contains(sampleQCTypeEnum.getLabel());
    }

    public boolean isSampleFormDependentAttribute() {
        return !getSampleFormsRequired().isEmpty() || !getSampleFormsOptional().isEmpty();
    }

    public boolean isSampleFormDependentAttributeAndOptional(String type, SampleFormEnum sampleFormEnum) {
        return !isSampleFormDependentAttribute() && (SampleTypeEnum.SPATIAL_MS.getLabel().equals(type) && getTypesOptional()
            .contains(SampleTypeEnum.SPATIAL_MS.getLabel()) || SampleTypeEnum.PROTEOMICS_INTERACTION.getLabel().equals(type) && getTypesOptional()
            .contains(SampleTypeEnum.PROTEOMICS_INTERACTION.getLabel()) || SampleTypeEnum.PROTEOMICS_SERVICES.getLabel().equals(type) && getTypesOptional()
            .contains(SampleTypeEnum.PROTEOMICS_SERVICES.getLabel()) || SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel().equals(type) && getTypesOptional()
            .contains(SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel()) || SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION.getLabel().equals(type) && getTypesOptional()
            .contains(SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION.getLabel()) || SampleTypeEnum.GLYCOPROTEIN_ANALYSIS.getLabel().equals(type) && getTypesOptional()
            .contains(SampleTypeEnum.GLYCOPROTEIN_ANALYSIS.getLabel()) || SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel().equals(type) && getTypesOptional()
            .contains(SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel())) || sampleFormEnum != null && getSampleFormsOptional().contains(sampleFormEnum.getLabel());
    }

    public boolean isSampleFormDependentAttributeAndRequired(String type, SampleFormEnum sampleFormEnum) {
        return !isSampleFormDependentAttribute() && (SampleTypeEnum.SPATIAL_MS.getLabel().equals(type) && getTypesRequired()
            .contains(SampleTypeEnum.SPATIAL_MS.getLabel()) || SampleTypeEnum.PROTEOMICS_INTERACTION.getLabel().equals(type) && getTypesRequired()
            .contains(SampleTypeEnum.PROTEOMICS_INTERACTION.getLabel()) || SampleTypeEnum.PROTEOMICS_SERVICES.getLabel().equals(type) && getTypesRequired()
            .contains(SampleTypeEnum.PROTEOMICS_SERVICES.getLabel()) || SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel().equals(type) && getTypesRequired()
            .contains(SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel()) || SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION.getLabel().equals(type) && getTypesRequired()
            .contains(SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION.getLabel()) || SampleTypeEnum.GLYCOPROTEIN_ANALYSIS.getLabel().equals(type) && getTypesRequired()
            .contains(SampleTypeEnum.GLYCOPROTEIN_ANALYSIS.getLabel()) || SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel().equals(type) && getTypesRequired()
            .contains(SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel())) || sampleFormEnum != null && getSampleFormsRequired().contains(sampleFormEnum.getLabel());
    }

    public boolean isSampleFormDependentAttributeEnabled(SampleFormEnum sampleFormEnum) {
        return sampleFormEnum != null && (getSampleFormsRequired().contains(sampleFormEnum.getLabel()) || getSampleFormsOptional().contains(sampleFormEnum.getLabel()));
    }

    public boolean isSampleFormDependentAttributeEnabled(String type, SampleFormEnum sampleFormEnum) {
        return !hasSampleTypeSampleForm(type) || isSampleFormDependentAttributeEnabled(sampleFormEnum);
    }

    public boolean isSampleFormDependentAttributeRequired(String type, SampleFormEnum sampleFormEnum) {
        return hasSampleTypeSampleForm(type) && sampleFormEnum != null && getSampleFormsRequired().contains(sampleFormEnum.getLabel());
    }

    public boolean isSelectionAndNotAnnotationType() {
        return getClazz().equals(SamplePreparationProtocol.class) || getClazz().equals(MultiplexKit.class) || getClazz().equals(Instrument.class) || isEnumType();
    }

    public boolean isSelectionType() {
        return isAnnotationType() || isSelectionAndNotAnnotationType();
    }

    public boolean isSizeGenomeEstimated() {
        return equals(SIZE_GENOME_ESTIMATED);
    }

    public boolean isSizeGenomeEstimatedAndRendered(Order order) {
        return isSizeGenomeEstimated() && order != null && order.getServiceType() != null && order.getServiceType().getName().equals(Constants.SERVICE_TYPE_NAME_LONG_READ_SEQUENCING) && order
            .getSequencingApplication() != null && (order.getSequencingApplication().getName().equals("Genome de novo Sequencing") || order.getSequencingApplication().getName()
            .equals("Genome Resequencing") || order.getSequencingApplication().getName().equals("Methylation Detection"));
    }

    public boolean isStringType() {
        return getClazz().equals(String.class);
    }

    public boolean isSubTypeDependentAttribute(String type) {
        return (SampleTypeEnum.PROTEOMICS_INTERACTION.getLabel().equals(type) || SampleTypeEnum.PROTEOMICS_SERVICES.getLabel().equals(type) || SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel()
            .equals(type)
            || SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION.getLabel().equals(type) || SampleTypeEnum.GLYCOPROTEIN_ANALYSIS.getLabel().equals(type) || SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel()
            .equals(type)) && isSampleFormDependentAttribute() || SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) && isQCTypeDependentAttribute();
    }

    public boolean isSubTypeDependentAttributeEnabled(String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        if ((SampleTypeEnum.PROTEOMICS_INTERACTION.getLabel().equals(type) || SampleTypeEnum.PROTEOMICS_SERVICES.getLabel().equals(type) || SampleTypeEnum.AMINO_ACID_ANALYSIS.getLabel()
            .equals(type) ||
            SampleTypeEnum.BIOMOLECULES_CHARACTERIZATION.getLabel().equals(type) || SampleTypeEnum.GLYCOPROTEIN_ANALYSIS.getLabel().equals(type) || SampleTypeEnum.PROTEOMICS_USER_LAB.getLabel()
            .equals(type)) && isSampleFormDependentAttribute()) {
            return isSampleFormDependentAttributeEnabled(type, sampleFormEnum);
        }
        if (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(type) && isQCTypeDependentAttribute()) {
            return isQCTypeDependentAttributeEnabled(type, sampleQCTypeEnum);
        }
        return false;
    }

    public boolean isSubTypeDependentAttributeRequired(String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        return isSampleFormDependentAttributeRequired(type, sampleFormEnum) || isQCTypeDependentAttributeRequired(type, sampleQCTypeEnum);
    }

    public boolean isTypeDecimal() {
        return getClazz().equals(BigDecimal.class);
    }

    public boolean isTypeFloat() {
        return getClazz().equals(Double.class) || getClazz().equals(Float.class);
    }

    public boolean isTypeInteger() {
        return getClazz().equals(BigInteger.class) || getClazz().equals(Long.class) || getClazz().equals(Integer.class) || getClazz().equals(Short.class);
    }

    public String toString(String type) {
        String ret = null;
        if (isAttribute(type)) {
            ret = getClazz().getSimpleName();
            if (isAttributeRequired(type)) {
                ret += " REQUIRED";
            }
        }
        return ret;
    }
}