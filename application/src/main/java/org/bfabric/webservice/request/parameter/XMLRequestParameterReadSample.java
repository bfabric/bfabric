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

package org.bfabric.webservice.request.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.Constants;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.forms.MFHelper;
import org.bfabric.service.AnnotationService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

public class XMLRequestParameterReadSample extends XMLRequestParameterReadContainerReferencingEntity {

    private final List<XMLRequestParameterReadAttribute> attribute = new ArrayList<>();

    private final List<Long> oldextractid = new ArrayList<>();

    private final List<Long> oldsampleid = new ArrayList<>();

    private final List<Long> plateid = new ArrayList<>();

    private final List<Long> runid = new ArrayList<>();

    private final List<String> tubeid = new ArrayList<>();

    private final List<Long> rununitid = new ArrayList<>();

    private final List<Long> rununitlaneid = new ArrayList<>();

    private final List<String> type = new ArrayList<>();

    public Boolean includefamily = false;

    public Boolean includeplates = false;

    public Boolean includeresources = false;

    public Boolean includeruns = false;

    public Boolean includechildren = false;

    public Boolean includeparents = false;

    public Boolean includereplacements = false;

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(oldextractid, "oldExtractId"));
        items.addAll(getWhereClauseItemsLong(oldsampleid, "oldSampleId"));
        items.addAll(getWhereClauseItemsString(tubeid, "tubeId"));
        items.addAll(getWhereClauseItemsString(type, "type"));
        items.addAll(getJoinWhereClauseItemsLong(plateid, "samplePlatePositions samplePlatePosition", "samplePlatePosition.plate.id", "plateid"));
        items.addAll(getJoinWhereClauseItemsLong(rununitlaneid, "runUnitLanes runUnitLane", "runUnitLane.id", "rununitlaneid"));
        items.addAll(getJoinWhereClauseItemsLong(rununitid, "runUnitLanes runUnitLane", "runUnitLane.runUnit.id", "rununitid"));
        items.addAll(getJoinWhereClauseItemsLong(runid, "runUnitLanes runUnitLane", "runUnitLane.runUnit.run.id", "runid"));
        handleAttributes(items, attribute);
        return items;
    }

    private void handleAttributes(List<WhereClauseItem> items, List<XMLRequestParameterReadAttribute> attributes) throws InvalidDataException, InvalidEnumValueException {
        if (items != null && attributes != null && !attributes.isEmpty()) {
            Map<String, SampleAttributeEnum> sampleAttributeEnumsMap = new HashMap<>();
            for (SampleAttributeEnum sampleAttributeEnum : new HashSet<>(Arrays.asList(SampleAttributeEnum.values()))) {
                sampleAttributeEnumsMap.put(sampleAttributeEnum.getName().toLowerCase() + (sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum
                    .isEnumType() ? Constants.ID : Constants.EMPTY_STRING), sampleAttributeEnum);
            }

            Map<SampleAttributeEnum, List<String>> stringMap = new HashMap<>();
            Map<SampleAttributeEnum, List<Long>> longMap = new HashMap<>();
            Map<SampleAttributeEnum, List<Number>> numberMap = new HashMap<>();
            Map<SampleAttributeEnum, List<Enum<?>>> enumMap = new HashMap<>();
            Map<SampleAttributeEnum, List<Boolean>> booleanMap = new HashMap<>();

            for (XMLRequestParameterReadAttribute xmlRequestParameterReadAttribute : attributes) {
                String sampleAttributeNameLowerCase = xmlRequestParameterReadAttribute.getName().toLowerCase();
                String value = xmlRequestParameterReadAttribute.getValue();
                if (StringHelper.isNotEmpty(sampleAttributeNameLowerCase) && StringHelper.isNotEmpty(value)) {
                    if (!sampleAttributeEnumsMap.containsKey(sampleAttributeNameLowerCase)) {
                        throw new InvalidEnumValueException("No attribute found with name", sampleAttributeNameLowerCase, CollectionHelper.print(sampleAttributeEnumsMap.keySet()));
                    }
                    SampleAttributeEnum sampleAttributeEnum = sampleAttributeEnumsMap.get(sampleAttributeNameLowerCase);
                    if (sampleAttributeEnum.isAnnotationTypeMultiValued() || sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                        List<Annotation> annotations = CDI.current().select(AnnotationService.class).get().getAnnotationsByNameAndType(value, sampleAttributeEnum.getLabel());
                        if (annotations.isEmpty()) {
                            throw new InvalidDataException("No annotation found of type " + sampleAttributeNameLowerCase + " with name " + value);
                        }
                        if (!longMap.containsKey(sampleAttributeEnum)) {
                            longMap.put(sampleAttributeEnum, new ArrayList<>());
                        }
                        longMap.get(sampleAttributeEnum).add(annotations.get(0).getId());
                    } else if (sampleAttributeEnum.isStringType()) {
                        if (!stringMap.containsKey(sampleAttributeEnum)) {
                            stringMap.put(sampleAttributeEnum, new ArrayList<>());
                        }
                        stringMap.get(sampleAttributeEnum).add(value);
                    } else if (sampleAttributeEnum.isNumericType()) {
                        if (!numberMap.containsKey(sampleAttributeEnum)) {
                            numberMap.put(sampleAttributeEnum, new ArrayList<>());
                        }

                        if (sampleAttributeEnum.isIntegerType()) {
                            numberMap.get(sampleAttributeEnum).add(MFHelper.integerValueOf(sampleAttributeEnum.getName(), value));
                        } else if (sampleAttributeEnum.isBigDecimalType()) {
                            numberMap.get(sampleAttributeEnum).add(MFHelper.bigDecimalValueOf(sampleAttributeEnum.getName(), value));
                        } else if (sampleAttributeEnum.isFloatType()) {
                            numberMap.get(sampleAttributeEnum).add(MFHelper.floatValueOf(sampleAttributeEnum.getName(), value));
                        } else if (sampleAttributeEnum.isDoubleType()) {
                            numberMap.get(sampleAttributeEnum).add(MFHelper.doubleValueOf(sampleAttributeEnum.getName(), value));
                        }
                    } else if (sampleAttributeEnum.isLocalDateType()) {
                        if (!stringMap.containsKey(sampleAttributeEnum)) {
                            stringMap.put(sampleAttributeEnum, new ArrayList<>());
                        }
                        stringMap.get(sampleAttributeEnum).add(value);
                    } else if (sampleAttributeEnum.isLocalDateTimeType()) {
                        if (!stringMap.containsKey(sampleAttributeEnum)) {
                            stringMap.put(sampleAttributeEnum, new ArrayList<>());
                        }
                        stringMap.get(sampleAttributeEnum).add(value);
                    } else if (sampleAttributeEnum.isBooleanType()) {
                        if (!booleanMap.containsKey(sampleAttributeEnum)) {
                            booleanMap.put(sampleAttributeEnum, new ArrayList<>());
                        }
                        booleanMap.get(sampleAttributeEnum).add(MFHelper.booleanValueOf(sampleAttributeEnum.getName(), value));
                    } else if (sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType()) {
                        if (!longMap.containsKey(sampleAttributeEnum)) {
                            longMap.put(sampleAttributeEnum, new ArrayList<>());
                        }

                        if (SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum)) {
                            longMap.get(sampleAttributeEnum).add(fetch(SamplePreparationProtocol.class, MFHelper.positiveLongValueOf(sampleAttributeNameLowerCase, value)).getId());
                        } else if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum) || SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                            longMap.get(sampleAttributeEnum).add(fetch(MultiplexKit.class, MFHelper.positiveLongValueOf(sampleAttributeNameLowerCase, value)).getId());
                        } else if (SampleAttributeEnum.INSTRUMENT.equals(sampleAttributeEnum)) {
                            longMap.get(sampleAttributeEnum).add(fetch(Instrument.class, MFHelper.positiveLongValueOf(sampleAttributeNameLowerCase, value)).getId());
                        }
                    } else if (sampleAttributeEnum.isEnumType()) {
                        if (!enumMap.containsKey(sampleAttributeEnum)) {
                            enumMap.put(sampleAttributeEnum, new ArrayList<>());
                        }

                        if (SampleFormEnum.class.equals(sampleAttributeEnum.getClazz())) {
                            enumMap.get(sampleAttributeEnum).add(SampleFormEnum.valueByLabel(value));
                        } else if (SampleQCTypeEnum.class.equals(sampleAttributeEnum.getClazz())) {
                            enumMap.get(sampleAttributeEnum).add(SampleQCTypeEnum.valueByLabel(value));
                        } else if (SampleStatusEnum.class.equals(sampleAttributeEnum.getClazz())) {
                            enumMap.get(sampleAttributeEnum).add(SampleStatusEnum.valueByLabel(value));
                        }
                    }
                }
            }

            for (Map.Entry<SampleAttributeEnum, List<String>> entry : stringMap.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    SampleAttributeEnum sampleAttributeEnum = entry.getKey();
                    if (sampleAttributeEnum.isLocalDateType()) {
                        items.addAll(getWhereClauseItemsDate(entry.getValue(), sampleAttributeEnum.getName()));
                    } else if (sampleAttributeEnum.isLocalDateTimeType()) {
                        items.addAll(getWhereClauseItemsDateTime(entry.getValue(), sampleAttributeEnum.getName()));
                    } else {
                        items.addAll(getWhereClauseItemsString(entry.getValue(), sampleAttributeEnum.getName()));
                    }
                }
            }

            for (Map.Entry<SampleAttributeEnum, List<Long>> entry : longMap.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    SampleAttributeEnum sampleAttributeEnum = entry.getKey();
                    if (sampleAttributeEnum.isAnnotationTypeSingleValued() || sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType()) {
                        items.addAll(getWhereClauseItemsLong(entry.getValue(), sampleAttributeEnum.getName() + "." + Constants.ID));
                    } else if (sampleAttributeEnum.isAnnotationTypeMultiValued()) {
                        items.addAll(getJoinWhereClauseItemsLong(entry.getValue(), sampleAttributeEnum.getName() + " multiValuedAnnotation", "multiValuedAnnotation.id", sampleAttributeEnum.getName()
                            .toLowerCase() + Constants.ID));
                    } else {
                        items.addAll(getWhereClauseItemsLong(entry.getValue(), sampleAttributeEnum.getName()));
                    }
                }
            }

            for (Map.Entry<SampleAttributeEnum, List<Number>> entry : numberMap.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    items.addAll(getWhereClauseItemsNumber(entry.getValue(), entry.getKey().getName()));
                }
            }

            for (Map.Entry<SampleAttributeEnum, List<Enum<?>>> entry : enumMap.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    items.addAll(getWhereClauseItemsEnum(entry.getValue(), entry.getKey().getName()));
                }
            }

            for (Map.Entry<SampleAttributeEnum, List<Boolean>> entry : booleanMap.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    items.addAll(getWhereClauseItemsBooleanList(entry.getValue(), entry.getKey().getName()));
                }
            }
        }
    }
}
