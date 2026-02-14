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

package org.bfabric.webservice.request.parameter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveComment extends XMLRequestParameterSaveAbstractEntity {

    @XmlElement
    private List<XMLRequestParameterSaveUploadedFile> attachment;

    @XmlElement(required = true)
    private String discriminator;

    @XmlElement(required = true)
    private String internal;

    @XmlElement(required = true)
    private String parentclassname;

    @XmlElement(required = true)
    private String parentid;

    @XmlElement(required = true)
    private String sendmail;

    @XmlElement
    private List<String> sendmailusergroupid;

    @XmlElement
    private String subject;

    @XmlElement
    private String text;

    public List<XMLRequestParameterSaveUploadedFile> getAttachment() {
        return attachment;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getInternal() {
        return internal;
    }

    public String getParentclassname() {
        return parentclassname;
    }

    public String getParentid() {
        return parentid;
    }

    public String getSendmail() {
        return sendmail;
    }

    public List<String> getSendmailusergroupid() {
        return sendmailusergroupid;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public void setAttachment(List<XMLRequestParameterSaveUploadedFile> attachment) {
        this.attachment = attachment;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public void setParentclassname(String parentclassname) {
        this.parentclassname = parentclassname;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public void setSendmail(String sendmail) {
        this.sendmail = sendmail;
    }

    public void setSendmailusergroupid(List<String> sendmailusergroupid) {
        this.sendmailusergroupid = sendmailusergroupid;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }
}
