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

package org.bfabric.filter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.PageSequenceResults;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.xmlgraphics.util.MimeConstants;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AccessRequest;
import org.bfabric.entity.Booking;
import org.bfabric.entity.Configuration;
import org.bfabric.service.IdentityService;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.util.Servlets;

@WebFilter(urlPatterns = { "/report-templates/*" })
public class PDFFilter implements Filter {

    public static final String PDF_FILE_SUFFIX = ".pdf";

    public static final String FO_FILE_SUFFIX = ".fo";

    private static final Logger logger = Logger.getLogger(PDFFilter.class.getName());

    private FilterConfig filterConfiguration;

    private FopFactory fopFactory;

    @Inject
    private IdentityService identityService;

    private static void mergePDFFiles(String name, File... files) throws IOException {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        for (File file : files) {
            pdfMergerUtility.addSource(file);
        }
        pdfMergerUtility.setDestinationFileName(name);
        pdfMergerUtility.mergeDocuments(null);
    }

    private static void setField(PDDocument pdfDocument, String name, String value) throws IOException {
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm pdAcroForm = docCatalog.getAcroForm();
        if (pdAcroForm != null) {
            PDField field = pdAcroForm.getField(name);
            if (field != null) {
                field.setValue(value);
            } else {
                logger.warning("Cannot find field " + name);
            }
        }
    }

