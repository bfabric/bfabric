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

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Container;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ContainerService;
import org.bfabric.service.util.BfabricLazyDataModel;

@MeasureCalls
@Named
@ViewScoped
public class ContainerList extends AbstractList<Container> {

    private static final long serialVersionUID = 1;

    @Inject
    private ContainerService containerService;

    public List<Container> getAcceptedReadableContainers(String filterString) {
        return getService().getAcceptedReadableContainers(filterString, null, identityManager.getCurrentUser());
    }

    public List<Container> getChargeAssignableContainers(String filterString) {
        return getService().getChargeAssignableContainers(filterString, identityManager.getCurrentUser());
    }

    public List<Container> getChargeableReadableContainers(String filterString) {
        return getService().getChargeableReadableContainers(filterString, identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Container> getCoachedContainersLazyModelByUserId(long userId) {
        return getService().getCoachedContainersLazyModelByUserId(userId);
    }

    @CachedMethodResult
    public List<Container> getContainersByPlateId(Long plateId) {
        return getService().getContainersByPlateId(plateId);
    }

    @CachedMethodResult
    public Long getContainersByPlateIdCount(Long plateId) {
        return getService().getContainersByPlateIdCount(plateId);
    }

    @CachedMethodResult
    public List<Container> getContainersByRunId(Long runId) {
        return getService().getContainersByRunId(runId);
    }

    @CachedMethodResult
    public Long getContainersByRunIdCount(Long runId) {
        return getService().getContainersByRunIdCount(runId);
    }

    public List<Container> getExtensibleReadableContainers(String filterString) {
        return getService().getExtensibleReadableContainers(filterString, identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Container> getFinanceSourceToBeChecked() {
        return getService().getFinanceSourceToBeChecked();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Container> getLazyDataModelByDiscussedWith(Long userId) {
        return getService().getLazyDataModelByDiscussedWith(userId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Container> getLazyModelByStorageModelId(long storageId) {
        return getService().getLazyModelByStorageModelId(storageId);
    }

    public List<Container> getReadableContainers(String filterString) {
        return getService().getReadableContainers(filterString, identityManager.getCurrentUser());
    }

    @Override
    protected ContainerService getService() {
        return containerService;
    }
}