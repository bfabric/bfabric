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

import java.util.Comparator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ParentDependent;

@Entity
@XmlRootElement
public class SamplePlatePosition extends AbstractEntity implements ParentDependent {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plateid")
    @NotNull
    @XmlIDREF
    private Plate plate;

    @NotNull
    @XmlElement
    private Long position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampleid")
    @NotNull
    @XmlIDREF
    private Sample sample;

    public SamplePlatePosition() {
    }

    public SamplePlatePosition(Sample sample, Plate plate, Long position) {
        setSample(sample);
        setPlate(plate);
        setPosition(position);
    }

    public static Comparator<SamplePlatePosition> getAssignmentPositionComparator(int numberOfColumns, boolean sampleAssignmentPerRow) {
        return (o1, o2) -> {
            if (!sampleAssignmentPerRow) {
                Long o1ColumnPosition = (o1.getPosition() - 1) % numberOfColumns;
                Long o2ColumnPosition = (o2.getPosition() - 1) % numberOfColumns;
                if (o1ColumnPosition < o2ColumnPosition) {
                    return -1;
                }
                if (o1ColumnPosition > o2ColumnPosition) {
                    return 1;
                }
                return Long.compare(o1.getPosition(), o2.getPosition());
            }
            return Long.compare(o1.getPosition(), o2.getPosition());
        };
    }

    @Override
    public AbstractBaseEntity getParent() {
        return getPlate();
    }

    @Override
    public String getParentClassName() {
        return getParent() != null ? getParent().getTrimmedClassName() : null;
    }

    @Override
    public Long getParentId() {
        return getParent().getId();
    }

    @Override
    public String getParentUrlShowScreen() {
        return getParent() != null ? getParent().getUrlShowScreen() : null;
    }

    public Plate getPlate() {
        return plate;
    }

    public Long getPosition() {
        return position;
    }

    public Sample getSample() {
        return sample;
    }

    @Override
    public void setParent(AbstractEntity parent) {
        setPlate((Plate) parent);
    }

    public void setPlate(Plate plate) {
        this.plate = plate;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }
}