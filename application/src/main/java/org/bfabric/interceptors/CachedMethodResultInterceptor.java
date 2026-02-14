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

package org.bfabric.interceptors;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.commons.lang3.StringUtils;

@CachedMethodResult
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CachedMethodResultInterceptor implements Serializable {

    private static final long serialVersionUID = 1;

    private final Map<String, Object> cachedMethods = new HashMap<>();

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        Method m = ic.getMethod();
        Object[] parameters = ic.getParameters();
        String key = CachedMethodResultInterceptor.class.getName() + "#" + ic.getTarget() + "/" + m.getName() + "(" + StringUtils.join(parameters, ",") + ")";
        if (!cachedMethods.containsKey(key)) {
            Object result = ic.proceed();
            cachedMethods.put(key, result);
            return result;
        }
        return cachedMethods.get(key);
    }
}