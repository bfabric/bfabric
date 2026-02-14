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

public enum CreditTypeEnum {

    OVERDRAWN(
        "Overdrawn Credit",
        "The overdrawn vacation credits from last year which are carried over to the next year as negative credits."),
    REMAINING(
        "Remaining Credit",
        "The remaining vacation credits from last year which are carried over to the next year."),
    SPECIAL(
        "Special Credit",
        "A special vacation credit."),
    VACATION(
        "Vacation Credit",
        "Annual vacation credit. Full-time employees get 25 days off. Full-time employees above age 50 get 30 days off.");

    private final String description;

    private final String label;

    CreditTypeEnum(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public static CreditTypeEnum value(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }
}
