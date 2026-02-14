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

package org.bfabric.manager;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Comment;
import org.bfabric.entity.api.Links;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class LinksManager extends AbstractEntityManager {

    private static final long serialVersionUID = 1;

    @Inject
    private CommentHelper commentHelper;

    @Param
    private String parentClassName;

    @Param
    private String parentId;

    @Produces
    @Named("linkParent")
    public Links getLinkParent() {
        return getInstance();
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public String getParentId() {
        return parentId;
    }

    public Comment getParentIfComment() {
        return Comment.class.isAssignableFrom(entityClass) ? (Comment) entityService.find(ClassHelper.getClassByName(getParentClassName()), Long.valueOf(getParentId())) : null;
    }

    @Override
    public String getRedirectURLAfterCancelCreated() {
        Comment comment = getParentIfComment();
        return comment != null ? commentHelper.getRedirectURL(comment) : super.getRedirectURLAfterCancelCreated();
    }

    @Override
    public String getRedirectURLAfterCancelManaged() {
        Comment comment = getParentIfComment();
        return comment != null ? commentHelper.getRedirectURL(comment) : super.getRedirectURLAfterCancelManaged();
    }

    public String getRedirectURLAfterSave() {
        Comment comment = getParentIfComment();
        return comment != null ? createRedirectShowScreenURL(comment.getParent(), comment.getTab(), null) : super.getRedirectURLAfterSave();
    }

    @Override
    @PostConstruct
    public void init() {
        setId(getParentId());
        entityClass = ClassHelper.getClassByName(getParentClassName().toLowerCase());
        if (entityClass.getGenericSuperclass().equals(Comment.class)) {
            entityClass = Comment.class;
        }
        // Important: set id and entityClass before calling super.init()!
        super.init();
    }

    public void removeEmptyLinks() {
        getLinkParent().getLinks().removeIf(link -> link == null || StringHelper.isEmpty(link.getUrl()));
    }
}