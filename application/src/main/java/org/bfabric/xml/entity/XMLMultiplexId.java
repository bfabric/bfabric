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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.MultiplexId;

@XmlRootElement(name = "multiplexid")
public class XMLMultiplexId extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private Boolean combinedxaxis;

    @XmlElement
    private XMLMultiplexKit multiplexkit;

    @XmlElement
    private Long platenumber;

    @XmlElement
    private String plateposition;

    @XmlElement
    private String reversecomplementsequence;

    @XmlElement
    private String reversesequence;

    @XmlElement
    private String sequence;

    @XmlElement
    private String type;

    public XMLMultiplexId() {
    }

    public XMLMultiplexId(MultiplexId entity, boolean reference) {
        super(entity, reference);
    }

    public XMLMultiplexId(MultiplexId entity) {
        super(entity);
        if (entity != null) {
            setMultiplexkit(new XMLMultiplexKit(entity.getMultiplexKit(), true));
            if (entity.getMultiplexKit() != null && entity.getMultiplexKit().isCombinedMultiplexId()) {
                setCombinedxaxis(entity.getCombinedXAxis());
            }
            if (entity.getPlateNumber() != null) {
                setPlatenumber(entity.getPlateNumber());
            }
            if (entity.getPlatePosition() != null) {
                setPlateposition(entity.getPlatePosition());
            }
            if (entity.getReverseComplementSequence() != null) {
                setReversecomplementsequence(entity.getReverseComplementSequence());
            }
            if (entity.getReverseSequence() != null) {
                setReversesequence(entity.getReverseSequence());
            }
            setSequence(entity.getSequence());
            if (entity.getType() != null) {
                setType(entity.getType());
            }
        }
    }

    public Boolean getCombinedxaxis() {
        return combinedxaxis;
    }

    public XMLMultiplexKit getMultiplexkit() {
        return multiplexkit;
    }

    public Long getPlatenumber() {
        return platenumber;
    }

    public String getPlateposition() {
        return plateposition;
    }

    public String getReversecomplementsequence() {
        return reversecomplementsequence;
    }

    public String getReversesequence() {
        return reversesequence;
    }

    public String getSequence() {
        return sequence;
    }

    public String getType() {
        return type;
    }

    public void setCombinedxaxis(Boolean combinedxaxis) {
        this.combinedxaxis = combinedxaxis;
    }

    public void setMultiplexkit(XMLMultiplexKit multiplexkit) {
        this.multiplexkit = multiplexkit;
    }

    public void setPlatenumber(Long platenumber) {
        this.platenumber = platenumber;
    }

    public void setPlateposition(String plateposition) {
        this.plateposition = plateposition;
    }

    public void setReversecomplementsequence(String reversecomplementsequence) {
        this.reversecomplementsequence = reversecomplementsequence;
    }

    public void setReversesequence(String reversesequence) {
        this.reversesequence = reversesequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void setType(String type) {
        this.type = type;
    }
}
