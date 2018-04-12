package de.digitalcollections.cudami.client.business;

import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;

public interface CudamiService {

  public Locale getDefaultLocale() throws CudamiException;

  public List<Locale> getAllLocales() throws CudamiException;

  public Webpage getWebpage(String uuid) throws CudamiException;

  public Website getWebsite(String uuid) throws CudamiException;

}
