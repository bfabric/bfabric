package org.bfabric.list;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Supplier;
import org.bfabric.service.SupplierService;

@Named
@ViewScoped
public class SupplierList extends AbstractList<Supplier> {

    private static final long serialVersionUID = 1;

    @Inject
    private SupplierService SupplierService;

    @Override
    protected SupplierService getService() {
        return SupplierService;
    }
}
