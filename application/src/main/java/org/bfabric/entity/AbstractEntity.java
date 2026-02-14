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

package org.bfabric.entity;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;

import net.sf.ehcache.util.FindBugsSuppressWarnings;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.CustomAttributes;
import org.bfabric.entity.api.Dashboard;
import org.bfabric.entity.api.Links;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.entity.api.Options;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMap;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.manager.SessionManager;
import org.bfabric.service.ApplicationService;
import org.bfabric.service.CommentService;
import org.bfabric.service.CustomAttributeService;
import org.bfabric.service.DatasetService;
import org.bfabric.service.EntityService;
import org.bfabric.service.IdentityService;
import org.bfabric.service.JobService;
import org.bfabric.service.LinkService;
import org.bfabric.service.OptionService;
import org.bfabric.service.RoleService;
import org.bfabric.service.UserService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.DateUtils;
import org.bfabric.util.FileHelper;
import org.bfabric.util.FileUploadHelper;
import org.bfabric.util.ImageCropperHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.util.TokenUtils;
import org.bfabric.util.UriHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadEntity;
import org.bfabric.xml.EntityXmlExporter;
import org.bfabric.xml.JAXBMarshaller;
import org.bfabric.xml.XmlHelper;
import org.bfabric.xml.entity.XMLCustomAttribute;
import org.hibernate.Session;
import org.w3c.dom.Document;

