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

import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AccessRequest;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.AccessRequestService;

@MeasureCalls
@Named
@ViewScoped
public class AccessRequestManager extends AbstractEntityManager<AccessRequest> {

    private static final long serialVersionUID = 1;

    @Inject
    private AccessRequestService accessRequestService;

    public AccessRequestManager() {
        super(AccessRequest.class);
    }

    public String approveAccessRequest() {
        Map<String, Set<String>> facesMessages = accessRequestService.approveAccessRequest(getAccessRequest(), getConfiguration());
        printFacesMessagesClear(facesMessages);
        return !facesMessages.get(Constants.DISPLAY_MESSAGES).isEmpty() ? postSave(false, false) : null;
    }

    public String approveGuestAccessCardApplication() {
        // The following if part is only needed because after the feature revision, there are still some applications where the user birthdate is not set.
        if (getAccessRequest().getBirthDate() == null) {
            getAccessRequest().setBirthDate(getAccessRequest().getUser().getBirthDate());
        }
        return save();
    }

    @Override
    public String createRedirectURL(String screen, Long redirectId, String tab, Map<String, String> fParams) {
        return super.createRedirectShowScreenURL(getAccessRequest().getUser(), "accessrequests", null);
    }

    @Produces
    @Named("accessRequest")
    public AccessRequest getAccessRequest() {
        return getInstance();
    }

    public String rejectAccessRequest() {
        Map<String, Set<String>> facesMessages = accessRequestService.rejectAccessRequest(getAccessRequest(), getConfiguration());
        printFacesMessagesClear(facesMessages);
        return !facesMessages.get(Constants.DISPLAY_MESSAGES).isEmpty() ? postSave(false, false) : null;
    }
}
