package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;

/**
 * Repository for Digital object persistence handling.
 *
 * @param <D> digital object instance
 */
public interface DigitalObjectRepository<D extends DigitalObject> extends EntityRepository<D> {

}
