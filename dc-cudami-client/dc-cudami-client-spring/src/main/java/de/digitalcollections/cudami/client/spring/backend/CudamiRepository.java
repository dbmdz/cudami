package de.digitalcollections.cudami.client.spring.backend;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface CudamiRepository {

  public List<Locale> getAllLocales() throws Exception;

  public Locale getDefaultLocale() throws Exception;

  public Webpage getWebpage(UUID uuid) throws Exception;

  public Webpage getWebpage(Locale locale, UUID uuid) throws Exception;

  public Webpage getWebpage(String uuid) throws Exception;

  public Webpage getWebpage(Locale locale, String uuid) throws Exception;

  public Website getWebsite(String uuid) throws Exception;
}
