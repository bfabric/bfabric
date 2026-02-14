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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class PlateLayout extends AbstractDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @Min(0)
    @XmlElement
    private Integer capacity;

    @NotNull
    @Min(0)
    @XmlElement
    private Integer columns;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "plateLayout")
    @OrderBy("id desc")
    private Set<Plate> plates = new HashSet<>();

    public static Comparator<Integer> getSamplePlatePositionAssignmentComparator(int numberOfColumns, boolean sampleAssignmentPerRow) {
        return (o1, o2) -> {
            if (!sampleAssignmentPerRow) {
                if (o1 % numberOfColumns < o2 % numberOfColumns) {
                    return -1;
                }
                if (o1 % numberOfColumns > o2 % numberOfColumns) {
                    return 1;
                }
                return Integer.compare(o1, o2);
            }
            return Integer.compare(o1, o2);
        };
    }

    @Override
    public PlateLayout clone() throws CloneNotSupportedException {
        PlateLayout clone = (PlateLayout) super.clone();
        clone.plates = new HashSet<>();
        return clone;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getColumns() {
        return columns;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.PLATEMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getCapacity() != null) {
            addEntityInfoItem(summary, "capacity", getCapacity());
        }
        if (getColumns() != null) {
            addEntityInfoItem(summary, "columns", getColumns());
        }
        if (getRows() != null) {
            addEntityInfoItem(summary, "rows", getRows());
        }
        return summary.toString();
    }

    public String getGridPosition(long rowIndex) {
        // Get the grid position based on the position, e.g. 1 --> A1 or 21 --> C1.
        return String.valueOf((char) ((rowIndex - 1) / getColumns() + 65)) + ((rowIndex - 1) % getColumns() + 1);
    }

    public List<String> getGridPositions() {
        List<String> plateGridPositions = new ArrayList<>();
        for (long platePosition : getPositions()) {
            plateGridPositions.add(getGridPosition(platePosition));
        }
        return plateGridPositions;
    }

    public Set<Plate> getPlates() {
        return plates;
    }

    public long getPosition(String gridPosition) {
        /*
         * Note: The first letter is always a character [A - Z] followed by a number. At the moment there are not more than 26 columns,
         * i.e., the grid position has only one character at the start and not more. Therefore, it is safe to always consider the first index only.
         */
        return (long) (gridPosition.charAt(0) % 65) * getColumns() + Integer.parseInt(gridPosition.substring(1));
    }

    public List<Long> getPositions() {
        return LongStream.rangeClosed(1, getCapacity()).boxed().collect(Collectors.toList());
    }

    public Integer getRows() {
        return (getCapacity() + getColumns() - 1) / getColumns();
    }

    public List<SamplePlatePosition> getSamplePlatePositionsOrderedByAssignmentOrder(Collection<SamplePlatePosition> samplePlatePositions) {
        // Example:Column-wise: A1, B1, A2, B2 (ascending) vs.Row-wise: A1, A2, B1, B2 (ascending)
        List<SamplePlatePosition> orderedSamplePlatePositions = new ArrayList<>();
        if (samplePlatePositions != null && !samplePlatePositions.isEmpty()) {
            List<Integer> sortedPositions = new ArrayList<>();
            Map<Integer, SamplePlatePosition> positionSamplePlatePositionMap = new HashMap<>();
            for (SamplePlatePosition samplePlatePosition : samplePlatePositions) {
                sortedPositions.add(samplePlatePosition.getPosition().intValue() - 1);
                positionSamplePlatePositionMap.put(samplePlatePosition.getPosition().intValue() - 1, samplePlatePosition);
            }
            Plate plate = positionSamplePlatePositionMap.values().iterator().next().getPlate();
            sortedPositions.sort(getSamplePlatePositionAssignmentComparator(plate.getPlateLayout().getColumns(), plate.isSampleAssignmentPerRow()));
            for (Integer position : sortedPositions) {
                orderedSamplePlatePositions.add(positionSamplePlatePositionMap.get(position));
            }
        }
        return orderedSamplePlatePositions;
    }

    public String getSortGridPosition(long rowIndex, boolean sampleAssignmentPerRow) {
        if (sampleAssignmentPerRow) {
            return getGridPosition(rowIndex);
        }
        return StringUtils.leftPad((rowIndex - 1) % getColumns() + 1 + String.valueOf((char) ((rowIndex - 1) / getColumns() + 65)), 4, "0");
    }

    public boolean hasOneColumn() {
        return getColumns() != null && getColumns() == 1;
    }

    public boolean hasOneRow() {
        return getRows() != null && getRows() == 1;
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration() != null && getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getPlates().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return getConfiguration() != null && getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.PLATEREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }
}
