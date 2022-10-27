package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jdbi.v3.core.array.SqlArrayType;

public class PublicationMapper implements SqlArrayType<Publisher> {

  @Override
  public String getTypeName() {
    return "publication";
  }

  @Override
  public Object convertArrayElement(Publisher element) {
    Predicate<List<?>> isNullOrEmpty = list -> list == null || list.isEmpty();
    return "("
        + (isNullOrEmpty.test(element.getLocations())
            ? "{}"
            : String.format("{%s}", commaSeparatedUuids(element.getLocations())))
        + (element.getAgent() == null ? ",null" : String.format(",%s", element.getAgent()))
        + (element.getPublisherPresentation() == null
            ? ",null"
            : String.format(",%s", element.getPublisherPresentation()))
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
