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

package org.bfabric.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.bfabric.entity.Sample;

public class MultiplexIdConflictRecord implements Serializable {

    private static final long serialVersionUID = 1;

    private Set<Sample> conflictingMultiplexIdSamples = new HashSet<>();

    // If physicalSeparation is true, lanePosition > 0, else -1
    private int lanePosition = -1;

    public MultiplexIdConflictRecord() {
    }

    public MultiplexIdConflictRecord(Integer lanePosition, Set<Sample> conflictingMultiplexIdSamples) {
        if (lanePosition != null) {
            setLanePosition(lanePosition);
        }
        setConflictingMultiplexIdSamples(conflictingMultiplexIdSamples);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof MultiplexIdConflictRecord && hashCode() == object.hashCode();
    }

    public Set<Sample> getConflictingMultiplexIdSamples() {
        return conflictingMultiplexIdSamples;
    }

    public int getLanePosition() {
        return lanePosition;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + super.hashCode();
        result = PRIME * result + lanePosition;
        for (Sample sample : getConflictingMultiplexIdSamples()) {
            result = PRIME * result + sample.hashCode();
        }
        return result;
    }

    public void setConflictingMultiplexIdSamples(Set<Sample> conflictingMultiplexIdSamples) {
        this.conflictingMultiplexIdSamples = conflictingMultiplexIdSamples;
    }

    public void setLanePosition(int lanePosition) {
        this.lanePosition = lanePosition;
    }
}
