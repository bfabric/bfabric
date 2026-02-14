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
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Technology;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class InstrumentList extends AbstractList<Instrument> {

    private static final long serialVersionUID = 1;

    @Inject
    private InstrumentService instrumentService;

    @CachedMethodResult
    public List<Instrument> getBookableInstruments() {
        return getService().getBookableInstruments();
    }

    @CachedMethodResult
    public List<Instrument> getBookableInstrumentsByTechnology(Technology technology) {
        return getService().getBookableInstrumentsByTechnology(technology, !identityManager.getCurrentUser().hasRoleImplicit(RoleEnum.INSTRUMENTREADER));
    }

    @CachedMethodResult
    public List<Instrument> getInstruments(boolean all, Technology technology) {
        return getService().getInstruments(all, technology, false);
    }

    @CachedMethodResult
    public List<EntityLog> getLastEntityLogs(Instrument instrument) {
        return getService().getLastEntityLogs(instrument, Math.max(instrument.getStates().size(), 5));
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Instrument> getLazyModelByAnnotation(Annotation annotation) {
        return getService().getLazyModelByAnnotation(annotation);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Instrument> getReassignInstrumentAdminTasks() {
        return getService().getReassignInstrumentAdminTasks();
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Instrument> getReassignInstrumentSupervisorTasks() {
        return getService().getReassignInstrumentSupervisorTasks();
    }

    @CachedMethodResult
    public List<Instrument> getResultListBookableIncluding(Instrument instrument) {
        return getService().getResultListBookableIncluding(instrument);
    }

    @CachedMethodResult
    public List<Instrument> getResultListEnabledIncludingByServiceType(Instrument instrument, ServiceType serviceType) {
        return getService().getResultListEnabledIncludingByServiceType(instrument, serviceType);
    }

    @CachedMethodResult
    public List<Instrument> getRunEnabledInstruments() {
        return getService().getRunEnabledInstruments();
    }

    @Override
    protected InstrumentService getService() {
        return instrumentService;
    }

    @CachedMethodResult
    public List<Technology> getTechnologiesByBookableInstruments() {
        return getService().getTechnologiesByBookableInstruments();
    }
}