package org.bfabric.util;

import java.util.ArrayList;
import java.util.List;

import org.bfabric.entity.Container;

public class DashboardHelper extends UserFunctionHelper {

    private static final long serialVersionUID = 1;

    private Container container;

    private List<UserFunction> managers;

    private List<UserFunction> members;

    private List<UserFunction> supporters;

    public DashboardHelper(Container container) {
        this.container = container;
        if (container != null) {
            addUserFunction(getContainer().getRequester(), REQUESTER, true);
            addUserFunction(getContainer().getContact(), CONTACT, true);
            addUserFunction(getContainer().getBudgetOfficer(), BUDGET_OFFICER, true);
            addUserFunction(getContainer().getLeader(), LEADER, true);
            addUserFunction(getContainer().getCoach(), COACH, true);
            addUserFunctions(getContainer().getMembers(), MEMBER, true);
            addUserFunctions(getContainer().getTrackingUsers(), TRACKER, true);
        }
    }

    public Container getContainer() {
        return container;
    }

    public List<UserFunction> getManagers() {
        if (managers == null) {
            managers = new ArrayList<>();
            for (UserFunction userFunction : getUserFunctions()) {
                if (userFunction.isManager()) {
                    managers.add(userFunction);
                }
            }
        }
        return managers;
    }

    public List<UserFunction> getMembers() {
        if (members == null) {
            members = new ArrayList<>();
            for (UserFunction userFunction : getUserFunctions()) {
                if (!userFunction.isManager() && !userFunction.isSupporter()) {
                    members.add(userFunction);
                }
            }
        }
        return members;
    }

    public List<UserFunction> getSupporters() {
        if (supporters == null) {
            supporters = new ArrayList<>();
            for (UserFunction userFunction : getUserFunctions()) {
                if (userFunction.isSupporter()) {
                    supporters.add(userFunction);
                }
            }
        }
        return supporters;
    }
}