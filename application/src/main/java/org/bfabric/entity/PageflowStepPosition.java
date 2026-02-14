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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "pageflowid", "position" }) })
@XmlRootElement
public class PageflowStepPosition extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pageflowid")
    @XmlIDREF
    private Pageflow pageflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pageflowstepid")
    @XmlIDREF
    private PageflowStep pageflowStep;

    private int position;

    public PageflowStepPosition() {
    }

    public PageflowStepPosition(Pageflow pageflow, PageflowStep pageflowStep, int position) {
        this.pageflow = pageflow;
        this.pageflowStep = pageflowStep;
        this.position = position;
    }

    public Pageflow getPageflow() {
        return pageflow;
    }

    public PageflowStep getPageflowStep() {
        return pageflowStep;
    }

    public int getPosition() {
        return position;
    }

    public void setPageflow(Pageflow pageflow) {
        this.pageflow = pageflow;
    }

    public void setPageflowStep(PageflowStep pageflowStep) {
        this.pageflowStep = pageflowStep;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}