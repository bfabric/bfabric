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
import java.util.Set;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Job;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveJob;

public class MFJob extends AbstractMF {

    private final Job job;

    private final XMLRequestParameterSaveJob xmlRequestSaveJob;

    public MFJob(Job job, XMLRequestParameterSaveJob xmlRequestParameterSaveJob) {
        this.job = job;
        this.xmlRequestSaveJob = xmlRequestParameterSaveJob;
    }

    @Override
    public synchronized void apply() throws Exception {
        getJob().setParent(getParent());
        getJob().setUrl(getUrl());
        getJob().setRequester(getRequester());
        getJob().setAction(getAction());
        getJob().changeStatus(getStatus());
        getJob().setWorkunits(getWorkunits());
        setLog();
    }

    public LogActionEnum getAction() throws InvalidDataException, InvalidEnumValueException {
        if (getXmlRequestSaveJob().getAction() != null) {
            return LogActionEnum.value(getXmlRequestSaveJob().getAction());
        }
        return getJob().getAction();
    }

    public Job getJob() {
        return job;
    }

    public String getLog() {
        if (getXmlRequestSaveJob().getLog() != null) {
            return getXmlRequestSaveJob().getLog();
        }
        return getJob().getLog();
    }

    public AbstractEntity getParent() throws InvalidDataException {
        if (!getJob().isManaged() || getXmlRequestSaveJob().getParentid() != null) {
            Long parentId = MFHelper.positiveLongValueOf("parentid", getXmlRequestSaveJob().getParentid());
            AbstractEntity ret = getJob().getParent();
            if (getParentClassName() != null && (ret == null || !getParentClassName().equals(ret.getTrimmedClassName()) || !parentId.equals(ret.getId()))) {
                ret = fetch(getParentClassName(), parentId);
            }
            return ret;
        } else {
            return getJob().getParent();
        }
    }

    public String getParentClassName() throws InvalidDataException {
        if (getXmlRequestSaveJob().getParentclassname() != null) {
            return MFHelper.getEntityClass("parentclassname", getXmlRequestSaveJob().getParentclassname()).getSimpleName();
        }
        return getJob().getParentClassName();
    }

    public User getRequester() throws InvalidDataException {
        if (getXmlRequestSaveJob().getRequesterid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("requesterid", getXmlRequestSaveJob().getRequesterid()));
        }
        return getJob().getRequester() != null ? getJob().getRequester() : getIdentityService().getCurrentUser();
    }

    public StatusEnum getStatus() throws InvalidDataException, InvalidEnumValueException {
        if (getXmlRequestSaveJob().getStatus() != null) {
            return StatusEnum.value(getXmlRequestSaveJob().getStatus());
        }
        return getJob().getStatus();
    }

    public String getUrl() {
        if (getXmlRequestSaveJob().getUrl() != null) {
            return getXmlRequestSaveJob().getUrl();
        }
        return getJob().getUrl();
    }

    public Set<Workunit> getWorkunits() throws InvalidDataException {
        if (getXmlRequestSaveJob().getWorkunitid() != null && !getXmlRequestSaveJob().getWorkunitid().isEmpty()) {
            Set<Workunit> workunits = new HashSet<>();
            for (String workunitid : getXmlRequestSaveJob().getWorkunitid()) {
                if (workunitid != null && !workunitid.isEmpty()) {
                    workunits.add((Workunit) fetch(Workunit.class, MFHelper.positiveLongValueOf("workunitid", workunitid)));
                }
            }
            return workunits;
        }
        return getJob().getWorkunits();
    }

    public XMLRequestParameterSaveJob getXmlRequestSaveJob() {
        return xmlRequestSaveJob;
    }

    public void setLog() {
        if (getXmlRequestSaveJob().getLog() != null) {
            getJob().appendLog(getXmlRequestSaveJob().getLog());
        } else if (getXmlRequestSaveJob().getLogthis() != null) {
            getJob().appendLog(getXmlRequestSaveJob().getLogthis());
        }
    }
}
