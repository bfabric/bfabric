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

import org.bfabric.entity.OptionValue;

@XmlRootElement(name = "optionvalue")
public class XMLOptionValue extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private XMLOption option;

    @XmlElement
    private Integer orders;

    public XMLOptionValue() {
    }

    public XMLOptionValue(OptionValue entity, boolean reference) {
        super(entity, reference);
    }

    public <T extends OptionValue> XMLOptionValue(T entity) {
        super(entity);
        if (entity != null) {
            if (entity.getOption() != null) {
                setOption(new XMLOption(entity.getOption(), true));
            }
            if (!entity.getOrders().isEmpty()) {
                setOrders(entity.getOrders().size());
            }
        }
    }

    public XMLOption getOption() {
        return option;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOption(XMLOption option) {
        this.option = option;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }
}
