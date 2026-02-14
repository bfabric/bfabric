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

import org.bfabric.entity.InstrumentEventType;

@XmlRootElement(name = "instrumenteventtype")
public class XMLInstrumentEventType extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private String instrumentevents;

    @XmlElement
    private String usercreatable;

    @XmlElement
    private String uservisible;

    public XMLInstrumentEventType() {
    }

    public XMLInstrumentEventType(InstrumentEventType entity, boolean reference) {
        super(entity, reference);
    }

    public XMLInstrumentEventType(InstrumentEventType entity) {
        super(entity);
        if (entity != null) {
            if (entity.getInstrumentEvents() != null) {
                setInstrumentevents(String.valueOf(entity.getInstrumentEvents().size()));
            }
            setUservisible(String.valueOf(entity.isUserVisible()));
            setUsercreatable(String.valueOf(entity.isUserVisible()));
        }
    }

    public String getInstrumentevents() {
        return instrumentevents;
    }

    public String getUsercreatable() {
        return usercreatable;
    }

    public String getUservisible() {
        return uservisible;
    }

    public void setInstrumentevents(String instrumentevents) {
        this.instrumentevents = instrumentevents;
    }

    public void setUsercreatable(String usercreatable) {
        this.usercreatable = usercreatable;
    }

    public void setUservisible(String uservisible) {
        this.uservisible = uservisible;
    }
}