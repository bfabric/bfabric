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

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentEvent;
import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class InstrumentEventService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    public InstrumentEventService() {
        super(InstrumentEvent.class);
    }

    public BfabricLazyDataModel<InstrumentEvent> getLazyModelByInstrumentIdAndUser(long instrumentId, User user) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("instrument.id = :instrumentId");
        entityQuery.addParameter("instrumentId", instrumentId);
        if (user == null || !user.hasRoleImplicit(RoleEnum.INSTRUMENTREADER)) {
            entityQuery.addWhereClause("instrumentEventType.userVisible = true");
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<InstrumentEvent> getLazyModelTransitivelyByInstrumentIdAndUser(long instrumentId, User user) {
        final Instrument instrument = find(Instrument.class, instrumentId);
        if (instrument != null) {
            final EntityQuery entityQuery = createEntityQuery();
            entityQuery.addWhereClause("instrument.id in (:instrumentIds)");
            if (user == null || !user.hasRoleImplicit(RoleEnum.INSTRUMENTREADER)) {
                entityQuery.addWhereClause("instrumentEventType.userVisible = true");
            }
            Set<Long> instrumentIds = new HashSet<>();
            instrumentIds.add(instrumentId);
            for (Instrument child : instrument.getDescendants()) {
                instrumentIds.add(child.getId());
            }
            entityQuery.addParameter("instrumentIds", instrumentIds);
            return new BfabricLazyDataModel<>(entityQuery);
        }
        return new BfabricLazyDataModel<>();
    }

    @Override
    public void save(AbstractEntity entity, boolean index) {
        if (entity instanceof InstrumentEvent) {
            InstrumentEvent instrumentEvent = (InstrumentEvent) entity;
            boolean isCreated = !instrumentEvent.isManaged();
            super.save(instrumentEvent, index);
            if (isCreated) {
                Instrument instrument = instrumentEvent.getInstrument();
                if (instrument != null && (!entity.isRenderedSendEmailCheckbox() || entity.isSendMail())) {
                    final Mail mail = new Mail();
                    mail.setParent(instrument);
                    mail.setType(MailTypeEnum.INSTRUMENT_EVENT, Constants.EMPTY_STRING, instrumentEvent.getEventTitle());
                    mail.addRecipient(instrument.getSupervisor());
                    mail.addRecipient(instrument.getAdmin());
                    mail.setInput("instrumentEvent", instrumentEvent);
                    mailSendService.send(mail);
                }
            }
        }
    }
}
