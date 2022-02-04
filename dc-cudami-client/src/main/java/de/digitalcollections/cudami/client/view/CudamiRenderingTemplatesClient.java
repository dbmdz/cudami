package de.digitalcollections.cudami.client.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.view.RenderingTemplate;
import java.net.http.HttpClient;

public class CudamiRenderingTemplatesClient extends CudamiRestClient<RenderingTemplate> {

  public CudamiRenderingTemplatesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, RenderingTemplate.class, mapper, "/v5/renderingtemplates");
  }
}
