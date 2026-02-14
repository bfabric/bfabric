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

import org.bfabric.entity.Consumable;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFConsumable;
import org.bfabric.service.ConsumableService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveConsumable;
import org.bfabric.xml.entity.XMLConsumable;

public class WSConsumableManager extends AbstractWSEntityManager<Consumable, XMLConsumable> {

    @Inject
    private ConsumableService consumableService;

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFConsumable(getInstance(), (XMLRequestParameterSaveConsumable) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFConsumable(getInstance(), (XMLRequestParameterSaveConsumable) aXmlRequestSaveEntity);
    }

    @Override
    protected <T> void isValid(T entity) throws Exception {
        super.isValid(entity);
        handleValidationErrors(consumableService.isValid(getInstance()));
    }

    @Override
    public void save() {
        consumableService.save(getInstance(), false);
    }
}