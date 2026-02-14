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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "sampletype_name_unique", columnNames = { "name" }) })
@XmlRootElement
@NamedQuery(name = "SampleType.findByName", query = "SELECT a FROM SampleType a WHERE lower(a.name) = lower(:name)")
@NamedQuery(name = "SampleType.findByRunId", query = "SELECT DISTINCT sample.sampleType FROM RunUnit a JOIN a.runUnitLanes rul JOIN rul.samples sample WHERE a.run.id = :runId")
@NamedQuery(name = "SampleType.checkByName", query = "SELECT a FROM SampleType a WHERE lower(a.name) = lower(:name)")
public class SampleType extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "sampleType")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<Sample> samples = new HashSet<>();

    @OneToMany(mappedBy = "sampleType")
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<ServiceType> serviceTypes = new HashSet<>();

    @OneToMany(mappedBy = "sampleType")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<Order> orders = new HashSet<>();

    @NotBlank
    @Column(length = 9)
    @Size(max = 9)
    @XmlElement
    private String color = "#000000";

    @Transient
    private List<SampleAttributeEnum> sampleAttributesOptional;

    @Transient
    private List<SampleAttributeEnum> sampleAttributesRequired;

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocolsampletype", joinColumns = @JoinColumn(name = "sampletypeid"), inverseJoinColumns = @JoinColumn(name = "samplepreparationprotocolid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<SamplePreparationProtocol> samplePreparationProtocols = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean userDecisionOnParent = false;

    public SampleType() {
        super();
    }

    public String getColor() {
        return color;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getColor())) {
            addEntityInfoItem(summary, "color", getColor());
        }
        return summary.toString();
    }

    @Override
    public String getGroupingAttributes() {
        return getName();
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Collection<SampleAttributeEnum> getSampleAttributes() {
        Set<SampleAttributeEnum> attributes = new HashSet<>();
        attributes.addAll(getSampleAttributesOptional());
        attributes.addAll(getSampleAttributesRequired());
        return CollectionHelper.sortObjects(attributes);
    }

    public List<SampleAttributeEnum> getSampleAttributesOptional() {
        if (sampleAttributesOptional == null) {
            sampleAttributesOptional = SampleAttributeEnum.getAttributeEnumsOptional(getName());
        }
        return sampleAttributesOptional;
    }

    public List<SampleAttributeEnum> getSampleAttributesRequired() {
        if (sampleAttributesRequired == null) {
            sampleAttributesRequired = SampleAttributeEnum.getAttributeEnumsRequired(getName());
        }
        return sampleAttributesRequired;
    }

    public Set<SamplePreparationProtocol> getSamplePreparationProtocols() {
        return samplePreparationProtocols;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public boolean isAttributeOptional(SampleAttributeEnum sampleAttribute) {
        return getSampleAttributesOptional().contains(sampleAttribute);
    }

    public boolean isAttributeRequired(SampleAttributeEnum sampleAttribute) {
        return getSampleAttributesRequired().contains(sampleAttribute);
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getSamples().isEmpty() && getServiceTypes().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.SERVICEREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public boolean isUserDecisionOnParent() {
        return userDecisionOnParent;
    }

    public void setColor(String color) {
        this.color = StringHelper.format(color);
    }

    public void setSamplePreparationProtocols(Set<SamplePreparationProtocol> samplePreparationProtocols) {
        this.samplePreparationProtocols = samplePreparationProtocols;
    }

    public void setUserDecisionOnParent(boolean userDecisionOnParent) {
        this.userDecisionOnParent = userDecisionOnParent;
    }
}