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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import org.bfabric.enums.DownloadTypeEnum;
import org.bfabric.gui.ButtonEditor;
import org.bfabric.gui.ButtonRenderer;
import org.bfabric.gui.CustomDialog;
import org.bfabric.gui.EditableTableModel;
import org.bfabric.gui.FileSizeRenderer;
import org.bfabric.gui.NonEditableTableModel;
import org.bfabric.gui.ProgressBarRenderer;
import org.bfabric.gui.TextEditor;
import org.bfabric.gui.TextRenderer;

public class DownloadManager extends JFrame {

    private static final long serialVersionUID = 1;

    // Constants for the configuration of download manager settings.
    private static final Path DEFAULT_DIRECTORY_PATH = Paths.get(getHomeDirectory(), "B-Fabric-Downloads");

    // Executors
    public static ExecutorService downloadExecutor;

    public static ExecutorService calculateExecutor;

    // Main frame
    private static JFrame frame;

    private static String applicationTitle;

    private static String applicationURL;

    private static String directoryId;

    private static Path directoryPath;

    private static boolean directoryEditable;

    private static String directoryStructure;

    private static String authStorage;

    private static String logUrl;

    private static String logCode;

    private static int numberOfFiles;

    private static List<String> fileIds;

    private static List<String> parentIds;

    private static List<String> filePaths;

    private static List<String> fileRelativePaths;

    private static List<URL> fileURLs;

    private static List<String> fileChecksums;

    private static List<FileDownloader> fileDownloaders;

    private static long timeStarted;

    private static long lastDownloadedSize;

    private static DownloadTypeEnum downloadType;

    private static FileSetDownloadStatus fileSetDownloadStatus = null;

    private static LocalDateTime expirationDateTime;

    private final NonEditableTableModel filesModel;

    private final EditableTableModel footerModel;

    // Helper variables to hold the dynamic table models.
    private NonEditableTableModel headerModel;

