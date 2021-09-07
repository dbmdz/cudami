package de.digitalcollections.cudami.server.business.impl.service.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.HeadwordService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HeadwordServiceImpl implements HeadwordService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordServiceImpl.class);

  private final HeadwordRepository repository;

  public HeadwordServiceImpl(HeadwordRepository repository) {
    this.repository = repository;
  }

  @Override
  public void addRelatedEntity(UUID headwordUuid, UUID entityUuid) {
    repository.addRelatedEntity(headwordUuid, entityUuid);
  }

  @Override
  public void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid) {
    repository.addRelatedFileresource(headwordUuid, fileResourceUuid);
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    return repository.delete(uuids);
  }

  @Override
  public PageResponse<Headword> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return repository.find(pageRequest);
  }

  @Override
  public SearchPageResponse<Headword> find(SearchPageRequest searchPageRequest) {
    setDefaultSorting(searchPageRequest);
    return repository.find(searchPageRequest);
  }

  @Override
  public List<Headword> find(String searchTerm, int maxResults) {
    return repository.find(searchTerm, maxResults);
  }

  @Override
  public List<Headword> findAll() {
    return repository.findAll();
  }

  @Override
  public PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<Headword> result =
        repository.findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }

  @Override
  public Headword get(UUID uuid) {
    return repository.findOne(uuid);
  }

  @Override
  public List<Headword> get(String label, Locale locale) {
    return repository.find(label, locale);
  }

  @Override
  public List<Locale> getLanguages() {
    return repository.getLanguages();
  }

  @Override
  public List<Headword> getRandom(int count) {
    return repository.findRandom(count);
  }

  @Override
  public List<Entity> getRelatedEntities(UUID headwordUuid) {
    return repository.getRelatedEntities(headwordUuid);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID headwordUuid) {
    return repository.getRelatedFileResources(headwordUuid);
  }

  @Override
  public Headword save(Headword headword) throws ServiceException {
    try {
      return repository.save(headword);
    } catch (Exception e) {
      LOGGER.error("Cannot save headword " + headword + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }

  @Override
  public List<Entity> saveRelatedEntities(UUID headwordUuid, List<Entity> entities) {
    return repository.saveRelatedEntities(headwordUuid, entities);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID headwordUuid, List<FileResource> fileResources) {
    return repository.saveRelatedFileResources(headwordUuid, fileResources);
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      final Order labelOrder1 = new Order(Direction.ASC, "label");
      Sorting sorting = Sorting.defaultBuilder().order(labelOrder1).build();
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public Headword update(Headword headword) throws ServiceException {
    try {
      return repository.update(headword);
    } catch (Exception e) {
      LOGGER.error("Cannot update headword " + headword + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }
}
