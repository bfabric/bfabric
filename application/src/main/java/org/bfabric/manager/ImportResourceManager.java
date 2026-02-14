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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Application;
import org.bfabric.entity.ImportResource;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ApplicationService;
import org.bfabric.service.ImportResourceService;

@MeasureCalls
@Named
@ViewScoped
public class ImportResourceManager extends AbstractEntityManager<ImportResource> {

    private static final long serialVersionUID = 1;

    @Inject
    private ApplicationService applicationService;

    @Inject
    private ImportResourceService importResourceService;

    public ImportResourceManager() {
        super(ImportResource.class);
    }

    @Override
    protected ImportResource createInstance() {
        ImportResource importResource = super.createInstance();
        importResource.setContainer(getContextContainer());
        return importResource;
    }

    public List<Application> getImportApplications(String filterString) {
        Set<Application> exclude = null;
        if (getImportResource().getApplication() != null) {
            exclude = new HashSet<>();
            exclude.add(getImportResource().getApplication());
        }
        return applicationService.getFilteredImportResourceApplicationsExcluding(filterString, exclude);
    }

    @Produces
    @Named("importResource")
    public ImportResource getImportResource() {
        return getInstance();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = importResourceService.isValid(getImportResource());

        if (validationErrorMsg.isEmpty()) {
            if (!isManaged()) {
                setInstance(importResourceService.getOverwrittenEquivalentImportResourceIfExists(getImportResource()));
            }
            return super.save();
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }
}
