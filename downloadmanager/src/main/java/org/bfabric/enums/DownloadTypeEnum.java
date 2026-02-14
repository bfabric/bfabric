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

package org.bfabric.enums;

public enum DownloadTypeEnum {

    RESOURCE(
        "Resource",
        false),
    RESOURCE_BASKET(
        "ResourceBasket",
        true),
    WORKUNIT(
        "Workunit",
        true),
    ORDER(
        "Order",
        true),
    PROJECT(
        "Project",
        true);

    private final boolean fileSet;

    private final String name;

    /**
     * Constructor.
     *
     * @param name the name to set
     * @param fileSet the fileSet to set
     */
    DownloadTypeEnum(String name, boolean fileSet) {
        this.name = name;
        this.fileSet = fileSet;
    }

    /**
     * Get the DownloadTypeEnum according to the name.
     *
     * @param name The name to set
     * @return the <code>DownloadTypeEnum</code> enum with the specified name.
     */
    public static DownloadTypeEnum getDownloadTypeEnum(String name) {
        if (name != null) {
            for (DownloadTypeEnum downloadType : values()) {
                if (downloadType.getName().equals(name)) {
                    return downloadType;
                }
            }
        }
        return null;
    }

    /**
     * Get name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get fileSet.
     *
     * @return the fileSet
     */
    public boolean isFileSet() {
        return fileSet;
    }

    /**
     * Is this PROJECT?
     *
     * @return true if this is WORKUNIT; false otherwise.
     */
    public boolean isProject() {
        return this.equals(PROJECT);
    }

    /**
     * Is this RESOURCE?
     *
     * @return true if this is RESOURCE; false otherwise.
     */
    public boolean isResource() {
        return this.equals(RESOURCE);
    }

    /**
     * Is this RESOURCE_BASKET?
     *
     * @return true if this is RESOURCE_BASKET; false otherwise.
     */
    public boolean isResourceBasket() {
        return this.equals(RESOURCE_BASKET);
    }

    /**
     * Is this WORKUNIT?
     *
     * @return true if this is WORKUNIT; false otherwise.
     */
    public boolean isWorkunit() {
        return this.equals(WORKUNIT);
    }
}