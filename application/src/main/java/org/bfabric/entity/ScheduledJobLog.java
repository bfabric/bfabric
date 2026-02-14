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

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.JobEnum;
import org.bfabric.enums.LogStatusEnum;

@Entity
@XmlRootElement
public class ScheduledJobLog extends AbstractLogDuration implements ShowScreen {

    private static final long serialVersionUID = 1;

    public ScheduledJobLog() {
    }

    public ScheduledJobLog(JobEnum createdBy) {
        setCreatedBy(createdBy);
    }

    public ScheduledJobLog(JobEnum jobEnum, LogStatusEnum status) {
        setCreatedBy(jobEnum);
        setStatus(status);
    }

    public ScheduledJobLog(JobEnum jobEnum, LogStatusEnum logStatus, String log) {
        setCreatedBy(jobEnum);
        setStatus(logStatus);
        setLog(log);
    }

    @Transient
    @Override
    public boolean isDeletable() {
        return false;
    }

    public String logDeletedObjects(Map<String, List<?>> deletedObjects) {
        int deleted = deletedObjects.values().stream().mapToInt(List::size).sum();
        return deleted > 0 ? String.valueOf(deleted) : null;
    }

    public void setCreatedBy(final JobEnum createdBy) {
        super.setCreatedBy(createdBy.name());
    }
}
