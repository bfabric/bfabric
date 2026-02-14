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

import java.util.Collection;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.SampleService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class SampleList extends AbstractList<Sample> {

    private static final long serialVersionUID = 1;

    @Inject
    private SampleService sampleService;

    public static List<SampleAttributeEnum> getEnabledSampleAttributeEnum() {
        return SampleAttributeEnum.getEnabledSampleAttributeEnum();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getAvailableSamplesByContainersAndSampleType(Collection<Container> containers, SampleType sampleType) {
        return getService().getAvailableSamplesByContainersAndSampleType(containers, sampleType);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLabeledAndMultiplexedByTypeLazyModel(String libraryType, boolean excludeOrderItemSamples) {
        return getService().getLabeledAndMultiplexedByTypeLazyModel(libraryType, excludeOrderItemSamples);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLabeledSamplesByType(String type) {
        return getService().getLabeledSamplesByType(type);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLabeledSamplesByType(SampleTypeEnum sampleTypeEnum) {
        return getService().getLabeledSamplesBySampleTypeEnum(sampleTypeEnum);
    }

    @Override
    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModel() {
        return getService().getLazyModel();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByAnnotation(Annotation annotation) {
        return getService().getLazyModelByAnnotation(annotation);
    }

    @Override
    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByContainerId(long containerId) {
        return getService().getLazyModelByContainerId(containerId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByContainerIdAndTypes(long containerId, Collection<String> types) {
        return getService().getLazyModelByContainerIdAndTypes(containerId, types);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByControlSampleId(long controlSampleId) {
        return getService().getLazyModelByControlSampleId(controlSampleId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByInstrumentReadConfigurationId(long instrumentReadConfigurationId) {
        return getService().getLazyModelByInstrumentReadConfigurationId(instrumentReadConfigurationId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByInstrumentReadConfigurationIdAndTypes(long instrumentReadConfigurationId, Collection<String> types, boolean excludeOrderItemSamples) {
        return getService().getLazyModelByInstrumentReadConfigurationIdAndTypes(instrumentReadConfigurationId, types, excludeOrderItemSamples);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByOrphansOnlyAndControlOnly(Boolean showOrphansOnly, Boolean showControlOnly, Boolean filterQcPassed, Boolean filterUserDecision) {
        return getService().getLazyModelByOrphansOnlyAndControlOnly(showOrphansOnly, showControlOnly, filterQcPassed, filterUserDecision);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByRunId(long runId) {
        return getService().getLazyModelByRunId(runId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelBySamplePreparationProtocolId(long samplePreparationProtocolId) {
        return getService().getLazyModelBySamplePreparationProtocolId(samplePreparationProtocolId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByType(String type) {
        return getService().getLazyModelByType(type);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelByTypeId(long typeId) {
        return getService().getLazyModelByTypeId(typeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLazyModelExcludingTypes(Collection<String> exclude) {
        return getService().getLazyModelExcludingTypes(exclude);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Sample> getLibrariesLazyModel() {
        return getService().getLibrariesLazyModel();
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getSampleAttributeEnums(SampleType sampleType) {
        // Note: Do not eliminate this method as it is necessary and used solely for caching purposes.
        return SampleAttributeEnum.getAttributeEnums(true, null, sampleType != null ? sampleType.getName() : null, false, null, false, null, false, false);
    }

    @Override
    protected SampleService getService() {
        return sampleService;
    }
}
