/*
 *
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

import java.util.LinkedHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.InstrumentReservationSettingService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class InstrumentReservationSettingManager extends AbstractEntityManager<InstrumentReservationSetting> {

    private static final long serialVersionUID = 1;

    @Param
    private Long instrumentId;

    @Inject
    private InstrumentReservationSettingService instrumentReservationSettingService;

    public InstrumentReservationSettingManager() {
        super(InstrumentReservationSetting.class);
    }

    public void bookingAheadMaxDurationHelperChanged() {
        getInstrumentReservationSetting().setBookingAheadMaxDuration(getInstrumentReservationSetting().getBookingAheadMaxDurationHelper().getBeyondTimeDuration());
    }

    public void bookingAheadMinDurationHelperChanged() {
        getInstrumentReservationSetting().setBookingAheadMinDuration(getInstrumentReservationSetting().getBookingAheadMinDurationHelper().getBeyondTimeDuration());
    }

    @Override
    protected InstrumentReservationSetting createInstance() {
        InstrumentReservationSetting instrumentReservationSetting = super.createInstance();
        if (instrumentId != null) {
            Instrument instrument = entityService.find(Instrument.class, instrumentId);
            if (instrument != null) {
                if (instrument.getLastReservationSetting() != null) {
                    try {
                        instrumentReservationSetting = instrument.getLastReservationSetting().clone();
                        instrumentReservationSetting.setPreviousSetting(instrument.getLastReservationSetting());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                } else {
                    instrumentReservationSetting = new InstrumentReservationSetting(instrument);
                }
                setInstance(instrumentReservationSetting);
            }
        }
        return instrumentReservationSetting;
    }

    @Produces
    @Named("instrumentReservationSetting")
    public InstrumentReservationSetting getInstrumentReservationSetting() {
        return getInstance();
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (isManaged()) {
            if (getInstrumentReservationSetting().getValidFrom() != null) {
                getInstrumentReservationSetting().setPreviousSetting(instrumentReservationSettingService
                    .getSettingByInstrumentAndDate(getInstrumentReservationSetting().getInstrument(), getInstrumentReservationSetting().getValidFrom().minusDays(1)));
            }
            if (getInstrumentReservationSetting().getValidUntil() != null) {
                getInstrumentReservationSetting().setNextSetting(instrumentReservationSettingService
                    .getSettingByInstrumentAndDate(getInstrumentReservationSetting().getInstrument(), getInstrumentReservationSetting().getValidUntil().plusDays(1)));
            }
        }
    }

    @Override
    public String remove() {
        try {
            instrumentId = getInstrumentReservationSetting().getInstrument().getId();
            instrumentReservationSettingService.remove(getInstrumentReservationSetting());
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted"));
            return createRedirectShowScreenURL(getInstrumentReservationSetting().getInstrument(), "reservationsettings", null);
        } catch (final Exception e) {
            e.printStackTrace();
            getFacesMessagesManager().printError(Messages.get("eventDeletionFailed"));
        }
        return null;
    }

    @Override
    public String save() {
        String ret = null;
        LinkedHashMap<String, String> validationErrorMsg = instrumentReservationSettingService.save(getInstrumentReservationSetting());
        if (validationErrorMsg.isEmpty()) {
            ret = getRedirectURLAfterSave();
        } else {
            handleValidationErrors(validationErrorMsg);
        }
        return ret;
    }
}



