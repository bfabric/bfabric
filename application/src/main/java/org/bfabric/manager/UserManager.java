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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Address;
import org.bfabric.entity.Container;
import org.bfabric.entity.HomeAddress;
import org.bfabric.entity.HomePhoneNumber;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.PhoneNumber;
import org.bfabric.entity.Role;
import org.bfabric.entity.Service;
import org.bfabric.entity.User;
import org.bfabric.entity.UserBillingInfo;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ContainerService;
import org.bfabric.service.RoleService;
import org.bfabric.service.ServiceService;
import org.bfabric.service.UserService;
import org.bfabric.util.DataTableHelper;
import org.bfabric.util.ImageCropperHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.UnselectEvent;

@MeasureCalls
@Named
@ViewScoped
public class UserManager extends AbstractEntityManager<User> {

    private static final Logger logger = Logger.getLogger(UserManager.class.getName());

    private static final long serialVersionUID = 1;

    private final Set<Instrument> selectedInstruments = new HashSet<>();

    private final Map<Instrument, Boolean> selectedInstrumentsMap = new HashMap<>();

    private final Set<User> selectedUsers = new HashSet<>();

    @Inject
    protected UserService userService;

    @Inject
    ImageCropperHelper imageCropperHelper;

    // Complete user details required for access request to check that all required fields are set.
    @Param
    private boolean completeRequired = false;

    private char[] confirmPassword;

    @Inject
    private ContainerService containerService;

    @Inject
    private DataTableHelper dataTableHelper;

    // Helper variables password set.
    private char[] oldPassword;

    private char[] password;

    @Inject
    private RoleService roleService;

    private List<Role> selectedRoles = null;

    @Inject
    private ServiceService serviceService;

    @Param
    private String shibbolethEmail;

    @Param
    private String shibbolethFirstName;

    @Param
    private String shibbolethGender;

    @Param
    private String shibbolethId;

    @Param
    private String shibbolethLastName;

    public UserManager() {
        super(User.class);
    }

    public void assignTrainedInstrument(Instrument instrument, boolean isSelect) {
        if (isSelect) {
            getSelectedInstruments().add(instrument);
            getSelectedInstrumentsMap().put(instrument, Boolean.TRUE);
            getUser().getTrainedInstruments().add(instrument);
        } else {
            getSelectedInstruments().remove(instrument);
            getSelectedInstrumentsMap().remove(instrument);
            getUser().getTrainedInstruments().remove(instrument);
        }
    }

    public String assignTrainedInstruments() {
        if (!getSelectedInstruments().isEmpty() && !getSelectedUsers().isEmpty()) {
            int assignments = userService.assignInstruments(getSelectedInstruments(), getSelectedUsers());
            if (assignments > 0) {
                getFacesMessagesManager().bufferWarning(Messages.get("successfullyAssignedInstruments").replace("{0}", Integer.toString(assignments)));
            } else {
                getFacesMessagesManager().bufferWarning(Messages.get("noInstrumentAssignmentSelected"));
            }
            return createRedirectURL("user/assign-trainedinstruments");
        }
        return null;
    }

    @Override
    public String cancel() {
        // Clear sensitive data.
        StringHelper.clearCharArray(password);
        StringHelper.clearCharArray(confirmPassword);
        StringHelper.clearCharArray(oldPassword);
        return super.cancel();
    }

    public void checkAllInstruments(boolean check) {
        DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
        if (dataTable != null) {
            List<Instrument> instruments = dataTableHelper.getDataTableValuesPageOnly(dataTable);
            for (Instrument instrument : instruments) {
                assignTrainedInstrument(instrument, check);
            }
        }
    }

    public void clearForm() {
        getSelectedInstruments().clear();
        getSelectedUsers().clear();
        getFacesMessagesManager().bufferWarningClear(Messages.get("formCleared"));
    }

    @Override
    public User createInstance() {
        User user = super.createInstance();
        user.setAddress(new Address());
        user.setHomeAddress(new HomeAddress());
        user.setPhoneNumber(new PhoneNumber());
        user.getPhoneNumber().setCountryCode(41);
        user.setHomePhoneNumber(new HomePhoneNumber());
        return user;
    }

    public String deleteDeletableUsers() {
        List<User> users = userService.getDeletableUsers();
        int deleted = 0;
        for (User user : users) {
            userService.remove(user);
            deleted++;
        }
        String msg = Messages.get("deletedDeletableUsers") + ": " + deleted;
        logger.info(msg);
        getFacesMessagesManager().bufferWarning(msg);
        return createRedirectURL("user/list-deletable");
    }

