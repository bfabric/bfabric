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
import javax.swing.table.TableCellRenderer;

import org.bfabric.Constants;
import org.bfabric.DownloadManager;
import org.bfabric.DownloadStatus;

public class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {

    private static final long serialVersionUID = 1;

    private final boolean fileSetDownload;

    public ProgressBarRenderer(boolean fileSetDownload) {
        super(0, 100);
        this.fileSetDownload = fileSetDownload;
        setBackground(Color.WHITE);
        setBorder(null);
        setBorderPainted(false);
        setStringPainted(true);
        setFont(Constants.TABLE_FONT);
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", Constants.TEXT_FONT_COLOR);
        updateUI();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        int sortedRow = row;
        if (table.getRowSorter() != null) {
            sortedRow = table.getRowSorter().convertRowIndexToModel(row);
        }

        DownloadStatus downloadStatus;
        if (fileSetDownload) {
            downloadStatus = DownloadManager.getFileSetDownloadStatus();
        } else {
            downloadStatus = DownloadManager.getFileDownloaders().get(sortedRow).getFileDownloadStatus();
        }

        if (downloadStatus.isDone()) {
            if (downloadStatus.isFileChecksumTestFailed() || downloadStatus.isFileSizeTestFailed()) {
                setForeground(new Color(234, 107, 19));
            } else {
                // The green color from B-Fabric.
                setForeground(Constants.GREEN_COLOR);
            }
        } else if (downloadStatus.isFailed()) {
            setForeground(Color.RED);
        } else {
            // The blue color on button images from B-Fabric.
            setForeground(Constants.BUTTON_COLOR);
        }

        setValue((value == null) ? 0 : Integer.parseInt(value.toString()));
        return this;
    }
}
