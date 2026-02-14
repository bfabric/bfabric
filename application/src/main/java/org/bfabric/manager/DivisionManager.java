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

import java.util.LinkedHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Company;
import org.bfabric.entity.Division;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.AffiliationHelperService;
import org.bfabric.service.DivisionService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class DivisionManager extends AbstractEntityManager<Division> {

    private static final long serialVersionUID = 1;

    @Inject
    private AffiliationHelperService affiliationHelperService;

    @Inject
    private DivisionService divisionService;

    private Division mergeSelection = new Division();

    private Division merged;

    public DivisionManager() {
        super(Division.class);
    }

    public void createNewParentCompany() {
        getDivision().setCompany(new Company());
    }

    @Produces
    @Named("division")
    public Division getDivision() {
        return getInstance();
    }

    public Division getMergeSelection() {
        return mergeSelection;
    }

    public Division getMerged() {
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
                    mergeSelection.setName(getDivision().getName());
                    mergeSelection.setCompany(getDivision().getCompany());
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
            divisionService.merge(getDivision(), getMerged(), getMergeSelection());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        if (StringHelper.isEmpty(getDivision().getName())) {
            getDivision().setName(getConfiguration().getDefaultDivision());
        }

        LinkedHashMap<String, String> validationErrorMsg = divisionService.isValid(getDivision());
        if (validationErrorMsg.isEmpty()) {
            return super.save();
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void saveCompanyIfNotExists() {
        Company company = affiliationHelperService.saveCompanyIfNotExists(getDivision().getOrganizationType(), getDivision().getCompanyName());
        getDivision().setCompany(company);
    }

    public void setMergeSelection(Division mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Division division) {
        merged = division;
    }
}