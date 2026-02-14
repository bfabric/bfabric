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

package org.bfabric.listener;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.bfabric.Constants;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.User;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.filter.MeasureCallsFilter;
import org.bfabric.manager.ConfManager;
import org.bfabric.service.IdentityService;
import org.bfabric.xml.XmlHelper;

@WebListener
public class BfabricServletContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(BfabricServletContextListener.class.getName());

    @Inject
    private ConfManager confManager;

    @Inject
    private IdentityService identityService;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Set<User> loggedInUsers = new HashSet<>(confManager.getLoggedInUsers());
        for (User user : loggedInUsers) {
            identityService.logLogout(user, new EntityLog(user, LogActionEnum.LOGOUT_RESTART));
        }
        User admin = identityService.getUserByLogin(Constants.ADMIN);
        if (admin != null) {
            identityService.persist(new EntityLog(admin, LogActionEnum.SYSTEM_STOP));
        }
        logger.fine("BfabricServletContextListener destroyed");
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        if (confManager.getConfiguration().isMeasureCallsFilterEnabled()) {
            FilterRegistration filterRegistration = event.getServletContext().addFilter("MeasureCallsFilter", MeasureCallsFilter.class);
            filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "*.html");
        }
        User admin = identityService.getUserByLogin(Constants.ADMIN);
        if (admin != null) {
            String log = XmlHelper.getReleaseLog(confManager.getConfiguration(), identityService.getLastReleaseEntityLog());
            if (log != null) {
                identityService.persist(new EntityLog(admin, LogActionEnum.SYSTEM_RELEASE, log));
            }
            identityService.persist(new EntityLog(admin, LogActionEnum.SYSTEM_START));
        }
        logger.fine("BfabricServletContextListener started");
    }
}
