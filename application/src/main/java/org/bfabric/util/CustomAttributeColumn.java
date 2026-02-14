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
import java.util.Comparator;
import java.util.UUID;

public class CustomAttributeColumn implements Serializable {

    private static final long serialVersionUID = 1;

    private final String id;

    private String name;

    private String oldName;

    private int position;

    public CustomAttributeColumn(String name, int position) {
        id = "CA-" + UUID.randomUUID();
        this.name = name;
        this.position = position;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof CustomAttributeColumn && hashCode() == object.hashCode();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOldName() {
        return oldName;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        if (getId() == null) {
            return super.hashCode();
        }
        // Important: use trimmed class name because of hibernate proxy issues.
        return ClassHelper.getTrimmedClassName(getClass().getName()).concat(getId()).hashCode();
    }

    public boolean isMovable(int lastColumnPosition) {
        return getPosition() != lastColumnPosition;
    }

    public void setName(String name) {
        if (this.name != null && !this.name.equals(name)) {
            oldName = this.name;
        }
        this.name = name;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void switchPositions(CustomAttributeColumn column) {
        int temp = getPosition();
        setPosition(column.getPosition());
        column.setPosition(temp);
    }

    public static class Comparators {

        public static final Comparator<CustomAttributeColumn> NAME = Comparator.comparing(o -> o.name);

        public static final Comparator<CustomAttributeColumn> POSITION = Comparator.comparingInt(o -> o.position);
    }
}