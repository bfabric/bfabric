/*
 *
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

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import net.sf.ehcache.util.FindBugsSuppressWarnings;
import org.bfabric.Messages;
import org.bfabric.entity.Configuration;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.util.ConfigurationHelper;

@MeasureCalls
@Named
@SessionScoped
public class SessionTimeoutManager extends AbstractHttpServletManager {

    private static final long serialVersionUID = 1;

    private long lastAccessedTime;

    private long maxInactiveInterval;

    private volatile Boolean timedout;

    private long warningTime;

    private static long getTime() {
        return (long) Math.rint((double) System.currentTimeMillis() / 1000);
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public long getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public long getTimeUntilSessionTimeout() {
        return isTimedout() ? 0 : getLastAccessedTime() + getMaxInactiveInterval() - getTime();
    }

    public long getTimerTimeout() {
        long timeUntilSessionTimeout = getTimeUntilSessionTimeout();
        return timeUntilSessionTimeout <= getWarningTime() ? timeUntilSessionTimeout : timeUntilSessionTimeout - getWarningTime();
    }

    public String getWarningMessage() {
        if (isTimeoutWarning()) {
            return Messages.get("sessionInactivityWarning");
        }
        return Messages.get("sessionExpiredWarning");
    }

    public long getWarningTime() {
        return warningTime;
    }

    @PostConstruct
    public void init() {
        setMaxInactiveInterval(getHttpSession().getMaxInactiveInterval());
        setLastAccessedTime(getTime());
        setWarningTime(ConfigurationHelper.getConfiguration() != null ? ConfigurationHelper.getConfiguration().getSessionTimeoutWarningTime() : new Configuration()
            .getSessionTimeoutWarningTime());
    }

    public boolean isTimedout() {
        return timedout != null && timedout;
    }

    public boolean isTimeoutWarning() {
        return !isTimedout() && getTimeUntilSessionTimeout() <= getWarningTime();
    }

    @FindBugsSuppressWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void keepSessionAlive() {
        getHttpServletRequest().getSession();
        setLastAccessedTime();
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public void setLastAccessedTime() {
        setLastAccessedTime(getTime());
    }

    public void setMaxInactiveInterval(long maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public void setTimedout(boolean timedout) {
        this.timedout = timedout;
    }

    public void setWarningTime(long warningTime) {
        this.warningTime = warningTime;
    }

    public void timeout() {
        if (getTimeUntilSessionTimeout() <= 0 && !isTimedout()) {
            setTimedout(true);
            getHttpServletRequest().getSession(false).setMaxInactiveInterval(1);
        }
    }
}
