package de.digitalcollections.cudami.server.business.impl.service.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.TagService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TagServiceImpl extends UniqueObjectServiceImpl<Tag, TagRepository>
    implements TagService {

  public TagServiceImpl(TagRepository repository) {
    super(repository);
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
  public PageResponse<Tag> find(PageRequest pageRequest) {
    return super.find(pageRequest);
  }

  @Override
  public Tag getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public Tag getByValue(String value) throws ServiceException {
    try {
      return repository.getByValue(value);
    } catch (Exception e) {
      throw new ServiceException("cannot get by value=" + value + ": " + e, e);
    }
  }

  @Override
  public Tag save(Tag tag) {
    return repository.save(tag);
  }

  @Override
  public Tag update(Tag tag) {
    return repository.update(tag);
  }
}
