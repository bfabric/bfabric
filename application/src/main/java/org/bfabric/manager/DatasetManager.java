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

package org.bfabric.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Application;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetAttribute;
import org.bfabric.entity.DatasetField;
import org.bfabric.entity.DatasetItem;
import org.bfabric.entity.DatasetTemplate;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.entity.Workunit;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.DatasetService;
import org.bfabric.service.DatasetTemplateService;
import org.bfabric.util.StringHelper;
import org.bfabric.xml.XmlHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class DatasetManager extends AbstractContainerDependentEntityManager<Dataset> {

    private static final Logger logger = Logger.getLogger(DatasetManager.class.getName());

    private static final long serialVersionUID = 1;

    @Param
    protected Long applicationId;

    @Param
    protected Long datasetTemplateId;

    @Inject
    private DatasetTemplateService DatasetTemplateService;

    @Inject
    private DatasetService datasetService;

    @Param
    private Boolean fromExistingData;

    private Set<Workunit> markedWorkunits = new HashSet<>();

    // Helper variable for redirection if prepared is true
    private String originalRefererURL;

    private ResourceBasket resourceBasket;

    @Param
    private Long resourceBasketId;

    private Set<Workunit> selectedWorkunits = new HashSet<>();

    private List<Workunit> workunitList = new ArrayList<>();

    public DatasetManager() {
        super(Dataset.class);
    }

    public void attributeNameChanged(ValueChangeEvent event) {
        final int index = (int) ((UIComponent) event.getSource()).getAttributes().get("index");

        for (final DatasetAttribute attribute : getDataset().getAttributes()) {
            if (attribute.getName().equalsIgnoreCase(event.getNewValue().toString())) {
                getFacesMessagesManager().printError(Messages.get("validationErrorColumnsSameName"));
                getFacesMessagesManager().validationError("edit:datasetItems:attribute" + index, Messages.get("nameNotUniqueException"));
                break;
            }
        }
    }

    @Override
    protected Dataset createInstance() {
        final Dataset dataset = super.createInstance();
        if (getContainerId() != null) {
            Container container = entityService.find(Container.class, getContainerId());
            if (container != null && container.isExtensible()) {
                dataset.setContainer(container);
            }
        } else if (getContextContainer() != null && getContextContainer().isExtensible()) {
            dataset.setContainer(entityService.find(Container.class, getContextContainer().getId()));
        }
        if (isFromResourceBasket()) {
            resourceBasket = entityService.find(ResourceBasket.class, resourceBasketId);
            if (resourceBasket != null) {
                dataset.createDatasetItems(resourceBasket.getResources());
                next();
            }
        } else {
            dataset.setPrepared(getFromExistingData() != null && !isFromExistingData());
            if (datasetTemplateId != null) {
                dataset.setDatasetTemplate(entityService.find(DatasetTemplate.class, datasetTemplateId));
            } else {
                dataset.initializeContent();
            }
        }
        if (applicationId != null) {
            dataset.setApplication(entityService.find(Application.class, applicationId));
            dataset.setName(dataset.getApplication().getName() + " " + Constants.DATETIME_FORMATTER_NAME.format(LocalDateTime.now()));
        }
        return dataset;
    }

    public void deselectWorkunits(boolean deselectAll) {
        if (deselectAll) {
            getSelectedWorkunits().clear();
        } else {
            final Set<Workunit> newSelectedWorkunits = new HashSet<>();
            for (final Workunit workunit : getSelectedWorkunits()) {
                if (!workunit.isChecked()) {
                    newSelectedWorkunits.add(workunit);
                } else {
                    workunit.setChecked(false);
                }
            }
            setSelectedWorkunits(newSelectedWorkunits);
        }

        if (getSelectedWorkunits().isEmpty()) {
            getDataset().setShowEmptyAttributes(false);
        }

        // Recompute the list of dataset items after a workunit has been added or removed.
        recomputeDatasetItems();
    }

    @Produces
    @Named("dataset")
    public Dataset getDataset() {
        return getInstance();
    }

    public Long getDatasetTemplateId() {
        return datasetTemplateId;
    }

    public List<DatasetTemplate> getEnabledDatasetTemplates(String filterString) {
        return DatasetTemplateService.getFilteredEnabledIncludingOrderBy(getDataset().getDatasetTemplate(), filterString);
    }

    public Boolean getFromExistingData() {
        return fromExistingData;
    }

    public Set<Workunit> getMarkedWorkunits() {
        return markedWorkunits;
    }

    @Override
    public String getRedirectURLAfterCancel() {
        String ret;
        if (isCloned()) {
            ret = createRedirectShowScreenURL(Dataset.class.getSimpleName(), getClonedId(), null, null);
        } else if (isManaged()) {
            ret = getShowScreenRedirectURL();
        } else {
            if (getDataset().isPrepared()) {
                setRefererURL(originalRefererURL);
            }
            return getRedirectURLFromRefererUrl();
        }
        return ret;
    }

    public Set<Workunit> getSelectedWorkunits() {
        return selectedWorkunits;
    }

    public List<Workunit> getWorkunitList() {
        return workunitList;
    }

    public boolean isFromExistingData() {
        return fromExistingData != null && fromExistingData;
    }

    public boolean isFromResourceBasket() {
        return resourceBasketId != null;
    }

    public boolean isInitScreen() {
        return fromExistingData == null;
    }

    @Override
    public Dataset loadInstance() {
        Dataset dataset = super.loadInstance();
        if (dataset != null) {
            dataset.setOldStateAsXml();
            logger.fine("--- loadInstance " + dataset + " " + XmlHelper.getXmlLog(dataset.getOldStateAsXml()));
        }
        return dataset;
    }

    public void markWorkunit(Workunit workunit) {
        if (getMarkedWorkunits().contains(workunit)) {
            getMarkedWorkunits().remove(workunit);
        } else {
            getMarkedWorkunits().add(workunit);
        }
    }

    public void next() {
        originalRefererURL = getRefererURL();
        if (isFromExistingData() || isFromResourceBasket()) {
            final List<DatasetItem> allItems = new ArrayList<>(getDataset().getItems());
            final List<DatasetAttribute> allAttributes = new ArrayList<>(getDataset().getAttributes());
            for (final DatasetAttribute attribute : allAttributes) {
                if (!attribute.isChecked()) {
                    getDataset().getAttributes().remove(attribute);
                }
            }
            for (final DatasetItem item : allItems) {
                if (!item.isChecked()) {
                    getDataset().getItems().remove(item);
                } else {
                    final List<DatasetField> fieldsToRemove = new ArrayList<>();
                    for (final DatasetField field : item.getFields()) {
                        if (!getDataset().getAttributes().contains(field.getAttribute())) {
                            fieldsToRemove.add(field);
                        }
                    }
                    item.getFields().removeAll(fieldsToRemove);
                }
            }
        } else {
            getDataset().initializeContent();
        }
        if (StringHelper.isEmpty(getDataset().getName())) {
            getDataset().setName(Constants.DATETIME_FORMATTER_NAME.format(LocalDateTime.now()));
        }
        getDataset().setPrepared(true);
    }

    private void recomputeDatasetItems() {
        boolean checkedAllAttributes = getDataset().getAttributes().stream().allMatch(i -> !i.isChecked()) || getDataset().getAttributes().stream().allMatch(AbstractEntity::isChecked);
        boolean checkedAllItems = getDataset().getItems().stream().allMatch(i -> !i.isChecked()) || getDataset().getItems().stream().allMatch(AbstractEntity::isChecked);

        // Determine the selected items.
        final List<DatasetItem> selectedItems = new ArrayList<>();
        final Set<Long> selectedResources = new HashSet<>();
        for (final DatasetItem item : getDataset().getItems()) {
            if (item.isChecked()) {
                selectedItems.add(item);
                for (final Resource resource : item.getResources()) {
                    selectedResources.add(resource.getId());
                }
            }
        }

        // Retain the selected items, i.e., clear all items except the selected ones.
        final List<DatasetItem> currentItems = new ArrayList<>(getDataset().getItems());
        for (final DatasetItem item : currentItems) {
            if (!selectedItems.contains(item)) {
                getDataset().getItems().remove(item);
            }
        }

        // Add all the items that were selected which are not already in the selected list.
        for (final Workunit workunit : getSelectedWorkunits()) {
            if (workunit.getResources() != null) {
                for (final Resource resource : workunit.getResources()) {
                    if (!selectedResources.contains(resource.getId())) {
                        getDataset().createDatasetItem(resource);
                    }
                }
            }
        }

        getDataset().checkVisibleAttributes();
        if (checkedAllAttributes) {
            getDataset().checkAllAttributes();
        }
        if (checkedAllItems) {
            getDataset().checkAllItems();
        }
    }

    @Override
    public String remove() {
        // Detach the dataset object from the attached workunit before removal.
        if (getDataset().getWorkunit() != null) {
            getDataset().getWorkunit().setDataset(null);
            getDataset().setWorkunit(null);
        }

        return super.remove();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = datasetService.isValid(getDataset());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!getDataset().isManaged());
            datasetService.save(getDataset(), resourceBasket);
            getContextManager().setContextContainer(getDataset().getContainer());
            if (isCreated() && getDataset().getApplication() != null) {
                getFacesMessagesManager().clearGlobalMessages();
                getFacesMessagesManager().bufferWarning(Messages.get("newlyCreatedInputDatasetPresetForRunningApplication")
                    .replace("{0}", getDataset().getName()) + " " + getDataset().getApplication().getDisplayName());
                return "/workunit/edit.xhtml?creationType=" + Constants.CREATION_FROM_INPUT_DATASET + "&applicationId=" + applicationId + "&selectedDatasetId=" + getDataset().getId() + "&faces-redirect=true";
            }
            return postSave(true, false);
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void selectWorkunits() {
        for (final Workunit workunit : getMarkedWorkunits()) {
            if (!getSelectedWorkunits().contains(workunit)) {
                getSelectedWorkunits().add(workunit);
                workunit.check();
            }
        }

        getMarkedWorkunits().clear();

        if (getSelectedWorkunits().isEmpty()) {
            getDataset().setShowEmptyAttributes(false);
        }

        // Recompute the list of dataset items after a workunit has been added or removed.
        recomputeDatasetItems();
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    public void setFromExistingData(boolean fromExistingData) {
        this.fromExistingData = fromExistingData;
    }

    public void setMarkedWorkunits(Set<Workunit> markedWorkunits) {
        this.markedWorkunits = markedWorkunits;
    }

    public void setSelectedWorkunits(Set<Workunit> selectedWorkunits) {
        this.selectedWorkunits = selectedWorkunits;
    }

    public void setWorkunitList(List<Workunit> workunitList) {
        this.workunitList = workunitList;
    }

    public void typeCheck() {
        getInstance().typeCheck();
        if (getInstance().getTypeInvalidFields().isEmpty()) {
            getFacesMessagesManager().printWarn(Messages.get("typeCheckSuccessful"));
        } else {
            for (DatasetField field : getInstance().getTypeInvalidFields()) {
                getFacesMessagesManager().validationError("edit:datasetItems:" + (field.getItem().getPosition() - 1) + ":field_" + (field.getAttribute()
                    .getPosition() - 1), Messages.get("invalid"));
            }
            getFacesMessagesManager().printError(Messages.get("fieldTypeCheckException"));
        }
    }
}