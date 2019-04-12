package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;

/**
 * Repository for Digital Object persistence handling.
 *
 * @param <D> digital object instance
 */
public interface DigitalObjectRepository<D extends DigitalObject> extends EntityRepository<D> {

}
