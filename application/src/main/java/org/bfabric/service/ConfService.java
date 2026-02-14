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

package org.bfabric.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.Constants;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.ContextProperty;
import org.bfabric.entity.DeployerContextProperty;
import org.bfabric.entity.EnvironmentContextProperty;
import org.bfabric.entity.InstanceContextProperty;
import org.bfabric.entity.Role;
import org.bfabric.entity.SystemProperty;
import org.bfabric.enums.ContextPropertyDiscriminator;
import org.bfabric.enums.SystemPropertyDiscriminator;
import org.bfabric.exception.BfabricValidatorException;

@Named
@Stateless
public class ConfService extends AbstractService {

    private static final Logger logger = Logger.getLogger(ConfService.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    private StorageService storageService;

    private static String debugContext(String environment, String deployer, String instance) {
        return "[environment: " + environment + ", deployer: " + deployer + ", instance: " + instance + "]";
    }

    private static String deployerOrNull(String deployer) {
        return " and ( deployer = '" + deployer + "' or deployer is null )";
    }

    private static String deployerOrNullNotExistsDeployer(String mappedClass, String deployer) {
        return "( deployer = '" + deployer + "' or deployer is null and not exists(select id from " + mappedClass + " p2 where p2.name=p1.name and deployer = '" + deployer + "')) ";
    }

    private static String envOrNull(String environment) {
        return " and ( environment = '" + environment + "' or environment is null )";
    }

    private static String envOrNullNotExistsEnv(String mappedClass, String environment) {
        return "( environment = '" + environment + "' or environment is null and not exists(select id from " + mappedClass + " p2 where p2.name=p1.name and environment = '" + environment + "')) ";
    }

    private static String instanceOrNull(String instance) {
        return " and ( instance = '" + instance + "' or instance is null )";
    }

    private static String instanceOrNullNotExistsInstance(String mappedClass, String instance) {
        return "( instance = '" + instance + "' or instance is null and not exists(select id from " + mappedClass + " p2 where p2.name=p1.name and instance = '" + instance + "')) ";
    }

    private static String makeGetActivePropertiesHQLFromWhereEqualClause(String environment, String deployer, String instance) {
        final String mappedClass = SystemProperty.class.getName();
        return "from " + mappedClass + " p1 " + "where " + envOrNullNotExistsEnv(mappedClass, environment) + "and " + deployerOrNullNotExistsDeployer(mappedClass, deployer) + "and "
            + instanceOrNullNotExistsInstance(mappedClass, instance) + " order by name";
    }

    private static String makeGetPropertyHQLFromWhereEqualClauseForType(String environment, String deployer, String instance) {
        return "from SystemProperty where name = :name" + envOrNull(environment) + deployerOrNull(deployer) + instanceOrNull(instance) + " order by deployer, environment, instance";
    }

    private static String makeGetPropertyHQLFromWhereLikeClause(String environment, String deployer, String instance) {
        return "from SystemProperty where name like :name" + envOrNull(environment) + deployerOrNull(deployer) + instanceOrNull(instance) + " order by name, deployer, environment, instance";
    }

    public <T> T getActiveContext(Class<T> type) {
        ContextProperty activeContext;
        String sql = "from " + type.getName() + " " + "context where context.active=true";
        String logMessagePrefix = "getActiveContext [" + sql + "]";
        try {
            activeContext = (ContextProperty) createQuery(sql).getSingleResult();
            // logger.fine(logMessagePrefix + " returned [" + activeContext.toString() + "]");
        } catch (NoResultException e) {
            logger.severe(logMessagePrefix + " throws " + e);
            throw new RuntimeException("No active context property found for " + type.getName(), e);
        } catch (NonUniqueResultException e) {
            logger.severe(logMessagePrefix + " throws " + e);
            throw new RuntimeException("Not unique active context property found for " + type.getName(), e);
        }
        return (T) activeContext;
    }

    public Map<ContextPropertyDiscriminator, ContextProperty> getActiveContextProperties() {
        Map<ContextPropertyDiscriminator, ContextProperty> activeContextProperties = new HashMap<>();
        String sql = "from " + ContextProperty.class.getName() + " " + " where active=true";
        final List<ContextProperty> activeContexts = createQuery(sql).getResultList();
        for (ContextProperty contextProperty : activeContexts) {
            activeContextProperties.put(contextProperty.getDiscriminator(), contextProperty);
        }
        return activeContextProperties;
    }

    private Map<String, SystemProperty> getActiveProperties(String environment, String deployer, String instance) {
        Map<String, SystemProperty> properties = new HashMap<>();
        for (SystemProperty property : getActiveSystemProperties(environment, deployer, instance)) {
            properties.put(property.getName(), property);
        }
        return properties;
    }

    public List<SystemProperty> getActiveSystemProperties(String environment, String deployer, String instance) {
        String sql = makeGetActivePropertiesHQLFromWhereEqualClause(environment, deployer, instance);
        return createQuery(sql).getResultList();
    }

    public Configuration getConfiguration() {
        // Create new configuration object and set its context properties.
        Configuration configuration = new Configuration();

        configuration.setEnvironment(getActiveContext(EnvironmentContextProperty.class));
        configuration.setDeployer(getActiveContext(DeployerContextProperty.class));
        configuration.setInstance(getActiveContext(InstanceContextProperty.class));

        // Read all active (context-dependent) system properties from the database.
        Map<String, SystemProperty> activeProperties = getActiveProperties(configuration.getEnvironment().getValue(), configuration.getDeployer().getValue(), configuration.getInstance().getValue());

        logger.info("Active Context " + debugContext(configuration.getEnvironment().getValue(), configuration.getDeployer().getValue(), configuration.getInstance().getValue()));

        // IMPORTANT: The deployerDefaultEmail parameter must be set before the other (email related) ones since this
        // email is used whenever the specific email is not set. Therefore, please leave method invocation at the beginning.
        configuration.setDeployerDefaultEmail((String) activeProperties.get("deployerDefaultEmail").getValueTyped());

        // Check if the necessary local storages are available in the database
        storageService.getStorageByName(Constants.LOCAL_INTERNAL_STORAGE);
        storageService.getStorageByName(Constants.LOCAL_EXTERNAL_STORAGE);
        storageService.getStorageByName(Constants.LOCAL_TEMPORARY_STORAGE);

        // Read and set configuration parameters.
        for (Entry<String, SystemProperty> activeProperty : activeProperties.entrySet()) {
            try {
                PropertyUtils.setProperty(configuration, activeProperty.getKey(), activeProperty.getValue().getValueTyped());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        }
        return configuration;
    }

    public <T> T getContext(String value, Class<T> type) {
        String sql = "from " + type.getName() + " " + "context where context.value=:value";
        String logMessagePrefix = "getContext [" + sql + "] [value=" + value + "]";
        try {
            final ContextProperty context = (ContextProperty) createQuery(sql).setParameter("value", value).getSingleResult();
            // logger.fine(logMessagePrefix + " returned [" + context.toString() + "]");
            return (T) context;
        } catch (NoResultException e) {
            logger.severe(logMessagePrefix + " throws " + e);
            throw new RuntimeException("No context property found for " + type.getName(), e);
        } catch (NonUniqueResultException e) {
            logger.severe(logMessagePrefix + " throws " + e);
            throw new RuntimeException("Not unique context property found for " + type.getName(), e);
        }
    }

    public List<? extends ContextProperty> getContexts(Class<? extends ContextProperty> type) {
        String sql = "from " + type.getName() + " " + " order by value";
        return createQuery(sql).getResultList();
    }

    public List<? extends ContextProperty> getEnabledContexts(Class<? extends ContextProperty> type) {
        String sql = "from " + type.getName() + " " + "context where enabled = TRUE order by context.value";
        return createQuery(sql).getResultList();
    }

    public List<SystemProperty> getProperties(final String name, final String environment, final String deployer, final String instance) {
        String sql = makeGetPropertyHQLFromWhereLikeClause(environment, deployer, instance);
        logger.fine("HQL query: " + sql);
        return createQuery(sql).setParameter("name", name).getResultList();
    }

    public <T> T getProperty(String name, String environment, String deployer, String instance, final T defaultValue) {
        String sql = makeGetPropertyHQLFromWhereEqualClauseForType(environment, deployer, instance);
        final List<SystemProperty> list = createQuery(sql).setParameter("name", name).getResultList();
        logger.fine("getProperty [" + sql + "]" + "returned [" + list.size() + "] rows - " + debugContext(environment, deployer, instance));
        if (!list.isEmpty()) {
            return (T) list.get(0).getValue();
        }
        return defaultValue;
    }

    public void setContext(String value, Boolean active, Class<? extends ContextProperty> type) {
        if (value != null && value.contains("%")) {
            throw new IllegalArgumentException("Property names are not allowed to contain the % character but '" + value + "' was supplied.");
        }

        final List<? extends ContextProperty> list = getContexts(type);
        boolean found = false;

        if (!list.isEmpty()) {
            for (ContextProperty currentProp : list) {
                if (currentProp.getValue() != null && currentProp.getValue().equals(value) || currentProp.getValue() == null && value == null) {
                    found = true;
                    currentProp.setActive(active != null && active);
                    update(currentProp);
                }
            }
        }

        if (list.isEmpty() || !found) {
            try {
                ContextProperty newProp = type.getDeclaredConstructor().newInstance();
                newProp.setValue(value);
                newProp.setActive(active != null && active);
                newProp.setEnabled(true);
                persist(newProp);
            } catch (Exception e) {
                logger.severe(e.toString());
            }
        }
    }

    public void setProperty(String name, SystemPropertyDiscriminator discriminator, String value, String comment, String environment, String deployer, String instance, Role requiredRole) {
        if (name.contains("%")) {
            throw new BfabricValidatorException("errorInvalidPercentCharacter");
        }

        final List<? extends SystemProperty> list = getProperties(name, environment, deployer, instance);
        boolean found = false;
        if (!list.isEmpty()) {
            for (SystemProperty currentProp : list) {
                if (currentProp.configurationContextMatches(environment, deployer, instance)) {
                    try {
                        found = true;
                        currentProp.setDiscriminator(discriminator);
                        currentProp.setValue(value);
                        currentProp.setComment(comment);
                        currentProp.setEnvironment(environment);
                        currentProp.setDeployer(deployer);
                        currentProp.setInstance(instance);
                        currentProp.setRequiredRole(requiredRole);
                        update(currentProp);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Changing type of configuration property " + currentProp.getName() + " from " + currentProp.getValue().getClass().getSimpleName() + " to "
                            + value.getClass().getSimpleName() + " is not allowed");
                    }
                }
            }
        }

        if (list.isEmpty() || !found) {
            logger.fine("Creating new property with name: " + name + " value: " + value + " comment: " + comment);
            persist(new SystemProperty(name, discriminator, value, comment, environment, deployer, instance, null));
        }
    }
}