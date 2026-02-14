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

package org.bfabric.service;

import java.util.LinkedHashMap;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.CostCentre;

@Named
@Stateless
public class CostCentreService extends AbstractService {

    private static final long serialVersionUID = 1;

    public CostCentreService() {
        super(CostCentre.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final CostCentre costCentre = (CostCentre) entity;
        return createNamedQuery("CostCentre.checkUniqueName").setParameter("name", costCentre.getName()).setParameter("id", costCentre.getId()).setParameter("booker", costCentre.getBooker())
            .setMaxResults(1).getResultList().isEmpty();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final CostCentre costCentre = (CostCentre) entity;
        return isValidName(costCentre, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "booker")
            .replace("{1}", costCentre.getBooker().getName()));
    }
}