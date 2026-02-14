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

import org.bfabric.entity.CommentTemplate;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCommentTemplate;

public class MFCommentTemplate extends AbstractMF {

    private final CommentTemplate commentTemplate;

    private final XMLRequestParameterSaveCommentTemplate xmlRequestSaveCommentTemplate;

    public MFCommentTemplate(CommentTemplate commentTemplate, XMLRequestParameterSaveCommentTemplate xmlRequestSaveCommentTemplate) {
        this.commentTemplate = commentTemplate;
        this.xmlRequestSaveCommentTemplate = xmlRequestSaveCommentTemplate;
    }

    @Override
    public synchronized void apply() throws Exception {
        getCommentTemplate().setName(getName());
        getCommentTemplate().setContent(getContent());
        getCommentTemplate().setDescription(getDescription());
    }

    public CommentTemplate getCommentTemplate() {
        return commentTemplate;
    }

    public String getContent() {
        if (getXmlRequestSaveCommentTemplate().getContent() != null) {
            return getXmlRequestSaveCommentTemplate().getContent();
        }
        return getCommentTemplate().getContent();
    }

    public String getDescription() {
        if (getXmlRequestSaveCommentTemplate().getDescription() != null) {
            return getXmlRequestSaveCommentTemplate().getDescription();
        }
        return getCommentTemplate().getDescription();
    }

    public String getName() {
        if (getXmlRequestSaveCommentTemplate().getName() != null) {
            return getXmlRequestSaveCommentTemplate().getName();
        }
        return getCommentTemplate().getName();
    }

    public XMLRequestParameterSaveCommentTemplate getXmlRequestSaveCommentTemplate() {
        return xmlRequestSaveCommentTemplate;
    }
}
