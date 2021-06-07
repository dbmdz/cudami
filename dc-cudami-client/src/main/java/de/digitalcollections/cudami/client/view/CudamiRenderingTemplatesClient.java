package de.digitalcollections.cudami.client.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.view.RenderingTemplate;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiRenderingTemplatesClient extends CudamiBaseClient<RenderingTemplate> {
  public CudamiRenderingTemplatesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, RenderingTemplate.class, mapper);
  }

  public RenderingTemplate create() {
    return new RenderingTemplate();
  }

  public PageResponse<RenderingTemplate> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/renderingtemplates", pageRequest);
  }

  public RenderingTemplate findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/renderingtemplates/%s", uuid));
  }

  public RenderingTemplate save(RenderingTemplate template) throws HttpException {
    return doPostRequestForObject("/v5/renderingtemplates", template);
  }

  public RenderingTemplate update(UUID uuid, RenderingTemplate template) throws HttpException {
    return doPutRequestForObject(String.format("/v5/renderingtemplates/%s", uuid), template);
  }
}
