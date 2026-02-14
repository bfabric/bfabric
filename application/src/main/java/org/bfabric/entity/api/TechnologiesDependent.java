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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bfabric.entity.Technology;
import org.bfabric.util.CollectionHelper;

public interface TechnologiesDependent {

    default void addTechnologies(Set<Technology> technologies) {
        if (technologies != null && !technologies.isEmpty()) {
            getTechnologies().addAll(technologies);
            setTechnologiesAsString();
        }
    }

    default void addTechnology(Technology technology) {
        if (technology != null) {
            getTechnologies().add(technology);
            setTechnologiesAsString();
        }
    }

    Set<Technology> getTechnologies();

    String getTechnologiesAsString();

    default String getTechnologiesAsStringComputed() {
        return CollectionHelper.print(getTechnologiesAsStringList());
    }

    default List<String> getTechnologiesAsStringList() {
        List<String> technologiesAsStringList = new ArrayList<>();
        for (Technology technology : getTechnologies()) {
            technologiesAsStringList.add(technology.getName());
        }
        return CollectionHelper.sortObjects(technologiesAsStringList);
    }

    default String getTechnology() {
        return CollectionHelper.print(getTechnologies());
    }

    default void removeTechnologies(Set<Technology> technologies) {
        if (technologies != null && !technologies.isEmpty()) {
            getTechnologies().removeAll(technologies);
            setTechnologiesAsString();
        }
    }

    default void removeTechnology(Technology technology) {
        if (technology != null) {
            getTechnologies().remove(technology);
            setTechnologiesAsString();
        }
    }

    default void setTechnologies(Set<Technology> technologies) {
        if (technologies != null && !technologies.isEmpty()) {
            getTechnologies().clear();
            getTechnologies().addAll(technologies);
            setTechnologiesAsString();
        }
    }

    default void setTechnologiesAsString() {
        setTechnologiesAsString(getTechnologiesAsStringComputed());
    }

    void setTechnologiesAsString(String technologiesAsString);
}