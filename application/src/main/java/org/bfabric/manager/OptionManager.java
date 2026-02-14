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
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Option;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OptionService;
import org.bfabric.util.ClassHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class OptionManager extends AbstractEntityManager<Option> {

    private static final long serialVersionUID = 1;

    @Param
    private String parentClassName;

    @Param
    private Long parentId;

    public OptionManager() {
        super(Option.class);
    }

    @Override
    protected Option createInstance() {
        final Option option = super.createInstance();
        if (option != null && parentId != null && parentClassName != null) {
            option.setParent(entityService.find((Class<? extends AbstractEntity>) ClassHelper.getClassByName(parentClassName), parentId));
        }
        return option;
    }

    @Produces
    @Named("option")
    public Option getOption() {
        return getInstance();
    }

    @Override
    public String save() {
        return validateAndSave(CDI.current().select(OptionService.class).get());
    }

    public String switchEnabled() {
        getOption().switchEnabled();
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(getOption().isEnabled() ? Messages.get("enabled") : Messages.get("disabled"));
        return getShowScreenRedirectURL();
    }
}