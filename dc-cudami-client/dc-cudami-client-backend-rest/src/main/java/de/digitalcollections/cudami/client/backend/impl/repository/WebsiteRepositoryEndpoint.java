package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.cudami.model.impl.entity.WebsiteImpl;
import feign.RequestLine;
import java.util.List;

public interface WebsiteRepositoryEndpoint {

  @RequestLine("GET /website/v1/websites")
  List<WebsiteImpl> findAll();
}
