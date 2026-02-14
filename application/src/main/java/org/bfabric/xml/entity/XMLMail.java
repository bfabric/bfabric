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

import org.bfabric.entity.Mail;

@XmlRootElement(name = "mail")
public class XMLMail extends XMLAbstractBaseEntity {

    @XmlElement
    private String message;

    @XmlElement
    private String parentclassname;

    @XmlElement
    private String parentid;

    @XmlElement
    private String recipients;

    @XmlElement
    private String subject;

    @XmlElement
    private String type;

    public XMLMail() {
    }

    public XMLMail(Mail entity, boolean reference) {
        super(entity, reference);
    }

    public XMLMail(Mail mail) {
        super(mail);
        if (mail != null) {
            if (mail.getMessage() != null) {
                setMessage(mail.getMessagePlainText());
            }
            if (mail.getParentClassName() != null) {
                setParentclassname(mail.getParentClassName());
            }
            setParentid(String.valueOf(mail.getParentId()));
            if (mail.getRecipientsAddressList() != null) {
                setRecipients(mail.getRecipientsAddressList());
            }
            if (mail.getSubject() != null) {
                setSubject(mail.getSubject());
            }
            if (mail.getType() != null) {
                setType(mail.getType().toString());
            }
        }
    }

    public String getMessage() {
        return message;
    }

    public String getParentclassname() {
        return parentclassname;
    }

    public String getParentid() {
        return parentid;
    }

    public String getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getType() {
        return type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setParentclassname(String parentclassname) {
        this.parentclassname = parentclassname;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setType(String type) {
        this.type = type;
    }
}