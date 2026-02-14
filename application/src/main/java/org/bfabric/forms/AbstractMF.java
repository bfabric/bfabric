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

package org.bfabric.forms;

import javax.enterprise.inject.spi.CDI;

import org.apache.commons.codec.binary.Base64;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Configuration;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.EntityService;
import org.bfabric.service.IdentityService;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.server.manager.WSValidationManager;

public abstract class AbstractMF {

    private Configuration configuration;

    private IdentityService identityService;

    private WSValidationManager wsValidationManager;

    public static BfabricUploadedFile decodeAndCreateFile(String input, String fileName) throws InvalidDataException {
        if (StringHelper.isNotEmpty(input) && StringHelper.isNotEmpty(fileName)) {
            try {
                byte[] decodedString = Base64.decodeBase64(input);
                return new BfabricUploadedFile(decodedString, fileName, ConfigurationHelper.getConfiguration().getDefaultCharset());
            } catch (Exception e) {
                throw new InvalidDataException("Internal error: " + e.getMessage());
            }
        }
        return null;
    }

    public abstract void apply() throws Exception;

    public AbstractEntity fetch(Class<? extends AbstractEntity> entityClass, Long entityId) throws InvalidDataException {
        return CDI.current().select(EntityService.class).get().fetch(entityClass, entityId);
    }

    public AbstractEntity fetch(String entityClassName, Long entityId) throws InvalidDataException {
        return CDI.current().select(EntityService.class).get().fetch(entityClassName, entityId);
    }

    public AbstractEntity findByName(Class<? extends AbstractEntity> entityClass, String entityName) throws InvalidDataException {
        return CDI.current().select(EntityService.class).get().findByName(entityClass, entityName);
    }

    public AbstractEntity findByName(String entityClassName, String entityName) throws InvalidDataException {
        return CDI.current().select(EntityService.class).get().findByName(entityClassName, entityName);
    }

    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = ConfigurationHelper.getConfiguration();
        }
        return configuration;
    }

    protected IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = CDI.current().select(IdentityService.class).get();
        }
        return identityService;
    }

    public WSValidationManager getWSValidationManager() {
        if (wsValidationManager == null) {
            wsValidationManager = CDI.current().select(WSValidationManager.class).get();
        }
        return wsValidationManager;
    }
}
