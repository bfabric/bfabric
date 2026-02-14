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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Membership;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.list.TechnologyList;
import org.bfabric.service.SampleService;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

public class ContainerManager<T extends Container> extends AbstractEntityManager<T> {

    private static final Logger logger = Logger.getLogger(ContainerManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    protected ConfManager confManager;

    @Inject
    protected UserService userService;

    @Inject
    protected TechnologyList technologyList;

    private Set<String> allCustomStates = new HashSet<>();

    @Param
    private String formerMembers;

    private String modalPanelType;

    private User modalUser;

    @Inject
    private SampleService sampleService;

    private List<User> selectedDiscussedWith = null;

    public ContainerManager() {
    }

    public ContainerManager(Class<T> entityClass) {
        super(entityClass);
    }

    public void addFacesMessagesForChangedManager() {
        final List<String> facesMessages = getContainer().createFacesMessageForChangedManager();
        for (final String msg : facesMessages) {
            getFacesMessagesManager().bufferWarning(msg);
        }
    }

    protected String addMember(User user, boolean asManager) {
        if (user != null) {
            Set<Mail> mails = new HashSet<>();
            final Map<String, Set<String>> facesMessages = containerService.addMember(user, asManager, getContainer(), getCurrentUser(), true, mails);
            printFacesMessages(facesMessages);
            if (!facesMessages.get(Constants.DISPLAY_MESSAGES).isEmpty()) {
                logger.fine(CollectionHelper.print(facesMessages.get(Constants.DISPLAY_MESSAGES)));
                return getShowScreenRedirectURL("members&formerMembers=" + getContainer().isShowFormerMembers());
            }
        }
        return null;
    }

    public String addUserAsManager(User user) {
        return addMember(user, true);
    }

    public String addUserAsMember(User user) {
        return addMember(user, false);
    }

    public String changeCustomStatus() {
        if (getContainer().getSelectedCustomStatus() != null) {
            printFacesMessagesClear(containerService.changeCustomStatus(getContainer(), getContainer().getSelectedCustomStatus()));
        }
        return getShowScreenRedirectURL();
    }

    public String chargeSelectedInstrumentReservations(Set<InstrumentReservation> selectedInstrumentReservations) {
        if (!selectedInstrumentReservations.isEmpty()) {
            HashMap<String, String> params = new HashMap<>();
            String selectedInstrumentReservationsParameter = selectedInstrumentReservations.stream().map(InstrumentReservation::getId).map(String::valueOf).collect(Collectors.joining(","));
            params.put("selectedInstrumentReservations", selectedInstrumentReservationsParameter);
            return createRedirectURL("charge/edit", null, null, params);
        } else {
            getFacesMessagesManager().bufferWarning(Messages.get("noInstrumentReservationSelected"));
        }
        return null;
    }

    @Override
    public T createInstance() {
        T container = null;
        if (getCurrentUser() != null) {
            container = super.createInstance();
            container.init(getCurrentUser());
            container.setReviewRequired(getConfiguration().isReviewRequired());
            container.setShowEula(getConfiguration().isReviewRequired());
        }
        if (container != null) {
            List<Technology> technologies = technologyList.getTechnologiesEnabledIncludingTechnologies(container.getTechnologies());
            if (technologies != null && technologies.size() == 1) {
                container.addTechnology(technologies.get(0));
            }
        }
        return container;
    }

    protected void createNew(String type) {
        setModalUser(new User());
        setModalPanelType(type);
    }

    public void createNewBudgetOfficer() {
        createNew(Constants.ROLE_BUDGETOFFICER);
    }

    public void createNewContact() {
        createNew(Constants.ROLE_CONTACT);
    }

    public void createNewLeader() {
        createNew(Constants.ROLE_LEADER);
    }

    public void createNewMember() {
        createNew(Constants.ROLE_MEMBER);
    }

    public void createNewRequester() {
        createNew(Constants.ROLE_REQUESTER);
    }

    public String createReplacement(Sample sample) {
        if (sample != null) {
            Sample sampleReplacement = sampleService.createReplacement(sample);
            if (sampleReplacement != null) {
                getInstance().getSampleReplacements().add(sampleReplacement);
            }
        }
        return getShowScreenRedirectURL();
    }

    public String exclude(Sample sample) {
        if (sample != null) {
            sampleService.exclude(sample);
        }
        return userDecisionSubmitted();
    }

    public String excludeAll() {
        sampleService.excludeAll(getInstance().getSamplesUserDecisionRequired());
        return userDecisionSubmitted();
    }

    public Set<String> getAllCustomStates() {
        return allCustomStates;
    }

    @Produces
    @Named("container")
    public Container getContainer() {
        return getInstance();
    }

    public List<String> getCustomOrderStates() {
        return containerService.getCustomStatesFiltered(null, Constants.ORDER, getContainer().getSelectedStatus() != null ? getContainer().getSelectedStatus().getLabel() : null, null);
    }

    public List<String> getCustomStatesFiltered(String filterString) {
        return containerService.getCustomStatesFiltered(filterString, getContainer());
    }

    public List<User> getEmployeesFiltered(String filterString) {
        return userService.getEmployeesFiltered(filterString, getSelectedDiscussedWith());
    }

    public String getModalPanelType() {
        return modalPanelType;
    }

    public User getModalUser() {
        return modalUser;
    }

    public List<User> getSelectedDiscussedWith() {
        if (selectedDiscussedWith == null) {
            selectedDiscussedWith = new ArrayList<>(getContainer().getDiscussedWith());
        }
        return selectedDiscussedWith;
    }

    public String getSwitchButtonHint(String flag, String type) {
        String switchTo = !getContainer().isContainerProject() ? Constants.ORDER : Constants.PROJECT;
        String associates = getContainer().isContainerProject() ? Constants.ORDERS : Constants.PROJECT;
        switch (flag) {
        case Constants.SWITCH_BUTTON_SHOW_TITLE:
            return Messages.get("switchButtonShowTitle").replace("{0}", type).replace("{1}", switchTo).replace("{2}", associates);
        case Constants.SWITCH_BUTTON_HIDE_TITLE:
            return Messages.get("switchButtonHideTitle").replace("{0}", type).replace("{1}", switchTo).replace("{2}", associates);
        default:
            return Constants.EMPTY_STRING;
        }
    }

    public String getSwitchButtonLabel(String flag, String type, String count) {
        String switchTo = getContainer().isContainerProject() ? Constants.ORDER : Constants.PROJECT;
        switch (flag) {
        case Constants.SWITCH_BUTTON_SHOW:
        case Constants.SWITCH_BUTTON_HIDE:
            return Messages.get(flag).replace("{0}", switchTo).replace("{1}", type);
        case Constants.SWITCH_INCLUDED:
        case Constants.SWITCH_INCLUDE:
            return Messages.get(flag).replace("{0}", switchTo).replace("{1}", type).replace("{2}", count);
        default:
            return Constants.EMPTY_STRING;
        }
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getContainer() != null && StringHelper.isNotEmpty(formerMembers) && formerMembers.equalsIgnoreCase("true")) {
            getContainer().setShowFormerMembers(true);
        }

        if (getContainer() != null && getContainer().isManaged() && getContainer().getBillingInfo() != null) {
            getContainer().getBillingInfo().setOldVatNumber(getContainer().getBillingInfo().getVatNumber());
            getContainer().getBillingInfo().setOldReferenceNumber(getContainer().getBillingInfo().getReferenceNumber());
        }
    }

