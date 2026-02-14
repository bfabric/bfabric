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

package org.bfabric.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.UserService;

@Entity
@XmlRootElement
public class InstrumentEvent extends AbstractDescriptionBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @XmlElement
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentId")
    @NotNull
    @XmlIDREF
    private Instrument instrument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentEventTypeId")
    @NotNull
    @XmlIDREF
    private InstrumentEventType instrumentEventType;

    @Enumerated(EnumType.STRING)
    @XmlElement
    private LogStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @XmlIDREF
    private User user;

    public InstrumentEvent() {
    }

    @Override
    public InstrumentEvent clone() throws CloneNotSupportedException {
        return (InstrumentEvent) super.clone();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.INSTRUMENTMANAGER;
    }

    public List<User> getEmployeesFilteredIncludingUser(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getUser());
    }

    public String getEventInfo() {
        return getEventInfo(true);
    }

    public String getEventInfo(boolean full) {
        StringBuilder info = new StringBuilder();
        if (getInstrumentEventType() != null) {
            info.append(getInstrumentEventType().getName()).append(": ");
        }
        if (getInstrument() != null) {
            info.append(getInstrument().getDisplayName()).append(" ");
        }
        if (full) {
            if (getUser() != null) {
                info.append(getUser().getFullName()).append(" ");
            }
            if (getDateTime() != null) {
                info.append(getDateTime().format(Constants.DATETIME_FORMATTER)).append(" ");
            }
            if (getStatus() != null) {
                info.append(getStatus()).append(" ");
            }
        }
        return info.toString();
    }

    public String getEventTitle() {
        return getEventInfo(false);
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public InstrumentEventType getInstrumentEventType() {
        return instrumentEventType;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.INSTRUMENT_EVENT_NOTE;
    }

    public String getRowStyleClass(Instrument instrument) {
        return instrument != null && !instrument.equals(getInstrument()) ? Constants.BACKGROUND_COLOR_RED : "";
    }

    public LogStatusEnum getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean isCreatable() {
        return super.isCreatable() || getInstrument() != null && getInstrument().isUserBookable();
    }

    @Override
    public boolean isDeletable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isLastEvent() {
        InstrumentEvent lastEvent = getInstrument().getInstrumentEvents().stream().findFirst().orElse(null);
        return lastEvent != null && lastEvent.equals(this);
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.INSTRUMENTREADER) || getInstrument() != null && getInstrument().isUserVisible() && getInstrumentEventType() != null && getInstrumentEventType().isUserVisible();
    }

    public boolean isRenderedSendEmailCheckbox() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatable() {
        return isReadable() && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isCreator() && isLastEvent());
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentEventType(InstrumentEventType instrumentEventType) {
        this.instrumentEventType = instrumentEventType;
    }

    public void setStatus(LogStatusEnum status) {
        this.status = status;
    }

    public void setUser(User user) {
        this.user = user;
    }
}