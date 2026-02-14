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

package org.bfabric.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.bfabric.entity.Configuration;
import org.bfabric.entity.User;

public class MailHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private List<InternetAddress> bcc = new ArrayList<>();

    private User cachedUser;

    private List<InternetAddress> cc = new ArrayList<>();

    private Configuration configuration;

    private InternetAddress from;

    private Map<String, Object> input = new HashMap<>();

    private String reportYear;

    private List<InternetAddress> to = new ArrayList<>();

    public MailHelper() {
    }

    public List<InternetAddress> getBcc() {
        return bcc;
    }

    public User getCachedUser() {
        return cachedUser;
    }

    public List<InternetAddress> getCc() {
        return cc;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public InternetAddress getFrom() {
        return from;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public String getReportYear() {
        return reportYear;
    }

    public List<InternetAddress> getTo() {
        return to;
    }

    public boolean isRecipientsNotEmpty() {
        return !getTo().isEmpty() || !getCc().isEmpty() || !getBcc().isEmpty();
    }

    public void setBcc(List<InternetAddress> bcc) {
        this.bcc = bcc;
    }

    public void setCachedUser(User cachedUser) {
        this.cachedUser = cachedUser;
    }

    public void setCc(List<InternetAddress> cc) {
        this.cc = cc;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setFrom(InternetAddress from) {
        this.from = from;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public void setReportYear(String reportYear) {
        this.reportYear = reportYear;
        getInput().put("reportYear", reportYear);
    }

    public void setTo(List<InternetAddress> to) {
        this.to = to;
    }
}
