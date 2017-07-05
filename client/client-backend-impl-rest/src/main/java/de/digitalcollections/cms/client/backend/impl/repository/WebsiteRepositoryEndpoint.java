package de.digitalcollections.cms.client.backend.impl.repository;

import de.digitalcollections.cms.model.api.entity.Website;
import feign.RequestLine;
import java.util.List;

public interface WebsiteRepositoryEndpoint {

  @RequestLine("GET /website/v1/websites")
  List<Website> findAll();
}
