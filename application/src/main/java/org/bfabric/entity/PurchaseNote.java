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

import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.enums.CommentDiscriminator;

@Entity
@DiscriminatorValue(value = "PURCHASE_NOTE")
@XmlRootElement
public class PurchaseNote extends Note {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentid", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
    @NotNull
    @XmlIDREF
    private Purchase parent;

    public PurchaseNote() {
        setInternal(true);
        setDiscriminator(CommentDiscriminator.PURCHASE_NOTE);
    }

    @Override
    public Purchase getParent() {
        return parent;
    }

    @Override
    public void setParent(AbstractEntity parent) {
        super.setParent(parent);
        this.parent = (Purchase) parent;
    }
}
