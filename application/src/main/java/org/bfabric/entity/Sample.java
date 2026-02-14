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

package org.bfabric.entity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleStatusEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.SampleUserDecisionEnum;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.AnnotationService;
import org.bfabric.service.MultiplexIdService;
import org.bfabric.service.OrderItemService;
import org.bfabric.service.PlateService;
import org.bfabric.service.RunService;
import org.bfabric.service.SampleService;
import org.bfabric.service.SampleTypeService;
import org.bfabric.service.WorkunitService;
import org.bfabric.util.AJAX;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.CustomAttributeColumn;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadSample;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "Sample.findByContainerAndType", query = "SELECT a FROM Sample a WHERE a.type = :type and (a.container = :container OR a.container in (SELECT c FROM Container c WHERE c.project = :container)) ORDER BY a.modified DESC")
@NamedQuery(name = "Sample.findByContainerAndTypes", query = "SELECT a FROM Sample a WHERE a.type IN (:types) AND a.container = :container")
@NamedQuery(name = "Sample.findByRunId", query = "SELECT DISTINCT sample FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE a.run.id = :runId")
@NamedQuery(name = "Sample.countByContainerId", query = "SELECT COUNT(a) FROM Sample a WHERE container.id = :containerId")
@NamedQuery(name = "Sample.findReplacementsByContainerId", query = "SELECT DISTINCT replacement FROM Sample a join a.replacements replacement WHERE replacement.container.id = :containerId ORDER BY replacement.tubeIdPadded")
@NamedQuery(name = "Sample.findByContainerIdAndUserDecisionRequired", query = "SELECT a FROM Sample a WHERE a.container.id = :containerId and a.userDecision = org.bfabric.enums.SampleUserDecisionEnum.REQUIRED ORDER BY a.tubeIdPadded")
public class Sample extends AbstractContainerDependentEntity implements Indexable {

    private static final long serialVersionUID = 1;

    @Transient
    private final Set<Sample> initialParentSamplesOfUserMultiplex = new HashSet<>();

    @Transient
    final private Map<String, String> sampleAttributeEnumNameValueMap = new HashMap<>();

    @DecimalMin("0")
    @XmlElement
    private BigDecimal age;

    @Size(max = 9)
    @XmlElement
    private String ageUnit;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal amountEluted;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal amountInput;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal amountTotal;

    @Size(max = 1024)
    @XmlElement
    private String ampliconSequence;

    @Transient
    private Set<Sample> ancestors;

    @Size(max = 128)
    @XmlElement
    private String arrayDesignName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiascaleid")
    @XmlIDREF
    private Annotation asiaScale;

    @Transient
    private boolean assignSampleValuesChanged = false;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal averageSizeInRange;

    @Size(max = 128)
    @XmlElement
    private String baitId;

    @Size(max = 128)
    @XmlElement
    private String beadsType;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal bias;

    @Size(max = 32)
    @XmlElement
    private String block;

    @Size(max = 2048)
    @XmlElement
    private String buffer;

    @Size(max = 64)
    @XmlElement
    private String cellCompartment;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal cellConcentration;

    @Size(max = 64)
    @XmlElement
    private String cellLine;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal cellNumbers;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal cellSize;

    @Size(max = 64)
    @XmlElement
    private String cellType;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal cellViability;

