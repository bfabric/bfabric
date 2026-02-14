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

import org.bfabric.Constants;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.User;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;

public interface ShowScreen {

    String getClassName();

    Configuration getConfiguration();

    String getDisplayName();

    default String getEditScreenLink() {
        return getScreenLink("edit");
    }

    default String getHref(String urlPostfix, String content) {
        return "<a href=\"" + getUrl(urlPostfix) + "\">" + content + "</a>";
    }

    long getId();

    default String getScreenLink(String entityClassName, String screen, long entityId, String tab) {
        return entityClassName + "/" + screen + ".html?id=" + entityId + (StringHelper.isNotEmpty(tab) ? "&amp;tab=" + tab : Constants.EMPTY_STRING);
    }

    default String getScreenLink(ShowScreen entity, String screen) {
        return getScreenLink(entity, screen, null);
    }

    default String getScreenLink(ShowScreen entity, String screen, String tab) {
        return getScreenLink(entity.getClassName(), screen, entity.getId(), tab);
    }

    default String getScreenLink(String screen) {
        return getScreenLink(this, screen);
    }

    default String getShowScreenHrefLink() {
        return getShowScreenHrefLink(getDisplayName(), null);
    }

    default String getShowScreenHrefLink(String tab) {
        return getShowScreenHrefLink(null, tab);
    }

    default String getShowScreenHrefLink(String content, String tab) {
        return getClassName() + " " + getHref(getShowScreenLink(tab), content);
    }

    default String getShowScreenHrefLinkWithoutClassName() {
        return getHref(getShowScreenLink("details"), getDisplayName());
    }

    default String getShowScreenLink(ShowScreen entity) {
        return getShowScreenLink(entity, null);
    }

    default String getShowScreenLink() {
        return getShowScreenLink(this);
    }

    default String getShowScreenLink(String tab) {
        return getShowScreenLink(this, tab);
    }

    default String getShowScreenLink(ShowScreen entity, String tab) {
        return getScreenLink(entity, Constants.SHOW, tab);
    }

    default String getShowScreenMemberTabHrefLink() {
        return getClassName() + " " + getHref(getShowScreenLink(), getDisplayName());
    }

    default String getShowScreenUrl() {
        return getConfiguration().getBaseUrl() + getShowScreenLink(this, null);
    }

    default String getShowScreenUserHrefLink(User user) {
        return getHref(getShowScreenLink(user), user.getName());
    }

    default String getUrl(String urlPostfix) {
        return UriHelper.normalize(getConfiguration().getBaseUrl() + urlPostfix);
    }
}