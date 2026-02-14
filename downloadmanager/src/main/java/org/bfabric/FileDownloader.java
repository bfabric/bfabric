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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bfabric.enums.DownloadStatusEnum;

public class FileDownloader extends Thread {

    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;

    // HTTP response codes
    private static final int HTTP_OK = 200;

    private static final int HTTP_REDIRECT_TEMP = 307; // normally, 3xx stands for redirect

    private final String fileChecksum;

    private final String filePath;

    private final URL fileURL;

    // Attributes.
    private long downloadedSize;

    private FileDownloadStatus fileDownloadStatus;

    private long fileSize;

    private MessageDigest messageDigest;

    /**
     * Constructor.
     *
     * @param filePath the filePath
     * @param fileURL the fileURL
     * @param fileChecksum the fileChecksum
     * @param parentFileSetDownloadStatus In case of FileSetDownload the parent will be non-null; otherwise in case of a single FileDownload it will be null.
     */
    public FileDownloader(String filePath, URL fileURL, String fileChecksum, FileSetDownloadStatus parentFileSetDownloadStatus) {
        this.filePath = filePath;
        this.fileURL = fileURL;
        this.fileChecksum = fileChecksum;
        this.fileDownloadStatus = new FileDownloadStatus(DownloadStatusEnum.READY);

        if (parentFileSetDownloadStatus != null) {
            this.fileDownloadStatus.setParentFileSetDownloadStatus(parentFileSetDownloadStatus);
        }

        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // ignore
        }
    }

    /**
     * Open new url connection.
     *
     * @param url the rule
     * @return a fresh instance of HttpURLConnection
     * @throws IOException the {@link IOException}
     */
    private static HttpURLConnection openConnection(URL url) throws IOException {
        // Add Single Sign On cookie
        String cookie = DownloadManager.getAuthStorage();

        // Open connection to URL.
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Cookie", cookie);

        int status = connection.getResponseCode();
        connection.disconnect();

        if (status == HTTP_OK || status == HTTP_REDIRECT_TEMP) {
            // open the new connection again
            if (status == HTTP_OK) {
                connection = (HttpURLConnection) url.openConnection();
            } else {
                String redirectURL = connection.getHeaderField("Location");
                connection = (HttpURLConnection) new URL(redirectURL).openConnection();
            }

            connection.setRequestProperty("Cookie", cookie);

            // Ignore cache
            connection.setUseCaches(false);
        } else {
            connection = null;
        }

        return connection;
    }

    /**
     * Enqueue the download.
     */
    public void calculateDownload() {
        getFileDownloadStatus().calculate();
        DownloadManager.calculateExecutor.execute(this);
    }

    /**
     * Calculate the checksum of the downloaded file.
     *
     * @return the checksum of the downloaded file
     */
    private String calculateHash() {
        byte[] bytes = messageDigest.digest();

        // convert the byte to hex format
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            hexString.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return hexString.toString();
    }

    /**
     * Enqueue the download.
     */
    public void enqueueDownload() {
        getFileDownloadStatus().enqueue();
        DownloadManager.downloadExecutor.execute(this);
    }

    /**
     * Get downloadedSize.
     *
     * @return the downloadedSize
     */
    public long getDownloadedSize() {
        return downloadedSize;
    }

    /**
     * Get fileChecksum.
     *
     * @return the fileChecksum
     */
    public String getFileChecksum() {
        return fileChecksum;
    }

    /**
     * Get filedownloadStatus.
     *
     * @return the filedownloadStatus
     */
    public FileDownloadStatus getFileDownloadStatus() {
        return fileDownloadStatus;
    }

    /**
     * Get filePath.
     *
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Get fileSize.
     *
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Get the formatted version of fileSize.
     *
     * @return the formatted version of fileSize
     */
    public String getFileSizePadded() {
        return String.format("%1$012d", fileSize);
    }

    /**
     * Get fileURL.
     *
     * @return the fileURL
     */
    public URL getFileURL() {
        return fileURL;
    }

    /**
     * Get the downloaded percentage of the file.
     *
     * @return the downloaded percentage of the file
     */
    public int getProgress() {
        int ret;
        try {
            ret = (int) (downloadedSize * 100 / fileSize);
        } catch (ArithmeticException e) {
            ret = 0;
        }
        return ret;
    }

    /**
     * Get formatted version of downloaded percentage of the file.
     *
     * @return the formatted version of downloaded percentage of the file
     */
    public String getProgressFormatted() {
        String format = "%1$03d";
        return String.format(format, getProgress());
    }

    /**
     * Download the file.
     */
    @SuppressWarnings("resource")
    @Override
    public void run() {
        HttpURLConnection connection = null;
        RandomAccessFile file = null;
        try {
            if (getFileDownloadStatus().isQueued()) {
                getFileDownloadStatus().start();
            }
            // Open connection to URL.
            connection = openConnection(fileURL);
            if (connection != null) {
                // Update message
                updateHash();

                // Create parent directory if not exists already
                Files.createDirectories(Paths.get(filePath).getParent());

                // Open file and seek to the end of it.
                file = new RandomAccessFile(filePath, "rw");
                downloadedSize = file.length();
                file.seek(downloadedSize);
                fileSize = downloadedSize;

                // Specify what portion of file to download.
                connection.setRequestProperty("Range", "bytes=" + downloadedSize + "-");

                // Check for valid content length.
                long contentLength = connection.getContentLengthLong();
                if (contentLength > 0) {
                    // Set the size for the download if it hasn't been already set.
                    fileSize += contentLength;
                    InputStream stream = connection.getInputStream();
                    byte[] chunk = new byte[MAX_BUFFER_SIZE];
                    int nread;
                    while (getFileDownloadStatus().isRunning()) {
                        // Read from server into buffer.
                        if ((nread = stream.read(chunk)) <= 0) {
                            break;
                        }
                        // Update messageDigest.
                        messageDigest.update(chunk, 0, nread);
                        // Write buffer to file.
                        file.write(chunk, 0, nread);
                        downloadedSize += nread;
                    }
                }

                if (getFileDownloadStatus().isRunning()) {
                    getFileDownloadStatus().done();
                    getFileDownloadStatus().setFileChecksumTestFailed(fileChecksum != null && !fileChecksum.equals(calculateHash()));
                    getFileDownloadStatus().setFileSizeTestFailed(fileSize != downloadedSize);
                }

                if (getFileDownloadStatus().isCalculating()) {
                    enqueueDownload();
                }
            } else { // File not found
                getFileDownloadStatus().missing();
                fileSize = 0;
                downloadedSize = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getFileDownloadStatus().isRunning()) {
                getFileDownloadStatus().failed();
            } else {
                getFileDownloadStatus().missing();
            }
        } finally {
            try {
                // Close the file.
                if (file != null) {
                    file.close();
                }
                // Close connection to server.
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                // not interested in the exception.
            }
        }
    }

    /**
     * Set fileDownloadStatus.
     *
     * @param fileDownloadStatus the fileDownloadStatus to set
     */
    public void setFileDownloadStatus(FileDownloadStatus fileDownloadStatus) {
        this.fileDownloadStatus = fileDownloadStatus;
    }

    /**
     * Update hash.
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private void updateHash() {
        if (getFileDownloadStatus().isCalculating() && fileChecksum != null) {
            // Update messageDigest for already downloaded.
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(filePath);
                byte[] chunk = new byte[MAX_BUFFER_SIZE];
                int nread;
                while (!((nread = fis.read(chunk)) < 0)) {
                    messageDigest.update(chunk, 0, nread);
                }
            } catch (Exception e) {
                // no file, not initialized yet.
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }
}
