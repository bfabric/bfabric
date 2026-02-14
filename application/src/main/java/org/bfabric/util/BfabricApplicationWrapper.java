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

package org.bfabric.util;

import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.primefaces.component.column.ColumnBase;
import org.primefaces.component.columns.ColumnsBase;
import org.primefaces.component.datepicker.DatePicker;

public class BfabricApplicationWrapper extends ApplicationWrapper {

    public BfabricApplicationWrapper(Application wrapped) {
        super(wrapped);
    }

    @Override
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType) {
        final UIComponent component = super.createComponent(context, componentType, rendererType);

        if (component instanceof ColumnBase) {
            final ColumnBase instance = (ColumnBase) component;
            instance.setFilterMatchMode("contains");
        }
        if (component instanceof ColumnsBase) {
            final ColumnsBase instance = (ColumnsBase) component;
            instance.setFilterMatchMode("contains");
        }

        if (component instanceof DatePicker) {
            final DatePicker instance = (DatePicker) component;
            instance.setPattern(ConfigurationHelper.getConfiguration().getDefaultDateTimePattern());
            instance.setYearNavigator(true);
            instance.setShowButtonBar(true);
            instance.setInputStyle("width: 122px");
        }

        /* Comment out if you want to setIncludeViewParams to true by default.
        try {
            component.getClass().getMethod("setIncludeViewParams", boolean.class).invoke(component, true);
        } catch (Exception ignored) {
        }
         */

        return component;
    }
}
