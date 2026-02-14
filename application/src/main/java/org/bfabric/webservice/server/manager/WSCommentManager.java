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

package org.bfabric.webservice.server.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.bfabric.entity.Comment;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFComment;
import org.bfabric.forms.MFHelper;
import org.bfabric.service.CommentService;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveAbstractEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveComment;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUploadedFile;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLComment;

public class WSCommentManager extends AbstractWSEntityManager<Comment, XMLComment> {

    private final Set<BfabricUploadedFile> uploadedFiles = new HashSet<>();

    @Inject
    private CommentService commentService;

    @Override
    protected XMLComment createNewXmlEntity(Comment comment) {
        return new XMLComment(comment);
    }

    @Override
    protected Class<Comment> getEntityClass() throws Exception {
        if (getXmlRequestSaveEntity() != null && getXmlRequestSaveEntity().getId() == null) {
            XMLRequestParameterSaveComment xmlRequestSaveComment = (XMLRequestParameterSaveComment) getXmlRequestSaveEntity();
            MFHelper.checkNotNull("type", xmlRequestSaveComment.getDiscriminator());
            CommentDiscriminator commentDiscriminator = CommentDiscriminator.value(xmlRequestSaveComment.getDiscriminator());
            if (commentDiscriminator == null) {
                throw new InvalidDataException(xmlRequestSaveComment.getDiscriminator() + " ist not a valid discriminator!");
            }
            return (Class<Comment>) commentDiscriminator.getCommentClass();

        }
        return Comment.class;
    }

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFComment(getInstance(), (XMLRequestParameterSaveComment) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFComment(getInstance(), (XMLRequestParameterSaveComment) aXmlRequestSaveEntity);
    }

    public Set<BfabricUploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    @Override
    protected <T> void isValid(T entity) throws Exception {
        super.isValid(entity);
        handleValidationErrors(commentService.isValid(getInstance(), getUploadedFiles()));
    }

    @Override
    public void save() {
        commentService.save(getInstance(), getUploadedFiles(), false);
    }

    @Override
    public synchronized <XMLRequestSaveEntity extends XMLRequestParameterSaveAbstractEntity> XMLResponse save(List<XMLRequestSaveEntity> xmlRequestSaveList, boolean idOnly) {
        List<XMLRequestParameterSaveComment> xmlRequestSaveCommentList = (List<XMLRequestParameterSaveComment>) xmlRequestSaveList;

        for (XMLRequestParameterSaveComment xmlRequestSaveComment : xmlRequestSaveCommentList) {
            if (xmlRequestSaveComment.getAttachment() != null) {
                for (XMLRequestParameterSaveUploadedFile xmlRequestSaveUploadedFile : xmlRequestSaveComment.getAttachment()) {
                    try {
                        getUploadedFiles().add(AbstractMF.decodeAndCreateFile(xmlRequestSaveUploadedFile.getBase64(), xmlRequestSaveUploadedFile.getName()));
                    } catch (InvalidDataException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        XMLResponse xmlResponseComments = super.save(xmlRequestSaveCommentList, idOnly);

        getUploadedFiles().clear();

        return xmlResponseComments;
    }
}