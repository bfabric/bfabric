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
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.entity.InstrumentReadConfiguration;

@Named
@Stateless
public class InstrumentReadConfigurationService extends AbstractService {

    private static final long serialVersionUID = 1;

    public InstrumentReadConfigurationService() {
        super(InstrumentReadConfiguration.class);
    }

    @Override
    public boolean checkUniqueName(AbstractNamedBaseEntity entity) {
        final InstrumentReadConfiguration instrumentReadConfiguration = (InstrumentReadConfiguration) entity;
        return createNamedQuery("InstrumentReadConfiguration.checkUniqueName").setParameter("name", instrumentReadConfiguration.getName())
            .setParameter("id", instrumentReadConfiguration.getId()).setParameter("instrument", instrumentReadConfiguration.getInstrument())
            .setMaxResults(1).getResultList().isEmpty();
    }

    public List<InstrumentReadConfiguration> getInstrumentReadConfigurations(String filterString, Set<InstrumentReadConfiguration> exclude) {
        return (List<InstrumentReadConfiguration>) getFilteredExcluded(filterString, exclude);
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final InstrumentReadConfiguration instrumentReadConfiguration = (InstrumentReadConfiguration) entity;
        return isValidName(instrumentReadConfiguration, Constants.EDIT + ":" + Constants.NAME, Messages.get("notUniqueExceptionForAttribute").replace("{0}", "instrument")
            .replace("{1}", instrumentReadConfiguration.getInstrument().getName()));
    }
}