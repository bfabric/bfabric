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

import java.util.LinkedHashMap;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.ServiceType;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.MultiplexKitService;
import org.bfabric.service.ServiceTypeService;

@MeasureCalls
@Named
@ViewScoped
public class MultiplexKitManager extends AbstractEntityManager<MultiplexKit> {

    private static final long serialVersionUID = 1;

    @Inject
    private MultiplexKitService multiplexKitService;

    public MultiplexKitManager() {
        super(MultiplexKit.class);
    }

    public List<MultiplexKit> getFilteredEnabledIncluding(String filterString) {
        return (List<MultiplexKit>) multiplexKitService.getFilteredEnabledIncludingOrderBy(getMultiplexKit(), filterString, "name");
    }

    @Produces
    @Named("multiplexKit")
    public MultiplexKit getMultiplexKit() {
        return getInstance();
    }

    public List<ServiceType> getPossibleServiceTypes(String filterString) {
        return CDI.current().select(ServiceTypeService.class).get().getPossibleServiceTypes(filterString, getMultiplexKit().getServiceTypes());
    }

    public String saveMultiplexIds() {
        LinkedHashMap<String, String> validationErrorMsg = multiplexKitService.isValid(getMultiplexKit());
        if (validationErrorMsg.isEmpty()) {
            super.save();
            return getShowScreenRedirectURL("multiplexids");
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }
}