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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.entity.MultiplexId;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.MultiplexIdService;

@MeasureCalls
@Named
@ViewScoped
public class MultiplexIdManager extends AbstractOrderedEnabledNamedBaseEntityManager<MultiplexId> {

    private static final long serialVersionUID = 1;

    public MultiplexIdManager() {
        super(MultiplexId.class);
    }

    @Produces
    @Named("multiplexId")
    public MultiplexId getMultiplexId() {
        return getInstance();
    }

    public void multiplexKitChangedListener(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            MultiplexKit newMultiplexKit = (MultiplexKit) event.getNewValue();
            if (newMultiplexKit.isCombinedMultiplexId()) {
                getMultiplexId().setPlateNumber(null);
            }
        } else {
            getMultiplexId().setPlateNumber(null);
        }
        // Set default to true as usually the majority of multiplex ids within a multiplex kit of combined multiplex ids have combinedXAxis set to true.
        getMultiplexId().setCombinedXAxis(true);
    }

    @Override
    public String save() {
        return validateAndSave(CDI.current().select(MultiplexIdService.class).get());
    }
}