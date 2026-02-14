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

import javax.inject.Inject;

import org.bfabric.entity.Application;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFApplication;
import org.bfabric.service.ApplicationService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveApplication;
import org.bfabric.xml.entity.XMLApplication;

public class WSApplicationManager extends AbstractWSEntityManager<Application, XMLApplication> {

    @Inject
    private ApplicationService applicationService;

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFApplication(getInstance(), (XMLRequestParameterSaveApplication) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFApplication(getInstance(), (XMLRequestParameterSaveApplication) aXmlRequestSaveEntity);
    }

    @Override
    public <T> void isValid(T entity) throws Exception {
        if (!getInstance().isWebApp()) {
            getAdditionalFieldsToExcludeFromValidation().add("webUrl");
        }
        super.isValid(entity);
        String errorMessage = applicationService.isValid(getInstance());
        if (errorMessage != null) {
            throw new InvalidDataException(errorMessage);
        }
    }

    @Override
    public void save() {
        applicationService.save(getInstance());
    }
}
