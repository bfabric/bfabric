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

package org.bfabric.forms;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleStatusEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.service.AnnotationService;
import org.bfabric.service.EntityService;
import org.bfabric.service.SampleTypeService;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveSample;

public class MFSample extends AbstractMF {

    private final Sample sample;

    private final XMLRequestParameterSaveSample xmlRequestSaveSample;

    public MFSample(Sample sample, XMLRequestParameterSaveSample xmlSampleRequestSave) {
        this.sample = sample;
        this.xmlRequestSaveSample = xmlSampleRequestSave;
    }

    private static List<Annotation> createAndSaveAnnotationIfNotExists(String value, SampleAttributeEnum sampleAttributeEnum) {
        List<Annotation> annotations = CDI.current().select(AnnotationService.class).get().getAnnotationsByNameAndType(value, sampleAttributeEnum.getLabel());
        if (annotations.isEmpty()) {
            Annotation annotation = new Annotation();
            annotation.setName(value);
            annotation.setType(sampleAttributeEnum.getLabel());
            CDI.current().select(EntityService.class).get().save(annotation);
            annotations.add(annotation);
        }
        return annotations;
    }

    @Override
    public synchronized void apply() throws Exception {
        if (getSample().getId() == 0 || getXmlRequestSaveSample().getType() != null) {
            MFHelper.checkNotNull("type", getXmlRequestSaveSample().getType());
            MFHelper.checkNotSampleTypeUserSampleInMultiplex(getXmlRequestSaveSample().getType());
        }
        if (getXmlRequestSaveSample().getMultiplexed() != null && MFHelper.booleanValueOf("multiplexed", getXmlRequestSaveSample().getMultiplexed())) {
            throw new InvalidDataException("Creation/Update of a multiplexed sample is not supported via Web services API.");
        }
        getSample().setName(getName());
        getSample().setSampleType(getSampleType());
        getSample().setContainer(getContainer());
        getSample().setDescription(getDescription());
        getSample().setParents(getParents());
        // Make sure the oldSampleAttributeEnums are initialized.
        getSample().setOldSampleAttributeEnums();

        // Sample attributes.
        for (SampleAttributeEnum attributeEnum : SampleAttributeEnum.values()) {
            PropertyUtils.setProperty(getSample(), attributeEnum.getName(), getProperty(attributeEnum));
        }

        getSample().setCustomAttributes(getXmlRequestSaveSample().getCustomattribute());

        getSample().resetFields();

        // Calculate the Illumina library values.
        if (getSample().isIlluminaLibraryCalculationEnabledForInitialQc()) {
            getSample().calculateVolumeDilutionSampleAndWater();
        }
        if (getSample().isIlluminaLibraryCalculationEnabledForFinalQc()) {
            getSample().calculateSampleVolumeAndEbtVolumeToAdd();
        }
        if (getSample().isNanoporeLibraryCalculationEnabled() || getSample().isPacBioLibraryCalculationEnabled()) {
            getSample().calculateAmountTotal();
        }
    }

