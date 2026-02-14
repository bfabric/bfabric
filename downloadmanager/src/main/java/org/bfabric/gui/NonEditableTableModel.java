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

import javax.swing.table.DefaultTableModel;

import org.bfabric.DownloadManager;
import org.bfabric.FileDownloadStatus;
import org.bfabric.FileSetDownloadStatus;
import org.bfabric.enums.DownloadStatusEnum;

public class NonEditableTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1;

    private final boolean editable;

    public NonEditableTableModel(boolean editable) {
        this.editable = editable;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int colIndex) {
        FileSetDownloadStatus fileSetDownloadStatus = DownloadManager.getFileSetDownloadStatus();
        FileDownloadStatus fileDownloadStatus = DownloadManager.getFileDownloaders().get(rowIndex).getFileDownloadStatus();

        boolean ret;
        switch (colIndex) {
        case 4:
            if (editable) {
                ret = DownloadStatusEnum.getDownloadStatusEnum(fileSetDownloadStatus.getLabel()).isIconActive();
            } else {
                ret = DownloadStatusEnum.getDownloadStatusEnum(fileDownloadStatus.getLabel()).isIconActive();
            }
            break;
        case 5:
            ret = editable && !fileSetDownloadStatus.isRunningAll() && !fileSetDownloadStatus.isDone() && !fileSetDownloadStatus.isFailed();
            break;
        default:
            ret = false;
            break;
        }

        return ret;
    }
}