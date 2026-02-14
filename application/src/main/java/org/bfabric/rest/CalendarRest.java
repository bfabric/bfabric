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

package org.bfabric.rest;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import net.fortuna.ical4j.model.Calendar;
import org.bfabric.Messages;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.User;
import org.bfabric.service.EntityService;
import org.bfabric.service.UserService;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.TokenUtils;

@Path("/calendar")
public class CalendarRest {

    @GET
    @Path("/sync")
    public Response getEventsAsIcal(@Nonnull @QueryParam("token") String token) {
        String decryptedToken = TokenUtils.decrypt(token);

        if (decryptedToken != null) {
            //convert parameters from token to key/value map
            Map<String, String> userAndEventScope = Arrays.stream(decryptedToken.split(",")).map(s -> s.split("=")).collect(Collectors.toMap(s -> s[0], s -> s[1]));
            final Calendar calendar = ConfigurationHelper.getConfiguration().getIcsCalendar();
            if (userAndEventScope.containsKey("user")) {
                final User user = CDI.current().select(UserService.class).get().getUserByLogin(userAndEventScope.get("user"));
                if (user == null) {
                    throw new EntityNotFoundException(Messages.get("errorNoValidUserFoundOutOfToken"));
                }
                if (!userAndEventScope.containsKey("scope") || userAndEventScope.get("scope").equals("instrumentreservation")) {
                    user.getInstrumentReservations().forEach(event -> event.addEventToIcsCalendar(calendar));
                }
                if (!userAndEventScope.containsKey("scope") || userAndEventScope.get("scope").equals("agendaevent")) {
                    user.getEvents().forEach(event -> event.addEventToIcsCalendar(calendar));
                }
                if (!userAndEventScope.containsKey("scope") || userAndEventScope.get("scope").equals("publicevent")) {
                    user.getPublicEvents().forEach(event -> event.addEventToIcsCalendar(calendar));
                }
            } else if (userAndEventScope.containsKey("instrument")) {
                final Instrument instrument = CDI.current().select(EntityService.class).get().find(Instrument.class, Long.parseLong(userAndEventScope.get("instrument")));
                if (instrument == null) {
                    throw new EntityNotFoundException(Messages.get("errorNoValidInstrumentFoundOutOfToken"));
                }
                instrument.getReservations().forEach(event -> event.addEventToIcsCalendar(calendar));
            }
            return Response.ok(calendar.toString()).build();
        }
        return null;
    }
}