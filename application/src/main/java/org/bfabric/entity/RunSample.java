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

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;

@Entity
@XmlRootElement
@NamedQuery(name = "RunSample.findByRunIdAndSampleIds", query = "SELECT a FROM RunSample a WHERE a.run.id = :runId AND a.sample.id IN (:sampleIds)")
public class RunSample extends AbstractBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal readCountTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runid")
    @NotNull
    @XmlIDREF
    private Run run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampleid")
    @NotNull
    @XmlIDREF
    private Sample sample;

    public RunSample() {
    }

    public RunSample(Run run, Sample sample) {
        setRun(run);
        setSample(sample);
    }

    @Override
    public RunSample clone() throws CloneNotSupportedException {
        RunSample clone = (RunSample) super.clone();
        clone.readCountTotal = null;
        return clone;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getRun().getName() + "_" + getSample().getName();
    }

    public BigDecimal getReadCountTotal() {
        return readCountTotal;
    }

    public Run getRun() {
        return run;
    }

    public Sample getSample() {
        return sample;
    }

    @Override
    public boolean isUpdatable() {
        return !getRun().isDemultiplexedOrFinished() && getRun().isUpdatable() && getSample().isUpdatable();
    }

    @Override
    public boolean isUpdatableWS() {
        return getRun().isUpdatableWS() && getSample().isUpdatableWS() && super.isUpdatableWS();
    }

    public void setReadCountTotal(BigDecimal readCountTotal) {
        this.readCountTotal = readCountTotal;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }
}