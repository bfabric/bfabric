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

import org.bfabric.enums.DownloadStatusEnum;

public class DownloadStatus {

    protected DownloadStatusEnum status;

    private boolean fileChecksumTestFailed = false;

    private boolean fileSizeTestFailed = false;

    public DownloadStatus() {
        this.status = DownloadStatusEnum.READY;
    }

    /**
     * Constructor.
     *
     * @param status The status
     */
    public DownloadStatus(DownloadStatusEnum status) {
        this.status = status;
    }

    /**
     * Calculate the download site.
     */
    public void calculate() {
        if (isReady() || isPaused() || isStopped()) {
            setStatus(DownloadStatusEnum.CALCULATING);
        }
    }

    /**
     * Mark the download as disable.
     */
    public void disable() {
        setStatus(DownloadStatusEnum.DISABLED);
    }

    /**
     * Mark the download as done.
     */
    public void done() {
        if (!isFailed()) {
            setStatus(DownloadStatusEnum.DONE);
        }
    }

    /**
     * Enqueue the download.
     */
    public void enqueue() {
        if (isReady() || isPaused() || isStopped() || isCalculating()) {
            setStatus(DownloadStatusEnum.QUEUED);
        }
    }

    /**
     * Mark the download as failed.
     */
    public void failed() {
        if (!isDone()) {
            setStatus(DownloadStatusEnum.FAILED);
        }
    }

    /**
     * Get the label.
     *
     * @return String The label.
     */
    public String getLabel() {
        return getStatus().getLabel();
    }

    /**
     * Get status.
     *
     * @return the status
     */
    public DownloadStatusEnum getStatus() {
        return status;
    }

    /**
     * Is status = Calculating?
     *
     * @return true if status = Calculating; false otherwise.
     */
    public boolean isCalculating() {
        return getStatus() == DownloadStatusEnum.CALCULATING;
    }

    /**
     * Is status = Disabled?
     *
     * @return true if status = Disabled; false otherwise.
     */
    public boolean isDisabled() {
        return getStatus() == DownloadStatusEnum.DISABLED;
    }

    /**
     * Is status = Done?
     *
     * @return true if status = Done; false otherwise.
     */
    public boolean isDone() {
        return getStatus() == DownloadStatusEnum.DONE;
    }

    /**
     * Is status = Failed?
     *
     * @return true if status = Failed; false otherwise.
     */
    public boolean isFailed() {
        return getStatus() == DownloadStatusEnum.FAILED;
    }

    /**
     * Get fileChecksumTestFailed.
     *
     * @return the fileChecksumTestFailed
     */
    public boolean isFileChecksumTestFailed() {
        return fileChecksumTestFailed;
    }

    /**
     * Get fileSizeTestFailed.
     *
     * @return the fileSizeTestFailed
     */
    public boolean isFileSizeTestFailed() {
        return fileSizeTestFailed;
    }

    /**
     * Is status = Invalid?
     *
     * @return true if status = Invalid; false otherwise.
     */
    public boolean isInvalid() {
        return getStatus() == DownloadStatusEnum.INVALID;
    }

    /**
     * Is status = missing?
     *
     * @return true if status = missing; false otherwise.
     */
    public boolean isMissing() {
        return getStatus() == DownloadStatusEnum.MISSING;
    }

    /**
     * Is status = Paused?
     *
     * @return true if status = Paused; false otherwise.
     */
    public boolean isPaused() {
        return getStatus() == DownloadStatusEnum.PAUSED;
    }

    /**
     * Is status = Queued?
     *
     * @return true if status = Queued; false otherwise.
     */
    public boolean isQueued() {
        return getStatus() == DownloadStatusEnum.QUEUED;
    }

    /**
     * Is status = Ready?
     *
     * @return true if status = Ready; false otherwise.
     */
    public boolean isReady() {
        return getStatus() == DownloadStatusEnum.READY;
    }

    /**
     * Is status = Running?
     *
     * @return true if status = Running; false otherwise.
     */
    public boolean isRunning() {
        return getStatus() == DownloadStatusEnum.RUNNING;
    }

    /**
     * Is status = Stopped?
     *
     * @return true if status = Stopped; false otherwise.
     */
    public boolean isStopped() {
        return getStatus() == DownloadStatusEnum.STOPPED;
    }

    /**
     * Mark the download as missing.
     */
    public void missing() {
        if (!isDone()) {
            setStatus(DownloadStatusEnum.MISSING);
        }
    }

    /**
     * Pause the download.
     */
    public void pause() {
        if (isRunning() || isQueued()) {
            setStatus(DownloadStatusEnum.PAUSED);
        }
    }

    /**
     * Mark the download as ready.
     */
    public void ready() {
        setStatus(DownloadStatusEnum.READY);
    }

    /**
     * Start the download.
     */
    public void resume() {
        if (isPaused()) {
            setStatus(DownloadStatusEnum.RUNNING);
        }
    }

    /**
     * Set fileChecksumTestFailed.
     *
     * @param fileChecksumTestFailed the fileChecksumTestFailed to set
     */
    public void setFileChecksumTestFailed(boolean fileChecksumTestFailed) {
        this.fileChecksumTestFailed = fileChecksumTestFailed;
    }

    /**
     * Set fileSizeTestFailed.
     *
     * @param fileSizeTestFailed the fileSizeTestFailed to set
     */
    public void setFileSizeTestFailed(boolean fileSizeTestFailed) {
        this.fileSizeTestFailed = fileSizeTestFailed;
    }

    /**
     * Set status.
     *
     * @param status the status to set
     */
    public void setStatus(DownloadStatusEnum status) {
        this.status = status;
    }

    /**
     * Start the download.
     */
    public void start() {
        if (isReady() || isPaused() || isStopped() || isFailed() || isMissing() || isCalculating() || isQueued()) {
            setStatus(DownloadStatusEnum.RUNNING);
        }
    }

    /**
     * Stop the download.
     */
    public void stop() {
        if (!isDone() && !isFailed() && !isDisabled() && !isInvalid() && !isMissing()) {
            setStatus(DownloadStatusEnum.STOPPED);
        }
    }
}
