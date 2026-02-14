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

package org.bfabric.webservice.request.parameter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveSamplePreparationProtocol extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String adapter1;

    @XmlElement
    private String adapter2;

    @XmlElement(required = true)
    private String discriminator;

    @XmlElement
    private List<String> instrumentid;

    @XmlElement
    private String predecessorid;

    @XmlElement
    private List<String> sampletypeid;

    @XmlElement
    private List<String> sequencingapplicationid;

    @XmlElement
    private String strandedness;

    @XmlElement
    private List<String> technologyid;

    @XmlElement(required = true)
    private String type;

    public String getAdapter1() {
        return adapter1;
    }

    public String getAdapter2() {
        return adapter2;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public List<String> getInstrumentid() {
        return instrumentid;
    }

    public String getPredecessorid() {
        return predecessorid;
    }

    public List<String> getSampletypeid() {
        return sampletypeid;
    }

    public List<String> getSequencingapplicationid() {
        return sequencingapplicationid;
    }

    public String getStrandedness() {
        return strandedness;
    }

    public List<String> getTechnologyid() {
        return technologyid;
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

    public void setInstrumentid(List<String> instrumentid) {
        this.instrumentid = instrumentid;
    }

    public void setPredecessorid(String predecessorid) {
        this.predecessorid = predecessorid;
    }

    public void setSampletypeid(List<String> sampletypeid) {
        this.sampletypeid = sampletypeid;
    }

    public void setSequencingapplicationid(List<String> sequencingapplicationid) {
        this.sequencingapplicationid = sequencingapplicationid;
    }

    public void setStrandedness(String strandedness) {
        this.strandedness = strandedness;
    }

    public void setTechnologyid(List<String> technologyid) {
        this.technologyid = technologyid;
    }

    public void setType(String type) {
        this.type = type;
    }
}