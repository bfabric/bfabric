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

import org.bfabric.entity.Container;
import org.bfabric.entity.Executable;
import org.bfabric.entity.ExternalJob;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.ExternalJobClientClassEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveExternalJob;

public class MFExternalJob extends AbstractMF {

    private final ExternalJob externalJob;

    private final XMLRequestParameterSaveExternalJob xmlRequestSaveExternalJob;

    public MFExternalJob(ExternalJob externalJob, XMLRequestParameterSaveExternalJob xmlRequestSaveExternalJob) {
        this.externalJob = externalJob;
        this.xmlRequestSaveExternalJob = xmlRequestSaveExternalJob;
    }

    @Override
    public synchronized void apply() throws Exception {
        getExternalJob().setClientEntityClassName(getClientEntityClass());
        getExternalJob().setClientEntityId(getClientEntityId());
        getExternalJob().setExecutable(getExecutable());
        getExternalJob().setAction(getAction());
        getExternalJob().changeStatus(getStatus());
        setLogthis();
    }

    public String getAction() {
        if (getXmlRequestSaveExternalJob().getAction() != null) {
            return getXmlRequestSaveExternalJob().getAction();
        }
        return getExternalJob().getAction();
    }

    public ExternalJobClientClassEnum getClientEntityClass() throws InvalidDataException {
        if (getExternalJob().getId() == 0) {
            ExternalJobClientClassEnum clientEntityClass = null;

            if (getXmlRequestSaveExternalJob().getContainerid() != null) {
                clientEntityClass = ExternalJobClientClassEnum.CONTAINER;
            } else if (getXmlRequestSaveExternalJob().getUserid() != null) {
                clientEntityClass = ExternalJobClientClassEnum.USER;
            } else if (getXmlRequestSaveExternalJob().getWorkunitid() != null) {
                clientEntityClass = ExternalJobClientClassEnum.WORKUNIT;
            }

            if (clientEntityClass == null) {
                throw new InvalidDataException("No container, user or workunit id specified!");
            }

            return clientEntityClass;
        }
        return ExternalJobClientClassEnum.getExternalJobClientClassEnum(getExternalJob().getClientEntityClassName());
    }

    public long getClientEntityId() throws Exception {
        if (getExternalJob().getId() == 0) {
            ExternalJobClientClassEnum clientEntityClass = getClientEntityClass();

            switch (clientEntityClass) {
            case WORKUNIT:
                Workunit workunit = (Workunit) fetch(Workunit.class, MFHelper.positiveLongValueOf("workunitid", getXmlRequestSaveExternalJob().getWorkunitid()));

                if (!workunit.getContainer().isExtensible()) {
                    throw new InvalidDataException("Workunit's container is not extensible!");
                }

                if (workunit.isAvailable()) {
                    throw new InvalidDataException("The status of the specified workunit is " + WorkunitStatusEnum.AVAILABLE + ", therefore no external job can be attached to it!");
                }

                return workunit.getId();
            case CONTAINER:
                Container container = (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveExternalJob().getContainerid()));

                if (!container.isExtensible()) {
                    throw new InvalidDataException("Container is not extensible!");
                }

                return container.getId();
            case USER:
                User user = (User) fetch(User.class, MFHelper.positiveLongValueOf("userid", getXmlRequestSaveExternalJob().getUserid()));
                return user.getId();
            default:
                return 0;
            }
        }
        return getExternalJob().getClientEntityId();
    }

    public Executable getExecutable() throws InvalidDataException {
        if (getXmlRequestSaveExternalJob().getExecutableid() != null) {
            Executable executable = (Executable) fetch(Executable.class, MFHelper.positiveLongValueOf("executableid", getXmlRequestSaveExternalJob().getExecutableid()));
            if (executable != null && getExternalJob().getExecutable() != null && !executable.equals(getExternalJob().getExecutable())) {
                throw new InvalidDataException("The external job is already connected to another executable with the id " + getExternalJob().getExecutable().getId() + "!");
            }
            return (Executable) fetch(Executable.class, MFHelper.positiveLongValueOf("executableid", getXmlRequestSaveExternalJob().getExecutableid()));
        }
        return getExternalJob().getExecutable();
    }

    public ExternalJob getExternalJob() {
        return externalJob;
    }

    public StatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSaveExternalJob().getStatus() != null) {
            if (getExternalJob().getId() != 0) {
                getExternalJob().appendUpdateLogInfo("Status set to " + getXmlRequestSaveExternalJob().getStatus() + ".");
            }
            return StatusEnum.value(getXmlRequestSaveExternalJob().getStatus(), ExternalJob.class);
        }
        return getExternalJob().getStatus();
    }

    public XMLRequestParameterSaveExternalJob getXmlRequestSaveExternalJob() {
        return xmlRequestSaveExternalJob;
    }

    public void setLogthis() {
        getExternalJob().appendUpdateLogInfo(getXmlRequestSaveExternalJob().getLogthis());
    }
}