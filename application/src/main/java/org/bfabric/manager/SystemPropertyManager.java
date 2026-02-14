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

import org.bfabric.entity.DeployerContextProperty;
import org.bfabric.entity.EnvironmentContextProperty;
import org.bfabric.entity.InstanceContextProperty;
import org.bfabric.entity.SystemProperty;
import org.bfabric.enums.ContextPropertyDiscriminator;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ConfService;

@MeasureCalls
@Named
@ViewScoped
public class SystemPropertyManager extends AbstractEntityManager<SystemProperty> {

    private static final long serialVersionUID = 1;

    @Inject
    private ConfManager confManager;

    @Inject
    private ConfService confService;

    private ContextPropertyDiscriminator contextPropertyDiscriminator = ContextPropertyDiscriminator.D;

    private String contextPropertyValue;

    public SystemPropertyManager() {
        super(SystemProperty.class);
    }

    @Override
    public String cancel() {
        return getListScreenRedirectURL();
    }

    public ContextPropertyDiscriminator getContextPropertyDiscriminator() {
        return contextPropertyDiscriminator;
    }

    public String getContextPropertyValue() {
        return contextPropertyValue;
    }

    @Override
    public String getListScreenRedirectURL() {
        return "/systemproperty/list.html?faces-redirect=true";
    }

    @Produces
    @Named("systemProperty")
    public SystemProperty getSystemProperty() {
        return getInstance();
    }

    public String remove(SystemProperty systemProperty) {
        setInstance(systemProperty);
        remove();
        return remove();
    }

    @Override
    public String save() {
        String ret = super.save();
        confManager.setConfiguration();
        return ret;
    }

    public String saveContextProperty() {
        switch (getContextPropertyDiscriminator()) {
        case D:
            confService.setContext(getContextPropertyValue(), Boolean.FALSE, DeployerContextProperty.class);
            break;
        case E:
            confService.setContext(getContextPropertyValue(), Boolean.FALSE, EnvironmentContextProperty.class);
            break;
        case I:
            confService.setContext(getContextPropertyValue(), Boolean.FALSE, InstanceContextProperty.class);
            break;
        default:
            break;
        }
        return getListScreenRedirectURL();
    }

    public void setContextPropertyDiscriminator(ContextPropertyDiscriminator contextPropertyDiscriminator) {
        this.contextPropertyDiscriminator = contextPropertyDiscriminator;
    }

    public void setContextPropertyValue(String contextPropertyValue) {
        this.contextPropertyValue = contextPropertyValue;
    }
}