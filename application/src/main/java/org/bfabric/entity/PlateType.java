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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "PlateType.findAllIncludingExcluding", query = "SELECT a FROM PlateType a WHERE a.name NOT IN (:excludedNames) OR a.id = :plateTypeId ORDER BY a.id DESC")
@NamedQuery(name = "PlateType.findByName", query = "SELECT a FROM PlateType a WHERE lower(a.name) = lower(:name)")
public class PlateType extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "plateType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Plate> plates = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampletypeid")
    private SampleType sampleType;

    @Override
    public PlateType clone() throws CloneNotSupportedException {
        PlateType clone = (PlateType) super.clone();
        clone.plates = new HashSet<>();
        return clone;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.PLATEMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getSampleType() != null) {
            addEntityInfoItem(summary, "sampleType", getSampleType().getName());
        }
        return summary.toString();
    }

    public Set<Plate> getPlates() {
        return plates;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration() != null && getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getPlates().isEmpty();
    }

    public boolean isIlluminaLibraryPlateType() {
        return getSampleType() != null && getSampleType().getName().equals(Constants.ILLUMINA_LIBRARY);
    }

    public boolean isLibraryPlateType() {
        return isIlluminaLibraryPlateType() || isNanoporeLibraryPlateType() || isPacBioLibraryPlateType() || isONTReadyMadeLibraryPlateType();
    }

    public boolean isNanoporeLibraryPlateType() {
        return getSampleType() != null && getSampleType().getName().equals(Constants.NANOPORE_LIBRARY);
    }

    public boolean isONTReadyMadeLibraryPlateType() {
        return getSampleType() != null && getSampleType().getName().equals(Constants.ONT_READY_MADE_LIBRARY);
    }

    public boolean isPacBioLibraryPlateType() {
        return getSampleType() != null && getSampleType().getName().equals(Constants.PACBIO_LIBRARY);
    }

    public boolean isQualityControlPlateType() {
        return getName().equals(Messages.get("qualityControl"));
    }

    @Override
    public boolean isReadable() {
        return getConfiguration() != null && getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.PLATEREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    public boolean isSampleTypeDisabled() {
        if (getSampleType() != null) {
            for (Plate plate : getPlates()) {
                if (!plate.getSamplePlatePositions().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }
}