    public String callSOAPTransfer(String content) {
        String soapString = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bil=\"http://ethz.ch/po/fi/billing/v2\"><soapenv:Header/><soapenv:Body><bil:BillingDocCreateReq>" + content + "</bil:BillingDocCreateReq></soapenv:Body></soapenv:Envelope>";
        String endPointString = "https://sap-wdw.ethz.ch:12443/XISOAPAdapter/MessageServlet?senderParty=&senderService=PO&receiverParty=&receiverService=&interface=BillingDocOutb&interfaceNamespace=http://ethz.ch/po/fi/billing/v2";
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            InputStream inputStream = new ByteArrayInputStream(soapString.getBytes(StandardCharsets.UTF_8));
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, inputStream);
            String soapETHAuthorization = "PO_BFABRIC" + ":" + "w1KC`,;UmXe@9$D:";
            soapMessage.getMimeHeaders().addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(soapETHAuthorization.getBytes(StandardCharsets.UTF_8)));
            URL endPointUrl = new URL(null, endPointString, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    URL target = new URL(url.toString());
                    URLConnection connection = target.openConnection();
                    connection.setConnectTimeout(5000); // 5 sec
                    connection.setReadTimeout(5000); // 5 sec
                    return connection;
                }
            });
            SOAPMessage response = soapConnection.call(soapMessage, endPointUrl);
            String soapResponse = response.getSOAPBody().getTextContent();
            return Messages.get("transferETHSuccessful") + ": " + soapResponse;
        } catch (SOAPException ste) {
            return Messages.get("transferETHFailed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void convertFO2PDF(File fopFile, File pdfFile) throws IOException {
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(pdfFile.toPath()))) {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outputStream);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source src = new StreamSource(fopFile);
            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());
            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);
            // convertFO2PDFTestPrint(fop); // Comment-out for testing purposes!
        } catch (Exception e) {
            logger.severe("Conversion from FOP to PDF failed: " + e);
        }
    }

    // Do not remove this method since it is necessary for testing purposes!
    @SuppressWarnings("unused")
    public void convertFO2PDFTestPrint(Fop fop) {
        FormattingResults fopResults = fop.getResults();
        List<PageSequenceResults> pageSequences = fopResults.getPageSequences();
        for (PageSequenceResults pageSequenceResult : pageSequences) {
            logger.fine("PageSequence " + (!String.valueOf(pageSequenceResult.getID())
                .isEmpty() ? pageSequenceResult.getID() : "<no id>") + " generated " + pageSequenceResult.getPageCount() + " pages.");
        }
        logger.fine("Generated " + fopResults.getPageCount() + " pages in total.");
    }

    @Override
    public void destroy() {
        fopFactory = null;
        filterConfiguration = null;
    }

    @Override
    public void doFilter(final ServletRequest request, ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!(response instanceof HttpServletResponse)) {
            // Abort if not http response.
            ServletException servletException = new ServletException("This filter only supports HTTP");
            logger.severe(servletException.toString());
        } else {
            BufferedHttpResponseWrapper responseWrapper = new BufferedHttpResponseWrapper((HttpServletResponse) response);
            chain.doFilter(request, responseWrapper);
            responseWrapper.flushBuffer();
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession httpSession = httpRequest.getSession();
            if (httpSession != null && identityService.getCurrentUser() != null) {
                // Create (temporary) files.
                File fopFile = File.createTempFile("fop_", FO_FILE_SUFFIX);
                fopFile.deleteOnExit();
                File pdfFile = File.createTempFile("pdf_", PDF_FILE_SUFFIX);
                pdfFile.deleteOnExit();
                File pdfFormFile = null;
                FileInputStream inputStream = null;
                try {
                    String idString = httpRequest.getParameter("id");
                    // Get pdf form (if any associated with the request).
                    pdfFormFile = getPdfFormFile(httpRequest.getRequestURI(), idString);
                    byte[] origXml = responseWrapper.getBuffer();
                    if (origXml.length != 0) {
                        FileOutputStream fileOutputStream = null;
                        try {
                            fileOutputStream = new FileOutputStream(fopFile);
                            fileOutputStream.write(origXml);
                            convertFO2PDF(fopFile, pdfFile);
                            // If a PDF form is associated with this request, then merge it into the generated PDF file.
                            if (pdfFormFile != null) {
                                String pdfFormName = getPDFFormName(httpRequest);
                                if (pdfFormName != null) {
                                    switch (pdfFormName) {
                                    case "offer":
                                    case "order-confirmation-form":
                                        mergePDFFiles(pdfFile.getAbsolutePath(), pdfFile, pdfFormFile); // append to back
                                        break;
                                    default:
                                        mergePDFFiles(pdfFile.getAbsolutePath(), pdfFormFile, pdfFile); // append to front
                                        break;
                                    }
                                }
                            }
                            inputStream = new FileInputStream(pdfFile);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            logger.severe(e.toString());
                            throw new ServletException(e);
                        } finally {
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                        }
                    } else {
                        if (pdfFormFile != null) {
                            inputStream = new FileInputStream(pdfFormFile);
                        }
                    }
                    if (inputStream != null) {
                        String transfer = httpRequest.getParameter("transfer");
                        if (transfer != null && transfer.equals("true")) {
                            Booking booking = identityService.find(Booking.class, Long.valueOf(idString));
                            if (booking != null) {
                                File file = new File(getPDFFileName(httpRequest));
                                OutputStream outputStream = Files.newOutputStream(file.toPath());
                                IOUtils.copy(inputStream, outputStream);
                                String soapTransferContent = booking.getSOAPTransferContent() + "<pdfattachment>" + StringHelper.encodeBase64(file) + "</pdfattachment>";
                                String soapResponse = callSOAPTransfer(soapTransferContent);
                                String targetUrl = httpRequest.getContextPath() + "/" + booking.getShowScreenUrl().replace(".xhtml", ".html");
                                targetUrl += "&soapResponse=" + Base64.getEncoder().encodeToString(soapResponse.getBytes(StandardCharsets.UTF_8));
                                ((HttpServletResponse) response).sendRedirect(targetUrl);
                            }
                        } else {
                            // Prepare response and sent it to the browser.
                            response.setContentType("application/pdf");
                            ((HttpServletResponse) response).setHeader("Content-Disposition", Servlets.formatContentDispositionHeader(getPDFFileName(httpRequest), false));
                            response.getOutputStream().write(IOUtils.toByteArray(inputStream));
                            response.getOutputStream().flush();
                            response.flushBuffer();
                        }
                    }
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    // Delete temporary files.
                    if (!fopFile.delete()) {
                        logger.warning("File '" + fopFile.getAbsolutePath() + "' could not be deleted.");
                    }
                    if (!pdfFile.delete()) {
                        logger.warning("File '" + pdfFile.getAbsolutePath() + "' could not be deleted.");
                    }
                    if (pdfFormFile != null && !pdfFormFile.delete()) {
                        logger.warning("File '" + pdfFormFile.getAbsolutePath() + "' could not be deleted.");
                    }
                }
            }
        }
    }

    private void fillPdfForm(File file, String pdfFormName, String idString) throws IOException {
        File copy = new File("copy.pdf");
        FileUtils.copyFile(file, copy);
        copy.deleteOnExit();
        PDDocument pdDocument = Loader.loadPDF(copy);
        Configuration configuration = ConfigurationHelper.getConfiguration();
        if (idString != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(configuration.getAccessRequestManagerDatePattern());
            if (pdfFormName.contains("user-access-request-fop") || pdfFormName.contains("user-access-request-extension-fop")) {
                AccessRequest accessRequest = identityService.find(AccessRequest.class, Long.valueOf(idString));
                if (accessRequest != null) {
                    String instituteExtension = configuration.getAccessRequestManagerInstituteExtension();
                    if (StringHelper.isEmpty(instituteExtension)) {
                        instituteExtension = Constants.EMPTY_STRING;
                    }
                    setField(pdDocument, "institute", configuration.getAccessRequestManagerInstitute() + instituteExtension);
                    setField(pdDocument, "nameDZB", configuration.getAccessRequestManagerName());
                    setField(pdDocument, "addressDZB", configuration.getAccessRequestManagerAddress());
                    setField(pdDocument, "emailDZB", configuration.getAccessRequestManagerEmail());
                    setField(pdDocument, "phoneDZB", configuration.getAccessRequestManagerPhone());
                    setField(pdDocument, "date", DateTimeFormatter.ofPattern(configuration.getAccessRequestManagerDatePattern()).format(LocalDate.now()));
                    setField(pdDocument, "place", configuration.getAccessRequestManagerPlace());
                    if (accessRequest.getBirthDate() != null) {
                        setField(pdDocument, "birthdateGuest", dateTimeFormatter.format(accessRequest.getBirthDate()));
                    }
                    if (pdfFormName.contains("user-access-request-fop")) {
                        if (accessRequest.isSalutationFemale()) {
                            setField(pdDocument, "genderGuestFemale", "Ja");
                        }
                        if (accessRequest.isSalutationMale()) {
                            setField(pdDocument, "genderGuestMale", "Ja");
                        }
                        setField(pdDocument, "lastNameGuest", accessRequest.getLastName());
                        setField(pdDocument, "firstNameGuest", accessRequest.getFirstName());
                        if (accessRequest.getAccessCardValidityStartDate() != null) {
                            setField(pdDocument, "cardValidityStart", dateTimeFormatter.format(accessRequest.getAccessCardValidityStartDate()));
                        }
                        if (accessRequest.getAccessCardValidityEndDate() != null) {
                            setField(pdDocument, "cardValidityEnd", dateTimeFormatter.format(accessRequest.getAccessCardValidityEndDate()));
                        }
                    } else if (pdfFormName.contains("user-access-request-extension-fop")) {
                        setField(pdDocument, "guestNumber", accessRequest.getAccessCardCode());
                        setField(pdDocument, "nameGuest", accessRequest.getLastName() + " " + accessRequest.getFirstName());
                        if (accessRequest.getAccessCardExpiryDate() != null) {
                            setField(pdDocument, "validUntil", dateTimeFormatter.format(accessRequest.getAccessCardExpiryDate()));
                        }
                    }
                }
            }
        }
        pdDocument.save(file);
        pdDocument.close();
    }

    private String getPDFFileName(HttpServletRequest request) {
        String fileName = LocalDate.now().toString();
        if (request != null) {
            fileName = getPDFFormName(request);
            String idString = request.getParameter("id");
            if (idString != null) {
                fileName += "-" + idString;
            }
        }
        return fileName + PDF_FILE_SUFFIX;
    }

    private String getPDFFormName(HttpServletRequest request) {
        return request == null ? null : FilenameUtils.getBaseName(request.getRequestURI()).replaceAll("-fop", "");
    }

    private File getPdfFormFile(String pdfFormName, String idString) {
        try {
            if (pdfFormName != null) {
                final String pdfFormPath = getPdfFormPath(FilenameUtils.getBaseName(pdfFormName));
                if (pdfFormPath != null) {
                    File file = File.createTempFile("pdf_", PDF_FILE_SUFFIX);
                    file.deleteOnExit();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    InputStream inputStream = filterConfiguration.getServletContext().getResourceAsStream(pdfFormPath);
                    IOUtils.copy(inputStream, fileOutputStream);
                    fillPdfForm(file, pdfFormName, idString);
                    inputStream.close();
                    fileOutputStream.close();
                    return file;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPdfFormPath(String pdfFormName) {
        String pdfFormPath = null;
        if (pdfFormName != null) {
            switch (pdfFormName) {
            case "user-access-request-fop":
                pdfFormPath = Constants.REPORT_TEMPLATE_FOLDER + "user-access-request-guestcard-form.pdf";
                break;
            case "user-access-request-extension-fop":
                pdfFormPath = Constants.REPORT_TEMPLATE_FOLDER + "user-access-request-extension-guestcard-form.pdf";
                break;
            case "offer-fop":
            case "order-confirmation-form-fop":
                if (ConfigurationHelper.getConfiguration().isDeployerFGCZ()) {
                    pdfFormPath = Constants.REPORT_TEMPLATE_FOLDER + "terms-and-conditions.pdf";
                }
                break;
            default:
                break;
            }
        }
        return pdfFormPath;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        filterConfiguration = filterConfig;
        fopFactory = FopFactory.newInstance(new File(filterConfig.getServletContext().getRealPath("/")).toURI());
    }
}
