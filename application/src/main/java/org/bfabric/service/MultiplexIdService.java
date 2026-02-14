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

package org.bfabric.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.MultiplexId;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.PlateLayout;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class MultiplexIdService extends AbstractService {

    private static final long serialVersionUID = 1;

    public MultiplexIdService() {
        super(MultiplexId.class);
    }

    public List<MultiplexId> getMultiplexIdsByMultiplexKitId(Long multiplexKitId) {
        return createNamedQuery("MultiplexId.findByMultiplexKitIdOrderByNameAsc").setParameter("multiplexKitId", multiplexKitId).getResultList();
    }

    public List<MultiplexId> getMultiplexIdsByMultiplexKitIdAndSequence(Long multiplexKitId, String sequence) {
        return createNamedQuery("MultiplexId.findByMultiplexKitIdAndSequenceOrderByNameAsc").setParameter("multiplexKitId", multiplexKitId).setParameter("sequence", sequence).getResultList();
    }

    public List<MultiplexId> getMultiplexIdsEnabledByMultiplexKitId(Long multiplexKitId) {
        return createNamedQuery("MultiplexId.findByMultiplexKitIdEnabledOrderByOrderPositionAsc").setParameter("multiplexKitId", multiplexKitId).getResultList();
    }

    public List<MultiplexId> getMultiplexIdsEnabledByMultiplexKitIdFiltered(String filterString, Long multiplexKitId) {
        final EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("multiplexKit.id = :multiplexKitId and enabled = true");
        entityQuery.addParameter("multiplexKitId", multiplexKitId);
        entityQuery.setOrder("orderPosition ASC");
        // Note: Do not change the maxResult!
        entityQuery.setMaxResult(-1);
        return (List<MultiplexId>) entityQuery.getResultList();
    }

    public List<MultiplexId> getOrderedEnabledMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(Long multiplexKitId, PlateLayout plateLayout, boolean isSampleAssignmentPerRow, String type) {
        return orderMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(getMultiplexIdsEnabledByMultiplexKitId(multiplexKitId), plateLayout, isSampleAssignmentPerRow, type);
    }

    public List<MultiplexId> getOrderedEnabledMultiplexIdsByMultiplexKitIdAndPlateLayoutAndTypeFiltered(String filterString, Long multiplexKitId, PlateLayout plateLayout, boolean isSampleAssignmentPerRow, String type) {
        return orderMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(getMultiplexIdsEnabledByMultiplexKitIdFiltered(filterString, multiplexKitId), plateLayout, isSampleAssignmentPerRow, type);
    }

    public List<String> orderCombinedMultiplexIdNamesByPlateLayout(List<String> multiplexIdNames, int numberOfColumns, boolean isSampleAssignmentPerRow, int plateCapacity) {
        // Example: multiplexIdNames = [R1F1, R1F2, R1F3, R1F4, R1F5, R1F6, R1F7, R1F8, R2F1, R2F2, R2F3, R2F4, R2F5, R2F6, R2F7, R2F8, R3F1, R3F2, R3F3, R3F4, ..., R12F6, R12F7, R12F8]
        // The ordered combined multiplex id names, e.g., R1F1, R1F2, R1F3, and so on, are enough if isSampleAssignmentPerRow is false.
        if (isSampleAssignmentPerRow) {
            /*
             * Important: Combined multiplex ids are always "assigned per column" by default, i.e., assignedPerRow is false, as they are combined with x-axis cross y-axis.
             * The size of the x-axis corresponds to the number of columns in the plate.
             *
             * Example: Multiplex kit 'Clontech SMARTer (LowInputHT & Pico) I1:R,I2:F'
             * - x-axis: R1, R2, R3, ..., R12
             * - y-axis: F1, F2, F3, ..., F8
             */
            List<String> orderedMultiplexIdNames = new ArrayList<>();
            int numberOfRows = plateCapacity / numberOfColumns;
            for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {
                for (int index = 0; index < plateCapacity; index += numberOfRows) {
                    orderedMultiplexIdNames.add(multiplexIdNames.get(index));
                }
            }
            return orderedMultiplexIdNames;
        }

        return multiplexIdNames;
    }

    public List<MultiplexId> orderMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(List<MultiplexId> multiplexIds, PlateLayout plateLayout, boolean isSampleAssignmentPerRow, String type) {
        return !multiplexIds.isEmpty() && plateLayout != null ? orderMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(multiplexIds, plateLayout.getColumns(), plateLayout
            .getRows(), isSampleAssignmentPerRow, type) : multiplexIds;
    }

    private List<MultiplexId> orderMultiplexIdsByMultiplexKitIdAndPlateLayoutAndType(List<MultiplexId> multiplexIds, int numberOfColumns, int numberOfRows, boolean isSampleAssignmentPerRow, String type) {
        // The ordered multiplexIds, e.g., i5_1_A01, i5_1_B01, i5_1_C01, etc.
        if (!multiplexIds.isEmpty()) {
            boolean isPlateNumberAndTypeComplete = true;
            for (MultiplexId multiplexId : multiplexIds) {
                if (multiplexId.getPlateNumber() == null || multiplexId.getType() == null) {
                    isPlateNumberAndTypeComplete = false;
                    break;
                }
            }

            if (isPlateNumberAndTypeComplete) {
                if (type != null) {
                    // Ensure that i7 can only be applied to multiplexId and i5 to multiplexId2.
                    multiplexIds.removeIf(multiplexId -> (type.equals(Messages.get("multiplexId")) || type.equals(Messages.get("multiplexIdAndMultiplexId2"))) && multiplexId
                        .isMultiplexId2AssignableOnly() || type
                        .equals(Messages.get("multiplexId2")) && multiplexId.isMultiplexIdAssignableOnly());
                }

                if (!multiplexIds.isEmpty()) {
                    MultiplexKit multiplexKit = multiplexIds.iterator().next().getMultiplexKit();
                    // Rule: isSampleAssignmentPerRow XOR multiplexKit.isMultiplexIdOrderColumnWise <=> FALSE -> reorder
                    if (!isSampleAssignmentPerRow && !multiplexKit.isMultiplexIdOrderColumnWise() || isSampleAssignmentPerRow && multiplexKit.isMultiplexIdOrderColumnWise()) {
                        Map<Long, List<MultiplexId>> multiplexIdsPerPlate = multiplexIds.stream().collect(Collectors.groupingBy(MultiplexId::getPlateNumber));
                        List<Long> multiplexIdsPerPlateKeysOrdered = new ArrayList<>(multiplexIdsPerPlate.keySet());
                        multiplexIdsPerPlateKeysOrdered.sort(Long::compareTo);
                        multiplexIds.clear();

                        if (!isSampleAssignmentPerRow && !multiplexKit.isMultiplexIdOrderColumnWise()) {
                            // columnWise and not columnWise -> reorder
                            for (Long plateNumber : multiplexIdsPerPlateKeysOrdered) {
                                List<Integer> sortedIndices = Arrays.stream(IntStream.rangeClosed(0, multiplexIdsPerPlate.get(plateNumber).size() - 1).toArray()).boxed().distinct()
                                    .sorted(PlateLayout.getSamplePlatePositionAssignmentComparator(numberOfColumns, false)).collect(Collectors.toList());
                                List<MultiplexId> multiplexIdsPerPLateOrdered = multiplexIdsPerPlate.get(plateNumber).stream().sorted(Comparator.comparing(MultiplexId::getOrderPosition))
                                    .collect(Collectors.toList());
                                for (Integer index : sortedIndices) {
                                    multiplexIds.add(multiplexIdsPerPLateOrdered.get(index));
                                }
                            }
                        } else if (isSampleAssignmentPerRow && multiplexKit.isMultiplexIdOrderColumnWise()) {
                            // not columnWise and columnWise -> reorder
                            for (Long plateNumber : multiplexIdsPerPlateKeysOrdered) {
                                List<MultiplexId> multiplexIdsPerPLateOrdered = multiplexIdsPerPlate.get(plateNumber).stream().sorted(Comparator.comparing(MultiplexId::getOrderPosition))
                                    .collect(Collectors.toList());
                                int plateCapacity = numberOfColumns * numberOfRows;
                                for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {
                                    for (int index = rowIndex; index < plateCapacity; index += numberOfRows) {
                                        multiplexIds.add(multiplexIdsPerPLateOrdered.get(index));
                                    }
                                }
                            }
                        }
                        // columnWise and columnWise or not columnWise and not columnWise -> no reorder
                    }
                }
            }
        }

        return multiplexIds;
    }
}