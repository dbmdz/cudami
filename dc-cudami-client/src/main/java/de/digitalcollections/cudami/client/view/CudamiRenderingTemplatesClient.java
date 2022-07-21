package de.digitalcollections.cudami.client.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.view.RenderingTemplate;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiRenderingTemplatesClient extends CudamiRestClient<RenderingTemplate> {

  public CudamiRenderingTemplatesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(
        http,
        serverUrl,
        RenderingTemplate.class,
        mapper,
        API_VERSION_PREFIX + "/renderingtemplates");
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return this.doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }
}
