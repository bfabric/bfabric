# B-Fabric Documentation

## Security related coding hints

* ./filtering-output.html Filtering Output

\<h:outputText value="#{param.name}" escape="false"/\> \<!-- DON'T DO THIS! XSS SECURITY HOLE! --\>

* ./glassfish-directory-listing.html GlassFish directory listing

Directory listing is enabled by default in GlassFish.

* ./virus-scan-for-file-upload.html VirusScan for FileUpload

To prevent uploading files (e.g. as comment attachment) the uploaded files can be processed by a virus scanner after being uploaded to the server (into a temporary directory) but before the files are
stored within the application.