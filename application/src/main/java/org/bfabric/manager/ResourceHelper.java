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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractContainerResource;
import org.bfabric.entity.Application;
import org.bfabric.entity.ImportResource;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Sample;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ResourceService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.DataTableHelper;
import org.bfabric.util.StringHelper;
import org.primefaces.component.datatable.DataTable;

@MeasureCalls
@Named
@ViewScoped
public class ResourceHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private final Set<ImportResource> markedImportResources = new HashSet<>();

    private final Set<Resource> markedInputResources = new HashSet<>();

    private final Set<ImportResource> selectedImportResources = new HashSet<>();

    private final Set<Resource> selectedInputResources = new HashSet<>();

    private boolean basketSelection;

    @Inject
    private DataTableHelper dataTableHelper;

    @Inject
    private IdentityManager identityManager;

    private boolean imported;

    private Map<ImportResource, Boolean> markedSelectImportResourcesMap = new HashMap<>();

    @Inject
    private ResourceService resourceService;

    private Sample sampleForAllImportResources;

    private Sample sampleForAllResources;

    private AbstractContainerResource selectedResource;

    public ResourceHelper() {
        super();
    }

    public void checkAll(boolean check) {
        if (UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID) != null) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
                .findComponent(String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID)));

            List<ImportResource> importResources = dataTableHelper.getDataTableValuesPageOnly(dataTable);
            for (ImportResource importResource : importResources) {
                importResource.setChecked(check);
                markImportResource(importResource);
                if (dataTable.isLazy() && !getSelectedImportResources().contains(importResource)) {
                    // The datatable is lazy, i.e, it is the selection table.
                    if (check) {
                        getMarkedSelectImportResourcesMap().put(importResource, Boolean.TRUE);
                    } else {
                        getMarkedSelectImportResourcesMap().remove(importResource);
                    }
                }
            }
            dataTableHelper.updateColumn(dataTable.getClientId(), Constants.SELECT_CHECK_BOX, false);
        }
    }

    public void clearSelectedResource(boolean importResource) {
        setSelectedResource(null);
        setImported(importResource);
    }

    public void deselectAllSamplesFromImportResources() {
        for (final ImportResource importResource : getSelectedImportResources()) {
            importResource.setSample(null);
        }
    }

    public void deselectImportResources(boolean deselectAll) {
        if (deselectAll) {
            for (final ImportResource importResource : getMarkedImportResources()) {
                importResource.setChecked(false);
            }
            getSelectedImportResources().clear();
        } else {
            for (final ImportResource importResource : getMarkedImportResources()) {
                importResource.setChecked(false);
                getSelectedImportResources().remove(importResource);
            }
        }
        getMarkedImportResources().clear();
    }

    public void deselectInputResources(boolean deselectAll) {
        if (deselectAll) {
            for (final Resource inputResource : getMarkedInputResources()) {
                inputResource.setChecked(false);
            }
            getSelectedInputResources().clear();
        } else {
            for (final Resource inputResource : getMarkedInputResources()) {
                inputResource.setChecked(false);
                getSelectedInputResources().remove(inputResource);
            }
        }

        getMarkedInputResources().clear();
    }

    private Sample findMatchingSample(Set<Sample> samples, String fileName) {
        return samples.stream()
            .filter(sample -> sample.isIdContainedInFileName(fileName))
            .findFirst()
            .orElseGet(() -> samples.stream()
                .filter(sample -> sample.isIdPotentiallyContainedInFileName(fileName))
                .findFirst()
                .orElseGet(() -> samples.stream()
                    .max((s1, s2) -> Integer.compare(
                        StringHelper.containsPartly(fileName, s1.getName(), StringHelper.MIN_SUBSTRING_LENGTH),
                        StringHelper.containsPartly(fileName, s2.getName(), StringHelper.MIN_SUBSTRING_LENGTH)))
                    .orElse(null)));
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Resource> getAvailableResources(Application application) {
        return resourceService
            .getAvailableResourcesByApplicationAndUser(application, identityManager.getCurrentUser(), isBasketSelection(), identityManager.hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER));
    }

    public Set<ImportResource> getMarkedImportResources() {
        return markedImportResources;
    }

    public Set<Resource> getMarkedInputResources() {
        return markedInputResources;
    }

    public Map<ImportResource, Boolean> getMarkedSelectImportResourcesMap() {
        return markedSelectImportResourcesMap;
    }

    public Sample getSampleForAllImportResources() {
        return sampleForAllImportResources;
    }

    public Sample getSampleForAllResources() {
        return sampleForAllResources;
    }

    public Set<ImportResource> getSelectedImportResources() {
        return selectedImportResources;
    }

    public Set<Resource> getSelectedInputResources() {
        return selectedInputResources;
    }

    public AbstractContainerResource getSelectedResource() {
        return selectedResource;
    }

    public boolean isBasketSelection() {
        return basketSelection;
    }

    public boolean isImported() {
        return imported;
    }

    public boolean isInputResourcesSelected() {
        return !selectedInputResources.isEmpty();
    }

    public void makeSamplesProposal(Set<Sample> samples) {
        for (final ImportResource importResource : getSelectedImportResources()) {
            if (importResource.getSample() == null) {
                importResource.setSample(findMatchingSample(samples, importResource.getFileName()));
            }
        }
    }

    public void markImportResource(ImportResource importResource, boolean isSelect) {
        markImportResource(importResource);
        if (isSelect) {
            getMarkedSelectImportResourcesMap().put(importResource, Boolean.TRUE);
        } else {
            getMarkedSelectImportResourcesMap().remove(importResource);
        }
    }

    public void markImportResource(ImportResource importResource) {
        if (getMarkedImportResources().contains(importResource)) {
            getMarkedImportResources().remove(importResource);
        } else {
            getMarkedImportResources().add(importResource);
        }
    }

    public void markImportResources(String tableClientId, String tableClientId2) {
        if (tableClientId != null) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableClientId);
            List<ImportResource> dataList = dataTableHelper.getDataTableValuesPageOnly(dataTable);
            for (ImportResource importResource : dataList) {
                getMarkedImportResources().add(importResource);
            }
            selectImportResources();
            dataTableHelper.clear(tableClientId2);
        }
    }

    public void markInputResource(Resource inputResource) {
        if (getMarkedInputResources().contains(inputResource)) {
            getMarkedInputResources().remove(inputResource);
        } else {
            getMarkedInputResources().add(inputResource);
        }
    }

    public void markInputResources(String tableClientId, String tableClientId2) {
        if (tableClientId != null) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableClientId);
            List<Resource> dataList = dataTableHelper.getDataTableValuesPageOnly(dataTable);
            for (Resource resource : dataList) {
                getMarkedInputResources().add(resource);
            }
            selectInputResources();
            dataTableHelper.clear(tableClientId2);
        }
    }

    public void markWorkunitResources(String tableClientId, String tableClientId2) {
        if (tableClientId != null) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(tableClientId);
            List<Resource> dataList = dataTableHelper.getDataTableValuesPageOnly(dataTable);
            for (Resource resource : dataList) {
                if (resource.isChecked() && !getSelectedInputResources().contains(resource)) {
                    getMarkedInputResources().addAll(resource.getWorkunit().getResources());
                }
            }
            selectInputResources();
            dataTableHelper.clear(tableClientId2);
        }
    }

    public void selectImportResources() {
        for (final ImportResource importResource : getMarkedImportResources()) {
            importResource.setChecked(false);
            getSelectedImportResources().add(importResource);
            getMarkedSelectImportResourcesMap().clear();
        }
        getMarkedImportResources().clear();
    }

    public void selectInputResources() {
        for (final Resource inputResource : getMarkedInputResources()) {
            inputResource.setChecked(false);
            getSelectedInputResources().add(inputResource);
        }
        getMarkedInputResources().clear();
    }

    public void setBasketSelection(boolean basketSelection) {
        this.basketSelection = basketSelection;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public void setMarkedSelectImportResourcesMap(Map<ImportResource, Boolean> markedSelectImportResourcesMap) {
        this.markedSelectImportResourcesMap = markedSelectImportResourcesMap;
    }

    public void setSampleForAllImportResources(Sample sampleForAllImportResources) {
        this.sampleForAllImportResources = sampleForAllImportResources;
    }

    public void setSampleForAllResources(Sample sampleForAllResources) {
        this.sampleForAllResources = sampleForAllResources;
    }

    public void setSelectedResource(AbstractContainerResource selectedResource) {
        this.selectedResource = selectedResource;
    }

    public void setValueForAllImportResources() {
        for (ImportResource importResource : getSelectedImportResources()) {
            importResource.setSample(sampleForAllImportResources);
        }
    }

    public void setValueForAllResources(Set<Resource> resources) {
        if (resources != null) {
            for (Resource resource : resources) {
                resource.setSample(sampleForAllResources);
            }
        }
    }

    public void setValueForSelectedResources(Sample sample, Set<Resource> resources) {
        if (isImported()) {
            setSampleForAllImportResources(sample);
            setValueForAllImportResources();
        } else {
            setSampleForAllResources(sample);
            setValueForAllResources(resources);
        }
    }

    public void switchSelectionTable() {
        basketSelection = !basketSelection;
    }
}