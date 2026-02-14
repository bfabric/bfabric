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

import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Technology;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.LocalDateTimeInterval;

@Named
@ViewScoped
public class InstrumentReservationList extends AbstractList<InstrumentReservation> {

    private static final long serialVersionUID = 1;

    @Inject
    private InstrumentReservationService instrumentReservationService;

    @CachedMethodResult
    public BfabricLazyDataModel<InstrumentReservation> getApprovalRequiredTasks() {
        return getService().getApprovalRequiredTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public BfabricLazyDataModel<InstrumentReservation> getAssignOperatorTasks() {
        return getService().getAssignOperatorTasks(identityManager.getCurrentUser());
    }

    @CachedMethodResult
    public List<InstrumentReservation> getInstrumentReservationsByInterval(LocalDateTimeInterval interval, String view) {
        return getService().getEventsByInterval(interval, view);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<InstrumentReservation> getLazyModelByInstrumentReservationTypeId(long instrumentReservationTypeId) {
        return getService().getLazyModelByInstrumentReservationTypeId(instrumentReservationTypeId);
    }

    @CachedMethodResult
    public BfabricLazyDataModel<InstrumentReservation> getLazyModelByTechnology(Technology technology) {
        return getService().getLazyModelByTechnology(technology);
    }

    @Override
    protected InstrumentReservationService getService() {
        return instrumentReservationService;
    }
}