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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Application;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.JsonService;
import org.bfabric.service.ResourceBasketService;
import org.bfabric.util.CollectionHelper;
import org.primefaces.component.datatable.DataTable;

@MeasureCalls
@Named
@ViewScoped
public class ResourceBasketManager extends AbstractEntityManager<ResourceBasket> {

    private static final long serialVersionUID = 1;

    @Inject
    private JsonService jsonService;

    @Inject
    private ResourceBasketService resourceBasketService;

    public ResourceBasketManager() {
        super(ResourceBasket.class);
    }

    public void createBasketFromSelectedResourcesAsJnlp() {
        ResourceBasket basketFromSelectedResources = resourceBasketService.saveBasketFromSelectedResources(getResourceBasket(), getSelectedResources());
        HashMap<String, String> fParams = new HashMap<>();
        fParams.put("resourceBasketId", String.valueOf(basketFromSelectedResources.getId()));
        redirectRelative(createRedirectURL("/fragments", true, "download-jnlp", null, null, fParams));
    }

    public String createBasketFromSelectedResourcesAsJson() {
        ResourceBasket basketFromSelectedResources = resourceBasketService.saveBasketFromSelectedResources(getResourceBasket(), getSelectedResources());
        HashMap<String, String> fParams = new HashMap<>();
        fParams.put("entity", ResourceBasket.class.getSimpleName() + " " + basketFromSelectedResources.getId());
        fParams.put("resourceBasketId", String.valueOf(basketFromSelectedResources.getId()));
        return jsonService.getJson(fParams);
    }

    public String createDatasetFromSelectedResources() {
        ResourceBasket basketFromSelectedResources = resourceBasketService.saveBasketFromSelectedResources(getResourceBasket(), getSelectedResources());
        HashMap<String, String> fParams = new HashMap<>();
        fParams.put("resourceBasketId", String.valueOf(basketFromSelectedResources.getId()));
        fParams.put("containerId", String.valueOf(getContextContainer().getId()));
        return createRedirectURL("dataset/edit", null, null, fParams);
    }

    public List<Application> getApplications() {
        return resourceBasketService.getApplications(isAllSelectedResourcesAvailable(), getSelectedResources());
    }

    public String getApplicationsNames() {
        return CollectionHelper.printDisplayNames(getApplications());
    }

    @Produces
    @Named("resourceBasket")
    public ResourceBasket getResourceBasket() {
        return getInstance();
    }

    public int getResourceBasketLimit() {
        return getConfiguration().getResourceBasketLimit();
    }

    public Set<Resource> getSelectedResources() {
        Set<Resource> ret = new HashSet<>();
        for (Resource resource : getResourceBasket().getResources()) {
            if (resource.isChecked()) {
                ret.add(resource);
            }
        }
        return ret;
    }

    @Override
    @PostConstruct
    public void init() {
        if (getCurrentUser() != null) {
            if (getCurrentUser().getSelectedResourceBasket() != null) {
                setInstance(getCurrentUser().getSelectedResourceBasket());
            } else {
                setInstance(resourceBasketService.createResourceBasketForUser(getCurrentUser()));
                id = getInstance().getIdString();
            }
            for (Resource resource : getInstance().getResources()) {
                resource.setChecked(false);
            }
        }
        super.init();
    }

    public boolean isAllSelectedResourcesAvailable() {
        boolean allSelectedResourcesAvailable = !getSelectedResources().isEmpty();
        for (Resource resource : getSelectedResources()) {
            if (!resource.isAvailable()) {
                allSelectedResourcesAvailable = false;
            }
        }
        return allSelectedResourcesAvailable;
    }

    public boolean isDownloadButtonRendered() {
        return Resource.isDownloadButtonRendered(getSelectedResources());
    }

    public boolean isDownloadManagerDownloadButtonRendered() {
        return Resource.isDownloadManagerDownloadButtonRendered(getSelectedResources());
    }

    public String remove(Resource resource) {
        resourceBasketService.removeResourceFromBasket(getResourceBasket(), resource);
        getFacesMessagesManager().printWarn(Messages.get("successfullyRemoved"));
        return getShowScreenRedirectURL();
    }

    public String removeAll() {
        if (UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID) != null) {
            DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
                .findComponent(String.valueOf(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get(Constants.TABLE_CLIENT_ID)));
            List<Resource> resources = dataTableHelper.getDataTableValues(dataTable);
            resourceBasketService.removeResourcesFromBasket(getResourceBasket(), resources);
        } else {
            resourceBasketService.removeResourcesFromBasket(getResourceBasket());
        }
        getFacesMessagesManager().bufferWarning(Messages.get("successfullyRemoved"));
        return getShowScreenRedirectURL();
    }

    public String runApplication(Long applicationId) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("selectedResources", getSelectedResources());
        HashMap<String, String> fParams = new HashMap<>();
        fParams.put("containerId", String.valueOf(getContextContainer().getId()));
        fParams.put("applicationId", String.valueOf(applicationId));
        fParams.put("creationType", Constants.CREATION_FROM_INPUT_RESOURCES);
        return createRedirectURL("workunit/edit", null, null, fParams);
    }
}