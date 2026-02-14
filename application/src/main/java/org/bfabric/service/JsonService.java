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

package org.bfabric.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bfabric.entity.Access;
import org.bfabric.entity.Attachment;
import org.bfabric.entity.Container;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.ExternalJobClientClassEnum;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.RepositoryHelper;
import org.primefaces.PrimeFaces;

@Named
@Stateless
public class JsonService extends AbstractService {

    private static final long serialVersionUID = 1;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Attachment> attachments = new ArrayList<>();

    private String authStorage;

    private Long entityId;

    @Inject
    private EntityService entityService;

    private ExternalJobClientClassEnum externalJobClientClassEnum;

    private Set<Container> parentContainers;

    private Set<Resource> resources;

    private User user;

    @Transactional
    public void createEntityLog() {
        EntityLog entityLog = new EntityLog(null, LogActionEnum.DOWNLOAD_HTTP, LogStatusEnum.INVOKED, user.getLogin());
        entityLog.setEntityClassName(externalJobClientClassEnum.getClientClassName());
        entityLog.setEntityId(entityId);
        if (parentContainers.size() == 1) {
            Container container = parentContainers.iterator().next();
            entityLog.setParentEntityClassName(container.getTrimmedClassName());
            entityLog.setParentEntityId(container.getId());
        }
        entityService.persist(entityLog);
    }

    private String generateTicket(String uid, String tokens) {
        ProcessBuilder processBuilder;
        Process process;
        String ticket = null;

        try {
            // Initializing a bash process.
            processBuilder = new ProcessBuilder(getConfiguration().getPubtktGeneratorFilePath(), uid, tokens);
            process = processBuilder.start();

            // Reading the result of the execution.
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            ticket = br.readLine();

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // The DSA signature
        return ticket;
    }

    private String getJSONContent() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(new Json());
    }

    public String getJson(Map<String, String> requestParameter) {
        String entity = requestParameter.get("entity");
        String[] entitySplit = entity.split(" ");
        String entityClass = entitySplit[0];
        String entityId = entitySplit[1];

        user = CDI.current().select(IdentityService.class).get().getCurrentUser();
        resources = new HashSet<>();
        parentContainers = new HashSet<>();
        try {
            if (Workunit.class.getSimpleName().equals(entityClass)) {
                setupWorkunitFields(entityId);
            } else if (Resource.class.getSimpleName().equals(entityClass)) {
                setupResourceFields(entityId);
            } else if (ResourceBasket.class.getSimpleName().equals(entityClass)) {
                setupResourceBasketFields(entityId);
            }
            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String json = getJSONContent();
        PrimeFaces.current().ajax().addCallbackParam("json", json);
        return json;
    }

    public String getJson() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> map = context.getExternalContext().getRequestParameterMap();
        return getJson(map);
    }

    private void process() throws IOException {
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

        String ticket = generateTicket(uid, tokens);
        final String EQUALS = "=";
        if (ticket != null && ticket.contains(EQUALS)) {
            String cookieName = ticket.split(EQUALS)[0];
            String cookieContent = ticket.substring(cookieName.length() + EQUALS.length());
            authStorage = cookieName + EQUALS + URLEncoder.encode(cookieContent, "UTF-8");
        }

        createEntityLog();
    }

    @Asynchronous
    @Transactional
    public void removeResourceBasket(ResourceBasket resourceBasket) {
        entityService.remove(resourceBasket);
    }

    private void setResourcesAndParentContainers(Set<Resource> selectedResources) {
        for (Resource resource : selectedResources) {
            if (resource.isDownloadButtonRendered()) {
                resources.add(resource);
                parentContainers.add(resource.getContainer());
            }
        }
    }

    private void setupEntity(ExternalJobClientClassEnum clientClassEnum, String entityIdString) {
        externalJobClientClassEnum = clientClassEnum;
        entityId = Long.valueOf(entityIdString);
    }

    private void setupResourceBasketFields(String entityIdString) {
        setupEntity(ExternalJobClientClassEnum.RESOURCE_BASKET, entityIdString);
        ResourceBasket resourceBasket = entityService.find(ResourceBasket.class, entityId);
        if (resourceBasket != null) {
            setResourcesAndParentContainers(resourceBasket.getResources());
        }
        removeResourceBasket(resourceBasket);
    }

    private void setupResourceFields(String entityIdString) {
        setupEntity(ExternalJobClientClassEnum.RESOURCE, entityIdString);
        Resource resource = entityService.find(Resource.class, entityId);
        resources.add(resource);
        parentContainers.add(resource.getContainer());
    }

    private void setupWorkunitFields(String entityIdString) {
        setupEntity(ExternalJobClientClassEnum.WORKUNIT, entityIdString);
        Workunit workunit = entityService.find(Workunit.class, entityId);
        if (workunit != null) {
            setResourcesAndParentContainers(workunit.getResources());
        }
    }

    private class Json {

        public final ArrayList<String> entities = new ArrayList<>();

        public final String authStorage;

        public Json() {
            this.authStorage = JsonService.this.authStorage;
            if (ExternalJobClientClassEnum.PROJECT.equals(externalJobClientClassEnum) || ExternalJobClientClassEnum.ORDER.equals(externalJobClientClassEnum)) {
                String downloadExternalFullPathPrefix = null;
                for (Access access : RepositoryHelper.getLocalStorage(false).getAccesses()) {
                    if (access.isDM()) {
                        downloadExternalFullPathPrefix = access.getFullPathPrefix();
                    }
                }
                this.entities.add(downloadExternalFullPathPrefix);
                for (Attachment attachment : attachments) {
                    String localPathFromStorageFullPathPrefix = null;
                    for (Access access : RepositoryHelper.getLocalStorage(attachment.getComment() != null && attachment.getComment().isInternal()).getAccesses()) {
                        if (access.isDM()) {
                            localPathFromStorageFullPathPrefix = access.getFullPathPrefix();
                            break;
                        }
                    }
                    this.entities.add(localPathFromStorageFullPathPrefix + attachment.getRelativePath());
                }
            }
            for (Resource resource : resources) {
                this.entities.add(resource.getUriDownloadManager() == null ? resource.getUriDownloadHttp() : resource.getUriDownloadManager());
            }
        }
    }
}