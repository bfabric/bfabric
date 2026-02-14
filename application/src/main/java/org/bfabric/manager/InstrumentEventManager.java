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

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentEvent;
import org.bfabric.entity.InstrumentEventType;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.InstrumentEventService;
import org.bfabric.service.InstrumentEventTypeService;
import org.bfabric.service.InstrumentService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class InstrumentEventManager extends AbstractEntityManager<InstrumentEvent> {

    private static final long serialVersionUID = 1;

    @Inject
    private InstrumentEventService instrumentEventService;

    @Inject
    private InstrumentEventTypeService instrumentEventTypeService;

    @Param
    private Long instrumentId;

    @Inject
    private InstrumentService instrumentService;

    public InstrumentEventManager() {
        super(InstrumentEvent.class);
    }

    @Override
    protected InstrumentEvent createInstance() {
        final InstrumentEvent instrumentEvent = super.createInstance();
        User user = entityService.find(User.class, getCurrentUser().getId());
        instrumentEvent.setUser(user);
        if (getInstrumentId() != null) {
            instrumentEvent.setInstrument(entityService.find(Instrument.class, getInstrumentId()));
        }
        return instrumentEvent;
    }

    @Produces
    @Named("instrumentEvent")
    public InstrumentEvent getInstrumentEvent() {
        return getInstance();
    }

    public List<InstrumentEventType> getInstrumentEventTypes(String filterString) {
        return (List<InstrumentEventType>) instrumentEventTypeService.getFilteredEnabledIncludingOrderBy(getInstrumentEvent().getInstrumentEventType(), filterString, null);
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public List<Instrument> getInstruments(String filterString) {
        return (List<Instrument>) instrumentService.getFilteredEnabledIncludingOrderBy(getInstrumentEvent().getInstrument(), filterString, null);
    }

    @Override
    public String getRedirectURLAfterSave() {
        return createRedirectShowScreenURL(getInstrumentEvent().getInstrument(), "instrumentEvents", null);
    }

    @Override
    public String save() {
        return validateAndSave(instrumentEventService);
    }
}