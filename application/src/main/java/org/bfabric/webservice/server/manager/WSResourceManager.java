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

import org.bfabric.Messages;
import org.bfabric.entity.Resource;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFResource;
import org.bfabric.service.ResourceService;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveAbstractEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveResource;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;
import org.bfabric.xml.entity.XMLResource;
import org.bfabric.xml.entity.XMLResourceContent;

public class WSResourceManager extends AbstractWSEntityManager<Resource, XMLResource> {

    @Inject
    private ResourceService resourceService;

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFResource(getInstance(), (XMLRequestParameterSaveResource) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFResource(getInstance(), (XMLRequestParameterSaveResource) aXmlRequestSaveEntity);
    }

    @Override
    protected <T> void isValid(T entity) throws Exception {
        super.isValid(entity);
        handleValidationErrors(resourceService.isValid(getInstance()));
    }

    public <Q extends XMLRequestParameterReadEntity> XMLResponse readContent(Q query, Integer requestedPage, boolean idOnly) {
        XMLResponse xmlResponse = new XMLResponse();
        try {
            xmlResponse.setNumberofpages(getNumberOfPages(query));
            int page = requestedPage != null && requestedPage > 0 ? requestedPage : 1;
            xmlResponse.setPage(page);
            List<Resource> entitiesToRead = getEntities(query, page - 1);
            if (entitiesToRead != null) {
                xmlResponse.setEntitiesonpage(entitiesToRead.size());
                for (Resource entity : entitiesToRead) {
                    XMLResourceContent xmlEntity = new XMLResourceContent(entity);
                    xmlResponse.add(xmlEntity);
                }
            }
        } catch (Exception e) {
            xmlResponse.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
        }
        return xmlResponse;
    }

    @Override
    public void save() {
        resourceService.save(getInstance(), !getInstance().getWorkunit().isProcessing());
    }

    public synchronized <XMLRequestSaveEntity extends XMLRequestParameterSaveAbstractEntity> XMLResponse update(List<XMLRequestSaveEntity> xmlRequestSaveList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterSaveAbstractEntity xmlRequestSaveBaseEntity : xmlRequestSaveList) {
            XMLAbstractBaseEntity xmlEntity;
            try {
                setXmlRequestSaveEntity(xmlRequestSaveBaseEntity);
                applyModificationForm();
                resourceService.update(getInstance());

                performEntityCheckAndSetInstance(getInstance().getId());
                xmlEntity = createNewXmlEntity(getInstance());
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }
}
