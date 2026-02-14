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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Job;
import org.bfabric.entity.Workunit;

@XmlRootElement(name = "job")
public class XMLJob extends XMLAbstractBaseEntity {

    @XmlElement
    private String action;

    @XmlElement
    private String log;

    @XmlElement
    private String parentclassname;

    @XmlElement
    private String parentid;

    @XmlElement
    private XMLUser requester;

    @XmlElement
    private String status;

    @XmlElement
    private String url;

    @XmlElement
    private List<XMLWorkunit> workunit = new ArrayList<>();

    public XMLJob(Job entity) {
        super(entity);
        if (entity != null) {
            if (entity.getAction() != null) {
                setAction(entity.getAction().name());
            }
            if (entity.getStatus() != null) {
                setStatus(entity.getStatus().name());

            }
            if (entity.getParentClassName() != null) {
                setParentclassname(entity.getParentClassName());
            }
            setParentid(String.valueOf(entity.getParentId()));
            if (entity.getLog() != null && !entity.getLog().isEmpty()) {
                setLog(entity.getLog());
            }
            if (entity.getUrl() != null && !entity.getUrl().isEmpty()) {
                setUrl(entity.getUrl());
            }
            if (entity.getRequester() != null) {
                requester = new XMLUser(entity.getRequester(), true);
            }
            if (entity.getWorkunits() != null) {
                for (Workunit aWorkunit : entity.getWorkunits()) {
                    getWorkunit().add(new XMLWorkunit(aWorkunit, true));
                }
            }
        }
    }

    public XMLJob() {
    }

    public XMLJob(Job entity, boolean reference) {
        super(entity, reference);
    }

    public String getAction() {
        return action;
    }

    public String getLog() {
        return log;
    }

    public String getParentclassname() {
        return parentclassname;
    }

    public String getParentid() {
        return parentid;
    }

    public XMLUser getRequester() {
        return requester;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public List<XMLWorkunit> getWorkunit() {
        return workunit;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void setParentclassname(String parentclassname) {
        this.parentclassname = parentclassname;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public void setRequester(XMLUser requester) {
        this.requester = requester;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setWorkunit(List<XMLWorkunit> workunit) {
        this.workunit = workunit;
    }
}