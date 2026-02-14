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

import org.bfabric.entity.Technology;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.TechnologyService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class TechnologyManager extends AbstractOrderedEnabledNamedBaseEntityManager<Technology> {

    private static final long serialVersionUID = 1;

    private Technology mergeSelection = new Technology();

    private Technology merged;

    @Inject
    private TechnologyService technologyService;

    public TechnologyManager() {
        super(Technology.class);
    }

    public Technology getMergeSelection() {
        return mergeSelection;
    }

    public Technology getMerged() {
        return merged;
    }

    @Produces
    @Named("technology")
    public Technology getTechnology() {
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
                    mergeSelection.setName(getTechnology().getName());
                    mergeSelection.setEnabled(getTechnology().isEnabled());
                    mergeSelection.setOrderPosition(getTechnology().getOrderPosition());
                    if (StringHelper.isNotEmpty(getTechnology().getDescription())) {
                        mergeSelection.setDescription(getTechnology().getDescription());
                    } else {
                        mergeSelection.setDescription(getMerged().getDescription());
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
            technologyService.merge(getTechnology(), getMerged(), getMergeSelection());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    @Override
    public String save() {
        return validateAndSave(technologyService);
    }

    public void setMergeSelection(Technology mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Technology technology) {
        merged = technology;
    }
}
