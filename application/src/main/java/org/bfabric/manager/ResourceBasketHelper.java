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
import java.util.Collection;
import java.util.logging.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.entity.Workunit;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.ResourceList;
import org.bfabric.service.EntityService;
import org.bfabric.service.ResourceBasketService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.ConfigurationHelper;

@MeasureCalls
@Named
@ViewScoped
public class ResourceBasketHelper implements Serializable {

    private static final Logger logger = Logger.getLogger(ResourceBasketHelper.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    protected EntityService entityService;

    @Inject
    protected FacesMessagesManager facesMessagesManager;

    @Inject
    private ResourceBasketService resourceBasketService;

    @Inject
    private ResourceList resourceList;

    public ResourceBasketHelper() {
        super();
    }

    public void addBasketLimitMessage() {
        facesMessagesManager.clearGlobalMessages();
        facesMessagesManager.printWarn(Messages.get("resourcesLimitExceeds").replace("{0}", Integer.toString(getResourceBasketLimit())));
    }

    public void addResourceToBasket(ResourceBasket resourceBasket, Long resourceId) {
        addResourceToBasket(resourceBasket, entityService.find(Resource.class, resourceId));
    }

    public void addResourceToBasket(ResourceBasket resourceBasket, Resource resource) {
        logger.fine("addResourceToBasket " + resourceBasket + " " + resource);
        StringBuilder msg = new StringBuilder();
        if (resource != null && resourceBasket != null) {
            if (resourceBasket.getResources().contains(resource)) {
                msg.append(Messages.get("resourceAlreadyInBasket").replace("{0}", Long.toString(resource.getId())));
            } else if (resourceBasket.getResources().size() >= getResourceBasketLimit()) {
                msg.append(Messages.get("resourcesLimitExceeds").replace("{0}", Integer.toString(getResourceBasketLimit())));
            } else {
                resourceBasket.getResources().add(resource);
                resourceBasketService.addResourceToBasket(resource, resourceBasket.getId());
                msg.append(Messages.get("resourceAddedToBasket").replace("{0}", Long.toString(resource.getId())));
            }
            facesMessagesManager.printWarn(msg.toString());
        }
    }

    public void addResourcesToBasket(ResourceBasket resourceBasket, BfabricLazyDataModel<Resource> lazyDataModel) {
        if (lazyDataModel != null) {
            if (lazyDataModel.getFilteredValueCount() > getResourceBasketLimit()) {
                addBasketLimitMessage();
            } else {
                addResourcesToBasket(resourceBasket, lazyDataModel.getFilteredValue());
            }
        }
    }

    public void addResourcesToBasket(ResourceBasket resourceBasket, Collection<Resource> resources) {
        if (resourceBasket != null && resources != null && !resources.isEmpty()) {
            logger.fine("addResourcesToBasket " + resourceBasket + "  resources=" + resources.size());
            if (resources.size() > getResourceBasketLimit()) {
                addBasketLimitMessage();
            } else {
                int resourceBasketSizeBefore = resourceBasket.getResources().size();
                resourceBasket.addResources(resources);
                int resourceBasketSizeAfter = resourceBasket.getResources().size();
                int addedResourcesSize = resourceBasketSizeAfter - resourceBasketSizeBefore;
                int duplicateResourcesSize = resourceBasketSizeBefore + resources.size() - resourceBasketSizeAfter;
                if (resourceBasketSizeAfter > getResourceBasketLimit()) {
                    addBasketLimitMessage();
                } else {
                    resourceBasketService.addResourcesToBasket(resources, resourceBasket.getId());
                    StringBuilder msg = new StringBuilder();
                    if (addedResourcesSize > 0) {
                        msg.append(Messages.get("resourcesAddedToBasket")).append(": ").append(addedResourcesSize);
                    }
                    if (duplicateResourcesSize > 0) {
                        msg.append("; ").append(Messages.get("alreadyIncluded")).append(": ").append(Long.valueOf(duplicateResourcesSize));
                    }
                    facesMessagesManager.printWarn(msg.toString());
                }
            }
        } else {
            facesMessagesManager.printWarn(Messages.get("noResourcesAddBasket"));
        }
    }

    public void addWorkunitResourcesToBasket(ResourceBasket resourceBasket, Long workunitId) {
        logger.fine("addWorkunitResourcesToBasket " + resourceBasket + "  workunitId=" + workunitId);
        addResourcesToBasket(resourceBasket, resourceList.getResourcesByWorkunitId(workunitId));
    }

    public int getResourceBasketLimit() {
        return ConfigurationHelper.getConfiguration().getResourceBasketLimit();
    }

    public boolean isAddResourceIdToBasketDisabled(ResourceBasket resourceBasket, String resourceId) {
        try {
            return isAddResourceToBasketDisabled(resourceBasket, (Resource) entityService.fetch(Resource.class, Long.valueOf(resourceId)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isAddResourceToBasketDisabled(ResourceBasket resourceBasket, Resource resource) {
        // logger.fine("isAddResourceToBasketDisabled " + resourceBasket + "  " + resource);
        // TODO: This will not perform for big resource baskets. Better create a named query that delivers single value without loading any of the potentially hundreds of resources.
        return resourceBasket == null || resource == null || resourceBasket.getResources().contains(resource);
    }

    public boolean isAddWorkunitIdToBasketDisabled(ResourceBasket resourceBasket, String workunitId) {
        boolean ret = true;
        try {
            ret = isAddWorkunitToBasketDisabled(resourceBasket, (Workunit) entityService.fetch(Workunit.class, Long.valueOf(workunitId)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean isAddWorkunitToBasketDisabled(ResourceBasket resourceBasket, Workunit workunit) {
        // logger.fine("isAddWorkunitToBasketDisabled " + resourceBasket + "  " + workunit);
        // TODO: This will not perform for big resource baskets. Better create a named query that delivers single value without loading any of the potentially hundreds of resources.
        return resourceBasket == null || workunit == null || resourceBasket.getResources().containsAll(workunit.getResources());
    }
}