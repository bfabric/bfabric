/*
 *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.Mail;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.TechnologyList;
import org.bfabric.list.UserGroupList;
import org.bfabric.list.UserList;
import org.bfabric.service.MailSendService;
import org.bfabric.util.MailHelper;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class MailBatchManager extends AbstractManager {

    public static final String SEPARATOR_LINE = "---\n";

    private static final Logger logger = Logger.getLogger(MailBatchManager.class.getName());

    private static final long serialVersionUID = 1;

    private static final String SERVICE_TYPE = "ServiceType";

    private final Map<String, Set<User>> filterLists = new HashMap<>();

    private String containerId;

    private List<Long> containerIds = new ArrayList<>();

    private boolean containerManagers = true;

    private Map<Long, Boolean> containerManagersMap = new HashMap<>();

    private Map<Long, Container> containerMap = new HashMap<>();

    private boolean containerMembers = true;

    private Map<Long, Boolean> containerMembersMap = new HashMap<>();

    private boolean detailedSelection = false;

    private Long feedbackContainerId;

    private Long feedbackTemplateId;

    private boolean forced = false;

    @Inject
    private MailSendService mailSendService;

    private String message = Constants.EMPTY_STRING;

    private Set<User> recipients = new HashSet<>();

    private Set<String> selectedRecipientFilters = new HashSet<>();

    private StringBuilder sendReport = new StringBuilder();

    private String serviceTypeId;

    private List<Long> serviceTypeIds = new ArrayList<>();

    private Map<Long, ServiceType> serviceTypeMap = new HashMap<>();

    private String subject = Constants.EMPTY_STRING;

    @Inject
    private TechnologyList technologyList;

    @Inject
    private UserGroupList userGroupList;

    @Inject
    private UserList userList;

    public void addContainerMembersManagers() {
        if (isContainerSelectionValid(getContainerId())) {
            try {
                Long id = Long.valueOf(getContainerId());

                // check whether the container has been added before
                if (!getContainerMap().containsKey(id)) {
                    // container has not been added, check whether it exists, then fetch filter lists
                    Container container = entityService.find(Container.class, id);
                    if (container != null) {
                        // container exists, get members and managers
                        long targetContainerId = container.getProject() != null ? container.getProject().getId() : id;
                        getContainerMap().put(id, container);
                        Set<User> members = new HashSet<>(userList.getMembersByContainerId(targetContainerId));
                        getFilterLists().put(RoleEnum.CONTAINERMEMBER.getName() + id, members);
                        Set<User> managers = new HashSet<>(userList.getManagersByContainerId(targetContainerId));
                        getFilterLists().put(RoleEnum.CONTAINERMANAGER.getName() + id, managers);
                    } else {
                        throw new Exception("Container " + getContainerId() + " not found.");
                    }
                }

                // add container to selected filters
                if (!getContainerIds().contains(id)) {
                    getContainerIds().add(id);
                } else {
                    getSelectedRecipientFilters().remove(RoleEnum.CONTAINERMEMBER.getName() + id);
                    getSelectedRecipientFilters().remove(RoleEnum.CONTAINERMANAGER.getName() + id);
                    getContainerMembersMap().remove(id);
                    getContainerManagersMap().remove(id);
                    removeFilter(RoleEnum.CONTAINERMEMBER.getName() + id);
                    removeFilter(RoleEnum.CONTAINERMANAGER.getName() + id);
                }

                if (isContainerMembers()) {
                    getSelectedRecipientFilters().add(RoleEnum.CONTAINERMEMBER.getName() + id);
                    getContainerMembersMap().put(id, Boolean.TRUE);
                    addFilter(RoleEnum.CONTAINERMEMBER.getName() + id);
                }
                if (isContainerManagers()) {
                    getSelectedRecipientFilters().add(RoleEnum.CONTAINERMANAGER.getName() + id);
                    getContainerManagersMap().put(id, Boolean.TRUE);
                    addFilter(RoleEnum.CONTAINERMANAGER.getName() + id);
                }
            } catch (NumberFormatException e) {
                getFacesMessagesManager().printError(Messages.get("containerIdNotValid").replace("{0}", getContainerId()));
            } catch (Exception e) {
                getFacesMessagesManager().printError(Messages.get("containerDoesNotExist").replace("{0}", getContainerId()));
            }

        }

        applyFilter(true);

        setContainerId(null);
        setContainerManagers(true);
        setContainerMembers(true);
    }

    private void addFilter(String name) {
        getSelectedRecipientFilters().add(name);
        if (isDetailedSelection()) {
            // get list of users in this group from the db if we do not have it already
            if (!getFilterLists().containsKey(name)) {
                getFilterLists().put(name, getGroup(name));
            }
            // get list of users
            Set<User> filter = getFilterLists().get(name);
            // add it to the list of recipients
            getRecipients().addAll(getFilterLists().get(name));
            // and preselect them if there is no value set, yet

            for (User user : filter) {
                user.setChecked(true);
            }
        }
    }

    public void addServiceTypeUsers() {
        if (isServiceTypeSelectionValid(getServiceTypeId())) {
            try {
                Long id = Long.valueOf(getServiceTypeId());
                // check whether the serviceType has been added before
                if (!getServiceTypeMap().containsKey(id)) {
                    // serviceType has not been added, check whether it exists, then fetch filter lists
                    ServiceType serviceType = entityService.find(ServiceType.class, id);
                    if (serviceType != null) {
                        getServiceTypeMap().put(id, serviceType);
                        Set<User> users = new HashSet<>(userList.getUsersByServiceTypeId(id));
                        getFilterLists().put(SERVICE_TYPE + id, users);
                        if (!getServiceTypeIds().contains(id)) {
                            getServiceTypeIds().add(id);
                        }
                        getSelectedRecipientFilters().add(SERVICE_TYPE + id);
                        addFilter(SERVICE_TYPE + id);
                    } else {
                        throw new Exception("ServiceType " + getServiceTypeId() + " not found.");
                    }
                }
            } catch (NumberFormatException e) {
                getFacesMessagesManager().printError(Messages.get("serviceTypeIdNotValid").replace("{0}", getServiceTypeId()));
            } catch (Exception e) {
                getFacesMessagesManager().printError(Messages.get("serviceTypeDoesNotExist").replace("{0}", getServiceTypeId()));
            }
        }

        applyFilter(true);

        setServiceTypeId(null);
    }

    public String addedContainerManagerMembers(Long pid) {
        StringBuilder result = new StringBuilder();
        if (!getContainerManagersMap().containsKey(pid)) {
            getContainerManagersMap().put(pid, Boolean.FALSE);
        }
        if (!getContainerMembersMap().containsKey(pid)) {
            getContainerMembersMap().put(pid, Boolean.FALSE);
        }
        if (getContainerManagersMap().get(pid) ^ getContainerMembersMap().get(pid)) {
            result.append("Only ");
        }
        if (getContainerManagersMap().get(pid)) {
            result.append("Managers");
        }
        if (getContainerManagersMap().get(pid) && getContainerMembersMap().get(pid)) {
            result.append(" and ");
        }
        if (getContainerMembersMap().get(pid)) {
            result.append("Members");
        }

        return result.toString();
    }

    private void applyFilter() {
        applyFilter(false);
    }

    private void applyFilter(boolean force) {
        if (isDetailedSelection() || force) {
            recipients = new HashSet<>();

            // go through the list of filters
            for (String filter : selectedRecipientFilters) {
                // if the filter is selected, get all the users in this filter
                Set<User> filterUsers;
                if (!filterLists.containsKey(filter)) {
                    // we have to get the list of users for this filter
                    if (filter.startsWith(RoleEnum.CONTAINERMANAGER.getName())) {
                        filterUsers = getGroup(filter.substring(RoleEnum.CONTAINERMANAGER.getName().length()), Integer.parseInt(filter.substring(RoleEnum.CONTAINERMANAGER.getName().length() + 1)));
                    } else if (filter.startsWith(RoleEnum.CONTAINERMEMBER.getName())) {
                        filterUsers = getGroup(filter.substring(RoleEnum.CONTAINERMEMBER.getName().length()), Integer.parseInt(filter.substring(RoleEnum.CONTAINERMEMBER.getName().length() + 1)));
                    } else if (filter.startsWith(SERVICE_TYPE)) {
                        filterUsers = getGroup(filter.substring(SERVICE_TYPE.length()), Integer.parseInt(filter.substring(SERVICE_TYPE.length() + 1)));
                    } else {
                        filterUsers = getGroup(filter);
                    }
                    filterLists.put(filter, filterUsers);
                } else {
                    // we already fetched the list once
                    filterUsers = filterLists.get(filter);
                }

                // preselect all users
                for (User user : filterUsers) {
                    user.setChecked(true);
                }

                // and add all filtered users to the recipients list
                recipients.addAll(filterUsers);
            }
        }
    }

    public String cancel() {
        return createRedirectURL(!isUserSelectionEnabled() ? "feedbacktemplate/list" : "mail/list");
    }

    private void checkRequestFeedbackMail() {
        if (getFeedbackTemplateId() != null) {
            setSubject("Feedback Request");
            String url = getConfiguration().getBaseUrl() + "feedback/submit.html?defaultFeedbackTemplateId=" + getFeedbackTemplateId();
            if (getFeedbackContainerId() != null) {
                url += "&containerId=" + getFeedbackContainerId();
            }
            String messageString = "Dear $fullname,<p>please use the following link to give us feedback: " + "<a href=\"" + url + "\">" + url + "</a></p>";
            setMessage(messageString);
        }
    }

    public void clear() {
        setRecipients(new HashSet<>());
        setSelectedRecipientFilters(new HashSet<>());
        setContainerIds(new ArrayList<>());
        setContainerMembersMap(new HashMap<>());
        setContainerManagersMap(new HashMap<>());
        setContainerId("");
        setContainerManagers(true);
        setContainerMembers(true);
        setServiceTypeIds(new ArrayList<>());
        setServiceTypeMap(new HashMap<>());
        setServiceTypeId("");
        setDetailedSelection(false);
    }

    public void deselectAll() {
        for (User user : getPotentialRecipients()) {
            user.setChecked(false);
        }
    }

    public void deselectContainer(Long pid) {
        getContainerIds().remove(pid);
        if (getContainerManagersMap().containsKey(pid)) {
            getContainerManagersMap().put(pid, Boolean.FALSE);
        }
        if (getContainerMembersMap().containsKey(pid)) {
            getContainerMembersMap().put(pid, Boolean.FALSE);
        }
        if (getSelectedRecipientFilters().contains(RoleEnum.CONTAINERMANAGER.getName() + pid)) {
            removeFilter(RoleEnum.CONTAINERMANAGER.getName() + pid);
        }
        if (getSelectedRecipientFilters().contains(RoleEnum.CONTAINERMEMBER.getName() + pid)) {
            removeFilter(RoleEnum.CONTAINERMEMBER.getName() + pid);
        }
        if (getSelectedRecipientFilters().contains(SERVICE_TYPE + pid)) {
            removeFilter(SERVICE_TYPE + pid);
        }
        applyFilter();
    }

    public void deselectServiceType(Long pid) {
        getServiceTypeIds().remove(pid);
        getServiceTypeMap().remove(pid);
        if (getSelectedRecipientFilters().contains(SERVICE_TYPE + pid)) {
            removeFilter(SERVICE_TYPE + pid);
        }
        applyFilter();
    }

    public void forcedChanged(ValueChangeEvent event) {
        setForced((Boolean) event.getNewValue());
    }

    public String getContainerId() {
        return containerId;
    }

    public List<Long> getContainerIds() {
        return containerIds;
    }

    public Map<Long, Boolean> getContainerManagersMap() {
        return containerManagersMap;
    }

    public Map<Long, Container> getContainerMap() {
        return containerMap;
    }

    public Map<Long, Boolean> getContainerMembersMap() {
        return containerMembersMap;
    }

    public Long getFeedbackContainerId() {
        return feedbackContainerId;
    }

    public Long getFeedbackTemplateId() {
        return feedbackTemplateId;
    }

    public Map<String, Set<User>> getFilterLists() {
        return filterLists;
    }

    private Set<User> getGroup(String name) {
        HashSet<User> result = new HashSet<>();
        if (name.equals(Messages.get("runningProjectMembers"))) {
            result.addAll(userList.getUsersByRunningProjects());
        } else if (name.equals(Messages.get("userAll"))) {
            result.addAll(userList.getAllUsers());
        } else if (name.equals(Messages.get("budgetOfficers"))) {
            result.addAll(userList.getAllBudgetOfficers());
        } else if (name.equals(Messages.get("projectLeaders"))) {
            result.addAll(userList.getAllProjectLeaders());
        } else if (name.equals(Messages.get("orderRequesters"))) {
            result.addAll(userList.getAllOrderRequesters());
        } else if (name.equals(Messages.get("userRole"))) {
            result.addAll(userList.getUsersByRoleEnum(RoleEnum.USER));
        } else if (name.equals(Messages.get("employees"))) {
            result.addAll(userList.getUsersByRoleEnum(RoleEnum.EMPLOYEE));
        } else if (name.equals(Messages.get("alumni"))) {
            result.addAll(userList.getUsersByRoleEnum(RoleEnum.ALUMNI));
        } else if (name.equals(Messages.get("steeringCommittee"))) {
            result.addAll(userList.getUsersByRoleEnum(RoleEnum.STEERINGCOMMITTEE));
        } else {
            result.addAll(userList.getAllUsersByTechnology(name));
        }

        return result;
    }

    private Set<User> getGroup(String name, long id) {
        HashSet<User> result = new HashSet<>();
        if (name.equals(RoleEnum.CONTAINERMEMBER.getName())) {
            result.addAll(userList.getMembersByContainerId(id));
        } else if (name.equals(RoleEnum.CONTAINERMANAGER.getName())) {
            result.addAll(userList.getManagersByContainerId(id));
        } else if (name.equals(SERVICE_TYPE)) {
            result.addAll(userList.getUsersByServiceTypeId(id));
        }
        return result;
    }

    public String getMessage() {
        return message;
    }

    public List<User> getPotentialRecipients() {
        List<User> ret = new ArrayList<>();
        for (User user : getRecipients()) {
            if (isForced() || user.isEmailActive() && user.isMassMailEnabled()) {
                ret.add(user);
            }
        }
        return ret;
    }

    public List<User> getRecipients() {
        return new ArrayList<>(recipients);
    }

    public List<String> getRoleGroups() {
        return Arrays.asList(Messages.get("runningProjectMembers"), Messages.get("budgetOfficers"), Messages.get("projectLeaders"), Messages.get("orderRequesters"), Messages.get("userRole"), Messages.get("userAll"),
            Messages.get("employees"), Messages.get("alumni"), Messages.get("steeringCommittee"));
    }

    public Set<String> getSelectedRecipientFilters() {
        return selectedRecipientFilters;
    }

    public int getSelectedSize() {
        int selectedSize = 0;
        for (User user : getPotentialRecipients()) {
            if (user.isChecked()) {
                selectedSize++;
            }
        }
        return selectedSize;
    }

    public StringBuilder getSendReport() {
        return sendReport;
    }

    public String getSendReportSafeHtml() {
        return StringHelper.getSafeHtml(getSendReport().toString());
    }

    public String getServiceTypeId() {
        return serviceTypeId;
    }

    public List<Long> getServiceTypeIds() {
        return serviceTypeIds;
    }

    public Map<Long, ServiceType> getServiceTypeMap() {
        return serviceTypeMap;
    }

    public String getSubject() {
        return subject;
    }

    public List<UserGroup> getUserGroups() {
        return userGroupList.getAllEnabledInternalOrderByName();
    }

    public void hideDetails() {
        setDetailedSelection(false);
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        setFeedbackMail();
        checkRequestFeedbackMail();
    }

    public boolean isAnyPotentialRecipientsChecked() {
        for (User user : getPotentialRecipients()) {
            if (user.isChecked()) {
                return true;
            }
        }
        return false;
    }

    public boolean isContainerManagers() {
        return containerManagers;
    }

    public boolean isContainerMembers() {
        return containerMembers;
    }

    private boolean isContainerSelectionValid(String id) {
        if (id == null) {
            getFacesMessagesManager().validationError("send-mail:containers", Messages.get("required"));
            return false;
        }
        if (!isContainerManagers() && !isContainerMembers()) {
            getFacesMessagesManager().validationError("send-mail:containers", Messages.get("selectAtLeastOneOption"));
            return false;
        }
        return true;
    }

    public boolean isDetailedSelection() {
        return detailedSelection;
    }

    public boolean isForced() {
        return forced;
    }

    private boolean isServiceTypeSelectionValid(String id) {
        if (id == null) {
            getFacesMessagesManager().validationError("send-mail:serviceTypes", Messages.get("required"));
            return false;
        }
        return true;
    }

    public boolean isUserSelectionEnabled() {
        return getFeedbackContainerId() == null;
    }

    private void removeFilter(String name) {
        getSelectedRecipientFilters().remove(name);
        // We have to create a copy of the list, so we can remove users
        Collection<User> tmp = getFilterLists().get(name);
        if (tmp != null && !tmp.isEmpty()) {
            Set<User> toBeRemoved = new HashSet<>(tmp);
            // now remove all users from this list that are also in other selected lists
            for (Entry<String, Set<User>> filter : getFilterLists().entrySet()) {
                String filterName = filter.getKey();
                // check whether this filter is selected
                if (getSelectedRecipientFilters().contains(filterName)) {
                    // filter is selected, check whether filter contains this user
                    toBeRemoved.removeAll(filter.getValue());
                }
            }

            // finish by removing the users left over from the recipients list
            getRecipients().removeAll(toBeRemoved);
            // and removing them from the selection map
            for (User user : toBeRemoved) {
                user.setChecked(false);
            }
        }
    }

    public void returnToForm() {
        setSendReport(new StringBuilder());
    }

    public void roleGroupsChanged(ValueChangeEvent event) {
        for (String roleGroup : getRoleGroups()) {
            removeFilter(roleGroup);
        }
        if (event.getNewValue() != null) {
            for (String roleGroup : (String[]) event.getNewValue()) {
                addFilter(roleGroup);
            }
        }
        applyFilter(true);
    }

    public void selectAll() {
        for (User user : getPotentialRecipients()) {
            user.setChecked(true);
        }
    }

    @Transactional
    public String sendBatch() {
        // Create empty send report.
        setSendReport(new StringBuilder());

        // Apply selection filter if not done so far.
        if (!isDetailedSelection()) {
            applyFilter(true);
        }

        // Get the selected recipients.
        List<User> definitiveRecipients = new ArrayList<>();
        for (User recipient : getPotentialRecipients()) {
            if (recipient.isChecked() && !definitiveRecipients.contains(recipient)) {
                definitiveRecipients.add(recipient);
            }
        }

        if (definitiveRecipients.isEmpty()) {
            getSendReport().append(Messages.get("noRecipientSelectedSelectOne"));
            getSendReport().append(getMessage());
            getFacesMessagesManager().printError(Messages.get("sendMailFailed"));
            return null;
        }

        // Sort recipients list such that the email is sent first to the users with the newest last logins.
        definitiveRecipients.sort(Comparator.comparing(User::getLastLoginDate, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());

        int count = 0;
        int failed = 0;

        // Add send report entries.
        getSendReport().append(Messages.get("sendingEmailToRecipient").replace("{0}", Integer.toString(definitiveRecipients.size()))).append(StringHelper.getEnding(definitiveRecipients.size()))
            .append("\n");
        logger.fine(getSendReport().toString());

        // Send email to all recipients.
        Mail mail = new Mail();
        mail.setParent(getCurrentUser());
        mail.setType(MailTypeEnum.CUSTOM);

        String ret = null;

        // Send email to each recipient.
        for (User recipient : definitiveRecipients) {
            // Important: Reset subject and message to the original ones such that the macros can be replaced correctly!
            mail.setMailHelper(new MailHelper());
            mail.setSubject(getSubject());
            mail.setMessage(getMessage());
            mail.setRecipient(recipient);
            mail.replaceMacros(recipient);

            // Send email but do not log the email.
            ret = mailSendService.send(mail, false);
            count++;

            // Add send report entries.
            getSendReport().append(count).append(" ").append(recipient.getFullName()).append(" ").append(recipient.getEmail());
            if (ret != null) {
                failed++;
                getSendReport().append(" -> failed!");
            }
            getSendReport().append("\n");
        }

        mail.setSubject(getSubject());
        mail.setMessage(getMessage());
        mail.setRecipients(definitiveRecipients);
        entityService.persist(mail);

        // Add send report entries.
        getSendReport().append(SEPARATOR_LINE);
        getSendReport().append("Sent emails: ").append(count - failed).append(failed > 0 ? " -> failed: " + failed : Constants.EMPTY_STRING).append("\n");
        getSendReport().append(SEPARATOR_LINE);
        getSendReport().append("Subject: ").append(mail.getSubject()).append("\n");
        getSendReport().append(SEPARATOR_LINE);
        getSendReport().append("Message: ").append(mail.getMessagePlainText()).append("\n");
        getSendReport().append(SEPARATOR_LINE);

        // ScheduledJobLog scheduledJobLog = new ScheduledJobLog(JobEnum.SendMail, LogStatusEnum.DONE, getSendReport().toString());
        // entityService.persist(scheduledJobLog);

        logger.fine("Sent emails: " + (count - failed) + (failed > 0 ? " -> failed: " + failed : Constants.EMPTY_STRING));
        return ret;
    }

    public void sendMailCheck() {
        Mail mail = new Mail();
        mail.setParent(getCurrentUser());
        mail.setType(MailTypeEnum.CUSTOM);
        mail.setMailHelper(new MailHelper());
        mail.setSubject(getSubject());
        mail.setMessage(getMessage());
        mail.setRecipient(getCurrentUser());
        mail.replaceMacros(getCurrentUser());
        // Send email but do not log the email.
        String ret = mailSendService.send(mail, false);
        if (ret != null) {
            getFacesMessagesManager().printWarn(Messages.get("sendMailFailed"));
        } else {
            getFacesMessagesManager().printWarn(Messages.get("sentMailCheckTo") + " " + getCurrentUser().getEmail());
        }
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setContainerIds(List<Long> containerIds) {
        this.containerIds = containerIds;
    }

    public void setContainerManagers(boolean containerManagers) {
        this.containerManagers = containerManagers;
    }

    public void setContainerManagersMap(Map<Long, Boolean> containerManagersMap) {
        this.containerManagersMap = containerManagersMap;
    }

    public void setContainerMap(Map<Long, Container> containerMap) {
        this.containerMap = containerMap;
    }

    public void setContainerMembers(boolean containerMembers) {
        this.containerMembers = containerMembers;
    }

    public void setContainerMembersMap(Map<Long, Boolean> containerMembersMap) {
        this.containerMembersMap = containerMembersMap;
    }

    public void setDetailedSelection(boolean detailedSelection) {
        this.detailedSelection = detailedSelection;
    }

    public void setFeedbackContainerId(Long feedbackContainerId) {
        this.feedbackContainerId = feedbackContainerId;
    }

    public void setFeedbackMail() {
        Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (map.containsKey("feedbackTemplateId")) {
            setFeedbackTemplateId(Long.valueOf(map.get("feedbackTemplateId")));
            if (getFeedbackTemplateId() > 0) {
                setDetailedSelection(true);
                if (map.containsKey("feedbackContainerId")) {
                    setFeedbackContainerId(Long.valueOf(map.get("feedbackContainerId")));
                    if (getFeedbackContainerId() > 0) {
                        setContainerId(getFeedbackContainerId().toString());
                        addContainerMembersManagers();
                    }
                }
            }
        }
    }

    public void setFeedbackTemplateId(Long feedbackTemplateId) {
        this.feedbackTemplateId = feedbackTemplateId;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecipients(Set<User> recipients) {
        this.recipients = recipients;
    }

    public void setSelectedRecipientFilters(Set<String> selectedRecipientFilters) {
        this.selectedRecipientFilters = selectedRecipientFilters;
    }

    public void setSendReport(StringBuilder sendReport) {
        this.sendReport = sendReport;
    }

    public void setServiceTypeId(String serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public void setServiceTypeIds(List<Long> serviceTypeIds) {
        this.serviceTypeIds = serviceTypeIds;
    }

    public void setServiceTypeMap(Map<Long, ServiceType> serviceTypeMap) {
        this.serviceTypeMap = serviceTypeMap;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void showDetails() {
        // set detailed selection to active
        setDetailedSelection(true);

        // update recipients list
        applyFilter();
    }

    public void technologiesChanged(ValueChangeEvent event) {
        for (Technology technology : technologyList.getResultList()) {
            removeFilter(technology.getName());
        }
        if (event.getNewValue() != null) {
            for (Object object : (Object[]) event.getNewValue()) {
                addFilter(((Technology) object).getName());
            }
        }
        applyFilter(true);
    }

    public void userGroupsChanged(ValueChangeEvent event) {
        final String USERGROUP_PREFIX = "usergroup";
        for (UserGroup userGroup : getUserGroups()) {
            removeFilter(USERGROUP_PREFIX + userGroup.getId());
        }
        if (event.getNewValue() != null) {
            for (Object object : (Object[]) event.getNewValue()) {
                UserGroup userGroup = (UserGroup) object;
                getFilterLists().put(USERGROUP_PREFIX + userGroup.getId(), userGroup.getUsers());
                addFilter(USERGROUP_PREFIX + userGroup.getId());
            }
        }
        applyFilter(true);
    }
}