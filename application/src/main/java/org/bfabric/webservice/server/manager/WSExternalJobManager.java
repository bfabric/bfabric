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

package org.bfabric.webservice.server.manager;

import java.util.List;

import javax.inject.Inject;

import org.bfabric.entity.ExternalJob;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFExternalJob;
import org.bfabric.service.ExternalJobService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveExternalJob;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLExternalJob;

public class WSExternalJobManager extends AbstractWSEntityManager<ExternalJob, XMLExternalJob> {

    @Inject
    private ExternalJobService externalJobService;

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFExternalJob(getInstance(), (XMLRequestParameterSaveExternalJob) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFExternalJob(getInstance(), (XMLRequestParameterSaveExternalJob) aXmlRequestSaveEntity);
    }

    @Override
    public <Q extends XMLRequestParameterReadEntity> XMLResponse read(Q query, Integer requestedPage, boolean idOnly) {
        XMLResponse xmlResponse = new XMLResponse();
        try {
            xmlResponse.setNumberofpages(getNumberOfPages(query));
            int page = requestedPage != null && requestedPage > 0 ? requestedPage : 1;
            xmlResponse.setPage(page);
            List<ExternalJob> entitiesToRead = getEntities(query, page - 1);
            if (entitiesToRead != null) {
                xmlResponse.setEntitiesonpage(entitiesToRead.size());
                for (ExternalJob externalJob : entitiesToRead) {
                    setInstance(externalJob);
                    externalJob.setChecked(query.fulldetails);
                    if (entitiesToRead.size() == 1) {
                        externalJob.appendReadLogInfo();
                        super.save();
                    }
                    XMLExternalJob xmlExternalJob = new XMLExternalJob(externalJob);
                    xmlResponse.add(xmlExternalJob);
                }
            }
        } catch (Exception e) {
            xmlResponse.setErrorreport(e.getMessage());
        }
        return xmlResponse;
    }

    @Override
    public void save() {
        externalJobService.save(getInstance(), false);
    }
}
