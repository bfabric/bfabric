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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bfabric.Constants;
import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.entity.User;

public class AgendaInstrumentReservationHelperSlot extends AgendaHelperSlot {

    private String content;

    private String description;

    private List<InstrumentReservation> events;

    private Instrument instrument;

    private String link;

    private String style;

    public AgendaInstrumentReservationHelperSlot() {
    }

    public void computeContent() {
        StringBuilder descriptionBuilder = new StringBuilder();
        if (getEvents() != null && !getEvents().isEmpty()) {
            List<Container> containers = new ArrayList<>();
            for (InstrumentReservation instrumentReservation : getEvents()) {
                descriptionBuilder.append(instrumentReservation.getFullEventInfo()).append("\n\n");
                if (instrumentReservation.getInstrumentReservationType() != null && instrumentReservation.getInstrumentReservationType().isContainerAssociated()) {
                    containers.addAll(instrumentReservation.getContainers());
                }
            }
            if (containers.isEmpty()) {
                content = "N/A";
            } else {
                content = containers.get(0).getIdString();
                if (containers.size() > 1) {
                    content += " +";
                }
            }
        } else {
            descriptionBuilder.append(getDateAsText()).append(" ").append(getInstrument().getName());
            if (isWeekend() && (getLink() == null || getLink().isEmpty())) {
                content = "N/A";
                descriptionBuilder.append(" cannot be booked on weekend days!\n");
            } else {
                content = Constants.EMPTY_STRING;
            }
        }
        description = descriptionBuilder.toString();
    }

    public String getColor(User user) {
        List<Pair<String, Integer>> colors = new ArrayList<>();
        if (getEvents() != null && !getEvents().isEmpty()) {
            LocalDateTime slotDayStart = getDate().toLocalDate().atStartOfDay();
            LocalDateTime slotDayEnd = slotDayStart.plusDays(1);
            LocalDateTime timeIterator = slotDayStart;
            for (InstrumentReservation instrumentReservation : getEvents()) {

                if (instrumentReservation.getStartDate().isAfter(timeIterator)) {
                    // White for unused slots.
                    colors.add(new ImmutablePair<>("#fff", DateUtils.getDaySlotPercentage(instrumentReservation.getStartDate())));
                }

                timeIterator = instrumentReservation.getEndDate().isBefore(slotDayEnd) ? instrumentReservation.getEndDate() : slotDayEnd;
                String color;
                if (instrumentReservation.getInstrumentReservationType() != null) {
                    if (instrumentReservation.getInstrumentReservationType().getColor() != null) {
                        color = instrumentReservation.getInstrumentReservationType().getColor();
                    } else if (instrumentReservation.getApproved() == null && instrumentReservation.isApprovalRequired()) {
                        color = "#F90";
                    } else {
                        if (user != null) {
                            if (instrumentReservation.getUser() != null && instrumentReservation.getUser().getLogin().equals(user.getLogin())) {
                                // Blue if instrument reservation belongs to the user.
                                color = "#09f";
                            } else {
                                // Grey if instrument reservation belongs to another user.
                                color = "#aaa";
                            }
                        } else {
                            if (instrumentReservation.isCharged()) {
                                // Green if instrument reservation is charged.
                                color = "#090";
                            } else if (!instrumentReservation.isChargeable()) {
                                // Blue if instrument reservation is not chargeable.
                                color = "#03c";
                            } else {
                                // Red if instrument is not charged.
                                color = "#f00";
                            }
                        }
                    }
                    colors.add(new ImmutablePair<>(color, DateUtils.getDaySlotPercentage(timeIterator)));
                }
            }
            if (slotDayEnd.isAfter(timeIterator)) {
                // White for unused slots at the end of the day slot.
                colors.add(new ImmutablePair<>("#fff", DateUtils.getDaySlotPercentage(slotDayEnd, timeIterator)));
            }
        } else {
            if (isWeekend() && (getLink() == null || getLink().isEmpty())) {
                // Dark grey if instrument is non-bookable on the weekend.
                colors.add(new ImmutablePair<>("#999", 100));
            }
        }
        if (colors.isEmpty()) {
            return Constants.EMPTY_STRING;
        }
        StringBuilder colorString = new StringBuilder();
        int percent = 0;
        for (Pair<String, Integer> color : colors) {
            colorString.append(",").append(color.getKey()).append(" ").append(percent).append("%,").append(color.getKey()).append(" ").append(color.getValue()).append("%");
            percent = color.getValue() + 1;
        }
        return "background-image: linear-gradient(to right" + colorString + "); ";
    }

    public String getContent() {
        if (content == null) {
            computeContent();
        }
        return content;
    }

    @Override
    public String getDateAsText() {
        return getDate().format(Constants.DATE_FORMATTER);
    }

    @Override
    public String getDescription() {
        if (description == null) {
            computeContent();
        }
        return description;
    }

    @Override
    public InstrumentReservation getEvent() {
        return (InstrumentReservation) event;
    }

    public List<InstrumentReservation> getEvents() {
        return events;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public String getLink() {
        if (link == null) {
            if (isWeekend()) {
                Optional<InstrumentReservationSetting> reservationSetting = getInstrument().getReservationSetting(getDate().toLocalDate());
                if (reservationSetting.isPresent() && !reservationSetting.get().isWeekends()) {
                    link = Constants.EMPTY_STRING;
                }
            }
            if (link == null) {
                link = "show.html?slot=" + getDateAsText() + "&instrumentId=" + getInstrument().getIdString();
            }
        }
        return link;
    }

    public String getStyle(User user) {
        if (style == null) {
            style = getColor(user);
            if (!getInstrument().isUp()) {
                style += "opacity: 0.5; ";
            }
        }
        return style;
    }

    @Override
    public boolean isMorningSlot() {
        return true;
    }

    public void setEvents(List<InstrumentReservation> events) {
        this.events = events;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
}
