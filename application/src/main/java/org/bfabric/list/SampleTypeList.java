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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.SampleType;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.SampleTypeService;

@Named
@ViewScoped
public class SampleTypeList extends AbstractList<SampleType> {

    private static final long serialVersionUID = 1;

    @Inject
    private SampleTypeService sampleTypeService;

    @CachedMethodResult
    public List<SampleType> getAllEnabledIncluding(SampleType entity) {
        return getService().getResultListEnabledIncludingExcludingOrderBy(entity, null, null);
    }

    @CachedMethodResult
    public List<String> getOnRunAndMultiplexedTypeLabels() {
        return getOnRunAndMultiplexedTypeLabelsIncluding(getConfiguration().isLabEnabled() && identityManager.hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) ? null : Collections.singletonList(SampleTypeEnum.CONTROL_SAMPLE.getLabel()));
    }

    @CachedMethodResult
    public List<String> getOnRunAndMultiplexedTypeLabelsIncluding(Collection<String> includeLabels) {
        List<String> labels = SampleTypeEnum.getOnRunAndMultiplexedTypeLabels();
        if (includeLabels != null && !includeLabels.isEmpty()) {
            labels.addAll(includeLabels);
        }
        return labels;
    }

    @CachedMethodResult
    public List<SampleType> getResultListEnabledIncludingExcludingOnRunAndMultiplexed(SampleType entity) {
        return getService().getResultListEnabledIncludingExcludingOrderBy(entity, getOnRunAndMultiplexedTypeLabels(), null);
    }

    @CachedMethodResult
    public List<SampleType> getResultListEnabledIncludingExcludingUserSampleInMultiplexAndControl(SampleType including) {
        return getService().getResultListEnabledIncludingExcludingOrderBy(including, Arrays.asList(SampleTypeEnum.USER_LIBRARY_IN_POOL.getLabel(), SampleTypeEnum.CONTROL_SAMPLE.getLabel()), null);
    }

    @CachedMethodResult
    public List<SampleTypeEnum> getSampleTypeEnumsLabelable() {
        return SampleTypeEnum.getLabelable();
    }

    @CachedMethodResult
    public List<SampleTypeEnum> getSampleTypeEnumsLabeled() {
        return SampleTypeEnum.getLabeled();
    }

    @CachedMethodResult
    public List<SampleTypeEnum> getSampleTypeEnumsMultiplexed() {
        return SampleTypeEnum.getMultiplexed();
    }

    @CachedMethodResult
    public List<String> getSampleTypeMultiplexedLabels() {
        return SampleTypeEnum.getMultiplexedLabels();
    }

    @Override
    protected SampleTypeService getService() {
        return sampleTypeService;
    }
}