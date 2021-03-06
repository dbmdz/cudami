package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl extends EntityServiceImpl<Item> implements ItemService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

  @Autowired
  public ItemServiceImpl(ItemRepository repository) {
    super(repository);
  }

  @Override
  public boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid) {
    return ((ItemRepository) repository).addDigitalObject(itemUuid, digitalObjectUuid);
  }

  @Override
  public boolean addWork(UUID itemUuid, UUID workUuid) {
    return ((ItemRepository) repository).addWork(itemUuid, workUuid);
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID itemUuid) {
    return ((ItemRepository) repository).getDigitalObjects(itemUuid);
  }

  @Override
  public Set<Work> getWorks(UUID itemUuid) {
    return ((ItemRepository) repository).getWorks(itemUuid);
  }
}
