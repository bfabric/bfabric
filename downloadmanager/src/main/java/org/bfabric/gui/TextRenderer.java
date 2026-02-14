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

package org.bfabric.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.bfabric.Constants;

public class TextRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = 1;

    private boolean clickable;

    public TextRenderer() {
        this(false);
    }

    /**
     * Constructor.
     *
     * @param clickable the clickable
     */
    public TextRenderer(boolean clickable) {
        this.setClickable(clickable);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setFont(Constants.TABLE_FONT);
        setToolTipText(null);
        if (isClickable()) {
            setForeground(Constants.LINK_FONT_COLOR);
            setToolTipText("Open your download directory");
        } else {
            setForeground(Constants.TEXT_FONT_COLOR);
        }
        setText(value.toString());

        Border margin = new EmptyBorder(0, 5, 0, 5);
        setBorder(margin);

        return this;
    }

    /**
     * Get clickable.
     *
     * @return the clickable
     */
    public boolean isClickable() {
        return clickable;
    }

    /**
     * Set clickable.
     *
     * @param clickable the clickable to set
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
}
