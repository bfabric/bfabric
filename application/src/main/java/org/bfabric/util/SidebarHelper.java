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

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.Constants;
import org.omnifaces.cdi.Param;

@Named
@ViewScoped
public class SidebarHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private static final String tabDefault = Constants.DETAILS;

    @Param
    private String panel;

    @Param
    private String tab;

    public String getBody() {
        return getTab() + ".xhtml";
    }

    public String getPanel() {
        return panel;
    }

    public String getTab() {
        if (tab == null) {
            tab = tabDefault;
        }
        return tab;
    }

    public boolean isDetails() {
        return isTab(Constants.DETAILS);
    }

    public boolean isTab(String tab) {
        return getTab() != null && getTab().equalsIgnoreCase(tab);
    }

    public void setPanel(String panel) {
        this.panel = panel;
    }

    public void setTab(String tab) {
        if (tab != null) {
            this.tab = tab;
        }
    }

    public String tabStyleClass(String tab) {
        return isTab(tab) ? "sidebar-selected" : "sidebar-unselected";
    }
}