    public void deselectInstrument(Instrument instrument) {
        if (instrument != null) {
            getSelectedInstruments().remove(instrument);
        }
    }

    public void deselectUser(User user) {
        if (user != null) {
            getSelectedUsers().remove(user);
        }
    }

    public String employeeEntry() {
        if (getUser().getEmpDegree() != null) {
            userService.employeeEntry(getUser());
            getFacesMessagesManager().bufferWarning(Messages.get("employeeEntrySuccessful"));
            return getShowScreenRedirectURL();
        }
        getFacesMessagesManager().validationError("empDegreeGrid:employeeEntry", "empDegree required");
        return null;
    }

    public String employeeLeave() {
        userService.employeeLeave(getUser());
        getFacesMessagesManager().bufferWarning(Messages.get("employeeLeaveSuccessful"));
        return getShowScreenRedirectURL();
    }

    public List<Service> getAvailableTrackableServices(String filterString) {
        return serviceService.getAvailableTrackableServices(filterString, getUser().getTrackedServicesAsList());
    }

    public char[] getConfirmPassword() {
        return confirmPassword != null ? confirmPassword.clone() : null;
    }

    public LocalDate getDate() {
        return LocalDate.now();
    }

    @Override
    public String getListScreenRedirectURL() {
        return getUrlHomeScreen();
    }

    public Set<User> getLoggedInUsers() {
        Set<User> getLoggedInUsers = new HashSet<>();
        for (User user : getConfManager().getLoggedInUsers()) {
            getLoggedInUsers.add(entityService.find(User.class, user.getId()));
        }
        return getLoggedInUsers;
    }

    public char[] getOldPassword() {
        return oldPassword != null ? oldPassword.clone() : null;
    }

    public char[] getPassword() {
        return password != null ? password.clone() : null;
    }

    public List<Role> getRoles() {
        return getRoles(getUser());
    }

    public List<Role> getRoles(User user) {
        return new ArrayList<>(user.getRoles());
    }

    public List<Role> getRolesFiltered(String filterString) {
        return roleService.getRolesFiltered(filterString, getSelectedRoles());
    }

    public Set<Instrument> getSelectedInstruments() {
        return selectedInstruments;
    }

    public Map<Instrument, Boolean> getSelectedInstrumentsMap() {
        return selectedInstrumentsMap;
    }

    public List<Role> getSelectedRoles() {
        if (selectedRoles == null) {
            selectedRoles = new ArrayList<>();
            // Add the roles of the user.
            selectedRoles.addAll(getUser().getRoles());
            // Remove the roles that cannot be explicitly granted.
            selectedRoles.removeAll(roleService.getRolesSpecific());
        }
        return selectedRoles;
    }

    public Set<User> getSelectedUsers() {
        return selectedUsers;
    }

    public List<Container> getTrackableContainers(String filterString) {
        return containerService.getTrackableContainers(filterString, getUser().getAllTrackedContainers(), getCurrentUser());
    }

    @Produces
    @Named("user")
    public User getUser() {
        return getInstance();
    }

    public String grantRoleEmployee() {
        userService.grantRoleEmployee(getUser());
        getFacesMessagesManager().bufferWarning(Messages.get("grantRoleEmployeeSuccessful"));
        return getShowScreenRedirectURL();
    }

    public void handleUnselect(UnselectEvent<Role> event) {
        if (selectedRoles == null) {
            selectedRoles = new ArrayList<>();
        }
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (shibbolethId != null && getUser() != null && !isManaged()) {
            getUser().setShibbolethId(shibbolethId);
            getUser().setFirstName(shibbolethFirstName);
            getUser().setLastName(shibbolethLastName);
            getUser().setEmail(shibbolethEmail);
            getUser().setSalutation("female".equals(shibbolethGender) ? "Ms" : "Mr");
            getUser().setLogin(getUser().generateLoginName());
            logger.fine("Generating a user account for the current Shibboleth account with uniqueID = " + shibbolethId + ", givenName = " + shibbolethFirstName + ", surname = " + shibbolethLastName
                + ", mail = " + shibbolethEmail + ", gender = " + shibbolethGender);
        }
        if (getUser() != null) {
            imageCropperHelper.setImage(getUser().getImage());
            if (getUser().isManaged() && getUser().getUserBillingInfo() != null) {
                getUser().getUserBillingInfo().setOldVatNumber(getUser().getUserBillingInfo().getVatNumber());
                getUser().getUserBillingInfo().setOldReferenceNumber(getUser().getUserBillingInfo().getReferenceNumber());
            }
        }
        if (sidebarHelper.isTab("trainedInstruments")) {
            resetTrainedInstruments();
        }
    }

