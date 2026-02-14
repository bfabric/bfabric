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

import org.bfabric.DownloadManager;
import org.bfabric.FileSetDownloadStatus;
import org.bfabric.enums.DownloadStatusEnum;

public class ButtonRenderer extends JButton implements TableCellRenderer {

    public static final int PLAY_BUTTON = 0;

    public static final int PAUSE_BUTTON = 1;

    public static final int CUSTOM_BUTTON = 2;

    private static final long serialVersionUID = 1;

    private int type;

    public ButtonRenderer(int type) {
        super();
        this.setType(type);
        setBorder(null);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setDebugGraphicsOptions(javax.swing.DebugGraphics.BUFFERED_OPTION);
        setDoubleBuffered(true);
        setFocusPainted(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        FileSetDownloadStatus fileSetDownloadStatus = DownloadManager.getFileSetDownloadStatus();

        switch (getType()) {
        case PLAY_BUTTON:
            setToolTipText(DownloadStatusEnum.PAUSED.getAction());
            setIcon(new ImageIcon(getClass().getResource(DownloadStatusEnum.PAUSED.getIcon())));
            setEnabled(!fileSetDownloadStatus.isRunningAll() && !fileSetDownloadStatus.isDone() && !fileSetDownloadStatus.isFailed());
            break;
        case PAUSE_BUTTON:
            setToolTipText(DownloadStatusEnum.RUNNING.getAction());
            setIcon(new ImageIcon(getClass().getResource(DownloadStatusEnum.RUNNING.getIcon())));
            setEnabled(fileSetDownloadStatus.isRunning());
            break;
        case CUSTOM_BUTTON:
        default:
            int sortedRow = row;
            if (table.getRowSorter() != null) {
                sortedRow = table.getRowSorter().convertRowIndexToModel(row);
            }

            DownloadStatusEnum downloadStatusEnum = DownloadStatusEnum.getDownloadStatusEnum(DownloadManager.getFileDownloaders().get(sortedRow).getFileDownloadStatus().getStatus().getLabel());

            if (downloadStatusEnum != null) {
                setToolTipText(downloadStatusEnum.getAction());
                setIcon(new ImageIcon(getClass().getResource(downloadStatusEnum.getIcon())));
                setEnabled(downloadStatusEnum.isIconActive() || !DownloadManager.getDownloadType().isFileSet());
            }
            break;
        }

        return this;
    }

    /**
     * Get type.
     *
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * Set type.
     *
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }
}
