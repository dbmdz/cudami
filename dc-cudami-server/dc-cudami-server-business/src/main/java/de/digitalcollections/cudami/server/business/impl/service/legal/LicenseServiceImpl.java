package de.digitalcollections.cudami.server.business.impl.service.legal;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.legal.LicenseRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.legal.LicenseService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LicenseServiceImpl extends UniqueObjectServiceImpl<License, LicenseRepository>
    implements LicenseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseServiceImpl.class);

  public LicenseServiceImpl(LicenseRepository repository) {
    super(repository);
  }

  @Override
  public void deleteByUrl(URL url) throws ServiceException {
    try {
      repository.deleteByUrl(url);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public License getByUrl(URL url) throws ServiceException {
    try {
      return repository.getByUrl(url);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguages() throws ServiceException {
    try {
      return repository.getLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "url");
      pageRequest.setSorting(sorting);
    }
  }
}