    /**
     * Constructor. Defines the download manager modal panel.
     */
    public DownloadManager() {
        // Define specific GUI properties.
        UIManager.put("OptionPane.messageFont", Constants.TABLE_FONT);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        if (isInvalid() || isExpired()) {
            List<String> messages = new ArrayList<>();
            if (isInvalid()) {
                messages.add("Invalid JNLP file expiration datetime: " + expirationDateTime);
            }
            if (isExpired()) {
                messages.add("Expired JNLP file: " + expirationDateTime);
            }
            messages.add("Please go to B-Fabric and create a new JNLP file for data download.");
            List<String> buttons = new ArrayList<>();
            buttons.add("Close");
            CustomDialog expiredDialog = new CustomDialog(this, true, messages, buttons);
            exit();
        }

        if (directoryEditable) {
            chooseDownloadDirectory();
        }

        if (!directoryPath.isAbsolute()) {
            setDirectoryPath(Paths.get(getHomeDirectory(), directoryPath.toString()));
        }

        initializeFilePaths();

        File dir = Paths.get(directoryPath.toString(), directoryId).toFile();
        if (dir.exists()) {
            List<String> messages = new ArrayList<>();
            messages.add("A previous download with the same name found: " + directoryId);
            messages.add("Do you want to resume this download or start a new one from scratch?");
            // String commonFilePathPrefix = getCommonFilePathPrefix();
            // for (int i = 0; i < numberOfFiles; i++) {
            //     messages.add("file=" + getFilePath(i) + "  path=" + filePaths.get(i) + "  prefix=" + commonFilePathPrefix);
            // }
            List<String> buttons = new ArrayList<>();
            buttons.add("Resume");
            buttons.add("Restart");
            CustomDialog resumeDialog = new CustomDialog(this, true, messages, buttons);
            switch (resumeDialog.getChoice()) {
            case 0:
                // Resume button pressed.
                break;
            case 1:
                // Restart button pressed.
                rm(dir);
                break;
            default:
                // Exit button pressed.
                exit();
            }
        } else {
            rm(dir);
        }

        setFileSetDownloadStatus(new FileSetDownloadStatus());
        setFileDownloaders(new ArrayList<>());
        for (int i = 0; i < numberOfFiles; i++) {
            fileDownloaders.add(new FileDownloader(filePaths.get(i), fileURLs.get(i), fileChecksums.get(i), fileSetDownloadStatus));
        }
        fileSetDownloadStatus.resetStatus();

        // Helper variable for creating a JTable based on a given table model.
        JTable table;
        JScrollPane tableScrollPane;

        // Helper variable for holding the table headers and values.
        Object[] tableHeader;
        Object[][] tableValues;

        // Create the file set table.
        if (downloadType.isFileSet()) {
            headerModel = new NonEditableTableModel(true);
            tableHeader = new Object[] { Constants.DOWNLOAD, Constants.SIZE, Constants.STATUS, Constants.DONE, Constants.PAUSE, Constants.RESUME };
            tableValues = new Object[1][5];
            tableValues[0] = new Object[] { directoryId, -1, fileSetDownloadStatus.getLabel(), null, null, null };
            headerModel.setDataVector(tableValues, tableHeader);

            table = createJTable(headerModel, false);
            table.getColumn(Constants.DOWNLOAD).setPreferredWidth(250);
            table.getColumn(Constants.DOWNLOAD).setCellRenderer(new TextRenderer());
            table.getColumn(Constants.SIZE).setPreferredWidth(130);
            table.getColumn(Constants.SIZE).setCellRenderer(new FileSizeRenderer());
            table.getColumn(Constants.STATUS).setPreferredWidth(220);
            table.getColumn(Constants.STATUS).setCellRenderer(new TextRenderer());
            table.getColumn(Constants.DONE).setPreferredWidth(130);
            table.getColumn(Constants.DONE).setCellRenderer(new ProgressBarRenderer(true));
            table.getColumn(Constants.PAUSE).setPreferredWidth(65);
            ButtonRenderer pauseButtonRenderer = new ButtonRenderer(ButtonRenderer.PAUSE_BUTTON);
            table.getColumn(Constants.PAUSE).setCellRenderer(pauseButtonRenderer);
            table.getColumn(Constants.PAUSE).setCellEditor(new ButtonEditor(table, pauseButtonRenderer));
            table.getColumn(Constants.RESUME).setPreferredWidth(65);
            ButtonRenderer playButtonRenderer = new ButtonRenderer(ButtonRenderer.PLAY_BUTTON);
            table.getColumn(Constants.RESUME).setCellRenderer(playButtonRenderer);
            table.getColumn(Constants.RESUME).setCellEditor(new ButtonEditor(table, playButtonRenderer));
            table.setPreferredScrollableViewportSize(table.getPreferredSize());

            tableScrollPane = new JScrollPane(table);
            tableScrollPane.setBorder(BorderFactory.createMatteBorder(20, 20, 0, 20, Constants.BORDER_COLOR));
            add(tableScrollPane);
        }

        // Create the file table.
        filesModel = new NonEditableTableModel(!downloadType.isFileSet());
        tableValues = new Object[numberOfFiles][5];
        tableHeader = new Object[] { Constants.DOWNLOAD, Constants.SIZE, Constants.STATUS, Constants.DONE, Constants.ACTION };
        for (int i = 0; i < numberOfFiles; i++) {
            String fileId = fileIds.get(i);
            String fileSize = fileDownloaders.get(i).getFileSizePadded();
            String status = fileDownloaders.get(i).getFileDownloadStatus().getLabel();
            String progress = fileDownloaders.get(i).getProgressFormatted();
            tableValues[i] = new Object[] { fileId, fileSize, status, progress, status };
        }
        filesModel.setDataVector(tableValues, tableHeader);

        table = createJTable(filesModel, true);
        table.getColumn(Constants.DOWNLOAD).setPreferredWidth(250);
        table.getColumn(Constants.DOWNLOAD).setCellRenderer(new TextRenderer());
        table.getColumn(Constants.SIZE).setPreferredWidth(130);
        table.getColumn(Constants.SIZE).setCellRenderer(new FileSizeRenderer());
        table.getColumn(Constants.STATUS).setPreferredWidth(220);
        table.getColumn(Constants.STATUS).setCellRenderer(new TextRenderer());
        table.getColumn(Constants.DONE).setPreferredWidth(130);
        table.getColumn(Constants.DONE).setCellRenderer(new ProgressBarRenderer(false));
        table.getColumn(Constants.ACTION).setPreferredWidth(130);
        ButtonRenderer buttonRenderer = new ButtonRenderer(ButtonRenderer.CUSTOM_BUTTON);
        table.getColumn(Constants.ACTION).setCellRenderer(buttonRenderer);
        table.getColumn(Constants.ACTION).setCellEditor(new ButtonEditor(table, buttonRenderer));
        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        if (numberOfFiles >= 20) {
            table.getColumn(Constants.ACTION).setPreferredWidth(114);
            table.setPreferredScrollableViewportSize(new Dimension(750, 360));
            table.setFillsViewportHeight(true);
        }

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createMatteBorder(20, 20, 0, 20, Constants.BORDER_COLOR));
        add(tableScrollPane);

        // Create the download directory table.
        footerModel = new EditableTableModel();
        tableHeader = new Object[] { Constants.DOWNLOAD_DIRECTORY, Constants.TIME_REMAINING };
        tableValues = new Object[1][1];
        tableValues[0] = new Object[] { directoryPath, Constants.CALCULATING };
        footerModel.setDataVector(tableValues, tableHeader);

