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

package org.bfabric.entity.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.xml.entity.XMLCustomAttribute;

public interface CustomAttributes {

    void addCustomAttribute();

    default void addCustomAttribute(CustomAttribute customAttribute) {
        if (getCustomAttributes() != null) {
            getCustomAttributes().add(customAttribute);
        }
    }

    default void addCustomAttribute(AbstractEntity parent) {
        if (parent != null && getCustomAttributes() != null) {
            new CustomAttribute(parent);
        }
    }

    default void addCustomAttributes(List<CustomAttribute> customAttributes) {
        if (getCustomAttributes() != null) {
            for (CustomAttribute customAttribute : customAttributes) {
                addCustomAttribute(customAttribute);
            }
        }
    }

    default String convertToText(Collection<CustomAttribute> customAttributes) {
        return customAttributes.stream().map(CustomAttribute::getNameValue).collect(Collectors.joining(" "));
    }

    List<CustomAttribute> getCustomAttributes();

    default String getCustomAttributesAsText() {
        return convertToText(getCustomAttributes());
    }

    Set<CustomAttribute> getCustomAttributesToBeRemoved();

    default void removeCustomAttribute(CustomAttribute customAttribute) {
        if (getCustomAttributes() != null && getCustomAttributesToBeRemoved() != null) {
            getCustomAttributesToBeRemoved().add(customAttribute);
            getCustomAttributes().remove(customAttribute);
        }
    }

    default void removeCustomAttributes() {
        if (getCustomAttributes() != null && getCustomAttributesToBeRemoved() != null) {
            getCustomAttributesToBeRemoved().addAll(getCustomAttributes());
            getCustomAttributes().clear();
        }
    }

    void setCustomAttributes(List<XMLCustomAttribute> xmlCustomAttributes);
}