    @ManyToMany
    @JoinTable(name = "chargesample", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "chargeid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Charge> charges = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "samplechemicalmodification", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "chemicalmodificationid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "chemicalModification")
    private Set<Annotation> chemicalModifications = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "sampleparent", joinColumns = @JoinColumn(name = "parentid"), inverseJoinColumns = @JoinColumn(name = "sampleid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Sample> children = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = { CascadeType.MERGE, CascadeType.REMOVE })
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<SampleComment> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compoundclassid")
    @XmlIDREF
    private Annotation compoundClass;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal concentration;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal concentrationInRange;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal concentrationInputQc;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal concentrationLoading;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal concentrationMolar;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal concentrationMolarInRange;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal concentrationProtein;

    @Size(max = 256)
    @XmlElement
    private String condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controlsampleid")
    @XmlIDREF
    private ControlSample controlSample;

    @Transient
    private Sample controlSampleParent;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal correctionRate;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal coverage;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal cq;

    @Size(max = 256)
    @XmlElement
    private String crisprLibrary;

    @Transient
    private Set<Sample> descendants;

    @Size(max = 64)
    @XmlElement
    private String developmentStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digestionprotocolid")
    @XmlElement
    private Annotation digestionProtocol;

    @Size(max = 256)
    @XmlElement
    private String dilution;

    @Size(max = 64)
    @XmlElement
    private String diseaseState;

    @Size(max = 1024)
    @XmlElement
    private String dmxFlag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dsodnid")
    @XmlIDREF
    private Annotation dsOdn;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal dv200;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effectortypeid")
    @XmlIDREF
    private Annotation effectorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embeddingMediumId")
    @XmlIDREF
    private Annotation embeddingMedium;

    @ManyToMany
    @JoinTable(name = "sampleenzyme", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "enzymeid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "enzyme")
    private Set<Annotation> enzymes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expressionSystemId")
    @XmlIDREF
    private Annotation expressionSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extractionprotocolid")
    @XmlIDREF
    private Annotation extractionProtocol;

    @Size(max = 64)
    @Column(name = "extractionprotocol")
    @XmlElement
    private String extractionProtocolString;

    @Transient
    private Set<Sample> family;

    @Transient
    private Long familyId;

    @Size(max = 256)
    @XmlElement
    private String fastqScreen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixationId")
    @XmlIDREF
    private Annotation fixation;

    @XmlElement
    private Boolean fraction;

    @Size(max = 64)
    @XmlElement
    private String geneticModification;

    @Size(max = 64)
    @XmlElement
    private String genomicCoordinates;

    @Size(max = 64)
    @XmlElement
    private String genotype;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupingvarid")
    @XmlIDREF
    private Annotation groupingVar;

    @Size(max = 64)
    @XmlElement
    private String growthConditions;

    @Size(max = 256)
    @XmlElement
    private String guideName;

    @Size(max = 1024)
    @XmlElement
    private String guideSequence;

    @Size(max = 64)
    @XmlElement
    private String hybridizationProtocol;

    @Size(max = 64)
    @XmlElement
    private String immunoPrecipitationTarget;

    @OneToMany(mappedBy = "sample")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ImportResource> importResources = new HashSet<>();

    @Size(max = 64)
    @XmlElement
    private String individualId;

    @Transient
    private boolean initialParentSamplesOfUserMultiplexInitialized = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initialtimepointid")
    @XmlIDREF
    private Annotation initialTimePoint;

    @Transient
    private Sample inputQcSample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentid")
    @XmlIDREF
    private Instrument instrument;

    @Size(max = 256)
    @XmlElement
    private String instrumentMethod;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal integrityNumber;

    @ManyToMany
    @JoinTable(name = "sampleinternalstandard", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "internalstandardid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "internalStandard")
    private Set<Annotation> internalStandards = new HashSet<>();

    @Size(max = 256)
    @XmlElement
    private String irts;

    @Transient
    private Boolean isRenderedTree;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal labelAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labelingmethodid")
    @XmlElement
    private Annotation labelingMethod;

    @Size(max = 128)
    @XmlElement
    private String labelingProtocol;

    @Column(name = "libraryprotocol")
    @Size(max = 64)
    @XmlElement
    private String libraryProtocol;

    @Size(max = 64)
    @XmlElement
    private String librarySelection;

    @Size(max = 64)
    @XmlElement
    private String libraryStrategy;

    @Size(max = 256)
    @XmlElement
    private String lotInformation;

    @Size(max = 256)
    @XmlElement
    private String lysisBuffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matrixId")
    @XmlIDREF
    private Annotation matrix;

    @Size(max = 64)
    @XmlElement
    private String media;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal molarity;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal molarityFmol;

    @Transient
    private Sample molaritySample;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal molarityTarget;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal molecularWeight;

    @Size(max = 64)
    @XmlElement
    private String multiplexId;

    @Transient
    private String multiplexId1Old;

    @Size(max = 64)
    @XmlElement
    private String multiplexId2;

    @Size(max = 64)
    @XmlElement
    private String multiplexId2Dmx;

    @Transient
    private String multiplexId2NameWithSequence;

    @Transient
    private String multiplexId2NameWithoutSequence;

    @Transient
    private String multiplexId2Old;

    @Transient
    private Sample multiplexIdConflictMultiplexedSample;

    @Size(max = 64)
    @XmlElement
    private String multiplexIdDmx;

    @Transient
    private String multiplexIdNameWithSequence;

    @Transient
    private String multiplexIdNameWithoutSequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplexkitid")
    @XmlIDREF
    private MultiplexKit multiplexKit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplexkit2id")
    @XmlIDREF
    private MultiplexKit multiplexKit2;

    @XmlElement
    private Boolean multiplexed;

    @XmlElement
    private Boolean multiplexedByUser;

    @Transient
    private String namePrefix;

    @DecimalMin("0")
    @XmlElement
    private Integer numberOfCellsLoaded;

    @Min(0)
    @XmlElement
    private Integer numberOfCycles;

    @Min(0)
    private Long oldExtractId;

    @Transient
    private String oldNamePrefix;

    @Transient
    private List<SampleAttributeEnum> oldSampleAttributeEnums;

    @Min(0)
    private Long oldSampleId;

    @Transient
    private String oldUserSampleInMultiplexName;

    @Transient
    private String oldUserSampleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onSlideModificationId")
    @XmlIDREF
    private Annotation onSlideModification;

    @OneToMany(mappedBy = "sample")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrderItem> orderItems = new HashSet<>();

    @Transient
    private List<Order> orders;

    @Size(max = 256)
    @XmlElement
    private String organism;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organismpartid")
    @XmlIDREF
    private Annotation organismPart;

    @Transient
    private List<Sample> parentSamplesOfUserMultiplex = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "sampleparent", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "parentid"))
    @XmlIDREF
    @XmlElement(name = "parent")
    private Set<Sample> parents = new HashSet<>();

    @Transient
    private List<Plate> plates;

    @Transient
    private Long platesCount;

    @Size(max = 256)
    @XmlElement
    private String preTreatment;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal proteinAmount;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal purityA260230;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal purityA260280;

    @Transient
    private Set<Sample> qcChildrenWithNonEmptyConcentration;

    @XmlElement
    private Boolean qcPassed;

    @Transient
    private Set<Plate> qcPlates;

    @Transient
    private Set<Sample> qcSiblingsWithNonEmptyConcentration;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal qpcr;

    @Enumerated(EnumType.STRING)
    @XmlElement
    private SampleQCTypeEnum qualityControlType;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal qubit;

    @XmlElement
    private Boolean reMultiplexed;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal readCount;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal readCountTotal;

    @Transient
    private transient XMLRequestParameterReadSample readRequestParameter;

    @OneToMany(mappedBy = "replaces")
    @OrderBy("id DESC")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Sample> replacements = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacesid")
    @XmlIDREF
    private Sample replaces;

    @OneToMany(mappedBy = "sample")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Resource> resources = new HashSet<>();

    @Transient
    private Set<Sample> roots;

    @OneToMany(mappedBy = "sample", cascade = { CascadeType.REMOVE })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<RunSample> runSamples = new HashSet<>();

    @Transient
    private RunUnitLane runUnitLane;

    @ManyToMany
    @JoinTable(name = "rununitlanesample", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "rununitlaneid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "rununitlane")
    private Set<RunUnitLane> runUnitLanes = new HashSet<>();

    @Transient
    private List<Run> runs;

    @Transient
    private Long runsCount;

    @Enumerated(EnumType.STRING)
    @XmlElement
    private SampleFormEnum sampleForm;

    @OneToMany(mappedBy = "sample")
    @OrderBy("id asc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<SamplePlatePosition> samplePlatePositions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "samplepreparationprotocolid")
    @XmlIDREF
    private SamplePreparationProtocol samplePreparationProtocol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampletypeid")
    @NotNull
    @XmlIDREF
    private SampleType sampleType;

    @XmlElement
    private LocalDateTime samplingDate;

    @Size(max = 64)
    @XmlElement
    private String scanningProtocol;

    @Transient
    private MultiplexId selectedMultiplexId;

    @Transient
    private MultiplexId selectedMultiplexId2;

    @Transient
    private List<MultiplexId> selectionMultiplexId2s;

    @Transient
    private List<MultiplexId> selectionMultiplexIds;

    @ManyToMany
    @JoinTable(name = "sampleseparationtechnique", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "separationtechniqueid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "separationTechnique")
    private Set<Annotation> separationTechniques = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequencingmethodid")
    @XmlIDREF
    private Annotation sequencingMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequencingmodeid")
    @XmlIDREF
    private Annotation sequencingMode;

    @Size(max = 64)
    @XmlElement
    private String sequencingPlatform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequencingprimerid")
    @XmlIDREF
    private Annotation sequencingPrimer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sexid")
    @XmlIDREF
    private Annotation sex;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal size;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal sizeAverage;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal sizeGenomeEstimated;

    @Size(max = 256)
    @XmlElement
    private String sizeRange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slideTypeId")
    @XmlIDREF
    private Annotation slideType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sourcetypeid")
    @XmlIDREF
    private Annotation sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "speciesid")
    @XmlIDREF
    private Annotation species;

    @Transient
    private String speciesString;

    @Enumerated(EnumType.STRING)
    @XmlElement
    private SampleStatusEnum status;

    @Size(max = 64)
    @XmlElement
    private String strain;

    @Size(max = 64)
    @XmlElement
    private String subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surfaceid")
    @XmlIDREF
    private Annotation surface;

    @Size(max = 256)
    @XmlElement
    private String tissue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatmentid")
    @XmlIDREF
    private Annotation treatment;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal ts;

    @Size(max = 32)
    @XmlElement
    private String tubeId;

    @Column(updatable = false, insertable = false)
    private String tubeIdPadded;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String type;

    @Enumerated(EnumType.STRING)
    @XmlElement
    private SampleUserDecisionEnum userDecision;

    @Transient
    private String userSampleInMultiplexName;

    @Transient
    private String userSampleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vectorid")
    @XmlIDREF
    private Annotation vector;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volume;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeDilutionSample;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeDilutionWater;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeEluted;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeInput;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeLysisBuffer;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeMeasured;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeReaction;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeTarget;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeToAddEbt;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal volumeToAddSample;

    @ManyToMany
    @JoinTable(name = "workflowstepsample", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "workflowstepid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowStep> workflowSteps = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workflowsample", joinColumns = @JoinColumn(name = "sampleid"), inverseJoinColumns = @JoinColumn(name = "workflowid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workflow> workflows = new HashSet<>();

    @Transient
    private List<Workunit> workunits;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal yield;

    public Sample() {
    }

    public void assignName(String namePrefix, Map<Long, Long> containerIdSampleNameSuffixMap) {
        if (!isManaged() && containerIdSampleNameSuffixMap != null) {
            Long containerIdSampleNameSuffix = containerIdSampleNameSuffixMap.get(getContainer().getId());
            if (containerIdSampleNameSuffix == null) {
                containerIdSampleNameSuffix = getEntityService().getNextSampleNameSuffix(getContainer().getId(), namePrefix);
                if (containerIdSampleNameSuffix == null) {
                    containerIdSampleNameSuffix = 1L;
                }
            }
            setName(namePrefix + containerIdSampleNameSuffix);
            containerIdSampleNameSuffixMap.put(getContainer().getId(), containerIdSampleNameSuffix + 1);
        }
    }

    public void assignValidName() {
        int i = 1;
        String newName = getName();
        String postfix;
        while (!CDI.current().select(SampleService.class).get().isValidName(newName, Collections.singleton(getId()), getContainer().getId())) {
            postfix = "_" + i++;
            newName = (getName().length() <= 256 - postfix.length() ? getName() : getName().substring(0, 256 - postfix.length())) + postfix;
        }
        setName(newName);
    }

    public void assignValidName(Set<String> names) {
        int i = 1;
        String newName = getName();
        String postfix;
        Set<String> existingNames = new HashSet<>(names);
        while (!CDI.current().select(SampleService.class).get().isValidName(newName, Collections.singleton(getId()), getContainer().getId()) || existingNames.contains(newName)) {
            postfix = "_" + i++;
            newName = (getName().length() <= 256 - postfix.length() ? getName() : getName().substring(0, 256 - postfix.length())) + postfix;
        }
        setName(newName);
    }

    public void booleanTypeValueChanged(ValueChangeEvent event) {
        String sampleAttributeEnumName = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("sampleAttributeEnumName");
        if (sampleAttributeEnumName != null) {
            if (SampleAttributeEnum.QC_PASSED.getName().equals(sampleAttributeEnumName) && event.getNewValue() != null && ((Boolean) event.getNewValue())) {
                setStatus(null);
                AJAX.update(Constants.EDIT + ":" + Constants.STATUS);
            } else if (SampleAttributeEnum.MULTIPLEXED.getName().equals(sampleAttributeEnumName)) {
                if (!isMultiplexedType()) {
                    setMultiplexedByUser(event.getNewValue() != null && ((Boolean) event.getNewValue()) ? true : null);
                }
                AJAX.update(Constants.EDIT + ":parents");
                AJAX.update(Constants.EDIT + ":typeSpecific");
            }
        }
    }

    public void calculateAmountTotal() {
        if (getConcentration() != null && getVolume() != null) {
            setAmountTotal(BigDecimal.valueOf(NumberUtils.roundToDecimals(getConcentration().doubleValue() * getVolume().doubleValue() / 1000)));
        } else {
            setAmountTotal(null);
        }
        AJAX.update("@this", Constants.EDIT + ":amountTotal");
    }

    public void calculateEbtVolumeToAdd() {
        if (getVolumeTarget() != null && getVolumeToAddSample() != null) {
            setVolumeToAddEbt(BigDecimal.valueOf(NumberUtils.roundToDecimals(getVolumeTarget().subtract(getVolumeToAddSample()).doubleValue())));
            if (!(getVolumeToAddEbt().compareTo(BigDecimal.ZERO) > 0)) {
                setVolumeToAddEbt(BigDecimal.ZERO);
            }
        } else {
            setVolumeToAddEbt(null);
        }
        AJAX.update("@this", Constants.EDIT + "volumeToAddEbt");
    }

    public void calculateSampleVolumeAndEbtVolumeToAdd() {
        if (getMolarityTarget() != null && getVolumeTarget() != null && getMolarity() != null && getMolarity().compareTo(BigDecimal.ZERO) > 0) {
            setVolumeToAddSample(BigDecimal.valueOf(NumberUtils.roundToDecimals(getMolarityTarget().doubleValue() * getVolumeTarget().doubleValue() / getMolarity().doubleValue())));
        } else {
            setVolumeToAddSample(null);
        }
        AJAX.update("@this", Constants.EDIT + ":volumeToAddSample");
        calculateEbtVolumeToAdd();
    }

    public void calculateVolumeDilutionSampleAndWater() {
        if (getAmountInput() != null && getConcentrationInputQc() != null && getConcentrationInputQc().compareTo(BigDecimal.ZERO) > 0) {
            setVolumeDilutionSample(BigDecimal.valueOf(NumberUtils.roundToDecimals(getAmountInput().doubleValue() / getConcentrationInputQc().doubleValue())));
        } else {
            setVolumeDilutionSample(null);
        }
        AJAX.update("@this", Constants.EDIT + ":volumeDilutionSample");
        calculateVolumeDilutionWater();
    }

    public void calculateVolumeDilutionWater() {
        if (getVolumeInput() != null && getVolumeDilutionSample() != null) {
            setVolumeDilutionWater(BigDecimal.valueOf(NumberUtils.roundToDecimals(getVolumeInput().subtract(getVolumeDilutionSample()).doubleValue())));
            if (!(getVolumeDilutionWater().compareTo(BigDecimal.ZERO) > 0)) {
                setVolumeDilutionWater(BigDecimal.ZERO);
            }
        } else {
            setVolumeDilutionWater(null);
        }
        AJAX.update("@this", Constants.EDIT + ":volumeDilutionWater");
    }

    public void changeNumberOfSamplesInMultiplex(ValueChangeEvent event) {
        Integer oldValue = getParentSamplesOfUserMultiplex().size();
        Integer newValue = null;
        if (event.getNewValue() != null) {
            newValue = Integer.valueOf(event.getNewValue().toString());
        }

        // The default value is always 1, so the old value and new value can never be null.
        if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
            List<Sample> aParentSamplesOfUserMultiplex = getParentSamplesOfUserMultiplex();
            if (oldValue > newValue) {
                for (int i = newValue; i < aParentSamplesOfUserMultiplex.size(); i++) {
                    getParents().remove(aParentSamplesOfUserMultiplex.get(i));
                }
            } else {
                // oldValue < newValue
                for (int i = oldValue + 1; i <= newValue; i++) {
                    aParentSamplesOfUserMultiplex.add(createTemporaryParentOfUserSampleInMultiplex(Constants.EMPTY_STRING));
                }
            }

            // Adapt all the name prefixes based on the name or the tube id of the multiplexed sample with the correct padding, so they can be ordered by their name prefixes.
            for (int i = 0; i < newValue; i++) {
                aParentSamplesOfUserMultiplex.get(i).setNamePrefix(createNamePrefixForParentOfUserSampleInMultiplex(i + 1, newValue));
            }

            setChanged(true);
            recomputeParentSamplesOfUserMultiplex();
        }
    }

    public boolean checkMultiplexIdsOnlyOfPattern(Pattern pattern) {
        if (!isMultiplexIdAndMultiplexId2Empty() && pattern != null) {
            boolean onlyACTG = true;
            if (StringHelper.isNotEmpty(getMultiplexId())) {
                onlyACTG = pattern.matcher(getMultiplexId()).matches();
            }
            if (StringHelper.isNotEmpty(getMultiplexId2())) {
                onlyACTG = onlyACTG && pattern.matcher(getMultiplexId2()).matches();
            }
            return onlyACTG;
        }
        return false;
    }

    @Override
    public Sample clone() throws CloneNotSupportedException {
        Sample cloned = (Sample) super.clone();
        return cloneAttributes(cloned, false);
    }

    public Sample clone(boolean keepParents) throws CloneNotSupportedException {
        Sample cloned = (Sample) super.clone();
        return cloneAttributes(cloned, keepParents);
    }

    private Sample cloneAttributes(Sample clone, boolean keepParents) {
        clone.tubeId = null;
        clone.replaces = null;
        clone.userDecision = null;
        clone.parents = new HashSet<>();
        if (keepParents) {
            clone.parents.addAll(getParents());
        }
        clone.children = new HashSet<>();
        clone.comments = new HashSet<>();

        clone.replacements = new HashSet<>();
        clone.chemicalModifications = new HashSet<>();
        clone.enzymes = new HashSet<>();
        clone.internalStandards = new HashSet<>();
        clone.separationTechniques = new HashSet<>();
        clone.importResources = new HashSet<>();
        clone.orderItems = new HashSet<>();
        clone.charges = new HashSet<>();
        clone.resources = new HashSet<>();
        clone.runSamples = new HashSet<>();
        clone.runUnitLanes = new HashSet<>();
        clone.samplePlatePositions = new HashSet<>();
        clone.workflows = new HashSet<>();
        clone.workflowSteps = new HashSet<>();
        if (chemicalModifications != null && !chemicalModifications.isEmpty()) {
            clone.chemicalModifications.addAll(chemicalModifications);
        }
        if (enzymes != null && !enzymes.isEmpty()) {
            clone.enzymes.addAll(enzymes);
        }
        if (internalStandards != null && !internalStandards.isEmpty()) {
            clone.internalStandards.addAll(internalStandards);
        }
        if (separationTechniques != null && !separationTechniques.isEmpty()) {
            clone.separationTechniques.addAll(separationTechniques);
        }
        ClassHelper.initializeFullObject(clone);
        return clone;
    }

    /**
     * Clone the child hierarchy and build up references between existing and cloned samples.
     * Note: Use this method only for cloning (not moving)!
     */
    public Sample cloneChildren(Sample origin) throws CloneNotSupportedException {
        Set<Sample> clonedChildren = new HashSet<>();
        for (Sample child : getChildren()) {
            clonedChildren.add(getId() > 0 ? child.cloneChildren(this) : child.cloneChildren(origin));
        }
        if (getId() > 0) {
            initClone();
            getClone().setTubeId(null);
            getClone().assignValidName();
            getClone().setChildren(clonedChildren);
            getClone().setParents(new HashSet<>(getParents()));
            getClone().getParents().remove(origin);
        } else {
            setChildren(clonedChildren);
            setParents(new HashSet<>(getParents()));
            getParents().remove(origin);
        }
        return getClone();
    }

    /**
     * Clone the parent hierarchy and build up references between existing and cloned samples.
     * Note: Use this method only for cloning (not moving)!
     */
    private Sample cloneParents(Container target, Set<Sample> excludeFromCloning) throws CloneNotSupportedException {
        Set<Sample> clonedParents = new HashSet<>();
        for (Sample parent : getParents()) {
            if (!excludeFromCloning.contains(parent)) {
                clonedParents.add(parent.cloneParents(target, excludeFromCloning));
            }
        }
        initClone();
        if (target != null) {
            getClone().setContainer(target);
        }
        getClone().setTubeId(null);
        getClone().assignValidName();
        getClone().setParents(clonedParents);
        return getClone();
    }

    public void computeDescendants(Set<Sample> descendants) {
        if (descendants == null) {
            descendants = new HashSet<>();
        }
        for (Sample child : getChildren()) {
            if (!descendants.contains(child)) {
                child.computeDescendants(descendants);
            }
        }
    }

    public Set<Sample> computeFamily(boolean all) {
        Set<Sample> family = new HashSet<>();
        for (Sample root : getRoots(all)) {
            family.add(root);
            root.getDescendants(family);
        }
        return family;
    }

    public Set<Sample> computeFamily() {
        return computeFamily(false);
    }

    public Set<Sample> computeRoots(boolean all) {
        Set<Sample> computeRoots = new HashSet<>();
        for (Sample sample : getParents()) {
            if (all || sample.getContainer().equals(getContainer())) {
                if (sample.getParents().isEmpty()) {
                    computeRoots.add(sample);
                } else {
                    computeRoots.addAll(sample.computeRoots(all));
                }
            }
        }
        return computeRoots;
    }

    public Sample createChildSample() {
        Sample childSample = new Sample();
        childSample.setContainer(getContainer());
        childSample.getParents().add(this);
        return childSample;
    }

    public Sample createChildSample(SampleType childSampleType, String childName, String childTubeId, boolean assignValidName) {
        if (childSampleType != null) {
            Sample childSample = createChildSample();
            childSample.setSampleType(childSampleType);
            childSample.setTubeId(childTubeId);
            childSample.setName(childName);
            if (assignValidName) {
                childSample.assignValidName();
            }
            return childSample;
        }
        return null;
    }

    public Sample createChildSampleOnPlate(SampleType aSampleType) {
        return createChildSample(aSampleType, null, getTubeId(), false);
    }

    public Sample createChildSampleOnRun(SampleType aSampleType, String aMultiplexIdDmx, String aMultiplexId2Dmx) {
        Sample childSample = createChildSample(aSampleType, null, null, false);
        if (aSampleType != null) {
            if (SampleAttributeEnum.MULTIPLEX_ID_DMX.isAttribute(childSample.getType())) {
                childSample.setMultiplexIdDmx(StringHelper.isEmpty(aMultiplexIdDmx) ? getMultiplexId() : aMultiplexIdDmx);
            }
            if (SampleAttributeEnum.MULTIPLEX_ID_2_DMX.isAttribute(childSample.getType())) {
                childSample.setMultiplexId2Dmx(StringHelper.isEmpty(aMultiplexId2Dmx) ? getMultiplexId2() : aMultiplexId2Dmx);
            }
        }
        return childSample;
    }

    public String createNamePrefixForParentOfUserSampleInMultiplex(int sampleNumber, int numberOfSamples) {
        return (getTubeId() != null ? getTubeId() : getName()) + "_" + StringUtils.leftPad(String.valueOf(sampleNumber), (int) (Math.log10(numberOfSamples) + 1), "0");
    }

    public Sample createTemporaryParentOfUserSampleInMultiplex(String aNamePrefix) {
        Sample parentSample = null;
        if (aNamePrefix != null) {
            parentSample = new Sample();
            parentSample.setContainer(getContainer());
            parentSample.setSampleType(CDI.current().select(SampleTypeService.class).get().getSampleTypeByName(SampleTypeEnum.USER_LIBRARY_IN_POOL.getLabel()));
            parentSample.setNamePrefix(aNamePrefix);
            getParents().add(parentSample);
        }
        return parentSample;
    }

    public String formatName(String sampleName) {
        String formattedSampleName = StringHelper.format(sampleName);
        return StringHelper.isNotEmpty(formattedSampleName) ? formattedSampleName.replaceAll("[\\s]+", "_")
            .replaceAll("[^" + Constants.SAMPLE_NAME_CHARACTERS + "]", Constants.EMPTY_STRING) : null;
    }

    public BigDecimal getAge() {
        return age;
    }

    public String getAgeUnit() {
        return ageUnit;
    }

    public BigDecimal getAmountEluted() {
        return amountEluted;
    }

    public BigDecimal getAmountInput() {
        return amountInput;
    }

    public BigDecimal getAmountTotal() {
        return amountTotal;
    }

    public String getAmpliconSequence() {
        return ampliconSequence;
    }

    public Set<Sample> getAncestors(Set<Sample> ancestors) {
        for (Sample sample : getParents()) {
            if (!ancestors.contains(sample)) {
                ancestors.add(sample);
                sample.getAncestors(ancestors);
            }
        }
        return ancestors;
    }

    public Set<Sample> getAncestors() {
        if (ancestors == null) {
            ancestors = new HashSet<>();
            ancestors = getAncestors(ancestors);
        }
        return ancestors;
    }

    public String getArrayDesignName() {
        return arrayDesignName;
    }

    public Annotation getAsiaScale() {
        return asiaScale;
    }

    public BigDecimal getAverageSizeInRange() {
        return averageSizeInRange;
    }

    public String getBaitId() {
        return baitId;
    }

    public String getBeadsType() {
        return beadsType;
    }

    public BigDecimal getBias() {
        return bias;
    }

    public String getBlock() {
        return block;
    }

    public String getBuffer() {
        return buffer;
    }

    public String getCalculatedAttributeStyle(SampleAttributeEnum sampleAttributeEnum) {
        if (sampleAttributeEnum != null && sampleAttributeEnum.isCalculatedAttribute(getType())) {
            if (getType().equals(SampleTypeEnum.ILLUMINA_LIBRARY.getLabel())) {
                // Calculated values from the initial and final qc.
                if (sampleAttributeEnum.equals(SampleAttributeEnum.VOLUME_DILUTION_SAMPLE) && getVolumeDilutionSample() != null && !(getVolumeDilutionSample()
                    .compareTo(BigDecimal.ZERO) > 0) || sampleAttributeEnum.equals(SampleAttributeEnum.VOLUME_DILUTION_WATER) && getVolumeDilutionWater() != null && !(getVolumeDilutionWater()
                    .compareTo(BigDecimal.ZERO) > 0) || sampleAttributeEnum.equals(SampleAttributeEnum.VOLUME_TO_ADD_EBT) && getVolumeToAddEbt() != null && !(getVolumeToAddEbt()
                    .compareTo(BigDecimal.ZERO) > 0) || sampleAttributeEnum.equals(SampleAttributeEnum.VOLUME_TO_ADD_SAMPLE) && getVolumeToAddSample() != null && getVolumeToAddSample()
                    .compareTo(BigDecimal.ONE) < 0) {
                    return Constants.BACKGROUND_COLOR_CALCULATED_SAMPLE_ATTRIBUTE_WARNING;
                }
            } else if ((getType().equals(SampleTypeEnum.NANOPORE_LIBRARY.getLabel()) || getType().equals(SampleTypeEnum.ONT_READY_MADE_LIBRARY.getLabel()) || getType().equals(SampleTypeEnum.PACBIO_LIBRARY.getLabel())) && getAmountTotal() != null && !(getAmountTotal().compareTo(BigDecimal.ZERO) > 0)) {
                return Constants.BACKGROUND_COLOR_CALCULATED_SAMPLE_ATTRIBUTE_WARNING;
            }
        }
        return Constants.EMPTY_STRING;
    }

    public String getCellCompartment() {
        return cellCompartment;
    }

    public BigDecimal getCellConcentration() {
        return cellConcentration;
    }

    public String getCellLine() {
        return cellLine;
    }

    public BigDecimal getCellNumbers() {
        return cellNumbers;
    }

    public BigDecimal getCellSize() {
        return cellSize;
    }

    public String getCellType() {
        return cellType;
    }

    public BigDecimal getCellViability() {
        return cellViability;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public List<Annotation> getChemicalModifications() {
        return CollectionHelper.asList(chemicalModifications);
    }

    public Set<Annotation> getChemicalModificationsAsSet() {
        return chemicalModifications;
    }

    public Set<Sample> getChildren() {
        return children;
    }

    public List<Sample> getChildrenAsList() {
        return CollectionHelper.asList(children);
    }

    public List<Sample> getChildrenFiltered(String filterString) {
        List<Sample> childrenFiltered = new ArrayList<>();
        if (StringHelper.isEmpty(filterString)) {
            childrenFiltered.addAll(getChildren());
        } else {
            for (Sample parent : getChildren()) {
                if (parent.getName().contains(filterString) || parent.getIdString().contains(filterString)) {
                    childrenFiltered.add(parent);
                }
            }
        }
        return childrenFiltered;
    }

    @Override
    public Sample getClone() {
        return (Sample) super.getClone();
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.SAMPLE_COMMENT;
    }

    public Set<SampleComment> getComments() {
        return comments;
    }

    public Annotation getCompoundClass() {
        return compoundClass;
    }

    public BigDecimal getConcentration() {
        return concentration;
    }

    public BigDecimal getConcentrationInRange() {
        return concentrationInRange;
    }

    public BigDecimal getConcentrationInputQc() {
        return concentrationInputQc;
    }

    public BigDecimal getConcentrationLoading() {
        return concentrationLoading;
    }

    public BigDecimal getConcentrationMolar() {
        return concentrationMolar;
    }

    public BigDecimal getConcentrationMolarInRange() {
        return concentrationMolarInRange;
    }

    public BigDecimal getConcentrationProtein() {
        return concentrationProtein;
    }

    public String getCondition() {
        return condition;
    }

    public ControlSample getControlSample() {
        return controlSample;
    }

    public Sample getControlSampleParent() {
        return controlSampleParent;
    }

    public BigDecimal getCorrectionRate() {
        return correctionRate;
    }

    public BigDecimal getCoverage() {
        return coverage;
    }

    public BigDecimal getCq() {
        return cq;
    }

    public String getCrisprLibrary() {
        return crisprLibrary;
    }

    public Set<Sample> getDescendants() {
        if (descendants == null) {
            descendants = new HashSet<>();
            computeDescendants(descendants);
        }
        return descendants;
    }

    public Set<Sample> getDescendants(Set<Sample> descendants) {
        for (Sample sample : getChildren()) {
            if (!descendants.contains(sample)) {
                descendants.add(sample);
                sample.getDescendants(descendants);
            }
        }
        return descendants;
    }

    public String getDevelopmentStage() {
        return developmentStage;
    }

    public Annotation getDigestionProtocol() {
        return digestionProtocol;
    }

    public String getDilution() {
        return dilution;
    }

    public String getDiseaseState() {
        return diseaseState;
    }

    public String getDmxFlag() {
        return dmxFlag;
    }

    public Annotation getDsOdn() {
        return dsOdn;
    }

    public BigDecimal getDv200() {
        return dv200;
    }

    public Annotation getEffectorType() {
        return effectorType;
    }

    public Annotation getEmbeddingMedium() {
        return embeddingMedium;
    }

    @Override
    public String getEntityInfo() {
        if (entityInfo == null) {
            StringBuilder summary = new StringBuilder(super.getEntityInfo());
            if (getSampleType().getName().equals(SampleTypeEnum.QUALITY_CONTROL.getLabel()) && !getParents().isEmpty()) {
                summary.append("\n").append(Messages.get("parents")).append(": ").append(getParents().size());
                summary.append("\n").append("---");
                for (Sample parent : getParents()) {
                    summary.append(parent.getEntityInfo());
                    summary.append("\n").append("---");
                }
            }
            entityInfo = summary.toString();
        }
        return entityInfo;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getReplaces() != null) {
            addEntityInfoItem(summary, "replaces", getReplaces().getDisplayName());
        }
        if (!getReplacements().isEmpty()) {
            addEntityInfoItem(summary, "replacements", CollectionHelper.printIds(getReplacements()));
        }
        if (getUserDecision() != null) {
            addEntityInfoItem(summary, "userDecision", getUserDecision());
        }
        if (!getParents().isEmpty()) {
            if (getParents().size() > 10) {
                addEntityInfoItem(summary, "parents", getParents().size());
            } else {
                addEntityInfoItem(summary, "parents", CollectionHelper.printIds(getParents()));
            }
        }
        if (!getChildren().isEmpty()) {
            if (getChildren().size() > 10) {
                addEntityInfoItem(summary, "children", getChildren().size());
            } else {
                addEntityInfoItem(summary, "children", CollectionHelper.printIds(getChildren()));
            }
        }
        addEntityInfoItem(summary, "type", getType());

        addEntityInfoItem(summary, "multiplexIdParentLibrary", getMultiplexIdParentLibrary());
        addEntityInfoItem(summary, "multiplexId2ParentLibrary", getMultiplexId2ParentLibrary());

        for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
            try {
                Field field = Sample.class.getDeclaredField(sampleAttributeEnum.getName());
                Object value = field.get(this);
                String ret = null;
                if (value != null) {
                    if (sampleAttributeEnum.isSelectionType() && !sampleAttributeEnum.isEnumType()) {
                        if (sampleAttributeEnum.isAnnotationType()) {
                            // The sampleAttributeEnum is an annotation.
                            if (sampleAttributeEnum.getMultiValued()) {
                                Set<Annotation> annotations = (Set<Annotation>) value;
                                if (!annotations.isEmpty()) {
                                    ret = CollectionHelper.print(annotations);
                                }
                            } else {
                                ret = ((Annotation) value).getName();
                            }
                        } else {
                            // The sampleAttributeEnum is one of the following classes (all having the name attribute): SamplePreparationProtocol, MultiplexKit, Instrument
                            ret = ((AbstractNamedBaseEntity) value).getName();
                        }
                    } else if (sampleAttributeEnum.isStringType()) {
                        if (StringHelper.isNotEmpty((String) value)) {
                            if (sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEX_ID)) {
                                ret = getMultiplexIdNameWithSequence();
                            } else if (sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEX_ID_2)) {
                                ret = getMultiplexId2NameWithSequence();
                            } else {
                                ret = (String) value;
                            }
                        }
                    } else {
                        ret = value.toString();
                    }
                }
                if (ret != null) {
                    addEntityInfoItem(summary, StringHelper.firstLower(sampleAttributeEnum.getName()), ret);
                }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ignored) {
            }
        }

        addEntityInfoItems(summary, getCustomAttributes());

        if (!getQualityControlValues().isEmpty()) {
            summary.append("\n").append("---").append(" QC Values ").append("---");
            for (CustomAttribute customAttribute : getQualityControlValues()) {
                summary.append("\n").append(customAttribute.getName()).append(" = ").append(customAttribute.getValue());
            }
            summary.append("\n").append("---");
        }
        return summary.toString();
    }

    public List<Annotation> getEnzymes() {
        return CollectionHelper.asList(enzymes);
    }

    public Set<Annotation> getEnzymesAsSet() {
        return enzymes;
    }

    public Annotation getExpressionSystem() {
        return expressionSystem;
    }

    public Annotation getExtractionProtocol() {
        return extractionProtocol;
    }

    public String getExtractionProtocolString() {
        return extractionProtocolString;
    }

    public Set<Sample> getFamily() {
        if (family == null) {
            family = computeFamily();
        }
        return family;
    }

    public List<Sample> getFamilyAsList() {
        return CollectionHelper.asList(getFamily());
    }

    public Long getFamilyId() {
        return familyId;
    }

    public String getFastqScreen() {
        return fastqScreen;
    }

    public Annotation getFixation() {
        return fixation;
    }

    public Boolean getFraction() {
        return fraction;
    }

    public String getGeneticModification() {
        return geneticModification;
    }

    public String getGenomicCoordinates() {
        return genomicCoordinates;
    }

    public String getGenotype() {
        return genotype;
    }

    public Annotation getGroupingVar() {
        return groupingVar;
    }

    public String getGrowthConditions() {
        return growthConditions;
    }

    public String getGuideName() {
        return guideName;
    }

    public String getGuideSequence() {
        return guideSequence;
    }

    public String getHybridizationProtocol() {
        return hybridizationProtocol;
    }

    public String getImmunoPrecipitationTarget() {
        return immunoPrecipitationTarget;
    }

    public Set<ImportResource> getImportResources() {
        return importResources;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
        fields.add(IndexMapContentEnum.PARENTID.getField());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.TYPE.getField());
        fields.add(IndexMapContentEnum.PROJECTID.getField());
        fields.add(IndexMapContentEnum.ORDERID.getField());
        fields.add(IndexMapContentEnum.PARENTS.getField());
        fields.add(IndexMapContentEnum.CHILDREN.getField());
        for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
            fields.add(sampleAttributeEnum.getName());
        }
        for (CustomAttribute customAttribute : getCustomAttributes()) {
            fields.add(customAttribute.getName());
        }
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();
        content.add(IndexMapContentEnum.TYPE, getType());
        for (Sample parentSample : getParents()) {
            content.add(IndexMapContentEnum.PARENTID, parentSample.getId());
        }
        content.add(IndexMapContentEnum.PARENTS, getParents().size());
        content.add(IndexMapContentEnum.CHILDREN, getChildren().size());
        for (SampleAttributeEnum sampleAttributeEnum : getSampleType().getSampleAttributes()) {
            try {
                Object value = PropertyUtils.getProperty(this, sampleAttributeEnum.getName());
                Object ret = null;
                if (value != null) {
                    if (sampleAttributeEnum.isAnnotationType()) {
                        if (sampleAttributeEnum.getMultiValued()) {
                            Collection<Annotation> annotations = (Collection<Annotation>) value;
                            if (!annotations.isEmpty()) {
                                ret = CollectionHelper.print(annotations);
                            }
                        } else {
                            ret = ((Annotation) value).getName();
                        }
                    } else if (sampleAttributeEnum.isStringType()) {
                        if (StringHelper.isNotEmpty((String) value)) {
                            ret = value;
                        }
                    } else if (sampleAttributeEnum.isLocalDateType() || sampleAttributeEnum.isLocalDateTimeType()) {
                        ret = value;
                    } else {
                        ret = value.toString();
                    }
                }
                if (ret != null) {
                    content.add(sampleAttributeEnum, ret);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        for (CustomAttribute customAttribute : getCustomAttributes()) {
            content.put(customAttribute.getName(), customAttribute.getValue());
        }

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.SAMPLE;
    }

    public String getIndividualId() {
        return individualId;
    }

    public Set<Sample> getInitialParentSamplesOfUserMultiplex() {
        return initialParentSamplesOfUserMultiplex;
    }

    public Annotation getInitialTimePoint() {
        return initialTimePoint;
    }

    public Sample getInputQcSample() {
        return inputQcSample;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public String getInstrumentMethod() {
        return instrumentMethod;
    }

    public BigDecimal getIntegrityNumber() {
        return integrityNumber;
    }

    public List<Annotation> getInternalStandards() {
        return CollectionHelper.asList(internalStandards);
    }

    public Set<Annotation> getInternalStandardsAsSet() {
        return internalStandards;
    }

    public String getIrts() {
        return irts;
    }

    public BigDecimal getLabelAmount() {
        return labelAmount;
    }

    public Sample getLabeledParentForSampleOnRunType() {
        return getLabeledParentForSampleOnRunType(false);
    }

    public Sample getLabeledParentForSampleOnRunType(boolean includingUserSampleInMultiplexType) {
        if (isOnRunType() && !getParents().isEmpty() && getParents().size() < 3) {
            for (Sample parent : getParents()) {
                if (parent.isLabeledType() || includingUserSampleInMultiplexType && parent.isUserSampleInMultiplexType()) {
                    return parent;
                }
            }
        }
        return null;
    }

    public Annotation getLabelingMethod() {
        return labelingMethod;
    }

    public String getLabelingProtocol() {
        return labelingProtocol;
    }

    public String getLanePositionsForRunAsText(Run run) {
        return CollectionHelper.print(getRunUnitLanesForRunOrdered(run), "getPosition");
    }

    public String getLibraryProtocol() {
        return libraryProtocol;
    }

    public String getLibrarySelection() {
        return librarySelection;
    }

    public String getLibraryStrategy() {
        return libraryStrategy;
    }

    public String getLotInformation() {
        return lotInformation;
    }

    public String getLysisBuffer() {
        return lysisBuffer;
    }

    public Annotation getMatrix() {
        return matrix;
    }

    public String getMedia() {
        return media;
    }

    public BigDecimal getMolarity() {
        return molarity;
    }

    public BigDecimal getMolarityFmol() {
        return molarityFmol;
    }

    public Sample getMolaritySample() {
        return molaritySample;
    }

    public BigDecimal getMolarityTarget() {
        return molarityTarget;
    }

    public BigDecimal getMolecularWeight() {
        return molecularWeight;
    }

    public String getMultiplexId() {
        return multiplexId;
    }

    public String getMultiplexId1Old() {
        return multiplexId1Old;
    }

    public String getMultiplexId2() {
        return multiplexId2;
    }

    public String getMultiplexId2Dmx() {
        return multiplexId2Dmx;
    }

    /**
     * Get the name of the multiplexId2 (if any) along with its sequence.
     * Example:
     * - 'Nugen_UDI_BC096_i5 (GTACGATC)' if multiplexId2 is GTACGATC and multiplexKit2 is Nugen UDI 96
     * - 'i7_2_H06 (TGTAAGGTGG)' if multiplexId2 is TGTAAGGTGG and multiplexKit is Illumina IDT 384 10nt V1b.
     */
    public String getMultiplexId2NameWithSequence() {
        if (multiplexId2NameWithSequence == null) {
            if (getMultiplexKit2() != null && getMultiplexId2() != null) {
                final List<MultiplexId> multiplexIds = CDI.current().select(MultiplexIdService.class).get().getMultiplexIdsByMultiplexKitIdAndSequence(getMultiplexKit2().getId(), getMultiplexId2());
                if (multiplexIds.size() == 1) {
                    // E.g., 'Nugen_UDI_BC096_i5' for sequence 'GTACGATC'
                    setMultiplexId2NameWithSequenceFromMultiplexId(multiplexIds.get(0));
                } else if (multiplexIds.size() == 2) {
                    // E.g., 'i5_1_A07' and 'i7_2_H06' for sequence 'TGTAAGGTGG'
                    if (multiplexIds.get(0).isMultiplexId2AssignableOnly()) {
                        setMultiplexId2NameWithSequenceFromMultiplexId(multiplexIds.get(0));
                    } else if (multiplexIds.get(1).isMultiplexId2AssignableOnly()) {
                        setMultiplexId2NameWithSequenceFromMultiplexId(multiplexIds.get(1));
                    }
                } else if (multiplexIds.size() > 2) {
                    final Set<String> multiplexIdNames = new HashSet<>();
                    for (MultiplexId aMultiplexId : multiplexIds) {
                        if (aMultiplexId.isMultiplexId2AssignableOnly()) {
                            multiplexIdNames.add(aMultiplexId.getName());
                            if (multiplexIdNames.size() > 1) {
                                return multiplexId2NameWithSequence;
                            }
                        } else {
                            return multiplexId2NameWithSequence;
                        }
                    }
                    setMultiplexId2NameWithSequenceFromMultiplexId(multiplexIds.get(0));
                }
            } else {
                multiplexId2NameWithSequence = getMultiplexId2();
            }
        }
        return multiplexId2NameWithSequence;
    }

    /**
     * Get the name of the multiplexId2 (if any).
     * Example:
     * - 'Nugen_UDI_BC096_i5' if multiplexId2 is GTACGATC and multiplexKit2 is Nugen UDI 96.
     * - 'i5_1_A07' if multiplexId2 is TGTAAGGTGG and multiplexKit is Illumina IDT 384 10nt V1b.
     */
    public String getMultiplexId2NameWithoutSequence() {
        if (multiplexId2NameWithoutSequence == null && getMultiplexKit2() != null && getMultiplexId2() != null) {
            final List<MultiplexId> multiplexIds = CDI.current().select(MultiplexIdService.class).get().getMultiplexIdsByMultiplexKitIdAndSequence(getMultiplexKit2().getId(), getMultiplexId2());
            if (multiplexIds.size() == 1) {
                // E.g., 'Nugen_UDI_BC096_i5' for sequence 'GTACGATC'
                setMultiplexId2NameWithoutSequenceFromMultiplexId(multiplexIds.get(0));
            } else if (multiplexIds.size() == 2) {
                // E.g., 'i5_1_A07' and 'i7_2_H06' for sequence 'TGTAAGGTGG'
                if (multiplexIds.get(0).isMultiplexId2AssignableOnly()) {
                    setMultiplexId2NameWithoutSequenceFromMultiplexId(multiplexIds.get(0));
                } else if (multiplexIds.get(1).isMultiplexId2AssignableOnly()) {
                    setMultiplexId2NameWithoutSequenceFromMultiplexId(multiplexIds.get(1));
                }
            } else if (multiplexIds.size() > 2) {
                final Set<String> multiplexIdNames = new HashSet<>();
                for (MultiplexId aMultiplexId : multiplexIds) {
                    if (aMultiplexId.isMultiplexId2AssignableOnly()) {
                        multiplexIdNames.add(aMultiplexId.getName());
                        if (multiplexIdNames.size() > 1) {
                            return multiplexId2NameWithoutSequence;
                        }
                    } else {
                        return multiplexId2NameWithoutSequence;
                    }
                }
                setMultiplexId2NameWithoutSequenceFromMultiplexId(multiplexIds.get(0));
            }
        }
        return multiplexId2NameWithoutSequence;
    }

    public String getMultiplexId2Old() {
        return multiplexId2Old;
    }

    public String getMultiplexId2ParentLibrary() {
        Sample LabeledParentForSampleOnRunType = getLabeledParentForSampleOnRunType(true);
        return LabeledParentForSampleOnRunType != null ? LabeledParentForSampleOnRunType.getMultiplexId2() : null;
    }

    public String getMultiplexIdAndMultiplexId2Concatenation(String delimiter) {
        String aDelimiter = Constants.EMPTY_STRING;
        if (StringHelper.isNotEmpty(delimiter)) {
            aDelimiter = delimiter;
        }
        return (StringHelper.isNotEmpty(getMultiplexId()) ? getMultiplexId() : Constants.EMPTY_STRING) + aDelimiter + (StringHelper
            .isNotEmpty(getMultiplexId2()) ? getMultiplexId2() : Constants.EMPTY_STRING);
    }

    public Sample getMultiplexIdConflictMultiplexedSample() {
        return multiplexIdConflictMultiplexedSample;
    }

    public String getMultiplexIdDmx() {
        return multiplexIdDmx;
    }

    /**
     * Get the name of the multiplexId (if any) along with its sequence.
     * Example:
     * - 'Nugen_UDI_BC096_i5 (GTACGATC)' if multiplexId is GTACGATC and multiplexKit is Nugen UDI 96.
     * - 'i5_1_A07 (TGTAAGGTGG)' if multiplexId is TGTAAGGTGG and multiplexKit is Illumina IDT 384 10nt V1b.
     */
    public String getMultiplexIdNameWithSequence() {
        if (multiplexIdNameWithSequence == null) {
            if (getMultiplexKit() != null && getMultiplexId() != null) {
                final List<MultiplexId> multiplexIds = CDI.current().select(MultiplexIdService.class).get().getMultiplexIdsByMultiplexKitIdAndSequence(getMultiplexKit().getId(), getMultiplexId());
                if (multiplexIds.size() == 1) {
                    // E.g., 'Nugen_UDI_BC096_i5' for sequence 'GTACGATC'
                    setMultiplexIdNameWithSequenceFromMultiplexId(multiplexIds.get(0));
                } else if (multiplexIds.size() == 2) {
                    // E.g., 'i5_1_A07' and 'i7_2_H06' for sequence 'TGTAAGGTGG'
                    if (multiplexIds.get(0).isMultiplexIdAssignableOnly()) {
                        setMultiplexIdNameWithSequenceFromMultiplexId(multiplexIds.get(0));
                    } else if (multiplexIds.get(1).isMultiplexIdAssignableOnly()) {
                        setMultiplexIdNameWithSequenceFromMultiplexId(multiplexIds.get(1));
                    }
                } else if (multiplexIds.size() > 2) {
                    final Set<String> multiplexIdNames = new HashSet<>();
                    for (MultiplexId aMultiplexId : multiplexIds) {
                        if (aMultiplexId.isMultiplexIdAssignableOnly()) {
                            multiplexIdNames.add(aMultiplexId.getName());
                            if (multiplexIdNames.size() > 1) {
                                return multiplexIdNameWithSequence;
                            }
                        } else {
                            return multiplexIdNameWithSequence;
                        }
                    }
                    setMultiplexIdNameWithSequenceFromMultiplexId(multiplexIds.get(0));
                }
            } else {
                multiplexIdNameWithSequence = getMultiplexId();
            }
        }
        return multiplexIdNameWithSequence;
    }

    /**
     * Get the name of the multiplexId (if any).
     * Example:
     * - 'Nugen_UDI_BC096_i5' if multiplexId is GTACGATC and multiplexKit is Nugen UDI 96.
     * - 'i5_1_A07' if multiplexId is TGTAAGGTGG and multiplexKit is Illumina IDT 384 10nt V1b.
     */
    public String getMultiplexIdNameWithoutSequence() {
        if (multiplexIdNameWithoutSequence == null && getMultiplexKit() != null && getMultiplexId() != null) {
            final List<MultiplexId> multiplexIds = CDI.current().select(MultiplexIdService.class).get().getMultiplexIdsByMultiplexKitIdAndSequence(getMultiplexKit().getId(), getMultiplexId());
            if (multiplexIds.size() == 1) {
                // E.g., 'Nugen_UDI_BC096_i5' for sequence 'GTACGATC'
                setMultiplexIdNameWithoutSequenceFromMultiplexId(multiplexIds.get(0));
            } else if (multiplexIds.size() == 2) {
                // E.g., 'i5_1_A07' and 'i7_2_H06' for sequence 'TGTAAGGTGG'
                if (multiplexIds.get(0).isMultiplexIdAssignableOnly()) {
                    setMultiplexIdNameWithoutSequenceFromMultiplexId(multiplexIds.get(0));
                } else if (multiplexIds.get(1).isMultiplexIdAssignableOnly()) {
                    setMultiplexIdNameWithoutSequenceFromMultiplexId(multiplexIds.get(1));
                }
            } else if (multiplexIds.size() > 2) {
                final Set<String> multiplexIdNames = new HashSet<>();
                for (MultiplexId aMultiplexId : multiplexIds) {
                    if (aMultiplexId.isMultiplexIdAssignableOnly()) {
                        multiplexIdNames.add(aMultiplexId.getName());
                        if (multiplexIdNames.size() > 1) {
                            return multiplexIdNameWithoutSequence;
                        }
                    } else {
                        return multiplexIdNameWithoutSequence;
                    }
                }
                setMultiplexIdNameWithoutSequenceFromMultiplexId(multiplexIds.get(0));
            }
        }
        return multiplexIdNameWithoutSequence;
    }

    public String getMultiplexIdParentLibrary() {
        Sample LabeledParentForSampleOnRunType = getLabeledParentForSampleOnRunType(true);
        return LabeledParentForSampleOnRunType != null ? LabeledParentForSampleOnRunType.getMultiplexId() : null;
    }

    public MultiplexKit getMultiplexKit() {
        return multiplexKit;
    }

    public MultiplexKit getMultiplexKit2() {
        return multiplexKit2;
    }

    public SampleTypeEnum getMultiplexParentSampleTypeEnum() {
        return SampleTypeEnum.getMultiplexParentSampleTypeEnumByLabel(getType());
    }

    public Boolean getMultiplexed() {
        return multiplexed;
    }

    public Boolean getMultiplexedByUser() {
        return multiplexedByUser;
    }

    public Sample getMultiplexedParentForSampleOnRunType() {
        if (isOnRunType() && !getParents().isEmpty() && getParents().size() < 3) {
            for (Sample parent : getParents()) {
                if (parent.isMultiplexedType()) {
                    return parent;
                }
            }
        }
        return null;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public int getNumberOfAssociatedResources() {
        return getResources().isEmpty() ? 0 : getResources().size();
    }

    public Integer getNumberOfCellsLoaded() {
        return numberOfCellsLoaded;
    }

    public Integer getNumberOfCycles() {
        return numberOfCycles;
    }

    public Long getOldExtractId() {
        return oldExtractId;
    }

    public String getOldNamePrefix() {
        return oldNamePrefix;
    }

    public List<SampleAttributeEnum> getOldSampleAttributeEnums() {
        return oldSampleAttributeEnums;
    }

    public Long getOldSampleId() {
        return oldSampleId;
    }

    public String getOldUserSampleInMultiplexName() {
        return oldUserSampleInMultiplexName;
    }

    public String getOldUserSampleName() {
        return oldUserSampleName;
    }

    public Annotation getOnSlideModification() {
        return onSlideModification;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public List<Order> getOrders() {
        if (orders == null) {
            orders = CDI.current().select(OrderItemService.class).get().getOrdersBySampleId(getId());
        }
        return orders;
    }

    public String getOrganism() {
        return organism;
    }

    public Annotation getOrganismPart() {
        return organismPart;
    }

    public List<Sample> getParentSamplesOfUserMultiplex() {
        return parentSamplesOfUserMultiplex;
    }

    public Set<Sample> getParents() {
        return parents;
    }

    public String getParentsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Sample parent : getParents()) {
            if (parent != null) {
                stringBuilder.append(parent.getId()).append(" - ").append(parent.getName()).append("   ");
            }
        }
        return stringBuilder.toString();
    }

    public List<Sample> getParentsExcludingUserSampleInMultiplexType() {
        List<Sample> parentsExcludingUserSampleInMultiplexType = CollectionHelper.asList(getParents());
        parentsExcludingUserSampleInMultiplexType.removeIf(Sample::isUserSampleInMultiplexType);
        return parentsExcludingUserSampleInMultiplexType;
    }

    public List<Sample> getParentsFiltered(String filterString) {
        List<Sample> parentsFiltered = new ArrayList<>();
        if (StringHelper.isEmpty(filterString)) {
            parentsFiltered.addAll(getParents());
        } else {
            for (Sample parent : getParents()) {
                if (parent.getName().contains(filterString) || parent.getIdString().contains(filterString)) {
                    parentsFiltered.add(parent);
                }
            }
        }
        return parentsFiltered;
    }

    public List<Sample> getParentsOrderedByName() {
        List<Sample> parentsOrderedByName = CollectionHelper.asList(parents);
        parentsOrderedByName.sort(Comparator.comparing(Sample::getName));
        return parentsOrderedByName;
    }

    public List<Sample> getParentsOrderedByTubeIdOrName() {
        for (Sample parentSample : parents) {
            if (parentSample.getTubeIdPadded() == null) {
                return getParentsOrderedByName();
            }
        }
        List<Sample> parentsOrderedByTubeId = CollectionHelper.asList(parents);
        parentsOrderedByTubeId.sort(Comparator.comparing(Sample::getTubeIdPadded));
        return parentsOrderedByTubeId;
    }

    public String getPlateTileInfo() {
        return getTubeId() != null ? getTubeId() : Messages.get("notSet");
    }

    public String getPlateTileInfoAdditional(SampleAttributeEnum sampleAttributeEnum) {
        StringBuilder info = new StringBuilder();
        if (sampleAttributeEnum != null) {
            try {
                Object value = PropertyUtils.getProperty(this, sampleAttributeEnum.getName());
                String attributeValue = null;
                if (value != null) {
                    if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                        attributeValue = ((Annotation) value).getName();
                    } else if (sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType()) {
                        if (SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum)) {
                            attributeValue = ((SamplePreparationProtocol) value).getName();
                        } else if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum) || SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                            attributeValue = ((MultiplexKit) value).getName();
                        } else if (SampleAttributeEnum.INSTRUMENT.equals(sampleAttributeEnum)) {
                            attributeValue = ((Instrument) value).getName();
                        }
                    } else if (sampleAttributeEnum.isEnumType()) {
                        if (SampleFormEnum.class.equals(sampleAttributeEnum.getClazz())) {
                            attributeValue = ((SampleFormEnum) value).getLabel();
                        } else if (SampleQCTypeEnum.class.equals(sampleAttributeEnum.getClazz())) {
                            attributeValue = ((SampleQCTypeEnum) value).getLabel();
                        } else if (SampleStatusEnum.class.equals(sampleAttributeEnum.getClazz())) {
                            attributeValue = ((SampleStatusEnum) value).getLabel();
                        }
                    } else {
                        if (sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEX_ID)) {
                            attributeValue = getMultiplexIdNameWithoutSequence();
                        } else if (sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEX_ID_2)) {
                            attributeValue = getMultiplexId2NameWithoutSequence();
                        } else {
                            attributeValue = value + (StringHelper.isNotEmpty(sampleAttributeEnum.getUnit()) ? " " + sampleAttributeEnum.getUnit() : Constants.EMPTY_STRING);
                        }
                    }
                    info.append(attributeValue);
                } else {
                    info.append(Messages.get("notSet"));
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        }
        return info.toString();
    }

    public String getPlateTileInfoExtended(SampleAttributeEnum sampleAttributeEnum) {
        StringBuilder info = new StringBuilder(getPlateTileInfo());
        if (sampleAttributeEnum != null) {
            info.append("\n");
            info.append(getPlateTileInfoAdditional(sampleAttributeEnum));
        }
        return info.toString();
    }

    public List<Plate> getPlates() {
        if (plates == null) {
            plates = CDI.current().select(PlateService.class).get().getPlatesBySampleId(getId());
        }
        return plates;
    }

    public Long getPlatesCount() {
        if (platesCount == null) {
            platesCount = CDI.current().select(PlateService.class).get().getPlatesBySampleIdCount(getId());
        }
        return platesCount;
    }

    public String getPreTreatment() {
        return preTreatment;
    }

    public BigDecimal getProteinAmount() {
        return proteinAmount;
    }

    public BigDecimal getPurityA260230() {
        return purityA260230;
    }

    public BigDecimal getPurityA260280() {
        return purityA260280;
    }

    public Set<Sample> getQcChildrenWithNonEmptyMolarConcentrationInRange() {
        if (qcChildrenWithNonEmptyConcentration == null) {
            qcChildrenWithNonEmptyConcentration = new HashSet<>();
            qcChildrenWithNonEmptyConcentration.addAll(getChildren());
            qcChildrenWithNonEmptyConcentration.removeIf(sample -> !sample.getType()
                .equals(SampleTypeEnum.QUALITY_CONTROL.getLabel()) || SampleQCTypeEnum.QPCR.equals(sample.getQualityControlType()) || SampleQCTypeEnum.SEQUENCING_CONTROL
                .equals(sample.getQualityControlType()) || SampleQCTypeEnum.SINGLE_CELL_VISUAL_ASSESSMENT_AND_COUNTING.equals(sample.getQualityControlType()) || sample
                .getConcentrationMolarInRange() == null);
        }
        return qcChildrenWithNonEmptyConcentration;
    }

    public Boolean getQcPassed() {
        return qcPassed;
    }

    public Set<Plate> getQcPlates() {
        if (qcPlates == null) {
            qcPlates = new HashSet<>();
            for (SamplePlatePosition samplePlatePosition : getSamplePlatePositions()) {
                if (samplePlatePosition.getPlate().getPlateType().isQualityControlPlateType()) {
                    qcPlates.add(samplePlatePosition.getPlate());
                }
            }
        }
        return qcPlates;
    }

    public Set<Sample> getQcSiblingsWithNonEmptyConcentration() {
        if (qcSiblingsWithNonEmptyConcentration == null) {
            qcSiblingsWithNonEmptyConcentration = new HashSet<>();
            if (getParents().size() == 1) {
                qcSiblingsWithNonEmptyConcentration.addAll(getParents().iterator().next().getChildren());
                qcSiblingsWithNonEmptyConcentration.removeIf(sample -> !sample.getType()
                    .equals(SampleTypeEnum.QUALITY_CONTROL.getLabel()) || SampleQCTypeEnum.QPCR.equals(sample.getQualityControlType()) || SampleQCTypeEnum.SEQUENCING_CONTROL
                    .equals(sample.getQualityControlType()) || SampleQCTypeEnum.SINGLE_CELL_VISUAL_ASSESSMENT_AND_COUNTING.equals(sample.getQualityControlType()) || sample
                    .getConcentration() == null);
            }
        }
        return qcSiblingsWithNonEmptyConcentration;
    }

    public BigDecimal getQpcr() {
        return qpcr;
    }

    public SampleQCTypeEnum getQualityControlType() {
        return qualityControlType;
    }

    /**
     * Get qualityControlValues. Note: At the moment only the concentration is shown. This should later be adapted depending on which values are relevant for which quality control types.
     */
    public List<CustomAttribute> getQualityControlValues() {
        List<CustomAttribute> qualityControlValues = new ArrayList<>();
        int i = 1;
        for (Sample child : getChildren()) {
            if (SampleTypeEnum.QUALITY_CONTROL.getLabel().equals(child.getType()) && child.getConcentration() != null) {
                qualityControlValues.add(new CustomAttribute(child, new CustomAttributeColumn("Concentration " + child.getId(), i++), SampleAttributeEnum.CONCENTRATION.getLabel(), child
                    .getConcentration() + " (" + SampleAttributeEnum.CONCENTRATION.getUnit() + "; QC SampleId " + child.getId() + ")", Constants.STRING, false));
            }
        }
        return qualityControlValues;
    }

    public String getQualityControlValuesAsText() {
        return convertToText(getQualityControlValues());
    }

    public BigDecimal getQubit() {
        return qubit;
    }

    public Boolean getReMultiplexed() {
        return reMultiplexed;
    }

    public BigDecimal getReadCount() {
        return readCount;
    }

    public BigDecimal getReadCountTotal() {
        return readCountTotal;
    }

    public XMLRequestParameterReadSample getReadRequestParameter() {
        return readRequestParameter;
    }

    public String getRemoveHint() {
        return getDescendants() != null ? Messages.get("removeSampleHint").replace("{0}", String.valueOf(getDescendants().size())) : null;
    }

    public Set<Sample> getReplacements() {
        return replacements;
    }

    public Sample getReplaces() {
        return replaces;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public List<Resource> getResourcesTransitive() {
        Set<Resource> childResources = new HashSet<>();
        for (Sample sample : getChildren()) {
            childResources.addAll(sample.getResources());
        }
        return new ArrayList<>(childResources);
    }

    public Set<Sample> getRoots() {
        return getRoots(true);
    }

    public Set<Sample> getRoots(boolean all) {
        if (roots == null) {
            roots = computeRoots(all);
        }
        return roots;
    }

    public String getRowStyleClass() {
        if (getQcPassed() != null && (getType().equals(SampleTypeEnum.QUALITY_CONTROL.getLabel()) || SampleTypeEnum.isLabeled(getType()))) {
            return getQcPassed() ? Constants.BACKGROUND_COLOR_GREEN : Constants.BACKGROUND_COLOR_RED;
        }
        return getUserDecision() == null || getUserDecision().equals(SampleUserDecisionEnum.PROCEED) ? Constants.BACKGROUND_COLOR_YELLOW : Constants.BACKGROUND_COLOR_RED;
    }

    public Set<RunSample> getRunSamples() {
        return runSamples;
    }

    public RunUnitLane getRunUnitLane() {
        return runUnitLane;
    }

    public Set<RunUnitLane> getRunUnitLanes() {
        return runUnitLanes;
    }

    public Set<RunUnitLane> getRunUnitLanesForRun(Run run) {
        Set<RunUnitLane> aRunUnitLanes = new HashSet<>();
        if (run.getRunUnit() != null && run.getRunUnit().getRunUnitLanes() != null) {
            for (RunUnitLane aRunUnitLane : run.getRunUnit().getRunUnitLanes()) {
                if (aRunUnitLane.getSamples().contains(this)) {
                    aRunUnitLanes.add(aRunUnitLane);
                }
            }
        }
        return aRunUnitLanes;
    }

    public List<RunUnitLane> getRunUnitLanesForRunOrdered(Run run) {
        Set<RunUnitLane> aRunUnitLanes = new HashSet<>();
        if (run.getRunUnit() != null && run.getRunUnit().getRunUnitLanes() != null) {
            for (RunUnitLane aRunUnitLane : run.getRunUnit().getRunUnitLanes()) {
                if (aRunUnitLane.getSamples().contains(this)) {
                    aRunUnitLanes.add(aRunUnitLane);
                }
            }
        }
        List<RunUnitLane> orderedRunUnitLanes = new ArrayList<>(aRunUnitLanes);
        orderedRunUnitLanes.sort(Comparator.comparing(RunUnitLane::getPosition));
        return orderedRunUnitLanes;
    }

    public List<Run> getRuns() {
        if (runs == null) {
            runs = CDI.current().select(RunService.class).get().getRunsBySampleId(getId());
        }
        return runs;
    }

    public Long getRunsCount() {
        if (runsCount == null) {
            runsCount = CDI.current().select(RunService.class).get().getRunsBySampleIdCount(getId());
        }
        return runsCount;
    }

    public Map<String, String> getSampleAttributeEnumNameValueMap() {
        return sampleAttributeEnumNameValueMap;
    }

    public String getSampleCalculationInfoForFinalQc() {
        return "(Conc.): " + getConcentrationMolarInRange() + (!getQcPlates().isEmpty() ? " / (P): " + CollectionHelper.printNames(getQcPlates()) : Constants.EMPTY_STRING);
    }

    public String getSampleCalculationInfoForInitialQc() {
        return "(Conc.): " + getConcentration() + (!getQcPlates().isEmpty() ? " / (P): " + CollectionHelper.printNames(getQcPlates()) : Constants.EMPTY_STRING);
    }

    public SampleFormEnum getSampleForm() {
        return sampleForm;
    }

    public Set<SamplePlatePosition> getSamplePlatePositions() {
        return samplePlatePositions;
    }

    public SamplePreparationProtocol getSamplePreparationProtocol() {
        return samplePreparationProtocol;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public SampleTypeEnum getSampleTypeEnumOnRun() {
        return SampleTypeEnum.getSampleTypeEnumOnRun(getType());
    }

    public LocalDateTime getSamplingDate() {
        return samplingDate;
    }

    public String getScanningProtocol() {
        return scanningProtocol;
    }

    public MultiplexId getSelectedMultiplexId() {
        return selectedMultiplexId;
    }

    public MultiplexId getSelectedMultiplexId2() {
        return selectedMultiplexId2;
    }

    public List<MultiplexId> getSelectionMultiplexId2s() {
        return selectionMultiplexId2s;
    }

    public List<MultiplexId> getSelectionMultiplexIds() {
        return selectionMultiplexIds;
    }

    public List<MultiplexId> getSelectionMultiplexIdsBySampleAttributeName(String sampleAttributeEnumName) {
        if (sampleAttributeEnumName != null) {
            if (sampleAttributeEnumName.equals(Constants.MULTIPLEX_ID)) {
                if (getSelectionMultiplexIds() == null && getMultiplexKit() != null) {
                    setSelectionMultiplexIds(CDI.current().select(MultiplexIdService.class).get().getMultiplexIdsByMultiplexKitId(getMultiplexKit().getId()));
                }
                return getSelectionMultiplexIds();
            }
            if (sampleAttributeEnumName.equals(Constants.MULTIPLEX_ID_2)) {
                if (getSelectionMultiplexId2s() == null && getMultiplexKit2() != null) {
                    setSelectionMultiplexId2s(CDI.current().select(MultiplexIdService.class).get().getMultiplexIdsByMultiplexKitId(getMultiplexKit2().getId()));
                }
                return getSelectionMultiplexId2s();
            }
        }
        return new ArrayList<>();
    }

    public List<Annotation> getSeparationTechniques() {
        return CollectionHelper.asList(separationTechniques);
    }

    public Set<Annotation> getSeparationTechniquesAsSet() {
        return separationTechniques;
    }

    public Annotation getSequencingMethod() {
        return sequencingMethod;
    }

    public Annotation getSequencingMode() {
        return sequencingMode;
    }

    public String getSequencingPlatform() {
        return sequencingPlatform;
    }

    public Annotation getSequencingPrimer() {
        return sequencingPrimer;
    }

    public Annotation getSex() {
        return sex;
    }

    public Sample getSingleParent() {
        return getParents() != null && getParents().size() == 1 ? getParents().stream().findFirst().orElse(null) : null;
    }

    public Long getSingleParentId() {
        return getSingleParent() != null ? getSingleParent().getId() : null;
    }

    public BigDecimal getSize() {
        return size;
    }

    public BigDecimal getSizeAverage() {
        return sizeAverage;
    }

    public BigDecimal getSizeGenomeEstimated() {
        return sizeGenomeEstimated;
    }

    public String getSizeRange() {
        return sizeRange;
    }

    public Annotation getSlideType() {
        return slideType;
    }

    public Annotation getSourceType() {
        return sourceType;
    }

    public String getSourceTypeOld() {
        String ret = Constants.EMPTY_STRING;
        if (getOrganismPart() != null) {
            ret += getOrganismPart().getName();
        }
        if (getCellType() != null) {
            if (!ret.isEmpty()) {
                ret += ", ";
            }
            ret += getCellType();
        }
        if (getCellLine() != null) {
            if (!ret.isEmpty()) {
                ret += ", ";
            }
            ret += getCellLine();
        }
        return ret;
    }

    public Annotation getSpecies() {
        return species;
    }

    public String getSpeciesString() {
        if (speciesString == null && getSpecies() != null) {
            speciesString = getSpecies().getName();
        }
        return speciesString;
    }

    public SampleStatusEnum getStatus() {
        return status;
    }

    public String getStrain() {
        return strain;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public Annotation getSurface() {
        return surface;
    }

    public String getTissue() {
        return tissue;
    }

    public Annotation getTreatment() {
        return treatment;
    }

    public BigDecimal getTs() {
        return ts;
    }

    public String getTubeId() {
        return tubeId;
    }

    public String getTubeIdOrId() {
        return tubeId != null ? tubeId : getIdString();
    }

    public String getTubeIdPadded() {
        return tubeIdPadded;
    }

    public String getType() {
        return type;
    }

    public SampleUserDecisionEnum getUserDecision() {
        return userDecision;
    }

    public String getUserSampleInMultiplexName() {
        return userSampleInMultiplexName;
    }

    public String getUserSampleName() {
        return userSampleName;
    }

    public Annotation getVector() {
        return vector;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getVolumeDilutionSample() {
        return volumeDilutionSample;
    }

    public BigDecimal getVolumeDilutionWater() {
        return volumeDilutionWater;
    }

    public BigDecimal getVolumeEluted() {
        return volumeEluted;
    }

    public BigDecimal getVolumeInput() {
        return volumeInput;
    }

    public BigDecimal getVolumeLysisBuffer() {
        return volumeLysisBuffer;
    }

    public BigDecimal getVolumeMeasured() {
        return volumeMeasured;
    }

    public BigDecimal getVolumeReaction() {
        return volumeReaction;
    }

    public BigDecimal getVolumeTarget() {
        return volumeTarget;
    }

    public BigDecimal getVolumeToAddEbt() {
        return volumeToAddEbt;
    }

    public BigDecimal getVolumeToAddSample() {
        return volumeToAddSample;
    }

    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public List<WorkflowStep> getWorkflowStepsOld() {
        List<WorkflowStep> aWorkflowSteps = new ArrayList<>();
        for (Workflow workflow : getWorkflowsOld()) {
            if (!workflow.getWorkflowSteps().isEmpty()) {
                aWorkflowSteps.addAll(workflow.getWorkflowSteps());
            }
        }
        return aWorkflowSteps;
    }

    public Set<Workflow> getWorkflows() {
        return workflows;
    }

    public Set<Workflow> getWorkflowsOld() {
        Set<Workflow> workflows = new HashSet<>();
        for (OrderItem orderItem : getOrderItems()) {
            workflows.addAll(orderItem.getWorkflows());
        }
        return workflows;
    }

    public List<Workunit> getWorkunits() {
        if (workunits == null) {
            workunits = CDI.current().select(WorkunitService.class).get().getWorkunitsBySampleId(getId());
        }
        return workunits;
    }

    public BigDecimal getYield() {
        return yield;
    }

    public boolean hasReplacements() {
        return !getReplacements().isEmpty();
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntities(getParents());
    }

    public void initMove() {
        setContainer(null);
    }

    public void initializeInitialParentSamplesOfUserMultiplex() {
        getInitialParentSamplesOfUserMultiplex().clear();
        if (getMultiplexedByUser() != null && getMultiplexedByUser()) {
            recomputeParentSamplesOfUserMultiplex();
            getInitialParentSamplesOfUserMultiplex().addAll(new HashSet<>(getParentSamplesOfUserMultiplex()));
            setInitialParentSamplesOfUserMultiplexInitialized(true);
        }
    }

    private void initializeNamePrefixAndUserSampleInMultiplexName() {
        if (StringHelper.isNotEmpty(getName())) {
            int indexOf = getName().indexOf(Constants.IN_MULTIPLEX_SAMPLE_NAME_SEPARATOR);
            if (indexOf > 0) {
                setNamePrefix(getName().substring(0, indexOf));
                if (indexOf + 1 < getName().length()) {
                    setUserSampleInMultiplexName(getName().substring(indexOf + 1));
                }
            }
        } else {
            setNamePrefix(Constants.EMPTY_STRING);
        }
    }

    public void inputQcSampleChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setInputQcSample((Sample) event.getNewValue());
            if (getInputQcSample().getConcentration() != null && getInputQcSample().getConcentration().compareTo(BigDecimal.ZERO) > 0) {
                setConcentrationInputQc(getInputQcSample().getConcentration());
                calculateVolumeDilutionSampleAndWater();
            }
        } else {
            setInputQcSample(null);
        }
        AJAX.update("@this", Constants.EDIT + ":concentrationInputQc");
    }

    public boolean isAssignSampleValuesChanged() {
        return assignSampleValuesChanged;
    }

    public boolean isAssignedToRuns() {
        for (Sample child : getChildren()) {
            if (child.isOnRunType()) {
                return true;
            }
        }
        return false;
    }

    public boolean isChildCreatable() {
        return isCreatable() && !isUserSampleInMultiplexType() && !isOnRunType() && isUpdatable();
    }

    @Override
    public boolean isCloneable() {
        return isCreatable() && !isMultiplexedType() && !isMultiplexType() && !isUserSampleInMultiplexType() && !isOnRunType() && (isUpdatable() || getCurrentUser().hasRoleImplicit(RoleEnum.CONTAINERMANAGER) && getCurrentUser().hasExtensibleContainerForSampleCreation());
    }

    public boolean isControlType() {
        return SampleTypeEnum.isControl(getType());
    }

    public boolean isCoupleable(Sample sample) {
        if (sample == null || !(StringHelper.isNotEmpty(getName()) && StringHelper.isNotEmpty(sample.getName()) && getName().equals(sample.getName()))) {
            // If the two samples do not have the same name, they are not coupleable.
            return false;
        }
        boolean equal = true, empty = true;
        try {
            Set<SampleAttributeEnum> sampleFormDependentAttributes = new HashSet<>(SampleAttributeEnum.getSampleFormDependentAttributes());
            Set<SampleAttributeEnum> qcTypeDependentAttributes = new HashSet<>(SampleAttributeEnum.getQCTypeDependentAttributes());
            /*
             * Two samples are coupleable if the tuples of their coupleable attributes are congruent. Exception: If the changed sample has the same name as the compared one and the changed sample has
             * null/empty values for all coupleable attributes.
             */
            for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.getAttributeEnums(true, type)) {
                if (!sampleAttributeEnum.getLabel()
                    .equals(SampleAttributeEnum.TUBE_ID.getLabel()) && (!isSampleFormDependentAttributeSkipForCoupling(sampleFormDependentAttributes, sampleAttributeEnum, getSampleForm(), sample
                    .getSampleForm()) || !isQcTypeDependentAttributeSkipForCoupling(qcTypeDependentAttributes, sampleAttributeEnum, getQualityControlType(), sample.getQualityControlType()))) {
                    Object current = PropertyUtils.getProperty(this, sampleAttributeEnum.getName());
                    Object changed = PropertyUtils.getProperty(sample, sampleAttributeEnum.getName());
                    boolean currentEmpty = current == null || sampleAttributeEnum.isStringType() && StringHelper.isEmpty((String) current) || sampleAttributeEnum
                        .getMultiValued() && ((Collection<?>) current).isEmpty();
                    boolean changedEmpty = changed == null || sampleAttributeEnum.isStringType() && StringHelper.isEmpty((String) changed) || sampleAttributeEnum
                        .getMultiValued() && ((Collection<?>) changed).isEmpty();
                    equal &= changedEmpty && currentEmpty || Objects.equals(current, changed);
                    empty &= changedEmpty;
                    if (!equal && !empty) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCreatable() {
        return getContainer() != null && getContainer().isSampleCreatable();
    }

    @Override
    public boolean isDeletable() {
        return isDeletableUponContainerDeletion(null);
    }

    public boolean isDeletableUponContainerDeletion(Container container) {
        if (getId() <= 0 || isUserSampleInMultiplexType() || !getResources().isEmpty() || !getImportResources().isEmpty() || !getCharges().isEmpty() || !getSamplePlatePositions().isEmpty() || !getRunUnitLanes().isEmpty()
            || !getWorkflows().isEmpty() || !getWorkflowSteps().isEmpty() || !getReplacements().isEmpty()
            || (container != null && !container.isContainerProject() ? isLinkedToAnotherOrder((Order) container) : !getOrderItems().isEmpty())) {
            return false;
        }
        for (Sample descendant : getChildren()) {
            if (!descendant.isDeletableUponContainerDeletion(container)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDeletableWS() {
        return !(getMultiplexed() != null && getMultiplexed()) && isDeletable();
    }

    private boolean isEligibleForSamplePreparationProtocolPreset() {
        /*
         * Conditions:
         * - The container of the sample needs to be an order (reused samples from an existing project, i.e., samples on the project level reused in an order, therefore do not qualify)
         * - The container of the sample must have the "Library Protocol" set
         * - The sample has to be of type 'Library - Illumina', 'Library - Nanopore', or 'Library - PacBio'
         * - The sample has to have a parent of type 'Biological Samples - Sequencing' or 'Biological Sample - Single Cell Sequencing'
         * - The sample has exactly one parent (else there would be an ambiguity which parent, i.e., order, to choose the sample preparation protocol from)
         * Note: This "inheritance" only happens at the child respectively library creation
         */
        if (!isManaged() && !getContainer().isContainerProject() && ((Order) Hibernate.unproxy(getContainer())).getLibraryProtocol() != null && SampleTypeEnum.isLabeled(getType()) && getParents().size() == 1) {
            Sample parent = getParents().iterator().next();
            return parent.getType().equals(SampleTypeEnum.SEQUENCING.getLabel()) || parent.getType().equals(SampleTypeEnum.SINGLE_CELL_SEQUENCING.getLabel());
        }
        return false;
    }

    public boolean isEmpty() {
        boolean ret = !StringHelper.isNotEmpty(getName());
        if (ret) {
            try {
                for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
                    if (sampleAttributeEnum.isAttribute(getType(), getSampleForm(), getQualityControlType()) && !sampleAttributeEnum.equals(SampleAttributeEnum.TUBE_ID) && !sampleAttributeEnum
                        .isEmptySampleAttribute(PropertyUtils.getProperty(this, sampleAttributeEnum.getName()))) {
                        ret = false;
                        break;
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
                ret = false;
            }
        }
        return ret;
    }

    public boolean isFractionatable() {
        return isChildCreatable();
    }

    public boolean isIdContainedInFileName(String fileName) {
        return StringHelper.isNotEmpty(fileName) && (fileName.contains("_S" + getId() + "_") || fileName.contains("_s" + getId() + "_"));
    }

    public boolean isIdPotentiallyContainedInFileName(String fileName) {
        return StringHelper.isNotEmpty(fileName) && fileName.contains("_" + getId() + "_");
    }

    /**
     * Is the calculation for Illumina library related attributes for the final QC enabled?
     * The calculation is enabled iff:
     * - the sample is of type 'Library - Illumina'
     * - the sample has children of type 'Quality Control Sample' with a non-empty molar concentration in range
     */
    public boolean isIlluminaLibraryCalculationEnabledForFinalQc() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && getType()
            .equals(SampleTypeEnum.ILLUMINA_LIBRARY.getLabel()) && !getQcChildrenWithNonEmptyMolarConcentrationInRange().isEmpty();
    }

    /**
     * Is the calculation for Illumina library related attributes for the initial QC enabled?
     * The calculation is enabled iff:
     * - the sample is of type 'Library - Illumina'
     * - the sample has siblings of type 'Quality Control Sample' with a non-empty concentration
     */
    public boolean isIlluminaLibraryCalculationEnabledForInitialQc() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && getType()
            .equals(SampleTypeEnum.ILLUMINA_LIBRARY.getLabel()) && !getQcSiblingsWithNonEmptyConcentration().isEmpty();
    }

    public boolean isInNonUpdatableOrder() {
        for (Order order : getOrders()) {
            if (!order.isUpdatable()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInNonUpdatablePlate() {
        for (SamplePlatePosition samplePlatePosition : getSamplePlatePositions()) {
            if (!samplePlatePosition.getPlate().isUpdatableOrUserUpdatable()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInNonUpdatableRun() {
        for (Run run : getRuns()) {
            if (!run.isUpdatable()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInitialParentSamplesOfUserMultiplexInitializationPermitted() {
        return getType() != null && getMultiplexedByUser() != null && getMultiplexedByUser() && (getType().equals(SampleTypeEnum.SEQUENCING.getLabel()) || isMultiplexedType());
    }

    public boolean isInitialParentSamplesOfUserMultiplexInitialized() {
        return initialParentSamplesOfUserMultiplexInitialized;
    }

    public boolean isLabeledType() {
        return SampleTypeEnum.isLabeled(getType());
    }

    /**
     * Is the sample deletable upon the deletion of the given order w.r.t to the order items, i.e., despite having non-empty order items?
     * Note: The cascade of the delete operation for the given sample is handled in the order service.
     */
    public boolean isLinkedToAnotherOrder(Order order) {
        Set<Order> orderSet = new HashSet<>(getOrders());
        orderSet.remove(order);
        return !orderSet.isEmpty();
    }

    public boolean isMovable() {
        return isCreatable() && !isMultiplexedType() && !isMultiplexType() && !isUserSampleInMultiplexType() && !isOnRunType() && isUpdatable() && isDeletable();
    }

    public boolean isMultiplexIdAndMultiplexId2Empty() {
        return StringHelper.isEmpty(getMultiplexId()) && StringHelper.isEmpty(getMultiplexId2());
    }

    public boolean isMultiplexType() {
        return SampleTypeEnum.MS_SAMPLE_MULTIPLEXED.getLabel().equals(getType());
    }

    public boolean isMultiplexedType() {
        return SampleTypeEnum.isMultiplexed(getType());
    }

    public boolean isMultiplexedTypeAndOnOnlyPendingOrReadyRuns() {
        if (isMultiplexedType()) {
            for (Sample child : getChildren()) {
                if (child.isOnRunType()) {
                    for (Run run : child.getRuns()) {
                        if (!run.isPendingOrReady()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean isNanoporeLibraryCalculationEnabled() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && getType().equals(SampleTypeEnum.NANOPORE_LIBRARY.getLabel());
    }

    public boolean isNotMultiplexed() {
        return getMultiplexed() == null || !getMultiplexed();
    }

    public boolean isOfType(Set<SampleTypeEnum> sampleTypeEnums) {
        for (SampleTypeEnum sampleTypeEnum : sampleTypeEnums) {
            if (sampleTypeEnum.isOfType(getType())) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnRun(Run run) {
        return getRuns().contains(run);
    }

    public boolean isOnRunType() {
        return SampleTypeEnum.isOnRunType(getType());
    }

    public boolean isOnRunTypeAndOnDemultiplexedOrFinishedRun() {
        if (isOnRunType()) {
            for (Run run : getRuns()) {
                if (run.isDemultiplexedOrFinished()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isOnlyOnRun(Run run) {
        return getRuns().size() == 1 && getRuns().contains(run);
    }

    public boolean isPacBioLibraryCalculationEnabled() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && getType().equals(SampleTypeEnum.PACBIO_LIBRARY.getLabel());
    }

    private boolean isQcTypeDependentAttributeSkipForCoupling(Set<SampleAttributeEnum> qcTypeDependentAttributes, SampleAttributeEnum sampleAttributeEnum, SampleQCTypeEnum current, SampleQCTypeEnum changed) {
        return !(!qcTypeDependentAttributes.contains(sampleAttributeEnum) || current != null && changed == null || sampleAttributeEnum.isQCTypeDependentAttributeEnabled(changed));
    }

    public boolean isReMultiplexable() {
        return isCreatable() && getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && (isMultiplexedType() || isMultiplexType()) && (getMultiplexedByUser() == null || !getMultiplexedByUser());
    }

    @Override
    public boolean isReadable() {
        return getContainer() != null && getContainer().isReadable();
    }

    public boolean isRenderFamily() {
        return !isMultiplexedType();
    }

    public boolean isRenderedTree() {
        if (isRenderedTree == null) {
            isRenderedTree = !getAssociatedDatasets().isEmpty() || !getChildren().isEmpty() || !getParents().isEmpty() || !getPlates().isEmpty() || !getRuns().isEmpty() || !getWorkunits().isEmpty();
        }
        return isRenderedTree;
    }

    public boolean isReplacement() {
        return getReplaces() != null;
    }

    public boolean isReplacementCreatable() {
        return isCreatable() && isUserDecisionRequired();
    }

    public boolean isRequiringSameDayProcessing() {
        return getSampleForm() != null && (getSampleForm().equals(SampleFormEnum.BEADS_REQUIRED) || getSampleForm().equals(SampleFormEnum.BEADS_OPTIONAL));
    }

    private boolean isSampleFormDependentAttributeSkipForCoupling(Set<SampleAttributeEnum> sampleFormDependentAttributes, SampleAttributeEnum sampleAttributeEnum, SampleFormEnum current, SampleFormEnum changed) {
        return !(!sampleFormDependentAttributes.contains(sampleAttributeEnum) || current != null && changed == null || sampleAttributeEnum.isSampleFormDependentAttributeEnabled(changed));
    }

    public boolean isSampleTypeSpecificAttributesEditable() {
        return !isManaged() || getContainer() != null && (getContainer().isContainerProject() || !getContainer().isContainerProject() && (!((Order) Hibernate.unproxy(getContainer()))
            .hasServiceTypeReadyMadeLibrariesSequencing() || hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE)));
    }

    public boolean isTubeIdEditable() {
        // The tube id is only editable iff the sample is not created before the bfabric10 release date and is not associated with any order item as well as the current user has the employee role or the current user has the admin role.
        return !isCreatedBeforeBfabric10ReleaseDate() && (getOrderItems().isEmpty() && hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE) || hasCurrentUserRoleEnum(RoleEnum.ADMIN));
    }

    @Override
    public boolean isUpdatable() {
        return getContainer() != null && getContainer().isExtensible() && (hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || !isReplacement() || Order.USER_DECISION_SUBMITTED.equals(getContainer().getCustomStatus())
            || Order.USER_DECISION_REQUIRED.equals(getContainer().getCustomStatus())) && !isInNonUpdatableOrder() && !isInNonUpdatablePlate() && !isInNonUpdatableRun() && (!isAssignedToRuns() || isMultiplexedTypeAndOnOnlyPendingOrReadyRuns())
            && !isOnRunTypeAndOnDemultiplexedOrFinishedRun() && (!isUserSampleInMultiplexType() || hasCurrentUserRoleEnum(RoleEnum.ADMIN)) || isMoved();
    }

    public boolean isUpdatableMultiplex() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && (isMultiplexedType() || isMultiplexType()) && (getMultiplexedByUser() == null || !getMultiplexedByUser()) && isUpdatable() && !isAssignedToRuns();
    }

    @Override
    public boolean isUpdatableWS() {
        return !isUserSampleInMultiplexType() && super.isUpdatableWS();
    }

    public boolean isUserDecisionConfirmRequired() {
        return getParents().size() > 1;
    }

    public Boolean isUserDecisionOnParent() {
        return getSampleType().isUserDecisionOnParent();
    }

    public Boolean isUserDecisionRequired() {
        return SampleUserDecisionEnum.REQUIRED.equals(getUserDecision());
    }

    public boolean isUserDecisionRequiredRendered() {
        return !isUserDecisionRequired() && hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && getContainer() != null && getContainer().isProcessing() && getContainer().isExtensible() && !isUserDecisionRequiredSetForParent();
    }

    public boolean isUserDecisionRequiredSetForParent() {
        if (getSampleType().isUserDecisionOnParent()) {
            for (Sample parent : getParents()) {
                if (parent.isUserDecisionRequired() || parent.isUserDecisionRequiredSetForParent()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUserSampleInMultiplexType() {
        return SampleTypeEnum.USER_LIBRARY_IN_POOL.getLabel().equals(getType());
    }

    /**
     * Is the sample a valid user submitted multiplexed sample
     * Note: A sample is a valid user submitted multiplexed sample iff the sample is either not multiplexed at all or it is multiplexed and has multiplexIds assigned for each sample therein:
     * - all multiplexId set
     * - no multiplexId set and all multiplexId2
     * - the combination (not concatenation) of multiplexId and multiplexId2 needs to be unique
     * - (the multiplexIds only consist of the letters T, C, G, A)
     * - (the multiplexIds have all the same length)
     */
    public String isValidMultiplexedByUser() {
        if (getMultiplexedByUser() != null && getMultiplexedByUser()) {
            if (getParentSamplesOfUserMultiplex().isEmpty()) {
                // A user submitted multiplexed sample must have at least one sample in the multiplex.
                return Constants.REQUIRED;
            }
            /*
             * The combination (not concatenation) of multiplexId and multiplexId2 needs to be unique.
             * AA TT and AA TT --> invalid
             * A ATT and AA TT or AAT T --> valid
             */
            Set<String> concatenatedMultiplexIds = new HashSet<>();
            Map<Integer, Set<String>> multiplexIdLengthConcatenatedMultiplexIdsMap = new HashMap<>();
            Map<Integer, Set<String>> multiplexId2LengthConcatenatedMultiplexIdsMap = new HashMap<>();
            /*
             - all multiplexId set
             - no multiplexId set and all multiplexId2
             */
            int multiplexIdSetCounter = 0;
            int multiplexId2SetCounter = 0;
            int parentSamplesOfUserMultiplexSize = getParentSamplesOfUserMultiplex().size();
            for (Sample parentOfMultiplex : getParentSamplesOfUserMultiplex()) {
                // Allowed characters for userSampleInMultiplexName are alphanumeric, underscores, and hyphens (no whitespaces allowed). The maximum length is 200.
                if (StringHelper.isNotEmpty(parentOfMultiplex.getUserSampleInMultiplexName()) && !Pattern.compile(Constants.SAMPLE_IN_MULTIPLEX_NAME_CHARACTERS_REGEXP)
                    .matcher(parentOfMultiplex.getUserSampleInMultiplexName()).matches()) {
                    return "Invalid name: " + parentOfMultiplex.getUserSampleInMultiplexName();
                }
                if (StringHelper.isNotEmpty(parentOfMultiplex.getMultiplexId())) {
                    multiplexIdSetCounter++;
                }
                if (StringHelper.isNotEmpty(parentOfMultiplex.getMultiplexId2())) {
                    multiplexId2SetCounter++;
                }
            }
            boolean multiplexId2SetOnly = false;
            if (multiplexIdSetCounter < parentSamplesOfUserMultiplexSize && multiplexId2SetCounter < parentSamplesOfUserMultiplexSize) {
                // Samples with no multiplexIds at all or with mixed multiplexId/multiplexId2 set, i.e., multiplexId set and multiplexId2 not set for a sample and vice versa for another.
                return Messages.get("multiplexId") + " or " + Messages.get("multiplexId2") + " " + Constants.REQUIRED;
            }
            if (multiplexIdSetCounter > 0 && multiplexIdSetCounter < parentSamplesOfUserMultiplexSize && multiplexId2SetCounter == parentSamplesOfUserMultiplexSize) {
                return Messages.get("multiplexId") + " " + Constants.REQUIRED;
            }
            if (multiplexIdSetCounter == 0 && multiplexId2SetCounter == parentSamplesOfUserMultiplexSize) {
                // no multiplexId set and all multiplexId2
                multiplexId2SetOnly = true;
            }

            // At this point, either all multiplexId are set or no multiplexId are set and all multiplexId2 are set.
            for (Sample parentOfMultiplex : getParentSamplesOfUserMultiplex()) {
                String aMultiplexId = parentOfMultiplex.getMultiplexId();
                String aMultiplexId2 = parentOfMultiplex.getMultiplexId2();
                if (StringHelper.isEmpty(aMultiplexId) && StringHelper.isEmpty(aMultiplexId2)) {
                    return Messages.get("multiplexId") + " or " + Messages.get("multiplexId2") + " " + Constants.REQUIRED;
                }

                if (!multiplexId2SetOnly) {
                    int multiplexIdLength = aMultiplexId.length();
                    String concatenatedMultiplexId = aMultiplexId + (aMultiplexId2 != null ? aMultiplexId2 : Constants.EMPTY_STRING);
                    if (concatenatedMultiplexIds.contains(concatenatedMultiplexId) && multiplexIdLengthConcatenatedMultiplexIdsMap
                        .containsKey(multiplexIdLength) && multiplexIdLengthConcatenatedMultiplexIdsMap.get(multiplexIdLength).contains(concatenatedMultiplexId)) {
                        return "Not unique multiplexId combination " + aMultiplexId + " " + (aMultiplexId2 != null ? aMultiplexId2 : Constants.EMPTY_STRING);
                    }
                    concatenatedMultiplexIds.add(concatenatedMultiplexId);
                    if (multiplexIdLengthConcatenatedMultiplexIdsMap.containsKey(multiplexIdLength)) {
                        multiplexIdLengthConcatenatedMultiplexIdsMap.get(multiplexIdLength).add(concatenatedMultiplexId);
                    } else {
                        multiplexIdLengthConcatenatedMultiplexIdsMap.put(multiplexIdLength, new HashSet<>(Collections.singleton(concatenatedMultiplexId)));
                    }
                } else {
                    int multiplexId2Length = aMultiplexId2.length();
                    if (concatenatedMultiplexIds.contains(aMultiplexId2) && multiplexId2LengthConcatenatedMultiplexIdsMap
                        .containsKey(multiplexId2Length) && multiplexId2LengthConcatenatedMultiplexIdsMap.get(multiplexId2Length).contains(aMultiplexId2)) {
                        return "Not unique multiplexId combination " + aMultiplexId2;
                    }
                    concatenatedMultiplexIds.add(aMultiplexId2);
                    if (multiplexId2LengthConcatenatedMultiplexIdsMap.containsKey(multiplexId2Length)) {
                        multiplexId2LengthConcatenatedMultiplexIdsMap.get(multiplexId2Length).add(aMultiplexId2);
                    } else {
                        multiplexId2LengthConcatenatedMultiplexIdsMap.put(multiplexId2Length, new HashSet<>(Collections.singleton(aMultiplexId2)));
                    }
                }
            }
        }
        return null;
    }

    public void molaritySampleChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            setMolaritySample((Sample) event.getNewValue());
            if (getMolaritySample().getConcentrationMolarInRange() != null && getMolaritySample().getConcentrationMolarInRange().compareTo(BigDecimal.ZERO) > 0) {
                setMolarity(getMolaritySample().getConcentrationMolarInRange());
                calculateSampleVolumeAndEbtVolumeToAdd();
            }
        } else {
            setMolaritySample(null);
        }
        AJAX.update("@this", Constants.EDIT + ":molarity");
    }

    public void move() throws CloneNotSupportedException {
        Set<Sample> excludeFromCloning = new HashSet<>(Collections.singleton(this));
        Set<Sample> clonedParents = new HashSet<>();
        for (Sample parent : getParents()) {
            clonedParents.add(parent.cloneParents(getContainer(), excludeFromCloning));
        }
        setParents(clonedParents);
        for (Sample child : getChildren()) {
            child.moveChildren(getContainer(), excludeFromCloning);
        }
        moveAttachments();
    }

    private void moveAttachments() {
        for (Comment comment : getComments()) {
            for (Attachment attachment : comment.getAttachments()) {
                String relativePathAttachment = attachment.getRelativePath();
                String relativePathToSampleRepo = relativePathAttachment.substring(0, relativePathAttachment.indexOf(File.separator, relativePathAttachment.indexOf(File.separator) + 1));
                String oldPath = attachment.getStorage().getBasePath() + relativePathToSampleRepo;
                attachment.setRelativePath(getRelativeRepositoryPath() + File.separator + comment.getDirectory() + File.separator + attachment.getName());
                String newPath = attachment.getStorage().getBasePath() + getRelativeRepositoryPath();
                RepositoryHelper.moveImports(new File(oldPath), new File(newPath));
            }
        }
    }

    private void moveChildren(Container target, Set<Sample> excludeFromCloning) throws CloneNotSupportedException {
        excludeFromCloning.add(this);
        for (Sample child : getChildren()) {
            child.moveChildren(target, excludeFromCloning);
        }
        setContainer(target);
        moveAttachments();
        Set<Sample> clonedParents = new HashSet<>();
        for (Sample parent : getParents()) {
            if (!excludeFromCloning.contains(parent)) {
                clonedParents.add(parent.cloneParents(getContainer(), excludeFromCloning));
            }
        }
        getParents().addAll(clonedParents);
        for (Sample clonedParent : clonedParents) {
            getParents().remove(clonedParent.getClone());
        }
        moveAttachments();
    }

    @Override
    protected void preRemove() {
        super.preRemove();
        for (Sample parent : getParents()) {
            parent.getChildren().remove(this);
        }
    }

    public void presetSamplePreparationProtocolFromParentIfEligible() {
        if (isEligibleForSamplePreparationProtocolPreset()) {
            setSamplePreparationProtocol((SamplePreparationProtocol) Hibernate.unproxy(((Order) Hibernate.unproxy(getContainer())).getLibraryProtocol()));
        }
    }

    public void recomputeParentSamplesOfUserMultiplex() {
        List<Sample> parentSamplesOfUserMultiplexInitial = new ArrayList<>();
        for (Sample sample : getParents()) {
            if (sample.isUserSampleInMultiplexType()) {
                if (sample.getNamePrefix() == null) {
                    sample.initializeNamePrefixAndUserSampleInMultiplexName();
                }
                parentSamplesOfUserMultiplexInitial.add(sample);
            }
        }
        setParentSamplesOfUserMultiplex(parentSamplesOfUserMultiplexInitial);
    }

    public void removeParentSamplesOfUserMultiplex() {
        getParents().removeIf(Sample::isUserSampleInMultiplexType);
    }

    public void resetFields() {
        for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
            if (!getOldSampleAttributeEnums().contains(sampleAttributeEnum) && (SampleAttributeEnum.hasSampleTypeSampleForm(getType()) && sampleAttributeEnum
                .isSampleFormDependentAttribute() && !sampleAttributeEnum.isSampleFormDependentAttributeEnabled(getType(), getSampleForm()) || SampleTypeEnum.QUALITY_CONTROL.getLabel()
                .equals(getType()) && sampleAttributeEnum.isQCTypeDependentAttribute() && !sampleAttributeEnum
                .isQCTypeDependentAttributeEnabled(getType(), getQualityControlType()) || !sampleAttributeEnum
                .isAttribute(getType()) || getMultiplexed() != null && getMultiplexed() && (sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEX_ID) || sampleAttributeEnum
                .equals(SampleAttributeEnum.MULTIPLEX_ID_2)))) {
                try {
                    Field field = Sample.class.getDeclaredField(sampleAttributeEnum.getName());
                    if (!sampleAttributeEnum.isAnnotationTypeMultiValued()) {
                        field.set(this, null);
                    } else {
                        field.set(this, new HashSet<>());
                    }
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ignored) {
                }
            }
        }
        // Set the sample preparation protocol to null if it is not associated with the current sample type.
        if (getSamplePreparationProtocol() != null && !getSampleType().getSamplePreparationProtocols().contains(getSamplePreparationProtocol())) {
            setSamplePreparationProtocol(null);
        }
        // Set the status to null if qcPassed is true; in case user manually set a status for a qcPassed sample.
        if (getQcPassed() != null && getQcPassed()) {
            setStatus(null);
        }
        // Set the control sample to null if the current sample type is not 'Control Sample';
        if (getControlSample() != null && !getSampleType().getName().equals(SampleTypeEnum.CONTROL_SAMPLE.getLabel())) {
            setControlSample(null);
        }
    }

    public void resetMultiplexIdAssignmentSample(Set<Sample> initialParentsOfMultiplexIdAssignmentSample) {
        getParents().clear();
        setParents(new HashSet<>(initialParentsOfMultiplexIdAssignmentSample));
        recomputeParentSamplesOfUserMultiplex();
        for (Sample parentSample : getParents()) {
            parentSample.setCurrentMultiplexIdsToOld();
            parentSample.resetOldMultiplexIds();
            parentSample.setCurrentNamePrefixToOld();
            parentSample.resetOldNamePrefix();
            parentSample.setCurrentUserSampleInMultiplexNameToOld();
            parentSample.resetOldUserSampleInMultiplexName();
        }
    }

    public void resetOldMultiplexIds() {
        setMultiplexId1Old(null);
        setMultiplexId2Old(null);
    }

    public void resetOldNamePrefix() {
        setOldNamePrefix(null);
    }

    public void resetOldUserSampleInMultiplexName() {
        setOldUserSampleInMultiplexName(null);
    }

    public void resetSelectedMultiplexId2AndSelectionMultiplexId2s() {
        setSelectedMultiplexId2(null);
        setSelectionMultiplexId2s(null);
    }

    public void resetSelectedMultiplexIdAndSelectionMultiplexIds() {
        setSelectedMultiplexId(null);
        setSelectionMultiplexIds(null);
    }

    public void sampleAttributeValueChanged(ValueChangeEvent event) {
        final String sampleAttributeName = ((UIInput) event.getSource()).getClientId().replaceAll(Constants.EDIT + ":", Constants.EMPTY_STRING);
        if (SampleAttributeEnum.CONCENTRATION_INPUT_QC.getName().equals(sampleAttributeName)) {
            if (event.getNewValue() != null && ((BigDecimal) event.getNewValue()).compareTo(BigDecimal.ZERO) > 0) {
                setConcentrationInputQc((BigDecimal) event.getNewValue());
                calculateVolumeDilutionSampleAndWater();
            }
        } else if (SampleAttributeEnum.AMOUNT_INPUT.getName().equals(sampleAttributeName)) {
            if (event.getNewValue() != null) {
                setAmountInput((BigDecimal) event.getNewValue());
                calculateVolumeDilutionSampleAndWater();
            }
        } else if (SampleAttributeEnum.VOLUME_INPUT.getName().equals(sampleAttributeName)) {
            if (event.getNewValue() != null) {
                setVolumeInput((BigDecimal) event.getNewValue());
                calculateVolumeDilutionWater();
            }
        } else if (SampleAttributeEnum.VOLUME_DILUTION_SAMPLE.getName().equals(sampleAttributeName) && event.getNewValue() != null) {
            setVolumeDilutionSample((BigDecimal) event.getNewValue());
            calculateVolumeDilutionWater();
        }
        if (SampleAttributeEnum.MOLARITY_TARGET.getName().equals(sampleAttributeName)) {
            if (event.getNewValue() != null) {
                setMolarityTarget((BigDecimal) event.getNewValue());
                calculateSampleVolumeAndEbtVolumeToAdd();
            }
        } else if (SampleAttributeEnum.VOLUME_TARGET.getName().equals(sampleAttributeName)) {
            if (event.getNewValue() != null) {
                setVolumeTarget((BigDecimal) event.getNewValue());
                calculateSampleVolumeAndEbtVolumeToAdd();
            }
        } else if (SampleAttributeEnum.MOLARITY.getName().equals(sampleAttributeName)) {
            if (event.getNewValue() != null && ((BigDecimal) event.getNewValue()).compareTo(BigDecimal.ZERO) > 0) {
                setMolarity((BigDecimal) event.getNewValue());
                calculateSampleVolumeAndEbtVolumeToAdd();
            }
        } else if (SampleAttributeEnum.VOLUME_TO_ADD_SAMPLE.getName().equals(sampleAttributeName) && event.getNewValue() != null) {
            setVolumeToAddSample((BigDecimal) event.getNewValue());
            calculateEbtVolumeToAdd();
        }
        if (event.getNewValue() != null && (SampleAttributeEnum.CONCENTRATION.getName().equals(sampleAttributeName) || SampleAttributeEnum.VOLUME.getName().equals(sampleAttributeName))) {
            if (SampleAttributeEnum.CONCENTRATION.getName().equals(sampleAttributeName)) {
                setConcentration((BigDecimal) event.getNewValue());
            } else {
                setVolume((BigDecimal) event.getNewValue());
            }
            calculateAmountTotal();
        }
        AJAX.update("@this");
    }

    public void selectedMultiplexIdChanged(ValueChangeEvent event) {
        selectedMultiplexIdChangedHelper(event, (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("sampleAttributeEnumName"));
        AJAX.update("@this", Constants.EDIT + ":" + Constants.MULTIPLEX_ID, Constants.EDIT + ":" + Constants.MULTIPLEX_ID_2);
    }

    public void selectedMultiplexIdChangedHelper(ValueChangeEvent event, String sampleAttributeEnumName) {
        if (event.getNewValue() != null) {
            if (Constants.MULTIPLEX_ID.equals(sampleAttributeEnumName)) {
                setChanged(true);
                setSelectedMultiplexId((MultiplexId) event.getNewValue());
                setMultiplexId(getSelectedMultiplexId().getSequence());
            } else if (Constants.MULTIPLEX_ID_2.equals(sampleAttributeEnumName)) {
                setChanged(true);
                setSelectedMultiplexId2((MultiplexId) event.getNewValue());
                setMultiplexId2(getSelectedMultiplexId2().getSequence());
            }
        }
    }

    public void selectionValueChanged(ValueChangeEvent event) {
        final FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            final SampleAttributeEnum sampleAttributeEnum = SampleAttributeEnum.getAttributeByName((String) UIComponent.getCurrentComponent(context).getAttributes().get("sampleAttributeEnum"));
            // Currently, only the multiplexKit and multiplexKit2 selection need actions on value change.
            if (sampleAttributeEnum != null) {
                if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum)) {
                    resetSelectedMultiplexIdAndSelectionMultiplexIds();
                    if (event.getNewValue() != null) {
                        setMultiplexKit((MultiplexKit) event.getNewValue());
                    }
                    AJAX.update(Constants.EDIT + ":" + Constants.STRING_TYPE + Constants.MULTIPLEX_ID);
                } else if (SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                    resetSelectedMultiplexId2AndSelectionMultiplexId2s();
                    if (event.getNewValue() != null) {
                        setMultiplexKit((MultiplexKit) event.getNewValue());
                    }
                    AJAX.update(Constants.EDIT + ":" + Constants.STRING_TYPE + Constants.MULTIPLEX_ID_2);
                }
            }
        }
    }

    public void setAge(final BigDecimal age) {
        this.age = age;
    }

    public void setAgeUnit(final String ageUnit) {
        this.ageUnit = ageUnit;
    }

    public void setAmountEluted(BigDecimal amountEluted) {
        this.amountEluted = amountEluted;
    }

    public void setAmountInput(BigDecimal amountInput) {
        this.amountInput = amountInput;
        calculateVolumeDilutionSampleAndWater();
    }

    public void setAmountTotal(BigDecimal amountTotal) {
        this.amountTotal = amountTotal;
    }

    public void setAmpliconSequence(String ampliconSequence) {
        this.ampliconSequence = StringHelper.format(ampliconSequence);
    }

    public void setAnnotation(Annotation annotation) {
        String trimmedName = annotation.getType().replaceAll("\\s+", Constants.EMPTY_STRING);
        for (Field field : Sample.class.getDeclaredFields()) {
            try {
                if (field.getName().equalsIgnoreCase(trimmedName)) {
                    PropertyUtils.setProperty(this, field.getName(), annotation);
                    break;
                } else if (field.getName().equalsIgnoreCase(trimmedName.concat(Constants.PLURAL_S))) { // attribute is an annotation list
                    ((Set<Annotation>) PropertyUtils.getProperty(this, field.getName().concat("AsSet"))).add(annotation);
                    break;
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        }
    }

    public void setArrayDesignName(final String arrayDesignName) {
        this.arrayDesignName = arrayDesignName;
    }

    public void setAsiaScale(Annotation asiaScale) {
        this.asiaScale = asiaScale;
    }

    public void setAssignSampleValuesChanged(boolean assignSampleValuesChanged) {
        this.assignSampleValuesChanged = assignSampleValuesChanged;
    }

    public void setAverageSizeInRange(BigDecimal averageSizeInRange) {
        this.averageSizeInRange = averageSizeInRange;
    }

    public void setBaitId(String baitId) {
        this.baitId = StringHelper.format(baitId);
    }

    public void setBeadsType(String beadsType) {
        this.beadsType = StringHelper.format(beadsType);
    }

    public void setBias(BigDecimal bias) {
        this.bias = bias;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setBlock(String blockCounterPrefix, int blockCounter) {
        if (StringHelper.isNotEmpty(blockCounterPrefix)) {
            setBlock(blockCounterPrefix.trim() + blockCounter);
        } else {
            setBlock(String.valueOf(blockCounter));
        }
    }

    public void setBuffer(String buffer) {
        this.buffer = StringHelper.format(buffer);
    }

    public void setCalculatedAttributes() {
        if (getAmountTotal() == null) {
            calculateAmountTotal();
        }
        if (getVolumeToAddSample() == null) {
            calculateSampleVolumeAndEbtVolumeToAdd();
        }
        if (getVolumeToAddEbt() == null) {
            calculateEbtVolumeToAdd();
        }
        if (getVolumeDilutionSample() == null) {
            calculateVolumeDilutionSampleAndWater();
        }
        if (getVolumeDilutionWater() == null) {
            calculateVolumeDilutionWater();
        }
    }

    public void setCellCompartment(final String cellCompartment) {
        this.cellCompartment = cellCompartment;
    }

    public void setCellConcentration(BigDecimal cellConcentration) {
        this.cellConcentration = cellConcentration;
    }

    public void setCellLine(final String cellLine) {
        this.cellLine = cellLine;
    }

    public void setCellNumbers(BigDecimal cellNumbers) {
        this.cellNumbers = cellNumbers;
    }

    public void setCellSize(BigDecimal cellSize) {
        this.cellSize = cellSize;
    }

    public void setCellType(final String cellType) {
        this.cellType = cellType;
    }

    public void setCellViability(BigDecimal cellViability) {
        this.cellViability = cellViability;
    }

    public void setCharges(Set<Charge> charges) {
        this.charges = charges;
    }

    public void setChemicalModifications(final List<Annotation> chemicalModifications) {
        this.chemicalModifications.clear();
        if (chemicalModifications != null) {
            this.chemicalModifications.addAll(chemicalModifications);
        }
    }

    public void setChildren(Set<Sample> children) {
        this.children = children;
    }

    public void setChildrenAsList(List<Sample> children) {
        this.children = (Set<Sample>) CollectionHelper.asSet(children);
    }

    public void setComments(Set<SampleComment> comments) {
        this.comments = comments;
    }

    public void setCompoundClass(final Annotation compoundClass) {
        this.compoundClass = compoundClass;
    }

    public void setConcentration(BigDecimal concentration) {
        this.concentration = concentration;
        calculateAmountTotal();
    }

    public void setConcentrationInRange(BigDecimal concentrationInRange) {
        this.concentrationInRange = concentrationInRange;
    }

    public void setConcentrationInputQc(BigDecimal concentrationInputQc) {
        this.concentrationInputQc = concentrationInputQc;
        calculateVolumeDilutionSampleAndWater();
    }

    public void setConcentrationLoading(BigDecimal concentrationLoading) {
        this.concentrationLoading = concentrationLoading;
    }

    public void setConcentrationMolar(BigDecimal concentrationMolar) {
        this.concentrationMolar = concentrationMolar;
    }

    public void setConcentrationMolarInRange(BigDecimal concentrationMolarInRange) {
        this.concentrationMolarInRange = concentrationMolarInRange;
    }

    public void setConcentrationProtein(BigDecimal concentrationProtein) {
        this.concentrationProtein = concentrationProtein;
    }

    public void setCondition(String condition) {
        String formattedCondition = StringHelper.format(condition);
        if (formattedCondition != null) {
            formattedCondition = formattedCondition.replace(" ", "_");
        }
        this.condition = formattedCondition;
    }

    public void setControlSample(ControlSample controlSample) {
        this.controlSample = controlSample;
    }

    public void setControlSampleParent(Sample controlSampleParent) {
        this.controlSampleParent = controlSampleParent;
    }

    public void setCorrectionRate(BigDecimal correctionRate) {
        this.correctionRate = correctionRate;
    }

    public void setCoverage(BigDecimal coverage) {
        this.coverage = coverage;
    }

    public void setCq(BigDecimal cq) {
        this.cq = cq;
    }

    public void setCrisprLibrary(String crisprLibrary) {
        this.crisprLibrary = StringHelper.format(crisprLibrary);
    }

    public void setCurrentMultiplexIdsToOld() {
        setMultiplexId(getMultiplexId1Old());
        setMultiplexId2(getMultiplexId2Old());
    }

    public void setCurrentNamePrefixToOld() {
        setNamePrefix(getOldNamePrefix());
    }

    public void setCurrentUserSampleInMultiplexNameToOld() {
        setUserSampleInMultiplexName(getOldUserSampleInMultiplexName());
    }

    public void setDevelopmentStage(final String developmentStage) {
        this.developmentStage = developmentStage;
    }

    public void setDigestionProtocol(Annotation digestionProtocol) {
        this.digestionProtocol = digestionProtocol;
    }

    public void setDilution(String dilution) {
        this.dilution = StringHelper.format(dilution);
    }

    public void setDiseaseState(final String diseaseState) {
        this.diseaseState = StringHelper.format(diseaseState);
    }

    public void setDmxFlag(String dmxFlag) {
        this.dmxFlag = StringHelper.format(dmxFlag);
    }

    public void setDsOdn(Annotation dsodn) {
        this.dsOdn = dsodn;
    }

    public void setDv200(BigDecimal dv200) {
        this.dv200 = dv200;
    }

    public void setEffectorType(Annotation effectorType) {
        this.effectorType = effectorType;
    }

    public void setEmbeddingMedium(Annotation embeddingMedium) {
        this.embeddingMedium = embeddingMedium;
    }

    public void setEnzymes(final List<Annotation> enzymes) {
        this.enzymes.clear();
        if (enzymes != null) {
            this.enzymes.addAll(enzymes);
        }
    }

    public void setExpressionSystem(Annotation expressionSystem) {
        this.expressionSystem = expressionSystem;
    }

    public void setExtractionProtocol(final Annotation extractionProtocolAnnotation) {
        extractionProtocol = extractionProtocolAnnotation;
    }

    public void setExtractionProtocolString(String extractionProtocol) {
        extractionProtocolString = StringHelper.format(extractionProtocol);
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public void setFastqScreen(String fastqScreen) {
        this.fastqScreen = StringHelper.format(fastqScreen);
    }

    public void setFinalNameForSampleOfTypeUserSampleInMultiplex() {
        setName(getNamePrefix() + Constants.IN_MULTIPLEX_SAMPLE_NAME_SEPARATOR + (StringHelper
            .isNotEmpty(getUserSampleInMultiplexName()) ? getUserSampleInMultiplexName() : Constants.EMPTY_STRING));
    }

    public void setFixation(Annotation fixation) {
        this.fixation = fixation;
    }

    public void setFraction(Boolean fraction) {
        this.fraction = fraction;
    }

    public void setGeneticModification(final String geneticModification) {
        this.geneticModification = geneticModification;
    }

    public void setGenomicCoordinates(String genomicCoordinates) {
        this.genomicCoordinates = genomicCoordinates;
    }

    public void setGenotype(final String genotype) {
        this.genotype = genotype;
    }

    public void setGroupingVar(Annotation groupingVar) {
        this.groupingVar = groupingVar;
    }

    public void setGrowthConditions(final String growthConditions) {
        this.growthConditions = growthConditions;
    }

    public void setGuideName(String guideName) {
        this.guideName = StringHelper.format(guideName);
    }

    public void setGuideSequence(String guideSequence) {
        this.guideSequence = StringHelper.format(guideSequence);
    }

    public void setHybridizationProtocol(final String hybridizationProtocol) {
        this.hybridizationProtocol = StringHelper.format(hybridizationProtocol);
    }

    public void setImmunoPrecipitationTarget(String immunoPrecipitationTarget) {
        this.immunoPrecipitationTarget = StringHelper.format(immunoPrecipitationTarget);
    }

    public void setImportResources(Set<ImportResource> importResources) {
        this.importResources = importResources;
    }

    public void setIndividualId(final String individualId) {
        this.individualId = StringHelper.format(individualId);
    }

    public void setInitialParentSamplesOfUserMultiplexInitialized(boolean initialParentSamplesOfUserMultiplexInitialized) {
        this.initialParentSamplesOfUserMultiplexInitialized = initialParentSamplesOfUserMultiplexInitialized;
    }

    public void setInitialTimePoint(final Annotation initialTimePoint) {
        this.initialTimePoint = initialTimePoint;
    }

    public void setInputQcSample(Sample inputQcSample) {
        this.inputQcSample = inputQcSample;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentMethod(String instrumentMethod) {
        this.instrumentMethod = StringHelper.format(instrumentMethod);
    }

    public void setIntegrityNumber(BigDecimal integrityNumber) {
        this.integrityNumber = integrityNumber;
    }

    public void setInternalStandards(final List<Annotation> internalStandards) {
        this.internalStandards.clear();
        if (internalStandards != null) {
            this.internalStandards.addAll(internalStandards);
        }
    }

    public void setIrts(String irts) {
        this.irts = StringHelper.format(irts);
    }

    public void setLabelAmount(BigDecimal labelAmount) {
        this.labelAmount = labelAmount;
    }

    public void setLabelingMethod(Annotation labelingMethod) {
        this.labelingMethod = labelingMethod;
    }

    public void setLabelingProtocol(final String labelingProtocol) {
        this.labelingProtocol = StringHelper.format(labelingProtocol);
    }

    public void setLibraryProtocol(String libraryProtocol) {
        this.libraryProtocol = StringHelper.format(libraryProtocol);
    }

    public void setLibrarySelection(String librarySelection) {
        this.librarySelection = StringHelper.format(librarySelection);
    }

    public void setLibraryStrategy(String libraryStrategy) {
        this.libraryStrategy = StringHelper.format(libraryStrategy);
    }

    public void setLotInformation(String lotInformation) {
        this.lotInformation = lotInformation;
    }

    public void setLysisBuffer(String lysisBuffer) {
        this.lysisBuffer = StringHelper.format(lysisBuffer);
    }

    public void setMatrix(Annotation matrix) {
        this.matrix = matrix;
    }

    public void setMedia(String media) {
        this.media = StringHelper.format(media);
    }

    public void setMolarity(BigDecimal molarity) {
        this.molarity = molarity;
        calculateSampleVolumeAndEbtVolumeToAdd();
    }

    public void setMolarityFmol(BigDecimal molarityFmol) {
        this.molarityFmol = molarityFmol;
    }

    public void setMolaritySample(Sample molaritySample) {
        this.molaritySample = molaritySample;
    }

    public void setMolarityTarget(BigDecimal molarityTarget) {
        this.molarityTarget = molarityTarget;
        calculateSampleVolumeAndEbtVolumeToAdd();
    }

    public void setMolecularWeight(BigDecimal molecularWeight) {
        this.molecularWeight = molecularWeight;
    }

    public void setMultiplexId(String multiplexId) {
        this.multiplexId = StringHelper.format(multiplexId);
    }

    public void setMultiplexId1Old(String multiplexId1Old) {
        this.multiplexId1Old = multiplexId1Old;
    }

    public void setMultiplexId2(String multiplexId2) {
        this.multiplexId2 = StringHelper.format(multiplexId2);
    }

    public void setMultiplexId2Dmx(String multiplexId2Dmx) {
        this.multiplexId2Dmx = StringHelper.format(multiplexId2Dmx);
    }

    public void setMultiplexId2NameWithSequence(String multiplexId2NameWithSequence) {
        this.multiplexId2NameWithSequence = StringHelper.format(multiplexId2NameWithSequence);
    }

    private void setMultiplexId2NameWithSequenceFromMultiplexId(MultiplexId multiplexId) {
        multiplexId2NameWithSequence = multiplexId.getName() + " (" + multiplexId.getSequence() + ")";
    }

    private void setMultiplexId2NameWithoutSequenceFromMultiplexId(MultiplexId multiplexId) {
        multiplexId2NameWithoutSequence = multiplexId.getName();
    }

    public void setMultiplexId2Old(String multiplexId2Old) {
        this.multiplexId2Old = multiplexId2Old;
    }

    public void setMultiplexIdConflictMultiplexedSample(Sample multiplexIdConflictMultiplexedSample) {
        this.multiplexIdConflictMultiplexedSample = multiplexIdConflictMultiplexedSample;
    }

    public void setMultiplexIdDmx(String multiplexIdDmx) {
        this.multiplexIdDmx = StringHelper.format(multiplexIdDmx);
    }

    public void setMultiplexIdNameWithSequence(String multiplexIdNameWithSequence) {
        this.multiplexIdNameWithSequence = StringHelper.format(multiplexIdNameWithSequence);
    }

    private void setMultiplexIdNameWithSequenceFromMultiplexId(MultiplexId multiplexId) {
        multiplexIdNameWithSequence = multiplexId.getName() + " (" + multiplexId.getSequence() + ")";
    }

    private void setMultiplexIdNameWithoutSequenceFromMultiplexId(MultiplexId multiplexId) {
        multiplexIdNameWithoutSequence = multiplexId.getName();
    }

    public void setMultiplexKit(MultiplexKit multiplexKit) {
        this.multiplexKit = multiplexKit;
    }

    public void setMultiplexKit2(MultiplexKit multiplexKit2) {
        this.multiplexKit2 = multiplexKit2;
    }

    public void setMultiplexed(Boolean multiplexed) {
        if (isMultiplexedType() || isMultiplexType()) {
            this.multiplexed = true;
        } else {
            this.multiplexed = multiplexed;
        }
    }

    public void setMultiplexedByUser(Boolean multiplexedByUser) {
        this.multiplexedByUser = multiplexedByUser;
    }

    @Override
    public void setName(String name) {
        super.setName(formatName(name));
    }

    public void setNameFromUserSampleName(boolean setChanged) {
        if (StringHelper.isNotEmpty(getUserSampleName())) {
            if (getOldUserSampleName() == null || !getOldUserSampleName().equals(getUserSampleName())) {
                setName(getUserSampleName());
                if (setChanged) {
                    setChanged(true);
                }
            }
        } else {
            if (StringHelper.isNotEmpty(getOldUserSampleName())) {
                setName(getUserSampleName());
                if (setChanged) {
                    setChanged(true);
                }
            }
        }
        setOldUserSampleName(null);
        setUserSampleName(null);
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = StringHelper.format(namePrefix);
    }

    public void setNumberOfCellsLoaded(Integer numberOfCellsLoaded) {
        this.numberOfCellsLoaded = numberOfCellsLoaded;
    }

    public void setNumberOfCycles(Integer numberOfCycles) {
        this.numberOfCycles = numberOfCycles;
    }

    public void setOldMultiplexIdsToCurrent() {
        setMultiplexId1Old(getMultiplexId());
        setMultiplexId2Old(getMultiplexId2());
    }

    public void setOldNamePrefix(String oldNamePrefix) {
        this.oldNamePrefix = oldNamePrefix;
    }

    public void setOldNamePrefixToCurrent() {
        setOldNamePrefix(getNamePrefix());
    }

    public void setOldSampleAttributeEnums() {
        if (oldSampleAttributeEnums == null) {
            oldSampleAttributeEnums = new ArrayList<>();
            for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
                if (!sampleAttributeEnum.isAttribute(getType())) {
                    try {
                        Object value = PropertyUtils.getProperty(this, sampleAttributeEnum.getName());
                        boolean empty = value == null || sampleAttributeEnum.isStringType() && StringHelper.isEmpty((String) value) || sampleAttributeEnum.getMultiValued() && ((Collection<?>) value)
                            .isEmpty();
                        if (!empty) {
                            getOldSampleAttributeEnums().add(sampleAttributeEnum);
                        }
                    } catch (ClassCastException | IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException ignored) {
                    }
                }
            }
        }
    }

    public void setOldUserSampleInMultiplexName(String oldUserSampleInMultiplexName) {
        this.oldUserSampleInMultiplexName = oldUserSampleInMultiplexName;
    }

    public void setOldUserSampleInMultiplexNameToCurrent() {
        setOldUserSampleInMultiplexName(getUserSampleInMultiplexName());
    }

    public void setOldUserSampleName(String oldUserSampleName) {
        this.oldUserSampleName = oldUserSampleName;
    }

    public void setOnSlideModification(Annotation onSlideModification) {
        this.onSlideModification = onSlideModification;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setOrganism(String organism) {
        this.organism = StringHelper.format(organism);
    }

    public void setOrganismPart(final Annotation organismPart) {
        this.organismPart = organismPart;
    }

    public void setParentSamplesOfUserMultiplex(List<Sample> parentSamplesOfUserMultiplex) {
        this.parentSamplesOfUserMultiplex = parentSamplesOfUserMultiplex;
        if (this.parentSamplesOfUserMultiplex != null) {
            boolean isSortByName = false;
            for (Sample sample : this.parentSamplesOfUserMultiplex) {
                if (sample.getNamePrefix() == null) {
                    isSortByName = true;
                    break;
                }
            }
            if (isSortByName) {
                this.parentSamplesOfUserMultiplex.sort(Comparator.comparing(Sample::getName));
            } else {
                this.parentSamplesOfUserMultiplex.sort(Comparator.comparing(Sample::getNamePrefix));
            }
        }
    }

    public void setParents(Set<Sample> parents) {
        if (parents != null && !parents.equals(this.parents)) {
            // Set index dependents to true when this attribute is changed.
            setIndexDependents(true);
        }
        this.parents = parents;
    }

    public void setParentsAsList(final List<Sample> parents) {
        setParents((Set<Sample>) CollectionHelper.asSet(parents));
    }

    public void setParentsExcludingUserSampleInMultiplexType(final List<Sample> parents) {
        setParents((Set<Sample>) CollectionHelper.asSet(parents));
    }

    public void setPreTreatment(String preTreatment) {
        this.preTreatment = StringHelper.format(preTreatment);
    }

    public void setProteinAmount(BigDecimal proteinAmount) {
        this.proteinAmount = proteinAmount;
    }

    public void setPurityA260230(BigDecimal purityA260230) {
        this.purityA260230 = purityA260230;
    }

    public void setPurityA260280(BigDecimal purityA260280) {
        this.purityA260280 = purityA260280;
    }

    public void setQcPassed(Boolean qcPassed) {
        this.qcPassed = qcPassed;
    }

    public void setQpcr(BigDecimal qpcr) {
        this.qpcr = qpcr;
    }

    public void setQualityControlType(SampleQCTypeEnum qualityControlType) {
        this.qualityControlType = qualityControlType;
    }

    public void setQubit(BigDecimal qubit) {
        this.qubit = qubit;
    }

    public void setReMultiplexed(Boolean reMultiplexed) {
        this.reMultiplexed = reMultiplexed;
    }

    public void setReadCount(BigDecimal readCount) {
        this.readCount = readCount;
    }

    public void setReadCountTotal(BigDecimal readCountTotal) {
        this.readCountTotal = readCountTotal;
    }

    public void setReadRequestParameter(XMLRequestParameterReadSample readRequestParameter) {
        this.readRequestParameter = readRequestParameter;
    }

    public void setReplaces(Sample replaces) {
        if (replaces != null && this.replaces != replaces) {
            replaces.setUserDecision(SampleUserDecisionEnum.REPLACED);
            setTubeIdToNext();
        }
        this.replaces = replaces;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public void setRunSamples(Set<RunSample> runSamples) {
        this.runSamples = runSamples;
    }

    public void setRunUnitLane(RunUnitLane runUnitLane) {
        this.runUnitLane = runUnitLane;
    }

    public void setRunUnitLanes(Set<RunUnitLane> runUnitLanes) {
        this.runUnitLanes = runUnitLanes;
    }

    public void setSampleForm(SampleFormEnum sampleForm) {
        this.sampleForm = sampleForm;
    }

    public void setSamplePlatePositions(Set<SamplePlatePosition> samplePlatePositions) {
        this.samplePlatePositions = samplePlatePositions;
    }

    public void setSamplePreparationProtocol(SamplePreparationProtocol samplePreparationProtocol) {
        this.samplePreparationProtocol = samplePreparationProtocol;
    }

    public void setSamplePreparationProtocolFromOrder() {
        if (!getContainer().isContainerProject() && ((Order) Hibernate.unproxy(getContainer())).getLibraryProtocol() != null && getSampleType() != null && getSampleType()
            .getSamplePreparationProtocols().contains(((Order) Hibernate.unproxy(getContainer())).getLibraryProtocol())) {
            setSamplePreparationProtocol((SamplePreparationProtocol) Hibernate.unproxy(((Order) Hibernate.unproxy(getContainer())).getLibraryProtocol()));
        }
    }

    public void setSampleType(SampleType type) {
        if (type != null) {
            sampleType = type;
            setType(type.getName());
        }
    }

    public void setSamplingDate(LocalDateTime samplingDate) {
        this.samplingDate = samplingDate;
    }

    public void setScanningProtocol(final String scanningProtocol) {
        this.scanningProtocol = scanningProtocol;
    }

    public void setSelectedMultiplexId(MultiplexId selectedMultiplexId) {
        this.selectedMultiplexId = selectedMultiplexId;
    }

    public void setSelectedMultiplexId2(MultiplexId selectedMultiplexId2) {
        this.selectedMultiplexId2 = selectedMultiplexId2;
    }

    public void setSelectionMultiplexId2s(List<MultiplexId> selectionMultiplexId2s) {
        this.selectionMultiplexId2s = selectionMultiplexId2s;
    }

    public void setSelectionMultiplexIds(List<MultiplexId> selectionMultiplexIds) {
        this.selectionMultiplexIds = selectionMultiplexIds;
    }

    public void setSeparationTechniques(final List<Annotation> separationTechniques) {
        this.separationTechniques.clear();
        if (separationTechniques != null) {
            this.separationTechniques.addAll(separationTechniques);
        }
    }

    public void setSequencingMethod(Annotation sequencingMethod) {
        this.sequencingMethod = sequencingMethod;
    }

    public void setSequencingMode(Annotation sequencingMode) {
        this.sequencingMode = sequencingMode;
    }

    public void setSequencingPlatform(String sequencingPlatform) {
        this.sequencingPlatform = StringHelper.format(sequencingPlatform);
    }

    public void setSequencingPrimer(Annotation sequencingPrimer) {
        this.sequencingPrimer = sequencingPrimer;
    }

    public void setSex(final Annotation sex) {
        this.sex = sex;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public void setSizeAverage(BigDecimal sizeAverage) {
        this.sizeAverage = sizeAverage;
    }

    public void setSizeGenomeEstimated(BigDecimal sizeGenomeEstimated) {
        this.sizeGenomeEstimated = sizeGenomeEstimated;
    }

    public void setSizeRange(String sizeRange) {
        this.sizeRange = StringHelper.format(sizeRange);
    }

    public void setSlideType(Annotation slideType) {
        this.slideType = slideType;
    }

    public void setSourceType(Annotation sourceType) {
        this.sourceType = sourceType;
    }

    public void setSpecies(final Annotation species) {
        this.species = species;
    }

    public void setSpeciesString(String speciesString) {
        this.speciesString = speciesString;
        if (StringHelper.isNotEmpty(speciesString)) {
            List<Annotation> speciesAnnotations = CDI.current().select(AnnotationService.class).get().getAnnotationsByNameAndType(speciesString, "Species");
            if (speciesAnnotations != null && !speciesAnnotations.isEmpty()) {
                setSpecies(speciesAnnotations.get(0));
            } else {
                setSpecies(null);
            }
        } else {
            setSpecies(null);
        }
    }

    public void setStatus(SampleStatusEnum status) {
        this.status = status;
    }

    public void setStrain(final String strain) {
        this.strain = strain;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = StringHelper.format(subjectId);
    }

    public void setSurface(Annotation surface) {
        this.surface = surface;
    }

    public void setTissue(String tissue) {
        this.tissue = StringHelper.format(tissue);
    }

    public void setTreatment(final Annotation treatment) {
        this.treatment = treatment;
    }

    public void setTs(BigDecimal ts) {
        this.ts = ts;
    }

    public void setTubeId(String tubeId) {
        this.tubeId = StringHelper.format(tubeId);
    }

    public void setTubeIdBySuffix(long suffix) {
        setTubeId(getContainer().getId() + "/" + suffix);
    }

    public void setTubeIdPadded(String tubeIdPadded) {
        this.tubeIdPadded = StringHelper.format(tubeIdPadded);
    }

    public void setTubeIdToNext() {
        setTubeIdBySuffix(getEntityService().getNextTubeIdSuffix(getContainer().getId()));
    }

    public void setType(String type) {
        this.type = StringHelper.format(type);
    }

    public void setUserDecision(SampleUserDecisionEnum userDecision) {
        this.userDecision = userDecision;
    }

    public void setUserSampleInMultiplexName(String userSampleInMultiplexName) {
        this.userSampleInMultiplexName = StringHelper.format(userSampleInMultiplexName);
    }

    public void setUserSampleName(String userSampleName) {
        this.userSampleName = StringHelper.format(userSampleName);
    }

    public void setVector(Annotation vector) {
        this.vector = vector;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
        calculateAmountTotal();
    }

    public void setVolumeDilutionSample(BigDecimal volumeDilutionSample) {
        this.volumeDilutionSample = volumeDilutionSample;
        calculateVolumeDilutionWater();
    }

    public void setVolumeDilutionWater(BigDecimal volumeDilutionWater) {
        this.volumeDilutionWater = volumeDilutionWater;
    }

    public void setVolumeEluted(BigDecimal volumeEluted) {
        this.volumeEluted = volumeEluted;
    }

    public void setVolumeInput(BigDecimal volumeInput) {
        this.volumeInput = volumeInput;
        calculateVolumeDilutionWater();
    }

    public void setVolumeLysisBuffer(BigDecimal volumeLysisBuffer) {
        this.volumeLysisBuffer = volumeLysisBuffer;
    }

    public void setVolumeMeasured(BigDecimal volumeMeasured) {
        this.volumeMeasured = volumeMeasured;
    }

    public void setVolumeReaction(BigDecimal volumeReaction) {
        this.volumeReaction = volumeReaction;
    }

    public void setVolumeTarget(BigDecimal volumeTarget) {
        this.volumeTarget = volumeTarget;
        calculateEbtVolumeToAdd();
        calculateSampleVolumeAndEbtVolumeToAdd();
    }

    public void setVolumeToAddEbt(BigDecimal volumeToAddEbt) {
        this.volumeToAddEbt = volumeToAddEbt;
    }

    public void setVolumeToAddSample(BigDecimal volumeToAddSample) {
        this.volumeToAddSample = volumeToAddSample;
        calculateEbtVolumeToAdd();
    }

    public void setWorkflowSteps(Set<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public void setWorkflows(Set<Workflow> workflows) {
        this.workflows = workflows;
    }

    public void setYield(BigDecimal yield) {
        this.yield = yield;
    }

    public void typeChanged(ValueChangeEvent event) {
        setSampleType((SampleType) event.getNewValue());
    }
}