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

package org.bfabric.list;

import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Role;
import org.bfabric.entity.SystemProperty;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.ConfService;
import org.bfabric.service.SystemPropertyService;

@Named
@ViewScoped
public class SystemPropertyList extends AbstractList<SystemProperty> {

    private static final long serialVersionUID = 1;

    @Inject
    private ConfService confService;

    private boolean filterActiveOnly;

    @Inject
    private SystemPropertyService systemPropertyService;

    public void filterActiveOnlyChanged(ValueChangeEvent event) {
        setFilterActiveOnly((Boolean) event.getNewValue());
    }

    public List<SystemProperty> getProperties() {
        return filterActiveOnly ? confService.getActiveSystemProperties(getConfiguration().getEnvironment().getValue(), getConfiguration().getDeployer().getValue(), getConfiguration().getInstance()
            .getValue()) : getService().getProperties();
    }

    @Override
    protected SystemPropertyService getService() {
        return systemPropertyService;
    }

    @CachedMethodResult
    public List<SystemProperty> getSystemPropertiesByRoles(Set<Role> roles) {
        return getService().getSystemPropertiesByRoles(roles);
    }

    public boolean isFilterActiveOnly() {
        return filterActiveOnly;
    }

    public void setFilterActiveOnly(boolean filterActiveOnly) {
        this.filterActiveOnly = filterActiveOnly;
    }
}