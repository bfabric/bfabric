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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Application;
import org.bfabric.entity.ApplicationTestLog;
import org.bfabric.util.DateUtils;
import org.bfabric.util.StringHelper;

@XmlRootElement(name = "applicationtestlog")
public class XMLApplicationTestLog extends XMLAbstractEntity {

    @XmlElement
    private XMLApplication application;

    @XmlElement
    private String created;

    @XmlElement
    private String createdby;

    @XmlElement
    private String log;

    @XmlElement
    private String status;

    public XMLApplicationTestLog() {
    }

    public XMLApplicationTestLog(Application entity, boolean reference) {
        super(entity, reference);
    }

    public XMLApplicationTestLog(ApplicationTestLog entity) {
        super(entity);
        if (entity != null) {
            setCreated(DateUtils.getDateAsFormattedString(entity.getCreated()));
            setCreatedby(entity.getCreatedBy());
            setStatus(entity.getStatus().name());
            if (entity.getApplication() != null) {
                setApplication(new XMLApplication(entity.getApplication(), true));
            }
            if (StringHelper.isNotEmpty(entity.getLog())) {
                setLog(entity.getLog());
            }
        }
    }

    public XMLApplication getApplication() {
        return application;
    }

    public String getCreated() {
        return created;
    }

    public String getCreatedby() {
        return createdby;
    }

    public String getLog() {
        return log;
    }

    public String getStatus() {
        return status;
    }

    public void setApplication(XMLApplication application) {
        this.application = application;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
