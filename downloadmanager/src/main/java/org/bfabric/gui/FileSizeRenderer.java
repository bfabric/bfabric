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
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;

import org.bfabric.Constants;

public class FileSizeRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = 1;

    /**
     * Get the formatted version of the given file size.
     *
     * @param size The given file size
     * @return the formatted version of the given file size
     */
    public static String getFileSizeFormatted(long size) {
        if (size <= 0) {
            return "-";
        }

        int length = (int) (Math.log(size) / Math.log(1024));
        switch (length) {
        case 0:
            return String.format("%d  B", (int) size);
        case 1:
            return String.format("%.3f KB", size / Math.pow(1024, 1));
        case 2:
            return String.format("%.3f MB", size / Math.pow(1024, 2));
        case 3:
            return String.format("%.3f GB", size / Math.pow(1024, 3));
        default:
            return String.format("%.3f TB", size / Math.pow(1024, 4));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        long fileSize = Long.parseLong(value.toString().trim());

        setFont(Constants.TABLE_FONT);
        setForeground(Constants.TEXT_FONT_COLOR);
        setHorizontalAlignment(SwingConstants.RIGHT);
        setText(getFileSizeFormatted(fileSize));

        MatteBorder border = new MatteBorder(0, 5, 0, 5, Color.WHITE);
        setBorder(border);

        return this;
    }
}
