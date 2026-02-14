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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Configuration;
import org.bfabric.entity.User;
import org.bfabric.service.ConfService;
import org.bfabric.util.MailTemplateEngine;

@Named
@ApplicationScoped
public class ConfManager extends AbstractHttpServletManager {

    private static final long serialVersionUID = 1;

    private final Set<User> loggedInUsers = new HashSet<>();

    @Inject
    private ConfService confService;

    private Configuration configuration = null;

    private transient MailTemplateEngine mailTemplateEngine = null;

    private String randomUUID;

    @Produces
    @Named("configuration")
    public Configuration getConfiguration() {
        if (configuration == null) {
            setConfiguration();
        }
        return configuration;
    }

    public Set<User> getLoggedInUsers() {
        return loggedInUsers;
    }

    public MailTemplateEngine getMailTemplateEngine() {
        return mailTemplateEngine;
    }

    public String getRandomUUID() {
        return randomUUID;
    }

    @PostConstruct
    public void init() {
        setConfiguration();
    }

    public void setConfiguration() {
        // Generate a random UUID.
        randomUUID = String.valueOf(UUID.randomUUID());

        // Create new configuration object and set its context properties.
        configuration = confService.getConfiguration();

        // Create and set mail template engine.
        mailTemplateEngine = new MailTemplateEngine(configuration);
    }
}
