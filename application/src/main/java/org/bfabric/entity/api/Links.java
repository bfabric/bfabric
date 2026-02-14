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

package org.bfabric.entity.api;

import java.util.List;
import java.util.Set;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Link;

public interface Links {

    void addLink();

    default void addLink(Link link) {
        if (getLinks() != null) {
            getLinks().add(link);
        }
    }

    default void addLink(AbstractEntity parent) {
        if (getLinks() != null && parent != null) {
            getLinks().add(new Link(parent));
        }
    }

    List<Link> getLinks();

    Set<Link> getLinksToBeRemoved();

    default void removeLink(Link link) {
        if (getLinks() != null && getLinksToBeRemoved() != null) {
            getLinksToBeRemoved().add(link);
            getLinks().remove(link);
        }
    }

    default void removeLinks() {
        if (getLinks() != null && getLinksToBeRemoved() != null) {
            getLinksToBeRemoved().addAll(getLinks());
            getLinks().clear();
        }
    }

    default void setLinks(List<Link> links) {
        if (getLinks() != null) {
            getLinks().clear();
            getLinks().addAll(links);
        }
    }
}
