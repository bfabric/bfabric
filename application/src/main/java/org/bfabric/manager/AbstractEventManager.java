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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEvent;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.service.UserService;
import org.omnifaces.cdi.Param;
import org.primefaces.event.schedule.ScheduleEntryMoveEvent;
import org.primefaces.event.schedule.ScheduleEntryResizeEvent;
import org.primefaces.model.ScheduleEvent;

public abstract class AbstractEventManager<T extends AbstractEvent> extends AbstractEntityManager<T> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(AbstractEventManager.class.getName());

    @Inject
    protected UserService userService;

    protected UserGroup userGroup = null;

    protected List<User> recipientsList = null;

    protected boolean sendMail = false;

    @Param
    protected String slot;

    @Param
    protected String userId;

    @Param
    protected String view;

    public AbstractEventManager() {
        super();
    }

    public AbstractEventManager(Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    protected T createInstance() {
        final T event = super.createInstance();
        event.setUser(getCurrentUser());
        return event;
    }

    public List<User> getEmployeesFiltered(String filterString) {
        return userService.getEmployeesFiltered(filterString, getRecipientsList());
    }

    public List<User> getRecipientsList() {
        if (recipientsList == null) {
            recipientsList = new ArrayList<>();
        }
        return recipientsList;
    }

    public boolean getSendMail() {
        return sendMail;
    }

    public String getSlot() {
        return slot;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getInstance() != null && !isManaged()) {
            getInstance().setDateTimeSlot(slot);
        }
        if (getInstance() != null && userId != null) {
            try {
                getInstance().setUser(entityService.find(User.class, Long.valueOf(userId)));
            } catch (Exception e) {
                logger.fine("There is no user that matches userId " + userId);
            }
        }
    }

    public void onEventMove(ScheduleEntryMoveEvent event) {
        saveEvent(event.getScheduleEvent());
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {
        saveEvent(event.getScheduleEvent());
    }

    public String removeAll() {
        return removeRepeating(Constants.REMOVE_ALL);
    }

    public String removeFollowing() {
        return removeRepeating(Constants.REMOVE_FOLLOWING);
    }

    public abstract String removeRepeating(String series);

    public String removeThis() {
        return removeRepeating(Constants.REMOVE_THIS);
    }

    public void repeaterEndChanged() {
        getFacesMessagesManager().printWarn(getInstance().repeaterEndChanged());
    }

    public void repeaterWeeksChanged(ValueChangeEvent event) {
        getFacesMessagesManager().printWarn(getInstance().repeaterWeeksChanged(event));
    }

    public void saveEvent(ScheduleEvent<T> event) {
        setInstance(event);
        if (getInstance().isUpdatable()) {
            save();
            getFacesMessagesManager().clearGlobalMessages();
            getFacesMessagesManager().printWarn(Messages.get("successfullyUpdated"));
        } else {
            getFacesMessagesManager().clearGlobalMessages();
            getFacesMessagesManager().printWarn(Messages.get("notUpdatableHint"));
        }
    }

    public void sendMailChanged(ValueChangeEvent event) {
        sendMail = (Boolean) event.getNewValue();
        if (!sendMail) {
            getRecipientsList().clear();
        }
    }

    public void setInstance(ScheduleEvent<T> event) {
        if (event.getData() != null) {
            AbstractEvent abstractEvent = entityService.find(event.getData().getClass(), event.getData().getId());
            if (abstractEvent != null && abstractEvent.isReadable()) {
                setInstance(abstractEvent);
                getInstance().setStartDate(event.getStartDate());
                getInstance().setEndDate(event.getEndDate());
            }
        }
    }

    public void setRecipientsList(List<User> recipientsList) {
        this.recipientsList = recipientsList;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

}