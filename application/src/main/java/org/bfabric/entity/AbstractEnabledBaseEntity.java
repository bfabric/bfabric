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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.Constants;

@MappedSuperclass
public abstract class AbstractEnabledBaseEntity extends AbstractDescriptionNamedBaseEntity {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    protected boolean enabled = true;

    @Transient
    protected Boolean oldEnabled;

    @Transient
    private boolean propagateEnabled;

    public void disable() {
        setEnabled(false);
    }

    public void enable() {
        setEnabled(true);
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "enabled", isEnabled());
        return summary.toString();
    }

    public String getNameWithEnabledMessage() {
        return getName() + (isEnabled() ? Constants.EMPTY_STRING : " -> not enabled anymore!");
    }

    public Boolean getOldEnabled() {
        return oldEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEnabledChanged() {
        return getOldEnabled() == null || !getOldEnabled().equals(isEnabled());
    }

    public boolean isPropagateEnabled() {
        return propagateEnabled;
    }

    public void propagateEnabled() {
    }

    public void setAndPropagateEnabled(boolean enabled) {
        setEnabled(enabled);
        propagateEnabled();
    }

    public void setEnabled(boolean enabled) {
        if (getOldEnabled() == null) {
            setOldEnabled(isEnabled());
        }
        this.enabled = enabled;
    }

    public void setOldEnabled(Boolean oldEnabled) {
        this.oldEnabled = oldEnabled;
    }

    public void setPropagateEnabled(boolean propagateEnabled) {
        this.propagateEnabled = propagateEnabled;
    }

    public void switchEnabled() {
        setEnabled(!isEnabled());
    }
}
