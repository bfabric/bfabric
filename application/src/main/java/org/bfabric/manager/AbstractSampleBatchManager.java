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

package org.bfabric.manager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleStatusEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.forms.MFHelper;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.list.AnnotationList;
import org.bfabric.service.AnnotationService;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.MultiplexKitService;
import org.bfabric.service.SamplePreparationProtocolService;
import org.bfabric.service.SampleService;
import org.bfabric.service.SampleTypeService;
import org.bfabric.util.CustomAttributeColumn;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.SampleAttributeHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DualListModel;

public abstract class AbstractSampleBatchManager<T extends AbstractEntity> extends AbstractBatchManager<T> {

    protected static final int customAttributesValidationErrorMsgKey = -1;

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(AbstractSampleBatchManager.class.getName());

    protected final Map<String, List<?>> cachedSelectionValuesListsHashMap = new HashMap<>();

    final private List<SampleAttributeEnum> sampleAttributeEnumsForModalPanel = new ArrayList<>();

    private final Set<String> sampleAttributeEnumLabelsInvalidInput = new HashSet<>();

    private final LinkedHashMap<Long, Long> containerSpecificNextTubeIdSuffix = new LinkedHashMap<>();

    private final List<CustomAttributeColumn> customAttributeColumns = new ArrayList<>();

    private final List<CustomAttribute> customAttributes = new ArrayList<>();

    private final Set<Sample> initialParentsOfMultiplexIdAssignmentSample = new HashSet<>();

    private final Map<Sample, Map<Plate, Sample>> inputQcSamplePlatesSampleMap = new HashMap<>();

    private final Map<Sample, Map<Plate, Sample>> molaritySamplePlatesSampleMap = new HashMap<>();

    private final Map<String, List<String>> cachedSampleAttributeSelectionValuesMap = new HashMap<>();

    @Inject
    protected SampleService sampleService;

    @Inject
    protected AnnotationService annotationService;

    @Inject
    protected SampleTypeService sampleTypeService;

    private DualListModel<Annotation> allRowsModal;

    private Annotation annotation;

    @Inject
    private AnnotationList annotationList;

    private Map<String, Map<String, Annotation>> annotationTypeAnnotationNamesAnnotationMap = null;

    private Sample editedSample;

    private Set<String> generatedSampleNames;

    private Map<Integer, Integer> initialSampleIdentifiersNumberMap = new HashMap<>();

    private List<Plate> inputQcSamplePlates = new ArrayList<>();

    @Inject
    private InstrumentService instrumentService;

    private List<Plate> molaritySamplePlates = new ArrayList<>();

    private Sample multiplexIdAssignmentSample;

    @Inject
    private MultiplexKitService multiplexKitService;

    private List<SampleAttributeEnum> qcTypeDependentAttributes;

    private DualListModel<Annotation> rowModal;

    private SampleAttributeEnum sampleAttributeEnum = null;

    @Inject
    private SampleAttributeHelper sampleAttributeHelper;

    private List<SampleAttributeEnum> sampleFormDependentAttributes;

    private Map<Integer, Integer> sampleIdentifiersNumberMap = new HashMap<>();

    private String sampleNamePrefix;

    private boolean sampleNamesGenerated;

    @Inject
    private SamplePreparationProtocolService samplePreparationProtocolService;

    private SampleType sampleType;

    private Set<SampleType> sampleTypes = new HashSet<>();

    private List<Sample> samplesInEditList = new ArrayList<>();

    private String type;

    private Set<String> types = new HashSet<>();

    private boolean unsavedChanges;

    public AbstractSampleBatchManager() {
    }

    public AbstractSampleBatchManager(Class<T> entityClass) {
        super(entityClass);
    }

    public void addAttributeColumn() {
        addAttributeColumn(null);
    }

    public CustomAttributeColumn addAttributeColumn(String columnName) {
        CustomAttributeColumn column = new CustomAttributeColumn(columnName != null ? columnName : Constants.EMPTY_STRING, getNextColumnPosition());
        getCustomAttributeColumns().add(column);
        return column;
    }

    public void applyChangesToMultiValueField() {
        if (getEditedSample() != null && getRowModal() != null && getRowModal().getTarget() != null) {
            try {
                PropertyUtils.setProperty(getEditedSample(), getSampleAttributeEnum().getName(), getRowModal().getTarget());
                getEditedSample().setChanged(true);
                updateColumn(getSampleAttributeEnum().getName());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.fine(getEditedSample() + " does not have the property" + getSampleAttributeEnum().getName());
            }
            setRowModal(null);
            setEditedSample(null);
        }
    }

    public void assignMultiplexIds() {
        getMultiplexIdAssignmentSample().recomputeParentSamplesOfUserMultiplex();
        resetAssignMultiplexIdsModalPanel();
        updateTriStateCheckboxGroup(Constants.MULTIPLEXED);
    }

