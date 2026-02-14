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

package org.bfabric.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections4.CollectionUtils;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.enums.StatusEnum;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

public abstract class UserFunctionHelper implements Serializable {

    public static final String CUSTOM = "Custom";

    public static final String REQUESTER = "Requester";

    public static final String CONTACT = "Contact";

    public static final String BUDGET_OFFICER = "BudgetOfficer";

    public static final String LEADER = "Leader";

    public static final String MEMBER = "Member";

    public static final String INTERNAL_MEMBER = "InternalMember";

    public static final String COACH = "Coach";

    public static final String COACH_BACKUP = "CoachBackup";

    public static final String BIOINFORMATICIAN = "Bioinformatician";

    public static final String SERVICE_TRACKER = "ServiceTracker";

    public static final String SERVICE_TYPE_COACH = "ServiceTypeCoach";

    public static final String SERVICE_TYPE_TRACKER = "ServiceTypeTracker";

    public static final String TRACKER = "Tracker";

    public static final String PROJECT_REQUESTER = "PRequester";

    public static final String PROJECT_CONTACT = "PContact";

    public static final String PROJECT_BUDGET_OFFICER = "PBudgetOfficer";

    public static final String PROJECT_LEADER = "PLeader";

    public static final String PROJECT_COACH = "PCoach";

    public static final String PROJECT_COACH_BACKUP = "PCoachBackup";

    public static final String PROJECT_BIOINFORMATICIAN = "PBioinformatician";

    public static final String REPLY_TO_USER = "ReplyToUser";

    private static final List<String> functionsSupporter = new ArrayList<>();

    private static final List<String> functionsManager = new ArrayList<>();

    private static final List<String> functionsUser = new ArrayList<>();

    private static final List<String> functionsAll = new ArrayList<>();

    private static final long serialVersionUID = 1;

    static {
        functionsManager.add(REQUESTER);
        functionsManager.add(CONTACT);
        functionsManager.add(BUDGET_OFFICER);
        functionsManager.add(LEADER);
        functionsManager.add(PROJECT_REQUESTER);
        functionsManager.add(PROJECT_CONTACT);
        functionsManager.add(PROJECT_BUDGET_OFFICER);
        functionsManager.add(PROJECT_LEADER);

        functionsUser.addAll(functionsManager);
        functionsUser.add(MEMBER);
        functionsUser.add(INTERNAL_MEMBER);

        functionsSupporter.add(COACH);
        functionsSupporter.add(COACH_BACKUP);
        functionsSupporter.add(BIOINFORMATICIAN);
        functionsSupporter.add(PROJECT_COACH);
        functionsSupporter.add(PROJECT_COACH_BACKUP);
        functionsSupporter.add(PROJECT_BIOINFORMATICIAN);
        functionsSupporter.add(SERVICE_TRACKER);
        functionsSupporter.add(SERVICE_TYPE_COACH);
        functionsSupporter.add(SERVICE_TYPE_TRACKER);
        functionsSupporter.add(TRACKER);

        functionsAll.addAll(functionsUser);
        functionsAll.addAll(functionsSupporter);
        functionsAll.add(REPLY_TO_USER);
        functionsAll.add(CUSTOM);
    }

    private final List<String> functionsInitList = new ArrayList<>();

    private final List<UserFunction> userFunctions = new ArrayList<>();

    private List<String> functions = new ArrayList<>();

    private boolean functionsSupporterChecked;

    private boolean functionsUserChecked;

    private boolean sendMail = true;

    private UserGroup userGroup;

    private List<User> usersCustom = new ArrayList<>();

    public static Collection<String> getFunctionsManager() {
        return functionsManager;
    }

    public static Collection<String> getFunctionsSupporter() {
        return functionsSupporter;
    }

    public static Collection<String> getFunctionsUser() {
        return functionsUser;
    }

    public void addFunction(String function) {
        if (function != null && !getFunctions().contains(function)) {
            getFunctions().add(function);
        }
    }

    public void addFunctionInit(String functionInit) {
        if (functionInit != null && !getFunctionsInitList().contains(functionInit)) {
            getFunctionsInitList().add(functionInit);
        }
    }

    public void addFunctions(Collection<String> functionsList) {
        if (functionsList != null && !functionsList.isEmpty()) {
            for (String function : functionsList) {
                addFunction(function);
            }
        }
    }

    public void addUserFunction(User user, String function, boolean init) {
        if (user != null && function != null) {
            boolean done = false;
            addFunction(function);
            for (UserFunction userFunction : getUserFunctions()) {
                if (userFunction.getUserId() == user.getId()) {
                    userFunction.check();
                    userFunction.addFunction(function);
                    done = true;
                    break;
                }
            }
            if (!done) {
                getUserFunctions().add(new UserFunction(user, function, !init));
            }
        }
    }

    public void addUserFunctions(Collection<User> users, String function, boolean init) {
        if (users != null && !users.isEmpty() && function != null) {
            for (User user : users) {
                addUserFunction(user, function, init);
            }
        }
    }

