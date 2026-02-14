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

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Company;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.CompanyService;

@MeasureCalls
@Named
@ViewScoped
public class CompanyManager extends AbstractEntityManager<Company> {

    private static final long serialVersionUID = 1;

    @Inject
    private CompanyService companyService;

    private long mergeBillingOrganizationTypeCompanyId;

    private Company mergeSelection = new Company();

    private Company merged;

    public CompanyManager() {
        super(Company.class);
    }

    @Produces
    @Named("company")
    public Company getCompany() {
        return getInstance();
    }

    public long getMergeBillingOrganizationTypeCompanyId() {
        return mergeBillingOrganizationTypeCompanyId;
    }

    public Company getMergeSelection() {
        return mergeSelection;
    }

    public Company getMerged() {
        return merged;
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
                    mergeSelection.setName(getCompany().getName());
                    mergeSelection.setOrganizationType(getCompany().getOrganizationType());
                    mergeSelection.setBillingOrganizationType(getCompany().getBillingOrganizationType());
                    mergeSelection.setDefaultBookingType(getCompany().getDefaultBookingType());
                    mergeSelection.setDebitorNumber(getCompany().getDebitorNumber());
                    mergeSelection.setVatNumber(getCompany().getVatNumber());
                    mergeBillingOrganizationTypeCompanyId = getCompany().getId();
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
            companyService.merge(getCompany(), getMerged(), getMergeSelection(), mergeBillingOrganizationTypeCompanyId);
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        return validateAndSave(companyService);
    }

    public void setMergeBillingOrganizationTypeCompanyId(long mergeBillingOrganizationTypeCompanyId) {
        this.mergeBillingOrganizationTypeCompanyId = mergeBillingOrganizationTypeCompanyId;
    }

    public void setMergeSelection(Company mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Company company) {
        merged = company;
    }
}
