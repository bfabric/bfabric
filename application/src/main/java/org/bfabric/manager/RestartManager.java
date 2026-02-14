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

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.interceptors.MeasureCalls;

@MeasureCalls
@Named
@ApplicationScoped
public class RestartManager implements Serializable {

    private static final long serialVersionUID = 1;

    private long downtime = 1;

    // Default minutes to restart.
    private long scheduleDiff = 5;

    private long scheduledRestartTime = 0;

    public void abortRestart() {
        scheduledRestartTime = 0;
    }

    public long getDowntime() {
        return downtime;
    }

    public String getDowntimeMessage() {
        return getDowntime() == 0 ? Constants.EMPTY_STRING : "Expected downtime in minutes is less than " + getDowntime() + ".";
    }

    public long getScheduleDiff() {
        return scheduleDiff;
    }

    public String getTimeLeftMessage() {
        return " minutes to shut down for maintenance. If you are logged in, please save your work and log out. " + getDowntimeMessage();
    }

    public long getTimeUntilRestart() {
        return (scheduledRestartTime - System.currentTimeMillis()) / 1000;
    }

    public boolean isRestartScheduled() {
        return scheduledRestartTime > 0;
    }

    public void scheduleRestart() {
        scheduledRestartTime = System.currentTimeMillis() + scheduleDiff * 60 * 1000;
    }

    public void setDowntime(long downtime) {
        this.downtime = downtime;
    }

    public void setScheduleDiff(long scheduleDiff) {
        this.scheduleDiff = scheduleDiff;
    }
}