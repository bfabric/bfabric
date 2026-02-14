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

import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.Resource;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.util.ClassHelper;

public enum ExternalJobClientClassEnum {

    ORDER(Order.class),
    PROJECT(Project.class),
    CONTAINER(Container.class),
    RESOURCE(Resource.class),
    RESOURCE_BASKET(ResourceBasket.class),
    USER(User.class),
    WORKUNIT(Workunit.class);

    private final Class<? extends AbstractBaseEntity> clientClass;

    ExternalJobClientClassEnum(Class<? extends AbstractBaseEntity> clientClass) {
        this.clientClass = clientClass;
    }

    public static ExternalJobClientClassEnum getExternalJobClientClassEnum(String className) {
        ExternalJobClientClassEnum ret = null;
        if (className != null) {
            for (ExternalJobClientClassEnum entityClass : values()) {
                if (entityClass.getClientClassName().equals(className)) {
                    ret = entityClass;
                    break;
                }
            }
        }
        return ret;
    }

    public Class<? extends AbstractBaseEntity> getClientClass() {
        return clientClass;
    }

    public String getClientClassName() {
        return ClassHelper.getTrimmedClassName(clientClass);
    }

    public String getClientClassRequestParameterId() {
        return Project.class.equals(clientClass) || Order.class.equals(clientClass) ? ClassHelper.getRequestParameterId(Container.class) : ClassHelper.getRequestParameterId(clientClass);
    }
}
