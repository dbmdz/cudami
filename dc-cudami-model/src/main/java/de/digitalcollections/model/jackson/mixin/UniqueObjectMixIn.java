package de.digitalcollections.model.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.security.User;
import de.digitalcollections.model.semantic.Headword;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.view.RenderingTemplate;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "objectType", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = Headword.class, name = "HEADWORD"),
  @JsonSubTypes.Type(value = Identifiable.class, name = "IDENTIFIABLE"),
  @JsonSubTypes.Type(value = Identifier.class, name = "IDENTIFIER"),
  @JsonSubTypes.Type(value = IdentifierType.class, name = "IDENTIFIER_TYPE"),
  @JsonSubTypes.Type(value = License.class, name = "LICENSE"),
  @JsonSubTypes.Type(value = Predicate.class, name = "PREDICATE"),
  @JsonSubTypes.Type(value = Publisher.class, name = "PUBLISHER"),
  @JsonSubTypes.Type(value = RenderingTemplate.class, name = "RENDERING_TEMPLATE"),
  @JsonSubTypes.Type(value = Tag.class, name = "TAG"),
  @JsonSubTypes.Type(value = UrlAlias.class, name = "URL_ALIAS"),
  @JsonSubTypes.Type(value = User.class, name = "USER")
})
@JsonInclude(value = Include.NON_EMPTY)
public interface UniqueObjectMixIn {
  @JsonIgnore
  boolean isPersisted();
}
