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

import java.io.File;
import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.security.auth.message.AuthException;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.primefaces.model.DefaultStreamedContent;

@Entity
@XmlRootElement
public class Attachment extends AbstractResource implements NotEntityLoggable, ShowScreen {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentId")
    @XmlIDREF
    private Comment comment;

    public Attachment() {
    }

    public Attachment(Comment comment, BfabricUploadedFile uploadedFile) {
        setUploadedFile(uploadedFile);
        setRelativePath(comment.getRelativeRepositoryPath() + File.separator + getName());
        setStorage(RepositoryHelper.getLocalStorage(comment.isInternal()));
        setComment(comment);
    }

    public Attachment(Project project, BfabricUploadedFile uploadedFile, String parentFolder) {
        setUploadedFile(uploadedFile);
        setRelativePath(project.getRelativeRepositoryPath() + File.separator + parentFolder + File.separator + getName());
        setStorage(RepositoryHelper.getLocalStorage(false));
    }

    public Comment getComment() {
        return comment;
    }

    @Override
    public DefaultStreamedContent getDefaultStreamedContent() throws IOException, AuthException {
        return super.getDefaultStreamedContent();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean isAvailable() {
        return super.isAvailable();
    }

    @Override
    public boolean isCreatable() {
        return getComment() != null && getComment().isUpdatableWS();
    }

    @Override
    public boolean isDeletableWS() {
        return getComment() != null && getComment().isUpdatableWS() && (StringHelper.isNotEmpty(getComment().getComment()) || getComment().getAttachments().size() > 1);
    }

    @Override
    public boolean isStreamedContent() {
        return super.isStreamedContent();
    }

    @Override
    public boolean isUpdatableWS() {
        return false;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}