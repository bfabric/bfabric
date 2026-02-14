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

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.UserService;

@MeasureCalls
@Named
@ViewScoped
public class UserMergeManager extends AbstractEntityManager<User> {

    private static final long serialVersionUID = 1;

    private User mergeSelection = new User();

    private User merged;

    private User userLeft;

    @Inject
    private UserService userService;

    public User getMergeSelection() {
        return mergeSelection;
    }

    public User getMerged() {
        return merged;
    }

    public User getUserLeft() {
        return userLeft;
    }

    @Override
    @PostConstruct
    public void init() {
        // Important: do not invoke super.init since this will cause an error!
        initMerge();
    }

    private void initMerge() {
        try {
            setUserLeft((User) entityService.fetch(User.class, getIdLong(id)));
            setMerged((User) entityService.fetch(User.class, getIdLong(mergeId)));
            // Create the merge selection.
            setMergeSelection(getUserLeft().createMergeSelection(getMerged()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String merge() {
        try {
            // Check in which direction the merge should be done whereas a merge into the left is the default.
            if (getMerged().getLogin().equals(getMergeSelection().getLogin())) {
                // Switch userLeft and merged to perform merge right, i.e., merge the userLeft into merged.
                User tmp = getUserLeft();
                setUserLeft(getMerged());
                setMerged(tmp);
            }
            String errorMsg = userService.merge(getUserLeft(), getMergeSelection(), getMerged(), getCurrentUser());
            if (errorMsg != null) {
                getFacesMessagesManager().bufferErrorClear(errorMsg);
            }
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyMerged"));
            return createRedirectShowScreenURL(getUserLeft());
        } catch (final Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    public void setMergeSelection(User mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(User user) {
        merged = user;
        if (user != null) {
            mergeId = user.getIdString();
        }
    }

    public void setUserLeft(User user) {
        userLeft = user;
        if (user != null) {
            setIdLong(user.getId());
        }
    }
}
