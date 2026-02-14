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

import org.bfabric.entity.RunUnit;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFRunUnit;
import org.bfabric.service.RunUnitService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveRunUnit;
import org.bfabric.xml.entity.XMLRunUnit;

public class WSRunUnitManager extends AbstractWSEntityManager<RunUnit, XMLRunUnit> {

    @Inject
    private RunUnitService runUnitService;

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFRunUnit(getInstance(), (XMLRequestParameterSaveRunUnit) aXmlRequestSaveEntity);
    }

    @Override
    protected <T> void isValid(T entity) throws Exception {
        super.isValid(entity);
        handleValidationErrors(runUnitService.isValid(getInstance()));
    }

    @Override
    public void save() {
        runUnitService.save(getInstance(), false);
    }
}
