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

import java.util.LinkedHashMap;

import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetTemplate;
import org.bfabric.entity.DatasetTemplateAttribute;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.DatasetService;
import org.bfabric.service.DatasetTemplateService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class DatasetTemplateManager extends AbstractEntityManager<DatasetTemplate> {

    private static final long serialVersionUID = 1;

    private Dataset dataset;

    @Param
    private Long datasetId;

    @Inject
    private DatasetService datasetService;

    @Inject
    private DatasetTemplateService datasetTemplateService;

    private boolean resetAndSaveDataset;

    @Param
    private Boolean resetDataset;

    public DatasetTemplateManager() {
        super(DatasetTemplate.class);
    }

    public void attributeNameChanged(ValueChangeEvent event) {
        final int index = (int) ((UIComponent) event.getSource()).getAttributes().get("index");
        for (final DatasetTemplateAttribute attribute : getDatasetTemplate().getDatasetTemplateAttributes()) {
            if (attribute.getName().equalsIgnoreCase(event.getNewValue().toString())) {
                getFacesMessagesManager().printError(Messages.get("validationErrorColumnsSameName"));
                getFacesMessagesManager().validationError("edit:datasetTemplateAttributes:attribute" + index, Messages.get("nameNotUniqueException"));
                break;
            }
        }
    }

    @Override
    protected DatasetTemplate createInstance() {
        DatasetTemplate datasetTemplate = super.createInstance();
        if (datasetId != null) {
            dataset = entityService.find(Dataset.class, datasetId);
            if (dataset != null) {
                datasetTemplate = dataset.getDatasetTemplateDerived();
                datasetTemplate.setName(dataset.getName());
                if (resetDataset != null && resetDataset) {
                    resetAndSaveDataset = true;
                }
            }
        }
        datasetTemplate.setSupervisor(getCurrentUser());
        return datasetTemplate;
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Produces
    @Named("datasetTemplate")
    public DatasetTemplate getDatasetTemplate() {
        return getInstance();
    }

    public boolean isResetAndSaveDataset() {
        return resetAndSaveDataset;
    }

    @Override
    public DatasetTemplate loadInstance() {
        DatasetTemplate datasetTemplate = super.loadInstance();
        if (datasetTemplate != null) {
            datasetTemplate.setOldStateAsXml();
        }
        return datasetTemplate;
    }

    @Override
    public String remove() {
        datasetTemplateService.remove(getDatasetTemplate());
        return getRedirectURLAfterRemove();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = datasetTemplateService.isValid(getDatasetTemplate());
        if (validationErrorMsg.isEmpty()) {
            datasetTemplateService.save(getDatasetTemplate());
            if (resetAndSaveDataset) {
                dataset = entityService.find(Dataset.class, datasetId);
                if (dataset != null && dataset.isCompatibleWith(getDatasetTemplate())) {
                    dataset.setOldStateAsXml();
                    dataset.setDatasetTemplate(getDatasetTemplate());
                    datasetService.save(dataset);
                }
            }
            return postSave(true, false);
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void setResetAndSaveDataset(boolean resetAndSaveDataset) {
        this.resetAndSaveDataset = resetAndSaveDataset;
    }

    public String switchEnabled() {
        getDatasetTemplate().switchEnabled();
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(getDatasetTemplate().isEnabled() ? Messages.get("enabled") : Messages.get("disabled"));
        return getShowScreenRedirectURL();
    }
}
