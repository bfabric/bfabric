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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.converter.CustomListConverter;
import org.bfabric.interceptors.MeasureCalls;
import org.hibernate.Hibernate;
import org.primefaces.PrimeFaces;
import org.primefaces.component.autocomplete.AutoComplete;

@MeasureCalls
@Named
@ViewScoped
public class AutoCompleteHelper implements Serializable {

    private static final long serialVersionUID = 1;

    public void initializeAutocompleteMultipleForNonRemovableItems(Class<?> clazz, String isRemovableFromAutoCompleteMethodName, String autocompleteId) {
        if (clazz != null && StringHelper.isNotEmpty(isRemovableFromAutoCompleteMethodName)) {
            String autocompleteClientId = StringHelper.isNotEmpty(autocompleteId) ? autocompleteId : (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes()
                .get(Constants.AUTOCOMPLETE_CLIENT_ID);
            if (StringHelper.isNotEmpty(autocompleteClientId)) {
                AutoComplete autoComplete = (AutoComplete) FacesContext.getCurrentInstance().getViewRoot().findComponent(autocompleteClientId);
                if (autoComplete != null) {
                    CustomListConverter customListConverter = (CustomListConverter) autoComplete.getConverter();
                    if (customListConverter != null && !customListConverter.getEntities().isEmpty()) {

                        Set<String> uuids = new HashSet<>();
                        Map<Object, String> entities = customListConverter.getEntities();
                        for (Map.Entry<Object, String> entry : entities.entrySet()) {
                            Object key = ClassHelper.isProxied(entry.getKey().getClass().getName()) ? Hibernate.unproxy(entry.getKey()) : entry.getKey();
                            if (clazz.isAssignableFrom(key.getClass())) {
                                try {
                                    boolean value = Boolean.parseBoolean(clazz.getMethod(isRemovableFromAutoCompleteMethodName).invoke(key, (Object[]) null).toString());
                                    if (!value) {
                                        uuids.add(entities.get(key));
                                    }
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                                }
                            }
                        }

                        StringBuilder javaScriptCommand = new StringBuilder("disableAutocompleteItems('" + autocompleteClientId + "',[");
                        Set<String> uuidsParameters = new HashSet<>();
                        for (String uuid : uuids) {
                            uuidsParameters.add("'" + uuid + "'");
                        }
                        if (!uuidsParameters.isEmpty()) {
                            javaScriptCommand.append(String.join(",", uuidsParameters));
                        }
                        javaScriptCommand.append("])");
                        PrimeFaces.current().executeScript(javaScriptCommand.toString());
                    }
                }
            }
        }
    }
}