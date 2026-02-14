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

import org.bfabric.Constants;
import org.bfabric.entity.Event;
import org.bfabric.entity.User;

public class AgendaEventHelperSlot extends AgendaHelperSlot {

    private Event commonEvent;

    private User user;

    public AgendaEventHelperSlot() {
    }

    public String getBackgroundClass() {
        return hasPublicEvent() ? "slotOpac" : Constants.EMPTY_STRING;
    }

    @Override
    public String getColor() {
        return getEvent() != null ? getEvent().getEventType().getColor() : hasPublicEvent() ? "#DDDDDD" : "#FFFFFF";
    }

    public Event getCommonEvent() {
        return commonEvent;
    }

    @Override
    public String getDescription() {
        String description = Constants.EMPTY_STRING;
        if (getEvent() != null) {
            description = getEvent().getFullEventInfo();
            if (getEvent().getDescription() != null) {
                description += " " + getEvent().getDescription();
            }
        } else {
            if (user != null) {
                description = user.getName() + ": ";
            }
            description += getDateAsText();
        }
        return description;
    }

    @Override
    public Event getEvent() {
        return (Event) event;
    }

    @Override
    public String getName() {
        if (getEvent() == null) {
            return null;
        }
        return getEvent().getEventType().getName();
    }

    public User getUser() {
        return user;
    }

    public boolean hasPublicEvent() {
        return getCommonEvent() != null && getCommonEvent().getEventType().isPublicEvent();
    }

    public boolean isAccountable() {
        return getEvent() != null && getEvent().getEventType().isAccountable() && !isWeekend() && !(getCommonEvent() != null && getCommonEvent().getEventType().isFreeEvent());
    }

    public void setCommonEvent(Event commonEvent) {
        this.commonEvent = commonEvent;
    }

    public void setUser(User user) {
        this.user = user;
    }
}