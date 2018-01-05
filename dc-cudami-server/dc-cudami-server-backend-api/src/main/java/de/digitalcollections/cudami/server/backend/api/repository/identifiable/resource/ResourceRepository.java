package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.resource.Resource;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;

public interface ResourceRepository<R extends Resource> extends IdentifiableRepository<R> {

}
