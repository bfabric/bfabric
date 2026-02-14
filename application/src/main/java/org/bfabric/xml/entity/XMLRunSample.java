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

import org.bfabric.entity.RunSample;

@XmlRootElement(name = "runsample")
public class XMLRunSample extends XMLAbstractBaseEntity {

    @XmlElement
    private String readcounttotal;

    @XmlElement
    private XMLRun run;

    @XmlElement
    private XMLSample sample;

    public XMLRunSample() {
    }

    public XMLRunSample(RunSample entity, boolean reference) {
        super(entity, reference);
    }

    public XMLRunSample(RunSample entity) {
        super(entity);
        if (entity != null) {
            setRun(new XMLRun(entity.getRun(), true));
            setSample(new XMLSample(entity.getSample(), true));
            if (entity.getReadCountTotal() != null) {
                setReadcounttotal(String.valueOf(entity.getReadCountTotal()));
            }
        }
    }

    public String getReadcounttotal() {
        return readcounttotal;
    }

    public XMLRun getRun() {
        return run;
    }

    public XMLSample getSample() {
        return sample;
    }

    public void setReadcounttotal(String readcounttotal) {
        this.readcounttotal = readcounttotal;
    }

    public void setRun(XMLRun run) {
        this.run = run;
    }

    public void setSample(XMLSample sample) {
        this.sample = sample;
    }
}
