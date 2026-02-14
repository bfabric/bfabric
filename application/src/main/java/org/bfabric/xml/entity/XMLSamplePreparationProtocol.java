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

package org.bfabric.xml.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Instrument;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SampleType;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.entity.Technology;

@XmlRootElement(name = "samplepreparationprotocol")
public class XMLSamplePreparationProtocol extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private String adapter1;

    @XmlElement
    private String adapter2;

    @XmlElement
    private String discriminator;

    @XmlElement
    private List<XMLInstrument> instrument = new ArrayList<>();

    @XmlElement
    private XMLSamplePreparationProtocol predecessor;

    @XmlElement
    private List<String> sampletype = new ArrayList<>();

    @XmlElement
    private List<XMLSequencingApplication> sequencingapplication = new ArrayList<>();

    @XmlElement
    private String strandedness;

    @XmlElement
    private List<XMLSamplePreparationProtocol> successor = new ArrayList<>();

    @XmlElement
    private List<String> technology = new ArrayList<>();

    @XmlElement
    private String type;

    public XMLSamplePreparationProtocol() {
    }

    public XMLSamplePreparationProtocol(SamplePreparationProtocol entity, boolean reference) {
        super(entity, reference);
    }

    public XMLSamplePreparationProtocol(SamplePreparationProtocol entity) {
        super(entity);
        if (entity.getDiscriminator() != null) {
            setDiscriminator(entity.getDiscriminator().name());
        }
        if (entity.getType() != null) {
            setType(entity.getType().name());
        }
        if (entity.getPredecessor() != null) {
            setPredecessor(new XMLSamplePreparationProtocol(entity.getPredecessor(), true));
        }
        if (entity.getStrandedness() != null) {
            setStrandedness(entity.getStrandedness().getName());
        }
        if (entity.getAdapter1() != null) {
            setAdapter1(entity.getAdapter1());
        }
        if (entity.getAdapter2() != null) {
            setAdapter2(entity.getAdapter2());
        }
        if (entity.getSuccessors() != null) {
            for (SamplePreparationProtocol aSuccessor : entity.getSuccessors()) {
                getSuccessor().add(new XMLSamplePreparationProtocol(aSuccessor, true));
            }
        }
        if (entity.getTechnologies() != null) {
            for (Technology aTechnology : entity.getTechnologies()) {
                getTechnology().add(aTechnology.getName());
            }
        }
        if (entity.getInstruments() != null) {
            for (Instrument aInstrument : entity.getInstruments()) {
                getInstrument().add(new XMLInstrument(aInstrument, true));
            }
        }
        if (entity.getSequencingApplications() != null) {
            for (SequencingApplication aSequencingApplication : entity.getSequencingApplications()) {
                getSequencingapplication().add(new XMLSequencingApplication(aSequencingApplication, true));
            }
        }
        if (!entity.getSampleTypes().isEmpty()) {
            for (SampleType sampleType : entity.getSampleTypes()) {
                getSampletype().add(sampleType.getName());
            }
        }
    }

    public String getAdapter1() {
        return adapter1;
    }

    public String getAdapter2() {
        return adapter2;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public List<XMLInstrument> getInstrument() {
        return instrument;
    }

    public XMLSamplePreparationProtocol getPredecessor() {
        return predecessor;
    }

    public List<String> getSampletype() {
        return sampletype;
    }

    public List<XMLSequencingApplication> getSequencingapplication() {
        return sequencingapplication;
    }

    public String getStrandedness() {
        return strandedness;
    }

    public List<XMLSamplePreparationProtocol> getSuccessor() {
        return successor;
    }

    public List<String> getTechnology() {
        return technology;
    }

    public String getType() {
        return type;
    }

    public void setAdapter1(String adapter1) {
        this.adapter1 = adapter1;
    }

    public void setAdapter2(String adapter2) {
        this.adapter2 = adapter2;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public void setInstrument(List<XMLInstrument> instrument) {
        this.instrument = instrument;
    }

    public void setPredecessor(XMLSamplePreparationProtocol predecessor) {
        this.predecessor = predecessor;
    }

    public void setSampletype(List<String> sampletype) {
        this.sampletype = sampletype;
    }

    public void setSequencingapplication(List<XMLSequencingApplication> sequencingapplication) {
        this.sequencingapplication = sequencingapplication;
    }

    public void setStrandedness(String strandedness) {
        this.strandedness = strandedness;
    }

    public void setSuccessor(List<XMLSamplePreparationProtocol> successor) {
        this.successor = successor;
    }

    public void setTechnology(List<String> technology) {
        this.technology = technology;
    }

    public void setType(String type) {
        this.type = type;
    }
}