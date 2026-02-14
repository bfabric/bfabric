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
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.MultiplexIdService;
import org.bfabric.service.OrderedEntityService;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class MultiplexKit extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "multiplexKit", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlElement(name = "multiplexid")
    private final Set<MultiplexId> multiplexIds = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean combinedMultiplexId = false;

    @Transient
    private Integer createMultiplexIdsNumber;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean multiplexIdOrderColumnWise = true;

    @Transient
    private List<MultiplexId> orderedEnabledMultiplexIds;

    @ManyToMany
    @JoinTable(name = "multiplexkitservicetype", joinColumns = @JoinColumn(name = "multiplexkitid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "servicetype")
    private Set<ServiceType> serviceTypes = new HashSet<>();

    public void createMultiplexIds() {
        getMultiplexIds().clear();
        long nextOrderPosition = CDI.current().select(OrderedEntityService.class).get().getNextOrderPositionByClass(MultiplexId.class);
        for (int i = 1; i <= getCreateMultiplexIdsNumber(); i++) {
            MultiplexId newMultiplexId = new MultiplexId();
            newMultiplexId.setOrderPosition(nextOrderPosition + i - 1);
            newMultiplexId.setMultiplexKit(this);
            getMultiplexIds().add(newMultiplexId);
        }
    }

    public int getCreateMultiplexIdsNumber() {
        if (createMultiplexIdsNumber == null) {
            createMultiplexIdsNumber = Math.max(getMultiplexIds().size(), 1);
        }
        return createMultiplexIdsNumber;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "combinedMultiplexId", isCombinedMultiplexId());
        addEntityInfoItem(summary, "multiplexIdOrderColumnWise", isMultiplexIdOrderColumnWise());
        if (getMultiplexIds() != null && !getMultiplexIds().isEmpty()) {
            addEntityInfoItem(summary, "multiplexIds", getMultiplexIds().size());
        }
        return summary.toString();
    }

    public Set<MultiplexId> getMultiplexIds() {
        return multiplexIds;
    }

    public List<MultiplexId> getMultiplexIdsAsList() {
        return CollectionHelper.sortObjects(multiplexIds);
    }

    public List<MultiplexId> getOrderedEnabledMultiplexIdsByPlateLayoutAndType(PlateLayout plateLayout, boolean isSampleAssignmentPerRow, String type) {
        if (orderedEnabledMultiplexIds == null) {
            orderedEnabledMultiplexIds = CDI.current().select(MultiplexIdService.class).get()
                .getOrderedEnabledMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(getId(), plateLayout, isSampleAssignmentPerRow, type);
        }
        return orderedEnabledMultiplexIds;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public List<ServiceType> getServiceTypesAsList() {
        return CollectionHelper.asList(serviceTypes);
    }

    public boolean isCombinedMultiplexId() {
        return combinedMultiplexId;
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getMultiplexIds().isEmpty();
    }

    public boolean isMultiplexIdOrderColumnWise() {
        return multiplexIdOrderColumnWise;
    }

    @Override
    public boolean isReadable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.RUNREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setCombinedMultiplexId(boolean combinedMultiplexId) {
        this.combinedMultiplexId = combinedMultiplexId;
    }

    public void setCreateMultiplexIdsNumber(int createMultiplexIdsNumber) {
        this.createMultiplexIdsNumber = createMultiplexIdsNumber;
    }

    public void setMultiplexIdOrderColumnWise(boolean multiplexIdOrderColumnWise) {
        this.multiplexIdOrderColumnWise = multiplexIdOrderColumnWise;
    }

    public void setOrderedEnabledMultiplexIds(List<MultiplexId> orderedEnabledMultiplexIds) {
        this.orderedEnabledMultiplexIds = orderedEnabledMultiplexIds;
    }

    public void setServiceTypes(Set<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setServiceTypesAsList(final List<ServiceType> serviceTypes) {
        setServiceTypes((Set<ServiceType>) CollectionHelper.asSet(serviceTypes));
    }
}
