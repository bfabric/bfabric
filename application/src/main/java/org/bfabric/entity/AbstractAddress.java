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


package org.bfabric.entity;

import java.io.Serializable;

import org.bfabric.Constants;
import org.bfabric.util.StringHelper;

public abstract class AbstractAddress implements Serializable {

    private static final long serialVersionUID = 1;

    public abstract String getCity();

    public abstract Country getCountry();

    public String getFullAddress() {
        return StringHelper.getFullAddress(getStreet(), getZip(), getCity(), getCountry());
    }

    public String getFullAddressWithLineBreaks() {
        return StringHelper.getFullAddress(getStreet(), getZip(), getCity(), getCountry(), 1);
    }

    public String getFullCity() {
        return StringHelper.getFullAddress(null, getZip(), getCity(), getCountry());
    }

    public String getMapLink() {
        String params = Constants.EMPTY_STRING;

        if (StringHelper.isNotEmpty(getStreet())) {
            params += getStreet();
        }

        if (StringHelper.isNotEmpty(getZip())) {
            if (StringHelper.isNotEmpty(params)) {
                params += ",+";
            }
            params += getZip();
        }

        if (StringHelper.isNotEmpty(getCity())) {
            if (StringHelper.isNotEmpty(params)) {
                params += "+";
            }
            params += getCity();
        }

        if (getCountry() != null) {
            if (StringHelper.isNotEmpty(params)) {
                params += "+";
            }
            params += getCountry().getName();
        }

        return StringHelper.isNotEmpty(params) ? "https://www.google.com/maps/place/" + params : Constants.EMPTY_STRING;
    }

    public abstract String getStreet();

    public abstract String getSupplement();

    public abstract String getZip();

    public String getZipCity() {
        return StringHelper.getFullAddress(null, getZip(), getCity(), null);
    }

    public boolean isComplete() {
        return !(getStreet() == null || getStreet().isEmpty() || getCity() == null || getCity().isEmpty() || getCountry() == null || getZip() == null || getZip().isEmpty());
    }

    public boolean isEmpty() {
        return StringHelper.isEmpty(getSupplement()) && StringHelper.isEmpty(getStreet()) && StringHelper.isEmpty(getZip()) && StringHelper.isEmpty(getCity()) && getCountry() == null;
    }

    public boolean isForeign() {
        return getCountry() == null || getCountry().getId() == null || !"CH".equals(getCountry().getId());
    }

    public boolean isForeignAddress() {
        return getCountry() != null && !getCountry().getId().isEmpty() && !getCountry().getId().equals("CH");
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}