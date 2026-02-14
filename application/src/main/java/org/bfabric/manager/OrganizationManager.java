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

import org.bfabric.entity.Organization;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OrganizationService;

@MeasureCalls
@Named
@ViewScoped
public class OrganizationManager extends AbstractEntityManager<Organization> {

    private static final long serialVersionUID = 1;

    private long mergeBillingOrganizationTypeOrganizationId;

    private Organization mergeSelection = new Organization();

    private Organization merged;

    @Inject
    private OrganizationService organizationService;

    public OrganizationManager() {
        super(Organization.class);
    }

    public long getMergeBillingOrganizationTypeOrganizationId() {
        return mergeBillingOrganizationTypeOrganizationId;
    }

    public Organization getMergeSelection() {
        return mergeSelection;
    }

    public Organization getMerged() {
        return merged;
    }

    @Produces
    @Named("organization")
    public Organization getOrganization() {
        return getInstance();
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
                    mergeSelection.setName(getOrganization().getName());
                    mergeSelection.setOrganizationType(getOrganization().getOrganizationType());
                    mergeSelection.setBillingOrganizationType(getOrganization().getBillingOrganizationType());
                    mergeSelection.setDefaultBookingType(getOrganization().getDefaultBookingType());
                    mergeSelection.setDebitorNumber(getOrganization().getDebitorNumber());
                    mergeSelection.setVatNumber(getOrganization().getVatNumber());
                    mergeBillingOrganizationTypeOrganizationId = getOrganization().getId();
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
            organizationService.merge(getOrganization(), getMerged(), getMergeSelection(), mergeBillingOrganizationTypeOrganizationId);
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (final Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        return validateAndSave(organizationService);
    }

    public void setMergeBillingOrganizationTypeOrganizationId(long mergeBillingOrganizationTypeOrganizationId) {
        this.mergeBillingOrganizationTypeOrganizationId = mergeBillingOrganizationTypeOrganizationId;
    }

    public void setMergeSelection(Organization mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Organization organization) {
        merged = organization;
    }
}
