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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Storage;
import org.bfabric.entity.Workunit;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFHelper;
import org.bfabric.forms.MFResource;
import org.bfabric.forms.MFWorkunit;
import org.bfabric.forms.MFWorkunitChangeStatus;
import org.bfabric.forms.MFWorkunitCheckAndInsert;
import org.bfabric.service.ResourceService;
import org.bfabric.service.WorkunitService;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterChangeWorkunitStatus;
import org.bfabric.webservice.request.parameter.XMLRequestParameterCheckAndInsertWorkunit;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInputResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveResource;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkunit;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLWorkunit;

@Named
public class WSWorkunitManager extends AbstractWSEntityManager<Workunit, XMLWorkunit> {

    @Inject
    private ResourceService resourceService;

    @Inject
    private WorkunitService workunitService;

    private static String getWorkunitAttributeDesc(XMLRequestParameterCheckAndInsertWorkunit xmlRequestCheckAndInsertWorkunit) {
        return "name: " + xmlRequestCheckAndInsertWorkunit.getName() + "; " + "applicationid: " +
            xmlRequestCheckAndInsertWorkunit.getApplicationid() + "; " + "containerid: " + xmlRequestCheckAndInsertWorkunit.getContainerid() + ". ";
    }

    public XMLResponse changeStatus(List<XMLRequestParameterChangeWorkunitStatus> xmlRequestChangeStatusWorkunits) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterChangeWorkunitStatus xmlRequestChangeStatusWorkunit : xmlRequestChangeStatusWorkunits) {
            XMLWorkunit xmlWorkunit;
            try {
                performEntityCheckAndSetInstance(xmlRequestChangeStatusWorkunit.getId());
                MFWorkunitChangeStatus mf = new MFWorkunitChangeStatus(getInstance(), xmlRequestChangeStatusWorkunit);
                mf.apply();
                workunitService.save(getInstance(), false, false);
                getInstance().setIndexDependents(true);
                xmlWorkunit = new XMLWorkunit(getInstance());
            } catch (Exception e) {
                xmlWorkunit = new XMLWorkunit();
                xmlWorkunit.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
            }
            xmlResponse.add(xmlWorkunit);
        }
        indexEntities();
        return xmlResponse;
    }

    public XMLResponse checkAndInsert(List<XMLRequestParameterCheckAndInsertWorkunit> xmlRequestCheckAndInsertWorkunits) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterCheckAndInsertWorkunit xmlRequestCheckAndInsertWorkunit : xmlRequestCheckAndInsertWorkunits) {
            XMLWorkunit xmlWorkunit;
            try {
                // make sure that one or more input resources were specified
                if (xmlRequestCheckAndInsertWorkunit.getResource().isEmpty()) {
                    throw new InvalidDataException("No resources specified for the workunit with following attributes: " + getWorkunitAttributeDesc(xmlRequestCheckAndInsertWorkunit));
                }
                // make sure that one or more input resources were specified
                if (xmlRequestCheckAndInsertWorkunit.getInputResource().isEmpty()) {
                    throw new InvalidDataException("No input resources specified for the workunit with following attributes: " + getWorkunitAttributeDesc(xmlRequestCheckAndInsertWorkunit));
                }
                // make sure that there is no other resource already imported with the same relativepath for the given combination of storageid, containerid, and applicationid.
                for (XMLRequestParameterSaveResource xmlRequestSaveResource : xmlRequestCheckAndInsertWorkunit.getResource()) {
                    if (!resourceService.checkUniqueImport(xmlRequestSaveResource.getRelativepath(), Long.parseLong(xmlRequestSaveResource.getStorageid()), Long
                        .parseLong(xmlRequestCheckAndInsertWorkunit.getContainerid()), Long.parseLong(xmlRequestCheckAndInsertWorkunit.getApplicationid()))) {
                        throw new InvalidDataException("There already exists a resource with the following attributes: relativepath=" + xmlRequestSaveResource.getRelativepath() +
                            " storageid=" + xmlRequestSaveResource.getStorageid() + " containerid=" + xmlRequestCheckAndInsertWorkunit.getContainerid() +
                            " applicationid=" + xmlRequestCheckAndInsertWorkunit.getApplicationid());
                    }
                }

                setInstance(new Workunit());
                AbstractMF mf = new MFWorkunitCheckAndInsert(getInstance(), xmlRequestCheckAndInsertWorkunit);
                mf.apply();

                Set<Resource> selectedInputResources = new HashSet<>();
                for (XMLRequestParameterSaveInputResource xmlRequestSaveInputResource : xmlRequestCheckAndInsertWorkunit.getInputResource()) {
                    MFHelper.checkNotNull("relativepath", xmlRequestSaveInputResource.getRelativepath());
                    Storage storage = (Storage) wsService.fetch(Storage.class, MFHelper.positiveLongValueOf("storageid", xmlRequestSaveInputResource.getStorageid()));

                    List<Resource> fetchedInputResources = resourceService.getResourcesByContainerAndRelativePathAndStorage(getInstance().getContainer(), xmlRequestSaveInputResource.getRelativepath(),
                        storage);

                    if (fetchedInputResources.isEmpty()) {
                        throw new InvalidDataException("Resource(s) with the specified attribute combination not found: storage id: " + storage.getId() + " relative path: "
                            + xmlRequestSaveInputResource.getRelativepath());
                    }

                    selectedInputResources.addAll(fetchedInputResources);
                }
                Set<Resource> selectedResources = new HashSet<>();
                for (XMLRequestParameterSaveResource xmlRequestSaveResource : xmlRequestCheckAndInsertWorkunit.getResource()) {
                    Resource resource = new Resource();
                    resource.setWorkunit(getInstance());

                    mf = new MFResource(resource, xmlRequestSaveResource);
                    mf.apply();

                    // If resource status is not explicitly set, then set it by default to available.
                    if (xmlRequestSaveResource.getStatus() == null) {
                        resource.setAvailable();
                    }

                    handleValidationErrors(resourceService.isValid(resource));
                    selectedResources.add(resource);
                }

                // Save Workunit.
                workunitService.save(getInstance(), selectedResources, selectedInputResources, null, null, false);
                xmlWorkunit = new XMLWorkunit(getInstance());
                performEntityCheckAndSetInstance(getInstance().getId());
                addIndexableEntities(getInstance().getResources());
            } catch (Exception e) {
                xmlWorkunit = new XMLWorkunit();
                xmlWorkunit.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
            }

            xmlResponse.add(xmlWorkunit);
        }
        indexEntities();
        return xmlResponse;
    }

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFWorkunit<>(getInstance(), (XMLRequestParameterSaveWorkunit) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFWorkunit<>(getInstance(), (XMLRequestParameterSaveWorkunit) aXmlRequestSaveEntity);
    }

    @Override
    public void save() {
        // System.out.println("save workunit: baseurl=" + getConfiguration().getBaseUrl());
        workunitService.save(getInstance(), false, false);
        if (getInstance().getDataset() != null) {
            getInstance().getDataset().setLogEntity(false);
            wsService.save(getInstance().getDataset(), true);
        }
    }
}