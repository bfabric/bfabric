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

import org.bfabric.entity.Plate;
import org.bfabric.entity.SamplePlatePosition;

@XmlRootElement(name = "plate")
public class XMLPlate extends XMLAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private XMLContainer container;

    @XmlElement
    private String layout;

    @XmlElement
    private List<XMLSamplePlatePosition> sample = new ArrayList<>();

    @XmlElement
    private String status;

    @XmlElement
    private String statusmodified;

    @XmlElement
    private XMLUser statusmodifiedby;

    @XmlElement
    private XMLUser supervisor;

    @XmlElement
    private String type;

    public XMLPlate() {
    }

    public XMLPlate(Plate entity, boolean reference) {
        super(entity, reference);
    }

    public XMLPlate(Plate entity) {
        super(entity);
        if (entity != null) {
            if (entity.getStatus() != null) {
                setStatus(entity.getStatus().getLabel());
            }
            if (entity.getPlateLayout() != null) {
                setLayout(entity.getPlateLayout().getName());
            }
            if (entity.getPlateType() != null) {
                setType(entity.getPlateType().getName());
            }
            if (entity.getContainer() != null) {
                setContainer(new XMLContainer(entity.getContainer(), true));
            }
            if (entity.getSupervisor() != null) {
                setSupervisor(new XMLUser(entity.getSupervisor(), true));
            }
            if (entity.getSamplePlatePositions() != null) {
                for (SamplePlatePosition samplePlatePosition : entity.getSamplePlatePositions()) {
                    getSample().add(new XMLSamplePlatePosition(samplePlatePosition, true));
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

    public XMLContainer getContainer() {
        return container;
    }

    public String getLayout() {
        return layout;
    }

    public List<XMLSamplePlatePosition> getSample() {
        return sample;
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

    public String getType() {
        return type;
    }

    public void setContainer(XMLContainer container) {
        this.container = container;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setSample(List<XMLSamplePlatePosition> sample) {
        this.sample = sample;
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

    public void setType(String type) {
        this.type = type;
    }
}
