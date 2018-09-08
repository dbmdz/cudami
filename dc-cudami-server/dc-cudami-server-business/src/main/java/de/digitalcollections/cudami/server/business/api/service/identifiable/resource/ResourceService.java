package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import java.util.Locale;
import java.util.UUID;

public interface ResourceService<R extends Resource> extends IdentifiableService<R> {

  R get(UUID uuid, Locale locale) throws IdentifiableServiceException;
}
