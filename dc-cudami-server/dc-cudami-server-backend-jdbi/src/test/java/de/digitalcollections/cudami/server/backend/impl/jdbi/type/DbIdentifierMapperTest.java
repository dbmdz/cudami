package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.identifiable.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The DbIdentiferMapper")
class DbIdentifierMapperTest {

  private DbIdentifierMapper dbIdentifierMapper;

  @BeforeEach
  public void beforeEach() {
    dbIdentifierMapper = new DbIdentifierMapper();
  }

  @DisplayName("formats the entry correctly")
  @Test
  public void formatEntry() {
    Identifier identifier = Identifier.builder().namespace("the-namespace").id("the-id").build();
    String expected = "(\"the-namespace\",\"the-id\")";
    assertThat((String) dbIdentifierMapper.convertArrayElement(identifier)).isEqualTo(expected);
  }
}
