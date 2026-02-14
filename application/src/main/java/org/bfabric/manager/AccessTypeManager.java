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
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.AccessType;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.AccessTypeService;

@MeasureCalls
@Named
@ViewScoped
public class AccessTypeManager extends AbstractOrderedEnabledNamedBaseEntityManager<AccessType> {

    private static final long serialVersionUID = 1;

    @Inject
    private AccessTypeService accessTypeService;

    public AccessTypeManager() {
        super(AccessType.class);
    }

    @Produces
    @Named("accessType")
    public AccessType getAccessType() {
        return getInstance();
    }

    @Override
    public String remove() {
        accessTypeService.remove(getAccessType());
        return getRedirectURLAfterRemove();
    }

    @Override
    public String save() {
        return validateAndSave(accessTypeService);
    }
}