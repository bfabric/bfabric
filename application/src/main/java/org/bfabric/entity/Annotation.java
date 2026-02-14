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

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.SamplePreparationProtocolService;
import org.bfabric.service.SampleService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.StringHelper;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "annotation_name_type_unique", columnNames = { "name", "type" }) })
@XmlRootElement
@NamedQuery(name = "Annotation.findByNameAndType", query = "SELECT a FROM Annotation a WHERE LOWER(a.name) = LOWER(:name) AND a.type = :type")
@NamedQuery(name = "Annotation.findByType", query = "SELECT a FROM Annotation a WHERE a.type = :type ORDER BY a.na DESC, a.top DESC, LOWER(a.name)")
@NamedQuery(name = "Annotation.findNamesByType", query = "SELECT a.name FROM Annotation a WHERE a.type = :type ORDER BY a.na DESC, a.top DESC, LOWER(a.name)")
@NamedQuery(name = "Annotation.findByTypeAndReleasedNot", query = "SELECT a FROM Annotation a WHERE a.type = :type AND a.released <> true ORDER BY a.name")
@NamedQuery(name = "Annotation.findReleased", query = "SELECT a FROM Annotation a WHERE a.released = true ORDER BY a.name")
@NamedQuery(name = "Annotation.findReleasedIncluding", query = "SELECT a FROM Annotation a WHERE a.released = true OR a = :entity ORDER BY a.name")
@NamedQuery(name = "Annotation.types", query = "SELECT distinct a.type FROM Annotation a ORDER BY a.type")
@NamedQuery(name = "Annotation.findSimilarAnnotations", query = "SELECT DISTINCT a2 FROM Annotation a1, Annotation a2 WHERE a1.id = :id AND a1.id <> a2.id AND a1.type = a2.type AND (LOWER(a1.name) like '%' || LOWER(a2.name) || '%' OR LOWER(a2.name) like '%' || LOWER(a1.name) || '%')")
@NamedQuery(name = "Annotation.checkUniqueName", query = "SELECT a.id FROM Annotation a WHERE LOWER(a.name) = LOWER(:name) AND a.id <> :id AND a.type = :type")
public class Annotation extends AbstractDescriptionNamedBaseEntity implements ShowScreen, Indexable, Mergeable {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<AnnotationComment> comments = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean common = true;

    @Transient
    private BfabricLazyDataModel<Instrument> instruments;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean na = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean released = false;

    @Transient
    private BfabricLazyDataModel<SamplePreparationProtocol> samplePreparationProtocols;

    @Transient
    private BfabricLazyDataModel<Sample> samples;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean top = false;

    @NotBlank
    @Size(max = 256)
    @XmlElement
    private String type;

    public Annotation() {
        super();
    }

    public Annotation(String name, String type) {
        super();
        setName(name);
        setType(type);
    }

    public static String toColumnName(String typename) {
        return typename.isEmpty() ? Constants.EMPTY_STRING : StringHelper.transformKeyName(typename);
    }

    @Override
    public Annotation clone() throws CloneNotSupportedException {
        return (Annotation) super.clone();
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.ANNOTATION_COMMENT;
    }

    public Set<AnnotationComment> getComments() {
        return comments;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ANNOTATIONMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getType())) {
            addEntityInfoItem(summary, "type", getType());
        }
        addEntityInfoItem(summary, "na", isNa());
        addEntityInfoItem(summary, "top", isTop());
        addEntityInfoItem(summary, "released", isReleased());
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
        fields.add(IndexMapContentEnum.RELEASED.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();
        content.add(IndexMapContentEnum.NAME, getName());
        content.add(IndexMapContentEnum.TYPE, getType());
        content.add(IndexMapContentEnum.RELEASED, isReleased());
        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.ANNOTATION;
    }

    public BfabricLazyDataModel<Instrument> getInstruments() {
        if (instruments == null) {
            instruments = CDI.current().select(InstrumentService.class).get().getLazyModelByAnnotation(this);
        }
        return instruments;
    }

    public BfabricLazyDataModel<SamplePreparationProtocol> getSamplePreparationProtocols() {
        if (samplePreparationProtocols == null) {
            samplePreparationProtocols = CDI.current().select(SamplePreparationProtocolService.class).get().getLazyModelByAnnotation(this);
        }
        return samplePreparationProtocols;
    }

    public BfabricLazyDataModel<Sample> getSamples() {
        if (samples == null) {
            samples = CDI.current().select(SampleService.class).get().getSamplesByAnnotation(this);
        }
        return samples;
    }

    public String getType() {
        return type;
    }

    @Override
    public void indexDependents() {
        // IndexHelper.indexEntities(getSamples());
    }

    public boolean isCommon() {
        return common;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getInstruments().isEmpty() && getSamplePreparationProtocols().isEmpty() && getSamples().isEmpty();
    }

    public boolean isMultiValued() {
        return SampleAttributeEnum.isAnnotationTypeMultiValued(getType());
    }

    public boolean isNa() {
        return na;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    public boolean isReleasable() {
        return !isReleased() && isUpdatable() && hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isReleased() {
        return released;
    }

    public boolean isSingleValued() {
        return SampleAttributeEnum.isAnnotationTypeSingleValued(getType());
    }

    public boolean isTop() {
        return top;
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || getCurrentUsername().equals(getModifiedBy());
    }

    public void setComments(Set<AnnotationComment> comments) {
        this.comments = comments;
    }

    public void setCommon(boolean common) {
        this.common = common;
    }

    public void setNa(boolean na) {
        this.na = na;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public void setType(String type) {
        this.type = StringHelper.format(type);
    }
}
