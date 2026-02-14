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

package org.bfabric.manager;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentDataPackage;
import org.bfabric.entity.RunUnitType;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.InstrumentDataPackageService;
import org.bfabric.service.InstrumentService;

@MeasureCalls
@Named
@ViewScoped
public class InstrumentDataPackageManager extends AbstractOrderedEnabledNamedBaseEntityManager<InstrumentDataPackage> {

    private static final long serialVersionUID = 1;

    @Inject
    private InstrumentService instrumentService;

    public InstrumentDataPackageManager() {
        super(InstrumentDataPackage.class);
    }

    @Produces
    @Named("instrumentDataPackage")
    public InstrumentDataPackage getInstrumentDataPackage() {
        return getInstance();
    }

    public List<Instrument> getInstruments(String filterString) {
        return (List<Instrument>) instrumentService.getFilteredEnabledIncludingOrderBy(getInstrumentDataPackage().getInstrument(), filterString, null);
    }

    @CachedMethodResult
    public List<RunUnitType> getRunUnitTypesEnabledIncluding(RunUnitType including) {
        Set<RunUnitType> runUnitTypesEnabled = new HashSet<>();
        if (including != null) {
            runUnitTypesEnabled.add(including);
        }
        if (getInstrumentDataPackage().getInstrument() != null) {
            for (RunUnitType runUnitType : getInstrumentDataPackage().getInstrument().getRunUnitTypes()) {
                if (runUnitType.isEnabled()) {
                    runUnitTypesEnabled.add(runUnitType);
                }
            }
        }
        return runUnitTypesEnabled.stream().sorted(Comparator.comparing(RunUnitType::getOrderPosition)).collect(Collectors.toList());
    }

    @Override
    public String save() {
        return validateAndSave(CDI.current().select(InstrumentDataPackageService.class).get());
    }
}
