package de.digitalcollections.cudami.client.repository;

import de.digitalcollections.cudami.client.rest.api.CudamiClient;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;
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
  public Locale getDefaultLocale() throws Exception {
    return cudamiClient.getDefaultLocale();
  }

  @Override
  public List<Locale> getAllLocales() throws Exception {
    return cudamiClient.getAllLocales();
  }

  @Override
  public Webpage getWebpage(String uuid) throws Exception {
    return cudamiClient.getWebpage(uuid);
  }

  @Override
  public Website getWebsite(String uuid) throws Exception {
    return cudamiClient.getWebsite(uuid);
  }
}
