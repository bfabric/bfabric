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

import org.bfabric.entity.RunUnitLane;
import org.bfabric.entity.Sample;

@XmlRootElement(name = "rununitlane")
public class XMLRunUnitLane extends XMLAbstractBaseEntity {

    @XmlElement
    private String position;

    @XmlElement
    private XMLRun run;

    @XmlElement
    private XMLRunUnit rununit;

    @XmlElement
    private List<XMLSample> sample = new ArrayList<>();

    @XmlElement
    private String unassignedreads;

    public XMLRunUnitLane() {
    }

    public XMLRunUnitLane(RunUnitLane entity) {
        super(entity);
        if (entity != null) {
            setUnassignedreads(String.valueOf(entity.getUnassignedReads()));
            if (entity.getRunUnit() != null) {
                setRununit(new XMLRunUnit(entity.getRunUnit(), true));
                if (entity.getRunUnit().getRun() != null) {
                    setRun(new XMLRun(entity.getRunUnit().getRun(), true));
                }
            }
            if (entity.getPosition() != null) {
                setPosition(String.valueOf(entity.getPosition()));
            }
            if (entity.getSamples() != null) {
                for (Sample aSample : entity.getSamples()) {
                    getSample().add(new XMLSample(aSample, true));
                }
            }
        }
    }

    public XMLRunUnitLane(RunUnitLane entity, boolean reference) {
        super(entity, reference);
    }

    public String getPosition() {
        return position;
    }

    public XMLRun getRun() {
        return run;
    }

    public XMLRunUnit getRununit() {
        return rununit;
    }

    public List<XMLSample> getSample() {
        return sample;
    }

    public String getUnassignedreads() {
        return unassignedreads;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setRun(XMLRun run) {
        this.run = run;
    }

    public void setRununit(XMLRunUnit rununit) {
        this.rununit = rununit;
    }

    public void setSample(List<XMLSample> sample) {
        this.sample = sample;
    }

    public void setUnassignedreads(String unassignedreads) {
        this.unassignedreads = unassignedreads;
    }
}