package de.digitalcollections.cudami.admin.backend.impl.repository;

import feign.RequestLine;
import java.util.List;

public interface LocaleRepositoryEndpoint {

  @RequestLine("GET /latest/locales")
  List<String> find();

  @RequestLine("GET /latest/locales/default")
  String getDefault();
}