    public boolean isCompleteRequired() {
        return completeRequired;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean isInstrumentSelected() {
        // Note: Needed because JSF requires a properly writable property, i.e., roleManager.selectedUsers.contains(entity) is not applicable.
        return true;
    }

    public boolean isRenderAssignTrainedInstruments() {
        return !getSelectedUsers().isEmpty() && !getSelectedInstruments().isEmpty();
    }

    public boolean isRenderClearAssignTrainedInstruments() {
        return !getSelectedUsers().isEmpty() || !getSelectedInstruments().isEmpty();
    }

    @SuppressWarnings("SameReturnValue")
    public boolean isUserSelected() {
        // Note: Needed because JSF requires a properly writable property, i.e., roleManager.selectedUsers.contains(entity) is not applicable.
        return true;
    }

    @Override
    public User loadInstance() {
        final User user = super.loadInstance();
        if (user != null) {
            // Set affiliation values.
            user.setAffiliationValues();
            if (user.getUserBillingInfo() != null) {
                user.getUserBillingInfo().setAffiliationValues();
            } else if (sidebarHelper.isTab("userbillinginfo")) {
                user.setUserBillingInfo();
            }
        }
        return user;
    }

    public String removeMembershipAndTrackContainer(Container container, User user) {
        final Map<String, Set<String>> facesMessages = containerService.removeMembershipAndTrackContainer(container, user);
        printFacesMessagesClear(facesMessages);
        return getShowScreenRedirectURL(container.getClassNameLowerCase() + Constants.PLURAL_S);
    }

    public String removeUserBillingInfo() {
        // Important: load billing info from the database again in case it has been changed in the form before pressing on delete!
        UserBillingInfo userBillingInfo = entityService.find(UserBillingInfo.class, getUser().getUserBillingInfo().getId());
        getUser().setUserBillingInfo(null);
        userService.remove(userBillingInfo);
        save();
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted") + " " + Messages.get("userBillingInfo"));
        return getShowScreenRedirectURL(Constants.DETAILS);
    }

    public String requestAccess() {
        String validationErrorMsg = userService.isRequestAccessAllowed(getUser());
        if (validationErrorMsg != null) {
            getFacesMessagesManager().printError(validationErrorMsg);
            if (validationErrorMsg.equals(Messages.get("permissionDenied"))) {
                return createRedirectURL("user/edit", getUser().getId(), null, null);
            }
            if (validationErrorMsg.equals(Messages.get("profileIncomplete"))) {
                final HashMap<String, String> fParams = new HashMap<>();
                fParams.put("completeRequired", "true");
                return createRedirectURL("user/edit", getUser().getId(), null, fParams);
            }
        }

        Map<String, Set<String>> map = userService.requestAccess(getUser());
        printFacesMessages(map);
        return createRedirectURL("accessrequest/confirm", Long.valueOf(new ArrayList<>(map.get("accessRequestId")).get(0)), null, null);
    }

    public void resendVerificationMail() {
        printFacesMessages(userService.resendVerificationMail(getCurrentUser()));
    }

    public void resetShibbolethId() {
        userService.resetShibbolethId(getUser());
    }

    public void resetTrainedInstruments() {
        if (!getUser().getTrainedInstruments().isEmpty()) {
            getSelectedInstruments().addAll(getUser().getTrainedInstruments());
            for (Instrument instrument : getUser().getTrainedInstruments()) {
                getSelectedInstrumentsMap().put(instrument, true);
            }
        }
    }

    public String revokeRoleEmployee() {
        userService.revokeRoleEmployee(getUser());
        getFacesMessagesManager().bufferWarning(Messages.get("revokeRoleEmployeeSuccessful"));
        return getShowScreenRedirectURL();
    }

    @Override
    public String save() {
        try {
            LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
            if (password != null && confirmPassword != null && !Arrays.equals(password, confirmPassword)) {
                validationErrorMsg.put(Constants.EDIT + ":confirmNewPassword", Messages.get("passwordNotMatchException"));
            }
            validationErrorMsg.putAll(userService.isValid(getUser()));

            if (validationErrorMsg.isEmpty()) {
                printFacesMessagesClear(userService.save(getUser(), getCurrentUser(), password, !isManaged(), isCompleteRequired()));
                // The user is automatically logged in with the newly created login.
                if (getCurrentUser() == null) {
                    identityManager.authenticate(getUser());
                    // Clear sensitive data.
                    StringHelper.clearCharArray(password);
                    StringHelper.clearCharArray(confirmPassword);
                    StringHelper.clearCharArray(oldPassword);
                    return createRedirectURL("home-active");
                } else if (getCurrentUser().equals(getUser())) {
                    getSessionManager().initFilters(getUser());
                }
                // Clear sensitive data.
                StringHelper.clearCharArray(password);
                StringHelper.clearCharArray(confirmPassword);
                StringHelper.clearCharArray(oldPassword);
                // If the user was created by another user.
                return postSave(true, false);
            }
            handleValidationErrors(validationErrorMsg);
        } catch (Exception e) {
            getFacesMessagesManager().printError(e.getLocalizedMessage());
        }
        return null;
    }

    public String savePassword() {
        String ret = save();
        getUser().createEntityLog(LogActionEnum.PASSWORD_CHANGE);
        return ret;
    }

    public String saveRoles() {
        String message = userService.saveRoles(getUser(), getSelectedRoles());
        if (message != null) {
            getFacesMessagesManager().bufferWarningClear(message);
        }
        return createRedirectShowScreenURL(getUser(), "roles", null);
    }

    public String saveTrainedInstruments() {
        return super.save();
    }

    public void selectInstrument(Instrument instrument) {
        if (instrument != null) {
            getSelectedInstruments().add(instrument);
        }
    }

    public void selectUser(User user) {
        if (user != null) {
            getSelectedUsers().add(user);
        }
    }

    public String setCompleteRequired() {
        setCompleteRequired(true);
        return "/user/edit.html?userId=" + getUser().getId();
    }

    public void setCompleteRequired(boolean completeRequired) {
        this.completeRequired = completeRequired;
    }

    public void setComputerLoginActivated(List<User> users) {
        userService.setComputerLoginActivated(users);
    }

    public void setComputerLoginActivated(User user) {
        userService.setComputerLoginActivated(user);
    }

    public void setConfirmPassword(char[] confirmPassword) {
        this.confirmPassword = confirmPassword != null ? confirmPassword.clone() : null;
        // Clear sensitive data.
        StringHelper.clearCharArray(confirmPassword);
    }

    public void setInstrumentSelected(boolean isInstrumentSelected) {
        /*
         * Note: Needed because JSF requires a properly writable property, i.e., roleManager.selectedInstruments.contains(entity) is not applicable.
         */
    }

    public void setOldPassword(char[] oldPassword) {
        this.oldPassword = oldPassword != null ? oldPassword.clone() : null;
        // Clear sensitive data.
        StringHelper.clearCharArray(oldPassword);
    }

    public void setPassword(char[] password) {
        this.password = password != null ? password.clone() : null;
        getUser().setPassword(password);
        // Clear sensitive data.
        StringHelper.clearCharArray(password);
    }

    public void setSelectedRoles(List<Role> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public void setSystemUser() {
        setSystemUser(getUser());
    }

    public void setSystemUser(User user) {
        if (user != null) {
            userService.setSystemUser(user);
            getFacesMessagesManager().bufferWarningClear(Messages.get("setSystemUserHint"));
        }
    }

    public void setUserSelected(boolean isUserSelected) {
        // Note: Needed because JSF requires a properly writable property, i.e., roleManager.selectedUsers.contains(entity) is not applicable.
    }

    public String switchAccountEnabled() {
        userService.switchAccountEnabled(getUser());
        getFacesMessagesManager()
            .bufferWarningClear(Messages.get("successfullyEnabledDisabledUserAccount").replace("{0}", getUser().isAccountEnabled() ? Messages.get("enabled").toLowerCase()
                : Messages.get("disabled").toLowerCase()));
        return getShowScreenRedirectURL();
    }

    public void synchronizeWithAD(User user) {
        if (userService.synchronizeWithAD(user)) {
            getFacesMessagesManager().printWarning(Messages.get("synchronizationWithADStarted"));
        } else {
            getFacesMessagesManager().printWarning(Messages.get("synchronizationWithADNotStartedUser"));
        }
    }
}
