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

import java.time.LocalDate;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.InstrumentReservationList;
import org.bfabric.util.AgendaInstrumentReservationHelper;

@MeasureCalls
@Named
@ViewScoped
public class AgendaInstrumentReservationManager extends AbstractAgendaManager {

    private static final long serialVersionUID = 1;

    private transient AgendaInstrumentReservationHelper agendaHelper;

    @Inject
    private InstrumentReservationList instrumentReservationList;

    @Override
    @Produces
    @Named("agendaInstrumentReservationHelper")
    @CachedMethodResult
    public AgendaInstrumentReservationHelper getAgendaHelper() {
        if (agendaHelper == null) {
            agendaHelper = getNewAgendaHelper();
        }
        return agendaHelper;
    }

    @Override
    public long getInitialSpan() {
        return 2 * weekSpan - 1;
    }

    public AgendaInstrumentReservationHelper getNewAgendaHelper() {
        AgendaInstrumentReservationHelper newAgendaHelper = new AgendaInstrumentReservationHelper();

        // Set the interval.
        newAgendaHelper.setInterval(getIntervalForAgendaHelper());

        // Set the events
        newAgendaHelper.setEvents(instrumentReservationList.getInstrumentReservationsByInterval(newAgendaHelper.getInterval(), view));

        // Set the jump date to interval start week if the jump date is not set explicitly.
        if (getJumpDate() == null || getJumpDate().equals(LocalDate.now())) {
            setJumpDate(newAgendaHelper.getInterval().getStart().toLocalDate());
        }

        return newAgendaHelper;
    }

    public String getView() {
        return view;
    }

    public void redirectWithApprovalView() {
        StringBuilder relativePath = new StringBuilder("/instrumentschedule/list.html");
        if (getInterval() != null) {
            relativePath.append("?interval=");
            relativePath.append(getInterval());
            relativePath.append("&view=");
        } else {
            relativePath.append("?view=");
        }
        relativePath.append(getView());
        getSessionManager().redirectRelative(relativePath.toString());
    }

    public void setView(String view) {
        this.view = view;
    }
}