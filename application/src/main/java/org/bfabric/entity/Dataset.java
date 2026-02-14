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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.Positive;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Messages;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.ApplicationService;
import org.bfabric.service.DatasetService;
import org.bfabric.service.DatasetTemplateService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.ArrayListReader;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "Dataset.findByEntityReference", query = "SELECT DISTINCT a.dataset FROM DatasetAttribute a JOIN a.fields field WHERE a.type = :attributeType and field.value = :fieldValue")
@NamedQuery(name = "Dataset.findByAttributeTypeAndFieldValue", query = "SELECT DISTINCT a.dataset FROM DatasetAttribute a JOIN a.fields field WHERE lower(a.type) = lower(:attributeType) and lower(field.value) = lower(:fieldValue)")
public class Dataset extends AbstractContainerDependentEntity implements Indexable {

    public static final String DELIMITER = " | ";

    private static final long serialVersionUID = 1;

    // Helper variables indicating whether the check all button was pressed
    @Transient
    private final Map<DatasetAttribute, Boolean> checkedVisibleAttributes = new HashMap<>();

    @Transient
    private Application application;

    @Transient
    private BfabricLazyDataModel<DatasetTemplate> attributeEquivalentDatasetTemplates;

    @Transient
    private BfabricLazyDataModel<Dataset> attributeEquivalentDatasets;

