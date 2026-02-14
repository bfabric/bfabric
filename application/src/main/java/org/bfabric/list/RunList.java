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

import org.bfabric.entity.Run;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.RunService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class RunList extends AbstractList<Run> {

    private static final long serialVersionUID = 1;

    @Inject
    private RunService runService;

    @CachedMethodResult
    public BfabricLazyDataModel<Run> getLazyModelByInstrumentReadConfigurationId(long instrumentReadConfigurationId) {
        return getService().getLazyModelByInstrumentReadConfigurationId(instrumentReadConfigurationId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Run> getLazyModelByInstrumentReadConfigurationIdAndRunUnitTypeId(long instrumentReadConfigurationId, long runUnitTypeId) {
        return getService().getLazyModelByInstrumentReadConfigurationIdAndRunUnitTypeId(instrumentReadConfigurationId, runUnitTypeId);
    }

    @CachedMethodResult
    public List<Run> getNonFinishedRuns(Long supervisorId) {
        return getService().getNonFinishedRuns(supervisorId);
    }

    @CachedMethodResult
    public List<StatusEnum> getRunStatusEnums() {
        return getService().getRunStatusEnums();
    }

    @CachedMethodResult
    public List<Run> getRunsByContainerId(Long containerId) {
        return getService().getRunsByContainerId(containerId);
    }

    @CachedMethodResult
    public Long getRunsByContainerIdCount(Long containerId) {
        return getService().getRunsByContainerIdCount(containerId);
    }

    @CachedMethodResult
    public List<Run> getRunsBySampleId(Long sampleId) {
        return getService().getRunsBySampleId(sampleId);
    }

    @CachedMethodResult
    public Long getRunsBySampleIdCount(Long sampleId) {
        return getService().getRunsBySampleIdCount(sampleId);
    }

    @Override
    protected RunService getService() {
        return runService;
    }

    @CachedMethodResult
    public List<StatusEnum> getStatusEnums() {
        return getService().getStatusEnums();
    }
}