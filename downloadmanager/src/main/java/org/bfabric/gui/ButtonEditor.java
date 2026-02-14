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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import org.bfabric.DownloadManager;

public class ButtonEditor extends DefaultCellEditor implements MouseListener {

    private static final long serialVersionUID = 1;

    private final JButton button;

    private final JTable parentTable;

    private final ButtonRenderer buttonRenderer;

    public ButtonEditor(final JTable table, ButtonRenderer buttonRenderer) {
        super(new JCheckBox());
        parentTable = table;
        this.buttonRenderer = buttonRenderer;
        button = new JButton();
        button.addActionListener(e -> fireEditingStopped());
        button.addMouseListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.DefaultCellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
        return 0L;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return button;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (buttonRenderer.getType()) {
        case ButtonRenderer.PLAY_BUTTON:
            DownloadManager.setTimeStarted(System.currentTimeMillis());
            DownloadManager.setLastDownloadedSize(0);
            for (int i = 0; i < DownloadManager.getNumberOfFiles(); i++) {
                if (DownloadManager.getFileDownloaders().get(i).getFileDownloadStatus().isReady() || DownloadManager.getFileDownloaders().get(i).getFileDownloadStatus().isPaused()) {
                    DownloadManager.getFileDownloaders().get(i).enqueueDownload();
                }
            }
            break;
        case ButtonRenderer.PAUSE_BUTTON:
            for (int i = 0; i < DownloadManager.getNumberOfFiles(); i++) {
                DownloadManager.getFileDownloaders().get(i).getFileDownloadStatus().pause();
            }
            break;
        case ButtonRenderer.CUSTOM_BUTTON:
        default:
            DownloadManager.setTimeStarted(System.currentTimeMillis());
            DownloadManager.setLastDownloadedSize(0);
            int clickedId = parentTable.getEditingRow();
            if (parentTable.getRowSorter() != null) {
                clickedId = parentTable.getRowSorter().convertRowIndexToModel(clickedId);
            }
            if (DownloadManager.getFileDownloaders().get(clickedId).getFileDownloadStatus().isRunning()) {
                DownloadManager.getFileDownloaders().get(clickedId).getFileDownloadStatus().pause();
            } else {
                DownloadManager.getFileDownloaders().get(clickedId).enqueueDownload();
            }
            break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
