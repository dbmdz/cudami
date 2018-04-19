package de.digitalcollections.cudami.client.spring.business;

import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface CudamiService {

  public List<Locale> getAllLocales() throws CudamiException;

  public Locale getDefaultLocale() throws CudamiException;

  public Webpage getWebpage(UUID uuid) throws CudamiException;

  public Webpage getWebpage(String uuid) throws CudamiException;

  public Webpage getWebpage(Locale locale, UUID uuid) throws CudamiException;

  public Webpage getWebpage(Locale locale, String uuid) throws CudamiException;

  public Website getWebsite(String uuid) throws CudamiException;

}
