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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;

@Entity
@XmlRootElement
public class JobLog extends AbstractLog {

    private static final long serialVersionUID = 1;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private LogActionEnum action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jobid")
    @NotNull
    @XmlIDREF
    private Job job;

    public JobLog() {
    }

    public JobLog(Job job, String message, LogActionEnum action, LogStatusEnum status) {
        setJob(job);
        setLog(message);
        setAction(action);
        setStatus(status);
    }

    @Override
    public JobLog clone() throws CloneNotSupportedException {
        return (JobLog) super.clone();
    }

    public LogActionEnum getAction() {
        return action;
    }

    @Transient
    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "action", getJob().getAction());
        addEntityInfoItem(summary, "status", getJob().getStatus());
        addEntityInfoItem(summary, "log", getJob().getLog());
        if (getJob() != null) {
            addEntityInfoItem(summary, "job", getJob().getDisplayName());
        }
        return summary.toString();
    }

    public Job getJob() {
        return job;
    }

    public void setAction(LogActionEnum action) {
        this.action = action;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}