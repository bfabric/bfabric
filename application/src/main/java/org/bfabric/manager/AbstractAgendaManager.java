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
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;

import org.bfabric.Constants;
import org.bfabric.util.AbstractAgendaHelper;
import org.bfabric.util.DateUtils;
import org.bfabric.util.LocalDateTimeInterval;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

public abstract class AbstractAgendaManager extends AbstractManager {

    protected static final long weekSpan = 7;

    protected static final long initialSpan = 4 * weekSpan - 1;

    protected static final long minimumSpan = weekSpan - 1;

    protected static final long maximumSpan = 64 * weekSpan - 1;

    private static final long serialVersionUID = 1;

    protected LocalDate jumpDate = LocalDate.now();

    @Param
    protected String interval;

    @Param
    protected String view;

    protected Long span;

    private LocalDateTimeInterval intervalForAgendaHelper;

    public abstract AbstractAgendaHelper getAgendaHelper();

    public long getInitialSpan() {
        return initialSpan;
    }

    public String getInterval() {
        return interval;
    }

    public LocalDateTimeInterval getIntervalForAgendaHelper() {
        if (intervalForAgendaHelper == null) {
            setIntervalForAgendaHelper();
        }
        return intervalForAgendaHelper;
    }

    public String getIntervalParameter(int action) {
        final LocalDate newJumpDate = getJumpDate();
        // Start of the interval
        LocalDate date = getAgendaHelper().getInterval().getStart().toLocalDate();

        // Get days span by computing the duration in milliseconds and converting it to days
        long span = getSpan();

        switch (action) {
        case 0: // set to jump date
            date = getStartWeek(newJumpDate);
            break;

        case 1: // expand (to double) interval
            span = (span + 1) * 2 - 1;
            break;

        case 2: // reduce (to half) interval
            span = (span - 1) / 2;
            break;

        case 3: // previous interval
            date = date.minusDays(span + 1);
            break;

        case 4: // next interval
            date = date.plusDays(span + 1);
            break;

        case 5: // previous week
            date = date.minusDays(weekSpan);
            break;

        case 6: // next week
            date = date.plusDays(weekSpan);
            break;

        case 7: // reset (to initial) interval
            span = getInitialSpan();
            break;

        default: // today
            date = getStartWeek();
        }

        return span + "-" + DateUtils.getDateAsFormattedString(date);
    }

    public LocalDate getJumpDate() {
        return jumpDate;
    }

    public String getJumpDateInterval() {
        return getIntervalParameter(0);
    }

    public String getLink(String prefix) {
        String link = StringHelper.isNotEmpty(prefix) ? prefix : Constants.EMPTY_STRING;
        if (StringHelper.isNotEmpty(view)) {
            if (StringHelper.isNotEmpty(link)) {
                link += "&";
            }
            link += "view=" + view.trim();
        }
        return link;
    }

    public long getMaximumSpan() {
        return maximumSpan;
    }

    public long getMinimumSpan() {
        return minimumSpan;
    }

    public String getNewIntervalDate(LocalDate date) {
        return getSpan() + "-" + DateUtils.getDateAsFormattedString(getStartWeek(date != null ? date : LocalDate.now()));
    }

    public String getRedirectURL(int action) {
        StringBuilder redirectURL = new StringBuilder();
        if (this instanceof AgendaEventManager) {
            redirectURL.append("/eventschedule/list.xhtml");
        } else {
            redirectURL.append("/instrumentschedule/list.xhtml");
        }
        redirectURL.append("?interval=").append(getIntervalParameter(action));
        if (view != null) {
            redirectURL.append("&view=").append(view);
        }
        return redirectURL.toString();
    }

    public long getSpan() {
        return span;
    }

    public LocalDate getStartWeek() {
        return getStartWeek(LocalDate.now());
    }

    public LocalDate getStartWeek(LocalDate date) {
        LocalDate startWeekDate = date != null ? date : LocalDate.now();
        return startWeekDate.minusDays(startWeekDate.getDayOfWeek().getValue() - 1);
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        setIntervalForAgendaHelper();
    }

    public boolean isExpandButton() {
        return getSpan() < getMaximumSpan();
    }

    public boolean isPreviousNextButton() {
        return getSpan() > weekSpan;
    }

    public boolean isReduceButton() {
        return getSpan() <= 0 || getSpan() > getMinimumSpan();
    }

    public void jumpDateChanged(ValueChangeEvent event) {
        jumpDate = (LocalDate) event.getNewValue();
    }

    public void jumpToDate() {
        interval = getJumpDateInterval();
        if (this instanceof AgendaEventManager) {
            getSessionManager().redirectRelative("/eventschedule/list.xhtml?interval=" + interval);
        } else {
            getSessionManager().redirectRelative("/instrumentschedule/list.xhtml?interval=" + interval);
        }
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    protected void setIntervalForAgendaHelper() {
        // Default day is Monday of the current week
        final LocalDate initialDay = getStartWeek();
        int agendaYear = initialDay.getYear();
        int agendaMonth = initialDay.getMonthValue();
        int agendaDay = initialDay.getDayOfMonth();
        span = getInitialSpan();
        if (getInterval() != null) {
            // Match D+-yyyy-MM-dd
            final Pattern p = Pattern.compile("(\\d+)-(\\d{4})-(\\d{2})-(\\d{2})");
            final Matcher m = p.matcher(getInterval());
            if (m.matches()) {
                span = Long.parseLong(m.group(1));
                agendaYear = Integer.parseInt(m.group(2));
                agendaMonth = Integer.parseInt(m.group(3));
                agendaDay = Integer.parseInt(m.group(4));
                // Intervals shorter than minimum span are disallowed
                if (span < getMinimumSpan()) {
                    span = getMinimumSpan();
                    setInterval(span + "-" + agendaYear + "-" + agendaMonth + "-" + agendaDay);
                }
                // Intervals longer than maximum span are disallowed
                if (span > getMaximumSpan()) {
                    span = getMaximumSpan();
                    setInterval(span + "-" + agendaYear + "-" + agendaMonth + "-" + agendaDay);
                }
            }
        }
        intervalForAgendaHelper = new LocalDateTimeInterval(LocalDateTime.of(agendaYear, agendaMonth, agendaDay, 8, 0), LocalDateTime.of(agendaYear, agendaMonth, agendaDay, 13, 0).plusDays(span));
    }

    public void setJumpDate(LocalDate jumpDate) {
        this.jumpDate = jumpDate;
    }
}
