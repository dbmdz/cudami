package de.digitalcollections.cudami.client.business;

import de.digitalcollections.cudami.client.repository.CudamiRepository;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CudamiServiceImpl implements CudamiService {

  @Autowired
  private CudamiRepository repository;

  @Override
  public Locale getDefaultLocale() throws CudamiException {
    try {
      return repository.getDefaultLocale();
    } catch ( Exception e ) {
      throw new CudamiException("Cannot get default locale: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Locale> getAllLocales() throws CudamiException {
    try {
      return repository.getAllLocales();
    } catch ( Exception e ) {
      throw new CudamiException("Cannot get all locales: " + e.getMessage(), e);
    }
  }

  @Override
  public Webpage getWebpage(String uuid) throws CudamiException {
    try {
      return repository.getWebpage(uuid);
    } catch ( Exception e ) {
      throw new CudamiException("Cannot get webpage with uuid=" + uuid + ": " + e.getMessage(), e);
    }
  }

  @Override
  public Website getWebsite(String uuid) throws CudamiException {
    try {
      return repository.getWebsite(uuid);
    } catch ( Exception e ) {
      throw new CudamiException("Cannot get website with uuid=" + uuid + ": " + e.getMessage(), e);
    }
  }
}
