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

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "MultiplexId.findByMultiplexKitIdOrderByNameAsc", query = "SELECT a FROM MultiplexId a WHERE a.multiplexKit.id = :multiplexKitId ORDER BY a.name ASC")
@NamedQuery(name = "MultiplexId.findByMultiplexKitIdEnabledOrderByOrderPositionAsc", query = "SELECT a FROM MultiplexId a WHERE a.multiplexKit.id = :multiplexKitId and a.enabled = true ORDER BY a.orderPosition ASC")
@NamedQuery(name = "MultiplexId.findByMultiplexKitIdAndSequenceOrderByNameAsc", query = "SELECT a FROM MultiplexId a WHERE a.multiplexKit.id = :multiplexKitId and a.sequence = :sequence ORDER BY a.name ASC")
public class MultiplexId extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @Column(columnDefinition = "boolean DEFAULT true")
    @XmlElement
    private boolean combinedXAxis = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplexkitid")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @NotNull
    @XmlIDREF
    private MultiplexKit multiplexKit;

    @XmlElement
    private Long plateNumber;

    @Size(max = 16)
    @XmlElement
    private String platePosition;

    @Size(max = 64)
    @XmlElement
    private String reverseComplementSequence;

    @Size(max = 64)
    @XmlElement
    private String reverseSequence;

    @NotNull
    @Size(max = 64)
    @XmlElement
    private String sequence;

    @Size(max = 16)
    @XmlElement
    private String type;

    public static String generateSequenceComplement(String sequence) {
        if (sequence != null) {
            //Generate the complement for the given sequence. C <-> G and  A <-> T
            char[] reverseComplementSequence = sequence.toCharArray();
            for (int i = 0; i < reverseComplementSequence.length; i++) {
                char character = reverseComplementSequence[i];
                if (character == 'C') {
                    reverseComplementSequence[i] = 'G';
                } else if (character == 'G') {
                    reverseComplementSequence[i] = 'C';
                } else if (character == 'A') {
                    reverseComplementSequence[i] = 'T';
                } else if (character == 'T') {
                    reverseComplementSequence[i] = 'A';
                }
            }
            return new String(reverseComplementSequence);
        }
        return null;
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        setReverseSequenceAndReverseComplementSequence();
    }

    public boolean getCombinedXAxis() {
        return combinedXAxis;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getType())) {
            addEntityInfoItem(summary, "type", getType());
        }
        if (getMultiplexKit() != null) {
            addEntityInfoItem(summary, "multiplexKit", getMultiplexKit().getName());
        }
        if (StringHelper.isNotEmpty(getSequence())) {
            addEntityInfoItem(summary, "sequence", getSequence());
        }
        if (StringHelper.isNotEmpty(getReverseSequence())) {
            addEntityInfoItem(summary, "reverseSequence", getReverseSequence());
        }
        if (StringHelper.isNotEmpty(getReverseComplementSequence())) {
            addEntityInfoItem(summary, "reverseComplementSequence", getReverseComplementSequence());
        }
        if (StringHelper.isNotEmpty(getSequence())) {
            addEntityInfoItem(summary, "sequenceLength", getSequence().length());
        }
        if (getPlateNumber() != null) {
            addEntityInfoItem(summary, "plateNumber", getPlateNumber());
        }
        if (StringHelper.isNotEmpty(getPlatePosition())) {
            addEntityInfoItem(summary, "platePosition", getPlatePosition());
        }
        if (getMultiplexKit() != null && getMultiplexKit().isCombinedMultiplexId()) {
            addEntityInfoItem(summary, "combinedXAxis", getCombinedXAxis());
        }
        return summary.toString();
    }

    @Override
    public String getGroupingAttributes() {
        return getMultiplexKit().getName();
    }

    public MultiplexKit getMultiplexKit() {
        return multiplexKit;
    }

    public Long getPlateNumber() {
        return plateNumber;
    }

    public String getPlatePosition() {
        return platePosition;
    }

    public String getReverseComplementSequence() {
        return reverseComplementSequence;
    }

    public String getReverseSequence() {
        return reverseSequence;
    }

    public String getSequence() {
        return sequence;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    public boolean isMultiplexId2AssignableOnly() {
        return getType() != null && getType().equals("i5");
    }

    public boolean isMultiplexIdAssignableOnly() {
        return getType() != null && getType().equals("i7");
    }

    @Override
    public boolean isReadable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.RUNREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void sequenceChangedListener(ValueChangeEvent event) {
        setSequence((String) event.getNewValue());
    }

    public void setCombinedXAxis(boolean combinedXAxis) {
        this.combinedXAxis = combinedXAxis;
    }

    public void setMultiplexKit(MultiplexKit multiplexKit) {
        this.multiplexKit = multiplexKit;
    }

    public void setPlateNumber(Long plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setPlatePosition(String platePosition) {
        this.platePosition = platePosition;
    }

    public void setReverseComplementSequence(String reverseComplementSequence) {
        this.reverseComplementSequence = reverseComplementSequence;
    }

    public void setReverseSequence(String reverseSequence) {
        this.reverseSequence = reverseSequence;
    }

    public void setReverseSequenceAndReverseComplementSequence() {
        if (getSequence() != null) {
            StringBuilder aReverseSequence = new StringBuilder(getSequence()).reverse();
            setReverseSequence(aReverseSequence.toString());
            setReverseComplementSequence(generateSequenceComplement(aReverseSequence.toString()));
        } else {
            setReverseSequence(null);
            setReverseComplementSequence(null);
        }
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
        setReverseSequenceAndReverseComplementSequence();
    }

    public void setType(String type) {
        this.type = type;
    }
}
