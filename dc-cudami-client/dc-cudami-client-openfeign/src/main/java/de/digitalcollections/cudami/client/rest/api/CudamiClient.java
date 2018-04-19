package de.digitalcollections.cudami.client.rest.api;

import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;

public interface CudamiClient extends Client {

  List<Locale> getAllLocales() throws Exception;

  Locale getDefaultLocale() throws Exception;

  Webpage getWebpage(String uuid);

  Webpage getWebpage(Locale locale, String uuid);

  Website getWebsite(String uuid);

}
