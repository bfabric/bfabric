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

package org.bfabric.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Application;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.util.CollectionHelper;

@Named
@Stateless
public class ResourceBasketService extends AbstractService {

    private static final long serialVersionUID = 1;

    public ResourceBasketService() {
        super(ResourceBasket.class);
    }

    public void addResourceToBasket(Resource resource, Long resourceBasketId) {
        if (resource != null && resourceBasketId != null) {
            ResourceBasket resourceBasket = find(ResourceBasket.class, resourceBasketId);
            resourceBasket.getResources().add(resource);
            merge(resourceBasket);
        }
    }

    public void addResourcesToBasket(Collection<Resource> resources, Long resourceBasketId) {
        if (resources != null && resourceBasketId != null) {
            ResourceBasket resourceBasket = find(ResourceBasket.class, resourceBasketId);
            resourceBasket.getResources().addAll(resources);
            merge(resourceBasket);
        }
    }

    public ResourceBasket createResourceBasketForUser(User user) {
        ResourceBasket resourceBasket = null;
        if (user != null && user.getSelectedResourceBasket() == null) {
            resourceBasket = new ResourceBasket();
            resourceBasket.setName(Messages.get("configureDefaultBasketName"));
            persist(resourceBasket);
            user.setSetModifiedEnabled(false);
            user.setSelectedResourceBasket(resourceBasket);
            user.getResourceBaskets().add(resourceBasket);
            merge(user);
        }
        return resourceBasket;
    }

    public List<Application> getApplications(boolean isAllSelectedResourcesAvailable, Set<Resource> selectedResources) {
        Set<Application> succeedingApplications = new HashSet<>();
        Set<Workunit> workunits = new HashSet<>();
        if (isAllSelectedResourcesAvailable) {
            boolean first = true;
            for (Resource resource : selectedResources) {
                if (first) {
                    succeedingApplications.addAll(resource.getWorkunit().getRunnableApplications());
                    workunits.add(resource.getWorkunit());
                    first = false;
                } else if (!workunits.contains(resource.getWorkunit())) {
                    succeedingApplications.retainAll(resource.getWorkunit().getRunnableApplications());
                }

                if (succeedingApplications.isEmpty()) {
                    // Stop if the intersection is empty, i.e., there is no application for all selected resources.
                    break;
                }
            }
        }
        return CollectionHelper.sortObjects(succeedingApplications);
    }

    public void removeResourceFromBasket(ResourceBasket resourceBasket, Resource resource) {
        if (resource != null) {
            removeResourcesFromBasket(resourceBasket, Collections.singleton(resource));
        }
    }

    public void removeResourcesFromBasket(ResourceBasket resourceBasket, Collection<Resource> resources) {
        if (resourceBasket != null) {
            if (resources != null) {
                resourceBasket.getResources().removeAll(resources);
            } else {
                resourceBasket.getResources().clear();
            }
            merge(resourceBasket);
        }
    }

    public void removeResourcesFromBasket(ResourceBasket resourceBasket) {
        removeResourcesFromBasket(resourceBasket, null);
    }

    public ResourceBasket saveBasketFromSelectedResources(ResourceBasket resourceBasket, Set<Resource> resources) {
        ResourceBasket basketFromSelectedResources = new ResourceBasket();
        basketFromSelectedResources.setName(Messages.get("configureDerivedBasketNamePrefix") + " " + resourceBasket.getId());
        basketFromSelectedResources.setResources(resources);
        persist(basketFromSelectedResources);
        return basketFromSelectedResources;
    }
}
