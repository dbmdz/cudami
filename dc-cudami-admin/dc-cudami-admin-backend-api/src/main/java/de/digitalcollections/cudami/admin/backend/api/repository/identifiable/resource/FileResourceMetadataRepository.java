package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;

public interface FileResourceMetadataRepository extends IdentifiableRepository<FileResource> {

  SearchPageResponse<FileResource> findImages(SearchPageRequest searchPageRequest);
}
