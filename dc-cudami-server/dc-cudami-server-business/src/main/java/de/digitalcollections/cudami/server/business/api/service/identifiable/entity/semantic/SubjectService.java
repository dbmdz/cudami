package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.semantic.Subject;

public interface SubjectService extends UniqueObjectService<Subject> {

  Subject getByTypeAndIdentifier(String type, Identifier identifier) throws ServiceException;
}
