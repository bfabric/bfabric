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

package org.bfabric.webservice.server.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Configuration;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.forms.AbstractMF;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.service.WSService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.AbstractXMLRequestReadEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveAbstractEntity;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLAbstractEntity;

@NotThreadSafe
public abstract class AbstractWSEntityManager<Entity extends AbstractEntity, XMLEntity extends XMLAbstractEntity> {

    private static final Logger logger = Logger.getLogger(AbstractWSEntityManager.class.getName());

    protected final Set<Indexable> indexableEntities = new HashSet<>();

    private final List<String> additionalFieldsToExcludeFromValidation = new ArrayList<>();

    private final List<String> fieldsToExcludeFromValidation = new ArrayList<>();

    @Inject
    protected WSService wsService;

    protected Entity instance;

    private XMLRequestParameterSaveAbstractEntity xmlRequestSaveEntity;

    @PostConstruct
    public void addFieldsToExcludeFromValidation() {
        // Do not validate all properties of the AbstractBaseEntity class and above since the values are set after validation.
        for (Field field : AbstractBaseEntity.class.getDeclaredFields()) {
            getFieldsToExcludeFromValidation().add(field.getName());
        }
    }

    protected void addIndexableEntities(Set<? extends Indexable> entities) {
        if (entities != null && !entities.isEmpty()) {
            indexableEntities.addAll(entities);
        }
    }

    protected void addIndexableEntity(Entity entity) {
        if (entity instanceof Indexable) {
            // Important: Remove the entity from the set before adding it again to ensure that the current state is indexed.
            indexableEntities.remove((Indexable) entity);
            indexableEntities.add((Indexable) entity);
        }
    }

    protected synchronized void applyModificationForm() throws Exception {
        AbstractMF modificationForm;
        setXmlRequestSaveEntity(xmlRequestSaveEntity);

        if (xmlRequestSaveEntity.getId() == null) {
            setInstance(createInstance());

            // Apply attribute value setting.
            modificationForm = getModificationFormPersist(xmlRequestSaveEntity);
            if (modificationForm == null) {
                throw new InvalidDataException("Persist not possible. Please check your values.");
            }

            modificationForm.apply();
            isValid(getInstance());

            // Check permission of the user.
            if (getInstance() != null && !getInstance().isCreatableWS()) {
                throw new InvalidDataException("You have not the permission to perform this create.");
            }
        } else {
            performEntityCheckAndSetInstance(xmlRequestSaveEntity.getId());

            // Create attribute value setting.
            modificationForm = getModificationFormUpdate(xmlRequestSaveEntity);
            if (modificationForm == null) {
                throw new InvalidDataException("Update not possible. Please check your values.");
            }

            // Apply attribute value setting. Important: Do this after the cleanup of the removed objects!
            modificationForm.apply();
            isValid(getInstance());

            // Check permission of the user.
            if (getInstance() != null && !getInstance().isUpdatableWS()) {
                throw new InvalidDataException("You have not the permission to perform this update.");
            }
        }

        setXmlRequestSaveEntity(null);
    }

    private Entity createInstance() throws Exception {
        Entity entity = null;
        if (getEntityClass() != null) {
            try {
                entity = getEntityClass().getDeclaredConstructor().newInstance();
                setInstance(entity);
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
        return entity;
    }

    protected XMLEntity createNewXmlEntity() {
        XMLEntity xmlEntity;
        try {
            xmlEntity = getXMLEntityClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.warning(e.getMessage());
            throw new RuntimeException("Failed to create xml Entity object: " + getXMLEntityClass().getCanonicalName());
        }
        return xmlEntity;
    }

    protected XMLEntity createNewXmlEntity(Entity entity) {
        XMLEntity xmlEntity;
        try {
            xmlEntity = getXMLEntityClass().getConstructor(getEntityClass()).newInstance(entity);
        } catch (Exception e) {
            logger.warning(e.getMessage());
            throw new RuntimeException("Failed to create xml Entity " + entity + ": " + getXMLEntityClass().getCanonicalName());
        }
        return xmlEntity;
    }

    protected XMLEntity createNewXmlEntity(Long id) {
        XMLEntity xmlEntity = createNewXmlEntity();
        if (xmlEntity != null) {
            xmlEntity.setId(id);
        }
        return xmlEntity;
    }

    protected XMLEntity createNewXmlEntity(Entity entity, boolean idOnly) {
        return idOnly && entity != null ? createNewXmlEntity(entity.getId()) : createNewXmlEntity(entity);
    }

    public synchronized XMLResponse delete(List<Long> idList) {
        XMLResponse xmlResponse = new XMLResponse();
        for (Long entityId : idList) {
            XMLEntity xmlEntity = createNewXmlEntity();
            try {
                performEntityCheckAndSetInstance(entityId);
                if (!getInstance().isDeletableWS()) {
                    xmlEntity.setDeletionreport(getEntityNotDeletableMessage());
                } else {
                    deleteManagedEntity();
                    xmlEntity.setDeletionreport(getReportRemovingSuccessful(entityId));
                }
            } catch (Exception e) {
                xmlEntity.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
            }

            xmlResponse.add(xmlEntity);
        }

        return xmlResponse;
    }

    public synchronized void deleteManagedEntity() {
        wsService.remove(getInstance());
    }

    public List<String> getAdditionalFieldsToExcludeFromValidation() {
        return additionalFieldsToExcludeFromValidation;
    }

    public Configuration getConfiguration() {
        return ConfigurationHelper.getConfiguration();
    }

    protected List<Entity> getEntities(XMLRequestParameterReadEntity item, int page) throws Exception {
        return (List<Entity>) wsService.getEntities(item, page, getConfiguration().getWebServiceQueryResultMaxEntitiesPerPage(), getEntityClass());
    }

    protected Class<Entity> getEntityClass() throws Exception {
        return (Class<Entity>) ClassHelper.getRuntimeClass(getClass(), 0);
    }

    private synchronized String getEntityNotDeletableMessage() {
        return getInstance() != null ? getInstance().getTrimmedClassName() + " " + getInstance().getId() + " is not deletable." : null;
    }

    private List<String> getFieldsToExcludeFromValidation() {
        return fieldsToExcludeFromValidation;
    }

    public Entity getInstance() {
        return instance;
    }

    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) throws Exception {
        return null;
    }

    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) throws Exception {
        return null;
    }

