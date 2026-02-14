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

package org.bfabric.util;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.interceptors.MeasureCalls;

@MeasureCalls
public class AgendaInstrumentReservationHelper extends AbstractAgendaHelper {

    protected List<InstrumentReservation> events;

    public AgendaInstrumentReservationHelper() {
    }

    @Override
    public List<InstrumentReservation> getEvents() {
        if (events == null) {
            events = new ArrayList<>();
        }
        return events;
    }

    @Override
    public List<AgendaHelperSlot> getHeadSlots() {
        List<AgendaHelperSlot> headSlots = new ArrayList<>();
        LocalDateTime tmpDateTime = interval.getStart();

        // create slots in interval
        while (interval.contains(tmpDateTime)) {
            try {
                // create slot
                AgendaHelperSlot agendaHelperSlot = new AgendaHelperSlot();
                agendaHelperSlot.setDate(tmpDateTime);

                // set month span: number of slots until the end of the month
                if (tmpDateTime.getDayOfMonth() == 1 || tmpDateTime.equals(interval.getStart())) {
                    LocalDateTime endOfMonthDate = tmpDateTime.plusMonths(1).withDayOfMonth(1);
                    endOfMonthDate = endOfMonthDate.minusDays(1);
                    if (interval.contains(endOfMonthDate)) {
                        agendaHelperSlot.setMonthSpan(Period.between(tmpDateTime.toLocalDate(), endOfMonthDate.toLocalDate()).getDays() + 1);
                    } else {
                        agendaHelperSlot.setMonthSpan(Period.between(tmpDateTime.toLocalDate(), interval.getEnd().toLocalDate()).getDays() + 1);
                    }
                }

                headSlots.add(agendaHelperSlot);

                // proceed to next slot
                tmpDateTime = tmpDateTime.plusDays(1);
            } catch (Exception e) {
                break;
            }
        }
        return headSlots;
    }

    private Map<Instrument, List<InstrumentReservation>> getInstrumentReservationsMap() {
        return getEvents().stream().collect(Collectors.groupingBy(InstrumentReservation::getInstrument));
    }

    public List<Instrument> getInstruments() {
        if (hasEvents()) {
            Map<Instrument, List<InstrumentReservation>> map = getInstrumentReservationsMap();
            LinkedList<Instrument> instruments = new LinkedList<>();
            Iterator<Instrument> iter = map.keySet().iterator();
            Instrument current;
            ListIterator<Instrument> currentPos;
            boolean done;
            while (iter.hasNext()) {
                Instrument instrument = iter.next();
                if (instrument != null) {
                    if (instruments.isEmpty()) {
                        instruments.add(instrument);
                    } else {
                        // iterate through list to alphabetically correct position
                        currentPos = instruments.listIterator();
                        current = currentPos.next();
                        done = false;
                        do {
                            if (currentPos.hasNext()) {
                                if (current.getName().compareToIgnoreCase(instrument.getName()) < 0) {
                                    // if name lexically smaller, go to next
                                    current = currentPos.next();
                                } else {
                                    if (currentPos.hasPrevious()) {
                                        currentPos.previous();
                                        currentPos.add(instrument);
                                    } else {
                                        instruments.addFirst(instrument);
                                    }
                                    done = true;
                                }
                            } else {
                                if (current.getName().compareToIgnoreCase(instrument.getName()) < 0) {
                                    // if name lexically smaller, go to next
                                    currentPos.add(instrument);
                                } else {
                                    if (currentPos.hasPrevious()) {
                                        currentPos.previous();
                                        currentPos.add(instrument);
                                    } else {
                                        instruments.addFirst(instrument);
                                    }
                                }
                                done = true;
                            }

                        } while (!done);
                    }
                }
            }
            return instruments;
        }
        return null;
    }

    public Set<AgendaInstrumentReservationHelperSlot> getSlotsForInstrument(Instrument instrument) {
        // Number of days between the beginning and the end of interval for when reservation slots must be extracted.
        long numOfDaysBetween = ChronoUnit.DAYS.between(interval.getStart().toLocalDate(), interval.getEnd().toLocalDate().plusDays(1));
        // Iterate over numbers representing days of the interval.
        return IntStream.iterate(0, i -> i + 1)
            // Set the limit of the iteration to the interval.
            .limit(numOfDaysBetween)
            // Get the current date with respect to the start of the interval.
            .mapToObj(i -> interval.getStart().plusDays(i))
            .map(today -> {
                AgendaInstrumentReservationHelperSlot agendaHelperSlot = new AgendaInstrumentReservationHelperSlot();
                agendaHelperSlot.setDate(today);
                agendaHelperSlot.setInstrument(instrument);
                if (getInstrumentReservationsMap().containsKey(instrument))
                    agendaHelperSlot.setEvents(getInstrumentReservationsMap().get(instrument).stream()
                        .filter(ir -> (ir.getStartDate().toLocalDate().isEqual(today.toLocalDate()) || ir.getStartDate().toLocalDate().isBefore(today.toLocalDate())) && !ir.getEndDate()
                            .isEqual(today.toLocalDate().atStartOfDay()) && (ir.getEndDate().toLocalDate().isEqual(today.toLocalDate()) || ir.getEndDate().toLocalDate().isAfter(today.toLocalDate())))
                        .collect(Collectors.toList()));
                return agendaHelperSlot;
            }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setEvents(List<InstrumentReservation> events) {
        this.events = events;
    }
}