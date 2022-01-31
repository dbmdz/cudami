package de.digitalcollections.cudami.server.business.impl.service.legal;

import de.digitalcollections.cudami.server.backend.api.repository.legal.LicenseRepository;
import de.digitalcollections.cudami.server.business.api.service.legal.LicenseService;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LicenseServiceImpl implements LicenseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseServiceImpl.class);

  private final LicenseRepository repository;

  public LicenseServiceImpl(LicenseRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public void deleteByUrl(String url) {
    repository.deleteByUrl(url);
  }

  @Override
  public void deleteByUuid(UUID uuid) {
    repository.deleteByUuid(uuid);
  }

  @Override
  public void deleteByUuids(List<UUID> uuids) {
    repository.deleteByUuids(uuids);
  }

  @Override
  public PageResponse<License> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public List<License> findAll() {
    return repository.findAll();
  }

  @Override
  public License getByUrl(URL url) {
    return repository.getByUrl(url);
  }

  @Override
  public License getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public License save(License license) {
    return repository.save(license);
  }

  @Override
  public License update(License license) {
    return repository.update(license);
  }
}
