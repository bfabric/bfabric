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

package org.bfabric;

import java.util.HashSet;
import java.util.Set;

import org.bfabric.enums.DownloadStatusEnum;

public class FileSetDownloadStatus extends DownloadStatus {

    private Set<FileDownloadStatus> childStates;

    public FileSetDownloadStatus() {
        super();
    }

    /**
     * Constructor.
     *
     * @param status the status
     */
    public FileSetDownloadStatus(DownloadStatusEnum status) {
        super(status);
    }

    /**
     * Mark the download as failed.
     */
    @Override
    public void failed() {
        if (!isDone()) {
            setStatus(DownloadStatusEnum.FAILED);
        }
    }

    /**
     * Get childStates.
     *
     * @return the childStates
     */
    public Set<FileDownloadStatus> getChildStates() {
        if (childStates == null) {
            childStates = new HashSet<>();
        }
        return childStates;
    }

    /**
     * Is status = Running All, i.e., all child downloads are running or completed?
     *
     * @return true if status = Running All; false otherwise.
     */
    public boolean isRunningAll() {
        boolean isRunningAll = isRunning();
        if (isRunningAll) {
            for (FileDownloadStatus childState : getChildStates()) {
                if (childState.isPaused()) {
                    isRunningAll = false;
                    break;
                }
            }
        }
        return isRunningAll;
    }

    /**
     * Pause the download.
     */
    @Override
    public void pause() {
        if (isRunning()) {
            setStatus(DownloadStatusEnum.PAUSED);
            for (FileDownloadStatus childState : getChildStates()) {
                childState.pause();
            }
        }
    }

    /**
     * Mark the download as ready.
     */
    @Override
    public void ready() {
        setStatus(DownloadStatusEnum.READY);
        for (FileDownloadStatus childState : getChildStates()) {
            childState.ready();
        }
    }

    /**
     * Set the status of the file set download dependent on the status of its file downloads.
     *
     * FAILED: if it exists a failed file download
     *
     * READY: if all file downloads are ready
     *
     * PAUSED: if all file downloads are paused
     *
     * DONE: if all file downloads are done
     *
     * RUNNING: if it exists not failed but at least one running file download
     */
    public void resetStatus() {
        DownloadStatusEnum newStatus = DownloadStatusEnum.INVALID;
        for (FileDownloadStatus childState : getChildStates()) {
            if (childState.isCalculating()) {
                newStatus = DownloadStatusEnum.CALCULATING;
            } else if ((childState.isQueued() || childState.isRunning()) && newStatus != DownloadStatusEnum.CALCULATING) {
                newStatus = DownloadStatusEnum.RUNNING;
            } else if (childState.isDisabled() && newStatus == DownloadStatusEnum.INVALID) {
                newStatus = DownloadStatusEnum.DISABLED;
            } else if (childState.isPaused() && newStatus != DownloadStatusEnum.RUNNING) {
                newStatus = DownloadStatusEnum.PAUSED;
            } else if (childState.isDone() && (newStatus == DownloadStatusEnum.INVALID || newStatus == DownloadStatusEnum.DISABLED || newStatus == DownloadStatusEnum.DONE)) {
                newStatus = DownloadStatusEnum.DONE;
            } else if (childState.isReady() && (newStatus == DownloadStatusEnum.INVALID || newStatus == DownloadStatusEnum.DISABLED || newStatus == DownloadStatusEnum.READY)) {
                newStatus = DownloadStatusEnum.READY;
            } else if (childState.isStopped() && (newStatus == DownloadStatusEnum.INVALID || newStatus == DownloadStatusEnum.DISABLED || newStatus == DownloadStatusEnum.STOPPED)) {
                newStatus = DownloadStatusEnum.STOPPED;
            } else if (childState.isFailed() && (newStatus == DownloadStatusEnum.INVALID || newStatus == DownloadStatusEnum.DISABLED || newStatus == DownloadStatusEnum.FAILED || newStatus == DownloadStatusEnum.DONE)) {
                newStatus = DownloadStatusEnum.FAILED;
            }
        }
        if (newStatus != DownloadStatusEnum.INVALID) {
            setStatus(newStatus);
        }
    }

    @Override
    public void resume() {
        if (isPaused()) {
            setStatus(DownloadStatusEnum.RUNNING);
            for (FileDownloadStatus childState : getChildStates()) {
                childState.resume();
            }
        }
    }

    /**
     * Set childStates.
     *
     * @param childStates the childStates to set
     */
    public void setChildStates(Set<FileDownloadStatus> childStates) {
        this.childStates = childStates;
    }

    @Override
    public void setStatus(DownloadStatusEnum status) {
        boolean logDownloadStatus = this.status != status;
        this.status = status;
        // If the file set download is terminated (done, failed, or stopped), then write the summary file.
        if (isDone() || isFailed() || isStopped()) {
            DownloadManager.createSummaryFile();
        }
        if (logDownloadStatus) {
            DownloadManager.logDownloadStatus();
        }
    }

    @Override
    public void start() {
        if (isReady()) {
            setStatus(DownloadStatusEnum.RUNNING);
            for (FileDownloadStatus childState : getChildStates()) {
                childState.start();
            }
        }
    }

    @Override
    public void stop() {
        if (!isDone()) {
            setStatus(DownloadStatusEnum.STOPPED);
            for (FileDownloadStatus childState : getChildStates()) {
                childState.stop();
            }
        }
    }
}
