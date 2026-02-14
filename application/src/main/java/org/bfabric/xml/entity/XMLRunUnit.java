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

import org.bfabric.entity.RunUnit;
import org.bfabric.entity.RunUnitLane;
import org.bfabric.entity.Sample;

@XmlRootElement(name = "rununit")
public class XMLRunUnit extends XMLAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String capacity;

    @XmlElement
    private String cellid;

    @XmlElement
    private XMLInstrument instrument;

    @XmlElement
    private String numberoflanes;

    @XmlElement
    private String physicalseparation;

    @XmlElement
    private XMLRun run;

    @XmlElement
    private List<XMLRunUnitLane> rununitlane = new ArrayList<>();

    @XmlElement
    private String rununittype;

    @XmlElement
    private List<XMLSample> sample = new ArrayList<>();

    public XMLRunUnit() {
    }

    public XMLRunUnit(RunUnit entity, boolean reference) {
        super(entity, reference);
    }

    public XMLRunUnit(RunUnit entity) {
        super(entity);
        if (entity != null) {
            if (entity.getRun() != null) {
                setRun(new XMLRun(entity.getRun(), true));
            }
            if (entity.getRunUnitType() != null) {
                setRununittype(entity.getRunUnitType().getDisplayName());
                setNumberoflanes(String.valueOf(entity.getRunUnitType().getNumberOfLanes()));
                setCapacity(String.valueOf(entity.getRunUnitType().getCapacity()));
                setPhysicalseparation(String.valueOf(entity.isPhysicalSeparation()));
                setInstrument(new XMLInstrument(entity.getRunUnitType().getInstrument(), true));
            }
            if (entity.getRunUnitLanes() != null) {
                for (RunUnitLane runUnitLane : entity.getRunUnitLanes()) {
                    getRununitlane().add(new XMLRunUnitLane(runUnitLane, true));
                }
            }
            if (entity.getSamples() != null) {
                for (Sample aSample : entity.getSamples()) {
                    getSample().add(new XMLSample(aSample, true));
                }
            }
            setCellid(entity.getCellId());
        }
    }

    public String getCapacity() {
        return capacity;
    }

    public String getCellid() {
        return cellid;
    }

    public XMLInstrument getInstrument() {
        return instrument;
    }

    public String getNumberoflanes() {
        return numberoflanes;
    }

    public String getPhysicalseparation() {
        return physicalseparation;
    }

    public XMLRun getRun() {
        return run;
    }

    public List<XMLRunUnitLane> getRununitlane() {
        return rununitlane;
    }

    public String getRununittype() {
        return rununittype;
    }

    public List<XMLSample> getSample() {
        return sample;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public void setCellid(String cellid) {
        this.cellid = cellid;
    }

    public void setInstrument(XMLInstrument instrument) {
        this.instrument = instrument;
    }

    public void setNumberoflanes(String numberoflanes) {
        this.numberoflanes = numberoflanes;
    }

    public void setPhysicalseparation(String physicalseparation) {
        this.physicalseparation = physicalseparation;
    }

    public void setRun(XMLRun run) {
        this.run = run;
    }

    public void setRununitlane(List<XMLRunUnitLane> rununitlane) {
        this.rununitlane = rununitlane;
    }

    public void setRununittype(String rununittype) {
        this.rununittype = rununittype;
    }

    public void setSample(List<XMLSample> sample) {
        this.sample = sample;
    }
}
