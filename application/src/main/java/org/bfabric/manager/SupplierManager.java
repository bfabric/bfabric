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

package org.bfabric.manager;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Supplier;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.SupplierService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class SupplierManager extends AbstractEntityManager<Supplier> {

    private static final long serialVersionUID = 1;

    private Supplier mergeSelection = new Supplier();

    private Supplier merged;

    @Inject
    private SupplierService supplierService;

    public SupplierManager() {
        super(Supplier.class);
    }

    public Supplier getMergeSelection() {
        return mergeSelection;
    }

    public Supplier getMerged() {
        return merged;
    }

    @Produces
    @Named("supplier")
    public Supplier getSupplier() {
        return getInstance();
    }

    public List<Supplier> getSupplierFiltered(String filterString) {
        return supplierService.getSupplierFiltered(filterString, null);
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        initMerge();
    }

    public void initMerge() {
        if (getInstance() != null && mergeId != null) {
            try {
                merged = getInstance(mergeId);
                if (merged != null) {
                    mergeSelection.setName(getSupplier().getName());
                    if (StringHelper.isNotEmpty(getSupplier().getDescription())) {
                        mergeSelection.setDescription(getSupplier().getDescription());
                    } else {
                        mergeSelection.setDescription(getMerged().getDescription());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getCompanyId())) {
                        mergeSelection.setCompanyId(getSupplier().getCompanyId());
                    } else {
                        mergeSelection.setCompanyId(getMerged().getCompanyId());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getCompanyId())) {
                        mergeSelection.setCompanyId(getSupplier().getCompanyId());
                    } else {
                        mergeSelection.setCompanyId(getMerged().getCompanyId());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getFullAddress())) {
                        mergeSelection.setAddress(getSupplier().getAddress());
                    } else {
                        mergeSelection.setAddress(getMerged().getAddress());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getPhone())) {
                        mergeSelection.setPhoneNumber(getSupplier().getPhoneNumber());
                    } else {
                        mergeSelection.setPhoneNumber(getMerged().getPhoneNumber());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getEmail())) {
                        mergeSelection.setEmail(getSupplier().getEmail());
                    } else {
                        mergeSelection.setEmail(getMerged().getEmail());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getUrl())) {
                        mergeSelection.setUrl(getSupplier().getUrl());
                    } else {
                        mergeSelection.setUrl(getMerged().getUrl());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getContactTitle())) {
                        mergeSelection.setContactTitle(getSupplier().getContactTitle());
                    } else {
                        mergeSelection.setContactTitle(getMerged().getContactTitle());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getContactSalutation())) {
                        mergeSelection.setContactSalutation(getSupplier().getContactSalutation());
                    } else {
                        mergeSelection.setContactSalutation(getMerged().getContactSalutation());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getContactFirstName())) {
                        mergeSelection.setContactFirstName(getSupplier().getContactFirstName());
                    } else {
                        mergeSelection.setContactFirstName(getMerged().getContactFirstName());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getContactLastName())) {
                        mergeSelection.setContactLastName(getSupplier().getContactLastName());
                    } else {
                        mergeSelection.setContactLastName(getMerged().getContactLastName());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getContactPhone())) {
                        mergeSelection.setContactPhoneNumber(getSupplier().getContactPhoneNumber());
                    } else {
                        mergeSelection.setContactPhoneNumber(getMerged().getContactPhoneNumber());
                    }
                    if (StringHelper.isNotEmpty(getSupplier().getContactEmail())) {
                        mergeSelection.setContactEmail(getSupplier().getContactEmail());
                    } else {
                        mergeSelection.setContactEmail(getMerged().getContactEmail());
                    }
                } else {
                    redirectToEntityNotFoundErrorPage(getEntityClass().getSimpleName(), String.valueOf(mergeId));
                }
            } catch (NumberFormatException e) {
                redirectToEntityIdInvalidErrorPage(getEntityClass().getSimpleName(), mergeId);
            }
        }
    }

    @Override
    public String merge() {
        try {
            supplierService.merge(getSupplier(), getMerged(), getMergeSelection());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        return validateAndSave(supplierService);
    }

    public void setMergeSelection(Supplier mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Supplier supplier) {
        merged = supplier;
    }
}
