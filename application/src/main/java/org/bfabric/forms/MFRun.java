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

package org.bfabric.forms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReadConfiguration;
import org.bfabric.entity.Run;
import org.bfabric.entity.RunUnit;
import org.bfabric.entity.RunUnitType;
import org.bfabric.entity.User;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveRun;

public class MFRun extends AbstractMF {

    private final Run run;

    private final XMLRequestParameterSaveRun xmlRequestSaveRun;

    public MFRun(Run run, XMLRequestParameterSaveRun xmlRunRequestSave) {
        this.run = run;
        this.xmlRequestSaveRun = xmlRunRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getRun().setName(getName());
        getRun().setDescription(getDescription());
        getRun().setDataFolder(getDataFolder());
        getRun().setRunUnit(getRunUnit());
        getRun().setInstrument(getInstrument());
        getRun().setSupervisor(getSupervisor());
        getRun().setInstrumentReadConfiguration(getInstrumentReadConfiguration());
        getRun().changeStatus(getStatus());
        getRun().setServerLocation(getServerLocation());
        getRun().setCustomAttributes(getXmlRequestSaveRun().getCustomattribute());
    }

    public List<Container> getContainers() throws InvalidDataException {
        if (getXmlRequestSaveRun().getContainerid() != null) {
            Set<Container> containers = new HashSet<>();
            for (String containerId : getXmlRequestSaveRun().getContainerid()) {
                if (!containerId.isEmpty()) {
                    Container container = (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", containerId));
                    containers.add(container);
                }
            }
            return CollectionHelper.asList(containers);
        }
        return getRun().getContainers();
    }

    public String getDataFolder() {
        if (getXmlRequestSaveRun().getDatafolder() != null) {
            return getXmlRequestSaveRun().getDatafolder();
        }
        return getRun().getDataFolder();
    }

    public String getDescription() {
        if (getXmlRequestSaveRun().getDescription() != null) {
            return getXmlRequestSaveRun().getDescription();
        }
        return getRun().getDescription();
    }

    public Instrument getInstrument() throws InvalidDataException {
        if (getRun().getId() == 0 || getXmlRequestSaveRun().getInstrumentid() != null) {
            MFHelper.checkNotNull("instrumentid", getXmlRequestSaveRun().getInstrumentid());
            return (Instrument) fetch(Instrument.class, MFHelper.positiveLongValueOf("instrumentid", getXmlRequestSaveRun().getInstrumentid()));
        }
        return getRun().getInstrument();
    }

    public InstrumentReadConfiguration getInstrumentReadConfiguration() throws InvalidDataException {
        if (getRun().getId() == 0 || getXmlRequestSaveRun().getInstrumentreadconfigurationid() != null) {
            MFHelper.checkNotNull("rununittypeid", getXmlRequestSaveRun().getInstrumentreadconfigurationid());
            return (InstrumentReadConfiguration) fetch(InstrumentReadConfiguration.class, MFHelper
                .positiveLongValueOf("rununittypeid", getXmlRequestSaveRun().getInstrumentreadconfigurationid()));
        }
        return getRun().getInstrumentReadConfiguration();
    }

    public String getName() {
        if (getXmlRequestSaveRun().getName() != null) {
            return getXmlRequestSaveRun().getName();
        }
        return getRun().getName();
    }

    public Run getRun() {
        return run;
    }

    public RunUnit getRunUnit() throws InvalidDataException {
        if (getRun().getId() == 0 && getXmlRequestSaveRun().getRununittypeid() != null) {
            MFHelper.checkNotNull("rununittypeid", getXmlRequestSaveRun().getRununittypeid());
            RunUnitType runUnitType = (RunUnitType) fetch(RunUnitType.class, MFHelper.positiveLongValueOf("rununittypeid", getXmlRequestSaveRun().getRununittypeid()));
            return new RunUnit(runUnitType, getRun());
        }
        return getRun().getRunUnit();
    }

    public String getServerLocation() {
        if (getXmlRequestSaveRun().getServerlocation() != null) {
            return getXmlRequestSaveRun().getServerlocation();
        }
        return getRun().getServerLocation();
    }

    public StatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSaveRun().getStatus() != null) {
            return StatusEnum.value(getXmlRequestSaveRun().getStatus());
        }
        return getRun().getStatus();
    }

    public User getSupervisor() throws InvalidDataException {
        if (getRun().getId() == 0 || getXmlRequestSaveRun().getSupervisorid() != null) {
            MFHelper.checkNotNull("supervisorid", getXmlRequestSaveRun().getSupervisorid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSaveRun().getSupervisorid()));
        }
        return getRun().getSupervisor();
    }

    public XMLRequestParameterSaveRun getXmlRequestSaveRun() {
        return xmlRequestSaveRun;
    }
}