    @OneToMany(mappedBy = "dataset", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("position")
    private List<DatasetAttribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<DatasetComment> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasetTemplateid")
    @XmlIDREF
    private DatasetTemplate datasetTemplate;

    @Transient
    private Set<DatasetField> entityNotFoundFields;

    @Transient
    private Boolean isRenderedTree;

    @OneToMany(mappedBy = "dataset", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("position")
    private List<DatasetItem> items = new ArrayList<>();

    @Transient
    @Positive
    private int maxNumberOfNewItems = 99;

    @Transient
    @Positive
    private int numberOfNewItems = 1;

    @Transient
    private boolean prepared;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runid")
    @XmlIDREF
    private Run run;

    @Transient
    private boolean showEmptyAttributes = false;

    @OneToMany(mappedBy = "inputDataset")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> succeedingWorkunits = new HashSet<>();

    @Transient
    private Set<DatasetField> typeInvalidFields;

    @ManyToMany
    @JoinTable(name = "workflowstepdataset", joinColumns = @JoinColumn(name = "datasetid"), inverseJoinColumns = @JoinColumn(name = "workflowstepid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowStep> workflowSteps = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workflowdataset", joinColumns = @JoinColumn(name = "datasetid"), inverseJoinColumns = @JoinColumn(name = "workflowid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workflow> workflows = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workunitid")
    @XmlIDREF
    private Workunit workunit;

    public Dataset() {
    }

    public static Dataset createDataset(String value, String delimiter) throws InvalidDataException {
        return createDataset(ArrayListReader.createArrayList(value, delimiter));
    }

    public static Dataset createDataset(List<List<String>> values) throws InvalidDataException {
        Dataset dataset = new Dataset();
        dataset.setContent(values);
        return dataset;
    }

    public static Dataset createDatasetFromCSV(String value) throws InvalidDataException {
        return createDataset(value, ",");
    }

    public static Dataset createDatasetFromTSV(String value) throws InvalidDataException {
        return createDataset(value, "\t");
    }

    private static String getObjectValue(Object value) {
        if (value != null) {
            if (value instanceof Collection) {
                if (!((Collection<?>) value).isEmpty()) {
                    return CollectionHelper.print((Collection<?>) value);
                }
            } else {
                if (value instanceof Annotation) {
                    return ((Annotation) value).getName();
                }
                return String.valueOf(value);
            }
        }
        return null;
    }

    public void addAttribute() {
        addAttribute(null, null, null, null, false);
    }

    public DatasetAttribute addAttribute(DatasetItem datasetItem, String attributeName, String attributeValue, String attributeType) {
        return addAttribute(datasetItem, attributeName, attributeValue, attributeType, false);
    }

    public DatasetAttribute addAttribute(DatasetItem datasetItem, String attributeName, String attributeValue, String attributeType, boolean required) {
        DatasetAttribute datasetAttribute = null;
        if (attributeName != null) {
            datasetAttribute = getAttribute(attributeName);
        }
        if (datasetAttribute == null) {
            datasetAttribute = new DatasetAttribute(this, null, attributeName, attributeType, required);
            getAttributes().add(datasetAttribute);
            if (getDatasetTemplate() != null && !isEquivalentDatasetTemplate()) {
                setDatasetTemplate(null);
            }
        }
        if (datasetItem != null) {
            datasetItem.getFields().add(new DatasetField(datasetItem, datasetAttribute, attributeValue));
        } else {
            for (DatasetItem item : getItems()) {
                item.getFields().add(new DatasetField(item, datasetAttribute, attributeValue));
            }
        }
        return datasetAttribute;
    }

    public void addItem() {
        DatasetItem datasetItem = new DatasetItem(this);

        for (DatasetAttribute datasetAttribute : getAttributes()) {
            DatasetField newField = new DatasetField(datasetItem, datasetAttribute);
            datasetItem.getFields().add(newField);
        }

        getItems().add(datasetItem);
    }

    public void addItems() {
        for (int i = 1; i <= getNumberOfNewItems(); i++) {
            addItem();
        }
    }

    public void checkAllAttributes() {
        for (final DatasetAttribute attribute : getAttributes()) {
            attribute.check();
        }
        checkVisibleAttributes();
    }

    public void checkAllItems() {
        for (final DatasetItem item : getItems()) {
            item.check();
        }
    }

    public void checkAttribute(String attributeName) throws InvalidDataException {
        DatasetAttribute attribute = getAttribute(attributeName);
        if (attribute != null) {
            List<String> availableNames = new ArrayList<>();
            for (DatasetAttribute existingAttribute : getAttributes()) {
                availableNames.add(existingAttribute.getName());
            }
            throw new InvalidDataException("The dataset already contains an attribute " + attributeName + "! Current attributes: " + CollectionHelper.print(availableNames) + ".");
        }
    }

    public void checkVisibleAttributes() {
        for (final DatasetAttribute attribute : getAttributes()) {
            if (!attribute.isRequired() && !isShowEmptyAttributes() && !attribute.hasNonEmptyField()) {
                getCheckedVisibleAttributes().put(attribute, Boolean.FALSE);
                attribute.uncheck();
            } else {
                getCheckedVisibleAttributes().put(attribute, Boolean.TRUE);
            }
        }
    }

    public void clearContent() {
        getAttributes().clear();
        getItems().clear();
    }

    @Override
    public Dataset clone() throws CloneNotSupportedException {
        Dataset clone = (Dataset) super.clone();
        clone.setRun(null);
        clone.setAttributes(new ArrayList<>());
        clone.setItems(new ArrayList<>());
        clone.setLinks(new ArrayList<>());
        clone.setSucceedingWorkunits(new HashSet<>());

        // Clone project reference.
        clone.setContainer(getContainer());

        // Set name.
        clone.setName("Clone " + getName());

        // Important: to ensure the one-to-one mapping, the workunit association should not be cloned!
        clone.setWorkunit(null);

        // Attribute map needed later for cloning the items/fields.
        Map<DatasetAttribute, DatasetAttribute> attributeMap = new HashMap<>();

        // Clone attributes.
        for (DatasetAttribute attribute : getAttributes()) {
            DatasetAttribute attributeClone = attribute.clone(clone);
            attributeMap.put(attribute, attributeClone);
        }

        // Clone items.
        for (DatasetItem item : getItems()) {
            clone.getItems().add(item.clone(clone, attributeMap));
        }

        // Mark the clone as prepared.
        clone.setPrepared(true);

        return clone;
    }

    public String convert(String delimiter) {
        StringBuilder value = new StringBuilder();
        value.append(getAttributes().stream().map(a -> a.getName(delimiter)).collect(Collectors.joining(delimiter)));
        value.append("\n");
        for (DatasetItem item : getItems()) {
            value.append(item.getFieldsOrderByPosition().stream().map(f -> f.getValue(delimiter)).collect(Collectors.joining(delimiter)));
            value.append("\n");
        }
        return value.toString();
    }

    public String convert2CSV() {
        return convert(",");
    }

    public Path convert2CSVFile() {
        return convert2File(",", "csv");
    }

    public Path convert2File(String delimiter, String extension) {
        try {
            Path file = Files.createTempFile(getTableContext(), extension);
            Files.write(file, convert(delimiter).getBytes(StandardCharsets.UTF_8));
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String convert2HTML() {
        StringBuilder value = new StringBuilder();
        value.append("<!DOCTYPE html><html><head><title>").append(getDisplayName())
            .append("</title><style>body{background-color: lightblue;}th,td{border: 1px black solid;background-color: white;}</style></head><body><h3>").append(this).append(": ")
            .append(getItems().size()).append(" ").append(Messages.get("items")).append(" ")
            .append(getHref(getShowScreenLink(""), getDisplayName())).append("</h3>");
        StringBuilder table = new StringBuilder();
        table.append(wrapTR(getAttributes().stream().map(a -> wrapTH(a.getName())).collect(Collectors.joining())));
        table.append(wrapTR(getAttributes().stream().map(a -> wrapTH(a.getType())).collect(Collectors.joining())));
        for (DatasetItem item : getItems()) {
            table.append(wrapTR(item.getFieldsOrderByPosition().stream().map(f -> wrapTD(f.getValue())).collect(Collectors.joining())));
        }
        value.append(wrapTable(table.toString()));
        value.append("</body></html>");
        return value.toString();
    }

    public Path convert2HTMLFile() {
        try {
            Path file = Files.createTempFile(getTableContext(), "html");
            Files.write(file, convert2HTML().getBytes(StandardCharsets.UTF_8));
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String convert2TSV() {
        return convert("\t");
    }

    public Path convert2TSVFile() {
        return convert2File("\t", "tsv");
    }

    public void createDatasetItem(Resource resource) {
        if (resource != null) {
            DatasetTemplate cachedDatasetTemplate = getDatasetTemplate();
            DatasetItem item = new DatasetItem(this);
            getItems().add(item);
            item.getResources().add(resource);
            if (cachedDatasetTemplate == null) {
                addAttribute(item, Resource.class.getSimpleName(), resource.getIdString(), Resource.class.getSimpleName());
                addAttribute(item, "Relative Path", resource.getRelativePath(), null);
                Sample sample = resource.getSampleTransitive();
                if (sample != null) {
                    addAttribute(item, "Sample", sample.getIdString(), null);
                    addAttribute(item, "Name", sample.getName(), null);
                } else {
                    addAttribute(item, "Sample", null, null);
                    addAttribute(item, "Name", null, null);
                }
                for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
                    String sampleAttributeValue = null;
                    if (sample != null) {
                        try {
                            sampleAttributeValue = getObjectValue(PropertyUtils.getProperty(sample, sampleAttributeEnum.getName()));
                        } catch (Exception ignored) {
                        }
                    }
                    addAttribute(item, sampleAttributeEnum.getLabel(), sampleAttributeValue, null);
                }
            } else {
                for (DatasetTemplateAttribute datasetTemplateAttribute : cachedDatasetTemplate.getDatasetTemplateAttributes()) {
                    String attributeName = datasetTemplateAttribute.getName();
                    String attributeValue = null;
                    if (attributeName.equalsIgnoreCase(Resource.class.getSimpleName())) {
                        attributeValue = resource.getIdString();
                    } else if (attributeName.equalsIgnoreCase("Relative Path")) {
                        attributeValue = resource.getRelativePath();
                    } else {
                        Sample sample = resource.getSampleTransitive();
                        if (sample != null) {
                            if (attributeName.equalsIgnoreCase("Sample")) {
                                attributeValue = sample.getIdString();
                            } else if (attributeName.equalsIgnoreCase("Name")) {
                                attributeValue = sample.getName();
                            } else {
                                for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
                                    if (attributeName.equalsIgnoreCase(sampleAttributeEnum.getLabel())) {
                                        try {
                                            attributeValue = getObjectValue(PropertyUtils.getProperty(sample, sampleAttributeEnum.getName()));
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    addAttribute(item, attributeName, attributeValue, datasetTemplateAttribute.getType());
                }
            }
            setDatasetTemplate(cachedDatasetTemplate);
        }
    }

    public void createDatasetItems(Set<Resource> resources) {
        for (final Resource resource : resources) {
            createDatasetItem(resource);
        }
        checkAllItems();
        checkAllAttributes();
    }

    public void datasetTemplateChanged(ValueChangeEvent event) {
        setDatasetTemplate((DatasetTemplate) event.getNewValue());
    }

    public void datasetTemplateClear() {
        setDatasetTemplate(null);
    }

    public DatasetTemplate deriveDatasetTemplate() {
        DatasetTemplate derivedDatasetTemplate = null;
        if (getDatasetTemplate() == null) {
            derivedDatasetTemplate = new DatasetTemplate();
            for (DatasetAttribute datasetAttribute : getAttributes()) {
                DatasetTemplateAttribute datasetTemplateAttribute = new DatasetTemplateAttribute(datasetAttribute, derivedDatasetTemplate);
                derivedDatasetTemplate.getDatasetTemplateAttributes().add(datasetTemplateAttribute);
            }
        }
        return derivedDatasetTemplate;
    }

    public void exportAndDownloadCSV() {
        download(getTableContext() + ".csv", convert2CSV());
    }

    public void exportAndDownloadHTML() {
        download(getTableContext() + ".html", convert2HTML());
    }

    public void exportAndDownloadTSV() {
        download(getTableContext() + ".tsv", convert2TSV());
    }

    public Application getApplication() {
        return application;
    }

    public DatasetAttribute getAttribute(String attributeName) {
        for (DatasetAttribute attribute : getAttributes()) {
            if (attributeName.equalsIgnoreCase(attribute.getName())) {
                return attribute;
            }
        }
        return null;
    }

    public DatasetAttribute getAttributeByPosition(int position) {
        return position > 0 && position <= getAttributes().size() ? getAttributes().get(position - 1) : null;
    }

    public DatasetAttribute getAttributeChecked(String attributeName) throws InvalidDataException {
        DatasetAttribute attribute = getAttribute(attributeName);
        if (attribute == null) {
            List<String> availableNames = new ArrayList<>();
            for (DatasetAttribute existingAttribute : getAttributes()) {
                availableNames.add(existingAttribute.getName());
            }
            throw new InvalidDataException("There is no dataset attribute " + attributeName + "! Available attributes: " + CollectionHelper.print(availableNames) + ".");
        }
        return attribute;
    }

    public BfabricLazyDataModel<DatasetTemplate> getAttributeEquivalentDatasetTemplates() {
        if (attributeEquivalentDatasetTemplates == null) {
            attributeEquivalentDatasetTemplates = CDI.current().select(DatasetTemplateService.class).get().getAttributeEquivalentDatasetTemplatesByDatasetId(getId());
        }
        return attributeEquivalentDatasetTemplates;
    }

    public BfabricLazyDataModel<Dataset> getAttributeEquivalentDatasets() {
        if (attributeEquivalentDatasets == null) {
            attributeEquivalentDatasets = CDI.current().select(DatasetService.class).get().getAttributeEquivalentDatasetsByDatasetId(getId());
        }
        return attributeEquivalentDatasets;
    }

    public String getAttributeTypesForXmlExport() {
        return getAttributes().stream().map(DatasetAttribute::getType).collect(Collectors.joining(DELIMITER));
    }

    public List<DatasetAttribute> getAttributes() {
        return attributes;
    }

    @XmlElement(name = "attribute")
    public List<String> getAttributesAsXml() {
        List<String> ret = new ArrayList<>();
        for (DatasetAttribute attribute : getAttributes()) {
            ret.add(attribute.getPosition() + " " + attribute.getName() + " (" + attribute.getType() + ")");
        }
        return ret;
    }

    public String getAttributesForXmlExport() {
        return getAttributes().stream().map(AbstractNamedBaseEntity::getName).collect(Collectors.joining(DELIMITER));
    }

    public Map<DatasetAttribute, Boolean> getCheckedVisibleAttributes() {
        return checkedVisibleAttributes;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.DATASET_COMMENT;
    }

    public Set<DatasetComment> getComments() {
        return comments;
    }

    // @XmlElement(name = "content")
    public List<String> getContentForXmlExport() {
        List<String> ret = new ArrayList<>();
        ret.add(getAttributesForXmlExport());
        ret.add(getAttributeTypesForXmlExport());
        for (DatasetItem item : getItems()) {
            ret.add(item.getFieldsForXmlExport());
        }
        return ret;
    }

    public DatasetTemplate getDatasetTemplate() {
        return datasetTemplate;
    }

    public DatasetTemplate getDatasetTemplateDerived() {
        return getDatasetTemplate() != null ? getDatasetTemplate() : deriveDatasetTemplate();
    }

    public Set<DatasetItem> getEmptyItems() {
        return getItems().stream().filter(DatasetItem::isEmpty).collect(Collectors.toSet());
    }

    public Set<DatasetField> getEntityNotFoundFields() {
        if (entityNotFoundFields == null) {
            typeCheck();
        }
        return entityNotFoundFields;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getAttributes() != null && !getAttributes().isEmpty()) {
            addEntityInfoItem(summary, "attributes", getAttributes().size());
        }
        if (getItems() != null && !getItems().isEmpty()) {
            addEntityInfoItem(summary, "items", getItems().size());
        }
        return summary.toString();
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = super.getIndexListingFields();
        fields.add(IndexMapContentEnum.WORKUNITID.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        if (getWorkunit() != null) {
            content.add(IndexMapContentEnum.WORKUNITID, getWorkunit().getId());
        }

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.DATASET;
    }

    public Set<Resource> getInputResources() {
        Set<Resource> inputResources = new HashSet<>();
        for (DatasetItem datasetItem : getItems()) {
            if (datasetItem.getResources() != null) {
                inputResources.addAll(datasetItem.getResources());
            }
        }
        return inputResources;
    }

    public DatasetItem getItem(int position) {
        for (DatasetItem item : getItems()) {
            if (position == item.getPosition()) {
                return item;
            }
        }

        return null;
    }

    public DatasetItem getItemChecked(int itemPosition) throws InvalidDataException {
        DatasetItem item = getItem(itemPosition);
        if (item == null) {
            List<Integer> availablePositions = new ArrayList<>();
            for (DatasetItem existingItem : getItems()) {
                availablePositions.add(existingItem.getPosition());
            }
            throw new InvalidDataException("There is no dataset item position " + itemPosition + "! Available item positions: " + CollectionHelper.print(availablePositions) + ".");
        }
        return item;
    }

    public List<DatasetItem> getItems() {
        return items;
    }

    @XmlElement(name = "item")
    public List<String> getItemsAsXml() {
        List<String> ret = new ArrayList<>();
        for (DatasetItem item : getItems()) {
            ret.add(item.getFieldsForXmlExport());
        }
        return ret;
    }

    public int getLastAttributePosition() {
        return !getAttributes().isEmpty() ? getAttributes().get(getAttributes().size() - 1).getPosition() : 0;
    }

    public int getLastItemPosition() {
        return !getItems().isEmpty() ? getItems().get(getItems().size() - 1).getPosition() : 0;
    }

    public int getMaxNumberOfNewItems() {
        return maxNumberOfNewItems;
    }

    public int getNextAttributePosition() {
        return getLastAttributePosition() + 1;
    }

    public DatasetAttribute getNextDatasetAttribute(DatasetAttribute datasetAttribute) {
        int listPosition = getAttributes().indexOf(datasetAttribute) + 1;
        if (listPosition < getAttributes().size()) {
            return getAttributes().get(listPosition);
        }
        return null;
    }

    public int getNextItemPosition() {
        return getLastItemPosition() + 1;
    }

    public int getNumberOfNewItems() {
        return numberOfNewItems;
    }

    public Run getRun() {
        return run;
    }

    @Override
    public List<Application> getRunnableApplications() {
        return CDI.current().select(ApplicationService.class).get().getRunnableApplications(getCurrentUser().hasCurrentUserRoleEnum(RoleEnum.APPLICATIONREADER), getDatasetTemplate());
    }

    public Set<Workunit> getSucceedingWorkunits() {
        return succeedingWorkunits;
    }

    public Set<DatasetField> getTypeInvalidFields() {
        if (typeInvalidFields == null) {
            typeCheck();
        }
        return typeInvalidFields;
    }

    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public Set<Workflow> getWorkflows() {
        return workflows;
    }

    public Workunit getWorkunit() {
        return workunit;
    }

    public boolean hasEmptyItems() {
        return getItems().stream().anyMatch(DatasetItem::isEmpty);
    }

    public void hideVisibleAttributes() {
        setShowEmptyAttributes(false);
        checkVisibleAttributes();
    }

    public void initializeContent() {
        if (getDatasetTemplate() != null) {
            setShowEmptyAttributes(true);
            getCheckedVisibleAttributes().clear();
            List<DatasetAttribute> datasetAttributeList = new ArrayList<>();
            for (DatasetTemplateAttribute datasetTemplateAttribute : getDatasetTemplate().getDatasetTemplateAttributes()) {
                DatasetAttribute datasetAttribute = new DatasetAttribute(datasetTemplateAttribute, this);
                datasetAttributeList.add(datasetAttribute);
                getCheckedVisibleAttributes().put(datasetAttribute, Boolean.TRUE);
                datasetAttribute.check();
            }
            setAttributes(datasetAttributeList);
            getItems().clear();
        } else {
            clearContent();
            addAttribute();
        }
        addItem();
    }

    public void initializeEmpty() {
        clearContent();
        addAttribute();
        addItem();
    }

    public boolean isAttributeEquivalentWith(DatasetTemplate datasetTemplate) {
        if (datasetTemplate != null && datasetTemplate.getDatasetTemplateAttributes().size() == getAttributes().size()) {
            for (DatasetTemplateAttribute attribute : datasetTemplate.getDatasetTemplateAttributes()) {
                DatasetAttribute attributeAtSamePosition = getAttributeByPosition(attribute.getPosition());
                if (!attribute.getType().equalsIgnoreCase(attributeAtSamePosition.getType()) || !attribute.getName().equalsIgnoreCase(attributeAtSamePosition.getName())
                    || attribute.isRequired() != attributeAtSamePosition.isRequired()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isAttributeNameUnique(String attributeName) {
        return getAttribute(attributeName) == null;
    }

    public boolean isCompatibleWith(DatasetTemplate datasetTemplate) {
        if (datasetTemplate != null) {
            for (DatasetTemplateAttribute datasetTemplateAttribute : datasetTemplate.getDatasetTemplateAttributes()) {
                DatasetAttribute datasetAttribute = getAttributeByPosition(datasetTemplateAttribute.getPosition());
                if (datasetAttribute == null || !datasetAttribute.getName().equalsIgnoreCase(datasetTemplateAttribute.getName()) || !datasetAttribute.getType()
                    .equalsIgnoreCase(datasetTemplateAttribute.getType())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isCompatibleWithDatasetTemplate() {
        return isCompatibleWith(getDatasetTemplate());
    }

    public boolean isContentEmpty() {
        return getAttributes().isEmpty() && getItems().isEmpty();
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    public boolean isDatasetTemplateCreatable() {
        return getDatasetTemplate() == null && new DatasetTemplate().isCreatable();
    }

    public boolean isDatasetTemplateDerivable() {
        return isDatasetTemplateCreatable() && isUpdatable();
    }

    public boolean isDatasetTemplateFixed() {
        return getDatasetTemplate() != null;
    }

    public boolean isDatasetTypeCheckEnabled() {
        return getConfiguration().isDatasetTypeCheckEnabled();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getRun() == null && getWorkflows().isEmpty() && getWorkflowSteps().isEmpty();
    }

    public boolean isEntityNotFound() {
        return !getEntityNotFoundFields().isEmpty();
    }

    public boolean isEquivalentByDatasetTemplate(DatasetTemplate datasetTemplate) {
        return datasetTemplate != null && datasetTemplate.getDatasetTemplateAttributes().size() == getAttributes().size() && isCompatibleWith(datasetTemplate);
    }

    public boolean isEquivalentDatasetTemplate() {
        return isEquivalentByDatasetTemplate(getDatasetTemplate());
    }

    @Override
    public boolean isExtensible() {
        return getContainer() != null && getContainer().isExtensible();
    }

    public boolean isForApplicationRun() {
        return getApplication() != null;
    }

    public boolean isMovable() {
        return getId() == 0 || isDeletable();
    }

    public boolean isPrepared() {
        return prepared || isManaged();
    }

    @Override
    public boolean isReadable() {
        return getContainer() != null && getContainer().isReadable();
    }

    public boolean isRenderedRemoveAttribute(DatasetAttribute attribute) {
        return !isDatasetTemplateFixed() && isUpdatable() && getAttributes().size() > 1;
    }

    public boolean isRenderedRemoveItem() {
        return getItems().size() > 1;
    }

    public boolean isRenderedTree() {
        if (isRenderedTree == null) {
            isRenderedTree = !getSucceedingWorkunits().isEmpty();
        }
        return isRenderedTree;
    }

    public boolean isShowEmptyAttributes() {
        return showEmptyAttributes;
    }

    public boolean isTypeInvalid() {
        return !getTypeInvalidFields().isEmpty();
    }

    @Override
    public boolean isUpdatable() {
        return getId() <= 0 || getContainer() != null && getContainer().isExtensible() && getSucceedingWorkunits().isEmpty() && getWorkflows().isEmpty() && getWorkflowSteps().isEmpty() && (getRun() == null || hasCurrentUserRoleEnum(RoleEnum.LABMANAGER));
    }

    public void keepDatasetTemplateAttributesOnly() {
        if (getDatasetTemplate() != null) {
            DatasetTemplate cachedDatasetTemplate = getDatasetTemplate();
            for (DatasetTemplateAttribute datasetTemplateAttribute : getDatasetTemplate().getDatasetTemplateAttributes()) {
                DatasetAttribute datasetAttribute = getAttribute(datasetTemplateAttribute.getName());
                if (datasetAttribute != null) {
                    datasetAttribute.setPosition(datasetTemplateAttribute.getPosition());
                } else {
                    datasetAttribute = new DatasetAttribute(datasetTemplateAttribute, this);
                    getAttributes().add(datasetAttribute);
                }
            }
            for (DatasetAttribute datasetAttribute : getAttributes()) {
                DatasetTemplateAttribute datasetTemplateAttribute = cachedDatasetTemplate.getAttribute(datasetAttribute.getName());
                if (datasetTemplateAttribute == null) {
                    removeAttribute(datasetAttribute);
                }
            }
            setDatasetTemplate(cachedDatasetTemplate);
        }
    }

    @Override
    protected void preUpdate() {
        // Important: Do not log update using the preUpdate. This is done via the save method of the DatasetService!
        setLogEntity(false);
        super.preUpdate();
    }

    public void prettyPrint() {
        for (DatasetAttribute attribute : getAttributes()) {
            attribute.prettyPrint();
        }
    }

    public void removeAttribute(DatasetAttribute attribute) {
        if (getDatasetTemplate() != null) {
            setDatasetTemplate(null);
        }
        for (DatasetItem item : getItems()) {
            Iterator<DatasetField> iterator = item.getFields().iterator();
            while (iterator.hasNext()) {
                final DatasetField field = iterator.next();
                if (field.getAttribute().equals(attribute)) {
                    field.setAttribute(null);
                    field.setItem(null);
                    iterator.remove();
                }
            }
        }
        getAttributes().remove(attribute);
        resetAttributePositions();
    }

    public void removeDanglingFields() {
        for (DatasetItem item : getItems()) {
            Iterator<DatasetField> iterator = item.getFields().iterator();
            while (iterator.hasNext()) {
                final DatasetField field = iterator.next();
                if (!getAttributes().contains(field.getAttribute())) {
                    field.setAttribute(null);
                    field.setItem(null);
                    iterator.remove();
                }
            }
        }
    }

    public void removeEmptyItems() {
        for (DatasetItem emptyItem : getEmptyItems()) {
            removeItem(emptyItem);
        }
        if (getItems().isEmpty()) {
            addItem();
        }
    }

    public void removeItem(DatasetItem item) {
        for (DatasetAttribute attribute : getAttributes()) {
            for (DatasetField field : item.getFields()) {
                attribute.getFields().remove(field);
            }
        }
        getItems().remove(item);
        resetItemPositions();
    }

    public void reorderAttributePositions() {
        getAttributes().sort(Comparator.comparingInt(DatasetAttribute::getPosition));
    }

    public void resetAttributePositions() {
        int position = 1;
        for (DatasetAttribute attribute : getAttributes()) {
            attribute.setPosition(position);
            position++;
        }
    }

    public void resetContent() {
        if (getDatasetTemplate() != null) {
            setShowEmptyAttributes(true);
            getCheckedVisibleAttributes().clear();
            for (DatasetTemplateAttribute datasetTemplateAttribute : getDatasetTemplate().getDatasetTemplateAttributes()) {
                if (datasetTemplateAttribute != null) {
                    DatasetAttribute datasetAttribute;
                    datasetAttribute = getAttribute(datasetTemplateAttribute.getName());
                    if (datasetAttribute == null) {
                        datasetAttribute = new DatasetAttribute(datasetTemplateAttribute, this);
                        getAttributes().add(datasetAttribute);
                        for (DatasetItem item : getItems()) {
                            item.getFields().add(new DatasetField(item, datasetAttribute, null));
                        }
                    }
                    getCheckedVisibleAttributes().put(datasetAttribute, Boolean.TRUE);
                    datasetAttribute.check();
                }
            }
            Set<DatasetAttribute> attributesToRemove = new HashSet<>();
            for (DatasetAttribute datasetAttribute : getAttributes()) {
                DatasetTemplateAttribute datasetTemplateAttribute = getDatasetTemplate().getAttribute(datasetAttribute.getName());
                if (datasetTemplateAttribute == null) {
                    attributesToRemove.add(datasetAttribute);
                } else {
                    datasetAttribute.setType(datasetTemplateAttribute.getType());
                    datasetAttribute.setPosition(datasetTemplateAttribute.getPosition());
                }
            }
            for (DatasetAttribute datasetAttribute : attributesToRemove) {
                removeAttribute(datasetAttribute);
            }
            reorderAttributePositions();
        }
    }

    public void resetItemPositions() {
        int position = 1;
        for (DatasetItem item : getItems()) {
            item.setPosition(position);
            position++;
        }
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setAttributes(List<DatasetAttribute> attributes) {
        this.attributes = attributes;
    }

    public void setComments(Set<DatasetComment> comments) {
        this.comments = comments;
    }

    public void setContent(List<List<String>> values) throws InvalidDataException {
        clearContent();
        int i = 0;
        List<String> attributesLine = new ArrayList<>();
        for (final List<String> line : values) {
            // System.out.println("line " + i + ": " + line);
            // line.forEach(field -> System.out.println("field: " + field));
            i++;
            if (i == 1) {
                AtomicInteger pos = new AtomicInteger(1);
                line.forEach(field -> getAttributes().add(new DatasetAttribute(this, pos.getAndIncrement(), field, "String")));
                attributesLine.addAll(line);
            } else {
                AtomicInteger pos = new AtomicInteger(0);
                DatasetItem item = new DatasetItem(this);
                try {
                    line.forEach(field -> item.getFields().add(new DatasetField(item, getAttributes().get(pos.getAndIncrement()), field)));
                } catch (Exception e) {
                    throw new InvalidDataException("Error: fields does not match attributes! Fields " + line + " <-> Attributes " + attributesLine);
                }
                getItems().add(item);
            }
        }
        if (getAttributes().isEmpty()) {
            throw new InvalidDataException("Error: dataset has no attributes!");
        }
        if (getItems().isEmpty()) {
            throw new InvalidDataException("Error: dataset is empty, i.e., has no items!");
        }
    }

    public void setDatasetTemplate(DatasetTemplate datasetTemplate) {
        boolean changed = datasetTemplate != null && !datasetTemplate.equals(this.datasetTemplate);
        this.datasetTemplate = datasetTemplate;
        if (changed && !isAttributeEquivalentWith(getDatasetTemplate())) {
            resetContent();
        }
    }

    public void setItems(List<DatasetItem> items) {
        this.items = items;
    }

    public void setMaxNumberOfNewItems(int maxNumberOfNewItems) {
        this.maxNumberOfNewItems = maxNumberOfNewItems;
    }

    public void setNumberOfNewItems(int numberOfNewItems) {
        this.numberOfNewItems = numberOfNewItems;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public void setShowEmptyAttributes(boolean showEmptyAttributes) {
        this.showEmptyAttributes = showEmptyAttributes;
    }

    public void setSucceedingWorkunits(Set<Workunit> succeedingWorkunits) {
        this.succeedingWorkunits = succeedingWorkunits;
    }

    public void setWorkflowSteps(Set<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public void setWorkflows(Set<Workflow> workflows) {
        this.workflows = workflows;
    }

    public void setWorkunit(Workunit workunit) {
        this.workunit = workunit;
    }

    public void showVisibleAttributes() {
        setShowEmptyAttributes(true);
        checkVisibleAttributes();
    }

    public void switchAttributePositions(DatasetAttribute datasetAttribute) {
        if (datasetAttribute != null) {
            switchAttributePositions(datasetAttribute, getNextDatasetAttribute(datasetAttribute));
        }
    }

    public void switchAttributePositions(DatasetAttribute attribute1, DatasetAttribute attribute2) {
        if (attribute1 != null && attribute2 != null) {
            int listPosition1 = getAttributes().indexOf(attribute1);
            int listPosition2 = getAttributes().indexOf(attribute2);
            if (listPosition1 >= 0 && listPosition2 >= 0) {
                int temp = attribute2.getPosition();
                attribute2.setPosition(attribute1.getPosition());
                attribute1.setPosition(temp);
                getAttributes().set(listPosition2, attribute1);
                getAttributes().set(listPosition1, attribute2);
                for (DatasetItem item : getItems()) {
                    DatasetField tempField = item.getFields().get(listPosition2);
                    item.getFields().set(listPosition2, item.getFields().get(listPosition1));
                    item.getFields().set(listPosition1, tempField);
                }
            }
        }
    }

    public void typeCheck() {
        typeInvalidFields = new HashSet<>();
        entityNotFoundFields = new HashSet<>();
        for (DatasetItem item : getItems()) {
            for (DatasetField field : item.getFields()) {
                if (field.isTypeInvalid()) {
                    typeInvalidFields.add(field);
                } else if (field.isEntityReference() && field.isEntityNotFound()) {
                    entityNotFoundFields.add(field);
                }
            }
        }
    }

    public void uncheckAllAttributes() {
        for (final DatasetAttribute attribute : getAttributes()) {
            if (!attribute.isRequired()) {
                attribute.uncheck();
            }
        }
        checkVisibleAttributes();
    }

    public void uncheckAllItems() {
        for (final DatasetItem item : getItems()) {
            item.uncheck();
        }
    }

    public String wrapTD(String value) {
        return "<td>" + (value != null ? value : "") + "</td>";
    }

    public String wrapTH(String value) {
        return "<th>" + (value != null ? value : "") + "</th>";
    }

    public String wrapTR(String value) {
        return "<tr>" + (value != null ? value : "") + "</tr>";
    }

    public String wrapTable(String value) {
        return "<table>" + (value != null ? value : "") + "</table>";
    }
}