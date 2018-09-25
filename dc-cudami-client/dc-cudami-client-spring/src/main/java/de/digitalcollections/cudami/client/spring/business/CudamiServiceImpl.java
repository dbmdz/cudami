package de.digitalcollections.cudami.client.spring.business;

import de.digitalcollections.cudami.client.spring.backend.CudamiRepository;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CudamiServiceImpl implements CudamiService {

  @Autowired
  private CudamiRepository repository;

  @Override
  public List<Locale> getAllLocales() throws CudamiException {
    try {
      return repository.getAllLocales();
    } catch (Exception e) {
      throw new CudamiException("Cannot get all locales: " + e.getMessage(), e);
    }
  }

  @Override
  public Locale getDefaultLocale() throws CudamiException {
    try {
      return repository.getDefaultLocale();
    } catch (Exception e) {
      throw new CudamiException("Cannot get default locale: " + e.getMessage(), e);
    }
  }

  @Override
  public Webpage getWebpage(UUID uuid) throws CudamiException {
    try {
      return repository.getWebpage(uuid);
    } catch (Exception e) {
      throw new CudamiException("Cannot get webpage with uuid=" + uuid + ": " + e.getMessage(), e);
    }
  }

  @Override
  public Webpage getWebpage(Locale locale, UUID uuid) throws CudamiException {
    try {
      return repository.getWebpage(locale, uuid);
    } catch (Exception e) {
      throw new CudamiException("Cannot get webpage with locale=" + locale + " and uuid=" + uuid + ": " + e.getMessage(), e);
    }
  }

  @Override
  public Webpage getWebpage(String uuid) throws CudamiException {
    try {
      return repository.getWebpage(uuid);
    } catch (Exception e) {
      throw new CudamiException("Cannot get webpage with uuid=" + uuid + ": " + e.getMessage(), e);
    }
  }

  @Override
  public Webpage getWebpage(Locale locale, String uuid) throws CudamiException {
    try {
      return repository.getWebpage(locale, uuid);
    } catch (Exception e) {
      throw new CudamiException("Cannot get webpage with locale=" + locale + " and uuid=" + uuid + ": " + e.getMessage(), e);
    }
  }

  @Override
  public Website getWebsite(String uuid) throws CudamiException {
    try {
      return repository.getWebsite(uuid);
    } catch (Exception e) {
      throw new CudamiException("Cannot get website with uuid=" + uuid + ": " + e.getMessage(), e);
    }
  }
}
