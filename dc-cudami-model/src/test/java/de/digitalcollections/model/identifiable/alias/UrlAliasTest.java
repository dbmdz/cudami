package de.digitalcollections.model.identifiable.alias;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The UrlAlias")
class UrlAliasTest {

  @Test
  @DisplayName("fills the field target.targetIdentifiableObjectType")
  void fillsTargetIdentifiableObjectType() {
    UrlAlias urlAlias = UrlAlias.builder().target(Collection.builder().build()).build();
    assertThat(urlAlias.getTargetIdentifiableObjectType())
        .isEqualTo(IdentifiableObjectType.COLLECTION);
  }

  @Test
  @DisplayName("fills the field target.targetUuid")
  void fillsTargetUuid() {
    UrlAlias urlAlias = UrlAlias.builder().target(Collection.builder().build()).build();
    assertThat(urlAlias.getTargetIdentifiableObjectType())
        .isEqualTo(IdentifiableObjectType.COLLECTION);
  }
}
