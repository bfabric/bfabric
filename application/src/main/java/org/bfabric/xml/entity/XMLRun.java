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

import org.bfabric.entity.Container;
import org.bfabric.entity.Run;
import org.bfabric.entity.Sample;

@XmlRootElement(name = "run")
public class XMLRun extends XMLAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private List<XMLContainer> container = new ArrayList<>();

    @XmlElement
    private String datafolder;

    @XmlElement
    private String demultiplexingrequired;

    @XmlElement
    private String instrument;

    @XmlElement
    private String instrumentreadconfiguration;

    @XmlElement
    private String physicalseparation;

    @XmlElement
    private String qc;

    @XmlElement
    private XMLRunUnit rununit;

    @XmlElement
    private List<XMLSample> sample = new ArrayList<>();

    @XmlElement
    private String serverlocation;

    @XmlElement
    private String status;

    @XmlElement
    private String statusmodified;

    @XmlElement
    private XMLUser statusmodifiedby;

    @XmlElement
    private XMLUser supervisor;

    public XMLRun() {
    }

    public XMLRun(Run entity, boolean reference) {
        super(entity, reference);
    }

    public XMLRun(Run entity) {
        super(entity);
        if (entity != null) {
            if (entity.getInstrument() != null) {
                setInstrument(entity.getInstrument().getDisplayName());
            }
            setDemultiplexingrequired(String.valueOf(entity.isDemultiplexingRequired()));
            setQc(String.valueOf(entity.isQc()));
            setDatafolder(entity.getDataFolder());
            setServerlocation(entity.getServerLocation());
            if (entity.getInstrumentReadConfiguration() != null) {
                setInstrumentreadconfiguration(entity.getInstrumentReadConfiguration().getDisplayName());
            }
            if (entity.getRunUnit() != null) {
                setRununit(new XMLRunUnit(entity.getRunUnit(), true));
                setPhysicalseparation(String.valueOf(entity.getRunUnit().isPhysicalSeparation()));
            }
            if (entity.getStatus() != null) {
                setStatus(entity.getStatus().getLabel());
            }
            if (entity.getSupervisor() != null) {
                setSupervisor(new XMLUser(entity.getSupervisor(), true));
            }
            if (entity.getSamples() != null) {
                for (Sample aSample : entity.getSamples()) {
                    getSample().add(new XMLSample(aSample, true));
                }
            }
            if (entity.getContainers() != null) {
                for (Container aContainer : entity.getContainers()) {
                    getContainer().add(new XMLContainer(aContainer, true));
                }
            }
            if (entity.getStatusModified() != null) {
                setStatusmodified(String.valueOf(entity.getStatusModified()));
            }
            if (entity.getStatusModifiedBy() != null) {
                setStatusmodifiedby(new XMLUser(entity.getStatusModifiedBy(), true));
            }
        }
    }

    public List<XMLContainer> getContainer() {
        return container;
    }

    public String getDatafolder() {
        return datafolder;
    }

    public String getDemultiplexingrequired() {
        return demultiplexingrequired;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getInstrumentreadconfiguration() {
        return instrumentreadconfiguration;
    }

    public String getPhysicalseparation() {
        return physicalseparation;
    }

    public String getQc() {
        return qc;
    }

    public XMLRunUnit getRununit() {
        return rununit;
    }

    public List<XMLSample> getSample() {
        return sample;
    }

    public String getServerlocation() {
        return serverlocation;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusmodified() {
        return statusmodified;
    }

    public XMLUser getStatusmodifiedby() {
        return statusmodifiedby;
    }

    public XMLUser getSupervisor() {
        return supervisor;
    }

    public void setContainer(List<XMLContainer> container) {
        this.container = container;
    }

    public void setDatafolder(String datafolder) {
        this.datafolder = datafolder;
    }

    public void setDemultiplexingrequired(String demultiplexingrequired) {
        this.demultiplexingrequired = demultiplexingrequired;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentreadconfiguration(String instrumentreadconfiguration) {
        this.instrumentreadconfiguration = instrumentreadconfiguration;
    }

    public void setPhysicalseparation(String physicalseparation) {
        this.physicalseparation = physicalseparation;
    }

    public void setQc(String qc) {
        this.qc = qc;
    }

    public void setRununit(XMLRunUnit rununit) {
        this.rununit = rununit;
    }

    public void setSample(List<XMLSample> sample) {
        this.sample = sample;
    }

    public void setServerlocation(String serverlocation) {
        this.serverlocation = serverlocation;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatusmodified(String statusmodified) {
        this.statusmodified = statusmodified;
    }

    public void setStatusmodifiedby(XMLUser statusmodifiedby) {
        this.statusmodifiedby = statusmodifiedby;
    }

    public void setSupervisor(XMLUser supervisor) {
        this.supervisor = supervisor;
    }
}