        table = createJTable(footerModel, false);
        table.getColumn(Constants.DOWNLOAD_DIRECTORY).setPreferredWidth(640);
        TextRenderer textRenderer = new TextRenderer(true);
        table.getColumn(Constants.DOWNLOAD_DIRECTORY).setCellRenderer(textRenderer);
        table.getColumn(Constants.DOWNLOAD_DIRECTORY).setCellEditor(new TextEditor(textRenderer));
        table.getColumn(Constants.TIME_REMAINING).setPreferredWidth(130);
        table.getColumn(Constants.TIME_REMAINING).setCellRenderer(new TextRenderer());
        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createMatteBorder(20, 20, 20, 20, Constants.BORDER_COLOR));
        add(tableScrollPane);

        downloadExecutor = Executors.newFixedThreadPool(4);
        calculateExecutor = Executors.newFixedThreadPool(8); // Greedier than the downloadExecutor.

        calculateExecutor.execute(new UIUpdater());
        setTimeStarted(System.currentTimeMillis());
        for (int i = 0; i < numberOfFiles; i++) {
            fileDownloaders.get(i).calculateDownload();
        }
    }

    /**
     * Add given character to given string at given position.
     *
     * @return the DownloadDate
     */
    public static String addChar(String str, char ch, int position) {
        StringBuilder sb = new StringBuilder(str);
        sb.insert(position, ch);
        return sb.toString();
    }

    /**
     * Choose the download directory.
     */
    private static void chooseDownloadDirectory() {
        JFileChooser chooser = new JFileChooser(getHomeDirectory());
        chooser.setDialogTitle("Select target directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            setDirectoryPath(Paths.get(chooser.getSelectedFile().getAbsolutePath()));
        } else {
            exit();
        }
    }

    /**
     * Create a file in the given file path with the given content.
     *
     * @param filePath the filePath
     * @param fileContent the fileContent
     */
    public static void createFile(String filePath, String fileContent) {
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), StandardCharsets.UTF_8));
            printWriter.println(fileContent);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a JTable for the given table model.
     *
     * @param tableModel the tableModel
     * @return the JTable
     */
    private static JTable createJTable(DefaultTableModel tableModel, boolean sortable) {
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(sortable);
        table.setRowHeight(18);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);

        MatteBorder border = new MatteBorder(0, 1, 1, 1, Color.GRAY);
        table.setBorder(border);
        border = new MatteBorder(1, 1, 1, 1, Color.GRAY);
        table.getTableHeader().setBorder(border);
        table.getTableHeader().setFont(Constants.TABLE_FONT_BOLD);
        table.getTableHeader().setForeground(Constants.TEXT_FONT_COLOR);

        return table;
    }

    /**
     * Create the download summary file.
     */
    public static void createSummaryFile() {
        createFile(Paths.get(directoryPath.toString(), directoryId, Constants.DOWNLOAD_SUMMARY_FILE_NAME).toString(), getSummaryFileContent(true));
    }

    /**
     * Exit from the download manager.
     */
    private static void exit() {
        System.exit(EXIT_ON_CLOSE);
    }

    /**
     * Get applicationTitle.
     *
     * @return the applicationTitle
     */
    public static String getApplicationTitle() {
        return applicationTitle;
    }

    /**
     * Get authStorage.
     *
     * @return the authStorage
     */
    public static String getAuthStorage() {
        return authStorage;
    }

    /**
     * Find the common file path prefix of given URL lists in a file hierarchy.
     */
    private static String getCommonFilePathPrefix() {
        ArrayList<String[]> folders = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++) {
            if (fileIds.get(i).startsWith(Constants.RESOURCE_PREFIX)) {
                // Storage on only Linux file systems.
                folders.add(getFilePath(i).split("/"));
            }
        }
        StringBuilder ret = new StringBuilder();
        if (folders.size() > 0) {
            for (int j = 0; j < folders.get(0).length - 1; j++) {
                String thisFolder = folders.get(0)[j];
                boolean allMatched = true;
                for (int i = 1; i < folders.size() && allMatched; i++) {
                    // If there is no folder here.
                    if (folders.get(i).length < j) {
                        // No match.
                        allMatched = false;
                        // Stop looking because it cannot go further.
                        break;
                    }
                    // Otherwise
                    allMatched = folders.get(i)[j].equals(thisFolder);
                }
                if (allMatched) {
                    // If they all matched this folder name, add it to the answer.
                    ret.append(thisFolder).append("/");
                } else {
                    // Stop looking.
                    break;
                }
            }
        }
        return ret.toString();
    }

    /**
     * Get common path for the given paths.
     *
     * @return the common path
     */
    public static String getCommonPath(String... paths) {
        String commonPath = "";
        String[][] folders = new String[paths.length][];
        for (int i = 0; i < paths.length; i++) {
            folders[i] = paths[i].split("/");
        }
        for (int j = 0; j < folders[0].length; j++) {
            String currentFolder = folders[0][j];
            boolean allMatched = true;
            for (int i = 1; i < folders.length && allMatched; i++) {
                if (folders[i].length < j) {
                    allMatched = false;
                    break;
                }
                allMatched &= folders[i][j].equals(currentFolder);
            }
            if (allMatched) {
                commonPath += currentFolder + "/";
            } else {
                break;
            }
        }
        return commonPath;
    }

    /**
     * Get DownloadDate.
     *
     * @return the DownloadDate
     */
    public static String getDownloadDate() {
        String date = directoryId.substring(getIndexOfSecondUnderscore() + 1);
        if (date != null) {
            date = date.replaceAll("_", " ");
            date = addChar(date, '-', 4);
            date = addChar(date, '-', 7);
            date = addChar(date, ':', 13);
            date = addChar(date, ':', 16);
        }
        return date;
    }

    /**
     * Get downloadType.
     *
     * @return the downloadType
     */
    public static DownloadTypeEnum getDownloadType() {
        return downloadType;
    }

    /**
     * Get Entity.
     *
     * @return the Entity
     */
    public static String getEntity() {
        return directoryId != null ? directoryId.substring(0, getIndexOfSecondUnderscore()) : "";
    }

    /**
     * Get fileDownloaders.
     *
     * @return the fileDownloaders
     */
    public static List<FileDownloader> getFileDownloaders() {
        return fileDownloaders;
    }

    /**
     * Get the file path for the given file in the given position.
     *
     * @return the file path
     */
    public static String getFilePath(int i) {
        return fileRelativePaths.get(i);
    }

    /**
     * Get fileSetDownloadStatus.
     *
     * @return the fileSetDownloadStatus
     */
    public static FileSetDownloadStatus getFileSetDownloadStatus() {
        return fileSetDownloadStatus;
    }

    /**
     * Get downloaded size of the fileSet.
     *
     * @return the downloaded fileSet size
     */
    public static long getFileSetDownloadedSize() {
        long downloadedSize = 0;
        for (int i = 0; i < numberOfFiles; i++) {
            downloadedSize += fileDownloaders.get(i).getDownloadedSize();
        }
        return downloadedSize;
    }

    /**
     * Get the downloaded percentage of the fileSet.
     *
     * @return the fileSet progress
     */
    private static int getFileSetProgress() {
        long fileSetSize = getFileSetSize();
        if (fileSetSize <= 0) {
            return 0;
        }
        return (int) (getFileSetDownloadedSize() * 100 / fileSetSize);
    }

    /**
     * Get fileSet size.
     *
     * @return the fileSet size
     */
    private static long getFileSetSize() {
        long fileSetSize = 0;
        for (int i = 0; i < numberOfFiles; i++) {
            fileSetSize += fileDownloaders.get(i).getFileSize();
        }
        return fileSetSize;
    }

    /**
     * Get the home directory of the user.
     *
     * @return the home directory
     */
    private static String getHomeDirectory() {
        String ret = System.getProperty("user.home");
        if (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().contains("windows") && System.getenv("HOMEDRIVE") != null && System.getenv("HOMEPATH") != null) {
            ret = Paths.get(System.getenv("HOMEDRIVE"), System.getenv("HOMEPATH")).toString();
        }
        return ret;
    }

    /**
     * Get host address.
     *
     * @return the host address.
     */
    private static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Get IndexOfSecondUnderscore.
     *
     * @return the IndexOfSecondUnderscore
     */
    public static int getIndexOfSecondUnderscore() {
        return directoryId != null ? directoryId.indexOf("_", directoryId.indexOf("_") + 1) : 0;
    }

    /**
     * Get numberOfFiles.
     *
     * @return the numberOfFiles
     */
    public static int getNumberOfFiles() {
        return numberOfFiles;
    }

    /**
     * Create the content of the summary file as a string.
     *
     * @return the summary file.
     */
    private static String getSummaryFileContent(boolean urlIncluded) {
        StringBuilder summaryFileContent = new StringBuilder();
        String title = "B-Fabric Download Summary";
        summaryFileContent.append("<html><head><meta charset=\"utf-8\"><title>").append(title).append("</title>");
        summaryFileContent.append("<style>th,td {white-space: nowrap;} a,a:link,a:visited,a:active,a:focus,.ui-state-highlight a, .ui-widget-content .ui-state-highlight a, .ui-widget-header .ui-state-highlight a { text-decoration: none; color: #003c68; } a:hover,.ui-state-highlight a:cover { color: #ea6b13; }");
        summaryFileContent.append("</style></head><body><h3>").append(title).append("</h3>");

        int missingFiles = 0;
        StringBuilder missingFilesContent = new StringBuilder();
        StringBuilder downloadedFilesContent = new StringBuilder();
        downloadedFilesContent.append("<table style=\"text-align: right\">");

        int maxLength = 0;
        for (int i = 0; i < numberOfFiles; i++) {
            int length = filePaths.get(i).substring(filePaths.get(i).indexOf(directoryId) + directoryId.length() + 1).length();
            if (length > maxLength) {
                maxLength = length;
            }
        }

        if (downloadType.isFileSet()) {
            String url = "";
            if (urlIncluded) {
                if (downloadType.equals(DownloadTypeEnum.WORKUNIT)) {
                    url = applicationURL + Constants.SHOW_WORKUNIT_PATH + directoryId.split("_")[1];
                }
                if (downloadType.equals(DownloadTypeEnum.PROJECT)) {
                    url = applicationURL + Constants.SHOW_PROJECT_PATH + directoryId.split("_")[1];
                }
                if (downloadType.equals(DownloadTypeEnum.ORDER)) {
                    url = applicationURL + Constants.SHOW_ORDER_PATH + directoryId.split("_")[1];
                }
            }

            if (directoryId.length() > maxLength) {
                maxLength = directoryId.length();
            }

            downloadedFilesContent.append(String
                .format("<tr><th style=\"text-align: left\">%s</th><th>%s</th><th style=\"text-align: left\">%s</th><th>%s</th><th style=\"text-align: left\"><a href=\"%s\">%s</a></th></tr>%n",
                    directoryId, FileSizeRenderer.getFileSizeFormatted(getFileSetSize()), getFileSetDownloadStatus().getLabel(), getFileSetProgress() + "%", url, url));
        }

        String tableRowFormat = "<tr><td style=\"text-align: left\"><a href=\"%s\">%s</a></td><td>%s</td><td style=\"text-align: left\">%s</td><td>%s</td><td style=\"text-align: left\"><a href=\"%s\">%s</a></td></tr>%n";

        for (int i = 0; i < numberOfFiles; i++) {
            String bfabricUrl = "";
            if (fileIds.get(i).startsWith(Constants.ATTACHMENT_PREFIX)) {
                String fileUrl = fileURLs.get(i).toString();
                if (fileUrl.contains(Constants.COMMENT_PREFIX) || fileUrl.contains(Constants.RESULT_PREFIX) || fileUrl.contains(Constants.NOTE_PREFIX)) {
                    String right = null;
                    if (fileUrl.contains(Constants.COMMENT_PREFIX)) {
                        right = fileUrl.split(Constants.COMMENT_PREFIX)[1];
                    } else if (fileUrl.contains(Constants.RESULT_PREFIX)) {
                        right = fileUrl.split(Constants.RESULT_PREFIX)[1];
                    } else if (fileUrl.contains(Constants.NOTE_PREFIX)) {
                        right = fileUrl.split(Constants.NOTE_PREFIX)[1];
                    }
                    String commentId = right.substring(0, right.indexOf("/"));
                    bfabricUrl = applicationURL + Constants.SHOW_COMMENT_PATH + commentId;
                }
            } else if (fileIds.get(i).startsWith(Constants.RESOURCE_PREFIX)) {
                bfabricUrl = applicationURL + Constants.SHOW_RESOURCE_PATH + fileIds.get(i).split("_")[1];
            }

            String pathSubstring = filePaths.get(i).substring(filePaths.get(i).indexOf(directoryId) + directoryId.length() + 1);
            if (fileDownloaders.get(i).getFileDownloadStatus().isMissing()) {
                missingFiles++;
                missingFilesContent.append(String.format(tableRowFormat, pathSubstring, pathSubstring, "", fileDownloaders.get(i).getFileDownloadStatus().getLabel(), "", bfabricUrl, bfabricUrl));
            } else {
                downloadedFilesContent.append(String.format(tableRowFormat, pathSubstring, pathSubstring, FileSizeRenderer.getFileSizeFormatted(fileDownloaders.get(i).getFileSize()),
                    fileDownloaders.get(i).getFileDownloadStatus().getLabel() + (fileDownloaders.get(i).getFileDownloadStatus()
                        .isFileChecksumTestFailed() ? " (Checksum check failed!)" : fileDownloaders.get(i).getFileDownloadStatus()
                        .isFileSizeTestFailed() ? " (File size incorrect!)" : ""), fileDownloaders.get(i).getProgress() + "%", bfabricUrl, bfabricUrl));
            }
        }

        summaryFileContent.append("<p><b>").append(getEntity()).append("</b> download on <b>").append(getDownloadDate()).append("</b> via <b>").append(getHostAddress()).append("</b></p>");
        summaryFileContent.append("<p>Directory Structure: <b>").append(directoryStructure).append("</b> Downloaded Files: <b>").append(numberOfFiles - missingFiles).append("</b>");
        if (missingFiles > 0) {
            summaryFileContent.append(" Missing Files: <b>").append(missingFiles).append("</b>");
        }
        summaryFileContent.append("</p><br/>");
        if (!downloadedFilesContent.toString().isEmpty()) {
            summaryFileContent.append(downloadedFilesContent);
        }
        if (!missingFilesContent.toString().isEmpty()) {
            summaryFileContent.append(missingFilesContent);
        }
        summaryFileContent.append("</table>");
        if (missingFiles > 0) {
            summaryFileContent.append("<p>").append("Please contact the corresponding container coach for the missing files!</p>");
        }

        return summaryFileContent.toString();
    }

    /**
     * Initialize the download manager.
     *
     * @param args the args
     */
    private static void initialize(String[] args) {
        try {
            applicationTitle = args[0];
            String jarURL = new URL(args[1]).toString();
            applicationURL = jarURL.replace(Constants.JAR_FILE_PATH, "");
            downloadType = DownloadTypeEnum.getDownloadTypeEnum(args[2]);
            directoryId = args[3];
            directoryPath = Constants.NULL.equals(args[4]) ? DEFAULT_DIRECTORY_PATH : Paths.get(args[4]);
            directoryEditable = Boolean.parseBoolean(args[5]);
            directoryStructure = Constants.NULL.equals(args[6]) ? Constants.PLAIN : args[6];
            numberOfFiles = Integer.parseInt(args[7]);
            authStorage = args[8];
            try {
                String authStorageDecoded = URLDecoder.decode(authStorage, "UTF-8").toLowerCase();
                Pattern pattern = Pattern.compile("validuntil=[0-9]*");
                Matcher matcher = pattern.matcher(authStorageDecoded);
                if (matcher.find()) {
                    expirationDateTime = LocalDateTime.ofEpochSecond(Long.valueOf(matcher.group().replace("validuntil=", "")), 0, ZoneOffset.UTC);
                }
            } catch (Exception e) {
                expirationDateTime = null;
            }
            int logId = Integer.parseInt(args[9]);
            logCode = args[10];
            logUrl = jarURL.substring(0, jarURL.indexOf(Constants.BFABRIC_PATH_PREFIX) + Constants.BFABRIC_PATH_PREFIX.length() - 1) + "/logdownloadstatus?id=" + logId + "&status=";
            fileIds = new ArrayList<>();
            parentIds = new ArrayList<>();
            filePaths = new ArrayList<>();
            fileURLs = new ArrayList<>();
            fileChecksums = new ArrayList<>();
            fileRelativePaths = new ArrayList<>();

            for (int i = 0; i < numberOfFiles; i++) {
                String[] resource = args[i + 11].split(", ");
                fileIds.add(resource[0]);
                parentIds.add(resource[1].equals(Constants.NULL) ? null : resource[1]);
                fileURLs.add(new URL(resource[2]));
                fileRelativePaths.add(resource[3]);
                fileChecksums.add(resource[4].equals(Constants.NULL) ? null : resource[4]);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the file paths.
     */
    private static void initializeFilePaths() {
        filePaths.clear();
        String commonFilePathPrefix = getCommonFilePathPrefix();
        for (int i = 0; i < numberOfFiles; i++) {
            String fileId = fileIds.get(i);
            Path path = Paths.get(getFilePath(i));
            if (path.getFileName() != null) {
                String fileName = path.getFileName().toString();
                String parent = parentIds.get(i);
                if (parent == null || parent.startsWith(Constants.PROJECT_PREFIX) || parent.startsWith(Constants.ORDER_PREFIX)) {
                    parent = "";
                }

                String root = Paths.get(directoryPath.toString(), directoryId).toString();
                String intermediary;
                String leaf;
                if (fileId.startsWith(Constants.ATTACHMENT_PREFIX)) {
                    intermediary = Constants.ATTACHMENTS_DOWNLOAD_FOLDER;
                    switch (directoryStructure) {
                    case Constants.PLAIN:
                        leaf = fileName;
                        break;
                    case Constants.STORAGE:
                    case Constants.BFABRIC:
                    default:
                        leaf = Paths.get(parent, path.getParent().getFileName().toString(), fileName).toString();
                    }
                } else if (fileId.startsWith(Constants.RESOURCE_PREFIX)) {
                    intermediary = Constants.RESOURCES_DOWNLOAD_FOLDER;
                    if (downloadType.equals(DownloadTypeEnum.RESOURCE)) {
                        intermediary = "";
                    }
                    switch (directoryStructure) {
                    case Constants.PLAIN:
                        leaf = fileName;
                        break;
                    case Constants.STORAGE:
                        leaf = Paths.get(getFilePath(i).substring(Math.max(commonFilePathPrefix.length() - 1, 0))).toString();
                        break;
                    case Constants.BFABRIC:
                    default:
                        leaf = fileName;
                        if (downloadType.equals(DownloadTypeEnum.PROJECT) || downloadType.equals(DownloadTypeEnum.ORDER)) {
                            leaf = Paths.get(parent, fileId, fileName).toString();
                        }
                        break;
                    }
                } else {
                    intermediary = "";
                    leaf = Constants.METADATA_FILE_NAME;
                }
                Path filePath = Paths.get(root, intermediary, leaf);
                filePaths.add(filePath.toString());
            }
        }
        resolveDuplicates();
    }

    /**
     * Is expired.
     *
     * @return true if is the case; false otherwise.
     */
    private static boolean isExpired() {
        return expirationDateTime != null && expirationDateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Is invalid.
     *
     * @return true if is the case; false otherwise.
     */
    private static boolean isInvalid() {
        return expirationDateTime == null;
    }

    /**
     * Log download status.
     */
    public static void logDownloadStatus() {
        try {
            String status = getFileSetDownloadStatus().getLabel().toLowerCase();
            String ip = getHostAddress();
            String code = logCode + status.length() + "c1a3Nt" + (ip != null ? ip.length() : 0);
            String summary = URLEncoder.encode(getSummaryFileContent(false), "UTF-8");
            URL connectionURL = new URL(logUrl + status + "&ip=" + ip + "&code=" + code + "&summary=" + summary);
            HttpURLConnection connection = (HttpURLConnection) connectionURL.openConnection();
            connection.setRequestMethod("GET");
            connection.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method.
     *
     * @param args the args
     */
    public static void main(String[] args) {
        // Create a frame (JFrame) and make an applet of the content pane.
        try {
            initialize(args);
            frame = new DownloadManager();
            frame.setTitle(applicationTitle);
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(DownloadManager.class.getResource(Constants.ICON_PATH)));
            JFrame.setDefaultLookAndFeelDecorated(true);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    if (!getFileSetDownloadStatus().isDone() && !getFileSetDownloadStatus().isFailed()) {
                        List<String> messages = new ArrayList<>();
                        messages.add("Do you really want to close the download manager and stop the current download?");
                        if (!downloadType.isResourceBasket()) {
                            messages.add("You may later resume it from its " + downloadType.getName().toLowerCase() + " screen on the B-Fabric.");
                            messages.add("Or you may also later resume it by executing your JNLP file.");
                        } else {
                            messages.add("You may later resume it by executing your JNLP file.");
                        }
                        List<String> buttons = new ArrayList<>();
                        buttons.add("Close");
                        buttons.add("Cancel");
                        CustomDialog exitDialog = new CustomDialog(frame, true, messages, buttons);
                        switch (exitDialog.getChoice()) {
                        case 0:
                            // Close button pressed.
                            stopDownload();
                            openDownloadFolder();
                            exit();
                            break;
                        case 1:
                            // Cancel button pressed.
                        default:
                            // Exit button pressed.
                            break;
                        }
                    } else {
                        openDownloadFolder();
                        exit();
                    }
                }
            });
            frame.setLocationByPlatform(true);
            frame.setResizable(false);
            // Arrange the components.
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open the download folder.
     */
    public static void openDownloadFolder() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    File directory = Paths.get(directoryPath.toString(), directoryId).toFile();
                    if (directory.exists()) {
                        desktop.open(directory);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Resolve duplicates.
     */
    private static void resolveDuplicates() {
        // find duplicate file paths
        List<String> copyFilePaths = new ArrayList<>(filePaths);
        for (String filePath : new HashSet<>(filePaths)) {
            copyFilePaths.remove(filePath);
        }
        Set<String> duplicatesFilePaths = new HashSet<>(copyFilePaths);

        // find duplicate file checksums
        List<String> copyFileChecksums = new ArrayList<>(fileChecksums);
        for (String fileChecksum : new HashSet<>(fileChecksums)) {
            copyFileChecksums.remove(fileChecksum);
        }
        Set<String> duplicatesFileChecksums = new HashSet<>(copyFileChecksums);

        Map<String, ArrayList<Integer>> duplicateMap = new HashMap<>();
        Set<Integer> ignoredIndexes = new TreeSet<>(Collections.reverseOrder());
        for (int i = 0; i < numberOfFiles; i++) {
            String filePath = filePaths.get(i);
            String fileChecksum = fileChecksums.get(i);
            if (duplicatesFilePaths.contains(filePath)) {
                if (fileChecksum != null && duplicatesFileChecksums.contains(fileChecksum)) {
                    // Duplicate file
                    if (!duplicateMap.containsKey(filePath)) {
                        // The file will not be ignored.
                        duplicateMap.put(filePath, new ArrayList<>());
                    } else {
                        // The file will be ignored.
                        ignoredIndexes.add(i);
                    }
                    duplicateMap.get(filePath).add(i);
                } else {
                    // Concat resource ids to the end of file paths.
                    if (filePath.contains(".")) {
                        filePaths.set(i, filePath.substring(0, filePath.lastIndexOf(".")) + "_" + fileIds.get(i) + filePath.substring(filePath.lastIndexOf(".")));
                    } else {
                        filePaths.set(i, filePath + "_" + fileIds.get(i));
                    }
                }
            }
        }

        // Removes the duplicates from the lists.
        for (int i : ignoredIndexes) {
            fileIds.remove(i);
            filePaths.remove(i);
            fileURLs.remove(i);
            fileRelativePaths.remove(i);
            fileChecksums.remove(i);
            numberOfFiles--;
        }
    }

    /**
     * Remove files or directories.
     *
     * @param file the file to be deleted
     * @return true if the given file / directory and all its sub-files are deleted, false otherwise
     */
    private static boolean rm(File file) {
        boolean succeed = true;
        if (file != null) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children != null) {
                    for (File child : children) {
                        if (child.isDirectory()) {
                            succeed &= rm(child);
                        }
                        succeed &= child.delete();
                    }
                }
            }
            succeed &= file.delete();
        }
        return succeed;
    }

    /**
     * Set directoryPath.
     *
     * @param directoryPath the directoryPath to set
     */
    private static void setDirectoryPath(Path directoryPath) {
        DownloadManager.directoryPath = directoryPath;
    }

    /**
     * Set fileDownloaders.
     *
     * @param fileDownloaders the fileDownloaders to set
     */
    private static void setFileDownloaders(List<FileDownloader> fileDownloaders) {
        DownloadManager.fileDownloaders = fileDownloaders;
    }

    /**
     * Set fileSetDownloadStatus.
     *
     * @param fileSetDownloadStatus the fileSetDownloadStatus to set
     */
    private static void setFileSetDownloadStatus(FileSetDownloadStatus fileSetDownloadStatus) {
        DownloadManager.fileSetDownloadStatus = fileSetDownloadStatus;
    }

    /**
     * Set lastDownloadedSize.
     *
     * @param lastDownloadedSize the lastDownloadedSize to set
     */
    public static void setLastDownloadedSize(long lastDownloadedSize) {
        DownloadManager.lastDownloadedSize = lastDownloadedSize;
    }

    /**
     * Set timeStarted.
     *
     * @param timeStarted the timeStarted to set
     */
    public static void setTimeStarted(long timeStarted) {
        DownloadManager.timeStarted = timeStarted;
    }

    /**
     * Stop the download.
     */
    private static void stopDownload() {
        for (int i = 0; i < numberOfFiles; i++) {
            fileDownloaders.get(i).getFileDownloadStatus().stop();
        }
        if (getFileSetDownloadStatus() != null) {
            getFileSetDownloadStatus().resetStatus();
        }
    }

    /**
     * The LoopState Enum.
     */
    private enum LoopState {
        RUN,
        DONE,
        STOP
    }

    /**
     * The UIUpdater class.
     */
    private class UIUpdater implements Runnable {

        private double averageSpeed = 0;

        private double lastSpeed = 0;

        private long oldFileSetDownloadedSize = 0;

        /**
         * Format the time such that it can be printed for showing the estimated remaining download time.
         *
         * @param time the time
         * @return the formatted time
         */
        public String formatTime(long time) {
            long days = (int) TimeUnit.SECONDS.toDays(time);
            long hours = TimeUnit.SECONDS.toHours(time) - (days * 24);
            long minutes = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time) * 60);
            long seconds = TimeUnit.SECONDS.toSeconds(time) - (TimeUnit.SECONDS.toMinutes(time) * 60);
            return (days != 0 ? days + "d " : "") + (hours != 0 ? hours + "h " : "") + (minutes != 0 ? minutes + "m " : "") + seconds + "s";
        }

        @Override
        public void run() {
            LoopState loop = LoopState.RUN;
            while (loop != LoopState.STOP) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore
                }

                // Single file (resource) download.
                for (int i = 0; i < numberOfFiles; i++) {
                    filesModel.setValueAt(fileDownloaders.get(i).getFileSizePadded(), i, 1);
                    filesModel.setValueAt(fileDownloaders.get(i).getFileDownloadStatus().getLabel() + (fileDownloaders.get(i).getFileDownloadStatus()
                        .isFileChecksumTestFailed() ? " (Checksum check failed!)" : fileDownloaders.get(i).getFileDownloadStatus()
                        .isFileSizeTestFailed() ? " (File size incorrect!)" : ""), i, 2);
                    filesModel.setValueAt(fileDownloaders.get(i).getProgressFormatted(), i, 3);
                    filesModel.setValueAt(fileDownloaders.get(i).getFileDownloadStatus().getLabel(), i, 4);
                }

                long currentFileSetDownloadedSize = getFileSetDownloadedSize();
                lastDownloadedSize += currentFileSetDownloadedSize - oldFileSetDownloadedSize;
                oldFileSetDownloadedSize = currentFileSetDownloadedSize;

                // If it is a file set download.
                if (downloadType.isFileSet()) {
                    headerModel.setValueAt(getFileSetSize(), 0, 1);
                    headerModel.setValueAt(fileSetDownloadStatus.getLabel(), 0, 2);
                    headerModel.setValueAt(getFileSetProgress(), 0, 3);
                    headerModel.setValueAt(null, 0, 4);
                    headerModel.setValueAt(null, 0, 5);
                }

                if (getFileSetDownloadStatus().isRunning()) {
                    // Compute the estimated remaining download time.
                    if (lastSpeed == 0) {
                        averageSpeed = 0;
                    }

                    long timeElapsed = System.currentTimeMillis() - timeStarted;
                    lastSpeed = (double) lastDownloadedSize / timeElapsed;

                    if (averageSpeed == 0) {
                        averageSpeed = lastSpeed;
                    }
                    // Exponential moving average
                    double SMOOTHING_FACTOR = 0.5;
                    averageSpeed = SMOOTHING_FACTOR * lastSpeed + (1 - SMOOTHING_FACTOR) * averageSpeed;
                    long timeRemained = (long) ((getFileSetSize() - getFileSetDownloadedSize()) / averageSpeed);

                    footerModel.setValueAt(getFileSetSize() > 0 ? formatTime(timeRemained / 1000) : Constants.CALCULATING, 0, 1);
                } else {
                    footerModel.setValueAt(getFileSetDownloadStatus().getLabel(), 0, 1);
                }

                if (getFileSetDownloadStatus().isDone()) {
                    loop = loop == LoopState.DONE ? LoopState.STOP : LoopState.DONE;
                }
            }
        }
    }
}
