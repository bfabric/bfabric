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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.bfabric.Messages;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.EventList;
import org.bfabric.service.AgendaYearClosedService;
import org.bfabric.service.EventTypeService;
import org.bfabric.util.AgendaEventHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class AgendaEventManager extends AbstractAgendaManager {

    private static final long serialVersionUID = 1;

    private transient AgendaEventHelper agendaHelper;

    @Inject
    private AgendaYearClosedService agendaYearClosedService;

    private List<String> closeAgendaYearResult = new ArrayList<>();

    @Inject
    private EventList eventList;

    @Inject
    private EventTypeService eventTypeService;

    private int selectedYear;

    private UserGroup userGroupFilter;

    private List<User> usersFiltered;

    @Param
    private Integer year;

    @Transactional
    public void closeAgendaForPreviousYear() {
        int agendaYear = LocalDate.now().getYear() - 1;
        closeAgendaYearResult = agendaYearClosedService.closeAgendaYear(agendaYear, getCurrentUser());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyClosedVacation") + " " + agendaYear);
    }

    @CachedMethodResult
    @Override
    @Produces
    @Named("agendaEventHelper")
    public AgendaEventHelper getAgendaHelper() {
        if (agendaHelper == null) {
            agendaHelper = getNewAgendaHelper();
        }
        return agendaHelper;
    }

    @CachedMethodResult
    @Produces
    @Named("agendaEventHelperMonths")
    public List<AgendaEventHelper> getAgendaHelperMonths() {
        final List<AgendaEventHelper> agendaHelperMonths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            final AgendaEventHelper agendaHelperMonth = new AgendaEventHelper();
            final LocalDateTime intervalStart = LocalDateTime.of(getYear(), i, 1, 8, 0);
            final LocalDateTime intervalEnd = intervalStart.plusMonths(1).minusDays(1).plusHours(12);
            agendaHelperMonth.setInterval(intervalStart, intervalEnd);
            agendaHelperMonth.setEvents(eventList.getEventsByInterval(agendaHelperMonth.getInterval(), getUserGroupFilter()));
            agendaHelperMonths.add(agendaHelperMonth);
        }
        return agendaHelperMonths;
    }

    public List<String> getCloseAgendaYearResult() {
        return closeAgendaYearResult;
    }

    @CachedMethodResult
    public List<Object> getEventTypesForLegend() {
        return eventTypeService.getEventTypesForLegend();
    }

    public AgendaEventHelper getNewAgendaHelper() {
        final AgendaEventHelper newAgendaHelper = new AgendaEventHelper();

        // Set the interval.
        newAgendaHelper.setInterval(getIntervalForAgendaHelper());

        // Set the events.
        newAgendaHelper.setEvents(eventList.getEventsByInterval(newAgendaHelper.getInterval(), getUserGroupFilter()));

        // Set the jump date to interval start week if the jump date is not set explicitly.
        if (getJumpDate() == null || getJumpDate().isEqual(LocalDate.now())) {
            setJumpDate(newAgendaHelper.getInterval().getStart().toLocalDate());
        }

        return newAgendaHelper;
    }

    @Produces
    @Named("agendaEventYear")
    public int getSelectedYear() {
        if (selectedYear == 0) {
            selectedYear = getYear();
        }
        return selectedYear;
    }

    public UserGroup getUserGroupFilter() {
        return userGroupFilter;
    }

    public List<User> getUsersFiltered() {
        if (usersFiltered == null) {
            usersFiltered = new ArrayList<>(getAgendaHelper().getUsers());
            if (getUserGroupFilter() != null) {
                usersFiltered.retainAll(getUserGroupFilter().getUsers());
            }
            if (!usersFiltered.contains(getCurrentUser())) {
                usersFiltered.add(getCurrentUser());
            }
        }
        return usersFiltered;
    }

    public int getYear() {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        return year;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        setUserGroupFilter(getSessionManager().getUserGroupFilter());
    }

    @Produces
    @Named("agendaEventClosable")
    public boolean isClosable() {
        return agendaYearClosedService.isOpen(LocalDate.now().getYear() - 1);
    }

    public void setSelectedYear(int year) {
        selectedYear = year;
    }

    public void setUserGroupFilter(UserGroup userGroupFilter) {
        this.userGroupFilter = userGroupFilter;
        getSessionManager().setUserGroupFilter(userGroupFilter);
    }

    @SuppressWarnings("EmptyMethod")
    public void setUserGroupFilterName(String userGroupFilterName) {
        // required dummy method for default filter option in application tables.
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void userGroupFilterChanged(ValueChangeEvent event) {
        setUserGroupFilter((UserGroup) event.getNewValue());
        usersFiltered = null;
    }
}