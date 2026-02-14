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

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Storage;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.StorageService;

@MeasureCalls
@Named
@ViewScoped
public class StorageManager extends AbstractEntityManager<Storage> {

    private static final long serialVersionUID = 1;

    @Inject
    private StorageService storageService;

    public StorageManager() {
        super(Storage.class);
    }

    public void deselectStorageExecutable() {
        getStorage().setExecutable(null);
    }

    @Produces
    @Named("storage")
    public Storage getStorage() {
        return getInstance();
    }

    public boolean isDeselectStorageExecutableAvailable() {
        return getStorage().getExecutable() != null;
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = storageService.isValid(getStorage());

        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            storageService.save(getStorage());
            return postSave(true, false);
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }
}
