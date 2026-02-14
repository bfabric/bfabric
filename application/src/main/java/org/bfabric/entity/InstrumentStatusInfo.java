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

package org.bfabric.entity;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.util.StringHelper;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
public class InstrumentStatusInfo implements Serializable {

    private static final long serialVersionUID = 1;

    @XmlElement
    private boolean available = false;

    @NotNull
    @XmlElement
    private Duration availableTime = Duration.ofNanos(0);

    @Transient
    private String availableTimeAsText;

    @NotNull
    @XmlElement
    private Duration availableTimeTotal = Duration.ofNanos(0);

    @XmlElement
    private boolean bookable = false;

    @NotNull
    @XmlElement
    private Duration bookableTime = Duration.ofNanos(0);

    @Transient
    private String bookableTimeAsText;

    @NotNull
    @XmlElement
    private Duration bookableTimeTotal = Duration.ofNanos(0);

    @XmlElement
    private boolean runEnabled = false;

    @NotNull
    @XmlElement
    private Duration runEnabledTime = Duration.ofNanos(0);

    @Transient
    private String runEnabledTimeAsText;

    @NotNull
    @XmlElement
    private Duration runEnabledTimeTotal = Duration.ofNanos(0);

    @Size(max = 512)
    @XmlElement
    private String statusComment;

    @NotNull
    @XmlElement
    private boolean up = false;

    @NotNull
    @XmlElement
    private Duration upTime = Duration.ofNanos(0);

    @Transient
    private String upTimeAsText;

    @NotNull
    @XmlElement
    private Duration upTimeTotal = Duration.ofNanos(0);

    @XmlElement
    private boolean userBookable = false;

    @NotNull
    @XmlElement
    private Duration userBookableTime = Duration.ofNanos(0);

    @Transient
    private String userBookableTimeAsText;

    @NotNull
    @XmlElement
    private Duration userBookableTimeTotal = Duration.ofNanos(0);

    @XmlElement
    private boolean userVisible = false;

    @NotNull
    @XmlElement
    private Duration userVisibleTime = Duration.ofNanos(0);

    @Transient
    private String userVisibleTimeAsText;

    @NotNull
    @XmlElement
    private Duration userVisibleTimeTotal = Duration.ofNanos(0);

    public InstrumentStatusInfo() {
    }

    public InstrumentStatusInfo(Instrument instrument) {
        setStatus(instrument);
    }

    public Duration getAvailableTime() {
        return availableTime;
    }

    public String getAvailableTimeAsText() {
        if (availableTimeAsText == null) {
            availableTimeAsText = !isAvailable() && !getAvailableTime().isZero() ? StringHelper.getFormattedDuration(getAvailableTime()) : null;
        }
        return availableTimeAsText;
    }

    public Duration getAvailableTimeTotal() {
        return availableTimeTotal;
    }

    public Duration getBookableTime() {
        return bookableTime;
    }

    public String getBookableTimeAsText() {
        if (bookableTimeAsText == null) {
            bookableTimeAsText = !isBookable() && !getBookableTime().isZero() ? StringHelper.getFormattedDuration(getBookableTime()) : null;
        }
        return bookableTimeAsText;
    }

    public Duration getBookableTimeTotal() {
        return bookableTimeTotal;
    }

    public Duration getRunEnabledTime() {
        return runEnabledTime;
    }

    public String getRunEnabledTimeAsText() {
        if (runEnabledTimeAsText == null) {
            runEnabledTimeAsText = !isRunEnabled() && !getRunEnabledTime().isZero() ? StringHelper.getFormattedDuration(getRunEnabledTime()) : null;
        }
        return runEnabledTimeAsText;
    }

    public Duration getRunEnabledTimeTotal() {
        return runEnabledTimeTotal;
    }

    public String getStatusComment() {
        return statusComment;
    }

    public Duration getUpTime() {
        return upTime;
    }

    public String getUpTimeAsText() {
        if (upTimeAsText == null) {
            upTimeAsText = !isUp() && !getUpTime().isZero() ? StringHelper.getFormattedDuration(getUpTime()) : null;
        }
        return upTimeAsText;
    }

    public Duration getUpTimeTotal() {
        return upTimeTotal;
    }

    public Duration getUserBookableTime() {
        return userBookableTime;
    }

    public String getUserBookableTimeAsText() {
        if (userBookableTimeAsText == null) {
            userBookableTimeAsText = !isUserBookable() && !getUserBookableTime().isZero() ? StringHelper.getFormattedDuration(getUserBookableTime()) : null;
        }
        return userBookableTimeAsText;
    }

    public Duration getUserBookableTimeTotal() {
        return userBookableTimeTotal;
    }

    public Duration getUserVisibleTime() {
        return userVisibleTime;
    }

    public String getUserVisibleTimeAsText() {
        if (userVisibleTimeAsText == null) {
            userVisibleTimeAsText = !isUserVisible() && !getUserVisibleTime().isZero() ? StringHelper.getFormattedDuration(getUserVisibleTime()) : null;
        }
        return userVisibleTimeAsText;
    }

