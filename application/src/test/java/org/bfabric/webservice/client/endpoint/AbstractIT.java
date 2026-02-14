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

package org.bfabric.webservice.client.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bfabric.util.StringHelper;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.xml.entity.XMLCustomAttribute;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

public class AbstractIT {

    public static final Long L1 = 1L;

    public static final Long L2 = 2L;

    public static final String S3 = StringHelper.generateString(3);

    public static final String S5 = StringHelper.generateString(5);

    public static final String COLOR = "#FFFFFF";

    public static final String COLOR_NEW = "#333333";

    public static final String GENERATED_NAME = StringHelper.generateString(5);

    public static final String GENERATED_NAME_NEW = StringHelper.generateString(3);

    protected static final String CHARGER_ID = "4634";

    protected static final String CONTAINER_ID = "403";

    protected static final String CONTAINER_ID_NEW = "31726";

    protected static final String SERVICE_ID = "2321";

    protected static final String SERVICE_ID_NEW = "2454";

    protected static final String TAX_TYPE_ID = "3";

    protected static final String DIVISION_ID = "4";

    protected static final String APPLICATION_ID = "1";

    protected static final String INSTITUTE_ID = "1";

    protected static final String ORGANIZATION_TYPE = "1";

    protected static final String ORGANIZATION_TYPE_NEW = "2";

    protected static final String STORAGE_ID = "1";

    protected static final String USER_ID = "595";

    protected static final String USER_NEW = "4634";

    protected static final List<String> USERS = Arrays.asList("2", "3");

    protected static final List<String> USERS_NEW = Collections.singletonList("3");

    private static final long ENTITY_ID_NON_EXISTING = Long.MAX_VALUE - 1;

    private static String beforeAllFailedErrorMessage = null;

    private static SoapClient soapClient;

    @AfterAll
    public static void afterAllCommon() {
        resetCommonResources();
    }

    @BeforeAll
    public static void beforeAllCommon() {
        resetCommonResources();
        setSoapClient(SoapClientFactory.get());
    }

    protected static List<XMLCustomAttribute> createCustomAttributes(int number) {
        List<XMLCustomAttribute> customAttributes = new ArrayList<>();
        for (int i = 1; i < number + 1; i++) {
            XMLCustomAttribute customAttribute = new XMLCustomAttribute();
            customAttribute.setName("custom attribute" + i);
            customAttribute.setValue("value" + i);
            customAttributes.add(customAttribute);
        }
        return customAttributes;
    }

    public static String getBeforeAllFailedErrorMessage() {
        return beforeAllFailedErrorMessage;
    }

    public static long getEntityIdNonExisting() {
        return ENTITY_ID_NON_EXISTING;
    }

    public static String getEntityIdNonExistingAsString() {
        return String.valueOf(ENTITY_ID_NON_EXISTING);
    }

    public static List<String> getListWithNonExistingEntityId() {
        return Collections.singletonList(String.valueOf(ENTITY_ID_NON_EXISTING));
    }

    public static SoapClient getSoapClient() {
        return soapClient;
    }

    public static void resetCommonResources() {
        setBeforeAllFailedErrorMessage(null);
        setSoapClient(null);
    }

    public static void setBeforeAllFailedErrorMessage(String beforeAllFailedErrorMessage) {
        AbstractIT.beforeAllFailedErrorMessage = beforeAllFailedErrorMessage;
    }

    public static void setSoapClient(SoapClient soapClient) {
        AbstractIT.soapClient = soapClient;
    }

    protected static void testCustomAttributes(List<XMLCustomAttribute> customAttributes) {
        for (int i = 0; i < customAttributes.size(); i++) {
            Assertions.assertEquals("custom attribute" + (i + 1), customAttributes.get(i).getName());
            Assertions.assertEquals("value" + (i + 1), customAttributes.get(i).getValue());
            Assertions.assertEquals("String", customAttributes.get(i).getType());
        }
    }
}
