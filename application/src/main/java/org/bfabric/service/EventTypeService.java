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

package org.bfabric.service;

import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.EventType;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;

@MeasureCalls
@Named
@Stateless
public class EventTypeService extends AbstractService {

    private static final long serialVersionUID = 1;

    public EventTypeService() {
        super(EventType.class);
    }

    public List<EventType> getEventTypesByUser(User user) {
        if (!user.hasRoleImplicit(RoleEnum.AGENDAMANAGER)) {
            return createNamedQuery("EventType.findAllNonPublicOrderByColor").getResultList();
        }
        return createNamedQuery("EventType.findAllOrderByColor").getResultList();
    }

    public List<EventType> getEventTypesByUserOrderByName(User user) {
        if (!user.hasRoleImplicit(RoleEnum.AGENDAMANAGER)) {
            return createNamedQuery("EventType.findAllNonPublicOrderByName").getResultList();
        }
        return (List<EventType>) getResultListOrderByName();
    }

    public List<Object> getEventTypesForLegend() {
        return createNamedQuery("EventType.findAllGroupedByColor").getResultList();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((EventType) entity);
    }
}