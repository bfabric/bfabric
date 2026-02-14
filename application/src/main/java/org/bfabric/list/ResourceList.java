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

package org.bfabric.list;

import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Application;
import org.bfabric.entity.Resource;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.ResourceService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class ResourceList extends AbstractList<Resource> {

    private static final long serialVersionUID = 1;

    @Inject
    private ResourceService resourceService;

    private String resourceUriDownloadFolder;

    @CachedMethodResult
    public BfabricLazyDataModel<Resource> getAvailableResourcesByApplicationAndUser(Application application, User user, boolean insideBasket) {
        return getService().getAvailableResourcesByApplicationAndUser(application, user, insideBasket, identityManager.hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER));
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Resource> getLazyModelByApplicationId(long applicationId) {
        return getService().getLazyModelByApplicationId(applicationId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Resource> getLazyModelByStorageId(long storageId) {
        return getService().getLazyModelByStorageId(storageId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Resource> getLazyModelByWorkunitId(long workunitId) {
        return getService().getLazyModelByWorkunitId(workunitId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Object> getLazyModelResourceSampleViewByContainerId(Long containerId) {
        return getService().getLazyModelResourceSampleViewByContainerId(containerId);
    }

    public String getResourceUriDownloadFolder() {
        return resourceUriDownloadFolder;
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Resource> getResourcesByIds(Set<Long> ids) {
        return getService().getResourcesByIds(ids);
    }

    @CachedMethodResult
    public List<Resource> getResourcesByWorkunitId(long workunitId) {
        return getService().getResourcesByWorkunitId(workunitId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Resource> getResourcesByWorkunitIds(Set<Long> workunitIds) {
        return getService().getResourcesByWorkunitIds(workunitIds);
    }

    @Override
    protected ResourceService getService() {
        return resourceService;
    }

    public void setResourceUriDownloadFolder(String resourceUriDownloadFolder) {
        this.resourceUriDownloadFolder = resourceUriDownloadFolder;
    }
}