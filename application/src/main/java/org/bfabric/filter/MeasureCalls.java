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

package org.bfabric.filter;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

/**
 * This class is used for the @MeasureCalls annotation.
 */
public class MeasureCalls implements Comparable<MeasureCalls> {

    private final Method method;

    private int calls = 1;

    private long dt;

    public MeasureCalls(Method method, long dt) {
        this.method = method;
        this.dt = dt;
    }

    public void anotherCall(long callDt) {
        this.dt += callDt;
        calls++;
    }

    @Override
    public int compareTo(MeasureCalls o) {
        Long thisDT = dt;
        Long otherDT = o.dt;
        return thisDT.compareTo(otherDT);
    }

    @Override
    public boolean equals(Object o) {
        boolean ret = false;
        if (o instanceof MeasureCalls) {
            Long thisDT = dt;
            MeasureCalls timedInvocation = (MeasureCalls) o;
            Long otherDT = timedInvocation.dt;
            ret = thisDT.equals(otherDT);
        }
        return ret;
    }

    public int getCalls() {
        return calls;
    }

    public long getDt() {
        return dt;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    @Override
    public String toString() {
        String className = method.getDeclaringClass().getName();
        String duration = StringUtils.leftPad((dt / 1e6) + " ms", 15);
        String nCallStr = StringUtils.leftPad(String.valueOf(calls), 4);
        return duration + nCallStr + "   " + className.substring(method.getDeclaringClass().getPackage().getName().length() + 1) + "." + method.getName() + "()";
    }
}
