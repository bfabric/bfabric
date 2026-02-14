package org.bfabric.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.security.auth.message.AuthException;

import org.apache.commons.codec.digest.DigestUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractResource;
import org.bfabric.entity.Configuration;
import org.bfabric.exception.InvalidDataException;
import org.omnifaces.util.Faces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.virusscan.impl.ClamDaemonClient;
import org.primefaces.virusscan.impl.ClamDaemonScanner;

public class FileHelper {

    private static final Logger logger = Logger.getLogger(FileHelper.class.getName());

    public static void addToZipFile(String prefix, File file, ZipOutputStream zipOutputStream) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry((StringHelper.isNotEmpty(prefix) ? prefix : Constants.EMPTY_STRING) + file.getName());
            zipOutputStream.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fileInputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }

            zipOutputStream.closeEntry();
            fileInputStream.close();
        } catch (IOException e) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw new IOException(e);
        }
    }

    public static String calculateHash(InputStream uploadedFile) {
        String hash;
        try {
            hash = DigestUtils.md5Hex(uploadedFile);
        } catch (IOException e) {
            hash = null;
        }
        return hash;
    }

    public static void checkFileAccessibility(File file) throws FileNotFoundException, AuthException {
        if (!file.exists()) {
            throw new FileNotFoundException("The file " + file.getAbsolutePath() + " does not exist.");
        }
        if (!file.canRead()) {
            throw new AuthException("No permission to read the file " + file.getAbsolutePath() + ".");
        }
    }

    public static void download(String targetFileName, String fileContent) {
        Faces.sendFile(targetFileName, true, outputStream -> {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
                bufferedWriter.write(fileContent);
            }
        });
    }

    private static String fileExists(BfabricUploadedFile uploadedFile, Set<String> fileNamesToCheck, Set<String> fileChecksumsToCheck) {
        if (fileNamesToCheck != null && fileNamesToCheck.contains(uploadedFile.getFileName())) {
            return Messages.get("attachmentNameNotUniqueWithinComment").replace("{0}", uploadedFile.getFileName());
        }
        if (fileChecksumsToCheck != null && fileChecksumsToCheck.contains(calculateHash(uploadedFile.getInputStream()))) {
            return Messages.get("attachmentSameContentWithinComment").replace("{0}", uploadedFile.getFileName());
        }
        return null;
    }

    public static String fileExistsAmongAbstractResources(BfabricUploadedFile uploadedFile, Set<AbstractResource> abstractResources) {
        Set<String> fileNamesToCheck = new HashSet<>();
        Set<String> fileChecksumsToCheck = new HashSet<>();
        if (abstractResources != null) {
            for (AbstractResource abstractResource : abstractResources) {
                fileNamesToCheck.add(abstractResource.getFileName());
                fileChecksumsToCheck.add(abstractResource.getFileChecksum());
            }
        }
        return fileExists(uploadedFile, fileNamesToCheck, fileChecksumsToCheck);
    }

    public static String fileExistsAmongBfabricUploadedFiles(BfabricUploadedFile uploadedFile, Set<BfabricUploadedFile> uploadedFiles) {
        Set<String> fileNamesToCheck = new HashSet<>();
        Set<String> fileChecksumsToCheck = new HashSet<>();
        if (uploadedFiles != null) {
            for (BfabricUploadedFile file : uploadedFiles) {
                fileNamesToCheck.add(file.getFileName());
                fileChecksumsToCheck.add(file.getFileChecksum());
            }
        }
        return fileExists(uploadedFile, fileNamesToCheck, fileChecksumsToCheck);
    }

    public static String formatBasePath(String value) {
        if (StringHelper.isNotEmpty(value)) {
            String formattedValue = StringHelper.format(value);
            if (formattedValue != null) {
                Path path = Paths.get(formattedValue);
                return path.endsWith("/") ? path.toString() : path + "/";
            }
            return null;
        }
        return value;
    }

    public static DefaultStreamedContent getDefaultStreamedContent(InputStream inputStream, String contentType, String fileName) {
        return DefaultStreamedContent.builder().stream(() -> inputStream).contentType(contentType).name(fileName).build();
    }

    public static String getFileName(String fullPath) throws InvalidDataException {
        String fileName = null;
        if (StringHelper.isNotEmpty(fullPath)) {
            File file = new File(fullPath);
            fileName = file.getName();
            if (StringHelper.isEmpty(fileName)) {
                throw new InvalidDataException("Invalid file name in fullPath " + fullPath + ".");
            }
        }
        return fileName;
    }

    public static String isValid(BfabricUploadedFile uploadedFile, Set<BfabricUploadedFile> bfabricUploadedFilesToCheck, Set<AbstractResource> abstractResourcesToCheck) throws IOException {
        Configuration configuration = ConfigurationHelper.getConfiguration();
        if (configuration != null) {
            if (!configuration.isVirusScannerDisabled()) {
                String errorMsg = virusScan(uploadedFile);
                if (errorMsg != null) {
                    return errorMsg;
                }
            }
            long maxAttachmentFiles = configuration.getMaxAttachmentFiles();
            if ((bfabricUploadedFilesToCheck != null ? bfabricUploadedFilesToCheck.size() + 1 : abstractResourcesToCheck.size() + 1) > maxAttachmentFiles) {
                return Messages.get("invalidFileAmountMessage").replace("{0}", String.valueOf(maxAttachmentFiles));
            }
            if (uploadedFile.getSize() > configuration.getMaxAttachmentSize()) {
                return Messages.get("invalidSizeMessage") + uploadedFile.getFileName() + "(" + uploadedFile.getSize() + ")";
            }
            if (bfabricUploadedFilesToCheck != null) {
                String errorMsg = fileExistsAmongBfabricUploadedFiles(uploadedFile, bfabricUploadedFilesToCheck);
                if (errorMsg != null) {
                    return errorMsg;
                }
            }
            if (abstractResourcesToCheck != null) {
                return fileExistsAmongAbstractResources(uploadedFile, abstractResourcesToCheck);
            }
        }
        return null;
    }

    public static String virusScan(UploadedFile uploadedFile) {
        ClamDaemonScanner scanner = new ClamDaemonScanner();
        ClamDaemonClient client = scanner.getClamAvClient();
        try {
            if (client.ping()) {
                try {
                    scanner.scan(uploadedFile);
                } catch (Exception e) {
                    String msg = Messages.get("virusScannerInfectedFileReport").replace("{0}", uploadedFile.getFileName());
                    logger.warning(msg);
                    return msg;
                }
            }
        } catch (Exception e) {
            String msg = Messages.get("virusScannerPingError").replace("{0}", client.getHost()).replace("{1}", String.valueOf(client.getPort()))
                .replace("{2}", String.valueOf(client.getTimeout()));
            logger.severe(msg);
            return msg;
        }
        return null;
    }
}
