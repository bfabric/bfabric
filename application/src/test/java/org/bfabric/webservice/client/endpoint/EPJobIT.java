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

package org.bfabric.webservice.client.endpoint;

import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveJob;
import org.bfabric.xml.entity.XMLJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPJobIT extends AbstractIT {

    public static final String REQUESTER_ID = "6";

    public static final String WORKUNIT_ID = "23444";

    public static final String WORKUNIT_PARENT_CLASS_NAME = "Workunit";

    public XMLJob createJob() {
        XMLRequestParameterSaveJob xmlRequestSaveJob = new XMLRequestParameterSaveJob();
        xmlRequestSaveJob.setAction(LogActionEnum.UNARCHIVE.getLabel());
        xmlRequestSaveJob.setLog("log");
        xmlRequestSaveJob.setStatus(StatusEnum.NEW.getLabel());
        xmlRequestSaveJob.setRequesterid(REQUESTER_ID);
        xmlRequestSaveJob.setParentid(WORKUNIT_ID);
        xmlRequestSaveJob.setParentclassname(WORKUNIT_PARENT_CLASS_NAME);
        XMLJob job = getSoapClient().getEpJob().getWmSave().save(xmlRequestSaveJob);
        if (job.getErrorreport() != null) {
            throw new SoapClientException("Could not create job: " + job.getErrorreport());
        }
        return job;
    }

    public void deleteJob(Long id) {
        getSoapClient().getEpJob().getWmDelete().delete(id);
    }

    @Test
    public void jobShouldBeCreated() {
        XMLJob job = createJob();
        Assertions.assertNull(job.getErrorreport());
        Assertions.assertNotNull(job.getParentid());
        Assertions.assertNotNull(job.getParentclassname());
        Assertions.assertNotNull(job.getAction());
        Assertions.assertNotNull(job.getLog());
        Assertions.assertNotNull(job.getRequester());
        Assertions.assertNotNull(job.getStatus());

        Assertions.assertEquals(LogActionEnum.UNARCHIVE.name(), job.getAction());
        Assertions.assertEquals(StatusEnum.NEW.name(), job.getStatus());
        Assertions.assertEquals(REQUESTER_ID, job.getRequester().getIdString());
        Assertions.assertEquals(WORKUNIT_ID, job.getParentid());
        Assertions.assertEquals(WORKUNIT_PARENT_CLASS_NAME, job.getParentclassname());

        XMLJob job2 = getSoapClient().getEpJob().getWmRead().getEntity(job.getId());
        Assertions.assertEquals(LogActionEnum.UNARCHIVE.name(), job2.getAction());
        Assertions.assertEquals(StatusEnum.NEW.name(), job2.getStatus());
        Assertions.assertEquals(REQUESTER_ID, job2.getRequester().getIdString());
        Assertions.assertEquals(WORKUNIT_ID, job2.getParentid());
        Assertions.assertEquals(WORKUNIT_PARENT_CLASS_NAME, job2.getParentclassname());

        /* Requires Feeder role to update a job!
        XMLRequestParameterSaveJob xmlRequestSaveJob = new XMLRequestParameterSaveJob();
        xmlRequestSaveJob.setStatus("DONE");
        xmlRequestSaveJob.setId(job.getId());
        XMLJob job3 = getSoapClient().getEpJob().getWmSave().save(xmlRequestSaveJob);
        Assertions.assertEquals(StatusEnum.DONE.name(), job3.getStatus());
        */

        deleteJob(job.getId());
    }
}
