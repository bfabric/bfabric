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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.bfabric.filter.MeasureCalls;

@org.bfabric.interceptors.MeasureCalls
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class MeasureCallsInterceptor implements Serializable {

    public static final CallChain callChain = new CallChain();

    private static final long serialVersionUID = 1;

    @AroundInvoke
    public Object timeCall(InvocationContext invocation) throws Exception {
        long t0 = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            long dt = System.nanoTime() - t0;
            callChain.addInvocation(invocation, dt);
        }
    }

    public static class CallChain extends ThreadLocal<Map<Method, MeasureCalls>> {

        public void addInvocation(InvocationContext invocation, long dt) {
            Map<Method, MeasureCalls> invocations = get();
            Method method = invocation.getMethod();
            if (!invocations.containsKey(method)) {
                invocations.put(method, new MeasureCalls(invocation.getMethod(), dt));
            } else {
                MeasureCalls timedInvocation = invocations.get(method);
                timedInvocation.anotherCall(dt);
            }
        }

        @Override
        protected Map<Method, MeasureCalls> initialValue() {
            return new HashMap<>();
        }

        public int totalNumberOfInvocations() {
            Map<Method, MeasureCalls> invocations = get();
            Collection<MeasureCalls> timedInvocationCollection = invocations.values();
            int totCalls = 0;
            for (MeasureCalls invocation : timedInvocationCollection) {
                totCalls += invocation.getCalls();
            }
            return totCalls;
        }
    }

}
