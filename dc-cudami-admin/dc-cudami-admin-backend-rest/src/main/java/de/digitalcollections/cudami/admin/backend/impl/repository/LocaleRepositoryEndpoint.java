package de.digitalcollections.cudami.admin.backend.impl.repository;

import feign.RequestLine;
import java.util.List;

public interface LocaleRepositoryEndpoint {

  @RequestLine("GET /v1/locales")
  List<String> find();

  @RequestLine("GET /v1/locales/default")
  String getDefault();
}
