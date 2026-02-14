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

import java.util.LinkedHashMap;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Contract;
import org.bfabric.entity.ContractType;
import org.bfabric.entity.Instrument;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ContractService;
import org.bfabric.service.ContractTypeService;
import org.bfabric.service.InstrumentService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class ContractManager extends AbstractEntityManager<Contract> {

    private static final long serialVersionUID = 1;

    @Inject
    private ContractService contractService;

    @Inject
    private ContractTypeService contractTypeService;

    @Param
    private Long instrumentId;

    @Inject
    private InstrumentService instrumentService;

    public ContractManager() {
        super(Contract.class);
    }

    @Override
    protected Contract createInstance() {
        final Contract contract = super.createInstance();
        if (instrumentId != null) {
            contract.getInstruments().add(entityService.find(Instrument.class, instrumentId));
        }
        contract.setSupervisor(getCurrentUser());
        if (getCurrentUser().getDefaultTechnology() != null) {
            contract.getTechnologies().add(getCurrentUser().getDefaultTechnology());
        }
        return contract;
    }

    public String enable() {
        contractService.enable(getContract());
        // Print faces message.
        getFacesMessagesManager().bufferWarningClear(Messages.get("enabled"));
        return postSave(false, false);
    }

    @Produces
    @Named("contract")
    public Contract getContract() {
        return getInstance();
    }

    public List<ContractType> getContractTypesFiltered(String filterString) {
        return contractTypeService.getContractTypesIncludingFiltered(filterString, getContract().getContractType());
    }

    public List<Instrument> getInstrumentsFiltered(String filterString) {
        return instrumentService.getInstruments(filterString, null, getContract().getInstruments());
    }

    public String obsolete() {
        contractService.obsolete(getContract());

        // Print faces message.
        getFacesMessagesManager().bufferWarningClear(Messages.get("obsolete"));
        return postSave(false, false);
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = contractService.isValid(getContract());
        if (validationErrorMsg.isEmpty()) {
            return super.save();
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    @SuppressWarnings("EmptyMethod")
    public void setInstrumentSelected(boolean isInstrumentSelected) {

    }
}