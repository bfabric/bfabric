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

import java.awt.*;

public class Constants {

    /*
     * Constants for configuration of download manager settings.
     */
    public static final String DOWNLOAD_SUMMARY_FILE_NAME = "summary.html";

    public static final String ATTACHMENTS_DOWNLOAD_FOLDER = "attachments";

    public static final String RESOURCES_DOWNLOAD_FOLDER = "resources";

    public static final String ATTACHMENT_PREFIX = "attachment_";

    public static final String COMMENT_PREFIX = "comment_";

    public static final String NOTE_PREFIX = "note_";

    public static final String RESULT_PREFIX = "result_";

    public static final String ORDER_PREFIX = "order_";

    public static final String PROJECT_PREFIX = "project_";

    public static final String RESOURCE_PREFIX = "resource_";

    public static final String METADATA_FILE_NAME = "metadata.xml";

    /*
     * Constants for configuration (given by B-Fabric architecture).
     */
    public static final String BFABRIC_PATH_PREFIX = "/bfabric/";

    public static final String JAR_FILE_PATH = BFABRIC_PATH_PREFIX + "fragments/downloadmanager.jar";

    public static final String SHOW_SCREEN_PATH = "/show.html?id=";

    public static final String SHOW_COMMENT_PATH = BFABRIC_PATH_PREFIX + "comment" + SHOW_SCREEN_PATH;

    public static final String SHOW_ORDER_PATH = BFABRIC_PATH_PREFIX + "order" + SHOW_SCREEN_PATH;

    public static final String SHOW_PROJECT_PATH = BFABRIC_PATH_PREFIX + "project" + SHOW_SCREEN_PATH;

    public static final String SHOW_RESOURCE_PATH = BFABRIC_PATH_PREFIX + "resource" + SHOW_SCREEN_PATH;

    public static final String SHOW_WORKUNIT_PATH = BFABRIC_PATH_PREFIX + "workunit" + SHOW_SCREEN_PATH;

    /*
     * Constants for deployers.
     */
    public static final String ICON_PATH = "/favicon.png";

    /*
     * Constants for table headers.
     */
    public static final String DOWNLOAD = "Download";

    public static final String DOWNLOAD_DIRECTORY = "Download Directory";

    public static final String SIZE = "Size";

    public static final String DONE = "Done";

    public static final String STATUS = "Status";

    public static final String ACTION = "Action";

    public static final String RESUME = "Resume";

    public static final String PAUSE = "Pause";

    public static final String CALCULATING = "Calculating...";

    public static final String TIME_REMAINING = "Time Remaining";

    /*
     * Constants for colors and fonts.
     */
    public static final Color BORDER_COLOR = Color.WHITE; // new Color(238, 238,

    // 238);
    public static final Color BUTTON_COLOR = new Color(0, 60, 104);

    public static final Color GREEN_COLOR = new Color(0, 153, 0);

    public static final Color TEXT_FONT_COLOR = new Color(84, 94, 105);

    public static final Color LINK_FONT_COLOR = new Color(0, 60, 104);

    public static final Font TABLE_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    public static final Font TABLE_FONT_BOLD = new Font(Font.DIALOG, Font.BOLD, 12);

    /*
     * Constants for directory structure.
     */
    public static final String PLAIN = "Plain";

    public static final String BFABRIC = "B-Fabric";

    public static final String STORAGE = "Storage";

    public static final String NULL = "null";
}