    public void assignSampleValues() {
        Set<Sample> samplesWithValuesAssigned = new HashSet<>();
        getSampleAttributeEnumLabelsInvalidInput().clear();
        for (Sample sample : getSamplesInEditList()) {
            if (!samplesWithValuesAssigned.contains(sample)) {
                if (StringHelper.isNotEmpty(sample.getUserSampleName())) {
                    if (sample.getOldUserSampleName() == null || !sample.getOldUserSampleName().equals(sample.getUserSampleName())) {
                        sample.setName(sample.getUserSampleName());
                    }
                } else {
                    if (StringHelper.isNotEmpty(sample.getOldUserSampleName())) {
                        sample.setName(sample.getUserSampleName());
                    }
                }
                sample.setOldUserSampleName(null);
                sample.setUserSampleName(null);

                sample.setAssignSampleValuesChanged(false);
                for (SampleAttributeEnum sampleAttributeEnum : getSampleAttributeEnumsForModalPanel()) {
                    boolean setProperty = true;
                    String sampleAttributeName = sampleAttributeEnum.getName();
                    String stringValue = sample.getSampleAttributeEnumNameValueMap().get(sampleAttributeName);
                    Object convertedValue = null;

                    if (StringHelper.isNotEmpty(stringValue)) {
                        if (sampleAttributeEnum.isStringType()) {
                            convertedValue = stringValue;
                        } else {
                            try {
                                if (sampleAttributeEnum.isNumericType()) {
                                    stringValue = stringValue.replaceAll(",", ".");
                                    if (NumberUtils.isNumericGreaterOrEqualsZero(stringValue) == null) {
                                        // Every number needs to be >= 0. If this changes, this check needs to be extended.
                                        if (sampleAttributeEnum.isIntegerType()) {
                                            convertedValue = MFHelper.integerValueOf(sampleAttributeName, stringValue);
                                        } else if (sampleAttributeEnum.isLongType()) {
                                            convertedValue = MFHelper.longValueOf(sampleAttributeName, stringValue);
                                        } else if (sampleAttributeEnum.isBigDecimalType()) {
                                            convertedValue = MFHelper.bigDecimalValueOf(sampleAttributeName, stringValue);
                                        } else if (sampleAttributeEnum.isFloatType()) {
                                            convertedValue = MFHelper.floatValueOf(sampleAttributeName, stringValue);
                                        } else if (sampleAttributeEnum.isDoubleType()) {
                                            convertedValue = MFHelper.doubleValueOf(sampleAttributeName, stringValue);
                                        } else {
                                            setProperty = false;
                                            getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                        }
                                    } else {
                                        setProperty = false;
                                        getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                    }
                                } else {
                                    if (sampleAttributeEnum.isLocalDateType()) {
                                        convertedValue = MFHelper.dateValueOf(sampleAttributeName, stringValue);
                                    } else if (sampleAttributeEnum.isLocalDateTimeType()) {
                                        convertedValue = MFHelper.dateTimeValueOf(sampleAttributeName, stringValue);
                                    } else if (sampleAttributeEnum.isBooleanType()) {
                                        convertedValue = MFHelper.booleanValueOf(sampleAttributeName, stringValue);
                                    } else if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                                        if (getAnnotationTypeAnnotationNamesAnnotationMap().containsKey(sampleAttributeEnum.getLabel()) && getAnnotationTypeAnnotationNamesAnnotationMap()
                                            .get(sampleAttributeEnum.getLabel()).containsKey(stringValue.toLowerCase())) {
                                            convertedValue = getAnnotationTypeAnnotationNamesAnnotationMap().get(sampleAttributeEnum.getLabel()).get(stringValue.toLowerCase());
                                        } else {
                                            setProperty = false;
                                            getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                        }
                                    } else if (sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType()) {
                                        if (SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum)) {
                                            List<SamplePreparationProtocol> results = samplePreparationProtocolService.getEnabledSamplePreparationProtocolsIncludingByName(stringValue, sample);
                                            if (results.size() == 1) {
                                                convertedValue = results.get(0);
                                            } else if (results.isEmpty() && sample.getSamplePreparationProtocol() != null) {
                                                convertedValue = sample.getSamplePreparationProtocol();
                                            } else {
                                                setProperty = false;
                                                getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                            }
                                        } else if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum) || SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                                            List<MultiplexKit> results = multiplexKitService.getResultListEnabledByName(stringValue);
                                            if (results.size() == 1) {
                                                convertedValue = results.get(0);
                                            } else if (results.isEmpty()) {
                                                if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum) && sample.getMultiplexKit() != null) {
                                                    convertedValue = sample.getMultiplexKit();
                                                } else if (SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum) && sample.getMultiplexKit2() != null) {
                                                    convertedValue = sample.getMultiplexKit2();
                                                } else {
                                                    setProperty = false;
                                                    getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                                }
                                            } else {
                                                setProperty = false;
                                                getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                            }
                                        } else if (SampleAttributeEnum.INSTRUMENT.equals(sampleAttributeEnum)) {
                                            List<Instrument> results = instrumentService.getResultListEnabledByName(stringValue);
                                            if (results.size() == 1) {
                                                convertedValue = results.get(0);
                                            } else if (results.isEmpty() && sample.getInstrument() != null) {
                                                convertedValue = sample.getInstrument();
                                            } else {
                                                setProperty = false;
                                                getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                            }
                                        } else {
                                            setProperty = false;
                                            getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                        }
                                    } else if (sampleAttributeEnum.isEnumType()) {
                                        try {
                                            if (SampleFormEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                                SampleFormEnum sampleFormEnum = SampleFormEnum.valueByLabel(stringValue, false);
                                                if (sampleFormEnum.isEnabled() || sampleFormEnum.equals(sample.getSampleForm())) {
                                                    convertedValue = sampleFormEnum;
                                                } else {
                                                    setProperty = false;
                                                    getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                                }
                                            } else if (SampleQCTypeEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                                SampleQCTypeEnum sampleQCTypeEnum = SampleQCTypeEnum.valueByLabel(stringValue, false);
                                                if (sampleQCTypeEnum.isEnabled() || sampleQCTypeEnum.equals(sample.getQualityControlType())) {
                                                    convertedValue = sampleQCTypeEnum;
                                                } else {
                                                    setProperty = false;
                                                    getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                                }
                                            } else if (SampleStatusEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                                SampleStatusEnum sampleStatusEnum = SampleStatusEnum.valueByLabel(stringValue, false);
                                                if (sampleStatusEnum.isEnabled() || sampleStatusEnum.equals(sample.getStatus())) {
                                                    convertedValue = sampleStatusEnum;
                                                } else {
                                                    setProperty = false;
                                                    getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                                }
                                            } else {
                                                setProperty = false;
                                                getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                            }
                                        } catch (InvalidEnumValueException e) {
                                            setProperty = false;
                                            getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                                        }
                                    } else {
                                        setProperty = false;
                                    }
                                }
                            } catch (InvalidDataException e) {
                                setProperty = false;
                                getSampleAttributeEnumLabelsInvalidInput().add(sampleAttributeEnum.getLabel());
                            }
                        }
                    }

                    if (setProperty) {
                        try {
                            Object oldValue = PropertyUtils.getProperty(sample, sampleAttributeName);
                            PropertyUtils.setProperty(sample, sampleAttributeName, convertedValue);
                            if (oldValue == null && convertedValue != null || oldValue != null && !oldValue.equals(convertedValue)) {
                                sample.setAssignSampleValuesChanged(true);
                                sample.setChanged(true);
                                setUnsavedChanges(true);
                            }
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                        }
                    }
                }
                sample.getSampleAttributeEnumNameValueMap().clear();
                samplesWithValuesAssigned.add(sample);
                if (sample.isAssignSampleValuesChanged()) {
                    // Set the fields null which are not an attribute of the chosen sample type or subtype.
                    resetSampleFields(sample);
                }
            }
        }
        getSamplesInEditList().clear();
    }

    public void calculateIlluminaLibraryValues(SampleAttributeEnum aSampleAttributeEnum, Sample changedSample, Object value) {
        if (value != null) {
            BigDecimal newValue = (BigDecimal) value;
            // Calculations from the initial qc.
            if (SampleAttributeEnum.CONCENTRATION_INPUT_QC.equals(aSampleAttributeEnum) && newValue.compareTo(BigDecimal.ZERO) > 0) {
                changedSample.setConcentrationInputQc(newValue);
                changedSample.calculateVolumeDilutionSampleAndWater();
            } else if (SampleAttributeEnum.AMOUNT_INPUT.equals(aSampleAttributeEnum)) {
                changedSample.setAmountInput(newValue);
                changedSample.calculateVolumeDilutionSampleAndWater();
            } else if (SampleAttributeEnum.VOLUME_INPUT.equals(aSampleAttributeEnum)) {
                changedSample.setVolumeInput(newValue);
                changedSample.calculateVolumeDilutionWater();
            } else if (SampleAttributeEnum.VOLUME_DILUTION_SAMPLE.equals(aSampleAttributeEnum)) {
                changedSample.setVolumeDilutionSample(newValue);
                changedSample.calculateVolumeDilutionWater();
            }
            // Calculations from the final qc.
            if (SampleAttributeEnum.MOLARITY_TARGET.equals(aSampleAttributeEnum)) {
                changedSample.setMolarityTarget(newValue);
                changedSample.calculateSampleVolumeAndEbtVolumeToAdd();
            } else if (SampleAttributeEnum.VOLUME_TARGET.equals(aSampleAttributeEnum)) {
                changedSample.setVolumeTarget(newValue);
                changedSample.calculateSampleVolumeAndEbtVolumeToAdd();
            } else if (SampleAttributeEnum.MOLARITY.equals(aSampleAttributeEnum) && newValue.compareTo(BigDecimal.ZERO) > 0) {
                changedSample.setMolarity(newValue);
                changedSample.calculateSampleVolumeAndEbtVolumeToAdd();
            } else if (SampleAttributeEnum.VOLUME_TO_ADD_SAMPLE.equals(aSampleAttributeEnum)) {
                changedSample.setVolumeToAddSample(newValue);
                changedSample.calculateEbtVolumeToAdd();
            }
        }
    }

    public void calculateLibraryValues(SampleAttributeEnum aSampleAttributeEnum, Sample changedSample, Object value) {
        if (aSampleAttributeEnum != null && aSampleAttributeEnum.hasSampleAttributeValueChangedListener()) {
            if (changedSample.isIlluminaLibraryCalculationEnabledForInitialQc() || changedSample.isIlluminaLibraryCalculationEnabledForFinalQc()) {
                // Illumina library calculation specific logic.
                calculateIlluminaLibraryValues(aSampleAttributeEnum, changedSample, value);
            } else if (changedSample.isNanoporeLibraryCalculationEnabled() || changedSample.isPacBioLibraryCalculationEnabled()) {
                // PacBio library calculation specific logic.
                calculateValues(aSampleAttributeEnum, changedSample, value);
            }
        }
    }

    public void calculateValues(SampleAttributeEnum aSampleAttributeEnum, Sample changedSample, Object value) {
        if (value != null) {
            BigDecimal newValue = (BigDecimal) value;
            if (SampleAttributeEnum.CONCENTRATION.equals(aSampleAttributeEnum)) {
                changedSample.setConcentration(newValue);
            } else if (SampleAttributeEnum.VOLUME.equals(aSampleAttributeEnum)) {
                changedSample.setVolume(newValue);
            }
            changedSample.calculateAmountTotal();
        }
    }

    public void cancelAssignMultiplexIds() {
        getMultiplexIdAssignmentSample().resetMultiplexIdAssignmentSample(getInitialParentsOfMultiplexIdAssignmentSample());
        getInitialParentsOfMultiplexIdAssignmentSample().clear();
        setSampleIdentifiersNumberMap(new HashMap<>(getInitialSampleIdentifiersNumberMap()));
        getInitialSampleIdentifiersNumberMap().clear();
        resetAssignMultiplexIdsModalPanel();
    }

    public void cancelAssignSampleValues() {
        for (Sample sample : getSamplesInEditList()) {
            sample.setOldUserSampleName(null);
            sample.setUserSampleName(null);
            sample.getSampleAttributeEnumNameValueMap().clear();
        }
        getSamplesInEditList().clear();
        getSampleAttributeEnumLabelsInvalidInput().clear();
    }

    protected void checkSampleAttributesValidity(Sample sample, int index, LinkedHashMap<Integer, LinkedHashMap<String, String>> validationErrorMsg) {
        validationErrorMsg.get(index).putAll(isValidSampleAttributes(sample));

        if (validationErrorMsg.get(index).isEmpty()) {
            // The row contains no errors, so the entry can be removed entirely.
            validationErrorMsg.remove(index);
        }

        LinkedHashMap<String, String> customAttributesValidationErrorMsg = isValidCustomAttributes();
        if (!customAttributesValidationErrorMsg.isEmpty()) {
            validationErrorMsg.put(customAttributesValidationErrorMsgKey, customAttributesValidationErrorMsg);
        }
    }

    public void clearGeneratedSampleNames() {
    }

    public void clearGeneratedSampleNames(List<Sample> sampleList) {
        // Iterate over sample list to clear generated sample names
        for (final Sample sample : sampleList) {
            if (isGeneratedSampleName(sample)) {
                sample.setName(null);
            }
        }
        setSampleNamesGenerated(false);
        setSampleNamePrefix("");
        updateColumn(Constants.SAMPLE_NAME);
    }

    public void columnNameChanged(ValueChangeEvent event) {
        CustomAttributeColumn column = (CustomAttributeColumn) ((UIComponent) event.getSource()).getAttributes().get("column");
        String newColumnName = (String) event.getNewValue();
        String oldColumnName = (String) event.getOldValue();

        if (!StringHelper.isEmpty(newColumnName)) {
            for (CustomAttribute attribute : getCustomAttributes()) {
                if (attribute.getColumn().equals(column)) {
                    attribute.setName(newColumnName);
                    attribute.setChanged(true);
                }
            }

            if (getValidationErrorMsg().containsKey(customAttributesValidationErrorMsgKey)) {
                getValidationErrorMsg().get(customAttributesValidationErrorMsgKey).remove(getBatchTableId() + ":" + column.getId() + Constants.NAME_INPUT);
                Ajax.update(getBatchTableId() + ":" + column.getId() + Constants.NAME_INPUT);
                Ajax.update(getBatchTableId() + ":" + column.getId() + Constants.MESSAGE);
                if (getValidationErrorMsg().get(customAttributesValidationErrorMsgKey).isEmpty()) {
                    getValidationErrorMsg().remove(customAttributesValidationErrorMsgKey);
                }
            }
        } else {
            // An empty name is not allowed.
            if (!StringHelper.isEmpty(oldColumnName)) {
                ((UIInput) event.getSource()).setValue(event.getOldValue());
                getFacesMessagesManager().validationError(getBatchTableId() + ":" + column.getId() + Constants.NAME_INPUT, Constants.REQUIRED);
                Ajax.update(getBatchTableId() + ":" + column.getId() + Constants.NAME_INPUT);
                Ajax.update(getBatchTableId() + ":" + column.getId() + Constants.MESSAGE);
            }
        }
    }

    public void confirmGeneratedSampleNames() {
        setSampleNamesGenerated(false);
        setSampleNamePrefix("");
        updateColumn(Constants.SAMPLE_NAME);
    }

    public boolean correctSampleNameFormat(String sampleName) {
        return sampleName != null && sampleName.matches(Constants.SAMPLE_NAME_CHARACTERS_REGEXP);
    }

    public void createAnnotation(SampleAttributeEnum aSampleAttributeEnum) {
        setAnnotation(new Annotation());
        getAnnotation().setType(aSampleAttributeEnum.getLabel());
    }

    protected void evaluateCustomAttributeColumns() {
        getCustomAttributeColumns().clear();
        CustomAttributeColumn columnToAdd = null;

        if (!getCustomAttributes().isEmpty()) {
            for (CustomAttribute attribute : getCustomAttributes()) {
                for (CustomAttributeColumn column : getCustomAttributeColumns()) {
                    if (attribute.getName() == null || attribute.getName().equals(column.getName())) {
                        columnToAdd = column;
                    }
                }

                if (columnToAdd == null) {
                    columnToAdd = addAttributeColumn(attribute.getName());
                }

                attribute.setColumn(columnToAdd);

                columnToAdd = null;
            }
        } else {
            addAttributeColumn();
        }
    }

    public void generateSampleNames() {
    }

    public void generateSampleNames(Container container, List<Sample> sampleList) {
        if (!isSampleNamesGenerated()) {
            if (getSampleNamePrefix() == null) {
                setSampleNamePrefix("");
            }
            long maxSuffix = 0;
            long generatedNamesCount = 0;

            for (final Sample sample : sampleList) {
                if (StringHelper.isEmpty(sample.getName())) {
                    generatedNamesCount++;
                    break;
                }
            }

            // Check for unique name constraint in the database.
            final List<BigInteger> sampleSuffixes = containerService.getSampleSuffixBySampleNamePrefixAndContainer(getSampleNamePrefix(), container);
            for (final BigInteger sampleSuffix : sampleSuffixes) {
                if (sampleSuffix.longValue() > maxSuffix && sampleSuffix.longValue() + generatedNamesCount < Long.MAX_VALUE) {
                    maxSuffix = sampleSuffix.longValue();
                    break;
                }
            }

            // Check for unique name constraint in editList
            for (final Sample sample : sampleList) {
                if (StringHelper.isNotEmpty(sample.getName()) && sample.getName().startsWith(getSampleNamePrefix()) && Pattern
                    .matches("^\\d+$", sample.getName().substring(getSampleNamePrefix().length()))) {
                    final long suffix = Long.parseLong(sample.getName().substring(getSampleNamePrefix().length()));
                    if (suffix > maxSuffix && suffix + generatedNamesCount < Long.MAX_VALUE) {
                        maxSuffix = suffix;
                    }
                }
            }

            setGeneratedSampleNames(new HashSet<>());
            // Iterate over the sample list for samples whose names are empty.
            for (final Sample sample : sampleList) {
                if (StringHelper.isEmpty(sample.getName())) {
                    sample.setName(getSampleNamePrefix() + (++maxSuffix));
                    getGeneratedSampleNames().add(sample.getName());
                }
            }
            setSampleNamesGenerated(!getGeneratedSampleNames().isEmpty());
        }
    }

    public DualListModel<Annotation> getAllRowsModal() {
        if (allRowsModal == null) {
            allRowsModal = new DualListModel<>(annotationList.getAnnotationsByType(getSampleAttributeEnum().getLabel()), new ArrayList<>());
        }
        return allRowsModal;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Map<String, Map<String, Annotation>> getAnnotationTypeAnnotationNamesAnnotationMap() {
        return annotationTypeAnnotationNamesAnnotationMap;
    }

    public List<Annotation> getAnnotationsByType(SampleAttributeEnum aSampleAttributeEnum) {
        return annotationService.getAnnotationsByType(aSampleAttributeEnum.getLabel());
    }

    public Map<String, List<String>> getCachedSampleAttributeSelectionValuesMap() {
        return cachedSampleAttributeSelectionValuesMap;
    }

    public long getContainerSpecificNextTubeIdSuffix(long containerId) {
        Long suffix = containerSpecificNextTubeIdSuffix.get(containerId);
        if (suffix == null) {
            suffix = entityService.getNextTubeIdSuffix(containerId);
            containerSpecificNextTubeIdSuffix.put(containerId, suffix);
        }
        return suffix;
    }

    public CustomAttribute getCustomAttribute(Sample sample, CustomAttributeColumn column) {
        CustomAttribute ret = null;
        for (CustomAttribute customAttribute : getCustomAttributes()) {
            if (customAttribute.getParent().equals(sample) && customAttribute.getColumn().equals(column)) {
                ret = customAttribute;
                break;
            }
        }
        if (ret == null) {
            ret = new CustomAttribute(sample, column, column.getName(), Constants.STRING, null, false);
            getCustomAttributes().add(ret);
        }
        return ret;
    }

    public List<CustomAttributeColumn> getCustomAttributeColumns() {
        return customAttributeColumns;
    }

    public List<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    private Sample getEditedSample() {
        return editedSample;
    }

    public List<SampleAttributeEnum> getExtensibleAnnotationTypes() {
        return getSampleType() != null ? SampleAttributeEnum.getExtensibleAnnotationTypes(SampleTypeEnum.getSampleTypeEnumByLabel(getSampleType().getName())) : null;
    }

    public Set<String> getGeneratedSampleNames() {
        return generatedSampleNames;
    }

    public Set<Sample> getInitialParentsOfMultiplexIdAssignmentSample() {
        return initialParentsOfMultiplexIdAssignmentSample;
    }

    public Map<Integer, Integer> getInitialSampleIdentifiersNumberMap() {
        return initialSampleIdentifiersNumberMap;
    }

    public List<Plate> getInputQcSamplePlates() {
        return inputQcSamplePlates;
    }

    public Map<Sample, Map<Plate, Sample>> getInputQcSamplePlatesSampleMap() {
        return inputQcSamplePlatesSampleMap;
    }

    public int getLastColumnPosition() {
        return !getCustomAttributeColumns().isEmpty() ? getCustomAttributeColumns().get(getCustomAttributeColumns().size() - 1).getPosition() : 0;
    }

    public List<Plate> getMolaritySamplePlates() {
        return molaritySamplePlates;
    }

    public Map<Sample, Map<Plate, Sample>> getMolaritySamplePlatesSampleMap() {
        return molaritySamplePlatesSampleMap;
    }

    public Sample getMultiplexIdAssignmentSample() {
        return multiplexIdAssignmentSample;
    }

    private CustomAttributeColumn getNextColumn(CustomAttributeColumn column) {
        CustomAttributeColumn nextColumn = null;

        int listPosition = getCustomAttributeColumns().indexOf(column) + 1;
        if (listPosition < getCustomAttributeColumns().size()) {
            nextColumn = getCustomAttributeColumns().get(listPosition);
        }

        return nextColumn;
    }

    private int getNextColumnPosition() {
        return getLastColumnPosition() + 1;
    }

    public Order getOrder() {
        return null;
    }

    public List<SampleAttributeEnum> getQcTypeDependentAttributes() {
        if (qcTypeDependentAttributes == null) {
            qcTypeDependentAttributes = SampleAttributeEnum.getQCTypeDependentAttributes();
        }
        return qcTypeDependentAttributes;
    }

    public DualListModel<Annotation> getRowModal() {
        return rowModal;
    }

    public SampleAttributeEnum getSampleAttributeEnum() {
        return sampleAttributeEnum;
    }

    public Set<String> getSampleAttributeEnumLabelsInvalidInput() {
        return sampleAttributeEnumLabelsInvalidInput;
    }

    public List<SampleAttributeEnum> getSampleAttributeEnumsForModalPanel() {
        return sampleAttributeEnumsForModalPanel;
    }

    public List<SampleAttributeEnum> getSampleFormDependentAttributes() {
        if (sampleFormDependentAttributes == null) {
            sampleFormDependentAttributes = SampleAttributeEnum.getSampleFormDependentAttributes();
        }
        return sampleFormDependentAttributes;
    }

    public Map<Integer, Integer> getSampleIdentifiersNumberMap() {
        return sampleIdentifiersNumberMap;
    }

    public String getSampleNamePrefix() {
        return sampleNamePrefix;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public Set<SampleType> getSampleTypes() {
        return sampleTypes;
    }

    public List<Sample> getSamplesInEditList() {
        return samplesInEditList;
    }

    public List<?> getSelectionValuesBySampleAttributeAndSampleType(SampleAttributeEnum aSampleAttributeEnum, String aType) {
        List<?> emptyList = new ArrayList<>();
        if (aSampleAttributeEnum != null) {
            String key = aSampleAttributeEnum.getLabel();
            if (aSampleAttributeEnum.isAnnotationType()) {
                if (!cachedSelectionValuesListsHashMap.containsKey(key)) {
                    cachedSelectionValuesListsHashMap.put(key, getAnnotationsByType(aSampleAttributeEnum));
                }
                if (!getCachedSampleAttributeSelectionValuesMap().containsKey(aSampleAttributeEnum.getLabel())) {
                    List<String> annotationNames = new ArrayList<>();
                    for (Object object : cachedSelectionValuesListsHashMap.get(key)) {
                        annotationNames.add(((Annotation) object).getName());
                    }
                    getCachedSampleAttributeSelectionValuesMap().put(aSampleAttributeEnum.getLabel(), annotationNames);
                }
                return cachedSelectionValuesListsHashMap.get(key);
            }
            if (aSampleAttributeEnum.isSelectionAndNotAnnotationType()) {
                if (StringHelper.isNotEmpty(aType)) {
                    key += " " + aType;
                }
                if (aSampleAttributeEnum.isEnumType()) {
                    if (!cachedSelectionValuesListsHashMap.containsKey(key)) {
                        cachedSelectionValuesListsHashMap.put(key, aSampleAttributeEnum.getEnumSelectionValues(aType));
                    }
                    if (!getCachedSampleAttributeSelectionValuesMap().containsKey(aSampleAttributeEnum.getLabel())) {
                        List<String> enumLabels = new ArrayList<>();
                        for (Object object : cachedSelectionValuesListsHashMap.get(key)) {
                            if (aSampleAttributeEnum.getClazz().equals(SampleFormEnum.class)) {
                                enumLabels.add(((SampleFormEnum) object).getLabel());
                            } else if (aSampleAttributeEnum.getClazz().equals(SampleQCTypeEnum.class)) {
                                enumLabels.add(((SampleQCTypeEnum) object).getLabel());
                            } else if (aSampleAttributeEnum.getClazz().equals(SampleStatusEnum.class)) {
                                enumLabels.add(((SampleStatusEnum) object).getLabel());
                            }
                        }
                        getCachedSampleAttributeSelectionValuesMap().put(aSampleAttributeEnum.getLabel(), enumLabels);
                    }
                    return cachedSelectionValuesListsHashMap.get(key);
                }
                if (!cachedSelectionValuesListsHashMap.containsKey(key)) {
                    switch (aSampleAttributeEnum) {
                    case SAMPLE_PREPARATION_PROTOCOL:
                        cachedSelectionValuesListsHashMap.put(key, StringHelper.isNotEmpty(aType) ? samplePreparationProtocolService
                            .getEnabledSampleTypeSpecificProtocols(sampleTypeService.getSampleTypeByName(aType)) : samplePreparationProtocolService.getResultListEnabledOrderedByName());
                        if (!getCachedSampleAttributeSelectionValuesMap().containsKey(aSampleAttributeEnum.getLabel())) {
                            List<String> names = new ArrayList<>();
                            for (Object object : cachedSelectionValuesListsHashMap.get(key)) {
                                names.add(((AbstractNamedBaseEntity) object).getName());
                            }
                            getCachedSampleAttributeSelectionValuesMap().put(aSampleAttributeEnum.getLabel(), names);
                        }
                        break;
                    case MULTIPLEX_KIT:
                    case MULTIPLEX_KIT_2:
                        cachedSelectionValuesListsHashMap.put(key, multiplexKitService.getResultListEnabledOrderedByName());
                        if (!getCachedSampleAttributeSelectionValuesMap().containsKey(aSampleAttributeEnum.getLabel())) {
                            List<String> names = new ArrayList<>();
                            for (Object object : cachedSelectionValuesListsHashMap.get(key)) {
                                names.add(((AbstractNamedBaseEntity) object).getName());
                            }
                            getCachedSampleAttributeSelectionValuesMap().put(aSampleAttributeEnum.getLabel(), names);
                        }
                        break;
                    case INSTRUMENT:
                        cachedSelectionValuesListsHashMap.put(key, instrumentService.getResultListEnabledOrderedByName());
                        if (!getCachedSampleAttributeSelectionValuesMap().containsKey(aSampleAttributeEnum.getLabel())) {
                            List<String> names = new ArrayList<>();
                            for (Object object : cachedSelectionValuesListsHashMap.get(key)) {
                                names.add(((AbstractNamedBaseEntity) object).getName());
                            }
                            getCachedSampleAttributeSelectionValuesMap().put(aSampleAttributeEnum.getLabel(), names);
                        }
                        break;
                    default:
                        break;
                    }
                    return cachedSelectionValuesListsHashMap.getOrDefault(key, emptyList);
                }
                return cachedSelectionValuesListsHashMap.get(key);
            }
        }
        return emptyList;
    }

    @CachedMethodResult
    public List<?> getSelectionValuesIncludingBySampleAttributeAndSample(SampleAttributeEnum aSampleAttributeEnum, Sample sample) {
        List<?> emptyList = new ArrayList<>();
        if (aSampleAttributeEnum != null && aSampleAttributeEnum.isSelectionAndNotAnnotationType() && sample != null) {
            String key = aSampleAttributeEnum.getLabel() + sample.getId();
            if (!cachedSelectionValuesListsHashMap.containsKey(key)) {
                if (!aSampleAttributeEnum.isEnumType()) {
                    switch (aSampleAttributeEnum) {
                    case SAMPLE_PREPARATION_PROTOCOL:
                        cachedSelectionValuesListsHashMap.put(key, samplePreparationProtocolService.getEnabledSamplePreparationProtocolsIncluding(sample));
                        break;
                    case MULTIPLEX_KIT:
                        cachedSelectionValuesListsHashMap
                            .put(key, multiplexKitService.getResultListEnabledIncludingOrderByEntityId(sample.getMultiplexKit() != null ? sample.getMultiplexKit().getId() : 0, "name"));
                        break;
                    case MULTIPLEX_KIT_2:
                        cachedSelectionValuesListsHashMap
                            .put(key, multiplexKitService.getResultListEnabledIncludingOrderByEntityId(sample.getMultiplexKit2() != null ? sample.getMultiplexKit2().getId() : 0, "name"));
                        break;
                    case INSTRUMENT:
                        cachedSelectionValuesListsHashMap
                            .put(key, instrumentService.getResultListEnabledIncludingOrderByEntityId(sample.getInstrument() != null ? sample.getInstrument().getId() : 0, "name"));
                        break;
                    default:
                        break;
                    }
                    return cachedSelectionValuesListsHashMap.getOrDefault(key, emptyList);
                }
                cachedSelectionValuesListsHashMap.put(key, aSampleAttributeEnum.getEnumSelectionValuesIncluding(sample));
            }
            return cachedSelectionValuesListsHashMap.get(key);
        }
        return emptyList;
    }

    public String getType() {
        return type;
    }

    @CachedMethodResult
    public Set<String> getTypes() {
        return types;
    }

    public void headerInputChanged(ValueChangeEvent event) {
        UIComponent source = (UIComponent) event.getSource();
        String columnId = source.getId().replaceAll(Constants.HEADER_INPUT, Constants.EMPTY_STRING);
        String value = (String) event.getNewValue();

        if (StringHelper.isNotEmpty(value)) {
            for (CustomAttribute attribute : getCustomAttributes()) {
                if (attribute.getColumn().getId().equals(columnId)) {
                    attribute.setValue(value);
                    attribute.setChanged(true);
                }
            }

            updateColumn(columnId);
        }
    }

    public void incrementContainerSpecificNextTubeIdSuffix(long containerId) {
        Long suffix = containerSpecificNextTubeIdSuffix.get(containerId);
        if (suffix != null) {
            suffix = suffix + 1;
            containerSpecificNextTubeIdSuffix.put(containerId, suffix);
        }
    }

    protected void initializeInitialParentSamplesOfUserMultiplexForAllPooledLibraries(List<Sample> samples) {
        for (Sample sample : samples) {
            sample.initializeInitialParentSamplesOfUserMultiplex();
            if (sample.getMultiplexedByUser() != null && sample.getMultiplexedByUser()) {
                getSampleIdentifiersNumberMap().put(sample.hashCode(), sample.getInitialParentSamplesOfUserMultiplex().size());
            }
        }
    }

    protected void initializeSelectAllValuesForIlluminaLibraryCalculation(List<Sample> samples) {

        final Set<Plate> aInputQcSamplePlates = new HashSet<>();
        final Set<Plate> aMolaritySamplePlates = new HashSet<>();
        final Set<Plate> initialQcCurrentQcPlates = new HashSet<>();
        final Set<Plate> finalQcCurrentQcPlates = new HashSet<>();
        for (Sample sample : samples) {
            // Calculations from the initial qc.
            final Set<Sample> qcSiblingsWithNonEmptyConcentration = sample.getQcSiblingsWithNonEmptyConcentration();
            if (!qcSiblingsWithNonEmptyConcentration.isEmpty()) {
                initialQcCurrentQcPlates.clear();
                for (Sample qcSiblingWithNonEmptyConcentration : qcSiblingsWithNonEmptyConcentration) {
                    aInputQcSamplePlates.addAll(qcSiblingWithNonEmptyConcentration.getQcPlates());
                    initialQcCurrentQcPlates.addAll(qcSiblingWithNonEmptyConcentration.getQcPlates());
                }
                getInputQcSamplePlatesSampleMap().put(sample, new HashMap<>());
                for (Sample qcSiblingWithNonEmptyConcentration : qcSiblingsWithNonEmptyConcentration) {
                    for (Plate qcPlate : initialQcCurrentQcPlates) {
                        if (qcSiblingWithNonEmptyConcentration.getQcPlates().contains(qcPlate) && qcPlate.getSamples().contains(qcSiblingWithNonEmptyConcentration)) {
                            if (!getInputQcSamplePlatesSampleMap().get(sample).containsKey(qcPlate)) {
                                getInputQcSamplePlatesSampleMap().get(sample).put(qcPlate, qcSiblingWithNonEmptyConcentration);
                            } else {
                                // At this point, multiple QC sample siblings with a non-empty concentration exist on the same plate.
                                getInputQcSamplePlatesSampleMap().get(sample).remove(qcPlate);
                                break;
                            }
                        }
                    }
                }
                if (getInputQcSamplePlatesSampleMap().get(sample).isEmpty()) {
                    getInputQcSamplePlatesSampleMap().remove(sample);
                }
            }

            // Calculations from the final qc.
            final Set<Sample> qcChildrenWithNonEmptyMolarConcentrationInRange = sample.getQcChildrenWithNonEmptyMolarConcentrationInRange();
            if (!qcChildrenWithNonEmptyMolarConcentrationInRange.isEmpty()) {
                finalQcCurrentQcPlates.clear();
                for (Sample qcChildWithNonEmptyMolarConcentrationInRange : qcChildrenWithNonEmptyMolarConcentrationInRange) {
                    aMolaritySamplePlates.addAll(qcChildWithNonEmptyMolarConcentrationInRange.getQcPlates());
                    finalQcCurrentQcPlates.addAll(qcChildWithNonEmptyMolarConcentrationInRange.getQcPlates());
                }
                getMolaritySamplePlatesSampleMap().put(sample, new HashMap<>());
                for (Sample qcChildWithNonEmptyMolarConcentrationInRange : qcChildrenWithNonEmptyMolarConcentrationInRange) {
                    for (Plate qcPlate : finalQcCurrentQcPlates) {
                        if (qcChildWithNonEmptyMolarConcentrationInRange.getQcPlates().contains(qcPlate) && qcPlate.getSamples().contains(qcChildWithNonEmptyMolarConcentrationInRange)) {
                            if (!getMolaritySamplePlatesSampleMap().get(sample).containsKey(qcPlate)) {
                                getMolaritySamplePlatesSampleMap().get(sample).put(qcPlate, qcChildWithNonEmptyMolarConcentrationInRange);
                            } else {
                                // At this point, multiple QC sample children with a non-empty molar concentration in range exist on the same plate.
                                getMolaritySamplePlatesSampleMap().get(sample).remove(qcPlate);
                                break;
                            }
                        }
                    }
                }
                if (getMolaritySamplePlatesSampleMap().get(sample).isEmpty()) {
                    getMolaritySamplePlatesSampleMap().remove(sample);
                }
            }
        }

        getInputQcSamplePlates().addAll(aInputQcSamplePlates);
        getInputQcSamplePlates().sort(Comparator.comparing(Plate::getName));
        getMolaritySamplePlates().addAll(aMolaritySamplePlates);
        getMolaritySamplePlates().sort(Comparator.comparing(Plate::getName));
    }

    public void inputQcSampleChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        if (event.getNewValue() != null) {
            inputQcSampleChanged((Sample) event.getNewValue(), (Sample) getEditList().get(rowIndex));
        } else {
            ((Sample) getEditList().get(rowIndex)).setInputQcSample(null);
        }
        updateBatchTable(SampleAttributeEnum.CONCENTRATION_INPUT_QC.getName(), rowIndex);
    }

    protected void inputQcSampleChanged(Sample aInputQcSample, Sample changedSample) {
        if (aInputQcSample != null && changedSample != null) {
            changedSample.setInputQcSample(aInputQcSample);
            if (changedSample.getInputQcSample().getConcentration() != null && changedSample.isIlluminaLibraryCalculationEnabledForInitialQc()) {
                changedSample.setChanged(true);
                calculateIlluminaLibraryValues(SampleAttributeEnum.CONCENTRATION_INPUT_QC, changedSample, changedSample.getInputQcSample().getConcentration());
            }
        }
    }

    public void inputQcSamplePlateChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            final Plate inputQcSamplePlate = (Plate) event.getNewValue();
            for (AbstractEntity abstractEntity : getEditList()) {
                final Sample changedSample = (Sample) abstractEntity;
                if (getInputQcSamplePlatesSampleMap().containsKey(changedSample)) {
                    final Map<Plate, Sample> inputQcSamplePlatesSampleMap = getInputQcSamplePlatesSampleMap().get(changedSample);
                    if (inputQcSamplePlatesSampleMap != null && !inputQcSamplePlatesSampleMap.isEmpty() && inputQcSamplePlatesSampleMap.containsKey(inputQcSamplePlate)) {
                        inputQcSampleChanged(inputQcSamplePlatesSampleMap.get(inputQcSamplePlate), changedSample);
                    }
                }
            }
        }
        updateBatchTable(SampleAttributeEnum.CONCENTRATION_INPUT_QC.getName(), -1);
        dataTableHelper.updateColumn(getBatchTableId(), Constants.INPUT_QC_SAMPLE, false);
    }

    private boolean isGeneratedSampleName(Sample sample) {
        return sampleNamesGenerated && generatedSampleNames != null && StringHelper.isNotEmpty(sample.getName()) && generatedSampleNames.contains(sample.getName());
    }

    public boolean isSampleNamesGenerated() {
        return sampleNamesGenerated;
    }

    public boolean isSampleNamesNotEditable() {
        return isSampleNamesGenerated();
    }

    public boolean isUnsavedChanges() {
        return unsavedChanges;
    }

    protected LinkedHashMap<String, String> isValidCustomAttributes() {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        HashSet<String> attributeNames = new HashSet<>();
        HashSet<CustomAttributeColumn> changedColumns = new HashSet<>();

        for (CustomAttributeColumn column : getCustomAttributeColumns()) {
            if (StringHelper.isNotEmpty(column.getName())) {
                if (column.getOldName() == null) {
                    attributeNames.add(column.getName().toLowerCase());
                } else {
                    changedColumns.add(column);
                }
            } else {
                for (CustomAttribute attribute : getCustomAttributes()) {
                    if (attribute.getColumn().equals(column) && attribute.getValue() != null && !attribute.getValue().isEmpty()) {
                        validationErrorMsg.put(getBatchTableId() + ":" + column.getId() + Constants.NAME_INPUT, Messages.get("nameEmptyException"));
                        break;
                    }
                }
            }
        }

        for (CustomAttributeColumn column : changedColumns) {
            boolean isValid = true;
            if (attributeNames.contains(column.getName().toLowerCase())) {
                validationErrorMsg.put(getBatchTableId() + ":" + column.getId() + Constants.NAME_INPUT, Messages.get("nameNotUniqueException"));
                isValid = false;
            }

            for (Field field : FieldUtils.getAllFields(Sample.class)) {
                if (column.getName().replaceAll("\\s", Constants.EMPTY_STRING).equalsIgnoreCase(field.getName().replaceAll("\\s", Constants.EMPTY_STRING))) {
                    validationErrorMsg.put(getBatchTableId() + ":" + column.getId() + Constants.NAME_INPUT, Messages.get("reservedKeyword"));
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                attributeNames.add(column.getName().toLowerCase());
            }
        }

        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> isValidQcTypeDependentAttribute(Object value, String aType, SampleAttributeEnum aSampleAttributeEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (aSampleAttributeEnum.isAttribute(aType) && aSampleAttributeEnum.isAttributeRequired(aType, null, sampleQCTypeEnum) && aSampleAttributeEnum.isEmptySampleAttribute(value)) {
            validationErrorMsg.put(aSampleAttributeEnum.getName(), Constants.REQUIRED);
        }

        validationErrorMsg.putAll(isValidSampleAttributeNumericType(value, aSampleAttributeEnum));
        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> isValidSampleAttribute(Object value, String aType, SampleFormEnum sampleForm, SampleQCTypeEnum sampleQCTypeEnum, String columnId) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        SampleAttributeEnum aSampleAttributeEnum = SampleAttributeEnum.getAttributeByName(columnId);
        if (aSampleAttributeEnum != null && !aSampleAttributeEnum.isSelectionType()) {
            if (aSampleAttributeEnum.isEmptySampleAttribute(value)) {
                if (aSampleAttributeEnum.isAttributeRequired(aType, sampleForm, sampleQCTypeEnum)) {
                    validationErrorMsg.put(aSampleAttributeEnum.getName(), Constants.REQUIRED);
                }
            } else {
                validationErrorMsg.putAll(isValidSampleAttributeNumericType(value, aSampleAttributeEnum));
            }
        }

        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> isValidSampleAttributeNumericType(Object value, SampleAttributeEnum aSampleAttributeEnum) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

        if (value != null && aSampleAttributeEnum.isNumericType()) {
            // Every number needs to be >= 0. If this changes, this check needs to be extended.
            String errorMessage = NumberUtils.isNumericGreaterOrEqualsZero(value);
            if (errorMessage != null) {
                validationErrorMsg.put(aSampleAttributeEnum.getName(), errorMessage);
            }
        }

        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> isValidSampleAttributes(Sample sample) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        for (SampleAttributeEnum aSampleAttributeEnum : SampleAttributeEnum.getAttributeEnums(true, sample.getType())) {
            if (aSampleAttributeEnum.isAttribute(sample.getType(), sample.getSampleForm(), sample.getQualityControlType())) {
                try {
                    String componentId = aSampleAttributeEnum.getName();
                    Object value = PropertyUtils.getProperty(sample, componentId);

                    if (aSampleAttributeEnum.isAttributeRequired(sample.getType(), sample.getSampleForm(), sample.getQualityControlType()) && aSampleAttributeEnum.isEmptySampleAttribute(value)) {
                        validationErrorMsg.put(componentId, Constants.REQUIRED);
                    }

                    if (StringHelper.isEmpty(sample.getCalculatedAttributeStyle(aSampleAttributeEnum))) {
                        validationErrorMsg.putAll(isValidSampleAttributeNumericType(value, aSampleAttributeEnum));
                    }
                } catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException ignored) {
                }
            }
        }

        return validationErrorMsg;
    }

    public LinkedHashMap<String, String> isValidSampleFormDependentAttribute(Object value, String aType, SampleAttributeEnum aSampleAttributeEnum, SampleFormEnum sampleFormEnum) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (aSampleAttributeEnum.isAttribute(aType) && aSampleAttributeEnum.isAttributeRequired(aType, sampleFormEnum, null) && aSampleAttributeEnum.isEmptySampleAttribute(value)) {
            validationErrorMsg.put(aSampleAttributeEnum.getName(), Constants.REQUIRED);
        }

        validationErrorMsg.putAll(isValidSampleAttributeNumericType(value, aSampleAttributeEnum));
        return validationErrorMsg;
    }

    public void molaritySampleChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        if (event.getNewValue() != null) {
            molaritySampleChanged((Sample) event.getNewValue(), (Sample) getEditList().get(rowIndex));
        } else {
            ((Sample) getEditList().get(rowIndex)).setMolaritySample(null);
        }
        updateBatchTable(SampleAttributeEnum.MOLARITY.getName(), rowIndex);
    }

    public void molaritySampleChanged(Sample aMolaritySample, Sample changedSample) {
        if (aMolaritySample != null && changedSample != null) {
            changedSample.setMolaritySample(aMolaritySample);
            if (changedSample.getMolaritySample().getConcentrationMolarInRange() != null && changedSample.isIlluminaLibraryCalculationEnabledForFinalQc()) {
                changedSample.setChanged(true);
                calculateIlluminaLibraryValues(SampleAttributeEnum.MOLARITY, changedSample, changedSample.getMolaritySample().getConcentrationMolarInRange());
            }
        }
    }

    public void molaritySamplePlateChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            final Plate molaritySamplePlate = (Plate) event.getNewValue();
            for (AbstractEntity abstractEntity : getEditList()) {
                final Sample changedSample = (Sample) abstractEntity;
                if (getMolaritySamplePlatesSampleMap().containsKey(changedSample)) {
                    final Map<Plate, Sample> molaritySamplePlatesSampleMap = getMolaritySamplePlatesSampleMap().get(changedSample);
                    if (molaritySamplePlatesSampleMap != null && !molaritySamplePlatesSampleMap.isEmpty() && molaritySamplePlatesSampleMap
                        .containsKey(molaritySamplePlate)) {
                        molaritySampleChanged(molaritySamplePlatesSampleMap.get(molaritySamplePlate), changedSample);
                    }
                }
            }
        }
        updateBatchTable(SampleAttributeEnum.MOLARITY.getName(), -1);
        dataTableHelper.updateColumn(getBatchTableId(), Constants.MOLARITY_SAMPLE, false);
    }

    public void moveColumn(CustomAttributeColumn column) {
        if (column != null) {
            column.switchPositions(getNextColumn(column));
            getCustomAttributeColumns().sort(CustomAttributeColumn.Comparators.POSITION);
        }
    }

    public void numberOfSamplesInMultiplexChanged(ValueChangeEvent event) {
        if (getMultiplexIdAssignmentSample() != null) {
            getMultiplexIdAssignmentSample().changeNumberOfSamplesInMultiplex(event);
        }
    }

    @Override
    public void pageListener() {
        super.pageListener();
        if (getValidationErrorMsg().containsKey(customAttributesValidationErrorMsgKey)) {
            for (Map.Entry<String, String> aEntry : getValidationErrorMsg().get(customAttributesValidationErrorMsgKey).entrySet()) {
                getFacesMessagesManager().validationError(aEntry.getKey(), aEntry.getValue());
                Ajax.update(aEntry.getKey());
                Ajax.update(aEntry.getKey().replace(Constants.NAME_INPUT, Constants.MESSAGE));
            }
        }
    }

    public void performMultiplexSpecificLogic(String columnId, Object value, SampleAttributeEnum aSampleAttributeEnum, Sample changedSample) {
        if (columnId != null && aSampleAttributeEnum != null && changedSample != null) {
            if (columnId.equals(Constants.QC_PASSED) && value != null && ((Boolean) value)) {
                changedSample.setStatus(null);
            }
            if (columnId.equals(Constants.MULTIPLEXED) && !changedSample.isMultiplexedType()) {
                changedSample.setMultiplexedByUser(value != null && ((Boolean) value) ? true : null);
            }
            // Multiplex ID selection from multiplex kit specific logic.
            if (SampleAttributeEnum.MULTIPLEX_KIT.equals(aSampleAttributeEnum) || SampleAttributeEnum.MULTIPLEX_KIT_2.equals(aSampleAttributeEnum)) {
                resetSelectedMultiplexIdAndSelectionMultiplexIds(aSampleAttributeEnum, changedSample, value);
            }
            // Library calculation specific logic.
            calculateLibraryValues(aSampleAttributeEnum, changedSample, value);
        }
    }

    public void prepareAnnotationModalPanel() {
        setSampleType(getSampleType());
        setAnnotation(new Annotation());
    }

    public void prepareMultiValueModalPanel(OrderItem orderItem, SampleAttributeEnum attributeEnum) {
        prepareMultiValueModalPanel(orderItem.getSample(), attributeEnum);
    }

    public void prepareMultiValueModalPanel(Sample sample, SampleAttributeEnum aSampleAttributeEnum) {
        ArrayList<Annotation> sourceList = new ArrayList<>(annotationList.getAnnotationsByType(aSampleAttributeEnum.getLabel()));
        ArrayList<Annotation> targetList;
        try {
            targetList = (ArrayList<Annotation>) PropertyUtils.getProperty(sample, aSampleAttributeEnum.getName());
        } catch (Exception e) {
            targetList = new ArrayList<>();
        }
        sourceList.removeAll(targetList);
        rowModal = new DualListModel<>(sourceList, targetList);
        setEditedSample(sample);
        setSampleAttributeEnum(aSampleAttributeEnum);
    }

    public void prepareMultiplexIdsModalPanel(Sample sample) {
        resetAssignMultiplexIdsModalPanel();
        sample.recomputeParentSamplesOfUserMultiplex();

        // Keep the initial values in case of cancel.
        for (Sample parentSample : sample.getParentSamplesOfUserMultiplex()) {
            parentSample.setOldMultiplexIdsToCurrent();
            parentSample.setOldNamePrefixToCurrent();
            parentSample.setOldUserSampleInMultiplexNameToCurrent();
        }
        getInitialParentsOfMultiplexIdAssignmentSample().addAll(sample.getParents());
        setInitialSampleIdentifiersNumberMap(new HashMap<>(getSampleIdentifiersNumberMap()));

        if (sample.getParentSamplesOfUserMultiplex().isEmpty()) {
            // Create a parent sample as no parent sample(s) exist yet.
            sample.createTemporaryParentOfUserSampleInMultiplex(sample.createNamePrefixForParentOfUserSampleInMultiplex(1, 1));
            sample.recomputeParentSamplesOfUserMultiplex();
        }
        int sampleHashCode = sample.hashCode();
        if (!getSampleIdentifiersNumberMap().containsKey(sampleHashCode)) {
            // Not already marked as multiplexed and used for the multiplex id assigment.
            getSampleIdentifiersNumberMap().put(sampleHashCode, sample.getParentSamplesOfUserMultiplex().size());
        }
        // Adapt all the names based on the name or the tube id of the multiplexed sample with the correct padding, so they can be ordered by their names.
        int parentSamplesOfUserMultiplexSize = sample.getParentSamplesOfUserMultiplex().size();
        for (int i = 0; i < parentSamplesOfUserMultiplexSize; i++) {
            sample.getParentSamplesOfUserMultiplex().get(i).setName(sample.createNamePrefixForParentOfUserSampleInMultiplex(i + 1, parentSamplesOfUserMultiplexSize));
        }
        setMultiplexIdAssignmentSample(sample);
    }

    public void prepareSampleValuesModalPanel() {
        getSampleAttributeEnumsForModalPanel().clear();
        getSampleAttributeEnumsForModalPanel().addAll((List<SampleAttributeEnum>) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("sampleAttributeEnums"));
        getSampleAttributeEnumsForModalPanel().removeIf(SampleAttributeEnum::isAnnotationTypeMultiValued);
        getSampleAttributeEnumsForModalPanel().removeIf(sampleAttributeEnum -> sampleAttributeHelper.isMultiplexed(sampleAttributeEnum) && !sampleAttributeHelper.isMultiplexedAndRendered(sampleAttributeEnum, getOrder()));
        getSampleAttributeEnumsForModalPanel().removeIf(sampleAttributeEnum -> sampleAttributeHelper.isSizeGenomeEstimated(sampleAttributeEnum) && !sampleAttributeHelper.isSizeGenomeEstimatedAndRendered(sampleAttributeEnum, getOrder(), null));
        getSampleAttributeEnumsForModalPanel().removeIf(sampleAttributeEnum -> SampleAttributeHelper.isMultiplexedType(getSampleType().getName()) && !SampleAttributeEnum.MOLARITY.equals(sampleAttributeEnum) && !identityManager.hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE));
        if (getAnnotationTypeAnnotationNamesAnnotationMap() == null) {
            setAnnotationTypeAnnotationNamesAnnotationMap(new HashMap<>());
            // Cache all the annotations for each annotation type.
            for (SampleAttributeEnum sampleAttributeEnum : getSampleAttributeEnumsForModalPanel()) {
                if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                    String annotationType = sampleAttributeEnum.getLabel();
                    List<Annotation> annotationsByType = annotationService.getAnnotationsByType(annotationType);
                    for (Annotation annotation : annotationsByType) {
                        if (!getAnnotationTypeAnnotationNamesAnnotationMap().containsKey(annotationType)) {
                            getAnnotationTypeAnnotationNamesAnnotationMap().put(annotationType, new HashMap<>());
                        }
                        getAnnotationTypeAnnotationNamesAnnotationMap().get(annotationType).put(annotation.getName().toLowerCase(), annotation);
                    }
                }
            }
        }

        getSamplesInEditList().clear();
        getSampleAttributeEnumLabelsInvalidInput().clear();
        for (AbstractEntity entity : getEditList()) {
            Sample sample = null;
            if (entity instanceof OrderItem) {
                sample = ((OrderItem) entity).getSample();
            } else if (entity instanceof Sample) {
                sample = (Sample) entity;
            }
            if (sample != null) {
                sample.setOldUserSampleName(sample.getName());
                sample.setUserSampleName(sample.getName());
                for (SampleAttributeEnum sampleAttributeEnum : getSampleAttributeEnumsForModalPanel()) {
                    String sampleAttributeName = sampleAttributeEnum.getName();
                    if (!sample.getSampleAttributeEnumNameValueMap().containsKey(sampleAttributeName)) {
                        sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, null);
                    }
                    Object value = null;
                    try {
                        if (PropertyUtils.isReadable(sample, sampleAttributeName)) {
                            value = PropertyUtils.getProperty(sample, sampleAttributeName);
                        }
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                    }
                    if (value != null) {
                        if (sampleAttributeEnum.isStringType() || sampleAttributeEnum.isNumericType() || sampleAttributeEnum.isLocalDateType() || sampleAttributeEnum
                            .isLocalDateTimeType() || sampleAttributeEnum.isBooleanType()) {
                            sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, String.valueOf(value));
                        }
                        if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                            sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, ((Annotation) value).getName());
                        }
                        if (sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType()) {
                            if (SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum)) {
                                sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, ((SamplePreparationProtocol) value).getName());
                            }
                            if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum) || SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                                sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, ((MultiplexKit) value).getName());
                            }
                            if (SampleAttributeEnum.INSTRUMENT.equals(sampleAttributeEnum)) {
                                sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, ((Instrument) value).getName());
                            }
                        }
                        if (sampleAttributeEnum.isEnumType()) {
                            if (SampleFormEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, ((SampleFormEnum) value).getLabel());
                            }
                            if (SampleQCTypeEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, ((SampleQCTypeEnum) value).getLabel());
                            }
                            if (SampleStatusEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                sample.getSampleAttributeEnumNameValueMap().put(sampleAttributeName, ((SampleStatusEnum) value).getLabel());
                            }
                        }
                    }
                }
                getSamplesInEditList().add(sample);
            }
        }
    }

    public void removeAttributeColumn(CustomAttributeColumn column) {
        getCustomAttributeColumns().remove(column);
        if (getCustomAttributeColumns().isEmpty()) {
            addAttributeColumn();
        }
        for (CustomAttribute customAttribute : getCustomAttributes()) {
            if (customAttribute.getColumn().equals(column)) {
                customAttribute.setValue(Constants.EMPTY_STRING);
            }
        }
        resetColumnPositions();
    }

    private void resetAssignMultiplexIdsModalPanel() {
        setMultiplexIdAssignmentSample(null);
    }

    private void resetColumnPositions() {
        int position = 1;
        for (CustomAttributeColumn column : getCustomAttributeColumns()) {
            column.setPosition(position);
            position++;
        }
    }

    public void resetSampleFields(Sample sample) {
        if (sample != null) {
            // Make sure the oldSampleAttributeEnums are initialized.
            sample.setOldSampleAttributeEnums();
            sample.resetFields();
        }
    }

    public void resetSelectedMultiplexIdAndSelectionMultiplexIds(SampleAttributeEnum aSampleAttributeEnum, Sample changedSample, Object value) {
        if (SampleAttributeEnum.MULTIPLEX_KIT.equals(aSampleAttributeEnum)) {
            changedSample.resetSelectedMultiplexIdAndSelectionMultiplexIds();
            if (value != null) {
                changedSample.setMultiplexKit((MultiplexKit) value);
            }
        } else if (SampleAttributeEnum.MULTIPLEX_KIT_2.equals(aSampleAttributeEnum)) {
            changedSample.resetSelectedMultiplexId2AndSelectionMultiplexId2s();
            if (value != null) {
                changedSample.setMultiplexKit2((MultiplexKit) value);
            }
        }
    }

    public void saveAnnotation() {
        if (!annotationService.checkUniqueName(getAnnotation())) {
            getFacesMessagesManager().validationError("annotationName", Messages.get("nameNotUniqueForTypeException").replace("{0}", getAnnotation().getType()));
            FacesContext.getCurrentInstance().validationFailed();
        } else {
            entityService.persist(getAnnotation());
            cachedSelectionValuesListsHashMap.remove(getAnnotation().getType());
            setAnnotation(null);
        }
    }

    public void selectedMultiplexIdChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        final String columnId = clientId.split(":")[3].replaceAll(Constants.INPUT, Constants.EMPTY_STRING).replaceAll(Constants.SELECTED, Constants.EMPTY_STRING);
        ((Sample) getEditList().get(rowIndex)).selectedMultiplexIdChangedHelper(event, columnId);
        updateBatchTable(columnId, rowIndex);
    }

    public void setAllRowsModal(DualListModel<Annotation> allRowsModal) {
        this.allRowsModal = allRowsModal;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setAnnotationTypeAnnotationNamesAnnotationMap(Map<String, Map<String, Annotation>> annotationTypeAnnotationNamesAnnotationMap) {
        this.annotationTypeAnnotationNamesAnnotationMap = annotationTypeAnnotationNamesAnnotationMap;
    }

    public void setBooleanAttribute(Boolean value, String columnId) {
        setValueAll(SampleAttributeEnum.getAttributeByName(columnId), value, columnId);
        if (columnId.equals(Constants.QC_PASSED) && value != null && value) {
            setValueAll(SampleAttributeEnum.getAttributeByName(Constants.STATUS), null, Constants.STATUS);
        }
        if (columnId.equals(Constants.MULTIPLEXED)) {
            updateTriStateCheckboxGroup(columnId);
        }
        updateBatchTable(columnId, -1);
    }

    private void setEditedSample(Sample editedSample) {
        this.editedSample = editedSample;
    }

    public void setGeneratedSampleNames(Set<String> generatedSampleNames) {
        this.generatedSampleNames = generatedSampleNames;
    }

    public void setInitialSampleIdentifiersNumberMap(Map<Integer, Integer> initialSampleIdentifiersNumberMap) {
        this.initialSampleIdentifiersNumberMap = initialSampleIdentifiersNumberMap;
    }

    public void setInputQcSamplePlates(List<Plate> inputQcSamplePlates) {
        this.inputQcSamplePlates = inputQcSamplePlates;
    }

    public void setMolaritySamplePlates(List<Plate> molaritySamplePlates) {
        this.molaritySamplePlates = molaritySamplePlates;
    }

    public void setMultiplexIdAssignmentSample(Sample multiplexIdAssignmentSample) {
        this.multiplexIdAssignmentSample = multiplexIdAssignmentSample;
    }

    public void setRowModal(DualListModel<Annotation> rowModal) {
        this.rowModal = rowModal;
    }

    public void setSampleAttributeEnum(SampleAttributeEnum sampleAttributeEnum) {
        this.sampleAttributeEnum = sampleAttributeEnum;
    }

    public void setSampleIdentifiersNumberMap(Map<Integer, Integer> sampleIdentifiersNumberMap) {
        this.sampleIdentifiersNumberMap = sampleIdentifiersNumberMap;
    }

    public void setSampleNamePrefix(String sampleNamePrefix) {
        this.sampleNamePrefix = sampleNamePrefix;
    }

    public void setSampleNamesGenerated(boolean sampleNamesGenerated) {
        this.sampleNamesGenerated = sampleNamesGenerated;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public void setSampleTypes(Set<SampleType> sampleTypes) {
        this.sampleTypes = sampleTypes;
    }

    public void setSamplesInEditList(List<Sample> samplesInEditList) {
        this.samplesInEditList = samplesInEditList;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        this.unsavedChanges = unsavedChanges;
    }

    public void setValueAll(SampleAttributeEnum aSampleAttributeEnum, Object value, String columnId) {
        for (AbstractEntity entity : getEditList()) {
            try {
                if (aSampleAttributeEnum != null && entity instanceof OrderItem) {
                    Object oldValue = PropertyUtils.getProperty(((OrderItem) entity).getSample(), columnId);
                    PropertyUtils.setProperty(((OrderItem) entity).getSample(), columnId, value);
                    if (oldValue == null || !oldValue.equals(value)) {
                        ((OrderItem) entity).getSample().setChanged(true);
                    }
                } else {
                    // Do not set the value for the sample attribute if it is the tube id, the entity is a sample, and its tube id is not editable.
                    if (!(SampleAttributeEnum.TUBE_ID.equals(aSampleAttributeEnum) && entity instanceof Sample && !((Sample) entity).isTubeIdEditable())) {
                        boolean setProperty = true;
                        if (entity instanceof Sample && aSampleAttributeEnum != null) {
                            Sample aSample = (Sample) entity;
                            setProperty = getTypes() == null || getTypes().size() == 1 || (getTypes().size() > 1 && SampleAttributeEnum.TUBE_ID.equals(aSampleAttributeEnum) ? SampleAttributeHelper
                                .isTubeIdEditable(aSample) : SampleAttributeHelper
                                .isCellInputEnabled(aSampleAttributeEnum, aSample.getMultiplexed(), aSample.getSampleType().getName(), aSample.getSampleForm(), aSample.getQualityControlType()));
                        }

                        if (setProperty) {
                            Object oldValue = PropertyUtils.getProperty(entity, columnId);
                            PropertyUtils.setProperty(entity, columnId, value);
                            if (oldValue == null && value != null || oldValue != null && !oldValue.equals(value)) {
                                entity.setChanged(true);
                                // Multiplex ID selection from multiplex kit specific logic.
                                if (entity instanceof Sample && (SampleAttributeEnum.MULTIPLEX_KIT.equals(aSampleAttributeEnum) || SampleAttributeEnum.MULTIPLEX_KIT_2.equals(aSampleAttributeEnum))) {
                                    resetSelectedMultiplexIdAndSelectionMultiplexIds(aSampleAttributeEnum, (Sample) entity, value);
                                }
                                // Library calculation specific logic.
                                if (entity instanceof Sample) {
                                    calculateLibraryValues(aSampleAttributeEnum, (Sample) entity, value);
                                }
                            }
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        }
    }

    @Override
    public void updateBatchTable(String columnId, int rowIndex) {
        if (columnId != null) {
            super.updateBatchTable(columnId, rowIndex);
            updateSubTypeDependentBatchTable(columnId, rowIndex);
            updateCalculationDependentBatchTable(columnId, rowIndex);
            updateMultiplexKitDependentBatchTable(columnId, rowIndex);
        }
    }

    public void updateCalculationDependentBatchTable(String columnId, int rowIndex) {
        if (columnId != null) {
            // Calculations from the initial qc.
            if (columnId.equals(SampleAttributeEnum.CONCENTRATION_INPUT_QC.getName()) || columnId.equals(SampleAttributeEnum.AMOUNT_INPUT.getName())) {
                if (rowIndex > -1) {
                    updateCell(SampleAttributeEnum.VOLUME_DILUTION_SAMPLE.getName(), rowIndex);
                    updateCell(SampleAttributeEnum.VOLUME_DILUTION_WATER.getName(), rowIndex);
                } else {
                    updateColumn(SampleAttributeEnum.VOLUME_DILUTION_SAMPLE.getName());
                    updateColumn(SampleAttributeEnum.VOLUME_DILUTION_WATER.getName());
                }
            } else if (columnId.equals(SampleAttributeEnum.VOLUME_INPUT.getName()) || columnId.equals(SampleAttributeEnum.VOLUME_DILUTION_SAMPLE.getName())) {
                if (rowIndex > -1) {
                    updateCell(SampleAttributeEnum.VOLUME_DILUTION_WATER.getName(), rowIndex);
                } else {
                    updateColumn(SampleAttributeEnum.VOLUME_DILUTION_WATER.getName());
                }
            }
            // Calculations from the final qc.
            if (columnId.equals(SampleAttributeEnum.MOLARITY_TARGET.getName()) || columnId.equals(SampleAttributeEnum.VOLUME_TARGET.getName()) || columnId
                .equals(SampleAttributeEnum.MOLARITY.getName())) {
                if (rowIndex > -1) {
                    updateCell(SampleAttributeEnum.VOLUME_TO_ADD_SAMPLE.getName(), rowIndex);
                    updateCell(SampleAttributeEnum.VOLUME_TO_ADD_EBT.getName(), rowIndex);
                } else {
                    updateColumn(SampleAttributeEnum.VOLUME_TO_ADD_SAMPLE.getName());
                    updateColumn(SampleAttributeEnum.VOLUME_TO_ADD_EBT.getName());
                }
            } else if (columnId.equals(SampleAttributeEnum.VOLUME_TO_ADD_SAMPLE.getName())) {
                if (rowIndex > -1) {
                    updateCell(SampleAttributeEnum.VOLUME_TO_ADD_EBT.getName(), rowIndex);
                } else {
                    updateColumn(SampleAttributeEnum.VOLUME_TO_ADD_EBT.getName());
                }
            }
            // Calculations for Library Amount
            // TODO: add check whether the attribute should be calculated!
            if (columnId.equals(SampleAttributeEnum.CONCENTRATION.getName()) || columnId.equals(SampleAttributeEnum.VOLUME.getName())) {
                if (rowIndex > -1) {
                    updateCell(SampleAttributeEnum.AMOUNT_TOTAL.getName(), rowIndex);
                } else {
                    updateColumn(SampleAttributeEnum.AMOUNT_TOTAL.getName());
                }
            }
        }
    }

    public void updateMultiplexKitDependentBatchTable(String columnId, int rowIndex) {
        if (columnId != null) {
            if (columnId.equals(SampleAttributeEnum.MULTIPLEX_KIT.getName())) {
                if (rowIndex > -1) {
                    dataTableHelper.updateCell(getBatchTableId(), SampleAttributeEnum.MULTIPLEX_ID.getName() + Constants.SELECTED + Constants.GROUP, rowIndex, false);
                } else {
                    dataTableHelper.updateColumn(getBatchTableId(), SampleAttributeEnum.MULTIPLEX_ID.getName() + Constants.SELECTED + Constants.GROUP, false);
                }
            } else if (columnId.equals(SampleAttributeEnum.MULTIPLEX_KIT_2.getName())) {
                if (rowIndex > -1) {
                    dataTableHelper.updateCell(getBatchTableId(), SampleAttributeEnum.MULTIPLEX_ID_2.getName() + Constants.SELECTED + Constants.GROUP, rowIndex, false);
                } else {
                    dataTableHelper.updateColumn(getBatchTableId(), SampleAttributeEnum.MULTIPLEX_ID_2.getName() + Constants.SELECTED + Constants.GROUP, false);
                }
            }
        }
    }

    public void updateSubTypeDependentBatchTable(String columnId, int rowIndex) {
        if (columnId != null) {
            if (columnId.equals(SampleAttributeEnum.SAMPLE_FORM.getName())) {
                if (rowIndex > -1) {
                    for (SampleAttributeEnum aSampleAttributeEnum : getSampleFormDependentAttributes()) {
                        updateCell(aSampleAttributeEnum.getName(), rowIndex);
                    }
                } else {
                    for (SampleAttributeEnum aSampleAttributeEnum : getSampleFormDependentAttributes()) {
                        updateColumn(aSampleAttributeEnum.getName());
                    }
                }
            } else if (columnId.equals(Constants.QUALITY_CONTROL_TYPE)) {
                if (rowIndex > -1) {
                    for (SampleAttributeEnum aSampleAttributeEnum : getQcTypeDependentAttributes()) {
                        updateCell(aSampleAttributeEnum.getName(), rowIndex);
                    }
                } else {
                    for (SampleAttributeEnum aSampleAttributeEnum : getQcTypeDependentAttributes()) {
                        updateColumn(aSampleAttributeEnum.getName());
                    }
                }
            } else if (columnId.equals(Constants.QC_PASSED)) {
                if (rowIndex > -1) {
                    updateCell(Constants.STATUS, rowIndex);
                } else {
                    updateColumn(Constants.STATUS);
                }
            } else if (columnId.equals(Constants.MULTIPLEXED)) {
                if (rowIndex > -1) {
                    updateCell(Constants.MULTIPLEX_ID, rowIndex);
                    updateCell(Constants.MULTIPLEX_ID_2, rowIndex);
                } else {
                    updateColumn(Constants.MULTIPLEX_ID);
                    updateColumn(Constants.MULTIPLEX_ID_2);
                }
            }
        }
    }

    private void updateTriStateCheckboxGroup(String columnId) {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(getBatchTableId());

        int lo = dataTable.getPage() * dataTable.getRowsToRender();
        int hi = lo + dataTable.getRowsToRender();

        for (int i = lo; i < hi; i++) {
            Ajax.update(getBatchTableId() + ":" + i + ":" + columnId + Constants.TRI_STATE_CHECKBOX_GROUP);
        }
    }

    @Override
    public void valueChanged(ValueChangeEvent event) {
        final UIInput source = (UIInput) event.getSource();
        final Object value = event.getNewValue();
        final String clientId = source.getClientId();
        final int rowIndex = Integer.parseInt(clientId.split(":")[2]);
        final String columnId = clientId.split(":")[3].replaceAll(Constants.INPUT, Constants.EMPTY_STRING);
        final Sample changedSample = (Sample) getEditList().get(rowIndex);

        getValueChangedValidationErrorMsg().clear();
        SampleAttributeEnum aSampleAttributeEnum = SampleAttributeEnum.getAttributeByName(columnId);
        if (aSampleAttributeEnum != null && !aSampleAttributeEnum.isBooleanType()) {
            getValueChangedValidationErrorMsg().putAll(isValidSampleAttribute(value, changedSample.getType(), changedSample.getSampleForm(), changedSample.getQualityControlType(), columnId));
        }
        if (getValueChangedValidationErrorMsg().isEmpty()) {
            source.setValue(value);
            changedSample.setChanged(true);
            performMultiplexSpecificLogic(columnId, value, aSampleAttributeEnum, changedSample);
            if (getValidationErrorMsg().containsKey(rowIndex)) {
                getValidationErrorMsg().get(rowIndex).remove(columnId);
            }
        } else {
            source.setValue(event.getOldValue());
            if (!getValidationErrorMsg().containsKey(rowIndex)) {
                getValidationErrorMsg().put(rowIndex, new LinkedHashMap<>());
            }
            getValidationErrorMsg().get(rowIndex).putAll(getValueChangedValidationErrorMsg());
            handleValidationErrorsForRow(getValueChangedValidationErrorMsg(), rowIndex);
        }
        getValueChangedValidationErrorMsg().clear();
        updateBatchTable(columnId, rowIndex);
    }

    @Override
    public void valueChangedAll(ValueChangeEvent event) {
        UIComponent source = (UIComponent) event.getSource();
        String columnId = source.getId().replaceAll(Constants.HEADER_INPUT, Constants.EMPTY_STRING);
        Object value = event.getNewValue();
        boolean setProperty = !(value instanceof String) || !StringHelper.isEmpty((String) value);

        SampleAttributeEnum aSampleAttributeEnum = SampleAttributeEnum.getAttributeByName(columnId);
        if (aSampleAttributeEnum != null && aSampleAttributeEnum.isNumericType()) {
            // Every number needs to be >= 0. If this changes, this check needs to be extended.
            try {
                if (aSampleAttributeEnum.isIntegerType()) {
                    if (Integer.parseInt(value.toString()) >= 0) {
                        value = Integer.parseInt(value.toString());
                    } else {
                        setProperty = false;
                    }
                } else if (aSampleAttributeEnum.isLongType()) {
                    if (Long.parseLong(value.toString()) >= 0) {
                        value = Long.parseLong(value.toString());
                    } else {
                        setProperty = false;
                    }
                } else if (aSampleAttributeEnum.isBigDecimalType()) {
                    if (!(new BigDecimal(value.toString()).doubleValue() < 0)) {
                        value = new BigDecimal(value.toString());
                    } else {
                        setProperty = false;
                    }
                } else if (aSampleAttributeEnum.isFloatType()) {
                    if (!(Float.parseFloat(value.toString()) < 0)) {
                        value = Float.parseFloat(value.toString());
                    } else {
                        setProperty = false;
                    }
                } else if (aSampleAttributeEnum.isDoubleType()) {
                    if (!(Double.parseDouble(value.toString()) < 0)) {
                        value = Double.parseDouble(value.toString());
                    } else {
                        setProperty = false;
                    }
                }
            } catch (Exception e) {
                setProperty = false;
            }
        }
        if (setProperty) {
            setValueAll(aSampleAttributeEnum, value, columnId);
            updateBatchTable(columnId, -1);
        }
    }
}
