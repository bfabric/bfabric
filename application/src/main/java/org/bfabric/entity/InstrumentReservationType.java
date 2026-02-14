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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
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
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@XmlRootElement
public class InstrumentReservationType extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @XmlElement
    private boolean chargeable = true;

    @NotBlank
    @Column(length = 9)
    @Size(max = 9)
    @XmlElement
    private String color = "#000000";

    @XmlElement
    private boolean containerAssociated = true;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "instrumentReservationType", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<InstrumentReservation> instrumentReservations = new HashSet<>();

    @Override
    public InstrumentReservationType clone() throws CloneNotSupportedException {
        InstrumentReservationType clone = (InstrumentReservationType) super.clone();
        clone.instrumentReservations = new HashSet<>();
        return clone;
    }

    public String getColor() {
        return color;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public Set<InstrumentReservation> getInstrumentReservations() {
        return instrumentReservations;
    }

    public String getStyleClass() {
        return getColor() != null && getName() != null ? StringHelper.firstLower(StringHelper.stripNonAlphaNumeric(getName())) : null;
    }

    public boolean isChargeable() {
        return chargeable;
    }

    public boolean isContainerAssociated() {
        return containerAssociated;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return isCreatable();
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && getInstrumentReservations().isEmpty();
    }

    public void setChargeable(boolean chargeable) {
        this.chargeable = chargeable;
    }

    public void setColor(String color) {
        this.color = StringHelper.format(color);
    }

    public void setContainerAssociated(boolean containerAssociated) {
        this.containerAssociated = containerAssociated;
    }

    public void setInstrumentReservations(Set<InstrumentReservation> instrumentReservations) {
        this.instrumentReservations = instrumentReservations;
    }
}