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

package org.bfabric.webservice.server.manager;

import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Division;
import org.bfabric.entity.Institute;
import org.bfabric.entity.User;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFUser;
import org.bfabric.service.AffiliationHelperService;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUser;
import org.bfabric.xml.entity.XMLUser;

@Named
public class WSUserManager extends AbstractWSEntityManager<User, XMLUser> {

    @Inject
    private AffiliationHelperService affiliationHelperService;

    @Inject
    private UserService userService;

    /** {@inheritDoc} */
    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFUser(getInstance(), (XMLRequestParameterSaveUser) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFUser(getInstance(), (XMLRequestParameterSaveUser) aXmlRequestSaveEntity);
    }

    @Override
    public void save() {
        if (StringHelper.isNotEmpty(getInstance().getDivisionName())) {
            Division division = affiliationHelperService.saveDivisionIfNotExists(getInstance().getOrganizationType(), getInstance().getCompanyName(), getInstance().getDivisionName());
            getInstance().setDivision(division);
        } else if (StringHelper.isNotEmpty(getInstance().getInstituteName())) {
            Institute institute = affiliationHelperService.saveInstituteIfNotExists(getInstance().getOrganizationType(), getInstance().getOrganizationName(), getInstance().getDepartmentName(), getInstance().getInstituteName());
            getInstance().setInstitute(institute);
        }
        userService.save(getInstance());
    }
}