    protected Integer getNumberOfPages(XMLRequestParameterReadEntity query) throws Exception {
        return wsService.getNumberOfPages(query, getConfiguration().getWebServiceQueryResultMaxEntitiesPerPage(), getEntityClass());
    }

    private synchronized String getReportRemovingSuccessful(long entityId) {
        return getInstance() != null ? getInstance().getTrimmedClassName() + " " + entityId + " removed successfully." : null;
    }

    private Class<XMLEntity> getXMLEntityClass() {
        return (Class<XMLEntity>) ClassHelper.getRuntimeClass(getClass(), 1);
    }

    public XMLRequestParameterSaveAbstractEntity getXmlRequestSaveEntity() {
        return xmlRequestSaveEntity;
    }

    public void handleValidationErrors(LinkedHashMap<String, String> validationErrorMsg) throws InvalidDataException {
        for (Map.Entry<String, String> entry : validationErrorMsg.entrySet()) {
            if (entry.getKey() != null) {
                throw new InvalidDataException(entry.getKey().split(":")[1].toLowerCase() + ": " + entry.getValue());
            }
            throw new InvalidDataException(entry.getValue());
        }
    }

    public void indexEntities() {
        wsService.indexEntities(indexableEntities);
        indexableEntities.clear();
    }

    protected <T> void isValid(T entity) throws Exception {
        // Hint: Override this method whenever more than just bean validation needs to be applied, i.e., for every service where the isValid method is overridden, e.g., for the AnnotationService
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<T>> errors = validator.validate(entity);
        validatorFactory.close();
        throwBeanValidationErrors(errors);
    }

    protected synchronized void performEntityCheckAndSetInstance(Long entityId) throws Exception {
        setInstance((Entity) wsService.fetch(getEntityClass(), entityId));
        addIndexableEntity(getInstance());
    }

    public <Q extends XMLRequestParameterReadEntity> XMLResponse read(Q query, AbstractXMLRequestReadEntity requestRead) {
        return read(query, requestRead.getPage(), requestRead.getIdonly());
    }

    public <Q extends XMLRequestParameterReadEntity> XMLResponse read(Q query, Integer requestedPage, boolean idOnly) {
        XMLResponse xmlResponse = new XMLResponse();
        try {
            xmlResponse.setNumberofpages(getNumberOfPages(query));
            int page = requestedPage != null && requestedPage > 0 ? requestedPage : 1;
            xmlResponse.setPage(page);
            List<Entity> entitiesToRead = getEntities(query, page - 1);
            if (entitiesToRead != null) {
                xmlResponse.setEntitiesonpage(entitiesToRead.size());
                for (Entity entity : entitiesToRead) {
                    try {
                        entity.setChecked(query.fulldetails);
                        entity.setReadRequestParameter(query);
                        XMLEntity xmlEntity = createNewXmlEntity(entity, idOnly);
                        xmlResponse.add(xmlEntity);
                    } catch (Exception e) {
                        xmlResponse.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
                    }
                }
            }
        } catch (Exception e) {
            xmlResponse.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
        }
        return xmlResponse;
    }

    public synchronized void save() {
        wsService.save(getInstance(), false);
    }

    public synchronized <XMLRequestSaveEntity extends XMLRequestParameterSaveAbstractEntity> XMLResponse save(List<XMLRequestSaveEntity> xmlRequestSaveList, boolean idOnly) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterSaveAbstractEntity xmlRequestSaveBaseEntity : xmlRequestSaveList) {
            XMLEntity xmlEntity;
            try {
                setXmlRequestSaveEntity(xmlRequestSaveBaseEntity);
                applyModificationForm();
                save();
                performEntityCheckAndSetInstance(getInstance().getId());
                xmlEntity = createNewXmlEntity(getInstance(), idOnly);
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
            }
            xmlResponse.add(xmlEntity);
        }
        indexEntities();
        return xmlResponse;
    }

    public void setInstance(Entity instance) {
        this.instance = instance;
    }

    public void setXmlRequestSaveEntity(XMLRequestParameterSaveAbstractEntity xmlRequestSaveEntity) {
        this.xmlRequestSaveEntity = xmlRequestSaveEntity;
    }

    private <T> void throwBeanValidationErrors(Set<ConstraintViolation<T>> errors) throws InvalidDataException {
        for (ConstraintViolation<T> error : errors) {
            if (!getFieldsToExcludeFromValidation().contains(error.getPropertyPath().toString()) && !getAdditionalFieldsToExcludeFromValidation().contains(error.getPropertyPath().toString())) {
                throw new InvalidDataException(error.getPropertyPath() + " " + error.getMessage());
            }
        }
        getAdditionalFieldsToExcludeFromValidation().clear();
    }
}