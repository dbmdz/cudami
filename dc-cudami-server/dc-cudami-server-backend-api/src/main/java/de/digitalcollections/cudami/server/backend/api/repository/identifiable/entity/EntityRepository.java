package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.Entity;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;

public interface EntityRepository<E extends Entity> extends IdentifiableRepository<E> {

}
