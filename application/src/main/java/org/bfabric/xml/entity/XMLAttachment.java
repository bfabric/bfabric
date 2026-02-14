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

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Attachment;
import org.bfabric.util.StringHelper;

@XmlRootElement(name = "attachment")
public class XMLAttachment extends XMLAbstractResource {

    @XmlElement
    private String base64;

    @XmlElement
    private XMLComment comment;

    public XMLAttachment() {
    }

    public XMLAttachment(Attachment entity, boolean reference) {
        super(entity, reference);
    }

    public XMLAttachment(Attachment attachment) {
        super(attachment);
        if (attachment != null) {
            if (attachment.isAvailable() && attachment.getRelativePath() != null) {
                File file = new File(attachment.getRelativePath());
                if (file.exists() && file.canRead()) {
                    base64 = StringHelper.encodeBase64(file);
                }
            }
            if (attachment.getComment() != null) {
                comment = new XMLComment(attachment.getComment(), true);
            }
        }
    }

    public String getBase64() {
        return base64;
    }

    public XMLComment getComment() {
        return comment;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public void setComment(XMLComment comment) {
        this.comment = comment;
    }

}
