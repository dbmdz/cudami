package de.digitalcollections.cudami.server.business.impl.service.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.TagService;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TagServiceImpl implements TagService {

  private final TagRepository repository;

  public TagServiceImpl(TagRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public Tag getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public Tag save(Tag tag) {
    return repository.save(tag);
  }

  @Override
  public Tag update(Tag tag) {
    return repository.update(tag);
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    return repository.delete(uuids);
  }

  @Override
  public PageResponse<Tag> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public Tag getByTagTypeAndIdentifier(String tagType, String namespace, String id)
      throws CudamiServiceException {
    try {
      return repository.getByTagTypeAndIdentifier(tagType, namespace, id);
    } catch (Exception e) {
      throw new CudamiServiceException(
          "cannot get by tagType=" + tagType + ", namespace=" + namespace + ", id=" + id + ": " + e,
          e);
    }
  }
}
