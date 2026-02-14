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

package org.bfabric.manager;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.SystemException;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEnabledBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.AbstractService;
import org.bfabric.service.ContainerService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.DataTableHelper;
import org.omnifaces.cdi.Param;

public abstract class AbstractEntityManager<T extends AbstractEntity> extends AbstractManager {

    private static final Logger logger = Logger.getLogger(AbstractEntityManager.class.getName());

    private static final long serialVersionUID = 1;

    @Param
    protected String clone;

    protected Long clonedId = null;

    @Inject
    protected DataTableHelper dataTableHelper;

    @Param
    protected String id;

    @Param
    protected String mergeId;

    protected Class<T> entityClass;

    @Inject
    protected ContainerService containerService;

    private AbstractEntity abstractEntity;

    @Transient
    private boolean containerAll = false;

    @Transient
    private boolean created = false;

    private Set<Container> selectedContainers = new HashSet<>();

    public AbstractEntityManager() {
    }

    public AbstractEntityManager(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void bufferMergeSuccessMessage() {
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyMerged"));
    }

    public String cancel() {
        return getRedirectURLAfterCancel();
    }

    public String cancelEdit() throws IllegalStateException, SecurityException, SystemException {
        // HINT: No messages are shown after cancelEdit is called. If there exist cases in which after cancelEdit the messages should be displayed, then another 'cancelEdit' method has to be implemented.
        getFacesMessagesManager().clearGlobalMessages();
        rollback();
        return cancel();
    }

    protected T createInstance() {
        T entity = null;
        if (getEntityClass() != null) {
            try {
                entity = getEntityClass().getDeclaredConstructor().newInstance();
                // logger.fine("Create new instance: " + entity.getTrimmedClassName());
                setAbstractEntity(entity);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    @Override
    public String createRedirectURL(String screen, Long redirectId, String tab, Map<String, String> fParams) {
        return super.createRedirectURL(screen, redirectId, tab, fParams);
    }

    public AbstractEntity getAbstractEntity() {
        return abstractEntity;
    }

    public String getAction() {
        return isCloned() ? Constants.CLONE : isManaged() ? Constants.EDIT : Constants.ADD;
    }

    public Long getClonedId() {
        return clonedId;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public String getId() {
        return id;
    }

    public Long getIdLong() {
        return getIdLong(id);
    }

    public T getInstance() {
        return (T) abstractEntity;
    }

    public T getInstance(Long entityId) {
        return entityId != null ? entityService.find(getEntityClass(), entityId) : null;
    }

    public T getInstance(String entityId) throws NumberFormatException {
        return getInstance(Long.valueOf(entityId));
    }

    public String getListScreenRedirectURL() {
        return createRedirectURL(getEntityClass().getSimpleName().toLowerCase() + "/" + Constants.LIST, null, null, null);
    }

    public String getListScreenRedirectURL(boolean condition) {
        return condition ? getListScreenRedirectURL() : getDefaultRedirectURL();
    }

    public String getManageDuplicatesScreenRedirectURL() {
        return createRedirectURL("duplicate/list");
    }

    public String getRedirectURLAfterCancel() {
        if (getRefererURL() != null) {
            return getRedirectURLFromRefererUrl();
        }
        if (isManaged()) {
            return getRedirectURLAfterCancelManaged();
        }
        return getRedirectURLAfterCancelCreated();
    }

    public String getRedirectURLAfterCancelCreated() {
        return getInstance().isClonedOrMoved() ? createRedirectShowScreenURL(getEntityClass().getSimpleName(), getClonedId(), null, null) : getListScreenRedirectURL();
    }

    public String getRedirectURLAfterCancelManaged() {
        return getShowScreenRedirectURL();
    }

    public String getRedirectURLAfterRemove() {
        return getRefererURL() != null && getRefererURL().matches(".*(tab=|/list).*") ? getRedirectURLFromRefererUrl() : getListScreenRedirectURL(getCurrentUser().hasRoleImplicit(RoleEnum.EMPLOYEE));
    }

    public String getRedirectURLAfterSave() {
        return getShowScreenRedirectURL();
    }

    public String getRedirectURLFromRefererUrl() {
        return getRedirectURLFromRefererUrl(false);
    }

    public String getRedirectURLFromRefererUrl(boolean condition) {
        final HttpServletRequest httpServletRequest = getHttpServletRequest();
        // If the referer URL is null, the same as the requested URL (without parameters), if the referer URL is an edit screen or if the condition is satisfied, redirect to the 'container' or 'home'.
        if (getRefererURL() == null || getRefererURL().contains(httpServletRequest.getRequestURL().toString()) || getRefererURL().contains(getEntityClass().getSimpleName().toLowerCase() + "/edit")
            || condition) {
            return getDefaultRedirectURL();
        }
        return createRedirectURLFromRefererURL();
    }

    public List<Container> getSelectableContainers(String filterString) {
        return containerService.getContainersFilteredExcluding(filterString, getSelectedContainers());
    }

    public Set<Container> getSelectedContainers() {
        return selectedContainers;
    }

    public List<Container> getSelectedContainersAsList() {
        return CollectionHelper.asList(getSelectedContainers());
    }

    public String getShowScreenRedirectURL() {
        return getShowScreenRedirectURL(sidebarHelper.getTab());
    }

    public String getShowScreenRedirectURL(String tab) {
        return createRedirectURL(getEntityClass().getSimpleName().toLowerCase() + "/" + Constants.SHOW, getAbstractEntity().getId(), tab, null);
    }

    public void handleValidationErrors(LinkedHashMap<String, String> validationErrorMsg) {
        getFacesMessagesManager().printValidationErrors(validationErrorMsg);
        if (getOldRefererURL() != null) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("oldRefererURL", getOldRefererURL());
        }
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        initInstance();
        if (getContextContainer() != null) {
            getSelectedContainers().add(getContextContainer());
        }
        if (getInstance() != null && isManaged() && clone != null) {
            try {
                initClone();
            } catch (final CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    public void initClone() throws CloneNotSupportedException {
        // Set the manager to clone mode.
        setCloned();
        // Check whether the entity already has a clone.
        if (!getInstance().isCloned()) {
            // Initialize the clone of the current entity.
            getInstance().initClone();
            // Switch to editing the clone.
            setInstance(getInstance().getClone());
        }
    }

    public void initInstance() {
        if (abstractEntity == null) {
            if (getId() == null && !isShowScreenUrl()) {
                abstractEntity = createInstance();
            } else {
                if (getIdLong() != null) {
                    abstractEntity = loadInstance();
                    if (abstractEntity == null && !FacesContext.getCurrentInstance().getResponseComplete()) {
                        // System.out.println("initInstance: " + getIdLong() + " --- " + getEntityClass().getSimpleName() + " currentUser=" + getCurrentUser());
                        redirectToEntityNotFoundErrorPage();
                    }
                } else {
                    redirectToEntityIdInvalidErrorPage();
                }
            }
        }
    }

    public boolean isCloned() {
        return clonedId != null && clonedId > 0L;
    }

    public boolean isContainerAll() {
        return containerAll;
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isManaged() {
        return getInstance() != null && getInstance().isManaged();
    }

    public T loadInstance() {
        if (getIdLong() != null) {
            T entity = getInstance(getIdLong());
            if (entity != null) {
                if (entity.isReadable()) {
                    return entity;
                } else {
                    // System.out.println("loadInstance: " + getIdLong() + " --- " + getEntityClass().getSimpleName() + " currentUser=" + getCurrentUser());
                    getSessionManager().redirectRelative("/error/permission-denied.html");
                }
            }
        }
        return null;
    }

    protected String merge() {
        return null;
    }

    public void mergeFailed(Exception exception) {
        getFacesMessagesManager().printError(exception.getLocalizedMessage());
        logger.severe("Merge " + getAbstractEntity() + " throws " + exception);
    }

    public String postSave(boolean showMessage, boolean enforceCreatedMessage) {
        if (showMessage) {
            getFacesMessagesManager().clearGlobalMessages();
            facesMessageAdd(isCreated() || enforceCreatedMessage);
        }
        return getRedirectURLAfterSave();
    }

    public String printFacesMessagesAndRedirect(Map<String, Set<String>> facesMessages) {
        getFacesMessagesManager().clearGlobalMessages();
        printFacesMessages(facesMessages);
        return getRedirectURLAfterSave();
    }

    public void redirectToEntityIdInvalidErrorPage() {
        redirectToEntityIdInvalidErrorPage(getEntityClass().getSimpleName(), getId());
    }

    public void redirectToEntityNotFoundErrorPage() {
        redirectToEntityNotFoundErrorPage(getEntityClass().getSimpleName(), getId());
    }

    public String rejectTerms() throws IllegalStateException, SecurityException, SystemException {
        getFacesMessagesManager().bufferWarningClear(Messages.get("rejectedTerms"));
        rollback();
        return getRefererURL() != null ? getRedirectURLFromRefererUrl() : getUrlHomeScreen();
    }

    public String remove() {
        return remove(getAbstractEntity());
    }

    public String remove(AbstractEntity entity) {
        try {
            if (entity != null) {
                final String entityName = entity.toString();
                entityService.remove(entity);
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted") + " " + entityName);
                return getRedirectURLAfterRemove();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
        return null;
    }

    public void resetCloned() {
        this.clonedId = null;
    }

    public String save() {
        return save(true, true, false);
    }

    public String save(boolean index) {
        return save(index, false, false);
    }

    public <S extends AbstractService> String save(S abstractService, boolean index, boolean showMessage, boolean enforceCreatedMessage) {
        try {
            setCreated(!isManaged());
            abstractService.save(getAbstractEntity(), index);
            return postSave(showMessage, enforceCreatedMessage);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }

    public String save(boolean index, boolean showMessage, boolean enforceCreatedMessage) {
        return save(entityService, index, showMessage, enforceCreatedMessage);
    }

    public void setAbstractEntity(AbstractEntity abstractEntity) {
        this.abstractEntity = abstractEntity;
    }

    public void setCloned() {
        setClonedId(getAbstractEntity().getId());
    }

    private void setClonedId(Long clonedId) {
        this.clonedId = clonedId;
    }

    public void setContainerAll(boolean containerAll) {
        this.containerAll = containerAll;
        dataTableHelper.clearTableIdRowsPerPageTemplate();
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIdLong(Long id) {
        setId(String.valueOf(id));
    }

    public void setInstance(AbstractEntity abstractEntity) {
        if (abstractEntity != null) {
            setIdLong(abstractEntity.getId());
            setAbstractEntity(abstractEntity);
        }
    }

    public void setInstance(Long entityId) {
        if (entityId != null && !String.valueOf(entityId).equals(getId())) {
            setIdLong(entityId);
            setAbstractEntity(loadInstance());
        }
    }

    public void setSelectedContainers(Set<Container> selectedContainers) {
        this.selectedContainers = selectedContainers;
    }

    public void setSelectedContainersAsList(List<Container> selectedContainers) {
        this.selectedContainers = (Set<Container>) CollectionHelper.asSet(selectedContainers);
    }

    public String switchEnabled() {
        if (getInstance() instanceof AbstractEnabledBaseEntity) {
            AbstractEnabledBaseEntity enabledEntity = (AbstractEnabledBaseEntity) getInstance();
            enabledEntity.switchEnabled();
            save(true, true, false);
            getFacesMessagesManager().bufferWarningClear(enabledEntity.isEnabled() ? Messages.get("enabled") : Messages.get("disabled"));
            return getShowScreenRedirectURL();
        }
        return null;
    }

    public <S extends AbstractService> String validateAndSave(S abstractService, boolean index, boolean showMessage, boolean enforceCreatedMessage) {
        String ret = null;
        LinkedHashMap<String, String> validationErrorMsg = abstractService.isValid(getAbstractEntity());
        if (validationErrorMsg.isEmpty()) {
            ret = save(abstractService, index, showMessage, enforceCreatedMessage);
        } else {
            handleValidationErrors(validationErrorMsg);
        }
        return ret;
    }

    public <S extends AbstractService> String validateAndSave(S abstractService, boolean index) {
        return validateAndSave(abstractService, index, false, false);
    }

    public <S extends AbstractService> String validateAndSave(S abstractService) {
        return validateAndSave(abstractService, true, true, false);
    }
}