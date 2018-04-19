package de.digitalcollections.cudami.client.spring.backend;

import de.digitalcollections.cudami.client.feign.api.CudamiClient;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CudamiRepositoryImpl implements CudamiRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiRepositoryImpl.class);

  @Autowired
  private CudamiClient cudamiClient;

  @Override
  public List<Locale> getAllLocales() throws Exception {
    return cudamiClient.getAllLocales();
  }

  @Override
  public Locale getDefaultLocale() throws Exception {
    return cudamiClient.getDefaultLocale();
  }

  @Override
  public Webpage getWebpage(UUID uuid) throws Exception {
    return cudamiClient.getWebpage(uuid.toString());
  }

  @Override
  public Webpage getWebpage(Locale locale, UUID uuid) throws Exception {
    return cudamiClient.getWebpage(locale, uuid.toString());
  }

  @Override
  public Webpage getWebpage(String uuid) throws Exception {
    return cudamiClient.getWebpage(uuid);
  }

  @Override
  public Webpage getWebpage(Locale locale, String uuid) throws Exception {
    return cudamiClient.getWebpage(locale, uuid);
  }

  @Override
  public Website getWebsite(String uuid) throws Exception {
    return cudamiClient.getWebsite(uuid);
  }
}
