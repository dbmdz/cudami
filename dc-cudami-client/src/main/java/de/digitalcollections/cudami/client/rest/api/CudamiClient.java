package de.digitalcollections.cudami.client.rest.api;

import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import java.util.List;
import java.util.Locale;

public interface CudamiClient extends Client {

  List<Locale> getAllLocales() throws Exception;

  Locale getDefaultLocale() throws Exception;

  String getWebpage(String uuid);

  Website getWebsite(String uuid);

}
