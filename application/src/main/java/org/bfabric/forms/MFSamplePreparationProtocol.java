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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SampleType;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.entity.Technology;
import org.bfabric.enums.SamplePreparationProtocolDiscriminator;
import org.bfabric.enums.SamplePreparationProtocolType;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.AnnotationService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveSamplePreparationProtocol;

public class MFSamplePreparationProtocol extends AbstractMF {

    private final SamplePreparationProtocol samplePreparationProtocol;

    private final XMLRequestParameterSaveSamplePreparationProtocol xmlRequestSaveSamplePreparationProtocol;

    public MFSamplePreparationProtocol(SamplePreparationProtocol samplePreparationProtocol, XMLRequestParameterSaveSamplePreparationProtocol xmlSamplePreparationProtocolRequestSave) {
        this.samplePreparationProtocol = samplePreparationProtocol;
        this.xmlRequestSaveSamplePreparationProtocol = xmlSamplePreparationProtocolRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getSamplePreparationProtocol().setName(getName());
        getSamplePreparationProtocol().setDiscriminator(SamplePreparationProtocolDiscriminator.value(getDiscriminator()));
        getSamplePreparationProtocol().setType(SamplePreparationProtocolType.valueOf(getType()));
        getSamplePreparationProtocol().setTechnologies(getTechnologies());
        getSamplePreparationProtocol().setStrandedness(getStrandedness());
        getSamplePreparationProtocol().setAdapter1(getAdapter1());
        getSamplePreparationProtocol().setAdapter2(getAdapter2());
        getSamplePreparationProtocol().setPredecessor(getPredecessor());
        getSamplePreparationProtocol().setDescription(getDescription());
        getSamplePreparationProtocol().setInstruments(getInstruments());
        getSamplePreparationProtocol().setSequencingApplications(getSequencingApplications());
        getSamplePreparationProtocol().setSampleTypes(getSampleTypes());
    }

    public String getAdapter1() {
        if (getXmlRequestSaveSamplePreparationProtocol().getAdapter1() != null) {
            return getXmlRequestSaveSamplePreparationProtocol().getAdapter1();
        }
        return getSamplePreparationProtocol().getAdapter1();
    }

    public String getAdapter2() {
        if (getXmlRequestSaveSamplePreparationProtocol().getAdapter2() != null) {
            return getXmlRequestSaveSamplePreparationProtocol().getAdapter2();
        }
        return getSamplePreparationProtocol().getAdapter2();
    }

    public String getDescription() {
        if (getXmlRequestSaveSamplePreparationProtocol().getDescription() != null) {
            return getXmlRequestSaveSamplePreparationProtocol().getDescription();
        }
        return getSamplePreparationProtocol().getDescription();
    }

    public String getDiscriminator() {
        if (getXmlRequestSaveSamplePreparationProtocol().getDiscriminator() != null) {
            return getXmlRequestSaveSamplePreparationProtocol().getDiscriminator();
        }
        return getSamplePreparationProtocol().getDiscriminator() != null ? getSamplePreparationProtocol().getDiscriminator().toString() : null;
    }

    public Set<Instrument> getInstruments() throws InvalidDataException {
        if (getXmlRequestSaveSamplePreparationProtocol().getInstrumentid() != null) {
            Set<Instrument> instruments = new HashSet<>();
            for (String instrumentId : getXmlRequestSaveSamplePreparationProtocol().getInstrumentid()) {
                if (!instrumentId.isEmpty()) {
                    Instrument instrument = (Instrument) fetch(Instrument.class, MFHelper.positiveLongValueOf("instrumentid", instrumentId));
                    instruments.add(instrument);
                }
            }
            return instruments;
        }
        return getSamplePreparationProtocol().getInstruments();
    }

    public String getName() {
        if (getXmlRequestSaveSamplePreparationProtocol().getName() != null) {
            return getXmlRequestSaveSamplePreparationProtocol().getName();
        }
        return getSamplePreparationProtocol().getName();
    }

