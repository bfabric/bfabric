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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Attachment;
import org.bfabric.entity.Comment;

@XmlRootElement(name = "comment")
public class XMLComment extends XMLAbstractComment {

    @XmlElement
    private List<XMLAttachment> attachment = new ArrayList<>();

    @XmlElement
    private String discriminator;

    @XmlElement
    private String internal;

    @XmlElement
    private XMLParent parent;

    @XmlElement
    private String sendmail;

    @XmlElement
    private String subject;

    public XMLComment() {
    }

    public XMLComment(Comment entity, boolean reference) {
        super(entity, reference);
    }

    public <T extends Comment> XMLComment(T entity) {
        super(entity);
        if (entity != null) {
            if (entity.getSubject() != null) {
                setSubject(entity.getSubject());
            }
            if (entity.getAttachments() != null) {
                for (Attachment aAttachment : entity.getAttachments()) {
                    getAttachment().add(new XMLAttachment(aAttachment, true));
                }
            }
            setInternal(Boolean.toString(entity.isInternal()));
            if (entity.getParent() != null) {
                setParent(new XMLParent(entity.getParent()));
            }
            setSendmail(Boolean.toString(entity.isSendMail()));
            if (entity.getDiscriminator() != null) {
                setDiscriminator(entity.getDiscriminator().toString());
            }
        }
    }

    public List<XMLAttachment> getAttachment() {
        return attachment;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getInternal() {
        return internal;
    }

    public XMLParent getParent() {
        return parent;
    }

    public String getSendmail() {
        return sendmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setAttachment(List<XMLAttachment> attachment) {
        this.attachment = attachment;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public void setParent(XMLParent parent) {
        this.parent = parent;
    }

    public void setSendmail(String sendmail) {
        this.sendmail = sendmail;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}