    public Duration getUserVisibleTimeTotal() {
        return userVisibleTimeTotal;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isBookable() {
        return bookable;
    }

    public boolean isRunEnabled() {
        return runEnabled;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isUserBookable() {
        return userBookable;
    }

    public boolean isUserVisible() {
        return userVisible;
    }

    public void setAvailable(boolean available) {
        if (this.available != available) {
            this.available = available;
            if (!available) {
                setUp(false);
                setBookable(false);
            }
        }
    }

    public void setAvailableTime(Duration availableTime) {
        this.availableTime = availableTime;
    }

    public void setAvailableTimeTotal(Duration availableTimeTotal) {
        this.availableTimeTotal = availableTimeTotal;
    }

    public void setBookable(boolean bookable) {
        if (this.bookable != bookable) {
            this.bookable = bookable;
            if (bookable) {
                setAvailable(true);
            }
            if (!bookable) {
                setUserVisible(false);
            }
        }
    }

    public void setBookableTime(Duration bookableTime) {
        this.bookableTime = bookableTime;
    }

    public void setBookableTimeTotal(Duration bookableTimeTotal) {
        this.bookableTimeTotal = bookableTimeTotal;
    }

    public void setRunEnabled(boolean runEnabled) {
        this.runEnabled = runEnabled;
    }

    public void setRunEnabledTime(Duration runEnabledTime) {
        this.runEnabledTime = runEnabledTime;
    }

    public void setRunEnabledTimeTotal(Duration runEnabledTimeTotal) {
        this.runEnabledTimeTotal = runEnabledTimeTotal;
    }

    public void setStatus(Instrument instrument) {
        if (instrument != null && instrument.getInstrumentStatusInfo() != null) {
            InstrumentStatusInfo newStatusInfo = instrument.getInstrumentStatusInfo();
            setAvailable(newStatusInfo.isAvailable());
            setUp(newStatusInfo.isUp());
            setBookable(newStatusInfo.isBookable());
            setUserVisible(newStatusInfo.isUserVisible());
            setUserBookable(newStatusInfo.isUserBookable());
            setRunEnabled(newStatusInfo.isRunEnabled());
            setStatusComment(newStatusInfo.getStatusComment());

            InstrumentStatus lastStatus = instrument.getLastStatus();
            if (lastStatus != null) {
                Duration statusDuration = Duration.ZERO;
                if (instrument.getStatusModified() != null) {
                    statusDuration = Duration.between(instrument.getStatusModified(), Instant.now());
                }
                InstrumentStatusInfo lastStatusInfo = lastStatus.getInstrumentStatusInfo();
                if (lastStatusInfo.isAvailable()) {
                    setAvailableTime(lastStatusInfo.getAvailableTime().plus(statusDuration));
                    setAvailableTimeTotal(lastStatusInfo.getAvailableTimeTotal().plus(statusDuration));
                } else {
                    setAvailableTime(Duration.ofNanos(0));
                    setAvailableTimeTotal(lastStatusInfo.getAvailableTimeTotal());
                }
                if (lastStatusInfo.isUp()) {
                    setUpTime(lastStatusInfo.getUpTime().plus(statusDuration));
                    setUpTimeTotal(lastStatusInfo.getUpTimeTotal().plus(statusDuration));
                } else {
                    setUpTime(Duration.ofNanos(0));
                    setUpTimeTotal(lastStatusInfo.getUpTimeTotal());
                }
                if (lastStatusInfo.isBookable()) {
                    setBookableTime(lastStatusInfo.getBookableTime().plus(statusDuration));
                    setBookableTimeTotal(lastStatusInfo.getBookableTimeTotal().plus(statusDuration));
                } else {
                    setBookableTime(Duration.ofNanos(0));
                    setBookableTimeTotal(lastStatusInfo.getBookableTimeTotal());
                }
                if (lastStatusInfo.isUserBookable()) {
                    setUserBookableTime(lastStatusInfo.getUserBookableTime().plus(statusDuration));
                    setUserBookableTimeTotal(lastStatusInfo.getUserBookableTimeTotal().plus(statusDuration));
                } else {
                    setUserBookableTime(Duration.ofNanos(0));
                    setUserBookableTimeTotal(lastStatusInfo.getUserBookableTimeTotal());
                }
                if (lastStatusInfo.isRunEnabled()) {
                    setRunEnabledTime(lastStatusInfo.getRunEnabledTime().plus(statusDuration));
                    setRunEnabledTimeTotal(lastStatusInfo.getRunEnabledTimeTotal().plus(statusDuration));
                } else {
                    setRunEnabledTime(Duration.ofNanos(0));
                    setRunEnabledTimeTotal(lastStatusInfo.getRunEnabledTimeTotal());
                }
                if (lastStatusInfo.isUserVisible()) {
                    setUserVisibleTime(lastStatusInfo.getUserVisibleTime().plus(statusDuration));
                    setUserVisibleTimeTotal(lastStatusInfo.getUserVisibleTimeTotal().plus(statusDuration));
                } else {
                    setUserVisibleTime(Duration.ofNanos(0));
                    setUserVisibleTimeTotal(lastStatusInfo.getUserVisibleTimeTotal());
                }
            }
        }
    }

    public void setStatusComment(String statusComment) {
        this.statusComment = StringHelper.format(statusComment);
    }

    public void setUp(boolean up) {
        if (this.up != up) {
            this.up = up;
            if (up) {
                setAvailable(true);
            }
        }
    }

    public void setUpTime(Duration upTime) {
        this.upTime = upTime;
    }

    public void setUpTimeTotal(Duration upTimeTotal) {
        this.upTimeTotal = upTimeTotal;
    }

    public void setUserBookable(boolean userBookable) {
        this.userBookable = userBookable;
    }

    public void setUserBookableTime(Duration userBookableTime) {
        this.userBookableTime = userBookableTime;
    }

    public void setUserBookableTimeTotal(Duration userBookableTimeTotal) {
        this.userBookableTimeTotal = userBookableTimeTotal;
    }

    public void setUserVisible(boolean userVisible) {
        this.userVisible = userVisible;
        if (!userVisible) {
            setUserBookable(false);
        }
    }

    public void setUserVisibleTime(Duration userVisibleTime) {
        this.userVisibleTime = userVisibleTime;
    }

    public void setUserVisibleTimeTotal(Duration userVisibleTimeTotal) {
        this.userVisibleTimeTotal = userVisibleTimeTotal;
    }
}