    public SamplePreparationProtocol getPredecessor() throws InvalidDataException {
        if (getXmlRequestSaveSamplePreparationProtocol().getPredecessorid() != null) {
            return (SamplePreparationProtocol) fetch(SamplePreparationProtocol.class, MFHelper.positiveLongValueOf("predecessorid", getXmlRequestSaveSamplePreparationProtocol().getPredecessorid()));
        }
        return getSamplePreparationProtocol().getPredecessor();
    }

    public SamplePreparationProtocol getSamplePreparationProtocol() {
        return samplePreparationProtocol;
    }

    public Set<SampleType> getSampleTypes() throws InvalidDataException {
        if (getXmlRequestSaveSamplePreparationProtocol().getSampletypeid() != null) {
            Set<SampleType> sampleTypes = new HashSet<>();
            for (String sampleTypeId : getXmlRequestSaveSamplePreparationProtocol().getSampletypeid()) {
                if (!sampleTypeId.isEmpty()) {
                    SampleType sampleType = (SampleType) fetch(SampleType.class, MFHelper.positiveLongValueOf("sampletypeid", sampleTypeId));
                    sampleTypes.add(sampleType);
                }
            }
            return sampleTypes;
        }
        return getSamplePreparationProtocol().getSampleTypes();
    }

    public Set<SequencingApplication> getSequencingApplications() throws InvalidDataException {
        if (getXmlRequestSaveSamplePreparationProtocol().getSequencingapplicationid() != null) {
            Set<SequencingApplication> sequencingApplications = new HashSet<>();
            for (String sequencingApplicationId : getXmlRequestSaveSamplePreparationProtocol().getSequencingapplicationid()) {
                if (!sequencingApplicationId.isEmpty()) {
                    SequencingApplication sequencingApplication = (SequencingApplication) fetch(SequencingApplication.class, MFHelper.positiveLongValueOf("sequencingapplicationid", sequencingApplicationId));
                    sequencingApplications.add(sequencingApplication);
                }
            }
            return sequencingApplications;
        }
        return getSamplePreparationProtocol().getSequencingApplications();
    }

    public Annotation getStrandedness() throws InvalidDataException {
        if (getXmlRequestSaveSamplePreparationProtocol().getStrandedness() != null) {
            List<Annotation> annotations = CDI.current().select(AnnotationService.class).get()
                .getAnnotationsByNameAndType(getXmlRequestSaveSamplePreparationProtocol().getStrandedness(), "Strandedness");
            if (annotations.size() != 1) {
                throw new InvalidDataException("Invalid strandedness value: " + getXmlRequestSaveSamplePreparationProtocol().getStrandedness() + " Valid values: " + CollectionHelper.printNames(CDI.current()
                    .select(AnnotationService.class).get()
                    .getAnnotationsByType("Strandedness")));
            }
            return annotations.get(0);
        }
        return getSamplePreparationProtocol().getStrandedness();
    }

    public Set<Technology> getTechnologies() throws InvalidDataException {
        if (getXmlRequestSaveSamplePreparationProtocol().getTechnologyid() != null) {
            Set<Technology> technologies = new HashSet<>();
            for (String technologyId : getXmlRequestSaveSamplePreparationProtocol().getTechnologyid()) {
                if (!technologyId.isEmpty()) {
                    Technology technology = (Technology) fetch(Technology.class, MFHelper.positiveLongValueOf("technologyid", technologyId));
                    technologies.add(technology);
                }
            }
            return technologies;
        }
        return getSamplePreparationProtocol().getTechnologies();
    }

    public String getType() {
        if (getXmlRequestSaveSamplePreparationProtocol().getType() != null) {
            return getXmlRequestSaveSamplePreparationProtocol().getType();
        }
        return getSamplePreparationProtocol().getType() != null ? getSamplePreparationProtocol().getType().toString() : null;
    }

    public XMLRequestParameterSaveSamplePreparationProtocol getXmlRequestSaveSamplePreparationProtocol() {
        return xmlRequestSaveSamplePreparationProtocol;
    }
}
