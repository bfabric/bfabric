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

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Pageflow;
import org.bfabric.entity.PageflowStep;
import org.bfabric.entity.PageflowStepPosition;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.PageflowService;
import org.bfabric.service.PageflowStepService;

@MeasureCalls
@Named
@ViewScoped
public class PageflowManager extends AbstractOrderedEnabledNamedBaseEntityManager<Pageflow> {

    private static final long serialVersionUID = 1;

    @Inject
    private PageflowService pageflowService;

    @Inject
    private PageflowStepService pageflowStepService;

    public PageflowManager() {
        super(Pageflow.class);
    }

    @Produces
    @Named("pageflow")
    public Pageflow getPageflow() {
        return getInstance();
    }

    public List<PageflowStep> getPageflowStepsFiltered(String filterString) {
        return (List<PageflowStep>) pageflowStepService.getFilteredEnabledExcludingOrderBy(getPageflow().getPageflowSteps(), filterString, null);
    }

    @Override
    public String remove() {
        pageflowService.remove(getPageflow());
        return getRedirectURLAfterRemove();
    }

    public String removePageflowStepPosition(PageflowStepPosition pageflowStepPosition) {
        if (pageflowStepPosition != null) {
            if (getPageflow().isUpdatable()) {
                getPageflow().removePageflowStepPosition(pageflowStepPosition);
                validateAndSave(pageflowService);
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted") + " " + pageflowStepPosition);
            } else {
                getFacesMessagesManager().bufferWarningClear(Messages.get("notDeletableHint") + " " + pageflowStepPosition);
            }
            return createRedirectShowScreenURL(pageflowStepPosition.getPageflow());
        }
        return null;
    }

    @Override
    public String save() {
        if (getPageflow().getSelectedPageflowStep() != null) {
            getPageflow().addStep(getPageflow().getSelectedPageflowStep());
        }
        return validateAndSave(pageflowService);
    }
}