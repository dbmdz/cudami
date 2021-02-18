package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.identifiable.entity.Entity;
import java.util.List;

/** @param <E> entity instance */
public interface EntityService<E extends Entity> extends IdentifiableService<E> {

  E getByRefId(long refId);

  List<E> getRandom(int count);
}
