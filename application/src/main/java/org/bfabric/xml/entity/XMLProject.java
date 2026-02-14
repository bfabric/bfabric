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

package org.bfabric.xml.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Container;
import org.bfabric.entity.Project;
import org.bfabric.util.DateUtils;

@XmlRootElement(name = "project")
public class XMLProject extends XMLContainer {

    @XmlElement
    private Integer countorders;

    @XmlElement
    private String doicreated;

    @XmlElement
    private String doicreatedby;

    @XmlElement
    private String enddate;

    @XmlElement
    private String extensionreport1approved;

    @XmlElement
    private String extensionreport1remindersent;

    @XmlElement
    private String extensionreport2approved;

    @XmlElement
    private String extensionreport2remindersent;

    @XmlElement
    private String extensionreport3approved;

    @XmlElement
    private String extensionreport3remindersent;

    @XmlElement
    private String finaldecisiondate;

    @XmlElement
    private String finishannounceddate;

    @XmlElement
    private String finishdate;

    @XmlElement
    private String privateannounceddate;

    @XmlElement
    private String privatedate;

    @XmlElement
    private String publishdate;

    @XmlElement
    private String publishgranted;

    @XmlElement
    private String remindercomment;

    @XmlElement
    private String reminderdate;

    @XmlElement
    private String startdate;

    @XmlElement
    private String summary;

    public XMLProject() {
    }

    public XMLProject(Container entity, boolean reference) {
        super(entity, reference);
    }

    public XMLProject(Project entity) {
        super(entity);
        if (entity != null) {
            if (entity.getDoiCreated() != null) {
                setDoicreated(DateUtils.getDateAsFormattedString(entity.getDoiCreated()));
            }
            if (entity.getDoiCreatedBy() != null) {
                setDoicreatedby(entity.getDoiCreatedBy());
            }
            if (entity.getEndDate() != null) {
                setEnddate(DateUtils.getDateAsFormattedString(entity.getEndDate()));
            }
            setExtensionreport1approved(Boolean.toString(entity.isExtensionReport1Approved()));
            setExtensionreport1remindersent(Boolean.toString(entity.isExtensionReport1ReminderSent()));
            setExtensionreport2approved(Boolean.toString(entity.isExtensionReport2Approved()));
            setExtensionreport2remindersent(Boolean.toString(entity.isExtensionReport2ReminderSent()));
            setExtensionreport3approved(Boolean.toString(entity.isExtensionReport3Approved()));
            setExtensionreport3remindersent(Boolean.toString(entity.isExtensionReport3ReminderSent()));
            if (entity.getFinalDecisionDate() != null) {
                setFinaldecisiondate(DateUtils.getDateAsFormattedString(entity.getFinalDecisionDate()));
            }
            if (entity.getFinishAnnouncedDate() != null) {
                setFinishannounceddate(DateUtils.getDateAsFormattedString(entity.getFinishAnnouncedDate()));
            }
            if (entity.getFinishDate() != null) {
                setFinishdate(DateUtils.getDateAsFormattedString(entity.getFinishDate()));
            }
            if (entity.getPrivateAnnouncedDate() != null) {
                setPrivateannounceddate(DateUtils.getDateAsFormattedString(entity.getPrivateAnnouncedDate()));
            }
            if (entity.getPrivateDate() != null) {
                setPrivatedate(DateUtils.getDateAsFormattedString(entity.getPrivateDate()));
            }
            if (entity.getPublishDate() != null) {
                setPublishdate(DateUtils.getDateAsFormattedString(entity.getPublishDate()));
            }
            if (entity.getPublishGrantedDate() != null) {
                setPublishgranted(DateUtils.getDateAsFormattedString(entity.getPublishGrantedDate()));
            }
            if (entity.getReminderComment() != null) {
                setRemindercomment(entity.getReminderComment());
            }
            if (entity.getReminderDate() != null) {
                setReminderdate(DateUtils.getDateAsFormattedString(entity.getReminderDate()));
            }
            if (entity.getStartDate() != null) {
                setStartdate(DateUtils.getDateAsFormattedString(entity.getStartDate()));
            }
            if (entity.getSummary() != null) {
                setSummary(entity.getSummary());
            }
            if (entity.getOrders() != null) {
                setCountorders(entity.getOrders().size());
            }
        }
    }

