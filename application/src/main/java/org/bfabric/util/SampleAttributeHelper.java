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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.Order;
import org.bfabric.entity.Sample;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.manager.IdentityManager;
import org.hibernate.Hibernate;

@Named
@ViewScoped
public class SampleAttributeHelper implements Serializable {

    private static final long serialVersionUID = 1;

    @Inject
    private IdentityManager identityManager;

    @CachedMethodResult
    public static List<SampleAttributeEnum> getAttributeEnums(String type, Set<String> types, Set<SampleQCTypeEnum> qcTypes, Set<SampleFormEnum> sampleForms) {
        return SampleAttributeEnum.getAttributeEnums(type, types, qcTypes, sampleForms);
    }

    public static List<SampleAttributeEnum> getAttributeEnums(Collection<Sample> samples) {
        return SampleAttributeEnum.getAttributeEnums(getTypes(samples));
    }

    public static boolean[] getMultiplexIdsAssignability(String type, Set<String> types) {
        return SampleAttributeEnum.getMultiplexIdsAssignability(type, types);
    }

    public static Set<String> getTypes(Collection<Sample> samples) {
        Set<String> types = new HashSet<>();
        for (Sample sample : samples) {
            types.add(sample.getType());
        }
        return types;
    }

    @CachedMethodResult
    public static boolean isCalculatedAttribute(SampleAttributeEnum sampleAttributeEnum, String sampleType) {
        return sampleAttributeEnum != null && sampleAttributeEnum.isCalculatedAttribute(sampleType);
    }

    @CachedMethodResult
    public static boolean isCellInputEnabled(SampleAttributeEnum sampleAttributeEnum, Boolean multiplexed, String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        return sampleAttributeEnum.isAttribute(type) && !sampleAttributeEnum.isLabeledOrMultiplexed(multiplexed) && !(sampleAttributeEnum.equals(SampleAttributeEnum.MULTIPLEXED) && isMultiplexedType(type))
            && (!sampleAttributeEnum.isSubTypeDependentAttribute(type) || sampleAttributeEnum.isSubTypeDependentAttributeEnabled(type, sampleFormEnum, sampleQCTypeEnum));
    }

    @CachedMethodResult
    public static boolean isMultiplexedType(String type) {
        return SampleTypeEnum.isMultiplexed(type);
    }

