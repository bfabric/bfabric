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

import java.util.Arrays;

import org.bfabric.entity.Application;
import org.bfabric.entity.ApplicationTestLog;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveApplicationTestLog;

public class MFApplicationTestLog extends AbstractMF {

    private final ApplicationTestLog applicationTestLog;

    private final XMLRequestParameterSaveApplicationTestLog xmlRequestSaveApplicationTestLog;

    public MFApplicationTestLog(ApplicationTestLog applicationTestLog, XMLRequestParameterSaveApplicationTestLog xmlRequestSaveApplicationTestLog) {
        this.applicationTestLog = applicationTestLog;
        this.xmlRequestSaveApplicationTestLog = xmlRequestSaveApplicationTestLog;
    }

    @Override
    public synchronized void apply() throws Exception {
        getApplicationTestLog().setApplication(getApplication());
        getApplicationTestLog().setStatus(getStatus());
        getApplicationTestLog().setLog(getLog());
    }

    public Application getApplication() throws InvalidDataException {
        if (getXmlRequestSaveApplicationTestLog().getApplicationid() != null) {
            return (Application) fetch(Application.class, MFHelper.positiveLongValueOf("applicationid", getXmlRequestSaveApplicationTestLog().getApplicationid()));
        }
        return getApplicationTestLog().getApplication();
    }

    public ApplicationTestLog getApplicationTestLog() {
        return applicationTestLog;
    }

    public String getLog() {
        if (getXmlRequestSaveApplicationTestLog().getLog() != null) {
            return getXmlRequestSaveApplicationTestLog().getLog();
        }
        return getApplicationTestLog().getLog();
    }

    public LogStatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSaveApplicationTestLog().getStatus() != null) {
            LogStatusEnum status = LogStatusEnum.value(getXmlRequestSaveApplicationTestLog().getStatus());
            if (status == null) {
                throw new InvalidEnumValueException("status", getXmlRequestSaveApplicationTestLog().getStatus(), CollectionHelper.print(Arrays.asList(LogStatusEnum.values())));
            }
            return status;
        }
        return getApplicationTestLog().getStatus();
    }

    public XMLRequestParameterSaveApplicationTestLog getXmlRequestSaveApplicationTestLog() {
        return xmlRequestSaveApplicationTestLog;
    }
}
