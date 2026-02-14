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

import org.bfabric.entity.Option;

@XmlRootElement(name = "option")
public class XMLOption extends XMLAbstractDescriptionNamedBaseEntity {

    @XmlElement
    protected Boolean enabled;

    @XmlElement
    protected Boolean required;

    @XmlElement
    protected Boolean multiple;

    @XmlElement
    private String parentclassname;

    @XmlElement
    private Long parentid;

    public XMLOption() {
    }

    public XMLOption(Option entity, boolean reference) {
        super(entity, reference);
    }

    public <T extends Option> XMLOption(T entity) {
        super(entity);
        if (entity != null) {
            setEnabled(entity.isEnabled());
            setRequired(entity.isRequired());
            setMultiple(entity.isMultiple());
            if (entity.getParent() != null) {
                setParentid(entity.getParentId());
                setParentclassname(entity.getParentClassName());
            }
        }
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public String getParentclassname() {
        return parentclassname;
    }

    public Long getParentid() {
        return parentid;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public void setParentclassname(String parentclassname) {
        this.parentclassname = parentclassname;
    }

    public void setParentid(Long parentid) {
        this.parentid = parentid;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