    @CachedMethodResult
    public static boolean isTubeIdEditable(Sample sample) {
        return sample.isTubeIdEditable();
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnums(String type, Set<String> types) {
        return SampleAttributeEnum.getAttributeEnums(type, types);
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnums(Set<String> types) {
        return SampleAttributeEnum.getAttributeEnums(null, types);
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnums(String type, boolean orderForm, boolean confirmationForm) {
        return SampleAttributeEnum.getAttributeEnums(false, null, type, false, null, false, null, orderForm, confirmationForm);
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnumsAssignSamplesToMultiplexSourceTargetColumns() {
        return Arrays
            .asList(SampleAttributeEnum.TUBE_ID, SampleAttributeEnum.MULTIPLEX_ID, SampleAttributeEnum.MULTIPLEX_ID_2, SampleAttributeEnum.BLOCK, SampleAttributeEnum.QC_PASSED, SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL);
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnumsAssignSamplesToRunSourceTargetColumns() {
        return Arrays
            .asList(SampleAttributeEnum.TUBE_ID, SampleAttributeEnum.MULTIPLEX_ID, SampleAttributeEnum.MULTIPLEX_ID_2, SampleAttributeEnum.QC_PASSED, SampleAttributeEnum.RE_MULTIPLEXED, SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL);
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnumsExcluding(Collection<Sample> samples, Collection<SampleAttributeEnum> excluded) {
        List<SampleAttributeEnum> attributeEnums = SampleAttributeEnum.getAttributeEnums(getTypes(samples));
        if (excluded != null) {
            attributeEnums.removeAll(excluded);
        }
        return attributeEnums;
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getAttributeEnumsExcluding(String type, boolean orderForm, boolean confirmationForm, Collection<SampleAttributeEnum> excluded) {
        List<SampleAttributeEnum> attributeEnums = SampleAttributeEnum.getAttributeEnums(false, null, type, false, null, false, null, orderForm, confirmationForm);
        if (excluded != null) {
            attributeEnums.removeAll(excluded);
        }
        return attributeEnums;
    }

    @CachedMethodResult
    public String getWatermarkValueForSampleAttribute(SampleAttributeEnum sampleAttributeEnum, Boolean multiplexed, String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        return !sampleAttributeEnum.isAttribute(type) || sampleAttributeEnum.isLabeledOrMultiplexed(multiplexed) ? Messages.get("disabled") : sampleAttributeEnum
            .getWatermarkValueForSampleSubTypeDependentAttribute(type, sampleFormEnum, sampleQCTypeEnum);
    }

    @CachedMethodResult
    public String getWatermarkValueHintForSampleAttribute(SampleAttributeEnum sampleAttributeEnum, Boolean multiplexed, String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        return !sampleAttributeEnum.isAttribute(type) ? Messages.get("watermarkDisabledHint").replace("{0}", sampleAttributeEnum.getLabel()).replace("{1}", Messages.get("sampleType"))
            .replace("{2}", type) : sampleAttributeEnum.isLabeledOrMultiplexed(multiplexed) ? Messages.get("watermarkDisabledHint").replace("{0}", sampleAttributeEnum.getLabel())
            .replace("{1}", Messages.get("multiplexed")).replace("{2}", Constants.EMPTY_STRING) : sampleAttributeEnum
            .getWatermarkValueHintForSampleSubTypeDependentAttribute(type, sampleFormEnum, sampleQCTypeEnum);
    }

    @CachedMethodResult
    public boolean isAttributeCheckRequired(Order order) {
        return order != null && LocalDateTime.of(2019, 9, 26, 0, 0).isAfter(order.getCreated());
    }

    @CachedMethodResult
    public boolean isAttributeRenderedForBatchOrderItemScreen(String type, SampleAttributeEnum sampleAttributeEnum, Order order) {
        if (isMultiplexedType(type) && !identityManager.hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE)) {
            return SampleAttributeEnum.MULTIPLEXED.equals(sampleAttributeEnum) || SampleAttributeEnum.MOLARITY.equals(sampleAttributeEnum);
        }
        if (isMultiplexed(sampleAttributeEnum)) {
            return isMultiplexedAndRendered(sampleAttributeEnum, order);
        }
        if (isSizeGenomeEstimated(sampleAttributeEnum)) {
            return isSizeGenomeEstimatedAndRendered(sampleAttributeEnum, order, type);
        }
        return true;
    }

    @CachedMethodResult
    public boolean isDisabled(Sample sample, SampleAttributeEnum sampleAttributeEnum) {
        if (sample != null && (SampleAttributeEnum.RE_MULTIPLEXED.equals(sampleAttributeEnum) || SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum))) {
            return !sample.isUpdatable() || sample.isAssignedToRuns();
        }
        return false;
    }

    @CachedMethodResult
    public boolean isDisplayRequired(SampleAttributeEnum sampleAttributeEnum, String type) {
        return !sampleAttributeEnum.isSubTypeDependentAttribute(type) && sampleAttributeEnum.isAttributeRequired(type);
    }

    @CachedMethodResult
    public boolean isDisplayRequiredCell(SampleAttributeEnum sampleAttributeEnum, String type, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        return sampleAttributeEnum.isSubTypeDependentAttribute(type) && sampleAttributeEnum.isSubTypeDependentAttributeRequired(type, sampleFormEnum, sampleQCTypeEnum);
    }

    @CachedMethodResult
    public boolean isMultiplexed(SampleAttributeEnum sampleAttributeEnum) {
        return sampleAttributeEnum != null && sampleAttributeEnum.isMultiplexed();
    }

    @CachedMethodResult
    public boolean isMultiplexedAndRendered(SampleAttributeEnum sampleAttributeEnum, Order order) {
        return sampleAttributeEnum != null && sampleAttributeEnum.isMultiplexedAndRendered(order);
    }

    @CachedMethodResult
    public boolean isRenderedColumn(Order order, String columnName) {
        return isRenderedColumn(order, columnName, false);
    }

    @CachedMethodResult
    public boolean isRenderedColumn(Order order, String columnName, boolean confirmationForm) {
        if (order != null) {
            // Show the order item specific columns.
            if (order.getServiceType() != null && order.isServiceColumnEnabled()) {
                if (Constants.SERVICE.equals(columnName) && order.getSampleType() != null) {
                    return true;
                }
            } else {
                if (SampleAttributeEnum.ARRAY_DESIGN_NAME.getName().equals(columnName)) {
                    return order.getServiceType().getServiceArea().getName().equalsIgnoreCase(Constants.SERVICE_AREA_MICROARRAYS);
                }
                if (isAttributeCheckRequired(order) && (Constants.ORDER_ITEM_INSERT_SIZE.equals(columnName) || Constants.ORDER_ITEM_READ_TYPE.equals(columnName) || Constants.ORDER_ITEM_REGION
                    .equals(columnName)
                    || Constants.ORDER_ITEM_LIBRARY_TYPE.equals(columnName) || Constants.ORDER_ITEM_MULTIPLEXING.equals(columnName))) {
                    return order.getServiceType().getServiceArea().getName().equalsIgnoreCase(Constants.SERVICE_AREA_NEXTGENSEQUENCING);
                }
            }
            if (order.getSampleType() != null) {
                if (order.getSampleType().getName().equals(SampleTypeEnum.PROTEOMICS_SERVICES.getLabel()) && (Constants.SAMPLE_FORM.equals(columnName) || Constants.ANALYSIS_REASONS
                    .equals(columnName) && order.getServiceType().getId() != 87)) {
                    return true;
                }
                // Show the sample type specific columns.
                SampleAttributeEnum sampleAttributeEnum = SampleAttributeEnum.getAttributeByName(columnName);
                if (sampleAttributeEnum != null) {
                    return sampleAttributeEnum.isAttribute(order.getSampleType().getName(), true, confirmationForm);
                }
            }
            return false;
        }
        return true;
    }

    @CachedMethodResult
    public boolean isRequiredColumn(Order order, String columnName) {
        if (order != null) {
            // The order item specific columns.
            if (order.getServiceType().getServiceArea().getName().equalsIgnoreCase(Constants.SERVICE_AREA_MICROARRAYS) || order.getServiceType().getServiceArea().getName()
                .equalsIgnoreCase(Constants.SERVICE_AREA_NEXTGENSEQUENCING)) {
                if (SampleAttributeEnum.ARRAY_DESIGN_NAME.getName().equals(columnName)) {
                    return order.getServiceType().getServiceArea().getName().equalsIgnoreCase(Constants.SERVICE_AREA_MICROARRAYS);
                }
                if (Constants.ORDER_ITEM_REGION.equals(columnName)) {
                    return order.getSampleType().getName().equals(SampleTypeEnum.SEQUENCING.getLabel());
                }
            }
            if (order.getSampleType() != null) {
                // The sample type specific columns.
                if (Constants.SAMPLE_FORM.equals(columnName)) {
                    return order.getSampleType().getName().equals(SampleTypeEnum.PROTEOMICS_SERVICES.getLabel());
                }
                return SampleAttributeEnum.isAttributeRequired(columnName, order.getSampleType().getName());
            }
        }
        return false;
    }

    @CachedMethodResult
    public boolean isSizeGenomeEstimated(SampleAttributeEnum sampleAttributeEnum) {
        return sampleAttributeEnum != null && sampleAttributeEnum.isSizeGenomeEstimated();
    }

    @CachedMethodResult
    public boolean isSizeGenomeEstimatedAndRendered(SampleAttributeEnum sampleAttributeEnum, Container container, String type) {
        return sampleAttributeEnum != null && container != null && (container.isContainerProject() && type != null && sampleAttributeEnum.isAttribute(type) || sampleAttributeEnum
            .isSizeGenomeEstimatedAndRendered((Order) Hibernate.unproxy(container)));
    }
}