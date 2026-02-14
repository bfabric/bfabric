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

import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.SamplePreparationProtocolService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class SamplePreparationProtocolList extends AbstractList<SamplePreparationProtocol> {

    private static final long serialVersionUID = 1;

    @Inject
    private SamplePreparationProtocolService samplePreparationProtocolService;

    public List<SamplePreparationProtocol> getEnabledLibraryProtocolsIncludingByInstrumentOrSequencingApplication(Instrument instrument, SequencingApplication sequencingApplication,
        SamplePreparationProtocol samplePreparationProtocol) {
        return getService().getEnabledLibraryProtocolsIncludingByInstrumentOrSequencingApplication(instrument, sequencingApplication, samplePreparationProtocol);
    }

    public List<SamplePreparationProtocol> getEnabledSamplePreparationProtocolsIncluding(Sample sample) {
        return getService().getEnabledSamplePreparationProtocolsIncluding(sample);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<SamplePreparationProtocol> getLazyModelByAnnotation(Annotation annotation) {
        return getService().getLazyModelByAnnotation(annotation);
    }

    @Override
    protected SamplePreparationProtocolService getService() {
        return samplePreparationProtocolService;
    }
}