    public Integer getCountorders() {
        return countorders;
    }

    public String getDoicreated() {
        return doicreated;
    }

    public String getDoicreatedby() {
        return doicreatedby;
    }

    public String getEnddate() {
        return enddate;
    }

    public String getExtensionreport1approved() {
        return extensionreport1approved;
    }

    public String getExtensionreport1remindersent() {
        return extensionreport1remindersent;
    }

    public String getExtensionreport2approved() {
        return extensionreport2approved;
    }

    public String getExtensionreport2remindersent() {
        return extensionreport2remindersent;
    }

    public String getExtensionreport3approved() {
        return extensionreport3approved;
    }

    public String getExtensionreport3remindersent() {
        return extensionreport3remindersent;
    }

    public String getFinaldecisiondate() {
        return finaldecisiondate;
    }

    public String getFinishannounceddate() {
        return finishannounceddate;
    }

    public String getFinishdate() {
        return finishdate;
    }

    public String getPrivateannounceddate() {
        return privateannounceddate;
    }

    public String getPrivatedate() {
        return privatedate;
    }

    public String getPublishdate() {
        return publishdate;
    }

    public String getPublishgranted() {
        return publishgranted;
    }

    public String getRemindercomment() {
        return remindercomment;
    }

    public String getReminderdate() {
        return reminderdate;
    }

    public String getStartdate() {
        return startdate;
    }

    public String getSummary() {
        return summary;
    }

    public void setCountorders(Integer countorders) {
        this.countorders = countorders;
    }

    public void setDoicreated(String doicreated) {
        this.doicreated = doicreated;
    }

    public void setDoicreatedby(String doicreatedby) {
        this.doicreatedby = doicreatedby;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public void setExtensionreport1approved(String extensionreport1approved) {
        this.extensionreport1approved = extensionreport1approved;
    }

    public void setExtensionreport1remindersent(String extensionreport1remindersent) {
        this.extensionreport1remindersent = extensionreport1remindersent;
    }

    public void setExtensionreport2approved(String extensionreport2approved) {
        this.extensionreport2approved = extensionreport2approved;
    }

    public void setExtensionreport2remindersent(String extensionreport2remindersent) {
        this.extensionreport2remindersent = extensionreport2remindersent;
    }

    public void setExtensionreport3approved(String extensionreport3approved) {
        this.extensionreport3approved = extensionreport3approved;
    }

    public void setExtensionreport3remindersent(String extensionreport3remindersent) {
        this.extensionreport3remindersent = extensionreport3remindersent;
    }

    public void setFinaldecisiondate(String finaldecisiondate) {
        this.finaldecisiondate = finaldecisiondate;
    }

    public void setFinishannounceddate(String finishannounceddate) {
        this.finishannounceddate = finishannounceddate;
    }

    public void setFinishdate(String finishdate) {
        this.finishdate = finishdate;
    }

    public void setPrivateannounceddate(String privateannounceddate) {
        this.privateannounceddate = privateannounceddate;
    }

    public void setPrivatedate(String privatedate) {
        this.privatedate = privatedate;
    }

    public void setPublishdate(String publishdate) {
        this.publishdate = publishdate;
    }

    public void setPublishgranted(String publishgranted) {
        this.publishgranted = publishgranted;
    }

    public void setRemindercomment(String remindercomment) {
        this.remindercomment = remindercomment;
    }

    public void setReminderdate(String reminderdate) {
        this.reminderdate = reminderdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