    public boolean isCustomContainerStatusConfirmDialogRendered() {
        return getContainer().getSelectedCustomStatus() != null && !getAllCustomStates().contains(getContainer().getSelectedCustomStatus());
    }

    @Override
    public T loadInstance() {
        final T container = super.loadInstance();
        if (container != null) {
            getContextManager().setContextContainer(container);
            container.setAffiliationValues();
        }
        return container;
    }

    public void prepareCustomStatusModalDialog() {
        resetCustomStatusModalDialog();
        getAllCustomStates().addAll(containerService.getCustomStates(getContainer().getClassName()));
    }

    public String printFacesMessagesClearMenuHeaderAndRedirect(Map<String, Set<String>> facesMessages) {
        // Fire events to update the container context.
        return printFacesMessagesAndRedirect(facesMessages);
    }

    public String proceed(Sample sample) {
        if (sample != null) {
            sampleService.proceed(sample);
        }
        return userDecisionSubmitted();
    }

    public String proceedAll() {
        sampleService.proceedAll(getInstance().getSamplesUserDecisionRequired());
        return userDecisionSubmitted();
    }

    public String removeMembership(Membership membership) {
        Set<Mail> mails = new HashSet<>();
        final Map<String, Set<String>> facesMessages = containerService.removeMembership(membership, true, mails, true);
        printFacesMessagesClear(facesMessages);
        if (facesMessages.get(Constants.ERROR_MESSAGES).isEmpty()) {
            getContainer().setShowFormerMembers(false);
            if (getCurrentUser().equals(membership.getUser())) {
                if (getContainer().equals(getContextContainer())) {
                    getContextManager().setContextContainer(null);
                }
                return getUrlHomeScreen();
            }
        }
        return getShowScreenRedirectURL("members");
    }

    public String replaceAll() {
        sampleService.replaceAll(getInstance().getSamplesUserDecisionRequired());
        return getShowScreenRedirectURL();
    }

    public void requestBooking() {
        String errorMsg = containerService.sendMail(getContainer(), MailTypeEnum.CONTAINER_BOOKING_REQUEST);
        if (errorMsg != null) {
            getFacesMessagesManager().bufferErrorClear(errorMsg);
        }
    }

    public void resetCustomStatusModalDialog() {
        getAllCustomStates().clear();
        getContainer().setSelectedStatus(getContainer().getStatus());
        getContainer().setSelectedCustomStatus(null);
    }

