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

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.bfabric.Constants;
import org.bfabric.service.IdentityService;
import org.bfabric.util.StringHelper;

// Important: Do not delete! The Web listener is currently used for testing purposes to see when an HTTP session is created or destroyed.
@WebListener
public class BfabricSessionListener implements HttpSessionListener {

    private static final Logger logger = Logger.getLogger(BfabricSessionListener.class.getName());

    @Inject
    private IdentityService identityService;

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String login = (String) event.getSession().getAttribute(Constants.LOGIN);
        String logout = (String) event.getSession().getAttribute(Constants.LOGOUT);
        if (!StringHelper.isEmpty(login)) {
            if (StringHelper.isEmpty(logout)) {
                identityService.logLogoutViaTimeout(login);
                logger.fine("SessionListener sessionDestroyed via timeout " + login);
            } else {
                logger.fine("SessionListener sessionDestroyed via logout " + logout);
            }
        } else {
            logger.fine("SessionListener sessionDestroyed via timeout ");
        }
    }
}
