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

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class ShibbolethLog extends AbstractEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @NotNull
    @XmlElement
    private LocalDateTime created;

    @NotBlank
    @Size(max = 32)
    @XmlElement
    private String createdBy;

    @NotBlank
    @Size(min = 8, max = 32)
    @XmlElement
    private String shibbolethId;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    private String type;

    public LocalDateTime getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getShibbolethId() {
        return shibbolethId;
    }

    public String getType() {
        return type;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setCreatedBy(String user) {
        createdBy = StringHelper.format(user);
    }

    public void setShibbolethId(String shibbolethId) {
        this.shibbolethId = StringHelper.format(shibbolethId);
    }

    public void setType(String type) {
        this.type = StringHelper.format(type);
    }
}