    public void checkAll() {
        for (UserFunction userFunction : getUserFunctions()) {
            userFunction.check();
        }
        resetFunctions();
        setFunctionsChecked();
    }

    public void checkByUserFunction() {
        for (UserFunction userFunction : getUserFunctions()) {
            if (CollectionUtils.containsAny(getFunctions(), userFunction.getFunctions())) {
                userFunction.check();
            } else if (isUncheckEnabled(userFunction)) {
                userFunction.uncheck();
            }
        }
        setFunctionsChecked();
    }

    public void functionsChanged(ValueChangeEvent event) {
        getFunctions().clear();
        getFunctions().addAll((List<String>) event.getNewValue());
        checkByUserFunction();
    }

    public void functionsSupporterCheckedChanged(ValueChangeEvent event) {
        setFunctionsSupporterChecked((Boolean) event.getNewValue());
        if (isFunctionsSupporterChecked()) {
            getFunctions().addAll(functionsSupporter);
        } else {
            getFunctions().removeAll(functionsSupporter);
        }
        checkByUserFunction();
    }

    public void functionsUserCheckedChanged(ValueChangeEvent event) {
        setFunctionsUserChecked((Boolean) event.getNewValue());
        if (isFunctionsUserChecked()) {
            getFunctions().addAll(functionsUser);
        } else {
            getFunctions().removeAll(functionsUser);
        }
        checkByUserFunction();
    }

    public String getFunctionStyleClass(String value) {
        return isFunctionsSupporter(value) ? StatusEnum.PROCESSED.getLabel() : isFunctionsUser(value) ? StatusEnum.PENDING.getLabel() : null;
    }

    public List<String> getFunctions() {
        if (functions == null) {
            functions = new ArrayList<>();
        }
        return functions;
    }

    public List<String> getFunctionsInitList() {
        return functionsInitList;
    }

    public Collection<String> getFunctionsSupporterInit() {
        return CollectionUtils.retainAll(functionsSupporter, functionsInitList);
    }

    public String getFunctionsSupporterStyleClass() {
        return getFunctionStyleClass(getFunctionsSupporterInit().stream().findFirst().orElse(null));
    }

    public Collection<String> getFunctionsUserInit() {
        return CollectionUtils.retainAll(functionsUser, functionsInitList);
    }

    public String getFunctionsUserStyleClass() {
        return getFunctionsUserInit().stream().findFirst().map(this::getFunctionStyleClass).orElse(null);
    }

    public List<UserFunction> getUserFunctions() {
        return userFunctions;
    }

    public List<UserFunction> getUserFunctionsChecked() {
        List<UserFunction> userFunctions = new ArrayList<>();
        for (UserFunction userFunction : getUserFunctions()) {
            if (userFunction.isChecked()) {
                userFunctions.add(userFunction);
            }
        }
        return userFunctions;
    }

    public String getUserFunctionsDetails() {
        StringBuilder builder = new StringBuilder();
        for (UserFunction userFunction : getUserFunctions()) {
            builder.append("\n").append(userFunction.getUserId()).append(" ").append(userFunction.getUserFirstName()).append(" ").append(userFunction.getUserLastName()).append(" checked=")
                .append(userFunction.isChecked()).append(" deletable=").append(userFunction.isDeletable()).append(" ").append(userFunction.getFunctions());
        }
        return builder.toString();
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (UserFunction userFunction : getUserFunctions()) {
            if (userFunction.isChecked()) {
                users.add(userFunction.getUser());
            }
        }
        return users;
    }

    public List<User> getUsersCustom() {
        return usersCustom;
    }

    public void init() {
        getFunctions().clear();
        getFunctionsInitList().clear();
        getUserFunctions().clear();
        getUsersCustom().clear();
        setUserGroup(null);
        resetFunctions();
        resetFunctionsInitList();
        uncheckAll();
        setFunctionsChecked();
    }

    public boolean isFunctionsSupporter(String value) {
        return functionsSupporter.contains(value);
    }

    public boolean isFunctionsSupporterChecked() {
        return functionsSupporterChecked;
    }

    public boolean isFunctionsUser(String value) {
        return functionsUser.contains(value);
    }

    public boolean isFunctionsUserChecked() {
        return functionsUserChecked;
    }

    public boolean isRenderedFunctionsInitSelect() {
        return getFunctionsUserInit().size() > 1 || getFunctionsSupporterInit().size() > 1;
    }

    public boolean isRenderedFunctionsSelect() {
        return !getFunctionsInitList().isEmpty();
    }

    public boolean isRenderedFunctionsSupporterInitSelect() {
        return !getFunctionsSupporterInit().isEmpty();
    }

    public boolean isRenderedFunctionsUserInitSelect() {
        return !getFunctionsUserInit().isEmpty();
    }

    public boolean isSendMail() {
        return sendMail;
    }

    public boolean isUncheckEnabled(UserFunction userFunction) {
        return true;
    }

    public void removeFunction(String function) {
        if (function != null) {
            getFunctions().remove(function);
        }
    }

