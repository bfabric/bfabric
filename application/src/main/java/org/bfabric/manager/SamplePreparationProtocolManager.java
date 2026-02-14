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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReadConfiguration;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.SampleType;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.InstrumentReadConfigurationService;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.SamplePreparationProtocolService;
import org.bfabric.service.SampleTypeService;
import org.bfabric.service.SequencingApplicationService;

@MeasureCalls
@Named
@ViewScoped
public class SamplePreparationProtocolManager extends AbstractEntityManager<SamplePreparationProtocol> {

    private static final long serialVersionUID = 1;

    @Inject
    private InstrumentReadConfigurationService instrumentReadConfigurationService;

    @Inject
    private InstrumentService instrumentService;

    @Inject
    private SamplePreparationProtocolService samplePreparationProtocolService;

    @Inject
    private SampleTypeService sampleTypeService;

    @Inject
    private SequencingApplicationService sequencingApplicationService;

    public SamplePreparationProtocolManager() {
        super(SamplePreparationProtocol.class);
    }

    public List<SamplePreparationProtocol> getFilteredEnabledPotentialPredecessorProtocols(String filterString) {
        return samplePreparationProtocolService.getFilteredEnabledSamplePreparationProtocolsIncluding(filterString, getSamplePreparationProtocol().getPredecessor());
    }

    public List<InstrumentReadConfiguration> getInstrumentReadConfigurationsFiltered(String filterString) {
        return instrumentReadConfigurationService.getInstrumentReadConfigurations(filterString, getSamplePreparationProtocol().getInstrumentReadConfigurations());
    }

    public List<Instrument> getInstrumentsFiltered(String filterString) {
        return instrumentService.getInstruments(filterString, null, getSamplePreparationProtocol().getInstruments());
    }

    public List<SampleType> getPossibleSampleTypes(String filterString) {
        Set<SampleType> exclude = new HashSet<>(getSamplePreparationProtocol().getSampleTypes());
        exclude.add(sampleTypeService.getSampleTypeByName(SampleTypeEnum.USER_LIBRARY_IN_POOL.getLabel()));
        return sampleTypeService.getSampleTypesFiltered(filterString, exclude);
    }

    public List<SamplePreparationProtocol> getPotentialPredecessorProtocols() {
        List<SamplePreparationProtocol> potentialPredecessorProtocols = (List<SamplePreparationProtocol>) samplePreparationProtocolService.getResultListOrderByName();
        potentialPredecessorProtocols.remove(getSamplePreparationProtocol());
        if (getSamplePreparationProtocol() != null && !getSamplePreparationProtocol().getSuccessors().isEmpty()) {
            potentialPredecessorProtocols.removeAll(getSamplePreparationProtocol().getSuccessors());
        }
        return potentialPredecessorProtocols;
    }

    @Produces
    @Named("samplePreparationProtocol")
    public SamplePreparationProtocol getSamplePreparationProtocol() {
        return getInstance();
    }

    public List<SequencingApplication> getSequencingApplicationFiltered(String filterString) {
        return sequencingApplicationService.getSequencingApplications(filterString, null, getSamplePreparationProtocol().getSequencingApplications());
    }

    @Override
    public String save() {
        getSamplePreparationProtocol().resetSamplePreparationProtocolAttributes();

        LinkedHashMap<String, String> validationErrorMsg = samplePreparationProtocolService.isValid(getSamplePreparationProtocol());

        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            samplePreparationProtocolService.save(getSamplePreparationProtocol());
            return postSave(true, false);
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }
}
