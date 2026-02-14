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
import java.util.List;
import java.util.logging.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.User;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.IndexingService;
import org.bfabric.service.UserService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CollectionHelper;

@MeasureCalls
@Named
@ViewScoped
public class IndexingManager extends AbstractManager {

    private static final Logger logger = Logger.getLogger(IndexingManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    private IndexingService indexingService;

    private List<String> selectedClasses = new ArrayList<>();

    @Inject
    private UserService userService;

    public List<String> getIndexableClasses() {
        return ClassHelper.getIndexableClasses();
    }

    public List<String> getSelectedClasses() {
        return selectedClasses;
    }

    public void logMessage(String logMessage) {
        logMessage(logMessage, null, true);
    }

    public void logMessage(String logMessage, boolean printFacesMessage) {
        logMessage(logMessage, null, printFacesMessage);
    }

    public void logMessage(String logMessage, String facesMessage, boolean printFacesMessage) {
        if (printFacesMessage) {
            if (facesMessage != null) {
                getFacesMessagesManager().printWarn(facesMessage);
            } else {
                getFacesMessagesManager().printWarn(logMessage);
            }
        }
        logger.info(logMessage);
    }

    public void reindex() {
        indexingService.reindex();
        logMessage(Messages.get("reindex"));
    }

    public void reindex(Indexable entity) {
        indexingService.reindex(entity);
        logMessage(Messages.get("reindex") + " " + entity);
    }

    public void reindexClasses() {
        if (getSelectedClasses() != null) {
            indexingService.reindexClasses(getSelectedClasses());
            logMessage(Messages.get("reindex") + " " + CollectionHelper.printBasic(getSelectedClasses(), " "));
        }
    }

    public String resetComputerLoginActivated() {
        userService.resetComputerLoginActivated();
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyReset"));
        return createRedirectURL("about");
    }

    public void setSelectedClasses(List<String> selectedClasses) {
        this.selectedClasses = selectedClasses;
    }

    public String synchronizeAllUsersWithAD() {
        logger.info("synchronizeAllUsersWithAD started");
        int synchronizedUsers = 0;
        for (User user : userService.getAllUsers()) {
            if (user.isRecomputeComputerLoginAndDataAccessEnabledRequired()) {
                userService.synchronizeWithAD(user);
                logger.fine(++synchronizedUsers + ": synchronizing " + user);
            }
        }
        logger.info("Users synchronized: " + synchronizedUsers);
        getFacesMessagesManager().bufferWarningClear(Messages.get("synchronizationWithADFinished").replace("{0}", String.valueOf(synchronizedUsers)));
        return createRedirectURL("about");
    }
}