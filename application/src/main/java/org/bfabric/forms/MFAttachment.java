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

package org.bfabric.forms;

import org.bfabric.entity.Attachment;
import org.bfabric.entity.Comment;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveAttachment;

public class MFAttachment extends AbstractMF {

    private final Attachment attachment;

    private final XMLRequestParameterSaveAttachment xmlRequestSaveAttachment;

    public MFAttachment(Attachment attachment, XMLRequestParameterSaveAttachment xmlRequestSaveAttachment) {
        this.attachment = attachment;
        this.xmlRequestSaveAttachment = xmlRequestSaveAttachment;
    }

    @Override
    public synchronized void apply() throws Exception {
        getAttachment().setName(getName());
        getAttachment().setUploadedFile(getUploadedFile());
        getAttachment().setComment(getComment());
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public Comment getComment() throws Exception {
        return (Comment) fetch(Comment.class, MFHelper.positiveLongValueOf("commentid", getXmlRequestSaveAttachment().getCommentid()));
    }

    public String getName() {
        return getXmlRequestSaveAttachment().getName();
    }

    public BfabricUploadedFile getUploadedFile() throws Exception {
        MFHelper.checkNotNull("base64", getXmlRequestSaveAttachment().getBase64());
        return decodeAndCreateFile(getXmlRequestSaveAttachment().getBase64(), getName());
    }

    public XMLRequestParameterSaveAttachment getXmlRequestSaveAttachment() {
        return xmlRequestSaveAttachment;
    }
}
