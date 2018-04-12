package de.digitalcollections.cudami.client.repository;

import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;

public interface CudamiRepository {

  public Locale getDefaultLocale() throws Exception;

  public List<Locale> getAllLocales() throws Exception;

  public Webpage getWebpage(String uuid) throws Exception;

  public Website getWebsite(String uuid) throws Exception;
}
