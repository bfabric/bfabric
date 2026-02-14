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

package org.bfabric.converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Country;
import org.bfabric.service.EntityService;
import org.bfabric.util.ClassHelper;
import org.omnifaces.converter.ListConverter;

@ViewScoped
@FacesConverter("customListConverter")
public class CustomListConverter extends ListConverter implements Serializable {

    private static final long serialVersionUID = 1;

    private final Map<Object, String> entities = new HashMap<>();

    @Inject
    private EntityService entityService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String uuid) {
        for (Entry<Object, String> entry : entities.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                try {
                    Long id = ((AbstractEntity) entry.getKey()).getId();
                    return entityService.find(ClassHelper.getClassByName(ClassHelper.getTrimmedClassName(entry.getKey().getClass())), id);
                } catch (Exception e) {
                    // Note: This is a hack. All "entity" classes extend the super class AbstractEntity except the class Country which has a string id.
                    String id = ((Country) entry.getKey()).getId();
                    return entityService.find(Country.class, id);
                }
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object entity) {
        synchronized (entities) {
            if (!entities.containsKey(entity)) {
                String uuid = UUID.randomUUID().toString();
                entities.put(entity, uuid);
                return uuid;
            }
            return entities.get(entity);
        }
    }

    public Map<Object, String> getEntities() {
        return entities;
    }
}