package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;

public interface ManifestationService extends EntityService<Manifestation> {

  PageResponse<Manifestation> findManifestationsByWork(Work work, PageRequest pageRequest)
      throws ServiceException;

  PageResponse<Manifestation> findSubParts(Manifestation manifestation, PageRequest pageRequest)
      throws ServiceException;

  List<Locale> getLanguagesOfManifestationsForWork(Work work) throws ServiceException;

  boolean removeParent(Manifestation manifestation, Manifestation parentManifestation)
      throws ServiceException;
}
