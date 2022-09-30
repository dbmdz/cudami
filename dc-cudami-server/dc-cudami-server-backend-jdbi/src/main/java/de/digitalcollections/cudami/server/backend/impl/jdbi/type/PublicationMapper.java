package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.entity.work.Publication;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jdbi.v3.core.array.SqlArrayType;

public class PublicationMapper implements SqlArrayType<Publication> {

  @Override
  public String getTypeName() {
    return "publication";
  }

  @Override
  public Object convertArrayElement(Publication element) {
    Predicate<List<?>> isNullOrEmpty = list -> list == null || list.isEmpty();
    return "("
        + (isNullOrEmpty.test(element.getPublicationLocations())
            ? ""
            : String.format("{%s}", commaSeparatedUuids(element.getPublicationLocations())))
        + (isNullOrEmpty.test(element.getPublishers())
            ? ","
            : String.format(",{%s}", commaSeparatedUuids(element.getPublishers())))
        + (isNullOrEmpty.test(element.getPublishersPresentation())
            ? ","
            : String.format(",{%s}", commaSeparatedStrings(element.getPublishersPresentation())))
        + ")";
  }

  private String commaSeparatedUuids(Collection<? extends Identifiable> list) {
    return list.stream()
        .map(identifiable -> identifiable.getUuid().toString())
        .collect(Collectors.joining(","));
  }

  private String commaSeparatedStrings(Collection<String> list) {
    return list.stream()
        .map(s -> String.format("\"%s\"", s.replaceAll("['\"]", "$0$0")))
        .collect(Collectors.joining(","));
  }
}
