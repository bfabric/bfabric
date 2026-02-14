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

package org.bfabric.enums;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.entity.Configuration;
import org.bfabric.entity.User;
import org.bfabric.service.IdentityService;
import org.bfabric.util.ConfigurationHelper;

public enum MailSenderEnum {

    COORDINATOR,
    PERSONAL,
    SUPPORT,
    SYSTEM;

    public String getAddress() {
        Configuration configuration = ConfigurationHelper.getConfiguration();
        switch (this) {
        case COORDINATOR:
            return configuration.getCoordinatorEmail();
        case PERSONAL:
        case SYSTEM:
            return configuration.getFromEmailAddress();
        case SUPPORT:
            return configuration.getSupportEmail();
        default:
            return "MailSenderEnumAddress to be set!";
        }
    }

    public String getLabel() {
        return name().toLowerCase();
    }

    public String getName() {
        User user = CDI.current().select(IdentityService.class).get().getCurrentUser();
        Configuration configuration = ConfigurationHelper.getConfiguration();
        switch (this) {
        case COORDINATOR:
            return configuration.getDeployer() + " " + "Coordinator";
        case PERSONAL:
            return user.getLastNameFirstName() + " via " + configuration.getApplicationName();
        case SUPPORT:
            return configuration.getDeployer().getValue();
        case SYSTEM:
            return "No-Reply (" + configuration.getDeployer() + " " + configuration.getApplicationName() + ")";
        default:
            return "MailSenderEnumName to be set!";
        }
    }

    public String getReplyToAddress() {
        User user = CDI.current().select(IdentityService.class).get().getCurrentUser();
        return user != null && this == PERSONAL ? user.getEmail() : null;
    }
}