    public void removeFunctionInit(String functionInit) {
        if (functionInit != null) {
            getFunctionsInitList().remove(functionInit);
        }
    }

    public void removeUserFunction(User user, String function) {
        if (user != null && function != null) {
            UserFunction matchingUserFunction = null;
            for (UserFunction userFunction : getUserFunctions()) {
                if (userFunction.getUserId() == user.getId()) {
                    matchingUserFunction = userFunction;
                    break;
                }
            }
            if (matchingUserFunction != null) {
                matchingUserFunction.removeFunction(function);
                if (matchingUserFunction.getFunctions().isEmpty() && matchingUserFunction.isDeletable()) {
                    getUserFunctions().remove(matchingUserFunction);
                }

            }
        }
    }

    public void removeUserFunctions(Collection<User> users, String function) {
        if (users != null && !users.isEmpty() && function != null) {
            for (User user : users) {
                removeUserFunction(user, function);
            }
        }
    }

    public void resetFunctions() {
        Set<String> functionsChecked = new HashSet<>();
        Set<String> functionsUnchecked = new HashSet<>();
        for (UserFunction userFunction : getUserFunctions()) {
            if (userFunction.isChecked()) {
                functionsChecked.addAll(userFunction.getFunctions());
            } else {
                functionsUnchecked.addAll(userFunction.getFunctions());
            }
        }
        getFunctions().clear();
        addFunctions(CollectionHelper.sortObjects(CollectionUtils.removeAll(functionsChecked, functionsUnchecked)));
    }

    public void resetFunctionsInitList() {
        Set<String> functionsChecked = new HashSet<>();
        for (UserFunction userFunction : getUserFunctions()) {
            if (!userFunction.isDeletable() && userFunction.isChecked()) {
                functionsChecked.addAll(userFunction.getFunctions());
            }
        }
        List<String> functionsInitList = new ArrayList<>(functionsAll);
        functionsInitList.retainAll(functionsChecked);
        getFunctionsInitList().addAll(functionsInitList);
        if (getUserGroup() != null) {
            getFunctionsInitList().add(getUserGroup().getName());
        }
        if (getUsersCustom() != null && !getUsersCustom().isEmpty()) {
            getFunctionsInitList().add(CUSTOM);
        }
    }

    public void sendMailChanged(ValueChangeEvent event) {
        setSendMail((Boolean) event.getNewValue());
        if (!isSendMail()) {
            uncheckAll();
        } else {
            init();
            checkAll();
        }
    }

    public void setFunctions(List<String> functions) {
        this.functions = functions;
    }

    public void setFunctionsChecked() {
        setFunctionsUserChecked(!getFunctions().isEmpty() && getFunctions().containsAll(getFunctionsUserInit()));
        setFunctionsSupporterChecked(!getFunctions().isEmpty() && getFunctions().containsAll(getFunctionsSupporterInit()));
        setSendMail(!getUsers().isEmpty());
    }

    public void setFunctionsSupporterChecked(boolean functionsSupporterChecked) {
        this.functionsSupporterChecked = functionsSupporterChecked;
    }

    public void setFunctionsUserChecked(boolean functionsUserChecked) {
        this.functionsUserChecked = functionsUserChecked;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public void setUsersCustom(List<User> usersCustom) {
        this.usersCustom = usersCustom;
    }

    public void uncheckAll() {
        for (UserFunction userFunction : getUserFunctions()) {
            userFunction.uncheck();
        }
        getFunctions().clear();
        setFunctionsChecked();
    }

    public void userFunctionCheckedChanged(ValueChangeEvent event) {
        UserFunction userFunction = (UserFunction) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("userFunction");
        userFunction.setChecked((Boolean) event.getNewValue());
        resetFunctions();
        setFunctionsChecked();
    }

    public void userFunctionCustomSelectListener(SelectEvent<User> event) {
        addFunction(CUSTOM);
        addFunctionInit(CUSTOM);
        addUserFunction(event.getObject(), CUSTOM, false);
        setFunctionsChecked();
    }

    public void userFunctionCustomUnselectListener(UnselectEvent<User> event) {
        removeUserFunction(event.getObject(), CUSTOM);
        if (getUsersCustom().isEmpty()) {
            removeFunction(CUSTOM);
            removeFunctionInit(CUSTOM);
        }
        setFunctionsChecked();
    }

    public void userGroupChanged(ValueChangeEvent event) {
        UserGroup oldUserGroup = (UserGroup) event.getOldValue();
        UserGroup newUserGroup = (UserGroup) event.getNewValue();
        setUserGroup(newUserGroup);
        if (newUserGroup != null) {
            addFunction(newUserGroup.getName());
            addFunctionInit(newUserGroup.getName());
            addUserFunctions(newUserGroup.getUsers(), newUserGroup.getName(), false);
        }
        if (oldUserGroup != null) {
            removeFunction(oldUserGroup.getName());
            removeFunctionInit(oldUserGroup.getName());
            removeUserFunctions(oldUserGroup.getUsers(), oldUserGroup.getName());
        }
        setFunctionsChecked();
    }
}