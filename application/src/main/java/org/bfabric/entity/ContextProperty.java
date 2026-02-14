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
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;

import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.enums.ContextPropertyDiscriminator;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
public abstract class ContextProperty extends AbstractEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @NotNull
    @Column(columnDefinition = "boolean DEFAULT true")
    private boolean active;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(insertable = false, updatable = false)
    private ContextPropertyDiscriminator discriminator;

    @NotNull
    @Column(columnDefinition = "boolean DEFAULT true")
    private boolean enabled = true;

    @NotNull
    private String value;

    public ContextProperty() {
    }

    public ContextProperty(@NotNull final ContextPropertyDiscriminator discriminator) {
        this.discriminator = discriminator;
    }

    public boolean getActive() {
        return active;
    }

    public ContextPropertyDiscriminator getDiscriminator() {
        return discriminator;
    }

    public String getValue() {
        return value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setDiscriminator(ContextPropertyDiscriminator discriminator) {
        this.discriminator = discriminator;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
            "id=" + getId() + ", " +
            "discriminator=" + discriminator + ", " +
            "value=" + value + ", " +
            "active=" + active + ", " +
            "enabled=" + enabled +
            "]";
    }
}