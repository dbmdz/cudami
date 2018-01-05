package de.digitalcollections.cudami.client.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.client.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.model.api.identifiable.resource.Resource;

public interface ResourceRepository<R extends Resource> extends IdentifiableRepository<R> {

}
