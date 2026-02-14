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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.util.StringHelper;

@MappedSuperclass
public abstract class AbstractComment extends AbstractBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "TEXT")
    @XmlElement
    private String comment;

    public AbstractComment() {
    }

    public String getComment() {
        return comment;
    }

    public String getCommentSafeHtml() {
        return StringHelper.getSafeHtml(getComment());
    }

    public String getCommentTrimmed() {
        return StringHelper.removeDoubleEmptyLines(getComment());
    }

    public String getCommentTrunc(int maxLength) {
        return StringHelper.truncate(getComment(), maxLength);
    }

    public String getIndexedComment() {
        return getComment() != null && !getComment().isEmpty() ? getComment() : Constants.EMPTY_STRING;
    }

    public void setComment(String comment) {
        this.comment = StringHelper.trimCommentText(comment);
    }
}
