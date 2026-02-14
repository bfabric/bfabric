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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SamplePreparationProtocolDiscriminator;
import org.bfabric.enums.SamplePreparationProtocolType;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.PlateService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "SamplePreparationProtocol.filterEnabledIncludingByIdOrName", query = "SELECT a FROM SamplePreparationProtocol a WHERE (a.enabled is true or a.id = :entityId) and (str(a.id) LIKE :filterString or lower(a.name) LIKE lower(:filterString))")
@NamedQuery(name = "SamplePreparationProtocol.filterInstrument", query = "SELECT a FROM SamplePreparationProtocol a WHERE a.discriminator = org.bfabric.enums.SamplePreparationProtocolDiscriminator.Instrument and (str(a.id) LIKE :filterString or lower(a.name) LIKE lower(:filterString)) ORDER BY a.name ASC")
@NamedQuery(name = "SamplePreparationProtocol.filterInstrumentExcluding", query = "SELECT a FROM SamplePreparationProtocol a WHERE a.discriminator = org.bfabric.enums.SamplePreparationProtocolDiscriminator.Instrument and a NOT IN (:excluded) and (str(a.id) LIKE :filterString or lower(a.name) LIKE lower(:filterString)) ORDER BY a.name ASC")
public class SamplePreparationProtocol extends AbstractEnabledBaseEntity implements ShowScreen, TechnologiesDependent, Indexable {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "samplePreparationProtocol")
    private final Set<WorkflowStep> workflowSteps = new HashSet<>();

    @OneToMany(mappedBy = "samplePreparationProtocol")
    private final Set<WorkflowTemplateStep> workflowTemplateSteps = new HashSet<>();

    @Size(max = 64)
    @Pattern(regexp = Constants.SEQUENCE_CHARACTERS_REGEXP)
    @XmlElement
    private String adapter1;

    @Size(max = 64)
    @Pattern(regexp = Constants.SEQUENCE_CHARACTERS_REGEXP)
    @XmlElement
    private String adapter2;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private SamplePreparationProtocolDiscriminator discriminator;

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocolinstrumentreadconfiguration", joinColumns = @JoinColumn(name = "samplepreparationprotocolid"), inverseJoinColumns = @JoinColumn(name = "instrumentreadconfigurationid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<InstrumentReadConfiguration> instrumentReadConfigurations = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocolinstrument", joinColumns = @JoinColumn(name = "samplepreparationprotocolid"), inverseJoinColumns = @JoinColumn(name = "instrumentid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Instrument> instruments = new HashSet<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<SamplePreparationProtocolNote> notes = new HashSet<>();

    @OneToMany(mappedBy = "libraryProtocol")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessorid")
    @XmlIDREF
    private SamplePreparationProtocol predecessor;

    @ManyToMany
    @JoinTable(name = "samplepreparationprotocolsampletype", joinColumns = @JoinColumn(name = "samplepreparationprotocolid"), inverseJoinColumns = @JoinColumn(name = "sampletypeid"))
    @OrderBy("id desc")
    @XmlIDREF
    @XmlElement(name = "sampletype")
    private Set<SampleType> sampleTypes = new HashSet<>();

    @OneToMany(mappedBy = "samplePreparationProtocol")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Sample> samples = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "samplepreparationprotocolsequencingapplication", joinColumns = @JoinColumn(name = "samplepreparationprotocolid"), inverseJoinColumns = @JoinColumn(name = "sequencingapplicationid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<SequencingApplication> sequencingApplications = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strandednessid")
    @XmlIDREF
    private Annotation strandedness;

    @OneToMany(mappedBy = "predecessor")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<SamplePreparationProtocol> successors = new HashSet<>();

    @NotEmpty
    @ManyToMany
    @JoinTable(name = "samplepreparationprotocoltechnology", joinColumns = @JoinColumn(name = "samplepreparationprotocolid"), inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technology")
    private String technologiesAsString;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private SamplePreparationProtocolType type;

    public SamplePreparationProtocol() {
    }

    @Override
    public SamplePreparationProtocol clone() throws CloneNotSupportedException {
        SamplePreparationProtocol clone = (SamplePreparationProtocol) super.clone();
        clone.samples = new HashSet<>();
        clone.successors = new HashSet<>();
        return clone;
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        resetFields();
    }

    public String getAdapter1() {
        return adapter1;
    }

    public String getAdapter2() {
        return adapter2;
    }

    @Override
    public SamplePreparationProtocol getClone() {
        return (SamplePreparationProtocol) super.getClone();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.SAMPLEPREPARATIONPROTOCOLMANAGER;
    }

    @Override
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    public SamplePreparationProtocolDiscriminator getDiscriminator() {
        return discriminator;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getTechnologiesAsString())) {
            addEntityInfoItem(summary, "technologies", getTechnologiesAsString());
        }
        if (getType() != null) {
            addEntityInfoItem(summary, "type", getType());
        }
        if (getStrandedness() != null) {
            addEntityInfoItem(summary, "strandedness", getStrandedness().getName());
        }
        if (getDiscriminator() != null) {
            addEntityInfoItem(summary, "discriminator", getDiscriminator());
        }
        if (getPredecessor() != null) {
            addEntityInfoItem(summary, "predecessor", getPredecessor().getName());
        }
        return summary.toString();
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.TYPE.getField());
        fields.add(IndexMapContentEnum.DISCRIMINATOR.getField());
        fields.add(IndexMapContentEnum.ENABLED.getField());
        fields.add(IndexMapContentEnum.PREDECESSOR.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        fields.add(IndexMapContentEnum.SAMPLETYPE.getField());
        fields.add(IndexMapContentEnum.TECHNOLOGY.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();
        content.add(IndexMapContentEnum.NAME, getName());
        content.add(IndexMapContentEnum.TYPE, getType());
        content.add(IndexMapContentEnum.DISCRIMINATOR, getDiscriminator());
        content.add(IndexMapContentEnum.ENABLED, isEnabled());
        if (getPredecessor() != null) {
            content.add(IndexMapContentEnum.PREDECESSOR, getPredecessor().getName());
        }
        if (getSampleTypes() != null) {
            for (SampleType sampletype : getSampleTypes()) {
                content.add(IndexMapContentEnum.SAMPLETYPE, sampletype.getName(), CollectionHelper.print(getSampleTypes()));
            }
        }
        if (StringHelper.isNotEmpty(getTechnologiesAsString())) {
            content.add(IndexMapContentEnum.TECHNOLOGY, getTechnologiesAsString());
        }
        content.add(IndexMapContentEnum.DESCRIPTION, getDescription());
        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.SAMPLEPREPARATIONPROTOCOL;
    }

    public String getInstrumentReadConfiguration() {
        return instrumentReadConfigurations.stream().map(InstrumentReadConfiguration::getName).collect(Collectors.joining(", "));
    }

    public Set<InstrumentReadConfiguration> getInstrumentReadConfigurations() {
        return instrumentReadConfigurations;
    }

    public List<InstrumentReadConfiguration> getInstrumentReadConfigurationsAsList() {
        return CollectionHelper.asList(getInstrumentReadConfigurations());
    }

    public String getInstrumentReadConfigurationsDisplayNames() {
        return getInstrumentReadConfigurationsAsList().stream().map(AbstractNamedBaseEntity::getDisplayName).collect(Collectors.joining(", "));
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }

    public List<Instrument> getInstrumentsAsList() {
        return new ArrayList<>(getInstruments());
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.SAMPLE_PREPARATION_PROTOCOL_NOTE;
    }

    public Set<SamplePreparationProtocolNote> getNotes() {
        return notes;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public BfabricLazyDataModel<Plate> getPlatesLazyModel() {
        return CDI.current().select(PlateService.class).get().getPlatesBySamplePreparationProtocolId(getId());
    }

    public SamplePreparationProtocol getPredecessor() {
        return predecessor;
    }

    public Set<SampleType> getSampleTypes() {
        return sampleTypes;
    }

    public List<SampleType> getSampleTypesAsList() {
        return CollectionHelper.asList(sampleTypes);
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public Set<SequencingApplication> getSequencingApplications() {
        return sequencingApplications;
    }

    public List<SequencingApplication> getSequencingApplicationsAsList() {
        return CollectionHelper.asList(getSequencingApplications());
    }

    public Annotation getStrandedness() {
        return strandedness;
    }

    public Set<SamplePreparationProtocol> getSuccessors() {
        return successors;
    }

    public Set<Technology> getTechnologies() {
        return technologies;
    }

    public String getTechnologiesAsString() {
        return technologiesAsString;
    }

    public SamplePreparationProtocolType getType() {
        return type;
    }

    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public Set<WorkflowTemplateStep> getWorkflowTemplateSteps() {
        return workflowTemplateSteps;
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntities(getSamples());
    }

    public boolean isAttributesUpdatable() {
        return !getOrders().isEmpty() || !getSuccessors().isEmpty() || !getWorkflowSteps().isEmpty();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getOrders().isEmpty() && getSamples().isEmpty() && getSuccessors().isEmpty() && getWorkflowSteps().isEmpty() && getWorkflowTemplateSteps().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    public boolean isStrandednessRendered() {
        return getTechnologies().stream().map(Technology::getName).collect(Collectors.toSet()).contains("Genomics");
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void resetFields() {
        switch (getDiscriminator()) {
        case Instrument:
            getSequencingApplications().clear();
            break;
        case SequencingApplication:
            getInstruments().clear();
            break;
        default:
            break;
        }
    }

    public void resetSamplePreparationProtocolAttributes() {
        if (!isStrandednessRendered()) {
            setStrandedness(null);
        }
    }

    public void setAdapter1(String adapter1) {
        this.adapter1 = StringHelper.format(adapter1 != null ? adapter1.toUpperCase() : null);
    }

    public void setAdapter2(String adapter2) {
        this.adapter2 = StringHelper.format(adapter2 != null ? adapter2.toUpperCase() : null);
    }

    public void setDiscriminator(SamplePreparationProtocolDiscriminator discriminator) {
        this.discriminator = discriminator;
    }

    public void setInstrumentReadConfigurations(Set<InstrumentReadConfiguration> instrumentReadConfigurations) {
        this.instrumentReadConfigurations = instrumentReadConfigurations;
    }

    public void setInstrumentReadConfigurationsAsList(List<InstrumentReadConfiguration> instrumentReadConfigurations) {
        this.instrumentReadConfigurations = new HashSet<>(instrumentReadConfigurations);
    }

    public void setInstruments(Set<Instrument> instruments) {
        this.instruments = instruments;
    }

    public void setInstrumentsAsList(List<Instrument> instruments) {
        this.instruments = (Set<Instrument>) CollectionHelper.asSet(instruments);
    }

    public void setNotes(Set<SamplePreparationProtocolNote> notes) {
        this.notes = notes;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setPredecessor(SamplePreparationProtocol predecessor) {
        this.predecessor = predecessor;
    }

    public void setSampleTypes(Set<SampleType> sampleTypes) {
        this.sampleTypes = sampleTypes;
    }

    public void setSampleTypesAsList(List<SampleType> sampleTypes) {
        this.sampleTypes = (Set<SampleType>) CollectionHelper.asSet(sampleTypes);
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void setSequencingApplications(Set<SequencingApplication> sequencingApplications) {
        this.sequencingApplications = sequencingApplications;
    }

    public void setSequencingApplicationsAsList(List<SequencingApplication> sequencingApplications) {
        this.sequencingApplications = (Set<SequencingApplication>) CollectionHelper.asSet(sequencingApplications);
    }

    public void setStrandedness(Annotation strandedness) {
        this.strandedness = strandedness;
    }

    public void setSuccessors(Set<SamplePreparationProtocol> successors) {
        this.successors = successors;
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }

    public void setType(SamplePreparationProtocolType type) {
        this.type = type;
    }
}
