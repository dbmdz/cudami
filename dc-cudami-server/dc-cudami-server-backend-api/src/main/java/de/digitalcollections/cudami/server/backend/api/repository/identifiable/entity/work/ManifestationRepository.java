package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface ManifestationRepository extends EntityRepository<Manifestation> {

  PageResponse<Manifestation> findManifestationsByWork(UUID workUuid, PageRequest pageRequest)
      throws RepositoryException;

  // TODO: documentation: no parent - child relation; is modelled as
  // List<RelationSpecification<Manifestation>> parents ("partOf");
  // so maybe rename to "findSubParts"? (volumes of a multi-volume manifestation like lexika)
  PageResponse<Manifestation> findSubParts(UUID uuid, PageRequest pageRequest)
      throws RepositoryException;

  List<Locale> getLanguagesOfManifestationsForWork(UUID workUuid);
}
