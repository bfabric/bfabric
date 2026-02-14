package org.bfabric.service;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.entity.Consumable;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.Supplier;
import org.bfabric.entity.User;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class SupplierService extends AbstractService {

    private static final long serialVersionUID = 1;

    public SupplierService() {
        super(Supplier.class);
    }

    public List<Supplier> getSupplierFiltered(String filterString, Collection<User> excluded) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addIdOrNameWhereClause(filterString);
        entityQuery.addNotInEntitiesClause(excluded);
        entityQuery.setOrder("name");
        return (List<Supplier>) entityQuery.getResultList();
    }

    public void merge(Supplier supplier, Supplier merged, Supplier mergeSelection) throws RollbackException {
        try {
            // Merge the attributes.
            supplier.setName(mergeSelection.getName());
            supplier.setCompanyId(mergeSelection.getCompanyId());
            supplier.setDescription(mergeSelection.getDescription());
            supplier.setAddress(mergeSelection.getAddress());
            supplier.setPhoneNumber(mergeSelection.getPhoneNumber());
            supplier.setEmail(mergeSelection.getEmail());
            supplier.setUrl(mergeSelection.getUrl());
            supplier.setContactTitle(mergeSelection.getContactTitle());
            supplier.setContactSalutation(mergeSelection.getContactSalutation());
            supplier.setContactFirstName(mergeSelection.getContactFirstName());
            supplier.setContactLastName(mergeSelection.getContactLastName());
            supplier.setContactPhoneNumber(mergeSelection.getContactPhoneNumber());
            supplier.setContactEmail(mergeSelection.getContactEmail());

            // Merge the one-to-many associations. IMPORTANT: Do this before merging the many-to-many associations.
            reassign(Consumable.class, merged, supplier, "supplier");
            reassign(Purchase.class, merged, supplier, "supplier");

            saveMerge(supplier, merged);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }

    public void reassign(Class<?> clazz, Supplier oldSupplier, Supplier newSupplier, String attribute) {
        if (clazz != null && oldSupplier != null && newSupplier != null && attribute != null) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE ").append(clazz.getSimpleName()).append(" SET ").append(attribute).append(" = :new WHERE ").append(attribute).append(" = :old");
            if (clazz.equals(Supplier.class)) {
                queryBuilder.append(" AND id <> :id");
            }

            Query query = createQuery(queryBuilder.toString()).setParameter("old", oldSupplier).setParameter("new", newSupplier);
            if (clazz.equals(Supplier.class)) {
                query.setParameter("id", oldSupplier.getId());
            }

            query.executeUpdate();
        }
    }
}
