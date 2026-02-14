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

package org.bfabric.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.enterprise.inject.spi.CDI;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.bfabric.Constants;
import org.bfabric.entity.Access;
import org.bfabric.entity.Attachment;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.entity.Sample;
import org.bfabric.entity.User;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.DownloadDirectoryStructureEnum;
import org.bfabric.enums.ExternalJobClientClassEnum;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.EntityService;
import org.bfabric.service.IdentityService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.DateUtils;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;

@WebServlet(urlPatterns = "/fragments/download-jnlp.html")
public class JNLPServlet extends HttpServlet {

    public static final String EQUALS = "=";

    private static final Logger logger = Logger.getLogger(JNLPServlet.class.getName());

    private static final long serialVersionUID = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        CustomHttpServletRequest customContextualHttpServletRequest = new CustomHttpServletRequest(request, response);
        try {
            customContextualHttpServletRequest.initialize();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    public static class CustomHttpServletRequest {

        private static final Logger logger = Logger.getLogger(CustomHttpServletRequest.class.getName());

        private static final String SEPARATOR = ", ";

        private final Configuration configuration;

        private final EntityService entityService;

        private final HttpServletRequest request;

        private final HttpServletResponse response;

        private String applicationTitle;

        private List<Attachment> attachments;

        // auth_bfabric the (DSA) ticket
        private String authStorage;

        private String codebase;

        private String deployer;

        private String directoryId;

        private Long entityId;

        private EntityLog entityLog;

        private ExternalJobClientClassEnum externalJobClientClassEnum;

        private String jarURL;

        private String metadataRelativePath;

        private Set<Container> parentContainers;

        private Set<Resource> resources;

        private String timestamp;

        private User user;

        public CustomHttpServletRequest(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
            entityService = CDI.current().select(EntityService.class).get();
            configuration = ConfigurationHelper.getConfiguration();
        }

        private static Set<Attachment> getAttachmentsFromComments(Collection<Comment> comments) {
            Set<Attachment> result = new HashSet<>();
            if (comments != null && !comments.isEmpty()) {
                for (Comment comment : comments) {
                    if (!comment.isInternal() && !comment.getAttachments().isEmpty()) {
                        result.addAll(comment.getAttachments());
                    }
                }
            }
            return result;
        }

        private static Set<Attachment> getAttachmentsFromContainer(Container container) {
            Set<Attachment> attachments = getAttachmentsFromComments(container.getCommentsCurrentUser());
            for (Sample sample : container.getSamples()) {
                attachments.addAll(getAttachmentsFromComments(sample.getCommentsCurrentUser()));
            }
            for (Workunit workunit : container.getWorkunits()) {
                attachments.addAll(getAttachmentsFromComments(workunit.getCommentsCurrentUser()));
            }
            for (Resource resource : container.getResources()) {
                attachments.addAll(getAttachmentsFromComments(resource.getCommentsCurrentUser()));
            }
            for (Dataset dataset : container.getDatasets()) {
                attachments.addAll(getAttachmentsFromComments(dataset.getCommentsCurrentUser()));
            }
            if (container.getWorkflows() != null) {
                for (Workflow workflow : container.getWorkflows()) {
                    for (WorkflowStep workflowStep : workflow.getWorkflowSteps()) {
                        attachments.addAll(getAttachmentsFromComments(workflowStep.getCommentsCurrentUser()));
                    }
                }
            }
            return attachments;
        }

        private static Set<Attachment> getAttachmentsFromOrder(Order order) {
            Set<Attachment> attachments = new HashSet<>(getAttachmentsFromContainer(order));
            attachments.addAll(getAttachmentsFromComments(order.getResultsCurrentUser()));
            attachments.addAll(getAttachmentsFromComments(order.getNotesCurrentUser()));
            return attachments;
        }

        private static Set<Attachment> getAttachmentsFromProject(Project project, boolean includeOrders) {
            Set<Attachment> attachments = new HashSet<>(getAttachmentsFromContainer(project));
            if (project.getDescription() != null) {
                attachments.add(project.getDescription());
            }
            if (project.getExtensionReport1() != null) {
                attachments.add(project.getExtensionReport1());
            }
            if (project.getExtensionReport2() != null) {
                attachments.add(project.getExtensionReport2());
            }
            if (project.getExtensionReport3() != null) {
                attachments.add(project.getExtensionReport3());
            }
            if (includeOrders) {
                // Get all attachments of orders belonging to this project.
                for (Order order : project.getOrders()) {
                    attachments.addAll(getAttachmentsFromOrder(order));
                }
            }
            return attachments;
        }

        private void addArgumentElement(StringBuilder stringBuilder, String argument) {
            if (stringBuilder != null && argument != null) {
                stringBuilder.append("<argument>").append(argument).append("</argument>\n");
            }
        }

        @Transactional
        public void createEntityLog() {
            entityLog = new EntityLog();
            entityLog.setEntityClassName(externalJobClientClassEnum.getClientClassName());
            entityLog.setEntityId(entityId);
            entityLog.setAction(LogActionEnum.DOWNLOAD);
            entityLog.setStatus(LogStatusEnum.INVOKED);
            entityLog.setCreatedBy(user.getLogin());
            if (parentContainers.size() == 1) {
                Container container = parentContainers.iterator().next();
                entityLog.setParentEntityClassName(container.getTrimmedClassName());
                entityLog.setParentEntityId(container.getId());
            }
            entityService.persist(entityLog);
        }

        private String generateTicket(String pathToTicketGenerator, String uid, String tokens) {
            ProcessBuilder processBuilder;
            Process process;
            String ticket = null;
            if (StringHelper.isNotEmpty(pathToTicketGenerator)) {
                try {
                    // Initializing a bash process.
                    processBuilder = new ProcessBuilder(pathToTicketGenerator, uid, tokens, String.valueOf(configuration.getDownloadManagerJNLPValidityDuration()));
                    process = processBuilder.start();
                    // Reading the result of the execution.
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    ticket = br.readLine();
                    // Exception handling in case the generator does not deliver the right content.
                    String cookieName = ticket != null ? ticket.split(EQUALS)[0] : null;
                    if (StringHelper.isEmpty(ticket) || StringHelper.isEmpty(cookieName)) {
                        System.out.println("Cannot generate ticket with PubtktGeneratorFilePath=" + pathToTicketGenerator + (StringHelper.isNotEmpty(ticket) ? ticket : Constants.EMPTY_STRING));
                    }
                    br.close();
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                }
            }
            // The DSA signature
            return ticket;
        }

        private String getJNLPContent() {
            StringBuilder jnlpContent = new StringBuilder();
            jnlpContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            jnlpContent.append("<jnlp spec=\"1.0+\" codebase=\"").append(codebase).append("\" >\n");
            jnlpContent.append("<information>\n");
            jnlpContent.append("<title>").append(applicationTitle).append("</title>\n");
            jnlpContent.append("<vendor>").append(deployer).append("</vendor>\n");
            jnlpContent.append("</information>\n");
            jnlpContent.append("<resources>\n");
            jnlpContent.append("<j2se version=\"1.7+\" />\n");
            jnlpContent.append("<jar href=\"").append(jarURL).append("\" main=\"true\" />\n");
            jnlpContent.append("</resources>\n");
            jnlpContent.append("<security><all-permissions/></security>\n");
            jnlpContent.append("<application-desc main-class=\"org.bfabric.DownloadManager\">\n");
            addArgumentElement(jnlpContent, applicationTitle);
            addArgumentElement(jnlpContent, jarURL);
            addArgumentElement(jnlpContent, externalJobClientClassEnum.getClientClassName());
            addArgumentElement(jnlpContent, directoryId);
            addArgumentElement(jnlpContent, user.getDownloadDirectoryPath());
            addArgumentElement(jnlpContent, String.valueOf(user.isDownloadDirectoryEditable()));
            addArgumentElement(jnlpContent, DownloadDirectoryStructureEnum.getDownloadDirectoryStructureEnumByCode(user.getDownloadDirectoryStructure()).getName());
            int numberOfFiles = 0;
            if (resources != null && !resources.isEmpty()) {
                numberOfFiles += resources.size();
            }
            if (attachments != null && !attachments.isEmpty()) {
                numberOfFiles += attachments.size();
            }
            if (StringHelper.isNotEmpty(metadataRelativePath)) {
                numberOfFiles += 1;
            }
            addArgumentElement(jnlpContent, String.valueOf(numberOfFiles));
            addArgumentElement(jnlpContent, authStorage);

            StringBuilder entityLogCode = new StringBuilder();
            if (entityLog != null) {
                addArgumentElement(jnlpContent, String.valueOf(entityLog.getId()));
                entityLogCode.append(entityLog.getId());
                if (entityLog.getCreated() != null) {
                    entityLogCode.append(entityLog.getCreated());
                }
                addArgumentElement(jnlpContent, DigestUtils.md5Hex(entityLogCode.toString()));
            }
            if (StringHelper.isNotEmpty(metadataRelativePath)) {
                String localExternalRepositoryBasePath = null;
                String downloadManagerExternalFullPathPrefix = null;
                for (Access access : RepositoryHelper.getLocalStorage(false).getAccesses()) {
                    if (access.isFM()) {
                        localExternalRepositoryBasePath = access.getBasePath();
                    } else if (access.isDM()) {
                        downloadManagerExternalFullPathPrefix = access.getFullPathPrefix();
                    }
                }
                File metadataFile = new File(localExternalRepositoryBasePath + metadataRelativePath);
                StringBuilder metadataArgument = new StringBuilder();
                metadataArgument.append(Constants.DOWNLOAD_METADATA_FILE_NAME).append(SEPARATOR);
                metadataArgument.append(Constants.NULL).append(SEPARATOR);
                metadataArgument.append(downloadManagerExternalFullPathPrefix).append(metadataRelativePath).append(SEPARATOR);
                metadataArgument.append(Constants.DOWNLOAD_METADATA_FILE_NAME).append(SEPARATOR);
                try {
                    String fileChecksum;
                    FileInputStream fis = new FileInputStream(metadataFile);
                    fileChecksum = DigestUtils.md5Hex(fis);
                    fis.close();
                    metadataArgument.append(fileChecksum);
                } catch (IOException e) {
                    metadataArgument.append(Constants.NULL);
                }
                addArgumentElement(jnlpContent, metadataArgument.toString());
            }
            if (attachments != null) {
                for (Attachment attachment : attachments) {
                    StringBuilder attachmentArgument = new StringBuilder();
                    attachmentArgument.append("attachment_").append(attachment.getId()).append(SEPARATOR);
                    if (attachment.getComment() != null && attachment.getComment().getParent() != null) {
                        attachmentArgument.append(attachment.getComment().getParent().toString().toLowerCase().replaceAll(" ", "_")).append(SEPARATOR);
                    } else {
                        // No parent id
                        attachmentArgument.append(Constants.NULL).append(SEPARATOR);
                    }
                    String localPathFromStorageFullPathPrefix = null;
                    for (Access access : RepositoryHelper.getLocalStorage(attachment.getComment() != null && attachment.getComment().isInternal()).getAccesses()) {
                        if (access.isDM()) {
                            localPathFromStorageFullPathPrefix = access.getFullPathPrefix();
                            break;
                        }
                    }
                    attachmentArgument.append(localPathFromStorageFullPathPrefix).append(attachment.getRelativePath()).append(SEPARATOR);
                    attachmentArgument.append(StringHelper.isEmpty(attachment.getRelativePath()) ? Constants.NULL : attachment.getRelativePath()).append(SEPARATOR);
                    attachmentArgument.append(StringHelper.isEmpty(attachment.getFileChecksum()) ? Constants.NULL : attachment.getFileChecksum());
                    addArgumentElement(jnlpContent, attachmentArgument.toString());
                }
            }
            if (resources != null) {
                for (Resource resource : resources) {
                    String resourceArgument = ClassHelper.getAttributeName(Resource.class) + "_" + resource.getId() + SEPARATOR +
                        ClassHelper.getAttributeName(Workunit.class) + "_" + resource.getWorkunit().getId() + SEPARATOR +
                        (resource.getUriDownloadManager() != null ? resource.getUriDownloadManager() : Constants.HTTP) + SEPARATOR +
                        (StringHelper.isEmpty(resource.getRelativePath()) ? Constants.NULL : resource.getRelativePath()) + SEPARATOR +
                        (StringHelper.isEmpty(resource.getFileChecksum()) ? Constants.NULL : resource.getFileChecksum());
                    addArgumentElement(jnlpContent, resourceArgument);
                }
            }
            jnlpContent.append("</application-desc>\n");
            jnlpContent.append("</jnlp>");

            // Returning JNLP file content
            return jnlpContent.toString();
        }

        public void initialize() {
            // Force creation of a session
            if (request.getSession(true) == null) {
                throw new RuntimeException("Could not create a session");
            }
            try {
                process();
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }

        private void process() throws IOException {
            user = CDI.current().select(IdentityService.class).get().getCurrentUser();
            if (user != null) {
                setupJNLPFields();

                if (!user.hasRoleImplicit(RoleEnum.CONTAINERREADER)) {
                    parentContainers.removeIf(container -> !container.isPublished() && !container.isMember(user));
                }

                Set<Long> parentContainerIds = new TreeSet<>();
                for (Container container : parentContainers) {
                    parentContainerIds.add(container.getId());
                }

                // If the resource has been imported from another container.
                for (Resource resource : resources) {
                    String[] folders = resource.getRelativePath().split("/");
                    if (folders.length > 0 && folders[0].matches("p\\d+")) {
                        parentContainerIds.add(Long.valueOf(folders[0].substring(1)));
                    }
                }

                // Initializing the required user related parameters.
                String uid = user.getLogin();
                String tokens = StringUtils.join(parentContainerIds, ",");
                if (user.hasRoleImplicit(RoleEnum.COMMENTMANAGER)) {
                    tokens = tokens.concat(",").concat(RoleEnum.COMMENTMANAGER.getName());
                }

                // Note: the current path for the developers is /usr/bin/bfabric-pubtkt-cookie
                String ticket = null;
                try {
                    ticket = generateTicket(configuration.getPubtktGeneratorFilePath(), uid, tokens);
                    if (StringHelper.isNotEmpty(ticket) && ticket.contains(EQUALS)) {
                        String cookieName = ticket.split(EQUALS)[0];
                        String cookieContent = ticket.substring(cookieName.length() + EQUALS.length());
                        authStorage = cookieName + EQUALS + URLEncoder.encode(cookieContent, "UTF-8");
                    }
                } catch (Exception e) {
                    throw new IOException("JNLP ticket generation failed! Ticket=" + ticket);
                }

                deployer = configuration.getDeployerUpperCase();
                String applicationName = configuration.getApplicationName();
                applicationTitle = applicationName + " Download Manager";

                // Initializing the file names.
                codebase = request.getRequestURL().toString().replace(request.getServletPath(), "/");
                jarURL = request.getRequestURL().toString().replace("download-jnlp.html", "downloadmanager.jar");

                String separator = "_";
                String jnlpName = applicationTitle.replace(" ", separator) + separator + directoryId + ".jnlp";

                // Setting the response header.
                response.setHeader("Content-Disposition", "inline; filename=\"" + jnlpName + "\"");
                // Setting the response content type.
                response.setContentType("application/x-java-jnlp-file");

                // HTTP 1.1.
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                // HTTP 1.0.
                response.setHeader("Pragma", "no-cache");
                // Proxies.
                response.setDateHeader("Expires", 0);

                // Writing the generated JNLP file to the response output stream.
                PrintWriter out;
                try {
                    createEntityLog();
                    out = response.getWriter();
                    out.println(getJNLPContent());
                    out.close();
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }
        }

        @Asynchronous
        @Transactional
        public void removeResourceBasket(ResourceBasket resourceBasket) {
            entityService.remove(resourceBasket);
        }

        private void setupContainerFields(boolean isContainerProject, boolean includeOrders) throws IOException {
            setupEntityClass(isContainerProject ? ExternalJobClientClassEnum.PROJECT : ExternalJobClientClassEnum.ORDER);
            Container container = (Container) entityService.find(externalJobClientClassEnum.getClientClass(), entityId);
            Set<Resource> allResources = new HashSet<>(container.getResources());
            // Include the order resources if the container is a project and includeOrders is true.
            if (container.isContainerProject() && includeOrders) {
                Project project = (Project) container;
                for (Order order : project.getOrders()) {
                    allResources.addAll(order.getResources());
                    parentContainers.add(order);
                }
            }
            for (Resource resource : allResources) {
                if (resource.isDownloadManagerDownloadButtonRendered()) {
                    resources.add(resource);
                }
            }
            parentContainers.add(container);
            attachments = new ArrayList<>(isContainerProject ? getAttachmentsFromProject((Project) container, includeOrders) : getAttachmentsFromOrder((Order) container));
            metadataRelativePath = RepositoryHelper.createMetadataDownloadFile(container, timestamp);
        }

        private void setupEntityClass(ExternalJobClientClassEnum externalJobClientClassEnum) {
            this.externalJobClientClassEnum = externalJobClientClassEnum;
            entityId = Long.valueOf(request.getParameter(externalJobClientClassEnum.getClientClassRequestParameterId()));
            directoryId = externalJobClientClassEnum.getClientClass().getSimpleName() + "_" + entityId + "_" + timestamp;
        }

        private void setupJNLPFields() throws IOException {
            resources = new HashSet<>();
            parentContainers = new HashSet<>();
            timestamp = DateUtils.getDateDownloadString();
            if (request.getParameter(ClassHelper.getRequestParameterId(Resource.class)) != null) {
                setupResourceFields();
            } else if (request.getParameter(ClassHelper.getRequestParameterId(Workunit.class)) != null) {
                setupWorkunitFields();
            } else if (request.getParameter(ClassHelper.getRequestParameterId(ResourceBasket.class)) != null) {
                setupResourceBasketFields();
            } else if (request.getParameter(ClassHelper.getRequestParameterId(Container.class)) != null) {
                setupContainerFields(Boolean.parseBoolean(request.getParameter("isContainerProject")), Boolean.parseBoolean(request.getParameter("includeOrderData")));
            }
        }

        private void setupResourceBasketFields() {
            setupEntityClass(ExternalJobClientClassEnum.RESOURCE_BASKET);
            ResourceBasket resourceBasket = entityService.find(ResourceBasket.class, entityId);
            for (Resource resource : resourceBasket.getResources()) {
                if (resource.isDownloadManagerDownloadButtonRendered()) {
                    resources.add(resource);
                    parentContainers.add(resource.getContainer());
                }
            }
            removeResourceBasket(resourceBasket);
        }

        private void setupResourceFields() {
            setupEntityClass(ExternalJobClientClassEnum.RESOURCE);
            Resource resource = entityService.find(Resource.class, entityId);
            resources.add(resource);
            parentContainers.add(resource.getContainer());
        }

        private void setupWorkunitFields() throws IOException {
            setupEntityClass(ExternalJobClientClassEnum.WORKUNIT);
            Workunit workunit = entityService.find(Workunit.class, entityId);
            for (Resource resource : workunit.getResources()) {
                if (resource.isDownloadManagerDownloadButtonRendered()) {
                    resources.add(resource);
                    parentContainers.add(resource.getContainer());
                }
            }
            parentContainers.add(workunit.getContainer());
            metadataRelativePath = RepositoryHelper.createMetadataDownloadFile(workunit, timestamp);
        }
    }
}