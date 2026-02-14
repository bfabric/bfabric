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

package org.bfabric.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import org.apache.commons.io.FileUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.Executable;
import org.bfabric.entity.Storage;
import org.bfabric.entity.api.ResourceDependent;
import org.bfabric.service.StorageService;

public class RepositoryHelper {

    private static final Logger logger = Logger.getLogger(RepositoryHelper.class.getName());

    private static void createDirectoryIfNotExists(File directory) throws IOException {
        if (directory != null) {
            createDirectoryIfNotExists(Paths.get(directory.getAbsolutePath()));
        } else {
            logger.info("createDirectoryIfNotExists called with null");
        }
    }

    private static void createDirectoryIfNotExists(Path directory) throws IOException {
        if (directory != null && !Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new IOException("Directory " + directory + " cannot be created. Access to local storage is not configured correctly. Please report to technical support!");
            }
        } else {
            logger.fine("Directory already exists: " + directory);
        }
    }

    private static void createFile(String filePath, String fileContent) {
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(filePath)), StandardCharsets.UTF_8));
            printWriter.println(fileContent);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createImport(ResourceDependent abstractResource) throws IOException {
        final File file = new File(abstractResource.getAbsolutePathFM());
        final File directory = new File(file.getParent());

        logger.fine("createImport = " + abstractResource.getAbsolutePathFM() + " / file.getParent() = " + file.getParent());

        createDirectoryIfNotExists(directory);

        Files.copy(abstractResource.getUploadedFile().getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        if (!file.exists()) {
            logger.info("FILE UPLOAD FAILED: " + abstractResource.getAbsolutePathFM());
        }

        if (abstractResource instanceof Executable) {
            File executableFile = new File(abstractResource.getAbsolutePathFM());
            if (!executableFile.setExecutable(true)) {
                logger.severe("File " + abstractResource.getAbsolutePathFM() + " was not made executable.");
            }
        }
    }

    public static String createMetadataDownloadFile(AbstractBaseEntity entity, String timestamp) throws IOException {
        if (entity != null && timestamp != null) {
            String suffix = File.separator + Messages.get("configureDownloadMetadataDirectory") + File.separator + Constants.DOWNLOAD_METADATA_FILE_NAME;
            suffix = suffix.replace(".", "_" + timestamp + "."); // added time stamp to end of file name
            Path absolutePath = Paths.get(getLocalStorage(false).getBasePath() + entity.getMetadataRepositoryPath() + File.separator + suffix);
            createDirectoryIfNotExists(absolutePath.getParent());
            createFile(absolutePath.toString(), entity.getMetadataExport(timestamp));
            return entity.getMetadataRepositoryPath() + suffix;
        }
        return null;
    }

    private static void deleteDirectoryAndParentsIfEmpty(File commentDirectory) {
        File localInternalDirectory = new File(getLocalStorage(true).getBasePath());
        File localExternalDirectory = new File(getLocalStorage(false).getBasePath());
        File localTemporaryDirectory = new File(getTemporaryStorage().getBasePath());

        File directoryToDelete = commentDirectory;

        try {
            while (!directoryToDelete.equals(localInternalDirectory) && !directoryToDelete.equals(localExternalDirectory) && !directoryToDelete.equals(localTemporaryDirectory)) {
                File parentDirectory = directoryToDelete.getParentFile();

                String[] directoryToDeleteContent = directoryToDelete.list();
                if (directoryToDeleteContent != null && directoryToDeleteContent.length == 0) {
                    FileUtils.forceDelete(directoryToDelete);
                    logger.fine("Delete directory " + directoryToDelete.getName());
                }

                directoryToDelete = parentDirectory;
            }
        } catch (Exception e) {
            logger.severe("Cleaning parent directories for comment directory " + commentDirectory + " failed: " + e);
        }
    }

    public static Storage getLocalStorage(Boolean internal) {
        if (internal) {
            return CDI.current().select(StorageService.class).get().getStorageByName(Constants.LOCAL_INTERNAL_STORAGE);
        }
        return CDI.current().select(StorageService.class).get().getStorageByName(Constants.LOCAL_EXTERNAL_STORAGE);
    }

    public static Storage getTemporaryStorage() {
        return CDI.current().select(StorageService.class).get().getStorageByName(Constants.LOCAL_TEMPORARY_STORAGE);
    }

    public static void moveImports(File fromPath, File toPath) {
        if (fromPath != null && toPath != null && fromPath.exists()) {
            try {
                if (toPath.exists()) {
                    if (fromPath.listFiles() != null) {
                        for (File attachment : Objects.requireNonNull(fromPath.listFiles())) {
                            FileUtils.copyFileToDirectory(attachment, toPath, false);
                        }
                    } else {
                        logger.info(Messages.get("couldNotRemoveAttachmentEmptyListHint").replace("{0}", fromPath.getAbsolutePath()));
                    }
                } else {
                    FileUtils.copyDirectory(fromPath, toPath);
                }

                File[] attachments = fromPath.listFiles();
                if (attachments != null) {
                    for (File attachment : attachments) {
                        removeImport(attachment);
                    }
                }
            } catch (Exception e) {
                logger.severe(Messages.get("couldNotMoveAttachmentFromTo").replace("{0}", fromPath.getAbsolutePath()).replace("{1}", toPath.getAbsolutePath()) + " " + e);
            }
        }
    }

    public static void removeImport(ResourceDependent resourceDependent) {
        String path = resourceDependent.getAbsolutePathFM();
        if (path != null) {
            removeImport(new File(path));
            resourceDependent.setSize(0);
            resourceDependent.setFileChecksum(null);
            resourceDependent.setRelativePath(null);
            resourceDependent.setStorage(null);
            resourceDependent.setDeleted();
        }
    }

    private static void removeImport(File file) {
        final File commentDirectory = file.getParentFile();
        try {
            FileUtils.forceDelete(file);
            logger.fine("Remove attachment successful: " + file);
        } catch (Exception e) {
            logger.severe("Remove attachment failed: " + file + " " + e);
        }
        deleteDirectoryAndParentsIfEmpty(commentDirectory);
    }
}
