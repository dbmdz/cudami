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

  @DisplayName("can handle a null value from the database")
  @Test
  public void handleNullValue() {
    assertThat(dbIdentifierMapper.extractIdentifier(null)).isNull();
  }

  @DisplayName("can handle an empty value from the database")
  @Test
  public void handleEmptyValue() {
    assertThat(dbIdentifierMapper.extractIdentifier("")).isNull();
  }

  @DisplayName("can handle an empty dataset from the database")
  @Test
  public void handleEmptyDataset() {
    assertThat(dbIdentifierMapper.extractIdentifier("()")).isNull();
  }

  @DisplayName("rejects an empty filled identifier")
  @Test
  public void handleEmptyFilled() {
    assertThat(dbIdentifierMapper.extractIdentifier("('','')")).isNull();
  }

  @DisplayName("rejects a partially filled identifier (only namespace)")
  @Test
  public void partialFillOnlyNamespace() {
    assertThat(dbIdentifierMapper.extractIdentifier("('namespace','')")).isNull();
  }

  @DisplayName("rejects a partially filled identifier (only id)")
  @Test
  public void partialFillOnlyId() {
    assertThat(dbIdentifierMapper.extractIdentifier("('','id')")).isNull();
  }

  @DisplayName("can fill an identifier")
  @Test
  public void fillIdentifier() {
    Identifier expected = Identifier.builder().namespace("name,space").id("id'd").build();
    assertThat(dbIdentifierMapper.extractIdentifier("('name,space','id'd')")).isEqualTo(expected);
  }
}
