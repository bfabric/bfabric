package org.bfabric.list;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.ViewService;
import org.bfabric.service.util.BfabricLazyDataModel;

@Named
@ViewScoped
public class ViewList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ViewService viewService;

    @CachedMethodResult
    public BfabricLazyDataModel<Object> getEntityCountOverview() {
        return viewService.getViewResult("entity_count_overview");
    }

    @CachedMethodResult
    public BfabricLazyDataModel<Object> getView(String viewName) {
        return viewService.getViewResult(viewName);
    }
}