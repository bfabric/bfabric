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
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.bfabric.Messages;
import org.bfabric.entity.User;

public class UserFunction implements Serializable {

    private static final long serialVersionUID = 1;

    private final List<String> functions = new ArrayList<>();

    private boolean checked;

    private boolean deletable;

    private User user;

    private String userFirstName;

    private long userId;

    private String userLastName;

    public UserFunction(User user, String function, boolean deletable) {
        if (user != null) {
            setChecked(true);
            setUser(user);
            setUserId(user.getId());
            setUserFirstName(user.getFirstName());
            setUserLastName(user.getLastName());
            setDeletable(deletable);
            getFunctions().add(function);
        }
    }

    public void addFunction(String function) {
        if (function != null && !getFunctions().contains(function)) {
            getFunctions().add(function);
        }
    }

    public void check() {
        setChecked(true);
    }

    public String getDisableNotificationExclusionHint(boolean cannotExcludeSupporter) {
        if (cannotExcludeSupporter && isSupporter()) {
            return getUser().isEmployee() ? Messages.get("disableNotificationExclusionSupporterHint") : Messages.get("disableNotificationExclusionRecipientHint");
        }
        return getUser().isDisableNotificationExclusion() ? Messages.get("disableNotificationExclusionRecipientHint") : "";
    }

    public List<String> getFunctions() {
        return functions;
    }

    public User getUser() {
        return user;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public boolean isDisableNotificationExclusion(boolean cannotExcludeSupporter) {
        return cannotExcludeSupporter && isSupporter() || getUser().isDisableNotificationExclusion();
    }

    public boolean isManager() {
        return CollectionUtils.containsAny(getFunctions(), UserFunctionHelper.getFunctionsManager());
    }

    public boolean isSupporter() {
        return CollectionUtils.containsAny(getFunctions(), UserFunctionHelper.getFunctionsSupporter());
    }

    public void removeFunction(String function) {
        if (function != null) {
            getFunctions().remove(function);
        }
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public void uncheck() {
        setChecked(false);
    }
}