    public Container getContainer() throws InvalidDataException {
        if (getXmlRequestSaveSample().getContainerid() != null) {
            return (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveSample().getContainerid()));
        }
        return getSample().getContainer();
    }

    public String getDescription() {
        if (getXmlRequestSaveSample().getDescription() != null) {
            return getXmlRequestSaveSample().getDescription();
        }
        return getSample().getDescription();
    }

    public Instrument getInstrument() throws InvalidDataException {
        if (getXmlRequestSaveSample().getInstrumentid() != null) {
            return (Instrument) fetch(Instrument.class, MFHelper.positiveLongValueOf("instrumentid", getXmlRequestSaveSample().getInstrumentid()));
        }
        return getSample().getInstrument();
    }

    public MultiplexKit getMultiplexKit() throws InvalidDataException {
        if (getXmlRequestSaveSample().getMultiplexkitid() != null) {
            return (MultiplexKit) fetch(MultiplexKit.class, MFHelper.positiveLongValueOf("multiplexkitid", getXmlRequestSaveSample().getMultiplexkitid()));
        }
        return getSample().getMultiplexKit();
    }

    public MultiplexKit getMultiplexKit2() throws InvalidDataException {
        if (getXmlRequestSaveSample().getMultiplexkit2id() != null) {
            return (MultiplexKit) fetch(MultiplexKit.class, MFHelper.positiveLongValueOf("multiplexkit2id", getXmlRequestSaveSample().getMultiplexkit2id()));
        }
        return getSample().getMultiplexKit2();
    }

    public String getName() {
        if (getXmlRequestSaveSample().getName() != null) {
            return getXmlRequestSaveSample().getName();
        }
        return getSample().getName();
    }

    public Set<Sample> getParents() throws InvalidDataException {
        if (getXmlRequestSaveSample().getParentid() != null) {
            Set<Sample> parents = new HashSet<>();
            for (String parentId : getXmlRequestSaveSample().getParentid()) {
                if (!parentId.isEmpty()) {
                    Sample parent = (Sample) fetch(Sample.class, MFHelper.positiveLongValueOf("parentid", parentId));
                    parents.add(parent);
                }
            }
            return parents;
        }
        return getSample().getParents();
    }

    public Object getProperty(SampleAttributeEnum sampleAttributeEnum) throws InvalidDataException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InvalidEnumValueException {
        List<Annotation> annotations = new ArrayList<>();
        String attributeNameLowerCase = sampleAttributeEnum.getName().toLowerCase();
        if (sampleAttributeEnum.getMultiValued()) {
            List<String> valuesAsString = (List<String>) PropertyUtils.getProperty(getXmlRequestSaveSample(), attributeNameLowerCase);
            if (valuesAsString != null && !valuesAsString.isEmpty()) {
                if (!sampleAttributeEnum.isAttribute(getSample().getType())) {
                    MFHelper.throwValueNotSupportedError(attributeNameLowerCase, getSample().getType());
                }
                for (String value : valuesAsString) {
                    if (StringHelper.isNotEmpty(value)) {
                        annotations = createAndSaveAnnotationIfNotExists(value, sampleAttributeEnum);
                    }
                }
            }
            return annotations;
        }
        String value = (String) PropertyUtils.getProperty(getXmlRequestSaveSample(), attributeNameLowerCase);
        if (StringHelper.isNotEmpty(value)) {
            if (!sampleAttributeEnum.isAttribute(getSample().getType())) {
                MFHelper.throwValueNotSupportedError(attributeNameLowerCase, getSample().getType());
            }
            if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                annotations = createAndSaveAnnotationIfNotExists(value, sampleAttributeEnum);
                return annotations.get(0);
            }
            if (sampleAttributeEnum.isStringType()) {
                return value;
            }
            if (sampleAttributeEnum.isIntegerType()) {
                return MFHelper.integerValueOf(attributeNameLowerCase, value);
            }
            if (sampleAttributeEnum.isBigDecimalType()) {
                return MFHelper.bigDecimalValueOf(attributeNameLowerCase, value);
            }
            if (sampleAttributeEnum.isFloatType()) {
                return MFHelper.floatValueOf(attributeNameLowerCase, value);
            }
            if (sampleAttributeEnum.isDoubleType()) {
                return MFHelper.doubleValueOf(attributeNameLowerCase, value);
            }
            if (sampleAttributeEnum.isLocalDateType()) {
                return MFHelper.dateValueOf(attributeNameLowerCase, value);
            }
            if (sampleAttributeEnum.isLocalDateTimeType()) {
                return MFHelper.dateTimeValueOf(attributeNameLowerCase, value);
            }
            if (sampleAttributeEnum.isBooleanType()) {
                return MFHelper.booleanValueOf(attributeNameLowerCase, value);
            }
        }
        if (sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType()) {
            if (SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum)) {
                return getSamplePreparationProtocol();
            }
            if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum)) {
                return getMultiplexKit();
            }
            if (SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                return getMultiplexKit2();
            }
            if (SampleAttributeEnum.INSTRUMENT.equals(sampleAttributeEnum)) {
                return getInstrument();
            }
        } else if (sampleAttributeEnum.isEnumType()) {
            if (SampleFormEnum.class.equals(sampleAttributeEnum.getClazz())) {
                return getXmlRequestSaveSample().getSampleform() != null ? SampleFormEnum.valueByLabel(getXmlRequestSaveSample().getSampleform()) : getSample()
                    .getSampleForm();
            }
            if (SampleQCTypeEnum.class.equals(sampleAttributeEnum.getClazz())) {
                return getXmlRequestSaveSample().getQualitycontroltype() != null ? SampleQCTypeEnum.valueByLabel(getXmlRequestSaveSample().getQualitycontroltype()) : getSample()
                    .getQualityControlType();
            }
            if (SampleStatusEnum.class.equals(sampleAttributeEnum.getClazz())) {
                return getXmlRequestSaveSample().getStatus() != null ? SampleStatusEnum.valueByLabel(getXmlRequestSaveSample().getStatus()) : getSample().getStatus();
            }
        }
        return PropertyUtils.getProperty(getSample(), sampleAttributeEnum.getName());
    }

    public Sample getSample() {
        return sample;
    }

    public SamplePreparationProtocol getSamplePreparationProtocol() throws InvalidDataException {
        if (getXmlRequestSaveSample().getSamplepreparationprotocolid() != null) {
            return (SamplePreparationProtocol) fetch(SamplePreparationProtocol.class, MFHelper
                .positiveLongValueOf("samplepreparationprotocolid", getXmlRequestSaveSample().getSamplepreparationprotocolid()));
        }
        return getSample().getSamplePreparationProtocol();
    }

    public SampleType getSampleType() throws InvalidDataException, InvalidEnumValueException {
        if (getSample().getId() == 0 || getXmlRequestSaveSample().getType() != null) {
            MFHelper.checkNotNull("type", getXmlRequestSaveSample().getType());
            return CDI.current().select(SampleTypeService.class).get().getSampleTypeByName(SampleTypeEnum.valueByLabel(getXmlRequestSaveSample().getType()).getLabel());
        }
        return getSample().getSampleType();
    }

    public XMLRequestParameterSaveSample getXmlRequestSaveSample() {
        return xmlRequestSaveSample;
    }
}