    public String rollbackStatus() {
        containerService.rollbackStatus(getContainer());
        getFacesMessagesManager().bufferWarningClear(Messages.get("statusRolledBack"));
        return getShowScreenRedirectURL();
    }

    public String saveNewMember() {
        String result = saveUserOnBehalf();
        if (result != null && result.equals(Constants.SAVED)) {
            switch (getModalPanelType()) {
            case Constants.ROLE_CONTACT:
                if (getContainer().getOldContact() == null) {
                    getContainer().setOldContact(getContainer().getContact());
                }
                getContainer().setContact(getModalUser());
                break;
            case Constants.ROLE_REQUESTER:
                if (getContainer().getOldRequester() == null) {
                    getContainer().setOldRequester(getContainer().getRequester());
                }
                getContainer().setRequester(getModalUser());
                break;
            case Constants.ROLE_BUDGETOFFICER:
                if (getContainer().getOldBudgetOfficer() == null) {
                    getContainer().setOldBudgetOfficer(getContainer().getBudgetOfficer());
                }
                getContainer().setBudgetOfficerAndBillingData(getModalUser());
                break;
            case Constants.ROLE_LEADER:
                if (getContainer().getOldLeader() == null) {
                    getContainer().setOldLeader(getContainer().getLeader());
                }
                getContainer().setLeader(getModalUser());
                break;

            case Constants.ROLE_MEMBER:
                result = addUserAsMember(getModalUser());
                break;
            default:
                break;
            }
            getFacesMessagesManager().printWarning(Messages.get("successfullyCreatedUser") + " " + getModalUser().getFullName());
        }
        return result;
    }

    private String saveUserOnBehalf() {
        try {
            printFacesMessages(containerService.saveUserOnBehalf(getModalUser()));
            return Constants.SAVED;
        } catch (Exception e) {
            getFacesMessagesManager().printError(e.getLocalizedMessage());
        }
        return null;
    }

    public void setAllCustomStates(Set<String> allCustomStates) {
        this.allCustomStates = allCustomStates;
    }

    public void setModalPanelType(String modalPanelType) {
        this.modalPanelType = modalPanelType;
    }

    public void setModalUser(User modalUser) {
        this.modalUser = modalUser;
    }

    public void setNewUser(User newUser) {
        if (newUser != null) {
            switch (getModalPanelType()) {
            case Constants.ROLE_CONTACT:
                if (getContainer().getOldContact() == null) {
                    getContainer().setOldContact(getContainer().getContact());
                }
                getContainer().setContact(newUser);
                break;
            case Constants.ROLE_REQUESTER:
                if (getContainer().getOldRequester() == null) {
                    getContainer().setOldRequester(getContainer().getRequester());
                }
                getContainer().setRequester(newUser);
                break;
            case Constants.ROLE_BUDGETOFFICER:
                if (getContainer().getOldBudgetOfficer() == null) {
                    getContainer().setOldBudgetOfficer(getContainer().getBudgetOfficer());
                }
                getContainer().setBudgetOfficerAndBillingData(newUser);
                break;
            case Constants.ROLE_LEADER:
                if (getContainer().getOldLeader() == null) {
                    getContainer().setOldLeader(getContainer().getLeader());
                }
                getContainer().setLeader(newUser);
                break;

            default:
                break;
            }
        }
    }

    public void setSelectedDiscussedWith(List<User> selectedDiscussedWith) {
        this.selectedDiscussedWith = selectedDiscussedWith;
    }

    public String switchRole(Membership membership) {
        Set<Mail> mails = new HashSet<>();
        printFacesMessagesClear(containerService.switchRole(membership, true, mails));
        return getShowScreenRedirectURL();
    }

    public String switchTracked() {
        if (getCurrentUser().hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER)) {
            getContainer().switchTracked();
            if (getContainer().getTracked()) {
                getCurrentUser().getTrackedContainers().add(getContainer());
                getFacesMessagesManager().bufferWarningClear(Messages.get("trackedContainerHint").replace("{0}", getContainer().toString()));
            } else {
                getCurrentUser().getTrackedContainers().remove(getContainer());
                getFacesMessagesManager().bufferWarningClear(Messages.get("untrackedContainerHint").replace("{0}", getContainer().toString()));
            }
            userService.save(getCurrentUser());
        }
        return getShowScreenRedirectURL(sidebarHelper.getTab());
    }

    public void synchronizeWithAD(Container container) {
        if (userService.synchronizeWithADEntireContainer(container)) {
            getFacesMessagesManager().printWarning(Messages.get("synchronizationWithADStarted"));
        } else {
            getFacesMessagesManager().printWarning(Messages.get("synchronizationWithADNotStartedContainer"));
        }
    }

    public String userDecisionSubmitted() {
        containerService.userDecisionSubmitted(getContainer());
        return getShowScreenRedirectURL();
    }
}