@MappedSuperclass
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractEntity implements Serializable, Comparable, Cloneable, Links, Options, CustomAttributes {

    // private static final Logger logger = Logger.getLogger(AbstractEntity.class.getName());

    private static final long serialVersionUID = 1;

    @Transient
    protected List<Dataset> associatedDatasets;

    @Transient
    protected List<CustomAttribute> customAttributes;

    @Transient
    protected String entityInfo;

    @Transient
    protected String entityInfoHtml;

    @Transient
    protected List<Link> links;

    @Transient
    protected List<Option> options;

    @Transient
    protected List<Job> jobs;

    @Transient
    protected List<Application> runnableApplications;

    @Transient
    protected List<Comment> commentsPinnedCurrentUser;

    @Transient
    protected List<Comment> lastCommentsCurrentUser;

    @Transient
    private boolean changed = false;

    @Transient
    private boolean checked = false;

    @Transient
    private String className;

    @Transient
    private AbstractEntity clone = null;

    @Transient
    private List<Comment> commentsCurrentUser;

    @Transient
    private Configuration configuration;

    @Transient
    private User currentUser;

    @Transient
    private List<String> currentUserRoleNames;

    @Transient
    private String currentUsername;

    @Transient
    private Set<CustomAttribute> customAttributesToBeRemoved = new HashSet<>();

    @Transient
    private EntityService entityService;

    @Transient
    private FileUploadHelper fileUploadHelper;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Transient
    private IdentityService identityService;

    @Transient
    private ImageCropperHelper imageCropperHelper;

    @Transient
    private boolean indexDependents = false;

    @Transient
    private Set<Link> linksToBeRemoved = new HashSet<>();

    @Transient
    private boolean logEntity = true;

    @Transient
    private List<Comment> notesCurrentUser;

    @Transient
    private long oldParentId;

    @Transient
    private Document oldStateAsXml = null;

    @Transient
    private Set<Option> optionsToBeRemoved = new HashSet<>();

    @Transient
    private AbstractEntity parent;

    @Transient
    private transient XMLRequestParameterReadEntity readRequestParameter;

    @Transient
    private List<Comment> resultsCurrentUser;

    @Transient
    private RoleService roleService;

    @Transient
    private long rowKeyId;

    @Transient
    private List<Application> runnableWebApps;

    @Transient
    private boolean sendMail;

    @Transient
    private SessionManager sessionManager;

    @Transient
    private boolean setModifiedEnabled = true;

    @Transient
    private String trimmedClassName;

    @Transient
    private String urlShowScreen;

    @Transient
    private UserService userService;

    public AbstractEntity() {
    }

    public AbstractEntity(long id) {
        this.id = id;
    }

    public static Set<String> getNonLoggedAttributes() {
        Set<String> nonLoggedAttributes = new HashSet<>();
        nonLoggedAttributes.add("opLockVersion");
        return nonLoggedAttributes;
    }

    @Override
    public void addCustomAttribute() {
        addCustomAttribute(this);
    }

    public void addEntityInfoItem(StringBuilder entityInfo, String label, Object value) {
        StringHelper.addEntityInfoItem(entityInfo, label, value);
    }

    public void addEntityInfoItems(StringBuilder entityInfo, List<CustomAttribute> customAttributes) {
        StringHelper.addEntityInfoItems(entityInfo, customAttributes);
    }

    @Override
    public void addLink() {
        addLink(this);
    }

    @Override
    public void addOption() {
        addOption(this);
    }

    public void addUserToIndexMapContent(User user, IndexMapContent content) {
        if (user != null) {
            content.add(IndexMapContentEnum.USER, user.getFullName());
            content.add(IndexMapContentEnum.USER, user.getLogin());
            content.add(IndexMapContentEnum.USERID, user.getId());
        }
    }

    public void check() {
        setChecked(true);
    }

    @Override
    @FindBugsSuppressWarnings("MC_OVERRIDABLE_METHOD_CALL_IN_CLONE")
    public AbstractEntity clone() throws CloneNotSupportedException {
        AbstractEntity aClone = (AbstractEntity) super.clone();
        aClone.setId(0L);
        aClone.links = new ArrayList<>();
        aClone.options = new ArrayList<>();
        aClone.jobs = new ArrayList<>();
        for (Link link : getLinks()) {
            aClone.addLink(link.clone(aClone));
        }
        for (Option option : getOptions()) {
            aClone.addOption(option.clone(aClone));
        }
        aClone.customAttributes = new ArrayList<>();
        for (CustomAttribute customAttribute : getCustomAttributes()) {
            if (StringHelper.isNotEmpty(customAttribute.getValue()) && StringHelper.isNotEmpty(customAttribute.getName())) {
                customAttribute.clone(aClone);
            }
        }
        return aClone;
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            // Important: use trimmed class name because of hibernate proxy issues.
            String objectClassName = ClassHelper.getTrimmedClassName(object.getClass().getName());
            if (objectClassName != null && objectClassName.equals(ClassHelper.getTrimmedClassName(getClass().getName()))) {
                AbstractEntity entity = (AbstractEntity) object;
                return Long.compare(getId(), entity.getId());
            }
            throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + object.getClass().getName());
        }
        throw new ClassCastException("Cannot compare this " + getClass().getName() + " with null");
    }

    public void createEntityLog(LogActionEnum logAction) {
        createEntityLog(logAction, LogStatusEnum.DONE, null);
    }

    public EntityLog createEntityLog(LogActionEnum logAction, LogStatusEnum logStatus) {
        return createEntityLog(logAction, logStatus, null);
    }

    public EntityLog createEntityLog(LogActionEnum logAction, LogStatusEnum logStatus, String log) {
        if (isLogEntity() && (!LogActionEnum.UPDATE.equals(logAction) || StringHelper.isNotEmpty(log))) {
            // Create entity log object and set additional attribute values as needed.
            AbstractEntity parent = getParent();
            Object container = getField(ClassHelper.getAttributeName(Container.class));
            if (container instanceof Container) {
                parent = (Container) container;
            }
            Object project = getField(ClassHelper.getAttributeName(Project.class));
            if (project instanceof Project) {
                parent = (Project) project;
            }
            Object order = getField(ClassHelper.getAttributeName(Order.class));
            if (order instanceof Order) {
                parent = (Order) order;
            }
            Object multiplexKit = getField(ClassHelper.getAttributeName(MultiplexKit.class));
            if (this instanceof MultiplexId && multiplexKit instanceof MultiplexKit) {
                parent = (MultiplexKit) multiplexKit;
            }
            Object offer = getField(ClassHelper.getAttributeName(Offer.class));
            if (this instanceof OfferedCharge && offer instanceof Offer) {
                parent = (Offer) offer;
            }

            EntityLog entityLog = new EntityLog(this, logAction, logStatus, getCurrentUsername(), log, parent);

            getEntityService().persist(entityLog);

            return entityLog;
        }
        return null;
    }

    public void createEntityLogPersist() {
        createEntityLog(LogActionEnum.CREATE, LogStatusEnum.DONE, XmlHelper.getWrappedXmlElementNew(getXmlLog()));
    }

    public void createEntityLogRemove() {
        createEntityLog(LogActionEnum.DELETE, LogStatusEnum.DONE, XmlHelper.getWrappedXmlElementOld(getXmlLog()));
    }

    public void createEntityLogUpdate() {
        String log = XmlHelper.getXmlLogDiff(getOldStateAsXml(), getXml());
        if (StringHelper.isNotEmpty(log)) {
            // Wrap the XML value by a document element named log to ensure that the log value of the UPDATE event is a well-formed XML document.
            log = XmlHelper.getWrappedXmlElementLog(log);
        }
        createEntityLog(LogActionEnum.UPDATE, LogStatusEnum.DONE, log);
    }

    public EntityLog createEntityUpdateLog(String tagName, String oldContent, String newContent) {
        if (StringHelper.isNotEmpty(tagName)) {
            String oldContentLog = XmlHelper.getWrappedXmlElementOld(oldContent, tagName);
            String newContentLog = XmlHelper.getWrappedXmlElementNew(newContent, tagName);
            String log = XmlHelper.getWrappedXmlElementLog(oldContentLog + newContentLog);
            // Create entity log object and set additional attribute values as needed.
            AbstractEntity parent = getParent();
            if (getClass().equals(Project.class) || getClass().equals(Order.class)) {
                parent = this;
            }
            if (this instanceof OfferedCharge) {
                parent = ((OfferedCharge) this).getOffer();
            }
            return new EntityLog(this, LogActionEnum.UPDATE, LogStatusEnum.DONE, getCurrentUsername(), log, parent);
        }
        return null;
    }

    public void createExecutionInvokedEntityLog(AbstractEntity parent) {
        EntityLog entityLog = new EntityLog(this, LogActionEnum.EXECUTE, LogStatusEnum.INVOKED, getCurrentUsername(), null, parent);
        getEntityService().persist(entityLog);
    }

    public void download(String targetFileName, String fileContent) {
        FileHelper.download(targetFileName, fileContent);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AbstractEntity && hashCode() == object.hashCode();
    }

    public void exportAndDownloadXML() {
        String export = getMetadataExport(DateUtils.getDateDownloadString());
        if (StringHelper.isEmpty(export)) {
            export = JAXBMarshaller.getXmlAsText(this);
        }
        download(getTableContext() + ".xml", export);
    }

    public void fixDependencies() {
    }

    private List<Field> getAllFields() {
        return FieldUtils.getAllFieldsList(getClass());
    }

    public String getAllNonEmptyAttributeValuePairsAsText() {
        return getAllNonEmptyAttributeValuePairsAsText("\n");
    }

    public String getAllNonEmptyAttributeValuePairsAsText(String delimiter) {
        TreeMap<String, String> attributeValuePairsMap = new TreeMap<>();
        attributeValuePairsMap.put("class", getTrimmedClassName());

        List<Field> fields = getAllFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String attributeName = field.getName();

                // Ignore attributes with the JPA annotations @Transient, @Version.
                boolean ignore = false;
                Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Transient || annotation instanceof Version) {
                        ignore = true;
                        break;
                    }
                }

                // Ignore static and transient attributes.
                if (!ignore && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && !getNonLoggedAttributes().contains(attributeName)) {
                    Object attributeValue = field.get(this);
                    // Add attribute value pair to map if the value is non-empty.
                    if (attributeValue != null && StringHelper.isTrimBothNotEmpty(String.valueOf(attributeValue)) && (!(field.get(this) instanceof Collection<?>) || !((Collection<?>) field.get(this)).isEmpty())) {
                        attributeValuePairsMap.put(attributeName, String.valueOf(attributeValue));
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return CollectionHelper.printBasic(attributeValuePairsMap.entrySet(), delimiter);
    }

    public Map<String, List<Application>> getApplicationsGroupedByCategory(List<Application> applications) {
        return applications.stream().collect(Collectors.groupingBy(Application::getApplicationCategoryName));
    }

    public List<Dataset> getAssociatedDatasets() {
        if (associatedDatasets == null) {
            associatedDatasets = CDI.current().select(DatasetService.class).get().getDatasetsByEntity(this);
        }
        return associatedDatasets;
    }

    public String getClassLabel() {
        return Messages.get(ClassHelper.getAttributeName(getClass()));
    }

    public String getClassLabelId() {
        return getClassLabel() + " " + getId();
    }

    public String getClassLabelLowerCase() {
        return StringHelper.toLowerCase(getClassLabel());
    }

    public String getClassLabelLowerCaseId() {
        return getClassLabelLowerCase() + "_" + getId();
    }

    public String getClassLabelUpperCase() {
        return StringHelper.toUpperCase(getClassLabel());
    }

    public String getClassName() {
        if (className == null && getTrimmedClassName() != null) {
            className = getTrimmedClassName().toLowerCase();
        }
        return className;
    }

    public String getClassNameFirstLowerCase() {
        return StringHelper.firstLower(getTrimmedClassName());
    }

    public String getClassNameLowerCase() {
        return StringHelper.toLowerCase(getClassName());
    }

    public String getClassUrlPrefix() {
        return getConfiguration().getBaseUrl() + getClassNameLowerCase() + "/";
    }

    public AbstractEntity getClone() {
        return clone;
    }

    public CommentDiscriminator getCommentDiscriminator() {
        return null;
    }

    public List<Comment> getCommentsCurrentUser() {
        if (commentsCurrentUser == null) {
            commentsCurrentUser = CDI.current().select(CommentService.class).get().getCommentsByParentAndType(this, getCommentDiscriminator(), hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER));
        }
        return commentsCurrentUser;
    }

    public List<Comment> getCommentsPinned(boolean isCommentManager, boolean isOrderCommentsShown) {
        if (commentsPinnedCurrentUser == null) {
            return commentsPinnedCurrentUser = CDI.current().select(CommentService.class).get().getCommentsPinnedByParentAndType(this, getCommentDiscriminator(), isCommentManager);
        }
        return commentsPinnedCurrentUser;
    }

    public List<Comment> getCommentsPinnedCurrentUser() {
        if (commentsPinnedCurrentUser == null) {
            return commentsPinnedCurrentUser = CDI.current().select(CommentService.class).get()
                .getCommentsPinnedByParentAndType(this, getCommentDiscriminator(), hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER));
        }
        return commentsPinnedCurrentUser;
    }

    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = ConfigurationHelper.getConfiguration();
        }
        return configuration;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = getIdentityService().getUserByLogin(getCurrentUsername());
        }
        return currentUser;
    }

    public List<String> getCurrentUserRoleNames() {
        if (currentUserRoleNames == null && getIdentityService() != null) {
            currentUserRoleNames = getIdentityService().getRoleNamesByUsername(getCurrentUsername());
        }
        return currentUserRoleNames;
    }

    public String getCurrentUsername() {
        if (currentUsername == null && getIdentityService() != null) {
            currentUsername = getIdentityService().getCurrentUsername();
        }
        return currentUsername;
    }

    public List<CustomAttribute> getCustomAttributes() {
        if (customAttributes == null) {
            customAttributes = CDI.current().select(CustomAttributeService.class).get().getCustomAttributesByParent(this);
        }
        return customAttributes;
    }

    public Set<CustomAttribute> getCustomAttributesToBeRemoved() {
        return customAttributesToBeRemoved;
    }

    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public String getDisplayName() {
        return Long.toString(getId());
    }

    public String getEntityInfo() {
        if (entityInfo == null) {
            entityInfo = toString();
        }
        return entityInfo;
    }

    public String getEntityInfoHtml() {
        if (entityInfoHtml == null) {
            entityInfoHtml = StringHelper.textToHtml(getEntityInfo());
        }
        return entityInfoHtml;
    }

    protected EntityService getEntityService() {
        if (entityService == null) {
            entityService = CDI.current().select(EntityService.class).get();
        }
        return entityService;
    }

    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(toString());
        if (getParent() != null && !(this instanceof AbstractContainerDependentEntity)) {
            addEntityInfoItem(summary, "parent", getParent());
        }
        return summary.toString();
    }

    public Object getField(String attribute) {
        Object value = null;
        try {
            if (StringHelper.isNotEmpty(attribute) && PropertyUtils.isReadable(this, attribute)) {
                value = PropertyUtils.getProperty(this, attribute);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
        return value;
    }

    public FileUploadHelper getFileUploadHelper() {
        if (fileUploadHelper == null) {
            return fileUploadHelper = CDI.current().select(FileUploadHelper.class).get();
        }
        return fileUploadHelper;
    }

    public long getId() {
        return id;
    }

    @XmlID
    @XmlElement(name = "id")
    public String getIdString() {
        return String.valueOf(id);
    }

    protected IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = CDI.current().select(IdentityService.class).get();
        }
        return identityService;
    }

    public ImageCropperHelper getImageCropperHelper() {
        if (imageCropperHelper == null) {
            return imageCropperHelper = CDI.current().select(ImageCropperHelper.class).get();
        }
        return imageCropperHelper;
    }

    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = new IndexMap();
        indexMap.put(Constants.INDEXMAP_ID, Long.toString(getId()));
        if (this instanceof Indexable) {
            indexMap.put(Constants.INDEXMAP_TYPE, ((Indexable) this).getIndexMapEnum().getType().name());
        }
        indexMap.put(Constants.INDEXMAP_CONTENT, getIndexMapContent());
        indexMap.put(Constants.INDEXMAP_GROUP, getDefaultRequiredRole());
        return indexMap;
    }

    public IndexMapContent getIndexMapContent() throws Exception {
        return new IndexMapContent();
    }

    public List<Job> getJobs() {
        if (jobs == null) {
            jobs = (List<Job>) CDI.current().select(JobService.class).get().getEntitiesByParent(this);
        }
        return jobs;
    }

    @SuppressWarnings("unused")
    public List<Comment> getLastCommentsCurrentUser(Integer maxResult) {
        if (lastCommentsCurrentUser == null) {
            return lastCommentsCurrentUser = CDI.current().select(CommentService.class).get()
                .getLastCommentsByParentAndType(this, getCommentDiscriminator(), maxResult, hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER));
        }
        return lastCommentsCurrentUser;
    }

    public List<Link> getLinks() {
        if (links == null) {
            links = (List<Link>) CDI.current().select(LinkService.class).get().getEntitiesByParent(this);
        }
        return links;
    }

    public Set<Link> getLinksToBeRemoved() {
        return linksToBeRemoved;
    }

    public String getMetadataExport(String exported) {
        return new EntityXmlExporter(this, exported).createXml();
    }

    public String getMetadataRepositoryPath() {
        return getRelativeRepositoryPath();
    }

    public CommentDiscriminator getNoteDiscriminator() {
        return null;
    }

    public List<Comment> getNotesCurrentUser() {
        if (notesCurrentUser == null) {
            return notesCurrentUser = getNoteDiscriminator() != null ? CDI.current().select(CommentService.class).get()
                .getCommentsByParentAndType(this, getNoteDiscriminator(), hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER)) : new ArrayList<>();
        }
        return notesCurrentUser;
    }

    public long getOldParentId() {
        return oldParentId;
    }

    public Document getOldStateAsXml() {
        return oldStateAsXml;
    }

    public List<Option> getOptions() {
        if (options == null) {
            options = (List<Option>) CDI.current().select(OptionService.class).get().getEntitiesByParent(this);
        }
        return options;
    }

    public Set<Option> getOptionsToBeRemoved() {
        return optionsToBeRemoved;
    }

    public String getPageTitle() {
        return getClassLabel() + " " + getDisplayName();
    }

    public AbstractEntity getParent() {
        if (parent == null && getParentId() != null && getParentClassName() != null) {
            parent = CDI.current().select(EntityService.class).get().getEntityByClassNameAndId(getParentClassName(), getParentId());
        }
        return parent;
    }

    public String getParentClassName() {
        return null;
    }

    public AbstractEntity getParentEntity() {
        return getParent();
    }

    public Long getParentId() {
        return null;
    }

    public String getParentUrlShowScreen() {
        return getParent() != null ? getParent().getUrlShowScreen() : null;
    }

    public XMLRequestParameterReadEntity getReadRequestParameter() {
        return readRequestParameter;
    }

    public String getRelativeRepositoryPath() {
        return getClass().getSimpleName().toLowerCase() + "_" + getId();
    }

    public String getReportPDFLink(String reportFileName) {
        return ".." + Constants.REPORT_TEMPLATE_FOLDER + reportFileName + ".pdf?id=" + getId();
    }

    public String getReportPDFUrl(String reportFileName) {
        return getUrl(getReportPDFLink(reportFileName));
    }

    public CommentDiscriminator getResultDiscriminator() {
        return null;
    }

    public List<Comment> getResultsCurrentUser() {
        if (resultsCurrentUser == null) {
            return resultsCurrentUser = getResultDiscriminator() != null ? CDI.current().select(CommentService.class).get()
                .getCommentsByParentAndType(this, getResultDiscriminator(), hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER)) : new ArrayList<>();
        }
        return resultsCurrentUser;
    }

    protected RoleService getRoleService() {
        if (roleService == null) {
            roleService = CDI.current().select(RoleService.class).get();
        }
        return roleService;
    }

    public long getRowKeyId() {

        return isManaged() ? getId() : rowKeyId;
    }

    public String getRowKeyIdAsString() {
        return String.valueOf(getRowKeyId());
    }

    public List<Application> getRunnableApplications() {
        if (runnableApplications == null) {
            runnableApplications = new ArrayList<>();
        }
        return runnableApplications;
    }

    public Map<String, List<Application>> getRunnableApplicationsGroupedByCategory() {
        return getApplicationsGroupedByCategory(getRunnableApplications());
    }

    public List<Application> getRunnableWebApps() {
        if (runnableWebApps == null) {
            runnableWebApps = CDI.current().select(ApplicationService.class).get().getRunnableWebAppsByEntity(this, getCurrentUser().hasRoleImplicit(RoleEnum.EMPLOYEE));
        }
        return runnableWebApps;
    }

    public Map<String, List<Application>> getRunnableWebAppsGroupedByCategory() {
        return getApplicationsGroupedByCategory(getRunnableWebApps());
    }

    protected SessionManager getSessionManager() {
        if (sessionManager == null) {
            sessionManager = CDI.current().select(SessionManager.class).get();
        }
        return sessionManager;
    }

    public String getShowScreenPathPrefix() {
        return (getTrimmedClassName() != null ? getTrimmedClassName() : getClass().getSimpleName()).toLowerCase() + "/" + Constants.SHOW;
    }

    public String getTableContext() {
        return getClass().getSimpleName() + "_" + getId();
    }

    public String getToken() {
        return getTokenIncludingParameter(null);
    }

    public String getTokenIncludingParameter(String parameter) {
        return new TokenUtils().getToken("entityId=" + getId() + ",entityClassName=" + getTrimmedClassName() + ",user=" + getCurrentUser().getLogin() + ",userWsPassword=" + getCurrentUser().getPasswordWS() + ",expiryDateTime=" + Constants.DATETIME_FORMATTER.format(LocalDateTime.now()
            .plusSeconds(getConfiguration().getWebAppTokenExpirationTime())) + (parameter != null ? "," + parameter : "") + ",environment=" + getConfiguration().getEnvironmentName() + ",caller=" + getConfiguration().getBaseUrl() + ",webServiceUser=" + getCurrentUser().hasRoleImplicit(RoleEnum.WEBSERVICEUSER));
    }

    public String getTransferLink(String reportFileName) {
        return getReportPDFLink(reportFileName) + "&transfer=true";
    }

    public String getTrimmedClassName() {
        if (trimmedClassName == null) {
            trimmedClassName = ClassHelper.getTrimmedClassName(getClass());
        }
        return trimmedClassName;
    }

    public String getUrl(String relativePath) {
        return getConfiguration().getBaseUrl() + relativePath.replace("../", "");
    }

    public String getUrlEditScreen() {
        return getUrlScreen(Constants.EDIT);
    }

    public String getUrlListScreen() {
        return getUrlScreen(Constants.LIST);
    }

    public String getUrlScreen(String screen) {
        return UriHelper.getUrlScreen(getClassName(), screen);
    }

    public String getUrlShowScreen() {
        if (urlShowScreen == null && this instanceof ShowScreen) {
            urlShowScreen = getUrlScreen(Constants.SHOW);
        }
        return urlShowScreen;
    }

    public String getUrlShowScreenLink() {
        return getUrlShowScreen() != null ? getUrlShowScreen().replace(".xhtml", ".html") : null;
    }

    public User getUserByLogin(String username) {
        return getUserService().getUserByLogin(username);
    }

    public String getUserDate(User user, String login, LocalDateTime date, boolean dateAppend) {
        StringBuilder userDate = new StringBuilder();
        if (user != null) {
            userDate.append(user.getFullNameLogin());
        } else if (StringHelper.isNotEmpty(login)) {
            userDate.append(login);
        }
        if (date != null) {
            String dateString = Constants.DATETIME_FORMATTER.format(date);
            if (dateAppend) {
                userDate.append(" ").append(dateString);
            } else {
                userDate.insert(0, dateString + " ");
            }
        }
        return userDate.toString().trim();
    }

    protected UserService getUserService() {
        if (userService == null) {
            userService = CDI.current().select(UserService.class).get();
        }
        return userService;
    }

    public Document getXml() {
        return JAXBMarshaller.getXml(this);
    }

    public String getXmlAsText() {
        return JAXBMarshaller.getXmlAsText(this);
    }

    public String getXmlLog() {
        return XmlHelper.getXmlLog(getXml());
    }

    public boolean hasCurrentUserRole(String roleName) {
        return getCurrentUserRoleNames() != null && getCurrentUserRoleNames().contains(roleName);
    }

    public boolean hasCurrentUserRoleEnum(RoleEnum roleEnum) {
        return hasCurrentUserRole(roleEnum.getName());
    }

    public boolean hasDashboard() {
        return this instanceof Dashboard;
    }

    @Override
    public int hashCode() {
        if (getId() == 0) {
            return super.hashCode();
        }
        // Important: use trimmed class name because of hibernate proxy issues.
        return ClassHelper.getTrimmedClassName(getClass().getName()).concat(Long.toString(getId())).hashCode();
    }

    public void index() {
        // logger.fine(this + "---index() " + this + " logEntity=" + " " + isLogEntity() + " indexDependents=" + isIndexDependents());
        if (isLogEntity()) {
            if (this instanceof Indexable) {
                IndexHelper.indexEntity((Indexable) this);
            }
            if (isIndexDependents()) {
                indexDependents();
            }
        }
    }

    public void index(final boolean dependents) {
        setIndexDependents(dependents);
        index();
    }

    public void indexDependents() {
    }

    public void initClone() throws CloneNotSupportedException {
        // Create and set clone.
        setClone(clone());

        // Create a symmetric relationship between 'this' and its clone.
        getClone().setClone(this);
        setClone(getClone());
    }

    public boolean isChanged() {
        return changed;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isCloneable() {
        return isCreatable();
    }

    public boolean isCloned() {
        return clone != null;
    }

    public boolean isClonedOrMoved() {
        return isCloned() || isMoved();
    }

    public boolean isCreatable() {
        return getDefaultRequiredRole() == null || hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isCreatableWS() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDER) || isCreatable();
    }

    public boolean isDeletable() {
        return isUpdatable() && getCommentsCurrentUser().isEmpty() && getNotesCurrentUser().isEmpty() && getResultsCurrentUser().isEmpty();
    }

    public boolean isDeletableWS() {
        return isDeletable();
    }

    public boolean isEditable() {
        return isManaged() ? isUpdatable() : isCreatable();
    }

    public boolean isExtensible() {
        return isManaged() && isCreatable();
    }

    public boolean isIndexDependents() {
        return indexDependents;
    }

    public boolean isLogEntity() {
        return logEntity && isLoggable();
    }

    public boolean isLoggable() {
        return !(this instanceof NotEntityLoggable);
    }

    public boolean isManaged() {
        return getId() > 0;
    }

    public boolean isMoved() {
        return getOldParentId() != 0 && (getParent() == null || getOldParentId() != getParent().getId());
    }

    public boolean isReadable() {
        return isCreatable();
    }

    public boolean isRenderedAddCommentButton() {
        return isReadable();
    }

    public boolean isRenderedAddNoteButton() {
        return isUpdatable();
    }

    public boolean isRenderedSendEmailCheckbox() {
        return false;
    }

    public boolean isRunnableApplicationsRendered() {
        return !getRunnableApplications().isEmpty() && getRunnableApplications().size() < getConfiguration().getMaxItemsOnShowDetails();
    }

    public boolean isRunnableWebAppsRendered() {
        return !getRunnableWebApps().isEmpty() && getRunnableWebApps().size() < getConfiguration().getMaxItemsOnShowDetails();
    }

    public boolean isSendMail() {
        return sendMail;
    }

    public boolean isSetModifiedEnabled() {
        return setModifiedEnabled;
    }

    public boolean isUpdatable() {
        return isReadable();
    }

    public boolean isUpdatableWS() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDER) || isUpdatable();
    }

    @PostPersist
    protected void postPersist() {
        // logger.info(this + "---AbstractEntity.postPersist-----------------logEntity=" + isLogEntity());
        // Create entity log. Important: This statement must be first!
        if (isLogEntity()) {
            createEntityLogPersist();
        }
    }

    @PostRemove
    protected void postRemove() {
        // logger.info(this + "---AbstractEntity.postRemove-----------------logEntity=" + isLogEntity());
        if (this instanceof Indexable) {
            // Remove entity and its dependents from the index.
            IndexHelper.removeEntities(((Indexable) this).getEntriesToBeRemovedFromIndex());
        }
    }

    @SuppressWarnings("EmptyMethod")
    @PostUpdate
    protected void postUpdate() {
        // logger.info(this + "---AbstractEntity.postUpdate-----------------logEntity=" + isLogEntity());
    }

    @PrePersist
    protected void prePersist() {
        // logger.info(this + "---AbstractEntity.prePersist-----------------logEntity=" + isLogEntity());
        fixDependencies();
    }

    @PreRemove
    protected void preRemove() {
        // logger.info(this + "---AbstractEntity.preRemove-----------------logEntity=" + isLogEntity());
        // Create entity log. Important: This statement must be first!
        if (isLogEntity()) {
            createEntityLogRemove();
        }
    }

    @PreUpdate
    protected void preUpdate() {
        // logger.info(this + "---AbstractEntity.preUpdate-----------------logEntity=" + isLogEntity());
        fixDependencies();
        if (isLogEntity()) {
            Session session = (Session) getEntityService().getEntityManager().getDelegate();
            session.evict(this);
            AbstractEntity oldState = getEntityService().getEntityManager().find(getClass(), getId());
            if (oldState != null) {
                if (getOldStateAsXml() == null) {
                    setOldStateAsXml(oldState.getXml());
                }
                // logger.fine(this + "---oldState.xml=" + oldState.getXmlAsText());
                session.evict(oldState);
            }
            session.update(this);
            if (oldState != null) {
                // logger.fine(this + "---this.xml=" + this.getXmlAsText());
                createEntityLogUpdate();
            }
        }
    }

    @Override
    public void removeCustomAttribute(CustomAttribute customAttribute) {
        if (customAttribute != null) {
            if (getCustomAttributes() != null) {
                getCustomAttributes().remove(customAttribute);
            }
            if (getCustomAttributesToBeRemoved() != null && customAttribute.isManaged()) {
                getCustomAttributesToBeRemoved().add(customAttribute);
            }
        }
    }

    @Override
    public void removeLink(Link link) {
        if (link != null) {
            if (getLinks() != null) {
                getLinks().remove(link);
            }
            if (getLinksToBeRemoved() != null && link.isManaged()) {
                getLinksToBeRemoved().add(link);
            }
        }
    }

    @Override
    public void removeOption(Option option) {
        if (option != null) {
            if (getOptions() != null) {
                getOptions().remove(option);
            }
            if (getOptionsToBeRemoved() != null && option.isManaged()) {
                getOptionsToBeRemoved().add(option);
            }
        }
    }

    public void resetCommentCaches() {
        commentsCurrentUser = null;
        commentsPinnedCurrentUser = null;
    }

    public void sendMailChanged() {
        setSendMail(!isSendMail());
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setClone(AbstractEntity clone) {
        this.clone = clone;
    }

    public void setCustomAttributes(List<XMLCustomAttribute> xmlCustomAttributes) {
        if (xmlCustomAttributes != null && !xmlCustomAttributes.isEmpty()) {
            boolean addable = false;
            for (XMLCustomAttribute xmlCustomAttribute : xmlCustomAttributes) {
                if (!xmlCustomAttribute.isEmpty()) {
                    addable = true;
                }
            }
            if (addable) {
                removeCustomAttributes();
                for (XMLCustomAttribute xmlCustomAttribute : xmlCustomAttributes) {
                    if (!xmlCustomAttribute.isEmpty()) {
                        new CustomAttribute(this, xmlCustomAttribute.getName(), xmlCustomAttribute.getType(), xmlCustomAttribute.getValue());
                    }
                }
            }
        }
    }

    public void setCustomAttributesToBeRemoved(Set<CustomAttribute> customAttributesToBeRemoved) {
        this.customAttributesToBeRemoved = customAttributesToBeRemoved;
    }

    protected void setId(long id) {
        this.id = id;
    }

    public void setIndexDependents(boolean indexDependents) {
        this.indexDependents = indexDependents;
    }

    public void setLinksToBeRemoved(Set<Link> linksToBeRemoved) {
        this.linksToBeRemoved = linksToBeRemoved;
    }

    public void setLogEntity(boolean logEntity) {
        this.logEntity = logEntity;
    }

    public void setOldParentId(long oldParentId) {
        this.oldParentId = oldParentId;
    }

    public void setOldStateAsXml(Document oldStateAsXml) {
        this.oldStateAsXml = oldStateAsXml;
    }

    public void setOldStateAsXml() {
        setOldStateAsXml(getXml());
    }

    public void setOptionsToBeRemoved(Set<Option> optionsToBeRemoved) {
        this.optionsToBeRemoved = optionsToBeRemoved;
    }

    public void setParent(AbstractEntity parent) {
        this.parent = parent;
        setParentIdAndClassName();
    }

    public void setParentClassName(String parentClassName) {
    }

    public void setParentId(Long parentId) {
    }

    public void setParentIdAndClassName() {
        if (parent != null) {
            setParentId(parent.getId());
            setParentClassName(parent.getTrimmedClassName());
        }
    }

    public void setReadRequestParameter(XMLRequestParameterReadEntity readRequestParameter) {
        this.readRequestParameter = readRequestParameter;
    }

    public void setRowKeyId(long rowKeyId) {
        this.rowKeyId = rowKeyId;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public void setSetModifiedEnabled(boolean setModifiedEnabled) {
        this.setModifiedEnabled = setModifiedEnabled;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getId();
    }

    public void uncheck() {
        setChecked(false);
    }

    public void validateEmail(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (StringHelper.isInvalidEmailAddress(StringHelper.format((String) value))) {
            throw new BfabricValidatorException("emailNotValidException");
        }
    }
}