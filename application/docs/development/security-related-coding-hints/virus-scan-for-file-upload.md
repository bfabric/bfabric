# B-Fabric — Virus scan for file uploads

To prevent infected files from being stored in the application, uploaded files should be scanned after they are saved to a temporary upload directory and before they are moved into permanent storage.

The project currently integrates with ClamAV (http://www.clamav.net). Ensure a ClamAV daemon (clamd) is reachable from the application host.

### Configuration

The configuration is done via the following three system properties (DB):

 ```
  *---------+---------+---------+
  || Property || Value || Description ||
  *---------+---------+---------+
  | virusScannerDisabled | FALSE | Set to TRUE if the virus scanner shall be disabled to scan uploads (via browser). |
  *---------+---------+---------+
 ```

The given values in the table above are used as default values if none are configured (they are for an installed clamav scanner on a linux box).

### Where is the scan done in the code

The scan happens in the org.bfabric.beans.FileUpload.java bean in the listener method.

  ```
  public void listener(FileUploadEvent event) {
          BfabricUploadedFile uploadedFile = new BfabricUploadedFile(event.getFile());
  
          if (getConfManager().getConfiguration().isVirusScannerDisabled()) {
              if (!fileExists(uploadedFile)) {
                  getUploadedFiles().add(uploadedFile);
              }
          } else {
              try {
                  ClamAvScanner scanner = new ClamAvScanner("localhost", 3310);
                  ScanResult result = scanner.scan(new File(getAbsolutePath(uploadedFile)));
  
                  switch (result.getStatus()) {
                  case PASSED:
                      if (!fileExists(uploadedFile)) {
                          getUploadedFiles().add(uploadedFile);
                      }
                      break;
                  case WARNING:
                      logger.severe("Error during scanning for virus.", result.getMessage());
                      break;
                  case INFECTED:
                  default:
                      String errorMsg = "Upload failed: " + uploadedFile.getFileName() + " is infected!";
                      logger.warning(errorMsg);
                      ((FacesMessagesManager) Component.getInstance("facesMessagesManager"))
                              .add(Severity.ERROR, errorMsg);
                      errorMsg = errorMsg + " to " + getAbsolutePath(uploadedFile) + "\n";
                      logger.warning(errorMsg);
                  }
              } catch (IOException ioe) {
                  logger.severe("Error during scanning for virus.", ioe);
              }
          }
      }
  ```