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

public enum DownloadStatusEnum {
    INVALID(
        "Invalid",
        "Invalid",
        "Invalid",
        "",
        false),
    READY(
        "Ready",
        "Ready for download",
        "Start Download",
        "/download-resume.png",
        false),
    RUNNING(
        "Running",
        "Download is running",
        "Pause the download",
        "/download-pause.png",
        true),
    PAUSED(
        "Paused",
        "Download is paused",
        "Resume the download",
        "/download-resume.png",
        true),
    DONE(
        "Done",
        "Download is done",
        "Download done",
        "/download-resume.png",
        false),
    FAILED(
        "Failed",
        "Download is failed",
        "Download failed",
        "/download-pause.png",
        false),
    STOPPED(
        "Stopped",
        "Download is stopped",
        "Download stopped",
        "/download-resume.png",
        true),
    DISABLED(
        "Disabled",
        "Download will not be performed",
        "Download disabled",
        "",
        false),
    MISSING(
        "Missing",
        "Download failed since the file could not be found on the storage",
        "Download failed",
        "",
        false),
    CALCULATING(
        "Calculating",
        "Calculating the download size",
        "Calculating",
        "",
        false),
    QUEUED(
        "Queued",
        "Download queued, starts when it is enqueued",
        "Download queued",
        "",
        false);

    private final String action;

    private final String icon;

    private final boolean iconActive;

    /**
     * Attributes
     */
    private final String label;

    private final String toolTip;

    /**
     * Constructor.
     *
     * @param label The label to set.
     * @param toolTip The toolTip.
     * @param action The action.
     * @param icon The icon.
     * @param iconActive The iconActive.
     */
    DownloadStatusEnum(String label, String toolTip, String action, String icon, boolean iconActive) {
        this.label = label;
        this.toolTip = toolTip;
        this.action = action;
        this.icon = icon;
        this.iconActive = iconActive;
    }

    /**
     * Get DownloadStatusEnum of the given label.
     *
     * @param label The label to set
     * @return the corresponding enum; default if there is no enum of the given label.
     */
    public static DownloadStatusEnum getDownloadStatusEnum(String label) {
        DownloadStatusEnum ret = DownloadStatusEnum.READY;
        for (DownloadStatusEnum DownloadStatusEnum : values()) {
            if (DownloadStatusEnum.getLabel().equals(label)) {
                ret = DownloadStatusEnum;
                break;
            }
        }
        return ret;
    }

    /**
     * Get action.
     *
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Get icon.
     *
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Get the label.
     *
     * @return String The label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get toolTip.
     *
     * @return the toolTip
     */
    public String getToolTip() {
        return toolTip;
    }

    /**
     * Get iconActive.
     *
     * @return the iconActive
     */
    public boolean isIconActive() {
        return iconActive;
    }
}