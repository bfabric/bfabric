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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "option_name_unique", columnNames = { "name", "parentClassName", "parentId" }) })
@XmlRootElement
@NamedQuery(name = "Option.checkUniqueName", query = "SELECT a.id FROM Option a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.parentId = :parentId and a.parentClassName = :parentClassName")
@NamedQuery(name = "Option.findByParent", query = "SELECT a FROM Option a WHERE a.parentId = :parentId and a.parentClassName = :parentClassName ORDER BY a.name")
@NamedQuery(name = "Option.findAllParentClassNames", query = "SELECT DISTINCT a.parentClassName FROM Option a WHERE a.parentClassName IS NOT NULL ORDER BY a.parentClassName")
public class Option extends AbstractParentDependentDescriptionNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "boolean DEFAULT true")
    @XmlElement
    protected boolean enabled = true;

    @Transient
    protected Boolean oldEnabled;

    @Transient
    private List<OptionValue> enabledOptionValues;

    @Column(columnDefinition = "boolean DEFAULT false")
    @XmlElement
    private boolean multiple = false;

    @Transient
    private OptionValue optionValueHolder;

    @Transient
    private Set<OptionValue> optionValueHolders = new HashSet<>();

    @OneToMany(mappedBy = "option", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlElement(name = "optionValue")
    private List<OptionValue> optionValues = new ArrayList<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @XmlElement
    private boolean required = false;

    public Option() {
    }

    public Option(AbstractEntity parent) {
        if (parent != null) {
            setParent(parent);
        }
    }

    @Override
    public Option clone() throws CloneNotSupportedException {
        Option clone = (Option) super.clone();
        clone.optionValues = new ArrayList<>();
        for (OptionValue optionValue : getOptionValues()) {
            if (optionValue.isEnabled()) {
                OptionValue optionValueClone = optionValue.clone();
                optionValueClone.setOption(clone);
                clone.optionValues.add(optionValueClone);
            }
        }
        clone.setEnabled(true);
        return clone;
    }

    public Option clone(AbstractEntity parent) throws CloneNotSupportedException {
        Option clone = clone();
        clone.setParent(parent);
        return clone;
    }

    public void disable() {
        setEnabled(false);
    }

    public void enable() {
        setEnabled(true);
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @SuppressWarnings("unused")
    public List<OptionValue> getEnabledOptionValues() {
        if (enabledOptionValues == null) {
            enabledOptionValues = getOptionValues().stream().filter(optionValue -> optionValue.isEnabled() || optionValueHolders.contains(optionValue)).collect(Collectors.toList());
        }
        return enabledOptionValues;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "required", isRequired());
        addEntityInfoItem(summary, "multiple", isMultiple());
        addEntityInfoItem(summary, "enabled", isEnabled());
        return summary.toString();
    }

    public String getNameWithEnabledMessage() {
        return getName() + (isEnabled() ? Constants.EMPTY_STRING : " -> not enabled anymore!");
    }

    public Boolean getOldEnabled() {
        return oldEnabled;
    }

    @SuppressWarnings("unused")
    public OptionValue getOptionValueHolder() {
        return optionValueHolder;
    }

    @SuppressWarnings("unused")
    public Set<OptionValue> getOptionValueHolders() {
        return optionValueHolders;
    }

    @SuppressWarnings("unused")
    public String getOptionValueHoldersAsString() {
        return optionValueHolders.stream().map(OptionValue::getName).collect(Collectors.joining(", "));
    }

    public List<OptionValue> getOptionValues() {
        return optionValues;
    }

    public boolean hasNonDeletableOptionValues() {
        return getOptionValues().stream().anyMatch(optionValue -> !optionValue.isDeletable());
    }

    @SuppressWarnings("unused")
    public void initOptionValueHolders(Collection<OptionValue> optionValues) {
        if (optionValues != null) {
            optionValueHolders = optionValues.stream().filter(optionValue -> this.equals(optionValue.getOption())).collect(Collectors.toSet());
            optionValueHolder = optionValueHolders.stream().findFirst().orElse(null);
        }
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && !hasNonDeletableOptionValues();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @SuppressWarnings("unused")
    public boolean isEnabledChanged() {
        return getOldEnabled() == null || !getOldEnabled().equals(isEnabled());
    }

    public boolean isMultiple() {
        return multiple;
    }

    public boolean isNameUnique() {
        return getParent().getOptions().stream().noneMatch(option -> !option.equals(this) && option.getName().equalsIgnoreCase(this.getName()));
    }

    @Override
    public boolean isReadable() {
        return getParent() != null && getParent().isReadable();
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setEnabled(boolean enabled) {
        if (getOldEnabled() == null) {
            setOldEnabled(isEnabled());
        }
        this.enabled = enabled;
    }

    @SuppressWarnings("unused")
    public void setEnabledOptionValues(List<OptionValue> enabledOptionValues) {
        this.enabledOptionValues = enabledOptionValues;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public void setOldEnabled(Boolean oldEnabled) {
        this.oldEnabled = oldEnabled;
    }

    @SuppressWarnings("unused")
    public void setOptionValueHolder(OptionValue optionValueHolder) {
        if (optionValueHolder != null) {
            optionValueHolders.add(optionValueHolder);
        } else {
            optionValueHolders.clear();
        }
    }

    @SuppressWarnings("unused")
    public void setOptionValueHolders(Set<OptionValue> optionValueHolders) {
        this.optionValueHolders = optionValueHolders;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void switchEnabled() {
        setEnabled(!isEnabled());
    }
}